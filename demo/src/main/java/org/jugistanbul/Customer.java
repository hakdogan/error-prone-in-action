package org.jugistanbul;

/**
 * A customer; its record-generated {@code toString()} prints every component, sensitive or not.
 * The {@link SensitiveData} components are what the SensitiveDataLogging check guards.
 *
 * @author hakdogan (hakdogan75@gmail.com)
 * Created on 7.10.2026
 ***/
public record Customer(
        String name,
        @SensitiveData String nationalId,
        @SensitiveData String email,
        @SensitiveData String cardNumber) {}
