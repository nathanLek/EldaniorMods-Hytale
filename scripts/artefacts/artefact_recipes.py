"""Recettes des artefacts : tous se fabriquent a la Forge Divine avec du Lingot Divin (voir scripts/minerai/gen_divin.py).

A relancer apres tout script qui reecrit un Artefact_*.json (weapon_catalog.py, gen_katanas_jumeaux.py...) : il pose la recette
sur chaque artefact et peut etre execute autant de fois qu'on veut. La recette heritee d'un template est remplacee.

Le cout depend de la qualite de l'artefact. Onglet de la forge selon le dossier de l'arme.

Usage : python3 scripts/artefacts/artefact_recipes.py <src/main/resources>
"""
import glob, json, os, sys

RES = sys.argv[1]
COSTS = {   # qualite -> (lingots divins, lingots d'adamantite, secondes)
    "Unique": (6, 10, 10),
    "Legendary": (12, 20, 20),
    "Divin": (24, 40, 30),
}
TABS = {"Sword": "Forge_Epees", "Longsword": "Forge_Epees", "Spear": "Forge_Lances", "Daggers": "Forge_Dagues",
        "Axe": "Forge_Haches", "Battleaxe": "Forge_Haches", "Mace": "Forge_Masses", "Club": "Forge_Masses",
        "Shortbow": "Forge_Arcs", "Handgun": "Forge_Arcs", "Staff": "Forge_Batons", "Wand": "Forge_Batons", "Shield": "Forge_Boucliers"}

for path in sorted(glob.glob(os.path.join(RES, "Server", "Item", "Items", "Weapon", "*", "Artefact_*.json"))):
    with open(path, encoding="utf-8") as f:
        item = json.load(f)
    family = os.path.basename(os.path.dirname(path))
    divine, adamantite, seconds = COSTS[item["Quality"]]
    item["Recipe"] = {
        "TimeSeconds": seconds,
        "Input": [{"ItemId": "Ingredient_Bar_Divin", "Quantity": divine}, {"ItemId": "Ingredient_Bar_Adamantite", "Quantity": adamantite}],
        "BenchRequirement": [{"Type": "Crafting", "Id": "Forge_Divine", "Categories": [TABS[family]]}],
    }
    with open(path, "w", encoding="utf-8") as f:
        json.dump(item, f, indent=2, ensure_ascii=False)
        f.write("\n")
    print("%-34s %-10s %3d divins + %3d adamantite  %s" % (os.path.basename(path)[:-5], item["Quality"], divine, adamantite, TABS[family]))
