package org.shortener;

import org.shortener.cli.CommandParser;
import org.shortener.cli.ConsoleApp;
import org.shortener.config.AppConfig;

public class Main {
    public static void main(String[] args) {
        AppConfig appConfig = AppConfig.load("application.properties");
        System.out.println("TTL ссылки: " + appConfig.getLinkTtlSeconds() + " сек.");
        CommandParser commandParser = new CommandParser();
        ConsoleApp consoleApp = new ConsoleApp(commandParser);
        consoleApp.start();
    }
}