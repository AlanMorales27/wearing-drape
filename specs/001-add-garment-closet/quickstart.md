# Quickstart: Verificar agregar prenda al closet (001)

**Spec**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md) | **Contracts**: [contracts/](./contracts/)

Guía de validación manual de punta a punta (sin tests automatizados por decisión del autor, igual que 000). Cubre US1–US3 y SC-001..SC-004.

## Prerrequisitos

- [ ] Base 000 en verde: `gradlew.bat assembleDebug` + `lintDebug` sin `Error`.
- [ ] Emulador API 29+ x86_64 con una foto disponible en galería (y un archivo no-imagen, p. ej. un PDF, para la prueba de rechazo).
- [ ] Cronómetro para SC-001.

## 1. Compilar + lint

```bat
gradlew.bat assembleDebug
gradlew.bat lintDebug
```

- ✅ Esperado: `BUILD SUCCESSFUL` en ambas; `lintDebug` sin `Error`.
- Detalle entidades/valores: [data-model.md](./data-model.md).

## 2. US1 — Guardar prenda completa con foto (SC-001, SC-003)

1. Abrir la app → se ve la lista del closet (vacía: "aún no hay prendas").
2. Tocar "Agregar prenda", completar nombre + general (p. ej. superior) + específico + elegir color del picker, adjuntar 1 foto válida y guardar. Cronometrar desde que se abre el formulario.
3. ✅ Esperado: prenda guardada y visible en la lista con miniatura + muestra de color + nombre + específico + color (<2 min, SC-001); snackbar "Prenda guardada".
4. Tocar cada tab (Todas, superior, inferior, calzado, accesorio) → cada tab muestra solo prendas de su grupo; Todas las muestra todas (US1-A4).
5. Repetir sin nombre / sin general / sin específico / sin color → cada intento muestra qué campo falta y conserva el resto (US1-A2, FR-008).
6. Adjuntar el archivo no-imagen → rechazo con mensaje claro, resto del formulario intacto (US1-A3, FR-007).

## 3. US2 — Filtrado por tipo general (SC-002, SC-004)

1. Elegir general "prenda superior" → el selector de específico solo ofrece camiseta/camisa/chaqueta/sudadera/jersey (cero jeans, tenis o botas).
2. Elegir "calzado" → solo tenis/botas/zapatos/sandalias.
3. Elegir "prenda inferior" → fits sugeridos de pantalón (loose/baggy/slim/recto/skinny), nunca oversize/boxy.
4. Elegir específico + fit, cambiar el general a otro grupo → el sistema limpia o pide corregir lo que ya no pertenece (FR-006).
5. ✅ Esperado: cero opciones intrusas en los 4 grupos; 100% de prendas guardadas con combinación coherente (revisar lista tras guardar una prenda por grupo).

## 4. US3 — Fit y observaciones opcionales

1. Guardar una prenda sin fit ni observaciones → se guarda OK.
2. Guardar otra con fit + observaciones ("requiere cinturón", etc.) → al reabrir/ver el detalle se lee el texto exacto.
3. ✅ Esperado: ambos casos visibles en la lista/detalle mínimo.

## 5. Casos borde (spot-check)

- Nombre duplicado → permitido, dos registros independientes.
- Texto muy largo (nombre >60, observaciones >500) → límite informado antes de guardar (el color viene del picker, sin texto libre).
- Cancelar a mitad → nada guardado + confirmación de pérdida de cambios.
- Foto borrada del almacenamiento → la prenda sigue con marcador "imagen no disponible".

## Siguiente paso

Si todo está en verde, la 001 es válida → proceder a `/speckit-tasks` para generar `tasks.md` de implementación.
