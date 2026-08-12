package dashboard.gui;

import dashboard.database.ApiClient;
import dashboard.database.SchemaIntrospector.TableMeta;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
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

    private final Map<String, TableMeta> schema;

    private KpiPanel kpiPanel;
    private RevenueChartPanel revenueChartPanel;

    private JComboBox<Integer> yearFilter;
    private JComboBox<String> scopeFilter;
    private JComboBox<String> periodFilter;
    private JComboBox<String> regionFilter;

    public OverviewPanel(Map<String, TableMeta> schema) {

        this.schema = schema;

        configurePanel();
        createLayout();
    }

    private void configurePanel() {

        setLayout(
                new BorderLayout(0, 15)
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
                        new BorderLayout(0, 15)
                );

        topSection.setBackground(
                BACKGROUND_COLOR
        );

        JPanel headerFilterSection =
                new JPanel(
                        new BorderLayout(0, 10)
                );

        headerFilterSection.setBackground(
                BACKGROUND_COLOR
        );

        headerFilterSection.add(
                createHeader(),
                BorderLayout.NORTH
        );

        headerFilterSection.add(
                createGlobalFilter(),
                BorderLayout.CENTER
        );

        topSection.add(
                headerFilterSection,
                BorderLayout.NORTH
        );

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

        add(
                createDashboardArea(),
                BorderLayout.CENTER
        );

        /*
         * Load the first selected filter
         * when the page starts.
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

        JLabel title =
                new JLabel(
                        "Overview"
                );

        title.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        28
                )
        );

        title.setForeground(
                PRIMARY_TEXT
        );

        title.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "EEEE, d MMMM yyyy"
                );

        JLabel date =
                new JLabel(
                        LocalDate.now()
                                .format(formatter)
                                + "  |  Live retail overview"
                );

        date.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        14
                )
        );

        date.setForeground(
                SECONDARY_TEXT
        );

        date.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        titleArea.add(title);

        titleArea.add(
                Box.createVerticalStrut(4)
        );

        titleArea.add(date);

        JButton refreshButton =
                new JButton(
                        "Refresh Dashboard"
                );

        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setOpaque(true);

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

        JLabel yearLabel =
                new JLabel("Year:");

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

        JLabel scopeLabel =
                new JLabel("Scope:");

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

        JLabel periodLabel =
                new JLabel("Period:");

        periodFilter =
                new JComboBox<>();

        updatePeriodFilter();

        scopeFilter.addActionListener(
                event -> updatePeriodFilter()
        );

        JLabel regionLabel =
                new JLabel("Region:");

        regionFilter =
                new JComboBox<>(
                        new String[]{
                                "All Regions",
                                "Auckland",
                                "Wellington",
                                "Christchurch"
                        }
                );

        JButton applyButton =
                new JButton(
                        "Apply Filters"
                );

        applyButton.setFocusPainted(false);
        applyButton.setBorderPainted(false);
        applyButton.setOpaque(true);

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

        JButton resetButton =
                new JButton(
                        "Reset"
                );

        resetButton.setFocusPainted(false);

        resetButton.addActionListener(
                event -> resetFilters()
        );

        filterPanel.add(yearLabel);
        filterPanel.add(yearFilter);

        filterPanel.add(scopeLabel);
        filterPanel.add(scopeFilter);

        filterPanel.add(periodLabel);
        filterPanel.add(periodFilter);

        filterPanel.add(regionLabel);
        filterPanel.add(regionFilter);

        filterPanel.add(applyButton);
        filterPanel.add(resetButton);

        return filterPanel;
    }

    private void updatePeriodFilter() {

        if (scopeFilter == null
                || periodFilter == null) {

            return;
        }

        periodFilter.removeAllItems();

        String scope =
                (String)
                        scopeFilter.getSelectedItem();

        if ("Yearly".equals(scope)) {

            periodFilter.addItem(
                    "Full Year"
            );

        } else if ("Quarterly".equals(scope)) {

            periodFilter.addItem("Q1");
            periodFilter.addItem("Q2");
            periodFilter.addItem("Q3");
            periodFilter.addItem("Q4");

        } else {

            periodFilter.addItem("January");
            periodFilter.addItem("February");
            periodFilter.addItem("March");
            periodFilter.addItem("April");
            periodFilter.addItem("May");
            periodFilter.addItem("June");
            periodFilter.addItem("July");
            periodFilter.addItem("August");
            periodFilter.addItem("September");
            periodFilter.addItem("October");
            periodFilter.addItem("November");
            periodFilter.addItem("December");
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

        constraints.gridx = 0;
        constraints.gridy = 0;

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

        constraints.gridx = 1;

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
     * ONE BIG FILTER
     *
     * This passes the SAME values to:
     * - KPI cards
     * - Revenue graph
     */
    private void applyGlobalFilters() {

        Integer year =
                (Integer)
                        yearFilter.getSelectedItem();

        String scope =
                (String)
                        scopeFilter.getSelectedItem();

        String period =
                (String)
                        periodFilter.getSelectedItem();

        String region =
                (String)
                        regionFilter.getSelectedItem();

        if (year == null
                || scope == null
                || period == null
                || region == null) {

            return;
        }

        System.out.println(
                "GLOBAL FILTER:"
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
         * Update KPI cards.
         */
        loadFilteredKpis(
                year,
                scope,
                period,
                region
        );

        /*
         * Update graph.
         */
        revenueChartPanel.applyFilters(
                year,
                scope,
                period,
                region
        );
    }

    /*
     * REAL filtered KPI endpoint.
     */
    private void loadFilteredKpis(
            Integer year,
            String scope,
            String period,
            String region
    ) {

        try {

            Map<String, String> params =
                    new HashMap<>();

            params.put(
                    "year",
                    String.valueOf(year)
            );

            params.put(
                    "scope",
                    scope.toLowerCase()
            );

            params.put(
                    "period",
                    period
            );

            params.put(
                    "region",
                    region
            );

            String json =
                    ApiClient.getData(
                            "api/kpis/filtered",
                            params
                    );

            System.out.println(
                    "FILTERED KPI RESPONSE:"
            );

            System.out.println(
                    json
            );

            /*
             * These are currently the two
             * SOLO KPIs returned by the backend.
             */
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

            /*
             * Only update those two cards.
             */
            kpiPanel.updateSoloKpis(
                    revenue,
                    growth
            );

        } catch (Exception e) {

            System.err.println(
                    "Filtered KPI error:"
            );

            e.printStackTrace();

            kpiPanel.showError();
        }
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
                "Missing KPI field: "
                        + key
        );
    }

    private void refreshDashboard() {

        /*
         * Re-run the currently selected
         * global filter against the database.
         */
        applyGlobalFilters();

        JOptionPane.showMessageDialog(
                this,
                "Dashboard refreshed from database.",
                "Refresh Complete",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

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
}