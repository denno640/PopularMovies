package com.example.dennis.popularmovies;

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

import android.arch.lifecycle.LiveData;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;

import com.example.dennis.popularmovies.api.MoviesDataSourceFactory;
import com.example.dennis.popularmovies.pojos.SingleMovie;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MoviesRepository {
    private Executor mExecutor;
    private static final Object LOCK = new Object();
    private static MoviesRepository sInstance;

    public MoviesRepository() {
        mExecutor= Executors.newFixedThreadPool(3);

    }


    public synchronized static MoviesRepository getInstance() {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new MoviesRepository();
            }
        }
        return sInstance;
    }


    public LiveData<PagedList<SingleMovie>> provieMovieList(MoviesDataSourceFactory factory) {
        //initial number of movies to download per request is set at 20
        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder()).setEnablePlaceholders(false)
                        .setInitialLoadSizeHint(20)
                        .setPageSize(20).build();
        return (new LivePagedListBuilder(factory, pagedListConfig))
                .setFetchExecutor(mExecutor)/*BackgroundThreadExecutor(executor)*/
                .build();
    }
}
