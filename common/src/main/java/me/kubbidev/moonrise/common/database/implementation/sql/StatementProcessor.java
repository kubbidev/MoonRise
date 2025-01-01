package me.kubbidev.moonrise.common.database.implementation.sql;

import java.util.Objects;

@FunctionalInterface
public interface StatementProcessor {

    StatementProcessor USE_BACKTICKS = s -> s.replace('\'', '`');

    StatementProcessor USE_DOUBLE_QUOTES = s -> s.replace('\'', '"');

    String process(String statement);

    default StatementProcessor compose(StatementProcessor before) {
        Objects.requireNonNull(before);
        return s -> process(before.process(s));
    }
}