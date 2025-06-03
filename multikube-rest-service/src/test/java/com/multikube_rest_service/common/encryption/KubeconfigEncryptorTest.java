package com.multikube_rest_service.common.encryption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link KubeconfigEncryptor}.
 */
class KubeconfigEncryptorTest {

    private KubeconfigEncryptor kubeconfigEncryptor;
    // Use keys that match the required length (16, 24, or 32 bytes for AES key; 16 for IV)
    private final String validKey16 = "ThisIsA16ByteKey"; // 16 bytes
    private final String validKey24 = "ThisIsA24ByteTestKey!!!!"; // 24 bytes
    private final String validKey32 = "ThisIsA32ByteTestKeyForCrypto!!!"; // 32 bytes
    private final String validIv = "AnInitialization"; // 16 bytes (IV for AES/CBC is typically 16 bytes)


    @BeforeEach
    void setUp() {
        // Initialize with a 16-byte key and IV for most tests
        kubeconfigEncryptor = new KubeconfigEncryptor(validKey16, validIv);
    }

    /**
     * Tests that a string encrypted and then decrypted returns the original value using a 16-byte key.
     */
    @Test
    void encrypt_decrypt_with16ByteKey_shouldReturnOriginalValue() {
        String originalValue = "This is a test kubeconfig content with 16-byte key.";
        String encryptedValue = kubeconfigEncryptor.encrypt(originalValue);
        assertNotNull(encryptedValue, "Encrypted value should not be null");
        assertNotEquals(originalValue, encryptedValue, "Encrypted value should not be the same as original");

        String decryptedValue = kubeconfigEncryptor.decrypt(encryptedValue);
        assertEquals(originalValue, decryptedValue, "Decrypted value should match the original");
    }

    /**
     * Tests that a string encrypted and then decrypted returns the original value using a 24-byte key.
     */
    @Test
    void encrypt_decrypt_with24ByteKey_shouldReturnOriginalValue() {
        KubeconfigEncryptor encryptor24 = new KubeconfigEncryptor(validKey24, validIv);
        String originalValue = "Kubeconfig test with a 24-byte key!";
        String encryptedValue = encryptor24.encrypt(originalValue);
        assertNotNull(encryptedValue, "Encrypted value should not be null");
        assertNotEquals(originalValue, encryptedValue, "Encrypted value should not be the same as original");

        String decryptedValue = encryptor24.decrypt(encryptedValue);
        assertEquals(originalValue, decryptedValue, "Decrypted value should match the original");
    }

    /**
     * Tests that a string encrypted and then decrypted returns the original value using a 32-byte key.
     */
    @Test
    void encrypt_decrypt_with32ByteKey_shouldReturnOriginalValue() {
        KubeconfigEncryptor encryptor32 = new KubeconfigEncryptor(validKey32, validIv);
        String originalValue = "Secure kubeconfig data with a 32-byte key.";
        String encryptedValue = encryptor32.encrypt(originalValue);
        assertNotNull(encryptedValue, "Encrypted value should not be null");
        assertNotEquals(originalValue, encryptedValue, "Encrypted value should not be the same as original");

        String decryptedValue = encryptor32.decrypt(encryptedValue);
        assertEquals(originalValue, decryptedValue, "Decrypted value should match the original");
    }


    /**
     * Tests that encrypting a null value returns null.
     */
    @Test
    void encrypt_nullValue_shouldReturnNull() {
        assertNull(kubeconfigEncryptor.encrypt(null), "Encrypting null should return null");
    }

    /**
     * Tests that decrypting a null value returns null.
     */
    @Test
    void decrypt_nullValue_shouldReturnNull() {
        assertNull(kubeconfigEncryptor.decrypt(null), "Decrypting null should return null");
    }

    /**
     * Tests that encrypting an empty string returns a non-empty encrypted string.
     */
    @Test
    void encrypt_emptyValue_shouldReturnEncryptedEmptyString() {
        String originalValue = "";
        String encryptedEmpty = kubeconfigEncryptor.encrypt(originalValue);
        assertNotNull(encryptedEmpty, "Encrypted empty string should not be null");
        assertTrue(encryptedEmpty.length() > 0, "Encrypted empty string should not be empty");
        assertNotEquals(originalValue, encryptedEmpty, "Encrypted empty string should differ from original");
    }

    /**
     * Tests that decrypting an encrypted empty string returns the original empty string.
     */
    @Test
    void decrypt_encryptedEmptyString_shouldReturnEmptyString() {
        String encryptedEmpty = kubeconfigEncryptor.encrypt("");
        String decryptedEmpty = kubeconfigEncryptor.decrypt(encryptedEmpty);
        assertEquals("", decryptedEmpty, "Decrypting an encrypted empty string should return an empty string");
    }

    /**
     * Tests constructor with an invalid (too short) encryption key length.
     * Expects an IllegalArgumentException.
     */
    @Test
    void constructor_invalidKeyLength_tooShort_shouldThrowIllegalArgumentException() {
        String invalidKey = "shortKey"; // Less than 16 bytes
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new KubeconfigEncryptor(invalidKey, validIv);
        });
        assertEquals("Encryption key must be 16, 24, or 32 bytes long for AES.", exception.getMessage());
    }

    /**
     * Tests constructor with an invalid (too long, not 16, 24 or 32) encryption key length.
     * Expects an IllegalArgumentException.
     */
    @Test
    void constructor_invalidKeyLength_intermediate_shouldThrowIllegalArgumentException() {
        String invalidKey = "ThisIsA20ByteTestKey"; // 20 bytes (not 16, 24, or 32)
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new KubeconfigEncryptor(invalidKey, validIv);
        });
        assertEquals("Encryption key must be 16, 24, or 32 bytes long for AES.", exception.getMessage());
    }


    /**
     * Tests constructor with an invalid (too short) IV length.
     * Expects an IllegalArgumentException.
     */
    @Test
    void constructor_invalidIvLength_tooShort_shouldThrowIllegalArgumentException() {
        String invalidIv = "shortIV"; // Less than 16 bytes
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new KubeconfigEncryptor(validKey16, invalidIv);
        });
        assertEquals("Initialization Vector must be 16 bytes long for AES.", exception.getMessage());
    }

    /**
     * Tests constructor with an invalid (too long) IV length.
     * Expects an IllegalArgumentException.
     */
    @Test
    void constructor_invalidIvLength_tooLong_shouldThrowIllegalArgumentException() {
        String invalidIv = "ThisIsAnIVThatIsTooLong"; // More than 16 bytes
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new KubeconfigEncryptor(validKey16, invalidIv);
        });
        assertEquals("Initialization Vector must be 16 bytes long for AES.", exception.getMessage());
    }

    /**
     * Tests decrypting a string that is not valid Base64.
     * Expects a RuntimeException (wrapping a decoding/decryption error).
     */
    @Test
    void decrypt_invalidBase64EncryptedString_shouldThrowRuntimeException() {
        String nonBase64String = "This is not a Base64 string %^&*";
        Exception exception = assertThrows(RuntimeException.class, () -> {
            kubeconfigEncryptor.decrypt(nonBase64String);
        });
        assertTrue(exception.getMessage().startsWith("Error decrypting kubeconfig"),
                "Exception message should indicate decryption error.");
    }

    /**
     * Tests decrypting a valid Base64 string that is not a valid AES encrypted payload (e.g., wrong key or corrupted).
     * Expects a RuntimeException.
     */
    @Test
    void decrypt_validBase64ButInvalidAesPayload_shouldThrowRuntimeException() {
        // Encrypt with a different key/IV
        KubeconfigEncryptor anotherEncryptor = new KubeconfigEncryptor(validKey32, "OtherValidIv!!!!");
        String encryptedWithDifferentContext = anotherEncryptor.encrypt("some data");

        // Attempt to decrypt with the main kubeconfigEncryptor instance
        Exception exception = assertThrows(RuntimeException.class, () -> {
            kubeconfigEncryptor.decrypt(encryptedWithDifferentContext);
        });
        assertTrue(exception.getMessage().startsWith("Error decrypting kubeconfig"),
                "Exception message should indicate decryption error.");
        // The cause is typically something like BadPaddingException or IllegalBlockSizeException
        // javax.crypto.BadPaddingException is common if the key/IV is wrong or data is corrupt
        assertNotNull(exception.getCause(), "Cause of RuntimeException should not be null");
    }
}