# Prison de glace (Porte de Niflheim) : texture de pique de glace + modele de cristaux qui jaillissent du corps.
# Usage : python3 gen_ice_prison.py <dossier_sortie>
import json, math, os, random, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import png
out = sys.argv[1]
W, H = 16, 48
px = [[[0, 0, 0, 0] for _ in range(W)] for _ in range(H)]
for y in range(H):
    t = y / (H - 1)                      # 0 = pointe (haut), 1 = base
    half = (W / 2) * (0.08 + 0.92 * t)
    for x in range(W):
        d = abs(x + 0.5 - W / 2)
        if d > half: continue
        edge = d / max(half, 0.01)
        core = 1 - edge
        r = 150 + 105 * (1 - t) * 0.7 + 60 * core * 0.3
        g = 215 + 40 * (1 - t) * 0.6
        b = 255
        a = 0.55 + 0.35 * t + 0.25 * (edge > 0.75)
        if abs(x + 0.5 - W / 2 + half * 0.35) < 0.8: r, g, a = 255, 255, 0.95   # reflet
        px[y][x] = [min(255, r), min(255, g), b, 255 * min(1.0, a)]
png.write_padded(os.path.join(out, "Ice_Spike.png"), W, H, px)

def quat_mul(a, b):
    ax, ay, az, aw = a; bx, by, bz, bw = b
    return (aw*bx + ax*bw + ay*bz - az*by, aw*by - ax*bz + ay*bw + az*bx, aw*bz + ax*by - ay*bx + az*bw, aw*bw - ax*bx - ay*by - az*bz)
def axis(ax, deg):
    s = math.sin(math.radians(deg) / 2); return (ax[0]*s, ax[1]*s, ax[2]*s, math.cos(math.radians(deg) / 2))
def q(t): return {"x": round(t[0], 6), "y": round(t[1], 6), "z": round(t[2], 6), "w": round(t[3], 6)}
def quad(nid, name, pos, orient, stretch):
    return {"id": str(nid), "name": name, "position": pos, "orientation": q(orient),
            "shape": {"type": "quad", "offset": {"x": 0, "y": H / 2, "z": 0}, "stretch": {"x": stretch, "y": stretch, "z": 1},
                      "settings": {"isPiece": False, "size": {"x": W, "y": H, "z": 0}, "normal": "+Z"},
                      "textureLayout": {"front": {"offset": {"x": 0, "y": 0}, "mirror": {"x": False, "y": False}, "angle": 0}},
                      "unwrapMode": "custom", "visible": True, "doubleSided": True, "shadingMode": "fullbright"},
            "children": []}
random.seed(7)
nodes = []; nid = 2
spikes = [(0, 0, 0, 0.9)]  # (azimut, inclinaison, hauteur, taille)
for i in range(11):
    spikes.append((i * 360 / 11 + random.uniform(-12, 12), random.uniform(25, 65), random.uniform(0, 30), random.uniform(0.55, 1.05)))
for az, tilt, h, size in spikes:
    rot = quat_mul(axis((0, 1, 0), az), axis((1, 0, 0), tilt))   # incline vers l'exterieur puis tourne autour du corps
    r = 6 if tilt else 0
    pos = {"x": round(math.sin(math.radians(az)) * r, 3), "y": round(h, 3), "z": round(math.cos(math.radians(az)) * r, 3)}
    a = quad(nid, "Spike_%d" % nid, pos, rot, size); nid += 1
    # enfant : place par rapport au CENTRE de la forme du parent (decalee de H/2) -> on compense
    b = quad(nid, "Spike_%d_X" % nid, {"x": 0, "y": -H / 2, "z": 0}, axis((0, 1, 0), 90), 1.0); nid += 1
    b["shape"]["stretch"] = a["shape"]["stretch"]
    a["children"].append(b)
    nodes.append(a)
model = {"nodes": [{"id": "1", "name": "Origin", "position": {"x": 0, "y": 0, "z": 0}, "orientation": q((0, 0, 0, 1)),
                    "shape": {"type": "none", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": 1, "y": 1, "z": 1},
                              "settings": {"isPiece": False}, "visible": True, "doubleSided": False, "shadingMode": "standard",
                              "unwrapMode": "custom", "textureLayout": {}},
                    "children": nodes}], "lod": "auto"}
json.dump(model, open(os.path.join(out, "Ice_Prison.blockymodel"), "w"), indent=2)
