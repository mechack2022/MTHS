package com.mths.payment.entity;

import com.mths.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@EqualsAndHashCode(callSuper = false)
public class Payment extends BaseEntity {

    @Column(name = "reference", unique = true, nullable = false)
    private String reference; // Paystack reference

    @Column(name = "patient_profile_id", nullable = false)
    private Long patientProfileId;

    @Column(name = "doctor_profile_id", nullable = false)
    private Long doctorProfileId;

    @Column(name = "appointment_id")
    private Long appointmentId; // NULL until appointment is created

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "payment_provider")
    private String paymentProvider = "PAYSTACK";

    @Column(name = "authorization_url")
    private String authorizationUrl; // Paystack payment URL

    @Column(name = "patient_email", nullable = false)
    private String patientEmail;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "failed_reason")
    private String failedReason;

    // Booking details (stored until appointment is created)
    @Column(name = "scheduled_datetime", nullable = false)
    private LocalDateTime scheduledDatetime;

    @Column(name = "appointment_type")
    private String appointmentType; // VIRTUAL or IN_PERSON

    @Column(name = "symptoms", columnDefinition = "TEXT")
    private String symptoms;

    @Column(name = "duration_minutes")
    private Integer durationMinutes = 30;

    public enum PaymentStatus {
        PENDING,        // Payment initiated, waiting for user to pay
        PROCESSING,     // Payment in progress
        COMPLETED,      // Payment successful and verified
        FAILED,         // Payment failed
        REFUNDED,       // Payment refunded
        CANCELLED       // Payment cancelled
    }

    public enum PaymentMethod {
        CARD,
        BANK_TRANSFER,
        USSD,
        QR_CODE,
        MOBILE_MONEY
    }

    public boolean isCompleted() {
        return status == PaymentStatus.COMPLETED;
    }

    public boolean isPending() {
        return status == PaymentStatus.PENDING || status == PaymentStatus.PROCESSING;
    }

    public boolean canCreateAppointment() {
        return status == PaymentStatus.COMPLETED && appointmentId == null;
    }
}
