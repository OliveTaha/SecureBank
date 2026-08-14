package SecureBank;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type { INFO, REQUEST }

    private String sender; // userId or SYSTEM
    private Type type;
    private String content;
    private boolean read = false;
    private LocalDateTime time;
    // for request
    private double amount;

    public Message(String sender, String content) {
        this.sender = sender; this.content = content; this.type = Type.INFO; this.time = LocalDateTime.now();
    }

    public static Message createRequest(String fromUserId, double amount, String note) {
        Message m = new Message(fromUserId, "Money request: " + amount + " BDT. Note: " + note);
        m.type = Type.REQUEST; m.amount = amount;
        m.time = LocalDateTime.now();
        return m;
    }

    public String getSender() { return sender; }
    public String getContent() { return content; }
    public LocalDateTime getTime() { return time; }
    public boolean isRead() { return read; }
    public void setRead(boolean r) { read = r; }
    public Type getType() { return type; }
    public double getAmount() { return amount; }
    public String getSummary() { return content.length() > 40 ? content.substring(0,40)+"..." : content; }
}
