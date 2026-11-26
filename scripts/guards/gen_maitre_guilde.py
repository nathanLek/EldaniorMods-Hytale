"""Comptoir du Maitre de guilde (GuardType.MAITRE_GUILDE, 2026-11-25) : BarterShops/Eldanior_Maitre_Guilde.json.

Le maitre de guilde vend les fournitures des aventuriers : parchemin de relance, failles scellees (F, E, D), cle du donjon
d'entrainement, pilules de caracteristiques (+1 et +5 ; les +10 restent des trouvailles), tome d'experience.
Il rachete tout ce que les marchands rachetent, et les butins de monstres au double du bareme (Java : LuoMerchants.GUILD_*,
ShopPage.pricing) : rien a decrire ici pour le rachat.

Prix : ceux de scripts/economie/prix.json (fichier central, en cuivres), payes avec la plus grosse piece qui tombe juste.
Garde-fou : un article ne peut pas couter moins que ce qu'un marchand en rendrait (bareme ShopPage.buyback, x 1,5), sinon on
pourrait l'acheter ici et le revendre plus cher ailleurs. Le script refuse d'ecrire la boutique dans ce cas.

Usage : python3 scripts/guards/gen_maitre_guilde.py [<src/main/resources>]
Apres un changement de prix dans prix.json : relancer aussi scripts/economie/gen_economie.py.
"""
import glob
import json
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "..", "..", "src", "main", "resources")
PRIX = os.path.join(HERE, "..", "economie", "prix.json")

# (objet, stock par reassort) ; reassort chaque jour a 6 h
ARTICLES = [
    ("Parchemin_Relance", 1),
    ("Eldanior_Faille_Scellee_F", 3),
    ("Eldanior_Faille_Scellee_E", 2),
    ("Eldanior_Faille_Scellee_D", 1),
    ("PortalKey_Dungeon_V1", 3),
    ("Tome_Experience", 3),
] + [(f"Consomable_Stat_{s}_One", 3) for s in ("Force", "Vitalite", "Endurance", "Agilite", "Intelligence", "Chance")] \
  + [(f"Consomable_Stat_{s}_Five", 1) for s in ("Force", "Vitalite", "Endurance", "Agilite", "Intelligence", "Chance")]

COINS = [("Elda_Imperial_Coins", 1_000_000), ("Elda_Astral_Coins", 100_000), ("Elda_Zenith_Coins", 10_000),
         ("Elda_Diamond_Coins", 1_000), ("Elda_Gold_Coins", 100), ("Elda_Silver_Coins", 10), ("Elda_Copper_Coins", 1)]

# niveau suppose par qualite quand l'objet n'a pas d'ItemLevel (ShopPage.buyback)
QUALITY_LEVEL = {"Uncommon": 25, "Rare": 80, "Epic": 180, "Unique": 350, "Legendary": 500}


def prices():
    d = json.load(open(PRIX, encoding="utf-8"))
    flat = {}
    for fam in d.get("ressources", {}).values():
        if isinstance(fam, dict):
            flat.update(fam)
    flat.update(d.get("forces", {}))
    return flat


def item_json(iid):
    hits = glob.glob(os.path.join(RES, "Server", "Item", "**", iid + ".json"), recursive=True)
    if not hits:
        sys.exit(f"objet introuvable : {iid}")
    return json.load(open(hits[0], encoding="utf-8"))


def buyback(item):
    """ShopPage.buyback pour un objet hors equipement (consommables, cles, failles)."""
    level = item.get("ItemLevel") or 0
    if level <= 0:
        level = QUALITY_LEVEL.get(item.get("Quality"), 10)
    return max(1, round(40 * level ** 1.3 * 0.08 * 0.25))


def coin(price):
    """Plus grosse piece qui tombe juste (12 000 -> 12 diamants)."""
    for cid, v in COINS:
        if price >= v and price % v == 0:
            return cid, price // v
    raise AssertionError(price)


def main():
    flat = prices()
    slots, errors = [], []
    for iid, stock in ARTICLES:
        price = flat.get(iid)
        if not price:
            errors.append(f"{iid} : pas de prix dans prix.json")
            continue
        floor = round(buyback(item_json(iid)) * 1.5)
        if price < floor:
            errors.append(f"{iid} : {price} cuivres < {floor} (rachat des marchands x 1,5)")
            continue
        cid, qty = coin(price)
        slots.append({"Type": "Fixed", "Trade": {"Output": {"ItemId": iid, "Quantity": 1},
                                                  "Input": [{"ItemId": cid, "Quantity": qty}], "Stock": stock}})
        print(f"  {iid:32} {price:>7} cuivres = {qty} {cid:20} stock {stock}")
    if errors:
        sys.exit("Boutique non ecrite :\n  " + "\n  ".join(errors))
    shop = {"DisplayNameKey": "Maitre de guilde", "RefreshInterval": {"Days": 1}, "RestockHour": 6, "TradeSlots": slots}
    path = os.path.join(RES, "Server", "BarterShops", "Eldanior_Maitre_Guilde.json")
    with open(path, "w", encoding="utf-8") as f:
        json.dump(shop, f, indent=2, ensure_ascii=False)
        f.write("\n")
    print("Maitre de guilde :", os.path.normpath(path))


if __name__ == "__main__":
    main()
