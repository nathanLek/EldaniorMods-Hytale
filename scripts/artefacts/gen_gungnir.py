"""Gungnir, la Lance d'Odin (artefact divin) : modele 3D, texture, icone, et texture des corbeaux Hugin et Munin.

Modele : meme hierarchie que les lances vanilla (R-Attachment > Origin_Projectile (pointe) > Origin_Item > Handle) pour que
les animations de lance la tiennent ; la hampe suit l'axe x local de Handle. Les enfants sont places par rapport au CENTRE
de la forme du parent (pivot + offset). UV au pixel pres (une face de 124x5 lit 124x5 pixels) : chaque materiau occupe une
grande zone uniforme de l'atlas et toutes ses faces y lisent depuis le coin, quel que soit leur sens.
Icone : rendu orthographique du modele (face visible des boites), en diagonale comme les icones vanilla.
Corbeaux : texture du vautour vanilla recoloree en noir a reflets bleus (modele et animation de vol vanilla).
L'arme utilise les animations vanilla de la lance (pointe devant pendant le lancer) : ne pas les modifier.

Usage : python3 scripts/artefacts/gen_gungnir.py <src/main/resources/Common> <dossier contenant Common/... extrait d'Assets.zip>
"""
import json, math, os, random, sys

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "rune_aura_icons"))
import png  # noqa: E402

OUT, SRC = sys.argv[1], sys.argv[2]

# ---------------- Materiaux : zone de l'atlas (x, y, taille), couleur de base, rendu ----------------
ATLAS_W, ATLAS_H = 256, 128
MATERIALS = {
    "wood":    ((0, 0, 128),   (74, 50, 36),    "flat"),        # frene sombre
    "gold":    ((128, 0, 64),  (236, 184, 70),  "flat"),        # or
    "crystal": ((192, 0, 64),  (140, 220, 255), "fullbright"),  # cristal de glace d'Asgard
    "blade":   ((128, 64, 64), (246, 234, 196), "flat"),        # lame d'or pale
    "glow":    ((192, 64, 64), (205, 245, 255), "fullbright"),  # runes lumineuses
}


def atlas():
    rnd = random.Random(7)
    px = [[[0, 0, 0, 0] for _ in range(ATLAS_W)] for _ in range(ATLAS_H)]
    for name, ((x0, y0, s), base, _) in MATERIALS.items():
        for y in range(s):
            for x in range(s):
                n = rnd.uniform(-1, 1)
                if name == "wood":                                   # grain fin, isotrope a petite echelle
                    k = 1 + 0.12 * n + 0.08 * math.sin((x + y) * 0.9)
                elif name in ("gold", "blade"):                      # metal : reflets doux
                    k = 1 + 0.07 * n + 0.06 * math.sin(x * 0.7) * math.cos(y * 0.5)
                else:                                                # cristal / lueur : scintillement
                    k = 1 + 0.05 * n
                px[y0 + y][x0 + x] = [min(255, c * k) for c in base] + [255]
    png.write(os.path.join(MODEL_DIR, "Artefact_Gungnir.png"), ATLAS_W, ATLAS_H, px)


# ---------------- Noeuds ----------------
IDENT = {"x": 0, "y": 0, "z": 0, "w": 1}
_id = [0]
BOXES = []                                                     # (taille, position locale Handle, quaternion local, materiau) pour l'icone


def q_axis(axis, deg):
    s = math.sin(math.radians(deg) / 2)
    return {"x": round(axis[0] * s, 6), "y": round(axis[1] * s, 6), "z": round(axis[2] * s, 6), "w": round(math.cos(math.radians(deg) / 2), 6)}


def qmul(a, b):
    return {"x": a["w"] * b["x"] + a["x"] * b["w"] + a["y"] * b["z"] - a["z"] * b["y"],
            "y": a["w"] * b["y"] - a["x"] * b["z"] + a["y"] * b["w"] + a["z"] * b["x"],
            "z": a["w"] * b["z"] + a["x"] * b["y"] - a["y"] * b["x"] + a["z"] * b["w"],
            "w": a["w"] * b["w"] - a["x"] * b["x"] - a["y"] * b["y"] - a["z"] * b["z"]}


def node(name, pos, orient=IDENT, shape=None, children=None):
    _id[0] += 1
    return {"id": str(_id[0]), "name": name, "position": {"x": round(pos[0], 3), "y": round(pos[1], 3), "z": round(pos[2], 3)},
            "orientation": orient, "shape": shape or empty(), "children": children or []}


def empty(piece=False):
    """Noeud sans forme. piece=True : le moteur le fixe sur l'os du meme nom (R-Attachment de la main, Origin_Projectile du
    projectile), comme les armes vanilla ; sinon l'arme ne suit pas les animations de la main (lancer : pointe derriere)."""
    return {"type": "none", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": 1, "y": 1, "z": 1}, "settings": {"isPiece": piece},
            "textureLayout": {}, "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": "flat"}


def box(size, material, offset=(0, 0, 0)):
    (x0, y0, _), _, shading = MATERIALS[material]
    face = {"offset": {"x": x0, "y": y0}, "mirror": {"x": False, "y": False}, "angle": 0}
    return {"type": "box", "offset": {"x": offset[0], "y": offset[1], "z": offset[2]}, "stretch": {"x": 1, "y": 1, "z": 1},
            "settings": {"size": {"x": size[0], "y": size[1], "z": size[2]}},
            "textureLayout": {k: dict(face) for k in ("top", "bottom", "front", "back", "left", "right")},
            "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": shading}


def part(name, pos, size, material, orient=IDENT):
    """Piece posee sur la hampe (position relative au centre de la hampe)."""
    BOXES.append((size, pos, orient, material))
    return node(name, pos, orient, box(size, material))


SHAFT_LEN, SHAFT_OFFSET = 124, 28                              # hampe : 124 unites, centre 28 unites devant la main


def gungnir():
    kids = []
    # Talon : capuchon d'or et pointe en losange
    kids.append(part("Butt_Cap", (-63, 0, 0), (6, 8, 8), "gold"))
    kids.append(part("Butt_Disc", (-66.5, 0, 0), (2.5, 6.4, 6.4), "gold"))           # talon emousse : rien qui ressemble a une pointe
    # Anneaux d'or et runes lumineuses gravees sur la hampe (pas a l'endroit de la main : x = -28)
    for i, x in enumerate((-50, -8, 20, 46)):
        kids.append(part("Ring_%d" % i, (x, 0, 0), (2.5, 6.6, 6.6), "gold"))
    for c, x0 in enumerate((2, 33)):
        for r, x in enumerate((x0, x0 + 7)):
            kids.append(part("Rune_%d_%d_a" % (c, r), (x, 2.75, 0), (4.5, 0.7, 1.6), "glow"))
            kids.append(part("Rune_%d_%d_b" % (c, r), (x, -2.75, 0), (4.5, 0.7, 1.6), "glow"))
            kids.append(part("Rune_%d_%d_c" % (c, r), (x, 0, 2.75), (4.5, 1.6, 0.7), "glow"))
            kids.append(part("Rune_%d_%d_d" % (c, r), (x, 0, -2.75), (4.5, 1.6, 0.7), "glow"))
    # Collier en losange, ailes de plumes d'or (Hugin et Munin), coeur de cristal
    kids.append(part("Collar", (62, 0, 0), (8, 9, 9), "gold", q_axis((1, 0, 0), 45)))
    for s in (1, -1):
        for k, (length, width) in enumerate(((19, 4.2), (15.5, 3.6), (12, 3))):
            theta = s * (118 + 17 * k)
            d = (math.cos(math.radians(theta)), math.sin(math.radians(theta)))
            reach = length / 2 + 2.5
            kids.append(part("Wing_%s_%d" % ("L" if s > 0 else "R", k), (62 + d[0] * reach, d[1] * reach, 0),
                             (length, width, 2), "gold", q_axis((0, 0, 1), theta)))
    kids.append(part("Core", (68.5, 0, 0), (8, 5.5, 5.5), "crystal", q_axis((1, 0, 0), 45)))
    # Lame en feuille : base large, milieu, haut, pointe en losange ; arete centrale de cristal
    # Deux lames croisees (comme le fer des lances vanilla) : la pointe se lit sous tous les angles, meme de dos
    for k, roll in enumerate((0, 90)):
        r = q_axis((1, 0, 0), roll)
        kids.append(part("Blade_Base_%d" % k, (77, 0, 0), (10, 10.5, 1.6), "blade", r))
        kids.append(part("Blade_Mid_%d" % k, (88, 0, 0), (13, 8.5, 1.6), "blade", r))
        kids.append(part("Blade_Top_%d" % k, (99.5, 0, 0), (11, 5.5, 1.6), "blade", r))
        kids.append(part("Blade_Tip_%d" % k, (106, 0, 0), (5.2, 5.2, 1.6), "blade", qmul(r, q_axis((0, 0, 1), 45))))
    kids.append(part("Blade_Ridge", (90, 0, 0), (34, 1.8, 2.3), "crystal"))
    BOXES.append(((SHAFT_LEN, 5, 5), (0, 0, 0), IDENT, "wood"))
    handle = node("Handle", (0, -34, 0), {"x": 0.5, "y": 0.5, "z": -0.5, "w": -0.5},
                  box((SHAFT_LEN, 5, 5), "wood", (SHAFT_OFFSET, 0, 0)), kids)
    # Pointe (Origin_Projectile) : main a y = 42, pointe a 28 + 110 unites devant
    tip = SHAFT_OFFSET + 110
    origin_item = node("Origin_Item", (0, -64 - (tip - 98), 0), children=[handle])
    origin_proj = node("Origin_Projectile", (0, tip, 0), shape=empty(piece=True), children=[origin_item])
    root = node("R-Attachment", (0, 42, 0), shape=empty(piece=True), children=[origin_proj])
    json.dump({"nodes": [root], "lod": "auto"}, open(os.path.join(MODEL_DIR, "Artefact_Gungnir.blockymodel"), "w"), indent=2)
    print("modele : %d pieces, %.2f blocs de long" % (len(BOXES), (tip + SHAFT_LEN / 2 - SHAFT_OFFSET + 8) / 64))


# ---------------- Icone (64x64) ----------------
def qrot(q, v):
    x, y, z, w = q["x"], q["y"], q["z"], q["w"]; vx, vy, vz = v
    ix = w * vx + y * vz - z * vy; iy = w * vy + z * vx - x * vz; iz = w * vz + x * vy - y * vx; iw = -x * vx - y * vy - z * vz
    return (ix * w - iw * x - iy * z + iz * y, iy * w - iw * y - iz * x + ix * z, iz * w - iw * z - ix * y + iy * x)


def icon():
    S = 64
    px = [[[0, 0, 0, 0] for _ in range(S)] for _ in range(S)]
    depth = [[-1e9] * S for _ in range(S)]
    a = math.radians(45)                                        # hampe en diagonale, pointe en haut a droite
    lo, hi = -72, 111
    scale = (S - 6) * math.sqrt(2) / (hi - lo)
    mid = (lo + hi) / 2

    def proj(p):
        x, y = p[0] - mid, p[1]
        u = (x * math.cos(a) - y * math.sin(a)) * scale + S / 2
        v = S / 2 - (x * math.sin(a) + y * math.cos(a)) * scale
        return u, v, p[2]

    order = sorted(BOXES, key=lambda b: (b[3] != "wood", b[3] in ("crystal", "glow"), b[1][2]))
    for size, pos, orient, mat in order:
        hx, hy, hz = size[0] / 2, size[1] / 2, size[2] / 2
        corners = [qrot(orient, (sx * hx, sy * hy, hz)) for sx, sy in ((-1, -1), (1, -1), (1, 1), (-1, 1))]
        poly = [proj((pos[0] + c[0], pos[1] + c[1], pos[2] + c[2])) for c in corners]
        base = MATERIALS[mat][1]
        us, vs = [p[0] for p in poly], [p[1] for p in poly]
        for y in range(max(0, int(min(vs)) - 1), min(S, int(max(vs)) + 2)):
            for x in range(max(0, int(min(us)) - 1), min(S, int(max(us)) + 2)):
                cx, cy = x + 0.5, y + 0.5
                dists = []
                for i in range(4):
                    (x1, y1, _), (x2, y2, _) = poly[i], poly[(i + 1) % 4]
                    length = math.hypot(x2 - x1, y2 - y1) or 1
                    dists.append(((x2 - x1) * (cy - y1) - (y2 - y1) * (cx - x1)) / length)
                if not (all(d >= 0 for d in dists) or all(d <= 0 for d in dists)): continue   # ecran : y vers le bas
                edge = min(abs(d) for d in dists)
                shade = 0.72 if edge < 0.8 else 1.0 + 0.12 * (1 - min(1, edge / 3))
                px[y][x] = [min(255, c * shade) for c in base] + [255]
    out = [[p[:] for p in row] for row in px]                   # contour sombre d'un pixel, comme les icones vanilla
    for y in range(S):
        for x in range(S):
            if px[y][x][3]: continue
            if any(0 <= x + dx < S and 0 <= y + dy < S and px[y + dy][x + dx][3] for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                out[y][x] = [28, 22, 18, 255]
    path = os.path.join(OUT, "Icons", "ItemsGenerated")
    os.makedirs(path, exist_ok=True)
    png.write(os.path.join(path, "Artefact_Gungnir.png"), S, S, out)


# ---------------- Corbeaux Hugin et Munin ----------------
def ravens():
    w, h, img = png.read(os.path.join(SRC, "Common/NPC/Flying_Beast/Vulture/Models/Texture.png"))
    for row in img:
        for p in row:
            if p[3] == 0: continue
            lum = (p[0] + p[1] + p[2]) / 3
            p[0], p[1], p[2] = 16 + lum * 0.22, 18 + lum * 0.24, 26 + lum * 0.36   # noir a reflets bleus
    path = os.path.join(OUT, "Items", "Projectiles", "Gungnir")
    os.makedirs(path, exist_ok=True)
    png.write(os.path.join(path, "Raven.png"), w, h, img)


MODEL_DIR = os.path.join(OUT, "Items", "Weapons", "artefacts")
os.makedirs(MODEL_DIR, exist_ok=True)
atlas()
gungnir()
icon()
ravens()
print("ok")
