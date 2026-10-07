package org.jugistanbul;

/**
 * Validates customer input. Built with {@code -Prefaster}, its {@code length() == 0} checks on
 * {@link String} are rewritten to {@code isEmpty()} by the StringIsEmpty Refaster rule.
 *
 * @author hakdogan (hakdogan75@gmail.com)
 * Created on 7.10.2026
 ***/
public class CustomerValidator {

    static boolean isIncomplete(Customer customer) {
        return customer.name().length() == 0 || customer.email().strip().length() == 0;
    }

    // StringBuilder also has length(), but the rule only matches String, so this stays as is
    static boolean isEmptyNote(StringBuilder note) {
        return note.length() == 0;
    }
}
