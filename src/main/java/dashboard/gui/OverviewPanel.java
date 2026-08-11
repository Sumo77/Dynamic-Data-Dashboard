package dashboard.gui;

import dashboard.database.ApiClient;
import dashboard.database.SchemaIntrospector.TableMeta;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OverviewPanel extends JPanel {

    private static final Color BACKGROUND_COLOR =
            new Color(245, 247, 250);

    private static final Color ACTIVE_COLOR =
            new Color(0, 212, 255);

    private static final Color SIDEBAR_COLOUR =
            new Color(17, 24, 39);

    private static final Color PRIMARY_TEXT =
            new Color(31, 41, 55);

    private static final Color SECONDARY_TEXT =
            new Color(100, 116, 139);

    private static final Color BORDER_COLOR =
            new Color(226, 232, 240);

    private KpiPanel kpiPanel;
    private RevenueChartPanel revenueChartPanel;

    private final Map<String, TableMeta> schema;

    /*
     * Global dashboard filters.
     */
    private JComboBox<Integer> yearFilter;
    private JComboBox<String> scopeFilter;
    private JComboBox<String> periodFilter;
    private JComboBox<String> regionFilter;

    public OverviewPanel(
            Map<String, TableMeta> schema
    ) {

        this.schema = schema;

        configurePanel();
        createLayout();
    }

    private void configurePanel() {

        setLayout(
                new BorderLayout(
                        0,
                        15
                )
        );

        setBackground(
                BACKGROUND_COLOR
        );

        setBorder(
                new EmptyBorder(
                        5,
                        25,
                        25,
                        25
                )
        );
    }

    private void createLayout() {

        JPanel topSection =
                new JPanel(
                        new BorderLayout(
                                0,
                                15
                        )
                );

        topSection.setBackground(
                BACKGROUND_COLOR
        );

        /*
         * Header + global filter.
         */
        JPanel headerSection =
                new JPanel(
                        new BorderLayout(
                                0,
                                10
                        )
                );

        headerSection.setBackground(
                BACKGROUND_COLOR
        );

        headerSection.add(
                createHeader(),
                BorderLayout.NORTH
        );

        headerSection.add(
                createGlobalFilter(),
                BorderLayout.CENTER
        );

        topSection.add(
                headerSection,
                BorderLayout.NORTH
        );

        /*
         * KPI cards.
         */
        kpiPanel =
                new KpiPanel();

        topSection.add(
                kpiPanel,
                BorderLayout.CENTER
        );

        add(
                topSection,
                BorderLayout.NORTH
        );

        /*
         * Graph + Compare Data.
         */
        add(
                createDashboardArea(),
                BorderLayout.CENTER
        );

        /*
         * Load real data when dashboard opens.
         */
        applyGlobalFilters();
    }

    private JPanel createHeader() {

        JPanel header =
                new JPanel(
                        new BorderLayout()
                );

        header.setBackground(
                BACKGROUND_COLOR
        );

        JPanel titleArea =
                new JPanel();

        titleArea.setLayout(
                new BoxLayout(
                        titleArea,
                        BoxLayout.Y_AXIS
                )
        );

        titleArea.setBackground(
                BACKGROUND_COLOR
        );

        JLabel titleLabel =
                new JLabel(
                        "Overview"
                );

        titleLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        28
                )
        );

        titleLabel.setForeground(
                PRIMARY_TEXT
        );

        titleLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "EEEE, d MMMM yyyy"
                );

        JLabel dateLabel =
                new JLabel(
                        LocalDate.now()
                                .format(formatter)
                                + "  |  Live retail overview"
                );

        dateLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        14
                )
        );

        dateLabel.setForeground(
                SECONDARY_TEXT
        );

        dateLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        titleArea.add(
                titleLabel
        );

        titleArea.add(
                Box.createVerticalStrut(
                        4
                )
        );

        titleArea.add(
                dateLabel
        );

        JButton refreshButton =
                new JButton(
                        "Refresh Dashboard"
                );

        refreshButton.setFocusPainted(
                false
        );

        refreshButton.setBorderPainted(
                false
        );

        refreshButton.setOpaque(
                true
        );

        refreshButton.setBackground(
                ACTIVE_COLOR
        );

        refreshButton.setForeground(
                SIDEBAR_COLOUR
        );

        refreshButton.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        13
                )
        );

        refreshButton.setPreferredSize(
                new Dimension(
                        150,
                        38
                )
        );

        refreshButton.addActionListener(
                event -> refreshDashboard()
        );

        JPanel refreshArea =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                0,
                                5
                        )
                );

        refreshArea.setBackground(
                BACKGROUND_COLOR
        );

        refreshArea.add(
                refreshButton
        );

        header.add(
                titleArea,
                BorderLayout.WEST
        );

        header.add(
                refreshArea,
                BorderLayout.EAST
        );

        return header;
    }

    /*
     * Creates the large dashboard-wide filter.
     */
    private JPanel createGlobalFilter() {

        JPanel filterPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                12,
                                10
                        )
                );

        filterPanel.setBackground(
                Color.WHITE
        );

        filterPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                BORDER_COLOR
                        ),
                        new EmptyBorder(
                                5,
                                10,
                                5,
                                10
                        )
                )
        );

        /*
         * YEAR
         */
        JLabel yearLabel =
                new JLabel(
                        "Year:"
                );

        yearLabel.setForeground(
                PRIMARY_TEXT
        );

        yearFilter =
                new JComboBox<>(
                        new Integer[]{
                                2023,
                                2024
                        }
                );

        yearFilter.setSelectedItem(
                2023
        );

        /*
         * SCOPE
         */
        JLabel scopeLabel =
                new JLabel(
                        "Scope:"
                );

        scopeLabel.setForeground(
                PRIMARY_TEXT
        );

        scopeFilter =
                new JComboBox<>(
                        new String[]{
                                "Yearly",
                                "Quarterly",
                                "Monthly"
                        }
                );

        scopeFilter.setSelectedItem(
                "Monthly"
        );

        /*
         * PERIOD
         */
        JLabel periodLabel =
                new JLabel(
                        "Period:"
                );

        periodLabel.setForeground(
                PRIMARY_TEXT
        );

        periodFilter =
                new JComboBox<>();

        updatePeriodFilter();

        /*
         * REGION
         */
        JLabel regionLabel =
                new JLabel(
                        "Region:"
                );

        regionLabel.setForeground(
                PRIMARY_TEXT
        );

        regionFilter =
                new JComboBox<>(
                        new String[]{
                                "All Regions",
                                "Auckland",
                                "Wellington",
                                "Christchurch"
                        }
                );

        /*
         * Change available periods when scope changes.
         */
        scopeFilter.addActionListener(
                event -> updatePeriodFilter()
        );

        /*
         * APPLY BUTTON
         */
        JButton applyButton =
                new JButton(
                        "Apply Filters"
                );

        applyButton.setFocusPainted(
                false
        );

        applyButton.setBorderPainted(
                false
        );

        applyButton.setOpaque(
                true
        );

        applyButton.setBackground(
                ACTIVE_COLOR
        );

        applyButton.setForeground(
                SIDEBAR_COLOUR
        );

        applyButton.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        12
                )
        );

        applyButton.addActionListener(
                event -> applyGlobalFilters()
        );

        /*
         * RESET BUTTON
         */
        JButton resetButton =
                new JButton(
                        "Reset"
                );

        resetButton.setFocusPainted(
                false
        );

        resetButton.addActionListener(
                event -> resetFilters()
        );

        filterPanel.add(
                yearLabel
        );

        filterPanel.add(
                yearFilter
        );

        filterPanel.add(
                scopeLabel
        );

        filterPanel.add(
                scopeFilter
        );

        filterPanel.add(
                periodLabel
        );

        filterPanel.add(
                periodFilter
        );

        filterPanel.add(
                regionLabel
        );

        filterPanel.add(
                regionFilter
        );

        filterPanel.add(
                applyButton
        );

        filterPanel.add(
                resetButton
        );

        return filterPanel;
    }

    /*
     * Updates Period based on scope.
     */
    private void updatePeriodFilter() {

        if (periodFilter == null
                || scopeFilter == null) {
            return;
        }

        periodFilter.removeAllItems();

        String scope =
                (String) scopeFilter.getSelectedItem();

        if ("Yearly".equals(scope)) {

            periodFilter.addItem(
                    "Full Year"
            );

        } else if ("Quarterly".equals(scope)) {

            periodFilter.addItem(
                    "Q1"
            );

            periodFilter.addItem(
                    "Q2"
            );

            periodFilter.addItem(
                    "Q3"
            );

            periodFilter.addItem(
                    "Q4"
            );

        } else {

            periodFilter.addItem(
                    "January"
            );

            periodFilter.addItem(
                    "February"
            );

            periodFilter.addItem(
                    "March"
            );

            periodFilter.addItem(
                    "April"
            );

            periodFilter.addItem(
                    "May"
            );

            periodFilter.addItem(
                    "June"
            );

            periodFilter.addItem(
                    "July"
            );

            periodFilter.addItem(
                    "August"
            );

            periodFilter.addItem(
                    "September"
            );

            periodFilter.addItem(
                    "October"
            );

            periodFilter.addItem(
                    "November"
            );

            periodFilter.addItem(
                    "December"
            );
        }
    }

    private JPanel createDashboardArea() {

        JPanel dashboardArea =
                new JPanel(
                        new GridBagLayout()
                );

        dashboardArea.setBackground(
                BACKGROUND_COLOR
        );

        GridBagConstraints constraints =
                new GridBagConstraints();

        constraints.fill =
                GridBagConstraints.BOTH;

        constraints.weighty =
                1.0;

        revenueChartPanel =
                new RevenueChartPanel();

        constraints.gridx =
                0;

        constraints.gridy =
                0;

        constraints.weightx =
                0.60;

        constraints.insets =
                new Insets(
                        0,
                        0,
                        0,
                        15
                );

        dashboardArea.add(
                revenueChartPanel,
                constraints
        );

        DataComparisonPanel comparisonPanel =
                new DataComparisonPanel(
                        schema
                );

        constraints.gridx =
                1;

        constraints.gridy =
                0;

        constraints.weightx =
                0.40;

        constraints.insets =
                new Insets(
                        0,
                        0,
                        0,
                        0
                );

        dashboardArea.add(
                comparisonPanel,
                constraints
        );

        return dashboardArea;
    }

    /*
     * Loads real KPI values using the selected year.
     */
    private void loadKpiData() {

        try {
    
            Integer selectedYear =
                    (Integer) yearFilter.getSelectedItem();
    
            if (selectedYear == null) {
                return;
            }
    
            String json =
                    ApiClient.getData(
                            "api/kpis/summary",
                            Map.of(
                                    "year_from",
                                    String.valueOf(selectedYear)
                            )
                    );
    
            System.out.println(
                    "KPI FILTER YEAR: " + selectedYear
            );
    
            System.out.println(
                    "KPI RESPONSE: " + json
            );
    
            double revenue =
                    extractNumber(
                            json,
                            "total_revenue"
                    );
    
            double growth =
                    extractNumber(
                            json,
                            "revenue_growth_pct"
                    );
    
            double profit =
                    extractNumber(
                            json,
                            "profit"
                    );
    
            double margin =
                    extractNumber(
                            json,
                            "profit_margin_pct"
                    );
    
            double turnover =
                    extractNumber(
                            json,
                            "inventory_turnover"
                    );
    
            double retention =
                    extractNumber(
                            json,
                            "retention_rate_pct"
                    );
    
            kpiPanel.updateKpis(
                    revenue,
                    growth,
                    profit,
                    margin,
                    turnover,
                    retention
            );
    
        } catch (Exception e) {
    
            e.printStackTrace();
    
            kpiPanel.showError();
        }
    }
    /*
     * Applies the global filters to the whole dashboard.
     */
    private void applyGlobalFilters() {

        Integer year =
                (Integer) yearFilter.getSelectedItem();
    
        String scope =
                (String) scopeFilter.getSelectedItem();
    
        String period =
                (String) periodFilter.getSelectedItem();
    
        String region =
                (String) regionFilter.getSelectedItem();
    
        System.out.println(
                "Applying filters:"
        );
    
        System.out.println(
                "Year = " + year
        );
    
        System.out.println(
                "Scope = " + scope
        );
    
        System.out.println(
                "Period = " + period
        );
    
        System.out.println(
                "Region = " + region
        );
    
        /*
         * Reload REAL KPI data.
         */
        loadKpiData();
    
        /*
         * Reload REAL graph data.
         */
        revenueChartPanel.applyFilters(
                year,
                scope,
                period,
                region
        );
    }

    /*
     * Resets the dashboard filter.
     */
    private void resetFilters() {

        yearFilter.setSelectedItem(
                2023
        );

        scopeFilter.setSelectedItem(
                "Monthly"
        );

        updatePeriodFilter();

        regionFilter.setSelectedItem(
                "All Regions"
        );

        applyGlobalFilters();
    }

    private double extractNumber(
            String json,
            String key
    ) {

        Pattern pattern =
                Pattern.compile(
                        "\""
                                + Pattern.quote(key)
                                + "\"\\s*:\\s*"
                                + "\"?(-?\\d+(?:\\.\\d+)?)\"?"
                );

        Matcher matcher =
                pattern.matcher(
                        json
                );

        if (matcher.find()) {

            return Double.parseDouble(
                    matcher.group(1)
            );
        }

        throw new IllegalArgumentException(
                "KPI value not found: "
                        + key
        );
    }

    private void refreshDashboard() {

        applyGlobalFilters();

        JOptionPane.showMessageDialog(
                this,
                "Dashboard data refreshed.",
                "Refresh Complete",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}