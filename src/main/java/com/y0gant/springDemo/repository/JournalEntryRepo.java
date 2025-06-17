package com.y0gant.springDemo.repository;

import com.y0gant.springDemo.entity.JournalEntry;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JournalEntryRepo extends MongoRepository<JournalEntry, Long> {
    void deleteJournalEntriesByIdIsTrue(long id);
}
