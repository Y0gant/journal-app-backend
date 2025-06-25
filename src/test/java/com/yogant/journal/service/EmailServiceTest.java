package com.yogant.journal.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EmailServiceTest {

    @Autowired
    private EmailService service;


    @Test
    void testSendEmail() {
        service.sendEmail("yogantfaye7@gmail.com", "Test mail", "Test body text");
    }
}