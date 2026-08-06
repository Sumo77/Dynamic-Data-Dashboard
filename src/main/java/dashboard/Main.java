package dashboard;

import dashboard.gui.DashboardFrame;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            DashboardFrame dashboard =
                    new DashboardFrame();

            dashboard.setVisible(true);
        });
    }
}