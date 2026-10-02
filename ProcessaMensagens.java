import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ProcessaMensagens {
    private final PrintWriter out;
    private final BufferedReader in;
    private final User loggedInUser;

    public ProcessaMensagens(PrintWriter out, BufferedReader in, User loggedInUser) {
        this.out = out;
        this.in = in;
        this.loggedInUser = loggedInUser;
    }

    public void handlePrivateMessages() throws IOException {
        int action;
        do {
            out.println("");
            out.println("Mensagens privadas:");
            out.println("1- Enviar mensagem");
            out.println("2- Ver histórico de mensagens");
            out.println("3- Ver utilizadores registados");
            out.println("0- Voltar");
            out.println("Digite uma opção:");

            try {
                action = Integer.parseInt(in.readLine());
            } catch (NumberFormatException e) {
                action = -1;
            }

            switch (action) {
                case 1:
                    sendDirectMessage();
                    break;
                case 2:
                    viewMessageHistory();
                    break;
                case 3:
                    viewRegisteredUsers();
                    break;
                case 0:
                    break;
                default:
                    out.println("Comando inválido.");
            }
        } while (action != 0);
    }

    private void sendDirectMessage() throws IOException {
        out.println("Email do destinatário:");
        String recipient = in.readLine().trim();

        if (recipient.equalsIgnoreCase(loggedInUser.getEmail())) {
            out.println("Não pode enviar uma mensagem privada para si próprio.");
            return;
        }

        if (!MultiServer.isRegisteredUser(recipient)) {
            out.println("Utilizador não encontrado.");
            return;
        }

        out.println("Mensagem:");
        String content = in.readLine();
        if (content == null || content.isBlank()) {
            out.println("A mensagem não pode estar vazia.");
            return;
        }

        DirectMessage message = new DirectMessage(
                loggedInUser.getEmail(), recipient, content, LocalDateTime.now());
        BDDirectMessages.saveMessage(message);
        NotificationService.sendDirectNotification(recipient, loggedInUser.getEmail(), content);
        BDUsers.logMessage("Mensagem privada enviada de " + loggedInUser.getEmail() + " para " + recipient);
        out.println("Mensagem privada guardada e enviada com sucesso.");
    }

    private void viewMessageHistory() {
        List<DirectMessage> messages = BDDirectMessages.loadMessagesForUser(loggedInUser.getEmail());
        if (messages.isEmpty()) {
            out.println("Não existem mensagens privadas no histórico.");
            return;
        }

        out.println("Histórico de mensagens privadas:");
        for (DirectMessage message : messages) {
            String direction = loggedInUser.getEmail().equalsIgnoreCase(message.getSenderEmail())
                    ? "Enviada para " + message.getRecipientEmail()
                    : "Recebida de " + message.getSenderEmail();
            out.println(direction + " [" + message.getTimestamp() + "]");
            out.println(message.getContent());
            out.println("---");
        }
    }

    private void viewRegisteredUsers() {
        Map<String, User> users = MultiServer.getRegisteredUsersSnapshot();
        out.println("Utilizadores registados:");
        users.values().stream()
                .filter(user -> !user.getEmail().equalsIgnoreCase(loggedInUser.getEmail()))
                .sorted((first, second) -> first.getEmail().compareToIgnoreCase(second.getEmail()))
                .forEach(user -> out.println(user.getEmail() + " - " + user.getPerfil()));
    }
}
