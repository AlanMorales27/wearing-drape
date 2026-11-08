# Feature Specification: Agregar prenda al closet

**Feature Branch**: `001-agregar-prenda-closet`

**Created**: 2026-10-31

**Status**: Draft

**Input**: User description: "Crea una especificación para una carácteróstica que busca poder agregar una prenda al closet, la idea es poder guardar una prenda y poder: subir una foto, especificar tipo general (prenda superior, prenda inferior, calzado, accesorio), tipo específico (camiseta, camisa, chaqueta, botas, tenis, zapatos, gorro, gorra, jean, pantalón, etc.), color, nombre, fit opcional (oversize, boxy, etc.), observaciones. Todo empieza desde el tipo general y filtra los tipos específicos y fits recomendados."

## User Scenarios

### User Story 1 - Guardar una prenda con sus datos básicos (Priority: P1)

El usuario abre la opción "agregar prenda", sube una foto de la prenda, le asigna un nombre, elige el tipo general (superior, inferior, calzado, accesorio), elige el tipo específico dentro de ese grupo, elige el color desde un picker con paleta predefinida de hexadecimales y guarda. La prenda queda registrada en su closet y puede volver a verla después (la tarjeta muestra la muestra del color).

**Why this priority**: Es el valor central de la feature: sin guardar prendas no existe closet. Todo lo demás (filtros, fits, observaciones) depende de que esto funcione.

**Acceptance Scenarios**:

1. **Given** que el usuario está en el formulario de nueva prenda, **When** completa nombre, tipo general, tipo específico, elige un color del picker y adjunta una foto válida, **Then** el sistema guarda la prenda y la muestra en su closet.
2. **Given** que el usuario intenta guardar sin nombre o sin tipo general o sin tipo específico o sin color, **When** confirma el guardado, **Then** el sistema rechaza el guardado y le indica qué campo falta.
3. **Given** que el usuario adjunta un archivo que no es imagen o es excesivamente grande, **When** intenta guardarlo, **Then** el sistema rechaza esa foto con un mensaje claro y no pierde el resto de los datos ingresados.
4. **Given** que el closet tiene prendas de varios tipos generales, **When** el usuario toca cada tab (Todas, prenda superior, prenda inferior, calzado, accesorio), **Then** solo ve las prendas de ese grupo (Todas las muestra todas).

---

### User Story 2 - Tipos específicos y fits filtrados por tipo general (Priority: P1)

El usuario primero elige el tipo general de la prenda. A partir de esa elección, las opciones de tipo específico y de fit que se le ofrecen se limitan a las que tienen sentido para ese grupo. Por ejemplo, si elige "prenda superior" solo ve camiseta, camisa, chaqueta, etc., y nunca jean o tenis; si elige "prenda inferior" los fits ofrecidos son de pantalón (loose fit, baggy fit, etc.) y no oversize/boxy.

**Why this priority**: Es la regla de negocio clave pedida por el usuario ("todo empieza desde el tipo general"). Sin ella el catálogo se vuelve confuso y se guardan datos incoherentes.

**Independent Test**: Se puede probar seleccionando cada tipo general y verificando que la lista de tipos específicos y la lista de fits sugeridos cambian y solo contienen opciones del grupo correspondiente.

**Acceptance Scenarios**:

1. **Given** que el usuario selecciona tipo general "prenda superior", **When** abre el selector de tipo específico, **Then** solo ve tipos de prenda superior (camiseta, camisa, chaqueta, etc.) y no ve jean, tenis ni botas.
2. **Given** que el usuario selecciona tipo general "calzado", **When** abre el selector de tipo específico, **Then** solo ve opciones de calzado (tenis, botas, zapatos, etc.).
3. **Given** que el usuario cambia el tipo general después de haber elegido un tipo específico o un fit, **When** confirma el cambio, **Then** el sistema limpia o le pide corregir el tipo específico / fit que ya no pertenece al nuevo grupo.
4. **Given** que el usuario selecciona tipo general "prenda inferior", **When** abre las sugerencias de fit, **Then** ve fits de pantalón (p. ej. loose fit, baggy fit, slim, recto) y no ve fits exclusivos de prenda superior como oversize/boxy.

---

### User Story 3 - Completar la prenda con fit opcional y observaciones (Priority: P2)

El usuario puede, si quiere, indicar el fit de la prenda (oversize, boxy, loose fit, etc.) y escribir observaciones libres, como "requiere cinturón", "no es de mis favoritas" o "quiero experimentar más con esta prenda". Si no quiere, puede guardar sin estos datos.

**Why this priority**: Enriquece la prenda para futuros usos (combinaciones, recomendaciones), pero no bloquea el guardado básico; por eso es P2.

**Independent Test**: Se puede probar guardando una prenda sin fit ni observaciones (debe permitirlo) y otra con ambos campos completos (deben quedar guardados y visibles).

**Acceptance Scenarios**:

1. **Given** que el usuario completa todos los campos obligatorios pero deja fit y observaciones vacíos, **When** guarda, **Then** la prenda se guarda correctamente sin esos datos.
2. **Given** que el usuario escribe observaciones de hasta una longitud razonable, **When** guarda y vuelve a abrir la prenda, **Then** ve exactamente el texto que escribió.

---

### Edge Cases

- ¿Qué pasa si el usuario intenta guardar dos prendas con el mismo nombre? El sistema lo permite (el nombre no es identificador único) pero cada prenda es un registro independiente.
- ¿Cómo maneja el sistema la pérdida de conexión a mitad del guardado (especialmente subiendo la foto)? No guarda una prenda a medias: o se guarda completa (datos + foto) o no se guarda y avisa al usuario.
- ¿Qué pasa si el catálogo de un tipo general aún no tiene tipos específicos o fits definidos? El selector muestra un estado vacío con mensaje ("aún no hay opciones para este grupo") en lugar de fallar o mostrar opciones de otro grupo.
- ¿Qué pasa si la foto se elimina o queda inaccesible después de guardada? La prenda sigue existiendo con sus datos y muestra un marcador de imagen no disponible.
- ¿Qué pasa con textos muy largos en nombre u observaciones? El sistema aplica límites máximos y los informa antes de guardar (el color no tiene este problema: viene del picker, sin texto libre).
- ¿Qué pasa si el usuario cancela a mitad del formulario? No se guarda nada y se le confirma que los cambios no guardados se perderán.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST permitir al usuario crear una prenda en su closet con foto, nombre, tipo general, tipo específico y color.
- **FR-002**: El sistema MUST exigir como obligatorios: nombre, tipo general, tipo específico y color. El fit y las observaciones MUST ser opcionales.
- **FR-003**: El sistema MUST ofrecer exactamente estos tipos generales: prenda superior, prenda inferior, calzado, accesorio.
- **FR-004**: El sistema MUST filtrar las opciones de tipo específico según el tipo general seleccionado; NUNCA MUST mostrar tipos específicos de otro grupo.
- **FR-005**: El sistema MUST ofrecer sugerencias de fit filtradas por tipo general (p. ej. fits de torso para prenda superior, fits de pantalón para prenda inferior); NUNCA MUST sugerir fits de otro grupo como opción principal.
- **FR-006**: El sistema MUST permitir al usuario cambiar el tipo general antes de guardar; si el tipo específico o fit ya elegido no pertenece al nuevo grupo, el sistema MUST invalidarlo y pedir una nueva selección.
- **FR-007**: El sistema MUST aceptar solo archivos de imagen como foto de la prenda y MUST rechazar otros formatos con un mensaje claro.
- **FR-008**: El sistema MUST validar límites de tamaño de foto y de longitud de texto (nombre, color, observaciones) e informar al usuario cuando los exceda, sin perder los demás datos del formulario.
- **FR-009**: Cada prenda guardada MUST pertenecer a un único usuario y solo ese usuario MUST poder verla y gestionarla.
- **FR-010**: El sistema MUST conservar la foto junto con los datos de la prenda de modo que al reabrir la prenda se vean juntos.
- **FR-011**: El campo color MUST ser un picker con paleta predefinida de hexadecimales; el usuario MUST elegir un color de la paleta, no escribir texto libre. El valor guardado es el hexadecimal ("#RRGGBB").
- **FR-012**: El sistema MUST impedir el guardado parcial: si falla la subida de la foto o la validación de datos, ninguna prenda incompleta queda registrada como válida.
- **FR-013**: El catálogo de tipos generales, tipos específicos y fits sugeridos MUST ser gestionable (se pueden agregar nuevas opciones por grupo) sin modificar prendas ya guardadas.

### Key Entities

- **Prenda**: Representa una prenda del closet de un usuario. Atributos: nombre, foto, tipo general, tipo específico, color, fit (opcional), observaciones (opcional), propietario. Cada prenda pertenece a un solo usuario.
- **Tipo general**: Grupo raíz de clasificación (prenda superior, prenda inferior, calzado, accesorio). Determina qué tipos específicos y qué fits se sugieren.
- **Tipo específico**: Subclasificación dentro de un tipo general (p. ej. camiseta/camisa/chaqueta bajo prenda superior; jean/pantalón bajo prenda inferior; tenis/botas/zapatos bajo calzado; gorro/gorra bajo accesorio). Cada tipo específico pertenece a exactamente un tipo general.
- **Fit**: Descripción de la caída o ajuste (p. ej. oversize, boxy para superior; loose fit, baggy fit para inferior). Opcional en la prenda; las sugerencias están agrupadas por tipo general.
- **Catálogo de opciones**: Conjunto de tipos específicos y fits disponibles por tipo general, usado para poblar los selectores del formulario.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Un usuario puede registrar una prenda completa (con foto) en menos de 2 minutos desde que abre el formulario.
- **SC-002**: El 100% de las prendas guardadas tienen una combinación coherente de tipo general + tipo específico (cero prendas "superior + tenis" o similares).
- **SC-003**: El 90% de los usuarios completa el registro al primer intento sin errores de validación bloqueantes.
- **SC-004**: Al seleccionar un tipo general, las opciones de tipo específico y fit mostradas pertenecen en su totalidad a ese grupo (cero opciones intrusas en pruebas con los 4 grupos).

## Assumptions

- El usuario está autenticado cuando agrega prendas; cada closet es personal y privado por defecto.
- La foto es opcional pero fuertemente recomendada: se puede guardar una prenda sin foto, pero el formulario invita a subirla. Límite razonable por defecto: 1 foto por prenda en la primera versión, formatos de imagen comunes, tamaño máximo de unos pocos MB.
- El color se elige desde un picker con paleta predefinida de hexadecimales (sin texto libre); la paleta inicial es: #000000, #FFFFFF, #9E9E9E, #1E3A8A, #29B6F6, #D32F2F, #2E7D32, #F5F5DC, #795548, #F48FB1, #FFEB3B, #7B1FA2. Más opciones pueden agregarse sin cambiar esta spec.
- El catálogo inicial incluye al menos: superior → camiseta, camisa, chaqueta, sudadera, jersey; inferior → jean, pantalón, short, falda; calzado → tenis, botas, zapatos, sandalias; accesorio → gorro, gorra, cinturón, bufanda. Más opciones pueden agregarse sin cambiar esta spec.
- Los fits iniciales sugeridos: superior → oversize, boxy, regular, slim; inferior → loose fit, baggy fit, slim, recto, skinny; calzado y accesorio → sin fits sugeridos obligatorios (campo libre u oculto según grupo).
- Solo se contempla "agregar/guardar" en esta feature; listar, editar, eliminar y buscar prendas quedan fuera de alcance y se tratarán en features separadas.
- Se asume conectividad de red al guardar; el modo sin conexión queda fuera de alcance de la primera versión.
