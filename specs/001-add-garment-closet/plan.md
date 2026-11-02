# Implementation Plan: Agregar prenda al closet (001)

**Branch**: `feature-001/add-garment-closet` | **Date**: 2026-11-02 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification de `/specs/001-add-garment-closet/spec.md` + base 000 ya implementada (single-module `:app`, Compose Material3, `com.wearingdrape.app`, minSdk 29, sin tests) + decisiones del autor 2026-11-02 (solo local, usuario único, formulario + lista mínima, sin tests).

## Summary

Formulario "agregar prenda" (foto + nombre + tipo general + tipo específico filtrado + color + fit/observaciones opcionales) con guardado local atómico y lista mínima del closet donde se ve lo guardado. Persistencia con Room + fotos en almacenamiento interno; catálogo estático versionado en código con validación de coherencia en el repositorio; navegación con navigation-compose (2 destinos); capas UI + Data sin Domain ni DI framework. Sin Supabase/Auth/red y sin tests automatizados (decisiones del autor).

## Technical Context

**Language/Version**: Kotlin 2.2.10 + Java toolchain 17 (heredado de 000, pineado en `gradle/libs.versions.toml`)

**Primary Dependencies**: Las de 000 (AGP 9.5.0, Compose BOM 2026.09, material3, activity-compose, lifecycle-runtime-compose) + `androidx.room:room-*` (KSP), `navigation-compose`, `coil-compose`, `lifecycle-viewmodel-compose`/`runtime-compose`. Versiones exactas compatibles con Kotlin 2.2.10 a verificar al implementar y pinear en el catalog.

**Storage**: Room (SQLite local, tabla `garments`) + ficheros internos `filesDir/closet/` (1 foto por prenda, JPEG/PNG/WebP ≤5 MB). Sin backend.

**Testing**: Sin tests automatizados por decisión explícita del autor (igual que 000). Verificación: `assembleDebug` + `lintDebug` en verde + guion manual de [quickstart.md](./quickstart.md).

**Target Platform**: Android, minSdk 29, target/compile 37 (heredado de 000); verificación en emulador API 29+.

**Project Type**: mobile-app, single-module (`:app`), variante Android de "Mobile + API" sin API remota.

**Performance Goals**: Guardado prenda + foto <2 s en dispositivo medio; lista con cientos de prendas sin paginación (`LazyColumn` + Coil sampling); cold start <2 s; registro completo <2 min (SC-001).

**Constraints**: YAGNI estricto (sin editar/borrar/buscar, sin gestión remota de catálogo, sin Auth); secretos: no hay (cero credenciales en 001); foto picker sin permisos amplios (`PickVisualMedia` con fallback `GetContent` en API 29); límites: nombre 1–60, color 1–40, observaciones 0–500, foto ≤5 MB.

**Scale/Scope**: 1 usuario local (`ownerId` constante `"local"`), cientos de prendas, 2 destinos de navegación, ~10-15 archivos Kotlin nuevos bajo `com.wearingdrape.app`.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **I. Simplicidad/YAGNI**: PASS — Room + ficheros en vez de Supabase (decisión autor); sin Domain (lógica en repo, 1 consumidor); sin Hilt/Koin (2 ViewModels, factory manual); catálogo estático en vez de tablas gestionadas (sin editor en 001); lista mínima solo-lectura. Nuevas dependencias (Room/Nav/Coil) justificadas en [research.md](./research.md).
- [x] **II. Arquitectura por capas**: PASS — UI (`closet/`, `addgarment/` Compose + ViewModels, UDF con StateFlow) → Data (`GarmentRepository` único punto de entrada + `GarmentDao`, `FilePhotoDataSource`, `StaticCatalogDataSource`). UI no toca data sources; la validación de negocio (coherencia general→específico/fit) vive en el repositorio. Sin Domain por no haber lógica reutilizada entre ViewModels.
- [x] **Secretos fuera del código**: PASS — 001 no maneja credenciales; nada que versionar fuera del código.
- [x] **Datos/RLS**: PASS con adaptación documentada — sin Supabase en 001 (decisión autor); el aislamiento por usuario se cumple en local con `ownerId = "local"` y columna reservada para migrar a `user_id` + RLS en spec posterior. FR-009 reinterpretado: un solo closet por dispositivo, sin login.
- [x] **CI/lint estricto**: PASS — `assembleDebug` + `lintDebug` en verde + quickstart manual como criterio de aceptación; sin tests por decisión del autor (la constitución exige build+lint, no tests).
- [x] **Complejidad justificada**: PASS — cada abstracción nueva (Room, Nav, Coil, Repository, 2 ViewModels) tiene justificación escrita en research.md; sin casos de uso, sin DI, sin tablas de catálogo.

Re-check post-Phase 1 (2026-11-02): sin cambios — research/data-model/contracts/quickstart no añaden capas ni dependencias nuevas. PASS.

## Project Structure

### Documentation (this feature)

```text
specs/001-add-garment-closet/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
│   ├── add-garment-flow-contract.md
│   └── closet-list-contract.md
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
/
├── settings.gradle.kts / build.gradle.kts / gradle/libs.versions.toml  # + Room, Nav, Coil, KSP
└── app/
    ├── build.gradle.kts         # ksp + room + navigation-compose + coil-compose
    └── src/main/java/com/wearingdrape/app/
        ├── MainActivity.kt                  # NavHost: closet (start) + addGarment
        ├── ui/
        │   ├── welcome/                     # existente 000 (se conserva o redirige a closet)
        │   ├── closet/                      # ClosetListScreen.kt + ClosetListViewModel.kt
        │   └── addgarment/                  # AddGarmentScreen.kt + AddGarmentViewModel.kt
        └── data/
            ├── AppDatabase.kt               # RoomDatabase + GarmentEntity
            ├── GarmentDao.kt                # insert + observe list
            ├── GarmentRepository.kt         # única entrada: valida + guarda atómico + expone flows
            ├── Catalog.kt                   # GarmentGeneral + specificsByGeneral + fitsByGeneral
            ├── StaticCatalogDataSource.kt   # expone catálogo
            └── FilePhotoDataSource.kt       # copia/valida/borra fotos en filesDir/closet/
```

**Structure Decision**: Se extiende el single-module `:app` de 000 sin reestructurar: `ui/` crece con 2 features (patrón ya sembrado en `ui/welcome/`) y se crea `data/` según la capa Data obligatoria de la constitución. Sin módulo nuevo, sin `domain/` (YAGNI), sin `domain` hasta que una lógica se reutilice en ≥2 ViewModels.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| navigation-compose para 2 pantallas | Back-stack/gesto atrás oficial sin bugs; estándar para crecer a editar/detalle | Estado manual `showForm` rompe botón atrás y deep-links |
| Room en vez de ficheros JSON | Queries/orden/observación con Flow; base migrable a Supabase | DataStore/JSON sin queries ni observación reactiva |
| coil-compose (1 lib imagen) | Sampling/caché sin OOM en miniaturas | BitmapFactory manual = código propenso a OOM |
| `ownerId` constante sin Auth | Reserva la columna para `user_id` + RLS futuros sin migración | Omitirla obligaría a migrar la tabla al llegar Supabase |
