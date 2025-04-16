package me.kubbidev.moonrise.common.dependencies.relocation;

import java.util.Objects;

public final class Relocation {

    private static final String RELOCATION_PREFIX = "me.kubbidev.moonrise.lib.";

    public static Relocation of(String id, String pattern) {
        return new Relocation(pattern.replace("{}", "."), RELOCATION_PREFIX + id);
    }

    private final String pattern;
    private final String relocatedPattern;

    private Relocation(String pattern, String relocatedPattern) {
        this.pattern = pattern;
        this.relocatedPattern = relocatedPattern;
    }

    public String getPattern() {
        return this.pattern;
    }

    public String getRelocatedPattern() {
        return this.relocatedPattern;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Relocation that)) {
            return false;
        }

        return this.pattern.equals(that.pattern)
            && this.relocatedPattern.equals(that.relocatedPattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.pattern, this.relocatedPattern);
    }
}