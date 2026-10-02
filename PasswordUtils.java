import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utilitário simples para armazenar passwords com PBKDF2 em vez de texto simples.
 * Mantém compatibilidade de leitura com ficheiros antigos que ainda contenham passwords em claro.
 */
public final class PasswordUtils {
    private static final String PREFIX = "pbkdf2";
    private static final int ITERATIONS = 120_000;
    private static final int SALT_LENGTH = 16;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtils() {
    }

    public static String hashPassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password não pode ser nula.");
        }

        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);

        return PREFIX + "$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    public static String hashIfNeeded(String value) {
        return isHashed(value) ? value : hashPassword(value);
    }

    public static boolean verifyPassword(String candidate, String storedValue) {
        if (candidate == null || storedValue == null) {
            return false;
        }

        if (!isHashed(storedValue)) {
            return MessageDigest.isEqual(candidate.getBytes(StandardCharsets.UTF_8),
                    storedValue.getBytes(StandardCharsets.UTF_8));
        }

        try {
            String[] parts = storedValue.split("\\$", 4);
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);
            byte[] candidateHash = pbkdf2(candidate.toCharArray(), salt, iterations, expectedHash.length * 8);
            return MessageDigest.isEqual(candidateHash, expectedHash);
        } catch (RuntimeException e) {
            return false;
        }
    }

    public static boolean isHashed(String value) {
        return value != null && value.startsWith(PREFIX + "$");
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível proteger a password.", e);
        } finally {
            spec.clearPassword();
        }
    }
}
