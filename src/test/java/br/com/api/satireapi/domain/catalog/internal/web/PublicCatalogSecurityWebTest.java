package br.com.api.satireapi.domain.catalog.internal.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.api.satireapi.domain.catalog.dto.CategorySummary;
import br.com.api.satireapi.domain.catalog.internal.dto.response.PublicProductDetailsResponse;
import br.com.api.satireapi.domain.catalog.internal.usecase.query.GetPublicCategoryUseCase;
import br.com.api.satireapi.domain.catalog.internal.usecase.query.GetPublicProductUseCase;
import br.com.api.satireapi.domain.catalog.internal.usecase.query.ListPublicCategoriesUseCase;
import br.com.api.satireapi.domain.catalog.internal.usecase.query.ListPublicProductImagesUseCase;
import br.com.api.satireapi.domain.catalog.internal.usecase.query.ListPublicProductsUseCase;
import br.com.api.satireapi.domain.catalog.internal.usecase.query.ListPublicProductVariationsUseCase;
import br.com.api.satireapi.domain.customer.CustomerAuthenticationGateway;
import br.com.api.satireapi.infra.security.SecurityConfig;
import br.com.api.satireapi.infra.security.jwt.JwtAuthenticationFilter;
import br.com.api.satireapi.infra.security.jwt.JwtTokenService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {ProductController.class, CategoryController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenService.class})
@TestPropertySource(properties = {
    "app.jwt.secret=test-secret-key-with-at-least-32-bytes",
    "app.jwt.expiration=3600",
    "app.cors.allowed-origins=https://store.example.com"
})
class PublicCatalogSecurityWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerAuthenticationGateway customerAuthenticationGateway;

    @MockitoBean
    private ListPublicCategoriesUseCase listCategoriesUseCase;

    @MockitoBean
    private GetPublicCategoryUseCase getCategoryUseCase;

    @MockitoBean
    private ListPublicProductsUseCase listProductsUseCase;

    @MockitoBean
    private GetPublicProductUseCase getProductUseCase;

    @MockitoBean
    private ListPublicProductVariationsUseCase listVariationsUseCase;

    @MockitoBean
    private ListPublicProductImagesUseCase listImagesUseCase;

    private UUID categoryId;
    private UUID productId;

    @BeforeEach
    void configureResponses() {
        categoryId = UUID.randomUUID();
        productId = UUID.randomUUID();
        var category = new CategorySummary(categoryId, "Shoes", "shoes");
        var details = new PublicProductDetailsResponse(
            productId, "Satire Shoes", "satire-shoes", "Description", category,
            List.of(), List.of()
        );

        when(listCategoriesUseCase.execute(any())).thenReturn(Page.empty());
        when(getCategoryUseCase.execute(any())).thenReturn(category);
        when(listProductsUseCase.execute(any(), any())).thenReturn(Page.empty());
        when(getProductUseCase.byId(any())).thenReturn(details);
        when(getProductUseCase.bySlug(any())).thenReturn(details);
        when(listVariationsUseCase.execute(any())).thenReturn(List.of());
        when(listImagesUseCase.execute(any())).thenReturn(List.of());
    }

    @Test
    void publicCatalogRoutesDoNotRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/categories")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/categories/" + categoryId)).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/products")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/products/" + productId)).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/products/slug/satire-shoes")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/products/" + productId + "/variations"))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/products/" + productId + "/images"))
            .andExpect(status().isOk());
    }

    @Test
    void invalidPaginationAndSortingAreRejected() throws Exception {
        mockMvc.perform(get("/api/v1/products").param("size", "101"))
            .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/categories").param("sortBy", "active"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void writeRouteRemainsProtected() throws Exception {
        mockMvc.perform(post("/api/v1/products"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void corsAcceptsOnlyConfiguredOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/products")
                .header("Origin", "https://store.example.com")
                .header("Access-Control-Request-Method", "GET"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "https://store.example.com"));

        mockMvc.perform(options("/api/v1/products")
                .header("Origin", "https://attacker.example")
                .header("Access-Control-Request-Method", "GET"))
            .andExpect(status().isForbidden());
    }
}
