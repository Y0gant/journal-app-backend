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


    @GetMapping("/{userName}")
    public ResponseEntity<List<JournalEntry>> getAll(@PathVariable String userName) {
        Optional<User> userOptional = userService.getByUsername(userName);
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

    @PostMapping("/{userName}")
    public ResponseEntity<JournalEntry> createEntry(@RequestBody JournalEntry entry, @PathVariable String userName) {
        return journalEntryService.saveEntry(entry, userName).map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/{userName}/{id}")
    public ResponseEntity<Boolean> deleteJournalById(@PathVariable long id, @PathVariable String userName) {
        boolean deleted = journalEntryService.deleteById(id, userName);
        if (deleted) {
            return ResponseEntity.status(HttpStatus.OK).body(true);
        } else return ResponseEntity.notFound().build();
    }


    @PutMapping("{userName}/{id}")
    public ResponseEntity<JournalEntry> updateJournalWithId(@PathVariable String userName, @PathVariable long id, @RequestBody JournalEntry entry) {
        return journalEntryService.updateJournalById(userName, id, entry).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/multiple/{userName}")
    public ResponseEntity<List<JournalEntry>> createMultipleEntries(@RequestBody List<JournalEntry> entries, @PathVariable String userName) {
        return ResponseEntity.status(HttpStatus.CREATED).body(journalEntryService.saveMultipleEntries(entries, userName));
    }
}