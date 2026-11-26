"""Bannieres de famille : une par famille noble, sur le modele de Furniture_Human_Ruins_Banner (vanilla).

Meme objet que la banniere vanilla (se fixe a un mur par les faces Est/Ouest, hitbox "Banner", balancement
Banner_Swing.blockyanim vanilla) mais un tissu de noble, neuf : champ aux couleurs de la famille, plis, bordure d'or,
armoiries de la famille (familles.arms), chevrons d'or, pointe a gland d'or. Hampe de bois sombre a embouts d'or.

Geometrie reprise du vanilla (unites de bloc, 32 par bloc), a 3 px par unite au lieu de 1 :
  Beam (hampe 40x6x6, centre a y=16) > Cloth (36) > Cloth2 (31) > Cloth3 (27).
Cloth, Cloth2 et Cloth3 sont des noeuds vides places a la charniere (haut de chaque pan) : ce sont eux que l'animation
vanilla fait osciller ; le quad du pan est leur enfant. Ainsi, pas d'offset de forme a etirer.

Un seul modele (Blocks/Eldanior/Banniere_Famille.blockymodel), une texture par famille : la disposition de l'atlas ne
depend que des tailles, elle est donc la meme pour toutes (verifie a l'ecriture).

Usage : python3 scripts/familles/gen_bannieres.py <src/main/resources> [dossier_apercu]
"""
import json, math, os, random, sys

HERE = os.path.dirname(os.path.abspath(__file__))
for folder in ("armures", "rune_aura_icons"):
    sys.path.insert(0, os.path.join(HERE, "..", folder))
import png  # noqa: E402
import armor_kit as K  # noqa: E402
import blason  # noqa: E402
from familles import FAMILIES, GOLD, GOLD_DARK, GOLD_LIGHT, LINE, arms, category  # noqa: E402

RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "..", "..", "src", "main", "resources")
PREVIEW = sys.argv[2] if len(sys.argv) > 2 else None
COMMON, SERVER = os.path.join(RES, "Common"), os.path.join(RES, "Server")

D = 3                                    # pixels par unite
K.set_density(D, 256)
ITEM_PREFIX = "Eldanior_Banniere_Famille_"
MODEL = "Blocks/Eldanior/Banniere_Famille.blockymodel"
ANIMATION = "Blocks/Decorative_Sets/Human_Ruins/Banner_Swing.blockyanim"      # vanilla : noeuds Cloth, Cloth2, Cloth3

PANELS = (36, 31, 27)                    # hauteur des trois pans, en unites (vanilla)
CW = 32                                  # largeur du tissu
W, H = CW * D, sum(PANELS) * D           # tissu entier : 96 x 282 px
HIDDEN_TOP = 2 * D                       # haut du tissu cache dans la hampe (charniere a y=15, hampe de 13 a 19)
TIP = H - 12                             # pointe ; le gland d'or pend dessous
V_START = TIP - 40                       # debut de la pointe sur les bords

WOOD, WOOD_DARK, WOOD_LIGHT = (70, 44, 30), (42, 26, 18), (98, 64, 42)


def shade(c, k):
    return [max(0, min(255, c[i] * k)) for i in range(3)]


# Tissu ----------------------------------------------------------------------------------------------------------------
def edge_distance(x, y):
    """Distance (px) au bord du tissu ; negative dehors. Le haut compte a partir de ce qui sort de la hampe."""
    cx = (W - 1) / 2.0
    slope = (TIP - V_START) / cx
    bottom = V_START + (TIP - V_START) * (1 - abs(x - cx) / cx)
    return min(x + 0.5, W - 0.5 - x, y - HIDDEN_TOP + 0.5, (bottom - y) / math.sqrt(1 + slope * slope))


def chevron(cx, cy, half, drop, thick):
    """Chevron pointe en bas : renvoie une fonction (x, y) -> vrai si le pixel est dans le chevron."""
    def inside(x, y):
        t = abs(x - cx) / half
        if t > 1:
            return False
        line = cy + drop * (1 - t)
        return line - thick <= y <= line
    return inside


def cloth(color, kind):
    rnd = random.Random(sum(color) * 7 + len(kind))
    field = list(color)
    if sum(field) < 120:                                       # champs presque noirs (Luo, Nighthollow) : un rien de relief
        field = [c + 10 for c in field]
    img = [[[0, 0, 0, 0] for _ in range(W)] for _ in range(H)]
    shield = arms(color, kind, 64, px=128)
    sh, sw = len(shield), len(shield[0])
    sx0, sy0 = (W - sw) // 2, 34
    marks = [chevron(W / 2.0, 150, 30, 18, 6), chevron(W / 2.0, 172, 22, 13, 5)]
    for y in range(H):
        for x in range(W):
            d = edge_distance(x, y)
            if d < 0:
                continue
            fold = 0.88 + 0.12 * math.sin(2 * math.pi * x / 24.0 + 0.6) + 0.03 * math.sin(2 * math.pi * x / 9.0)
            fold *= 1.0 - 0.10 * y / H                         # le bas un peu plus sombre
            weave = 1.0 + (rnd.random() - 0.5) * 0.06 + (0.025 if (x + y) % 2 else -0.025)
            if d < 1.2:
                c = shade(field, 0.45)                         # lisiere
            elif 4 <= d < 7:
                c = GOLD_LIGHT if d < 5 else (GOLD if d < 6 else GOLD_DARK)
            elif 7 <= d < 8:
                c = shade(field, 0.55)
            else:
                c = field
                if any(m(x, y) for m in marks):
                    c = GOLD
                elif 196 <= y <= 214 and abs(x - W / 2.0) + abs(y - 205) <= 9:
                    c = GOLD_LIGHT if abs(x - W / 2.0) + abs(y - 205) <= 4 else GOLD   # losange d'or
            k = fold * weave
            img[y][x] = shade(c, k) + [255]
    # armoiries, posees par-dessus (alpha)
    for j in range(sh):
        for i in range(sw):
            p = shield[j][i]
            if p[3] <= 0:
                continue
            a = p[3] / 255.0
            q = img[sy0 + j][sx0 + i]
            img[sy0 + j][sx0 + i] = [q[k] * (1 - a) + p[k] * a for k in range(3)] + [255]
    # gland d'or sous la pointe
    cx = (W - 1) / 2.0
    for y in range(TIP - 3, H):
        t = (y - (TIP - 3)) / float(H - 1 - (TIP - 3))
        half = 1.5 + 2.5 * t if t > 0.25 else 1.5
        for x in range(W):
            if abs(x - cx) <= half:
                strand = 0.85 + 0.25 * ((x * 3 + y) % 3 == 0)
                img[y][x] = shade(GOLD if t < 0.25 else GOLD_LIGHT if (x % 2) else GOLD, strand) + [255]
    return img


# Hampe ----------------------------------------------------------------------------------------------------------------
def beam_paint(face, w, h, seed):
    rnd = random.Random(seed)
    img = [[[0, 0, 0, 255] for _ in range(w)] for _ in range(h)]
    end = face in ("left", "right")
    for y in range(h):
        for x in range(w):
            if end:                                            # bout de la hampe : disque d'or cercle de bois
                r = max(abs(x - (w - 1) / 2.0), abs(y - (h - 1) / 2.0))
                c = GOLD_DARK if r > w / 2.0 - 2 else GOLD if r > 3 else GOLD_LIGHT
            elif x < 2 * D or x >= w - 2 * D:                  # viroles d'or aux deux bouts
                c = GOLD_LIGHT if y < h * 0.35 else GOLD if y < h * 0.7 else GOLD_DARK
            else:
                grain = 0.5 + 0.5 * math.sin(x * 0.21 + math.sin(y * 0.9 + seed) * 2.2)
                c = blason.mix(WOOD_DARK, WOOD_LIGHT, 0.25 + 0.5 * grain)
                c = shade(c, 1.12 if y < 2 else 0.8 if y >= h - 2 else 1.0)
            img[y][x] = shade(c, 1.0 + (rnd.random() - 0.5) * 0.08) + [255]
    return img


def finial_paint(face, w, h, seed):
    img = [[[0, 0, 0, 255] for _ in range(w)] for _ in range(h)]
    for y in range(h):
        for x in range(w):
            edge = min(x, y, w - 1 - x, h - 1 - y)
            c = GOLD_DARK if edge == 0 else GOLD_LIGHT if (x + y) < w * 0.6 else GOLD
            img[y][x] = list(c) + [255]
    return img


# Modele ---------------------------------------------------------------------------------------------------------------
def hinge(model, name, pos, children):
    return {"id": model.nid(), "name": name, "position": K.vec(pos), "orientation": dict(K.ID_Q),
            "shape": {"type": "none", "offset": K.vec((0, 0, 0)), "stretch": {"x": 1, "y": 1, "z": 1}, "settings": {},
                      "textureLayout": {}, "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": "flat"},
            "children": children}


def build(color, kind):
    model = K.Model("banniere")
    full = cloth(color, kind)
    slices, top = [], 0
    for n in PANELS:
        slices.append(full[top:top + n * D])
        top += n * D
    panels = []
    for i, (n, img) in enumerate(zip(PANELS, slices)):
        q = K.quad(model, "Tissu%d" % (i + 1), (0, -n / 2.0, 0), img)
        q["shape"]["shadingMode"] = "standard"                 # comme le tissu vanilla
        panels.append(q)
    cloth3 = hinge(model, "Cloth3", (0, -PANELS[1], 0), [panels[2]])
    cloth2 = hinge(model, "Cloth2", (0, -PANELS[0], 0), [panels[1], cloth3])
    cloth1 = hinge(model, "Cloth", (0, -1, 0), [panels[0], cloth2])
    beam = K.box(model, "Beam", (0, 16, 0), (40, 6, 6), beam_paint)
    beam["children"] = [K.box(model, "Embout_E", (22, 0, 0), (4, 4, 4), finial_paint),
                        K.box(model, "Embout_O", (-22, 0, 0), (4, 4, 4), finial_paint),
                        cloth1]
    w, h, tex = model.texture()
    return {"nodes": [beam], "lod": "auto"}, (w, h, tex), full


def front_icon(tex, full):
    """Vue de face, comme l'icone vanilla : hampe, embouts et tissu, reduits a 64x64."""
    top = 19                                                   # haut de la hampe, en unites
    bottom = 15 - sum(PANELS)
    cw, ch = 48 * D, (top - bottom) * D
    img = [[[0, 0, 0, 0] for _ in range(cw)] for _ in range(ch)]

    def blit(src, x0, y0):
        for j, row in enumerate(src):
            for i, p in enumerate(row):
                if p[3] > 0 and 0 <= y0 + j < ch and 0 <= x0 + i < cw:
                    img[y0 + j][x0 + i] = list(p)
    blit(full, (cw - W) // 2, (top - 15) * D)
    blit(beam_paint("front", 40 * D, 6 * D, 1), 4 * D, 0)
    for x0 in (0, 44 * D):
        blit(finial_paint("front", 4 * D, 4 * D, 0), x0, 1 * D)
    k = 60.0 / ch
    small = blason.shrink(img, max(1, int(round(cw * k))), 60)
    out = [[[0, 0, 0, 0] for _ in range(64)] for _ in range(64)]
    ox, oy = (64 - len(small[0])) // 2, 2
    for j, row in enumerate(small):
        for i, p in enumerate(row):
            out[oy + j][ox + i] = p
    return out


def item(fid, name, color):
    hexa = "#%02x%02x%02x" % tuple(color)
    return {
        "TranslationProperties": {"Name": "Bannière de famille - " + name,
                                  "Description": "Bannière aux armes de la famille " + name + ". Se fixe à un mur et pend sur trois blocs."},
        "PlayerAnimationsId": "Block",
        "Categories": [category(fid), "Furniture.Signs"],
        "Icon": "Icons/ItemsGenerated/%s%s.png" % (ITEM_PREFIX, fid),
        "IconProperties": {"Rotation": [0, 0, 0], "Scale": 0.26, "Translation": [0, 16.9]},
        "Interactions": {"Primary": "Block_Primary", "Secondary": "Block_Secondary"},
        "BlockType": {
            "BlockParticleSetId": "Wood",
            "CustomModel": MODEL,
            "CustomModelAnimation": ANIMATION,
            "CustomModelTexture": [{"Texture": "Blocks/Eldanior/Banniere_Famille_%s_Texture.png" % fid, "Weight": 1}],
            "DrawType": "Model",
            "Flags": {},
            "Gathering": {"Soft": {"IsWeaponBreakable": False}},
            "Material": "Solid",
            "Opacity": "Transparent",
            "ParticleColor": hexa,
            "Support": {face: [{"AllowSupportPropagation": False, "FaceType": "Full", "Rotate": True}] for face in ("West", "East")},
            "VariantRotation": "NESW",
            "BlockSoundSetId": "Cloth",
            "PhysicalMaterialId": "Wool",
            "Looping": True,
            "HitboxType": "Banner",
            "TextureComputedColor": hexa,
        },
        "Quality": "Rare",
        "Tags": {"Type": ["Furniture"], "Family": ["Eldanior"]},
        "ItemSoundSetId": "ISS_Items_Cloth",
    }


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


def main():
    block_dir = os.path.join(COMMON, "Blocks", "Eldanior")
    item_dir = os.path.join(SERVER, "Item", "Items", "Furniture", "Eldanior", "Bannieres_Famille")
    shared = None
    for fid, name, color, kind in FAMILIES:
        model, (w, h, tex), full = build(color, kind)
        if shared is None:
            shared = model
            write_json(os.path.join(COMMON, *MODEL.split("/")), model)
        assert model == shared, "la disposition de l'atlas a change d'une famille a l'autre"
        png.write(os.path.join(block_dir, "Banniere_Famille_%s_Texture.png" % fid), w, h, tex)
        png.write(os.path.join(COMMON, "Icons", "ItemsGenerated", "%s%s.png" % (ITEM_PREFIX, fid)), 64, 64, front_icon(tex, full))
        write_json(os.path.join(item_dir, ITEM_PREFIX + fid + ".json"), item(fid, name, color))
        if PREVIEW:
            os.makedirs(PREVIEW, exist_ok=True)
            quads = K.quads_of(model["nodes"][0], (0, 0, 0), K.ID_Q, tex, ("\0",))
            png.write(os.path.join(PREVIEW, "banniere_%s.png" % fid), 256, 256, K.downscale(K.draw(quads, 768, 25, 10), 3))
        print("%-12s texture %dx%d" % (fid, w, h))


if __name__ == "__main__":
    main()
