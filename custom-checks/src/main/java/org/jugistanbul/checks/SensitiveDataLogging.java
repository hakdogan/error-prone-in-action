package org.jugistanbul.checks;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import java.util.stream.Stream;
import javax.lang.model.element.ElementKind;

/**
 * Flags log calls that would write data marked with the team's {@code @SensitiveData} annotation,
 * such as national ids, card numbers or email addresses. Covers {@link System.Logger}, {@code
 * java.util.logging.Logger} and SLF4J's {@code Logger}.
 *
 * <p>A logged value is sensitive when it is an annotated field or accessor ({@code
 * customer.email()}), when its class is annotated, or when its class has an annotated field, since
 * {@code toString()} would then print that field. String concatenation is followed into its
 * operands, so {@code "placed by " + customer} is caught while {@code "placed by " +
 * customer.name()} is not. Values passed through local variables are not tracked.
 *
 * @author hakdogan (hakdogan75@gmail.com)
 * Created on 7.10.2026
 ***/
@AutoService(BugChecker.class)
@BugPattern(summary = "Sensitive data must not be written to logs", severity = ERROR)
public final class SensitiveDataLogging extends BugChecker implements MethodInvocationTreeMatcher {

    private static final String SENSITIVE_DATA = "org.jugistanbul.SensitiveData";

    private static final Matcher<ExpressionTree> LOGGER_CALL =
            instanceMethod()
                    .onDescendantOfAny(
                            "java.lang.System.Logger", "java.util.logging.Logger", "org.slf4j.Logger");

    @Override
    public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
        if (!LOGGER_CALL.matches(tree, state)) {
            return Description.NO_MATCH;
        }
        tree.getArguments().stream()
                .flatMap(argument -> stringifiedValues(argument, state))
                .filter(value -> isSensitive(value, state))
                .forEach(value -> state.reportMatch(
                        buildDescription(value)
                                .setMessage("'%s' contains sensitive data and must not be written to logs"
                                        .formatted(state.getSourceForNode(value)))
                                .build()));
        return Description.NO_MATCH;
    }

    /** The values a log argument turns into text: the operands of a string concatenation, or itself. */
    private static Stream<ExpressionTree> stringifiedValues(ExpressionTree tree, VisitorState state) {
        var expression = ASTHelpers.stripParentheses(tree);
        if (expression.getKind() == Kind.PLUS
                && ASTHelpers.isSameType(ASTHelpers.getType(expression), state.getSymtab().stringType, state)) {
            var concatenation = (BinaryTree) expression;
            return Stream.concat(
                    stringifiedValues(concatenation.getLeftOperand(), state),
                    stringifiedValues(concatenation.getRightOperand(), state));
        }
        return Stream.of(expression);
    }

    private static boolean isSensitive(ExpressionTree value, VisitorState state) {
        var symbol = ASTHelpers.getSymbol(value);
        if (symbol != null && ASTHelpers.hasAnnotation(symbol, SENSITIVE_DATA, state)) {
            return true;
        }
        var type = ASTHelpers.getType(value);
        if (type == null || type.isPrimitive() || !(type.tsym instanceof ClassSymbol valueClass)) {
            return false;
        }
        return ASTHelpers.hasAnnotation(valueClass, SENSITIVE_DATA, state)
                || valueClass.getEnclosedElements().stream()
                        .anyMatch(member -> member.getKind() == ElementKind.FIELD
                                && ASTHelpers.hasAnnotation(member, SENSITIVE_DATA, state));
    }
}
