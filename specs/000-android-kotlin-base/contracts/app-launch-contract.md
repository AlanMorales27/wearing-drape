# App Launch Contract: 000-android-kotlin-base

**Spec**: [spec.md](../spec.md)

Contrato mínimo de lo que el usuario ve al abrir la app. Verificación manual (sin tests automatizados en 000).

## Given / When / Then

- **Given** app `com.wearingdrape.app` instalada (debug) en emulador/dispositivo API 29+
- **When** se lanza desde el launcher (tap icono) o `adb shell am start -n com.wearingdrape.app/.MainActivity`
- **Then**:
  1. `MainActivity` es `exported=true` y responde al intent `MAIN` + `LAUNCHER`.
  2. En <2s (emulador medio) se muestra `WelcomeScreen` con texto que contiene el nombre de la app (string resource `app_name`, p. ej. "wearing-drape").
  3. No hay crash, pantalla negra permanente ni ANR en launch ni en rotación simple.
  4. Cerrar (back) + relanzar repite el resultado.

## Verificación manual

- Abrir app y confirmar visualmente el texto de bienvenida (string `app_name`).
- Sin requisito de `testTag`; se agregará cuando se introduzcan tests en un spec posterior.

## Fuera de contrato (000)

Navegación, login, lista de prendas, red, permisos runtime, deep links. Todo eso llega en specs 001+.
