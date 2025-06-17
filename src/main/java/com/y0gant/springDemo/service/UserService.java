package com.y0gant.springDemo.service;

import com.y0gant.springDemo.entity.JournalEntry;
import com.y0gant.springDemo.entity.User;
import com.y0gant.springDemo.repository.JournalEntryRepo;
import com.y0gant.springDemo.repository.UserRepo;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserService {
    private final UserRepo repo;
    private final JournalEntryRepo journalEntryRepo;

    UserService(UserRepo userRepo,
                JournalEntryRepo journalEntryRepo) {
        this.repo = userRepo;
        this.journalEntryRepo = journalEntryRepo;
    }

    @Transactional
    public Optional<User> saveUser(User user) {
        try {
            if (user.getJournalEntries() != null || !(user.getJournalEntries().isEmpty())) {
                List<Long> existingIds = user.getJournalEntries().stream()
                        .map(JournalEntry::getId)
                        .collect(Collectors.toList());

                List<JournalEntry> existingEntries = journalEntryRepo.findAllById(existingIds);
                Set<Long> existingIdSet = existingEntries.stream()
                        .map(JournalEntry::getId)
                        .collect(Collectors.toSet());

                // Save only non-existing entries
                for (JournalEntry entry : user.getJournalEntries()) {
                    if (!existingIdSet.contains(entry.getId())) {
                        if (entry.getDate() == null) {
                            entry.setDate(LocalDateTime.now());
                        }
                        journalEntryRepo.save(entry);
                    }
                }
            }
            return Optional.of(repo.save(user));
        } catch (Exception e) {
            throw new RuntimeException("Failed to save user", e);
        }
    }

    public List<User> getAll() {
        return repo.findAll();
    }

    public Optional<User> getById(long id) {
        return repo.findById(id);
    }

    @Transactional
    public Optional<User> updateUserById(long id, User userToUpdate) {
        return repo.findById(id).map(existing -> {
            if (!userToUpdate.getUserName().isEmpty()) existing.setUserName(userToUpdate.getUserName());
            if (!userToUpdate.getPassword().isEmpty()) existing.setPassword(userToUpdate.getPassword());
            return repo.save(existing);
        });
    }


    @Transactional
    public boolean deleteUserById(long id) {
        Optional<User> userOpt = repo.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            List<Long> ids = user.getJournalEntries().stream().map(JournalEntry::getId).toList();
            journalEntryRepo.deleteAllById(ids);
            repo.deleteById(id);
            return true;
        }
        return false;
    }

}
