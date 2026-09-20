package com.jobtrail.backend.service;

import com.jobtrail.backend.exception.DecryptionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionServiceTest {

    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        encryptionService = new EncryptionService();
        ReflectionTestUtils.setField(encryptionService, "base64EncryptionKey", "MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=");
        encryptionService.init();
    }

    @Test
    void testEncryptDecrypt_Roundtrip() {
        String original = "my-secret-token";
        String encrypted = encryptionService.encrypt(original);
        assertNotNull(encrypted);
        assertNotEquals(original, encrypted);

        String decrypted = encryptionService.decrypt(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    void testDecrypt_WrongKey_ThrowsException() {
        String original = "my-secret-token";
        String encrypted = encryptionService.encrypt(original);

        EncryptionService wrongService = new EncryptionService();
        ReflectionTestUtils.setField(wrongService, "base64EncryptionKey", "MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTM=");
        wrongService.init();

        assertThrows(DecryptionException.class, () -> wrongService.decrypt(encrypted));
    }

    @Test
    void testInit_InvalidKeyLength() {
        EncryptionService badService = new EncryptionService();
        ReflectionTestUtils.setField(badService, "base64EncryptionKey", "c2hvcnQta2V5");
        assertThrows(IllegalArgumentException.class, badService::init);
    }
}
