package me.kubbidev.moonrise.common.message.source;

import me.kubbidev.moonrise.common.message.ComponentEmbed;
import me.kubbidev.moonrise.common.serializer.ComponentSerializer;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class InteractionSource implements Source {
    protected final CommandInteraction interaction;
    private boolean deferred = false;

    @Nullable
    protected InteractionHook cachedHook = null;

    public static Source wrap(CommandInteraction interaction) {
        return new InteractionSource(interaction);
    }

    protected InteractionSource(CommandInteraction interaction) {
        this.interaction = interaction;
    }

    public void setDeferred(boolean deferred) {
        this.deferred = deferred;
    }

    private void assertHookIsCached() {
        if (this.cachedHook == null) {
            this.cachedHook = this.interaction.deferReply(this.deferred).complete();
        }
    }

    public @NotNull InteractionHook getInteraction() {
        this.assertHookIsCached();

        assert this.cachedHook != null;
        return this.cachedHook;
    }

    @Override
    public CompletableFuture<Message> sendMessage(Component message) {
        String translated = ComponentSerializer.serialize(message,
                this.interaction.getUserLocale().toLocale()
        );

        return getInteraction().sendMessage(translated).submit();
    }

    @Override
    public CompletableFuture<Message> sendMessage(ComponentEmbed embed) {
        var message = new MessageCreateBuilder()
                .setEmbeds(embed.build(
                        this.interaction.getUserLocale().toLocale()
                ))
                .build();

        return getInteraction().sendMessage(message).submit();
    }
}
