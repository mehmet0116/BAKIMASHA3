package com.assanhanil.techassist.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.assanhanil.techassist.domain.model.BearingSearchResult
import com.assanhanil.techassist.domain.repository.BearingRepository
import com.assanhanil.techassist.domain.usecase.FindBearingUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Bearing Finder feature.
 */
class BearingFinderViewModel(
    private val findBearingUseCase: FindBearingUseCase
) : ViewModel() {

    private val _searchResult = MutableStateFlow<BearingSearchResult?>(null)
    val searchResult: StateFlow<BearingSearchResult?> = _searchResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Search for bearings matching the given dimensions.
     */
    fun searchBearing(
        innerDiameter: Double,
        outerDiameter: Double,
        width: Double,
        tolerance: Double = 0.5
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = findBearingUseCase(
                    innerDiameter = innerDiameter,
                    outerDiameter = outerDiameter,
                    width = width,
                    tolerance = tolerance
                )
                _searchResult.value = result
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear search results.
     */
    fun clearResults() {
        _searchResult.value = null
    }

    /**
     * Factory for creating BearingFinderViewModel with dependencies.
     */
    class Factory(
        private val bearingRepository: BearingRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BearingFinderViewModel::class.java)) {
                return BearingFinderViewModel(
                    findBearingUseCase = FindBearingUseCase(bearingRepository)
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
