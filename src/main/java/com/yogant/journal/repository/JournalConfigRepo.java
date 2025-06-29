package com.yogant.journal.repository;

import com.yogant.journal.entity.JournalConfig;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface JournalConfigRepo extends MongoRepository<JournalConfig, ObjectId> {

    Optional<JournalConfig> findByKey(String key);
}
