package com.pkang.findtranslations.view;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.tabs.TabLayout;
import com.pkang.findtranslations.R;
import com.pkang.findtranslations.databinding.ActivityMainBinding;
import com.pkang.findtranslations.viewmodel.CrosswordGameViewModel;

public class MainActivity extends AppCompatActivity {
    private CrosswordGameViewModel _gameViewModel;

    private CrosswordPagerAdapter _adapter;
    private ProgressBar _progressBar;
    private Button _refreshButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setLifecycleOwner(this);
        binding.setActivity(this);

        _gameViewModel = ViewModelProviders.of(this).get(CrosswordGameViewModel.class);
        setScreenLogic(_gameViewModel);

        _progressBar = findViewById(R.id.progress_bar);
        _refreshButton = findViewById(R.id.refresh_button);
        loadCrosswordData();
    }

    /**
     * Subscribe to Crossword Game ViewModel callbacks
     * - Screen has changed
     * - Game is complete
     * @param gameModel ViewModel representing overall crossword game activity.
     */
    private void setScreenLogic(CrosswordGameViewModel gameModel) {
        gameModel.getCurrentScreen().observe(this, screenNumber -> {
            CrosswordViewPager viewPager = findViewById(R.id.view_pager);
            viewPager.setCurrentItem(screenNumber - 1, true);
        });

        gameModel.isGameComplete().observe(this, complete -> {
            if (complete) {
                Toast.makeText(MainActivity.this,R.string.game_won_congratulations_message, Toast.LENGTH_LONG).show();
            }
        });
    }

    //region retrieve data from web

    private void loadCrosswordData() {
        //check for internet connection
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        if (isConnected) {
            createCrosswordScreens();
            retrieveJSONData(_gameViewModel);
        } else {
            Toast.makeText(this, "No internet connection, please try again.", Toast.LENGTH_SHORT).show();
            _refreshButton.setVisibility(View.VISIBLE);
        }
    }

    private void retrieveJSONData(CrosswordGameViewModel gameModel) {
        gameModel.getChallenges().observe(this, crosswordChallenges -> _adapter.notifyDataSetChanged());

        gameModel.isLoading().observe(this, isLoading -> {
            _refreshButton.setVisibility(View.GONE);
            _progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        gameModel.getError().observe(this, errorMessage -> {
            Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            _refreshButton.setVisibility(View.VISIBLE);
        });
    }

    public void onRefresh() {
        loadCrosswordData();
    }
    //endregion

    private void createCrosswordScreens() {
        _adapter = new CrosswordPagerAdapter(_gameViewModel.getChallenges(), getSupportFragmentManager());
        CrosswordViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(_adapter);

        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        _gameViewModel.getShowTabs().observe(this, show -> tabs.setVisibility(show ? View.VISIBLE : View.GONE));
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.exit_confirmation_title).setMessage(R.string.exit_confirmation_message)
                .setCancelable(true)
                .setPositiveButton(R.string.quit, (dialog, which) -> MainActivity.super.onBackPressed()).setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        AlertDialog exitAlert = builder.create();
        exitAlert.show();
    }
}