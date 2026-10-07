package org.jugistanbul.checks;

import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.matchers.field.FieldMatchers.staticField;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.IdentifierTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MemberSelectTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;

/**
 * Flags any use of {@code System.out} or {@code System.err}; application code should log instead.
 *
 * <p>Matches the field access itself rather than {@code println} calls, so {@code printf},
 * {@code flush} and passing the stream around are caught too. {@link IdentifierTreeMatcher} covers
 * {@code import static java.lang.System.out}.
 *
 * @author hakdogan (hakdogan75@gmail.com)
 * Created on 7.10.2026
 ***/
@AutoService(BugChecker.class)
@BugPattern(
        summary = "Use a logger instead of System.out / System.err",
        severity = WARNING)
public final class NoSystemOut extends BugChecker
        implements MemberSelectTreeMatcher, IdentifierTreeMatcher {

    private static final Matcher<ExpressionTree> SYSTEM_STREAM =
            staticField().onClass("java.lang.System").namedAnyOf("out", "err");

    @Override
    public Description matchMemberSelect(MemberSelectTree tree, VisitorState state) {
        return match(tree, state);
    }

    @Override
    public Description matchIdentifier(IdentifierTree tree, VisitorState state) {
        return match(tree, state);
    }

    private Description match(ExpressionTree tree, VisitorState state) {
        return SYSTEM_STREAM.matches(tree, state) ? describeMatch(tree) : Description.NO_MATCH;
    }
}
