# SIFOX STORE — MASTER PROJECT DEPLOYMENT & INTEGRATION GUIDE

**Project Name:** SiFox Store  
**Target Market:** Bangladesh  
**Brand Design:** Premium White + Sky Blue (`#0284C7`, `#E0F2FE`) with Fox Logo  
**Fixed Delivery Charges:** Inside Dhaka ৳80 | Outside Dhaka ৳140  
**Support WhatsApp:** `+966530197252`  
**Support Email:** `sifoxstore@gmail.com`  
**Super Admin Login Email:** `mdsifatmozumder07@gmail.com`  
**Super Admin Access Location:** Top Right Three Dots Menu (⋮) → **SUPER ADMIN ACCESS**  

---

## 0. Ultimate Super Admin Access System (Version 1.0)
* **Access Location:** Top right corner Three Dots menu (⋮) on both the Android Native App and Web Platform. Selecting **‘SUPER ADMIN ACCESS’** opens the dedicated Super Admin Portal (completely segregated from Customer accounts).
* **Owner Authentication Credentials:**
  - **Admin Name:** Sifat Mozumder (Owner & Super Admin)
  - **Admin Email:** `mdsifatmozumder07@gmail.com`
  - **Admin Password:** `Sifatvai07` (Verified using salted cryptographic digest; never exposed in client code)
  - **Admin Phone:** `+966530197252` / `+8801712345678`
* **Security & Two-Factor Authentication (2FA):**
  - Salted SHA-256 / Argon2id encryption on credentials.
  - 2FA PIN verification challenge with owner demo code `707436` or bypass option.
  - Active session tokens (`SFX-AUTH-...`) and automated activity audit logging (timestamps, IPs, and event records).
* **Super Admin Control Center:**
  - KPI Cards: Total Gross Sales (৳), Orders, Customers, Catalog Products, Order Pipeline (Pending, Processing, Shipped, Delivered, Cancelled).
  - Complete control across: Products, Ladies Fashion, Gents Fashion, Gadgets, Lifestyle, Accessories, Dispatches, Shipments Tracking, Customers, Payments, Discounts, Reports, Settings, Security.
  - Shipment Tracking Hub: Assign courier (Steadfast, Pathao, RedX), tracking code, estimated delivery, and delivery confirmation.

---

## 1. System Architecture Overview
SiFox Store operates on a **Single Source of Truth** architecture where the Android Native Application and the Web Frontend communicate with the same PostgreSQL/Supabase database:

```
                  ┌───────────────────────────────┐
                  │       PostgreSQL Database     │
                  │   (Supabase / Cloud SQL BD)   │
                  └──────────────┬────────────────┘
                                 │
                 ┌───────────────┴───────────────┐
                 │    Secure REST / Edge APIs    │
                 │   - Product Catalog Service   │
                 │   - Cart & Price Verifier     │
                 │   - bKash/Nagad Webhooks      │
                 │   - WhatsApp Message Builder  │
                 └───────┬───────────────┬───────┘
                         │               │
        ┌────────────────┴──────┐ ┌──────┴────────────────┐
        │  SiFox Android Native │ │  SiFox Web Platform   │
        │  (Jetpack Compose M3) │ │  (Responsive Web App) │
        └───────────────────────┘ └───────────────────────┘
```

---

## 2. Product Categories (10 Verified Departments)
1. **Men's Fashion**: Shirts, Panjabis, Chinos, Denim, Hoodies, Footwear.
2. **Women's Fashion**: Sarees, Kurtis, Jamdani Silk, Abayas, Festive Wear.
3. **Gadgets & Electronics**: Smart Watches, Bluetooth ANC Earbuds, Power Banks, Chargers.
4. **Lifestyle Products**: Wallets, Backpacks, Travel Organizers, Flasks.
5. **Accessories**: TAC Polarized Sunglasses, Leather Belts, Caps, Bracelets.
6. **Beauty & Personal Care**: Niacinamide Serums, Sunscreens, Beard Grooming Kits.
7. **Home & Living**: Nordic Ambient Lamps, Smart LED Lighting, Kitchen Organizers.
8. **Sports & Fitness**: High-Bounce Running Shoes, Gym Apparel, Resistance Gear.
9. **Kids & Baby Products**: Organic Bamboo Rompers, Soft Plush Toys, Toddler Care.
10. **Special Offers**: Summer Mega Sale, B1G1 Combos, Flash 50% Off Deals.

---

## 3. Fixed Delivery Charge Rules (Bangladesh)
* **Inside Dhaka Metropolitan Area**: ৳80 (Fixed)
  - Applicable to Dhaka North and South City Corporations (Dhanmondi, Gulshan, Banani, Mirpur, Uttara, Mohammadpur, Old Dhaka, etc.).
* **Outside Dhaka (All 63 Districts)**: ৳140 (Fixed)
  - Applicable to Chittagong, Sylhet, Rajshahi, Khulna, Barishal, Rangpur, Mymensingh, Gazipur, Narayanganj, Comilla, etc.
* **Server-side validation**: The checkout pipeline recalculates the shipping rate based on the destination district, preventing any client-side tampering.

---

## 4. Payment Gateway Integration Specification

### A. Cash on Delivery (COD)
- No upfront fee required.
- Delivery rider collects cash at the customer's doorstep upon parcel inspection.

### B. bKash Merchant Payment (Direct API)
* **Endpoint:** `POST https://tokenized.pay.bka.sh/v1.2.0-beta/tokenized/checkout/create`
* **Headers:**
  - `Content-Type: application/json`
  - `authorization: <id_token>`
  - `x-app-key: <bKash_app_key>`
* **Payload:**
```json
{
  "mode": "0011",
  "payerReference": "017XXXXXXXX",
  "callbackURL": "https://api.sifoxstore.com/api/payment/bkash/callback",
  "amount": "2850",
  "currency": "BDT",
  "intent": "sale",
  "merchantInvoiceNumber": "SF-74291"
}
```
* **Status Verification:** `POST https://tokenized.pay.bka.sh/v1.2.0-beta/tokenized/checkout/execute`
* The webhook confirms the transaction ID and updates `orders.payment_status` to `VERIFIED`.

### C. Nagad Merchant Payment (Direct API)
* **Endpoint:** `POST https://api.mynagad.com/api/dfs/check-out/initialize/{merchantId}/{orderId}`
* **Verification:** `GET https://api.mynagad.com/api/dfs/verify/payment/{paymentRefId}`
* Validates unique transaction reference and prevents duplicate processing.

---

## 5. WhatsApp Order Synchronization Mechanism
Customer Support WhatsApp: `+966530197252`

Upon order completion, an instant deep link is generated:
```
https://wa.me/966530197252?text=<URL_ENCODED_MESSAGE>
```

**Formatted Message Content:**
```
🛍️ NEW ORDER — SIFOX STORE
----------------------------------------
Order Number: SF-74291
Date: 10 Nov 2026, 03:00 PM
Customer Name: Sifat Mozumder
Phone: 01712345678 (01887654321)

📍 Delivery Address:
House 42, Road 27, Dhanmondi, Dhaka 1209
Dhanmondi, Dhaka
Zone: Inside Dhaka

📦 Ordered Products:
• SiFox Edition Premium Smart Watch Series 9 (45mm/Black) x 1 = ৳2850

----------------------------------------
Subtotal: ৳2850
Coupon (SIFOX10): -৳285
Delivery Fee: ৳80
Grand Total: ৳2645
Payment Method: bKash Merchant Payment
Transaction ID: 9K88F4A2
Payment Status: Verified & Paid
----------------------------------------
Thank you for shopping with SiFox Store!
```

---

## 6. Building Android APK & AAB

### To Build Debug APK:
```bash
gradle :app:assembleDebug
```
* The output APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

### To Build Release Android App Bundle (AAB for Google Play):
```bash
gradle :app:bundleRelease
```
* The output AAB is generated at `app/build/outputs/bundle/release/app-release.aab`.

---

## 7. Web Frontend Deployment (Cloudflare Pages / Vercel)
1. Fork or push this repository to GitHub.
2. In Cloudflare Pages or Vercel, connect the repository.
3. Configure environment variables:
   - `NEXT_PUBLIC_SUPABASE_URL` = `https://<your-project>.supabase.co`
   - `NEXT_PUBLIC_SUPABASE_ANON_KEY` = `<your-anon-key>`
   - `BKASH_APP_KEY` = `<your-merchant-key>`
   - `BKASH_APP_SECRET` = `<your-merchant-secret>`
   - `NEXT_PUBLIC_WHATSAPP_NUMBER` = `+966530197252`
   - `NEXT_PUBLIC_SUPPORT_EMAIL` = `sifoxstore@gmail.com`
4. Run schema migration from `/BACKEND_DATABASE_SCHEMA.sql` in your Supabase SQL editor.
5. Deploy! Both the Android app and web frontend will immediately sync live products, stock, coupons, and customer orders.
