package me.kubbidev.moonrise.common.storage.misc;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public final class DataConstraints {

    private DataConstraints() {
    }

    @Contract("!null -> !null")
    public static @Nullable String sanitize(String s) {
        return DataConstraints.sanitize(s, false);
    }

    @Contract("!null, _ -> !null")
    public static @Nullable String sanitize(String s, boolean lowerCase) {
        if (s != null && (s.isEmpty() || s.equalsIgnoreCase("null"))) {
            s = null;
        }

        if (s != null && lowerCase) {
            s = s.toLowerCase(Locale.ROOT);
        }
        return s;
    }

    public static @NotNull String desanitize(String s) {
        return DataConstraints.desanitize(s, false);
    }

    public static @NotNull String desanitize(String s, boolean lowerCase) {
        if (s == null) {
            s = "null";
        }
        if (lowerCase) {
            s = s.toLowerCase(Locale.ROOT);
        }
        return s;
    }
}
