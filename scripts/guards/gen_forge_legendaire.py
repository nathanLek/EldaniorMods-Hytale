"""Forges des trois forgerons legendaires (guards/LegendarySmiths) : Eldanior_Forge_Legendaire_<Humain|Nain|Elfe>.

Les seuls PNJ qui forgent le fer divin. Meme catalogue pour les trois (meme niveau), stocks propres a chacun :
  - tout ce que forge la Forge Divine (recettes BenchRequirement Forge_Divine), sauf les tresors de famille (FAMILY) et le
    bloc Metal_Divin ; les armures divines de classe (Armor_Divin_<Classe>_<Piece>) ; les trois mousquets legendaires ;
    les munitions divines par lots (balles divines et de sniper par 10, obus par 2) ;
  - le prix : les materiaux de la recette (ou MATERIALS pour ce qui n'en a pas) + des pieces, tres cher
    (PRICE_FACTOR x le bareme des boutiques, gen_marchand_luo.price) ;
  - une piece de chaque, reforgee chaque semaine (RefreshInterval 7 jours) ;
  - kits de reparation (Goblin : sans perte, Rare), toujours en rayon.
La Forge Divine reste utilisable par les joueurs qui en ont appris la competence (decision du 2026-11-09).

Usage : python3 scripts/guards/gen_forge_legendaire.py <src/main/resources>
"""
import glob, json, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import gen_marchand_luo as g  # noqa: E402

RES = g.RES
SMITHS = ("Humain",)                       # 2026-11-16 : un seul forgeron legendaire (trois noms), une seule forge
FAMILY = ("Artefact_Sword_Ryukiri", "Artefact_Sword_Tenryumaru", "Artefact_Sword_Kinryu")   # tresors de famille : jamais
PRICE_FACTOR = 2.0
BAR, ADAM = "Ingredient_Bar_Divin", "Ingredient_Bar_Adamantite"
PIECE_MATERIALS = {"Chest": (16, 24), "Head": (10, 16), "Legs": (12, 20), "Hands": (8, 12)}
# armes a feu (2026-11-15) : seulement les mousquets legendaires ; les Colts, le SARS et le canon sont leurs transformations
MATERIALS = {"Artefact_Mousquet_BlackBess": [(BAR, 12), (ADAM, 20)], "Artefact_Mousquet_GreenColt": [(BAR, 12), (ADAM, 20)],
             "Artefact_Mousquet_BlueTiger": [(BAR, 12), (ADAM, 20)]}
AMMOS = [("Munition_Balle_Divine", 10), ("Munition_Balle_Sniper_Divine", 10), ("Munition_Obus_Divin", 2)]   # (objet, taille du lot)
AMMO = AMMOS[0]
REPAIR = [("Tool_Repair_Kit_Goblin", 5), ("Tool_Repair_Kit_Rare", 5)]


def coins(value):
    """Valeur en cuivres -> une pile de la plus grosse piece qui reste au-dessus de 2 (comme gen_marchand_luo.price)."""
    for coin, worth in g.COINS:
        q = round(value / worth)
        if q >= 2 or worth == 1:
            return {"ItemId": coin, "Quantity": min(999, max(1, q))}


def coin_value(iid, factor=1.0):
    p = g.price(iid, factor)[0]
    return dict(g.COINS)[p["ItemId"]] * p["Quantity"]


def forge_recipes():
    out = {}
    for f in glob.glob(os.path.join(RES, "Server/Item/Items/**/*.json"), recursive=True):
        with open(f, encoding="utf-8") as fh:
            d = json.load(fh)
        r = d.get("Recipe") or {}
        if not any(b.get("Id") == "Forge_Divine" for b in r.get("BenchRequirement", [])):
            continue
        iid = os.path.basename(f)[:-5]
        if iid in FAMILY or iid.startswith(("Metal_", "Munition_")):
            continue
        out[iid] = [(i["ItemId"], i["Quantity"]) for i in r.get("Input", []) if i.get("ItemId")]
    return out


def catalog():
    items = forge_recipes()
    for f in sorted(glob.glob(os.path.join(RES, "Server/Item/Items/**/Armor_Divin_*.json"), recursive=True)):
        iid = os.path.basename(f)[:-5]
        piece = iid.rsplit("_", 1)[-1]
        if piece in PIECE_MATERIALS:
            b, a = PIECE_MATERIALS[piece]
            items[iid] = [(BAR, b), (ADAM, a)]
    items.update(MATERIALS)
    return dict(sorted(items.items()))


def trade(iid, mats, qty=1, stock=1, factor=PRICE_FACTOR):
    value = coin_value(iid, factor) * qty
    return {"Output": {"ItemId": iid, "Quantity": qty},
            "Input": [{"ItemId": m, "Quantity": n * qty} for m, n in mats] + [coins(value)], "Stock": stock}


def main():
    items = catalog()
    slots = [{"Type": "Fixed", "Trade": trade(iid, mats)} for iid, mats in items.items()]
    for ammo, lot in AMMOS:
        slots.append({"Type": "Fixed", "Trade": trade(ammo, forge_recipes_ammo(ammo), qty=lot, stock=5, factor=0.25)})
    slots += [g.fixed(iid, 1, n) for iid, n in REPAIR]
    for smith in SMITHS:
        g.write("Server/BarterShops/Eldanior_Forge_Legendaire_%s.json" % smith,
                g.shop("Forge légendaire", 7, slots))
    print("Forge legendaire : %d objets (+ balles divines, kits de reparation), 3 boutiques" % len(items))
    for iid, mats in list(items.items())[:6]:
        t = trade(iid, mats)
        print("  %-38s %s" % (iid, ", ".join("%d %s" % (i["Quantity"], i["ItemId"]) for i in t["Input"])))


def forge_recipes_ammo(ammo):
    with open(glob.glob(os.path.join(RES, "Server/Item/Items/**/%s.json" % ammo), recursive=True)[0], encoding="utf-8") as f:
        r = json.load(f).get("Recipe") or {}
    return [(i["ItemId"], i["Quantity"]) for i in r.get("Input", []) if i.get("ItemId")]


if __name__ == "__main__":
    main()
