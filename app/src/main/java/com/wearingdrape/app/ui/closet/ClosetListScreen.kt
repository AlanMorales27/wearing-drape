package com.wearingdrape.app.ui.closet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.wearingdrape.app.R
import com.wearingdrape.app.data.Garment
import com.wearingdrape.app.data.GarmentGeneral
import com.wearingdrape.app.ui.colorFromHex

@Composable
fun ClosetListScreen(
    onAddGarment: () -> Unit,
    showSavedMessage: Boolean,
    onSavedMessageShown: () -> Unit,
    viewModel: ClosetListViewModel = viewModel(factory = ClosetListViewModel.factory(
        androidx.compose.ui.platform.LocalContext.current.applicationContext
            as android.app.Application,
    )),
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val savedMessage = stringResource(R.string.garment_saved)
    // Orden fijo: Todas + un tab por grupo (contrato closet-list).
    val tabs = remember {
        listOf(null) + GarmentGeneral.entries.toList()
    }

    LaunchedEffect(showSavedMessage) {
        if (showSavedMessage) {
            snackbarHostState.showSnackbar(savedMessage)
            onSavedMessageShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ScrollableTabRow(selectedTabIndex = tabs.indexOf(selectedTab), edgePadding = 0.dp) {
                tabs.forEach { tab ->
                    Tab(
                        selected = tab == selectedTab,
                        onClick = { viewModel.onTabSelected(tab) },
                        text = {
                            Text(
                                when (tab) {
                                    null -> stringResource(R.string.tab_all)
                                    GarmentGeneral.SUPERIOR -> stringResource(R.string.general_superior)
                                    GarmentGeneral.INFERIOR -> stringResource(R.string.general_inferior)
                                    GarmentGeneral.CALZADO -> stringResource(R.string.general_calzado)
                                    GarmentGeneral.ACCESORIO -> stringResource(R.string.general_accesorio)
                                },
                            )
                        },
                    )
                }
            }
            when (val state = uiState) {
                is ClosetListUiState.Empty -> Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.closet_empty),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Button(onClick = onAddGarment) {
                            Text(stringResource(R.string.add_garment))
                        }
                    }
                }
                is ClosetListUiState.Content -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.items, key = { it.id }) { garment ->
                            GarmentCard(
                                garment = garment,
                                photoFile = viewModel.photoFile(garment.photoPath),
                            )
                        }
                    }
                    Button(
                        onClick = onAddGarment,
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                    ) {
                        Text(stringResource(R.string.add_garment))
                    }
                }
            }
        }
    }
}

@Composable
private fun GarmentCard(garment: Garment, photoFile: java.io.File?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (photoFile != null) {
                AsyncImage(
                    model = photoFile,
                    contentDescription = garment.name,
                    modifier = Modifier.size(64.dp),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.image_unavailable),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = garment.name, style = MaterialTheme.typography.titleMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(colorFromHex(garment.color))
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                    )
                    Text(
                        text = "${garment.specific} · ${garment.color}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (!garment.fit.isNullOrBlank()) {
                    Text(text = garment.fit, style = MaterialTheme.typography.bodySmall)
                }
                if (garment.notes.isNotBlank()) {
                    Text(text = garment.notes, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
