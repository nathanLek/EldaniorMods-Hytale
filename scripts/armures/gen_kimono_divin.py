"""Kimono divin : Tenue de l'Eclipse, noir et or, qualite Divin, niveau 800.

Idee : le soleil noir d'une eclipse, cerne de sa couronne d'or. Le kimono suit le kurotomesode, le kimono de ceremonie noir :
soie noire damassee, cinq blasons (kamon) d'eclipse, et tout le decor d'or reserve au bas des pans et des manches (vagues
seigaiha, nuages kumo, paillettes d'or kirikane).

  Tete    Kasa de l'Eclipse       cone du kasa de Sakura en laque noire : rayons d'or partant du sommet, paillettes, kamon
                                  devant, bord d'or, pampilles d'or (yoraku) au bord, cordon tresse noir et or ; la grande
                                  aureole d'eclipse (kohai) se dresse juste derriere le bord
  Torse   Kimono de l'Eclipse     soie noire damassee, kamon d'eclipse (poitrine, dos, manches), cols superposes noir et or,
                                  epaules rigides de ceremonie, manches aux formes de Sakura (sode, grand tamoto, ourlet de
                                  brocart, juban d'or a l'ouverture), brodees au bas,
                                  obi de brocart d'or (kinran) au medaillon, gros noeud taiko au dos, etole celeste (hagoromo)
                                  qui flotte en boucle derriere le dos, pans du kimono aux vagues et nuages d'or
  Mains   Tekko de l'Eclipse      gants de soie noire, manchettes d'or gravees, plaques de laque noire au soleil noir, bague
  Jambes  Hakama de l'Eclipse     formes du hakama de Sakura (trois etages evases) : soie noire aux plis lisérés d'or,
                                  paillettes au genou, vagues d'or et ourlet de brocart en bas, tabi noirs, zori laques

Reprend les formes de gen_runkandel (4 px par unite) et les toiles de gen_divines_classes (importes sans rien ecrire).
Ecrit dans la famille Kimono : modeles sous Items/Armors/Kimono/Divin, items sous Armor/Kimono, IDs Armor_Kimono_Divin_*.

Usage : python3 scripts/armures/gen_kimono_divin.py <src/main/resources> [dossier_apercu]
"""
import math
import random

import gen_runkandel as rk
import gen_divines_classes as gd
from armor_kit import (ID_Q, border, box, canvas, disc, finish, mix, mirror, noise, put, q_axis, qmul, qrot, quad, rect, rod, stroke, sym,
                       coplanar_report, write_sets)
import blason
import familles

D = rk.D
RES, PREVIEW = rk.RES, rk.PREVIEW
MODEL_DIR = "Items/Armors/Kimono"
GOLD, GOLD_DARK, GOLD_LIGHT = rk.GOLD_MID, rk.GOLD_SHADE, rk.GOLD_BRIGHT
KURO, KURO_DARK, KURO_LIGHT = (22, 20, 24), (4, 4, 6), (66, 62, 72)
SUN_BLACK = (10, 9, 12)
keep = gd.keep


# ---------------------------------------------------------------------------------------------------------------------
# Motifs
# ---------------------------------------------------------------------------------------------------------------------
def eclipse_art(px, rays=16):
    """Soleil noir : disque noir, anneau de lumiere d'or, couronne de rayons en flammes alternees."""
    cv = blason.Canvas(64, -1, 1, -1, 1)
    cv.fill(familles.starry(rays, 0.98, 0.6, cy=0.0), GOLD, GOLD_DARK, 0.02)
    inner = [(p[0] * math.cos(math.pi / rays) - p[1] * math.sin(math.pi / rays), p[0] * math.sin(math.pi / rays) + p[1] * math.cos(math.pi / rays))
             for p in familles.starry(rays, 0.8, 0.58, cy=0.0)]
    cv.fill(inner, GOLD_LIGHT)
    cv.fill(familles.regular(48, 0.62, cy=0.0), (255, 244, 200))
    cv.fill(familles.regular(48, 0.56, cy=0.0), GOLD)
    cv.fill(familles.regular(48, 0.5, cy=0.0), SUN_BLACK)
    cv.fill(familles.regular(48, 0.44, cy=0.0), (18, 16, 22))
    return blason.shrink(cv.img, px, px)


def kamon(px):
    """Blason brode : eclipse dans un cercle d'or (mon)."""
    cv = blason.Canvas(64, -1, 1, -1, 1)
    cv.fill(familles.regular(48, 0.98, cy=0.0), GOLD)
    cv.fill(familles.regular(48, 0.88, cy=0.0), KURO)
    cv.fill(familles.starry(12, 0.8, 0.5, cy=0.0), GOLD)
    cv.fill(familles.regular(48, 0.48, cy=0.0), GOLD_LIGHT)
    cv.fill(familles.regular(48, 0.4, cy=0.0), SUN_BLACK)
    return rk.embroider(blason.shrink(cv.img, px, px))


def seigaiha(x, y, R):
    """Vagues seigaiha : arcs concentriques en ecailles. Rend (anneau, distance au trait) ou None."""
    j0 = int(math.floor(y / (R / 2.0)))
    for j in range(j0 + 2, j0 - 1, -1):                                      # les rangs du bas recouvrent ceux du haut
        cy = j * R / 2.0
        if y > cy:
            continue
        off = R if j % 2 else 0.0
        cx = round((x - off) / (2 * R)) * 2 * R + off
        d = math.hypot(x - cx, y - cy)
        if d <= R:
            step = R / 4.0
            return int(d / step), (d % step)
    return None


def cloud(img, cx, cy, s, fill, line):
    """Nuage kumo : lobes ronds sur une base plate, trait d'or, volute dans le grand lobe."""
    lobes = [(-1.25, 0.05, 0.55), (-0.35, -0.3, 0.75), (0.65, -0.12, 0.62), (1.45, 0.12, 0.42)]
    h, w = len(img), len(img[0])
    x0, x1, y0, y1 = int(cx - 2.1 * s), int(cx + 2.1 * s), int(cy - 1.2 * s), int(cy + 0.75 * s)

    def inside(x, y, grow=0.0):
        if cx - 1.8 * s - grow <= x <= cx + 1.9 * s + grow and cy - grow <= y <= cy + 0.42 * s + grow:
            return True
        return any(math.hypot(x - (cx + a * s), y - (cy + b * s)) <= r * s + grow for a, b, r in lobes)

    for y in range(max(0, y0), min(h, y1)):
        for x in range(max(0, x0), min(w, x1)):
            if inside(x + 0.5, y + 0.5):
                img[y][x][:3] = list(fill)
            elif inside(x + 0.5, y + 0.5, 1.6):
                img[y][x][:3] = list(line)
    a, b, r = lobes[1]
    pts = [(cx + a * s + r * s * 0.55 * (1 - t * 0.7) * math.cos(t * 5.5 + 1), cy + b * s + r * s * 0.55 * (1 - t * 0.7) * math.sin(t * 5.5 + 1)) for t in [i / 30.0 for i in range(31)]]
    stroke(img, pts, line, 0.5)


# ---------------------------------------------------------------------------------------------------------------------
# Toiles
# ---------------------------------------------------------------------------------------------------------------------
SILK_BASE = gd.weave(KURO, KURO_DARK, KURO_LIGHT)


def kuro_silk(face, w, h, seed):
    """Soie noire : damas ton sur ton (rinzu), lustre satine doux."""
    img = SILK_BASE(face, w, h, seed)
    for y in range(h):
        for x in range(w):
            if rk.damask_on(x, y, 40):
                img[y][x][:3] = [v * 1.35 + 3 for v in img[y][x][:3]]
    return img


def kirikane(img, top, rng, per=260):
    """Paillettes et fils d'or coupes (kirikane) semes de top (0-1) jusqu'en bas."""
    h, w = len(img), len(img[0])
    y_top = int(h * top)
    for _ in range((h - y_top) * w // per):
        x, y = rng.uniform(0, w), rng.uniform(y_top, h)
        if rng.random() < 0.35:
            stroke(img, [(x, y), (x + rng.uniform(2, 5), y - rng.uniform(0, 2))], GOLD, 0.45)
        else:
            put(img, x, y, mix(GOLD, GOLD_LIGHT, rng.random()))


def hem_design(img, top, seed, waves=True, clouds=True):
    """Decor d'or du bas (kurotomesode) : paillettes d'or qui montent, nuages kumo, vagues seigaiha au ras de l'ourlet."""
    h, w = len(img), len(img[0])
    rng = random.Random(seed)
    y_top = int(h * top)
    kirikane(img, top, rng)
    band_y = y_top + (h - y_top) * 0.35
    for k in range(-1, 4) if clouds else ():
        s = rng.uniform(5.0, 7.0)
        cloud(img, k * w / 2.6 + rng.uniform(-4, 4) + (seed % 7), band_y + rng.uniform(-6, 6), s, mix(KURO, GOLD_DARK, 0.3), GOLD)
    if waves:
        wy = int(h - (h - y_top) * 0.38)
        R = 12.0
        for y in range(wy, h):
            for x in range(w):
                r = seigaiha(x, y - wy, R)
                if r is None:
                    continue
                ring, dist = r
                if dist < 1.1:
                    img[y][x][:3] = list(GOLD_LIGHT if ring == 0 else GOLD)
                elif ring >= 3:
                    img[y][x][:3] = list(mix(KURO, GOLD_DARK, 0.25))
        rect(img, 0, wy - 2, w, wy - 1, GOLD_DARK)
    rect(img, 0, h - 3, w, h, GOLD)
    rect(img, 0, h - 4, w, h - 3, GOLD_DARK)


def kimono_body(top=0.55, waves=True, kamon_face=None, kamon_y=0.3, kamon_px=0):
    """Toile du kimono : soie noire, decor d'or a partir de top (0-1), blason brode sur une face."""
    def paint(face, w, h, seed):
        img = kuro_silk(face, w, h, seed)
        if face not in ("top", "bottom"):
            if top < 1.0:
                hem_design(img, top, seed, waves)
            if face == kamon_face and kamon_px:
                art = kamon(kamon_px)
                rk.stamp(img, art, (w - kamon_px) // 2, int(h * kamon_y))
        elif face == "bottom":
            img = canvas(w, h, GOLD)
            img = finish(img, face, edges=False)
        return img
    return keep(paint)


def kimono_torso(face, w, h, seed):
    """Torse : V de l'encolure (sous-cols or et or sombre), deux kamon de poitrine devant, grand kamon dans le dos."""
    img = kuro_silk(face, w, h, seed)
    if face == "front":
        c = w / 2.0
        for y in range(h):
            Y = 12.0 - (y + 0.5) / D
            if Y <= 1.0:
                continue
            half = (Y - 1.0) * 0.62 * D
            for x in range(w):
                t = half - abs(x + 0.5 - c)
                if t > 0:
                    img[y][x][:3] = list(GOLD if t < 1.5 * D else GOLD_DARK if t < 3 * D else (36, 26, 14))
        art = kamon(int(4.5 * D))
        for sx in (-1, 1):
            rk.stamp(img, art, int(c + sx * 9.5 * D - len(art) / 2.0), int(2.2 * D))
    elif face == "back":
        art = kamon(int(8 * D))
        rk.stamp(img, art, (w - len(art)) // 2, int(2.0 * D))
    return img


def kinran(face, w, h, seed):
    """Brocart d'or (kinran) : fond d'or tisse, treillis de losanges noirs, petits soleils noirs aux croisements."""
    img = canvas(w, h, GOLD)
    for y in range(h):
        for x in range(w):
            k = (1.06 if (x + y) % 2 == 0 else 0.94) * (0.92 + 0.12 * rk.fbm(x * 0.08 + seed, y * 0.08))
            c = mix(GOLD_DARK, GOLD, 0.55 + 0.45 * math.cos((x / max(1.0, w) - 0.35) * math.pi))
            d = abs(x % 14 - 7) + abs(y % 14 - 7)
            if d in (6, 7):
                c = (30, 24, 20)
            elif d < 2:
                c = SUN_BLACK
            img[y][x][:3] = [v * k for v in c]
    if face not in ("top", "bottom"):
        rect(img, 0, 0, w, 2, GOLD_LIGHT)
        rect(img, 0, h - 2, w, h, GOLD_LIGHT)
    return finish(img, face, edges=False)


def gold_cord(face, w, h, seed):
    """Obijime : cordon tresse noir et or."""
    img = canvas(w, h, GOLD)
    for y in range(h):
        for x in range(w):
            p = (x + 2 * y) % 8
            img[y][x][:3] = list(GOLD_LIGHT if p < 2 else GOLD if p < 4 else (40, 32, 22) if p < 7 else SUN_BLACK)
    return finish(img, face, edges=False)


def stole_image(L_u, W_u, seed):
    """Etole celeste (hagoromo) : gaze noire aux bords d'or, nuages d'or, bords ajoures."""
    w, h = int(L_u * D), int(W_u * D)
    img = SILK_BASE("front", w, h, seed)
    for x in range(-10, w + 10, 26):
        cloud(img, x + (seed * 7) % 13, h * 0.55, 3.2, mix(KURO, GOLD_DARK, 0.3), GOLD)
    rect(img, 0, 0, w, 2, GOLD)
    rect(img, 0, h - 2, w, h, GOLD)
    rect(img, 0, 2, w, 3, GOLD_DARK)
    rect(img, 0, h - 3, w, h - 2, GOLD_DARK)
    return img


S_SILK = keep(kuro_silk)
S_GOLD = rk.steel(GOLD, GOLD_DARK, GOLD_LIGHT, sheen=0.32)
S_GOLD_ENGRAVED = rk.steel(GOLD, GOLD_DARK, GOLD_LIGHT, gold=GOLD_LIGHT, ground=SUN_BLACK, sheen=0.32, bands=((0.3, 10),))
S_LACQUER = gd.urushi(edge=GOLD)
S_LACQUER_PLAIN = gd.urushi()
S_SLEEVE_TOP = kimono_body(top=1.0, kamon_face="left", kamon_y=0.25, kamon_px=int(4.5 * D))
S_TAMOTO = kimono_body(top=0.45)
S_SUSO = kimono_body(top=0.42)
S_HIPS = kimono_body(top=1.0)
S_LAPEL_BLACK = gd.weave(KURO, KURO_DARK, KURO_LIGHT, hem=GOLD)
S_LAPEL_GOLD = rk.steel(GOLD, GOLD_DARK, GOLD_LIGHT, sheen=0.25)
S_KINRAN = keep(kinran)
S_CORD = keep(gold_cord)
S_TABI = gd.weave((26, 24, 28), (6, 6, 8), (70, 66, 76))


def hakama_divin(tier=None):
    """Hakama de soie noire aux plis lisérés d'un fil d'or. tier : None, "genou" (paillettes d'or), "ourlet" (vagues d'or)."""
    def paint(face, w, h, seed):
        img = kuro_silk(face, w, h, seed)
        if face in ("top", "bottom"):
            return img
        P = 20
        for y in range(h):
            for x in range(w):
                t = (x % P) / float(P)
                k = 0.66 + 0.5 * t * t                                           # le pli s'eclaire vers son arete, creux sombre
                img[y][x][:3] = [v * k for v in img[y][x][:3]]
                if x % P == 0:
                    img[y][x][:3] = list(GOLD_DARK)
        if tier == "genou":
            kirikane(img, 0.35, random.Random(seed), 180)
        elif tier == "ourlet":
            hem_design(img, 0.2, seed, True, clouds=False)
        return img
    return keep(paint)


def sleeve_mouth(paint):
    """Ouverture de la manche : doublure d'or au bord, interieur sombre."""
    def wrapped(face, w, h, seed):
        if face == "bottom":
            img = canvas(w, h, (16, 12, 10))
            for i, c in enumerate((GOLD_DARK, GOLD, GOLD, GOLD_LIGHT)):
                border(img, i, c)
            return finish(img, face, 1.3, edges=False)
        return paint(face, w, h, seed)
    return keep(wrapped)


S_HAKAMA = hakama_divin()
S_HAKAMA_GENOU = hakama_divin("genou")
S_HAKAMA_HEM = hakama_divin("ourlet")
S_TAMOTO_OPEN = sleeve_mouth(S_TAMOTO)
S_JUBAN = gd.weave(GOLD, GOLD_DARK, GOLD_LIGHT)


# ---------------------------------------------------------------------------------------------------------------------
# Formes
# ---------------------------------------------------------------------------------------------------------------------
def quat_from_basis(xa, ya, za):
    """Quaternion d'une base orthonormee (colonnes x, y, z)."""
    m00, m01, m02 = xa[0], ya[0], za[0]
    m10, m11, m12 = xa[1], ya[1], za[1]
    m20, m21, m22 = xa[2], ya[2], za[2]
    tr = m00 + m11 + m22
    if tr > 0:
        s = math.sqrt(tr + 1.0) * 2
        q = ((m21 - m12) / s, (m02 - m20) / s, (m10 - m01) / s, 0.25 * s)
    elif m00 > m11 and m00 > m22:
        s = math.sqrt(1.0 + m00 - m11 - m22) * 2
        q = (0.25 * s, (m01 + m10) / s, (m02 + m20) / s, (m21 - m12) / s)
    elif m11 > m22:
        s = math.sqrt(1.0 + m11 - m00 - m22) * 2
        q = ((m01 + m10) / s, 0.25 * s, (m12 + m21) / s, (m02 - m20) / s)
    else:
        s = math.sqrt(1.0 + m22 - m00 - m11) * 2
        q = ((m02 + m20) / s, (m12 + m21) / s, 0.25 * s, (m10 - m01) / s)
    return {"x": q[0], "y": q[1], "z": q[2], "w": q[3]}


def norm(v):
    n = math.sqrt(sum(c * c for c in v)) or 1.0
    return tuple(c / n for c in v)


def cross(a, b):
    return (a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0])


def smooth_path(pts, steps):
    """Courbe de Catmull-Rom en 3D passant par les points."""
    ext = [pts[0]] + list(pts) + [pts[-1]]
    out = []
    for i in range(1, len(ext) - 2):
        p0, p1, p2, p3 = ext[i - 1], ext[i], ext[i + 1], ext[i + 2]
        for k in range(steps):
            t = k / float(steps)
            out.append(tuple(0.5 * (2 * p1[j] + (p2[j] - p0[j]) * t + (2 * p0[j] - 5 * p1[j] + 4 * p2[j] - p3[j]) * t * t
                                    + (3 * p1[j] - p0[j] - 3 * p2[j] + p3[j]) * t ** 3) for j in range(3)))
    out.append(pts[-1])
    return out


def ribbon(m, name, pts, width, seed):
    """Ruban de quads le long d'une ligne : chaque pan est tourne vers l'exterieur (loin de l'axe du corps)."""
    nodes = []
    for i, (a, b) in enumerate(zip(pts, pts[1:])):
        xa = norm(tuple(b[k] - a[k] for k in range(3)))
        mid = tuple((a[k] + b[k]) / 2.0 for k in range(3))
        out = norm((mid[0], 0.0, mid[2]))
        za = norm(tuple(out[k] - xa[k] * sum(out[j] * xa[j] for j in range(3)) for k in range(3)))
        ya = cross(za, xa)
        L = math.sqrt(sum((b[k] - a[k]) ** 2 for k in range(3))) + 0.5
        nodes.append(quad(m, "%s_%d" % (name, i), mid, stole_image(L, width, seed + i), quat_from_basis(xa, ya, za)))
    return nodes


def kasa_panel(k, W, H, light):
    """Pan du kasa : laque noire lustree ; du sommet partent les rayons d'or du soleil noir, longs et courts en alternance ;
    paillettes d'or vers le bord, kamon sur le pan avant, bord d'or."""
    img = canvas(W, H, KURO)
    for y in range(H):
        t = y / float(H)
        s = max(0.0, math.sin(t * math.pi * 1.1)) ** 3
        img[y] = [[*mix((8, 7, 10), (74, 68, 82), s * 0.55 * light), 255] for _ in range(W)]
    rng = random.Random(k * 13 + 7)
    for _ in range(W * H // 170):                                             # paillettes plus denses vers le bord
        y = H * (1 - rng.random() ** 2 * 0.6) - 12
        x = rng.uniform(0, W)
        if abs(x - W / 2.0) < W / 2.0 * y / H:
            put(img, x, y, mix(GOLD_DARK, GOLD_LIGHT, rng.random()), 0.9)
    reach = 0.74 if k % 2 == 0 else 0.46                                    # rayon : de l'anneau du sommet vers le bord
    y0, y1 = int(H * 0.13), int(H * reach)
    for y in range(y0, y1):
        v = (y - y0) / float(y1 - y0)
        half = (0.6 + 2.6 * math.sin(v * math.pi) ** 0.7) * (1.0 if k % 2 == 0 else 0.7)
        sway = 1.2 * math.sin(v * 6.0 + k)
        for x in range(W):
            d = abs(x + 0.5 - W / 2.0 - sway)
            if d <= half:
                img[y][x][:3] = list(mix(GOLD_LIGHT, GOLD, d / max(0.5, half)) if d < half - 0.8 else GOLD_DARK)
    band_y = int(H * 0.11)
    rect(img, 0, band_y, W, band_y + 3, GOLD)                                 # anneau d'or autour du sommet
    rect(img, 0, band_y + 3, W, band_y + 4, GOLD_DARK)
    if k == 0:                                                                # kamon d'eclipse sur le pan avant
        art = kamon(int(6.5 * D))
        rk.stamp(img, art, (W - len(art)) // 2, int(H * 0.79) - len(art) // 2)
    rect(img, 0, H - 17, W, H - 16, GOLD_DARK)                                # filet, puis bord d'or
    rect(img, 0, H - 10, W, H - 4, GOLD)
    rect(img, 0, H - 4, W, H, GOLD_DARK)
    rect(img, 0, H - 10, W, H - 9, GOLD_LIGHT)
    return img


def cone_panels(m, count, apothem, base_y, apex_y, paint_panel):
    """Cone a count pans (repris du kasa de Sakura) : quads triangulaires decoupes en alpha, qui se recouvrent sans jour."""
    rise = apex_y - base_y
    slant = math.hypot(apothem, rise)
    w_u = 2 * apothem * math.tan(math.pi / count) + 1.5
    W = int(math.ceil(w_u * D))
    W += W % 2
    H = int(round(slant * D))
    tilt = math.degrees(math.atan2(apothem, rise))
    nodes = []
    for k in range(count):
        yaw_deg = k * 360.0 / count
        light = 0.86 + 0.2 * math.cos(math.radians(yaw_deg - 30))
        qy = q_axis((0, 1, 0), yaw_deg)
        pos = qrot(qy, (0, (base_y + apex_y) / 2.0, apothem / 2.0))
        img = paint_panel(k, W, H, light)
        for y in range(H):
            half = W / 2.0 * (y + 0.5) / H + 1.5 * D / 2.0
            for x in range(W):
                if abs(x - W / 2.0 + 0.5) > half:
                    img[y][x][3] = 0
        nodes.append(quad(m, "Cone_%d" % k, pos, img, qmul(qy, q_axis((1, 0, 0), -tilt))))
    return nodes


# ---------------------------------------------------------------------------------------------------------------------
# Pieces
# ---------------------------------------------------------------------------------------------------------------------
def build_head():
    """Kasa de l'Eclipse : le cone du kasa de Sakura en laque noire aux rayons d'or ; l'aureole d'eclipse se dresse derriere."""
    m = rk.new_model("Head")
    kids = cone_panels(m, 16, 31.0, 9.5, 27.5, kasa_panel)                   # coins de la tete (r 21, y 14) sous le cone
    kids.append(box(m, "Couronne", (0, 13.5, 0), (24, 2, 24), S_KINRAN))
    kids.append(box(m, "Fleuron", (0, 27.5, 0), (5, 2, 5), S_GOLD_ENGRAVED))
    kids.append(box(m, "Fleuron_Soleil", (0, 29.25, 0), (3.5, 1.5, 3.5), S_LACQUER_PLAIN))
    kids.append(box(m, "Fleuron_Haut", (0, 30.5, 0), (2, 1.5, 2), S_GOLD))
    kids.append(rod(m, "R-Cordon", (-15.7, 13, 6), (-15.7, -14.8, 8), 1, S_CORD))
    kids.append(rod(m, "L-Cordon", (15.7, 13, 6), (15.7, -14.8, 8), 1, S_CORD))
    kids.append(box(m, "Cordon_Menton", (0, -14.8, 8), (31.5, 1, 1), S_CORD))
    for tag, side in (("R", -1), ("L", 1)):                                   # pompons d'or au bout du cordon
        kids.append(box(m, "%s-Pompon_Noeud" % tag, (side * 15.7, -16.0, 8), (2, 1.5, 2), S_LACQUER_PLAIN))
        kids.append(box(m, "%s-Pompon" % tag, (side * 15.7, -19.0, 8), (2.5, 4.5, 2.5), S_GOLD))
    for i, deg in enumerate((60, 120, -60, -120)):                            # yoraku : pampilles d'or pendues au bord
        a = math.radians(deg)
        top = (30.0 * math.sin(a), 9.8, 30.0 * math.cos(a))
        bottom = (top[0], 2.0, top[2])
        kids.append(rod(m, "Yoraku_%d" % i, top, bottom, 0.5, S_GOLD))
        for b in range(2):
            p = (top[0], 7.0 - 3.25 * b, top[2])
            kids.append(box(m, "Yoraku_Perle_%d_%d" % (i, b), p, (1.25, 1.25, 1.25), S_GOLD))
        kids.append(box(m, "Yoraku_Goutte_%d" % i, (top[0], 1.0, top[2]), (1.75, 2.0, 1.75), S_LACQUER_PLAIN))
    kids.append(gd.lit(quad(m, "Kohai", (0, 14.0, -33.0), eclipse_art(int(40 * D), rays=24))))
    return m, [rk.piece(m, "Head", (0, 0, 0), kids)]


def build_chest():
    m = rk.new_model("Chest")
    sode = box(m, "R-Sode", (-1, 0.5, -2), (14, 23, 22), S_SLEEVE_TOP)              # formes des manches de Sakura
    tamoto = box(m, "R-Tamoto", (-1, -1, -5.75), (15, 19, 30), S_TAMOTO_OPEN)
    fuki_manche = box(m, "R-Fuki_Manche", (-1, -10.5, -5.75), (16, 2, 31), S_KINRAN)
    juban = box(m, "R-Juban", (-1, -11.75, 0), (14, 3, 16), S_JUBAN)
    arm = rk.arm_piece(m, [sode], [tamoto, fuki_manche, juban])
    suso = box(m, "R-Suso", (-1, 0.5, 0), (17, 17, 23), S_SUSO)
    fuki = box(m, "R-Fuki", (-1.6, -8, 0), (18, 2, 24), S_KINRAN)
    thigh = rk.thigh_piece(m, [suso, fuki])
    pelvis = rk.octa(m, "Hanches", (0, -0.5, 0), (31, 13, 24), S_HIPS, 3.5)
    chest = rk.octa(m, "Kimono_Torse", (0, 0, 0), (31.5, 24.5, 24), keep(kimono_torso), 3.75, corners=S_SILK)
    chest.append(gd.stick(m, "Col_Or_Dessous", (-7.0, 12.6, 12.3), (0.75, 4.0, 12.3), 2.0, 0.5, S_LAPEL_GOLD))
    chest.append(gd.stick(m, "Col_Or_Dessus", (7.0, 12.6, 12.45), (-1.75, 2.5, 12.45), 2.0, 0.75, S_LAPEL_GOLD))
    chest.append(gd.stick(m, "Col_Dessous", (-9.0, 12.75, 12.6), (2.0, 0.5, 12.6), 4.0, 0.6, S_LAPEL_BLACK))
    chest.append(gd.stick(m, "Col_Dessus", (9.0, 12.75, 12.95), (-6.0, -5.5, 12.95), 4.0, 1.0, S_LAPEL_BLACK))
    chest += rk.octa(m, "Col_Nuque", (0, 12.9, -2.5), (20, 2.25, 17), S_LAPEL_BLACK, 3)
    wing = box(m, "R-Kataginu", (-19.5, 12.5, -0.5), (15, 1.5, 25), S_LACQUER, q=q_axis((0, 0, 1), 12))
    wing_edge = box(m, "R-Kataginu_Bord", (-26.75, 10.95, -0.5), (1.5, 2.25, 25.75), S_GOLD, q=q_axis((0, 0, 1), 12))
    chest += sym(m, wing, wing_edge)
    arc = [(12, 11, -13.5), (19, 4, -17), (22, -5, -22), (18, -13, -27), (7, -17, -29), (0, -18, -29.5)]
    ctrl = [(-x, y, z) for x, y, z in arc] + [(x, y, z) for x, y, z in reversed(arc[:-1])]
    pts = smooth_path(ctrl, 3)
    chest += ribbon(m, "Hagoromo", pts, 5.5, 60)
    belly = rk.octa(m, "Ventre", (0, 1, 0), (29.5, 16, 23.5), S_SILK, 4.5)
    belly += rk.octa(m, "Obi", (0, -3.0, 0), (32.5, 9.5, 26.5), S_KINRAN, 4.75)
    belly += rk.octa(m, "Obiage", (0, 2.1, 0), (32.25, 1.25, 26.25), S_LAPEL_BLACK, 4.75)
    belly += rk.octa(m, "Obijime", (0, -3.0, 0), (33.25, 1.25, 27.25), S_CORD, 5)
    belly.append(box(m, "Obidome", (0, -3.0, 14.1), (5.5, 5.5, 1.0), S_GOLD))
    belly.append(gd.lit(quad(m, "Obidome_Soleil", (0, -3.0, 14.65), eclipse_art(int(5 * D)))))
    belly.append(box(m, "Taiko", (0, -1.0, -16.0), (22, 13, 4.5), S_KINRAN))
    belly.append(box(m, "Taiko_Noeud", (0, 3.0, -18.6), (9, 4.5, 1.5), S_KINRAN))
    belly.append(gd.hang(m, "Taiko_Tare", (0, -7.0, -16.5), (0, -16.0, -17.25), gd.panel_image(20, 9, S_KINRAN, 4)))
    return m, rk.body_chain(m, pelvis, belly, chest, arm, thigh)


def build_hands():
    m = rk.new_model("Hands")
    cuff = rk.octa(m, "R-Manchette", (0, -2.5, 0), (12, 9, 16), S_GOLD_ENGRAVED, 2.5)
    glove = rk.octa(m, "R-Gant", (0, -0.5, 0), (11, 13, 15), S_SILK, 0.75)
    tekko = box(m, "R-Tekko", (-6.0, 1.0, 0), (0.75, 8, 11), S_LACQUER)
    sun = gd.lit(quad(m, "R-Tekko_Soleil", (-6.45, 1.0, 0), eclipse_art(int(4 * D)), rk.yaw(-90)))
    ring = box(m, "R-Bague", (0, -3.0, 0), (11.5, 1.25, 15.5), S_GOLD)
    return m, rk.hands_chain(m, cuff, glove + [tekko, sun, ring])


def build_legs():
    """Hakama aux formes de Sakura : trois etages qui s'evasent vers le bas, ourlet de brocart d'or."""
    m = rk.new_model("Legs")
    waist = rk.octa(m, "Koshi", (0, 0, 0), (28.5, 10, 21.5), S_HAKAMA, 1.5)
    waist.append(box(m, "Koshi_Ita", (0, 1.5, -11.25), (12, 6, 1), S_LACQUER))
    upper = box(m, "R-Hakama_Haut", (-0.75, -1, 0), (16, 24, 20), S_HAKAMA)
    mid = box(m, "R-Hakama_Genou", (-1.3, 5, -0.5), (17.5, 13, 23.5), S_HAKAMA_GENOU)
    low = box(m, "R-Hakama_Bas", (-2.6, -6, -1), (20, 11, 28), S_HAKAMA_HEM)
    ring = box(m, "R-Hakama_Ourlet", (-3.0, -11.25, -1), (21, 1.5, 29), S_KINRAN)
    ankle = box(m, "R-Guetre", (0, -8.5, 0), (12, 5, 14), S_TABI)
    shoe = rk.octa(m, "R-Tabi", (-0.375, 0.75, 0), (15.25, 8, 21.25), S_TABI, 0.75)
    sole = box(m, "R-Zori", (-0.375, -3.6, 0), (15.5, 1.75, 22.5), S_LACQUER)
    toe = (-2.5, 4.6, 10.0)
    straps = [rod(m, "R-Hanao_A", toe, (-8.2, 1.5, 1.5), 1.0, S_CORD), rod(m, "R-Hanao_B", toe, (7.4, 1.5, 1.5), 1.0, S_CORD)]
    return m, rk.legs_chain(m, waist, [upper], [mid, low, ring, ankle], shoe + [sole] + straps)


LORE = "Tenue Ancestrale du Dieu-Dragon d'Or. Les anciens disaient qu'une éclipse est un dragon qui dévore le soleil : ce soleil noir est celui qu'avala le Dieu-Dragon d'Or."
STEP = "Pas dans les airs : un saut de plus en plein vol. Tenue complète : sauts illimités et +15 % de vitesse."
JUMP_PARTICLES = "Eldanior_Pas_Celeste"
REGEN_ALWAYS = [{"Id": "Stat", "Stat": "Health", "Amount": 0, "Comparison": "Gte"}]

SETS = {
    "Divin": {
        "prefix": "Armor_Kimono_Divin_", "quality": "Divin", "level": 800, "durability": 2000, "durability_loss": 0.25,
        "sound": "ISS_Armor_Cloth",
        "build": {"Head": build_head, "Chest": build_chest, "Hands": build_hands, "Legs": build_legs},
        # Totaux de la tenue : vie +600, mana +600, protection physique 75 %, projectiles 90 %, endurance +200, feu 40 %,
        # degats physiques +12 %, regeneration 0,5 vie/s et 1 mana/s, esquive a moitie prix, recul et chute -50 %, double saut.
        "pieces": {
            "Head": ("Kasa du Dieu-Dragon d'Or", LORE + " Un grand chapeau de laque noire aux rayons d'or ; derrière lui se lève l'auréole du soleil dévoré.", 120, 170, 0.16),
            "Chest": ("Kimono Ancestral du Dieu-Dragon d'Or", LORE + " Soie noire damassée frappée de cinq soleils noirs, obi de brocart d'or et étole céleste.", 210, 190, 0.26),
            "Hands": ("Tekkō du Dieu-Dragon d'Or", LORE + " Gants de soie noire, manchettes d'or gravées et plaques de laque au soleil noir.", 100, 110, 0.13),
            "Legs": ("Hakama du Dieu-Dragon d'Or", LORE + " " + STEP, 170, 130, 0.20),
        },
        "stats_extra": {"Head": {"Stamina": 40}, "Chest": {"Stamina": 60}, "Hands": {"Stamina": 40}, "Legs": {"Stamina": 60}},
        "resist_extra": {"Head": {"Projectile": 0.19, "Fire": 0.10}, "Chest": {"Projectile": 0.31, "Fire": 0.20},
                         "Hands": {"Projectile": 0.16}, "Legs": {"Projectile": 0.24, "Fire": 0.10, "Fall": 0.5}},
        "armor_extra": {
            "Head": {"Regenerating": {"Mana": [{"Interval": 1.0, "Amount": 1.0, "RegenType": "Additive", "Conditions": REGEN_ALWAYS}]}},
            "Chest": {"DamageEnhancement": {"Physical": [{"Amount": 0.06, "CalculationType": "Multiplicative"}]},
                      "Regenerating": {"Health": [{"Interval": 1.0, "Amount": 0.5, "RegenType": "Additive", "Conditions": REGEN_ALWAYS}]}},
            "Hands": {"DamageEnhancement": {"Physical": [{"Amount": 0.06, "CalculationType": "Multiplicative"}]}},
            "Legs": {"KnockbackResistances": {"Physical": 0.5, "Projectile": 0.5},
                     "InteractionModifiers": {"Dodge": {"Stamina": {"Amount": 0.5, "CalculationType": "Multiplicative"}}},
                     "MovementSettings": {"ExtraJumpCount": 1, "ExtraJumpParticleSystem": JUMP_PARTICLES}},
        },
    },
}


def write_awakened_legs(res):
    """Hakama eveille : meme objet que le hakama, mais sauts illimites. Le serveur (ArmorSetBonusSystem) l'echange avec le hakama
    tant que la tenue complete est portee, et le rend des qu'elle ne l'est plus : le nombre de sauts en l'air est lu par le client
    sur l'objet porte, on ne peut pas le changer par joueur autrement. Absent du menu creatif."""
    import json, os
    item_dir = os.path.join(res, "Server", "Item", "Items", "Armor", "Kimono")
    with open(os.path.join(item_dir, "Armor_Kimono_Divin_Legs.json"), encoding="utf-8") as f:
        item = json.load(f)
    item.pop("Categories", None)
    item["Armor"]["MovementSettings"] = {"ExtraJumpCount": 127, "ExtraJumpParticleSystem": JUMP_PARTICLES}
    with open(os.path.join(item_dir, "Armor_Kimono_Divin_Legs_Eveil.json"), "w", encoding="utf-8") as f:
        json.dump(item, f, indent=2, ensure_ascii=False)
        f.write("\n")


if __name__ == "__main__":
    reports = write_sets(RES, PREVIEW, MODEL_DIR, "Kimono", SETS, "kimono", family_tag="Cloth_Kimono",
                         skip_in_icon=("Kohai", "Hagoromo", "Taiko_Tare"), preview_fit=5.2, closeup_fit=9.5)
    write_awakened_legs(RES)
    for set_name, nodes in reports.items():
        found = coplanar_report(nodes)
        print("%s : %d paires coplanaires" % (set_name, len(found)))
        for line in found[:40]:
            print("   ", line)
