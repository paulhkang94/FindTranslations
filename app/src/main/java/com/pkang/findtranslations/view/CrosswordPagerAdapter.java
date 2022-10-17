package com.pkang.findtranslations.view;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.LiveData;

import com.pkang.findtranslations.model.CrosswordChallenge;

import java.util.List;
import java.util.Objects;

/**
 * FragmentPagerAdapter that presents the different CrossWordScreenFragments
 * one of the sections/tabs/pages.
 */
public class CrosswordPagerAdapter extends FragmentPagerAdapter {

    private LiveData<List<CrosswordChallenge>> _crosswordChallenges;

    CrosswordPagerAdapter(LiveData<List<CrosswordChallenge>> crosswords, FragmentManager fm) {
        super(fm);
        _crosswordChallenges = crosswords;
    }

    @Override
    public Fragment getItem(int position) {
        //create a crossword screen for each crossword challenge
        return CrosswordScreenFragment.newInstance(position + 1, Objects.requireNonNull(_crosswordChallenges.getValue()).get(position));
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return String.valueOf(position + 1);
    }

    @Override
    public int getCount() {
        return _crosswordChallenges.getValue() == null ? 0 : _crosswordChallenges.getValue().size();
    }
}