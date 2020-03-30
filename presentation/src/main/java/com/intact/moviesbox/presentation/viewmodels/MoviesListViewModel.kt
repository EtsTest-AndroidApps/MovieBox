package com.intact.moviesbox.presentation.viewmodels

import androidx.lifecycle.MutableLiveData
import com.intact.moviesbox.domain.entities.PopularMoviesEntity
import com.intact.moviesbox.domain.entities.TrendingMoviesEntity
import com.intact.moviesbox.domain.usecases.PopularMoviesUseCase
import com.intact.moviesbox.domain.usecases.TrendingMoviesUseCase
import com.intact.moviesbox.presentation.mapper.Mapper
import com.intact.moviesbox.presentation.model.ErrorDTO
import com.intact.moviesbox.presentation.model.MovieDTO
import com.intact.moviesbox.presentation.model.PopularMoviesDTO
import com.intact.moviesbox.presentation.model.TrendingMoviesDTO
import com.intact.moviesbox.presentation.viewmodels.base.BaseViewModel
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

/**
 * view models don't care about the source of data. They are only
 * dependable on the observables handed over by use cases. View models
 * don't have any idea how to get and set the data. View models convert these
 * observables into live data using live data reactive streams and expose
 * only live data
 *
 * If you want an Observable to emit a specific sequence of items before it
 * begins emitting the items normally expected from it, apply the StartWith
 * operator to it.
 *
 * handling the error cases in rx can be checked at
 * https://github.com/ReactiveX/RxJava/wiki/Error-Handling-Operators
 * doOnError, onErrorComplete, onErrorResumeNext, onErrorReturn, onErrorReturnItem
 * onExceptionResumeNext, retry, retryUntil, retryWhen
 */

class MoviesListViewModel @Inject constructor(
    private val popularMoviesUseCase: PopularMoviesUseCase,
    private val trendingMoviesUseCase: TrendingMoviesUseCase,
    private val popularMoviesMapper: Mapper<PopularMoviesEntity, PopularMoviesDTO>,
    private val trendingMoviesMapper: Mapper<TrendingMoviesEntity, TrendingMoviesDTO>
) : BaseViewModel() {

    private val isLoading = MutableLiveData<Boolean>()
    private val errorLiveData = MutableLiveData<ErrorDTO>()
    private val popularMoviesLiveData = MutableLiveData<ArrayList<MovieDTO>>()
    private val trendingMoviesLiveData = MutableLiveData<ArrayList<MovieDTO>>()
    private fun getCompositeDisposable() = CompositeDisposable()

    // get trending movies
    fun getTrendingMovies(pageNumber: String) {
        isLoading.value = true
        getCompositeDisposable().add(
            trendingMoviesUseCase.buildUseCase(TrendingMoviesUseCase.Param(pageNumber = pageNumber))
                .map { trendingMoviesMapper.to(it) }
                .subscribe({ it ->
                    Timber.d("Success: Trending movies response received: ${it.movies}")
                    trendingMoviesLiveData.value = it.movies
                }, {
                    errorLiveData.value = ErrorDTO(code = 400, message = it.localizedMessage)
                })
        )
    }

    // get popular movies
    fun getPopularMovies(pageNumber: String) {
        isLoading.value = true
        getCompositeDisposable().add(
            popularMoviesUseCase.buildUseCase(PopularMoviesUseCase.Param(pageNumber = pageNumber))
                .map { popularMoviesMapper.to(it) }
                .subscribe({ it ->
                    Timber.d("Success: Popular movies response received: ${it.movies}")
                    popularMoviesLiveData.value = it.movies
                }, {
                    errorLiveData.value = ErrorDTO(code = 400, message = it.localizedMessage)
                })
        )
    }

    fun getErrorLiveData() = errorLiveData
    fun getPopularMoviesLiveData() = popularMoviesLiveData
    fun getTrendingMoviesLiveData() = trendingMoviesLiveData

    override fun onCleared() {
        super.onCleared()
        getCompositeDisposable().clear()
    }
}