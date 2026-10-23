"""Assets des sorts divins (2e vague) : ciel etoile de Nyx, temple et soleil d'Apollon.
Usage : python3 gen_divine_v2.py <dossier_resources_Common>"""
import math, os, random, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import png
from gen_divine_models import q_axis, node, save_model, tex
out = sys.argv[1]
N = 128; c = (N - 1) / 2

# ---------------- Nyx : voute etoilee (disque) ----------------
D = os.path.join(out, "Items/Projectiles/Nyx"); os.makedirs(D, exist_ok=True)
rnd = random.Random(13)
stars = [(rnd.uniform(0, N), rnd.uniform(0, N), rnd.uniform(0.4, 1.6)) for _ in range(140)]
px = []
for y in range(N):
    row = []
    for x in range(N):
        d = math.hypot(x - c, y - c) / c
        if d > 1: row.append([0, 0, 0, 0]); continue
        neb = 0.5 + 0.5 * math.sin(x * 0.09 + math.sin(y * 0.07) * 2) * math.cos(y * 0.05)
        r, g, b = 10 + 30 * neb, 4 + 8 * neb, 22 + 45 * neb
        for sx, sy, s in stars:
            k = max(0.0, 1 - math.hypot(x - sx, y - sy) / s)
            r += 255 * k; g += 240 * k; b += 255 * k
        row.append([min(255, r), min(255, g), min(255, b), 255 * min(1.0, (1 - d) * 6)])
    px.append(row)
png.write(os.path.join(D, "Starry_Sky.png"), N, N, px)

# ---------------- Apollon : soleil rayonnant ----------------
A = os.path.join(out, "Items/Projectiles/Apollo"); os.makedirs(A, exist_ok=True)
px = []
for y in range(N):
    row = []
    for x in range(N):
        d = math.hypot(x - c, y - c) / c; a = math.atan2(y - c, x - c)
        if d > 1: row.append([0, 0, 0, 0]); continue
        disc = max(0.0, min(1.0, (0.42 - d) / 0.05 + 1)) if d < 0.47 else 0.0
        rays = (0.5 + 0.5 * math.cos(a * 16)) ** 6 * max(0.0, 1 - (d - 0.4) / 0.6) if d >= 0.4 else 0.0
        halo = math.exp(-((d - 0.45) / 0.18) ** 2) * 0.6
        k = min(1.0, disc + rays + halo)
        white = min(1.0, disc * (1 - d / 0.47) * 1.6)
        row.append([255, 190 + 65 * max(white, rays * 0.4), 60 + 190 * white, 255 * k])
    px.append(row)
png.write(os.path.join(A, "Sun.png"), N, N, px)

# ---------------- Apollon : temple (8 colonnes, echelle 4 : 16 unites = 1 bloc) ----------------
tex(os.path.join(A, "Temple.png"), {
    (0, 0): lambda u, v: [240 - 20 * v, 232 - 25 * v, 210 - 30 * v, 255],          # marbre
    (16, 0): lambda u, v: [255, 200 + 30 * (1 - v), 80 + 40 * (1 - v), 255],       # or
    (0, 16): lambda u, v: [255, 248, 200, 255],                                     # lumiere
    (16, 16): lambda u, v: [200, 150, 50, 255],                                     # or sombre
})
R = 192
kids = []
for i in range(8):
    t = 2 * math.pi * i / 8
    x, z = R * math.cos(t), R * math.sin(t)
    rot = q_axis((0, 1, 0), -math.degrees(t))
    kids.append(node("Plinth_%d" % i, (x, 4, z), rot, (26, 8, 26), (16, 16)))
    kids.append(node("Shaft_%d" % i, (x, 52, z), rot, (14, 88, 14), (0, 0)))
    for k, y in enumerate((20, 52, 84)):
        kids.append(node("Band_%d_%d" % (i, k), (x, y, z), rot, (16, 3, 16), (16, 0)))
    kids.append(node("Capital_%d" % i, (x, 100, z), rot, (26, 8, 26), (16, 0)))
for i in range(32):
    t = 2 * math.pi * i / 32
    rot = q_axis((0, 1, 0), -math.degrees(t) - 90)
    kids.append(node("Lintel_%d" % i, (R * math.cos(t), 108, R * math.sin(t)), rot, (40, 8, 20), (0, 0)))
    kids.append(node("Glow_%d" % i, (R * math.cos(t), 113, R * math.sin(t)), rot, (40, 2, 8), (0, 16)))
    kids.append(node("Step_%d" % i, ((R + 6) * math.cos(t), 1, (R + 6) * math.sin(t)), rot, (44, 2, 34), (16, 16)))
save_model(os.path.join(A, "Temple.blockymodel"), node("Origin", (0, 0, 0), children=kids))
print("ok")
