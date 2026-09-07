package dashboard.gui;

import dashboard.database.AnalyticsApi;
import dashboard.database.SchemaIntrospector.TableMeta;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;
import java.util.function.Consumer;

public class OverviewPanel extends JPanel implements FilterableDashboardPage {

    private static final Color BACKGROUND = new Color(245, 247, 250);
    private static final Color ACTIVE = new Color(0, 212, 255);
    private static final Color PRIMARY = new Color(31, 41, 55);
    private static final Color BORDER = new Color(226, 232, 240);

    private final KpiPanel kpiPanel = new KpiPanel();
    private final RevenueChartPanel revenueChartPanel = new RevenueChartPanel();
    private final Consumer<DashboardFilter> filterListener;

    private JComboBox<Integer> yearFilter;
    private JComboBox<String> scopeFilter;
    private JComboBox<String> periodFilter;
    private JComboBox<String> regionFilter;

    private DashboardFilter currentFilter = DashboardFilter.defaults();

    public OverviewPanel(Map<String, TableMeta> schema, Consumer<DashboardFilter> filterListener) {
        this.filterListener = filterListener;
        setLayout(new BorderLayout(0, 15));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(5, 25, 25, 25));
        createLayout();
        applyCurrentFilter();
    }

    private void createLayout() {
        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setBackground(BACKGROUND);

        JLabel title = new JLabel("Overview");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(PRIMARY);

        top.add(title, BorderLayout.NORTH);
        top.add(createFilterBar(), BorderLayout.CENTER);
        top.add(kpiPanel, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(BACKGROUND);
        body.add(revenueChartPanel, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    private JPanel createFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(3, 8, 3, 8)
        ));

        yearFilter = new JComboBox<>(new Integer[]{2023, 2024});
        scopeFilter = new JComboBox<>(new String[]{"Yearly", "Quarterly", "Monthly"});
        periodFilter = new JComboBox<>();
        regionFilter = new JComboBox<>(new String[]{
                "All Regions", "Auckland", "Wellington", "Christchurch", "Sydney", "Melbourne"
        });

        yearFilter.setSelectedItem(2023);
        scopeFilter.setSelectedItem("Yearly");
        updatePeriods();

        scopeFilter.addActionListener(e -> updatePeriods());

        JButton apply = new JButton("Apply Filters");
        apply.setBackground(ACTIVE);
        apply.setFocusPainted(false);
        apply.addActionListener(e -> applyCurrentFilter());

        JButton reset = new JButton("Reset");
        reset.addActionListener(e -> {
            yearFilter.setSelectedItem(2023);
            scopeFilter.setSelectedItem("Yearly");
            regionFilter.setSelectedItem("All Regions");
            updatePeriods();
            applyCurrentFilter();
        });

        bar.add(new JLabel("Year:"));
        bar.add(yearFilter);
        bar.add(new JLabel("Scope:"));
        bar.add(scopeFilter);
        bar.add(new JLabel("Period:"));
        bar.add(periodFilter);
        bar.add(new JLabel("Region:"));
        bar.add(regionFilter);
        bar.add(apply);
        bar.add(reset);
        return bar;
    }

    private void updatePeriods() {
        String scope = String.valueOf(scopeFilter.getSelectedItem());
        periodFilter.removeAllItems();
        if ("Quarterly".equals(scope)) {
            for (String q : new String[]{"Q1", "Q2", "Q3", "Q4"}) periodFilter.addItem(q);
        } else if ("Monthly".equals(scope)) {
            for (String month : new String[]{"January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"}) {
                periodFilter.addItem(month);
            }
        } else {
            periodFilter.addItem("Full Year");
        }
    }

    private void applyCurrentFilter() {
        currentFilter = new DashboardFilter(
                (Integer) yearFilter.getSelectedItem(),
                String.valueOf(scopeFilter.getSelectedItem()),
                String.valueOf(periodFilter.getSelectedItem()),
                String.valueOf(regionFilter.getSelectedItem())
        );
        applyFilter(currentFilter);
        if (filterListener != null) filterListener.accept(currentFilter);
    }

    @Override
    public void applyFilter(DashboardFilter filter) {
        currentFilter = filter;
        try {
            kpiPanel.update(AnalyticsApi.overview(filter.toParams()));
        } catch (Exception ex) {
            ex.printStackTrace();
            kpiPanel.showError();
        }

        revenueChartPanel.applyFilters(
                filter.year(), filter.scope(), filter.period(), filter.region()
        );
    }
}
