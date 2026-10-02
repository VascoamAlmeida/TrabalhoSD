import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProcessaComunicacoes {
    private static final String COMUNICACOES_FILE = "pedidosComunicacoes.txt";
    private static final String ZONAS_FILE = "zonas.txt";
    private static final String DANGERS_FILE = "dangers.txt";
    private static final String COMUNICACOES_TYPE_FILE = "comunicacoes.txt";

    private PrintWriter out;
    private BufferedReader in;
    private User userLogIn;

    public ProcessaComunicacoes(PrintWriter out, BufferedReader in, User userLogIn) {
        this.out = out;
        this.in = in;
        this.userLogIn = userLogIn;
    }

    // Carregar Pedidos Evacuaçao do ficheiro
    public static synchronized void saveComunicacao(Comunicacao comunicacao) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(COMUNICACOES_FILE, true))) {
            System.out.println("Pedido comunicacao gravado com sucesso: " + comunicacao.getEmail() + ", " + comunicacao.getZona() + ", " + comunicacao.getDanger() + "," + comunicacao.getStatus() + "," + comunicacao.getcommunications());
            writer.println(comunicacao.getId() + "," + comunicacao.getEmail() + "," + comunicacao.getZona() + "," + comunicacao.getDanger() + "," + comunicacao.getcommunications() + "," + comunicacao.getStatus());
        } catch (Exception e) {
            System.out.println("Erro guardar Comunicacao");
        }
    }

    // Guardar Pedidos Evacuaçao Ficheiro
    public static synchronized List<Comunicacao> loadComunicaoes() {
        List<Comunicacao> comunicacaos = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(COMUNICACOES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    comunicacaos.add(new Comunicacao(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]));
                }
            }
        } catch (IOException e) {
            System.out.println("Erro carregar Comunicaçoes");
        }
        return comunicacaos;
    }

    public static List<String> loadZonas() {
        return loadFromFile(ZONAS_FILE);
    }

    public static List<String> loadCommunications() {
        return loadFromFile(COMUNICACOES_TYPE_FILE);
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

    protected void realizarPedidoComunicacao(PrintWriter out, BufferedReader in, User userLogIn) throws IOException {
        out.println("Pedidos de comunicacao:");

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

        //Apresentar comunicacoes
        List<String> comunicacoes = ProcessaComunicacoes.loadCommunications();
        out.println("Indique o comunicacao: ");
        for (int i = 0; i < comunicacoes.size(); i++) {
            out.println((i + 1) + " - " + comunicacoes.get(i));
        }

        int comunicacaoIndex = Integer.parseInt(in.readLine()) - 1;
        if (comunicacaoIndex < 0 || comunicacaoIndex >= comunicacoes.size()) {
            out.println("Comunicação inválida");
            return;
        }
        String comunicacaoEscolhida = comunicacoes.get(comunicacaoIndex);

        loadComunicaoes(); // Atualiza o contador de IDs com os pedidos já persistidos.
        Comunicacao novaComunicacao = new Comunicacao(userLogIn.getEmail(), zonaEscolhida, perigoEscolhido, comunicacaoEscolhida, "pendente");
        ProcessaComunicacoes.saveComunicacao(novaComunicacao);
        BDUsers.logMessage("Pedido de comunicaçoes criado por: " + userLogIn.getEmail());

        out.println("Pedido registado com sucesso.");
        out.println("Zona: " + zonaEscolhida);
        out.println("Perigo: " + perigoEscolhido);
        out.println("Comunicacao: " + comunicacaoEscolhida);
    }

    protected void verPedidosComunicacao(PrintWriter out) throws IOException {
        if (!"medio".equalsIgnoreCase(userLogIn.getPerfil())) {
            out.println("Permissão negada: a aprovação de comunicações requer perfil médio.");
            return;
        }
        out.println("Pedidos comunicação pendentes:");

        // Carregar apenas pedidos com status 'pendente'
        List<Comunicacao> comunicacoesPendentes = loadComunicaoes().stream()
                .filter(comunicacao -> "pendente".equals(comunicacao.getStatus()))
                .collect(Collectors.toList());

        //List<Comunicacao> comunicacoesPendentes = ProcessaComunicacoes.loadComunicaoes();
        if (comunicacoesPendentes.isEmpty()) {
            out.println("Nao tem comunicacoes");
            return;
        }

        for (int i = 0; i < comunicacoesPendentes.size(); i++) {
            Comunicacao comunicacao = comunicacoesPendentes.get(i);
            out.println("ID: " + comunicacao.getId() + ", Email: " + comunicacao.getEmail() + ", Zona: " + comunicacao.getZona() + ", Perigo: " + comunicacao.getDanger() + ", Comunicacao: " + comunicacao.getcommunications() + ", Estado: " + comunicacao.getStatus());
        }

        boolean continuar = true;
        while (continuar) {
            out.println("Escolha o número do pedido para aprovar/reprovar( 0 para sair)");
            String entrada = in.readLine();

            if ("0".equals(entrada)) {
                out.println("Saindo da visualização de pedidos de comunicação.");
                continuar = false;
                continue;
            }
            Comunicacao pedidoSelecionado = comunicacoesPendentes.stream()
                    .filter(comunicacao -> comunicacao.getId().equals(entrada))
                    .findFirst()
                    .orElse(null);
            try {
                if (pedidoSelecionado != null) {
                    out.println("Deseja aprovar (1) ou rejeitar (2) o pedido?");
                    int decisao = Integer.parseInt(in.readLine());

                    if (decisao == 1) {

                        pedidoSelecionado.setStatus("aprovado");
                        atualizarComunicacao(pedidoSelecionado);
                        out.println("Pedido aprovado");
                        BDUsers.logMessage("Pedido de comunicação criado por " + pedidoSelecionado.getEmail() + " aprovado por: " + userLogIn.getEmail());

                        String mensagemEmergencia = "EMERGENCIA: COMUNICAÇÃO " + pedidoSelecionado.getZona() + " DEVIDO A " + pedidoSelecionado.getDanger() + " COMUNICAÇÃO PEDIDA " + pedidoSelecionado.getcommunications() + "!!!!";
                        MultiServer.sendMessageToAll(mensagemEmergencia); // Envia a mensagem para todos os utilizadores conectados
                        
                    } else if (decisao == 2) {

                        pedidoSelecionado.setStatus("rejeitado");
                        atualizarComunicacao(pedidoSelecionado);
                        out.println("Pedido reprovado");
                        BDUsers.logMessage("Pedido de comunicação criado por " + pedidoSelecionado.getEmail() + " rejeitado por: " + userLogIn.getEmail());

                    } else {
                        out.println("Decisao invalida");
                    }
                } else {
                    out.println("ID invalida.");
                }
            } catch (NumberFormatException e) {
                out.println("Entrada inválida. Digite um número.");
            }
        }
    }

    private static synchronized void atualizarComunicacao(Comunicacao comunicacao) {
        List<Comunicacao> comunicacoes = loadComunicaoes();
        try (PrintWriter writer = new PrintWriter(new FileWriter(COMUNICACOES_FILE))) {
            for (Comunicacao c : comunicacoes) {
                if (c.getId().equals(comunicacao.getId())) {
                    // Atualiza a comunicação com o novo status
                    writer.println(comunicacao.getId() + "," + comunicacao.getEmail() + "," + comunicacao.getZona() + "," + comunicacao.getDanger() + "," + comunicacao.getcommunications() + "," + comunicacao.getStatus());
                } else {
                    // Mantém as outras comunicações inalteradas
                    writer.println(c.getId() + "," + c.getEmail() + "," + c.getZona() + "," + c.getDanger() + "," + c.getcommunications() + "," + c.getStatus());
                }
            }
            BDUsers.logMessage("Pedido de comunicação de " + comunicacao.getEmail() + " atualizado para: " + comunicacao.getStatus());
        } catch (IOException e) {
            System.out.println("Erro ao atualizar o arquivo de comunicação: " + e.getMessage());
        }
    }
}