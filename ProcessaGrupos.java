import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ProcessaGrupos{
    private PrintWriter out;
    private BufferedReader in;
    private User loggedInUser;

    public ProcessaGrupos(PrintWriter out, BufferedReader in, User loggedInUser){
        this.out = out;
        this.in = in;
        this.loggedInUser = loggedInUser;
    }
    public void handleGroupOperations() throws IOException{
        out.println("");
        out.println("Bem-vindo à gestão de grupos.");
        int action;
        while(true){
            out.println("Opções disponíveis:");
            out.println("1- Criar grupo");
            out.println("2- Editar grupo");
            out.println("3- Apagar Grupo");
            out.println("4- Ver Grupos");
            out.println("5- Ver Pedidos de criação");
            out.println("6- Enviar mensagem para grupo");
            out.println("7- Ver mensagens do grupo");
            out.println("0 - Sair");
            out.println("Digite uma opção:");

            try{
                action = Integer.parseInt(in.readLine());
            }catch (NumberFormatException e){
                out.println("Entrada inválida! Por favor insira um número: ");
                continue;
            }

            if(action == 0){
                out.println("Saíndo da gestão de grupos...");
                break;
            }

            switch (action){
                case 1:
                    createGroup();
                    break;
                case 2:
                    editGroup();
                    break;
                case 3:
                    deleteGroup();
                    break;
                case 4:
                    viewGroups();
                    break;
                case 5:
                    approveGroup();
                    break;
                case 6:
                    sendMessageToGroup();
                    break;
                case 7:
                    viewGroupMessages();
                    break;
                default:
                    out.println("Comando inválido.");
            }
        }
    }

    private void createGroup() throws IOException{
        out.println("Indique o ID do grupo: ");
        int id = Integer.parseInt(in.readLine());

        out.println("Indique o nome do grupo: ");
        String nome = in.readLine();

        out.println("Indique os emails dos membros separados por ';': ");
        String[] emails = in.readLine().split(";");

        List<String> memberList = new ArrayList<>(Arrays.asList(emails));
        if (!memberList.contains(loggedInUser.getEmail())){
            memberList.add(loggedInUser.getEmail());
        }

        Groups newGroup = new Groups(id, nome, memberList.toArray(new String[0]));
        BDGroups.savePendingRequestG(newGroup);
        MultiServer.notifyHighProfiles(
                "Pedido registo grupo: \n" +
                        "ID: " + id +
                        "\nNome: " + nome +
                        "\nMembros: " + Arrays.toString(emails) +
                        "\nAceda à gestão de grupos para aprovar.");
        out.println("Espera aceitação de Perfil superior.");
    }

    private void approveGroup() throws IOException{
        if("medio".equalsIgnoreCase(loggedInUser.getPerfil()) || "alto".equalsIgnoreCase(loggedInUser.getPerfil())){
            List<String[]> pendingRequests = BDGroups.loadPendingRequestG();
            if (pendingRequests.isEmpty()){
                out.println("Não há pedidos pendentes para aprovação");
                return;
            }

            out.println("Pedidos pendentes:");
            for(String[] request : pendingRequests){
                out.println("ID: " + request[0] + ", Nome: " + request[1] + ", Membros: " + request[2]);
            }

            out.println("Indique o Id do grupo a aprovar: ");
            int groupId = Integer.parseInt(in.readLine());

            BDGroups.approveGroupRequest(groupId);
            out.println("Grupo aprovado com sucesso!");
            BDUsers.logMessage("Pedido de aprovar grupo com sucesso: " + groupId);
        }

    }

    private void editGroup() throws IOException{
        out.println("Indique o ID do grupo: ");
        int editId = Integer.parseInt(in.readLine());

        Map<Integer, Groups> groups = BDGroups.loadGroups();
        Groups groupToEdit = groups.get(editId);

        if(groupToEdit != null && Arrays.asList(groupToEdit.getUserEmail()).contains(loggedInUser.getEmail())){
            out.println("Grupo encontrado: " + groupToEdit);

            out.println("Deseja editar o nome do grupo? (sim/nao)");
            String editName = in.readLine().toLowerCase();
            if("sim".equals(editName)){
                out.println("Indique o novo nome do grupo: ");
                String newName = in.readLine();
                groupToEdit.setName(newName);
            }

            out.println("Deseja editar os membros do grupo? (sim/nao)");
            String editMembers = in.readLine().toLowerCase();
            if("sim".equals(editMembers)){
                out.println("Escolha uma operação: 1- adicionarEmails, 2- removerEmails");

                int memberAction = Integer.parseInt(in.readLine());
                switch (memberAction){
                    case 1:
                        out.println("Indique os emails a adicionar, separados por ';': ");
                        String[] emailsToAdd = in.readLine().split(";");
                        List<String> currentMembers = new ArrayList<>(Arrays.asList(groupToEdit.getUserEmail()));
                        currentMembers.addAll(Arrays.asList(emailsToAdd));
                        groupToEdit.setUserEmail(currentMembers.toArray(new String[0]));
                        break;

                    case 2:
                        out.println("Indique os emails a adicionar, separados por ';': ");
                        String[] emailsToRemove = in.readLine().split(";");
                        currentMembers = new ArrayList<>(Arrays.asList(groupToEdit.getUserEmail()));
                        currentMembers.removeAll(Arrays.asList(emailsToRemove));
                        groupToEdit.setUserEmail(currentMembers.toArray(new String[0]));
                        break;

                    default:
                        out.println("Operação inválida para editar membros");
                }
            }

            BDGroups.saveGroup(groupToEdit);
            out.println("Grupo editado com sucesso: " + groupToEdit);
        }else{
            out.println("Grupo não encontrado ou você não tem permissão para editar este grupo!");
        }
    }

    private void deleteGroup() throws IOException{
        out.println("Indique o ID do grupo a ser apagado: ");
        int groupId = Integer.parseInt(in.readLine());

        Map<Integer, Groups> groups = BDGroups.loadGroups();
        Groups groupToDelete = groups.get(groupId);

        if(groupToDelete != null && Arrays.asList(groupToDelete.getUserEmail()).contains(loggedInUser.getEmail())){
            out.println("Tem a certeza que deseja eliminar o grupo?(sim ou nao)");
            String confirmation = in.readLine().toLowerCase();
            if("sim".equals(confirmation)){
                BDGroups.deleteGroup(groupId);
                out.println("Grupo eliminado com sucesso");
                BDUsers.logMessage("Pedido de eliminar grupo com sucesso: " + groupId);
            }else{
                out.println("Operação cancelada.");
            }
        }else{
            out.println("Grupo não encontrado ou você não tem permissão para editar este grupo!");
        }
    }

    private void viewGroups(){
        Map<Integer, Groups> allGroups = BDGroups.loadGroups();
        boolean hasGroup = false;

        out.println("Grupos a que pertence:");
        for(Groups group : allGroups.values()){
            if(Arrays.asList(group.getUserEmail()).contains(loggedInUser.getEmail())){
                out.println(group);
                hasGroup = true;
            }
        }

        if(!hasGroup){
            out.println("Não pertence a nenhum grupo..");
        }
    }

    private void sendMessageToGroup() throws IOException{
        out.println("Indique o ID do grupo para enviar mensagem: ");
        int groupId = Integer.parseInt(in.readLine());

        Map<Integer, Groups> groups = BDGroups.loadGroups();
        Groups group = groups.get(groupId);

        if(group != null && Arrays.asList(group.getUserEmail()).contains(loggedInUser.getEmail())){
            out.println("Digite a mensagem para enviar: ");
            String message = in.readLine();
            group.broadcastMessage(message, loggedInUser.getEmail());
            out.println("Mensagem enviada com sucesso para o grupo.");
        }else {
            out.println("Grupo não encontrado ou não pertence a este grupo");
        }
    }

    private void viewGroupMessages() throws IOException {
        out.println("Indique o ID do grupo para visualizar mensagem: ");
        int groupId = Integer.parseInt(in.readLine());

        Map<Integer, Groups> groups = BDGroups.loadGroups();
        Groups group = groups.get(groupId);
        if (group != null && Arrays.asList(group.getUserEmail()).contains(loggedInUser.getEmail())) {
            List<Message> messages = BDMessages.loadMessageForGroup(groupId);
            if(messages.isEmpty()){
                out.println("Nenhuma mensagem encontrada para este grupo.");
            }else {
                out.println("Mensagens do grupo " + group.getName() + ":");
                for (Message message : messages){
                    out.println(message);
                }
            }
        }else {
            out.println("Grupo não encontrado ou não pertence a este grupo.");
        }
    }
}
