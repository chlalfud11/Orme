package com.orme.app.ui.login;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class LoginCredentialsTest {
    @Test
    public void allowsOnlyDefaultAdminCredentials() {
        assertTrue(LoginScreen.isAdminLogin("admin", "admin"));
        assertFalse(LoginScreen.isAdminLogin("admin", "wrong"));
        assertFalse(LoginScreen.isAdminLogin("guest", "admin"));
    }
}
