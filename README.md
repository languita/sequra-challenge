# Sequra Backend Challenge

A Spring Boot application that imports merchants and orders from CSV, stores them in an embedded H2 database, and schedules **daily**, **weekly**, and **monthly** disbursement processing.

## ✅ Key Features

- **CSV import endpoints** for merchants and orders
- **Disbursement scheduler** (daily & weekly payouts + monthly top-up)
- **Commission calculation per order**:
	- `1.00%` for orders `< 50 EUR`
	- `0.95%` for orders `>= 50 EUR` and `< 300 EUR`
	- `0.85%` for orders `>= 300 EUR`
- **Historical backfill endpoint** to process preexisting orders (for example 2023 data)
- Disbursement records now include `orderAmount`, `feeAmount`, and `monthlyFeeDisbursement`
- Uses **Spring Boot 4**, **Spring Data JPA**, and **H2 (file-based persistent DB)**
- Includes **H2 console** for inspecting data during development

## 🚀 Quick Start

### 1) Run the app

```bash
mvn spring-boot:run
```

The application starts on `http://localhost:8080` by default.

### 2) API Documentation (Swagger)

Once the application is running, you can access the Swagger UI for interactive API documentation at:

`http://localhost:8080/swagger-ui/index.html`

### 3) Import data via HTTP

#### Merchants
POST multipart to:

`POST http://localhost:8080/merchants/import`

Body form field: **file** (CSV)

**Expected columns (semicolon-separated):**
- `id` (UUID)
- `reference` (String)
- `email` (String)
- `live_on` (yyyy-MM-dd) (optional; used for weekly merchants)
- `disbursement_frequency` (`DAILY` or `WEEKLY`)
- `minimum_monthly_fee` (decimal, optional)

Example CSV:

```csv
id;reference;email;live_on;disbursement_frequency;minimum_monthly_fee
5f7d1b5c-...;merchant-123;merchant@example.com;2025-12-01;WEEKLY;100.00
```

#### Orders
POST multipart to:

`POST http://localhost:8080/orders/import`

Body form field: **file** (CSV)

**Expected columns (semicolon-separated):**
- `id` (String)
- `merchant_reference` (matches `reference` from merchants)
- `amount` (decimal)
- `created_at` (yyyy-MM-dd)

Example CSV:

```csv
id;merchant_reference;amount;created_at
abc123;merchant-123;120.00;2025-12-05
```

### 4) API Endpoints

In addition to the import endpoints, the following REST API endpoints are available:

#### Merchants
- `GET /merchants` - Get all merchants (paginated, use ?page=0&size=10)
- `GET /merchants/{id}` - Get merchant by ID (UUID)
- `POST /merchants/import` - Import merchants from uploaded CSV file

#### Orders
- `GET /orders` - Get all orders (paginated, use ?page=0&size=10)
- `GET /orders/{id}` - Get order by ID (String)
- `POST /orders/import` - Import orders from uploaded CSV file

#### Disbursements
- `GET /disbursements` - Get all disbursements
- `POST /disbursements/process` - Manually trigger disbursement processing
- `POST /disbursements/process/historical` - Process all pending historical orders using merchant frequency and commission rules

**Note:** Merchants and orders are loaded automatically on startup from CSV files. Duplicate records are skipped. The app stays running after loading.

### 5) Initial processing for preexisting orders (historical backfill)

If your orders are old (for example from 2023), run this once after imports/startup:

`POST http://localhost:8080/disbursements/process/historical`

Example with curl:

```bash
curl -X POST http://localhost:8080/disbursements/process/historical
```

What it does:
- Processes all `disbursed = false` orders
- Applies merchant frequency (`DAILY` / `WEEKLY`)
- Applies commission tiers
- Creates disbursements and marks processed orders as `disbursed = true`

### 6) How to manage all old data safely

Recommended one-time migration flow for large historical datasets (for example ~1.3M orders):

1. Start application with persistent DB enabled (`jdbc:h2:file:./data/sequra_db`).
2. Import merchants first, then orders.
3. Trigger historical processing once:
	- `POST /disbursements/process/historical`
4. Verify results in DB:
	- old orders should now have `disbursed = true`
	- new rows should exist in `disbursement`
5. Keep scheduler enabled for incremental daily/weekly/monthly processing.

Important behavior:
- The historical endpoint is idempotent for already processed orders because it only processes `disbursed = false`.
- If you call it again, it will process only newly imported pending orders.
- Monthly top-up disbursements are flagged with `monthlyFeeDisbursement = true`.

Operational tips for big imports:
- Run historical endpoint after import completes (do not run while importing in parallel).
- Use H2 console to monitor counts during migration.
- For very large runs, expect runtime to depend on machine/disk speed.

## 🗂️ Database & Console

This project uses an **embedded H2 file-based database** automatically configured by Spring Boot.

To inspect the DB at runtime, open:

- `http://localhost:8080/h2-console`

JDBC URL: `jdbc:h2:file:./data/sequra_db`

## 📖 API Documentation (Swagger)

Access the interactive API docs at:

- `http://localhost:8080/swagger-ui/index.html`

This includes endpoints for importing merchants and orders.

## 🕒 Scheduled Disbursements

The project includes a scheduler that triggers automatically (UTC timezone):

- **Daily** (`11:15 UTC`, temporary) → processes yesterday’s orders for DAILY merchants
- **Weekly** (`11:15 UTC`, temporary) → processes the past week for WEEKLY merchants based on their `live_on` day
- **Monthly top-up** (`11:15 UTC`, temporary) → creates top-up entries to meet each merchant `minimum_monthly_fee`

Note: original cron values are kept commented in scheduler code.

## 💸 Disbursement Amount Fields

Each disbursement now stores:
- `orderAmount`: gross amount from included orders
- `feeAmount`: total commission fee (or top-up amount for monthly fee disbursements)
- `amount`: net disbursed amount
- `monthlyFeeDisbursement`: `true` when generated by monthly minimum fee logic

## 🧪 Build & Test

Build the project:

```bash
mvn clean package
```

Run tests:

```bash
mvn test
```

## 🔧 Notes

- Merchant IDs are expected as **UUIDs** and Order IDs as **String** in CSVs.
- Orders are marked as `disbursed` after being processed by the scheduler.
- The scheduler uses UTC time and is enabled via `@EnableScheduling`.

---

If you want to change the database settings or add persistent storage, update `src/main/resources/application.properties`.
