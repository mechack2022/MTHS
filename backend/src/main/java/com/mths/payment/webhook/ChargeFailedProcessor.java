package com.mths.payment.webhook;

import com.mths.payment.entity.Payment;
import com.mths.payment.factory.PaymentFactory;
import com.mths.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Processes failed payment webhooks (charge.failed)
 * Marks payment as failed in the database
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChargeFailedProcessor extends AbstractWebhookEventProcessor {

    private final PaymentRepository paymentRepository;
    private final PaymentFactory paymentFactory;

    @Override
    public String getEventType() {
        return "charge.failed";
    }

    @Override
    protected void processEventData(Map<String, Object> data) {
        String reference = extractValue(data, "reference");
        String gatewayResponse = extractValue(data, "gateway_response");
        String message = extractValue(data, "message");

        log.warn("Processing failed charge - Reference: {}, Reason: {}, Message: {}",
                reference, gatewayResponse, message);

        if (reference == null) {
            log.error("No reference found in charge.failed webhook");
            return;
        }

        // Find payment and mark as failed
        Optional<Payment> paymentOpt = paymentRepository.findByReference(reference);

        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            String failureReason = String.format("%s - %s", gatewayResponse, message);

            paymentFactory.markPaymentAsFailed(payment, failureReason);
            paymentRepository.save(payment);

            log.info("Payment marked as failed - Reference: {}", reference);
        } else {
            log.error("Payment not found for reference: {}", reference);
        }
    }
}
