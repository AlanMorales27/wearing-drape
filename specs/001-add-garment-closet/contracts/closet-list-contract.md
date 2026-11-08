# Contract: Lista mínima del closet (UI, solo lectura)

**Spec**: [spec.md](../spec.md) | **Data model**: [data-model.md](../data-model.md)

La lista existe solo para probar "la prenda queda registrada y puede volver a verse" (US1-A1). Buscar/editar/borrar quedan fuera (spec Assumptions).

## UI state (`ClosetListUiState`, filtrado por el tab activo)

| Estado | Contenido |
|---|---|
| `Empty` | Ilustración/texto "aún no hay prendas" + CTA "Agregar prenda" |
| `Content(items)` | Tarjetas con miniatura (o marcador si `photoPath` inaccesible), muestra de color + `name`, `specific`, `color` (hex); orden `createdAt` desc |
| `Saving` | Tras volver de `addGarment` con `OK`: snackbar "Prenda guardada" |

## Tabs (filtro por tipo general, US1-A4)

- Tabs fijos: `Todas` + un tab por `GarmentGeneral` (`SUPERIOR`, `INFERIOR`, `CALZADO`, `ACCESORIO`).
- `Todas` muestra todo el closet; cada tab de grupo muestra solo prendas con ese `generalKey`.
- El tab activo filtra `Empty`/`Content`: un grupo sin prendas muestra `Empty`.

## Reglas

- Cada tarjeta refleja el snapshot guardado (no el catálogo vigente): si el catálogo cambia después, la prenda muestra sus valores originales (FR-013).
- Foto inaccesible → marcador "imagen no disponible", resto de datos visibles (edge spec).
- Sin paginación en 001 (escala: cientos de prendas; `LazyColumn` basta).
- Sin estados de sincronización/red: todo es local y síncrono a efectos de UI.
