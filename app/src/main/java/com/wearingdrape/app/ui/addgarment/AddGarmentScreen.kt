package com.wearingdrape.app.ui.addgarment

import android.app.Application
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.wearingdrape.app.R
import com.wearingdrape.app.data.GarmentGeneral
import com.wearingdrape.app.data.SaveResult
import com.wearingdrape.app.ui.colorFromHex

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddGarmentScreen(
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    viewModel: AddGarmentViewModel = viewModel(factory = AddGarmentViewModel.factory(
        LocalContext.current.applicationContext as Application,
    )),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCancelDialog by remember { mutableStateOf(false) }
    val dirty = state.name.isNotBlank() || state.specific.isNotBlank() ||
        state.color.isNotBlank() || state.fit.isNotBlank() ||
        state.notes.isNotBlank() || state.photoUri != null ||
        state.generalKey != null

    // PickVisualMedia (androidx) usa el photo picker del sistema cuando está
    // disponible y hace fallback a ACTION_OPEN_DOCUMENT en API 29-32,
    // por lo que funciona en todo el rango soportado sin permisos amplios.
    val pickMediaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        // null = usuario canceló; no se toca el resto del formulario.
        if (uri != null) viewModel.onPhotoPick(uri)
    }

    LaunchedEffect(state.saveResult) {
        if (state.saveResult is SaveResult.Ok) {
            viewModel.consumeSaveResult()
            onSaved()
        }
    }

    state.generalChangeNotice?.let { notice ->
        LaunchedEffect(notice) {
            snackbarHostState.showSnackbar(notice)
            viewModel.consumeGeneralNotice()
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            confirmButton = {
                Button(onClick = {
                    showCancelDialog = false
                    onCancel()
                }) { Text(stringResource(R.string.discard_changes)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCancelDialog = false }) {
                    Text(stringResource(R.string.keep_editing))
                }
            },
            title = { Text(stringResource(R.string.cancel_title)) },
            text = { Text(stringResource(R.string.cancel_message)) },
        )
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Foto
            if (state.photoUri != null) {
                AsyncImage(
                    model = state.photoUri,
                    contentDescription = null,
                    modifier = Modifier.size(160.dp),
                    contentScale = ContentScale.Crop,
                )
            }
            OutlinedButton(onClick = {
                pickMediaLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            }) {
                Text(stringResource(R.string.pick_photo))
            }
            state.fieldErrors.photo?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            // Nombre
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text(stringResource(R.string.field_name)) },
                isError = state.fieldErrors.name != null,
                supportingText = { state.fieldErrors.name?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            // Tipo general
            var generalExpanded by remember { mutableStateOf(false) }
            val generalLabels = mapOf(
                GarmentGeneral.SUPERIOR.key to stringResource(R.string.general_superior),
                GarmentGeneral.INFERIOR.key to stringResource(R.string.general_inferior),
                GarmentGeneral.CALZADO.key to stringResource(R.string.general_calzado),
                GarmentGeneral.ACCESORIO.key to stringResource(R.string.general_accesorio),
            )
            ExposedDropdownMenuBox(
                expanded = generalExpanded,
                onExpandedChange = { generalExpanded = !generalExpanded },
            ) {
                OutlinedTextField(
                    value = state.generalKey?.let { generalLabels[it] } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.field_general)) },
                    isError = state.fieldErrors.generalKey != null,
                    supportingText = { state.fieldErrors.generalKey?.let { Text(it) } },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(generalExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                )
                ExposedDropdownMenu(
                    expanded = generalExpanded,
                    onDismissRequest = { generalExpanded = false },
                ) {
                    GarmentGeneral.entries.forEach { general ->
                        DropdownMenuItem(
                            text = { Text(generalLabels[general.key] ?: general.key) },
                            onClick = {
                                viewModel.onGeneralChange(general.key)
                                generalExpanded = false
                            },
                        )
                    }
                }
            }

            // Tipo específico (filtrado por general; pide elegir general primero)
            val specifics = viewModel.specificsForCurrent()
            var specificExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = specificExpanded,
                onExpandedChange = { specificExpanded = !specificExpanded },
            ) {
                OutlinedTextField(
                    value = state.specific,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.field_specific)) },
                    placeholder = {
                        if (state.generalKey == null) Text(stringResource(R.string.choose_general_first))
                        else if (specifics.isEmpty()) Text(stringResource(R.string.no_options_for_group))
                    },
                    isError = state.fieldErrors.specific != null,
                    supportingText = { state.fieldErrors.specific?.let { Text(it) } },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(specificExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                )
                ExposedDropdownMenu(
                    expanded = specificExpanded,
                    onDismissRequest = { specificExpanded = false },
                ) {
                    specifics.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                viewModel.onSpecificChange(option)
                                specificExpanded = false
                            },
                        )
                    }
                }
            }

            // Color (picker de paleta predefinida, FR-011, solo hex)
            val colorOptions = viewModel.colorOptions
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.field_color),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    if (state.color in colorOptions) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(colorFromHex(state.color))
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                        )
                        Text(
                            text = state.color,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    colorOptions.forEach { hex ->
                        val selected = state.color == hex
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(colorFromHex(hex))
                                .border(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outline
                                    },
                                    shape = CircleShape,
                                )
                                .clickable { viewModel.onColorChange(hex) },
                        )
                    }
                }
                state.fieldErrors.color?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }

            // Fit opcional (sugerencias del grupo; calzado/accesorio sin sugerencias)
            val fits = viewModel.fitsForCurrent()
            if (fits.isEmpty()) {
                OutlinedTextField(
                    value = state.fit,
                    onValueChange = viewModel::onFitChange,
                    label = { Text(stringResource(R.string.field_fit_optional)) },
                    isError = state.fieldErrors.fit != null,
                    supportingText = { state.fieldErrors.fit?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            } else {
                var fitExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = fitExpanded,
                    onExpandedChange = { fitExpanded = !fitExpanded },
                ) {
                    OutlinedTextField(
                        value = state.fit,
                        onValueChange = viewModel::onFitChange,
                        readOnly = false,
                        label = { Text(stringResource(R.string.field_fit_optional)) },
                        isError = state.fieldErrors.fit != null,
                        supportingText = { state.fieldErrors.fit?.let { Text(it) } },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(fitExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        singleLine = true,
                    )
                    ExposedDropdownMenu(
                        expanded = fitExpanded,
                        onDismissRequest = { fitExpanded = false },
                    ) {
                        fits.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    viewModel.onFitChange(option)
                                    fitExpanded = false
                                },
                            )
                        }
                    }
                }
            }

            // Observaciones opcionales
            OutlinedTextField(
                value = state.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text(stringResource(R.string.field_notes_optional)) },
                isError = state.fieldErrors.notes != null,
                supportingText = { state.fieldErrors.notes?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )

            // Guardar / cancelar
            Button(
                onClick = viewModel::onSave,
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text(stringResource(R.string.save_garment))
                }
            }
            OutlinedButton(
                onClick = { if (dirty) showCancelDialog = true else onCancel() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    }
}
