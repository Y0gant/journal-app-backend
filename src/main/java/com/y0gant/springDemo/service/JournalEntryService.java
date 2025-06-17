package com.y0gant.springDemo.service;

import com.y0gant.springDemo.entity.JournalEntry;
import com.y0gant.springDemo.entity.User;
import com.y0gant.springDemo.repository.JournalEntryRepo;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JournalEntryService {
    private final JournalEntryRepo repo;
    private final UserService userService;

    JournalEntryService(JournalEntryRepo repo, UserService userService) {
        this.repo = repo;
        this.userService = userService;
    }

    private JournalEntry prepareEntry(JournalEntry entry) {
        if (entry.getDate() == null) {
            entry.setDate(LocalDateTime.now());
        }
        return entry;
    }

    public Optional<JournalEntry> saveEntry(JournalEntry entry, Long id) {
        Optional<User> userOptional = userService.getById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            JournalEntry entryToBeSaved = repo.save(prepareEntry(entry));
            user.getJournalEntries().add(entryToBeSaved);
            userService.saveUser(user);
            return Optional.of(entryToBeSaved);
        }
        return Optional.empty();
    }

    public Optional<JournalEntry> getById(long id) {
        return repo.findById(id);
    }

    public Optional<JournalEntry> updateJournalById(long id, JournalEntry entryToUpdate) {
        return repo.findById(id).map(existing -> {
            if (entryToUpdate.getTitle() != null && !entryToUpdate.getTitle().isEmpty())
                existing.setTitle(entryToUpdate.getTitle());
            if (entryToUpdate.getContent() != null) existing.setContent(entryToUpdate.getContent());
            if (entryToUpdate.getDate() != null) existing.setDate(entryToUpdate.getDate());
            return repo.save(existing);
        });
    }

    public boolean deleteById(long id, Long userId) {
        Optional<User> userOptional = userService.getById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.getJournalEntries().removeIf(x -> x.getId() == id);
            userService.saveUser(user);
            repo.deleteById(id);
            return true;
        }
        return false;
    }

    public List<JournalEntry> saveMultipleEntries(List<JournalEntry> entries, Long id) {
        Optional<User> userOptional = userService.getById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            List<JournalEntry> entryToBeSaved = repo.saveAll(entries.stream()
                    .map(this::prepareEntry)
                    .collect(Collectors.toList()));
            user.getJournalEntries().addAll(entryToBeSaved);
            userService.saveUser(user);
            return entryToBeSaved;
        }
        return Collections.emptyList();
    }

}
