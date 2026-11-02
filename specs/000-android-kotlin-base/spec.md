# Feature Specification: Base Proyecto Android Kotlin

**Feature Branch**: `000-android-kotlin-base`

**Created**: 2026-10-31

**Status**: Draft

**Input**: User description: "Quiero que crees el spec 000 para crear el proyecto en kotlin, quiero un proyecto para android en kotlin, solo quiero crear las carpetas bassicas y lo necesario para que corra el proyecto"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Obtener esqueleto de proyecto que abre sin errores (Priority: P1)

Como desarrollador quiero clonar/abrir el repositorio en Android Studio y que el IDE reconozca el proyecto Android sin errores de configuración, para empezar a trabajar inmediatamente.

**Why this priority**: Sin un proyecto reconocible y sincronizable no existe ningún otro flujo de valor. Es el prerrequisito de todo el producto wearing-drape.

**Independent Test**: Se puede probar abriendo la carpeta raíz en Android Studio y verificando que la sincronización del proyecto finaliza sin errores y el módulo app aparece listado.

**Acceptance Scenarios**:

1. **Given** un equipo con Android Studio instalado, **When** el desarrollador abre la carpeta raíz del repositorio, **Then** el IDE reconoce un proyecto Android con un módulo `app` y completa la sincronización sin errores.
2. **Given** el proyecto sincronizado, **When** el desarrollador explora el árbol de archivos, **Then** encuentra solo las carpetas y archivos básicos esperados (manifiesto, código fuente Kotlin, recursos, ficheros de construcción y `local.properties.example`/gitignore) sin carpetas especulativas adicionales.

---

### User Story 2 - Compilar y ejecutar app mínima (Priority: P1)

Como desarrollador quiero pulsar Run y ver una pantalla mínima de la app en un emulador o dispositivo, para confirmar que la base compila y se ejecuta de punta a punta.

**Why this priority**: "Que corra el proyecto" es el criterio explícito del usuario. Sin ejecución verificada no hay base válida.

**Independent Test**: Se puede probar ejecutando la configuración de Run por defecto y observando que la app se instala, abre y muestra una pantalla de bienvenida sin cierres inesperados.

**Acceptance Scenarios**:

1. **Given** el proyecto sincronizado, **When** el desarrollador ejecuta la configuración Run por defecto en un emulador/dispositivo, **Then** la app compila en modo debug, se instala y se abre mostrando una pantalla mínima de bienvenida (p. ej. nombre de la app).
2. **Given** la app instalada, **When** el desarrollador la cierra y la reabre, **Then** vuelve a abrirse sin cierres forzosos ni pantallas en blanco permanentes.

---

### User Story 3 - Base lista para crecer según constitución (Priority: P3)

Como desarrollador quiero que la base mínima ya respete las reglas transversales (secretos fuera del código, estructura lista para capas UI/Data), para no tener que refactorizar al añadir la primera funcionalidad real.

**Why this priority**: P3 porque no bloquea el "corre", pero evita deuda inmediata y alinea con la constitución del proyecto.

**Independent Test**: Se puede probar revisando que no hay claves en el código, que `local.properties` está ignorado y que existe una carpeta de partida clara para el código UI donde vivirá la primera pantalla.

**Acceptance Scenarios**:

1. **Given** el repositorio recién creado, **When** se busca cualquier clave, token o URL secreta en archivos versionados, **Then** no se encuentra ninguna y existe documentación mínima de dónde colocarlas fuera del código.
2. **Given** la estructura base, **When** se planifica la funcionalidad 001 (armario/prendas), **Then** hay un lugar evidente donde añadir UI y, más tarde, repositorios/data sources sin reestructurar el proyecto.

---

### Edge Cases

- ¿Qué pasa si el desarrollador no tiene el SDK de Android instalado? El proyecto debe fallar con un mensaje guiado (instalar SDK / aceptar licencias), no con un error críptico de fichero ausente.
- ¿Cómo se comporta el proyecto si se abre con una versión antigua de Android Studio? Debe indicarse la versión mínima probada en el README para evitar sincronizaciones parciales.
- ¿Qué pasa si faltan archivos locales no versionados (`local.properties`)? La compilación debug debe seguir funcionando con valores por defecto vacíos/documentados, sin requerir secretos reales.
- ¿Cómo se evita commitear secretos por accidente? `.gitignore` debe cubrir `local.properties`, carpeta `build/`, `.idea` sensible y keystores.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El repositorio MUST contener una estructura mínima de proyecto Android reconocible por Android Studio con un único módulo `app`.
- **FR-002**: El proyecto MUST incluir manifiesto de la app con una actividad principal lanzable (launcher) que muestre una pantalla mínima de bienvenida.
- **FR-003**: El proyecto MUST incluir código fuente inicial en Kotlin con la actividad/pantalla principal y organización de paquetes base (p. ej. paquete raíz de la app).
- **FR-004**: El proyecto MUST incluir recursos mínimos (nombre de app, tema, icono/strings) para que la app se instale y se muestre correctamente en el lanzador.
- **FR-005**: El proyecto MUST incluir ficheros de construcción que permitan sincronizar y compilar el variant `debug` con un solo comando sin intervención manual más allá de instalar SDK.
- **FR-005d**: El proyecto MUST NOT incluir tests automatizados de ningún tipo en este spec (sin `src/test`, sin `src/androidTest`, sin dependencias de test). La verificación es manual vía Run (US2) y la introducción de tests se difiere a un spec posterior.
- **FR-006**: El proyecto MUST incluir configuración de control de versiones que excluya artefactos generados y secretos locales (`build/`, `local.properties`, keystores).
- **FR-007**: El proyecto MUST NOT incluir claves, tokens ni URLs secretas en archivos versionados; MUST documentar (README breve) dónde colocar valores locales y cómo obtener una compilación debug sin ellos.
- **FR-008**: El proyecto MUST incluir un README breve con requisitos previos (Android Studio mínimo, JDK/SDK), pasos de apertura, sincronización y Run, y solución a los 2-3 errores más comunes.
- **FR-009**: La estructura MUST limitarse a lo necesario para compilar y correr (YAGNI): sin módulos extra, sin capas Domain/Data completas, sin integraciones (Supabase, LLM, navegación, red) en este spec.
- **FR-010**: La estructura de código MUST dejar un punto de extensión evidente para la capa UI futura (carpeta/paquete donde vivirá la primera pantalla y su ViewModel) sin crear ViewModels, repositorios o data sources vacíos por anticipado.

### Key Entities

- **Proyecto Android base**: Raíz versionada que se abre en el IDE; atributos: nombre, paquete base, SDK mínimo/objetivo documentados, variante debug ejecutable.
- **Módulo app**: Unidad compilable; atributos: manifiesto, código Kotlin inicial, recursos mínimos, ficheros de construcción.
- **Pantalla de bienvenida mínima**: Actividad/pantalla lanzable; atributos: texto/título visible, sin lógica de negocio, criterio de éxito = se muestra sin cierres.
- **Configuración local no versionada**: Valores propios de cada máquina (SDK path, futuros secretos); atributos: ubicación fuera del repo versionado, ejemplo documentado, ignorada por git.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Un desarrollador con Android Studio y SDK instalados abre el proyecto, sincroniza y pulsa Run, y ve la pantalla de bienvenida en un emulador en menos de 10 minutos sin ayuda externa.
- **SC-002**: La compilación `debug` finaliza con éxito al primer intento en una máquina limpia con prerrequisitos instalados (tasa de éxito 100% en 3 intentos de verificación).
- **SC-003**: Cero secretos o claves reales presentes en archivos versionados en una auditoría por búsqueda de texto (resultado 0 coincidencias).
- **SC-004**: El 100% de los archivos versionados son necesarios para abrir, compilar o correr (revisión manual: no hay módulos, pantallas ni dependencias especulativas).

## Assumptions

- Se asume Android Studio reciente estable (se documentará la versión mínima probada en el README durante la implementación).
- Se asume Gradle con wrapper incluido para no depender de una instalación global de Gradle.
- Se asume paquete base `com.wearingdrape.app` salvo que el autor indique otro durante la revisión del plan.
- Se asume UI mínima con la plantilla oficial actual de Android (Compose Material3). Si la plantilla oficial cambia, se usa la opción por defecto más simple sin navegación ni pantallas extra.
- Se asume SDK mínimo 26 y SDK objetivo/compilación con la versión estable soportada por el Android Gradle Plugin elegido; se documentan en el README.
- Se asume que este spec no crea tablas Supabase, políticas RLS, ni integración LLM; esas llegarán en specs posteriores (p. ej. 001).
- Se asume control de versiones git ya inicializado en la carpeta (repositorio wearing-drape existente).
- Se asume explícitamente sin tests automatizados en 000 por decisión del autor (2026-11-02); no se agregan dependencias ni source-sets de test.
