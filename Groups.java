import java.time.LocalDateTime;
import java.util.Arrays;

public class Groups {

    private int id;
    private String name;
    private String[] userEmail;

    public String[] getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String[] userEmail) {
        this.userEmail = userEmail;
    }

    public Groups (int id, String name, String[] userEmail){
        this.id = id;
        this.name = name;
        this.userEmail = userEmail;
    }

    public int getId(){
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void broadcastMessage(String message, String senderEmail){
        if(Arrays.asList(userEmail).contains(senderEmail)){
            Message newMessage = new Message(id, senderEmail, message, LocalDateTime.now());
            BDMessages.saveMessage(newMessage);

            for(String email : userEmail){
                if(!email.equals(senderEmail)){
                    NotificationService.sendNotification(email, message);
                }
            }
        }else {
            throw new IllegalArgumentException("Utilizador não autorizado a enviar mensagem");
        }
    }

    public String toString(){
        return "ID: " + id + ", Nome: " + name + ", Emails: " + Arrays.toString(userEmail);
    }
}
