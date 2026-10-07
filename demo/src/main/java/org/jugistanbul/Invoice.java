package org.jugistanbul;

import java.math.BigDecimal;

/**
 * Triggers the BigDecimalDoubleConstructor and NoSystemOut checks from the custom-checks module.
 *
 * @author hakdogan (hakdogan75@gmail.com)
 * Created on 7.10.2026
 ***/
public class Invoice {

    public static void main(String[] args) {

        double unitPrice = 0.1;
        // BigDecimalDoubleConstructor: total is 1.00000000000000005551115123125782702..., not 1.0
        BigDecimal total = new BigDecimal(unitPrice).multiply(BigDecimal.TEN);
        // NoSystemOut
        System.out.println(total);
    }
}
