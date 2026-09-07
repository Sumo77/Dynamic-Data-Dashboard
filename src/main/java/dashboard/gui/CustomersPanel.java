package dashboard.gui;

import dashboard.database.AnalyticsApi;

public class CustomersPanel extends BaseAnalyticsPage {

    public CustomersPanel() {
        super("Customers", "Customer acquisition, retention and revenue segments");
        refreshData();
    }

    @Override
    protected void refreshData() {
        startRefresh();
        try {
            var params = filter.toParams();

            charts.add(AnalyticsCharts.bar(
                    "New Customers by Signup Month (customers)", "Month", "Customers",
                    AnalyticsApi.points("api/customers/new-signups", params),
                    "New Customers", false, null
            ));

            charts.add(AnalyticsCharts.bar(
                    "Customer Breakdown by Country (customers)", "Country", "Customers",
                    AnalyticsApi.points("api/customers/country-breakdown", params),
                    "Customers", false, null
            ));

            charts.add(AnalyticsCharts.line(
                    "Customer Retention % Trend (customers + sales)", "Month", "Retention %",
                    AnalyticsApi.points("api/customers/retention", params),
                    "Retention %",
                    month -> DrilldownDialog.showSales(this, month, null, filter.region())
            ));

            charts.add(AnalyticsCharts.bar(
                    "Revenue by Customer Segment - Country (sales + customers)", "Country", "Revenue ($)",
                    AnalyticsApi.points("api/customers/revenue-segment", params),
                    "Revenue", false, null
            ));
        } catch (Exception ex) {
            showError(ex);
        }
        finishRefresh();
    }
}
