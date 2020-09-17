package com.cinlogic.jwtexampleserver.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping(path = "/test")
public class TestController {

    @GetMapping
    public ResponseEntity<Instant> test() {
        return ResponseEntity.ok(Instant.now());
    }
}