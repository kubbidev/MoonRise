package me.kubbidev.moonrise.common.storage.implementation.sql.connection;

import me.kubbidev.moonrise.common.storage.StorageMetadata;
import me.kubbidev.moonrise.common.storage.implementation.sql.StatementProcessor;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionFactory {

    String getImplementationName();

    void init(MoonRisePlugin plugin);

    void shutdown() throws Exception;

    StorageMetadata getMeta();

    StatementProcessor getStatementProcessor();

    Connection getConnection() throws SQLException;
}
