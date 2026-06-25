package com.telemedclinic.doctor.controller;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalTime;
import java.util.Map;

import com.telemedclinic.user.entity.Doctor;
import com.telemedclinic.user.entity.Role;
import com.telemedclinic.user.entity.User;
import com.telemedclinic.user.repository.DoctorRepository;
import com.telemedclinic.user.repository.UserRepository;
import com.telemedclinic.consultation.model.Consultation;
import com.telemedclinic.consultation.model.ConsultationStatus;
import com.telemedclinic.consultation.repository.ConsultationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.telemedclinic.medicine.repository.MedicineRepository;
import com.telemedclinic.prescription.repository.PrescriptionRepository;
@Controller
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    @Autowired
    private ConsultationRepository consultationRepository;

    @Autowired
    private com.telemedclinic.consultation.repository.ChatMessageRepository chatMessageRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    public DoctorController(UserRepository userRepository, DoctorRepository doctorRepository) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
    }

    @ModelAttribute("doctor")
    public Doctor getAuthenticatedDoctor(HttpSession session) {
        return findAuthenticatedDoctor(session).orElse(null);
    }

    private Optional<Doctor> findAuthenticatedDoctor(HttpSession session) {
        String roleValue = session.getAttribute("currentUserRole") != null
                ? session.getAttribute("currentUserRole").toString()
                : null;

        if (!Role.ROLE_DOCTOR.name().equals(roleValue)) {
            return Optional.empty();
        }

        String email = (String) session.getAttribute("currentUserEmail");
        if (email == null) {
            return Optional.empty();
        }

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty() || !(optionalUser.get() instanceof Doctor doctor)) {
            return Optional.empty();
        }

        return Optional.of(doctor);
    }

    @GetMapping("/dashboard")
public String dashboard(HttpSession session, Model model) {
    Optional<Doctor> optionalDoctor = findAuthenticatedDoctor(session);
    if (optionalDoctor.isEmpty()) {
        return "redirect:/auth/login";
    }

    Doctor doctor = optionalDoctor.get();

    Long doctorId = doctor.getUserId();

    // Total pasien yang pernah ditangani
    long totalPatients = consultationRepository
            .findByDoctorUserIdAndStatusOrderByCreatedAtDesc(
                    doctorId,
                    ConsultationStatus.COMPLETED
            ).size();

    // Antrean menunggu
    long waitingQueue = consultationRepository
            .findByDoctorUserIdAndStatusInOrderByCreatedAtAsc(
                    doctorId,
                    java.util.List.of(ConsultationStatus.IN_PROGRESS)
            ).size();

    // Konsultasi hari ini
    List<Consultation> todayConsultations =
            consultationRepository.findByDoctorUserIdAndStatusInOrderByCreatedAtAsc(
                    doctorId,
                    java.util.List.of(ConsultationStatus.COMPLETED)
            );

    // Self-healing check & Build prescription medicines map
    java.util.Map<Long, String> prescriptionMedicinesMap = new java.util.HashMap<>();
    for (Consultation consultation : todayConsultations) {
        if (!consultation.isPrescription()) {
            boolean hasRx = prescriptionRepository.findByConsultationId(consultation.getId()).isPresent();
            if (hasRx) {
                consultation.setPrescription(true);
                consultationRepository.save(consultation);
            }
        }

        Optional<com.telemedclinic.prescription.model.Prescription> rxOpt = prescriptionRepository.findByConsultationId(consultation.getId());
        if (rxOpt.isPresent() && rxOpt.get().getItems() != null) {
            java.util.List<String> medNames = rxOpt.get().getItems().stream()
                    .map(item -> item.getMedicine() != null ? item.getMedicine().getName() : "Obat")
                    .toList();
            if (!medNames.isEmpty()) {
                prescriptionMedicinesMap.put(consultation.getId(), String.join(", ", medNames));
            } else {
                prescriptionMedicinesMap.put(consultation.getId(), "Resep Kosong");
            }
        } else {
            prescriptionMedicinesMap.put(consultation.getId(), null);
        }
    }

    model.addAttribute("doctor", doctor);
    model.addAttribute("totalPatients", totalPatients);
    model.addAttribute("waitingQueue", waitingQueue);
    model.addAttribute("todayConsultations", todayConsultations);
    model.addAttribute("prescriptionMedicinesMap", prescriptionMedicinesMap);

    // Format tanggal saat ini (Indonesian)
    java.time.LocalDate today = java.time.LocalDate.now();
    java.util.Locale localeId = java.util.Locale.forLanguageTag("id-ID");
    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", localeId);
    String formattedDate = today.format(formatter);
    model.addAttribute("currentDate", formattedDate);

    return "doctor/doctor-dashboard";
}

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        Optional<Doctor> optionalDoctor = findAuthenticatedDoctor(session);
        if (optionalDoctor.isEmpty()) {
            return "redirect:/auth/login";
        }

        Doctor doctor = optionalDoctor.get();
        model.addAttribute("doctor", doctor);
        return "doctor/doctor-profile";
    }

    @GetMapping("/antrean")
    public String antrean(HttpSession session, Model model) {
        Optional<Doctor> optionalDoctor = findAuthenticatedDoctor(session);
        if (optionalDoctor.isEmpty()) {
            return "redirect:/auth/login";
        }

        Doctor doctor = optionalDoctor.get();
        Optional<Consultation> activeSession = consultationRepository.findByDoctorUserIdAndStatusOrderByCreatedAtDesc(doctor.getUserId(), ConsultationStatus.IN_PROGRESS).stream().findFirst();
        
        if (activeSession.isPresent()) {
            model.addAttribute("activeConsultationId", activeSession.get().getId());
        } else {
            model.addAttribute("activeConsultationId", null);
        }

        List<Consultation> queue = consultationRepository.findByDoctorUserIdAndStatusInOrderByCreatedAtAsc(doctor.getUserId(), java.util.Arrays.asList(ConsultationStatus.WAITING, ConsultationStatus.IN_PROGRESS));
        
        // Self-healing check: Sync prescription status for existing queue items
        for (Consultation consultation : queue) {
            if (!consultation.isPrescription()) {
                boolean hasRx = prescriptionRepository.findByConsultationId(consultation.getId()).isPresent();
                if (hasRx) {
                    consultation.setPrescription(true);
                    consultationRepository.save(consultation);
                }
            }
        }

        model.addAttribute("queue", queue);
        model.addAttribute("doctor", doctor);

        return "doctor/doctor-antrean-masuk";
    }

    @org.springframework.web.bind.annotation.PostMapping("/accept-consultation/{id}")
    public String acceptConsultation(HttpSession session, @PathVariable Long id) {
        Optional<Doctor> optionalDoctor = findAuthenticatedDoctor(session);
        if (optionalDoctor.isEmpty()) return "redirect:/auth/login";

        Optional<Consultation> optionalConsultation = consultationRepository.findById(id);
        if (optionalConsultation.isPresent()) {
            Consultation consultation = optionalConsultation.get();
            // Pastikan konsultasi ini milik dokter tersebut dan statusnya WAITING
            if (consultation.getDoctor().getUserId().equals(optionalDoctor.get().getUserId()) && consultation.getStatus() == ConsultationStatus.WAITING) {
                consultation.setStatus(ConsultationStatus.IN_PROGRESS);
                consultationRepository.save(consultation);
            }
            return "redirect:/doctor/live-konsultasi/" + consultation.getId();
        }
        return "redirect:/doctor/antrean";
    }

    @PostMapping("/prescription/create")
    public String createPrescription(
            @RequestParam Long consultationId,
            @RequestParam java.util.List<Long> medicineIds,
            @RequestParam java.util.List<String> instructions,
            @RequestParam java.util.List<Integer> quantities,
            HttpSession session) {
        
        Optional<com.telemedclinic.consultation.model.Consultation> optConsultation = consultationRepository.findById(consultationId);
        if (optConsultation.isEmpty()) return "redirect:/doctor/antrean";
        
        com.telemedclinic.consultation.model.Consultation consultation = optConsultation.get();
        
        // Buat objek Prescription baru
        com.telemedclinic.prescription.model.Prescription prescription = new com.telemedclinic.prescription.model.Prescription(consultation.getDoctor(), consultation.getCustomer());
        prescription.setConsultation(consultation);
        
        boolean allStockAvailable = true;
        java.util.List<com.telemedclinic.prescription.model.PrescriptionItem> items = new java.util.ArrayList<>();
        
        for (int i = 0; i < medicineIds.size(); i++) {
            com.telemedclinic.prescription.model.PrescriptionItem item = new com.telemedclinic.prescription.model.PrescriptionItem();
            // Ambil medicine dari repository
            Optional<com.telemedclinic.medicine.entity.Medicine> medOpt = medicineRepository.findById(medicineIds.get(i));
            if (medOpt.isPresent()) {
                com.telemedclinic.medicine.entity.Medicine med = medOpt.get();
                int requestedQty = quantities.get(i);

                // Cek apakah stok fisik obat mencukupi
                if (med.getStock() == null || med.getStock() < requestedQty) {
                    allStockAvailable = false;
                }

                item.setMedicine(med);
                item.setInstructions(instructions.get(i));
                item.setQuantity(requestedQty);
                item.setPrescription(prescription);
                items.add(item);
            }
        }
        
        prescription.setIsStockAvailable(allStockAvailable);
        prescription.setItems(items);
        prescriptionRepository.save(prescription);

        consultation.setPrescription(true);
        consultationRepository.save(consultation);
        
        // Kirim sinyal Prescription Ready ke WebSocket
        com.telemedclinic.consultation.model.ChatMessage systemMsg = new com.telemedclinic.consultation.model.ChatMessage();
        systemMsg.setSenderId("SYSTEM");
        systemMsg.setSenderRole("SYSTEM");
        systemMsg.setContent("PRESCRIPTION_READY");
        messagingTemplate.convertAndSend("/topic/consultation/" + consultationId, systemMsg);
        
        return "redirect:/doctor/live-konsultasi/" + consultationId;
    }

    @PostMapping("/live-konsultasi/{id}/end")
    public String endConsultation(HttpSession session, @PathVariable Long id) {
        Optional<Doctor> optionalDoctor = findAuthenticatedDoctor(session);
        if (optionalDoctor.isEmpty()) return "redirect:/auth/login";

        Optional<Consultation> optionalConsultation = consultationRepository.findById(id);
        if (optionalConsultation.isPresent()) {
            Consultation consultation = optionalConsultation.get();
            // Validasi: Milik dokter ini dan sedang IN_PROGRESS
            if (consultation.getDoctor().getUserId().equals(optionalDoctor.get().getUserId()) && 
                consultation.getStatus() == ConsultationStatus.IN_PROGRESS) {
                
                consultation.setStatus(ConsultationStatus.COMPLETED);
                consultationRepository.save(consultation);
                
                // Tambahkan tarif konsultasi ke balance dokter
                Doctor doctor = consultation.getDoctor();
                if (doctor != null) {
                    double fee = doctor.getConsultationFee() != null ? doctor.getConsultationFee() : 0.0;
                    double currentBalance = doctor.getBalance() != null ? doctor.getBalance() : 0.0;
                    doctor.setBalance(currentBalance + fee);
                    doctorRepository.save(doctor);
                }
                
                // Kirim sinyal Kill-Switch ke WebSocket
                com.telemedclinic.consultation.model.ChatMessage systemMessage = new com.telemedclinic.consultation.model.ChatMessage();
                systemMessage.setSenderId("SYSTEM");
                systemMessage.setSenderRole("SYSTEM");
                systemMessage.setContent("SESSION_ENDED");
                messagingTemplate.convertAndSend("/topic/consultation/" + consultation.getId(), systemMessage);
            }
            return "redirect:/doctor/live-konsultasi/" + consultation.getId();
        }
        return "redirect:/doctor/antrean";
    }

    @GetMapping("/live-konsultasi")
    public String smartLiveConsultationShortcut(HttpSession session, Model model) {
        Optional<Doctor> optionalDoctor = findAuthenticatedDoctor(session);
        if (optionalDoctor.isEmpty()) return "redirect:/auth/login";

        // Cari sesi aktif
        Optional<Consultation> activeSession = consultationRepository.findByDoctorUserIdAndStatusOrderByCreatedAtDesc(optionalDoctor.get().getUserId(), ConsultationStatus.IN_PROGRESS).stream().findFirst();
        
        if (activeSession.isPresent()) {
            // Redirect langsung ke ruangan yang sedang aktif
            return "redirect:/doctor/live-konsultasi/" + activeSession.get().getId();
        } else {
            // Tidak ada sesi aktif
            model.addAttribute("noActiveSession", true);
            model.addAttribute("activeSection", "live");
            return "doctor/doctor-live-konsultasi";
        }
    }

    @GetMapping("/live-konsultasi/{id}")
    public String showLiveConsultationRoom(HttpSession session, Model model, @PathVariable Long id) {
        Optional<Doctor> optionalDoctor = findAuthenticatedDoctor(session);
        if (optionalDoctor.isEmpty()) return "redirect:/auth/login";

        Optional<Consultation> optionalConsultation = consultationRepository.findById(id);
        // Validasi: Pastikan konsultasi ada dan milik dokter ini
        if (optionalConsultation.isEmpty() || !optionalConsultation.get().getDoctor().getUserId().equals(optionalDoctor.get().getUserId())) {
            return "redirect:/doctor/dashboard";
        }

        Consultation consultation = optionalConsultation.get();
        model.addAttribute("consultation", consultation);
        model.addAttribute("patient", consultation.getCustomer());
        model.addAttribute("doctor", optionalDoctor.get());
        model.addAttribute("activeSection", "live");
        
        java.util.List<com.telemedclinic.consultation.model.ChatMessage> messages = chatMessageRepository.findByConsultation_IdOrderByCreatedAtAsc(consultation.getId());
        model.addAttribute("messages", messages);
        model.addAttribute("currentUserEmail", session.getAttribute("currentUserEmail"));
        
        Optional<com.telemedclinic.prescription.model.Prescription> prescription = prescriptionRepository.findByConsultationId(consultation.getId());
        prescription.ifPresent(value -> model.addAttribute("prescription", value));
        
        return "doctor/doctor-live-konsultasi";
    }

    public static class WithdrawalRecord implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private String date;
        private double amount;
        private String method;
        private String status;
        private String reference;

        public WithdrawalRecord(String date, double amount, String method, String status, String reference) {
            this.date = date;
            this.amount = amount;
            this.method = method;
            this.status = status;
            this.reference = reference;
        }

        public String getDate() { return date; }
        public double getAmount() { return amount; }
        public String getMethod() { return method; }
        public String getStatus() { return status; }
        public String getReference() { return reference; }
    }

    @GetMapping("/earnings")
    public String earnings(HttpSession session, Model model) {
        Optional<Doctor> optionalDoctor = findAuthenticatedDoctor(session);
        if (optionalDoctor.isEmpty()) {
            return "redirect:/auth/login";
        }

        Doctor doctor = optionalDoctor.get();
        Long doctorId = doctor.getUserId();

        // Get completed consultations
        List<Consultation> completedConsultations = consultationRepository
                .findByDoctorUserIdAndStatusOrderByCreatedAtDesc(doctorId, ConsultationStatus.COMPLETED);

        // Calculate statistics
        double fee = doctor.getConsultationFee();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime startOfThisMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        java.time.LocalDateTime startOfLastMonth = startOfThisMonth.minusMonths(1);
        java.time.LocalDateTime startOf3MonthsAgo = startOfThisMonth.minusMonths(2);

        double earningsThisMonth = 0;
        double earningsLastMonth = 0;
        double earnings3Months = 0;

        for (Consultation c : completedConsultations) {
            if (c.getCreatedAt() != null) {
                if (c.getCreatedAt().isAfter(startOfThisMonth)) {
                    earningsThisMonth += fee;
                } else if (c.getCreatedAt().isAfter(startOfLastMonth)) {
                    earningsLastMonth += fee;
                }
                
                if (c.getCreatedAt().isAfter(startOf3MonthsAgo)) {
                    earnings3Months += fee;
                }
            }
        }

        // Handle simulated withdrawals history in Session
        java.util.List<WithdrawalRecord> withdrawals = (java.util.List<WithdrawalRecord>) session.getAttribute("simulatedWithdrawals");
        if (withdrawals == null) {
            withdrawals = new java.util.ArrayList<>();
            // Add some initial mock history records
            withdrawals.add(new WithdrawalRecord("20 Nov 2024", 10000000.0, "Transfer Bank (BNI)", "Berhasil", "TRX-20241120-001"));
            withdrawals.add(new WithdrawalRecord("10 Nov 2024", 5000000.0, "Transfer Bank (Mandiri)", "Berhasil", "TRX-20241110-002"));
            session.setAttribute("simulatedWithdrawals", withdrawals);
        }

        model.addAttribute("doctor", doctor);
        model.addAttribute("completedConsultations", completedConsultations);
        model.addAttribute("withdrawals", withdrawals);
        model.addAttribute("earningsThisMonth", earningsThisMonth);
        model.addAttribute("earningsLastMonth", earningsLastMonth);
        model.addAttribute("earnings3Months", earnings3Months);

        return "doctor/doctor-earnings";
    }

    @PostMapping("/withdraw")
    public String withdraw(
            @RequestParam Double amount,
            @RequestParam String method,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Optional<Doctor> optionalDoctor = findAuthenticatedDoctor(session);
        if (optionalDoctor.isEmpty()) {
            return "redirect:/auth/login";
        }

        Doctor doctor = optionalDoctor.get();
        if (amount == null || amount < 50000) {
            redirectAttributes.addFlashAttribute("errorMessage", "Minimal penarikan adalah Rp 50.000");
            return "redirect:/doctor/earnings";
        }
        if (amount > doctor.getBalance()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Saldo tidak mencukupi untuk melakukan penarikan");
            return "redirect:/doctor/earnings";
        }

        try {
            // Deduct doctor balance
            doctor.setBalance(doctor.getBalance() - amount);
            doctorRepository.save(doctor);

            // Record simulated withdrawal transaction in session
            java.util.List<WithdrawalRecord> withdrawals = (java.util.List<WithdrawalRecord>) session.getAttribute("simulatedWithdrawals");
            if (withdrawals == null) {
                withdrawals = new java.util.ArrayList<>();
            }
            
            java.time.LocalDate today = java.time.LocalDate.now();
            java.util.Locale localeId = java.util.Locale.forLanguageTag("id-ID");
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy", localeId);
            String formattedDate = today.format(formatter);

            String refNumber = "TRX-" + today.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + String.format("%03d", withdrawals.size() + 1);

            withdrawals.add(0, new WithdrawalRecord(formattedDate, amount, method, "Berhasil", refNumber));
            session.setAttribute("simulatedWithdrawals", withdrawals);

            redirectAttributes.addFlashAttribute("successMessage", "Penarikan dana sebesar Rp " + String.format("%,.0f", amount) + " berhasil diproses!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal memproses penarikan: " + e.getMessage());
        }

        return "redirect:/doctor/earnings";
    }

    @PostMapping("/status/toggle")
    @ResponseBody
    public ResponseEntity<?> togglePracticeStatus(HttpSession session) {
        Optional<Doctor> optionalDoctor = findAuthenticatedDoctor(session);
        if (optionalDoctor.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        Doctor doctor = optionalDoctor.get();
        doctor.setPracticeActive(!doctor.isPracticeActive());
        doctorRepository.save(doctor);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "practiceActive", doctor.isPracticeActive()
        ));
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam String phoneNumber,
            @RequestParam(required = false) String licenseNumber,
            @RequestParam(required = false) org.springframework.web.multipart.MultipartFile profilePhoto,
            @RequestParam(required = false) org.springframework.web.multipart.MultipartFile sipDocument,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Optional<Doctor> optionalDoctor = findAuthenticatedDoctor(session);
        if (optionalDoctor.isEmpty()) {
            return "redirect:/auth/login";
        }
        try {
            Doctor doctor = optionalDoctor.get();
            doctor.setPhoneNumber(phoneNumber);

            if (licenseNumber != null && !licenseNumber.isBlank()) {
                doctor.setLicenseNumber(licenseNumber);
            }

            if (profilePhoto != null && !profilePhoto.isEmpty()) {
                String fileName = org.springframework.util.StringUtils.cleanPath(profilePhoto.getOriginalFilename());
                String uniqueFileName = java.util.UUID.randomUUID().toString() + "_" + fileName;
                String uploadDir = "uploads/profile-photos";
                java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);

                if (!java.nio.file.Files.exists(uploadPath)) {
                    java.nio.file.Files.createDirectories(uploadPath);
                }

                java.nio.file.Path filePath = uploadPath.resolve(uniqueFileName);
                java.nio.file.Files.copy(profilePhoto.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                
                doctor.setProfilePhotoPath("/uploads/profile-photos/" + uniqueFileName);
            }

            if (sipDocument != null && !sipDocument.isEmpty()) {
                String fileName = org.springframework.util.StringUtils.cleanPath(sipDocument.getOriginalFilename());
                String uniqueFileName = java.util.UUID.randomUUID().toString() + "_" + fileName;
                String uploadDir = "uploads/sip-documents";
                java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);

                if (!java.nio.file.Files.exists(uploadPath)) {
                    java.nio.file.Files.createDirectories(uploadPath);
                }

                java.nio.file.Path filePath = uploadPath.resolve(uniqueFileName);
                java.nio.file.Files.copy(sipDocument.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                
                doctor.setSipDocumentPath("/uploads/sip-documents/" + uniqueFileName);
            }

            doctorRepository.save(doctor);
            redirectAttributes.addFlashAttribute("successMessage", "Profil berhasil diperbarui.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal memperbarui profil: " + e.getMessage());
        }
        return "redirect:/doctor/profile";
    }

    @PostMapping("/profile/update-fee")
    public String updateConsultationFee(
            @RequestParam Double consultationFee,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Optional<Doctor> optionalDoctor = findAuthenticatedDoctor(session);
        if (optionalDoctor.isEmpty()) {
            return "redirect:/auth/login";
        }
        
        if (consultationFee == null || consultationFee < 100000 || consultationFee > 500000) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tarif harus berada di antara Rp 100.000 dan Rp 500.000");
            return "redirect:/doctor/earnings";
        }

        try {
            Doctor doctor = optionalDoctor.get();
            doctor.setConsultationFee(consultationFee);
            doctorRepository.save(doctor);
            redirectAttributes.addFlashAttribute("successMessage", "Tarif konsultasi berhasil diperbarui.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal memperbarui tarif: " + e.getMessage());
        }
        return "redirect:/doctor/earnings";
    }
}
