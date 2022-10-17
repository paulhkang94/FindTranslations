package com.pkang.findtranslations.model;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ParseCrosswordJSONTest {

    private static final String testJSON = "{\"source_language\": \"en\", \"word\": \"man\", \"character_grid\": [[\"i\", \"q\", \"\\u00ed\", \"l\", \"n\", \"n\", \"m\", \"\\u00f3\"], [\"f\", \"t\", \"v\", \"\\u00f1\", \"b\", \"m\", \"h\", \"a\"], [\"h\", \"j\", \"\\u00e9\", \"t\", \"e\", \"t\", \"o\", \"z\"], [\"x\", \"\\u00e1\", \"o\", \"i\", \"e\", \"\\u00f1\", \"m\", \"\\u00e9\"], [\"q\", \"\\u00e9\", \"i\", \"\\u00f3\", \"q\", \"s\", \"b\", \"s\"], [\"c\", \"u\", \"m\", \"y\", \"v\", \"l\", \"r\", \"x\"], [\"\\u00fc\", \"\\u00ed\", \"\\u00f3\", \"m\", \"o\", \"t\", \"e\", \"k\"], [\"a\", \"g\", \"r\", \"n\", \"n\", \"\\u00f3\", \"s\", \"m\"]], \"word_locations\": {\"6,1,6,2,6,3,6,4,6,5,6,6\": \"hombre\"}, \"target_language\": \"es\"}";

    private String[][] _testCharGrid;
    private Map<String, String> _testWordLocations;

    @Before
    public void setUp() {
        _testCharGrid = new String[][]{
                {"i", "q", "\u00ed", "l", "n", "n", "m", "\u00f3"},
                {"f", "t", "v", "\u00f1", "b", "m", "h", "a"},
                {"h", "j", "\u00e9", "t", "e", "t", "o", "z"},
                {"x", "\u00e1", "o", "i", "e", "\u00f1", "m", "\u00e9"},
                {"q", "\u00e9", "i", "\u00f3", "q", "s", "b", "s"},
                {"c", "u", "m", "y", "v", "l", "r", "x"},
                {"\u00fc", "\u00ed", "\u00f3", "m", "o", "t", "e", "k"},
                {"a", "g", "r", "n", "n", "\u00f3", "s", "m"},};

        _testWordLocations = new HashMap<>();
        _testWordLocations.put("6,1,6,2,6,3,6,4,6,5,6,6", "hombre");
    }

    /**
     * Test whether RetrieveJSONTask parses the JSON correctly into CrosswordChallenge model.
     * @throws IOException exception thrown when trying to parse the JSON
     */
    @Test
    public void testJSONParse() throws IOException {
        RetrieveCrosswordTask task = new RetrieveCrosswordTask(null);
        InputStream testInputStream = new ByteArrayInputStream(testJSON.getBytes());

        List<CrosswordChallenge> crossword = task.parseCrosswordJSON(testInputStream);
        assertEquals(crossword.size(), 1);

        CrosswordChallenge screen = crossword.get(0);
        assertEquals(screen.getSourceLocale(), new Locale("en"));
        assertEquals(screen.getTargetLanguage(), new Locale("es"));
        assertEquals(screen.getWord(),"man");
        assertArrayEquals(screen.getCharacterGrid(), _testCharGrid);
        assertEquals(screen.getWordLocations(), _testWordLocations);
    }
}