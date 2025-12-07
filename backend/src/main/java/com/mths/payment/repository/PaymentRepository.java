package com.mths.payment.repository;

import com.mths.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByReference(String reference);

    List<Payment> findByPatientProfileId(Long patientProfileId);

    List<Payment> findByDoctorProfileId(Long doctorProfileId);

    List<Payment> findByAppointmentId(Long appointmentId);

    List<Payment> findByStatus(Payment.PaymentStatus status);

    List<Payment> findByPatientProfileIdAndStatus(Long patientProfileId, Payment.PaymentStatus status);

    boolean existsByReference(String reference);

    // Find payments that are completed but don't have appointments yet
    List<Payment> findByStatusAndAppointmentIdIsNull(Payment.PaymentStatus status);

    /**
     * Check if doctor has a COMPLETED payment for the given time slot
     * Only COMPLETED payments should block new bookings (PENDING payments might never complete)
     *
     * Uses native PostgreSQL query for date arithmetic
     *
     * @param doctorProfileId The doctor's profile ID
     * @param startTime Start of the requested time slot
     * @param endTime End of the requested time slot
     * @param status Payment status as String (e.g., "COMPLETED")
     * @return List of conflicting completed payments
     */
    @Query(value = "SELECT * FROM payments p WHERE p.doctor_profile_id = :doctorProfileId " +
           "AND p.status = :status " +
           "AND p.scheduled_datetime < :endTime " +
           "AND (p.scheduled_datetime + (p.duration_minutes || ' minutes')::INTERVAL) > :startTime",
           nativeQuery = true)
    List<Payment> findCompletedPaymentsInTimeRange(
            @Param("doctorProfileId") Long doctorProfileId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("status") String status
    );
}
