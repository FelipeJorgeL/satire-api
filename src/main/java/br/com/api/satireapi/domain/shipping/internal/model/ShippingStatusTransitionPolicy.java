package br.com.api.satireapi.domain.shipping.internal.model;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class ShippingStatusTransitionPolicy {

    private static final Map<ShippingStatus, Set<ShippingStatus>> ALLOWED_TRANSITIONS = Map.of(
        ShippingStatus.AGUARDANDO_ENVIO,
        EnumSet.of(ShippingStatus.ENVIADO),
        ShippingStatus.ENVIADO,
        EnumSet.of(ShippingStatus.EM_TRANSITO, ShippingStatus.DEVOLVIDO),
        ShippingStatus.EM_TRANSITO,
        EnumSet.of(ShippingStatus.ENTREGUE, ShippingStatus.DEVOLVIDO),
        ShippingStatus.ENTREGUE,
        Set.of(),
        ShippingStatus.DEVOLVIDO,
        Set.of()
    );

    private ShippingStatusTransitionPolicy() {
    }

    public static boolean allows(ShippingStatus current, ShippingStatus target) {
        return current != null
            && target != null
            && ALLOWED_TRANSITIONS.getOrDefault(current, Set.of()).contains(target);
    }
}
