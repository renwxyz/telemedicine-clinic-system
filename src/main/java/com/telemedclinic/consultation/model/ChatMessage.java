package com.telemedclinic.consultation.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "consultation_id")
    private Consultation consultation;

    private String senderId; // Akan diisi dengan email user (principal.username)
    private String senderRole;
    private String content;
    private LocalDateTime createdAt;

    public ChatMessage() {}

    @PrePersist
    protected void prePersist() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    // BUAT GETTER DAN SETTER UNTUK SEMUA PROPERTI DI ATAS
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Consultation getConsultation() { return consultation; } public void setConsultation(Consultation consultation) { this.consultation = consultation; }
    public String getSenderId() { return senderId; } public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getSenderRole() { return senderRole; } public void setSenderRole(String senderRole) { this.senderRole = senderRole; }
    public String getContent() { return content; } public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
