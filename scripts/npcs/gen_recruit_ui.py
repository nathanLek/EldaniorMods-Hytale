"""Images de la fenetre de recrutement des aventuriers (Common/UI/Custom/Npc/Recruit.ui).

La fenetre reprend la carte d'invocation des portails (client : Data/Shared/UI/Portals/PortalSummonPanel.xaml) :
  - les arches du cadre, copiees depuis le client (PortalFrameTop / PortalFrameBottom, a 2x comme l'original), bordure
    bleu-gris recoloree en or ;
  - un embleme dans le losange du portail : deux epees dorees croisees (a la place de l'etoile du Vide).

Usage : python3 gen_recruit_ui.py <src/main/resources> <dossier client .../Data/Shared/UI/Portals>
"""
import math, os, shutil, sys

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'rune_aura_icons'))
import png  # noqa: E402  (module PNG du projet : RGBA 8 bits)

RES, PORTALS = sys.argv[1], sys.argv[2]
OUT = os.path.join(RES, "Common", "UI", "Custom", "Npc")
os.makedirs(OUT, exist_ok=True)

GOLD_LIGHT, GOLD_DEEP = (246, 196, 92), (150, 98, 30)


def gild(p):
    """Bordure bleu-gris claire du cadre (#47516B et nuances) -> or, nuances conservees ; le reste ne change pas."""
    r, g, b, a = p
    lum = max(r, g, b)
    if a < 10 or lum < 52 or b < r + 15:
        return p
    t = max(0.0, min(1.0, (lum - 52) / 63.0))
    return [GOLD_DEEP[i] + (GOLD_LIGHT[i] - GOLD_DEEP[i]) * t for i in range(3)] + [a]


for src, dst in (("PortalFrameTop.png", "RecruitFrameTop@2x.png"), ("PortalFrameBottom.png", "RecruitFrameBottom@2x.png")):
    w, h, img = png.read(os.path.join(PORTALS, "Images", src))
    png.write(os.path.join(OUT, dst), w, h, [[gild(px_) for px_ in row] for row in img])
    print("cadre dore", dst)

# ---- Embleme (192 px = 96 a l'ecran) : losange du portail + epees croisees dorees ----
S = 192
BORDER, FILL = (214, 160, 62), (26, 34, 49)
GOLD, GOLD_DARK, STEEL = (232, 169, 59), (150, 98, 30), (214, 222, 232)


def seg(u, v, ax, ay, bx, by):
    px, py, dx, dy = u - ax, v - ay, bx - ax, by - ay
    t = max(0.0, min(1.0, (px * dx + py * dy) / (dx * dx + dy * dy)))
    return math.hypot(px - dx * t, py - dy * t)


def sword(u, v, sgn):
    """Distance (negative dedans) a une epee en diagonale ; sgn = 1 ou -1 (miroir)."""
    a = (-0.52 * sgn, 0.52)                                    # pommeau
    h = (-0.36 * sgn, 0.36)                                    # garde
    tip = (0.50 * sgn, -0.50)
    blade = seg(u, v, h[0], h[1], tip[0], tip[1]) - 0.055
    grip = seg(u, v, a[0], a[1], h[0], h[1]) - 0.04
    gx, gy = 0.13, 0.13 * sgn                                   # garde perpendiculaire a la lame
    guard = seg(u, v, h[0] - gx, h[1] - gy, h[0] + gx, h[1] + gy) - 0.035
    pommel = math.hypot(u - a[0], v - a[1]) - 0.06
    return blade, min(grip, guard, pommel)


px = []
for y in range(S):
    row = []
    for x in range(S):
        u, v = (x + 0.5) / S * 2 - 1, (y + 0.5) / S * 2 - 1
        d = abs(u) + abs(v)                                      # losange
        col, a = [0, 0, 0], 0.0
        if d <= 0.97:
            if d >= 0.88:
                col, a = list(BORDER), 1.0
            else:
                col, a = list(FILL), 1.0
                glow = max(0.0, 1 - math.hypot(u, v) / 0.75)
                col = [c + (g - c) * 0.25 * glow for c, g in zip(col, GOLD)]
                for sgn in (1, -1):
                    blade, hilt = sword(u, v, sgn)
                    if blade < 0:
                        shade = 0.75 + 0.25 * max(0.0, min(1.0, -blade / 0.055))
                        col = [c * shade for c in STEEL]
                    elif hilt < 0:
                        col = list(GOLD) if hilt < -0.012 else list(GOLD_DARK)
            if d > 0.95:                                         # bord adouci
                a *= max(0.0, (0.97 - d) / 0.02)
        row.append([col[0], col[1], col[2], a * 255])
    px.append(row)
png.write(os.path.join(OUT, "RecruitEmblem@2x.png"), S, S, px)
print("ecrit RecruitEmblem@2x.png")
