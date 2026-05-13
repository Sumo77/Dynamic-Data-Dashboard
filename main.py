import csv
from datetime import date


# --------------------------------------------------
# 1. DATA LOADING
# --------------------------------------------------

def load_csv(path):
    with open(path, newline="", encoding="utf-8") as file:
        return list(csv.DictReader(file))


def in_range(row, field, start, end):
    row_date = date.fromisoformat(row[field])
    return start <= row_date <= end


# --------------------------------------------------
# 2. FILTERS
# --------------------------------------------------

def apply_filters(sales, products, region="", category=""):
    filtered_sales = sales

    if region:
        filtered_sales = [
            row for row in filtered_sales
            if row["region"].lower() == region.lower()
        ]

    if category:
        product_ids = {
            product["product_id"]
            for product in products
            if product["category"].lower() == category.lower()
        }

        filtered_sales = [
            row for row in filtered_sales
            if row["product_id"] in product_ids
        ]

    return filtered_sales


# --------------------------------------------------
# 3. KPI CALCULATIONS
# --------------------------------------------------

def total_revenue(sales, start, end):
    return sum(
        float(row["revenue"])
        for row in sales
        if in_range(row, "order_date", start, end)
    )


def total_cogs(sales, products, start, end):
    product_costs = {
        product["product_id"]: float(product["cost"])
        for product in products
    }

    return sum(
        float(row["quantity"]) * product_costs.get(row["product_id"], 0)
        for row in sales
        if in_range(row, "order_date", start, end)
    )


def marketing_cost(marketing, start, end):
    return sum(
        float(row["cost"])
        for row in marketing
        if in_range(row, "date", start, end)
    )


def profit(sales, products, marketing, start, end):
    return (
        total_revenue(sales, start, end)
        - total_cogs(sales, products, start, end)
        - marketing_cost(marketing, start, end)
    )


def growth(current, previous):
    if previous == 0:
        return 0

    return ((current - previous) / previous) * 100


def average_inventory(inventory, start, end):
    rows = [
        row for row in inventory
        if in_range(row, "date", start, end)
    ]

    if not rows:
        return 0

    first_stock = float(rows[0]["stock_level"])
    last_stock = float(rows[-1]["stock_level"])

    return (first_stock + last_stock) / 2


def inventory_turnover(sales, products, inventory, start, end):
    avg_inv = average_inventory(inventory, start, end)

    if avg_inv == 0:
        return 0

    return total_cogs(sales, products, start, end) / avg_inv


def customer_retention(sales, start, end):
    previous_customers = {
        row["customer_id"]
        for row in sales
        if date.fromisoformat(row["order_date"]) < start
    }

    returning_customers = {
        row["customer_id"]
        for row in sales
        if start <= date.fromisoformat(row["order_date"]) <= end
        and row["customer_id"] in previous_customers
    }

    if len(previous_customers) == 0:
        return 0

    return (len(returning_customers) / len(previous_customers)) * 100


def general_budget(sales, products, marketing, expected_units, cash_reserves, start, end):
    revenue = total_revenue(sales, start, end)
    current_profit = profit(sales, products, marketing, start, end)

    profit_margin = current_profit / revenue if revenue > 0 else 0.15

    avg_price = sum(float(row["price"]) for row in sales) / len(sales)

    return (expected_units * avg_price * profit_margin) + cash_reserves


def best_product(sales, products, start, end):
    product_totals = {}

    for row in sales:
        if in_range(row, "order_date", start, end):
            product_id = row["product_id"]
            product_totals[product_id] = product_totals.get(product_id, 0) + float(row["revenue"])

    if not product_totals:
        return None

    best_id = max(product_totals, key=product_totals.get)

    product_info = next(
        (product for product in products if product["product_id"] == best_id),
        {}
    )

    return {
        "product_id": best_id,
        "category": product_info.get("category", "Unknown"),
        "revenue": product_totals[best_id]
    }


def monthly_revenue(sales):
    monthly_totals = {}

    for row in sales:
        month = row["order_date"][:7]
        monthly_totals[month] = monthly_totals.get(month, 0) + float(row["revenue"])

    return sorted(monthly_totals.items())


# --------------------------------------------------
# 4. ALERT LOGIC
# --------------------------------------------------

def alert(name, current, previous):
    change = growth(current, previous)

    if change <= -5:
        return f"🔴 {name} dropped by {abs(change):.2f}%"

    if change >= 10:
        return f"🟢 {name} increased by {change:.2f}%"

    return f"✅ {name} is stable ({change:.2f}%)"


def retention_alert(current, previous):
    change = current - previous

    if change < -2:
        return f"🔴 Customer Retention dropped by {abs(change):.2f}%"

    if change > 2:
        return f"🟢 Customer Retention increased by {change:.2f}%"

    return f"✅ Customer Retention is stable ({change:.2f}%)"


# --------------------------------------------------
# 5. DISPLAY REPORT
# --------------------------------------------------

def display_report(
    sales,
    products,
    marketing,
    customers,
    inventory,
    curr_start,
    curr_end,
    prev_start,
    prev_end,
    region_filter,
    category_filter
):
    rev_curr = total_revenue(sales, curr_start, curr_end)
    rev_prev = total_revenue(sales, prev_start, prev_end)

    profit_curr = profit(sales, products, marketing, curr_start, curr_end)
    profit_prev = profit(sales, products, marketing, prev_start, prev_end)

    inv_curr = inventory_turnover(sales, products, inventory, curr_start, curr_end)
    inv_prev = inventory_turnover(sales, products, inventory, prev_start, prev_end)

    ret_curr = customer_retention(sales, curr_start, curr_end)
    ret_prev = customer_retention(sales, prev_start, prev_end)

    budget = general_budget(
        sales,
        products,
        marketing,
        expected_units=1000,
        cash_reserves=5000,
        start=prev_start,
        end=prev_end
    )

    best = best_product(sales, products, curr_start, curr_end)

    print("\n" + "=" * 70)
    print("                BUSINESS KPI REPORT")
    print("=" * 70)

    print("\nREPORTING PERIOD")
    print("-" * 70)
    print(f"Current Year : {curr_start} → {curr_end}")
    print(f"Previous Year: {prev_start} → {prev_end}")

    print("\nACTIVE FILTERS")
    print("-" * 70)
    print(f"Region   : {region_filter if region_filter else 'All'}")
    print(f"Category : {category_filter if category_filter else 'All'}")

    print("\nKPI SUMMARY")
    print("-" * 70)

    print("Total Revenue")
    print(f"  2024  : ${rev_curr:,.2f}")
    print(f"  2023  : ${rev_prev:,.2f}")
    print(f"  Growth: {growth(rev_curr, rev_prev):+.2f}%")

    print("\nProfit")
    print(f"  2024  : ${profit_curr:,.2f}")
    print(f"  2023  : ${profit_prev:,.2f}")
    print(f"  Growth: {growth(profit_curr, profit_prev):+.2f}%")

    print("\nInventory Turnover")
    print(f"  2024  : {inv_curr:,.2f}")
    print(f"  2023  : {inv_prev:,.2f}")
    print(f"  Growth: {growth(inv_curr, inv_prev):+.2f}%")

    print("\nCustomer Retention")
    print(f"  2024  : {ret_curr:.1f}%")
    print(f"  2023  : {ret_prev:.1f}%")
    print(f"  Change: {ret_curr - ret_prev:+.1f}%")

    print("\nGeneral Budget")
    print(f"  ${budget:,.2f}")

    if best:
        print("\nBEST PERFORMING PRODUCT")
        print("-" * 70)
        print(f"Product ID : {best['product_id']}")
        print(f"Category   : {best['category']}")
        print(f"Revenue    : ${best['revenue']:,.2f}")

    print("\nMONTHLY REVENUE")
    print("-" * 70)

    for month, revenue in monthly_revenue(sales):
        print(f"{month} : ${revenue:,.2f}")

    print("\nDATASET SUMMARY")
    print("-" * 70)
    print(f"Sales Records      : {len(sales):,}")
    print(f"Products           : {len(products):,}")
    print(f"Marketing Records  : {len(marketing):,}")
    print(f"Customers          : {len(customers):,}")
    print(f"Inventory Records  : {len(inventory):,}")

    print("\nALERTS")
    print("-" * 70)
    print(alert("Total Revenue", rev_curr, rev_prev))
    print(alert("Profit", profit_curr, profit_prev))
    print(alert("Inventory Turnover", inv_curr, inv_prev))
    print(retention_alert(ret_curr, ret_prev))

    print("\n" + "=" * 70)


# --------------------------------------------------
# 6. MAIN PROGRAM
# --------------------------------------------------

def main():
    sales = load_csv("files/sales.csv")
    products = load_csv("files/products.csv")
    marketing = load_csv("files/marketing.csv")
    customers = load_csv("files/customers.csv")
    inventory = load_csv("files/inventory.csv")

    print("\nFILTER OPTIONS")
    print("-" * 70)

    year_input = input("Enter year to analyse (2023/2024): ").strip()
    region_filter = input("Enter region, or press Enter for All: ").strip()
    category_filter = input("Enter product category, or press Enter for All: ").strip()

    if year_input == "":
        year = 2024
    else:
        year = int(year_input)

    curr_start = date(year, 1, 1)
    curr_end = date(year, 12, 31)

    prev_start = date(year - 1, 1, 1)
    prev_end = date(year - 1, 12, 31)

    filtered_sales = apply_filters(
        sales,
        products,
        region_filter,
        category_filter
    )

    display_report(
        filtered_sales,
        products,
        marketing,
        customers,
        inventory,
        curr_start,
        curr_end,
        prev_start,
        prev_end,
        region_filter,
        category_filter
    )


if __name__ == "__main__":
    main()