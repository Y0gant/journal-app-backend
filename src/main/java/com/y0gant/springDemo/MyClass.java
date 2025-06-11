package com.y0gant.springDemo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyClass {
    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }
}
