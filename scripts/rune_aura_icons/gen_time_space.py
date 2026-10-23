"""Assets des sorts divins du temps et de l'espace : Sablier de Chronos, Abime du Chaos, Pont du Bifrost.
Usage : python3 gen_time_space.py <dossier_resources_Common>"""
import json, math, os, random, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import png
out = sys.argv[1]
N = 128; c = (N - 1) / 2
ID = {"x": 0, "y": 0, "z": 0, "w": 1}
_id = [1]
def q_axis(axis, deg):
    s = math.sin(math.radians(deg) / 2)
    return {"x": round(axis[0] * s, 6), "y": round(axis[1] * s, 6), "z": round(axis[2] * s, 6), "w": round(math.cos(math.radians(deg) / 2), 6)}
def face(uv): return {"offset": {"x": uv[0], "y": uv[1]}, "mirror": {"x": False, "y": False}, "angle": 0}
def box(name, pos, size, uv, orient=ID, bright=False):
    _id[0] += 1
    base = [max(1, min(16, round(s))) for s in size]
    return {"id": str(_id[0]), "name": name, "position": {"x": round(pos[0], 3), "y": round(pos[1], 3), "z": round(pos[2], 3)}, "orientation": orient,
            "shape": {"type": "box", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {k: round(size[i] / base[i], 4) for i, k in enumerate("xyz")},
                      "settings": {"size": {"x": base[0], "y": base[1], "z": base[2]}}, "textureLayout": {k: face(uv) for k in ("top", "bottom", "front", "back", "left", "right")},
                      "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": "fullbright" if bright else "standard"}, "children": []}
def quad(name, pos, size, uv, orient=ID, stretch=1.0, normal="+Y"):
    _id[0] += 1
    return {"id": str(_id[0]), "name": name, "position": {"x": pos[0], "y": pos[1], "z": pos[2]}, "orientation": orient,
            "shape": {"type": "quad", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": stretch, "y": stretch, "z": 1},
                      "settings": {"size": {"x": size[0], "y": size[1]}, "normal": normal}, "visible": True, "doubleSided": True,
                      "shadingMode": "fullbright", "unwrapMode": "custom", "textureLayout": {"front": face(uv)}}, "children": []}
def group(name, children, pos=(0, 0, 0)):
    _id[0] += 1
    return {"id": str(_id[0]), "name": name, "position": {"x": pos[0], "y": pos[1], "z": pos[2]}, "orientation": ID,
            "shape": {"type": "none", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": 1, "y": 1, "z": 1}, "settings": {"isPiece": False},
                      "visible": True, "doubleSided": False, "shadingMode": "standard", "unwrapMode": "custom", "textureLayout": {}}, "children": children}
def save(path, root): json.dump({"nodes": [root], "lod": "auto"}, open(path, "w"), indent=2)
def spin_anim(path, turns_by_node, axis=(0, 1, 0), steps=12):
    json.dump({"formatVersion": 1, "duration": 600, "holdLastKeyframe": False, "nodeAnimations": {
        n: {"position": [], "orientation": [{"time": round(600 * k / steps), "delta": q_axis(axis, 360 * t * k / steps), "interpolationType": "linear"} for k in range(steps + 1)],
            "shapeStretch": [], "shapeVisible": [], "shapeUvOffset": []} for n, t in turns_by_node.items()}}, open(path, "w"), indent=2)
def solid(px, x0, y0, col):
    for y in range(16):
        for x in range(16): px[y0 + y][x0 + x] = list(col)

# ======================= Sablier de Chronos =======================
T = os.path.join(out, "Items/Projectiles/Chronos"); os.makedirs(T, exist_ok=True)
# Cadran : anneau dore, 60 graduations, 12 heures (barres "romaines"), engrenage interieur, fond bleu nuit translucide
px = []
for y in range(N):
    row = []
    for x in range(N):
        d = math.hypot(x - c, y - c) / c; a = math.atan2(y - c, x - c)
        if d > 1: row.append([0, 0, 0, 0]); continue
        col, al = [40, 70, 130], 0.35
        if abs(d - 0.95) < 0.035 or abs(d - 0.62) < 0.015: col, al = [255, 215, 110], 1.0                    # anneaux
        tick = (a / (2 * math.pi) * 60) % 1
        if 0.86 < d < 0.92 and (tick < 0.12 or tick > 0.88): col, al = [255, 230, 160], 0.95                # minutes
        hour = (a / (2 * math.pi) * 12) % 1
        if 0.70 < d < 0.88 and (hour < 0.05 or hour > 0.95): col, al = [255, 225, 140], 1.0                 # heures
        gear = 0.5 + 0.5 * math.cos(a * 16)
        if 0.36 < d < 0.44 + 0.04 * gear: col, al = [210, 170, 80], 0.9                                     # engrenage
        if d < 0.08: col, al = [255, 235, 170], 1.0
        row.append(col + [255 * al])
    px.append(row)
png.write(os.path.join(T, "Clock_Face.png"), N, N, px)
# Atlas 32x32 : or (0,0), or sombre (16,0), verre bleute (0,16), sable (16,16)
atlas = [[[0, 0, 0, 0] for _ in range(32)] for _ in range(32)]
solid(atlas, 0, 0, (255, 205, 100, 255)); solid(atlas, 16, 0, (180, 130, 50, 255))
solid(atlas, 0, 16, (170, 220, 255, 150)); solid(atlas, 16, 16, (240, 205, 130, 255))
png.write(os.path.join(T, "Chronos.png"), 32, 32, atlas)
GOLD, DARK, GLASS, SAND = (0, 0), (16, 0), (0, 16), (16, 16)
# Aiguilles (echelle 14 comme le cadran : 1 unite = 0.22 bloc) : la grande fait 2 tours, la petite 1/6 de tour par cycle
hands = group("Origin", [group("Minute", [box("Minute_hand", (26, 0.6, 0), (52, 1, 3), GOLD, bright=True)]),
                         group("Hour", [box("Hour_hand", (17, 1.2, 0), (34, 1, 5), DARK, bright=True)]),
                         box("Hub", (0, 1.5, 0), (7, 2, 7), GOLD, q_axis((0, 1, 0), 45), bright=True)])
save(os.path.join(T, "Hands.blockymodel"), hands)
spin_anim(os.path.join(T, "Hands_Spin.blockyanim"), {"Minute": -2, "Hour": -1 / 6})
# Sablier (echelle 1 : 64 unites = 1 bloc)
hg = [box("Top", (0, 22, 0), (16, 2, 16), GOLD), box("Bottom", (0, -22, 0), (16, 2, 16), GOLD)]
for i in range(4):
    t = math.pi / 4 + i * math.pi / 2
    hg.append(box("Post_%d" % i, (6.5 * math.cos(t), 0, 6.5 * math.sin(t)), (1.5, 42, 1.5), DARK))
for k, (y, w) in enumerate(((17, 10), (12, 8), (7, 5), (3, 2.5), (-3, 2.5), (-7, 5), (-12, 8), (-17, 10))):
    hg.append(box("Glass_%d" % k, (0, y, 0), (w, 5, w), GLASS, bright=True))
hg += [box("Sand_top", (0, 12, 0), (5, 4, 5), SAND), box("Sand_stream", (0, 0, 0), (0.8, 12, 0.8), SAND, bright=True), box("Sand_bottom", (0, -16, 0), (8, 5, 8), SAND)]
save(os.path.join(T, "Hourglass.blockymodel"), group("Circle", hg))

# ======================= Abime du Chaos =======================
C = os.path.join(out, "Items/Projectiles/Chaos"); os.makedirs(C, exist_ok=True)
rnd = random.Random(5)
W = 160
atlas = [[[0, 0, 0, 0] for _ in range(W)] for _ in range(N)]
for y in range(N):                                                  # disque d'accretion (0..127)
    for x in range(N):
        d = math.hypot(x - c, y - c) / c; a = math.atan2(y - c, x - c)
        if d > 1 or d < 0.32: continue
        swirl = 0.5 + 0.5 * math.cos(a * 3 + d * 14)
        k = max(0.0, 1 - abs(d - 0.6) / 0.4) * (0.45 + 0.55 * swirl)
        hot = max(0.0, 1 - (d - 0.32) / 0.12)
        atlas[y][x] = [min(255, 120 + 130 * k + 120 * hot), min(255, 40 + 80 * k + 180 * hot), 255, 255 * min(1.0, k * 1.1 + hot)]
solid(atlas, 128, 0, (6, 2, 12, 255)); solid(atlas, 144, 0, (150, 70, 255, 255)); solid(atlas, 128, 16, (235, 225, 255, 255))
png.write(os.path.join(C, "Chaos.png"), W, N, atlas)
BLACK, PURPLE, WHITE = (128, 0), (144, 0), (128, 16)
# Singularite : sphere noire (couches), liseré violet, deux disques d'accretion (echelle 24 : 1 unite = 0.375 bloc)
sphere = []
for k, y in enumerate(range(-4, 5)):
    r = math.sqrt(max(0.5, 16.5 - y * y))
    sphere.append(box("Core_%d" % k, (0, y, 0), (2 * r, 1, 2 * r), BLACK, q_axis((0, 1, 0), 22.5 * (k % 2)), bright=True))
    sphere.append(box("Core_r%d" % k, (0, y, 0), (2 * r, 1, 2 * r), BLACK, q_axis((0, 1, 0), 45 + 22.5 * (k % 2)), bright=True))
sphere.append(box("Horizon", (0, 0, 0), (9.6, 0.5, 9.6), PURPLE, bright=True))
disk = group("Disk", [quad("Disk_q", (0, 0, 0), (N, N), (0, 0), stretch=0.22)])
disk2 = group("Disk2", [quad("Disk2_q", (0, 0, 0), (N, N), (0, 0), q_axis((1, 0, 0), 24), stretch=0.15)])
save(os.path.join(C, "Singularity.blockymodel"), group("Origin", [group("Core", sphere), disk, disk2]))
spin_anim(os.path.join(C, "Singularity_Spin.blockyanim"), {"Disk": 1.5, "Disk2": -2.5, "Core": 0.3})
# Faille : quad vertical en forme d'eclair, etoiles a l'interieur, bords violets (32x64)
rift = [[[0, 0, 0, 0] for _ in range(32)] for _ in range(64)]
for y in range(64):
    cx = 16 + 6 * math.sin(y * 0.35) + 3 * math.sin(y * 0.9)
    half = 2 + 5 * math.sin(math.pi * y / 63)
    for x in range(32):
        dx = abs(x - cx)
        if dx > half + 1.5: continue
        if dx > half: rift[y][x] = [180, 100, 255, 230]
        else:
            star = rnd.random() < 0.08
            rift[y][x] = [255, 255, 255, 255] if star else [12, 4, 28, 245]
png.write(os.path.join(C, "Rift.png"), 32, 64, rift)
save(os.path.join(C, "Rift.blockymodel"), group("Circle", [quad("Rift_a", (0, 32, 0), (32, 64), (0, 0), normal="+Z"),
                                                          quad("Rift_b", (0, 32, 0), (32, 64), (0, 0), q_axis((0, 1, 0), 90), normal="+Z")]))

# Pont du Bifrost : voir gen_time_space_v2.py (colonne de convocation runique)
print("ok")
