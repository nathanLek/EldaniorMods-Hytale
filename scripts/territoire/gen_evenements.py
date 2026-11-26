"""Evenements du royaume (2026-11-19) : faille d'invasion, marqueurs de carte du Boss Mondial, marqueur vide (boussole).

- Eldanior_Faille_Invasion : bloc technique pose au sol par Invasions (territory/laws) : l'effet de la faille des gobelins du jeu
  (GoblinBreach_Portal_Active, son SFX_Goblin_World_Breach_Portal), sans collision. Les monstres en sortent un par un ; il est
  retire a la fin de l'invasion. Marqueur de carte : GoblinBreach.png du jeu (pose par le code, ParcelMapSystem).
- Common/UI/WorldMap/MapMarkers/Eldanior_BossMondial.png (+ _Boussole) : couronne d'or sur crane, 48 px, comme GoblinBreach.png.
- Common/UI/WorldMap/MapMarkers/Eldanior_Vide.png : image transparente, donnee comme image de boussole aux marqueurs des zones
  (demande de l'utilisateur : plus de marqueurs de zone dans la boussole, ils restent sur la carte).

Usage : python3 scripts/territoire/gen_evenements.py [src/main/resources]
"""
import json, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "rune_aura_icons"))
import png  # noqa: E402

RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "..", "..", "src", "main", "resources")


def write_json(rel, data):
    path = os.path.join(RES, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


write_json("Server/Item/Items/Eldanior/Evenements/Eldanior_Faille_Invasion.json", {
    "TranslationProperties": {"Name": "Faille d'invasion",
                              "Description": "Bloc technique : faille ouverte au sol pendant une invasion, d'où sortent les monstres."},
    "Icon": "Icons/ItemsGenerated/Portal_Device.png",
    "Categories": ["Eldanior_System.Autres"],
    "MaxStack": 1,
    "Quality": "Technical",
    "BlockType": {
        "Material": "Empty",
        "DrawType": "Empty",
        "Opacity": "Transparent",
        "Support": {"Down": [{"Support": "Ignored"}]},
        "AmbientSoundEventId": "SFX_Goblin_World_Breach_Portal",
        "Particles": [{"SystemId": "GoblinBreach_Portal_Active", "Scale": 1.1, "PositionOffset": {"Y": 1.0}}],
        "Light": {"Color": "#cb2"},
    },
})

# ---- icones de carte (48 x 48) : couronne d'or sur un crane, liseré sombre ----
N = 48
OUT = os.path.join(RES, "Common", "UI", "WorldMap", "MapMarkers")
os.makedirs(OUT, exist_ok=True)
CROWN = [
    "..#....##....#..",
    ".#y#..#yy#..#y#.",
    ".#yy##yyyy##yy#.",
    ".#yyyyyrryyyyy#.",
    ".#yyyyrrrryyyy#.",
    ".#oooooooooooo#.",
    ".##############.",
]
SKULL = [
    "...########...",
    ".##wwwwwwwww#..",
    "#wwwwwwwwwwww#.",
    "#wwwwwwwwwwww#.",
    "#weeewwwweeew#.",
    "#weeewwwweeew#.",
    "#wwwwwnnwwwww#.",
    ".#wwwwwwwwww#..",
    "..#wtwtwtwt#...",
    "...########....",
]
COL = {"#": (40, 24, 8, 255), "y": (250, 204, 64, 255), "o": (200, 140, 30, 255), "r": (214, 40, 50, 255),
       "w": (236, 230, 214, 255), "e": (30, 10, 40, 255), "n": (30, 10, 40, 255), "t": (236, 230, 214, 255)}


def icon(scale_px):
    img = [[[0, 0, 0, 0] for _ in range(N)] for _ in range(N)]
    rows = CROWN + SKULL
    h = len(rows)
    w = max(len(r) for r in rows)
    ox, oy = (N - w * scale_px) // 2, (N - h * scale_px) // 2
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch not in COL:
                continue
            for dy in range(scale_px):
                for dx in range(scale_px):
                    X, Y = ox + x * scale_px + dx, oy + y * scale_px + dy
                    if 0 <= X < N and 0 <= Y < N:
                        img[Y][X] = list(COL[ch])
    # liseré noir d'un pixel autour de la forme (lisible sur la carte)
    out = [row[:] for row in img]
    for y in range(N):
        for x in range(N):
            if img[y][x][3]:
                continue
            if any(0 <= x + dx < N and 0 <= y + dy < N and img[y + dy][x + dx][3] for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                out[y][x] = [12, 6, 2, 220]
    return out


png.write(os.path.join(OUT, "Eldanior_BossMondial.png"), N, N, icon(2))
png.write(os.path.join(OUT, "Eldanior_BossMondial_Boussole.png"), N, N, icon(2))
png.write(os.path.join(OUT, "Eldanior_Vide.png"), 16, 16, [[[0, 0, 0, 0] for _ in range(16)] for _ in range(16)])
print("Evenements ecrits dans", RES)
