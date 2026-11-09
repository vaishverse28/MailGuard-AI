package com.mailguard.dto;

import com.mailguard.model.User;

public record UserDto(Long id, String fullName, String email) {
    public static UserDto from(User u) {
        return new UserDto(u.getId(), u.getFullName(), u.getEmail());
    }
}
