# Nuage d'orage en volume (Sanctuaire de Taranis) : 5 quads empiles, tournes et de tailles differentes, sur un atlas
# de 3 textures de nuages. Noeud racine "Circle" (animation de rotation Sky_Circle_Spin).
# Usage : python3 gen_storm_volume.py <dossier> <nuage1.png> <nuage2.png> <nuage3.png>  -> Storm_Clouds.blockymodel + Storm_Clouds_Atlas.png
import json, math, os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import png
out = sys.argv[1]
texs = [png.read(p) for p in sys.argv[2:5]]
N = texs[0][0]
atlas = [sum((t[2][y] for t in texs), []) for y in range(N)]
png.write(os.path.join(out, "Storm_Clouds_Atlas.png"), N * 3, N, atlas)
def q(yaw):
    a = math.radians(yaw) / 2
    return {"x": 0, "y": round(math.sin(a), 6), "z": 0, "w": round(math.cos(a), 6)}
layers = [(-6, 0, 0.80, 0), (-3, 70, 0.95, 1), (0, 140, 1.0, 2), (3, 215, 0.92, 0), (6, 290, 0.75, 1)]
children = []
for i, (y, yaw, stretch, tex) in enumerate(layers):
    children.append({"id": str(i + 2), "name": "Layer_%d" % i, "position": {"x": 0, "y": y, "z": 0}, "orientation": q(yaw),
        "shape": {"type": "quad", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": stretch, "y": stretch, "z": stretch},
                  "settings": {"size": {"x": N, "y": N}, "normal": "+Y"}, "visible": True, "doubleSided": True,
                  "shadingMode": "fullbright", "unwrapMode": "custom",
                  "textureLayout": {"front": {"offset": {"x": N * tex, "y": 0}, "mirror": {"x": False, "y": False}, "angle": 0}}},
        "children": []})
root = {"id": "1", "name": "Circle", "position": {"x": 0, "y": 0, "z": 0}, "orientation": {"x": 0, "y": 0, "z": 0, "w": 1},
        "shape": {"type": "none", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": 1, "y": 1, "z": 1}, "settings": {"isPiece": False},
                  "visible": True, "doubleSided": False, "shadingMode": "standard", "unwrapMode": "custom", "textureLayout": {}},
        "children": children}
json.dump({"nodes": [root], "lod": "auto"}, open(os.path.join(out, "Storm_Clouds.blockymodel"), "w"), indent=2)
