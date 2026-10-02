import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProcessaEvacuacoes  {
    private static final String PEDIDOS_FILE = "pedidos.txt";
    private static final String ZONAS_FILE = "zonas.txt";
    private static final String DANGERS_FILE = "dangers.txt";

    private PrintWriter out;
    private BufferedReader in;
    private User userLogIn;

    public ProcessaEvacuacoes(PrintWriter out, BufferedReader in, User userLogIn) {
        this.out = out;
        this.in = in;
        this.userLogIn = userLogIn;
    }

    // Carregar Pedidos Evacuaçao do ficheiro
    public static synchronized void savePedido(Evacuacao evacuacao) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(PEDIDOS_FILE, true))) {
            System.out.println("Pedido evacuacao gravado com sucesso:: " + evacuacao.getEmail() + ", " + evacuacao.getZona() + ", " + evacuacao.getDanger() + "," + evacuacao.getStatus());
            writer.println(evacuacao.getId() + "," + evacuacao.getEmail() + "," + evacuacao.getZona() + "," + evacuacao.getDanger() + "," + evacuacao.getStatus());
        } catch (Exception e) {
            System.out.println("Erro guardar Pedido");
        }
    }


    // Guardar Pedidos Evacuaçao Ficheiro
    public static synchronized List<Evacuacao> loadPedidos() {
        List<Evacuacao> evacuacaos = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(PEDIDOS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    evacuacaos.add(new Evacuacao(parts[0], parts[1], parts[2], parts[3], parts[4]));
                }
            }
        } catch (IOException e) {
            System.out.println("Erro carregar Users");
        }
        return evacuacaos;
    }

    public static List<String> loadZonas() {
        return loadFromFile(ZONAS_FILE);
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

    public void verZonas(PrintWriter out) {
        List<String> zonas = loadZonas();
        if (zonas.isEmpty()) {
            out.println("Não há zonas cadastradas.");
        } else {
            out.println("Zonas existentes:");
            for (String zona : zonas) {
                out.println(zona);
            }
        }
    }

    protected void addZona(String zona) {
        List<String> zonas = loadZonas();
        if (zonas.contains(zona)) {
            out.println("Zona " + zona + " já existe.");
        } else {
            zonas.add(zona);
            saveZonas(zonas);
            out.println("Zona " + zona + " adicionada com sucesso.");
        }
    }

    protected void removeZona(String zona) {
        List<String> zonas = loadZonas();
        if (zonas.contains(zona)) {
            zonas.remove(zona);
            saveZonas(zonas);
            out.println("Zona " + zona + " removida com sucesso.");
        } else {
            out.println("Zona " + zona + " não encontrada.");
        }
    }

    private void saveZonas(List<String> zonas) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ZONAS_FILE))) {
            for (String zona : zonas) {
                writer.println(zona);
            }
        } catch (IOException e) {
            out.println("Erro ao salvar zonas: " + e.getMessage());
        }
    }

    protected void realizarPedidoEvacuacao(PrintWriter out, BufferedReader in, User userLogIn) throws IOException {
        out.println("Pedidos de evacuação:");

        List<String> zonas = loadZonas();
        if (zonas.isEmpty()) {
            out.println("Erro: Nenhuma zona disponível.");
            return;
        }

        // Apresentar zonas
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

        // Apresentar perigos
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

        loadPedidos(); // Atualiza o contador de IDs com os pedidos já persistidos.
        Evacuacao novoEvacuacao = new Evacuacao(userLogIn.getEmail(), zonaEscolhida, perigoEscolhido, "pendente");
        System.out.println("Novo Pedido: " + novoEvacuacao.getEmail() + ", " + novoEvacuacao.getZona() + ", " + novoEvacuacao.getDanger() + ", pendente");
        ProcessaEvacuacoes.savePedido(novoEvacuacao);
        BDUsers.logMessage("Pedido de evacuacao criado por: " + userLogIn.getEmail());


        out.println("Pedido registado com sucesso.");
        out.println("Zona: " + zonaEscolhida);
        out.println("Perigo: " + perigoEscolhido);
    }

    protected void verPedidosEvacuacao(PrintWriter out) throws IOException {
        if (!"alto".equalsIgnoreCase(userLogIn.getPerfil())) {
            out.println("Permissão negada: a aprovação de evacuações requer perfil alto.");
            return;
        }
        out.println("Pedidos evacuação pendentes:");

        List<Evacuacao> pedidosPendentes = loadPedidos().stream()
                .filter(evacuacao -> "pendente".equalsIgnoreCase(evacuacao.getStatus()))
                .collect(Collectors.toList());

        if (pedidosPendentes.isEmpty()) {
            out.println("Nao tem pedidos");
            return;
        }

        //Listar Pedidos
        for (int i = 0; i < pedidosPendentes.size(); i++) {
            Evacuacao evacuacao = pedidosPendentes.get(i);
            out.println("ID: " + evacuacao.getId() + ", Email: " + evacuacao.getEmail() + ", Zona: " + evacuacao.getZona() + ", Perigo: " + evacuacao.getDanger());
        }

        boolean continuar = true;
        while (continuar) {
            out.println("Escolha o número do pedido para aprovar/reprovar ou 0 para sair");
            String entrada = in.readLine();

            if ("0".equals(entrada)) {
                out.println("Saindo da visualização de pedidos de evacuação.");
                continuar = false;
                continue;
            }
            Evacuacao pedidoSelecionado = pedidosPendentes.stream()
                    .filter(evacuacao -> evacuacao.getId().equals(entrada))
                    .findFirst()
                    .orElse(null);

            try {
                if (pedidoSelecionado != null) {

                    out.println("Deseja aprovar (1) ou rejeitar (2) o pedido?");
                    int decisao = Integer.parseInt(in.readLine());

                    if (decisao == 1) {
                        pedidoSelecionado.setStatus("aprovado");
                        atualizarEvacuacao(pedidoSelecionado);
                        out.println("Pedido aprovado");
                        BDUsers.logMessage("Pedido de evacuação criado por " + pedidoSelecionado.getEmail() + " aprovado por: " + userLogIn.getEmail());

                        String mensagemEmergencia = "EMERGENCIA: EVACUAÇÃO " + pedidoSelecionado.getZona() + " DEVIDO A " + pedidoSelecionado.getDanger() + "!!!!";
                        MultiServer.sendMessageToAll(mensagemEmergencia); // Envia a mensagem para todos os utilizadores conectados
                        
                    } else if (decisao == 2) {
                        pedidoSelecionado.setStatus("reprovado");
                        atualizarEvacuacao(pedidoSelecionado);
                        out.println("Pedido reprovado");
                        BDUsers.logMessage("Pedido de evacuação criado por " + pedidoSelecionado.getEmail() + " rejeitado por: " + userLogIn.getEmail());
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

    private static synchronized void atualizarEvacuacao(Evacuacao evacuacao) {
        List<Evacuacao> evacuacoes = loadPedidos();
        try (PrintWriter writer = new PrintWriter(new FileWriter(PEDIDOS_FILE))) {
            for (Evacuacao c : evacuacoes) {
                if (c.getId().equals(evacuacao.getId())) {
                    // Atualiza a comunicação com o novo status
                    writer.println(evacuacao.getId() + "," +evacuacao.getEmail() + "," + evacuacao.getZona() + "," + evacuacao.getDanger() + "," + evacuacao.getStatus());
                } else {
                    // Mantém as outras comunicações inalteradas
                    writer.println(c.getId() + "," +c.getEmail() + "," + c.getZona() + "," + c.getDanger() + ","  + c.getStatus());
                }
            }
            BDUsers.logMessage("Pedido de evacuação de " + evacuacao.getEmail() + " atualizado para: " + evacuacao.getStatus());
        } catch (IOException e) {
            System.out.println("Erro ao atualizar o ficheiro de evacuação: " + e.getMessage());
        }
    }
}
