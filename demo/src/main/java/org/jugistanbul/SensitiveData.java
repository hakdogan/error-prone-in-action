package org.jugistanbul;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks data that must never reach the logs, such as national ids, card numbers or email
 * addresses. Enforced at compile time by the SensitiveDataLogging check.
 *
 * @author hakdogan (hakdogan75@gmail.com)
 * Created on 7.10.2026
 ***/
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.RECORD_COMPONENT})
public @interface SensitiveData {}
