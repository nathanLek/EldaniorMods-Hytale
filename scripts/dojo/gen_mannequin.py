"""Mannequin d'entrainement du Dojo (2026-11-14, demande de l'utilisateur : « un mannequin d'entrainement pour le dojo, les
disciples s'entrainent dessus, les joueurs testent leurs attaques, les coups donnent de l'energie de signature, style un peu jap »).

Deux pieces :
- le bloc pose par le joueur : un socle de granit a chapeau de cedre laque et douille de fer (32 unites = 1 bloc, 2 px/unite).
  C'est l'objet du joueur ; le casser retire le mannequin (Mannequins.java) ;
- le mannequin lui-meme, une entite PNJ immobile (role Eldanior_Mannequin_Dojo) debout sur le socle, pour que les armes et les
  runes le touchent comme une vraie cible (64 unites = 1 bloc, 1 px/unite) : poteau de cedre, corps de paille liee (makiwara),
  shimenawa a la taille avec ses shide de papier, bras en croix aux poings de paille, tete de paille a chignon et hachimaki
  blanc au soleil rouge, rubans dans le dos, ofuda (talisman de papier) sur la poitrine.

Animations : Idle (les rubans du hachimaki flottent), Hurt (le mannequin plie sous le coup et revient en oscillant ; jouee par
le moteur a chaque coup recu).

Usage : python3 scripts/dojo/gen_mannequin.py <src/main/resources> [dossier_apercu]
"""
import json, math, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
for folder in ("rune_aura_icons", "armures"):
    sys.path.insert(0, os.path.join(HERE, "..", folder))
import png  # noqa: E402
from familles import category  # noqa: E402

RES = sys.argv[1]
PREVIEW = sys.argv[2] if len(sys.argv) > 2 else None
COMMON = os.path.join(RES, "Common")
SERVER = os.path.join(RES, "Server")
ID = "Eldanior_Mannequin_Dojo"          # bloc, objet, role et modele portent le meme nom (lu par Mannequins.java)
FLOOR = 6                               # dessus du socle en unites de bloc (6/32) : Mannequins.FLOOR
NPC_DIR = "NPC/Eldanior/Dojo"           # un modele d'entite doit vivre sous Characters/, NPC/, Items/ ou VFX/


# ---------------------------------------------------------------------------------------------------------------------
# Atlas et noeuds (meme ecriture que gen_presentoir.py)
# ---------------------------------------------------------------------------------------------------------------------
class Atlas:
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


def uv(pos):
    return {"offset": {"x": pos[0], "y": pos[1]}, "mirror": {"x": False, "y": False}, "angle": 0}


def box(atlas, name, center, size, paint, px, q=ID_Q, offset=(0, 0, 0), faces=None):
    """Tailles en unites, px pixels de texture par unite. faces : peinture propre a certaines faces ({"front": f})."""
    sx, sy, sz = [int(round(v * px)) for v in size]
    dims = {"front": (sx, sy), "back": (sx, sy), "left": (sz, sy), "right": (sz, sy), "top": (sx, sz), "bottom": (sx, sz)}
    layout = {}
    for f, (w, h) in dims.items():
        p = (faces or {}).get(f, paint)
        k = FACE_LIGHT[f]
        shaded = lambda i, j, fw, fh, k=k, p=p: [min(255, int(c * k)) for c in p(i, j, fw, fh)[:3]] + [255]
        layout[f] = uv(atlas.put(w, h, shaded, key=(id(p), k)))
    return {"id": atlas.nid(), "name": name, "position": vec(center), "orientation": quat(q),
            "shape": {"type": "box", "offset": vec(offset), "stretch": vec((1.0 / px,) * 3), "settings": {"size": vec((sx, sy, sz))},
                      "textureLayout": layout, "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": "flat"},
            "children": []}


def quad(atlas, name, center, wpx, hpx, paint, px, q=ID_Q):
    """Image a plat, face vers +Z, visible des deux cotes."""
    return {"id": atlas.nid(), "name": name, "position": vec(center), "orientation": quat(q),
            "shape": {"type": "quad", "offset": vec((0, 0, 0)), "stretch": vec((1.0 / px, 1.0 / px, 1)), "settings": {"size": {"x": wpx, "y": hpx}, "normal": "+Z"},
                      "textureLayout": {"front": uv(atlas.put(wpx, hpx, paint))}, "unwrapMode": "custom", "visible": True, "doubleSided": True,
                      "shadingMode": "flat"},
            "children": []}


def group(atlas, name, pos, children):
    return {"id": atlas.nid(), "name": name, "position": vec(pos), "orientation": quat(ID_Q),
            "shape": {"type": "none", "offset": vec((0, 0, 0)), "stretch": vec((1, 1, 1)), "settings": {}, "textureLayout": {},
                      "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": "flat"},
            "children": list(children)}


# ---------------------------------------------------------------------------------------------------------------------
# Peintures
# ---------------------------------------------------------------------------------------------------------------------
def clamp(c):
    return [max(0, min(255, int(v))) for v in c]


def noise(i, j, amp):
    return ((i * 73856093 ^ j * 19349663) >> 3) % (2 * amp + 1) - amp


def tint(c, k):
    return clamp([v * k for v in c])


CEDAR = (92, 58, 36)
STRAW = (214, 178, 92)
ROPE = (118, 82, 46)
SACRED = (232, 214, 160)
PAPER = (244, 240, 228)
RED = (186, 36, 40)
IRON = (62, 62, 66)
GRANITE = (128, 126, 122)
LACQUER = (58, 30, 24)


def cedar(i, j, w, h):
    """Bois de cedre sombre, veine verticale ondulee."""
    g = math.sin((i + 0.6 * math.sin(j * 0.21)) * 1.3) * 7
    return clamp([CEDAR[0] + g + noise(i, j, 4), CEDAR[1] + g * 0.7 + noise(i, j, 3), CEDAR[2] + g * 0.5 + noise(i, j, 2)])


def straw(i, j, w, h):
    """Paille en brins verticaux : alternance claire et foncee, quelques brins sombres, extremites plus pales."""
    strand = (i * 7 + (j // 9) * 3) % 5
    k = (1.08, 0.96, 1.0, 0.86, 1.04)[strand] + noise(i, j // 3, 3) / 100.0
    if (i * 13 + j // 6) % 23 == 0:
        k *= 0.72
    return tint(STRAW, k)


def straw_end(i, j, w, h):
    """Bout coupe d'une botte de paille : petits cercles de tiges."""
    k = 0.8 if (i % 3 == 1 and j % 3 == 1) else 1.0 + noise(i, j, 6) / 100.0
    return tint(STRAW, k * 0.95)


def rope(i, j, w, h):
    """Corde torsadee : stries diagonales."""
    k = 1.12 if (i + j) % 4 < 2 else 0.8
    return tint(ROPE, k + noise(i, j, 3) / 100.0)


def sacred_rope(i, j, w, h):
    """Shimenawa : grosse corde de paille pale, torsade large."""
    t = (i + j * 1.5) % 8
    k = 1.08 if t < 4 else 0.86
    return tint(SACRED, k + noise(i, j, 3) / 100.0)


def paper(i, j, w, h):
    return clamp([PAPER[0] + noise(i, j, 3), PAPER[1] + noise(i, j, 3), PAPER[2] + noise(i, j, 3)])


def iron(i, j, w, h):
    edge = 1.25 if j == 0 or j == h - 1 else 1.0
    return tint(IRON, edge + noise(i, j, 5) / 100.0)


def band(i, j, w, h):
    """Hachimaki blanc, liseres gris."""
    k = 0.86 if j == 0 or j == h - 1 else 1.0
    return tint(PAPER, k + noise(i, j, 2) / 100.0)


def band_front(i, j, w, h):
    """Face avant du hachimaki : soleil rouge au centre."""
    cx, cy = (w - 1) / 2.0, (h - 1) / 2.0
    if (i - cx) ** 2 + ((j - cy) * 1.0) ** 2 <= 2.3 ** 2:
        return list(RED)
    return band(i, j, w, h)


def ofuda(i, j, w, h):
    """Talisman de papier : liseré rouge, traits d'encre noire et sceau rouge."""
    if i == 0 or i == w - 1 or j == 0 or j == h - 1:
        return list(RED)
    strokes = {(2, 2), (3, 2), (4, 2), (3, 3), (2, 4), (4, 4), (2, 6), (3, 6), (4, 6), (3, 7)}
    if (i, j) in strokes:
        return [30, 26, 24]
    if 2 <= i <= 4 and 8 <= j <= 9:
        return list(RED)
    return paper(i, j, w, h)


def granite(i, j, w, h):
    speck = 1.25 if (i * 31 + j * 17) % 13 == 0 else (0.7 if (i * 11 + j * 29) % 17 == 0 else 1.0)
    edge = 1.15 if j == 0 else (0.82 if j == h - 1 else 1.0)
    return tint(GRANITE, speck * edge + noise(i, j, 5) / 100.0)


def lacquer(i, j, w, h):
    edge = 1.3 if j == 0 or i == 0 or i == w - 1 else 1.0
    return tint(LACQUER, edge + noise(i, j, 3) / 100.0)


# ---------------------------------------------------------------------------------------------------------------------
# Modeles
# ---------------------------------------------------------------------------------------------------------------------
def dummy_model():
    """Mannequin : origine aux pieds, avant vers +Z. Tout pend du noeud Corps, qui plie depuis la base (Hurt)."""
    a = Atlas(256)
    P = 1
    parts = [
        box(a, "Poteau", (0, 56, 0), (8, 112, 8), cedar, P),
        box(a, "Bague", (0, 4, 0), (10, 8, 10), iron, P),
        box(a, "Paille", (0, 66, 0), (22, 52, 22), straw, P, faces={"top": straw_end, "bottom": straw_end}),
    ]
    for k, y in enumerate((50, 66, 82)):
        parts.append(box(a, "Corde_%d" % k, (0, y, 0), (23, 3, 23), rope, P))
    parts.append(box(a, "Shimenawa", (0, 42, 0), (26, 6, 26), sacred_rope, P))
    for side, z in (("Av", 13.5), ("Ar", -13.5)):                                       # shide en zigzag, devant et derriere
        for n, x in enumerate((-8, 8)):
            for s, (dx, dy) in enumerate(((0, 36), (2, 32), (0, 28))):
                parts.append(box(a, "Shide_%s%d_%d" % (side, n, s), (x + dx * (1 if x < 0 else -1), dy, z), (4, 4, 1), paper, P))
    parts += [                                                                          # bras en croix, poings de paille lies
        box(a, "Bras", (0, 84, 0), (64, 5, 5), cedar, P),
        box(a, "Poing_G", (-28, 84, 0), (10, 10, 10), straw, P, faces={"left": straw_end, "right": straw_end}),
        box(a, "Poing_D", (28, 84, 0), (10, 10, 10), straw, P, faces={"left": straw_end, "right": straw_end}),
        box(a, "Lien_G", (-23, 84, 0), (2, 11, 11), rope, P),
        box(a, "Lien_D", (23, 84, 0), (2, 11, 11), rope, P),
        box(a, "Tete", (0, 112, 0), (16, 16, 16), straw, P, faces={"top": straw_end}),
        box(a, "Chignon", (0, 122, -1), (5, 4, 5), straw, P),
        box(a, "Chignon_Lien", (0, 121, -1), (6, 1, 6), rope, P),
        box(a, "Hachimaki", (0, 114, 0), (17, 4, 17), band, P, faces={"front": band_front}),
        box(a, "Noeud", (0, 114, -9), (4, 4, 2), band, P),
        quad(a, "Ofuda", (0, 74, 11.7), 7, 12, ofuda, P),
    ]
    # Rubans : pivot au noeud (en haut), la bande pend en dessous (offset) ; animes en Idle et en Hurt
    for name, x, deg in (("Ruban_G", -1.5, 12), ("Ruban_D", 1.5, -12)):
        parts.append(box(a, name, (x, 113, -10), (3, 12, 1), band, P, q=axis((0, 0, 1), deg), offset=(0, -6, 0)))
    corps = group(a, "Corps", (0, 0, 0), parts)
    return a, group(a, "Origin", (0, 0, 0), [corps])


def plinth_model():
    """Socle (unites de bloc : 32 = 1 bloc, 2 px/unite), origine au centre bas du bloc."""
    a = Atlas(256)
    P = 2
    parts = [
        box(a, "Granit", (0, 2, 0), (30, 4, 30), granite, P),
        box(a, "Chapeau", (0, 5, 0), (24, 2, 24), lacquer, P),
        box(a, "Douille", (0, 7, 0), (10, 2, 10), iron, P),
    ]
    for sx in (-1, 1):                                                                  # clous de fer aux coins du chapeau
        for sz in (-1, 1):
            parts.append(box(a, "Clou_%d_%d" % (sx, sz), (sx * 9.5, 6.25, sz * 9.5), (2, 0.5, 2), iron, P))
    return a, group(a, "Origin", (0, 0, 0), parts)


# ---------------------------------------------------------------------------------------------------------------------
# Animations (60 images par seconde)
# ---------------------------------------------------------------------------------------------------------------------
def rot_keys(keys, ax):
    out = []
    for t, deg in keys:
        q = axis(ax, deg)
        out.append({"time": t, "delta": {"x": round(q[0], 6), "y": round(q[1], 6), "z": round(q[2], 6), "w": round(q[3], 6)},
                    "interpolationType": "smooth"})
    return out


def track(orientation):
    return {"position": [], "orientation": orientation, "shapeStretch": [], "shapeVisible": [], "shapeUvOffset": []}


def idle_anim():
    """Les rubans flottent doucement (3 s)."""
    return {"duration": 180, "holdLastKeyframe": False, "nodeAnimations": {
        "Ruban_G": track(rot_keys(((0, 0), (60, 14), (120, 4), (180, 0)), (1, 0, 0))),
        "Ruban_D": track(rot_keys(((0, 6), (50, 0), (110, 16), (180, 6)), (1, 0, 0))),
    }}


def hurt_anim():
    """Le corps plie en arriere sous le coup et revient en oscillant (0,7 s) ; les rubans fouettent."""
    return {"duration": 42, "holdLastKeyframe": False, "nodeAnimations": {
        "Corps": track(rot_keys(((0, 0), (5, -9), (14, 6), (23, -3.5), (32, 1.5), (42, 0)), (1, 0, 0))),
        "Ruban_G": track(rot_keys(((0, 0), (6, 40), (16, -10), (28, 12), (42, 0)), (1, 0, 0))),
        "Ruban_D": track(rot_keys(((0, 0), (7, 34), (18, -6), (30, 10), (42, 0)), (1, 0, 0))),
    }}


# ---------------------------------------------------------------------------------------------------------------------
# Assets serveur
# ---------------------------------------------------------------------------------------------------------------------
def item():
    return {
        "TranslationProperties": {
            "Name": "Mannequin d'entraînement",
            "Description": "Makiwara du Dojo, sur son socle de granit. Frappez-le pour éprouver vos attaques : il affiche les dégâts "
                           "reçus et chaque coup remplit l'énergie de signature. Les disciples d'un dojo proche viennent s'y exercer.",
        },
        "Icon": "Icons/ItemsGenerated/%s.png" % ID,
        "Categories": [category("ArtsMartiaux"), "Furniture.Furniture"],
        "Recipe": {
            "TimeSeconds": 4,
            "Input": [{"ResourceTypeId": "Wood_Trunk", "Quantity": 2}, {"ItemId": "Ingredient_Fibre", "Quantity": 6},
                      {"ResourceTypeId": "Rock", "Quantity": 4}],
            "BenchRequirement": [{"Type": "Crafting", "Categories": ["Furniture_Misc"], "Id": "Furniture_Bench"}],
        },
        "BlockType": {
            "Material": "Solid",
            "DrawType": "Model",
            "Opacity": "Transparent",
            "CustomModel": "Blocks/Eldanior/Mannequin_Dojo_Socle.blockymodel",
            "CustomModelTexture": [{"Texture": "Blocks/Eldanior/Mannequin_Dojo_Socle_Texture.png", "Weight": 1}],
            "HitboxType": ID,
            "Gathering": {"Breaking": {"GatherType": "Benches"}},
            "BlockParticleSetId": "Stone",
            "ParticleColor": "#807e7a",
            "BlockSoundSetId": "Stone",
            "PhysicalMaterialId": "Stone",
            "TextureComputedColor": "#807E7A",
            "Support": {"Down": [{"FaceType": "Full"}]},
        },
        "PlayerAnimationsId": "Block",
        "IconProperties": {"Scale": 0.3, "Translation": [0, -26], "Rotation": [22.5, 45, 22.5]},
        "MaxStack": 4,
        "ItemLevel": 10,
        "ItemSoundSetId": "ISS_Blocks_Wood",
        "Quality": "Uncommon",
    }


def hitbox():
    """Le socle seul : au-dessus, les coups vont au mannequin (entite), pas au bloc."""
    return {"Boxes": [{"Min": {"X": 0, "Y": 0, "Z": 0}, "Max": {"X": 1, "Y": FLOOR / 32.0, "Z": 1}}]}


def model_asset():
    base = NPC_DIR + "/Mannequin_Dojo"
    return {
        "Model": base + ".blockymodel",
        "Texture": base + "_Texture.png",
        "EyeHeight": 1.6,
        "HitBox": {"Min": {"X": -0.42, "Y": 0, "Z": -0.42}, "Max": {"X": 0.42, "Y": 1.95, "Z": 0.42}},
        "AnimationSets": {
            "Idle": {"Animations": [{"Animation": base + "_Idle.blockyanim", "Looping": True}]},
            "Hurt": {"Animations": [{"Animation": base + "_Hurt.blockyanim", "Looping": False, "BlendingDuration": 0.05}]},
        },
    }


def role():
    """Cible immobile : ni deplacement, ni recul, ni attaque. Vie enorme, remise au plein par le code (Mannequins)."""
    return {
        "$Comment": "Mannequin d'entrainement du Dojo : cible immobile (Mannequins.java : vie, plaque, energie de signature). "
                    "Genere par scripts/dojo/gen_mannequin.py.",
        "Type": "Generic",
        "Appearance": ID,
        "DropList": "Empty",
        "KnockbackScale": 0,
        "MotionControllerList": [{"Type": "Walk", "MaxWalkSpeed": 1, "Gravity": 10, "MaxFallSpeed": 8, "Acceleration": 10}],
        "MaxHealth": {"Compute": "MaxHealth"},
        "Parameters": {"MaxHealth": {"Value": 1000000, "Description": "Vie du mannequin (jamais atteinte : remise au plein a chaque coup)"}},
        "Instructions": [{"Instructions": [{"Sensor": {"Type": "Any"}, "BodyMotion": {"Type": "Nothing"}}]}],
        "NameTranslationKey": "server.npcRoles.%s.name" % ID,
    }


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


# ---------------------------------------------------------------------------------------------------------------------
# Icone et apercu : rendu orthographique (arbre de noeuds parcouru en entier)
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


def faces_of(node, at=(0, 0, 0), q=ID_Q, scale=1.0, out=None):
    """scale : unites du modele -> unites de rendu (le socle compte 32 par bloc, le mannequin 64)."""
    out = [] if out is None else out
    c, o = node["position"], node["orientation"]
    p = qrot(q, (c["x"] * scale, c["y"] * scale, c["z"] * scale))
    at2 = (at[0] + p[0], at[1] + p[1], at[2] + p[2])
    q2 = qmul(q, (o["x"], o["y"], o["z"], o["w"]))
    s = node["shape"]
    if s["type"] in ("box", "quad"):
        off = s["offset"]
        offw = (off["x"] * scale, off["y"] * scale, off["z"] * scale)
        L = s["textureLayout"]

        def world(v, at2=at2, q2=q2, offw=offw):
            r = qrot(q2, (v[0] + offw[0], v[1] + offw[1], v[2] + offw[2]))
            return (at2[0] + r[0], at2[1] + r[1], at2[2] + r[2])
        if s["type"] == "quad":
            wq, hq, k = s["settings"]["size"]["x"], s["settings"]["size"]["y"], s["stretch"]["x"] * scale
            out.append((L["front"], wq, hq, lambda u, v, wq=wq, hq=hq, k=k, world=world: world(((u - wq / 2.0) * k, (hq / 2.0 - v) * k, 0))))
        else:
            sz, st = s["settings"]["size"], s["stretch"]["x"] * scale
            hx, hy, hz = sz["x"] * st / 2, sz["y"] * st / 2, sz["z"] * st / 2

            def add(face, w, h, fn, world=world, st=st):
                out.append((L[face], w, h, lambda u, v, fn=fn: world(fn(u * st, v * st))))
            add("front", sz["x"], sz["y"], lambda u, v: (-hx + u, hy - v, hz))
            add("back", sz["x"], sz["y"], lambda u, v: (hx - u, hy - v, -hz))
            add("right", sz["z"], sz["y"], lambda u, v: (hx, hy - v, hz - u))
            add("left", sz["z"], sz["y"], lambda u, v: (-hx, hy - v, -hz + u))
            add("top", sz["x"], sz["z"], lambda u, v: (-hx + u, hy, -hz + v))
            add("bottom", sz["x"], sz["z"], lambda u, v: (-hx + u, -hy, hz - v))
    for ch in node["children"]:
        faces_of(ch, at2, q2, scale, out)
    return out


def render(scene, size, yaw_deg, pitch_deg):
    """scene : liste de (racine, texture, echelle, decalage) dessinees ensemble."""
    yaw, pitch = math.radians(yaw_deg), math.radians(pitch_deg)
    cy, sy, cp, sp = math.cos(yaw), math.sin(yaw), math.cos(pitch), math.sin(pitch)

    def project(v):
        x, y, z = v
        rx, rz = x * cy + z * sy, -x * sy + z * cy
        return rx, y * cp - rz * sp, y * sp + rz * cp

    faces = []
    for root, tex, scale, shift in scene:
        for layout, w, h, fn in faces_of(root, shift, ID_Q, scale):
            faces.append((layout, w, h, fn, tex))
    pts = [project(f[3](u, v)) for f in faces for u, v in ((0, 0), (f[1], 0), (0, f[2]), (f[1], f[2]))]
    x0, x1 = min(p[0] for p in pts), max(p[0] for p in pts)
    y0, y1 = min(p[1] for p in pts), max(p[1] for p in pts)
    k = 0.94 * size / max(x1 - x0, y1 - y0)
    ox, oy = size / 2.0 - k * (x0 + x1) / 2, size / 2.0 + k * (y0 + y1) / 2
    img = [[[0, 0, 0, 0] for _ in range(size)] for _ in range(size)]
    zb = [[-1e9] * size for _ in range(size)]
    for layout, w, h, at, tex in faces:
        tx0, ty0 = layout["offset"]["x"], layout["offset"]["y"]
        step = min(0.9, 1.0 / max(k, 1e-6) * 0.9)
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
                v += step
            u += step
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
def main():
    da, droot = dummy_model()
    dw, dh, dtex = da.image()
    npc_dir = os.path.join(COMMON, *NPC_DIR.split("/"))
    os.makedirs(npc_dir, exist_ok=True)
    png.write(os.path.join(npc_dir, "Mannequin_Dojo_Texture.png"), dw, dh, dtex)
    write_json(os.path.join(npc_dir, "Mannequin_Dojo.blockymodel"), {"nodes": [droot], "lod": "auto"})
    write_json(os.path.join(npc_dir, "Mannequin_Dojo_Idle.blockyanim"), idle_anim())
    write_json(os.path.join(npc_dir, "Mannequin_Dojo_Hurt.blockyanim"), hurt_anim())

    pa, proot = plinth_model()
    pw, ph, ptex = pa.image()
    block_dir = os.path.join(COMMON, "Blocks", "Eldanior")
    os.makedirs(block_dir, exist_ok=True)
    png.write(os.path.join(block_dir, "Mannequin_Dojo_Socle_Texture.png"), pw, ph, ptex)
    write_json(os.path.join(block_dir, "Mannequin_Dojo_Socle.blockymodel"), {"nodes": [proot], "lod": "auto"})

    write_json(os.path.join(SERVER, "Item", "Items", "Furniture", "Eldanior", ID + ".json"), item())
    write_json(os.path.join(SERVER, "Item", "Block", "Hitboxes", "Furniture", ID + ".json"), hitbox())
    write_json(os.path.join(SERVER, "Models", "Eldanior", "Dojo", ID + ".json"), model_asset())
    write_json(os.path.join(SERVER, "NPC", "Roles", "Eldanior", "Dojo", ID + ".json"), role())

    # Rendu commun : le socle (32 u/bloc) et le mannequin (64 u/bloc) ramenes a 64 unites par bloc, mannequin pose sur le socle
    scene = [(proot, ptex, 2.0, (0, 0, 0)), (droot, dtex, 1.0, (0, FLOOR * 2.0, 0))]
    icon = downscale(render(scene, 256, 35, 18), 4)
    png.write(os.path.join(COMMON, "Icons", "ItemsGenerated", ID + ".png"), 64, 64, icon)
    if PREVIEW:
        os.makedirs(PREVIEW, exist_ok=True)
        S = 384
        views = [render(scene, S, yaw, 12) for yaw in (20, 110, 200)]
        sheet = [sum((v[y] for v in views), []) for y in range(S)]
        png.write(os.path.join(PREVIEW, "mannequin_dojo.png"), S * 3, S, sheet)
    print("Mannequin du Dojo : modele %dx%d, socle %dx%d" % (dw, dh, pw, ph))


main()
