#!/usr/bin/env python3
"""Generateur de monstres (2026-11-23) : bloc Eldanior_Generateur_Monstres (invisible et traversable comme le poste de PNJ ;
F dessus = choisir le monstre et le nombre ; code failles/Generateurs) et son icone, au design du marqueur d'apparition du jeu
(Common/Items/NPC_Spawner_Block : cadre cyan, fleche rouge et tete de monstre a cornes).

Usage : python3 -I scripts/failles/gen_generateur.py [dossier des assets vanilla]
"""
import json, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.normpath(os.path.join(HERE, "..", ".."))
sys.path.insert(0, os.path.join(HERE, "..", "rune_aura_icons"))
import png  # noqa: E402

VANILLA = sys.argv[1] if len(sys.argv) > 1 else os.path.expanduser("~/Desktop/Assets Hytale")
RES = os.path.join(ROOT, "src", "main", "resources")

# ---- icone 64 px : cadre cyan (texture 65..85 x 1..20) agrandi x3, tete de monstre (36..54 x 33..61) au centre ----
w, h, tex = png.read(os.path.join(VANILLA, "Common", "Items", "NPC_Spawner_Block_Texture.png"))
N = 64
icon = [[[0, 0, 0, 0] for _ in range(N)] for _ in range(N)]


def blit(sx0, sy0, sx1, sy1, dx, dy, scale):
    for y in range(sy0, sy1):
        for x in range(sx0, sx1):
            c = tex[y][x]
            if c[3] < 10:
                continue
            for a in range(scale):
                for b in range(scale):
                    X, Y = dx + (x - sx0) * scale + a, dy + (y - sy0) * scale + b
                    if 0 <= X < N and 0 <= Y < N:
                        icon[Y][X] = list(c)


blit(65, 1, 85, 21, 2, 2, 3)                       # cadre cyan 20 x 20 -> 60 x 60
blit(86, 4, 95, 18, 28, 13, 1)                     # fleche rouge (vers le bas) 9 x 14
blit(36, 50, 55, 62, 13, 29, 2)                    # tete de monstre a cornes 19 x 12 -> 38 x 24
out = os.path.join(RES, "Common", "Icons", "ItemsGenerated", "Eldanior_Generateur_Monstres.png")
os.makedirs(os.path.dirname(out), exist_ok=True)
png.write(out, N, N, icon)

item = {
    "$Comment": "Generateur de monstres (failles/Generateurs) : bloc invisible et traversable ; le poser ou F dessus ouvre sa fenetre (admins).",
    "TranslationProperties": {
        "Name": "Générateur de monstres",
        "Description": ("Admins : posez ce bloc invisible et traversable, puis choisissez le monstre et le nombre (F sur le bloc pour y "
                        "revenir). Les monstres apparaissent quand un joueur approche et reviennent 1 h après leur mort. Dans le modèle "
                        "d'une faille, chaque faille ouverte le reprend. Tenez-le en main pour voir les générateurs posés."),
    },
    "Categories": ["Eldanior_System.Dungeons"],
    "Quality": "Technical",
    "PlayerAnimationsId": "Block",
    "Icon": "Icons/ItemsGenerated/Eldanior_Generateur_Monstres.png",
    "MaxStack": 64,
    "BlockType": {
        "Material": "Empty",
        "DrawType": "Empty",
        "Opacity": "Transparent",
        "Support": {"Down": [{"Support": "Ignored"}]},
        "Interactions": {"Use": {"Interactions": [{"Type": "Simple"}]}},
    },
}
p = os.path.join(RES, "Server", "Item", "Items", "Eldanior", "Failles", "Eldanior_Generateur_Monstres.json")
with open(p, "w", encoding="utf-8") as f:
    json.dump(item, f, indent=2, ensure_ascii=False)
    f.write("\n")
print("generateur :", p, "+ icone", out)
