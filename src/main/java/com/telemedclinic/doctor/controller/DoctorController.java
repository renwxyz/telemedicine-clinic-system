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
        model.addAttribute("doctor", doctor);
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

        List<Consultation> queue = consultationRepository.findByDoctorUserIdAndStatusInOrderByCreatedAtAsc(doctor.getUserId(), java.util.Arrays.asList(ConsultationStatus.WAITING, ConsultationStatus.IN_PROGRESS, ConsultationStatus.COMPLETED));
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

    @GetMapping("/earnings")
    public String earnings(HttpSession session, Model model) {
        Optional<Doctor> optionalDoctor = findAuthenticatedDoctor(session);
        if (optionalDoctor.isEmpty()) {
            return "redirect:/auth/login";
        }

        model.addAttribute("doctor", optionalDoctor.get());
        return "doctor/doctor-earnings";
    }
}
