package dashboard;

import dashboard.database.DatabaseInitializer;

public class Main {

    public static void main(String[] args) {

        DatabaseInitializer.initialize();

        System.out.println("Dynamic Data Dashboard Started");
    }
}