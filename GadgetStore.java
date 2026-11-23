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
                case SORT -> {
                    List<Employee> sorted = EmployeeSort.byName(staff);
                    System.out.printf("Showing %d of %d employees in alphabetical order:%n",
                            Math.min(20, sorted.size()), sorted.size());
                    sorted.stream().limit(20).forEach(employee -> System.out.println(employee.name()));
                }
                case EXIT -> { return; }
            }
        }
    }
}

enum MenuOption {
    PREVIEW("Preview first three employees"), EXIT("Exit"), SORT("Sort employees by name");
    final String label;
    MenuOption(String label) { this.label = label; }
}

record Employee(String name, String managerType, String department) {}

class EmployeeSort {
    static final Comparator<Employee> NAME_ORDER =
            Comparator.comparing(Employee::name, String.CASE_INSENSITIVE_ORDER);

    static List<Employee> byName(List<Employee> staff) {
        Employee[] records = staff.toArray(Employee[]::new);
        sortRange(records, new Employee[records.length], 0, records.length);
        return new ArrayList<>(Arrays.asList(records));
    }

    // Sort the range from start (inclusive) to end (exclusive).
    private static void sortRange(Employee[] records, Employee[] buffer, int start, int end) {
        if (end - start < 2) return;
        int middle = start + (end - start) / 2;
        sortRange(records, buffer, start, middle);
        sortRange(records, buffer, middle, end);

        int left = start;
        int right = middle;
        for (int i = start; i < end; i++) {
            if (right == end || (left < middle && NAME_ORDER.compare(records[left], records[right]) <= 0)) {
                buffer[i] = records[left++];
            } else {
                buffer[i] = records[right++];
            }
        }
        System.arraycopy(buffer, start, records, start, end - start);
    }
}

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
