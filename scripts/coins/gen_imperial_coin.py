"""Piece Imperiale (Elda_Imperial_Coins, 1 000 000 cuivres = 10 Astrales) : texture, icone et item, derives de la piece de Zenith.

Demande du 2026-11-16 : une piece au-dessus de l'Astrale. Meme modele (Elda_Coins.blockymodel) et meme silhouette que les
autres pieces (pixels opaques de la piece de Zenith) ; seules les couleurs changent : pourpre imperial, soleil d'or a douze
rayons au centre, liseré d'or clair pour qu'elle se detache sur le fond sombre de l'inventaire. Qualite Divin (au-dessus de
l'Astrale, Legendaire).

La valeur et les echanges sont ailleurs : CoinItemRegistry, CoinWallet, WithdrawCommand, BankPage (Bank.ui par
scripts/npcs/gen_bank_ui.py), BarterShops/Bank_Npc.json, scripts/economie/gen_economie.py.

Usage : python3 scripts/coins/gen_imperial_coin.py <src/main/resources>
"""
import json, math, os, sys

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "rune_aura_icons"))
import png  # noqa: E402

RES = sys.argv[1]
COMMON = os.path.join(RES, "Common")
ITEMS = os.path.join(RES, "Server", "Item", "Items", "Coins")

NIGHT = (34, 6, 22)
CRIMSON = (120, 14, 44)
PURPLE = (92, 22, 96)
GOLD_DARK = (150, 96, 22)
GOLD = (232, 176, 52)
GOLD_LIGHT = (255, 236, 150)
RIM = (246, 206, 98)
RIM_DARK = (176, 120, 30)


def mix(a, b, t):
    t = max(0.0, min(1.0, t))
    return [a[i] + (b[i] - a[i]) * t for i in range(3)]


def field(u, v):
    """Fond pourpre (cramoisi au centre, violet vers le bord) et soleil d'or a douze rayons."""
    r = math.hypot(u, v)
    a = math.atan2(v, u)
    c = mix(CRIMSON, PURPLE, r * 1.1)
    c = mix(c, NIGHT, max(0.0, r - 0.75) * 2.5)
    rays = max(0.0, math.cos(6 * a)) ** 6 * max(0.0, 1 - r * 1.15)
    core = max(0.0, 1 - r * 2.6)
    gold = mix(GOLD_DARK, GOLD, 0.4 + 0.6 * rays) if rays > 0.08 else None
    if gold is not None:
        c = mix(c, gold, min(1.0, rays * 1.6))
    if core > 0:
        c = mix(c, mix(GOLD, GOLD_LIGHT, core), min(1.0, core * 3))
    return c


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


def paint(px, cells, sparkles):
    xs = [c[0] for c in cells]
    ys = [c[1] for c in cells]
    cx, cy = (min(xs) + max(xs)) / 2.0, (min(ys) + max(ys)) / 2.0
    rx, ry = (max(xs) - min(xs) + 1) / 2.0, (max(ys) - min(ys) + 1) / 2.0
    for x, y in cells:
        u, v = (x - cx) / rx, (y - cy) / ry
        edge = any((x + dx, y + dy) not in cells for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
        c = mix(RIM, RIM_DARK, 0.5 + 0.5 * (v - u) * 0.7) if edge else field(u, v)
        px[y][x][:3] = [int(round(i)) for i in c]
    for su, sv in sparkles:
        x, y = int(round(cx + su * rx)), int(round(cy + sv * ry))
        if (x, y) in cells:
            px[y][x][:3] = list(GOLD_LIGHT)
    for y, row in enumerate(px):                                    # tranche : or, nuance du modele d'origine gardee
        for x, p in enumerate(row):
            if p[3] > 0 and (x, y) not in cells:
                lum = (p[0] * 0.3 + p[1] * 0.59 + p[2] * 0.11) / 255.0
                p[:3] = [int(round(i)) for i in mix(RIM_DARK, RIM, lum * 1.2)]


def recolor(src, dst, sparkles):
    w, h, px = png.read(src)
    paint(px, face_cells(px), sparkles)
    png.write(dst, w, h, px)


recolor(os.path.join(COMMON, "Items/Coins/Elda_Zenith_Coins.png"), os.path.join(COMMON, "Items/Coins/Elda_Imperial_Coins.png"),
        [(-0.5, -0.4), (0.52, 0.4)])
recolor(os.path.join(COMMON, "Icons/ItemsGenerated/Elda_Zenith_Coins_Icone.png"), os.path.join(COMMON, "Icons/ItemsGenerated/Elda_Imperial_Coins_Icone.png"),
        [(-0.6, -0.42), (0.6, -0.32), (0.45, 0.58), (-0.36, 0.64)])

with open(os.path.join(ITEMS, "Elda_Zenith_Coins.json"), encoding="utf-8") as f:
    item = json.load(f)
item["TranslationProperties"]["Name"] = "Pièce Impériale"
item["TranslationProperties"]["Description"] = ("Monnaie d'Eldanior frappée au sceau de l'Empereur : elle vaut dix pièces astrales "
                                                "(un million de cuivres). Elle se dépose et se change chez le banquier.")
item["BlockType"]["CustomModelTexture"] = [{"Texture": "Items/Coins/Elda_Imperial_Coins.png", "Weight": 1} for _ in range(2)]
item["BlockType"]["ParticleColor"] = "#d8a63a"
item["BlockType"]["Particles"][0]["Color"] = "#ffe08a"
item["Icon"] = "Icons/ItemsGenerated/Elda_Imperial_Coins_Icone.png"
item["Quality"] = "Divin"
with open(os.path.join(ITEMS, "Elda_Imperial_Coins.json"), "w", encoding="utf-8") as f:
    json.dump(item, f, indent=2, ensure_ascii=False)
    f.write("\n")
print("Piece Imperiale ecrite : item, texture et icone.")
