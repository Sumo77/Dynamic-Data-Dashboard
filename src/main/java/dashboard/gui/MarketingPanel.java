package dashboard.gui;

import dashboard.database.AnalyticsApi;

public class MarketingPanel extends BaseAnalyticsPage {

    public MarketingPanel() {
        super("Marketing", "Channel spend plus whole-business month-level sales comparison");
        refreshData();
    }

    @Override
    protected void refreshData() {
        startRefresh();
        try {
            var params = filter.toParams();

            charts.add(AnalyticsCharts.pie(
                "Spend by Channel (marketing)",
                AnalyticsApi.points("api/marketing/spend-channel", params)
            ));

            charts.add(AnalyticsCharts.line(
                "Cost per Conversion Over Time (marketing)",
                "Month", "Cost per Conversion ($)",
                AnalyticsApi.points("api/marketing/cost-per-conversion-trend", params),
                "Cost per Conversion",
                null
            ));

            charts.add(AnalyticsCharts.multiLine(
                "Marketing Spend vs Revenue Over Time (period-level only)", "Month", "Value ($)",
                AnalyticsApi.seriesPoints("api/marketing/spend-revenue", params),
                month -> DrilldownDialog.showSales(this, month, null, filter.region())
            ));
        } catch (Exception ex) {
            showError(ex);
        }
        finishRefresh();
    }
}
