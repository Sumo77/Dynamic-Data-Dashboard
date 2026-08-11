package dashboard.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import dashboard.database.ApiClient;
import dashboard.database.SchemaIntrospector;
import dashboard.database.SchemaIntrospector.TableMeta;
import java.util.Map;

// This class creates the main dashboard window. It controls the overall layout
// and switches between the different pages using CardLayout.
public class DashboardFrame extends JFrame {

    private static final Color SIDEBAR_COLOUR =
            new Color(17, 24, 39);

    private static final Color BACKGROUND_COLOR =
            new Color(245, 247, 250);

    private static final Color ACTIVE_COLOR =
            new Color(0, 212, 255);

    private CardLayout contentCardLayout;
    private JPanel contentCardPanel;

    // Populated once at startup, before anything is drawn. Every panel that
    // needs to know "is this column a date, a measure, or a label" reads
    // from here instead of guessing per-panel.
    private Map<String, TableMeta> schema;

    // This constructor configures the window and builds the dashboard layout.
    // Schema introspection runs first and is intentionally blocking: nothing
    // should draw a chart or a field picker before we know what the columns
    // actually mean.
    public DashboardFrame() {
        try {
            schema = SchemaIntrospector.introspect();
            for (TableMeta table : schema.values()) {
                System.out.println(table.name + " -> " + table.columns.values());
            }
        } catch (Exception e) {
            System.err.println("Schema introspection failed, falling back to no schema metadata:");
            e.printStackTrace();
            schema = Map.of();
        }

        try {
            String json = ApiClient.getData("api/kpis/summary", Map.of("year_from", "2024"));
            System.out.println(json); // just proving it works, for now
        } catch (Exception e) {
            e.printStackTrace();
        }
        configureWindow();
        createLayout();
    }

    // This method configures the main dashboard window.
    private void configureWindow() {
        setTitle("Dynamic Retail Dashboard");
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    // This method creates the top logo area, sidebar and centre content section.
    private void createLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        JPanel northPanel = createTopPanel();

        SidebarPanel sidebarPanel =
                new SidebarPanel(this::showPage);

        JPanel centrePanel = createContentArea();

        JPanel southPanel = new JPanel();
        southPanel.setPreferredSize(new Dimension(0, 0));

        JPanel eastPanel = new JPanel();
        eastPanel.setPreferredSize(new Dimension(0, 0));

        mainPanel.add(northPanel, BorderLayout.NORTH);
        mainPanel.add(sidebarPanel, BorderLayout.WEST);
        mainPanel.add(centrePanel, BorderLayout.CENTER);
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        mainPanel.add(eastPanel, BorderLayout.EAST);

        add(mainPanel);
    }

    // This method creates the top section and places the logo on the left side.
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());

        topPanel.setPreferredSize(
                new Dimension(0, 85)
        );

        topPanel.setBackground(BACKGROUND_COLOR);

        JPanel logoPanel = createLogoPanel();

        logoPanel.setPreferredSize(
                new Dimension(210, 85)
        );

        JPanel futureContentArea =
                new JPanel(new BorderLayout());

        futureContentArea.setBackground(BACKGROUND_COLOR);

        topPanel.add(logoPanel, BorderLayout.WEST);
        topPanel.add(futureContentArea, BorderLayout.CENTER);

        return topPanel;
    }

    // This method creates the dashboard logo displayed in the top-left corner.
    private JPanel createLogoPanel() {
        JPanel logoPanel = new JPanel();

        logoPanel.setLayout(
                new BoxLayout(logoPanel, BoxLayout.Y_AXIS)
        );

        logoPanel.setBackground(SIDEBAR_COLOUR);

        logoPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        17,
                        18,
                        15,
                        18
                )
        );

        JLabel firstLine =
                new JLabel("Dynamic Retail");

        firstLine.setForeground(Color.WHITE);

        firstLine.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        18
                )
        );

        firstLine.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        JLabel secondLine =
                new JLabel("Dashboard");

        secondLine.setForeground(ACTIVE_COLOR);

        secondLine.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        18
                )
        );

        secondLine.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        JLabel subtitle =
                new JLabel("Admin Dashboard");

        subtitle.setForeground(
                new Color(170, 180, 195)
        );

        subtitle.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        11
                )
        );

        subtitle.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        logoPanel.add(firstLine);
        logoPanel.add(secondLine);
        logoPanel.add(Box.createVerticalStrut(3));
        logoPanel.add(subtitle);

        return logoPanel;
    }

    // This method creates every dashboard page and stores them in CardLayout.
    private JPanel createContentArea() {
        contentCardLayout = new CardLayout();

        contentCardPanel =
                new JPanel(contentCardLayout);

        contentCardPanel.setBackground(BACKGROUND_COLOR);

        contentCardPanel.add(
                new OverviewPanel(schema),
                "Overview"
        );

        contentCardPanel.add(
                createPlaceholderPage("Sales"),
                "Sales"
        );

        contentCardPanel.add(
                createPlaceholderPage("Inventory"),
                "Inventory"
        );

        contentCardPanel.add(
                createPlaceholderPage("Reports"),
                "Reports"
        );

        contentCardPanel.add(
                createPlaceholderPage("Alerts"),
                "Alerts"
        );

        return contentCardPanel;
    }

    // This method creates temporary pages for sections that have not been developed yet.
    private JPanel createPlaceholderPage(String pageName) {
        JPanel page =
                new JPanel(new GridBagLayout());

        page.setBackground(BACKGROUND_COLOR);

        JLabel label =
                new JLabel(pageName + " Page");

        label.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        28
                )
        );

        label.setForeground(
                new Color(31, 41, 55)
        );

        page.add(label);

        return page;
    }

    // This method displays the page selected from the sidebar.
    private void showPage(String pageName) {
        contentCardLayout.show(
                contentCardPanel,
                pageName
        );

        contentCardPanel.revalidate();
        contentCardPanel.repaint();
    }
}