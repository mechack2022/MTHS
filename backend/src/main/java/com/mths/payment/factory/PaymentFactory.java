package com.mths.payment.factory;

import com.mths.consultation.dto.CreateAppointmentRequest;
import com.mths.payment.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Factory Pattern: Creates Payment entities with proper defaults and validation
 * Encapsulates the complex logic of payment creation
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentFactory {

    @Value("${payment.consultation-fee}")
    private BigDecimal consultationFee;

    @Value("${payment.currency}")
    private String currency;

    /**
     * Create a payment from appointment request
     * Applies business rules and defaults
     *
     * @param request The appointment request
     * @param patientEmail Patient's email address
     * @return Configured payment entity
     */
    public Payment createPaymentFromAppointmentRequest(
            CreateAppointmentRequest request,
            String patientEmail) {

        Payment payment = new Payment();

        // Set basic payment information
        payment.setReference(generatePaymentReference(request.getPatientProfileId()));
        payment.setPatientProfileId(request.getPatientProfileId());
        payment.setDoctorProfileId(request.getDoctorProfileId());
        payment.setPatientEmail(patientEmail);
        payment.setAmount(determinePaymentAmount(request));

        // Set initial status
        payment.setStatus(Payment.PaymentStatus.PENDING);

        // Set payment provider
        payment.setPaymentProvider("PAYSTACK");

        // Store appointment details (for later creation)
        payment.setScheduledDatetime(request.getScheduledDatetime());
        payment.setAppointmentType(request.getAppointmentType().name());
        payment.setDurationMinutes(request.getDurationMinutes());

        log.info("Created payment entity - Reference: {}, Amount: {} {}, Patient: {}, Doctor: {}",
                payment.getReference(), payment.getAmount(), currency,
                payment.getPatientProfileId(), payment.getDoctorProfileId());

        return payment;
    }

    /**
     * Generate unique payment reference
     * Format: MTHS-APT-{patientId}-{timestamp}
     */
    private String generatePaymentReference(Long patientId) {
        return String.format("MTHS-APT-%d-%d",
                patientId,
                System.currentTimeMillis());
    }

    /**
     * Determine payment amount based on request
     * Currently uses flat fee, but can be extended for dynamic pricing
     */
    private BigDecimal determinePaymentAmount(CreateAppointmentRequest request) {
        // For now, use flat fee
        // Future: Can implement dynamic pricing based on:
        // - Doctor's specialization
        // - Appointment type (VIRTUAL vs IN_PERSON)
        // - Duration
        // - Time of day
        // - Urgency

        BigDecimal amount = request.getConsultationFee() != null
                ? BigDecimal.valueOf(request.getConsultationFee())
                : consultationFee;

        log.debug("Determined payment amount: {} {} for appointment type: {}",
                amount, currency, request.getAppointmentType());

        return amount;
    }

    /**
     * Create appointment request from payment (after payment is completed)
     * Reconstructs the original appointment request from stored payment data
     */
    public CreateAppointmentRequest createAppointmentRequestFromPayment(Payment payment) {
        CreateAppointmentRequest request = new CreateAppointmentRequest();

        request.setPatientProfileId(payment.getPatientProfileId());
        request.setDoctorProfileId(payment.getDoctorProfileId());
        request.setScheduledDatetime(payment.getScheduledDatetime());
        request.setAppointmentType(
                com.mths.consultation.entity.Appointment.AppointmentType.valueOf(payment.getAppointmentType())
        );
        request.setDurationMinutes(payment.getDurationMinutes());
        request.setConsultationFee(payment.getAmount().doubleValue());

        log.info("Reconstructed appointment request from payment reference: {}", payment.getReference());

        return request;
    }

    /**
     * Update payment with authorization URL from provider
     */
    public void setAuthorizationUrl(Payment payment, String authorizationUrl) {
        payment.setAuthorizationUrl(authorizationUrl);
        log.debug("Set authorization URL for payment: {}", payment.getReference());
    }

    /**
     * Mark payment as completed
     */
    public void markPaymentAsCompleted(Payment payment) {
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setPaidAt(LocalDateTime.now());
        payment.setVerifiedAt(LocalDateTime.now());

        log.info("Marked payment as completed - Reference: {}", payment.getReference());
    }

    /**
     * Mark payment as failed
     */
    public void markPaymentAsFailed(Payment payment, String reason) {
        payment.setStatus(Payment.PaymentStatus.FAILED);
        payment.setFailedReason(reason);

        log.warn("Marked payment as failed - Reference: {}, Reason: {}",
                payment.getReference(), reason);
    }

    /**
     * Link payment to appointment after appointment creation
     */
    public void linkPaymentToAppointment(Payment payment, Long appointmentId) {
        payment.setAppointmentId(appointmentId);
        log.info("Linked payment {} to appointment {}", payment.getReference(), appointmentId);
    }
}
