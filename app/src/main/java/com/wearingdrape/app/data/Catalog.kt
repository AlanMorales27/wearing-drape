package com.wearingdrape.app.data

/**
 * Catálogo estático versionado en código (spec 001, FR-013).
 *
 * Las prendas guardan strings snapshot ([GarmentEntity.specific] / `fit`),
 * no FKs al catálogo: agregar opciones aquí nunca modifica prendas guardadas.
 */
enum class GarmentGeneral(val key: String) {
    SUPERIOR("SUPERIOR"),
    INFERIOR("INFERIOR"),
    CALZADO("CALZADO"),
    ACCESORIO("ACCESORIO");

    companion object {
        fun fromKey(key: String?): GarmentGeneral? =
            entries.firstOrNull { it.key == key }
    }
}

/** Tipos específicos por grupo (seed verbatim de spec.md Assumptions). */
val specificsByGeneral: Map<GarmentGeneral, List<String>> = mapOf(
    GarmentGeneral.SUPERIOR to listOf("camiseta", "camisa", "chaqueta", "sudadera", "jersey"),
    GarmentGeneral.INFERIOR to listOf("jean", "pantalón", "short", "falda"),
    GarmentGeneral.CALZADO to listOf("tenis", "botas", "zapatos", "sandalias"),
    GarmentGeneral.ACCESORIO to listOf("gorro", "gorra", "cinturón", "bufanda"),
)

/**
 * Fits sugeridos por grupo (seed verbatim de spec.md Assumptions).
 * CALZADO y ACCESORIO no tienen sugerencias: campo libre u oculto según UI,
 * nunca se sugieren fits de otro grupo (FR-005).
 */
val fitsByGeneral: Map<GarmentGeneral, List<String>> = mapOf(
    GarmentGeneral.SUPERIOR to listOf("oversize", "boxy", "regular", "slim"),
    GarmentGeneral.INFERIOR to listOf("loose fit", "baggy fit", "slim", "recto", "skinny"),
    GarmentGeneral.CALZADO to emptyList(),
    GarmentGeneral.ACCESORIO to emptyList(),
)

/**
 * Paleta predefinida del picker de color (spec 001 FR-011).
 * Solo hexadecimales "#RRGGBB", sin nombres ni texto libre: la prenda
 * guarda el hex elegido.
 */
val colorOptions: List<String> = listOf(
    "#000000",
    "#FFFFFF",
    "#9E9E9E",
    "#1E3A8A",
    "#29B6F6",
    "#D32F2F",
    "#2E7D32",
    "#F5F5DC",
    "#795548",
    "#F48FB1",
    "#FFEB3B",
    "#7B1FA2",
)
