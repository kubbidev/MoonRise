package me.kubbidev.moonrise.common.api;

import net.moonrise.api.MoonRise;
import net.moonrise.api.MoonRiseProvider;

import java.lang.reflect.Method;

@SuppressWarnings("CallToPrintStackTrace")
public class ApiRegistrationUtil {
    private static final Method REGISTER;
    private static final Method UNREGISTER;
    static {
        try {
            REGISTER = MoonRiseProvider.class.getDeclaredMethod("register", MoonRise.class);
            REGISTER.setAccessible(true);

            UNREGISTER = MoonRiseProvider.class.getDeclaredMethod("unregister");
            UNREGISTER.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static void registerProvider(MoonRise moonRiseApi) {
        try {
            REGISTER.invoke(null, moonRiseApi);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unregisterProvider() {
        try {
            UNREGISTER.invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}