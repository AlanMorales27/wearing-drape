package com.wearingdrape.app.ui.addgarment

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wearingdrape.app.WearingDrapeApp
import com.wearingdrape.app.data.FilePhotoDataSource
import com.wearingdrape.app.data.GarmentGeneral
import com.wearingdrape.app.data.GarmentRepository
import com.wearingdrape.app.data.GarmentValidationErrors
import com.wearingdrape.app.data.SaveResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado UDF del formulario (contrato add-garment-flow-contract). */
data class AddGarmentUiState(
    val name: String = "",
    val generalKey: String? = null,
    val specific: String = "",
    val color: String = "",
    val fit: String = "",
    val notes: String = "",
    val photoUri: Uri? = null,
    val fieldErrors: GarmentValidationErrors = GarmentValidationErrors(),
    val generalChangeNotice: String? = null,
    val isSaving: Boolean = false,
    val saveResult: SaveResult? = null,
)

class AddGarmentViewModel(
    private val app: Application,
    private val repository: GarmentRepository,
    private val photos: FilePhotoDataSource,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddGarmentUiState())
    val uiState: StateFlow<AddGarmentUiState> = _uiState.asStateFlow()

    val specificsByGeneral get() = repository.specificsByGeneral
    val fitsByGeneral get() = repository.fitsByGeneral
    /** Paleta predefinida del picker de color (FR-011, solo hex). */
    val colorOptions get() = repository.colorOptions

    fun specificsForCurrent(): List<String> {
        val general = GarmentGeneral.fromKey(_uiState.value.generalKey) ?: return emptyList()
        return repository.specificsByGeneral[general] ?: emptyList()
    }

    fun fitsForCurrent(): List<String> {
        val general = GarmentGeneral.fromKey(_uiState.value.generalKey) ?: return emptyList()
        return repository.fitsByGeneral[general] ?: emptyList()
    }

    fun onNameChange(value: String) = _uiState.update {
        it.copy(name = value, fieldErrors = it.fieldErrors.copy(name = null))
    }

    fun onColorChange(value: String) = _uiState.update {
        it.copy(color = value, fieldErrors = it.fieldErrors.copy(color = null))
    }

    fun onNotesChange(value: String) = _uiState.update {
        val capped = if (value.length > 500) value.take(500) else value
        it.copy(notes = capped, fieldErrors = it.fieldErrors.copy(notes = null))
    }

    fun onFitChange(value: String) = _uiState.update {
        it.copy(fit = value, fieldErrors = it.fieldErrors.copy(fit = null))
    }

    fun onSpecificChange(value: String) = _uiState.update {
        it.copy(specific = value, fieldErrors = it.fieldErrors.copy(specific = null))
    }

    /**
     * FR-006: al cambiar el tipo general, si `specific`/`fit` previos no
     * pertenecen al nuevo grupo se limpian y se informa al usuario.
     */
    fun onGeneralChange(generalKey: String?) {
        _uiState.update { current ->
            val general = GarmentGeneral.fromKey(generalKey)
            val allowedSpecifics = repository.specificsByGeneral[general] ?: emptyList()
            val allowedFits = repository.fitsByGeneral[general] ?: emptyList()
            var notice: String? = null
            var specific = current.specific
            var fit = current.fit
            if (specific.isNotBlank() && specific !in allowedSpecifics) {
                specific = ""
                notice = app.getString(com.wearingdrape.app.R.string.general_reset_specific)
            }
            if (fit.isNotBlank() && allowedFits.isNotEmpty() && fit !in allowedFits) {
                fit = ""
                val fitNotice = app.getString(com.wearingdrape.app.R.string.general_reset_fit)
                notice = if (notice == null) fitNotice else "$notice $fitNotice"
            }
            current.copy(
                generalKey = generalKey,
                specific = specific,
                fit = fit,
                generalChangeNotice = notice,
                fieldErrors = current.fieldErrors.copy(generalKey = null, specific = null, fit = null),
            )
        }
    }

    fun onPhotoPick(uri: Uri?) {
        if (uri == null) return
        when (val v = photos.validate(uri)) {
            is FilePhotoDataSource.Validation.Ok -> _uiState.update {
                it.copy(photoUri = uri, fieldErrors = it.fieldErrors.copy(photo = null))
            }
            is FilePhotoDataSource.Validation.Rejected -> _uiState.update {
                // Foto inválida: error claro sin perder el resto (US1-A3).
                it.copy(fieldErrors = it.fieldErrors.copy(photo = v.message))
            }
        }
    }

    fun consumeGeneralNotice() = _uiState.update { it.copy(generalChangeNotice = null) }

    fun consumeSaveResult() = _uiState.update { it.copy(saveResult = null) }

    fun onSave() {
        val current = _uiState.value
        if (current.isSaving) return
        _uiState.update { it.copy(isSaving = true, saveResult = null) }
        viewModelScope.launch {
            val result = repository.save(
                name = current.name,
                generalKey = current.generalKey,
                specific = current.specific.ifBlank { null },
                color = current.color,
                fit = current.fit.ifBlank { null },
                notes = current.notes,
                photoUri = current.photoUri,
            )
            _uiState.update { it.copy(isSaving = false, saveResult = result) }
            if (result is SaveResult.Rejected) {
                _uiState.update { it.copy(fieldErrors = result.errors) }
            }
        }
    }

    companion object {
        fun factory(app: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val wda = app as WearingDrapeApp
                    return AddGarmentViewModel(
                        app = app,
                        repository = wda.garmentRepository,
                        photos = FilePhotoDataSource(app),
                    ) as T
                }
            }
    }
}
