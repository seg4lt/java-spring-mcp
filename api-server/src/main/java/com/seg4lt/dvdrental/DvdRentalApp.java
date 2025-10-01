package com.seg4lt.dvdrental;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class DvdRentalApp {
    public static void main(String[] args) {
        SpringApplication.run(DvdRentalApp.class, args);
    }
}

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/test")
class TestController {
    @GetMapping
    public String test() {
        return "Hello World 1";
    }
}
