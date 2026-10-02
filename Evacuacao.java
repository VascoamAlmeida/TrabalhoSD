import java.util.concurrent.atomic.AtomicInteger;

public class Evacuacao {
    private static final AtomicInteger idCounter = new AtomicInteger(1);
    private String id;
    private String email;
    private String zona;
    private String danger;
    private String status;

    public Evacuacao(String email, String zona, String danger, String status) {
        this.id = String.valueOf(idCounter.getAndIncrement());
        this.email = email;
        this.zona = zona;
        this.danger = danger;
        this.status = status;
    }

    public Evacuacao(String id, String email, String zona, String danger, String status) {
        this.id = id;
        this.email = email;
        this.zona = zona;
        this.danger = danger;
        this.status = status;
        updateCounter(id);
    }

    private static void updateCounter(String id) {
        try {
            int parsedId = Integer.parseInt(id.trim());
            idCounter.updateAndGet(current -> Math.max(current, parsedId + 1));
        } catch (NumberFormatException ignored) {
            // IDs antigos não numéricos não impedem o carregamento dos restantes pedidos.
        }
    }

    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getZona() { return zona; }
    public String getDanger() { return danger; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
