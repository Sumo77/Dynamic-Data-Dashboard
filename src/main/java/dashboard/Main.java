package dashboard;

import dashboard.cli.CommandLineInterface;
import dashboard.database.DatabaseConnection;

import java.sql.Connection;

public class Main {

    public static void main(String[] args) {

        System.out.println("Running from: "
                + System.getProperty("user.dir"));

        try (Connection connection = DatabaseConnection.connect()) {

            System.out.println("Database connected successfully.");

            CommandLineInterface cli =
                    new CommandLineInterface();

            cli.start();

        } catch (Exception exception) {

            System.out.println(
                    "Database connection failed: "
                    + exception.getMessage()
            );
        }
    }
}