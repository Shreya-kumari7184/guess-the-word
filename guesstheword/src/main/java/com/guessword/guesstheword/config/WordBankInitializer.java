package com.guessword.guesstheword.config;

import com.guessword.guesstheword.entity.Word;
import com.guessword.guesstheword.repository.WordRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class WordBankInitializer implements ApplicationRunner {

    public static final List<String> WORDS = List.of(
            "APPLE", "BRAVE", "CANDY", "DREAM", "EAGLE",
            "FLAME", "GRAPE", "HOUSE", "INDEX", "JUICE",
            "KNIFE", "LEMON", "MANGO", "NURSE", "OCEAN",
            "PARTY", "QUEEN", "RIVER", "STORM", "TIGER"
    );

    private final WordRepository wordRepository;

    public WordBankInitializer(WordRepository wordRepository) {
        this.wordRepository = wordRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String value : WORDS) {
            if (wordRepository.findByWord(value).isEmpty()) {
                Word word = new Word();
                word.setWord(value);
                wordRepository.save(word);
            }
        }
    }
}
