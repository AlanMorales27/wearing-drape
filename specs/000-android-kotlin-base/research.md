# Research: Base Proyecto Android Kotlin (000)

**Date**: 2026-11-02 | **Spec**: [spec.md](./spec.md)

Todas las decisiones abajo resuelven el Technical Context sin dejar NEEDS CLARIFICATION abiertos (las 4 dudas críticas se preguntaron al usuario).

## Decision 1: Compose Material3 para pantalla mínima

- **Decision**: Usar Jetpack Compose con Material3 + `ComponentActivity` (`MainActivity.kt` + `WelcomeScreen` en `ui/welcome/`).
- **Rationale**: Elección explícita del usuario; es la plantilla oficial actual de Android Studio; evita migración Views→Compose en spec 001; encaja con constitución (UI = Compose + state holders futuros, UDF).
- **Alternatives considered**:
  - XML Views + `AppCompatActivity`: descartado — más estable en máquinas viejas pero obliga a reescribir UI en 001 y duplica temas/estilos.
  - Compose sin Material3 (bare): descartado — Material3 ya viene en BOM, coste cero y mejor accesibilidad/temas.

## Decision 2: Kotlin DSL + Version Catalog

- **Decision**: `settings.gradle.kts` + `build.gradle.kts` (root y `:app`) con `gradle/libs.versions.toml` para pinear AGP, Kotlin, Compose BOM y core (sin dependencias de test).
- **Rationale**: Elección explícita del usuario; tipado + autocompletado en IDE; catalog centraliza versiones y hace reproducible el build; estándar 2024+.
- **Alternatives considered**:
  - Groovy DSL (`build.gradle`): descartado — sin tipos, peor refactor, el usuario lo rechazó.
  - Sin catalog (versiones inline): descartado — deriva de versiones y "en mi máquina sí corre".

## Decision 3: minSdk 29, target/compile = última estable del AGP

- **Decision**: `minSdk 29` (Android 10), `targetSdk`/`compileSdk` = última estable soportada por el AGP pineado (p. ej. 34/35 según fecha de implementación; se documenta en README).
- **Rationale**: Elección explícita del usuario; 29 cubre base moderna amplia y simplifica compat (sinlegacys de 24-28: permisos scoped storage, gestos, dark theme nativo); reduce matriz de verificación manual.
- **Alternatives considered**:
  - minSdk 26: descartado — más cobertura (~+3-4%) pero más ramas de compat innecesarias para MVP.
  - minSdk 24: descartado por el usuario.
- **Riesgo Windows**: emulador API 29 x86_64 + HAXM/WHPX debe documentarse en README troubleshooting.

## Decision 4: Java toolchain 17 + Gradle wrapper

- **Decision**: `JavaVersion 17` toolchain, Gradle wrapper incluido (`gradlew`/`gradlew.bat` + `gradle-wrapper.jar`), `gradle.properties` con `org.gradle.jvmargs=-Xmx3g`, `android.useAndroidX=true`.
- **Rationale**: AGP 8.x exige JDK 17; wrapper elimina dependencia de Gradle global; flags estándar evitan OOM en sync en Windows.
- **Alternatives considered**: JDK 11/21: descartados — 11 obsoleto para AGP actual, 21 aún no default soportado en todas las versiones de Studio.

## Decision 5: Sin tests automatizados en 000

- **Decision**: No incluir `src/test` ni `src/androidTest` ni dependencias de test (JUnit, androidx.test, compose-test). Verificación manual vía Run + `assembleDebug` + `lintDebug` en verde. Tests se difieren a un spec posterior.
- **Rationale**: Decisión explícita del autor (2026-11-02: "No quiero que se hagan test de ningún tipo todavía"). Respeta YAGNI y no viola constitución (exige build en verde + lint, no tests obligatorios).
- **Alternatives considered**:
  - Tests mínimos (unit + smoke): descartado por petición del autor; se retomará cuando lo pida.

## Decision 6: Secretos y .gitignore

- **Decision**: `.gitignore` con `*.iml`, `.gradle/`, `/build`, `/app/build`, `local.properties`, `*.jks`/`*.keystore`, `.idea/` sensible; `local.properties.example` con `sdk.dir=` comentado + sección README "dónde irán SUPABASE_URL/ANON_KEY en 001 (nunca `service_role` en cliente)".
- **Rationale**: Constitución lo exige; `local.properties` es el mecanismo oficial del SDK; example evita "falta fichero" en máquina limpia (edge spec).
- **Alternatives considered**: `secrets.properties` desde ya: descartado por YAGNI — no hay secretos en 000.

## Decision 7: Sin contratos de red/datos en 000

- **Decision**: `contracts/` solo documenta tareas de build y contrato de lanzamiento (qué se ve al abrir), no APIs. `data-model.md` describe entidades de proyecto, no tablas.
- **Rationale**: No hay backend/LLM en 000 (FR-009); crear OpenAPI/SQL ahora violaría YAGNI.
- **Alternatives considered**: Stub de Supabase/Retrofit: rechazado explícitamente en spec.

## Referencias a verificar al implementar

- Versión exacta AGP/Kotlin/Compose BOM estables el día de implementación (anotar en README + `libs.versions.toml`).
- Versión mínima de Android Studio probada (p. ej. Ladybug+), ruta SDK Windows sin espacios preferente.
