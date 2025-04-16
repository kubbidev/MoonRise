package me.kubbidev.moonrise.common.sender.command.tabcomplete;

import java.util.Locale;
import java.util.function.UnaryOperator;
import me.kubbidev.moonrise.common.util.Predicates;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@FunctionalInterface
public interface CompletionSupplier {

    CompletionSupplier EMPTY = partial -> Collections.emptyList();

    static CompletionSupplier startsWith(String... strings) {
        return startsWith(() -> Arrays.stream(strings));
    }

    static CompletionSupplier startsWith(Collection<String> strings) {
        return startsWith(strings::stream);
    }

    static CompletionSupplier startsWith(Supplier<Stream<String>> stringsSupplier) {
        return partial -> stringsSupplier.get().filter(Predicates.startsWithIgnoreCase(partial))
            .collect(Collectors.toList());
    }

    static CompletionSupplier contains(String... strings) {
        return contains(() -> Arrays.stream(strings));
    }

    static CompletionSupplier contains(Collection<String> strings) {
        return contains(strings::stream);
    }

    static CompletionSupplier contains(Supplier<Stream<String>> stringsSupplier) {
        return partial -> stringsSupplier.get().filter(Predicates.containsIgnoreCase(partial))
            .collect(Collectors.toList());
    }

    List<String> supplyCompletions(String partial);

    default CompletionSupplier compose(CompletionSupplier supplier) {
        return partial -> {
            List<String> completions = supplyCompletions(partial);
            List<String> composeList = supplier.supplyCompletions(partial);
            return Stream.concat(completions.stream(), composeList.stream())
                .distinct()
                .collect(Collectors.toList());
        };
    }

    default CompletionSupplier operate(UnaryOperator<List<String>> filter) {
        return partial -> filter.apply(supplyCompletions(partial));
    }

    default CompletionSupplier limit(long limit) {
        return operate(strings -> strings.stream().limit(limit).collect(Collectors.toList()));
    }

    default CompletionSupplier distinct() {
        return operate(strings -> strings.stream().distinct().collect(Collectors.toList()));
    }

    default CompletionSupplier prefixed(String prefix) {
        return operate(strings -> strings.stream().map(s -> prefix + s).collect(Collectors.toList()));
    }

    default CompletionSupplier suffixed(String suffix) {
        return operate(strings -> strings.stream().map(s -> suffix + s).collect(Collectors.toList()));
    }

    default CompletionSupplier lowercase(Locale locale) {
        return operate(strings -> strings.stream().map(s -> s.toLowerCase(locale)).collect(Collectors.toList()));
    }

    default CompletionSupplier uppercase(Locale locale) {
        return operate(strings -> strings.stream().map(s -> s.toUpperCase(locale)).collect(Collectors.toList()));
    }
}