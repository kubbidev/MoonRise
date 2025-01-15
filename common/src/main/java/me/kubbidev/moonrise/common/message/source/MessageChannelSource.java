package me.kubbidev.moonrise.common.message.source;

import me.kubbidev.moonrise.common.message.ComponentEmbed;
import me.kubbidev.moonrise.common.serializer.ComponentSerializer;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class MessageChannelSource implements Source {
    private final MessageChannel channel;

    public static Source wrap(MessageChannel channel) {
        return new MessageChannelSource(channel);
    }

    protected MessageChannelSource(MessageChannel channel) {
        this.channel = channel;
    }

    @Override
    public CompletableFuture<Message> sendMessage(Component message) {
        return this.channel.sendMessage(ComponentSerializer.serialize(
                message, getLocale()
        )).submit();
    }

    @Override
    public CompletableFuture<Message> sendMessage(ComponentEmbed embed) {
        return this.channel.sendMessageEmbeds(embed.build(getLocale()
        )).submit();
    }

    private @Nullable Locale getLocale() {
        return this.channel instanceof GuildMessageChannel
                ? ((GuildMessageChannel) this.channel).getGuild().getLocale().toLocale() : null;
    }
}
