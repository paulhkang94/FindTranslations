package com.pkang.findtranslations.view;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.pkang.findtranslations.R;
import com.pkang.findtranslations.databinding.CrosswordScreenBinding;
import com.pkang.findtranslations.model.CrosswordChallenge;
import com.pkang.findtranslations.model.Point;
import com.pkang.findtranslations.viewmodel.CrosswordGameViewModel;
import com.pkang.findtranslations.viewmodel.CrosswordScreenViewModel;

import java.util.List;
import java.util.Objects;

/**
 * Fragment representing a single crossword challenge screen.
 */
public class CrosswordScreenFragment extends Fragment implements CrosswordPuzzleLayout.Listener {

    private static final String CROSSWORD_CHALLENGE_NUMBER = "crossword_number";
    private static final String CROSSWORD_CHALLENGE_DATA = "crossword_data";

    private CrosswordGameViewModel _crosswordGameViewModel;
    private CrosswordScreenViewModel _crosswordScreenViewModel;
    private ProgressBar _progressBar;
    private CrosswordPuzzleLayout _crosswordPuzzleLayout;
    private int _screenIndex;

    static CrosswordScreenFragment newInstance(int index, CrosswordChallenge crosswordChallenge) {
        CrosswordScreenFragment fragment = new CrosswordScreenFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(CROSSWORD_CHALLENGE_NUMBER, index);
        bundle.putSerializable(CROSSWORD_CHALLENGE_DATA, crosswordChallenge);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _crosswordScreenViewModel = ViewModelProviders.of(this).get(CrosswordScreenViewModel.class);
        _crosswordGameViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(CrosswordGameViewModel.class);

        if (getArguments() != null) {
            _screenIndex = getArguments().getInt(CROSSWORD_CHALLENGE_NUMBER);
            CrosswordChallenge crosswordChallenge = (CrosswordChallenge) getArguments().getSerializable(CROSSWORD_CHALLENGE_DATA);
            _crosswordScreenViewModel.loadCrossword(crosswordChallenge);
            _crosswordScreenViewModel.setScreenIndex(_screenIndex);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.crossword_screen, container, false);
        _crosswordPuzzleLayout = root.findViewById(R.id.crossword_puzzle_content);
        _progressBar = root.findViewById(R.id.screen_progress);

        CrosswordScreenBinding binding = CrosswordScreenBinding.bind(root);
        binding.setLifecycleOwner(this);
        binding.setCrosswordPuzzle(_crosswordScreenViewModel);
        binding.setCrosswordGame(_crosswordGameViewModel);
        binding.setCrosswordScreen(this);
        _crosswordScreenViewModel.getCharacterGrid().observe(this, strings -> _crosswordPuzzleLayout.loadCrosswords(strings, CrosswordScreenFragment.this));
        _crosswordScreenViewModel.getTargetWordLocations().observe(this, points -> _crosswordPuzzleLayout.setTargetWordPath(points));

        _crosswordScreenViewModel.getScreenIndex().observe(this, integer -> setScreenProgress(_screenIndex));

        _crosswordScreenViewModel.isSelectionCorrect().observe(this, correct -> {
            setSelectedCorrect(correct);
            _crosswordPuzzleLayout.setScreenCompleted(correct);
        });

        _crosswordGameViewModel.isGameComplete().observe(this, complete -> _progressBar.setVisibility(complete ? View.GONE : View.VISIBLE));

        return root;
    }

    private void setSelectedCorrect(Boolean isCorrect) {
        _crosswordPuzzleLayout.setEnabled(false);

        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            _crosswordPuzzleLayout.setEnabled(true);
            if (!isCorrect) {
                _crosswordScreenViewModel.clearSelectedPath();
            }
        }, 500);
    }

    /**
     * Sets appropriate value for progress bar.
     * @param screenIndex current crossword screen progress
     */
    private void setScreenProgress(int screenIndex) {
        int totalScreens = Objects.requireNonNull(_crosswordGameViewModel.getChallenges().getValue()).size();
        _progressBar.setProgress(screenIndex * 100 / totalScreens);
    }


    @Override
    public void onPathPressed(List<Point> path) {
        _crosswordScreenViewModel.setPressedPath(path);
    }


    @Override
    public void onPathSelected(List<Point> path) {
        _crosswordScreenViewModel.checkSelectedPath(path);
    }

    public void onNextPressed(boolean complete) {
        _crosswordGameViewModel.setScreenComplete(_screenIndex);
    }
}