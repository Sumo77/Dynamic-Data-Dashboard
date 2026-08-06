package dashboard.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/*
 * This class creates the sidebar and handles the navigation button styling.
 */
public class SidebarPanel extends JPanel {

    private static final Color SIDEBAR_COLOUR =
            new Color(17, 24, 39);

    private static final Color ACTIVE_COLOR =
            new Color(0, 212, 255);

    private final Consumer<String> pageChangeHandler;

    private final Map<String, JButton> navigationButtons =
            new LinkedHashMap<>();

    /*
     * The page-change handler tells DashboardFrame which page to display.
     */
    public SidebarPanel(
            Consumer<String> pageChangeHandler
    ) {
        this.pageChangeHandler = pageChangeHandler;

        configurePanel();
        createNavigation();
    }

    /*
     * This method configures the sidebar size, layout and colours.
     */
    private void configurePanel() {
        setLayout(new BorderLayout());

        setPreferredSize(
                new Dimension(210, 0)
        );

        setBackground(SIDEBAR_COLOUR);

        setBorder(
                new EmptyBorder(
                        25,
                        18,
                        25,
                        18
                )
        );
    }

    /*
     * This method creates the navigation buttons displayed in the sidebar.
     */
    private void createNavigation() {
        JPanel navigationPanel = new JPanel();

        navigationPanel.setLayout(
                new BoxLayout(
                        navigationPanel,
                        BoxLayout.Y_AXIS
                )
        );

        navigationPanel.setBackground(SIDEBAR_COLOUR);

        addNavigationButton(
                navigationPanel,
                "Overview",
                true
        );

        addNavigationButton(
                navigationPanel,
                "Sales",
                false
        );

        addNavigationButton(
                navigationPanel,
                "Inventory",
                false
        );

        addNavigationButton(
                navigationPanel,
                "Reports",
                false
        );

        addNavigationButton(
                navigationPanel,
                "Alerts",
                false
        );

        add(
                navigationPanel,
                BorderLayout.NORTH
        );
    }

    /*
     * This method adds one button and spacing to the navigation panel.
     */
    private void addNavigationButton(
            JPanel navigationPanel,
            String pageName,
            boolean active
    ) {
        JButton button =
                createNavigationButton(
                        pageName,
                        active
                );

        navigationButtons.put(
                pageName,
                button
        );

        button.addActionListener(event -> {
            updateActiveButton(pageName);
            pageChangeHandler.accept(pageName);
        });

        navigationPanel.add(button);
        navigationPanel.add(
                Box.createVerticalStrut(8)
        );
    }

    /*
     * This method creates one reusable navigation button.
     */
    private JButton createNavigationButton(
            String text,
            boolean active
    ) {
        JButton button = new JButton(text);

        button.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);

        button.setBackground(
                active
                        ? ACTIVE_COLOR
                        : SIDEBAR_COLOUR
        );

        button.setForeground(Color.WHITE);

        button.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        14
                )
        );

        button.setPreferredSize(
                new Dimension(174, 40)
        );

        button.setMaximumSize(
                new Dimension(174, 40)
        );

        button.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        button.setBorder(
                new EmptyBorder(
                        0,
                        15,
                        0,
                        15
                )
        );

        return button;
    }

    /*
     * This method resets the button colours and highlights the selected page.
     */
    private void updateActiveButton(
            String selectedPage
    ) {
        for (Map.Entry<String, JButton> entry
                : navigationButtons.entrySet()) {

            JButton button = entry.getValue();

            if (entry.getKey().equals(selectedPage)) {
                button.setBackground(ACTIVE_COLOR);
            } else {
                button.setBackground(SIDEBAR_COLOUR);
            }
        }
    }
}