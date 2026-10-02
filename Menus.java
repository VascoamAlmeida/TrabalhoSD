import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Menus {
    private final PrintWriter out;
    private final BufferedReader in;

    public Menus(PrintWriter out, BufferedReader in) {
        this.out = out;
        this.in = in;
    }

    public void displayPerfilAltoMenuOptions() {
        out.println("");
        out.println("Opções disponíveis:");
        out.println("1- Gerir grupos");
        out.println("2- Realizar pedido de comunicação");
        out.println("3- Realizar pedido de recurso");
        out.println("4- Realizar pedido de evacuação");
        out.println("5- Aprovar pedidos de evacuação");
        out.println("6- Ver pedidos de registo pendentes");
        out.println("7- Ver logs do sistema");
        out.println("8- Ver zonas existentes");
        out.println("9- Adicionar nova zona");
        out.println("10- Remover zona");
        out.println("11- Mensagens privadas");
        out.println("0- Sair");
    }

    public void displayPerfilMedioMenuOptions() {
        out.println("");
        out.println("Opções disponíveis:");
        out.println("1- Gerir grupos");
        out.println("2- Realizar pedido de comunicação");
        out.println("3- Realizar pedido de recurso");
        out.println("4- Realizar pedido de evacuação");
        out.println("5- Aprovar pedidos de comunicação");
        out.println("6- Mensagens privadas");
        out.println("0- Sair");
    }

    public void displayPerfilBaixoMenuOptions() {
        out.println("");
        out.println("Opções disponíveis:");
        out.println("1- Gerir grupos");
        out.println("2- Realizar pedido de comunicação");
        out.println("3- Realizar pedido de recurso");
        out.println("4- Aprovar pedidos de recurso");
        out.println("5- Mensagens privadas");
        out.println("0- Sair");
    }

    public void displayPerfilGeralMenuOptions() {
        out.println("");
        out.println("Opções disponíveis:");
        out.println("1- Realizar pedido de recurso");
        out.println("2- Mensagens privadas");
        out.println("0- Sair");
    }

    public int getMenuAction() throws IOException {
        out.println("Digite uma opção: ");
        try {
            String input = in.readLine();
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
