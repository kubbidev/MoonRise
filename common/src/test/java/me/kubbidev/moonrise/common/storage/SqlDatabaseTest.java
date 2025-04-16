package me.kubbidev.moonrise.common.storage;

import me.kubbidev.moonrise.common.storage.implementation.StorageImplementation;
import me.kubbidev.moonrise.common.storage.implementation.sql.SqlStorage;
import me.kubbidev.moonrise.common.storage.implementation.sql.StatementProcessor;
import me.kubbidev.moonrise.common.storage.implementation.sql.connection.ConnectionFactory;
import me.kubbidev.moonrise.common.storage.implementation.sql.connection.file.H2ConnectionFactory;
import me.kubbidev.moonrise.common.storage.implementation.sql.connection.file.NonClosableConnection;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlDatabaseTest extends AbstractDatabaseTest {

    @Override
    protected StorageImplementation makeDatabase(MoonRisePlugin plugin) throws Exception {
        return new SqlStorage(plugin, new TestH2ConnectionFactory(), "moonrise_");
    }

    private static class TestH2ConnectionFactory implements ConnectionFactory {

        private final NonClosableConnection connection;

        TestH2ConnectionFactory() throws SQLException {
            this.connection = new NonClosableConnection(
                DriverManager.getConnection("jdbc:h2:mem:test")
            );
        }

        @Override
        public Connection getConnection() {
            return this.connection;
        }

        @Override
        public String getImplementationName() {
            return "H2";
        }

        @Override
        public StorageMetadata getMeta() {
            return new StorageMetadata();
        }

        @Override
        public void init(MoonRisePlugin plugin) {

        }

        @Override
        public StatementProcessor getStatementProcessor() {
            return H2ConnectionFactory.STATEMENT_PROCESSOR;
        }

        @Override
        public void shutdown() throws Exception {
            this.connection.shutdown();
        }
    }
}
