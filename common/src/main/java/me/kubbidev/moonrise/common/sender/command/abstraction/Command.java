package me.kubbidev.moonrise.common.sender.command.abstraction;

import java.util.Locale;
import java.util.stream.Collectors;
import me.kubbidev.moonrise.common.locale.Message;
import me.kubbidev.moonrise.common.sender.command.access.CommandPermission;
import me.kubbidev.moonrise.common.sender.command.spec.Argument;
import me.kubbidev.moonrise.common.sender.command.spec.CommandDefinition;
import me.kubbidev.moonrise.common.sender.command.util.ArgumentList;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import me.kubbidev.moonrise.common.sender.Sender;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * An abstract command class
 */
public abstract class Command {

    private final           String             name;
    private final           CommandDefinition  definition;
    private final @Nullable CommandPermission  permission;
    /**
     * A predicate used for testing the size of the arguments list passed to this command
     */
    private final           Predicate<Integer> argumentCheck;

    public Command(String name, CommandDefinition definition, @Nullable CommandPermission permission,
                   Predicate<Integer> argumentCheck) {
        this.name = name;
        this.definition = definition;
        this.permission = permission;
        this.argumentCheck = argumentCheck;
    }

    /**
     * Gets the short name of this command
     *
     * <p>The result should be appropriately capitalised.</p>
     *
     * @return the command name
     */
    public @NotNull String getName() {
        return this.name;
    }

    /**
     * Gets the commands definition.
     *
     * @return the command definition
     */
    public @NotNull CommandDefinition getDefinition() {
        return this.definition;
    }

    /**
     * Gets the permission required by this command, if present
     *
     * @return the command permission
     */
    public @NotNull Optional<CommandPermission> getPermission() {
        return Optional.ofNullable(this.permission);
    }

    /**
     * Gets the predicate used to validate the number of arguments provided to the command on execution
     *
     * @return the argument checking predicate
     */
    public @NotNull Predicate<Integer> getArgumentCheck() {
        return this.argumentCheck;
    }

    /**
     * Gets the commands description.
     *
     * @return the description
     */
    public @NotNull Component getDescription() {
        return this.getDefinition().description();
    }

    /**
     * Gets the arguments required by this command
     *
     * @return the commands arguments
     */
    public @NotNull Optional<List<Argument>> getArgs() {
        return Optional.ofNullable(this.getDefinition().args());
    }

    // Main execution method for the command.
    public abstract void execute(MoonRisePlugin plugin, Sender sender, ArgumentList args, String label)
        throws CommandException;

    // Tab completion method - default implementation is provided as some commands do not provide tab completions.
    public List<String> tabComplete(MoonRisePlugin plugin, Sender sender, ArgumentList args) {
        return Collections.emptyList();
    }

    /**
     * Sends a brief command usage message to the Sender. If this command has child commands, the children are listed.
     * Otherwise, a basic usage message is sent.
     *
     * @param sender the sender to send the usage to
     * @param label  the label used when executing the command
     */
    public void sendUsage(Sender sender, String label) {
        TextComponent.Builder builder = Component.text()
            .append(Component.text('>', NamedTextColor.DARK_AQUA))
            .append(Component.space())
            .append(Component.text(getName().toLowerCase(Locale.ROOT), NamedTextColor.GREEN))
            .clickEvent(ClickEvent.suggestCommand(String.format("/%s", label)));

        if (getArgs().isPresent()) {
            List<Component> argUsages = getArgs().get().stream()
                .map(Argument::asPrettyString)
                .collect(Collectors.toList());

            builder.append(Component.text(" - ", NamedTextColor.DARK_AQUA))
                .append(Component.join(JoinConfiguration.separator(Component.space()), argUsages));
        }

        sender.sendMessage(builder.build());
    }

    /**
     * Sends a detailed command usage message to the Sender. If this command has child commands, nothing is sent.
     * Otherwise, a detailed messaging containing a description and argument usage is sent.
     *
     * @param sender the sender to send the usage to
     * @param label  the label used when executing the command
     */
    public void sendDetailedUsage(Sender sender, String label) {
        Message.COMMAND_USAGE_DETAILED_HEADER.send(sender, getName(), getDescription());
        Message.COMMAND_USAGE_DETAILED_FOOTER.send(sender, label);

        if (getArgs().isPresent()) {
            Message.COMMAND_USAGE_DETAILED_ARGS_HEADER.send(sender);
            for (Argument arg : getArgs().get()) {
                Message.COMMAND_USAGE_DETAILED_ARG.send(sender, arg.asPrettyString(), arg.getDescription());
            }
        }
    }

    /**
     * Returns true if the sender is authorised to use this command
     * <p>
     * Commands with children are likely to override this method to check for permissions based upon whether a sender
     * has access to any sub commands.
     *
     * @param sender the sender
     * @return true if the sender has permission to use this command
     */
    public boolean hasPermission(Sender sender) {
        return this.permission == null || this.permission.isAuthorized(sender);
    }

    /**
     * Gets if this command should be displayed in command listings, or "hidden"
     *
     * @param sender the sender
     * @return if the command should be displayed
     */
    public boolean shouldDisplay(@SuppressWarnings("unused") Sender sender) {
        return true;
    }
}