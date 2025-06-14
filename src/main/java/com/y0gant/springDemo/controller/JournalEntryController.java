package com.y0gant.springDemo.controller;

import com.y0gant.springDemo.entity.JournalEntry;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/journal", "/"})
public class JournalEntryController {
    private Map<Long, JournalEntry> entries = new HashMap<>();

    @GetMapping
    public List<JournalEntry> getAll() {
        return new ArrayList<>(entries.values());
    }

    @GetMapping("id/{id}")
    public JournalEntry getById(@PathVariable Long id) {
        return entries.get(id);
    }

    @PostMapping
    public boolean createEntry(@RequestBody JournalEntry entry) {
        if (entry == null || entry.getId() == 0 || entry.getContent() == null || entry.getContent().isBlank()) {
            return false;
        }
        entries.put(entry.getId(), entry);
        return true;
    }

    @DeleteMapping("id/{id}")
    public JournalEntry deleteById(@PathVariable long id) {
        return entries.remove(id);
    }

    @PutMapping("id/{id}")
    public JournalEntry updateJournalWithId(@PathVariable long id, @RequestBody JournalEntry entry) {
        entries.put(id, entry);
        return entries.get(id);
    }
}