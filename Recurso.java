import java.util.concurrent.atomic.AtomicInteger;

public class Recurso {
    private static final AtomicInteger idCounter = new AtomicInteger(1);
    private String id;
    private String email;
    private String zona;
    private String danger;
    private String status;
    private String resource;

    public Recurso(String email, String zona, String danger, String resource, String status) {
        this.id = String.valueOf(idCounter.getAndIncrement());
        this.email = email;
        this.zona = zona;
        this.danger = danger;
        this.resource = resource;
        this.status = status;
    }

    public Recurso(String id, String email, String zona, String danger, String resource, String status) {
        this.id = id;
        this.email = email;
        this.zona = zona;
        this.danger = danger;
        this.resource = resource;
        this.status = status;
        updateCounter(id);
    }

    private static void updateCounter(String id) {
        try {
            int parsedId = Integer.parseInt(id.trim());
            idCounter.updateAndGet(current -> Math.max(current, parsedId + 1));
        } catch (NumberFormatException ignored) {
            // Mantém compatibilidade com dados antigos.
        }
    }

    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getZona() { return zona; }
    public String getDanger() { return danger; }
    public String getStatus() { return status; }
    public String getresource() { return resource; }
    public void setStatus(String status) { this.status = status; }
}
