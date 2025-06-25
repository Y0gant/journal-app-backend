package com.yogant.journal.scheduler;

import com.yogant.journal.entity.JournalEntry;
import com.yogant.journal.entity.User;
import com.yogant.journal.repository.UserRepoImpl;
import com.yogant.journal.service.EmailService;
import com.yogant.journal.service.SentimentService;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;


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

    @Scheduled(cron = "${scheduler.emailCron}")
    public void fetchUsersAndSendEmail() {
        List<User> userList = userRepository.getUsersWithSA();
        for (User user : userList) {
            List<JournalEntry> entries = user.getJournalEntries();
            List<String> filtered = entries.stream().filter(entry -> entry.getDate().isAfter(LocalDateTime.now().minusDays(7))).map(JournalEntry::getContent).toList();
            String entry = String.join(" ", filtered);
            int moodValue = sentimentService.getSentiment(entry);
            emailService.sendEmail(user.getEmail(), "Sentiment for this week's mood", mood[moodValue]);
        }
    }
}
