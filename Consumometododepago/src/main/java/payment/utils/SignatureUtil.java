package payment.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class SignatureUtil {

    public static String generateSignature(String reference,
                                           int amount,
                                           String currency,
                                           String integrityKey) {
        try {
            // Forzar String explícito para evitar problemas de concatenación
            String data = reference + String.valueOf(amount) + currency + integrityKey;

            // LOG para verificar la cadena exacta que se firma
            System.out.println("======== SIGNATURE DEBUG ========");
            System.out.println("Cadena a firmar: [" + data + "]");

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            String signature = hexString.toString();
            System.out.println("Firma generada: " + signature);
            System.out.println("=================================");

            return signature;

        } catch (Exception e) {
            throw new RuntimeException("Error generando la firma", e);
        }
    }
}