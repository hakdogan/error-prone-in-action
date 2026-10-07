package org.jugistanbul.refaster;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

/**
 * Rewrites {@code s.length() == 0} to {@code s.isEmpty()} for any {@link String} expression.
 *
 * <p>RefasterRuleCompiler compiles this class into {@code rules.refaster}, which the demo module's
 * {@code refaster} profile applies to its sources.
 *
 * @author hakdogan (hakdogan75@gmail.com)
 * Created on 7.10.2026
 ***/
class StringIsEmpty {

    @BeforeTemplate
    boolean before(String s) {
        return s.length() == 0;
    }

    @AfterTemplate
    boolean after(String s) {
        return s.isEmpty();
    }
}
