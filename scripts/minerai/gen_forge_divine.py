"""Forge Divine : vrai atelier de forgeron de 3 blocs de large sur 2 de profondeur, modele construit piece par piece.

Foyer maconne en basalte, lit de braises, hotte et cheminee, enclume sur billot avec une lame chauffee a blanc et son marteau,
soufflet relie au foyer, bac de trempe, caisse de minerai. Le metal divin n'apparait que la ou il a du sens : anneaux, ruban de
la hotte, runes, lingots.

Les faces reprennent les textures de blocs vanilla, posees en coordonnees du monde : les joints de briques se suivent d'une
piece a l'autre et la forge se fond dans le decor. Echelle des blocs : 32 unites = 1 bloc, 1 px = 1 unite.
Origine du modele = centre bas du bloc pose ; +Z = face vers le joueur. L'emprise va donc de x -48 a 48 et de z -48 a 16 ;
la hitbox est en blocs depuis le coin du bloc : (x + 16) / 32.

Animation (fin de fabrication) : le marteau frappe, le soufflet respire, les flammes montent.

Usage : python3 scripts/minerai/gen_forge_divine.py <src/main/resources> [dossier_apercu]
"""
import json, math, os, random, sys

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "rune_aura_icons"))
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import png  # noqa: E402
from vanilla_assets import ZIP, vanilla  # noqa: E402

RES = sys.argv[1]
PREVIEW = sys.argv[2] if len(sys.argv) > 2 else None
COMMON = os.path.join(RES, "Common")
SERVER = os.path.join(RES, "Server")
ATLAS_W = 512
MODEL = "Blocks/Eldanior/Forge_Divine"

FACE_LIGHT = {"front": 1.0, "back": 0.8, "left": 0.88, "right": 0.88, "top": 1.1, "bottom": 0.55}
WHITE = (255, 252, 236)
GOLD = (255, 214, 110)
GOLD_DARK = (176, 124, 52)
CYAN = (120, 224, 236)
BLUE = (34, 128, 190)
LEATHER = (92, 58, 36)


def clamp(v):
    return max(0, min(255, int(round(v))))


def mix(a, b, t):
    t = max(0.0, min(1.0, t))
    return tuple(a[i] + (b[i] - a[i]) * t for i in range(3))


_rng = random.Random(5)
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
    """Coordonnees de monde d'un pixel : deux boites voisines partagent les memes joints."""
    x, y, z = o
    if face == "front":
        return x + i, -(y + h) + j
    if face == "back":
        return -(x + w) + i, -(y + h) + j
    if face in ("left", "right"):
        return z + i, -(y + h) + j
    return x + i, z + j


def material(name, top=None, light=1.0, soot=0.0, edge=0.85):
    """Texture vanilla de bloc. soot > 0 noircit la surface comme la suie d'un foyer."""
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
                    k *= 1.08 - 0.22 * (j + 0.5) / h
                if soot:
                    k *= 1 - soot * (0.35 + 0.65 * noise(u * 0.09, v * 0.09))
                if min(i, j, w - 1 - i, h - 1 - j) == 0 and min(w, h) > 3:
                    k *= edge
                row.append([clamp(p[0] * k), clamp(p[1] * k), clamp(p[2] * k), 255])
            img.append(row)
        return img
    return paint


def divine_metal(light=1.0):
    """Metal divin : or chaud veine de lumiere, plus sombre que le lingot pour rester credible sur de la pierre."""
    def paint(face, w, h, o):
        img = []
        for j in range(h):
            row = []
            for i in range(w):
                u, v = world_uv(face, i, j, w, h, o)
                t = noise(u * 0.25, v * 0.25)
                c = mix(GOLD_DARK, GOLD, 0.25 + 0.75 * t)
                k = FACE_LIGHT[face] * light * (1.06 - 0.16 * (j + 0.5) / h)
                row.append([clamp(c[0] * k), clamp(c[1] * k), clamp(c[2] * k), 255])
            img.append(row)
        return img
    return paint


def flat(color, light=1.0):
    def paint(face, w, h, o):
        img = []
        for j in range(h):
            row = []
            for i in range(w):
                k = FACE_LIGHT[face] * light * (0.92 + 0.16 * noise(i * 0.4 + o[0], j * 0.4 + o[2]))
                row.append([clamp(color[0] * k), clamp(color[1] * k), clamp(color[2] * k), 255])
            img.append(row)
        return img
    return paint


def leather(face, w, h, o):
    img = flat(LEATHER)(face, w, h, o)
    if face not in ("top", "bottom"):                            # plis du cuir du soufflet
        for j in range(0, h, 2):
            for i in range(w):
                for c in range(3):
                    img[j][i][c] = clamp(img[j][i][c] * 0.68)
    return img


def coals(face, w, h, o):
    """Lit de braises : blocs de charbon noirs, joints incandescents, coeur blanc au centre du foyer."""
    rnd = random.Random(3)
    cells = [(rnd.uniform(0, w), rnd.uniform(0, h)) for _ in range(max(4, w * h // 14))]
    img = []
    for j in range(h):
        row = []
        for i in range(w):
            d = sorted(math.hypot(i - cx, j - cy) for cx, cy in cells)
            gap = d[1] - d[0] if len(d) > 1 else 9
            heat = max(0.0, 1 - math.hypot((i - w / 2.0) / (w / 2.0 + 1), (j - h / 2.0) / (h / 2.0 + 1)))
            if gap < 0.7:
                c = mix((214, 96, 28), GOLD, heat * 1.6)
                c = mix(c, WHITE, max(0.0, heat - 0.55) * 1.6)
            elif gap < 1.5:
                c = mix((96, 34, 20), (214, 96, 28), heat * 1.4)
            else:
                c = mix((16, 15, 19), (44, 26, 24), heat)
            row.append([clamp(c[0]), clamp(c[1]), clamp(c[2]), 255])
        img.append(row)
    return img


def hot_metal(face, w, h, o):
    img = []
    for j in range(h):
        row = []
        for i in range(w):
            t = (i + 0.5) / w
            c = mix(WHITE, (255, 150, 60), abs(t - 0.5) * 1.6)
            row.append([clamp(c[0]), clamp(c[1]), clamp(c[2]), 255])
        img.append(row)
    return img


def runes(face, w, h, o):
    """Bandeau de pierre noire grave de runes lumineuses."""
    img = [[[18, 18, 26, 255] for _ in range(w)] for _ in range(h)]
    if face != "front":
        return img
    rnd = random.Random(21)
    for x in range(2, w - 4, 6):
        for _ in range(3):
            x0, y0 = x + rnd.randint(0, 3), rnd.randint(1, max(1, h - 2))
            dx, dy = rnd.choice(((1, 0), (0, 1), (1, 1), (1, -1)))
            for k in range(rnd.randint(2, 4)):
                xx, yy = x0 + dx * k, y0 + dy * k
                if 0 <= yy < h and 0 <= xx < w:
                    img[yy][xx][:3] = list(CYAN if rnd.random() < 0.4 else GOLD)
    return img


def flame(w, h, seed):
    """Flamme decoupee en alpha : coeur blanc, corps or, pointes cyan."""
    img = []
    for j in range(h):
        row = []
        v = 1 - (j + 0.5) / h                                     # 0 en bas, 1 en haut
        wob = (noise(j * 0.22, seed) - 0.5) * 0.5 * v
        for i in range(w):
            u = (i + 0.5) / w * 2 - 1 - wob
            half = (1 - v) ** 0.7 * (0.6 + 0.4 * math.sin(min(1.0, v * 3.2) * math.pi / 2))
            if abs(u) > half:
                row.append([0, 0, 0, 0])
                continue
            t = 1 - abs(u) / max(0.01, half)                      # 1 au centre, 0 au bord
            t *= 1 - v * 0.7
            c = mix((70, 170, 214), (240, 150, 60), min(1.0, t * 3.0))
            c = mix(c, GOLD, max(0.0, t * 2.6 - 0.7))
            c = mix(c, WHITE, max(0.0, t * 2.4 - 1.5))
            row.append([clamp(c[0]), clamp(c[1]), clamp(c[2]), 255])
        img.append(row)
    return img


def water(face, w, h, o):
    """L'eau vanilla est grise et teintee en jeu : ici la couleur est peinte, sombre, avec quelques reflets."""
    img = []
    for j in range(h):
        row = []
        for i in range(w):
            t = noise(i * 0.22, j * 0.22)
            c = mix((22, 62, 94), (58, 124, 158), t * t * 1.7)
            row.append([clamp(c[0]), clamp(c[1]), clamp(c[2]), 255])
        img.append(row)
    return img


def crystal(face, w, h, o):
    return [[[clamp(mix(BLUE, CYAN, noise(i * 0.7, j * 0.7) * 1.3)[k]) for k in range(3)] + [255] for i in range(w)] for j in range(h)]


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
    """center et size en unites ; size est arrondi au pixel."""
    sx, sy, sz = [max(1, int(round(s))) for s in size]
    o = (int(round(center[0] - size[0] / 2.0)), int(round(center[1] - size[1] / 2.0)), int(round(center[2] - size[2] / 2.0)))
    dims = {"front": (sx, sy), "back": (sx, sy), "left": (sz, sy), "right": (sz, sy), "top": (sx, sz), "bottom": (sx, sz)}
    stretch = {"x": size[0] / float(sx), "y": size[1] / float(sy), "z": size[2] / float(sz)}
    layout = {f: uv(atlas.put(paint(f, w, h, o))) for f, (w, h) in dims.items()}
    return {"id": atlas.nid(), "name": name, "position": vec(center), "orientation": round_q(q),
            "shape": {"type": "box", "offset": vec((0, 0, 0)), "stretch": {k: round(v, 4) for k, v in stretch.items()}, "settings": {"size": {"x": sx, "y": sy, "z": sz}},
                      "textureLayout": layout, "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": "fullbright" if fullbright else "flat"},
            "children": []}


def quad(name, center, img, q=ID_Q, fullbright=True):
    h, w = len(img), len(img[0])
    return {"id": atlas.nid(), "name": name, "position": vec(center), "orientation": round_q(q),
            "shape": {"type": "quad", "offset": vec((0, 0, 0)), "stretch": {"x": 1, "y": 1, "z": 1}, "settings": {"size": {"x": w, "y": h}, "normal": "+Z"},
                      "textureLayout": {"front": uv(atlas.put(img))}, "unwrapMode": "custom", "visible": True, "doubleSided": True, "shadingMode": "fullbright" if fullbright else "flat"},
            "children": []}


def group(name, pos, children, q=ID_Q):
    return {"id": atlas.nid(), "name": name, "position": vec(pos), "orientation": round_q(q),
            "shape": {"type": "none", "offset": vec((0, 0, 0)), "stretch": {"x": 1, "y": 1, "z": 1}, "settings": {}, "textureLayout": {},
                      "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": "flat"},
            "children": list(children)}


def rod(name, a, b, thick, paint, fullbright=False):
    d = [b[i] - a[i] for i in range(3)]
    L = math.sqrt(sum(c * c for c in d))
    n = [c / L for c in d]
    axis = (n[2], 0.0, -n[0])
    s = math.hypot(axis[0], axis[2])
    q = ID_Q if s < 1e-6 else q_axis((axis[0] / s, 0, axis[2] / s), math.degrees(math.atan2(s, n[1])))
    return box(name, tuple((a[i] + b[i]) / 2.0 for i in range(3)), (thick, L, thick), paint, q=q, fullbright=fullbright)


BRICK = material("Rock_Shale_Brick", light=0.95)
BRICK_SOOT = material("Rock_Shale_Brick", light=0.82, soot=0.6)
SMOOTH = material("Rock_Shale_Brick_Smooth", light=0.9)
STONE = material("Rock_Shale_Cobble", light=0.95)
IRON = material("Metal_Iron", light=0.72)
STEEL = material("Metal_Iron_Smooth", light=1.0)
SOOT_IRON = material("Metal_Iron", light=0.5, soot=0.5)
WOOD = material("Wood_Trunk_Beech_Side", top="Wood_Trunk_Beech_Top", light=0.95)
PLANK = material("Wood_Trunk_Beech_Side", light=0.62)
ASH = material("Soil_Ashes", light=0.8)
WATER = water
DIVINE = divine_metal()


def hood():
    """Hotte : quatre pans de brique inclines qui se resserrent du linteau vers la cheminee (quads trapezes decoupes en alpha)."""
    y0, y1 = 69.5, 94.0
    base_x, base_z0, base_z1 = 31.5, -48.5, 8.5
    top_x, top_z0, top_z1 = 11.5, -36.0, -16.0
    panels = (
        ("Hotte_Avant", 0.0, (0, (base_z1 + top_z1) / 2.0), base_z1 - top_z1, 2 * base_x, 2 * top_x, 0.0, 1.0),
        ("Hotte_Arriere", 180.0, (0, (base_z0 + top_z0) / 2.0), top_z0 - base_z0, 2 * base_x, 2 * top_x, 0.0, 0.72),
        ("Hotte_Droite", 90.0, ((base_x + top_x) / 2.0, 0), base_x - top_x, base_z1 - base_z0, top_z1 - top_z0, ((base_z0 + base_z1) - (top_z0 + top_z1)) / 2.0, 0.85),
        ("Hotte_Gauche", -90.0, (-(base_x + top_x) / 2.0, 0), base_x - top_x, base_z1 - base_z0, top_z1 - top_z0, ((top_z0 + top_z1) - (base_z0 + base_z1)) / 2.0, 0.85),
    )
    tex = tile("Rock_Shale_Brick")
    nodes = []
    for name, yaw, (cx, cz), run, wb, wt, skew, light in panels:
        rise = y1 - y0
        length = math.hypot(run, rise)
        W, H = int(math.ceil(wb)) + 2, int(math.ceil(length))
        img = []
        for j in range(H):
            t = 1 - (j + 0.5) / H                                 # 0 en bas, 1 en haut
            half = (wb + (wt - wb) * t) / 2.0
            mid = skew * t / 2.0
            row = []
            for i in range(W):
                mx = i - W / 2.0 + 0.5
                if abs(mx - mid) > half + 1.0:                    # les pans se recouvrent aux angles
                    row.append([0, 0, 0, 0])
                    continue
                p = tex[j % len(tex)][i % len(tex[0])]
                k = light * 0.82 * (1 - 0.62 * (0.35 + 0.65 * noise(i * 0.09, j * 0.09)) * (0.35 + 0.65 * t))
                row.append([clamp(p[0] * k), clamp(p[1] * k), clamp(p[2] * k), 255])
            img.append(row)
        qy = q_axis((0, 1, 0), yaw)
        tilt = q_axis((1, 0, 0), -math.degrees(math.atan2(run, rise)))
        nodes.append(quad(name, (cx, (y0 + y1) / 2.0, cz if yaw in (0.0, 180.0) else -20 - 2.5 / 2.0), img, qmul(qy, tilt), fullbright=False))
    return nodes


def hearth():
    n = []
    n.append(box("Socle", (0, 3, -17), (62, 6, 60), SMOOTH))
    n.append(box("Foyer", (0, 16, -18), (56, 20, 54), BRICK))
    for name, c, s in (("Margelle_Avant", (0, 28, 8), (58, 4, 4)), ("Margelle_Arriere", (0, 28, -44), (58, 4, 4)),
                       ("Margelle_Gauche", (-27, 28, -18), (4, 4, 48)), ("Margelle_Droite", (27, 28, -18), (4, 4, 48))):
        n.append(box(name, c, s, IRON))
    n.append(box("Cendres", (0, 27.5, -18), (50, 3, 48), ASH))
    n.append(box("Braises", (0, 29.5, -20), (38, 2, 34), coals, fullbright=True))
    for i, x in enumerate((-20, 20)):                             # coins de renfort
        n.append(box("Coin_%d" % i, (x * 1.45, 16, 9.6), (2.5, 20, 1.5), DIVINE))
    # mur du fond et piliers
    n.append(box("Mur_Fond", (0, 46, -42), (42, 32, 6), BRICK_SOOT))
    for i, x in enumerate((-25, 25)):
        n.append(box("Pilier_%d" % i, (x, 46, -40), (8, 32, 12), BRICK))
        n.append(box("Colonne_%d" % i, (x, 45, 4), (4, 30, 4), IRON))
        n.append(box("Bague_%d" % i, (x, 32, 4), (5.5, 2, 5.5), DIVINE))
        n.append(box("Bague_Haut_%d" % i, (x, 58, 4), (5.5, 2, 5.5), DIVINE))
    # hotte : resserre vers la cheminee
    n.append(box("Linteau", (0, 64, -20), (62, 8, 56), BRICK))
    n.append(box("Ruban_Hotte", (0, 68.75, -20), (63, 1.5, 57), DIVINE))
    n.append(box("Runes", (0, 64, 8.6), (46, 5, 1), runes, fullbright=True))
    n.append(box("Hotte_Coeur", (0, 77, -24), (24, 14, 18), BRICK_SOOT))
    n += hood()
    n.append(box("Cheminee", (0, 104, -26), (22, 22, 20), BRICK_SOOT))
    n.append(box("Cheminee_Anneau", (0, 100, -26), (24, 2, 22), SOOT_IRON))
    n.append(box("Cheminee_Chapeau", (0, 118.5, -26), (26, 2.5, 24), SOOT_IRON))
    for i, (x, z) in enumerate(((-10, -35), (10, -35), (-10, -17), (10, -17))):
        n.append(box("Cheminee_Pied_%d" % i, (x, 116, z), (2, 3, 2), SOOT_IRON))
    # flammes
    flames = []
    for i, (x, z, w, h, yaw) in enumerate(((-10, -24, 10, 13, 0), (9, -17, 9, 11, 0), (0, -22, 13, 17, 0), (-3, -21, 11, 13, 90), (6, -24, 9, 12, 90))):
        flames.append(quad("Flamme_%d" % i, (x, h / 2.0, z - (-20)), flame(w, h, i * 7 + 2), q_axis((0, 1, 0), yaw)))
    n.append(group("Fire", (0, 31, -20), flames))
    return n


def anvil():
    n = []
    n.append(box("Billot", (-38, 8, -2), (18, 16, 18), WOOD))
    for i, y in enumerate((5, 12)):
        n.append(box("Cerclage_%d" % i, (-38, y, -2), (18.8, 1.5, 18.8), IRON))
    n.append(box("Enclume_Pied", (-38, 18, -2), (14, 4, 12), STEEL))
    n.append(box("Enclume_Taille", (-38, 22.5, -2), (7, 5, 6), STEEL))
    n.append(box("Enclume_Table", (-37, 27, -2), (20, 4, 8), STEEL))
    n.append(box("Enclume_Bigorne", (-30, 27, -2), (6, 2.5, 5), STEEL))
    n.append(box("Enclume_Bigorne_Pointe", (-26.5, 27, -2), (3, 1.5, 3), STEEL))
    n.append(box("Lame_Chauffee", (-38, 29.5, -2), (15, 1, 3), hot_metal, fullbright=True))
    n.append(box("Lame_Soie", (-47, 29.5, -2), (4, 0.8, 1.4), IRON))
    head = box("Marteau_Tete", (-12, 1.2, 0), (3.5, 6, 3.5), STEEL)
    handle = box("Marteau_Manche", (-6, 0, 0), (13, 1.6, 1.6), WOOD)
    n.append(group("Marteau", (-23, 31.5, 1), [handle, head], q_axis((0, 0, 1), 9)))
    return n


def bellows():
    n = []
    for i, (x, z) in enumerate(((36, -28), (46, -28), (36, -12), (46, -12))):
        n.append(box("Soufflet_Pied_%d" % i, (x, 9, z), (2.5, 18, 2.5), WOOD))
    n.append(box("Soufflet_Base", (41, 19, -20), (15, 2, 20), PLANK))
    n.append(box("Soufflet_Cuir", (41, 22.5, -20), (13, 5, 17), leather))
    n.append(rod("Soufflet_Bec", (34, 21, -20), (27, 26, -20), 3, IRON))
    top = box("Soufflet_Plateau", (8, 1, 0), (16, 1.5, 20), PLANK)
    lever = box("Soufflet_Levier", (15, 6, 0), (1.6, 10, 1.6), WOOD)
    n.append(group("Soufflet", (33, 25, -20), [top, lever], q_axis((0, 0, 1), 12)))
    return n


def quench():
    n = []
    n.append(box("Bac_Fond", (38, 1.5, 3), (20, 3, 24), STONE))
    n.append(box("Bac_Eau", (38, 8, 3), (17, 9, 21), WATER))
    for name, c, s in (("Bac_Avant", (38, 7.5, 14.25), (20, 12, 2.5)), ("Bac_Arriere", (38, 7.5, -8.25), (20, 12, 2.5)),
                       ("Bac_Gauche", (29.25, 7.5, 3), (2.5, 12, 20)), ("Bac_Droite", (46.75, 7.5, 3), (2.5, 12, 20))):
        n.append(box(name, c, s, STONE))
    for i, z in enumerate((-4, 10)):
        n.append(box("Bac_Cercle_%d" % i, (38, 7, z), (20.6, 1.5, 1), IRON))
    # pinces et marteau de reserve poses sur le bord du bac
    n.append(rod("Pince_A", (46, 13.5, 11), (32, 14.5, -7), 1, IRON))
    n.append(rod("Pince_B", (44, 13.5, 13), (32, 14.5, -6), 1, IRON))
    return n


def crate():
    n = []
    n.append(box("Caisse", (-38, 7, -32), (20, 14, 22), PLANK))
    for i, y in enumerate((3, 11)):
        n.append(box("Caisse_Bande_%d" % i, (-38, y, -32), (20.6, 1.5, 22.6), IRON))
    # lingots et cristaux bruts sur la caisse
    for i, (x, z, r) in enumerate(((-43, -38, 0), (-36, -38, 0), (-43, -29, 0), (-36, -29, 0))):
        n.append(box("Lingot_%d" % i, (x, 15.5, z), (6, 3, 8), DIVINE))
    n.append(box("Lingot_Haut", (-39.5, 18.5, -33), (6, 3, 8), DIVINE))
    for i, (x, z, s, yaw) in enumerate(((-34, -40, 6, 20), (-31, -36, 4, 50))):
        n.append(box("Cristal_%d" % i, (x, 17, z), (s, s + 3, s), crystal, q=q_axis((0, 1, 0), yaw), fullbright=True))
    return n


def tools():
    """Outils suspendus au linteau, cote gauche, comme dans un vrai atelier."""
    n = []
    x = -30
    n.append(box("Barre_Outils", (x, 57, -39), (1.5, 1.5, 14), IRON))
    n.append(rod("Outil_Pince_A", (x, 56, -44), (x, 42, -45.2), 1, IRON))
    n.append(rod("Outil_Pince_B", (x, 56, -44), (x, 42, -42.8), 1, IRON))
    n.append(box("Outil_Masse_Manche", (x, 49, -39), (1.2, 13, 1.2), WOOD))
    n.append(box("Outil_Masse_Tete", (x, 43, -39), (3, 4, 6), STEEL))
    n.append(box("Outil_Pelle_Manche", (x, 49, -34), (1.2, 13, 1.2), WOOD))
    n.append(box("Outil_Pelle_Fer", (x, 40, -34), (0.8, 6, 5), IRON))
    return n


root = group("Forge", (0, 0, 0), hearth() + anvil() + bellows() + quench() + crate() + tools())


# ---------------------------------------------------------------------------------------------------------------------
# Animation et item
# ---------------------------------------------------------------------------------------------------------------------
def key(t, q):
    return {"time": t, "delta": round_q(q), "interpolationType": "smooth"}


def stretch_key(t, s):
    return {"time": t, "delta": {"x": 1 + (s - 1) * 0.3, "y": s, "z": 1}, "interpolationType": "smooth"}


def node_anim(orientation=(), stretch=()):
    return {"position": [], "orientation": list(orientation), "shapeStretch": list(stretch), "shapeVisible": [], "shapeUvOffset": []}


def animation():
    z = (0, 0, 1)
    nodes = {
        "Marteau": node_anim(orientation=[key(0, ID_Q), key(14, q_axis(z, -44)), key(22, q_axis(z, 4)), key(24, ID_Q), key(36, q_axis(z, -44)), key(44, q_axis(z, 4)), key(46, ID_Q), key(60, ID_Q)]),
        "Soufflet": node_anim(orientation=[key(0, ID_Q), key(30, q_axis(z, -11)), key(60, ID_Q)]),
    }
    for i in range(5):
        p = i * 4
        nodes["Flamme_%d" % i] = node_anim(stretch=[stretch_key(0, 1.0), stretch_key(15 + p, 1.35), stretch_key(45, 1.1), stretch_key(60, 1.0)])
    return {"duration": 60, "holdLastKeyframe": False, "nodeAnimations": nodes}


def forge_item():
    item = json.loads(ZIP.read("Server/Item/Items/Bench/Bench_Weapon.json"))
    item["TranslationProperties"] = {"Name": "Forge Divine", "Description": "Forge céleste. Seule sa chaleur peut façonner le métal divin en artefact. Compétence requise : Forge Divine."}
    item["Quality"] = "Divin"
    item["ItemLevel"] = 800
    item["Categories"] = ["Eldanior_System.Autres", "Furniture.Benches"]
    item["Icon"] = "Icons/ItemsGenerated/Bench_Forge_Divine.png"
    item["IconProperties"] = {"Scale": 0.3, "Translation": [0, -20], "Rotation": [22.5, 45, 22.5]}
    item["Recipe"] = {"TimeSeconds": 8, "Input": [{"ItemId": "Ingredient_Bar_Divin", "Quantity": 12}, {"ItemId": "Ingredient_Bar_Adamantite", "Quantity": 12},
                                                  {"ResourceTypeId": "Rock", "Quantity": 20}],
                      "BenchRequirement": [{"Type": "Crafting", "Categories": ["Weapon_Sword"], "Id": "Weapon_Bench", "RequiredTierLevel": 3}]}   # l'etabli est interdit (CraftingConfig.BLOCKED_BENCHES)
    bt = item["BlockType"]
    bt["CustomModel"] = MODEL + ".blockymodel"
    bt["CustomModelTexture"] = [{"Texture": MODEL + "_Texture.png", "Weight": 1}]
    bt["HitboxType"] = "Bench_Forge_Divine"
    bt["ParticleColor"] = "#3a3a44"
    bt["Light"] = {"Color": "#fc8"}
    bt["Particles"] = [{"SystemId": "Fire_Furnace_On", "TargetNodeName": "Fire", "Scale": 1.1, "PositionOffset": {"X": 0, "Z": 0}}]
    bt["PhysicalMaterialId"] = "Stone"
    bt["TextureComputedColor"] = "#35343C"
    anim = MODEL + "_Crafting.blockyanim"
    bt["State"] = {"Definitions": {"CraftCompleted": {"CustomModelAnimation": anim, "Looping": True}, "CraftCompletedInstant": {"CustomModelAnimation": anim}}}
    bench = bt["Bench"]
    bench["Id"] = "Forge_Divine"
    bench["TierLevels"] = [{"CraftingTimeReductionModifier": 0.0}]
    bench["Categories"] = [
        {"Id": "Forge_Epees", "Icon": "Icons/CraftingCategories/Armory/Sword.png", "Name": "server.benchCategories.forge_epees"},
        {"Id": "Forge_Lances", "Icon": "Icons/CraftingCategories/Armory/Weapons.png", "Name": "server.benchCategories.forge_lances"},
        {"Id": "Forge_Dagues", "Icon": "Icons/CraftingCategories/Armory/Daggers.png", "Name": "server.benchCategories.forge_dagues"},
        {"Id": "Forge_Blocs", "Icon": "Icons/CraftingCategories/Armory/MetalArmor.png", "Name": "server.benchCategories.forge_blocs"},
    ]
    return item


HITBOX = {"Boxes": [
    {"Min": {"X": -1.0, "Y": 0, "Z": -1.0}, "Max": {"X": 2.0, "Y": 0.9, "Z": 1.0}},                  # atelier, jusqu'a hauteur de table
    {"Min": {"X": -0.44, "Y": 0.9, "Z": -1.0}, "Max": {"X": 1.44, "Y": 1.7, "Z": 0.0}},              # fond du foyer
    {"Min": {"X": -0.5, "Y": 1.7, "Z": -1.0}, "Max": {"X": 1.5, "Y": 2.45, "Z": 0.82}},              # hotte
    {"Min": {"X": 0.1, "Y": 2.45, "Z": -0.9}, "Max": {"X": 0.9, "Y": 3.8, "Z": 0.1}},                # cheminee
]}


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


def write_json(rel, data):
    path = os.path.join(SERVER, *rel.split("/"))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


w, h, tex = atlas.texture()
model_dir = os.path.join(COMMON, "Blocks", "Eldanior")
os.makedirs(model_dir, exist_ok=True)
png.write(os.path.join(model_dir, "Forge_Divine_Texture.png"), w, h, tex)
with open(os.path.join(model_dir, "Forge_Divine.blockymodel"), "w", encoding="utf-8") as f:
    json.dump({"nodes": [root], "lod": "auto"}, f, indent=2, ensure_ascii=False)
with open(os.path.join(model_dir, "Forge_Divine_Crafting.blockyanim"), "w", encoding="utf-8") as f:
    json.dump(animation(), f, indent=2)
write_json("Item/Items/Bench/Bench_Forge_Divine.json", forge_item())
write_json("Item/Block/Hitboxes/Bench/Bench_Forge_Divine.json", HITBOX)

quads = []
collect(root, (0, 0, 0), ID_Q, quads)
png.write(os.path.join(COMMON, "Icons", "ItemsGenerated", "Bench_Forge_Divine.png"), 64, 64, downscale(draw(quads, tex, 256, 35, 24), 4))
print("Forge Divine : %dx%d, %d noeuds" % (w, h, atlas.ids))

if PREVIEW:
    os.makedirs(PREVIEW, exist_ok=True)
    SIZE = 900
    views = ((0, 8), (35, 20), (-35, 20), (180, 12))
    sheet = [[[58, 62, 70, 255] for _ in range(SIZE * 2)] for _ in range(SIZE * 2)]
    for i, (yaw, pitch) in enumerate(views):
        frame = draw(quads, tex, SIZE, yaw, pitch)
        ox, oy = (i % 2) * SIZE, (i // 2) * SIZE
        for y in range(SIZE):
            for x in range(SIZE):
                if frame[y][x][3]:
                    sheet[oy + y][ox + x] = frame[y][x]
    png.write(os.path.join(PREVIEW, "forge_divine.png"), SIZE * 2, SIZE * 2, sheet)
