(() => {
    const cfg = window.SIFOX_CONFIG || {};
    const $ = id => document.getElementById(id);

    const isConfigured = window.supabase &&
        cfg.SUPABASE_URL &&
        !cfg.SUPABASE_URL.includes("YOUR_PROJECT") &&
        cfg.SUPABASE_ANON_KEY &&
        !cfg.SUPABASE_ANON_KEY.includes("YOUR_SUPABASE_ANON_KEY");

    let db = null;
    if (isConfigured) {
        db = window.supabase.createClient(cfg.SUPABASE_URL, cfg.SUPABASE_ANON_KEY);
    }

    // Built-in catalog fallback for immediate local testing
    const fallbackProducts = [
        {
            id: "prod_1",
            name: "SiFox Edition Premium Smart Watch Series 9",
            description: "AMOLED HD display, Bluetooth Calling, 7 days battery, IP68 Waterproof with official warranty.",
            price: 4200,
            discount_price: 2850,
            stock: 35,
            image_url: "https://images.unsplash.com/photo-1579586337278-3befd40fd17a?w=800&q=80",
            category_id: "gadgets"
        },
        {
            id: "prod_2",
            name: "SiFox Signature Royal Oxford Cotton Shirt",
            description: "100% fine combed cotton, premium pinpoint texture, wrinkle resistant finish for all-day elegance.",
            price: 2200,
            discount_price: 1450,
            stock: 50,
            image_url: "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=800&q=80",
            category_id: "mens"
        },
        {
            id: "prod_3",
            name: "Acoustic Pro Wireless Active Noise Canceling Headphones",
            description: "Hybrid ANC -35dB, 40mm neodymium drivers, 50 hours battery playtime, plush memory foam.",
            price: 5500,
            discount_price: 3890,
            stock: 20,
            image_url: "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&q=80",
            category_id: "gadgets"
        },
        {
            id: "prod_4",
            name: "Urban Commuter Waterproof Laptop Backpack 25L",
            description: "Water-repellent 900D fabric, 16-inch fleece laptop sleeve, hidden anti-theft zipper pocket.",
            price: 2800,
            discount_price: 1850,
            stock: 45,
            image_url: "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800&q=80",
            category_id: "lifestyle"
        },
        {
            id: "prod_5",
            name: "ProFit High-Bounce Running Shoes Lightweight",
            description: "Engineered breathable mesh upper, nitrogen-cushioned cloud foam bounce midsole.",
            price: 3800,
            discount_price: 2450,
            stock: 30,
            image_url: "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80",
            category_id: "sports"
        }
    ];

    const fallbackCategories = [
        { id: "mens", name: "Men's Fashion" },
        { id: "womens", name: "Women's Fashion" },
        { id: "gadgets", name: "Gadgets & Electronics" },
        { id: "lifestyle", name: "Lifestyle Products" },
        { id: "sports", name: "Sports & Fitness" }
    ];

    let products = [];
    let cart = JSON.parse(localStorage.getItem("sifox_cart") || "[]");

    const money = n => "৳" + Number(n || 0).toLocaleString("en-US");
    const safe = s => String(s ?? "").replace(/[&<>"']/g, c => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    }[c]));
    const unit = p => Number(p.discount_price ?? p.price);

    function save() {
        localStorage.setItem("sifox_cart", JSON.stringify(cart));
        renderCart();
    }

    function renderProducts() {
        const term = ($("search") ? $("search").value : "").toLowerCase();
        const cat = $("category") ? $("category").value : "";

        const filtered = products.filter(p =>
            (p.name + " " + (p.description || "")).toLowerCase().includes(term) &&
            (!cat || p.category_id === cat)
        );

        const container = $("products");
        if (!container) return;

        if (!filtered.length) {
            container.innerHTML = `
                <div class="col-span-full py-16 text-center text-slate-500 bg-white rounded-2xl border border-dashed border-slate-300 p-8">
                    <p class="text-base font-medium">কোনো পণ্য পাওয়া যায়নি।</p>
                    <p class="text-xs text-slate-400 mt-1">অন্য কোনো নাম দিয়ে সার্চ করুন অথবা ক্যাটাগরি পরিবর্তন করুন।</p>
                </div>
            `;
            return;
        }

        container.innerHTML = filtered.map(p => `
            <div class="bg-white rounded-2xl overflow-hidden border border-slate-200/80 shadow-sm hover:shadow-md transition flex flex-col group">
                <div class="h-48 bg-slate-100 overflow-hidden relative">
                    <img src="${safe(p.image_url || 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&q=80')}" alt="${safe(p.name)}" class="w-full h-full object-cover group-hover:scale-105 transition duration-300">
                    ${p.discount_price && p.discount_price < p.price ? `
                        <span class="absolute top-2 left-2 bg-red-600 text-white text-[10px] font-bold px-2 py-0.5 rounded-full shadow">
                            -${Math.round(((p.price - p.discount_price) / p.price) * 100)}% ছাড়
                        </span>
                    ` : ''}
                </div>
                <div class="p-4 flex flex-col flex-1">
                    <h3 class="font-bold text-slate-900 text-sm line-clamp-2 leading-snug mb-1">${safe(p.name)}</h3>
                    <p class="text-xs text-slate-500 line-clamp-2 mb-3 flex-1">${safe(p.description || '')}</p>
                    <div class="flex items-baseline gap-2 mb-3">
                        <span class="text-base font-extrabold text-sky-600">${money(unit(p))}</span>
                        ${p.discount_price ? `<span class="text-xs text-slate-400 line-through">${money(p.price)}</span>` : ''}
                    </div>
                    <div class="flex items-center justify-between pt-2 border-t border-slate-100">
                        <span class="text-[11px] font-medium ${p.stock > 0 ? 'text-emerald-600' : 'text-red-500'}">
                            ${p.stock > 0 ? `স্টক: ${p.stock} টি` : 'স্টক শেষ'}
                        </span>
                        <button data-add="${p.id}" ${p.stock <= 0 ? 'disabled' : ''} class="bg-sky-600 hover:bg-sky-700 disabled:bg-slate-300 text-white text-xs font-bold px-3 py-1.5 rounded-lg shadow-sm transition">
                            কার্টে যোগ করুন
                        </button>
                    </div>
                </div>
            </div>
        `).join("");
    }

    async function load() {
        if (!db) {
            // Run in demo mode with fallback products
            products = fallbackProducts;
            if ($("category")) {
                $("category").innerHTML = '<option value="">সব ক্যাটাগরি</option>' +
                    fallbackCategories.map(x => `<option value="${safe(x.id)}">${safe(x.name)}</option>`).join("");
            }
            renderProducts();
            return;
        }

        try {
            const [{ data: p, error: pErr }, { data: c, error: cErr }] = await Promise.all([
                db.from("products").select("*").eq("is_active", true).order("created_at", { ascending: false }),
                db.from("categories").select("id, name").order("name")
            ]);

            if (pErr) throw pErr;

            products = p || [];
            if ($("category")) {
                $("category").innerHTML = '<option value="">সব ক্যাটাগরি</option>' +
                    (c || []).map(x => `<option value="${safe(x.id)}">${safe(x.name)}</option>`).join("");
            }
            renderProducts();

            // Check if current authenticated user is owner
            const { data: { user } } = await db.auth.getUser();
            if (user?.email?.toLowerCase() === String(cfg.OWNER_EMAIL || "").toLowerCase()) {
                const ownerLink = $("ownerLink");
                if (ownerLink) ownerLink.classList.remove("hidden");
            }
        } catch (err) {
            console.error("Load error:", err);
            products = fallbackProducts;
            renderProducts();
        }
    }

    function renderCart() {
        if ($("cartCount")) $("cartCount").textContent = cart.reduce((a, x) => a + x.qty, 0);

        const cartItems = $("cartItems");
        if (!cartItems) return;

        if (!cart.length) {
            cartItems.innerHTML = '<p class="text-slate-500 text-center py-8 text-sm">আপনার কার্ট খালি।</p>';
        } else {
            cartItems.innerHTML = cart.map(x => `
                <div class="py-3 flex items-center justify-between text-sm">
                    <div class="pr-2 flex-1">
                        <h4 class="font-bold text-slate-800 text-xs">${safe(x.name)}</h4>
                        <div class="text-xs text-slate-500 mt-0.5">${money(x.price)} × ${x.qty}</div>
                    </div>
                    <div class="flex items-center space-x-1.5">
                        <button data-dec="${x.id}" class="w-6 h-6 rounded bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold flex items-center justify-center">−</button>
                        <span class="w-6 text-center font-bold text-xs">${x.qty}</span>
                        <button data-inc="${x.id}" class="w-6 h-6 rounded bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold flex items-center justify-center">+</button>
                        <button data-remove="${x.id}" class="text-xs text-red-600 hover:text-red-700 font-medium pl-2">সরান</button>
                    </div>
                    <div class="w-16 text-right font-bold text-slate-900 text-xs">
                        ${money(x.price * x.qty)}
                    </div>
                </div>
            `).join("");
        }

        const sub = cart.reduce((a, x) => a + (x.price * x.qty), 0);
        const zone = $("zone") ? $("zone").value : "inside_dhaka";
        const fee = zone === "inside_dhaka" ? 80 : 140;

        if ($("subtotal")) $("subtotal").textContent = money(sub);
        if ($("delivery")) $("delivery").textContent = money(fee);
        if ($("total")) $("total").textContent = money(sub + fee);
    }

    // Event Listeners
    if ($("products")) {
        $("products").addEventListener("click", e => {
            const b = e.target.closest("[data-add]");
            if (!b) return;
            const p = products.find(x => x.id === b.dataset.add);
            if (!p) return;
            const existing = cart.find(x => x.id === p.id);
            if (existing) {
                if (existing.qty >= p.stock) return alert("পর্যাপ্ত স্টক নেই।");
                existing.qty++;
            } else {
                cart.push({ id: p.id, name: p.name, price: unit(p), qty: 1, stock: p.stock });
            }
            save();
            if ($("cartPanel")) $("cartPanel").classList.remove("hidden");
        });
    }

    if ($("cartItems")) {
        $("cartItems").addEventListener("click", e => {
            const removeId = e.target.dataset.remove;
            const incId = e.target.dataset.inc;
            const decId = e.target.dataset.dec;

            if (removeId) {
                cart = cart.filter(i => i.id !== removeId);
                save();
            } else if (incId) {
                const item = cart.find(i => i.id === incId);
                if (item) {
                    if (item.qty >= item.stock) return alert("পর্যাপ্ত স্টক নেই।");
                    item.qty++;
                    save();
                }
            } else if (decId) {
                const item = cart.find(i => i.id === decId);
                if (item) {
                    item.qty--;
                    if (item.qty <= 0) cart = cart.filter(i => i.id !== decId);
                    save();
                }
            }
        });
    }

    if ($("search")) $("search").addEventListener("input", renderProducts);
    if ($("category")) $("category").addEventListener("change", renderProducts);
    if ($("zone")) $("zone").addEventListener("change", renderCart);

    if ($("cartOpen")) $("cartOpen").onclick = () => $("cartPanel") && $("cartPanel").classList.remove("hidden");
    if ($("cartClose")) $("cartClose").onclick = () => $("cartPanel") && $("cartPanel").classList.add("hidden");
    if ($("cartCloseBtn")) $("cartCloseBtn").onclick = () => $("cartPanel") && $("cartPanel").classList.add("hidden");

    // Checkout Submit
    if ($("checkout")) {
        $("checkout").addEventListener("submit", async e => {
            e.preventDefault();
            if (!cart.length) return alert("আপনার কার্ট খালি!");

            const btn = $("placeOrder");
            btn.disabled = true;
            btn.textContent = "অর্ডার সংরক্ষণ হচ্ছে...";

            try {
                const customerName = $("customerName").value.trim();
                const phone = $("phone").value.trim();
                const altPhone = $("altPhone").value.trim() || null;
                const district = $("district").value.trim();
                const thana = $("thana").value.trim();
                const area = $("area").value.trim();
                const address = $("address").value.trim();
                const zone = $("zone").value;
                const paymentRadio = document.querySelector('input[name="payment"]:checked');
                const paymentMethod = paymentRadio ? paymentRadio.value : "cod";
                const trxId = $("trx").value.trim() || null;
                const notes = $("notes").value.trim() || null;

                const deliveryFee = zone === "inside_dhaka" ? 80 : 140;
                const subtotal = cart.reduce((a, x) => a + (x.price * x.qty), 0);
                const grandTotal = subtotal + deliveryFee;

                let orderNumber = "SF-" + Math.floor(10000 + Math.random() * 90000);

                if (db) {
                    const payload = {
                        p_customer_name: customerName,
                        p_phone: phone,
                        p_alt_phone: altPhone,
                        p_district: district,
                        p_thana: thana,
                        p_area: area,
                        p_address: address,
                        p_delivery_zone: zone,
                        p_payment_method: paymentMethod,
                        p_transaction_id: trxId,
                        p_notes: notes,
                        p_items: cart.map(x => ({ product_id: x.id, quantity: x.qty }))
                    };

                    const { data, error } = await db.rpc("place_order", payload);
                    if (error) throw error;
                    if (data?.order_number) orderNumber = data.order_number;
                }

                // Prepare WhatsApp Notification Message
                const itemsSummary = cart.map(x => `• ${x.name} x ${x.qty} = ${money(x.price * x.qty)}`).join("%0A");
                const fullAddressText = `${district}, ${thana}, ${area}, ${address}`;
                const paymentInfo = paymentMethod.toUpperCase() + (trxId ? ` (TrxID: ${trxId})` : "");

                const msg = `*🛍️ SiFox Store — নতুন অর্ডার*%0A----------------------------------------%0A*অর্ডার নম্বর:* ${orderNumber}%0A*গ্রাহকের নাম:* ${encodeURIComponent(customerName)}%0A*ফোন:* ${encodeURIComponent(phone)}%0A*ঠিকানা:* ${encodeURIComponent(fullAddressText)}%0A*জোন:* ${zone === "inside_dhaka" ? "ঢাকার ভেতরে (৳৮০)" : "ঢাকার বাইরে (৳১৪০)"}%0A%0A*অর্ডারকৃত পণ্য:*%0A${itemsSummary}%0A----------------------------------------%0A*সাবটোটাল:* ${encodeURIComponent(money(subtotal))}%0A*ডেলিভারি ফি:* ${encodeURIComponent(money(deliveryFee))}%0A*সর্বমোট প্রদেয়:* ${encodeURIComponent(money(grandTotal))}%0A*পেমেন্ট পদ্ধতি:* ${encodeURIComponent(paymentInfo)}%0A----------------------------------------%0Aধন্যবাদ!`;

                cart = [];
                save();
                $("checkout").reset();
                if ($("cartPanel")) $("cartPanel").classList.add("hidden");

                alert(`🎉 অর্ডার সফলভাবে সম্পন্ন হয়েছে!\nআপনার অর্ডার নম্বর: ${orderNumber}`);

                const whatsappNumber = cfg.WHATSAPP_NUMBER || "966530197252";
                window.open(`https://wa.me/${whatsappNumber}?text=${msg}`, "_blank", "noopener");

            } catch (err) {
                console.error("Order error:", err);
                alert("অর্ডার করা যায়নি: " + (err.message || "অনুগ্রহ করে আবার চেষ্টা করুন"));
            } finally {
                btn.disabled = false;
                btn.textContent = "অর্ডার কনফার্ম করুন 🚀";
            }
        });
    }

    renderCart();
    load();
})();
