package studio.humpback.backend.security;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TwoFactorSecretCryptoService {

    private static final String ALGORITHM = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String ENCRYPTION_KEY_PROPERTY = "${security.2fa.encryption-key-base64:}";
    private static final String ERR_SECRET_TO_ENCRYPT_EMPTY = "Secret to encrypt cannot be empty";
    private static final String ERR_SECRET_TO_DECRYPT_EMPTY = "Secret to decrypt cannot be empty";
    private static final String ERR_INVALID_ENCRYPTED_SECRET_FORMAT = "Invalid encrypted 2FA secret format";
    private static final String ERR_ENCRYPTION_FAILED = "Failed to encrypt 2FA secret";
    private static final String ERR_DECRYPTION_FAILED = "Could not decrypt 2FA secret";
    private static final String ERR_MISSING_ENCRYPTION_KEY = "Missing TWO_FACTOR_ENCRYPTION_KEY_BASE64 configuration";
    private static final String ERR_INVALID_ENCRYPTION_KEY_LENGTH =
            "TWO_FACTOR_ENCRYPTION_KEY_BASE64 must decode to 16, 24, or 32 bytes";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final String ENCRYPTED_PREFIX = "enc:v1:";

    private final SecureRandom secureRandom = new SecureRandom();

    @Value(ENCRYPTION_KEY_PROPERTY)
    private String encryptionKeyBase64;

    public String encrypt(String plaintext) {
        String normalizedPlaintext = Optional.ofNullable(plaintext)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .orElseThrow(() -> new IllegalArgumentException(ERR_SECRET_TO_ENCRYPT_EMPTY));

        if (isEncrypted(normalizedPlaintext)) {
            return normalizedPlaintext;
        }

        byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
        secureRandom.nextBytes(iv);

        byte[] key = decodeEncryptionKey();
        byte[] encryptedBytes;
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, ALGORITHM), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            encryptedBytes = cipher.doFinal(normalizedPlaintext.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(ERR_ENCRYPTION_FAILED, e);
        }

        ByteBuffer buffer = ByteBuffer.allocate(iv.length + encryptedBytes.length);
        buffer.put(iv);
        buffer.put(encryptedBytes);
        return ENCRYPTED_PREFIX + Base64.getEncoder().encodeToString(buffer.array());
    }

    public String decrypt(String ciphertext) {
        String normalizedCiphertext = Optional.ofNullable(ciphertext)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .orElseThrow(() -> new IllegalArgumentException(ERR_SECRET_TO_DECRYPT_EMPTY));

        if (!isEncrypted(normalizedCiphertext)) {
            return normalizedCiphertext;
        }

        byte[] combined = Base64.getDecoder().decode(normalizedCiphertext.substring(ENCRYPTED_PREFIX.length()));
        if (combined.length <= GCM_IV_LENGTH_BYTES) {
            throw new IllegalArgumentException(ERR_INVALID_ENCRYPTED_SECRET_FORMAT);
        }

        byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
        byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH_BYTES];
        System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH_BYTES);
        System.arraycopy(combined, GCM_IV_LENGTH_BYTES, encrypted, 0, encrypted.length);

        byte[] key = decodeEncryptionKey();
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, ALGORITHM), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException(ERR_DECRYPTION_FAILED, e);
        }
    }

    public boolean isEncrypted(String value) {
        return value != null && value.startsWith(ENCRYPTED_PREFIX);
    }

    private byte[] decodeEncryptionKey() {
        String normalizedKey = Optional.ofNullable(encryptionKeyBase64)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .orElseThrow(() -> new IllegalStateException(ERR_MISSING_ENCRYPTION_KEY));

        byte[] key = Base64.getDecoder().decode(normalizedKey);
        if (key.length != 16 && key.length != 24 && key.length != 32) {
            throw new IllegalStateException(ERR_INVALID_ENCRYPTION_KEY_LENGTH);
        }
        return key;
    }
}
