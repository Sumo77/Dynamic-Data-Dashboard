package dashboard;

import dashboard.gui.DashboardFrame;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DashboardFrame dashboardFrame =
                    new DashboardFrame();

            dashboardFrame.setVisible(true);
        });
    }
}