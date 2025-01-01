package me.kubbidev.moonrise.common.database.implementation.sql;

import me.kubbidev.moonrise.common.database.DatabaseMetadata;
import me.kubbidev.moonrise.common.database.implementation.DatabaseImplementation;
import me.kubbidev.moonrise.common.database.implementation.sql.connection.ConnectionFactory;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import me.kubbidev.moonrise.common.util.ImmutableCollectors;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SqlDatabase implements DatabaseImplementation {
    private final MoonRisePlugin plugin;

    private final ConnectionFactory connectionFactory;
    private final StatementProcessor statementProcessor;

    public SqlDatabase(MoonRisePlugin plugin, ConnectionFactory connectionFactory, String prefix) {
        this.plugin = plugin;
        this.connectionFactory = connectionFactory;
        this.statementProcessor = connectionFactory.getStatementProcessor().compose(s -> s.replace("{prefix}", prefix));
    }

    @Override
    public MoonRisePlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public String getImplementationName() {
        return this.connectionFactory.getImplementationName();
    }

    public ConnectionFactory getConnectionFactory() {
        return this.connectionFactory;
    }

    public StatementProcessor getStatementProcessor() {
        return this.statementProcessor;
    }

    @Override
    public void init() throws Exception {
        this.connectionFactory.init(this.plugin);

        List<String> tables;
        try (Connection c = this.connectionFactory.getConnection()) {
            tables = listTables(c);
        }
    }

    @Override
    public void shutdown() {
        try {
            this.connectionFactory.shutdown();
        } catch (Exception e) {
            this.plugin.getLogger().severe("Exception whilst disabling SQL database", e);
        }
    }

    @Override
    public DatabaseMetadata getMeta() {
        return this.connectionFactory.getMeta();
    }

    private static List<String> listTables(Connection connection) throws SQLException {
        List<String> tables = new ArrayList<>();
        try (ResultSet rs = connection.getMetaData().getTables(connection.getCatalog(), null, "%", null)) {
            while (rs.next()) tables.add(rs.getString(3).toLowerCase(Locale.ROOT));
        }
        return tables;
    }

    private void applySchema(List<String> existingTables) throws IOException, SQLException {
        String schemaFileName = "me/kubbidev/moonrise/schema/" + this.getImplementationName().toLowerCase(Locale.ROOT) + ".sql";

        List<String> statements;
        try (InputStream is = this.plugin.getBootstrap().getResourceStream(schemaFileName)) {
            if (is == null) {
                throw new IOException("Couldn't locate schema file for " + this.getImplementationName());
            }

            statements = SchemaReader.getStatements(is).stream()
                    .map(this.statementProcessor::process).collect(ImmutableCollectors.toList());
        }

        statements = SchemaReader.filterStatements(statements, existingTables);
        if (statements.isEmpty()) return;

        try (Connection connection = this.connectionFactory.getConnection()) {
            boolean utf8mb4Unsupported = false;

            try (Statement s = connection.createStatement()) {
                for (String query : statements) {
                    s.addBatch(query);
                }

                try {
                    s.executeBatch();
                } catch (BatchUpdateException e) {
                    if (e.getMessage().contains("Unknown character set")) {
                        utf8mb4Unsupported = true;
                    } else {
                        throw e;
                    }
                }
            }

            // try again
            if (utf8mb4Unsupported) {
                try (Statement s = connection.createStatement()) {
                    for (String query : statements) {
                        s.addBatch(query.replace("utf8mb4", "utf8"));
                    }

                    s.executeBatch();
                }
            }
        }
    }

}
