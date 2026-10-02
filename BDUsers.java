import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class BDUsers {
    private static final String USERS_FILE = "user.txt";
    private static final String PENDING_REQUESTS_FILE = "pendingRequest.txt";
    private static final String LOG_FILE = "log.txt";

    // Carregar Users do ficheiro
    public static synchronized Map<String, User> loadUsers() {
        Map<String, User> users = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 4);
                if (parts.length == 4) {
                    users.put(parts[1], new User(parts[0], parts[1], parts[2], parts[3]));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Ficheiro de utilizadores não encontrado. Será criado no primeiro registo.");
        } catch (IOException e) {
            System.out.println("Erro carregar Users");
        }
        return users;
    }

    // Guardar users Ficheiro
    public static synchronized void saveUser(User user) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE, true))) {
            writer.println(formatUser(user));
        } catch (Exception e) {
            System.out.println("Erro guardar Users");
        }
    }

    public static synchronized void replaceUser(User updatedUser) {
        Map<String, User> users = loadUsers();
        users.put(updatedUser.getEmail(), updatedUser);
        saveAllUsers(users);
    }

    private static void saveAllUsers(Map<String, User> users) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (User user : users.values()) {
                writer.println(formatUser(user));
            }
        } catch (IOException e) {
            System.out.println("Erro guardar Users");
        }
    }

    private static String formatUser(User user) {
        return user.getNome() + "," + user.getEmail() + "," + user.getPass() + "," + user.getPerfil();
    }

    // Carregar Pedidos Pendentes Ficheiro
    public static synchronized List<String[]> loadPendingRequest() {
        List<String[]> pendingRequests = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(PENDING_REQUESTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 4);
                if (parts.length == 4) {
                    pendingRequests.add(parts);
                }
            }
        } catch (FileNotFoundException e) {
            return pendingRequests;
        } catch (Exception e) {
            System.out.println("Erro carregar Pedidos");
        }
        return pendingRequests;
    }

    // Guardar Pedidos Pendentes Ficheiro
    public static synchronized void savePendingRequest(String nome, String email, String pass, String perfil) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(PENDING_REQUESTS_FILE, true))) {
            writer.println(nome + "," + email + "," + PasswordUtils.hashIfNeeded(pass) + "," + perfil);
            System.out.println("Pedido de registo salvo: " + nome + ", " + email + ", " + perfil);
        } catch (Exception e) {
            System.out.println("Erro guardar pedido:" + e.getMessage());
        }
    }

    // Eliminar Pedidos Pendentes
    public static synchronized void deletePendingRequest(String email) {
        List<String[]> pendingRequests = loadPendingRequest();

        try (PrintWriter writer = new PrintWriter(new FileWriter(PENDING_REQUESTS_FILE))) {
            for (String[] request : pendingRequests) {
                if (!request[1].equalsIgnoreCase(email)) {
                    writer.println(String.join(",", request));
                }
            }
        } catch (Exception e) {
            System.out.println("Erro guardar Users: " + e.getMessage());
        }
    }

    //Faz logs mensagens
    public static synchronized void logMessage(String message) {
        try (PrintWriter logWriter = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            String time = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
            logWriter.println(time + " - " + message);
        } catch (Exception e) {
            System.out.println("Erro registar log");
        }
    }

    //Realiza pedido para mudar perfil
    public static synchronized void savePendingProfileChange(String email, String novoPerfil) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("profileChangeRequests.txt", true))) {
            writer.write(email + ";" + novoPerfil);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Erro ao salvar pedido de alteração de perfil: " + e.getMessage());
        }
    }

    public static synchronized long countLogEntries() {
        File logFile = new File(LOG_FILE);
        if (!logFile.exists()) {
            return 0;
        }

        long count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            while (reader.readLine() != null) {
                count++;
            }
        } catch (IOException e) {
            System.out.println("Erro ao contar operações do log");
        }
        return count;
    }
}
