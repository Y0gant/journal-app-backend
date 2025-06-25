package com.yogant.journal.service;

import com.yogant.journal.entity.JournalEntry;
import com.yogant.journal.entity.User;
import com.yogant.journal.repository.JournalEntryRepo;
import com.yogant.journal.repository.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class UserService {
    private final UserRepo repo;
    private final JournalEntryRepo journalEntryRepo;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserRepo userRepo, JournalEntryRepo journalEntryRepo) {
        this.repo = userRepo;
        this.journalEntryRepo = journalEntryRepo;

    }

    @Transactional
    public Optional<User> saveNewUser(User user) {
        log.info("Initiating user creation for username: {}", user.getUserName());
        try {
            log.debug("Trying to save new user with user name {}", user.getUserName());
            user.setPassword(encoder.encode(user.getPassword()));
            user.setRoles(List.of("USER"));
            User savedUser = repo.save(user);
            log.info("User [{}] successfully saved with ID: {}", savedUser.getUserName(), savedUser.getId());
            return Optional.of(savedUser);
        } catch (DataIntegrityViolationException e) {
            log.warn("User creation failed for username [{}] - possible duplicate or constraint violation: {}", user.getUserName(), e.getMessage());
            throw new RuntimeException("User already exists or data integrity issue", e);
        } catch (Exception e) {
            log.error("Unexpected error during user save for [{}]: {}", user.getUserName(), e.getMessage(), e);
            throw new RuntimeException("Failed to save user", e);
        }
    }

    public void saveUser(User user) {
        log.debug("Trying to save existing user with user name {} ", user.getUserName());
        try {
            repo.save(user);
        } catch (DataIntegrityViolationException e) {
            log.warn("User creation failed for username [{}] - possible constraint violation: {}", user.getUserName(), e.getMessage());
            throw new RuntimeException("User already exists or data integrity issue", e);
        } catch (Exception e) {
            log.error("Unexpected error during user save for [{}]: {}", user.getUserName(), e.getMessage(), e);
            throw new RuntimeException("Failed to save user", e);
        }
    }

    public List<User> getAll() {
        log.info("Fetching all users from the database.");

        try {
            List<User> users = repo.findAll();
            log.info("Retrieved {} users from the database.", users.size());
            return users;
        } catch (Exception e) {
            log.error("Error occurred while retrieving all users: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch users", e);
        }
    }

    public Optional<User> getByUsername(String userName) {
        log.info("Attempting to retrieve user with username: {}", userName);
        try {
            User user = repo.findByUserName(userName);
            if (user != null) {
                log.info("User [{}] found in the database.", userName);
            } else {
                log.warn("User [{}] not found in the database.", userName);
            }
            return Optional.ofNullable(user);
        } catch (Exception e) {
            log.error("Error occurred while fetching user [{}]: {}", userName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve user", e);
        }
    }

    @Transactional
    public Optional<User> updateUser(String userName, User userToUpdate) {
        log.info("Initiating update for user [{}]", userName);

        try {
            User existing = repo.findByUserName(userName);
            if (existing == null) {
                log.warn("Update failed: User [{}] not found in the database.", userName);
                return Optional.empty();
            }

            boolean updated = false;

            String newUsername = userToUpdate.getUserName();
            String newPassword = userToUpdate.getPassword();

            if (newUsername != null && !newUsername.isBlank()) {
                log.debug("Updating username from [{}] to [{}]", existing.getUserName(), newUsername);
                existing.setUserName(newUsername);
                updated = true;
            }
            if (newPassword != null && !newPassword.isBlank()) {
                log.debug("Updating password for user [{}]", existing.getUserName());
                existing.setPassword(encoder.encode(newPassword));
                updated = true;
            }
            if (!updated) {
                log.info("No fields were updated for user [{}]. Skipping save.", userName);
                return Optional.of(existing);
            }
            User savedUser = repo.save(existing);
            log.info("Successfully updated user [{}] with ID [{}]", savedUser.getUserName(), savedUser.getId());
            return Optional.of(savedUser);
        } catch (Exception e) {
            log.error("Error occurred while updating user [{}]: {}", userName, e.getMessage(), e);
            throw new RuntimeException("Failed to update user", e);
        }
    }


    @Transactional
    public boolean deleteUserByUserName(String userName) {
        log.info("Initiating deletion process for user [{}]", userName);

        try {
            Optional<User> userOpt = Optional.ofNullable(repo.findByUserName(userName));
            if (userOpt.isEmpty()) {
                log.warn("Deletion aborted: User [{}] not found.", userName);
                return false;
            }
            User user = userOpt.get();
            List<String> journalEntryIds = user.getJournalEntries()
                    .stream()
                    .map(JournalEntry::getId)
                    .toList();
            if (!journalEntryIds.isEmpty()) {
                log.debug("Deleting {} journal entries for user [{}]", journalEntryIds.size(), userName);
                journalEntryRepo.deleteAllById(journalEntryIds);
                log.info("Journal entries deleted for user [{}]", userName);
            } else {
                log.debug("No journal entries to delete for user [{}]", userName);
            }
            repo.deleteByUserName(userName);
            log.info("User [{}] successfully deleted from the system.", userName);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete user [{}]: {}", userName, e.getMessage(), e);
            throw new RuntimeException("User deletion failed", e);
        }
    }

    public Optional<User> saveAdmin(User user) {
        log.info("Attempting to create a new admin user with username: {}", user.getUserName());
        try {
            user.setPassword(encoder.encode(user.getPassword()));
            user.setRoles(List.of("ADMIN", "USER"));

            User savedUser = repo.save(user);
            log.info("Admin user [{}] successfully saved with ID: {}", savedUser.getUserName(), savedUser.getId());
            return Optional.of(savedUser);

        } catch (DataIntegrityViolationException e) {
            log.warn("Admin user creation failed for [{}] - possible duplicate or constraint issue: {}", user.getUserName(), e.getMessage());
            throw new RuntimeException("Admin user already exists or data integrity violation", e);

        } catch (Exception e) {
            log.error("Unexpected error while saving admin user [{}]: {}", user.getUserName(), e.getMessage(), e);
            throw new RuntimeException("Failed to save admin user", e);
        }
    }
}
