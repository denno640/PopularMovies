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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.dennis.popularmovies.R;
import com.example.dennis.popularmovies.pojos.SingleMovie;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * could not set ButterKnife with gradle 3.1.4
 * ButterKnife will be used later when i figure out a workaround
 * For now its boilerplate code
 */

public class DetailsActivity extends AppCompatActivity {
    private ImageView movie_poster_iv;
    private ProgressBar poster_download_pgbar;
    SingleMovie singleMovie;
    private TextView retry_tv;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        movie_poster_iv=findViewById(R.id.movie_poster_iv);
        TextView orig_title_tv = findViewById(R.id.orig_title_tv);
        TextView release_date_tv = findViewById(R.id.release_date_tv);
        TextView rating_tv = findViewById(R.id.rating_tv);
        TextView plot_tv = findViewById(R.id.plot_tv);
        retry_tv = findViewById(R.id.retry_tv);
        poster_download_pgbar= findViewById(R.id.poster_download_pgbar);
        poster_download_pgbar.setVisibility(View.VISIBLE);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if(savedInstanceState != null){
            if(savedInstanceState.containsKey(getString(R.string.singleMovie))){
                singleMovie = savedInstanceState.getParcelable(getString(R.string.singleMovie));
            }
        }
        if(getIntent() != null) {
            if(getIntent().hasExtra(getString(R.string.singleMovie)))
            singleMovie = getIntent().getParcelableExtra(getString(R.string.singleMovie));
        }
        Picasso.with(this)
                .load(getString(R.string.base_image_url)+singleMovie.getPosterPath())
                .priority(Picasso.Priority.HIGH)
                .into(movie_poster_iv, new Callback() {
                    @Override
                    public void onSuccess() {
                        retry_tv.setVisibility(View.INVISIBLE);
                       poster_download_pgbar.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onError() {
                        poster_download_pgbar.setVisibility(View.INVISIBLE);
                        retry_tv.setVisibility(View.VISIBLE);

                    }
                });
        //setting up original title
        orig_title_tv.setText(String.format("%s%s", getString(R.string.original_movie_title), singleMovie.getOriginalTitle()));
        //setting up user rating
        rating_tv.setText(String.format("%s%s", getString(R.string.user_rating), singleMovie.getVoteAverage()));
        //setting up release date
        release_date_tv.setText(String.format("%s%s", getString(R.string.release_date), singleMovie.getReleaseDate()));
        //setting up synopsis
        plot_tv.setText(String.format("%s%s", getString(R.string.plot_synopsis), singleMovie.getOverview()));

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(singleMovie != null)
        outState.putParcelable(getString(R.string.singleMovie),singleMovie);
    }

    public void retryPosterDownload(View view) {
        poster_download_pgbar.setVisibility(View.VISIBLE);
        Picasso.with(this)
                .load(getString(R.string.base_image_url)+singleMovie.getPosterPath())
                .priority(Picasso.Priority.HIGH)
                .into(movie_poster_iv, new Callback() {
                    @Override
                    public void onSuccess() {
                        retry_tv.setVisibility(View.INVISIBLE);
                        poster_download_pgbar.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onError() {
                        poster_download_pgbar.setVisibility(View.INVISIBLE);
                        retry_tv.setVisibility(View.VISIBLE);

                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // When the home button is pressed, take the user back to the Main movies page
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }
}
