package me.kubbidev.moonrise.common.locale;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import me.kubbidev.moonrise.api.platform.Platform;
import me.kubbidev.moonrise.common.command.spec.Argument;
import me.kubbidev.moonrise.common.command.spec.CommandSpec;
import me.kubbidev.moonrise.common.database.Database;
import me.kubbidev.moonrise.common.extension.SimpleExtensionManager;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import me.kubbidev.moonrise.common.plugin.bootstrap.MoonRiseBootstrap;
import me.kubbidev.moonrise.common.util.ImmutableCollectors;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentIteratorFlag;
import net.kyori.adventure.text.ComponentIteratorType;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.*;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class MessageTest {

    private static final Set<Class<?>> MESSAGE_CLASSES = ImmutableSet.of(
            Message.Args0.class,
            Message.Args1.class,
            Message.Args2.class,
            Message.Args4.class
    );

    private static final Set<String> IGNORED_MISSING_TRANSLATION_KEYS = ImmutableSet.of(
            "moonrise.command.misc.invalid-input-empty-stub"
    );

    private static TranslationRegistry registry;
    private static Set<String> translationKeys;

    @BeforeAll
    public static void setupRenderer() {
        registry = TranslationRegistry.create(Key.key("moonrise", "test"));

        ResourceBundle bundle = ResourceBundle.getBundle("moonrise", Locale.ENGLISH, UTF8ResourceBundleControl.get());
        translationKeys = ImmutableSet.copyOf(bundle.keySet());
        registry.registerAll(Locale.ENGLISH, bundle, false);
    }

    private static Stream<Field> getMessageFields() {
        return Arrays.stream(Message.class.getDeclaredFields())
                .filter(f -> Modifier.isStatic(f.getModifiers()))
                .filter(f -> MESSAGE_CLASSES.contains(f.getType()));
    }

    @ParameterizedTest
    @MethodSource("getMessageFields")
    public void testMessage(Field field) {
        Component baseComponent = buildMessage(field);
        for (Component part : getNestedComponents(baseComponent)) {
            if (part instanceof TranslatableComponent component) {
                assertTranslatableComponentValid(component);
            }
        }
    }

    @ParameterizedTest
    @EnumSource
    public void testCommandUsageMessages(CommandSpec commandSpec) {
        assertTranslatableComponentValid(commandSpec.description());

        List<Argument> args = commandSpec.args();
        if (args != null) {
            for (Argument arg : args) {
                assertTranslatableComponentValid(arg.description());
            }
        }
    }

    private static void assertTranslatableComponentValid(TranslatableComponent component) {
        String key = component.key();

        if (IGNORED_MISSING_TRANSLATION_KEYS.contains(key)) {
            return;
        }

        assertTrue(translationKeys.contains(key), "unknown translation key: " + key);

        List<Component> args = component.args();
        MessageFormat format = registry.translate(key, Locale.ENGLISH);
        assertNotNull(format);
        assertEquals(format.getFormats().length, args.size(), "number of formats in translation for " + key + " does not match number of arguments");
    }

    private static Iterable<Component> getNestedComponents(Component component) {
        return component.iterable(ComponentIteratorType.BREADTH_FIRST, ImmutableSet.of(
                ComponentIteratorFlag.INCLUDE_HOVER_SHOW_TEXT_COMPONENT,
                ComponentIteratorFlag.INCLUDE_TRANSLATABLE_COMPONENT_ARGUMENTS
        ));
    }

    private static Component buildMessage(Field field) {
        Class<?> type = field.getType();

        List<Method> buildMethods = Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.getName().equals("build"))
                .collect(ImmutableCollectors.toList());

        if (buildMethods.size() != 1) {
            throw new IllegalStateException("Expected exactly one build() method - " + buildMethods);
        }

        Method buildMethod = buildMethods.getFirst();
        Object[] parameters = new Object[buildMethod.getParameterCount()];

        if (buildMethod.getParameterCount() != 0) {
            Type genericType = field.getGenericType();
            Type[] typeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
            for (int i = 0; i < typeArguments.length; i++) {
                Type typeArgument = typeArguments[i];
                parameters[i] = mockArgument(typeArgument);
            }
        }

        try {
            Object builder = field.get(null);
            return (Component) buildMethod.invoke(builder, parameters);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object mockArgument(Type type) {
        if (type instanceof ParameterizedType) {
            return mockArgument(((ParameterizedType) type).getRawType());
        }

        Class<?> clazz = (Class<?>) type;

        if (clazz == String.class) {
            return "stub";
        } else if (clazz == Integer.class) {
            return 0;
        } else if (clazz == Boolean.class) {
            return false;
        } else if (clazz == Double.class) {
            return 0.0;
        } else if (clazz == Component.class) {
            return Component.text("stub");
        } else if (clazz == List.class) {
            return ImmutableList.of();
        } else if (clazz == Collection.class) {
            return ImmutableList.of();
        }

        Object mock;
        if (clazz == MoonRisePlugin.class) {
            mock = mock(clazz, Answers.RETURNS_DEEP_STUBS);
        } else {
            mock = mock(clazz, Answers.RETURNS_SMART_NULLS);
        }

        if (mock instanceof MoonRiseBootstrap bootstrap) {
            lenient().when(bootstrap.getType()).thenReturn(Platform.Type.STANDALONE);
            lenient().when(bootstrap.getStartupTime()).thenReturn(Instant.now());
        } else if (mock instanceof MoonRisePlugin plugin) {

            MoonRiseBootstrap bootstrap = (MoonRiseBootstrap) mockArgument(MoonRiseBootstrap.class);
            lenient().when(plugin.getBootstrap()).thenReturn(bootstrap);

            Database database = (Database) mockArgument(Database.class);
            lenient().when(plugin.getDatabase()).thenReturn(database);

            SimpleExtensionManager extensionManager = (SimpleExtensionManager) mockArgument(SimpleExtensionManager.class);
            lenient().when(plugin.getExtensionManager()).thenReturn(extensionManager);
        } else if (mock instanceof SimpleExtensionManager extensionManager) {
            lenient().when(extensionManager.getLoadedExtensions()).thenReturn(ImmutableList.of());
        }

        return mock;
    }
}
