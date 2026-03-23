# AI Prompts Log

This file summarizes the main AI prompts used during the challenge and explains how AI output was reviewed and adapted.

## Tools Used

- Assistant: GitHub Copilot Chat (GPT-5.3-Codex)
- Purpose: implementation support, refactoring, debugging, and documentation

## Representative Prompt History

🧾 Prompt Log – Project Creation (SeQura Backend Challenge) - Design and start of architecture from ChatGPT
🏗️ Project Setup & Architecture

- "I am building a backend service for the SeQura challenge using Spring Boot. Can you outline the main components (entities, repositories, services, controllers) and how they should interact in a clean architecture?"

- "Based on the SeQura challenge requirements, what REST endpoints should I implement to import merchants and orders from CSV files and manage disbursements?"

- "How should I design the data model for merchants, orders, and disbursements, including relationships and key fields required for daily and weekly processing?"

📂 CSV Import & Data Handling

- "What is the best way to implement a CSV import endpoint in Spring Boot for merchants using Apache Commons CSV, considering the file uses semicolon delimiters?"

- "How should I implement an endpoint to import orders from CSV and correctly associate each order with its merchant using a reference field?"

- "Should I create dedicated endpoints like /merchants/import and /orders/import, and what should their request and response structure look like?"

🗄️ Database & Persistence

- "If I am using JPA entities directly without DTOs, do I still need to configure a database, and what is the simplest setup for this challenge?"

- "Can you provide a complete Spring Boot configuration using an H2 in-memory database, including JPA settings and SQL logging for debugging?"

- "What tables and relationships will be generated from my entities, and how should I structure foreign keys between orders, merchants, and disbursements?"

⚙️ Batch Processing & Scheduling

- "How should I implement scheduled jobs in Spring Boot to process disbursements daily and weekly according to merchant configuration?"

- "For daily merchants, how do I process orders from the previous day, and for weekly merchants, how do I determine when to trigger disbursement based on their live_on date?"

- "How should I design the batch logic to run at 08:00 UTC and ensure that orders are processed only once using a disbursed flag?"

💰 Business Logic (Disbursements)

- "How should I calculate disbursements by aggregating orders per merchant within a given date range and persist the results?"

- "How should I implement the minimum_monthly_fee requirement by creating a monthly job that tops up the merchant's disbursements if they do not reach the minimum?"

- "How should I structure the DisbursementService to handle daily, weekly, and monthly logic in a clean and maintainable way?"

🧱 Implementation Details

- "What is the correct way to model the relationship between Order and Merchant using JPA annotations, and why is it better than using a reference string?"

- "What repositories do I need for Merchant, Order, and Disbursement, and what custom queries should I define for batch processing?"

- "How should I structure the scheduler class to trigger different disbursement processes while keeping the logic separated in the service layer?"

📄 Documentation & Delivery

- "Can you generate a professional README file explaining how to run the project, import data, and understand the disbursement logic?"

- "What key technical decisions and assumptions should I document in the README to make the solution clear for reviewers?"


📄 Implementation from Visual Studio Copilot

1. `How can I prevent database data from being lost on shutdown?`
2. `Should I keep ddl-auto as update, or set it back to create-drop for delivery?`
3. `InvalidDataAccessApiUsageException...` (sorting/pagination runtime error)
4. `Set schedulers to run at 09:45 and keep previous cron expressions commented.`
5. `I do not see fee calculation from the requirements. Is it implemented?`
6. `Implement commission tiers: 1.00% / 0.95% / 0.85%.`
7. `Scheduler is not running. Can we move it to 11:00 UTC?`
8. `Set it to 11:15.`
9. `Why were disbursements created today if orders are very old?`
10. `Generate disbursements for all preexisting orders using merchant frequency and fees.`
11. `Monthly-fee disbursements must be marked in the disbursements table.`
12. `Add fee_amount and order_amount columns to disbursement.`
13. `Update README with latest changes and initial order-processing instructions.`
14. `Apply disbursement requirements: exactly once, unique alphanumeric reference, traceable orders/amounts/fees, daily by 08:00 UTC, weekly by live_on.`
15. `Include required yearly reporting table in README.`

## How AI Output Was Adapted

- Suggestions were checked against challenge requirements before being accepted.
- Scheduling values were temporarily adjusted for local testing and later aligned with requirement timing.
- Reporting fields were added to disbursement data (`orderAmount`, `feeAmount`, `monthlyFeeDisbursement`, `reference`).
- Historical backfill behavior was tuned for large datasets and exactly-once semantics.
- README content was rewritten into a submission-friendly format and expanded with required reporting sections.

## Verification Process

- Compilation checks were run after major code updates.
- Runtime behavior was verified through API execution and H2 database inspection.
- Reporting-related changes were validated with aggregate queries over disbursement data.

## Scope Note

- This log contains the main implementation prompts.
- Initial project-creation prompts done in external chats are not included in this file.
- Short conversational messages (for example, simple acknowledgements) are omitted unless they changed implementation decisions.
