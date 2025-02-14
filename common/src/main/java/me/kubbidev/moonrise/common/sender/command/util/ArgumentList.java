package me.kubbidev.moonrise.common.sender.command.util;

import com.google.common.collect.ForwardingList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

/**
 * A list of {@link String} arguments, with extra methods to help
 * with parsing.
 */
public class ArgumentList extends ForwardingList<String> {
    private final List<String> backingList;

    public ArgumentList(List<String> backingList) {
        this.backingList = backingList;
    }

    @Override
    protected List<String> delegate() {
        return this.backingList;
    }

    public boolean indexOutOfBounds(int index) {
        return index < 0 || index >= this.size();
    }

    @Override
    public String get(int index) throws IndexOutOfBoundsException {
        return super.get(index).replace("{SPACE}", " ");
    }

    public String getOrDefault(int index, String defaultValue) {
        return this.indexOutOfBounds(index) ? defaultValue : this.get(index);
    }

    @Override
    public @NotNull ArgumentList subList(int fromIndex, int toIndex) {
        return new ArgumentList(super.subList(fromIndex, toIndex));
    }

    public int getInt(int index) throws ArgumentException {
        return getInt(index, ArgumentException.DetailedUsage::new);
    }

    public int getInt(int index, Supplier<ArgumentException> exceptionSupplier)
            throws ArgumentException {
        try {
            return Integer.parseInt(get(index));
        } catch (NumberFormatException e) {
            throw exceptionSupplier.get();
        }
    }

    public int getIntOrDefault(int index, int defaultValue) {
        if (indexOutOfBounds(index)) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(get(index));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}