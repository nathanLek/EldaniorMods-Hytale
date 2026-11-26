"""Boutiques des commercants : trois rangs par metier (guards/NpcTrades.Rang), comme les marchands Luo.

Eldanior_Metier_<Metier>_<Rang>.json pour forgeron, alchimiste, tavernier, fermier, mineur, bucheron, eleveur, poissonnier,
menuisier et marchand de meubles (banquier : fenetre de banque, BankPage ; intendant : a part).
  - Apprenti  : Commun et Peu commun, reassort chaque jour, stocks genereux ;
  - Compagnon : Rare et Epique, tous les 2 jours ;
  - Maitre    : Unique et Legendaire, une piece, tous les 3 jours.
Sans qualite (bois, meubles, recoltes) : materiau, essence, style de meuble, sinon tiers par prix. Jamais d'objets divins
(fer divin : forgerons legendaires), de famille, du Clan Luo, ni d'objets techniques.
Forgeron : kits de reparation toujours en rayon (REPAIR).
Chaque rang reprend la liste de rachat du metier (data/rachats_metiers.json, tiree des anciennes boutiques Kweebec) : l'onglet
Rachat de la boutique (ShopPage) l'affiche, en plus de ce que le metier accepte (NpcTrades.Metier.buys).
Prix : gen_marchand_luo.price (40 x niveau^1,3 cuivres pour l'equipement, 8 % pour les marchandises, 30 % pour les meubles).

Usage : python3 scripts/guards/gen_metiers.py <src/main/resources>
"""
import glob, json, os, random, sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import gen_marchand_luo as g  # noqa: E402

RES = g.RES
RANKS = [("Apprenti", 1, 16), ("Compagnon", 2, 12), ("Maitre", 3, 6)]       # (asset, jours de reassort, emplacements)
QUALITY_TIER = {"Common": 1, "Uncommon": 1, "Rare": 2, "Epic": 2, "Unique": 3, "Legendary": 3}
MATERIAL = {"Crude": 1, "Wood": 1, "Stone": 1, "Copper": 1, "Bronze": 1, "Iron": 1, "Basic": 1,
            "Steel": 2, "Silver": 2, "Gold": 2, "Cobalt": 2, "Thorium": 2, "Scarab": 2,
            "Mithril": 3, "Adamantite": 3, "Onyxium": 3}
FURNITURE_STYLE = {"Crude": 1, "Village": 1, "Lumberjack": 1, "Tavern": 1, "Kweebec": 1, "Bookshelf": 1,
                   "Human": 2, "Feran": 2, "Jungle": 2, "Desert": 2, "Frozen": 2, "Goblin": 2, "Scarak": 2, "Flag": 2,
                   "Royal": 3, "Temple": 3, "Ancient": 3, "Dungeon": 3, "Christmas": 3, "Cybercity": 3}
WOOD = {"Softwood": 1, "Village": 1, "Oak": 1, "Birch": 1, "Hardwood": 2, "Redwood": 2, "Jungle": 2, "Bamboo": 2,
        "Lightwood": 2, "Tropicalwood": 2, "Darkwood": 2, "Amber": 3, "Sallow": 3, "Crystal": 3, "Petrified": 3}
BENCH = {"WorkBench": 1, "Campfire": 1, "Cooking": 1, "Farming": 1, "Lumbermill": 1, "Furniture": 1,
         "Furnace": 2, "Tannery": 2, "Loom": 2, "Salvage": 2, "Weapon": 2, "Armour": 2, "Alchemy": 2, "Trough": 2,
         "Arcane": 3, "Armory": 3}
EXCLUDE = ("Template", "_Debug", "Prototype", "QA", "_Test", "Test_", "Armor_Kimono_", "Armor_Runkandel_", "Armor_Eldanior_",
           "Eldanior_Weapon_Sword_Katana_", "Artefact_", "Divin", "Luo_", "Potion_Morph", "_Eternal", "Bench_Memories",
           "Bench_Abilities", "Bench_Builders", "Weapon_Bomb")

# Ce que vend chaque metier : prefixes (ou fonction) sur l'identifiant
SELLS = {
    "Forgeron": ("Weapon_", "Eldanior_Weapon_", "Armor_", "Ingredient_Bar_"),
    "Alchimiste": ("Potion_", "Eld_Potion_", "Elixir_", "Consomable_Stat_", "Bandage_", "Plant_Crop_Health", "Plant_Crop_Mana",
                   "Plant_Crop_Stamina"),
    "Tavernier": ("Food_", "Ingredient_Salt", "Ingredient_Dough", "Plant_Fruit_"),
    "Fermier": lambda i: (i.startswith("Plant_Crop_") and i.endswith("_Item")) or i.startswith(
        ("Plant_Seeds_", "Plant_Sapling_", "Tool_Hoe", "Tool_Sickle", "Tool_Watering", "Tool_Fertilizer", "Tool_Growth",
         "Utility_Bag_Seed", "Food_Egg")),
    "Mineur": ("Ore_", "Ingredient_Bar_", "Ingredient_Crystal", "Ingredient_Powder", "Tool_Pickaxe", "Tool_Shovel", "Tool_Hammer",
               "Tool_Repair"),
    "Bucheron": lambda i: i.startswith(("Tool_Hatchet", "Tool_Bark", "Tool_Sap", "Ingredient_Tree_", "Ingredient_Stick",
                                        "Ingredient_Fibre", "Plant_Sapling_"))
                          or (i.startswith("Wood_") and (i.endswith("_Trunk") or i.endswith("_Planks"))),
    "Eleveur": ("Egg_Spawner_", "Tool_Capture", "Tool_Feedbag", "Tool_Trap", "Tool_Shears", "Ingredient_Hide", "Ingredient_Leather",
                "Ingredient_Feathers", "Coop_"),
    "Poissonnier": lambda i: (i.startswith("Fish_") and i.endswith("_Item")) or i.startswith(("Food_Fish", "Tool_Fishing")),
    "Menuisier": ("Bench_", "Furniture_Village", "Furniture_Crude", "Furniture_Tavern", "Furniture_Lumberjack", "Furniture_Human",
                  "Furniture_Royal"),
    "Meubles": ("Furniture_",),
}
BUYLIST_KEY = {"Forgeron": "FORGERON", "Alchimiste": "ALCHIMISTE", "Tavernier": "TAVERNIER", "Fermier": "FERMIER", "Mineur": "MINEUR",
               "Bucheron": "BUCHERON", "Eleveur": "ELEVEUR", "Poissonnier": "POISSONNIER", "Menuisier": "MENUISIER", "Meubles": "MEUBLES"}
TITLE = {"Forgeron": "Forgeron", "Alchimiste": "Alchimiste", "Tavernier": "Tavernier", "Fermier": "Fermier", "Mineur": "Mineur",
         "Bucheron": "Bûcheron", "Eleveur": "Éleveur", "Poissonnier": "Poissonnier", "Menuisier": "Menuisier", "Meubles": "Marchand de meubles"}
RANK_LABEL = {"Apprenti": "Apprenti", "Compagnon": "Compagnon", "Maitre": "Maître"}
# Kits de reparation, toujours en rayon chez le forgeron (perte de durabilite max a la reparation : Crude 20 %, Iron 10 %,
# Rare 15 %, Goblin 0 %)
REPAIR = {"Apprenti": [("Tool_Repair_Kit_Crude", 10), ("Tool_Repair_Kit_Iron", 6)],
          "Compagnon": [("Tool_Repair_Kit_Iron", 10), ("Tool_Repair_Kit_Rare", 4)],
          "Maitre": [("Tool_Repair_Kit_Iron", 10), ("Tool_Repair_Kit_Goblin", 3)]}


def all_ids():
    ids = set(g._VANILLA)
    for f in glob.glob(os.path.join(RES, "Server/Item/Items/**/*.json"), recursive=True):
        ids.add(os.path.basename(f)[:-5])
    return sorted(ids)


def keyword_tier(iid, table):
    for k, v in table.items():
        if "_" + k + "_" in iid + "_":
            return v
    return None


def tier(iid, d):
    """1 a 3, ou None si rien ne permet de trancher (tiers par prix ensuite)."""
    if iid.startswith("Furniture_"):
        t = keyword_tier(iid, FURNITURE_STYLE)
        if t: return t
    if iid.startswith("Bench_"):
        return BENCH.get(iid[len("Bench_"):], 2)
    if iid.startswith("Wood_"):
        return keyword_tier(iid, WOOD) or 1
    q = d.get("Quality")
    if q in QUALITY_TIER:
        return QUALITY_TIER[q]
    return keyword_tier(iid, MATERIAL)


def candidates(metier):
    rule = SELLS[metier]
    match = rule if callable(rule) else (lambda i: i.startswith(rule))
    out = []
    for iid in all_ids():
        if not match(iid) or any(x in iid for x in EXCLUDE):
            continue
        try:
            d = g.item(iid)
        except SystemExit:
            continue
        q = d.get("Quality")
        if q is not None and q not in QUALITY_TIER:                      # Divin, Developer, Technical, Junk...
            continue
        if any("Familles" in c or "PrefabEditing" in c for c in d.get("Categories", [])):
            continue
        try:
            p = g.price(iid)[0]
        except Exception:
            continue
        out.append((iid, tier(iid, d), dict(g.COINS)[p["ItemId"]] * p["Quantity"], q))
    # sans qualite ni materiau : tiers par prix
    unknown = sorted([c for c in out if c[1] is None], key=lambda c: (c[2], c[0]))
    n = len(unknown)
    fixed = {c[0]: 1 + (3 * k) // max(1, n) for k, c in enumerate(unknown)}
    return [(iid, t if t is not None else fixed[iid], price, q) for iid, t, price, q in out]


def rank_shop(metier, asset, days, slots, cands, buylist):
    t = {"Apprenti": 1, "Compagnon": 2, "Maitre": 3}[asset]
    band = [c for c in cands if c[1] == t]
    if len(band) < slots:                                               # rang trop maigre : les meilleurs du rang en dessous
        below = sorted([c for c in cands if c[1] == t - 1], key=lambda c: -c[2])
        band += below[:slots - len(band)]
    if not band:
        band = cands
    gear = lambda i: i.startswith(("Weapon_", "Eldanior_Weapon_", "Armor_", "Tool_", "Bench_", "Furniture_", "Egg_Spawner_"))
    if t == 1:
        stock = lambda i: (1, 3) if gear(i) else (4, 12)
    elif t == 2:
        stock = lambda i: (1, 2) if gear(i) else (2, 6)
    else:
        stock = lambda i: (1, 1) if gear(i) else (1, 3)
    weight = lambda q: g.WEIGHT.get(q or "Common", 20)
    entries = [(iid, weight(q), 1, stock(iid)) for iid, _, _, q in band]
    trade_slots = [g.fixed(iid, 1, n) for iid, n in REPAIR[asset]] if metier == "Forgeron" else []
    trade_slots.append(g.pool(min(slots, len(entries)), entries))
    for tr in buylist:                                                  # liste de rachat (onglet Rachat)
        tr = dict(tr)
        tr["Stock"] = 999
        trade_slots.append({"Type": "Fixed", "Trade": tr})
    return g.shop("%s — %s" % (TITLE[metier], RANK_LABEL[asset]), days, trade_slots)


def main():
    with open(os.path.join(HERE, "data", "rachats_metiers.json"), encoding="utf-8") as f:
        buylists = json.load(f)
    random.seed(7)
    total = 0
    for metier in SELLS:
        cands = candidates(metier)
        counts = [sum(1 for c in cands if c[1] == t) for t in (1, 2, 3)]
        print("%-12s %3d objets (apprenti %d, compagnon %d, maitre %d), rachat %d"
              % (metier, len(cands), *counts, len(buylists.get(BUYLIST_KEY[metier], []))))
        for asset, days, slots in RANKS:
            data = rank_shop(metier, asset, days, slots, cands, buylists.get(BUYLIST_KEY[metier], []))
            g.write("Server/BarterShops/Eldanior_Metier_%s_%s.json" % (metier, asset), data)
            total += 1
    print("%d boutiques ecrites" % total)


if __name__ == "__main__":
    main()
