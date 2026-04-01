package io.github.roberto22palomar.pepenium.core.runtime;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestWatcher;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JUnit 5 extension that powers the annotation-first Pepenium authoring model.
 */
public class PepeniumExtension implements BeforeAllCallback, BeforeEachCallback,
        AfterEachCallback, AfterAllCallback, TestWatcher, ParameterResolver {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(PepeniumExtension.class);

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        PepeniumTest config = requireConfig(context);
        PepeniumRuntime runtime = getRuntime(context);
        if (config.automaticLifecycle()) {
            runtime.initializeDriverForProfile(config.target(), normalizeProfile(config.profile()));
        }
        injectFields(context, runtime, config);
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        PepeniumRuntime runtime = getRuntime(context);
        runtime.clearPerTestState();
        runtime.beginTestObservability();
        injectFields(context, runtime, requireConfig(context));
    }

    @Override
    public void afterEach(ExtensionContext context) {
        PepeniumRuntime runtime = getRuntime(context);
        runtime.writeTestReport(context.getDisplayName(), context.getExecutionException().orElse(null));
        runtime.clearPerTestState();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        PepeniumTest config = requireConfig(context);
        PepeniumRuntime runtime = getRuntime(context);
        if (config.automaticLifecycle()) {
            runtime.cleanupDriver();
        }
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        getRuntime(context).reportFailure(context.getDisplayName(), cause);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return isSupportedInjectionPoint(parameterContext.getParameter().getType(), parameterContext.isAnnotated(PepeniumInject.class));
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        try {
            PepeniumTest config = requireConfig(extensionContext);
            PepeniumRuntime runtime = getRuntime(extensionContext);
            PepeniumInjectionSupport injector = getInjector(extensionContext, runtime, config);
            return injector.resolve(parameterContext.getParameter().getType());
        } catch (RuntimeException e) {
            throw new ParameterResolutionException(e.getMessage(), e);
        }
    }

    private void injectFields(ExtensionContext context, PepeniumRuntime runtime, PepeniumTest config) {
        Object testInstance = context.getTestInstance()
                .orElseThrow(() -> new IllegalStateException(
                        "@PepeniumTest requires a PER_CLASS test instance. " +
                                "If you are not using the default annotation metadata, add @TestInstance(PER_CLASS)."
                ));
        PepeniumInjectionSupport injector = getInjector(context, runtime, config);
        injector.injectInto(testInstance);
    }

    private PepeniumRuntime getRuntime(ExtensionContext context) {
        ExtensionContext.Store store = context.getRoot().getStore(NAMESPACE);
        String key = context.getRequiredTestClass().getName() + ".runtime";
        return store.getOrComputeIfAbsent(key, ignored -> new PepeniumRuntime(), PepeniumRuntime.class);
    }

    @SuppressWarnings("unchecked")
    private PepeniumInjectionSupport getInjector(ExtensionContext context, PepeniumRuntime runtime, PepeniumTest config) {
        ExtensionContext.Store store = context.getRoot().getStore(NAMESPACE);
        String key = context.getRequiredTestClass().getName() + ".components";
        Map<Class<?>, Object> cache = store.getOrComputeIfAbsent(key, ignored -> new ConcurrentHashMap<>(), Map.class);
        return new PepeniumInjectionSupport(runtime, config, cache);
    }

    private PepeniumTest requireConfig(ExtensionContext context) {
        PepeniumTest config = context.getRequiredTestClass().getAnnotation(PepeniumTest.class);
        if (config == null) {
            throw new IllegalStateException("PepeniumExtension requires @PepeniumTest on the test class");
        }
        return config;
    }

    private boolean isSupportedInjectionPoint(Class<?> type, boolean annotated) {
        return annotated || PepeniumInjectionSupport.isDirectlySupported(type);
    }

    private String normalizeProfile(String profile) {
        return profile == null || profile.isBlank() ? null : profile;
    }
}
