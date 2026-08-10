package br.com.api.satireapi.domain.cart.internal.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.api.satireapi.domain.cart.internal.dto.response.CartResponse;
import br.com.api.satireapi.domain.cart.internal.usecase.item.AddCartItemUseCase;
import br.com.api.satireapi.domain.cart.internal.usecase.item.ClearCartUseCase;
import br.com.api.satireapi.domain.cart.internal.usecase.item.RemoveCartItemUseCase;
import br.com.api.satireapi.domain.cart.internal.usecase.item.UpdateCartItemUseCase;
import br.com.api.satireapi.domain.cart.internal.usecase.query.GetCartUseCase;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = CustomerCartController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenService.class})
@TestPropertySource(properties = {
    "app.jwt.secret=test-secret-key-with-at-least-32-bytes",
    "app.jwt.expiration=3600"
})
class CustomerCartSecurityWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private CustomerAuthenticationGateway customerAuthenticationGateway;

    @MockitoBean
    private GetCartUseCase getCartUseCase;

    @MockitoBean
    private AddCartItemUseCase addCartItemUseCase;

    @MockitoBean
    private UpdateCartItemUseCase updateCartItemUseCase;

    @MockitoBean
    private RemoveCartItemUseCase removeCartItemUseCase;

    @MockitoBean
    private ClearCartUseCase clearCartUseCase;

    @Test
    void cartRoutesRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/me/cart"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void customerCanReadAndAddToOwnCart() throws Exception {
        var customerId = UUID.randomUUID();
        when(getCartUseCase.execute(customerId)).thenReturn(CartResponse.empty(customerId));
        when(addCartItemUseCase.execute(any(), any())).thenReturn(CartResponse.empty(customerId));

        mockMvc.perform(get("/api/v1/me/cart")
                .header("Authorization", bearer(customerId, "CLIENTE")))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/me/cart/items")
                .header("Authorization", bearer(customerId, "CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"variationId\":\"" + UUID.randomUUID()
                    + "\",\"quantity\":2}"))
            .andExpect(status().isCreated());
    }

    @Test
    void invalidCartQuantityIsRejected() throws Exception {
        var customerId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/me/cart/items")
                .header("Authorization", bearer(customerId, "CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"variationId\":\"" + UUID.randomUUID()
                    + "\",\"quantity\":0}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void customerCanUpdateAndRemoveCartItem() throws Exception {
        var customerId = UUID.randomUUID();
        var itemId = UUID.randomUUID();
        when(updateCartItemUseCase.execute(any(), any(), any())).thenReturn(CartResponse.empty(customerId));

        mockMvc.perform(patch("/api/v1/me/cart/items/" + itemId)
                .header("Authorization", bearer(customerId, "CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":3}"))
            .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/me/cart/items/" + itemId)
                .header("Authorization", bearer(customerId, "CLIENTE")))
            .andExpect(status().isNoContent());
    }

    @Test
    void invalidCartItemQuantityIsRejected() throws Exception {
        var customerId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/me/cart/items/" + UUID.randomUUID())
                .header("Authorization", bearer(customerId, "CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":0}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void customerCanClearCart() throws Exception {
        var customerId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/me/cart")
                .header("Authorization", bearer(customerId, "CLIENTE")))
            .andExpect(status().isNoContent());
    }

    private String bearer(UUID customerId, String profile) {
        when(customerAuthenticationGateway.findById(customerId))
            .thenReturn(Optional.of(new CustomerAuthentication(customerId, true, Set.of(profile))));
        return "Bearer " + jwtTokenService.generate(
            customerId.toString(), "customer@example.com", List.of(profile)
        );
    }
}
