package com.guessword.guesstheword.exception;

public class WordBankEmptyException extends RuntimeException {
    public WordBankEmptyException() {
        super("The word bank is not initialized");
    }
}
