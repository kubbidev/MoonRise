package me.kubbidev.moonrise.common.storage.implementation.sql;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import me.kubbidev.moonrise.common.util.ImmutableCollectors;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SchemaReaderTest {

    private static List<String> readStatements(String type) throws IOException {
        List<String> statements;
        try (InputStream is = SchemaReaderTest.class.getResourceAsStream("/me/kubbidev/moonrise/schema/" + type + ".sql")) {
            if (is == null) {
                throw new IOException("Couldn't locate schema file");
            }

            statements = SchemaReader.getStatements(is);
        }
        return statements;
    }

    @Test
    public void testReadH2() throws IOException {
        assertEquals(ImmutableList.of(
                "CREATE TABLE `{prefix}users`( `id` BIGINT NOT NULL, `username` VARCHAR(32) NOT NULL, `global_name` VARCHAR(32) NOT NULL, `avatar` VARCHAR(300) NOT NULL, `last_seen` BIGINT NOT NULL, PRIMARY KEY (`id`))",
                "CREATE INDEX ON `{prefix}users` (`username`)",
                "CREATE TABLE `{prefix}guilds`( `id` BIGINT NOT NULL, `name` VARCHAR(100) NOT NULL, `icon` VARCHAR(300) NOT NULL, PRIMARY KEY (`id`))",
                "CREATE TABLE `{prefix}members`( `user_id` BIGINT NOT NULL, `guild_id` BIGINT NOT NULL, `nickname` VARCHAR(32) NOT NULL, `guild_avatar` VARCHAR(300) NOT NULL, `biography` VARCHAR(300) NOT NULL, PRIMARY KEY (`user_id`))",
                "CREATE INDEX ON `{prefix}members` (`guild_id`)"
        ), readStatements("h2"));
    }

    @Test
    public void testReadSqlite() throws IOException {
        assertEquals(ImmutableList.of(
                "CREATE TABLE `{prefix}users`( `id` BIGINT PRIMARY KEY NOT NULL, `username` VARCHAR(32) NOT NULL, `global_name` VARCHAR(32) NOT NULL, `avatar` VARCHAR(300) NOT NULL, `last_seen` BIGINT NOT NULL)",
                "CREATE INDEX `{prefix}users_username` ON `{prefix}users` (`username`)",
                "CREATE TABLE `{prefix}guilds`( `id` BIGINT PRIMARY KEY NOT NULL, `name` VARCHAR(100) NOT NULL, `icon` VARCHAR(300) NOT NULL)",
                "CREATE TABLE `{prefix}members`( `user_id` BIGINT PRIMARY KEY NOT NULL, `guild_id` BIGINT NOT NULL, `nickname` VARCHAR(32) NOT NULL, `guild_avatar` VARCHAR(300) NOT NULL, `biography` VARCHAR(300) NOT NULL)",
                "CREATE INDEX `{prefix}members_guild_id` ON `{prefix}members` (`guild_id`)"
        ), readStatements("sqlite"));
    }

    @Test
    public void testReadMysql() throws IOException {
        ImmutableList<String> expected = ImmutableList.of(
                "CREATE TABLE `{prefix}users`( `id` BIGINT NOT NULL, `username` VARCHAR(32) NOT NULL, `global_name` VARCHAR(32) NOT NULL, `avatar` VARCHAR(300) NOT NULL, `last_seen` BIGINT NOT NULL, PRIMARY KEY (`id`)) DEFAULT CHARSET = utf8mb4",
                "CREATE INDEX `{prefix}users_username` ON `{prefix}users` (`username`)",
                "CREATE TABLE `{prefix}guilds`( `id` BIGINT NOT NULL, `name` VARCHAR(100) NOT NULL, `icon` VARCHAR(300) NOT NULL, PRIMARY KEY (`id`)) DEFAULT CHARSET = utf8mb4",
                "CREATE TABLE `{prefix}members`( `user_id` BIGINT NOT NULL, `guild_id` BIGINT NOT NULL, `nickname` VARCHAR(32) NOT NULL, `guild_avatar` VARCHAR(300) NOT NULL, `biography` VARCHAR(300) NOT NULL, PRIMARY KEY (`user_id`)) DEFAULT CHARSET = utf8mb4",
                "CREATE INDEX `{prefix}members_guild_id` ON `{prefix}members` (`guild_id`)"
        );
        assertEquals(expected, readStatements("mysql"));
        assertEquals(expected, readStatements("mariadb"));
    }

    @Test
    public void testReadPostgres() throws IOException {
        assertEquals(ImmutableList.of(
                "CREATE TABLE \"{prefix}users\"( \"id\" BIGINT PRIMARY KEY NOT NULL, \"username\" VARCHAR(32) NOT NULL, \"global_name\" VARCHAR(32) NOT NULL, \"avatar\" VARCHAR(300) NOT NULL, \"last_seen\" BIGINT NOT NULL)",
                "CREATE INDEX \"{prefix}users_username\" ON \"{prefix}users\" (\"username\")",
                "CREATE TABLE \"{prefix}guilds\"( \"id\" BIGINT PRIMARY KEY NOT NULL, \"name\" VARCHAR(100) NOT NULL, \"icon\" VARCHAR(300) NOT NULL)",
                "CREATE TABLE \"{prefix}members\"( \"user_id\" BIGINT PRIMARY KEY NOT NULL, \"guild_id\" BIGINT NOT NULL, \"nickname\" VARCHAR(32) NOT NULL, \"guild_avatar\" VARCHAR(300) NOT NULL, \"biography\" VARCHAR(300) NOT NULL)",
                "CREATE INDEX \"{prefix}members_guild_id\" ON \"{prefix}members\" (\"guild_id\")"
        ), readStatements("postgresql"));
    }

    @Test
    public void testTableFromStatement() throws IOException {
        Set<String> allowedTables = ImmutableSet.of(
                "moonrise_users",
                "moonrise_guilds",
                "moonrise_members"
        );

        for (String type : new String[]{"h2", "mariadb", "mysql", "postgresql", "sqlite"}) {
            List<String> tables = readStatements(type).stream()
                    .map(s -> s.replace("{prefix}", "moonrise_"))
                    .map(SchemaReader::tableFromStatement)
                    .collect(ImmutableCollectors.toList());

            assertTrue(allowedTables.containsAll(tables));
        }
    }

    @Test
    public void testFilter() throws IOException {
        StatementProcessor processor = s -> s.replace("{prefix}", "moonrise_");
        List<String> statements = readStatements("mysql").stream().map(processor::process).collect(Collectors.toList());

        // no tables exist, all should be created
        List<String> filtered = SchemaReader.filterStatements(statements, ImmutableList.of());
        assertEquals(statements, filtered);

        // all tables exist, none should be created
        filtered = SchemaReader.filterStatements(statements, ImmutableList.of(
                "moonrise_users",
                "moonrise_guilds",
                "moonrise_members"
        ));
        assertEquals(ImmutableList.of(), filtered);

        // some tables exist, some should be created
        filtered = SchemaReader.filterStatements(statements, ImmutableList.of(
                "moonrise_users",
                "moonrise_members"
        ));
        assertEquals(ImmutableList.of(
                "CREATE TABLE `moonrise_guilds`( `id` BIGINT NOT NULL, `name` VARCHAR(100) NOT NULL, `icon` VARCHAR(300) NOT NULL, PRIMARY KEY (`id`)) DEFAULT CHARSET = utf8mb4"
        ), filtered);
    }
}
