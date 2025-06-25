package com.yogant.journal.controller;

import com.yogant.journal.entity.JournalEntry;
import com.yogant.journal.entity.User;
import com.yogant.journal.service.JournalEntryService;
import com.yogant.journal.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/journal")
public class JournalEntryControllerV2 {

    private final JournalEntryService journalEntryService;
    private final UserService userService;

    public JournalEntryControllerV2(JournalEntryService journalEntryService, UserService userService) {
        this.journalEntryService = journalEntryService;
        this.userService = userService;
    }


    @GetMapping
    public ResponseEntity<List<JournalEntry>> getAll() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOptional = userService.getByUsername(auth.getName());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return ResponseEntity.ok(user.getJournalEntries());
        } else return ResponseEntity.notFound().build();
    }

    @GetMapping("id/{id}")
    public ResponseEntity<JournalEntry> getJournalById(@PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Optional<User> userOptional = userService.getByUsername(auth.getName());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<JournalEntry> journalEntryOptional = journalEntryService.getById(id);
        if (journalEntryOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();
        JournalEntry entry = journalEntryOptional.get();

        if (user.getJournalEntries().contains(entry)) {
            return ResponseEntity.ok(entry);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


    @PostMapping
    public ResponseEntity<JournalEntry> createEntry(@RequestBody JournalEntry entry) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return journalEntryService.saveEntry(entry, auth.getName()).map(ResponseEntity::ok).orElse(ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteJournalById(@PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOptional = userService.getByUsername(auth.getName());
        String userName = userOptional.map(User::getUserName).orElse("");
        boolean deleted = journalEntryService.deleteById(id, userName);
        if (deleted) {
            return ResponseEntity.status(HttpStatus.OK).body(true);
        } else return ResponseEntity.notFound().build();
    }


    @PutMapping("/{id}")
    public ResponseEntity<JournalEntry> updateJournalWithId(@PathVariable String id, @RequestBody JournalEntry entry) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOptional = userService.getByUsername(auth.getName());
        String userName = userOptional.map(User::getUserName).orElse("");
        return journalEntryService.updateJournalById(userName, id, entry).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/multiple")
    public ResponseEntity<List<JournalEntry>> createMultipleEntries(@RequestBody List<JournalEntry> entries) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOptional = userService.getByUsername(auth.getName());
        String userName = userOptional.map(User::getUserName).orElse("");
        return ResponseEntity.status(HttpStatus.CREATED).body(journalEntryService.saveMultipleEntries(entries, userName));
    }
}