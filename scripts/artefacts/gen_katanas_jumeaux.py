"""Amaterasu et Tsukuyomi, Lames Jumelles (artefact legendaire) : texture et icone, derivees du katana vanilla.

Le modele reste celui du katana vanilla (Items/Weapons/Longsword/Katana.blockymodel). L'arme est une epee : template,
combo et parade de l'epee, avec "RenderDualWielded" pour que le katana soit aussi rendu dans la main gauche. (Les
animations de dagues ont ete ecartees : prise inversee et gestes de dague ne conviennent pas a des sabres.)
  - attaque maintenue, Croix Jumelle : charge vanilla de l'epee, petit elan (variable Thrust_Force), taille a deux
    lames, puis le sort Java CroixJumelle lance une croix de lames d'energie qui traverse les ennemis a distance
    (variable Thrust_Selector -> interaction Katanas_Jumeaux_Croix) ;
  - signature, Eclipse des Jumeaux (remplace le tourbillon Vortexstrike) : Katanas_Jumeaux_Signature + sort Java
    EclipseJumelle (croix geante, plus lente, qui explose en fin de course).
Croix d'energie (modele Katanas_Croix, croix()) : deux copies de la lame de la Windstrike vanilla
(WindStrike_Projectile.blockymodel), tournees de +45 et -45 degres autour de l'axe de vol ; atlas 160x256 : lame d'or
(Amaterasu) en haut, lame de glace (Tsukuyomi) en bas. Deplacee par le code comme la lance de Gungnir.

Animations (jeu Katanas_Jumeaux, derive du jeu vanilla "Sword") : le squelette du joueur est symetrique (orientation de
base d'un noeud gauche = celle du droit avec y et z opposes), donc le geste d'un bras se transpose exactement a l'autre :
rotation (x, y, z, w) -> (x, -y, -z, w), position (x, y, z) -> (-x, y, z), noeuds R <-> L.
  - SwingRight = miroir complet de SwingLeft : la lame gauche frappe a son tour (coup 1 main droite, coup 2 main gauche) ;
  - coup plongeant, charge de coup, garde, repos et deplacements au sol = "double" : le bras gauche refait le geste du
    droit en miroir (la signature Eclipse des Jumeaux enchaine SwingLeftCharging puis SwingDownStrong doubles) ;
  - charge de l'attaque maintenue (StabDashCharging), nage, escalade, vol, chute, saut : vanilla.
Vue FPS traitee de la meme facon (fichiers *_FPS). L'epaule gauche suit aussi le regard (Camera.Pitch).

La texture garde la disposition UV vanilla : lame nacree au fil dore (hamon), tsuba et pommeau dores, poignee
cramoisie. Icone : deux katanas croises, Amaterasu (soleil, reflets chauds) devant Tsukuyomi (lune, reflets froids).
Item : degats de la ligne Legendaire (x3,8 l'epee en fer vanilla) ; coup de la lame droite = feu d'Amaterasu, coup de la
lame gauche = givre de Tsukuyomi, coup plongeant a deux lames = les deux.

Usage : python3 scripts/artefacts/gen_katanas_jumeaux.py <src/main/resources>
"""
import colorsys, copy, json, math, os, sys, zipfile

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "rune_aura_icons"))
import png  # noqa: E402

RES = sys.argv[1]
OUT = os.path.join(RES, "Common")
ASSETS = os.path.expanduser("~/Library/Application Support/Hytale/install/release/package/game/latest/Assets.zip")
NAME = "Artefact_Sword_Katanas_Jumeaux"

CRIMSON = 350 / 360          # teinte de la poignee
HAMON = (255, 214, 130)      # fil dore de la lame
SUN, MOON = (1.06, 1.0, 0.86), (0.86, 0.95, 1.08)   # reflets de l'icone (multiplicateurs RGB)


def load(path):
    tmp = os.path.join(OUT, ".tmp_" + os.path.basename(path))
    with open(tmp, "wb") as f:
        f.write(zipfile.ZipFile(ASSETS).read(path))
    w, h, px = png.read(tmp)
    os.remove(tmp)
    return w, h, px


def kind(c):
    """Matiere d'un pixel : 'blade' (gris clair), 'grip' (sombre), 'gold' (dore) ou None (transparent)."""
    if c[3] == 0:
        return None
    hh, s, v = colorsys.rgb_to_hsv(c[0] / 255, c[1] / 255, c[2] / 255)
    if s < 0.16 and v > 0.5:
        return "blade"
    if 0.08 < hh < 0.2 and s >= 0.35:
        return "gold"
    if v < 0.45 and s > 0.15:                                            # cuir brun de la poignee (pas les gris d'ombre de la lame)
        return "grip"
    return None


def crimson(c):
    _, s, v = colorsys.rgb_to_hsv(c[0] / 255, c[1] / 255, c[2] / 255)
    r, g, b = colorsys.hsv_to_rgb(CRIMSON, max(0.7, s), min(1.0, v * 1.5 + 0.05))
    return [r * 255, g * 255, b * 255, c[3]]


def pearl(c, tint=(0.97, 0.98, 1.04)):
    return [min(255, c[0] * tint[0]), min(255, c[1] * tint[1]), min(255, c[2] * tint[2]), c[3]]


def texture():
    w, h, px = load("Common/Items/Weapons/Longsword/Katana_Texture.png")
    kinds = [[kind(px[y][x]) for x in range(w)] for y in range(h)]
    out = [[list(px[y][x]) for x in range(w)] for y in range(h)]
    for y in range(h):
        for x in range(w):
            k = kinds[y][x]
            if k == "grip":
                out[y][x] = crimson(px[y][x])
            elif k == "blade":
                edge = y + 1 >= h or kinds[y + 1][x] != "blade"          # bas de chaque bande de lame : le fil
                c = pearl(px[y][x])
                out[y][x] = [(c[i] * 0.35 + HAMON[i] * 0.65) for i in range(3)] + [c[3]] if edge else c
    png.write(os.path.join(OUT, "Items", "Weapons", "artefacts", NAME + ".png"), w, h, out)


def icon():
    w, h, px = load("Common/Icons/ItemsGenerated/Weapon_Longsword_Katana.png")

    def recolor(c, tint):
        k = kind(c)
        if k == "grip":
            return crimson(c)
        if k == "blade":
            return pearl(c, tint)
        return list(c)

    out = [[[0, 0, 0, 0] for _ in range(w)] for _ in range(h)]
    for y in range(h):                                                  # Tsukuyomi, en miroir, derriere
        for x in range(w):
            c = px[y][w - 1 - x]
            if c[3] > 0:
                out[y][x] = recolor(c, MOON)
    for y in range(h):                                                  # Amaterasu devant
        for x in range(w):
            c = px[y][x]
            if c[3] > 0:
                out[y][x] = recolor(c, SUN)
    png.write(os.path.join(OUT, "Icons", "ItemsGenerated", NAME + ".png"), w, h, out)


MULT = 3.8                                                     # ligne Legendaire
ON_HIT = {"Swing_Left_Damage": "Eldanior_OnHit_Embrasement",          # lame droite : Amaterasu
          "Swing_Right_Damage": "Eldanior_OnHit_Givre",                # lame gauche : Tsukuyomi
          "Swing_Down_Damage": "Eldanior_OnHit_Soleil_Lune"}           # deux lames
THRUST_FORCE = 9                                                     # elan de l'attaque maintenue (vanilla : 20)
INTERACTIONS = {"Primary": "Root_Weapon_Sword_Primary", "Secondary": "Root_Weapon_Sword_Secondary_Guard",
                "Ability1": "Root_Katanas_Jumeaux_Signature"}          # signature Eclipse des Jumeaux (sort Java EclipseJumelle)

ANIM_SET = "Katanas_Jumeaux"
SRC_ANIMS, DST_ANIMS = "Characters/Animations/Items/Main_Handed/Sword/", "Characters/Animations/Items/Dual_Handed/Katanas_Jumeaux/"
MIRRORED = {"SwingRight": "SwingLeft"}                                 # animation -> animation dont elle est le miroir
DOUBLE = ["SwingDown", "SwingDownStrong", "Stab", "SwingLeftCharging", "SwingLeftCharged", "Guard", "GuardBash",
          "Idle", "Walk", "WalkBackward", "Run", "RunBackward", "Sprint", "Crouch", "CrouchWalk", "CrouchWalkBackward",
          "FluidIdle", "FluidWalk", "FluidWalkBackward", "FluidRun"]
# Laissees vanilla : la charge et la fente de l'attaque maintenue (StabDash*, lame droite), le reste (nage, vol...).


def side_swap(name):
    """Nom du noeud symetrique (R-Arm <-> L-Arm, R_Side_Cloth_1 <-> L_Side_Cloth_1), sinon None (noeud central)."""
    for a, b in (("R-", "L-"), ("L-", "R-"), ("R_", "L_"), ("L_", "R_")):
        if name.startswith(a):
            return b + name[len(a):]
    return None


def mirror_track(track):
    out = copy.deepcopy(track)
    for k in out.get("orientation", []):
        k["delta"]["y"], k["delta"]["z"] = -k["delta"]["y"], -k["delta"]["z"]
    for k in out.get("position", []):
        k["delta"]["x"] = -k["delta"]["x"]
    return out


def mirrored(anim):
    """Geste entier en miroir : chaque noeud prend la piste symetrique de son vis-a-vis, le corps se tord a l'oppose."""
    out = copy.deepcopy(anim)
    nodes = anim.get("nodeAnimations", {})
    out["nodeAnimations"] = {(side_swap(n) or n): mirror_track(t) for n, t in nodes.items()}
    return out


def doubled(anim):
    """Le cote gauche refait le geste du cote droit en miroir ; le corps et le cote droit ne changent pas."""
    out = copy.deepcopy(anim)
    nodes = out.get("nodeAnimations", {})
    for n in [n for n in nodes if n.startswith(("L-", "L_"))]:
        del nodes[n]
    for n, t in anim.get("nodeAnimations", {}).items():
        if n.startswith(("R-", "R_")):
            nodes[side_swap(n)] = mirror_track(t)
    return out


def animations():
    z = zipfile.ZipFile(ASSETS)
    anim_set = json.loads(z.read("Server/Item/Animations/Sword.json"))
    pitch = anim_set.setdefault("Camera", {}).setdefault("Pitch", {}).setdefault("TargetNodes", [])
    if "LShoulder" not in pitch:
        pitch.append("LShoulder")
    entries = anim_set["Animations"]
    written = {}

    def derive(entry, kind):
        entry = copy.deepcopy(entry)
        for key, path in list(entry.items()):
            if not (isinstance(path, str) and path.startswith(SRC_ANIMS) and path.endswith(".blockyanim")):
                continue
            dst = DST_ANIMS + path[len(SRC_ANIMS):-len(".blockyanim")] + "_" + kind + ".blockyanim"
            if dst not in written:
                anim = json.loads(z.read("Common/" + path))
                written[dst] = mirrored(anim) if kind == "Miroir" else doubled(anim)
            entry[key] = dst
        return entry

    for name, source in MIRRORED.items():
        entries[name] = derive(entries[source], "Miroir")
    for name in DOUBLE:
        if name in entries:
            entries[name] = derive(entries[name], "Double")
    for dst, anim in written.items():
        path = os.path.join(OUT, dst)
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w", encoding="utf-8") as f:
            json.dump(anim, f, separators=(",", ":"))                    # compact, comme les fichiers vanilla
    server = os.path.join(RES, "Server", "Item", "Animations")
    os.makedirs(server, exist_ok=True)
    with open(os.path.join(server, ANIM_SET + ".json"), "w", encoding="utf-8") as f:
        json.dump(anim_set, f, indent=2)
        f.write("\n")
    return len(written)


CROIX = "Katanas_Croix"
SUN_FIRE = ((255, 110, 20), (255, 246, 190))                           # ombre -> lumiere de la lame d'Amaterasu
MOON_ICE = ((70, 120, 255), (236, 246, 255))                           # ombre -> lumiere de la lame de Tsukuyomi


def croix():
    """Croix d'energie : modele (2 lames Windstrike a +-45 degres), atlas, traînees, modele d'entite, config de projectile."""
    z = zipfile.ZipFile(ASSETS)
    base = json.loads(z.read("Common/Items/Projectiles/WindStrike_Projectile.blockymodel"))
    w, h, px = load("Common/Items/Projectiles/WindStrike_Projectile_Texture.png")

    def tint(c, ramp):
        lum = ((0.3 * c[0] + 0.59 * c[1] + 0.11 * c[2]) / 255) ** 3     # texture tres claire : contraste renforce
        lo, hi = ramp
        return [lo[i] + (hi[i] - lo[i]) * lum for i in range(3)] + [c[3]]
    atlas = [[tint(px[y][x], SUN_FIRE) for x in range(w)] for y in range(h)] + \
            [[tint(px[y][x], MOON_ICE) for x in range(w)] for y in range(h)]
    png.write(os.path.join(OUT, "Items", "Projectiles", CROIX + ".png"), w, 2 * h, atlas)

    ids = iter(range(1000, 2000))

    def blade(name, roll, v_offset):
        children = copy.deepcopy(base["nodes"][0]["children"])

        def fix(n):
            n["id"] = str(next(ids))
            for face in (n.get("shape", {}).get("textureLayout") or {}).values():
                face["offset"]["y"] += v_offset
            for c in n.get("children", []):
                fix(c)
        for c in children:
            fix(c)
        half = math.radians(roll) / 2
        return {"id": str(next(ids)), "name": name, "position": {"x": 0, "y": 0, "z": 0},
                "orientation": {"x": 0, "y": 0, "z": round(math.sin(half), 6), "w": round(math.cos(half), 6)},
                "shape": {"type": "none", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": 1, "y": 1, "z": 1},
                          "settings": {"isPiece": False}, "textureLayout": {}, "unwrapMode": "custom", "visible": True,
                          "doubleSided": False, "shadingMode": "flat"},
                "children": children}
    model = copy.deepcopy(base)
    model["nodes"][0]["children"] = [blade("Lame_Soleil", 45, 0), blade("Lame_Lune", -45, h)]
    with open(os.path.join(OUT, "Items", "Projectiles", CROIX + ".blockymodel"), "w", encoding="utf-8") as f:
        json.dump(model, f, indent=2)
        f.write("\n")

    trail = json.loads(z.read("Server/Entity/Trails/Abilities/Ability_Windstrike_Trail.json"))
    trails_dir = os.path.join(RES, "Server", "Entity", "Trails")
    for name, (start, end) in {"Katanas_Croix_Trail_Soleil": ("#ffd27a", "#ff7a1a"),
                               "Katanas_Croix_Trail_Lune": ("#e4f1ff", "#7fa8ff")}.items():
        t = copy.deepcopy(trail)
        t["Start"]["Color"], t["End"]["Color"] = f"rgba({start}, 0.9)", f"rgba({end}, 0)"
        with open(os.path.join(trails_dir, name + ".json"), "w", encoding="utf-8") as f:
            json.dump(t, f, indent=2)
            f.write("\n")

    model_asset = {
        "$Comment": "Croix Jumelle / Eclipse des Jumeaux (Lames Jumelles) : croix de lames d'energie deplacee par le code (LamesJumelles). "
                    "HitBox elargie en X (LOD serveur). Genere par scripts/artefacts/gen_katanas_jumeaux.py.",
        "Model": "Items/Projectiles/" + CROIX + ".blockymodel",
        "Texture": "Items/Projectiles/" + CROIX + ".png",
        "MinScale": 1.0, "MaxScale": 1.0,
        "HitBox": {"Max": {"X": 1.2, "Y": 0.6, "Z": 0.6}, "Min": {"X": -1.2, "Y": -0.6, "Z": -0.6}},
        "Trails": [{"TrailId": "Katanas_Croix_Trail_" + lame, "TargetNodeName": node, "PositionOffset": {"X": x, "Z": 0.5}}
                   for node, lame in (("Lame_Soleil", "Soleil"), ("Lame_Lune", "Lune")) for x in (1.0, -1.0)],
        "Light": {"Color": "#ffe2b0", "Radius": 6},
    }
    with open(os.path.join(RES, "Server", "Models", "Projectiles", "Abilities", CROIX + ".json"), "w", encoding="utf-8") as f:
        json.dump(model_asset, f, indent=2, ensure_ascii=False)
        f.write("\n")
    neutral = {"Interactions": [{"Type": "Simple", "RunTime": 0.01}]}
    config = {"$Comment": "Croix des Lames Jumelles, deplacee par le code (LamesJumelles). Interactions neutres obligatoires.",
              "Model": CROIX, "UseModelScale": True,
              "Physics": {"Type": "Standard", "Gravity": 0, "TerminalVelocityAir": 1, "TerminalVelocityWater": 1,
                          "Bounciness": 0, "SticksVertically": False},
              "LaunchForce": 0, "SpawnOffset": {"X": 0, "Y": 0, "Z": 0},
              "Interactions": {"ProjectileHit": neutral, "ProjectileMiss": neutral}}
    with open(os.path.join(RES, "Server", "ProjectileConfigs", "Abilities", "Projectile_Config_" + CROIX + ".json"), "w", encoding="utf-8") as f:
        json.dump(config, f, indent=2, ensure_ascii=False)
        f.write("\n")


def item():
    iron = json.loads(zipfile.ZipFile(ASSETS).read("Server/Item/Items/Weapon/Sword/Weapon_Sword_Iron.json"))
    variables = {}
    for var, entry in iron["InteractionVars"].items():
        if var.startswith("Vortexstrike") or var == "Thrust_Damage":  # tourbillon et fente remplaces par les croix (Java)
            continue
        it = json.loads(json.dumps(entry["Interactions"][0]))
        if var in ON_HIT:
            it["DamageCalculator"]["BaseDamage"] = {k: round(v * MULT) for k, v in it["DamageCalculator"]["BaseDamage"].items()}
            it["Next"] = ON_HIT[var]
        else:                                                      # parade : 20 % d'endurance en moins
            it["StaminaCost"]["Value"] = round(it["StaminaCost"]["Value"] * 0.8, 2)
        variables[var] = {"Interactions": [it]}
    variables["Thrust_Force"] = {"Interactions": [{"Parent": "Weapon_Sword_Primary_Thrust_Force", "Force": THRUST_FORCE}]}
    variables["Thrust_Selector"] = {"Interactions": ["Katanas_Jumeaux_Croix"]}             # attaque maintenue Croix Jumelle
    data = {
        "$Comment": "Doubles katanas : epee (Template_Weapon_Sword) rendue dans les deux mains, modele du katana vanilla, animations Katanas_Jumeaux (Sword + bras gauche en miroir). "
                    "Texture, icone et ce fichier : scripts/artefacts/gen_katanas_jumeaux.py.",
        "Parent": "Template_Weapon_Sword",
        "TranslationProperties": {
            "Name": "Amaterasu et Tsukuyomi, Lames Jumelles",
            "Description": "Artefact légendaire. Deux katanas jumeaux, l'un forgé dans la lumière du soleil, l'autre dans celle de la lune. "
                           "Les deux lames frappent tour à tour : Amaterasu, à droite, embrase la cible (6 dégâts de feu par seconde pendant 3 s) ; "
                           "Tsukuyomi, à gauche, la ralentit (−40 % de vitesse pendant 2,5 s). "
                           "Le coup plongeant frappe des deux lames à la fois et réunit le soleil et la lune. "
                           "Maintenir, Croix Jumelle : les deux lames tranchent en croix et projettent une croix de lames d'énergie qui "
                           "traverse les ennemis à distance (90 dégâts, embrasement et givre). "
                           "Signature, Éclipse des Jumeaux : une croix géante file droit devant, transperce tout sur son passage "
                           "puis explose en feu et en glace. "
                           "Guerriers, Assassins et Dragons."},
        "Quality": "Legendary",
        "ItemLevel": 580,
        "Model": "Items/Weapons/Longsword/Katana.blockymodel",
        "PlayerAnimationsId": ANIM_SET,
        "Texture": "Items/Weapons/artefacts/" + NAME + ".png",
        "Icon": "Icons/ItemsGenerated/" + NAME + ".png",
        "IconProperties": {"Scale": 0.3, "Rotation": [45, 90, 0], "Translation": [-30, -30]},
        "Categories": ["Eldanior_System.Artefacts", "Items.Weapons"],
        "MaxStack": 1,
        "Variant": False,
        "Interactions": INTERACTIONS,
        "Weapon": {"EntityStatsToClear": ["SignatureEnergy"],
                   "StatModifiers": {"SignatureEnergy": [{"Amount": 20, "CalculationType": "Additive"}]},
                   "RenderDualWielded": True},
        "InteractionVars": variables,
        "MaxDurability": 0,
        "DurabilityLossOnHit": 0,
        "Trails": [{"TrailId": "Excalibur_Trail", "TargetNodeName": "Handle", "TargetEntityPart": "PrimaryItem",
                    "PositionOffset": {"X": 1.0, "Y": 0}}],
    }
    dst = os.path.join(RES, "Server", "Item", "Items", "Weapon", "Sword", NAME + ".json")
    with open(dst, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


if __name__ == "__main__":
    os.makedirs(os.path.join(OUT, "Items", "Weapons", "artefacts"), exist_ok=True)
    os.makedirs(os.path.join(OUT, "Icons", "ItemsGenerated"), exist_ok=True)
    texture()
    icon()
    n = animations()
    croix()
    item()
    print("ok,", n, "animations")
