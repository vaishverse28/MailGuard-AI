package com.mailguard.controller;

import com.mailguard.dto.AnalyzeRequest;
import com.mailguard.dto.AnalyzeResponse;
import com.mailguard.dto.MessageResponse;
import com.mailguard.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emails")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<AnalyzeResponse> analyze(@RequestAttribute("userId") Long userId,
                                                   @Valid @RequestBody AnalyzeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(emailService.analyze(userId, request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<AnalyzeResponse>> history(@RequestAttribute("userId") Long userId,
                                                         @RequestParam(required = false) String search,
                                                         @RequestParam(required = false) String status,
                                                         @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(emailService.history(userId, search, status, sort));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnalyzeResponse> getById(@RequestAttribute("userId") Long userId,
                                                   @PathVariable Long id) {
        return ResponseEntity.ok(emailService.getById(userId, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> delete(@RequestAttribute("userId") Long userId,
                                                  @PathVariable Long id) {
        return ResponseEntity.ok(emailService.delete(userId, id));
    }
}
