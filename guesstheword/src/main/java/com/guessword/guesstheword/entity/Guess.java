package com.guessword.guesstheword.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Locale;

@Entity
@Table(name = "guesses", uniqueConstraints = @UniqueConstraint(columnNames = {"game_id", "guess_number"}))
public class Guess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @NotNull
    @Size(min = 5, max = 5)
    @Column(nullable = false, length = 5)
    private String guess;

    @NotNull
    @Min(1)
    @Max(5)
    @Column(name = "guess_number", nullable = false)
    private Integer guessNumber;

    @Column(nullable = false)
    private boolean correct = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Guess() {
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        normalizeGuess();
    }

    @PreUpdate
    protected void onUpdate() {
        normalizeGuess();
    }

    private void normalizeGuess() {
        if (guess != null) {
            guess = guess.toUpperCase(Locale.ROOT);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }
    public String getGuess() { return guess; }
    public void setGuess(String guess) { this.guess = guess; }
    public Integer getGuessNumber() { return guessNumber; }
    public void setGuessNumber(Integer guessNumber) { this.guessNumber = guessNumber; }
    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
