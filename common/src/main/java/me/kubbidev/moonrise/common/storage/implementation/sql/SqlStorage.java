package me.kubbidev.moonrise.common.storage.implementation.sql;

import me.kubbidev.moonrise.common.model.ApiGuild;
import me.kubbidev.moonrise.common.model.ApiMember;
import me.kubbidev.moonrise.common.model.Snowflake;
import me.kubbidev.moonrise.common.model.ApiUser;
import me.kubbidev.moonrise.common.storage.StorageMetadata;
import me.kubbidev.moonrise.common.storage.implementation.StorageImplementation;
import me.kubbidev.moonrise.common.storage.implementation.sql.connection.ConnectionFactory;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import me.kubbidev.moonrise.common.storage.misc.DataConstraints;
import me.kubbidev.moonrise.common.util.ImmutableCollectors;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.Instant;
import java.util.*;

public class SqlStorage implements StorageImplementation {

    private static final String USER_SELECT_BY_ID = "SELECT username, global_name, avatar, last_seen FROM '{prefix}users' WHERE id=? LIMIT 1";
    private static final String USER_UPDATE_BY_ID = "UPDATE '{prefix}users' SET username=?, global_name=?, avatar=?, last_seen=? WHERE id=?";
    private static final String USER_INSERT = "INSERT INTO '{prefix}users' (id, username, global_name, avatar, last_seen) VALUES(?, ?, ?, ?, ?)";
    private static final String USER_SELECT_IDS = "SELECT id FROM '{prefix}users'";

    private static final String GUILD_SELECT_BY_ID = "SELECT name, icon FROM '{prefix}guilds' WHERE id=? LIMIT 1";
    private static final String GUILD_UPDATE_BY_ID = "UPDATE '{prefix}guilds' SET name=?, icon=? WHERE id=?";
    private static final String GUILD_INSERT = "INSERT INTO '{prefix}guilds' (id, name, icon) VALUES(?, ?, ?)";
    private static final String GUILD_SELECT_IDS = "SELECT id FROM '{prefix}guilds'";

    private static final String MEMBER_SELECT_BY_IDS = "SELECT nickname, guild_avatar, biography FROM '{prefix}members' WHERE user_id=?, guild_id=? LIMIT 1";
    private static final String MEMBER_UPDATE_BY_IDS = "UPDATE '{prefix}members' SET nickname=?, guild_avatar=?, biography=? WHERE user_id=?, guild_id=?";
    private static final String MEMBER_INSERT = "INSERT INTO '{prefix}members' (user_id, guild_id, nickname, guild_avatar, biography) VALUES(?, ?, ?, ?, ?)";
    private static final String MEMBER_SELECT_USER_ID_BY_GUILD_ID = "SELECT user_id FROM '{prefix}members' WHERE guild_id=?";
    private static final String MEMBER_SELECT_USER_IDS = "SELECT user_id FROM '{prefix}members'";

    private final MoonRisePlugin plugin;

    private final ConnectionFactory connectionFactory;
    private final StatementProcessor statementProcessor;

    public SqlStorage(MoonRisePlugin plugin, ConnectionFactory connectionFactory, String prefix) {
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
        applySchema(tables);
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
    public StorageMetadata getMeta() {
        return this.connectionFactory.getMeta();
    }

    @Override
    public ApiUser loadUser(long userId) throws Exception {
        return this.bootstrapUser(new ApiUser(userId, this.plugin));
    }

    private <T extends ApiUser> T bootstrapUser(T user) throws SQLException {
        return this.GET(USER_SELECT_BY_ID, ps -> ps.setLong(1, user.getId()), rs -> {
            if (rs.next()) {
                user.setUsername(rs.getString("username"));
                user.setGlobalName(rs.getString("global_name"));
                user.setAvatar(rs.getString("avatar"));
                user.setLastSeen(Instant.ofEpochMilli(rs.getLong("last_seen")));
            }
            return user;
        });
    }

    @Override
    public ApiGuild loadGuild(long guildId) throws Exception {
        return this.GET(GUILD_SELECT_BY_ID, ps -> ps.setLong(1, guildId), rs -> {
            ApiGuild guild = new ApiGuild(guildId, this.plugin);
            if (rs.next()) {
                guild.setName(rs.getString("name"));
                guild.setIcon(rs.getString("icon"));
            }
            return guild;
        });
    }

    @Override
    public ApiMember loadMember(long guildId, long userId) throws Exception {
        return this.GET(MEMBER_SELECT_BY_IDS, ps -> {
            ps.setLong(1, userId);
            ps.setLong(2, guildId);
        }, rs -> {
            ApiMember member = new ApiMember(userId, this.plugin);
            member.setGuildId(guildId);

            this.bootstrapUser(member);
            if (rs.next()) {
                member.setNickname(rs.getString("nickname"));
                member.setGuildAvatar(rs.getString("guild_avatar"));
                member.setBiography(rs.getString("biography"));
            }
            return member;
        });
    }

    @Override
    public void saveUser(ApiUser user) throws Exception {
        SAVE(USER_SELECT_BY_ID, USER_UPDATE_BY_ID, USER_INSERT,
                ps -> ps.setLong(1, user.getId()),
                ps -> {
                    // longs
                    ps.setLong(4, user.getLastSeen().toEpochMilli());
                    ps.setLong(5, user.getId());

                    // strings
                    ps.setString(1, DataConstraints.desanitize(user.getUsername(), true));
                    ps.setString(2, DataConstraints.desanitize(user.getGlobalName().orElse(null)));
                    ps.setString(3, DataConstraints.desanitize(user.getAvatar()));
                },
                ps -> {
                    // longs
                    ps.setLong(1, user.getId());
                    ps.setLong(5, user.getLastSeen().toEpochMilli());

                    // strings
                    ps.setString(2, DataConstraints.desanitize(user.getUsername(), true));
                    ps.setString(3, DataConstraints.desanitize(user.getGlobalName().orElse(null)));
                    ps.setString(4, DataConstraints.desanitize(user.getAvatar()));
                });

    }

    @Override
    public void saveGuild(ApiGuild guild) throws Exception {
        SAVE(GUILD_SELECT_BY_ID, GUILD_UPDATE_BY_ID, GUILD_INSERT,
                ps -> ps.setLong(1, guild.getId()),
                ps -> {
                    // longs
                    ps.setLong(3, guild.getId());

                    // strings
                    ps.setString(1, DataConstraints.desanitize(guild.getName()));
                    ps.setString(2, DataConstraints.desanitize(guild.getIcon()));
                },
                ps -> {
                    // longs
                    ps.setLong(1, guild.getId());

                    // strings
                    ps.setString(2, DataConstraints.desanitize(guild.getName()));
                    ps.setString(3, DataConstraints.desanitize(guild.getIcon()));
                });
    }

    @Override
    public void saveMember(ApiMember member) throws Exception {
        SAVE(MEMBER_SELECT_BY_IDS, MEMBER_UPDATE_BY_IDS, MEMBER_INSERT,
                ps -> {
                    ps.setLong(1, member.getId());
                    ps.setLong(2, member.getGuildId());
                },
                ps -> {
                    // longs
                    ps.setLong(4, member.getId());
                    ps.setLong(5, member.getGuildId());

                    // strings
                    ps.setString(1, DataConstraints.desanitize(member.getNickname().orElse(null), true));
                    ps.setString(2, DataConstraints.desanitize(member.getGuildAvatar()));
                    ps.setString(3, DataConstraints.desanitize(member.getBiography().orElse(null)));
                },
                ps -> {
                    // longs
                    ps.setLong(1, member.getId());
                    ps.setLong(2, member.getGuildId());

                    // strings
                    ps.setString(3, DataConstraints.desanitize(member.getNickname().orElse(null), true));
                    ps.setString(4, DataConstraints.desanitize(member.getGuildAvatar()));
                    ps.setString(5, DataConstraints.desanitize(member.getBiography().orElse(null)));
                });
    }

    @Override
    public Set<Snowflake> getUniqueUsers() throws Exception {
        return ALL(USER_SELECT_IDS, null, rs -> Snowflake.of(rs.getLong("id")));
    }

    @Override
    public Set<Snowflake> getUniqueGuilds() throws Exception {
        return ALL(GUILD_SELECT_IDS, null, rs -> Snowflake.of(rs.getLong("id")));
    }

    @Override
    public Set<Snowflake> getUniqueMembers() throws Exception {
        return ALL(MEMBER_SELECT_USER_IDS, null, rs -> Snowflake.of(rs.getLong("user_id")));
    }

    @Override
    public Set<Snowflake> getUniqueMembers(long guildId) throws Exception {
        return ALL(MEMBER_SELECT_USER_ID_BY_GUILD_ID, ps -> ps.setLong(1, guildId), rs -> Snowflake.of(rs.getLong("user_id")));
    }

    @FunctionalInterface
    private interface RSFunction<T> {
        T apply(ResultSet rs) throws SQLException;
    }

    @FunctionalInterface
    private interface PSConsumer {
        void accept(PreparedStatement ps) throws SQLException;
    }

    private <T> T GET(
            String query, @Nullable PSConsumer ps, RSFunction<T> rs)
            throws SQLException {
        T t;
        Connection c = this.connectionFactory.getConnection();
        try (var statement = c.prepareStatement(this.statementProcessor.process(query))) {
            if (ps != null) ps.accept(statement);

            try (var result = statement.executeQuery()) {
                t = rs.apply(result);
            }
        }
        c.close();
        return t;
    }

    private <T> Set<T> ALL(String query, @Nullable PSConsumer ps, RSFunction<T> mapper) throws SQLException {
        return GET(query, ps, rs -> {
            Set<T> set = new HashSet<>();
            while (rs.next()) set.add(mapper.apply(rs));
            return set;
        });
    }

    private void SAVE(
            String exist, String update, String insert,
            @Nullable PSConsumer psExist,
            @Nullable PSConsumer psUpdate,
            @Nullable PSConsumer psInsert) throws SQLException {

        Connection c = this.connectionFactory.getConnection();
        boolean exists = EXISTS(c, exist, psExist);
        if (exists) {
            EXECUTE(c, update, psUpdate);
        } else {
            EXECUTE(c, insert, psInsert);
        }
        c.close();
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    private boolean EXISTS(Connection c, String query, @Nullable PSConsumer ps) throws SQLException {
        var statement = c.prepareStatement(this.statementProcessor.process(query));
        if (ps != null) ps.accept(statement);

        try (var rs = statement.executeQuery()) {
            return rs.next();
        } finally {
            statement.close();
        }
    }

    private void EXECUTE(Connection c, String query, @Nullable PSConsumer ps) throws SQLException {
        try (var statement = c.prepareStatement(this.statementProcessor.process(query))) {
            if (ps != null) ps.accept(statement);
            statement.execute();
        }
    }

    private static List<String> listTables(Connection connection) throws SQLException {
        List<String> tables = new ArrayList<>();
        try (var rs = connection.getMetaData().getTables(connection.getCatalog(), null, "%", null)) {
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