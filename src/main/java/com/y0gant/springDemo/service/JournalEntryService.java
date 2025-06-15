package com.y0gant.springDemo.service;

import com.y0gant.springDemo.entity.JournalEntry;
import com.y0gant.springDemo.repository.JournalEntryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class JournalEntryService {
    private final JournalEntryRepo repo;

    JournalEntryService(@Autowired JournalEntryRepo repo) {
        this.repo = repo;
    }

    public JournalEntry saveEntry(JournalEntry entry) {
        if (entry.getDate() == null) {
            entry.setDate(LocalDateTime.now());
        }
        return repo.save(entry);
    }

    public List<JournalEntry> getAll() {
        return repo.findAll();
    }

    public Optional<JournalEntry> getById(long id) {
        return repo.findById(id);
    }

    public JournalEntry updateJournalById(long id, JournalEntry entryToUpdate) {
        Optional<JournalEntry> existing = repo.findById(id);

        if (existing.isEmpty()) {
            return null;
        }
        if (entryToUpdate.getTitle() != null) existing.get().setTitle(entryToUpdate.getTitle());
        if (entryToUpdate.getContent() != null) existing.get().setContent(entryToUpdate.getContent());
        if (entryToUpdate.getDate() != null) existing.get().setDate(entryToUpdate.getDate());

        return repo.save(existing.get());
    }

    public boolean deleteById(long id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return true;
        }
        return false;
    }

}
