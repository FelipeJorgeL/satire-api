package br.com.api.satireapi.infra.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import br.com.api.satireapi.infra.security.jwt.JwtAuthenticationFilter;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class SecurityConfigCorsTest {

    @Test
    void configuresOnlyTheExplicitBrowserOriginsAndRequiredHeaders() {
        var securityConfig = new SecurityConfig(mock(JwtAuthenticationFilter.class));
        var source = securityConfig.corsConfigurationSource(
            "https://store.example.com, https://admin.example.com"
        );
        var configuration = source.getCorsConfiguration(
            new MockHttpServletRequest("GET", "/api/v1/products")
        );

        assertNotNull(configuration);
        assertEquals(
            List.of("https://store.example.com", "https://admin.example.com"),
            configuration.getAllowedOrigins()
        );
        assertEquals(Boolean.TRUE, configuration.getAllowCredentials());
        assertEquals(
            "https://store.example.com",
            configuration.checkOrigin("https://store.example.com")
        );
        assertEquals(null, configuration.checkOrigin("https://attacker.example"));
    }
}
