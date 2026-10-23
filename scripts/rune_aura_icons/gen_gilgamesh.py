"""Assets du tresor de Gilgamesh Babylon : armes de lumiere (textures vanilla recolorees), portail dore ondulant.
Usage : python3 gen_gilgamesh.py <dossier_resources_Common> <dossier_contenant_Common/...extrait d'Assets.zip>"""
import json, math, os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import png
out, src = sys.argv[1], sys.argv[2]
G = os.path.join(out, 'Items/Projectiles/Gilgamesh')
os.makedirs(G, exist_ok=True)

# Armes : texture vanilla eclaircie en blanc-or lumineux (motifs conserves)
WEAPONS = {'Light_Sword': 'Sword/Adamantite_Texture.png', 'Light_Katana': 'Longsword/Katana_Texture.png',
           'Light_Ornate': 'Sword/Frost_Texture.png', 'Light_Longsword': 'Longsword/Adamantite_Texture.png'}
for name, tex in WEAPONS.items():
    w, h, px = png.read(os.path.join(src, 'Common/Items/Weapons', tex))
    for row in px:
        for p in row:
            if p[3] == 0: continue
            l = 0.55 + 0.45 * (0.3 * p[0] + 0.59 * p[1] + 0.11 * p[2]) / 255
            p[0], p[1], p[2] = 255 * l, 240 * l, 190 * l + 40 * (1 - l)
    png.write(os.path.join(G, name + '.png'), w, h, px)

# Portail : ondulations dorees concentriques, centre sombre et lumineux sur les bords
N = 128; c = (N - 1) / 2
px = [[[0, 0, 0, 0] for _ in range(N)] for _ in range(N)]
for y in range(N):
    for x in range(N):
        d = math.hypot(x - c, y - c) / c; a = math.atan2(y - c, x - c)
        if d > 1: continue
        wave = (0.5 + 0.5 * math.cos((d * 6.0 - 0.06 * math.sin(a * 5)) * 2 * math.pi)) ** 6   # ondulations fines
        rim = math.exp(-((d - 0.92) / 0.05) ** 2)
        fade = min(1.0, (1 - d) / 0.08)
        alpha = min(1.0, 0.28 + wave * 0.6 * d + rim) * fade
        bright = min(1.0, wave * 0.8 + rim)
        px[y][x] = [120 + 135 * (0.4 + 0.6 * bright), 70 + 175 * bright, 10 + 180 * bright ** 2, 255 * alpha]
        continue
        px[y][x] = [255, 175 + 75 * bright, 40 + 170 * bright ** 2, 255 * alpha]
png.write(os.path.join(G, 'Portal.png'), N, N, px)

quad = {"type": "quad", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": 1, "y": 1, "z": 1},
        "settings": {"size": {"x": N, "y": N}, "normal": "+Z"}, "visible": True, "doubleSided": True,
        "shadingMode": "fullbright", "unwrapMode": "custom",
        "textureLayout": {"front": {"offset": {"x": 0, "y": 0}, "mirror": {"x": False, "y": False}, "angle": 0}}}
none = {"type": "none", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": 1, "y": 1, "z": 1}, "settings": {"isPiece": False},
        "visible": True, "doubleSided": False, "shadingMode": "standard", "unwrapMode": "custom", "textureLayout": {}}
ident = {"x": 0, "y": 0, "z": 0, "w": 1}
model = {"nodes": [{"id": "1", "name": "Origin", "position": {"x": 0, "y": 0, "z": 0}, "orientation": ident, "shape": none,
                    "children": [{"id": "2", "name": "Ripple", "position": {"x": 0, "y": 0, "z": 0}, "orientation": ident,
                                  "shape": quad, "children": []}]}], "lod": "auto"}
json.dump(model, open(os.path.join(G, 'Portal.blockymodel'), 'w'), indent=2)
# Rotation du portail autour de son axe (Z)
keys = []
for i in range(7):
    t = i * 100; half = math.radians(i * 60) / 2
    keys.append({"time": t, "delta": {"x": 0, "y": 0, "z": round(math.sin(half), 6), "w": round(math.cos(half), 6)},
                 "interpolationType": "linear"})
anim = {"formatVersion": 1, "duration": 600, "holdLastKeyframe": False,
        "nodeAnimations": {"Ripple": {"position": [], "orientation": keys, "shapeStretch": [], "shapeVisible": [], "shapeUvOffset": []}}}
json.dump(anim, open(os.path.join(G, 'Portal_Spin.blockyanim'), 'w'), indent=2)
print('ok')
