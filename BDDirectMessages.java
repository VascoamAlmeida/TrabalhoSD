import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class BDDirectMessages {
    private static final String DIRECT_MESSAGES_FILE = "directMessages.txt";

    public static synchronized void saveMessage(DirectMessage message) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DIRECT_MESSAGES_FILE, true))) {
            String content = Base64.getEncoder().encodeToString(
                    message.getContent().getBytes(StandardCharsets.UTF_8));
            writer.println(message.getSenderEmail() + ";" + message.getRecipientEmail() + ";"
                    + message.getTimestamp() + ";" + content);
        } catch (IOException e) {
            System.err.println("Erro ao guardar mensagem privada: " + e.getMessage());
        }
    }

    public static synchronized List<DirectMessage> loadMessagesForUser(String email) {
        List<DirectMessage> messages = new ArrayList<>();
        File file = new File(DIRECT_MESSAGES_FILE);
        if (!file.exists()) {
            return messages;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                DirectMessage message = parseMessage(line);
                if (message != null && (email.equalsIgnoreCase(message.getSenderEmail())
                        || email.equalsIgnoreCase(message.getRecipientEmail()))) {
                    messages.add(message);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar mensagens privadas: " + e.getMessage());
        }
        return messages;
    }

    private static DirectMessage parseMessage(String line) {
        try {
            String[] parts = line.split(";", 4);
            if (parts.length != 4) {
                return null;
            }
            String content = new String(Base64.getDecoder().decode(parts[3]), StandardCharsets.UTF_8);
            return new DirectMessage(parts[0], parts[1], content, LocalDateTime.parse(parts[2]));
        } catch (RuntimeException e) {
            System.err.println("Mensagem privada inválida ignorada.");
            return null;
        }
    }
}
