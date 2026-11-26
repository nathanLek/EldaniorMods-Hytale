"""Trois Boss Mondiaux de plus (2026-11-20, demande de l'utilisateur : « cree 3 autres boss mondiaux dont un dragon noir et or
sur le design de Dragon_Frost »). S'ajoutent au Roi de la Mort (gen_roi_mort.py) et a Ursaroth (gen_ours_braises.py).

  - Vel'Kazhar, le Dragon d'Ebene et d'Or : modele Dragon_Frost du jeu ; cristaux de glace -> or, ecailles et ailes -> noir,
    flammes d'or (copie recoloree de Fireplace_Blue) au front et au bout de la queue. Le dragon du jeu n'a pas d'animation
    d'attaque (comme les dragons etoiles, gen_dragons_etoiles.py) : morsure sans animation, portee a l'echelle. x1,5.
  - Gorgoth, le Tyran des Cendres : Rex_Cave du jeu ; peau charbon, ventre de cendre, plumes en lave, feux sur l'echine. x2.
  - Ignarok, le Titan de Fer Ardent : Golem_Firesteel du jeu ; fer plus sombre, lave plus vive (garde ses feux). x3.

Pour chacun : texture recoloree, modele (Server/Models/Eldanior/<Role>.json), role combattant (Variant du role du jeu, vie,
degats x6, vue et laisse de boss, portee des coups elargie a la taille du boss). Taille, couronnes, niveau et nom poses par
WorldBosses (cle des couronnes : gen_emblemes_etoiles.py, CROWN_BOSSES).

Usage : python3 scripts/monstres/gen_boss_mondiaux.py [src/main/resources] [dossier_apercu]
"""
import colorsys, json, os, re, sys, zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "rune_aura_icons"))
import png  # noqa: E402

RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "..", "..", "src", "main", "resources")
PREVIEW = sys.argv[2] if len(sys.argv) > 2 else None
ASSETS = os.path.expanduser("~/Library/Application Support/Hytale/install/release/package/game/latest/Assets.zip")
Z = zipfile.ZipFile(ASSETS)
NAMES = Z.namelist()
DAMAGE = 6.0


def jread(name):
    return json.loads(Z.read(name))


def find(interaction_id):
    for n in NAMES:
        if n.startswith("Server/Item/Interactions/") and n.endswith("/" + interaction_id + ".json"):
            return n
    raise SystemExit("interaction introuvable : " + interaction_id)


def write_json(rel, data):
    path = os.path.join(RES, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


def tmp_png(data):
    path = os.path.join(os.environ.get("TMPDIR", "/tmp"), "eldanior_boss_tmp.png")
    with open(path, "wb") as f:
        f.write(data)
    return png.read(path)


def recolor(src, dst, fn):
    w, h, px = tmp_png(Z.read(src))
    out = []
    for row in px:
        line = []
        for r, g, b, a in row:
            if a == 0:
                line.append([0, 0, 0, 0])
                continue
            hh, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            nr, ng, nb = fn(hh * 360, s, v)
            line.append([max(0, min(255, nr * 255)), max(0, min(255, ng * 255)), max(0, min(255, nb * 255)), a])
        out.append(line)
    path = os.path.join(RES, "Common", dst)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    png.write(path, w, h, out)
    if PREVIEW:
        os.makedirs(PREVIEW, exist_ok=True)
        png.write(os.path.join(PREVIEW, os.path.basename(os.path.dirname(dst)) + ".png"), w, h, out)


def hsv(h, s, v):
    return colorsys.hsv_to_rgb((h % 360) / 360, max(0.0, min(1.0, s)), max(0.0, min(1.0, v)))


def widen(node, factor, top):
    if isinstance(node, dict):
        sel = node.get("Selector")
        if isinstance(sel, dict) and "EndDistance" in sel:
            sel["EndDistance"] = round(sel["EndDistance"] * factor, 2)
            sel["ExtendTop"] = max(sel.get("ExtendTop", 0.5), top)
            sel["ExtendBottom"] = max(sel.get("ExtendBottom", 0.5), 1.0)
            for k in ("ExtendLeft", "ExtendRight"):
                if k in sel:
                    sel[k] = round(sel[k] * factor, 2)
        for v in node.values():
            widen(v, factor, top)
    elif isinstance(node, list):
        for v in node:
            widen(v, factor, top)


def scale_damage(vars_):
    for var in vars_.values():
        for inter in var.get("Interactions", []):
            dmg = inter.get("DamageCalculator", {}).get("BaseDamage", {})
            for k in list(dmg):
                dmg[k] = round(dmg[k] * DAMAGE)


def flame(name, hue_of):
    """Copie recoloree de la flamme bleue du jeu (Fireplace_Blue) : Server/Particles/Eldanior/<name>/<name>.particlesystem."""
    base = "Server/Particles/Item/Fireplace_Blue/"
    system = jread(base + "Fire_Blue.particlesystem")
    parts = {"Fire_Blue_Smoke": "Fumee", "Fire_Up_Blue": "Monte", "Fire_Center_Blue": "Coeur", "Fire_Sparks_Blue": "Etincelles"}
    for sp in system["Spawners"]:
        if sp["SpawnerId"] in parts:
            sp["SpawnerId"] = name + "_" + parts[sp["SpawnerId"]]
    system["$Comment"] = "Flamme de Boss Mondial (gen_boss_mondiaux.py), copie recoloree de Fire_Blue."
    write_json("Server/Particles/Eldanior/%s/%s.particlesystem" % (name, name), system)

    def recolor_hex(m):
        c = m.group(1)
        r, g, b = int(c[0:2], 16), int(c[2:4], 16), int(c[4:6], 16)
        h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
        nr, ng, nb = hsv(hue_of(s, v), min(1.0, s * 0.6 + 0.4), v)
        return '"#%02x%02x%02x"' % (int(nr * 255), int(ng * 255), int(nb * 255))

    for old, new in parts.items():
        text = Z.read(base + "Spawners/%s.particlespawner" % old).decode("utf-8")
        text = re.sub(r'"#([0-9a-fA-F]{6})"', recolor_hex, text)
        text = text.replace("Particles/Textures/Fire/Fire_Center_Blue.png", "Particles/Textures/Eldanior/%s.png" % name)
        write_json("Server/Particles/Eldanior/%s/Spawners/%s_%s.particlespawner" % (name, name, new), json.loads(text))
    w, h, px = tmp_png(Z.read("Common/Particles/Textures/Fire/Fire_Center_Blue.png"))
    out = []
    for row in px:
        line = []
        for r, g, b, a in row:
            if a == 0:
                line.append([0, 0, 0, 0])
                continue
            hh, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            nr, ng, nb = hsv(hue_of(s, v), min(1.0, s * 0.7 + 0.3), v)
            line.append([nr * 255, ng * 255, nb * 255, a])
        out.append(line)
    path = os.path.join(RES, "Common", "Particles", "Textures", "Eldanior", name + ".png")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    png.write(path, w, h, out)


def copy_chain(root_src, root_dst, prefix_from, prefix_to, factor, top):
    """Racine d'attaque du jeu -> copie dont chaque coup a une portee x factor."""
    root = jread(root_src)
    nexts = root["Interactions"][0]["Next"]
    for n in nexts:
        data = jread(find(n))
        widen(data, factor, top)
        write_json("Server/Item/Interactions/NPCs/Eldanior/%s.json" % n.replace(prefix_from, prefix_to), data)
    root["Interactions"][0]["Next"] = [n.replace(prefix_from, prefix_to) for n in nexts]
    root["Interactions"][0]["ChainId"] = prefix_to + "_Slashes"
    write_json("Server/Item/RootInteractions/NPCs/Eldanior/" + root_dst + ".json", root)


# ===================================================================== Vel'Kazhar, dragon d'ebene et d'or

def dragon():
    role_id = "Eldanior_Vel_Kazhar"

    def color(h, s, v):
        if s > 0.12 and 120 <= h <= 250:
            if h < 178 and v < 0.85:                                      # membranes des ailes (vert-bleu) -> noir, reflets d'or
                return hsv(40, 0.45, 0.06 + v * 0.30)
            return hsv(44 - 8 * (1 - v), 0.55 + 0.35 * (1 - v), 0.45 + v * 0.55)   # cristaux de glace -> or
        if s <= 0.12 and v > 0.62:                                        # coeur blanc des cristaux -> or pale
            return hsv(46, 0.28 + 0.2 * (1 - v), 0.55 + v * 0.45)
        if s > 0.4 and (h > 300 or h < 20) and v > 0.3:                   # gueule : rouge garde, plus sombre
            return hsv(h, s, v * 0.8)
        return hsv(30, s * 0.25, 0.03 + v * 0.50)                          # ecailles -> noir

    recolor("Common/NPC/Elemental/Dragon_Frost/Models/Texture.png", "NPC/Eldanior/VelKazhar/Texture.png", color)
    flame("Feu_Or", lambda s, v: 40 + 12 * v)
    model = jread("Server/Models/Elemental/Dragon_Frost.json")
    model["Texture"] = "NPC/Eldanior/VelKazhar/Texture.png"
    model["Particles"] = [
        {"SystemId": "Feu_Or", "TargetNodeName": "Forehead", "Scale": 0.45},
        {"SystemId": "Feu_Or", "TargetNodeName": "SpikeTail", "Scale": 0.6},
    ]
    write_json("Server/Models/Eldanior/%s.json" % role_id, model)

    bite = {
        "$Comment": "Morsure de Vel'Kazhar (gen_boss_mondiaux.py) : le dragon du jeu n'a pas d'animation d'attaque ; portee a la taille x1,5.",
        "Type": "Simple", "RunTime": 0.6,
        "Next": {"Type": "Parallel", "Interactions": [
            {"Interactions": [{
                "Type": "Selector", "RunTime": 0.2,
                "Selector": {"Id": "Horizontal", "Direction": "ToLeft", "TestLineOfSight": True, "ExtendTop": 3.5, "ExtendBottom": 1.5,
                             "StartDistance": 0.5, "EndDistance": 9.5, "Length": 130, "RollOffset": 0, "YawStartOffset": -65,
                             "PitchOffset": -10},
                "HitEntity": {"Interactions": [{"Type": "Replace", "DefaultValue": {"Interactions": ["NPC_Attack_Melee_Damage"]},
                                                "Var": "Melee_Damage"}]},
                "Next": {"Type": "Simple", "RunTime": 0.8}}]},
            {"Interactions": [{"Type": "Simple", "RunTime": 0.25, "Effects": {"WorldSoundEventId": "SFX_Golem_Earth_Swing"}}]},
        ]},
    }
    write_json("Server/Item/Interactions/NPCs/Eldanior/Vel_Kazhar_Morsure.json", bite)
    write_json("Server/Item/RootInteractions/NPCs/Eldanior/Root_NPC_Eldanior_Vel_Kazhar_Attack.json",
               {"Interactions": ["Vel_Kazhar_Morsure"], "Tags": {"Attack": ["Melee"]}})
    role = json.load(open(os.path.join(RES, "Server/NPC/Roles/Eldanior/Monstres/Eldanior_Dragon_Etoile_Givre.json"), encoding="utf-8"))
    m = role["Modify"]
    m.update({"Appearance": role_id, "MaxHealth": 4000, "Attack": "Root_NPC_Eldanior_Vel_Kazhar_Attack", "AttackDistance": 8,
              "TargetRange": 8, "CombatBehaviorDistance": 13, "ViewRange": 40, "HearingRange": 32, "AlertedRange": 48,
              "LeashDistance": 120, "HardLeashDistance": 180, "LeashMinPlayerDistance": 16, "MaxSpeed": 8})
    m["_InteractionVars"] = {"Melee_Damage": {"Interactions": [{
        "Parent": "NPC_Attack_Melee_Damage",
        "DamageCalculator": {"Type": "Absolute", "BaseDamage": {"Fire": 420}, "RandomPercentageModifier": 0.1},
        "DamageEffects": {"Knockback": {"Force": 4}}}]}}
    role["$Comment"] = ("Boss Mondial « Vel'Kazhar, le Dragon d'Ebene et d'Or » (gen_boss_mondiaux.py) : Dragon_Frost du jeu noir et or ; "
                        "taille x1,5, couronnes, niveau et nom poses par WorldBosses.")
    write_json("Server/NPC/Roles/Eldanior/Monstres/%s.json" % role_id, role)


# ===================================================================== Gorgoth, tyran des cendres (Rex_Cave)

def rex():
    role_id = "Eldanior_Gorgoth"

    def color(h, s, v):
        if s > 0.25 and 170 <= h <= 260:                                   # plumes bleues -> lave
            return hsv(18 + 22 * v, 0.95, 0.55 + 0.45 * v)
        if s < 0.25 and v > 0.55:                                          # ventre, crocs -> cendre
            return hsv(25, 0.08, 0.35 + 0.45 * v)
        if s > 0.5 and v > 0.75 and (h < 45 or h > 340):                   # reflets vifs de la peau -> braise
            return hsv(12, 0.9, 0.85)
        return hsv(8, 0.25 + 0.2 * s, 0.05 + v * 0.38)                     # peau -> charbon rougeoyant

    recolor("Common/NPC/Beast/Rex_Cave/Models/Texture.png", "NPC/Eldanior/Gorgoth/Texture.png", color)
    model = jread("Server/Models/Beast/Rex_Cave.json")
    model["Texture"] = "NPC/Eldanior/Gorgoth/Texture.png"
    model["Particles"] = [
        {"SystemId": "Campfire_New_Cartoon", "TargetNodeName": "Chest-Spine", "Scale": 0.7},
        {"SystemId": "Campfire_New_Cartoon", "TargetNodeName": "Tail-Spine", "Scale": 0.55},
        {"SystemId": "Golem_Fire_Glow", "TargetNodeName": "Head", "Scale": 0.6},
    ]
    write_json("Server/Models/Eldanior/%s.json" % role_id, model)
    role = jread("Server/NPC/Roles/Creature/Reptile/Rex_Cave.json")
    m = role["Modify"]
    m.update({"Appearance": role_id, "FlockArray": [], "MaxHealth": 3500, "ViewRange": 32, "HearingRange": 32, "FleeRange": 0.5,
              "AttackDistance": 7, "TargetRange": 7, "CombatBehaviorDistance": 12, "LeashDistance": 120, "HardLeashDistance": 180,
              "LeashMinPlayerDistance": 16, "IsMemory": False, "KnockbackScale": 0})
    m.pop("MemoriesCategory", None)
    widen(m["_InteractionVars"], 2.0, 3.0)
    scale_damage(m["_InteractionVars"])
    role["$Comment"] = ("Boss Mondial « Gorgoth, le Tyran des Cendres » (gen_boss_mondiaux.py) : Rex_Cave du jeu charbon et lave ; "
                        "taille x2, couronnes, niveau et nom poses par WorldBosses.")
    write_json("Server/NPC/Roles/Eldanior/Monstres/%s.json" % role_id, role)


# ===================================================================== Ignarok, titan de fer ardent (Golem_Firesteel)

def golem():
    role_id = "Eldanior_Ignarok"

    def color(h, s, v):
        if s < 0.35 and v > 0.78:                                          # coeur blanc de la lave -> jaune incandescent
            return hsv(48, 0.35, 1.0)
        if s > 0.45 and (h < 55 or h > 340) and v > 0.45:                  # lave -> plus vive, plus doree
            return hsv(h + 6, min(1.0, s * 1.05), min(1.0, v * 1.15))
        return hsv(h, s * 0.6, v * 0.62)                                   # fer -> plus sombre

    recolor("Common/NPC/Elemental/Golem_Firesteel/Models/Texture.png", "NPC/Eldanior/Ignarok/Texture.png", color)
    model = jread("Server/Models/Elemental/Golem_Firesteel.json")
    model["Texture"] = "NPC/Eldanior/Ignarok/Texture.png"
    write_json("Server/Models/Eldanior/%s.json" % role_id, model)
    copy_chain("Server/Item/RootInteractions/NPCs/Elemental/Golem/Root_NPC_Golem_Firesteel_Attack.json",
               "Root_NPC_Eldanior_Ignarok_Attack", "Golem_Firesteel", "Ignarok", 2.6, 4.0)
    role = jread("Server/NPC/Roles/Elemental/Golem/Golem_Firesteel.json")
    m = role["Modify"]
    m.update({"Appearance": role_id, "MaxHealth": 4500, "Attack": "Root_NPC_Eldanior_Ignarok_Attack", "AttackDistance": 6.5,
              "TargetRange": 6.5, "DesiredAttackDistanceRange": [4.5, 6], "ViewRange": 32, "ViewSector": 240, "LeashDistance": 120,
              "HardLeashDistance": 180, "IsMemory": False, "WakingPeriod": [0.0, 24.0]})
    m.pop("MemoriesCategory", None)
    scale_damage(m.get("_InteractionVars", {}))
    role["$Comment"] = ("Boss Mondial « Ignarok, le Titan de Fer Ardent » (gen_boss_mondiaux.py) : Golem_Firesteel du jeu, fer sombre et "
                        "lave vive ; taille x3, couronnes, niveau et nom poses par WorldBosses.")
    write_json("Server/NPC/Roles/Eldanior/Monstres/%s.json" % role_id, role)


if __name__ == "__main__":
    dragon()
    rex()
    golem()
    print("Boss Mondiaux ecrits dans", RES)
