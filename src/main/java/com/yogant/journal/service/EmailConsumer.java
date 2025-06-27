package com.yogant.journal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yogant.journal.model.SentimentEmailDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class EmailConsumer {

    private EmailService emailService;
    private ObjectMapper objectMapper;


    @KafkaListener(topics = "sentiments", groupId = "weekly-sentiment-group")
    public void consumer(String payload) {
        try {
            SentimentEmailDTO data = objectMapper.readValue(payload, SentimentEmailDTO.class);
            sendEmail(data);
        } catch (Exception e) {
            log.error("Failed to deserialize message: {}", payload, e);
        }
    }

    public void sendEmail(SentimentEmailDTO data) {
        emailService.sendEmail(data.getEmail(), data.getSubject(), data.getBody());
    }
}
