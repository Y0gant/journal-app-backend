package com.yogant.journal.service;

import com.yogant.journal.entity.JournalEntry;
import com.yogant.journal.entity.User;
import com.yogant.journal.model.LoginDTO;
import com.yogant.journal.model.SaveNewUserDTO;
import com.yogant.journal.repository.JournalEntryRepo;
import com.yogant.journal.repository.UserRepo;
import com.yogant.journal.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class UserService {
    private final UserRepo repo;
    private final JournalEntryRepo journalEntryRepo;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserRepo userRepo, JournalEntryRepo journalEntryRepo, JwtUtils jwtUtils) {
        this.repo = userRepo;
        this.journalEntryRepo = journalEntryRepo;
        this.jwtUtils = jwtUtils;
    }

    @Transactional
    public Optional<User> saveNewUser(SaveNewUserDTO userToSave) {
        User user = new User();
        String email = userToSave.getEmail();
        String userName = userToSave.getUserName();
        String password = userToSave.getPassword();
        if (userName == null || userName.isBlank() || password == null || password.isBlank()) {
            log.warn("Either of user: {} or password: {} isn't provided or is blank/empty", userName, password);
            throw new DataIntegrityViolationException("Illegal data constraints userName and password");
        }
        user.setUserName(userName);
        user.setPassword(password);
        if (email != null && !email.isBlank()) user.setEmail(email);
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
    public Optional<User> updateUser(String userName, SaveNewUserDTO userToUpdate) {
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
            boolean newSentiment = userToUpdate.isSentimentAnalysis();
            String newEmail = userToUpdate.getEmail();

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
            if (newSentiment != existing.isSentimentAnalysis()) {
                log.debug("Updating sentimentAnalysis for user [{}]", existing.getUserName());
                existing.setSentimentAnalysis(newSentiment);
                updated = true;
            }
            if (newEmail != null && !newEmail.isBlank()) {
                log.debug("Updating email for user [{}]", existing.getUserName());
                existing.setEmail(newEmail);
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
            List<String> journalEntryIds = user.getJournalEntries().stream().map(JournalEntry::getId).toList();
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

    public Optional<User> grantAdminPrivilege(String userName) {
        log.info("Attempting to grant admin privileges to user with username: {}", userName);
        try {
            User user = repo.findByUserName(userName);
            if (user == null || user.getRoles() == null) {
                throw new IllegalStateException("User or roles not found");
            }

            List<String> roles = user.getRoles();
            boolean hasAdmin = roles.stream().anyMatch(role -> role.equalsIgnoreCase("ADMIN"));

            if (hasAdmin) {
                log.info("User [{}] already has ADMIN privileges. No changes made.", userName);
                return Optional.of(user);
            }

            roles.add("ADMIN");
            repo.save(user);
            log.info("ADMIN role successfully granted to user [{}]", userName);
            return Optional.of(user);

        } catch (UsernameNotFoundException e) {
            log.warn("User not found [{}]: {}", userName, e.getMessage());
            throw new RuntimeException("User not found: " + userName, e);

        } catch (DataIntegrityViolationException e) {
            log.warn("Data integrity violation while granting ADMIN to [{}]: {}", userName, e.getMessage());
            throw new RuntimeException("Data integrity violation while granting admin", e);

        } catch (Exception e) {
            log.error("Unexpected error while granting admin to [{}]: {}", userName, e.getMessage(), e);
            throw new RuntimeException("Failed to grant admin role", e);
        }
    }

    public Optional<User> revokeAdminPrivilege(String userName) {
        log.info("Attempting to revoke admin privileges for user: {}", userName);
        try {
            User user = repo.findByUserName(userName);
            if (user == null || user.getRoles() == null) {
                throw new IllegalStateException("User or roles not found");
            }

            List<String> roles = user.getRoles();
            boolean isAdmin = roles.stream().anyMatch(role -> role.equalsIgnoreCase("ADMIN"));

            if (!isAdmin) {
                log.info("User [{}] does not have ADMIN privileges. No changes made.", userName);
                return Optional.of(user);
            }

            roles.removeIf(role -> role != null && role.equalsIgnoreCase("ADMIN"));
            repo.save(user);
            log.info("ADMIN role successfully revoked for user [{}]", userName);
            return Optional.of(user);

        } catch (UsernameNotFoundException e) {
            log.warn("User not found [{}]: {}", userName, e.getMessage());
            throw new RuntimeException("User not found: " + userName, e);

        } catch (DataIntegrityViolationException e) {
            log.warn("Data integrity violation while revoking ADMIN for [{}]: {}", userName, e.getMessage());
            throw new RuntimeException("Data integrity violation while revoking admin", e);

        } catch (Exception e) {
            log.error("Unexpected error while revoking admin from [{}]: {}", userName, e.getMessage(), e);
            throw new RuntimeException("Failed to revoke admin role", e);
        }
    }

    public String generateJwtForLogin(LoginDTO loginUser) {
        User user = repo.findByUserName(loginUser.getUserName());
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + loginUser.getUserName());
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("sentiment_analysis", user.isSentimentAnalysis());
        claims.put("roles", user.getRoles());
        return jwtUtils.generateToken(user.getUserName(), claims);
    }

}
