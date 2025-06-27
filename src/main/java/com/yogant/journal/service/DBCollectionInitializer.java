package com.yogant.journal.service;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class DBCollectionInitializer {

    private MongoTemplate mongoTemplate;

    @PostConstruct
    public void createCollectionIfNotExists() {
        String configCollection = "configurations";
        if (!mongoTemplate.collectionExists(configCollection)) {
            mongoTemplate.createCollection(configCollection);
            log.info("Collection 'configurations' created.");
        } else {
            log.info("Collection 'configurations' already exists.");
        }
        String journalCollection = "journal_entries";
        if (!mongoTemplate.collectionExists(journalCollection)) {
            mongoTemplate.createCollection(journalCollection);
            log.info("Collection 'journal_entries' created.");
        } else {
            log.info("Collection 'journal_entries' already exists.");
        }
    }
}
