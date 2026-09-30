package com.guessword.guesstheword.exception;

public class DailyGameLimitException extends RuntimeException {
    public static final String MESSAGE = "Daily game limit reached. You can play at most 3 games per day.";

    public DailyGameLimitException() {
        super(MESSAGE);
    }
}
