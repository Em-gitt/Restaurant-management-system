package com.unipoli.auth.dto;

public record RegisterResponse(
        Long id,
        String username,
        String email,
        String role
) {
}