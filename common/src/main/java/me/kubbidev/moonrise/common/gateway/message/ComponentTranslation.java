package me.kubbidev.moonrise.common.gateway.message;

import com.google.common.collect.ImmutableMap;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.Translator;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class ComponentTranslation implements LocalizationFunction {
    public static final ComponentTranslation INSTANCE = new ComponentTranslation();

    private static final Class<?> REGISTRY;
    private static final Class<?> TRANSLATION;

    private static final Field FORMATS;
    private static final Field TRANSLATIONS;

    static {
        try {
            REGISTRY = Class.forName("net.kyori.adventure.translation.TranslationRegistryImpl");
            TRANSLATION = Class.forName("net.kyori.adventure.translation.TranslationRegistryImpl$Translation");

            FORMATS = TRANSLATION.getDeclaredField("formats");
            FORMATS.setAccessible(true);

            TRANSLATIONS = REGISTRY.getDeclaredField("translations");
            TRANSLATIONS.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private ComponentTranslation() {}

    @SuppressWarnings("unchecked")
    private static @NotNull Map<Locale, MessageFormat> getFormats(Object o, @NotNull String key) {
        try {
            var translation = ((Map<?, ?>) TRANSLATIONS.get(o)).get(key);
            if (translation == null) {
                return ImmutableMap.of();
            }

            return (Map<Locale, MessageFormat>) FORMATS.get(translation);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull Map<DiscordLocale, String> apply(@NotNull String key) {
        Map<DiscordLocale, String> translations = new HashMap<>();

        for (Translator source : GlobalTranslator.translator().sources()) {
            processSource(source, key, translations);
        }

        return translations;
    }

    private static void processSource(
            @NotNull Translator source,
            @NotNull String key,
            @NotNull Map<DiscordLocale, String> translations
    ) {
        if (REGISTRY.isInstance(source)) {
            translations.putAll(fetchTranslations(source, key));
        }
    }

    private static @NotNull Map<DiscordLocale, String> fetchTranslations(
            @NotNull Translator source,
            @NotNull String key
    ) {
        return translate(getFormats(source, key));
    }

    private static @NotNull Map<DiscordLocale, String> translate(@NotNull Map<Locale, MessageFormat> formats) {
        return formats.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        e -> DiscordLocale.from(e.getKey()),
                        e -> e.getValue().toString(),
                        (a, b) -> b
                ));
    }
}
