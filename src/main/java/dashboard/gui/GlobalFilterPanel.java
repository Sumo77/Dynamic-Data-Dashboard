package dashboard.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * Shared dashboard filter bar.
 *
 * The user chooses the time/region once and DashboardFrame keeps that filter
 * when moving between Overview, Sales, Inventory, Products, Marketing and Customers.
 */
public class GlobalFilterPanel extends JPanel {

    private static final Color ACTIVE = new Color(0, 190, 225);
    private static final Color ACTIVE_HOVER = new Color(0, 168, 204);
    private static final Color TEXT = new Color(31, 41, 55);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);

    private final JComboBox<Integer> yearFilter = new JComboBox<>(new Integer[]{2023, 2024});
    private final JComboBox<String> scopeFilter = new JComboBox<>(new String[]{"Yearly", "Quarterly", "Monthly", "Weekly"});
    private final JComboBox<String> monthFilter = new JComboBox<>(months());
    private final JComboBox<String> periodFilter = new JComboBox<>();
    private final JComboBox<String> regionFilter = new JComboBox<>(new String[]{
            "All Regions", "Auckland", "Christchurch", "Melbourne", "Sydney", "Wellington"
    });

    private final JLabel monthLabel = label("Month");
    private final JLabel statusLabel = new JLabel(" ");
    private final Consumer<DashboardFilter> listener;

    public GlobalFilterPanel(Consumer<DashboardFilter> listener) {
        this.listener = listener;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(12, 22, 12, 22)
        ));

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        filters.setOpaque(false);

        filters.add(group("Year", yearFilter));
        filters.add(group("Scope", scopeFilter));

        JPanel monthGroup = new JPanel();
        monthGroup.setOpaque(false);
        monthGroup.setLayout(new BoxLayout(monthGroup, BoxLayout.Y_AXIS));
        monthLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        monthFilter.setAlignmentX(Component.LEFT_ALIGNMENT);
        monthGroup.add(monthLabel);
        monthGroup.add(Box.createVerticalStrut(4));
        monthGroup.add(monthFilter);
        filters.add(monthGroup);

        filters.add(group("Period", periodFilter));
        filters.add(group("Region", regionFilter));

        // Custom rounded button gives Apply Filters a stronger dashboard action style.
        RoundedButton apply = new RoundedButton("Apply Filters", ACTIVE, ACTIVE_HOVER);
        apply.setPreferredSize(new Dimension(140, 38));
        apply.setFont(new Font("SansSerif", Font.BOLD, 12));

        JButton reset = new JButton("Reset");
        reset.setPreferredSize(new Dimension(72, 38));
        reset.setFont(new Font("SansSerif", Font.PLAIN, 12));
        reset.setForeground(MUTED);
        reset.setBackground(Color.WHITE);
        reset.setFocusPainted(false);
        reset.setBorder(BorderFactory.createLineBorder(BORDER));

        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        statusLabel.setForeground(new Color(22, 163, 74));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 18));
        actions.setOpaque(false);
        actions.add(apply);
        actions.add(reset);
        actions.add(statusLabel);
        filters.add(actions);

        add(filters, BorderLayout.CENTER);

        styleCombo(yearFilter);
        styleCombo(scopeFilter);
        styleCombo(monthFilter);
        styleCombo(periodFilter);
        styleCombo(regionFilter);

        scopeFilter.addActionListener(e -> {
            updatePeriodOptions();
            clearAppliedMessage();
        });
        yearFilter.addActionListener(e -> clearAppliedMessage());
        monthFilter.addActionListener(e -> clearAppliedMessage());
        periodFilter.addActionListener(e -> clearAppliedMessage());
        regionFilter.addActionListener(e -> clearAppliedMessage());

        apply.addActionListener(e -> publishFilter());
        reset.addActionListener(e -> resetFilters());

        updatePeriodOptions();
    }

    private JPanel group(String title, JComboBox<?> combo) {
        JPanel group = new JPanel();
        group.setOpaque(false);
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        JLabel groupLabel = label(title);
        groupLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.add(groupLabel);
        group.add(Box.createVerticalStrut(4));
        group.add(combo);
        return group;
    }

    private static JLabel label(String text) {
        JLabel label = new JLabel(text.toUpperCase());
        label.setFont(new Font("SansSerif", Font.BOLD, 10));
        label.setForeground(MUTED);
        return label;
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setPreferredSize(new Dimension(125, 36));
        combo.setMaximumSize(new Dimension(145, 36));
        combo.setBackground(Color.WHITE);
        combo.setForeground(TEXT);
        combo.setFont(new Font("SansSerif", Font.PLAIN, 12));
    }

    /** Changes the Period choices to match the selected scope. */
    private void updatePeriodOptions() {
        String scope = String.valueOf(scopeFilter.getSelectedItem());
        periodFilter.removeAllItems();

        boolean weekly = "Weekly".equals(scope);
        monthLabel.setVisible(weekly);
        monthFilter.setVisible(weekly);

        switch (scope) {
            case "Quarterly" -> {
                periodFilter.addItem("Q1");
                periodFilter.addItem("Q2");
                periodFilter.addItem("Q3");
                periodFilter.addItem("Q4");
            }
            case "Monthly" -> {
                for (String month : months()) periodFilter.addItem(month);
            }
            case "Weekly" -> {
                for (int week = 1; week <= 5; week++) periodFilter.addItem("Week " + week);
            }
            default -> periodFilter.addItem("Full Year");
        }

        revalidate();
        repaint();
    }

    /** Packages the UI selections into one object for the currently visible page. */
    private void publishFilter() {
        String scope = String.valueOf(scopeFilter.getSelectedItem());
        String period = String.valueOf(periodFilter.getSelectedItem());
        String month = "Weekly".equals(scope)
                ? String.valueOf(monthFilter.getSelectedItem())
                : ("Monthly".equals(scope) ? period : "January");

        DashboardFilter filter = new DashboardFilter(
                (Integer) yearFilter.getSelectedItem(),
                scope,
                month,
                period,
                String.valueOf(regionFilter.getSelectedItem())
        );

        statusLabel.setText("✓ Applied");
        if (listener != null) listener.accept(filter);
    }

    private void resetFilters() {
        yearFilter.setSelectedItem(2023);
        scopeFilter.setSelectedItem("Yearly");
        monthFilter.setSelectedItem("January");
        regionFilter.setSelectedItem("All Regions");
        updatePeriodOptions();
        publishFilter();
    }

    private void clearAppliedMessage() {
        statusLabel.setText(" ");
    }

    private static String[] months() {
        return new String[]{
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
    }

    /** Small custom Swing button used only for the primary filter action. */
    private static class RoundedButton extends JButton {
        private Color fill;
        private final Color normal;
        private final Color hover;

        RoundedButton(String text, Color normal, Color hover) {
            super(text);
            this.normal = normal;
            this.hover = hover;
            this.fill = normal;
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { fill = RoundedButton.this.hover; repaint(); }
                @Override public void mouseExited(MouseEvent e) { fill = RoundedButton.this.normal; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
