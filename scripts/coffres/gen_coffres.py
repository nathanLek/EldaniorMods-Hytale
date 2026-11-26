#!/usr/bin/env python3
"""Genere les coffres au tresor (src/main/resources/eldanior/coffres.json) depuis conception.py et le catalogue du mod.

Catalogue = objets de economie.json (familles d'economie) + objets du mod ; qualite lue dans les JSON des objets (mod et jeu
de base : ~/Desktop/Assets Hytale). Chaque objet recoit une famille de coffre et un rang ; chaque coffre recoit les objets
de ses familles, dans les bornes de rang de chaque famille. Nettoie aussi LootTableConfig.java (objets inexistants, de
developpeur, doublons) pour les coffres d'or et a cle de boss qui l'utilisent encore.

Usage : python3 scripts/coffres/gen_coffres.py [--check]
Relancer apres un changement de conception.py, de nouveaux objets (runes, butins, armes...) ou gen_economie.py.
"""
import collections
import glob
import json
import os
import re
import sys

sys.path.insert(0, os.path.dirname(__file__))
from conception import COFFRES, PIECES, QUANTITES, QUALITES_EXCLUES, CATEGORIES_EXCLUES, MOTS_EXCLUS  # noqa: E402

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
RES = os.path.join(ROOT, "src/main/resources")
LOOT = os.path.join(ROOT, "src/main/java/com/eldanior/system/config/configs/LootTableConfig.java")
SKILLS = os.path.join(ROOT, "src/main/java/com/eldanior/system/skills/SkillManager.java")
VANILLA = os.path.expanduser("~/Desktop/Assets Hytale/Server/Item/Items")
OUT = os.path.join(RES, "eldanior/coffres.json")
CHECK = "--check" in sys.argv
RANG = {"Common": 1, "Uncommon": 2, "Rare": 3, "Epic": 4, "Unique": 5, "Legendary": 6, "Legendaire": 6, "Divin": 7}
LBL = {1: "Commun", 2: "Inhabituel", 3: "Rare", 4: "Épique", 5: "Unique", 6: "Légendaire", 7: "Divin"}

if not os.path.isdir(VANILLA):
    sys.exit("Assets du jeu introuvables (" + VANILLA + ") : impossible de lire la qualite des objets vanilla.")

# ------------------------------------------------------------------ objets : qualite et categories
info = {}   # id -> (qualite, categories, du mod ?)
for base, is_mod in ((os.path.join(RES, "Server/Item/Items"), True), (VANILLA, False)):
    for f in glob.glob(os.path.join(base, "**/*.json"), recursive=True):
        iid = os.path.basename(f)[:-5]
        if iid in info:
            continue
        try:
            d = json.load(open(f, encoding="utf-8"))
        except Exception:
            d = {}
        info[iid] = (d.get("Quality") or "Common", d.get("Categories") or [], is_mod)

eco = json.load(open(os.path.join(RES, "eldanior/economie.json"), encoding="utf-8"))
eco_fam = {iid: eco["familles"][v["f"]] for iid, v in eco["items"].items() if "f" in v}

pages = {}  # page de reference -> rang
for m in re.finditer(r'page\("([^"]+)", "[A-Z_]+", (\d), "[^"]*", true\)', open(SKILLS, encoding="utf-8").read()):
    pages[m.group(1)] = int(m.group(2))


def famille_de(iid):
    """Famille de coffre d'un objet, ou None s'il ne va dans aucun coffre."""
    q, cats, is_mod = info.get(iid, ("Common", [], False))
    if q in QUALITES_EXCLUES or any(w in iid for w in MOTS_EXCLUS):
        return None
    if any(c.startswith(CATEGORIES_EXCLUES) for c in cats):
        return None
    ef = eco_fam.get(iid)
    if iid.startswith("skill_page_"):
        return "page" if iid in pages else None
    if iid.startswith("Eldanior_Parchemin_Metier_"):
        return "metier"
    if iid.startswith("Eldanior_Manuel_Magie_"):
        return "manuel"
    if iid.startswith("Butin_"):
        return "butin"
    if iid.startswith("Artefact_"):
        return "artefact" if any(c == "Eldanior_System.Artefacts" for c in cats) else None
    if iid.startswith(("Weapon_", "Eldanior_Weapon_")):
        if is_mod and not cats:
            return None                                    # forme temporaire, objet de travail
        return "arme" if ef == "Armes" else None
    if iid.startswith("Armor_"):
        if is_mod and not cats:
            return None
        return "armure" if ef == "Armures" else None
    if iid.startswith(("Potion_", "Eld_Potion_")):
        return "potion"
    if iid.startswith("Consomable_Stat_"):
        return "pilule"
    if iid.startswith(("Elixir_", "Tome_Niveau", "Tome_Experience", "Essence_")):
        return "rare"
    if iid.startswith(("Ore_", "Ingredient_Bar_")):
        return "minerai"
    if iid.startswith(("Food_", "Fish_")):
        return "nourriture"
    if iid.startswith("Ingredient_") or re.match(r"Plant_(Crop_.*_Item|Fruit_[A-Za-z]+$|Crop_(Health|Mana|Stamina)\d?$)", iid):
        return "ressource"
    return None


def rang_de(iid):
    if iid in pages:
        return pages[iid]
    return RANG.get(info.get(iid, ("Common",))[0], 1)


catalogue = collections.defaultdict(list)   # famille -> [(id, rang)]
for iid in sorted(set(eco_fam) | {k for k, v in info.items() if v[2]}):
    if iid not in info:
        continue
    fam = famille_de(iid)
    if fam:
        catalogue[fam].append((iid, rang_de(iid)))
for r, (coin, mn, mx) in PIECES.items():
    catalogue["pieces"].append((coin, r))
pieces_qte = {r: (mn, mx) for r, (coin, mn, mx) in PIECES.items()}

# ------------------------------------------------------------------ coffres.json
out_coffres = {}
couverts = set()
rapport = []
for cid, c in COFFRES.items():
    bornes = c.get("bornes", {})
    objets = []
    for fam in c["familles"]:
        lo, hi = bornes.get(fam, (1, 7))
        for iid, r in catalogue.get(fam, []):
            if lo <= r <= hi and r in c["rangs"]:
                if fam == "pieces":
                    mn, mx = pieces_qte[r]
                else:
                    mn, mx = QUANTITES.get(fam, (1, 1))
                objets.append([iid, fam, r, mn, mx])
                couverts.add(iid)
    out_coffres[cid] = {"nom": c["nom"], "recharge": c["recharge"], "second": c["second"], "familles": c["familles"],
                        "rangs": {str(k): v for k, v in c["rangs"].items()}, "objets": objets}
    par_fam = collections.Counter(o[1] for o in objets)
    vides = [f for f in c["familles"] if par_fam[f] == 0]
    rapport.append(f"  {cid:14} {len(objets):4} objets  " + ", ".join(f"{f} {n}" for f, n in par_fam.most_common())
                   + (f"   FAMILLES VIDES : {vides}" if vides else ""))

out = {"_lisez_moi": "Genere par scripts/coffres/gen_coffres.py depuis scripts/coffres/conception.py : ne pas modifier.",
       "coffres": out_coffres}
if not CHECK:
    open(OUT, "w", encoding="utf-8").write(json.dumps(out, ensure_ascii=False, separators=(",", ":")) + "\n")
print("coffres.json :", len(COFFRES), "coffres")
print("\n".join(rapport))
hors = {f: [i for i, r in l if i not in couverts] for f, l in catalogue.items()}
print("objets du catalogue dans aucun coffre :", {f: len(l) for f, l in hors.items() if l})

# ------------------------------------------------------------------ nettoyage de LootTableConfig (or, cles de boss)
src = open(LOOT, encoding="utf-8").read()
removed = {"inexistant": set(), "exclu": set(), "doublon": 0}
parts = re.split(r"(\n    (?=[A-Z_]+\(\"[a-z_0-9]+\", \d+,))", src)
new_parts = []
for part in parts:
    seen = set()

    def fix(m):
        iid = m.group(2)
        if iid not in info:
            removed["inexistant"].add(iid)
            return ""
        if info[iid][0] in QUALITES_EXCLUES:
            removed["exclu"].add(iid)
            return ""
        if iid in seen:
            removed["doublon"] += 1
            return ""
        seen.add(iid)
        return m.group(0)

    new_parts.append(re.sub(r'(\n[ \t]*new LootEntry\("([^"]+)"[^\n]*)', fix, part))
new = "".join(new_parts)
new = re.sub(r"\),(\s*\n\s*\),)", r")\1", new)
new = re.sub(r"\),(\s*\n\s*\);)", r")\1", new)
if new != src and not CHECK:
    open(LOOT, "w", encoding="utf-8").write(new)
print("LootTableConfig :", len(removed["inexistant"]), "inexistants,", len(removed["exclu"]), "exclus,", removed["doublon"], "doublons retires")
