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

            charts.add(AnalyticsCharts.bar(
                    "Cost per Conversion by Channel (marketing)", "Channel", "Cost / Conversion ($)",
                    AnalyticsApi.points("api/marketing/cost-conversion", params),
                    "Cost per Conversion", false, null
            ));

            charts.add(AnalyticsCharts.line(
                    "WHOLE-BUSINESS Marketing ROI % Over Time (marketing + sales by month)",
                    "Month", "ROI %",
                    AnalyticsApi.points("api/marketing/roi-trend", params),
                    "Whole-business ROI %",
                    month -> DrilldownDialog.showSales(this, month, null, filter.region())
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
