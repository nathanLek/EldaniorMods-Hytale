#!/usr/bin/env python3
"""Index des effets de statut du mod (Server/Entity/Effects) pour le panneau admin : src/main/resources/eldanior/effets.json.

Un effet = son identifiant (nom du fichier), son dossier, sa duree, s'il est negatif (Debuff) ou infini, et son commentaire.
Relancer apres l'ajout d'un effet : python3 scripts/passifs/gen_effets_index.py
"""
import json
import os
import re

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
BASE = os.path.join(ROOT, "src/main/resources/Server/Entity/Effects")
OUT = os.path.join(ROOT, "src/main/resources/eldanior/effets.json")

dossiers = {}
for dp, _, fs in os.walk(BASE):
    for f in sorted(fs):
        if not f.endswith(".json"):
            continue
        rel = os.path.relpath(dp, BASE).replace(os.sep, "/")
        try:
            data = json.loads(re.sub(r"^﻿", "", open(os.path.join(dp, f), encoding="utf-8").read()))
        except Exception:
            data = {}
        dossiers.setdefault(rel, []).append({
            "id": f[:-5],
            "duree": data.get("Duration", 0),
            "negatif": bool(data.get("Debuff", False)),
            "infini": bool(data.get("Infinite", False)),
            "note": str(data.get("$Comment", ""))[:160],
        })
out = {"_lisez_moi": "Genere par scripts/passifs/gen_effets_index.py : ne pas modifier.",
       "dossiers": dict(sorted(dossiers.items()))}
open(OUT, "w").write(json.dumps(out, ensure_ascii=False, indent=1) + "\n")
print(sum(len(v) for v in dossiers.values()), "effets dans", len(dossiers), "dossiers ->", os.path.relpath(OUT, ROOT))
