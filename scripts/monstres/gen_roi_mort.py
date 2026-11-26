"""Boss Mondial « Roi de la Mort » (2026-11-19, demande de l'utilisateur : « un Wraith x5 de taille, blanc, avec des flammes
violettes ou noires ! a toi d'analyser le wraith comment il est fait »).

Analyse du Wraith du jeu (Assets.zip) :
  - Server/Models/Undead/Wraith.json : modele NPC/Undead/Wraith/Models/Model.blockymodel (Parent « Player » : squelette et
    animations du joueur), texture sombre a reflets turquoise, deux feux Fire_Blue (noeuds Pelvis et Hood-Top) ;
  - Server/NPC/Roles/Undead/Wraith.json : Variant de Template_Intelligent, faux du Vide, Root_NPC_Wraith_Attack, 193 PV.

Ici : meme modele et memes animations, texture recoloree (robe blanche, sangles noires, lueurs violettes), feux violets et noirs
(copies recolorees de Fireplace_Blue), role Variant du Wraith plus solide. La taille x5 et la couronnes d'or (1 a 3) sont posees par le
serveur (WorldBosses : Model.createScaledModel + attachement Couronnes_<N>_Roi de gen_emblemes_etoiles.py).

Sorties (src/main/resources) :
  Common/NPC/Eldanior/RoiDeLaMort/Texture.png
  Common/Particles/Textures/Eldanior/Feu_Violet.png, Feu_Noir.png
  Server/Models/Eldanior/Eldanior_Roi_De_La_Mort.json
  Server/NPC/Roles/Eldanior/Monstres/Eldanior_Roi_De_La_Mort.json
  Server/Particles/Eldanior/RoiDeLaMort/Feu_Violet_Roi.particlesystem, Feu_Noir_Roi.particlesystem (+ spawners)
Usage : python3 scripts/monstres/gen_roi_mort.py [src/main/resources] [dossier_apercu]
"""
import colorsys, json, os, sys, zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "rune_aura_icons"))
import png  # noqa: E402

RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "..", "..", "src", "main", "resources")
PREVIEW = sys.argv[2] if len(sys.argv) > 2 else None
ASSETS = os.path.expanduser("~/Library/Application Support/Hytale/install/release/package/game/latest/Assets.zip")
ROLE = "Eldanior_Roi_De_La_Mort"


def zread(z, name):
    return z.read(name)


def tmp_png(data):
    path = os.path.join(os.environ.get("TMPDIR", "/tmp"), "eldanior_roi_tmp.png")
    with open(path, "wb") as f:
        f.write(data)
    return png.read(path)


def write_json(rel, data):
    path = os.path.join(RES, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


# ===================================================================== texture du Roi

def recolor_wraith(px):
    """Robe sombre -> blanc ivoire (ombres gardees), turquoise -> violet, cuir et rouge -> noir violace."""
    out = []
    for row in px:
        line = []
        for r, g, b, a in row:
            if a == 0:
                line.append([0, 0, 0, 0])
                continue
            h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            deg = h * 360
            if s > 0.30 and 150 <= deg <= 210 and v > 0.25:                      # lueurs turquoise -> violet
                nr, ng, nb = colorsys.hsv_to_rgb(278 / 360, min(1, s * 1.05), min(1, v * 1.02))
                line.append([nr * 255, ng * 255, nb * 255, a])
            elif s > 0.35 and (deg < 45 or deg > 330):                           # cuir, rouge -> noir violace
                nr, ng, nb = colorsys.hsv_to_rgb(285 / 360, 0.35, 0.10 + v * 0.30)
                line.append([nr * 255, ng * 255, nb * 255, a])
            else:                                                                 # robe, capuche, chaines -> blanc ivoire
                lum = (0.3 * r + 0.59 * g + 0.11 * b) / 255
                k = 0.58 + 0.42 * min(1.0, lum * 2.4)
                line.append([246 * k, 241 * k, 233 * k, a])
        out.append(line)
    return out


# ===================================================================== flammes violettes et noires

FIRE_COLORS = {
    # couleur d'origine (Fireplace_Blue) -> violette / noire
    "Violet": {"#00fff4": "#d27bff", "#26b2ff": "#9b3dff", "#5514b7": "#3a0a6e", "#5bfff8": "#e3a8ff", "#6486b2": "#4b2a6e",
               "#681ca4": "#2a0648", "#773ded": "#5b1fa8", "#80d5fc": "#c48cff", "#825a98": "#3c2350", "#90f0f7": "#d9b4ff",
               "#d6feff": "#f2e2ff", "#ffffff": "#fbf2ff"},
    "Noir": {"#00fff4": "#5a2a7a", "#26b2ff": "#2a0f3e", "#5514b7": "#0a0410", "#5bfff8": "#6e3a8e", "#6486b2": "#140a1c",
             "#681ca4": "#06020a", "#773ded": "#1c0a2a", "#80d5fc": "#4a2266", "#825a98": "#120818", "#90f0f7": "#5e3480",
             "#d6feff": "#7a4a9a", "#ffffff": "#8e5cb0"},
}


def recolor_texture(px, mode):
    """Texture de flamme bleue (Fire_Center_Blue) -> violette ou noire a reflets violets."""
    out = []
    for row in px:
        line = []
        for r, g, b, a in row:
            if a == 0:
                line.append([0, 0, 0, 0])
                continue
            h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            if mode == "Violet":
                nr, ng, nb = colorsys.hsv_to_rgb(280 / 360, s, v)
            else:
                nr, ng, nb = colorsys.hsv_to_rgb(285 / 360, min(1, s * 0.8 + 0.2), v * 0.35)
            line.append([nr * 255, ng * 255, nb * 255, a])
        out.append(line)
    return out


def fire_system(z, mode):
    base = "Server/Particles/Item/Fireplace_Blue/"
    system = json.loads(zread(z, base + "Fire_Blue.particlesystem"))
    names = {"Fire_Blue_Smoke": "Fumee", "Fire_Up_Blue": "Monte", "Fire_Center_Blue": "Coeur", "Fire_Sparks_Blue": "Etincelles"}
    for sp in system["Spawners"]:
        old = sp["SpawnerId"]
        if old in names:
            sp["SpawnerId"] = "Roi_Feu_%s_%s" % (mode, names[old])
    system["$Comment"] = "Flamme %s du Roi de la Mort (gen_roi_mort.py), copie recoloree de Fire_Blue." % mode.lower()
    write_json("Server/Particles/Eldanior/RoiDeLaMort/Roi_Feu_%s.particlesystem" % mode, system)
    for old, new in names.items():
        text = zread(z, base + "Spawners/%s.particlespawner" % old).decode("utf-8")
        for src, dst in FIRE_COLORS[mode].items():
            text = text.replace('"%s"' % src, '"%s"' % dst).replace('"%s"' % src.upper(), '"%s"' % dst)
        text = text.replace("Particles/Textures/Fire/Fire_Center_Blue.png", "Particles/Textures/Eldanior/Roi_Feu_%s.png" % mode)
        data = json.loads(text)
        write_json("Server/Particles/Eldanior/RoiDeLaMort/Spawners/Roi_Feu_%s_%s.particlespawner" % (mode, new), data)


def main():
    z = zipfile.ZipFile(ASSETS)
    w, h, px = tmp_png(zread(z, "Common/NPC/Undead/Wraith/Models/Texture.png"))
    out_dir = os.path.join(RES, "Common", "NPC", "Eldanior", "RoiDeLaMort")
    os.makedirs(out_dir, exist_ok=True)
    roi = recolor_wraith(px)
    png.write(os.path.join(out_dir, "Texture.png"), w, h, roi)

    fw, fh, fpx = tmp_png(zread(z, "Common/Particles/Textures/Fire/Fire_Center_Blue.png"))
    tex_dir = os.path.join(RES, "Common", "Particles", "Textures", "Eldanior")
    os.makedirs(tex_dir, exist_ok=True)
    for mode in ("Violet", "Noir"):
        png.write(os.path.join(tex_dir, "Roi_Feu_%s.png" % mode), fw, fh, recolor_texture(fpx, mode))
        fire_system(z, mode)

    model = json.loads(zread(z, "Server/Models/Undead/Wraith.json"))
    model["Texture"] = "NPC/Eldanior/RoiDeLaMort/Texture.png"
    # 2026-11-20 (« les flammes ne sont pas bien placees ») : le moteur multiplie decalages et tailles par l'echelle du modele
    # (ModelParticle.scale) ; a x5, la flamme du sommet de la capuche (Hood-Top, Z +0,3) flottait 1,5 bloc devant le capuchon,
    # dans la couronne. Desormais : la traine de flammes sous la robe (comme le Wraith du jeu, vers le bas, au bas du bassin),
    # et un feu dans le visage, au fond de la capuche (os Head : pivot au cou, centre du visage 13 unites plus haut).
    model["Particles"] = [
        {"SystemId": "Roi_Feu_Violet", "PositionOffset": {"Y": -0.2}, "RotationOffset": {"Pitch": 180}, "TargetNodeName": "Pelvis"},
        {"SystemId": "Roi_Feu_Noir", "PositionOffset": {"Y": -0.2}, "RotationOffset": {"Pitch": 180}, "TargetNodeName": "Pelvis", "Scale": 0.7},
        {"SystemId": "Roi_Feu_Violet", "TargetNodeName": "Head", "Scale": 0.3, "PositionOffset": {"Y": 0.2, "Z": 0.1}},
    ]
    write_json("Server/Models/Eldanior/" + ROLE + ".json", model)

    # attaques : celles du Wraith, portee x3 (le Roi est x5 : la faux du jeu n'atteint que 3,5 blocs depuis son centre)
    names = ["Wraith_Scythe_Spin_Swing_Left", "Wraith_Scythe_Spin_Swing_Right", "Wraith_Scythe_Spin_Swing_Left_Heavy",
             "Wraith_Scythe_Spin_Swing_Right_Heavy"]
    for n in names:
        text = zread(z, "Server/Item/Interactions/NPCs/Undead/Wraith/%s.json" % n).decode("utf-8")
        data = json.loads(text)

        def widen(node):
            if isinstance(node, dict):
                sel = node.get("Selector")
                if isinstance(sel, dict) and "EndDistance" in sel:
                    sel["EndDistance"] = round(sel["EndDistance"] * 3, 2)
                    sel["ExtendTop"] = max(sel.get("ExtendTop", 0.5), 4.0)
                    sel["ExtendBottom"] = max(sel.get("ExtendBottom", 0.5), 1.0)
                for v in node.values():
                    widen(v)
            elif isinstance(node, list):
                for v in node:
                    widen(v)
        widen(data)
        write_json("Server/Item/Interactions/NPCs/Eldanior/" + n.replace("Wraith", "Roi_Mort") + ".json", data)
    root = json.loads(zread(z, "Server/Item/RootInteractions/NPCs/Undead/Root_NPC_Wraith_Attack.json"))
    root["Interactions"][0]["Next"] = [n.replace("Wraith", "Roi_Mort") for n in names]
    root["Interactions"][0]["ChainId"] = "Roi_Mort_Slashes"
    write_json("Server/Item/RootInteractions/NPCs/Eldanior/Root_NPC_Eldanior_Roi_Mort_Attack.json", root)

    role = json.loads(zread(z, "Server/NPC/Roles/Undead/Wraith.json"))
    mod = role["Modify"]
    mod["MaxHealth"] = 2500
    mod["Appearance"] = ROLE
    mod["DropList"] = "Drop_Wraith"
    mod["FlockArray"] = []
    mod["Attack"] = "Root_NPC_Eldanior_Roi_Mort_Attack"
    mod["AttackDistance"] = 8
    mod["TargetRange"] = 8
    mod["DesiredAttackDistanceRange"] = [4, 6]
    mod["HearingRange"] = 24
    mod["ViewRange"] = 40
    mod["IsMemory"] = False
    mod.pop("MemoriesCategory", None)
    mod["_InteractionVars"]["Melee_Damage"]["Interactions"][0]["DamageCalculator"]["BaseDamage"]["Physical"] = 160
    role["$Comment"] = ("Boss Mondial « Roi de la Mort » (gen_roi_mort.py) : Wraith du jeu, texture blanche, flammes violettes ; "
                        "taille x5, couronne, niveau et nom poses par WorldBosses.")
    write_json("Server/NPC/Roles/Eldanior/Monstres/" + ROLE + ".json", role)

    if PREVIEW:
        os.makedirs(PREVIEW, exist_ok=True)
        S = 3
        big = [[c for c in row for _ in range(S)] for row in roi for _ in range(S)]
        png.write(os.path.join(PREVIEW, "roi_texture.png"), w * S, h * S, big)
    print("Roi de la Mort ecrit dans", RES, "(texture %dx%d)" % (w, h))


if __name__ == "__main__":
    main()
