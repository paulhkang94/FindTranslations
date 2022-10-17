package com.pkang.findtranslations.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.pkang.findtranslations.model.CrosswordChallenge;
import com.pkang.findtranslations.model.RetrieveCrosswordTask;

import java.util.List;
import java.util.Objects;

/**
 * ViewModel class representing the overall crossword JSON data
 * Handles loading data in background thread
 */
public class CrosswordGameViewModel extends ViewModel {
    private static final String URL = "https://s3.amazonaws.com/duolingo-data/s3/js2/find_challenges.txt";

    private MutableLiveData<List<CrosswordChallenge>> _challenges;
    private MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    private MutableLiveData<String> _error = new MutableLiveData<>();

    private MutableLiveData<Integer> _currentScreen = new MutableLiveData<>();
    private MutableLiveData<Boolean> _isLastLevel = new MutableLiveData<>();
    private MutableLiveData<Boolean> _showTabs = new MutableLiveData<>();
    private MutableLiveData<Boolean> _gameComplete = new MutableLiveData<>();

    public LiveData<List<CrosswordChallenge>> getChallenges() {
        if (_challenges == null) {
            _challenges = new MutableLiveData<>();
            loadCrosswordChallenges();
        }
        return _challenges;
    }

    public LiveData<Integer> getCurrentScreen() {
        return _currentScreen;
    }

    public void setScreenComplete(int screenIndex) {
        if (screenIndex <= Objects.requireNonNull(_challenges.getValue()).size() - 1) {
            int nextScreen = screenIndex + 1;
            _currentScreen.setValue(nextScreen);
            _isLastLevel.setValue(nextScreen == _challenges.getValue().size());
        } else {
            setGameComplete(true);
            _showTabs.setValue(true);
        }
    }

    public LiveData<Boolean> isLastLevel() {
        return _isLastLevel;
    }

    public LiveData<Boolean> isGameComplete() {
        return _gameComplete;
    }

    private void setGameComplete(boolean complete) {
        _gameComplete.setValue(complete);
    }

    public LiveData<Boolean> getShowTabs() { return _showTabs; }

    //region data load
    public LiveData<Boolean> isLoading() {
        return _isLoading;
    }

    public LiveData<String> getError() {
        return _error;
    }

    private void loadCrosswordChallenges() {
        new RetrieveCrosswordTask(new RetrieveCrosswordTask.Callback() {
            @Override
            public void onLoadingStarted() {
                _isLoading.postValue(true);
            }

            @Override
            public void onLoadingSucceeded(List<CrosswordChallenge> crosswordChallenges) {
                _isLoading.setValue(false);
                _challenges.setValue(crosswordChallenges);
            }

            @Override
            public void onLoadingFailed(String errorMessage) {
                _isLoading.postValue(false);
                _error.postValue(errorMessage);
            }
        }).execute(URL);
    }
    //endregion
}
