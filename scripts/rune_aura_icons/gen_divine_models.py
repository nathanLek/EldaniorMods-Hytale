"""Modeles 3D des sorts divins : roue de Taranis, porte de Niflheim (+ animation d'ouverture), stalactite de glace.
Usage : python3 gen_divine_models.py <dossier_resources_Common> <dossier_contenant_Common/...extrait d'Assets.zip>"""
import json, math, os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import png

def q_axis(axis, deg):
    s = math.sin(math.radians(deg) / 2)
    return {"x": round(axis[0] * s, 6), "y": round(axis[1] * s, 6), "z": round(axis[2] * s, 6), "w": round(math.cos(math.radians(deg) / 2), 6)}
IDENT = {"x": 0, "y": 0, "z": 0, "w": 1}
def face(ox, oy): return {"offset": {"x": ox, "y": oy}, "mirror": {"x": False, "y": False}, "angle": 0}
_id = [1]
def node(name, pos, orient=IDENT, size=None, uv=(0, 0), offset=(0, 0, 0), children=None, shading="fullbright"):
    _id[0] += 1
    if size:
        shape = {"type": "box", "offset": {"x": offset[0], "y": offset[1], "z": offset[2]}, "stretch": {"x": 1, "y": 1, "z": 1},
                 "settings": {"size": {"x": size[0], "y": size[1], "z": size[2]}},
                 "textureLayout": {k: face(*uv) for k in ("top", "bottom", "front", "back", "left", "right")},
                 "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": shading}
    else:
        shape = {"type": "none", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": 1, "y": 1, "z": 1}, "settings": {"isPiece": False},
                 "visible": True, "doubleSided": False, "shadingMode": "standard", "unwrapMode": "custom", "textureLayout": {}}
    return {"id": str(_id[0]), "name": name, "position": {"x": round(pos[0], 3), "y": round(pos[1], 3), "z": round(pos[2], 3)},
            "orientation": orient, "shape": shape, "children": children or []}
def save_model(path, root):
    json.dump({"nodes": [root], "lod": "auto"}, open(path, "w"), indent=2)
def tex(path, regions):
    """Texture 32x32 : regions = {(x0, y0): fonction(u, v) -> rgba} sur des cases de 16x16."""
    px = [[[0, 0, 0, 0] for _ in range(32)] for _ in range(32)]
    for (x0, y0), fn in regions.items():
        for y in range(16):
            for x in range(16): px[y0 + y][x0 + x] = fn(x / 15, y / 15)
    png.write(path, 32, 32, px)

def main():
    out, src = sys.argv[1], sys.argv[2]

    # ---------------- Roue de Taranis ----------------
    T = os.path.join(out, "Items/Projectiles/Taranis"); os.makedirs(T, exist_ok=True)
    tex(os.path.join(T, "Wheel.png"), {
        (0, 0): lambda u, v: [255, 200 + 40 * (1 - v), 90 + 60 * (1 - v), 255],         # bronze dore
        (16, 0): lambda u, v: [210 + 45 * (1 - v), 235 + 20 * (1 - v), 255, 255],       # electrique blanc-bleu
    })
    R, SEG = 60, 40
    kids = []
    for i in range(SEG):
        t = 2 * math.pi * i / SEG
        kids.append(node("Rim_%d" % i, (R * math.cos(t), 0, R * math.sin(t)), q_axis((0, 1, 0), -math.degrees(t) - 90), (11, 6, 8)))
    for k in range(8):
        t = k * math.pi / 4
        kids.append(node("Spoke_%d" % k, (34 * math.cos(t), 0, 34 * math.sin(t)), q_axis((0, 1, 0), -math.degrees(t)), (52, 4, 4)))
        kids.append(node("Node_%d" % k, (R * math.cos(t), 0, R * math.sin(t)), q_axis((0, 1, 0), 45 - math.degrees(t)), (10, 10, 10), (16, 0)))
    kids.append(node("Hub", (0, 0, 0), q_axis((0, 1, 0), 45), (18, 10, 18), (16, 0)))
    save_model(os.path.join(T, "Wheel.blockymodel"), node("Circle", (0, 0, 0), children=kids))

    # ---------------- Porte de Niflheim (porte divine flottante) ----------------
    N = os.path.join(out, "Items/Projectiles/Niflheim"); os.makedirs(N, exist_ok=True)
    tex(os.path.join(N, "Gate.png"), {
        (0, 0): lambda u, v: [150 + 60 * (1 - v), 215 + 30 * (1 - v), 255, 235],        # glace
        (16, 0): lambda u, v: [225 + 30 * (1 - v), 248, 255, 255],                       # runes / givre lumineux
        (0, 16): lambda u, v: [70 + 60 * abs(u - .5), 150 + 60 * abs(u - .5), 230, 200],  # battants (glace sombre)
        (16, 16): lambda u, v: [40 + 40 * (1 - v), 90 + 60 * (1 - v), 170 + 50 * (1 - v), 245],  # glace profonde (socle)
    })
    kids = []
    # Socle flottant + stalactites dessous
    kids.append(node("Base", (0, 5, 0), size=(124, 10, 38), uv=(16, 16)))
    kids.append(node("Base_Rim", (0, 11, 0), size=(112, 2, 30), uv=(16, 0)))
    for i, x in enumerate((-50, -34, -18, 0, 18, 34, 50)):
        L = 26 - abs(x) // 4
        kids.append(node("Icicle_%d" % i, (x, -L / 2, 0), size=(5, L, 5), uv=(0, 0)))
    for sgn in (-1, 1):
        px = sgn * 50
        kids.append(node("Plinth_%d" % sgn, (px, 16, 0), size=(24, 10, 24), uv=(16, 16)))
        kids.append(node("Shaft_%d" % sgn, (px, 72, 0), size=(16, 102, 16)))
        for k, y in enumerate((40, 72, 104)):
            kids.append(node("Runes_%d_%d" % (sgn, k), (px, y, 0), q_axis((0, 1, 0), 45), (14, 5, 14), (16, 0)))
        kids.append(node("Capital_%d" % sgn, (px, 127, 0), size=(26, 10, 26), uv=(16, 16)))
        for k, (h, ang) in enumerate(((60, 35), (85, 25), (110, 15))):   # ailes de glace sur l'exterieur
            kids.append(node("Wing_%d_%d" % (sgn, k), (sgn * (62 + 6 * k), h, 0), q_axis((0, 0, 1), -sgn * ang), (6, 34 - 4 * k, 6), (0, 0)))
    for i in range(15):                                   # arche principale
        t = math.pi * i / 14
        kids.append(node("Arch_%d" % i, (50 * math.cos(t), 132 + 50 * math.sin(t), 0), q_axis((0, 0, 1), math.degrees(t) + 90), (16, 14, 20), (0, 0)))
    for i in range(13):                                   # filet lumineux interieur
        t = math.pi * i / 12
        kids.append(node("ArchGlow_%d" % i, (40 * math.cos(t), 132 + 40 * math.sin(t), 0), q_axis((0, 0, 1), math.degrees(t) + 90), (11, 3, 8), (16, 0)))
    for i in range(9):                                    # couronne de pics de glace rayonnants
        ang = 20 + i * 140 / 8
        t = math.radians(ang); L = 22 + (10 if i % 2 == 0 else 0)
        kids.append(node("Ray_%d" % i, ((62 + L / 2) * math.cos(t), 132 + (62 + L / 2) * math.sin(t), 0), q_axis((0, 0, 1), ang - 90), (6, L, 6), (0, 0)))
    flake = [node("Flake_Core", (0, 0, 0), q_axis((0, 0, 1), 45), (9, 9, 9), (16, 0))]
    for k in range(3):                                    # embleme : flocon a 6 branches
        flake.append(node("Flake_Arm_%d" % k, (0, 0, 0), q_axis((0, 0, 1), k * 60), (4, 30, 4), (16, 0)))
    kids.append(node("Emblem", (0, 196, 0), children=flake))
    # Battants : la porte vanilla du chateau gele (Frozen_Castle/Door) agrandie a la taille de l'ouverture.
    # Convention blockymodel : la forme est decalee de "offset" par rapport au pivot du noeud, et les noeuds enfants
    # sont places par rapport au CENTRE de la forme du parent (pivot + offset). Le pivot de la porte vanilla est sa
    # charniere (offset x = +16 : la porte s'etend vers +x). Texture : a droite de celle du cadre dans l'atlas (UV +32).
    vdoor = json.load(open(os.path.join(src, "Common/Blocks/Decorative_Sets/Frozen_Castle/Door.blockymodel")))
    leaf_src = vdoor["nodes"][0]["children"][0]           # noeud "Door" (sous R-Attachment)
    LEAF_W, LEAF_H = 42, 112
    SX, SY = LEAF_W / 32, LEAF_H / 64                       # vanilla 32x64 -> battant 42x112
    def leaf(n, mirror, root=False):
        n = json.loads(json.dumps(n))
        _id[0] += 1; n["id"] = str(_id[0])
        p = n.get("position", {"x": 0, "y": 0, "z": 0})
        m = -1 if mirror else 1
        n["position"] = {"x": 0, "y": LEAF_H / 2, "z": 0} if root else {"x": m * p["x"] * SX, "y": p["y"] * SY, "z": p["z"]}
        sh = n.get("shape") or {}
        if sh.get("type") in ("box", "quad"):
            size = sh["settings"]["size"]
            sx = SX if root or size.get("x", 0) < 20 else LEAF_W / size["x"]   # panneaux : largeur exacte du battant
            sh["stretch"] = {"x": sx, "y": SY, "z": 1}
            o = sh.get("offset", {"x": 0, "y": 0, "z": 0})
            sh["offset"] = {"x": m * o.get("x", 0) * SX, "y": o.get("y", 0) * SY, "z": o.get("z", 0)}
            sh["shadingMode"] = "fullbright"
            for f in (sh.get("textureLayout") or {}).values():
                f["offset"]["x"] = f["offset"]["x"] + 32
        n["children"] = [leaf(c, mirror) for c in n.get("children", [])]
        return n
    def arch_fill(sign):
        """Partie haute du battant, en escalier sous l'arche (rayon interieur ~40, centre a y=132 du cadre)."""
        parts, steps = [], [(126, 132, 42)]
        for y0 in range(132, 172, 8):
            y1 = y0 + 8
            w = math.sqrt(max(0.0, 40 ** 2 - (y1 - 132) ** 2))
            if w > 3: steps.append((y0, y1, w))
        for i, (y0, y1, w) in enumerate(steps):
            parts.append(node("Arch_Fill_%d" % i, (sign * (LEAF_W - w / 2), (y0 + y1) / 2 - 14, 0), size=(round(w), y1 - y0, 5), uv=(0, 16)))
        return parts
    # Charnieres sur l'interieur des colonnes (x = -/+42) : le battant gauche s'etend vers +x, le droit vers -x
    kids.append(node("Door_L", (-42, 14, 0), children=[leaf(leaf_src, False, True)] + arch_fill(1)))
    kids.append(node("Door_R", (42, 14, 0), children=[leaf(leaf_src, True, True)] + arch_fill(-1)))
    save_model(os.path.join(N, "Gate.blockymodel"), node("Origin", (0, 0, 0), children=kids))
    # Atlas : cadre (32x32) + texture vanilla de la porte du chateau gele (96x96) a droite
    _, _, frame = png.read(os.path.join(N, "Gate.png"))
    dw, dh, door = png.read(os.path.join(src, "Common/Blocks/Decorative_Sets/Frozen_Castle/Door_Texture.png"))
    W, H = 32 + dw, max(32, dh)
    atlas = [[(frame[y][x] if x < 32 and y < 32 else door[y][x - 32] if x >= 32 and y < dh else [0, 0, 0, 0]) for x in range(W)] for y in range(H)]
    png.write_padded(os.path.join(N, "Gate.png"), W, H, atlas)
    def door_keys(sign):
        return [{"time": t, "delta": q_axis((0, 1, 0), sign * a), "interpolationType": "smooth"} for t, a in ((0, 0), (20, 0), (70, 105), (80, 105))]
    anim = {"formatVersion": 1, "duration": 80, "holdLastKeyframe": True, "nodeAnimations": {
        n: {"position": [], "orientation": door_keys(sg), "shapeStretch": [], "shapeVisible": [], "shapeUvOffset": []}
        for n, sg in (("Door_L", -1), ("Door_R", 1))}}
    json.dump(anim, open(os.path.join(N, "Gate_Open.blockyanim"), "w"), indent=2)
    # Vortex glace derriere les battants : portail de Gilgamesh recolore
    w2, h2, pp = png.read(os.path.join(out, "Items/Projectiles/Gilgamesh/Portal.png"))
    for row in pp:
        for p in row:
            if p[3] == 0: continue
            l = (p[1] - 70) / 175.0
            p[0], p[1], p[2] = 120 + 135 * l, 200 + 55 * l, 255
    png.write(os.path.join(N, "Portal_Ice.png"), w2, h2, pp)

    # ---------------- Stalactite de glace (modele vanilla Stalactite_Falling, texture de glace aux dimensions de Rock_Stone.png) ----------------
    import random, struct
    w, h = struct.unpack(">II", open(os.path.join(src, "Common/Items/Projectiles/Stalactite/Rock_Stone.png"), "rb").read()[16:24])
    rnd = random.Random(5)
    px = []
    for y in range(h):
        row = []
        for x in range(w):
            n = rnd.uniform(-1, 1)
            row.append([170 + 40 * n, 225 + 20 * n, 255, 240])
        px.append(row)
    png.write_padded(os.path.join(N, "Ice_Stalactite.png"), w, h, px)
    print("ok")


if __name__ == '__main__':
    main()
