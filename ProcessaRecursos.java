import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProcessaRecursos {
    private static final String RECURSOS_FILE = "pedidosRecursos.txt";
    private static final String ZONAS_FILE = "zonas.txt";
    private static final String DANGERS_FILE = "dangers.txt";
    private static final String RECURSOS_TYPE_FILE = "recursos.txt";

    private PrintWriter out;
    private BufferedReader in;
    private User userLogIn;

    public ProcessaRecursos(PrintWriter out, BufferedReader in, User userLogIn) {
        this.out = out;
        this.in = in;
        this.userLogIn = userLogIn;
    }

    // Carregar Pedidos Evacuaçao do ficheiro
    public static synchronized void saveRecurso(Recurso recurso) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(RECURSOS_FILE, true))) {
            System.out.println("Pedido recursos gravado com sucesso:: " + recurso.getEmail() + ", " + recurso.getZona() + ", " + recurso.getDanger() + "," + recurso.getStatus() + "," + recurso.getresource());
            writer.println(recurso.getId() + ", " + recurso.getEmail() + "," + recurso.getZona() + "," + recurso.getDanger() + "," + recurso.getresource() + "," + recurso.getStatus());
        } catch (Exception e) {
            System.out.println("Erro guardar recursos");
        }
    }

    // Guardar Pedidos Evacuaçao Ficheiro
    public static synchronized List<Recurso> loadRecursos() {
        List<Recurso> recursos = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(RECURSOS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    recursos.add(new Recurso(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim(), parts[4].trim(), parts[5].trim()));
                }
            }
        } catch (IOException e) {
            System.out.println("Erro carregar Comunicaçoes");
        }
        return recursos;
    }

    public static List<String> loadZonas() {
        return loadFromFile(ZONAS_FILE);
    }

    public static List<String> loadResources() {
        return loadFromFile(RECURSOS_TYPE_FILE);
    }

    public static List<String> loadDangers() {
        return loadFromFile(DANGERS_FILE);
    }

    // Carregar Pedidos Evacuaçao Ficheiro
    private static List<String> loadFromFile(String fileName) {
        List<String> items = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                items.add(line.trim());
            }
        } catch (IOException e) {
            System.out.println("Erro ao carregar " + fileName + ": " + e.getMessage());
        }
        return items;
    }

    protected void realizarPedidoRecurso(PrintWriter out, BufferedReader in, User userLogIn) throws IOException {
        out.println("Pedidos de recurso:");

        List<String> zonas = loadZonas();
        if (zonas.isEmpty()) {
            out.println("Erro: Nenhuma zona disponível.");
            return;
        }
        //Apresentar zonas
        out.println("Indique a zona: ");
        for (int i = 0; i < zonas.size(); i++) {
            out.println((i + 1) + " - " + zonas.get(i));
        }

        int zonaIndex = Integer.parseInt(in.readLine()) - 1;
        if (zonaIndex < 0 || zonaIndex >= zonas.size()) {
            out.println("Zona inválida");
            return;
        }
        String zonaEscolhida = zonas.get(zonaIndex);

        //Apresentar perigos
        List<String> perigos = loadDangers();
        out.println("Indique o perigo: ");
        for (int i = 0; i < perigos.size(); i++) {
            out.println((i + 1) + " - " + perigos.get(i));
        }

        int perigoIndex = Integer.parseInt(in.readLine()) - 1;
        if (perigoIndex < 0 || perigoIndex >= perigos.size()) {
            out.println("Perigo inválido");
            return;
        }
        String perigoEscolhido = perigos.get(perigoIndex);

        //Apresentar recursos
        List<String> recursos = ProcessaRecursos.loadResources();
        out.println("Indique o recurso: ");
        for (int i = 0; i < recursos.size(); i++) {
            out.println((i + 1) + " - " + recursos.get(i));
        }

        int recursoIndex = Integer.parseInt(in.readLine()) - 1;
        if (recursoIndex < 0 || recursoIndex >= recursos.size()) {
            out.println("Recurso inválido");
            return;
        }
        String recursoEscolhida = recursos.get(recursoIndex);

        loadRecursos(); // Atualiza o contador de IDs com os pedidos já persistidos.
        Recurso novoRecurso= new Recurso(userLogIn.getEmail(), zonaEscolhida, perigoEscolhido, recursoEscolhida, "pendente");
        ProcessaRecursos.saveRecurso(novoRecurso);
        BDUsers.logMessage("Pedido de recursos criado por: " + userLogIn.getEmail());

        out.println("Pedido registado com sucesso.");
        out.println("Zona: " + zonaEscolhida);
        out.println("Perigo: " + perigoEscolhido);
        out.println("Recurso: " + recursoEscolhida);
    }

    protected void verPedidosRecursos(PrintWriter out) throws IOException {
        if (!"baixo".equalsIgnoreCase(userLogIn.getPerfil())) {
            out.println("Permissão negada: a aprovação de recursos requer perfil baixo.");
            return;
        }
        out.println("Pedidos de recursos pendentes:");

        // Carregar apenas pedidos com status 'pendente'
        List<Recurso> recursosPendentes = loadRecursos().stream()
                .filter(recurso -> "pendente".equals(recurso.getStatus()))
                .collect(Collectors.toList());

        if (recursosPendentes.isEmpty()) {
            out.println("Nao tem recursos");
            return;
        }

        for (int i = 0; i < recursosPendentes.size(); i++) {
            Recurso recurso = recursosPendentes.get(i);
            out.println("ID: " + recurso.getId() + ", Email: " + recurso.getEmail() + ", Zona: " + recurso.getZona() + ", Perigo: " + recurso.getDanger() + ", Recurso: " + recurso.getresource() + ", Estado: " + recurso.getStatus());
        }

        boolean continuar = true;
        while (continuar) {
            out.println("Escolha o número do pedido para aprovar/reprovar( 0 para sair)");
            String entrada = in.readLine();

            if ("0".equals(entrada)) {
                out.println("Saindo da visualização de pedidos de recursos.");
                continuar = false;
                continue;
            }

            Recurso pedidoSelecionado = recursosPendentes.stream()
                    .filter(recurso -> recurso.getId().equals(entrada))
                    .findFirst()
                    .orElse(null);
            try {
                if (pedidoSelecionado != null) {
                    out.println("Deseja aprovar (1) ou rejeitar (2) o pedido?");
                    int decisao = Integer.parseInt(in.readLine());

                    if (decisao == 1) {
                        pedidoSelecionado.setStatus("aprovado");
                        // Atualiza o status do pedido no arquivo
                        atualizarRecurso(pedidoSelecionado);
                        out.println("Pedido aprovado");
                        BDUsers.logMessage("Pedido de recursos criado por " + pedidoSelecionado.getEmail() + " aprovado por: " + userLogIn.getEmail());

                        // Enviar mensagem de emergência para todos os utilizadores
                        String mensagemEmergencia = "EMERGENCIA: RECURSOS " + pedidoSelecionado.getZona() + " DEVIDO A " + pedidoSelecionado.getDanger() +" RECURSO PEDIDO " + pedidoSelecionado.getresource() + "!!!!";
                        MultiServer.sendMessageToAll(mensagemEmergencia); // Envia a mensagem para todos os utilizadores conectados
                        
                    } else if (decisao == 2) {
                        pedidoSelecionado.setStatus("rejeitado");
                        // Atualiza o status do pedido no arquivo
                        atualizarRecurso(pedidoSelecionado);
                        out.println("Pedido reprovado");
                        BDUsers.logMessage("Pedido de recursos criado por " + pedidoSelecionado.getEmail() + " rejeitado por: " + userLogIn.getEmail());
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

    private static synchronized void atualizarRecurso(Recurso recurso) {
        List<Recurso> recursos = loadRecursos();

        try (PrintWriter writer = new PrintWriter(new FileWriter(RECURSOS_FILE))) {
            for (Recurso c : recursos) {
                if (c.getId().equals(recurso.getId())) {
                    // Atualiza a comunicação com o novo status
                    writer.println(recurso.getId() + "," + recurso.getEmail() + "," + recurso.getZona() + "," + recurso.getDanger() + "," + recurso.getresource() + "," + recurso.getStatus());
                } else {
                    // Mantém as outras comunicações inalteradas
                    writer.println(c.getId() + "," + c.getEmail() + "," + c.getZona() + "," + c.getDanger() + "," + c.getresource() + "," + c.getStatus());
                }
            }
            BDUsers.logMessage("Pedido de recursos de " + recurso.getEmail() + " atualizado para: " + recurso.getStatus());
        } catch (IOException e) {
            System.out.println("Erro ao atualizar o arquivo de recurso: " + e.getMessage());
        }
    }
}
