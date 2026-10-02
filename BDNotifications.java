import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class BDNotifications {
    private static final String NOTIFICATIONS_FILE = "notifications.txt";

    public static synchronized void saveNotification(String message) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(NOTIFICATIONS_FILE, true))) {
            String encoded = Base64.getEncoder().encodeToString(message.getBytes(StandardCharsets.UTF_8));
            writer.println(LocalDateTime.now() + ";" + encoded);
        } catch (IOException e) {
            System.err.println("Erro ao guardar notificação: " + e.getMessage());
        }
    }

    public static synchronized List<String> loadRecentNotifications(int limit) {
        List<String> all = new ArrayList<>();
        File file = new File(NOTIFICATIONS_FILE);
        if (!file.exists() || limit <= 0) {
            return all;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String parsed = parseNotification(line);
                if (parsed != null) {
                    all.add(parsed);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar notificações: " + e.getMessage());
        }

        int start = Math.max(0, all.size() - limit);
        return new ArrayList<>(all.subList(start, all.size()));
    }

    private static String parseNotification(String line) {
        try {
            String[] parts = line.split(";", 2);
            if (parts.length != 2) {
                return null;
            }
            String message = new String(Base64.getDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            return "[" + parts[0] + "] " + message;
        } catch (RuntimeException e) {
            return null;
        }
    }
}
