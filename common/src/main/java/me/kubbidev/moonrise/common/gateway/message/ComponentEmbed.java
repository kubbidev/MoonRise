package me.kubbidev.moonrise.common.gateway.message;

import me.kubbidev.moonrise.common.gateway.serializer.ComponentSerializer;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ComponentEmbed {
    private final List<CField> fields = new ArrayList<>(25);
    private int color = 0x1FFFFFFF;

    private @Nullable Component title;
    private @Nullable String url;
    private @Nullable Component content;

    private @Nullable OffsetDateTime timestamp;

    private @Nullable String thumbnail;
    private @Nullable String image;

    private CAuthor author = new CAuthor(null, null, null);
    private CFooter footer = new CFooter(null, null);

    public @NotNull ComponentEmbed title(
            @Nullable Component title
    ) {
        this.title = title;
        return this;
    }

    public @NotNull ComponentEmbed url(
            @Nullable String url
    ) {
        this.url = url;
        return this;
    }

    public @NotNull ComponentEmbed content(
            @Nullable Component content
    ) {
        this.content = content;
        return this;
    }

    public @NotNull ComponentEmbed timestamp(
            @Nullable OffsetDateTime timestamp
    ) {
        this.timestamp = timestamp;
        return this;
    }

    public @NotNull ComponentEmbed color(
            @NotNull TextColor color
    ) {
        this.color(Objects.requireNonNull(color, "color").value());
        return this;
    }

    public @NotNull ComponentEmbed color(int color) {
        this.color = color;
        return this;
    }

    public @NotNull ComponentEmbed thumbnail(
            @Nullable String thumbnail
    ) {
        this.thumbnail = thumbnail;
        return this;
    }

    public @NotNull ComponentEmbed image(
            @Nullable String image
    ) {
        this.image = image;
        return this;
    }

    public @NotNull ComponentEmbed author(
            @Nullable Component name
    ) {
        this.author(name, null);
        return this;
    }

    public @NotNull ComponentEmbed author(
            @Nullable Component name,
            @Nullable String url
    ) {
        this.author(name, url, null);
        return this;
    }

    public @NotNull ComponentEmbed author(
            @Nullable Component name,
            @Nullable String url,
            @Nullable String iconUrl
    ) {
        this.author = new CAuthor(name, url, iconUrl);
        return this;
    }

    public @NotNull ComponentEmbed footer(
            @Nullable Component name
    ) {
        this.footer(name, null);
        return this;
    }

    public @NotNull ComponentEmbed footer(
            @Nullable Component name,
            @Nullable String iconUrl
    ) {
        this.footer = new CFooter(name, iconUrl);
        return this;
    }

    public @NotNull ComponentEmbed blankField(boolean inline) {
        this.field(BLANK_FIELD, BLANK_FIELD, inline);
        return this;
    }

    public @NotNull ComponentEmbed field(
            @NotNull Component name,
            @NotNull Component value,
            boolean inline
    ) {
        this.fields.add(new CField(
                Objects.requireNonNull(name, "name"),
                Objects.requireNonNull(value, "value"), inline));
        return this;
    }

    public @NotNull ComponentEmbed clearFields() {
        this.fields.clear();
        return this;
    }

    public @NotNull MessageEmbed build(@Nullable Locale locale) {
        Function<Component, String> toString = c -> c == null ? null : ComponentSerializer.serialize(c, locale);
        return EntityBuilder.createMessageEmbed(this.url,
                toString.apply(this.title),
                toString.apply(this.content),
                EmbedType.RICH,
                this.timestamp,
                this.color,
                new MessageEmbed.Thumbnail(
                        this.thumbnail,
                        null, 0, 0
                ), null,
                new MessageEmbed.AuthorInfo(
                        toString.apply(this.author.name),
                        this.author.url,
                        this.author.iconUrl,
                        null
                ), null,
                new MessageEmbed.Footer(
                        toString.apply(this.footer.text),
                        this.footer.iconUrl,
                        null
                ),
                new MessageEmbed.ImageInfo(
                        this.image,
                        null, 0, 0
                ),
                new LinkedList<>(this.fields.stream().map(e -> new MessageEmbed.Field(
                        toString.apply(e.name),
                        toString.apply(e.value), e.inline)).collect(Collectors.toList())
                ));
    }

    public record CFooter(Component text, String iconUrl) {

    }

    public record CAuthor(Component name, String url, String iconUrl) {

    }

    public record CField(Component name, Component value, boolean inline) {

    }

    public static final Component BLANK_FIELD = Component.text('\u200E');
}
