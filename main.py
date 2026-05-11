"""
KPI Dashboard Calculations
==========================
Data sources (CSVs) loaded from /files/:
    - sales.csv      : customer_id, product_id, quantity, order_date, region, price, revenue
    - products.csv   : product_id, category, price, cost
    - marketing.csv  : campaign_id, channel, cost, conversions, date
    - customers.csv  : customer_id, age, gender, country, sign_up_date
    - inventory.csv  : inventory_id, product_id, stock_level, warehouse, date
"""

import csv
from datetime import date
from typing import Optional


# ---------------------------------------------------------------------------
# DATA LOADING
# ---------------------------------------------------------------------------

def load_csv(filepath: str) -> list[dict]:
    # Load and return all rows from a CSV as a list of dicts
    pass


def filter_by_date_range(
    rows: list[dict],
    date_field: str,
    start: Optional[date],
    end: Optional[date],
) -> list[dict]:
    # Return rows where date_field falls within [start, end] (inclusive)
    # Pass None for either bound to leave that side open
    pass


# ---------------------------------------------------------------------------
# PRIMITIVE KPIs
# ---------------------------------------------------------------------------

def total_revenue(
    sales: list[dict],
    start: Optional[date] = None,
    end: Optional[date] = None,
) -> float:
    # KPI: Total Revenue ($) = SUM(price x quantity) for the period
    pass


def total_cogs(
    sales: list[dict],
    products: list[dict],
    start: Optional[date] = None,
    end: Optional[date] = None,
) -> float:
    # Cost of Goods Sold = SUM(cost x quantity), joining sales -> products on product_id
    pass


def total_marketing_cost(
    marketing: list[dict],
    start: Optional[date] = None,
    end: Optional[date] = None,
) -> float:
    # Total marketing spend for the period = SUM(cost)
    pass


def net_sales(
    sales: list[dict],
    products: list[dict],
    marketing: list[dict],
    start: Optional[date] = None,
    end: Optional[date] = None,
) -> float:
    # Net Sales = total_revenue() - total_cogs() - total_marketing_cost()
    pass


def average_inventory(
    inventory: list[dict],
    start: Optional[date] = None,
    end: Optional[date] = None,
) -> float:
    # Average Inventory = (beginning stock_level + ending stock_level) / 2
    # Use earliest and latest records within the date range
    pass


def customer_count_at_start(
    customers: list[dict],
    sales: list[dict],
    start: Optional[date] = None,
) -> int:
    # Count of customers who made at least one purchase before `start`
    pass


def new_customers_in_period(
    customers: list[dict],
    start: Optional[date] = None,
    end: Optional[date] = None,
) -> int:
    # Count of customers whose sign_up_date falls within [start, end]
    pass


# ---------------------------------------------------------------------------
# COMPOSITE KPIs
# These must call the primitives above — do not re-implement their logic
# ---------------------------------------------------------------------------

def growth_revenue(
    sales: list[dict],
    current_start: date,
    current_end: date,
    previous_start: date,
    previous_end: date,
) -> float:
    # KPI: Revenue Growth (%) = ((current - previous) / previous) x 100
    # Call total_revenue() for both periods
    pass


def profit(
    sales: list[dict],
    products: list[dict],
    marketing: list[dict],
    start: Optional[date] = None,
    end: Optional[date] = None,
) -> float:
    # KPI: Profit ($) — call net_sales()
    pass


def inventory_turnover(
    sales: list[dict],
    products: list[dict],
    inventory: list[dict],
    start: Optional[date] = None,
    end: Optional[date] = None,
) -> float:
    # KPI: Inventory Turnover (ratio) = total_cogs() / average_inventory()
    pass


def customer_retention(
    customers: list[dict],
    sales: list[dict],
    start: Optional[date] = None,
    end: Optional[date] = None,
) -> float:
    # KPI: Customer Retention (%) = ((total_at_end - new_customers) / total_at_start) x 100
    # Call customer_count_at_start() and new_customers_in_period()
    pass


def general_budget(
    sales: list[dict],
    products: list[dict],
    marketing: list[dict],
    expected_sales_units: float,
    cash_reserves: float = 0.0,
    net_profit_pct: float = 0.15,
    start: Optional[date] = None,
    end: Optional[date] = None,
) -> float:
    # KPI: General Budget ($) = (expected_sales x net_profit_pct) + cash_reserves
    # net_profit_pct defaults to 15% (avg from internet, changeable)
    # Derive actual net_profit_pct from profit() / total_revenue() if data available
    pass


# ---------------------------------------------------------------------------
# BEST PRODUCT
# ---------------------------------------------------------------------------

def best_product_by_revenue(
    sales: list[dict],
    products: list[dict],
    start: Optional[date] = None,
    end: Optional[date] = None,
) -> dict:
    # Return the product with the highest total revenue in the period
    # Use filter_by_date_range() — do not re-implement date filtering
    pass


# ---------------------------------------------------------------------------
# ALERT THRESHOLDS
# Applied uniformly to all KPIs ("assume applies to all")
#   Negative: current < prior by >= 5%
#   Positive: current > prior by >= 10%
# ---------------------------------------------------------------------------

def evaluate_alert(
    kpi_name: str,
    current_value: float,
    prior_value: float,
    negative_threshold_pct: float = -5.0,
    positive_threshold_pct: float = 10.0,
) -> dict:
    # Return a dict with: kpi, current, prior, change_pct, alert ("NEGATIVE"/"POSITIVE"/"OK"), message
    pass


# ---------------------------------------------------------------------------
# MAIN
# ---------------------------------------------------------------------------

def main():
    sales     = load_csv("/files/sales.csv")
    products  = load_csv("/files/products.csv")
    marketing = load_csv("/files/marketing.csv")
    customers = load_csv("/files/customers.csv")
    inventory = load_csv("/files/inventory.csv")

    curr_start = date(2025, 1, 1)
    curr_end   = date(2025, 12, 31)
    prev_start = date(2024, 1, 1)
    prev_end   = date(2024, 12, 31)

    rev_curr      = total_revenue(sales, curr_start, curr_end)
    rev_prev      = total_revenue(sales, prev_start, prev_end)
    growth        = growth_revenue(sales, curr_start, curr_end, prev_start, prev_end)
    profit_curr   = profit(sales, products, marketing, curr_start, curr_end)
    profit_prev   = profit(sales, products, marketing, prev_start, prev_end)
    inv_turn_curr = inventory_turnover(sales, products, inventory, curr_start, curr_end)
    inv_turn_prev = inventory_turnover(sales, products, inventory, prev_start, prev_end)
    ret_curr      = customer_retention(customers, sales, curr_start, curr_end)
    ret_prev      = customer_retention(customers, sales, prev_start, prev_end)
    budget        = general_budget(sales, products, marketing, expected_sales_units=1000,
                                   cash_reserves=5000.0, start=prev_start, end=prev_end)
    best          = best_product_by_revenue(sales, products, curr_start, curr_end)

    print("=" * 60)
    print("  BUSINESS KPI REPORT")
    print(f"  Current : {curr_start} → {curr_end}")
    print(f"  Prior   : {prev_start} → {prev_end}")
    print("=" * 60)
    print(f"\n  Total Revenue   2025: ${rev_curr or 0:,.2f}  |  2024: ${rev_prev or 0:,.2f}")
    print(f"  Revenue Growth      : {growth or 0:+.2f}%")
    print(f"  Profit          2025: ${profit_curr or 0:,.2f}  |  2024: ${profit_prev or 0:,.2f}")
    print(f"  Inv. Turnover   2025: {inv_turn_curr or 0:.2f}  |  2024: {inv_turn_prev or 0:.2f}")
    print(f"  Cust. Retention 2025: {ret_curr or 0:.1f}%  |  2024: {ret_prev or 0:.1f}%")
    print(f"  General Budget      : ${budget or 0:,.2f}")
    if best:
        print(f"  Best Product        : {best.get('product_id')} | "
              f"{best.get('category')} | ${best.get('revenue') or 0:,.2f}")
 
    print("\n" + "-" * 60)
    print("  ALERTS")
    print("-" * 60)
    for name, curr_val, prev_val in [
        ("Total Revenue",      rev_curr,      rev_prev),
        ("Revenue Growth %",   growth,        0.0),
        ("Profit",             profit_curr,   profit_prev),
        ("Inv. Turnover",      inv_turn_curr, inv_turn_prev),
        ("Cust. Retention",    ret_curr,      ret_prev),
    ]:
        result = evaluate_alert(name, curr_val, prev_val)
        if result is None:
            continue
        symbol = "🔴" if result["alert"] == "NEGATIVE" else ("🟢" if result["alert"] == "POSITIVE" else "✅")
        print(f"  {symbol}  {result['message']}")
 
    print("\n" + "=" * 60)


if __name__ == "__main__":
    main()