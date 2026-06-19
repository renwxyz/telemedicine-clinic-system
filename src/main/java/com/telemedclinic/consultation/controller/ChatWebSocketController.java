package com.telemedclinic.consultation.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import com.telemedclinic.consultation.model.ChatMessage;
import com.telemedclinic.consultation.model.Consultation;
import com.telemedclinic.consultation.repository.ChatMessageRepository;
import com.telemedclinic.consultation.repository.ConsultationRepository;
import java.util.Optional;

@Controller
public class ChatWebSocketController {
    @Autowired private SimpMessagingTemplate messagingTemplate;
    @Autowired private ChatMessageRepository chatMessageRepository;
    @Autowired private ConsultationRepository consultationRepository;

    @MessageMapping("/chat/{consultationId}/sendMessage")
    public void sendMessage(@DestinationVariable Long consultationId, @Payload ChatMessage chatMessage) {
        Optional<Consultation> consultation = consultationRepository.findById(consultationId);
        if (consultation.isPresent()) {
            chatMessage.setConsultation(consultation.get());
            ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

            // Tembakkan pesan ke semua orang yang terhubung ke ruangan (topic) ini
            messagingTemplate.convertAndSend("/topic/consultation/" + consultationId, savedMessage);
        }
    }
}
