package org.shortener.cli;

public class CommandParser {

    public Command parseCommand(String inputLine) {
        if (inputLine == null || inputLine.trim().isEmpty()) {
            return Command.UNKNOWN;
        }
        String[] tokens = inputLine.trim().split("\\s+");
        try {
            return Command.valueOf(tokens[0].toUpperCase());
        } catch (IllegalArgumentException exception) {
            return Command.UNKNOWN;
        }
    }

    public String[] parseArguments(String inputLine) {
        if (inputLine == null || inputLine.trim().isEmpty()) {
            return new String[0];
        }
        String[] tokens = inputLine.trim().split("\\s+");
        if (tokens.length <= 1) {
            return new String[0];
        }
        String[] arguments = new String[tokens.length - 1];
        System.arraycopy(tokens, 1, arguments, 0, arguments.length);
        return arguments;
    }

    public boolean hasArguments(String[] arguments, int expectedCount) {
        return arguments != null && arguments.length >= expectedCount;
    }
}