package com.codebreaker.gaied.controller;

import com.codebreaker.gaied.controller.service.EmailRoutingService;
import com.codebreaker.gaied.entity.EmailResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/emailroute")
public class EmailRoutingController {
    @Autowired
    EmailRoutingService service;
    @GetMapping("/generate")
    public ResponseEntity<String> generate() {
        return ResponseEntity.ok("All Good !!!!");
    }

    @GetMapping("/inittest")
    public ResponseEntity<String> initTest(@RequestParam String prompt) throws Exception {
        return ResponseEntity.ok(service.queryMistral(prompt));
    }

    @GetMapping(value = "/init")
    public ResponseEntity<List<EmailResponse>> init() throws Exception {
        return new ResponseEntity<>(service.getEmailRoute(), HttpStatus.OK);
    }
}
