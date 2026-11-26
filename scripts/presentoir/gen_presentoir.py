"""Presentoir : petit temple de marbre d'un bloc de cote pour 3 de haut. Socle a gradins, cercle de runes au sol, colonnes
cannelees, arcs a encorbellement, frise bleu et or, coupole d'ecailles creuse (une lance tenue par le mannequin y loge sa pointe)
et gemme lumineuse au sommet. Textures a 2 px par unite.

Genere tout ce qui n'est pas du code :
- le bloc (32 unites = 1 bloc, origine = centre bas du bloc, 1 px = 1 unite), sa hitbox, son objet et son icone ;
- le support tournant des objets (64 unites = 1 bloc) : os R-Attachment / L-Attachment, comme une main ; une arme attachee a
  ces os a la lame vers +Y ; version a deux os pour les armes doubles ;
- l'animation des objets sans os de main (racine Origin, Root...), montres comme modele principal de l'entite ;
- les poses du mannequin (Parent vanilla : Mannequin, meme squelette que le joueur, donc les armures s'y posent).

Deux autres presentoirs, memes materiaux :
- le petit (1 de large, 2 de haut) : reliquaire sur colonne pour potions et trophees, a hauteur des yeux ;
- le presentoir de famille (3 x 3 x 3, un objet par famille) : estrade, mur d'honneur a l'ecu de la famille, bannieres et gemmes.
  Le bloc pose est le centre de l'estrade : l'objet expose y reste quelle que soit l'orientation du bloc.

La hitbox couvre tout le volume : l'entite exposee reste dedans et la touche d'interaction vise toujours le bloc.

Usage : python3 scripts/presentoir/gen_presentoir.py <src/main/resources> [dossier_apercu]
"""
import copy, json, math, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
for folder in ("rune_aura_icons", "armures"):
    sys.path.insert(0, os.path.join(HERE, "..", folder))
import png  # noqa: E402
from familles import FAMILIES, arms, category  # noqa: E402

RES = sys.argv[1]
PREVIEW = sys.argv[2] if len(sys.argv) > 2 else None
COMMON = os.path.join(RES, "Common")
SERVER = os.path.join(RES, "Server")
BLOCK_ID = "Eldanior_Presentoir"
SMALL_ID = BLOCK_ID + "_Petit"
FAMILY_ID = BLOCK_ID + "_Famille_"
HEIGHT = 96     # unites de bloc : 3 blocs
PX = 2          # pixels de texture par unite
FLOOR = 6       # dessus du socle : a reporter dans StandKind (6/32), comme les deux suivants
SMALL_FLOOR, FAMILY_FLOOR = 20, 8

# Noeuds racines des modeles d'objets sans os de main (inventaire vanilla + mod du 2026-11-01). Une racine absente de cette
# liste laisse simplement l'objet immobile.
OBJECT_ROOTS = ("Root", "Origin", "Ingot", "Node", "Fragment", "FabricMid", "Chest", "Onion", "Cube", "Wool", "R_Attachment", "Stud",
                "Flower", "LightOrb", "Sap", "Base", "Thread", "Origin_Projectile", "Bag")


# ---------------------------------------------------------------------------------------------------------------------
# Atlas et noeuds
# ---------------------------------------------------------------------------------------------------------------------
class Atlas:
    """Rangement par etageres ; deux faces de meme taille et de meme peinture partagent leur place."""

    def __init__(self, width):
        self.w, self.x, self.y, self.row, self.rects, self.ids, self.seen = width, 0, 0, 0, [], 0, {}

    def put(self, w, h, paint, key=None):
        k = (key or id(paint), w, h)
        if k in self.seen:
            return self.seen[k]
        if self.x + w > self.w:
            self.x, self.y, self.row = 0, self.y + self.row, 0
        pos = (self.x, self.y)
        self.seen[k] = pos
        self.rects.append((pos, w, h, paint))
        self.x += w
        self.row = max(self.row, h)
        return pos

    def nid(self):
        self.ids += 1
        return str(self.ids)

    def image(self):
        h = (self.y + self.row + 31) // 32 * 32
        img = [[[0, 0, 0, 0] for _ in range(self.w)] for _ in range(h)]
        for (ox, oy), w, rh, paint in self.rects:
            for j in range(rh):
                for i in range(w):
                    c = paint(i, j, w, rh)
                    img[oy + j][ox + i] = c if len(c) == 4 else c + [255]
        return self.w, h, img


ID_Q = (0, 0, 0, 1)
FACE_LIGHT = {"front": 1.0, "back": 0.84, "left": 0.92, "right": 0.92, "top": 1.1, "bottom": 0.62}


def vec(v):
    return {"x": v[0], "y": v[1], "z": v[2]}


def quat(q):
    return {"x": round(q[0], 6), "y": round(q[1], 6), "z": round(q[2], 6), "w": round(q[3], 6)}


def axis(ax, deg):
    h = math.radians(deg) / 2
    return (ax[0] * math.sin(h), ax[1] * math.sin(h), ax[2] * math.sin(h), math.cos(h))


def qmul(a, b):
    ax, ay, az, aw = a
    bx, by, bz, bw = b
    return (aw * bx + ax * bw + ay * bz - az * by, aw * by - ax * bz + ay * bw + az * bx,
            aw * bz + ax * by - ay * bx + az * bw, aw * bw - ax * bx - ay * by - az * bz)


def uv(pos):
    return {"offset": {"x": pos[0], "y": pos[1]}, "mirror": {"x": False, "y": False}, "angle": 0}


def box(atlas, name, center, size, paint, q=ID_Q, fullbright=False, px=PX):
    """Tailles en unites ; la texture compte px pixels par unite (taille en pixels, stretch 1/px)."""
    sx, sy, sz = [int(round(v * px)) for v in size]
    dims = {"front": (sx, sy), "back": (sx, sy), "left": (sz, sy), "right": (sz, sy), "top": (sx, sz), "bottom": (sx, sz)}
    layout = {}
    for f, (w, h) in dims.items():
        k = 1.0 if fullbright else FACE_LIGHT[f]
        shaded = lambda i, j, fw, fh, k=k: [min(255, int(c * k)) for c in paint(i, j, fw, fh)[:3]] + [255]
        layout[f] = uv(atlas.put(w, h, shaded, key=(id(paint), k)))
    return {"id": atlas.nid(), "name": name, "position": vec(center), "orientation": quat(q),
            "shape": {"type": "box", "offset": vec((0, 0, 0)), "stretch": vec((1.0 / px, 1.0 / px, 1.0 / px)), "settings": {"size": vec((sx, sy, sz))},
                      "textureLayout": layout, "unwrapMode": "custom", "visible": True, "doubleSided": False,
                      "shadingMode": "fullbright" if fullbright else "flat"},
            "children": []}


def quad(atlas, name, center, wpx, hpx, paint, q=ID_Q, fullbright=False):
    """Image a plat, face vers +Z avant rotation (parties transparentes decoupees), visible des deux cotes."""
    return {"id": atlas.nid(), "name": name, "position": vec(center), "orientation": quat(q),
            "shape": {"type": "quad", "offset": vec((0, 0, 0)), "stretch": vec((1.0 / PX, 1.0 / PX, 1)), "settings": {"size": {"x": wpx, "y": hpx}, "normal": "+Z"},
                      "textureLayout": {"front": uv(atlas.put(wpx, hpx, paint))}, "unwrapMode": "custom", "visible": True, "doubleSided": True,
                      "shadingMode": "fullbright" if fullbright else "flat"},
            "children": []}


def floor_disc(atlas, name, y, size, paint):
    n = int(size * PX)
    return quad(atlas, name, (0, y, 0), n, n, paint, q=axis((1, 0, 0), -90), fullbright=True)


def group(atlas, name, pos, children, piece=False):
    node = {"id": atlas.nid(), "name": name, "position": vec(pos), "orientation": quat(ID_Q),
            "shape": {"type": "none", "offset": vec((0, 0, 0)), "stretch": vec((1, 1, 1)), "settings": {}, "textureLayout": {},
                      "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": "flat"},
            "children": list(children)}
    if piece:
        node["settings"] = {"isPiece": True}
    return node


# ---------------------------------------------------------------------------------------------------------------------
# Peintures (i, j en pixels dans la face, w, h taille de la face)
# ---------------------------------------------------------------------------------------------------------------------
def clamp(c):
    return [max(0, min(255, int(v))) for v in c]


def noise(i, j, amp):
    return ((i * 73856093 ^ j * 19349663) >> 3) % (2 * amp + 1) - amp


def bevel(i, j, w, h):
    return 16 if i == 0 or j == 0 else (-18 if i == w - 1 or j == h - 1 else 0)


def marble(i, j, w, h):
    vein = math.sin(i * 0.31 + j * 0.17 + 3 * math.sin(j * 0.09 + i * 0.05))
    dark = 46 if vein > 0.93 else (16 if vein > 0.78 else 0)
    v = noise(i, j, 3) + bevel(i, j, w, h) - dark
    return clamp((226 + v, 222 + v, 216 + v * 0.8))


def fluted(i, j, w, h):
    t = math.cos((i + 0.5) / max(1, w) * math.pi * 6)
    v = t * 24 - 6 + noise(i, j, 2)
    return clamp((222 + v, 218 + v, 212 + v))


def basalt(i, j, w, h):
    joint = -14 if j % 8 == 7 else 0
    v = noise(i, j, 5) + joint + bevel(i, j, w, h)
    return clamp((48 + v, 52 + v, 72 + v))


def gold(i, j, w, h):
    t = 0.5 + 0.5 * math.sin((i + j) * 0.42)
    return clamp([a + (b - a) * t + bevel(i, j, w, h) * 1.4 for a, b in zip((184, 128, 40), (250, 214, 112))])


def scales(i, j, w, h):
    """Ecailles de coupole, bleu roi, rangees decalees."""
    row = j // 5
    x = (i + (row % 2) * 4) % 8
    arc = abs(x - 3.5) / 3.5
    edge = (j % 5) >= 4 - arc * 2.5
    v = noise(i, j, 3)
    base = (28, 54, 128) if row % 3 else (36, 72, 154)
    return clamp([c + v + (34 if edge else 0) for c in base])


def frieze(i, j, w, h):
    """Bandeau bleu borde d'or, grecque doree au milieu."""
    if j < 1 or j >= h - 1:
        return gold(i, j, w, h)
    y, x = j - 1, i % 8
    key = (y == 1 and x < 7) or (x == 0 and 1 <= y <= 5) or (y == 5 and x < 5) or (x == 4 and 3 <= y <= 5) or (y == 3 and 2 <= x <= 4)
    return gold(i, j, w, h) if key and h >= 8 else clamp((24 + noise(i, j, 3), 44 + noise(i, j, 3), 112 + noise(i, j, 4)))


def crystal(i, j, w, h):
    t = (i / max(1.0, w - 1) + j / max(1.0, h - 1)) / 2
    glint = 60 if i + j < 3 else 0
    return clamp((70 + 80 * t + glint, 190 + 50 * t + glint, 255))


def rune_disc(i, j, w, h):
    """Cercle de runes vu de dessus : deux anneaux, huit glyphes entre eux, etoile centrale."""
    c = (w - 1) / 2.0
    x, y = i - c, j - c
    r, a = math.hypot(x, y) / c, (math.atan2(y, x) + 2 * math.pi) % (2 * math.pi)
    glow = [90, 214, 255, 255]
    off = [0, 0, 0, 0]
    if 0.90 <= r <= 0.98 or 0.60 <= r <= 0.65:
        return glow
    if 0.66 < r < 0.89:
        sector = a / (math.pi / 4)
        k, f = int(sector), sector - int(sector)
        d = (r - 0.66) / 0.23
        strokes = ((0.5, None), (0.3 + 0.1 * (k % 3), 0.5), (0.7 - 0.1 * (k % 2), 0.25))
        for fx, fy in strokes:
            if abs(f - fx) < 0.05 and (fy is None or abs(d - fy) < 0.22 + 0.05 * (k % 2)) and 0.1 < d < 0.9:
                return [200, 240, 255, 255]
        return off
    if r < 0.58:
        star = abs(math.cos(a * 4)) ** 0.5 * 0.36 + 0.06
        if abs(r - star) < 0.035:
            return glow
    return off


# ---------------------------------------------------------------------------------------------------------------------
# Bloc
# ---------------------------------------------------------------------------------------------------------------------
def ring(a, name, y, outer, thick, height, paint):
    """Cadre carre creux : quatre poutres sans face commune."""
    h = outer / 2.0
    side = outer - 2 * thick
    c = h - thick / 2.0
    return [box(a, name + "_Avant", (0, y, c), (outer, height, thick), paint), box(a, name + "_Arriere", (0, y, -c), (outer, height, thick), paint),
            box(a, name + "_Droite", (c, y, 0), (thick, height, side), paint), box(a, name + "_Gauche", (-c, y, 0), (thick, height, side), paint)]


def block_model():
    a = Atlas(512)
    parts = [box(a, "Socle", (0, 1.5, 0), (32, 3, 32), basalt),
             box(a, "Socle_Filet", (0, 3.5, 0), (30, 1, 30), gold),
             box(a, "Dalle", (0, 5, 0), (28, 2, 28), marble),
             floor_disc(a, "Cercle_Runes", FLOOR + 0.1, 24, rune_disc)]

    for i, (sx, sz) in enumerate(((1, 1), (1, -1), (-1, 1), (-1, -1))):
        cx, cz = sx * 14, sz * 14
        foot, head = sx * 13.5, sz * 13.5
        parts += [box(a, "Base_Colonne_%d" % i, (foot, 7, head), (5, 2, 5), marble),
                  box(a, "Filet_Bas_%d" % i, (cx, 8.5, cz), (4, 1, 4), gold),
                  box(a, "Colonne_%d" % i, (cx, 42.5, cz), (3, 67, 3), fluted),
                  box(a, "Filet_Milieu_%d" % i, (cx, 37.5, cz), (4, 1, 4), gold),
                  box(a, "Filet_Haut_%d" % i, (foot, 65.5, head), (5, 1, 5), gold),
                  box(a, "Chapiteau_%d" % i, (cx, 67, cz), (4, 2, 4), marble),
                  box(a, "Fleuron_%d" % i, (cx, 81, cz), (4, 2, 4), gold),
                  box(a, "Fleuron_Gemme_%d" % i, (cx, 83.3, cz), (2, 2, 2), crystal, q=qmul(axis((0, 1, 0), 45), axis((1, 0, 0), 35)), fullbright=True)]

    # Arcs : trois pas de pierre partent de chaque colonne et se rejoignent sous une clef d'or.
    steps = ((69.5, 3, 9.5, 3), (72.5, 3, 7.0, 5.5), (75.0, 2, 4.0, 8.5))
    for side, (flip, sign) in enumerate(((False, 1), (False, -1), (True, 1), (True, -1))):
        def place(name, x, y, w, h):
            at, size = ((x, y, sign * 14), (w, h, 3)) if not flip else ((sign * 14, y, x), (3, h, w))
            parts.append(box(a, "%s_%d" % (name, side), at, size, gold if name == "Clef" else marble))
        for k, (y, h, reach, w) in enumerate(steps):
            place("Arc_D%d" % k, reach + w / 2.0, y, w, h)
            place("Arc_G%d" % k, -reach - w / 2.0, y, w, h)
        place("Clef", 0, 75, 8, 2)

    parts += ring(a, "Corniche", 78, 32, 4, 4, frieze)
    parts += ring(a, "Filet_Corniche", 80.5, 30, 2, 1, gold)
    # Coupole creuse : des cadres de plus en plus etroits, fermes par un chapeau d'or.
    parts += ring(a, "Coupole_1", 82.5, 24, 3, 3, scales)
    parts += ring(a, "Coupole_2", 85.5, 18, 3, 3, scales)
    parts += ring(a, "Coupole_3", 88.5, 12, 3, 3, scales)
    parts += [box(a, "Chapeau", (0, 90.5, 0), (8, 1, 8), gold),
              box(a, "Coupe_Gemme", (0, 91.5, 0), (5, 1, 5), gold),
              box(a, "Gemme", (0, 93.4, 0), (3, 3, 3), crystal, q=qmul(axis((0, 1, 0), 45), axis((1, 0, 0), 35.26)), fullbright=True)]
    return a, group(a, "Presentoir", (0, 0, 0), parts)


def small_model():
    """Reliquaire : colonne pleine, plateau a cercle de runes, cage de quatre fils d'or, petite coupole."""
    a = Atlas(256)
    parts = [box(a, "Socle", (0, 1.5, 0), (30, 3, 30), basalt),
             box(a, "Socle_Filet", (0, 3.5, 0), (28, 1, 28), gold),
             box(a, "Pied", (0, 6.5, 0), (22, 5, 22), marble),
             box(a, "Fut", (0, 13, 0), (14, 8, 14), fluted),
             box(a, "Collier", (0, 17.5, 0), (18, 1, 18), gold),
             box(a, "Plateau", (0, 19, 0), (28, 2, 28), marble),
             floor_disc(a, "Cercle_Runes", SMALL_FLOOR + 0.1, 22, rune_disc)]
    for i, (sx, sz) in enumerate(((1, 1), (1, -1), (-1, 1), (-1, -1))):
        parts.append(box(a, "Fil_%d" % i, (sx * 12, 35, sz * 12), (2, 30, 2), gold))
    parts += ring(a, "Corniche", 51.5, 28, 3, 3, frieze)
    parts += ring(a, "Coupole_1", 55.5, 20, 3, 3, scales)
    parts += ring(a, "Coupole_2", 58.5, 13, 3, 3, scales)
    parts += [box(a, "Chapeau", (0, 60.5, 0), (8, 1, 8), gold),
              box(a, "Gemme", (0, 62.5, 0), (2.5, 2.5, 2.5), crystal, q=qmul(axis((0, 1, 0), 45), axis((1, 0, 0), 35.26)), fullbright=True)]
    return a, group(a, "Presentoir", (0, 0, 0), parts)


# ---------------------------------------------------------------------------------------------------------------------
# Presentoir de famille
# ---------------------------------------------------------------------------------------------------------------------
FAM = {"color": FAMILIES[0][2], "shield": None}     # famille en cours : les peintures ci-dessous la lisent
SHIELD_PX = 88


def lit(c, k):
    return clamp([v * k for v in c])


def enamel(i, j, w, h):
    """Email de la famille a treillis, comme les battants de sa porte."""
    lattice = (i + j) % 24 < 1 or (i - j) % 24 < 1
    k = 1.0 + 0.1 * math.sin(i * 0.05 + j * 0.04) + noise(i, j, 2) / 90.0
    return lit(FAM["color"], k * (1.3 if lattice else 1.0))


def fam_frieze(i, j, w, h):
    if j < 2 or j >= h - 2:
        return gold(i, j, w, h)
    y, x = j - 2, i % 8
    key = 2 <= y <= 7 and ((y == 2 and x < 7) or (x == 0) or (y == 7 and x < 5) or (x == 4 and y >= 5) or (y == 5 and 2 <= x <= 4))
    return gold(i, j, w, h) if key else lit(FAM["color"], 0.9 + noise(i, j, 3) / 60.0)


def carpet(i, j, w, h):
    edge = min(i, w - 1 - i)
    if edge < 2:
        return gold(i, j, w, h)
    weave = 1.0 + (0.08 if (i + j) % 2 else -0.04) + noise(i, j, 2) / 80.0
    return lit(FAM["color"], weave * (1.3 if edge == 5 else 0.9))


def banner(i, j, w, h):
    """Banniere a queue d'aronde, filet d'or, losange central."""
    tail = (h - 1 - j) < 18 - abs(i - (w - 1) / 2.0) * 18 / (w / 2.0)
    if tail:
        return [0, 0, 0, 0]
    bottom = h - 1 - j
    edge = min(i, w - 1 - i, j, bottom - 18 + abs(i - (w - 1) / 2.0) * 18 / (w / 2.0))
    if edge < 2:
        return gold(i, j, w, h)
    if abs(i - (w - 1) / 2.0) + abs(j - h * 0.38) < 7:
        return gold(i, j, w, h)
    fold = 1.0 + 0.12 * math.sin(i * 0.6) + noise(i, j, 2) / 80.0
    return lit(FAM["color"], fold)


def shield(i, j, w, h):
    return FAM["shield"][j][i]


def family_model():
    a = Atlas(512)
    gem_q = qmul(axis((0, 1, 0), 45), axis((1, 0, 0), 35.26))
    parts = [box(a, "Degre_Bas", (0, 1.5, 0), (96, 3, 96), basalt),
             box(a, "Degre_Filet", (0, 3.5, 0), (94, 1, 94), gold),
             box(a, "Degre_Haut", (0, 6, 0), (86, 4, 86), marble),
             floor_disc(a, "Cercle_Runes", FAMILY_FLOOR + 0.1, 44, rune_disc),
             box(a, "Tapis", (0, FAMILY_FLOOR + 0.25, 33.5), (20, 0.5, 19), carpet),
             # Mur d'honneur
             box(a, "Mur", (0, 46, -40), (80, 76, 8), marble),
             box(a, "Panneau", (0, 46, -35.5), (52, 64, 1), enamel),
             box(a, "Cadre_Haut", (0, 79, -35), (56, 2, 2), gold), box(a, "Cadre_Bas", (0, 13, -35), (56, 2, 2), gold),
             box(a, "Cadre_Droit", (27, 46, -35), (2, 64, 2), gold), box(a, "Cadre_Gauche", (-27, 46, -35), (2, 64, 2), gold)]
    sw, sh = SHIELD_PX, len(FAM["shield"])
    parts.append(quad(a, "Ecu", (0, 46.5, -34.8), sw, sh, shield))
    for i, sx in enumerate((1, -1)):
        parts += [box(a, "Pilastre_%d" % i, (sx * 44, 46, -40), (8, 76, 12), fluted),
                  box(a, "Pilastre_Base_%d" % i, (sx * 44, 9, -39.5), (8, 2, 13), gold),
                  box(a, "Pilastre_Tete_%d" % i, (sx * 44, 80, -39.5), (8, 2, 13), gold),
                  box(a, "Hampe_%d" % i, (sx * 34, 76.5, -34.8), (12, 1, 1), gold),
                  quad(a, "Banniere_%d" % i, (sx * 34, 50, -35.2), 20, 104, banner),
                  # Gemmes de part et d'autre du tapis
                  box(a, "Vasque_Pied_%d" % i, (sx * 37, 10, 37), (12, 4, 12), marble),
                  box(a, "Vasque_Fut_%d" % i, (sx * 37, 20, 37), (6, 16, 6), fluted),
                  box(a, "Vasque_Filet_%d" % i, (sx * 37, 28.5, 37), (8, 1, 8), gold),
                  box(a, "Vasque_%d" % i, (sx * 37, 30.5, 37), (10, 3, 10), gold),
                  box(a, "Gemme_%d" % i, (sx * 37, 35, 37), (4, 4, 4), crystal, q=gem_q, fullbright=True)]
    parts += [box(a, "Corniche", (0, 85, -40), (96, 6, 16), fam_frieze),
              box(a, "Fronton_1", (0, 89.5, -40), (72, 3, 12), marble),
              box(a, "Fronton_2", (0, 92, -40), (44, 2, 10), scales),
              box(a, "Fronton_Filet", (0, 93.5, -40), (14, 1, 8), gold),
              box(a, "Gemme", (0, 94.8, -40), (2, 2, 2), crystal, q=gem_q, fullbright=True)]
    return a, group(a, "Presentoir", (0, 0, 0), parts)


# ---------------------------------------------------------------------------------------------------------------------
# Support des armes
# ---------------------------------------------------------------------------------------------------------------------
def support_model(double):
    a = Atlas(64)
    spread = 10 if double else 0
    halo = []
    for i in range(8):                  # octogone : la geometrie visible que tout porteur doit avoir
        an = i * 45
        c = (7 * math.sin(math.radians(an)), 0, 7 * math.cos(math.radians(an)))
        halo.append(box(a, "Halo_%d" % i, c, (6, 1, 1), crystal if i % 2 else gold, q=axis((0, 1, 0), an), fullbright=True, px=1))
    rotor = group(a, "Rotor", (0, 0, 0), [group(a, "Halo", (0, -3, 0), halo),
                                          group(a, "R-Attachment", (-spread, 0, 0), []),
                                          group(a, "L-Attachment", (spread, 0, 0), [])])
    return a, group(a, "Origin", (0, 0, 0), [rotor])


def turning_anim(nodes):
    """Un tour en 9 s a vitesse constante, et une lente respiration verticale, sur chaque noeud donne."""
    turn = 540
    keys = []
    for i in range(5):
        half = math.radians(i * 90) / 2
        keys.append({"time": turn * i // 4, "delta": {"x": 0, "y": round(math.sin(half), 6), "z": 0, "w": round(math.cos(half), 6)},
                     "interpolationType": "linear"})
    bob = [{"time": t, "delta": vec((0, y, 0)), "interpolationType": "smooth"} for t, y in ((0, 0), (turn // 2, 3), (turn, 0))]
    return {"duration": turn, "holdLastKeyframe": False,
            "nodeAnimations": {n: {"position": bob, "orientation": keys, "shapeStretch": [], "shapeVisible": [], "shapeUvOffset": []} for n in nodes}}


def pose(raised):
    """Avant-bras releves de 90 degres : la main tient l'arme lame en haut (repere de R-Attachment : +Y = pointe)."""
    q = {"x": round(-math.sin(math.radians(45)), 6), "y": 0, "z": 0, "w": round(math.cos(math.radians(45)), 6)}
    nodes = {}
    for bone in raised:
        nodes[bone] = {"position": [], "orientation": [{"time": 0, "delta": q, "interpolationType": "smooth"}],
                       "shapeStretch": [], "shapeVisible": [], "shapeUvOffset": []}
    return {"duration": 60, "holdLastKeyframe": True, "nodeAnimations": nodes}


# ---------------------------------------------------------------------------------------------------------------------
# Objet et assets serveur
# ---------------------------------------------------------------------------------------------------------------------
def item():
    return {
        "TranslationProperties": {
            "Name": "Présentoir",
            "Description": "Expose armes, artefacts, armures et trophées. Interagir avec un objet en main pour l'exposer, la main vide pour le reprendre.",
        },
        "Icon": "Icons/ItemsGenerated/%s.png" % BLOCK_ID,
        "Categories": ["Eldanior_System.Autres", "Furniture.Furniture"],
        "Recipe": {
            "TimeSeconds": 4,
            "Input": [{"ResourceTypeId": "Rock", "Quantity": 16}, {"ItemId": "Ingredient_Bar_Gold", "Quantity": 4}],
            "BenchRequirement": [{"Type": "Crafting", "Categories": ["Furniture_Misc"], "Id": "Furniture_Bench"}],
        },
        "Interactions": {"Primary": "Block_Primary", "Secondary": "Block_Secondary"},
        "BlockType": {
            "Material": "Solid",
            "DrawType": "Model",
            "Opacity": "Transparent",
            "CustomModel": "Blocks/Eldanior/Presentoir.blockymodel",
            "CustomModelTexture": [{"Texture": "Blocks/Eldanior/Presentoir_Texture.png", "Weight": 1}],
            "HitboxType": BLOCK_ID,
            "Gathering": {"Breaking": {"GatherType": "Benches"}},
            "BlockParticleSetId": "Stone",
            "ParticleColor": "#dcd8d0",
            "BlockSoundSetId": "Stone",
            "PhysicalMaterialId": "Stone",
            "TextureComputedColor": "#DCD8D0",
            "Support": {"Down": [{"FaceType": "Full"}]},
            "Light": {"Color": "#58a"},
            "Particles": [{"SystemId": "Block_Gem_Sparks", "Color": "#7fd8ff", "TargetNodeName": "Gemme"}],
            "Interactions": {"Use": {"Interactions": [{"Type": "Simple"}]}},
        },
        "PlayerAnimationsId": "Block",
        "IconProperties": {"Scale": 0.28, "Translation": [0, -24], "Rotation": [22.5, 45, 22.5]},
        "MaxStack": 1,
        "ItemLevel": 20,
        "ItemSoundSetId": "ISS_Blocks_Wood",
        "Quality": "Uncommon",
    }


def hitbox_boxes(*boxes):
    return {"Boxes": [{"Min": {"X": b[0], "Y": b[1], "Z": b[2]}, "Max": {"X": b[3], "Y": b[4], "Z": b[5]}} for b in boxes]}


def hitbox():
    return hitbox_boxes((0, 0, 0, 1, HEIGHT / 32.0, 1))


def family_hitbox():
    """En blocs depuis le coin du bloc pose (x + 16) / 32. L'estrade se parcourt ; seuls le mur, les vasques et le pilier central bloquent.
    Le pilier enveloppe l'objet expose : l'entite reste dedans et la touche d'interaction vise le bloc."""
    def cell(v):
        return (v + 16) / 32.0
    boxes = [(-1, 0, -1, 2, FAMILY_FLOOR / 32.0, 2), (-1, FAMILY_FLOOR / 32.0, -1, 2, 3, cell(-34)), (0, FAMILY_FLOOR / 32.0, 0, 1, 2.75, 1)]
    for sx in (1, -1):
        boxes.append((cell(sx * 37 - 6), FAMILY_FLOOR / 32.0, cell(31), cell(sx * 37 + 6), cell(38), cell(43)))
    return hitbox_boxes(*boxes)


def small_item():
    data = copy.deepcopy(item())
    data["TranslationProperties"] = {"Name": "Petit présentoir",
                                     "Description": "Reliquaire pour potions, butins et petits objets. Interagir avec un objet en main pour l'exposer, la main vide pour le reprendre."}
    data["Icon"] = "Icons/ItemsGenerated/%s.png" % SMALL_ID
    data["Recipe"]["Input"] = [{"ResourceTypeId": "Rock", "Quantity": 6}, {"ItemId": "Ingredient_Bar_Gold", "Quantity": 1}]
    data["BlockType"]["CustomModel"] = "Blocks/Eldanior/Presentoir_Petit.blockymodel"
    data["BlockType"]["CustomModelTexture"] = [{"Texture": "Blocks/Eldanior/Presentoir_Petit_Texture.png", "Weight": 1}]
    data["BlockType"]["HitboxType"] = SMALL_ID
    data["IconProperties"] = {"Scale": 0.36, "Translation": [0, -18], "Rotation": [22.5, 45, 22.5]}
    return data


def family_item(fid, name, color):
    data = copy.deepcopy(item())
    item_id = FAMILY_ID + fid
    data["TranslationProperties"] = {"Name": "Présentoir de famille - " + name,
                                     "Description": "Estrade d'honneur de 3 blocs sur 3 aux armes de la famille " + name + ". Se pose par son centre. Interagir avec un objet en main pour l'exposer, la main vide pour le reprendre."}
    data["Icon"] = "Icons/ItemsGenerated/%s.png" % item_id
    del data["Recipe"]
    data["Categories"] = [category(fid), "Furniture.Furniture"]
    data["BlockType"]["CustomModel"] = "Blocks/Eldanior/Presentoir_Famille.blockymodel"
    data["BlockType"]["CustomModelTexture"] = [{"Texture": "Blocks/Eldanior/Presentoir_Famille_%s_Texture.png" % fid, "Weight": 1}]
    data["BlockType"]["HitboxType"] = "Eldanior_Presentoir_Famille"
    data["BlockType"]["VariantRotation"] = "NESW"
    data["BlockType"]["ParticleColor"] = "#%02x%02x%02x" % color
    data["BlockType"]["Particles"] = [{"SystemId": "Block_Gem_Sparks", "Color": "#7fd8ff", "TargetNodeName": n} for n in ("Gemme", "Gemme_0", "Gemme_1")]
    data["IconProperties"] = {"Scale": 0.2, "Translation": [0, -20], "Rotation": [22.5, 45, 22.5]}
    data["ItemLevel"] = 300
    data["Quality"] = "Epic"
    return item_id, data


def support_asset(model, anim="Support_Idle"):
    return {
        "Model": "Items/Eldanior/Presentoir/%s.blockymodel" % model,
        "Texture": "Items/Eldanior/Presentoir/%s_Texture.png" % model,
        "EyeHeight": 0.5,
        "HitBox": {"Min": {"X": -0.4, "Y": 0, "Z": -0.4}, "Max": {"X": 0.4, "Y": 2, "Z": 0.4}},
        "AnimationSets": {"Idle": {"Animations": [{"Animation": "Items/Eldanior/Presentoir/%s.blockyanim" % anim, "Looping": True}]}},
    }


def mannequin_asset(anim):
    return {"Parent": "Mannequin",
            "AnimationSets": {"Idle": {"Animations": [{"Animation": "Items/Eldanior/Presentoir/%s.blockyanim" % anim, "Looping": True}]}}}


def write_json(path, data, indent=2):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=indent, ensure_ascii=False)
        f.write("\n")


# ---------------------------------------------------------------------------------------------------------------------
# Icone et apercu : rendu orthographique des boites droites
# ---------------------------------------------------------------------------------------------------------------------
def qrot(q, v):
    x, y, z, w = q
    t = (2 * (y * v[2] - z * v[1]), 2 * (z * v[0] - x * v[2]), 2 * (x * v[1] - y * v[0]))
    return (v[0] + w * t[0] + y * t[2] - z * t[1], v[1] + w * t[1] + z * t[0] - x * t[2], v[2] + w * t[2] + x * t[1] - y * t[0])


def faces_of(root):
    """Faces (coin de texture, taille en pixels, point local d'apres u, v en pixels), toutes tournees vers le monde."""
    out = []
    for n in root["children"]:
        c, o = n["position"], n["orientation"]
        at, q = (c["x"], c["y"], c["z"]), (o["x"], o["y"], o["z"], o["w"])
        s, L = n["shape"], n["shape"]["textureLayout"]
        def world(p, at=at, q=q):
            r = qrot(q, p)
            return (at[0] + r[0], at[1] + r[1], at[2] + r[2])
        if s["type"] == "quad":
            wq, hq, k = s["settings"]["size"]["x"], s["settings"]["size"]["y"], s["stretch"]["x"]
            out.append((L["front"], wq, hq, lambda u, v, wq=wq, hq=hq, k=k, world=world: world(((u - wq / 2.0) * k, (hq / 2.0 - v) * k, 0))))
            continue
        sz = s["settings"]["size"]
        st = s["stretch"]["x"]
        hx, hy, hz = sz["x"] * st / 2, sz["y"] * st / 2, sz["z"] * st / 2
        def add(face, w, h, fn):
            out.append((L[face], w, h, lambda u, v, fn=fn, world=world: world(fn(u * st, v * st))))
        add("front", sz["x"], sz["y"], lambda u, v, hx=hx, hy=hy, hz=hz: (-hx + u, hy - v, hz))
        add("back", sz["x"], sz["y"], lambda u, v, hx=hx, hy=hy, hz=hz: (hx - u, hy - v, -hz))
        add("right", sz["z"], sz["y"], lambda u, v, hx=hx, hy=hy, hz=hz: (hx, hy - v, hz - u))
        add("left", sz["z"], sz["y"], lambda u, v, hx=hx, hy=hy, hz=hz: (-hx, hy - v, -hz + u))
        add("top", sz["x"], sz["z"], lambda u, v, hx=hx, hy=hy, hz=hz: (-hx + u, hy, -hz + v))
        add("bottom", sz["x"], sz["z"], lambda u, v, hx=hx, hy=hy, hz=hz: (-hx + u, -hy, hz - v))
    return out


def render(root, tex, size, yaw_deg, pitch_deg, texels=None):
    """texels (dict) recoit le texel de chaque pixel : recolor() refait alors l'image d'une autre famille sans recalcul."""
    yaw, pitch = math.radians(yaw_deg), math.radians(pitch_deg)
    cy, sy, cp, sp = math.cos(yaw), math.sin(yaw), math.cos(pitch), math.sin(pitch)

    def project(v):
        x, y, z = v
        rx, rz = x * cy + z * sy, -x * sy + z * cy
        return rx, y * cp - rz * sp, y * sp + rz * cp

    faces = faces_of(root)
    pts = [project(f[3](u, v)) for f in faces for u, v in ((0, 0), (f[1], 0), (0, f[2]), (f[1], f[2]))]
    x0, x1 = min(p[0] for p in pts), max(p[0] for p in pts)
    y0, y1 = min(p[1] for p in pts), max(p[1] for p in pts)
    k = 0.94 * size / max(x1 - x0, y1 - y0)
    ox, oy = size / 2.0 - k * (x0 + x1) / 2, size / 2.0 + k * (y0 + y1) / 2
    img = [[[0, 0, 0, 0] for _ in range(size)] for _ in range(size)]
    zb = [[-1e9] * size for _ in range(size)]
    step = min(0.9, 1.0 / k)
    for layout, w, h, at in faces:
        tx0, ty0 = layout["offset"]["x"], layout["offset"]["y"]
        u = 0.0
        while u < w:
            v = 0.0
            while v < h:
                px, py, d = project(at(u, v))
                sx, sy2 = int(ox + k * px), int(oy - k * py)
                if 0 <= sx < size and 0 <= sy2 < size and d > zb[sy2][sx]:
                    col = tex[ty0 + int(v)][tx0 + int(u)]
                    if col[3]:
                        zb[sy2][sx] = d
                        img[sy2][sx] = list(col)
                        if texels is not None:
                            texels[(sy2, sx)] = (ty0 + int(v), tx0 + int(u))
                v += step
            u += step
    return img


def recolor(texels, tex, size):
    img = [[[0, 0, 0, 0] for _ in range(size)] for _ in range(size)]
    for (y, x), (ty, tx) in texels.items():
        img[y][x] = list(tex[ty][tx])
    return img


def downscale(img, f):
    n = len(img) // f
    out = []
    for y in range(n):
        row = []
        for x in range(n):
            px = [img[y * f + j][x * f + i] for j in range(f) for i in range(f)]
            a = sum(p[3] for p in px)
            row.append([0, 0, 0, 0] if a == 0 else [sum(p[k] * p[3] for p in px) // a for k in range(3)] + [a // len(px)])
        out.append(row)
    return out


# ---------------------------------------------------------------------------------------------------------------------
# Ecriture
# ---------------------------------------------------------------------------------------------------------------------
atlas, root = block_model()
w, h, tex = atlas.image()
block_dir = os.path.join(COMMON, "Blocks", "Eldanior")
os.makedirs(block_dir, exist_ok=True)
png.write(os.path.join(block_dir, "Presentoir_Texture.png"), w, h, tex)
write_json(os.path.join(block_dir, "Presentoir.blockymodel"), {"nodes": [root], "lod": "auto"})
write_json(os.path.join(SERVER, "Item", "Items", "Furniture", "Eldanior", BLOCK_ID + ".json"), item())
write_json(os.path.join(SERVER, "Item", "Block", "Hitboxes", "Furniture", BLOCK_ID + ".json"), hitbox())

item_dir = os.path.join(COMMON, "Items", "Eldanior", "Presentoir")
os.makedirs(item_dir, exist_ok=True)
for name, double in (("Support", False), ("Support_Double", True)):
    a, node = support_model(double)
    sw, sh, stex = a.image()
    png.write(os.path.join(item_dir, name + "_Texture.png"), sw, sh, stex)
    write_json(os.path.join(item_dir, name + ".blockymodel"), {"nodes": [node], "lod": "auto"})
    write_json(os.path.join(SERVER, "Models", "Eldanior", "Presentoir", "Eldanior_Presentoir_" + name + ".json"), support_asset(name))
write_json(os.path.join(item_dir, "Support_Idle.blockyanim"), turning_anim(("Rotor",)))
write_json(os.path.join(item_dir, "Objet_Idle.blockyanim"), turning_anim(OBJECT_ROOTS))
write_json(os.path.join(SERVER, "Models", "Eldanior", "Presentoir", "Eldanior_Presentoir_Objet.json"), support_asset("Support", "Objet_Idle"))

# Petit presentoir
small, small_root = small_model()
w2, h2, tex2 = small.image()
png.write(os.path.join(block_dir, "Presentoir_Petit_Texture.png"), w2, h2, tex2)
write_json(os.path.join(block_dir, "Presentoir_Petit.blockymodel"), {"nodes": [small_root], "lod": "auto"})
write_json(os.path.join(SERVER, "Item", "Items", "Furniture", "Eldanior", SMALL_ID + ".json"), small_item())
write_json(os.path.join(SERVER, "Item", "Block", "Hitboxes", "Furniture", SMALL_ID + ".json"), hitbox_boxes((0, 0, 0, 1, 2, 1)))
png.write(os.path.join(COMMON, "Icons", "ItemsGenerated", SMALL_ID + ".png"), 64, 64, downscale(render(small_root, tex2, 256, 35, 24), 4))

# Presentoirs de famille : un seul modele, une texture et un objet par famille
FAM["shield"] = arms(FAMILIES[0][2], FAMILIES[0][3], SHIELD_PX, px=56)
fam_atlas, fam_root = family_model()
write_json(os.path.join(block_dir, "Presentoir_Famille.blockymodel"), {"nodes": [fam_root], "lod": "auto"})
write_json(os.path.join(SERVER, "Item", "Block", "Hitboxes", "Furniture", "Eldanior_Presentoir_Famille.json"), family_hitbox())
icon_size = 256
texels = {}
preview_tex = None
for n, (fid, name, color, kind) in enumerate(FAMILIES):
    FAM["color"], FAM["shield"] = color, arms(color, kind, SHIELD_PX, px=56)
    fw, fh, ftex = fam_atlas.image()
    png.write(os.path.join(block_dir, "Presentoir_Famille_%s_Texture.png" % fid), fw, fh, ftex)
    item_id, data = family_item(fid, name, color)
    write_json(os.path.join(SERVER, "Item", "Items", "Furniture", "Eldanior", "Presentoirs_Famille", item_id + ".json"), data)
    frame = render(fam_root, ftex, icon_size, 28, 22, texels) if n == 0 else recolor(texels, ftex, icon_size)
    png.write(os.path.join(COMMON, "Icons", "ItemsGenerated", item_id + ".png"), 64, 64, downscale(frame, 4))
    if fid == "Runkandel":
        preview_tex = ftex
print("Petit : texture %dx%d, %d noeuds ; famille : %d noeuds, %d familles" % (w2, h2, small.ids, fam_atlas.ids, len(FAMILIES)))

POSES = {"Mannequin": ("Pose_Neutre", ()), "Mannequin_Droite": ("Pose_Droite", ("R-Forearm",)),
         "Mannequin_Gauche": ("Pose_Gauche", ("L-Forearm",)), "Mannequin_Double": ("Pose_Double", ("R-Forearm", "L-Forearm"))}
for name, (anim, raised) in POSES.items():
    write_json(os.path.join(item_dir, anim + ".blockyanim"), pose(raised))
    write_json(os.path.join(SERVER, "Models", "Eldanior", "Presentoir", "Eldanior_Presentoir_" + name + ".json"), mannequin_asset(anim))

png.write(os.path.join(COMMON, "Icons", "ItemsGenerated", BLOCK_ID + ".png"), 64, 64, downscale(render(root, tex, 256, 35, 24), 4))
print("Presentoir : texture %dx%d, %d noeuds" % (w, h, atlas.ids))

if PREVIEW:
    os.makedirs(PREVIEW, exist_ok=True)
    SIZE = 420
    sheet = [[[58, 62, 70, 255] for _ in range(SIZE * 3)] for _ in range(SIZE)]
    for i, (yaw, pitch) in enumerate(((35, 18), (0, 4), (20, 62))):
        frame = render(root, tex, SIZE, yaw, pitch)
        for y in range(SIZE):
            for x in range(SIZE):
                if frame[y][x][3]:
                    sheet[y][i * SIZE + x] = frame[y][x]
    png.write(os.path.join(PREVIEW, "presentoir.png"), SIZE * 3, SIZE, sheet)
    sheet = [[[58, 62, 70, 255] for _ in range(SIZE * 3)] for _ in range(SIZE)]
    for i, (root_, tex_, yaw, pitch) in enumerate(((small_root, tex2, 35, 16), (fam_root, preview_tex, 28, 20), (fam_root, preview_tex, 0, 6))):
        frame = render(root_, tex_, SIZE, yaw, pitch)
        for y in range(SIZE):
            for x in range(SIZE):
                if frame[y][x][3]:
                    sheet[y][i * SIZE + x] = frame[y][x]
    png.write(os.path.join(PREVIEW, "presentoirs_nouveaux.png"), SIZE * 3, SIZE, sheet)
