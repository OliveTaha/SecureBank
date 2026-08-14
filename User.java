package SecureBank;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable, AccountOperations {
    private static final long serialVersionUID = 1L;

    private String userId;
    private String username;
    private String password;
    private double balance;
    private int accountNumber;
     private String name;
     private String securityKey;
      private List<Transaction> transactions = new ArrayList<>();
    private List<Message> messages = new ArrayList<>();
     private List<Integer> favorites = new ArrayList<>(); // account numbers (max 5)

    public User(String userId, String username, String password, double balance, int accountNumber, String name, String securityKey) {
        this.userId = userId; this.username = username; this.password = password; this.balance = balance;
        this.accountNumber = accountNumber; this.name = name; this.securityKey = securityKey;
    }

    // getters / setters
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public double getBalance() { return balance; }
    public int getAccountNumber() { return accountNumber; }
    public String getName() { return name; }
    public String getSecurityKey() { return securityKey; }
    public void setPassword(String p) { this.password = p; }
    public void setSecurityKey(String k) { this.securityKey = k; }

    public List<Transaction> getTransactions() { return transactions; }
    public List<Message> getMessages() { return messages; }
    public List<Integer> getFavorites() { return favorites; }

    public String getBrief() {
        return userId + " | " + name + " | Acc:" + accountNumber + " | Bal:" + balance;
    }

    // messages
    public void addMessage(Message m) {
        messages.add(m);
    }
    public int countUnreadMessages() {
        int c=0;
        for (Message m:messages)
            if (!m.isRead()) c++;
        return c;
    }

    // favorites
    public void addFavorite(int accNo) {
        if (!favorites.contains(accNo) && favorites.size() < 5) favorites.add(accNo);
    }
    public boolean removeFavoriteAt(int idx) {
        if (idx < 0 || idx >= favorites.size()) return false;
        favorites.remove(idx); return true;
    }

    // transactions & AccountOperations
    @Override
    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount positive required.");
        balance += amount;
        transactions.add(new Transaction("Deposit", amount, LocalDateTime.now(), "Self deposit"));
    }

    @Override
    public void withdraw(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount positive required.");
        if (amount > balance) throw new IllegalArgumentException("Insufficient balance.");
        balance -= amount;
        transactions.add(new Transaction("Withdraw", amount, LocalDateTime.now(), "Self withdraw"));
    }

    @Override
    public void transfer(User receiver, double amount) {
       // no use
    }

    public void transferTo(User receiver, double amount, double fee) {
        if (amount <= 0) throw new IllegalArgumentException("Amount positive required.");
        double total = amount + fee;
        if (total > balance) throw new IllegalArgumentException("Insufficient balance.");
        balance -= total;
        receiver.balance += amount;
        Transaction t = new Transaction("Transfer Sent", amount, LocalDateTime.now(), "To " + receiver.getAccountNumber() + " fee:" + fee);
        transactions.add(t);
        receiver.transactions.add(new Transaction("Transfer Received", amount, LocalDateTime.now(), "From " + this.accountNumber));
        // receipts also stored as transactions (already above)
        // notify receiver
        receiver.addMessage(new Message(this.userId, "You received " + amount + " BDT from " + this.userId));
    }

    public void addTransaction(Transaction t) { transactions.add(t); }

    @Override
    public double viewBalance() { return balance; }
}
