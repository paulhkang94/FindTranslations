package com.pkang.findtranslations.model;

import com.fasterxml.jackson.annotation.JsonSetter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;

/**
 * Data model extracted from the JSON data. represents a single crossword challenge.
 * - The underscore setter function format is required
 */
public class CrosswordChallenge implements Serializable {
    private Locale _sourceLocale;
    private Locale _targetLocale;
    private String _word;
    private String[][] _characterGrid;
    private HashMap<String, String> _wordLocations;

    public CrosswordChallenge() {
    }

    public Locale getSourceLocale() {
        return _sourceLocale;
    }

    @JsonSetter("source_language")
    public void setSourceLocale(Locale sourceLocale) {
        _sourceLocale = sourceLocale;
    }

    public Locale getTargetLanguage() {
        return _targetLocale;
    }

    @JsonSetter("target_language")
    public void setTargetLocale(Locale targetLocale) {
        _targetLocale = targetLocale;
    }

    public String getWord() {
        return _word;
    }

    @JsonSetter("word")
    public void setWord(String word) {
        _word = word;
    }

    public String[][] getCharacterGrid() {
        return _characterGrid;
    }

    @JsonSetter("character_grid")
    public void setCharacterGrid(String[][] characterGrid) {
        _characterGrid = characterGrid;
    }

    public HashMap<String, String> getWordLocations() {
        return _wordLocations;
    }

    @JsonSetter("word_locations")
    public void setWordLocations(HashMap<String, String> wordLocations) {
        _wordLocations = wordLocations;
    }
}

