package org.shortener;

import org.shortener.cli.CommandParser;
import org.shortener.cli.ConsoleApp;

public class Main {
    public static void main(String[] args) {
        CommandParser commandParser = new CommandParser();
        ConsoleApp consoleApp = new ConsoleApp(commandParser);
        consoleApp.start();
    }
}