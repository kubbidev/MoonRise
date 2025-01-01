package me.kubbidev.moonrise.common.locale;

import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TranslationTest {

    @Test
    public void testBundleParse() {
        ResourceBundle bundle = ResourceBundle.getBundle("moonrise", Locale.ENGLISH, UTF8ResourceBundleControl.get());
        Set<String> keys = bundle.keySet();
        assertTrue(keys.size() > 50);

        for (String key : keys) {
            assertTrue(key.startsWith("moonrise."), "key " + key + " should start with 'moonrise.'");
        }
    }
}
