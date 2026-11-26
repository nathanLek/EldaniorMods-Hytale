"""Boss Mondial « Ursaroth, l'Ours des Braises Noires » (2026-11-20, demande de l'utilisateur : « cree un Ours Geant Noir avec le
design de Bear_Voidtaken, noir avec des flammes rouges, avec un nom qui sonne bien »).

Analyse de l'Ours du Vide du jeu (Assets.zip) :
  - Server/NPC/Roles/Creature/Hunt/Bear_Voidtaken.json : Variant de Template_Bear_Voidtaken_Boss (charge, frappe en cone, tempete
    de tentacules du Vide), apparence Bear_Grizzly_Voidtaken, kit de combat CAE_Bear_Voidtaken, 850 PV ;
  - Server/Models/Beast/Bear_Grizzly_Voidtaken.json : modele du grizzly, texture violette du Vide + attachement (yeux, gueule).

Ici : meme modele, memes animations et memes competences ; fourrure noire, pointes de la criniere en braise rouge, yeux et gueule
rouge vif, flammes rouges (copies recolorees de Fireplace_Blue) sur la criniere, le dos et les pattes avant. Ses coups de base
(griffes gauche/droite, pietinement) sont copies avec une portee x2,5 : agrandi x3 par le serveur, l'ours du jeu ne touchait
qu'a 4 blocs de son centre, a peine devant son museau. Taille, couronnes, niveau et nom poses par WorldBosses.

Sorties (src/main/resources) :
  Common/NPC/Eldanior/Ursaroth/Texture.png, Attachment_Texture.png
  Common/Particles/Textures/Eldanior/Ursaroth_Feu.png
  Server/Particles/Eldanior/Ursaroth/Ursaroth_Feu.particlesystem (+ spawners)
  Server/Models/Eldanior/Eldanior_Ursaroth.json
  Server/NPC/Balancing/Eldanior/CAE_Eldanior_Ursaroth.json
  Server/Item/Interactions/NPCs/Eldanior/Ursaroth_*.json, Server/Item/RootInteractions/NPCs/Eldanior/Root_NPC_Eldanior_Ursaroth_Attack.json
  Server/NPC/Roles/Eldanior/Monstres/Eldanior_Ursaroth.json
Usage : python3 scripts/monstres/gen_ours_braises.py [src/main/resources] [dossier_apercu]
"""
import colorsys, json, os, re, sys, zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "rune_aura_icons"))
import png  # noqa: E402

RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "..", "..", "src", "main", "resources")
PREVIEW = sys.argv[2] if len(sys.argv) > 2 else None
ASSETS = os.path.expanduser("~/Library/Application Support/Hytale/install/release/package/game/latest/Assets.zip")
ROLE = "Eldanior_Ursaroth"
REACH = 2.5                     # portee des coups de base (agrandi x3)
DAMAGE = 6.0                    # degats de base x6 (niveau et couronnes s'y ajoutent)


def tmp_png(data):
    path = os.path.join(os.environ.get("TMPDIR", "/tmp"), "eldanior_ursaroth_tmp.png")
    with open(path, "wb") as f:
        f.write(data)
    return png.read(path)


def write_json(rel, data):
    path = os.path.join(RES, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


# ===================================================================== textures

def recolor_bear(px):
    """Fourrure violette du Vide -> noire ; pointes claires de la criniere -> braise rouge ; lueurs magenta -> rouge vif."""
    out = []
    for row in px:
        line = []
        for r, g, b, a in row:
            if a == 0:
                line.append([0, 0, 0, 0])
                continue
            h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            deg = h * 360
            if s > 0.45 and v > 0.55 and (deg > 280 or deg < 20):             # lueurs magenta (yeux, gueule) -> rouge vif
                nr, ng, nb = colorsys.hsv_to_rgb(6 / 360, 0.92, min(1.0, v * 1.05))
            elif v > 0.58:                                                     # pointes claires de la criniere -> braise
                t = min(1.0, (v - 0.58) / 0.35)
                nr, ng, nb = colorsys.hsv_to_rgb((4 + 22 * t) / 360, 0.9 - 0.25 * t, 0.55 + 0.4 * t)
            else:                                                              # fourrure -> noir (ombres gardees)
                nr, ng, nb = colorsys.hsv_to_rgb(355 / 360, 0.25, 0.035 + v * 0.42)
            line.append([nr * 255, ng * 255, nb * 255, a])
        out.append(line)
    return out


def recolor_glow(px):
    """Attachement (yeux, gueule du Vide) : tout en rouge braise, lumineux."""
    out = []
    for row in px:
        line = []
        for r, g, b, a in row:
            if a == 0:
                line.append([0, 0, 0, 0])
                continue
            h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            nr, ng, nb = colorsys.hsv_to_rgb(5 / 360, max(0.75, s), 0.35 + v * 0.65)
            line.append([nr * 255, ng * 255, nb * 255, a])
        out.append(line)
    return out


def red_hex(m):
    """Couleur d'un spawner de flamme bleue -> rouge (meme luminosite, teinte 0-25 degres selon la clarte)."""
    c = m.group(1)
    r, g, b = int(c[0:2], 16), int(c[2:4], 16), int(c[4:6], 16)
    h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
    nh = (2 + 22 * (1 - s) * v) / 360                                          # coeur clair un peu orange, bords rouges
    nr, ng, nb = colorsys.hsv_to_rgb(nh, min(1.0, s * 0.6 + 0.4), v)
    return '"#%02x%02x%02x"' % (int(nr * 255), int(ng * 255), int(nb * 255))


def fire_system(z):
    base = "Server/Particles/Item/Fireplace_Blue/"
    system = json.loads(z.read(base + "Fire_Blue.particlesystem"))
    names = {"Fire_Blue_Smoke": "Fumee", "Fire_Up_Blue": "Monte", "Fire_Center_Blue": "Coeur", "Fire_Sparks_Blue": "Etincelles"}
    for sp in system["Spawners"]:
        if sp["SpawnerId"] in names:
            sp["SpawnerId"] = "Ursaroth_Feu_" + names[sp["SpawnerId"]]
    system["$Comment"] = "Flamme rouge d'Ursaroth (gen_ours_braises.py), copie recoloree de Fire_Blue."
    write_json("Server/Particles/Eldanior/Ursaroth/Ursaroth_Feu.particlesystem", system)
    for old, new in names.items():
        text = z.read(base + "Spawners/%s.particlespawner" % old).decode("utf-8")
        text = re.sub(r'"#([0-9a-fA-F]{6})"', red_hex, text)
        text = text.replace("Particles/Textures/Fire/Fire_Center_Blue.png", "Particles/Textures/Eldanior/Ursaroth_Feu.png")
        write_json("Server/Particles/Eldanior/Ursaroth/Spawners/Ursaroth_Feu_%s.particlespawner" % new, json.loads(text))


def recolor_flame(px):
    out = []
    for row in px:
        line = []
        for r, g, b, a in row:
            if a == 0:
                line.append([0, 0, 0, 0])
                continue
            h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            nr, ng, nb = colorsys.hsv_to_rgb((3 + 20 * (1 - s)) / 360, min(1.0, s * 0.7 + 0.3), v)
            line.append([nr * 255, ng * 255, nb * 255, a])
        out.append(line)
    return out


# ===================================================================== attaques

def widen(node):
    if isinstance(node, dict):
        sel = node.get("Selector")
        if isinstance(sel, dict) and "EndDistance" in sel:
            sel["EndDistance"] = round(sel["EndDistance"] * REACH, 2)
            sel["ExtendTop"] = max(sel.get("ExtendTop", 0.5), 2.5)
            sel["ExtendBottom"] = max(sel.get("ExtendBottom", 0.5), 1.0)
        for v in node.values():
            widen(v)
    elif isinstance(node, list):
        for v in node:
            widen(v)


def scale_damage(vars_):
    for var in vars_.values():
        for inter in var.get("Interactions", []):
            dmg = inter.get("DamageCalculator", {}).get("BaseDamage", {})
            for k in list(dmg):
                dmg[k] = round(dmg[k] * DAMAGE)


def attacks(z):
    names = ["Bear_Grizzly_Swipe_Left", "Bear_Grizzly_Swipe_Right", "Bear_Grizzly_Stomp"]
    for n in names:
        data = json.loads(z.read("Server/Item/Interactions/NPCs/Beast/Bear_Grizzly/%s.json" % n))
        widen(data)
        write_json("Server/Item/Interactions/NPCs/Eldanior/%s.json" % n.replace("Bear_Grizzly", "Ursaroth"), data)
    root = json.loads(z.read("Server/Item/RootInteractions/NPCs/Creature/Mammal/Root_NPC_Bear_Grizzly_Attack.json"))
    root["Interactions"][0]["Next"] = [n.replace("Bear_Grizzly", "Ursaroth") for n in names]
    root["Interactions"][0]["ChainId"] = "Ursaroth_Slashes"
    write_json("Server/Item/RootInteractions/NPCs/Eldanior/Root_NPC_Eldanior_Ursaroth_Attack.json", root)

    cae = json.loads(z.read("Server/NPC/Balancing/CAE_Bear_Voidtaken.json"))
    ev = cae["CombatActionEvaluator"]
    basic = ev["ActionSets"]["Main"]["BasicAttacks"]
    basic["Attacks"] = ["Root_NPC_Eldanior_Ursaroth_Attack"]
    basic["MaxRange"] = round(basic.get("MaxRange", 4) * REACH, 1)
    scale_damage(basic.get("InteractionVars", {}))
    acts = ev["AvailableActions"]
    for name, cond_i, xr in (("ConeSlam", 1, [0, 10]), ("Charge", 1, [8, 30])):     # cone et charge : distances a l'echelle
        if name in acts:
            acts[name]["Conditions"][cond_i]["Curve"]["XRange"] = xr
            acts[name]["PostExecuteDistanceRange"] = [2, 8]
    cae["$Comment"] = "Kit de combat d'Ursaroth (gen_ours_braises.py) : CAE_Bear_Voidtaken, coups de base elargis x2,5, degats x6."
    write_json("Server/NPC/Balancing/Eldanior/CAE_Eldanior_Ursaroth.json", cae)


def main():
    z = zipfile.ZipFile(ASSETS)
    out_dir = os.path.join(RES, "Common", "NPC", "Eldanior", "Ursaroth")
    os.makedirs(out_dir, exist_ok=True)
    w, h, px = tmp_png(z.read("Common/NPC/Beast/Bear_Grizzly/Models/Voidtaken_Texture.png"))
    bear = recolor_bear(px)
    png.write(os.path.join(out_dir, "Texture.png"), w, h, bear)
    aw, ah, apx = tmp_png(z.read("Common/NPC/Beast/Bear_Grizzly/Models/Attachments/Voidtaken_Texture.png"))
    png.write(os.path.join(out_dir, "Attachment_Texture.png"), aw, ah, recolor_glow(apx))

    fw, fh, fpx = tmp_png(z.read("Common/Particles/Textures/Fire/Fire_Center_Blue.png"))
    tex_dir = os.path.join(RES, "Common", "Particles", "Textures", "Eldanior")
    os.makedirs(tex_dir, exist_ok=True)
    png.write(os.path.join(tex_dir, "Ursaroth_Feu.png"), fw, fh, recolor_flame(fpx))
    fire_system(z)

    model = json.loads(z.read("Server/Models/Beast/Bear_Grizzly_Voidtaken.json"))
    model["Texture"] = "NPC/Eldanior/Ursaroth/Texture.png"
    model["DefaultAttachments"] = [{"Model": "NPC/Beast/Bear_Grizzly/Models/Attachments/Voidtaken.blockymodel",
                                    "Texture": "NPC/Eldanior/Ursaroth/Attachment_Texture.png"}]
    # decalages en blocs, multiplies par l'echelle du modele (ModelParticle.scale) : criniere, dos, pattes avant
    model["Particles"] = [
        {"SystemId": "Ursaroth_Feu", "TargetNodeName": "Neck-Hair-Top", "Scale": 0.8},
        {"SystemId": "Ursaroth_Feu", "TargetNodeName": "Back-Hair", "Scale": 0.65},
        {"SystemId": "Ursaroth_Feu", "TargetNodeName": "R-Hand", "Scale": 0.35},
        {"SystemId": "Ursaroth_Feu", "TargetNodeName": "L-Hand", "Scale": 0.35},
    ]
    model["Icon"] = "Icons/ModelsGenerated/Bear_Grizzly_Voidtaken.png"
    write_json("Server/Models/Eldanior/" + ROLE + ".json", model)

    attacks(z)

    role = json.loads(z.read("Server/NPC/Roles/Creature/Hunt/Bear_Voidtaken.json"))
    mod = role["Modify"]
    mod["Appearance"] = ROLE
    mod["_CombatConfig"] = "CAE_Eldanior_Ursaroth"
    mod["MaxHealth"] = 3000
    mod["LeashDistance"] = 160
    mod["HardLeashDistance"] = 200
    mod["IsMemory"] = False
    mod.pop("MemoriesCategory", None)
    scale_damage(mod.get("_InteractionVars", {}))
    role["$Comment"] = ("Boss Mondial « Ursaroth, l'Ours des Braises Noires » (gen_ours_braises.py) : Ours du Vide du jeu, fourrure "
                        "noire, flammes rouges ; taille x3, couronnes, niveau et nom poses par WorldBosses.")
    write_json("Server/NPC/Roles/Eldanior/Monstres/" + ROLE + ".json", role)

    if PREVIEW:
        os.makedirs(PREVIEW, exist_ok=True)
        S = 2
        big = [[c for c in row for _ in range(S)] for row in bear for _ in range(S)]
        png.write(os.path.join(PREVIEW, "ursaroth_texture.png"), w * S, h * S, big)
    print("Ursaroth ecrit dans", RES, "(texture %dx%d)" % (w, h))


if __name__ == "__main__":
    main()
