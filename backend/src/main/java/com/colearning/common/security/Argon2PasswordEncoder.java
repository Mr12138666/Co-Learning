package com.colearning.common.security;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.springframework.stereotype.Component;

/**
 * Password encoder using Argon2id (winner of the Password Hashing Competition 2015).
 * Parameters: memory=65536 (64MB), iterations=3, parallelism=4.
 */
@Component
public class Argon2PasswordEncoder {

    private final Argon2 argon2;

    public Argon2PasswordEncoder() {
        this.argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
    }

    public String encode(String rawPassword) {
        return argon2.hash(3, 65536, 4, rawPassword.toCharArray());
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isBlank()) {
            return false;
        }
        try {
            return argon2.verify(encodedPassword, rawPassword.toCharArray());
        } catch (Exception e) {
            return false;
        }
    }
}
