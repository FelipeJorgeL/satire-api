package br.com.api.satireapi.domain.catalog.internal.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.api.satireapi.domain.catalog.internal.usecase.favorite.AddFavoriteUseCase;
import br.com.api.satireapi.domain.catalog.internal.usecase.favorite.ListFavoritesUseCase;
import br.com.api.satireapi.domain.catalog.internal.usecase.favorite.RemoveFavoriteUseCase;
import br.com.api.satireapi.domain.customer.CustomerAuthentication;
import br.com.api.satireapi.domain.customer.CustomerAuthenticationGateway;
import br.com.api.satireapi.infra.security.SecurityConfig;
import br.com.api.satireapi.infra.security.jwt.JwtAuthenticationFilter;
import br.com.api.satireapi.infra.security.jwt.JwtTokenService;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = FavoriteController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenService.class})
@TestPropertySource(properties = {
    "app.jwt.secret=test-secret-key-with-at-least-32-bytes",
    "app.jwt.expiration=3600"
})
class FavoriteSecurityWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private CustomerAuthenticationGateway customerAuthenticationGateway;

    @MockitoBean
    private ListFavoritesUseCase listFavoritesUseCase;

    @MockitoBean
    private AddFavoriteUseCase addFavoriteUseCase;

    @MockitoBean
    private RemoveFavoriteUseCase removeFavoriteUseCase;

    @Test
    void favoritesRequireAuthentication() throws Exception {
        var productId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/me/favorites")).andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/v1/me/favorites/" + productId))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/v1/me/favorites/" + productId))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void customerIdentityComesFromBearerToken() throws Exception {
        var customerId = UUID.randomUUID();
        var productId = UUID.randomUUID();
        when(listFavoritesUseCase.execute(any(), any())).thenReturn(Page.empty());
        var authorization = bearer(customerId);

        mockMvc.perform(get("/api/v1/me/favorites").header("Authorization", authorization))
            .andExpect(status().isOk());
        mockMvc.perform(put("/api/v1/me/favorites/" + productId)
                .header("Authorization", authorization))
            .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/v1/me/favorites/" + productId)
                .header("Authorization", authorization))
            .andExpect(status().isNoContent());

        verify(addFavoriteUseCase).execute(customerId, productId);
        verify(removeFavoriteUseCase).execute(customerId, productId);
    }

    @Test
    void invalidPageSizeIsRejected() throws Exception {
        var customerId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/me/favorites")
                .header("Authorization", bearer(customerId))
                .param("size", "101"))
            .andExpect(status().isBadRequest());
    }

    private String bearer(UUID customerId) {
        when(customerAuthenticationGateway.findById(customerId)).thenReturn(Optional.of(
            new CustomerAuthentication(customerId, true, Set.of("CLIENTE"))
        ));
        return "Bearer " + jwtTokenService.generate(
            customerId.toString(), "customer@example.com", List.of("CLIENTE")
        );
    }
}
