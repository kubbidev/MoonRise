package me.kubbidev.moonrise.common.locale;

import me.kubbidev.moonrise.common.sender.Sender;
import me.kubbidev.moonrise.common.storage.StorageMetadata;
import me.kubbidev.moonrise.common.plugin.AbstractMoonRisePlugin;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import me.kubbidev.moonrise.common.plugin.bootstrap.MoonRiseBootstrap;
import me.kubbidev.moonrise.common.util.DurationFormatter;
import me.kubbidev.moonrise.common.util.Emote;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.JoinConfiguration.separator;
import static net.kyori.adventure.text.event.ClickEvent.suggestCommand;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.Style.style;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

/**
 * A collection of formatted messages used by the application.
 */
public interface Message {

    DateTimeFormatter DATE_TIME_FORMATTER
        = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    TextComponent OPEN_BRACKET  = text('(');
    TextComponent CLOSE_BRACKET = text(')');
    TextComponent FULL_STOP     = text('.');

    Component PREFIX_COMPONENT = text()
        .color(GRAY)
        .append(text('['))
        .append(text()
            .decoration(BOLD, true)
            .append(text('M', AQUA))
            .append(text('R', DARK_AQUA))
        )
        .append(text(']'))
        .build();

    static TextComponent prefixed(ComponentLike component) {
        return text()
            .append(PREFIX_COMPONENT)
            .append(space())
            .append(component)
            .build();
    }

    Args1<MoonRiseBootstrap> STARTUP_BANNER = bootstrap -> {
        Component infoLine1 = text()
            .append(text(AbstractMoonRisePlugin.getPluginName(), DARK_GREEN))
            .append(space())
            .append(text("v" + bootstrap.getVersion(), AQUA))
            .build();

        Component infoLine2 = text()
            .color(DARK_GRAY)
            .append(text("Running on "))
            .append(text(bootstrap.getType().getFriendlyName()))
            .append(text(" - "))
            .append(text(bootstrap.getServerBrand()))
            .build();

        // "        __    "
        // "  |\/| |__)   "
        // "  |  | |  \   "

        return joinNewline(
            text()
                .append(text("       ", AQUA))
                .append(text(" __    ", DARK_AQUA))
                .build(),
            text()
                .append(text("  |\\/| ", AQUA))
                .append(text("|__)   ", DARK_AQUA))
                .append(infoLine1)
                .build(),
            text()
                .append(text("  |  | ", AQUA))
                .append(text("|  \\   ", DARK_AQUA))
                .append(infoLine2)
                .build(),
            empty()
        );
    };

    Args1<MoonRiseBootstrap> PLUGIN_INFO = bootstrap -> prefixed(text()
        // "&2Running &b{} v{}&2."
        .color(DARK_GREEN)
        .append(text("Running "))
        .append(text(AbstractMoonRisePlugin.getPluginName(), AQUA))
        .append(space())
        .append(text("v" + bootstrap.getVersion(), AQUA))
        .append(FULL_STOP)
    );

    Args1<String> VIEW_AVAILABLE_COMMANDS_PROMPT = label -> prefixed(translatable()
        // "&3Use &a/{} help &3to view available commands."
        .key("moonrise.commandsystem.available-commands")
        .color(DARK_AQUA)
        .args(text('/' + label + " help", GREEN))
        .append(FULL_STOP)
    );

    Args0 NO_PERMISSION_FOR_SUBCOMMANDS = () -> prefixed(translatable()
        // "&3You do not have permission to use any sub commands."
        .key("moonrise.commandsystem.no-permission-subcommands")
        .color(DARK_AQUA)
        .append(FULL_STOP)
    );

    Args0 ALREADY_EXECUTING_COMMAND = () -> prefixed(translatable()
        // "&7Another command is being executed, waiting for it to finish..."
        .key("moonrise.commandsystem.already-executing-command")
        .color(GRAY)
    );

    Args0 COMMAND_NOT_RECOGNISED = () -> prefixed(translatable()
        // "&cCommand not recognised."
        .key("moonrise.commandsystem.command-not-recognised")
        .color(RED)
        .append(FULL_STOP)
    );

    Args0 COMMAND_NO_PERMISSION = () -> prefixed(translatable()
        // "&cYou do not have permission to use this command!"
        .key("moonrise.commandsystem.no-permission")
        .color(RED)
    );

    Args1<String> MAIN_COMMAND_USAGE_HEADER = usage -> prefixed(text()
        // "&bSub Commands: &7(/{} ...)"
        .color(AQUA)
        .append(translatable("moonrise.commandsystem.usage.sub-commands-header"))
        .append(text(": "))
        .append(text()
            .color(GRAY)
            .append(OPEN_BRACKET)
            .append(text('/'))
            .append(text(usage))
            .append(text(" ..."))
            .append(CLOSE_BRACKET)
        ));

    Args2<String, Component> COMMAND_USAGE_DETAILED_HEADER = (name, usage) -> joinNewline(
        // "&3&lCommand Usage &3- &b{}"
        // "&b> &7{}"
        prefixed(text()
            .append(translatable("moonrise.commandsystem.usage.usage-header", DARK_AQUA, BOLD))
            .append(text(" - ", DARK_AQUA))
            .append(text(name, AQUA))),
        prefixed(text()
            .append(text('>', AQUA))
            .append(space())
            .append(text().color(GRAY).append(usage)))
    );

    Args2<String, List<Component>> COMMAND_USAGE_DETAILED_FOOTER = (usage, arguments) -> prefixed(translatable()
        // "&3Usage: &7/{} {}"
        .key("moonrise.commandsystem.usage.usage-footer")
        .color(DARK_AQUA)
        .append(text(':'))
        .append(space())
        .append(text()
            .color(GRAY)
            .clickEvent(suggestCommand('/' + usage))
            .append(text('/'))
            .append(text(usage))
            .append(space())
            .append(join(separator(space()), arguments)))
    );

    Args0 COMMAND_USAGE_DETAILED_ARGS_HEADER = () -> prefixed(translatable()
        // "&3Arguments:"
        .key("moonrise.commandsystem.usage.arguments-header")
        .color(DARK_AQUA)
        .append(text(':'))
    );

    Args0 COMMAND_EXECUTION_EXCEPTION_HEADER = () -> text()
        // "&c:warning:Exception whilst executing command:"
        .color(RED)
        .append(text(Emote.WARNING.asString()))
        .append(space())
        .append(text()
            .decorate(BOLD)
            .append(translatable("moonrise.commandsystem.execution.exception-header"))
            .append(text(':')))
        .build();

    Args2<Component, Component> COMMAND_USAGE_DETAILED_ARG = (arg, usage) -> prefixed(text()
        // "&b- {}&3 -> &7{}"
        .append(text('-', AQUA))
        .append(space())
        .append(arg)
        .append(text(" -> ", DARK_AQUA))
        .append(text().color(GRAY).append(usage))
    );

    Args1<Integer> COMMAND_LIST_TOO_MUCH_REMAINING = remaining -> prefixed(translatable()
        // "&cToo many commands left. {} not displayed."
        .color(RED)
        .key("moonrise.commandsystem.too-much-remaining")
        .args(text(remaining))
        .append(FULL_STOP)
    );

    Args1<String> REQUIRED_ARGUMENT = name -> text()
        .color(DARK_GRAY)
        .append(text('<'))
        .append(text(name, GRAY))
        .append(text('>'))
        .build();

    Args1<String> OPTIONAL_ARGUMENT = name -> text()
        .color(DARK_GRAY)
        .append(text('['))
        .append(text(name, GRAY))
        .append(text(']'))
        .build();

    Args0 RELOAD_CONFIG_SUCCESS = () -> prefixed(translatable()
        // "&aThe configuration file was reloaded. &7(some options will only apply after the application has restarted)"
        .key("moonrise.command.reload-config.success")
        .color(GREEN)
        .append(FULL_STOP)
        .append(space())
        .append(text()
            .color(GRAY)
            .append(OPEN_BRACKET)
            .append(translatable("moonrise.command.reload-config.restart-note"))
            .append(CLOSE_BRACKET)
        )
    );

    Args2<MoonRisePlugin, StorageMetadata> INFO = (plugin, storageMeta) -> joinNewline(
        // "&2Running &bMoonRise v{}&2 by &bkubbidev&2."
        // "&f-  &3Platform: &f{}"
        // "&f-  &3Server Brand: &f{}"
        // "&f-  &3Server Version:"
        // "     &f{}"
        // "&f-  &bStorage:"
        // "     &3Type: &f{}"
        // "     &3Some meta value: {}"
        // "&f-  &3Extensions:"
        // "     &f{}"
        // "&f-  &bInstance:"
        // "     &3Uptime: &7{}"
        prefixed(translatable()
            .key("moonrise.command.info.running-plugin")
            .color(DARK_GREEN)
            .append(space())
            .append(text(AbstractMoonRisePlugin.getPluginName(), AQUA))
            .append(space())
            .append(text("v" + plugin.getBootstrap().getVersion(), AQUA))
            .append(text(" by "))
            .append(text("kubbidev", AQUA))
            .append(FULL_STOP)),
        prefixed(text()
            .color(DARK_AQUA)
            .append(text("-  ", WHITE))
            .append(translatable("moonrise.command.info.platform-key"))
            .append(text(": "))
            .append(text(plugin.getBootstrap().getType().getFriendlyName(), WHITE))),
        prefixed(text()
            .color(DARK_AQUA)
            .append(text("-  ", WHITE))
            .append(translatable("moonrise.command.info.server-brand-key"))
            .append(text(": "))
            .append(text(plugin.getBootstrap().getServerBrand(), WHITE))),
        prefixed(text()
            .color(DARK_AQUA)
            .append(text("-  ", WHITE))
            .append(translatable("moonrise.command.info.server-version-key"))
            .append(text(':'))),
        prefixed(text()
            .color(WHITE)
            .append(text("     "))
            .append(text(plugin.getBootstrap().getServerVersion()))),
        prefixed(text()
            .color(AQUA)
            .append(text("-  ", WHITE))
            .append(translatable("moonrise.command.info.storage-key"))
            .append(text(':'))),
        prefixed(text()
            .apply(builder -> {
                builder.append(text()
                    .color(DARK_AQUA)
                    .append(text("     "))
                    .append(translatable("moonrise.command.info.storage-type-key"))
                    .append(text(": "))
                    .append(text(plugin.getStorage().getName(), WHITE))
                );

                if (storageMeta.connected() != null) {
                    builder.append(newline());
                    builder.append(prefixed(text()
                        .color(DARK_AQUA)
                        .append(text("     "))
                        .append(translatable("moonrise.command.info.storage.meta.connected-key"))
                        .append(text(": "))
                        .append(formatBoolean(storageMeta.connected()))
                    ));
                }

                if (storageMeta.ping() != null) {
                    builder.append(newline());
                    builder.append(prefixed(text()
                        .color(DARK_AQUA)
                        .append(text("     "))
                        .append(translatable("moonrise.command.info.storage.meta.ping-key"))
                        .append(text(": "))
                        .append(text(storageMeta.ping() + "ms", GREEN))
                    ));
                }

                if (storageMeta.sizeBytes() != null) {
                    DecimalFormat format = new DecimalFormat("#.##");
                    String size = format.format(storageMeta.sizeBytes() / 1048576D) + "MB";

                    builder.append(newline());
                    builder.append(prefixed(text()
                        .color(DARK_AQUA)
                        .append(text("     "))
                        .append(translatable("moonrise.command.info.storage.meta.file-size-key"))
                        .append(text(": "))
                        .append(text(size, GREEN))
                    ));
                }
            })),
        prefixed(text()
            .color(AQUA)
            .append(text("-  ", WHITE))
            .append(translatable("moonrise.command.info.instance-key"))
            .append(text(':'))),
        prefixed(text()
            .color(DARK_AQUA)
            .append(text("     "))
            .append(translatable("moonrise.command.info.uptime-key"))
            .append(text(": "))
            .append(text().color(GRAY)
                .append(DurationFormatter.CONCISE_LOW_ACCURACY.format(plugin.getBootstrap().getStartupDuration()))))
    );

    Args0 TRANSLATIONS_SEARCHING = () -> prefixed(translatable()
        // "&7Searching for available translations, please wait..."
        .key("moonrise.command.translations.searching")
        .color(GRAY)
    );

    Args0 TRANSLATIONS_SEARCHING_ERROR = () -> prefixed(text()
        // "&cUnable to obtain a list of available translations. Check the console for errors."
        .color(RED)
        .append(translatable("moonrise.command.translations.searching-error"))
        .append(FULL_STOP)
        .append(space())
        .append(translatable("moonrise.command.misc.check-console-for-errors"))
        .append(FULL_STOP)
    );

    Args1<Collection<String>> INSTALLED_TRANSLATIONS = locales -> prefixed(translatable()
        // "&aInstalled Translations:"
        .key("moonrise.command.translations.installed-translations")
        .color(GREEN)
        .append(text(':'))
        .append(space())
        .append(formatStringList(locales))
    );

    Args0 AVAILABLE_TRANSLATIONS_HEADER = () -> prefixed(translatable()
        // "&aAvailable Translations:"
        .key("moonrise.command.translations.available-translations")
        .color(GREEN)
        .append(text(':'))
    );

    Args4<String, String, Integer, List<String>> AVAILABLE_TRANSLATIONS_ENTRY = (tag, name, percentComplete, contributors) -> prefixed(
        text()
            // - {} ({}) - {}% translated - by {}
            .color(GRAY)
            .append(text('-'))
            .append(space())
            .append(text(tag, AQUA))
            .append(space())
            .append(OPEN_BRACKET)
            .append(text(name, WHITE))
            .append(CLOSE_BRACKET)
            .append(text(" - "))
            .append(translatable("moonrise.command.translations.percent-translated", text(percentComplete, GREEN)))
            .apply(builder -> {
                if (!contributors.isEmpty()) {
                    builder.append(text(" - "));
                    builder.append(translatable("moonrise.command.translations.translations-by"));
                    builder.append(space());
                    builder.append(formatStringList(contributors));
                }
            })
    );

    Args1<String> TRANSLATIONS_DOWNLOAD_PROMPT = label -> joinNewline(
        // "Use /m translations install to download and install up-to-date versions of these translations provided by the community."
        // "Please note that this will override any changes you've made for these languages."
        prefixed(translatable()
            .key("moonrise.command.translations.download-prompt")
            .color(AQUA)
            .args(text("/" + label + " translations install", GREEN))
            .append(FULL_STOP)),
        prefixed(translatable()
            .key("moonrise.command.translations.download-override-warning")
            .color(GRAY)
            .append(FULL_STOP))
    );

    Args0 TRANSLATIONS_INSTALLING = () -> prefixed(translatable()
        // "&bInstalling translations, please wait..."
        .key("moonrise.command.translations.installing")
        .color(AQUA)
    );

    Args1<String> TRANSLATIONS_INSTALLING_SPECIFIC = name -> prefixed(translatable()
        // "&aInstalling language {}..."
        .key("moonrise.command.translations.installing-specific")
        .color(GREEN)
        .args(text(name))
    );

    Args0 TRANSLATIONS_INSTALL_COMPLETE = () -> prefixed(translatable()
        // "&bInstallation complete."
        .key("moonrise.command.translations.install-complete")
        .color(AQUA)
        .append(FULL_STOP)
    );

    Args1<String> TRANSLATIONS_DOWNLOAD_ERROR = name -> prefixed(text()
        // "&cUnable download translation for {}. Check the console for errors."
        .color(RED)
        .append(translatable("moonrise.command.translations.download-error", text(name, DARK_RED)))
        .append(FULL_STOP)
        .append(space())
        .append(translatable("moonrise.command.misc.check-console-for-errors"))
        .append(FULL_STOP)
    );

    Args0 LEADERBOARD_FIELD_HEADER = () -> translatable()
        // This week's leaderboard:
        .key("moonrise.leaderboard.field-header")
        .append(text(':'))
        .build();

    Args2<ZonedDateTime, ZonedDateTime> LEADERBOARD_FIELD_TITLE = (next, last) -> text()
        // `{}` - `{}`
        .append(text('`'))
        .append(text(last.format(DATE_TIME_FORMATTER))).append(text("` - `"))
        .append(text(next.format(DATE_TIME_FORMATTER)))
        .append(text('`'))
        .build();

    Args3<Emote, Integer, String> LEADERBOARD_FILED_NAME = (emote, placement, username) -> text()
        // {} `#{}` **{}**
        .append(text(emote.asString()))
        .append(text(" `#"))
        .append(text(placement))
        .append(text('`'))
        .append(space())
        .append(text(username, style(BOLD)))
        .build();

    Args2<String, String> LEADERBOARD_FIELD_GENERIC = (name, formattedValue) -> text()
        // {}exp: `{}`
        .append(text(Emote.EMPTY.asString()))
        .append(text(name))
        .append(text(": `"))
        .append(text(formattedValue))
        .append(text('`'))
        .build();

    Args1<Instant> LEADERBOARD_FIELD_UPDATE = nextUpdate -> text()
        // *Next update of the ranking* :: <t:{}:R>
        .append(translatable("moonrise.leaderboard.field-update", style(ITALIC)))
        .append(text(" :: <t:"))
        .append(text(nextUpdate.getEpochSecond()))
        .append(text(":R>"))
        .build();

    Args0 BIOGRAPHY_UPDATED = () -> translatable()
        // Your biography has been successfully updated.
        .key("moonrise.command.biography.updated")
        .append(FULL_STOP)
        .build();

    Args1<Channel> LEADERBOARD_CHANNEL_UPDATED = channel -> translatable()
        // From now on, {} will be the transmission channel for the classification.
        .key("moonrise.command.leaderboard.channel-updated")
        .args(text(channel.getAsMention()))
        .append(FULL_STOP)
        .build();

    Args0 LEADERBOARD_ENABLE = () -> translatable()
        // The ranking and experience system are now enabled on the server.
        .key("moonrise.command.leaderboard.enabled")
        .append(FULL_STOP)
        .build();

    Args0 LEADERBOARD_DISABLE = () -> translatable()
        // The ranking and experience system are now disabled on the server.
        .key("moonrise.command.leaderboard.disabled")
        .append(FULL_STOP)
        .build();

    Args0 LEADERBOARD_NOT_ACTIVE = () -> text()
        // :x: The leaderboard is not active in this guild.
        .content(Emote.RED_CROSS.asString())
        .append(space())
        .append(translatable("moonrise.command.leaderboard.not-active"))
        .append(FULL_STOP)
        .build();

    Args0 LEADERBOARD_REFRESHED = () -> text()
        // :information_source: The leaderboard have been successfully refreshed.
        .content(Emote.INFO.asString())
        .append(space())
        .append(translatable("moonrise.command.leaderboard.refreshed"))
        .append(FULL_STOP)
        .build();

    static Component formatStringList(Collection<String> strings) {
        Iterator<String> it = strings.iterator();
        if (!it.hasNext()) {
            return translatable("moonrise.command.misc.none", AQUA); // "&bNone"
        }

        TextComponent.Builder builder = text().color(DARK_AQUA).content(it.next());

        while (it.hasNext()) {
            builder.append(text(", ", GRAY));
            builder.append(text(it.next()));
        }

        return builder.build();
    }

    static Component formatBoolean(boolean bool) {
        return bool ? text("true", GREEN) : text("false", RED);
    }

    static Component joinNewline(final ComponentLike... components) {
        return join(JoinConfiguration.newlines(), components);
    }

    @FunctionalInterface
    interface Args0 {

        Component build();

        default void send(Sender sender) {
            sender.sendMessage(build());
        }
    }

    @FunctionalInterface
    interface Args1<A0> {

        Component build(A0 arg0);

        default void send(Sender sender, A0 arg0) {
            sender.sendMessage(build(arg0));
        }
    }

    @FunctionalInterface
    interface Args2<A0, A1> {

        Component build(A0 arg0, A1 arg1);

        default void send(Sender sender, A0 arg0, A1 arg1) {
            sender.sendMessage(build(arg0, arg1));
        }
    }

    @FunctionalInterface
    interface Args3<A0, A1, A2> {

        Component build(A0 arg0, A1 arg1, A2 arg2);

        default void send(Sender sender, A0 arg0, A1 arg1, A2 arg2) {
            sender.sendMessage(build(arg0, arg1, arg2));
        }
    }

    @FunctionalInterface
    interface Args4<A0, A1, A2, A3> {

        Component build(A0 arg0, A1 arg1, A2 arg2, A3 arg3);

        default void send(Sender sender, A0 arg0, A1 arg1, A2 arg2, A3 arg3) {
            sender.sendMessage(build(arg0, arg1, arg2, arg3));
        }
    }
}
