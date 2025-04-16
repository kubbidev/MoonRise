package me.kubbidev.moonrise.common.retriever;

import me.kubbidev.moonrise.common.model.ApiGuild;
import me.kubbidev.moonrise.common.model.ApiMember;
import me.kubbidev.moonrise.common.model.ApiUser;
import me.kubbidev.moonrise.common.storage.Storage;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public abstract class AbstractEntityRetriever implements EntityRetriever {

    private final Storage storage;

    public AbstractEntityRetriever(Storage storage) {
        this.storage = storage;
    }

    public abstract Executor actionExecutor();

    @Override
    public CompletableFuture<ApiUser> getUser(User user) {
        return this.storage.loadUser(user.getIdLong())
            .thenCompose(data -> {
                if (updateUser(data, user)) {
                    return this.storage.saveUser(data)
                        .thenApply(v -> data);
                } else {
                    return CompletableFuture.completedFuture(data);
                }
            });
    }

    @Override
    public CompletableFuture<Void> modifyUser(User user, Consumer<ApiUser> action) {
        return this.storage.loadUser(user.getIdLong())
            .thenApplyAsync(data -> {
                updateUser(data, user);
                return data;
            })
            .thenApplyAsync(data -> {
                action.accept(data);
                return data;
            }, this.actionExecutor())
            .thenCompose(this.storage::saveUser);
    }

    @Override
    public CompletableFuture<ApiGuild> getGuild(Guild guild) {
        return this.storage.loadGuild(guild.getIdLong())
            .thenCompose(data -> {
                if (updateGuild(data, guild)) {
                    return this.storage.saveGuild(data)
                        .thenApply(v -> data);
                } else {
                    return CompletableFuture.completedFuture(data);
                }
            });
    }

    @Override
    public CompletableFuture<Void> modifyGuild(Guild guild, Consumer<ApiGuild> action) {
        return this.storage.loadGuild(guild.getIdLong())
            .thenApplyAsync(data -> {
                updateGuild(data, guild);
                return data;
            })
            .thenApplyAsync(data -> {
                action.accept(data);
                return data;
            }, this.actionExecutor())
            .thenCompose(this.storage::saveGuild);
    }

    @Override
    public CompletableFuture<Set<ApiGuild>> getGuilds() {
        return this.storage.loadGuilds();
    }

    @Override
    public CompletableFuture<ApiMember> getMember(Member member) {
        long guildId = member.getGuild().getIdLong();
        return this.storage.loadMember(guildId, member.getIdLong())
            .thenCompose(data -> {
                if (updateMember(data, member)) {
                    return this.storage.saveMember(data)
                        .thenApply(v -> data);
                } else {
                    return CompletableFuture.completedFuture(data);
                }
            });
    }

    @Override
    public CompletableFuture<Void> modifyMember(Member member, Consumer<ApiMember> action) {
        long guildId = member.getGuild().getIdLong();
        return this.storage.loadMember(guildId, member.getIdLong())
            .thenApplyAsync(data -> {
                updateMember(data, member);
                return data;
            })
            .thenApplyAsync(data -> {
                action.accept(data);
                return data;
            }, this.actionExecutor())
            .thenCompose(this.storage::saveMember);
    }

    @Override
    public CompletableFuture<Void> saveMember(ApiMember member) {
        return this.storage.saveMember(member);
    }

    @Override
    public CompletableFuture<List<ApiMember>> getMembersWithHighestExperience(Guild guild, int limit) {
        return this.storage.loadMembersWithHighestExperience(guild.getIdLong(), limit);
    }

    private static boolean updateUser(ApiUser internal, User user) {
        boolean shouldSave = false;

        var username = user.getName();
        if (!Objects.equals(internal.getUsername(), username)) {
            internal.setUsername(username);
            shouldSave = true;
        }

        var globalName = user.getGlobalName();
        if (!Objects.equals(internal.getGlobalName().orElse(null), globalName)) {
            internal.setGlobalName(globalName);
            shouldSave = true;
        }

        var avatar = user.getAvatarUrl();
        if (!Objects.equals(internal.getAvatar(), avatar)) {
            internal.setAvatar(avatar);
            shouldSave = true;
        }

        return shouldSave;
    }

    private static boolean updateGuild(ApiGuild internal, Guild guild) {
        boolean shouldSave = false;

        var name = guild.getName();
        if (!Objects.equals(internal.getName(), name)) {
            internal.setName(name);
            shouldSave = true;
        }

        var icon = guild.getIconUrl();
        if (!Objects.equals(internal.getIcon(), icon)) {
            internal.setIcon(icon);
            shouldSave = true;
        }

        return shouldSave;
    }

    private static boolean updateMember(ApiMember internal, Member member) {
        boolean shouldSave = updateUser(internal, member.getUser());

        var nickname = member.getNickname();
        if (!Objects.equals(internal.getNickname().orElse(null), nickname)) {
            internal.setNickname(nickname);
            shouldSave = true;
        }

        var guildAvatar = member.getAvatarUrl();
        if (!Objects.equals(internal.getGuildAvatar(), guildAvatar)) {
            internal.setGuildAvatar(guildAvatar);
            shouldSave = true;
        }

        return shouldSave;
    }
}
