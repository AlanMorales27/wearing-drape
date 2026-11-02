# Research: Agregar prenda al closet (001)

**Date**: 2026-11-02 | **Spec**: [spec.md](./spec.md) | **Base**: spec 000 ya implementada (single-module `:app`, Compose Material3, `com.wearingdrape.app`, minSdk 29, sin tests)

Decisiones del autor (2026-11-02): persistencia **solo local**, **usuario local único** (sin Auth), alcance **formulario + lista mínima**, **sin tests automatizados** (verificación manual como en 000).

Todas las decisiones resuelven el Technical Context sin NEEDS CLARIFICATION abiertos.

## Decision 1: Persistencia local con Room, sin Supabase en 001

- **Decision**: Room (SQLite) como única fuente de datos estructurados. Tabla `garments` con snapshot de strings (ver data-model). Sin Supabase/Postgres/Storage/RLS en esta 001.
- **Rationale**: El autor pidió solo-local; evita red, Auth y RLS en el MVP; Room es el estándar oficial Android para local, funciona offline siempre y migra bien a Supabase después (el `ownerId` queda como columna reservada).
- **Alternatives considered**:
  - DataStore/Proto + ficheros JSON: descartado — sin queries, sin orden/filtrado futuro, más código manual.
  - Supabase desde ya (Postgres + Storage + Auth): descartado — contradice la decisión del autor; añade red/Auth/RLS y secretos al MVP.
  - Híbrido Room + Supabase: descartado por YAGNI — sincronización sin requisitos.

## Decision 2: Fotos en almacenamiento interno de la app, 1 por prenda

- **Decision**: Picker vía `ActivityResultContracts.PickVisualMedia` (photo picker del sistema, minSdk 29 compatible con fallback a `GetContent`); copia a `filesDir/closet/` con nombre `garment_<id>_<timestamp>.<ext>`; en Room solo se guarda `photoPath` relativo. Límite 5 MB, formatos JPEG/PNG/WebP. Carga en UI con `coil-compose` (`AsyncImage`).
- **Rationale**: Photo picker evita pedir permisos de lectura amplios; internal storage no necesita permisos y se borra con la app; 1 foto/prenda es lo asumido en el spec; Coil es el cargador estándar para Compose y maneja sampling sin OOM.
- **Alternatives considered**:
  - Guardar bytes/BLOB en Room: descartado — infla la BD, rompe cursores y rendimiento.
  - MediaStore compartido / SAF: descartado — permisos extra y fotos huérfanas si el usuario borra desde galería.
  - Sin librería (BitmapFactory manual): descartado — más código de sampling/caché propenso a OOM; Coil añade 1 dependencia pequeña y justificada.

## Decision 3: Navegación con navigation-compose, 2 destinos

- **Decision**: `androidx.navigation:navigation-compose` con 2 rutas: `closet` (lista mínima) y `addGarment` (formulario). `closet` es start destination; al guardar se vuelve con `popBackStack` + snackbar de confirmación.
- **Rationale**: Es la navegación oficial Compose, coste mínimo para 2 pantallas, evita back-stack manual con bugs; la lista mínima resuelve la contradicción del spec ("la muestra en su closet" vs "listar fuera de alcance") con una lista solo-lectura sin buscar/editar/borrar.
- **Alternatives considered**:
  - Estado manual (`var showForm` en Activity): descartado — rompe botón atrás/gestos y deep-links futuros.
  - Navegación manual con back-stack propio: descartado — reinventar NavController viola YAGNI.

## Decision 4: Capas UI + Data, sin Domain y sin DI framework

- **Decision**: `ClosetListViewModel` + `AddGarmentViewModel` (UDF con `StateFlow`, `collectAsStateWithLifecycle`) → `GarmentRepository` (única entrada a Data, contiene la validación de coherencia general→específico/fit) → `GarmentDao` (Room) + `FilePhotoDataSource` + `StaticCatalogDataSource`. ViewModels creados con `viewModel { }` + factory manual / `Application` holder; sin Hilt/Koin en 001.
- **Rationale**: Constitución: UI no toca data sources, repositorios exponen datos y contienen lógica de negocio. El filtrado/validación vive en el repositorio (+ catálogo estático), así que no hace falta capa Domain (casos de uso de 1 línea). Sin DI framework por YAGNI con 2 ViewModels.
- **Alternatives considered**:
  - Casos de uso (`ValidateGarmentUseCase`, etc.): descartado — lógica usada por un solo ViewModel; se introduce si se reutiliza en editar/duplicar (specs futuras).
  - Hilt/Koin desde ya: descartado — boilerplate (Application, módulos, kapt/ksp extra) para 2 grafos triviales.

## Decision 5: Catálogo estático versionado en código, prendas con snapshot

- **Decision**: `Catalog.kt` en Data con `GarmentGeneral` (enum: SUPERIOR, INFERIOR, CALZADO, ACCESORIO) + mapas `specificsByGeneral` y `fitsByGeneral` con los valores iniciales del spec. La prenda guarda **strings snapshot** (`generalKey`, `specific`, `fit?`), no FKs al catálogo, para que agregar opciones (FR-013) nunca modifique prendas guardadas. Validación de coherencia en `GarmentRepository.save()` (rechaza combinaciones intrusas, SC-002/SC-004).
- **Rationale**: FR-013 exige catálogo gestionable sin tocar prendas; snapshot lo garantiza con el modelo más simple. Tablas Room para el catálogo serían gestión dinámica sin UI de gestión en 001 (YAGNI). El enum + mapas cubre exactamente los 4 grupos del spec.
- **Alternatives considered**:
  - Tablas `generals`/`specifics`/`fits` + FKs: descartado — más migración/seed/DAO sin editor de catálogo en 001; FK estricta rompería FR-013 al renombrar opciones.
  - Strings libres sin validación: descartado — permitiría "superior + tenis" y violaría SC-002.

## Decision 6: Sin tests automatizados en 001

- **Decision**: Sin `src/test`/`src/androidTest` ni dependencias de test, igual que 000 por decisión del autor. Verificación: `assembleDebug` + `lintDebug` en verde + guion manual de [quickstart.md](./quickstart.md) (formulario, filtros por grupo, lista).
- **Rationale**: Decisión explícita del autor; la constitución exige build+lint en verde, no tests obligatorios.
- **Alternatives considered**: Tests unitarios de filtrado/validación: descartados por ahora; se retoman cuando el autor los pida (el repositorio aísla la lógica para testearla después sin refactorizar UI).

## Referencias a verificar al implementar

- Versión `room` + `navigation-compose` + `coil-compose` compatibles con Kotlin 2.2.10 / Compose BOM 2026.09 (anotar en `libs.versions.toml`).
- KSP para Room (`com.google.devtools.ksp`) alineado al AGP 9.5.0.
- `PickVisualMedia` fallback en API 29 (el picker moderno hace fallback a `GetContent`; probar en el emulador API 29 del quickstart).
