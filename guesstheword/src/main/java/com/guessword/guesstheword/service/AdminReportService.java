package com.guessword.guesstheword.service;

import com.guessword.guesstheword.dto.DailyReportResponse;
import com.guessword.guesstheword.dto.UserReportResponse;
import com.guessword.guesstheword.repository.GameRepository;
import com.guessword.guesstheword.repository.GuessRepository;
import com.guessword.guesstheword.repository.UserRepository;
import com.guessword.guesstheword.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class AdminReportService {
    private final GameRepository gameRepository;
    private final GuessRepository guessRepository;
    private final UserRepository userRepository;

    public AdminReportService(GameRepository gameRepository, GuessRepository guessRepository,
                              UserRepository userRepository) {
        this.gameRepository = gameRepository;
        this.guessRepository = guessRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public DailyReportResponse getDailyReport(LocalDate date) {
        long users = gameRepository.countDistinctUsersByPlayedDate(date);
        long correctGuesses = guessRepository.countByGamePlayedDateAndCorrectTrue(date);
        return new DailyReportResponse(date, users, correctGuesses);
    }

    @Transactional(readOnly = true)
    public UserReportResponse getUserReport(Long userId) {
        var user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return new UserReportResponse(user.getId(), user.getUsername(),
                gameRepository.findUserReportByPlayedDate(userId));
    }
}
