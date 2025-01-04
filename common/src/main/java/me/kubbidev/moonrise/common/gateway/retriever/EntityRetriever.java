package me.kubbidev.moonrise.common.gateway.retriever;

import me.kubbidev.moonrise.common.model.ApiGuild;
import me.kubbidev.moonrise.common.model.ApiMember;
import me.kubbidev.moonrise.common.model.ApiUser;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * EntityRetriever provides an interface for retrieving various entities asynchronously.
 */
public interface EntityRetriever {

    /**
     * Retrieves a corresponding {@link ApiUser} asynchronously for the given {@link User}.
     *
     * @param user the user object representing the user to retrieve
     * @return a {@link CompletableFuture} that will complete with the {@link ApiUser} associated with the given user,
     *         or complete exceptionally if the user is not found or an error occurs
     */
    CompletableFuture<ApiUser> getUser(User user);

    CompletableFuture<ApiUser> modifyUser(User user, Consumer<ApiUser> action);

    /**
     * Retrieves a corresponding {@link ApiGuild} asynchronously for the given {@link Guild}.
     *
     * @param guild the {@link Guild} object representing the guild to retrieve
     * @return a {@link CompletableFuture} that completes with the {@link ApiGuild} associated with the given guild,
     *         or completes exceptionally if the guild is not found or an error occurs
     */
    CompletableFuture<ApiGuild> getGuild(Guild guild);

    CompletableFuture<ApiGuild> modifyGuild(Guild guild, Consumer<ApiGuild> action);

    /**
     * Retrieves a corresponding {@link ApiMember} asynchronously for the given {@link Member}.
     *
     * @param member the {@link Member} object representing the member to retrieve
     * @return a {@link CompletableFuture} that will complete with the {@link ApiMember} associated with the given member,
     *         or complete exceptionally if the member is not found or an error occurs
     */
    CompletableFuture<ApiMember> getMember(Member member);

    CompletableFuture<ApiMember> modifyMember(Member member, Consumer<ApiMember> action);
}