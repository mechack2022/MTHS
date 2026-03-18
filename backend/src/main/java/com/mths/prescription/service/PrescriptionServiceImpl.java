package com.mths.prescription.service;

import com.mths.auth.dto.FileUploadResponse;
import com.mths.auth.service.FileUploadService;
import com.mths.consultation.entity.Appointment;
import com.mths.consultation.repository.AppointmentRepository;
import com.mths.hospital.entity.DoctorProfile;
import com.mths.hospital.repository.DoctorProfileRepository;
import com.mths.patient.entity.PatientProfile;
import com.mths.patient.repository.PatientProfileRepository;
import com.mths.pharmacy.entity.Pharmacist;
import com.mths.pharmacy.entity.Pharmacy;
import com.mths.pharmacy.repository.PharmacistRepository;
import com.mths.pharmacy.repository.PharmacyRepository;
import com.mths.prescription.dto.CreatePrescriptionRequest;
import com.mths.prescription.dto.DispenseMedicationRequest;
import com.mths.prescription.dto.PrescriptionResponse;
import com.mths.prescription.dto.SendToPharmacyRequest;
import com.mths.prescription.entity.Prescription;
import com.mths.prescription.mapper.PrescriptionMapper;
import com.mths.prescription.repository.PrescriptionRepository;
import com.mths.shared.constants.FileCategory;
import com.mths.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for managing prescriptions
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final AppointmentRepository appointmentRepository;
    private final PharmacyRepository pharmacyRepository;
    private final PharmacistRepository pharmacistRepository;
    private final FileUploadService fileUploadService;
    private final PrescriptionMapper prescriptionMapper;

    @Override
    public PrescriptionResponse createPrescription(CreatePrescriptionRequest request, MultipartFile document) {
        log.info("Creating prescription for patient: {}, doctor: {}, context: {}",
                request.getPatientProfileId(), request.getDoctorProfileId(), request.getCreationContext());

        // Validate document
        if (document == null || document.isEmpty()) {
            throw new IllegalArgumentException("Prescription document is required");
        }

        validateDocumentType(document);

        // Validate patient profile
        PatientProfile patientProfile = patientProfileRepository.findById(request.getPatientProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("PatientProfile", "id", request.getPatientProfileId()));

        // Validate doctor profile
        DoctorProfile doctorProfile = doctorProfileRepository.findById(request.getDoctorProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("DoctorProfile", "id", request.getDoctorProfileId()));

        // Create prescription entity
        Prescription prescription = new Prescription();
        prescription.setPatientProfile(patientProfile);
        prescription.setDoctorProfile(doctorProfile);
        prescription.setCreationContext(request.getCreationContext());
        prescription.setDiagnosis(request.getDiagnosis());
        prescription.setNotes(request.getNotes());
        prescription.setIssuedDate(request.getIssuedDate() != null ? request.getIssuedDate() : LocalDate.now());
        prescription.setValidUntil(request.getValidUntil());
        prescription.setIsRefillable(request.getIsRefillable() != null ? request.getIsRefillable() : false);
        prescription.setRefillCount(request.getRefillCount() != null ? request.getRefillCount() : 0);
        prescription.setRefillsRemaining(request.getRefillCount() != null ? request.getRefillCount() : 0);
        prescription.setIsOngoingTreatment(request.getIsOngoingTreatment() != null ? request.getIsOngoingTreatment() : false);

        // Handle appointment if provided
        if (request.getAppointmentId() != null) {
            Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", request.getAppointmentId()));
            prescription.setAppointment(appointment);
        }

        Pharmacy pharmacy = pharmacyRepository.findById(request.getPharmacyId())
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy", "id", request.getPharmacyId()));
        prescription.setPharmacy(pharmacy);

        // Handle previous prescription for refills
        if (request.getPreviousPrescriptionId() != null) {
            Prescription previousPrescription = prescriptionRepository.findById(request.getPreviousPrescriptionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", request.getPreviousPrescriptionId()));
            prescription.setPreviousPrescription(previousPrescription);
        }

        // Generate prescription number
        String prescriptionNumber = generatePrescriptionNumber(patientProfile.getId());
        prescription.setPrescriptionNumber(prescriptionNumber);

        // Upload document to MinIO
        FileUploadResponse uploadResponse = fileUploadService.uploadFile(document, FileCategory.PRESCRIPTION);
        prescription.setPrescriptionDocumentPath(uploadResponse.getFileUrl());
        prescription.setPrescriptionDocumentName(uploadResponse.getOriginalFileName());
        prescription.setPrescriptionDocumentType(uploadResponse.getContentType());
        prescription.setPrescriptionDocumentSize(uploadResponse.getFileSize());
        prescription.setPrescriptionDocumentUploadedAt(LocalDateTime.now());

        // Automatically send to pharmacy since pharmacy is required for document pickup and delivery
        prescription.setPharmacyStatus(Prescription.PharmacyStatus.PENDING);
        prescription.setSentToPharmacyAt(LocalDateTime.now());

        // Save prescription
        Prescription savedPrescription = prescriptionRepository.save(prescription);

        log.info("Prescription created and sent to pharmacy - Number: {}, ID: {}, Pharmacy: {}, Document: {}",
                savedPrescription.getPrescriptionNumber(),
                savedPrescription.getId(),
                pharmacy.getPharmacyName(),
                savedPrescription.getPrescriptionDocumentName());

        return prescriptionMapper.toResponse(savedPrescription);
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponse getPrescriptionById(Long prescriptionId) {
        Prescription prescription = findPrescriptionById(prescriptionId);
        return prescriptionMapper.toResponse(prescription);
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponse getPrescriptionByNumber(String prescriptionNumber) {
        Prescription prescription = prescriptionRepository.findByPrescriptionNumber(prescriptionNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "prescriptionNumber", prescriptionNumber));
        return prescriptionMapper.toResponse(prescription);
    }

    @Override
    public PrescriptionResponse updatePrescriptionDocument(Long prescriptionId, MultipartFile document) {
        log.info("Updating prescription document for prescription: {}", prescriptionId);

        if (document == null || document.isEmpty()) {
            throw new IllegalArgumentException("Prescription document is required");
        }

        validateDocumentType(document);

        Prescription prescription = findPrescriptionById(prescriptionId);

        // Upload new document
        FileUploadResponse uploadResponse = fileUploadService.uploadFile(document, FileCategory.PRESCRIPTION);
        prescription.setPrescriptionDocumentPath(uploadResponse.getFileUrl());
        prescription.setPrescriptionDocumentName(uploadResponse.getOriginalFileName());
        prescription.setPrescriptionDocumentType(uploadResponse.getContentType());
        prescription.setPrescriptionDocumentSize(uploadResponse.getFileSize());
        prescription.setPrescriptionDocumentUploadedAt(LocalDateTime.now());

        Prescription updatedPrescription = prescriptionRepository.save(prescription);

        log.info("Prescription document updated - ID: {}, New document: {}",
                prescriptionId, uploadResponse.getOriginalFileName());

        return prescriptionMapper.toResponse(updatedPrescription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getPatientPrescriptions(Long patientProfileId) {
        log.info("Retrieving all prescriptions for patient: {}", patientProfileId);

        List<Prescription> prescriptions = prescriptionRepository.findByPatientProfileId(patientProfileId);
        return prescriptions.stream()
                .map(prescriptionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getActivePatientPrescriptions(Long patientProfileId) {
        log.info("Retrieving active prescriptions for patient: {}", patientProfileId);

        List<Prescription> prescriptions = prescriptionRepository.findActiveByPatientProfileId(patientProfileId);
        return prescriptions.stream()
                .map(prescriptionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getDoctorPrescriptions(Long doctorProfileId) {
        log.info("Retrieving prescriptions for doctor: {}", doctorProfileId);

        List<Prescription> prescriptions = prescriptionRepository.findByDoctorProfileId(doctorProfileId);
        return prescriptions.stream()
                .map(prescriptionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getAppointmentPrescriptions(Long appointmentId) {
        log.info("Retrieving prescriptions for appointment: {}", appointmentId);

        List<Prescription> prescriptions = prescriptionRepository.findByAppointmentId(appointmentId);
        return prescriptions.stream()
                .map(prescriptionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PrescriptionResponse sendToPharmacy(Long prescriptionId, SendToPharmacyRequest request) {
        log.info("Sending prescription {} to pharmacy {}", prescriptionId, request.getPharmacyId());

        Prescription prescription = findPrescriptionById(prescriptionId);

        // Validate prescription can be sent
        if (!prescription.canBeSentToPharmacy()) {
            throw new IllegalStateException("Prescription cannot be sent to pharmacy. Status: " + prescription.getStatus() +
                    ", Pharmacy Status: " + prescription.getPharmacyStatus() +
                    ", Has Document: " + prescription.hasDocument() +
                    ", Expired: " + prescription.isExpired());
        }

        // Get pharmacy
        Pharmacy pharmacy = pharmacyRepository.findById(request.getPharmacyId())
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy", "id", request.getPharmacyId()));

        // Send to pharmacy
        prescription.sendToPharmacy(pharmacy);

        if (request.getNotes() != null) {
            prescription.setPharmacyNotes(request.getNotes());
        }

        Prescription updatedPrescription = prescriptionRepository.save(prescription);

        log.info("Prescription {} sent to pharmacy {} at {}",
                prescriptionId, pharmacy.getPharmacyName(), prescription.getSentToPharmacyAt());

        return prescriptionMapper.toResponse(updatedPrescription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getPharmacyPrescriptions(Long pharmacyId) {
        log.info("Retrieving prescriptions for pharmacy: {}", pharmacyId);

        List<Prescription> prescriptions = prescriptionRepository.findByPharmacyId(pharmacyId);
        return prescriptions.stream()
                .map(prescriptionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getPharmacyPrescriptionsByStatus(Long pharmacyId, Prescription.PharmacyStatus status) {
        log.info("Retrieving prescriptions for pharmacy: {} with status: {}", pharmacyId, status);

        List<Prescription> prescriptions = prescriptionRepository.findByPharmacyIdAndStatus(pharmacyId, status);
        return prescriptions.stream()
                .map(prescriptionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PrescriptionResponse updatePharmacyStatus(Long prescriptionId, Prescription.PharmacyStatus status, String pharmacyNotes) {
        log.info("Updating pharmacy status for prescription {} to {}", prescriptionId, status);

        Prescription prescription = findPrescriptionById(prescriptionId);
        prescription.setPharmacyStatus(status);

        if (pharmacyNotes != null) {
            prescription.setPharmacyNotes(pharmacyNotes);
        }

        Prescription updatedPrescription = prescriptionRepository.save(prescription);

        log.info("Pharmacy status updated for prescription {} to {}", prescriptionId, status);

        return prescriptionMapper.toResponse(updatedPrescription);
    }

    @Override
    public PrescriptionResponse dispenseMedication(Long prescriptionId, DispenseMedicationRequest request) {
        log.info("Dispensing medication for prescription: {}", prescriptionId);

        Prescription prescription = findPrescriptionById(prescriptionId);

        // Validate prescription can be dispensed
        if (!prescription.canBeDispensed()) {
            throw new IllegalStateException("Prescription cannot be dispensed. Status: " + prescription.getStatus() +
                    ", Expired: " + prescription.isExpired());
        }

        // Get pharmacist
        Pharmacist pharmacist = pharmacistRepository.findById(request.getPharmacistId())
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacist", "id", request.getPharmacistId()));

        // Update prescription
        prescription.setStatus(Prescription.PrescriptionStatus.DISPENSED);
        prescription.setPharmacyStatus(Prescription.PharmacyStatus.DISPENSED);
        prescription.setDispensedDate(LocalDateTime.now());
        prescription.setPharmacist(pharmacist);
        prescription.setDispensedBy(pharmacist.getFullName());

//        if (request.getNotes() != null) {
//            prescription.setPharmacyNotes(request.getNotes());
//        }

        // Decrease refills if applicable
        if (prescription.getIsRefillable() && prescription.getRefillsRemaining() != null && prescription.getRefillsRemaining() > 0) {
            prescription.setRefillsRemaining(prescription.getRefillsRemaining() - 1);
        }

        Prescription updatedPrescription = prescriptionRepository.save(prescription);

        log.info("Medication dispensed for prescription {} by pharmacist {}",
                prescriptionId, pharmacist.getFullName());

        return prescriptionMapper.toResponse(updatedPrescription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getRefillablePrescriptions(Long patientProfileId) {
        log.info("Retrieving refillable prescriptions for patient: {}", patientProfileId);

        List<Prescription> prescriptions = prescriptionRepository.findRefillableByPatientProfileId(patientProfileId);
        return prescriptions.stream()
                .map(prescriptionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PrescriptionResponse createRefill(Long originalPrescriptionId, MultipartFile document) {
        log.info("Creating refill for prescription: {}", originalPrescriptionId);

        Prescription originalPrescription = findPrescriptionById(originalPrescriptionId);

        // Validate refill is allowed
        if (!originalPrescription.canBeRefilled()) {
            throw new IllegalStateException("Prescription cannot be refilled. Is Refillable: " +
                    originalPrescription.getIsRefillable() + ", Refills Remaining: " +
                    originalPrescription.getRefillsRemaining());
        }

        // Create request for refill
        CreatePrescriptionRequest request = new CreatePrescriptionRequest();
        request.setPatientProfileId(originalPrescription.getPatientProfile().getId());
        request.setDoctorProfileId(originalPrescription.getDoctorProfile().getId());
        request.setPharmacyId(originalPrescription.getPharmacy().getId()); // Use same pharmacy as original
        request.setCreationContext(Prescription.CreationContext.REFILL);
        request.setDiagnosis(originalPrescription.getDiagnosis());
        request.setNotes("Refill of prescription: " + originalPrescription.getPrescriptionNumber());
        request.setPreviousPrescriptionId(originalPrescriptionId);
        request.setIsRefillable(originalPrescription.getIsRefillable());
        request.setRefillCount(originalPrescription.getRefillCount());
        request.setIsOngoingTreatment(originalPrescription.getIsOngoingTreatment());

        // Create the refill prescription (will automatically send to pharmacy)
        return createPrescription(request, document);
    }

    @Override
    public PrescriptionResponse cancelPrescription(Long prescriptionId, String reason) {
        log.info("Cancelling prescription: {} with reason: {}", prescriptionId, reason);

        Prescription prescription = findPrescriptionById(prescriptionId);
        prescription.setStatus(Prescription.PrescriptionStatus.CANCELLED);

        if (reason != null) {
            String currentNotes = prescription.getNotes() != null ? prescription.getNotes() + "\n\n" : "";
            prescription.setNotes(currentNotes + "CANCELLED: " + reason);
        }

        if (prescription.isSentToPharmacy() && prescription.getPharmacyStatus() != Prescription.PharmacyStatus.DISPENSED) {
            prescription.setPharmacyStatus(Prescription.PharmacyStatus.CANCELLED);
        }

        Prescription updatedPrescription = prescriptionRepository.save(prescription);

        log.info("Prescription {} cancelled", prescriptionId);

        return prescriptionMapper.toResponse(updatedPrescription);
    }

    @Override
    public PrescriptionResponse expirePrescription(Long prescriptionId) {
        log.info("Expiring prescription: {}", prescriptionId);

        Prescription prescription = findPrescriptionById(prescriptionId);
        prescription.setStatus(Prescription.PrescriptionStatus.EXPIRED);

        Prescription updatedPrescription = prescriptionRepository.save(prescription);

        log.info("Prescription {} expired", prescriptionId);

        return prescriptionMapper.toResponse(updatedPrescription);
    }

    @Override
    @Transactional(readOnly = true)
    public String getPrescriptionDocumentPath(Long prescriptionId) {
        Prescription prescription = findPrescriptionById(prescriptionId);

        if (!prescription.hasDocument()) {
            throw new ResourceNotFoundException( prescription.getPrescriptionDocumentType(),prescription.getPrescriptionDocumentName(),"Prescription document not found for prescription ID: " + prescriptionId );
        }

        return prescription.getPrescriptionDocumentPath();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getPrescriptionsReadyForPickup() {
        log.info("Retrieving prescriptions ready for pickup");

        List<Prescription> prescriptions = prescriptionRepository.findReadyForPickup();
        return prescriptions.stream()
                .map(prescriptionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getPendingPharmacyPrescriptions() {
        log.info("Retrieving prescriptions pending at pharmacy");

        List<Prescription> prescriptions = prescriptionRepository.findPendingAtPharmacy();
        return prescriptions.stream()
                .map(prescriptionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public int updateExpiredPrescriptions() {
        log.info("Updating expired prescriptions");

        List<Prescription> expiredPrescriptions = prescriptionRepository.findExpiredPrescriptions(LocalDate.now());

        expiredPrescriptions.forEach(prescription -> {
            prescription.setStatus(Prescription.PrescriptionStatus.EXPIRED);
            prescriptionRepository.save(prescription);
        });

        int count = expiredPrescriptions.size();
        log.info("Updated {} expired prescriptions", count);

        return count;
    }

    // ========================================================================
    // PRIVATE HELPER METHODS
    // ========================================================================

    private Prescription findPrescriptionById(Long prescriptionId) {
        return prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", prescriptionId));
    }

    private String generatePrescriptionNumber(Long patientId) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String prescriptionNumber = "RX-" + patientId + "-" + timestamp;

        // Ensure uniqueness
        while (prescriptionRepository.existsByPrescriptionNumber(prescriptionNumber)) {
            prescriptionNumber = "RX-" + patientId + "-" + System.currentTimeMillis();
        }

        return prescriptionNumber;
    }

    private void validateDocumentType(MultipartFile document) {
        String contentType = document.getContentType();

        boolean isValidType = contentType != null && (
                contentType.equals("application/pdf") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/jpeg") ||
                        contentType.equals("image/jpg")
        );

        if (!isValidType) {
            throw new IllegalArgumentException("Invalid document type. Only PDF and PNG/JPG files are allowed. Received: " + contentType);
        }

        // Check file size (max 10MB)
        if (document.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("Document size exceeds 10 MB limit");
        }
    }
}
