package com.guessword.guesstheword.service;

import com.guessword.guesstheword.dto.GameStartResponse;
import com.guessword.guesstheword.dto.GameHistoryResponse;
import com.guessword.guesstheword.dto.GuessRequest;
import com.guessword.guesstheword.dto.GuessResultResponse;
import com.guessword.guesstheword.dto.LetterResult;
import com.guessword.guesstheword.dto.LetterStatus;
import com.guessword.guesstheword.entity.Game;
import com.guessword.guesstheword.entity.Guess;
import com.guessword.guesstheword.entity.Role;
import com.guessword.guesstheword.entity.User;
import com.guessword.guesstheword.entity.Word;
import com.guessword.guesstheword.exception.DailyGameLimitException;
import com.guessword.guesstheword.exception.CompletedGameException;
import com.guessword.guesstheword.exception.GameNotFoundException;
import com.guessword.guesstheword.exception.GameOwnershipException;
import com.guessword.guesstheword.exception.InvalidGuessException;
import com.guessword.guesstheword.exception.MaximumGuessesExceededException;
import com.guessword.guesstheword.exception.WordBankEmptyException;
import com.guessword.guesstheword.repository.GameRepository;
import com.guessword.guesstheword.repository.GuessRepository;
import com.guessword.guesstheword.repository.UserRepository;
import com.guessword.guesstheword.repository.WordRepository;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class GameService {

    private final UserRepository userRepository;
    private final WordRepository wordRepository;
    private final GameRepository gameRepository;
    private final GuessRepository guessRepository;
    private final Clock clock;

    @Autowired
    public GameService(UserRepository userRepository, WordRepository wordRepository,
                       GameRepository gameRepository, GuessRepository guessRepository) {
        this(userRepository, wordRepository, gameRepository, guessRepository, Clock.systemDefaultZone());
    }

    GameService(UserRepository userRepository, WordRepository wordRepository,
                GameRepository gameRepository, GuessRepository guessRepository, Clock clock) {
        this.userRepository = userRepository;
        this.wordRepository = wordRepository;
        this.gameRepository = gameRepository;
        this.guessRepository = guessRepository;
        this.clock = clock;
    }

    @Transactional
    public GameStartResponse startGame(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new AuthenticationCredentialsNotFoundException("Authentication required");
        }

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Authenticated user not found"));
        if (user.getRole() != Role.PLAYER) {
            throw new org.springframework.security.access.AccessDeniedException("Only players can start games");
        }

        LocalDate today = LocalDate.now(clock);
        if (gameRepository.countByUserIdAndPlayedDate(user.getId(), today) >= 3) {
            throw new DailyGameLimitException();
        }

        List<Word> availableWords = wordRepository.findAll();
        if (availableWords.isEmpty()) {
            throw new WordBankEmptyException();
        }
        Word selectedWord = availableWords.get(ThreadLocalRandom.current().nextInt(availableWords.size()));
        LocalDateTime startedAt = LocalDateTime.now(clock);

        Game game = new Game();
        game.setUser(user);
        game.setWord(selectedWord);
        game.setPlayedDate(today);
        game.setStartedAt(startedAt);
        game.setWon(false);

        Game savedGame = gameRepository.save(game);
        return new GameStartResponse(savedGame.getId(), today, 5, "IN_PROGRESS");
    }

    @Transactional
    public GuessResultResponse submitGuess(Long gameId, GuessRequest request, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new AuthenticationCredentialsNotFoundException("Authentication required");
        }

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Authenticated user not found"));
        if (user.getRole() != Role.PLAYER) {
            throw new org.springframework.security.access.AccessDeniedException("Only players can submit guesses");
        }

        String normalizedGuess = request == null || request.getGuess() == null
                ? null : request.getGuess().toUpperCase(Locale.ROOT);
        if (normalizedGuess == null || !normalizedGuess.matches("[A-Z]{5}")) {
            throw new InvalidGuessException();
        }

        Game game = gameRepository.findById(gameId).orElseThrow(() -> new GameNotFoundException(gameId));
        if (!game.getUser().getId().equals(user.getId())) {
            throw new GameOwnershipException();
        }
        if (game.getCompletedAt() != null) {
            throw new CompletedGameException();
        }

        List<Guess> previousGuesses = guessRepository.findAllByGameIdOrderByGuessNumberAsc(gameId);
        if (previousGuesses.size() >= 5) {
            throw new MaximumGuessesExceededException();
        }

        int guessNumber = previousGuesses.size() + 1;
        String secretWord = game.getWord().getWord().toUpperCase(Locale.ROOT);
        List<LetterResult> results = evaluateGuess(secretWord, normalizedGuess);
        boolean correct = normalizedGuess.equals(secretWord);
        LocalDateTime now = LocalDateTime.now(clock);

        Guess guess = new Guess();
        guess.setGame(game);
        guess.setGuess(normalizedGuess);
        guess.setGuessNumber(guessNumber);
        guess.setCorrect(correct);
        guess.setCreatedAt(now);
        guessRepository.save(guess);

        boolean finished = correct || guessNumber == 5;
        if (finished) {
            game.setWon(correct);
            game.setCompletedAt(now);
            gameRepository.save(game);
        }

        String status = correct ? "WON" : finished ? "LOST" : "IN_PROGRESS";
        String message = correct ? "Congratulations! You guessed the word."
                : finished ? "Better luck next time." : "Guess submitted.";
        return new GuessResultResponse(gameId, guessNumber, normalizedGuess, results,
                5 - guessNumber, status, correct, finished, message);
    }

    @Transactional(readOnly = true)
    public GameHistoryResponse getGameHistory(Long gameId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new AuthenticationCredentialsNotFoundException("Authentication required");
        }

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Authenticated user not found"));
        if (user.getRole() != Role.PLAYER) {
            throw new org.springframework.security.access.AccessDeniedException("Only players can view game history");
        }

        Game game = gameRepository.findById(gameId).orElseThrow(() -> new GameNotFoundException(gameId));
        if (!game.getUser().getId().equals(user.getId())) {
            throw new GameOwnershipException("You are not allowed to view this game");
        }

        List<Guess> previousGuesses = guessRepository.findAllByGameIdOrderByGuessNumberAsc(gameId);
        String secretWord = game.getWord().getWord().toUpperCase(Locale.ROOT);
        List<GameHistoryResponse.PreviousGuessResponse> guesses = previousGuesses.stream()
                .map(guess -> new GameHistoryResponse.PreviousGuessResponse(
                        guess.getGuessNumber(), guess.getGuess(), evaluateGuess(secretWord, guess.getGuess()),
                        guess.getCreatedAt()))
                .toList();
        boolean finished = game.getCompletedAt() != null;
        String status = finished ? (game.isWon() ? "WON" : "LOST") : "IN_PROGRESS";
        return new GameHistoryResponse(game.getId(), game.getPlayedDate(), status, game.isWon(), finished,
                Math.max(0, 5 - previousGuesses.size()), guesses);
    }

    static List<LetterResult> evaluateGuess(String secretWord, String guess) {
        LetterStatus[] statuses = new LetterStatus[5];
        boolean[] secretLetterUsed = new boolean[5];

        for (int index = 0; index < 5; index++) {
            if (guess.charAt(index) == secretWord.charAt(index)) {
                statuses[index] = LetterStatus.GREEN;
                secretLetterUsed[index] = true;
            }
        }

        for (int guessIndex = 0; guessIndex < 5; guessIndex++) {
            if (statuses[guessIndex] == LetterStatus.GREEN) {
                continue;
            }
            statuses[guessIndex] = LetterStatus.GREY;
            for (int secretIndex = 0; secretIndex < 5; secretIndex++) {
                if (!secretLetterUsed[secretIndex] && guess.charAt(guessIndex) == secretWord.charAt(secretIndex)) {
                    statuses[guessIndex] = LetterStatus.ORANGE;
                    secretLetterUsed[secretIndex] = true;
                    break;
                }
            }
        }

        List<LetterResult> results = new ArrayList<>(5);
        for (int index = 0; index < 5; index++) {
            results.add(new LetterResult(String.valueOf(guess.charAt(index)), statuses[index]));
        }
        return List.copyOf(results);
    }
}
