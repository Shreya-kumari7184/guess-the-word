package com.guessword.guesstheword.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class GuessRequest {
    @NotBlank(message = "Guess is required")
    @Pattern(regexp = "^[A-Za-z]{5}$", message = "Guess must contain exactly 5 letters")
    private String guess;

    public GuessRequest() {
    }

    public GuessRequest(String guess) {
        this.guess = guess;
    }

    public String getGuess() { return guess; }
    public void setGuess(String guess) { this.guess = guess; }
}
