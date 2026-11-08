# Data Model: Agregar prenda al closet (001)

**Date**: 2026-11-02 | **Spec**: [spec.md](./spec.md) | **Research**: [research.md](./research.md)

Persistencia solo local (Room + ficheros internos). Usuario único implícito, sin Auth.

## Entity 1: Prenda (`garments`)

- **What**: Registro del closet. Cada fila es independiente; el nombre NO es único.
- **Attributes**:
  - `id`: Long auto-generado (PK).
  - `name`: String, obligatorio, 1–60 caracteres.
  - `generalKey`: String enum (`SUPERIOR | INFERIOR | CALZADO | ACCESORIO`), obligatorio.
  - `specific`: String snapshot (p. ej. "camiseta"), obligatorio; MUST pertenecer al catálogo de su `generalKey`.
   - `color`: String con el hexadecimal elegido en el picker (`Catalog.colorOptions`). Obligatorio.
  - `fit`: String snapshot opcional (nullable); si se informa, MUST pertenecer a los fits de su `generalKey`.
  - `notes`: String opcional (observaciones), 0–500 caracteres.
  - `photoPath`: String opcional (ruta relativa en `filesDir/closet/`); 1 foto por prenda en v1. Si la foto falta/inaccesible, la prenda sigue válida y muestra marcador (edge spec).
  - `ownerId`: String, constante `"local"` en 001 (columna reservada para el futuro `user_id` de Supabase; satisface FR-009 en local: un solo closet por dispositivo).
  - `createdAt`: Long epoch-ms.
- **Validation** (en `GarmentRepository`, nunca solo en UI):
  - Obligatorios presentes (FR-002); si falta alguno → error por campo, sin perder el resto del formulario (FR-008).
  - Coherencia `generalKey + specific + fit` contra catálogo (FR-004/FR-005/FR-006, SC-002/SC-004); combinación intrusa → rechazo.
  - Foto: solo imagen JPEG/PNG/WebP, ≤5 MB (FR-007/FR-008); fallo de copia → no se inserta fila (FR-012, guardado atómico: primero copia fichero, luego insert; si falla insert, se borra el fichero copiado).
  - Nombres duplicados permitidos (edge spec).
- **State transitions**: `draft(form)` → `validating` → `saved` | `rejected(con errores por campo)`. Cancelar → `discarded` (sin fila, se confirma pérdida de cambios). No hay edición/borrado en 001.

## Entity 2: Tipo general (`GarmentGeneral`, enum en código)

- **What**: Grupo raíz. Exactamente 4 valores (FR-003): `SUPERIOR`, `INFERIOR`, `CALZADO`, `ACCESORIO`.
- **Role**: Determina qué `specifics` y `fits` se ofrecen; cambiarlo invalida `specific`/`fit` previos si no pertenecen al nuevo grupo (FR-006).

## Entity 3: Tipo específico (catálogo estático)

- **What**: Subclasificación; cada valor pertenece a exactamente un general.
- **Seed inicial** (spec Assumptions):
  - SUPERIOR → camiseta, camisa, chaqueta, sudadera, jersey
  - INFERIOR → jean, pantalón, short, falda
  - CALZADO → tenis, botas, zapatos, sandalias
  - ACCESORIO → gorro, gorra, cinturón, bufanda
- **Regla**: el selector NUNCA mezcla grupos (FR-004); grupo sin opciones → estado vacío con mensaje, no fallo (edge spec).

## Entity 4: Fit (catálogo estático, opcional)

- **What**: Caída/ajuste sugerido por grupo; siempre opcional en la prenda.
- **Seed inicial**:
  - SUPERIOR → oversize, boxy, regular, slim
  - INFERIOR → loose fit, baggy fit, slim, recto, skinny
  - CALZADO, ACCESORIO → sin sugerencias (campo libre oculto o texto libre según grupo; nunca sugerir fits de otro grupo como opción principal, FR-005).

## Entity 5: Catálogo de opciones (`StaticCatalogDataSource` + `Catalog.kt`)

- **What**: Mapas `specificsByGeneral`, `fitsByGeneral` versionados en código.
- **FR-013**: agregar opciones = añadir entradas al mapa (cambio de código versionado, sin migración Room porque las prendas guardan snapshot); prendas existentes intactas.
- **Validation**: `GarmentRepository` valida contra la versión vigente del catálogo al guardar.

## Relaciones

```text
Prenda ──pertenece a──> 1 Tipo general (generalKey)
Prenda ──describe con──> 1 Tipo específico (snapshot del grupo)
Prenda ──opcional──────> 1 Fit (snapshot del grupo o libre en calzado/accesorio)
Prenda ──tiene─────────> 0..1 Foto (fichero interno referenciado por photoPath)
Prenda ──pertenece a──> ownerId "local" (1 closet por dispositivo en 001)
```

## Notas de migración futura (fuera de 001)

- `ownerId "local"` → `user_id` Supabase; `photoPath` → path/URL de Supabase Storage; catálogo estático → tablas + RLS si se necesita gestión remota. Ningún cambio de UI de formulario requerido por este diseño.
