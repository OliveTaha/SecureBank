package SecureBank;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    private String type;
    private double amount;
    private LocalDateTime time;
    private String detail;

    public Transaction(String type, double amount, LocalDateTime time, String detail) {
        this.type = type; this.amount = amount; this.time = time; this.detail = detail;
    }

    @Override
    public String toString() {
        return "[" + time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "] " + type + " : " + amount + " | " + detail;
    }
}
