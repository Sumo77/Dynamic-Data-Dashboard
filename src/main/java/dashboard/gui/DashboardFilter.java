package dashboard.gui;

import java.util.LinkedHashMap;
import java.util.Map;

/** Shared filter object used by Overview and analytics pages. */
public record DashboardFilter(int year, String scope, String period, String region) {

    public Map<String, String> toParams() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("year", String.valueOf(year));
        params.put("scope", scope == null ? "Yearly" : scope);
        params.put("period", period == null ? "Full Year" : period);
        params.put("region", region == null ? "All Regions" : region);
        return params;
    }

    public static DashboardFilter defaults() {
        return new DashboardFilter(2023, "Yearly", "Full Year", "All Regions");
    }
}
