# Contract: Flujo agregar prenda (UI)

**Spec**: [spec.md](../spec.md) | **Data model**: [data-model.md](../data-model.md)

Contrato del formulario `addGarment`: eventos que suben, estado que baja (UDF), validaciones y resultado.

## Rutas

- `closet` (start): lista mínima del closet.
- `addGarment`: formulario de nueva prenda. Volver con atrás sin guardar → confirma "los cambios se perderán" y descarta (edge spec).

## UI state (`AddGarmentUiState`)

| Campo | Tipo | Regla |
|---|---|---|
| `name` | String | 1–60; obligatorio |
| `generalKey` | `SUPERIOR\|INFERIOR\|CALZADO\|ACCESORIO\|null` | obligatorio; punto de partida del filtrado |
| `specific` | String? | obligatorio; MUST ∈ `specificsByGeneral[generalKey]` |
| `color` | String | 1–40; obligatorio |
| `fit` | String? | opcional; si presente y el grupo tiene sugerencias, MUST ∈ `fitsByGeneral[generalKey]` |
| `notes` | String | opcional, 0–500 |
| `photoUri` | Uri? | opcional pero invitada; 1 foto; JPEG/PNG/WebP ≤5 MB |
| `fieldErrors` | Map<campo, mensaje> | un mensaje por campo inválido |
| `isSaving` | Boolean | bloquea doble submit |
| `saveResult` | `null\|OK\|ERROR` | OK → `popBackStack` + snackbar en `closet` |

## Eventos (suben)

- `OnGeneralChange(general)`: recalcula listas; si `specific`/`fit` previos no pertenecen al nuevo grupo → se limpian y se informa (FR-006).
- `OnPhotoPick(uri)`: valida MIME + tamaño; archivo inválido → error claro, resto del formulario intacto (FR-007/FR-008, US1-A3).
- `OnSave`: valida todo; si falla → `fieldErrors`, sin fila parcial (FR-012); si OK → copia foto + insert atómico → `OK`.

## Reglas de filtrado (SC-004)

- Abrir selector de `specific` sin `generalKey` → pide elegir general primero.
- Selector de `specific`/`fit` con general elegido → 100% opciones del grupo, cero intrusas (US2-A1/A2/A4).
- Grupo sin opciones → estado vacío "aún no hay opciones para este grupo" (edge spec).
