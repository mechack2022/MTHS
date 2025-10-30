#!/bin/bash

cd /Users/MAC/Desktop/personal-project/MTHS/backend

# Fix imports for consultation entities
find src/main/java -name "*.java" -exec sed -i '' \
  -e 's|import com\.mths\.auth\.entity\.Appointment;|import com.mths.consultation.entity.Appointment;|g' \
  -e 's|import com\.mths\.auth\.entity\.AppointmentReschedule;|import com.mths.consultation.entity.AppointmentReschedule;|g' \
  -e 's|import com\.mths\.auth\.entity\.VideoConsultation;|import com.mths.consultation.entity.VideoConsultation;|g' \
  -e 's|import com\.mths\.auth\.entity\.ChatMessage;|import com.mths.consultation.entity.ChatMessage;|g' \
  -e 's|import com\.mths\.auth\.entity\.FileAttachment;|import com.mths.consultation.entity.FileAttachment;|g' \
  {} \;

# Fix imports for hospital entities
find src/main/java -name "*.java" -exec sed -i '' \
  -e 's|import com\.mths\.auth\.entity\.Hospital;|import com.mths.hospital.entity.Hospital;|g' \
  -e 's|import com\.mths\.auth\.entity\.DoctorProfile;|import com.mths.hospital.entity.DoctorProfile;|g' \
  -e 's|import com\.mths\.auth\.entity\.LabTechnicianProfile;|import com.mths.hospital.entity.LabTechnicianProfile;|g' \
  {} \;

# Fix imports for patient entities
find src/main/java -name "*.java" -exec sed -i '' \
  -e 's|import com\.mths\.auth\.entity\.Patient;|import com.mths.patient.entity.Patient;|g' \
  -e 's|import com\.mths\.auth\.entity\.PatientProfile;|import com.mths.patient.entity.PatientProfile;|g' \
  -e 's|import com\.mths\.auth\.entity\.VitalSigns;|import com.mths.patient.entity.VitalSigns;|g' \
  {} \;

# Fix DTO imports
find src/main/java -name "*.java" -exec sed -i '' \
  -e 's|import com\.mths\.auth\.dto\.AppointmentDTO;|import com.mths.consultation.dto.AppointmentDTO;|g' \
  -e 's|import com\.mths\.auth\.dto\.VideoConsultationDTO;|import com.mths.consultation.dto.VideoConsultationDTO;|g' \
  -e 's|import com\.mths\.auth\.dto\.ChatMessageDTO;|import com.mths.consultation.dto.ChatMessageDTO;|g' \
  {} \;

# Fix service imports
find src/main/java -name "*.java" -exec sed -i '' \
  -e 's|import com\.mths\.auth\.service\.AppointmentService;|import com.mths.consultation.service.AppointmentService;|g' \
  -e 's|import com\.mths\.auth\.service\.VideoConsultationService;|import com.mths.consultation.service.VideoConsultationService;|g' \
  -e 's|import com\.mths\.auth\.service\.ChatMessageService;|import com.mths.consultation.service.ChatMessageService;|g' \
  {} \;

# Fix controller imports
find src/main/java -name "*.java" -exec sed -i '' \
  -e 's|import com\.mths\.auth\.controller\.ChatController;|import com.mths.consultation.controller.ChatController;|g' \
  {} \;

# Fix repository imports
find src/main/java -name "*.java" -exec sed -i '' \
  -e 's|import com\.mths\.auth\.repository\.DoctorProfileRepository;|import com.mths.hospital.repository.DoctorProfileRepository;|g' \
  -e 's|import com\.mths\.auth\.repository\.PatientProfileRepository;|import com.mths.patient.repository.PatientProfileRepository;|g' \
  {} \;

echo "Entity imports fixed successfully"
