package com.mths.payment.service;

import com.mths.consultation.dto.CreateAppointmentRequest;
import com.mths.payment.dto.PaymentInitializationResponse;
import com.mths.payment.entity.Payment;

import java.time.LocalDateTime;

public interface PaymentService {

    /**
     * Initiate payment for appointment booking
     * Returns payment URL for patient to complete payment
     */
    PaymentInitializationResponse initiateAppointmentPayment(CreateAppointmentRequest request, String patientEmail);

    /**
     * Verify payment by reference
     */
    Payment verifyPayment(String reference);

    /**
     * Process successful payment webhook
     */
    void handlePaymentSuccess(String reference);

    /**
     * Get payment by reference
     */
    Payment getPaymentByReference(String reference);

    /**
     * Check if patient has paid for specific booking
     */
    boolean hasPatientPaidForBooking(Long patientProfileId, Long doctorProfileId, String scheduledDatetime);

    /**
     * Check if doctor has COMPLETED payment (and thus booked appointment) for the given time slot
     * PENDING payments are ignored since they might never be completed
     *
     * @param doctorProfileId The doctor's profile ID
     * @param startTime Start of the requested time slot
     * @param endTime End of the requested time slot
     * @return true if time slot is available (no completed payments), false otherwise
     */
    boolean isDoctorAvailableForPayment(Long doctorProfileId, LocalDateTime startTime, LocalDateTime endTime);
}
