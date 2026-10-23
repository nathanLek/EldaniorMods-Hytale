"""Sanctuaire d'Apollon : tholos grec (temple circulaire) realiste, colonne de lumiere, cercle runique au sol.
Toutes les boites ont une taille de base <= 16 (zone de texture 16x16) et sont agrandies par "stretch" pour que la
texture ne deborde jamais de sa zone. Echelle du temple : 4 (16 unites = 1 bloc).
Usage : python3 gen_apollo.py <dossier_resources_Common> [<dossier_contenant_Common/...extrait d'Assets.zip> pour la statue]"""
import json, math, os, random, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import png
out = sys.argv[1]
A = os.path.join(out, "Items/Projectiles/Apollo"); os.makedirs(A, exist_ok=True)
rnd = random.Random(21)

# ---------------- Texture 64x64 (zones de 16x16) ----------------
def marble(u, v, base, vein):
    n = math.sin(u * 9 + math.sin(v * 7) * 2.5) * math.sin(v * 3 + u * 2)
    k = max(0.0, 1 - abs(n) * 6) * 0.35
    return [base[i] - vein[i] * k + rnd.uniform(-4, 4) for i in range(3)] + [255]
REGIONS = {
    (0, 0): lambda u, v: marble(u, v, (246, 241, 230), (70, 60, 50)),          # marbre blanc veine
    (16, 0): lambda u, v: marble(u, v, (214, 206, 190), (60, 50, 40)),         # marbre ombre (cannelures)
    (32, 0): lambda u, v: [255, 205 + 30 * (1 - v), 90 + 50 * (1 - v), 255],  # or
    (48, 0): lambda u, v: [255, 250, 215, 255],                                # lumiere
    (0, 16): lambda u, v: marble(u, v, (230, 222, 205), (80, 60, 40)),         # dallage
    (16, 16): lambda u, v: [190, 140, 50, 255],                                # or sombre
    (32, 16): lambda u, v: [160, 95, 45, 255],                                 # bronze
    (48, 16): lambda u, v: [40, 70, 160, 255],                                 # lapis (frise)
    (0, 32): lambda u, v: [110 + 60 * (1 - v), 160 + 40 * (1 - v), 60, 255],   # laurier
    (16, 32): lambda u, v: [60, 100, 35, 255],                                 # feuillage sombre
    (32, 32): lambda u, v: [255, 160, 50, 255],                                # braise
}
UV = {"laurel": (0, 32), "leaf": (16, 32), "ember": (32, 32), "marble": (0, 0), "shade": (16, 0), "gold": (32, 0), "light": (48, 0), "floor": (0, 16), "darkgold": (16, 16), "bronze": (32, 16), "lapis": (48, 16)}
px = [[[0, 0, 0, 0] for _ in range(64)] for _ in range(96)]
for (x0, y0), fn in REGIONS.items():
    for y in range(16):
        for x in range(16):
            px[y0 + y][x0 + x] = fn(x / 15, y / 15)
png.write(os.path.join(A, "Temple.png"), 64, 96, px)

# ---------------- Outils de modele ----------------
_id = [1]
def q_axis(axis, deg):
    s = math.sin(math.radians(deg) / 2)
    return {"x": axis[0] * s, "y": axis[1] * s, "z": axis[2] * s, "w": math.cos(math.radians(deg) / 2)}
def qmul(a, b):
    return {"x": a["w"]*b["x"]+a["x"]*b["w"]+a["y"]*b["z"]-a["z"]*b["y"], "y": a["w"]*b["y"]-a["x"]*b["z"]+a["y"]*b["w"]+a["z"]*b["x"],
            "z": a["w"]*b["z"]+a["x"]*b["y"]-a["y"]*b["x"]+a["z"]*b["w"], "w": a["w"]*b["w"]-a["x"]*b["x"]-a["y"]*b["y"]-a["z"]*b["z"]}
ID = {"x": 0, "y": 0, "z": 0, "w": 1}
def r6(q): return {k: round(v, 6) for k, v in q.items()}
def face(uv): return {"offset": {"x": uv[0], "y": uv[1]}, "mirror": {"x": False, "y": False}, "angle": 0}
def box(name, pos, size, mat, orient=ID):
    """Boite de taille monde `size` : taille de base <= 16 (zone de texture), agrandie par stretch."""
    _id[0] += 1
    base = [max(1, min(16, round(s))) for s in size]
    stretch = {k: round(size[i] / base[i], 4) for i, k in enumerate("xyz")}
    return {"id": str(_id[0]), "name": name, "position": {"x": round(pos[0], 3), "y": round(pos[1], 3), "z": round(pos[2], 3)},
            "orientation": r6(orient),
            "shape": {"type": "box", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": stretch,
                      "settings": {"size": {"x": base[0], "y": base[1], "z": base[2]}},
                      "textureLayout": {k: face(UV[mat]) for k in ("top", "bottom", "front", "back", "left", "right")},
                      "unwrapMode": "custom", "visible": True, "doubleSided": False,
                      "shadingMode": "fullbright" if mat in ("light", "ember") else "standard"},   # le reste est eclaire par le monde
            "children": []}
def group(name, children, pos=(0, 0, 0)):
    _id[0] += 1
    return {"id": str(_id[0]), "name": name, "position": {"x": pos[0], "y": pos[1], "z": pos[2]}, "orientation": ID,
            "shape": {"type": "none", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": 1, "y": 1, "z": 1}, "settings": {"isPiece": False},
                      "visible": True, "doubleSided": False, "shadingMode": "standard", "unwrapMode": "custom", "textureLayout": {}},
            "children": children}
def ring(name, radius, y, height, depth, mat, seg_len=24, alt=None):
    """Anneau de boites tangentes (largeur radiale `depth`). alt : matiere alternee (frise)."""
    n = max(8, int(2 * math.pi * radius / seg_len))
    L = 2 * math.pi * radius / n * 1.04
    parts = []
    for i in range(n):
        t = 2 * math.pi * i / n
        m = alt if (alt and i % 2) else mat
        parts.append(box("%s_%d" % (name, i), (radius * math.cos(t), y, radius * math.sin(t)), (L, height, depth), m, q_axis((0, 1, 0), -math.degrees(t) - 90)))
    return parts

R = 192                                   # rayon des colonnes (12 blocs)
kids = []
# Soubassement : 3 marches + dallage en mosaique (anneaux concentriques)
for k, (r, h) in enumerate(((R + 30, 3), (R + 20, 6), (R + 10, 9))):
    kids += ring("Step%d" % k, r, h / 2, h, 12, "shade" if k % 2 else "marble", seg_len=44)
for k, r in enumerate(range(20, R + 8, 40)):
    kids += ring("Floor%d" % k, r, 9.5, 1, 40, "floor" if k % 2 else "gold", seg_len=56)
for i in range(12):                                        # soleil de mosaique au centre (rayons dores)
    t = 2 * math.pi * i / 12
    kids.append(box("Mosaic_ray_%d" % i, (40 * math.cos(t), 10.1, 40 * math.sin(t)), (44, 0.6, 6), "gold", q_axis((0, 1, 0), -math.degrees(t))))
kids += ring("Mosaic_disc", 18, 10.2, 0.6, 14, "gold", seg_len=12)
# 16 colonnes : base moulee, fut octogonal cannele, chapiteau dorique
for i in range(16):
    t = 2 * math.pi * i / 16
    x, z = R * math.cos(t), R * math.sin(t)
    yaw = -math.degrees(t)
    col = [box("Base_a", (0, 12, 0), (24, 4, 24), "marble"), box("Base_b", (0, 15, 0), (20, 3, 20), "marble", q_axis((0, 1, 0), 45)),
           box("Shaft_a", (0, 60, 0), (13, 90, 13), "marble"), box("Shaft_b", (0, 60, 0), (13, 90, 13), "shade", q_axis((0, 1, 0), 30)),
           box("Shaft_c", (0, 60, 0), (13, 90, 13), "marble", q_axis((0, 1, 0), 60)),
           box("Necking", (0, 105, 0), (15, 2, 15), "gold"),
           box("Echinus", (0, 108, 0), (19, 4, 19), "marble", q_axis((0, 1, 0), 22.5)), box("Abacus", (0, 112, 0), (24, 4, 24), "marble")]
    g = group("Column_%d" % i, col, (round(x, 3), 0, round(z, 3)))
    g["orientation"] = r6(q_axis((0, 1, 0), yaw))
    kids.append(g)
# Entablement : architrave, frise (metopes dorees / lapis), corniche en debord
kids += ring("Architrave", R, 118, 8, 24, "marble", seg_len=40)
kids += ring("Frieze", R, 126, 8, 22, "marble", seg_len=30, alt="gold")
kids += ring("Cornice", R, 132, 4, 32, "shade", seg_len=40)
kids += ring("Cornice_gold", R + 14, 134.5, 1, 6, "gold", seg_len=48)
# Denticules sous la corniche et acroteres dores au-dessus de chaque colonne
kids += ring("Dentil", R + 13, 129.5, 3, 4, "marble", seg_len=20)
for i in range(16):
    t = 2 * math.pi * i / 16
    x, z = (R + 10) * math.cos(t), (R + 10) * math.sin(t)
    kids += [box("Acroterion_%d" % i, (x, 139, z), (5, 9, 5), "gold", q_axis((0, 1, 0), 45 - math.degrees(t))),
             box("Acroterion_top_%d" % i, (x, 145, z), (3, 4, 3), "light", q_axis((0, 1, 0), -math.degrees(t)))]
# Tambour (mur circulaire) perce de 16 fenetres a encadrement dore
DRUM_R, DRUM_Y0, DRUM_Y1 = R - 8, 134, 160
for i in range(32):
    t = 2 * math.pi * i / 32
    orient = q_axis((0, 1, 0), -math.degrees(t) - 90)
    L = 2 * math.pi * DRUM_R / 32 * 1.04
    pos = (DRUM_R * math.cos(t), (DRUM_Y0 + DRUM_Y1) / 2, DRUM_R * math.sin(t))
    if i % 2:
        kids += [box("Window_%d" % i, pos, (L, DRUM_Y1 - DRUM_Y0 - 8, 6), "light", orient),
                 box("Window_frame_%d" % i, (pos[0], DRUM_Y0 + 3, pos[2]), (L, 2, 12), "gold", orient),
                 box("Window_top_%d" % i, (pos[0], DRUM_Y1 - 3, pos[2]), (L, 2, 12), "gold", orient)]
    else:
        kids.append(box("Drum_%d" % i, pos, (L, DRUM_Y1 - DRUM_Y0, 12), "marble", orient))
kids += ring("Drum_cap", DRUM_R, DRUM_Y1 + 1.5, 3, 18, "shade", seg_len=40)
# Coupole : 16 nervures dorees et caissons de marbre entre elles, jusqu'a l'oculus (le soleil y brille)
OCULUS, DOME_Y0, TOP = 44, DRUM_Y1 + 3, 250
def meridian(k, n=6):
    a = (math.pi / 2) * k / n
    return (OCULUS + (DRUM_R - OCULUS) * math.cos(a), DOME_Y0 + (TOP - DOME_Y0) * math.sin(a))
for i in range(16):
    for k in range(6):
        (r0, y0), (r1, y1) = meridian(k), meridian(k + 1)
        L = math.hypot(r1 - r0, y1 - y0) * 1.06
        alpha = math.degrees(math.atan2(y1 - y0, r0 - r1))
        rm, ym = (r0 + r1) / 2, (y0 + y1) / 2
        for half, (phi, width, mat, thick) in enumerate(((2 * math.pi * i / 16, 7, "gold", 7),                                   # nervure
                                                          (2 * math.pi * (i + 0.5) / 16, 2 * math.pi * rm / 16 * 1.02, "shade" if k % 2 else "marble", 3))):  # caisson
            orient = qmul(q_axis((0, 1, 0), -math.degrees(phi)), q_axis((0, 0, 1), -alpha))
            kids.append(box("Dome_%d_%d_%d" % (i, k, half), (rm * math.cos(phi), ym, rm * math.sin(phi)), (L, thick, width), mat, orient))
kids += ring("Oculus", OCULUS, TOP, 6, 10, "gold", seg_len=12)
kids += ring("Oculus_light", OCULUS - 4, TOP - 3, 2, 4, "light", seg_len=12)
# Braseros entre les colonnes (flamme attachee aux noeuds "Brazier_Flame_i") et guirlandes de laurier
for i in range(16):
    t = 2 * math.pi * (i + 0.5) / 16
    x, z = (R - 14) * math.cos(t), (R - 14) * math.sin(t)
    kids += [box("Brazier_leg_%d" % i, (x, 15, z), (3, 12, 3), "bronze"), box("Brazier_bowl_%d" % i, (x, 22, z), (10, 3, 10), "bronze", q_axis((0, 1, 0), 45)),
             box("Brazier_coal_%d" % i, (x, 23.5, z), (7, 1, 7), "ember")]
    kids.append(group("Brazier_Flame_%d" % i, [], (round(x, 3), 25, round(z, 3))))
    a0, a1 = 2 * math.pi * i / 16, 2 * math.pi * (i + 1) / 16
    p0 = (R * math.cos(a0), 100, R * math.sin(a0)); p1 = (R * math.cos(a1), 100, R * math.sin(a1))
    pts = []
    for k in range(4):                                                   # chainette qui pend entre deux colonnes
        u = k / 3
        pts.append((p0[0] + (p1[0] - p0[0]) * u, 100 - 14 * math.sin(math.pi * u), p0[2] + (p1[2] - p0[2]) * u))
    for k in range(3):
        (xa, ya, za), (xb, yb, zb) = pts[k], pts[k + 1]
        L = math.dist((xa, ya, za), (xb, yb, zb)) * 1.1
        yaw = -math.degrees(math.atan2(zb - za, xb - xa))
        tilt = math.degrees(math.atan2(yb - ya, math.hypot(xb - xa, zb - za)))
        kids.append(box("Garland_%d_%d" % (i, k), ((xa + xb) / 2, (ya + yb) / 2, (za + zb) / 2), (L, 4, 5), "laurel" if k % 2 == 0 else "leaf",
                        qmul(q_axis((0, 1, 0), yaw), q_axis((0, 0, 1), tilt))))
# Autel central et sa vasque de bronze (flamme eternelle attachee au noeud "Altar_Flame")
kids += [box("Altar_base", (0, 14, 0), (26, 10, 26), "marble"), box("Altar_mid", (0, 22, 0), (20, 6, 20), "marble", q_axis((0, 1, 0), 45)),
         box("Altar_top", (0, 27, 0), (28, 4, 28), "gold"), box("Altar_bowl", (0, 31, 0), (16, 4, 16), "bronze", q_axis((0, 1, 0), 45))]
kids.append(group("Altar_Flame", [], (0, 34, 0)))
json.dump({"nodes": [group("Origin", kids)], "lod": "auto"}, open(os.path.join(A, "Temple.blockymodel"), "w"), separators=(",", ":"))
print("temple : %d pieces" % (_id[0]))

# ---------------- Colonne de lumiere (deux quads croises, echelle 4) ----------------
bw, bh = 16, 64
bpx = []
for y in range(bh):
    row = []
    for x in range(32):
        if x >= bw: row.append([0, 0, 0, 0]); continue
        u = abs(x + 0.5 - bw / 2) / (bw / 2); v = y / (bh - 1)            # v = 0 en haut
        a = max(0.0, 1 - u ** 1.6) * (0.25 + 0.75 * v)
        core = max(0.0, 1 - u * 3)
        row.append([255, 236 + 19 * core, 170 + 85 * core, 255 * min(1.0, a) * 0.7])
    bpx.append(row)
png.write(os.path.join(A, "Beam.png"), 32, bh, bpx)
def quad(name, yaw, w, h):
    _id[0] += 1
    return {"id": str(_id[0]), "name": name, "position": {"x": 0, "y": h / 2, "z": 0}, "orientation": r6(q_axis((0, 1, 0), yaw)),
            "shape": {"type": "quad", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": w / bw, "y": h / bh, "z": 1},
                      "settings": {"size": {"x": bw, "y": bh}, "normal": "+Z"}, "visible": True, "doubleSided": True,
                      "shadingMode": "fullbright", "unwrapMode": "custom", "textureLayout": {"front": face((0, 0))}}, "children": []}
json.dump({"nodes": [group("Circle", [quad("Beam_A", 0, 40, 640), quad("Beam_B", 90, 40, 640), quad("Beam_C", 45, 24, 640), quad("Beam_D", 135, 24, 640)])], "lod": "auto"},
          open(os.path.join(A, "Beam.blockymodel"), "w"), indent=2)

# ---------------- Cercle runique au sol (quad 128) : frise grecque, soleils ----------------
N = 128; c = (N - 1) / 2
fpx = []
for y in range(N):
    row = []
    for x in range(N):
        d = math.hypot(x - c, y - c) / c; a = math.atan2(y - c, x - c)
        if d > 1: row.append([0, 0, 0, 0]); continue
        band = 0.86 < d < 0.97
        meander = band and ((int((a + math.pi) / (2 * math.pi) * 48) + int((d - 0.86) / 0.11 * 3)) % 2 == 0)
        rim = abs(d - 0.86) < 0.012 or abs(d - 0.97) < 0.012 or abs(d - 0.55) < 0.01
        sun = 0.0
        for k in range(8):
            sa = k * math.pi / 4
            sx, sy = c + math.cos(sa) * c * 0.72, c + math.sin(sa) * c * 0.72
            sd = math.hypot(x - sx, y - sy) / c
            sun = max(sun, max(0.0, 1 - sd / 0.06), 0.6 * (0.5 + 0.5 * math.cos(math.atan2(y - sy, x - sx) * 8)) ** 8 * max(0.0, 1 - sd / 0.1))
        glow = 0.12 * (1 - d)
        k = 1.0 if (meander or rim) else min(1.0, sun + glow)
        row.append([255, 215 + 30 * k, 110 + 100 * k, 255 * min(1.0, k * 0.9)])
    fpx.append(row)
png.write(os.path.join(A, "Floor_Ring.png"), N, N, fpx)
# ---------------- Couronne de laurier (echelle 1 : 64 unites = 1 bloc) ----------------
tex_l = [[[0, 0, 0, 0] for _ in range(32)] for _ in range(32)]
for y in range(16):
    for x in range(16):
        tex_l[y][x] = [120 + 70 * (1 - y / 15), 175 + 40 * (1 - y / 15), 70, 255]          # feuille
        tex_l[y][x + 16] = [255, 210 + 30 * (1 - y / 15), 90, 255]                          # or
png.write(os.path.join(A, "Laurel.png"), 32, 32, tex_l)
leaves = []
def lbox(name, pos, size, uv, orient):
    _id[0] += 1
    return {"id": str(_id[0]), "name": name, "position": {"x": round(pos[0], 3), "y": round(pos[1], 3), "z": round(pos[2], 3)}, "orientation": r6(orient),
            "shape": {"type": "box", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": 1, "y": 1, "z": 1}, "settings": {"size": {"x": size[0], "y": size[1], "z": size[2]}},
                      "textureLayout": {k: face(uv) for k in ("top", "bottom", "front", "back", "left", "right")},
                      "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": "fullbright"}, "children": []}
for i in range(18):
    t = 2 * math.pi * i / 18
    for tier, (r, y, tilt) in enumerate(((19, 0, 35), (17, 3, -35))):
        orient = qmul(q_axis((0, 1, 0), -math.degrees(t) - 90), q_axis((1, 0, 0), tilt))
        leaves.append(lbox("Leaf_%d_%d" % (i, tier), (r * math.cos(t), y, r * math.sin(t)), (7, 2, 4), (0, 0), orient))
for i in range(12):
    t = 2 * math.pi * i / 12
    leaves.append(lbox("Band_%d" % i, (18 * math.cos(t), 1.5, 18 * math.sin(t)), (11, 2, 2), (16, 0), q_axis((0, 1, 0), -math.degrees(t) - 90)))
json.dump({"nodes": [group("Circle", leaves)], "lod": "auto"}, open(os.path.join(A, "Laurel.blockymodel"), "w"), indent=2)

# ---------------- Statue d'Apollon sculptee (marbre et or, echelle 4, texture du temple) ----------------
st = []
def sb(name, pos, size, mat, orient=ID): st.append(box(name, pos, size, mat, orient))
sb("Pedestal", (0, 4, 0), (20, 8, 20), "marble"); sb("Pedestal_band", (0, 8.5, 0), (22, 1, 22), "gold")
sb("Robe_low", (0, 15, 0), (14, 12, 10), "marble"); sb("Robe_hem", (0, 9.5, 0), (15, 1.5, 11), "gold")
sb("Robe_fold_a", (-4, 15, 5.2), (2, 12, 1), "shade"); sb("Robe_fold_b", (3, 15, 5.2), (2, 12, 1), "shade")
sb("Robe_high", (0, 26, 0), (12, 10, 8), "marble"); sb("Belt", (0, 30.5, 0), (12.5, 1.5, 8.5), "gold")
sb("Torso", (0, 37, 0), (12, 12, 7), "marble"); sb("Sash", (0, 37, 3.8), (2.5, 15, 1), "gold", q_axis((0, 0, 1), 32))
sb("Cape", (0, 28, -4.6), (13, 26, 1), "shade")
sb("Neck", (0, 44.5, 0), (4, 3, 4), "marble"); sb("Head", (0, 50, 0), (8, 9, 8), "marble")
sb("Hair", (0, 54, -0.5), (9, 4, 9), "gold"); sb("Hair_back", (0, 50, -4.2), (9, 8, 1.5), "gold")
for i in range(10):
    t = 2 * math.pi * i / 10
    sb("Laurel_%d" % i, (5 * math.cos(t), 53, 5 * math.sin(t)), (3, 1.5, 2), "laurel", q_axis((0, 1, 0), -math.degrees(t) - 90))
# bras droit leve, tenant un soleil
sb("R_arm", (8, 46, 0), (4, 11, 4), "marble", q_axis((0, 0, 1), -20)); sb("R_forearm", (10.5, 56, 0), (3.5, 10, 3.5), "marble", q_axis((0, 0, 1), -8))
sb("R_hand", (11.5, 62, 0), (4, 4, 4), "marble")
sb("Sun_disc", (12, 69, 0), (10, 10, 2), "gold"); sb("Sun_core", (12, 69, 1.2), (6, 6, 1), "light")
for i in range(8):
    t = 2 * math.pi * i / 8
    sb("Sun_ray_%d" % i, (12 + 8 * math.cos(t), 69 + 8 * math.sin(t), 0), (5, 1.5, 1), "gold", q_axis((0, 0, 1), math.degrees(t)))
# bras gauche le long du corps, tenant une lyre
sb("L_arm", (-7.5, 38, 0), (4, 11, 4), "marble", q_axis((0, 0, 1), 10)); sb("L_forearm", (-8.5, 30, 3), (3.5, 9, 3.5), "marble", q_axis((1, 0, 0), -50))
sb("Lyre_arm_a", (-11, 30, 7), (1.5, 12, 1.5), "gold", q_axis((0, 0, 1), 12)); sb("Lyre_arm_b", (-5, 30, 7), (1.5, 12, 1.5), "gold", q_axis((0, 0, 1), -12))
sb("Lyre_bar", (-8, 36, 7), (9, 1.5, 1.5), "gold"); sb("Lyre_base", (-8, 24.5, 7), (6, 2, 2), "gold")
for i in range(4):
    sb("Lyre_string_%d" % i, (-9.5 + i, 30, 7), (0.4, 10, 0.4), "light")
# Ailes d'ange deployees dans le dos : deux eventails de plumes de marbre bordees d'or (3 rangs)
for side in (-1, 1):
    shoulder = (side * 3.5, 42, -5.5)
    for row, (count, base_len, step, a0, a1, mat, depth) in enumerate((
            (9, 14, 2.2, 5, 115, "marble", 0.0),       # remiges (grandes plumes)
            (7, 10, 1.4, 15, 105, "light", 0.6),       # couvertures
            (5, 6, 0.8, 25, 95, "gold", 1.2))):        # petites plumes dorees pres de l'epaule
        for k in range(count):
            u = k / (count - 1)
            ang = a0 + (a1 - a0) * u                    # 0 = horizontal vers l'exterieur, 90 = vers le haut
            L = base_len + step * (count - 1) * math.sin(math.pi * u * 0.9)
            d = (side * math.cos(math.radians(ang)), math.sin(math.radians(ang)))
            orient = qmul(q_axis((1, 0, 0), 12), q_axis((0, 0, 1), ang if side > 0 else 180 - ang))
            sb("Wing_%d_%d_%d" % (side, row, k), (shoulder[0] + d[0] * L / 2, shoulder[1] + d[1] * L / 2, shoulder[2] - 1 - k * 0.15 + depth),
               (L, 2.4, 1.2), mat if not (row == 0 and k % 2) else "shade", orient)
            if row == 0:                                # liseré dore au bout des grandes plumes
                sb("Wing_tip_%d_%d" % (side, k), (shoulder[0] + d[0] * (L - 1), shoulder[1] + d[1] * (L - 1), shoulder[2] - 1 - k * 0.15), (2.2, 2.6, 1.4), "gold", orient)
json.dump({"nodes": [group("Origin", st)], "lod": "auto"}, open(os.path.join(A, "Statue.blockymodel"), "w"), indent=2)
print("statue : %d pieces" % len(st))

# ---------------- (ancien) texture or du mannequin : plus utilisee ----------------
if False and len(sys.argv) > 2:
    w, h, spx = png.read(os.path.join(sys.argv[2], "Common/NPC/MISC/Mannequin/Models/Model_Default.png"))
    for row in spx:
        for p in row:
            if p[3] == 0: continue
            l = 0.4 + 0.6 * (0.3 * p[0] + 0.59 * p[1] + 0.11 * p[2]) / 255
            p[0], p[1], p[2] = min(255, 110 + 160 * l), min(255, 70 + 160 * l), min(255, 10 + 110 * l * l)
    png.write_padded(os.path.join(A, "Statue_Gold.png"), w, h, spx)
print("ok")
