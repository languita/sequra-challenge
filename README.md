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

### 3) Data import (automatic via Loader)

By default, merchants and orders are imported automatically at startup by `DataLoader` from:

- `src/main/resources/merchants.csv`
- `src/main/resources/orders.csv`

The HTTP import endpoints are optional and can be used for manual reloads or custom datasets.

### 3.1) Optional import via HTTP

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
- `POST /disbursements/process/historical/monthly-fees` - Backfill historical monthly minimum fee disbursements

**Note:** Merchants and orders are loaded automatically on startup from CSV files. Duplicate records are skipped. The app stays running after loading.

### 5) Initial processing for preexisting orders (historical backfill)

If your orders are old (for example from 2023), run this once after imports/startup:

`POST http://localhost:8080/disbursements/process/historical`

Then run historical monthly minimum fee backfill:

`POST http://localhost:8080/disbursements/process/historical/monthly-fees`

Example with curl:

```bash
curl -X POST http://localhost:8080/disbursements/process/historical
curl -X POST http://localhost:8080/disbursements/process/historical/monthly-fees
```

What the historical order process does:
- Processes all `disbursed = false` orders
- Applies merchant frequency (`DAILY` / `WEEKLY`)
- Applies commission tiers
- Creates disbursements and marks processed orders as `disbursed = true`

What the historical monthly fee process does:
- Reviews historical months with generated disbursements
- Sums collected order fees per merchant and month
- Creates one `monthlyFeeDisbursement = true` row when collected fees are below `minimumMonthlyFee`

### 6) How to manage all old data safely

Recommended one-time migration flow for large historical datasets (for example ~1.3M orders):

1. Start application with persistent DB enabled (`jdbc:h2:file:./data/sequra_db`).
2. Import merchants first, then orders.
3. Trigger historical order processing once:
	- `POST /disbursements/process/historical`
4. Trigger historical monthly fee processing once:
	- `POST /disbursements/process/historical/monthly-fees`
5. Verify results in DB:
	- old orders should now have `disbursed = true`
	- new rows should exist in `disbursement`
6. Keep scheduler enabled for incremental daily/weekly/monthly processing.

Important behavior:
- The historical endpoint is idempotent for already processed orders because it only processes `disbursed = false`.
- If you call it again, it will process only newly imported pending orders.
- The historical monthly fee endpoint creates backfilled monthly fee rows for past months after normal historical disbursements exist.
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

- **Daily** (`08:00 UTC`) → processes yesterday’s orders for DAILY merchants
- **Weekly** (`08:00 UTC`) → processes the past week for WEEKLY merchants based on their `live_on` day
- **Monthly top-up** (`08:00 UTC` on day 1) → creates top-up entries to meet each merchant `minimum_monthly_fee`

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
- Orders are marked as `disbursed` after being processed by scheduled or manual/historical disbursement processing.
- The scheduler uses UTC time and is enabled via `@EnableScheduling`.

## 🧠 Technical Choices

- **Spring Boot + Spring Data JPA + H2** were used to keep the solution simple, easy to run locally, and production-oriented in structure.
- **Persistent H2 file DB** (`jdbc:h2:file:./data/sequra_db`) was chosen so imported and processed data survives application restarts.
- **Disbursement domain model** includes:
	- `reference` (unique alphanumeric id per disbursement)
	- `orderAmount` (gross order amount)
	- `feeAmount` (commission or monthly top-up amount)
	- `amount` (net amount disbursed)
	- `monthlyFeeDisbursement` (flag to identify monthly minimum fee entries)
- **Order traceability** is implemented with `orders.disbursement_id` to identify exactly which orders are included in each disbursement.
- **Exactly-once processing** is handled via `orders.disbursed = true` after successful disbursement creation.

## 📌 Assumptions And Trade-Offs

Assumptions:
- Weekly merchants are disbursed according to the weekday in `live_on`.
- Commission is applied per order and rounded to 2 decimals (`HALF_UP`).
- Historical processing only includes pending orders (`disbursed = false`).

Trade-offs:
- Chose readability and correctness over maximum throughput in historical processing.
- Kept a single application/service for all flows (daily, weekly, monthly, historical) for maintainability.
- Used H2 for challenge simplicity; real production deployment would likely use PostgreSQL/MySQL.

Decisions not taken:
- No asynchronous/batch queue orchestration to avoid overengineering for challenge scope.
- No distributed lock mechanism (single-node execution assumed for this challenge).

## 📊 Yearly Reporting Table

Final values computed from processed disbursement data:

Note: yearly reporting is grouped by **disbursement date** (`disbursement.date`), not by order creation date.

| Year | Number of disbursements | Amount disbursed to merchants | Amount of order fees | Number of monthly fees charged | Amount of monthly fees charged |
| --- | ---: | ---: | ---: | ---: | ---: |
| 2022 | 1,568 | 37,149,198.23 EUR | 335,693.70 EUR | 29 | 529.90 EUR |
| 2023 | 10,489 | 188,344,721.55 EUR | 1,707,244.43 EUR | 120 | 2,030.22 EUR |

Use this SQL in H2 Console to generate the report values:

```sql
SELECT
	EXTRACT(YEAR FROM d.date) AS report_year,
	COUNT(*) AS number_of_disbursements,
	ROUND(SUM(CASE WHEN d.monthly_fee_disbursement = FALSE THEN d.amount ELSE 0 END), 2) AS amount_disbursed_to_merchants,
	ROUND(SUM(CASE WHEN d.monthly_fee_disbursement = FALSE THEN d.fee_amount ELSE 0 END), 2) AS amount_of_order_fees,
	SUM(CASE WHEN d.monthly_fee_disbursement = TRUE THEN 1 ELSE 0 END) AS number_of_monthly_fees_charged,
	ROUND(SUM(CASE WHEN d.monthly_fee_disbursement = TRUE THEN d.amount ELSE 0 END), 2) AS amount_of_monthly_fees_charged
FROM disbursement d
WHERE EXTRACT(YEAR FROM d.date) IN (2022, 2023)
GROUP BY EXTRACT(YEAR FROM d.date)
ORDER BY EXTRACT(YEAR FROM d.date);
```

## 🚧 Areas To Improve With More Time

- Add integration tests for end-to-end disbursement scenarios (daily/weekly/monthly/historical).
- Add performance optimizations for very large datasets (chunked updates, batch flush tuning, metrics).
- Add operational endpoints for progress/ETA during historical processing.
- Add idempotency and safety controls for concurrent manual and scheduled execution.

## 🤖 AI Usage (Optional Transparency)

AI tools were used as a productivity aid for:
- Refactoring and code-structure suggestions.
- Drafting documentation sections and endpoint descriptions.
- Reviewing edge cases and reporting requirements.

All generated output was reviewed and adapted manually, and final implementation decisions were taken based on challenge requirements.

Full prompt history: `AI_PROMPTS_LOG.md`.

---

If you want to change the database settings or add persistent storage, update `src/main/resources/application.properties`.
