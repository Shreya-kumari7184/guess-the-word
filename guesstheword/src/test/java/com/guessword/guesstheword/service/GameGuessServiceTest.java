package com.guessword.guesstheword.service;

import com.guessword.guesstheword.dto.GuessRequest;
import com.guessword.guesstheword.dto.GuessResultResponse;
import com.guessword.guesstheword.dto.LetterStatus;
import com.guessword.guesstheword.entity.Game;
import com.guessword.guesstheword.entity.Guess;
import com.guessword.guesstheword.entity.Role;
import com.guessword.guesstheword.entity.User;
import com.guessword.guesstheword.entity.Word;
import com.guessword.guesstheword.exception.CompletedGameException;
import com.guessword.guesstheword.exception.GameNotFoundException;
import com.guessword.guesstheword.exception.GameOwnershipException;
import com.guessword.guesstheword.exception.InvalidGuessException;
import com.guessword.guesstheword.exception.MaximumGuessesExceededException;
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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GameGuessServiceTest {

    private static final Long GAME_ID = 11L;
    private static final Instant NOW = Instant.parse("2026-09-27T10:15:30Z");

    private final UserRepository userRepository = mock(UserRepository.class);
    private final WordRepository wordRepository = mock(WordRepository.class);
    private final GameRepository gameRepository = mock(GameRepository.class);
    private final GuessRepository guessRepository = mock(GuessRepository.class);
    private final GameService gameService = new GameService(userRepository, wordRepository, gameRepository,
            guessRepository, Clock.fixed(NOW, ZoneOffset.UTC));
    private User player;
    private Game game;
    private Authentication playerAuthentication;
    private List<Guess> existingGuesses;

    @BeforeEach
    void setUp() {
        reset(userRepository, wordRepository, gameRepository, guessRepository);
        player = user(1L, "alice", Role.PLAYER);
        game = game(GAME_ID, player, "APPLE");
        playerAuthentication = UsernamePasswordAuthenticationToken.authenticated("alice", null, List.of());
        existingGuesses = new ArrayList<>();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(player));
        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));
        when(guessRepository.findAllByGameIdOrderByGuessNumberAsc(GAME_ID)).thenAnswer(invocation ->
                List.copyOf(existingGuesses));
        when(guessRepository.save(any(Guess.class))).thenAnswer(invocation -> {
            Guess guess = invocation.getArgument(0);
            existingGuesses.add(guess);
            return guess;
        });
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void validGuessIsUppercasedSavedAndNumbered() {
        GuessResultResponse response = gameService.submitGuess(GAME_ID, new GuessRequest("eAAAA"), playerAuthentication);

        Guess saved = existingGuesses.getFirst();
        assertSame(game, saved.getGame());
        assertEquals("EAAAA", saved.getGuess());
        assertEquals(1, saved.getGuessNumber());
        assertEquals(LocalDateTime.ofInstant(NOW, ZoneOffset.UTC), saved.getCreatedAt());
        assertFalse(saved.isCorrect());
        assertEquals(1, response.guessNumber());
        assertEquals("EAAAA", response.guess());
        assertEquals(List.of(LetterStatus.ORANGE, LetterStatus.ORANGE, LetterStatus.GREY,
                LetterStatus.GREY, LetterStatus.GREY), response.results().stream().map(r -> r.status()).toList());
    }

    @Test
    void exactPositionLettersAreGreen() {
        GuessResultResponse response = gameService.submitGuess(GAME_ID, new GuessRequest("APPLE"), playerAuthentication);
        assertEquals(List.of(LetterStatus.GREEN, LetterStatus.GREEN, LetterStatus.GREEN,
                LetterStatus.GREEN, LetterStatus.GREEN), response.results().stream().map(r -> r.status()).toList());
    }

    @Test
    void duplicateLettersAreConsumedOnlyOnceAcrossTwoPasses() {
        GuessResultResponse response = gameService.submitGuess(GAME_ID, new GuessRequest("ALLEY"), playerAuthentication);
        assertEquals(List.of(LetterStatus.GREEN, LetterStatus.ORANGE, LetterStatus.GREY,
                LetterStatus.ORANGE, LetterStatus.GREY), response.results().stream().map(r -> r.status()).toList());
    }

    @Test
    void repeatedSecretAndGuessLettersDoNotProduceExtraMatches() {
        GuessResultResponse response = gameService.submitGuess(GAME_ID, new GuessRequest("PPPPQ"), playerAuthentication);
        assertEquals(List.of(LetterStatus.GREY, LetterStatus.GREEN, LetterStatus.GREEN,
                LetterStatus.GREY, LetterStatus.GREY), response.results().stream().map(r -> r.status()).toList());
    }

    @Test
    void correctGuessWinsCompletesGameAndPreventsFurtherGuesses() {
        GuessResultResponse response = gameService.submitGuess(GAME_ID, new GuessRequest("apple"), playerAuthentication);

        assertTrue(response.won());
        assertTrue(response.finished());
        assertEquals("WON", response.status());
        assertTrue(game.isWon());
        assertEquals(LocalDateTime.ofInstant(NOW, ZoneOffset.UTC), game.getCompletedAt());
        assertThrows(CompletedGameException.class,
                () -> gameService.submitGuess(GAME_ID, new GuessRequest("BRAVE"), playerAuthentication));
        verify(guessRepository, times(1)).save(any(Guess.class));
    }

    @Test
    void fifthIncorrectGuessLosesAndSixthIsRejected() {
        for (int number = 1; number <= 4; number++) {
            existingGuesses.add(guess(game, number, "BRAVE"));
        }

        GuessResultResponse response = gameService.submitGuess(GAME_ID, new GuessRequest("ZZZZZ"), playerAuthentication);

        assertFalse(response.won());
        assertTrue(response.finished());
        assertEquals("LOST", response.status());
        assertEquals("Better luck next time.", response.message());
        assertFalse(game.isWon());
        assertEquals(LocalDateTime.ofInstant(NOW, ZoneOffset.UTC), game.getCompletedAt());
        assertThrows(CompletedGameException.class,
                () -> gameService.submitGuess(GAME_ID, new GuessRequest("BRAVE"), playerAuthentication));
        verify(guessRepository, times(1)).save(any(Guess.class));
    }

    @Test
    void incorrectGuessKeepsGameActiveAndDecrementsRemainingGuesses() {
        existingGuesses.add(guess(game, 1, "BRAVE"));

        GuessResultResponse response = gameService.submitGuess(GAME_ID, new GuessRequest("ZZZZZ"), playerAuthentication);

        assertEquals(2, response.guessNumber());
        assertEquals(3, response.guessesRemaining());
        assertEquals("IN_PROGRESS", response.status());
        assertFalse(response.finished());
        assertNull(game.getCompletedAt());
    }

    @Test
    void maximumExistingGuessesRejectsAnotherWithoutSaving() {
        for (int number = 1; number <= 5; number++) {
            existingGuesses.add(guess(game, number, "BRAVE"));
        }

        assertThrows(MaximumGuessesExceededException.class,
                () -> gameService.submitGuess(GAME_ID, new GuessRequest("ZZZZZ"), playerAuthentication));
        verify(guessRepository, never()).save(any(Guess.class));
    }

    @Test
    void invalidGuessLengthsAndCharactersAreRejected() {
        for (String value : List.of("FOUR", "TOOLONG", "AB1DE", "AB$DE", "AB DE")) {
            assertThrows(InvalidGuessException.class,
                    () -> gameService.submitGuess(GAME_ID, new GuessRequest(value), playerAuthentication), value);
        }
        verifyNoInteractions(gameRepository, guessRepository);
    }

    @Test
    void playerCannotGuessInAnotherPlayersGame() {
        User otherPlayer = user(2L, "bob", Role.PLAYER);
        game.setUser(otherPlayer);

        assertThrows(GameOwnershipException.class,
                () -> gameService.submitGuess(GAME_ID, new GuessRequest("BRAVE"), playerAuthentication));
        verifyNoInteractions(guessRepository);
    }

    @Test
    void adminCannotSubmitGuess() {
        User admin = user(3L, "alice", Role.ADMIN);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(admin));

        assertThrows(AccessDeniedException.class,
                () -> gameService.submitGuess(GAME_ID, new GuessRequest("BRAVE"), playerAuthentication));
        verifyNoInteractions(gameRepository, guessRepository);
    }

    @Test
    void unauthenticatedUserCannotSubmitGuess() {
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> gameService.submitGuess(GAME_ID, new GuessRequest("BRAVE"), null));
        verifyNoInteractions(userRepository, gameRepository, guessRepository);
    }

    @Test
    void missingGameReturnsNotFoundException() {
        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.empty());
        assertThrows(GameNotFoundException.class,
                () -> gameService.submitGuess(GAME_ID, new GuessRequest("BRAVE"), playerAuthentication));
    }

    @Test
    void responseDoesNotContainSecretWord() throws Exception {
        GuessResultResponse response = gameService.submitGuess(GAME_ID, new GuessRequest("BRAVE"), playerAuthentication);
        String json = new ObjectMapper().writeValueAsString(response);
        assertFalse(json.contains("APPLE"));
        assertFalse(json.contains("secretWord"));
        assertFalse(json.contains("targetWord"));
    }

    private static User user(Long id, String username, Role role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        return user;
    }

    private static Game game(Long id, User user, String secretWord) {
        Word word = new Word();
        word.setId(5L);
        word.setWord(secretWord);
        Game game = new Game();
        game.setId(id);
        game.setUser(user);
        game.setWord(word);
        return game;
    }

    private static Guess guess(Game game, int number, String value) {
        Guess guess = new Guess();
        guess.setGame(game);
        guess.setGuess(value);
        guess.setGuessNumber(number);
        return guess;
    }
}
