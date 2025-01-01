package me.kubbidev.moonrise.common.database.implementation.sql.connection;

import me.kubbidev.moonrise.common.database.DatabaseMetadata;
import me.kubbidev.moonrise.common.database.implementation.sql.StatementProcessor;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionFactory {
    String getImplementationName();

    void init(MoonRisePlugin plugin);

    void shutdown() throws Exception;

    DatabaseMetadata getMeta();

    StatementProcessor getStatementProcessor();

    Connection getConnection() throws SQLException;
}
