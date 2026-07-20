package dashboard.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private DatabaseConnection() {
    }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DatabaseConfig.DATABASE_URL);
    }
}