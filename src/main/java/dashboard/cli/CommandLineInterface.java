package dashboard.cli;

import dashboard.repository.SalesRepository;

import java.util.Scanner;

public class CommandLineInterface {

    private final Scanner scanner = new Scanner(System.in);
    private final SalesRepository salesRepository = new SalesRepository();

    public void start() {

        boolean running = true;

        while (running) {

            System.out.println();
            System.out.println("==================================");
            System.out.println(" Dynamic Retail Dashboard");
            System.out.println("==================================");
            System.out.println("1. View Sales");
            System.out.println("2. Exit");
            System.out.print("Select option: ");

            String option = scanner.nextLine();

            switch (option) {

                case "1":
                    salesRepository.displaySales();
                    break;

                case "2":
                    running = false;
                    break;

                default:
                    System.out.println("Invalid option.");
            }
        }

        scanner.close();
    }
}