package dashboard.repository;

import dashboard.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SalesRepository {

    public void displaySales() {

        String sql = """
                SELECT
                    sale_id,
                    customer_id,
                    product_id,
                    quantity
                FROM sales
                LIMIT 10
                """;

        try (
                Connection connection =
                        DatabaseConnection.connect();

                Statement statement =
                        connection.createStatement();

                ResultSet results =
                        statement.executeQuery(sql)
        ) {

            System.out.println();
            System.out.println(
                    "Sale ID | Customer ID | Product ID | Quantity"
            );
            System.out.println(
                    "----------------------------------------------"
            );

            while (results.next()) {

                System.out.printf(
                        "%-7d | %-11d | %-10d | %d%n",
                        results.getInt("sale_id"),
                        results.getInt("customer_id"),
                        results.getInt("product_id"),
                        results.getInt("quantity")
                );
            }

        } catch (SQLException exception) {

            System.out.println();
            System.out.println(
                    "Unable to retrieve sales: "
                            + exception.getMessage()
            );
        }
    }
}