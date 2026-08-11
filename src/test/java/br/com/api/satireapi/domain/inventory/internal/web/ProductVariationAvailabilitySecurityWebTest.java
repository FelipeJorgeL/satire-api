package br.com.api.satireapi.domain.inventory.internal.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.api.satireapi.domain.customer.CustomerAuthenticationGateway;
import br.com.api.satireapi.domain.inventory.internal.dto.response.ProductVariationAvailabilityResponse;
import br.com.api.satireapi.domain.inventory.internal.usecase.variation.GetProductVariationAvailabilityUseCase;
import br.com.api.satireapi.infra.security.SecurityConfig;
import br.com.api.satireapi.infra.security.jwt.JwtAuthenticationFilter;
import br.com.api.satireapi.infra.security.jwt.JwtTokenService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ProductVariationAvailabilityController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenService.class})
@TestPropertySource(properties = {
    "app.jwt.secret=test-secret-key-with-at-least-32-bytes",
    "app.jwt.expiration=3600"
})
class ProductVariationAvailabilitySecurityWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerAuthenticationGateway customerAuthenticationGateway;

    @MockitoBean
    private GetProductVariationAvailabilityUseCase availabilityUseCase;

    @Test
    void availabilityIsPublicButWriteMethodIsProtected() throws Exception {
        var productId = UUID.randomUUID();
        var variationId = UUID.randomUUID();
        when(availabilityUseCase.execute(any(), any())).thenReturn(
            new ProductVariationAvailabilityResponse(productId, variationId, true, 3)
        );
        var path = "/api/v1/products/" + productId
            + "/variations/" + variationId + "/availability";

        mockMvc.perform(get(path)).andExpect(status().isOk());
        mockMvc.perform(post(path)).andExpect(status().isUnauthorized());
    }
}
