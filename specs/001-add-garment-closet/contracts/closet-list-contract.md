# Contract: Lista mínima del closet (UI, solo lectura)

**Spec**: [spec.md](../spec.md) | **Data model**: [data-model.md](../data-model.md)

La lista existe solo para probar "la prenda queda registrada y puede volver a verse" (US1-A1). Buscar/editar/borrar quedan fuera (spec Assumptions).

## UI state (`ClosetListUiState`)

| Estado | Contenido |
|---|---|
| `Empty` | Ilustración/texto "aún no hay prendas" + CTA "Agregar prenda" |
| `Content(items)` | Tarjetas con miniatura (o marcador si `photoPath` inaccesible), `name`, `specific`, `color`; orden `createdAt` desc |
| `Saving` | Tras volver de `addGarment` con `OK`: snackbar "Prenda guardada" |

## Reglas

- Cada tarjeta refleja el snapshot guardado (no el catálogo vigente): si el catálogo cambia después, la prenda muestra sus valores originales (FR-013).
- Foto inaccesible → marcador "imagen no disponible", resto de datos visibles (edge spec).
- Sin paginación en 001 (escala: cientos de prendas; `LazyColumn` basta).
- Sin estados de sincronización/red: todo es local y síncrono a efectos de UI.
