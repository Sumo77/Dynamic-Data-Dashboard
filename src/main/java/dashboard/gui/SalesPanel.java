package dashboard.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Sales analytics page.
 *
 * The old Sales-only filter bar has intentionally been removed. Sales now
 * implements FilterableDashboardPage, so the large filter in DashboardFrame
 * controls these four charts in the same way as every other analytics page.
 */
public class SalesPanel extends JPanel implements FilterableDashboardPage {

    private static final Color BACKGROUND = new Color(245, 247, 250);
    private static final Color PRIMARY = new Color(31, 41, 55);
    private static final Color SECONDARY = new Color(100, 116, 139);

    private final RevenueOverTimeChart revenueOverTimeChart = new RevenueOverTimeChart();
    private final RevenueByRegionChart revenueByRegionChart = new RevenueByRegionChart();
    private final ProfitMarginOverTimeChart profitMarginOverTimeChart = new ProfitMarginOverTimeChart();
    private final QuantityRevenueScatterChart quantityRevenueScatterChart = new QuantityRevenueScatterChart();

    private DashboardFilter filter = DashboardFilter.defaults();

    public SalesPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(20, 25, 25, 25));
        createLayout();
    }

    private void createLayout() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Sales");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(PRIMARY);

        JLabel subtitle = new JLabel("Sales performance, regional revenue and cross-table profitability");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(SECONDARY);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        add(header, BorderLayout.NORTH);

        // Keep the existing four-chart Sales structure, stacked vertically.
        JPanel charts = new JPanel();
        charts.setOpaque(false);
        charts.setLayout(new BoxLayout(charts, BoxLayout.Y_AXIS));

        addChart(charts, revenueOverTimeChart);
        addChart(charts, revenueByRegionChart);
        addChart(charts, profitMarginOverTimeChart);
        addChart(charts, quantityRevenueScatterChart);

        JScrollPane scroll = new JScrollPane(charts);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        // Allow sideways movement on smaller windows instead of stretching/cropping charts.
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.getHorizontalScrollBar().setUnitIncrement(20);
        add(scroll, BorderLayout.CENTER);
    }

    private void addChart(JPanel parent, JComponent chart) {
        // Fixed chart size keeps the Sales graphs compact and consistent.
        chart.setPreferredSize(new Dimension(850, 320));
        chart.setMinimumSize(new Dimension(850, 320));
        chart.setMaximumSize(new Dimension(850, 320));
        chart.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(chart);
        parent.add(Box.createVerticalStrut(15));
    }

    /**
     * Receives the same filter used by Overview/Inventory/Products/Marketing/
     * Customers and forwards it to every Sales chart.
     */
    @Override
    public void applyFilter(DashboardFilter filter) {
        this.filter = filter == null ? DashboardFilter.defaults() : filter;

        String selectedMonth = "Weekly".equals(this.filter.scope()) ? this.filter.month() : null;

        // Existing Sales chart methods are reused so the page structure stays unchanged.
        revenueOverTimeChart.applyFilters(this.filter.year(), this.filter.scope(), selectedMonth, this.filter.period());
        revenueByRegionChart.applyFilters(this.filter.year(), this.filter.scope(), selectedMonth, this.filter.period());
        profitMarginOverTimeChart.applyFilters(this.filter.year(), this.filter.scope(), selectedMonth, this.filter.period());
        quantityRevenueScatterChart.applyFilters(this.filter.year(), this.filter.scope(), selectedMonth, this.filter.period());
    }
}
