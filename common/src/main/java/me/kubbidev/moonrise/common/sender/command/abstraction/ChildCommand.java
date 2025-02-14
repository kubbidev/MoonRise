package me.kubbidev.moonrise.common.sender.command.abstraction;

import me.kubbidev.moonrise.common.sender.command.access.CommandPermission;
import me.kubbidev.moonrise.common.sender.command.spec.Argument;
import me.kubbidev.moonrise.common.sender.command.spec.CommandSpec;
import me.kubbidev.moonrise.common.locale.Message;
import me.kubbidev.moonrise.common.sender.Sender;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Abstract SubCommand class
 */
public abstract class ChildCommand<T> extends Command<T> {

    public ChildCommand(CommandSpec spec, String name, @Nullable CommandPermission permission, Predicate<Integer> argumentCheck) {
        super(spec, name, permission, argumentCheck);
    }

    @Override
    public void sendUsage(Sender sender, String label) {
        TextComponent.Builder builder = Component.text()
                .append(Component.text('>', NamedTextColor.DARK_AQUA))
                .append(Component.space())
                .append(Component.text(getName().toLowerCase(Locale.ROOT), NamedTextColor.GREEN));

        if (getArgs().isPresent()) {
            List<Component> argUsages = getArgs().get().stream()
                    .map(Argument::asPrettyString)
                    .collect(Collectors.toList());

            builder.append(Component.text(" - ", NamedTextColor.DARK_AQUA))
                    .append(Component.join(JoinConfiguration.separator(Component.space()), argUsages));
        }

        sender.sendMessage(builder.build());
    }

    @Override
    public void sendDetailedUsage(Sender sender, String label) {
        Message.COMMAND_USAGE_DETAILED_HEADER.send(sender, getName(), getDescription());
        if (getArgs().isPresent()) {
            Message.COMMAND_USAGE_DETAILED_ARGS_HEADER.send(sender);
            for (Argument arg : getArgs().get()) {
                Message.COMMAND_USAGE_DETAILED_ARG.send(sender, arg.asPrettyString(), arg.description());
            }
        }
    }
}
