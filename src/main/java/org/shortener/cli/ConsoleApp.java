package org.shortener.cli;

import java.util.Scanner;

public class ConsoleApp {

    private final CommandParser commandParser;
    private final Scanner scanner;
    private boolean running = true;

    public ConsoleApp(CommandParser commandParser) {
        this.commandParser = commandParser;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("Сервис сокращения ссылок. Введите help для списка команд.");
        while (running) {
            System.out.print("> ");
            String inputLine = scanner.nextLine();
            Command command = commandParser.parseCommand(inputLine);
            String[] arguments = commandParser.parseArguments(inputLine);
            handleCommand(command, arguments);
        }
        System.out.println("Работа завершена.");
        scanner.close();
    }

    private void handleCommand(Command command, String[] arguments) {
        switch (command) {
            case HELP -> printHelp();
            case EXIT -> running = false;
            case UNKNOWN -> System.out.println("Неизвестная команда. Введите help для списка команд.");
            default -> System.out.println("Команда пока не реализована.");
        }
    }

    private void printHelp() {
        System.out.println("Доступные команды:");
        System.out.println("  create <url> [limit]      - сократить ссылку");
        System.out.println("  open <code>               - открыть короткую ссылку");
        System.out.println("  edit_limit <code> <limit> - изменить лимит переходов");
        System.out.println("  delete <code>             - удалить ссылку");
        System.out.println("  list                      - список ваших ссылок");
        System.out.println("  help                      - показать эту справку");
        System.out.println("  exit                      - выход");
    }
}