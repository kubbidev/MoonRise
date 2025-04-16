package me.kubbidev.moonrise.common.listener;

import me.kubbidev.moonrise.common.GatewayClient;
import me.kubbidev.moonrise.common.model.ApiUser;
import me.kubbidev.moonrise.common.util.ExpiringSet;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.user.update.UserUpdateAvatarEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateGlobalNameEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class UserListener extends ListenerAdapter {

    private final GatewayClient client;

    /**
     * A set of user IDs that should temporarily ignore status updates for a configurable duration.
     */
    private final Set<Long> statusUpdateIgnore = ExpiringSet.newExpiringSet(30, TimeUnit.SECONDS);

    public UserListener(GatewayClient client) {
        this.client = client;
    }

    @Override
    public void onUserUpdateName(@NotNull UserUpdateNameEvent e) {
        this.handleUserUpdate(e.getUser(), e.getOldName(), e.getNewName(), u -> u.setUsername(e.getNewName()),
            "An error occurred while updating user username");
    }

    @Override
    public void onUserUpdateGlobalName(@NotNull UserUpdateGlobalNameEvent e) {
        this.handleUserUpdate(e.getUser(), e.getOldGlobalName(), e.getNewGlobalName(),
            u -> u.setGlobalName(e.getNewGlobalName()),
            "An error occurred while updating user global name");
    }

    @Override
    public void onUserUpdateAvatar(@NotNull UserUpdateAvatarEvent e) {
        this.handleUserUpdate(e.getUser(), e.getOldAvatarUrl(), e.getNewAvatarUrl(),
            u -> u.setAvatar(e.getNewAvatarUrl()),
            "An error occurred while updating user avatar");
    }

    @Override
    public void onUserUpdateOnlineStatus(@NotNull UserUpdateOnlineStatusEvent e) {
        OnlineStatus newStatus = e.getNewOnlineStatus();
        OnlineStatus oldStatus = e.getOldOnlineStatus();
        if (newStatus == oldStatus) {
            return;
        }

        var user = e.getUser();
        if (user.isBot()) {
            return;
        }

        // Events are executed multiple times for each mutual guild!
        if (this.statusUpdateIgnore.contains(user.getIdLong())) {
            return;
        }

        if (isTransitionToOffline(newStatus, oldStatus)) {
            this.updateUserLastSeen(user);
            this.statusUpdateIgnore.add(user.getIdLong());
        }
    }

    /**
     * A generic method to handle user updates with minimal duplication.
     */
    private <T> void handleUserUpdate(User user, T oldValue, T newValue, Consumer<ApiUser> action,
                                      String errorMessage) {
        if (Objects.equals(oldValue, newValue)) {
            return;
        }
        if (user.isBot()) {
            return;
        }

        this.client.modifyUser(user, action).exceptionally(t -> {
            this.client.getPlugin().getLogger().warn(errorMessage, t);
            return null;
        });
    }

    private void updateUserLastSeen(User user) {
        this.client.modifyUser(user, u -> u.setLastSeen(Instant.now())).exceptionally(t -> {
            this.client.getPlugin().getLogger().warn("An error occurred while updating member last seen", t);
            return null;
        });
    }

    private boolean isStatusOffline(OnlineStatus status) {
        return status == OnlineStatus.OFFLINE || status == OnlineStatus.INVISIBLE;
    }

    private boolean isTransitionToOffline(OnlineStatus newStatus, OnlineStatus oldStatus) {
        return this.isStatusOffline(newStatus) && !this.isStatusOffline(oldStatus);
    }
}
