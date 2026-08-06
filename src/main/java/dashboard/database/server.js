const express = require('express');
const Database = require('better-sqlite3');
const path = require('path');
const fs = require('fs');

const app = express();
app.use(express.json());

// Points directly to retail_dashboard.db in the exact same directory as server.js
const dbPath = path.join(__dirname, 'retail_dashboard.db');
const db = new Database(dbPath);

// 1. Fetch pre-cached KPI Snapshot data
app.get('/api/kpis/summary', (req, res) => {
  try {
    const rows = db.prepare('SELECT * FROM kpi_snapshot ORDER BY kpi_year ASC').all();
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