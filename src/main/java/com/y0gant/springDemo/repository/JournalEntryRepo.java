package com.y0gant.springDemo.repository;

import com.y0gant.springDemo.entity.JournalEntry;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface JournalEntryRepo extends MongoRepository<JournalEntry, Long> {
    void deleteJournalEntriesByIdIsTrue(long id);

    Optional<JournalEntry> findJournalEntriesById(String id);

    void deleteById(String id);

    void deleteAllById(List<String> id);


}
