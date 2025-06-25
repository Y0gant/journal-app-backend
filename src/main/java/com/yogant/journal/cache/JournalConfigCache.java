package com.yogant.journal.cache;

import com.yogant.journal.entity.JournalConfig;
import com.yogant.journal.repository.JournalConfigRepo;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class JournalConfigCache {
    private final JournalConfigRepo configRepo;
    @Getter
    Map<String, String> configurations;

    public JournalConfigCache(JournalConfigRepo configRepo) {
        this.configRepo = configRepo;
    }

    @PostConstruct
    @Scheduled(cron = "${scheduler.cacheCron}")
    public void init() {
        log.info("Trying to initialize configurations from database");
        configurations = new HashMap<>();
        List<JournalConfig> journalConfigs = configRepo.findAll();
        for (JournalConfig config : journalConfigs) {
            configurations.put(config.getKey(), config.getValue());
        }
        if (configurations.isEmpty()) {
            log.info("No configurations were available");
        } else log.info("Successfully retrieved {} number of configurations", configurations.size());
    }

    public String saveNewConfig(JournalConfig config) {
        log.info("Trying to save new configuration");
        try {
            configRepo.save(config);
        } catch (Exception e) {
            log.error("Failed to save new configuration");
            return "Failed";
        }
        log.info("successfully saved new configuration");
        return "Success";
    }
}
