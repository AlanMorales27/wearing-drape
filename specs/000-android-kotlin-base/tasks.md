# Tasks: Base Proyecto Android Kotlin (000)

**Input**: Design documents from `/specs/000-android-kotlin-base/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/, quickstart.md

**Tests**: Ninguno — explícitamente excluido por el autor en FR-005d (2026-11-02). Verificación manual vía Run + `assembleDebug` + `lintDebug`. No generar tareas de test.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Gradle raíz, versionado e higiene base — sin código de app todavía.

- [ ] T001 Create settings with `include(":app")` and central repos in `settings.gradle.kts`
- [ ] T002 [P] Create root plugins/clean task in `build.gradle.kts`
- [ ] T003 [P] Create version catalog pinning AGP, Kotlin, Compose BOM in `gradle/libs.versions.toml`
- [ ] T004 [P] Create Gradle flags (AndroidX, JVM `-Xmx3g`) in `gradle.properties`
- [ ] T005 Setup Gradle wrapper executables and config in `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.properties`
- [ ] T006 [P] Create ignore rules for build outputs and secrets in `.gitignore`
- [ ] T007 [P] Create local SDK template with commented `sdk.dir` in `local.properties.example`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Módulo `:app` mínimo que permite sync en Android Studio. MUST completo antes de cualquier historia.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T008 Create app build config with namespace `com.wearingdrape.app`, minSdk 29, Compose in `app/build.gradle.kts`
- [ ] T009 Create launcher manifest with exported MainActivity in `app/src/main/AndroidManifest.xml`
- [ ] T010 [P] Create minimal ComponentActivity shell in `app/src/main/java/com/wearingdrape/app/MainActivity.kt`
- [ ] T011 [P] Create app name strings in `app/src/main/res/values/strings.xml`
- [ ] T012 [P] Create Material3 theme in `app/src/main/res/values/themes.xml`

**Checkpoint**: Foundation ready — Studio lista `:app` en Project view y `Sync` termina sin errores. User stories pueden empezar.

---

## Phase 3: User Story 1 - Obtener esqueleto que abre sin errores (Priority: P1) 🎯 MVP (parte 1)

**Goal**: El repo se abre en Android Studio y sincroniza sin errores, con solo archivos básicos (FR-001, FR-006).

**Independent Test**: Abrir carpeta raíz en Studio → Sync OK y módulo `app` listado; árbol sin carpetas especulativas (ver `specs/000-android-kotlin-base/quickstart.md` paso 1).

- [ ] T013 [US1] Validate Studio sync and `:app` listing per `specs/000-android-kotlin-base/quickstart.md`
- [ ] T014 [P] [US1] Audit versioned tree for YAGNI (no `src/test`, no `src/androidTest`, no módulos extra) per `specs/000-android-kotlin-base/spec.md` FR-009

**Checkpoint**: US1 funcional y testeable sola — Sync verde aunque la pantalla aún sea placeholder.

---

## Phase 4: User Story 2 - Compilar y ejecutar app mínima (Priority: P1) 🎯 MVP (parte 2)

**Goal**: `assembleDebug` + Run muestran bienvenida Compose sin crash (FR-002, FR-003, FR-004, FR-005).

**Independent Test**: `gradlew.bat assembleDebug` + `lintDebug` en verde y Run en emulador API 29+ muestra bienvenida <2s, sin crash ni pantalla negra (ver `specs/000-android-kotlin-base/contracts/app-launch-contract.md` y `contracts/build-tasks-contract.md`).

- [ ] T015 [P] [US2] Create welcome screen composable in `app/src/main/java/com/wearingdrape/app/ui/welcome/WelcomeScreen.kt`
- [ ] T016 [US2] Wire MainActivity content to WelcomeScreen with Material3 theme in `app/src/main/java/com/wearingdrape/app/MainActivity.kt`
- [ ] T017 [P] [US2] Add launcher icon assets in `app/src/main/res/mipmap-*/`
- [ ] T018 [US2] Validate `assembleDebug` + `lintDebug` green per `specs/000-android-kotlin-base/contracts/build-tasks-contract.md`
- [ ] T019 [US2] Validate manual Run on API 29+ emulator per `specs/000-android-kotlin-base/contracts/app-launch-contract.md` and `specs/000-android-kotlin-base/quickstart.md`

**Checkpoint**: US1 + US2 = MVP completo — app instalable y visible. Si esto falla, la base es inválida (SC-002).

---

## Phase 5: User Story 3 - Base lista para crecer (Priority: P3)

**Goal**: Cero secretos versionados y punto de extensión `ui/` documentado para 001 (FR-007, FR-010).

**Independent Test**: `git ls-files | findstr local.properties` vacío y búsqueda de `supabase|service_role|api_key` en `app/src` con 0 resultados; `ui/welcome/` evidente para 001 (ver `specs/000-android-kotlin-base/quickstart.md` paso 4).

- [ ] T020 [US3] Verify secrets hygiene (`local.properties` ignored, no keys in repo) in `.gitignore` and `local.properties.example`
- [ ] T021 [P] [US3] Document future secrets location (SUPABASE_URL/ANON_KEY, nunca service_role) in `README.md`
- [ ] T022 [P] [US3] Document `ui/` extension point for spec 001 without creating ViewModels/repos in `README.md`

**Checkpoint**: Las 3 historias funcionan; la base no requerirá reestructurar para 001.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: README final, auditoría SC-003/SC-004 y validación quickstart.

- [ ] T023 Complete README with prerequisites, Open/Sync/Run and Windows troubleshooting in `README.md`
- [ ] T024 [P] Audit final tree for SC-004 (100% archivos necesarios) per `specs/000-android-kotlin-base/plan.md`
- [ ] T025 [P] Audit repo for SC-003 (0 secretos versionados) via `git ls-files` and text search in `app/src`
- [ ] T026 Run end-to-end `specs/000-android-kotlin-base/quickstart.md` validation and record SC-001 time (<10 min)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion — BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - US1 (P1) → US2 (P1) en orden (US2 reusa MainActivity/manifest de US1)
  - US3 (P3) puede ir tras US2 o en paralelo si hay capacidad, pero valida el árbol final
- **Polish (Phase 6)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational — no dependencies on other stories
- **User Story 2 (P1)**: Can start after Foundational + US1 shell — integra MainActivity de US1, pero su criterio (Run visible) es independiente
- **User Story 3 (P3)**: Can start after Foundational — integra con US1/US2 solo como auditoría/doc, independientemente testeable

### Within Each User Story

- Sin tests automatizados (FR-005d) — no aplica TDD
- Estructura antes que UI (Manifest → MainActivity → WelcomeScreen → res)
- Implementación antes que validación (`assembleDebug`/`lintDebug`/Run al final)
- Story completa antes de pasar a la siguiente prioridad

### Parallel Opportunities

- T002, T003, T004, T006, T007 (Phase 1) en paralelo — archivos distintos
- T010, T011, T012 (Phase 2) en paralelo — `MainActivity.kt` vs `strings.xml` vs `themes.xml`
- T014 (auditoría) puede ir en paralelo a validación T013 si hay 2 personas
- T015 vs T017 (Phase 4) en paralelo — `WelcomeScreen.kt` vs `mipmap-*/`
- T021 vs T022 (Phase 5) en paralelo — secciones distintas de `README.md` (coordinar para no chocar en el mismo archivo; mejor secuencial si un solo editor)
- T024 vs T025 (Polish) en paralelo — auditoría archivos vs auditoría secretos

---

## Parallel Example: User Story 2

```bat
REM Misma tanda (archivos distintos, sin dependencia mutua):
REM Task T015: "Create welcome screen composable in app/src/main/java/com/wearingdrape/app/ui/welcome/WelcomeScreen.kt"
REM Task T017: "Add launcher icon assets in app/src/main/res/mipmap-*/"

REM Secuencial después (depende de las anteriores):
REM Task T016: "Wire MainActivity content to WelcomeScreen in app/src/main/java/com/wearingdrape/app/MainActivity.kt"
REM Task T018: "Validate assembleDebug + lintDebug per specs/000-android-kotlin-base/contracts/build-tasks-contract.md"
```

---

## Implementation Strategy

### MVP First (US1 + US2)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL — blocks all stories)
3. Complete Phase 3: US1 → **STOP and VALIDATE**: Sync verde
4. Complete Phase 4: US2 → **STOP and VALIDATE**: `assembleDebug` + Run visible (MVP listo, SC-001/SC-002)
5. Deploy/demo si está verde

### Incremental Delivery

1. Setup + Foundational → Foundation ready (sync OK)
2. + US1 → Sync validado
3. + US2 → MVP (app corre, SC-001/SC-002)
4. + US3 → Higiene + extensión documentada (SC-003)
5. + Polish → SC-004 + quickstart completo

### Notas de mapa a diseño

- Entidades `data-model.md`: Proyecto base → T001–T007; Módulo app → T008–T012; Pantalla bienvenida → T015–T017; Config local → T007/T020; Verificación (build+lint) → T018/T019/T026
- Contratos: `build-tasks-contract.md` → T018; `app-launch-contract.md` → T019
- Research: Kotlin DSL + catalog → T001–T003; minSdk 29 → T008; JDK17/wrapper → T004–T005; sin tests → cero tareas de test; secretos → T006/T007/T020

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Sin tests en 000 por FR-005d — si el autor los pide después, se agregan en spec posterior, no aquí
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence
