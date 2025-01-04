package me.kubbidev.moonrise.common.gateway.message.source;

import me.kubbidev.moonrise.common.gateway.message.ComponentEmbed;
import net.dv8tion.jda.api.entities.Message;
import net.kyori.adventure.text.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Represents a functional interface used for sending messages represented as {@link Component}s.
 * <p>
 * This interface can be implemented for various messaging systems to provide a consistent way of
 * sending messages, where the {@link #sendMessage(Component)} method serves as the primary entry
 * point for dispatching messages to a recipient.
 */
public interface Source {

    /**
     * Sends a message represented by a {@link Component} to the intended recipient.
     *
     * @param message the message to be sent, encapsulated as a {@link Component}
     * @return a {@code CompletableFuture} that completes with the resulting {@code Message} object
     *         once the message has been successfully sent
     */
    CompletableFuture<Message> sendMessage(Component message);

    /**
     * Sends a message in the form of a {@link ComponentEmbed} to the intended recipient.
     *
     * @param embed the embed containing the structured message details such as title, content,
     *              fields, and other visual elements
     * @return a {@code CompletableFuture} that completes with the resulting {@code Message} object
     *         once the message has been successfully sent
     */
    CompletableFuture<Message> sendMessage(ComponentEmbed embed);
}
