package dashboard.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public abstract class BaseAnalyticsPage extends JPanel implements FilterableDashboardPage {

    protected static final Color BACKGROUND = new Color(245, 247, 250);
    protected static final Color PRIMARY = new Color(31, 41, 55);
    protected static final Color SECONDARY = new Color(100, 116, 139);

    protected DashboardFilter filter = DashboardFilter.defaults();
    protected final JPanel charts = new JPanel(new GridLayout(0, 2, 15, 15));

    protected BaseAnalyticsPage(String title, String subtitle) {
        setLayout(new BorderLayout(0, 15));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(20, 25, 25, 25));

        JPanel header = new JPanel();
        header.setBackground(BACKGROUND);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(PRIMARY);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(SECONDARY);

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitleLabel);
        add(header, BorderLayout.NORTH);

        charts.setBackground(BACKGROUND);
        JScrollPane scroll = new JScrollPane(charts);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);
    }

    @Override
    public void applyFilter(DashboardFilter filter) {
        this.filter = filter == null ? DashboardFilter.defaults() : filter;
        refreshData();
    }

    protected final void startRefresh() {
        charts.removeAll();
    }

    protected final void finishRefresh() {
        charts.revalidate();
        charts.repaint();
    }

    protected final void showError(Exception ex) {
        charts.add(AnalyticsCharts.messageCard("Unable to load data", ex.getMessage()));
    }

    protected abstract void refreshData();
}
