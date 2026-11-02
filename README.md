# wearing-drape — Base Android (spec 000)

Esqueleto mínimo compilable y ejecutable de la app Android en Kotlin (spec
[`000-android-kotlin-base`](specs/000-android-kotlin-base/spec.md)): un solo
módulo `:app` con `MainActivity` + pantalla de bienvenida en Compose
Material3. Sin tests automatizados por decisión del autor (verificación
manual vía Run); se introducirán en un spec posterior.

## Versiones pineadas

| Pieza | Versión | Dónde |
|---|---|---|
| Android Gradle Plugin | 9.5.0 | `gradle/libs.versions.toml` (`agp`) |
| Gradle (wrapper) | 9.6.0 | `gradle/wrapper/gradle-wrapper.properties` |
| Kotlin | 2.2.10 embebido en AGP ("built-in Kotlin", **no** se aplica `kotlin-android`) | AGP 9.5.0 |
| Compose compiler plugin | 2.2.10 (debe coincidir con el Kotlin embebido) | `libs.versions.toml` (`kotlin`) |
| Compose BOM | 2026.09.00 | `libs.versions.toml` (`composeBom`) |
| compileSdk / targetSdk | 37 | `app/build.gradle.kts` |
| minSdk | 29 (Android 10) | `app/build.gradle.kts` |
| JDK para correr Gradle | 17+ (probado con el JBR 25 de Android Studio) | `JAVA_HOME` o JDK de Studio |

> APK `debug` limpio ≈ 28 MB (dex sin comprimir por alineación a páginas de
> 16 KB). El límite de 25 MB del plan se actualizó a 35 MB tras medir con el
> toolchain 2026 (AGP 9.5 + Compose BOM 2026.09): ver
> `specs/000-android-kotlin-base/contracts/build-tasks-contract.md`.

## Requisitos previos

- Windows 10/11, Android Studio **2026.2** o superior (probado con 2026.2.2).
- SDK Android con **platform android-37** + **Build-Tools 36.0.0** + un
  emulador API 37 (x86_64) creado desde Device Manager.
- RAM libre ≥ 8 GB para el emulador.
- No necesitas Gradle ni JDK instalados por separado: se usa el wrapper
  (`gradlew.bat`) y el JDK que trae Android Studio.

## Abrir, sincronizar y correr (<10 min)

```bat
cd C:\Users\alanm\Documents\wearing-drape
REM 1. Abrir esta carpeta en Android Studio (File > Open), esperar el Sync.
REM 2. Si pide SDK: apunta a %LOCALAPPDATA%\Android\Sdk (o crea local.properties, ver abajo).
REM 3. Compilar + lint desde terminal:
gradlew.bat assembleDebug
gradlew.bat lintDebug
REM 4. Arrancar el emulador y pulsar Run 'app' en Studio.
```

- ✅ Esperado: módulo `app` listado, Sync sin errores, `BUILD SUCCESSFUL`
  en ambas tareas, APK en `app\build\outputs\apk\debug\app-debug.apk`,
  bienvenida con el nombre de la app visible en <2 s, sin crash (ni en
  rotación).
- Detalle de contratos: `specs/000-android-kotlin-base/contracts/`.

### `local.properties` (no versionado)

El SDK se resuelve con `ANDROID_HOME` o con un fichero local `local.properties`
(ignorado por git). Plantilla: `local.properties.example`.

```properties
sdk.dir=C\:\\Users\\<tu-usuario>\\AppData\\Local\\Android\\Sdk
```

⚠️ En Windows hay que escapar **la barra y los dos puntos del disco**
(`C\:\\...`), si no `lintDebug` falla con `PropertyEscape`.

## Dónde irán los secretos (spec 001+)

En 000 **no hay secretos**. Cuando 001 necesite Supabase:

- `SUPABASE_URL` y `SUPABASE_ANON_KEY` irán en `local.properties` /
  variables de entorno, **nunca** en código versionado.
- La clave `service_role` está **prohibida** en el cliente.
- `.gitignore` ya cubre `local.properties`, `*.jks`/`*.keystore`, `build/`,
  `.gradle/` y `.idea/` sensible.

## Punto de extensión para 001

- UI nueva: `app/src/main/java/com/wearingdrape/app/ui/` — la pantalla
  `ui/welcome/WelcomeScreen.kt` es el ejemplo a seguir.
- Sin ViewModels, repositorios ni data sources vacíos por anticipado
  (YAGNI): se crean cuando 001 los necesite, con UI → Domain (opcional) →
  Data según la constitución (`ui` nunca accede a data sources directos).

## Troubleshooting (Windows)

| Síntoma | Causa probable / solución |
|---|---|
| `SDK location not found` | Crea `local.properties` con `sdk.dir` escapado (ver arriba) o define `ANDROID_HOME`. |
| SDK path con espacios falla | Mueve el SDK a una ruta sin espacios, p. ej. `C:\Android\Sdk`, y acepta licencias con `sdkmanager --licenses`. |
| `lintDebug` → `PropertyEscape` en `local.properties` | Escapa disco y barras: `C\:\\...` (ejemplo en este README). |
| Sync pide AGP/Gradle distintos | No cambies versiones a mano: el proyecto pinea AGP 9.5.0 + Gradle 9.6.0 (Studio 2026.2 los soporta con JDK 17+). |
| Emulador no arranca (HAXM/WHPX) | Activa "Windows Hypervisor Platform" en características de Windows y usa imagen x86_64 API 37. |
| `JAVA_HOME` apunta a JDK <17 | AGP 9 exige JDK 17+ para correr Gradle; usa el JBR de Studio (`<Studio>\jbr`) o JDK 17+. |
| Aviso "Gradle 9.8.x disponible" en lint | Informativo: nos quedamos en 9.6.0 (default probado de AGP 9.5.0). |
