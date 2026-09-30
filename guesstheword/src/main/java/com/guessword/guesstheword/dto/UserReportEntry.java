package com.guessword.guesstheword.dto;

import java.time.LocalDate;

public record UserReportEntry(LocalDate date, long numberOfWordsTried, long numberOfCorrectGuesses) {
}
