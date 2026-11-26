"""Fichier central de l'economie : docs/economie.md (tous les objets du jeu, vanilla + Eldanior, avec leur prix).

Principe :
  - Les objets SANS recette (ressources brutes, butins, objets trouves) ont un prix FIXE, saisi dans scripts/economie/prix.json
    (section "ressources", rangee par famille).
  - Les objets AVEC recette ont un prix CALCULE : somme du prix des ingredients x marge (prix.json "marge"), divisee par la
    quantite produite. Un prix peut etre impose a la main dans "forces" (il remplace le calcul).
  - Un ingredient "type de ressource" (ex. n'importe quel bois) vaut le moins cher des objets de ce type (ou prix.json "types").
  - Monnaie : valeur des pieces (CoinWallet), 1 = 1 cuivre.
  - Hors economie : objets de debug, d'editeur, effets visuels, prototypes, gabarits (listes, jamais vendus).

Tous les prix sont en cuivres (1 or = 100, 1 diamant = 1 000, 1 zenith = 10 000, 1 astrale = 100 000).

Usage :
  python3 scripts/economie/gen_economie.py          # regenere docs/economie.md depuis prix.json
  python3 scripts/economie/gen_economie.py --init   # ajoute aussi a prix.json une estimation pour chaque objet sans prix
                                                    # (n'ecrase jamais un prix deja saisi)
Variable HYTALE_ASSETS : dossier Server des assets vanilla (defaut ~/Desktop/Assets Hytale/Server).
"""
import collections
import datetime
import json
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.normpath(os.path.join(HERE, "..", ".."))
VANILLA = os.environ.get("HYTALE_ASSETS", os.path.expanduser("~/Desktop/Assets Hytale/Server"))
MOD = os.path.join(ROOT, "src", "main", "resources", "Server")
PRIX = os.path.join(HERE, "prix.json")
OUT = os.path.join(ROOT, "docs", "economie.md")
# Donnees lues par le mod (economy/PriceBook) : embarquees dans le jar, hors de Server/ et Common/ (pas des assets).
RUNTIME = os.path.join(ROOT, "src", "main", "resources", "eldanior", "economie.json")
# Prix modifies en jeu (/es prix, economy/PriceBook) sur le serveur de test : appliques par-dessus prix.json.
GAME = os.environ.get("ELDANIOR_PRIX_JEU", os.path.join(ROOT, "run", "mods", "eldaniorsystem_EldaniorSystem", "economie",
                                                        "prix_jeu.json"))

COINS = [("Elda_Imperial_Coins", 1_000_000, "impériale", "impériales"), ("Elda_Astral_Coins", 100_000, "astrale", "astrales"), ("Elda_Zenith_Coins", 10_000, "zénith", "zéniths"),
         ("Elda_Diamond_Coins", 1_000, "diamant", "diamants"), ("Elda_Gold_Coins", 100, "or", "or"),
         ("Elda_Silver_Coins", 10, "argent", "argent"), ("Elda_Copper_Coins", 1, "cuivre", "cuivres")]
COIN_VALUE = {c[0]: c[1] for c in COINS}

QUALITY_FR = {"Junk": "Rebut", "Common": "Commun", "Uncommon": "Peu commun", "Rare": "Rare", "Epic": "Épique",
              "Unique": "Unique", "Legendary": "Légendaire", "Divin": "Divin"}

# Familles, dans l'ordre du document.
FAMILIES = ["Monnaie", "Minerais & métaux", "Bois", "Pierres & terres", "Plantes & cultures", "Ingrédients", "Poissons",
            "Nourriture", "Potions", "Butins de monstres", "Armes", "Armures", "Outils & utilitaires",
            "Runes & compétences", "Construction", "Meubles & décoration", "Livres & histoires", "Divers"]
OUT_OF_ECONOMY = "Hors économie"

FOLDER_FAMILY = {
    "Coins": "Monnaie", "Ore": "Minerais & métaux", "Wood": "Bois", "Rock": "Pierres & terres", "Soil": "Pierres & terres",
    "Rubble": "Pierres & terres", "Plant": "Plantes & cultures", "Ingredient": "Ingrédients", "Fish": "Poissons",
    "Food": "Nourriture", "Potion": "Potions", "Butin": "Butins de monstres", "Weapon": "Armes", "Armor": "Armures",
    "Tool": "Outils & utilitaires", "Glider": "Outils & utilitaires", "Upgrade": "Outils & utilitaires",
    "Utility": "Outils & utilitaires", "Trap": "Outils & utilitaires", "Gadget": "Outils & utilitaires",
    "Rail": "Outils & utilitaires", "Electrum": "Outils & utilitaires", "Hunt": "Outils & utilitaires",
    "Rune": "Runes & compétences", "ActiveSkill": "Runes & compétences", "PassiveSkill": "Runes & compétences",
    "Build": "Construction", "Cloth": "Construction", "Metal": "Construction", "Bone": "Meubles & décoration",
    "Furniture": "Meubles & décoration", "Deco": "Meubles & décoration", "Bench": "Meubles & décoration",
    "Container": "Meubles & décoration", "Coops": "Meubles & décoration", "Lore": "Livres & histoires",
    "Recipe": "Livres & histoires", "Histoires": "Livres & histoires",
}
EXCLUDED_FOLDERS = {"_Debug", "EditorTool", "Editor", "VFX", "Categories", "Fluid", "WorldEvents", "EggSpawner", "Hypixel",
                    "Puzzles"}
EXCLUDED_QUALITIES = {"Debug", "Tool", "Developer", "Technical"}  # "Template" : herite des gabarits, pas une exclusion
EXCLUDED_PREFIXES = ("Template_", "Debug_", "EditorTool_", "Editor_", "Prototype_", "Test_", "VFX_", "Filter_",
                     "Spawner_", "Egg_Spawner_", "Hub_Portal_", "Fluid_")
SALVAGE_BENCHES = {"Salvagebench"}

# ---------- Estimations de depart (--init) ----------
# Echelle calee sur : epee en fer ~ 20 or (prix actuel des forgerons), lingot de fer divin ~ 1 000 000 (10 astrales).
METAL_ORE = {"Copper": 60, "Iron": 150, "Silver": 300, "Thorium": 400, "Gold": 500, "Cobalt": 600, "Adamantite": 1_000,
             "Mithril": 1_500, "Onyxium": 2_500, "Prisma": 4_000, "Divin": 385_000}
RAW = {None: 10, "Junk": 2, "Common": 10, "Uncommon": 30, "Rare": 120, "Epic": 500, "Unique": 2_500, "Legendary": 12_000,
       "Divin": 300_000}
GEAR = {None: 300, "Junk": 50, "Common": 500, "Uncommon": 1_500, "Rare": 4_000, "Epic": 10_000, "Unique": 50_000,
        "Legendary": 200_000, "Divin": 3_000_000}
PAGE = {None: 500, "Junk": 100, "Common": 300, "Uncommon": 800, "Rare": 2_500, "Epic": 8_000, "Unique": 30_000,
        "Legendary": 150_000, "Divin": 1_500_000}
DECOR = {None: 50, "Junk": 10, "Common": 50, "Uncommon": 150, "Rare": 500, "Epic": 2_000, "Unique": 10_000,
         "Legendary": 50_000, "Divin": 500_000}


def nice(v):
    """Arrondi lisible : entier sous 100, puis 2 chiffres significatifs."""
    if v <= 0:
        return 0
    if v < 100:
        return max(1, int(round(v)))
    mag = 10 ** (len(str(int(v))) - 2)
    return int(round(v / mag) * mag)


def coins_text(v):
    if v is None:
        return ""
    parts, rest = [], int(v)
    for _, worth, one, many in COINS:
        n, rest = divmod(rest, worth)
        if n:
            parts.append(f"{n} {one if n == 1 else many}")
    return " ".join(parts[:3]) + (" …" if len(parts) > 3 else "") if parts else "0"


def num(v):
    return f"{int(v):,}".replace(",", " ")


# ---------- Chargement ----------
def load_json(path):
    with open(path, encoding="utf-8-sig") as f:
        return json.load(f)


def load_items():
    items = {}
    for origin, base in (("Vanilla", os.path.join(VANILLA, "Item", "Items")), ("Eldanior", os.path.join(MOD, "Item", "Items"))):
        if not os.path.isdir(base):
            sys.exit(f"Dossier introuvable : {base}")
        for d, _, fs in os.walk(base):
            for f in fs:
                if not f.endswith(".json"):
                    continue
                rel = os.path.relpath(d, base)
                items[f[:-5]] = {"json": load_json(os.path.join(d, f)), "origin": origin,
                                 "folder": rel.split(os.sep)[0] if rel != "." else "", "path": os.path.join(rel, f)}
    return items


def load_lang():
    lang = {}
    for path in (os.path.join(VANILLA, "Languages", "fr-FR", "server.lang"),
                 os.path.join(MOD, "Languages", "en-US", "server.lang"),
                 os.path.join(MOD, "Languages", "fr-FR", "server.lang")):
        if not os.path.isfile(path):
            continue
        with open(path, encoding="utf-8-sig") as f:
            for line in f:
                if "=" in line and not line.lstrip().startswith("#"):
                    k, v = line.split("=", 1)
                    lang[k.strip()] = v.strip()
    return lang


def inherited(items, iid, key, depth=0):
    j = items.get(iid, {}).get("json", {})
    if key in j:
        return j[key]
    p = j.get("Parent")
    return inherited(items, p, key, depth + 1) if p and depth < 12 else None


def item_name(items, lang, iid):
    if iid.startswith("*") and "_State_" in iid:
        return item_name(items, lang, state_base(iid)) + " (" + iid.split("_State_", 1)[1].replace("_", " ") + ")"
    j = items.get(iid, {}).get("json", {})
    key = (j.get("TranslationProperties") or {}).get("Name")
    if isinstance(key, str) and key:
        k = key[len("server."):] if key.startswith("server.") else key
        if k in lang:
            return clean(lang[k])
        if not re.match(r"^[\w.]+$", key):  # nom ecrit en clair dans le JSON
            return clean(key)
    return clean(lang.get(f"items.{iid}.name", iid))


def clean(s):
    s = re.sub(r"<[^>]+>", "", s).replace("\\n", " ").replace("|", "/")
    return s.strip()


def family_of(it, iid):
    if iid.startswith("Ingredient_Bar_") or iid.startswith("Ore_"):
        return "Minerais & métaux"
    if iid.startswith(("Butin_",)):
        return "Butins de monstres"
    if iid.startswith(("Weapon_", "Artefact_", "Eldanior_Weapon_")):
        return "Armes"
    if iid.startswith("Armor_"):
        return "Armures"
    if iid.startswith(("rune_page_", "skill_page_", "Spell_", "Eldanior_Manuel_Magie_")):
        return "Runes & compétences"
    return FOLDER_FAMILY.get(it["folder"], "Divers")


# formes temporaires des armes a feu (ArmeTransfo, 2026-11-15) : jamais vendues ni achetees
TRANSIENT = ("Artefact_Sniper_SARS", "Artefact_Canon_Main", "Artefact_Handgun_Aigles_Desert")


def excluded_reason(items, iid):
    it = items[iid]
    if iid in TRANSIENT or iid.endswith("_Baionnette"):
        return "forme temporaire d'une arme (transformation, baïonnette)"
    if iid.startswith("skill_page_") and "Categories" not in it["json"]:
        return "ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25)"
    q = inherited(items, iid, "Quality")
    if iid in COIN_VALUE:
        return None
    if it["folder"] in EXCLUDED_FOLDERS:
        return f"dossier {it['folder']}"
    if iid.startswith(EXCLUDED_PREFIXES):
        return "gabarit / outil de développement"
    if q in EXCLUDED_QUALITIES:
        return f"qualité {q}"
    return None


def state_base(iid):
    """*Deco_Tankard_State_Filled_Water -> Deco_Tankard (etat d'un objet : meme prix que l'objet)."""
    return iid[1:].split("_State_")[0]


def recipe_of(it):
    rec = it["json"].get("Recipe")
    if not rec or not rec.get("Input"):
        return None
    if any(b.get("Id") in SALVAGE_BENCHES for b in rec.get("BenchRequirement", []) or []):
        return None  # recyclage : ne definit pas le prix
    return rec


def output_qty(rec, iid):
    q = rec.get("OutputQuantity")
    if q:
        return max(1, int(q))
    for o in rec.get("Output", []) or []:
        if o.get("ItemId") == iid:
            return max(1, int(o.get("Quantity", 1)))
    return 1


def estimate(items, iid, fam):
    q = inherited(items, iid, "Quality")
    if q not in RAW:
        q = None
    lvl = inherited(items, iid, "ItemLevel") or 0
    lvl_f = 1 + min(max(int(lvl), 0), 100) / 100 if q != "Divin" else 1
    if iid.startswith("Ore_"):
        for metal, p in METAL_ORE.items():
            if iid == f"Ore_{metal}" or iid.startswith(f"Ore_{metal}_"):
                return p
    if fam == "Pierres & terres":
        return 2 if q is None else nice(RAW[q] * lvl_f)
    if fam == "Bois":
        return 20 if "Trunk" in iid else 5
    if fam == "Plantes & cultures" and q in (None, "Junk", "Common", "Uncommon"):
        return 2 if "Seeds" in iid else 3 if "Crop" in iid or "Fruit" in iid else 5
    if fam in ("Armes", "Armures", "Outils & utilitaires"):
        return nice(GEAR.get(q, GEAR[None]) * lvl_f)
    if fam == "Runes & compétences":
        return nice(PAGE.get(q, PAGE[None]))
    if fam in ("Meubles & décoration", "Construction", "Livres & histoires", "Divers"):
        return nice(DECOR.get(q, DECOR[None]) * lvl_f)
    mult = 2 if fam == "Potions" else 1
    return nice(RAW.get(q, RAW[None]) * lvl_f * mult)


# ---------- Calcul ----------
class Economy:
    def __init__(self, items, lang, prix):
        self.items, self.lang, self.prix = items, lang, prix
        self.marge = float(prix.get("marge", 1.3))
        self.fixed = {}
        for fam, entries in (prix.get("ressources") or {}).items():
            for k, v in entries.items():
                self.fixed[k] = v
        self.forced = dict(prix.get("forces") or {})
        self.types_override = dict(prix.get("types") or {})
        self.excl = {iid: excluded_reason(items, iid) for iid in items}
        self.types = collections.defaultdict(list)
        for iid, it in items.items():
            if self.excl[iid]:
                continue
            for rt in inherited(items, iid, "ResourceTypes") or []:
                if rt.get("Id"):
                    self.types[rt["Id"]].append(iid)
            for state, sdef in (it["json"].get("State") or {}).items():
                if isinstance(sdef, dict):
                    for rt in sdef.get("ResourceTypes", []) or []:
                        if rt.get("Id"):
                            self.types[rt["Id"]].append(f"*{iid}_State_{state}")
        self.memo, self.why = {}, {}
        self.game = set()

    def price(self, iid, stack=()):
        """(prix, source) ; source : monnaie, fixé, forcé, calculé, à fixer, ?, hors économie"""
        p, src, _ = self._price(iid, stack)
        return p, src

    def _price(self, iid, stack):
        """Renvoie (prix, source, entache) ; entache = le calcul a croise une recette circulaire (non mis en cache)."""
        if iid in self.memo:
            return self.memo[iid] + (False,)
        tainted = False
        if iid in COIN_VALUE:
            r = (COIN_VALUE[iid], "monnaie")
        elif iid in self.forced:
            r = (int(self.forced[iid]), "forcé")
        elif iid.startswith("*") and "_State_" in iid:
            p, src, tainted = self._price(state_base(iid), stack)
            r = (p, src)
        elif iid not in self.items:
            r = (None, "?")
            self.why[iid] = "objet inconnu"
        elif self.excl.get(iid):
            r = (None, "hors économie")
        else:
            rec = recipe_of(self.items[iid])
            r = None
            if rec and iid in stack:
                tainted = True
            elif rec:
                total, missing = 0, []
                for inp in rec["Input"]:
                    qty = int(inp.get("Quantity", 1))
                    if inp.get("ItemId"):
                        p, _, t = self._price(inp["ItemId"], stack + (iid,))
                    else:
                        p, t = self._type_price(inp.get("ResourceTypeId"), stack + (iid,))
                    tainted |= t
                    if p is None:
                        missing.append(inp.get("ItemId") or inp.get("ResourceTypeId"))
                    else:
                        total += p * qty
                for o in rec.get("Output", []) or []:  # sous-produits rendus (ex. la chope de la pate)
                    oid = o.get("ItemId")
                    if oid and oid != iid and oid not in stack:
                        p, _, t = self._price(oid, stack + (iid,))
                        tainted |= t
                        total -= (p or 0) * int(o.get("Quantity", 1))
                if missing:
                    self.why[iid] = "ingrédient sans prix : " + ", ".join(missing)
                else:
                    r = (nice(max(total, 0) * self.marge / output_qty(rec, iid)), "calculé")
            if r is None:
                if iid in self.fixed:
                    r = (int(self.fixed[iid]), "fixé")
                else:
                    r = (None, "à fixer")
        if not tainted or not stack:
            self.memo[iid] = r
        return r + (tainted,)

    def _type_price(self, rtid, stack):
        if rtid in self.types_override:
            return int(self.types_override[rtid]), False
        best, tainted = None, False
        for m in self.types.get(rtid, []):
            if m in stack:
                tainted = True
                continue
            p, _, t = self._price(m, stack)
            tainted |= t
            if p is not None and (best is None or p < best):
                best = p
        return best, tainted

    def type_cheapest(self, rtid):
        best = None
        for m in self.types.get(rtid, []):
            p, _ = self.price(m)
            if p is not None and (best is None or p < best[0]):
                best = (p, m)
        return best


# ---------- prix.json ----------
def load_prix():
    if os.path.isfile(PRIX):
        return load_json(PRIX)
    return {"marge": 1.3, "types": {}, "forces": {}, "ressources": {}}


def load_game():
    """Prix modifies en jeu : {"ressources": {id: prix}, "forces": {id: prix}} (vide si le fichier n'existe pas)."""
    if os.path.isfile(GAME):
        try:
            g = load_json(GAME)
            return {"ressources": dict(g.get("ressources") or {}), "forces": dict(g.get("forces") or {})}
        except (OSError, ValueError) as e:
            print(f"prix_jeu.json illisible ({e}) : ignoré")
    return {"ressources": {}, "forces": {}}


def with_game(prix, game):
    """Copie de prix.json avec les prix du jeu par-dessus (sans modifier prix.json)."""
    out = json.loads(json.dumps(prix))
    res = out.setdefault("ressources", {})
    for iid, v in game["ressources"].items():
        for fam in res.values():
            fam.pop(iid, None)
        res.setdefault("Modifiés en jeu", {})[iid] = v
    out.setdefault("forces", {}).update(game["forces"])
    return out


def merge_game(prix, game, items):
    """--fusionner : recopie les prix du jeu dans prix.json (a la bonne famille)."""
    res = prix.setdefault("ressources", {})
    for iid, v in game["ressources"].items():
        for fam in res.values():
            fam.pop(iid, None)
        fam = family_of(items[iid], iid) if iid in items else "Divers"
        res.setdefault(fam, {})[iid] = v
        res[fam] = dict(sorted(res[fam].items()))
    prix.setdefault("forces", {}).update(game["forces"])
    prix["forces"] = dict(sorted(prix["forces"].items()))
    with open(PRIX, "w", encoding="utf-8") as f:
        json.dump(prix, f, ensure_ascii=False, indent=1)
        f.write("\n")


def export_runtime(eco):
    """eldanior/economie.json : tout ce qu'il faut au mod pour recalculer les prix (PriceBook), a partir de prix.json seul."""
    fams = FAMILIES + [OUT_OF_ECONOMY]
    out_items = {}
    for iid, it in sorted(eco.items.items()):
        e = {"f": fams.index(OUT_OF_ECONOMY) if eco.excl[iid] else fams.index(family_of(it, iid))
             if family_of(it, iid) in fams else fams.index("Divers")}
        if it["origin"] == "Eldanior":
            e["o"] = "E"
        if eco.excl[iid]:
            e["x"] = eco.excl[iid]
        if iid in eco.fixed:
            e["p"] = int(eco.fixed[iid])
        rec = recipe_of(it)
        if rec:
            e["r"] = [[inp["ItemId"] if inp.get("ItemId") else "#" + str(inp.get("ResourceTypeId")), int(inp.get("Quantity", 1))]
                      for inp in rec["Input"]]
            q = output_qty(rec, iid)
            if q > 1:
                e["q"] = q
            b = [[o["ItemId"], int(o.get("Quantity", 1))] for o in rec.get("Output", []) or []
                 if o.get("ItemId") and o.get("ItemId") != iid]
            if b:
                e["b"] = b
        out_items[iid] = e
    data = {"_lisez_moi": "Genere par scripts/economie/gen_economie.py depuis scripts/economie/prix.json : ne pas modifier.",
            "marge": eco.marge, "familles": fams, "coins": {c[0]: c[1] for c in COINS},
            "types": {k: v for k, v in sorted(eco.types.items())}, "typesPrix": eco.types_override,
            "forces": {k: int(v) for k, v in sorted(eco.forced.items())}, "items": out_items}
    os.makedirs(os.path.dirname(RUNTIME), exist_ok=True)
    with open(RUNTIME, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, separators=(",", ":"))


def init_prix(eco, prix):
    """1re passe : objets sans recette ; 2e passe (prix recalcules) : recettes incalculables (circulaires)."""
    added = 0
    res = prix.setdefault("ressources", {})
    for second in (False, True):
        if second:
            eco = Economy(eco.items, eco.lang, prix)
        for iid in sorted(eco.items):
            if iid in COIN_VALUE or eco.excl[iid] or iid in eco.fixed or iid in eco.forced:
                continue
            if recipe_of(eco.items[iid]) and (not second or eco.price(iid)[0] is not None):
                continue
            fam = family_of(eco.items[iid], iid)
            res.setdefault(fam, {})[iid] = estimate(eco.items, iid, fam)
            added += 1
    prix["ressources"] = {f: dict(sorted(res[f].items())) for f in FAMILIES + sorted(set(res) - set(FAMILIES)) if f in res}
    prix["_lisez_moi"] = [
        "Prix en cuivres (1 or = 100, 1 diamant = 1 000, 1 zénith = 10 000, 1 astrale = 100 000).",
        "ressources : prix FIXE des objets sans recette, rangés par famille (estimations de départ du 2026-11-15, à ajuster).",
        "forces : prix imposé qui remplace le calcul par recette (ex. un objet rare dont la recette est trop bon marché).",
        "types : prix d'un type de ressource (ex. Wood_Trunk) ; sinon le moins cher des objets de ce type.",
        "marge : multiplicateur appliqué à la somme des ingrédients d'une recette.",
        "Après modification : python3 scripts/economie/gen_economie.py (régénère docs/economie.md).",
    ]
    ordered = {k: prix[k] for k in ("_lisez_moi", "marge", "types", "forces", "ressources") if k in prix}
    with open(PRIX, "w", encoding="utf-8") as f:
        json.dump(ordered, f, ensure_ascii=False, indent=1)
        f.write("\n")
    return added


# ---------- Document ----------
def anchor(fam):
    return "fam-" + re.sub(r"[^a-z]+", "-", fam.lower().replace("é", "e").replace("è", "e").replace("ê", "e")).strip("-")


def recipe_text(eco, iid):
    rec = recipe_of(eco.items[iid])
    if not rec:
        return ""
    parts = []
    for inp in rec["Input"]:
        q = int(inp.get("Quantity", 1))
        if inp.get("ItemId"):
            parts.append(f"{q}× {item_name(eco.items, eco.lang, inp['ItemId'])}")
        else:
            rt = inp.get("ResourceTypeId")
            label = eco.lang.get(f"resourceType.{rt}.name", rt)
            parts.append(f"{q}× [{clean(label)}]")
    out = output_qty(rec, iid)
    benches = [b.get("Id") for b in rec.get("BenchRequirement", []) or [] if b.get("Id")]
    s = " + ".join(parts) + (f" → ×{out}" if out > 1 else "")
    return s + (f" · {benches[0]}" if benches else "")


def row(eco, iid, with_recipe):
    it = eco.items[iid]
    q = inherited(eco.items, iid, "Quality")
    lvl = inherited(eco.items, iid, "ItemLevel")
    p, src = eco.price(iid)
    cells = [f"`{iid}`", item_name(eco.items, eco.lang, iid), QUALITY_FR.get(q, "" if q in (None, "Template") else q), str(lvl) if lvl else "",
             num(p) if p is not None else "**à fixer**" if src == "à fixer" else "**?**", coins_text(p),
             (src + " (jeu)" if iid in eco.game else src), "E" if it["origin"] == "Eldanior" else "V"]
    if with_recipe:
        r = recipe_text(eco, iid)
        if src in ("?", "à fixer") and iid in eco.why:
            r = (r + " — " if r else "") + "⚠ " + eco.why[iid]
        cells.append(r)
    return "| " + " | ".join(cells) + " |"


def build_doc(eco):
    by_fam = collections.defaultdict(lambda: {"raw": [], "craft": []})
    excluded = []
    for iid in sorted(eco.items, key=str.lower):
        if eco.excl[iid]:
            excluded.append(iid)
            continue
        fam = family_of(eco.items[iid], iid)
        by_fam[fam]["craft" if recipe_of(eco.items[iid]) else "raw"].append(iid)
    total = len(eco.items)
    priced = sum(1 for iid in eco.items if not eco.excl[iid] and eco.price(iid)[0] is not None)
    todo = [iid for iid in eco.items if not eco.excl[iid] and eco.price(iid)[0] is None]
    stale = sorted(k for k in list(eco.fixed) + list(eco.forced) if k not in eco.items and k not in COIN_VALUE)

    L = []
    L.append("# Économie d'Eldanior — répertoire de tous les objets")
    L.append("")
    L.append(f"> Fichier **généré** par `scripts/economie/gen_economie.py` le {datetime.date.today().isoformat()} — "
             "ne pas modifier à la main : changer les prix dans `scripts/economie/prix.json`, puis relancer le script.")
    L.append("")
    L.append(f"**{total} objets** ({sum(1 for i in eco.items.values() if i['origin'] == 'Vanilla')} vanilla, "
             f"{sum(1 for i in eco.items.values() if i['origin'] == 'Eldanior')} Eldanior) · **{priced}** ont un prix · "
             f"**{len(todo)}** sans prix · **{len(excluded)}** hors économie.")
    L.append("")
    L.append("## Règles")
    L.append("")
    L.append("- Tous les prix sont en **cuivres** : 1 argent = 10 · 1 or = 100 · 1 diamant = 1 000 · 1 zénith = 10 000 · "
             "1 astrale = 100 000 (`CoinWallet`, 1 « or » du compte en banque = 1 cuivre).")
    L.append("- **fixé** : objet sans recette, prix saisi dans `prix.json` → `ressources`.")
    L.append(f"- **calculé** : objet avec recette = somme des ingrédients × **{eco.marge}** (marge de l'artisan) ÷ quantité "
             "produite, arrondi à 2 chiffres significatifs. Changer le prix d'une ressource recalcule tout ce qui en dépend.")
    L.append("- **forcé** : prix imposé dans `prix.json` → `forces` (remplace le calcul ; marche aussi pour un objet hors économie "
             "qu'on veut vendre).")
    L.append("- `[Type]` dans une recette : n'importe quel objet de ce type de ressource ; il vaut le moins cher d'entre eux "
             "(voir la table des types en fin de document).")
    L.append("- Repères : épée en fer ≈ 20 or ; lingot de fer divin ≈ 1 000 000 (10 astrales), la ressource la plus chère.")
    L.append("- Boutiques : rachat d'un objet porté = ¼ du prix ; taxe de commerce = 10 % du prix payé, versée à la zone.")
    L.append("- Colonne **O** : V = vanilla, E = Eldanior. « (jeu) » : prix modifié en jeu avec `/es prix` "
             "(fichier `prix_jeu.json` du serveur ; `--fusionner` le recopie dans `prix.json`).")
    L.append("")
    L.append("## Sommaire")
    L.append("")
    L.append("| Famille | Sans recette (fixé) | Avec recette (calculé) | Total |")
    L.append("|---|---:|---:|---:|")
    fams = [f for f in FAMILIES if f in by_fam] + sorted(set(by_fam) - set(FAMILIES))
    for fam in fams:
        g = by_fam[fam]
        L.append(f"| [{fam}](#{anchor(fam)}) | {len(g['raw'])} | {len(g['craft'])} | {len(g['raw']) + len(g['craft'])} |")
    L.append(f"| [{OUT_OF_ECONOMY}](#{anchor(OUT_OF_ECONOMY)}) | | | {len(excluded)} |")
    if todo:
        L.append(f"| [À fixer](#fam-a-fixer) | | | {len(todo)} |")
    L.append("")
    if todo:
        L.append('<a id="fam-a-fixer"></a>')
        L.append("## À fixer")
        L.append("")
        L.append("Objets sans prix : ressource absente de `prix.json` (relancer avec `--init` pour une estimation), ou recette "
                 "dont un ingrédient n'a pas de prix.")
        L.append("")
        L.append("| ID | Nom | Raison |")
        L.append("|---|---|---|")
        for iid in sorted(todo, key=str.lower):
            L.append(f"| `{iid}` | {item_name(eco.items, eco.lang, iid)} | {eco.why.get(iid, 'pas dans prix.json')} |")
        L.append("")
    if stale:
        L.append("## Entrées obsolètes de prix.json")
        L.append("")
        L.append("Ces identifiants n'existent plus : " + ", ".join(f"`{s}`" for s in stale))
        L.append("")

    head = "| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O |"
    sep = "|---|---|---|---:|---:|---|---|---|"
    for fam in fams:
        g = by_fam[fam]
        L.append(f'<a id="{anchor(fam)}"></a>')
        L.append(f"## {fam}")
        L.append("")
        if g["raw"]:
            L.append(f"### Sans recette — prix fixés ({len(g['raw'])})")
            L.append("")
            L.append(head)
            L.append(sep)
            L.extend(row(eco, i, False) for i in g["raw"])
            L.append("")
        if g["craft"]:
            L.append(f"### Avec recette — prix calculés ({len(g['craft'])})")
            L.append("")
            L.append(head[:-1] + " Recette |")
            L.append(sep + "---|")
            L.extend(row(eco, i, True) for i in g["craft"])
            L.append("")

    L.append('<a id="fam-types-de-ressource"></a>')
    L.append("## Types de ressource")
    L.append("")
    L.append("| Type | Nom | Prix | Objet le moins cher | Nb d'objets |")
    L.append("|---|---|---:|---|---:|")
    for rt in sorted(eco.types, key=str.lower):
        best = eco.type_cheapest(rt)
        p = eco.types_override.get(rt, best[0] if best else None)
        L.append(f"| `{rt}` | {clean(eco.lang.get(f'resourceType.{rt}.name', rt))} | {num(p) if p is not None else '?'} | "
                 f"{('`' + best[1] + '`') if best else ''} | {len(eco.types[rt])} |")
    L.append("")
    L.append(f'<a id="{anchor(OUT_OF_ECONOMY)}"></a>')
    L.append(f"## {OUT_OF_ECONOMY}")
    L.append("")
    L.append("Objets techniques (debug, éditeur, effets visuels, prototypes, gabarits) : listés pour être complet, jamais vendus.")
    L.append("")
    L.append("| ID | Nom | Raison | O |")
    L.append("|---|---|---|---|")
    for iid in excluded:
        L.append(f"| `{iid}` | {item_name(eco.items, eco.lang, iid)} | {eco.excl[iid]} | "
                 f"{'E' if eco.items[iid]['origin'] == 'Eldanior' else 'V'} |")
    L.append("")
    return "\n".join(L), len(todo)


def main():
    items = load_items()
    lang = load_lang()
    prix = load_prix()
    eco = Economy(items, lang, prix)
    if "--init" in sys.argv:
        added = init_prix(eco, prix)
        print(f"prix.json : {added} estimation(s) ajoutée(s)")
        prix = load_prix()
    game = load_game()
    if "--fusionner" in sys.argv and (game["ressources"] or game["forces"]):
        merge_game(prix, game, items)
        print(f"prix.json : {len(game['ressources']) + len(game['forces'])} prix du jeu recopiés "
              f"(vous pouvez maintenant supprimer {os.path.relpath(GAME, ROOT)})")
        game = {"ressources": {}, "forces": {}}
    export_runtime(Economy(items, lang, prix))
    eco = Economy(items, lang, with_game(prix, game))
    eco.game = set(game["ressources"]) | set(game["forces"])
    doc, todo = build_doc(eco)
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with open(OUT, "w", encoding="utf-8") as f:
        f.write(doc)
    print(f"écrit {os.path.relpath(OUT, ROOT)} : {len(items)} objets, {todo} sans prix"
          + (f", {len(eco.game)} prix modifiés en jeu" if eco.game else "") + f" ; {os.path.relpath(RUNTIME, ROOT)}")


if __name__ == "__main__":
    main()
