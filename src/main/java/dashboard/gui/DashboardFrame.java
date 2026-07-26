package dashboard.gui;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DashboardFrame extends JFrame  {
    private static final Color SIDEBAR_COLOUR = new Color(28, 36, 50);

    private static final Color BACKGROUND_COLOR = new Color(244, 247, 250);
    
    private static final Color ACTIVE_COLOR = new Color(24, 145, 132);

    public DashboardFrame() {
        configureWindow();
        createLayout();
    }

    public void configureWindow() {
        setTitle("Dynamic Retail Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void createLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        JPanel sidebar = createSidebar();
        JPanel contentArea = createContentArea();

        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(contentArea, BorderLayout.CENTER);

        add(mainPanel);
    }
    
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBackground(SIDEBAR_COLOUR);
        
        sidebar.setLayout(
            new BoxLayout(sidebar, BoxLayout.Y_AXIS)
    );
    sidebar.setBorder(
        new EmptyBorder(25, 18, 25, 18)
    );

        JLabel logo = new JLabel("Dynamic Retail Dashboard");
        logo.setForeground(Color.WHITE);
        logo.setFont(
                new Font("SansSerif", Font.BOLD, 22)
        );
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Admin Dashboard");
        subtitle.setForeground(new Color(170, 180, 195));
        subtitle.setFont(
                new Font("SansSerif", Font.PLAIN, 11)
        );

        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(subtitle);
        sidebar.add(Box.createVerticalStrut(35));

        sidebar.add(
                createNavigationButton("Overview", true)
        );

        sidebar.add(Box.createVerticalStrut(8));

        sidebar.add(
                createNavigationButton("Sales", false)
        );

        sidebar.add(Box.createVerticalStrut(8));

        sidebar.add(
                createNavigationButton("Inventory", false)
        );

        sidebar.add(Box.createVerticalStrut(8));

        sidebar.add(
                createNavigationButton("Reports", false)
        );

        sidebar.add(Box.createVerticalStrut(8));

        sidebar.add(
                createNavigationButton("Alerts", false)
        );

        return sidebar;
    }

    private JButton createNavigationButton(String text, boolean isActive) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBackground(isActive ? ACTIVE_COLOR : SIDEBAR_COLOUR);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setPreferredSize(new Dimension(180, 40));
        return button;
    }

    private JPanel createContentArea() {
        JPanel contentArea = new JPanel();
        contentArea.setBackground(BACKGROUND_COLOR);
        contentArea.setLayout(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Welcome to the Dynamic Retail Dashboard");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentArea.add(welcomeLabel, BorderLayout.CENTER);

        return contentArea;
    }

   
    
    
    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setBackground(BACKGROUND_COLOR);
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel titleLabel = new JLabel("Dashboard");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.add(titleLabel, BorderLayout.WEST);

        return header;
    }

    private JPanel createChartPlaceholder() {
        JPanel chartPlaceholder = new JPanel();
        chartPlaceholder.setBackground(Color.WHITE);
        chartPlaceholder.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        chartPlaceholder.setPreferredSize(new Dimension(0, 300));

        JLabel placeholderLabel = new JLabel("Chart Placeholder");
        placeholderLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        placeholderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        chartPlaceholder.add(placeholderLabel);

        return chartPlaceholder;        
    }

}



    

    

