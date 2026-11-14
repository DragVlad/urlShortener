package org.shortener;

import org.shortener.cli.CommandParser;
import org.shortener.cli.ConsoleApp;
import org.shortener.config.AppConfig;
import org.shortener.dao.ShortLinkDao;
import org.shortener.dao.UserDao;
import org.shortener.dao.impl.InMemoryShortLinkDao;
import org.shortener.dao.impl.InMemoryUserDao;
import org.shortener.service.LinkService;
import org.shortener.service.NotificationService;
import org.shortener.service.UserService;
import org.shortener.util.BrowserOpener;
import org.shortener.util.ShortCodeGenerator;
import org.shortener.util.UrlValidator;

public class Main {
    public static void main(String[] args) {
        AppConfig appConfig = AppConfig.load("application.properties");

        UserDao userDao = new InMemoryUserDao();
        ShortLinkDao shortLinkDao = new InMemoryShortLinkDao();

        ShortCodeGenerator codeGenerator = new ShortCodeGenerator(shortLinkDao::existsByCode,
                appConfig.getCodeLength());
        UrlValidator urlValidator = new UrlValidator();
        BrowserOpener browserOpener = new BrowserOpener();
        NotificationService notificationService = new NotificationService();

        UserService userService = new UserService(userDao);
        LinkService linkService = new LinkService(shortLinkDao, userDao, codeGenerator, urlValidator, appConfig,
                notificationService);
        CommandParser commandParser = new CommandParser();

        ConsoleApp consoleApp = new ConsoleApp(commandParser, linkService, userService, notificationService,
                browserOpener, appConfig);
        consoleApp.start();
    }
}