"""Catalogue des armes d'Eldanior (11 familles x 23 armes) : genere les JSON d'items, verifie les references, imprime le recap.

Une famille = une base vanilla (template + arme en fer de reference) ; une arme = rarete, multiplicateur de degats par
rapport au fer vanilla de la famille, modele vanilla (provisoire pour les artefacts) et effet eventuel.

Raretes (tableau valide le 2026-10-26) :
    Commune  Common     ItemLevel 5-25    x0,8-1,1   Eldanior_Weapon_
    Rare     Rare       ItemLevel 60-100  x1,4       Eldanior_Weapon_
    Epique   Epic       ItemLevel 150-220 x2         Eldanior_Weapon_
    Unique   Unique     ItemLevel 300-400 x2,8       Artefact_
    Legend.  Legendary  ItemLevel 500-600 x3,8       Artefact_   (restriction de classe, MasterySystem.ARTIFACT_CLASSES)
    Mythique Divin      ItemLevel 800     x5         Artefact_   (restriction de classe, signature)
Toutes les armes heritent directement du template vanilla de la famille (pas de l'arme vanilla elle-meme : elle porterait\nsa recette ; un seul niveau d'heritage, comme les artefacts existants).

Usage : python3 scripts/artefacts/weapon_catalog.py <famille|all> [--check]
    --check : verifie seulement (rien n'est ecrit).
"""
import json, os, sys, zipfile

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..")
RES = os.path.join(ROOT, "src", "main", "resources")
ITEMS = os.path.join(RES, "Server", "Item", "Items", "Weapon")
ASSETS = os.path.expanduser("~/Library/Application Support/Hytale/install/release/package/game/latest/Assets.zip")

QUALITY = {"Commune": "Common", "Rare": "Rare", "Epique": "Epic", "Unique": "Unique", "Legendaire": "Legendary", "Mythique": "Divin"}
DURABILITY = {"Rare": 350, "Epique": 450, "Unique": 600, "Legendaire": 0, "Mythique": 0}
ARTEFACT = ("Unique", "Legendaire", "Mythique")

# Ordre des tests de MasterySystem.getWeaponMastery (le premier mot-cle trouve gagne)
MASTERY_KEYWORDS = [("sword", "SWORD"), ("bow", "BOW"), ("dagger", "DAGGER"), ("staff", "STAFF"), ("wand", "STAFF"),
                    ("axe", "AXE"), ("shield", "SHIELD"), ("spear", "SPEAR"), ("spellbook", "SPELLBOOK"), ("flail", "FLAIL"),
                    ("mace", "MACE"), ("gun", "GUN"), ("club", "CLUB"), ("rifle", "RIFLE"), ("riffle", "RIFLE")]

# Loot (chance en % par arme et par table) : tableau valide le 2026-10-26
LOOT = {
    "Commune":    {"DUNGEON_TIER1_CHEST": 1.0, "DUNGEON_COMMON_CHEST": 1.0},
    "Rare":       {"DUNGEON_TIER2_CHEST": 0.6, "DUNGEON_TIER3_CHEST": 0.6},
    "Epique":     {"DUNGEON_TIER3_CHEST": 0.3, "DUNGEON_TIER4_CHEST": 0.3},
    "Unique":     {"DUNGEON_TIER4_CHEST": 0.1, "DUNGEON_BOSS_CHEST": 0.2},
    "Legendaire": {"DUNGEON_CHEST": 0.02, "DUNGEON_TIER4_CHEST": 0.03, "DUNGEON_BOSS_CHEST": 0.08},
    "Mythique":   {"DUNGEON_CHEST": 0.005, "DUNGEON_BOSS_CHEST": 0.02},
}

# ---------------------------------------------------------------- familles ----------------------------------------------------------------
FAMILIES = {
    "Sword": {
        "folder": "Sword", "keyword": "sword", "template": "Template_Weapon_Sword", "reference": "Weapon_Sword_Iron",
        "vanilla_dir": "Server/Item/Items/Weapon/Sword/",
        "damage_vars": ["Swing_Left_Damage", "Swing_Right_Damage", "Swing_Down_Damage", "Thrust_Damage",
                        "Vortexstrike_Spin_Damage", "Vortexstrike_Stab_Damage"],
        "guard_var": "Guard_Wield",
        "impact_sound": "SFX_Sword_T2_Impact",
        "classes": "Guerrier et Dragon Ancestral",
        "weapons": [
            # ---- Mythique ----
            dict(id="Artefact_Sword_Excalibur", name="Excalibur, Lame du Créateur", rarity="Mythique", level=800, mult=5.0,
                 model="Mithril", trail="Excalibur_Trail",
                 interactions={"Ability1": "Root_Excalibur_Signature"},
                 desc="Artefact divin. Une épée divine qui purifie les ténèbres : ses coups infligent +50 % de dégâts aux morts-vivants et aux créatures du Néant. "
                      "Signature, Jugement d'Avalon : un pilier de lumière s'abat devant le porteur, foudroie les ennemis proches, les éblouit et soigne le porteur. "
                      "Guerriers et Dragons."),
            # ---- Legendaires ----
            dict(id="Artefact_Sword_Gram", name="Gram, Tueuse de Dragons", rarity="Legendaire", level=560, mult=3.8,
                 model="Doomed", trail="Large_Charged_Red",
                 desc="Artefact légendaire. Transperce les armures des créatures colossales : +60 % de dégâts contre les dragons et les créatures géantes. Guerriers et Dragons."),
            dict(id="Artefact_Sword_Solarys", name="Solarys, Épée du Soleil", rarity="Legendaire", level=540, mult=3.8,
                 model="Adamantite", trail="Medium_Flame", next="Eldanior_OnHit_Embrasement",
                 particles=[{"SystemId": "FireSword", "PositionOffset": {"Y": 0.4}}],
                 desc="Artefact légendaire. Embrase les ennemis à chaque frappe (6 dégâts de feu par seconde pendant 3 s). Guerriers et Dragons."),
            # ---- Uniques ----
            dict(id="Artefact_Sword_Masamune", name="Masamune", rarity="Unique", level=380, mult=2.8,
                 model="Silversteel", trail="Critical",
                 desc="Artefact unique. Une lame d'une précision exceptionnelle : +15 % de chance de coup critique."),
            dict(id="Artefact_Sword_Croc_Du_Neant", name="Croc du Néant", rarity="Unique", level=360, mult=2.8,
                 model="Onyxium", trail="Void",
                 stats_on_hit={"Health": 3},
                 desc="Artefact unique. Absorbe une partie de l'énergie des ennemis : chaque coup porté rend 3 points de vie au porteur."),
            dict(id="Artefact_Sword_Roi_Dechu", name="Lame du Roi Déchu", rarity="Unique", level=340, mult=2.8,
                 model="Bronze_Ancient",
                 desc="Artefact unique. Inflige davantage de dégâts aux adversaires affaiblis : +40 % contre une cible sous la moitié de ses points de vie."),
            # ---- Epiques ----
            dict(id="Eldanior_Weapon_Sword_Givre", name="Lame du Givre", rarity="Epique", level=200, mult=2.0,
                 model="Frost", copy_visuals=True, next="Eldanior_OnHit_Givre",
                 desc="Une lame gelée qui ralentit les ennemis à chaque coup (−40 % de vitesse pendant 2,5 s)."),
            dict(id="Eldanior_Weapon_Sword_Tempetes", name="Épée des Tempêtes", rarity="Epique", level=210, mult=2.0,
                 model="Cobalt", trail="Medium_Charged", next="Eldanior_OnHit_Choc", elemental={"Lightning": 4},
                 impact_particles=["Taranis_Electric_Burst"],
                 desc="Libère des arcs électriques : chaque coup ajoute 4 dégâts de foudre et paralyse brièvement la cible."),
            dict(id="Eldanior_Weapon_Sword_Croc_Wyverne", name="Croc de Wyverne", rarity="Epique", level=190, mult=2.0,
                 model="Nexus", trail="Void_Green", next="Eldanior_OnHit_Venin",
                 desc="Applique un poison léger à chaque coup (3 dégâts par seconde pendant 5 s)."),
            dict(id="Eldanior_Weapon_Sword_Sabre_Vent", name="Sabre du Vent", rarity="Epique", level=180, mult=1.8,
                 model="Thorium", trail="Ability_Windstrike_Trail", next="Eldanior_OnHit_Vent", signature_per_hit=3,
                 desc="Favorise les attaques rapides : chaque coup porté allège le porteur (+20 % de vitesse pendant 3 s) et charge sa signature trois fois plus vite."),
            # ---- Rares ----
            dict(id="Eldanior_Weapon_Sword_Croissant", name="Épée du Croissant", rarity="Rare", level=80, mult=1.4,
                 model="Cutlass", desc="Une lame courbe équilibrée."),
            dict(id="Eldanior_Weapon_Sword_Gardien", name="Épée du Gardien", rarity="Rare", level=90, mult=1.4,
                 model="Steel", guard=6,
                 desc="Améliore les capacités défensives : parer coûte 40 % d'endurance en moins."),
            dict(id="Eldanior_Weapon_Sword_Braise", name="Lame de Braise", rarity="Rare", level=85, mult=1.4,
                 model="Steel_Incandescent", copy_visuals=True, elemental={"Fire": 3},
                 desc="Inflige des dégâts de feu modérés (+3 dégâts de feu par coup)."),
            dict(id="Eldanior_Weapon_Sword_Sabre_Vagabond", name="Sabre du Vagabond", rarity="Rare", level=70, mult=1.3,
                 model="Bone", signature_per_hit=2,
                 desc="Léger et maniable : sa signature se charge deux fois plus vite."),
            dict(id="Eldanior_Weapon_Sword_Runique", name="Épée runique", rarity="Rare", level=100, mult=1.4,
                 model="Runic", signature_start=40,
                 desc="Gravée de symboles magiques : l'énergie de signature démarre à 40 %."),
            # ---- Communes ----
            dict(id="Eldanior_Weapon_Sword_Fer", name="Épée en fer", rarity="Commune", level=20, mult=1.0, model="Iron", durability=250,
                 desc="Une épée en fer solide et fiable."),
            dict(id="Eldanior_Weapon_Sword_Courte", name="Épée courte", rarity="Commune", level=10, mult=0.8, model="Copper", durability=200,
                 desc="Une lame courte, facile à manier."),
            dict(id="Eldanior_Weapon_Sword_Entrainement", name="Épée d'entraînement", rarity="Commune", level=5, mult=0.8, model="Wood", durability=120,
                 desc="Une épée de bois pour apprendre les bases du combat."),
            dict(id="Eldanior_Weapon_Sword_Sabre_Soldat", name="Sabre de soldat", rarity="Commune", level=18, mult=0.95, model="Cutlass", durability=220,
                 desc="Le sabre réglementaire des soldats d'Eldanior."),
            dict(id="Eldanior_Weapon_Sword_Bronze", name="Épée de bronze", rarity="Commune", level=15, mult=0.9, model="Bronze", durability=200,
                 desc="Une épée de bronze à l'ancienne."),
            dict(id="Eldanior_Weapon_Sword_Lame_Garde", name="Lame de garde", rarity="Commune", level=22, mult=1.05, model="Steel", durability=240,
                 desc="La lame d'acier des gardes de la cité."),
            dict(id="Eldanior_Weapon_Sword_Fer_Usee", name="Épée de fer usée", rarity="Commune", level=12, mult=0.85, model="Steel_Rusty", durability=120,
                 desc="Une vieille épée de fer, rongée par la rouille."),
            dict(id="Eldanior_Weapon_Sword_Mercenaire", name="Épée de mercenaire", rarity="Commune", level=25, mult=1.1, model="Scrap", durability=250,
                 desc="Une lame lourde et grossière, prisée des mercenaires."),
        ],
    },
}

# Artefacts deja existants integres au catalogue (non generes ici)
EXISTING = {"Spear": ["Artefact_Gungnir"]}


# ---------------------------------------------------------------- assets ----------------------------------------------------------------
class Assets:
    """Index des assets vanilla (Assets.zip) et du mod (src/main/resources) : noms de fichiers sans extension."""

    def __init__(self):
        self.zip = zipfile.ZipFile(ASSETS)
        self.vanilla = set(self.zip.namelist())
        self.mod = set()
        for d, _, fs in os.walk(RES):
            for f in fs:
                self.mod.add(os.path.relpath(os.path.join(d, f), RES).replace(os.sep, "/"))
        self.names = {}
        for p in self.vanilla | self.mod:
            self.names.setdefault(os.path.splitext(os.path.basename(p))[0], []).append(p)

    def has_id(self, asset_id, prefix):
        return any(p.startswith(prefix) for p in self.names.get(asset_id, []))

    def has_path(self, path):
        return ("Common/" + path) in self.vanilla or ("Common/" + path) in self.mod

    def vanilla_json(self, path):
        return json.loads(self.zip.read(path))


def mastery_of(item_id):
    low = item_id.lower()
    for kw, m in MASTERY_KEYWORDS:
        if kw in low:
            return m
    return "ANY"


# ---------------------------------------------------------------- generation ----------------------------------------------------------------
def build(fam, w, assets):
    ref = assets.vanilla_json(fam["vanilla_dir"] + fam["reference"] + ".json")
    src = assets.vanilla_json(fam["vanilla_dir"] + fam["reference"].rsplit("_", 1)[0] + "_" + w["model"] + ".json")
    artefact = w["rarity"] in ARTEFACT
    item = {"Parent": fam["template"],
            "TranslationProperties": {"Name": w["name"], "Description": w["desc"]},
            "Quality": QUALITY[w["rarity"]], "ItemLevel": w["level"],
            "Model": src["Model"], "Texture": src["Texture"], "Icon": src["Icon"]}
    if "IconProperties" in src:
        item["IconProperties"] = src["IconProperties"]
    item["Categories"] = ["Eldanior_System.Artefacts", "Items.Weapons"] if artefact else ["Items.Weapons"]
    if artefact:
        item["MaxStack"] = 1
        item["Variant"] = False
    if "interactions" in w:
        item["Interactions"] = dict(w["interactions"])

    variables = {}
    for var in fam["damage_vars"]:
        base = ref["InteractionVars"][var]["Interactions"][0]
        dmg = {k: max(1, round(v * w["mult"])) for k, v in base["DamageCalculator"]["BaseDamage"].items()}
        dmg.update(w.get("elemental", {}))
        entry = {"Parent": base["Parent"], "DamageCalculator": {"BaseDamage": dmg},
                 "DamageEffects": {"WorldSoundEventId": fam["impact_sound"], "LocalSoundEventId": fam["impact_sound"]}}
        if w.get("impact_particles"):
            entry["DamageEffects"]["WorldParticles"] = [{"SystemId": "Impact_Sword_Basic_New", "Scale": 0.7}] + \
                                                       [{"SystemId": p, "Scale": 0.5} for p in w["impact_particles"]]
        on_hit = []
        if w.get("signature_per_hit"):
            on_hit.append({"EntityStatId": "SignatureEnergy", "Amount": w["signature_per_hit"]})
        for stat, amount in w.get("stats_on_hit", {}).items():
            on_hit.append({"EntityStatId": stat, "Amount": amount})
        if on_hit:
            if not any(s["EntityStatId"] == "SignatureEnergy" for s in on_hit):
                on_hit.insert(0, {"EntityStatId": "SignatureEnergy", "Amount": 1})
            entry["EntityStatsOnHit"] = on_hit
        if w.get("next"):
            entry["Next"] = w["next"]
        variables[var] = {"Interactions": [entry]}
    guard = ref["InteractionVars"][fam["guard_var"]]["Interactions"][0]
    variables[fam["guard_var"]] = {"Interactions": [{"Parent": guard["Parent"], "StaminaCost": {
        "Value": w.get("guard", guard["StaminaCost"]["Value"]), "CostType": guard["StaminaCost"]["CostType"]}}]}
    item["InteractionVars"] = variables

    item["MaxDurability"] = w["durability"] if w["rarity"] == "Commune" else DURABILITY[w["rarity"]]
    if item["MaxDurability"] == 0:
        item["DurabilityLossOnHit"] = 0
    if w.get("signature_start"):
        item["Weapon"] = {"EntityStatsToClear": ["SignatureEnergy"],
                          "StatModifiers": {"SignatureEnergy": [{"Amount": w["signature_start"], "CalculationType": "Additive"}]}}
    if w.get("copy_visuals"):
        for k in ("Trails", "Particles"):
            if k in src:
                item[k] = src[k]
    if w.get("trail"):
        item["Trails"] = [{"TrailId": w["trail"], "TargetNodeName": "Handle", "TargetEntityPart": "PrimaryItem",
                           "PositionOffset": {"X": 0.8, "Y": 0}}]
    if w.get("particles"):
        item["Particles"] = w["particles"]
    return item


# ---------------------------------------------------------------- verifications ----------------------------------------------------------------
def check(fam, items, assets):
    errors = []
    expected = fam["keyword"].upper() if fam["keyword"] != "wand" else "STAFF"
    for wid, item in items.items():
        m = mastery_of(wid)
        if m != expected:
            errors.append(f"{wid} : maitrise detectee {m}, attendu {expected}")
        for k in ("Model", "Texture", "Icon"):
            if not assets.has_path(item[k]):
                errors.append(f"{wid} : {k} introuvable ({item[k]})")
        for var in item["InteractionVars"].values():
            for it in var["Interactions"]:
                if not assets.has_id(it["Parent"], "Server/Item/Interactions/"):
                    errors.append(f"{wid} : interaction parente introuvable {it['Parent']}")
                if "Next" in it and not assets.has_id(it["Next"], "Server/Item/Interactions/"):
                    errors.append(f"{wid} : interaction Next introuvable {it['Next']}")
                for p in it.get("DamageEffects", {}).get("WorldParticles", []):
                    if not assets.has_id(p["SystemId"], "Server/Particles/") and not any(
                            x.endswith(".particlesystem") for x in assets.names.get(p["SystemId"], [])):
                        errors.append(f"{wid} : particule introuvable {p['SystemId']}")
        for t in item.get("Trails", []):
            if not assets.has_id(t["TrailId"], "Server/Entity/Trails/"):
                errors.append(f"{wid} : trail introuvable {t['TrailId']}")
        for p in item.get("Particles", []):
            if not any(x.endswith(".particlesystem") for x in assets.names.get(p["SystemId"], [])):
                errors.append(f"{wid} : particule introuvable {p['SystemId']}")
        for k, v in item.get("Interactions", {}).items():
            if not assets.has_id(v, "Server/Item/RootInteractions/"):
                errors.append(f"{wid} : RootInteraction introuvable {v}")
    return errors


def recap(fam, items):
    print(f"\n| ID | Nom | Rareté | Niv. | Dégâts (G/D · bas · estoc · Vortex) | Effet | Modèle | Loot |")
    print("|---|---|---|---|---|---|---|---|")
    for w in fam["weapons"]:
        it = items[w["id"]]
        d = [list(v["Interactions"][0]["DamageCalculator"]["BaseDamage"].values())[0]
             for k, v in it["InteractionVars"].items() if "DamageCalculator" in v["Interactions"][0]]
        loot = ", ".join(f"{t.replace('DUNGEON_', '').replace('_CHEST', '').lower()} {c} %" for t, c in LOOT[w["rarity"]].items())
        print(f"| `{w['id']}` | {w['name']} | {w['rarity']} | {w['level']} | {d[0]}/{d[1]} · {d[2]} · {d[3]} · {d[4]}/{d[5]} | "
              f"{w['desc'][:60]}… | {w['model']} | {loot} |")


def loot_lines(fam):
    by_table = {}
    for w in fam["weapons"]:
        for table, chance in LOOT[w["rarity"]].items():
            by_table.setdefault(table, []).append((w["id"], chance))
    for table, entries in by_table.items():
        print(f"\n// {table}")
        for wid, chance in entries:
            print(f'            new LootEntry("{wid}", 1, 1, 1, {chance}),')


def main():
    fams = list(FAMILIES) if sys.argv[1] == "all" else [sys.argv[1]]
    only_check = "--check" in sys.argv
    assets = Assets()
    failed = False
    for name in fams:
        fam = FAMILIES[name]
        items = {w["id"]: build(fam, w, assets) for w in fam["weapons"]}
        if not only_check:
            folder = os.path.join(ITEMS, fam["folder"])
            os.makedirs(folder, exist_ok=True)
            for wid, item in items.items():
                with open(os.path.join(folder, wid + ".json"), "w", encoding="utf-8") as f:
                    json.dump(item, f, indent=2, ensure_ascii=False)
                    f.write("\n")
            assets = Assets()                                                 # re-indexe avec les fichiers ecrits
        errors = check(fam, items, assets)
        for e in errors:
            print("ERREUR", e)
        failed |= bool(errors)
        print(f"{name} : {len(items)} armes, {len(errors)} erreur(s)")
        recap(fam, items)
        loot_lines(fam)
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
