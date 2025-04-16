package me.kubbidev.moonrise.common.listener;

import me.kubbidev.moonrise.common.GatewayClient;
import me.kubbidev.moonrise.common.model.ApiGuild;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateIconEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

public class GuildListener extends ListenerAdapter {

    private final GatewayClient client;

    public GuildListener(GatewayClient client) {
        this.client = client;
    }

    @Override
    public void onGuildUpdateName(@NotNull GuildUpdateNameEvent e) {
        this.handleGuildUpdate(e.getGuild(), e.getOldName(), e.getNewValue(), g -> g.setName(e.getNewName()),
            "An error occurred while updating guild name");
    }

    @Override
    public void onGuildUpdateIcon(@NotNull GuildUpdateIconEvent e) {
        this.handleGuildUpdate(e.getGuild(), e.getOldIconUrl(), e.getNewIconUrl(), g -> g.setIcon(e.getNewIconUrl()),
            "An error occurred while updating guild icon");
    }

    /**
     * A generic method to handle guild updates with minimal duplication.
     */
    private <T> void handleGuildUpdate(Guild guild, T oldValue, T newValue, Consumer<ApiGuild> action,
                                       String errorMessage) {
        if (Objects.equals(oldValue, newValue)) {
            return;
        }

        this.client.modifyGuild(guild, action).exceptionally(t -> {
            this.client.getPlugin().getLogger().warn(errorMessage, t);
            return null;
        });
    }
}
