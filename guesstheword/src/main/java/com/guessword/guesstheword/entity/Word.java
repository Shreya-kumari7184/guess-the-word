package com.guessword.guesstheword.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Locale;

@Entity
@Table(name = "words")
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 5, max = 5)
    @Column(nullable = false, unique = true, length = 5)
    private String word;

    public Word() {
    }

    @PrePersist
    @PreUpdate
    protected void normalizeWord() {
        if (word != null) {
            word = word.toUpperCase(Locale.ROOT);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }
}
