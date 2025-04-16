package me.kubbidev.moonrise.common.sender.command.spec;

import java.util.Arrays;
import java.util.List;
import me.kubbidev.moonrise.common.util.ImmutableCollectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An enumeration of the command defintion/usage messages used in the plugin.
 */
@SuppressWarnings("SpellCheckingInspection")
public enum BuildinDefinition implements CommandDefinition {

    HELP(arg("commands", "commands...", false)),
    INFO,
    RELOAD_CONFIG,
    TRANSLATIONS(arg("install", false));

    private final @Nullable List<Argument> args;

    BuildinDefinition(@NotNull PartialArgument... args) {
        this.args = args.length == 0 ? null : Arrays.stream(args)
            .map(builder -> {
                String key = builder.id.replace(".", "").replace(' ', '-');
                TranslatableComponent description = Component.translatable(
                    "moonrise.usage." + key() + ".argument." + key);

                return new Argument(builder.name, builder.required, description);
            })
            .collect(ImmutableCollectors.toList());
    }

    @Override
    public @Nullable List<Argument> args() {
        return this.args;
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
