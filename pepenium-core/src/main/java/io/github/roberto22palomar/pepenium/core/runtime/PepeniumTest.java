package io.github.roberto22palomar.pepenium.core.runtime;

import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation-first Pepenium test entry point for plug-and-play test classes.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
@ExtendWith(PepeniumExtension.class)
public @interface PepeniumTest {

    /**
     * Functional target for the test class.
     */
    TestTarget target();

    /**
     * Optional default execution profile id for the test class.
     */
    String profile() default "";

    /**
     * Enables or disables the built-in automatic lifecycle.
     */
    boolean automaticLifecycle() default true;
}
