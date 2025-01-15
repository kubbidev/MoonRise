package me.kubbidev.moonrise.common.util;

public record Long2(long a, long b) {

    @Override
    public boolean equals(Object o) {
        return o instanceof Long2(long a1, long b1)
                && this.a == a1
                && this.b == b1;

    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(this.a);
        result = 31 * result + Long.hashCode(this.b);
        return result;
    }
}
