package me.kubbidev.moonrise.common.database.implementation.sql;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertEquals(ImmutableList.of(), readStatements("h2"));
    }

    @Test
    public void testReadSqlite() throws IOException {
        assertEquals(ImmutableList.of(), readStatements("sqlite"));
    }

    @Test
    public void testReadMysql() throws IOException {
        assertEquals(ImmutableList.of(), readStatements("mysql"));
        assertEquals(ImmutableList.of(), readStatements("mariadb"));
    }

    @Test
    public void testReadPostgres() throws IOException {
        assertEquals(ImmutableList.of(), readStatements("postgresql"));
    }
}
