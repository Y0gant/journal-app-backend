package com.y0gant.springDemo.service;

import com.y0gant.springDemo.entity.JournalEntry;
import com.y0gant.springDemo.entity.User;
import com.y0gant.springDemo.repository.JournalEntryRepo;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public Optional<JournalEntry> saveEntry(JournalEntry entry, String userName) {
        Optional<User> userOptional = userService.getByUsername(userName);
        if (userOptional.isEmpty()) {
            return Optional.empty();
        }

        try {
            User user = userOptional.get();
            JournalEntry preparedEntry = prepareEntry(entry);
            JournalEntry savedEntry = repo.save(preparedEntry);

            user.getJournalEntries().add(savedEntry);
            userService.saveUser(user);

            return Optional.of(savedEntry);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save journal entry", e);
        }
    }

    public Optional<JournalEntry> getById(long id) {
        return repo.findById(id);
    }

    @Transactional
    public Optional<JournalEntry> updateJournalById(String userName, long id, JournalEntry entryToUpdate) {
        Optional<User> userOptional = userService.getByUsername(userName);
        List<JournalEntry> entries = userOptional.get().getJournalEntries();
        JournalEntry entry = null;
        for (JournalEntry entry1 : entries) {
            if (entry1.getId() == id) {
                entry = entry1;
                break;
            }

        }
        return Optional.ofNullable(entry).map(existing -> {
            if (entryToUpdate.getTitle() != null && !entryToUpdate.getTitle().isEmpty())
                existing.setTitle(entryToUpdate.getTitle());
            if (entryToUpdate.getContent() != null) existing.setContent(entryToUpdate.getContent());
            if (entryToUpdate.getDate() != null) existing.setDate(entryToUpdate.getDate());
            return repo.save(existing);
        });
    }

    @Transactional
    public boolean deleteById(long id, String userName) {
        Optional<User> userOptional = userService.getByUsername(userName);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.getJournalEntries().removeIf(x -> x.getId() == id);
            userService.saveUser(user);
            repo.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public List<JournalEntry> saveMultipleEntries(List<JournalEntry> entries, String userName) {
        if (entries == null || entries.isEmpty()) {
            return Collections.emptyList();
        }
        Optional<User> userOptional = userService.getByUsername(userName);
        if (userOptional.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            User user = userOptional.get();
            List<JournalEntry> entryToBeSaved = repo.saveAll(entries.stream()
                    .map(this::prepareEntry)
                    .collect(Collectors.toList()));
            user.getJournalEntries().addAll(entryToBeSaved);
            userService.saveUser(user);
            return entryToBeSaved;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
