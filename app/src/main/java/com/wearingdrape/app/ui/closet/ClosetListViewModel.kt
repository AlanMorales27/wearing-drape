package com.wearingdrape.app.ui.closet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wearingdrape.app.WearingDrapeApp
import com.wearingdrape.app.data.Garment
import com.wearingdrape.app.data.GarmentGeneral
import com.wearingdrape.app.data.GarmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import android.app.Application

/** Estado UDF de la lista mínima (contrato closet-list-contract). */
sealed interface ClosetListUiState {
    data object Empty : ClosetListUiState
    data class Content(val items: List<Garment>) : ClosetListUiState
}

class ClosetListViewModel(private val repository: GarmentRepository) : ViewModel() {
    /** Tab activo: null = Todas, otro = solo ese grupo (US1-A4). */
    private val _selectedTab = MutableStateFlow<GarmentGeneral?>(null)
    val selectedTab: StateFlow<GarmentGeneral?> = _selectedTab.asStateFlow()

    val uiState: StateFlow<ClosetListUiState> =
        combine(repository.observeGarments(), _selectedTab) { list, tab ->
            val filtered = if (tab == null) list else list.filter { it.generalKey == tab.key }
            if (filtered.isEmpty()) ClosetListUiState.Empty
            else ClosetListUiState.Content(filtered)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ClosetListUiState.Empty)

    fun onTabSelected(general: GarmentGeneral?) {
        _selectedTab.value = general
    }

    fun photoFile(relativePath: String?) = repository.photoFile(relativePath)

    companion object {
        fun factory(app: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repo = (app as WearingDrapeApp).garmentRepository
                    return ClosetListViewModel(repo) as T
                }
            }
    }
}
