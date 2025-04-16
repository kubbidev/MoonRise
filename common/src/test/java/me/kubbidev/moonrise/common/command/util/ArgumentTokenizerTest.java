package me.kubbidev.moonrise.common.command.util;

import com.google.common.collect.ImmutableList;
import me.kubbidev.moonrise.common.sender.command.util.ArgumentTokenizer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArgumentTokenizerTest {

    private static Stream<Arguments> testBasicTokenize() {
        return Stream.of(
            Arguments.of("", new String[]{}),
            Arguments.of("hello world", new String[]{"hello", "world"}),
            Arguments.of("hello  world", new String[]{"hello", "", "world"}),
            Arguments.of("hello   world", new String[]{"hello", "", "", "world"}),
            Arguments.of("\"hello world\"", new String[]{"hello world"}),
            Arguments.of("\"hello  world\"", new String[]{"hello  world"}),
            Arguments.of("\" hello world\"", new String[]{" hello world"}),
            Arguments.of("\"hello world \"", new String[]{"hello world "}),
            Arguments.of("\"hello\"\"world\"", new String[]{"hello", "world"}),
            Arguments.of("\"hello\" \"world\"", new String[]{"hello", "world"})
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testBasicTokenize(String input, String[] expectedTokens) {
        for (ArgumentTokenizer tokenizer : ArgumentTokenizer.values()) {
            List<String> tokens = tokenizer.tokenizeInput(input);
            assertEquals(ImmutableList.copyOf(expectedTokens), ImmutableList.copyOf(tokens),
                "tokenizer " + tokenizer + " produced tokens " + tokens);
        }
    }

    private static Stream<Arguments> testExecuteTokenize() {
        return Stream.of(
            Arguments.of("hello world ", new String[]{"hello", "world"}),
            Arguments.of("hello world  ", new String[]{"hello", "world", ""})
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testExecuteTokenize(String input, String[] expectedTokens) {
        List<String> tokens = ArgumentTokenizer.EXECUTE.tokenizeInput(input);
        assertEquals(ImmutableList.copyOf(expectedTokens), ImmutableList.copyOf(tokens));
    }

    private static Stream<Arguments> testTabCompleteTokenize() {
        return Stream.of(
            Arguments.of("hello world ", new String[]{"hello", "world", ""}),
            Arguments.of("hello world  ", new String[]{"hello", "world", "", ""})
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testTabCompleteTokenize(String input, String[] expectedTokens) {
        List<String> tokens = ArgumentTokenizer.TAB_COMPLETE.tokenizeInput(input);
        assertEquals(ImmutableList.copyOf(expectedTokens), ImmutableList.copyOf(tokens));
    }
}
