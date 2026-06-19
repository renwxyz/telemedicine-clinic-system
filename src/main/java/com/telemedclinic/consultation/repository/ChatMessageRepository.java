package com.telemedclinic.consultation.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.telemedclinic.consultation.model.ChatMessage;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByConsultation_IdOrderByCreatedAtAsc(Long consultationId);
}
