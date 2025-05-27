package com.multikube_rest_service.common.encryption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class KubeconfigEncryptor {

    private final String encryptionKey;
    private final String initVector;

    public KubeconfigEncryptor(
            @Value("${multikube.encryption.kubeconfig.key}") String encryptionKey,
            @Value("${multikube.encryption.kubeconfig.iv}") String initVector) {
        this.encryptionKey = encryptionKey;
        this.initVector = initVector;
        if (encryptionKey.length() != 16 && encryptionKey.length() != 24 && encryptionKey.length() != 32) {
            throw new IllegalArgumentException("Encryption key must be 16, 24, or 32 bytes long for AES.");
        }
        if (initVector.length() != 16) {
            throw new IllegalArgumentException("Initialization Vector must be 16 bytes long for AES.");
        }
    }

    public String encrypt(String value) {
        if (value == null) {
            return null;
        }
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(encryptionKey.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            throw new RuntimeException("Error encrypting kubeconfig", ex);
        }
    }

    public String decrypt(String encrypted) {
        if (encrypted == null) {
            return null;
        }
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(encryptionKey.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            return new String(original);
        } catch (Exception ex) {
            throw new RuntimeException("Error decrypting kubeconfig", ex);
        }
    }
}