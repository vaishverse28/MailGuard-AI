package com.mailguard.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnalyzeRequest(
        @NotBlank(message = "Sender email cannot be empty")
        @Email(message = "Sender email is not a valid email address")
        @Size(max = 255, message = "Sender email is too long")
        String senderEmail,

        @NotBlank(message = "Email subject cannot be empty")
        @Size(max = 500, message = "Email subject must be at most 500 characters")
        String subject,

        @NotBlank(message = "Email body cannot be empty")
        @Size(max = 20000, message = "Email body must be at most 20000 characters")
        String body
) {
}
