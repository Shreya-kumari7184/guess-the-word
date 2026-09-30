package com.guessword.guesstheword.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record GameHistoryResponse(Long gameId, LocalDate playedDate, String status, boolean won,
                                  boolean finished, int guessesRemaining,
                                  List<PreviousGuessResponse> guesses) {
    public record PreviousGuessResponse(int guessNumber, String guess, List<LetterResult> results,
                                        LocalDateTime createdAt) {
    }
}
