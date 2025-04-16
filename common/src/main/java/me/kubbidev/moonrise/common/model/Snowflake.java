package me.kubbidev.moonrise.common.model;

import org.jetbrains.annotations.NotNull;

/**
 * An <i>unsigned</i> 64-bit ID that is guaranteed to be unique across all of Discord, except in some unique scenarios
 * in which child objects share their parent's ID.
 *
 * @see <a href="https://discord.com/developers/docs/reference#snowflake-ids">Snowflake IDs</a>
 */
public class Snowflake implements Comparable<Snowflake> {

    /**
     * The <i>raw</i> mention for notifying all users in a channel.
     */
    public static final String EVERYONE = "@everyone";
    protected final     long   id;

    public static Snowflake of(long id) {
        return new Snowflake(id);
    }

    protected Snowflake(long id) {
        this.id = id;
    }

    /**
     * @return the <i>unsigned</i> ID of this {@link Snowflake} as a primitive long
     */
    public final long getId() {
        return this.id;
    }

    /**
     * @return The <i>unsigned</i> ID of this {@link Snowflake} as an object String
     */
    public @NotNull String asString() {
        return Long.toUnsignedString(getId());
    }

    /**
     * Gets a <i>raw</i> mention for a {@link Snowflake}.
     *
     * <p>The use of this mention requires being permitted by the "allowed mentions"
     * of any messages being sent.</p>
     *
     * <p>This is the format utilized to directly mention another snowflake (assuming
     * the snowflake exists in context of the mention).</p>
     *
     * @return The <i>raw</i> mention.
     */
    public @NotNull String asMention() {
        return "<@" + this.asString() + ">";
    }

    /**
     * Compares this snowflake to the specified snowflake.
     * <p>
     * The comparison is based on the timestamp portion of the snowflakes.
     *
     * @param o The other snowflake to compare to.
     * @return The comparator value.
     */
    @Override
    public int compareTo(@NotNull Snowflake o) {
        return Long.signum((this.id >>> 22) - (o.id >>> 22));
    }

    /**
     * Gets a string representation of this {@link Snowflake}.
     *
     * <p>The string representation typically includes its id in a user-friendly format.</p>
     *
     * @return a string of this {@link Snowflake}.
     */
    @Override
    public String toString() {
        return this.asString();
    }

    /**
     * Compares this object to the specified object to determine equality.
     *
     * <p>This method evaluates whether the provided object is equal to the current instance,
     * considering any relevant instance-specific properties.</p>
     *
     * @param o the object to compare with this instance for equality
     * @return {@code true} if the specified object is equal to this instance; {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Snowflake that)) {
            return false;
        }

        return this.id == that.id;
    }

    /**
     * Computes and returns a hash code value for this object.
     *
     * <p>This method is generally used to improve the performance of hash-based collections.</p>
     *
     * @return an integer hash code representing this object
     */
    @Override
    public int hashCode() {
        return Long.hashCode(this.id);
    }
}
