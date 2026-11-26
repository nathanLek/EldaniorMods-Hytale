"""Armures divines des classes : quatre tenues de 4 pieces, qualite Divin, niveau 800.

  Guerrier  Harnois du Seraphin     paladin en harnois d'argent poli et d'or, frises sur email lapis, grand heaume a cimier
                                    d'or et ailettes, aureole solaire, grandes ailes de plumes repliees en deux pans
                                    (arc osseux, couvertures, remiges), soleil d'or au plastron, tabard blanc
  Mage      Atours de l'Astre       archimage : grand chapeau pointu etoile au bout replie, anneau d'orbite et cristaux en
                                    suspens, haut col evase, robe de nuit ouverte sur une sous-robe d'ivoire, manches en
                                    cloche, longue cape aux constellations, astrolabe, grimoire enchaine a la hanche
  Archer    Parure du Cerf Celeste  rodeur : capuche profonde aux bois de cerf d'argent, pourpoint de cuir lace, spalieres de
                                    feuilles d'argent, cape de foret au bas decoupe en feuilles, grand carquois empenne,
                                    long brassard d'arc au bras gauche, gant d'archer a droite, hautes bottes
  Assassin  Voile de la Lune Noire  shinobi et samourai en dark fantasy, tout en noir : aucun casque : une marque d'encre au front
                                    et une fumee noire en particules autour de la tete (effet Armure_Fumee_Ombre) ;
                                    kimono noir a manches larges et cols superposes, haramaki et sode lamellaires laques laces
                                    de rouge sang, kusazuri, obi au tanto, ninjato dans le dos, kote, hakama plisse, tabi

Le divin se lit dans les pieces lumineuses (fullbright), sans les multiplier : aureole, gemmes, cristaux, fils de lune.
Reprend les formes du harnois Runkandel (gen_runkandel.py, importe sans rien ecrire) a 4 px par unite.

Usage : python3 scripts/armures/gen_divines_classes.py <src/main/resources> [dossier_apercu] [Guerrier|Mage|Archer|Assassin ...]
"""
import math
import os
import random
import sys

import gen_runkandel as rk
from armor_kit import (ID_Q, box, canvas, disc, finish, mix, mirror, noise, put, q_axis, q_from_y, qmul, qrot, quad, rect, rod,
                       stroke, sym, HIDE_HELMET, coplanar_report, write_sets)
import blason
import familles
from blason import curve, fill as fill_poly, layer

D = rk.D
RES, PREVIEW = rk.RES, rk.PREVIEW
MODEL_DIR = "Items/Armors/Divines"
GOLD_MID, GOLD_SHADE, GOLD_BRIGHT = rk.GOLD_MID, rk.GOLD_SHADE, rk.GOLD_BRIGHT
DIVINE = (7, 216, 251)                     # couleur de la qualite Divin
DIVINE_LIGHT = (196, 248, 255)
KEEP = []                                  # le cache des peintures est indexe par id() : on garde chaque peinture en vie


def keep(paint):
    KEEP.append(paint)
    return paint


# ---------------------------------------------------------------------------------------------------------------------
# Outils communs
# ---------------------------------------------------------------------------------------------------------------------
def lit(node):
    """Piece lumineuse : ignore l'eclairage du monde."""
    node["shape"]["shadingMode"] = "fullbright"
    return node


def lit_box(m, name, pos, size, color, q=ID_Q, hidden=("back",)):
    return box(m, name, pos, size, rk.glow(color), q=q, shading="fullbright", hidden=hidden)


def crystal(c=DIVINE, light=DIVINE_LIGHT):
    """Cristal : degrade vertical, facettes."""
    def paint(face, w, h, seed):
        img = canvas(w, h, c)
        for y in range(h):
            for x in range(w):
                t = (y + 0.5) / h
                facet = abs((x + 0.5) / w - 0.5) * 2
                img[y][x][:3] = list(mix(light, c, t * 0.9 + facet * 0.3))
        return img
    return paint


def open_bottom(paint):
    """Face du dessous transparente : manche en cloche, la main sort par l'ouverture."""
    def wrapped(face, w, h, seed):
        if face == "bottom":
            return [[[0, 0, 0, 0] for _ in range(w)] for _ in range(h)]
        return paint(face, w, h, seed)
    return wrapped


def shaded(paint, k):
    """Meme peinture, assombrie : bord interieur d'une capuche, doublure."""
    def wrapped(face, w, h, seed):
        img = paint(face, w, h, seed)
        for row in img:
            for p in row:
                p[0], p[1], p[2] = p[0] * k, p[1] * k, p[2] * k
        return img
    return wrapped


def starfield(ground, dark, light, star=(255, 248, 226), glow=DIVINE, hem=None, lines=True, density=230):
    """Etoffe de nuit semee d'etoiles ; quelques constellations reliees d'un fil de lumiere."""
    base = rk.velvet(ground, light, dark, motif=False)

    def paint(face, w, h, seed):
        img = base(face, w, h, seed)
        sow_stars(img, seed, ground, star, glow, lines and face not in ("top", "bottom"), density)
        if hem and face not in ("top", "bottom"):
            rect(img, 0, h - 5, w, h, hem)
            rect(img, 0, h - 6, w, h - 5, GOLD_SHADE)
        return img
    return paint


def sow_stars(img, seed, ground, star=(255, 248, 226), glow=DIVINE, lines=True, density=230):
    h, w = len(img), len(img[0])
    rng = random.Random(seed * 7919 + w * 31 + h)
    pts = []
    for _ in range(max(1, w * h // density)):
        x, y, b = rng.random() * w, rng.random() * h, rng.random()
        if b > 0.86:
            for dx, dy in ((0, 0), (1, 0), (-1, 0), (0, 1), (0, -1)):
                put(img, x + dx, y + dy, star if (dx, dy) == (0, 0) else mix(star, ground, 0.35))
            pts.append((x, y))
        else:
            put(img, x, y, mix(ground, star, 0.35 + 0.55 * b))
    if lines:
        for (x0, y0), (x1, y1) in zip(pts, pts[1:]):
            if math.hypot(x1 - x0, y1 - y0) < max(18, w * 0.35) and rng.random() < 0.6:
                n = int(math.hypot(x1 - x0, y1 - y0))
                for i in range(2, n - 1):
                    if i % 3:
                        put(img, x0 + (x1 - x0) * i / n, y0 + (y1 - y0) * i / n, glow, 0.45)


def panel_image(w_u, h_u, paint, seed=5, tip=0.0, torn=0, split=False, hem=None, art=None, art_y=0.1, leaves=0):
    """Pan d'etoffe : pointe (tip), bas dechire (torn, px), fendu (split), galon (hem), motif (art), bas en feuilles (leaves, px)."""
    w, h = int(w_u * D), int(h_u * D)
    img = paint("front", w, h, seed)
    if art:
        rk.stamp(img, art, (w - len(art[0])) // 2, int(h * art_y))
    for x in range(w):
        u = abs((x + 0.5) / w * 2 - 1)
        bottom = h - 1 - int(tip * h * u)
        if torn:
            bottom -= int(torn * (0.2 + 0.8 * noise(x * 0.35 + seed, seed * 0.7)))
        if leaves:
            period = leaves * 1.6
            f = (x % period) / period
            bottom -= int(leaves * (1 - math.sin(math.pi * f) ** 0.6))
        for y in range(h):
            if y > bottom or (split and abs(x + 0.5 - w / 2.0) < w * 0.05 and y > h * 0.35):
                img[y][x][3] = 0
            elif hem and y > bottom - 5:
                img[y][x][:3] = list(hem)
    return img


def hang(m, name, top, bottom, img):
    """Pan pendant entre deux points (meme x), comme un pan de cape."""
    dy, dz = top[1] - bottom[1], top[2] - bottom[2]
    return quad(m, name, tuple((top[k] + bottom[k]) / 2.0 for k in range(3)), img, q_axis((1, 0, 0), math.degrees(math.atan2(dz, dy))))


def hood(m, paint, rim, peak_paint, y=-3.0, h=22.0, x=16.25, z=15.5, front=15.0, cut=2.0):
    """Capuche profonde : timbre de tissu, flancs, bord interieur dans l'ombre, visiere d'etoffe qui avance sur le front."""
    nodes = rk.dome_stack(m, paint)
    nodes.append(box(m, "Nuque", (0, y, -z), (2 * x - 2 * cut, h, 1.5), paint, hidden=("bottom", "front")))
    z0 = -z + cut
    side = box(m, "R-Flanc", (-x, y, (z0 + front) / 2.0), (1.5, h, front - z0), paint, hidden=("bottom",))
    diag = box(m, "R-Flanc_Dos", (-x + cut / 2.0, y, -z + cut / 2.0), (cut * math.sqrt(2) + 0.5, h - 0.5, 1.25), paint, q=rk.yaw(45), hidden=("bottom", "left", "right"))
    rim_side = box(m, "R-Bord_Cote", (-15.6, y - 0.75, front + 0.6), (2.25, h - 2.5, 1.5), rim)
    nodes += sym(m, side, diag, rim_side)
    nodes.append(box(m, "Bord", (0, 6.25, 16.6), (34.5, 2.5, 1.5), rim))
    nodes.append(rk.plank(m, "Visiere_Capuche", (0, 6.5, 18.75), (0, 13.5, 15.75), 35, 1, peak_paint))
    return nodes


def pauldron(m, h, top, coupe, gem_paint, gold=None):
    """Epauliere a lames : sommet orne, coupe a frise, haute-piece, joyau."""
    out = []
    out += rk.octa(m, "R-Epaule_Haut", (-2.75, 15, 0), (16, 3, 19), top, 4.5, bottom=False)
    out += rk.octa(m, "R-Epaule_Coupe", (-2.5, 10.75, 0), (21, 6, 25), coupe, 5.5, bottom=False)
    out += rk.octa(m, "R-Epaule_Lame1", (-3.5, 6.25, 0), (20, 3.5, 24), h.plain, 5.5, bottom=False)
    out += rk.octa(m, "R-Epaule_Lame2", (-4, 3, 0), (18.75, 3.5, 22.75), h.plain, 5.5, bottom=False)
    out += rk.octa(m, "R-Epaule_Lame3", (-4.25, -0.25, 0), (17.5, 3.25, 21.5), h.trim, 5.5)
    out.append(box(m, "R-Haute_Piece", (6.25, 15.5, 0), (1.5, 7, 16), h.trim))
    out.append(box(m, "R-Rondache_Or", (-13.3, 6.25, 0), (0.75, 4, 4), gold or h.gold))
    out.append(box(m, "R-Rondache", (-13.85, 6.25, 0), (0.5, 2.5, 2.5), gem_paint, shading="fullbright", hidden=("right",)))
    return out


def body_chain2(m, pelvis_kids, belly_kids, chest_kids, arm_r, arm_l_as_right, thigh):
    """Comme rk.body_chain, mais les deux bras peuvent differer (le gauche est construit comme un droit puis reflechi)."""
    chest = rk.piece(m, "Chest", (0, 5, 0), list(chest_kids) + [arm_r, mirror(m, arm_l_as_right)], offset=(0, 11, 0))
    belly = rk.piece(m, "Belly", (0, 8, 0), list(belly_kids) + [chest], offset=(0, 4, 0))
    return [rk.piece(m, "Pelvis", (0, 12, 0), list(pelvis_kids) + [thigh, mirror(m, thigh), belly])]


def hands_chain2(m, cuff_r, hand_r, cuff_l_as_right, hand_l_as_right):
    """Comme rk.hands_chain, mais main gauche differente (construite comme une droite puis reflechie)."""
    def side(cuff, hand):
        return rk.piece(m, "R-Forearm", (-18, 40, 0), list(cuff) + [rk.piece(m, "R-Hand", (0, -8, 0), list(hand), offset=(0, -5, 0))])
    return [side(cuff_r, hand_r), mirror(m, side(cuff_l_as_right, hand_l_as_right))]


def supersampled(w, h, draw, k=2):
    big = layer(w * k, h * k)
    draw(big, w * k, h * k)
    return rk.harden(blason.shrink(big, w, h))


def spans(pts, h, w):
    """Lignes d'un polygone (pair-impair) : (y, x0, x1) en pixels."""
    y0, y1 = max(0, int(min(p[1] for p in pts))), min(h - 1, int(max(p[1] for p in pts)) + 1)
    for y in range(y0, y1 + 1):
        cross = []
        for (ax, ay), (bx, by) in zip(pts, pts[1:] + pts[:1]):
            if (ay <= y + 0.5) != (by <= y + 0.5):
                cross.append(ax + (y + 0.5 - ay) * (bx - ax) / (by - ay))
        cross.sort()
        for i in range(0, len(cross) - 1, 2):
            x0, x1 = max(0, int(math.ceil(cross[i] - 0.5))), min(w - 1, int(cross[i + 1] - 0.5))
            if x0 <= x1:
                yield y, x0, x1


def halo_image(diam_u, gold=GOLD_MID, light=GOLD_BRIGHT, rays=24, inner=0.6, ring=0.72):
    """Aureole : anneau et rayons alternes."""
    n = int(diam_u * D)

    def draw(img, W, H):
        c, R = W / 2.0, W / 2.0
        for k in range(rays):
            a = k * 2 * math.pi / rays
            L = (1.0 if k % 2 == 0 else 0.85) * R
            d = math.pi / rays * 0.55
            fill_poly(img, [(c + ring * R * math.cos(a - d), c + ring * R * math.sin(a - d)), (c + L * math.cos(a), c + L * math.sin(a)),
                            (c + ring * R * math.cos(a + d), c + ring * R * math.sin(a + d))], light)
        for y in range(H):
            for x in range(W):
                r = math.hypot(x + 0.5 - c, y + 0.5 - c) / R
                if inner <= r <= ring:
                    t = abs((r - inner) / (ring - inner) - 0.5) * 2
                    img[y][x] = list(mix(light, gold, t)) + [255]
    return supersampled(n, n, draw)


def crescent_image(diam_u, c=(206, 196, 255), light=(250, 246, 255), thin=0.36):
    """Croissant de lune, cornes vers le haut a droite."""
    n = int(diam_u * D)

    def draw(img, W, H):
        cx, R = W / 2.0, W / 2.0
        for y in range(H):
            for x in range(W):
                d1 = math.hypot(x + 0.5 - cx, y + 0.5 - cx) / R
                d2 = math.hypot(x + 0.5 - cx - R * thin, y + 0.5 - cx + R * 0.22) / (R * 0.8)
                if d1 <= 0.98 and d2 > 1.0:
                    img[y][x] = list(mix(light, c, min(1.0, (d2 - 1.0) * 3))) + [255]
    return supersampled(n, n, draw)


def fan_wing_image(w_u, h_u, white=(250, 248, 242), shade=(196, 190, 178), n=11):
    """Ailette de heaume : eventail de plumes, racine en bas a gauche."""
    W, H = int(w_u * D), int(h_u * D)

    def draw(img, Wp, Hp):
        root = (0.07 * Wp, 0.9 * Hp)
        for layer_k, (frac, spread0, spread, wid, c0) in enumerate(((1.0, 14, 74, 0.12, white), (0.55, 18, 70, 0.16, mix(white, GOLD_MID, 0.3)))):
            for k in range(n - 1, -1, -1):
                a = math.radians(spread0 + k * spread / (n - 1))
                L = frac / math.hypot(math.cos(a) / (0.9 * Wp), math.sin(a) / (0.86 * Hp))
                tip = (root[0] + math.cos(a) * L, root[1] - math.sin(a) * L)
                draw_feather(img, root, tip, L * wid / 2, c0, shade, (150, 140, 124), GOLD_MID, Wp)
    return supersampled(W, H, draw)


def feather_poly(b, t, hw, round_tip=0.3):
    dx, dy = t[0] - b[0], t[1] - b[1]
    L = math.hypot(dx, dy) or 1.0
    px_, py_ = -dy / L, dx / L
    left, right = [], []
    for i in range(17):
        s = i / 16.0
        w = hw * (0.3 + 0.7 * s / 0.25) if s < 0.25 else hw
        if s > 1 - round_tip:
            w = hw * math.sqrt(max(0.0, 1 - ((s - (1 - round_tip)) / round_tip) ** 2))
        cx, cy = b[0] + dx * s, b[1] + dy * s
        left.append((cx + px_ * w, cy + py_ * w))
        right.append((cx - px_ * w, cy - py_ * w))
    return left + right[::-1]


def draw_feather(img, b, t, hw, c0, c1, edge, rachis, Wp, shadow=None):
    """Plume : ombre portee sur les plumes deja posees, contour, degrade de la racine (c1) a la pointe (c0), rachis."""
    H, W = len(img), len(img[0])
    poly = feather_poly(b, t, hw)
    L = math.hypot(t[0] - b[0], t[1] - b[1]) or 1.0
    ux, uy = (t[0] - b[0]) / L, (t[1] - b[1]) / L
    if shadow:
        off = Wp * 0.012
        for y, x0, x1 in spans([(x + off * 0.5, y + off) for x, y in poly], H, W):
            for x in range(x0, x1 + 1):
                p = img[y][x]
                if p[3]:
                    p[0], p[1], p[2] = p[0] * shadow, p[1] * shadow, p[2] * shadow
    for ox, oy in ((1.2, 0), (-1.2, 0), (0, 1.2), (0, -1.2)):
        for y, x0, x1 in spans([(x + ox, y + oy) for x, y in poly], H, W):
            for x in range(x0, x1 + 1):
                img[y][x] = list(edge) + [255]
    for y, x0, x1 in spans(poly, H, W):
        for x in range(x0, x1 + 1):
            s = ((x + 0.5 - b[0]) * ux + (y + 0.5 - b[1]) * uy) / L
            lat = abs((x + 0.5 - b[0]) * -uy + (y + 0.5 - b[1]) * ux) / max(1.0, hw)
            k = 0.96 + 0.06 * noise(x * 0.4, y * 0.13)
            c = mix(c1, c0, min(1.0, max(0.0, s * 1.6)))
            c = mix(c, edge, 0.18 * lat ** 3)
            img[y][x] = [c[0] * k, c[1] * k, c[2] * k, 255]
    for i in range(int(L * 0.92)):
        s = i / L
        if s > 0.05:
            put(img, b[0] + ux * i, b[1] + uy * i, rachis)


def lerp2(a, b, t):
    return (a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t)


def angel_wing_image(w_u, h_u, white=(252, 250, 244), shade=(204, 198, 186), edge=(146, 136, 122), gold=GOLD_MID):
    """Aile de seraphin vue de dos, levee : arc osseux de la racine (gauche, mi-hauteur) au poignet (haut), remiges primaires
    qui tombent du poignet en eventail, secondaires sous le bras, puis trois rangs de couvertures, les petites dorees."""
    W, H = int(w_u * D), int(h_u * D)

    def draw(img, Wp, Hp):
        P = lambda u, v: (u * Wp, v * Hp)                                     # noqa: E731
        arm = curve([P(0.03, 0.5), P(0.16, 0.27), P(0.33, 0.12), P(0.56, 0.03)], 12)
        n_arm = len(arm) - 1

        def on_arm(t):
            i = min(n_arm - 1, int(t * n_arm))
            return lerp2(arm[i], arm[i + 1], t * n_arm - i)

        wrist = arm[-1]
        hand = lerp2(on_arm(0.72), wrist, 0.0)
        prim = 10
        for k in range(prim - 1, -1, -1):                                     # primaires : la plus externe forme la pointe
            s = k / float(prim - 1)
            base = lerp2(wrist, hand, s)
            base = (base[0], base[1] + Hp * 0.015)
            tip = P(0.99 - 0.27 * s ** 1.1, 0.38 + 0.58 * s ** 0.8)
            L = math.hypot(tip[0] - base[0], tip[1] - base[1])
            draw_feather(img, base, tip, L * 0.075, white, shade, edge, mix(white, gold, 0.5), Wp, shadow=0.86)
        sec = 11
        for k in range(sec):                                                  # secondaires, de l'exterieur vers la racine
            s = k / float(sec - 1)
            base = on_arm(0.70 - 0.62 * s)
            base = (base[0], base[1] + Hp * 0.02)
            tip = P(0.70 - 0.52 * s, 0.97 - 0.2 * s ** 1.5)
            L = math.hypot(tip[0] - base[0], tip[1] - base[1])
            draw_feather(img, base, tip, max(Wp * 0.03, L * 0.085), white, shade, edge, mix(white, gold, 0.5), Wp, shadow=0.86)
        rows = ((15, 0.44, 0.11, mix(white, shade, 0.15), 0.035), (18, 0.25, 0.13, mix(white, gold, 0.12), 0.025), (22, 0.12, 0.2, mix(white, gold, 0.45), 0.012))
        for count, frac, wid, col, drop in rows:                              # couvertures : grandes, moyennes, petites
            for k in range(count):
                t = 0.02 + 0.98 * k / float(count - 1)
                base = on_arm(t)
                base = (base[0], base[1] + Hp * drop)
                aim = lerp2(P(0.12, 0.95), P(0.95, 0.5), t)
                dx, dy = aim[0] - base[0], aim[1] - base[1]
                dist = math.hypot(dx, dy) or 1.0
                L = dist * frac + Wp * 0.03
                tip = (base[0] + dx / dist * L, base[1] + dy / dist * L)
                draw_feather(img, base, tip, max(Wp * 0.022, L * wid), col, mix(col, shade, 0.5), edge, gold, Wp, shadow=0.8)
        for i, p in enumerate(arm):                                           # arc osseux, liseré d'or
            r = Wp * (0.03 - 0.012 * i / float(n_arm))
            for y in range(int(p[1] - r) - 1, int(p[1] + r) + 2):
                for x in range(int(p[0] - r) - 1, int(p[0] + r) + 2):
                    if 0 <= y < Hp and 0 <= x < Wp and math.hypot(x + 0.5 - p[0], y + 0.5 - p[1]) <= r:
                        top = (y + 0.5 - p[1]) / r
                        img[y][x] = list(mix(GOLD_BRIGHT, gold, max(0.0, top))) + [255]
        for y in range(Hp):                                                   # ombre douce vers la racine et le bas
            for x in range(Wp):
                p = img[y][x]
                if p[3]:
                    d = math.hypot(x / Wp - 0.03, y / Hp - 0.5)
                    k = (0.8 + 0.2 * min(1.0, d * 2.2)) * (1.04 - 0.1 * y / Hp)
                    p[0], p[1], p[2] = min(255, p[0] * k), min(255, p[1] * k), min(255, p[2] * k)
    return supersampled(W, H, draw)


def folded_wing(m, name, img, root, root_px, split_px, yaws):
    """Aile en deux pans plats articules (bras, main) : root = racine en unites (cote gauche, +X), root_px = racine dans
    l'image, split_px = colonne de la pliure. Le pan exterieur part vers l'arriere : l'aile a du volume."""
    H, W = len(img), len(img[0])
    parts = [[row[:split_px] for row in img], [row[split_px:] for row in img]]
    dy = (H / 2.0 - root_px[1]) / D                                           # centre de l'image au-dessus de la racine
    nodes = []
    start, rel = root, -root_px[0] / D
    for i, (part, turn) in enumerate(zip(parts, yaws)):
        w_u = len(part[0]) / float(D)
        u = (math.cos(math.radians(turn)), 0.0, -math.sin(math.radians(turn)))
        c = rel + w_u / 2.0
        center = (start[0] + u[0] * c, start[1] + dy, start[2] + u[2] * c)
        nodes.append(quad(m, "%s_%d" % (name, i), center, part, rk.yaw(turn)))
        end = rel + w_u
        start = (start[0] + u[0] * end, start[1], start[2] + u[2] * end)
        rel = 0.0
    return nodes


def sun_art(width_px, gold=GOLD_MID, light=GOLD_BRIGHT, core=DIVINE):
    cv = blason.Canvas(64, -1, 1, -1, 1)
    cv.fill(familles.starry(16, 0.96, 0.58, cy=0.0), gold, (60, 40, 16), 0.03)
    cv.fill(familles.regular(32, 0.52, cy=0.0), light, (60, 40, 16), 0.03)
    cv.fill(familles.regular(32, 0.3, cy=0.0), core)
    cv.fill(familles.regular(32, 0.14, cy=0.0), DIVINE_LIGHT)
    return blason.shrink(cv.img, width_px, width_px)


def star_art(width_px, gold=GOLD_MID, light=GOLD_BRIGHT, core=DIVINE):
    """Etoile a huit branches, coeur de lumiere."""
    cv = blason.Canvas(64, -1, 1, -1, 1)
    cv.fill(familles.starry(4, 0.98, 0.22, cy=0.0), gold, (40, 30, 20), 0.03)
    pts = [(p[0] * math.cos(math.pi / 4) - p[1] * math.sin(math.pi / 4), p[0] * math.sin(math.pi / 4) + p[1] * math.cos(math.pi / 4)) for p in familles.starry(4, 0.62, 0.18, cy=0.0)]
    cv.fill(pts, light, (40, 30, 20), 0.025)
    cv.fill(familles.regular(24, 0.16, cy=0.0), core)
    return blason.shrink(cv.img, width_px, width_px)


def leaf_art(width_px, c=(222, 234, 228), vein=(120, 150, 136)):
    cv = blason.Canvas(64, -0.7, 0.7, -1, 1)
    pts = [(0.0, -0.95)] + [(0.55 * math.sin(math.pi * t) ** 0.8, -0.95 + 1.8 * t) for t in [i / 16.0 for i in range(1, 16)]] + [(0.0, 0.85)]
    pts += [(-x, y) for x, y in reversed(pts[1:-1])]
    cv.fill(pts, c, (40, 60, 50), 0.04)
    cv.line([(0, -0.8), (0, 0.95)], vein, 0.03)
    for k in range(5):
        v = -0.6 + k * 0.28
        cv.line([(0, v + 0.12), (0.32, v - 0.08)], vein, 0.02)
        cv.line([(0, v + 0.12), (-0.32, v - 0.08)], vein, 0.02)
    return blason.shrink(cv.img, width_px, int(width_px * 2 / 1.4))


def stag_art(width_px, c=(214, 226, 222), line=(40, 60, 50)):
    """Tete de cerf de face, bois en couronne : brodee d'argent sur la cape."""
    cv = blason.Canvas(64, -1, 1, -1, 1)
    head = [(0, 0.95), (0.16, 0.7), (0.24, 0.3), (0.34, 0.08), (0.2, 0.02), (0.14, -0.08), (-0.14, -0.08), (-0.2, 0.02), (-0.34, 0.08), (-0.24, 0.3), (-0.16, 0.7)]
    cv.fill(head, c, line, 0.03)
    for s in (1, -1):
        cv.fill([(s * 0.3, 0.12), (s * 0.62, 0.0), (s * 0.5, 0.2)], c, line, 0.025)       # oreilles
        beam = [(s * 0.14, -0.06), (s * 0.3, -0.42), (s * 0.5, -0.7), (s * 0.62, -0.95)]
        cv.line(beam, c, 0.045)
        for a, b in (((s * 0.27, -0.36), (s * 0.08, -0.62)), ((s * 0.42, -0.6), (s * 0.3, -0.92)), ((s * 0.52, -0.74), (s * 0.86, -0.8)), ((s * 0.2, -0.22), (s * 0.48, -0.3))):
            cv.line([a, b], c, 0.035)
    cv.dot(0.1, 0.32, 0.035, line)
    cv.dot(-0.1, 0.32, 0.035, line)
    return blason.shrink(cv.img, width_px, width_px)


def emblem_paint(art_fn, frac=0.5, y_frac=0.3):
    def emblem(img, w, h):
        art = art_fn(int(min(w, h) * frac))
        rk.stamp(img, art, (w - len(art[0])) // 2, int(h * y_frac - len(art) / 2.0))
    return emblem


def ring_image(diam_u, c=DIVINE, light=DIVINE_LIGHT, width=0.05, beads=8):
    """Anneau de lumiere, perles d'etoile."""
    n = int(diam_u * D)

    def draw(img, W, H):
        cx, R = W / 2.0, W / 2.0
        for y in range(H):
            for x in range(W):
                r = math.hypot(x + 0.5 - cx, y + 0.5 - cx) / R
                if 1 - 2 * width <= r <= 0.99:
                    img[y][x] = list(mix(light, c, abs(r - (1 - width)) / width)) + [255]
        for k in range(beads):
            a = k * 2 * math.pi / beads + 0.2
            for rr, col in ((W * 0.028, c), (W * 0.016, light)):
                for y in range(H):
                    for x in range(W):
                        if math.hypot(x + 0.5 - cx - (R * (1 - width)) * math.cos(a), y + 0.5 - cx - (R * (1 - width)) * math.sin(a)) <= rr:
                            img[y][x] = list(col) + [255]
    return supersampled(n, n, draw)


# ---------------------------------------------------------------------------------------------------------------------
# Guerrier : Harnois du Seraphin
# ---------------------------------------------------------------------------------------------------------------------
W_STEEL, W_DARK, W_LIGHT = (214, 220, 232), (86, 92, 112), (255, 255, 255)
LAPIS = (16, 36, 92)


def w_steel(**kw):
    base = dict(c=W_STEEL, dark=W_DARK, light=W_LIGHT, gold=GOLD_MID, ground=LAPIS, sheen=0.46, grain=0.04)
    base.update(kw)
    return keep(rk.steel(**base))


WH = rk.Harness(W_STEEL, W_DARK, W_LIGHT, GOLD_MID, LAPIS, sheen=0.46, grain=0.04)
WH.trim = w_steel(lames=2, edge="bottom")
W_GOLD = rk.steel(GOLD_MID, GOLD_SHADE, GOLD_BRIGHT, sheen=0.3)
W_SUN_TOP = w_steel(emblem=emblem_paint(lambda px: sun_art(px), 0.62, 0.5), emblem_faces=("top",))
W_BREAST = w_steel(edge="bottom", flutes=0, bands=((0.03, 12),), emblem=emblem_paint(lambda px: sun_art(px), 0.66, 0.5))
W_EDGE_BOTTOM = w_steel(edge="bottom")
W_EDGE_TOP = w_steel(edge="top")
W_EDGE_BOTH = w_steel(edge="both")
W_LAMES3 = w_steel(lames=3)
W_GREAVE = w_steel(flutes=16, edge="both")
W_SOLE = rk.steel(W_DARK, tuple(v * 0.5 for v in W_DARK), W_STEEL)
W_MAIL = rk.mail((230, 234, 242), (70, 76, 96))


def w_visor(face, w, h, seed):
    img = WH.plain(face, w, h, seed)
    if face == "front":
        rect(img, 4, h * 0.3, w - 2, h * 0.3 + 3, (10, 14, 26))
        rect(img, 4, h * 0.3 + 3, w - 2, h * 0.3 + 4, GOLD_MID)
        for row in range(2):
            for x in range(14, w - 8, 9):
                disc(img, x + (row % 2) * 4, h * 0.66 + row * 6, 1.0, (10, 14, 26))
    return img


def build_guerrier_head():
    m = rk.new_model("Head")
    h = WH
    kids = rk.dome_stack(m, h.spine, band=W_EDGE_BOTTOM)
    kids += rk.back_shell(m, h.plain, -3, 15, cut=2, x=16, z=15.25)
    kids += sym(m, box(m, "R-Mezail", (-7.75, -0.25, 17), (16.75, 11, 1), w_visor, q=rk.yaw(-14), hidden=("back",)))
    kids.append(box(m, "Mezail_Arete", (0, -0.25, 18.75), (1.25, 11, 1), W_GOLD))
    kids += rk.octa(m, "Bavier", (0, -9.75, 0.25), (31.5, 10, 31.5), h.lames2, 1.5, top=False)
    kids += sym(m, box(m, "R-Joue", (-16.25, 0, 9.25), (1.25, 18, 13.5), h.plain))
    kids += sym(m, box(m, "R-Fente", (-8.5, 1.5, 17.75), (8, 0.5, 0.5), rk.glow(DIVINE), q=rk.yaw(-14), shading="fullbright"))
    kids.append(lit_box(m, "Front_Joyau", (0, 5.25, 16.95), (2.5, 2.5, 0.6), DIVINE))
    kids.append(quad(m, "Cimier", (0, 22, 0), rk.comb_image(26, 6), rk.yaw(90)))
    kids += sym(m, box(m, "R-Rosace", (-16.6, 4.25, 4), (0.75, 4, 4), W_GOLD), lit_box(m, "R-Rosace_Joyau", (-17.1, 4.25, 4), (0.5, 2, 2), DIVINE, hidden=("right",)))
    small = fan_wing_image(14, 18)
    root = (17.2, 6.0, 3.0)
    q = qmul(rk.yaw(90 - 14), q_axis((0, 0, 1), 8))
    kids += sym(m, quad(m, "L-Ailette", (root[0] + 0.43 * 14 * math.sin(math.radians(14)), root[1] + 0.4 * 18, root[2] - 0.43 * 14 * math.cos(math.radians(14))), small, q))
    kids.append(lit(quad(m, "Halo", (0, 9, -19), halo_image(34))))
    return m, [rk.piece(m, "Head", (0, 0, 0), kids)]


def build_guerrier_chest():
    m = rk.new_model("Chest")
    h = WH
    arm = rk.plate_arm(m, h, pauldron(m, h, W_SUN_TOP, h.frieze, rk.glow(DIVINE)))
    thigh = rk.thigh_piece(m, rk.tassets(m, h, W_EDGE_BOTTOM))
    pelvis = rk.octa(m, "Hanches", (0, -0.5, 0), (29, 13, 22), W_MAIL, 3.25)
    pelvis += rk.octa(m, "Braconniere_0", (0, 3.25, 0), (30.5, 4.5, 24), h.trim, 4.25, bottom=False)
    pelvis += rk.octa(m, "Braconniere_1", (0, -0.75, 0), (31.5, 3.5, 25), h.trim, 4.5, bottom=False)
    sun_small = rk.embroider(sun_art(40))
    tabard = rk.banner(13, 32, ground=(236, 232, 222), light=(255, 255, 250), dark=(168, 160, 146), hem=GOLD_MID, art=sun_small)
    pelvis.append(quad(m, "Tabard", (0, -17, 13.25), tabard))
    pelvis.append(quad(m, "Tabard_Dos", (0, -17, -13.25), tabard))
    chest = rk.octa(m, "Cuirasse", (0, 0, 0), (32, 24.5, 25), W_BREAST, 4.25, corners=h.plain)
    chest += rk.octa(m, "Gorgerin", (0, 13.4, -0.5), (21.5, 3, 21), h.lames2, 4.5)
    chest += rk.octa(m, "Gorgerin_Haut", (0, 15.75, -0.5), (19, 2, 18.5), W_EDGE_TOP, 4.5)
    chest.append(lit_box(m, "Coeur_Solaire", (0, 0.3, 12.85), (3, 3, 0.6), DIVINE))
    chest.append(box(m, "Attache_Ailes", (0, 4.5, -12.85), (12, 9, 0.75), W_GOLD, hidden=("front",)))
    chest.append(lit_box(m, "Attache_Joyau", (0, 4.5, -13.45), (2.5, 2.5, 0.5), DIVINE, hidden=("front",)))
    belly = rk.octa(m, "Pansiere", (0, 1, 0), (29.5, 16, 23.5), W_LAMES3, 4.5)
    belly += rk.octa(m, "Ceinture", (0, -6.25, 0), (31, 3.5, 25.25), W_GOLD, 4.75)
    belly.append(box(m, "Boucle", (0, -6.25, 13), (7, 5, 1), W_EDGE_BOTH))
    belly.append(lit_box(m, "Boucle_Gemme", (0, -6.25, 13.75), (2.5, 2.5, 0.5), DIVINE))
    wing = angel_wing_image(42, 64)
    root_px = (int(0.03 * len(wing[0])), int(0.5 * len(wing)))
    chest += sym(m, *folded_wing(m, "L-Aile", wing, (4.5, 5.0, -13.4), root_px, int(0.36 * len(wing[0])), (24, 52)))
    return m, rk.body_chain(m, pelvis, belly, chest, arm, thigh)


def build_guerrier_hands():
    m = rk.new_model("Hands")
    gem = lit_box(m, "R-Joyau_Poignet", (0, -6.25, 8.95), (2.5, 2.5, 0.6), DIVINE)
    return m, rk.plate_gauntlet(m, WH, cuff_extra=[gem])


def build_guerrier_legs():
    m = rk.new_model("Legs")
    h = WH
    waist = rk.octa(m, "Taille", (0, 0, 0), (27, 10, 20.5), W_MAIL, 1.25)
    cuisse = rk.octa(m, "R-Cuissard", (0, -0.5, 0), (12.25, 20.5, 14.25), h.fluted, 1.75)
    poleyn = rk.octa(m, "R-Genouillere", (0, 11.5, 2.5), (12.75, 8, 10.5), h.frieze, 2)
    wing_out = box(m, "R-Aile_Genou", (-6.9, 11.5, 0), (1, 6, 7), h.lames2)
    gem = lit_box(m, "R-Joyau_Genou", (0, 11.5, 8.05), (2, 2, 0.6), DIVINE)
    greave = rk.octa(m, "R-Greve", (0, 0.25, 0), (12, 23, 14.5), W_GREAVE, 1.75)
    shoe = rk.octa(m, "R-Soleret", (-0.375, 0.5, 0), (15.25, 8.5, 21.5), h.lames2, 1)
    toe = rk.octa(m, "R-Pointe", (-0.5, -0.6, 11.5), (10.5, 5.25, 4), h.trim, 1.5)
    sole = box(m, "R-Semelle", (-0.375, -3.75, 0), (15.75, 1, 22), W_SOLE)
    return m, rk.legs_chain(m, waist, cuisse, greave + poleyn + [wing_out, gem], shoe + toe + [sole])


# ---------------------------------------------------------------------------------------------------------------------
# Mage : Atours de l'Astre
# ---------------------------------------------------------------------------------------------------------------------
NIGHT_BLUE, NIGHT_BLUE_DARK, NIGHT_BLUE_LIGHT = (26, 30, 80), (6, 8, 28), (66, 74, 152)
PALE, PALE_DARK, PALE_LIGHT = (232, 228, 238), (164, 160, 182), (255, 255, 255)
M_ROBE = starfield(NIGHT_BLUE, NIGHT_BLUE_DARK, NIGHT_BLUE_LIGHT)
M_ROBE_HEM = starfield(NIGHT_BLUE, NIGHT_BLUE_DARK, NIGHT_BLUE_LIGHT, hem=GOLD_MID)
M_ROBE_DARK = shaded(M_ROBE, 0.62)
M_PALE = starfield(PALE, PALE_DARK, PALE_LIGHT, star=GOLD_MID, glow=GOLD_SHADE, lines=False, density=420)
M_GOLD = rk.steel(GOLD_MID, GOLD_SHADE, GOLD_BRIGHT, sheen=0.3)
M_RUNES = rk.steel(GOLD_MID, GOLD_SHADE, GOLD_BRIGHT, gold=DIVINE_LIGHT, ground=NIGHT_BLUE_DARK, sheen=0.3, runes=((0.3, 11),))
M_CRYSTAL = crystal()
M_LEATHER = rk.fine_leather((34, 34, 70), GOLD_MID, dark=(10, 10, 26), light=(80, 84, 140))
M_LEATHER_PLAIN = rk.fine_leather((34, 34, 70), None, dark=(10, 10, 26), light=(80, 84, 140))
M_BOOK = rk.fine_leather((92, 22, 40), GOLD_MID, dark=(36, 8, 16), light=(150, 50, 70), tooled=True)
M_PAGES = rk.ivory


def open_robe(edge_frac=0.17):
    """Robe de nuit ouverte devant sur la sous-robe d'ivoire, orfroi d'or au bord de l'ouverture."""
    def paint(face, w, h, seed):
        img = M_ROBE(face, w, h, seed)
        if face == "front":
            pale = M_PALE("front", w, h, seed)
            half = w * edge_frac
            c = w / 2.0
            for y in range(h):
                for x in range(w):
                    d = abs(x + 0.5 - c)
                    if d < half:
                        img[y][x][:3] = list(pale[y][x][:3])
                    elif d < half + 3:
                        img[y][x][:3] = list(GOLD_MID if d < half + 2 else GOLD_SHADE)
            for y in range(5, h, 10):
                art = star_art(7)
                rk.stamp(img, art, int(c - half - 1.5 - 3.5), y - 3)
                rk.stamp(img, art, int(c + half + 1.5 - 3.5), y - 3)
        return img
    return keep(paint)


M_ROBE_OPEN = open_robe()


def collar_paint(face, w, h, seed):
    """Haut col : etoffe de nuit dehors (dos), doublure d'ivoire runique dedans (face avant, tournee vers la tete)."""
    if face == "front":
        img = M_PALE(face, w, h, seed)
        rk.rune_band(img, h * 0.18, h * 0.18 + 10, NIGHT_BLUE_DARK, GOLD_MID)
    else:
        img = M_ROBE(face, w, h, seed)
    rect(img, 0, 0, w, 3, GOLD_MID)
    return img


def astrolabe_image(size_u=10.0):
    n = int(size_u * D)

    def draw(img, W, H):
        c, R = W / 2.0, W / 2.0
        for y in range(H):
            for x in range(W):
                r = math.hypot(x + 0.5 - c, y + 0.5 - c) / R
                a = math.atan2(y + 0.5 - c, x + 0.5 - c)
                col = None
                if 0.86 <= r <= 1.0:
                    col = mix(GOLD_BRIGHT, GOLD_SHADE, abs(r - 0.93) * 14)
                    if int((a + math.pi) / (2 * math.pi) * 48) % 2 == 0 and r > 0.93:
                        col = GOLD_SHADE
                elif 0.62 <= r <= 0.7 or abs(math.sin(a * 2)) < 0.05 and r < 0.86:
                    col = GOLD_MID
                elif r < 0.62:
                    col = mix(DIVINE_LIGHT, DIVINE, r / 0.62) if r < 0.3 else mix(DIVINE, NIGHT_BLUE, (r - 0.3) / 0.32)
                elif r < 0.86:
                    col = NIGHT_BLUE_DARK
                if col:
                    img[y][x] = list(col) + [255]
        for k in range(4):
            a = k * math.pi / 2 + math.pi / 4
            fill_poly(img, [(c + 0.12 * R * math.cos(a + 1.2), c + 0.12 * R * math.sin(a + 1.2)), (c + 0.82 * R * math.cos(a), c + 0.82 * R * math.sin(a)),
                            (c + 0.12 * R * math.cos(a - 1.2), c + 0.12 * R * math.sin(a - 1.2))], GOLD_BRIGHT)
    return supersampled(n, n, draw)


def mage_cape_image(w=168, h=330):
    """Grande cape de nuit : constellations, etoile d'or brodee, orfrois d'or sur les bords, frise de runes en bas."""
    img = M_ROBE("front", w, h, 41)
    for y in range(h):
        k = 1 - 0.3 * y / float(h)
        for x in range(w):
            for i in range(3):
                img[y][x][i] *= k
    sow_stars(img, 77, NIGHT_BLUE, density=160)
    for x0 in (0, w - 6):
        rect(img, x0, 0, x0 + 6, h, GOLD_MID)
    rect(img, 6, 0, 7, h, GOLD_SHADE)
    rect(img, w - 7, 0, w - 6, h, GOLD_SHADE)
    rect(img, 0, 0, w, 6, GOLD_MID)
    art = rk.embroider(star_art(int(w * 0.46)))
    rk.stamp(img, art, (w - len(art[0])) // 2, int(h * 0.1))
    rk.rune_band(img, h - 34, h - 20, NIGHT_BLUE_DARK, GOLD_MID)
    rect(img, 0, h - 14, w, h, GOLD_MID)
    for x in range(w):                                                          # bas en pointes douces
        cut = int(10 * (1 - abs(math.sin(math.pi * x / (w / 6.0)))))
        for y in range(h - cut, h):
            img[y][x][3] = 0
    return img


def build_mage_head():
    m = rk.new_model("Head")
    brim = keep(rk.velvet(NIGHT_BLUE, NIGHT_BLUE_LIGHT, NIGHT_BLUE_DARK, motif=False, hem=GOLD_MID))
    kids = rk.octa(m, "Bord_Chapeau", (0, 6.25, 0.5), (54, 1.5, 53), brim, 15)
    kids += rk.octa(m, "Calotte", (0, 11.5, 0), (33, 9, 32), M_ROBE, 4, bottom=False)
    kids += rk.octa(m, "Ruban", (0, 8.75, 0), (34, 3.5, 33), M_RUNES, 4.25, bottom=False)
    kids.append(lit(quad(m, "Ruban_Etoile", (0, 9.5, 17.0), star_art(8 * D))))
    cone = [(30.5 - 2.6 * i, 2.75, -0.18 * i * i) for i in range(9)]
    y = 16.0
    for i, (w, hh, z) in enumerate(cone):
        kids += rk.octa(m, "Cone_%d" % i, (0, y + hh / 2.0, z), (w, hh, w - 1), M_ROBE, max(1.0, w * 0.22), bottom=False)
        y += hh
    pts = [(0, y - 1.0, -11.5), (0, y + 2.5, -15.5), (0, y + 2.75, -20.0), (0, y + 0.5, -23.5)]
    kids += [rod(m, "Pointe_%d" % i, pts[i], pts[i + 1], 6.0 - 1.6 * i, M_ROBE) for i in range(len(pts) - 1)]
    kids.append(lit_box(m, "Pointe_Etoile", (0, y - 1.5, -24.25), (2.5, 2.5, 2.5), (255, 226, 140), hidden=()))
    tilt = qmul(q_axis((1, 0, 0), -90 + 16), ID_Q)
    ring_c = (0, 22.0, -3.0)
    kids.append(lit(quad(m, "Orbite", ring_c, ring_image(40), q=qmul(q_axis((0, 0, 1), 10), tilt))))
    for i, a in enumerate((25, 145, 265)):
        p = qrot(qmul(q_axis((0, 0, 1), 10), tilt), (19.0 * math.cos(math.radians(a)), 19.0 * math.sin(math.radians(a)), 0))
        c = tuple(ring_c[k] + p[k] for k in range(3))
        kids.append(box(m, "Cristal_%d" % i, c, (1.75, 4.5, 1.75), M_CRYSTAL, q=qmul(q_axis((0, 0, 1), 20 * (i - 1)), rk.yaw(45)), shading="fullbright"))
    return m, [rk.piece(m, "Head", (0, 0, 0), kids)]


def build_mage_chest():
    m = rk.new_model("Chest")
    bell = keep(open_bottom(M_ROBE_HEM))
    upper = rk.octa(m, "R-Manche", (-0.25, -1.5, 0), (12, 18, 15), M_ROBE, 2.5, top=False)
    upper += rk.octa(m, "R-Epaule", (-1.5, 8.5, 0), (14.5, 5.5, 17.5), M_ROBE_HEM, 3.5)
    upper.append(lit_box(m, "R-Etoile_Epaule", (-8.85, 8.5, 0), (0.6, 2.5, 2.5), DIVINE, hidden=("right",)))
    lower = rk.octa(m, "R-Manche_Bas", (0, 4.5, 0), (11, 7, 14.5), M_ROBE, 2)
    lower += rk.octa(m, "R-Cloche", (0, -3.5, 0.5), (17, 10, 20), bell, 4, bottom=False)
    arm = rk.arm_piece(m, upper, lower)
    robe = box(m, "R-Robe", (-1.25, -11, 0.5), (13.75, 38, 17.25), M_ROBE_HEM, hidden=("top",))
    thigh = rk.thigh_piece(m, [robe])
    pelvis = rk.octa(m, "Hanches", (0, -0.5, 0), (29, 13, 22), M_ROBE_OPEN, 3.25, corners=M_ROBE)
    under = keep(starfield(PALE, PALE_DARK, PALE_LIGHT, star=GOLD_MID, glow=GOLD_SHADE, lines=False, density=420))
    pelvis.append(hang(m, "Pan_Avant", (0, 4, 11.6), (0, -37, 12.6), panel_image(11, 41, under, 3, tip=0.06, hem=GOLD_MID)))
    pelvis.append(hang(m, "Pan_Arriere", (0, 4, -11.6), (0, -37, -13.5), panel_image(14, 41, M_ROBE, 4, tip=0.06, hem=GOLD_MID)))
    chest = rk.octa(m, "Robe_Torse", (0, 0, 0), (31.5, 24.5, 24), M_ROBE_OPEN, 3.75, corners=M_ROBE)
    chest += rk.octa(m, "Mantelet", (0, 13.5, -1), (36, 10, 29), M_ROBE_HEM, 8, bottom=False)
    chest.append(box(m, "Col_Dos", (0, 18.5, -17.25), (25, 14, 1), collar_paint))
    chest += sym(m, box(m, "R-Col_Coin", (-15.75, 18.5, -13.9), (8.75, 14, 1), collar_paint, q=rk.yaw(45)))
    chest.append(rod(m, "Chaine_G", (-6, 11.5, 12.6), (0, 4.5, 12.6), 0.5, M_GOLD))
    chest.append(rod(m, "Chaine_D", (6, 11.5, 12.6), (0, 4.5, 12.6), 0.5, M_GOLD))
    chest.append(lit(quad(m, "Astrolabe", (0, 1.5, 12.45), astrolabe_image(9))))
    chest += rk.cape_quads(m, mage_cape_image(), [((0, 12.5, -16.0), (0, -16, -18.5)), ((0, -16, -18.5), (0, -44, -22.5)), ((0, -44, -22.5), (0, -64, -26))], 33, 44)
    belly = rk.octa(m, "Robe_Ventre", (0, 1, 0), (29.5, 16, 23.5), M_ROBE_OPEN, 4.5, corners=M_ROBE)
    belly += rk.octa(m, "Ceinture", (0, -6.25, 0), (31, 3.5, 25.25), M_RUNES, 4.75)
    belly.append(box(m, "Boucle", (0, -6.25, 13), (6, 5, 1), M_GOLD))
    belly.append(lit_box(m, "Boucle_Cristal", (0, -6.25, 13.75), (2.5, 3, 0.5), DIVINE))
    book = (17.0, -13.5, 10.5)                                                 # grimoire enchaine, a la hanche gauche, devant la main
    belly.append(box(m, "L-Grimoire", book, (2.5, 10, 8), M_BOOK))
    belly.append(box(m, "L-Grimoire_Pages", (book[0], book[1], book[2] - 0.5), (2.0, 9.25, 7.5), M_PAGES))
    belly.append(lit_box(m, "L-Grimoire_Sceau", (book[0] + 1.5, book[1], book[2]), (0.5, 3, 3), DIVINE, hidden=("left",)))
    belly += [box(m, "L-Grimoire_Coin_%d" % i, (book[0] + 1.35, book[1] + sy * 4.5, book[2] + sz * 3.5), (0.5, 1.25, 1.25), M_GOLD, hidden=("left",))
              for i, (sy, sz) in enumerate(((1, 1), (-1, 1), (1, -1), (-1, -1)))]
    belly.append(rod(m, "L-Grimoire_Chaine", (13.5, -6.25, 11.5), (book[0], book[1] + 5, book[2]), 0.6, M_GOLD))
    belly.append(box(m, "R-Etui", (-11.5, -11, -12.75), (3, 9, 3), M_LEATHER, q=q_axis((0, 0, 1), -12)))
    return m, rk.body_chain(m, pelvis, belly, chest, arm, thigh)


def build_mage_hands():
    m = rk.new_model("Hands")
    cuff = rk.octa(m, "R-Bracelet", (0, -2.5, 0), (11.75, 6, 15.5), M_RUNES, 2.5)
    glove = rk.octa(m, "R-Gant", (0, 0.5, 0), (11, 10, 15), M_LEATHER, 0.75)
    ring = box(m, "R-Bague", (-5.85, -3, 0), (0.75, 1.5, 4), M_GOLD)
    gem = box(m, "R-Bague_Gemme", (-6.4, -3, 0), (0.5, 2, 2), rk.glow(DIVINE), shading="fullbright", hidden=("right",))
    return m, rk.hands_chain(m, cuff, glove + [ring, gem])


def build_mage_legs():
    m = rk.new_model("Legs")
    pants = keep(rk.velvet(NIGHT_BLUE, NIGHT_BLUE_LIGHT, NIGHT_BLUE_DARK, motif=False))
    waist = rk.octa(m, "Taille", (0, 0, 0), (27, 11, 20.5), pants, 1.25)
    upper = rk.octa(m, "R-Chausse", (0, -1, 0), (11.5, 21, 13.5), pants, 1.25)
    shin = box(m, "R-Chausse_Bas", (0, 7.5, 0), (10.75, 9, 12.75), pants)
    boot = rk.octa(m, "R-Botte", (0, -3.5, 0), (12, 16.5, 14.5), M_LEATHER, 1.75, top=False)
    turn = rk.octa(m, "R-Botte_Revers", (0, 5.5, 0), (13.25, 3, 15.75), M_RUNES, 2.25)
    shoe = rk.octa(m, "R-Soulier", (-0.375, 0.5, 0), (15.25, 8.5, 21), M_LEATHER_PLAIN, 0.75)
    toe = rod(m, "R-Pointe", (-0.375, 1.0, 10.0), (-0.375, 4.0, 14.5), 3.5, M_LEATHER_PLAIN)
    tip = box(m, "R-Pointe_Or", (-0.375, 4.25, 14.75), (2, 2, 2), M_GOLD)
    sole = box(m, "R-Semelle", (-0.375, -3.75, 0), (15.75, 1, 22), M_GOLD)
    star = lit_box(m, "R-Etoile_Botte", (0, -1, 7.65), (2, 2, 0.6), DIVINE)
    return m, rk.legs_chain(m, waist, upper, [shin] + boot + turn + [star], shoe + [toe, tip, sole])


# ---------------------------------------------------------------------------------------------------------------------
# Archer : Parure du Cerf Celeste
# ---------------------------------------------------------------------------------------------------------------------
FOREST, FOREST_DARK, FOREST_LIGHT = (42, 86, 56), (12, 32, 22), (92, 144, 100)
JADE = (96, 255, 196)
SILVER_HEM = (206, 220, 214)
A_HOOD = rk.velvet(FOREST, FOREST_LIGHT, FOREST_DARK, motif=False)
A_HOOD_HEM = rk.velvet(FOREST, FOREST_LIGHT, FOREST_DARK, motif=False, hem=SILVER_HEM)
A_HOOD_RIM = shaded(A_HOOD, 0.55)
A_LEAVES = rk.engraved_scales(ground=(112, 136, 124), rim=(232, 242, 238), size=12, edge=False)
A_SILVER = rk.steel((206, 216, 212), (88, 104, 98), (250, 255, 252), sheen=0.4)
A_SILVER_RUNES = rk.steel((206, 216, 212), (88, 104, 98), (250, 255, 252), gold=JADE, ground=FOREST_DARK, sheen=0.4, runes=((0.3, 10),))
A_LEATHER = rk.fine_leather((104, 70, 42), SILVER_HEM)
A_LEATHER_PLAIN = rk.fine_leather((104, 70, 42), None)
A_DARK_LEATHER = rk.fine_leather((52, 38, 30), None, dark=(20, 14, 12), light=(110, 84, 62))
A_FLETCH = rk.glow(JADE)


def a_jerkin(face, w, h, seed):
    """Pourpoint de cuir lace devant, oeillets d'argent, couture d'epaule."""
    img = A_LEATHER_PLAIN(face, w, h, seed)
    if face == "front":
        c = w / 2.0
        rect(img, c - 1, 0, c + 1, h, (34, 24, 18))
        for y in range(6, h - 6, 9):
            stroke(img, [(c - 5, y), (c + 5, y + 7)], (196, 176, 132), 0.7)
            stroke(img, [(c + 5, y), (c - 5, y + 7)], (196, 176, 132), 0.7)
            disc(img, c - 5, y, 1.1, SILVER_HEM)
            disc(img, c + 5, y, 1.1, SILVER_HEM)
    if face in ("front", "back"):
        rect(img, 0, 0, w, 2, (54, 36, 24))
    return img


def a_feather_image(w_u=3.5, h_u=7.5):
    """Empenne : plume blanche a la pointe de jade."""
    W, H = int(w_u * D), int(h_u * D)
    img = layer(W, H)
    for y in range(H):
        t = y / float(H)
        half = W * (0.15 + 0.35 * math.sin(math.pi * min(1.0, t * 1.1)) ** 0.7)
        for x in range(W):
            if abs(x + 0.5 - W / 2.0) <= half:
                img[y][x] = list(mix(JADE, (244, 250, 246), min(1.0, t * 2.2))) + [255]
    for y in range(H):
        img[y][W // 2] = [150, 120, 80, 255]
    return img


def ranger_cape_image(w=152, h=230):
    """Cape de foret : velours vert, liseré d'argent, cerf brode, bas decoupe en feuilles."""
    img = rk.velvet(FOREST, FOREST_LIGHT, FOREST_DARK, motif=False)("front", w, h, 53)
    for y in range(h):
        k = 1 - 0.28 * y / float(h)
        for x in range(w):
            for i in range(3):
                img[y][x][i] *= k
    for x0 in (0, w - 4):
        rect(img, x0, 0, x0 + 4, h, SILVER_HEM)
    rect(img, 0, 0, w, 4, SILVER_HEM)
    art = rk.embroider(stag_art(int(w * 0.5)))
    rk.stamp(img, art, (w - len(art[0])) // 2, int(h * 0.12))
    period = 19.0
    for x in range(w):
        f = (x % period) / period
        cut = int(16 * (1 - math.sin(math.pi * f) ** 0.6))
        for y in range(h - cut, h):
            img[y][x][3] = 0
        for y in range(max(0, h - cut - 4), h - cut):
            img[y][x][:3] = list(mix(SILVER_HEM, FOREST, 0.35))
    return img


def build_archer_head():
    m = rk.new_model("Head")
    kids = hood(m, A_HOOD, A_HOOD_RIM, A_HOOD_HEM)
    kids += rk.octa(m, "Col_Capuche", (0, -16, 0.5), (30, 4.5, 31), A_HOOD_HEM, 4, top=False)
    kids.append(lit_box(m, "Joyau_Front", (0, 7.0, 20.25), (2.0, 2.0, 0.6), JADE))
    main = [(-14.5, 13, 2), (-18, 17.5, -1), (-20.5, 22.5, -5), (-20, 28, -10), (-17.5, 32, -14.5)]
    branches = [((-18, 17.5, -1), (-22.5, 20.5, 3.5)), ((-20.5, 22.5, -5), (-25.5, 26.5, -3)), ((-20, 28, -10), (-24, 32.5, -9)), ((-19, 30.5, -12.5), (-16.5, 35, -11))]
    right = [rod(m, "R-Bois_%d" % i, main[i], main[i + 1], 2.6 - 0.4 * i, A_SILVER) for i in range(len(main) - 1)]
    right += [rod(m, "R-Andouiller_%d" % i, a, b, 1.3, A_SILVER) for i, (a, b) in enumerate(branches)]
    right += [box(m, "R-Pointe_%d" % i, b, (1.1, 1.1, 1.1), A_FLETCH, shading="fullbright") for i, (a, b) in enumerate(branches + [(None, main[-1])])]
    right.append(box(m, "R-Meule", (-15.5, 12.5, 2), (4.5, 2, 4.5), A_SILVER_RUNES, q=q_from_y((-0.6, 0.8, 0))))
    leaf = leaf_art(10)
    right.append(quad(m, "R-Feuille_0", (-17.5, 13.5, 6.0), leaf, qmul(rk.yaw(-60), q_axis((0, 0, 1), 40))))
    right.append(quad(m, "R-Feuille_1", (-18.5, 14.5, -1.5), leaf, qmul(rk.yaw(-120), q_axis((0, 0, 1), -30))))
    kids += sym(m, *right)
    return m, [rk.piece(m, "Head", (0, 0, 0), kids)]


def build_archer_chest():
    m = rk.new_model("Chest")
    upper = rk.octa(m, "R-Spaliere", (-1.5, 8.5, 0), (14.5, 5.5, 17.5), A_LEATHER, 3.5)
    upper += rk.octa(m, "R-Spaliere_Argent", (-2, 12, 0), (11.5, 2, 14), A_LEAVES, 3, bottom=False)
    upper += rk.octa(m, "R-Manche", (-0.25, -1.5, 0), (12, 18, 15), A_HOOD, 2.5, top=False)
    lower = rk.octa(m, "R-Manche_Bas", (0, 3.5, 0), (10.25, 10, 13.75), A_HOOD, 2, top=False)
    arm = rk.arm_piece(m, upper, lower)
    thigh = rk.thigh_piece(m, [box(m, "R-Tassette", (-1.25, 3.5, 0.5), (13.75, 9, 16.75), A_LEATHER)])
    pelvis = rk.octa(m, "Hanches", (0, -0.5, 0), (29, 13, 22), A_LEATHER_PLAIN, 3.25)
    leaf = leaf_art(28)
    pelvis.append(quad(m, "Tabard", (0, -11, 11.75), rk.banner(12, 23, ground=FOREST, light=FOREST_LIGHT, dark=FOREST_DARK, hem=SILVER_HEM, fringe=False, art=leaf)))
    chest = rk.octa(m, "Pourpoint", (0, 0, 0), (31.5, 24.5, 24), a_jerkin, 3.75, corners=A_LEATHER_PLAIN)
    chest += rk.octa(m, "Mantelet", (0, 13.5, -1), (36, 10, 29), A_HOOD_HEM, 8, bottom=False)
    chest.append(rod(m, "Baudrier", (-12.5, 11, 12.7), (11.5, -11.5, 12.7), 1.6, A_DARK_LEATHER))
    chest.append(box(m, "Agrafe", (-0.4, -0.25, 13.2), (3, 3, 0.75), A_SILVER))
    chest.append(lit_box(m, "Agrafe_Jade", (-0.4, -0.25, 13.7), (1.5, 1.5, 0.4), JADE))
    chest.append(quad(m, "Broche_Feuille", (-9.5, 10.5, 14.95), leaf_art(16)))
    chest += rk.cape_quads(m, ranger_cape_image(), [((0, 12.5, -15.0), (0, -16, -17.5)), ((0, -16, -17.5), (0, -38, -21))], 31, 37)
    u = (0.40, 0.92, 0.0)
    n = math.sqrt(sum(c * c for c in u))
    u = tuple(c / n for c in u)
    c = (7.0, -0.5, -22.0)
    chest.append(box(m, "Carquois", c, (8, 28, 8), A_LEATHER, q=q_from_y(u)))
    top = tuple(c[i] + u[i] * 14 for i in range(3))
    chest.append(box(m, "Carquois_Col", top, (9, 2.5, 9), A_SILVER, q=q_from_y(u)))
    chest.append(box(m, "Carquois_Bas", tuple(c[i] - u[i] * 13.5 for i in range(3)), (8.75, 2, 8.75), A_SILVER, q=q_from_y(u)))
    chest.append(box(m, "Carquois_Jade", tuple(c[i] + u[i] * 4 - 0.0 for i in range(3)), (2.5, 2.5, 8.5), A_FLETCH, q=q_from_y(u), shading="fullbright"))
    chest.append(rod(m, "Carquois_Sangle", (-12.5, 11.5, -14.5), (top[0] - 2, top[1] - 4, top[2] + 4), 1.4, A_DARK_LEATHER))
    vane = a_feather_image()
    arrows = ((-2.2, 1.6, 1.0), (0.4, 2.2, 3.0), (2.2, 0.8, 0.0), (-1.0, -1.6, 2.2), (1.6, -1.8, 1.4), (-2.4, -0.6, -0.4))
    for i, (ox, oz, extra) in enumerate(arrows):
        a = (top[0] + ox * 0.9, top[1] - 1, top[2] + oz)
        L = 12.5 + extra
        b = (a[0] + u[0] * L, a[1] + u[1] * L, a[2])
        chest.append(rod(m, "Fleche_%d" % i, a, b, 0.6, A_DARK_LEATHER))
        f = (b[0] - u[0] * 3.4, b[1] - u[1] * 3.4, b[2])
        for j, turn in enumerate((0, 90)):
            chest.append(quad(m, "Empenne_%d_%d" % (i, j), f, vane, qmul(q_from_y(u), rk.yaw(turn + 20 * i))))
    belly = rk.octa(m, "Ventre", (0, 1, 0), (29.5, 15.5, 23), a_jerkin, 4, corners=A_LEATHER_PLAIN)
    belly += rk.octa(m, "Ceinture", (0, -6, 0), (31.5, 3.5, 25), A_DARK_LEATHER, 4.25)
    belly.append(box(m, "Boucle", (0, -6, 12.9), (6, 5, 1.25), A_SILVER))
    belly += sym(m, box(m, "R-Bourse", (-14.5, -9.5, 7.5), (4, 6, 5), A_LEATHER))
    belly.append(box(m, "L-Couteau_Fourreau", (12.5, -11, -11.5), (2, 10, 3), A_DARK_LEATHER, q=q_axis((1, 0, 0), -20)))
    belly.append(rod(m, "L-Couteau_Manche", (12.5, -6.6, -10.0), (12.5, -2.8, -8.6), 1.2, A_LEATHER_PLAIN))
    return m, rk.body_chain(m, pelvis, belly, chest, arm, thigh)


def build_archer_hands():
    m = rk.new_model("Hands")
    glove_r = rk.octa(m, "R-Gant", (0, -0.5, 0), (11, 13, 15), A_DARK_LEATHER, 0.75)
    tab = box(m, "R-Doigtier", (-5.9, -3, 0), (0.75, 5, 9), A_LEATHER_PLAIN)
    cuff_r = rk.octa(m, "R-Poignet", (0, -4, 0), (11.75, 6, 15.5), A_LEATHER, 2.5)
    bracer = rk.octa(m, "R-Brassard", (0, -0.5, 0), (11.75, 15, 15.5), A_LEATHER, 2.5)        # bras d'arc : le gauche
    bracer.append(box(m, "R-Brassard_Plaque", (-6.25, -0.5, 0), (0.75, 13, 8), A_SILVER_RUNES))
    bracer.append(lit_box(m, "R-Brassard_Jade", (-6.85, -0.5, 0), (0.5, 2.5, 2.5), JADE, hidden=("right",)))
    bracer += [box(m, "R-Brassard_Sangle_%d" % i, (0, y, 0), (12.25, 1, 16), A_DARK_LEATHER) for i, y in enumerate((-5.5, 4.5))]
    glove_l = rk.octa(m, "R-Mitaine", (0, 0.5, 0), (11, 10, 15), A_DARK_LEATHER, 0.75)
    return m, hands_chain2(m, cuff_r, glove_r + [tab], bracer, glove_l)


def build_archer_legs():
    m = rk.new_model("Legs")
    pants = keep(rk.fine_leather((70, 74, 52), None, dark=(30, 32, 22), light=(116, 120, 90)))
    waist = rk.octa(m, "Taille", (0, 0, 0), (27, 10, 20.5), pants, 1.25)
    thigh = rk.octa(m, "R-Cuissot", (0, -0.5, 0), (12.25, 21, 14), pants, 1.75)
    knee = rk.octa(m, "R-Genouillere", (0, 11.5, 2.5), (12.75, 8, 10.5), A_LEATHER, 2)
    knee_gem = lit_box(m, "R-Joyau_Genou", (0, 11.5, 8.05), (2, 2, 0.6), JADE)
    boot = rk.octa(m, "R-Botte", (0, -0.75, 0), (12.25, 21.5, 14.75), A_LEATHER_PLAIN, 1.75, top=False)
    cuff = rk.octa(m, "R-Botte_Revers", (0, 9.0, 0), (13.25, 4, 16), A_LEATHER, 2.25)
    shin = box(m, "R-Jambiere", (0, -0.5, 7.75), (9, 13, 1), A_LEAVES)
    straps = []
    for i, y in enumerate((-3.5, -7.75)):
        straps += rk.octa(m, "R-Sangle_%d" % i, (0, y, 0), (12.75, 1, 15.25), A_DARK_LEATHER, 2)
    shoe = rk.octa(m, "R-Soulier", (-0.375, 0.75, 0), (15.25, 8, 21.25), A_LEATHER_PLAIN, 0.75)
    sole = box(m, "R-Semelle", (-0.5, -3.5, 0), (15.75, 1.5, 22), A_DARK_LEATHER)
    return m, rk.legs_chain(m, waist, thigh, [knee_gem, shin] + knee + boot + cuff + straps, shoe + [sole])


# ---------------------------------------------------------------------------------------------------------------------
# Assassin : Voile de la Lune Noire
#   shinobi et samourai en dark fantasy, tout en noir : zukin (capuche nouee) au bandeau rouge et plaque de front laquee
#   au croissant, menpo en tete de mort d'os noirci aux orbites spectrales, gorgerin lamellaire ; veste croisee sur
#   cotte de mailles, do lamellaire de laque noire lace de rouge sang, sode aux epaules, kusazuri sur les cuisses,
#   obi rouge sang au tanto, ninjato dans le dos ; kote de mailles a attelles, tekko, lame cachee au poignet gauche ;
#   pantalon serre, bandes molletieres, suneate a attelles, tabi.
# ---------------------------------------------------------------------------------------------------------------------
NOIR, NOIR_DARK, NOIR_LIGHT = (36, 33, 40), (7, 7, 9), (80, 74, 88)
SANG, SANG_DARK, SANG_LIGHT = (100, 14, 26), (34, 4, 9), (156, 36, 48)
OS, OS_DARK, OS_LIGHT = (108, 100, 88), (28, 24, 21), (150, 142, 124)
OMBRE = (8, 7, 10)
SPECTRE = (196, 170, 255)                  # lueur des orbites et des fils de lame
URUSHI, URUSHI_DARK, URUSHI_HI = (24, 22, 27), (4, 4, 6), (112, 108, 126)
ODOSHI, ODOSHI_DARK, ODOSHI_LIGHT = (124, 16, 30), (48, 5, 12), (186, 50, 60)


def weave(ground, dark, light, wrap=0, hem=None, seam=False, lattice=False):
    """Toile teinte : plis souples (bruit etire en hauteur), armure toile fine, fibres. wrap : plis d'enroulement en biais
    tous les wrap px (capuche nouee) ; lattice : losanges ton sur ton (obi) ; hem : galon haut et bas."""
    def paint(face, w, h, seed):
        img = canvas(w, h, ground)
        side = face not in ("top", "bottom")
        for y in range(h):
            for x in range(w):
                f = rk.fbm(x * 0.035 + seed * 0.37, y * (0.011 if side else 0.035) + seed * 0.13)
                c = mix(dark, ground, min(1.0, f * 1.75)) if f < 0.57 else mix(ground, light, min(1.0, (f - 0.57) * 1.5))
                k = (1.035 if (x + y) % 2 == 0 else 0.965) * (0.96 + 0.08 * noise(x * 0.7 + seed, y * 0.7))
                if wrap and side:
                    p = (y + x * 0.42 + seed * 3) % wrap
                    k *= 0.6 if p < 1.5 else 1.12 if p < 3.0 else 1.0
                if lattice:
                    d = abs(x % 16 - 8) + abs(y % 16 - 8)
                    if d in (6, 7):
                        k *= 1.14
                img[y][x][:3] = [c[0] * k, c[1] * k, c[2] * k]
        if side and hem:
            rect(img, 0, h - 4, w, h, hem)
            rect(img, 0, 0, w, 3, hem)
        if seam and face in ("front", "back"):
            for y in range(1, h, 4):
                put(img, w // 2, y, dark)
                put(img, w // 2, y + 1, dark)
        return finish(img, face, edges=False)
    return paint


def urushi(edge=None, splints=0, laces=()):
    """Laque noire : reflet large, filet speculaire net, poussiere fine. splints : attelles verticales, chacune son reflet ;
    laces : positions (0-1) de laçages rouges en travers ; edge : filet de couleur en haut et en bas."""
    def paint(face, w, h, seed):
        img = canvas(w, h, URUSHI)
        side = face not in ("top", "bottom")
        sw = w / float(splints) if splints and side else w
        for y in range(h):
            for x in range(w):
                u = ((x % sw) + 0.5) / sw
                k = 0.45 + 0.35 * math.cos((u - 0.32) * math.pi * 1.4) + 0.06 * (rk.fbm(x * 0.05 + seed, y * 0.05) - 0.5)
                spec = max(0.0, 1 - abs(u - 0.28) / 0.07) ** 2 * (0.75 if side else 0.3) * min(1.0, min(w, h) / 20.0)
                c = mix(URUSHI_DARK, URUSHI, min(1.0, k * 1.6))
                c = mix(c, URUSHI_HI, spec * 0.85)
                if splints and side and (x % sw) < 1:
                    c = URUSHI_DARK
                img[y][x][:3] = list(c)
        if side:
            for v in laces:
                y0 = int(v * h)
                for x in range(w):
                    if (x // 3) % 2 == 0:
                        rect(img, x, y0, x + 1, y0 + 2, ODOSHI)
                        put(img, x, y0, ODOSHI_LIGHT)
                rect(img, 0, y0 + 2, w, y0 + 3, URUSHI_DARK, 0.6)
            if edge:
                rect(img, 0, 0, w, 2, edge)
                rect(img, 0, h - 2, w, h, edge)
        return finish(img, face, edges=False)
    return paint


def kozane(row=12, plate=6, lace_every=3, hishi=False):
    """Lamelles de laque noire en rangs, lacees de soie rouge sang (sugake : laçages espaces). Chaque rang a son liseré
    de lumiere en haut et l'ombre du rang suivant en bas ; hishi : croix de laçage au dernier rang."""
    def paint(face, w, h, seed):
        if face in ("top", "bottom"):
            return urushi()(face, w, h, seed)
        img = canvas(w, h, URUSHI)
        for y in range(h):
            r, j = y % row, y // row
            t = r / float(row - 1)
            for x in range(w):
                pc, pi = x % plate, x // plate
                if r <= 1:
                    c = mix(URUSHI_HI, URUSHI, r * 0.6)
                else:
                    c = mix(URUSHI, URUSHI_DARK, t ** 2 * 0.85)
                c = mix(c, URUSHI_HI, 0.14 * noise(pi * 1.7 + seed, j * 2.3))
                if pc == 0:
                    c = mix(c, URUSHI_DARK, 0.65)
                if (pi + j) % lace_every == 1 and 2 <= pc <= 3:
                    if r == 2:
                        c = URUSHI_DARK
                    elif r > 2:
                        c = mix(mix(ODOSHI_LIGHT, ODOSHI, abs(pc - 2.5) * 1.4), ODOSHI_DARK, t * 0.55)
                img[y][x][:3] = list(c)
        if hishi:
            y0 = h - row + 3
            for x0 in range(2, w - 6, 9):
                stroke(img, [(x0, y0), (x0 + 6, y0 + 6)], ODOSHI, 0.7)
                stroke(img, [(x0 + 6, y0), (x0, y0 + 6)], ODOSHI_LIGHT, 0.7)
        return finish(img, face, edges=False)
    return paint


def bindings(c=(48, 44, 52), dark=(12, 11, 14)):
    """Bandes molletieres : bandes de toile enroulees en biais, chacune dans l'ombre de la suivante."""
    base = weave(c, dark, mix(c, (140, 136, 150), 0.35))

    def paint(face, w, h, seed):
        img = base(face, w, h, seed)
        if face not in ("top", "bottom"):
            for y in range(h):
                for x in range(w):
                    p = (y + x * 0.3) % 7
                    k = 0.62 if p < 1 else 1.12 if p < 2 else 1.0 - 0.04 * p
                    img[y][x][:3] = [v * k for v in img[y][x][:3]]
        return img
    return paint


def tsuka(face, w, h, seed):
    """Poignee : tresse de soie noire en losanges sur peau de raie claire."""
    img = canvas(w, h, OS)
    for y in range(h):
        for x in range(w):
            a, b = (x + y) % 8, (x - y) % 8
            if a < 3 or b < 3:
                k = 0.8 + 0.4 * (1 - abs((a if a < 3 else b) - 1) / 1.5)
                img[y][x][:3] = [v * k for v in (40, 36, 44)]
            else:
                img[y][x][:3] = list(mix(OS, OS_DARK, 0.5 * noise(x * 0.9, y * 0.9)))
    return finish(img, face, edges=False)


S_CLOTH = weave(NOIR, NOIR_DARK, NOIR_LIGHT)
S_CLOTH_SEAM = weave(NOIR, NOIR_DARK, NOIR_LIGHT, seam=True)
S_HOOD = weave(NOIR, NOIR_DARK, NOIR_LIGHT, wrap=22)
S_HOOD_RIM = shaded(S_HOOD, 0.55)
S_LAPEL = weave(mix(NOIR, NOIR_DARK, 0.4), NOIR_DARK, NOIR, hem=SANG_DARK)
S_OBI = weave(SANG, SANG_DARK, SANG_LIGHT, lattice=True)
S_BAND = weave(SANG, SANG_DARK, SANG_LIGHT)
S_CORD = rk.wraps((40, 36, 44), (10, 9, 12))
S_MAIL = rk.mail((78, 78, 92), (10, 10, 13))
S_URUSHI = urushi()
S_URUSHI_EDGE = urushi(edge=ODOSHI)
S_SPLINTS = urushi(splints=3, laces=(0.22, 0.7))
S_KOZANE = kozane()
S_KOZANE_HISHI = kozane(hishi=True)
S_BIND = bindings()
S_GLOVE = rk.fine_leather((26, 23, 27), None, dark=(5, 5, 6), light=(72, 64, 70))
S_TABI = weave((30, 28, 34), (6, 6, 8), (70, 66, 78))


def uwagi(face, w, h, seed):
    """Veste croisee : en V entre les pans du col, la cotte de mailles portee dessous."""
    img = S_CLOTH_SEAM(face, w, h, seed)
    if face == "front":
        mail = S_MAIL(face, w, h, seed)
        for y in range(h):
            Y = 12.0 - (y + 0.5) / D
            for x in range(w):
                X = (x + 0.5) / D - w / (2.0 * D)
                if Y > 3.0 and abs(X) < (Y - 3.0) * 0.85:
                    img[y][x][:3] = list(mail[y][x][:3])
    return img


def tabi(face, w, h, seed):
    """Tabi : orteil fendu, couture, agrafes au talon."""
    img = S_TABI(face, w, h, seed)
    if face == "front":
        rect(img, w * 0.38, h * 0.25, w * 0.38 + 1, h, OMBRE)
    elif face == "top":
        rect(img, w * 0.38, h * 0.62, w * 0.38 + 1, h, OMBRE)
    elif face == "back":
        for y in range(3, h - 2, 6):
            disc(img, w * 0.7, y, 0.9, (120, 118, 132))
    return img


def bone(face, w, h, seed):
    """Os noirci : patine crasseuse, bords dans l'ombre."""
    img = canvas(w, h, OS)
    for y in range(h):
        for x in range(w):
            g = rk.fbm(x * 0.09 + seed, y * 0.09)
            c = mix(OS_DARK, OS, min(1.0, 0.35 + g * 1.1)) if g < 0.55 else mix(OS, OS_LIGHT, (g - 0.55) * 1.2)
            img[y][x][:3] = list(c)
    return finish(img, face, 0.92)


def skull_mask(face, w, h, seed):
    """Menpo en tete de mort : silhouette de crane dans l'ombre du zukin, orbites profondes, joues creusees, fosse nasale,
    dents, suie qui coule des orbites, felures."""
    if face != "front":
        return S_HOOD_RIM(face, w, h, seed)
    img = canvas(w, h, OMBRE)
    rng = random.Random(seed)
    for y in range(h):
        for x in range(w):
            u, v = (x + 0.5) / w * 2 - 1, (y + 0.5) / h
            hw = 0.93 if v < 0.55 else 0.93 - 0.34 * ((v - 0.55) / 0.45) ** 1.3
            if v > 0.9:
                hw *= math.sqrt(max(0.0, 1 - ((v - 0.9) / 0.1) ** 2)) * 0.5 + 0.5
            if abs(u) > hw:
                continue
            g = rk.fbm(x * 0.08 + seed, y * 0.08)
            col = mix(OS_DARK, OS, min(1.0, 0.3 + g * 1.15)) if g < 0.6 else mix(OS, OS_LIGHT, (g - 0.6) * 1.4)
            col = mix(col, OS_DARK, 0.65 * (abs(u) / hw) ** 3)
            col = mix(col, OS_DARK, 0.5 * max(0.0, 1 - v / 0.14))
            for sx in (-0.38, 0.38):
                d = math.hypot((u - sx) / 0.25, (v - 0.37) / (0.15 if v < 0.37 else 0.19))
                if d < 1.0:
                    col = mix(OMBRE, OS_DARK, max(0.0, (d - 0.72) / 0.28))
                elif d < 1.25 and v > 0.36:
                    col = mix(col, OS_DARK, 0.5 * (1.25 - d) / 0.25)
            dc = math.hypot((abs(u) - 0.62) / 0.22, (v - 0.68) / 0.09)
            if dc < 1.0:
                col = mix(col, OS_DARK, 0.6 * (1 - dc))
            if 0.52 < v < 0.69 and abs(u) < 0.12 * (v - 0.5) / 0.19 + 0.01:
                col = OMBRE
            if 0.76 < v < 0.94 and abs(u) < 0.46:
                tooth = ((u + 0.46) / 0.092) % 1.0
                col = mix(OS_LIGHT, OS, abs(tooth - 0.5) * 1.4)
                if tooth < 0.12 or v > 0.925:
                    col = OS_DARK
                if abs(v - 0.85) < 0.012:
                    col = OMBRE
            img[y][x][:3] = list(col)
    for sx in (-0.38, 0.38):
        for k in range(5):
            x0 = (sx + rng.uniform(-0.16, 0.16) + 1) / 2 * w
            for y in range(int(0.52 * h), int(rng.uniform(0.62, 0.78) * h)):
                if img[y][int(x0)][:3] != list(OMBRE):
                    put(img, x0, y, OS_DARK, 0.45)
    for _ in range(4):
        x, y = rng.uniform(0.15, 0.85) * w, rng.uniform(0.02, 0.3) * h
        for _ in range(int(h * 0.35)):
            if img[int(y)][int(x)][:3] != list(OMBRE):
                put(img, x, y, OS_DARK)
            x += rng.uniform(-1.0, 1.0)
            y += rng.uniform(0.2, 1.0)
            if not (0 <= x < w and 0 <= y < h):
                break
    return finish(img, face, edges=False)


def stick(m, name, a, b, w, d, paint, **kw):
    """Piece longue de a vers b, de largeur w (dans le plan de a, b et Y) et d'epaisseur d."""
    v = tuple(b[i] - a[i] for i in range(3))
    L = math.sqrt(sum(c * c for c in v))
    return box(m, name, tuple((a[i] + b[i]) / 2.0 for i in range(3)), (w, L, d), paint, q=q_from_y(v), **kw)


def blade_on_back(m, name, mouth, end, grip, paint_saya):
    """Sabre au fourreau : fourreau de mouth a end, garde, poignee tressee de grip unites, pommeau d'os."""
    v = tuple(end[i] - mouth[i] for i in range(3))
    n = math.sqrt(sum(c * c for c in v))
    u = tuple(c / n for c in v)
    q = q_from_y(u)
    hilt = tuple(mouth[i] - u[i] * grip for i in range(3))
    tip = tuple(end[i] + u[i] * 0.75 for i in range(3))
    return [stick(m, name + "_Saya", mouth, end, 2.4, 1.4, paint_saya),
            box(m, name + "_Kojiri", tip, (2.6, 1.5, 1.6), S_URUSHI_EDGE, q=q),
            box(m, name + "_Tsuba", mouth, (4.5, 0.75, 3.25), S_STEEL, q=q),
            stick(m, name + "_Tsuka", mouth, hilt, 1.9, 1.9, tsuka),
            box(m, name + "_Kashira", tuple(hilt[i] - u[i] * 0.6 for i in range(3)), (2.2, 1.2, 2.2), bone, q=q)]


BLACK_STEEL = dict(c=(46, 46, 54), dark=(8, 8, 11), light=(104, 106, 122), sheen=0.3, grain=0.1)
S_STEEL = rk.steel(**BLACK_STEEL)
S_SAYA = urushi(laces=(0.08,))


FUMEE_NOIR, FUMEE_MID, FUMEE_LIGHT, FUMEE_VIOLET = (5, 4, 7), (34, 31, 40), (70, 66, 80), (64, 48, 92)


def smoke(face, w, h, seed):
    """Fumee d'ombre : bruit deforme par lui-meme (volutes), etire vers le haut, presque noir, quelques reflets violets."""
    img = canvas(w, h, FUMEE_NOIR)
    side = face not in ("top", "bottom")
    for y in range(h):
        for x in range(w):
            qx = rk.fbm(x * 0.045 + seed * 0.7, y * 0.045)
            qy = rk.fbm(x * 0.045 + 5.2, y * 0.045 + seed * 0.3)
            f = rk.fbm(x * 0.04 + 3.0 * qx, y * (0.022 if side else 0.04) - 3.0 * qy + seed)
            c = mix(FUMEE_NOIR, FUMEE_MID, min(1.0, (f ** 1.8) * 2.2))
            if f > 0.68:
                c = mix(c, FUMEE_LIGHT if qx > 0.5 else FUMEE_VIOLET, min(1.0, (f - 0.68) * 3.5))
            img[y][x][:3] = list(c)
    return finish(img, face, 0.95, edges=False)


def wisp_image(w_u, h_u, seed, strands=9):
    """Volutes qui montent : filets courbes, epais a la base, effiles en haut, du gris d'ombre au noir."""
    W, H = int(w_u * D), int(h_u * D)
    rng = random.Random(seed)

    def draw(img, Wp, Hp):
        for k in range(strands):
            x0 = Wp * rng.uniform(0.2, 0.8)
            pts = [(x0, Hp * 1.0)]
            x, sway = x0, rng.uniform(-1, 1)
            for i in range(1, 7):
                x += Wp * 0.09 * math.sin(i * 1.3 + sway * 3) + Wp * rng.uniform(-0.05, 0.05)
                pts.append((min(Wp * 0.95, max(Wp * 0.05, x)), Hp * (1 - i / 6.0 * rng.uniform(0.75, 1.0))))
            line = curve(pts, 10)
            n = len(line)
            for i, (px_, py_) in enumerate(line):
                t = i / float(n - 1)
                r = Wp * (0.17 - 0.13 * t) * rng.uniform(0.8, 1.15)
                col = mix(mix(FUMEE_MID, FUMEE_LIGHT, 0.25), FUMEE_NOIR, t * 0.75)
                if k % 3 == 0 and t < 0.6:
                    col = mix(col, FUMEE_VIOLET, 0.35)
                for yy in range(int(py_ - r) - 1, int(py_ + r) + 2):
                    for xx in range(int(px_ - r) - 1, int(px_ + r) + 2):
                        if 0 <= yy < Hp and 0 <= xx < Wp and math.hypot(xx + 0.5 - px_, yy + 0.5 - py_) <= r:
                            img[yy][xx] = list(mix(col, FUMEE_NOIR, math.hypot(xx + 0.5 - px_, yy + 0.5 - py_) / max(1.0, r) * 0.6)) + [255]
    return supersampled(W, H, draw)


CLOUD_D = 2
HAKAMA, HAKAMA_DARK, HAKAMA_STRIPE = (32, 30, 36), (10, 9, 12), (58, 54, 64)


def hakama(face, w, h, seed):
    """Hakama de soie noire rayee (fines rayures sombres et grises, facon sendai-hira) a grands plis : chaque pli s'eclaire
    vers son bord et s'enfonce au creux ; ourlet assombri et effiloche de noir."""
    img = weave(HAKAMA, HAKAMA_DARK, mix(HAKAMA, HAKAMA_STRIPE, 0.6))(face, w, h, seed)
    if face in ("top", "bottom"):
        return img
    P = 24
    for y in range(h):
        v = y / float(h)
        for x in range(w):
            t = (x % P) / float(P)
            k = 0.7 + 0.42 * t ** 0.8
            if x % P == 0:
                k = 0.42
            elif x % P == P - 1:
                k = 1.18
            s = x % 6
            p = img[y][x]
            if s == 2:
                p[:3] = list(mix(p[:3], HAKAMA_STRIPE, 0.18))
            elif s == 4:
                p[:3] = list(mix(p[:3], HAKAMA_DARK, 0.25))
            k *= 1.0 - 0.25 * max(0.0, v - 0.82) / 0.18
            p[0], p[1], p[2] = p[0] * k, p[1] * k, p[2] * k
    rect(img, 0, h - 3, w, h, HAKAMA_DARK)
    return img


def kimono_sleeve(face, w, h, seed):
    """Manche de kimono : toile noire, kamon au croissant sur la face exterieure, ourlet rouge sombre."""
    img = S_CLOTH(face, w, h, seed)
    if face == "left":
        art = rk.harden(crescent_image(5, (60, 54, 70), (96, 88, 110), thin=0.3))
        rk.stamp(img, art, (w - len(art[0])) // 2, int(h * 0.2))
    if face not in ("top", "bottom"):
        rect(img, 0, h - 3, w, h, SANG_DARK)
    return img


S_SMOKE = keep(smoke)
S_HAKAMA = keep(hakama)
S_SLEEVE = keep(open_bottom(kimono_sleeve))
S_HAKAMA_OPEN = keep(open_bottom(hakama))
S_JUBAN = weave(SANG_DARK, (14, 2, 4), SANG)


def anchor(m, name, pos):
    """Noeud vide : point d'attache des particules de fumee."""
    return {"id": m.nid(), "name": name, "position": {"x": pos[0], "y": pos[1], "z": pos[2]}, "orientation": dict(ID_Q),
            "shape": {"type": "none", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": 1, "y": 1, "z": 1}, "settings": {},
                      "textureLayout": {}, "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": "flat"},
            "children": []}


def cloud_image(w_u, h_u, seed, blobs=30, lift=0.0, spread=0.36, dens=CLOUD_D):
    """Nuage de fumee noire : boules de fumee fondues, eclairees par le haut, bords effiloches, reflets violets.
    dens : px par unite (la fumee est floue, 2 px suffisent et menagent l'atlas)."""
    W, H = int(w_u * dens), int(h_u * dens)
    rng = random.Random(seed)

    def draw(img, Wp, Hp):
        bl = []
        for _ in range(blobs):
            a, rr = rng.uniform(0, 2 * math.pi), math.sqrt(rng.random())
            r = Wp * rng.uniform(0.11, 0.19) * (1.2 - 0.45 * rr)
            bl.append((Wp * (0.5 + spread * rr * math.cos(a)), Hp * (0.52 - lift + spread * rr * math.sin(a)), r))
        for y in range(Hp):
            for x in range(Wp):
                best, bb = 0.0, None
                for cx, cy, r in bl:
                    d = math.hypot(x - cx, y - cy) / r
                    if d < 1.0 and 1.0 - d > best:
                        best, bb = 1.0 - d, (cx, cy, r)
                if bb is None:
                    continue
                n = rk.fbm(x * 0.05 * D / dens + seed * 3.1, y * 0.05 * D / dens)
                if best + 0.4 * (n - 0.5) < 0.05:
                    continue
                cx, cy, r = bb
                lt = max(0.0, min(1.0, 0.45 - 0.55 * (y - cy) / r - 0.2 * (x - cx) / r))
                c = mix(FUMEE_NOIR, FUMEE_MID, lt * 0.85 + 0.25 * n)
                if n > 0.64:
                    c = mix(c, FUMEE_VIOLET, min(1.0, (n - 0.64) * 2.0))
                c = mix(c, FUMEE_NOIR, max(0.0, 0.25 - best) * 2.5)
                img[y][x] = list(c) + [255]
    return supersampled(W, H, draw)


def quad_lo(m, name, pos, img, q=ID_Q, dens=None):
    """Quad dont l'image est a dens px par unite (et non D)."""
    node = quad(m, name, pos, img, q)
    s = 1.0 / (dens or CLOUD_D)
    node["shape"]["stretch"] = {"x": s, "y": s, "z": s}
    return node


def ink_mark_image(size_u=5.0):
    """Marque d'encre noire au front : croissant trace au pinceau, une goutte qui coule, eclaboussures."""
    n = int(size_u * D)

    def draw(img, W, H):
        cv = layer(W, H)
        c, R = W / 2.0, W * 0.36
        for k in range(60):                                                   # croissant : trait epais qui s'affine aux cornes
            t = k / 59.0
            a = math.radians(120 + 240 * t)
            r = W * (0.025 + 0.075 * math.sin(math.pi * t))
            x, y = c + R * math.cos(a) * 0.9, c * 0.92 + R * math.sin(a)
            for yy in range(int(y - r) - 1, int(y + r) + 2):
                for xx in range(int(x - r) - 1, int(x + r) + 2):
                    if 0 <= yy < H and 0 <= xx < W and math.hypot(xx + 0.5 - x, yy + 0.5 - y) <= r:
                        img[yy][xx] = [6, 5, 8, 255]
        for yy in range(int(H * 0.8), int(H * 0.98)):                        # goutte qui coule
            for xx in range(int(W * 0.47), int(W * 0.53) + 1):
                img[yy][xx] = [6, 5, 8, 255]
        for x, y, r in ((0.5, 0.97, 0.035), (0.18, 0.2, 0.02), (0.84, 0.74, 0.018)):
            for yy in range(H):
                for xx in range(W):
                    if math.hypot(xx + 0.5 - x * W, yy + 0.5 - y * H) <= r * W:
                        img[yy][xx] = [6, 5, 8, 255]
    return supersampled(n, n, draw)


def build_assassin_head():
    """Plus de casque : la tete du joueur reste visible, marquee au front d'un croissant d'encre noire. La fumee noire est un effet
    d'entite (Armure_Fumee_Ombre, particules Eldanior_Fumee_Ombre sur l'os Head) pose par ArmorVisualSystem tant que la piece
    est portee."""
    m = rk.new_model("Head")
    kids = [quad(m, "Marque", (0, 7.0, 14.6), ink_mark_image(7.5))]
    return m, [rk.piece(m, "Head", (0, 0, 0), kids)]


def smoke_icon(path):
    """Icone du casque : un nuage de fumee noire (le modele ne porte qu'une marque, trop petite pour une icone)."""
    import png
    img = cloud_image(16, 16, 77, blobs=26, spread=0.3, dens=4)
    mark = ink_mark_image(3.0)
    rk.stamp(img, [[list(p[:3]) + [255 if p[3] else 0] for p in row] for row in mark], 26, 22)
    png.write(path, 64, 64, [[[int(v) for v in p] for p in row] for row in img])


def build_assassin_chest():
    m = rk.new_model("Chest")
    sleeve = rk.octa(m, "R-Manche", (-0.75, 0.25, 0), (14.5, 21.5, 17.5), S_CLOTH_SEAM, 3)
    sode = box(m, "R-Sode", (-9.75, 3.5, 0.5), (1.5, 15, 17), S_KOZANE_HISHI, q=q_axis((0, 0, 1), -12))
    sode_top = box(m, "R-Sode_Kanmuri", (-8.15, 11.0, 0.5), (2.5, 2, 18), S_URUSHI_EDGE, q=q_axis((0, 0, 1), -12))
    sode_cord = rod(m, "R-Sode_Cordon", (-7.6, 12.0, 7.0), (-3.0, 13.0, 10.25), 0.6, S_BAND)
    fore = rk.octa(m, "R-Manche_Bas", (0, 6.5, 0), (13.25, 4, 16.75), S_CLOTH, 2.5, top=False)
    fore += rk.octa(m, "R-Tamoto", (0, 2.5, 0.5), (16.5, 9, 20), S_SLEEVE, 4, bottom=False)
    arm = rk.arm_piece(m, sleeve + [sode, sode_top, sode_cord], fore)
    kz = [box(m, "R-Kusazuri_Av", (0.5, 10.0, 10.75), (13.5, 12, 1.25), S_KOZANE_HISHI, q=q_axis((1, 0, 0), -10)),
          box(m, "R-Kusazuri_Cote", (-10.0, 10.0, 0.5), (1.25, 12, 15), S_KOZANE_HISHI, q=q_axis((0, 0, 1), -10)),
          box(m, "R-Kusazuri_Ar", (0.5, 10.0, -9.75), (13.5, 12, 1.25), S_KOZANE_HISHI, q=q_axis((1, 0, 0), 10))]
    thigh = rk.thigh_piece(m, kz)
    pelvis = rk.octa(m, "Hanches", (0, -0.5, 0), (29, 13, 22), S_HAKAMA, 3.25)
    pelvis.append(box(m, "Koshi_Ita", (0, 1.5, -11.6), (12, 6, 1.25), S_URUSHI_EDGE))
    chest = rk.octa(m, "Uwagi", (0, 0, 0), (31.5, 24.5, 24), uwagi, 3.75, corners=S_CLOTH)
    chest.append(stick(m, "Juban_Dessus", (6.25, 12.6, 12.35), (-1.5, 4.0, 12.35), 2.0, 0.8, S_JUBAN))
    chest.append(stick(m, "Juban_Dessous", (-6.25, 12.6, 12.25), (0.5, 5.5, 12.25), 2.0, 0.5, S_JUBAN))
    chest.append(stick(m, "Col_Dessus", (8.5, 12.75, 12.9), (-6.0, -5.0, 12.9), 4.25, 1.0, S_LAPEL))
    chest.append(stick(m, "Col_Dessous", (-8.5, 12.75, 12.55), (2.5, 0.5, 12.55), 4.25, 0.6, S_LAPEL))
    chest += rk.octa(m, "Col_Nuque", (0, 12.9, -2.5), (20, 2.25, 17), S_LAPEL, 3)
    chest += rk.octa(m, "Do", (0, -8.85, 0), (33, 6, 26), S_KOZANE, 4.5)
    chest += rk.octa(m, "Do_Bord", (0, -5.125, 0), (33.5, 1.75, 26.5), S_URUSHI_EDGE, 4.6)
    for side in (-1, 1):                                                     # watagami : bretelles laquees du do
        tag = "L" if side > 0 else "R"
        chest.append(stick(m, "%s-Watagami_Av" % tag, (side * 10.5, -4.25, 13.15), (side * 10.5, 12.5, 12.4), 3, 0.75, S_URUSHI_EDGE))
        chest.append(stick(m, "%s-Watagami_Ar" % tag, (side * 10.5, -4.25, -13.15), (side * 10.5, 12.5, -12.4), 3, 0.75, S_URUSHI_EDGE))
    knot = (0, -3.0, -13.9)                                                  # agemaki : noeud de soie rouge au dos
    chest.append(box(m, "Agemaki", knot, (3, 3, 1.5), S_BAND))
    chest += sym(m, box(m, "R-Agemaki_Boucle", (-3.0, -1.6, -13.75), (4, 2.5, 1), S_BAND, q=q_axis((0, 0, 1), 28)),
                 stick(m, "R-Agemaki_Pan", (-1.0, -4.0, -13.8), (-3.25, -11.5, -14.3), 1.75, 0.6, S_BAND))
    chest += blade_on_back(m, "Ninjato", (-7.0, 8.5, -15.1), (8.5, -15.5, -15.1), 9, S_SAYA)
    belly = rk.octa(m, "Do_Ventre", (0, 2.5, 0), (31, 9, 24.5), S_KOZANE, 4.25)
    belly += rk.octa(m, "Obi", (0, -4.25, 0), (31.75, 5.5, 25.5), S_OBI, 4.5)
    belly += rk.octa(m, "Obi_Cordon", (0, -2.5, 0), (32.25, 1, 26), S_CORD, 4.75)
    belly.append(box(m, "Obi_Noeud", (0, -4.25, -13.9), (7, 4.5, 2.5), S_OBI))
    belly.append(hang(m, "Obi_Pan_0", (-2.0, -6.0, -14.4), (-3.5, -21, -15.6), panel_image(3.5, 15, S_OBI, 5, tip=0.3)))
    belly.append(hang(m, "Obi_Pan_1", (2.0, -6.0, -14.6), (3.0, -18, -15.4), panel_image(3.5, 12, S_OBI, 6, tip=0.3)))
    belly += blade_on_back(m, "Tanto", (5.5, -3.0, 13.6), (13.5, -6.75, 13.6), 4.5, S_SAYA)
    return m, rk.body_chain(m, pelvis, belly, chest, arm, thigh)


def build_assassin_hands():
    m = rk.new_model("Hands")

    def side(hidden_blade):
        kote = rk.octa(m, "R-Kote", (0, -1, 0), (11.75, 14, 15.5), S_MAIL, 2.5)
        kote.append(box(m, "R-Kote_Attelles", (-6.25, -1.25, 0), (0.75, 11.5, 10), S_SPLINTS))
        kote += rk.octa(m, "R-Kote_Anneau", (0, 6.75, 0), (12.5, 1.5, 16.25), S_URUSHI_EDGE, 2.75)
        kote += rk.octa(m, "R-Kote_Poignet", (0, -6.5, 0), (12.25, 1.5, 16), S_CORD, 2.75)
        if hidden_blade:
            kote.append(box(m, "R-Lame_Cachee", (0, -2, 8.25), (2.5, 14, 0.5), S_STEEL))
            kote.append(box(m, "R-Lame_Fil", (0, -2.5, 8.6), (0.4, 12, 0.3), rk.glow(SPECTRE), shading="fullbright"))
        hand = rk.octa(m, "R-Gant", (0, -0.5, 0), (11, 13, 15), S_GLOVE, 0.75)
        hand.append(box(m, "R-Tekko", (-6.0, 1.5, 0), (0.75, 7, 10), S_URUSHI_EDGE))
        hand.append(box(m, "R-Tekko_Lien", (-6.5, -2.5, 0), (0.5, 1, 10.5), S_BAND))
        return kote, hand

    kr, hr = side(False)
    kl, hl = side(True)
    return m, hands_chain2(m, kr, hr, kl, hl)


def pleats(m, name, x0, y, z, h, xs, paint):
    """Plis du hakama en relief : aretes posees sur la face avant ou arriere."""
    return [box(m, "%s_%d" % (name, i), (x0 + dx, y, z), (1.25, h, 0.75), paint) for i, dx in enumerate(xs)]


def build_assassin_legs():
    m = rk.new_model("Legs")
    waist = rk.octa(m, "Taille", (0, 0, 0), (28.5, 10, 21.5), S_HAKAMA, 1.5)
    waist += sym(m, rod(m, "R-Himo", (-14.4, 3.0, 2.0), (-1.0, -2.5, 11.1), 1.0, S_BAND))
    thigh = rk.octa(m, "R-Hakama_Haut", (-0.65, -0.5, 0.5), (16.25, 21, 19.5), S_HAKAMA, 3)
    thigh += pleats(m, "R-Pli_Cuisse", -0.65, -3.75, 10.6, 13.5, (-4.5, 1.5), S_HAKAMA)
    holster = [box(m, "R-Etui_Kunai", (-9.6, 0.5, 1.5), (1.5, 7, 6), S_GLOVE)]
    holster += [rod(m, "R-Kunai_%d" % i, (-9.6, 4.0, z), (-9.6, 7.5, z), 0.8, tsuka) for i, z in enumerate((-0.5, 1.5, 3.5))]
    holster += [box(m, "R-Kunai_Anneau_%d" % i, (-9.6, 8.25, z), (0.5, 1.5, 1.5), S_STEEL) for i, z in enumerate((-0.5, 1.5, 3.5))]
    calf = rk.octa(m, "R-Hakama_Bas", (-0.6, 0.25, 0.5), (16.75, 23.5, 19.75), S_HAKAMA, 3.25, top=False, bottom=False)
    calf += pleats(m, "R-Pli_Bas", -0.6, -0.5, 10.75, 21.5, (-4.5, 1.5), S_HAKAMA)
    calf += pleats(m, "R-Pli_Dos", -0.6, -0.5, -9.75, 21.5, (-3.0, 3.0), S_HAKAMA)
    shoe = rk.octa(m, "R-Tabi", (-0.375, 0.75, 0), (15.25, 8, 21.25), tabi, 0.75)
    sole = box(m, "R-Semelle", (-0.5, -3.5, 0), (15.75, 1.5, 22), keep(rk.fine_leather((14, 12, 14), None)))
    return m, rk.legs_chain(m, waist, thigh + holster, calf, shoe + [sole])


# ---------------------------------------------------------------------------------------------------------------------
# Tenues
# ---------------------------------------------------------------------------------------------------------------------
HELM = {"Head": HIDE_HELMET}


def spec(prefix, sound, build, pieces):
    return {"prefix": prefix, "quality": "Divin", "level": 800, "durability": 800, "sound": sound, "hide": HELM, "build": build, "pieces": pieces}


SETS = {
    "Guerrier": spec("Armor_Divin_Guerrier_", "ISS_Armor_Heavy",
                     {"Head": build_guerrier_head, "Chest": build_guerrier_chest, "Hands": build_guerrier_hands, "Legs": build_guerrier_legs}, {
        "Head": ("Heaume du Séraphin", "Armure divine du Guerrier. Un grand heaume d'argent poli au cimier d'or, ailé de plumes, dont la fente brille d'une lumière céleste sous une auréole solaire.", 70, 10, 0.15),
        "Chest": ("Cuirasse du Séraphin", "Armure divine du Guerrier. Un harnois d'argent frappé d'un soleil d'or, aux frises sur émail lapis, et deux grandes ailes de séraphin déployées dans le dos.", 120, 16, 0.28),
        "Hands": ("Gantelets du Séraphin", "Armure divine du Guerrier. Des gantelets d'argent poli rehaussés d'or, un joyau de lumière au poignet.", 45, 6, 0.12),
        "Legs": ("Grèves du Séraphin", "Armure divine du Guerrier. Des cuissards cannelés et des grèves d'argent bordées d'or, sur une cotte de mailles claire.", 85, 12, 0.22),
    }),
    "Mage": spec("Armor_Divin_Mage_", "ISS_Armor_Cloth",
                 {"Head": build_mage_head, "Chest": build_mage_chest, "Hands": build_mage_hands, "Legs": build_mage_legs}, {
        "Head": ("Chapeau de l'Astre", "Armure divine du Mage. Un grand chapeau d'archimage semé d'étoiles, ceint de runes, qu'entoure une orbite de lumière où gravitent trois cristaux.", 30, 60, 0.09),
        "Chest": ("Robe de l'Astre", "Armure divine du Mage. Une robe de nuit ouverte sur une sous-robe d'ivoire, un haut col, des manches en cloche, un astrolabe de lumière, une longue cape aux constellations et un grimoire enchaîné à la hanche.", 55, 110, 0.17),
        "Hands": ("Bracelets de l'Astre", "Armure divine du Mage. Des bracelets d'or gravés de runes lumineuses, des gants de nuit et une bague de cristal.", 20, 40, 0.07),
        "Legs": ("Chausses de l'Astre", "Armure divine du Mage. Des chausses de nuit et des bottes souples à la pointe recourbée, aux revers runiques.", 40, 80, 0.13),
    }),
    "Archer": spec("Armor_Divin_Archer_", "ISS_Armor_Leather",
                   {"Head": build_archer_head, "Chest": build_archer_chest, "Hands": build_archer_hands, "Legs": build_archer_legs}, {
        "Head": ("Capuche du Cerf Céleste", "Armure divine de l'Archer. Une capuche profonde de forêt d'où s'élèvent des bois de cerf d'argent aux pointes de jade.", 45, 25, 0.11),
        "Chest": ("Pourpoint du Cerf Céleste", "Armure divine de l'Archer. Un pourpoint de cuir lacé, des spalières de feuilles d'argent, une cape de forêt brodée du cerf et un grand carquois aux flèches empennées de jade.", 80, 40, 0.21),
        "Hands": ("Brassards du Cerf Céleste", "Armure divine de l'Archer. Un long brassard d'arc au bras gauche, plaqué d'argent runique, et un gant d'archer à la main droite.", 30, 15, 0.09),
        "Legs": ("Bottes du Cerf Céleste", "Armure divine de l'Archer. Des chausses de cuir, de hautes bottes à revers et des jambières de feuilles d'argent.", 60, 30, 0.16),
    }),
    "Assassin": spec("Armor_Divin_Assassin_", "ISS_Armor_Leather",
                     {"Head": build_assassin_head, "Chest": build_assassin_chest, "Hands": build_assassin_hands, "Legs": build_assassin_legs}, {
        "Head": ("Voile d'Ombre de la Lune Noire", "Armure divine de l'Assassin. Aucun casque : une marque d'encre noire au front, et la tête noyée dans une fumée d'ombre qui tourbillonne autour d'elle et la traverse.", 40, 30, 0.10),
        "Chest": ("Dō de la Lune Noire", "Armure divine de l'Assassin. Un kimono noir aux manches larges et aux cols superposés, une cuirasse lamellaire de laque noire lacée de soie rouge sang, des sode, des kusazuri, un obi au tantō et un ninjatō dans le dos.", 70, 50, 0.19),
        "Hands": ("Kote de la Lune Noire", "Armure divine de l'Assassin. Des manches de mailles à attelles laquées et des tekko ; au poignet gauche, une lame cachée au fil de lumière spectrale.", 28, 18, 0.08),
        "Legs": ("Hakama de la Lune Noire", "Armure divine de l'Assassin. Un ample hakama de soie noire rayée, aux grands plis en relief, évasé jusqu'aux chevilles, un étui à kunai et des tabi.", 52, 36, 0.15),
    }),
}

SETS["Assassin"]["hide"] = {"Head": ["HeadAccessory"]}                    # casque invisible : cheveux et oreilles restent

if __name__ == "__main__":
    only = sys.argv[3:]                                                           # ex. : ... Assassin  (une seule tenue)
    chosen = {k: v for k, v in SETS.items() if not only or k in only}
    reports = write_sets(RES, PREVIEW, MODEL_DIR, "Divines", chosen, "divin", family_tag="Divine",
                         skip_in_icon=("Cape_", "Demi_Cape", "L-Aile", "R-Aile", "Halo", "Echarpe_", "Pans_Dos", "Orbite", "Cristal_", "Carquois", "Fleche_", "Empenne_",
                                       "R-Pan_Avant", "L-Pan_Avant", "Rabat", "Queue_", "Obi_Pan", "Ninjato", "Volute_", "Nappe_"),
                         preview_fit=5.2, closeup_fit=9.5)
    if "Assassin" in chosen:
        smoke_icon(os.path.join(RES, "Common", "Icons", "ItemsGenerated", "Armor_Divin_Assassin_Head.png"))
    for set_name, nodes in reports.items():
        found = coplanar_report(nodes)
        print("%s : %d paires coplanaires" % (set_name, len(found)))
        for line in found[:40]:
            print("   ", line)
