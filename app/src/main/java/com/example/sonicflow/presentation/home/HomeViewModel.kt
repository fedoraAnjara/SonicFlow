package com.example.sonicflow.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.domain.usecase.GetAudioTracksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAudioTracksUseCase: GetAudioTracksUseCase
) : ViewModel(){
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadAudioTracks()
    }

    fun loadAudioTracks() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try{
                val tracks = getAudioTracksUseCase()

                _state.value = _state.value.copy(
                    audioTracks = tracks,
                    isLoading = false
                )
            } catch (e: Exception){
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
}