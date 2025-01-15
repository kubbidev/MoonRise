package me.kubbidev.moonrise.common.storage;

import me.kubbidev.moonrise.common.model.ApiGuild;
import me.kubbidev.moonrise.common.model.ApiMember;
import me.kubbidev.moonrise.common.model.ApiUser;
import me.kubbidev.moonrise.common.model.Snowflake;
import me.kubbidev.moonrise.common.storage.implementation.StorageImplementation;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import me.kubbidev.moonrise.common.util.AsyncInterface;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Provides a {@link CompletableFuture} based API for interacting with a {@link StorageImplementation}.
 */
public class Storage extends AsyncInterface {
    private final MoonRisePlugin plugin;
    private final StorageImplementation implementation;

    public Storage(MoonRisePlugin plugin, StorageImplementation implementation) {
        super(plugin);
        this.plugin = plugin;
        this.implementation = implementation;
    }

    public StorageImplementation getImplementation() {
        return this.implementation;
    }

    public String getName() {
        return this.implementation.getImplementationName();
    }

    public void init() {
        try {
            this.implementation.init();
        } catch (Exception e) {
            this.plugin.getLogger().severe("Failed to init storage implementation", e);
        }
    }

    public void shutdown() {
        try {
            this.implementation.shutdown();
        } catch (Exception e) {
            this.plugin.getLogger().severe("Failed to shutdown storage implementation", e);
        }
    }

    public StorageMetadata getMeta() {
        return this.implementation.getMeta();
    }

    public CompletableFuture<ApiUser> loadUser(long userId) {
        return future(() -> this.implementation.loadUser(userId));
    }

    public CompletableFuture<ApiGuild> loadGuild(long guildId) {
        return future(() -> this.implementation.loadGuild(guildId));
    }

    public CompletableFuture<Set<ApiGuild>> loadGuilds() {
        return future(this.implementation::loadGuilds);
    }

    public CompletableFuture<ApiMember> loadMember(long guildId, long userId) {
        return future(() -> this.implementation.loadMember(guildId, userId));
    }

    public CompletableFuture<List<ApiMember>> loadMembersWithHighestExperience(long guildId, int limit) {
        return future(() -> this.implementation.loadMembersWithHighestExperience(guildId, limit));
    }

    public CompletableFuture<Void> saveUser(ApiUser user) {
        return future(() -> this.implementation.saveUser(user));
    }

    public CompletableFuture<Void> saveGuild(ApiGuild guild) {
        return future(() -> this.implementation.saveGuild(guild));
    }

    public CompletableFuture<Void> saveMember(ApiMember member) {
        return future(() -> this.implementation.saveMember(member));
    }

    public CompletableFuture<Set<Snowflake>> getUniqueUsers() {
        return future(this.implementation::getUniqueUsers);
    }

    public CompletableFuture<Set<Snowflake>> getUniqueGuilds() {
        return future(this.implementation::getUniqueGuilds);
    }

    public CompletableFuture<Set<Snowflake>> getUniqueMembers() {
        return future(() -> this.implementation.getUniqueMembers());
    }

    public CompletableFuture<Set<Snowflake>> getUniqueMembers(long guildId) {
        return future(() -> this.implementation.getUniqueMembers(guildId));
    }
}
