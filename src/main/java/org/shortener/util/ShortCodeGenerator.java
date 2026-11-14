package org.shortener.util;

import java.security.SecureRandom;
import java.util.function.Predicate;

public class ShortCodeGenerator {

    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = ALPHABET.length(); // 62

    private final Predicate<String> codeExistsChecker;
    private final int codeLength;
    private final SecureRandom random;

    public ShortCodeGenerator(Predicate<String> codeExistsChecker, int codeLength) {
        this.codeExistsChecker = codeExistsChecker;
        this.codeLength = codeLength;
        this.random = new SecureRandom();
    }

    public String generateUniqueCode() {
        while (true) {
            long randomNumber = Math.abs(random.nextLong());
            String code = encodeToBase62(randomNumber, codeLength);

            if (!codeExistsChecker.test(code)) {
                return code;
            }
            long offset = 1;
            while (true) {
                code = encodeToBase62(randomNumber + offset, codeLength);
                if (!codeExistsChecker.test(code)) {
                    return code;
                }
                offset++;
            }
        }
    }

    private String encodeToBase62(long value, int length) {
        StringBuilder builder = new StringBuilder(length);
        long currentValue = value;

        for (int index = 0; index < length; index++) {
            int remainder = (int) (currentValue % BASE);
            builder.append(ALPHABET.charAt(remainder));
            currentValue /= BASE;
        }

        return builder.reverse().toString();
    }
}