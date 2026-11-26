#!/usr/bin/env python3
"""Failles-donjons (2026-11-22, docs/plan-failles-donjons.md) : les assets communs a toutes les failles.

- Eldanior_Faille_<Rang> (E, D, C, B, A, S) : la faille au sol. Effet de la faille gobeline du jeu RECOLORE a la couleur du rang
  (couleurs des rangs de QuestDifficulty) et AGRANDI selon le rang (decision de l'utilisateur : couleur ET taille changent).
  On y entre en marchant dedans (CollisionEnter -> interaction Eldanior_EntrerFaille, code Java failles/).
- Particules Eldanior_Faille_<Rang>_Active (+ generateurs) : copies de GoblinBreach_Portal_Active, couleurs remplacees.
- Eldanior_Faille_Scellee_<Rang> : objet a poser (option B choisie le 2026-11-22) ; pose = la faille s'ouvre a cet endroit.
- Eldanior_Faille_Barriere : barriere de la salle du boss, s'ouvre avec la Cle du Trone (F dessus).
- Eldanior_Faille_Cle_Trone : la cle lachee par le mini-boss (retiree a la sortie de la faille).
- Eldanior_Faille_Sortie : plateforme de sortie (ExitInstance du jeu), posee a l'arrivee et a la mort du boss.
- Marqueurs de carte Eldanior_Faille_<Rang>.png (+ _Boussole), icones des failles scellees.
- GameplayConfigs/Eldanior_Faille.json : mort = ejecte (ExitInstance), aucune perte d'objet, pas de casse de blocs.
- Instances/Eldanior_Faille_Terrier/instance.bson : monde vide, ambiance du terrier gobelin ; le donjon y est colle par le code.

Usage : python3 -I scripts/failles/gen_failles.py [dossier des assets vanilla]
"""
import base64, colorsys, json, math, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.normpath(os.path.join(HERE, "..", ".."))
sys.path.insert(0, os.path.join(HERE, "..", "rune_aura_icons"))
import png  # noqa: E402

VANILLA = sys.argv[1] if len(sys.argv) > 1 else os.path.expanduser("~/Desktop/Assets Hytale")
RES = os.path.join(ROOT, "src", "main", "resources")

# rang : (couleur de QuestDifficulty, echelle de la faille, nom)
RANKS = {
    # 2026-11-23 : un rang revient avec son donjon (demande de l'utilisateur) : F (Cimetiere, blanc), E (Terrier, vert),
    # D (Necropole Calcinee, bleu, 2026-11-24). A venir : C #9C27B0 1,4 ; B #ff9800 1,6 ; A #cc4444 1,9 ; S #FFD700 2,2.
    "F": ("#FFFFFF", 1.0),
    "E": ("#4CAF50", 1.0),
    "D": ("#2196F3", 1.2),
}
BASE_HUE = 80.0   # teinte moyenne de la faille gobeline (vert-jaune)


def write_json(rel, data, compact=False):
    path = os.path.join(RES, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        if compact:
            json.dump(data, f, separators=(",", ":"), ensure_ascii=False)
        else:
            json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


def hex2rgb(h):
    h = h.lstrip("#")
    return tuple(int(h[i:i + 2], 16) / 255 for i in (0, 2, 4))


def rgb2hex(r, g, b):
    return "#%02x%02x%02x" % tuple(max(0, min(255, round(v * 255))) for v in (r, g, b))


def recolor(hexcol, target):
    """Meme lumiere et saturation, teinte ramenee vers celle du rang (un peu de variation gardee). Blancs et gris : inchanges."""
    r, g, b = hex2rgb(hexcol[:7])
    h, s, v = colorsys.rgb_to_hsv(r, g, b)
    if s < 0.18:
        return hexcol
    th, ts, tv = colorsys.rgb_to_hsv(*hex2rgb(target))
    if ts < 0.05:  # blanc : on desature, on eclaircit
        return rgb2hex(*colorsys.hsv_to_rgb(h, s * 0.08, min(1.0, v * 1.25 + 0.2))) + hexcol[7:]
    d = ((h * 360 - BASE_HUE + 180) % 360) - 180
    nh = ((th * 360 + max(-40, min(40, d)) * 0.3) % 360) / 360
    if ts < 0.5:  # cible peu saturee : on la suit
        s = s * 0.6
    if target.upper() == "#FFD700":  # or : plus lumineux
        v = min(1.0, v * 1.15 + 0.08)
    return rgb2hex(*colorsys.hsv_to_rgb(nh, s, v)) + hexcol[7:]


def recolor_tree(o, target):
    if isinstance(o, dict):
        return {k: (recolor(v, target) if isinstance(v, str) and v.startswith("#") and len(v) in (7, 9) else recolor_tree(v, target))
                for k, v in o.items()}
    if isinstance(o, list):
        return [recolor_tree(v, target) for v in o]
    return o


# ---------------------------------------------------------------- particules (les 3 effets de la faille gobeline, recolores)
PSYS = os.path.join(VANILLA, "Server", "Particles", "Spell", "Portal")
SYSTEMS = {"Active": "GoblinBreach_Portal_Active",     # portail ouvert (sur le bloc de faille)
           "Etincelle": "GoblinBreach_Passive",       # etincelles qui annoncent la faille (compte a rebours)
           "Ouverture": "GoblinBreach_Portal"}        # eclat de l'ouverture (26 s)
for rank, (col, scale) in RANKS.items():
    for suffix, vanilla_id in SYSTEMS.items():
        with open(os.path.join(PSYS, vanilla_id + ".particlesystem"), encoding="utf-8") as f:
            sysdef = json.load(f)
        for sp in sysdef["Spawners"]:
            src = sp["SpawnerId"]
            path = os.path.join(PSYS, "Spawners", "GoblinBreach", src + ".particlespawner")
            if not os.path.exists(path):
                continue
            with open(path, encoding="utf-8") as f:
                spawner = json.load(f)
            new_id = f"Eldanior_Faille_{rank}_{src.replace('GoblinBreach_', '')}"
            write_json(f"Server/Particles/Eldanior/Failles/Spawners/{new_id}.particlespawner", recolor_tree(spawner, col))
            sp["SpawnerId"] = new_id
        if "CullDistance" in sysdef:
            sysdef["CullDistance"] = int(sysdef["CullDistance"] * scale)
        write_json(f"Server/Particles/Eldanior/Failles/Eldanior_Faille_{rank}_{suffix}.particlesystem", sysdef)

# ---------------------------------------------------------------- bloc de faille = copie du portail gobelin vanilla
# meme modele, meme boite (Goblin_Breach_Portal : bloc de 4 de large, cases de remplissage), meme decalage des particules ;
# pose tourne comme dans la structure vanilla (rotation 2). On y entre en marchant dedans (Eldanior_EntrerFaille).
with open(os.path.join(VANILLA, "Server", "Item", "Items", "Portal", "Goblin", "Goblin_Breach_Portal.json"), encoding="utf-8") as f:
    vportal = json.load(f)
for rank, (col, scale) in RANKS.items():
    bt = json.loads(json.dumps(vportal["BlockType"]))
    bt.pop("BlockEntity", None)                                         # pas de composant Portal vanilla ni de marqueur du jeu
    bt["Particles"] = [{"SystemId": f"Eldanior_Faille_{rank}_Active", "Scale": round(1.1 * scale, 2),
                        "PositionOffset": {"Y": 2, "X": 1}}]
    bt["Light"] = {"Color": col}
    bt["Gathering"] = {"Breaking": {"GatherType": "Unbreakable"}}
    bt["Interactions"] = {"CollisionEnter": {"Interactions": [{"Type": "Eldanior_EntrerFaille"}]}}
    write_json(f"Server/Item/Items/Eldanior/Failles/Eldanior_Faille_{rank}.json", {
        "TranslationProperties": {"Name": f"Faille de rang {rank}",
                                  "Description": "Une déchirure vers un donjon d'un autre plan. Marchez dedans pour y entrer avec votre groupe."},
        "Icon": f"Icons/ItemsGenerated/Eldanior_Faille_Scellee_{rank}.png",
        "MaxStack": 1, "Quality": "Technical",                         # pose par le mod : hors du menu creatif
        "BlockType": bt,
        "PlayerAnimationsId": "Block",
        "Tags": {"Type": ["Portal"]},
    })
    # etincelles qui annoncent la faille (copie de Spawn_Goblin_Breach_Sparks)
    write_json(f"Server/Item/Items/Eldanior/Failles/Eldanior_Faille_{rank}_Etincelle.json", {
        "TranslationProperties": {"Name": f"Faille de rang {rank} (sur le point de s'ouvrir)"},
        "Icon": f"Icons/ItemsGenerated/Eldanior_Faille_Scellee_{rank}.png",
        "MaxStack": 1, "Quality": "Technical",
        "BlockType": {
            "Material": "Empty", "DrawType": "Model", "Opacity": "Transparent",
            "Particles": [{"SystemId": f"Eldanior_Faille_{rank}_Etincelle", "PositionOffset": {"Z": 0.25}}],
            "AmbientSoundEventId": "SFX_Goblin_World_Breach_Passive",
            "CustomModel": "Blocks/Miscellaneous/Map.blockymodel",
            "CustomModelTexture": [{"Texture": "Items/Projectiles/Tornado_Texture.png", "Weight": 1}],
            "Support": {"Down": [{"Support": "Ignored"}]},
            "Light": {"Color": col},
        },
        "PlayerAnimationsId": "Block",
    })

# ---------------------------------------------------------------- structure d'apparition (comme la faille gobeline vanilla)
# Prefab Goblin_World_Event_Portal du jeu : arche de roche, sol brule, mousse, debris autour du portail. Le portail gobelin y est
# remplace par notre bloc de faille ; theme du rang F (cimetiere) : ossements au lieu des dechets gobelins, herbe seche.
with open(os.path.join(VANILLA, "Server", "Prefabs", "Orbis", "WorldEvent", "Goblins", "Goblin_World_Event_Portal.prefab.json"), encoding="utf-8") as f:
    site = json.load(f)
ax, ay, az = site["anchorX"], site["anchorY"], site["anchorZ"]
THEME = {"F": {"Deco_Trash_Pile_Large": "Deco_Bone_Pile", "Deco_Trash_Pile_Small": "Deco_Bone_Skulls", "Deco_Trash": "Deco_Bone_Pile",
               "Soil_Grass": "Soil_Grass_Dry", "Plant_Moss_Rug_Green_Dark": "Plant_Grass_Dry",
               "Plant_Moss_Wall_Green_Dark": "Plant_Vine_Wall_Dead"},
         # rang D (Necropole Calcinee) : arche de roche volcanique, cendres, gravats
         "D": {"Rock_Stone": "Rock_Volcanic", "Rock_Stone_Half": "Rock_Volcanic_Half", "Rock_Stone_Mossy": "Rock_Volcanic",
               "Soil_Dirt": "Soil_Dirt_Burnt", "Soil_Mud_Dry": "Soil_Ash", "Soil_Grass": "Soil_Grass_Burnt",
               "Plant_Moss_Rug_Green_Dark": "Rubble_Volcanic", "Plant_Moss_Wall_Green_Dark": "Empty",
               "Deco_Trash_Pile_Large": "Rubble_Volcanic_Medium", "Deco_Trash_Pile_Small": "Deco_Bone_Skulls",
               "Deco_Trash": "Rubble_Volcanic", "Plant_Grass_Sharp_Tall": "Plant_Bramble_Dead_Lavathorn"}}
for rank, (col, scale) in RANKS.items():
    blocks, cells, main = [], [], None
    for b in site["blocks"]:
        nb = {k: v for k, v in b.items() if k != "components"}
        if b["name"] == "Goblin_Breach_Portal":
            nb["name"] = f"Eldanior_Faille_{rank}"
            if "filler" not in b:
                main = [b["x"] - ax, b["y"] - ay, b["z"] - az]
        else:
            nb["name"] = THEME.get(rank, {}).get(b["name"], b["name"])
            if nb["name"] != b["name"]:
                nb.pop("rotation", None) if nb["name"].startswith("Plant_Grass") else None
        blocks.append(nb)
        cells.append([b["x"] - ax, b["y"] - ay, b["z"] - az])
    out = dict(site)
    out["blocks"] = blocks
    out.pop("entities", None)
    write_json(f"Server/Prefabs/Eldanior/Failles/Site_{rank}.prefab.json", out, compact=True)
    write_json(f"eldanior/failles/site_{rank}.json", {"prefab": f"Eldanior/Failles/Site_{rank}.prefab.json", "main": main,
                                                    "rotation": 2, "cells": cells}, compact=True)

# ---------------------------------------------------------------- icones (objets, carte)
ICON = os.path.join(VANILLA, "Common", "Icons", "ItemsGenerated", "PortalKey_Goblin_Breach.png")
iw, ih, ipx = png.read(ICON)
for rank, (col, scale) in RANKS.items():
    th, ts, tv = colorsys.rgb_to_hsv(*hex2rgb(col))
    out = []
    for row in ipx:
        line = []
        for r, g, b, a in row:
            h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            if s > 0.15:
                h = th
                s = min(1, s * 1.1) if ts > 0.05 else s * 0.06
                if ts <= 0.05:
                    v = min(1.0, v * 1.2 + 0.1)
            rr, gg, bb = colorsys.hsv_to_rgb(h, s, v)
            line.append([rr * 255, gg * 255, bb * 255, a])
        out.append(line)
    p = os.path.join(RES, "Common", "Icons", "ItemsGenerated", f"Eldanior_Faille_Scellee_{rank}.png")
    os.makedirs(os.path.dirname(p), exist_ok=True)
    png.write(p, iw, ih, out)

    # marqueur de carte : spirale de la couleur du rang sur fond sombre, 48 px (comme les autres marqueurs du mod)
    N = 48
    cr, cg, cb = (v * 255 for v in hex2rgb(col))
    px = [[[0, 0, 0, 0] for _ in range(N)] for _ in range(N)]
    for y in range(N):
        for x in range(N):
            dx, dy = x - N / 2 + 0.5, y - N / 2 + 0.5
            d = math.hypot(dx, dy)
            if d > 21:
                continue
            ang = math.atan2(dy, dx)
            spiral = (ang / (2 * math.pi) * 3 + d / 7.0) % 1.0
            if d > 19:
                px[y][x] = [20, 16, 24, 255]
            elif d < 4:
                px[y][x] = [255, 255, 255, 255]
            else:
                k = 0.35 + 0.65 * (1 - abs(spiral - 0.5) * 2)
                k *= 1.0 - (d / 19) * 0.35
                px[y][x] = [cr * k + 40 * (1 - k), cg * k + 30 * (1 - k), cb * k + 50 * (1 - k), 255]
    mdir = os.path.join(RES, "Common", "UI", "WorldMap", "MapMarkers")
    os.makedirs(mdir, exist_ok=True)
    png.write(os.path.join(mdir, f"Eldanior_Faille_{rank}.png"), N, N, px)
    small = [[px[y * 2][x * 2] for x in range(24)] for y in range(24)]
    png.write(os.path.join(mdir, f"Eldanior_Faille_{rank}_Boussole.png"), 24, 24, small)

# ---------------------------------------------------------------- faille scellee (objet a poser)
for rank, (col, scale) in RANKS.items():
    write_json(f"Server/Item/Items/Eldanior/Failles/Eldanior_Faille_Scellee_{rank}.json", {
        "TranslationProperties": {
            "Name": f"Faille scellée (rang {rank})",
            "Description": (f"Posez-la hors d'une ville : une faille de rang {rank} s'ouvre à cet endroit. "
                            "Votre groupe y entre (1 entrée par joueur). Fermez-la avant qu'elle ne déborde !")},
        "Icon": f"Icons/ItemsGenerated/Eldanior_Faille_Scellee_{rank}.png",
        "Categories": ["Eldanior_System.Dungeons"],
        "BlockType": {
            "DrawType": "Model", "Material": "Solid", "Opacity": "Transparent",
            "CustomModel": "Blocks/Miscellaneous/Portal_Shard.blockymodel",
            "CustomModelTexture": [{"Texture": "Blocks/Miscellaneous/Portal_Shard_Texture.png", "Weight": 1}],
            "TextureComputedColor": col,
            "Light": {"Color": col},
        },
        "PlayerAnimationsId": "Item",
        "Tags": {"Type": ["Portal"]},
        "Quality": {"F": "Common", "E": "Uncommon", "D": "Uncommon", "C": "Rare", "B": "Rare", "A": "Epic", "S": "Legendary"}[rank],
        "MaxStack": 4,
        "ItemLevel": {"F": 10, "E": 60, "D": 150, "C": 300, "B": 450, "A": 600, "S": 800}[rank],
        "IconProperties": {"Scale": 0.8, "Rotation": [22.5, 137, 22.5], "Translation": [-9, -15]},
        "ItemSoundSetId": "ISS_Items_Gems",
    })

# ---------------------------------------------------------------- barriere, cle, sortie
# 2026-11-23 : plus de Sceau ni de Cle de faille ; la salle du boss se ferme par une porte de boss du mod (Eldanior_Porte_Boss_ED)
# dont la cle sort d'un coffre ChestTreasureCleBossED pose dans le donjon (scripts/failles/gen_donjons.py).
# Balise d'apparition du boss du Cimetiere (le jeu n'en a pas pour le Squelette d'elite) :
write_json("Server/NPC/Spawn/Markers/Eldanior/Eldanior_Faille_Boss_Roi_Squelette.json", {
    "Model": "NPC_Spawn_Marker",
    "NPCs": [{"Name": "Skeleton_Elite", "Weight": 100, "SpawnAfterGameTime": "P1D"}],   # temps fige dans la faille : pas de retour
    "ExclusionRadius": 0, "MaxDropHeight": 4, "DeactivationDistance": 150,
})
with open(os.path.join(VANILLA, "Server", "Item", "Items", "Portal", "Forgotten_Temple", "Forgotten_Temple_Portal_Exit.json"), encoding="utf-8") as f:
    exit_pad = json.load(f)
exit_pad["TranslationProperties"] = {"Name": "Sortie de la faille", "Description": "Marchez dessus pour quitter la faille (on n'y revient pas)."}
exit_pad["Categories"] = ["Eldanior_System.Dungeons"]
exit_pad["Quality"] = "Technical"
exit_pad.pop("Recipe", None)
write_json("Server/Item/Items/Eldanior/Failles/Eldanior_Faille_Sortie.json", exit_pad)

# ---------------------------------------------------------------- anciens generateurs (retires le 2026-11-23)
# Remplaces par le bloc unique Eldanior_Generateur_Monstres (scripts/failles/gen_generateur.py). Les modeles deja retouches en jeu
# en contiennent encore : on garde leurs ids, en blocs invisibles, traversables, sans effet et hors du menu creatif, pour ne pas
# toucher a ces instances.
OLD_SPAWNERS = ["Eldanior_Spawner_Squelettes", "Eldanior_Spawner_Squelettes_Archers", "Eldanior_Spawner_Squelettes_Mages",
                "Eldanior_Spawner_Chevalier_Squelette", "Eldanior_Spawner_Boss_Roi_Squelette", "Eldanior_Spawner_Gobelins",
                "Eldanior_Spawner_Gobelins_Lanceurs", "Eldanior_Spawner_Gardien_Gobelin", "Eldanior_Spawner_Glouton",
                "Eldanior_Spawner_Boss_Duc"]
old_json = os.path.join(RES, "eldanior", "failles", "spawners.json")
if os.path.exists(old_json):
    os.remove(old_json)
for sid in OLD_SPAWNERS:
    write_json(f"Server/Item/Items/Eldanior/Failles/Spawners/{sid}.json", {
        "$Comment": "Ancien generateur (retire) : bloc vide garde pour les modeles de faille qui en contiennent encore.",
        "TranslationProperties": {"Name": "Ancien générateur (sans effet)"},
        "Icon": "Icons/ItemsGenerated/Editor_Empty.png",
        "Quality": "Technical",
        "MaxStack": 64,
        "BlockType": {"Material": "Empty", "DrawType": "Empty", "Opacity": "Transparent",
                      "Support": {"Down": [{"Support": "Ignored"}]}},
        "PlayerAnimationsId": "Block",
    })

# ---------------------------------------------------------------- regles du monde de la faille et instance
write_json("Server/GameplayConfigs/Eldanior_Faille.json", {
    "Parent": "Default_Instance",
    "Death": {"ItemsLossMode": "None", "RespawnController": {"Type": "ExitInstance"}},
    "World": {"AllowBlockBreaking": False, "AllowBlockGathering": False},
    "Plugin": {"WorldEvents": {}},
})


def instance(name, environment, weather, hour, comment, tint=None):
    data = {
        "$Comment": comment,
        "RequiredPlugins": {},
        "ChunkStorage": {"Type": "Hytale"},
        # modele = monde d'EDITION : creatif, sans monstre ; chaque faille ouverte en est une copie passee en aventure, PNJ et
        # balises d'apparition (NPC_Spawn_Marker) actifs (FailleManager.startDungeon)
        "GameMode": "Creative",
        "IsPvpEnabled": False,
        "IsSpawningNPC": False,
        "GameTime": f"0001-01-01T{hour:02d}:00:00Z",
        "UUID": {"$binary": base64.b64encode(("Eldanior" + name[-8:]).encode()[:16].ljust(16, b"_")).decode(), "$type": "04"},
        "GameplayConfig": "Eldanior_Faille",
        "WorldMap": {"Type": "Disabled"},
        "IsCompassUpdating": False,
        "IsTicking": True,
        "IsGameTimePaused": True,
        "IsObjectiveMarkersEnabled": False,
        "IsAllNPCFrozen": False,
        "IsSavingPlayers": False,
        "WorldGen": dict({"Type": "Void", "Environment": environment}, **({"Tint": tint} if tint else {})),
        "SpawnProvider": {"Id": "Global", "SpawnPoint": {"X": 0.5, "Y": 121, "Z": 0.5, "Pitch": 0, "Yaw": 90, "Roll": 0.0}},
        "IsSpawnMarkersEnabled": True,    # balises gardees (sinon retirees du modele) ; IsSpawningNPC coupe : aucun monstre en edition
        "Instance": {"RemovalConditions": [{"Type": "WorldEmpty"}]},
        "DeleteOnRemove": True,
        "Version": 2,
    }
    if weather:
        data["ForcedWeather"] = weather
    write_json(f"failles-pack/Server/Instances/{name}/instance.bson", data)   # pack dossier modifiable (failles/FaillePack)


instance("Eldanior_Faille_Terrier", "Dungeon_GoblinLair_Duke", None, 12,
         "Faille-donjon du Terrier du Duc (rang E) : monde vide, le donjon est colle par failles/FailleManager.")
instance("Eldanior_Faille_Cimetiere", "Env_Zone1_Graveyard", "Eldanior_Faille_Brume", 23,
         "Faille-donjon du Cimetiere des Oublies (rang F) : nuit, brouillard epais ; le donjon est colle par failles/FailleManager.")
instance("Eldanior_Faille_Necropole", "Env_Eldanior_Faille_Caldeira", "Eldanior_Faille_Caldeira", 17,
         "Faille-donjon de la Necropole Calcinee (rang D) : la Caldeira (biome exterieur, scripts/failles/biome_caldeira.py), "
         "teinte brulee, ciel de braise, cendres et braises ; le donjon est construit par l'admin en edition.", tint="#6b4b33")

# brouillard constant, peu de visibilite : la meteo du cimetiere vanilla, brouillard rapproche et plus dense
with open(os.path.join(VANILLA, "Server", "Weathers", "Zone1", "Dungeon_Cursed_Crypt_Graveyard.json"), encoding="utf-8") as f:
    brume = json.load(f)
brume["FogDistance"] = [-24, 46]
if isinstance(brume.get("FogDensities"), list):
    for e in brume["FogDensities"]:
        for k in ("Value", "Density"):
            if isinstance(e, dict) and isinstance(e.get(k), (int, float)):
                e[k] = min(1.0, e[k] * 2.5)
write_json("Server/Weathers/Eldanior/Eldanior_Faille_Brume.json", brume)

# ---------------------------------------------------------------- rang D : environnement propre a la Caldeira (2026-11-24)
# Demande de l'utilisateur : un biome a part, avec ses teintes et ses particules d'environnement (rien de commun avec le F).
#  - teinte des chunks : WorldGen.Tint de l'instance (vegetation et herbe brulees) ;
#  - meteo Eldanior_Faille_Caldeira : ciel lie-de-vin, horizon de braise, enorme soleil rouge bas, lumiere orangee, brume de
#    cendre brun-rouge rapprochee ; heure figee a 17 h (soleil rasant) ;
#  - particules Eldanior_Faille_Caldeira : flocons de cendre qui tombent, braises qui montent, fumee basse ;
#  - environnement Env_Eldanior_Faille_Caldeira : pas d'apparitions naturelles, meteo forcee a toute heure, ambiance sonore
#    (grondement de lave + vent) par son tag Eldanior_Caldeira.
PW = os.path.join(VANILLA, "Server", "Particles", "Weather")
with open(os.path.join(PW, "Firelands", "Spawners", "Ember.particlespawner"), encoding="utf-8") as f:
    braises = json.load(f)
braises["SpawnRate"] = {"Min": 700, "Max": 1400}
braises["ParticleLifeSpan"] = {"Min": 2.5, "Max": 4.5}
braises["EmitOffset"]["Y"] = {"Min": -2, "Max": 9}
braises["Attractors"][0]["LinearAcceleration"] = {"X": 0.004, "Y": 0.012, "Z": 0}         # elles montent et derivent
anim = braises["Particle"]["Animation"]
anim["30"] = {"Opacity": 0.9, "Color": "#ffd27a"}
anim["70"] = {"Opacity": 0.6, "Color": "#ff5a1a"}
anim["100"] = {"Opacity": 0, "Color": "#5a0f05"}
braises["Particle"]["InitialAnimationFrame"]["Scale"] = {"X": {"Min": 0.03, "Max": 0.22}}
write_json("Server/Particles/Eldanior/Failles/Spawners/Eldanior_Caldeira_Braises.particlespawner", braises)
with open(os.path.join(PW, "Ash", "Spawners", "Ash.particlespawner"), encoding="utf-8") as f:
    cendre = json.load(f)
cendre["SpawnRate"] = {"Min": 3000, "Max": 3000}
cendre["MaxConcurrentParticles"] = 260
cendre["ParticleLifeSpan"] = {"Min": 4, "Max": 7}
cendre["InitialVelocity"]["Speed"] = {"Min": -0.012, "Max": -0.026}                       # flocons lents
cendre["Attractors"][0]["RadialAcceleration"] = -0.05
cendre["Attractors"][0]["RadialTangentAcceleration"] = 0.06
cendre["Particle"]["InitialAnimationFrame"]["Color"] = "#8c827c"
cendre["Particle"]["InitialAnimationFrame"]["Scale"] = {"X": {"Min": 0.07, "Max": 0.13}, "Y": {"Min": 0.07, "Max": 0.13}}
write_json("Server/Particles/Eldanior/Failles/Spawners/Eldanior_Caldeira_Cendres.particlespawner", cendre)
write_json("Server/Particles/Eldanior/Failles/Eldanior_Faille_Caldeira.particlesystem", {
    "Spawners": [{"SpawnerId": "Eldanior_Caldeira_Cendres"},
                 {"SpawnerId": "Eldanior_Caldeira_Braises", "FixedRotation": True},
                 {"SpawnerId": "Firelands_Smoke", "FixedRotation": True}],
    "CullDistance": 32,
})

with open(os.path.join(VANILLA, "Server", "Weathers", "Zone4", "Zone4_Forest_Burnt.json"), encoding="utf-8") as f:
    caldeira = json.load(f)


def const(v, key="Color"):
    return [{"Hour": 0, key: v}]


caldeira.update({
    "$Comment": "Faille de rang D, la Caldeira (scripts/failles/gen_failles.py) : couleurs constantes, heure figee a 17 h.",
    "Tags": {"Eldanior_Caldeira": []},
    "SkyTopColors": const("rgba(#2a0b08, 1)"), "SkyBottomColors": const("rgba(#8f2d12, 1)"),
    "SkySunsetColors": const("#ff6a1fff"),
    "SunColors": const("#ff4a14"), "SunGlowColors": const("rgba(#ff3c0a, 0.95)"), "SunScales": const(2.4, "Value"),
    "SunlightColors": const("#ff9a66"), "SunlightDampingMultipliers": const(0.72, "Value"),
    "MoonColors": const("#ff2a10ff"), "MoonGlowColors": const("#802e18ff"), "MoonScales": const(1.8, "Value"),
    "FogColors": const("#3d1a10"), "FogDistance": [-12, 96], "FogDensities": const(0.72, "Value"),
    "FogHeightFalloffs": const(4, "Value"),
    "Clouds": [{"Texture": "Sky/Clouds/Storm_Highlights.png", "Colors": const("rgba(#4a1408, 0.85)"), "Speeds": const(0.5, "Value")}],
    "Particle": {"SystemId": "Eldanior_Faille_Caldeira", "OvergroundOnly": True},
})
caldeira.pop("Stars", None)
write_json("Server/Weathers/Eldanior/Eldanior_Faille_Caldeira.json", caldeira)
write_json("Server/Environments/Eldanior/Env_Eldanior_Faille_Caldeira.json", {
    "WaterTint": "#ff5a1a",
    "SpawnDensity": 0,
    "Tags": {"Eldanior_Caldeira": []},
    "WeatherForecasts": {str(h): [{"WeatherId": "Eldanior_Faille_Caldeira", "Weight": 100}] for h in range(24)},
})
for name, track, vol in (("Lave", "Sounds/Environments/Global/Cave/Cave_Volcanic/Cave_Volcanic_Stereo_LOOP.ogg", -4),
                         ("Vent", "Sounds/Environments/Zone2/Global/Wind/Z2_Wind_Base_Mid_Stereo_LOOP.ogg", -8)):
    write_json(f"Server/Audio/AmbienceFX/Eldanior/AmbFX_Eldanior_Caldeira_{name}.json", {
        "AudioCategory": "AudioCat_Ambient",
        "Conditions": {"EnvironmentTagPattern": {"Op": "Equals", "Tag": "Eldanior_Caldeira"}},
        "AmbientBed": {"Track": track, "Volume": vol},
    })
old = os.path.join(RES, "Server", "Weathers", "Eldanior", "Eldanior_Faille_Cendres.json")    # meteo de la v1 (abandonnee)
if os.path.exists(old):
    os.remove(old)

# .version du modele livre (FaillePack l'installe s'il est absent, ou s'il est plus recent qu'un modele jamais retouche).
# Necropole : 5 = la Caldeira (la v1, 4, abandonnee, est installee chez l'utilisateur). Ne jamais baisser une version.
for name, version in (("Eldanior_Faille_Necropole", 5),):
    vp = os.path.join(RES, "failles-pack", "Server", "Instances", name, ".version")
    cur = int(open(vp).read().strip()) if os.path.exists(vp) else 0
    if cur < version:
        with open(vp, "w", encoding="utf-8") as f:
            f.write(str(version))
print(f"failles : rangs {', '.join(RANKS)} (blocs, particules, failles scellees, marqueurs), barriere, cle, sortie, gameplay, instance")
