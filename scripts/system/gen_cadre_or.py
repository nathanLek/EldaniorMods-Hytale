"""Cadre dore du menu systeme (2026-11-12) : textures du cadre des fenetres du jeu (@DecoratedContainer de
Common/UI/Custom/Common.ui : ContainerPatch, ContainerHeader, ContainerDecorationTop/Bottom) dont la bordure bleu-gris est
repeinte en or, nuances conservees (meme principe que les arches du recrutement, scripts/npcs/gen_recruit_ui.py). Le fond reste
sombre. Ecrit Common/UI/Custom/System/Gold*.png, utilises par System/SystemPage.ui.

Usage : python3 scripts/system/gen_cadre_or.py [src/main/resources]
"""
import json, os, sys, zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "rune_aura_icons"))
import png  # noqa: E402

RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "..", "..", "src", "main", "resources")
OUT = os.path.join(RES, "Common", "UI", "Custom", "System")
ASSETS = os.path.expanduser("~/Library/Application Support/Hytale/install/release/package/game/latest/Assets.zip")
GOLD_DEEP, GOLD_LIGHT = (122, 80, 24), (246, 200, 98)
# cadre rouge des criminels (PK) : meme bordure, teinte sang (demande du 2026-11-12)
RED_DEEP, RED_LIGHT = (96, 14, 18), (236, 86, 74)


def gild(p, deep=GOLD_DEEP, light=GOLD_LIGHT):
    """Bordure opaque bleu-gris (lum 35-140, peu saturee) -> or ; fonds sombres ou translucides et bleu du bandeau gardes."""
    r, g, b = p[0], p[1], p[2]
    a = p[3] if len(p) > 3 else 255
    lum = max(r, g, b)
    if a < 250 or lum < 35 or lum > 160 or b - r > 34:
        return list(p)
    t = max(0.0, min(1.0, (lum - 35) / 95.0))
    return [deep[i] + (light[i] - deep[i]) * t for i in range(3)] + [a]


def main():
    z = zipfile.ZipFile(ASSETS)
    os.makedirs(OUT, exist_ok=True)
    tmp = os.path.join(OUT, "_tmp.png")
    for src, dst in (("ContainerPatch", "GoldPatch"), ("ContainerHeader", "GoldHeader"),
                     ("ContainerDecorationTop", "GoldDecorationTop"), ("ContainerDecorationBottom", "GoldDecorationBottom")):
        open(tmp, "wb").write(z.read("Common/UI/Custom/Common/%s@2x.png" % src))
        w, h, img = png.read(tmp)
        png.write(os.path.join(OUT, dst + "@2x.png"), w, h, [[gild(px) for px in row] for row in img])
        red = dst.replace("Gold", "Red")
        png.write(os.path.join(OUT, red + "@2x.png"), w, h, [[gild(px, RED_DEEP, RED_LIGHT) for px in row] for row in img])
        print("ecrit", dst + "@2x.png", red + "@2x.png", w, h)
    os.remove(tmp)
    panel()
    icons()


def panel():
    _panel("GoldPanelPatch", [10, 15, 22, 150], [186, 136, 56, 220])
    _panel("GoldPanelHover", [40, 34, 20, 200], [236, 186, 92, 255])   # survol des boutons (runes)


def _panel(name, fill, line):
    """Panneau interne (statistiques, caracteristiques) : 24 x 24 @2x, coins arrondis, fond sombre translucide, liseré or de 2 px
    (1 px a l'ecran) ; a utiliser en 9-tranches (Border: 4)."""
    S, R = 24, 5.0
    rows = []
    for y in range(S):
        row = []
        for x in range(S):
            dx = max(R - (x + 0.5), 0, (x + 0.5) - (S - R))
            dy = max(R - (y + 0.5), 0, (y + 0.5) - (S - R))
            d = (dx * dx + dy * dy) ** 0.5                       # distance au coin arrondi (0 hors des coins)
            edge = min(x, y, S - 1 - x, S - 1 - y)
            if d > R:
                row.append([0, 0, 0, 0])
            elif d > R - 2 or edge < 2:
                row.append(list(line))
            else:
                row.append(list(fill))
        rows.append(row)
    png.write(os.path.join(OUT, name + "@2x.png"), S, S, rows)
    print("ecrit", name + "@2x.png")


def _icon(name, inside):
    """Icone 78 x 78 @2x dans le style du panneau personnage (sur-echantillonnee 4x) ; inside(u, v) -> couleur RGB ou None."""
    S, K = 78, 4
    rows = []
    for y in range(S):
        row = []
        for x in range(S):
            acc, n = [0.0, 0.0, 0.0], 0
            for sy in range(K):
                for sx in range(K):
                    u = ((x + (sx + 0.5) / K) / S * 2 - 1) * 1.35          # meme echelle que les icones du jeu
                    v = ((y + (sy + 0.5) / K) / S * 2 - 1) * 1.35
                    c = inside(u, v)
                    if c:
                        n += 1
                        for i in range(3):
                            acc[i] += c[i]
            row.append([acc[0] / n, acc[1] / n, acc[2] / n, 255 * n / (K * K)] if n else [0, 0, 0, 0])
        rows.append(row)
    png.write(os.path.join(OUT, name + "@2x.png"), S, S, rows)
    print("ecrit", name + "@2x.png")


def icons():
    import math
    gold, gold_dark, gold_light, ruby, white = (232, 178, 70), (160, 108, 34), (255, 222, 140), (214, 52, 78), (250, 244, 225)

    def crown(u, v):                                            # Dignite : couronne doree, trois pointes et rubis
        if 0.18 <= v <= 0.42 and abs(u) <= 0.56:                # bandeau
            return gold_dark if v > 0.36 or abs(u) > 0.52 else gold
        if -0.42 <= v < 0.18 and abs(u) <= 0.56:                # pointes
            for cx in (-0.42, 0.0, 0.42):
                top = -0.42 if cx == 0 else -0.28
                half = 0.2 * (v - top) / (0.18 - top)
                if v >= top and abs(u - cx) <= half + 0.07:
                    return gold_light if u < cx else gold
        for cx, cy in ((-0.42, -0.36), (0.0, -0.5), (0.42, -0.36)):   # perles
            if math.hypot(u - cx, v - cy) <= 0.08:
                return gold_light
        if math.hypot(u, v - 0.30) <= 0.075:                    # rubis
            return ruby
        return None

    def faith(u, v):                                            # Foi : croix blanche dans un soleil d'or
        r = math.hypot(u, v)
        a = math.atan2(v, u)
        if abs(u) <= 0.1 and -0.6 <= v <= 0.6 or abs(v + 0.12) <= 0.1 and abs(u) <= 0.38:
            return white if u < 0.05 else (220, 210, 185)
        if r <= 0.42:
            return gold_light if r < 0.3 else gold
        ray = 0.42 + 0.22 * max(0.0, math.cos(a * 8))
        if r <= ray and r <= 0.66:
            return gold_dark if r > 0.56 else gold
        return None

    _icon("StatIconDignite", crown)
    _icon("StatIconFoi", faith)


if __name__ == "__main__":
    main()
