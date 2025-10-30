package com.mths.consultation.dto;

import com.mths.consultation.entity.AppointmentReschedule;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RescheduleAppointmentRequest {
    
    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;
    
    @NotNull(message = "New datetime is required")
    @Future(message = "New appointment time must be in the future")
    private LocalDateTime newDatetime;
    
    private String reason;
    
    @NotNull(message = "Rescheduled by is required")
    private AppointmentReschedule.RescheduledBy rescheduledBy;
}