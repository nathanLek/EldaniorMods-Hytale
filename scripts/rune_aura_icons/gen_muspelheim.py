"""Modeles 3D du Courroux de Muspelheim : cercle runique (anneaux contrarotatifs), nuages de feu, ailes et couronne de feu.
Usage : python3 gen_muspelheim.py <dossier_resources_Common>"""
import json, math, os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import png
from gen_divine_models import q_axis, node, save_model, tex, IDENT
out = sys.argv[1]
M = os.path.join(out, "Items/Projectiles/Muspelheim"); os.makedirs(M, exist_ok=True)

def qmul(a, b):
    ax, ay, az, aw = a["x"], a["y"], a["z"], a["w"]; bx, by, bz, bw = b["x"], b["y"], b["z"], b["w"]
    return {"x": round(aw*bx + ax*bw + ay*bz - az*by, 6), "y": round(aw*by - ax*bz + ay*bw + az*bx, 6),
            "z": round(aw*bz + ax*by - ay*bx + az*bw, 6), "w": round(aw*bw - ax*bx - ay*by - az*bz, 6)}

FIRE = {
    (0, 0): lambda u, v: [255, 90 + 110 * (1 - v), 20 + 40 * (1 - v), 255],     # feu orange-rouge
    (16, 0): lambda u, v: [255, 235 + 20 * (1 - v), 150 + 90 * (1 - v), 255],   # glyphes incandescents
    (0, 16): lambda u, v: [150 + 60 * (1 - v), 20, 10, 255],                    # pourpre profond
    (16, 16): lambda u, v: [255, 250, 235, 255],                                 # coeur blanc
}

# ---------------- Cercle runique ----------------
tex(os.path.join(M, "Rune_Circle.png"), FIRE)
def ring(name, R, seg, size, stones=0, stone_size=(7, 5, 7), extra=None):
    kids = []
    for i in range(seg):
        t = 2 * math.pi * i / seg
        kids.append(node("%s_Seg_%d" % (name, i), (R * math.cos(t), 0, R * math.sin(t)), q_axis((0, 1, 0), -math.degrees(t) - 90), size))
    for i in range(stones):
        t = 2 * math.pi * i / stones + math.pi / stones
        kids.append(node("%s_Rune_%d" % (name, i), (R * math.cos(t), 1, R * math.sin(t)), q_axis((0, 1, 0), 45 - math.degrees(t)), stone_size, (16, 0)))
    return node(name, (0, 0, 0), children=kids + (extra or []))
# Hexagramme inscrit dans l'anneau du milieu
hexa = []
for tri in range(2):
    pts = [(46 * math.cos(math.radians(90 + tri * 60 + k * 120)), 46 * math.sin(math.radians(90 + tri * 60 + k * 120))) for k in range(3)]
    for k in range(3):
        (ax, az), (bx, bz) = pts[k], pts[(k + 1) % 3]
        L = math.hypot(bx - ax, bz - az); ang = math.degrees(math.atan2(bz - az, bx - ax))
        hexa.append(node("Hexa_%d_%d" % (tri, k), ((ax + bx) / 2, 0, (az + bz) / 2), q_axis((0, 1, 0), -ang), (int(L), 2, 3), (0, 16)))
eye = [node("Eye_Core", (0, 0, 0), q_axis((0, 1, 0), 45), (14, 6, 14), (16, 16)),
       node("Eye_Ring", (0, 0, 0), q_axis((0, 1, 0), 22.5), (20, 3, 20), (0, 0))]
root = node("Origin", (0, 0, 0), children=[
    ring("Ring_Outer", 62, 48, (9, 3, 5), stones=12),
    ring("Ring_Mid", 46, 36, (8, 3, 4), extra=hexa),
    ring("Ring_Inner", 26, 24, (7, 3, 4), stones=6, stone_size=(5, 4, 5)),
    node("Eye", (0, 0, 0), children=eye)])
save_model(os.path.join(M, "Rune_Circle.blockymodel"), root)
def spin(turns, steps=12):
    return [{"time": round(600 * k / steps), "delta": q_axis((0, 1, 0), 360 * turns * k / steps), "interpolationType": "linear"} for k in range(steps + 1)]
anim = {"formatVersion": 1, "duration": 600, "holdLastKeyframe": False, "nodeAnimations": {
    n: {"position": [], "orientation": spin(t), "shapeStretch": [], "shapeVisible": [], "shapeUvOffset": []}
    for n, t in (("Ring_Outer", 1), ("Ring_Mid", -1), ("Ring_Inner", 2), ("Eye", -0.5))}}
json.dump(anim, open(os.path.join(M, "Rune_Circle_Spin.blockyanim"), "w"), indent=2)

# Nuages de feu et tourbillon : voir gen_fire_sky.py

# ---------------- Ailes de feu ----------------
tex(os.path.join(M, "Wings.png"), FIRE)
kids = []
for s in (-1, 1):
    for k in range(7):
        L = 22 + 7 * k
        a = 12 + k * 11                                         # eventail de 12 a 78 degres au-dessus de l'horizontale
        d = (s * math.cos(math.radians(a)), math.sin(math.radians(a)))
        orient = qmul(q_axis((1, 0, 0), -18), q_axis((0, 0, 1), a if s > 0 else 180 - a))
        uv = (16, 0) if k % 3 == 0 else (0, 0) if k % 3 == 1 else (0, 16)
        kids.append(node("Feather_%d_%d" % (s, k), (s * 4 + d[0] * L / 2, d[1] * L / 2, -3 - k * 0.6), orient, (L, 5, 2), uv))
save_model(os.path.join(M, "Wings.blockymodel"), node("Origin", (0, 0, 0), children=kids))

# ---------------- Couronne de feu ----------------
kids = []
for i in range(20):
    t = 2 * math.pi * i / 20
    kids.append(node("Band_%d" % i, (8 * math.cos(t), 0, 8 * math.sin(t)), q_axis((0, 1, 0), -math.degrees(t) - 90), (3, 2, 2), (0, 16)))
for i in range(10):
    t = 2 * math.pi * i / 10
    orient = qmul(q_axis((0, 1, 0), -math.degrees(t)), q_axis((0, 0, 1), -20))
    kids.append(node("Spike_%d" % i, (8.5 * math.cos(t), 4 + 2 * (i % 2), 8.5 * math.sin(t)), orient, (2, 8 + 4 * (i % 2), 2), (16, 0)))
save_model(os.path.join(M, "Crown.blockymodel"), node("Circle", (0, 0, 0), children=kids))
print("ok")
