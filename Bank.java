package SecureBank;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Bank implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<User> users = new ArrayList<>();
    private final String DATA_FILE = "bankData.ser";

    // create/load
    public Bank() {}

    public static Bank loadFromFile() {
        File f = new File("bankData.ser");
        if (!f.exists()) {
            Bank b = new Bank();
            b.saveToFile();
            System.out.println("New bank created at: " + f.getAbsolutePath());
            return b;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (Bank) ois.readObject();
        } catch (Exception e) {
            System.out.println("Load failed, starting fresh. (" + e.getMessage() + ")");
            Bank b = new Bank();
            b.saveToFile();
            return b;
        }
    }

    public void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("bankData.ser"))) {
            oos.writeObject(this);
        } catch (Exception e) { System.out.println("Save error: " + e.getMessage()); }
    }

    // user creation helper
    public User createUser(String username, String password, double balance, String name, String securityKey) {
        String uid;
        do { uid = "@" + username.toLowerCase() + (100 + (int)(Math.random()*900)); } while (findByUserId(uid) != null);
        int acc;
        do { acc = 2000 + (int)(Math.random()*8000); } while (findByAccountNumber(acc) != null);
        User u = new User(uid, username, password, balance, acc, name, securityKey);
        users.add(u);
        // add initial transaction
        u.addTransaction(new Transaction("Account Created", balance, LocalDateTime.now(), "Initial deposit"));
        return u;
    }

    public User findByUserId(String uid) {
        for (User u : users) if (u.getUserId().equals(uid)) return u;
        return null;
    }

    public User findByUsername(String username) {
        for (User u : users) if (u.getUsername().equalsIgnoreCase(username)) return u;
        return null;
    }

    public User findByAccountNumber(int acc) {
        for (User u : users) if (u.getAccountNumber() == acc) return u;
        return null;
    }

    public List<User> getUsers() { return users; }

    public void printAllUsers() {
        if (users.isEmpty()) { System.out.println("No users."); return; }
        System.out.println("--- Users ---");
        for (User u : users) System.out.println(u.getBrief());
    }

    public boolean deleteUserById(String uid) {
        User u = findByUserId(uid);
        if (u == null) return false;
        users.remove(u);
        return true;
    }

    public double totalBalance() {
        double s = 0.0;
        for (User u : users) s += u.getBalance();
        return s;
    }

    public int countUsers() { return users.size(); }

    // backup (serialize copy)
    public void backup() {
        String name = "backup_" + System.currentTimeMillis() + ".ser";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(name))) {
            oos.writeObject(this);
        } catch (Exception e) { System.out.println("Backup error: " + e.getMessage()); }
    }
}
