"""Assets 2e version : colonne de convocation runique du Bifrost, aiguilles a rebours de Chronos, lentille et debris du Chaos.
Usage : python3 gen_time_space_v2.py <dossier_resources_Common>"""
import json, math, os, random, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import png
from gen_time_space import box, quad, group, save, spin_anim, q_axis, solid, N, c
out = sys.argv[1]
rnd = random.Random(9)

# ======================= Bifrost : cercle runique et colonne de convocation =======================
B = os.path.join(out, "Items/Projectiles/Bifrost"); os.makedirs(B, exist_ok=True)
def rune_glyph(gx, gy, size=1.0):
    """Petits glyphes : segments aleatoires dans une case."""
    segs = []
    for _ in range(3):
        a, b = (rnd.uniform(-0.5, 0.5), rnd.uniform(-0.6, 0.6)), (rnd.uniform(-0.5, 0.5), rnd.uniform(-0.6, 0.6))
        segs.append((a, b))
    segs.append(((0, -0.6), (0, 0.6)))
    return segs
def seg_d(px_, py_, a, b):
    vx, vy = b[0] - a[0], b[1] - a[1]; wx, wy = px_ - a[0], py_ - a[1]
    t = max(0.0, min(1.0, (wx * vx + wy * vy) / (vx * vx + vy * vy or 1)))
    return math.hypot(px_ - (a[0] + vx * t), py_ - (a[1] + vy * t))
GLYPHS = [rune_glyph(0, 0) for _ in range(24)]
def circle_pixel(x, y):
    d = math.hypot(x - c, y - c) / c; a = math.atan2(y - c, x - c)
    if d > 1: return [0, 0, 0, 0]
    k = 0.0
    for r, w in ((0.97, 0.02), (0.84, 0.012), (0.56, 0.015), (0.30, 0.01)):
        k = max(k, max(0.0, 1 - abs(d - r) / w))
    if 0.86 < d < 0.95:                                         # anneau de runes
        idx = int((a + math.pi) / (2 * math.pi) * 24) % 24
        la = ((a + math.pi) / (2 * math.pi) * 24) % 1
        gx, gy = (la - 0.5) * 1.6, ((d - 0.905) / 0.045) * 0.9
        if min(seg_d(gx, gy, s0, s1) for s0, s1 in GLYPHS[idx]) < 0.12: k = 1.0
    for i in range(6):                                          # hexagramme
        a0 = i * math.pi / 3 - math.pi / 2; a1 = a0 + 2 * math.pi / 3
        p0 = (math.cos(a0) * 0.84, math.sin(a0) * 0.84); p1 = (math.cos(a1) * 0.84, math.sin(a1) * 0.84)
        if seg_d((x - c) / c, (y - c) / c, p0, p1) < 0.008: k = 1.0
    glow = 0.15 * max(0.0, 1 - d)
    a_ = min(1.0, k + glow)
    return [255, 200 + 50 * k, 110 + 130 * k, 255 * a_]
circle = [[circle_pixel(x, y) for x in range(N)] for y in range(N)]
png.write(os.path.join(B, "Rune_Circle.png"), N, N, circle)
# Atlas de la colonne (192x128) : cercle (0..127), faisceau runique (128..143, 64 de haut), glyphes 16x16 (144.., 160.., 176..)
W = 192
atlas = [[[0, 0, 0, 0] for _ in range(W)] for _ in range(N)]
for y in range(N):
    for x in range(N): atlas[y][x] = circle[y][x]
for y in range(64):
    for x in range(16):
        u = abs(x + 0.5 - 8) / 8; v = y / 63
        band = 1.0 if (y % 12) in (0, 1) and u < 0.6 else 0.0      # bandes runiques le long du faisceau
        a = max(0.0, 1 - u ** 1.8) * (0.25 + 0.75 * v) * 0.8
        atlas[y][128 + x] = [255, 235 + 20 * band, 180 + 75 * band, 255 * min(1.0, a + band * 0.5)]
for gi in range(6):
    ox, oy = 144 + (gi % 3) * 16, (gi // 3) * 16
    segs = GLYPHS[gi]
    for y in range(16):
        for x in range(16):
            gx, gy = (x + 0.5 - 8) / 8 * 0.7, (y + 0.5 - 8) / 8 * 0.7
            dd = min(seg_d(gx, gy, s0, s1) for s0, s1 in segs)
            if dd < 0.14: atlas[oy + y][ox + x] = [255, 245, 210, 255]
            elif dd < 0.3: atlas[oy + y][ox + x] = [255, 200, 90, 120]
png.write(os.path.join(B, "Column.png"), W, N, atlas)
# Colonne (echelle 4 : 16 unites = 1 bloc) : faisceau, 3 cercles a 1 / 5 / 9 blocs, 8 runes en orbite a 4 blocs
beam = [quad("Beam_%d" % i, (0, 320, 0), (16, 64), (128, 0), q_axis((0, 1, 0), yaw), 1.0, "+Z") for i, yaw in enumerate((0, 90, 45, 135))]
for b in beam: b["shape"]["stretch"] = {"x": 1.5, "y": 10, "z": 1}
circles = [group("Circle_%d" % (i + 1), [quad("Circle_%d_q" % (i + 1), (0, 0, 0), (N, N), (0, 0), stretch=st)], (0, h, 0))
           for i, (h, st) in enumerate(((1, 0.75), (80, 0.5625), (144, 0.375)))]
runes = []
for i in range(8):
    t = 2 * math.pi * i / 8
    gi = i % 6
    runes.append(quad("Rune_%d" % i, (round(48 * math.cos(t), 3), 64, round(48 * math.sin(t), 3)), (16, 16),
                      (144 + (gi % 3) * 16, (gi // 3) * 16), q_axis((0, 1, 0), -math.degrees(t) + 90), 1.2, "+Z"))
save(os.path.join(B, "Column.blockymodel"), group("Origin", beam + circles + [group("Runes", runes)]))
spin_anim(os.path.join(B, "Column_Spin.blockyanim"), {"Circle_1": 1, "Circle_2": -1.5, "Circle_3": 2, "Runes": -0.75})

# ======================= Chronos : aiguilles qui tournent a rebours, tres vite =======================
T = os.path.join(out, "Items/Projectiles/Chronos")
spin_anim(os.path.join(T, "Hands_Rewind.blockyanim"), {"Minute": 10, "Hour": 3})

# ======================= Chaos : lentille gravitationnelle + debris =======================
C = os.path.join(out, "Items/Projectiles/Chaos")
w, h, px = png.read(os.path.join(C, "Chaos.png"))
W2 = 288
atlas = [[(px[y][x] if x < w else [0, 0, 0, 0]) for x in range(W2)] for y in range(N)]
for y in range(N):                                                # anneau fin et brillant (160..287)
    for x in range(N):
        d = math.hypot(x - c, y - c) / c
        k = max(0.0, 1 - abs(d - 0.9) / 0.05) + 0.4 * max(0.0, 1 - abs(d - 0.9) / 0.15)
        if k > 0.01: atlas[y][160 + x] = [230, 215, 255, 255 * min(1.0, k)]
png.write(os.path.join(C, "Chaos.png"), W2, N, atlas)
m = json.load(open(os.path.join(C, "Singularity.blockymodel")))
root = m["nodes"][0]
lens = group("Lens", [quad("Lens_%d" % i, (0, 0, 0), (N, N), (160, 0), q_axis(ax, deg), 0.09) for i, (ax, deg) in
                      enumerate((((1, 0, 0), 90), ((0, 0, 1), 90), ((1, 0, 0), 45), ((1, 0, 0), -45)))])
root["children"] = [ch for ch in root["children"] if ch["name"] != "Lens"] + [lens]
json.dump(m, open(os.path.join(C, "Singularity.blockymodel"), "w"), indent=2)
spin_anim(os.path.join(C, "Singularity_Spin.blockyanim"), {"Disk": 1.5, "Disk2": -2.5, "Core": 0.3, "Lens": 0.5})
# Debris : cube de roche et de terre (echelle 4 : 16 unites = 1 bloc)
deb = [[[0, 0, 0, 0] for _ in range(32)] for _ in range(32)]
for y in range(32):
    for x in range(32):
        n = rnd.random()
        if x < 16 and y < 16: deb[y][x] = [110 + 40 * n, 95 + 30 * n, 80 + 25 * n, 255]          # roche
        elif x >= 16 and y < 16: deb[y][x] = [95 + 30 * n, 70 + 20 * n, 45 + 15 * n, 255]       # terre
save_path = os.path.join(C, "Debris.blockymodel")
png.write(os.path.join(C, "Debris.png"), 32, 32, deb)
save(save_path, group("Origin", [box("Rock", (0, 0, 0), (16, 16, 16), (0, 0)), box("Dirt", (3, 3, 2), (10, 9, 12), (16, 0), q_axis((0, 1, 0), 30))]))
print("ok")
