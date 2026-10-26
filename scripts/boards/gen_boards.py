"""Tableaux de prime et de guilde (boards/BoardType) : blocs-objets 3x3 poses au mur par un admin.

Modele : le tableau de taverne vanilla (Furniture_Tavern_Painting, TavernPainting.blockymodel) agrandi x3 a partir du plan
du mur (cadre : montants vanilla etires x3, memes coordonnees de texture), fond en liege a la place de la toile et six
affiches clouees, legerement de travers. Un seul modele, deux textures de meme disposition :
  - prime : avis de recherche (bandeau rouge, portrait, recompense), un avis "reclame" barre de rouge, contrat de monstre ;
  - guilde : avis aux blasons de guilde, classement (or, argent, bronze), contrat de guilde avec barre de progression.
Hitbox 3x3 (comme Painting_3x2 vanilla : X -1..2), icones dessinees depuis la vue de face.
Emblemes de la fenetre (Board/Board.ui) : losange de l'embleme de recrutement (scripts/npcs/gen_recruit_ui.py), avec une
piece d'or sous un viseur rouge (prime) ou un blason de guilde (guilde).
Icone d'oeil (bouton des lignes, BoardEye@2x.png) : blanche, teintee par la fenetre (Color).
Arches de la fenetre, deux fois plus larges que celles du recrutement : sommet (et pied) a pans coupes, memes bords que le
corps de la fenetre (sombre 3, or 7, sombre 2), fond degrade, filet dore interieur.

Usage : python3 gen_boards.py <src/main/resources> <Assets.zip du jeu>
"""
import json, math, os, random, sys, zipfile

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'rune_aura_icons'))
import png  # noqa: E402  (module PNG du projet : RGBA 8 bits)

RES, ASSETS = sys.argv[1], sys.argv[2]
VANILLA_MODEL = "Common/Blocks/Decorative_Sets/Tavern/TavernPainting.blockymodel"
VANILLA_TEXTURE = "Common/Blocks/Decorative_Sets/Tavern/TavernPainting_Texture.png"
DIR = "Blocks/Eldanior/Tableaux"
MODEL = f"{DIR}/Tableau.blockymodel"
SCALE, WALL_Z = 3, -16.0          # agrandissement, plan du mur (face nord du bloc)
TEX = 128                         # texture 128 x 128

# ===== Disposition de la texture : (x, y, largeur, hauteur) =====
REAR = (32, 0, 70, 70)            # fond en liege
POSTERS = [                       # zone de texture, centre (x, y) sur le tableau, angle (degres), profondeur
    ((0, 64, 20, 26), (-20, 62), -4, -11.95),
    ((0, 92, 20, 26), (1, 65), 3, -11.85),
    ((32, 72, 18, 22), (21, 61), -3, -11.95),
    ((72, 72, 24, 16), (-17, 31), 2, -11.90),
    ((52, 72, 18, 22), (6, 35), -5, -11.80),
    ((98, 72, 14, 12), (24, 30), 6, -11.85),
]
PIN = (114, 72, 4, 4)


def write(rel, data):
    path = os.path.join(RES, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")
    print("ecrit", rel)


with zipfile.ZipFile(ASSETS) as z:
    vanilla = json.loads(z.read(VANILLA_MODEL))
    tmp = os.path.join(os.path.dirname(os.path.abspath(__file__)), "_vanilla_tmp.png")
    with open(tmp, "wb") as f:
        f.write(z.read(VANILLA_TEXTURE))
    VW, VH, VIMG = png.read(tmp)
    os.remove(tmp)


# ===== Modele =====
def face(x, y):
    return {"offset": {"x": x, "y": y}, "mirror": {"x": False, "y": False}, "angle": 0}


def quad(nid, name, pos, size, uv, angle=0.0, double=False):
    a = math.radians(angle) / 2
    return {"id": nid, "name": name, "position": {"x": pos[0], "y": pos[1], "z": pos[2]},
            "orientation": {"w": round(math.cos(a), 6), "x": 0, "y": 0, "z": round(math.sin(a), 6)},
            "shape": {"offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": 1, "y": 1, "z": 1},
                      "textureLayout": {"front": face(uv[0], uv[1])}, "type": "quad",
                      "settings": {"size": {"x": size[0], "y": size[1]}, "normal": "+Z"},
                      "unwrapMode": "custom", "visible": True, "doubleSided": double, "shadingMode": "standard"},
            "children": []}


def pin(nid, pos):
    uv = face(PIN[0], PIN[1])
    return {"id": nid, "name": "Pin", "position": {"x": pos[0], "y": pos[1], "z": pos[2]},
            "orientation": {"w": 1, "x": 0, "y": 0, "z": 0},
            "shape": {"offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": 1, "y": 1, "z": 1},
                      "textureLayout": {k: uv for k in ("top", "bottom", "front", "back", "left", "right")},
                      "type": "box", "settings": {"size": {"x": 2, "y": 2, "z": 2}},
                      "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": "standard"},
            "children": []}


nodes = []
for n in vanilla["nodes"]:
    if n["shape"]["type"] != "box":
        continue                                                       # la toile : remplacee par le liege
    n = json.loads(json.dumps(n))
    p, off = n["position"], n["shape"]["offset"]
    x, y, zz = p["x"] + off["x"], p["y"] + off["y"], p["z"] + off["z"]  # montants vanilla sans rotation de decalage
    n["position"] = {"x": x * SCALE, "y": y * SCALE, "z": WALL_Z + (zz - WALL_Z) * SCALE}
    n["shape"]["offset"] = {"x": 0, "y": 0, "z": 0}
    n["shape"]["stretch"] = {k: v * SCALE for k, v in n["shape"]["stretch"].items()}
    nodes.append(n)
rear_z = WALL_Z + (-14.700531 - WALL_Z) * SCALE
nodes.append(quad("100", "Cork", (0, 48, round(rear_z, 4)), (REAR[2], REAR[3]), REAR, double=True))
for i, (uv, (cx, cy), ang, zz) in enumerate(POSTERS):
    nodes.append(quad(str(110 + i), f"Poster{i}", (cx, cy, zz), (uv[2], uv[3]), uv, ang))
    a = math.radians(ang)
    top = uv[3] / 2 - 2.5                                              # clou en haut, au milieu de l'affiche
    nodes.append(pin(str(130 + i), (round(cx - math.sin(a) * top, 3), round(cy + math.cos(a) * top, 3), -11)))
write(f"Common/{MODEL}", {"lod": "auto", "nodes": nodes})


# ===== Textures =====
def rgba(c, a=255):
    return [c[0], c[1], c[2], a]


def shade(c, d):
    return [max(0, min(255, v + d)) for v in c[:3]] + [255]


class Canvas:
    def __init__(self, rng):
        self.px = [[[0, 0, 0, 0] for _ in range(TEX)] for _ in range(TEX)]
        self.rng = rng

    def put(self, x, y, c):
        if 0 <= x < TEX and 0 <= y < TEX:
            self.px[y][x] = list(c)

    def rect(self, x, y, w, h, c, noise=0):
        for j in range(h):
            for i in range(w):
                self.put(x + i, y + j, shade(c, self.rng.randint(-noise, noise)) if noise else rgba(c))

    def line(self, x, y, w, c):
        for i in range(w):
            if self.rng.random() < 0.8:
                self.put(x + i, y, rgba(c))


PARCHMENT, INK, EDGE = (222, 201, 152), (74, 58, 44), (176, 146, 98)


def paper(cv, x, y, w, h, tint=PARCHMENT):
    """Feuille : papier bruite, bord fonce, coins uses."""
    cv.rect(x, y, w, h, tint, 9)
    for i in range(w):
        cv.put(x + i, y, shade(EDGE, cv.rng.randint(-8, 8)))
        cv.put(x + i, y + h - 1, shade(EDGE, cv.rng.randint(-8, 8)))
    for j in range(h):
        cv.put(x, y + j, shade(EDGE, cv.rng.randint(-8, 8)))
        cv.put(x + w - 1, y + j, shade(EDGE, cv.rng.randint(-8, 8)))
    for cx, cy in ((x, y), (x + w - 1, y), (x, y + h - 1), (x + w - 1, y + h - 1)):
        cv.put(cx, cy, rgba(shade(EDGE, -30)))


def text_lines(cv, x, y, w, rows, gap=2, color=INK):
    for r in range(rows):
        cv.line(x, y + r * gap, w - cv.rng.randint(0, w // 3), color)


def portrait(cv, x, y, w, h, bg, skin, cloth):
    cv.rect(x, y, w, h, bg, 6)
    hx, hy, r = x + w // 2, y + h // 2 - 1, max(2, w // 4)
    for j in range(h):
        for i in range(w):
            px, py = x + i, y + j
            if (px - hx) ** 2 + (py - hy) ** 2 <= r * r:
                cv.put(px, py, shade(skin, cv.rng.randint(-6, 6)))
            elif py > hy + r - 1 and abs(px - hx) <= (py - hy) + 1:
                cv.put(px, py, shade(cloth, cv.rng.randint(-6, 6)))
    for i in range(w):
        cv.put(x + i, y, rgba(INK)); cv.put(x + i, y + h - 1, rgba(INK))
    for j in range(h):
        cv.put(x, y + j, rgba(INK)); cv.put(x + w - 1, y + j, rgba(INK))


def coin(cv, x, y):
    for j, row in enumerate(("0110", "1221", "1221", "0110")):
        for i, ch in enumerate(row):
            if ch != "0":
                cv.put(x + i, y + j, rgba((232, 169, 59) if ch == "1" else (255, 222, 120)))


def cork(cv):
    x, y, w, h = REAR
    cv.rect(x, y, w, h, (150, 104, 62), 14)
    for _ in range(420):
        cv.put(x + cv.rng.randrange(w), y + cv.rng.randrange(h), rgba(cv.rng.choice([(118, 78, 44), (176, 128, 80), (102, 66, 38)])))


def pin_tex(cv, color):
    cv.rect(PIN[0], PIN[1], PIN[2], PIN[3], color, 10)


def wanted(cv, uv, bg, skin, cloth, claimed=False):
    x, y, w, h = uv
    paper(cv, x, y, w, h)
    cv.rect(x + 2, y + 2, w - 4, 3, (150, 32, 26), 8)                                   # bandeau "RECHERCHE"
    for i in range(x + 4, x + w - 4, 2):
        cv.put(i, y + 3, rgba((236, 214, 170)))
    portrait(cv, x + 4, y + 7, w - 8, h // 2 - 2, bg, skin, cloth)
    yy = y + 7 + h // 2
    coin(cv, x + 3, yy)
    cv.line(x + 8, yy + 1, w - 11, (150, 32, 26)); cv.line(x + 8, yy + 2, w - 13, (150, 32, 26))
    text_lines(cv, x + 3, yy + 6, w - 6, max(1, (y + h - 3 - (yy + 6)) // 2))
    if claimed:                                                                          # prime touchee : barre rouge
        for t in range(-1, 2):
            for i in range(w - 2):
                j = int(i * (h - 2) / (w - 2))
                cv.put(x + 1 + i, y + 1 + j + t, rgba((176, 26, 22)))
                cv.put(x + w - 2 - i, y + 1 + j + t, rgba((176, 26, 22)))


def monster_contract(cv, uv):
    x, y, w, h = uv
    paper(cv, x, y, w, h, (210, 196, 160))
    gx, gy = x + 3, y + 3                                                                # tete de trork
    for j, row in enumerate(("0111110", "1111111", "1201021", "1111111", "1311131", "0111110")):
        for i, ch in enumerate(row):
            c = {"1": (92, 128, 64), "2": (220, 40, 30), "3": (236, 230, 210)}.get(ch)
            if c:
                cv.put(gx + i, gy + j, shade(c, cv.rng.randint(-8, 8)))
    cv.rect(x + 12, y + 3, w - 15, 2, (120, 30, 24), 6)
    text_lines(cv, x + 12, y + 7, w - 15, 3)
    coin(cv, x + w - 7, y + h - 6)


def seal_note(cv, uv, wax):
    x, y, w, h = uv
    paper(cv, x, y, w, h)
    text_lines(cv, x + 2, y + 2, w - 4, 3)
    for j in range(4):
        for i in range(4):
            if (i, j) not in ((0, 0), (3, 0), (0, 3), (3, 3)):
                cv.put(x + w - 6 + i, y + h - 6 + j, shade(wax, cv.rng.randint(-15, 15)))


def crest(cv, x, y, w, h, field, charge):
    for j in range(h):
        half = w / 2 if j < h * 0.6 else (w / 2) * (1 - (j - h * 0.6) / (h * 0.4 + 0.01))
        for i in range(w):
            if abs(i + 0.5 - w / 2) <= half:
                cv.put(x + i, y + j, shade(field, cv.rng.randint(-8, 8)))
    for j in range(h // 3, h // 3 + 2):                                                   # chevron
        for i in range(w):
            if abs(i + 0.5 - w / 2) <= w / 2 - abs(j - h // 3) and abs((i + 0.5 - w / 2)) + (j - h // 3) * 1.0 > w / 2 - 3:
                cv.put(x + i, y + j + abs(int(i + 0.5 - w / 2)) // 2, rgba(charge))


def guild_notice(cv, uv, field, charge):
    x, y, w, h = uv
    paper(cv, x, y, w, h)
    crest(cv, x + w // 2 - 5, y + 3, 10, 12, field, charge)
    text_lines(cv, x + 3, y + 17, w - 6, (h - 19) // 2)


def ranking(cv, uv):
    x, y, w, h = uv
    paper(cv, x, y, w, h)
    cv.rect(x + 2, y + 2, w - 4, 2, (60, 90, 140), 6)
    for r, medal in enumerate([(232, 169, 59), (196, 202, 210), (176, 112, 60), INK, INK, INK]):
        yy = y + 6 + r * 3
        if yy + 1 >= y + h - 1:
            break
        cv.put(x + 3, yy, rgba(medal)); cv.put(x + 4, yy, rgba(medal))
        cv.line(x + 6, yy, w - 9, INK)


def guild_contract(cv, uv):
    x, y, w, h = uv
    paper(cv, x, y, w, h, (214, 200, 166))
    for i in range(7):                                                                    # epees croisees
        cv.put(x + 3 + i, y + 3 + i, rgba((190, 196, 206))); cv.put(x + 9 - i, y + 3 + i, rgba((190, 196, 206)))
    text_lines(cv, x + 13, y + 3, w - 16, 3)
    cv.rect(x + 3, y + h - 5, w - 6, 2, (40, 52, 44))                                     # progression
    cv.rect(x + 3, y + h - 5, (w - 6) * 3 // 5, 2, (74, 168, 88), 10)


def texture(kind, seed):
    cv = Canvas(random.Random(seed))
    for yy in range(VH):
        for xx in range(VW):
            cv.px[yy][xx] = list(VIMG[yy][xx])                                           # cadre vanilla, memes UV
    for yy in range(26, 44):                                                              # ancienne toile : inutilisee
        for xx in range(22):
            cv.px[yy][xx] = [0, 0, 0, 0]
    cork(cv)
    p = [uv for uv, _, _, _ in POSTERS]
    if kind == "Prime":
        wanted(cv, p[0], (120, 140, 160), (214, 170, 130), (60, 40, 40))
        wanted(cv, p[1], (140, 120, 100), (180, 130, 96), (40, 60, 90), claimed=True)
        wanted(cv, p[2], (110, 130, 110), (230, 190, 150), (90, 30, 30))
        monster_contract(cv, p[3])
        wanted(cv, p[4], (150, 140, 120), (160, 112, 80), (30, 30, 30))
        seal_note(cv, p[5], (170, 30, 30))
        pin_tex(cv, (190, 40, 36))
    else:
        guild_notice(cv, p[0], (46, 92, 168), (236, 196, 80))
        guild_notice(cv, p[1], (168, 40, 40), (236, 236, 236))
        ranking(cv, p[2])
        guild_contract(cv, p[3])
        guild_notice(cv, p[4], (52, 128, 72), (236, 196, 80))
        seal_note(cv, p[5], (46, 92, 168))
        pin_tex(cv, (200, 160, 60))
    path = os.path.join(RES, "Common", DIR, f"Tableau_{kind}_Texture.png")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    png.write(path, TEX, TEX, cv.px)
    print("ecrit", f"Common/{DIR}/Tableau_{kind}_Texture.png")
    return cv.px


# ===== Icone : vue de face (96 x 96 unites), reduite a 64 x 64 =====
def icon(kind, tex):
    S = 96
    img = [[[0, 0, 0, 0] for _ in range(S)] for _ in range(S)]

    def plot(ux, uy, c):                                                                  # ux : -48..48, uy : 0..96
        ix, iy = int(math.floor(ux + 48)), int(math.floor(96 - uy))
        if 0 <= ix < S and 0 <= iy < S and c[3] > 0:
            img[iy][ix] = list(c)

    rx, ry, rw, rh = REAR
    for j in range(rh):
        for i in range(rw):
            plot(-rw / 2 + i, 48 + rh / 2 - j, tex[ry + j][rx + i])
    for uv, (cx, cy), ang, _ in POSTERS:
        x, y, w, h = uv
        a = math.radians(ang)
        for j in range(-h, h):
            for i in range(-w, w):
                lx = i * math.cos(a) + j * math.sin(a)                                    # repere de l'affiche
                ly = -i * math.sin(a) + j * math.cos(a)
                if -w / 2 <= lx < w / 2 and -h / 2 <= ly < h / 2:
                    plot(cx + i, cy + j, tex[y + int(h / 2 - ly - 0.001)][x + int(lx + w / 2)])
    for uy in range(3, 94):                                                               # cadre : face avant des montants, x3
        for ux in range(-45, 46):
            side = abs(ux) >= 33
            if not side and 15 <= uy < 81:
                continue
            along, across = (uy - 3, (abs(ux) - 33)) if side else (ux + 33, (uy - 3) % 78 if uy < 15 else uy - 81)
            c = VIMG[9 + min(3, across // 3)][4 + (along // 3) % 22]
            plot(ux, uy, shade(c, 25) if side and (uy < 15 or uy >= 81) else c)
    out = [[img[int(y * 1.5)][int(x * 1.5)] for x in range(64)] for y in range(64)]
    path = os.path.join(RES, "Common", "Icons", "ItemsGenerated", f"Eldanior_Tableau_{kind}.png")
    png.write(path, 64, 64, out)
    print("ecrit", f"Common/Icons/ItemsGenerated/Eldanior_Tableau_{kind}.png")


# ===== Hitbox et objets =====
write("Server/Item/Block/Hitboxes/Eldanior/Tableau_3x3.json",
      {"Boxes": [{"Min": {"X": -1, "Y": 0, "Z": 0}, "Max": {"X": 2, "Y": 3, "Z": 0.4}}]})

for kind, name, seed in (("Prime", "Tableau de prime", 7), ("Guilde", "Tableau de guilde", 11)):
    icon(kind, texture(kind, seed))
    write(f"Server/Item/Items/Furniture/Eldanior/Eldanior_Tableau_{kind}.json", {
        "TranslationProperties": {"Name": name},
        "Quality": "Technical",
        "PlayerAnimationsId": "Block",
        "Categories": ["Eldanior_System.Autres"],
        "Icon": f"Icons/ItemsGenerated/Eldanior_Tableau_{kind}.png",
        "Interactions": {"Primary": "Block_Primary", "Secondary": "Block_Secondary"},
        "BlockType": {
            "BlockParticleSetId": "Wood",
            "BlockSoundSetId": "Wood",
            "PhysicalMaterialId": "Wood",
            "CustomModel": MODEL,
            "CustomModelScale": 1,
            "CustomModelTexture": [{"Texture": f"{DIR}/Tableau_{kind}_Texture.png", "Weight": 1}],
            "DrawType": "Model",
            "Gathering": {"Soft": {"IsWeaponBreakable": False}},
            "HitboxType": "Tableau_3x3",
            "Material": "Solid",
            "Opacity": "Transparent",
            "ParticleColor": "#543022",
            "Support": {"North": [{"FaceType": "Full"}]},
            "VariantRotation": "NESW",
            # Touche d'interaction : interaction Use sans effet, sinon le moteur n'emet pas UseBlockEvent (BoardInteractEvent)
            "Interactions": {"Use": {"Interactions": [{"Type": "Simple"}]}},
            "TextureComputedColor": "#4F3A29",
        },
        "Scale": 0.3,
        "MaxStack": 1,
        "ItemSoundSetId": "ISS_Blocks_Wood",
    })


# ===== Emblemes de la fenetre (192 px = 96 a l'ecran) =====
E = 192
BORDER, FILL, GOLD, GOLD_DARK = (214, 160, 62), (26, 34, 49), (232, 169, 59), (150, 98, 30)
RED, BLUE, PALE = (196, 40, 34), (46, 92, 168), (246, 222, 140)


def bounty_icon(u, v):
    """Piece d'or (bord fonce, reflet) sous un viseur rouge (cercle et quatre traits)."""
    r = math.hypot(u, v)
    col = None
    if r <= 0.42:
        col = GOLD_DARK if r > 0.36 else (PALE if (u + 0.12) ** 2 + (v + 0.14) ** 2 < 0.02 else GOLD)
        if 0.2 < r < 0.24:
            col = GOLD_DARK
    ring = abs(r - 0.55) < 0.035
    cross = (abs(u) < 0.03 and 0.3 < abs(v) < 0.72) or (abs(v) < 0.03 and 0.3 < abs(u) < 0.72)
    if ring or cross:
        col = RED
    return col


def guild_icon(u, v):
    """Blason : ecu bleu borde d'or, chevron d'or."""
    w = 0.42 if v < 0.12 else 0.42 * max(0.0, 1 - (v - 0.12) / 0.5)
    if not (-0.5 <= v <= 0.62 and abs(u) <= w):
        return None
    inner = 0.34 if v < 0.12 else 0.34 * max(0.0, 1 - (v - 0.12) / 0.42)
    if v < -0.42 or abs(u) > inner or v > 0.5:
        return GOLD
    if abs(v - (0.05 + abs(u) * 0.9)) < 0.08:
        return GOLD
    return BLUE


for kind, draw in (("Prime", bounty_icon), ("Guilde", guild_icon)):
    px = []
    for y in range(E):
        row = []
        for x in range(E):
            u, v = (x + 0.5) / E * 2 - 1, (y + 0.5) / E * 2 - 1
            d = abs(u) + abs(v)                                                           # losange
            col, a = [0, 0, 0], 0.0
            if d <= 0.97:
                if d >= 0.88:
                    col, a = list(BORDER), 1.0
                else:
                    glow = max(0.0, 1 - math.hypot(u, v) / 0.75)
                    col, a = [c + (g - c) * 0.25 * glow for c, g in zip(FILL, GOLD)], 1.0
                    c = draw(u, v)
                    if c is not None:
                        col = list(c)
                if d > 0.95:                                                              # bord adouci
                    a *= max(0.0, (0.97 - d) / 0.02)
            row.append([col[0], col[1], col[2], a * 255])
        px.append(row)
    out = os.path.join(RES, "Common", "UI", "Custom", "Board", f"BoardEmblem{kind}@2x.png")
    os.makedirs(os.path.dirname(out), exist_ok=True)
    png.write(out, E, E, px)
    print("ecrit", f"Common/UI/Custom/Board/BoardEmblem{kind}@2x.png")


# ===== Arches larges de la fenetre (a 2x : 1756 px = 878 a l'ecran) =====
AW = 1756
EDGE_DARK, EDGE_GOLD, EDGE_GOLD_DEEP = (22, 33, 47), (232, 179, 79), (176, 120, 40)


def arch(path, h, top, flat, shoulder, light, dark):
    """Arche convexe : cotes verticaux, pans coupes a 45 degres, bord plat (en haut si top, sinon en bas)."""
    px = []
    for y in range(h):
        row = []
        for x in range(AW):
            yy = y if top else h - 1 - y                                                  # distance au bord plat
            u = min(x, AW - 1 - x)
            d = min(u, yy - flat, (yy + u - shoulder) / math.sqrt(2))                     # distance au bord (polygone convexe)
            if d < -1:
                row.append([0, 0, 0, 0]); continue
            t = y / (h - 1)
            fill = [light[i] + (dark[i] - light[i]) * (t if top else 1 - t) for i in range(3)]
            if d < 6:
                c = list(EDGE_DARK)
            elif d < 20:
                k = abs(d - 13) / 7                                                       # or plus fonce sur les bords du filet
                c = [EDGE_GOLD[i] + (EDGE_GOLD_DEEP[i] - EDGE_GOLD[i]) * k * k for i in range(3)]
            elif d < 24:
                c = list(EDGE_DARK)
            elif 40 <= d < 43:
                c = [fill[i] + (EDGE_GOLD[i] - fill[i]) * 0.35 for i in range(3)]          # filet interieur
            else:
                c = fill
            a = 255 if d >= 24 or d < 20 else 255
            a = a if d >= 0 else int(255 * (1 + d))                                       # bord adouci
            row.append([c[0], c[1], c[2], a if d >= 24 else max(a, 0)])
        px.append(row)
    png.write(os.path.join(RES, "Common", "UI", "Custom", "Board", path), AW, h, px)
    print("ecrit", f"Common/UI/Custom/Board/{path}")


arch("BoardFrameTop@2x.png", 400, True, 60, 300, (29, 39, 56), (11, 20, 29))
arch("BoardFrameBottom@2x.png", 300, False, 40, 220, (20, 29, 43), (11, 20, 29))


# ===== Icone d'oeil (48 px = 24 a l'ecran), blanche : la fenetre la teinte =====
def eye():
    S2, px = 48, []
    for y in range(S2):
        row = []
        for x in range(S2):
            u, v = x + 0.5 - S2 / 2, y + 0.5 - S2 / 2
            R, k = 27.0, 17.0
            almond = min(R - math.hypot(u, v - k), R - math.hypot(u, v + k))             # amande : deux disques
            r = math.hypot(u, v)
            a = 0.0
            if 0 <= almond < 3.2:
                a = 1.0                                                                   # contour
            if r <= 8.5:
                a = 1.0                                                                   # iris
            if r <= 3.6 or math.hypot(u + 2.5, v + 2.5) <= 1.6:
                a = 0.0                                                                   # pupille, reflet
            if math.hypot(u + 2.5, v + 2.5) <= 1.6 and r <= 8.5:
                a = 1.0
            row.append([255, 255, 255, a * 255])
        px.append(row)
    png.write(os.path.join(RES, "Common", "UI", "Custom", "Board", "BoardEye@2x.png"), S2, S2, px)
    print("ecrit Common/UI/Custom/Board/BoardEye@2x.png")


eye()
