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

    private static final Color POSITIVE_COLOR =
            new Color(22, 163, 74);

    private static final Color NEGATIVE_COLOR =
            new Color(220, 38, 38);

    private final JLabel revenueValue =
            new JLabel("Loading...");

    private final JLabel growthValue =
            new JLabel("Loading...");

    private final JLabel profitValue =
            new JLabel("Loading...");

    private final JLabel marginValue =
            new JLabel("Loading...");

    private final JLabel turnoverValue =
            new JLabel("Loading...");

    private final JLabel retentionValue =
            new JLabel("Loading...");

    private final JLabel marketingRoiValue =
            new JLabel("Loading...");

    private final JLabel revenueDelta =
            new JLabel("--");

    private final JLabel growthDelta =
            new JLabel("--");

    private final JLabel profitDelta =
            new JLabel("--");

    private final JLabel marginDelta =
            new JLabel("--");

    private final JLabel turnoverDelta =
            new JLabel("--");

    private final JLabel retentionDelta =
            new JLabel("--");

    private final JLabel marketingDelta =
            new JLabel("--");

    public KpiPanel() {

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

        createCards();
    }

    private void createCards() {

        add(
                createCard(
                        "Total Revenue",
                        revenueValue,
                        revenueDelta
                )
        );

        add(
                createCard(
                        "Revenue Growth Rate",
                        growthValue,
                        growthDelta
                )
        );

        add(
                createCard(
                        "Profit",
                        profitValue,
                        profitDelta
                )
        );

        add(
                createCard(
                        "Profit Margin",
                        marginValue,
                        marginDelta
                )
        );

        add(
                createCard(
                        "Inventory Turnover",
                        turnoverValue,
                        turnoverDelta
                )
        );

        add(
                createCard(
                        "Customer Retention",
                        retentionValue,
                        retentionDelta
                )
        );

        add(
                createCard(
                        "Marketing ROI",
                        marketingRoiValue,
                        marketingDelta
                )
        );

        JPanel emptyPanel =
                new JPanel();

        emptyPanel.setBackground(
                BACKGROUND_COLOR
        );

        add(emptyPanel);
    }

    private JPanel createCard(
            String title,
            JLabel valueLabel,
            JLabel deltaLabel
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
                        19
                )
        );

        valueLabel.setForeground(
                PRIMARY_TEXT
        );

        valueLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        deltaLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        11
                )
        );

        deltaLabel.setForeground(
                SECONDARY_TEXT
        );

        deltaLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        card.add(titleLabel);

        card.add(
                Box.createVerticalStrut(
                        7
                )
        );

        card.add(valueLabel);

        card.add(
                Box.createVerticalStrut(
                        5
                )
        );

        card.add(deltaLabel);

        return card;
    }

    public void updateKpis(
            double revenue,
            double growth,
            double profit,
            double margin,
            double turnover,
            double retention,
            double marketingRoi,
            double revenueDeltaValue,
            double growthDeltaValue,
            double profitDeltaValue,
            double marginDeltaValue,
            double turnoverDeltaValue,
            double retentionDeltaValue,
            double marketingDeltaValue
    ) {

        revenueValue.setText(
                String.format(
                        "$%,.2f",
                        revenue
                )
        );

        growthValue.setText(
                String.format(
                        "%.2f%%",
                        growth
                )
        );

        profitValue.setText(
                String.format(
                        "$%,.2f",
                        profit
                )
        );

        marginValue.setText(
                String.format(
                        "%.2f%%",
                        margin
                )
        );

        turnoverValue.setText(
                String.format(
                        "%.2f",
                        turnover
                )
        );

        retentionValue.setText(
                String.format(
                        "%.2f%%",
                        retention
                )
        );

        marketingRoiValue.setText(
                String.format(
                        "%.2f%%",
                        marketingRoi
                )
        );

        setDelta(
                revenueDelta,
                revenueDeltaValue
        );

        setDelta(
                growthDelta,
                growthDeltaValue
        );

        setDelta(
                profitDelta,
                profitDeltaValue
        );

        setDelta(
                marginDelta,
                marginDeltaValue
        );

        setDelta(
                turnoverDelta,
                turnoverDeltaValue
        );

        setDelta(
                retentionDelta,
                retentionDeltaValue
        );

        setDelta(
                marketingDelta,
                marketingDeltaValue
        );
    }

    private void setDelta(
            JLabel label,
            double value
    ) {

        if (value > 0) {

            label.setText(
                    String.format(
                            "▲ %.2f%% vs prior period",
                            value
                    )
            );

            label.setForeground(
                    POSITIVE_COLOR
            );

        } else if (value < 0) {

            label.setText(
                    String.format(
                            "▼ %.2f%% vs prior period",
                            Math.abs(value)
                    )
            );

            label.setForeground(
                    NEGATIVE_COLOR
            );

        } else {

            label.setText(
                    "No change vs prior period"
            );

            label.setForeground(
                    SECONDARY_TEXT
            );
        }
    }

    public void showUnavailableCrossKpis() {

        profitValue.setText(
                "Backend pending"
        );

        marginValue.setText(
                "Backend pending"
        );

        turnoverValue.setText(
                "Backend pending"
        );

        retentionValue.setText(
                "Backend pending"
        );

        marketingRoiValue.setText(
                "Backend pending"
        );

        profitDelta.setText("--");
        marginDelta.setText("--");
        turnoverDelta.setText("--");
        retentionDelta.setText("--");
        marketingDelta.setText("--");
    }

    public void updateSoloKpis(
        double revenue,
        double growth
) {

    revenueValue.setText(
            String.format(
                    "$%,.2f",
                    revenue
            )
    );

    growthValue.setText(
            String.format(
                    "%.2f%%",
                    growth
            )
    );

    if (growth > 0) {

        revenueDelta.setText(
                String.format(
                        "▲ %.2f%% vs prior period",
                        growth
                )
        );

        growthDelta.setText(
                String.format(
                        "▲ %.2f%% vs prior period",
                        growth
                )
        );

        revenueDelta.setForeground(
                POSITIVE_COLOR
        );

        growthDelta.setForeground(
                POSITIVE_COLOR
        );

    } else if (growth < 0) {

        revenueDelta.setText(
                String.format(
                        "▼ %.2f%% vs prior period",
                        Math.abs(growth)
                )
        );

        growthDelta.setText(
                String.format(
                        "▼ %.2f%% vs prior period",
                        Math.abs(growth)
                )
        );

        revenueDelta.setForeground(
                NEGATIVE_COLOR
        );

        growthDelta.setForeground(
                NEGATIVE_COLOR
        );

    } else {

        revenueDelta.setText(
                "No change vs prior period"
        );

        growthDelta.setText(
                "No change vs prior period"
        );

        revenueDelta.setForeground(
                SECONDARY_TEXT
        );

        growthDelta.setForeground(
                SECONDARY_TEXT
        );
    }

    revalidate();
    repaint();
}


    public void showError() {

        revenueValue.setText("Unavailable");
        growthValue.setText("Unavailable");
        profitValue.setText("Unavailable");
        marginValue.setText("Unavailable");
        turnoverValue.setText("Unavailable");
        retentionValue.setText("Unavailable");
        marketingRoiValue.setText("Unavailable");
    }
}