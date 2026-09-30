package com.guessword.guesstheword.dto;

import java.time.LocalDate;

public record GameStartResponse(Long gameId, LocalDate playedDate, int guessesRemaining, String status) {
}
