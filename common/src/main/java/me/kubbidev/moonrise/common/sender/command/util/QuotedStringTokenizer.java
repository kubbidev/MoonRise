package me.kubbidev.moonrise.common.sender.command.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Tokenizes strings on whitespace, but ignoring whitespace enclosed within quotes.
 */
public class QuotedStringTokenizer {

    private final String string;
    private       int    cursor;

    public QuotedStringTokenizer(String string) {
        this.string = string;
    }

    public List<String> tokenize(boolean omitEmptyStringAtEnd) {
        List<String> output = new ArrayList<>();
        while (this.hasNext()) {
            output.add(this.readString());
        }

        if (!omitEmptyStringAtEnd && this.cursor > 0 && isWhitespace(peek(-1))) {
            output.add("");
        }
        return output;
    }

    @SuppressWarnings("UnnecessaryUnicodeEscape")
    private static boolean isQuoteCharacter(char c) {
        // return c == '"' || c == '“' || c == '”';
        return c == '\u0022' || c == '\u201C' || c == '\u201D';
    }

    private static boolean isWhitespace(char c) {
        return c == ' ';
    }

    private String readString() {
        if (isQuoteCharacter(peek())) {
            return this.readQuotedString();
        } else {
            return this.readUnquotedString();
        }
    }

    private String readUnquotedString() {
        int start = this.cursor;
        while (this.hasNext() && !isWhitespace(peek())) {
            this.skip();
        }

        int end = this.cursor;
        if (this.hasNext()) {
            this.skip(); // skip whitespace
        }

        return this.string.substring(start, end);
    }

    private String readQuotedString() {
        this.skip(); // skip start quote

        int start = this.cursor;
        while (this.hasNext() && !isQuoteCharacter(peek())) {
            this.skip();
        }

        int end = this.cursor;
        if (this.hasNext()) {
            this.skip(); // skip end quote
        }
        if (this.hasNext() && isWhitespace(peek())) {
            this.skip(); // skip whitespace
        }

        return this.string.substring(start, end);
    }

    private boolean hasNext() {
        return this.cursor + 1 <= this.string.length();
    }

    private char peek() {
        return this.string.charAt(this.cursor);
    }

    private char peek(int offset) {
        return this.string.charAt(this.cursor + offset);
    }

    private void skip() {
        this.cursor++;
    }
}