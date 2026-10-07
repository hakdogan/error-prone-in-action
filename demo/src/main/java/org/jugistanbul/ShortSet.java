package org.jugistanbul;

import java.util.HashSet;
import java.util.Set;

/**
 * The {@code Set<Short>} example from Error Prone's documentation. {@code i - 1} is an {@code int},
 * so {@code s.remove(i - 1)} removes nothing and the program prints 100 instead of 1. Error Prone's
 * CollectionIncompatibleType check fails the build on that line; casting the argument to {@code
 * short} fixes it.
 *
 * @author hakdogan (hakdogan75@gmail.com)
 * Created on 7.10.2026
 ***/
public class ShortSet {

    public static void main(String[] args) {

        Set<Short> s = new HashSet<>();
        for (short i = 0; i < 100; i++) {
            s.add(i);
            s.remove(i - 1);
        }
        System.out.println(s.size());
    }
}
