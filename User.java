public class User {
    private String nome;
    private String email;
    private String pass;
    private String perfil;

    public User (String nome, String email, String pass, String perfil) {
        this.nome = nome;
        this.email = email;
        this.pass = pass;
        this.perfil = perfil;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getPass() {
        return pass;
    }

    public String getPerfil() {
        return perfil;
    }

    public String toString() {
        return "Nome: " + nome + " Perfil: " + perfil;
    }
}