package com.mths.pharmacy.service;

import com.mths.pharmacy.entity.Pharmacy;
import com.mths.pharmacy.repository.PharmacyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Initializes sample pharmacy data for testing
 * Creates 6 verified pharmacies across different Nigerian cities
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Order(2) // Run after DataInitializationService
public class PharmacyDataInitializer implements CommandLineRunner {

    private final PharmacyRepository pharmacyRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (pharmacyRepository.count() == 0) {
            log.info("Initializing sample pharmacy data...");
            createSamplePharmacies();
            log.info("Sample pharmacy data initialization completed - 6 pharmacies created");
        } else {
            log.info("Pharmacy data already exists - skipping initialization");
        }
    }

    private void createSamplePharmacies() {
        List<Pharmacy> pharmacies = new ArrayList<>();

        // 1. HealthPlus Pharmacy - Lagos (24 hours)
        pharmacies.add(createPharmacy(
                "HealthPlus Pharmacy",
                "PH-001-LG-2024",
                "PCN-HP-001",
                "contact@healthplus.com.ng",
                "+2348012345001",
                "+2347098765001",
                "123 Victoria Island, Adeola Odeku Street",
                "Lagos",
                "Lagos State",
                "101241",
                6.4281, 3.4219,
                null, null, true,
                "Monday - Sunday (24 Hours)",
                "Dr. Adebayo Johnson",
                "Pharm. Grace Okonkwo",
                "Prescription filling, OTC medications, Health consultations, Home delivery, Vaccination services",
                true,
                "NHIS, Reliance HMO, AXA Mansard, Hygeia HMO",
                "Located in Victoria Island. Free delivery within Lagos Island for orders above ₦10,000. 24-hour emergency service available."
        ));

        // 2. MediCare Pharmacy - Abuja
        pharmacies.add(createPharmacy(
                "MediCare Pharmacy",
                "PH-002-AB-2024",
                "PCN-MC-002",
                "info@medicare.com.ng",
                "+2348012345002",
                "+2347098765002",
                "45 Gimbiya Street, Area 11, Garki",
                "Abuja",
                "Federal Capital Territory",
                "900211",
                9.0579, 7.4951,
                LocalTime.of(8, 0), LocalTime.of(21, 0), false,
                "Monday - Saturday (8am - 9pm)",
                "Mrs. Fatima Ibrahim",
                "Pharm. Mohammed Ali",
                "Prescription dispensing, Medical consultations, Diabetes care, Blood pressure monitoring",
                true,
                "NHIS, OPIC, MetroHealth HMO",
                "Convenient location in Garki. Specializing in chronic disease management. Free home delivery for seniors."
        ));

        // 3. Wellness Pharmacy - Port Harcourt
        pharmacies.add(createPharmacy(
                "Wellness Pharmacy",
                "PH-003-PH-2024",
                "PCN-WP-003",
                "contact@wellness.com.ng",
                "+2348012345003",
                "+2347098765003",
                "78 Aba Road, GRA Phase 2",
                "Port Harcourt",
                "Rivers State",
                "500211",
                4.8156, 7.0498,
                LocalTime.of(7, 30), LocalTime.of(20, 0), false,
                "Monday - Saturday (7:30am - 8pm), Sunday (10am - 4pm)",
                "Dr. Chinedu Nwankwo",
                "Pharm. Blessing Eze",
                "Full pharmacy services, Baby care products, Medical equipment sales, Health screening",
                false,
                null,
                "Family-friendly pharmacy in GRA. Wide range of baby and maternal care products. Quick prescription turnaround."
        ));

        // 4. CarePlus Pharmacy - Kano
        pharmacies.add(createPharmacy(
                "CarePlus Pharmacy",
                "PH-004-KN-2024",
                "PCN-CP-004",
                "info@careplus.com.ng",
                "+2348012345004",
                "+2347098765004",
                "12 Ahmadu Bello Way, Sabon Gari",
                "Kano",
                "Kano State",
                "700211",
                12.0022, 8.5920,
                LocalTime.of(8, 0), LocalTime.of(19, 0), false,
                "Monday - Saturday (8am - 7pm)",
                "Malam Usman Abdullahi",
                "Pharm. Aisha Suleiman",
                "Prescription services, Traditional medicine, Health supplements, First aid supplies",
                true,
                "NHIS, Total Health Trust",
                "Serving Kano community for over 10 years. Multilingual staff. Accepts NHIS cards."
        ));

        // 5. City Pharmacy - Ibadan
        pharmacies.add(createPharmacy(
                "City Pharmacy",
                "PH-005-IB-2024",
                "PCN-CTP-005",
                "contact@citypharmacy.com.ng",
                "+2348012345005",
                "+2347098765005",
                "234 Iwo Road, Ring Road",
                "Ibadan",
                "Oyo State",
                "200211",
                7.3775, 3.9470,
                LocalTime.of(7, 0), LocalTime.of(22, 0), false,
                "Monday - Sunday (7am - 10pm)",
                "Chief Adeyemi Ogunlana",
                "Pharm. Folake Adebisi",
                "24/7 hotline, Emergency prescriptions, Medical devices, Health consultations, COVID-19 testing",
                true,
                "NHIS, Avon HMO, Princeton HMO",
                "Central location on Ring Road. Extended hours for your convenience. COVID-19 testing available."
        ));

        // 6. Remedy Pharmacy - Enugu
        pharmacies.add(createPharmacy(
                "Remedy Pharmacy",
                "PH-006-EN-2024",
                "PCN-RM-006",
                "info@remedy.com.ng",
                "+2348012345006",
                "+2347098765006",
                "56 Ogui Road, Independence Layout",
                "Enugu",
                "Enugu State",
                "400211",
                6.4414, 7.4960,
                LocalTime.of(8, 0), LocalTime.of(20, 0), false,
                "Monday - Saturday (8am - 8pm), Sunday (12pm - 6pm)",
                "Prof. Emeka Okafor",
                "Pharm. Chioma Ugwu",
                "Prescription filling, Compounding services, Medical supplies, Home healthcare products, Delivery service",
                true,
                "NHIS, Hygeia HMO, Clearline HMO",
                "Professional compounding services available. Free medication counseling. Home delivery within Enugu metropolis."
        ));

        // Save all pharmacies with VERIFIED status
        LocalDateTime now = LocalDateTime.now();
        for (Pharmacy pharmacy : pharmacies) {
            pharmacy.setStatus(Pharmacy.PharmacyStatus.VERIFIED);
            pharmacy.setIsActive(true);
            pharmacy.setVerificationDate(now);
            pharmacy.setVerifiedBy("System");
            pharmacy.setRating(4.5); // Default good rating
            pharmacy.setTotalReviews(0);
            pharmacy.setTotalPrescriptionsFilled(0);

            pharmacyRepository.save(pharmacy);
            log.info("Created and verified pharmacy: {} in {}", pharmacy.getPharmacyName(), pharmacy.getCity());
        }
    }

    private Pharmacy createPharmacy(
            String pharmacyName,
            String licenseNumber,
            String registrationNumber,
            String email,
            String phoneNumber,
            String alternativePhone,
            String address,
            String city,
            String state,
            String postalCode,
            Double latitude,
            Double longitude,
            LocalTime openingTime,
            LocalTime closingTime,
            Boolean is24Hours,
            String operatingDays,
            String ownerName,
            String superintendentPharmacist,
            String servicesOffered,
            Boolean acceptsInsurance,
            String insuranceProviders,
            String publicNotes) {

        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setPharmacyName(pharmacyName);
        pharmacy.setLicenseNumber(licenseNumber);
        pharmacy.setRegistrationNumber(registrationNumber);
        pharmacy.setEmail(email);
        pharmacy.setPhoneNumber(phoneNumber);
        pharmacy.setAlternativePhone(alternativePhone);
        pharmacy.setAddress(address);
        pharmacy.setCity(city);
        pharmacy.setState(state);
        pharmacy.setPostalCode(postalCode);
        pharmacy.setCountry("Nigeria");
        pharmacy.setLatitude(latitude);
        pharmacy.setLongitude(longitude);
        pharmacy.setOpeningTime(openingTime);
        pharmacy.setClosingTime(closingTime);
        pharmacy.setIs24Hours(is24Hours);
        pharmacy.setOperatingDays(operatingDays);
        pharmacy.setOwnerName(ownerName);
        pharmacy.setSuperintendentPharmacist(superintendentPharmacist);
        pharmacy.setServicesOffered(servicesOffered);
        pharmacy.setAcceptsInsurance(acceptsInsurance);
        pharmacy.setInsuranceProviders(insuranceProviders);
        pharmacy.setPublicNotes(publicNotes);

        return pharmacy;
    }
}
