package com.mths.payment.webhook;

import com.mths.payment.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Processes successful payment webhooks (charge.success)
 * Creates appointment after payment is verified
 */
@Slf4j
@Component
public class ChargeSuccessProcessor extends AbstractWebhookEventProcessor {

    private final PaymentService paymentService;

    public ChargeSuccessProcessor(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public String getEventType() {
        return "charge.success";
    }

    @Override
    protected void processEventData(Map<String, Object> data) {
        String reference = extractValue(data, "reference");
        String status = extractValue(data, "status");
        Number amount = extractValue(data, "amount");

        log.info("Processing successful charge - Reference: {}, Status: {}, Amount: {}",
                reference, status, amount);

        if (reference == null) {
            log.error("No reference found in charge.success webhook");
            return;
        }

        // Delegate to payment service to handle the payment success
        // This will:
        // 1. Mark payment as completed
        // 2. Create the appointment
        // 3. Link payment to appointment
        paymentService.handlePaymentSuccess(reference);

        log.info("Charge success processed - Appointment created for reference: {}", reference);
    }
}
