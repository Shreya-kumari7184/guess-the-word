package com.guessword.guesstheword.config;

import com.guessword.guesstheword.entity.Word;
import com.guessword.guesstheword.repository.WordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class WordBankInitializerTest {

    @Test
    void seedsExactlyTwentyWordsAndIsIdempotent() throws Exception {
        WordRepository repository = mock(WordRepository.class);
        Map<String, Word> savedWords = new HashMap<>();
        when(repository.findByWord(anyString())).thenAnswer(invocation ->
                Optional.ofNullable(savedWords.get(invocation.getArgument(0))));
        when(repository.save(any(Word.class))).thenAnswer(invocation -> {
            Word word = invocation.getArgument(0);
            savedWords.put(word.getWord(), word);
            return word;
        });
        WordBankInitializer initializer = new WordBankInitializer(repository);
        DefaultApplicationArguments arguments = new DefaultApplicationArguments(new String[0]);

        initializer.run(arguments);
        initializer.run(arguments);

        assertEquals(20, savedWords.size());
        assertTrue(savedWords.keySet().stream().allMatch(word -> word.length() == 5 && word.equals(word.toUpperCase())));
        verify(repository, times(20)).save(any(Word.class));
    }
}
