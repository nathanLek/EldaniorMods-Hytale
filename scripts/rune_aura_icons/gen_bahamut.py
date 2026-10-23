"""Avenement de Bahamut : texture du dragon spectral dore (dragon de feu vanilla recolore) et ailes de dragon du lanceur.
Usage : python3 gen_bahamut.py <dossier_resources_Common> <dossier_contenant_Common/...extrait d'Assets.zip>"""
import os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import png
from gen_divine_models import tex
out, src = sys.argv[1], sys.argv[2]
B = os.path.join(out, "Items/Projectiles/Bahamut"); os.makedirs(B, exist_ok=True)
w, h, px = png.read(os.path.join(src, "Common/NPC/Elemental/Dragon_Fire/Models/Texture.png"))
for row in px:
    for p in row:
        if p[3] == 0: continue
        l = (0.3 * p[0] + 0.59 * p[1] + 0.11 * p[2]) / 255
        l = 0.35 + 0.65 * l                                  # or spectral : sombres -> bronze, clairs -> blanc dore
        p[0], p[1], p[2] = min(255, 90 + 190 * l), min(255, 55 + 170 * l), min(255, 10 + 120 * l * l)
png.write_padded(os.path.join(B, "Dragon_Gold.png"), w, h, px)
tex(os.path.join(B, "Dragon_Wings.png"), {
    (0, 0): lambda u, v: [110 + 50 * (1 - v), 18, 22, 255],                                   # ecailles pourpres
    (16, 0): lambda u, v: [255, 200 + 40 * (1 - v), 90, 255],                                 # bord dore
    (0, 16): lambda u, v: [60 + 30 * ((int(u * 6) + int(v * 6)) % 2), 10, 14, 255],          # membrane sombre en ecailles
    (16, 16): lambda u, v: [255, 245, 220, 255],
})
print("ok")
