package com.y0gant.springDemo.controller;

import com.y0gant.springDemo.entity.JournalEntry;
import com.y0gant.springDemo.entity.User;
import com.y0gant.springDemo.service.JournalEntryService;
import com.y0gant.springDemo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping({"/journal", "/"})
public class JournalEntryControllerV2 {

    private final JournalEntryService journalEntryService;
    private final UserService userService;

    public JournalEntryControllerV2(JournalEntryService journalEntryService, UserService userService) {
        this.journalEntryService = journalEntryService;
        this.userService = userService;
    }


    @GetMapping("/{id}")
    public ResponseEntity<List<JournalEntry>> getAll(@PathVariable long id) {
        Optional<User> userOptional = userService.getById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return ResponseEntity.ok(user.getJournalEntries());
        } else return ResponseEntity.notFound().build();
    }

    @GetMapping("id/{id}")
    public ResponseEntity<JournalEntry> getJournalById(@PathVariable long id) {
        return journalEntryService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}")
    public ResponseEntity<JournalEntry> createEntry(@RequestBody JournalEntry entry, @PathVariable long id) {
        return journalEntryService.saveEntry(entry, id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @DeleteMapping("id/{userId}/{id}")
    public ResponseEntity<Boolean> deleteJournalById(@PathVariable long id, @PathVariable long userId) {
        boolean deleted = journalEntryService.deleteById(id, userId);
        if (deleted) {
            return ResponseEntity.status(HttpStatus.OK).body(true);
        } else return ResponseEntity.notFound().build();
    }


    @PutMapping("id/{id}")
    public ResponseEntity<JournalEntry> updateJournalWithId(@PathVariable long id, @RequestBody JournalEntry entry) {
        return journalEntryService.updateJournalById(id, entry).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/multiple/{id}")
    public ResponseEntity<List<JournalEntry>> createMultipleEntries(@RequestBody List<JournalEntry> entries, @PathVariable long id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(journalEntryService.saveMultipleEntries(entries, id));
    }
}