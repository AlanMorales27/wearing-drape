
# wearing-drape Constitution

## Principios fundamentales

### I. Simplicidad / YAGNI

Toda implementación MUST empezar con la solución más simple que satisfaga la especificación.
Queda prohibido añadir abstracciones, capas o configuraciones especulativas ("por si acaso").
Toda complejidad adicional MUST justificarse por escrito (en la spec o en el PR) antes de integrarse.
Motivo: el proyecto combina app móvil, backend y LLM; la complejidad no justificada multiplica
los puntos de fallo y el coste de mantenimiento.

### II. Arquitectura por capas (variante oficial de Android)

El código MUST organizarse según la guía oficial de arquitectura de Android:

- **Capa UI** (presentación): elementos de UI (Compose) + *state holders* (ViewModels)
  que exponen UI state con flujo unidireccional de datos (el estado baja, los eventos
  suben). MUST NOT contener lógica de negocio ni acceder directamente a *data sources*.
- **Capa Data** (obligatoria): repositorios como único punto de entrada + *data sources*,
  cada uno dedicado a una sola fuente (Supabase, LLM, archivos, caché). Los repositorios
  exponen los datos, centralizan los cambios y contienen la lógica de negocio. La
  comunicación con Supabase y la configuración de servicios externos (endpoints,
  timeouts, referencias a credenciales) viven en esta capa.
- **Capa Domain** (opcional): casos de uso entre los ViewModels y los repositorios, solo
  cuando haya lógica compleja o reutilizada por varios ViewModels. Cada caso de uso
  MUST tener una sola responsabilidad y MUST NOT contener datos mutables.

Regla de dependencias: UI → Domain (si existe) → Data. Ninguna capa superior MUST
acceder a un *data source* directamente; la entrada a Data son siempre los repositorios.
Cada módulo MUST tener una única razón para cambiar.
Motivo: empezar simple y añadir capas solo cuando duelen (coherente con el principio I);
los repositorios aíslan al resto de la app de Supabase y del LLM, permitiendo sustituir
proveedores sin tocar la UI.

## Restricciones técnicas

- Stack definido: app Android en Kotlin; backend en Supabase (Postgres/Auth/Storage);
  integración LLM mediante API key.
- Secretos: las API keys y credenciales MUST vivir fuera del código (p. ej. `local.properties`,
  variables de entorno o gestor de secretos) y MUST NOT commitearse jamás al repositorio.
  La clave de servicio (service-role) de Supabase MUST NOT ir nunca en el cliente.
- Datos: toda tabla de Supabase accesible desde el cliente MUST estar protegida con
  políticas RLS (Row Level Security) revisadas antes de exponerlas.

## Flujo de desarrollo

- CI / lint estricto: ningún cambio se integra sin build en verde, lint de Kotlin
  (ktlint/detekt o equivalente) y tipos verificados.
- Complejidad justificada: todo PR o spec que introduzca una abstracción nueva MUST incluir
  su justificación; sin ella, el cambio se rechaza.
- Revisión: el autor del repositorio aprueba cada integración y verifica el cumplimiento
  de esta constitución.

## Gobernanza

Esta constitución prevalece sobre cualquier otra práctica o convención del proyecto.
Las enmiendas requieren: propuesta documentada, aprobación explícita del autor del
repositorio y plan de migración si el cambio rompe compatibilidad con trabajo existente.
Versionado semántico: MAJOR por eliminaciones o redefiniciones incompatibles de principios;
MINOR por principios o secciones nuevas o ampliaciones materiales; PATCH por aclaraciones
y correcciones de redacción. Toda spec, PR y revisión MUST verificar el cumplimiento
de la versión vigente de esta constitución.
