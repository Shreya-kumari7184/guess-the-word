package com.guessword.guesstheword.service;

import com.guessword.guesstheword.dto.GameHistoryResponse;
import com.guessword.guesstheword.dto.LetterStatus;
import com.guessword.guesstheword.entity.Game;
import com.guessword.guesstheword.entity.Guess;
import com.guessword.guesstheword.entity.Role;
import com.guessword.guesstheword.entity.User;
import com.guessword.guesstheword.entity.Word;
import com.guessword.guesstheword.exception.GameNotFoundException;
import com.guessword.guesstheword.exception.GameOwnershipException;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameHistoryServiceTest {
    private static final Long GAME_ID = 17L;
    private final UserRepository userRepository = mock(UserRepository.class);
    private final WordRepository wordRepository = mock(WordRepository.class);
    private final GameRepository gameRepository = mock(GameRepository.class);
    private final GuessRepository guessRepository = mock(GuessRepository.class);
    private final GameService gameService = new GameService(userRepository, wordRepository, gameRepository, guessRepository);
    private final User player = user(1L, "player", Role.PLAYER);
    private final Authentication playerAuthentication = auth("player");
    private final Game game = game(player, "APPLE");

    @BeforeEach
    void setUp() {
        reset(userRepository, gameRepository, guessRepository);
        when(userRepository.findByUsername("player")).thenReturn(Optional.of(player));
        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));
    }

    @Test
    void playerCanRetrieveOwnGameWithGuessesInGuessNumberOrderAndEvaluatedResults() {
        Guess third = guess(3, "ALLEY");
        Guess first = guess(1, "BRAVE");
        Guess second = guess(2, "PUPPY");
        when(guessRepository.findAllByGameIdOrderByGuessNumberAsc(GAME_ID))
                .thenReturn(List.of(first, second, third));

        GameHistoryResponse response = gameService.getGameHistory(GAME_ID, playerAuthentication);

        assertEquals(GAME_ID, response.gameId());
        assertEquals(LocalDate.of(2026, 9, 27), response.playedDate());
        assertEquals("IN_PROGRESS", response.status());
        assertFalse(response.won());
        assertFalse(response.finished());
        assertEquals(2, response.guessesRemaining());
        assertEquals(List.of(1, 2, 3), response.guesses().stream()
                .map(GameHistoryResponse.PreviousGuessResponse::guessNumber).toList());
        assertEquals(List.of(LetterStatus.GREEN, LetterStatus.ORANGE, LetterStatus.GREY,
                        LetterStatus.ORANGE, LetterStatus.GREY), response.guesses().get(2).results().stream()
                .map(result -> result.status()).toList());
        verify(guessRepository).findAllByGameIdOrderByGuessNumberAsc(GAME_ID);
    }

    @Test
    void emptyGameReturnsGameInformationAndNoGuesses() {
        when(guessRepository.findAllByGameIdOrderByGuessNumberAsc(GAME_ID)).thenReturn(List.of());

        GameHistoryResponse response = gameService.getGameHistory(GAME_ID, playerAuthentication);

        assertEquals(List.of(), response.guesses());
        assertEquals(5, response.guessesRemaining());
        assertEquals("IN_PROGRESS", response.status());
    }

    @Test
    void completedGameReturnsAllGuessesAndCorrectFinishedStatus() {
        game.setWon(true);
        game.setCompletedAt(LocalDateTime.of(2026, 9, 27, 10, 0));
        when(guessRepository.findAllByGameIdOrderByGuessNumberAsc(GAME_ID)).thenReturn(List.of(
                guess(1, "BRAVE"), guess(2, "APPLE")));

        GameHistoryResponse response = gameService.getGameHistory(GAME_ID, playerAuthentication);

        assertEquals("WON", response.status());
        assertTrue(response.won());
        assertTrue(response.finished());
        assertEquals(3, response.guessesRemaining());
        assertEquals(List.of(1, 2), response.guesses().stream()
                .map(GameHistoryResponse.PreviousGuessResponse::guessNumber).toList());
    }

    @Test
    void historyResponseNeverContainsSecretWordOrSecretWordProperty() throws Exception {
        when(guessRepository.findAllByGameIdOrderByGuessNumberAsc(GAME_ID)).thenReturn(List.of(guess(1, "BRAVE")));

        String json = new ObjectMapper().writeValueAsString(gameService.getGameHistory(GAME_ID, playerAuthentication));

        assertFalse(json.contains("APPLE"));
        assertFalse(json.contains("secretWord"));
        assertFalse(json.contains("targetWord"));
        assertFalse(json.contains("word"));
    }

    @Test
    void playerCannotRetrieveAnotherPlayersGame() {
        game.setUser(user(2L, "other", Role.PLAYER));

        assertThrows(GameOwnershipException.class, () -> gameService.getGameHistory(GAME_ID, playerAuthentication));
        verifyNoInteractions(guessRepository);
    }

    @Test
    void adminCannotRetrievePlayerGame() {
        User admin = user(3L, "player", Role.ADMIN);
        when(userRepository.findByUsername("player")).thenReturn(Optional.of(admin));

        assertThrows(AccessDeniedException.class, () -> gameService.getGameHistory(GAME_ID, playerAuthentication));
        verifyNoInteractions(gameRepository, guessRepository);
    }

    @Test
    void unauthenticatedUserCannotRetrieveGame() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> gameService.getGameHistory(GAME_ID, null));
        Authentication anonymous = UsernamePasswordAuthenticationToken.unauthenticated("anonymousUser", "");
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> gameService.getGameHistory(GAME_ID, anonymous));
        verifyNoInteractions(userRepository, gameRepository, guessRepository);
    }

    @Test
    void missingGameReturnsNotFound() {
        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.empty());

        assertThrows(GameNotFoundException.class, () -> gameService.getGameHistory(GAME_ID, playerAuthentication));
    }

    private Guess guess(int number, String value) {
        Guess guess = new Guess();
        guess.setGame(game);
        guess.setGuessNumber(number);
        guess.setGuess(value);
        guess.setCreatedAt(LocalDateTime.of(2026, 9, 27, 9, number));
        return guess;
    }

    private static Game game(User user, String secret) {
        Word word = new Word();
        word.setWord(secret);
        Game game = new Game();
        game.setId(GAME_ID);
        game.setUser(user);
        game.setWord(word);
        game.setPlayedDate(LocalDate.of(2026, 9, 27));
        return game;
    }

    private static User user(Long id, String username, Role role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        return user;
    }

    private static Authentication auth(String name) {
        return UsernamePasswordAuthenticationToken.authenticated(name, null, List.of());
    }
}
