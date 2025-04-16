package me.kubbidev.moonrise.common.storage.implementation;

import me.kubbidev.moonrise.common.model.ApiGuild;
import me.kubbidev.moonrise.common.model.ApiMember;
import me.kubbidev.moonrise.common.model.ApiUser;
import me.kubbidev.moonrise.common.model.Snowflake;
import me.kubbidev.moonrise.common.storage.StorageMetadata;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;

import java.util.List;
import java.util.Set;

public interface StorageImplementation {

    MoonRisePlugin getPlugin();

    String getImplementationName();

    void init() throws Exception;

    void shutdown();

    StorageMetadata getMeta();

    ApiUser loadUser(long userId) throws Exception;

    ApiGuild loadGuild(long guildId) throws Exception;

    Set<ApiGuild> loadGuilds() throws Exception;

    ApiMember loadMember(long guildId, long userId) throws Exception;

    List<ApiMember> loadMembersWithHighestExperience(long guildId, int limit) throws Exception;

    void saveUser(ApiUser user) throws Exception;

    void saveGuild(ApiGuild guild) throws Exception;

    void saveMember(ApiMember member) throws Exception;

    Set<Snowflake> getUniqueUsers() throws Exception;

    Set<Snowflake> getUniqueGuilds() throws Exception;

    Set<Snowflake> getUniqueMembers() throws Exception;

    Set<Snowflake> getUniqueMembers(long guildId) throws Exception;
}
