# Mom’s Kitchen — MVP (Spring Boot + React)

A food ordering app for a home food business. Customers can browse the menu, add items to a cart, choose add-ons, and check out with **Cash** or **Cash App**.  
No login required. Customers can later look up orders using their **phone number + order code**.  
An **Admin page** is included to review orders, mark them paid/unpaid, update statuses, and view full line-item details.

---

## Features

### Customer Flow
- Browse menu → category → item.
- Add-ons (extra protein, spicy shito, etc.).
- Cart → checkout form (name, email, phone, pickup slot).
- Payment options: Cash or Cash App redirect/QR.
- Pickup slots only Fri/Sat/Sun (predefined).
- Order lookup by phone + order code.

### Admin Flow
- Secure with API key (`ADMIN_API_KEY` backend / `VITE_ADMIN_KEY` frontend).
- Orders list with filters (status, payment).
- Update **status**: PENDING → CONFIRMED → READY → COMPLETED / CANCELED.
- Update **payment**: UNPAID, PAID, REFUNDED.
- **View Details Drawer**: full customer info, line items, add-ons, totals.

---

## Tech Stack
- **Backend:** Java 21, Spring Boot 3, JPA/Hibernate, MySQL
- **Frontend:** React + Vite (TypeScript, TailwindCSS)
- **Infra (MVP):** Local MySQL; images served from `/frontend/public/img/*`  
  (easy to migrate to S3/Cloudinary later)

  [ MenuPage ]      [ CheckoutPage ]     [ LookupPage ]     [ AdminOrdersPage ]
      │                   │                   │                    │
      └──────────────► [ CartContext ] ◄──────┘                    │
                            │                                     │
                            ▼                                     ▼
                        [ API Layer ]  (src/api/)
                        ┌─────────────┐
                        │ base.ts     │  <- common setup (base URL, headers)
                        │ menu.ts     │  <- fetch menu + categories
                        │ orders.ts   │  <- place/check orders
                        │ admin.ts    │  <- admin actions
                        └─────────────┘
                            │
                            ▼
                        [ Backend ]
                  (Spring Boot / Database)


---

## Repository Layout
```text
moms-kitchen/
├─ README.md
├─ .gitignore
├─ .env.example
├─ backend/
│  ├─ pom.xml
│  └─ src/main/...
│     ├─ resources/
│     │  ├─ application.yml
│     │  ├─ schema.sql   # database tables
│     │  └─ data.sql     # seed menu items, categories, slots
└─ frontend/
   ├─ package.json
   ├─ public/img/        # menu item images for MVP
   └─ src/
      ├─ pages/          # MenuPage, CheckoutPage, OrderLookupPage, AdminOrdersPage
      ├─ components/     # CartDrawer, AddonPicker, AdminOrderDetailsDrawer, etc.
      ├─ api/            # menuApi, orderApi, adminApi
      └─ context/        # CartContext
```

---

## Database: MySQL via Docker (no username/password)

This project is configured to work out-of-the-box with a local MySQL container and an empty root password. Spring Boot will run `schema.sql` and `data.sql` at startup.

Steps
- Start MySQL (and Adminer UI):
  - `docker compose up -d`
- Optional: stop any existing MySQL on port 3306 (Homebrew) or change the compose port mapping to avoid conflicts.
- Open Adminer at http://localhost:8081 and connect:
  - System: MySQL
  - Server: `mysql`
  - Username: `root`
  - Password: leave empty
  - Database: `moms_kitchen`

Spring Boot defaults (overridable via env):
- `DB_HOST` default `localhost`
- `DB_PORT` default `3306`
- `DB_NAME` default `moms_kitchen`
- `DB_USERNAME` default `root`
- `DB_PASSWORD` default empty

Run the backend after the DB is healthy and it will apply the schema and seed data.

Navigate:
Menu → Add items and Quote/Create from cart
Checkout → Fill info; uses cart items (or sample fallback)
Lookup → Enter order code + phone to fetch an order
Admin → View/manage orders (requires VITE_ADMIN_KEY)

### Payment methods
- Cash: choose Cash in Checkout; place order and pay on pickup.
- Cash App: choose Cash App in Checkout. If you set your cashtag in `frontend/.env` as `VITE_CASHAPP_TAG` (e.g., `$momsKitchen`), the frontend shows a "Pay with Cash App" link using the quote/order total.

---

## Menu Images (Frontend)

You can show images for each menu item. The backend exposes an `imageUrl` per item and the frontend renders it.

- Where to put files: add your images to `frontend/public/img/`.
- How to reference: set `image_url` in the database to the asset path, e.g. `/img/jollof.jpg`.
- Supported forms: absolute URLs (`https://...`), root-relative (`/img/file.jpg`), or just a filename (`jollof.jpg`), which resolves to `/img/jollof.jpg`.
- Fallback: if an image is missing, the frontend shows `/img/placeholder.svg`.

Quick options
- Use the seed data in `backend/momskitchen/src/main/resources/data.sql` which references files like `/img/jollof.jpg`, `/img/waakye.jpg`, etc. Drop matching images into `frontend/public/img/`.
- Or update existing items via Adminer: open the `menu_item` table and edit the `image_url` column to point to your files.

Note: In dev, Vite serves assets under `public/` at the site root. So a file at `frontend/public/img/jollof.jpg` is available at `http://localhost:5173/img/jollof.jpg`.

---

## Screenshots

Below are a few visual examples embedded directly from the repo. You can swap these with real UI screenshots at any time.

<div align="center">

<img src=" />
<img src="" />

</div>

Add your own screenshots
- Take screenshots in your browser (Menu, Checkout, Admin).
- Save them under `docs/screenshots/` (recommended) or `frontend/public/img/`.
- Embed in this README with repo‑relative paths, for example:
  - Markdown: `![Checkout](docs/screenshots/checkout.png)`
  - HTML with width: `<img src="docs/screenshots/checkout.png" alt="Checkout" width="600">`

Tip: Keep files < 1MB and use JPG/WebP for smaller size.

---

## Backend

The backend is a Spring Boot 3 service that exposes a small REST API and uses JPA/Hibernate for persistence.

### Entities
- Menu, MenuCategory, MenuItem: catalog with `image_url`, `display_order`, and availability.
- Addon: add-on options (e.g., Extra Protein) with `price_delta`.
- PickupSlot: active windows by day-of-week and start/end times.
- Order, OrderItem, OrderItemAddon: order header + line snapshots (names/prices captured at order time).

### Controllers & Endpoints
- `MenuController` (`/api/menu`)
  - `GET /menus`: list menus (id, name, active)
  - `GET /{menuId}/tree`: menu → categories → items → allowedAddons (for the customer app)
  - `GET /categories/{categoryId}/items`: items in a category
- `OrderController` (`/api/orders`)
  - `POST /quote`: price a cart (no DB writes)
  - `POST /`: create an order, returns an `OrderSummaryDTO` with an order code
  - `GET /{orderCode}?phone=...`: lookup order by code + phone
- `AdminOrderController` (`/api/admin/orders`) — requires `X-Admin-Key`
  - `GET /`: paged list with optional `status` and `paymentStatus`
  - `GET /{id}`: full order details
  - `PUT /{id}/status/{newStatus}`: update status (PENDING/CONFIRMED/READY/COMPLETED/CANCELED)
  - `PUT /{id}/payment/{newPaymentStatus}`: update payment (UNPAID/PAID/REFUNDED)

### Pickup Validation
`PickupService` validates pickup inputs:
- Enforces “future by N minutes” and that the time fits an active slot.
- When only a `pickupDay` (0..6) is sent, the backend derives a concrete `pickupAt` using the earliest active slot for that day.

### Pricing
`PricingService` computes totals:
- Line = (item price + sum(addon deltas)) × quantity
- Subtotal = sum(lines), Tax = subtotal × `pricing.taxRate`, Total = subtotal + tax

### Security
`AdminApiKeyFilter` protects admin routes with the `X-Admin-Key` header. Set the backend key and give the frontend the same value so it can call admin APIs.

### Configuration
- Date serialization: `JacksonConfig` outputs ISO-8601 strings for `java.time` types.
- DB auto-init: `schema.sql` creates tables, `data.sql` seeds menu/categories/items/add-ons/slots.
- Key properties (with defaults):
  - DB: `DB_HOST=localhost`, `DB_PORT=3306`, `DB_NAME=moms_kitchen`, `DB_USERNAME=root`, `DB_PASSWORD=`
  - Pricing: `pricing.taxRate=0.00`, `pricing.validateAddons=true`
  - Pickup: `pickup.requireFutureMinutes=30`, `pickup.strictDayMatch=true`, `pickup.zoneId=UTC`

---

## Frontend

React + Vite + TypeScript. The app hits the backend through a small API layer under `src/api`.

### Structure
- Pages: `MenuPage`, `CheckoutPage`, `LookupPage`, `AdminOrdersPage`
- State: `CartContext` stores cart lines and selected add-ons
- API: `src/api/{menu,orders,admin}.ts` built on `src/api/base.ts`

### Environment
Create `frontend/.env` with:
- `VITE_API_URL`: backend base URL (e.g., `http://localhost:8081`)
- `VITE_ADMIN_KEY`: admin API key (required for admin actions)
- `VITE_SHOW_ADMIN`: `true` to show the Admin tab in the UI
- `VITE_CASHAPP_TAG`: your Cash App tag (e.g., `$momsKitchen`) to show deep links
- `VITE_LANDING_BG`: optional landing hero background (path under `/public` or full URL)

### Menu Page
- Fetches `GET /api/menu/1/tree` and renders categories as tabs (Plates / Sides / Drinks from seed data).
- Each item shows thumbnail, price, description, and available add-ons.
- Multi-select add-ons via checkboxes, then “Add Selected” to add a combined variant to the cart.

### Cart & Checkout
- Cart lines are variant-aware (same item with different add-ons are separate lines).
- Checkout shows a detailed, editable list with plus/minus per line.
- Pickup day radios (Fri/Sat/Sun by default) send `pickupDay` and the backend derives a timestamp.
- Quote and Create buttons call the backend and display totals; a Cash App link appears when configured.
- “Clear Cart” removes all lines and resets quote/order.

### Admin Page
- Hidden by default; enable by setting `VITE_SHOW_ADMIN=true` and `VITE_ADMIN_KEY`.
- Lists orders with status/payment columns and totals; shows pickup day/time.
- Detail view shows customer, items, add-ons, totals; actions update status/payment.

### Assets & Background
- Menu item images live under `frontend/public/img/` and are referenced via the `image_url` column.
- The front page can use a full-bleed background photo controlled by `VITE_LANDING_BG`.

---

## Running Locally

Prereqs: Java 17+, Node 18+, Docker (for MySQL), Maven.

1) Database
- `docker compose up -d` (starts MySQL and Adminer on 8081)
- Confirm DB in Adminer: server `mysql`, user `root`, database `moms_kitchen`

2) Backend
- Configure env (optional): DB and pricing/pickup settings
- Run from your IDE or `mvn -f backend/momskitchen/pom.xml spring-boot:run`

3) Frontend
- `cd frontend`
- `npm i`
- Create `.env` from `.env.example` and set `VITE_API_URL`
- `npm run dev` then open the printed URL (default http://localhost:5173)

---

## Production Deploy (Docker + XO server)

The stack runs as three containers: `mysql` (internal), `backend` (Spring Boot on 8081), and `web` (nginx serving the built frontend and proxying `/api` to the backend). Only port `80` on `web` is published.

Steps
- Provision a VM in Xen Orchestra (Ubuntu 22.04+), assign a static IP, and open port 80 in the firewall/security group.
- Install Docker Engine and Compose plugin on the VM.
- Copy the repo to the VM (or pull from your VCS).
- Create a production env file from the example:
  - `cp deploy/.env.prod.example .env.prod`
  - Edit `.env.prod` and set strong values for `MYSQL_ROOT_PASSWORD` and `ADMIN_API_KEY`.
- Build and start the stack:
  - `docker compose --env-file ./.env.prod -f docker-compose.prod.yml up -d --build`
- Access the app from any computer at `http://<server-ip>` (or via your domain if DNS is set).

Notes
- The frontend is served by nginx and calls the backend at `/api` (same-origin), so CORS is not required.
- MySQL is not exposed publicly. Use Adminer only in local/dev.
- To update the app: re-run the compose command with `--build` to rebuild images from your latest code.


## API Examples

See `api.http` for runnable examples (VS Code REST Client / IntelliJ HTTP client). Highlights:
- `GET /api/menu/1/tree`
- `POST /api/orders/quote` and `POST /api/orders`
- `GET /api/orders/{ORDER_CODE}?phone=3025550123`
- Admin list/details and status/payment updates

---

## Troubleshooting

**Backend won’t compile (Lombok methods missing):**
- Ensure your IDE has Lombok annotation processing enabled.
- If building via Maven CLI, add `maven-compiler-plugin` with Lombok in `annotationProcessorPaths` if needed.

**Invalid Date in Admin UI:**
- Restart the backend after `JacksonConfig` changes so LocalDateTime serializes as ISO-8601.
- The frontend now parses both ISO strings and Jackson array dates.

**CORS/API URL issues:**
- Confirm `VITE_API_URL` points at your backend (e.g., `http://localhost:8081`).
- Check `CorsConfig` in the backend if you change ports/origins.

**Images not showing:**
- Drop files into `frontend/public/img/` and set `menu_item.image_url` (e.g., `/img/jollof.jpg`).
- The UI falls back to `/img/placeholder.svg` if an image is missing.
