# KPI Dashboard
 
A Python script that calculates business KPIs from CSV data across Sales, Products, Marketing, Inventory, and Customer datasets.
 
---
 
## Requirements
 
- Python 3.10+
- No external dependencies (standard library only)
---
 
## File Structure
 
```
project/
├── kpi_calculations.py
├── README.md
└── files/
    ├── sales.csv
    ├── products.csv
    ├── marketing.csv
    ├── customers.csv
    └── inventory.csv
```
 
---
 
## CSV Schemas
 
| File | Fields |
|---|---|
| `sales.csv` | `customer_id, product_id, quantity, order_date, region, price, revenue` |
| `products.csv` | `product_id, category, price, cost` |
| `marketing.csv` | `campaign_id, channel, cost, conversions, date` |
| `customers.csv` | `customer_id, age, gender, country, sign_up_date` |
| `inventory.csv` | `inventory_id, product_id, stock_level, warehouse, date` |
 
Dates should be in `YYYY-MM-DD`, `DD/MM/YYYY`, or `MM/DD/YYYY` format.
 
---
 
## KPIs Calculated
 
- **Total Revenue** — `SUM(price × quantity)`
- **Revenue Growth %** — change vs prior period
- **Profit** — revenue minus COGS and marketing costs
- **Inventory Turnover** — COGS / average inventory
- **Customer Retention %** — returning customers vs period start
- **General Budget** — based on expected sales and net profit % (default 15%)
- **Best Product** — highest revenue product in the period
All KPIs are evaluated against alert thresholds: **-5%** triggers a negative alert, **+10%** triggers a positive alert.
 
---
 
## Usage
 
```bash
python kpi_calculations.py
```
 
Output is printed to the console as a formatted report covering the current year (2025) vs prior year (2024), with alerts for each KPI.
 
---
 
## Notes
 
- Reporting periods and `expected_sales_units` / `cash_reserves` are set in `main()`.
- The 15% default net profit percentage is sourced from an industry average and can be changed in the `general_budget()` call.
- Composite KPIs call primitive functions — do not re-implement logic inline when filling in the functions.
 