package com.guessword.guesstheword.dto;

import java.time.LocalDate;

public record DailyReportResponse(LocalDate date, long numberOfUsers, long numberOfCorrectGuesses) {
}
