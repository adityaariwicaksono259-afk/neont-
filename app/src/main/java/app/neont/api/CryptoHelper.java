package app.neont.api;

import android.util.Base64;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoHelper {
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    // Kunci diturunkan dari roomId (agar 2 user di room yang sama dapat kunci yang sama)
    private static SecretKey getKey(String roomId) throws Exception {
        byte[] keyBytes = new byte[32]; // 256-bit
        byte[] roomBytes = roomId.getBytes("UTF-8");
        for (int i = 0; i < keyBytes.length; i++) {
            keyBytes[i] = roomBytes[i % roomBytes.length];
        }
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static String encrypt(String plainText, String roomId) {
        try {
            SecretKey key = getKey(roomId);
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes("UTF-8"));
            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

            return Base64.encodeToString(combined, Base64.NO_WRAP);
        } catch (Exception e) {
            return plainText; // Fallback jika gagal
        }
    }

    public static String decrypt(String cipherText, String roomId) {
        try {
            byte[] combined = Base64.decode(cipherText, Base64.NO_WRAP);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            System.arraycopy(combined, iv.length, encrypted, 0, encrypted.length);

            SecretKey key = getKey(roomId);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] plainText = cipher.doFinal(encrypted);
            return new String(plainText, "UTF-8");
        } catch (Exception e) {
            return cipherText; // Fallback jika gagal dekripsi
        }
    }
}
