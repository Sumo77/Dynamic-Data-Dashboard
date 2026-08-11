// KpiPanel.java

package dashboard.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class KpiPanel extends JPanel {

    private static final Color BACKGROUND_COLOR =
            new Color(245, 247, 250);

    private static final Color PRIMARY_TEXT =
            new Color(31, 41, 55);

    private static final Color SECONDARY_TEXT =
            new Color(100, 116, 139);

    private static final Color BORDER_COLOR =
            new Color(226, 232, 240);

    private final JLabel revenueValueLabel =
            new JLabel("Loading...");

    private final JLabel growthValueLabel =
            new JLabel("Loading...");

    private final JLabel profitValueLabel =
            new JLabel("Loading...");

    private final JLabel marginValueLabel =
            new JLabel("Loading...");

    private final JLabel turnoverValueLabel =
            new JLabel("Loading...");

    private final JLabel retentionValueLabel =
            new JLabel("Loading...");

    private final JLabel marketingRoiValueLabel =
            new JLabel("Not Available");

    public KpiPanel() {

        configurePanel();
        createCards();
    }

    private void configurePanel() {

        setLayout(
                new GridLayout(
                        2,
                        4,
                        12,
                        12
                )
        );

        setBackground(
                BACKGROUND_COLOR
        );
    }

    private void createCards() {

        add(
                createKpiCard(
                        "Total Revenue",
                        revenueValueLabel
                )
        );

        add(
                createKpiCard(
                        "Revenue Growth Rate",
                        growthValueLabel
                )
        );

        add(
                createKpiCard(
                        "Profit",
                        profitValueLabel
                )
        );

        add(
                createKpiCard(
                        "Profit Margin",
                        marginValueLabel
                )
        );

        add(
                createKpiCard(
                        "Inventory Turnover",
                        turnoverValueLabel
                )
        );

        add(
                createKpiCard(
                        "Customer Retention",
                        retentionValueLabel
                )
        );

        add(
                createKpiCard(
                        "Marketing ROI",
                        marketingRoiValueLabel
                )
        );

        JPanel emptyPanel =
                new JPanel();

        emptyPanel.setBackground(
                BACKGROUND_COLOR
        );

        add(emptyPanel);
    }

    private JPanel createKpiCard(
            String title,
            JLabel valueLabel
    ) {

        JPanel card =
                new JPanel();

        card.setLayout(
                new BoxLayout(
                        card,
                        BoxLayout.Y_AXIS
                )
        );

        card.setBackground(
                Color.WHITE
        );

        card.setBorder(
                BorderFactory.createCompoundBorder(

                        BorderFactory.createLineBorder(
                                BORDER_COLOR
                        ),

                        new EmptyBorder(
                                12,
                                15,
                                12,
                                15
                        )
                )
        );

        JLabel titleLabel =
                new JLabel(
                        title
                );

        titleLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        12
                )
        );

        titleLabel.setForeground(
                SECONDARY_TEXT
        );

        titleLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        valueLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        20
                )
        );

        valueLabel.setForeground(
                PRIMARY_TEXT
        );

        valueLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        card.add(
                titleLabel
        );

        card.add(
                Box.createVerticalStrut(
                        7
                )
        );

        card.add(
                valueLabel
        );

        return card;
    }

    /*
     * Updates the KPI cards using REAL values from the backend.
     */
    public void updateKpis(
            double revenue,
            double growth,
            double profit,
            double margin,
            double turnover,
            double retention
    ) {

        revenueValueLabel.setText(
                String.format(
                        "$%,.2f",
                        revenue
                )
        );

        growthValueLabel.setText(
                String.format(
                        "%.2f%%",
                        growth
                )
        );

        profitValueLabel.setText(
                String.format(
                        "$%,.2f",
                        profit
                )
        );

        marginValueLabel.setText(
                String.format(
                        "%.2f%%",
                        margin
                )
        );

        turnoverValueLabel.setText(
                String.format(
                        "%.2f",
                        turnover
                )
        );

        retentionValueLabel.setText(
                String.format(
                        "%.2f%%",
                        retention
                )
        );

        /*
         * Backend does not currently provide enough
         * information to calculate Marketing ROI.
         */
        marketingRoiValueLabel.setText(
                "Not Available"
        );
    }

    public void showError() {

        revenueValueLabel.setText(
                "Unavailable"
        );

        growthValueLabel.setText(
                "Unavailable"
        );

        profitValueLabel.setText(
                "Unavailable"
        );

        marginValueLabel.setText(
                "Unavailable"
        );

        turnoverValueLabel.setText(
                "Unavailable"
        );

        retentionValueLabel.setText(
                "Unavailable"
        );

        marketingRoiValueLabel.setText(
                "Unavailable"
        );
    }
}