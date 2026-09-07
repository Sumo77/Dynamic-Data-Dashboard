package dashboard.gui;

import dashboard.database.AnalyticsApi;

public class InventoryPanel extends BaseAnalyticsPage {

    public InventoryPanel() {
        super("Inventory", "Solo inventory analysis and cross-table stock performance");
        refreshData();
    }

    @Override
    protected void refreshData() {
        startRefresh();
        try {
            var params = filter.toParams();

            charts.add(AnalyticsCharts.bar(
                    "Stock Levels by Warehouse (inventory)", "Warehouse", "Units in Stock",
                    AnalyticsApi.points("api/inventory/stock-warehouse", params),
                    "Stock", false, null
            ));

            charts.add(AnalyticsCharts.line(
                    "Stock Level Over Time (inventory)", "Month", "Average Stock Level",
                    AnalyticsApi.points("api/inventory/stock-trend", params),
                    "Average Stock", null
            ));

            charts.add(AnalyticsCharts.line(
                    "Inventory Turnover Trend (inventory + products + sales)", "Month", "Turnover",
                    AnalyticsApi.points("api/inventory/turnover", params),
                    "Inventory Turnover",
                    month -> DrilldownDialog.showSales(this, month, null, filter.region())
            ));

            charts.add(AnalyticsCharts.groupedBar(
                    "Stock Cover by Category (inventory + sales + products)", "Category", "Units",
                    AnalyticsApi.seriesPoints("api/inventory/stock-cover", params),
                    category -> DrilldownDialog.showSales(this, null, category, filter.region())
            ));
        } catch (Exception ex) {
            showError(ex);
        }
        finishRefresh();
    }
}
