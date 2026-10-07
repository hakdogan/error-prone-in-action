package org.jugistanbul.checks;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link NoSystemOut}.
 *
 * @author hakdogan (hakdogan75@gmail.com)
 * Created on 7.10.2026
 ***/
class NoSystemOutTest {

    private final CompilationTestHelper helper =
            CompilationTestHelper.newInstance(NoSystemOut.class, getClass());

    @Test
    void flagsSystemOutAndErr() {
        helper.addSourceLines(
                        "Test.java",
                        "class Test {",
                        "  void run(Exception e) {",
                        "    // BUG: Diagnostic contains: NoSystemOut",
                        "    System.out.println(\"hello\");",
                        "    // BUG: Diagnostic contains: NoSystemOut",
                        "    System.err.printf(\"%s%n\", e);",
                        "    // BUG: Diagnostic contains: NoSystemOut",
                        "    java.io.PrintStream stream = System.out;",
                        "  }",
                        "}")
                .doTest();
    }

    @Test
    void flagsStaticImportUsage() {
        helper.addSourceLines(
                        "Test.java",
                        "import static java.lang.System.out;",
                        "class Test {",
                        "  void run() {",
                        "    // BUG: Diagnostic contains: NoSystemOut",
                        "    out.println(\"hello\");",
                        "  }",
                        "}")
                .doTest();
    }

    @Test
    void ignoresOtherSystemMembersAndLookalikes() {
        helper.addSourceLines(
                        "Test.java",
                        "class Test {",
                        "  static class Console { static final java.io.PrintStream out = null; }",
                        "  long run() {",
                        "    System.getenv(\"HOME\");",
                        "    Console.out.hashCode();",
                        "    return System.currentTimeMillis();",
                        "  }",
                        "}")
                .doTest();
    }

    @Test
    void canBeSuppressed() {
        helper.addSourceLines(
                        "Test.java",
                        "class Test {",
                        "  @SuppressWarnings(\"NoSystemOut\")",
                        "  void run() {",
                        "    System.out.println(\"cli output\");",
                        "  }",
                        "}")
                .doTest();
    }
}
