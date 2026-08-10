package br.com.api.satireapi.domain.order.internal.model;

import br.com.api.satireapi.domain.order.OrderStatus;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class OrderStatusTransitionPolicy {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
        OrderStatus.AGUARDANDO_PAGAMENTO,
        EnumSet.of(OrderStatus.PAGO, OrderStatus.CANCELADO),
        OrderStatus.PAGO,
        EnumSet.of(OrderStatus.EM_SEPARACAO, OrderStatus.CANCELADO),
        OrderStatus.EM_SEPARACAO,
        EnumSet.of(OrderStatus.ENVIADO, OrderStatus.CANCELADO),
        OrderStatus.ENVIADO,
        EnumSet.of(OrderStatus.ENTREGUE),
        OrderStatus.ENTREGUE,
        Set.of(),
        OrderStatus.CANCELADO,
        Set.of()
    );

    private OrderStatusTransitionPolicy() {
    }

    public static boolean allows(OrderStatus current, OrderStatus target) {
        return current != null && target != null
            && ALLOWED_TRANSITIONS.getOrDefault(current, Set.of()).contains(target);
    }
}
