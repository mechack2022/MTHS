package com.mths.payment.controller;

import com.mths.consultation.dto.CreateAppointmentRequest;
import com.mths.payment.dto.PaymentInitializationResponse;
import com.mths.payment.dto.PaymentVerificationResponse;
import com.mths.payment.entity.Payment;
import com.mths.payment.service.PaymentService;
import com.mths.payment.service.PaymentServiceImpl;
import com.mths.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Payment REST Controller
 * Handles payment-related HTTP requests
 *
 * Endpoints:
 * - POST /api/payments/initiate-booking - Initialize payment for appointment
 * - GET /api/payments/verify/{reference} - Verify payment status
 */
@RestController
@RequestMapping("/api/payments")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "APIs for managing payments and appointment bookings. " +
        "All appointments require payment before confirmation.")
@SecurityRequirement(name = "bearer-jwt")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentServiceImpl paymentServiceImpl; // For additional methods

    /**
     * Initiate payment for appointment booking
     * Patient calls this to start the payment-first booking flow
     *
     * Flow:
     * 1. Validate appointment request
     * 2. Create payment record (PENDING)
     * 3. Call Paystack to initialize payment
     * 4. Return payment URL for patient
     *
     * @param request Appointment booking details
     * @return Payment initialization response with URL
     */
    @Operation(
            summary = "Initiate Appointment Booking Payment",
            description = """
                    ### Payment-First Booking Flow

                    This endpoint initiates the payment for an appointment booking.
                    **IMPORTANT**: The appointment is NOT created until payment is completed and verified.

                    **Process:**
                    1. Submit appointment details with this endpoint
                    2. Receive Paystack payment URL in response
                    3. Redirect patient to payment URL
                    4. Patient completes payment
                    5. Paystack sends webhook to backend
                    6. Backend creates appointment (status: PENDING_CONFIRMATION)
                    7. Doctor confirms appointment

                    **Payment Amount**: NGN 10,000 (flat consultation fee)

                    **Requirements:**
                    - Valid JWT token (authenticated patient)
                    - Doctor must be available at requested time
                    - Scheduled time must be in the future
                    """,
            tags = {"Payment Management"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Payment initialized successfully. Redirect patient to authorizationUrl.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": true,
                                      "message": "Payment initialized. Please complete payment at the provided URL.",
                                      "data": {
                                        "reference": "MTHS-APT-123-1701234567890",
                                        "authorizationUrl": "https://checkout.paystack.com/abc123",
                                        "amount": 10000.00,
                                        "currency": "NGN",
                                        "message": "Payment initialized successfully. Please complete payment to confirm your appointment."
                                      },
                                      "statusCode": 201
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - validation errors or doctor not available"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token missing or invalid"
            )
    })
    @PostMapping("/initiate-booking")
    public ResponseEntity<ApiResponse<PaymentInitializationResponse>> initiateBookingPayment(
            @Parameter(description = "Appointment booking details", required = true)
            @Valid @RequestBody CreateAppointmentRequest request) {

        String patientEmail = getCurrentUserEmail();

        log.info("Initiating booking payment - Patient: {}, Doctor: {}",
                request.getPatientProfileId(), request.getDoctorProfileId());

        PaymentInitializationResponse response = paymentService.initiateAppointmentPayment(
                request,
                patientEmail
        );

        ApiResponse<PaymentInitializationResponse> apiResponse = ApiResponse.success(
                "Payment initialized. Please complete payment at the provided URL.",
                response,
                HttpStatus.CREATED.value()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    /**
     * Verify payment status
     * Called by frontend after patient returns from payment page
     *
     * @param reference Payment reference
     * @return Payment verification details
     */
    @Operation(
            summary = "Verify Payment Status",
            description = """
                    ### Verify Payment and Check Appointment Creation

                    After patient completes payment on Paystack and returns to your application,
                    call this endpoint to verify the payment status and check if appointment was created.

                    **Use Cases:**
                    - Patient returns from Paystack payment page
                    - Check if payment was successful
                    - Get appointment ID if created
                    - Display confirmation message to patient

                    **Payment Statuses:**
                    - `PENDING`: Payment not yet completed
                    - `COMPLETED`: Payment successful, appointment created
                    - `FAILED`: Payment failed

                    **Note**: If payment is completed but appointmentId is null, the webhook is still processing.
                    Retry after a few seconds.
                    """,
            tags = {"Payment Management"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Payment verification successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": true,
                                      "message": "Payment verified successfully",
                                      "data": {
                                        "reference": "MTHS-APT-123-1701234567890",
                                        "paymentStatus": "COMPLETED",
                                        "appointmentId": 999,
                                        "appointmentStatus": "PENDING_CONFIRMATION",
                                        "message": "Payment verified successfully. Appointment created and awaiting doctor confirmation."
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Payment reference not found"
            )
    })
    @GetMapping("/verify/{reference}")
    public ResponseEntity<ApiResponse<PaymentVerificationResponse>> verifyPayment(
            @Parameter(description = "Payment reference (e.g., MTHS-APT-123-1701234567890)", required = true, example = "MTHS-APT-123-1701234567890")
            @PathVariable String reference) {

        log.info("Verifying payment - Reference: {}", reference);

        PaymentVerificationResponse response = paymentServiceImpl.getPaymentVerificationDetails(reference);

        String message = response.getPaymentStatus() == Payment.PaymentStatus.COMPLETED
                ? "Payment verified successfully"
                : "Payment verification in progress";

        ApiResponse<PaymentVerificationResponse> apiResponse = ApiResponse.success(
                message,
                response
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get current authenticated user's email
     */
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }
        return authentication.getName(); // Assuming email is used as username
    }
}
