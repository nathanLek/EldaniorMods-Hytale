"""Butins de monstres utilisables comme attendus de quete (Kind.ITEM), tries par niveau, avec le rang de quete conseille.

Source : scripts/butin/catalog.py (meme calcul d'identifiant que gen_butin.slug). Chaque item est verifie dans
src/main/resources/Server/Item/Items/Butin/ (« ABSENT » = pas encore genere, ne pas l'utiliser).

Usage :
  python3 scripts/quetes/butins_quete.py              tout le catalogue
  python3 scripts/quetes/butins_quete.py C            familles du rang C (bande de niveaux du skill quest-creation)
  python3 scripts/quetes/butins_quete.py trork        familles dont le nom ou le slug contient « trork »
"""
import os, re, sys, unicodedata

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..")
sys.path.insert(0, os.path.join(ROOT, "scripts", "butin"))
from catalog import FAMILIES  # noqa: E402

ITEMS = os.path.join(ROOT, "src", "main", "resources", "Server", "Item", "Items", "Butin")

# Rang de quete -> niveaux de monstre (a garder identique a .claude/skills/quest-creation/SKILL.md)
BANDS = {"F": (0, 60), "E": (60, 150), "D": (150, 300), "C": (300, 450), "B": (450, 600), "A": (600, 800), "S": (800, 9999)}
RARITY = ["commun", "rare", "precieux", "trophee"]


def slug(name):
    t = unicodedata.normalize("NFD", name).encode("ascii", "ignore").decode()
    words = [w for w in re.split(r"[^A-Za-z0-9]+", t.replace("d'", " ")) if w and w.lower() not in ("de", "du", "des", "la", "le", "a", "d")]
    return "Butin_" + "_".join(w[0].upper() + w[1:] for w in words)


def rank_of(lvl):
    for r, (a, b) in BANDS.items():
        if a <= lvl < b: return r
    return "S"


def main():
    arg = sys.argv[1] if len(sys.argv) > 1 else None
    for f in sorted(FAMILIES, key=lambda f: f["lvl"]):
        r = rank_of(f["lvl"])
        if arg and arg.upper() in BANDS and r != arg.upper(): continue
        if arg and arg.upper() not in BANDS and arg.lower() not in (f["nom"] + f["slug"]).lower(): continue
        print(f"[{r}] niv {f['lvl']:>3}  {f['nom']}{' (BOSS)' if f['boss'] else ''}  -- monstres : {f['regex']}")
        for i, it in enumerate(f["items"]):
            if it[1] == "existing":
                print(f"        {RARITY[min(i, 3)]:<9} {it[0]} (objet du jeu)")
                continue
            iid = slug(it[0])
            ok = os.path.exists(os.path.join(ITEMS, iid + ".json"))
            print(f"        {RARITY[min(i, 3)]:<9} {iid:<34} {it[0]}{'' if ok else '  ABSENT'}")


if __name__ == "__main__":
    main()
