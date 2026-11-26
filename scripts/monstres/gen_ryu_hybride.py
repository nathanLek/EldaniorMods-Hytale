"""Kokuō-Ryū, forme hybride (demandes de l'utilisateur du 2026-11-23) : le corps du Loup-garou du jeu (rig, proportions,
animations), des cornes de dragon, la queue du Ryū et une texture aux couleurs du Dragon Céleste Impérial (noir et or).

  - Modele : copie du Loup-garou (memes noeuds, memes pivots), oreilles de loup retirees (les cornes prennent leur place),
    cornes d'or a andouillers du Kokuō-Ryū (gen_ryu_celeste.py, validees par l'utilisateur : meme dessin, a l'echelle de la
    tete du loup, couchees vers l'arriere) et longues moustaches de Ryū a bout dore.
  - Queue (v2) : la touffe du loup est remplacee par une queue de Ryū de 10 segments (meme construction que le Ryū : section
    arrondie, plaques dorsales, plaques ventrales or champagne, ligne laterale, cretes noires a pointe d'or, nageoires d'or
    sur la fin, touffe noire et or au bout). Tail et Tail2 restent les noeuds animes du loup ; Tail3-Tail10 ondulent grace a
    des copies des animations du loup (vague qui court vers le bout, amplitude selon l'allure).
  - Texture (v2) : passee a 2 px par unite (tailles x2, etirement 1/2 ; paupieres laissees a 1 px car leurs animations
    posent shapeStretch). Le dessin du loup sert de lumiere peinte (muscles, volumes, meches) sous les matieres du Ryū :
      corps -> ecailles noir obsidienne a bordure d'or ; echine -> rangee de plaques dorsales ; pectoraux et abdominaux ->
      plaques ventrales or champagne ; museau -> arete a clous d'or ; fourrure -> meches noires dont une sur quatre doree ;
      sourcils -> or ; yeux -> or a pupille fendue ; griffes noires a pointe d'or ; crocs ivoire ; gueule inchangee.
  - Vetements du loup : chemise dechiree retiree (v3 : elle faisait une armure doree) ; bandes des jambes noir et or.
  - v3 : queue reduite (epaisseur x1, longueur x0,8), epaules en ecailles (plus de bandes dorees).
  - Role : Variant de Template_Predator comme le Loup-garou du jeu (memes coups), plus solide.

Usage : python3 scripts/monstres/gen_ryu_hybride.py [src/main/resources] [dossier_apercu]
"""
import copy, json, math, os, sys, zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "..", "..", "src", "main", "resources")
PREVIEW = sys.argv[2] if len(sys.argv) > 2 else None
sys.argv = sys.argv[:1]                                   # gen_ryu_celeste lit sys.argv a l'import
sys.path.insert(0, HERE)
import gen_ryu_celeste as R  # noqa: E402  (palette, matieres, cornes, segments)
import png  # noqa: E402
import armor_kit as K  # noqa: E402

ASSETS = os.path.expanduser("~/Library/Application Support/Hytale/install/release/package/game/latest/Assets.zip")
Z = zipfile.ZipFile(ASSETS)
WW = "Common/NPC/Undead/Werewolf/Models/"
ROLE = "Eldanior_Ryu_Hybride"
REL = "NPC/Eldanior/RyuHybride"
UP = 2                                                    # pixels par unite de la nouvelle texture du corps

OBS, CHAR, GOLD, GOLD_ANT, GOLD_LUM, CHAMP, IVORY = R.OBS, R.CHAR, R.GOLD, R.GOLD_ANT, R.GOLD_LUM, R.CHAMP, R.IVORY
SHEEN, GAP = R.SHEEN, R.GAP
mix, smooth, clamp = R.mix, R.smooth, R.clamp


def zpng(name):
    path = os.path.join(HERE, "..", "..", "build", "_tmp_" + os.path.basename(name))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(Z.read(name))
    w, h, px = png.read(path)
    os.remove(path)
    return w, h, px


def lum(c):
    return (0.3 * c[0] + 0.59 * c[1] + 0.11 * c[2]) / 255.0


def h32(*a):
    return hash(a) & 0x7fffffff


# =====================================================================================================================
# Zones de texture : chaque face de chaque noeud -> rectangle de l'atlas du loup (a 1 px par unite)
# =====================================================================================================================
def face_dims(shape, face):
    s = shape["settings"]["size"]
    if shape["type"] == "quad":
        return s["x"], s["y"]
    return {"front": (s["x"], s["y"]), "back": (s["x"], s["y"]), "left": (s["z"], s["y"]), "right": (s["z"], s["y"]),
            "top": (s["x"], s["z"]), "bottom": (s["x"], s["z"])}[face]


def rects(node, out):
    s = node["shape"]
    for face, lay in s.get("textureLayout", {}).items():
        w, h = face_dims(s, face)
        if lay.get("angle", 0) in (90, 270):
            w, h = h, w
        out.append((node["name"], face, int(lay["offset"]["x"]), int(lay["offset"]["y"]), int(w), int(h)))
    for c in node["children"]:
        rects(c, out)


MANE = ("Fur", "Hair", "Beard", "Tail-End", "Face-Fur")
TEETH = ("Fang", "Teeth")
MOUTH = {("Jaw", "top"), ("Snout", "left"), ("Snout", "bottom")}
PLATES = {("Chest", "front"), ("Belly", "front")}
SPINE = {("Chest", "back"), ("Belly", "back"), ("Neck", "back")}
DROPPED = ("L-Ear", "R-Ear", "Tail-End")


def base_name(name):
    return name[2:] if name[:2] in ("L-", "R-") else name


def material(name, face):
    base = base_name(name)
    if name.startswith(DROPPED) or base in ("Tail", "Tail2"):
        return "skip"                                                       # zones des pieces retirees
    if base == "Eye":
        return "eye"
    if base == "Eye-Background":
        return "keep"
    if base.startswith("Claw"):
        return "claw"
    if base.startswith(TEETH) or base == "Fangs-Bot":
        return "ivory"
    if (base, face) in MOUTH:
        return "keep"
    if base == "Nose":
        return "nose"
    if base == "Eyebrow":
        return "brow"
    if (base, face) in PLATES:
        return "plates"
    if (base, face) in SPINE:
        return "spine"
    if (base, face) == ("Snout", "top"):
        return "snout"
    if base.startswith("Pelvis"):
        return "cloth"
    if base.startswith(MANE) or (base == "Head" and face == "top"):
        return "mane"
    if base.startswith("Eyelid"):
        return "lid"
    return "scale"


# =====================================================================================================================
# Matieres a 2 px par unite ; « k » = lumiere peinte par le jeu (pixel / moyenne de la face) : volumes et muscles gardes
# =====================================================================================================================
def scales(x, y, k, seed, gold_rate=0.1):
    """Ecailles noires imbriquees (9 px = 4,5 unites) a bordure d'or, reflet metallique, eclairees par le dessin du loup."""
    size, h = 11, 8.2
    row = math.floor(y / h)
    shift = size / 2 if row % 2 else 0
    col = math.floor((x + shift) / size)
    r = h32(seed, row, col)
    var = ((r % 1000) / 1000 - 0.5) * 0.16
    cx, ly = (x + shift) % size, y - row * h
    d = ((cx - size / 2) / (size / 2)) ** 2 + ((ly - 0.1 * h) / (h * 1.08)) ** 2
    base = mix(OBS, (58, 60, 70), smooth((k - 0.35) / 1.3))
    if d >= 1:
        return [v * 0.62 for v in base]
    c = [v * (1.18 - 0.36 * ly / h + var) for v in base]
    if ly < h * 0.42 and abs(cx - size / 2) < size * 0.3:
        c = mix(c, SHEEN, 0.3 - ly / h * 0.45)                                 # reflet en haut de l'ecaille
    if abs(cx - size / 2) < 0.6 and ly > h * 0.25:
        c = mix(c, SHEEN, 0.15)                                                 # arete centrale
    if d > 0.7 and ly > h * 0.42:
        edge = GOLD if (r >> 10) % 1000 < gold_rate * 1000 else [v * 0.72 for v in GOLD_ANT]
        c = mix(c, edge, 0.3 + 0.45 * (d - 0.7) / 0.3)                          # bordure d'or
    glint = max(0.0, math.sin(x * 0.045 + y * 0.022 + seed)) ** 14
    return mix(c, SHEEN, 0.35 * glint)


def scute(x, y, fw, k):
    """Plaque dorsale du Ryū : noire, liseré d'or antique, arete claire, joints dores tous les 12 px, clou d'or."""
    seg = 12
    ly = y % seg
    c = mix(OBS, CHAR, 0.35 + 0.25 * math.sin(ly / seg * math.pi))
    c = [v * (0.8 + 0.25 * min(k, 1.4)) for v in c]
    if x <= 1 or x >= fw - 2:
        return list(GOLD_ANT)
    if abs(x - fw / 2) < 1.3:
        c = mix(c, SHEEN, 0.5)
    if ly >= seg - 2:
        return mix(GOLD_ANT, GOLD, 0.4 if ly == seg - 2 else 0.0)
    if abs(x - fw / 2) + abs(ly - seg / 2) < 2.6:
        return mix(GOLD, GOLD_LUM, 0.5)
    return c


def mane(L, Lmax, x, y, seed):
    """Meches : noires (creux) a charbon ; une meche sur quatre doree de la racine a la pointe ; pointes claires dorees."""
    t = L / max(Lmax, 0.05)
    col = (x + seed) // 6
    golden = h32(seed, col) % 4 == 0
    streak = 0.9 + 0.1 * math.sin(x * 1.9 + seed)
    if golden:
        c = mix([v * 0.55 for v in GOLD_ANT], GOLD, smooth(t / 0.75))
        if t > 0.75:
            c = mix(GOLD, GOLD_LUM, (t - 0.75) / 0.25)
    elif t < 0.55:
        c = mix(OBS, (54, 56, 66), smooth(t / 0.55))
    else:
        g = smooth((t - 0.55) / 0.45)
        c = mix((54, 56, 66), GOLD_ANT, min(1.0, g * 1.4))
        if g > 0.7:
            c = mix(c, GOLD_LUM, (g - 0.7) / 0.3 * 0.7)
    return [v * streak for v in c]


def plates(x, y, k):
    """Plaques ventrales or champagne (bandes de 10 px), muscles du loup gardes par la lumiere peinte."""
    band = 10
    ly = y % band
    c = mix(GOLD_ANT, CHAMP, smooth((k - 0.4) / 1.0))
    c = mix(c, GOLD_LUM, 0.3 * max(0.0, 1 - ly / 3.0))
    if ly >= band - 2:
        c = [v * (0.55 if ly == band - 1 else 0.75) for v in c]
    if x % 18 == 0:
        c = mix(c, GOLD_ANT, 0.2)
    return [v * (0.75 + 0.3 * min(k, 1.4)) for v in c]


def eye_px(u, v):
    """Oeil du Ryū : iris d'or degrade a stries, pupille fendue verticale, reflet."""
    r = math.hypot(u - 0.5, v - 0.5)
    c = mix(GOLD_LUM, (255, 246, 190), 0.5) if r < 0.15 else mix(GOLD_LUM, GOLD, smooth((r - 0.15) / 0.2)) if r < 0.35 else mix(GOLD, R.AMBER, 0.75)
    if math.sin(math.atan2(v - 0.5, u - 0.5) * 12) > 0.8:
        c = mix(c, R.AMBER, 0.3)
    if abs(u - 0.5) < 0.07 + 0.03 * (1 - abs(v - 0.5) * 2):
        return (6, 4, 3)
    if 0.62 < u < 0.75 and 0.2 < v < 0.33:
        c = mix(c, (255, 255, 240), 0.85)
    return c


def paint(mat, name, face, x, y, fw, fh, L, Lmean, Lmax, seed):
    k = L / max(Lmean, 0.02)
    u, v = (x + 0.5) / fw, (y + 0.5) / fh
    if mat == "scale":
        return scales(x + seed, y, k, seed)
    if mat == "spine":
        half = min(10, fw // 5)
        if abs(x - fw / 2) < half:
            return scute(int(x - (fw / 2 - half)), y, 2 * half, k)
        return scales(x + seed, y, k, seed, gold_rate=0.25)
    if mat == "snout":
        if abs(x - fw / 2) < 4:
            c = scute(int(x - (fw / 2 - 4)), y, 8, k)
            return c
        return scales(x + seed, y, k, seed)
    if mat == "mane":
        return mane(L, Lmax, x, y, seed)
    if mat == "brow":
        return mix(GOLD_ANT, GOLD_LUM, smooth(L / max(Lmax, 0.05)))
    if mat == "plates":
        return plates(x, y, k)
    if mat == "eye":
        return eye_px(u, v)
    if mat == "claw":
        return mix(mix(OBS, CHAR, min(1.0, k * 0.6)), GOLD if v < 0.9 else GOLD_LUM, smooth((v - 0.5) / 0.35))
    if mat == "ivory":
        return mix([c * 0.55 for c in IVORY], IVORY, smooth(L / max(Lmax, 0.05)))
    if mat == "nose":
        return mix(OBS, SHEEN, 0.35 * smooth(k - 0.6))
    if mat == "cloth":                                                      # pagne : noir imperial, ourlet d'or
        c = mix(OBS, (48, 44, 40), smooth(k - 0.4))
        c = [cc * (0.9 + 0.1 * math.sin(x * 0.8)) for cc in c]
        if fh - 1 - y < 4:
            return mix(GOLD_ANT, GOLD, 0.5) if fh - 1 - y > 0 else list(GOLD_ANT)
        if y < 2:
            return list(GOLD_ANT)
        return c
    raise ValueError(mat)


def bilinear(Lmap, fx, fy):
    x0, y0 = math.floor(fx), math.floor(fy)
    tx, ty = fx - x0, fy - y0
    acc = wsum = 0.0
    for dx, dy, w in ((0, 0, (1 - tx) * (1 - ty)), (1, 0, tx * (1 - ty)), (0, 1, (1 - tx) * ty), (1, 1, tx * ty)):
        val = Lmap.get((x0 + dx, y0 + dy))
        if val is not None and w > 0:
            acc += val * w
            wsum += w
    return acc / wsum if wsum else None


def repaint(src, regions):
    """Texture du loup agrandie x2 puis repeinte zone par zone (la gueule et le fond des yeux gardent le dessin du jeu)."""
    w, h = len(src[0]), len(src)
    out = [[list(src[y // UP][x // UP]) for x in range(w * UP)] for y in range(h * UP)]
    lids = []
    for name, face, x0, y0, fw, fh in regions:
        mat = material(name, face)
        if mat in ("keep", "skip"):
            continue
        Lmap = {(x, y): lum(src[y][x]) for y in range(y0, min(h, y0 + fh)) for x in range(x0, min(w, x0 + fw)) if src[y][x][3] > 0}
        if not Lmap:
            continue
        if mat == "lid":
            lids.append((x0, y0, fw, fh, Lmap))
            continue
        Ls = list(Lmap.values())
        Lmean, Lmax = sum(Ls) / len(Ls), max(Ls)
        seed = sum(map(ord, base_name(name) + face)) % 997
        for j in range(fh * UP):
            for i in range(fw * UP):
                sx, sy = x0 + i // UP, y0 + j // UP
                if (sx, sy) not in Lmap:
                    continue
                L = bilinear(Lmap, x0 + (i + 0.5) / UP - 0.5, y0 + (j + 0.5) / UP - 0.5)
                c = paint(mat, name, face, i, j, fw * UP, fh * UP, Lmap[(sx, sy)] if L is None else L, Lmean, Lmax, seed)
                out[y0 * UP + j][x0 * UP + i] = [clamp(c[0]), clamp(c[1]), clamp(c[2]), src[sy][sx][3]]
    for x0, y0, fw, fh, Lmap in lids:                                     # paupieres : restees a 1 px par unite
        Lmean = sum(Lmap.values()) / len(Lmap)
        for (sx, sy), L in Lmap.items():
            c = mix(OBS, CHAR, min(1.0, L / max(Lmean, 0.02) * 0.7))
            if sy == y0:
                c = list(GOLD_ANT)
            out[y0 * UP + sy - y0][x0 * UP + sx - x0] = [clamp(c[0]), clamp(c[1]), clamp(c[2]), src[sy][sx][3]]
    return out


def upres_geometry(node):
    """Tailles x2, etirement 1/2, decalages d'UV x2 : meme forme a l'ecran, texture deux fois plus fine."""
    s = node["shape"]
    lid = base_name(node["name"]).startswith("Eyelid")
    for lay in s.get("textureLayout", {}).values():
        lay["offset"] = {"x": lay["offset"]["x"] * UP, "y": lay["offset"]["y"] * UP}
    if not lid and s["type"] in ("box", "quad"):
        for key in s["settings"]["size"]:
            s["settings"]["size"][key] *= UP
        for key in "xyz":
            s["stretch"][key] = round(s["stretch"][key] / UP, 6)
    for c in node["children"]:
        upres_geometry(c)


def recolor_gold(px, dark=False):
    """Vetements du loup (bleu canard) -> or ; « dark » : etoffe noire a reflets d'or (bandes des jambes)."""
    out = []
    for row in px:
        r2 = []
        for p in row:
            if p[3] == 0:
                r2.append(list(p))
                continue
            L = lum(p)
            if dark:
                c = mix(OBS, GOLD_ANT, smooth((L - 0.25) / 0.5))
            else:
                c = mix(GOLD_ANT, GOLD_LUM, smooth((L - 0.15) / 0.55)) if L > 0.15 else mix([v * 0.5 for v in GOLD_ANT], GOLD_ANT, L / 0.15)
            r2.append([clamp(c[0]), clamp(c[1]), clamp(c[2]), p[3]])
        out.append(r2)
    return out


# =====================================================================================================================
# Cornes et moustaches (repere du CENTRE de la tete du loup : +Z museau, +Y haut ; tete 35 x 32 x 31)
# =====================================================================================================================
HORN_SCALE = 0.56
HORN_ROOT = (8.5, 13.0, 1.0)                                  # sur le crane, devant les anciennes oreilles
K_ROOT = (8, 20, 14)                                          # racine des cornes du Ryū (repere de sa tete)
HORN_TILT = R.qmul(R.q_axis((0, 0, 1), -8), R.q_axis((1, 0, 0), -18))   # couchees vers l'arriere, un peu ecartees


def horn_pt(p):
    d = K.qrot(HORN_TILT, [(p[i] - K_ROOT[i]) * HORN_SCALE for i in range(3)])
    return tuple(HORN_ROOT[i] + d[i] for i in range(3))


def build_horns():
    beam = [(8, 20, 14), (13, 38, 6), (18, 56, -4), (22, 74, -16), (23, 90, -30), (20, 103, -44)]
    tines = [
        [(9, 28, 12), (14, 36, 22), (15, 42, 28)],
        [(13, 38, 6), (18, 50, 16), (20, 60, 22), (19, 67, 25)],
        [(18, 56, -4), (30, 66, 0), (38, 72, 6)],
        [(22, 74, -16), (26, 90, -8), (27, 100, -3)],
        [(23, 90, -30), (33, 98, -36), (40, 100, -45)],
        [(16, 47, 0), (8, 58, 4), (5, 64, 6)],
    ]
    s = HORN_SCALE
    parts = R.chain("L-Horn", [horn_pt(p) for p in beam], 9 * s * 1.15, 4 * s * 1.15, "horn")
    for k, tp in enumerate(tines):
        parts += R.chain("L-Horn_Tine%d" % k, [horn_pt(p) for p in tp], 5 * s * 1.2, 2.5 * s * 1.2, "horn", g0=0.35, g1=1.0)
    parts.append(R.P("L-Horn_Base", HORN_ROOT, (8, 4, 8), "gold", R.q_axis((0, 1, 0), 45)))
    for p in parts:
        p["shape"]["shadingMode"] = "reflective"
    return parts


def build_whiskers():
    """Longues moustaches du Ryū : partent des cotes du museau, s'ecartent puis retombent ; bout dore."""
    pts = [(10.5, -5, 29), (18, -6, 32), (27, -10, 30), (35, -17, 24), (40, -26, 15), (42, -36, 6), (41, -45, -1)]
    return R.chain("L-Whisker", pts, 2.6, 1.2, "whisker")


# =====================================================================================================================
# Queue du Ryū. Repere « propre » de la queue : x lateral, +y vers le bout, +z dos (au repos). Dans le repere local du noeud
# Tail du loup (+Y vers l'arriere, roule de 45 degres), le lateral vaut (0.707, 0, -0.707) et le dos (0.707, 0, 0.707).
# =====================================================================================================================
TAIL_FRAME = R.basis_quat((0.707107, 0, -0.707107), (0, 1, 0))
# (noeud, longueur, largeur, hauteur, crete) ; Tail et Tail2 = noeuds animes du loup (Tail2 a 29 unites du pivot de Tail)
TAIL_K, TAIL_LEN = 1.0, 0.8                                   # v3 : queue reduite (demande de l'utilisateur)
TAIL = [("Tail", 29, 20, 18, 9), ("Tail2", 28 * TAIL_LEN, 18, 16.5, 8.5)] + \
       [("Tail%d" % (k + 3), L * TAIL_LEN, W, H, s) for k, (L, W, H, s) in enumerate(
           [(26, 16, 14.5, 8), (24, 14.5, 13, 7), (22, 13, 11.5, 6.5), (20, 11.5, 10.5, 6), (18, 10.5, 9.5, 5.5),
            (16, 9.5, 8.5, 5), (14, 8.5, 7.5, 4.5), (12, 7.5, 6.5, 4)])]
TAIL = [(n, L, W * TAIL_K, H * TAIL_K, s * TAIL_K) for n, L, W, H, s in TAIL]
CURL = [0, 0, -15, -12, -8, -3, 5, 10, 12, 14]               # descend vers le sol puis la pointe se releve
FINS = ("Tail6", "Tail7", "Tail8", "Tail9")
EXTRA = [t[0] for t in TAIL[2:]]


def tail_parts(name, L, W, H, spike):
    y = L / 2
    parts = [R.P(name + "_Core", (0, y, 0), (W * 0.8, L + 3, H), "scale", fmat={"front": "scale_top", "back": "belly"}),
             R.P(name + "_Side", (0, y, -H * 0.06), (W, L + 2, H * 0.7), "scale", fmat={"front": "scale_top", "back": "belly"}),
             R.P(name + "_Scute", (0, y, H / 2 + 0.6), (W * 0.42, L * 0.78, 2), "scute"),
             R.P(name + "_Belly", (0, y, -H / 2 - 0.7), (W * 0.62, L + 1, 3), "belly", fmat={"front": "scale"}),
             *R.sym(R.P("L-%s_Flank" % name, (W / 2 + 0.5, y, -H * 0.12), (1.5, L - 3, H * 0.3), "scale_big")),
             R.bone(name + "_Crest", (0, L * 0.28, H / 2), (0, L * 0.28 + spike * 0.75, H / 2 + spike), (2.4, spike * 0.45), "spike",
                    up=(1, 0, 0)),
             R.P(name + "_Orn", (0, L * 0.82, H / 2 + 1.4), (5, 5, 2), "gold", R.q_axis((0, 0, 1), 45))]
    if name in FINS:
        parts += R.sym(R.P("L-%s_Fin" % name, (W / 2 + 1.6, L * 0.55, 0), (1, L * 0.8, H * 0.95), "gold",
                           R.qmul(R.q_axis((0, 0, 1), -14), R.q_axis((0, 1, 0), 28))))
    return parts


def tail_tuft():
    """Touffe noire et or du bout de la queue (celle du Ryū, son repere : +Y dos, -Z vers le bout)."""
    tip = []
    for k in range(7):
        a = math.radians(-60 + 20 * k)
        tip += R.strand("Tail_Tuft_%d" % k, (math.sin(a) * 2, math.cos(a) * 2, -8), (math.sin(a) * 0.7, math.cos(a) * 0.5, -1),
                        (30 - abs(k - 3) * 3.5) * TAIL_K, 4.2 * TAIL_K, golden=(k % 2 == 1), droop=0.1)
    return R.G("Tail_Tuft", (0, 0, 0), R.q_axis((1, 0, 0), 90), tip)       # repere du Ryū -> repere propre de la queue


def build_tail(nodes):
    """Formes du loup retirees de Tail/Tail2 (noeuds gardes pour ses animations), segments du Ryū ajoutes."""
    nxt = None
    for idx in reversed(range(2, len(TAIL))):
        name, L, W, H, sp = TAIL[idx]
        ch = tail_parts(name, L, W, H, sp)
        if idx == len(TAIL) - 1:
            ch.append(tail_tuft())
        if nxt:
            ch.append(nxt)
        nxt = R.G(name, (0, TAIL[idx - 1][1], 0), R.q_axis((1, 0, 0), CURL[idx]), ch)
    none = {"type": "none", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": 1, "y": 1, "z": 1}, "settings": {}, "textureLayout": {},
            "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": "flat"}
    t1, t2 = nodes["Tail"], nodes["Tail2"]
    t1["shape"] = copy.deepcopy(none)
    t2["shape"] = copy.deepcopy(none)
    t2["position"] = {"x": 0, "y": TAIL[0][1], "z": 0}
    t1["children"] = [c for c in t1["children"] if c["name"] != "Tail2"]
    t1["children"] += [R.G("Tail_Frame", (0, 0, 0), TAIL_FRAME, tail_parts(*TAIL[0])), t2]
    t2["children"] = [R.G("Tail2_Frame", (0, 0, 0), TAIL_FRAME, tail_parts(*TAIL[1]) + [nxt])]


# =====================================================================================================================
# Animations : copies de celles du loup + ondulation de Tail3-Tail10 (vague qui court vers le bout, lacet + tangage)
# =====================================================================================================================
AMP = {"Idle": 5, "Walk": 9, "Walk_Backward": 8, "Run": 12, "Sprint": 14, "Crouch": 4, "Crouch_Walk": 8, "Crouch_Walk_Backward": 7,
       "Alerted": 8, "Search": 6, "Hurt": 7, "Hurt2": 7, "Hurt3": 7, "Death": 3, "Laydown": 2, "Sleep": 2, "Wake": 4}


def tail_tracks(dur, amp):
    cyc = max(1, round(dur / 80))
    tracks = {}
    for k, name in enumerate(EXTRA):
        keys = []
        n = 8 * cyc
        for j in range(n + 1):
            f = j / n
            ph = 2 * math.pi * (f * cyc - 0.11 * (k + 1))
            yaw = amp * (0.6 + 0.16 * k) * math.sin(ph)
            pitch = amp * 0.35 * (0.6 + 0.12 * k) * math.sin(ph + math.pi / 2)
            q = R.qmul(R.q_axis((0, 0, 1), yaw), R.q_axis((1, 0, 0), pitch))
            keys.append({"time": int(round(f * dur)), "delta": R.r6(q), "interpolationType": "smooth"})
        tracks[name] = {"position": [], "orientation": keys, "shapeStretch": [], "shapeVisible": [], "shapeUvOffset": []}
    return tracks


def write_animations():
    m = json.loads(Z.read("Server/Models/Undead/Werewolf.json"))
    remap = {}
    for v in m["AnimationSets"].values():
        for an in v["Animations"]:
            path = an["Animation"]
            if path in remap:
                continue
            a = json.loads(Z.read("Common/" + path))
            for dead in [n for n in a["nodeAnimations"] if n.startswith(DROPPED)]:
                del a["nodeAnimations"][dead]
            name = os.path.basename(path)[:-len(".blockyanim")]
            amp = AMP.get(name, 9 if name.startswith(("Swim", "Fly", "Fluid")) else 4)
            a["nodeAnimations"].update(tail_tracks(a["duration"], amp))
            rel = "%s/Animations/%s" % (REL, path.split("/Animations/", 1)[1])
            out = os.path.join(RES, "Common", rel)
            os.makedirs(os.path.dirname(out), exist_ok=True)
            with open(out, "w") as f:
                json.dump(a, f, indent=1)
            remap[path] = rel
    return remap


# =====================================================================================================================
# Apercu (rendu logiciel, faces tournees/miroir gerees)
# =====================================================================================================================
def face_uvs(lay, w, h):
    o = lay["offset"]
    ang = lay.get("angle", 0)
    mx, my = lay.get("mirror", {}).get("x"), lay.get("mirror", {}).get("y")
    out = []
    for (u, v) in [(0, 0), (1, 0), (1, 1), (0, 1)]:
        if mx:
            u = 1 - u
        if my:
            v = 1 - v
        for _ in range(ang // 90):
            u, v = 1 - v, u
        rw, rh = (h, w) if ang in (90, 270) else (w, h)
        out.append((o["x"] + u * rw, o["y"] + v * rh))
    return out


def quads_of(node, origin, q, tex, out):
    s = node["shape"]
    st = (s["stretch"]["x"], s["stretch"]["y"], s["stretch"]["z"])
    off = (s["offset"]["x"], s["offset"]["y"], s["offset"]["z"])
    center = tuple(origin[i] + K.qrot(q, off)[i] for i in range(3))

    def world(v):
        r = K.qrot(q, (v[0] * st[0], v[1] * st[1], v[2] * st[2]))
        return tuple(center[i] + r[i] for i in range(3))

    size = s.get("settings", {}).get("size")
    if s["type"] == "box" and s.get("visible", True):
        hx, hy, hz = size["x"] / 2.0, size["y"] / 2.0, size["z"] / 2.0
        faces = {
            "front": [(-hx, hy, hz), (hx, hy, hz), (hx, -hy, hz), (-hx, -hy, hz)],
            "back": [(hx, hy, -hz), (-hx, hy, -hz), (-hx, -hy, -hz), (hx, -hy, -hz)],
            "right": [(hx, hy, hz), (hx, hy, -hz), (hx, -hy, -hz), (hx, -hy, hz)],
            "left": [(-hx, hy, -hz), (-hx, hy, hz), (-hx, -hy, hz), (-hx, -hy, -hz)],
            "top": [(-hx, hy, -hz), (hx, hy, -hz), (hx, hy, hz), (-hx, hy, hz)],
            "bottom": [(-hx, -hy, hz), (hx, -hy, hz), (hx, -hy, -hz), (-hx, -hy, -hz)],
        }
        for face, verts in faces.items():
            lay = s["textureLayout"].get(face)
            if lay:
                out.append(([world(v) for v in verts], face_uvs(lay, *face_dims(s, face)), tex))
    elif s["type"] == "quad":
        hx, hy = size["x"] / 2.0, size["y"] / 2.0
        verts = [(-hx, hy, 0), (hx, hy, 0), (hx, -hy, 0), (-hx, -hy, 0)]
        n = s["settings"].get("normal", "+Z")
        if n in ("+Y", "-Y"):
            verts = [(x, 0, -y if n == "+Y" else y) for x, y, _ in verts]
        elif n in ("+X", "-X"):
            verts = [(0, y, -x if n == "+X" else x) for x, y, _ in verts]
        elif n == "-Z":
            verts = [(-x, y, 0) for x, y, _ in verts]
        out.append(([world(v) for v in verts], face_uvs(s["textureLayout"]["front"], hx * 2, hy * 2), tex))
    for c in node["children"]:
        p = K.qrot(q, (c["position"]["x"], c["position"]["y"], c["position"]["z"]))
        quads_of(c, tuple(center[i] + p[i] for i in range(3)), K.qmul(q, c["orientation"]), tex, out)


def render(models, yaw, pitch=12, size=520):
    quads = []
    for nodes, tex in models:
        for n in nodes:
            quads_of(n, (n["position"]["x"], n["position"]["y"], n["position"]["z"]), n["orientation"], tex, quads)
    g = 160
    solid = [([(-g, -0.5, -g), (g, -0.5, -g), (g, -0.5, g), (-g, -0.5, g)], [(0, 0)] * 4, (84, 104, 84))]
    return K.downscale(K.draw(quads, size * 2, yaw, pitch, solid=solid, margin=0.04), 2)


def attach_nodes(att, body):
    """Les racines d'un attachement se fondent dans l'os du meme nom : on les rend au centre monde de cet os."""
    piv = {}

    def walk(n, origin, q):
        p = K.qrot(q, (n["position"]["x"], n["position"]["y"], n["position"]["z"]))
        pivot = tuple(origin[i] + p[i] for i in range(3))
        wq = K.qmul(q, n["orientation"])
        off = K.qrot(wq, (n["shape"]["offset"]["x"], n["shape"]["offset"]["y"], n["shape"]["offset"]["z"]))
        piv[n["name"]] = (tuple(pivot[i] + off[i] for i in range(3)), wq)
        for c in n["children"]:
            walk(c, piv[n["name"]][0], wq)
    for n in body:
        walk(n, (0, 0, 0), R.ID)
    out = []
    for n in att:
        c, q = piv.get(n["name"], ((0, 0, 0), R.ID))
        m = copy.deepcopy(n)
        m["position"] = {"x": c[0], "y": c[1], "z": c[2]}
        m["orientation"] = q
        out.append(m)
    return out


# =====================================================================================================================
# Fichiers
# =====================================================================================================================
def write_json(rel, data):
    path = os.path.join(RES, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


def model_asset(remap):
    m = json.loads(Z.read("Server/Models/Undead/Werewolf.json"))
    m = {"$Comment": "Kokuō-Ryū, forme hybride : corps et animations du Loup-garou, cornes d'or et queue du Ryū, texture noir "
                     "et or (gen_ryu_hybride.py).", **m}
    m["Model"] = REL + "/Model.blockymodel"
    m["Texture"] = REL + "/Texture.png"
    # v3 : la chemise dechiree du loup (torse, manches) retiree : recoloree en or, elle faisait une armure sur le torse,
    # les epaules et le dos ; seules les bandes des jambes restent
    m["DefaultAttachments"] = [
        {"Model": "NPC/Undead/Werewolf/Models/Attachments/Leg.blockymodel", "Texture": REL + "/Leg_Texture.png"},
    ]
    for v in m["AnimationSets"].values():
        for an in v["Animations"]:
            an["Animation"] = remap.get(an["Animation"], an["Animation"])
    return m


def role_file():
    ww = json.loads(Z.read("Server/NPC/Roles/Undead/Werewolf.json"))
    mod = ww["Modify"]
    mod["Appearance"] = ROLE
    mod["DropList"] = "Drop_Hunt"
    mod["MaxHealth"] = 1500
    mod["ViewRange"] = 32
    mod["HearingRange"] = 24
    mod["WanderRadius"] = 8
    mod["MaxSpeed"] = 11
    mod["IsMemory"] = False
    mod.pop("MemoriesCategory", None)
    mod["_InteractionVars"]["Melee_Damage"]["Interactions"][0]["DamageCalculator"]["BaseDamage"]["Physical"] = 140
    ww["Parameters"]["NameTranslationKey"]["Value"] = "server.npcRoles.%s.name" % ROLE
    return {"$Comment": "Kokuō-Ryū, forme hybride (gen_ryu_hybride.py) : Loup-garou du jeu (memes coups) aux cornes, a la queue "
                        "et aux ecailles du Dragon Céleste ; vie x5, degats x2.", **ww}


def lang_line():
    path = os.path.join(RES, "Server/Languages/en-US/server.lang")
    key = "npcRoles.%s.name=" % ROLE
    lines = open(path, encoding="utf-8").read().split("\n")
    lines = [ln for ln in lines if not ln.startswith(key)]
    i = max(k for k, ln in enumerate(lines) if ln.startswith("npcRoles.Eldanior_")) + 1
    lines.insert(i, key + "Kokuō-Ryū, forme hybride")
    open(path, "w", encoding="utf-8").write("\n".join(lines))


def main():
    rig = json.loads(Z.read(WW + "Model.blockymodel"))
    tw, th, tex = zpng(WW + "Texture.png")

    # ----- texture du corps x2 repeinte par zone (zones lues avant toute modification)
    regions = []
    for n in rig["nodes"]:
        rects(n, regions)
    body_tex = repaint(tex, regions)
    for n in rig["nodes"]:
        upres_geometry(n)
    TW, TH = tw * UP, th * UP

    # ----- oreilles et touffe de queue du loup retirees ; cornes, moustaches et queue du Ryū ajoutees
    nodes = {}

    def index(n):
        n["children"] = [c for c in n["children"] if not c["name"].startswith(DROPPED)]
        nodes[n["name"]] = n
        for c in n["children"]:
            index(c)
    for n in rig["nodes"]:
        index(n)
    R.atlas = R.Atlas()
    R._ids[0] = 90000
    nodes["Head"]["children"] += R.sym(*build_horns(), *build_whiskers())
    build_tail(nodes)

    # ----- zone des pieces du Ryū sous la texture du loup (rangee par hauteur)
    x, y, row = 0, TH, 0
    for ref, img in sorted(R.atlas.items, key=lambda it: (-len(it[1]), -len(it[1][0]))):
        h, w = len(img), len(img[0])
        if x + w > TW:
            x, y, row = 0, y + row, 0
        ref["x"], ref["y"] = x, y
        x += w
        row = max(row, h)
    H = (y + row + 31) // 32 * 32
    full = body_tex + [[[0, 0, 0, 0] for _ in range(TW)] for _ in range(H - TH)]
    for ref, img in R.atlas.items:
        for yy, r in enumerate(img):
            full[ref["y"] + yy][ref["x"]:ref["x"] + len(r)] = [list(p) for p in r]

    out_dir = os.path.join(RES, "Common", REL)
    os.makedirs(out_dir, exist_ok=True)
    with open(os.path.join(out_dir, "Model.blockymodel"), "w") as f:
        json.dump(rig, f, indent=1)
    png.write(os.path.join(out_dir, "Texture.png"), TW, H, full)
    lw, lh, lpx = zpng(WW + "Attachments/Leg_Texture.png")
    leg_tex = recolor_gold(lpx, dark=True)
    png.write(os.path.join(out_dir, "Leg_Texture.png"), lw, lh, leg_tex)

    remap = write_animations()
    write_json("Server/Models/Eldanior/%s.json" % ROLE, model_asset(remap))
    write_json("Server/NPC/Roles/Eldanior/Monstres/%s.json" % ROLE, role_file())
    lang_line()
    print("texture %dx%d (pieces du Ryū : %d faces), %d animations copiees" % (TW, H, len(R.atlas.items), len(remap)))

    if PREVIEW:
        os.makedirs(PREVIEW, exist_ok=True)
        leg = json.loads(Z.read(WW + "Attachments/Leg.blockymodel"))["nodes"]
        models = [(rig["nodes"], full), (attach_nodes(leg, rig["nodes"]), leg_tex)]
        imgs = [render(models, yaw) for yaw in (20, 90, 160, -125)]
        w, h, sheet = R.sheet(imgs, 4)
        png.write(os.path.join(PREVIEW, "ryu_hybride.png"), w, h, sheet)
        head = [render([(rig["nodes"], full)], yaw, 8, 460) for yaw in (25, 120)]
        w, h, sheet = R.sheet(head, 2)
        png.write(os.path.join(PREVIEW, "ryu_hybride_gros_plan.png"), w, h, sheet)
        print("apercus ->", PREVIEW)


if __name__ == "__main__":
    main()
