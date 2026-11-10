# SiFox Store
Supabase-backed Bengali ecommerce starter: storefront, customer auth, owner dashboard, order tracking, WhatsApp order notification, and Android application.

## 1) Supabase Setup
1. Create a Supabase project at [supabase.com](https://supabase.com).
2. Open **SQL Editor** and run `supabase/schema.sql`.
3. In **Authentication > URL Configuration**, add your deployed site URL and redirect URLs.
4. In **Authentication settings**, enable email/password. For phone-based customer identity, add phone verification separately.
5. Create your owner account through the app's signup/login flow using the intended owner email (`mdsifatmozumder07@gmail.com`).
6. In **SQL Editor**, run the one-time owner promotion statement in `supabase/make-owner.sql`. It updates the profile for the matching auth email. Do not expose service-role keys.

## 2) Configure Frontend
Copy `web/config.example.js` to `web/config.js` and replace:
- `SUPABASE_URL`
- `SUPABASE_ANON_KEY`
- `OWNER_EMAIL`
- `WHATSAPP_NUMBER` (international digits only, no plus, e.g. `966530197252`)

The anon/publishable key is designed to be public. Row Level Security (RLS) is the security boundary. Never put a Supabase service-role key in browser code.

## 3) Run Locally
Serve the `web` directory with any static HTTP server (not `file://`):
```bash
python -m http.server 8080 --directory web
```
Then open `http://localhost:8080`.

## 4) Deploy
Deploy the contents of `web/` to Cloudflare Pages, Netlify, or Firebase Hosting:
- Add the deployed URL to Supabase Auth redirect URLs.
- Configure environment values in `config.js` before deployment. Do not deploy `config.example.js` as the active config.

## 5) Android Application
Open the Android project in Android Studio or compile with Gradle:
- Build debug APK: `gradle :app:assembleDebug`
- Build signed bundle/APK: `gradle :app:bundleRelease`
- Supports 100% native Jetpack Compose with real-time cart, checkout, delivery calculations, and WhatsApp order synchronization.

## Features
- **Product catalog / search / category filtering** (10 verified departments).
- **Cart persisted in localStorage** with live stock validation.
- **Fixed delivery fees in Bangladesh:** ৳80 inside Dhaka and ৳140 outside Dhaka.
- **Checkout writes order + order_items to Supabase** via secure `place_order` RPC.
- **Order lookup by order number + phone** via security-definer `track_order` RPC.
- **Customer Auth**: Signup, login, password reset, and logout.
- **Owner-only Dashboard**: Products CRUD, inventory management, live order status & tracking number updater.
- **WhatsApp order notification**: Opens pre-filled WhatsApp message with order details to `+966530197252`.
- **Supabase RLS**: Zero-trust security-definer checkout and tracking RPCs.

## Important Notes
- COD / bKash / Nagad in this starter are order payment-method selections. bKash / Nagad transactions are manually verified via TrxID unless you integrate an official merchant API.
- The WhatsApp link opens a prefilled message; the customer confirms and presses send.
- Delivery fees are strictly validated server-side in `supabase/schema.sql`.
