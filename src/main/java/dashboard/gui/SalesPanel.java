package dashboard.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SalesPanel extends JPanel {

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

    private JComboBox<Integer> yearFilter;
    private JComboBox<String> scopeFilter;
    private JComboBox<String> periodFilter;

    private RevenueOverTimeChart revenueOverTimeChart;
    private RevenueByRegionChart revenueByRegionChart;

    public SalesPanel() {

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
                        20,
                        25,
                        25,
                        25
                )
        );
    }

    private void createLayout() {

        /*
         * ============================
         * TOP SECTION
         * ============================
         */

        JPanel topSection =
                new JPanel(
                        new BorderLayout(0, 12)
                );

        topSection.setBackground(
                BACKGROUND_COLOR
        );

        topSection.add(
                createHeader(),
                BorderLayout.NORTH
        );

        topSection.add(
                createFilterBar(),
                BorderLayout.CENTER
        );

        add(
                topSection,
                BorderLayout.NORTH
        );

        /*
         * ============================
         * CHARTS
         * ============================
         */

        revenueOverTimeChart =
                new RevenueOverTimeChart();

        revenueByRegionChart =
                new RevenueByRegionChart();

        revenueOverTimeChart.setPreferredSize(
                new Dimension(
                        900,
                        380
                )
        );

        revenueOverTimeChart.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        380
                )
        );

        revenueByRegionChart.setPreferredSize(
                new Dimension(
                        900,
                        380
                )
        );

        revenueByRegionChart.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        380
                )
        );

        revenueOverTimeChart.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        revenueByRegionChart.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        /*
         * ============================
         * VERTICAL CHART LAYOUT
         * ============================
         */

        JPanel chartsPanel =
                new JPanel();

        chartsPanel.setLayout(
                new BoxLayout(
                        chartsPanel,
                        BoxLayout.Y_AXIS
                )
        );

        chartsPanel.setBackground(
                BACKGROUND_COLOR
        );

        chartsPanel.add(
                revenueOverTimeChart
        );

        chartsPanel.add(
                Box.createVerticalStrut(15)
        );

        chartsPanel.add(
                revenueByRegionChart
        );

        /*
         * ============================
         * SCROLLING
         * ============================
         */

        JScrollPane scrollPane =
                new JScrollPane(
                        chartsPanel
                );

        scrollPane.setBorder(null);

        scrollPane.setBackground(
                BACKGROUND_COLOR
        );

        scrollPane.getViewport()
                .setBackground(
                        BACKGROUND_COLOR
                );

        scrollPane.getVerticalScrollBar()
                .setUnitIncrement(16);

        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        add(
                scrollPane,
                BorderLayout.CENTER
        );

        /*
         * Initial load.
         */
        applyFilters();
    }

    /*
     * ============================
     * HEADER
     * ============================
     */

    private JPanel createHeader() {

        JPanel header =
                new JPanel();

        header.setLayout(
                new BoxLayout(
                        header,
                        BoxLayout.Y_AXIS
                )
        );

        header.setBackground(
                BACKGROUND_COLOR
        );

        JLabel title =
                new JLabel(
                        "Sales"
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

        JLabel subtitle =
                new JLabel(
                        "Sales performance and regional revenue"
                );

        subtitle.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        13
                )
        );

        subtitle.setForeground(
                SECONDARY_TEXT
        );

        subtitle.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        header.add(title);

        header.add(
                Box.createVerticalStrut(4)
        );

        header.add(subtitle);

        return header;
    }

    /*
     * ============================
     * BIG SALES FILTER
     * ============================
     */

    private JPanel createFilterBar() {

        JPanel filterBar =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                12,
                                10
                        )
                );

        filterBar.setBackground(
                Color.WHITE
        );

        filterBar.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                BORDER_COLOR
                        ),
                        new EmptyBorder(
                                4,
                                10,
                                4,
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
                                "Monthly",
                                "Weekly"
                        }
                );

        /*
         * Start with yearly so the whole
         * year is initially displayed.
         */
        scopeFilter.setSelectedItem(
                "Yearly"
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

        /*
         * Fill period dropdown.
         */
        updatePeriodFilter();

        /*
         * Whenever scope changes,
         * rebuild period dropdown.
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
                event -> applyFilters()
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

        /*
         * ADD EVERYTHING
         */

        filterBar.add(
                yearLabel
        );

        filterBar.add(
                yearFilter
        );

        filterBar.add(
                scopeLabel
        );

        filterBar.add(
                scopeFilter
        );

        filterBar.add(
                periodLabel
        );

        filterBar.add(
                periodFilter
        );

        filterBar.add(
                applyButton
        );

        filterBar.add(
                resetButton
        );

        return filterBar;
    }

    /*
     * ============================
     * CHANGE PERIOD OPTIONS
     * ============================
     */

    private void updatePeriodFilter() {

        if (scopeFilter == null
                || periodFilter == null) {

            return;
        }

        periodFilter.removeAllItems();

        String scope =
                (String)
                        scopeFilter.getSelectedItem();

        /*
         * YEARLY
         */
        if ("Yearly".equals(scope)) {

            periodFilter.addItem(
                    "Full Year"
            );
        }

        /*
         * QUARTERLY
         */
        else if ("Quarterly".equals(scope)) {

            periodFilter.addItem("Q1");
            periodFilter.addItem("Q2");
            periodFilter.addItem("Q3");
            periodFilter.addItem("Q4");
        }

        /*
         * MONTHLY
         */
        else if ("Monthly".equals(scope)) {

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

        /*
         * WEEKLY
         */
        else if ("Weekly".equals(scope)) {

            periodFilter.addItem("Week 1");
            periodFilter.addItem("Week 2");
            periodFilter.addItem("Week 3");
            periodFilter.addItem("Week 4");
            periodFilter.addItem("Week 5");
        }
    }

    /*
     * ============================
     * APPLY FILTER
     * ============================
     */

    private void applyFilters() {

        Integer year =
                (Integer)
                        yearFilter.getSelectedItem();

        String scope =
                (String)
                        scopeFilter.getSelectedItem();

        String period =
                (String)
                        periodFilter.getSelectedItem();

        if (year == null
                || scope == null
                || period == null) {

            return;
        }

        System.out.println(
                "SALES FILTER"
        );

        System.out.println(
                "Year: " + year
        );

        System.out.println(
                "Scope: " + scope
        );

        System.out.println(
                "Period: " + period
        );

        /*
         * SAME FILTER GOES TO BOTH CHARTS.
         */

        revenueOverTimeChart.applyFilters(
                year,
                scope,
                period
        );

        revenueByRegionChart.applyFilters(
                year,
                scope,
                period
        );
    }

    /*
     * ============================
     * RESET
     * ============================
     */

    private void resetFilters() {

        yearFilter.setSelectedItem(
                2023
        );

        scopeFilter.setSelectedItem(
                "Yearly"
        );

        updatePeriodFilter();

        applyFilters();
    }
}