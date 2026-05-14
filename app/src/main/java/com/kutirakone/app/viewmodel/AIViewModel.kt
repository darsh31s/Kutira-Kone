package com.kutirakone.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kutirakone.app.model.DesignIdea
import com.kutirakone.app.repository.AIRepository
import com.kutirakone.app.ui.common.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AIViewModel(
    private val aiRepository: AIRepository = AIRepository()
) : ViewModel() {

    private val _designIdeas = MutableStateFlow<UiState<List<DesignIdea>>>(UiState.Idle)
    val designIdeas: StateFlow<UiState<List<DesignIdea>>> = _designIdeas.asStateFlow()

    val inspireMaterial = MutableStateFlow("")
    val inspireSize = MutableStateFlow("")

    fun generateIdeasForListing(listingId: String, material: String, sizeMetres: Double) {
        viewModelScope.launch {
            aiRepository.generateDesignIdeas(material, sizeMetres, listingId).collect {
                _designIdeas.value = it
            }
        }
    }

    fun generateInspireIdeas() {
        val material = inspireMaterial.value
        val sizeStr = inspireSize.value
        val size = sizeStr.toDoubleOrNull()
        
        if (material.isEmpty() || size == null || size <= 0) {
            _designIdeas.value = UiState.Error("Please enter valid material and size")
            return
        }

        viewModelScope.launch {
            aiRepository.generateDesignIdeas(material, size).collect {
                _designIdeas.value = it
            }
        }
    }

    fun clearIdeas() {
        _designIdeas.value = UiState.Idle
        inspireMaterial.value = ""
        inspireSize.value = ""
    }
}
