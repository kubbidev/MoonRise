package me.kubbidev.moonrise.common.command.spec;

import me.kubbidev.moonrise.common.util.ImmutableCollectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

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

    private final String usage;
    private final List<Argument> args;

    CommandSpec(String usage, PartialArgument... args) {
        this.usage = usage;
        this.args = args.length == 0 ? null : Arrays.stream(args)
                .map(builder -> {
                    String key = builder.id.replace(".", "").replace(' ', '-');
                    TranslatableComponent description = Component.translatable("moonrise.usage." + key() + ".argument." + key);
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

    public String usage() {
        return this.usage;
    }

    public List<Argument> args() {
        return this.args;
    }

    public String key() {
        return this.name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    private static PartialArgument arg(String id, String name, boolean required) {
        return new PartialArgument(id, name, required);
    }

    private static PartialArgument arg(String name, boolean required) {
        return new PartialArgument(name, name, required);
    }

    private record PartialArgument(String id, String name, boolean required) {
    }
}