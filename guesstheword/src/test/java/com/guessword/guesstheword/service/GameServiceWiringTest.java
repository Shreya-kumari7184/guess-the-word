package com.guessword.guesstheword.service;

import com.guessword.guesstheword.controller.GameController;
import com.guessword.guesstheword.repository.GameRepository;
import com.guessword.guesstheword.repository.GuessRepository;
import com.guessword.guesstheword.repository.UserRepository;
import com.guessword.guesstheword.repository.WordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class GameServiceWiringTest {

    @Test
    void serviceAndControllerAreConstructedWithConstructorInjection() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(UserRepository.class, () -> mock(UserRepository.class));
            context.registerBean(WordRepository.class, () -> mock(WordRepository.class));
            context.registerBean(GameRepository.class, () -> mock(GameRepository.class));
            context.registerBean(GuessRepository.class, () -> mock(GuessRepository.class));
            context.register(GameService.class, GameController.class);
            context.refresh();

            assertNotNull(context.getBean(GameService.class));
            assertNotNull(context.getBean(GameController.class));
        }
    }
}
