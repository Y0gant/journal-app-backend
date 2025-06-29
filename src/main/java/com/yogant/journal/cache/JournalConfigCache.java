package com.yogant.journal.cache;

import com.mongodb.DuplicateKeyException;
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
import java.util.Optional;

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
        log.info("Trying to save configuration for key: {}", config.getKey());

        try {
            Optional<JournalConfig> existingConfig = configRepo.findByKey(config.getKey());

            if (existingConfig.isPresent()) {
                JournalConfig configToUpdate = existingConfig.get();
                configToUpdate.setValue(config.getValue());
                configRepo.save(configToUpdate);
                log.info("Updated configuration for key: {}", config.getKey());
            } else {
                configRepo.save(config);
                log.info("Inserted new configuration for key: {}", config.getKey());
            }

            return "Success";
        } catch (DuplicateKeyException dke) {
            log.error("Duplicate key error while saving config with key: {}", config.getKey(), dke);
            return "Duplicate Key";
        } catch (Exception e) {
            log.error("Failed to save configuration for key: {}", config.getKey(), e);
            return "Failed";
        }
    }

}
