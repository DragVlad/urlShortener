package org.shortener.service;

import org.shortener.model.ShortLink;

public class NotificationService {

    public void notifyLimitReached(ShortLink shortLink) {
        System.out.println("Уведомление: лимит переходов для ссылки " + shortLink.getCode() + " исчерпан.");
    }

    public void notifyExpired(ShortLink shortLink) {
        System.out.println("Уведомление: срок жизни ссылки " + shortLink.getCode() + " истёк. Ссылка удалена.");
    }

    public void notifyAccessDenied(String reason) {
        System.out.println("Отказано в доступе: " + reason);
    }

    public void notifyError(String message) {
        System.out.println("Ошибка: " + message);
    }

    public void notifyInfo(String message) {
        System.out.println(message);
    }
}