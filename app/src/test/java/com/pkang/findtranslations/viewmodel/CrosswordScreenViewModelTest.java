package com.pkang.findtranslations.viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.pkang.findtranslations.model.CrosswordChallenge;
import com.pkang.findtranslations.model.Point;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@RunWith(MockitoJUnitRunner.class)
public class CrosswordScreenViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private List<Point> _targetPath = new ArrayList<>();

    //ViewModel and mock observables
    private CrosswordScreenViewModel viewModel;

    @Mock
    Observer<String> _sourceLocaleObserver;

    @Mock
    Observer<String> _targetLocaleObserver;

    @Mock
    Observer<String> _sourceWordObserver;

    @Mock
    Observer<String> _targetWordObserver;

    @Mock
    Observer<List<Point>> _targetWordLocObserver;

    //region setup
    @Before
    public void setUp() {
        //Test data
        MockitoAnnotations.initMocks(this);
        viewModel = new CrosswordScreenViewModel();
        viewModel.getSourceLocaleLabel().observeForever(_sourceLocaleObserver);
        viewModel.getTargetLocaleLabel().observeForever(_targetLocaleObserver);
        viewModel.getSourceWord().observeForever(_sourceWordObserver);
        viewModel.getTargetWord().observeForever(_targetWordObserver);
        viewModel.getTargetWordLocations().observeForever(_targetWordLocObserver);
        CrosswordChallenge challenge = new CrosswordChallenge();
        challenge.setWord("man");
        challenge.setCharacterGrid(new String[][]{
                {"i", "q", "\u00ed", "l", "n", "n", "m", "\u00f3"},
                {"f", "t", "v", "\u00f1", "b", "m", "h", "a"},
                {"h", "j", "\u00e9", "t", "e", "t", "o", "z"},
                {"x", "\u00e1", "o", "i", "e", "\u00f1", "m", "\u00e9"},
                {"q", "\u00e9", "i", "\u00f3", "q", "s", "b", "s"},
                {"c", "u", "m", "y", "v", "l", "r", "x"},
                {"\u00fc", "\u00ed", "\u00f3", "m", "o", "t", "e", "k"},
                {"a", "g", "r", "n", "n", "\u00f3", "s", "m"},});
        challenge.setSourceLocale(Locale.ENGLISH);
        challenge.setTargetLocale(new Locale("es", "ES"));
        HashMap<String, String> wordLocations = new HashMap<>();
        wordLocations.put("6,1,6,2,6,3,6,4,6,5,6,6", "hombre");
        challenge.setWordLocations(wordLocations);
        viewModel.loadCrossword(challenge);

        //Check data
        loadTargetPath();

    }


    private void loadTargetPath() {
        _targetPath.add(new Point(6,1));
        _targetPath.add(new Point(6,2));
        _targetPath.add(new Point(6,3));
        _targetPath.add(new Point(6,4));
        _targetPath.add(new Point(6,5));
        _targetPath.add(new Point(6,6));
    }
    //endregion

    @Test
    public void testTargetWordPath() {
        assert(Objects.equals(viewModel.getTargetWordLocations().getValue(), _targetPath));
    }

    @Test
    public void testSourceLabel() {
        assert(Objects.equals(viewModel.getSourceLocaleLabel().getValue(), "English:"));
    }

    @Test
    public void testTargetLabel() {
        assert(Objects.equals(viewModel.getTargetLocaleLabel().getValue(), "Spanish:"));
    }

    @Test
    public void testSourceWord() {
        assert(Objects.equals(viewModel.getSourceWord().getValue(), "man"));
    }

    @Test
    public void testTargetWord() {
        assert(Objects.equals(viewModel.getTargetWord().getValue(), "hombre"));
    }

    //test logic for checking highlighted letters against the target word
    @Test
    public void testCorrectGuess() {
        viewModel.checkSelectedPath(_targetPath);
        assert(viewModel.isSelectionCorrect().getValue());
    }

    @Test
    public void testIncorrectGuess() {
        _targetPath.add(new Point(7,6));
        viewModel.checkSelectedPath(_targetPath);
        assert(!viewModel.isSelectionCorrect().getValue());
    }

    //test logic for mapping user presses to letters on the crossword puzzle
    @Test
    public void testPressedPath() {
        List<Point> testPath = new ArrayList<>();
        testPath.add(new Point(7,7)); //m
        testPath.add(new Point(6,6)); //e
        testPath.add(new Point(5,5)); //l
        testPath.add(new Point(4,4)); //q
        viewModel.setPressedPath(testPath);
        assert(Objects.equals(viewModel.getSelectedLetters().getValue(), "melq"));
    }
}
