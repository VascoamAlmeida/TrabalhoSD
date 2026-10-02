import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationService {
    private static final Map<String, Socket> activeUsers = new ConcurrentHashMap<>();

    public static void addUserSocket(String email, Socket socket) {
        activeUsers.put(email, socket);
    }

    public static void removeUserSocket(String email) {
        activeUsers.remove(email);
    }

    public static void sendNotification(String email, String message) {
        sendToOnlineUser(email, "Nova mensagem no grupo: " + message);
    }

    public static void sendDirectNotification(String email, String senderEmail, String message) {
        sendToOnlineUser(email, "Nova mensagem privada de " + senderEmail + ": " + message);
    }

    private static void sendToOnlineUser(String email, String message) {
        Socket socket = activeUsers.get(email);
        if (socket != null && !socket.isClosed()) {
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(message);
            } catch (IOException e) {
                System.err.println("Erro ao enviar mensagem para " + email + ": " + e.getMessage());
            }
        } else {
            System.out.println("Utilizador: " + email + " não está online. Mensagem guardada no histórico.");
        }
    }
}
