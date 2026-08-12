package dashboard.gui;

import dashboard.database.ApiClient;
import dashboard.database.SchemaIntrospector;
import dashboard.database.SchemaIntrospector.ComparisonRow;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.NumberFormat;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RevenueByRegionChart extends JPanel {

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

    public RevenueByRegionChart() {

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
                        "Revenue By Region"
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
                        "Total revenue grouped by sales region"
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
                ChartFactory.createBarChart(
                        null,
                        "Region",
                        "Revenue ($)",
                        dataset,
                        PlotOrientation.HORIZONTAL,
                        false,
                        true,
                        false
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

        BarRenderer renderer =
                (BarRenderer)
                        plot.getRenderer();

        renderer.setSeriesPaint(
                0,
                ACTIVE_COLOR
        );

        renderer.setShadowVisible(
                false
        );

        renderer.setMaximumBarWidth(
                0.10
        );

        NumberAxis revenueAxis =
                (NumberAxis)
                        plot.getRangeAxis();

        revenueAxis.setNumberFormatOverride(
                NumberFormat.getCurrencyInstance(
                        Locale.US
                )
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
                        "Waiting for regional sales data..."
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
     * Receives the same Sales filter.
     *
     * Current backend limitation:
     * /api/query/compare can group by region,
     * but cannot simultaneously filter by order_date.
     *
     * Therefore the values below are REAL database
     * regional totals, but not yet time-filtered.
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
                                    "region",

                                    "aggFn",
                                    "SUM"
                            )
                    );

            List<ComparisonRow> rows =
                    SchemaIntrospector
                            .parseCompareRows(json);

            dataset.clear();

            for (ComparisonRow row : rows) {

                dataset.addValue(
                        row.value,
                        "Revenue",
                        row.label
                );
            }

            statusLabel.setText(
                    "Live regional sales totals"
                            + " • Time filtering requires backend support"
            );

        } catch (Exception e) {

            e.printStackTrace();

            dataset.clear();

            statusLabel.setText(
                    "Unable to load regional revenue"
            );
        }
    }
}