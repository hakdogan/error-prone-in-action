package org.jugistanbul.checks;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link BigDecimalDoubleConstructor}.
 *
 * @author hakdogan (hakdogan75@gmail.com)
 * Created on 7.10.2026
 ***/
class BigDecimalDoubleConstructorTest {

    private final CompilationTestHelper helper =
            CompilationTestHelper.newInstance(BigDecimalDoubleConstructor.class, getClass());

    private final BugCheckerRefactoringTestHelper refactoringHelper =
            BugCheckerRefactoringTestHelper.newInstance(BigDecimalDoubleConstructor.class, getClass());

    @Test
    void flagsDoubleConstructor() {
        helper.addSourceLines(
                        "Test.java",
                        "import java.math.BigDecimal;",
                        "class Test {",
                        "  double price() { return 9.99; }",
                        "  void run(double d) {",
                        "    // BUG: Diagnostic contains: BigDecimalDoubleConstructor",
                        "    new BigDecimal(0.1);",
                        "    // BUG: Diagnostic contains: BigDecimalDoubleConstructor",
                        "    new BigDecimal(d);",
                        "    // BUG: Diagnostic contains: BigDecimalDoubleConstructor",
                        "    new BigDecimal(price());",
                        "  }",
                        "}")
                .doTest();
    }

    @Test
    void ignoresOtherConstructors() {
        helper.addSourceLines(
                        "Test.java",
                        "import java.math.BigDecimal;",
                        "import java.math.MathContext;",
                        "class Test {",
                        "  void run() {",
                        "    new BigDecimal(\"0.1\");",
                        "    new BigDecimal(1);",
                        "    new BigDecimal(1L);",
                        "    new BigDecimal(new char[] {'1'});",
                        "    BigDecimal.valueOf(0.1);",
                        "  }",
                        "}")
                .doTest();
    }

    @Test
    void replacesWithValueOf() {
        refactoringHelper
                .addInputLines(
                        "Test.java",
                        "import java.math.BigDecimal;",
                        "class Test {",
                        "  BigDecimal run(double d) {",
                        "    BigDecimal a = new BigDecimal(0.1);",
                        "    return a.add(new BigDecimal(d * 2));",
                        "  }",
                        "}")
                .addOutputLines(
                        "Test.java",
                        "import java.math.BigDecimal;",
                        "class Test {",
                        "  BigDecimal run(double d) {",
                        "    BigDecimal a = BigDecimal.valueOf(0.1);",
                        "    return a.add(BigDecimal.valueOf(d * 2));",
                        "  }",
                        "}")
                .doTest();
    }

    @Test
    void addsImportWhenFullyQualified() {
        refactoringHelper
                .addInputLines(
                        "Test.java",
                        "class Test {",
                        "  Object run() {",
                        "    return new java.math.BigDecimal(0.5);",
                        "  }",
                        "}")
                .addOutputLines(
                        "Test.java",
                        "import java.math.BigDecimal;",
                        "class Test {",
                        "  Object run() {",
                        "    return BigDecimal.valueOf(0.5);",
                        "  }",
                        "}")
                .doTest();
    }
}
