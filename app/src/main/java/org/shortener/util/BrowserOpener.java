package org.shortener.util;

import java.awt.Desktop;
import java.net.URI;

public class BrowserOpener {

    public boolean open(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                return true;
            }
            System.out.println("Открытие браузера недоступно в этой среде. URL: " + url);
            return false;
        } catch (Exception exception) {
            System.out.println("Не удалось открыть браузер. URL: " + url);
            return false;
        }
    }
}