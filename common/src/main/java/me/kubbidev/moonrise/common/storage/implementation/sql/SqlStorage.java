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

    private static final String USER_SELECT_BY_ID = "SELECT" +
            " username, global_name, avatar, last_seen FROM '{prefix}users'" +
            " WHERE id=? LIMIT 1";

    private static final String USER_UPDATE_BY_ID = "UPDATE '{prefix}users' SET" +
            " username=?, global_name=?, avatar=?, last_seen=?" +
            " WHERE id=?";

    private static final String USER_INSERT = "INSERT INTO '{prefix}users' " +
            "(id, username, global_name, avatar, last_seen)" +
            " VALUES(?, ?, ?, ?, ?)";
    private static final String USER_SELECT_IDS
            = "SELECT id FROM '{prefix}users'";

    private static final String GUILD_SELECT_ALL = "SELECT " +
            "name, icon, leaderboard, leaderboard_channel FROM '{prefix}guilds'";

    private static final String GUILD_SELECT_BY_ID = "SELECT " +
            "name, icon, leaderboard, leaderboard_channel FROM '{prefix}guilds'" +
            " WHERE id=? LIMIT 1";

    private static final String GUILD_UPDATE_BY_ID = "UPDATE '{prefix}guilds'" +
            " SET name=?, icon=?, leaderboard=?, leaderboard_channel=?" +
            " WHERE id=?";

    private static final String GUILD_INSERT = "INSERT INTO '{prefix}guilds' " +
            "(id, name, icon, leaderboard, leaderboard_channel) " +
            "VALUES(?, ?, ?, ?, ?)";
    private static final String GUILD_SELECT_IDS
            = "SELECT id FROM '{prefix}guilds'";

    private static final String MEMBER_SELECT_ALL_WITH_HIGHEST_EXPERIENCE = """
            SELECT user_id, nickname, guild_avatar, biography, experience, voice_activity, placement \
            FROM '{prefix}members' \
            WHERE guild_id=? \
            ORDER BY experience DESC \
            LIMIT ?;""";

    private static final String MEMBER_SELECT_BY_IDS = "SELECT " +
            "nickname, guild_avatar, biography, experience, voice_activity, placement FROM '{prefix}members' " +
            "WHERE user_id=? AND guild_id=? LIMIT 1";

    private static final String MEMBER_UPDATE_BY_IDS = "UPDATE '{prefix}members' " +
            "SET nickname=?, guild_avatar=?, biography=?, experience=?, voice_activity=?, placement=?" +
            " WHERE user_id=? AND guild_id=?";

    private static final String MEMBER_INSERT = "INSERT INTO '{prefix}members' " +
            "(user_id, guild_id, nickname, guild_avatar, biography, experience, voice_activity, placement) " +
            "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String MEMBER_SELECT_USER_ID_BY_GUILD_ID
            = "SELECT user_id FROM '{prefix}members' WHERE guild_id=?";

    private static final String MEMBER_SELECT_USER_IDS
            = "SELECT user_id FROM '{prefix}members'";

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
        return this.populateUser(new ApiUser(userId, this.plugin));
    }

    private <T extends ApiUser> T populateUser(T user) throws SQLException {
        return this.executeQuery(USER_SELECT_BY_ID, ps -> ps.setLong(1, user.getId()), rs -> {
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
        return this.executeQuery(GUILD_SELECT_BY_ID, ps -> ps.setLong(1, guildId), rs -> {
            ApiGuild guild = new ApiGuild(guildId, this.plugin);
            if (rs.next()) {
                populateGuild(guild, rs);
            }
            return guild;
        });
    }

    @Override
    public Set<ApiGuild> loadGuilds() throws Exception {
        return getAllAsSet(GUILD_SELECT_ALL, null, rs -> populateGuild(new ApiGuild(rs.getLong("id"), this.plugin), rs));
    }

    private ApiGuild populateGuild(ApiGuild guild, ResultSet rs) throws SQLException {
        guild.setName(rs.getString("name"));
        guild.setIcon(rs.getString("icon"));
        guild.setLeaderboardEnabled(rs.getBoolean("leaderboard"));
        guild.setLeaderboardChannelId(rs.getLong("leaderboard_channel"));
        return guild;
    }

    @Override
    public ApiMember loadMember(long guildId, long userId) throws Exception {
        return this.executeQuery(MEMBER_SELECT_BY_IDS, ps -> {
            ps.setLong(1, userId);
            ps.setLong(2, guildId);
        }, rs -> {
            ApiMember member = new ApiMember(userId, this.plugin);
            member.setGuildId(guildId);

            if (rs.next()) {
                populateMember(member, rs);
            }
            return member;
        });
    }

    @Override
    public List<ApiMember> loadMembersWithHighestExperience(long guildId, int limit) throws Exception {
        return getAllAsList(MEMBER_SELECT_ALL_WITH_HIGHEST_EXPERIENCE, ps -> {
            ps.setLong(1, guildId);
            ps.setInt(2, limit);
        }, rs -> loadMemberFromResult(guildId, rs));
    }

    private ApiMember loadMemberFromResult(long guildId, ResultSet rs) throws SQLException {
        ApiMember member = new ApiMember(rs.getLong("user_id"), this.plugin);
        member.setGuildId(guildId);

        populateMember(member, rs);
        return member;
    }

    private void populateMember(ApiMember member, ResultSet rs) throws SQLException {
        this.populateUser(member);

        member.setNickname(rs.getString("nickname"));
        member.setGuildAvatar(rs.getString("guild_avatar"));
        member.setBiography(rs.getString("biography"));
        member.setExperience(rs.getLong("experience"));
        member.setVoiceActivity(rs.getLong("voice_activity"));
        member.setPlacement(rs.getInt("placement"));
    }

    @Override
    public void saveUser(ApiUser user) throws Exception {
        saveStatement(USER_SELECT_BY_ID, USER_UPDATE_BY_ID, USER_INSERT,
                ps -> ps.setLong(1, user.getId()),
                ps -> {
                    // where
                    ps.setLong(5, user.getId());

                    // content
                    ps.setString(1, DataConstraints.desanitize(user.getUsername(), true));
                    ps.setString(2, DataConstraints.desanitize(user.getGlobalName().orElse(null)));
                    ps.setString(3, DataConstraints.desanitize(user.getAvatar()));
                    ps.setLong(4, user.getLastSeen().toEpochMilli());
                },
                ps -> {
                    // where
                    ps.setLong(1, user.getId());

                    // content
                    ps.setString(2, DataConstraints.desanitize(user.getUsername(), true));
                    ps.setString(3, DataConstraints.desanitize(user.getGlobalName().orElse(null)));
                    ps.setString(4, DataConstraints.desanitize(user.getAvatar()));
                    ps.setLong(5, user.getLastSeen().toEpochMilli());
                });

    }

    @Override
    public void saveGuild(ApiGuild guild) throws Exception {
        saveStatement(GUILD_SELECT_BY_ID, GUILD_UPDATE_BY_ID, GUILD_INSERT,
                ps -> ps.setLong(1, guild.getId()),
                ps -> {
                    // where
                    ps.setLong(5, guild.getId());

                    // content
                    ps.setString(1, DataConstraints.desanitize(guild.getName()));
                    ps.setString(2, DataConstraints.desanitize(guild.getIcon()));
                    ps.setBoolean(3, guild.isLeaderboardEnabled());
                    ps.setLong(4, guild.getLeaderboardChannelId());
                },
                ps -> {
                    // where
                    ps.setLong(1, guild.getId());

                    // content
                    ps.setString(2, DataConstraints.desanitize(guild.getName()));
                    ps.setString(3, DataConstraints.desanitize(guild.getIcon()));
                    ps.setBoolean(4, guild.isLeaderboardEnabled());
                    ps.setLong(5, guild.getLeaderboardChannelId());
                });
    }

    @Override
    public void saveMember(ApiMember member) throws Exception {
        saveUser(member);
        saveStatement(MEMBER_SELECT_BY_IDS, MEMBER_UPDATE_BY_IDS, MEMBER_INSERT,
                ps -> {
                    ps.setLong(1, member.getId());
                    ps.setLong(2, member.getGuildId());
                },
                ps -> {
                    // where
                    ps.setLong(7, member.getId());
                    ps.setLong(8, member.getGuildId());

                    // content
                    ps.setString(1, DataConstraints.desanitize(member.getNickname().orElse(null)));
                    ps.setString(2, DataConstraints.desanitize(member.getGuildAvatar()));
                    ps.setString(3, DataConstraints.desanitize(member.getBiography().orElse(null)));
                    ps.setLong(4, member.getExperience());
                    ps.setLong(5, member.getVoiceActivity());
                    ps.setLong(6, member.getPlacement());
                },
                ps -> {
                    // where
                    ps.setLong(1, member.getId());
                    ps.setLong(2, member.getGuildId());

                    // content
                    ps.setString(3, DataConstraints.desanitize(member.getNickname().orElse(null)));
                    ps.setString(4, DataConstraints.desanitize(member.getGuildAvatar()));
                    ps.setString(5, DataConstraints.desanitize(member.getBiography().orElse(null)));
                    ps.setLong(6, member.getExperience());
                    ps.setLong(7, member.getVoiceActivity());
                    ps.setLong(8, member.getPlacement());
                });
    }

    @Override
    public Set<Snowflake> getUniqueUsers() throws Exception {
        return getAllAsSet(USER_SELECT_IDS, null, rs -> Snowflake.of(rs.getLong("id")));
    }

    @Override
    public Set<Snowflake> getUniqueGuilds() throws Exception {
        return getAllAsSet(GUILD_SELECT_IDS, null, rs -> Snowflake.of(rs.getLong("id")));
    }

    @Override
    public Set<Snowflake> getUniqueMembers() throws Exception {
        return getAllAsSet(MEMBER_SELECT_USER_IDS, null, rs -> Snowflake.of(rs.getLong("user_id")));
    }

    @Override
    public Set<Snowflake> getUniqueMembers(long guildId) throws Exception {
        return getAllAsSet(MEMBER_SELECT_USER_ID_BY_GUILD_ID, ps -> ps.setLong(1, guildId), rs -> Snowflake.of(rs.getLong("user_id")));
    }

    @FunctionalInterface
    private interface ResultSetFunction<T> {
        T apply(ResultSet rs) throws SQLException;
    }

    @FunctionalInterface
    private interface StatementConsumer {
        void accept(PreparedStatement ps) throws SQLException;
    }

    private void executeStatement(Connection c, String query, @Nullable StatementConsumer consumer) throws SQLException {
        try (PreparedStatement statement = c.prepareStatement(this.statementProcessor.process(query))) {
            if (consumer != null) {
                consumer.accept(statement);
            }
            statement.execute();
        }
    }

    private void executeStatement(String query, @Nullable StatementConsumer consumer) throws SQLException {
        try (Connection c = this.connectionFactory.getConnection()) {
            executeStatement(c, query, consumer);
        }
    }

    private <T> T executeQuery(Connection c, String query, @Nullable StatementConsumer consumer, ResultSetFunction<T> function) throws SQLException {
        try (PreparedStatement statement = c.prepareStatement(this.statementProcessor.process(query))) {
            if (consumer != null) {
                consumer.accept(statement);
            }

            try (ResultSet result = statement.executeQuery()) {
                return function.apply(result);
            }
        }
    }

    private <T> T executeQuery(String query, @Nullable StatementConsumer consumer, ResultSetFunction<T> function) throws SQLException {
        try (Connection c = this.connectionFactory.getConnection()) {
            return executeQuery(c, query, consumer, function);
        }
    }

    @FunctionalInterface
    private interface CollectionFactory<T> {
        Collection<T> create();
    }

    private <T> Collection<T> getAll(String query, @Nullable StatementConsumer consumer, ResultSetFunction<T> function, CollectionFactory<T> factory) throws SQLException {
        return executeQuery(query, consumer, rs -> {
            Collection<T> collection = factory.create();
            while (rs.next()) {
                collection.add(function.apply(rs));
            }
            return collection;
        });
    }

    private <T> List<T> getAllAsList(String query, @Nullable StatementConsumer consumer, ResultSetFunction<T> function) throws SQLException {
        return (List<T>) getAll(query, consumer, function, ArrayList::new);
    }

    private <T> Set<T> getAllAsSet(String query, @Nullable StatementConsumer consumer, ResultSetFunction<T> function) throws SQLException {
        return (Set<T>) getAll(query, consumer, function, HashSet::new);
    }

    private void saveStatement(
            String existsQuery,
            String updateQuery,
            String insertQuery,
            @Nullable StatementConsumer existsAction,
            @Nullable StatementConsumer updateAction,
            @Nullable StatementConsumer insertAction
    ) throws SQLException {
        try (Connection c = this.connectionFactory.getConnection()) {
            if (executeQuery(c, existsQuery, existsAction, ResultSet::next)) {
                executeStatement(c, updateQuery, updateAction);
            } else {
                executeStatement(c, insertQuery, insertAction);
            }
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