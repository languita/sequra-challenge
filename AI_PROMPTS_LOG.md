# AI Prompts Log

This file summarizes the main AI prompts used during the challenge and explains how AI output was reviewed and adapted.

## Tools Used

- Assistant: GitHub Copilot Chat (GPT-5.3-Codex)
- Purpose: implementation support, refactoring, debugging, and documentation

## Representative Prompt History

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
- Short conversational messages (for example, simple acknowledgements) are omitted unless they changed implementation decisions.
