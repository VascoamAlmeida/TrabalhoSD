import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class BDGroups {
    private static final String GROUPS_FILE = "groups.txt";
    private static final String PENDING_REQUESTS_G_FILE = "pendingRequestGroups.txt";

    // Carregar Users do ficheiro
    public static synchronized Map<Integer, Groups> loadGroups() {
        Map<Integer, Groups> groups = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(GROUPS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) { // Ensure enough parts for ID, name, and emails
                    int id = Integer.parseInt(parts[0].trim());
                    String name = parts[1].trim();
                    String[] userEmails = parts[2].trim().split(";"); // Emails are separated by semicolons
                    groups.put(id, new Groups(id, name, userEmails));
                }
            }
        } catch (IOException e) {
            System.out.println("Erro carregar Groups");
        } catch (NumberFormatException e) {
            System.out.println("Erro ao converter ID para número");
        }
        return groups;
    }

    public static synchronized void saveGroup(Groups group) {
        Map<Integer, Groups> groups = loadGroups();
        groups.put(group.getId(), group);
        saveAllGroups(groups);
    }

    // Delete a group by its ID
    public static synchronized void deleteGroup(int groupId) {
        Map<Integer, Groups> groups = loadGroups();
        groups.remove(groupId);
        saveAllGroups(groups);
    }

    // Save all groups to the file (helper method)
    private static synchronized void saveAllGroups(Map<Integer, Groups> groups) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(GROUPS_FILE))) {
            for (Groups group : groups.values()) {
                StringBuilder groupData = new StringBuilder(group.getId() + "," + group.getName() + ",");
                groupData.append(String.join(";", group.getUserEmail())); // Use semicolon to join emails
                writer.println(groupData);
            }
        } catch (Exception e) {
            System.out.println("Erro ao salvar todos os grupos: " + e.getMessage());
        }
    }

    public static synchronized List<String[]> loadPendingRequestG() {
        List<String[]> pendingRequestG = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(PENDING_REQUESTS_G_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) { // Ensure enough parts for ID, name, and emails
                    pendingRequestG.add(parts);
                    System.out.println("Pedido carregado: " + Arrays.toString(parts));
                }
            }
        } catch (IOException e) {
            System.out.println("Erro carregar Pedidos");
        }
        return pendingRequestG;
    }

    public static synchronized void savePendingRequestG(Groups group) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(PENDING_REQUESTS_G_FILE, true))) {
            writer.println(group.getId() + "," + group.getName() + "," +
                    String.join(";", group.getUserEmail()));
            System.out.println("Pedido de criação de grupo guardado: " + group.getId() + "," +
                    group.getName() + "," + String.join(";", group.getUserEmail()));

        } catch (Exception e) {
            System.out.println("Erro guardar pedido: " + e.getMessage());
        }
    }

    public static synchronized void approveGroupRequest(int groupId) {
        List<String[]> pendingRequests = loadPendingRequestG();

        for (String[] request : pendingRequests) {
            if (Integer.parseInt(request[0]) == groupId) {
                // Extract details from the request
                int id = Integer.parseInt(request[0]);
                String name = request[1];
                String[] members = request[2].split(";");

                // Save group in the database
                Groups newGroup = new Groups(id, name, members);
                saveGroup(newGroup);

                // Remove the approved request from pending requests
                deletePendingRequest(groupId);
                System.out.println("Pedido de criação de grupo aprovado e salvo: " + name);
                return;
            }
        }
        System.out.println("Pedido de criação de grupo não encontrado para o ID: " + groupId);
    }

    public static synchronized void deletePendingRequest(int groupId) {
        List<String[]> pendingRequests = loadPendingRequestG();

        try (PrintWriter writer = new PrintWriter(new FileWriter(PENDING_REQUESTS_G_FILE))) {
            for (String[] request : pendingRequests) {
                if (Integer.parseInt(request[0]) != groupId) {
                    writer.println(String.join(",", request));
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao salvar Pedidos Pendentes: " + e.getMessage());
        }
    }

}