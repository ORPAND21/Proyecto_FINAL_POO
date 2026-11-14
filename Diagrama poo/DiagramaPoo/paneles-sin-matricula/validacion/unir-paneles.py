#!/usr/bin/env python3
"""Regenera uml-completo.puml a partir del modelo completo y de los 17 paneles.

Uso:
    python unir-paneles.py

El archivo generado contiene 18 bloques PlantUML:
  1) modelo completo en un solo plano (referencia)
  2) ... 18) los 17 paneles, en orden 01-17
"""
import os

VALIDACION_DIR = os.path.dirname(os.path.abspath(__file__))
PANEL_DIR = os.path.dirname(VALIDACION_DIR)
ROOT = os.path.dirname(PANEL_DIR)
FULL = os.path.join(ROOT, "diagrama-qa-genericos-sin-matricula.puml")
OUT = os.path.join(PANEL_DIR, "uml-completo.puml")

PANELS = [
    ("01-modelo", "Panel 01 - Modelo"),
    ("02-dto", "Panel 02 - DTO"),
    ("03-repositorio-persistencia", "Panel 03 - Repositorio + persistencia"),
    ("04-mapeador-realizaciones", "Panel 04 - Mapeador (realizaciones)"),
    ("05-mapeador-asociaciones", "Panel 05 - Mapeador (asociaciones)"),
    ("06-servicio-controlador", "Panel 06 - Servicio + controlador"),
    ("07-vista-config", "Panel 07 - Vista + configuracion"),
    ("08-composicion-transversal", "Panel 08 - Composicion transversal"),
    ("09-agregacion-transversal", "Panel 09 - Agregacion transversal"),
    ("10-asociaciones-dto-modelo", "Panel 10 - Asociaciones DTO -> modelo"),
    ("11-asociaciones-servicio-mapeador", "Panel 11 - Asociaciones servicio -> mapeador"),
    ("12-asociaciones-vista-repositorio", "Panel 12 - Asociaciones configuracion de modulo -> repositorio"),
    ("13-asociaciones-config-servicio-controlador", "Panel 13 - Asociaciones configuracion -> servicio/controlador"),
    ("14-asociaciones-vista-web-controlador", "Panel 14 - Asociaciones vista web -> controlador"),
    ("15-asociaciones-servicio-repositorio-simple", "Panel 15 - Asociaciones servicio -> repositorio (simples)"),
    ("16-asociaciones-servicio-repositorio-curso-profesor-plan", "Panel 16 - Asociaciones servicio -> repositorio (CursoProfesor/PlanEstudio)"),
    ("17-asociaciones-servicio-repositorio-periodo-formacion-programa", "Panel 17 - Asociaciones servicio -> repositorio (CursoPeriodo/FormacionProfesor/Programa)"),
]


def read_block(path):
    text = open(path, encoding="utf-8").read().strip()
    if not text.startswith("@startuml") or not text.endswith("@enduml"):
        raise SystemExit("Bloque PlantUML invalido: " + path)
    return text


def main():
    parts = []
    parts.append("' " + "=" * 78)
    parts.append("' UML COMPLETO - ARCHIVO UNICO CON TODOS LOS BLOQUES")
    parts.append("' " + "=" * 78)
    parts.append("'")
    parts.append("' Este archivo contiene 18 diagramas PlantUML independientes:")
    parts.append("'   Diagrama 1  : modelo completo en un solo plano (referencia; el grafo")
    parts.append("'                 es no planar, por lo que puede contener cruces).")
    parts.append("'   Diagramas 2-18: los 17 paneles, cada uno con 0 OVERLAP/PERP/CROSS")
    parts.append("'                 en asociacion, agregacion, realizacion y composicion.")
    parts.append("'")
    parts.append("' Al renderizar con PlantUML se generan 18 salidas:")
    parts.append("'   uml-completo.svg      -> modelo completo")
    parts.append("'   uml-completo_001.svg  -> panel 01")
    parts.append("'   ...")
    parts.append("'   uml-completo_017.svg  -> panel 17")
    parts.append("'")
    parts.append("' Los .puml individuales de cada panel siguen existiendo en esta carpeta.")
    parts.append("' " + "=" * 78)
    parts.append("")

    parts.append("' " + "=" * 78)
    parts.append("' DIAGRAMA 1 - MODELO COMPLETO EN UN SOLO PLANO (REFERENCIA)")
    parts.append("' " + "=" * 78)
    parts.append(read_block(FULL))
    parts.append("")

    for idx, (name, label) in enumerate(PANELS, start=2):
        path = os.path.join(PANEL_DIR, name + ".puml")
        parts.append("' " + "=" * 78)
        parts.append(f"' DIAGRAMA {idx} - {label}")
        parts.append("' " + "=" * 78)
        parts.append(read_block(path))
        parts.append("")

    with open(OUT, "w", encoding="utf-8", newline="\n") as f:
        f.write("\n".join(parts))

    print("Escrito:", OUT)
    print("Lineas:", len(open(OUT, encoding="utf-8").read().splitlines()))
    print("Bloques @startuml:", open(OUT, encoding="utf-8").read().count("@startuml"))


if __name__ == "__main__":
    main()
