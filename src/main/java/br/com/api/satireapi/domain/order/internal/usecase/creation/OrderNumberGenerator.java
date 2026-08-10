package br.com.api.satireapi.domain.order.internal.usecase.creation;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class OrderNumberGenerator {

    public String generate() {
        return "SAT-" + UUID.randomUUID().toString().replace("-", "")
            .substring(0, 20).toUpperCase(java.util.Locale.ROOT);
    }
}
