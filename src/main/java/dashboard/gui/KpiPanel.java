package dashboard.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Random;

/*
 * This class creates and updates the four KPI cards displayed on the Overview page.
 */
public class KpiPanel extends JPanel {

    private static final Color BACKGROUND_COLOR =
            new Color(245, 247, 250);

    private static final Color PRIMARY_TEXT =
            new Color(31, 41, 55);

    private static final Color SECONDARY_TEXT =
            new Color(100, 116, 139);

    private static final Color BORDER_COLOR =
            new Color(226, 232, 240);

    private final Random random = new Random();

    private JLabel revenueValueLabel;
    private JLabel profitValueLabel;
    private JLabel customersValueLabel;
    private JLabel productsValueLabel;

    /*
     * These are temporary sample values. Cooper can replace them with database
     * results when the backend service is ready.
     */
    private double sampleRevenue = 61_500.00;
    private double sampleProfit = 16_400.00;
    private int sampleCustomers = 5_000;
    private int sampleProducts = 500;

    /*
     * This constructor creates the row of KPI cards.
     */
    public KpiPanel() {
        configurePanel();
        createCards();
        updateLabels();
    }

    /*
     * This method configures the layout used by the KPI cards.
     */
    private void configurePanel() {
        setLayout(
                new GridLayout(1, 4, 15, 0)
        );

        setBackground(BACKGROUND_COLOR);
    }

    /*
     * This method creates all four KPI cards.
     */
    private void createCards() {
        revenueValueLabel = new JLabel();
        profitValueLabel = new JLabel();
        customersValueLabel = new JLabel();
        productsValueLabel = new JLabel();

        add(
                createKpiCard(
                        "Total Revenue",
                        revenueValueLabel
                )
        );

        add(
                createKpiCard(
                        "Total Profit",
                        profitValueLabel
                )
        );

        add(
                createKpiCard(
                        "Customers",
                        customersValueLabel
                )
        );

        add(
                createKpiCard(
                        "Products",
                        productsValueLabel
                )
        );
    }

    /*
     * This method creates one reusable KPI card.
     */
    private JPanel createKpiCard(
            String title,
            JLabel valueLabel
    ) {
        JPanel card = new JPanel();

        card.setLayout(
                new BoxLayout(card, BoxLayout.Y_AXIS)
        );

        card.setBackground(Color.WHITE);

        card.setPreferredSize(
                new Dimension(0, 85)
        );

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                BORDER_COLOR
                        ),
                        new EmptyBorder(
                                15,
                                18,
                                15,
                                18
                        )
                )
        );

        JLabel titleLabel =
                new JLabel(title);

        titleLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        13
                )
        );

        titleLabel.setForeground(SECONDARY_TEXT);

        titleLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        valueLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        23
                )
        );

        valueLabel.setForeground(PRIMARY_TEXT);

        valueLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        card.add(titleLabel);
        card.add(
                Box.createVerticalStrut(8)
        );
        card.add(valueLabel);

        return card;
    }

    /*
     * This method makes small changes to the sample values when the dashboard
     * is refreshed. Cooper can replace this with a backend service call later.
     */
    public void refreshSampleValues() {
        sampleRevenue +=
                random.nextDouble() * 2_000 - 1_000;

        sampleProfit +=
                random.nextDouble() * 800 - 400;

        sampleCustomers +=
                random.nextInt(21) - 10;

        sampleProducts +=
                random.nextInt(7) - 3;

        sampleRevenue =
                Math.max(50_000, sampleRevenue);

        sampleProfit =
                Math.max(10_000, sampleProfit);

        sampleCustomers =
                Math.max(4_500, sampleCustomers);

        sampleProducts =
                Math.max(450, sampleProducts);

        updateLabels();
    }

    /*
     * This method displays the current KPI values inside their labels.
     */
    private void updateLabels() {
        revenueValueLabel.setText(
                String.format(
                        "$%,.2f",
                        sampleRevenue
                )
        );

        profitValueLabel.setText(
                String.format(
                        "$%,.2f",
                        sampleProfit
                )
        );

        customersValueLabel.setText(
                String.format(
                        "%,d",
                        sampleCustomers
                )
        );

        productsValueLabel.setText(
                String.format(
                        "%,d",
                        sampleProducts
                )
        );
    }
}