package com.guessword.guesstheword.dto;

import java.util.List;

public record GuessResultResponse(Long gameId, int guessNumber, String guess, List<LetterResult> results,
                                  int guessesRemaining, String status, boolean won, boolean finished,
                                  String message) {
}
