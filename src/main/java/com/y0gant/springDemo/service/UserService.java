package com.y0gant.springDemo.service;

import com.y0gant.springDemo.entity.JournalEntry;
import com.y0gant.springDemo.entity.User;
import com.y0gant.springDemo.repository.JournalEntryRepo;
import com.y0gant.springDemo.repository.UserRepo;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
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
        try {
            user.setPassword(encoder.encode(user.getPassword()));
            user.setRoles(List.of("USER"));
            return Optional.of(repo.save(user));
        } catch (Exception e) {
            throw new RuntimeException("Failed to save user", e);
        }
    }

    public void saveUser(User user) {
        repo.save(user);
    }

    public List<User> getAll() {
        return repo.findAll();
    }

    public Optional<User> getByUsername(String userName) {
        return Optional.ofNullable(repo.findByUserName(userName));
    }

    @Transactional
    public Optional<User> updateUser(String userName, User userToUpdate) {
        String userNameToUpdate = userToUpdate.getUserName();
        String passwordToUpdate = userToUpdate.getPassword();
        User existing = repo.findByUserName(userName);
        if (userNameToUpdate != null && !userNameToUpdate.isEmpty()) {
            existing.setUserName(userToUpdate.getUserName());
        }
        if (passwordToUpdate != null && !passwordToUpdate.isEmpty()) {
            existing.setPassword(encoder.encode(userToUpdate.getPassword()));
        }
        return Optional.of(repo.save(existing));
    }


    @Transactional
    public boolean deleteUserByUserName(String userName) {
        Optional<User> userOpt = Optional.ofNullable(repo.findByUserName(userName));
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            List<String> ids = user.getJournalEntries().stream().map(JournalEntry::getId).toList();
            journalEntryRepo.deleteAllById(ids);
            repo.deleteByUserName(userName);
            return true;
        }
        return false;
    }

    public Optional<User> saveAdmin(User user) {
        try {
            user.setPassword(encoder.encode(user.getPassword()));
            user.setRoles(List.of("ADMIN", "USER"));
            return Optional.of(repo.save(user));
        } catch (Exception e) {
            throw new RuntimeException("Failed to save user", e);
        }
    }
}
