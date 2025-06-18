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

    UserService(UserRepo userRepo, JournalEntryRepo journalEntryRepo) {
        this.repo = userRepo;
        this.journalEntryRepo = journalEntryRepo;

    }

    @Transactional
    public Optional<User> saveUser(User user) {
        try {
            user.setPassword(encoder.encode(user.getPassword()));
            user.setRoles(List.of("USER"));
            return Optional.of(repo.save(user));
        } catch (Exception e) {
            throw new RuntimeException("Failed to save user", e);
        }
    }

    public List<User> getAll() {
        return repo.findAll();
    }

    public Optional<User> getByUsername(String userName) {
        return Optional.ofNullable(repo.findByUserName(userName));
    }

    @Transactional
    public Optional<User> updateUser(String userName, User userToUpdate) {
        return Optional.of(repo.findByUserName(userName)).map(existing -> {
            if (!userToUpdate.getUserName().isEmpty()) existing.setUserName(userToUpdate.getUserName());
            if (!userToUpdate.getPassword().isEmpty()) existing.setPassword(userToUpdate.getPassword());
            return repo.save(existing);
        });
    }


    @Transactional
    public boolean deleteUserById(String userName) {
        Optional<User> userOpt = Optional.ofNullable(repo.findByUserName(userName));
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            List<Long> ids = user.getJournalEntries().stream().map(JournalEntry::getId).toList();
            journalEntryRepo.deleteAllById(ids);
            repo.deleteByUserName(userName);
            return true;
        }
        return false;
    }

}
