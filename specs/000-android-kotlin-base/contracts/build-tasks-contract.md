# Build Tasks Contract: 000-android-kotlin-base

**Spec**: [spec.md](../spec.md) | **Plan**: [plan.md](../plan.md)

Este contrato define las tareas que la base MUST exponer. Cualquier cambio de nombres/comportamiento rompe el contrato y debe actualizar README + `quickstart.md`.

## Tasks

| Tarea | Comando | Entrada | Salida esperada | Falla si |
|-------|---------|---------|-----------------|----------|
| Sync | Abrir en Android Studio → Sync | `settings.gradle.kts` válido | Módulo `:app` listado, sin errores | Falta SDK / AGP incompatible |
| Build debug | `./gradlew assembleDebug` (Windows: `gradlew.bat assembleDebug`) | Fuentes + recursos | `app/build/outputs/apk/debug/app-debug.apk` (<25 MB) | Error compilación, Manifest inválido |
| Lint | `./gradlew lintDebug` | Todo `src/main` | Sin `Error`, reporte `app/build/reports/lint-results-debug.html` | Regla `Error` |
| Run manual | Run 'app' en Studio (emulador API 29+) | APK debug | Bienvenida visible <2s, sin crash | Crash, texto no visible, sin dispositivo |

## Invariantes

- Todos los comandos corren desde la raíz del repo.
- Sin parámetros extra ni secretos para debug y sin suites de test en 000.
- `local.properties` ausente NO rompe `assembleDebug` (SDK por defecto o variable `ANDROID_HOME`).
