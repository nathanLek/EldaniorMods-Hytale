"""Boss Mondial « Kokuō-Ryū, le Dragon Céleste Impérial » (fiche de l'utilisateur du 2026-11-22 : Ryū japonais noir et or).

v2 (2026-11-22, « plus gros, plus impressionnant, plus realiste, il vole dans tous les sens en ondulant, les yeux a revoir ») :
  - VOL : role Variant de Template_Flying_Aggressive (controleur Fly du jeu : monte, descend, tourne, plonge sur sa cible,
    plane en cercle) au lieu du role terrestre ; morsure en attaque du gabarit (RangedAttack = racine de la morsure).
  - Animations de vol en 3D : vague en tire-bouchon (verticale + laterale dephasees) qui court du cou a la queue, derive en
    huit du corps entier (haut/bas, gauche/droite, diagonales) et roulis dans les virages ; vol stationnaire en S ;
    repos enroule (au sol) ; morsure en plongee ; rugissement ; blessure ; chute a la mort.
  - Corps : section arrondie (2 boites croisees), plaques dorsales, plaques ventrales bombees, ligne laterale d'ecailles,
    20 segments (Chest, Body1-9, Tail1-10) + 2 de cou ; profil qui enfle au milieu du corps et s'effile en longue queue.
  - Tete agrandie (x1,18) : museau en deux volumes, narines evasees, crocs, YEUX refaits (amande, iris d'or degrade a stries,
    pupille fendue, reflet, paupiere et arcade sourciliere a epines) ; cornes d'or inchangees (validees).
  - Texture a 2 px par unite (etirement 1/2), atlas range par hauteur.
Le reste comme la v1 : criniere noire a meches dorees, moustaches et barbillons articules, 4 pattes a griffes noires et
dorees, queue a nageoires d'or et touffe ; taille du boss x3 (WorldBosses), ~30 blocs de long.
Rappels : un enfant est place par rapport au CENTRE de forme du parent (ici offset nul partout) ; 64 unites = 1 bloc ;
une piste d'animation : orientation = liaison * delta.

Usage : python3 scripts/monstres/gen_ryu_celeste.py [src/main/resources] [dossier_apercu]
"""
import copy, json, math, os, random, sys

HERE = os.path.dirname(os.path.abspath(__file__))
for folder in ("armures", "rune_aura_icons"):
    sys.path.insert(0, os.path.join(HERE, "..", folder))
import png  # noqa: E402
import armor_kit as K  # noqa: E402  (rendu logiciel des apercus)

RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "..", "..", "src", "main", "resources")
PREVIEW = sys.argv[2] if len(sys.argv) > 2 else None
ROLE = "Eldanior_Ryu_Celeste"
REL = "NPC/Eldanior/RyuCeleste"
D = 2                                   # pixels de texture par unite
rnd = random.Random(1503)

# ----- Palette de la fiche -----
OBS = (0x10, 0x11, 0x14)          # noir obsidienne
CHAR = (0x20, 0x21, 0x24)         # noir charbon
GOLD = (0xD4, 0xA5, 0x2C)         # or imperial
GOLD_ANT = (0xA7, 0x7B, 0x20)     # or antique
GOLD_LUM = (0xF5, 0xD8, 0x79)     # or lumineux
CHAMP = (0xDC, 0xC5, 0x8D)        # or champagne
IVORY = (0xE9, 0xDF, 0xC8)        # ivoire
GAP = (5, 5, 7)
SHEEN = (84, 88, 104)             # reflet metallique
MOUTH, TONGUE = (86, 16, 24), (148, 42, 54)
AMBER = (176, 92, 14)             # bord de l'iris


def clamp(v):
    return max(0, min(255, int(round(v))))


def mix(a, b, t):
    t = max(0.0, min(1.0, t))
    return [a[i] + (b[i] - a[i]) * t for i in range(3)]


def smooth(t):
    t = max(0.0, min(1.0, t))
    return t * t * (3 - 2 * t)


# =====================================================================================================================
# Atlas : chaque face a sa zone ; places a la fin, les plus hautes d'abord (etageres)
# =====================================================================================================================
ATLAS_W = 2048


class Atlas:
    def __init__(self):
        self.items = []

    def put(self, img):
        ref = {"x": 0, "y": 0}
        self.items.append((ref, img))
        return ref

    def pack(self):
        x = y = row = 0
        for ref, img in sorted(self.items, key=lambda it: (-len(it[1]), -len(it[1][0]))):
            h, w = len(img), len(img[0])
            if x + w > ATLAS_W:
                x, y, row = 0, y + row, 0
            ref["x"], ref["y"] = x, y
            x += w
            row = max(row, h)
        self.H = (y + row + 31) // 32 * 32

    def texture(self):
        tex = [[[0, 0, 0, 0] for _ in range(ATLAS_W)] for _ in range(self.H)]
        for ref, img in self.items:
            for yy, r in enumerate(img):
                tex[ref["y"] + yy][ref["x"]:ref["x"] + len(r)] = [list(p) for p in r]
        return ATLAS_W, self.H, tex


atlas = Atlas()


# =====================================================================================================================
# Matieres (couleur avant eclairage), coordonnees en pixels de la face
# =====================================================================================================================
def scales(x, y, size, base, dark, seed, rim=0.7, gold_rate=0.08):
    """Ecailles imbriquees irregulieres : reflet metallique en haut, fine bordure d'or antique en bas, interstices noirs."""
    h = size * 0.75
    row = math.floor(y / h)
    shift = size / 2 if row % 2 else 0
    col = math.floor((x + shift) / size)
    r = random.Random(hash((seed, row, col)))
    jit, var, shine = r.uniform(-0.18, 0.18) * size, r.uniform(-0.1, 0.1), r.random()
    cx = (x + shift) % size
    ly = y - row * h
    d = ((cx - size / 2 - jit * 0.3) / (size / 2)) ** 2 + ((ly - 0.1 * h) / (h * 1.08)) ** 2
    if d >= 1:
        return list(GAP)
    c = [v * (1.12 - 0.34 * (ly / h) + var) for v in base]
    if ly < h * 0.4 and abs(cx - size / 2) < size * 0.3:                        # reflet en haut de l'ecaille
        c = mix(c, SHEEN, 0.5 - ly / h)
    if abs(cx - size / 2) < size * 0.06 and ly > h * 0.2:                       # arete centrale (relief)
        c = mix(c, SHEEN, 0.18)
    if d > 0.8 and ly > h * 0.45:                                               # bordure d'or (vive sur quelques ecailles)
        edge = GOLD if shine < gold_rate else [v * 0.75 for v in GOLD_ANT]
        c = mix(c, edge, rim * (0.3 + 0.45 * (d - 0.8) / 0.2))
    elif d > 0.72:
        c = mix(c, dark, 0.55)
    glint = max(0.0, math.sin(x * 0.07 + y * 0.035 + seed)) ** 12                # reflets metalliques en diagonale
    return mix(c, SHEEN, 0.32 * glint)


def plates(x, y, fw, fh, band=11):
    """Plaques ventrales or champagne bombees, segmentees en travers, bords plus sombres."""
    ly = y % band
    if ly >= band - 2:
        return [v * (0.45 if ly == band - 1 else 0.7) for v in GOLD_ANT]
    t = ly / (band - 3)
    c = mix(mix(CHAMP, GOLD_LUM, 0.45), CHAMP, smooth(t * 2)) if t < 0.5 else mix(CHAMP, [v * 0.82 for v in CHAMP], (t - 0.5) * 2)
    u = (x + 0.5) / fw
    c = [v * (0.8 + 0.25 * math.sin(math.pi * u) ** 0.6) for v in c]           # bombe
    if x % 9 == 0:
        c = mix(c, GOLD_ANT, 0.22)                                              # stries fines
    e = min(x, fw - 1 - x)
    if e < 3:
        c = mix(c, GOLD_ANT, 0.7 - 0.2 * e)
    return c


def gold(x, y, fw, fh):
    e = min(x, y, fw - 1 - x, fh - 1 - y)
    c = list(GOLD)
    if e == 0:
        c = list(GOLD_ANT) if (x == fw - 1 or y == fh - 1) else list(GOLD_LUM)
    elif fw > 8 and fh > 8 and ((x + y) % 8 == 0 or (x - y) % 8 == 0):
        c = mix(c, GOLD_ANT, 0.45)                                              # gravure en losanges
    return [v * (1.12 - 0.25 * y / max(1, fh - 1)) for v in c]


def horn(t, x, y, fh):
    """Or des cornes : antique a la base, imperial, lumineux aux pointes ; anneaux et gravures naturelles."""
    c = mix(GOLD_ANT, GOLD, smooth(t / 0.45)) if t < 0.45 else mix(GOLD, GOLD_LUM, smooth((t - 0.45) / 0.55))
    if x % 9 == 0:
        c = mix(c, GOLD_ANT, 0.55)
    if abs(math.sin(x * 0.3 + y * 0.8)) < 0.1:
        c = mix(c, (120, 84, 20), 0.5)
    if y < max(1, fh * 0.3):
        c = mix(c, GOLD_LUM, 0.35)
    return c


def fur(t, x, y, seed, golden):
    c = mix(OBS, CHAR, 0.5 + 0.5 * math.sin(y * 1.3 + seed * 0.7 + x * 0.08))
    if (y + seed) % 4 == 0:
        c = mix(c, (56, 58, 68), 0.5)                                           # brins clairs
    if golden and t > 0.5:
        c = mix(c, GOLD if t < 0.85 else GOLD_LUM, smooth((t - 0.5) / 0.35))
    elif t > 0.85:
        c = mix(c, (40, 40, 46), 0.6)
    return c


def eye(face, u, v, x, y, fw, fh):
    """Oeil en amande (face exterieure) : iris d'or degrade a stries, pupille fendue, reflet ; le reste = paupiere sombre."""
    if face not in ("left", "right"):
        return mix(OBS, CHAR, 0.6)
    fu = u if face == "right" else 1 - u                                        # 0 = avant du museau
    vc = 0.5 + 0.12 * (0.5 - fu)                                                # coin avant plus bas : regard menacant
    half = 0.44 * max(0.0, 1 - ((fu - 0.52) / 0.5) ** 2) ** 0.75
    dv = v - vc
    if abs(dv) > half:
        return [v_ * 0.9 for v_ in mix(OBS, CHAR, 0.5)]
    if abs(abs(dv) - half) < 0.07 or fu < 0.06 or fu > 0.96:
        return (4, 4, 5)                                                        # contour noir
    r = math.hypot((fu - 0.5) * 1.6, dv)
    c = mix(GOLD_LUM, (255, 246, 190), smooth(0.18 - r) * 1.0) if r < 0.18 else \
        mix(GOLD_LUM, GOLD, smooth((r - 0.18) / 0.2)) if r < 0.38 else mix(GOLD, AMBER, smooth((r - 0.38) / 0.25))
    ang = math.atan2(dv, fu - 0.5)
    if math.sin(ang * 14) > 0.75:
        c = mix(c, AMBER, 0.35)                                                 # stries de l'iris
    slit = 0.055 * (1 - (dv / max(half, 0.01)) ** 2) ** 0.5 + 0.012
    if abs(fu - 0.5) < slit:
        return (6, 4, 3)                                                        # pupille verticale
    if abs(fu - 0.5) < slit + 0.03:
        c = mix(c, (120, 40, 10), 0.6)
    if 0.6 < fu < 0.72 and -half * 0.75 < dv < -half * 0.35:
        c = mix(c, (255, 255, 240), 0.85)                                       # reflet
    return c


def base_color(spec, face, x, y, fw, fh, u, v):
    mat = spec.get("fmat", {}).get(face, spec["mat"])
    seed = spec["seed"]
    t0, t1 = spec.get("grad", (0, 1))
    along = {"front": u, "top": u, "bottom": u, "back": 1 - u, "left": 0.0, "right": 1.0}[face]
    t = t0 + (t1 - t0) * along
    if mat == "scale":
        return scales(x + seed, y, 13, OBS, CHAR, seed)
    if mat == "scale_top":
        return scales(x + seed, y, 15, (13, 14, 17), OBS, seed, rim=0.6, gold_rate=0.05)
    if mat == "scale_big":
        return scales(x + seed, y + 4, 17, CHAR, OBS, seed, rim=1.0, gold_rate=0.25)
    if mat == "scale_fine":
        return scales(x + seed, y, 9, OBS, CHAR, seed, rim=0.6)
    if mat == "scute":                                                          # plaque dorsale : noire, liseré d'or, arete
        e = min(x, y, fw - 1 - x, fh - 1 - y)
        if e == 0:
            return list(GOLD_ANT)
        c = mix(OBS, CHAR, 0.4 + 0.3 * math.sin(y * 0.5))
        if abs(x - fw / 2) < 1.2:
            c = mix(c, SHEEN, 0.5)
        if y % 12 == 0:
            c = mix(c, GOLD_ANT, 0.6)
        return c
    if mat == "brow":
        c = scales(x + seed, y + 3, 9, CHAR, OBS, seed, rim=0.8)
        return mix(c, GOLD, smooth((0.3 - v) / 0.2) * 0.9) if face not in ("top", "bottom") else mix(c, GOLD_ANT, 0.4)
    if mat == "skin":
        return mix(OBS, CHAR, 0.5 + 0.5 * math.sin(x * 0.4 + y * 0.25 + seed))
    if mat == "belly":
        return plates(x, y, fw, fh)
    if mat == "gold":
        return gold(x, y, fw, fh)
    if mat == "horn":
        return horn(t, x, y, fh)
    if mat in ("fur", "fur_gold"):
        return fur(t, x, y, seed, mat == "fur_gold")
    if mat == "crest":                                                          # pointe noire, bout dore (haut = v 0)
        c = mix(OBS, CHAR, 0.4 + 0.3 * math.sin(x * 0.7))
        return mix(c, GOLD_LUM if v < 0.15 else GOLD, smooth((0.45 - v) / 0.25))
    if mat == "spike":                                                          # epine noire a bout dore, le long de X
        c = mix(OBS, CHAR, 0.5 + 0.3 * math.sin(y * 0.9))
        return mix(c, GOLD_LUM if t > 0.9 else GOLD, smooth((t - 0.55) / 0.3))
    if mat == "claw":
        c = mix(OBS, CHAR, 0.3 + 0.4 * v)
        if y <= 1:
            c = mix(c, GOLD_ANT, 0.8)                                           # fil d'or sur l'arete
        return mix(c, GOLD_LUM if t > 0.92 else GOLD, smooth((t - 0.6) / 0.3))
    if mat == "whisker":
        return mix(mix(OBS, CHAR, 0.4 + 0.3 * math.sin(y * 1.0 + x)), GOLD_LUM if t > 0.9 else GOLD, smooth((t - 0.62) / 0.25))
    if mat == "ivory":
        return mix(IVORY, (190, 172, 140), v * 0.7)
    if mat == "mouth":
        return [c * (0.7 + 0.3 * v) for c in MOUTH]
    if mat == "tongue":
        return [c * (0.85 + 0.15 * math.sin(x * 0.3)) for c in TONGUE]
    if mat == "nose":
        c = mix(OBS, CHAR, 0.6)
        if face == "front":
            if abs(u - 0.5) < 0.03:
                return mix(c, GAP, 0.7)                                         # sillon median
            return mix(c, SHEEN, 0.25 * max(0.0, 1 - v * 2))
        if face == "top":
            for cx in (0.27, 0.73):                                             # narines fendues, vers l'avant du dessus
                if ((u - cx) / 0.1) ** 2 + ((v - 0.72) / 0.16) ** 2 < 1:
                    return list(GAP)
                if ((u - cx) / 0.15) ** 2 + ((v - 0.72) / 0.24) ** 2 < 1:
                    return mix(c, (70, 72, 82), 0.5)
            return scales(x + seed, y, 7, OBS, CHAR, seed, rim=0.4)
        return c
    if mat == "eye":
        return eye(face, u, v, x, y, fw, fh)
    raise ValueError(mat)


FACE_LIGHT = {"top": 1.18, "front": 1.0, "left": 0.9, "right": 0.9, "back": 0.84, "bottom": 0.74}
GLOW = {"eye"}


def paint_face(spec, face, fw, fh):
    img = []
    glow = spec["mat"] in GLOW
    for y in range(fh):
        row = []
        for x in range(fw):
            u, v = (x + 0.5) / fw, (y + 0.5) / fh
            c = base_color(spec, face, x, y, fw, fh, u, v)
            if glow:
                row.append([clamp(c[0]), clamp(c[1]), clamp(c[2]), 255])
                continue
            k = FACE_LIGHT[face] * spec.get("light", 1.0)
            if face in ("top", "bottom"):
                k *= 0.86 + 0.14 * math.sin(math.pi * u) * math.sin(math.pi * v)
            else:
                k *= 1.14 - 0.3 * v                                             # lumiere zenithale
                k *= 0.8 + 0.2 * math.sin(math.pi * u) ** 0.7                   # volume arrondi
            if min(x, y, fw - 1 - x, fh - 1 - y) < 2 and min(fw, fh) > 8:
                k *= 0.84                                                       # occlusion des aretes
            row.append([clamp(c[0] * k), clamp(c[1] * k), clamp(c[2] * k), 255])
        img.append(row)
    return img


# =====================================================================================================================
# Geometrie (toutes les formes ont un offset nul : enfants places par rapport au pivot du parent)
# =====================================================================================================================
ID = {"x": 0.0, "y": 0.0, "z": 0.0, "w": 1.0}
_ids = [0]
S_CTX = [1.0]                          # echelle courante (tete agrandie) : positions et tailles


def nid():
    _ids[0] += 1
    return str(_ids[0])


def face_uv(ref):
    return {"offset": ref, "mirror": {"x": False, "y": False}, "angle": 0}


def q_axis(axis, deg):
    a = math.radians(deg) / 2
    s = math.sin(a)
    n = math.sqrt(sum(c * c for c in axis))
    return {"x": axis[0] / n * s, "y": axis[1] / n * s, "z": axis[2] / n * s, "w": math.cos(a)}


qmul = K.qmul
qrot = K.qrot


def euler(pitch=0.0, yaw=0.0, roll=0.0):
    """Degres : lacet (Y) puis tangage (X) puis roulis (Z)."""
    return qmul(qmul(q_axis((0, 1, 0), yaw), q_axis((1, 0, 0), pitch)), q_axis((0, 0, 1), roll))


def qconj(q):
    return {"x": -q["x"], "y": -q["y"], "z": -q["z"], "w": q["w"]}


def norm(v):
    n = math.sqrt(sum(c * c for c in v)) or 1.0
    return [c / n for c in v]


def cross(a, b):
    return [a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]]


def basis_quat(X, Y):
    X = norm(X)
    Z = norm(cross(X, Y))
    Y = cross(Z, X)
    m = [[X[0], Y[0], Z[0]], [X[1], Y[1], Z[1]], [X[2], Y[2], Z[2]]]
    tr = m[0][0] + m[1][1] + m[2][2]
    if tr > 0:
        s = math.sqrt(tr + 1) * 2
        return {"w": s / 4, "x": (m[2][1] - m[1][2]) / s, "y": (m[0][2] - m[2][0]) / s, "z": (m[1][0] - m[0][1]) / s}
    if m[0][0] > m[1][1] and m[0][0] > m[2][2]:
        s = math.sqrt(1 + m[0][0] - m[1][1] - m[2][2]) * 2
        return {"w": (m[2][1] - m[1][2]) / s, "x": s / 4, "y": (m[0][1] + m[1][0]) / s, "z": (m[0][2] + m[2][0]) / s}
    if m[1][1] > m[2][2]:
        s = math.sqrt(1 + m[1][1] - m[0][0] - m[2][2]) * 2
        return {"w": (m[0][2] - m[2][0]) / s, "x": (m[0][1] + m[1][0]) / s, "y": s / 4, "z": (m[1][2] + m[2][1]) / s}
    s = math.sqrt(1 + m[2][2] - m[0][0] - m[1][1]) * 2
    return {"w": (m[1][0] - m[0][1]) / s, "x": (m[0][2] + m[2][0]) / s, "y": (m[1][2] + m[2][1]) / s, "z": s / 4}


def r6(q):
    return {k: round(q[k], 6) for k in "xyzw"}


def vec(v):
    return {"x": round(v[0], 3), "y": round(v[1], 3), "z": round(v[2], 3)}


SHAPES = [0]


def box(size, spec):
    sx, sy, sz = [max(1, int(round(s * D))) for s in size]
    spec = dict(spec)
    spec.setdefault("seed", rnd.randint(0, 999))
    dims = {"front": (sx, sy), "back": (sx, sy), "left": (sz, sy), "right": (sz, sy), "top": (sx, sz), "bottom": (sx, sz)}
    lay = {f: face_uv(atlas.put(paint_face(spec, f, w, h))) for f, (w, h) in dims.items()}
    SHAPES[0] += 1
    shading = "fullbright" if spec["mat"] in GLOW else spec.get("shading", "flat")
    s = 1.0 / D
    return {"type": "box", "offset": vec((0, 0, 0)), "stretch": {"x": s, "y": s, "z": s}, "settings": {"size": {"x": sx, "y": sy, "z": sz}},
            "textureLayout": lay, "unwrapMode": "custom", "visible": not spec.get("hidden"), "doubleSided": False, "shadingMode": shading}


def P(name, pos, size, mat, q=ID, ch=(), **spec):
    k = S_CTX[0]
    spec["mat"] = mat
    return {"id": nid(), "name": name, "position": vec([c * k for c in pos]), "orientation": r6(q),
            "shape": box([c * k for c in size], spec), "children": list(ch)}


def G(name, pos, q=ID, ch=()):
    """Noeud vide : pivot d'animation."""
    k = S_CTX[0]
    return {"id": nid(), "name": name, "position": vec([c * k for c in pos]), "orientation": r6(q),
            "shape": {"type": "none", "offset": vec((0, 0, 0)), "stretch": {"x": 1, "y": 1, "z": 1}, "settings": {}, "textureLayout": {},
                      "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": "flat"}, "children": list(ch)}


def Q(name, pos, img, q=ID):
    """Quad double face (alpha decoupe) a D px par unite, normale +Z locale."""
    k = S_CTX[0]
    h, w = len(img), len(img[0])
    SHAPES[0] += 1
    s = 1.0 / D
    return {"id": nid(), "name": name, "position": vec([c * k for c in pos]), "orientation": r6(q),
            "shape": {"type": "quad", "offset": vec((0, 0, 0)), "stretch": {"x": s, "y": s, "z": s}, "settings": {"size": {"x": w, "y": h}, "normal": "+Z"},
                      "textureLayout": {"front": face_uv(atlas.put(img))}, "unwrapMode": "custom", "visible": True, "doubleSided": True,
                      "shadingMode": "flat"}, "children": []}


def bone(name, a, b, thick, mat, up=(0, 1, 0), overlap=0.6, **spec):
    """Boite de a vers b (axe X local), section thick (ou (h, l))."""
    d = [b[i] - a[i] for i in range(3)]
    L = math.sqrt(sum(c * c for c in d))
    t = thick if isinstance(thick, (tuple, list)) else (thick, thick)
    if abs(sum(norm(d)[i] * up[i] for i in range(3))) > 0.97:
        up = (0, 0, 1) if abs(norm(d)[2]) < 0.9 else (1, 0, 0)
    return P(name, [(a[i] + b[i]) / 2 for i in range(3)], (L + min(t) * overlap, t[0], t[1]), mat, basis_quat(d, up), **spec)


def chain(name, pts, t0, t1, mat, up=(0, 1, 0), g0=0.0, g1=1.0):
    """Suite de segments qui s'affinent ; le degrade (corne, meche, epine) court sur toute la chaine."""
    n = len(pts) - 1
    return [bone("%s_%d" % (name, k), pts[k], pts[k + 1], max(1, t0 + (t1 - t0) * k / max(1, n - 1)), mat, up,
                 grad=(g0 + (g1 - g0) * k / n, g0 + (g1 - g0) * (k + 1) / n)) for k in range(n)]


def mirror(node):
    """Copie miroir (plan X = 0) : L-/R- echanges, positions et rotations reflechies, forme etiree en -X (meme texture)."""
    n = dict(node)
    n["id"] = nid()
    nm = node["name"]
    n["name"] = "R-" + nm[2:] if nm.startswith("L-") else "L-" + nm[2:] if nm.startswith("R-") else nm
    n["position"] = dict(node["position"], x=-node["position"]["x"])
    o = node["orientation"]
    n["orientation"] = {"x": o["x"], "y": -o["y"], "z": -o["z"], "w": o["w"]}
    s = dict(node["shape"])
    s["offset"] = dict(s["offset"], x=-s["offset"]["x"])
    s["stretch"] = dict(s["stretch"], x=-s["stretch"]["x"])
    n["shape"] = s                                                  # textureLayout partage (atlas place plus tard)
    n["children"] = [mirror(c) for c in node["children"]]
    return n


def sym(*parts):
    return list(parts) + [mirror(p) for p in parts]


def teeth_img(w, h, n, down=True):
    img = []
    for y in range(h):
        row = []
        for x in range(w):
            k = (x % (w / n)) / (w / n)
            tip = 1 - abs(k - 0.5) * 2
            yy = y if down else h - 1 - y
            c = mix(IVORY, (196, 180, 150), yy / max(1, h - 1))
            if abs(k - 0.5) > 0.42:
                c = mix(c, (150, 132, 104), 0.5)
            row.append([clamp(c[0]), clamp(c[1]), clamp(c[2]), 255] if yy <= tip * (h - 0.5) else [0, 0, 0, 0])
        img.append(row)
    return img


def add_v(a, b):
    return [a[i] + b[i] for i in range(3)]


def scl(a, k):
    return [a[i] * k for i in range(3)]


# =====================================================================================================================
# Criniere, moustaches, pattes
# =====================================================================================================================
def strand(name, root, direction, length, thick, golden, droop=0.0, curl=0.35):
    """Meche herissee : trois segments qui s'effilent et se couchent vers l'arriere (-Z) et le bas ; bout dore ou non."""
    d = norm(direction)
    pts = [tuple(root)]
    for frac in (0.4, 0.33, 0.27):
        pts.append(tuple(add_v(pts[-1], scl(d, length * frac))))
        d = norm(add_v(d, (0, -droop, -curl)))
    mat = "fur_gold" if golden else "fur"
    th = [(thick, thick * 0.85), (thick * 0.7, thick * 0.6), (max(1.5, thick * 0.42), max(1.5, thick * 0.38))]
    return [bone("%s_%s" % (name, "abc"[k]), pts[k], pts[k + 1], th[k], mat, grad=(k / 3, (k + 1) / 3)) for k in range(3)]


def mane_ring(prefix, center, rx, ry, z0, count, length, thick, back=1.0, gold_every=5, arc=115):
    """Couronne de meches autour du dessus d'un segment (angle 0 = dessus), orientees vers l'arriere et l'exterieur."""
    out = []
    for i in range(count):
        a = math.radians(-arc + 2 * arc * i / max(1, count - 1)) + rnd.uniform(-0.08, 0.08)
        radial = (math.sin(a), math.cos(a), 0)
        root = (center[0] + radial[0] * rx, center[1] + radial[1] * ry, z0 + rnd.uniform(-2, 2))
        direction = (radial[0] * 0.55 + rnd.uniform(-0.12, 0.12), radial[1] * 0.5 + 0.35 + rnd.uniform(-0.1, 0.1), -back)
        ln = length * rnd.uniform(0.8, 1.2) * (1.0 - 0.25 * abs(a) / math.radians(arc))
        out += strand("%s_%d" % (prefix, i), root, direction, ln, thick * rnd.uniform(0.85, 1.1), golden=(i % gold_every == gold_every // 2),
                      droop=0.18 + 0.25 * abs(math.sin(a)))
    return out


def whisker(prefix, segs, first_q, bend_yaw, bend_pitch, thick0, thick1, length):
    """Moustache articulee : pivots emboites (animes) ; chaque pivot porte une tige le long de +Z local."""
    node = None
    for k in reversed(range(segs)):
        t = max(1, thick0 + (thick1 - thick0) * k / max(1, segs - 1))
        rod = P("%s%d_Rod" % (prefix, k + 1), (0, 0, length / 2), (t, t, length + 1), "whisker", grad=(k / segs, (k + 1) / segs))
        ch = [rod] + ([node] if node else [])
        q = first_q if k == 0 else euler(bend_pitch, bend_yaw, 0)
        node = G("%s%d" % (prefix, k + 1), (0, 0, length) if k > 0 else (0, 0, 0), q, ch)
    return node


def leg(names, sizes, q_parts, at):
    """Patte (pointe vers le bas) : haut, bas, pied a 3 doigts + ergot, griffes noires et dorees, touffe au coude."""
    upper_n, lower_n, foot_n = names
    (uw, ul, ud), (lw, ll, ld), fw = sizes
    q_up, q_low, q_foot = q_parts
    toes = []
    for k, x in enumerate((-fw * 0.32, 0, fw * 0.32)):
        spread = (k - 1) * 14
        kv = (x * 0.25, -1.5, fw * 0.55)                                      # jointure, dans le repere de la base du doigt
        tipv = (math.sin(math.radians(spread)) * 3, -fw * 0.45, fw * 0.55)    # pointe, dans le repere de la jointure
        claw = G("L-%s_ClawJ%d" % (foot_n, k), kv, ID, [
            P("L-%s_Knuckle%d" % (foot_n, k), (0, 0, 0), (fw * 0.34,) * 3, "gold", q_axis((0, 1, 0), 45)),
            bone("L-%s_Claw%d" % (foot_n, k), (0, 0, 0), tipv, (fw * 0.27, fw * 0.3), "claw", up=(0, 0, 1), grad=(0.15, 1.0))])
        toes.append(G("L-%s_Toe%d" % (foot_n, k), (x, -2, fw * 0.35), ID, [
            bone("L-%s_ToeBone%d" % (foot_n, k), (0, 0, 0), kv, (fw * 0.36, fw * 0.4), "scale_fine"), claw]))
    toes += [bone("L-%s_Spur" % foot_n, (0, -1, -fw * 0.35), (0, -fw * 0.4, -fw * 0.85), (fw * 0.25,) * 2, "claw", up=(0, 0, 1), grad=(0.2, 1.0))]
    foot = G("L-" + foot_n, (0, -ll, 0), q_foot, [P("L-%s_Palm" % foot_n, (0, -1.5, 1.5), (fw, fw * 0.42, fw + 2), "scale_fine", fmat={"bottom": "skin"})] + toes)
    tuft = []
    for k in range(4):
        tuft += strand("L-%s_Tuft%d" % (lower_n, k), (rnd.uniform(-2, 2), -1 - k * 2.5, -ld / 2 + 1), (rnd.uniform(-0.3, 0.3), -0.3 - k * 0.2, -1),
                       20 - k * 2.5, 4.4, golden=(k == 1))
    lower = G("L-" + lower_n, (0, -ul, 0), q_low, [
        P("L-%s_Bone" % lower_n, (0, -ll / 2, 0), (lw, ll + 2, ld), "scale", fmat={"front": "belly"}),
        P("L-%s_Plate" % lower_n, (lw / 2 + 0.6, -ll / 2, 0), (2, ll * 0.6, ld * 0.7), "scale_big")] + tuft + [foot])
    return G("L-" + upper_n, at, q_up, [
        P("L-%s_Muscle" % upper_n, (0, -ul / 2 + 2, 0), (uw, ul + 4, ud), "scale"),
        P("L-%s_Bulge" % upper_n, (0, -ul * 0.3, ud * 0.12), (uw * 1.1, ul * 0.5, ud * 0.86), "scale_fine"),
        P("L-%s_Plate" % upper_n, (uw * 0.55 + 0.6, -ul * 0.35, 0), (2, ul * 0.55, ud * 0.75), "scale_big"),
        lower])


# =====================================================================================================================
# Corps serpentin
# =====================================================================================================================
# (nom, longueur, largeur, hauteur, pointe dorsale) : enfle au milieu du corps, longue queue effilee ; deux segments voisins
# n'ont jamais la meme taille (pas de faces coplanaires).
BODY = [("Chest", 32, 38, 36, 17)] + \
       [("Body%d" % (k + 1), 30, w, w - 2, 17 - k * 0.6) for k, w in enumerate((39, 41, 40, 39, 37, 36, 34, 33, 31))] + \
       [("Tail%d" % (k + 1), L, W, max(5, W - 2), s) for k, (L, W, s) in enumerate(
           [(28, 29, 11), (27, 27, 10), (26, 25, 9), (25, 22, 8), (24, 19, 7), (22, 16, 6), (20, 14, 5), (18, 12, 4), (16, 10, 3.5), (14, 8, 3)])]
NECK = [("Neck1", 24, 33, 32, 15), ("Neck2", 24, 30, 29, 14)]
SEGS = [b[0] for b in BODY[1:]]
ROOT_Y = 52                                    # pivot du poitrail au-dessus des pieds de l'entite (milieu de la boite)
HEAD_SCALE = 1.18


def segment_parts(name, L, W, H, spike, forward=False):
    """Geometrie d'un segment, du pivot vers -Z (ou +Z pour le cou) : section arrondie, plaques, ligne laterale, crete."""
    s = 1 if forward else -1
    zc = s * L / 2
    parts = [P(name + "_Core", (0, 0, zc), (W * 0.8, H, L + 3), "scale", fmat={"top": "scale_top", "bottom": "belly"}),
             P(name + "_Side", (0, -H * 0.06, zc), (W, H * 0.7, L + 2), "scale", fmat={"top": "scale_top", "bottom": "belly"}),
             P(name + "_Scute", (0, H / 2 + 0.6, zc), (W * 0.42, 2, L * 0.78), "scute"),
             P(name + "_Belly", (0, -H / 2 - 0.7, zc), (W * 0.62, 3, L + 1), "belly", fmat={"top": "scale"}),
             *sym(P("L-%s_Flank" % name, (W / 2 + 0.5, -H * 0.12, zc), (1.5, H * 0.3, L - 3), "scale_big"))]
    if spike > 0:
        parts += [P(name + "_Crest", (0, H / 2 + spike * 0.3, s * L * 0.38), (2.4, spike, spike * 0.6 + 2), "crest", q_axis((1, 0, 0), -38 * s)),
                  P(name + "_Orn", (0, H / 2 + 1.4, s * L * 0.85), (5, 2, 5), "gold", q_axis((0, 1, 0), 45))]
    return parts


def build_head():
    """Tete (repere : +Z vers le museau), construite a l'echelle HEAD_SCALE."""
    S_CTX[0] = HEAD_SCALE
    kids = [
        P("Skull", (0, 6, 12), (30, 24, 28), "scale_fine", fmat={"top": "scale_top"}),
        P("Skull_Back", (0, 4, 2), (27, 21, 12), "scale_fine"),
        P("Forehead", (0, 18.6, 17), (20, 3, 22), "scale_top"),
        P("Forehead_Gem", (0, 20.4, 22), (6, 2, 6), "gold", q_axis((0, 1, 0), 45)),
        P("Snout_Rear", (0, 2, 33), (24, 16, 18), "scale_fine", fmat={"top": "scale_top"}),
        P("Snout_Front", (0, 1, 48), (21, 13.5, 18), "scale_fine", fmat={"top": "scale_top"}),
        P("Snout_Ridge", (0, 8.6, 40), (9, 3, 30), "scale_top", ch=[P("Snout_Stud%d" % k, (0, 1.8, z), (3, 1, 3), "gold", q_axis((0, 1, 0), 45))
                                                                  for k, z in enumerate((-10, -2, 6))]),
        P("Nose", (0, 3.5, 58.5), (23, 11, 7), "nose"),
        P("Mouth", (0, -7.5, 40), (18, 3, 30), "mouth"),
        P("Chin_Mass", (0, -6, 16), (22, 6, 14), "scale_fine"),
    ]
    kids += sym(
        # -- oeil : orbite creusee, globe en amande qui depasse, paupieres, arcade a epines
        P("L-Eye_Socket", (13.4, 9.5, 28), (3, 13, 20), "skin", q_axis((0, 1, 0), 24)),
        P("L-Eye", (14.9, 9.6, 28.8), (2.4, 10.5, 18), "eye", q_axis((0, 1, 0), 24)),
        P("L-Lid_Up", (15.3, 15.0, 28.0), (4, 3, 21), "brow", euler(12, 24, -14)),
        P("L-Lid_Low", (15.0, 4.2, 28.8), (3, 2, 16), "scale_fine", euler(-6, 24, 10)),
        P("L-Blink_Lid", (16.2, 9.6, 28.2), (1.2, 11, 19), "scale_fine", q_axis((0, 1, 0), 24), hidden=True),
        P("L-Brow", (12.5, 17, 25), (7, 4, 20), "brow", euler(10, 14, -18)),
        *[bone("L-Brow_Spike%d" % k, (13 + k, 18 - k, 20 - k * 6), (22 + k * 3, 24 - k * 2, 4 - k * 8), (3.6 - k * 0.6, 2.4), "spike")
          for k in range(3)],
        P("L-Cheek", (14.5, 1, 14), (5, 14, 18), "scale_big"),
        P("L-Nostril_Flare", (9, 7.5, 57), (5, 3, 6), "scale_fine", euler(0, 20, -25)),
        P("L-Lip", (11.2, -5.5, 41), (2, 6, 30), "skin"),
        Q("L-Teeth_Up", (11.0, -9.6, 42), teeth_img(56, 10, 10), q_axis((0, 1, 0), 90)),
        P("L-Fang_Up", (9.5, -12.5, 54), (3.4, 11, 3.4), "ivory", euler(-8, 0, 0)),
        P("L-Fang_Up2", (10.5, -11, 46), (2.4, 7, 2.4), "ivory", euler(-4, 0, 0)),
        *[bone("L-Cheek_Spike%d" % k, (15, 4 - k * 6, 12), (29 - k * 2, 6 - k * 9, -6 - k * 2), (5 - k, 3), "spike") for k in range(3)],
        *[bone("L-Ear_Spike%d" % k, (13, 16, 6 - k * 3), (25 + k * 2, 22 + k * 3, -12 - k * 4), (4, 2.5), "spike") for k in range(2)],
    )
    # ---- cornes : merrain + andouillers (bois de cerf / corail), couronne autour de la tete (inchangees, validees)
    beam = [(8, 20, 14), (13, 38, 6), (18, 56, -4), (22, 74, -16), (23, 90, -30), (20, 103, -44)]
    tines = [
        [(9, 28, 12), (14, 36, 22), (15, 42, 28)],
        [(13, 38, 6), (18, 50, 16), (20, 60, 22), (19, 67, 25)],
        [(18, 56, -4), (30, 66, 0), (38, 72, 6)],
        [(22, 74, -16), (26, 90, -8), (27, 100, -3)],
        [(23, 90, -30), (33, 98, -36), (40, 100, -45)],
        [(16, 47, 0), (8, 58, 4), (5, 64, 6)],
    ]
    horn = chain("L-Horn", beam, 9, 4, "horn")
    for k, tp in enumerate(tines):
        horn += chain("L-Horn_Tine%d" % k, tp, 5, 2.5, "horn", g0=0.35, g1=1.0)
    for p in horn:
        p["shape"]["shadingMode"] = "reflective"
    kids += sym(*horn)
    # ---- criniere du crane
    kids.append(G("Mane_Head", (0, 0, 0), ch=mane_ring("Mane_Head_A", (0, 8), 13, 12, 6, 12, 36, 7.0, arc=95) +
                  mane_ring("Mane_Head_B", (0, 6), 15, 13, -1, 14, 44, 7.5, arc=125)))
    # ---- moustaches et barbillons
    kids += sym(whisker("L-Whisker", 6, euler(6, 42, 0), 21, 6, 3.6, 1.6, 16),
                whisker("L-Barbel_Cheek", 3, euler(25, 100, 0), -10, 15, 2.4, 1.4, 8))
    for c in kids:
        if c["name"].endswith("Whisker1"):
            c["position"] = vec(((12.5 if c["name"].startswith("L-") else -12.5) * HEAD_SCALE, -1 * HEAD_SCALE, 55 * HEAD_SCALE))
        if c["name"].endswith("Barbel_Cheek1"):
            c["position"] = vec(((15 if c["name"].startswith("L-") else -15) * HEAD_SCALE, -4 * HEAD_SCALE, 22 * HEAD_SCALE))
    # ---- machoire
    beard = []
    for k in range(5):
        x = (k - 2) * 3.5
        beard += strand("Beard_%d" % k, (x, -6, 16 - abs(k - 2) * 2), (x * 0.05, -1, -0.5), 18 - abs(k - 2) * 3, 3.6, golden=(k == 2))
    chin = whisker("L-Barbel_Chin", 3, euler(40, 30, 0), -8, 14, 2.2, 1.2, 7)
    chin["position"] = vec((7 * HEAD_SCALE, -5 * HEAD_SCALE, 26 * HEAD_SCALE))
    jaw = G("Jaw", (0, -7, 26), ch=[
        P("Jaw_Bone", (0, -3, 14), (20, 7, 34), "scale_fine", fmat={"bottom": "belly"}),
        P("Jaw_Rear", (0, -4, 0), (22, 8, 10), "scale_fine"),
        P("Tongue", (0, 1, 13), (11, 2, 22), "tongue"),
        *sym(Q("L-Teeth_Low", (8.6, 2.2, 16), teeth_img(52, 8, 9, down=False), q_axis((0, 1, 0), 90)),
             P("L-Fang_Low", (8, 4, 28.5), (3.4, 8, 3.4), "ivory", euler(6, 0, 0)),
             P("L-Jaw_Spike", (10.5, -3, -2), (2, 4, 10), "crest", euler(-30, 20, 0)),
             chin),
        *beard])
    kids.append(jaw)
    S_CTX[0] = 1.0
    return kids


def build_model():
    tail_tip = []
    for k in range(7):
        a = math.radians(-60 + 20 * k)
        tail_tip += strand("Tail_Tuft_%d" % k, (math.sin(a) * 2, math.cos(a) * 2, -10), (math.sin(a) * 0.7, math.cos(a) * 0.5, -1),
                           36 - abs(k - 3) * 4, 5.0, golden=(k % 2 == 1), droop=0.1)
    nxt = None
    for idx in reversed(range(len(BODY))):
        name, L, W, H, sp = BODY[idx]
        ch = segment_parts(name, L, W, H, sp)
        if name in ("Tail6", "Tail7", "Tail8", "Tail9"):
            ch += sym(P("L-%s_Fin" % name, (W / 2 + 2, 0, -L * 0.6), (1, H * 0.8, L * 0.75), "gold", euler(0, -18, -20)))
        if name == "Tail10":
            ch += tail_tip
        if name == "Body6":
            ch += sym(leg(("Thigh", "Calf", "Foot"), ((17, 26, 18), (12, 23, 13), 15),
                          (euler(35, 0, -8), euler(-55, 0, 0), euler(25, 0, 0)), (W / 2 - 1, -H / 2 + 7, -13)))
        if nxt:
            ch.append(nxt)
        pos = (0, ROOT_Y, 0) if idx == 0 else (0, 0, -BODY[idx - 1][1])
        nxt = G(name, pos, ID, ch)
    chest = nxt
    chest["children"] += sym(leg(("Arm", "Forearm", "Hand"), ((15, 26, 16), (11, 22, 12), 14),
                                 (euler(-30, 0, -8), euler(50, 0, 0), euler(-20, 0, 0)), (38 / 2 - 1, -36 / 2 + 7, -12)))
    head = G("Head", (0, 0, NECK[1][1]), ID, build_head())
    neck2 = G("Neck2", (0, 0, NECK[0][1]), ID, segment_parts("Neck2", *NECK[1][1:], forward=True) +
              [G("Mane_Neck2", (0, 0, 0), ch=mane_ring("Mane_N2", (0, 2), 13, 13, 14, 14, 46, 8.0, arc=120)), head])
    neck1 = G("Neck1", (0, 3, -3), ID, segment_parts("Neck1", *NECK[0][1:], forward=True) +
              [G("Mane_Neck1", (0, 0, 0), ch=mane_ring("Mane_N1", (0, 2), 14, 14, 13, 14, 44, 8.0, arc=115) +
                 mane_ring("Mane_N1b", (0, 2), 15, 15, 2, 10, 34, 7.0, arc=90)), neck2])
    chest["children"].append(neck1)
    return [chest]


# =====================================================================================================================
# SYSTEME D'ANIMATION (60 images/s)
#
# Possibilites reelles du moteur, verifiees (2026-11-22) :
#  - un .blockyanim = images-cles par noeud : orientation (delta * liaison), position (delta), shapeStretch, shapeVisible ;
#    interpolation lissee ou lineaire ; aucune cinematique ni physique : tout mouvement « suiveur » doit etre precalcule ;
#  - le moteur choisit seul l'etat de mouvement d'un PNJ volant : FlyIdle / Fly / FlyFast / FlyBackward, HurtFly,
#    DeathFly (chute en boucle) puis DeathFlyImpact (contact du sol), Alerted (gabarit), avec BlendingDuration au changement ;
#  - le code serveur peut jouer N'IMPORTE QUELLE animation du modele dans un emplacement superpose (AnimationUtils.playAnimation,
#    emplacements Movement < Status < Action < Face < Emote) : RyuFlight.java mesure la montee, le pique, le virage, la vitesse
#    et la hauteur du dragon et joue les manoeuvres en Status (virages, montee, pique, tonneau, approche du sol), le
#    clignement en Face ; la morsure reste en Action (interaction d'attaque).
#
# Coeur : « meneur et suiveurs ». Un signal meneur L(t) = (lacet, tangage, roulis) en cap MONDE ; le segment i (0 = tete,
# puis cou, poitrail, torse, bassin, queue) prend base_i + gain_i * L(t - retard_i) : la tete initie, chaque segment suit
# le precedent avec un retard qui s'allonge vers la queue, et l'amplitude grandit vers l'arriere (gain 0,2 a la tete, 1,45
# au bout de la queue). Lacet et tangage dephases de 90 degres = chaque segment decrit une ellipse (queue circulaire, vague
# en diagonale) ; roulis propage = tire-bouchon. Les caps monde sont convertis en rotations locales (local = parent^-1 * monde).
# Convention : tangage + = le nez plonge (pour la queue : l'arriere remonte), lacet + = vers +X (cote gauche du dragon),
# roulis - = le dos penche vers +X.
# =====================================================================================================================
CHAIN = ["Head", "Neck2", "Neck1", "Chest"] + SEGS                       # de la tete au bout de la queue
NC = len(CHAIN)
I_CHEST = 3


def gain_of(i):
    """Amplitude relative du segment i : tete stable, puis croissante vers l'arriere."""
    if i <= I_CHEST:
        return (0.15, 0.4, 0.62, 0.8)[i]
    k = (i - I_CHEST) / (NC - 1 - I_CHEST)
    return 0.85 + 0.45 * k ** 1.3


def delays(tau):
    """Retard cumule du segment i (en fraction de cycle) : chaque pas s'allonge vers la queue."""
    out, acc = [], 0.0
    for i in range(NC):
        out.append(acc)
        acc += tau * (1 + 0.9 * i / NC)
    return out


def side_sign(name):
    return -1 if name.startswith("R-") else 1


def wq(yaw, pitch, roll=0.0):
    return qmul(qmul(q_axis((0, 1, 0), yaw), q_axis((1, 0, 0), pitch)), q_axis((0, 0, 1), roll))


def rot_of(val):
    return val[0]["q"] if isinstance(val[0], dict) else euler(*val[:3])


def pos_of(val):
    if isinstance(val[0], dict):
        return val[1] if len(val) > 1 else None
    return val[3] if len(val) > 3 else None


def apply_chain(p, heads, chest_pos=(0, 0, 0)):
    """heads[i] = (lacet, tangage, roulis) monde de CHAIN[i] -> rotations locales (cou vers l'avant, corps vers l'arriere)."""
    q = [wq(*h) for h in heads]
    p["Chest"] = ({"q": q[I_CHEST]}, tuple(chest_pos))
    for child, parent in (("Neck1", I_CHEST), ("Neck2", 2), ("Head", 1)):
        ci = CHAIN.index(child)
        p[child] = ({"q": qmul(qconj(q[parent]), q[ci])},)
    for i in range(I_CHEST + 1, NC):
        p[CHAIN[i]] = ({"q": qmul(qconj(q[i - 1]), q[i])},)
    return p


def lead(t, cyc, ay, ap, ar, phase=0.0):
    """Signal meneur periodique : cyc cycles par animation ; tangage dephase de 90 degres (ellipse : vague en diagonale),
    roulis de 45 ; harmoniques 2 et 3 pour un mouvement organique (jamais une sinusoide parfaite)."""
    w = 2 * math.pi * (cyc * t + phase)
    return (ay * (math.sin(w) + 0.22 * math.sin(2 * w + 0.7)),
            ap * (math.sin(w + math.pi / 2) + 0.18 * math.sin(3 * w + 1.1)),
            ar * math.sin(w + math.pi / 4))


def legs(p, t, mode, cyc=1, turn=0.0, amp=1.0):
    """Quatre pattes independantes. mode : 'rest' (pendantes), 'fly' (legerement repliees, alternees), 'fast' (plaquees),
    'land' (deployees vers l'avant et le bas, articulations flechies pour l'impact). turn : virage (+ = vers +X)."""
    w = 2 * math.pi * cyc * t
    poses = {
        "rest": {"Arm": -10, "Forearm": -10, "Hand": 25, "Thigh": -20, "Calf": 30, "Foot": -5},
        "fly": {"Arm": 48, "Forearm": 34, "Hand": 40, "Thigh": 40, "Calf": 18, "Foot": 45},
        "fast": {"Arm": 82, "Forearm": 18, "Hand": 60, "Thigh": 70, "Calf": 6, "Foot": 68},
        "land": {"Arm": -48, "Forearm": 28, "Hand": -18, "Thigh": -40, "Calf": 34, "Foot": -22},
    }[mode]
    swing = {"rest": 2, "fly": 7, "fast": 2, "land": 5}[mode] * amp
    hug = {"rest": -10, "fly": -12, "fast": -2, "land": -18}[mode]              # ecart lateral (roulis du haut de patte)
    for s in "LR":
        g = side_sign(s + "-")
        for front, parts in ((True, ("Arm", "Forearm", "Hand")), (False, ("Thigh", "Calf", "Foot"))):
            ph = (0 if front else math.pi) + (0 if s == "L" else math.pi)      # diagonales alternees
            outer = turn * g                                                    # > 0 : patte du cote exterieur au virage ?
            for j, part in enumerate(parts):
                sw = swing * math.sin(w + ph + j * 0.9)
                pitch = poses[part] + sw
                roll = 0.0
                if j == 0:
                    roll = hug * g - 14 * turn                                  # les pattes s'ouvrent vers l'exterieur du virage
                    pitch += 10 * abs(turn) * (1 if outer < 0 else -0.5)
                p["%s-%s" % (s, part)] = (pitch, 0, roll)
    claws(p, t, {"rest": 8, "fly": 18, "fast": 34, "land": -12}[mode], cyc, amp)
    return p


def claws(p, t, curl, cyc=1, amp=1.0):
    """Griffes : chaque doigt (pivot) se recourbe et bouge legerement, decale d'un doigt a l'autre."""
    w = 2 * math.pi * cyc * t
    for s in "LR":
        for foot in ("Hand", "Foot"):
            for k in range(3):
                f = curl + 6 * amp * math.sin(w * 2 + k * 1.3 + (0 if s == "L" else 2))
                p["%s-%s_Toe%d" % (s, foot, k)] = (f * 0.6, (k - 1) * (4 if curl < 0 else 0), 0)
                p["%s-%s_ClawJ%d" % (s, foot, k)] = (f, 0, 0)
    return p


def secondary(p, t, L_of, tau, cyc=1, trail=0.0, amp=1.0):
    """Moustaches, barbillons, criniere, machoire : inertie = ils repondent au meneur avec retard et a contre-sens
    (ils trainent derriere le mouvement de la tete), plus une ondulation propre ; trail les couche vers l'arriere (vitesse)."""
    w = 2 * math.pi * cyc * t
    for s in "LR":
        g = side_sign(s + "-")
        for k in range(1, 7):
            ly, lp, _ = L_of(t - tau * (1.5 + k * 0.6))
            # par articulation : l'effet s'additionne le long des 6 pivots (trop fort = la moustache s'enroule en boucle)
            p["%s-Whisker%d" % (s, k)] = (-0.07 * lp + 4 * amp * math.sin(2 * w - k * 0.8) - 2 * trail,
                                          -0.08 * ly + 4 * amp * g * math.sin(2 * w - k * 0.8 + 1.3) + (12 if k == 1 else 3) * trail * g, 0)
        for k in range(1, 4):
            ly, lp, _ = L_of(t - tau * (1 + k))
            p["%s-Barbel_Cheek%d" % (s, k)] = (-0.12 * lp + 7 * amp * math.sin(2 * w - k) + 4 * trail, -0.1 * ly, 0)
            p["%s-Barbel_Chin%d" % (s, k)] = (-0.12 * lp + 7 * amp * math.sin(2 * w - k + 0.5) + 5 * trail, -0.1 * ly, 0)
    for j, n in enumerate(("Mane_Head", "Mane_Neck2", "Mane_Neck1")):
        ly, lp, lr = L_of(t - tau * (1 + j))
        p[n] = (-0.2 * lp + 3 * amp * math.sin(2 * w - j * 0.7) + 5 * trail, -0.15 * ly, -0.3 * lr)
    return p


# ----- parametres de chaque mode de vol : (cycles par animation, lacet, tangage, roulis, retard par segment, derive x, derive y)
# Retard par segment ~0,06 cycle : environ DEUX vagues completes le long du corps (une seule = « une ligne qui se deplace »).
# MEME retard pour tous les modes (vol rapide : 2 cycles x 0,03 = meme vague) : au changement d'animation, synchronise sur
# la fin de cycle par RyuFlight, la vague est au meme endroit du corps ; seule la forme de la manoeuvre se fond.
# Amplitudes en degres de cap monde ; x gain (0,15 a la tete -> 1,3 au bout de la queue).
MODES = {
    "hover": (1, 42, 36, 10, 0.06, 18, 18),
    "fly": (1, 48, 42, 14, 0.06, 30, 22),
    "fast": (2, 36, 28, 14, 0.03, 16, 10),
    "climb": (1, 42, 32, 12, 0.06, 22, 18),
    "dive": (1, 42, 30, 12, 0.06, 22, 18),
    "turn": (1, 40, 32, 12, 0.06, 22, 18),
    "land": (1, 32, 26, 8, 0.06, 14, 14),
}


def flight_heads(t, mode, turn=0.0):
    """Caps monde de toute la chaine (de la tete au bout de la queue) + derive du poitrail pour un mode de vol."""
    cyc, ay, ap, ar, tau, dx, dy = MODES[mode]
    d = delays(tau)
    L_of = lambda tt: lead(tt, cyc, ay, ap, ar)                             # noqa: E731
    w = 2 * math.pi * cyc * t
    heads = []
    for i in range(NC):
        ly, lp, lr = L_of(t - d[i])
        g = gain_of(i)
        by = bp = br = 0.0
        k = i / (NC - 1)
        if mode == "climb":
            bp = -30 + 22 * k ** 0.8 - (12 if i == 0 else 0)                   # nez vers le ciel, la queue suit en arc
        elif mode == "dive":
            bp = 32 - 24 * k ** 0.8 + (10 if i == 0 else 0)
        elif mode == "land":
            bp = -14 + 30 * math.sin(math.pi * min(1.0, k * 1.6)) * (1 if k < 0.62 else -0.6)   # corps en S sous le poitrail
        elif mode == "fast":
            bp = 6 if i < I_CHEST else 0                                        # tete tendue vers l'avant
        if turn:
            by = turn * (34 - 150 * k)                                          # le corps epouse la courbe du virage
            by += turn * 14 * math.sin(w - 2 * math.pi * d[i] * cyc - 1.2) * k  # inertie : la queue deborde puis rattrape
            br = -turn * (16 + 22 * math.sin(math.pi * min(1.0, k * 1.3)))     # inclinaison dans le virage
        if i == 0:                                                              # petits regards d'observation
            ly += 7 * math.sin(2 * math.pi * t) + 3 * math.sin(6 * math.pi * t + 1)
            lp += 3 * math.sin(4 * math.pi * t + 0.5)
        heads.append([by + g * ly, bp + g * lp, br + g * lr])
    for i in range(1, NC - 1):                                                  # le corps penche dans chacune de ses courbes
        heads[i][2] -= 0.45 * (heads[i + 1][0] - heads[i - 1][0]) * (0.3 if i < I_CHEST else 1.0)
    chest_pos = (dx * math.sin(w), dy * math.sin(2 * w + 0.4), 0)               # derive en huit : haut/bas, gauche/droite
    return [tuple(h) for h in heads], chest_pos, L_of, tau, cyc


def flight(t, mode, turn=0.0):
    """Pose de vol complete pour un mode et un virage (-1..1)."""
    heads, chest_pos, L_of, tau, cyc = flight_heads(t, mode, turn)
    p = apply_chain({}, heads, chest_pos)
    p["Jaw"] = (5 + 3 * math.sin(2 * math.pi * cyc * t), 0, 0)
    legs(p, t, {"hover": "fly", "fly": "fly", "fast": "fast", "climb": "fly", "dive": "fast", "turn": "fly", "land": "land"}[mode],
         cyc, turn, 0.6 if mode == "hover" else 1.0)
    trail = {"hover": 0.2, "fly": 1.0, "fast": 1.6, "climb": 1.0, "dive": 1.8, "turn": 1.0, "land": 0.3}[mode]
    return secondary(p, t, L_of, tau, cyc, trail)


def barrel_roll(t):
    """Tonneau aerien complet (3 s) : un tour de roulis qui part de la tete et descend jusqu'au bout de la queue, par-dessus
    l'ondulation du vol, pendant que le corps decrit une spirale (le poitrail tourne autour de l'axe de vol)."""
    tt = t                                                                      # meme phase que Fly : enchainement sans saut
    p = flight(tt, "fly")
    heads, _, _, _, _ = flight_heads(tt, "fly")
    d = [0.04 + 0.42 * i / (NC - 1) for i in range(NC)]
    heads = [(h[0], h[1], h[2] - 360 * smooth((t - d[i]) / 0.42)) for i, h in enumerate(heads)]
    rc = smooth((t - d[I_CHEST]) / 0.42)
    apply_chain(p, heads, (26 * math.sin(2 * math.pi * rc), 26 * (1 - math.cos(2 * math.pi * rc)), 0))
    return p


def coil_pose(t, breath=1.0):
    """Repos au sol : le corps descend du poitrail et s'enroule en anneau couche sous la tete dressee ; queue en volute."""
    w = 2 * math.pi * t
    heads = [(2 * math.sin(w), -12 + 3 * math.sin(w + 1.2), 0), (0, -50, 0), (0, -38 + 2 * math.sin(w), 0), (0, -6 + 1.5 * math.sin(w), 0)]
    heads += [(y + 2.5 * math.sin(w - k * 0.55) * breath, pt + 2 * math.sin(w - k * 0.55 + 1) * breath, 0) for k, (y, pt) in enumerate(COIL)]
    p = apply_chain({}, heads, (0, 2.5 * math.sin(w) * breath, 0))
    p["Jaw"] = (6 + 3 * math.sin(w), 0, 0)
    legs(p, t, "rest", 1, 0, 0.5)
    return secondary(p, t, lambda tt: lead(tt, 1, 6, 4, 2), 0.04, 1, 0.0, 0.8)


COIL = [(90, -32), (115, -12), (140, 0)] + [(140 + 25 * k, 0) for k in range(1, 13)] + \
       [(140 + 25 * 12 + 32 * k, pt) for k, pt in enumerate((14, 24, 30, 30), 1)]


def curve(t, pts):
    """Interpolation lissee par morceaux : pts = [(t, valeur)]."""
    for (ta, va), (tb, vb) in zip(pts, pts[1:]):
        if t <= tb:
            return va + (vb - va) * smooth((t - ta) / max(1e-6, tb - ta))
    return pts[-1][1]


def front_override(p, chest_pitch, chest_pos, n1, n2, hd, jaw):
    """Avant du dragon pose en cap monde (poitrail, cou, tete) ; elevation + = vers le haut."""
    cq = wq(0, chest_pitch)
    p["Chest"] = ({"q": cq}, chest_pos)
    q1, q2, qh = wq(n1[0], -n1[1]), wq(n2[0], -n2[1]), wq(hd[0], -hd[1], hd[2] if len(hd) > 2 else 0)
    p["Neck1"] = ({"q": qmul(qconj(cq), q1)},)
    p["Neck2"] = ({"q": qmul(qconj(q1), q2)},)
    p["Head"] = ({"q": qmul(qconj(q2), qh)},)
    p["Jaw"] = (jaw, 0, 0)
    return p


def bite_pose(t):
    """Morsure en plongee (1,2 s), emplacement Action PAR-DESSUS le vol : n'anime que le cou, la tete, la machoire, les pattes
    avant et les moustaches (le corps continue d'onduler dessous). Rotations locales qui partent de 0 et y reviennent : aucun
    saut a l'entree ni a la sortie. La tete recule et se dresse, puis l'avant fond vers l'avant et le bas, gueule grande
    ouverte ; la gueule claque a 0,62 s."""
    p = {}
    p["Neck1"] = (curve(t, [(0, 0), (0.42, -26), (0.56, 22), (0.72, 18), (1, 0)]), 0, 0)
    p["Neck2"] = (curve(t, [(0, 0), (0.42, -18), (0.56, 18), (0.72, 14), (1, 0)]), 0, 0)
    p["Head"] = (curve(t, [(0, 0), (0.42, 24), (0.56, -10), (0.72, -6), (1, 0)]), 0, 0)
    p["Jaw"] = (curve(t, [(0, 5), (0.4, 24), (0.53, 52), (0.6, 0), (0.75, 4), (1, 5)]), 0, 0)
    for s_ in "LR":
        p[s_ + "-Arm"] = (curve(t, [(0, 48), (0.45, -30), (0.58, -60), (1, 48)]), 0, -12 * side_sign(s_ + "-"))
        p[s_ + "-Forearm"] = (curve(t, [(0, 34), (0.45, 30), (0.58, -10), (1, 34)]), 0, 0)
        p[s_ + "-Hand"] = (curve(t, [(0, 40), (0.45, -15), (0.58, 35), (1, 40)]), 0, 0)
    claws(p, t, curve(t, [(0, 18), (0.45, -15), (0.58, 40), (1, 18)]))
    for k in range(1, 7):                                                          # moustaches fouettees par le coup de tete
        for s_ in "LR":
            g = side_sign(s_ + "-")
            p["%s-Whisker%d" % (s_, k)] = (curve(t, [(0, 0), (0.45, -6), (0.6, 10), (0.8, -3), (1, 0)]), 3 * g * math.sin(6 * t - k), 0)
    return p


def flinch_pose(t):
    """Sursaut quand le dragon est touche (0,45 s, emplacement Face, joue par RyuFlight quand ses PV baissent) : la tete
    recule et se detourne, la gueule s'ouvre, la criniere se herisse ; le corps n'est pas touche (il continue d'onduler)."""
    k = curve(t, [(0, 0), (0.25, 1), (1, 0)])
    return {"Neck2": (-10 * k, 6 * k, 0), "Head": (-14 * k, -10 * k, 8 * k), "Jaw": (5 + 28 * k, 0, 0),
            "Mane_Head": (-10 * k, 0, 0), "Mane_Neck2": (-6 * k, 0, 0)}


def roar_pose(t):
    """Rugissement (2 s) : le dragon se cabre en vol stationnaire, tete levee, gueule grande ouverte, criniere herissee."""
    p = flight(t, "hover")
    e = curve(t, [(0, 0), (0.22, 1), (0.82, 1), (1, 0)])
    shake = 3 * math.sin(t * 60) * e
    front_override(p, 4 - 16 * e, (0, 8 * e, -6 * e), (0, 32 + 30 * e), (0, 18 + 24 * e), (shake, -6 + 30 * e), 6 + 48 * e)
    p["Mane_Head"] = (-14 * e + 2 * math.sin(t * 50), 0, 0)
    p["Mane_Neck2"] = (-10 * e, 0, 0)
    for s in "LR":
        p[s + "-Arm"] = (20 - 50 * e, 0, -22 * side_sign(s + "-") * e)
    claws(p, t, -10 * e + 15 * (1 - e))
    return p


def death_fly(t):
    """Chute (boucle, tant que le dragon tombe) : corps inerte qui tournoie lentement, pattes ecartees, gueule ouverte."""
    w = 2 * math.pi * t
    heads = []
    for i in range(NC):
        heads.append((25 * math.sin(w - i * 0.35) * (0.4 + i / NC), -20 * math.cos(w - i * 0.3) * (0.3 + i / NC) - (25 if i < I_CHEST else 0),
                      40 * math.sin(w - i * 0.2) + 20))
    p = apply_chain({}, heads, (0, 0, 0))
    p["Jaw"] = (30 + 6 * math.sin(3 * w), 0, 0)
    for s in "LR":
        g = side_sign(s + "-")
        for part, base in (("Arm", -30), ("Forearm", -10), ("Hand", -20), ("Thigh", -30), ("Calf", 10), ("Foot", -20)):
            p["%s-%s" % (s, part)] = (base + 10 * math.sin(w * 2 + (part in ("Arm", "Thigh")) * 1.5), 0, (-40 * g if part in ("Arm", "Thigh") else 0))
    claws(p, t, -15, 2)
    return secondary(p, t, lambda tt: lead(tt, 1, 20, -30, 10), 0.03, 1, -1.5, 1.4)


def death_impact(t):
    """Impact au sol (non bouclee, derniere image tenue) : le corps s'ecrase et rebondit, puis s'affale enroule, tete au sol."""
    k = curve(t, [(0, 0), (0.25, 1), (1, 1)])
    bounce = curve(t, [(0, 0), (0.12, -1), (0.24, 0.35), (0.36, 0), (1, 0)])
    heads = [(30 * k, 20 * k, 30 * k), (24 * k, 12 * k, 14 * k), (10 * k, 8 * k, 4 * k), (0, 0, 14 * k)]
    heads += [(y * k * 0.85 + 6 * (1 - k) * math.sin(i), 0, 0) for i, (y, _) in enumerate(COIL)]
    p = apply_chain({}, heads, (0, 14 * bounce - 30 * k, 0))
    p["Jaw"] = (6 + 26 * curve(t, [(0, 0), (0.2, 1), (0.6, 0.6), (1, 0.6)]), 0, 0)
    for s in "LR":
        g = side_sign(s + "-")
        for part in ("Arm", "Thigh"):
            p["%s-%s" % (s, part)] = (-40 * k, 0, -50 * k * g)
        for j in range(1, 7):
            p["%s-Whisker%d" % (s, j)] = ((25 if j == 1 else 5) * k, 0, 0)
    claws(p, t, -10 * k, 1, 1 - k)
    p["Mane_Head"] = p["Mane_Neck1"] = p["Mane_Neck2"] = (8 * k, 0, 0)
    return p


# nom : (pose(t), images, images-cles, bouclee, derniere image tenue)
# Toutes les animations de vol du chef d'orchestre (RyuFlight, emplacement Status) durent EXACTEMENT 150 images (2,5 s) et
# partent de la meme phase de vague : RyuFlight ne change d'animation qu'en fin de cycle, la vague continue sans saut.
CYCLE = 150
ANIMS = {
    "Idle": (coil_pose, 240, 16, True, False),
    "Hover": (lambda t: flight(t, "hover"), CYCLE, 50, True, False),
    "Fly": (lambda t: flight(t, "fly"), CYCLE, 50, True, False),
    "FlyFast": (lambda t: flight(t, "fast"), CYCLE, 72, True, False),
    "FlyClimb": (lambda t: flight(t, "climb"), CYCLE, 50, True, False),
    "FlyDive": (lambda t: flight(t, "dive"), CYCLE, 50, True, False),
    "FlyTurnLeft": (lambda t: flight(t, "turn", 1.0), CYCLE, 50, True, False),
    "FlyTurnRight": (lambda t: flight(t, "turn", -1.0), CYCLE, 50, True, False),
    "FlyLand": (lambda t: flight(t, "land"), CYCLE, 50, True, False),
    "FlyRoll": (barrel_roll, CYCLE, 60, False, False),
    "Alerted": (roar_pose, 120, 24, False, False),
    "Flinch": (flinch_pose, 27, 9, False, False),
    "DeathFly": (death_fly, 120, 16, True, False),
    "DeathFlyImpact": (death_impact, 90, 18, False, True),
    "Bite": (bite_pose, 72, 24, False, False),
}


def blink_anim():
    """Clignement (emplacement Face, joue par RyuFlight toutes les 3 a 7 s) : les paupieres cachees apparaissent et
    descendent sur l'oeil (shapeVisible + shapeStretch, comme le clignement des PNJ du jeu), la paupiere haute s'abaisse."""
    tracks = {}
    for s in "LR":
        tracks["%s-Blink_Lid" % s] = {
            "position": [{"time": tt, "delta": vec((0, dy, 0)), "interpolationType": "linear"} for tt, dy in ((0, 3), (4, 0.5), (6, 0), (12, 0), (16, 3))],
            "orientation": [], "shapeUvOffset": [], "shapeStretch": [],
            "shapeVisible": [{"time": 0, "delta": False, "interpolationType": "linear"}, {"time": 2, "delta": True, "interpolationType": "linear"},
                             {"time": 15, "delta": True, "interpolationType": "linear"}, {"time": 17, "delta": False, "interpolationType": "linear"}]}
        tracks["%s-Lid_Up" % s] = {
            "position": [{"time": tt, "delta": vec((0, dy, 0)), "interpolationType": "smooth"} for tt, dy in ((0, 0), (5, -1.2), (12, -1.2), (18, 0))],
            "orientation": [], "shapeStretch": [], "shapeVisible": [], "shapeUvOffset": []}
    return {"duration": 18, "holdLastKeyframe": False, "nodeAnimations": tracks}


def anim_from(fn, frames, keys, hold=False):
    tracks, prev = {}, {}
    for i in range(keys + 1):
        t = i / keys
        time = round(frames * t)
        for n, val in fn(t).items():
            tr = tracks.setdefault(n, {"position": [], "orientation": [], "shapeStretch": [], "shapeVisible": [], "shapeUvOffset": []})
            q = rot_of(val)
            if n in prev and sum(prev[n][k] * q[k] for k in "xyzw") < 0:        # meme hemisphere : pas de tour par le long chemin
                q = {k: -q[k] for k in "xyzw"}
            prev[n] = q
            tr["orientation"].append({"time": time, "delta": {k: round(q[k], 5) for k in "xyzw"}, "interpolationType": "smooth"})
            if pos_of(val) is not None:
                tr["position"].append({"time": time, "delta": vec(pos_of(val)), "interpolationType": "smooth"})
    return {"duration": frames, "holdLastKeyframe": hold, "nodeAnimations": tracks}


# =====================================================================================================================
# Apercus (rendu logiciel de la pose)
# =====================================================================================================================
def posed(nodes, pose):
    out = copy.deepcopy(nodes)

    def walk(n):
        if n["name"] in pose:
            val = pose[n["name"]]
            n["orientation"] = qmul(n["orientation"], rot_of(val))
            if pos_of(val) is not None:
                for i, k in enumerate("xyz"):
                    n["position"][k] += pos_of(val)[i]
        for c in n["children"]:
            walk(c)
    for n in out:
        walk(n)
    return out


def render(nodes, tex, pose, yaw, pitch, size=560, ground=0.0):
    quads = []
    for n in posed(nodes, pose):
        quads += K.child_quads(n, (0, ground, 0), ID, tex, ("L-Blink_Lid", "R-Blink_Lid"))
    g = 330
    solid = [([(-g, -0.5, -g), (g, -0.5, -g), (g, -0.5, g), (-g, -0.5, g)], [(0, 0)] * 4, (70, 88, 70)),
             ([(-90, 0, -90), (90, 0, -90), (90, 0, 90), (-90, 0, 90)], [(0, 0)] * 4, (98, 120, 98))]
    return K.downscale(K.draw(quads, size * 2, yaw, pitch, solid=solid, margin=0.04), 2)


def sheet(images, cols, bg=(150, 170, 190)):
    h, w = len(images[0]), len(images[0][0])
    rows = (len(images) + cols - 1) // cols
    out = [[list(bg) + [255] for _ in range(w * cols)] for _ in range(h * rows)]
    for i, img in enumerate(images):
        ox, oy = (i % cols) * w, (i // cols) * h
        for y in range(h):
            for x in range(w):
                p = img[y][x]
                if p[3] > 0:
                    a = p[3] / 255
                    out[oy + y][ox + x] = [p[k] * a + bg[k] * (1 - a) for k in range(3)] + [255]
    return w * cols, h * rows, out


# =====================================================================================================================
# Sortie
# =====================================================================================================================
def write_json(rel, data):
    path = os.path.join(RES, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


def model_asset():
    a = "%s/Animations/" % REL

    def one(name, blend=0.4, speed=None, loop=None):
        e = {"Animation": a + name + ".blockyanim", "BlendingDuration": blend}
        if speed:
            e["Speed"] = speed
        if loop is not None:
            e["Looping"] = loop
        return [e]

    sets = {
        # etats choisis par le moteur (PNJ volant)
        "FlyIdle": one("Hover", 0.6), "Fly": one("Fly", 0.6), "FlyFast": one("FlyFast", 0.6), "FlyBackward": one("Hover", 0.8, 0.8),
        "DeathFly": one("DeathFly", 0.6),
        "DeathFlyImpact": one("DeathFlyImpact", 0.1, None, False), "Alerted": one("Alerted", 0.4, None, False),
        # au sol (s'il s'y pose) et repli des autres etats
        "Idle": one("Idle", 0.8), "Sleep": one("Idle", 0.8, 0.5), "Laydown": one("Idle", 0.8, 0.5), "Wake": one("Alerted", 0.4, None, False),
        "Walk": one("Fly", 0.8), "WalkBackward": one("Hover", 0.8), "Run": one("FlyFast", 0.7),
        "Jump": one("FlyClimb", 0.6), "JumpWalk": one("FlyClimb", 0.6), "JumpRun": one("FlyClimb", 0.6), "Fall": one("FlyDive", 0.6),
        "SwimIdle": one("Hover", 0.8), "Swim": one("Fly", 0.8), "SwimFast": one("FlyFast", 0.7),
        "Death": one("DeathFlyImpact", 0.3, None, False),
        # manoeuvres jouees par RyuFlight.java (emplacement Status) et clignement (Face)
        # PAS de Hurt / HurtFly : le moteur (DamageSystems.HitAnimation) ne joue la blessure, dans l'emplacement Status, que si
        # le modele en a une ; sans elle, les coups n'interrompent plus le vol ; le sursaut (Flinch) passe par Face.
        "Hover": one("Hover", 0.6), "FlyClimb": one("FlyClimb", 0.8), "FlyDive": one("FlyDive", 0.8),
        "FlyTurnLeft": one("FlyTurnLeft", 0.8), "FlyTurnRight": one("FlyTurnRight", 0.8),
        "FlyLand": one("FlyLand", 1.0), "FlyRoll": one("FlyRoll", 0.4, None, False),
        "Roar": one("Alerted", 0.4, None, False), "Blink": one("Blink", 0.05, None, False),
        "Flinch": one("Flinch", 0.08, None, False),
    }
    for k in ("Alerted", "Roar", "Wake"):
        sets[k][0]["SoundEventId"] = "SFX_Eldanior_Ryu_Roar"
    return {
        "$Comment": "Boss Mondial « Kokuō-Ryū, le Dragon Céleste Impérial » (scripts/monstres/gen_ryu_celeste.py) : Ryū noir et or "
                    "construit de zéro, volant ; taille x6, couronnes, niveau et nom posés par WorldBosses.",
        "Model": REL + "/Model.blockymodel",
        "Texture": REL + "/Texture.png",
        "EyeHeight": 1.2,
        "HitBox": {"Max": {"X": 1.5, "Y": 1.7, "Z": 1.5}, "Min": {"X": -1.5, "Y": 0, "Z": -1.5}},     # x6 : 18 x 10 blocs
        "Camera": {"Pitch": {"AngleRange": {"Max": 45, "Min": -45}, "TargetNodes": ["Head"]}, "Yaw": {"TargetNodes": ["Head"]}},
        "AnimationSets": {k: {"Animations": v} for k, v in sets.items()},
        # trainees dorees : bout de la queue et des moustaches, elles dessinent les ondulations dans l'air
        "Trails": [{"TrailId": "Ryu_Celeste_Trail", "TargetNodeName": "Tail_Tuft_3_c", "PositionOffset": {"X": 0, "Y": 0, "Z": 0},
                    "RotationOffset": {"Yaw": 0, "Pitch": 0, "Roll": 0}}] +
                  [{"TrailId": "Ryu_Celeste_Trail_Fin", "TargetNodeName": "%s-Whisker6_Rod" % s_, "PositionOffset": {"X": 0, "Y": 0, "Z": 0},
                    "RotationOffset": {"Yaw": 0, "Pitch": 0, "Roll": 0}} for s_ in "LR"],
    }


def fx_files():
    """Trainees dorees (bout de queue, moustaches) et rugissement (rugissement de l'ours du Vide du jeu, plus grave, plus loin)."""
    for name, width, life, alpha in (("Ryu_Celeste_Trail", 0.55, 14, 0.55), ("Ryu_Celeste_Trail_Fin", 0.16, 9, 0.6)):
        write_json("Server/Entity/Trails/%s.json" % name, {
            "TexturePath": "Trails/White.png", "LifeSpan": life, "Roll": 0,
            "Start": {"Color": "rgba(#f5d879, %s)" % alpha, "Width": width},
            "End": {"Color": "rgba(#a77b20, 0)", "Width": 0},
            "LightInfluence": 0, "Smooth": True, "RenderMode": "BlendLinear"})
    write_json("Server/Audio/SoundEvents/SFX/Eldanior/SFX_Eldanior_Ryu_Roar.json", {
        "Layers": [{"Files": ["Sounds/NPC/Beasts/Bear_Voidtaken/Bear_Voidtaken_Roar_0%d.ogg" % k for k in range(1, 5)],
                    "RandomSettings": {"MinPitch": -1, "MaxPitch": 1, "MinVolume": -1}, "Volume": 0}],
        "Volume": 2, "Pitch": -9, "PreventSoundInterruption": True, "AudioCategory": "AudioCat_NPC_Bear_Grizzly",
        "Parent": "SFX_Attn_Moderate", "SpatialBlend": 0.5, "MaxDistance": 110})


def attack_files():
    write_json("Server/Item/Animations/Eldanior_Ryu_Celeste.json", {
        "Animations": {"Bite": {"Speed": 1, "Looping": False, "ThirdPerson": REL + "/Animations/Bite.blockyanim"}},
        "Camera": {"Pitch": {"AngleRange": {"Max": 45, "Min": -45}, "TargetNodes": ["Head"]},
                   "Yaw": {"AngleRange": {"Max": 45, "Min": -45}, "TargetNodes": ["Head"]}},
        "WiggleWeights": {"Pitch": 2, "PitchDeceleration": 0.1, "Roll": 0.1, "RollDeceleration": 0.1, "X": 3, "XDeceleration": 0.1,
                          "Y": 0.1, "YDeceleration": 0.1, "Z": 0.1, "ZDeceleration": 0.1}})
    bite = {
        "$Comment": "Morsure en plongée de Kokuō-Ryū (gen_ryu_celeste.py) : animation Bite du modèle (la gueule claque à 0,62 s), "
                    "portée à la taille x6, atteint vers le bas (il attaque en vol). Dégâts posés ici (rôle volant sans _InteractionVars).",
        "Type": "Simple", "RunTime": 0.68,
        "Effects": {"ItemPlayerAnimationsId": "Eldanior_Ryu_Celeste", "ItemAnimationId": "Bite"},
        "Next": {"Type": "Parallel", "Interactions": [
            {"Interactions": [{
                "Type": "Selector", "RunTime": 0.2,
                "Selector": {"Id": "Horizontal", "Direction": "ToLeft", "TestLineOfSight": True, "ExtendTop": 8.0, "ExtendBottom": 14.0,
                             "StartDistance": 1.0, "EndDistance": 24.0, "Length": 120, "RollOffset": 0, "YawStartOffset": -60,
                             "PitchOffset": -20},
                "HitEntity": {"Interactions": [{
                    "Parent": "NPC_Attack_Melee_Damage",
                    "DamageCalculator": {"Type": "Absolute", "BaseDamage": {"Physical": 650}, "RandomPercentageModifier": 0.1},
                    "Effects": {"WorldSoundEventId": "SFX_Bone_Hit"},
                    "DamageEffects": {"Knockback": {"Force": 9}}}]},
                "Next": {"Type": "Simple", "RunTime": 0.6}}]},
            {"Interactions": [{"Type": "Simple", "RunTime": 0.25, "Effects": {"WorldSoundEventId": "SFX_Rex_Bite"}}]},
        ]},
    }
    write_json("Server/Item/Interactions/NPCs/Eldanior/Ryu_Celeste_Morsure.json", bite)
    write_json("Server/Item/RootInteractions/NPCs/Eldanior/Root_NPC_Eldanior_Ryu_Celeste_Attack.json",
               {"Interactions": ["Ryu_Celeste_Morsure"], "Tags": {"Attack": ["Melee"]}})


def role_file():
    write_json("Server/NPC/Roles/Eldanior/Monstres/%s.json" % ROLE, {
        "$Comment": "Boss Mondial « Kokuō-Ryū, le Dragon Céleste Impérial » (gen_ryu_celeste.py) : Ryū volant (gabarit volant du jeu : "
                    "monte, descend, tourne et plonge sur sa cible pour mordre) ; manoeuvres animees par RyuFlight.java ; taille x6, couronnes, niveau et nom posés par WorldBosses.",
        "Type": "Variant",
        "Reference": "Template_Flying_Aggressive",
        "Modify": {
            "Appearance": ROLE,
            "DropList": "Empty",
            "MaxHealth": 9000,
            "DisableDamageGroups": ["Self"],
            "MotionFlyMaxHorizontalSpeed": 16,
            "MotionFlyMinHeightAboveGround": 3,
            "MotionFlyMaxHeightAboveGround": 45,
            "MotionDesiredAltitudeCombat": [3, 11],
            "MotionDesiredAltitudeSearch": [16, 32],
            "ViewRange": 72,
            "ViewSector": 240,
            "HearingRange": 48,
            "AbsoluteDetectionRange": 18,
            "AlertedRange": 80,
            "IdleWanderInCircleRadius": 50,
            "CombatBehaviorDistance": 70,
            "CombatRelativeSpeedFlyMotion": 0.6,
            "CombatRelativeSpeedChase": 1,
            "RangedAttack": "Root_NPC_Eldanior_Ryu_Celeste_Attack",
            "RangedAttackDistance": 24,
            "RangedAttackAimingRange": [0.3, 0.6],
            "RangedAttackPauseRange": [1.4, 2.4],
            "CombatSeekSlowdownDistance": 24,
            "CombatSeekStopDistance": 13,
            "TargetRange": 40,
            "DeathAnimationTime": 3,
            "KnockbackScale": 0,
            "IsMemory": False,
            "NameTranslationKey": "server.npcRoles.Dragon_Frost.name",
        },
    })


def count_nodes(nodes):
    return sum(1 + count_nodes(n["children"]) for n in nodes)


def main():
    nodes = build_model()
    names = set()

    def walk(n):
        if n["name"] in names and n["shape"]["type"] == "none":
            raise SystemExit("pivot en double : " + n["name"])
        names.add(n["name"])
        for c in n["children"]:
            walk(c)
    for n in nodes:
        walk(n)
    for name, (fn, frames, keys, loop, hold) in ANIMS.items():
        for node in fn(0.3):
            if node not in names:
                raise SystemExit("noeud anime inconnu : %s (%s)" % (node, name))
    atlas.pack()
    w, h, tex = atlas.texture()
    out = os.path.join(RES, "Common", REL)
    os.makedirs(os.path.join(out, "Animations"), exist_ok=True)
    for old in os.listdir(os.path.join(out, "Animations")):
        if old.endswith(".blockyanim") and old[:-len(".blockyanim")] not in ANIMS and old != "Blink.blockyanim":
            os.remove(os.path.join(out, "Animations", old))
    png.write(os.path.join(out, "Texture.png"), w, h, tex)
    with open(os.path.join(out, "Model.blockymodel"), "w", encoding="utf-8") as f:
        json.dump({"nodes": nodes, "lod": "auto"}, f, indent=1)
        f.write("\n")
    for name, (fn, frames, keys, loop, hold) in ANIMS.items():
        with open(os.path.join(out, "Animations", name + ".blockyanim"), "w", encoding="utf-8") as f:
            json.dump(anim_from(fn, frames, keys, hold), f, separators=(",", ":"))      # compact : ~4x plus leger a charger
            f.write("\n")
    with open(os.path.join(out, "Animations", "Blink.blockyanim"), "w", encoding="utf-8") as f:
        json.dump(blink_anim(), f, indent=1)
        f.write("\n")
    write_json("Server/Models/Eldanior/%s.json" % ROLE, model_asset())
    attack_files()
    fx_files()
    role_file()
    print("Kokuo-Ryu : %d noeuds, %d formes, texture %dx%d -> %s" % (count_nodes(nodes), SHAPES[0], w, h, out))
    if PREVIEW:
        os.makedirs(PREVIEW, exist_ok=True)
        alt = 110                                                   # apercus du vol : 1,7 bloc au-dessus du sol (avant x6)
        shots = [
            ("vol_a", render(nodes, tex, flight(0.0, "fly"), 90, 4, ground=alt)),
            ("vol_b", render(nodes, tex, flight(0.33, "fly"), 90, 4, ground=alt)),
            ("vol_dessus_a", render(nodes, tex, flight(0.0, "fly"), 0, 85, ground=alt)),
            ("vol_dessus_b", render(nodes, tex, flight(0.5, "fly"), 0, 85, ground=alt)),
            ("vol_34", render(nodes, tex, flight(0.15, "fly"), 55, 20, ground=alt)),
            ("stationnaire", render(nodes, tex, flight(0.0, "hover"), 40, 14, ground=alt)),
            ("vol_rapide", render(nodes, tex, flight(0.3, "fast"), 60, 22, ground=alt)),
            ("montee", render(nodes, tex, flight(0.2, "climb"), 90, 0, ground=alt)),
            ("pique", render(nodes, tex, flight(0.2, "dive"), 90, 0, ground=alt)),
            ("virage_gauche_dessus", render(nodes, tex, flight(0.2, "turn", 1.0), 0, 80, ground=alt)),
            ("tonneau", render(nodes, tex, barrel_roll(0.42), 60, 15, ground=alt)),
            ("atterrissage", render(nodes, tex, flight(0.0, "land"), 75, 8, ground=alt)),
            ("rugissement", render(nodes, tex, roar_pose(0.5), 40, 10, ground=alt)),
            ("morsure", render(nodes, tex, {**flight(0.0, "fly"), **bite_pose(0.56)}, 75, 10, ground=alt)),
            ("sursaut", render(nodes, tex, {**flight(0.0, "fly"), **flinch_pose(0.25)}, 75, 10, ground=alt)),
            ("chute", render(nodes, tex, death_fly(0.3), 40, 20, ground=alt)),
            ("impact", render(nodes, tex, death_impact(1.0), 40, 30)),
        ]
        for name, img in shots:
            png.write(os.path.join(PREVIEW, "ryu_%s.png" % name), len(img[0]), len(img), img)
        W, H, s = sheet([img for _, img in shots], 5)
        png.write(os.path.join(PREVIEW, "ryu_planche.png"), W, H, s)
        head = nodes[0]["children"][-1]["children"][-1]["children"][-1]
        assert head["name"] == "Head", head["name"]
        close = []
        for yaw, pitch in ((35, 12), (150, 10), (90, 0), (0, 0)):
            quads = []
            for c in head["children"]:
                quads += K.child_quads(c, (0, 0, 0), ID, tex, ("L-Blink_Lid", "R-Blink_Lid"))
            close.append(K.downscale(K.draw(quads, 1040, yaw, pitch, margin=0.04), 2))
        W, H, s = sheet(close, 2)
        png.write(os.path.join(PREVIEW, "ryu_tete.png"), W, H, s)
        # oeil en gros plan
        eye_n = [c for c in head["children"] if c["name"] == "L-Eye"][0]
        quads = [q for c in head["children"] if c["name"] in ("L-Eye", "L-Lid_Up", "L-Lid_Low", "L-Eye_Socket", "L-Brow", "L-Cheek")
                 for q in K.child_quads(c, (0, 0, 0), ID, tex, ("\0",))]
        img = K.downscale(K.draw(quads, 800, -70, 5, margin=0.1), 2)
        png.write(os.path.join(PREVIEW, "ryu_oeil.png"), len(img[0]), len(img), img)
        print("apercus ->", PREVIEW, eye_n["name"])


if __name__ == "__main__":
    main()
