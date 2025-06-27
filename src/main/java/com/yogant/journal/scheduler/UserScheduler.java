package com.yogant.journal.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yogant.journal.entity.JournalEntry;
import com.yogant.journal.entity.User;
import com.yogant.journal.model.SentimentEmailDTO;
import com.yogant.journal.repository.UserRepoImpl;
import com.yogant.journal.service.EmailService;
import com.yogant.journal.service.SentimentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Component
@AllArgsConstructor
public class UserScheduler {

    private final String[] mood = {"joy",
            "anger",
            "curiosity",
            "sadness",
            "hope",
            "fear",
            "contentment",
            "frustration",
            "surprise",
            "love"};
    private UserRepoImpl userRepository;
    private EmailService emailService;
    private SentimentService sentimentService;
    private KafkaTemplate<String, String> kafkaTemplate;
    private ObjectMapper objectMapper;

    @Scheduled(cron = "${scheduler.emailCron}")
    public void fetchUsersAndSendEmail() {
        List<User> userList = userRepository.getUsersWithSA();
        for (User user : userList) {
            List<JournalEntry> entries = user.getJournalEntries();
            List<String> filtered = entries.stream().filter(entry -> entry.getDate().isAfter(LocalDateTime.now().minusDays(7))).map(JournalEntry::getContent).toList();
            String entry = String.join(" ", filtered);
            int moodValue = sentimentService.getSentiment(entry);
            SentimentEmailDTO data = new SentimentEmailDTO();
            data.setEmail(user.getEmail());
            data.setSubject("This weeks sentiment analysis");
            data.setBody(mood[moodValue]);
            try {
                String json = objectMapper.writeValueAsString(data);
                kafkaTemplate.send("sentiments", data.getEmail(), json);
            } catch (Exception e) {
                log.error("Error sending data through kafka {}", e.getMessage(), e);
                emailService.sendEmail(data.getEmail(), data.getSubject(), data.getBody());
            }
        }
    }
}
