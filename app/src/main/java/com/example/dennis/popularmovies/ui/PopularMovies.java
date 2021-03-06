package com.example.dennis.popularmovies.ui;

/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dennis.popularmovies.R;
import com.example.dennis.popularmovies.adapters.PopularMoviesAdapter;
import com.example.dennis.popularmovies.pojos.SingleMovie;
import com.example.dennis.popularmovies.utils.ColumnCalculator;
import com.example.dennis.popularmovies.utils.InjectorUtils;
import com.example.dennis.popularmovies.viewmodels.PopularMoviesViewModel;
import com.example.dennis.popularmovies.viewmodels.PopularMoviesViewModelFactory;

/**
 * could not set ButterKnife with gradle 3.1.4
 * ButterKnife will be used later when i figure out a workaround
 * For now its boilerplate code
 */

public class PopularMovies extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        PopularMoviesAdapter.OnMoviePosterClicked {
    private Toast mToast;
    private SwipeRefreshLayout swipe;
    private PopularMoviesAdapter mAdapter;
    private TextView sort_criteria_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular_movies);
        swipe = findViewById(R.id.swipe);
        RecyclerView moviesRvItem = findViewById(R.id.moviesRvItem);
        swipe.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark, android.R.color.holo_blue_dark, android.R.color.holo_orange_dark,
                android.R.color.holo_purple);
        swipe.setRefreshing(true);
        moviesRvItem.setHasFixedSize(true);
        int columns = ColumnCalculator.calculateNoOfColumns(this);
        GridLayoutManager manager = new GridLayoutManager(this, columns, GridLayoutManager.VERTICAL, false);
        moviesRvItem.setLayoutManager(manager);
        mAdapter = new PopularMoviesAdapter(this);
        moviesRvItem.setAdapter(mAdapter);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String sortCriteria = preferences.getString(getString(R.string.sort_list_key), "");
        if (sortCriteria.equals(""))
            sortCriteria = getString(R.string.most_popular);
        sort_criteria_tv = findViewById(R.id.sort_criteria_tv);
        sort_criteria_tv.setText(sortCriteria);
        PopularMoviesViewModelFactory factory = InjectorUtils.providePopularMoviesViewModelFactory(sortCriteria);
        PopularMoviesViewModel viewModel = ViewModelProviders.of(this, factory).get(PopularMoviesViewModel.class);
        viewModel.getMovieList().observe(this, singleMovies -> mAdapter
                .submitList(singleMovies));
        viewModel.getNetworkState().observe(this, networkState -> {
            if (networkState != null) {
                //some feedback to the user to know what causes the problem
                //in production we cannot tell the user the actual cause of
                //loading failure especially if it relates to our server
                switch (networkState.getStatus()) {
                    case RUNNING:
                        // Toast.makeText(this, "loading...", Toast.LENGTH_SHORT).show();
                        break;
                    case FAILED:
                        // Toast.makeText(this, "failed...", Toast.LENGTH_SHORT).show();
                        if (swipe.isRefreshing()) swipe.setRefreshing(false);
                        if (mToast != null) {
                            mToast.cancel();
                        }
                        String toastMessage = getString(R.string.could_not_load_movies) + networkState.getMsg();
                        mToast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);
                        mToast.show();
                        break;
                    default:
                        if (swipe.isRefreshing()) swipe.setRefreshing(false);
                        break;
                }
                mAdapter.setNetworkState(networkState);
            }

        });
        ///swipe to refresh implementation
        swipe.setOnRefreshListener(() -> {

            if(mAdapter.getCurrentList() != null){
                mAdapter.getCurrentList().getDataSource().invalidate();
            }

        });
        // registering preferenceChangeListener
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

    }

    /**
     * Methods for setting up the menu
     **/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.popular_movies_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            //start settings activity
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called each time the preference changes
     * @param sharedPreferences
     * @param key
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        String newCriteria = sharedPreferences.getString(key, "");
        sort_criteria_tv.setText(newCriteria);
      //as soon as the user chooses a new setting
        swipe.setRefreshing(true);
        if(mAdapter.getCurrentList() != null){
            mAdapter.getCurrentList().getDataSource().invalidate();
        }
    }
//we unregister the preferenceChangeLister when the activity is destroyed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister PopularMovies as an OnPreferenceChangedListener
        // to avoid any memory leaks.
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }
//interface method to open details activity
    @Override
    public void onMoviePosterClicked(SingleMovie singleMovie) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("singleMovie", singleMovie);
        startActivity(intent);

    }
}
