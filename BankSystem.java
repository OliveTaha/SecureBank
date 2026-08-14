package SecureBank;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class BankSystem {
    private static final Scanner sc = new Scanner(System.in);
    private static Bank bank;

    // Admin fixed
    private static final String ADMIN_ID = "@admin";
    private static final String ADMIN_PASS = "admin200";

    public static void main(String[] args) {
        bank = Bank.loadFromFile(); // loads or creates
        System.out.println("=== SecureBank Console ===");
        while (true) {
            System.out.println("\nMain Menu:\n1. Login as User\n2. Create Account\n3. Exit\nEnter choice: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> loginMenu();
                case "2" -> createAccountFlow();
                case "3" -> {
                    bank.saveToFile();
                    System.out.println("Goodbye — data saved.");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void loginMenu() {
        System.out.println("\n--- Login Menu (type 'b' to go back) ---\n1. Login\n2. Forgot Password\nEnter choice: ");

        String c = sc.nextLine().trim();
        if (c.equalsIgnoreCase("b")) return;
        switch (c) {
            case "1" -> loginFlow();
            case "2" -> forgotPasswordFlow();
            default -> System.out.println("Invalid.");
        }
    }

    private static void loginFlow() {
        System.out.print("Enter User ID: ");
        String uid = sc.nextLine().trim();
        System.out.print("Enter Password: ");
        String pass = sc.nextLine().trim();

        // Admin check
        if (uid.equals(ADMIN_ID) && pass.equals(ADMIN_PASS)) {
            System.out.println("Admin verified.");
            adminPanel();
            return;
        }

        User user = bank.findByUserId(uid);
        if (user == null) {
            System.out.println("User not found.");
            return;
        }
        if (!user.getPassword().equals(pass)) {
            System.out.println("Wrong password.");
            return;
        }
        System.out.println("Welcome, " + user.getName() + "!");
        userSession(user);
    }

    private static void forgotPasswordFlow() {
        System.out.print("Enter your User ID: ");
        String uid = sc.nextLine().trim();
        User user = bank.findByUserId(uid);
        if (user == null) { System.out.println("User not found."); return; }

        System.out.print("Enter your security key (set at account creation): ");
        String key = sc.nextLine().trim();
        if (!user.getSecurityKey().equals(key)) {
            System.out.println("Security key mismatch.");
            return;
        }
        System.out.print("Enter new password: ");
        String np = sc.nextLine().trim();
        user.setPassword(np);
        bank.saveToFile();
        System.out.println("Password reset successful.");
    }

    private static void createAccountFlow() {
        System.out.println("\n--- Create Account (type 'b' to go back anytime) ---");
        System.out.print("Name: ");
        String name = sc.nextLine().trim();
        if (name.equalsIgnoreCase("b")) return;

        System.out.print("Choose username: ");
        String username = sc.nextLine().trim();
        if (username.equalsIgnoreCase("b")) return;

        if (bank.findByUsername(username) != null || username.equalsIgnoreCase("admin")) {
            System.out.println("Username not available.");
            return;
        }

        System.out.print("Set password: ");
        String pass = sc.nextLine().trim();
        if (pass.equalsIgnoreCase("b")) return;

        System.out.print("Initial deposit (number): ");
        String balStr = sc.nextLine().trim();
        if (balStr.equalsIgnoreCase("b")) return;
        double balance;
        try { balance = Double.parseDouble(balStr); if (balance < 0) throw new NumberFormatException(); }
        catch (Exception ex) { System.out.println("Invalid amount."); return; }

        System.out.print("Set a security key (for password recovery): ");
        String sk = sc.nextLine().trim();
        if (sk.equalsIgnoreCase("b")) return;

        User u = bank.createUser(username, pass, balance, name, sk);
        bank.saveToFile();
        System.out.println("Account created! Your UserID: " + u.getUserId() + "  Account#: " + u.getAccountNumber());
    }

    /* ---------------- User Session ---------------- */
    private static void userSession(User user) {
        while (true) {
            int unread = user.countUnreadMessages();
            System.out.println("\n--- User Menu (type 'b' to logout) ---");
            System.out.println("Unread messages: " + unread);
            System.out.println("1. View Balance");
            System.out.println("2. Deposit Money");
            System.out.println("3. Withdraw Money");
            System.out.println("4. Transfer Money");
            System.out.println("5. Requests & Messages");
            System.out.println("6. View Profile");
            System.out.println("7. Transaction History");
            System.out.println("b. Logout (back)");
            System.out.print("Choice: ");
            String ch = sc.nextLine().trim();
            if (ch.equalsIgnoreCase("b")) { bank.saveToFile(); return; }
            switch (ch) {
                case "1" -> System.out.println("Balance: " + user.getBalance());
                case "2" -> depositFlow(user);
                case "3" -> withdrawFlow(user);
                case "4" -> transferFlow(user);
                case "5" -> messagesFlow(user);
                case "6" -> profileFlow(user);
                case "7" -> printTransactions(user);
                default -> System.out.println("Invalid.");
            }
        }
    }

    private static void depositFlow(User user) {
        System.out.print("Amount to deposit: ");
        String s = sc.nextLine().trim();
        if (s.equalsIgnoreCase("b")) return;
        try {
            double amt = Double.parseDouble(s);
            if (amt <= 0) throw new NumberFormatException();
            user.deposit(amt);
            bank.saveToFile();
            System.out.println("Deposited. New balance: " + user.getBalance());
            System.out.println(receiptString(user, "Deposit", amt, 0.0));
        } catch (Exception ex) { System.out.println("Invalid amount."); }
    }

    private static void withdrawFlow(User user) {
        System.out.print("Amount to withdraw: ");
        String s = sc.nextLine().trim();
        if (s.equalsIgnoreCase("b")) return;
        try {
            double amt = Double.parseDouble(s);
            if (amt <= 0) throw new NumberFormatException();
            // AI-alert if >50%
            if (amt > user.getBalance() * 0.5) {
                user.addMessage(new Message("SYSTEM",
                        "Warning: You are withdrawing more than 50% of your balance (" + amt + ")."));
                System.out.println("⚠️ A warning message has been added to your inbox.");
            }
            user.withdraw(amt);
            bank.saveToFile();
            System.out.println("Withdrawn. New balance: " + user.getBalance());
            System.out.println(receiptString(user, "Withdraw", amt, 0.0));
        } catch (IllegalArgumentException ia) { System.out.println(ia.getMessage()); }
        catch (Exception ex) { System.out.println("Invalid amount."); }
    }

    private static void transferFlow(User user) {
        System.out.println("Transfer to: 1) Enter account number  2) Choose favourite");
        System.out.print("Choice (1/2 or b): ");
        String c = sc.nextLine().trim();
        if (c.equalsIgnoreCase("b")) return;
        User receiver = null;
        boolean isFavourite = false;
        if (c.equals("1")) {
            System.out.print("Enter receiver account number: ");
            String acc = sc.nextLine().trim();
            if (acc.equalsIgnoreCase("b")) return;
            try { int accNo = Integer.parseInt(acc); receiver = bank.findByAccountNumber(accNo); }
            catch (Exception ex) { System.out.println("Invalid account number."); return; }
        } else if (c.equals("2")) {
            List<Integer> favs = user.getFavorites();
            if (favs.isEmpty()) { System.out.println("No favourites."); return; }
            System.out.println("Favourites:");
            for (int i = 0; i < favs.size(); i++) System.out.println((i+1)+". " + favs.get(i));
            System.out.print("Choose index (or b): ");
            String idx = sc.nextLine().trim();
            if (idx.equalsIgnoreCase("b")) return;
            try {
                int i = Integer.parseInt(idx)-1;
                if (i < 0 || i >= favs.size()) { System.out.println("Invalid."); return; }
                int accNo = favs.get(i);
                receiver = bank.findByAccountNumber(accNo);
                isFavourite = true;
            } catch (Exception ex) { System.out.println("Invalid."); return; }
        } else { System.out.println("Invalid."); return; }

        if (receiver == null) { System.out.println("Receiver not found."); return; }
        if (receiver.getAccountNumber() == user.getAccountNumber()) { System.out.println("Cannot send to self."); return; }

        System.out.print("Enter amount to transfer: ");
        String s = sc.nextLine().trim();
        if (s.equalsIgnoreCase("b")) return;
        try {
            double amt = Double.parseDouble(s);
            double fee = 0.0;
            if (!isFavourite) fee = Math.ceil(amt / 1000.0) * 10.0; // 10 per 1000 (ceil)
            double total = amt + fee;
            if (amt <= 0) throw new NumberFormatException();
            if (total > user.getBalance()) { System.out.println("Insufficient balance (including fee)."); return; }

            // send request? no — direct transfer
            user.transferTo(receiver, amt, fee);
            bank.saveToFile();
            System.out.println("Transfer successful. New balance: " + user.getBalance());
            System.out.println(receiptString(user, "Transfer to " + receiver.getAccountNumber(), amt, fee));
        } catch (Exception ex) { System.out.println("Invalid amount."); }
    }

    private static void messagesFlow(User user) {
        while (true) {
            System.out.println("\n--- Messages & Requests --- (type 'b' to go back)");
            System.out.println("You have " + user.countUnreadMessages() + " unread messages.");
            System.out.println("1. View messages");
            System.out.println("2. Send money request to someone");
            System.out.println("b. Back");
            System.out.print("Choice: ");
            String c = sc.nextLine().trim();
            if (c.equalsIgnoreCase("b")) return;
            if (c.equals("1")) {
                List<Message> msgs = user.getMessages();
                if (msgs.isEmpty()) { System.out.println("No messages."); continue; }
                for (int i = 0; i < msgs.size(); i++) {
                    Message m = msgs.get(i);
                    System.out.println((i+1) + ". [" + (m.isRead() ? "Read" : "Unread") + "] From: " + m.getSender()
                            + " | " + m.getSummary() + " | at " + m.getTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                }
                System.out.print("Enter index to open (or b): ");
                String idx = sc.nextLine().trim();
                if (idx.equalsIgnoreCase("b")) continue;
                try {
                    int i = Integer.parseInt(idx)-1;
                    if (i < 0 || i >= msgs.size()) { System.out.println("Invalid."); continue; }
                    openMessage(user, msgs.get(i));
                } catch (Exception ex) { System.out.println("Invalid."); }
            } else if (c.equals("2")) {
                System.out.print("Enter receiver account number: ");
                String acc = sc.nextLine().trim(); if (acc.equalsIgnoreCase("b")) continue;
                int accNo; try { accNo = Integer.parseInt(acc); } catch (Exception e) { System.out.println("Invalid."); continue; }
                User r = bank.findByAccountNumber(accNo);
                if (r == null) { System.out.println("Receiver not found."); continue; }
                System.out.print("Amount to request: ");
                String amtS = sc.nextLine().trim(); if (amtS.equalsIgnoreCase("b")) continue;
                double amt; try { amt = Double.parseDouble(amtS); if (amt <= 0) throw new Exception(); } catch (Exception e) { System.out.println("Invalid."); continue; }
                System.out.print("Optional note: ");
                String note = sc.nextLine().trim();
                Message request = Message.createRequest(user.getUserId(), amt, note);
                r.addMessage(request);
                bank.saveToFile();
                System.out.println("Request sent.");
            } else System.out.println("Invalid.");
        }
    }

    private static void openMessage(User user, Message m) {
        System.out.println("\n---- MESSAGE ----");
        System.out.println("From: " + m.getSender());
        System.out.println("Type: " + m.getType());
        System.out.println("Time: " + m.getTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        System.out.println("Content: " + m.getContent());
        if (!m.isRead()) { m.setRead(true); bank.saveToFile(); }
        if (m.getType() == Message.Type.REQUEST) {
            System.out.println("1. Accept request");
            System.out.println("2. Reject request");
            System.out.println("b. back");
            System.out.print("Choice: ");
            String ch = sc.nextLine().trim();
            if (ch.equals("1")) {
                // find sender user by id
                User sender = bank.findByUserId(m.getSender());
                if (sender == null) { System.out.println("Original requester not found."); return; }
                double amt = m.getAmount();
                if (user.getBalance() < amt) {
                    System.out.println("Insufficient balance to accept.");
                    return;
                }
                // transfer without fee for acceptance? fee applies normally; treat as normal transfer (not favorite)
                double fee = Math.ceil(amt / 1000.0) * 10.0;
                user.transferTo(sender, amt, fee);
                bank.saveToFile();
                System.out.println("Request accepted and money sent. Receipt:");
                System.out.println(receiptString(user, "Request Payment to " + sender.getAccountNumber(), amt, fee));
            } else if (ch.equals("2")) {
                System.out.println("Request rejected.");
                // optionally notify sender
                User s = bank.findByUserId(m.getSender());
                if (s != null) s.addMessage(new Message("SYSTEM", "Your money request to " + user.getUserId() + " was rejected."));
                bank.saveToFile();
            }
        }
    }

    private static void profileFlow(User user) {
        while (true) {
            System.out.println("\n--- Profile --- (type 'b' to go back)");
            System.out.println("1. View details");
            System.out.println("2. Change password");
            System.out.println("3. Manage favourites");
            System.out.println("b. Back");
            System.out.print("Choice: ");
            String c = sc.nextLine().trim();
            if (c.equalsIgnoreCase("b")) return;
            if (c.equals("1")) {
                System.out.println("UserID: " + user.getUserId());
                System.out.println("Name: " + user.getName());
                System.out.println("Username: " + user.getUsername());
                System.out.println("Account#: " + user.getAccountNumber());
                System.out.println("Balance: " + user.getBalance());
                System.out.println("Favorites: " + user.getFavorites());
            } else if (c.equals("2")) {
                System.out.print("Enter new password: ");
                String np = sc.nextLine().trim();
                user.setPassword(np);
                bank.saveToFile();
                System.out.println("Password changed.");
            } else if (c.equals("3")) manageFavorites(user);
            else System.out.println("Invalid.");
        }
    }

    private static void manageFavorites(User user) {
        while (true) {
            System.out.println("\n--- Favourites --- (max 5) type 'b' to back");
            System.out.println("Current favourites: " + user.getFavorites());
            System.out.println("1. Add favourite (by account#)");
            System.out.println("2. Remove favourite (by index)");
            System.out.println("b. Back");
            System.out.print("Choice: ");
            String c = sc.nextLine().trim();
            if (c.equalsIgnoreCase("b")) return;
            if (c.equals("1")) {
                if (user.getFavorites().size() >= 5) { System.out.println("Max 5 reached."); continue; }
                System.out.print("Enter account number to add: ");
                String a = sc.nextLine().trim();
                if (a.equalsIgnoreCase("b")) continue;
                try {
                    int acc = Integer.parseInt(a);
                    User found = bank.findByAccountNumber(acc);
                    if (found == null) { System.out.println("Not found."); continue; }
                    if (found.getAccountNumber() == user.getAccountNumber()) { System.out.println("Cannot add self."); continue; }
                    user.addFavorite(acc);
                    bank.saveToFile();
                    System.out.println("Added.");
                } catch (Exception ex) { System.out.println("Invalid."); }
            } else if (c.equals("2")) {
                System.out.print("Enter index to remove (1-based): ");
                String idx = sc.nextLine().trim();
                if (idx.equalsIgnoreCase("b")) continue;
                try {
                    int i = Integer.parseInt(idx)-1;
                    if (user.removeFavoriteAt(i)) { bank.saveToFile(); System.out.println("Removed."); }
                    else System.out.println("Invalid index.");
                } catch (Exception ex) { System.out.println("Invalid."); }
            } else System.out.println("Invalid.");
        }
    }

    private static void printTransactions(User user) {
        List<Transaction> t = user.getTransactions();
        if (t.isEmpty()) { System.out.println("No transactions."); return; }
        System.out.println("\n--- Transaction History ---");
        for (Transaction tr : t) System.out.println(tr.toString());
    }

    //Admin Panel
    private static void adminPanel() {
        while (true) {
            System.out.println("\n--- ADMIN PANEL ---");
            System.out.println("1. View all users");
            System.out.println("2. Search user by ID");
            System.out.println("3. Delete user");
            System.out.println("4. Total bank balance");
            System.out.println("5. Total number of accounts");
            System.out.println("6. Backup data");
            System.out.println("7. Exit admin");
            System.out.print("Choice: ");
            String c = sc.nextLine().trim();
            switch (c) {
                case "1" -> bank.printAllUsers();
                case "2" -> {
                    System.out.print("Enter User ID: "); String id = sc.nextLine().trim();
                    User u = bank.findByUserId(id);
                    if (u == null) System.out.println("Not found."); else {
                        System.out.println("Found: " + u.getBrief());
                    }
                }
                case "3" -> {
                    System.out.print("Enter User ID to delete: "); String id = sc.nextLine().trim();
                    if (id.equals(ADMIN_ID)) { System.out.println("Cannot delete admin."); break; }
                    boolean ok = bank.deleteUserById(id);
                    if (ok) { bank.saveToFile(); System.out.println("Deleted."); } else System.out.println("Not found.");
                }
                case "4" -> System.out.println("Total bank balance: " + bank.totalBalance());
                case "5" -> System.out.println("Total accounts: " + bank.countUsers());
                case "6" -> { bank.backup(); System.out.println("Backup created."); }
                case "7" -> { bank.saveToFile(); return; }
                default -> System.out.println("Invalid.");
            }
        }
    }

  // recept
    private static String receiptString(User user, String type, double amount, double fee) {
        String id = "TXN-" + System.currentTimeMillis();
        String sb = "----- RECEIPT -----\n" +
                "Transaction ID: " + id + "\n" +
                "User: " + user.getUserId() + "\n" +
                "Type: " + type + "\n" +
                "Amount: " + amount + "\n" +
                "Fee: " + fee + "\n" +
                "Balance after: " + user.getBalance() + "\n" +
                "Date: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "\n" +
                "-------------------";
        // save as transaction note already done by user methods
        return sb;
    }
}
