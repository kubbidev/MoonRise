package me.kubbidev.moonrise.common.storage.implementation.sql.connection.file;

import me.kubbidev.moonrise.common.storage.implementation.sql.StatementProcessor;
import me.kubbidev.moonrise.common.dependencies.Dependency;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;

import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Properties;

public class H2ConnectionFactory extends FlatfileConnectionFactory {
    public static final StatementProcessor STATEMENT_PROCESSOR = s -> s
            .replace('\'', '`')
            .replace("LIKE", "ILIKE")
            .replace("value", "`value`")
            .replace("``value``", "`value`");

    private Constructor<?> connectionConstructor;

    public H2ConnectionFactory(Path file) {
        super(file);
    }

    @Override
    public String getImplementationName() {
        return "H2";
    }

    @Override
    public void init(MoonRisePlugin plugin) {
        ClassLoader classLoader = plugin.getDependencyManager().obtainClassLoaderWith(EnumSet.of(Dependency.H2_DRIVER));
        try {
            Class<?> connectionClass = classLoader.loadClass("org.h2.jdbc.JdbcConnection");
            this.connectionConstructor = connectionClass.getConstructor(String.class, Properties.class, String.class, Object.class, boolean.class);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Connection createConnection(Path file) throws SQLException {
        try {
            return (Connection) this.connectionConstructor.newInstance("jdbc:h2:" + file.toString(), new Properties(), null, null, false);
        } catch (ReflectiveOperationException e) {
            if (e.getCause() instanceof SQLException) {
                throw (SQLException) e.getCause();
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Path getWriteFile() {
        // h2 appends '.mv.db' to the end of the database name
        Path writeFile = super.getWriteFile();
        return writeFile.getParent().resolve(writeFile.getFileName().toString() + ".mv.db");
    }

    @Override
    public StatementProcessor getStatementProcessor() {
        return STATEMENT_PROCESSOR;
    }
}
