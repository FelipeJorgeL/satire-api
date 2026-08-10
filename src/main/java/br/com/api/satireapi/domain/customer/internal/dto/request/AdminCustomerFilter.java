package br.com.api.satireapi.domain.customer.internal.dto.request;

import java.util.Locale;

public record AdminCustomerFilter(String search, Boolean active, String profile) {

    public AdminCustomerFilter {
        search = normalizeSearch(search);
        profile = profile == null ? null : profile.toUpperCase(Locale.ROOT);
    }

    private static String normalizeSearch(String search) {
        return search == null || search.isBlank() ? null : search.trim().toLowerCase(Locale.ROOT);
    }
}
