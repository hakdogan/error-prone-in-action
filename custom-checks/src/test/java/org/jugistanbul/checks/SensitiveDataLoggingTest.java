package org.jugistanbul.checks;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SensitiveDataLogging}.
 *
 * @author hakdogan (hakdogan75@gmail.com)
 * Created on 7.10.2026
 ***/
class SensitiveDataLoggingTest {

    // The team's annotation, a customer with sensitive components, and an SLF4J stand-in
    private final CompilationTestHelper helper =
            CompilationTestHelper.newInstance(SensitiveDataLogging.class, getClass())
                    .addSourceLines(
                            "SensitiveData.java",
                            "package org.jugistanbul;",
                            "import java.lang.annotation.*;",
                            "@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD,",
                            "    ElementType.RECORD_COMPONENT})",
                            "public @interface SensitiveData {}")
                    .addSourceLines(
                            "Customer.java",
                            "import org.jugistanbul.SensitiveData;",
                            "record Customer(String name, @SensitiveData String email) {}")
                    .addSourceLines(
                            "Logger.java",
                            "package org.slf4j;",
                            "public interface Logger { void info(String message); }");

    @Test
    void flagsSensitiveAccessorAndObjectWithSensitiveFields() {
        helper.addSourceLines(
                        "Test.java",
                        "import java.lang.System.Logger.Level;",
                        "class Test {",
                        "  void run(System.Logger log, Customer customer) {",
                        "    // BUG: Diagnostic contains: 'customer.email()' contains sensitive data",
                        "    log.log(Level.INFO, \"Receipt sent to \" + customer.email());",
                        "    // BUG: Diagnostic contains: 'customer' contains sensitive data",
                        "    log.log(Level.INFO, \"Order placed by \" + (customer));",
                        "  }",
                        "}")
                .doTest();
    }

    @Test
    void flagsSensitiveFieldAndAnnotatedTypeAcrossLoggers() {
        helper.addSourceLines(
                        "Test.java",
                        "import org.jugistanbul.SensitiveData;",
                        "class Test {",
                        "  @SensitiveData record CardNumber(String value) {}",
                        "  static class Card { @SensitiveData String number; }",
                        "  void run(java.util.logging.Logger jul, org.slf4j.Logger slf4j,",
                        "      Card card, CardNumber cardNumber) {",
                        "    // BUG: Diagnostic contains: 'card.number' contains sensitive data",
                        "    jul.info(card.number);",
                        "    // BUG: Diagnostic contains: 'cardNumber' contains sensitive data",
                        "    slf4j.info(\"Charged \" + cardNumber);",
                        "  }",
                        "}")
                .doTest();
    }

    @Test
    void ignoresNonSensitiveValuesAndNonLoggerCalls() {
        helper.addSourceLines(
                        "Test.java",
                        "import java.lang.System.Logger.Level;",
                        "class Test {",
                        "  void run(System.Logger log, Customer customer, StringBuilder out) {",
                        "    log.log(Level.INFO, \"Order placed by \" + customer.name());",
                        "    log.log(Level.INFO, \"Email length: \" + customer.email().length());",
                        "    out.append(customer.email());",
                        "  }",
                        "}")
                .doTest();
    }

    @Test
    void canBeSuppressed() {
        helper.addSourceLines(
                        "Test.java",
                        "import java.lang.System.Logger.Level;",
                        "class Test {",
                        "  @SuppressWarnings(\"SensitiveDataLogging\")",
                        "  void run(System.Logger log, Customer customer) {",
                        "    log.log(Level.DEBUG, customer.email());",
                        "  }",
                        "}")
                .doTest();
    }
}
