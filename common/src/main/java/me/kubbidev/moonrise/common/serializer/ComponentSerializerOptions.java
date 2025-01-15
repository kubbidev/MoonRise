package me.kubbidev.moonrise.common.serializer;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import org.jetbrains.annotations.NotNull;

/**
 * Options for {@link ComponentSerializer}s.
 */
public record ComponentSerializerOptions(boolean maskedLinks, boolean escapeMarkdown,
        @NotNull ComponentFlattener flattener
) {

    /**
     * Provides a default set of {@link ComponentSerializerOptions}.
     *
     * @return the default {@link ComponentSerializerOptions} instance
     */
    public static ComponentSerializerOptions defaults() {
        return new ComponentSerializerOptions(false, true, ComponentFlattener.builder()
                .mapper(TextComponent.class, TextComponent::content)
                .build());
    }

    public ComponentSerializerOptions withMaskedLinks(boolean maskedLinks) {
        return new ComponentSerializerOptions(maskedLinks, this.escapeMarkdown, this.flattener);
    }

    public ComponentSerializerOptions withEscapeMarkdown(boolean escapeMarkdown) {
        return new ComponentSerializerOptions(this.maskedLinks, escapeMarkdown, this.flattener);
    }

    public ComponentSerializerOptions withFlattener(ComponentFlattener flattener) {
        return new ComponentSerializerOptions(this.maskedLinks, this.escapeMarkdown, flattener);
    }
}
