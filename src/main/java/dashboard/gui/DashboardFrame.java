package dashboard.gui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/*
 * This class creates the main dashboard window. It includes the logo area,
 * navigation sidebar, KPI cards and the interactive revenue bar chart.
 */
public class DashboardFrame extends JFrame {

    private static final Color SIDEBAR_COLOUR =
            new Color(17, 24, 39);

    private static final Color BACKGROUND_COLOR =
            new Color(245, 247, 250);

    private static final Color ACTIVE_COLOR =
            new Color(0, 212, 255);

    private static final Color PRIMARY_TEXT =
            new Color(31, 41, 55);

    private static final Color SECONDARY_TEXT =
            new Color(100, 116, 139);

    private static final Color BORDER_COLOR =
            new Color(226, 232, 240);

    /*
     * CardLayout allows the centre section to switch between dashboard pages
     * whenever the user clicks one of the navigation buttons.
     */
    private CardLayout contentCardLayout;
    private JPanel contentCardPanel;

    /*
     * These buttons are stored so the currently selected page can remain
     * highlighted in the sidebar.
     */
    private JButton overviewButton;
    private JButton salesButton;
    private JButton inventoryButton;
    private JButton reportsButton;
    private JButton alertsButton;

    /*
     * These labels will later display KPI values returned by the backend.
     * Placeholder values are used until the database integration is complete.
     */
    private JLabel totalRevenueValue;
    private JLabel totalProfitValue;
    private JLabel customersValue;
    private JLabel productsValue;

    /*
     * These fields control the interactive revenue chart. The dataset is
     * updated whenever the user changes the selected scope or year.
     */
    private DefaultCategoryDataset revenueDataset;
    private JComboBox<String> scopeFilter;
    private JComboBox<Integer> yearFilter;
    private JLabel chartStatusLabel;

    private final java.util.Random random = new java.util.Random();

private double sampleRevenue = 61500.00;
private double sampleProfit = 16400.00;
private int sampleCustomers = 5000;
private int sampleProducts = 500;

    /*
     * This constructor configures the window and creates the dashboard layout
     * before the application is displayed.
     */
    public DashboardFrame() {
        configureWindow();
        createLayout();
    }

    /*
     * This method configures the main dashboard window by setting its title,
     * size, minimum size, close operation and location on the screen.
     */
    private void configureWindow() {
        setTitle("Dynamic Retail Dashboard");
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    /*
     * This method creates the main BorderLayout and keeps all five regions
     * available so more dashboard features can be added later.
     */
    private void createLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        /*
         * The NORTH section contains the logo on the left and leaves the
         * remaining top area available for future controls.
         */
        JPanel north = new JPanel(new BorderLayout());
        north.setPreferredSize(new Dimension(0, 85));
        north.setBackground(BACKGROUND_COLOR);

        JPanel logoArea = createLogoArea();
        logoArea.setPreferredSize(new Dimension(210, 85));

        JPanel topContentArea = new JPanel(new BorderLayout());
        topContentArea.setBackground(BACKGROUND_COLOR);

        north.add(logoArea, BorderLayout.WEST);
        north.add(topContentArea, BorderLayout.CENTER);

        /*
         * The EAST and SOUTH sections are kept for future use, but their
         * dimensions are currently set to zero.
         */
        JPanel east = new JPanel();
        east.setPreferredSize(new Dimension(0, 0));
        east.setBackground(BACKGROUND_COLOR);

        JPanel south = new JPanel();
        south.setPreferredSize(new Dimension(0, 0));
        south.setBackground(BACKGROUND_COLOR);

        JPanel west = createLeftSidebar();
        JPanel center = createContentArea();

        mainPanel.add(north, BorderLayout.NORTH);
        mainPanel.add(south, BorderLayout.SOUTH);
        mainPanel.add(east, BorderLayout.EAST);
        mainPanel.add(west, BorderLayout.WEST);
        mainPanel.add(center, BorderLayout.CENTER);

        add(mainPanel);
    }

    /*
     * This method creates the logo area in the top-left corner and keeps it
     * visually connected to the sidebar underneath it.
     */
    private JPanel createLogoArea() {
        JPanel logoArea = new JPanel();
        logoArea.setLayout(
                new BoxLayout(logoArea, BoxLayout.Y_AXIS)
        );
        logoArea.setBackground(SIDEBAR_COLOUR);
        logoArea.setBorder(
                new EmptyBorder(17, 18, 15, 18)
        );

        JLabel logoLineOne =
                new JLabel("Dynamic Retail");

        logoLineOne.setForeground(Color.WHITE);
        logoLineOne.setFont(
                new Font("SansSerif", Font.BOLD, 18)
        );
        logoLineOne.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel logoLineTwo =
                new JLabel("Dashboard");

        logoLineTwo.setForeground(ACTIVE_COLOR);
        logoLineTwo.setFont(
                new Font("SansSerif", Font.BOLD, 18)
        );
        logoLineTwo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle =
                new JLabel("Admin Dashboard");

        subtitle.setForeground(
                new Color(170, 180, 195)
        );
        subtitle.setFont(
                new Font("SansSerif", Font.PLAIN, 11)
        );
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        logoArea.add(logoLineOne);
        logoArea.add(logoLineTwo);
        logoArea.add(Box.createVerticalStrut(3));
        logoArea.add(subtitle);

        return logoArea;
    }

    /*
     * This method creates the sidebar on the left side of the application and
     * places the navigation buttons at the top.
     */
    private JPanel createLeftSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());

        sidebar.setPreferredSize(
                new Dimension(210, 0)
        );

        sidebar.setBackground(SIDEBAR_COLOUR);

        sidebar.setBorder(
                new EmptyBorder(25, 18, 25, 18)
        );

        sidebar.add(
                createNavButtons(),
                BorderLayout.NORTH
        );

        return sidebar;
    }

    /*
     * This method creates the navigation buttons and connects each one to its
     * matching dashboard page using CardLayout.
     */
    private JPanel createNavButtons() {
        JPanel nav = new JPanel();

        nav.setLayout(
                new BoxLayout(nav, BoxLayout.Y_AXIS)
        );

        nav.setBackground(SIDEBAR_COLOUR);

        overviewButton =
                createNavigationButton("Overview", true);

        salesButton =
                createNavigationButton("Sales", false);

        inventoryButton =
                createNavigationButton("Inventory", false);

        reportsButton =
                createNavigationButton("Reports", false);

        alertsButton =
                createNavigationButton("Alerts", false);

        overviewButton.addActionListener(event ->
                showPage("Overview", overviewButton)
        );

        salesButton.addActionListener(event ->
                showPage("Sales", salesButton)
        );

        inventoryButton.addActionListener(event ->
                showPage("Inventory", inventoryButton)
        );

        reportsButton.addActionListener(event ->
                showPage("Reports", reportsButton)
        );

        alertsButton.addActionListener(event ->
                showPage("Alerts", alertsButton)
        );

        nav.add(overviewButton);
        nav.add(Box.createVerticalStrut(8));

        nav.add(salesButton);
        nav.add(Box.createVerticalStrut(8));

        nav.add(inventoryButton);
        nav.add(Box.createVerticalStrut(8));

        nav.add(reportsButton);
        nav.add(Box.createVerticalStrut(8));

        nav.add(alertsButton);

        return nav;
    }

    /*
     * This method creates one reusable navigation button. The active button
     * uses the cyan colour so the selected page is easy to identify.
     */
    private JButton createNavigationButton(
            String text,
            boolean isActive
    ) {
        JButton button = new JButton(text);

        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);

        button.setBackground(
                isActive
                        ? ACTIVE_COLOR
                        : SIDEBAR_COLOUR
        );

        button.setForeground(Color.WHITE);

        button.setFont(
                new Font("SansSerif", Font.PLAIN, 14)
        );

        button.setPreferredSize(
                new Dimension(174, 40)
        );

        button.setMaximumSize(
                new Dimension(174, 40)
        );

        button.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        button.setBorder(
                new EmptyBorder(0, 15, 0, 15)
        );

        return button;
    }

    /*
     * This method creates the centre CardLayout and adds each dashboard page
     * using a unique name.
     */
    private JPanel createContentArea() {
        contentCardLayout = new CardLayout();

        contentCardPanel =
                new JPanel(contentCardLayout);

        contentCardPanel.setBackground(BACKGROUND_COLOR);

        contentCardPanel.add(
                createOverviewPage(),
                "Overview"
        );

        contentCardPanel.add(
                createPagePlaceholder("Sales"),
                "Sales"
        );

        contentCardPanel.add(
                createPagePlaceholder("Inventory"),
                "Inventory"
        );

        contentCardPanel.add(
                createPagePlaceholder("Reports"),
                "Reports"
        );

        contentCardPanel.add(
                createPagePlaceholder("Alerts"),
                "Alerts"
        );

        return contentCardPanel;
    }

    /*
     * This method creates the Overview page. The heading and KPI cards appear
     * at the top, while the smaller chart area appears underneath them.
     */
    private JPanel createOverviewPage() {
        JPanel overviewPage =
                new JPanel(new BorderLayout(0, 15));

        overviewPage.setBackground(BACKGROUND_COLOR);

        overviewPage.setBorder(
                new EmptyBorder(5, 25, 25, 25)
        );

        JPanel topSection =
                new JPanel(new BorderLayout(0, 15));

        topSection.setBackground(BACKGROUND_COLOR);

        topSection.add(
                createHeader(),
                BorderLayout.NORTH
        );

        topSection.add(
                createKpiPanel(),
                BorderLayout.CENTER
        );

        overviewPage.add(
                topSection,
                BorderLayout.NORTH
        );

        overviewPage.add(
                createDashboardGraphArea(),
                BorderLayout.CENTER
        );

        return overviewPage;
    }

    /*
     * This method creates the Overview page header. The current date appears
     * automatically and the refresh button reloads all dashboard information.
     */
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BACKGROUND_COLOR);

        JPanel leftHeader = new JPanel();

        leftHeader.setLayout(
                new BoxLayout(leftHeader, BoxLayout.Y_AXIS)
        );

        leftHeader.setBackground(BACKGROUND_COLOR);

        JLabel titleLabel =
                new JLabel("Overview");

        titleLabel.setFont(
                new Font("SansSerif", Font.BOLD, 28)
        );

        titleLabel.setForeground(PRIMARY_TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        LocalDate currentDate =
                LocalDate.now();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "EEEE, d MMMM yyyy"
                );

        JLabel dateLabel = new JLabel(
                currentDate.format(formatter)
                        + "  |  Live retail overview"
        );

        dateLabel.setFont(
                new Font("SansSerif", Font.PLAIN, 14)
        );

        dateLabel.setForeground(SECONDARY_TEXT);
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftHeader.add(titleLabel);
        leftHeader.add(Box.createVerticalStrut(4));
        leftHeader.add(dateLabel);

        JButton refreshButton =
                new JButton("Refresh Dashboard");

        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setOpaque(true);

        refreshButton.setForeground(SIDEBAR_COLOUR);
        refreshButton.setBackground(ACTIVE_COLOR);

        refreshButton.setFont(
                new Font("SansSerif", Font.BOLD, 13)
        );

        refreshButton.setPreferredSize(
                new Dimension(150, 38)
        );

        refreshButton.addActionListener(event ->
                refreshDashboard()
        );

        JPanel refreshArea =
                new JPanel(new FlowLayout(
                        FlowLayout.RIGHT,
                        0,
                        5
                ));

        refreshArea.setBackground(BACKGROUND_COLOR);
        refreshArea.add(refreshButton);

        header.add(leftHeader, BorderLayout.WEST);
        header.add(refreshArea, BorderLayout.EAST);

        return header;
    }

    /*
     * This method creates the row of four KPI cards. The placeholder values
     * can later be replaced with results returned by the backend.
     */
    private JPanel createKpiPanel() {
        JPanel kpiPanel =
                new JPanel(new GridLayout(1, 4, 15, 0));

        kpiPanel.setBackground(BACKGROUND_COLOR);

        totalRevenueValue = new JLabel("$0.00");
        totalProfitValue = new JLabel("$0.00");
        customersValue = new JLabel("0");
        productsValue = new JLabel("0");

        kpiPanel.add(
                createKpiCard(
                        "Total Revenue",
                        totalRevenueValue
                )
        );

        kpiPanel.add(
                createKpiCard(
                        "Total Profit",
                        totalProfitValue
                )
        );

        kpiPanel.add(
                createKpiCard(
                        "Customers",
                        customersValue
                )
        );

        kpiPanel.add(
                createKpiCard(
                        "Products",
                        productsValue
                )
        );

        return kpiPanel;
    }

    /*
     * This method creates one reusable KPI card. The value is passed as a label
     * so it can easily be updated when backend results become available.
     */
    private JPanel createKpiCard(
            String title,
            JLabel valueLabel
    ) {
        JPanel card = new JPanel();

        card.setLayout(
                new BoxLayout(card, BoxLayout.Y_AXIS)
        );

        card.setBackground(Color.WHITE);

        card.setPreferredSize(
                new Dimension(0, 85)
        );

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                BORDER_COLOR
                        ),
                        new EmptyBorder(15, 18, 15, 18)
                )
        );

        JLabel titleLabel =
                new JLabel(title);

        titleLabel.setFont(
                new Font("SansSerif", Font.PLAIN, 13)
        );

        titleLabel.setForeground(SECONDARY_TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel.setFont(
                new Font("SansSerif", Font.BOLD, 23)
        );

        valueLabel.setForeground(PRIMARY_TEXT);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);

        return card;
    }

    /*
     * This method creates the graph section underneath the KPI cards. The
     * current bar chart uses part of the available width so another chart can
     * be added beside it later without changing the overall page structure.
     */
    private JPanel createDashboardGraphArea() {
        JPanel graphArea =
                new JPanel(new GridBagLayout());

        graphArea.setBackground(BACKGROUND_COLOR);

        GridBagConstraints constraints =
                new GridBagConstraints();

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weighty = 1.0;

        /*
         * The working revenue chart uses around two-thirds of the graph row.
         */
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0.67;
        constraints.insets =
                new Insets(0, 0, 0, 15);

        graphArea.add(
                createInteractiveRevenueChart(),
                constraints
        );

        /*
         * This temporary area reserves space for a second graph or another
         * dashboard feature that can be developed later.
         */
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 0.33;
        constraints.insets =
                new Insets(0, 0, 0, 0);

        graphArea.add(
                createFutureGraphPlaceholder(),
                constraints
        );

        return graphArea;
    }

    /*
     * This method creates the interactive revenue bar chart. The dropdown
     * controls allow the user to change the time scope and selected year.
     */
    private JPanel createInteractiveRevenueChart() {
        JPanel chartCard =
                new JPanel(new BorderLayout(0, 10));

        chartCard.setBackground(Color.WHITE);

        chartCard.setPreferredSize(
                new Dimension(600, 350)
        );

        chartCard.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                BORDER_COLOR
                        ),
                        new EmptyBorder(15, 16, 12, 16)
                )
        );

        chartCard.add(
                createChartHeaderAndFilters(),
                BorderLayout.NORTH
        );

        revenueDataset =
                new DefaultCategoryDataset();

        JFreeChart revenueChart =
                ChartFactory.createBarChart(
                        null,
                        "Period",
                        "Revenue ($)",
                        revenueDataset
                );

        styleRevenueBarChart(revenueChart);

        ChartPanel chartPanel =
                new ChartPanel(revenueChart);

        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(null);

        /*
         * Mouse-wheel zooming is disabled because the graph should remain
         * fixed inside the dashboard card.
         */
        chartPanel.setMouseWheelEnabled(false);

        chartCard.add(
                chartPanel,
                BorderLayout.CENTER
        );

        chartStatusLabel =
                new JLabel("Displaying monthly sample data");

        chartStatusLabel.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );

        chartStatusLabel.setForeground(SECONDARY_TEXT);

        chartCard.add(
                chartStatusLabel,
                BorderLayout.SOUTH
        );

        refreshRevenueChart();

        return chartCard;
    }

    /*
     * This method creates the title and filters shown above the revenue chart.
     * Changing either filter immediately reloads the sample chart information.
     */
    private JPanel createChartHeaderAndFilters() {
        JPanel header =
                new JPanel(new BorderLayout(10, 6));

        header.setBackground(Color.WHITE);

        JPanel titleArea = new JPanel();

        titleArea.setLayout(
                new BoxLayout(titleArea, BoxLayout.Y_AXIS)
        );

        titleArea.setBackground(Color.WHITE);

        JLabel chartTitle =
                new JLabel("Revenue Trend");

        chartTitle.setFont(
                new Font("SansSerif", Font.BOLD, 17)
        );

        chartTitle.setForeground(PRIMARY_TEXT);
        chartTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel chartDescription =
                new JLabel("Revenue by selected time period");

        chartDescription.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );

        chartDescription.setForeground(SECONDARY_TEXT);
        chartDescription.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleArea.add(chartTitle);
        titleArea.add(Box.createVerticalStrut(2));
        titleArea.add(chartDescription);

        JPanel filters =
                new JPanel(new FlowLayout(
                        FlowLayout.RIGHT,
                        7,
                        0
                ));

        filters.setBackground(Color.WHITE);

        JLabel scopeLabel =
                new JLabel("Scope:");

        scopeLabel.setForeground(PRIMARY_TEXT);

        scopeFilter = new JComboBox<>(
                new String[]{
                        "Weekly",
                        "Monthly",
                        "Quarterly",
                        "Yearly"
                }
        );

        scopeFilter.setSelectedItem("Monthly");

        JLabel yearLabel =
                new JLabel("Year:");

        yearLabel.setForeground(PRIMARY_TEXT);

        int currentYear =
                LocalDate.now().getYear();

        yearFilter = new JComboBox<>(
                new Integer[]{
                        currentYear,
                        currentYear - 1,
                        currentYear - 2,
                        currentYear - 3
                }
        );

        scopeFilter.addActionListener(event ->
                refreshRevenueChart()
        );

        yearFilter.addActionListener(event ->
                refreshRevenueChart()
        );

        filters.add(scopeLabel);
        filters.add(scopeFilter);
        filters.add(yearLabel);
        filters.add(yearFilter);

        header.add(titleArea, BorderLayout.NORTH);
        header.add(filters, BorderLayout.SOUTH);

        return header;
    }

    /*
     * This method creates a temporary card beside the current graph. It keeps
     * space available for another graph without requiring the layout to be
     * rebuilt later.
     */
    private JPanel createFutureGraphPlaceholder() {
        JPanel placeholder =
                new JPanel(new GridBagLayout());

        placeholder.setBackground(Color.WHITE);

        placeholder.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                BORDER_COLOR
                        ),
                        new EmptyBorder(15, 15, 15, 15)
                )
        );

        JLabel placeholderLabel =
                new JLabel(
                        "<html><div style='text-align:center;'>"
                                + "Future Graph<br>"
                                + "<span style='font-size:10px;'>"
                                + "Another graph can be added here later"
                                + "</span></div></html>"
                );

        placeholderLabel.setFont(
                new Font("SansSerif", Font.BOLD, 15)
        );

        placeholderLabel.setForeground(SECONDARY_TEXT);

        placeholder.add(placeholderLabel);

        return placeholder;
    }

    /*
 * This method refreshes the whole Overview page. For now, it updates the
 * sample KPI values and reloads the sample chart data. Later, Cooper can
 * replace these values with results returned from the database.
 */
private void refreshDashboard() {

    refreshSampleKpiValues();
    refreshRevenueChart();

    JOptionPane.showMessageDialog(
            this,
            "Dashboard refreshed successfully.",
            "Refresh Complete",
            JOptionPane.INFORMATION_MESSAGE
    );
}

    /*
 * This method updates the KPI cards using small temporary changes. The values
 * stay close to the original sample figures so refreshing the dashboard does
 * not create unrealistic jumps.
 */
private void refreshSampleKpiValues() {

    sampleRevenue += random.nextDouble() * 2000 - 1000;
    sampleProfit += random.nextDouble() * 800 - 400;
    sampleCustomers += random.nextInt(21) - 10;
    sampleProducts += random.nextInt(7) - 3;

    /*
     * These checks stop the temporary sample values from becoming negative
     * or dropping below a realistic minimum.
     */
    sampleRevenue = Math.max(50000, sampleRevenue);
    sampleProfit = Math.max(10000, sampleProfit);
    sampleCustomers = Math.max(4500, sampleCustomers);
    sampleProducts = Math.max(450, sampleProducts);

    totalRevenueValue.setText(
            String.format("$%,.2f", sampleRevenue)
    );

    totalProfitValue.setText(
            String.format("$%,.2f", sampleProfit)
    );

    customersValue.setText(
            String.format("%,d", sampleCustomers)
    );

    productsValue.setText(
            String.format("%,d", sampleProducts)
    );
}

    /*
     * This method reads the selected filters and refreshes the chart. It uses
     * one sample-data method now so Cooper can easily replace the sample call
     * with a database service call later.
     */
    private void refreshRevenueChart() {
        if (revenueDataset == null
                || scopeFilter == null
                || yearFilter == null) {
            return;
        }

        String selectedScope =
                (String) scopeFilter.getSelectedItem();

        Integer selectedYear =
                (Integer) yearFilter.getSelectedItem();

        if (selectedScope == null
                || selectedYear == null) {
            return;
        }

        /*
         * This is the main line Cooper can replace when the backend is ready.
         *
         * It could later become:
         *
         * List<RevenueTrendPoint> data =
         *         dashboardService.getRevenueTrend(
         *                 selectedScope,
         *                 selectedYear
         *         );
         * or along these lines, depending on how the backend is structured. 
         */
        loadSampleRevenueData(
                selectedScope,
                selectedYear
        );

        if (chartStatusLabel != null) {
            chartStatusLabel.setText(
                    "Displaying "
                            + selectedScope.toLowerCase()
                            + " data for "
                            + selectedYear
            );
        }
    }

    /*
     * This method contains all temporary chart values in one place. Cooper can
     * later remove this method and replace it with results from the database.
     */
    private void loadSampleRevenueData(
            String selectedScope,
            int selectedYear
    ) {
        revenueDataset.clear();

        switch (selectedScope) {

            case "Weekly" -> {
                revenueDataset.addValue(
                        12000,
                        "Revenue",
                        "Week 1"
                );

                revenueDataset.addValue(
                        15750,
                        "Revenue",
                        "Week 2"
                );

                revenueDataset.addValue(
                        14100,
                        "Revenue",
                        "Week 3"
                );

                revenueDataset.addValue(
                        18900,
                        "Revenue",
                        "Week 4"
                );
            }

            case "Monthly" -> {
                String[] months = {
                        "Jan", "Feb", "Mar", "Apr",
                        "May", "Jun", "Jul", "Aug",
                        "Sep", "Oct", "Nov", "Dec"
                };

                double[] monthlyRevenue = {
                        12000, 16000, 14500, 19000,
                        22000, 25000, 23000, 27500,
                        26000, 29000, 31000, 34000
                };

                for (int index = 0;
                     index < months.length;
                     index++) {

                    revenueDataset.addValue(
                            monthlyRevenue[index],
                            "Revenue",
                            months[index]
                    );
                }
            }

            case "Quarterly" -> {
                revenueDataset.addValue(
                        42500,
                        "Revenue",
                        "Q1"
                );

                revenueDataset.addValue(
                        66000,
                        "Revenue",
                        "Q2"
                );

                revenueDataset.addValue(
                        76500,
                        "Revenue",
                        "Q3"
                );

                revenueDataset.addValue(
                        94000,
                        "Revenue",
                        "Q4"
                );
            }

            case "Yearly" -> {
                revenueDataset.addValue(
                        235000,
                        "Revenue",
                        String.valueOf(selectedYear - 3)
                );

                revenueDataset.addValue(
                        278000,
                        "Revenue",
                        String.valueOf(selectedYear - 2)
                );

                revenueDataset.addValue(
                        315000,
                        "Revenue",
                        String.valueOf(selectedYear - 1)
                );

                revenueDataset.addValue(
                        342000,
                        "Revenue",
                        String.valueOf(selectedYear)
                );
            }

            default -> throw new IllegalArgumentException(
                    "Unknown scope: " + selectedScope
            );
        }
    }

    /*
     * This method applies consistent colours and formatting to the revenue bar
     * chart so it matches the rest of the dashboard interface.
     */
    private void styleRevenueBarChart(
            JFreeChart chart
    ) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);

        CategoryPlot plot =
                chart.getCategoryPlot();

        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(BORDER_COLOR);
        plot.setDomainGridlinesVisible(false);

        BarRenderer renderer =
                (BarRenderer) plot.getRenderer();

        renderer.setSeriesPaint(
                0,
                ACTIVE_COLOR
        );

        renderer.setMaximumBarWidth(0.09);
        renderer.setShadowVisible(false);
    }

    /*
     * This method creates a temporary page for sections that have not been
     * developed yet while keeping the navigation feature fully testable.
     */
    private JPanel createPagePlaceholder(
            String pageName
    ) {
        JPanel page =
                new JPanel(new GridBagLayout());

        page.setBackground(BACKGROUND_COLOR);

        page.setBorder(
                new EmptyBorder(20, 25, 25, 25)
        );

        JLabel pageLabel =
                new JLabel(pageName + " Page");

        pageLabel.setFont(
                new Font("SansSerif", Font.BOLD, 28)
        );

        pageLabel.setForeground(PRIMARY_TEXT);

        page.add(pageLabel);

        return page;
    }

    /*
     * This method changes the page displayed in the centre and updates the
     * sidebar so the selected navigation button remains highlighted.
     */
    private void showPage(
            String pageName,
            JButton selectedButton
    ) {
        contentCardLayout.show(
                contentCardPanel,
                pageName
        );

        updateActiveButton(selectedButton);

        contentCardPanel.revalidate();
        contentCardPanel.repaint();
    }

    /*
     * This method resets every navigation button before applying the active
     * cyan colour to the button selected by the user.
     */
    private void updateActiveButton(
            JButton selectedButton
    ) {
        JButton[] navigationButtons = {
                overviewButton,
                salesButton,
                inventoryButton,
                reportsButton,
                alertsButton
        };

        for (JButton button : navigationButtons) {
            button.setBackground(SIDEBAR_COLOUR);
        }

        selectedButton.setBackground(ACTIVE_COLOR);
    }
}