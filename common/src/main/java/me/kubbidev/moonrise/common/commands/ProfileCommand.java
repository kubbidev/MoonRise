package me.kubbidev.moonrise.common.commands;

import me.kubbidev.moonrise.common.command.Interaction;
import me.kubbidev.moonrise.common.command.InteractionContext;
import me.kubbidev.moonrise.common.message.ComponentEmbed;
import me.kubbidev.moonrise.common.message.source.Source;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import me.kubbidev.moonrise.common.util.Emote;
import me.kubbidev.moonrise.common.util.ImmutableCollectors;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static me.kubbidev.moonrise.common.locale.Message.FULL_STOP;

public class ProfileCommand implements Interaction {

    @Override
    public void execute(MoonRisePlugin plugin, Source channel, InteractionContext context) {
        Objects.requireNonNull(context.getMember());

        Member author = context.getMember();
        Member target = context.get("user", OptionMapping::getAsMember);
        if (target == null) {
            target = author;
        }

        ComponentEmbed componentEmbed = new ComponentEmbed();
        componentEmbed.color(0x1663FF);
        componentEmbed.thumbnail(target.getEffectiveAvatarUrl());
        componentEmbed.author(Component.translatable("moonrise.command.profile.information",
            Component.text(target.getEffectiveName())
        ));
        componentEmbed.footer(Component.translatable("moonrise.command.misc.requested",
            Component.text(author.getEffectiveName())
        ), author.getEffectiveAvatarUrl());

        Map<Activity.ActivityType, List<Activity>> targetActivities = target.getActivities()
            .stream()
            .collect(Collectors.groupingBy(Activity::getType));

        String effective = target.getEffectiveName();
        String username = target.getUser().getName();

        if (!Objects.equals(effective, username)) {
            username += " (" + effective + ")";
        }

        OnlineStatus status = target.getOnlineStatus();
        String statusKey = "moonrise.command.profile.status." + status.name()
            .toLowerCase(Locale.ROOT)
            .replace('_', '-')
            .replace(' ', '-');

        Emote statusEmote = (status == OnlineStatus.UNKNOWN ? Emote.EMPTY : Emote.valueOf(status.name()));
        Component onlineStatus = Component.text(statusEmote.asString())
            .append(Component.translatable(statusKey));

        Component activities;
        if (targetActivities.isEmpty()) {
            activities = Component.translatable("moonrise.command.profile.activity.nothing")
                .append(FULL_STOP);
        } else {
            Map<Component, String> activitiesFormatted = targetActivities.entrySet().stream()
                .filter(a -> a.getKey() != Activity.ActivityType.CUSTOM_STATUS)
                .collect(ImmutableCollectors.toMap(
                    entry -> {
                        String activityKey = "moonrise.command.profile.activity."
                            + entry.getKey().name().toLowerCase(Locale.ROOT)
                            .replace("_", "-")
                            .replace(" ", "-");
                        return Component.translatable(activityKey);
                    },
                    entry -> entry.getValue().stream()
                        .map(activity -> {
                            String activityName = activity.getName();
                            String activityUrl = activity.getUrl();
                            if (activityUrl != null) {
                                activityName = "[" + activityName + "](" + activityUrl + ")";
                            }
                            return activityName;
                        })
                        .collect(Collectors.joining(", "))
                ));
            TextComponent.Builder builder = Component.text();
            activitiesFormatted.forEach((key, value) -> {
                builder.append(Component.text("- "));
                builder.append(Component.text()
                    .decorate(TextDecoration.BOLD)
                    .append(key)
                    .append(Component.text(":")));
                builder.append(Component.space());
                builder.append(Component.text(value));
                builder.append(Component.newline());
            });
            activities = builder.build();
        }

        CompletableFuture<?> profileFuture = target.getUser().retrieveProfile()
            .submit().thenAcceptAsync(profile -> {
                componentEmbed.color(profile.getAccentColorRaw());

                var userBanner = profile.getBannerUrl();
                if (userBanner != null) {
                    componentEmbed.image(userBanner);
                }
            });

        Component componentName = Component.text(username);
        plugin.getGatewayClient().getMember(target).thenComposeAsync(apiMember -> {

            Component lastSeen = status == OnlineStatus.OFFLINE || status == OnlineStatus.INVISIBLE
                ? Component.text("<t:" + apiMember.getLastSeen().getEpochSecond() + ":R>")
                : Component.translatable("moonrise.command.profile.last-seen.online");

            Component biography = Component.translatable("moonrise.command.profile.biography",
                Component.text("`/bio`")).append(FULL_STOP);

            var optBiography = apiMember.getBiography();
            if (optBiography.isPresent()) {
                biography = Component.text(optBiography.get());
            }

            // > You can add here some useful info about yourself using {} command
            //
            // **__Common information__**
            // > **Username:** {}
            // > **Status:** {}
            // > **Last seen:** {}
            //
            // **__Activities__**
            // {}
            componentEmbed.content(Component.text()
                .append(Component.text("> "))
                .append(biography)
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.translatable()
                    .key("moonrise.command.profile.commons")
                    .decorate(TextDecoration.BOLD, TextDecoration.UNDERLINED))
                .append(Component.newline())
                .append(Component.text("> "))
                .append(Component.translatable()
                    .key("moonrise.command.profile.username")
                    .decorate(TextDecoration.BOLD)
                    .append(Component.text(":")))
                .append(Component.space())
                .append(componentName)
                .append(Component.newline())
                .append(Component.text("> "))
                .append(Component.translatable()
                    .key("moonrise.command.profile.status")
                    .decorate(TextDecoration.BOLD)
                    .append(Component.text(":")))
                .append(Component.space())
                .append(onlineStatus)
                .append(Component.newline())
                .append(Component.text("> "))
                .append(Component.translatable()
                    .key("moonrise.command.profile.last-seen")
                    .decorate(TextDecoration.BOLD)
                    .append(Component.text(":")))
                .append(Component.space())
                .append(lastSeen)
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.translatable()
                    .key("moonrise.command.profile.activities")
                    .decorate(TextDecoration.BOLD, TextDecoration.UNDERLINED))
                .append(Component.newline())
                .append(activities)
                .build());

            return profileFuture;
        }).thenAcceptAsync(__ -> {
            context.setDeferred(false);
            context.sendMessage(componentEmbed);
        }).join();
    }

    @Override
    public @NotNull SlashCommandData getMetadata() {
        return Commands.slash("profile", "Display the supplied user profile.")
            .setGuildOnly(true)
            .addOption(OptionType.USER, "user", "The user to display the profile of.");
    }
}
