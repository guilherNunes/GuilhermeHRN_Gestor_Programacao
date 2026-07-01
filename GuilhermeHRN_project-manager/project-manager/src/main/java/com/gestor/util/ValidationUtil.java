package com.gestor.util;

import java.util.regex.Pattern;

/**
 * Utilitário para validar entradas do usuário.
 * Requisito: Validação de campos obrigatórios, tipos e formatos.
 */
public class ValidationUtil {
    
    // Regex para validar e-mail (Requisito: Validação de e-mail)
    // Mais robusto para cobrir a maioria dos casos, mas não todos os edge cases (que seriam complexos demais para um regex simples)
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";

    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return Pattern.compile(EMAIL_REGEX).matcher(email).matches();
    }

    public static boolean isNumeric(String text) {
        if (text == null) return false;
        try {
            Double.parseDouble(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean hasLength(String text, int min, int max) {
        if (text == null) return false;
        int length = text.length();
        return length >= min && length <= max;
    }

    public static boolean hasMaxLength(String text, int max) {
        if (text == null) return true; // Considera nulo como válido se só há max length
        return text.length() <= max;
    }
}
