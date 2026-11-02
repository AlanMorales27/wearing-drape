# Data Model: Base Proyecto Android Kotlin (000)

**Date**: 2026-11-02 | **Spec**: [spec.md](./spec.md) | **Research**: [research.md](./research.md)

> Nota: 000 no tiene base de datos ni entidades de dominio (prendas/armario llegan en 001). Este modelo describe las entidades de *proyecto* necesarias para cumplir FR-001..FR-010.

## Entity 1: Proyecto Android base

- **What**: Raíz versionada que Android Studio reconoce como proyecto.
- **Attributes**:
  - `rootDir`: raíz del repo (contiene `settings.gradle.kts`)
  - `packageBase`: `com.wearingdrape.app` (namespace + applicationId debug)
  - `minSdk`: 29 | `targetSdk`/`compileSdk`: última estable del AGP pineado (documentado)
  - `agpVersion`, `kotlinVersion`, `composeBomVersion`: pineados en `libs.versions.toml`
  - `gradleWrapperVersion`: incluida en repo
- **Validation**:
  - Sync en Android Studio finaliza sin errores (US1).
  - `./gradlew help` funciona offline tras primer sync (caché).
- **State**: `no-sync` → `synced` → `built-debug` → `installed-running`.

## Entity 2: Módulo app

- **What**: Único módulo compilable `:app`.
- **Attributes**:
  - `namespace`: `com.wearingdrape.app`
  - `buildFile`: `app/build.gradle.kts` (plugins `com.android.application` + `kotlin-android` + compose, sin deps de test)
  - `manifest`: `src/main/AndroidManifest.xml` con `MainActivity` launcher + `android:exported="true"`
  - `sourceSets`: solo `main` (sin `test`/`androidTest` en 000)
- **Validation**:
  - `assembleDebug` genera `app-debug.apk` <25 MB.
  - `lintDebug` sin errores bloqueantes.
- **Relationships**: pertenece a 1 Proyecto; contiene 1 Pantalla bienvenida.

## Entity 3: Pantalla de bienvenida mínima

- **What**: `MainActivity` + `ui/welcome/WelcomeScreen.kt` (Compose) sin lógica de negocio.
- **Attributes**:
  - `route`: launcher (sin nav library en 000)
  - `visibleText`: nombre app ("wearing-drape" o string resource `app_name`)
  - `theme`: Material3 `Theme.wearingdrape` (claro/oscuro por defecto sistema)
- **Validation**: verificación manual — launch muestra texto, sin crash en apertura ni en rotación básica.
- **State transitions**: `not-installed` → `launched` → `visible` → (`recreated` tras rotación sigue `visible`).

## Entity 4: Configuración local no versionada

- **What**: Valores por máquina/entorno, jamás commiteados.
- **Attributes**:
  - `sdk.dir`: ruta SDK local (en `local.properties`)
  - `futureSecrets`: placeholder documentado (`SUPABASE_URL`, `SUPABASE_ANON_KEY` llegarán en 001; `service_role` PROHIBIDA en cliente)
  - `exampleFile`: `local.properties.example` versionado como plantilla
- **Validation**:
  - `git ls-files | grep local.properties` → vacío (SC-003).
  - Build debug funciona sin secretos reales (defaults vacíos documentados).
- **Relationships**: referenciada por build, nunca por código versionado.

## Entity 5: Verificación base (build + lint, sin tests)

- **What**: Criterio ejecutable de "base válida" sin tests automatizados.
- **Attributes**:
  - `lint`: `lintDebug` sin `Error`
  - `commands`: `assembleDebug`, `lintDebug` + Run manual en emulador
- **Validation**: build + lint en verde y Run manual muestra bienvenida (SC-001/SC-002).
