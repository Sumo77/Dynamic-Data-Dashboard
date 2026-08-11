package dashboard.gui;

import dashboard.database.SchemaIntrospector.TableMeta;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

// This class creates the Overview page. It combines the header, KPI cards,
// revenue chart and space for another dashboard component.
public class OverviewPanel extends JPanel {

    private static final Color BACKGROUND_COLOR =
            new Color(245, 247, 250);

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

    private KpiPanel kpiPanel;
    private RevenueChartPanel revenueChartPanel;
    private final Map<String, TableMeta> schema;

    // This constructor creates all parts of the Overview page. The schema
    // comes from DashboardFrame, which runs SchemaIntrospector.introspect()
    // once at startup before any page is built.
    public OverviewPanel(Map<String, TableMeta> schema) {
        this.schema = schema;
        configurePanel();
        createLayout();
    }

    // This method configures the Overview page layout and spacing.
    private void configurePanel() {
        setLayout(
                new BorderLayout(0, 15)
        );

        setBackground(BACKGROUND_COLOR);

        setBorder(
                new EmptyBorder(
                        5,
                        25,
                        25,
                        25
                )
        );
    }

    // This method combines the header, KPI cards and graph area.
    private void createLayout() {
        JPanel topSection =
                new JPanel(new BorderLayout(0, 15));

        topSection.setBackground(BACKGROUND_COLOR);

        topSection.add(
                createHeader(),
                BorderLayout.NORTH
        );

        kpiPanel = new KpiPanel();

        topSection.add(
                kpiPanel,
                BorderLayout.CENTER
        );

        add(
                topSection,
                BorderLayout.NORTH
        );

        add(
                createGraphArea(),
                BorderLayout.CENTER
        );
    }

    // This method creates the page title, live date and Refresh Dashboard button.
    private JPanel createHeader() {
        JPanel header =
                new JPanel(new BorderLayout());

        header.setBackground(BACKGROUND_COLOR);

        JPanel titleArea = new JPanel();

        titleArea.setLayout(
                new BoxLayout(
                        titleArea,
                        BoxLayout.Y_AXIS
                )
        );

        titleArea.setBackground(BACKGROUND_COLOR);

        JLabel titleLabel =
                new JLabel("Overview");

        titleLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        28
                )
        );

        titleLabel.setForeground(PRIMARY_TEXT);

        titleLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "EEEE, d MMMM yyyy"
                );

        JLabel dateLabel = new JLabel(
                LocalDate.now().format(formatter)
                        + "  |  Live retail overview"
        );

        dateLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        14
                )
        );

        dateLabel.setForeground(SECONDARY_TEXT);

        dateLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        titleArea.add(titleLabel);
        titleArea.add(
                Box.createVerticalStrut(4)
        );
        titleArea.add(dateLabel);

        JButton refreshButton =
                new JButton("Refresh Dashboard");

        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setOpaque(true);

        refreshButton.setBackground(ACTIVE_COLOR);
        refreshButton.setForeground(SIDEBAR_COLOUR);

        refreshButton.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        13
                )
        );

        refreshButton.setPreferredSize(
                new Dimension(150, 38)
        );

        refreshButton.addActionListener(event ->
                refreshDashboard()
        );

        JPanel refreshArea =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                0,
                                5
                        )
                );

        refreshArea.setBackground(BACKGROUND_COLOR);
        refreshArea.add(refreshButton);

        header.add(
                titleArea,
                BorderLayout.WEST
        );

        header.add(
                refreshArea,
                BorderLayout.EAST
        );

        return header;
    }

    // This method creates the graph row. The revenue chart uses two-thirds of
    // the available space and the remaining area can hold another graph later.
    private JPanel createGraphArea() {
        JPanel graphArea =
                new JPanel(new GridBagLayout());

        graphArea.setBackground(BACKGROUND_COLOR);

        GridBagConstraints constraints =
                new GridBagConstraints();

        constraints.fill =
                GridBagConstraints.BOTH;

        constraints.weighty = 1.0;

        revenueChartPanel =
                new RevenueChartPanel();

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0.67;
        constraints.insets =
                new Insets(0, 0, 0, 15);

        graphArea.add(
                revenueChartPanel,
                constraints
        );

        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 0.33;
        constraints.insets =
                new Insets(0, 0, 0, 0);

        graphArea.add(
                new DataComparisonPanel(schema),
                constraints
        );

        return graphArea;
    }

    // This method refreshes the KPI cards and revenue chart together.
    private void refreshDashboard() {
        kpiPanel.refreshSampleValues();
        revenueChartPanel.refreshChart();

        JOptionPane.showMessageDialog(
                this,
                "Dashboard refreshed successfully.",
                "Refresh Complete",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}