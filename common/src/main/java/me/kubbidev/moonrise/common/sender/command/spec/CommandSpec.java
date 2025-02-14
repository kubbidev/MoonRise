package me.kubbidev.moonrise.common.sender.command.spec;

import me.kubbidev.moonrise.common.util.ImmutableCollectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * An enumeration of the command defintion/usage messages used in the plugin.
 */
@SuppressWarnings("SpellCheckingInspection")
public enum CommandSpec {

    INFO("/%s info"),
    RELOAD_CONFIG("/%s reloadconfig"),
    TRANSLATIONS("/%s translations",
            arg("install", false)
    );

    private final @Nullable String usage;
    private final @Nullable List<Argument> args;

    CommandSpec(@Nullable String usage, @NotNull PartialArgument... args) {
        this.usage = usage;
        this.args = args.length == 0 ? null : Arrays.stream(args)
                .map(builder -> {
                    String key = builder.id.replace(".", "").replace(' ', '-');
                    TranslatableComponent description = Component.translatable(
                            "moonrise.usage." + key() + ".argument." + key);

                    return new Argument(builder.name, builder.required, description);
                })
                .collect(ImmutableCollectors.toList());
    }

    CommandSpec(PartialArgument... args) {
        this(null, args);
    }

    public TranslatableComponent description() {
        return Component.translatable("moonrise.usage." + this.key() + ".description");
    }

    /**
     * Gets the usage information for the command.
     *
     * @return the usage string or null if not applicable
     */
    public @Nullable String usage() {
        return this.usage;
    }

    /**
     * Gets the list of command arguments.
     *
     * @return the list of arguments, or null if no arguments exist
     */
    public @Nullable List<Argument> args() {
        return this.args;
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    private static PartialArgument arg(String name, boolean required) {
        return new PartialArgument(name, name, required);
    }

    private static PartialArgument arg(String id, String name, boolean required) {
        return new PartialArgument(id, name, required);
    }

    private record PartialArgument(String id, String name, boolean required) {
    }
}