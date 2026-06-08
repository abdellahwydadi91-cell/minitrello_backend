package org.minitrello.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilsTest {

    private final JwtUtils jwtUtils = new JwtUtils();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "mySecretKeyForJWTTokenGenerationAndValidation12345678901234567890");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 86_400_000);
    }

    @Test
    void genereValideEtLitLeSujetDuToken() {
        User user = new User("user@example.com", "password", Collections.emptyList());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        String token = jwtUtils.generateJwtToken(authentication);

        assertThat(jwtUtils.validateJwtToken(token)).isTrue();
        assertThat(jwtUtils.getUserNameFromJwtToken(token)).isEqualTo("user@example.com");
    }

    @Test
    void tokenInvalideRetourneFaux() {
        assertThat(jwtUtils.validateJwtToken("not-a-token")).isFalse();
    }
}
