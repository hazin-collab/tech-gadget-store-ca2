import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class GadgetStore {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.println("Tech Gadget Department Store | Staff directory");
        while (true) {
            System.out.print("Employee filename: ");
            if (!input.hasNextLine()) return;
            try {
                List<Employee> staff = EmployeeFile.read(Path.of(input.nextLine().trim()));
                System.out.printf("Loaded %d employees.%n", staff.size());
                runMenu(input, staff);
                return;
            } catch (IOException | IllegalArgumentException e) {
                System.out.println("Could not load file: " + e.getMessage());
            }
        }
    }

    private static void runMenu(Scanner input, List<Employee> staff) {
        MenuOption[] options = MenuOption.values();
        while (true) {
            for (int i = 0; i < options.length; i++) {
                System.out.printf("%d. %s%n", i + 1, options[i].label);
            }
            System.out.print("Choice: ");
            if (!input.hasNextLine()) return;
            int choice;
            try {
                choice = Integer.parseInt(input.nextLine().trim());
            } catch (NumberFormatException e) {
                choice = 0;
            }
            if (choice < 1 || choice > options.length) {
                System.out.println("Choose a displayed number.");
                continue;
            }
            switch (options[choice - 1]) {
                case PREVIEW -> staff.stream().limit(3).forEach(employee ->
                    System.out.printf("%s | %s | %s%n", employee.name(),
                        employee.managerType(), employee.department()));
                case EXIT -> { return; }
            }
        }
    }
}

enum MenuOption {
    PREVIEW("Preview first three employees"), EXIT("Exit");
    final String label;
    MenuOption(String label) { this.label = label; }
}

record Employee(String name, String managerType, String department) {}

class EmployeeFile {
    static List<Employee> read(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file);
        String header = "First name,Last name,Gender,Email,Salary,Department,Position,Job title,Company";
        if (lines.isEmpty() || !lines.get(0).replace("\uFEFF", "").trim().equals(header)) {
            throw new IOException("Expected the nine-column Applicants_Form header.");
        }
        List<Employee> employees = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).isBlank()) continue;
            String[] fields = lines.get(i).split(",", -1);
            if (fields.length != 9 || fields[0].isBlank() || fields[1].isBlank()
                    || fields[5].isBlank() || fields[7].isBlank()) {
                throw new IOException("Check employee fields on line " + (i + 1));
            }
            String title = fields[7].trim();
            String managerType = switch (title) {
                case "Head Manager", "Senior Manager", "Manager", "Assistant Manager", "Team Lead" -> title;
                default -> "Staff";
            };
            employees.add(new Employee(fields[0].trim() + " " + fields[1].trim(),
                    managerType, fields[5].trim()));
        }
        if (employees.isEmpty()) throw new IOException("File contains no employees.");
        return employees;
    }
}
