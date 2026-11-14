package org.shortener.util;

import java.util.UUID;
import java.util.function.Predicate;

public class ShortCodeGenerator {

    private final Predicate<String> codeExistsChecker;
    private final int codeLength;

    public ShortCodeGenerator(Predicate<String> codeExistsChecker, int codeLength) {
        this.codeExistsChecker = codeExistsChecker;
        this.codeLength = codeLength;
    }

    public String generateUniqueCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().replace("-", "").substring(0, codeLength);
        } while (codeExistsChecker.test(code));
        return code;
    }
}