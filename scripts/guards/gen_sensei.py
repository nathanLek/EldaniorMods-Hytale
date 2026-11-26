"""Boutiques des Sensei (2026-11-10, demande de l'utilisateur : « un Sensei par famille, tenue noble ; il permet d'apprendre
les techniques du clan, c'est un commercant, il donnera des quetes et on peut lui demander un duel »).

Une boutique par famille qui a des techniques : Eldanior_Sensei_<Famille> (BarterShop du jeu, comme les metiers) qui vend
les manuels secrets (Eldanior_Manuel_Luo_<Technique>, scripts/familles/gen_voie_martiale_objets.py) :
  - Sama, Hanazakura, Setsuka : les techniques du clan (LuoClans.TECHNIQUES), prix selon le niveau requis ;
  - Luo : les manuels de la voie martiale (de l'Apprenti au Dieu Martial) et les 12 Mouvements de la famille, prix selon le
    palier ; le Sensei ne montre que ce que le rang martial du joueur permet d'apprendre (2026-11-12).
Les familles sans technique (Kage, Runkandel, les autres) n'ont pas encore de boutique : le Sensei le dit (Sensei.java).
Reserve aux membres de la famille du Sensei (Sensei.openShop) ; un manuel ne s'utilise que par qui peut apprendre la technique.

Usage : python3 scripts/guards/gen_sensei.py [src/main/resources]
"""
import json, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "..", "..", "src", "main", "resources")
sys.path.insert(0, os.path.join(HERE, "..", "familles"))
sys.argv = [sys.argv[0], RES]
import gen_voie_martiale_objets as V  # noqa: E402

COINS = [("Elda_Zenith_Coins", 10000), ("Elda_Diamond_Coins", 1000), ("Elda_Gold_Coins", 100), ("Elda_Silver_Coins", 10),
         ("Elda_Copper_Coins", 1)]
# palier de la voie martiale -> niveau du personnage equivalent (pour le prix)
TIER_LEVEL = {1: 40, 2: 100, 3: 180, 4: 280, 5: 400, 6: 550, 7: 750, 8: 999}


def coins(value):
    for coin, worth in COINS:
        q = round(value / worth)
        if q >= 2 or worth == 1:
            return {"ItemId": coin, "Quantity": min(999, max(1, q))}


def price(level):
    return coins(max(200, 60 * level ** 1.35))


# Equipement du dojo (2026-11-13, demande de l'utilisateur) : vendu par tout Sensei a tous (boutique Eldanior_Sensei_Dojo) et en
# tete des boutiques de famille. Prix en cuivres (provisoires).
DOJO = [
    ("Eldanior_Parchemin_Voie_Martiale", 1000, 1),
    ("Eldanior_Weapon_Sword_Katana_Bokken", 250, 3),
    ("Armor_Kimono_Ronin_Head", 150, 3),
    ("Armor_Kimono_Ronin_Chest", 300, 3),
    ("Armor_Kimono_Ronin_Hands", 120, 3),
    ("Armor_Kimono_Ronin_Legs", 220, 3),
]


def dojo_slots():
    return [{"Type": "Fixed", "Trade": {"Output": {"ItemId": iid, "Quantity": 1}, "Input": [coins(value)], "Stock": stock}}
            for iid, value, stock in DOJO]


def main():
    shops = {}
    for key, name, tier, quality, color in V.BOOKS:
        if isinstance(tier, tuple):
            family, level = tier
        else:
            family, level = "Luo", TIER_LEVEL[tier]
        shops.setdefault(family, []).append((level, "Eldanior_Manuel_Luo_" + key))
    # Mouvements de famille (2026-11-12) : tous en vente, la boutique ne montre au joueur que ceux de son rang (Sensei.java) ;
    # le 1er est aussi remis a l'entree dans la famille. Les Mouvements sans technique sont vendus pour tester (choix de
    # l'utilisateur) : leur lecture dit « technique a venir » et ne consume pas le manuel.
    for family, moves in V.MOVEMENTS.items():
        for n, tier, rune_key, name in moves:
            shops.setdefault(family, []).append((TIER_LEVEL[tier] + n, V.movement_item(family, n)))
    out = os.path.join(RES, "Server", "BarterShops")
    for family, books in shops.items():
        books.sort()
        slots = dojo_slots() + [{"Type": "Fixed", "Trade": {"Output": {"ItemId": iid, "Quantity": 1}, "Input": [price(level)], "Stock": 3}}
                                for level, iid in books]
        data = {"DisplayNameKey": "Sensei " + family, "RefreshInterval": {"Days": 7}, "RestockHour": 6, "TradeSlots": slots}
        with open(os.path.join(out, "Eldanior_Sensei_%s.json" % family), "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
            f.write("\n")
        print("Eldanior_Sensei_%s : %d manuels" % (family, len(books)))
    data = {"DisplayNameKey": "Dojo", "RefreshInterval": {"Days": 7}, "RestockHour": 6, "TradeSlots": dojo_slots()}
    with open(os.path.join(out, "Eldanior_Sensei_Dojo.json"), "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")
    print("Eldanior_Sensei_Dojo : %d objets" % len(DOJO))


if __name__ == "__main__":
    main()
