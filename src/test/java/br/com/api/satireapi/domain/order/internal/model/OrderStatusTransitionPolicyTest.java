package br.com.api.satireapi.domain.order.internal.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.api.satireapi.domain.order.OrderStatus;
import org.junit.jupiter.api.Test;

class OrderStatusTransitionPolicyTest {

    @Test
    void allowsOnlyForwardOperationalTransitionsAndCancellationBeforeShipping() {
        assertTrue(OrderStatusTransitionPolicy.allows(
            OrderStatus.AGUARDANDO_PAGAMENTO, OrderStatus.PAGO
        ));
        assertTrue(OrderStatusTransitionPolicy.allows(
            OrderStatus.PAGO, OrderStatus.EM_SEPARACAO
        ));
        assertTrue(OrderStatusTransitionPolicy.allows(
            OrderStatus.EM_SEPARACAO, OrderStatus.CANCELADO
        ));
        assertFalse(OrderStatusTransitionPolicy.allows(
            OrderStatus.ENVIADO, OrderStatus.CANCELADO
        ));
    }

    @Test
    void keepsDeliveredAndCancelledOrdersTerminal() {
        assertFalse(OrderStatusTransitionPolicy.allows(
            OrderStatus.ENTREGUE, OrderStatus.CANCELADO
        ));
        assertFalse(OrderStatusTransitionPolicy.allows(
            OrderStatus.CANCELADO, OrderStatus.PAGO
        ));
        assertFalse(OrderStatusTransitionPolicy.allows(
            OrderStatus.PAGO, OrderStatus.PAGO
        ));
    }
}
