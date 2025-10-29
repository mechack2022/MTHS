package com.mths.consultation.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointment_reschedules")
@Data
@EqualsAndHashCode(callSuper = false)
public class AppointmentReschedule extends com.mths.shared.entity.BaseEntity {
    // Primary key inherited from BaseEntity as 'id'
    // Using 'id' field from BaseEntity instead of rescheduleId

    // Appointment ID is handled through relationship below
    // @Column(name = "appointment_id") - handled by @JoinColumn

    @Column(name = "old_datetime", nullable = false)
    private LocalDateTime oldDatetime;

    @Column(name = "new_datetime", nullable = false)
    private LocalDateTime newDatetime;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "rescheduled_by")
    private RescheduledBy rescheduledBy;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    public enum RescheduledBy {
        PATIENT("Patient"),
        DOCTOR("Doctor"),
        SYSTEM("System");

        private final String description;

        RescheduledBy(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Helper method for appointment ID (delegate to relationship)
    public Long getAppointmentId() {
        return appointment != null ? appointment.getId() : null;
    }
}