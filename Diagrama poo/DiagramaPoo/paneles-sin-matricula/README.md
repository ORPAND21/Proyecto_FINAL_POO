# UML canónico web-only sin Matrícula — presentación en 17 paneles

Fecha de cierre: 2026-11-08

Fuente canónica completa: `../diagrama-qa-genericos-sin-matricula.puml`
(2.479 líneas, 141 clases, 190 relaciones de los 4 tipos estructurales y 30 generalizaciones).

Este directorio contiene la presentación verificada con **0 líneas superpuestas, 0 perpendiculares y 0 cruces oblicuos** entre las relaciones de **asociación, agregación, realización y composición**, en cada uno de los 17 paneles.

---

## 1. Resultado

| Métrica | Valor |
|---|---|
| Paneles | 17 (01–07 de capa + 08–17 transversales) |
| Clases únicas | 141 (todas las del UML fuente) |
| Relaciones de los 4 tipos | 190 (123 asociación, 34 realización, 24 composición, 9 agregación) |
| Cobertura | 190/190, cada relación aparece en exactamente un panel |
| Validación geométrica | 17/17 paneles con OVERLAP = 0, PERP = 0, CROSS = 0 |
| Archivo único para PlantUML | `uml-completo.puml` (18 bloques: modelo completo + 17 paneles) |
| Proyecto para el editor | `DiagramaFinal/` (modelo completo en un solo bloque, 141 posiciones) |
| Referencias a Matrícula en `.puml`/`.svg` | 0 |
| Clases de consola | 0 |

Cada panel tiene su `.puml` (fuente), `.svg` (vectorial) y `.png` (raster).

---

## 2. Por qué no es posible un solo plano

El grafo de las 190 relaciones de los 4 tipos tiene:

- 113 nodos conectados (141 clases contando las aisladas),
- 2 componentes conexas,
- 190 aristas: 123 asociación, 34 realización, 24 composición, 9 agregación.

`networkx.check_planarity` devuelve **False**. Por el teorema de Kuratowski, el grafo contiene una subdivisión de `K5` o de `K3,3`; por tanto, **ningún dibujo en un solo plano puede tener 0 cruces**.

Como referencia, la planarización codiciosa conserva 162 aristas y descarta 28. El detalle de las 28 aristas descartadas está en `planarizacion-4-tipos.csv`. Esto no afirma que 28 sea el mínimo: solo documenta una planarización posible y confirma que el grafo completo no es planar.

La solución adoptada es una **partición por vistas**: cada panel contiene un subconjunto de relaciones que sí admite un dibujo sin cruces. La unión de los 17 paneles cubre las 190 relaciones sin duplicarlas.

---

## 3. Partición en paneles

### 3.1 Paneles de capa (01–07)

Cubren las **88 relaciones internas** (ambos extremos en el mismo módulo):

- **01-modelo** — 20 clases, 18 asociaciones.
- **02-dto** — 31 clases, 7 asociaciones.
- **03-repositorio-persistencia** — 31 clases, 27 relaciones (19 realizaciones + 8 asociaciones).
- **04-mapeador-realizaciones** — 8 clases, 5 realizaciones.
- **05-mapeador-asociaciones** — 11 clases, 7 asociaciones.
- **06-servicio-controlador** — 20 clases, 13 asociaciones.
- **07-vista-config** — 28 clases, 11 relaciones (10 realizaciones + 1 agregación interna).

### 3.2 Paneles transversales (08–17)

Cubren las **102 relaciones cruzadas** entre módulos:

| Panel | Contenido | Relaciones |
|---|---|---|
| 08-composicion-transversal | Composición configuración → controladores/repositorios | 24 composiciones |
| 09-agregacion-transversal | Agregación cargador → modelo (caché) | 8 agregaciones |
| 10-asociaciones-dto-modelo | DTO → enum del modelo | 14 asociaciones |
| 11-asociaciones-servicio-mapeador | Servicio → mapeador | 10 asociaciones |
| 12-asociaciones-vista-repositorio | Configuración de módulo → repositorio | 10 asociaciones |
| 13-asociaciones-config-servicio-controlador | Configuración → servicio/controlador | 6 asociaciones |
| 14-asociaciones-vista-web-controlador | Vista web → controlador | 10 asociaciones |
| 15-asociaciones-servicio-repositorio-simple | Servicio → repositorio (una dependencia) | 5 asociaciones |
| 16-asociaciones-servicio-repositorio-curso-profesor-plan | CursoProfesor y PlanEstudio → repositorios | 8 asociaciones |
| 17-asociaciones-servicio-repositorio-periodo-formacion-programa | CursoPeriodo, FormacionProfesor y Programa → repositorios | 7 asociaciones |

La agregación interna restante (1 de las 9) está cubierta por el panel 07; por eso el panel 09 contiene 8, no 9.

### 3.3 Tabla de métricas

| Panel | Clases | Total | Asoc. | Realiz. | Comp. | Agreg. | O/P/C | SVG |
|---|---:|---:|---:|---:|---:|---:|---|---|
| 01-modelo | 20 | 18 | 18 | 0 | 0 | 0 | 0/0/0 | 5618×1569 |
| 02-dto | 31 | 7 | 7 | 0 | 0 | 0 | 0/0/0 | 2789×1071 |
| 03-repositorio-persistencia | 31 | 27 | 8 | 19 | 0 | 0 | 0/0/0 | 3103×1781 |
| 04-mapeador-realizaciones | 8 | 5 | 0 | 5 | 0 | 0 | 0/0/0 | 2889×571 |
| 05-mapeador-asociaciones | 11 | 7 | 7 | 0 | 0 | 0 | 0/0/0 | 3293×625 |
| 06-servicio-controlador | 20 | 13 | 13 | 0 | 0 | 0 | 0/0/0 | 2983×2390 |
| 07-vista-config | 28 | 11 | 0 | 10 | 0 | 1 | 0/0/0 | 4395×2305 |
| 08-composicion-transversal | 35 | 24 | 0 | 0 | 24 | 0 | 0/0/0 | 3140×3654 |
| 09-agregacion-transversal | 16 | 8 | 0 | 0 | 0 | 8 | 0/0/0 | 1752×2601 |
| 10-asociaciones-dto-modelo | 16 | 14 | 14 | 0 | 0 | 0 | 0/0/0 | 859×1526 |
| 11-asociaciones-servicio-mapeador | 20 | 10 | 10 | 0 | 0 | 0 | 0/0/0 | 3402×2354 |
| 12-asociaciones-vista-repositorio | 11 | 10 | 10 | 0 | 0 | 0 | 0/0/0 | 4613×421 |
| 13-asociaciones-config-servicio-controlador | 9 | 6 | 6 | 0 | 0 | 0 | 0/0/0 | 3140×1310 |
| 14-asociaciones-vista-web-controlador | 20 | 10 | 10 | 0 | 0 | 0 | 0/0/0 | 1659×1975 |
| 15-asociaciones-servicio-repositorio-simple | 10 | 5 | 5 | 0 | 0 | 0 | 0/0/0 | 1364×1051 |
| 16-asociaciones-servicio-repositorio-curso-profesor-plan | 10 | 8 | 8 | 0 | 0 | 0 | 0/0/0 | 2660×705 |
| 17-asociaciones-servicio-repositorio-periodo-formacion-programa | 10 | 7 | 7 | 0 | 0 | 0 | 0/0/0 | 2135×725 |

La columna **O/P/C** es OVERLAP/PERP/CROSS. La suma de clases por panel es 306 porque una misma clase puede aparecer en varios paneles; las clases únicas son 141.

---

## 4. Validación geométrica

### 4.1 Método

- Renderizado con **PlantUML 1.2026.9**, layout estándar (sin Smetana ni ELK), `linetype` ortho o polyline según el panel.
- El analizador `validacion/analyze_puml_types.py` lee el `.puml` para clasificar cada enlace por tipo y el `.svg` para extraer la geometría de cada camino (`<g class="link">` + `data-source-line`).
- Se comparan todos los pares de segmentos de enlaces distintos, excluyendo:
  - los puntos interiores a las cajas de clase (margen de 2 px),
  - los puntos que coinciden con un extremo de alguno de los dos caminos (uniones, no cruces).
- Clasificación:
  - **OVERLAP**: dos segmentos colineales se solapan más de 1 px.
  - **PERP**: se cortan con un ángulo entre 75° y 105°.
  - **CROSS**: se cortan con cualquier otro ángulo.

### 4.2 Resultado

Los 17 paneles dan **0/0/0**. La salida completa está en `validacion/resultados-validacion.txt` y se resume así:

```text
01-modelo: enlaces=18/18 OVERLAP=0 PERP=0 CROSS=0 TOTAL=0 [OK]
02-dto: enlaces=7/7 OVERLAP=0 PERP=0 CROSS=0 TOTAL=0 [OK]
03-repositorio-persistencia: enlaces=27/27 OVERLAP=0 PERP=0 CROSS=0 TOTAL=0 [OK]
04-mapeador-realizaciones: enlaces=5/5 OVERLAP=0 PERP=0 CROSS=0 TOTAL=0 [OK]
05-mapeador-asociaciones: enlaces=7/7 OVERLAP=0 PERP=0 CROSS=0 TOTAL=0 [OK]
06-servicio-controlador: enlaces=13/13 OVERLAP=0 PERP=0 CROSS=0 TOTAL=0 [OK]
07-vista-config: enlaces=11/11 OVERLAP=0 PERP=0 CROSS=0 TOTAL=0 [OK]
08-composicion-transversal: enlaces=24/24 OVERLAP=0 PERP=0 CROSS=0 TOTAL=0 [OK]
09-agregacion-transversal: enlaces=8/8 OVERLAP=0 PERP=0 CROSS=0 TOTAL=0 [OK]
10-asociaciones-dto-modelo: enlaces=14/14 OVERLAP=0 PERP=0 CROSS=0 TOTAL=0 [OK]
11-asociaciones-servicio-mapeador: enlaces=10/10 OVERLAP=0 PERP=0 CROSS=0 TOTAL=0 [OK]
12-asociaciones-vista-repositorio: enlaces=10/10 OVERLAP=0 PERP=0 CROSS=0 TOTAL=0 [OK]
13-asociaciones-config-servicio-controlador: enlaces=6/6 OVERLAP=0 PERP=0 CROSS=0 TOTAL=0 [OK]
14-asociaciones-vista-web-controlador: enlaces=10/10 OVERLAP=0 PERP=0 CROSS=0 TOTAL=0 [OK]
15-asociaciones-servicio-repositorio-simple: enlaces=5/5 OVERLAP=0 PERP=0 CROSS=0 TOTAL=0 [OK]
16-asociaciones-servicio-repositorio-curso-profesor-plan: enlaces=8/8 OVERLAP=0 PERP=0 CROSS=0 TOTAL=0 [OK]
17-asociaciones-servicio-repositorio-periodo-formacion-programa: enlaces=7/7 OVERLAP=0 PERP=0 CROSS=0 TOTAL=0 [OK]

Paneles validados: 17
Enlaces de los 4 tipos: 190
Paneles con fallo: 0
```

### 4.3 Nota sobre Smetana y ELK

Se probaron también los layouts Smetana y ELK. El análisis geométrico de sus SVG dio cruces reales: 12, 34 y 8 en los tres paneles Smetana, y 9, 22 y 2 en los tres paneles ELK. Por eso se descartaron. Los 17 paneles finales usan el layout estándar de PlantUML, que sí permite validar cada enlace por separado.

---

## 5. Cobertura de las 190 relaciones

### 5.1 Por tipo y alcance

| Tipo | Total | Internas | Cruzadas |
|---|---:|---:|---:|
| Asociación | 123 | 53 | 70 |
| Realización | 34 | 34 | 0 |
| Composición | 24 | 0 | 24 |
| Agregación | 9 | 1 | 8 |
| **Total** | **190** | **88** | **102** |

### 5.2 Matriz de relaciones cruzadas

Filas = módulo de origen, columnas = módulo de destino.

| Origen \ Destino | modelo | dto | repo+pers | mapeador | servicio+controlador | vista+config | Total |
|---|---:|---:|---:|---:|---:|---:|---:|
| modelo | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| dto | 14 | 0 | 0 | 0 | 0 | 0 | 14 |
| repo+pers | 8 | 0 | 0 | 0 | 0 | 0 | 8 |
| mapeador | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| servicio+controlador | 0 | 0 | 20 | 10 | 0 | 0 | 30 |
| vista+config | 0 | 0 | 20 | 0 | 30 | 0 | 50 |
| **Total** | **22** | **0** | **40** | **10** | **30** | **0** | **102** |

Las 102 cruzadas se desglosan por tipo en: 70 asociaciones, 24 composiciones y 8 agregaciones.

### 5.3 Trazabilidad

- `cobertura-relaciones.csv` — una fila por cada una de las 190 relaciones, con línea del fuente, tipo, origen, destino, panel asignado y texto original.
- `relaciones-entre-paneles.csv` — las 102 relaciones cruzadas con su panel.
- `matriz-paneles.csv` — la matriz anterior en formato CSV.
- `planarizacion-4-tipos.csv` — las 162 aristas conservadas y las 28 descartadas por la planarización codiciosa.

---

## 6. Matrícula excluida y archivos conservados

### 6.1 Exclusión

El conjunto `paneles-sin-matricula` no contiene la entidad Matrícula ni sus DTO, repositorio, configuración, enum, campos, getters o relaciones. La comprobación case-insensitive de `matr[ií]cula` sobre los 17 `.puml` y los 17 `.svg` da **0 referencias**. Tampoco hay clases de vista de consola (`AppScannerCli`, `ConfiguracionCli`, `CursoPeriodoVista`, `EstudianteVista`, `ProfesorVista`).

### 6.2 Archivos que no se modificaron

| Archivo | SHA-256 |
|---|---|
| `diagrama-qa-genericos (1).puml` (canónico con Matrícula) | `D97A835D3DC62BA408063C5DBF578F72B0DF8000058BC5A55AE0EDD1C023D96A` |
| `diagrama-qa-genericos (1).svg` (SVG canónico) | `136213F426BDFF4A8BE90E3A8119E96A6888C716594D9C32AEBBCAC837B65DBA` |
| `proyecto.json` (posiciones/rutas manuales) | `BBA61AC9FBF2764913D274A7A4D6AA57D3174C0218FB70DED0C9144736445A73` |
| `flujos/matricula-clases.puml` (diagrama enfocado) | `EE46F95418F48EE1C62D731F0C1F14793BD8CA7D5D56DA696CAD673A348383E6` |
| `flujos/matricula-clases.svg` (diagrama enfocado) | `5A03E5EF5E70B6B12714C8FAA91E1AA44CAC3C6B8E397DA222EC2639212CB18F` |
| `flujos/matricula-clases.png` (diagrama enfocado) | `0758096A73FE5A8CE8D310134036447CA30720CF2A1A0D85167CBEE125BE7632` |

Las fechas de modificación de esos archivos son anteriores al cierre de esta entrega (7/11/2026), lo que confirma que no fueron tocados durante la generación de los paneles.

### 6.3 Alcance de los paneles

Los 17 paneles representan las **190 relaciones de los 4 tipos estructurales exigidos**. El UML fuente completo conserva además **30 generalizaciones** (10 de `ManejadorWeb` a las vistas web, 10 de `RepositorioBase` a los repositorios y 10 de `RepositorioBaseAbstracto` a las implementaciones). Esas generalizaciones no forman parte de la métrica de cruces solicitada y permanecen en `diagrama-qa-genericos-sin-matricula.puml`; los paneles son vistas de los 4 tipos, no una sustitución del fuente.

---

## 7. Reproducción

### 7.1 Renderizado

```powershell
$dir = "C:\Users\dacd1\OneDrive\Documentos\NetBeansProjects\proyecto_clase_poo_2\Diagrama poo\DiagramaPoo\paneles-sin-matricula"
$jar = "C:\Users\dacd1\AppData\Local\Temp\opencode\plantuml-1.2026.9.jar"
Get-ChildItem -LiteralPath $dir -Filter *.puml | ForEach-Object {
    java -Djava.awt.headless=true -DPLANTUML_LIMIT_SIZE=16384 -jar $jar -tsvg -charset UTF-8 $_.FullName
    java -Djava.awt.headless=true -DPLANTUML_LIMIT_SIZE=16384 -jar $jar -tpng -charset UTF-8 $_.FullName
}
```

### 7.2 Validación

```powershell
python "C:\Users\dacd1\OneDrive\Documentos\NetBeansProjects\proyecto_clase_poo_2\Diagrama poo\DiagramaPoo\paneles-sin-matricula\validacion\validar-paneles.py"
```

El script recorre los 17 `.puml` de panel (excluye el combinado `uml-completo.puml`, que es la referencia no planar), ejecuta el analizador y falla si algún panel no tiene 0/0/0 o si el número de enlaces no coincide con las relaciones del `.puml`.

---

## 8. Archivo combinado y proyecto del editor

### 8.1 Archivo combinado para PlantUML

`uml-completo.puml` reúne todo en un solo archivo PlantUML con **18 bloques**:

1. **Bloque 1 — modelo completo en un solo plano.** Es la referencia canónica completa (141 clases, 190 relaciones de los 4 tipos y 30 generalizaciones). Como el grafo es no planar, este bloque puede contener cruces; no se usa para la validación de 0.
2. **Bloques 2–18 — los 17 paneles.** Cada bloque conserva el layout y la validación 0/0/0 del panel individual correspondiente.

Al renderizar:

```powershell
java -Djava.awt.headless=true -DPLANTUML_LIMIT_SIZE=16384 -jar plantuml-1.2026.9.jar -tsvg -charset UTF-8 uml-completo.puml
```

PlantUML genera:

| Salida | Contenido |
|---|---|
| `uml-completo.svg` | modelo completo (referencia) |
| `uml-completo_001.svg` | panel 01 |
| ... | ... |
| `uml-completo_017.svg` | panel 17 |

Los 17 bloques de panel se validaron con el mismo analizador: 0 OVERLAP, 0 PERP, 0 CROSS. El archivo combinado no contiene referencias a Matrícula.

Para regenerar `uml-completo.puml` después de editar cualquier panel o el modelo completo:

```powershell
python validacion\unir-paneles.py
```

El script lee `diagrama-qa-genericos-sin-matricula.puml` y los 17 `.puml` de panel, y reescribe el combinado con los mismos 18 bloques en el mismo orden. La ejecución es idempotente: con las fuentes sin cambios produce un archivo idéntico byte a byte (SHA-256 `2D4380165F3A9082782D718CD0CA5D935C7F0A1259B0A43FCED3C6A45E4B8B4C`).

El bloque 1 usa `nodesep 8` y `ranksep 80` para compactar el plano completo; antes usaba `12`/`260`. Solo se cambiaron esos dos parámetros de espaciado: ninguna clase ni relación se añadió, eliminó o modificó. Los bloques 2–18 conservan exactamente la configuración validada de cada panel.

### 8.2 Proyecto del editor (Visor UML)

El editor **Visor UML** no interpreta varios diagramas por archivo: su lector ignora `@startuml`/`@enduml` y fusiona todos los bloques en un solo modelo. Por eso, al abrir el combinado de 18 bloques, las declaraciones `package edu.unimag` de los paneles sobrescribían el paquete de cada clase y todo terminaba dentro de `edu.unimag`.

Para el editor se usa un archivo distinto, `DiagramaFinal/uml-completo.puml`, que contiene **solo el modelo completo en un bloque** (copia exacta de `../diagrama-qa-genericos-sin-matricula.puml`, SHA-256 `2DD0948BA3EC41DA7F8459A0D82FF310B9BD1E552E9B50C6F61D1C93AD95CAA2`). Así el editor muestra los **30 paquetes individuales** del modelo, sin modificar el `.puml` combinado.

`DiagramaFinal/proyecto.json` guarda las **141 posiciones** calculadas con el layout propio del editor (`LayeredLayout`) y los tamaños de caja medidos en Chrome: 30 marcos de paquete, 0 solapamientos de clases y 0 solapamientos de marcos. La vista queda en el valor por defecto `{zoom: 1, x: 0, y: 0}`, que el editor interpreta como «mostrar todo». SHA-256 del estado: `57154563F36033621D31A6DD0538587D144035C0B17CE55A2B1EDD66B8B20AAC`.

Verificación con el lector del editor:

| Archivo | Clases | Ids únicos | Relaciones | Paquetes | `edu.unimag` |
|---|---|---|---|---|---|
| `DiagramaFinal/uml-completo.puml` | 141 | 141 | 533 | 30 | 1 (AppWeb) |
| `uml-completo.puml` (combinado) | 447 | 141 | 723 | 1 | 141 |

El combinado se conserva para generar los 18 SVG con PlantUML; para ver el diagrama en el editor hay que abrir el proyecto `DiagramaFinal`.

---

## 9. Contenido de la carpeta

```text
paneles-sin-matricula/
├── 01-modelo.puml / .svg / .png
├── ...
├── 17-asociaciones-servicio-repositorio-periodo-formacion-programa.puml / .svg / .png
├── uml-completo.puml
├── uml-completo.svg
├── uml-completo_001.svg ... uml-completo_017.svg
├── metricas-paneles.csv
├── cobertura-relaciones.csv
├── relaciones-entre-paneles.csv
├── matriz-paneles.csv
├── planarizacion-4-tipos.csv
├── README.md
├── DiagramaFinal/
│   ├── proyecto.json
│   ├── uml-completo.puml (modelo completo, un solo bloque)
│   └── anteriores/
└── validacion/
    ├── analyze_puml_types.py
    ├── validar-paneles.py
    ├── unir-paneles.py
    └── resultados-validacion.txt
```
