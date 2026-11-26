"""Boutique du Maroquinier (NpcTrades.Metier.MAROQUINIER, 2026-11-16) : BarterShops/Eldanior_Maroquinier.json.

Seul vendeur des ameliorations du sac a dos du jeu (leurs recettes sont retirees : surcharges Server/Item/Items/Upgrade/) :
  - Upgrade_Backpack_2 « Amelioration de sac I » (9 -> 18 cases)  : 1 zenith (10 000 cuivres) ;
  - Upgrade_Backpack_3 « Amelioration de sac II » (18 -> 27 cases) : 5 zeniths (50 000 cuivres) ;
  - la Poche dimensionnelle : 10 pieces imperiales (10 000 000 cuivres, PocheDimensionnelle.PRICE).
La derniere, Upgrade_Backpack_4 « Amelioration de sac III » (27 -> 36), n'est pas vendue : elle reste celle du jeu (marchand de
ferraille Klops des donjons, 2 noyaux de Gobliterator). Upgrade_Backpack_1 est inutile : le sac a dos est a 9 cases des l'arrivee.

Usage : python3 scripts/poche/gen_maroquinier.py <src/main/resources>
"""
import json, os, sys

RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "src", "main", "resources")


def fixed(item, coin, qty, stock):
    return {"Type": "Fixed", "Trade": {"Output": {"ItemId": item, "Quantity": 1}, "Input": [{"ItemId": coin, "Quantity": qty}], "Stock": stock}}


shop = {"DisplayNameKey": "Maroquinier", "RefreshInterval": {"Days": 1}, "RestockHour": 6, "TradeSlots": [
    fixed("Upgrade_Backpack_2", "Elda_Zenith_Coins", 1, 20),
    fixed("Upgrade_Backpack_3", "Elda_Zenith_Coins", 5, 10),
    fixed("Eldanior_Poche_Dimensionnelle", "Elda_Imperial_Coins", 10, 5),
]}
path = os.path.join(RES, "Server", "BarterShops", "Eldanior_Maroquinier.json")
with open(path, "w", encoding="utf-8") as f:
    json.dump(shop, f, indent=2, ensure_ascii=False)
    f.write("\n")
print("Maroquinier :", path)
