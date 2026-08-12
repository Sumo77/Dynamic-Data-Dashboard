package dashboard.gui;

import dashboard.database.ApiClient;
import dashboard.database.SchemaIntrospector;
import dashboard.database.SchemaIntrospector.ComparisonRow;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RevenueOverTimeChart extends JPanel {

    private static final Color ACTIVE_COLOR =
            new Color(0, 212, 255);

    private static final Color PRIMARY_TEXT =
            new Color(31, 41, 55);

    private static final Color SECONDARY_TEXT =
            new Color(100, 116, 139);

    private static final Color BORDER_COLOR =
            new Color(226, 232, 240);

    private DefaultCategoryDataset dataset;

    private JLabel statusLabel;

    public RevenueOverTimeChart() {

        configurePanel();
        createChart();
    }

    private void configurePanel() {

        setLayout(
                new BorderLayout(
                        0,
                        8
                )
        );

        setBackground(
                Color.WHITE
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

    private void createChart() {

        JPanel titlePanel =
                new JPanel();

        titlePanel.setLayout(
                new BoxLayout(
                        titlePanel,
                        BoxLayout.Y_AXIS
                )
        );

        titlePanel.setBackground(
                Color.WHITE
        );

        JLabel title =
                new JLabel(
                        "Revenue Over Time"
                );

        title.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        17
                )
        );

        title.setForeground(
                PRIMARY_TEXT
        );

        title.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        JLabel description =
                new JLabel(
                        "Revenue grouped by time from the sales table"
                );

        description.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        12
                )
        );

        description.setForeground(
                SECONDARY_TEXT
        );

        description.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        titlePanel.add(title);

        titlePanel.add(
                Box.createVerticalStrut(2)
        );

        titlePanel.add(description);

        add(
                titlePanel,
                BorderLayout.NORTH
        );

        dataset =
                new DefaultCategoryDataset();

        JFreeChart chart =
                ChartFactory.createLineChart(
                        null,
                        "Period",
                        "Revenue ($)",
                        dataset
                );

        chart.setBackgroundPaint(
                Color.WHITE
        );

        CategoryPlot plot =
                chart.getCategoryPlot();

        plot.setBackgroundPaint(
                Color.WHITE
        );

        plot.setOutlineVisible(
                false
        );

        plot.setRangeGridlinePaint(
                BORDER_COLOR
        );

        /*
         * Line styling.
         */
        LineAndShapeRenderer renderer =
                new LineAndShapeRenderer(
                        true,
                        true
                );

        renderer.setSeriesPaint(
                0,
                ACTIVE_COLOR
        );

        renderer.setSeriesStroke(
                0,
                new BasicStroke(
                        2.0f
                )
        );

        plot.setRenderer(
                renderer
        );

        CategoryAxis domainAxis =
                plot.getDomainAxis();

        domainAxis.setMaximumCategoryLabelLines(
                1
        );

        ChartPanel chartPanel =
                new ChartPanel(
                        chart
                );

        chartPanel.setMouseWheelEnabled(
                false
        );

        chartPanel.setBackground(
                Color.WHITE
        );

        add(
                chartPanel,
                BorderLayout.CENTER
        );

        statusLabel =
                new JLabel(
                        "Waiting for sales data..."
                );

        statusLabel.setForeground(
                SECONDARY_TEXT
        );

        statusLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        12
                )
        );

        add(
                statusLabel,
                BorderLayout.SOUTH
        );
    }

    /*
     * Applies Year + Scope + Period.
     */
    public void applyFilters(
            int selectedYear,
            String scope,
            String period
    ) {

        try {

            String json =
                    ApiClient.getData(
                            "api/query/compare",
                            Map.of(
                                    "table",
                                    "sales",

                                    "measureColumn",
                                    "revenue",

                                    "groupColumn",
                                    "order_date",

                                    "aggFn",
                                    "SUM"
                            )
                    );

            List<ComparisonRow> rows =
                    SchemaIntrospector
                            .parseCompareRows(json);

            Map<String, Double> totals =
                    new LinkedHashMap<>();

            dataset.clear();

            /*
             * ISO week handling.
             */
            WeekFields weekFields =
                    WeekFields.ISO;

            int selectedWeek =
                    -1;

            if ("Weekly".equals(scope)) {

                selectedWeek =
                        Integer.parseInt(
                                period.replace(
                                        "Week ",
                                        ""
                                )
                        );
            }

            for (ComparisonRow row : rows) {

                LocalDate date;

                try {
                    date =
                            LocalDate.parse(
                                    row.label
                            );

                } catch (Exception dateError) {
                    continue;
                }

                /*
                 * YEAR
                 */
                if (
                        date.getYear()
                        != selectedYear
                ) {
                    continue;
                }

                /*
                 * ===================
                 * YEARLY
                 * ===================
                 */
                if ("Yearly".equals(scope)) {

                    String label =
                            date.getMonth()
                                    .getDisplayName(
                                            TextStyle.SHORT,
                                            Locale.ENGLISH
                                    );

                    totals.put(
                            label,
                            totals.getOrDefault(
                                    label,
                                    0.0
                            ) + row.value
                    );
                }

                /*
                 * ===================
                 * QUARTERLY
                 * ===================
                 */
                else if (
                        "Quarterly".equals(scope)
                ) {

                    int requestedQuarter =
                            Integer.parseInt(
                                    period.substring(1)
                            );

                    int dateQuarter =
                            (
                                    (
                                            date.getMonthValue()
                                                    - 1
                                    )
                                            / 3
                            ) + 1;

                    if (
                            dateQuarter
                            != requestedQuarter
                    ) {
                        continue;
                    }

                    String label =
                            date.getMonth()
                                    .getDisplayName(
                                            TextStyle.SHORT,
                                            Locale.ENGLISH
                                    );

                    totals.put(
                            label,
                            totals.getOrDefault(
                                    label,
                                    0.0
                            ) + row.value
                    );
                }

                /*
                 * ===================
                 * MONTHLY
                 * ===================
                 */
                else if (
                        "Monthly".equals(scope)
                ) {

                    String fullMonth =
                            date.getMonth()
                                    .getDisplayName(
                                            TextStyle.FULL,
                                            Locale.ENGLISH
                                    );

                    if (
                            !fullMonth.equals(period)
                    ) {
                        continue;
                    }

                    /*
                     * Show each day of the month.
                     */
                    String label =
                            String.valueOf(
                                    date.getDayOfMonth()
                            );

                    totals.put(
                            label,
                            totals.getOrDefault(
                                    label,
                                    0.0
                            ) + row.value
                    );
                }

                /*
                 * ===================
                 * WEEKLY
                 * ===================
                 */
                else if (
                        "Weekly".equals(scope)
                ) {

                    int rowWeek =
                            date.get(
                                    weekFields.weekOfWeekBasedYear()
                            );

                    int weekYear =
                            date.get(
                                    weekFields.weekBasedYear()
                            );

                    /*
                     * Keep the ISO week in the
                     * selected week-based year.
                     */
                    if (
                            rowWeek != selectedWeek
                                    || weekYear
                                    != selectedYear
                    ) {

                        continue;
                    }

                    String label =
                            date.getDayOfWeek()
                                    .getDisplayName(
                                            TextStyle.SHORT,
                                            Locale.ENGLISH
                                    );

                    totals.put(
                            label,
                            totals.getOrDefault(
                                    label,
                                    0.0
                            ) + row.value
                    );
                }
            }

            /*
             * Put filtered database values
             * into the JFreeChart dataset.
             */
            for (
                    Map.Entry<String, Double> entry
                    : totals.entrySet()
            ) {

                dataset.addValue(
                        entry.getValue(),
                        "Revenue",
                        entry.getKey()
                );
            }

            if (totals.isEmpty()) {

                statusLabel.setText(
                        "No sales data found for "
                                + selectedYear
                                + " • "
                                + scope
                                + " • "
                                + period
                );

            } else {

                statusLabel.setText(
                        selectedYear
                                + " • "
                                + scope
                                + " • "
                                + period
                                + " • Live sales data"
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            dataset.clear();

            statusLabel.setText(
                    "Unable to load revenue data"
            );
        }
    }
}