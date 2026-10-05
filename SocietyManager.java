import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Housing Society Management System (console, no dependencies, Java 11+)
 * Features: residents, monthly maintenance bills, payments, complaints,
 * notices, visitor log. Data auto-saves to society.dat.
 *
 * Compile: javac SocietyManager.java
 * Run:     java SocietyManager
 */
public class SocietyManager {

    // ---------- Models ----------
    static class Resident implements Serializable {
        String flat, name, phone;
        Resident(String flat, String name, String phone) {
            this.flat = flat; this.name = name; this.phone = phone;
        }
        public String toString() { return String.format("%-8s %-20s %s", flat, name, phone); }
    }

    static class Bill implements Serializable {
        int id; String flat, month; double amount; boolean paid; String paidOn;
        Bill(int id, String flat, String month, double amount) {
            this.id = id; this.flat = flat; this.month = month; this.amount = amount;
        }
        public String toString() {
            return String.format("#%-4d %-8s %-9s Rs.%-9.2f %s", id, flat, month, amount,
                    paid ? "PAID (" + paidOn + ")" : "DUE");
        }
    }

    static class Complaint implements Serializable {
        int id; String flat, text, status = "OPEN", raisedOn;
        Complaint(int id, String flat, String text) {
            this.id = id; this.flat = flat; this.text = text; this.raisedOn = now();
        }
        public String toString() {
            return String.format("#%-4d %-8s [%s] %s (%s)", id, flat, status, text, raisedOn);
        }
    }

    static class Visitor implements Serializable {
        String name, flat, in, out;
        Visitor(String name, String flat) { this.name = name; this.flat = flat; this.in = now(); }
        public String toString() {
            return String.format("%-15s -> %-8s in: %s out: %s", name, flat, in, out == null ? "-" : out);
        }
    }

    static class Data implements Serializable {
        Map<String, Resident> residents = new LinkedHashMap<>();
        List<Bill> bills = new ArrayList<>();
        List<Complaint> complaints = new ArrayList<>();
        List<String> notices = new ArrayList<>();
        List<Visitor> visitors = new ArrayList<>();
        int billSeq = 1, complaintSeq = 1;
    }

    // ---------- State ----------
    static final String FILE = "society.dat";
    static final Scanner in = new Scanner(System.in);
    static Data db = load();

    static String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yy HH:mm"));
    }

    // ---------- Persistence ----------
    static Data load() {
        File f = new File(FILE);
        if (!f.exists()) return new Data();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (Data) ois.readObject();
        } catch (Exception e) {
            System.out.println("Could not read saved data, starting fresh.");
            return new Data();
        }
    }

    static void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE))) {
            oos.writeObject(db);
        } catch (IOException e) {
            System.out.println("Warning: failed to save data: " + e.getMessage());
        }
    }

    // ---------- Input helpers ----------
    static String ask(String prompt) {
        System.out.print(prompt + ": ");
        return in.hasNextLine() ? in.nextLine().trim() : "";
    }

    static double askAmount(String prompt) {
        while (true) {
            try {
                double v = Double.parseDouble(ask(prompt));
                if (v > 0) return v;
            } catch (NumberFormatException ignored) { }
            System.out.println("Enter a positive number.");
        }
    }

    static int askInt(String prompt) {
        try { return Integer.parseInt(ask(prompt)); } catch (NumberFormatException e) { return -1; }
    }

    static boolean flatExists(String flat) {
        if (db.residents.containsKey(flat)) return true;
        System.out.println("No resident registered for flat " + flat);
        return false;
    }

    // ---------- Features ----------
    static void addResident() {
        String flat = ask("Flat no. (e.g. A-101)").toUpperCase();
        if (flat.isEmpty()) { System.out.println("Flat no. required."); return; }
        if (db.residents.containsKey(flat)) { System.out.println("Flat already registered."); return; }
        String name = ask("Resident name");
        String phone = ask("Phone");
        if (name.isEmpty()) { System.out.println("Name required."); return; }
        db.residents.put(flat, new Resident(flat, name, phone));
        System.out.println("Resident added.");
    }

    static void listResidents() {
        if (db.residents.isEmpty()) { System.out.println("No residents yet."); return; }
        System.out.printf("%-8s %-20s %s%n", "FLAT", "NAME", "PHONE");
        db.residents.values().forEach(System.out::println);
    }

    static void generateBills() {
        if (db.residents.isEmpty()) { System.out.println("Add residents first."); return; }
        String month = ask("Month (e.g. Oct-2026)");
        double amt = askAmount("Maintenance amount per flat");
        int count = 0;
        for (String flat : db.residents.keySet()) {
            boolean exists = db.bills.stream()
                    .anyMatch(b -> b.flat.equals(flat) && b.month.equalsIgnoreCase(month));
            if (exists) continue;
            db.bills.add(new Bill(db.billSeq++, flat, month, amt));
            count++;
        }
        System.out.println(count + " bill(s) generated for " + month + ".");
    }

    static void payBill() {
        int id = askInt("Bill id");
        for (Bill b : db.bills) {
            if (b.id == id) {
                if (b.paid) { System.out.println("Already paid."); return; }
                b.paid = true; b.paidOn = now();
                System.out.printf("Received Rs.%.2f from %s for %s. Receipt: R-%d%n",
                        b.amount, b.flat, b.month, b.id);
                return;
            }
        }
        System.out.println("Bill not found.");
    }

    static void viewDues() {
        String flat = ask("Flat no. (blank = all flats)").toUpperCase();
        double total = 0; boolean any = false;
        for (Bill b : db.bills) {
            if (b.paid) continue;
            if (!flat.isEmpty() && !b.flat.equals(flat)) continue;
            System.out.println(b); total += b.amount; any = true;
        }
        System.out.println(any ? String.format("Total outstanding: Rs.%.2f", total) : "No dues.");
    }

    static void billHistory() {
        String flat = ask("Flat no.").toUpperCase();
        if (!flatExists(flat)) return;
        db.bills.stream().filter(b -> b.flat.equals(flat)).forEach(System.out::println);
    }

    static void raiseComplaint() {
        String flat = ask("Flat no.").toUpperCase();
        if (!flatExists(flat)) return;
        String text = ask("Complaint");
        if (text.isEmpty()) return;
        Complaint c = new Complaint(db.complaintSeq++, flat, text);
        db.complaints.add(c);
        System.out.println("Complaint registered with id #" + c.id);
    }

    static void resolveComplaint() {
        int id = askInt("Complaint id");
        for (Complaint c : db.complaints) {
            if (c.id == id) { c.status = "RESOLVED"; System.out.println("Marked resolved."); return; }
        }
        System.out.println("Complaint not found.");
    }

    static void listComplaints() {
        if (db.complaints.isEmpty()) { System.out.println("No complaints."); return; }
        db.complaints.forEach(System.out::println);
    }

    static void notices() {
        String choice = ask("1) Post notice  2) View notices");
        if (choice.equals("1")) {
            String text = ask("Notice text");
            if (!text.isEmpty()) db.notices.add(now() + " - " + text);
        } else if (db.notices.isEmpty()) {
            System.out.println("No notices.");
        } else {
            for (int i = db.notices.size() - 1; i >= 0; i--) System.out.println("* " + db.notices.get(i));
        }
    }

    static void visitors() {
        String choice = ask("1) Entry  2) Exit  3) Log");
        switch (choice) {
            case "1" -> {
                String name = ask("Visitor name");
                String flat = ask("Visiting flat").toUpperCase();
                if (flatExists(flat)) {
                    db.visitors.add(new Visitor(name, flat));
                    System.out.println("Entry logged.");
                }
            }
            case "2" -> {
                String name = ask("Visitor name");
                for (int i = db.visitors.size() - 1; i >= 0; i--) {
                    Visitor v = db.visitors.get(i);
                    if (v.name.equalsIgnoreCase(name) && v.out == null) {
                        v.out = now(); System.out.println("Exit logged."); return;
                    }
                }
                System.out.println("No active visit found for that name.");
            }
            case "3" -> {
                if (db.visitors.isEmpty()) System.out.println("No visitors logged.");
                db.visitors.forEach(System.out::println);
            }
            default -> System.out.println("Invalid choice.");
        }
    }

    // ---------- Main ----------
    static void menu() {
        System.out.println("\n===== HOUSING SOCIETY MANAGEMENT =====");
        System.out.println(" 1. Add resident          2. List residents");
        System.out.println(" 3. Generate bills        4. Pay bill");
        System.out.println(" 5. View dues             6. Flat bill history");
        System.out.println(" 7. Raise complaint       8. Resolve complaint");
        System.out.println(" 9. List complaints      10. Notices");
        System.out.println("11. Visitor log           0. Exit");
    }

    public static void main(String[] args) {
        while (true) {
            menu();
            switch (ask("Choose")) {
                case "1" -> addResident();
                case "2" -> listResidents();
                case "3" -> generateBills();
                case "4" -> payBill();
                case "5" -> viewDues();
                case "6" -> billHistory();
                case "7" -> raiseComplaint();
                case "8" -> resolveComplaint();
                case "9" -> listComplaints();
                case "10" -> notices();
                case "11" -> visitors();
                case "0" -> { save(); System.out.println("Saved. Goodbye!"); return; }
                default -> System.out.println("Invalid option.");
            }
            save();
        }
    }
}
