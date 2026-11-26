"""Potions d'Eldanior : memes bouteilles et meme fonctionnement que les potions du jeu.

Chaque potion herite de Potion_Template (animation de boisson, sons, pose au sol) et garde un des 5 modeles vanilla selon sa
taille. Son design propre est le liquide : les pixels de liquide des textures et icones vanilla sont reperes par difference entre
deux couleurs vanilla, puis recolores en degrade a deux teintes propre a la potion.

Usage : python3 scripts/consommables/gen_potions.py <dossier Assets Hytale> [sheet <dossier>]
"""
import json, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "butin"))
sys.path.insert(0, os.path.join(HERE, "..", "rune_aura_icons"))
import png  # noqa: E402
from sprites import rgb, mix  # noqa: E402
from gen_butin import slug  # noqa: E402

RES = os.path.join(HERE, "..", "..", "src", "main", "resources")
SIZES = {"Lesser": ("Potion_Lesser", "Lesser"), "Small": ("Potion_Small", "Small"), "Normal": ("Potion_Normal", ""),
         "Greater": ("Potion", "Greater"), "Large": ("Potion_Large", "Large")}
EMPTY = {"Lesser": "Potion_Empty_Small", "Small": "Potion_Empty_Small", "Normal": "Potion_Empty", "Greater": "Potion_Empty", "Large": "Potion_Empty_Large"}


def apply(*ids):
    node = None
    for e in reversed(ids):
        node = {"Type": "ApplyEffect", "EffectId": e, **({"Next": node} if node else {})}
    return {"Interactions": [node]}


def clear_poison():
    node = {"Type": "ApplyEffect", "EffectId": "Antidote"}
    for e in ("Ability_Poison_Stack_C", "Ability_Poison_Stack_B", "Ability_Poison_Stack_A", "Poison_T3", "Poison_T2", "Poison_T1"):
        node = {"Type": "ClearEntityEffect", "EntityEffectId": e, "Next": node}
    return {"Interactions": [node]}


# id, nom, description, taille, teinte sombre, teinte vive, qualite, niveau, boire (health|stamina|mana|buff), effet, ingredients, palier d'etabli
H, S, M, B = "health", "stamina", "mana", "buff"
POTIONS = [
    ("Vie", "Potion de Vie", "Boire : rend 25 % de vie, puis 35 % en 5 s", "Normal", "#7a0f24", "#ff4a5a", "Uncommon", 100, H,
     apply("Potion_Health_Instant", "Potion_Health_Regen"), ["Coeur de Loup", "Seve Spirituelle"], 1),
    ("Vie_Grande", "Grande Potion de Vie", "Boire : rend 25 % de vie, puis 50 % en 5 s", "Greater", "#8a0a4a", "#ff5a9a", "Rare", 300, H,
     apply("Potion_Health_Instant_Greater", "Potion_Health_Regen_Greater"), ["Coeur d'Ours", "Seve Spirituelle"], 2),
    ("Vie_Supreme", "Potion de Vie Suprême", "Boire : rend 35 % de vie, puis 55 % en 5 s", "Large", "#a01818", "#ffc24a", "Epic", 600, H,
     apply("Potion_Health_Instant_Large", "Potion_Health_Regen_Large"), ["Coeur de Yeti", "Ame des Racines"], 3),
    ("Regeneration", "Baume de Régénération", "Boire : rend 35 % de vie en 5 s", "Small", "#1f7a3a", "#a8f06a", "Uncommon", 100, H,
     apply("Potion_Health_Regen"), ["Pulpe de Cactee", "Seve Spirituelle"], 1),
    ("Mana_Petite", "Petite Potion de Mana", "Boire : rend 20 % de mana", "Small", "#1a3fa0", "#5ab0ff", "Common", 50, M,
     apply("Eld_Effet_Mana_Petit"), ["Flocon Spirituel"], 1),
    ("Mana", "Potion de Mana", "Boire : rend 40 % de mana", "Normal", "#2a1fa8", "#6a7aff", "Uncommon", 200, M,
     apply("Eld_Effet_Mana"), ["Ame de Givre"], 2),
    ("Mana_Grande", "Grande Potion de Mana", "Boire : rend 70 % de mana", "Large", "#4a1aa8", "#a07aff", "Rare", 500, M,
     apply("Eld_Effet_Mana_Grand"), ["Noyau de Givre"], 3),
    ("Mana_Vive", "Élixir de Mana Vive", "Boire : rend 8 % de mana par seconde pendant 10 s", "Greater", "#0f6a8a", "#5af0e8", "Epic", 600, M,
     apply("Eld_Effet_Mana_Vive"), ["Ame de Foudre", "Larme Gelee"], 3),
    ("Tonique", "Tonique du Coureur", "Boire : rend 60 % d'endurance", "Small", "#a06a0a", "#ffe04a", "Common", 50, S,
     apply("Potion_Stamina_Instant"), ["Glande de Crapaud"], 1),
    ("Fortifiant", "Fortifiant du Marcheur", "Boire : régénère l'endurance pendant 15 s", "Normal", "#a0500a", "#ffa63a", "Uncommon", 100, S,
     apply("Potion_Stamina_Regen"), ["Graisse de Baleine"], 2),
    ("Antidote", "Antidote Ancien", "Boire : soigne tous les poisons", "Lesser", "#3a6a0f", "#c8f03a", "Uncommon", 50, B,
     clear_poison(), ["Glande a Venin", "Soie d'Araignee"], 1),
    ("Ignifuge", "Potion Ignifuge", "Boire : immunité au feu pendant 3 min", "Normal", "#8a1f0a", "#ff7a2a", "Rare", 300, B,
     apply("Eld_Effet_Ignifuge"), ["Ecaille de Lezard", "Bave de Magma"], 2),
    ("Antigel", "Potion Antigel", "Boire : forte résistance au froid pendant 3 min", "Normal", "#2a7a9a", "#c8f8ff", "Rare", 300, B,
     apply("Eld_Effet_Antigel"), ["Branchie Gelee", "Perle de Givre"], 2),
    ("Plume", "Potion de Plume", "Boire : immunité aux chutes pendant 3 min", "Lesser", "#8a8aa8", "#f4f4ff", "Uncommon", 200, B,
     apply("Eld_Effet_Plume"), ["Plume d'Archeopteryx"], 2),
    ("Bouclier", "Potion de Bouclier", "Boire : immunité aux projectiles pendant 1 min", "Greater", "#3a4a6a", "#a8c0d8", "Epic", 500, B,
     apply("Eld_Effet_Bouclier"), ["Plaque Royale"], 3),
    ("Rage", "Potion de Rage", "Boire : +25 % de dégâts physiques pendant 2 min", "Normal", "#3a0808", "#e02a1a", "Epic", 500, B,
     apply("Eld_Effet_Rage"), ["Griffe de Tigre", "Croc de Braise"], 3),
    ("Peau_Pierre", "Potion de Peau de Pierre", "Boire : -25 % de dégâts physiques subis pendant 2 min", "Large", "#4a4238", "#b8a888", "Epic", 500, B,
     apply("Eld_Effet_Peau_Pierre"), ["Pierre Vive", "Carapace de Scorpion"], 3),
    ("Celerite", "Potion de Célérité", "Boire : +30 % de vitesse pendant 2 min", "Small", "#0f7a6a", "#5affb0", "Rare", 300, B,
     apply("Eld_Effet_Celerite"), ["Griffe de Raptor"], 3),
    ("Precision", "Potion de Précision", "Boire : +20 % de chance de critique pendant 2 min", "Greater", "#6a5a0a", "#f0f05a", "Epic", 500, B,
     apply("Eld_Effet_Precision"), ["Oeil de Leopard"], 3),
    ("Force_Vitale", "Potion de Force Vitale", "Boire : +20 % de vie max pendant 5 min", "Large", "#7a1a5a", "#ff7ad0", "Epic", 700, B,
     apply("Eld_Effet_Force_Vitale"), ["Coeur de Rex"], 3),
    ("Arcane", "Potion d'Arcane", "Boire : +25 % de dégâts élémentaires pendant 2 min", "Greater", "#2a0f6a", "#c05aff", "Epic", 600, B,
     apply("Eld_Effet_Arcane"), ["Ame du Vide"], 3),
    ("Sagesse", "Potion de Sagesse", "Boire : +25 % d'XP sur les monstres pendant 30 min", "Normal", "#0f3a6a", "#ffd86a", "Epic", 600, B,
     apply("Eld_Effet_Sagesse"), ["Pupille du Vide"], 3),
    ("Fortune", "Potion de Fortune", "Boire : second tirage de butin pendant 30 min", "Greater", "#7a5a0a", "#7affa0", "Legendary", 700, B,
     apply("Eld_Effet_Fortune"), ["Rire Fige"], 3),
]


def icon_of(pid):
    return "Icons/ItemsGenerated/Eld_Potion_%s.png" % pid


EFFECTS = {  # effet : (contenu, potion dont l'icone s'affiche)
    "Eld_Effet_Mana_Petit": ({"StatModifiers": {"Mana": 20}, "ValueType": "Percent", "OverlapBehavior": "Overwrite", "Duration": 0.1, "DamageCalculatorCooldown": 0}, None),
    "Eld_Effet_Mana": ({"StatModifiers": {"Mana": 40}, "ValueType": "Percent", "OverlapBehavior": "Overwrite", "Duration": 0.1, "DamageCalculatorCooldown": 0}, None),
    "Eld_Effet_Mana_Grand": ({"StatModifiers": {"Mana": 70}, "ValueType": "Percent", "OverlapBehavior": "Overwrite", "Duration": 0.1, "DamageCalculatorCooldown": 0}, None),
    "Eld_Effet_Mana_Vive": ({"StatModifiers": {"Mana": 8}, "ValueType": "Percent", "DamageCalculatorCooldown": 1, "Duration": 10.05, "OverlapBehavior": "Overwrite"}, "Mana_Vive"),
    "Eld_Effet_Ignifuge": ({"DamageResistance": {"Fire": [{"Amount": 1.0, "CalculationType": "Percent"}]}, "Duration": 180, "OverlapBehavior": "Overwrite"}, "Ignifuge"),
    "Eld_Effet_Antigel": ({"DamageResistance": {"Ice": [{"Amount": 0.75, "CalculationType": "Percent"}]}, "Duration": 180, "OverlapBehavior": "Overwrite"}, "Antigel"),
    "Eld_Effet_Plume": ({"DamageResistance": {"Fall": [{"Amount": 1.0, "CalculationType": "Percent"}]}, "Duration": 180, "OverlapBehavior": "Overwrite"}, "Plume"),
    "Eld_Effet_Bouclier": ({"DamageResistance": {"Projectile": [{"Amount": 0.95, "CalculationType": "Percent"}]}, "Duration": 60, "OverlapBehavior": "Overwrite"}, "Bouclier"),
    "Eld_Effet_Rage": ({"OutgoingDamage": {"Physical": [{"CalculationType": "Percent", "Amount": 0.25}]}, "Duration": 120, "OverlapBehavior": "Overwrite"}, "Rage"),
    "Eld_Effet_Peau_Pierre": ({"DamageResistance": {"Physical": [{"Amount": 0.25, "CalculationType": "Percent"}]}, "Duration": 120, "OverlapBehavior": "Overwrite"}, "Peau_Pierre"),
    "Eld_Effet_Celerite": ({"ApplicationEffects": {"HorizontalSpeedMultiplier": 1.3}, "Duration": 120, "OverlapBehavior": "Overwrite"}, "Celerite"),
    "Eld_Effet_Precision": ({"Duration": 120, "OverlapBehavior": "Overwrite"}, "Precision"),
    "Eld_Effet_Force_Vitale": ({"RawStatModifiers": {"Health": [{"Amount": 1.2, "CalculationType": "Multiplicative", "Target": "Max"}]}, "Duration": 300, "OverlapBehavior": "Overwrite"}, "Force_Vitale"),
    "Eld_Effet_Arcane": ({"OutgoingDamage": {"Elemental": [{"CalculationType": "Percent", "Amount": 0.25}]}, "Duration": 120, "OverlapBehavior": "Overwrite"}, "Arcane"),
    "Eld_Effet_Sagesse": ({"Duration": 1800, "OverlapBehavior": "Overwrite"}, "Sagesse"),
    "Eld_Effet_Fortune": ({"Duration": 1800, "OverlapBehavior": "Overwrite"}, "Fortune"),
}


def recolor(base, other, a, b):
    """Recolore les pixels de liquide (ceux qui different entre deux couleurs vanilla) d'apres leur luminosite."""
    A, B = rgb(a), rgb(b)
    out = []
    for row, row2 in zip(base, other):
        r2 = []
        for p, q in zip(row, row2):
            if p[:3] == q[:3] or p[3] == 0:
                r2.append(p[:])
                continue
            t = min(1.0, max(0.0, (p[0] / 255 - 0.35) / 0.65))        # rouge du liquide : profondeur
            h = min(1.0, p[1] / 200)                                  # vert : reflets
            c = [A[i] + (B[i] - A[i]) * t for i in range(3)]
            c = [v + (255 - v) * h * h * 0.6 for v in c]
            r2.append(c + [p[3]])
        out.append(r2)
    return out


def item_json(p):
    pid, name, desc, size, a, b, q, lvl, kind, eff, ingr, tier = p
    model = SIZES[size][0]
    iid = "Eld_Potion_" + pid
    root = {H: "Root_Secondary_Consume_Potion_Health", S: "Root_Secondary_Consume_Potion_Stamina", M: "Root_Secondary_Consume_Potion_Mana",
            B: "Root_Secondary_Consume_Potion"}[kind]
    v = {"RemoveEffect": {"Interactions": [{"Type": "Simple"}]}, "Effect": eff}
    cd = {H: "Consume_Potion_Health_Cooldown", S: "Consume_Potion_Stamina_Cooldown", M: "Consume_Potion_Mana_Cooldown"}.get(kind)
    if cd:
        v["ConsumeCooldown"] = {"Interactions": [cd]}
    v["ConsumeSFX"] = {"Interactions": [{"Parent": "Consume_SFX", "Effects": {"LocalSoundEventId": "SFX_Health_Potion_Low_Drink"}}]}
    v["ConsumedSFX"] = {"Interactions": [{"Parent": "Consumed_SFX", "Effects": {"LocalSoundEventId": "SFX_Potion_Drink_Success"}}]}
    rec = {"KnowledgeRequired": False, "Input": [{"ItemId": EMPTY[size], "Quantity": 1}] + [{"ItemId": slug(i), "Quantity": 1} for i in ingr],
           "BenchRequirement": [{"Id": "Alchemybench", "Type": "Crafting", "Categories": ["Alchemy_Potions"], **({"RequiredTierLevel": tier} if tier > 1 else {})}],
           "OutputQuantity": 1, "TimeSeconds": 1}
    return iid, {"Parent": "Potion_Template", "TranslationProperties": {"Name": name, "Description": desc}, "Quality": q, "ItemLevel": lvl,
                 "Categories": ["Eldanior_System.Consommables"], "Icon": icon_of(pid), "Recipe": rec,
                 "Interactions": {"Secondary": root}, "InteractionVars": v,
                 "BlockType": {"CustomModel": "Items/Consumables/Potions/%s.blockymodel" % model,
                               "CustomModelTexture": [{"Texture": "Items/Consommables/Potions/%s.png" % iid, "Weight": 1}],
                               "BlockParticleSetId": "GlassMagic", "ParticleColor": b,
                               "Light": {"Color": mix(a, "#000000", .55)[:7]}, "TextureComputedColor": "#A7A6A4"}}


def textures(assets):
    T = os.path.join(assets, "Common", "Items", "Consumables", "Potions", "Potion_Textures")
    _, _, red = png.read(os.path.join(T, "Red.png"))
    _, _, blue = png.read(os.path.join(T, "Blue.png"))
    I = os.path.join(assets, "Common", "Icons", "ItemsGenerated")
    ic = {}
    for size, (_, suf) in SIZES.items():
        s = "_" + suf if suf else ""
        ic[size] = (png.read(os.path.join(I, "Potion_Health%s.png" % s))[2], png.read(os.path.join(I, "Potion_Stamina%s.png" % s))[2])
    return red, blue, ic


def main():
    assets = sys.argv[1]
    red, blue, ic = textures(assets)
    if len(sys.argv) > 3:
        cell = 132
        cols = 8
        rows = -(-len(POTIONS) // cols)
        px = [[[46, 42, 50, 255] for _ in range(cols * cell)] for _ in range(rows * cell)]
        for n, p in enumerate(POTIONS):
            icon = recolor(ic[p[3]][0], ic[p[3]][1], p[4], p[5])
            ox, oy = n % cols * cell + 2, n // cols * cell + 2
            for y in range(128):
                for x in range(128):
                    c = icon[y // 2][x // 2]
                    if c[3]:
                        t = c[3] / 255
                        d = px[oy + y][ox + x]
                        px[oy + y][ox + x] = [d[k] + (c[k] - d[k]) * t for k in range(3)] + [255]
        png.write(os.path.join(sys.argv[3], "potions.png"), cols * cell, rows * cell, px)
        return
    ditems = os.path.join(RES, "Server", "Item", "Items", "Potion")
    dfx = os.path.join(RES, "Server", "Entity", "Effects", "Potion")
    dtex = os.path.join(RES, "Common", "Items", "Consommables", "Potions")
    dint = os.path.join(RES, "Server", "Item", "Interactions", "Consumables")
    droot = os.path.join(RES, "Server", "Item", "RootInteractions", "Consumables")
    for d in (dtex, dint, droot):
        os.makedirs(d, exist_ok=True)
    for p in POTIONS:
        iid, js = item_json(p)
        png.write(os.path.join(RES, "Common", "Icons", "ItemsGenerated", iid + ".png"), 64, 64, recolor(ic[p[3]][0], ic[p[3]][1], p[4], p[5]))
        png.write(os.path.join(dtex, iid + ".png"), 32, 64, recolor(red, blue, p[4], p[5]))
        json.dump(js, open(os.path.join(ditems, iid + ".json"), "w", encoding="utf-8"), ensure_ascii=False, indent=2)
    for eid, (fx, pid) in EFFECTS.items():
        fx = dict(fx)
        if pid:
            fx["StatusEffectIcon"] = icon_of(pid)
        json.dump(fx, open(os.path.join(dfx, eid + ".json"), "w", encoding="utf-8"), indent=2)
    json.dump({"Type": "Condition", "RequiredGameMode": "Adventure", "Crouching": False,
               "Next": {"Type": "CooldownCondition", "Id": "Potion_Consume_Mana", "Next": "Consume_Charge_Potion_Fast"}, "Failed": "Block_Secondary"},
              open(os.path.join(dint, "Condition_Consume_Potion_Mana.json"), "w"), indent=2)
    json.dump({"Type": "TriggerCooldown", "Cooldown": {"Id": "Potion_Consume_Mana", "Cooldown": 15}},
              open(os.path.join(dint, "Consume_Potion_Mana_Cooldown.json"), "w"), indent=2)
    json.dump({"RequireNewClick": True, "Interactions": ["Condition_Consume_Potion_Mana"]},
              open(os.path.join(droot, "Root_Secondary_Consume_Potion_Mana.json"), "w"), indent=2)
    # La Petite Potion de Vie existante reprend la petite bouteille du jeu
    r = recolor(ic["Small"][0], ic["Small"][1], "#8a0f1a", "#ff3a30")
    png.write(os.path.join(RES, "Common", "Icons", "ItemsGenerated", "Potion_Stats_Small.png"), 64, 64, r)
    png.write(os.path.join(dtex, "Potion_Stats_Small.png"), 32, 64, recolor(red, blue, "#8a0f1a", "#ff3a30"))
    print("potions", len(POTIONS), "effets", len(EFFECTS))


if __name__ == "__main__":
    main()
