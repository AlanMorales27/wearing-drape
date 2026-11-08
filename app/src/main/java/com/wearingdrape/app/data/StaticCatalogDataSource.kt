package com.wearingdrape.app.data

/** Expone el catálogo estático ([specificsByGeneral]/[fitsByGeneral]) a la capa UI vía repositorio. */
class StaticCatalogDataSource {
    val specifics: Map<GarmentGeneral, List<String>> get() = specificsByGeneral
    val fits: Map<GarmentGeneral, List<String>> get() = fitsByGeneral
    /** Paleta predefinida del picker de color (FR-011, solo hex). */
    val colors: List<String> get() = colorOptions

    fun specificsFor(general: GarmentGeneral?): List<String> =
        specificsByGeneral[general] ?: emptyList()

    fun fitsFor(general: GarmentGeneral?): List<String> =
        fitsByGeneral[general] ?: emptyList()
}
