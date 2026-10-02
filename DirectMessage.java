import java.time.LocalDateTime;

public class DirectMessage {
    private final String senderEmail;
    private final String recipientEmail;
    private final String content;
    private final LocalDateTime timestamp;

    public DirectMessage(String senderEmail, String recipientEmail, String content, LocalDateTime timestamp) {
        this.senderEmail = senderEmail;
        this.recipientEmail = recipientEmail;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return senderEmail + " -> " + recipientEmail + " [" + timestamp + "]\n" + content;
    }
}
