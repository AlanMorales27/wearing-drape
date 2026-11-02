# Quickstart: Verificar la base 000

**Spec**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md) | **Contracts**: [contracts/](./contracts/)

Guía para probar de punta a punta que la base cumple SC-001..SC-004. Sin código de implementación, solo validación.

## Prerrequisitos

- [ ] Windows 10/11, Android Studio reciente (versión exacta en README al implementar)
- [ ] JDK 17 (toolchain Gradle, no requiere `JAVA_HOME` manual si Studio lo provee)
- [ ] SDK Android instalado + Platform-Tools (`adb`) + emulador API 29+ x86_64 creado (p. ej. Pixel 6 API 30)
- [ ] Ram libre ≥8 GB para emulador

## 1. Abrir y sincronizar (US1 / SC-001)

```bat
cd C:\Users\alanm\Documents\wearing-drape
REM Abrir esta carpeta en Android Studio (File > Open), esperar Sync
```

- ✅ Esperado: módulo `app` listado, Sync sin errores.
- ❌ Si falla: ver README troubleshooting (SDK path con espacios → mover SDK a `C:\Android\Sdk`; aceptar licencias con `sdkmanager --licenses`; actualizar AGP/Studio a versión pineada).

## 2. Compilar + lint (sin emulador, sin tests)

```bat
gradlew.bat assembleDebug
gradlew.bat lintDebug
```

- ✅ Esperado: `BUILD SUCCESSFUL` en ambas; APK en `app\build\outputs\apk\debug\app-debug.apk`; lint sin `Error`.
- Detalle contrato: [build-tasks-contract.md](./contracts/build-tasks-contract.md)

## 3. Verificación manual en emulador (US2 / SC-002)

```bat
REM 1. Arrancar emulador desde Device Manager y verificar:
adb devices
REM 2. Pulsar Run 'app' en Studio y verificar pantalla
```

- ✅ Esperado: al abrir manual se ve bienvenida con nombre app en <2s, sin crash; rotación mantiene texto. Oracle: [app-launch-contract.md](./contracts/app-launch-contract.md)
- ❌ Si falla: `adb logcat`, verificar `android:exported="true"` en Manifest, tema Material3 aplicado, emulador API ≥29.

## 4. Higiene repo (US3 / SC-003 / SC-004)

```bat
git status --short
git ls-files | findstr local.properties
findstr /S /I "supabase key- service_role api_key" app\src
```

- ✅ Esperado: `git status` limpio salvo ficheros intencionales; `local.properties` NO listado; 0 coincidencias de secretos; árbol solo con ficheros de [plan.md](./plan.md#source-code-repository-root).
- Revisar `.gitignore` cubre `build/`, `.gradle/`, `local.properties`, `*.jks`.

## 5. Medir SC-001 (tiempo puesta en marcha)

Cronometrar desde `File > Open` hasta bienvenida visible en emulador ya creado. Objetivo: <10 min con red estable. Anotar tiempo real en el PR de implementación.

## Siguiente paso

Si todo está en verde, la base es válida → proceder a `/speckit-tasks` para generar `tasks.md` de implementación.
