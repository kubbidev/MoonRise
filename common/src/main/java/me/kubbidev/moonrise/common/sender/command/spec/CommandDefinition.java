package me.kubbidev.moonrise.common.sender.command.spec;

import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CommandDefinition {

    /**
     * Gets the name of the command spec.
     *
     * @return the command specification name
     */
    @NotNull String name();

    default TranslatableComponent description() {
        return Component.translatable("moonrise.usage." + this.key() + ".description");
    }

    /**
     * Gets the list of command arguments.
     *
     * @return the list of arguments, or null if no arguments exist
     */
    @Nullable List<Argument> args();

    default String key() {
        return name().toLowerCase(Locale.ROOT).replaceAll("\\s+", "-").replace('_', '-');
    }
}
