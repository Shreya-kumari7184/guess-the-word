package com.guessword.guesstheword.service;

import com.guessword.guesstheword.dto.DailyReportResponse;
import com.guessword.guesstheword.dto.UserReportEntry;
import com.guessword.guesstheword.entity.User;
import com.guessword.guesstheword.exception.UserNotFoundException;
import com.guessword.guesstheword.repository.GameRepository;
import com.guessword.guesstheword.repository.GuessRepository;
import com.guessword.guesstheword.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminReportServiceTest {
    private final GameRepository gameRepository = mock(GameRepository.class);
    private final GuessRepository guessRepository = mock(GuessRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final AdminReportService service = new AdminReportService(gameRepository, guessRepository, userRepository);

    @BeforeEach
    void resetMocks() {
        reset(gameRepository, guessRepository, userRepository);
    }

    @Test
    void dailyReportCountsDistinctPlayersAndSavedCorrectGuesses() {
        LocalDate date = LocalDate.of(2026, 9, 27);
        when(gameRepository.countDistinctUsersByPlayedDate(date)).thenReturn(2L);
        when(guessRepository.countByGamePlayedDateAndCorrectTrue(date)).thenReturn(3L);

        DailyReportResponse report = service.getDailyReport(date);

        assertEquals(new DailyReportResponse(date, 2, 3), report);
        verify(gameRepository).countDistinctUsersByPlayedDate(date);
        verify(guessRepository).countByGamePlayedDateAndCorrectTrue(date);
    }

    @Test
    void dailyReportReturnsZeroCountsWhenThereIsNoActivity() {
        LocalDate date = LocalDate.of(2026, 9, 27);

        assertEquals(new DailyReportResponse(date, 0, 0), service.getDailyReport(date));
    }

    @Test
    void userReportReturnsGamesAndCorrectGuessesPerDateInRepositoryOrder() {
        User user = new User();
        user.setId(8L);
        user.setUsername("player8");
        when(userRepository.findById(8L)).thenReturn(Optional.of(user));
        List<UserReportEntry> entries = List.of(
                new UserReportEntry(LocalDate.of(2026, 9, 26), 2, 1),
                new UserReportEntry(LocalDate.of(2026, 9, 27), 1, 0));
        when(gameRepository.findUserReportByPlayedDate(8L)).thenReturn(entries);

        var report = service.getUserReport(8L);

        assertEquals(8L, report.userId());
        assertEquals("player8", report.username());
        assertEquals(entries, report.reports());
        verify(gameRepository).findUserReportByPlayedDate(8L);
    }

    @Test
    void nonexistentUserReturnsNotFoundException() {
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.getUserReport(404L));
        verifyNoInteractions(gameRepository);
    }
}
