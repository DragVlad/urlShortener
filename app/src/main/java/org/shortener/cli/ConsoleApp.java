package org.shortener.cli;

import org.shortener.config.AppConfig;
import org.shortener.model.ShortLink;
import org.shortener.model.User;
import org.shortener.service.LinkService;
import org.shortener.service.NotificationService;
import org.shortener.service.UserService;
import org.shortener.util.BrowserOpener;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

public class ConsoleApp {

    private final CommandParser commandParser;
    private final LinkService linkService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final BrowserOpener browserOpener;
    private final AppConfig appConfig;
    private final Scanner scanner;
    private UUID currentUserId;
    private boolean running = true;

    public ConsoleApp(CommandParser commandParser,
            LinkService linkService,
            UserService userService,
            NotificationService notificationService,
            BrowserOpener browserOpener,
            AppConfig appConfig) {
        this.commandParser = commandParser;
        this.linkService = linkService;
        this.userService = userService;
        this.notificationService = notificationService;
        this.browserOpener = browserOpener;
        this.appConfig = appConfig;
        this.scanner = new Scanner(System.in);
        this.currentUserId = null;
    }

    public void start() {
        notificationService.notifyInfo("Сервис сокращения ссылок. Введите help для списка команд.");
        while (running) {
            System.out.print("> ");
            String inputLine = scanner.nextLine();
            Command command = commandParser.parseCommand(inputLine);
            String[] arguments = commandParser.parseArguments(inputLine);
            handleCommand(command, arguments);
        }
        notificationService.notifyInfo("Работа завершена.");
        scanner.close();
    }

    private void handleCommand(Command command, String[] arguments) {
        switch (command) {
            case CREATE -> handleCreate(arguments);
            case OPEN -> handleOpen(arguments);
            case EDIT_LIMIT -> handleEditLimit(arguments);
            case DELETE -> handleDelete(arguments);
            case LIST -> handleList();
            case HELP -> printHelp();
            case EXIT -> running = false;
            case UNKNOWN -> notificationService.notifyError("неизвестная команда. Введите help для списка команд.");
        }
    }

    private void handleCreate(String[] arguments) {
        if (!commandParser.hasArguments(arguments, 1)) {
            notificationService.notifyError("использование: create <url> [limit]");
            return;
        }

        String url = arguments[0];
        int limit = 0;
        if (arguments.length >= 2) {
            try {
                limit = Integer.parseInt(arguments[1]);
            } catch (NumberFormatException exception) {
                notificationService.notifyError("лимит должен быть числом.");
                return;
            }
        }

        if (currentUserId == null) {
            User newUser = userService.getOrCreateUser(null);
            currentUserId = newUser.getId();
            notificationService.notifyInfo("Создан новый пользователь. Ваш UUID: " + currentUserId);
        }

        Optional<ShortLink> createdLink = linkService.createLink(currentUserId, url, limit);
        createdLink.ifPresent(link -> notificationService.notifyInfo(
                "Короткая ссылка: " + appConfig.getLinkBaseUrl() + link.getCode()
                        + " (лимит: " + link.getVisitLimit() + ")"));
    }

    private void handleOpen(String[] arguments) {
        if (!commandParser.hasArguments(arguments, 1)) {
            notificationService.notifyError("использование: open <code> или open <полная_ссылка>");
            return;
        }

        String shortCode = extractCode(arguments[0]);
        Optional<String> originalUrl = linkService.openLink(shortCode);
        originalUrl.ifPresent(url -> browserOpener.open(url));
    }

    private void handleEditLimit(String[] arguments) {
        if (!checkUserExists())
            return;

        if (!commandParser.hasArguments(arguments, 2)) {
            notificationService.notifyError("использование: edit_limit <code> <limit>");
            return;
        }

        String shortCode = extractCode(arguments[0]);
        int newLimit;
        try {
            newLimit = Integer.parseInt(arguments[1]);
        } catch (NumberFormatException exception) {
            notificationService.notifyError("лимит должен быть числом.");
            return;
        }

        linkService.editLimit(currentUserId, shortCode, newLimit);
    }

    private void handleDelete(String[] arguments) {
        if (!checkUserExists())
            return;

        if (!commandParser.hasArguments(arguments, 1)) {
            notificationService.notifyError("использование: delete <code>");
            return;
        }

        String shortCode = extractCode(arguments[0]);
        linkService.deleteLink(currentUserId, shortCode);
    }

    private void handleList() {
        if (!checkUserExists())
            return;

        List<ShortLink> ownedLinks = linkService.findLinksByOwner(currentUserId);
        if (ownedLinks.isEmpty()) {
            notificationService.notifyInfo("У вас пока нет ссылок.");
            return;
        }

        notificationService.notifyInfo("Ваши ссылки:");
        for (ShortLink shortLink : ownedLinks) {
            System.out.println("  " + appConfig.getLinkBaseUrl() + shortLink.getCode()
                    + " | " + shortLink.getOriginalUrl()
                    + " | статус: " + shortLink.getStatus()
                    + " | переходы: " + shortLink.getVisitCount() + "/" + shortLink.getVisitLimit()
                    + " | истекает: " + shortLink.getExpiresAt());
        }
    }

    private boolean checkUserExists() {
        if (currentUserId == null) {
            notificationService.notifyError("пользователь ещё не создан. Сначала создайте ссылку командой create.");
            return false;
        }
        return true;
    }

    private String extractCode(String input) {
        if (input.contains("/")) {
            return input.substring(input.lastIndexOf("/") + 1);
        }
        return input;
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