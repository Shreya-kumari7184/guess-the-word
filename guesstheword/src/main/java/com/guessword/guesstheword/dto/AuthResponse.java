package com.guessword.guesstheword.dto;

import com.guessword.guesstheword.entity.Role;

public record AuthResponse(String username, Role role, boolean authenticated, String message) {
}
