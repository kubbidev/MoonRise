package me.kubbidev.moonrise.common.gateway.serializer;

import me.kubbidev.moonrise.common.locale.TranslationManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.flattener.FlattenerListener;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.ComponentEncoder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

/**
 * ComponentSerializer is a utility class that encodes a {@link Component} into a Discord-formatted
 * markdown string.
 * <p>
 * It supports features such as bold, italic, underline, strikethrough, link parsing,
 * and additional custom serialization options.
 * <p>
 * This serializer provides a flexible way to flatten and serialize components with defined
 * formatting and escaping behaviors.
 * <p>
 * Serialization behavior can be controlled via {@link ComponentSerializerOptions}, which provides
 * the ability to customize the default options or specify options on a per-serialization basis.
 *
 * @see ComponentSerializerOptions
 */
public class ComponentSerializer implements ComponentEncoder<Component, String> {
    private static final Pattern LINK_PATTERN = Pattern.compile("(https?://.*\\.[^ ]*)$");

    /**
     * Default instance of the ComponentSerializer, in case that's all you need.
     *
     * <p>Using {@link ComponentSerializer#setDefaultOptions(ComponentSerializerOptions)} is not allowed.</p>
     */
    private static final ComponentSerializer INSTANCE = new ComponentSerializer();

    /**
     * Serializes a given {@link Component} into a string, optionally taking a {@link Locale}
     * into account for localization purposes.
     *
     * @param message the {@link Component} to be serialized; must not be null
     * @param locale  the {@link Locale} used for localization; may be null
     * @return the serialized string representation of the given {@link Component},
     *         never null
     */
    public static @NotNull String serialize(Component message, @Nullable Locale locale) {
        return ComponentSerializer.INSTANCE.serialize(TranslationManager.render(message, locale));
    }

    /**
     * The default {@link ComponentSerializerOptions} to use for this serializer.
     */
    @NotNull
    private ComponentSerializerOptions defaultOptions;

    /**
     * Constructor for creating a serializer, which {@link ComponentSerializerOptions#defaults()} as defaults.
     */
    public ComponentSerializer() {
        this(ComponentSerializerOptions.defaults());
    }

    /**
     * Constructor for creating a serializer, with the specified {@link ComponentSerializerOptions} as defaults.
     *
     * @param defaultOptions the default serializer options (can be overridden on serialize)
     * @see ComponentSerializerOptions#defaults()
     */
    public ComponentSerializer(@NotNull ComponentSerializerOptions defaultOptions) {
        this.defaultOptions = defaultOptions;
    }

    /**
     * Gets the default options for this serializer.
     *
     * @return the default options for this serializer
     */
    public @NotNull ComponentSerializerOptions getDefaultOptions() {
        return this.defaultOptions;
    }

    /**
     * Sets the default options for this serializer.
     *
     * @param defaultOptions the new default options
     */
    public void setDefaultOptions(@NotNull ComponentSerializerOptions defaultOptions) {
        this.defaultOptions = defaultOptions;
    }

    /**
     * Serializes a {@link Component} to Discord formatting (markdown) with this serializer's
     * {@link ComponentSerializer#getDefaultOptions()}.
     * <p>
     * Use {@link ComponentSerializer#serialize(Component, ComponentSerializerOptions)} to fine
     * tune the serialization options.
     *
     * @param component The text component from a Minecraft chat message
     * @return Discord markdown formatted String
     */
    @Override
    public @NotNull String serialize(@NotNull Component component) {
        ComponentSerializerOptions options = getDefaultOptions();
        return serialize(component, options);
    }

    public @NotNull String serialize(@NotNull Component component, @NotNull ComponentSerializerOptions options) {
        ComponentFlattener flattener = options.flattener();

        FlattenListener listener = new FlattenListener(options);
        flattener.flatten(component, listener);

        StringBuilder builder = new StringBuilder();
        for (Text text : listener.getTexts()) {
            var content = text.getContent().toString();
            if (content.isEmpty()) continue;

            if (text.isBold()) {
                builder.append("**");
            }
            if (text.isStrikethrough()) {
                builder.append("~~");
            }
            if (text.isItalic()) {
                builder.append("_");
            }
            if (text.isUnderline()) {
                builder.append("__");
            }

            if (options.escapeMarkdown() && !LINK_PATTERN.matcher(content).find()) {
                content = content
                        .replace("*", "\\*")
                        .replace("~", "\\~")
                        .replace("_", "\\_")
                        .replace("`", "\\`")
                        .replace("|", "\\|");
            }

            String openUrl = text.getOpenUrl();
            if (options.maskedLinks() && openUrl != null) {
                String display = text.getUrlHover();
                content = "[" + content + "](<" + openUrl + ">" + (display != null ? " \"" + display + "\"" : "") + ")";
            }

            builder.append(content);

            if (text.isUnderline()) {
                builder.append("__");
            }
            if (text.isItalic()) {
                builder.append("_");
            }
            if (text.isStrikethrough()) {
                builder.append("~~");
            }
            if (text.isBold()) {
                builder.append("**");
            }

            // Separator for formatting, since going from bold -> bold underline
            // would lead to "**bold****__bold underline__**" which doesn't work
            builder.append("\u200B");
        }
        int length = builder.length();
        return length < 1 ? "" : builder.substring(0, length - 1);
    }

    private static class FlattenListener implements FlattenerListener {
        private final ComponentSerializerOptions serializerOptions;
        private final boolean gatherLinks;

        private Text currentText = null;
        private final Map<Style, Text> previousText = new HashMap<>();
        private final List<Text> texts = new ArrayList<>();

        public FlattenListener(ComponentSerializerOptions serializerOptions) {
            this.serializerOptions = serializerOptions;
            this.gatherLinks = serializerOptions.maskedLinks();
        }

        public List<Text> getTexts() {
            if (this.currentText != null) {
                this.texts.add(this.currentText);
            }
            return this.texts;
        }

        @Override
        public void pushStyle(@NotNull Style style) {
            Text text;
            if (this.currentText != null) {
                text = this.currentText.clone();
                text.getContent().setLength(0);
            } else {
                text = new Text();
            }

            TextDecoration.State bold = style.decoration(TextDecoration.BOLD);
            text.setBold(bold == TextDecoration.State.TRUE);

            TextDecoration.State italic = style.decoration(TextDecoration.ITALIC);
            text.setItalic(italic == TextDecoration.State.TRUE);

            TextDecoration.State underline = style.decoration(TextDecoration.UNDERLINED);
            text.setUnderline(underline == TextDecoration.State.TRUE);

            TextDecoration.State strikethrough = style.decoration(TextDecoration.STRIKETHROUGH);
            text.setStrikethrough(strikethrough == TextDecoration.State.TRUE);

            ClickEvent clickEvent = style.clickEvent();
            if (this.gatherLinks && clickEvent != null && clickEvent.action() == ClickEvent.Action.OPEN_URL) {
                text.setOpenUrl(clickEvent.value());
            }

            HoverEvent<?> hoverEvent = style.hoverEvent();
            if (this.gatherLinks && hoverEvent != null && hoverEvent.action() == HoverEvent.Action.SHOW_TEXT) {
                FlattenToTextOnly flatten = new FlattenToTextOnly();
                this.serializerOptions.flattener().flatten((Component) hoverEvent.value(), flatten);
                text.setUrlHover(flatten.getContent());
            }

            if (this.currentText == null) {
                this.currentText = text;
            } else if (!text.formattingMatches(this.currentText)) {
                this.texts.add(this.currentText);
                this.previousText.put(style, this.currentText.clone());
                this.currentText = text;
            }
        }

        @Override
        public void popStyle(@NotNull Style style) {
            var popped = this.previousText.remove(style);
            if (popped != null) {
                this.texts.add(this.currentText);
                this.currentText = popped;
                this.currentText.getContent().setLength(0);
            }
        }

        @Override
        public void component(@NotNull String text) {
            if (this.currentText == null) {
                this.currentText = new Text();
            }
            this.currentText.appendContent(text);
        }
    }

    private static class FlattenToTextOnly implements FlattenerListener {
        private final StringBuilder builder = new StringBuilder();

        @Override
        public void component(@NotNull String text) {
            this.builder.append(text);
        }

        public @NotNull String getContent() {
            return this.builder.toString();
        }
    }

    private static class Text implements Cloneable {
        private final StringBuilder content = new StringBuilder();
        private boolean bold;
        private boolean strikethrough;
        private boolean underline;
        private boolean italic;

        private String openUrl;
        private String urlHover;

        private Text() {
        }

        public Text(
                StringBuilder content,
                boolean bold,
                boolean strikethrough,
                boolean underline,
                boolean italic,
                String openUrl,
                String urlHover
        ) {
            this.content.append(content);
            this.bold = bold;
            this.strikethrough = strikethrough;
            this.underline = underline;
            this.italic = italic;
            this.openUrl = openUrl;
            this.urlHover = urlHover;
        }

        public StringBuilder getContent() {
            return this.content;
        }

        public void appendContent(String content) {
            this.content.append(content);
        }

        public boolean isBold() {
            return this.bold;
        }

        public void setBold(boolean bold) {
            this.bold = bold;
        }

        public boolean isStrikethrough() {
            return this.strikethrough;
        }

        public void setStrikethrough(boolean strikethrough) {
            this.strikethrough = strikethrough;
        }

        public boolean isUnderline() {
            return this.underline;
        }

        public void setUnderline(boolean underline) {
            this.underline = underline;
        }

        public boolean isItalic() {
            return this.italic;
        }

        public void setItalic(boolean italic) {
            this.italic = italic;
        }

        public String getOpenUrl() {
            return this.openUrl;
        }

        public void setOpenUrl(String openUrl) {
            this.openUrl = openUrl;
        }

        public String getUrlHover() {
            return this.urlHover;
        }

        public void setUrlHover(String urlHover) {
            this.urlHover = urlHover;
        }

        /**
         * Checks if the formatting matches between this and another {@link Text} object.
         *
         * @param other The other Text object.
         * @return true if the formatting matches the other Text object.
         */
        public boolean formattingMatches(@Nullable Text other) {
            return other != null
                    && this.bold == other.bold
                    && this.strikethrough == other.strikethrough
                    && this.underline == other.underline
                    && this.italic == other.italic
                    && Objects.equals(this.openUrl, other.openUrl)
                    && Objects.equals(this.urlHover, other.urlHover);
        }

        @SuppressWarnings("MethodDoesntCallSuperMethod")
        @Override
        public Text clone() {
            return new Text(
                    this.content,
                    this.bold,
                    this.strikethrough,
                    this.underline,
                    this.italic,
                    this.openUrl,
                    this.urlHover
            );
        }

        @Override
        public final boolean equals(Object o) {
            if (!(o instanceof Text text)) return false;

            return this.bold == text.bold
                    && this.strikethrough == text.strikethrough
                    && this.underline == text.underline
                    && this.italic == text.italic
                    && this.content.toString().contentEquals(text.content)
                    && Objects.equals(this.openUrl, text.openUrl)
                    && Objects.equals(this.urlHover, text.urlHover);
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    this.content,
                    this.bold,
                    this.strikethrough,
                    this.underline,
                    this.italic,
                    this.openUrl,
                    this.urlHover
            );
        }
    }
}
