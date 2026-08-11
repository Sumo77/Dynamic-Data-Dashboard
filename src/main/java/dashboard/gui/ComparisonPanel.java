package dashboard.gui;

import dashboard.database.ApiClient;
import dashboard.database.SchemaIntrospector;
import dashboard.database.SchemaIntrospector.ColumnRef;
import dashboard.database.SchemaIntrospector.SemanticType;
import dashboard.database.SchemaIntrospector.TableMeta;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;


// Reference implementation of the "pick dataset A, pick dataset B, run the
// comparison" flow. This is the pattern to copy for any other panel that
// needs a schema-aware picker - the three steps below are the whole system:
//
//   1. POPULATE a dropdown from SchemaIntrospector.columnsOfType(schema, TYPE)
//   2. VALIDATE the selected pair with SchemaIntrospector.canCompare(a, b)
//      before doing anything else with it
//   3. QUERY the server, passing table/column names as request params -
//      the server re-validates them against the real schema before
//      building SQL, so a stale/mismatched schema on the client can't
//      produce a bad query

public class ComparisonPanel extends JPanel {

    private final JComboBox<ColumnRef> measureDropdown = new JComboBox<>();
    private final JComboBox<ColumnRef> groupByDropdown = new JComboBox<>();
    private final JLabel statusLabel = new JLabel(" ");
    private final JTextArea resultArea = new JTextArea(15, 60);

    public ComparisonPanel(Map<String, TableMeta> schema) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Step 1: populate each dropdown from the classified schema
        // "This dropdown contains every MEASURE column" is exactly this one call:
        List<ColumnRef> measures = SchemaIntrospector.columnsOfType(schema, SemanticType.MEASURE);
        for (ColumnRef ref : measures) {
            measureDropdown.addItem(ref);
        }

        // "This dropdown contains every DIMENSION or DATE column" - same call, different type:
        List<ColumnRef> groupables = SchemaIntrospector.columnsOfType(schema, SemanticType.DIMENSION);
        groupables.addAll(SchemaIntrospector.columnsOfType(schema, SemanticType.DATE));
        for (ColumnRef ref : groupables) {
            groupByDropdown.addItem(ref);
        }

        JButton runButton = new JButton("Compare");
        runButton.addActionListener(e -> runComparison());

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(new JLabel("Measure:"));
        controls.add(measureDropdown);
        controls.add(new JLabel("Group by:"));
        controls.add(groupByDropdown);
        controls.add(runButton);

        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        add(controls, BorderLayout.NORTH);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    // Fires when the user selects a pair and hits Compare. This method is
    // the full "dataset A against dataset B" syntax - copy this shape for
    // any new picker.
    
    private void runComparison() {
        ColumnRef measure = (ColumnRef) measureDropdown.getSelectedItem();
        ColumnRef groupBy = (ColumnRef) groupByDropdown.getSelectedItem();

        if (measure == null || groupBy == null) {
            statusLabel.setText("Pick both a measure and a group-by column first.");
            return;
        }

        // Step 2: validate the pairing BEFORE building any query
        if (!SchemaIntrospector.canCompare(measure.column, groupBy.column)) {
            statusLabel.setText("Not comparable: " + measure + " vs " + groupBy);
            resultArea.setText("");
            return;
        }

        // This endpoint only handles same-table grouping. A cross-table
        // pairing (e.g. sales.revenue vs customers.country) needs a JOIN
        // through a foreign key first - flagged rather than silently
        // sent as a query that would fail or return nonsense.
        if (!measure.table.equals(groupBy.table)) {
            statusLabel.setText("Cross-table comparison (" + measure.table + " vs " + groupBy.table
                    + ") needs a join - not supported by this panel yet.");
            resultArea.setText("");
            return;
        }

        // Step 3: query, passing table/column names as params
        try {
            String json = ApiClient.getData("api/query/compare", Map.of(
                    "table", measure.table,
                    "measureColumn", measure.column.name,
                    "groupColumn", groupBy.column.name,
                    "aggFn", "SUM"
            ));
            resultArea.setText(json);
            statusLabel.setText("SUM(" + measure + ") grouped by " + groupBy);
        } catch (Exception ex) {
            statusLabel.setText("Query failed: " + ex.getMessage());
            resultArea.setText("");
        }
    }
}