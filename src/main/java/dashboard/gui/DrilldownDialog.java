package dashboard.gui;

import dashboard.database.AnalyticsApi;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public final class DrilldownDialog {

    private DrilldownDialog() {}

    public static void showSales(Component parent, String month, String category, String region) {
        try {
            AnalyticsApi.TableData data = AnalyticsApi.drilldownSales(month, category, region);
            DefaultTableModel model = new DefaultTableModel(data.columns(), 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            for (Object[] row : data.rows()) model.addRow(row);

            JTable table = new JTable(model);
            table.setAutoCreateRowSorter(true);
            table.setFillsViewportHeight(true);

            JScrollPane scroll = new JScrollPane(table);
            scroll.setPreferredSize(new Dimension(950, 520));

            String title = "Sales drill-down";
            if (month != null) title += " - " + month;
            if (category != null) title += " - " + category;

            JOptionPane.showMessageDialog(parent, scroll, title, JOptionPane.PLAIN_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent,
                    "Unable to load drill-down data:\n" + ex.getMessage(),
                    "Drill-down error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
