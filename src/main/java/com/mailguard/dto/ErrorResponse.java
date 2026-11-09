package com.mailguard.dto;

public record ErrorResponse(boolean success, int status, String message) {
}
