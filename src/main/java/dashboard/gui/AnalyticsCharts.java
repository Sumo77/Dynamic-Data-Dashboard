package dashboard.gui;

import dashboard.database.AnalyticsApi.Point;
import dashboard.database.AnalyticsApi.SeriesPoint;
import dashboard.database.AnalyticsApi.XYPoint;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class AnalyticsCharts {

    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color GRID = new Color(226, 232, 240);

    private AnalyticsCharts() {}

    public static JPanel line(String title, String xLabel, String yLabel,
                              List<Point> values, String series,
                              Consumer<String> categoryClick) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Point p : values) dataset.addValue(p.value(), series, p.label());

        JFreeChart chart = ChartFactory.createLineChart(
                title, xLabel, yLabel, dataset,
                PlotOrientation.VERTICAL, false, true, false
        );
        styleCategory(chart);
        return wrap(chart, categoryClick);
    }

    public static JPanel bar(String title, String xLabel, String yLabel,
                             List<Point> values, String series,
                             boolean horizontal,
                             Consumer<String> categoryClick) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Point p : values) dataset.addValue(p.value(), series, p.label());

        JFreeChart chart = ChartFactory.createBarChart(
                title, xLabel, yLabel, dataset,
                horizontal ? PlotOrientation.HORIZONTAL : PlotOrientation.VERTICAL,
                false, true, false
        );
        styleCategory(chart);
        return wrap(chart, categoryClick);
    }

    public static JPanel groupedBar(String title, String xLabel, String yLabel,
                                    List<SeriesPoint> values,
                                    Consumer<String> categoryClick) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (SeriesPoint p : values) dataset.addValue(p.value(), p.series(), p.label());

        JFreeChart chart = ChartFactory.createBarChart(
                title, xLabel, yLabel, dataset,
                PlotOrientation.VERTICAL, true, true, false
        );
        styleCategory(chart);
        return wrap(chart, categoryClick);
    }

    public static JPanel multiLine(String title, String xLabel, String yLabel,
                                   List<SeriesPoint> values,
                                   Consumer<String> categoryClick) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (SeriesPoint p : values) dataset.addValue(p.value(), p.series(), p.label());

        JFreeChart chart = ChartFactory.createLineChart(
                title, xLabel, yLabel, dataset,
                PlotOrientation.VERTICAL, true, true, false
        );
        styleCategory(chart);
        return wrap(chart, categoryClick);
    }

    public static JPanel pie(String title, List<Point> values) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        for (Point p : values) dataset.setValue(p.label(), p.value());
        JFreeChart chart = ChartFactory.createPieChart(title, dataset, true, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        return wrap(chart, null);
    }

    public static JPanel scatter(String title, String xLabel, String yLabel,
                                 List<XYPoint> values) {
        Map<String, XYSeries> byCategory = new LinkedHashMap<>();
        for (XYPoint p : values) {
            byCategory.computeIfAbsent(p.category(), XYSeries::new).add(p.x(), p.y());
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        for (XYSeries series : byCategory.values()) dataset.addSeries(series);

        JFreeChart chart = ChartFactory.createScatterPlot(
                title, xLabel, yLabel, dataset,
                PlotOrientation.VERTICAL, true, true, false
        );
        chart.setBackgroundPaint(Color.WHITE);
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(GRID);
        plot.setRangeGridlinePaint(GRID);
        return wrap(chart, null);
    }

    public static JPanel messageCard(String title, String message) {
        JPanel panel = baseCard();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("SansSerif", Font.BOLD, 17));
        JLabel body = new JLabel("<html>" + message + "</html>");
        body.setFont(new Font("SansSerif", Font.PLAIN, 13));
        panel.add(heading);
        panel.add(Box.createVerticalStrut(10));
        panel.add(body);
        return panel;
    }

    private static JPanel wrap(JFreeChart chart, Consumer<String> categoryClick) {
        JPanel card = baseCard();
        card.setLayout(new BorderLayout());
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(null);
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setMouseWheelEnabled(false);

        if (categoryClick != null) {
            chartPanel.addChartMouseListener(new ChartMouseListener() {
                @Override public void chartMouseMoved(ChartMouseEvent event) {}
                @Override public void chartMouseClicked(ChartMouseEvent event) {
                    if (event.getEntity() instanceof CategoryItemEntity entity) {
                        categoryClick.accept(entity.getColumnKey().toString());
                    }
                }
            });
        }

        card.add(chartPanel, BorderLayout.CENTER);
        return card;
    }

    private static JPanel baseCard() {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(12, 12, 12, 12)
        ));
        return card;
    }

    private static void styleCategory(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(GRID);
        plot.setDomainGridlinePaint(GRID);
    }
}
