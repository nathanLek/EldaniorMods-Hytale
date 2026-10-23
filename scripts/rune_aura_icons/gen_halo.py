# Halo d'ange 3D (Gilgamesh Babylon) : anneau en volume (32 segments dores disposes en cercle) + 8 gemmes blanches.
# Le noeud racine s'appelle "Circle" pour reutiliser l'animation de rotation Sky_Circle_Spin.
# Usage : python3 gen_halo.py <dossier_sortie>   -> Halo.blockymodel + Halo.png
import json, math, os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import png
out = sys.argv[1]
# Texture 16x8 : or (0..7) avec reflet central, blanc lumineux (8..15) pour les gemmes
T = 16
px = [[[0, 0, 0, 0] for _ in range(T)] for _ in range(8)]
for y in range(8):
    for x in range(8):
        h = 1 - abs(y - 1.5) / 6.5                     # reflet vers le haut de chaque face
        px[y][x] = [255, 170 + 70 * h, 40 + 120 * h ** 2, 255]
    for x in range(8, 16):
        px[y][x] = [255, 252, 225, 255]
png.write_padded(os.path.join(out, "Halo.png"), T, 8, px)

def face(ox, oy): return {"offset": {"x": ox, "y": oy}, "mirror": {"x": False, "y": False}, "angle": 0}
def box(nid, name, pos, yaw_deg, size, uv):
    a = math.radians(yaw_deg) / 2
    return {"id": str(nid), "name": name, "position": pos,
            "orientation": {"x": 0, "y": round(math.sin(a), 6), "z": 0, "w": round(math.cos(a), 6)},
            "shape": {"type": "box", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": 1, "y": 1, "z": 1},
                      "settings": {"size": size},
                      "textureLayout": {k: face(*uv) for k in ("top", "bottom", "front", "back", "left", "right")},
                      "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": "fullbright"},
            "children": []}
R, SEG = 20, 32
children = []; nid = 2
for i in range(SEG):
    t = 2 * math.pi * i / SEG
    pos = {"x": round(R * math.cos(t), 3), "y": 0, "z": round(R * math.sin(t), 3)}
    children.append(box(nid, "Seg_%d" % i, pos, -math.degrees(t) - 90, {"x": 5, "y": 2, "z": 3}, (0, 0))); nid += 1
for i in range(8):
    t = 2 * math.pi * i / 8 + math.pi / 8
    pos = {"x": round(R * math.cos(t), 3), "y": 1.5, "z": round(R * math.sin(t), 3)}
    children.append(box(nid, "Gem_%d" % i, pos, -math.degrees(t) + 45, {"x": 2, "y": 2, "z": 2}, (8, 0))); nid += 1
root = {"id": "1", "name": "Circle", "position": {"x": 0, "y": 0, "z": 0}, "orientation": {"x": 0, "y": 0, "z": 0, "w": 1},
        "shape": {"type": "none", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": 1, "y": 1, "z": 1},
                  "settings": {"isPiece": False}, "visible": True, "doubleSided": False, "shadingMode": "standard",
                  "unwrapMode": "custom", "textureLayout": {}},
        "children": children}
json.dump({"nodes": [root], "lod": "auto"}, open(os.path.join(out, "Halo.blockymodel"), "w"), indent=2)
