"""Armures de la famille Runkandel, lignee noble de guerriers tueurs de dragons : trois tenues de 4 pieces, trois rangs.

  Recrue      Rare        brigandine de velours cramoisi cloutee de laiton, hauberk de mailles, bassinet ouvert, bottes cirees
  Capitaine   Epique      armure de parade cannelee, rinceaux d'or, bourguignotte a panache, cape de velours damasse
  Patriarche  Legendaire  acier noir mat grave d'or, armet couronne aux cornes d'ivoire, cape de velours noir bordee de fourrure
                          brodee d'or, medaillon au dragon, longs gantelets de cuir, bottes de montagne

Armoiries : voir blason.py (de gueules a la tete de dragon d'or, transpercee d'une epee d'argent). Le feu de dragon se retrouve
dans une part de la resistance (cle Fire), que le passif de famille viendra completer.

Densite : 4 px par unite (les kimonos restent a 2). Les volumes sont des prismes octogonaux (octa) plutot que des boites, et
les faces jamais visibles ne prennent pas de place dans l'atlas (hidden).

Usage : python3 scripts/armures/gen_runkandel.py <src/main/resources> [dossier_apercu]
"""
import math
import random
import sys

import armor_kit
from armor_kit import *  # noqa: F401,F403
from armor_kit import HIDE_HELMET, coplanar_report, write_sets
import blason
from blason import arms_sized, curve, fill as fill_poly, layer

armor_kit.set_density(4, 1024)
D = 4

RES = sys.argv[1]
PREVIEW = sys.argv[2] if len(sys.argv) > 2 else None
MODEL_DIR = "Items/Armors/Runkandel"

VELVET = (128, 18, 30)
VELVET_DARK = (70, 8, 16)
VELVET_LIGHT = (186, 44, 52)
LEATHER = (98, 58, 34)
LEATHER_DARK = (46, 28, 22)
LEATHER_LIGHT = (156, 104, 66)
MAIL_LIGHT = (196, 202, 212)
MAIL_DARK = (58, 62, 74)
STEEL = (140, 148, 164)
STEEL_DARK = (74, 82, 100)
STEEL_LIGHT = (206, 214, 226)
GOLD_MID = (222, 170, 70)
GOLD_SHADE = (128, 82, 28)
GOLD_BRIGHT = (255, 232, 156)
NIGHT = (40, 40, 46)                       # acier noir mat du Patriarche
NIGHT_DARK = (15, 15, 19)
NIGHT_LIGHT = (82, 82, 92)
GOLD_MATTE = (186, 146, 74)                # or mat des garnitures
GOLD_MATTE_DARK = (100, 70, 30)
GOLD_MATTE_LIGHT = (222, 190, 122)
BLACK_LEATHER = (32, 29, 29)
BLACK_LEATHER_DARK = (12, 11, 12)
BLACK_LEATHER_LIGHT = (78, 72, 70)
VELVET_BLACK = (26, 24, 28)
VELVET_BLACK_DARK = (8, 8, 10)
VELVET_BLACK_LIGHT = (60, 56, 62)
FUR = (44, 36, 32)
FUR_DARK = (16, 13, 12)
FUR_LIGHT = (104, 90, 76)
IVORY_FUR = (244, 240, 230)
RUBY = (214, 30, 44)
RUBY_LIGHT = (255, 150, 140)
EMBER = (255, 118, 40)
HORN_IVORY = (232, 220, 192)
HORN_SHADE = (150, 122, 86)


# ---------------------------------------------------------------------------------------------------------------------
# Peintures : gravure, cannelures, velours, hermine
# ---------------------------------------------------------------------------------------------------------------------
def rinceau(img, y0, y1, ground, gold=GOLD_MID, dark=GOLD_SHADE, period=34):
    """Frise de rinceaux : une tige ondulee et des volutes alternees, sur un fond plein."""
    w = len(img[0])
    y0, y1 = int(y0), int(y1)
    rect(img, 0, y0, w, y1, ground)
    mid, amp = (y0 + y1) / 2.0, max(1.5, (y1 - y0) * 0.24)
    for x in range(w):
        y = mid + amp * math.sin(x * 2 * math.pi / period)
        put(img, x, y, gold)
        put(img, x, y + 1, dark)
    for k in range(0, w + period, period // 2):
        sign = 1 if (k // (period // 2)) % 2 == 0 else -1
        cx, cy = k + period / 4.0, mid - sign * amp * 0.1
        for i in range(16):
            a = i * 0.62 * sign
            r = (1 - i / 16.0) * amp * 1.05
            put(img, cx + math.cos(a) * r, cy + math.sin(a) * r, gold)
        disc(img, cx, cy, 0.9, GOLD_BRIGHT)
    rect(img, 0, y0, w, y0 + 1, gold)
    rect(img, 0, y1 - 1, w, y1, gold)


def rinceau_v(img, x0, x1, ground, gold=GOLD_MID, dark=GOLD_SHADE):
    """Meme frise, a la verticale."""
    h = len(img)
    width = int(x1 - x0)
    tmp = canvas(h, width, ground)
    rinceau(tmp, 0, width, ground, gold, dark)
    for y in range(h):
        for i in range(width):
            img[y][int(x0) + i][:3] = list(tmp[i][y][:3])


def steel(c=STEEL, dark=STEEL_DARK, light=STEEL_LIGHT, gold=None, ground=None, flutes=0, lames=0, edge=None, bands=(), spine=False, rivets=0, emblem=None, shade=1.0,
          sheen=0.36, grain=0.06, emblem_faces=("front",), runes=()):
    """Acier poli : reflet courbe, cannelures douces, lames, frises gravees (bands = [(position 0-1, hauteur px)]).
    edge : filet d'or "bottom", "top" ou "both". L'or reste rare : c'est lui qui fait le noble, pas sa quantite."""
    def paint(face, w, h, seed):
        img = canvas(w, h, c)
        side = face not in ("top", "bottom")
        for y in range(h):
            for x in range(w):
                u = (x + 0.5) / w
                k = 0.5 + sheen * math.cos((u - 0.36) * math.pi * 1.5)
                k += grain * (fbm(x * 0.05 + seed, y * 0.05) - 0.5)
                if grain > 0.08:                                       # acier forge : martelage
                    k += grain * 0.6 * (noise(x * 0.35 + seed, y * 0.35) - 0.5)
                if flutes and side:
                    t = (x % flutes) / float(flutes)
                    k += 0.13 * math.sin(t * 2 * math.pi)
                    if x % flutes == 0:
                        k -= 0.1
                col = mix(dark, c, k * 2) if k < 0.5 else mix(c, light, (k - 0.5) * 2)
                img[y][x][:3] = list(col)
        if side:
            if lames:
                for i in range(1, lames):
                    y0 = int(h * i / float(lames))
                    rect(img, 0, y0 - 2, w, y0 - 1, light, 0.6)
                    rect(img, 0, y0 - 1, w, y0 + 1, tuple(v * 0.42 for v in dark))
            for v, size in bands:
                y0 = int(v * h)
                rinceau(img, y0, y0 + size, ground or tuple(v2 * 0.5 for v2 in dark))
            for v, size in runes:
                y0 = int(v * h)
                rune_band(img, y0, y0 + size, ground or tuple(v2 * 0.5 for v2 in dark), gold or GOLD_MID)
            if spine and face in ("front", "back"):
                rinceau_v(img, w // 2 - 5, w // 2 + 5, ground or tuple(v2 * 0.5 for v2 in dark))
            if edge in ("top", "both") and gold:
                rect(img, 0, 0, w, 2, gold)
            if edge in ("bottom", "both") and gold:
                rect(img, 0, h - 2, w, h, gold)
            if rivets and gold:
                for x in range(rivets // 2, w, rivets):
                    disc(img, x, h - 7, 1.5, gold)
                    put(img, x - 1, h - 8, GOLD_BRIGHT)
        if emblem and face in emblem_faces:
            emblem(img, w, h)
        return finish(img, face, shade, edges=False)
    return paint


def rune_band(img, y0, y1, ground, gold=GOLD_MID):
    """Frise de runes ornees : glyphes a hampe, branches et boucles, separes par des points d'or."""
    w = len(img[0])
    y0, y1 = int(y0), int(y1)
    rect(img, 0, y0, w, y1, ground)
    rect(img, 0, y0, w, y0 + 1, gold)
    rect(img, 0, y1 - 1, w, y1, gold)
    top, bottom = y0 + 2.5, y1 - 3.0
    gh = bottom - top
    gw = max(4.0, gh * 0.62)
    x, i = 3.0, 0
    while x + gw < w - 2:
        rnd = random.Random(i * 7919 + 13)
        cx = x + gw / 2.0
        stroke(img, [(cx, top), (cx, bottom)], gold, 0.5)
        for _ in range(rnd.randint(1, 3)):
            kind = rnd.randint(0, 4)
            ya = top + gh * rnd.choice((0.0, 0.25, 0.5))
            if kind == 0:
                stroke(img, [(cx, ya), (x, ya + gh * 0.3)], gold, 0.45)
            elif kind == 1:
                stroke(img, [(cx, ya), (x + gw, ya + gh * 0.3)], gold, 0.45)
            elif kind == 2:
                stroke(img, [(x, ya), (cx, ya + gh * 0.25), (x + gw, ya)], gold, 0.45)
            elif kind == 3:
                for a in range(0, 360, 30):
                    put(img, cx + gw * 0.3 * math.cos(math.radians(a)), ya + gh * 0.2 + gh * 0.18 * math.sin(math.radians(a)), gold)
            else:
                stroke(img, [(cx, ya + gh * 0.5), (x + gw, ya + gh * 0.2), (x + gw, ya + gh * 0.55)], gold, 0.45)
        x += gw + 3
        disc(img, x - 1.5, (top + bottom) / 2.0, 0.7, gold)
        i += 1


def fur(c=FUR, dark=FUR_DARK, light=FUR_LIGHT, shaggy=0):
    """Fourrure sombre et dense : meches qui tombent, pointes plus claires. shaggy : bas effiloche (pixels)."""
    def paint(face, w, h, seed):
        img = canvas(w, h, c)
        for y in range(h):
            for x in range(w):
                strand = noise(x * 0.75 + seed, y * 0.09)
                tip = noise(x * 0.75 + seed, y * 0.09 + 0.6)
                k = strand * 1.1 + 0.25 * fbm(x * 0.08, y * 0.08)
                col = mix(dark, c, k * 1.6) if k < 0.62 else mix(c, light, (k - 0.62) * 1.6 * (0.5 + tip))
                img[y][x][:3] = list(col)
        if shaggy and face not in ("top", "bottom"):
            for x in range(w):
                cut = int(shaggy * noise(x * 0.6 + seed, 1.7))
                for y in range(h - cut, h):
                    img[y][x][3] = 0
        return finish(img, face, edges=False)
    return paint


def engraved(art, gold=GOLD_MID, inlay=(70, 66, 64), hatch=True):
    """Gravure a partir d'un dessin : trait d'or sur les contours et entre les couleurs, fond legerement poli et hachure."""
    h, w = len(art), len(art[0])
    out = layer(w, h)
    for y in range(h):
        for x in range(w):
            p = art[y][x]
            if p[3] < 128:
                continue
            edge = False
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                nx, ny = x + dx, y + dy
                if not (0 <= nx < w and 0 <= ny < h) or art[ny][nx][3] < 128:
                    edge = True
                    break
                q = art[ny][nx]
                if abs(p[0] - q[0]) + abs(p[1] - q[1]) + abs(p[2] - q[2]) > 90:
                    edge = True
                    break
            if edge:
                out[y][x] = [gold[0], gold[1], gold[2], 255]
            elif hatch and (x + y) % 4 == 0:
                out[y][x] = [gold[0] * 0.55, gold[1] * 0.55, gold[2] * 0.55, 200]
            else:
                bright = (p[0] + p[1] + p[2]) / 765.0
                out[y][x] = [inlay[0] * (0.7 + bright * 0.6), inlay[1] * (0.7 + bright * 0.6), inlay[2] * (0.7 + bright * 0.6), 150]
    return out


def embroider(art):
    """Fil d'or : point de satin en biais sur tout le dessin."""
    out = []
    for y, row in enumerate(art):
        r = []
        for x, p in enumerate(row):
            k = 0.8 + 0.32 * (((x + 2 * y) % 5) / 4.0)
            r.append([min(255, p[0] * k), min(255, p[1] * k), min(255, p[2] * k), p[3]])
        out.append(r)
    return out


def gold_plate(lames=0, gem=False):
    return steel(GOLD_MID, GOLD_SHADE, GOLD_BRIGHT, gold=GOLD_BRIGHT, lames=lames)


def damask_on(x, y, p=44):
    """Motif de damas : treillis en ogive et grenade, en quinconce. Vrai sur un trait du motif."""
    col = math.floor(x / p)
    yy = y + (col % 2) * p * 0.75
    a = ((x % p) / float(p) - 0.5) * 2
    b = ((yy % (p * 1.5)) / (p * 1.5) - 0.5) * 2
    if abs(abs(a) - (0.5 + 0.5 * math.cos(math.pi * b))) < 0.075:
        return True
    r = math.hypot(a * 1.5, b * 1.45)
    th = math.atan2(b, a)
    return 0.1 < r < 0.46 * (0.72 + 0.28 * math.cos(th * 6))


def velvet(ground=VELVET, light=VELVET_LIGHT, dark=VELVET_DARK, motif=True, hem=None, shade=1.0, seedless=False):
    """Velours damasse, ton sur ton : plis larges et motif en relief."""
    def paint(face, w, h, seed):
        img = canvas(w, h, ground)
        for y in range(h):
            for x in range(w):
                f = fbm(x * 0.028 + seed * 0.2, y * 0.01 + seed * 0.1)
                c = mix(dark, ground, f * 1.6) if f < 0.62 else mix(ground, light, (f - 0.62) * 2)
                if motif and damask_on(x, y):
                    c = mix(c, light, 0.42)
                img[y][x][:3] = list(c)
        if hem and face not in ("top", "bottom"):
            rect(img, 0, h - 4, w, h, hem)
            rect(img, 0, 0, w, 3, hem)
        return finish(img, face, shade, edges=False)
    return paint


def brigandine(face, w, h, seed):
    """Velours cramoisi riveté de laiton : rangs de clous en quinconce, couture centrale."""
    img = velvet(motif=False)(face, w, h, seed)
    if face not in ("top", "bottom"):
        for j, y in enumerate(range(6, h, 11)):
            for x in range(5 + (j % 2) * 6, w, 12):
                disc(img, x + 0.5, y + 0.5, 1.5, GOLD_SHADE)
                put(img, x, y, GOLD_BRIGHT)
        if face == "front":
            rect(img, w // 2 - 1, 0, w // 2 + 1, h, VELVET_DARK)
    return img


def mail(light=MAIL_LIGHT, dark=MAIL_DARK, hem=None, shade=1.0, bars=0):
    """Cotte de mailles : anneaux ronds, rangs decales. bars : renforts d'acier rivetes tous les bars pixels."""
    def paint(face, w, h, seed):
        img = canvas(w, h, dark)
        for y in range(h):
            row = y // 4
            for x in range(w):
                px, py = (x + row * 3) % 6 - 2.5, y % 4 - 1.5
                r = math.hypot(px * 0.8, py * 1.05)
                if 1.0 < r < 2.3:
                    k = 0.45 + 0.55 * ((py < 0) * 0.7 + (px < 0) * 0.3)
                    img[y][x][:3] = list(mix(dark, light, k))
                else:
                    img[y][x][:3] = [v * 0.42 for v in dark]
        if bars and face not in ("top", "bottom"):
            for y0 in range(bars // 2, h - 3, bars):
                rect(img, 0, y0, w, y0 + 3, tuple(v * 0.7 for v in dark))
                rect(img, 0, y0, w, y0 + 1, tuple(min(255, v * 1.4) for v in dark))
                for x in range(4, w, 8):
                    put(img, x, y0 + 1, light)
        if hem and face not in ("top", "bottom"):
            rect(img, 0, h - 5, w, h, hem)
            rect(img, 0, h - 6, w, h - 5, GOLD_SHADE)
        return finish(img, face, shade, edges=False)
    return paint


def fine_leather(ground=LEATHER, stitch=GOLD_MID, edge=None, shade=1.0, tooled=False, dark=LEATHER_DARK, light=LEATHER_LIGHT):
    """Cuir cire : reflet doux, surpiqure, gaufrage facultatif."""
    def paint(face, w, h, seed):
        img = canvas(w, h, ground)
        for y in range(h):
            for x in range(w):
                u = (x + 0.5) / w
                k = 0.5 + 0.5 * math.cos((u - 0.36) * math.pi * 1.5) + 0.25 * (fbm(x * 0.1 + seed, y * 0.1) - 0.5)
                c = mix(dark, ground, k * 2) if k < 0.5 else mix(ground, light, (k - 0.5) * 1.5)
                if tooled and damask_on(x, y, 30):
                    c = tuple(v * 0.78 for v in c)
                img[y][x][:3] = list(c)
        if face not in ("top", "bottom"):
            if edge:
                rect(img, 0, 0, w, 3, edge)
                rect(img, 0, h - 3, w, h, edge)
            if stitch and h > 14:
                stitch_line(img, [(1, 5), (w - 1, 5)], stitch, 2)
                stitch_line(img, [(1, h - 6), (w - 1, h - 6)], stitch, 2)
        return finish(img, face, shade, edges=False)
    return paint


def ermine(face, w, h, seed):
    """Hermine : fourrure blanche, mouchetures noires en quinconce."""
    img = canvas(w, h, IVORY_FUR)
    for y in range(h):
        for x in range(w):
            k = 0.86 + 0.2 * noise(x * 0.5 + seed, y * 0.18)
            for i in range(3):
                img[y][x][i] *= k
    for j, y in enumerate(range(4, h, 18)):
        for x in range(6 + (j % 2) * 10, w, 20):
            poly_img(img, [(x - 2, y), (x + 2, y), (x + 3, y + 7), (x, y + 10), (x - 3, y + 7)], (20, 20, 24))
            put(img, x, y - 1, (20, 20, 24))
    return finish(img, face, edges=False)


def poly_img(img, pts, c):
    y0, y1 = int(min(p[1] for p in pts)), int(max(p[1] for p in pts)) + 1
    for y in range(max(0, y0), min(len(img), y1 + 1)):
        cross = []
        for (ax, ay), (bx, by) in zip(pts, pts[1:] + pts[:1]):
            if (ay <= y + 0.5) != (by <= y + 0.5):
                cross.append(ax + (y + 0.5 - ay) * (bx - ax) / (by - ay))
        cross.sort()
        for i in range(0, len(cross) - 1, 2):
            for x in range(int(cross[i]), int(cross[i + 1]) + 1):
                put(img, x, y, c)


def engraved_scales(ground=NIGHT, rim=GOLD_MID, size=16, edge=True):
    """Ecailles gravees : dernier souvenir du dragon, filet d'or sur l'acier."""
    def paint(face, w, h, seed):
        img = canvas(w, h, ground)
        for y in range(h):
            row = size * 0.62
            j = math.floor(y / row)
            for x in range(w):
                best = None
                for jj in (j - 1, j):
                    ox = size / 2.0 if jj % 2 else 0.0
                    cx = ox + round((x - ox) / float(size)) * size
                    dy = y - jj * row
                    t = abs(x - cx) / (size * 0.5) + dy / (size * 0.95)
                    if 0 <= dy and t <= 1.0:
                        best = t
                        if jj == j - 1:
                            break
                c = ground
                if best is not None:
                    c = rim if best > 0.86 else mix(tuple(min(255, v * 1.7) for v in ground), ground, best)
                img[y][x][:3] = list(c)
        if edge and face not in ("top", "bottom"):
            rect(img, 0, 0, w, 3, rim)
            rect(img, 0, h - 3, w, h, rim)
        return finish(img, face, edges=False)
    return paint


def glow(c):
    def paint(face, w, h, seed):
        return canvas(w, h, c)
    return paint


def gem(c=RUBY, light=RUBY_LIGHT):
    """Cabochon : reflet haut a gauche."""
    def paint(face, w, h, seed):
        img = canvas(w, h, c)
        for y in range(h):
            for x in range(w):
                d = math.hypot((x + 0.5) / w - 0.34, (y + 0.5) / h - 0.3)
                img[y][x][:3] = list(mix(light, tuple(v * 0.55 for v in c), min(1.0, d * 1.5)))
        return img
    return paint


def ivory(face, w, h, seed):
    img = canvas(w, h, HORN_IVORY)
    for y in range(h):
        for x in range(w):
            ring = 0.95 + 0.05 * math.sin(y * 0.9)
            c = mix(HORN_IVORY, HORN_SHADE, 0.4 * (1 - (x + 0.5) / w))
            img[y][x][:3] = [v * ring for v in c]
    return finish(img, face, edges=False)


# ---------------------------------------------------------------------------------------------------------------------
# Formes
# ---------------------------------------------------------------------------------------------------------------------
def octa(m, name, pos, size, paint, cut, top=True, bottom=True, corners=None):
    """Prisme octogonal d'axe Y : section (w, d) aux coins coupes de cut. corners : peinture des pans coupes."""
    w, h, d = size
    x, y, z = pos
    caps = () if top else ("top",)
    caps += () if bottom else ("bottom",)
    nodes = [
        box(m, name + "_L", pos, (w, h, d - 2 * cut), paint, hidden=("front", "back") + caps),
        box(m, name + "_P", pos, (w - 2 * cut, h - 0.5, d), paint, hidden=("left", "right") + caps),
    ]
    thick, s2 = min(0.75, cut * 0.4), math.sqrt(0.5)                  # les extremites restent enfouies, meme pour un petit pan
    length = cut * math.sqrt(2) + 0.1
    for i, (sx, sz) in enumerate(((1, 1), (-1, 1), (1, -1), (-1, -1))):
        cx = sx * (w / 2.0 - cut / 2.0 - thick / 2.0 * s2)
        cz = sz * (d / 2.0 - cut / 2.0 - thick / 2.0 * s2)
        nodes.append(box(m, "%s_C%d" % (name, i), (x + cx, y, z + cz), (length, h - 1.0, thick), corners or paint,
                         q=q_axis((0, 1, 0), math.degrees(math.atan2(sx, sz))), hidden=("back", "left", "right") + caps))
    return nodes


def yaw(deg):
    return q_axis((0, 1, 0), deg)


def harden(img):
    out = []
    for row in img:
        r = []
        for p in row:
            r.append([p[0], p[1], p[2], 255] if p[3] >= 128 else [0, 0, 0, 0])
        out.append(r)
    return out


_arms_cache = {}


def arms_image(rank, width_u, crown=False, wings=False):
    key = (rank, width_u, crown, wings)
    if key not in _arms_cache:
        _arms_cache[key] = arms_sized(rank, int(width_u * D), crown, wings, px=max(48, int(width_u * D * (0.5 if wings else 1.0))))
    return _arms_cache[key]


def arms_quad(m, name, pos, width_u, rank, crown=False, wings=False, q=ID_Q):
    """Ecu decoupe en alpha, pose comme une piece d'applique."""
    return quad(m, name, pos, harden(arms_image(rank, width_u, crown, wings)), q)


def stamp(img, art, x0, y0):
    for y, row in enumerate(art):
        for x, p in enumerate(row):
            if p[3] > 0 and 0 <= y0 + y < len(img) and 0 <= x0 + x < len(img[0]):
                a = p[3] / 255.0
                q = img[y0 + y][x0 + x]
                for i in range(3):
                    q[i] = p[i] * a + q[i] * (1 - a)


def banner(w_u, h_u, ground=VELVET, light=VELVET_LIGHT, dark=VELVET_DARK, hem=GOLD_MID, tip=0.16, fringe=True, motif=True, art=None):
    """Pan d'etoffe en pointe, galon et frange d'or. art : armoiries posees en haut du pan."""
    w, h = int(w_u * D), int(h_u * D)
    img = velvet(ground, light, dark, motif)("front", w, h, 7)
    fringe_h = 10 if fringe else 0
    if art:
        stamp(img, art, (w - len(art[0])) // 2, int(h * 0.1))
    for x in range(w):
        u = abs((x + 0.5) / w * 2 - 1)
        bottom = h - 1 - int(tip * h * u)
        for y in range(h):
            if y > bottom:
                img[y][x][3] = 0
            elif fringe and y > bottom - fringe_h:
                if x % 3 == 2:
                    img[y][x][3] = 0
                else:
                    img[y][x][:3] = list(mix(GOLD_BRIGHT, GOLD_SHADE, (y - bottom + fringe_h) / float(fringe_h)))
            elif y > bottom - fringe_h - 5:
                img[y][x][:3] = list(hem)
            elif x < 4 or x >= w - 4:
                img[y][x][:3] = list(hem)
            elif x < 6 or x >= w - 6:
                img[y][x][:3] = list(GOLD_SHADE)
    return img


def cape_image(w, h, seed, rank, arms_width, ermine_edge=False, crown=False, wings=False, dark_base=False):
    """Manteau de velours damasse : galon d'or, frange, armoiries ; bandes d'hermine pour le Patriarche."""
    img = velvet()("front", w, h, seed)
    for y in range(h):
        k = 1 - 0.36 * y / float(h)
        for x in range(w):
            for i in range(3):
                img[y][x][i] *= k
    rect(img, 0, 0, w, 7, GOLD_MID)
    rinceau(img, 0, 14, VELVET_DARK)
    side = 0
    if ermine_edge:
        side = 22
        fur = ermine("front", side, h, 5)
        for y in range(h):
            for x in range(side):
                img[y][x][:3] = list(fur[y][x][:3])
                img[y][w - 1 - x][:3] = list(fur[y][side - 1 - x][:3])
    rect(img, side, 0, side + 4, h, GOLD_MID)
    rect(img, w - side - 4, 0, w - side, h, GOLD_MID)
    rect(img, side + 4, 0, side + 5, h, GOLD_SHADE)
    rect(img, w - side - 5, 0, w - side - 4, h, GOLD_SHADE)
    art = arms_sized(rank, arms_width, crown, wings, px=int(arms_width * (0.45 if wings else 1.0)))
    stamp(img, art, (w - len(art[0])) // 2, int(h * 0.1))
    rinceau(img, h - 34, h - 16, VELVET_DARK)
    for y in range(h - 16, h):
        for x in range(w):
            if x % 3 == 2:
                img[y][x][3] = 0
            else:
                img[y][x][:3] = list(mix(GOLD_BRIGHT, GOLD_SHADE, (y - h + 16) / 16.0))
    return img


def cape_quads(m, big, segments, top_width, bottom_width):
    """Cape en pans plats (comme la cape vanilla) : segments = [(haut, bas)], tranches successives de big.
    La largeur va de top_width a bottom_width sans marche : chaque pan est aussi large que son bas, le surplus est coupe en alpha."""
    H, W = len(big), len(big[0])
    total = sum(math.hypot(b[1] - a[1], b[2] - a[2]) for a, b in segments)

    def width_at(d):
        return top_width + (bottom_width - top_width) * d / total

    done = 0.0
    nodes = []
    for i, (a, b) in enumerate(segments):
        L = math.hypot(b[1] - a[1], b[2] - a[2])
        h = int(math.ceil(L * D)) + 1                               # un pixel de recouvrement entre deux pans
        w = int(math.ceil(width_at(done + L) * D))
        w += w % 2
        img = []
        for y in range(h):
            along = done + L * (y + 0.5) / h
            half = width_at(along) * D / 2.0
            sy = min(H - 1, int(along / total * H))
            row = []
            for x in range(w):
                u = (x + 0.5 - w / 2.0) / half
                if abs(u) > 1:
                    row.append([0, 0, 0, 0])
                else:
                    row.append(list(big[sy][min(W - 1, int((u + 1) / 2.0 * W))]))
            img.append(row)
        done += L
        dy, dz = a[1] - b[1], a[2] - b[2]
        nodes.append(quad(m, "Cape_%d" % i, tuple((a[k] + b[k]) / 2.0 for k in range(3)), img, q_axis((1, 0, 0), math.degrees(math.atan2(dz, dy)))))
    return nodes


def fleuron(height=11.0, width=8.0, big=True):
    """Fleuron de couronne, decoupe en alpha : gemme au coeur, perle au sommet."""
    W, H = int(width * D), int(height * D)
    img = layer(W, H)
    pts = [(0.5, 0.0), (0.62, 0.3), (0.86, 0.26), (0.72, 0.52), (0.98, 0.58), (0.74, 1.0), (0.26, 1.0), (0.02, 0.58), (0.28, 0.52), (0.14, 0.26), (0.38, 0.3)]
    if not big:
        pts = [(0.5, 0.0), (0.66, 0.4), (0.74, 1.0), (0.26, 1.0), (0.34, 0.4)]
    fill_poly(img, [(x * W, y * H) for x, y in pts], GOLD_MID)
    for y in range(H):
        for x in range(W):
            if img[y][x][3]:
                d = abs((x + 0.5) / W - 0.5) * 2
                img[y][x][:3] = list(mix(GOLD_BRIGHT, GOLD_SHADE, d))
    disc(img, W / 2.0, H * 0.72, W * 0.11, RUBY)
    put(img, W / 2.0 - 1, H * 0.7, RUBY_LIGHT)
    return finish(img, "front", edges=False)


def plume_image(w_u=40, h_u=22, seed=3, ground=VELVET_LIGHT, tip=(240, 226, 214)):
    """Plume vue de profil, pleine : le pied est a droite (+Z), elle monte, se couche vers l'arriere et retombe."""
    W, H = int(w_u * D), int(h_u * D)
    img = layer(W, H)
    spine = curve([(0.97, 0.94), (0.8, 0.42), (0.5, 0.14), (0.2, 0.22), (0.03, 0.72)], 30)
    n = len(spine)
    for i in range(n):
        t = i / float(n - 1)
        half = (0.05 + 0.15 * math.sin(math.pi * min(1.0, t * 1.02)) ** 0.8) * H
        cx, cy = spine[i][0] * W, spine[i][1] * H
        for y in range(int(cy - half), int(cy + half) + 1):
            for x in range(int(cx - half), int(cx + half) + 1):
                d = math.hypot(x - cx, y - cy)
                if d <= half and 0 <= y < H and 0 <= x < W:
                    strand = 0.74 + 0.4 * noise(x * 0.16 + y * 0.32, y * 0.1 - x * 0.08 + seed)
                    c = mix(VELVET_DARK, ground, 1 - d / max(1.0, half) * 0.8)
                    c = mix(c, tip, smooth(t * 1.6 - 0.75))
                    img[y][x] = [c[0] * strand, c[1] * strand, c[2] * strand, 255]
    for i in range(n):
        x, y = int(spine[i][0] * W), int(spine[i][1] * H)
        if 0 <= y < H and 0 <= x < W:
            img[y][x] = [246, 232, 206, 255]
    edge = [row[:] for row in img]
    for y in range(H):                                                     # barbes : bord dechiquete
        for x in range(W):
            if edge[y][x][3] and noise(x * 0.5, y * 0.5 + seed) < 0.26:
                ny = y - 1
                if ny >= 0 and edge[ny][x][3] == 0:
                    img[y][x] = [0, 0, 0, 0]
    return harden(img)


def back_shell(m, paint, y, h, cut=8.5, x=16.0, z=15.5):
    """Dos et cotes d'un casque ouvert : trois pans droits et deux pans coupes."""
    nodes = [box(m, "Nuque", (0, y, -z), (2 * x - 2 * cut, h, 1.5), paint, hidden=("bottom", "front"))]
    z0, z1 = -z + cut, 3.0                                                     # le flanc rejoint la joue (z 2.5) sans jour
    side = box(m, "R-Flanc", (-x, y, (z0 + z1) / 2.0), (1.5, h, z1 - z0), paint, hidden=("bottom",))
    diag = box(m, "R-Flanc_Dos", (-x + cut / 2.0, y, -z + cut / 2.0), (cut * math.sqrt(2) + 0.5, h - 0.5, 1.25), paint, q=yaw(45), hidden=("bottom", "left", "right"))
    return nodes + sym(m, side, diag)


def arm_piece(m, upper, lower):
    return piece(m, "R-Arm", (-17, 0, 0), list(upper) + [piece(m, "R-Forearm", (-1, 0, 0), list(lower))])


def thigh_piece(m, kids):
    return piece(m, "R-Thigh", (-8, -1, 1), list(kids), offset=(0, -9, 0))


def body_chain(m, pelvis_kids, belly_kids, chest_kids, arm, thigh):
    chest = piece(m, "Chest", (0, 5, 0), list(chest_kids) + [arm, mirror(m, arm)], offset=(0, 11, 0))
    belly = piece(m, "Belly", (0, 8, 0), list(belly_kids) + [chest], offset=(0, 4, 0))
    return [piece(m, "Pelvis", (0, 12, 0), list(pelvis_kids) + [thigh, mirror(m, thigh), belly])]


def hands_chain(m, cuff_kids, hand_kids):
    hand = piece(m, "R-Hand", (0, -8, 0), list(hand_kids), offset=(0, -5, 0))
    right = piece(m, "R-Forearm", (-18, 40, 0), list(cuff_kids) + [hand])
    return [right, mirror(m, right)]


def legs_chain(m, waist_kids, thigh_kids, calf_kids, foot_kids):
    calf = piece(m, "R-Calf", (0, -12, 3), list(calf_kids), offset=(0, -12, -3))
    thigh = piece(m, "R-Thigh", (-8, -1, 1), list(thigh_kids) + [calf], offset=(0, -9, 0))
    foot = piece(m, "R-Foot", (-7.85, 7, -3), list(foot_kids), offset=(0, -3, 6))
    return [piece(m, "Pelvis", (0, 51, 0), list(waist_kids) + [thigh, mirror(m, thigh)]), foot, mirror(m, foot)]


def new_model(name):
    m = Model(name)
    m.reuse = True
    return m


def dome_stack(m, paint, band=None, y=4.5, x=33.0, z=32.0):
    """Timbre de casque arrondi. L'etage du bas contient les coins de la tete : son pan coupe reste petit."""
    nodes = []
    nodes += octa(m, "Crane_0", (0, y + 5.75, 0), (x, 11.5, z), paint, 3, top=False, bottom=False)
    nodes += octa(m, "Crane_1", (0, y + 12.25, 0), (x - 3, 1.75, z - 3), paint, 6, bottom=False, top=False)
    nodes += octa(m, "Crane_2", (0, y + 13.75, 0), (x - 8, 1.5, z - 8), paint, 6.5, bottom=False, top=False)
    nodes += octa(m, "Crane_3", (0, y + 15, 0), (x - 16, 1, z - 16), paint, 4.5, bottom=False)
    if band:
        nodes += octa(m, "Frontal", (0, y + 0.75, 0), (x + 1, 2.5, z + 1), band, 3.25)
    return nodes


# ---------------------------------------------------------------------------------------------------------------------
# Recrue : l'ecuyer d'une grande maison
# ---------------------------------------------------------------------------------------------------------------------
R_STEEL = steel()
R_STEEL_TRIM = steel(gold=GOLD_MID, edge="bottom")
R_MAIL = mail()
R_MAIL_HEM = mail(hem=GOLD_MID)
R_LEATHER = fine_leather()
R_LEATHER_TOOLED = fine_leather(LEATHER, GOLD_MID, GOLD_MID, tooled=True)
R_DARK_LEATHER = fine_leather(LEATHER_DARK, None)


def build_recrue_head():
    m = new_model("Head")
    kids = dome_stack(m, R_STEEL, band=steel(gold=GOLD_MID, edge="bottom", bands=((0.28, 5),), ground=(44, 46, 58)))
    kids += back_shell(m, R_MAIL_HEM, -5.5, 19, cut=2)
    kids += octa(m, "Coif", (0, -16, 0.5), (29, 4.5, 30), R_MAIL, 4, top=False)
    kids += [
        box(m, "Nasal", (0, 0.4, 16.9), (3, 11, 1), steel(GOLD_MID, GOLD_SHADE, GOLD_BRIGHT)),
        box(m, "Crete", (0, 20.4, 0), (1.5, 1, 14), steel(GOLD_MID, GOLD_SHADE, GOLD_BRIGHT)),
    ]
    kids += sym(m, rod(m, "R-Jugulaire", (-15.4, 4, 9), (-12.5, -13.2, 15.2), 1, R_LEATHER))
    kids.append(box(m, "Mentonniere", (0, -14.2, 15.4), (26, 1.75, 1.25), R_LEATHER))
    kids.append(arms_quad(m, "Ecusson", (0, 11.2, 16.75), 8, "recrue"))
    return m, [piece(m, "Head", (0, 0, 0), kids)]


def build_recrue_chest():
    m = new_model("Chest")
    spaulder = octa(m, "R-Spaliere", (-1.5, 8.5, 0), (14.5, 5.5, 17.5), R_LEATHER_TOOLED, 3.5)
    cap = octa(m, "R-Spaliere_Fer", (-2, 12, 0), (11.5, 2, 14), R_STEEL_TRIM, 3, bottom=False)
    sleeve = octa(m, "R-Manche", (-0.25, -1.5, 0), (12, 18, 15), R_MAIL, 2.5, top=False)
    fore = octa(m, "R-Manche_Bas", (0, 3.5, 0), (10.25, 10, 13.75), R_MAIL, 2, top=False)
    arm = arm_piece(m, spaulder + [] + cap + sleeve, fore)
    skirt = box(m, "R-Pan", (-1.25, 2.5, 0.5), (13.75, 12, 16.75), R_MAIL_HEM)
    thigh = thigh_piece(m, [skirt])
    pelvis = octa(m, "Hanches", (0, -0.5, 0), (29, 13, 22), R_MAIL, 3.25)
    pelvis.append(quad(m, "Tabard", (0, -9.5, 11.75), banner(13, 21, tip=0.2)))
    pelvis.append(quad(m, "Tabard_Dos", (0, -9.5, -11.75), banner(13, 21, tip=0.2)))
    belly = octa(m, "Ventre", (0, 1, 0), (29.5, 15.5, 23), brigandine, 4)
    belly += octa(m, "Ceinture", (0, -6, 0), (31.5, 3.5, 25), fine_leather(LEATHER_DARK, GOLD_MID), 4.25)
    belly.append(box(m, "Boucle", (0, -6, 12.9), (7, 5.5, 1.25), gold_plate()))
    chest = octa(m, "Torse", (0, 0, 0), (31.5, 24.5, 24), brigandine, 3.75)
    chest += octa(m, "Col", (0, 13.4, -0.5), (20.5, 3, 20.5), R_STEEL_TRIM, 4.5)
    chest.append(arms_quad(m, "Blason", (0, 1.25, 12.3), 15, "recrue"))
    return m, body_chain(m, pelvis, belly, chest, arm, thigh)


def build_recrue_hands():
    m = new_model("Hands")
    cuff = octa(m, "R-Bracelet", (0, -2.5, 0), (11.75, 11, 15.5), R_LEATHER_TOOLED, 2.5)
    flare = octa(m, "R-Revers", (0, 4, 0), (13, 3, 16.75), fine_leather(LEATHER_DARK, GOLD_MID, GOLD_MID), 3)
    glove = octa(m, "R-Gant", (0, -0.5, 0), (11, 13, 15), R_DARK_LEATHER, 0.75)
    knuckle = box(m, "R-Phalanges", (-5.9, -2.5, 0), (1, 4.5, 11), R_STEEL_TRIM)
    return m, hands_chain(m, cuff + flare, glove + [knuckle])


def build_recrue_legs():
    m = new_model("Legs")
    hose = velvet((62, 30, 34), (112, 62, 62), (32, 14, 18), motif=False)
    waist = octa(m, "Taille", (0, 0, 0), (27, 11, 20.5), hose, 1.25)
    upper = octa(m, "R-Chausse", (0, -1, 0), (11.5, 21, 13.5), hose, 1.25)
    knee = octa(m, "R-Genouillere", (0, 12, 3), (12, 7, 9.5), R_STEEL_TRIM, 1.5)
    shin = box(m, "R-Chausse_Bas", (0, 7.5, 0), (10.75, 9, 12.75), hose)
    boot = octa(m, "R-Botte", (0, -3.5, 0), (12, 16.5, 14.5), fine_leather(LEATHER_DARK, None), 1.75, top=False)
    turn = octa(m, "R-Botte_Revers", (0, 5.5, 0), (13.25, 3, 15.75), R_LEATHER_TOOLED, 2.25)
    shoe = octa(m, "R-Soulier", (-0.375, 0.5, 0), (15.25, 8.5, 21), fine_leather(LEATHER_DARK, None), 0.75)
    sole = box(m, "R-Semelle", (-0.375, -3.75, 0), (15.75, 1, 22), steel(LEATHER_DARK, (20, 12, 10), LEATHER))
    spur = box(m, "R-Bride", (-0.5, 2.25, -4.5), (15.75, 1, 12.5), gold_plate())
    return m, legs_chain(m, [] + waist, upper, knee + [shin] + boot + turn, shoe + [sole, spur])


# ---------------------------------------------------------------------------------------------------------------------
# Harnois de parade : base commune du Capitaine et du Patriarche
# ---------------------------------------------------------------------------------------------------------------------
class Harness:
    def __init__(self, c, dark, light, gold, ground, sheen=0.36, grain=0.06):
        kw = dict(c=c, dark=dark, light=light, gold=gold, ground=ground, sheen=sheen, grain=grain)
        self.plain = steel(**kw)
        self.trim = steel(edge="bottom", **kw)
        self.fluted = steel(flutes=16, **kw)
        self.lames = steel(lames=3, **kw)
        self.lames2 = steel(lames=2, **kw)
        self.frieze = steel(edge="both", bands=((0.3, 10),), **kw)
        self.spine = steel(spine=True, **kw)
        self.brow = steel(edge="bottom", **kw)
        self.gold = gold_plate()
        self.gold_lames = gold_plate(lames=4)
        self.chest = steel(flutes=16, edge="bottom", bands=((0.04, 12), (0.86, 12)), **kw)
        self.c, self.dark, self.light, self.ground = c, dark, light, ground


def pauldron(m, h, top, wide=False):
    extra = 2 if wide else 0
    out = []
    out += octa(m, "R-Epaule_Haut", (-2.25, 13, 0), (14, 2.5, 16.5), top, 4, bottom=False)
    out += octa(m, "R-Epaule_Coupe", (-2, 9.5, 0), (18 + extra, 4.75, 21.5 + extra), h.frieze, 5, top=True, bottom=False)
    out += octa(m, "R-Epaule_Lame1", (-3, 5.75, 0), (17 + extra, 3.5, 20.5 + extra), h.plain, 5, bottom=False)
    out += octa(m, "R-Epaule_Lame2", (-3.5, 2.75, 0), (15.75 + extra, 3.25, 19.25 + extra), h.plain, 5, bottom=False)
    out += octa(m, "R-Epaule_Lame3", (-3.75, 0, 0), (14.5 + extra, 3, 18 + extra), h.trim, 5)
    return out


def plate_arm(m, h, shoulder, lower_extra=()):
    upper = octa(m, "R-Canon", (-0.25, -2.5, 0), (12, 14, 15), h.fluted, 3, top=False)
    under = box(m, "R-Pli", (0, 3, 0), (10.75, 11, 14.25), mail(h.light, h.dark), hidden=("top", "bottom"))
    couter = octa(m, "R-Coudiere", (0, 7, -1), (13, 6, 15.5), h.trim, 3.5)
    wing = box(m, "R-Aile_Coude", (-6.9, 7, 2.5), (1, 6.5, 7), h.lames2)
    return arm_piece(m, list(shoulder) + upper, [under] + couter + [wing] + list(lower_extra))


def plate_gauntlet(m, h, cuff_extra=(), hand_extra=()):
    vamb = octa(m, "R-Canon_Bras", (0, -0.5, 0), (11.5, 14, 15), h.fluted, 2.5, bottom=False)
    flare = octa(m, "R-Evase", (0, -6.25, 0), (13.5, 3.5, 17.25), h.frieze, 3.5)
    glove = octa(m, "R-Gant", (0, 1.75, 0), (11.25, 8.5, 15.25), h.lames2, 0.75)
    fingers = octa(m, "R-Doigts", (0, -4.5, 0), (10.75, 6, 14.75), h.lames, 0.75, top=False)
    knuckle = box(m, "R-Phalanges", (-6, 0, 0), (1, 3, 11), h.gold_lames)
    return hands_chain(m, vamb + flare + list(cuff_extra), glove + fingers + [knuckle] + list(hand_extra))


def plate_legs(m, h, knee_extra=(), foot_extra=()):
    waist = octa(m, "Taille", (0, 0, 0), (27, 10, 20.5), mail(h.light, h.dark), 1.25)
    cuisse = octa(m, "R-Cuissard", (0, -0.5, 0), (12.25, 20.5, 14.25), h.fluted, 1.75)
    poleyn = octa(m, "R-Genouillere", (0, 11.5, 2.5), (12.75, 8, 10.5), h.frieze, 2)
    wing_out = box(m, "R-Aile_Genou", (-6.9, 11.5, 0), (1, 6, 7), h.lames2)
    greave = octa(m, "R-Greve", (0, 0.25, 0), (12, 23, 14.5), steel(c=h.c, dark=h.dark, light=h.light, gold=GOLD_MID, ground=h.ground, flutes=16, spine=True), 1.75)
    shoe = octa(m, "R-Soleret", (-0.375, 0.5, 0), (15.25, 8.5, 21.5), h.lames2, 1)
    toe = octa(m, "R-Pointe", (-0.5, -0.6, 11.5), (10.5, 5.25, 4), h.trim, 1.5)
    sole = box(m, "R-Semelle", (-0.375, -3.75, 0), (15.75, 1, 22), steel(h.dark, tuple(v * 0.5 for v in h.dark), h.c))
    return m, legs_chain(m, waist, cuisse, greave + poleyn + [wing_out] + list(knee_extra), shoe + toe + [sole] + list(foot_extra))


def plate_torso(m, h, arms_rank, arms_width, arms_pos, crown=False, wings=False):
    chest = octa(m, "Cuirasse", (0, 0, 0), (32, 24.5, 25), h.chest, 4.25, corners=h.fluted)
    chest += octa(m, "Gorgerin", (0, 13.4, -0.5), (21.5, 3, 21), h.lames2, 4.5)
    chest += octa(m, "Gorgerin_Haut", (0, 15.75, -0.5), (19, 2, 18.5), h.trim, 4.5)
    chest.append(arms_quad(m, "Blason", arms_pos, arms_width, arms_rank, crown, wings))
    belly = octa(m, "Pansiere", (0, 1, 0), (29.5, 16, 23.5), h.lames, 4.5)
    return chest, belly


def fauld(m, h):
    """Braconniere : lames sous la ceinture, posees sur la maille."""
    nodes = octa(m, "Hanches", (0, -0.5, 0), (29, 13, 22), mail(h.light, h.dark), 3.25)
    nodes += octa(m, "Braconniere_0", (0, 3.25, 0), (30.5, 4.5, 24), h.trim, 4.25, bottom=False)
    nodes += octa(m, "Braconniere_1", (0, -0.75, 0), (31.5, 3.5, 25), h.trim, 4.5, bottom=False)
    return nodes


def tassets(m, h, paint=None):
    return [box(m, "R-Tassette_Haut", (-1, 2.75, 0.5), (15, 7, 18), paint or h.trim), box(m, "R-Tassette_Bas", (-1, -2.25, 0.5), (14, 5.5, 17), h.lames2)]


# ---------------------------------------------------------------------------------------------------------------------
# Capitaine : acier poli et or, panache et manteau
# ---------------------------------------------------------------------------------------------------------------------
CAPT = Harness(STEEL, STEEL_DARK, STEEL_LIGHT, GOLD_MID, (34, 36, 48))


def comb_image(length=34.0, height=8.0):
    """Crete de bourguignotte, decoupee en alpha : profil courbe, rinceau grave."""
    W, H = int(length * D), int(height * D)
    img = canvas(W, H, GOLD_MID)
    rinceau(img, H * 0.45, H, (34, 36, 48))
    for x in range(W):
        u = (x + 0.5) / W
        top = H * (1 - math.sin(u * math.pi) ** 0.55)
        for y in range(H):
            if y < top:
                img[y][x][3] = 0
            elif y < top + 3:
                img[y][x][:3] = list(GOLD_BRIGHT)
    return finish(img, "front", edges=False)


def build_capitaine_head():
    m = new_model("Head")
    h = CAPT
    kids = dome_stack(m, h.spine, band=h.brow)
    kids += back_shell(m, h.plain, -4.5, 18, cut=2, x=16, z=15.25)
    kids.append(plank(m, "Visiere", (0, 3.5, 20.5), (0, 6.5, 16.25), 25, 1, h.trim))
    kids.append(box(m, "Visiere_Ourlet", (0, 3.5, 20.75), (25.5, 0.75, 1), h.gold))
    kids.append(quad(m, "Crete", (0, 22, 0), comb_image(28, 6), yaw(90)))
    cheek = box(m, "R-Joue", (-16.25, -3.5, 7.75), (1.25, 19, 12.5), h.trim)
    cheek2 = box(m, "R-Joue_Pointe", (-15.75, -13.25, 11), (1.25, 2.5, 6), h.gold, q=q_axis((1, 0, 0), 14))
    kids += sym(m, cheek, cheek2, box(m, "R-Charniere", (-17.1, 4, 9), (0.75, 2, 2), h.gold))
    kids.append(plank(m, "Couvre_Nuque_0", (0, -13, -19), (0, -3, -16.75), 25, 1, h.lames2))
    kids.append(box(m, "Mentonniere", (0, -14.25, 15), (25, 1.75, 1.25), fine_leather(LEATHER_DARK, GOLD_MID)))
    kids.append(arms_quad(m, "Ecusson", (0, 11.4, 16.9), 7, "capitaine"))
    plume = plume_image()
    for i, (x, turn) in enumerate(((0.0, -90), (-1.75, -97), (1.75, -83))):
        kids.append(quad(m, "Panache_%d" % i, (x, 29.5, -8), plume, yaw(turn)))
    kids.append(box(m, "Plumail", (0, 21.5, 8.5), (3.25, 4, 3.25), h.gold))
    return m, [piece(m, "Head", (0, 0, 0), kids)]


def capitaine_cape():
    return cape_image(150, 300, 19, "capitaine", 80)


def build_capitaine_chest():
    m = new_model("Chest")
    h = CAPT
    shoulder = pauldron(m, h, h.spine)
    arm = plate_arm(m, h, shoulder)
    thigh = thigh_piece(m, tassets(m, h))
    pelvis = fauld(m, h)
    pelvis.append(quad(m, "Tabard", (0, -15.5, 13.25), banner(14, 30)))
    chest, belly = plate_torso(m, h, "capitaine", 16, (0, 0.5, 12.8))
    belly += octa(m, "Ceinture", (0, -6.25, 0), (31, 3.5, 25.25), gold_plate(), 4.75)
    belly.append(box(m, "Boucle", (0, -6.25, 13), (7, 5, 1), h.gold))
    belly.append(box(m, "Boucle_Gemme", (0, -6.25, 13.6), (2.5, 2.5, 0.5), gem()))
    brooch = box(m, "R-Fermail", (-11.5, 9.5, 13.1), (2, 2, 0.5), gem())
    clasp = box(m, "R-Fermail_Or", (-11.5, 9.5, 12.75), (3.5, 3.5, 0.75), h.gold)
    chain = [rod(m, "Chaine_%d" % i, (-11.5 + i * 5.75, 9.5 - [0, 1.2, 1.2, 0][i], 12.95), (-11.5 + (i + 1) * 5.75, 9.5 - [1.2, 1.2, 0, 0][i], 12.95), 0.6, h.gold) for i in range(3)]
    strap = box(m, "R-Attache", (-11.5, 12.75, -3), (4, 1, 28), fine_leather(VELVET, GOLD_MID), hidden=("bottom",))
    chest += sym(m, brooch, clasp, strap) + chain
    chest += cape_quads(m, capitaine_cape(), [((0, 15, -13.6), (0, -16, -16.5)), ((0, -16, -16.5), (0, -44, -21)), ((0, -44, -21), (0, -60, -25))], 31, 39)
    return m, body_chain(m, pelvis, belly, chest, arm, thigh)


def build_capitaine_hands():
    m = new_model("Hands")
    return m, plate_gauntlet(m, CAPT)


def build_capitaine_legs():
    m = new_model("Legs")
    return plate_legs(m, CAPT)


# ---------------------------------------------------------------------------------------------------------------------
# Patriarche : le Marquis tueur de dragons
#   acier noir mat forge, gravures d'or (dragons heraldiques, frises de runes), grandes epaulieres incrustees d'or pur,
#   cotte de mailles sombre renforcee au torse et aux cuisses, lourde cape de velours noir bordee de fourrure sombre et
#   brodee d'or aux armes de la maison, medaillon d'or a la tete de dragon arrachee qui ferme le col, longs gantelets de
#   cuir noir renforces d'acier noir et garnis d'or mat, bottes de cuir sombre ajustees pour la montagne.
# ---------------------------------------------------------------------------------------------------------------------
PATR = Harness(NIGHT, NIGHT_DARK, NIGHT_LIGHT, GOLD_MID, (14, 14, 18), sheen=0.17, grain=0.13)
P_MAIL = mail((112, 114, 124), (24, 24, 30), bars=26)
P_LEATHER = fine_leather(BLACK_LEATHER, (120, 94, 52), dark=BLACK_LEATHER_DARK, light=BLACK_LEATHER_LIGHT)
P_LEATHER_PLAIN = fine_leather(BLACK_LEATHER, None, dark=BLACK_LEATHER_DARK, light=BLACK_LEATHER_LIGHT)
P_MATTE_GOLD = steel(GOLD_MATTE, GOLD_MATTE_DARK, GOLD_MATTE_LIGHT, sheen=0.2, grain=0.08)
P_PURE_GOLD = steel(GOLD_MID, GOLD_SHADE, GOLD_BRIGHT, gold=GOLD_BRIGHT, bands=((0.25, 9),), ground=(60, 40, 18))
P_FUR = fur()
_engr = {}


def dragon_engraving(width_px, wings=True):
    """Dragon heraldique (tete arrachee, epee, ailes) grave a l'or, sans ecu."""
    key = (width_px, wings)
    if key not in _engr:
        art = blason.arms_sized("patriarche", width_px, wings=wings, px=int(width_px * (0.55 if wings else 1.4)), shield=False)
        _engr[key] = engraved(art)
    return _engr[key]


def breast_emblem(img, w, h):
    """Tete de dragon et epee, gravees en grand sous le medaillon."""
    art = dragon_engraving(int(h * 0.6 * 0.77), wings=False)
    stamp(img, art, (w - len(art[0])) // 2, int(h * 0.24))


def pauldron_emblem(img, w, h):
    art = dragon_engraving(int(min(w * 0.8, h * 1.0 / 0.62)), wings=False)
    stamp(img, art, (w - len(art[0])) // 2, max(0, (h - len(art)) // 2))


def visor_paint(face, w, h, seed):
    base = PATR.plain(face, w, h, seed)
    if face == "front":
        rect(base, 4, h * 0.3, w - 2, h * 0.3 + 3, (6, 6, 8))
        rect(base, 4, h * 0.3 + 3, w - 2, h * 0.3 + 4, GOLD_MATTE)
        for row in range(2):
            for x in range(14, w - 8, 9):
                disc(base, x + (row % 2) * 4, h * 0.66 + row * 6, 1.0, (6, 6, 8))
    return base


def build_patriarche_head():
    m = new_model("Head")
    h = PATR
    kids = dome_stack(m, steel(NIGHT, NIGHT_DARK, NIGHT_LIGHT, gold=GOLD_MID, ground=(14, 14, 18), sheen=0.17, grain=0.13, runes=((0.55, 10),)), band=None)
    kids += back_shell(m, h.plain, -3, 15, cut=2, x=16, z=15.25)
    wing = box(m, "R-Mezail", (-7.75, -0.25, 17), (16.75, 11, 1), visor_paint, q=yaw(-14), hidden=("back",))
    kids += sym(m, wing)
    kids.append(box(m, "Mezail_Arete", (0, -0.25, 18.75), (1.25, 11, 1), P_MATTE_GOLD))
    kids += octa(m, "Bavier", (0, -9.75, 0.25), (31.5, 10, 31.5), h.lames2, 1.5, top=False)
    kids += sym(m, box(m, "R-Joue", (-16.25, 0, 9.25), (1.25, 18, 13.5), h.plain))
    kids += octa(m, "Couronne", (0, 10.5, 0), (35, 3.5, 34), P_PURE_GOLD, 3.5, top=False, bottom=False)
    big, small = fleuron(), fleuron(7.5, 5.5, big=False)
    ring = [(0, 17.75, 0.0, big), (11, 17.75, 0.0, small), (-11, 17.75, 0.0, small), (0, -17.75, 180.0, big), (17.75, 0, 90.0, big), (-17.75, 0, -90.0, big),
            (17.75, 9, 90.0, small), (-17.75, 9, -90.0, small), (17.75, -9, 90.0, small), (-17.75, -9, -90.0, small)]
    for i, (x, z, turn, point) in enumerate(ring):
        hgt = (len(point) / float(D)) / 2.0 + 11.75
        kids.append(quad(m, "Fleuron_%d" % i, (x, hgt, z * 0.98 if abs(z) > abs(x) else z), point, yaw(turn)))
    kids += octa(m, "Bonnet", (0, 20.5, 0), (16, 2, 15.5), velvet(VELVET_BLACK, VELVET_BLACK_LIGHT, VELVET_BLACK_DARK), 4.5, bottom=False)
    pts = [(-15, 8.5, -6), (-20, 10.5, -13), (-22.75, 13.5, -21), (-22.75, 18, -28.5), (-20.5, 23.5, -34), (-17.5, 29, -37.5)]
    n = len(pts) - 1
    right = [rod(m, "R-Corne_%d" % i, pts[i], pts[i + 1], 4.5 * (1 - 0.8 * i / float(n)) + 0.5, ivory) for i in range(n)]
    right.append(box(m, "R-Bague_Corne", (-16.5, 9, -8), (5.5, 1.5, 5.5), P_MATTE_GOLD, q=q_from_y((-0.66, 0.26, -0.7))))
    kids += sym(m, *right)
    kids += sym(m, box(m, "R-Fente_Braise", (-8.5, 1.5, 17.75), (8, 0.5, 0.5), glow(EMBER), q=yaw(-14), shading="fullbright"))
    return m, [piece(m, "Head", (0, 0, 0), kids)]


def medallion_image(size_u=10.0):
    """Medaillon d'or : grenetis, champ martele, tete de dragon arrachee en relief."""
    px = 96
    cv = blason.Canvas(px, -1.5, 1.5, -1.5, 1.5)
    ring = lambda r, n=48: [(r * math.cos(k * 2 * math.pi / n), r * math.sin(k * 2 * math.pi / n)) for k in range(n)]
    cv.fill(ring(1.48), (70, 46, 18))
    cv.fill(ring(1.42), (236, 186, 84))
    cv.fill(ring(1.24), (150, 102, 38))
    cv.fill(ring(1.18), (196, 146, 60))
    for k in range(28):
        a = k * 2 * math.pi / 28
        cv.dot(1.33 * math.cos(a), 1.33 * math.sin(a), 0.055, (255, 232, 156))
    blason.dragon_head(cv, (232, 182, 78), (255, 236, 168), (140, 92, 34), (88, 56, 20), (214, 34, 44), (255, 240, 200))
    art = blason.shrink(cv.img, int(size_u * D), int(size_u * D))
    return harden(art)


def patriarche_cape():
    """Velours noir lourd, bordure de fourrure sombre, armoiries et rinceaux brodes d'or."""
    w, h = 216, 380
    img = velvet(VELVET_BLACK, VELVET_BLACK_LIGHT, VELVET_BLACK_DARK)("front", w, h, 23)
    side = 24
    pelt = P_FUR("front", side, h, 5)
    for y in range(h):
        for x in range(side):
            img[y][x][:3] = list(pelt[y][x][:3])
            img[y][w - 1 - x][:3] = list(pelt[y][side - 1 - x][:3])
    for x0 in (side, w - side - 3):
        rect(img, x0, 0, x0 + 3, h, GOLD_MID)
    rinceau(img, 4, 20, VELVET_BLACK_DARK)
    for x in range(side + 6, w - side - 6):                                   # double filet brode le long de la fourrure
        for y in (h - 64, h - 60):
            put(img, x, y, GOLD_SHADE)
    for y in range(24, h - 64):
        put(img, side + 7, y, GOLD_SHADE)
        put(img, w - side - 8, y, GOLD_SHADE)
    rune_band(img, h - 58, h - 44, VELVET_BLACK_DARK, GOLD_MID)
    art = embroider(blason.arms_sized("brode", 168, crown=True, wings=True, px=76))
    stamp(img, art, (w - len(art[0])) // 2, 34)
    hem = P_FUR("front", w, 40, 9)                                             # ourlet de fourrure, bas effiloche
    for y in range(40):
        for x in range(w):
            img[h - 40 + y][x][:3] = list(hem[y][x][:3])
    for x in range(w):
        cut = int(9 * noise(x * 0.45, 2.3))
        for y in range(h - cut, h):
            img[y][x][3] = 0
    return img


def grand_pauldron(m, h):
    """Epauliere imposante : coupe gravee de runes, lames, incrustations d'or pur, haute-piece au col."""
    coupe = steel(NIGHT, NIGHT_DARK, NIGHT_LIGHT, gold=GOLD_MID, ground=(14, 14, 18), sheen=0.17, grain=0.13, edge="both", runes=((0.3, 11),))
    out = []
    out += octa(m, "R-Epaule_Haut", (-2.75, 15, 0), (16, 3, 19), steel(NIGHT, NIGHT_DARK, NIGHT_LIGHT, sheen=0.17, grain=0.13, emblem=pauldron_emblem, emblem_faces=("top",)), 4.5, bottom=False)
    out += octa(m, "R-Epaule_Coupe", (-2.5, 10.75, 0), (21, 6, 25), coupe, 5.5, bottom=False)
    out += octa(m, "R-Epaule_Lame1", (-3.5, 6.25, 0), (20, 3.5, 24), h.plain, 5.5, bottom=False)
    out += octa(m, "R-Epaule_Lame2", (-4, 3, 0), (18.75, 3.5, 22.75), h.plain, 5.5, bottom=False)
    out += octa(m, "R-Epaule_Lame3", (-4.25, -0.25, 0), (17.5, 3.25, 21.5), h.trim, 5.5)
    out.append(box(m, "R-Haute_Piece", (6.25, 15.5, 0), (1.5, 7, 16), steel(NIGHT, NIGHT_DARK, NIGHT_LIGHT, gold=GOLD_MID, sheen=0.17, grain=0.13, edge="top")))
    for z in (-7.5, 7.5):                                                       # incrustations d'or pur sur la coupe
        out.append(box(m, "R-Incrustation_%s" % ("Av" if z > 0 else "Ar"), (-4.5, 10.75, z * 1.67), (9, 4, 0.75), P_PURE_GOLD))
    out.append(box(m, "R-Incrustation_Ext", (-13.25, 10.75, 0), (0.75, 4, 9), P_PURE_GOLD))
    out.append(box(m, "R-Rondache", (-13.4, 6.25, 0), (0.75, 3, 3), gem()))
    return out


def build_patriarche_chest():
    m = new_model("Chest")
    h = PATR
    arm = plate_arm(m, h, grand_pauldron(m, h))
    skirt = box(m, "R-Pan", (-1.25, 0.25, 0.5), (13.75, 16.5, 16.75), P_MAIL, hidden=("top",))
    thigh = thigh_piece(m, [skirt] + tassets(m, h))
    pelvis = octa(m, "Hanches", (0, -0.5, 0), (29, 13, 22), P_MAIL, 3.25)
    pelvis += octa(m, "Braconniere_0", (0, 3.25, 0), (30.5, 4.5, 24), h.trim, 4.25, bottom=False)
    pelvis += octa(m, "Braconniere_1", (0, -0.75, 0), (31.5, 3.5, 25), h.lames2, 4.5, bottom=False)
    tabard_art = embroider(blason.arms_sized("brode", 44, px=40))
    pelvis.append(quad(m, "Tabard", (0, -17, 13.25), banner(13, 32, ground=VELVET_BLACK, light=VELVET_BLACK_LIGHT, dark=VELVET_BLACK_DARK, art=tabard_art)))
    breast = steel(NIGHT, NIGHT_DARK, NIGHT_LIGHT, gold=GOLD_MID, ground=(14, 14, 18), sheen=0.17, grain=0.13, edge="bottom", runes=((0.03, 12), (0.86, 12)), emblem=breast_emblem)
    chest = octa(m, "Cuirasse", (0, 0, 0), (32, 24.5, 25), breast, 4.25, corners=h.plain)
    chest += octa(m, "Gorgerin", (0, 13.4, -0.5), (21.5, 3, 21), h.lames2, 4.5)
    belly = octa(m, "Pansiere", (0, 1, 0), (29.5, 16, 23.5), steel(NIGHT, NIGHT_DARK, NIGHT_LIGHT, gold=GOLD_MID, sheen=0.17, grain=0.13, lames=3), 4.5)
    belly += octa(m, "Ceinture", (0, -6.25, 0), (31, 3.5, 25.25), P_LEATHER, 4.75)
    belly.append(box(m, "Boucle", (0, -6.25, 12.9), (6.5, 5, 1), P_MATTE_GOLD))
    chest += octa(m, "Col_Fourrure", (0, 16, -1.5), (34, 7, 27.5), P_FUR, 7.5, bottom=False)
    chest += sym(m, box(m, "R-Revers_Fourrure", (-11, 7, 12.85), (6, 17, 1.25), fur(shaggy=6), hidden=("back",)))
    chest.append(quad(m, "Medaillon", (0, 7.5, 14.1), medallion_image()))
    chest += sym(m, rod(m, "R-Agrafe", (-8, 8.5, 13.6), (-4.75, 7.75, 13.95), 0.75, P_MATTE_GOLD))
    chest += cape_quads(m, patriarche_cape(), [((0, 17, -15.6), (0, -14, -18.5)), ((0, -14, -18.5), (0, -44, -23.5)), ((0, -44, -23.5), (0, -70, -30))], 38, 52)
    return m, body_chain(m, pelvis, belly, chest, arm, thigh)


def build_patriarche_hands():
    """Longs gantelets de cuir noir, attelles et plaques d'acier noir, garnitures d'or mat."""
    m = new_model("Hands")
    h = PATR
    cuff = octa(m, "R-Gantelet", (0, -0.5, 0), (11.75, 15, 15.25), P_LEATHER_PLAIN, 2.25)
    cuff += octa(m, "R-Revers", (0, 6.5, 0), (12.75, 2, 16.25), P_MATTE_GOLD, 2.5)
    cuff += octa(m, "R-Poignet", (0, -7.125, 0), (12.5, 1.5, 16), P_MATTE_GOLD, 2.5)
    cuff.append(box(m, "R-Attelle_Ext", (-6.25, -0.5, 0), (0.75, 12, 6), h.trim))
    cuff.append(box(m, "R-Attelle_Dos", (0, -0.5, 7.9), (5, 12, 0.75), h.trim))
    glove = octa(m, "R-Gant", (0, -0.125, 0), (11, 12.75, 15), P_LEATHER_PLAIN, 0.75)
    plate_ = box(m, "R-Plaque_Main", (-5.85, 1.5, 0), (0.75, 6, 11.5), steel(NIGHT, NIGHT_DARK, NIGHT_LIGHT, gold=GOLD_MATTE, sheen=0.17, grain=0.13, edge="both"))
    fingers = box(m, "R-Phalanges", (-5.9, -3.5, 0), (0.75, 3.5, 11), steel(NIGHT, NIGHT_DARK, NIGHT_LIGHT, gold=GOLD_MATTE, sheen=0.17, grain=0.13, lames=3, edge="bottom"))
    return m, hands_chain(m, cuff, glove + [plate_, fingers])


def build_patriarche_legs():
    """Mailles sombres aux cuisses, genouilleres d'acier noir, bottes de cuir ajustees a semelle crantee."""
    m = new_model("Legs")
    h = PATR
    waist = octa(m, "Taille", (0, 0, 0), (27, 10, 20.5), P_MAIL, 1.25)
    thigh_mail = octa(m, "R-Cuisse_Mailles", (0, -0.5, 0), (12.25, 21, 14), P_MAIL, 1.75)
    cuisse = box(m, "R-Cuissard", (0, -1, 7.4), (10.5, 15, 1), steel(NIGHT, NIGHT_DARK, NIGHT_LIGHT, gold=GOLD_MATTE, sheen=0.17, grain=0.13, edge="bottom", spine=True, ground=(14, 14, 18)))
    poleyn = octa(m, "R-Genouillere", (0, 11.5, 2.5), (12.75, 8, 10.5), steel(NIGHT, NIGHT_DARK, NIGHT_LIGHT, gold=GOLD_MATTE, sheen=0.17, grain=0.13, edge="both"), 2)
    wing = box(m, "R-Aile_Genou", (-6.9, 11.5, 0), (1, 6, 7), h.lames2)
    boot = octa(m, "R-Botte", (0, -0.75, 0), (12.25, 21.5, 14.75), P_LEATHER_PLAIN, 1.75, top=False)
    cuff = octa(m, "R-Botte_Haut", (0, 10.5, 0), (13, 2, 15.75), P_LEATHER, 2)
    straps = []
    for i, y in enumerate((2, -3.5, -8.25)):
        straps += octa(m, "R-Sangle_%d" % i, (0, y, 0), (12.75, 1, 15.25), fine_leather((58, 48, 40), None, dark=BLACK_LEATHER_DARK, light=(110, 96, 80)), 2)
        straps.append(box(m, "R-Boucle_%d" % i, (-6.6, y, 3.5), (0.75, 1.75, 2.25), P_MATTE_GOLD))
    shoe = octa(m, "R-Soulier", (-0.375, 0.75, 0), (15.25, 8, 21.25), P_LEATHER_PLAIN, 0.75)
    toe = octa(m, "R-Bout", (-0.5, 0, 10.25), (12, 5.5, 3.25), h.trim, 1)
    sole = box(m, "R-Semelle", (-0.5, -3.5, 0), (15.75, 1.5, 22), tread)
    heel = box(m, "R-Talon", (-0.5, -2.25, -7.25), (14.75, 1.25, 6), P_LEATHER_PLAIN)
    return m, legs_chain(m, waist, thigh_mail + [cuisse], poleyn + [wing] + boot + cuff + straps, shoe + toe + [sole, heel])


def tread(face, w, h, seed):
    """Semelle crantee : crampons pour la roche."""
    img = canvas(w, h, (26, 22, 20))
    for y in range(h):
        for x in range(w):
            if face == "bottom":
                on = (y // 5) % 2 == 0 and (x + (y // 5) * 3) % 8 < 5
                img[y][x][:3] = [46, 40, 36] if on else [12, 10, 10]
            elif (x // 3) % 2 == 0:
                img[y][x][:3] = [38, 32, 30]
    return finish(img, face, edges=False)


# ---------------------------------------------------------------------------------------------------------------------
# Tenues
# ---------------------------------------------------------------------------------------------------------------------
HELM = {"Head": HIDE_HELMET}

SETS = {
    "Recrue": {
        "prefix": "Armor_Runkandel_Recrue_", "quality": "Rare", "level": 60, "durability": 120, "sound": "ISS_Armor_Leather", "fire": 0.5, "hide": HELM,
        "build": {"Head": build_recrue_head, "Chest": build_recrue_chest, "Hands": build_recrue_hands, "Legs": build_recrue_legs},
        "pieces": {
            "Head": ("Bassinet de Recrue", "Tenue de Recrue Runkandel. Un bassinet poli frappé de l'écu familial, sur une coiffe de mailles.", 8, 2, 0.05),
            "Chest": ("Brigandine de Recrue", "Tenue de Recrue Runkandel. Du velours cramoisi riveté de laiton, sur un hauberk de mailles : l'habit des écuyers de la maison.", 14, 3, 0.09),
            "Hands": ("Gants de Recrue", "Tenue de Recrue Runkandel. Des gants à crispin de cuir gaufré, surpiqués d'or.", 6, 1, 0.04),
            "Legs": ("Chausses de Recrue", "Tenue de Recrue Runkandel. Des chausses de laine fine, des genouillères d'acier et de hautes bottes cirées.", 10, 2, 0.07),
        },
    },
    "Capitaine": {
        "prefix": "Armor_Runkandel_Capitaine_", "quality": "Epic", "level": 250, "durability": 260, "sound": "ISS_Armor_Heavy", "fire": 0.5, "hide": HELM,
        "build": {"Head": build_capitaine_head, "Chest": build_capitaine_chest, "Hands": build_capitaine_hands, "Legs": build_capitaine_legs},
        "pieces": {
            "Head": ("Bourguignotte de Capitaine", "Harnois de Capitaine Runkandel. Une bourguignotte gravée d'or au panache écarlate, que les soldats suivent au cœur des flammes.", 15, 5, 0.084),
            "Chest": ("Cuirasse de Capitaine", "Harnois de Capitaine Runkandel. Une armure de parade cannelée et un manteau de velours damassé aux armes de la maison.", 28, 9, 0.151),
            "Hands": ("Gantelets de Capitaine", "Harnois de Capitaine Runkandel. Des gantelets articulés, cannelés et rehaussés d'or.", 12, 4, 0.067),
            "Legs": ("Jambières de Capitaine", "Harnois de Capitaine Runkandel. Des jambières cannelées, aux genouillères gravées de rinceaux.", 20, 6, 0.118),
        },
    },
    "Patriarche": {
        "prefix": "Armor_Runkandel_Patriarche_", "quality": "Legendary", "level": 550, "durability": 500, "sound": "ISS_Armor_Heavy", "fire": 0.5, "hide": HELM,
        "build": {"Head": build_patriarche_head, "Chest": build_patriarche_chest, "Hands": build_patriarche_hands, "Legs": build_patriarche_legs},
        "pieces": {
            "Head": ("Armet Couronné de Vorn", "Harnois de Vorn. Un armet d'acier noir mat ceint de la couronne de marquis, dressé des cornes d'ivoire du premier dragon tombé sous une lame Runkandel.", 30, 12, 0.116),
            "Chest": ("Harnois de Vorn", "Harnois de Vorn. Acier noir mat gravé de dragons et de runes d'or, cotte de mailles sombre et lourde cape de velours noir bordée de fourrure, brodée d'or aux armes des tueurs de dragons.", 55, 20, 0.21),
            "Hands": ("Gantelets de Vorn", "Harnois de Vorn. De longs gantelets de cuir noir, renforcés d'acier noir et garnis d'or mat.", 22, 8, 0.093),
            "Legs": ("Bottes de Vorn", "Harnois de Vorn. Des cuissards de mailles sombres, des genouillères d'acier noir et des bottes de cuir ajustées, crantées pour les sentiers de montagne.", 40, 14, 0.161),
        },
    },
}

if __name__ == "__main__":                                                   # importable pour inspection
    reports = write_sets(RES, PREVIEW, MODEL_DIR, "Runkandel", SETS, "runkandel", family_tag="Plate_Runkandel", skip_in_icon=("Cape",), preview_fit=5.2, closeup_fit=9.5)
    for set_name, nodes in reports.items():
        found = coplanar_report(nodes)
        print("%s : %d paires coplanaires" % (set_name, len(found)))
        for line in found[:40]:
            print("   ", line)
