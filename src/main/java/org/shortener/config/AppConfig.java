package org.shortener.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {

    private final long linkTtlSeconds;
    private final int defaultVisitLimit;
    private final int maxVisitLimit;
    private final int codeLength;

    private AppConfig(long linkTtlSeconds, int defaultVisitLimit, int maxVisitLimit, int codeLength) {
        this.linkTtlSeconds = linkTtlSeconds;
        this.defaultVisitLimit = defaultVisitLimit;
        this.maxVisitLimit = maxVisitLimit;
        this.codeLength = codeLength;
    }

    public static AppConfig load(String resourceName) {
        Properties properties = new Properties();
        try (InputStream inputStream = AppConfig.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new IllegalStateException("Файл конфигурации не найден: " + resourceName);
            }
            properties.load(inputStream);
        } catch (IOException exception) {
            throw new IllegalStateException("Ошибка чтения конфигурации", exception);
        }

        long linkTtlSeconds = Long.parseLong(properties.getProperty("link.ttl.seconds", "86400"));
        int defaultVisitLimit = Integer.parseInt(properties.getProperty("link.default.limit", "100"));
        int maxVisitLimit = Integer.parseInt(properties.getProperty("link.max.limit", "1000000"));
        int codeLength = Integer.parseInt(properties.getProperty("link.code.length", "8"));

        return new AppConfig(linkTtlSeconds, defaultVisitLimit, maxVisitLimit, codeLength);
    }

    public long getLinkTtlSeconds() {
        return linkTtlSeconds;
    }

    public int getDefaultVisitLimit() {
        return defaultVisitLimit;
    }

    public int getMaxVisitLimit() {
        return maxVisitLimit;
    }

    public int getCodeLength() {
        return codeLength;
    }
}