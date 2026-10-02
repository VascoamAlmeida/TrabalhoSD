import java.net.*;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ProcessaCliente extends Thread {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private User userLogIn;

    public ProcessaCliente(Socket socket) {
        this.clientSocket = socket;
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            MultiServer.addClient(clientSocket, out);
            Menus menus = new Menus(out, in);

            String pedido;
            out.println("Indique se quer realizar registo, login ou sair no sistema: ");
            while ((pedido = in.readLine()) != null) {
                if ("registo".equals(pedido)) {
                    realizarRegisto();
                } else if ("login".equals(pedido)) {
                    realizarLogin(menus);
                } else if ("sair".equals(pedido)) {
                    out.println("Encerrar ligação...");
                    break;
                } else {
                    out.println("Comando inválido. Utilize registo, login ou sair.");
                }
            }
        } catch (
                IOException e) {
            e.printStackTrace();
        } finally {
            if (userLogIn != null && out != null) {
                MultiServer.removeClientLogged(out, userLogIn.getEmail());
                NotificationService.removeUserSocket(userLogIn.getEmail());
            }
            if (out != null) {
                MultiServer.removeClient(out);
            }
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void realizarRegisto() {
        try {
            out.println("Indique seu nome:");
            String nome = in.readLine();

            out.println("Indique seu email:");
            String email = in.readLine();

            out.println("Indique sua pass:");
            String pass = in.readLine();

            out.println("Indique seu perfil(alto, medio, baixo e geral):");
            String perfil = in.readLine().trim().toLowerCase();

            if (!Arrays.asList("alto", "medio", "baixo", "geral").contains(perfil)) {
                out.println("Perfil inválido.");
                return;
            }

            if (!MultiServer.registarUser(nome, email, pass, perfil)) {
                out.println("Erro: Email já registado ou em processo de aprovação.");
                return;
            }

            if ("geral".equals(perfil)) {
                out.println("Registo concluído com sucesso. Pode fazer login imediatamente.");
            } else {
                out.println("Registo submetido. Aguarde aprovação de um perfil alto.");
                MultiServer.noficationAdmin(
                        "Pedido registo: \n" +
                                "Nome: " + nome +
                                "\nEmail: " + email +
                                "\nPerfil: " + perfil,
                        email, out);
            }
        } catch (IOException e) {
            out.println("Erro ao processar registo: " + e.getMessage());
        }
    }

    private void realizarLogin(Menus menus) throws IOException {
        out.println("Indique seu email:");
        String email = in.readLine();

        out.println("Indique sua pass:");
        String pass = in.readLine();

        if (!MultiServer.authUser(email, pass)) {
            out.println("Autenticação falhou!");
            return;
        }

        userLogIn = MultiServer.userRegistados.get(email);
        MultiServer.addClientLogged(clientSocket, out, email);
        NotificationService.addUserSocket(email, clientSocket);
        BDUsers.logMessage("Login de utilizador: " + email + " realizado com sucesso.");
        out.println("Login com sucesso: " + email);
        mostrarAlertasRecentes();

        try {
            switch (userLogIn.getPerfil().toLowerCase()) {
                case "alto":
                    handlePerfilAlto(menus);
                    break;
                case "medio":
                    handlePerfilMedio(menus);
                    break;
                case "baixo":
                    handlePerfilBaixo(menus);
                    break;
                case "geral":
                    handlePerfilGeral(menus);
                    break;
                default:
                    out.println("Perfil de utilizador inválido.");
            }
        } finally {
            MultiServer.removeClientLogged(out, email);
            NotificationService.removeUserSocket(email);
            BDUsers.logMessage("Logout de utilizador: " + email);
            userLogIn = null;
        }
    }

    private void verLogs() {
        // Check if the user's profile is "alto"
        if ("alto".equals(userLogIn.getPerfil())) {
            try (BufferedReader logReader = new BufferedReader(new FileReader("log.txt"))) {
                String logLine;
                out.println("Logs do sistema:");
                while ((logLine = logReader.readLine()) != null) {
                    out.println(logLine);
                }
            } catch (IOException e) {
                out.println("Erro ao carregar os logs: " + e.getMessage());
            }
        } else {
            out.println("Permissão negada: Apenas utilizadores com perfil 'alto' podem visualizar os logs.");
        }
    }

    private void handlePerfilAlto(Menus menus) throws IOException {
        int action;
        do {
            menus.displayPerfilAltoMenuOptions();
            action = menus.getMenuAction();

            switch (action) {
                case 1:
                    new ProcessaGrupos(out, in, userLogIn).handleGroupOperations();
                    break;
                case 2:
                    new ProcessaComunicacoes(out, in, userLogIn).realizarPedidoComunicacao(out, in, userLogIn);
                    BDUsers.logMessage("Pedido de comunicacao realizado por: " + userLogIn.getEmail());
                    break;
                case 3:
                    new ProcessaRecursos(out, in, userLogIn).realizarPedidoRecurso(out, in, userLogIn);
                    BDUsers.logMessage("Pedido de recursos realizado por: " + userLogIn.getEmail());
                    break;
                case 4:
                    new ProcessaEvacuacoes(out, in, userLogIn).realizarPedidoEvacuacao(out, in, userLogIn);
                    BDUsers.logMessage("Pedido de evacuacao realizado por: " + userLogIn.getEmail());
                    break;
                case 5:
                    new ProcessaEvacuacoes(out, in, userLogIn).verPedidosEvacuacao(out);
                    break;
                case 6:
                    verPedidos();
                    break;
                case 7:
                    verLogs();
                    break;
                case 8:
                    new ProcessaEvacuacoes(out, in, userLogIn).verZonas(out);
                    break;
                case 9:
                    out.println("Digite o nome da nova zona:");
                    new ProcessaEvacuacoes(out, in, userLogIn).addZona(in.readLine());
                    break;
                case 10:
                    out.println("Digite o nome da zona a remover:");
                    new ProcessaEvacuacoes(out, in, userLogIn).removeZona(in.readLine());
                    break;
                case 11:
                    new ProcessaMensagens(out, in, userLogIn).handlePrivateMessages();
                    break;
                case 0:
                    out.println("Logging out...");
                    break;
                default:
                    out.println("Comando inválido!");
            }
        } while (action != 0);
    }

    private void handlePerfilMedio(Menus menus) throws IOException {
        int action;
        do {
            menus.displayPerfilMedioMenuOptions();
            action = menus.getMenuAction();

            switch (action) {
                case 1:
                    new ProcessaGrupos(out, in, userLogIn).handleGroupOperations();
                    break;
                case 2:
                    new ProcessaComunicacoes(out, in, userLogIn).realizarPedidoComunicacao(out, in, userLogIn);
                    break;
                case 3:
                    new ProcessaRecursos(out, in, userLogIn).realizarPedidoRecurso(out, in, userLogIn);
                    break;
                case 4:
                    new ProcessaEvacuacoes(out, in, userLogIn).realizarPedidoEvacuacao(out, in, userLogIn);
                    break;
                case 5:
                    new ProcessaComunicacoes(out, in, userLogIn).verPedidosComunicacao(out);
                    break;
                case 6:
                    new ProcessaMensagens(out, in, userLogIn).handlePrivateMessages();
                    break;
                case 0:
                    out.println("Logging out...");
                    break;
                default:
                    out.println("Comando inválido!");
            }
        } while (action != 0);
    }

    private void handlePerfilBaixo(Menus menus) throws IOException {
        int action;
        do {
            menus.displayPerfilBaixoMenuOptions();
            action = menus.getMenuAction();

            switch (action) {
                case 1:
                    new ProcessaGrupos(out, in, userLogIn).handleGroupOperations();
                    break;
                case 2:
                    new ProcessaComunicacoes(out, in, userLogIn).realizarPedidoComunicacao(out, in, userLogIn);
                    break;
                case 3:
                    new ProcessaRecursos(out, in, userLogIn).realizarPedidoRecurso(out, in, userLogIn);
                    break;
                case 4:
                    new ProcessaRecursos(out, in, userLogIn).verPedidosRecursos(out);
                    break;
                case 5:
                    new ProcessaMensagens(out, in, userLogIn).handlePrivateMessages();
                    break;
                case 0:
                    out.println("Logging out...");
                    break;
                default:
                    out.println("Comando inválido!");
            }
        } while (action != 0);
    }

    private void handlePerfilGeral(Menus menus) throws IOException {
        int action;
        do {
            menus.displayPerfilGeralMenuOptions();
            action = menus.getMenuAction();

            switch (action) {
                case 1:
                    new ProcessaRecursos(out, in, userLogIn).realizarPedidoRecurso(out, in, userLogIn);
                    break;
                case 2:
                    new ProcessaMensagens(out, in, userLogIn).handlePrivateMessages();
                    break;
                case 0:
                    out.println("Logging out...");
                    break;
                default:
                    out.println("Comando inválido!");
            }
        } while (action != 0);
    }

    private void mostrarAlertasRecentes() {
        List<String> notifications = BDNotifications.loadRecentNotifications(5);
        if (notifications.isEmpty()) {
            return;
        }

        out.println("Alertas de emergência recentes:");
        for (String notification : notifications) {
            out.println(notification);
        }
    }

    private void verPedidos() throws IOException {
        List<String[]> pendingRequest = BDUsers.loadPendingRequest();
        BDUsers.logMessage("Pedido de ver lista pedidos de registo de conta realizado por: " + userLogIn.getEmail());

        if (pendingRequest.isEmpty()) {
            out.println("Não tem pedidos");
            return;
        }

        out.println("Pedidos Pendentes:");
        for (int i = 0; i < pendingRequest.size(); i++) {
            String[] request = pendingRequest.get(i);
            out.println((i + 1) + ": Nome: " + request[0] + ", Email: " + request[1] + ", Perfil: "
                    + request[3]);
        }
            boolean continuar = true;
            while (continuar) {
                out.println("\nEscolha o número do pedido para aprovar/reprovar ou 0 para sair.");
                String entrada = in.readLine();

                if ("0".equals(entrada)) {
                    out.println("Saindo da visualização de pedidos registo.");
                    continuar = false;
                    continue;
                }

                try {
                    int indice = Integer.parseInt(entrada) - 1;

                    if (indice >= 0 && indice < pendingRequest.size()) {
                        String[] requestSelecionado = pendingRequest.get(indice);

                        out.println("Deseja aprovar (1) ou rejeitar (2) o pedido?");
                        int decisao = Integer.parseInt(in.readLine());

                        if (decisao == 1) {
                            aprovarPedido(requestSelecionado);
                            pendingRequest.remove(indice);
                            out.println("Pedido aprovado");
                            BDUsers.logMessage("Aprovação de pedido de registo feito por: " + userLogIn.getEmail() + " aprovado.");

                        } else if (decisao == 2) {
                            rejeitarPedido(requestSelecionado);
                            pendingRequest.remove(indice);
                            out.println("Pedido reprovado");
                            BDUsers.logMessage("Reprovação de pedido de registo feita por: " + userLogIn.getEmail() + " reprovado.");
                        } else {
                            out.println("Decisao invalida");
                        }
                    } else {
                        out.println("opcao invalida.");
                    }
                } catch (NumberFormatException e) {
                    out.println("Entrada inválida. Digite um número.");
                }
            }
        }


    private void aprovarPedido(String[] request) throws IOException {
        String email = request[1];
        MultiServer.respostaAdmin(email, true, request[3], request[0], request[2], MultiServer.clientesPendentes.get(email));
        BDUsers.logMessage("User aprovado: " + email);
    }

    private void rejeitarPedido(String[] request) throws IOException {
        String email = request[1];
        MultiServer.respostaAdmin(email, false, request[3], request[0], request[2], MultiServer.clientesPendentes.get(email));
        BDUsers.logMessage("User reprovado: " + email);
    }

}
