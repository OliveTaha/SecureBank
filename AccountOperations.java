package SecureBank;

public interface AccountOperations {
    void deposit(double amount);
    void withdraw(double amount);
    void transfer(User receiver, double amount);
    double viewBalance();
}
