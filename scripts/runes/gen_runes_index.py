#!/usr/bin/env python3
"""Index des runes pour le panneau admin (statistiques des competences actives) : src/main/resources/eldanior/runes.json.

Tire de scripts/runes/specs/*.json : identifiant, sort du moteur (champ Spell), nom, type, rarete, classes.
Relancer apres une nouvelle rune (rune_tool.py new) : python3 scripts/runes/gen_runes_index.py
"""
import glob
import json
import os

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
OUT = os.path.join(ROOT, "src/main/resources/eldanior/runes.json")

runes = []
for f in sorted(glob.glob(os.path.join(ROOT, "scripts/runes/specs/*.json"))):
    s = json.load(open(f, encoding="utf-8"))
    runes.append({"id": s.get("id"), "spell": s.get("spell") or "", "nom": s.get("name") or s.get("id"),
                  "type": s.get("type") or "?", "rarete": s.get("rarity") or "?", "classes": s.get("classes") or ""})
open(OUT, "w", encoding="utf-8").write(json.dumps({"_lisez_moi": "Genere par scripts/runes/gen_runes_index.py : ne pas modifier.",
                                                   "runes": runes}, ensure_ascii=False, indent=1) + "\n")
print(len(runes), "runes ->", os.path.relpath(OUT, ROOT))
