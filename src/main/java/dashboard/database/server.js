const express = require('express');
const Database = require('better-sqlite3');
const path = require('path');
const fs = require('fs');

const app = express();
app.use(express.json());

// Points directly to retail_dashboard.db in the exact same directory as server.js
const dbPath = path.join(__dirname, '../../../../../retail_database.db');
const db = new Database(dbPath);

// 0. Dynamic schema introspection: table names, column names, SQL storage
//    types, real foreign key relationships, and one sample value per column
//    (used client-side to disambiguate TEXT/INTEGER columns that carry
//    different semantic meaning - e.g. a date string vs. a category label).
app.get('/api/schema/introspect', (req, res) => {
  try {
    const tableRows = db.prepare(
      "SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%'"
    ).all();

    const tables = {};

    for (const { name: tableName } of tableRows) {
      const columnInfo = db.prepare(`PRAGMA table_info(${tableName})`).all();
      const foreignKeys = db.prepare(`PRAGMA foreign_key_list(${tableName})`).all();

      const columns = columnInfo.map(col => {
        let sample = null;
        try {
          // Grab the first non-null value in this column to help the client
          // disambiguate storage type from semantic meaning (e.g. TEXT that
          // is actually a date vs. TEXT that is actually a label).
          const row = db.prepare(
            `SELECT "${col.name}" AS v FROM "${tableName}" WHERE "${col.name}" IS NOT NULL LIMIT 1`
          ).get();
          sample = row ? row.v : null;
        } catch (sampleErr) {
          sample = null;
        }

        return {
          name: col.name,
          sqlType: col.type,        // INTEGER / REAL / TEXT (as SQLite reports it)
          nullable: col.notnull === 0,
          primaryKey: col.pk > 0,
          sample                     // first observed value, or null if empty/unreadable
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

    res.json({ success: true, tables });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// 1. Fetch pre-cached KPI Snapshot data
app.get('/api/kpis/summary', (req, res) => {
  try {
    const { year_from, year_to } = req.query;

    let query = 'SELECT * FROM kpi_snapshot';
    const conditions = [];
    const params = [];

    if (year_from) {
      conditions.push('kpi_year >= ?');
      params.push(year_from);
    }
    if (year_to) {
      conditions.push('kpi_year <= ?');
      params.push(year_to);
    }
    if (conditions.length > 0) {
      query += ' WHERE ' + conditions.join(' AND ');
    }
    query += ' ORDER BY kpi_year ASC';

    const rows = db.prepare(query).all(...params);
    res.json({ success: true, data: rows });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// 2. Fetch specific live View (e.g., Marketing details)
app.get('/api/kpis/marketing', (req, res) => {
  try {
    const rows = db.prepare('SELECT * FROM v_kpi_marketing ORDER BY yr, channel').all();
    res.json({ success: true, data: rows });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// 3. Fetch Low Stock Alerts
app.get('/api/alerts/low-stock', (req, res) => {
  try {
    const rows = db.prepare('SELECT * FROM v_low_stock_alert ORDER BY stock_level ASC').all();
    res.json({ success: true, data: rows });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// 4. Trigger KPI Snapshot Recalculation
app.post('/api/kpis/refresh', (req, res) => {
  try {
    const refreshScript = fs.readFileSync(
      path.join(__dirname, 'refresh_kpi_snapshot_sqlite.sql'),
      'utf8'
    );
    db.exec(refreshScript);
    res.json({ success: true, message: 'KPI cache successfully updated.' });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

app.listen(3000, () => {
  console.log('Backend listening on http://localhost:3000');
});