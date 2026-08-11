package dashboard.gui;

import dashboard.database.ApiClient;
import dashboard.database.SchemaIntrospector;
import dashboard.database.SchemaIntrospector.ColumnRef;
import dashboard.database.SchemaIntrospector.ComparisonRow;
import dashboard.database.SchemaIntrospector.SemanticType;
import dashboard.database.SchemaIntrospector.TableMeta;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Locale;
import java.util.Map;


// This class creates the "Compare Data" card on the Overview page. It lets
// the user pick any two columns the schema has already classified - one
// measure, one dimension or date - and runs the resulting SUM(...) GROUP BY
// query against the live server, the same way the KPI cards and revenue chart do.
public class DataComparisonPanel extends JPanel {

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

    private static final Color ERROR_TEXT =
            new Color(190, 60, 60);

    private final JComboBox<ColumnRef> columnADropdown = new JComboBox<>();
    private final JComboBox<ColumnRef> columnBDropdown = new JComboBox<>();

    private JPanel resultsList;
    private JLabel statusLabel;

    // The schema is already classified by the time this panel is built -
    // SchemaIntrospector.introspect() runs once in DashboardFrame before
    // any page is drawn, so every dropdown here is populated instantly.
    public DataComparisonPanel(Map<String, TableMeta> schema) {
        configurePanel();
        createLayout(schema);
    }

    private void configurePanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.WHITE);

        setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR),
                        new EmptyBorder(15, 16, 12, 16)
                )
        );
    }

    private void createLayout(Map<String, TableMeta> schema) {
        add(createHeader(), BorderLayout.NORTH);
        add(createPickerRow(schema), BorderLayout.CENTER);

        statusLabel = new JLabel("Pick two columns and press Compare.");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(SECONDARY_TEXT);

        add(statusLabel, BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel titleArea = new JPanel();
        titleArea.setLayout(new BoxLayout(titleArea, BoxLayout.Y_AXIS));
        titleArea.setBackground(Color.WHITE);

        JLabel title = new JLabel("Compare Data");
        title.setFont(new Font("SansSerif", Font.BOLD, 17));
        title.setForeground(PRIMARY_TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Pick any two columns the dashboard already understands");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(SECONDARY_TEXT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleArea.add(title);
        titleArea.add(Box.createVerticalStrut(2));
        titleArea.add(subtitle);

        return titleArea;
    }

    // Column A is restricted to MEASURE columns (the things worth summing),
    // Column B to DIMENSION or DATE columns (the things worth grouping by).
    // This mirrors SchemaIntrospector.canCompare - the dropdowns can't even
    // offer a pairing that wouldn't be relatable in the first place.
    private JPanel createPickerRow(Map<String, TableMeta> schema) {
        List<ColumnRef> measures =
                SchemaIntrospector.columnsOfType(schema, SemanticType.MEASURE);

        List<ColumnRef> groupables =
                SchemaIntrospector.columnsOfType(schema, SemanticType.DIMENSION);
        groupables.addAll(
                SchemaIntrospector.columnsOfType(schema, SemanticType.DATE)
        );

        for (ColumnRef ref : measures) columnADropdown.addItem(ref);
        for (ColumnRef ref : groupables) columnBDropdown.addItem(ref);

        styleDropdown(columnADropdown);
        styleDropdown(columnBDropdown);

        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setBackground(Color.WHITE);
        row.add(Box.createVerticalStrut(8));

        JPanel pickers = new JPanel(new GridLayout(2, 1, 0, 6));
        pickers.setBackground(Color.WHITE);
        pickers.add(labeledField("Column A (measure)", columnADropdown));
        pickers.add(labeledField("Column B (group by)", columnBDropdown));

        JButton compareButton = new JButton("Compare");
        compareButton.setFocusPainted(false);
        compareButton.setBorderPainted(false);
        compareButton.setOpaque(true);
        compareButton.setBackground(ACTIVE_COLOR);
        compareButton.setForeground(SIDEBAR_COLOUR);
        compareButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        compareButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        compareButton.addActionListener(event -> runComparison());

        resultsList = new JPanel();
        resultsList.setLayout(new BoxLayout(resultsList, BoxLayout.Y_AXIS));
        resultsList.setBackground(Color.WHITE);

        JScrollPane resultsScroll = new JScrollPane(resultsList);
        resultsScroll.setBorder(null);
        resultsScroll.setBackground(Color.WHITE);
        resultsScroll.getViewport().setBackground(Color.WHITE);

        row.add(pickers);
        row.add(Box.createVerticalStrut(8));
        row.add(compareButton);
        row.add(Box.createVerticalStrut(8));
        row.add(resultsScroll);

        return row;
    }

    private JPanel labeledField(String labelText, JComboBox<ColumnRef> dropdown) {
        JPanel field = new JPanel(new BorderLayout(0, 2));
        field.setBackground(Color.WHITE);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.PLAIN, 11));
        label.setForeground(SECONDARY_TEXT);

        field.add(label, BorderLayout.NORTH);
        field.add(dropdown, BorderLayout.CENTER);
        return field;
    }

    private void styleDropdown(JComboBox<ColumnRef> dropdown) {
        dropdown.setFont(new Font("SansSerif", Font.PLAIN, 12));
        dropdown.setBackground(Color.WHITE);
    }

    // This is the whole "pick A, pick B, run the query" flow:
    //   1. read the two selections
    //   2. validate the pairing with SchemaIntrospector.canCompare
    //   3. ask the server to run SUM(A) GROUP BY B
    //   4. render the rows
    private void runComparison() {
        ColumnRef columnA = (ColumnRef) columnADropdown.getSelectedItem();
        ColumnRef columnB = (ColumnRef) columnBDropdown.getSelectedItem();

        resultsList.removeAll();

        if (columnA == null || columnB == null) {
            showStatus("Pick a value for both Column A and Column B.", true);
            return;
        }

        if (!SchemaIntrospector.canCompare(columnA.column, columnB.column)) {
            showStatus("Those two columns aren't relatable: " + columnA + " vs " + columnB, true);
            return;
        }

        if (!columnA.table.equals(columnB.table)) {
            showStatus(
                    "\"" + columnA.table + "\" and \"" + columnB.table
                            + "\" are different tables - that needs a join through a "
                            + "foreign key, which isn't wired up yet.",
                    true
            );
            return;
        }

        try {
            String json = ApiClient.getData("api/query/compare", Map.of(
                    "table", columnA.table,
                    "measureColumn", columnA.column.name,
                    "groupColumn", columnB.column.name,
                    "aggFn", "SUM"
            ));

            List<ComparisonRow> rows = SchemaIntrospector.parseCompareRows(json);

            if (rows.isEmpty()) {
                showStatus("Query ran fine but returned no rows.", false);
            } else {
                for (ComparisonRow row : rows) {
                    resultsList.add(createResultRow(row));
                }
                showStatus(
                        "SUM(" + columnA + ") grouped by " + columnB
                                + "  -  " + rows.size() + " rows",
                        false
                );
            }
        } catch (Exception ex) {
            showStatus("Query failed: " + ex.getMessage(), true);
        }

        resultsList.revalidate();
        resultsList.repaint();
    }

    private JPanel createResultRow(ComparisonRow row) {
        JPanel line = new JPanel(new BorderLayout());
        line.setBackground(Color.WHITE);
        line.setBorder(new EmptyBorder(3, 0, 3, 0));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        JLabel labelSide = new JLabel(row.label);
        labelSide.setFont(new Font("SansSerif", Font.PLAIN, 13));
        labelSide.setForeground(PRIMARY_TEXT);

        JLabel valueSide = new JLabel(String.format(Locale.US, "%,.2f", row.value));
        valueSide.setFont(new Font("SansSerif", Font.BOLD, 13));
        valueSide.setForeground(PRIMARY_TEXT);
        valueSide.setHorizontalAlignment(SwingConstants.RIGHT);

        line.add(labelSide, BorderLayout.WEST);
        line.add(valueSide, BorderLayout.EAST);
        return line;
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setForeground(isError ? ERROR_TEXT : SECONDARY_TEXT);
    }
}