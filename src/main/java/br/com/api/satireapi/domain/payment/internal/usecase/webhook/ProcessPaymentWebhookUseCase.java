package br.com.api.satireapi.domain.payment.internal.usecase.webhook;

import br.com.api.satireapi.domain.order.PaymentOrderGateway;
import br.com.api.satireapi.domain.payment.internal.dto.request.PaymentWebhookRequest;
import br.com.api.satireapi.domain.payment.internal.model.PaymentStatus;
import br.com.api.satireapi.domain.payment.internal.persistence.PaymentRepository;
import br.com.api.satireapi.domain.payment.internal.persistence.PaymentWebhookEventRepository;
import br.com.api.satireapi.domain.payment.internal.security.PaymentWebhookVerifier;
import br.com.api.satireapi.domain.payment.internal.usecase.GatewayTransactionConflictException;
import br.com.api.satireapi.domain.payment.internal.usecase.InvalidPaymentWebhookPayloadException;
import br.com.api.satireapi.domain.payment.internal.usecase.PaymentNotFoundException;
import br.com.api.satireapi.domain.payment.internal.usecase.WebhookReplayConflictException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class ProcessPaymentWebhookUseCase {

    private static final Set<PaymentStatus> ALLOWED_STATUSES = Set.of(
        PaymentStatus.APROVADO,
        PaymentStatus.RECUSADO,
        PaymentStatus.CANCELADO
    );

    private final PaymentWebhookVerifier webhookVerifier;
    private final ObjectMapper objectMapper;
    private final PaymentRepository paymentRepository;
    private final PaymentWebhookEventRepository eventRepository;
    private final PaymentOrderGateway orderGateway;

    public ProcessPaymentWebhookUseCase(
        PaymentWebhookVerifier webhookVerifier,
        ObjectMapper objectMapper,
        PaymentRepository paymentRepository,
        PaymentWebhookEventRepository eventRepository,
        PaymentOrderGateway orderGateway
    ) {
        this.webhookVerifier = webhookVerifier;
        this.objectMapper = objectMapper;
        this.paymentRepository = paymentRepository;
        this.eventRepository = eventRepository;
        this.orderGateway = orderGateway;
    }

    @Transactional
    public void execute(
        UUID paymentId,
        String eventId,
        String timestamp,
        String signature,
        String rawPayload
    ) {
        webhookVerifier.verify(eventId, timestamp, signature, rawPayload);
        var request = parse(rawPayload);
        validate(request);

        var payment = paymentRepository.findByIdForUpdate(paymentId)
            .orElseThrow(PaymentNotFoundException::new);
        var payloadHash = sha256(rawPayload);
        var inserted = eventRepository.insertIfAbsent(eventId, paymentId, payloadHash);
        if (inserted == 0) {
            var existing = eventRepository.findById(eventId)
                .orElseThrow(WebhookReplayConflictException::new);
            if (!existing.getPaymentId().equals(paymentId)
                || !existing.getPayloadHash().equals(payloadHash)) {
                throw new WebhookReplayConflictException();
            }
            return;
        }

        var transactionId = request.gatewayTransactionId().trim();
        if (paymentRepository.existsByGatewayTransactionIdAndIdNot(transactionId, paymentId)) {
            throw new GatewayTransactionConflictException();
        }
        payment.applyGatewayUpdate(request.status(), transactionId);
        if (request.status() == PaymentStatus.APROVADO) {
            orderGateway.markPaid(payment.getOrderId());
        }
    }

    private PaymentWebhookRequest parse(String rawPayload) {
        try {
            return objectMapper.readValue(rawPayload, PaymentWebhookRequest.class);
        } catch (JacksonException exception) {
            throw new InvalidPaymentWebhookPayloadException();
        }
    }

    private void validate(PaymentWebhookRequest request) {
        if (request == null || request.status() == null
            || !ALLOWED_STATUSES.contains(request.status())
            || request.gatewayTransactionId() == null
            || request.gatewayTransactionId().isBlank()
            || request.gatewayTransactionId().trim().length() > 255) {
            throw new InvalidPaymentWebhookPayloadException();
        }
    }

    private String sha256(String payload) {
        try {
            var digest = MessageDigest.getInstance("SHA-256")
                .digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", exception);
        }
    }
}
