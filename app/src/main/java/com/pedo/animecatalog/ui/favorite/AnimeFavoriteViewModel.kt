package com.pedo.animecatalog.ui.favorite

import android.app.Application
import androidx.lifecycle.*
import com.pedo.animecatalog.database.getDatabase
import com.pedo.animecatalog.domain.Anime
import com.pedo.animecatalog.repository.AnimeRepository
import com.pedo.animecatalog.utils.AnimeListingStatus
import com.pedo.animecatalog.utils.TYPE_LIST
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class AnimeFavoriteViewModel(app : Application) : AndroidViewModel(app) {
    //coroutine
    private val viewModelJob = Job()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val repository : AnimeRepository = AnimeRepository(getDatabase(app))

    //livedata
    //selected movie todetail
    private val _navigateToDetail = MutableLiveData<Anime>()
    val navigateToDetail : LiveData<Anime>
        get() = _navigateToDetail
    //request status (api status)
    private val _status = MutableLiveData<AnimeListingStatus>()

    val status : LiveData<AnimeListingStatus>
        get() = _status
    private val _animes = MutableLiveData<List<Anime>>()
    val animes : LiveData<List<Anime>>
        get() = _animes

    private val _viewMode = MutableLiveData<String>()
    val viewMode : LiveData<String>
        get() = _viewMode

    init {
        initFavoriteAnime()
        _viewMode.value = TYPE_LIST
    }

    fun initFavoriteAnime(){
        viewModelScope.launch {
            _status.value = AnimeListingStatus.LOADING
            _animes.value = repository.getFavoriteAnime()
            _status.value = AnimeListingStatus.DONE
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun displayMovieDetail(movie: Anime){
        Timber.d("Anime : ${movie.title}, url = ${movie.url}")
        _navigateToDetail.value = movie
    }

    fun displayMovieDetailCompleted(){
        _navigateToDetail.value = null
    }

    fun onRefresh(){
        initFavoriteAnime()
    }

    fun changeViewType(value : String){
        _viewMode.value = value
        onRefresh()
    }


    class Factory(val type : String,val app: Application) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            if(modelClass.isAssignableFrom(AnimeFavoriteViewModel::class.java)){
                return AnimeFavoriteViewModel(app) as T
            }
            throw IllegalArgumentException("Unable To Construct ViewModel")
        }

    }
}