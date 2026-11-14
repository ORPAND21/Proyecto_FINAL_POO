#!/usr/bin/env python3
"""Valida los 17 paneles: 0 OVERLAP/PERP/CROSS y cobertura exacta del UML fuente.

Uso:
    python validar-paneles.py

Requiere Python 3 y PlantUML ya renderizado (los .svg deben existir junto a los .puml).
"""
import os
import re
import subprocess
import sys

BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ANALYZER = os.path.join(os.path.dirname(os.path.abspath(__file__)), "analyze_puml_types.py")


def classify(line):
    s = line.strip()
    if not s or s.startswith("'") or s.startswith("@"):
        return None
    if "<|--" in s:
        return "generalizacion"
    if "<|.." in s:
        return "realizacion"
    if "*-->" in s:
        return "composicion"
    if "o-->" in s:
        return "agregacion"
    if ".[#" in s and "].>" in s:
        return "dependencia"
    if "-->" in s:
        return "asociacion"
    if "..>" in s:
        return "dependencia"
    return None


def count_relations(path):
    n = 0
    for line in open(path, encoding="utf-8"):
        if classify(line) in ("asociacion", "composicion", "agregacion", "realizacion"):
            n += 1
    return n


def main():
    # El combinado uml-completo.puml es la referencia de un solo plano: el grafo
    # completo no es planar, asi que no se valida contra 0/0/0.
    panels = sorted(
        f[:-5] for f in os.listdir(BASE)
        if f.endswith(".puml") and f != "uml-completo.puml"
    )
    if not panels:
        sys.exit("No se encontraron paneles .puml en " + BASE)

    out = []
    failures = 0
    total_links = 0
    for name in panels:
        puml = os.path.join(BASE, name + ".puml")
        svg = os.path.join(BASE, name + ".svg")
        if not os.path.exists(svg):
            out.append(f"{name}: FALTA SVG")
            failures += 1
            continue
        expected = count_relations(puml)
        proc = subprocess.run(
            [sys.executable, ANALYZER, puml, svg, "4"],
            capture_output=True,
            text=True,
            timeout=600,
        )
        vals = {}
        links = 0
        for line in proc.stdout.splitlines():
            m = re.match(r"Links 4 tipos: (\d+)", line)
            if m:
                links = int(m.group(1))
            m = re.match(r"(OVERLAP|PERP|CROSS|TOTAL) (\d+)", line)
            if m:
                vals[m.group(1)] = int(m.group(2))
        total_links += links
        status = "OK" if links == expected and vals.get("TOTAL") == 0 else "FALLO"
        if status == "FALLO":
            failures += 1
        out.append(
            f"{name}: enlaces={links}/{expected} "
            f"OVERLAP={vals.get('OVERLAP')} PERP={vals.get('PERP')} "
            f"CROSS={vals.get('CROSS')} TOTAL={vals.get('TOTAL')} [{status}]"
        )

    out.append("")
    out.append(f"Paneles validados: {len(panels)}")
    out.append(f"Enlaces de los 4 tipos: {total_links}")
    out.append(f"Paneles con fallo: {failures}")
    text = "\n".join(out)
    print(text)

    dst = os.path.join(os.path.dirname(os.path.abspath(__file__)), "resultados-validacion.txt")
    with open(dst, "w", encoding="utf-8", newline="\n") as f:
        f.write(text + "\n")
    sys.exit(1 if failures else 0)


if __name__ == "__main__":
    main()
