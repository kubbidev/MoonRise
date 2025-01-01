package me.kubbidev.moonrise.common.database.implementation.sql.connection.hikari;

import me.kubbidev.moonrise.common.database.implementation.sql.StatementProcessor;
import me.kubbidev.moonrise.common.database.misc.DatabaseCredentials;

public class MariaDbConnectionFactory extends DriverBasedHikariConnectionFactory {
    public MariaDbConnectionFactory(DatabaseCredentials configuration) {
        super(configuration);
    }

    @Override
    public String getImplementationName() {
        return "MariaDB";
    }

    @Override
    protected String defaultPort() {
        return "3306";
    }

    @Override
    protected String driverClassName() {
        return "org.mariadb.jdbc.Driver";
    }

    @Override
    protected String driverJdbcIdentifier() {
        return "mariadb";
    }

    @Override
    public StatementProcessor getStatementProcessor() {
        return StatementProcessor.USE_BACKTICKS;
    }
}
