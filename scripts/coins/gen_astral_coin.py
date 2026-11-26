"""Piece Astrale (Elda_Astral_Coins, 100 000 Eldan) : texture, icone et item, derives de la piece de Zenith.

Le modele reste Elda_Coins.blockymodel. La silhouette vient de la texture et de l'icone de Zenith (memes pixels opaques) ;
seules les couleurs changent : ciel bleu galaxie, bras spiraux, nebuleuse violette, etoiles, liseré bleu electrique pour que
la piece se detache sur le fond sombre de l'inventaire.

La valeur et les echanges sont ailleurs : CoinItemRegistry, WithdrawCommand, BarterShops/Bank_Npc.json.

Usage : python3 scripts/coins/gen_astral_coin.py <src/main/resources>
"""
import json, math, os, sys

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "rune_aura_icons"))
import png  # noqa: E402

RES = sys.argv[1]
COMMON = os.path.join(RES, "Common")
ITEMS = os.path.join(RES, "Server", "Item", "Items", "Coins")

VOID = (8, 10, 44)
DEEP = (20, 34, 120)
BLUE = (38, 92, 226)
CYAN = (108, 200, 255)
WHITE = (236, 248, 255)
VIOLET = (120, 58, 214)
RIM = (86, 146, 255)
RIM_DARK = (56, 92, 222)


def mix(a, b, t):
    t = max(0.0, min(1.0, t))
    return [a[i] + (b[i] - a[i]) * t for i in range(3)]


def ramp(v):
    """Noir bleute -> indigo -> bleu -> cyan -> blanc."""
    stops = (VOID, DEEP, BLUE, CYAN, WHITE)
    v = max(0.0, min(0.999, v)) * (len(stops) - 1)
    i = int(v)
    return mix(stops[i], stops[i + 1], v - i)


def galaxy(u, v):
    """Couleur en (u, v), centre en (0, 0), bord du disque a 1 : deux bras spiraux et un noyau brillant."""
    r = math.hypot(u, v)
    a = math.atan2(v, u)
    arms = 0.5 + 0.5 * math.cos(2 * a + 5.2 * r)
    glow = max(0.0, 1 - r * 1.25) ** 1.4
    value = 0.12 + 0.52 * arms ** 2 * (1 - r * 0.45) + 0.75 * glow
    c = ramp(value)
    nebula = max(0.0, 0.5 + 0.5 * math.cos(2 * a + 5.2 * r + 2.4)) * (1 - glow)
    return mix(c, VIOLET, 0.42 * nebula * min(1.0, r * 2))


def disc_mask(px, x0, y0, x1, y1):
    return {(x, y) for y in range(y0, y1) for x in range(x0, x1) if px[y][x][3] > 0}


def paint_disc(px, cells, stars):
    xs = [c[0] for c in cells]
    ys = [c[1] for c in cells]
    cx, cy = (min(xs) + max(xs)) / 2.0, (min(ys) + max(ys)) / 2.0
    rx, ry = (max(xs) - min(xs) + 1) / 2.0, (max(ys) - min(ys) + 1) / 2.0
    for x, y in cells:
        u, v = (x - cx) / rx, (y - cy) / ry
        edge = any((x + dx, y + dy) not in cells for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
        if edge:
            c = mix(RIM, RIM_DARK, 0.5 + 0.5 * (v - u) * 0.7)            # liseré, plus clair en haut a gauche
        else:
            c = galaxy(u, v)
        px[y][x][:3] = [int(round(i)) for i in c]
    for su, sv in stars:
        x, y = int(round(cx + su * rx)), int(round(cy + sv * ry))
        if (x, y) in cells:
            px[y][x][:3] = list(WHITE)


def recolor_rest(px, cells):
    """Tranche de la piece et petites zones d'UV hors disque : bleu electrique, nuance gardee du modele d'origine."""
    for y, row in enumerate(px):
        for x, p in enumerate(row):
            if p[3] > 0 and (x, y) not in cells:
                lum = (p[0] * 0.3 + p[1] * 0.59 + p[2] * 0.11) / 255.0
                p[:3] = [int(round(i)) for i in mix(RIM_DARK, RIM, lum * 1.2)]


def face_cells(px):
    """Le disque = la plus grande zone opaque connectee (le reste de l'atlas : tranche, UV annexes)."""
    h, w = len(px), len(px[0])
    seen, best = set(), set()
    for y in range(h):
        for x in range(w):
            if px[y][x][3] > 0 and (x, y) not in seen:
                stack, zone = [(x, y)], set()
                seen.add((x, y))
                while stack:
                    a, b = stack.pop()
                    zone.add((a, b))
                    for n in ((a + 1, b), (a - 1, b), (a, b + 1), (a, b - 1)):
                        if 0 <= n[0] < w and 0 <= n[1] < h and n not in seen and px[n[1]][n[0]][3] > 0:
                            seen.add(n)
                            stack.append(n)
                if len(zone) > len(best):
                    best = zone
    return best


def recolor(src, dst, stars):
    w, h, px = png.read(src)
    cells = face_cells(px)
    paint_disc(px, cells, stars)
    recolor_rest(px, cells)
    png.write(dst, w, h, px)


recolor(os.path.join(COMMON, "Items/Coins/Elda_Zenith_Coins.png"), os.path.join(COMMON, "Items/Coins/Elda_Astral_Coins.png"),
        [(-0.45, -0.38), (0.5, 0.42)])
recolor(os.path.join(COMMON, "Icons/ItemsGenerated/Elda_Zenith_Coins_Icone.png"), os.path.join(COMMON, "Icons/ItemsGenerated/Elda_Astral_Coins_Icone.png"),
        [(-0.55, -0.45), (0.58, -0.3), (0.42, 0.55), (-0.3, 0.62), (-0.68, 0.12), (0.12, -0.7)])

with open(os.path.join(ITEMS, "Elda_Zenith_Coins.json"), encoding="utf-8") as f:
    item = json.load(f)
item["TranslationProperties"]["Name"] = "Pièce Astrale"
item["BlockType"]["CustomModelTexture"] = [{"Texture": "Items/Coins/Elda_Astral_Coins.png", "Weight": 1} for _ in range(2)]
item["BlockType"]["ParticleColor"] = "#4a7dff"
item["BlockType"]["Particles"][0]["Color"] = "#8fb8ff"
item["Icon"] = "Icons/ItemsGenerated/Elda_Astral_Coins_Icone.png"
with open(os.path.join(ITEMS, "Elda_Astral_Coins.json"), "w", encoding="utf-8") as f:
    json.dump(item, f, indent=2, ensure_ascii=False)
    f.write("\n")
