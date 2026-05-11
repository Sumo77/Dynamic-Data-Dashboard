"""
KPI Dashboard Calculations
==========================
Data sources (CSVs) loaded from /files/:
    - sales.csv      : customer_id, product_id, quantity, order_date, region, price, revenue
    - products.csv   : product_id, category, price, cost
    - marketing.csv  : campaign_id, channel, cost, conversions, date
    - customers.csv  : customer_id, age, gender, country, signup_date
    - inventory.csv  : inventory_id, product_id, stock_level, warehouse, date
"""


import csv
from datetime import date
from typing import Optional


# ---------------------------------------------------------------------------
# DATA LOADING
# ---------------------------------------------------------------------------

def load_csv(filepath: str) -> list[dict]:
    with open(filepath, newline='', encoding='utf-8') as file:
        reader = csv.DictReader(file)
        return list(reader)


def filter_by_date_range(
    rows: list[dict],
    date_field: str,
    start: Optional[date],
    end: Optional[date],
) -> list[dict]:

    filtered = []

    for row in rows:
        row_date = date.fromisoformat(row[date_field])

        if start and row_date < start:
            continue

        if end and row_date > end:
            continue

        filtered.append(row)

    return filtered


# ---------------------------------------------------------------------------
# PRIMITIVE KPIs
# ---------------------------------------------------------------------------
def monthly_revenue_growth(sales: list[dict]) -> list[dict]:
    monthly_totals = {}

    for row in sales:
        order_date = date.fromisoformat(row["order_date"])
        month_key = order_date.strftime("%Y-%m")
        revenue = float(row["revenue"])

        if month_key not in monthly_totals:
            monthly_totals[month_key] = 0.0

        monthly_totals[month_key] += revenue

    months = sorted(monthly_totals.keys())
    result = []
    previous_revenue = None

    for month in months:
        current_revenue = monthly_totals[month]

        if previous_revenue is None or previous_revenue == 0:
            growth = 0.0
        else:
            growth = ((current_revenue - previous_revenue) / previous_revenue) * 100

        result.append({
            "month": month,
            "revenue": current_revenue,
            "growth": growth
        })

        previous_revenue = current_revenue

    return result
    
def total_revenue(
    sales: list[dict],
    start: Optional[date] = None,
    end: Optional[date] = None,
) -> float:

    filtered_sales = filter_by_date_range(
        sales,
        "order_date",
        start,
        end
    )

    total = 0.0

    for row in filtered_sales:
        total += float(row["revenue"])

    return total

def growth_revenue(
    sales: list[dict],
    current_start: date,
    current_end: date,
    previous_start: date,
    previous_end: date,
) -> float:

    current = total_revenue(sales, current_start, current_end)
    previous = total_revenue(sales, previous_start, previous_end)

    if previous == 0:
        return 0.0

    return ((current - previous) / previous) * 100


def total_marketing_cost(
    marketing: list[dict],
    start: Optional[date] = None,
    end: Optional[date] = None,
) -> float:

    filtered = filter_by_date_range(
        marketing,
        "date",
        start,
        end
    )

    total = 0.0

    for row in filtered:
        total += float(row["cost"])

    return total


def net_sales(
    sales: list[dict],
    products: list[dict],
    marketing: list[dict],
    start: Optional[date] = None,
    end: Optional[date] = None,
) -> float:

    revenue = total_revenue(sales, start, end)
    cogs = total_cogs(sales, products, start, end)
    marketing_cost = total_marketing_cost(marketing, start, end)

    return revenue - cogs - marketing_cost


def average_inventory(
    inventory: list[dict],
    start: Optional[date] = None,
    end: Optional[date] = None,
) -> float:

    filtered = filter_by_date_range(
        inventory,
        "date",
        start,
        end
    )

    if not filtered:
        return 0.0

    beginning = float(filtered[0]["stock_level"])
    ending = float(filtered[-1]["stock_level"])

    return (beginning + ending) / 2


def customer_count_at_start(
    customers: list[dict],
    sales: list[dict],
    start: Optional[date] = None,
) -> int:

    if start is None:
        return 0

    customer_ids = set()

    for row in sales:
        order_date = date.fromisoformat(row["order_date"])

        if order_date < start:
            customer_ids.add(row["customer_id"])

    return len(customer_ids)


def new_customers_in_period(
    customers: list[dict],
    start: Optional[date] = None,
    end: Optional[date] = None,
) -> int:

    count = 0

    for row in customers:
        signup_date = date.fromisoformat(row["signup_date"])

        if start and signup_date < start:
            continue

        if end and signup_date > end:
            continue

        count += 1

    return count

def total_cogs(
    sales: list[dict],
    products: list[dict],
    start: Optional[date] = None,
    end: Optional[date] = None,
) -> float:

    filtered_sales = filter_by_date_range(
        sales,
        "order_date",
        start,
        end
    )

    product_costs = {}

    for product in products:
        product_costs[product["product_id"]] = float(product["cost"])

    total = 0.0

    for row in filtered_sales:
        product_id = row["product_id"]
        quantity = float(row["quantity"])

        cost = product_costs.get(product_id, 0.0)

        total += cost * quantity

    return total
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

    current = total_revenue(
        sales,
        current_start,
        current_end
    )

    previous = total_revenue(
        sales,
        previous_start,
        previous_end
    )

    if previous == 0:
        return 0.0

    growth = ((current - previous) / previous) * 100

    return growth


def profit(
    sales: list[dict],
    products: list[dict],
    marketing: list[dict],
    start: Optional[date] = None,
    end: Optional[date] = None,
) -> float:

    return net_sales(sales, products, marketing, start, end)


def inventory_turnover(
    sales: list[dict],
    products: list[dict],
    inventory: list[dict],
    start: Optional[date] = None,
    end: Optional[date] = None,
) -> float:

    cogs = total_cogs(sales, products, start, end)
    avg_inv = average_inventory(inventory, start, end)

    if avg_inv == 0:
        return 0.0

    return cogs / avg_inv


def customer_retention(
    customers: list[dict],
    sales: list[dict],
    start: Optional[date] = None,
    end: Optional[date] = None,
) -> float:

    starting_customers = customer_count_at_start(customers, sales, start)
    new_customers = new_customers_in_period(customers, start, end)

    if starting_customers == 0:
        return 0.0

    total_at_end = starting_customers + new_customers

    retention = ((total_at_end - new_customers) / starting_customers) * 100

    return retention


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

    revenue = total_revenue(sales, start, end)
    calculated_profit = profit(sales, products, marketing, start, end)

    if revenue > 0:
        net_profit_pct = calculated_profit / revenue

    avg_price = 0.0
    if sales:
        total_price = 0.0
        count = 0

        for row in sales:
            total_price += float(row["price"])
            count += 1

        avg_price = total_price / count

    expected_revenue = expected_sales_units * avg_price

    return (expected_revenue * net_profit_pct) + cash_reserves

# ---------------------------------------------------------------------------
# BEST PRODUCT
# ---------------------------------------------------------------------------

def best_product_by_revenue(
    sales: list[dict],
    products: list[dict],
    start: Optional[date] = None,
    end: Optional[date] = None,
) -> dict:

    filtered_sales = filter_by_date_range(sales, "order_date", start, end)

    product_revenue = {}

    for row in filtered_sales:
        product_id = row["product_id"]
        revenue = float(row["revenue"])

        if product_id not in product_revenue:
            product_revenue[product_id] = 0.0

        product_revenue[product_id] += revenue

    if not product_revenue:
        return {}

    best_product_id = max(product_revenue, key=product_revenue.get)

    product_category = "Unknown"

    for product in products:
        if product["product_id"] == best_product_id:
            product_category = product.get("category", "Unknown")
            break

    return {
        "product_id": best_product_id,
        "category": product_category,
        "revenue": product_revenue[best_product_id]
    }


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

    if prior_value == 0:
        change_pct = 0.0
    else:
        change_pct = ((current_value - prior_value) / prior_value) * 100

    if change_pct <= negative_threshold_pct:
        alert = "NEGATIVE"
        message = f"{kpi_name} dropped by {abs(change_pct):.2f}%"

    elif change_pct >= positive_threshold_pct:
        alert = "POSITIVE"
        message = f"{kpi_name} increased by {change_pct:.2f}%"

    else:
        alert = "OK"
        message = f"{kpi_name} is stable ({change_pct:.2f}%)"

    return {
        "kpi": kpi_name,
        "current": current_value,
        "prior": prior_value,
        "change_pct": change_pct,
        "alert": alert,
        "message": message
    }


# ---------------------------------------------------------------------------
# MAIN
# ---------------------------------------------------------------------------

def main():

    sales     = load_csv("files/sales.csv")
    products  = load_csv("files/products.csv")
    marketing = load_csv("files/marketing.csv")
    customers = load_csv("files/customers.csv")
    inventory = load_csv("files/inventory.csv")

    curr_start = date(2024, 1, 1)
    curr_end   = date(2024, 12, 31)

    prev_start = date(2023, 1, 1)
    prev_end   = date(2023, 12, 31)

    rev_curr      = total_revenue(sales, curr_start, curr_end)
    rev_prev      = total_revenue(sales, prev_start, prev_end)

    growth        = growth_revenue(
        sales,
        curr_start,
        curr_end,
        prev_start,
        prev_end
    )

    profit_curr   = profit(
        sales,
        products,
        marketing,
        curr_start,
        curr_end
    )

    profit_prev   = profit(
        sales,
        products,
        marketing,
        prev_start,
        prev_end
    )

    inv_turn_curr = inventory_turnover(
        sales,
        products,
        inventory,
        curr_start,
        curr_end
    )

    inv_turn_prev = inventory_turnover(
        sales,
        products,
        inventory,
        prev_start,
        prev_end
    )

    ret_curr      = customer_retention(
        customers,
        sales,
        curr_start,
        curr_end
    )

    ret_prev      = customer_retention(
        customers,
        sales,
        prev_start,
        prev_end
    )

    budget = general_budget(
        sales,
        products,
        marketing,
        expected_sales_units=1000,
        cash_reserves=5000.0,
        start=prev_start,
        end=prev_end
    )

    best = best_product_by_revenue(
        sales,
        products,
        curr_start,
        curr_end
    )

    monthly_growth = monthly_revenue_growth(sales)

    print("\n" + "=" * 75)
    print("                     BUSINESS KPI REPORT")
    print("=" * 75)

    print(f"\n  Reporting Period")
    print(f"  Current : {curr_start} → {curr_end}")
    print(f"  Prior   : {prev_start} → {prev_end}")

    print("\n" + "-" * 75)
    print("  KPI SUMMARY")
    print("-" * 75)

    print(f"  {'Total Revenue':<22} 2024: ${rev_curr or 0:>15,.2f}  |  2023: ${rev_prev or 0:>15,.2f}")

    print(f"  {'Revenue Growth':<22} {(growth or 0):>+15.2f}%")

    revenue_status = "Increase 📈" if growth > 0 else "Decrease 📉"

    print(f"  {'Revenue Trend':<22} {revenue_status}")

    print()

    print(f"  {'Profit':<22} 2024: ${profit_curr or 0:>15,.2f}  |  2023: ${profit_prev or 0:>15,.2f}")

    profit_change = 0

    if profit_prev != 0:
        profit_change = ((profit_curr - profit_prev) / profit_prev) * 100

    print(f"  {'Profit Change':<22} {profit_change:+.2f}%")

    print()

    print(f"  {'Inv. Turnover':<22} 2024: {inv_turn_curr or 0:>15,.2f}  |  2023: {inv_turn_prev or 0:>15,.2f}")

    inventory_status = "Efficient ✅" if inv_turn_curr > inv_turn_prev else "Declining ⚠️"

    print(f"  {'Inventory Status':<22} {inventory_status}")

    print()

    print(f"  {'Cust. Retention':<22} 2024: {ret_curr or 0:>14.1f}%  |  2023: {ret_prev or 0:>14.1f}%")

    retention_status = "Stable 👥" if ret_curr >= ret_prev else "Dropping ⚠️"

    print(f"  {'Retention Status':<22} {retention_status}")

    print()

    print(f"  {'General Budget':<22} ${budget or 0:>15,.2f}")

    print()

    if best:

        print("  BEST PERFORMING PRODUCT")

        print(f"    Product ID : {best.get('product_id')}")
        print(f"    Category   : {best.get('category')}")
        print(f"    Revenue    : ${best.get('revenue') or 0:,.2f}")

    print("\n" + "-" * 75)
    print("  MONTHLY REVENUE GROWTH")
    print("-" * 75)

    print(f"  {'Month':<15} {'Revenue':>20} {'Growth':>15}")

    print("  " + "-" * 52)

    for row in monthly_growth:

        print(
            f"  {row['month']:<15} "
            f"${row['revenue']:>19,.2f} "
            f"{row['growth']:>+14.2f}%"
        )

    print("\n" + "-" * 75)
    print("  DATASET SUMMARY")
    print("-" * 75)

    print(f"  Total Sales Records      : {len(sales):,}")
    print(f"  Total Products           : {len(products):,}")
    print(f"  Total Marketing Records  : {len(marketing):,}")
    print(f"  Total Customers          : {len(customers):,}")
    print(f"  Total Inventory Records  : {len(inventory):,}")

    print("\n" + "-" * 75)
    print("  ALERTS")
    print("-" * 75)

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

        symbol = (
            "🔴" if result["alert"] == "NEGATIVE"
            else ("🟢" if result["alert"] == "POSITIVE" else "✅")
        )

        print(f"  {symbol}  {result['message']}")

    print("\n" + "=" * 75)


if __name__ == "__main__":
    main()