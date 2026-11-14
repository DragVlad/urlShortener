package org.shortener.cli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandParserTest {

    private final CommandParser parser = new CommandParser();

    @Test
    void parsesKnownCommandsCaseInsensitive() {
        assertEquals(Command.CREATE, parser.parseCommand("create"));
        assertEquals(Command.CREATE, parser.parseCommand("CREATE"));
        assertEquals(Command.OPEN, parser.parseCommand("OpEn"));
        assertEquals(Command.EDIT_LIMIT, parser.parseCommand("edit_limit"));
        assertEquals(Command.DELETE, parser.parseCommand("delete"));
        assertEquals(Command.LIST, parser.parseCommand("list"));
        assertEquals(Command.HELP, parser.parseCommand("help"));
        assertEquals(Command.EXIT, parser.parseCommand("exit"));
    }

    @Test
    void returnsUnknownForBlankAndNull() {
        assertEquals(Command.UNKNOWN, parser.parseCommand(null));
        assertEquals(Command.UNKNOWN, parser.parseCommand(""));
        assertEquals(Command.UNKNOWN, parser.parseCommand("   "));
    }

    @Test
    void returnsUnknownForUnknownCommand() {
        assertEquals(Command.UNKNOWN, parser.parseCommand("foobar"));
    }

    @Test
    void parseArgumentsReturnsEmptyForCommandOnly() {
        assertArrayEquals(new String[0], parser.parseArguments("help"));
        assertArrayEquals(new String[0], parser.parseArguments("   "));
        assertArrayEquals(new String[0], parser.parseArguments(null));
    }

    @Test
    void parseArgumentsReturnsArgumentsWithoutCommand() {
        String[] args = parser.parseArguments("create https://example.com 10");
        assertArrayEquals(new String[]{"https://example.com", "10"}, args);
    }

    @Test
    void parseArgumentsHandlesExtraWhitespace() {
        String[] args = parser.parseArguments("  create    https://example.com    10  ");
        assertArrayEquals(new String[]{"https://example.com", "10"}, args);
    }

    @Test
    void hasArgumentsChecksCount() {
        assertTrue(parser.hasArguments(new String[]{"a"}, 1));
        assertTrue(parser.hasArguments(new String[]{"a", "b"}, 2));
        assertTrue(parser.hasArguments(new String[]{"a", "b"}, 1));
        assertFalse(parser.hasArguments(new String[]{"a"}, 2));
        assertFalse(parser.hasArguments(new String[0], 1));
        assertFalse(parser.hasArguments(null, 0));
    }
}