package org.jugistanbul.checks;

import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.matchers.method.MethodMatchers.constructor;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.NewClassTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewClassTree;

/**
 * Flags {@code new BigDecimal(double)}, which captures the exact binary value of the double
 * ({@code new BigDecimal(0.1)} is 0.1000000000000000055511151231257827...).
 *
 * <p>Suggests {@code BigDecimal.valueOf(double)}, which goes through {@link Double#toString} and so
 * yields the value the author most likely meant. Unlike the built-in {@code BigDecimalLiteralDouble},
 * this also catches non-literal arguments such as variables and method results.
 *
 * @author hakdogan (hakdogan75@gmail.com)
 * Created on 7.10.2026
 ***/
@AutoService(BugChecker.class)
@BugPattern(
        summary = "new BigDecimal(double) is imprecise; use BigDecimal.valueOf(double) instead",
        severity = WARNING)
public final class BigDecimalDoubleConstructor extends BugChecker implements NewClassTreeMatcher {

    private static final Matcher<ExpressionTree> DOUBLE_CONSTRUCTOR =
            constructor().forClass("java.math.BigDecimal").withParameters("double");

    @Override
    public Description matchNewClass(NewClassTree tree, VisitorState state) {
        if (!DOUBLE_CONSTRUCTOR.matches(tree, state)) {
            return Description.NO_MATCH;
        }
        var fix = SuggestedFix.builder();
        var bigDecimal = SuggestedFixes.qualifyType(state, fix, "java.math.BigDecimal");
        var argument = state.getSourceForNode(tree.getArguments().getFirst());
        fix.replace(tree, bigDecimal + ".valueOf(" + argument + ")");
        return describeMatch(tree, fix.build());
    }
}
