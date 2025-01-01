package me.kubbidev.moonrise.common.command.util;

import com.google.common.collect.ForwardingList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
}