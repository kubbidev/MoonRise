package me.kubbidev.moonrise.common.leaderboard;

import me.kubbidev.moonrise.common.leaderboard.util.Experiences;
import me.kubbidev.moonrise.common.locale.Message;
import me.kubbidev.moonrise.common.message.ComponentEmbed;
import me.kubbidev.moonrise.common.util.Emote;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public final class Leaderboard {
    public static final int MAX_ENTRIES = 16;

    /**
     * Represents an individual entry in the {@link Leaderboard}.
     * <p>
     * This record stores information about a leaderboard entry, including:
     * <ul>
     *     <li>The name of the participant.</li>
     *     <li>The total experience points accumulated by the participant.</li>
     *     <li>The current placement of the participant in the leaderboard.</li>
     *     <li>The previous placement of the participant in the leaderboard.</li>
     * </ul>
     *
     * @param name          The name of the participant for this leaderboard entry.
     * @param exp           The total experience points associated with this participant.
     * @param placement     The current ranking or position of the participant in the leaderboard.
     * @param prevPlacement The previous ranking or position of the participant in the leaderboard.
     */
    public record Entry(String name, long exp, int placement, int prevPlacement) {

    }

    private final LeaderboardManager leaderboardManager;
    private final List<Entry> entries = new ArrayList<>(MAX_ENTRIES);

    public Leaderboard(LeaderboardManager leaderboardManager) {
        this.leaderboardManager = leaderboardManager;
    }

    /**
     * Registers a new entry in the {@link Leaderboard} if the maximum
     * number of entries has not been reached.
     *
     * @param entry The leaderboard entry to be added. This includes information such as the participant's name,
     *              experience points, current placement, and previous placement.
     */
    public void registerEntry(Entry entry) {
        if (this.entries.size() < MAX_ENTRIES) this.entries.add(entry);
    }

    public ComponentEmbed build() {
        ComponentEmbed embed = new ComponentEmbed()
                .author(Message.LEADERBOARD_FIELD_HEADER.build())
                .thumbnail("https://static.wikia.nocookie.net/gensin-impact/images/1/17/Achievement_Wonders_of_the_World.png")
                .color(0xFFE193);

        ZonedDateTime nextDate = this.leaderboardManager.getNextScheduleDate();
        ZonedDateTime pastDate = nextDate.minusWeeks(1);

        embed.title(Message.LEADERBOARD_FIELD_TITLE.build(nextDate, pastDate));
        embed.field(ComponentEmbed.BLANK_FIELD, createNameField(), true);
        embed.field(ComponentEmbed.BLANK_FIELD, createExpField(), true);
        embed.field(ComponentEmbed.BLANK_FIELD, createLevelField(), true);
        embed.field(ComponentEmbed.BLANK_FIELD, createNextUpdateField(), false);
        return embed;
    }

    private Component createNameField() {
        TextComponent.Builder builder = Component.text();
        this.entries.forEach(entry -> {
            Emote emoteIcon = Emote.EQUAL;
            int prevPlacement = entry.prevPlacement;

            int placement = entry.placement;
            if (placement > prevPlacement) {
                emoteIcon = Emote.RED_TRIANGLE;
            } else if (placement < prevPlacement) {
                emoteIcon = Emote.GREEN_TRIANGLE;
            }

            builder.append(Message.LEADERBOARD_FILED_NAME.build(emoteIcon, placement, truncateString(entry.name, 16)));
            builder.append(Component.newline());
        });
        return builder.build();
    }

    private Component createExpField() {
        TextComponent.Builder builder = Component.text();
        this.entries.forEach(entry -> {
            builder.append(Message.LEADERBOARD_FIELD_GENERIC.build("exp", formatExp(entry.exp)));
            builder.append(Component.newline());
        });
        return builder.build();
    }

    private Component createLevelField() {
        TextComponent.Builder builder = Component.text();
        this.entries.forEach(entry -> {
            String level = String.valueOf(Experiences.determineLevelFromExperience(entry.exp));
            builder.append(Message.LEADERBOARD_FIELD_GENERIC.build("level", level));
            builder.append(Component.newline());
        });
        return builder.build();
    }

    private Component createNextUpdateField() {
        Instant nextMonday = this.leaderboardManager.getNextScheduleDate().toInstant();
        return Message.LEADERBOARD_FIELD_UPDATE.build(nextMonday);
    }

    /**
     * Truncates a given {@link String} to a specified maximum length.
     * <p>
     * If the string exceeds the maximum length, it appends an ellipsis ("...")
     * to indicate truncation.
     *
     * @param message   The original string to be truncated.
     * @param maxLength The maximum allowed length of the resulting string, including the ellipsis, if applicable.
     * @return The truncated string, either unchanged if it does not exceed the specified length
     *         or modified with an ellipsis if necessary.
     */
    public static String truncateString(String message, int maxLength) {
        return message.length() <= maxLength ? message : message.substring(0, maxLength - 3) + "...";
    }

    /**
     * Formats an experience value (exp) into a more readable {@link String} representation.
     * <p>
     * If the absolute value of the experience is greater than or equal to 1000, it converts
     * the value into a shortened format with a "K" suffix (e.g., 1500 becomes "1.50K").
     *
     * <p>Otherwise, it returns the value as a string representation.</p>
     *
     * @param experience The experience value to format, expressed as a long integer.
     * @return A formatted string representing the experience value. If the value is
     *         greater than or equal to 1000 (absolute value), it is returned in "K" format;
     *         otherwise, the original value is returned as a string.
     */
    public static String formatExp(long experience) {
        if (Math.abs(experience) >= 1000) {
            double expDouble = Math.abs(experience) / 1000.0;
            return String.format("%.2fK", expDouble);
        } else {
            return String.valueOf(experience);
        }
    }
}
