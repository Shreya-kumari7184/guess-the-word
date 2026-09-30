package com.guessword.guesstheword.repository;

import com.guessword.guesstheword.entity.Guess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface GuessRepository extends JpaRepository<Guess, Long> {
    List<Guess> findAllByGameIdOrderByGuessNumberAsc(Long gameId);
    long countByCorrectTrueAndCreatedAtBetween(java.time.LocalDateTime from, java.time.LocalDateTime to);
    long countByGamePlayedDateAndCorrectTrue(LocalDate playedDate);
}
