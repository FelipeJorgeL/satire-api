package br.com.api.satireapi.domain.catalog.internal.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.api.satireapi.domain.customer.CustomerAuthentication;
import br.com.api.satireapi.domain.customer.CustomerAuthenticationGateway;
import br.com.api.satireapi.domain.catalog.internal.dto.response.AdminCategoryResponse;
import br.com.api.satireapi.domain.catalog.internal.dto.response.AdminProductDetailsResponse;
import br.com.api.satireapi.domain.catalog.internal.usecase.category.CreateCategoryUseCase;
import br.com.api.satireapi.domain.catalog.internal.usecase.category.DeactivateCategoryUseCase;
import br.com.api.satireapi.domain.catalog.internal.usecase.category.UpdateCategoryUseCase;
import br.com.api.satireapi.domain.catalog.internal.usecase.product.ChangeProductStatusUseCase;
import br.com.api.satireapi.domain.catalog.internal.usecase.product.CreateProductUseCase;
import br.com.api.satireapi.domain.catalog.internal.usecase.product.DeleteProductImageUseCase;
import br.com.api.satireapi.domain.catalog.internal.usecase.product.ListAdminProductsUseCase;
import br.com.api.satireapi.domain.catalog.internal.usecase.product.ReplaceProductUseCase;
import br.com.api.satireapi.infra.security.SecurityConfig;
import br.com.api.satireapi.infra.security.jwt.JwtAuthenticationFilter;
import br.com.api.satireapi.infra.security.jwt.JwtTokenService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {AdminProductController.class, AdminCategoryController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenService.class})
@TestPropertySource(properties = {
    "app.jwt.secret=test-secret-key-with-at-least-32-bytes",
    "app.jwt.expiration=3600"
})
class AdminCatalogSecurityWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private CustomerAuthenticationGateway customerAuthenticationGateway;

    @MockitoBean
    private ListAdminProductsUseCase listAdminProductsUseCase;

    @MockitoBean
    private CreateProductUseCase createProductUseCase;

    @MockitoBean
    private ReplaceProductUseCase replaceProductUseCase;

    @MockitoBean
    private ChangeProductStatusUseCase changeProductStatusUseCase;

    @MockitoBean
    private DeleteProductImageUseCase deleteProductImageUseCase;

    @MockitoBean
    private CreateCategoryUseCase createCategoryUseCase;

    @MockitoBean
    private UpdateCategoryUseCase updateCategoryUseCase;

    @MockitoBean
    private DeactivateCategoryUseCase deactivateCategoryUseCase;

    @Test
    void catalogAdminRoutesRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/admin/products"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void customerProfileCannotAccessCatalogAdminRoutes() throws Exception {
        var customerId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/admin/products")
                .header("Authorization", bearer(customerId, "CLIENTE")))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCanListProducts() throws Exception {
        var adminId = UUID.randomUUID();
        when(listAdminProductsUseCase.execute(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/admin/products")
                .header("Authorization", bearer(adminId, "ADMIN")))
            .andExpect(status().isOk());
    }

    @Test
    void adminCanCreateProductWithImageUrls() throws Exception {
        var adminId = UUID.randomUUID();
        when(createProductUseCase.execute(any())).thenReturn(productResponse());

        mockMvc.perform(post("/api/v1/admin/products")
                .header("Authorization", bearer(adminId, "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson()))
            .andExpect(status().isCreated());
    }

    @Test
    void productWithInvalidImageMetadataIsRejected() throws Exception {
        var adminId = UUID.randomUUID();
        var body = productJson().replace("\"altText\":\"Tênis preto\"", "\"altText\":null");

        mockMvc.perform(post("/api/v1/admin/products")
                .header("Authorization", bearer(adminId, "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }

    @Test
    void productWithoutSinglePrimaryImageIsRejected() throws Exception {
        var adminId = UUID.randomUUID();
        var body = productJson().replace("\"primary\":true", "\"primary\":false");

        mockMvc.perform(post("/api/v1/admin/products")
                .header("Authorization", bearer(adminId, "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }

    @Test
    void adminCanReplaceProduct() throws Exception {
        var adminId = UUID.randomUUID();
        when(replaceProductUseCase.execute(any(), any())).thenReturn(productResponse());

        mockMvc.perform(put("/api/v1/admin/products/" + UUID.randomUUID())
                .header("Authorization", bearer(adminId, "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson()))
            .andExpect(status().isOk());
    }

    @Test
    void invalidProductStatusPayloadIsRejected() throws Exception {
        var adminId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/admin/products/" + UUID.randomUUID() + "/status")
                .header("Authorization", bearer(adminId, "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void adminCanChangeProductStatusAndDeleteImage() throws Exception {
        var adminId = UUID.randomUUID();
        var productId = UUID.randomUUID();
        var imageId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/admin/products/" + productId + "/status")
                .header("Authorization", bearer(adminId, "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"active\":false}"))
            .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/v1/admin/products/" + productId + "/images/" + imageId)
                .header("Authorization", bearer(adminId, "ADMIN")))
            .andExpect(status().isNoContent());
    }

    @Test
    void adminCanDeactivateProduct() throws Exception {
        var adminId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/admin/products/" + UUID.randomUUID())
                .header("Authorization", bearer(adminId, "ADMIN")))
            .andExpect(status().isNoContent());
    }

    @Test
    void adminCanCreateAndDeactivateCategory() throws Exception {
        var adminId = UUID.randomUUID();
        when(createCategoryUseCase.execute(any()))
            .thenReturn(new AdminCategoryResponse(UUID.randomUUID(), "Tênis", "tenis", true));

        mockMvc.perform(post("/api/v1/admin/categories")
                .header("Authorization", bearer(adminId, "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Tênis\",\"slug\":\"tenis\"}"))
            .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/v1/admin/categories/" + UUID.randomUUID())
                .header("Authorization", bearer(adminId, "ADMIN")))
            .andExpect(status().isNoContent());
    }

    @Test
    void adminCanUpdateCategory() throws Exception {
        var adminId = UUID.randomUUID();
        when(updateCategoryUseCase.execute(any(), any()))
            .thenReturn(new AdminCategoryResponse(UUID.randomUUID(), "Tenis", "tenis", true));

        mockMvc.perform(patch("/api/v1/admin/categories/" + UUID.randomUUID())
                .header("Authorization", bearer(adminId, "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Tenis\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void emptyCategoryUpdateIsRejected() throws Exception {
        var adminId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/admin/categories/" + UUID.randomUUID())
                .header("Authorization", bearer(adminId, "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    private String bearer(UUID customerId, String profile) {
        when(customerAuthenticationGateway.findById(customerId))
            .thenReturn(Optional.of(new CustomerAuthentication(customerId, true, Set.of(profile))));
        return "Bearer " + jwtTokenService.generate(
            customerId.toString(), "admin@example.com", List.of(profile)
        );
    }

    private String productJson() {
        return """
            {
              "categoryId":"%s",
              "name":"Tênis Satire",
              "slug":"tenis-satire",
              "description":"Tênis casual",
              "variations":[{"sku":"TENIS-42","name":"Preto 42","price":299.90,"stock":10}],
              "images":[{"url":"https://cdn.example.com/tenis.jpg","altText":"Tênis preto","decorative":false,"primary":true,"displayOrder":1}]
            }
            """.formatted(UUID.randomUUID());
    }

    private AdminProductDetailsResponse productResponse() {
        return new AdminProductDetailsResponse(
            UUID.randomUUID(), UUID.randomUUID(), "Tênis Satire", "tenis-satire", "Tênis casual", true,
            OffsetDateTime.now(), List.of(), List.of()
        );
    }
}
