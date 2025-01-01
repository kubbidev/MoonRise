package me.kubbidev.moonrise.common.command.util;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ArgumentListTest {

    @Test
    public void testGetString() {
        ArgumentList list = new ArgumentList(ImmutableList.of("hello", "world{SPACE}"));

        assertEquals("hello", list.getOrDefault(0, "def"));
        assertEquals("world ", list.getOrDefault(1, "def"));
        assertEquals("def", list.getOrDefault(2, "def"));
        assertEquals("def", list.getOrDefault(-1, "def"));
        assertNull(list.getOrDefault(2, null));
        assertNull(list.getOrDefault(-1, null));
    }
}
