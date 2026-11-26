"""Borne de ville (Eldanior_Borne_Ville, guards/CityBornes) : stele a blason construite piece par piece.

Demande du 2026-11-19 : « je veux un bloc personnalise » (avant : modele du Bench_Abilities du jeu). Choix de l'utilisateur :
stele a blason. Socle de basalte a deux gradins, fut de calcite lisse borde de pilastres d'or, banniere bleue de la ville
qui tombe du chapiteau, blason (tour d'or crenelee sur champ de gueules) en facade, plaque gravee du registre sous le blason,
chapiteau orne, couronne d'or et cristal lumineux au sommet. Hauteur ~2 blocs, emprise < 1 bloc.

Echelle des blocs : 32 unites = 1 bloc, 1 px = 1 unite. Origine = centre bas du bloc pose ; +Z = face vers le joueur
(VariantRotation NESW tourne la stele vers celui qui la pose). Hitbox en blocs depuis le coin : (x + 16) / 32.

Usage : python3 scripts/territoire/gen_borne_ville.py [src/main/resources] [dossier_apercu]
"""
import json, math, os, random, sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "rune_aura_icons"))
sys.path.insert(0, os.path.join(HERE, "..", "minerai"))
import png  # noqa: E402
from vanilla_assets import vanilla  # noqa: E402

RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "..", "..", "src", "main", "resources")
PREVIEW = sys.argv[2] if len(sys.argv) > 2 else None
COMMON = os.path.join(RES, "Common")
SERVER = os.path.join(RES, "Server")
ITEM = "Eldanior_Borne_Ville"
MODEL = "Blocks/Eldanior/Territoire/Borne_Ville"
ATLAS_W = 256

FACE_LIGHT = {"front": 1.0, "back": 0.8, "left": 0.88, "right": 0.88, "top": 1.1, "bottom": 0.55}
GOLD, GOLD_DARK, GOLD_LIGHT = (232, 184, 84), (150, 102, 40), (255, 228, 150)
AZURE, AZURE_DARK = (44, 78, 156), (24, 42, 96)
GULES, GULES_DARK = (176, 40, 40), (110, 22, 26)
CYAN, WHITE = (130, 220, 255), (250, 252, 255)
INK = (34, 30, 36)


def clamp(v):
    return max(0, min(255, int(round(v))))


def mix(a, b, t):
    t = max(0.0, min(1.0, t))
    return tuple(a[i] + (b[i] - a[i]) * t for i in range(3))


_rng = random.Random(11)
_grid = [[_rng.random() for _ in range(64)] for _ in range(64)]


def noise(x, y):
    ix, iy = math.floor(x), math.floor(y)
    fx, fy = x - ix, y - iy
    fx, fy = fx * fx * (3 - 2 * fx), fy * fy * (3 - 2 * fy)
    a, b = _grid[iy & 63][ix & 63], _grid[iy & 63][(ix + 1) & 63]
    c, d = _grid[(iy + 1) & 63][ix & 63], _grid[(iy + 1) & 63][(ix + 1) & 63]
    return (a + (b - a) * fx) * (1 - fy) + (c + (d - c) * fx) * fy


_tiles = {}


def tile(name):
    if name not in _tiles:
        _tiles[name] = vanilla("Common/BlockTextures/%s.png" % name)[2]
    return _tiles[name]


# ---------------------------------------------------------------------------------------------------------------------
# Matieres : paint(face, w, h, (x, y, z) coin bas de la boite) -> image
# ---------------------------------------------------------------------------------------------------------------------
def world_uv(face, i, j, w, h, o):
    x, y, z = o
    if face == "front":
        return x + i, -(y + h) + j
    if face == "back":
        return -(x + w) + i, -(y + h) + j
    if face in ("left", "right"):
        return z + i, -(y + h) + j
    return x + i, z + j


def material(name, top=None, light=1.0, edge=0.82):
    def paint(face, w, h, o):
        tex = tile(top if top and face in ("top", "bottom") else name)
        th, tw = len(tex), len(tex[0])
        img = []
        for j in range(h):
            row = []
            for i in range(w):
                u, v = world_uv(face, i, j, w, h, o)
                p = tex[int(v) % th][int(u) % tw]
                k = FACE_LIGHT[face] * light
                if face not in ("top", "bottom"):
                    k *= 1.06 - 0.18 * (j + 0.5) / h
                if min(i, j, w - 1 - i, h - 1 - j) == 0 and min(w, h) > 3:
                    k *= edge
                row.append([clamp(p[0] * k), clamp(p[1] * k), clamp(p[2] * k), 255])
            img.append(row)
        return img
    return paint


def gold(light=1.0):
    def paint(face, w, h, o):
        img = []
        for j in range(h):
            row = []
            for i in range(w):
                u, v = world_uv(face, i, j, w, h, o)
                c = mix(GOLD_DARK, GOLD, 0.3 + 0.7 * noise(u * 0.3, v * 0.3))
                if min(i, j, w - 1 - i, h - 1 - j) == 0 and min(w, h) > 2:
                    c = mix(c, GOLD_LIGHT, 0.35)
                k = FACE_LIGHT[face] * light * (1.05 - 0.14 * (j + 0.5) / h)
                row.append([clamp(c[0] * k), clamp(c[1] * k), clamp(c[2] * k), 255])
            img.append(row)
        return img
    return paint


def crystal(face, w, h, o):
    return [[[clamp(mix(AZURE, CYAN, 0.3 + noise(i * 0.8, j * 0.8))[k]) for k in range(3)] + [255] for i in range(w)] for j in range(h)]


def engraved(face, w, h, o):
    """Plaque de basalte gravee de lignes d'or (le registre de la ville)."""
    img = material("Rock_Basalt_Brick_Smooth", light=0.7, edge=1.0)(face, w, h, o)
    if face != "front":
        return img
    for j in range(h):
        for i in range(w):
            if min(i, j, w - 1 - i, h - 1 - j) == 0:
                img[j][i][:3] = list(map(clamp, GOLD_DARK))
            elif j in (2, 4) and 2 <= i <= w - 3 and (i * 7 + j) % 5 != 0:
                img[j][i][:3] = list(map(clamp, GOLD))
    return img


STONE_DARK = material("Rock_Basalt_Brick", light=0.95)
STONE_DARK_SMOOTH = material("Rock_Basalt_Brick_Smooth", light=0.9)
SHAFT = material("Calcite_Brick_Smooth", light=1.0)
CAP = material("Calcite_Brick_Ornate", top="Calcite_Brick_Top", light=1.0)
GOLD_M = gold()


# ---------------------------------------------------------------------------------------------------------------------
# Images peintes : banniere, blason
# ---------------------------------------------------------------------------------------------------------------------
def banner(w, h):
    """Etoffe d'azur bordee d'or, fendue en queue d'aronde en bas."""
    img = []
    for j in range(h):
        row = []
        for i in range(w):
            tail = h - 6
            if j >= tail and abs(i - (w - 1) / 2.0) < (j - tail) * (w / 12.0):
                row.append([0, 0, 0, 0])
                continue
            fold = 0.88 + 0.12 * math.sin(i / w * math.pi * 3)               # plis verticaux
            c = mix(AZURE_DARK, AZURE, 0.55 + 0.45 * noise(i * 0.5, j * 0.15))
            c = tuple(x * fold for x in c)
            edge = i in (0, w - 1) or j in (0, 1)
            if edge or (j >= tail and abs(i - (w - 1) / 2.0) < (j - tail) * (w / 12.0) + 1.2):
                c = GOLD
            row.append([clamp(c[0]), clamp(c[1]), clamp(c[2]), 255])
        img.append(row)
    return img


def shield(w, h):
    """Ecu (heater) : champ de gueules (rouge, pour ressortir sur la banniere d'azur), bordure d'or, tour d'or crenelee a porte noire, etoile au chef."""
    img = []
    cx = (w - 1) / 2.0
    for j in range(h):
        row = []
        for i in range(w):
            u = abs(i - cx)
            # contour : bord droit sur 55 %, puis pointe arrondie
            t = j / (h - 1.0)
            half = w / 2.0 if t < 0.55 else w / 2.0 * math.sqrt(max(0.0, 1 - ((t - 0.55) / 0.45) ** 2))
            if u > half:
                row.append([0, 0, 0, 0])
                continue
            c = mix(GULES_DARK, GULES, 0.6 + 0.4 * noise(i * 0.6, j * 0.6))
            if u > half - 1.3 or j == 0:
                c = GOLD_DARK if u > half - 0.6 else GOLD
            row.append([clamp(c[0]), clamp(c[1]), clamp(c[2]), 255])
        img.append(row)
    # tour : corps 6 x 7, creneaux, porte
    tx0, tx1, ty0, ty1 = int(cx - 3), int(cx + 3), 5, 11
    for j in range(ty0, ty1 + 1):
        for i in range(tx0, tx1 + 1):
            img[j][i][:3] = list(map(clamp, GOLD if i not in (tx0, tx1) else GOLD_DARK))
    for i in range(tx0, tx1 + 1, 2):
        img[ty0 - 1][i][:3] = list(map(clamp, GOLD))
    for j in range(ty1 - 2, ty1 + 1):
        for i in (int(cx), int(cx) + (1 if w % 2 == 0 else 0)):
            img[j][i][:3] = list(INK)
    img[ty0 + 2][int(cx)][:3] = list(INK)                                     # fenetre
    # etoile d'or au chef
    for (dx, dy) in ((0, 0), (1, 0), (-1, 0), (0, 1), (0, -1)):
        img[2 + dy][int(cx) + dx][:3] = list(map(clamp, GOLD_LIGHT))
    return img


# ---------------------------------------------------------------------------------------------------------------------
# Modele
# ---------------------------------------------------------------------------------------------------------------------
ID_Q = {"x": 0, "y": 0, "z": 0, "w": 1}


def q_axis(axis, deg):
    s, c = math.sin(math.radians(deg) / 2), math.cos(math.radians(deg) / 2)
    return {"x": axis[0] * s, "y": axis[1] * s, "z": axis[2] * s, "w": c}


def qmul(a, b):
    return {"w": a["w"] * b["w"] - a["x"] * b["x"] - a["y"] * b["y"] - a["z"] * b["z"],
            "x": a["w"] * b["x"] + a["x"] * b["w"] + a["y"] * b["z"] - a["z"] * b["y"],
            "y": a["w"] * b["y"] - a["x"] * b["z"] + a["y"] * b["w"] + a["z"] * b["x"],
            "z": a["w"] * b["z"] + a["x"] * b["y"] - a["y"] * b["x"] + a["z"] * b["w"]}


def qrot(q, v):
    u = (q["x"], q["y"], q["z"])
    c1 = (u[1] * v[2] - u[2] * v[1], u[2] * v[0] - u[0] * v[2], u[0] * v[1] - u[1] * v[0])
    c2 = (u[1] * c1[2] - u[2] * c1[1], u[2] * c1[0] - u[0] * c1[2], u[0] * c1[1] - u[1] * c1[0])
    return tuple(v[i] + 2 * (q["w"] * c1[i] + c2[i]) for i in range(3))


def vec(v):
    return {"x": round(v[0], 3), "y": round(v[1], 3), "z": round(v[2], 3)}


def round_q(q):
    return {k: round(q[k], 6) for k in "xyzw"}


class Atlas:
    def __init__(self):
        self.x = self.y = self.row = 0
        self.blits = []
        self.ids = 0

    def put(self, img):
        h, w = len(img), len(img[0])
        if self.x + w > ATLAS_W:
            self.x, self.y, self.row = 0, self.y + self.row + 1, 0
        pos = (self.x, self.y)
        self.blits.append((pos, img))
        self.x += w + 1
        self.row = max(self.row, h)
        return pos

    def texture(self):
        H = (self.y + self.row + 31) // 32 * 32
        tex = [[[0, 0, 0, 0] for _ in range(ATLAS_W)] for _ in range(H)]
        for (x0, y0), img in self.blits:
            for y, row in enumerate(img):
                for x, c in enumerate(row):
                    tex[y0 + y][x0 + x] = list(c)
        return ATLAS_W, H, tex

    def nid(self):
        self.ids += 1
        return str(self.ids)


atlas = Atlas()


def uv(pos):
    return {"offset": {"x": pos[0], "y": pos[1]}, "mirror": {"x": False, "y": False}, "angle": 0}


def box(name, center, size, paint, q=ID_Q, fullbright=False):
    sx, sy, sz = [max(1, int(round(s))) for s in size]
    o = (int(round(center[0] - size[0] / 2.0)), int(round(center[1] - size[1] / 2.0)), int(round(center[2] - size[2] / 2.0)))
    dims = {"front": (sx, sy), "back": (sx, sy), "left": (sz, sy), "right": (sz, sy), "top": (sx, sz), "bottom": (sx, sz)}
    stretch = {"x": size[0] / float(sx), "y": size[1] / float(sy), "z": size[2] / float(sz)}
    layout = {f: uv(atlas.put(paint(f, w, h, o))) for f, (w, h) in dims.items()}
    return {"id": atlas.nid(), "name": name, "position": vec(center), "orientation": round_q(q),
            "shape": {"type": "box", "offset": vec((0, 0, 0)), "stretch": {k: round(v, 4) for k, v in stretch.items()},
                      "settings": {"size": {"x": sx, "y": sy, "z": sz}}, "textureLayout": layout, "unwrapMode": "custom",
                      "visible": True, "doubleSided": False, "shadingMode": "fullbright" if fullbright else "flat"},
            "children": []}


def quad(name, center, img, q=ID_Q, fullbright=False):
    h, w = len(img), len(img[0])
    return {"id": atlas.nid(), "name": name, "position": vec(center), "orientation": round_q(q),
            "shape": {"type": "quad", "offset": vec((0, 0, 0)), "stretch": {"x": 1, "y": 1, "z": 1},
                      "settings": {"size": {"x": w, "y": h}, "normal": "+Z"}, "textureLayout": {"front": uv(atlas.put(img))},
                      "unwrapMode": "custom", "visible": True, "doubleSided": False,
                      "shadingMode": "fullbright" if fullbright else "flat"},
            "children": []}


def group(name, pos, children, q=ID_Q):
    return {"id": atlas.nid(), "name": name, "position": vec(pos), "orientation": round_q(q),
            "shape": {"type": "none", "offset": vec((0, 0, 0)), "stretch": {"x": 1, "y": 1, "z": 1}, "settings": {},
                      "textureLayout": {}, "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": "flat"},
            "children": list(children)}


# ---------------------------------------------------------------------------------------------------------------------
# Pieces (y en unites depuis le sol)
# ---------------------------------------------------------------------------------------------------------------------
SHAFT_W, SHAFT_D, SHAFT_Y0, SHAFT_Y1 = 16, 10, 9, 49


def socle():
    return [box("Socle_Bas", (0, 2.5, 0), (28, 5, 28), STONE_DARK),
            box("Socle_Haut", (0, 7, 0), (22, 4, 22), STONE_DARK_SMOOTH),
            box("Socle_Liseret", (0, 9.25, 0), (18, 0.5, 12), GOLD_M)]


def shaft():
    h = SHAFT_Y1 - SHAFT_Y0
    n = [box("Fut", (0, SHAFT_Y0 + h / 2.0, 0), (SHAFT_W, h, SHAFT_D), SHAFT)]
    for sx in (-1, 1):                                                         # pilastres d'or aux quatre aretes
        for sz in (-1, 1):
            n.append(box("Pilastre_%d_%d" % (sx, sz), (sx * (SHAFT_W / 2.0 - 0.5), SHAFT_Y0 + h / 2.0, sz * (SHAFT_D / 2.0 - 0.5)),
                         (1.6, h, 1.6), GOLD_M))
    n.append(box("Bague_Bas", (0, SHAFT_Y0 + 1, 0), (SHAFT_W + 1, 2, SHAFT_D + 1), GOLD_M))
    n.append(box("Bague_Haut", (0, SHAFT_Y1 - 1, 0), (SHAFT_W + 1, 2, SHAFT_D + 1), GOLD_M))
    z = SHAFT_D / 2.0
    n.append(quad("Banniere", (0, 34.5, z + 0.6), banner(12, 25)))
    n.append(quad("Blason", (0, 37, z + 0.8), shield(10, 14)))
    n.append(box("Plaque_Registre", (0, 17, z + 0.5), (12, 7, 1), engraved))
    # banniere au dos (meme etoffe, sans blason)
    n.append(quad("Banniere_Dos", (0, 31, -z - 0.6), banner(12, 32), q=q_axis((0, 1, 0), 180)))
    return n


def capital():
    y = SHAFT_Y1
    return [box("Chapiteau", (0, y + 2.5, 0), (22, 5, 14), CAP),
            box("Chapiteau_Corniche", (0, y + 5.5, 0), (18, 1, 11), GOLD_M),
            box("Chapiteau_Toit", (0, y + 7, 0), (14, 2, 9), STONE_DARK_SMOOTH)]


def crown():
    y = SHAFT_Y1 + 8
    n = [box("Couronne_Cercle", (0, y + 1, 0), (9, 2, 9), GOLD_M)]
    for i, (dx, dz) in enumerate(((-4, -4), (4, -4), (-4, 4), (4, 4), (0, -4.5), (0, 4.5), (-4.5, 0), (4.5, 0))):
        tall = 3 if i < 4 else 2
        n.append(box("Couronne_Fleuron_%d" % i, (dx, y + 2 + tall / 2.0, dz), (1.5, tall, 1.5), GOLD_M))
    n.append(group("Particle_Emiter", (0, y + 6, 0), [
        box("Cristal", (0, 0, 0), (4, 6, 4), crystal, q=qmul(q_axis((0, 1, 0), 45), q_axis((1, 0, 0), 0)), fullbright=True)]))
    return n


root = group("Borne_Ville", (0, 0, 0), socle() + shaft() + capital() + crown())

HITBOX = {"Boxes": [
    {"Min": {"X": 0.06, "Y": 0, "Z": 0.06}, "Max": {"X": 0.94, "Y": 0.34, "Z": 0.94}},          # socle
    {"Min": {"X": 0.16, "Y": 0.34, "Z": 0.25}, "Max": {"X": 0.84, "Y": 2.0, "Z": 0.75}},          # fut, chapiteau, couronne
]}


def item():
    return {
        "$Comment": "Borne de ville (guards/CityBornes) : stele a blason. Genere par scripts/territoire/gen_borne_ville.py.",
        "TranslationProperties": {
            "Name": "Borne de ville",
            "Description": "Posée par le Comte dans sa ville (une par ville). Touche d'interaction : le registre de la ville "
                           "(trésor, règles, lois, biens, Bailli) pour le Comte, les lois de la ville pour les visiteurs."},
        "Icon": "Icons/ItemsGenerated/" + ITEM + ".png",
        "IconProperties": {"Scale": 0.45, "Rotation": [22.5, 45, 22.5], "Translation": [0, -26]},
        "Categories": ["Eldanior_System.Autres"],
        "MaxStack": 1,
        "Quality": "Rare",
        "PlayerAnimationsId": "Block",
        "Interactions": {"Primary": "Block_Primary", "Secondary": "Block_Secondary"},
        "BlockType": {
            "CustomModel": MODEL + ".blockymodel",
            "CustomModelTexture": [{"Texture": MODEL + "_Texture.png", "Weight": 1}],
            "HitboxType": ITEM,
            "VariantRotation": "NESW",
            "Material": "Solid",
            "Opacity": "Transparent",
            "DrawType": "Model",
            "Light": {"Color": "#7ac"},
            "BlockSoundSetId": "Stone",
            "PhysicalMaterialId": "Stone",
            "BlockParticleSetId": "Stone",
            "ParticleColor": "#e6e2d6",
            "TextureComputedColor": "#b8b2a0",
            "Support": {"Down": [{"FaceType": "Full"}]},
            "Gathering": {"Breaking": {"GatherType": "Benches"}},
            "Flags": {},
            "InteractionHint": "server.interactionHints.eldanior_borne_ville",
            "Interactions": {"Use": {"Interactions": [{"Type": "Simple"}]}},
        },
        "ItemSoundSetId": "ISS_Blocks_Stone",
    }


# ---------------------------------------------------------------------------------------------------------------------
# Rendu de controle et icone
# ---------------------------------------------------------------------------------------------------------------------
def collect(node, origin, q, out):
    p = qrot(q, (node["position"]["x"], node["position"]["y"], node["position"]["z"]))
    c = tuple(origin[i] + p[i] for i in range(3))
    q2 = qmul(q, node["orientation"])
    s = node["shape"]
    st = (s["stretch"]["x"], s["stretch"]["y"], s["stretch"]["z"])

    def world(v):
        r = qrot(q2, (v[0] * st[0], v[1] * st[1], v[2] * st[2]))
        return tuple(c[i] + r[i] for i in range(3))

    size = s["settings"].get("size")
    if s["type"] == "box":
        hx, hy, hz = size["x"] / 2.0, size["y"] / 2.0, size["z"] / 2.0
        faces = {
            "front": ([(-hx, hy, hz), (hx, hy, hz), (hx, -hy, hz), (-hx, -hy, hz)], (size["x"], size["y"])),
            "back": ([(hx, hy, -hz), (-hx, hy, -hz), (-hx, -hy, -hz), (hx, -hy, -hz)], (size["x"], size["y"])),
            "right": ([(hx, hy, hz), (hx, hy, -hz), (hx, -hy, -hz), (hx, -hy, hz)], (size["z"], size["y"])),
            "left": ([(-hx, hy, -hz), (-hx, hy, hz), (-hx, -hy, hz), (-hx, -hy, -hz)], (size["z"], size["y"])),
            "top": ([(-hx, hy, -hz), (hx, hy, -hz), (hx, hy, hz), (-hx, hy, hz)], (size["x"], size["z"])),
            "bottom": ([(-hx, -hy, hz), (hx, -hy, hz), (hx, -hy, -hz), (-hx, -hy, -hz)], (size["x"], size["z"])),
        }
        for f, (verts, (w, h)) in faces.items():
            o = s["textureLayout"][f]["offset"]
            out.append(([world(v) for v in verts], [(o["x"], o["y"]), (o["x"] + w, o["y"]), (o["x"] + w, o["y"] + h), (o["x"], o["y"] + h)]))
    elif s["type"] == "quad":
        hx, hy = size["x"] / 2.0, size["y"] / 2.0
        o = s["textureLayout"]["front"]["offset"]
        out.append(([world(v) for v in [(-hx, hy, 0), (hx, hy, 0), (hx, -hy, 0), (-hx, -hy, 0)]],
                    [(o["x"], o["y"]), (o["x"] + size["x"], o["y"]), (o["x"] + size["x"], o["y"] + size["y"]), (o["x"], o["y"] + size["y"])]))
    for ch in node["children"]:
        collect(ch, c, q2, out)


def draw(quads, tex, size, yaw, pitch, margin=0.05):
    cy, sy, cp, sp = math.cos(math.radians(yaw)), math.sin(math.radians(yaw)), math.cos(math.radians(pitch)), math.sin(math.radians(pitch))

    def project(v):
        x = v[0] * cy + v[2] * sy
        z = -v[0] * sy + v[2] * cy
        return x, v[1] * cp - z * sp, v[1] * sp + z * cp

    pts = [project(v) for vs, _ in quads for v in vs]
    x0, x1 = min(p[0] for p in pts), max(p[0] for p in pts)
    y0, y1 = min(p[1] for p in pts), max(p[1] for p in pts)
    scale = size * (1 - 2 * margin) / max(x1 - x0, y1 - y0)
    mx, my = (x0 + x1) / 2.0, (y0 + y1) / 2.0
    img = [[[0, 0, 0, 0] for _ in range(size)] for _ in range(size)]
    zb = [[-1e9] * size for _ in range(size)]

    def tri(a, b, c, ta, tb, tc):
        area = (b[0] - a[0]) * (c[1] - a[1]) - (b[1] - a[1]) * (c[0] - a[0])
        if abs(area) < 1e-6:
            return
        for y in range(max(0, int(min(a[1], b[1], c[1]))), min(size, int(max(a[1], b[1], c[1])) + 2)):
            for x in range(max(0, int(min(a[0], b[0], c[0]))), min(size, int(max(a[0], b[0], c[0])) + 2)):
                px, py = x + 0.5, y + 0.5
                w0 = ((b[0] - px) * (c[1] - py) - (b[1] - py) * (c[0] - px)) / area
                w1 = ((c[0] - px) * (a[1] - py) - (c[1] - py) * (a[0] - px)) / area
                w2 = 1 - w0 - w1
                if w0 < -1e-4 or w1 < -1e-4 or w2 < -1e-4:
                    continue
                d = w0 * a[2] + w1 * b[2] + w2 * c[2]
                if d <= zb[y][x]:
                    continue
                u, v = w0 * ta[0] + w1 * tb[0] + w2 * tc[0], w0 * ta[1] + w1 * tb[1] + w2 * tc[1]
                col = tex[min(len(tex) - 1, max(0, int(v)))][min(len(tex[0]) - 1, max(0, int(u)))]
                if col[3] < 128:
                    continue
                zb[y][x] = d
                img[y][x] = list(col)

    for verts, uvs in quads:
        s = [project(v) for v in verts]
        s = [(size / 2.0 + (p[0] - mx) * scale, size / 2.0 - (p[1] - my) * scale, p[2]) for p in s]
        tri(s[0], s[1], s[2], uvs[0], uvs[1], uvs[2])
        tri(s[0], s[2], s[3], uvs[0], uvs[2], uvs[3])
    return img


def downscale(img, f):
    n = len(img) // f
    out = []
    for y in range(n):
        row = []
        for x in range(n):
            px = [img[y * f + j][x * f + i] for j in range(f) for i in range(f)]
            a = sum(p[3] for p in px)
            row.append([0, 0, 0, 0] if a == 0 else [sum(p[k] * p[3] for p in px) / a for k in range(3)] + [a / len(px)])
        out.append(row)
    return out


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


def hint():
    lang = os.path.join(SERVER, "Languages", "en-US", "server.lang")
    text = open(lang, encoding="utf-8").read()
    key = "interactionHints.eldanior_borne_ville="
    if key not in text:
        text = text.replace("interactionHints.eldanior_npcpost=", key + "Appuyez sur [{key}] pour ouvrir le registre de la ville\n"
                            "interactionHints.eldanior_npcpost=", 1)
        open(lang, "w", encoding="utf-8").write(text)
        print("ajoute", key)


w, h, tex = atlas.texture()
model_dir = os.path.join(COMMON, *MODEL.split("/")[:-1])
os.makedirs(model_dir, exist_ok=True)
png.write(os.path.join(COMMON, *(MODEL + "_Texture.png").split("/")), w, h, tex)
with open(os.path.join(COMMON, *(MODEL + ".blockymodel").split("/")), "w", encoding="utf-8") as f:
    json.dump({"nodes": [root], "lod": "auto"}, f, indent=2, ensure_ascii=False)
write_json(os.path.join(SERVER, "Item", "Items", "Furniture", "Eldanior", ITEM + ".json"), item())
write_json(os.path.join(SERVER, "Item", "Block", "Hitboxes", "Furniture", ITEM + ".json"), HITBOX)
hint()

quads = []
collect(root, (0, 0, 0), ID_Q, quads)
png.write(os.path.join(COMMON, "Icons", "ItemsGenerated", ITEM + ".png"), 64, 64, downscale(draw(quads, tex, 256, 30, 18), 4))
print("Borne de ville : atlas %dx%d, %d noeuds" % (w, h, atlas.ids))

if PREVIEW:
    os.makedirs(PREVIEW, exist_ok=True)
    SIZE = 500
    views = ((0, 6), (35, 18), (-60, 14), (180, 10))
    sheet = [[[70, 76, 86, 255] for _ in range(SIZE * 2)] for _ in range(SIZE * 2)]
    for i, (yaw, pitch) in enumerate(views):
        frame = draw(quads, tex, SIZE, yaw, pitch)
        ox, oy = (i % 2) * SIZE, (i // 2) * SIZE
        for y in range(SIZE):
            for x in range(SIZE):
                if frame[y][x][3]:
                    sheet[oy + y][ox + x] = frame[y][x]
    png.write(os.path.join(PREVIEW, "borne_ville.png"), SIZE * 2, SIZE * 2, sheet)
