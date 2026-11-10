-- ====================================================================
-- SIFOX STORE — MASTER DATABASE SCHEMA (PostgreSQL / Supabase)
-- Full-Stack Shared Backend for Android Application & Website
-- Target Market: Bangladesh (BDT ৳)
-- ====================================================================

-- 1. Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. ENUMS
CREATE TYPE user_role AS ENUM ('CUSTOMER', 'STAFF', 'ADMIN', 'OWNER');
CREATE TYPE order_status_type AS ENUM ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED');
CREATE TYPE payment_method_type AS ENUM ('COD', 'BKASH', 'NAGAD');
CREATE TYPE payment_status_type AS ENUM ('PENDING', 'VERIFIED', 'FAILED', 'REFUNDED');
CREATE TYPE delivery_zone_type AS ENUM ('INSIDE_DHAKA', 'OUTSIDE_DHAKA');

-- 3. PROFILES & USERS
CREATE TABLE profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    alternative_phone VARCHAR(20),
    email VARCHAR(150),
    role user_role DEFAULT 'CUSTOMER',
    district VARCHAR(100) DEFAULT 'Dhaka',
    thana VARCHAR(100),
    area VARCHAR(100),
    full_address TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 4. CATEGORIES (10 Standard Departments + Extensible)
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(120) NOT NULL UNIQUE,
    icon_name VARCHAR(50),
    description TEXT,
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Seed Categories
INSERT INTO categories (name, slug, icon_name, description, display_order) VALUES
('Men''s Fashion', 'mens-fashion', 'checkroom', 'Shirts, Panjabis, Pants, Hoodies & Footwear', 1),
('Women''s Fashion', 'womens-fashion', 'auto_awesome', 'Sarees, Kurtis, Abayas & Western Wear', 2),
('Gadgets & Electronics', 'gadgets-electronics', 'devices', 'Smart Watches, Earbuds, Power Banks & Audio', 3),
('Lifestyle Products', 'lifestyle-products', 'spa', 'Wallets, Travel Gear, Mugs & Daily Essentials', 4),
('Accessories', 'accessories', 'watch', 'Sunglasses, Belts, Caps, Rings & Bracelets', 5),
('Beauty & Personal Care', 'beauty-personal-care', 'face', 'Skincare, Beard Grooming, Perfumes & Hair Care', 6),
('Home & Living', 'home-living', 'home', 'Decor, Smart Lighting, Bedding & Kitchenware', 7),
('Sports & Fitness', 'sports-fitness', 'fitness_center', 'Gym Apparel, Resistance Bands & Bottles', 8),
('Kids & Baby Products', 'kids-baby-products', 'child_care', 'Baby Clothing, Soft Toys & Nursery Care', 9),
('Special Offers', 'special-offers', 'local_fire_department', 'Mega Flash Deals, B1G1 & 50% Off Sales', 10)
ON CONFLICT (slug) DO NOTHING;

-- 5. PRODUCTS
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    category_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(280) NOT NULL UNIQUE,
    price NUMERIC(12, 2) NOT NULL,
    discount_price NUMERIC(12, 2),
    stock INT NOT NULL DEFAULT 0,
    description TEXT,
    sizes TEXT[] DEFAULT ARRAY['Standard'],
    colors TEXT[] DEFAULT ARRAY['Default'],
    image_url TEXT,
    video_url TEXT,
    is_featured BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    rating NUMERIC(3, 2) DEFAULT 4.80,
    review_count INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 6. PRODUCT IMAGES (Multiple images per product)
CREATE TABLE product_images (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL,
    is_thumbnail BOOLEAN DEFAULT FALSE,
    display_order INT DEFAULT 0
);

-- 7. COUPONS & DISCOUNTS
CREATE TABLE coupons (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(50) NOT NULL UNIQUE,
    discount_percent INT DEFAULT 0,
    flat_discount NUMERIC(12, 2) DEFAULT 0.00,
    min_spend NUMERIC(12, 2) DEFAULT 0.00,
    description TEXT,
    expires_at TIMESTAMP WITH TIME ZONE,
    usage_limit INT,
    used_count INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Seed Coupons
INSERT INTO coupons (code, discount_percent, flat_discount, min_spend, description) VALUES
('SIFOX10', 10, 0, 1000.00, '10% discount on all orders above ৳1,000'),
('WELCOME500', 0, 500.00, 3000.00, 'Flat ৳500 OFF on orders above ৳3,000'),
('DHAKA20', 20, 0, 2000.00, '20% special discount on selected items')
ON CONFLICT (code) DO NOTHING;

-- 8. ORDERS
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_number VARCHAR(30) NOT NULL UNIQUE, -- e.g. SF-74291
    customer_id UUID REFERENCES profiles(id) ON DELETE SET NULL,
    customer_name VARCHAR(150) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    alternative_phone VARCHAR(20),
    district VARCHAR(100) NOT NULL,
    thana VARCHAR(100) NOT NULL,
    area VARCHAR(100) NOT NULL,
    full_address TEXT NOT NULL,
    delivery_instructions TEXT,
    delivery_zone delivery_zone_type NOT NULL, -- INSIDE_DHAKA (80), OUTSIDE_DHAKA (140)
    delivery_fee NUMERIC(8, 2) NOT NULL, -- Verified server-side: 80.00 or 140.00
    subtotal NUMERIC(12, 2) NOT NULL,
    discount NUMERIC(12, 2) DEFAULT 0.00,
    coupon_code VARCHAR(50),
    total NUMERIC(12, 2) NOT NULL,
    payment_method payment_method_type NOT NULL,
    payment_status payment_status_type DEFAULT 'PENDING',
    transaction_id VARCHAR(100),
    order_status order_status_type DEFAULT 'PENDING',
    courier_partner VARCHAR(100) DEFAULT 'Steadfast Courier BD',
    tracking_number VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 9. ORDER ITEMS
CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id UUID REFERENCES products(id) ON DELETE SET NULL,
    product_title VARCHAR(255) NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    selected_size VARCHAR(50),
    selected_color VARCHAR(50),
    subtotal NUMERIC(12, 2) NOT NULL
);

-- 10. PAYMENTS (Audit log & verification tracking)
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    gateway VARCHAR(50) NOT NULL, -- 'BKASH', 'NAGAD', 'COD'
    payment_id VARCHAR(100),
    transaction_id VARCHAR(100) UNIQUE,
    amount NUMERIC(12, 2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'BDT',
    status payment_status_type DEFAULT 'PENDING',
    raw_response JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 11. SHIPMENTS
CREATE TABLE shipments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    courier_name VARCHAR(100) NOT NULL, -- 'Pathao', 'Steadfast', 'RedX', 'eCourier'
    consignment_id VARCHAR(100),
    tracking_code VARCHAR(100) NOT NULL,
    status VARCHAR(50) DEFAULT 'In Hub',
    dispatched_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE
);

-- 12. STORE CUSTOMIZATION SETTINGS
CREATE TABLE store_settings (
    id INT PRIMARY KEY DEFAULT 1,
    store_name VARCHAR(100) DEFAULT 'SiFox Store',
    tagline VARCHAR(200) DEFAULT 'Premium E-commerce in Bangladesh',
    whatsapp_number VARCHAR(30) DEFAULT '+966530197252',
    support_email VARCHAR(100) DEFAULT 'sifoxstore@gmail.com',
    inside_dhaka_delivery_fee NUMERIC(8, 2) DEFAULT 80.00,
    outside_dhaka_delivery_fee NUMERIC(8, 2) DEFAULT 140.00,
    announcement_text TEXT DEFAULT '🔥 Mega Sale LIVE! Flat ৳80 Delivery inside Dhaka, ৳140 Nationwide.',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

INSERT INTO store_settings (id) VALUES (1) ON CONFLICT (id) DO NOTHING;

-- 13. ROW LEVEL SECURITY (RLS) POLICIES
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE products ENABLE ROW LEVEL SECURITY;
ALTER TABLE categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE coupons ENABLE ROW LEVEL SECURITY;
ALTER TABLE orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE order_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE store_settings ENABLE ROW LEVEL SECURITY;

-- Products & Categories: Anyone can read active products; Only Admins can insert/update/delete
CREATE POLICY "Public products viewable by everyone" ON products
    FOR SELECT USING (is_active = TRUE);

CREATE POLICY "Categories viewable by everyone" ON categories
    FOR SELECT USING (is_active = TRUE);

CREATE POLICY "Store settings viewable by everyone" ON store_settings
    FOR SELECT USING (TRUE);

-- Orders: Customers can view their own orders; Admins can view/update all
CREATE POLICY "Customers view own orders" ON orders
    FOR SELECT USING (auth.uid() = customer_id OR customer_id IS NULL);
