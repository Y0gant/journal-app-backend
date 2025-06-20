package com.y0gant.springDemo.service;

import com.y0gant.springDemo.entity.JournalEntry;
import com.y0gant.springDemo.entity.User;
import com.y0gant.springDemo.repository.JournalEntryRepo;
import com.y0gant.springDemo.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private JournalEntryRepo journalEntryRepo;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Test
    void testSaveNewUser_returnsUserWithHashedPassword() {
        User user = new User();
        user.setUserName("john");
        user.setPassword("1234");

        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<User> result = userService.saveNewUser(user);

        assertTrue(result.isPresent());
        User saved = result.get();

        assertEquals("john", saved.getUserName());
        assertTrue(saved.getRoles().contains("USER"));
        assertNotEquals("1234", saved.getPassword());
        assertTrue(new BCryptPasswordEncoder().matches("1234", saved.getPassword()));
    }

    @Test
    void testUpdateUser_changesUsernameAndPassword() {
        User existing = new User();
        existing.setUserName("old");
        existing.setPassword("oldpass");

        User update = new User();
        update.setUserName("new");
        update.setPassword("newpass");

        when(userRepo.findByUserName("old")).thenReturn(existing);
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<User> result = userService.updateUser("old", update);

        assertTrue(result.isPresent());
        assertEquals("new", result.get().getUserName());
        assertTrue(new BCryptPasswordEncoder().matches("newpass", result.get().getPassword()));
    }

    @Test
    void testDeleteUserByUserName_found_deletesJournalEntries() {
        JournalEntry j1 = new JournalEntry();
        j1.setId("1");
        JournalEntry j2 = new JournalEntry();
        j2.setId("2");

        User user = new User();
        user.setUserName("john");
        user.setJournalEntries(List.of(j1, j2));

        when(userRepo.findByUserName("john")).thenReturn(user);

        boolean deleted = userService.deleteUserByUserName("john");

        assertTrue(deleted);
        verify(journalEntryRepo).deleteAllById(List.of("1", "2"));
        verify(userRepo).deleteByUserName("john");
    }

    @Test
    void testDeleteUserByUserName_notFound_returnsFalse() {
        when(userRepo.findByUserName("ghost")).thenReturn(null);

        boolean result = userService.deleteUserByUserName("ghost");

        assertFalse(result);
        verifyNoInteractions(journalEntryRepo);
        verify(userRepo, never()).deleteByUserName(any());
    }
}
