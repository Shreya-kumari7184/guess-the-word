package com.guessword.guesstheword.service;

import com.guessword.guesstheword.dto.GameStartResponse;
import com.guessword.guesstheword.entity.Game;
import com.guessword.guesstheword.entity.Role;
import com.guessword.guesstheword.entity.User;
import com.guessword.guesstheword.entity.Word;
import com.guessword.guesstheword.exception.DailyGameLimitException;
import com.guessword.guesstheword.repository.GameRepository;
import com.guessword.guesstheword.repository.GuessRepository;
import com.guessword.guesstheword.repository.UserRepository;
import com.guessword.guesstheword.repository.WordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GameServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final WordRepository wordRepository = mock(WordRepository.class);
    private final GameRepository gameRepository = mock(GameRepository.class);
    private final GuessRepository guessRepository = mock(GuessRepository.class);
    private final Instant instant = Instant.parse("2026-09-26T12:30:00Z");
    private final Clock clock = Clock.fixed(instant, ZoneOffset.UTC);
    private final GameService gameService = new GameService(userRepository, wordRepository, gameRepository, guessRepository, clock);

    @BeforeEach
    void resetMocks() {
        reset(userRepository, wordRepository, gameRepository, guessRepository);
    }

    @Test
    void playerStartsGameWithSafeResponseAndPersistedGameDetails() throws Exception {
        User player = user(Role.PLAYER);
        Word secret = word("APPLE");
        when(userRepository.findByUsername("player1")).thenReturn(Optional.of(player));
        when(gameRepository.countByUserIdAndPlayedDate(42L, LocalDate.of(2026, 9, 26))).thenReturn(0L);
        when(wordRepository.findAll()).thenReturn(List.of(secret));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> {
            Game game = invocation.getArgument(0);
            game.setId(100L);
            return game;
        });

        GameStartResponse response = gameService.startGame(playerAuthentication());

        var captor = org.mockito.ArgumentCaptor.forClass(Game.class);
        verify(gameRepository).save(captor.capture());
        Game savedGame = captor.getValue();
        assertSame(player, savedGame.getUser());
        assertSame(secret, savedGame.getWord());
        assertEquals(LocalDate.of(2026, 9, 26), savedGame.getPlayedDate());
        assertEquals(LocalDateTime.ofInstant(instant, ZoneOffset.UTC), savedGame.getStartedAt());
        assertFalse(savedGame.isWon());
        assertEquals(100L, response.gameId());
        assertEquals(5, response.guessesRemaining());
        assertEquals("IN_PROGRESS", response.status());
        assertFalse(new ObjectMapper().writeValueAsString(response).contains("APPLE"));
    }

    @Test
    void playerCanStartThreeGamesButFourthIsRejectedWithoutSaving() {
        User player = user(Role.PLAYER);
        AtomicInteger gamesCreated = new AtomicInteger();
        when(userRepository.findByUsername("player1")).thenReturn(Optional.of(player));
        when(gameRepository.countByUserIdAndPlayedDate(42L, LocalDate.of(2026, 9, 26)))
                .thenAnswer(invocation -> (long) gamesCreated.get());
        when(wordRepository.findAll()).thenReturn(List.of(word("APPLE")));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> {
            gamesCreated.incrementAndGet();
            Game game = invocation.getArgument(0);
            game.setId((long) gamesCreated.get());
            return game;
        });

        assertEquals(1L, gameService.startGame(playerAuthentication()).gameId());
        assertEquals(2L, gameService.startGame(playerAuthentication()).gameId());
        assertEquals(3L, gameService.startGame(playerAuthentication()).gameId());
        DailyGameLimitException exception = assertThrows(DailyGameLimitException.class,
                () -> gameService.startGame(playerAuthentication()));

        assertEquals("Daily game limit reached. You can play at most 3 games per day.", exception.getMessage());
        assertEquals(3, gamesCreated.get());
        verify(gameRepository, times(3)).save(any(Game.class));
    }

    @Test
    void adminCannotStartPlayerGame() {
        User admin = user(Role.ADMIN);
        when(userRepository.findByUsername("player1")).thenReturn(Optional.of(admin));

        assertThrows(AccessDeniedException.class, () -> gameService.startGame(playerAuthentication()));
        verifyNoInteractions(gameRepository, wordRepository);
    }

    @Test
    void unauthenticatedUserCannotStartGame() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> gameService.startGame(null));
        Authentication anonymous = UsernamePasswordAuthenticationToken.unauthenticated("anonymousUser", "");
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> gameService.startGame(anonymous));
        verifyNoInteractions(userRepository, gameRepository, wordRepository);
    }

    private static User user(Role role) {
        User user = new User();
        user.setId(42L);
        user.setUsername("player1");
        user.setPassword("hashed");
        user.setRole(role);
        return user;
    }

    private static Word word(String value) {
        Word word = new Word();
        word.setId(7L);
        word.setWord(value);
        return word;
    }

    private static Authentication playerAuthentication() {
        return UsernamePasswordAuthenticationToken.authenticated("player1", null, List.of());
    }
}
