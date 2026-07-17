package com.minimarket.security.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StrongPasswordValidatorTest {

    private StrongPasswordValidator validator;

    @BeforeEach
    void setUp() {
        validator = new StrongPasswordValidator();
    }

    @Test
    void acceptsPasswordMeetingAllCriteria() {
        assertTrue(validator.isValid("Admin123!", null));
    }

    @Test
    void rejectsPasswordWithoutUppercase() {
        assertFalse(validator.isValid("admin123!", null));
    }

    @Test
    void rejectsPasswordWithoutSpecialCharacter() {
        assertFalse(validator.isValid("Admin1234", null));
    }

    @Test
    void rejectsPasswordShorterThanEightCharacters() {
        assertFalse(validator.isValid("Adm1!", null));
    }
}
