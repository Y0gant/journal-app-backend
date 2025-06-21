package com.y0gant.springDemo.service;

import com.y0gant.springDemo.entity.JournalEntry;
import com.y0gant.springDemo.entity.User;
import com.y0gant.springDemo.repository.JournalEntryRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JournalEntryService {
    private final JournalEntryRepo repo;
    private final UserService userService;

    JournalEntryService(JournalEntryRepo repo, UserService userService) {
        this.repo = repo;
        this.userService = userService;
    }

    private JournalEntry prepareEntry(JournalEntry entry) {
        if (entry.getDate() == null) {
            log.debug("Journal entry date not provided. Setting current timestamp.");
            entry.setDate(LocalDateTime.now());
        } else {
            log.debug("Journal entry date already set to: {}", entry.getDate());
        }
        return entry;
    }


    @Transactional
    public Optional<JournalEntry> saveEntry(JournalEntry entry, String userName) {
        log.info("Attempting to save journal entry for user [{}]", userName);
        Optional<User> userOptional = userService.getByUsername(userName);
        if (userOptional.isEmpty()) {
            log.warn("Journal entry save failed: user [{}] not found", userName);
            return Optional.empty();
        }
        try {
            User user = userOptional.get();
            JournalEntry preparedEntry = prepareEntry(entry);
            log.debug("Prepared journal entry for user [{}] with timestamp [{}]", userName, preparedEntry.getDate());
            JournalEntry savedEntry = repo.save(preparedEntry);
            log.info("Journal entry saved with ID [{}] for user [{}]", savedEntry.getId(), userName);
            user.getJournalEntries().add(savedEntry);
            userService.saveUser(user);
            log.info("Journal entry [{}] linked to user [{}]", savedEntry.getId(), userName);
            return Optional.of(savedEntry);
        } catch (Exception e) {
            log.error("Failed to save journal entry for user [{}]: {}", userName, e.getMessage(), e);
            throw new RuntimeException("Failed to save journal entry", e);
        }
    }

    public Optional<JournalEntry> getById(String id) {
        log.info("Fetching journal entry with ID [{}]", id);
        try {
            Optional<JournalEntry> entryOpt = repo.findJournalEntriesById(id);
            if (entryOpt.isPresent()) {
                log.info("Journal entry [{}] retrieved successfully.", id);
            } else {
                log.warn("Journal entry with ID [{}] not found.", id);
            }
            return entryOpt;
        } catch (Exception e) {
            log.error("Error retrieving journal entry [{}]: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve journal entry", e);
        }
    }


    @Transactional
    public Optional<JournalEntry> updateJournalById(String userName, String id, JournalEntry entryToUpdate) {
        log.info("Initiating update of journal entry [{}] for user [{}]", id, userName);

        Optional<User> userOptional = userService.getByUsername(userName);
        if (userOptional.isEmpty()) {
            log.warn("Update aborted: user [{}] not found", userName);
            return Optional.empty();
        }
        User user = userOptional.get();
        List<JournalEntry> entries = user.getJournalEntries();

        JournalEntry entry = entries.stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElse(null);
        if (entry == null) {
            log.warn("Journal entry [{}] not found for user [{}]", id, userName);
            return Optional.empty();
        }
        try {
            if (entryToUpdate.getTitle() != null && !entryToUpdate.getTitle().isBlank()) {
                log.debug("Updating title of entry [{}] for user [{}]", id, userName);
                entry.setTitle(entryToUpdate.getTitle());
            }
            if (entryToUpdate.getContent() != null) {
                log.debug("Updating content of entry [{}] for user [{}]", id, userName);
                entry.setContent(entryToUpdate.getContent());
            }
            if (entryToUpdate.getDate() != null) {
                log.debug("Updating date of entry [{}] for user [{}]", id, userName);
                entry.setDate(entryToUpdate.getDate());
            }
            JournalEntry updated = repo.save(entry);
            log.info("Successfully updated journal entry [{}] for user [{}]", id, userName);
            return Optional.of(updated);
        } catch (Exception e) {
            log.error("Error updating journal entry [{}] for user [{}]: {}", id, userName, e.getMessage(), e);
            throw new RuntimeException("Failed to update journal entry", e);
        }
    }


    @Transactional
    public boolean deleteById(String id, String userName) {
        log.info("Initiating deletion of journal entry [{}] for user [{}]", id, userName);

        Optional<User> userOptional = userService.getByUsername(userName);
        if (userOptional.isEmpty()) {
            log.warn("Deletion aborted: User [{}] not found", userName);
            return false;
        }

        User user = userOptional.get();
        boolean removed = user.getJournalEntries().removeIf(entry -> entry.getId().equals(id));

        if (!removed) {
            log.warn("Deletion aborted: Journal entry [{}] not found for user [{}]", id, userName);
            return false;
        }

        try {
            userService.saveUser(user);
            repo.deleteById(id);
            log.info("Successfully deleted journal entry [{}] for user [{}]", id, userName);
            return true;
        } catch (Exception e) {
            log.error("Error deleting journal entry [{}] for user [{}]: {}", id, userName, e.getMessage(), e);
            throw new RuntimeException("Failed to delete journal entry", e);
        }
    }


    @Transactional
    public List<JournalEntry> saveMultipleEntries(List<JournalEntry> entries, String userName) {
        if (entries == null || entries.isEmpty()) {
            log.warn("Batch journal entry save aborted: No entries provided for user [{}]", userName);
            return Collections.emptyList();
        }
        Optional<User> userOptional = userService.getByUsername(userName);
        if (userOptional.isEmpty()) {
            log.warn("Batch journal entry save aborted: User [{}] not found", userName);
            return Collections.emptyList();
        }
        try {
            User user = userOptional.get();
            log.info("Saving {} journal entries for user [{}]", entries.size(), userName);
            List<JournalEntry> preparedEntries = entries.stream()
                    .map(this::prepareEntry)
                    .collect(Collectors.toList());
            List<JournalEntry> savedEntries = repo.saveAll(preparedEntries);
            user.getJournalEntries().addAll(savedEntries);
            userService.saveUser(user);
            log.info("Successfully saved and linked {} journal entries for user [{}]", savedEntries.size(), userName);
            return savedEntries;
        } catch (Exception e) {
            log.error("Failed to save multiple journal entries for user [{}]: {}", userName, e.getMessage(), e);
            throw new RuntimeException("Failed to save journal entries", e);
        }
    }

}
