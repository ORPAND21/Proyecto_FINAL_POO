# Flujos de CursoEstudiante, Monitoria y PlanEstudioMateria

Diagramas UML independientes de los flujos de **CursoEstudiante**, **Monitoria** y
**PlanEstudioMateria**, generados a partir del código Java actual del proyecto.
Esta carpeta es independiente de `../flujos-entidades/`; los 10 diagramas
anteriores no se modificaron.

## Entregables

| Diagrama | Entidad | Clases | Relaciones estructurales | Dependencias `..> <<use>>` | Enlaces dibujados | Tamaño SVG |
|---|---|---:|---:|---:|---:|---|
| `11-curso-estudiante` | CursoEstudiante | 46 | 71 | 44 | 115 | 7710 × 3197 px |
| `12-monitoria` | Monitoria | 63 | 101 | 58 | 159 | 8933 × 3384 px |
| `13-plan-estudio-materia` | PlanEstudioMateria | 46 | 71 | 46 | 117 | 6469 × 3044 px |

Cada diagrama se entrega en tres formatos:

- `.puml`: fuente PlantUML editable.
- `.svg`: vector de revisión y ampliación sin pérdida.
- `.png`: raster de revisión a 96 dpi.

## Contenido

Los diagramas conservan **todas las clases y todas las relaciones** del flujo
correspondiente, incluidas las dependencias de método `..> <<use>>`. No se usa
`hide members`: se muestran atributos, constructores y métodos de cada clase.

Formato y estilo:

- Tipografía Helvetica; 11 px para nombres de clase y 9 px para miembros en el
  SVG a escala nativa.
- Paleta pastel: interfaces en verde, clases abstractas en naranja y records en
  azul.
- Paquetes con estilo carpeta, fondo blanco y enrutado ortogonal
  (`linetype ortho`), dirección *top to bottom*.
- Título `Flujo de <Entidad>` en cada diagrama.

## Tamaño de impresión

Las medidas de impresión se calculan escalando el ancho del diagrama a **90 cm**;
la letra escala en la misma proporción.

| Diagrama | Impresión a 90 cm de ancho | Clase | Atributos/métodos |
|---|---|---|---|
| CursoEstudiante | 90,0 × 37,3 cm | ≈ 3,64 pt | ≈ 2,98 pt |
| Monitoria | 90,0 × 34,1 cm | ≈ 3,14 pt | ≈ 2,57 pt |
| PlanEstudioMateria | 90,0 × 42,3 cm | ≈ 4,34 pt | ≈ 3,55 pt |

Para otro ancho de impresión, el tamaño de letra se escala de forma lineal: por
ejemplo, al doble de ancho (180 cm) la letra duplica su tamaño en puntos.

## Validación

Los tres diagramas fueron validados sobre los archivos copiados en esta carpeta:

- Conjunto de clases exacto: 46, 63 y 46 clases, respectivamente.
- Relaciones estructurales y dependencias exactas: 115, 159 y 117 enlaces.
- Todos los nombres de clase visibles en el SVG.
- Cero solapamientos entre nodos.
- Resultado del validador: `PROBLEMS: 0`.

Métrica de trazado (cruces de líneas): 528 en CursoEstudiante, 984 en Monitoria
y 503 en PlanEstudioMateria. Los diagramas incluyen todas las dependencias
`..>`, por lo que la densidad de líneas es mayor que en los diagramas antiguos,
que las conservaban comentadas. `PlanEstudioMateria` utiliza además 6 enlaces
ocultos de alineación; no se dibujan y no alteran el conteo de relaciones.

Los PNG se generaron a 96 dpi y son deterministas. Sus dimensiones son 1 px
menores que las del SVG por el redondeo propio de PlantUML:

| Diagrama | PNG |
|---|---|
| CursoEstudiante | 7709 × 3196 px |
| Monitoria | 8932 × 3383 px |
| PlanEstudioMateria | 6468 × 3043 px |

## Fuente y método

Los diagramas se regeneraron desde el UML completo del código Java actual
(185 clases y 551 relaciones). Tras depurar los artefactos `ID` y
`AutoCloseable` del generador quedaron 182 clases y 547 relaciones. Para cada
entidad se aplicó un recorte por
alcance con búsqueda en anchura y reglas de inclusión, conservando las clases y
relaciones del flujo completo. El layout se eligió por búsqueda de variantes de
paquetes, separación y semillas, y se verificó con renderizado secuencial para
evitar variaciones de medida.

## Integridad

`SHA256SUMS.txt` contiene el hash SHA-256 de los nueve archivos entregados.
