#!/usr/bin/env python3
"""
Portail Continental (2026-11-19) : voile translucide qui remplit l'ouverture (5 x 7 + 3 au sommet) quand un voyageur a paye,
et l'effet du teleporteur du jeu (Portal_Square_Blue_Fit) recentre sur son emetteur pour pouvoir l'agrandir.
  - Common/BlockTextures/Eldanior_Voile_Portail.png : texture raccordable 32 x 32, bleu-cyan, semi-transparente ;
  - Server/Particles/Portail/Eldanior_Portail_Continental.particlesystem : copie de Portal_Square_Blue_Fit, decalages
    verticaux ramenes au centre (le bloc Coeur porte le decalage et l'echelle) ;
  - Eldanior_Voile_Portail (voile) et Eldanior_Voile_Portail_Coeur (voile + effet), traversables (Material Empty).
Doit correspondre a territory/teleport/ContinentalGates.java (VEIL, VEIL_CORE).
Usage : python3 scripts/portail/gen_portail.py
"""
import json, math, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "rune_aura_icons"))
import png  # noqa: E402

RES = os.path.join(HERE, "..", "..", "src", "main", "resources")
VANILLA = os.path.expanduser("~/Desktop/Assets Hytale/Server/Particles/Spell/Portal/Portal_Square_Blue_Fit.particlesystem")
ITEMS = os.path.join(RES, "Server", "Item", "Items", "Furniture", "Eldanior")

# echelle et hauteur de l'effet sur le Coeur (bloc au pied de l'ouverture, au centre) : ouverture de 7 de haut -> centre a +3,5
FX_SCALE, FX_Y = 2.6, 3.5


def texture():
    S = 32
    deep, light = (30, 80, 200), (130, 235, 255)
    px = []
    for y in range(S):
        row = []
        for x in range(S):
            u, v = x / S, y / S
            t = 0.5 + 0.22 * math.sin(2 * math.pi * (2 * u + v)) + 0.18 * math.sin(2 * math.pi * (3 * v - u)) \
                + 0.10 * math.sin(2 * math.pi * (u + 4 * v))
            t = max(0.0, min(1.0, t))
            col = [int(d + (l - d) * t) for d, l in zip(deep, light)]
            a = int(95 + 90 * t)
            row.append([col[0], col[1], col[2], a])
        px.append(row)
    out = os.path.join(RES, "Common", "BlockTextures", "Eldanior_Voile_Portail.png")
    png.write(out, S, S, px)
    return out


def particles():
    d = json.load(open(VANILLA))
    for s in d["Spawners"]:
        off = s.setdefault("PositionOffset", {})
        off["Y"] = round(off.get("Y", 0) - 2.5, 3)
    folder = os.path.join(RES, "Server", "Particles", "Portail")
    os.makedirs(folder, exist_ok=True)
    out = os.path.join(folder, "Eldanior_Portail_Continental.particlesystem")
    json.dump(d, open(out, "w"), indent=2)
    return out


def veil(core):
    bt = {
        "Material": "Empty",
        "DrawType": "Cube",
        "Opacity": "Transparent",
        "RequiresAlphaBlending": True,
        "VariantRotation": "NESW",
        "Textures": [{"All": "BlockTextures/Eldanior_Voile_Portail.png"}],
        "ParticleColor": "#4ee4ff",
        "TextureComputedColor": "#3f8fd8",
        "Support": {"Down": [{"Support": "Ignored"}]},
    }
    bt = {k: v for k, v in bt.items() if v is not None}
    if core:
        bt["Particles"] = [{"SystemId": "Eldanior_Portail_Continental", "PositionOffset": {"Y": FX_Y}, "Scale": FX_SCALE}]
        bt["AmbientSoundEventId"] = "SFX_Portal_Neutral"
    item = {
        "TranslationProperties": {
            "Name": "Cœur du Portail Continental" if core else "Voile du Portail Continental",
            "Description": "Bloc technique : il remplit la porte quand un voyageur a payé son passage, puis disparaît.",
        },
        "Icon": "Icons/ItemsGenerated/Teleporter.png",
        "Categories": ["Eldanior_System.Autres"],
        "MaxStack": 1,
        "Quality": "Technical",
        "BlockType": bt,
    }
    name = "Eldanior_Voile_Portail_Coeur" if core else "Eldanior_Voile_Portail"
    out = os.path.join(ITEMS, name + ".json")
    json.dump(item, open(out, "w", encoding="utf-8"), ensure_ascii=False, indent=2)
    return out


if __name__ == "__main__":
    for p in (texture(), particles(), veil(False), veil(True)):
        print("ecrit", os.path.relpath(p, RES))
