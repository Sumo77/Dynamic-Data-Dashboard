-- IDE   : VSCode + Oracle SQL Developer Extension
-- HOW TO RUN
--   1. Connect as SYSTEM (or any DBA) to your PDB (e.g. FREEPDB1).
--   2. Run SECTION 0 once to create the schema user if need.
--   3. Reconnect as RETAIL_DASH and run SECTIONS 1 - 5.
--   4. Use the VSCode "Import Data" wizard to load the 5 CSVs
--      AFTER section 1 (tables created), BEFORE section 4 (queries).
--      Load order: products -> customers -> marketing -> inventory -> sales
--**
-- SECTION 0 : CREATE SCHEMA USER  (run as SYSTEM)
-- Comment this block out if the user already exists.
-- CREATE USER retail_dash IDENTIFIED BY "Retail#2026";
-- GRANT CONNECT, RESOURCE, CREATE VIEW TO retail_dash;
-- ALTER USER retail_dash QUOTA UNLIMITED ON USERS;


-- SECTION 1 : BASE TABLES (5 from CSVs)  -- run as RETAIL_DASH
-- 1. PRODUCTS
CREATE TABLE products (
    product_id   NUMBER(10)   PRIMARY KEY,
    category     VARCHAR2(50) NOT NULL,
    price        NUMBER(10,2) NOT NULL,
    cost         NUMBER(10,2) NOT NULL
);

-- 2. CUSTOMERS
CREATE TABLE customers (
    customer_id  NUMBER(10)   PRIMARY KEY,
    age          NUMBER(3),
    gender       VARCHAR2(10),
    country      VARCHAR2(5),
    signup_date  DATE
);

-- 3. MARKETING
CREATE TABLE marketing (
    campaign_id   NUMBER(10)   PRIMARY KEY,
    channel       VARCHAR2(30) NOT NULL,
    cost          NUMBER(12,2) NOT NULL,
    conversions   NUMBER(10)   NOT NULL,
    campaign_date DATE         NOT NULL
);

-- 4. INVENTORY
CREATE TABLE inventory (
    inventory_id  NUMBER(10)   PRIMARY KEY,
    product_id    NUMBER(10)   NOT NULL,
    stock_level   NUMBER(10)   NOT NULL,
    warehouse     VARCHAR2(10) NOT NULL,
    snapshot_date DATE         NOT NULL,
    CONSTRAINT fk_inv_product FOREIGN KEY (product_id)
        REFERENCES products(product_id)
);

-- 5. SALES
CREATE TABLE sales (
    order_id    NUMBER(12)   PRIMARY KEY,
    customer_id NUMBER(10)   NOT NULL,
    product_id  NUMBER(10)   NOT NULL,
    quantity    NUMBER(6)    NOT NULL,
    order_date  DATE         NOT NULL,
    region      VARCHAR2(30),
    price       NUMBER(10,2) NOT NULL,
    revenue     NUMBER(12,2) NOT NULL,
    CONSTRAINT fk_sales_cust FOREIGN KEY (customer_id)
        REFERENCES customers(customer_id),
    CONSTRAINT fk_sales_prod FOREIGN KEY (product_id)
        REFERENCES products(product_id)
);

-- Indexes for dashboard performance
CREATE INDEX ix_sales_date    ON sales(order_date);
CREATE INDEX ix_sales_cust    ON sales(customer_id);
CREATE INDEX ix_sales_prod    ON sales(product_id);
CREATE INDEX ix_mkt_date      ON marketing(campaign_date);
CREATE INDEX ix_inv_prod_date ON inventory(product_id, snapshot_date);



-- SECTION 2 : CALCULATION TABLE
-- Stores precomputed KPI values per year for fast dashboard tiles
CREATE TABLE kpi_snapshot (
    snapshot_id         NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    kpi_year            NUMBER(4)    NOT NULL,
    revenue_total       NUMBER(14,2),
    revenue_growth_pct  NUMBER(7,2),   -- Financial KPI
    conversion_rate_pct NUMBER(7,2),   -- Marketing KPI
    marketing_roi_pct   NUMBER(9,2),   -- Marketing KPI
    inventory_turnover  NUMBER(7,2),   -- Operational KPI
    retention_rate_pct  NUMBER(7,2),   -- Customer Experience KPI
    calculated_at       TIMESTAMP DEFAULT SYSTIMESTAMP,
    CONSTRAINT uq_kpi_year UNIQUE (kpi_year)
);


-- SECTION 3 : SANITY CHECK  (run AFTER importing the 5 CSVs)
 SELECT 'products'  t, COUNT(*) n FROM products  UNION ALL
 SELECT 'customers',   COUNT(*)   FROM customers UNION ALL
 SELECT 'marketing',   COUNT(*)   FROM marketing UNION ALL
 SELECT 'inventory',   COUNT(*)   FROM inventory UNION ALL
 SELECT 'sales',       COUNT(*)   FROM sales;


-- SECTION 4 : KPI QUERIES (one per KPI in the spec)

-- 4.1 Total Revenue & Revenue Growth Rate (Financial)
CREATE VIEW v_kpi_revenue AS
WITH yearly AS (
    SELECT CAST(strftime('%Y', order_date) AS INTEGER) AS yr,
           SUM(revenue) AS total_revenue
    FROM sales
    GROUP BY yr
)
SELECT yr,
       total_revenue,
       ROUND(
         (total_revenue - LAG(total_revenue) OVER (ORDER BY yr)) * 100.0
         / NULLIF(LAG(total_revenue) OVER (ORDER BY yr), 0), 2
       ) AS revenue_growth_pct
FROM yearly;

-- 4.2 Profit & Profit Margin (Financial)
CREATE VIEW v_kpi_profit AS
WITH yr_rev AS (
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
    SELECT CAST(strftime('%Y', campaign_date) AS INTEGER) AS yr, SUM(cost) AS mkt_cost
    FROM marketing GROUP BY yr
)
SELECT r.yr,
       r.rev - c.cogs - COALESCE(m.mkt_cost, 0)                        AS profit,
       ROUND((r.rev - c.cogs) * 100.0 / NULLIF(r.rev, 0), 2)           AS profit_margin_pct
FROM yr_rev r
JOIN yr_cogs c ON c.yr = r.yr
LEFT JOIN yr_mkt m ON m.yr = r.yr;

-- 4.3 Inventory Turnover 
CREATE VIEW v_kpi_inventory_turnover AS
WITH cogs AS (
    SELECT CAST(strftime('%Y', s.order_date) AS INTEGER) AS yr,
           SUM(p.cost * s.quantity) AS cogs
    FROM sales s JOIN products p ON p.product_id = s.product_id
    GROUP BY yr
),
avg_inv AS (
    SELECT CAST(strftime('%Y', i.snapshot_date) AS INTEGER) AS yr,
           AVG(i.stock_level * p.cost) AS avg_inv_value
    FROM inventory i JOIN products p ON p.product_id = i.product_id
    GROUP BY yr
)
SELECT c.yr,
       c.cogs,
       a.avg_inv_value,
       ROUND(c.cogs / NULLIF(a.avg_inv_value, 0), 2) AS inventory_turnover
FROM cogs c JOIN avg_inv a ON a.yr = c.yr;

-- 4.4 Customer Retention Rate (Customer Experience)
CREATE VIEW v_kpi_retention AS
WITH cust_year AS (
    SELECT customer_id, CAST(strftime('%Y', order_date) AS INTEGER) AS yr
    FROM sales GROUP BY customer_id, yr
)
SELECT cy.yr,
       (SELECT COUNT(DISTINCT customer_id) FROM cust_year p2 WHERE p2.yr = cy.yr - 1) AS starting_customers,
       COUNT(DISTINCT CASE WHEN EXISTS (
             SELECT 1 FROM cust_year p WHERE p.customer_id = cy.customer_id AND p.yr = cy.yr - 1
           ) THEN cy.customer_id END) AS repeat_customers,
       ROUND(
         COUNT(DISTINCT CASE WHEN EXISTS (
               SELECT 1 FROM cust_year p WHERE p.customer_id = cy.customer_id AND p.yr = cy.yr - 1
             ) THEN cy.customer_id END) * 100.0
         / NULLIF((SELECT COUNT(DISTINCT customer_id) FROM cust_year p2 WHERE p2.yr = cy.yr - 1), 0), 2
       ) AS retention_rate_pct
FROM cust_year cy
GROUP BY cy.yr;

-- 4.5 Marketing spend / conversions (Marketing)
CREATE VIEW v_kpi_marketing AS
SELECT CAST(strftime('%Y', campaign_date) AS INTEGER) AS yr,
       channel,
       SUM(cost)                                    AS total_cost,
       SUM(conversions)                             AS total_conversions,
       ROUND(SUM(cost) / NULLIF(SUM(conversions), 0), 2) AS cost_per_conversion
FROM marketing
GROUP BY yr, channel;

-- 4.6 Low-stock alert 
CREATE VIEW v_low_stock_alert AS
SELECT i.product_id, p.category, i.warehouse, i.stock_level
FROM inventory i
JOIN products p ON p.product_id = i.product_id
WHERE i.snapshot_date = (SELECT MAX(snapshot_date) FROM inventory)
  AND i.stock_level < 50;



-- SECTION 5 : KPI REFRESH PROCEDURE
-- Populates KPI_SNAPSHOT in one call. Re-run whenever new
-- CSV data has been loaded (dynamic refresh).
CREATE OR REPLACE PROCEDURE refresh_kpi_snapshot IS
BEGIN
    DELETE FROM kpi_snapshot;

    INSERT INTO kpi_snapshot (
        kpi_year, revenue_total, revenue_growth_pct,
        conversion_rate_pct, marketing_roi_pct,
        inventory_turnover, retention_rate_pct)
    WITH
    yr_rev AS (
        SELECT EXTRACT(YEAR FROM order_date) yr, SUM(revenue) rev
        FROM sales GROUP BY EXTRACT(YEAR FROM order_date)
    ),
    rev_growth AS (
        SELECT yr, rev,
               ROUND((rev - LAG(rev) OVER (ORDER BY yr))
                     / NULLIF(LAG(rev) OVER (ORDER BY yr),0) * 100, 2) growth
        FROM yr_rev
    ),
    conv AS (
        SELECT EXTRACT(YEAR FROM campaign_date) yr,
               ROUND(SUM(conversions)/NULLIF(COUNT(*),0),2) conv_rate
        FROM marketing GROUP BY EXTRACT(YEAR FROM campaign_date)
    ),
    roi AS (
        SELECT EXTRACT(YEAR FROM m.campaign_date) yr,
               ROUND((SUM(s.revenue) - SUM(m.cost))
                     / NULLIF(SUM(m.cost),0) * 100, 2) roi_pct
        FROM marketing m
        LEFT JOIN sales s ON TRUNC(s.order_date) = TRUNC(m.campaign_date)
        GROUP BY EXTRACT(YEAR FROM m.campaign_date)
    ),
    turn AS (
        SELECT c.yr,
               ROUND(c.cogs / NULLIF(a.avg_inv_value,0), 2) turnover
        FROM (SELECT EXTRACT(YEAR FROM s.order_date) yr,
                     SUM(p.cost*s.quantity) cogs
              FROM sales s JOIN products p ON p.product_id=s.product_id
              GROUP BY EXTRACT(YEAR FROM s.order_date)) c
        JOIN (SELECT EXTRACT(YEAR FROM snapshot_date) yr,
                     AVG(stock_level*p.cost) avg_inv_value
              FROM inventory i JOIN products p ON p.product_id=i.product_id
              GROUP BY EXTRACT(YEAR FROM snapshot_date)) a ON a.yr=c.yr
    ),
    cust_year AS (
        SELECT customer_id, EXTRACT(YEAR FROM order_date) yr
        FROM sales GROUP BY customer_id, EXTRACT(YEAR FROM order_date)
    ),
    ret AS (
        SELECT cy.yr,
               ROUND(
                  (COUNT(DISTINCT CASE WHEN EXISTS
                       (SELECT 1 FROM cust_year p
                        WHERE p.customer_id=cy.customer_id AND p.yr=cy.yr-1)
                     THEN cy.customer_id END)
                   - (COUNT(DISTINCT cy.customer_id)
                      - COUNT(DISTINCT CASE WHEN EXISTS
                           (SELECT 1 FROM cust_year p
                            WHERE p.customer_id=cy.customer_id AND p.yr=cy.yr-1)
                         THEN cy.customer_id END)))
                  / NULLIF((SELECT COUNT(DISTINCT customer_id)
                            FROM cust_year p2 WHERE p2.yr=cy.yr-1),0) * 100, 2) ret_pct
        FROM cust_year cy
        GROUP BY cy.yr
    )
    SELECT rg.yr, rg.rev, rg.growth,
           conv.conv_rate, roi.roi_pct,
           turn.turnover, ret.ret_pct
    FROM rev_growth rg
    LEFT JOIN conv ON conv.yr = rg.yr
    LEFT JOIN roi  ON roi.yr  = rg.yr
    LEFT JOIN turn ON turn.yr = rg.yr
    LEFT JOIN ret  ON ret.yr  = rg.yr;

    COMMIT;
END;
/

-- Execute it:
BEGIN refresh_kpi_snapshot; END;
/

-- Inspect results:
SELECT * FROM kpi_snapshot ORDER BY kpi_year;
