"""Armoiries des familles : dessin vectoriel en coordonnees normalisees, rendu sur-echantillonne puis reduit.

Runkandel : de gueules a la tete de dragon d'or arrachee, transpercee d'une epee d'argent, a la bordure d'or.
Rangs : Recrue = ecu seul ; Capitaine = ecu a la bordure gravee ; Patriarche = ecu couronne, aux ailes de dragon.

Un calque est une liste de lignes de [r, g, b, a].
"""
import math


def layer(w, h):
    return [[[0, 0, 0, 0] for _ in range(w)] for _ in range(h)]


def fill(img, pts, c):
    """Polygone plein, pair-impair. Les points sont en pixels."""
    h, w = len(img), len(img[0])
    y0, y1 = max(0, int(min(p[1] for p in pts))), min(h - 1, int(max(p[1] for p in pts)) + 1)
    for y in range(y0, y1 + 1):
        crossings = []
        for (ax, ay), (bx, by) in zip(pts, pts[1:] + pts[:1]):
            if (ay <= y + 0.5) != (by <= y + 0.5):
                crossings.append(ax + (y + 0.5 - ay) * (bx - ax) / (by - ay))
        crossings.sort()
        for i in range(0, len(crossings) - 1, 2):
            for x in range(max(0, int(math.ceil(crossings[i] - 0.5))), min(w - 1, int(crossings[i + 1] - 0.5)) + 1):
                img[y][x] = [c[0], c[1], c[2], 255]


def curve(points, steps=10):
    """Courbe de Catmull-Rom passant par les points (ouverte)."""
    pts = [points[0]] + list(points) + [points[-1]]
    out = []
    for i in range(1, len(pts) - 2):
        p0, p1, p2, p3 = pts[i - 1], pts[i], pts[i + 1], pts[i + 2]
        for k in range(steps):
            t = k / float(steps)
            out.append(tuple(0.5 * (2 * p1[j] + (p2[j] - p0[j]) * t + (2 * p0[j] - 5 * p1[j] + 4 * p2[j] - p3[j]) * t * t
                                    + (3 * p1[j] - p0[j] - 3 * p2[j] + p3[j]) * t * t * t) for j in (0, 1)))
    out.append(points[-1])
    return out


class Canvas:
    """Coordonnees : u vers la droite, v vers le bas ; (0, 0) est le centre de l'ecu, 1 sa demi-largeur."""

    def __init__(self, px_per_unit, u0, u1, v0, v1):
        self.k = px_per_unit
        self.u0, self.v0 = u0, v0
        self.w, self.h = int((u1 - u0) * px_per_unit), int((v1 - v0) * px_per_unit)
        self.img = layer(self.w, self.h)

    def px(self, pts):
        return [((u - self.u0) * self.k, (v - self.v0) * self.k) for u, v in pts]

    def fill(self, pts, c, outline=None, width=0.0):
        p = self.px(pts)
        if outline:
            r = width * self.k
            for i in range(8):
                a = i * math.pi / 4
                fill(self.img, [(x + math.cos(a) * r, y + math.sin(a) * r) for x, y in p], outline)
        fill(self.img, p, c)

    def line(self, pts, c, width):
        r = width * self.k
        for a, b in zip(self.px(pts), self.px(pts)[1:]):
            n = max(1, int(math.hypot(b[0] - a[0], b[1] - a[1]) / max(1.0, r * 0.5)))
            for i in range(n + 1):
                t = i / float(n)
                self.dot_px(a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t, r, c)

    def dot_px(self, cx, cy, r, c):
        for y in range(max(0, int(cy - r - 1)), min(self.h, int(cy + r + 2))):
            for x in range(max(0, int(cx - r - 1)), min(self.w, int(cx + r + 2))):
                if math.hypot(x + 0.5 - cx, y + 0.5 - cy) <= r:
                    self.img[y][x] = [c[0], c[1], c[2], 255]

    def dot(self, u, v, r, c):
        (x, y), = self.px([(u, v)])
        self.dot_px(x, y, r * self.k, c)

    def over(self, other):
        """Pose un calque de meme taille."""
        for y in range(self.h):
            for x in range(self.w):
                p = other.img[y][x]
                if p[3]:
                    self.img[y][x] = list(p)


def shrink(img, w, h):
    """Reduction par moyenne, ponderee par l'alpha."""
    H, W = len(img), len(img[0])
    out = []
    for y in range(h):
        y0, y1 = y * H // h, max(y * H // h + 1, (y + 1) * H // h)
        row = []
        for x in range(w):
            x0, x1 = x * W // w, max(x * W // w + 1, (x + 1) * W // w)
            total = [0.0, 0.0, 0.0, 0.0]
            for j in range(y0, y1):
                for i in range(x0, x1):
                    p = img[j][i]
                    a = p[3]
                    total[0] += p[0] * a
                    total[1] += p[1] * a
                    total[2] += p[2] * a
                    total[3] += a
            n = (y1 - y0) * (x1 - x0)
            if total[3] == 0:
                row.append([0, 0, 0, 0])
            else:
                row.append([total[0] / total[3], total[1] / total[3], total[2] / total[3], total[3] / n])
        out.append(row)
    return out


def mix(a, b, t):
    t = max(0.0, min(1.0, t))
    return tuple(a[i] + (b[i] - a[i]) * t for i in range(3))


def shield_outline(scale=1.0):
    """Ecu a pointe : haut legerement echancre, flancs droits, base en ogive."""
    right = [(1.0, -1.16), (1.0, 0.0), (0.97, 0.36), (0.84, 0.78), (0.58, 1.14), (0.27, 1.38), (0.0, 1.52)]
    right = curve(right, 8)
    top = [(0.0, -1.06), (0.5, -1.12), (1.0, -1.16)]
    pts = [(u * scale, v * scale) for u, v in curve(top, 6)[:-1] + right]
    left = [(-u, v) for u, v in reversed(pts)]
    return pts + left[1:-1]


DRAGON_HEAD = [
    (-0.86, -0.14), (-0.84, -0.25), (-0.72, -0.31), (-0.62, -0.28), (-0.44, -0.33), (-0.28, -0.45), (-0.14, -0.6), (0.06, -0.62), (0.24, -0.54),
    (0.38, -0.4), (0.5, -0.2), (0.58, 0.06), (0.6, 0.34), (0.54, 0.62), (0.47, 0.84),
    (0.36, 0.68), (0.27, 0.94), (0.14, 0.72), (0.03, 1.0), (-0.08, 0.72), (-0.18, 0.86), (-0.21, 0.6),
    (-0.17, 0.36), (-0.12, 0.2), (-0.14, 0.12), (-0.28, 0.27), (-0.5, 0.31), (-0.68, 0.27), (-0.78, 0.16),
    (-0.6, 0.15), (-0.42, 0.13), (-0.26, 0.03), (-0.2, -0.05), (-0.42, -0.04), (-0.66, -0.03), (-0.8, -0.05),
]
HORN_BIG = [(0.0, -0.58), (0.12, -0.82), (0.36, -0.98), (0.66, -1.04), (0.86, -0.98), (0.62, -0.92), (0.42, -0.8), (0.3, -0.6), (0.24, -0.5)]
HORN_SMALL = [(0.22, -0.52), (0.42, -0.68), (0.66, -0.72), (0.8, -0.66), (0.62, -0.62), (0.46, -0.52), (0.36, -0.4)]
FRILLS = [
    [(0.46, -0.28), (0.74, -0.34), (0.56, -0.12)],
    [(0.54, -0.08), (0.82, -0.08), (0.6, 0.1)],
    [(0.58, 0.14), (0.84, 0.2), (0.6, 0.32)],
    [(0.6, 0.36), (0.8, 0.46), (0.57, 0.52)],
]


def dragon_head(cv, gold, gold_light, gold_dark, line, eye, tooth):
    w = 0.028
    for frill in FRILLS:
        cv.fill(frill, gold_dark, line, w)
    cv.fill(HORN_SMALL, gold_dark, line, w)
    cv.fill(HORN_BIG, gold_light, line, w)
    for t in (0.2, 0.34, 0.48):                                                    # stries de la corne
        cv.line([(0.05 + t, -0.78 - t * 0.24), (0.11 + t, -0.68 - t * 0.2)], gold_dark, 0.008)
    for x in (-0.74, -0.6, -0.46, -0.32):                                           # crocs du haut
        cv.fill([(x, -0.06), (x + 0.075, -0.05), (x + 0.03, 0.09)], tooth, line, 0.012)
    for x in (-0.68, -0.54, -0.4):                                                  # crocs du bas
        cv.fill([(x, 0.17), (x + 0.07, 0.16), (x + 0.045, 0.03)], tooth, line, 0.012)
    head = Canvas(cv.k, cv.u0, cv.u0 + cv.w / float(cv.k), cv.v0, cv.v0 + cv.h / float(cv.k))
    head.fill(DRAGON_HEAD, gold, line, w)
    cv.over(head)
    # volume : lumiere sur le dessus du museau et du cou, ombre sous la machoire
    inner = Canvas(cv.k, cv.u0, cv.u0 + cv.w / float(cv.k), cv.v0, cv.v0 + cv.h / float(cv.k))
    inner.fill(DRAGON_HEAD, gold)
    for y in range(cv.h):
        for x in range(cv.w):
            p = inner.img[y][x]
            if p[3]:
                u, v = cv.u0 + x / float(cv.k), cv.v0 + y / float(cv.k)
                t = (v + 0.6) / 1.6 + 0.25 * (u + 0.2)
                c = mix(gold_light, gold, t * 2) if t < 0.5 else mix(gold, gold_dark, (t - 0.5) * 1.5)
                if u > -0.12 and v > -0.3:                                         # ecailles du cou
                    row = 0.11
                    j = int(math.floor((v + 2) / row))
                    ox = 0.065 if j % 2 else 0.0
                    du = ((u + ox) % 0.13) - 0.065
                    dv = ((v + 2) % row)
                    if abs(math.hypot(du * 1.1, dv * 0.95) - 0.075) < 0.012:
                        c = mix(c, gold_dark, 0.85)
                cv.img[y][x] = [c[0], c[1], c[2], 255]
    cv.line(curve([(-0.32, -0.27), (-0.14, -0.44), (0.1, -0.44)], 6), gold_dark, 0.016)   # arcade
    cv.line(curve([(-0.2, -0.04), (-0.08, 0.04), (0.04, 0.0)], 6), gold_dark, 0.016)      # commissure
    cv.line(curve([(-0.76, -0.2), (-0.62, -0.2), (-0.5, -0.16)], 6), gold_dark, 0.01)     # levre
    cv.fill([(-0.2, -0.3), (-0.02, -0.34), (0.03, -0.25), (-0.08, -0.2)], line)            # orbite
    cv.fill([(-0.16, -0.29), (-0.03, -0.31), (0.0, -0.26), (-0.09, -0.235)], eye)
    cv.dot(-0.06, -0.272, 0.016, line)
    cv.fill([(-0.76, -0.22), (-0.69, -0.24), (-0.67, -0.19)], line)                        # naseau


def sword(cv, u, top, tip, blade, blade_dark, hilt, hilt_dark, line, jewel):
    half = 0.085
    w = 0.024
    guard = top + 0.33
    cv.fill([(u - half, guard), (u + half, guard), (u + half * 0.86, tip - 0.3), (u, tip), (u - half * 0.86, tip - 0.3)], blade, line, w)
    cv.fill([(u, guard), (u + half, guard), (u + half * 0.86, tip - 0.3), (u, tip)], blade_dark)
    cv.line([(u, guard + 0.02), (u, tip - 0.46)], line, 0.009)                               # gorge
    cv.fill([(u - 0.036, top + 0.08), (u + 0.036, top + 0.08), (u + 0.036, guard), (u - 0.036, guard)], (84, 40, 28), line, w)
    for i in range(4):                                                                       # fusee filetee
        y = top + 0.11 + i * 0.05
        cv.line([(u - 0.036, y), (u + 0.036, y + 0.02)], hilt_dark, 0.006)
    q = [(u - 0.4, guard - 0.075), (u - 0.45, guard - 0.11), (u - 0.48, guard - 0.02), (u - 0.45, guard + 0.05), (u - 0.38, guard + 0.03), (u - 0.1, guard + 0.05),
         (u, guard + 0.09), (u + 0.1, guard + 0.05), (u + 0.38, guard + 0.03), (u + 0.45, guard + 0.05), (u + 0.48, guard - 0.02), (u + 0.45, guard - 0.11),
         (u + 0.4, guard - 0.075), (u + 0.1, guard - 0.06), (u, guard - 0.08), (u - 0.1, guard - 0.06)]
    cv.fill(q, hilt, line, w)
    cv.dot(u, guard - 0.005, 0.042, jewel)
    cv.dot(u, top + 0.03, 0.082, line)
    cv.dot(u, top + 0.03, 0.062, hilt)
    cv.dot(u - 0.015, top + 0.015, 0.022, (255, 244, 200))


def bat_wing(cv, side, membrane, membrane_dark, bone, line):
    """Aile de dragon derriere l'ecu : doigts rayonnants, membrane festonnee."""
    root = (side * 0.82, -0.62)
    wrist = (side * 1.42, -1.42)
    tips = [(side * 2.5, -1.12), (side * 2.62, -0.42), (side * 2.4, 0.3), (side * 1.95, 0.82), (side * 1.32, 1.08)]
    outline = [root, wrist, tips[0]]
    for a, b in zip(tips, tips[1:]):
        mid = ((a[0] + b[0]) / 2.0 + (root[0] - (a[0] + b[0]) / 2.0) * 0.2, (a[1] + b[1]) / 2.0 + (root[1] - (a[1] + b[1]) / 2.0) * 0.2)
        outline += [mid, b]
    outline.append((side * 0.9, 0.5))
    cv.fill(outline, membrane, line, 0.03)
    for i, tip in enumerate(tips):
        for j in range(1, 5):
            t = j / 5.0
            shade = mix(membrane, membrane_dark, 0.5 + 0.1 * j)
            mid = (wrist[0] + (tip[0] - wrist[0]) * t, wrist[1] + (tip[1] - wrist[1]) * t)
            if i < len(tips) - 1:
                nxt = tips[i + 1]
                cv.line([mid, (wrist[0] + (nxt[0] - wrist[0]) * t, wrist[1] + (nxt[1] - wrist[1]) * t)], shade, 0.008)
    cv.line(curve([root, (side * 1.1, -1.12), wrist], 8), line, 0.06)
    cv.line(curve([root, (side * 1.1, -1.12), wrist], 8), bone, 0.036)
    for tip in tips:
        cv.line([wrist, tip], line, 0.036)
        cv.line([wrist, tip], bone, 0.018)
    cv.fill([(side * 1.42, -1.42), (side * 1.5, -1.72), (side * 1.3, -1.46)], bone, line, 0.02)


def coronet(cv, y, gold, gold_light, gold_dark, line, gem, pearl, velvet):
    """Couronne de marquis : cercle orne, fleurons et perles, bonnet de velours."""
    cv.fill(curve([(-0.5, y - 0.02), (-0.34, y - 0.46), (0, y - 0.6), (0.34, y - 0.46), (0.5, y - 0.02)], 8), velvet, line, 0.024)
    for u, big in ((-0.56, 1), (-0.28, 0), (0.0, 1), (0.28, 0), (0.56, 1)):
        if big:
            base = y - 0.14
            cv.fill([(u - 0.1, base), (u - 0.075, base - 0.1), (u - 0.13, base - 0.17), (u - 0.04, base - 0.18), (u, base - 0.3),
                     (u + 0.04, base - 0.18), (u + 0.13, base - 0.17), (u + 0.075, base - 0.1), (u + 0.1, base)], gold_light, line, 0.022)
        else:
            cv.line([(u, y - 0.12), (u, y - 0.3)], line, 0.03)
            cv.line([(u, y - 0.12), (u, y - 0.3)], gold, 0.016)
            cv.dot(u, y - 0.34, 0.058, line)
            cv.dot(u, y - 0.34, 0.042, pearl)
            cv.dot(u - 0.012, y - 0.352, 0.014, (255, 255, 255))
    band = [(-0.7, y - 0.17), (0, y - 0.21), (0.7, y - 0.17), (0.74, y + 0.08), (0, y + 0.04), (-0.74, y + 0.08)]
    cv.fill(band, gold, line, 0.026)
    cv.line([(-0.71, y + 0.02), (0, y - 0.02), (0.71, y + 0.02)], gold_dark, 0.01)
    cv.line([(-0.7, y - 0.13), (0, y - 0.17), (0.7, y - 0.13)], gold_light, 0.01)
    for i, u in enumerate((-0.5, -0.25, 0.0, 0.25, 0.5)):
        c = gem if i % 2 == 0 else pearl
        cv.dot(u, y - 0.075 - (0.02 if abs(u) < 0.3 else 0), 0.045, line)
        cv.dot(u, y - 0.075 - (0.02 if abs(u) < 0.3 else 0), 0.032, c)


PALETTES = {
    "recrue": dict(field=(150, 24, 34), field2=(116, 16, 26), rim=(214, 206, 188), rim2=(138, 128, 112), head=(232, 180, 76), light=(255, 234, 164), dark=(150, 96, 34)),
    "capitaine": dict(field=(158, 22, 32), field2=(118, 14, 24), rim=(232, 182, 80), rim2=(150, 98, 34), head=(236, 186, 82), light=(255, 238, 172), dark=(154, 100, 36)),
    "patriarche": dict(field=(150, 18, 30), field2=(96, 10, 20), rim=(240, 192, 88), rim2=(150, 98, 34), head=(240, 192, 88), light=(255, 242, 184), dark=(158, 102, 36)),
    # broderie d'or sur velours noir : tout est fil d'or, le champ reste le velours
    "brode": dict(field=(22, 20, 24), field2=(118, 86, 40), rim=(214, 168, 78), rim2=(138, 96, 40), head=(226, 178, 84), light=(252, 224, 150), dark=(138, 92, 36),
                  blade=((246, 226, 170), (178, 140, 78)), membrane=((24, 22, 26), (120, 90, 46)), cap=(44, 34, 30)),
}
LINE = (32, 14, 16)
BLADE = (238, 242, 248)
BLADE_DARK = (168, 178, 194)
HILT = (240, 194, 92)
HILT_DARK = (150, 98, 34)
RUBY = (214, 34, 44)
PEARL = (246, 240, 226)
EYE = (255, 92, 30)


def runkandel_arms(rank="capitaine", crown=False, wings=False, px=96, shield=True):
    """Calque des armoiries, en haute definition (px par demi-largeur de l'ecu).
    shield=False : la charge seule (tete, epee, ailes), pour les gravures."""
    pal = PALETTES[rank]
    blade, blade_dark = pal.get("blade", (BLADE, BLADE_DARK))
    membrane, membrane_dark = pal.get("membrane", ((30, 20, 26), (66, 44, 40)))
    u0, u1 = (-2.75, 2.75) if wings else (-1.14, 1.14)
    v0 = -2.02 if wings or crown else -1.3
    if wings:
        v0 = -2.02
    cv = Canvas(px, u0, u1, v0, 1.66)
    if wings:
        for side in (-1, 1):
            bat_wing(cv, side, membrane, membrane_dark, (232, 184, 84), LINE)
    if shield:
        draw_shield(cv, pal, px, u0, v0, rank)
    # charge decalee a gauche de l'epee
    charge = Canvas(px, u0, u1, v0, 1.66)
    dragon_head(charge, pal["head"], pal["light"], pal["dark"], LINE, EYE, (250, 244, 228))
    scaled = [[list(p) for p in row] for row in charge.img]
    k = 0.84
    for y in range(cv.h):
        for x in range(cv.w):
            u, v = u0 + x / float(px), v0 + y / float(px)
            su, sv = (u - 0.02) / k, (v - 0.1) / k
            sx, sy = int((su - u0) * px), int((sv - v0) * px)
            if 0 <= sy < cv.h and 0 <= sx < cv.w and scaled[sy][sx][3]:
                cv.img[y][x] = list(scaled[sy][sx])
    sword(cv, 0.1, -1.0, 1.3, blade, blade_dark, HILT, HILT_DARK, LINE, RUBY)
    if crown:
        coronet(cv, -1.16, (232, 182, 78), (255, 234, 164), (150, 98, 34), LINE, RUBY, PEARL, pal.get("cap", (128, 16, 28)))
    return cv.img


def draw_shield(cv, pal, px, u0, v0, rank):
    cv.fill(shield_outline(1.09), LINE)
    cv.fill(shield_outline(1.07), pal["rim"])
    cv.fill(shield_outline(0.955), pal["rim2"])
    cv.fill(shield_outline(0.94), pal["field"])
    field = Canvas(px, u0, u0 + cv.w / float(px), v0, v0 + cv.h / float(px))
    field.fill(shield_outline(0.94), pal["field"])
    for y in range(cv.h):
        for x in range(cv.w):
            if field.img[y][x][3]:
                u, v = u0 + x / float(px), v0 + y / float(px)
                a, b = ((u + v) % 0.34), ((u - v) % 0.34)
                k = 1.0 + 0.12 * math.sin(a / 0.34 * math.pi) * math.sin(b / 0.34 * math.pi) - 0.3 * max(0.0, v - 0.2) / 1.3
                c = pal["field2"] if min(a, b) < 0.012 else tuple(min(255, ch * k) for ch in pal["field"])
                if 0.13 < a < 0.21 and 0.13 < b < 0.21:
                    c = mix(c, pal["field2"], 0.55)
                cv.img[y][x] = [c[0], c[1], c[2], 255]
    if rank != "recrue":                                                                    # bordure perlee
        pts = shield_outline(1.012)
        for i in range(0, len(pts), 1):
            if i % 2 == 0:
                cv.dot(pts[i][0], pts[i][1], 0.022, pal["rim2"])


def arms_sized(rank, width, crown=False, wings=False, px=64, shield=True):
    """Armoiries reduites a width pixels de large, alpha conserve."""
    big = runkandel_arms(rank, crown, wings, px, shield)
    H, W = len(big), len(big[0])
    return shrink(big, width, max(1, int(round(width * H / float(W)))))
