"""Marchand du Clan Luo : role du PNJ et boutiques (BarterShops).

PNJ de poste « marchand » (/es garde poser marchand --type armurerie|tailleur|herboriste|maison_the
--statut apprenti|negociant|maitre) : role interactif Eldanior_Npc_Marchand_Luo (copie du maitre de guilde, invulnerable).
A l'interaction, guards/LuoMerchants choisit la boutique selon le client (Java, BarterPage) :
  - membre du clan : la boutique du marchand, Luo_<Boutique>_<Rang>, si son rang le permet
    (Apprenti : tout le clan ; Negociant : Nobles et au-dessus ; Grand Marchand : Seigneur, Doyens, Patriarche) ;
  - marchand agree (plaque) : le Comptoir, Luo_Comptoir (marchandises neutres : soie, the, riz) ;
  - etranger : le laissez-passer, Luo_Laissez_Passer (plaque Visiteur, 2 jours ; 2026-11-16).

Rangs : Apprenti = Commun et Rare, reassort chaque jour ; Negociant = Epique et Unique, tous les 2 jours ; Grand Marchand =
le haut de gamme, une seule piece en stock, tous les 3 jours. Stocks : emplacements fixes + tirages ponderes (Pool).
Prix (en pieces Eldanior, CoinItemRegistry) : 40 x niveau^1,3 cuivres pour un equipement (kimono : torse plein tarif, autres
pieces 60 %), 8 % de cela pour les consommables et marchandises ; rendus dans la plus grosse piece qui reste au-dessus de 2.

Usage : python3 scripts/guards/gen_marchand_luo.py <src/main/resources>
"""
import copy, glob, json, os, sys, zipfile

RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "src", "main", "resources")
ASSETS = os.path.expanduser("~/Library/Application Support/Hytale/install/release/package/game/latest/Assets.zip")
COINS = [("Elda_Zenith_Coins", 10000), ("Elda_Diamond_Coins", 1000), ("Elda_Gold_Coins", 100), ("Elda_Silver_Coins", 10),
         ("Elda_Copper_Coins", 1)]
_Z = zipfile.ZipFile(ASSETS)
_VANILLA = {n.rsplit("/", 1)[-1][:-5]: n for n in _Z.namelist() if n.startswith("Server/Item/Items/") and n.endswith(".json")}


def write(rel, data):
    path = os.path.join(RES, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


def item(iid):
    mod = glob.glob(os.path.join(RES, "Server/Item/Items/**/%s.json" % iid), recursive=True)
    if mod:
        with open(mod[0], encoding="utf-8") as f:
            return json.load(f)
    if iid in _VANILLA:
        return json.loads(_Z.read(_VANILLA[iid]))
    raise SystemExit("Objet introuvable : " + iid)


QUALITY_LEVEL = {"Common": 10, "Uncommon": 25, "Rare": 80, "Epic": 180, "Unique": 350, "Legendary": 500}
PRICE_OVERRIDE = {"Consomable_Stat_%s_One" % st: 10000 for st in ("Force", "Vitalite", "Intelligence", "Endurance", "Agilite", "Chance")}
PRICE_OVERRIDE["Eldanior_Plaque_Luo_Visiteur"] = 500                 # laissez-passer de 2 jours : 5 pieces d'or (2026-11-16)
GOODS_FACTOR = 0.08                       # consommables et marchandises (the, nourriture, soie, riz) : 12 fois moins cher qu'un equipement


def price(iid, factor=1.0):
    data = item(iid)
    level = data.get("ItemLevel") or QUALITY_LEVEL.get(data.get("Quality"), 5)
    gear = iid.startswith(("Armor_", "Eldanior_Weapon_", "Artefact_", "Weapon_", "Tool_"))
    kind = 1.0 if gear else 0.3 if iid.startswith("Furniture_") else GOODS_FACTOR
    value = PRICE_OVERRIDE.get(iid, 0) * factor or max(10, 40 * level ** 1.3 * factor * kind)
    for coin, worth in COINS:
        q = round(value / worth)
        if q >= 2 or worth == 1:
            return [{"ItemId": coin, "Quantity": min(999, max(1, q))}]


def trade(iid, qty=1, stock=10, factor=1.0):
    return {"Output": {"ItemId": iid, "Quantity": qty}, "Input": price(iid, factor * qty), "Stock": stock}


def fixed(iid, qty=1, stock=10, factor=1.0):
    return {"Type": "Fixed", "Trade": trade(iid, qty, stock, factor)}


def pool(count, entries):
    """entries : [(objet, poids, quantite, (stock min, stock max))]"""
    trades = []
    for iid, weight, qty, stock in entries:
        t = trade(iid, qty, 1)
        t["Weight"] = weight
        t["Stock"] = list(stock)
        trades.append(t)
    return {"Type": "Pool", "SlotCount": count, "Trades": trades}


def kimono(outfit, stock):
    """Les quatre pieces d'une tenue (Armor_Kimono_<outfit>_<piece> ; outfit vide : Sakura)."""
    prefix = "Armor_Kimono_" + (outfit + "_" if outfit else "")
    return [fixed(prefix + p, 1, stock, 1.0 if p == "Chest" else 0.6) for p in ("Head", "Chest", "Hands", "Legs")]


def katana(name):
    return ("Artefact_Sword_Katana_" if name in ("Shirasagi", "Hien", "Gotetsu", "Yamikiri") else "Eldanior_Weapon_Sword_Katana_") + name


def shop(title, days, slots):
    return {"DisplayNameKey": title, "RefreshInterval": {"Days": days}, "RestockHour": 6, "TradeSlots": slots}


SHOPS = {
    # Laissez-passer (2026-11-16) : seule boutique d'un marchand Luo ouverte aux etrangers (LuoMerchants.PASS_SHOP)
    "Luo_Laissez_Passer": shop("Laissez-passer du Clan Luo", 1, [fixed("Eldanior_Plaque_Luo_Visiteur", stock=50)]),
    "Luo_Armurerie_Apprenti": shop("Armurerie du Clan Luo — Apprenti", 1, [
        fixed(katana("Bokken"), stock=6), fixed(katana("Fer"), stock=4),
        pool(1, [(katana("Aonami"), 60, 1, (1, 2)), (katana("Fer"), 40, 1, (2, 3))]),
        fixed("Weapon_Longsword_Katana", stock=3), fixed("Weapon_Sword_Iron", stock=4), fixed("Weapon_Shield_Iron", stock=3),
        pool(8, [(w, 20, 1, (1, 3)) for w in ("Weapon_Sword_Copper", "Weapon_Sword_Bronze", "Weapon_Daggers_Iron", "Weapon_Daggers_Copper",
                                               "Weapon_Shortbow_Iron", "Weapon_Shortbow_Copper", "Weapon_Spear_Iron", "Weapon_Spear_Copper",
                                               "Weapon_Axe_Iron", "Weapon_Axe_Copper", "Weapon_Longsword_Iron", "Weapon_Shield_Copper",
                                               "Weapon_Club_Copper", "Weapon_Battleaxe_Iron")]),
        pool(4, [(t, 15, 1, (1, 3)) for t in ("Tool_Pickaxe_Iron", "Tool_Hatchet_Iron", "Tool_Shovel_Iron", "Tool_Hammer_Iron",
                                               "Tool_Pickaxe_Copper", "Tool_Hatchet_Copper")])]),
    "Luo_Armurerie_Negociant": shop("Armurerie du Clan Luo — Négociant", 2, [
        fixed(katana("Aonami"), stock=2), fixed(katana("Tsukikage"), stock=1),
        pool(1, [(katana("Shirasagi"), 50, 1, (1, 1)), (katana("Hien"), 50, 1, (1, 1))]),
        pool(6, [(w, 20, 1, (1, 2)) for w in ("Weapon_Sword_Cobalt", "Weapon_Sword_Thorium", "Weapon_Daggers_Cobalt", "Weapon_Shortbow_Cobalt",
                                               "Weapon_Longsword_Thorium", "Weapon_Shield_Cobalt", "Weapon_Sword_Mithril", "Weapon_Sword_Steel",
                                               "Weapon_Daggers_Thorium", "Weapon_Shield_Thorium", "Weapon_Spear_Cobalt")]),
        pool(2, [(t, 15, 1, (1, 2)) for t in ("Tool_Pickaxe_Cobalt", "Tool_Hatchet_Cobalt", "Tool_Pickaxe_Thorium", "Tool_Hatchet_Thorium")])]),
    "Luo_Armurerie_Maitre": shop("Armurerie du Clan Luo — Grand Marchand", 3, [
        fixed(katana("Tsukikage"), stock=1), fixed(katana("Shirasagi"), stock=1), fixed(katana("Hien"), stock=1),
        fixed(katana("Gotetsu"), stock=1), fixed(katana("Yamikiri"), stock=1),
        pool(2, [(w, 20, 1, (1, 1)) for w in ("Weapon_Sword_Onyxium", "Weapon_Sword_Adamantite", "Weapon_Longsword_Mithril",
                                               "Weapon_Daggers_Onyxium", "Weapon_Shield_Onyxium")])]),

    "Luo_Tailleur_Apprenti": shop("Tailleur du Clan Luo — Apprenti", 1,
                                  kimono("Pauvre", 4) + kimono("Rizieres", 4) + kimono("Recrue", 3) + kimono("Marchand", 2)
                                  + kimono("Ronin", 2) + [
                                      pool(8, [("Armor_Cloth_%s_%s" % (c, p), 10, 1, (1, 2)) for c in ("Cotton", "Linen", "Wool") for p in ("Head", "Chest", "Hands", "Legs")]
                                           + [("Armor_Leather_%s_%s" % (c, p), 8, 1, (1, 2)) for c in ("Light", "Soft") for p in ("Head", "Chest", "Hands", "Legs")])]),
    # tenues des clans (Sakura : Hanazakura, Setsuka, Garde d'Honneur : Sama, Ombre : Kage) et des nobles ; les kimonos de Doyen
    # (Armor_Kimono_Noble_*) ne se vendent plus : remis au Patriarche de chaque clan (FamilyKeySystem)
    "Luo_Tailleur_Negociant": shop("Tailleur du Clan Luo — Négociant", 2, kimono("", 2) + kimono("Setsuka", 2) + kimono("Pelerin", 2) + [
        pool(6, [("Armor_Kimono_%s_%s" % (c, p), 25, 1, (1, 1)) for c in ("Cour", "Lettre", "Garde") for p in ("Head", "Chest", "Hands", "Legs")]
             + [("Armor_Kimono_Ombre_" + p, 15, 1, (1, 1)) for p in ("Head", "Chest", "Hands", "Legs")])]),
    "Luo_Tailleur_Maitre": shop("Tailleur du Clan Luo — Grand Marchand", 3, kimono("Seigneur", 1) + kimono("Negociant", 1) + [
        pool(2, [("Armor_Cloth_Silk_" + p, 10, 1, (1, 1)) for p in ("Head", "Chest", "Hands", "Legs")])]),

    "Luo_Herboriste_Apprenti": shop("Herboriste du Clan Luo — Apprenti", 1, [
        fixed("Luo_The_Vert", 2, 20), fixed("Luo_Onguent_Cerisier", 1, 8),
        pool(1, [("Luo_The_Jasmin", 60, 1, (3, 6)), ("Potion_Antidote", 40, 1, (2, 4))]),
        fixed("Potion_Health_Lesser", 1, 10), fixed("Bandage_Crude", 3, 10),
        pool(6, [(i, 20, 1, (2, 5)) for i in ("Potion_Health_Small", "Potion_Stamina_Small", "Potion_Stamina_Lesser", "Eld_Potion_Mana_Petite",
                                               "Eld_Potion_Antidote", "Potion_Regen_Health_Small", "Potion_Regen_Stamina_Small", "Potion_Mana_Small",
                                               "Potion_Antidote")]),
        pool(1, [("Consomable_Stat_%s_One" % st, 10, 1, (1, 1)) for st in ("Force", "Vitalite", "Intelligence", "Endurance", "Agilite", "Chance")])]),
    "Luo_Herboriste_Negociant": shop("Herboriste du Clan Luo — Négociant", 2, [
        fixed("Luo_The_Jade", 1, 6), fixed("Luo_Onguent_Cerisier", 2, 8),
        pool(1, [("Luo_Onguent_Dragon", 60, 1, (1, 2)), ("Luo_The_Jasmin", 40, 2, (3, 5))]),
        pool(2, [(i, 20, 1, (1, 3)) for i in ("Potion_Health", "Potion_Stamina", "Eld_Potion_Celerite", "Eld_Potion_Fortifiant",
                                               "Eld_Potion_Mana", "Eld_Potion_Antigel", "Eld_Potion_Ignifuge")]),
        pool(1, [("Consomable_Stat_%s_One" % st, 10, 1, (1, 1)) for st in ("Force", "Vitalite", "Intelligence", "Endurance", "Agilite", "Chance")])]),
    "Luo_Herboriste_Maitre": shop("Herboriste du Clan Luo — Grand Marchand", 3, [
        fixed("Luo_The_Eclipse", 1, 1), fixed("Luo_Onguent_Dragon", 1, 1),
        pool(2, [("Consomable_Stat_%s_One" % st, 10, 1, (1, 2)) for st in ("Force", "Vitalite", "Intelligence", "Endurance", "Agilite", "Chance")]),
        pool(1, [(i, 10, 1, (1, 2)) for i in ("Potion_Health_Large", "Eld_Potion_Peau_Pierre", "Eld_Potion_Force_Vitale", "Eld_Potion_Mana_Grande")])]),

    "Luo_Maison_The_Apprenti": shop("Maison de thé du Clan Luo — Apprenti", 1, [
        fixed("Luo_The_Vert", 2, 20), fixed("Food_Fish_Grilled", 2, 12), fixed("Food_Vegetable_Cooked", 2, 12),
        fixed("Plant_Crop_Rice_Item", 5, 20),
        pool(2, [("Food_Kebab_Vegetable", 30, 1, (4, 8)), ("Food_Kebab_Fruit", 30, 1, (4, 8)), ("Food_Kebab_Mushroom", 20, 1, (4, 8)),
                 ("Luo_The_Jasmin", 20, 1, (2, 4))]),
        pool(7, [(i, 10, q, (4, 10)) for i, q in (("Food_Bread", 3), ("Food_Cheese", 2), ("Food_Egg", 5), ("Food_Fish_Raw", 2),
                                                  ("Food_Kebab_Meat", 1), ("Food_Salad_Mushroom", 1), ("Food_Pie_Apple", 1), ("Food_Pie_Pumpkin", 1),
                                                  ("Food_Salad_Berry", 1), ("Food_Wildmeat_Cooked", 2), ("Plant_Seeds_Rice", 5))])]),
    "Luo_Maison_The_Negociant": shop("Maison de thé du Clan Luo — Négociant", 2, [
        fixed("Luo_The_Jasmin", 2, 10), fixed("Luo_The_Jade", 1, 4), fixed("Food_Pie_Meat", 1, 6),
        pool(2, [("Food_Salad_Caesar", 30, 1, (3, 6)), ("Food_Pie_Apple", 30, 1, (3, 6)), ("Food_Salad_Berry", 20, 1, (3, 6)),
                 ("Food_Wildmeat_Cooked", 20, 2, (3, 6))]),
        pool(2, [(i, 10, 1, (3, 6)) for i in ("Food_Pie_Pumpkin", "Food_Salad_Mushroom", "Food_Kebab_Meat", "Food_Fish_Grilled")])]),
    "Luo_Maison_The_Maitre": shop("Maison de thé du Clan Luo — Grand Marchand", 3, [
        fixed("Luo_The_Eclipse", 1, 1), fixed("Luo_The_Jade", 2, 3)]),

    "Luo_Comptoir": shop("Comptoir de la Porte du Dragon", 1, [
        fixed("Ingredient_Bolt_Silk", 2, 10), fixed("Ingredient_Fabric_Scrap_Silk", 5, 20), fixed("Luo_The_Vert", 4, 20),
        fixed("Plant_Crop_Rice_Item", 10, 30), fixed("Plant_Seeds_Rice", 5, 10),
        pool(1, [("Luo_The_Jasmin", 60, 2, (2, 4)), ("Ingredient_Bolt_Silk", 40, 4, (2, 3))])]),
}


# ===== Marchand ambulant : un peu de tout, tire de tous les objets du jeu et du mod =====
WANDERER_VARIANTS, WANDERER_SLOTS, WANDERER_CANDIDATES = 3, 16, 200
# Paliers (LuoMerchants.WANDERER_BRACKETS : niveau du joueur 0, 30, 100, 250, 450) -> niveaux d'objets proposes
WANDERER_BRACKETS = [(0, 30), (15, 100), (60, 250), (180, 450), (350, 9999)]
WANDER_PREFIXES = ("Weapon_", "Armor_", "Tool_", "Food_", "Potion_", "Ingredient_", "Eld_Potion_", "Consomable_Stat_", "Elixir_",
                   "Butin_", "Bandage_", "Fish_", "Ore_", "Plant_Crop_", "Plant_Seeds_", "Luo_The_Vert")
WANDER_EXCLUDE = ("Armor_Kimono_", "Armor_Runkandel_", "Armor_Eldanior_", "Armor_QA", "Armor_Divin", "Weapon_Bomb", "Tool_Growth",
                  "Tool_Capture", "Potion_Morph", "Potion_Template", "Potion_Empty", "_Template", "Bandage_Potion_Test", "_Eternal", "_Debug")
WEIGHT = {"Common": 40, "Uncommon": 30, "Rare": 15, "Epic": 8, "Unique": 4, "Legendary": 1}


def wanderer_candidates():
    ids = set(_VANILLA)
    for f in glob.glob(os.path.join(RES, "Server/Item/Items/**/*.json"), recursive=True):
        ids.add(os.path.basename(f)[:-5])
    out = []
    for iid in sorted(ids):
        if not iid.startswith(WANDER_PREFIXES) or any(x in iid for x in WANDER_EXCLUDE):
            continue
        try:
            d = item(iid)
        except SystemExit:
            continue
        q = d.get("Quality")
        if q not in WEIGHT or any("Familles" in c or "PrefabEditing" in c for c in d.get("Categories", [])):
            continue
        out.append((iid, q, d.get("ItemLevel") or QUALITY_LEVEL.get(q, 5)))
    return out


def wanderer_shops():
    """Un assortiment par palier de niveau et par variante : Eldanior_Ambulant_B<palier>_<variante>."""
    import random
    cands = wanderer_candidates()
    shops = {}
    for b, (lo, hi) in enumerate(WANDERER_BRACKETS, 1):
        band = [c for c in cands if lo <= c[2] <= hi]
        for k in range(1, WANDERER_VARIANTS + 1):
            rng = random.Random(1000 * b + k)
            picked = rng.sample(band, min(WANDERER_CANDIDATES, len(band)))
            entries = [(iid, WEIGHT[q], 1, (1, 2) if iid.startswith(("Weapon_", "Armor_", "Tool_")) else (2, 6)) for iid, q, _ in picked]
            shops["Eldanior_Ambulant_B%d_%d" % (b, k)] = shop("Marchand ambulant", 1, [pool(min(WANDERER_SLOTS, len(entries)), entries)])
        print("  palier B%d (objets niv. %d-%d) : %d objets possibles" % (b, lo, hi, len(band)))
    return shops


def furniture_shop():
    """Marchand de meubles (NpcTrades.MEUBLES) : 18 meubles tires parmi ceux du jeu, reassort chaque jour."""
    entries = []
    for iid in sorted(_VANILLA):
        if not iid.startswith("Furniture_") or "Template" in iid or "_Debug" in iid:
            continue
        d = item(iid)
        q = d.get("Quality") or "Common"
        if q not in WEIGHT or any("PrefabEditing" in c or "Prototype" in c for c in d.get("Categories", [])) or "Prototype" in iid:
            continue
        entries.append((iid, WEIGHT[q], 1, (1, 4)))
    print("Marchand de meubles : %d meubles possibles" % len(entries))
    return shop("Marchand de meubles", 1, [pool(18, entries)])


def role(rid="Eldanior_Npc_Marchand_Luo", comment="Marchand du Clan Luo (scripts/guards/gen_marchand_luo.py) : a l'interaction, LuoMerchants ouvre la boutique selon le client."):
    with open(os.path.join(RES, "Server/NPC/Roles/Eldanior/Npcs/Eldanior_Npc_Maitre_Guilde.json"), encoding="utf-8") as f:
        r = json.load(f)
    r = json.loads(json.dumps(r).replace("server.interactionHints.eldanior_guildmaster", "server.interactionHints.trade")
                   .replace("server.npcRoles.Eldanior_Npc_Maitre_Guilde.name", "server.npcRoles.%s.name" % rid))
    r["$Comment"] = comment
    return r


def main():
    for sid, data in SHOPS.items():
        for s in data["TradeSlots"]:
            for t in ([s["Trade"]] if s["Type"] == "Fixed" else s["Trades"]):
                item(t["Output"]["ItemId"])                       # existe
        write("Server/BarterShops/%s.json" % sid, data)
    # roles des marchands : scripts/npcs/gen_roles_civils.py
    for old in glob.glob(os.path.join(RES, "Server/BarterShops/Eldanior_Ambulant_*.json")):
        os.remove(old)
    for sid, data in wanderer_shops().items():
        write("Server/BarterShops/%s.json" % sid, data)
    # marchand de meubles : boutiques a trois rangs de scripts/guards/gen_metiers.py (Eldanior_Metier_Meubles_*)
    print("Marchand Luo : %d boutiques" % len(SHOPS))
    for sid, data in SHOPS.items():
        print(" ", sid, "; ".join("%s=%d %s" % (t["Output"]["ItemId"].split("_", 2)[-1], t["Input"][0]["Quantity"], t["Input"][0]["ItemId"][5:-6])
                                  for s in data["TradeSlots"] for t in ([s["Trade"]] if s["Type"] == "Fixed" else s["Trades"][:2]))[:220])


if __name__ == "__main__":
    main()
