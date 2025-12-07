package com.mths.payment.service;

import com.mths.consultation.dto.AppointmentDTO;
import com.mths.consultation.dto.CreateAppointmentRequest;
import com.mths.consultation.service.AppointmentService;
import com.mths.payment.dto.PaymentInitializationResponse;
import com.mths.payment.dto.PaymentVerificationResponse;
import com.mths.payment.entity.Payment;
import com.mths.payment.exception.PaymentException;
import com.mths.payment.factory.PaymentFactory;
import com.mths.payment.provider.PaymentProvider;
import com.mths.payment.repository.PaymentRepository;
import com.mths.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Payment Service Implementation following SOLID principles:
 * - Single Responsibility: Handles only payment operations
 * - Open/Closed: Open for extension via PaymentProvider interface
 * - Liskov Substitution: Any PaymentProvider implementation works
 * - Interface Segregation: Depends only on needed interfaces
 * - Dependency Inversion: Depends on abstractions (PaymentProvider), not concretions
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor // Constructor injection (best practice)
public class PaymentServiceImpl implements PaymentService {

    // Dependencies injected via constructor (Dependency Injection)
    private final PaymentRepository paymentRepository;
    private final PaymentFactory paymentFactory;
    private final PaymentProvider paymentProvider; // Depends on abstraction!
    private final AppointmentService appointmentService;

    @Override
    public PaymentInitializationResponse initiateAppointmentPayment(
            CreateAppointmentRequest request,
            String patientEmail) {

        log.info("Initiating payment for appointment - Patient: {}, Doctor: {}, Time: {}",
                request.getPatientProfileId(),
                request.getDoctorProfileId(),
                request.getScheduledDatetime());

        // STEP 1: Check if doctor has COMPLETED payment for this time slot
        // (PENDING payments are allowed since they might never complete)
        java.time.LocalDateTime endTime = request.getScheduledDatetime()
                .plusMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 30);

        if (!isDoctorAvailableForPayment(
                request.getDoctorProfileId(),
                request.getScheduledDatetime(),
                endTime)) {
            log.warn("Payment initiation rejected - Time slot already booked (COMPLETED payment exists) - Doctor: {}, Time: {}",
                    request.getDoctorProfileId(), request.getScheduledDatetime());
            throw new PaymentException("This time slot is no longer available. Please choose another time.");
        }

        log.info("Time slot available - Proceeding with payment initialization");

        // Use factory to create payment with proper defaults
        Payment payment = paymentFactory.createPaymentFromAppointmentRequest(request, patientEmail);

        // Save payment in PENDING state
        Payment savedPayment = paymentRepository.save(payment);

        // Build metadata for payment provider
        Map<String, Object> metadata = buildPaymentMetadata(savedPayment);

        // Use strategy pattern - delegate to payment provider
        PaymentInitializationResponse response = paymentProvider.initializePayment(savedPayment, metadata);

        // Update payment with authorization URL
        paymentFactory.setAuthorizationUrl(savedPayment, response.getAuthorizationUrl());
        paymentRepository.save(savedPayment);

        log.info("Payment initialized successfully - Reference: {}, URL: {}",
                savedPayment.getReference(), response.getAuthorizationUrl());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Payment verifyPayment(String reference) {
        log.info("Verifying payment - Reference: {}", reference);
        // Find payment in database
        Payment payment = getPaymentByReference(reference);
        // If already completed, return early
        if (payment.isCompleted()) {
            log.info("Payment already verified - Reference: {}", reference);
            return payment;
        }
        // Verify with payment provider
        boolean isVerified = paymentProvider.verifyPayment(reference);

        if (isVerified) {
            paymentFactory.markPaymentAsCompleted(payment);
            paymentRepository.save(payment);

            log.info("Payment verified successfully - Reference: {}", reference);
        } else {
            paymentFactory.markPaymentAsFailed(payment, "Payment verification failed");
            paymentRepository.save(payment);

            log.warn("Payment verification failed - Reference: {}", reference);
        }

        return payment;
    }

    @Override
    public void handlePaymentSuccess(String reference) {
        log.info("Handling successful payment webhook - Reference: {}", reference);

        // Find and verify payment
        Payment payment = verifyPayment(reference);

        if (!payment.isCompleted()) {
            log.error("Payment not completed after webhook - Reference: {}", reference);
            throw new PaymentException("Payment verification failed despite success webhook");
        }

        // Check if appointment already created
        if (payment.getAppointmentId() != null) {
            log.info("Appointment already exists for payment - Reference: {}, AppointmentId: {}",
                    reference, payment.getAppointmentId());
            return;
        }

        // Create appointment from payment
        createAppointmentFromPayment(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getPaymentByReference(String reference) {
        return paymentRepository.findByReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "reference", reference));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPatientPaidForBooking(Long patientProfileId, Long doctorProfileId, String scheduledDatetime) {
        // Use Java 8 Streams for collection processing
        List<Payment> completedPayments = paymentRepository.findByPatientProfileIdAndStatus(
                patientProfileId,
                Payment.PaymentStatus.COMPLETED
        );

        return completedPayments.stream()
                .anyMatch(payment ->
                        payment.getDoctorProfileId().equals(doctorProfileId) &&
                        payment.getScheduledDatetime().toString().equals(scheduledDatetime) &&
                        payment.getAppointmentId() == null // Not yet linked to appointment
                );
    }

    /**
     * Get payment verification details (for frontend)
     */
    @Transactional(readOnly = true)
    public PaymentVerificationResponse getPaymentVerificationDetails(String reference) {
        Payment payment = getPaymentByReference(reference);

        switch (payment.getStatus()) {
            case COMPLETED:
                String appointmentStatus = payment.getAppointmentId() != null
                        ? "PENDING_CONFIRMATION" // Appointment created, waiting for doctor
                        : "CREATING"; // Payment done, appointment being created

                return PaymentVerificationResponse.success(
                        reference,
                        payment.getAppointmentId(),
                        appointmentStatus
                );

            case PENDING:
            case PROCESSING:
                return PaymentVerificationResponse.pending(reference);

            case FAILED:
                return PaymentVerificationResponse.failed(reference, payment.getFailedReason());

            default:
                throw new PaymentException("Unknown payment status: " + payment.getStatus());
        }
    }

    // ========================================================================
    // PRIVATE HELPER METHODS (Encapsulation)
    // ========================================================================

    /**
     * Create appointment from completed payment
     * This is the critical step that happens AFTER payment success
     */
    private void createAppointmentFromPayment(Payment payment) {
        log.info("Creating appointment from payment - Reference: {}", payment.getReference());

        try {
            // Reconstruct appointment request from payment data
            CreateAppointmentRequest request = paymentFactory.createAppointmentRequestFromPayment(payment);

            // Create appointment via appointment service
            AppointmentDTO appointment = appointmentService.createAppointment(request);

            // Link payment to appointment
            paymentFactory.linkPaymentToAppointment(payment, appointment.getId());
            paymentRepository.save(payment);

            log.info("Appointment created successfully - PaymentRef: {}, AppointmentId: {}",
                    payment.getReference(), appointment.getId());

        } catch (Exception e) {
            log.error("Failed to create appointment from payment - Reference: {}, Error: {}",
                    payment.getReference(), e.getMessage(), e);

            throw new PaymentException(
                    "Payment completed but appointment creation failed. Contact support with reference: " +
                    payment.getReference(), e);
        }
    }

    /**
     * Build metadata for payment provider
     * Uses HashMap (part of Java Collections Framework)
     */
    private Map<String, Object> buildPaymentMetadata(Payment payment) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("payment_id", payment.getId());
        metadata.put("patient_profile_id", payment.getPatientProfileId());
        metadata.put("doctor_profile_id", payment.getDoctorProfileId());
        metadata.put("appointment_type", payment.getAppointmentType());

        return metadata;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDoctorAvailableForPayment(Long doctorProfileId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        log.debug("Checking doctor availability for payment - Doctor: {}, Start: {}, End: {}",
                doctorProfileId, startTime, endTime);

        // Check if there are any COMPLETED payments for this time slot
        // PENDING payments are ignored since they might never be completed
        List<Payment> completedPayments = paymentRepository.findCompletedPaymentsInTimeRange(
                doctorProfileId,
                startTime,
                endTime,
                Payment.PaymentStatus.COMPLETED.name() // Convert enum to String for native query
        );

        boolean isAvailable = completedPayments.isEmpty();

        if (!isAvailable) {
            log.warn("Doctor unavailable - Found {} completed payment(s) in this time slot",
                    completedPayments.size());
            completedPayments.forEach(p ->
                    log.debug("Conflicting payment: Reference={}, Time={}, Status={}",
                            p.getReference(), p.getScheduledDatetime(), p.getStatus())
            );
        } else {
            log.debug("Doctor available - No completed payments found in this time slot");
        }

        return isAvailable;
    }
}
