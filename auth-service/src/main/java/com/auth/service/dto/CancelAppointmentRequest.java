package com.auth.service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CancelAppointmentRequest {
    
    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;
    
    @NotBlank(message = "Cancellation reason is required")
    private String reason;
    
    @NotBlank(message = "Cancelled by is required")
    private String cancelledBy; // patient, doctor, system
}