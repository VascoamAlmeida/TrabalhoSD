import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.System.out;

public class MultiServer {

    // Guarda clientes conectados.
    protected static final Map<Socket, PrintWriter> clientWriters = new HashMap<>();
    // Guarda os utilizadores registados.
    protected static final Map<String, User> userRegistados = BDUsers.loadUsers();
    // Guarda clientes com pedidos de registo pendentes enquanto permanecem online.
    protected static final Map<String, PrintWriter> clientesPendentes = new HashMap<>();
    // Guarda utilizadores autenticados.
    protected static final Map<String, PrintWriter> loggedInUsers = new HashMap<>();

    private static final ScheduledExecutorService reportScheduler =
            Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(runnable, "periodic-report");
                thread.setDaemon(true);
                return thread;
            });

    public static synchronized void addClient(Socket socket, PrintWriter writer) {
        clientWriters.put(socket, writer);
    }

    public static synchronized void addClientLogged(Socket socket, PrintWriter writer, String email) {
        clientWriters.put(socket, writer);
        loggedInUsers.put(email, writer);
    }

    public static synchronized void removeClientLogged(PrintWriter writer, String email) {
        clientWriters.entrySet().removeIf(entry -> entry.getValue().equals(writer));
        if (email != null && !email.isBlank()) {
            loggedInUsers.remove(email);
        }
    }

    public static synchronized void removeClient(PrintWriter writer) {
        clientWriters.entrySet().removeIf(entry -> entry.getValue().equals(writer));
    }

    public static synchronized void sendMessageToAll(String message) {
        BDNotifications.saveNotification(message);
        BDUsers.logMessage("Alerta global: " + message);
        for (PrintWriter writer : loggedInUsers.values()) {
            writer.println(message);
        }
    }

    public static synchronized int getLoggedInUserCount() {
        return loggedInUsers.size();
    }

    public static synchronized boolean isRegisteredUser(String email) {
        return email != null && userRegistados.containsKey(email);
    }

    public static synchronized Map<String, User> getRegisteredUsersSnapshot() {
        return new HashMap<>(userRegistados);
    }

    public static void main(String[] args) throws IOException {
        int port = 666;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            out.println("Servidor na porta: " + port);
            startPeriodicReports();

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    new ProcessaCliente(clientSocket).start();
                } catch (IOException e) {
                    System.err.println("Erro ao tentar conectar a um cliente.");
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Não foi possivel ligar a porta: " + port);
            throw e;
        }
    }

    /**
     * Notifica os utilizadores de perfil alto que estejam online acerca de um pedido de registo pendente.
     */
    public static synchronized void noficationAdmin(String mensagem, String email, PrintWriter clientWriter) {
        clientesPendentes.put(email, clientWriter);
        notifyHighProfiles(mensagem);
        out.println("Pedido pendente: " + email);
    }

    /**
     * Envia uma notificação aos utilizadores de perfil alto atualmente autenticados.
     */
    public static synchronized void notifyHighProfiles(String mensagem) {
        for (Map.Entry<String, PrintWriter> entry : loggedInUsers.entrySet()) {
            User user = userRegistados.get(entry.getKey());
            if (user != null && "alto".equalsIgnoreCase(user.getPerfil())) {
                entry.getValue().println(mensagem);
            }
        }
    }

    /**
     * Processa a resposta a um pedido de registo e, se o cliente ainda estiver online, informa-o.
     */
    public static synchronized void respostaAdmin(String email, boolean aprovado, String perfil, String nome,
                                                   String pass, PrintWriter clientWriter) {
        if (aprovado) {
            User user = new User(nome, email, PasswordUtils.hashIfNeeded(pass), perfil);
            userRegistados.put(email, user);
            BDUsers.saveUser(user);
            if (clientWriter != null) {
                clientWriter.println("Registo Aprovado");
            }
        } else if (clientWriter != null) {
            clientWriter.println("Registo Reprovado");
        }

        BDUsers.deletePendingRequest(email);
        clientesPendentes.remove(email);
    }

    /**
     * Regista um utilizador. O perfil geral fica imediatamente ativo; os restantes aguardam aprovação.
     */
    public static synchronized boolean registarUser(String nome, String email, String pass, String perfil) {
        BDUsers.logMessage("Tentativa de registo por: " + email + " com perfil " + perfil);

        if (email == null || email.isBlank() || pass == null || pass.isBlank() || userRegistados.containsKey(email)) {
            return false;
        }

        List<String[]> pendingRequests = BDUsers.loadPendingRequest();
        for (String[] req : pendingRequests) {
            if (req.length > 1 && req[1].equalsIgnoreCase(email)) {
                return false;
            }
        }

        String protectedPassword = PasswordUtils.hashPassword(pass);
        if ("geral".equalsIgnoreCase(perfil)) {
            User user = new User(nome, email, protectedPassword, "geral");
            userRegistados.put(email, user);
            BDUsers.saveUser(user);
        } else {
            BDUsers.savePendingRequest(nome, email, protectedPassword, perfil);
        }
        return true;
    }

    public static synchronized boolean authUser(String email, String pass) {
        BDUsers.logMessage("Tentativa de login por: " + email);

        if (loggedInUsers.containsKey(email)) {
            out.println("Este utilizador já está autenticado noutra sessão.");
            return false;
        }

        User user = userRegistados.get(email);
        if (user == null || !PasswordUtils.verifyPassword(pass, user.getPass())) {
            return false;
        }

        // Migra automaticamente ficheiros antigos que ainda guardem passwords em texto simples.
        if (!PasswordUtils.isHashed(user.getPass())) {
            User migratedUser = new User(user.getNome(), user.getEmail(), PasswordUtils.hashPassword(pass), user.getPerfil());
            userRegistados.put(email, migratedUser);
            BDUsers.replaceUser(migratedUser);
        }
        return true;
    }

    private static void startPeriodicReports() {
        reportScheduler.scheduleAtFixedRate(MultiServer::sendPeriodicReport, 60, 60, TimeUnit.SECONDS);
    }

    private static synchronized void sendPeriodicReport() {
        String report = "[RELATÓRIO PERIÓDICO] Utilizadores ativos: " + loggedInUsers.size()
                + " | Utilizadores registados: " + userRegistados.size()
                + " | Operações registadas: " + BDUsers.countLogEntries();

        BDUsers.logMessage(report);
        for (Map.Entry<String, PrintWriter> entry : loggedInUsers.entrySet()) {
            User user = userRegistados.get(entry.getKey());
            if (user != null && "alto".equalsIgnoreCase(user.getPerfil())) {
                entry.getValue().println(report);
            }
        }
    }
}
