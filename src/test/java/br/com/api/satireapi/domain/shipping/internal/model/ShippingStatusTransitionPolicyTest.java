package br.com.api.satireapi.domain.shipping.internal.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ShippingStatusTransitionPolicyTest {

    @Test
    void allowsOnlyOperationalTransitions() {
        assertTrue(ShippingStatusTransitionPolicy.allows(
            ShippingStatus.AGUARDANDO_ENVIO, ShippingStatus.ENVIADO
        ));
        assertTrue(ShippingStatusTransitionPolicy.allows(
            ShippingStatus.ENVIADO, ShippingStatus.EM_TRANSITO
        ));
        assertTrue(ShippingStatusTransitionPolicy.allows(
            ShippingStatus.EM_TRANSITO, ShippingStatus.ENTREGUE
        ));
        assertTrue(ShippingStatusTransitionPolicy.allows(
            ShippingStatus.EM_TRANSITO, ShippingStatus.DEVOLVIDO
        ));
    }

    @Test
    void rejectsBackwardAndTerminalTransitions() {
        assertFalse(ShippingStatusTransitionPolicy.allows(
            ShippingStatus.EM_TRANSITO, ShippingStatus.AGUARDANDO_ENVIO
        ));
        assertFalse(ShippingStatusTransitionPolicy.allows(
            ShippingStatus.ENTREGUE, ShippingStatus.DEVOLVIDO
        ));
        assertFalse(ShippingStatusTransitionPolicy.allows(
            ShippingStatus.DEVOLVIDO, ShippingStatus.ENVIADO
        ));
    }
}
