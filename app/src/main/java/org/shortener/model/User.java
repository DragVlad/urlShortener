package org.shortener.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User {

    private final UUID id;
    private final List<String> linkCodes;

    public User(UUID id) {
        this.id = id;
        this.linkCodes = new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public List<String> getLinkCodes() {
        return linkCodes;
    }

    public void addLinkCode(String code) {
        linkCodes.add(code);
    }

    public void removeLinkCode(String code) {
        linkCodes.remove(code);
    }
}