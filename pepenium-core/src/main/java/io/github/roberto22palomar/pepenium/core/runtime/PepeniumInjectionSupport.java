package io.github.roberto22palomar.pepenium.core.runtime;

import io.appium.java_client.AppiumDriver;
import io.github.roberto22palomar.pepenium.core.observability.StepTracker;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class PepeniumInjectionSupport {

    private static final String ACTIONS_WEB = "io.github.roberto22palomar.pepenium.toolkit.actions.ActionsWeb";
    private static final String ASSERTIONS_WEB = "io.github.roberto22palomar.pepenium.toolkit.assertions.AssertionsWeb";
    private static final String ACTIONS_APP = "io.github.roberto22palomar.pepenium.toolkit.actions.ActionsApp";
    private static final String ASSERTIONS_APP = "io.github.roberto22palomar.pepenium.toolkit.assertions.AssertionsApp";
    private static final String ACTIONS_APP_IOS = "io.github.roberto22palomar.pepenium.toolkit.actions.ActionsAppIOS";
    private static final String ASSERTIONS_APP_IOS = "io.github.roberto22palomar.pepenium.toolkit.assertions.AssertionsAppIOS";

    static final class CacheState {
        private final Map<Class<?>, Object> components = new HashMap<>();
        private long lifecycleVersion = Long.MIN_VALUE;

        void align(long version) {
            if (lifecycleVersion != version) {
                components.clear();
                lifecycleVersion = version;
            }
        }

        Object get(Class<?> type) {
            return components.get(type);
        }

        void put(Class<?> type, Object instance) {
            components.put(type, instance);
        }
    }

    static final class MissingLifecycleDependencyException extends IllegalStateException {
        MissingLifecycleDependencyException(String message) {
            super(message);
        }
    }

    private final PepeniumRuntime runtime;
    private final PepeniumTest config;
    private final CacheState cache;

    PepeniumInjectionSupport(PepeniumRuntime runtime, PepeniumTest config, CacheState cache) {
        this.runtime = runtime;
        this.config = config;
        this.cache = cache;
    }

    static boolean isDirectlySupported(Class<?> type) {
        return type == WebDriver.class
                || type == DriverSession.class
                || type == AppiumDriver.class
                || type.getName().equals(ACTIONS_WEB)
                || type.getName().equals(ASSERTIONS_WEB)
                || type.getName().equals(ACTIONS_APP)
                || type.getName().equals(ASSERTIONS_APP)
                || type.getName().equals(ACTIONS_APP_IOS)
                || type.getName().equals(ASSERTIONS_APP_IOS)
                || type == PepeniumSteps.class;
    }

    Object resolve(Class<?> type) {
        return resolve(type, new HashSet<>());
    }

    void injectInto(Object target) {
        injectInto(target, true);
    }

    void injectInto(Object target, boolean strictLifecycle) {
        for (Field field : allFieldsOf(target.getClass())) {
            if (!field.isAnnotationPresent(PepeniumInject.class)) {
                continue;
            }
            if (Modifier.isFinal(field.getModifiers())) {
                throw new IllegalStateException("@PepeniumInject fields must not be final: " + field);
            }
            try {
                setField(field, target, resolve(field.getType()));
            } catch (MissingLifecycleDependencyException e) {
                if (strictLifecycle) {
                    throw e;
                }
                setField(field, target, null);
            }
        }

        if (target.getClass().isAnnotationPresent(PepeniumPage.class)) {
            PageFactory.initElements(requireWebDriver(target.getClass()), target);
        }
    }

    private Object resolve(Class<?> type, Set<Class<?>> resolutionPath) {
        if (isDirectlySupported(type)) {
            return resolveDirect(type);
        }

        if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
            throw new IllegalStateException("Unsupported Pepenium injection target: " + type.getName());
        }

        Object cached = cache.get(type);
        if (cached != null) {
            return cached;
        }

        if (!resolutionPath.add(type)) {
            throw new IllegalStateException("Circular Pepenium injection dependency detected for " + type.getName());
        }
        try {
            Object instance = instantiate(type, resolutionPath);
            injectInto(instance);
            cache.put(type, instance);
            return instance;
        } finally {
            resolutionPath.remove(type);
        }
    }

    private Object instantiate(Class<?> type, Set<Class<?>> resolutionPath) {
        Constructor<?> constructor = selectConstructor(type);
        try {
            constructor.setAccessible(true);
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            Object[] args = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                args[i] = resolve(parameterTypes[i], resolutionPath);
            }
            return constructor.newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to create Pepenium component " + type.getName(), e);
        }
    }

    private Constructor<?> selectConstructor(Class<?> type) {
        Constructor<?>[] constructors = type.getDeclaredConstructors();
        if (constructors.length == 0) {
            throw new IllegalStateException("Type " + type.getName() + " does not expose a usable constructor");
        }

        ArrayList<Constructor<?>> annotated = new ArrayList<>();
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(PepeniumInject.class)) {
                annotated.add(constructor);
            }
        }
        if (annotated.size() > 1) {
            throw new IllegalStateException("Type " + type.getName()
                    + " declares multiple @PepeniumInject constructors. Keep only one.");
        }
        if (annotated.size() == 1) {
            return annotated.get(0);
        }
        if (constructors.length == 1) {
            return constructors[0];
        }
        throw new IllegalStateException("Type " + type.getName()
                + " declares multiple constructors. Annotate the intended one with @PepeniumInject."
                + " Available constructors: " + Arrays.toString(constructors));
    }

    private Object resolveDirect(Class<?> type) {
        if (type == WebDriver.class) {
            return requireWebDriver(type);
        }
        if (type == DriverSession.class) {
            return requireSession(type);
        }
        if (type == AppiumDriver.class) {
            return requireAppiumDriver(type);
        }
        if (type.getName().equals(ACTIONS_WEB)) {
            return instantiateToolkitType(type, requireWebDriver(type));
        }
        if (type.getName().equals(ASSERTIONS_WEB)) {
            return instantiateToolkitType(type, requireWebDriver(type));
        }
        if (type.getName().equals(ACTIONS_APP)) {
            return instantiateToolkitType(type, requireAppiumDriver(type));
        }
        if (type.getName().equals(ASSERTIONS_APP)) {
            return instantiateToolkitType(type, requireAppiumDriver(type));
        }
        if (type.getName().equals(ACTIONS_APP_IOS)) {
            return instantiateToolkitType(type, requireAppiumDriver(type));
        }
        if (type.getName().equals(ASSERTIONS_APP_IOS)) {
            return instantiateToolkitType(type, requireAppiumDriver(type));
        }
        if (type == PepeniumSteps.class) {
            return (PepeniumSteps) StepTracker::record;
        }
        throw new IllegalStateException("Unsupported direct Pepenium injection type: " + type.getName());
    }

    private WebDriver requireWebDriver(Object target) {
        WebDriver driver = runtime.getDriver();
        if (driver == null) {
            throw missingLifecycle(target, "WebDriver");
        }
        return driver;
    }

    private DriverSession requireSession(Object target) {
        DriverSession session = runtime.getSession();
        if (session == null) {
            throw missingLifecycle(target, "DriverSession");
        }
        return session;
    }

    private AppiumDriver requireAppiumDriver(Object target) {
        DriverSession session = runtime.getSession();
        if (session == null) {
            throw missingLifecycle(target, "AppiumDriver");
        }
        return runtime.getAppiumDriver();
    }

    private MissingLifecycleDependencyException missingLifecycle(Object target, String dependencyName) {
        String targetName = target instanceof Class ? ((Class<?>) target).getName() : target.getClass().getName();
        return new MissingLifecycleDependencyException(
                "Cannot inject " + dependencyName + " into " + targetName
                        + " because no active Pepenium session exists. "
                        + "Use @PepeniumTest with automaticLifecycle=true or initialize the driver before requesting framework helpers."
        );
    }

    private Object instantiateToolkitType(Class<?> type, Object driverLike) {
        try {
            Constructor<?> constructor = type.getDeclaredConstructor(driverLike.getClass().getInterfaces().length == 0
                    ? driverLike.getClass()
                    : driverLike instanceof AppiumDriver ? AppiumDriver.class : WebDriver.class);
            constructor.setAccessible(true);
            return constructor.newInstance(driverLike);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to create toolkit helper " + type.getName(), e);
        }
    }

    private Iterable<Field> allFieldsOf(Class<?> type) {
        Set<String> seen = new HashSet<>();
        java.util.List<Field> fields = new java.util.ArrayList<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (seen.add(current.getName() + "#" + field.getName())) {
                    fields.add(field);
                }
            }
            current = current.getSuperclass();
        }
        return fields;
    }

    private void setField(Field field, Object target, Object value) {
        try {
            field.setAccessible(true);
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to inject field " + field, e);
        }
    }
}
