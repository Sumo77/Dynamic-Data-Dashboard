package dashboard.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/*
 * This class creates the main dashboard window. It sets up the top logo area,
 * sidebar navigation and the different pages shown in the centre.
 */
public class DashboardFrame extends JFrame {

    private static final Color SIDEBAR_COLOUR =
            new Color(17, 24, 39);

    private static final Color BACKGROUND_COLOR =
            new Color(245, 247, 250);

    private static final Color ACTIVE_COLOR =
            new Color(0, 212, 255);

    /*
     * CardLayout allows the centre section to switch between pages.
     */
    private CardLayout contentCardLayout;
    private JPanel contentCardPanel;

    /*
     * These buttons are stored so their active colours can be updated.
     */
    private JButton overviewButton;
    private JButton salesButton;
    private JButton inventoryButton;
    private JButton reportsButton;
    private JButton alertsButton;

    /*
     * This constructor prepares the window and creates the dashboard layout.
     */
    public DashboardFrame() {
        configureWindow();
        createLayout();
    }

    /*
     * This method configures the main dashboard window.
     */
    private void configureWindow() {
        setTitle("Dynamic Retail Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    /*
     * This method creates the main BorderLayout and keeps all five regions.
     */
    private void createLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        /*
         * The NORTH section contains the logo on the left and leaves space
         * for future controls on the right.
         */
        JPanel north = new JPanel(new BorderLayout());
        north.setPreferredSize(new Dimension(0, 85));
        north.setBackground(BACKGROUND_COLOR);

        JPanel logoArea = createLogoArea();
        logoArea.setPreferredSize(new Dimension(210, 85));

        JPanel topContentArea = new JPanel(new BorderLayout());
        topContentArea.setBackground(BACKGROUND_COLOR);

        north.add(logoArea, BorderLayout.WEST);
        north.add(topContentArea, BorderLayout.CENTER);

        /*
         * EAST and SOUTH are kept for future use but currently have no size.
         */
        JPanel east = new JPanel();
        east.setPreferredSize(new Dimension(0, 0));
        east.setBackground(BACKGROUND_COLOR);

        JPanel south = new JPanel();
        south.setPreferredSize(new Dimension(0, 0));
        south.setBackground(BACKGROUND_COLOR);

        JPanel west = createLeftSidebar();
        JPanel center = createContentArea();

        mainPanel.add(north, BorderLayout.NORTH);
        mainPanel.add(south, BorderLayout.SOUTH);
        mainPanel.add(east, BorderLayout.EAST);
        mainPanel.add(west, BorderLayout.WEST);
        mainPanel.add(center, BorderLayout.CENTER);

        add(mainPanel);
    }

    /*
     * This method creates the dashboard logo area.
     */
    private JPanel createLogoArea() {
        JPanel logoArea = new JPanel();
        logoArea.setLayout(new BoxLayout(logoArea, BoxLayout.Y_AXIS));
        logoArea.setBackground(SIDEBAR_COLOUR);
        logoArea.setBorder(new EmptyBorder(17, 18, 15, 18));

        JLabel logoLineOne = new JLabel("Dynamic Retail");
        logoLineOne.setForeground(Color.WHITE);
        logoLineOne.setFont(new Font("SansSerif", Font.BOLD, 18));
        logoLineOne.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel logoLineTwo = new JLabel("Dashboard");
        logoLineTwo.setForeground(ACTIVE_COLOR);
        logoLineTwo.setFont(new Font("SansSerif", Font.BOLD, 18));
        logoLineTwo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Admin Dashboard");
        subtitle.setForeground(new Color(170, 180, 195));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        logoArea.add(logoLineOne);
        logoArea.add(logoLineTwo);
        logoArea.add(Box.createVerticalStrut(3));
        logoArea.add(subtitle);

        return logoArea;
    }
    /*
 * This method creates the row of KPI cards shown at the top of the Overview page.
 * Placeholder values are used for now and can be replaced with backend data later.
 */
private JPanel createKpiPanel() {
    JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 15, 0));
    kpiPanel.setBackground(BACKGROUND_COLOR);

    kpiPanel.add(createKpiCard("Total Revenue", "$0.00"));
    kpiPanel.add(createKpiCard("Total Profit", "$0.00"));
    kpiPanel.add(createKpiCard("Customers", "0"));
    kpiPanel.add(createKpiCard("Products", "0"));

    return kpiPanel;
}

    /*
 * This method creates one reusable KPI card with a title and value. Using one
 * method keeps every card consistent and avoids repeating the same layout code.
 */
private JPanel createKpiCard(String title, String value) {
    JPanel card = new JPanel();
    card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
    card.setBackground(Color.WHITE);

    card.setBorder(
            BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(
                            new Color(226, 232, 240)
                    ),
                    new EmptyBorder(18, 18, 18, 18)
            )
    );

    JLabel titleLabel = new JLabel(title);
    titleLabel.setFont(
            new Font("SansSerif", Font.PLAIN, 13)
    );
    titleLabel.setForeground(
            new Color(100, 116, 139)
    );
    titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel valueLabel = new JLabel(value);
    valueLabel.setFont(
            new Font("SansSerif", Font.BOLD, 24)
    );
    valueLabel.setForeground(
            new Color(31, 41, 55)
    );
    valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    card.add(titleLabel);
    card.add(Box.createVerticalStrut(10));
    card.add(valueLabel);

    return card;
}

    /*
     * This method creates the sidebar containing the navigation buttons.
     */
    private JPanel createLeftSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBackground(SIDEBAR_COLOUR);
        sidebar.setBorder(new EmptyBorder(25, 18, 25, 18));

        sidebar.add(createNavButtons(), BorderLayout.NORTH);

        return sidebar;
    }

    /*
     * This method creates the navigation buttons and gives each button
     * an action listener.
     */
    private JPanel createNavButtons() {
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(SIDEBAR_COLOUR);

        overviewButton = createNavigationButton("Overview", true);
        salesButton = createNavigationButton("Sales", false);
        inventoryButton = createNavigationButton("Inventory", false);
        reportsButton = createNavigationButton("Reports", false);
        alertsButton = createNavigationButton("Alerts", false);

        overviewButton.addActionListener(event ->
                showPage("Overview", overviewButton)
        );

        salesButton.addActionListener(event ->
                showPage("Sales", salesButton)
        );

        inventoryButton.addActionListener(event ->
                showPage("Inventory", inventoryButton)
        );

        reportsButton.addActionListener(event ->
                showPage("Reports", reportsButton)
        );

        alertsButton.addActionListener(event ->
                showPage("Alerts", alertsButton)
        );

        nav.add(overviewButton);
        nav.add(Box.createVerticalStrut(8));

        nav.add(salesButton);
        nav.add(Box.createVerticalStrut(8));

        nav.add(inventoryButton);
        nav.add(Box.createVerticalStrut(8));

        nav.add(reportsButton);
        nav.add(Box.createVerticalStrut(8));

        nav.add(alertsButton);

        return nav;
    }

    /*
     * This method creates one reusable navigation button.
     */
    private JButton createNavigationButton(String text, boolean isActive) {
        JButton button = new JButton(text);

        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);

        button.setBackground(
                isActive ? ACTIVE_COLOR : SIDEBAR_COLOUR
        );

        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));

        button.setPreferredSize(new Dimension(174, 40));
        button.setMaximumSize(new Dimension(174, 40));

        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(new EmptyBorder(0, 15, 0, 15));

        return button;
    }

    /*
     * This method creates the centre area using CardLayout.
     * Each page is added using a unique name.
     */
    private JPanel createContentArea() {
        contentCardLayout = new CardLayout();
        contentCardPanel = new JPanel(contentCardLayout);
        contentCardPanel.setBackground(BACKGROUND_COLOR);

        contentCardPanel.add(createOverviewPage(), "Overview");
        contentCardPanel.add(createPagePlaceholder("Sales"), "Sales");
        contentCardPanel.add(
                createPagePlaceholder("Inventory"),
                "Inventory"
        );
        contentCardPanel.add(createPagePlaceholder("Reports"), "Reports");
        contentCardPanel.add(createPagePlaceholder("Alerts"), "Alerts");

        return contentCardPanel;
    }

    /*
     * This method creates the Overview page.
     */
    /*
 * This method creates the Overview page. It places the page header at the top,
 * the KPI cards underneath it and the chart area below the cards.
 */
private JPanel createOverviewPage() {
    JPanel overviewPage = new JPanel(new BorderLayout(0, 15));
    overviewPage.setBackground(BACKGROUND_COLOR);
    overviewPage.setBorder(new EmptyBorder(10, 25, 25, 25));

    JPanel topSection = new JPanel(new BorderLayout(0, 15));
    topSection.setBackground(BACKGROUND_COLOR);

    topSection.add(createHeader(), BorderLayout.NORTH);
    topSection.add(createKpiPanel(), BorderLayout.CENTER);

    overviewPage.add(topSection, BorderLayout.NORTH);
    overviewPage.add(createChartPlaceholder(), BorderLayout.CENTER);

    return overviewPage;
}
    /*
     * This method creates a temporary page for sections that have not been
     * developed yet.
     */
    private JPanel createPagePlaceholder(String pageName) {
        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(BACKGROUND_COLOR);
        page.setBorder(new EmptyBorder(20, 25, 25, 25));

        JLabel pageLabel = new JLabel(pageName + " Page");
        pageLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        pageLabel.setForeground(new Color(31, 41, 55));

        page.add(pageLabel);

        return page;
    }

    /*
     * This method changes the visible page and updates the active button.
     */
    private void showPage(String pageName, JButton selectedButton) {
        System.out.println("Opening page: " + pageName);

        contentCardLayout.show(contentCardPanel, pageName);
        updateActiveButton(selectedButton);

        contentCardPanel.revalidate();
        contentCardPanel.repaint();
    }

    /*
     * This method resets all button colours and highlights the selected one.
     */
    private void updateActiveButton(JButton selectedButton) {
        JButton[] navigationButtons = {
                overviewButton,
                salesButton,
                inventoryButton,
                reportsButton,
                alertsButton
        };

        for (JButton button : navigationButtons) {
            button.setBackground(SIDEBAR_COLOUR);
        }

        selectedButton.setBackground(ACTIVE_COLOR);
    }

    /*
     * This method creates the header shown on the Overview page.
     */
    /*
 * This method creates the header shown on the Overview page.
 * It displays the page title and the current date automatically.
 */
private JPanel createHeader() {
    JPanel header = new JPanel(new BorderLayout());
    header.setBackground(BACKGROUND_COLOR);

    JPanel leftHeader = new JPanel();
    leftHeader.setLayout(new BoxLayout(leftHeader, BoxLayout.Y_AXIS));
    leftHeader.setBackground(BACKGROUND_COLOR);

    JLabel titleLabel = new JLabel("Overview");
    titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
    titleLabel.setForeground(new Color(31, 41, 55));
    titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    /*
     * The date is created when the dashboard opens, so it always shows
     * the current day instead of using hardcoded text.
     */
    java.time.LocalDate currentDate =
            java.time.LocalDate.now();

    java.time.format.DateTimeFormatter dateFormatter =
            java.time.format.DateTimeFormatter.ofPattern(
                    "EEEE, d MMMM yyyy"
            );

    String formattedDate =
            currentDate.format(dateFormatter);

    JLabel dateLabel = new JLabel(
            formattedDate + "  |  Live retail overview"
    );

    dateLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
    dateLabel.setForeground(new Color(100, 116, 139));
    dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    leftHeader.add(titleLabel);
    leftHeader.add(Box.createVerticalStrut(4));
    leftHeader.add(dateLabel);

    header.add(leftHeader, BorderLayout.WEST);

    return header;
}

    /*
     * This method creates a temporary chart area for the Overview page.
     */
    private JPanel createChartPlaceholder() {
        JPanel chartPlaceholder = new JPanel(new GridBagLayout());
        chartPlaceholder.setBackground(Color.WHITE);

        chartPlaceholder.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(226, 232, 240)
                        ),
                        new EmptyBorder(20, 20, 20, 20)
                )
        );

        JLabel placeholderLabel =
                new JLabel("Chart Placeholder");

        placeholderLabel.setFont(
                new Font("SansSerif", Font.PLAIN, 16)
        );

        placeholderLabel.setForeground(
                new Color(100, 116, 139)
        );

        chartPlaceholder.add(placeholderLabel);

        return chartPlaceholder;
    }
}