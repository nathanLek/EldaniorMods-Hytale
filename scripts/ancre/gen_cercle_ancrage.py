"""Cercle d'Ancrage : cercle magique peint au sol (3 x 3 blocs), point d'arrivee de la Porte des Mondes.

Genere tout ce qui n'est pas du code :
- la texture (96 x 96 px, 1 px = 1 unite de bloc, 32 unites = 1 bloc) : double anneau, bande de runes courbes, hexagramme,
  orbes aux pointes, anneau interieur et une ancre au centre. Alpha tout ou rien (pixels decoupes), rendu sans ombrage (fullbright) ;
- le modele : un seul quad horizontal (normale +Y, comme les quads vanilla), centre sur le bloc pose, a peine au-dessus du sol ;
- la hitbox (3 x 3, 1/16 de haut : on marche dessus) ;
- l'objet-bloc et son icone (64 px).

Usage : python3 scripts/ancre/gen_cercle_ancrage.py <src/main/resources> [dossier_apercu]
"""
import json, math, os, random, sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "rune_aura_icons"))
import png  # noqa: E402

RES = sys.argv[1]
PREVIEW = sys.argv[2] if len(sys.argv) > 2 else None
BLOCK_ID = "Eldanior_Cercle_Ancrage"
SIZE = 96           # pixels = unites : 3 blocs
C = SIZE / 2.0
SS = 1              # un echantillon au centre du pixel : traits nets, facon pixel art

CORE = (236, 222, 255)      # trait lumineux
MID = (168, 120, 255)       # trait secondaire

# Runes 5 x 5 (lues de haut en bas = de l'exterieur vers l'interieur de la bande)
GLYPHS = [
    ["#...#", ".#.#.", "..#..", "..#..", "..#.."],
    ["###..", "#..#.", "###..", "#.#..", "#..#."],
    ["..#..", ".###.", "#.#.#", "..#..", ".#.#."],
    ["#####", "..#..", ".#.#.", "#...#", "#...#"],
    ["#..#.", "#.#..", "##...", "#.#..", "#..#."],
    [".###.", "#...#", ".###.", "..#..", ".###."],
    ["#.#.#", "#.#.#", "#####", "..#..", "..#.."],
    ["..#..", "..#..", "#####", "..#..", "#...#"],
    ["#...#", "##.##", "#.#.#", "#...#", "#...#"],
    [".#.#.", "#####", ".#.#.", "#####", ".#.#."],
]

# Ancre centrale 17 x 17
ANCHOR = [
    ".......###.......",
    "......#...#......",
    "......#...#......",
    ".......###.......",
    "........#........",
    "....#########....",
    "........#........",
    "........#........",
    "........#........",
    "........#........",
    "..#.....#.....#..",
    ".###....#....###.",
    "..##....#....##..",
    "...##...#...##...",
    "....###.#.###....",
    "......#####......",
    "........#........",
]

random.seed(7)
RUNE_CELLS = 26
RUNES = [random.randrange(len(GLYPHS)) for _ in range(RUNE_CELLS)]


def seg_dist(px, py, ax, ay, bx, by):
    dx, dy = bx - ax, by - ay
    t = max(0.0, min(1.0, ((px - ax) * dx + (py - ay) * dy) / (dx * dx + dy * dy)))
    return math.hypot(px - ax - t * dx, py - ay - t * dy)


R_OUT, R_OUT2, R_BAND_IN = 46.6, 44.6, 37.2
R_HEX = 36.0
R_IN, R_IN2 = 20.5, 17.5
HEX = [(C + R_HEX * math.cos(math.radians(-90 + 60 * k)), C + R_HEX * math.sin(math.radians(-90 + 60 * k))) for k in range(6)]
TRI = [(HEX[0], HEX[2]), (HEX[2], HEX[4]), (HEX[4], HEX[0]), (HEX[1], HEX[3]), (HEX[3], HEX[5]), (HEX[5], HEX[1])]


def level(x, y):
    """2 = trait lumineux, 1 = trait secondaire, 0 = rien (point continu en pixels de texture)."""
    r = math.hypot(x - C, y - C)
    # anneaux
    if abs(r - R_OUT) < 0.9 or abs(r - R_BAND_IN) < 0.5 or abs(r - R_IN) < 0.5:
        return 2
    if abs(r - R_OUT2) < 0.5:
        return 1
    if abs(r - R_IN2) < 0.5 and int((math.atan2(y - C, x - C) + math.pi) / (2 * math.pi) * 40) % 2 == 0:
        return 1                                          # anneau interieur en pointilles
    # bande de runes (entre R_BAND_IN et R_OUT2), lettres courbes
    if R_BAND_IN + 0.9 < r < R_OUT2 - 0.5:
        a = (math.atan2(y - C, x - C) + math.pi / 2) % (2 * math.pi)
        cell = a / (2 * math.pi) * RUNE_CELLS
        k = int(cell)
        u = (cell - k) * 10.0 - 2.5                     # 11 px par case a ce rayon, lettre de 5 px au milieu
        v = (R_OUT2 - 1.0 - r) / (R_OUT2 - 1.5 - R_BAND_IN - 1.0) * 5.0
        if 0 <= u < 5 and 0 <= v < 5:
            if GLYPHS[RUNES[k]][int(v)][int(u)] == "#":
                return 2
        elif 7.0 <= u < 8.0 and 2 <= v < 3:              # point separateur
            return 1
        return 0
    # hexagramme (seulement a l'interieur de la bande)
    if r < R_BAND_IN:
        for (a, b) in TRI:
            if seg_dist(x, y, a[0], a[1], b[0], b[1]) < 0.6:
                return 2 if r > R_IN + 1 else 1
        for (hx, hy) in HEX:                             # orbes aux pointes
            d = math.hypot(x - hx, y - hy)
            if d < 2.3:
                return 2
    # rayons entre l'anneau interieur et l'hexagone interieur
    if R_IN2 + 1 < r < R_IN - 1:
        return 0
    # ancre au centre
    ax, ay = int(math.floor(x - (C - 8.5))), int(math.floor(y - (C - 8.5)))
    if 0 <= ax < 17 and 0 <= ay < 17 and ANCHOR[ay][ax] == "#":
        return 2
    return 0


def texture():
    lv = [[0] * SIZE for _ in range(SIZE)]
    for j in range(SIZE):
        for i in range(SIZE):
            hits = [0, 0, 0]
            for sj in range(SS):
                for si in range(SS):
                    hits[level(i + (si + 0.5) / SS, j + (sj + 0.5) / SS)] += 1
            n = SS * SS
            if hits[2] >= n * 0.4:
                lv[j][i] = 3
            elif hits[1] + hits[2] >= n * 0.3:
                lv[j][i] = 2
    px = [[[0, 0, 0, 0] for _ in range(SIZE)] for _ in range(SIZE)]
    for j in range(SIZE):
        for i in range(SIZE):
            if lv[j][i] == 3:
                px[j][i] = list(CORE) + [255]
            elif lv[j][i] == 2:
                px[j][i] = list(MID) + [255]
    return px


def model():
    face = {"offset": {"x": 0, "y": 0}, "mirror": {"x": False, "y": False}, "angle": 0}
    disc = {"id": "1", "name": "Cercle", "position": {"x": 0, "y": 0.25, "z": 0}, "orientation": {"x": 0, "y": 0, "z": 0, "w": 1},
            "shape": {"type": "quad", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": 1, "y": 1, "z": 1},
                      "settings": {"size": {"x": SIZE, "y": SIZE}, "normal": "+Y"}, "visible": True, "doubleSided": False,
                      "shadingMode": "fullbright", "unwrapMode": "custom", "textureLayout": {"front": face}},
            "children": []}
    root = {"id": "0", "name": "Cercle_Ancrage", "position": {"x": 0, "y": 0, "z": 0}, "orientation": {"x": 0, "y": 0, "z": 0, "w": 1},
            "shape": {"type": "none", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": 1, "y": 1, "z": 1}, "settings": {},
                      "textureLayout": {}, "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": "flat"},
            "children": [disc]}
    return {"lod": "auto", "nodes": [root]}


def hitbox():
    return {"Boxes": [{"Min": {"X": -1, "Y": 0, "Z": -1}, "Max": {"X": 2, "Y": 0.0625, "Z": 2}}]}


def item():
    return {
        "TranslationProperties": {
            "Name": "Cercle d'Ancrage",
            "Description": "Cercle magique de 3 blocs sur 3, peint au sol par son centre. En le posant, vous le nommez : il devient une "
                           "ancre, point d'arrivée de votre Porte des Mondes. Une ancre par joueur, trois si vous connaissez la Porte "
                           "des Mondes. Donné avec les runes d'Espace. Touche d'interaction : renommer. Seul son propriétaire peut l'effacer.",
        },
        "Icon": "Icons/ItemsGenerated/%s.png" % BLOCK_ID,
        "Categories": ["Eldanior_System.Autres", "Furniture.Furniture"],
        "Interactions": {"Primary": "Block_Primary", "Secondary": "Block_Secondary"},
        "BlockType": {
            "Material": "Solid",
            "DrawType": "Model",
            "Opacity": "Transparent",
            "CustomModel": "Blocks/Eldanior/Cercle_Ancrage.blockymodel",
            "CustomModelTexture": [{"Texture": "Blocks/Eldanior/Cercle_Ancrage_Texture.png", "Weight": 1}],
            "HitboxType": BLOCK_ID,
            "Gathering": {"Breaking": {"GatherType": "Benches"}},
            "BlockParticleSetId": "Dust",
            "ParticleColor": "#9a6cff",
            "BlockSoundSetId": "Cloth",
            "PhysicalMaterialId": "Wool",
            "TextureComputedColor": "#6a48c0",
            "Support": {"Down": [{"FaceType": "Full"}]},
            "Light": {"Color": "#536"},
            "Particles": [{"SystemId": "Block_Gem_Sparks", "Color": "#c8a8ff", "TargetNodeName": "Cercle"}],
            "Interactions": {"Use": {"Interactions": [{"Type": "Simple"}]}},
        },
        "PlayerAnimationsId": "Block",
        "IconProperties": {"Scale": 0.3, "Translation": [0, -10], "Rotation": [22.5, 45, 22.5]},
        "MaxStack": 10,
        "DropOnDeath": False,
        "ItemLevel": 1,
        "ItemSoundSetId": "ISS_Items_Cloth",
        "Quality": "Rare",
    }


def downscale(px, src, dst):
    """Moyenne ponderee par l'alpha ; alpha final tout ou rien."""
    out = [[[0, 0, 0, 0] for _ in range(dst)] for _ in range(dst)]
    k = src / dst
    for j in range(dst):
        for i in range(dst):
            acc, a, n = [0, 0, 0], 0, 0
            for y in range(int(j * k), int((j + 1) * k + 0.999)):
                for x in range(int(i * k), int((i + 1) * k + 0.999)):
                    p = px[min(y, src - 1)][min(x, src - 1)]
                    n += 1
                    if p[3]:
                        a += 1
                        for c in range(3):
                            acc[c] += p[c]
            if a and a * 2 >= n * 0.6:
                out[j][i] = [acc[c] / a for c in range(3)] + [255]
    return out


def dump(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


tex = texture()
common, server = os.path.join(RES, "Common"), os.path.join(RES, "Server")
os.makedirs(os.path.join(common, "Blocks", "Eldanior"), exist_ok=True)
png.write(os.path.join(common, "Blocks", "Eldanior", "Cercle_Ancrage_Texture.png"), SIZE, SIZE, tex)
dump(os.path.join(common, "Blocks", "Eldanior", "Cercle_Ancrage.blockymodel"), model())
dump(os.path.join(server, "Item", "Block", "Hitboxes", "Furniture", BLOCK_ID + ".json"), hitbox())
dump(os.path.join(server, "Item", "Items", "Furniture", "Eldanior", BLOCK_ID + ".json"), item())
# Icone : fond sombre circulaire pour la lisibilite dans l'inventaire
icon = downscale(tex, SIZE, 64)
for j in range(64):
    for i in range(64):
        if icon[j][i][3] == 0 and math.hypot(i + 0.5 - 32, j + 0.5 - 32) < 31:
            icon[j][i] = [30, 18, 52, 255]
os.makedirs(os.path.join(common, "Icons", "ItemsGenerated"), exist_ok=True)
png.write(os.path.join(common, "Icons", "ItemsGenerated", BLOCK_ID + ".png"), 64, 64, icon)

if PREVIEW:
    # Apercu x6 sur une herbe : 3 x 3 blocs avec la grille des blocs
    S = 6
    W = SIZE * S
    grass = [(78, 120, 52), (72, 112, 48), (84, 128, 58)]
    sheet = []
    for y in range(W):
        row = []
        for x in range(W):
            tx, ty = x // S, y // S
            p = tex[ty][tx]
            if p[3]:
                row.append(p[:3] + [255])
            else:
                g = grass[(tx * 7 + ty * 13 + (tx // 3) * (ty // 5)) % 3]
                edge = (tx % 32 == 0 and x % S == 0) or (ty % 32 == 0 and y % S == 0)
                row.append([c - 25 for c in g] + [255] if edge else list(g) + [255])
        sheet.append(row)
    os.makedirs(PREVIEW, exist_ok=True)
    png.write(os.path.join(PREVIEW, "cercle_ancrage.png"), W, W, sheet)
    big_icon = [[icon[y // 4][x // 4] for x in range(256)] for y in range(256)]
    png.write(os.path.join(PREVIEW, "cercle_ancrage_icone.png"), 256, 256, big_icon)
print("Cercle d'Ancrage genere.")
