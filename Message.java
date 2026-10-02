import java.time.LocalDateTime;

public class Message {
    private int groupId;
    private String senderEmail;
    private String content;
    private LocalDateTime timestamp;

    public Message(int groupId, String senderEmail, String content, LocalDateTime timestamp) {
        this.groupId = groupId;
        this.senderEmail = senderEmail;
        this.content = content;
        this.timestamp = timestamp;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return senderEmail + " [" + timestamp + "]\n" +
                content;
    }
}
