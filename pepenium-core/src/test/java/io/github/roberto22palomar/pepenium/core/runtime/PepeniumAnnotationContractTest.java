package io.github.roberto22palomar.pepenium.core.runtime;

import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PepeniumAnnotationContractTest {

    @Test
    void pepeniumTestCarriesTheExpectedJUnitMetadata() {
        TestInstance testInstance = PepeniumTest.class.getAnnotation(TestInstance.class);
        ExtendWith extendWith = PepeniumTest.class.getAnnotation(ExtendWith.class);
        Target target = PepeniumTest.class.getAnnotation(Target.class);
        Retention retention = PepeniumTest.class.getAnnotation(Retention.class);

        assertNotNull(testInstance);
        assertEquals(TestInstance.Lifecycle.PER_CLASS, testInstance.value());
        assertNotNull(extendWith);
        assertEquals(PepeniumExtension.class, extendWith.value()[0]);
        assertNotNull(target);
        assertEquals(ElementType.TYPE, target.value()[0]);
        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());

        PepeniumTest annotation = AnnotationContractFixture.class.getAnnotation(PepeniumTest.class);
        assertNotNull(annotation);
        assertEquals(TestTarget.WEB_DESKTOP, annotation.target());
        assertEquals("local-web", annotation.profile());
        assertTrue(annotation.automaticLifecycle());
    }

    @Test
    void pepeniumInjectAndPepeniumPageStayRuntimeVisibleAuthoringAnnotations() {
        Target injectTarget = PepeniumInject.class.getAnnotation(Target.class);
        Retention injectRetention = PepeniumInject.class.getAnnotation(Retention.class);
        Target pageTarget = PepeniumPage.class.getAnnotation(Target.class);
        Retention pageRetention = PepeniumPage.class.getAnnotation(Retention.class);

        assertNotNull(injectTarget);
        assertEquals(ElementType.FIELD, injectTarget.value()[0]);
        assertEquals(ElementType.PARAMETER, injectTarget.value()[1]);
        assertEquals(ElementType.CONSTRUCTOR, injectTarget.value()[2]);
        assertNotNull(injectRetention);
        assertEquals(RetentionPolicy.RUNTIME, injectRetention.value());

        assertNotNull(pageTarget);
        assertEquals(ElementType.TYPE, pageTarget.value()[0]);
        assertNotNull(pageRetention);
        assertEquals(RetentionPolicy.RUNTIME, pageRetention.value());
    }

    @Test
    void pepeniumStepsRemainsAFriendlyLambdaContract() {
        PepeniumSteps steps = ignored -> {
        };

        steps.step("plug-and-play step");

        assertTrue(PepeniumSteps.class.isInterface());
        assertEquals(1, PepeniumSteps.class.getDeclaredMethods().length);
    }

    @PepeniumTest(target = TestTarget.WEB_DESKTOP, profile = "local-web")
    private static final class AnnotationContractFixture {
    }
}
