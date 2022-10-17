package com.pkang.findtranslations.viewmodel;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.pkang.findtranslations.model.CrosswordChallenge;
import com.pkang.findtranslations.model.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * ViewModel class representing a single crossword problem screen.
 */
public class CrosswordScreenViewModel extends ViewModel {
    private MutableLiveData<Locale> _sourceLocale = new MutableLiveData<>();
    private MutableLiveData<Locale> _targetLocale = new MutableLiveData<>();
    private MutableLiveData<String> _sourceWord = new MutableLiveData<>();
    private MutableLiveData<String[][]> _characterGrid = new MutableLiveData<>();
    private MutableLiveData<Map<String, String>> _targetWordLocations = new MutableLiveData<>();
    private MutableLiveData<String> _targetWord = new MutableLiveData<>();

    private MutableLiveData<Integer> _screenIndex = new MutableLiveData<>();
    private MutableLiveData<String> _selectedLetters = new MutableLiveData<>();
    private MutableLiveData<Boolean> _selectionCorrect = new MutableLiveData<>();

    private LiveData<String> _sourceLocaleLabel = Transformations.map(_sourceLocale, input -> String.format("%s:",input.getDisplayLanguage()));
    private LiveData<String> _targetLocaleLabel = Transformations.map(_targetLocale, input -> String.format("%s:",input.getDisplayLanguage()));

    /**
     * Separate out the target path from the target word
     */
    private LiveData<List<Point>> targetWordMap = Transformations.map(_targetWordLocations, input -> {
        List<Point> targetWordPoints = new ArrayList<>();
        String targetWordPointString = input.keySet().iterator().next();
        String targetWord = input.get(targetWordPointString);
        setTargetWord(targetWord);
        String[] points = targetWordPointString.split(",");
        int i = 0;
        while (i < points.length) {
            Integer x = Integer.valueOf(points[i++]);
            Integer y = Integer.valueOf(points[i++]);
            Point point = new Point(x,y);
            targetWordPoints.add(point);
        }
        return targetWordPoints;
    });

    public void setScreenIndex(int index) {
        _screenIndex.setValue(index);
    }

    public LiveData<Integer> getScreenIndex() {
        return _screenIndex;
    }

    private void setSourceLocale(Locale locale) {
        _sourceLocale.setValue(locale);
    }

    public LiveData<String> getSourceLocaleLabel() {
        return _sourceLocaleLabel;
    }

    private void setTargetLocale(Locale locale) {
        _targetLocale.setValue(locale);
    }

    public LiveData<String> getTargetLocaleLabel() {
        return _targetLocaleLabel;
    }

    private void setSourceWord(String word) {
        _sourceWord.setValue(word);
    }

    public LiveData<String> getSourceWord() {
        return _sourceWord;
    }

    private void setTargetWord(String word) {
        _targetWord.setValue(word);
    }

    LiveData<String> getTargetWord() {
        return _targetWord;
    }

    private void setTargetWordLocations(Map<String, String> locations) {
        _targetWordLocations.setValue(locations);
    }

    public LiveData<List<Point>> getTargetWordLocations() {
        return targetWordMap;
    }

    private void setCharacterGrid(String[][] grid) {
        _characterGrid.setValue(grid);
    }

    public LiveData<String[][]> getCharacterGrid() {
        return _characterGrid;
    }

    public LiveData<Boolean> isSelectionCorrect() {
        return _selectionCorrect;
    }

    private void setSelectionCorrect(boolean correct) {
        _selectionCorrect.setValue(correct);
    }

    public LiveData<String> getSelectedLetters() {
        return _selectedLetters;
    }

    private void setSelectedLetters(String string) {
        _selectedLetters.setValue(string);
    }

    /**
     * Given a path, return the sum of letters in the path.
     * @param letters list of points representing the path
     * @return wum of letters in the path
     */
    private String getString(List<Point> letters) {
        if (letters.isEmpty()) return null;

        StringBuilder stringBuilder = new StringBuilder();
        for (Point point : letters) {
            String s = Objects.requireNonNull(getCharacterGrid().getValue())[point.y][point.x];
            stringBuilder.append(s);
        }
        return stringBuilder.toString();
    }

    /**
     * Checks to see if the selected path is the correct one for the crossword.
     * @param letters selected path
     * @return whether path is correct
     */
    private boolean isCorrectGuess(List<Point> letters) {
        List<Point> correctLetters = getTargetWordLocations().getValue();
        if (correctLetters == null || letters.size() != correctLetters.size()) {
            return false;
        }
        for (int i = 0; i < correctLetters.size(); i++) {
            Point answerPoint = correctLetters.get(i);
            Point currentPoint = letters.get(i);
            if (!answerPoint.equals(currentPoint)) {
                return false;
            }
        }
        return true;
    }

    public void setPressedPath(List<Point> path) {
        setSelectedLetters(getString(path));
    }

    public void checkSelectedPath(List<Point> path) {
        setSelectedLetters(getString(path));

        if (isCorrectGuess(path)) {
            setSelectionCorrect(true);
        } else {
            setSelectionCorrect(false);
        }
    }

    public void clearSelectedPath() {
        setSelectedLetters(null);
    }

    public void loadCrossword(CrosswordChallenge crosswordChallenge) {
        setSourceLocale(crosswordChallenge.getSourceLocale());
        setTargetLocale(crosswordChallenge.getTargetLanguage());
        setSourceWord(crosswordChallenge.getWord());
        setCharacterGrid(crosswordChallenge.getCharacterGrid());
        setTargetWordLocations(crosswordChallenge.getWordLocations());
    }
}