# Tasks: Agregar prenda al closet (001)

**Input**: Design documents from `/specs/001-add-garment-closet/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: No se generan tareas de tests — decisión explícita del autor (igual que 000). Verificación vía `assembleDebug` + `lintDebug` + guion manual de `quickstart.md`.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Mobile single-module**: `app/src/main/java/com/wearingdrape/app/` + `app/src/main/res/`
- Base 000 ya existe (`MainActivity.kt`, `ui/welcome/`, Gradle Kotlin DSL + `gradle/libs.versions.toml`)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Dependencias nuevas pineadas y build en verde antes de picar feature

- [X] T001 Add Room + navigation-compose + coil-compose + KSP versions to `gradle/libs.versions.toml` (versiones compatibles con Kotlin 2.2.10 / AGP 9.5.0 / Compose BOM 2026.09, anotadas con fecha)
- [X] T002 Configure `app/build.gradle.kts` (aplica plugin KSP, añade deps room-runtime + room-ktx, navigation-compose, coil-compose, lifecycle-viewmodel-compose; mantiene compose buildFeatures existente)
- [X] T003 Verify `gradlew.bat assembleDebug` succeeds with new dependencies from repo root

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Capa Data completa y catálogo; BLOQUEA todas las user stories (ninguna UI puede guardar sin esto)

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T004 [P] Create `GarmentGeneral` enum + catalog maps in `app/src/main/java/com/wearingdrape/app/data/Catalog.kt` (generales `SUPERIOR | INFERIOR | CALZADO | ACCESORIO`; seed verbatim — superior: camiseta, camisa, chaqueta, sudadera, jersey; inferior: jean, pantalón, short, falda; calzado: tenis, botas, zapatos, sandalias; accesorio: gorro, gorra, cinturón, bufanda; fits superior: oversize, boxy, regular, slim; fits inferior: loose fit, baggy fit, slim, recto, skinny; calzado/accesorio sin sugerencias)
- [X] T005 [P] Create `GarmentEntity` in `app/src/main/java/com/wearingdrape/app/data/GarmentEntity.kt` (campos verbatim data-model: `id` Long PK auto, `name` 1–60 obligatorio, `generalKey` String enum obligatorio, `specific` String snapshot obligatorio, `color` 1–40 obligatorio, `fit` String? opcional, `notes` 0–500 opcional, `photoPath` String? opcional 1 foto, `ownerId` String default `"local"`, `createdAt` Long epoch-ms)
- [X] T006 Create `GarmentDao` in `app/src/main/java/com/wearingdrape/app/data/GarmentDao.kt` (depends on T005: `insert` suspend + `observeAll` Flow ordenado por `createdAt` desc)
- [X] T007 Create `AppDatabase` in `app/src/main/java/com/wearingdrape/app/data/AppDatabase.kt` (depends on T005/T006: RoomDatabase con `entities=[GarmentEntity]`, version 1; documenta política `exportSchema`)
- [X] T008 [P] Create `FilePhotoDataSource` in `app/src/main/java/com/wearingdrape/app/data/FilePhotoDataSource.kt` (copia a `filesDir/closet/` como `garment_<id>_<timestamp>.<ext>`; valida JPEG/PNG/WebP y `≤5 MB`; borra el fichero copiado si el insert posterior falla)
- [X] T009 Create `StaticCatalogDataSource` in `app/src/main/java/com/wearingdrape/app/data/StaticCatalogDataSource.kt` (depends on T004: expone `specificsByGeneral` y `fitsByGeneral` desde `Catalog.kt`)
- [X] T010 Create `GarmentRepository` in `app/src/main/java/com/wearingdrape/app/data/GarmentRepository.kt` (depends on T006–T009: único punto de entrada a Data; expone `Flow<List<Garment>>`; `save()` valida obligatorios y hace guardado atómico copia-foto→insert sin fila parcial)
- [X] T011 Create `Application` holder + register in `app/src/main/AndroidManifest.xml` (depends on T007/T010: singleton de `AppDatabase`/repositorio para factory manual de ViewModels, sin Hilt/Koin)

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Guardar prenda con sus datos básicos (Priority: P1) ⭐ MVP

**Goal**: El usuario completa nombre + tipo general + tipo específico + color + foto, guarda, y la prenda aparece en su closet; faltantes e foto inválida se rechazan con mensaje sin perder el resto.

**Independent Test**: quickstart §2 — guardar prenda completa visible en lista (<2 min); 4 intentos sin cada obligatorio muestran qué campo falta; archivo no-imagen rechazado con formulario intacto.

### Implementation for User Story 1

- [X] T012 [P] [US1] Create `ClosetListViewModel` in `app/src/main/java/com/wearingdrape/app/ui/closet/ClosetListViewModel.kt` (UDF con `StateFlow`: estados `Empty` / `Content(items)` desde repositorio; `Content` orden `createdAt` desc)
- [X] T013 [P] [US1] Create `ClosetListScreen` in `app/src/main/java/com/wearingdrape/app/ui/closet/ClosetListScreen.kt` (depends on T012 contrato: `LazyColumn` de tarjetas con miniatura Coil o marcador "imagen no disponible", `name` + `specific` + `color`; estado `Empty` con texto "aún no hay prendas" + CTA "Agregar prenda"; tarjetas reflejan snapshot guardado)
- [X] T014 [US1] Create `AddGarmentViewModel` in `app/src/main/java/com/wearingdrape/app/ui/addgarment/AddGarmentViewModel.kt` (depends on T010: `AddGarmentUiState` con `name` 1–60, `generalKey`, `specific`, `color` 1–40, `photoUri?`, `fieldErrors` por campo, `isSaving` que bloquea doble submit, `saveResult OK|ERROR`)
- [X] T015 [US1] Create `AddGarmentScreen` form in `app/src/main/java/com/wearingdrape/app/ui/addgarment/AddGarmentScreen.kt` (depends on T014: campos + `PickVisualMedia` con fallback `GetContent` en API 29; foto inválida → error claro sin perder datos; cancelar → confirma "los cambios se perderán" y descarta sin fila)
- [X] T016 [US1] Wire `NavHost` in `app/src/main/java/com/wearingdrape/app/MainActivity.kt` (depends on T013/T015: rutas `closet` start + `addGarment`; `OK` → `popBackStack` + snackbar "Prenda guardada")
- [ ] T017 [US1] Run quickstart §2 manual validation in emulator API 29+ (registro <2 min, 4 rechazos por campo faltante, rechazo no-imagen; `assembleDebug` + `lintDebug` en verde)

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - Tipos específicos y fits filtrados por tipo general (Priority: P1)

**Goal**: Todo parte del tipo general; selectores de específico/fit solo ofrecen opciones de ese grupo y cambiar el general invalida lo incompatible.

**Independent Test**: quickstart §3 — superior/calzado/inferior muestran solo sus opciones (cero intrusas en los 4 grupos); cambio de general limpia o pide corregir específico/fit; 100% prendas guardadas coherentes.

### Implementation for User Story 2

- [X] T018 [US2] Implement coherence validation in `app/src/main/java/com/wearingdrape/app/data/GarmentRepository.kt` (`specific` MUST ∈ `specificsByGeneral[generalKey]` y `fit` informado MUST ∈ `fitsByGeneral[generalKey]`; rechaza intrusas tipo "superior + tenis"; grupo sin opciones → guardado con mensaje "aún no hay opciones para este grupo" en selector, sin fallo)
- [X] T019 [P] [US2] Filtered specific/fit selectors in `app/src/main/java/com/wearingdrape/app/ui/addgarment/AddGarmentScreen.kt` (sin `generalKey` → pide elegir general primero; con general → 100% opciones del grupo; nunca sugerir fits de otro grupo como opción principal)
- [X] T020 [US2] Implement `OnGeneralChange` reset in `app/src/main/java/com/wearingdrape/app/ui/addgarment/AddGarmentViewModel.kt` (depends on T018: si `specific`/`fit` previos no pertenecen al nuevo grupo → se limpian y se informa al usuario, FR-006)
- [ ] T021 [US2] Run quickstart §3 manual validation in emulator API 29+ (matriz 4 grupos sin intrusas, invalidación al cambiar general, coherencia de guardadas)

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently

---

## Phase 5: User Story 3 - Fit opcional y observaciones (Priority: P2)

**Goal**: Fit y observaciones opcionales se guardan y se vuelven a ver exactamente como se escribieron; omitirlos no bloquea el guardado.

**Independent Test**: quickstart §4 — guardar sin fit/observaciones OK; guardar con ambos y reabrir muestra el texto exacto.

### Implementation for User Story 3

- [X] T022 [P] [US3] Add `fit` (String? opcional) + `notes` (0–500 opcional) to state/save in `app/src/main/java/com/wearingdrape/app/ui/addgarment/AddGarmentViewModel.kt` (guardado OK con ambos vacíos)
- [X] T023 [US3] Add fit + notes inputs in `app/src/main/java/com/wearingdrape/app/ui/addgarment/AddGarmentScreen.kt` (depends on T022: sugerencias de fit del grupo; `CALZADO`/`ACCESORIO` sin sugerencias — campo libre u oculto)
- [X] T024 [US3] Display fit + notes in `app/src/main/java/com/wearingdrape/app/ui/closet/ClosetListScreen.kt` (texto exacto guardado; foto ausente sigue con marcador)
- [ ] T025 [US3] Run quickstart §4 manual validation in emulator API 29+ (ambos casos visibles)

**Checkpoint**: All user stories should now be independently functional

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Higiene final y validación cruzada de SC/edge cases

- [X] T026 Run `gradlew.bat assembleDebug` + `gradlew.bat lintDebug` green and fix all `Error` issues from repo root
- [ ] T027 [P] Run full `specs/001-add-garment-closet/quickstart.md` validation pass (§1–§5) in emulator API 29+ including edge cases (nombres duplicados permitidos como registros independientes; límites nombre >60 / observaciones >500 informados; color desde picker sin texto libre; cancelar no guarda; foto borrada → marcador)
- [X] T028 [P] Update `README.md` with 001 run/verify notes and pinned dependency versions from `gradle/libs.versions.toml`
- [X] T029 Audit user-visible strings into `app/src/main/res/values/strings.xml` in Spanish (no hardcoded UI text in screens)
- [X] T030 Color picker (FR-011): paleta de hexadecimales en `data/Catalog.kt` (`colorOptions`), validación de pertenencia en `GarmentRepository`, picker de muestras en `AddGarmentScreen` en vez de texto libre y muestra de color en la tarjeta de `ClosetListScreen`; spec/contrato/data-model/quickstart actualizados
- [X] T031 Tabs por tipo general (US1-A4): `Todas` + 4 grupos en `TabRow` de `ClosetListScreen`, filtrado en `ClosetListViewModel.selectedTab`; spec/contrato/quickstart actualizados

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P1 → P2)
- **Polish (Final Phase)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P1)**: Can start after Foundational (Phase 2) - Refines US1 selectors + repository validation; independently testable per quickstart §3
- **User Story 3 (P2)**: Can start after Foundational (Phase 2) - Adds optional fields onto US1 form; independently testable per quickstart §4

### Within Each User Story

- ViewModels/state before screens
- Screens before navigation wiring (US1)
- Core implementation before manual validation task
- Story complete before moving to next priority

### Parallel Opportunities

- T004 + T005 + T008 can run in parallel (different files, no mutual dependencies)
- T012 + T013 can run in parallel (ViewModel contract + screen per `closet-list-contract.md`)
- T019 can run in parallel with T018/T020 (screen filtering vs repository validation, merged on contract)
- T022 can run in parallel with T023 prep (state vs inputs on different concerns of same files — coordinate to avoid same-file conflicts; prefer sequential)
- T027 + T028 can run in parallel (validation vs docs)

---

## Parallel Example: User Story 1

```text
# Launch ViewModel + screen together (contract-first, interfaces per contracts/):
Task: "Create ClosetListViewModel in app/src/main/java/com/wearingdrape/app/ui/closet/ClosetListViewModel.kt"
Task: "Create ClosetListScreen in app/src/main/java/com/wearingdrape/app/ui/closet/ClosetListScreen.kt"
```

## Parallel Example: Foundational

```text
Task: "Create GarmentGeneral enum + catalog maps in .../data/Catalog.kt"
Task: "Create GarmentEntity in .../data/GarmentEntity.kt"
Task: "Create FilePhotoDataSource in .../data/FilePhotoDataSource.kt"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: quickstart §2 independently (guardar + ver + rechazos)
5. Demo if ready (nótese: sin US2 el filtro por grupo aún no aplica — ver nota MVP ampliado)

### Incremental Delivery (recomendado: P1 completo)

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → MVP básico
3. Add User Story 2 → Test independently → **MVP real (P1 completo: SC-002/SC-004 exigen el filtrado)**
4. Add User Story 3 → Test independently → Feature completa
5. Polish → quickstart full pass → Done

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1
   - Developer B: User Story 2 (contrato de validación en repositorio)
   - Developer C: User Story 3 (campos opcionales)
3. Stories complete and integrate independently (US2/US3 tocan los mismos archivos de US1 — integrar en orden US1 → US2 → US3 si hay un solo implementador)

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- T017/T021/T025/T027 son validaciones manuales (no tests automatizados por decisión del autor)
- Restricciones de data-model.md citadas verbatim en cada tarea (longitudes, enum, ≤5 MB) para no dejarlas a discreción de implementación
- Constitución: UI → Data vía repositorio; sin Domain/DI; `ownerId "local"` reserva migración Supabase
- Commit after each task or logical group; stop at any checkpoint to validate story independently
