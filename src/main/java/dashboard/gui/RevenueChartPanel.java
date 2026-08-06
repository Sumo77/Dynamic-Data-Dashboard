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

/*
 * This class creates and controls the interactive revenue bar chart.
 */
public class RevenueChartPanel extends JPanel {

    private static final Color ACTIVE_COLOR =
            new Color(0, 212, 255);

    private static final Color PRIMARY_TEXT =
            new Color(31, 41, 55);

    private static final Color SECONDARY_TEXT =
            new Color(100, 116, 139);

    private static final Color BORDER_COLOR =
            new Color(226, 232, 240);

    private DefaultCategoryDataset revenueDataset;

    private JComboBox<String> scopeFilter;
    private JComboBox<Integer> yearFilter;

    private JLabel statusLabel;

    /*
     * This constructor creates the chart, filters and initial sample dataset.
     */
    public RevenueChartPanel() {
        configurePanel();
        createChartLayout();
    }

    /*
     * This method configures the chart card.
     */
    private void configurePanel() {
        setLayout(
                new BorderLayout(0, 10)
        );

        setBackground(Color.WHITE);

        setPreferredSize(
                new Dimension(600, 350)
        );

        setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                BORDER_COLOR
                        ),
                        new EmptyBorder(
                                15,
                                16,
                                12,
                                16
                        )
                )
        );
    }

    /*
     * This method creates the chart controls, JFreeChart and status label.
     */
    private void createChartLayout() {
        add(
                createHeaderAndFilters(),
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

        styleChart(revenueChart);

        ChartPanel chartPanel =
                new ChartPanel(revenueChart);

        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(null);
        chartPanel.setMouseWheelEnabled(false);

        add(
                chartPanel,
                BorderLayout.CENTER
        );

        statusLabel =
                new JLabel(
                        "Displaying monthly sample data"
                );

        statusLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        12
                )
        );

        statusLabel.setForeground(SECONDARY_TEXT);

        add(
                statusLabel,
                BorderLayout.SOUTH
        );

        refreshChart();
    }

    /*
     * This method creates the chart title and dropdown filters.
     */
    private JPanel createHeaderAndFilters() {
        JPanel header =
                new JPanel(new BorderLayout(10, 6));

        header.setBackground(Color.WHITE);

        JPanel titleArea = new JPanel();

        titleArea.setLayout(
                new BoxLayout(
                        titleArea,
                        BoxLayout.Y_AXIS
                )
        );

        titleArea.setBackground(Color.WHITE);

        JLabel chartTitle =
                new JLabel("Revenue Trend");

        chartTitle.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        17
                )
        );

        chartTitle.setForeground(PRIMARY_TEXT);

        chartTitle.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        JLabel description =
                new JLabel(
                        "Revenue by selected time period"
                );

        description.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        12
                )
        );

        description.setForeground(SECONDARY_TEXT);

        description.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        titleArea.add(chartTitle);
        titleArea.add(
                Box.createVerticalStrut(2)
        );
        titleArea.add(description);

        JPanel filters =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                7,
                                0
                        )
                );

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
                refreshChart()
        );

        yearFilter.addActionListener(event ->
                refreshChart()
        );

        filters.add(scopeLabel);
        filters.add(scopeFilter);
        filters.add(yearLabel);
        filters.add(yearFilter);

        header.add(
                titleArea,
                BorderLayout.NORTH
        );

        header.add(
                filters,
                BorderLayout.SOUTH
        );

        return header;
    }

    /*
     * This method refreshes the chart using the currently selected filters.
     */
    public void refreshChart() {
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
         * Cooper can replace this sample method with a backend service call.
         */
        loadSampleRevenueData(
                selectedScope,
                selectedYear
        );

        if (statusLabel != null) {
            statusLabel.setText(
                    "Displaying "
                            + selectedScope.toLowerCase()
                            + " data for "
                            + selectedYear
            );
        }
    }

    /*
     * This method contains all temporary chart data in one place.
     */
    private void loadSampleRevenueData(
            String selectedScope,
            int selectedYear
    ) {
        revenueDataset.clear();

        switch (selectedScope) {
            case "Weekly" -> {
                addValue(12_000, "Week 1");
                addValue(15_750, "Week 2");
                addValue(14_100, "Week 3");
                addValue(18_900, "Week 4");
            }

            case "Monthly" -> {
                String[] months = {
                        "Jan", "Feb", "Mar", "Apr",
                        "May", "Jun", "Jul", "Aug",
                        "Sep", "Oct", "Nov", "Dec"
                };

                double[] values = {
                        12_000, 16_000, 14_500, 19_000,
                        22_000, 25_000, 23_000, 27_500,
                        26_000, 29_000, 31_000, 34_000
                };

                for (int index = 0;
                     index < months.length;
                     index++) {

                    addValue(
                            values[index],
                            months[index]
                    );
                }
            }

            case "Quarterly" -> {
                addValue(42_500, "Q1");
                addValue(66_000, "Q2");
                addValue(76_500, "Q3");
                addValue(94_000, "Q4");
            }

            case "Yearly" -> {
                addValue(
                        235_000,
                        String.valueOf(selectedYear - 3)
                );

                addValue(
                        278_000,
                        String.valueOf(selectedYear - 2)
                );

                addValue(
                        315_000,
                        String.valueOf(selectedYear - 1)
                );

                addValue(
                        342_000,
                        String.valueOf(selectedYear)
                );
            }

            default -> throw new IllegalArgumentException(
                    "Unknown scope: " + selectedScope
            );
        }
    }

    /*
     * This small helper method keeps repeated dataset code easier to read.
     */
    private void addValue(
            double value,
            String period
    ) {
        revenueDataset.addValue(
                value,
                "Revenue",
                period
        );
    }

    /*
     * This method applies the dashboard styling to the bar chart.
     */
    private void styleChart(
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
}