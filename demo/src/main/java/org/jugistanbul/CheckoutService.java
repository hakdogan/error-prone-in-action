package org.jugistanbul;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

/**
 * Triggers the SensitiveDataLogging check. Both flagged lines compile, pass tests and look harmless
 * in review, yet together they write a national id, an email address and a card number to the logs.
 *
 * <p>The demo build lowers the check to a warning; {@code -Psensitive-data} makes these lines fail
 * the build.
 *
 * @author hakdogan (hakdogan75@gmail.com)
 * Created on 7.10.2026
 ***/
public class CheckoutService {

    private static final Logger LOG = System.getLogger(CheckoutService.class.getName());

    public void checkout(String orderId, Customer customer) {
        // Fine: only the name is logged
        LOG.log(Level.INFO, "Order " + orderId + " placed by " + customer.name());

        // Flagged: Customer's toString() prints nationalId, email and cardNumber
        LOG.log(Level.DEBUG, "Order " + orderId + " placed by " + customer);

        // Flagged: the email address itself
        LOG.log(Level.INFO, "Receipt for order " + orderId + " sent to " + customer.email());
    }
}
