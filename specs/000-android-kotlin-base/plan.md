# Implementation Plan: Base Proyecto Android Kotlin (000)

**Branch**: `000-android-kotlin-base` | **Date**: 2026-11-02 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/000-android-kotlin-base/spec.md` + respuestas usuario 2026-11-02 (Compose Material3, `com.wearingdrape.app`, minSdk 29+, Kotlin DSL).

## Summary

Crear el esqueleto mínimo compilable y ejecutable de la app Android en Kotlin en la raíz del repo: un solo módulo `app` con `MainActivity` Compose que muestra bienvenida, build con Gradle Kotlin DSL + version catalog, `local.properties` fuera de versionado y README con Run en <10 min. Sin tests automatizados de ningún tipo por decisión explícita del autor (verificación manual vía Run). Sin Supabase, sin navegación/DI/red (YAGNI, se difiere a 001+).

## Technical Context

**Language/Version**: Kotlin 2.x (última estable soportada por el AGP elegido al implementar; se pinea en `libs.versions.toml` y README) + Java toolchain 17

**Primary Dependencies**: Android Gradle Plugin (último estable), androidx.activity-compose, Compose BOM + material3 + ui-tooling-preview, lifecycle-runtime-compose. Sin dependencias de test (JUnit, androidx.test, compose-test excluidos a petición del autor).

**Storage**: N/A (solo filesystem local; `local.properties` no versionado + `local.properties.example` documentado)

**Testing**: Sin tests automatizados en 000 por decisión explícita del autor (2026-11-02). Verificación manual: Run en emulador + `assembleDebug` + `lintDebug` en verde. Tests se introducirán en spec posterior.

**Target Platform**: Android, minSdk 29 (decisión usuario), target/compile = última estable soportada por AGP, verificación en emulador API 29+ (x86_64)

**Project Type**: mobile-app, single-module (`:app`)

**Performance Goals**: sync limpio <3 min con red estable; `assembleDebug` incremental <2 min en máquina media; cold start bienvenida <2s en emulador medio; APK debug <25 MB

**Constraints**: Dev en Windows 10/11 + Android Studio reciente (versión mínima se documenta en README); secretos jamás versionados; YAGNI estricto (cero módulos/features extra); build offline posible tras primer sync (caché Gradle)

**Scale/Scope**: 1 módulo, 1 pantalla, ~15-25 archivos versionados, 1 dev inicial; crecimiento previsto en 001 sin reestructurar (paquete `ui` como punto de extensión)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **I. Simplicidad/YAGNI**: PASS — un módulo, una pantalla, sin Domain/Data, sin DI/navegación/red y sin tests automatizados (excluidos a petición del autor).
- [x] **II. Arquitectura por capas**: PASS — solo UI mínima (Compose + Activity). Sin ViewModel/repositorio/data-source vacíos por anticipado; `ui/` documentado como punto de extensión para 001. Sin acceso a data sources (no existen).
- [x] **Secretos fuera del código**: PASS — `local.properties` + keystores en `.gitignore`, cero secretos en repo (SC-003), README explica valores locales dummy.
- [x] **Datos/RLS**: N/A — este spec no crea tablas Supabase. Se anota explícitamente como fuera de alcance.
- [x] **CI/lint estricto**: PASS — plan incluye `lintDebug` + `assembleDebug` en verde como criterio de aceptación; tipos Kotlin verificados por compilación. Sin tests en 000 por decisión del autor; no viola constitución (exige build+lint, no tests obligatorios).
- [x] **Complejidad justificada**: PASS — cero abstracciones extra; sin instrumentación ni frameworks de test para no sobredimensionar.

Re-check post-Phase 1 (2026-11-02): sin cambios — research/data-model/quickstart no introducen abstracciones nuevas. PASS.

## Project Structure

### Documentation (this feature)

```text
specs/000-android-kotlin-base/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
│   ├── build-tasks-contract.md
│   └── app-launch-contract.md
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
/
├── settings.gradle.kts          # include(":app"), repos centralizados
├── build.gradle.kts             # plugins root, limpieza
├── gradle.properties            # flags AndroidX, Kotlin, memoria Gradle
├── gradle/
│   ├── libs.versions.toml       # AGP, Kotlin, Compose BOM pines (sin deps de test)
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradlew / gradlew.bat        # wrapper ejecutable
├── local.properties             # NO versionado (sdk.dir local)
├── local.properties.example     # plantilla documentada
├── .gitignore                   # build/, .gradle/, local.properties, *.jks, .idea sensibles
├── README.md                    # requisitos, apertura, Run, troubleshooting (sin sección tests)
└── app/
    ├── build.gradle.kts         # namespace com.wearingdrape.app, minSdk 29, compose, sin deps de test
    └── src/
        └── main/
            ├── AndroidManifest.xml
            ├── java/com/wearingdrape/app/
            │   ├── MainActivity.kt
            │   └── ui/
            │       └── welcome/      # punto extensión (WelcomeScreen.kt)
            └── res/
                ├── values/strings.xml + themes.xml
                └── mipmap-*/ic_launcher
```

**Structure Decision**: Proyecto Android single-module en la raíz del repo (no `src/` genérico ni `android/` anidado) porque el repo es la app. Paquete `com.wearingdrape.app`, Gradle Kotlin DSL + version catalog (decisión usuario). Carpeta `ui/welcome/` mínima como semilla extensible sin crear ViewModels/repos vacíos.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| Compose Material3 vs Views planas | Decisión usuario + estándar oficial; evita refactorizar UI en 001 | Views obligaría a migrar a Compose en 001 |

