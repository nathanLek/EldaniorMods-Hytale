"""Portes verrouillees et cles. Genere tout ce qui n'est pas du code du package com.eldanior.system.locks :

- la Porte du Tresor, 5 blocs de large pour 7 de haut, a deux battants : modele, hitboxes, animations (le volant tourne, puis les
  battants s'ouvrent), une texture et un objet par famille noble, avec son blason ;
- les portes de boss, une par palier de rang de donjon : une grande porte vanilla et son miroir, soit une double porte de 4 blocs
  sur 4 en une seule pose ;
- les cles : une par famille, une par palier ;
- les coffres qui donnent une cle de boss, un par palier.

Le lien porte -> cle n'est ecrit nulle part : c'est le nom. Eldanior_Porte_Famille_X s'ouvre avec Eldanior_Cle_Famille_X, et
Eldanior_Porte_Boss_X avec Eldanior_Cle_Boss_X (voir DoorLock.java). Un bloc peut donc aller dans un prefab ou une instance sans registre.

Bloc : 32 unites = 1 bloc, origine au centre bas du bloc pose. Ce bloc est le milieu du bas de la Porte du Tresor ; sur une porte de
boss, la deuxieme case en partant de la gauche.

Modeles animes : rester pres du vanilla (138 modeles releves : pieces de 128 pixels au plus, 82 noeuds au plus). Un battant d'un seul
tenant de 424 pixels de haut perdait son haut et son bas une fois ouvert : d'ou LEAF_SEGMENTS. Ici 106 de haut, 144 de large.

Usage : python3 scripts/portes/gen_portes.py <src/main/resources> [dossier_apercu]
"""
import copy, json, math, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
for folder in ("rune_aura_icons", "armures", "minerai"):
    sys.path.insert(0, os.path.join(HERE, "..", folder))
import png  # noqa: E402
import blason  # noqa: E402
from familles import FAMILIES, GOLD, GOLD_DARK, GOLD_LIGHT, LINE, arms, category  # noqa: E402
from vanilla_assets import ZIP, vanilla  # noqa: E402

RES = sys.argv[1]
PREVIEW = sys.argv[2] if len(sys.argv) > 2 else None
COMMON = os.path.join(RES, "Common")
SERVER = os.path.join(RES, "Server")

# Dimensions de la porte, en unites
WIDTH, HEIGHT = 160, 224
POST, LINTEL, FRAME_DEPTH = 8, 12, 24
LEAF_W, LEAF_H, LEAF_T = 72, 212, 10    # un battant
LEAF_PX = 2                             # pixels de texture par unite sur la face des battants
LEAF_SEGMENTS = 4                       # un battant est coupe en hauteur : 106 pixels par piece
SEGMENT_H = LEAF_H // LEAF_SEGMENTS

MODEL = "Blocks/Eldanior/Porte_Tresor.blockymodel"
ANIM = "Blocks/Eldanior/Porte_Tresor_%s.blockyanim"
HITBOX = "Eldanior_Porte_Tresor"
BOSS_HITBOX = "Eldanior_Porte_Boss"
# Double porte de boss : cases x = -1..2 autour du bloc pose. Charnieres, depuis l'origine du modele.
BOSS_HINGE_LEFT, BOSS_HINGE_RIGHT = -48, 80

STEEL, STEEL_DARK = (66, 69, 78), (40, 42, 50)

# id, libelle, couleur, porte vanilla, qualite, niveau de l'objet
BOSS_TIERS = [
    ("ED", "E-D", (176, 112, 64), "Furniture/Human/Unique/Furniture_Human_Ruins_Door_Large", "Rare", 100),
    ("CB", "C-B", (150, 172, 188), "Furniture/Temple_Earth/Furniture_Temple_Earth_Door", "Rare", 250),
    ("A", "A", (232, 182, 78), "Furniture/Temple_Light/Unique/Furniture_Temple_Light_Door_Large", "Epic", 500),
    ("S", "S", (196, 34, 44), "Furniture/Temple_Dark/Unique/Furniture_Temple_Dark_Door_Large", "Legendary", 800),
]


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


# ---------------------------------------------------------------------------------------------------------------------
# Atlas et noeuds (memes conventions que scripts/presentoir)
# ---------------------------------------------------------------------------------------------------------------------
class Atlas:
    """Rangement par etageres ; deux faces de meme taille et de meme peinture partagent leur place."""

    def __init__(self, width):
        self.w, self.x, self.y, self.row, self.rects, self.ids, self.seen = width, 0, 0, 0, [], 0, {}

    def put(self, w, h, paint, key):
        k = (key, w, h)
        if k in self.seen:
            return self.seen[k]
        if self.x + w > self.w:
            self.x, self.y, self.row = 0, self.y + self.row, 0
        self.seen[k] = (self.x, self.y)
        self.rects.append((self.seen[k], w, h, paint))
        self.x += w
        self.row = max(self.row, h)
        return self.seen[k]

    def nid(self):
        self.ids += 1
        return str(self.ids)

    def image(self):
        h = (self.y + self.row + 31) // 32 * 32
        img = [[[0, 0, 0, 0] for _ in range(self.w)] for _ in range(h)]
        for (ox, oy), w, rh, paint in self.rects:
            for j in range(rh):
                for i in range(w):
                    img[oy + j][ox + i] = paint(i, j, w, rh)
        return self.w, h, img


ID_Q = (0, 0, 0, 1)
FACE_LIGHT = {"front": 1.0, "back": 1.0, "left": 0.86, "right": 0.86, "top": 1.1, "bottom": 0.62}


def vec(v):
    return {"x": v[0], "y": v[1], "z": v[2]}


def quat(q):
    return {"x": round(q[0], 6), "y": round(q[1], 6), "z": round(q[2], 6), "w": round(q[3], 6)}


def axis(ax, deg):
    h = math.radians(deg) / 2
    return (ax[0] * math.sin(h), ax[1] * math.sin(h), ax[2] * math.sin(h), math.cos(h))


def uv(pos):
    return {"offset": {"x": pos[0], "y": pos[1]}, "mirror": {"x": False, "y": False}, "angle": 0}


def box(atlas, name, center, size, paint, q=ID_Q, px=1, back=None):
    """Tailles en unites ; back = autre peinture pour la face arriere (les battants, dont l'image se retourne)."""
    sx, sy, sz = [int(round(v * px)) for v in size]
    dims = {"front": (sx, sy), "back": (sx, sy), "left": (sz, sy), "right": (sz, sy), "top": (sx, sz), "bottom": (sx, sz)}
    layout = {}
    for f, (w, h) in dims.items():
        p, k = (back if f == "back" and back else paint), FACE_LIGHT[f]
        lit = lambda i, j, fw, fh, p=p, k=k: [min(255, int(c * k)) for c in p(i, j, fw, fh)[:3]] + [255]
        layout[f] = uv(atlas.put(w, h, lit, key=(id(p), k)))
    return {"id": atlas.nid(), "name": name, "position": vec(center), "orientation": quat(q),
            "shape": {"type": "box", "offset": vec((0, 0, 0)), "stretch": vec((1.0 / px, 1.0 / px, 1.0 / px)), "settings": {"size": vec((sx, sy, sz))},
                      "textureLayout": layout, "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": "flat"},
            "children": []}


def group(atlas, name, pos, children):
    return {"id": atlas.nid(), "name": name, "position": vec(pos), "orientation": quat(ID_Q),
            "shape": {"type": "none", "offset": vec((0, 0, 0)), "stretch": vec((1, 1, 1)), "settings": {}, "textureLayout": {},
                      "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": "flat"},
            "children": list(children)}


# ---------------------------------------------------------------------------------------------------------------------
# Peintures
# ---------------------------------------------------------------------------------------------------------------------
def noise(i, j, amp):
    return ((i * 73856093 ^ j * 19349663) >> 3) % (2 * amp + 1) - amp


def tint(c, k):
    return [max(0, min(255, int(v * k))) for v in c] + [255]


def bevel(i, j, w, h):
    return 1.14 if i == 0 or j == 0 else 0.7 if i == w - 1 or j == h - 1 else 1.0


def steel(i, j, w, h):
    n = noise(i, j, 4)
    return [STEEL_DARK[0] + 12 + n, STEEL_DARK[1] + 12 + n, STEEL_DARK[2] + 15 + n, 255] if (i + j) % 7 else tint(STEEL, 0.92)


def iron(i, j, w, h):
    n = noise(i, j, 5)
    return tint((STEEL[0] + n, STEEL[1] + n, STEEL[2] + n), bevel(i, j, w, h))


def gold(i, j, w, h):
    k = 1.0 + 0.16 * math.sin((i + j * 2) * 0.55) + noise(i, j, 3) / 120.0
    return tint(GOLD, k * bevel(i, j, w, h))


def stone(i, j, w, h):
    """Pierre sombre taillee : assises de 28 unites, joints decales d'une assise a l'autre."""
    row = j // 28
    joint = j % 28 == 0 or (i + (row % 2) * 13) % 26 == 0
    n = noise(i // 2, j // 2, 7) + (row * 13 + (i + (row % 2) * 13) // 26 * 7) % 9
    return [34, 33, 38, 255] if joint else [78 + n, 76 + n, 84 + n, 255]


def keystone(i, j, w, h):
    """Clef de voute : la couleur de la famille sertie d'or."""
    edge = min(i, j, w - 1 - i, h - 1 - j)
    if edge < 3:
        return tint(GOLD, bevel(i, j, w, h))
    return tint(FAM_COLOR[0], 1.0 + 0.2 * math.sin(i * 0.3) * math.sin(j * 0.3))


# ---------------------------------------------------------------------------------------------------------------------
# Blasons (u vers la droite, v vers le bas, 1 = demi-largeur de l'ecu)
# ---------------------------------------------------------------------------------------------------------------------
def leaf_image(color, kind):
    """Face d'un battant, pixel (0, 0) en haut, cote gonds : acier rive, deux panneaux emailles a la couleur de la famille, blason en haut."""
    W, H = LEAF_W * LEAF_PX, LEAF_H * LEAF_PX
    panels = ((16, 30, W - 12, 200), (16, 262, W - 12, H - 30))     # gauche, haut, droite, bas
    img = []
    for j in range(H):
        row = []
        for i in range(W):
            n = noise(i // 2, j // 2, 4)
            c = [STEEL[0] + n, STEEL[1] + n, STEEL[2] + n]
            if abs((j - 4) % 44 - 22) < 1 or i < 2:         # joints des plaques
                c = list(STEEL_DARK)
            for x0, y0, x1, y1 in panels:
                inside = min(i - x0, x1 - 1 - i, j - y0, y1 - 1 - j)
                if inside >= 4:
                    u, v = i - x0, j - y0
                    lattice = (u + v) % 40 < 1 or (u - v) % 40 < 1
                    glow = 1.0 + 0.1 * math.sin(u * 0.05 + v * 0.04) + noise(i, j, 2) / 90.0 - 0.25 * max(0, 8 - inside) / 8.0
                    c = tint(color, glow * (1.28 if lattice else 1.0))[:3]
                elif inside >= 1:
                    c = tint(GOLD, 0.8 + 0.1 * inside)[:3]
                elif inside == 0:
                    c = list(LINE)
            row.append([int(c[0]), int(c[1]), int(c[2]), 255])
        img.append(row)
    for j in range(8, H - 4, 22):                           # rivets
        for x in (7, W - 6):
            for dj in range(-3, 4):
                for di in range(-3, 4):
                    if di * di + dj * dj <= 9:
                        k = 1.5 if di + dj < -1 else 0.8 if di + dj > 1 else 1.1
                        img[j + dj][x + di] = tint(STEEL, k)
    shield = arms(color, kind, 96)
    ox, oy = (W + 4 - len(shield[0])) // 2, 115 - len(shield) // 2
    for y, row in enumerate(shield):
        for x, p in enumerate(row):
            if p[3] > 8:
                a = p[3] / 255.0
                img[oy + y][ox + x] = [int(img[oy + y][ox + x][c] * (1 - a) + p[c] * a) for c in range(3)] + [255]
    return img


FAM_COLOR = [FAMILIES[0][2]]    # couleur peinte par keystone, changee famille par famille
LEAF = [[[]]]                   # image du battant de la famille en cours


def leaf_piece(n, mirrored):
    """Peinture du morceau n du battant (0 = le plus haut). Les faces de tranche sont d'acier sombre."""
    top, width, height = n * SEGMENT_H * LEAF_PX, LEAF_W * LEAF_PX, SEGMENT_H * LEAF_PX

    def paint(i, j, w, h):
        if w != width or h != height:
            return tint(STEEL_DARK, 1.0)
        return LEAF[0][top + j][w - 1 - i if mirrored else i]
    return paint


LEAF_PIECES = [(leaf_piece(n, False), leaf_piece(n, True)) for n in range(LEAF_SEGMENTS)]


# ---------------------------------------------------------------------------------------------------------------------
# Modele
# ---------------------------------------------------------------------------------------------------------------------
def circle(a, name, radius, depth_z, seg, thick, count, paint, turn=0.0):
    """Anneau de petits segments dans le plan de la porte, a la cote depth_z."""
    nodes = []
    for k in range(count):
        deg = turn + k * 360.0 / count
        rad = math.radians(deg)
        nodes.append(box(a, name, (radius * math.cos(rad), radius * math.sin(rad), depth_z), (thick, seg, 3), paint, axis((0, 0, 1), deg)))
    return nodes


def wheel(a):
    """Volant du coffre, sur les deux faces. Tourne autour de l'axe de la porte avant l'ouverture."""
    parts = []
    for side in (1, -1):
        z = side * 12
        parts += circle(a, "Volant", 24, z, 20, 4, 8, gold)
        for deg in (0, 90):
            parts.append(box(a, "Rayon", (0, 0, z), (44, 5, 3), iron, axis((0, 0, 1), deg)))
        parts.append(box(a, "Moyeu", (0, 0, side * 13), (13, 13, 4), gold, axis((0, 0, 1), 45)))
    return group(a, "Roue", (LEAF_W, LEAF_H / 2.0, 0), parts)


def leaf(a, name, s):
    """Battant, de la charniere (x = 0) vers le centre (s = +1 a gauche, -1 a droite)."""
    kids = []
    for n, (plain, mirrored) in enumerate(LEAF_PIECES):
        face, back = (plain, mirrored) if s > 0 else (mirrored, plain)
        y = LEAF_H - (n + 0.5) * SEGMENT_H
        kids.append(box(a, "Vantail", (s * LEAF_W / 2.0, y, 0), (LEAF_W, SEGMENT_H, LEAF_T), face, px=LEAF_PX, back=back))
    for y in (7, LEAF_H / 2.0, LEAF_H - 7):
        kids.append(box(a, "Gond", (s * 1, min(max(y, 15), LEAF_H - 15), 0), (10, 30, 15), iron))
        for z in (6, -6):
            kids.append(box(a, "Penture", (s * 18, y, z), (36, 9, 3), iron))
            kids.append(box(a, "Fer", (s * 38, y, z), (8, 8, 3), gold, axis((0, 0, 1), 45)))
    if s > 0:                                                               # platine du coffre : suit le battant gauche
        center = (LEAF_W, LEAF_H / 2.0, 0)
        for z in (7, -7):
            for deg in (0, 18, 36, 54, 72):
                kids.append(box(a, "Platine", (center[0], center[1], z), (48, 48, 3), iron, axis((0, 0, 1), deg)))
        kids.append(wheel(a))
    return group(a, name, (-s * LEAF_W, 0, 0), kids)


def model():
    a = Atlas(1024)
    post_h = (HEIGHT - LINTEL) / 2.0
    frame = [box(a, "Montant", (s * (WIDTH - POST) / 2.0, post_h * (n + 0.5), 0), (POST, post_h, FRAME_DEPTH), stone) for s in (-1, 1) for n in (0, 1)]
    frame += [box(a, "Linteau", (s * WIDTH / 4.0, HEIGHT - LINTEL / 2.0, 0), (WIDTH / 2.0, LINTEL, FRAME_DEPTH), stone) for s in (-1, 1)]
    frame.append(box(a, "Clef", (0, HEIGHT - 11, 0), (26, 22, FRAME_DEPTH + 4), keystone))
    frame.append(leaf(a, "Door", 1))
    frame.append(leaf(a, "Door2", -1))
    return {"nodes": [group(a, "Porte", (0, 0, 0), frame)], "lod": "auto"}, a


# Noeuds des poignees : les animations vanilla attendent un 2 sur le second battant.
ANIMATED_FITTINGS = ("Door-Knob", "Door-Knocker-Front", "Door-Knocker-Back")


def mirror_node(node):
    """Symetrique d'un noeud par rapport au plan de la porte : x oppose, stretch x negatif (texture retournee)."""
    node["position"]["x"] *= -1
    o = node["orientation"]
    o["y"], o["z"] = -o["y"], -o["z"]
    shape = node.get("shape") or {}
    if shape.get("offset"):
        shape["offset"]["x"] *= -1
    if shape.get("stretch"):
        shape["stretch"]["x"] *= -1
    if node["name"] in ANIMATED_FITTINGS:
        node["name"] += "2"
    for child in node.get("children", []):
        mirror_node(child)


def renumber(node, counter):
    counter[0] += 1
    node["id"] = str(counter[0])
    for child in node.get("children", []):
        renumber(child, counter)


def double_door_model(vanilla_model):
    """Grande porte vanilla (un battant Door, charniere a gauche) + son miroir Door2 : les animations vanilla pilotent deja les deux."""
    tree = json.loads(ZIP.read("Common/" + vanilla_model))
    root = tree["nodes"][0]
    left = next(c for c in root["children"] if c["name"] == "Door")
    right = copy.deepcopy(left)
    mirror_node(right)
    right["name"] = "Door2"
    left["position"]["x"] = BOSS_HINGE_LEFT - root["position"]["x"]
    right["position"]["x"] = BOSS_HINGE_RIGHT - root["position"]["x"]
    root["children"].append(right)
    renumber(root, [0])
    return tree


# ---------------------------------------------------------------------------------------------------------------------
# Animations : 60 images par seconde. Les ouvertures vanilla durent 1/3 s ; ici 2 s, le volant d'abord.
# ---------------------------------------------------------------------------------------------------------------------
SPIN = 45           # le volant tourne pendant 45 images
SWING = 65          # les battants pivotent pendant 65 images
SPIN_TURNS = 2


def key(t, q):
    return {"time": int(t), "delta": quat(q), "interpolationType": "smooth"}


def track(keys):
    return {"position": [], "orientation": keys, "shapeStretch": [], "shapeVisible": [], "shapeUvOffset": []}


def spin(t0, sign):
    """Tours complets du volant, un repere par quart de tour : l'interpolation prendrait le chemin court sinon."""
    steps = SPIN_TURNS * 4
    return [key(t0 + SPIN * n / float(steps), axis((0, 0, 1), sign * 90 * n)) for n in range(steps + 1)]


def swing(t0, deg, closing):
    a, b = (deg, 0) if closing else (0, deg)
    return [key(t0, axis((0, 1, 0), a)), key(t0 + SWING / 2.0, axis((0, 1, 0), (a + b) / 2.0)), key(t0 + SWING, axis((0, 1, 0), b))]


def door_anim(deg, closing):
    """deg : rotation du battant gauche une fois ouvert (Door2 a l'oppose)."""
    last = SPIN + SWING
    if closing:
        tracks = {"Door": swing(0, deg, True), "Door2": swing(0, -deg, True), "Roue": spin(SWING, -1)}
    else:
        tracks = {"Door": [key(0, ID_Q)] + swing(SPIN, deg, False), "Door2": [key(0, ID_Q)] + swing(SPIN, -deg, False), "Roue": spin(0, 1)}
    return {"formatVersion": 1, "duration": last + 5, "holdLastKeyframe": True, "nodeAnimations": {n: track(k) for n, k in tracks.items()}}


# ---------------------------------------------------------------------------------------------------------------------
# Hitboxes, en blocs depuis le coin du bloc pose (qui occupe 0..1)
# ---------------------------------------------------------------------------------------------------------------------
def hitbox_boxes(*boxes):
    return {"Boxes": [{"Min": {"X": b[0], "Y": b[1], "Z": b[2]}, "Max": {"X": b[3], "Y": b[4], "Z": b[5]}} for b in boxes]}


def hitboxes():
    """Fermee : toute l'ouverture. Ouverte : battants plaques contre les montants, passage libre de 4 blocs."""
    folder = os.path.join(SERVER, "Item", "Block", "Hitboxes", "Furniture", "Porte_Tresor")
    top = (HEIGHT - LINTEL) / 32.0
    write_json(os.path.join(folder, HITBOX + ".json"), hitbox_boxes((-2, 0, 0.1, 3, 7, 0.9)))
    reach = 0.5 + LEAF_W / 32.0 + 0.05
    for name, z0, z1 in (("In", 0.1, reach), ("Out", 1 - reach, 0.9)):
        boxes = ((-2, 0, z0, -1.5, 7, z1), (2.5, 0, z0, 3, 7, z1), (-2, top, 0.1, 3, 7, 0.9))
        write_json(os.path.join(folder, "%s_Open_%s.json" % (HITBOX, name)), hitbox_boxes(*boxes))
        write_json(os.path.join(folder, "%s_Open_%s_Interaction.json" % (HITBOX, name)), hitbox_boxes(*boxes))

    # Double porte de boss : 4 blocs sur 4, chaque battant ouvert fait 2 blocs de long contre le mur
    folder = os.path.join(SERVER, "Item", "Block", "Hitboxes", "Furniture", "Porte_Boss")
    write_json(os.path.join(folder, BOSS_HITBOX + ".json"), hitbox_boxes((-1, 0, 0.2, 3, 4, 0.8)))
    for name, z0, z1, wide0, wide1 in (("In", 0.5, 2.5, 0.2, 2.8), ("Out", -1.5, 0.5, -1.8, 0.8)):
        write_json(os.path.join(folder, "%s_Open_%s.json" % (BOSS_HITBOX, name)), hitbox_boxes((-1, 0, z0, -0.7, 4, z1), (2.7, 0, z0, 3, 4, z1)))
        write_json(os.path.join(folder, "%s_Open_%s_Interaction.json" % (BOSS_HITBOX, name)), hitbox_boxes((-1, 0, wide0, -0.4, 4, wide1), (2.4, 0, wide0, 3, 4, wide1)))


# ---------------------------------------------------------------------------------------------------------------------
# Objets
# ---------------------------------------------------------------------------------------------------------------------
def door_states():
    states = {"DoorBlocked": {}}
    for way in ("In", "Out"):
        states["CloseDoor" + way] = {"InteractionSoundEventId": "SFX_Door_Temple_Dark_Close", "CustomModelAnimation": ANIM % ("Close_" + way)}
        states["OpenDoor" + way] = {"SoundOcclusionOpacity": 0.0, "HitboxType": "%s_Open_%s" % (HITBOX, way),
                                    "InteractionHitboxType": "%s_Open_%s_Interaction" % (HITBOX, way),
                                    "InteractionSoundEventId": "SFX_Door_Temple_Dark_Open", "CustomModelAnimation": ANIM % ("Open_" + way),
                                    "InteractionHint": "server.interactionHints.closeDoor"}
    return states


def family_door(fid, name, color):
    """Pas de Gathering, comme le bedrock : la porte ne se casse pas au combat ni a l'outil. DoorLockBreakEvent couvre le reste."""
    item_id = "Eldanior_Porte_Famille_" + fid
    return item_id, {
        "TranslationProperties": {"Name": "Porte du Trésor - " + name,
                                  "Description": "Porte blindée de 5 blocs sur 7. Seul le Patriarche de la famille " + name + " l'ouvre, avec sa clé. Se pose au milieu du bas de l'ouverture."},
        "Icon": "Icons/ItemsGenerated/%s.png" % item_id,
        "Categories": [category(fid), "Furniture.Doors"],
        "PlayerAnimationsId": "Block",
        "Interactions": {"Primary": "Block_Primary", "Secondary": "Block_Secondary"},
        "IconProperties": {"Rotation": [22.5, 45, 22.5], "Scale": 0.2, "Translation": [0, -40]},
        "BlockType": {"BlockParticleSetId": "Stone", "CustomModel": MODEL, "CustomModelTexture": [{"Texture": "Blocks/Eldanior/Porte_Tresor_%s_Texture.png" % fid}],
                      "DrawType": "Model", "Opacity": "Transparent", "HitboxType": HITBOX, "Material": "Solid", "State": {"Definitions": door_states()}, "VariantRotation": "NESW",
                      "ParticleColor": "#%02x%02x%02x" % color, "BlockSoundSetId": "Stone", "PhysicalMaterialId": "Stone", "SoundOcclusionOpacity": 2.0,
                      "InteractionHint": "server.interactionHints.openDoor", "IsDoor": True, "Interactions": {"Use": "Door"}},
        "Scale": 0.2,
        "MaxStack": 1,
        "Quality": "Legendary",
        "Tags": {"Type": ["Furniture"], "Family": ["Eldanior"]},
        "ItemSoundSetId": "ISS_Blocks_Wood",
    }


def boss_door(tid, label, path, quality):
    item_id = "Eldanior_Porte_Boss_" + tid
    data = json.loads(ZIP.read("Server/Item/Items/%s.json" % path))
    block = data["BlockType"]
    tree = double_door_model(block["CustomModel"])
    block["CustomModel"] = "Blocks/Eldanior/Porte_Boss_%s.blockymodel" % tid
    block["HitboxType"] = BOSS_HITBOX
    block["Opacity"] = "Transparent"
    for gone in ("Gathering", "ConnectedBlockRuleSet"):
        block.pop(gone, None)
    for way in ("In", "Out"):
        state = block["State"]["Definitions"]["OpenDoor" + way]
        state["HitboxType"] = "%s_Open_%s" % (BOSS_HITBOX, way)
        state["InteractionHitboxType"] = "%s_Open_%s_Interaction" % (BOSS_HITBOX, way)
    data.pop("Set", None)
    data["Icon"] = "Icons/ItemsGenerated/%s.png" % item_id
    data["TranslationProperties"] = {"Name": "Porte de Boss - Rang " + label,
                                     "Description": "Double porte de 4 blocs sur 4. S'ouvre avec une Clé de Boss de rang " + label + ", consommée à l'ouverture : tout le groupe passe."}
    data["Categories"] = ["Eldanior_System.Dungeons", "Furniture.Doors"]
    data["Quality"] = quality
    return item_id, data, tree, block["CustomModelTexture"][0]["Texture"]


def key_item(name, description, texture, icon, quality, level, stack, color, cat="Eldanior_System.Dungeons"):
    return {"TranslationProperties": {"Name": name, "Description": description},
            "Categories": [cat], "ItemLevel": level, "Quality": quality,
            "Model": KEY_MODEL, "Texture": texture, "Icon": icon,
            "PlayerAnimationsId": "Item",
            "IconProperties": {"Scale": 0.5, "Rotation": [22.5, 45, 22.5], "Translation": [0, -15]},
            "Tags": {"Type": ["Ingredient"]}, "ItemSoundSetId": "ISS_Items_Ingots", "MaxStack": stack, "DropOnDeath": False,
            "Light": {"Color": "#%x%x%x" % tuple(c >> 4 for c in color)}}


def key_chest(tid, label):
    """Coffre qui donne une cle de boss : le coffre de donjon, sous un autre identifiant (table cle_boss_* dans LootTableConfig)."""
    item_id = "ChestTreasureCleBoss" + tid
    with open(os.path.join(SERVER, "Item", "Items", "Furniture", "Dungeon", "ChestTreasureDungeon.json"), encoding="utf-8") as f:
        data = json.loads(f.read().replace('"ChestTreasureDungeon"', '"%s"' % item_id))
    data.pop("Recipe", None)
    data["TranslationProperties"] = {"Name": "Coffre à Clé de Boss - Rang " + label}
    data["Categories"] = ["Eldanior_System.Dungeons"]
    return item_id, data


KEY_MODEL = "Items/Eldanior/Cles/Cle.blockymodel"
KEY_ROOT = "R-Attachment"                       # os de la main, comme les objets vanilla
KEY_COLORS = [(232, 182, 78), (196, 34, 44)]    # metal, gemme de la cle en cours


def key_metal(i, j, w, h):
    k = 1.0 + 0.14 * math.sin((i + j) * 0.9)
    return tint(KEY_COLORS[0], k * bevel(i, j, w, h))


def key_gem(i, j, w, h):
    return tint(KEY_COLORS[1], 1.5 if i + j < 2 else 0.72 if i + j > w + h - 4 else 1.05)


def key_model():
    """Cle tenue par la tige : anneau ajoure et gemme en haut, dents en bas. Hauteur de l'ordre d'une epee courte."""
    a = Atlas(64)
    ring = []
    for k in range(10):
        deg = k * 36.0
        ring.append(box(a, "Anneau", (7 * math.cos(math.radians(deg)), 22 + 7 * math.sin(math.radians(deg)), 0), (3, 5, 3), key_metal, axis((0, 0, 1), deg)))
    parts = ring + [box(a, "Gemme", (0, 22, 0), (6, 6, 4), key_gem, axis((0, 0, 1), 45)),
                    box(a, "Col", (0, 13, 0), (7, 2, 4), key_metal),
                    box(a, "Tige", (0, 2, 0), (3, 20, 3), key_metal),
                    box(a, "Dent", (4, -5, 0), (6, 3, 2), key_metal),
                    box(a, "Dent", (3, -1, 0), (4, 2, 2), key_metal),
                    box(a, "Bout", (0, -9, 0), (5, 2, 4), key_gem)]
    root = group(a, KEY_ROOT, (0, 0, 0), parts)
    root["settings"] = {"isPiece": True}
    return {"nodes": [root], "lod": "auto"}, a


def write_key(key_tree, key_atlas, item_id, data, metal, gem):
    KEY_COLORS[0], KEY_COLORS[1] = metal, gem
    write_json(os.path.join(SERVER, "Item", "Items", "Eldanior", "Cles", item_id + ".json"), data)
    w, h, tex = key_atlas.image()
    full = os.path.join(COMMON, data["Texture"])
    os.makedirs(os.path.dirname(full), exist_ok=True)
    png.write(full, w, h, tex)
    pose = {KEY_ROOT: axis((0, 0, 1), -40)}
    png.write(os.path.join(COMMON, data["Icon"]), 64, 64, render(key_tree, tex, 64, 0, 0, pose))


# ---------------------------------------------------------------------------------------------------------------------
# Rendu (icones, apercu)
# ---------------------------------------------------------------------------------------------------------------------
def qrot(q, v):
    x, y, z, w = q
    t = (2 * (y * v[2] - z * v[1]), 2 * (z * v[0] - x * v[2]), 2 * (x * v[1] - y * v[0]))
    return (v[0] + w * t[0] + y * t[2] - z * t[1], v[1] + w * t[1] + z * t[0] - x * t[2], v[2] + w * t[2] + x * t[1] - y * t[0])


def qmul(a, b):
    ax, ay, az, aw = a
    bx, by, bz, bw = b
    return (aw * bx + ax * bw + ay * bz - az * by, aw * by - ax * bz + ay * bw + az * bx,
            aw * bz + ax * by - ay * bx + az * bw, aw * bw - ax * bx - ay * by - az * bz)


def collect(node, at, q, pose, out):
    """Parcourt l'arbre ; pose : rotation animee par nom de noeud."""
    p, o = node["position"], node["orientation"]
    here = qrot(q, (p["x"], p["y"], p["z"]))
    here = (at[0] + here[0], at[1] + here[1], at[2] + here[2])
    rot = qmul(q, (o["x"], o["y"], o["z"], o["w"]))
    if node["name"] in pose:
        rot = qmul(rot, pose[node["name"]])
    s = node["shape"]
    offset = s.get("offset") or {"x": 0, "y": 0, "z": 0}
    center = qrot(rot, (offset["x"], offset["y"], offset["z"]))        # les enfants partent du centre de la forme
    here = (here[0] + center[0], here[1] + center[1], here[2] + center[2])
    if s["type"] == "box":
        sz, L, st = s["settings"]["size"], s["textureLayout"], s.get("stretch") or {"x": 1, "y": 1, "z": 1}
        kx, ky, kz = st["x"], st["y"], st["z"]
        hx, hy, hz = sz["x"] * kx / 2, sz["y"] * ky / 2, sz["z"] * kz / 2
        faces = {"front": (sz["x"], sz["y"], lambda u, v: (-hx + u * kx, hy - v * ky, hz)), "back": (sz["x"], sz["y"], lambda u, v: (hx - u * kx, hy - v * ky, -hz)),
                 "right": (sz["z"], sz["y"], lambda u, v: (hx, hy - v * ky, hz - u * kz)), "left": (sz["z"], sz["y"], lambda u, v: (-hx, hy - v * ky, -hz + u * kz)),
                 "top": (sz["x"], sz["z"], lambda u, v: (-hx + u * kx, hy, -hz + v * kz)), "bottom": (sz["x"], sz["z"], lambda u, v: (-hx + u * kx, -hy, hz - v * kz))}
        for name, (w, h, fn) in faces.items():
            if name not in L:
                continue
            def world(u, v, fn=fn, rot=rot, here=here):
                r = qrot(rot, fn(u, v))
                return (here[0] + r[0], here[1] + r[1], here[2] + r[2])
            out.append((L[name], w, h, world))
    for c in node["children"]:
        collect(c, here, rot, pose, out)


def render(root, tex, size, yaw_deg, pitch_deg, pose=None):
    faces = []
    for n in root["nodes"]:
        collect(n, (0, 0, 0), ID_Q, pose or {}, faces)
    yaw, pitch = math.radians(yaw_deg), math.radians(pitch_deg)
    cy, sy, cp, sp = math.cos(yaw), math.sin(yaw), math.cos(pitch), math.sin(pitch)

    def project(v):
        x, y, z = v
        rx, rz = x * cy + z * sy, -x * sy + z * cy
        return rx, y * cp - rz * sp, y * sp + rz * cp

    pts = [project(f[3](u, v)) for f in faces for u, v in ((0, 0), (f[1], 0), (0, f[2]), (f[1], f[2]))]
    x0, x1 = min(p[0] for p in pts), max(p[0] for p in pts)
    y0, y1 = min(p[1] for p in pts), max(p[1] for p in pts)
    k = 0.94 * size / max(x1 - x0, y1 - y0)
    ox, oy = size / 2.0 - k * (x0 + x1) / 2, size / 2.0 + k * (y0 + y1) / 2
    img = [[[0, 0, 0, 0] for _ in range(size)] for _ in range(size)]
    zb = [[-1e9] * size for _ in range(size)]
    for layout, w, h, at in faces:
        tx0, ty0 = layout["offset"]["x"], layout["offset"]["y"]
        ustep = max(0.34, min(0.9, 0.8 / (k * max(1e-6, (abs(at(1, 0)[0] - at(0, 0)[0]) + abs(at(1, 0)[1] - at(0, 0)[1]) + abs(at(1, 0)[2] - at(0, 0)[2]))))))
        vstep = max(0.34, min(0.9, 0.8 / (k * max(1e-6, (abs(at(0, 1)[0] - at(0, 0)[0]) + abs(at(0, 1)[1] - at(0, 0)[1]) + abs(at(0, 1)[2] - at(0, 0)[2]))))))
        u = 0.0
        while u < w:
            v = 0.0
            while v < h:
                px, py, d = project(at(u, v))
                sx, sy2 = int(ox + k * px), int(oy - k * py)
                if 0 <= sx < size and 0 <= sy2 < size and d > zb[sy2][sx]:
                    col = tex[(ty0 + int(v)) % len(tex)][(tx0 + int(u)) % len(tex[0])]
                    if col[3]:
                        zb[sy2][sx] = d
                        img[sy2][sx] = list(col)
                v += vstep
            u += ustep
    return img


# ---------------------------------------------------------------------------------------------------------------------
def main():
    hitboxes()
    tree, atlas = model()
    write_json(os.path.join(COMMON, MODEL), tree)
    for way, deg in (("In", -90), ("Out", 90)):
        write_json(os.path.join(COMMON, ANIM % ("Open_" + way)), door_anim(deg, False))
        write_json(os.path.join(COMMON, ANIM % ("Close_" + way)), door_anim(deg, True))

    key_tree, key_atlas = key_model()
    write_json(os.path.join(COMMON, KEY_MODEL), key_tree)

    for fid, name, color, kind in FAMILIES:
        FAM_COLOR[0] = color
        LEAF[0] = leaf_image(color, kind)
        w, h, tex = atlas.image()
        png.write(os.path.join(COMMON, "Blocks", "Eldanior", "Porte_Tresor_%s_Texture.png" % fid), w, h, tex)
        item_id, data = family_door(fid, name, color)
        write_json(os.path.join(SERVER, "Item", "Items", "Furniture", "Eldanior", "Portes", item_id + ".json"), data)
        icon = render(tree, tex, 64, 0, 0)
        png.write(os.path.join(COMMON, "Icons", "ItemsGenerated", item_id + ".png"), 64, 64, icon)

        key_id = "Eldanior_Cle_Famille_" + fid
        tex_path, icon_path = "Items/Eldanior/Cles/%s_Texture.png" % key_id, "Icons/ItemsGenerated/%s.png" % key_id
        write_key(key_tree, key_atlas, key_id, key_item("Clé du Trésor - " + name,
                                                        "Clé de la salle au trésor de la famille " + name + ". Elle n'obéit qu'au Patriarche et lui revient toujours.",
                                                        tex_path, icon_path, "Legendary", 300, 1, color, category(fid)), GOLD, color)
        if PREVIEW:
            os.makedirs(PREVIEW, exist_ok=True)
            png.write(os.path.join(PREVIEW, "porte_%s.png" % fid), 512, 512, render(tree, tex, 512, 0, 0))
            if fid == "Runkandel":
                open_pose = {"Door": axis((0, 1, 0), -90), "Door2": axis((0, 1, 0), 90)}
                png.write(os.path.join(PREVIEW, "porte_ouverte.png"), 512, 512, render(tree, tex, 512, -35, 14, open_pose))
                png.write(os.path.join(PREVIEW, "porte_biais.png"), 512, 512, render(tree, tex, 512, 35, 10))

    for tid, label, color, path, quality, level in BOSS_TIERS:
        item_id, data, door_tree, texture = boss_door(tid, label, path, quality)
        write_json(os.path.join(SERVER, "Item", "Items", "Furniture", "Eldanior", "Portes", item_id + ".json"), data)
        write_json(os.path.join(COMMON, data["BlockType"]["CustomModel"]), door_tree)
        _, _, door_tex = vanilla("Common/" + texture)
        png.write(os.path.join(COMMON, data["Icon"]), 64, 64, render(door_tree, door_tex, 64, 0, 0))
        if PREVIEW:
            png.write(os.path.join(PREVIEW, "boss_%s.png" % tid), 384, 384, render(door_tree, door_tex, 384, 0, 0))
            if tid == "S":
                png.write(os.path.join(PREVIEW, "boss_ouverte.png"), 384, 384, render(door_tree, door_tex, 384, -35, 14, {"Door": axis((0, 1, 0), -90), "Door2": axis((0, 1, 0), 90)}))
        chest_id, chest = key_chest(tid, label)
        write_json(os.path.join(SERVER, "Item", "Items", "Furniture", "Dungeon", chest_id + ".json"), chest)
        key_id = "Eldanior_Cle_Boss_" + tid
        tex_path, icon_path = "Items/Eldanior/Cles/%s_Texture.png" % key_id, "Icons/ItemsGenerated/%s.png" % key_id
        write_key(key_tree, key_atlas, key_id, key_item("Clé de Boss - Rang " + label,
                                                        "Ouvre une Porte de Boss de rang " + label + ". Consommée à l'ouverture.",
                                                        tex_path, icon_path, quality, level, 8, color), color, (46, 26, 40))


main()
