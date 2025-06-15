package com.y0gant.springDemo.service;

import com.y0gant.springDemo.entity.JournalEntry;
import com.y0gant.springDemo.repository.JournalEntryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JournalEntryService {
    private final JournalEntryRepo repo;

    JournalEntryService(@Autowired JournalEntryRepo repo) {
        this.repo = repo;
    }

    public Optional<JournalEntry> saveEntry(JournalEntry entry) {
        return Optional.of(repo.save(prepareEntry(entry)));
    }

    public List<JournalEntry> getAll() {
        return repo.findAll();
    }

    public Optional<JournalEntry> getById(long id) {
        return repo.findById(id);
    }

    public Optional<JournalEntry> updateJournalById(long id, JournalEntry entryToUpdate) {
        return repo.findById(id).map(existing -> {
            if (entryToUpdate.getTitle() != null) existing.setTitle(entryToUpdate.getTitle());
            if (entryToUpdate.getContent() != null) existing.setContent(entryToUpdate.getContent());
            if (entryToUpdate.getDate() != null) existing.setDate(entryToUpdate.getDate());
            return repo.save(existing);
        });
    }


    public boolean deleteById(long id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return true;
        }
        return false;
    }

    private JournalEntry prepareEntry(JournalEntry entry) {
        if (entry.getDate() == null) {
            entry.setDate(LocalDateTime.now());
        }
        return entry;
    }

    public List<JournalEntry> saveMultipleEntries(List<JournalEntry> entries) {
        return repo.saveAll(entries.stream()
                .map(this::prepareEntry)
                .collect(Collectors.toList()));
    }

}
