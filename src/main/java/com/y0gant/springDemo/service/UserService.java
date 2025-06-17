package com.y0gant.springDemo.service;

import com.y0gant.springDemo.entity.User;
import com.y0gant.springDemo.repository.JournalEntryRepo;
import com.y0gant.springDemo.repository.UserRepo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UserService {
    private final UserRepo repo;
    private final JournalEntryRepo journalEntryRepo;

    UserService(UserRepo userRepo,
                JournalEntryRepo journalEntryRepo) {
        this.repo = userRepo;
        this.journalEntryRepo = journalEntryRepo;
    }

    public Optional<User> saveUser(User entry) {
        try {
            return Optional.of(repo.save(entry));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<User> getAll() {
        return repo.findAll();
    }

    public Optional<User> getById(long id) {
        return repo.findById(id);
    }

    public Optional<User> updateUserById(long id, User userToUpdate) {
        return repo.findById(id).map(existing -> {
            existing.setUserName(userToUpdate.getUserName());
            existing.setPassword(userToUpdate.getPassword());
            return repo.save(existing);
        });
    }


    public boolean deleteUserById(long id) {
        Optional<User> userOpt = repo.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            journalEntryRepo.deleteAll(user.getJournalEntries());
            repo.deleteById(id);
            return true;
        }
        return false;
    }

}
