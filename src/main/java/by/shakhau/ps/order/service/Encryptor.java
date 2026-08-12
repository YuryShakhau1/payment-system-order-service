package by.shakhau.ps.order.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class Encryptor {

    private static final String CRYPTO_STANDARD = "AES";
    private static final String ALGORITHM = CRYPTO_STANDARD + "/GCM/NoPadding";

    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final SecretKeySpec secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public Encryptor(@Value("${crypto.card-secret-key}") String secretKeyValueBase64) {
        byte[] keyBytes = Base64.getDecoder().decode(secretKeyValueBase64);

        if (keyBytes.length != 32) {
            throw new IllegalArgumentException(
                    "Key length must be 32 bytes (256 bits). Current length is %d bytes".formatted(keyBytes.length));
        }

        this.secretKey = new SecretKeySpec(keyBytes, CRYPTO_STANDARD);
    }

    public String encrypt(String data) {
        if (data == null) {
            return null;
        }

        try {
            var iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            var parameterSpec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] ciphertext = cipher.doFinal(data.getBytes());

            byte[] encryptedBuffer = ByteBuffer.allocate(iv.length + ciphertext.length)
                    .put(iv)
                    .put(ciphertext)
                    .array();

            return Base64.getEncoder().encodeToString(encryptedBuffer);
        } catch (Exception e) {
            throw new RuntimeException("Field encryption error", e);
        }
    }

    public String decrypt(String encryptedData) {
        if (encryptedData == null) {
            return null;
        }

        try {
            byte[] encryptedBuffer = Base64.getDecoder().decode(encryptedData);

            ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedBuffer);
            byte[] iv = new byte[IV_LENGTH_BYTES];
            byteBuffer.get(iv);

            byte[] ciphertext = new byte[byteBuffer.remaining()];
            byteBuffer.get(ciphertext);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            return new String(cipher.doFinal(ciphertext));
        } catch (Exception e) {
            throw new RuntimeException("Field decryption error", e);
        }
    }
}
