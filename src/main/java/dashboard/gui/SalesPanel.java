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
    private JComboBox<String> monthFilter;
    private JComboBox<String> periodFilter;

    private JLabel monthLabel;

    private RevenueOverTimeChart revenueOverTimeChart;
    private RevenueByRegionChart revenueByRegionChart;
    private ProfitMarginOverTimeChart profitMarginOverTimeChart;
    private QuantityRevenueScatterChart quantityRevenueScatterChart;

    public SalesPanel() {
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
                        20,
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
                                12
                        )
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
         * CREATE CHARTS
         */
        revenueOverTimeChart =
                new RevenueOverTimeChart();

        revenueByRegionChart =
                new RevenueByRegionChart();

        profitMarginOverTimeChart =
                new ProfitMarginOverTimeChart();

        quantityRevenueScatterChart =
                new QuantityRevenueScatterChart();

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

        profitMarginOverTimeChart.setPreferredSize(
                new Dimension(
                        900,
                        380
                )
        );

        profitMarginOverTimeChart.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        380
                )
        );

        quantityRevenueScatterChart.setPreferredSize(
                new Dimension(
                        900,
                        380
                )
        );

        quantityRevenueScatterChart.setMaximumSize(
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

        profitMarginOverTimeChart.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        quantityRevenueScatterChart.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        /*
         * STACK CHARTS VERTICALLY
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
                Box.createVerticalStrut(
                        15
                )
        );

        chartsPanel.add(
                revenueByRegionChart
        );

        chartsPanel.add(
                Box.createVerticalStrut(
                        15
                )
        );

        chartsPanel.add(
                profitMarginOverTimeChart
        );

        chartsPanel.add(
                Box.createVerticalStrut(
                        15
                )
        );

        chartsPanel.add(
                quantityRevenueScatterChart
        );

        /*
         * SCROLLING
         */
        JScrollPane scrollPane =
                new JScrollPane(
                        chartsPanel
                );

        scrollPane.setBorder(
                null
        );

        scrollPane.setBackground(
                BACKGROUND_COLOR
        );

        scrollPane.getViewport()
                .setBackground(
                        BACKGROUND_COLOR
                );

        scrollPane.getVerticalScrollBar()
                .setUnitIncrement(
                        16
                );

        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        add(
                scrollPane,
                BorderLayout.CENTER
        );

        /*
         * INITIAL LOAD
         */
        applyFilters();
    }

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

        header.add(
                title
        );

        header.add(
                Box.createVerticalStrut(
                        4
                )
        );

        header.add(
                subtitle
        );

        return header;
    }

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

        scopeFilter.setSelectedItem(
                "Yearly"
        );

        /*
         * MONTH
         * Only visible when Weekly is selected.
         */
        monthLabel =
                new JLabel(
                        "Month:"
                );

        monthLabel.setForeground(
                PRIMARY_TEXT
        );

        monthFilter =
                new JComboBox<>(
                        new String[]{
                                "January",
                                "February",
                                "March",
                                "April",
                                "May",
                                "June",
                                "July",
                                "August",
                                "September",
                                "October",
                                "November",
                                "December"
                        }
                );

        monthLabel.setVisible(
                false
        );

        monthFilter.setVisible(
                false
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

        updateFilterOptions();

        /*
         * Change period options when scope changes.
         */
        scopeFilter.addActionListener(
                event -> updateFilterOptions()
        );

        /*
         * Rebuild weekly options if month changes.
         */
        monthFilter.addActionListener(
                event -> updateWeeklyPeriods()
        );

        /*
         * APPLY
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
         * REFRESH
         */
        JButton refreshButton =
                new JButton(
                        "Refresh"
                );

        refreshButton.setFocusPainted(
                false
        );

        refreshButton.addActionListener(
                event -> applyFilters()
        );

        /*
         * RESET
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
                monthLabel
        );

        filterBar.add(
                monthFilter
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
                refreshButton
        );

        filterBar.add(
                resetButton
        );

        return filterBar;
    }

    /*
     * Updates dropdowns depending on Scope.
     */
    private void updateFilterOptions() {

        if (
                scopeFilter == null
                        || periodFilter == null
        ) {
            return;
        }

        String scope =
                (String)
                        scopeFilter.getSelectedItem();

        boolean weekly =
                "Weekly".equals(scope);

        /*
         * Only show Month selector
         * when Weekly is selected.
         */
        monthLabel.setVisible(
                weekly
        );

        monthFilter.setVisible(
                weekly
        );

        periodFilter.removeAllItems();

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
        }

        /*
         * MONTHLY
         */
        else if ("Monthly".equals(scope)) {

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

        /*
         * WEEKLY
         */
        else if ("Weekly".equals(scope)) {

            updateWeeklyPeriods();
        }

        revalidate();
        repaint();
    }

    /*
     * Weekly periods are inside the selected month.
     *
     * Week 1 = days 1-7
     * Week 2 = days 8-14
     * Week 3 = days 15-21
     * Week 4 = days 22-28
     * Week 5 = days 29-end
     */
    private void updateWeeklyPeriods() {

        if (
                periodFilter == null
                        || monthFilter == null
                        || scopeFilter == null
        ) {
            return;
        }

        String scope =
                (String)
                        scopeFilter.getSelectedItem();

        if (!"Weekly".equals(scope)) {
            return;
        }

        periodFilter.removeAllItems();

        periodFilter.addItem(
                "Week 1"
        );

        periodFilter.addItem(
                "Week 2"
        );

        periodFilter.addItem(
                "Week 3"
        );

        periodFilter.addItem(
                "Week 4"
        );

        periodFilter.addItem(
                "Week 5"
        );
    }

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

        String selectedMonth =
                null;

        if ("Weekly".equals(scope)) {

            selectedMonth =
                    (String)
                            monthFilter.getSelectedItem();
        }

        if (
                year == null
                        || scope == null
                        || period == null
        ) {
            return;
        }

        System.out.println(
                "SALES FILTER"
        );

        System.out.println(
                "Year: "
                        + year
        );

        System.out.println(
                "Scope: "
                        + scope
        );

        if (selectedMonth != null) {

            System.out.println(
                    "Month: "
                            + selectedMonth
            );
        }

        System.out.println(
                "Period: "
                        + period
        );

        revenueOverTimeChart.applyFilters(
                year,
                scope,
                selectedMonth,
                period
        );

        revenueByRegionChart.applyFilters(
                year,
                scope,
                selectedMonth,
                period
        );

        profitMarginOverTimeChart.applyFilters(
                year,
                scope,
                selectedMonth,
                period
        );

        quantityRevenueScatterChart.applyFilters(
                year,
                scope,
                selectedMonth,
                period
        );
    }

    private void resetFilters() {

        yearFilter.setSelectedItem(
                2023
        );

        scopeFilter.setSelectedItem(
                "Yearly"
        );

        monthFilter.setSelectedItem(
                "January"
        );

        updateFilterOptions();

        applyFilters();
    }
}