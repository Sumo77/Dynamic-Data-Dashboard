-- =====================================================================
-- refresh_kpi_snapshot.sql  (SQLite)
-- Re-run this file every time new CSV data has been loaded into the
-- base tables. This is the SQLite equivalent of the Oracle
-- refresh_kpi_snapshot procedure (SQLite has no stored procedures,
-- so the Java "KPI engine" component executes this script via JDBC).
-- =====================================================================

DELETE FROM kpi_snapshot;

INSERT INTO kpi_snapshot (
    kpi_year, total_revenue, revenue_growth_pct,
    profit, profit_margin_pct, inventory_turnover,
    retention_rate_pct, marketing_cost_total,
    conversions_total, cost_per_conversion
)
WITH
yr_rev AS (
    SELECT CAST(strftime('%Y', order_date) AS INTEGER) AS yr, SUM(revenue) AS rev
    FROM sales GROUP BY yr
),
yr_cogs AS (
    SELECT CAST(strftime('%Y', s.order_date) AS INTEGER) AS yr,
           SUM(p.cost * s.quantity) AS cogs
    FROM sales s JOIN products p ON p.product_id = s.product_id
    GROUP BY yr
),
yr_mkt AS (
    SELECT CAST(strftime('%Y', campaign_date) AS INTEGER) AS yr,
           SUM(cost) AS mkt_cost,
           SUM(conversions) AS conv_total,
           ROUND(SUM(cost) / NULLIF(SUM(conversions), 0), 2) AS cost_per_conv
    FROM marketing GROUP BY yr
),
rev_growth AS (
    SELECT yr, rev,
           ROUND((rev - LAG(rev) OVER (ORDER BY yr)) * 100.0
                / NULLIF(LAG(rev) OVER (ORDER BY yr), 0), 2) AS growth
    FROM yr_rev
),
profit_calc AS (
    SELECT r.yr,
           (r.rev - c.cogs - COALESCE(m.mkt_cost, 0))            AS profit,
           ROUND((r.rev - c.cogs) * 100.0 / NULLIF(r.rev, 0), 2) AS margin_pct
    FROM yr_rev r
    JOIN yr_cogs c ON c.yr = r.yr
    LEFT JOIN yr_mkt m ON m.yr = r.yr
),
turn AS (
    SELECT c.yr,
           ROUND(c.cogs / NULLIF(a.avg_inv_value, 0), 2) AS turnover
    FROM (SELECT CAST(strftime('%Y', s.order_date) AS INTEGER) AS yr,
                 SUM(p.cost * s.quantity) AS cogs
          FROM sales s JOIN products p ON p.product_id = s.product_id
          GROUP BY yr) c
    JOIN (SELECT CAST(strftime('%Y', snapshot_date) AS INTEGER) AS yr,
                 AVG(stock_level * p.cost) AS avg_inv_value
          FROM inventory i JOIN products p ON p.product_id = i.product_id
          GROUP BY yr) a ON a.yr = c.yr
),
cust_year AS (
    SELECT customer_id, CAST(strftime('%Y', order_date) AS INTEGER) AS yr
    FROM sales GROUP BY customer_id, yr
),
ret AS (
    SELECT cy.yr,
           ROUND(
             COUNT(DISTINCT CASE WHEN EXISTS (
                   SELECT 1 FROM cust_year p
                   WHERE p.customer_id = cy.customer_id AND p.yr = cy.yr - 1
                 ) THEN cy.customer_id END) * 100.0
             / NULLIF((SELECT COUNT(DISTINCT customer_id)
                       FROM cust_year p2 WHERE p2.yr = cy.yr - 1), 0), 2
           ) AS ret_pct
    FROM cust_year cy
    GROUP BY cy.yr
)
SELECT rg.yr, rg.rev, rg.growth,
       pc.profit, pc.margin_pct, turn.turnover, ret.ret_pct,
       mk.mkt_cost, mk.conv_total, mk.cost_per_conv
FROM rev_growth rg
LEFT JOIN profit_calc pc ON pc.yr = rg.yr
LEFT JOIN turn         ON turn.yr = rg.yr
LEFT JOIN ret          ON ret.yr  = rg.yr
LEFT JOIN yr_mkt mk    ON mk.yr   = rg.yr;

-- Inspect results:
SELECT * FROM kpi_snapshot ORDER BY kpi_year;
