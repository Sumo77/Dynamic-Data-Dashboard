const express = require('express');
const Database = require('better-sqlite3');
const path = require('path');
const fs = require('fs');

const app = express();
app.use(express.json());

// Points directly to retail_database.db in the exact same directory as server.js
const dbPath = path.join(
  __dirname,
  '../../../../../retail_database.db'
);

const db = new Database(dbPath);


// 0. Dynamic schema introspection: table names, column names, SQL storage
//    types, real foreign key relationships, and one sample value per column.
app.get('/api/schema/introspect', (req, res) => {
  try {
    const tableRows = db.prepare(
      "SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%'"
    ).all();

    const tables = {};

    for (const { name: tableName } of tableRows) {
      const columnInfo = db
        .prepare(`PRAGMA table_info(${tableName})`)
        .all();

      const foreignKeys = db
        .prepare(`PRAGMA foreign_key_list(${tableName})`)
        .all();

      const columns = columnInfo.map(col => {
        let sample = null;

        try {
          const row = db.prepare(
            `SELECT "${col.name}" AS v
             FROM "${tableName}"
             WHERE "${col.name}" IS NOT NULL
             LIMIT 1`
          ).get();

          sample = row ? row.v : null;

        } catch (sampleErr) {
          sample = null;
        }

        return {
          name: col.name,
          sqlType: col.type,
          nullable: col.notnull === 0,
          primaryKey: col.pk > 0,
          sample
        };
      });

      tables[tableName] = {
        columns,

        foreignKeys: foreignKeys.map(fk => ({
          column: fk.from,
          refTable: fk.table,
          refColumn: fk.to
        }))
      };
    }

    res.json({
      success: true,
      tables
    });

  } catch (err) {
    res.status(500).json({
      success: false,
      error: err.message
    });
  }
});


// Small helpers used to whitelist table/column names.
function tableExists(table) {
  return !!db.prepare(
    "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?"
  ).get(table);
}

function columnNames(table) {
  return db.prepare(
    `PRAGMA table_info("${table}")`
  ).all().map(c => c.name);
}


// 0b. Run a measure-grouped-by-dimension/date query.
app.get('/api/query/compare', (req, res) => {
  try {
    const {
      table,
      measureColumn,
      groupColumn,
      aggFn
    } = req.query;

    const ALLOWED_AGG = {
      SUM: 'SUM',
      AVG: 'AVG',
      COUNT: 'COUNT',
      MIN: 'MIN',
      MAX: 'MAX'
    };

    const fn =
      ALLOWED_AGG[
        String(
          aggFn || 'SUM'
        ).toUpperCase()
      ];

    if (!fn) {
      return res.status(400).json({
        success: false,
        error: `Unsupported aggFn: ${aggFn}`
      });
    }

    if (!tableExists(table)) {
      return res.status(400).json({
        success: false,
        error: `Unknown table: ${table}`
      });
    }

    const cols = columnNames(table);

    if (!cols.includes(measureColumn)) {
      return res.status(400).json({
        success: false,
        error: `Unknown column: ${measureColumn}`
      });
    }

    if (!cols.includes(groupColumn)) {
      return res.status(400).json({
        success: false,
        error: `Unknown column: ${groupColumn}`
      });
    }

    const query = `
      SELECT
        "${groupColumn}" AS label,
        ${fn}("${measureColumn}") AS value
      FROM "${table}"
      GROUP BY "${groupColumn}"
      ORDER BY "${groupColumn}" ASC
    `;

    const rows =
      db.prepare(query).all();

    res.json({
      success: true,
      data: rows
    });

  } catch (err) {
    res.status(500).json({
      success: false,
      error: err.message
    });
  }
});


// 1. Fetch pre-cached KPI Snapshot data
app.get('/api/kpis/summary', (req, res) => {
  try {
    const {
      year_from,
      year_to
    } = req.query;

    let query =
      'SELECT * FROM kpi_snapshot';

    const conditions = [];
    const params = [];

    if (year_from) {
      conditions.push(
        'kpi_year >= ?'
      );

      params.push(
        year_from
      );
    }

    if (year_to) {
      conditions.push(
        'kpi_year <= ?'
      );

      params.push(
        year_to
      );
    }

    if (conditions.length > 0) {
      query +=
        ' WHERE '
        + conditions.join(' AND ');
    }

    query +=
      ' ORDER BY kpi_year ASC';

    const rows =
      db.prepare(query).all(
        ...params
      );

    res.json({
      success: true,
      data: rows
    });

  } catch (err) {
    res.status(500).json({
      success: false,
      error: err.message
    });
  }
});


// ============================================================
// 1b. FILTERED SOLO KPI ENDPOINT
// ============================================================
//
// This is ADDED code.
// Cooper's existing routes above are unchanged.
//
// Calculates:
// - Total Revenue
// - Revenue Growth Rate
//
// Uses:
// - sales table only
//
// Supports global dashboard filters:
// - year
// - scope
// - period
// - region
//
app.get('/api/kpis/filtered', (req, res) => {
  try {
    const year =
      parseInt(
        req.query.year
      );

    const scope =
      String(
        req.query.scope || 'yearly'
      ).toLowerCase();

    const period =
      String(
        req.query.period || 'Full Year'
      );

    const region =
      String(
        req.query.region || 'All Regions'
      );

    if (!year) {
      return res.status(400).json({
        success: false,
        error: 'year is required'
      });
    }

    /*
     * Find the selected dashboard date range.
     */
    const currentRange =
      getFilteredDateRange(
        year,
        scope,
        period
      );

    /*
     * Find the equivalent previous period.
     */
    const previousRange =
      getPreviousFilteredDateRange(
        currentRange.start,
        scope
      );

    /*
     * Current period revenue.
     */
    const currentRevenue =
      getFilteredRevenue(
        currentRange.start,
        currentRange.end,
        region
      );

    /*
     * Previous period revenue.
     */
    const previousRevenue =
      getFilteredRevenue(
        previousRange.start,
        previousRange.end,
        region
      );

    /*
     * Revenue Growth Rate
     */
    let revenueGrowthPct = 0;

    if (previousRevenue !== 0) {
      revenueGrowthPct =
        (
          (
            currentRevenue
            - previousRevenue
          )
          / previousRevenue
        )
        * 100;
    }

    res.json({
      success: true,

      filters: {
        year,
        scope,
        period,
        region
      },

      current_period: {
        start:
          currentRange.start,

        end:
          currentRange.end
      },

      previous_period: {
        start:
          previousRange.start,

        end:
          previousRange.end
      },

      total_revenue:
        Number(
          currentRevenue.toFixed(2)
        ),

      revenue_growth_pct:
        Number(
          revenueGrowthPct.toFixed(2)
        ),

      /*
       * Can be used by the KPI delta badge.
       */
      revenue_delta_pct:
        Number(
          revenueGrowthPct.toFixed(2)
        )
    });

  } catch (err) {
    console.error(
      'Filtered KPI error:',
      err
    );

    res.status(500).json({
      success: false,
      error: err.message
    });
  }
});


// ============================================================
// FILTERED KPI HELPER METHODS
// ============================================================

/*
 * Gets total revenue from the REAL sales table
 * for the selected date range and region.
 */
function getFilteredRevenue(
  startDate,
  endDate,
  region
) {

  let query = `
    SELECT
      COALESCE(
        SUM(revenue),
        0
      ) AS total_revenue

    FROM sales

    WHERE order_date >= ?
    AND order_date <= ?
  `;

  const params = [
    startDate,
    endDate
  ];

  /*
   * Add region filter only if the user
   * selected a specific region.
   */
  if (
    region
    && region !== 'All Regions'
  ) {

    query += `
      AND region = ?
    `;

    params.push(
      region
    );
  }

  const row =
    db.prepare(query).get(
      ...params
    );

  return row
    ? Number(
        row.total_revenue
      )
    : 0;
}


/*
 * Converts the global filter into
 * start and end dates.
 */
function getFilteredDateRange(
  year,
  scope,
  period
) {

  /*
   * YEARLY
   */
  if (scope === 'yearly') {

    return {
      start:
        `${year}-01-01`,

      end:
        `${year}-12-31`
    };
  }


  /*
   * QUARTERLY
   */
  if (scope === 'quarterly') {

    const quarters = {

      Q1: [
        '01-01',
        '03-31'
      ],

      Q2: [
        '04-01',
        '06-30'
      ],

      Q3: [
        '07-01',
        '09-30'
      ],

      Q4: [
        '10-01',
        '12-31'
      ]
    };

    const range =
      quarters[period];

    if (!range) {
      throw new Error(
        'Invalid quarter: '
        + period
      );
    }

    return {
      start:
        `${year}-${range[0]}`,

      end:
        `${year}-${range[1]}`
    };
  }


  /*
   * MONTHLY
   */
  if (scope === 'monthly') {

    const months = {
      January: 1,
      February: 2,
      March: 3,
      April: 4,
      May: 5,
      June: 6,
      July: 7,
      August: 8,
      September: 9,
      October: 10,
      November: 11,
      December: 12
    };

    const month =
      months[period];

    if (!month) {
      throw new Error(
        'Invalid month: '
        + period
      );
    }

    const start =
      new Date(
        year,
        month - 1,
        1
      );

    const end =
      new Date(
        year,
        month,
        0
      );

    return {
      start:
        formatFilteredDate(
          start
        ),

      end:
        formatFilteredDate(
          end
        )
    };
  }

  throw new Error(
    'Invalid scope: '
    + scope
  );
}


/*
 * Finds the previous equivalent period.
 *
 * Monthly:
 * January -> previous December
 *
 * Quarterly:
 * Q2 -> Q1
 *
 * Yearly:
 * 2024 -> 2023
 */
function getPreviousFilteredDateRange(
  currentStart,
  scope
) {

  const start =
    new Date(
      currentStart
      + 'T00:00:00'
    );


  /*
   * MONTHLY
   */
  if (scope === 'monthly') {

    const previousStart =
      new Date(
        start.getFullYear(),
        start.getMonth() - 1,
        1
      );

    const previousEnd =
      new Date(
        start.getFullYear(),
        start.getMonth(),
        0
      );

    return {
      start:
        formatFilteredDate(
          previousStart
        ),

      end:
        formatFilteredDate(
          previousEnd
        )
    };
  }


  /*
   * QUARTERLY
   */
  if (scope === 'quarterly') {

    const previousStart =
      new Date(
        start.getFullYear(),
        start.getMonth() - 3,
        1
      );

    const previousEnd =
      new Date(
        start.getFullYear(),
        start.getMonth(),
        0
      );

    return {
      start:
        formatFilteredDate(
          previousStart
        ),

      end:
        formatFilteredDate(
          previousEnd
        )
    };
  }


  /*
   * YEARLY
   */
  const previousYear =
    start.getFullYear() - 1;

  return {
    start:
      `${previousYear}-01-01`,

    end:
      `${previousYear}-12-31`
  };
}


/*
 * Converts JavaScript Date into:
 *
 * YYYY-MM-DD
 */
function formatFilteredDate(
  date
) {

  const year =
    date.getFullYear();

  const month =
    String(
      date.getMonth() + 1
    ).padStart(
      2,
      '0'
    );

  const day =
    String(
      date.getDate()
    ).padStart(
      2,
      '0'
    );

  return (
    `${year}-${month}-${day}`
  );
}


// 2. Fetch specific live View (e.g., Marketing details)
app.get('/api/kpis/marketing', (req, res) => {
  try {
    const rows =
      db.prepare(
        'SELECT * FROM v_kpi_marketing ORDER BY yr, channel'
      ).all();

    res.json({
      success: true,
      data: rows
    });

  } catch (err) {
    res.status(500).json({
      success: false,
      error: err.message
    });
  }
});


// 3. Fetch Low Stock Alerts
app.get('/api/alerts/low-stock', (req, res) => {
  try {
    const rows =
      db.prepare(
        'SELECT * FROM v_low_stock_alert ORDER BY stock_level ASC'
      ).all();

    res.json({
      success: true,
      data: rows
    });

  } catch (err) {
    res.status(500).json({
      success: false,
      error: err.message
    });
  }
});


// 4. Trigger KPI Snapshot Recalculation
app.post('/api/kpis/refresh', (req, res) => {
  try {
    const refreshScript =
      fs.readFileSync(
        path.join(
          __dirname,
          'refresh_kpi_snapshot_sqlite.sql'
        ),
        'utf8'
      );

    db.exec(
      refreshScript
    );

    res.json({
      success: true,
      message:
        'KPI cache successfully updated.'
    });

  } catch (err) {
    res.status(500).json({
      success: false,
      error: err.message
    });
  }
});


app.listen(3000, () => {
  console.log(
    'Backend listening on http://localhost:3000'
  );
});