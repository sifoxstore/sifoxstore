-- ==============================================================================
-- SIFOX STORE — SUPABASE DATABASE SCHEMA & RPC FUNCTIONS
-- Full PostgreSQL schema with RLS, auth profile triggers, and checkout RPCs
-- Target: Bangladesh E-commerce (Fixed Dhaka ৳80 / Outside Dhaka ৳140)
-- ==============================================================================

-- 1. Enable UUID Extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. ENUM TYPES
DO $$ BEGIN
    CREATE TYPE user_role_type AS ENUM ('customer', 'staff', 'admin', 'owner');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE delivery_zone_type AS ENUM ('inside_dhaka', 'outside_dhaka');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE payment_method_type AS ENUM ('cod', 'bkash', 'nagad');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE order_status_type AS ENUM ('pending', 'confirmed', 'processing', 'shipped', 'delivered', 'cancelled');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- 3. PROFILES TABLE (Linked with Supabase auth.users)
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT,
    full_name TEXT,
    phone TEXT,
    role user_role_type DEFAULT 'customer',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Trigger to automatically create a profile row when a new user signs up
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, email, full_name, phone, role)
    VALUES (
        NEW.id,
        NEW.email,
        COALESCE(NEW.raw_user_meta_data->>'full_name', ''),
        COALESCE(NEW.raw_user_meta_data->>'phone', ''),
        'customer'
    )
    ON CONFLICT (id) DO NOTHING;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- 4. CATEGORIES TABLE
CREATE TABLE IF NOT EXISTS public.categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL UNIQUE,
    slug TEXT NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 5. PRODUCTS TABLE
CREATE TABLE IF NOT EXISTS public.products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    category_id UUID REFERENCES public.categories(id) ON DELETE SET NULL,
    name TEXT NOT NULL,
    description TEXT,
    price NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    discount_price NUMERIC(12, 2) CHECK (discount_price IS NULL OR discount_price >= 0),
    stock INT NOT NULL DEFAULT 0 CHECK (stock >= 0),
    image_url TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 6. ORDERS TABLE
CREATE TABLE IF NOT EXISTS public.orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_number TEXT NOT NULL UNIQUE,
    user_id UUID REFERENCES auth.users(id) ON DELETE SET NULL,
    customer_name TEXT NOT NULL,
    phone TEXT NOT NULL,
    alt_phone TEXT,
    district TEXT NOT NULL,
    thana TEXT NOT NULL,
    area TEXT NOT NULL,
    address TEXT NOT NULL,
    delivery_zone delivery_zone_type NOT NULL,
    delivery_fee NUMERIC(8, 2) NOT NULL,
    subtotal_amount NUMERIC(12, 2) NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL,
    payment_method payment_method_type NOT NULL,
    transaction_id TEXT,
    notes TEXT,
    status order_status_type DEFAULT 'pending',
    tracking_number TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 7. ORDER ITEMS TABLE
CREATE TABLE IF NOT EXISTS public.order_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL REFERENCES public.orders(id) ON DELETE CASCADE,
    product_id UUID REFERENCES public.products(id) ON DELETE SET NULL,
    product_name TEXT NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    subtotal NUMERIC(12, 2) NOT NULL
);

-- 8. CHECKOUT RPC FUNCTION: place_order
-- Calculates exact delivery fee (৳80 inside Dhaka / ৳140 outside Dhaka), verifies stock & prices server-side
CREATE OR REPLACE FUNCTION public.place_order(
    p_customer_name TEXT,
    p_phone TEXT,
    p_alt_phone TEXT,
    p_district TEXT,
    p_thana TEXT,
    p_area TEXT,
    p_address TEXT,
    p_delivery_zone TEXT,
    p_payment_method TEXT,
    p_transaction_id TEXT,
    p_notes TEXT,
    p_items JSONB
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_order_id UUID;
    v_order_number TEXT;
    v_delivery_fee NUMERIC(8, 2);
    v_subtotal NUMERIC(12, 2) := 0;
    v_total NUMERIC(12, 2);
    v_item JSONB;
    v_product RECORD;
    v_qty INT;
    v_price NUMERIC(12, 2);
    v_item_subtotal NUMERIC(12, 2);
    v_user_id UUID;
BEGIN
    -- Validate items list
    IF p_items IS NULL OR jsonb_array_length(p_items) = 0 THEN
        RAISE EXCEPTION 'কার্ট খালি, কোনো পণ্য নির্বাচন করা হয়নি।';
    END IF;

    -- Compute verified delivery charge
    IF LOWER(p_delivery_zone) = 'inside_dhaka' THEN
        v_delivery_fee := 80.00;
    ELSE
        v_delivery_fee := 140.00;
    END IF;

    -- Generate Unique Order Number e.g. SF-74921
    v_order_number := 'SF-' || LPAD(FLOOR(RANDOM() * 90000 + 10000)::TEXT, 5, '0');

    -- Try to capture auth user if logged in
    v_user_id := auth.uid();

    -- Calculate verified subtotal from actual database prices
    FOR v_item IN SELECT * FROM jsonb_array_elements(p_items)
    LOOP
        v_qty := (v_item->>'quantity')::INT;
        IF v_qty <= 0 THEN
            RAISE EXCEPTION 'অকার্যকর পণ্যের পরিমাণ।';
        END IF;

        SELECT id, name, price, discount_price, stock
        INTO v_product
        FROM public.products
        WHERE id = (v_item->>'product_id')::UUID AND is_active = TRUE;

        IF NOT FOUND THEN
            RAISE EXCEPTION 'পণ্য পাওয়া যায়নি অথবা বর্তমানে বিক্রয় বন্ধ রয়েছে।';
        END IF;

        IF v_product.stock < v_qty THEN
            RAISE EXCEPTION '% পণ্যের পর্যাপ্ত স্টক নেই (বর্তমান স্টক: %)', v_product.name, v_product.stock;
        END IF;

        v_price := COALESCE(v_product.discount_price, v_product.price);
        v_subtotal := v_subtotal + (v_price * v_qty);
    END LOOP;

    v_total := v_subtotal + v_delivery_fee;

    -- Insert into orders table
    INSERT INTO public.orders (
        order_number,
        user_id,
        customer_name,
        phone,
        alt_phone,
        district,
        thana,
        area,
        address,
        delivery_zone,
        delivery_fee,
        subtotal_amount,
        total_amount,
        payment_method,
        transaction_id,
        notes,
        status
    )
    VALUES (
        v_order_number,
        v_user_id,
        p_customer_name,
        p_phone,
        p_alt_phone,
        p_district,
        p_thana,
        p_area,
        p_address,
        p_delivery_zone::delivery_zone_type,
        v_delivery_fee,
        v_subtotal,
        v_total,
        p_payment_method::payment_method_type,
        p_transaction_id,
        p_notes,
        'pending'
    )
    RETURNING id INTO v_order_id;

    -- Insert items & decrement stock
    FOR v_item IN SELECT * FROM jsonb_array_elements(p_items)
    LOOP
        v_qty := (v_item->>'quantity')::INT;
        SELECT id, name, price, discount_price
        INTO v_product
        FROM public.products
        WHERE id = (v_item->>'product_id')::UUID;

        v_price := COALESCE(v_product.discount_price, v_product.price);
        v_item_subtotal := v_price * v_qty;

        INSERT INTO public.order_items (
            order_id,
            product_id,
            product_name,
            unit_price,
            quantity,
            subtotal
        )
        VALUES (
            v_order_id,
            v_product.id,
            v_product.name,
            v_price,
            v_qty,
            v_item_subtotal
        );

        -- Decrement product stock safely
        UPDATE public.products
        SET stock = stock - v_qty, updated_at = NOW()
        WHERE id = v_product.id;
    END LOOP;

    -- Return created order summary
    RETURN jsonb_build_object(
        'id', v_order_id,
        'order_number', v_order_number,
        'total_amount', v_total,
        'delivery_fee', v_delivery_fee,
        'subtotal', v_subtotal,
        'status', 'pending'
    );
END;
$$;

-- 9. TRACKING RPC FUNCTION: track_order
-- Allows public tracking lookup by matching order_number AND customer phone
CREATE OR REPLACE FUNCTION public.track_order(
    p_order_number TEXT,
    p_phone TEXT
)
RETURNS TABLE (
    order_number TEXT,
    customer_name TEXT,
    status order_status_type,
    tracking_number TEXT,
    delivery_zone delivery_zone_type,
    total_amount NUMERIC,
    created_at TIMESTAMP WITH TIME ZONE
)
LANGUAGE sql
SECURITY DEFINER
AS $$
    SELECT
        order_number,
        customer_name,
        status,
        tracking_number,
        delivery_zone,
        total_amount,
        created_at
    FROM public.orders
    WHERE UPPER(order_number) = UPPER(TRIM(p_order_number))
      AND (
          REPLACE(phone, ' ', '') LIKE '%' || RIGHT(REPLACE(TRIM(p_phone), ' ', ''), 10)
      )
    LIMIT 1;
$$;

-- 10. ROW LEVEL SECURITY (RLS) POLICIES
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.order_items ENABLE ROW LEVEL SECURITY;

-- Helper function to check if current user is owner or admin
CREATE OR REPLACE FUNCTION public.is_admin_or_owner()
RETURNS BOOLEAN AS $$
    SELECT EXISTS (
        SELECT 1 FROM public.profiles
        WHERE id = auth.uid() AND role IN ('admin', 'owner')
    );
$$ LANGUAGE sql SECURITY DEFINER;

-- Profiles: Users can view/edit their own profile; Owner can read all
CREATE POLICY "Users can read own profile" ON public.profiles
    FOR SELECT USING (auth.uid() = id OR public.is_admin_or_owner());

CREATE POLICY "Users can update own profile" ON public.profiles
    FOR UPDATE USING (auth.uid() = id);

-- Categories: Anyone can read, only admin/owner can insert/update/delete
CREATE POLICY "Public can read categories" ON public.categories
    FOR SELECT USING (true);

CREATE POLICY "Admin/Owner manage categories" ON public.categories
    FOR ALL USING (public.is_admin_or_owner());

-- Products: Public can read active products, admin/owner can manage all
CREATE POLICY "Public can read active products" ON public.products
    FOR SELECT USING (is_active = true OR public.is_admin_or_owner());

CREATE POLICY "Admin/Owner manage products" ON public.products
    FOR ALL USING (public.is_admin_or_owner());

-- Orders: Owner/Admin can see all, customers can see their own
CREATE POLICY "Admin/Owner manage all orders" ON public.orders
    FOR ALL USING (public.is_admin_or_owner());

CREATE POLICY "Users view own orders" ON public.orders
    FOR SELECT USING (auth.uid() = user_id);

-- Order Items
CREATE POLICY "Admin/Owner manage order items" ON public.order_items
    FOR ALL USING (public.is_admin_or_owner());

CREATE POLICY "Users view own order items" ON public.order_items
    FOR SELECT USING (
        EXISTS (SELECT 1 FROM public.orders WHERE orders.id = order_items.order_id AND orders.user_id = auth.uid())
    );

-- 11. INITIAL SEED DATA
INSERT INTO public.categories (name, slug) VALUES
('Men''s Fashion', 'mens-fashion'),
('Women''s Fashion', 'womens-fashion'),
('Gadgets & Electronics', 'gadgets-electronics'),
('Lifestyle Products', 'lifestyle-products'),
('Accessories', 'accessories'),
('Beauty & Personal Care', 'beauty-personal-care'),
('Home & Living', 'home-living'),
('Sports & Fitness', 'sports-fitness'),
('Kids & Baby Products', 'kids-baby-products'),
('Special Offers', 'special-offers')
ON CONFLICT (slug) DO NOTHING;

-- Seed Products
DO $$
DECLARE
    cat_gadgets UUID;
    cat_mens UUID;
    cat_lifestyle UUID;
    cat_sports UUID;
BEGIN
    SELECT id INTO cat_gadgets FROM public.categories WHERE slug = 'gadgets-electronics' LIMIT 1;
    SELECT id INTO cat_mens FROM public.categories WHERE slug = 'mens-fashion' LIMIT 1;
    SELECT id INTO cat_lifestyle FROM public.categories WHERE slug = 'lifestyle-products' LIMIT 1;
    SELECT id INTO cat_sports FROM public.categories WHERE slug = 'sports-fitness' LIMIT 1;

    INSERT INTO public.products (category_id, name, description, price, discount_price, stock, image_url, is_active)
    VALUES
    (cat_gadgets, 'SiFox Edition Premium Smart Watch Series 9', 'AMOLED HD display, Bluetooth Calling, 7 days battery, IP68 Waterproof with official warranty.', 4200.00, 2850.00, 35, 'https://images.unsplash.com/photo-1579586337278-3befd40fd17a?w=800&q=80', true),
    (cat_mens, 'SiFox Signature Royal Oxford Cotton Shirt', '100% fine combed cotton, premium pinpoint texture, wrinkle resistant finish for all-day elegance.', 2200.00, 1450.00, 50, 'https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=800&q=80', true),
    (cat_gadgets, 'Acoustic Pro Wireless Active Noise Canceling Headphones', 'Hybrid ANC -35dB, 40mm neodymium drivers, 50 hours battery playtime, plush memory foam.', 5500.00, 3890.00, 20, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&q=80', true),
    (cat_lifestyle, 'Urban Commuter Waterproof Laptop Backpack 25L', 'Water-repellent 900D fabric, 16-inch fleece laptop sleeve, hidden anti-theft zipper pocket.', 2800.00, 1850.00, 45, 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800&q=80', true),
    (cat_sports, 'ProFit High-Bounce Running Shoes Lightweight', 'Engineered breathable mesh upper, nitrogen-cushioned cloud foam bounce midsole.', 3800.00, 2450.00, 30, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80', true)
    ON CONFLICT DO NOTHING;
END $$;
