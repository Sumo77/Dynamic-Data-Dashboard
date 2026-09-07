package dashboard.gui;

import dashboard.database.SchemaIntrospector;
import dashboard.database.SchemaIntrospector.TableMeta;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardFrame extends JFrame {

    private static final Color SIDEBAR = new Color(17,24,39);
    private static final Color BACKGROUND = new Color(245,247,250);
    private static final Color ACTIVE = new Color(0,212,255);

    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, FilterableDashboardPage> filterablePages = new LinkedHashMap<>();
    private DashboardFilter globalFilter = DashboardFilter.defaults();
    private Map<String, TableMeta> schema = Map.of();

    public DashboardFrame() {
        try {
            schema = SchemaIntrospector.introspect();
            System.out.println("Schema loaded successfully.");
        } catch (Exception ex) {
            System.err.println("Schema introspection failed: " + ex.getMessage());
        }

        setTitle("Dynamic Retail Dashboard");
        setSize(1200,800);
        setMinimumSize(new Dimension(1000,700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        createLayout();
    }

    private void createLayout() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);
        root.add(createTopPanel(),BorderLayout.NORTH);
        root.add(new SidebarPanel(this::showPage),BorderLayout.WEST);
        root.add(createContentArea(),BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createTopPanel() {
        JPanel top = new JPanel(new BorderLayout());
        top.setPreferredSize(new Dimension(0,85));
        top.setBackground(BACKGROUND);

        JPanel logo = new JPanel();
        logo.setLayout(new BoxLayout(logo,BoxLayout.Y_AXIS));
        logo.setPreferredSize(new Dimension(210,85));
        logo.setBackground(SIDEBAR);
        logo.setBorder(BorderFactory.createEmptyBorder(17,18,15,18));

        JLabel line1 = new JLabel("Dynamic Retail");
        line1.setForeground(Color.WHITE);
        line1.setFont(new Font("SansSerif",Font.BOLD,18));
        JLabel line2 = new JLabel("Dashboard");
        line2.setForeground(ACTIVE);
        line2.setFont(new Font("SansSerif",Font.BOLD,18));
        JLabel sub = new JLabel("Admin Dashboard");
        sub.setForeground(new Color(170,180,195));
        sub.setFont(new Font("SansSerif",Font.PLAIN,11));

        logo.add(line1); logo.add(line2); logo.add(Box.createVerticalStrut(3)); logo.add(sub);
        top.add(logo,BorderLayout.WEST);
        return top;
    }

    private JPanel createContentArea() {
        content.setBackground(BACKGROUND);

        OverviewPanel overview = new OverviewPanel(schema,this::onGlobalFilterChanged);
        SalesPanel sales = new SalesPanel();
        InventoryPanel inventory = new InventoryPanel();
        ProductsPanel products = new ProductsPanel();
        MarketingPanel marketing = new MarketingPanel();
        CustomersPanel customers = new CustomersPanel();

        filterablePages.put("Overview",overview);
        filterablePages.put("Inventory",inventory);
        filterablePages.put("Products",products);
        filterablePages.put("Marketing",marketing);
        filterablePages.put("Customers",customers);

        content.add(overview,"Overview");
        content.add(sales,"Sales");
        content.add(inventory,"Inventory");
        content.add(products,"Products");
        content.add(marketing,"Marketing");
        content.add(customers,"Customers");
        content.add(placeholder("Reports","Report generation remains on the existing project roadmap."),"Reports");
        content.add(placeholder("Alerts","Use the existing low-stock alert backend route here."),"Alerts");

        // The overview constructor publishes the first filter; ensure all other pages match it.
        filterablePages.forEach((name,page)->{ if (!"Overview".equals(name)) page.applyFilter(globalFilter); });
        return content;
    }

    private void onGlobalFilterChanged(DashboardFilter filter) {
        globalFilter = filter;
        filterablePages.forEach((name,page)->{
            if (!"Overview".equals(name)) page.applyFilter(filter);
        });
    }

    private void showPage(String page) {
        cards.show(content,page);
        FilterableDashboardPage filterable = filterablePages.get(page);
        if (filterable != null && !"Overview".equals(page)) filterable.applyFilter(globalFilter);
    }

    private JPanel placeholder(String title,String message) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(25,25,25,25));
        JLabel label = new JLabel("<html><h1>"+title+"</h1><p>"+message+"</p></html>");
        panel.add(label,BorderLayout.NORTH);
        return panel;
    }
}
