"""Dessin procedural des butins de monstres (Python pur).

Chaque item = une forme (fonction de SHAPES : griffe, coeur, fiole...) dessinee avec une graine propre (crc32 de son id) et sa
palette. Une forme est une pile de couches ; chaque couche est un masque ombre comme un volume : hauteur tiree de la distance au
bord, normale, lumiere en haut a gauche, motif de surface (ecailles, fissures, fourrure...), ombre portee sur la couche du dessous.
Sortie : icone 64x64 (lissee) et sprite 32x32 (pixels nets) qui sert de texture et de silhouette au modele 3D.
Coordonnees de dessin : 0 a 100, centre 50,50.
"""
import math, random

N = 128
K = N / 100.0


def rgb(h):
    h = h.lstrip("#")
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def hexa(c):
    return "#%02x%02x%02x" % tuple(max(0, min(255, int(v))) for v in c)


def mix(a, b, t):
    a, b = rgb(a) if isinstance(a, str) else a, rgb(b) if isinstance(b, str) else b
    return hexa([a[i] + (b[i] - a[i]) * t for i in range(3)])


def dk(c, t=0.45):
    return mix(c, "#0a0812", t)


def lt(c, t=0.4):
    return mix(c, "#ffffff", t)


IVORY, BONE, STEEL, IRON, WOOD, GOLD, BLOOD, PAPER, LEATHER, GLASS = ("#e8dcc0", "#d8ccac", "#b8c0cc", "#5a606c", "#7a5230", "#e8b83a",
                                                                      "#a82a2a", "#e0d0a8", "#6e4a2c", "#bfe4ea")


def spline(pts, closed=False, step=8):
    """Catmull-Rom."""
    n = len(pts)
    out = []
    idx = range(n) if closed else range(n - 1)
    for i in idx:
        if closed:
            p0, p1, p2, p3 = pts[(i - 1) % n], pts[i], pts[(i + 1) % n], pts[(i + 2) % n]
        else:
            p0, p1, p2, p3 = pts[max(i - 1, 0)], pts[i], pts[i + 1], pts[min(i + 2, n - 1)]
        for k in range(step):
            t = k / step
            t2, t3 = t * t, t * t * t
            out.append(tuple(0.5 * (2 * p1[c] + (p2[c] - p0[c]) * t + (2 * p0[c] - 5 * p1[c] + 4 * p2[c] - p3[c]) * t2
                                    + (3 * p1[c] - p0[c] - 3 * p2[c] + p3[c]) * t3) for c in (0, 1)))
    if not closed:
        out.append(pts[-1])
    return out


class Layer:
    def __init__(self, art, col, pat, bevel, glow, gloss):
        self.a, self.col, self.pat, self.bevel, self.glow, self.gloss = art, col, pat, bevel, glow, gloss
        self.m = bytearray(N * N)

    # -- primitives (val=0 : decoupe) --
    def _disc(self, cx, cy, r, val):
        y0, y1 = max(0, int(cy - r)), min(N - 1, int(cy + r) + 1)
        m = self.m
        rr = r * r
        for y in range(y0, y1 + 1):
            dy = y - cy
            w = rr - dy * dy
            if w < 0:
                continue
            w = math.sqrt(w)
            x0, x1 = max(0, int(cx - w)), min(N - 1, int(cx + w))
            if x1 >= x0:
                m[y * N + x0:y * N + x1 + 1] = bytes([val]) * (x1 - x0 + 1)

    def tube(self, pts, r0, r1=None, val=1, raw=False):
        """Tube a rayon variable le long d'une courbe. r0/r1 : rayons extremes, ou liste de rayons (un par point)."""
        P = [self.a.t(p) for p in pts]
        sc = self.a.scale * K
        if isinstance(r0, (list, tuple)):
            rad = spline([(i, r) for i, r in enumerate(r0)]) if len(r0) > 2 else None
        path = spline(P) if len(P) > 2 else [(P[0][0] + (P[1][0] - P[0][0]) * i / 40, P[0][1] + (P[1][1] - P[0][1]) * i / 40) for i in range(41)]
        L = len(path) - 1
        for i, (x, y) in enumerate(path):
            t = i / max(1, L)
            if isinstance(r0, (list, tuple)):
                f = t * (len(r0) - 1)
                j = min(int(f), len(r0) - 2)
                r = r0[j] + (r0[j + 1] - r0[j]) * (f - j)
            else:
                r = r0 + ((r1 if r1 is not None else r0) - r0) * t
            self._disc(x, y, max(0.6, r * sc), val)
        return self

    def poly(self, pts, val=1, smooth=False):
        P = [self.a.t(p) for p in (spline(pts, closed=True) if smooth else pts)]
        ys = [p[1] for p in P]
        m = self.m
        n = len(P)
        for y in range(max(0, int(min(ys))), min(N - 1, int(max(ys)) + 1) + 1):
            yy = y + 0.5
            xs = []
            for i in range(n):
                (x1, y1), (x2, y2) = P[i], P[(i + 1) % n]
                if (y1 <= yy) != (y2 <= yy):
                    xs.append(x1 + (yy - y1) / (y2 - y1) * (x2 - x1))
            xs.sort()
            for i in range(0, len(xs) - 1, 2):
                a, b = max(0, int(xs[i])), min(N - 1, int(xs[i + 1]))
                if b >= a:
                    m[y * N + a:y * N + b + 1] = bytes([val]) * (b - a + 1)
        return self

    def blob(self, pts, val=1):
        return self.poly(pts, val, smooth=True)

    def ell(self, cx, cy, rx, ry=None, rot=0, val=1):
        ry = rx if ry is None else ry
        c, s = math.cos(math.radians(rot)), math.sin(math.radians(rot))
        pts = []
        for i in range(36):
            u, v = math.cos(i * math.pi / 18) * rx, math.sin(i * math.pi / 18) * ry
            pts.append((cx + u * c - v * s, cy + u * s + v * c))
        return self.poly(pts, val)

    def rect(self, x0, y0, x1, y1, val=1):
        return self.poly([(x0, y0), (x1, y0), (x1, y1), (x0, y1)], val)


class Art:
    def __init__(self, seed, spin=12, zoom=(0.94, 1.04), flip=True):
        self.rng = random.Random(seed)
        r = self.rng
        self.angle = math.radians(r.uniform(-spin, spin))
        self.scale = r.uniform(*zoom)
        self.flip = flip and r.random() < 0.5
        self.dx, self.dy = r.uniform(-1.2, 1.2), r.uniform(-1.2, 1.2)
        self.layers = []
        self.v = 0                                  # rang de l'item parmi ceux de sa forme : choisit le style
        self.noise = _noise(r, 11)
        self.noise2 = _noise(r, 4, 16)
        self.fine = _noise(r, 3)

    def t(self, p):
        x, y = p[0] - 50, p[1] - 50
        if self.flip:
            x = -x
        c, s = math.cos(self.angle), math.sin(self.angle)
        x, y = (x * c - y * s) * self.scale, (x * s + y * c) * self.scale
        return ((x + 50 + self.dx) * K, (y + 50 + self.dy) * K)

    def j(self, v, a=3.0):
        return v + self.rng.uniform(-a, a)

    def jp(self, pts, a=2.0):
        return [(self.j(x, a), self.j(y, a)) for x, y in pts]

    def L(self, col, pat="grain", bevel=12, glow=None, gloss=0.25):
        l = Layer(self, col, pat, bevel, glow, gloss)
        self.layers.append(l)
        return l


def _noise(r, cx, cy=None):
    cy = cy or cx
    w, h = N // cx + 3, N // cy + 3
    g = [[r.random() for _ in range(w)] for _ in range(h)]
    out = [0.0] * (N * N)
    for y in range(N):
        fy = y / cy
        iy = int(fy)
        ty = fy - iy
        ty = ty * ty * (3 - 2 * ty)
        g0, g1 = g[iy], g[iy + 1]
        for x in range(N):
            fx = x / cx
            ix = int(fx)
            tx = fx - ix
            tx = tx * tx * (3 - 2 * tx)
            a = g0[ix] + (g0[ix + 1] - g0[ix]) * tx
            b = g1[ix] + (g1[ix + 1] - g1[ix]) * tx
            out[y * N + x] = a + (b - a) * ty
    return out


def _dist(m, inside=True):
    """Distance de chanfrein (3-4)/3 au bord du masque, a l'interieur (ou a l'exterieur)."""
    big = 1 << 28
    d = [(big if (v != 0) == inside else 0) for v in m]
    for y in range(N):
        o = y * N
        for x in range(N):
            i = o + x
            v = d[i]
            if not v:
                continue
            if x:
                v = min(v, d[i - 1] + 3)
            if y:
                v = min(v, d[i - N] + 3)
                if x:
                    v = min(v, d[i - N - 1] + 4)
                if x < N - 1:
                    v = min(v, d[i - N + 1] + 4)
            d[i] = v
    for y in range(N - 1, -1, -1):
        o = y * N
        for x in range(N - 1, -1, -1):
            i = o + x
            v = d[i]
            if not v:
                continue
            if x < N - 1:
                v = min(v, d[i + 1] + 3)
            if y < N - 1:
                v = min(v, d[i + N] + 3)
                if x < N - 1:
                    v = min(v, d[i + N + 1] + 4)
                if x:
                    v = min(v, d[i + N - 1] + 4)
            d[i] = v
    return [min(v / 3.0, 1000.0) for v in d]


def _ramp(col):
    c = rgb(col)
    lum = (c[0] * 0.3 + c[1] * 0.59 + c[2] * 0.11) / 255
    dark = rgb(mix(col, "#14101e", 0.68 - lum * 0.1))
    light = rgb(mix(col, "#fffdf0", 0.55))
    return dark, c, light


L_DIR = (-0.46, -0.62, 0.635)


def _render_layer(art, l, buf, shadow):
    m = l.m
    if not any(m):
        return
    d = _dist(m)
    bev = float(l.bevel)
    hgt = [0.0] * (N * N)
    for i, v in enumerate(d):
        if v > 0:
            t = min(v / bev, 1.0)
            hgt[i] = bev * math.sqrt(1 - (1 - t) * (1 - t))
    dark, mid, light = _ramp(l.col)
    em = rgb(l.glow) if l.glow else (255, 240, 180)
    nz, n2, fine = art.noise, art.noise2, art.fine
    pat, gloss = l.pat, l.gloss
    seeds = []
    if pat in ("cracks", "lava"):
        seeds = [(art.rng.uniform(0, N), art.rng.uniform(0, N)) for _ in range(16)]
    lx, ly, lz = L_DIR
    sn, cs, at2, sq = math.sin, math.cos, math.atan2, math.sqrt
    fac = [0.16, -0.12, 0.26, -0.2, 0.08, -0.05, 0.2, -0.16, 0.12, -0.08]
    for y in range(N):
        o = y * N
        for x in range(N):
            i = o + x
            if not m[i]:
                continue
            xl = hgt[i - 1] if x else 0
            xr = hgt[i + 1] if x < N - 1 else 0
            yu = hgt[i - N] if y else 0
            yd = hgt[i + N] if y < N - 1 else 0
            nx, ny, nz_ = (xl - xr) * 0.5, (yu - yd) * 0.5, 1.0
            inv = 1 / sq(nx * nx + ny * ny + 1)
            dot = (nx * lx + ny * ly + nz_ * lz) * inv
            v = 0.5 + (dot - 0.635) * 0.95
            e = 0.0
            n = nz[i]
            # ---- motifs de surface ----
            if pat == "grain":
                v += 0.20 * (n - 0.5) + 0.10 * (fine[i] - 0.5)
            elif pat == "scales":
                s, sy = 13.0, 9.0
                r = int(y // sy)
                u = x / s + 0.5 * (r & 1)
                fu = u - int(u) - 0.5
                fv = y / sy - r
                k = fu * fu * 3.4 + fv * fv * 0.9
                v += 0.15 - min(k, 1.0) * 0.5 + 0.08 * (n - 0.5)
            elif pat in ("cracks", "lava"):
                f1 = f2 = 1e9
                for sx, sy in seeds:
                    q = (x - sx) ** 2 + (y - sy) ** 2
                    if q < f1:
                        f1, f2 = q, f1
                    elif q < f2:
                        f2 = q
                edge = sq(f2) - sq(f1)
                if pat == "lava":
                    v -= 0.14
                    if edge < 5.5:
                        e = 1 - edge / 5.5
                elif edge < 2.6:
                    v -= 0.34 * (1 - edge / 2.6)
                v += 0.10 * (n - 0.5)
            elif pat == "fur":
                k = n2[i]
                v += 0.42 * (k - 0.5) + (0.06 if fine[i] > 0.7 else 0)
            elif pat == "stripes":
                if sn(x * 0.16 + y * 0.05 + n * 5.5) > 0.5:
                    v -= 0.46
                v += 0.10 * (n2[i] - 0.5)
            elif pat == "spots":
                if fine[i] * 0.5 + n * 0.5 > 0.6:
                    v -= 0.36
                v += 0.08 * (n2[i] - 0.5)
            elif pat == "veins":
                w = abs(sn(x * 0.11 + y * 0.07 + n * 8))
                if w < 0.09:
                    e = 0.75 * (1 - w / 0.09)
                v += 0.12 * (n - 0.5)
            elif pat == "bolts":
                w = abs(((x * 0.09 + y * 0.05 + n * 2.2) % 2) - 1)
                if w < 0.13:
                    e = 1 - w / 0.13
                v += 0.08 * (n - 0.5)
            elif pat == "rings":
                v += 0.17 * sn((x + y * 2.1) * 0.27) + 0.08 * (n - 0.5)
            elif pat == "frost":
                t = min(d[i] / bev, 1.0)
                v += 0.26 * (1 - t) + 0.14 * (n - 0.5)
                if fine[i] > 0.83:
                    e = 0.9
            elif pat == "facets":
                k = at2(y - 64, x - 64) + 3.2
                v += fac[int(k * 1.55) % 10] + 0.05 * (n - 0.5)
            elif pat == "metal":
                v += 0.17 * sn((x - y) * 0.09 + n * 3) + 0.05 * (fine[i] - 0.5)
            elif pat == "weave":
                v += 0.09 * sn(x * 1.5) * sn(y * 1.5) + 0.14 * (n2[i] - 0.5)
            elif pat == "smoke":
                v += 0.34 * (n - 0.5) + 0.12 * (n2[i] - 0.5)
            elif pat == "void":
                w = abs(sn(x * 0.07 - y * 0.1 + n * 6))
                v += 0.26 * (n - 0.5) - 0.06
                if w < 0.05:
                    e = 0.7
                elif fine[i] > 0.86:
                    e = 1.0
            elif pat == "glow":
                t = min(d[i] / bev, 1.0)
                v = 0.46 + 0.5 * t + 0.08 * (n - 0.5)
            # brillance
            if gloss and dot > 0.9:
                v += (dot - 0.9) * 10 * gloss
            if v <= 0.5:
                t = max(0.0, v * 2)
                c0, c1 = dark, mid
            else:
                t = min(1.0, (v - 0.5) * 2)
                c0, c1 = mid, light
            r = c0[0] + (c1[0] - c0[0]) * t
            g = c0[1] + (c1[1] - c0[1]) * t
            b = c0[2] + (c1[2] - c0[2]) * t
            if e:
                r += (em[0] - r) * e
                g += (em[1] - g) * e
                b += (em[2] - b) * e
            buf[i] = (r, g, b)
    # ombre de cette couche sur ce qui est dessous : appliquee en dehors de son masque
    if shadow:
        out = _dist(m, inside=False)
        for i, v in enumerate(out):
            if 0 < v < 6 and buf[i] is not None and not m[i]:
                k = 0.62 + 0.38 * v / 6
                c = buf[i]
                buf[i] = (c[0] * k, c[1] * k, c[2] * k)


def render(art, outline):
    """Rend les couches. Retourne (icone 64x64, sprite 32x32) : listes de lignes de [r,g,b,a]."""
    buf = [None] * (N * N)
    for i, l in enumerate(art.layers):
        _render_layer(art, l, buf, shadow=i > 0)
    mask = bytearray(1 if v is not None else 0 for v in buf)
    out = _dist(mask, inside=False)
    ol = rgb(outline)
    full = list(buf)
    for i, v in enumerate(out):
        if 0 < v <= 3.2:
            full[i] = ol
    return _down(full, 2), _down(buf, 4, hard=True)


def _down(buf, f, hard=False):
    s = N // f
    px = []
    for y in range(s):
        row = []
        for x in range(s):
            r = g = b = a = 0
            for yy in range(y * f, y * f + f):
                for xx in range(x * f, x * f + f):
                    c = buf[yy * N + xx]
                    if c is not None:
                        r += c[0]
                        g += c[1]
                        b += c[2]
                        a += 1
            tot = f * f
            if a == 0 or (hard and a * 2 < tot):
                row.append([0, 0, 0, 0])
            else:
                row.append([r / a, g / a, b / a, 255 if hard else 255 * a / tot])
        px.append(row)
    return px


# =========================================================================================================
# Formes. a : Art ; p : palette (m principale, s secondaire, x accent, pat motif). Chaque forme varie avec a.rng.
# =========================================================================================================
SHAPES = {}


def shape(fn):
    SHAPES[fn.__name__.rstrip("_")] = fn
    return fn


def alias(name, fn):
    SHAPES[name] = fn


def veins(a, p, pts_list, r=1.6, col=None):
    l = a.L(col or dk(p.m, 0.3), "grain", 3)
    for pts in pts_list:
        l.tube(a.jp(pts, 2), r, r * 0.55)


def stitches(a, pts, col):
    l = a.L(col, "grain", 2)
    for x, y in pts:
        l.ell(x, y, 3.2, 1.2, rot=a.rng.uniform(-30, 30))


@shape
def hide_a(a, p):
    pts = [(50, 7), (63, 15), (73, 24), (91, 27), (76, 41), (69, 56), (72, 69), (90, 80), (70, 84), (56, 93), (44, 93), (30, 84), (10, 80), (28, 69),
           (31, 56), (24, 41), (9, 27), (27, 24), (37, 15)]
    a.L(dk(p.m, 0.25), "grain", 6).blob(a.jp(pts, 2.2))
    a.L(p.m, p.pat, 9).blob(a.jp([(50, 14), (66, 27), (65, 54), (66, 72), (50, 86), (34, 72), (35, 54), (34, 27)], 2.0))
    a.L(p.s, p.pat, 4).tube(a.jp([(50, 20), (50, 50), (50, 78)], 2), 2.2, 2.2)
    stitches(a, [(31, 30), (69, 30), (28, 66), (72, 66)], p.x)


@shape
def snakeskin(a, p):
    l = a.L(p.m, p.pat, 8)
    wob = a.rng.uniform(9, 14)
    l.tube([(18, 12), (36 + wob, 26), (40 - wob, 50), (58 + wob * 0.7, 72), (84, 90)], [9, 17, 18, 16, 3])
    a.L(p.x, "rings", 2).tube([(20, 14), (38 + wob, 27), (41 - wob, 50), (58 + wob * 0.7, 72), (80, 87)], [2, 4, 4, 3, 1])


@shape
def scale_a(a, p):
    for ox, oy, c, sc in ((-16, -8, dk(p.m, 0.22), 0.75), (16, -8, dk(p.m, 0.12), 0.75), (0, 6, p.m, 1.0)):
        pts = [(0, -38), (24, -28), (32, -2), (20, 26), (0, 40), (-20, 26), (-32, -2), (-24, -28)]
        a.L(c, "rings" if sc == 1 else "grain", 9, gloss=0.5).blob([(50 + ox + x * sc, 50 + oy + y * sc) for x, y in a.jp(pts, 2)])
    a.L(p.s, "grain", 3).tube(a.jp([(50, 26), (50, 50), (50, 76)], 1), 2.4, 0.8)


@shape
def claw_a(a, p):
    k = a.j(0, 7)
    bone = mix(IVORY, p.m, 0.5)
    a.L(bone, "grain", 9, gloss=0.6).tube([(30 + k, 16), (20, 44 + k), (36, 72), (80, 90)], [17, 15, 8, 0.8])
    a.L(lt(bone, 0.3), "none", 3, gloss=0.3).tube([(25 + k, 28), (20, 48 + k), (35, 68)], 2, 0.7)
    wrap = a.L(p.s, p.pat, 7)
    wrap.tube([(14, 14), (32 + k, 8), (52, 18)], 10, 8)
    wrap.tube([(11, 25), (32 + k, 20), (51, 31)], 6, 5)


@shape
def sickle(a, p):
    bone = mix(IVORY, p.m, 0.3)
    a.L(bone, "grain", 8, gloss=0.6).tube([(70, 74), (82, 40), (58, 16), (24, 20)], [11, 9, 5, 0.8])
    a.L(p.m, "rings", 8).tube([(58, 90), (72, 76), (78, 62)], 13, 10)
    a.L(p.x, "glow", 3).ell(72, 76, 4)


@shape
def talon(a, p):
    leg = a.L(p.m, p.pat, 7)
    leg.tube([(50, 8), (50, 36)], 8, 9)
    keratin = mix(dk(p.s, 0.3), "#2a2430", 0.4)
    c = a.L(keratin, "none", 6, gloss=0.8)
    c.tube([(46, 40), (22, 52), (14, 80)], 8, 0.7)
    c.tube([(50, 44), (50, 66), (46, 92)], 8, 0.7)
    c.tube([(54, 40), (78, 52), (86, 80)], 8, 0.7)
    a.L(p.m, p.pat, 8).ell(50, 38, 15, 11)


def _cone(a, base, tip, bend, r, col, pat="grain", gloss=0.6):
    mid = ((base[0] + tip[0]) / 2 + bend, (base[1] + tip[1]) / 2 - bend * 0.3)
    return a.L(col, pat, 9, gloss=gloss).tube([base, mid, tip], [r, r * 0.68, 0.8])


@shape
def fang_a(a, p):
    col = mix(IVORY, p.m, 0.3)
    _cone(a, (34, 18), (68, 90), a.j(-13, 4), 17, col)
    a.L(mix(BLOOD, p.x, 0.5), "veins", 7, glow=lt(p.x, .2)).tube([(16, 12), (34, 20), (52, 14)], 9, 8)
    a.L(lt(col, 0.4), "none", 2).tube([(28, 30), (34, 54)], 1.6, 0.6)


@shape
def sabre(a, p):
    col = mix(IVORY, p.m, 0.12)
    a.L(col, "rings", 10, gloss=0.7).tube([(22, 10), (52, 42), (56, 66), (46, 94)], [14, 12, 7, 0.6])
    a.L(mix(p.m, BLOOD, .5), "grain", 7).tube([(10, 8), (24, 14), (36, 6)], 9, 8)


@shape
def tooth_a(a, p):
    col = mix(IVORY, p.m, 0.35)
    l = a.L(col, p.pat if p.pat in ("metal", "spots", "cracks") else "grain", 9, gloss=0.7)
    l.blob(a.jp([(24, 30), (34, 16), (50, 22), (66, 16), (76, 30), (72, 52), (62, 58), (50, 56), (38, 58), (28, 52)], 1.5))
    l.tube([(38, 52), (34, 72), (36, 90)], 11, 2)
    l.tube([(62, 52), (66, 72), (64, 90)], 11, 2)
    a.L(dk(col, 0.28), "grain", 4).tube(a.jp([(34, 38), (50, 44), (66, 36)], 2), 1.8, 1.8)
    a.L(p.x, "glow", 3).ell(a.j(60, 6), a.j(32, 4), 4, 3, glow=None) if False else None


@shape
def tusk(a, p):
    col = mix(IVORY, p.m, 0.2)
    a.L(col, "rings", 10, gloss=0.6).tube([(22, 88), (22, 52), (46, 24), (84, 18)], [12, 14, 8, 0.8])
    b = a.L(p.x, "grain", 4)
    b.tube([(10, 70), (22, 72), (36, 74)], 4.6, 4.6)
    b.tube([(9, 58), (22, 60), (37, 62)], 3.6, 3.6)
    a.L(dk(p.m, .2), "grain", 6).ell(22, 89, 13, 7)


@shape
def bone_a(a, p):
    l = a.L(p.m, p.pat, 9, gloss=0.3)
    k = a.j(0, 4)
    l.tube([(26, 74), (50 + k, 50 - k), (74, 26)], [9, 7, 9])
    for dx, dy in ((-9, 2), (2, 9)):
        l.ell(21 + dx * 0.6, 79 + dy * 0.6, 12.5)
    for dx, dy in ((9, -2), (-2, -9)):
        l.ell(79 + dx * 0.6, 21 + dy * 0.6, 12.5)
    a.L(dk(p.m, 0.32), "grain", 3).tube(a.jp([(40, 56), (52, 48), (62, 36)], 2.5), 1.3, 0.7)


@shape
def skull_a(a, p):
    c = a.L(p.m, p.pat, 11, gloss=0.3)
    c.blob(a.jp([(50, 10), (72, 15), (84, 36), (78, 58), (68, 64), (66, 82), (34, 82), (32, 64), (22, 58), (16, 36), (28, 15)], 1.5))
    dark = a.L(dk(p.m, 0.75), "none", 5, gloss=0)
    dark.ell(36, 46, a.j(10, 1.5), a.j(10, 2)).ell(64, 46, a.j(10, 1.5), a.j(10, 2))
    dark.poly([(50, 56), (44, 68), (56, 68)])
    t = a.L(dk(p.m, 0.6), "none", 2)
    for x in (41, 50, 59):
        t.rect(x - 0.8, 72, x + 0.8, 83)
    g = a.L(p.x, "glow", 4, glow=lt(p.x, .5))
    g.ell(36, 47, 3.4).ell(64, 47, 3.4)


@shape
def beastskull(a, p):
    c = a.L(p.m, "cracks", 10, gloss=0.3)
    c.blob(a.jp([(50, 6), (70, 12), (78, 32), (68, 50), (62, 72), (56, 92), (44, 92), (38, 72), (32, 50), (22, 32), (30, 12)], 1.2))
    h = a.L(lt(p.s, .1), "rings", 6)
    h.tube([(28, 18), (14, 16), (8, 32)], 6, 0.8).tube([(72, 18), (86, 16), (92, 32)], 6, 0.8)
    d = a.L(dk(p.m, 0.8), "none", 4, gloss=0)
    d.ell(36, 36, 8, 6, rot=20).ell(64, 36, 8, 6, rot=-20).ell(45, 80, 3, 4).ell(55, 80, 3, 4)
    t = a.L(IVORY, "none", 3)
    t.poly([(38, 74), (42, 88), (35, 84)]).poly([(62, 74), (58, 88), (65, 84)])
    a.L(p.x, "glow", 4, glow=lt(p.x, .4)).ell(36, 37, 3).ell(64, 37, 3)


@shape
def horn_a(a, p):
    col = mix(p.m, IVORY, 0.35)
    k = a.j(0, 5)
    a.L(col, "rings", 11, gloss=0.5).tube([(28, 90), (20 + k, 56), (44, 26), (82 - k, 12)], [16, 14, 8, 0.8])
    a.L(p.x, p.pat if p.pat in ("lava", "frost") else "grain", 5, glow=lt(p.x, .3)).tube([(11, 86), (28, 94), (46, 90)], 6, 6)


@shape
def crest(a, p):
    l = a.L(p.m, "veins", 7, glow=p.s)
    l.blob(a.jp([(14, 76), (18, 42), (30, 56), (36, 22), (48, 48), (58, 14), (66, 46), (82, 26), (82, 60), (86, 78), (50, 84)], 1.5))
    a.L(p.s, "scales", 6).tube([(14, 80), (50, 86), (86, 80)], 8, 8)


@shape
def jaw_a(a, p):
    col = mix(IVORY, p.m, 0.4)
    l = a.L(col, "cracks", 9, gloss=0.2)
    l.tube([(12, 22), (22, 62), (50, 80), (78, 62), (88, 22)], [9, 11, 11, 11, 9])
    t = a.L(lt(IVORY, 0.3), "none", 4, gloss=0.6)
    for x, y, tx in ((27, 52, 24), (37, 62, 37), (50, 66, 50), (63, 62, 63), (73, 52, 76)):
        t.poly([(x - 5.5, y + 3), (x + 5.5, y + 3), (tx, y - 19)])
    a.L(p.x, "veins", 3, glow=lt(p.x, .4)).ell(a.j(20, 2), 28, 4)
    a.L(p.s, "grain", 5).tube([(82, 26), (92, 12)], 5, 3)


@shape
def heart_a(a, p):
    m = mix(p.m, BLOOD, 0.12)
    tube = a.L(p.s, "rings", 7)
    tube.tube([(40, 34), (38, 18), (24, 12)], 8, 6.5).tube([(56, 32), (62, 16)], 7.5, 6.2).tube([(68, 40), (84, 28)], 6, 4.8)
    l = a.L(m, p.pat if p.pat not in ("fur", "stripes", "weave", "metal") else "veins", 14, glow=lt(p.x, .25), gloss=0.5)
    l.blob(a.jp([(30, 28), (46, 28), (58, 34), (76, 36), (82, 54), (66, 76), (48, 92), (30, 74), (18, 52)], 2.5))
    v = a.L(dk(m, 0.2), "grain", 3)
    v.tube(a.jp([(58, 34), (48, 52), (54, 76)], 3), 2.3, 0.8).tube(a.jp([(50, 54), (34, 62)], 2), 1.5, 0.6).tube(a.jp([(52, 62), (68, 54)], 2), 1.5, 0.6)
    a.L(lt(m, 0.55), "none", 3, gloss=0.2).ell(a.j(34, 2), a.j(42, 2), 6, 3.5, rot=-40)


@shape
def liver(a, p):
    l = a.L(p.m, "veins", 13, glow=p.x, gloss=0.6)
    l.blob(a.jp([(10, 44), (26, 26), (56, 22), (86, 30), (92, 52), (72, 62), (56, 78), (42, 66), (22, 66)], 2))
    a.L(p.s, "grain", 4).tube([(50, 40), (46, 22), (56, 10)], 4, 2.6)


@shape
def eye_a(a, p):
    a.L(p.s, "veins", 16, glow=dk(BLOOD, 0.1), gloss=0.7).ell(50, 52, 37)
    a.L(p.x, "facets", 7, gloss=0.2).ell(a.j(50, 4), a.j(52, 4), 21)
    a.L("#100a14", "none", 4, gloss=0).ell(a.j(50, 1.5), 52, a.j(6, 2), a.j(17, 2))
    a.L("#ffffff", "none", 3).ell(36, 38, 6, 4, rot=-30)


@shape
def pupil(a, p):
    a.L(p.m, "void", 15, glow=lt(p.x, .2), gloss=0.5).ell(50, 50, 30)
    a.L(p.x, "glow", 5).ell(50, 50, 13)
    a.L("#08040c", "none", 3, gloss=0).ell(50, 50, 7)
    o = a.L(p.s, "grain", 4)
    for i in range(6):
        t = i * math.pi / 3 + a.rng.uniform(-.2, .2)
        o.tube([(50 + 31 * math.cos(t), 50 + 31 * math.sin(t)), (50 + 42 * math.cos(t + .3), 50 + 42 * math.sin(t + .3))], 3.4, 0.7)


@shape
def nerve(a, p):
    a.L(p.s, "veins", 7, glow=p.x).tube([(80, 20), (66, 50), (40, 56), (28, 84)], [11, 7, 7, 3.5])
    l = a.L(p.m, "grain", 3)
    l.tube([(32, 76), (14, 86)], 3, 0.8).tube([(30, 78), (22, 94)], 2.6, 0.8).tube([(34, 76), (44, 92)], 3, 0.8)
    a.L(lt(p.s, .4), "none", 11, gloss=.7).ell(80, 20, 13)


@shape
def shard_a(a, p):
    pts = [(46, 6), (66, 26), (72, 60), (54, 94), (34, 70), (28, 34)]
    a.L(p.m, "facets", 10, glow=lt(p.x, .4), gloss=0.7).poly(a.jp(pts, 3.5))
    a.L(lt(p.m, 0.5), "none", 3).poly(a.jp([(46, 12), (52, 26), (44, 44), (38, 32)], 1.5))
    a.L(dk(p.m, .28), "none", 4).poly(a.jp([(58, 50), (67, 62), (54, 88), (49, 70)], 1.5))
    r = a.L(p.s, p.pat if p.pat in ("lava", "bolts", "cracks", "frost") else "grain", 5, glow=lt(p.x, .3))
    r.poly(a.jp([(20, 76), (38, 66), (50, 92), (26, 92)], 2)).poly(a.jp([(66, 72), (82, 64), (84, 90), (62, 92)], 2))


@shape
def cluster(a, p):
    for (x, y, h, w, c) in ((28, 88, 44, 11, dk(p.m, .1)), (72, 88, 50, 12, dk(p.m, .05)), (50, 92, 80, 15, p.m)):
        a.L(c, "facets", 8, glow=lt(p.x, .3), gloss=.7).poly(a.jp([(x - w, y), (x - w * .9, y - h * .68), (x + a.j(0, 2), y - h), (x + w * .9, y - h * .68), (x + w, y)], 1.4))
    a.L(lt(p.m, .55), "none", 3).poly(a.jp([(46, 20), (50, 16), (50, 62), (43, 56)], 1))
    a.L(p.s, p.pat if p.pat in ("lava", "frost", "bolts") else "cracks", 6, glow=lt(p.x, .3)).ell(50, 88, 36, 8)


@shape
def stone(a, p):
    a.L(p.m, "cracks", 12).blob(a.jp([(22, 30), (46, 14), (74, 22), (88, 50), (76, 78), (46, 88), (20, 72), (12, 48)], 3))
    l = a.L(p.x, "glow", 5, glow=None)
    l.ell(38, 44, 5).ell(62, 44, 5)
    l.tube([(38, 66), (50, 72), (62, 64)], 3, 3)


@shape
def glass(a, p):
    a.L(lt(p.m, .18), "facets", 8, gloss=1.0).poly(a.jp([(14, 66), (22, 28), (46, 12), (78, 20), (90, 54), (70, 86), (34, 88)], 4))
    a.L("#fffbe8", "none", 3).poly(a.jp([(30, 34), (44, 22), (50, 26), (34, 48)], 1.5))
    a.L(p.x, "grain", 5).ell(a.j(62, 3), a.j(60, 3), 12, 8, rot=a.j(0, 30))


@shape
def core_a(a, p):
    a.L(dk(p.m, .22), p.pat if p.pat in ("lava", "cracks", "void", "metal") else "metal", 8, glow=p.x).ell(50, 50, 38)
    l = a.L(p.s, "metal", 4)
    for i in range(a.rng.choice((3, 4, 5, 6))):
        t = i * 2 * math.pi / 5 + a.rng.uniform(0, 1)
        l.tube([(50 + 26 * math.cos(t), 50 + 26 * math.sin(t)), (50 + 42 * math.cos(t), 50 + 42 * math.sin(t))], 4, 2.2)
    a.L(dk(p.m, .55), "none", 4).ell(50, 50, 25)
    a.L(p.x, "glow", 15, glow=None).ell(50, 50, 20)
    a.L("#ffffff", "none", 3).ell(43, 42, 5, 3.5, rot=-40)


@shape
def gland_a(a, p):
    a.L(p.s, "rings", 5).tube([(50, 34), (56, 16), (72, 8)], 6, 3.6)
    a.L(p.m, "veins", 15, glow=lt(p.x, .1), gloss=.8).blob(a.jp([(50, 26), (72, 40), (78, 64), (62, 86), (38, 86), (22, 64), (28, 40)], 2.5))
    a.L(p.x, "glow", 8).ell(a.j(50, 3), a.j(64, 3), 12, 9)
    a.L("#ffffff", "none", 3).ell(38, 46, 4, 7, rot=20)


@shape
def tumor(a, p):
    l = a.L(p.m, "veins", 12, glow=p.x, gloss=.7)
    for _ in range(6):
        t, r = a.rng.uniform(0, 6.3), a.rng.uniform(0, 26)
        l.ell(50 + r * math.cos(t), 52 + r * math.sin(t), a.rng.uniform(13, 21))
    o = a.L(p.x, "glow", 5)
    for _ in range(4):
        t, r = a.rng.uniform(0, 6.3), a.rng.uniform(8, 32)
        o.ell(50 + r * math.cos(t), 52 + r * math.sin(t), a.rng.uniform(3, 5.5))


@shape
def brain(a, p):
    a.L(p.m, "spots", 14, gloss=.5).blob(a.jp([(18, 52), (24, 28), (50, 16), (78, 26), (86, 52), (72, 72), (46, 76), (28, 70)], 2))
    s = a.L(dk(p.m, .35), "none", 2)
    s.tube([(50, 18), (46, 40), (52, 60), (46, 74)], 1.6, 1.2)
    for pts in ([(26, 36), (40, 42), (32, 54)], [(66, 28), (60, 44), (74, 48)], [(32, 62), (42, 58)], [(60, 62), (72, 60)]):
        s.tube(a.jp(pts, 1.5), 1.4, 1)
    a.L(p.s, "grain", 5).tube([(56, 72), (60, 90)], 6, 3)


@shape
def meat_a(a, p):
    a.L(p.m, p.pat, 12).blob(a.jp([(12, 44), (30, 18), (64, 14), (90, 40), (82, 70), (54, 88), (24, 78)], 4))
    f = a.L(lt(p.s, .35), "grain", 3)
    f.tube(a.jp([(26, 40), (46, 50), (40, 72)], 3), 2.4, 1.2).tube(a.jp([(50, 26), (64, 46), (74, 42)], 3), 2.4, 1.2)
    a.L(mix(p.x, IVORY, .5), "grain", 6).ell(a.j(66, 4), a.j(62, 4), 8, 7)


@shape
def steak(a, p):
    a.L(p.m, "spots", 12).blob(a.jp([(16, 56), (24, 22), (60, 14), (86, 34), (78, 64), (46, 72), (34, 88), (20, 80)], 2))
    a.L(p.x, "grain", 5).blob(a.jp([(30, 56), (34, 32), (60, 26), (74, 40), (66, 56), (44, 60)], 2))
    a.L(IVORY, "grain", 4).ell(56, 42, 6)


@shape
def ear(a, p):
    a.L(p.m, "grain", 9).blob(a.jp([(20, 80), (30, 50), (58, 22), (92, 8), (80, 42), (60, 70), (36, 86)], 2))
    a.L(dk(p.m, .3), "spots", 5).blob(a.jp([(34, 70), (42, 50), (62, 32), (76, 24), (68, 44), (52, 62)], 2))
    r = a.L(p.x, "metal", 3, gloss=.8)
    r.tube([(42, 76), (34, 90), (24, 78)], 2.2, 2.2)
    a.L(BLOOD, "grain", 4).ell(18, 82, 7, 5)


@shape
def tongue(a, p):
    a.L(p.s, "veins", 11, glow=lt(p.s, .3), gloss=.6).tube([(26, 16), (40, 40), (42, 64), (70, 86)], [15, 16, 13, 4])
    a.L(dk(p.s, .3), "none", 3).tube(a.jp([(32, 26), (42, 48), (46, 68)], 2), 1.6, .8)
    a.L(p.x, "glow", 4).ell(a.j(68, 2), a.j(88, 2), 4).ell(48, 74, 2.6)
    a.L(p.m, "grain", 6).tube([(12, 10), (28, 14), (42, 8)], 8, 6)


@shape
def finger(a, p):
    l = a.L(p.m, "frost", 9)
    l.tube([(26, 88), (42, 58), (60, 42), (70, 18)], [11, 10, 9, 8])
    a.L(dk(p.m, .3), "grain", 2).tube([(32, 62), (46, 74)], 1.3, 1.3).tube([(52, 40), (66, 52)], 1.3, 1.3)
    a.L(p.x, "metal", 4).ell(71, 14, 7, 6)
    a.L(dk(p.s, .5), "cracks", 6).ell(24, 92, 12, 6)


@shape
def tail(a, p):
    a.L(p.m, "scales", 9).tube([(84, 14), (60, 34), (64, 62), (26, 84)], [12, 12, 8, 1])
    a.L(p.x, "lava" if False else "grain", 7).ell(86, 13, 12, 10)
    a.L(p.s, "rings", 3).tube([(82, 24), (62, 40), (62, 60)], 2, 1)


@shape
def feather(a, p):
    k = a.j(0, 7)
    a.L(WOOD if False else lt(p.s, .4), "none", 3).tube([(24, 92), (40, 66), (72 + k, 12)], 2.2, .9)
    l = a.L(p.m, "fur", 7)
    l.blob(a.jp([(70 + k, 8), (82, 34), (62, 56), (46, 76), (30, 70), (34, 50), (46, 28)], 2))
    l.poly(a.jp([(60, 58), (76, 56), (56, 66)], 1), val=0)
    l.poly(a.jp([(32, 44), (26, 58), (40, 52)], 1), val=0)
    a.L(p.s, "stripes", 4).blob(a.jp([(68, 14), (76, 32), (60, 34), (58, 24)], 1.5))
    a.L(lt(p.s, .5), "none", 2).tube([(34, 76), (50, 54), (72 + k, 12)], 1.2, .6)
    a.L(p.x, "grain", 4).tube([(26, 86), (34, 82)], 3.8, 3.8)


@shape
def egg(a, p):
    a.L(p.m, p.pat if p.pat != "metal" else "veins", 18, glow=p.x, gloss=.6).blob(a.jp([(50, 8), (72, 30), (78, 60), (66, 84), (50, 90), (34, 84), (22, 60), (28, 30)], 1.5))
    s = a.L(p.s, "spots", 5)
    for _ in range(5):
        s.ell(a.rng.uniform(32, 68), a.rng.uniform(34, 78), a.rng.uniform(3, 6.5))
    a.L("#ffffff", "none", 3).ell(40, 26, 4, 7, rot=25)


@shape
def larva(a, p):
    l = a.L(p.m, "facets", 8, glow=lt(p.x, .3), gloss=.7)
    for i in range(5):
        l.ell(24 + i * 13, 70 - i * 10 + (6 if i % 2 else 0), 16 - i * 1.2)
    a.L(p.x, "glow", 6).ell(78, 22, 6).ell(48, 50, 5)
    s = a.L(lt(p.x, .4), "facets", 4)
    s.poly([(16, 74), (6, 56), (24, 60)]).poly([(38, 88), (34, 66), (50, 74)])


@shape
def slime(a, p):
    l = a.L(p.m, p.pat, 11, glow=lt(p.x, .2), gloss=.9)
    l.blob(a.jp([(10, 72), (22, 50), (36, 46), (46, 26), (62, 42), (80, 50), (92, 72), (72, 86), (30, 86)], 2.5))
    l.tube([(32, 48), (28, 28)], 5, 1.5)
    l.tube([(72, 50), (76, 36)], 4, 1)
    a.L(p.x, "glow", 7).ell(a.j(56, 5), a.j(68, 3), 8, 6).ell(a.j(32, 4), a.j(70, 3), 5, 4)
    a.L("#ffffff", "none", 2).ell(36, 56, 4, 2, rot=-25)


@shape
def pulp(a, p):
    a.L(p.m, "grain", 12).blob(a.jp([(20, 28), (50, 14), (80, 26), (86, 62), (60, 88), (28, 82), (12, 56)], 3))
    a.L(p.s, "spots", 7).blob(a.jp([(32, 38), (52, 28), (72, 38), (72, 62), (54, 74), (34, 66)], 2))
    d = a.L(lt(p.m, .2), "none", 4, gloss=.9)
    for _ in range(4):
        d.ell(a.rng.uniform(38, 66), a.rng.uniform(38, 64), 3)
    s = a.L(p.x, "grain", 3)
    for t in (0.5, 1.7, 2.9, 4.1, 5.3):
        s.tube([(50 + 40 * math.cos(t), 50 + 40 * math.sin(t)), (50 + 50 * math.cos(t), 50 + 50 * math.sin(t))], 2.2, .6)


@shape
def soul_a(a, p):
    a.L(p.s, "smoke", 13, gloss=.1).blob(a.jp([(50, 8), (68, 28), (74, 58), (58, 76), (46, 94), (42, 74), (26, 60), (28, 30)], 3))
    a.L(p.m, "glow", 12).blob(a.jp([(50, 22), (62, 38), (62, 58), (50, 70), (38, 58), (38, 38)], 1.5))
    f = a.L(dk(p.s, .6), "none", 4, gloss=0)
    f.ell(44, 44, 3.2, 4.4).ell(56, 44, 3.2, 4.4).ell(50, 56, 2.6, 3.8)


@shape
def flame(a, p):
    a.L(p.m, "glow", 12).blob(a.jp([(48, 6), (58, 26), (76, 44), (74, 72), (52, 92), (28, 76), (24, 50), (38, 38)], 2.5))
    a.L(p.x, "glow", 10).blob(a.jp([(50, 36), (62, 54), (60, 74), (50, 84), (38, 72), (40, 54)], 1.5))
    a.L("#ffffff", "glow", 5).ell(50, 70, 6, 8)


@shape
def ember(a, p):
    a.L("#2a2024", "lava", 11, glow=p.s).blob(a.jp([(16, 56), (32, 30), (60, 22), (86, 44), (80, 74), (48, 84), (24, 78)], 4))
    a.L(p.x, "glow", 6).ell(a.j(50, 6), a.j(54, 5), 11, 8)
    f = a.L(p.s, "glow", 4)
    for x, y in ((40, 14), (64, 10), (22, 26)):
        f.tube([(a.j(x, 2), y + 12), (x, y)], 3, .7)


@shape
def powder(a, p):
    a.L(p.m, "grain", 16).blob(a.jp([(10, 84), (30, 56), (50, 26), (68, 54), (90, 84), (50, 90)], 2))
    a.L(lt(p.m, .3), "spots", 7).blob(a.jp([(34, 72), (50, 42), (62, 62), (64, 78), (44, 80)], 2))
    s = a.L(p.s, "glow", 3)
    for _ in range(7):
        s.ell(a.rng.uniform(14, 86), a.rng.uniform(16, 62), a.rng.uniform(1.3, 2.8))


@shape
def spark(a, p):
    c = a.L(p.m, "glow", 12)
    c.poly(a.jp([(50, 6), (58, 40), (92, 50), (58, 60), (50, 94), (42, 60), (8, 50), (42, 40)], 2))
    a.L(p.s, "glow", 8).ell(50, 50, 11)
    a.L("#ffffff", "glow", 5).ell(50, 50, 5.5)


@shape
def bolt(a, p):
    a.L(p.m, "glow", 6).poly(a.jp([(60, 4), (28, 50), (46, 50), (26, 96), (74, 40), (54, 40), (72, 4)], 2))
    a.L("#ffffff", "glow", 3).poly(a.jp([(60, 12), (38, 46), (52, 46), (42, 76), (62, 44), (48, 44), (64, 12)], 1))
    a.L(p.s, "glow", 3).tube([(16, 40), (10, 26), (22, 18)], 1.8, .7).tube([(84, 60), (92, 72), (80, 82)], 1.8, .7)


@shape
def snowflake(a, p):
    l = a.L(p.m, "frost", 4, glow="#ffffff", gloss=.6)
    r0 = a.j(0, 15)
    for i in range(6):
        t = math.radians(i * 60 + r0)
        c, s = math.cos(t), math.sin(t)
        l.tube([(50, 50), (50 + 43 * c, 50 + 43 * s)], 3.2, 1.8)
        for f in (.5, .75):
            bx, by = 50 + 43 * f * c, 50 + 43 * f * s
            for d in (-.9, .9):
                l.tube([(bx, by), (bx + 13 * math.cos(t + d), by + 13 * math.sin(t + d))], 2, 1)
    a.L(p.s, "facets", 5).ell(50, 50, 9)


@shape
def tear(a, p):
    a.L(p.m, "frost", 16, gloss=1.0, glow="#ffffff").blob(a.jp([(50, 6), (64, 40), (76, 62), (64, 86), (50, 92), (36, 86), (24, 62), (36, 40)], 1))
    a.L(p.s, "glow", 9).ell(52, 66, 10, 11)
    a.L("#ffffff", "none", 3).ell(40, 54, 3.4, 7, rot=20)


@shape
def pearl(a, p):
    a.L(dk(p.s, .15), "rings", 8).tube([(14, 62), (30, 86), (70, 86), (86, 62)], [7, 9, 9, 7])
    a.L(p.m, "glow" if p.pat == "lava" else "frost", 20, glow="#ffffff", gloss=1.0).ell(50, 48, 28)
    a.L("#ffffff", "none", 3).ell(40, 37, 6, 4, rot=-35)


@shape
def shell(a, p):
    l = a.L(p.m, "lava", 11, glow=p.x)
    l.ell(50, 50, 38)
    s = a.L(dk(p.s, .1), "metal", 5)
    pts = [(50 + (6 + t * 2.2) * math.cos(t), 50 + (6 + t * 2.2) * math.sin(t)) for t in [i * .45 for i in range(26)]]
    s.tube(pts, 1.8, 3.6)
    a.L(p.x, "glow", 5).ell(50, 50, 6)


@shape
def carapace_a(a, p):
    a.L(p.m, p.pat if p.pat != "grain" else "metal", 13, gloss=.8).blob(a.jp([(50, 8), (76, 18), (88, 46), (76, 76), (50, 92), (24, 76), (12, 46), (24, 18)], 1.6))
    a.L(dk(p.m, .45), "none", 2).tube([(50, 10), (50, 90)], 1.4, 1.4)
    r = a.L(dk(p.m, .3), "none", 2)
    for y in (32, 50, 68):
        r.tube([(22, y - 5), (50, y + 6), (78, y - 5)], 1.1, 1.1)
    a.L(p.s, "grain", 4).ell(a.j(35, 2), 24, 7, 4, rot=-20).ell(a.j(65, 2), 24, 7, 4, rot=20)


@shape
def antenna(a, p):
    l = a.L(p.m, "rings", 4)
    l.tube([(18, 92), (30, 50), (60, 24), (88, 18)], 5.5, 1.5)
    l.tube([(32, 50), (20, 26), (6, 16)], 3, .9)
    a.L(p.s, "metal", 7).ell(a.j(16, 1), 90, 11, 9)
    a.L(p.x, "glow", 4).ell(88, 18, 3.5)


@shape
def fossil(a, p):
    a.L(p.m, "cracks", 12).blob(a.jp([(14, 40), (34, 12), (70, 14), (90, 44), (76, 80), (36, 88), (14, 68)], 4))
    l = a.L(dk(p.m, .4), "none", 3)
    l.ell(50, 50, 14, 24)
    for y in range(32, 70, 8):
        l.tube([(36, y), (24, y + 4)], 1.8, 1.8).tube([(64, y), (76, y + 4)], 1.8, 1.8)
    a.L(lt(p.m, .35), "rings", 3).ell(50, 50, 9, 20)


@shape
def membrane(a, p):
    a.L(p.m, "veins", 6, glow=lt(p.x, .1)).poly(a.jp([(12, 12), (40, 18), (88, 14), (80, 40), (88, 66), (64, 58), (46, 88), (34, 60), (10, 56), (22, 34)], 1.6))
    b = a.L(mix(IVORY, p.s, .5), "grain", 4)
    b.tube([(10, 10), (46, 26), (88, 14)], 3, 1.2).tube([(46, 26), (80, 40), (88, 66)], 2.4, .8).tube([(46, 26), (46, 88)], 2.4, .8).tube([(46, 26), (22, 34), (10, 56)], 2.4, .8)


@shape
def beak(a, p):
    a.L(dk(p.m, .1), "grain", 8, gloss=.5).poly(a.jp([(14, 44), (34, 34), (90, 76), (42, 74), (16, 66)], 1), smooth=False)
    a.L(p.x, "rings", 11, gloss=.7).blob(a.jp([(10, 34), (30, 18), (60, 28), (92, 68), (58, 60), (32, 56), (14, 52)], 1))
    a.L(dk(p.s, .5), "none", 3).ell(26, 32, 3.2)
    a.L(lt(p.x, .5), "none", 2).tube([(30, 26), (58, 36)], 1.6, .6)


@shape
def pincer(a, p):
    l = a.L(p.m, p.pat, 10, gloss=.8)
    l.tube([(70, 88), (58, 66), (62, 44), (84, 14)], [9, 14, 15, 1])
    l.tube([(54, 70), (34, 56), (26, 34), (34, 12)], [13, 12, 8, 1])
    a.L(p.x, "glow", 4).ell(60, 66, 4)
    a.L(p.s, "metal", 7).tube([(72, 84), (82, 94)], 9, 8)
    t = a.L(lt(p.s, .35), "none", 3)
    t.poly([(51, 44), (47, 54), (40, 46)]).poly([(46, 30), (38, 34), (40, 24)])


@shape
def stinger(a, p):
    for i, (x, y, r) in enumerate(((78, 84, 11), (64, 72, 12), (52, 58, 12))):
        a.L(p.m if i % 2 == 0 else dk(p.m, .2), "metal", 8, gloss=.7).ell(x, y, r, r * .85, rot=-40)
    a.L(p.s, "veins", 9, glow=p.x, gloss=.7).blob(a.jp([(40, 52), (58, 42), (54, 28), (22, 8), (32, 36)], 1))
    a.L(p.x, "glow", 3).ell(22, 12, 3.2)


@shape
def leg(a, p):
    l = a.L(p.s, "fur", 6)
    l.tube([(84, 14), (58, 22), (48, 52)], [7, 8, 6])
    l.tube([(48, 52), (34, 74), (12, 90)], [6, 4.5, 1])
    a.L(p.x, "rings", 8, gloss=.7).ell(48, 52, 9).ell(85, 14, 8.5)
    a.L(lt(p.m, .2), "grain", 3).tube([(70, 18), (56, 30)], 1.4, 1).tube([(40, 64), (26, 78)], 1.2, .8)


@shape
def tentacle(a, p):
    k = a.j(0, 6)
    a.L(p.m, p.pat, 10, gloss=.5).tube([(18, 84), (44 + k, 66), (70, 58), (64, 32), (38 - k, 26), (40, 42)], [13, 12, 10, 8, 5, 1])
    s = a.L(lt(p.s, .4), "none", 3, gloss=.7)
    for x, y, r in ((27, 72, 4.5), (45 + k, 57, 4), (62, 50, 3.6), (56, 36, 3)):
        s.ell(x, y, r)
    a.L(dk(p.x, .2), "cracks", 5).ell(14, 88, 13, 7, rot=-30)


@shape
def sucker(a, p):
    a.L(p.m, "void", 13, glow=lt(p.s, .35)).ell(50, 50, 40, 36)
    a.L(lt(p.m, .3), "rings", 6).ell(50, 50, 26, 23)
    a.L(dk(p.s, .5), "none", 6, gloss=0).ell(50, 50, 15, 13)
    l = a.L(p.x, "grain", 3)
    for i in range(9):
        t = i * .698 + a.rng.uniform(-.1, .1)
        l.ell(50 + 33 * math.cos(t), 50 + 30 * math.sin(t), 2.4)


@shape
def crown_a(a, p):
    col = mix(p.m, GOLD, .5)
    b = a.L(col, "metal", 7, gloss=.9)
    n = a.rng.choice((3, 4, 5))
    pts = [(12, 82), (12, 34)]
    for i in range(n):
        w = 76 / n
        pts += [(12 + w * (i + .5), a.j(12, 4) if i != n // 2 else 6), (12 + w * (i + 1), 50)]
    pts[-1] = (88, 34)
    pts.append((88, 82))
    b.poly(pts)
    a.L(lt(col, .25), "metal", 4).rect(10, 70, 90, 84)
    g = a.L(p.x, "facets", 4, gloss=.9)
    for i in range(3):
        g.ell(28 + i * 22, 62 - (6 if i == 1 else 0), 5.6 if i == 1 else 4)
    t = a.L(p.s, "glow", 3)
    for i in range(n):
        t.ell(12 + 76 / n * (i + .5), 14 if i != n // 2 else 9, 3.2)


@shape
def veil(a, p):
    a.L(p.m, "smoke", 8, gloss=.1).blob(a.jp([(30, 8), (52, 14), (72, 8), (86, 40), (78, 88), (68, 74), (56, 92), (44, 72), (32, 90), (22, 70), (12, 84), (16, 38)], 2.5))
    f = a.L(p.s, "smoke", 4)
    for x in (34, 52, 68):
        f.tube(a.jp([(x, 16), (x + 4, 46), (x - 2, 76)], 3), 2.4, 1.0)
    a.L(lt(p.x, .1), "glow", 3).tube([(30, 12), (52, 18), (72, 12)], 2, 2)


@shape
def cape(a, p):
    a.L(p.m, "weave", 8).poly(a.jp([(32, 12), (68, 12), (88, 88), (72, 80), (60, 92), (46, 80), (32, 92), (22, 80), (10, 88)], 2))
    f = a.L(p.s, "smoke", 3)
    for x0, x1 in ((42, 34), (58, 66), (50, 50)):
        f.tube([(x0, 16), (x1, 84)], 2.2, 3)
    c = a.L(mix(p.x, GOLD, .3), "metal", 5, gloss=.9)
    c.tube([(26, 12), (50, 18), (74, 12)], 5, 5)
    a.L(p.x, "facets", 4).ell(50, 18, 7)


@shape
def robe(a, p):
    a.L(p.m, "weave", 9).poly(a.jp([(38, 10), (62, 10), (92, 32), (80, 44), (70, 36), (78, 92), (22, 92), (30, 36), (20, 44), (8, 32)], 1.5))
    a.L(p.s, "weave", 4).tube([(50, 12), (50, 90)], 5, 7)
    a.L(p.x, "glow", 3).ell(50, 42, 4).ell(50, 60, 4).ell(50, 78, 4)
    a.L(p.s, "grain", 3).tube([(24, 88), (50, 91), (76, 88)], 3, 3)


@shape
def apron(a, p):
    a.L(p.m, "weave", 8).poly(a.jp([(36, 22), (64, 22), (68, 42), (84, 50), (80, 92), (20, 92), (16, 50), (32, 42)], 1))
    s = a.L(p.x, "grain", 3)
    s.tube([(38, 24), (30, 8), (70, 8), (62, 24)], 2, 2)
    s.tube([(16, 52), (4, 60)], 2, 1.4).tube([(84, 52), (96, 60)], 2, 1.4)
    g = a.L(dk(p.x, .1), "spots", 5)
    for _ in range(4):
        g.ell(a.rng.uniform(26, 74), a.rng.uniform(50, 84), a.rng.uniform(4, 8), a.rng.uniform(3, 5))
    a.L(p.s, "weave", 4).rect(36, 62, 64, 76)


@shape
def bandage(a, p):
    l = a.L(p.m, "weave", 8)
    l.ell(38, 62, 28, 26)
    a.L(dk(p.m, .3), "none", 7, gloss=0).ell(38, 62, 10, 9)
    b = a.L(p.s, "weave", 6)
    b.tube([(56, 42), (76, 30), (66, 14), (90, 8)], [11, 10, 9, 5])
    r = a.L(dk(p.m, .2), "none", 2)
    r.ell(38, 62, 19, 18, val=1)
    r.ell(38, 62, 17.6, 16.6, val=0)
    a.L(p.x, "glow", 3).ell(a.j(74, 3), 30, 3).ell(a.j(62, 3), 76, 3)


@shape
def mane(a, p):
    l = a.L(p.m, "fur", 5, gloss=.2)
    for i, x in enumerate((26, 38, 50, 62, 74)):
        l.tube([(x, 14), (x + a.j(4, 6), 48), (x + a.j(-4, 8), 92)], 6, 1.2)
    l2 = a.L(lt(p.m, .35), "fur", 3)
    for x in (32, 46, 58, 68):
        l2.tube([(x, 16), (x + a.j(2, 6), 72)], 2, .6)
    a.L(dk(p.s, .1), "metal", 5).tube([(20, 14), (50, 10), (80, 14)], 6, 6)
    a.L(lt(p.s, .4), "glow", 3).ell(50, 10, 3)


@shape
def horseshoe(a, p):
    a.L(p.m, "metal", 7, gloss=.9).tube([(26, 16), (16, 50), (32, 82), (68, 82), (84, 50), (74, 16)], [8, 11, 12, 12, 11, 8])
    h = a.L(dk(p.m, .6), "none", 2, gloss=0)
    for x, y in ((21, 34), (19, 52), (28, 70), (72, 70), (81, 52), (79, 34)):
        h.ell(x, y, 2.4)
    a.L(p.s, "veins", 3, glow=lt(p.s, .4)).tube([(40, 86), (50, 88), (60, 86)], 2.4, 2.4)


@shape
def plate_a(a, p):
    a.L(p.m, p.pat if p.pat in ("void", "cracks") else "metal", 10, glow=p.x, gloss=.9).poly(a.jp([(14, 22), (40, 10), (66, 14), (90, 34), (78, 62), (82, 84), (52, 92), (34, 78), (12, 66)], 2.2))
    a.L(p.s, "metal", 3).tube(a.jp([(22, 26), (46, 16), (70, 20), (84, 34)], 1), 2.6, 2.6)
    r = a.L(lt(p.s, .3), "none", 3, gloss=1)
    for x, y in ((24, 56), (72, 72), (40, 74)):
        r.ell(x, y, 3)
    a.L(p.x, "glow", 4).poly(a.jp([(50, 34), (60, 50), (50, 66), (40, 50)], 1.5))


@shape
def gear(a, p):
    t = a.L(p.m, "metal", 7, glow=p.x, gloss=.9)
    n = a.rng.choice((7, 8, 9, 10))
    pts = []
    for i in range(n):
        for da, r in ((-.32, 34), (-.2, 44), (.2, 44), (.32, 34)):
            th = (i + da) * 2 * math.pi / n
            pts.append((50 + r * math.cos(th), 50 + r * math.sin(th)))
    t.poly(pts)
    t.ell(50, 50, 12, val=0)
    a.L(p.s, "lava" if p.pat == "lava" else "metal", 4, glow=p.x).tube([(26, 50), (50, 74), (74, 50), (50, 26), (26, 50)], 3, 3)
    h = a.L(p.x, "glow", 4)
    h.ell(50, 34, 2.6).ell(50, 66, 2.6).ell(34, 50, 2.6).ell(66, 50, 2.6)


@shape
def scrap(a, p):
    a.L(dk(p.m, .12), "metal", 7, gloss=.7).poly(a.jp([(12, 60), (28, 30), (54, 40), (46, 76), (22, 84)], 3))
    a.L(p.s, "metal", 7, gloss=.9).poly(a.jp([(38, 66), (56, 18), (88, 28), (84, 62), (60, 84)], 3))
    r = a.L(lt(p.s, .4), "none", 3, gloss=1)
    for x, y in ((62, 32), (76, 38), (70, 66)):
        r.ell(x, y, 2.6)
    a.L(p.x, "cracks", 4).tube([(50, 28), (64, 54), (56, 78)], 2.6, 1.6)


@shape
def saw(a, p):
    b = a.L(p.s, "metal", 5, gloss=1)
    pts = [(10, 80), (20, 64)]
    for i in range(9):
        x, y = 22 + i * 7.4, 62 - i * 5.4 + a.j(0, 1.5)
        pts += [(x + 7, y + 3), (x + 3.5, y - 7)]
    pts = pts[:-2] + [(90, 14), (84, 42), (34, 84)]
    b.poly(pts)
    a.L(dk(p.s, .3), "none", 2).tube([(22, 74), (82, 26)], 1.1, 1.1)
    a.L(WOOD, "rings", 7).tube([(4, 92), (18, 78)], 9, 10)
    a.L(p.x, "metal", 3).ell(13, 83, 3)


@shape
def wheel(a, p):
    t = a.L(p.m, "grain", 6)
    t.tube([(50, 8), (84, 26), (84, 72), (50, 92), (16, 72), (16, 26), (50, 8)], 8.5, 8.5)
    s = a.L(p.s, "metal", 4, gloss=.9)
    s.tube([(50, 50), (50, 14)], 4, 3).tube([(50, 50), (20, 70)], 4, 3).tube([(50, 50), (80, 70)], 4, 3)
    a.L(p.x, "metal", 7, gloss=1).ell(50, 50, 12)
    a.L("#c02a2a", "glow", 3).ell(50, 50, 4.4)


@shape
def engine(a, p):
    a.L(p.m, "metal", 8, gloss=.8).rect(16, 34, 84, 84)
    c = a.L(p.s, "metal", 5, gloss=1)
    for x in (24, 42, 60):
        c.rect(x, 12, x + 14, 38)
    f = a.L(dk(p.m, .5), "none", 2)
    for y in (46, 56, 66, 76):
        f.rect(20, y, 80, y + 2.6)
    a.L(p.x, "glow", 6).ell(50, 60, 9)
    a.L(lt(p.x, .4), "metal", 3).tube([(84, 50), (94, 42), (92, 26)], 3.4, 2.4)


@shape
def coil(a, p):
    a.L(p.s, "metal", 6).rect(40, 8, 60, 92)
    w = a.L(p.m, "bolts", 3, glow=p.x, gloss=.8)
    for y in range(20, 84, 9):
        w.tube([(24, y + a.j(0, 1)), (50, y + 4), (76, y)], 3.8, 3.8)
    e = a.L(lt(p.s, .3), "metal", 5, gloss=1)
    e.rect(28, 6, 72, 15).rect(28, 85, 72, 94)
    a.L(p.x, "glow", 3).ell(a.j(26, 3), 30, 3).ell(a.j(74, 3), 66, 3)


@shape
def mace(a, p):
    a.L(p.s, "grain", 5).tube([(18, 90), (58, 44)], 5, 6)
    a.L(LEATHER, "rings", 3).tube([(14, 94), (28, 78)], 6.5, 6.5)
    a.L(dk(p.m, .1), "cracks", 11, gloss=.5).ell(64, 34, 25)
    s = a.L(lt(p.s, .4), "metal", 3, gloss=1)
    for i in range(8):
        t = i * .785 + .3
        s.poly([(64 + 21 * math.cos(t - .2), 34 + 21 * math.sin(t - .2)), (64 + 21 * math.cos(t + .2), 34 + 21 * math.sin(t + .2)), (64 + 35 * math.cos(t), 34 + 35 * math.sin(t))])
    a.L(p.x, "glow", 3).ell(64, 34, 5)


@shape
def blade(a, p):
    a.L(p.m if p.pat != "metal" else lt(p.s, .2), "metal", 5, gloss=1).poly(a.jp([(24, 76), (58, 26), (92, 6), (76, 44), (36, 86)], 1))
    a.L(dk(p.s, .25), "none", 2).tube([(32, 76), (80, 22)], 1.4, .6)
    n = a.L(dk(p.m, .6), "none", 3, gloss=0)
    for x, y in ((64, 42), (53, 56)):
        n.poly([(x + 8, y - 2), (x + 8, y + 7), (x, y + 8)])
    a.L(p.x, "metal", 4, gloss=.8).tube([(14, 62), (28, 72), (38, 88)], 5, 5)
    a.L(LEATHER, "rings", 4).tube([(26, 74), (10, 92)], 5, 4.5)


@shape
def cleaver(a, p):
    a.L(p.s, "metal", 5, gloss=1).poly(a.jp([(32, 12), (88, 22), (82, 62), (28, 56)], 1.5))
    a.L(lt(p.s, .5), "none", 2).tube([(28, 54), (82, 60)], 1.5, 1.5)
    a.L(dk(p.s, .75), "none", 3, gloss=0).ell(74, 32, 4)
    a.L(WOOD, "rings", 5).tube([(32, 34), (14, 40), (4, 74)], 6, 5)
    a.L(mix(BLOOD, p.x, .2), "spots", 4).ell(a.j(60, 5), a.j(50, 2), 10, 5).ell(a.j(44, 3), 40, 6, 3)


@shape
def spyglass(a, p):
    a.L(p.m, "metal", 6, gloss=.9).tube([(14, 84), (46, 50)], 8, 8)
    a.L(p.s if False else mix(p.m, LEATHER, .5), "weave", 7).tube([(42, 54), (74, 22)], 11, 11)
    b = a.L(GOLD, "metal", 3, gloss=1)
    b.tube([(38, 44), (52, 60)], 3.2, 3.2).tube([(62, 12), (84, 32)], 4.4, 4.4).tube([(8, 80), (18, 92)], 3, 3)
    a.L(p.x, "frost", 5, glow="#ffffff").ell(77, 18, 7, 7)
    a.L(dk(p.x, .6), "none", 2).tube([(70, 14), (78, 20), (82, 12)], 1, .7)


@shape
def scroll(a, p):
    a.L(lt(p.s if p.pat == "metal" else p.m, .3) if False else PAPER, "grain", 5).rect(18, 16, 82, 84)
    r = a.L(mix(PAPER, WOOD, .35), "rings", 7)
    r.tube([(12, 14), (88, 14)], 7, 7).tube([(12, 86), (88, 86)], 7, 7)
    t = a.L(dk(p.m, .55), "none", 2)
    for y in (30, 40, 50, 60):
        t.tube([(a.j(27, 2), y), (a.j(70, 5), y + a.j(0, 1.4))], 1, 1)
    a.L(p.x, "glow", 4).ell(a.j(64, 3), 72, 6.5)


@shape
def book(a, p):
    a.L(PAPER, "grain", 4).poly(a.jp([(20, 18), (82, 14), (84, 84), (22, 90)], .8))
    a.L(p.m, "weave", 8).poly(a.jp([(14, 14), (76, 10), (78, 82), (18, 88)], 1))
    a.L(p.s, "metal", 4).rect(12, 12, 24, 90)
    c = a.L(p.x, "glow", 5)
    c.ell(50, 46, 10)
    c.ell(50, 46, 6, val=0)
    a.L(dk(p.m, .7), "none", 3, gloss=0).poly(a.jp([(78, 40), (58, 58), (70, 84), (84, 84)], 2))


@shape
def map_(a, p):
    a.L(p.x, "grain", 4).poly(a.jp([(10, 22), (30, 12), (50, 20), (72, 10), (92, 18), (88, 80), (68, 90), (48, 82), (28, 92), (8, 82)], 1.5))
    f = a.L(dk(p.x, .25), "none", 2)
    f.tube([(30, 14), (28, 90)], .9, .9).tube([(50, 22), (48, 80)], .9, .9).tube([(70, 12), (68, 88)], .9, .9)
    i = a.L(p.s, "grain", 5)
    i.blob(a.jp([(24, 40), (40, 30), (54, 40), (50, 62), (32, 66)], 3))
    t = a.L("#b02a2a", "none", 2)
    for k in range(6):
        t.ell(50 + k * 3, 68 - k * 6, 1.1)
    x = a.L("#b02a2a", "none", 3)
    x.tube([(62, 24), (74, 36)], 2, 2).tube([(74, 24), (62, 36)], 2, 2)


@shape
def tricorn(a, p):
    a.L(p.m, "weave", 8).poly(a.jp([(8, 58), (30, 42), (50, 24), (70, 42), (92, 58), (76, 74), (50, 68), (24, 74)], 1.5))
    a.L(lt(p.m, .15), "weave", 12).blob(a.jp([(26, 56), (32, 26), (50, 16), (68, 26), (74, 56), (50, 62)], 1.5))
    b = a.L(GOLD, "metal", 3, gloss=1)
    b.tube([(8, 60), (24, 74), (50, 68), (76, 74), (92, 60)], 2, 2)
    a.L(p.x, "grain", 4).ell(50, 42, 7.5, 8)
    d = a.L("#201c1c", "none", 2)
    d.ell(47, 40, 1.5).ell(53, 40, 1.5).poly([(50, 43), (48, 46), (52, 46)])


@shape
def toque(a, p):
    a.L(p.m, "weave", 10).blob(a.jp([(22, 44), (16, 26), (32, 12), (50, 16), (68, 12), (84, 26), (78, 44), (74, 74), (26, 74)], 1.5))
    f = a.L(dk(p.m, .25), "none", 2)
    for x in (38, 50, 62):
        f.tube([(x, 34), (x, 66)], 1, 1)
    a.L(p.x, "grain", 5).tube([(24, 78), (50, 82), (76, 78)], 7, 7)
    a.L(mix(BLOOD, p.x, .3), "spots", 3).ell(a.j(66, 4), a.j(28, 4), 6, 4)


@shape
def helm(a, p):
    a.L(p.m, "metal", 13, glow=p.x, gloss=1).blob(a.jp([(50, 8), (74, 18), (82, 46), (76, 90), (58, 74), (50, 88), (42, 74), (24, 90), (18, 46), (26, 18)], 1))
    a.L(p.s, "metal", 4).tube([(50, 8), (50, 40)], 4, 3)
    v = a.L("#0c0a12", "none", 4, gloss=0)
    v.poly([(26, 46), (46, 50), (46, 56), (28, 54)]).poly([(74, 46), (54, 50), (54, 56), (72, 54)])
    g = a.L(p.x, "glow", 3)
    g.ell(38, 51, 3.6, 2).ell(62, 51, 3.6, 2)
    h = a.L(dk(p.s, .2), "metal", 5)
    h.tube([(26, 22), (12, 14), (8, 2)], 5, .8).tube([(74, 22), (88, 14), (92, 2)], 5, .8)


@shape
def sharkhead(a, p):
    l = a.L(p.m, "grain", 11)
    l.tube([(8, 34), (50, 30), (92, 34)], 10, 10)
    l.tube([(50, 32), (50, 84)], [16, 10])
    a.L(p.s, "grain", 5).blob(a.jp([(36, 44), (50, 40), (64, 44), (58, 90), (50, 94), (42, 90)], 1))
    d = a.L("#0c0a12", "none", 3, gloss=0)
    d.ell(11, 34, 4).ell(89, 34, 4)
    a.L(p.m, "grain", 6).poly([(50, 12), (60, 30), (40, 30)])
    t = a.L("#ffffff", "none", 2)
    for x in (42, 47, 53, 58):
        t.poly([(x - 2.2, 56), (x + 2.2, 56), (x, 62)])
    a.L(p.x, "glow", 3).ell(11, 34, 1.5).ell(89, 34, 1.5)


@shape
def talisman(a, p):
    a.L(LEATHER, "grain", 3).tube([(22, 8), (50, 34), (78, 8)], 1.8, 1.8)
    col = lt(p.s, .1)
    b = a.L(col, "rings" if p.pat == "metal" else "cracks", 9, gloss=.5)
    b.poly(a.jp([(50, 28), (76, 44), (66, 80), (50, 94), (34, 80), (24, 44)], 2))
    a.L(p.m, "glow", 3).tube([(50, 42), (50, 74)], 2, 2).tube([(38, 54), (62, 54)], 2, 2)
    a.L(p.x, "glow", 4).ell(50, 54, 5.5)


@shape
def seal(a, p):
    col = mix(p.m, p.s, .3)
    a.L(col, "metal", 9, gloss=.9).ell(50, 50, 40)
    a.L(dk(col, .4), "void", 5).ell(50, 50, 30)
    s = a.L(p.x, "glow", 3)
    n = a.rng.choice((3, 5, 6))
    pts = [(50 + 25 * math.cos(i * 2 * math.pi / n * (2 if n == 5 else 1) - 1.57), 50 + 25 * math.sin(i * 2 * math.pi / n * (2 if n == 5 else 1) - 1.57)) for i in range(n)]
    for i in range(n):
        s.tube([pts[i], pts[(i + 1) % n]], 1.8, 1.8)
    t = a.L(lt(col, .3), "none", 3, gloss=1)
    for i in range(8):
        k = i * .785
        t.ell(50 + 35 * math.cos(k), 50 + 35 * math.sin(k), 2.2)


@shape
def collar(a, p):
    a.L(LEATHER, "rings", 6).tube([(18, 30), (14, 58), (32, 80), (68, 80), (86, 58), (82, 30)], 7, 7)
    t = a.L(lt(p.s, .1), "none", 4, gloss=.7)
    for x, y, dx, dy in ((18, 62, -4, 18), (33, 80, -2, 18), (50, 84, 0, 16), (67, 80, 2, 18), (82, 62, 4, 18)):
        t.poly([(x - 5, y - 4), (x + 5, y - 4), (x + dx, y + dy - 4)])
    a.L(p.x, "metal", 4, gloss=1).ell(16, 26, 6).ell(84, 26, 6)
    a.L(p.m, "fur", 4).tube([(18, 38), (15, 56)], 3, 3)


@shape
def totem(a, p):
    a.L(p.s, "rings", 6).tube([(50, 94), (50, 20)], 8, 9)
    c = a.L(mix(IVORY, p.m, .25), "cracks", 9)
    c.blob(a.jp([(50, 10), (70, 22), (66, 50), (50, 58), (34, 50), (30, 22)], 1.5))
    d = a.L(dk(p.m, .7), "none", 3, gloss=0)
    d.ell(42, 30, 4.6).ell(58, 30, 4.6).poly([(50, 36), (47, 44), (53, 44)])
    f = a.L(p.x, "grain", 3)
    f.tube([(34, 58), (18, 76), (24, 88)], 3.4, 1).tube([(66, 58), (82, 76), (76, 88)], 3.4, 1)
    a.L(p.x, "grain", 3).tube([(40, 68), (60, 72)], 3, 3)


@shape
def pouch(a, p):
    a.L(p.m if False else LEATHER, "weave", 13).blob(a.jp([(36, 36), (64, 36), (84, 60), (76, 86), (50, 92), (24, 86), (16, 60)], 2))
    a.L(mix(LEATHER, p.s, .4), "grain", 6).poly(a.jp([(30, 12), (50, 22), (70, 12), (66, 34), (34, 34)], 1.5))
    a.L(p.x, "rings", 3).tube([(32, 34), (50, 38), (68, 34)], 3, 3)
    c = a.L(GOLD, "metal", 3, gloss=1)
    c.ell(58, 70, 5).ell(44, 76, 4)
    a.L("#d03a3a", "grain", 3).ell(16, 44, 2) if False else None


@shape
def vial_a(a, p):
    g = a.L(GLASS, "none", 8, gloss=1)
    g.ell(50, 62, 30)
    g.rect(40, 16, 60, 40)
    l = a.L(p.m, "glow", 12)
    l.ell(50, 64, 25.5)
    a.L(lt(p.m, .35), "none", 2).ell(50, a.j(44, 1), 24, 3)
    b = a.L(lt(p.x, .35), "glow", 3)
    for _ in range(4):
        b.ell(a.rng.uniform(36, 64), a.rng.uniform(54, 80), a.rng.uniform(1.6, 3.4))
    a.L(WOOD, "grain", 5).rect(38, 8, 62, 21)
    a.L("#ffffff", "none", 3).ell(32, 52, 3, 7, rot=20)


@shape
def bottle(a, p):
    a.L(p.m if False else dk(p.x, .1), "none", 12, gloss=1).blob(a.jp([(42, 34), (58, 34), (76, 52), (76, 88), (24, 88), (24, 52)], .6))
    a.L(dk(p.x, .1), "none", 5, gloss=1).rect(42, 12, 58, 40)
    a.L(PAPER, "grain", 3).rect(30, 56, 70, 78)
    c = a.L("#2a2420", "none", 3)
    c.ell(50, 64, 4.4)
    c.tube([(42, 73), (58, 73)], 1.2, 1.2)
    a.L(p.s, "metal", 4).rect(40, 7, 60, 16)


@shape
def urn(a, p):
    a.L(p.m, "cracks", 12, gloss=.3).blob(a.jp([(36, 26), (64, 26), (78, 50), (66, 84), (34, 84), (22, 50)], 1))
    a.L(dk(p.m, .2), "metal", 5).rect(36, 16, 64, 27)
    l = a.L(lt(p.m, .3), "grain", 4)
    l.tube([(25, 38), (12, 42), (22, 56)], 2.6, 2.6).tube([(75, 38), (88, 42), (78, 56)], 2.6, 2.6)
    a.L(p.s, "metal", 5).ell(50, 12, 14, 7)
    a.L(p.x, "grain", 5).tube([(26, 58), (50, 64), (74, 58)], 3, 3)


@shape
def lantern(a, p):
    f = a.L(lt(GLASS, .1), "frost", 7, glow="#ffffff", gloss=1)
    f.rect(26, 26, 74, 82)
    b = a.L(p.s, "none", 3)
    b.poly([(34, 34), (46, 30), (42, 44)]).poly([(58, 66), (68, 58), (66, 74)])
    a.L(p.x, "glow", 8).blob(a.jp([(50, 36), (60, 58), (50, 74), (40, 58)], 1.5))
    m = a.L(p.m, "metal", 4, gloss=.9)
    m.rect(20, 22, 80, 30).rect(20, 80, 80, 90).rect(22, 26, 28, 84).rect(72, 26, 78, 84)
    m.tube([(36, 22), (50, 6), (64, 22)], 2.4, 2.4)


@shape
def wick(a, p):
    a.L(p.m, "weave", 4).tube([(26, 92), (42, 66), (52, 44), (46, 22)], 6, 3.6)
    a.L(p.s, "smoke", 8).blob(a.jp([(46, 2), (58, 18), (54, 36), (40, 36), (34, 20)], 1.5))
    a.L(p.x, "glow", 5).ell(46, 26, 6, 8)
    a.L(dk(p.m, .3), "grain", 3).tube([(36, 78), (40, 70)], 2, 2)


@shape
def powderpaint(a, p):
    a.L(p.s if False else WOOD, "rings", 11).blob(a.jp([(14, 42), (86, 42), (78, 88), (22, 88)], .8))
    a.L(dk(WOOD, .3), "none", 7).ell(50, 42, 36, 13)
    a.L(p.m, "glow", 7).ell(50, 43, 31, 10)
    b = a.L(p.m, "grain", 4)
    b.tube([(30, 46), (28, 66)], 4, 3).tube([(62, 46), (64, 78)], 5, 3.4)
    a.L(lt(p.s, .1), "grain", 4).tube([(52, 40), (84, 8)], 3, 3)
    a.L(p.x, "glow", 4).ell(50, 40, 7, 3)


alias("paint", powderpaint)


@shape
def beetle(a, p):
    a.L(p.x, "metal", 11, gloss=1).ell(50, 58, 24, 32)
    a.L(dk(p.x, .65), "none", 2).tube([(50, 30), (50, 90)], 1.3, 1.3)
    a.L(mix(GOLD, p.m, .3), "metal", 6, gloss=1).ell(50, 26, 15, 11)
    l = a.L(GOLD, "metal", 3, gloss=1)
    for y, dx in ((38, 40), (56, 44), (74, 38)):
        l.tube([(32, y), (50 - dx, y + 7)], 2.3, 1.1).tube([(68, y), (50 + dx, y + 7)], 2.3, 1.1)
    l.tube([(42, 18), (34, 6)], 2, .8).tube([(58, 18), (66, 6)], 2, .8)
    a.L("#ffffff", "none", 3).ell(40, 46, 3, 8, rot=8)


@shape
def thornbase(a, p):
    a.L(p.s, "rings", 6).tube([(30, 94), (56, 54), (60, 30)], 7, 8)


@shape
def spine(a, p):
    col = mix(p.m, IVORY, .3)
    a.L(col, "rings", 8, gloss=.5).tube([(26, 88), (48, 56), (88, 12)], 11, .7)
    s = a.L(lt(col, .2), "none", 4, gloss=.7)
    s.poly([(38, 62), (20, 58), (40, 72)]).poly([(52, 48), (68, 58), (58, 42)])
    a.L(p.x, "glow", 4).ell(26, 88, 6)
    a.L(p.s, "grain", 5).tube([(14, 84), (28, 96), (38, 90)], 6, 5)


@shape
def bramble(a, p):
    l = a.L(p.s, "grain", 5)
    l.tube([(10, 88), (34, 56), (72, 62), (58, 28), (90, 10)], [7, 6, 6, 5, 2])
    t = a.L(p.m, "grain", 3)
    for (x, y, dx, dy) in ((26, 68, -10, -8), (36, 54, 2, -12), (58, 64, 8, 10), (72, 54, 11, 0), (58, 34, -11, -2), (74, 22, 4, 11)):
        t.poly([(x - 3.6, y), (x + 3.6, y), (x + dx, y + dy)])
    a.L(p.x, "glow", 4).ell(a.j(88, 1), 12, 4.2).ell(45, 58, 3)


@shape
def vine(a, p):
    a.L(p.m, "grain", 5).tube([(12, 90), (40, 70), (26, 46), (58, 36), (46, 12)], 5, 3)
    f = a.L(p.s, "veins", 5, glow=lt(p.s, .4))
    for x, y, r in ((46, 76, 25), (20, 40, -130), (66, 26, -30), (34, 56, 170)):
        f.ell(x + 8 * math.cos(math.radians(r)), y + 8 * math.sin(math.radians(r)), 11, 6, rot=r)
    a.L(p.x, "glow", 3).ell(45, 12, 4)


@shape
def root(a, p):
    l = a.L(p.m, "rings", 7)
    l.tube([(50, 8), (50, 44)], 11, 9)
    l.tube([(48, 42), (28, 60), (14, 88)], 7, 1)
    l.tube([(52, 42), (54, 66), (44, 94)], 7, 1)
    l.tube([(54, 40), (72, 56), (88, 78)], 6, 1)
    a.L(p.s, "grain", 5).tube([(38, 8), (50, 2), (62, 8)], 6, 6)
    a.L(p.x, "glow", 3).ell(50, 26, 3.4).ell(46, 40, 2.6)


@shape
def bark(a, p):
    a.L(p.m if False else p.s, "stripes", 7).poly(a.jp([(22, 10), (70, 6), (82, 40), (76, 92), (28, 90), (18, 52)], 3))
    g = a.L(dk(p.s, .5), "none", 2)
    for x in (34, 48, 62):
        g.tube(a.jp([(x, 12), (x + 4, 46), (x - 2, 86)], 3), 1.8, 1)
    a.L(p.m, "spots", 5).ell(a.j(62, 4), a.j(30, 4), 11, 8)
    a.L(p.x, "glow", 3).ell(a.j(38, 3), a.j(68, 3), 3.4)


@shape
def seed(a, p):
    a.L(dk(p.s, .3), "stripes", 13, gloss=.7).blob(a.jp([(50, 10), (70, 38), (72, 68), (50, 90), (28, 68), (30, 38)], 1.5))
    a.L(p.x, "glow", 4).tube([(50, 26), (50, 74)], 2.4, 1.1)
    s = a.L(p.m, "veins", 3, glow=p.x)
    s.tube([(50, 12), (46, 2), (60, 0)], 2.6, 1).tube([(40, 76), (24, 92)], 2.4, .8).tube([(60, 76), (76, 92)], 2.4, .8)


@shape
def flower(a, p):
    n = a.rng.choice((5, 6, 7))
    r0 = a.rng.uniform(0, 6.3)
    l = a.L(p.x, "veins", 8, glow=lt(p.x, .5), gloss=.5)
    for i in range(n):
        t = r0 + i * 2 * math.pi / n
        l.ell(50 + 27 * math.cos(t), 50 + 27 * math.sin(t), 21, 13, rot=math.degrees(t))
    q = a.L(lt(p.x, .25), "grain", 4)
    for i in range(n):
        t = r0 + (i + .5) * 2 * math.pi / n
        q.ell(50 + 19 * math.cos(t), 50 + 19 * math.sin(t), 11, 6, rot=math.degrees(t))
    a.L(p.s, "spots", 7).ell(50, 50, 12)
    a.L(lt(p.s, .5), "none", 3).ell(47, 47, 3)


@shape
def silk(a, p):
    a.L(p.m, "rings", 14, gloss=.5).blob(a.jp([(50, 14), (74, 28), (80, 56), (64, 80), (36, 80), (20, 56), (26, 28)], 1.5))
    f = a.L(lt(p.m, .6), "none", 2)
    for y in (32, 44, 56, 68):
        f.tube([(a.j(26, 2), y + 4), (50, y - 6), (a.j(74, 2), y + 4)], .9, .9)
    a.L(p.m, "grain", 2).tube([(70, 72), (88, 90), (66, 94)], 1.2, .8)
    a.L(p.x, "glow", 2).ell(66, 94, 2.4)


@shape
def gill(a, p):
    l = a.L(p.m, "frost", 8, glow="#ffffff")
    for i in range(5):
        y = 16 + i * 15
        l.tube([(14 + i * 3, y + 6), (50, y), (86 - i * 3, y + 6)], [4.5, 7, 4.5])
    a.L(p.s, "rings", 5).tube([(10, 18), (22, 50), (28, 84)], 4, 4)
    d = a.L(dk(p.m, .5), "none", 2)
    for i in range(4):
        d.tube([(30, 30 + i * 15), (76, 30 + i * 15)], .9, .9)
    a.L(p.x, "glow", 3).ell(70, 80, 3)


@shape
def blubber(a, p):
    a.L(p.m, "grain", 16, gloss=.7).blob(a.jp([(12, 54), (26, 24), (58, 16), (88, 36), (88, 66), (60, 86), (26, 80)], 3))
    a.L(dk(p.s, .1), "grain", 5).tube([(14, 40), (38, 18), (82, 22)], 6, 5)
    a.L(lt(p.m, .55), "none", 3).ell(46, 44, 12, 5, rot=-15)
    a.L(p.x, "grain", 5).ell(a.j(64, 3), a.j(62, 3), 7, 5)


@shape
def comb(a, p):
    a.L(p.m, "grain", 6).tube([(10, 20), (50, 12), (90, 20)], 8, 8)
    t = a.L(p.s, "rings", 3)
    for i in range(9):
        x = 14 + i * 9
        t.poly([(x - 3.8, 20), (x + 3.8, 20), (x + a.j(0, 1.5), a.j(92, 4) - abs(i - 4) * 5)])
    a.L(p.x, "metal", 3).ell(50, 12, 5)


@shape
def amber(a, p):
    a.L(p.x, "smoke", 15, gloss=.9).blob(a.jp([(20, 40), (40, 16), (70, 20), (86, 50), (70, 80), (38, 86), (16, 66)], 3.5))
    a.L(dk(p.s, .1), "grain", 4).ell(a.j(52, 4), a.j(54, 4), 9, 6, rot=a.j(0, 40))
    a.L("#ffffff", "none", 3).ell(38, 34, 6, 3.5, rot=-30)


@shape
def song(a, p):
    a.L(p.x, "facets", 9, gloss=.9).poly(a.jp([(50, 6), (86, 28), (86, 72), (50, 94), (14, 72), (14, 28)], 2))
    w = a.L(p.m, "glow", 3)
    pts = [(14 + i * 6, 50 + 22 * math.sin(i * .9) * math.exp(-((i - 6) / 5.0) ** 2)) for i in range(13)]
    w.tube(pts, 2.2, 2.2)
    a.L("#ffffff", "none", 2).poly([(36, 18), (46, 14), (34, 34)])


@shape
def laugh(a, p):
    a.L(p.m, "spots", 11).ell(50, 52, 39, 35)
    t = a.L("#120c10", "none", 6, gloss=0)
    t.blob([(16, 48), (50, 54), (84, 48), (66, 80), (50, 84), (34, 80)])
    w = a.L(p.s, "none", 3, gloss=.5)
    for x in (26, 38, 50, 62, 74):
        w.poly([(x - 5, 49), (x + 5, 49), (x, 63)])
    d = a.L(p.x, "glow", 4)
    d.ell(34, 32, 6).ell(66, 32, 6)
    a.L("#120c10", "none", 2).ell(34, 32, 2, 4.5).ell(66, 32, 2, 4.5)


@shape
def prism(a, p):
    a.L(p.m, "facets", 8, glow=lt(p.x, .4), gloss=1).poly(a.jp([(50, 6), (90, 84), (10, 84)], 2))
    a.L(lt(p.m, .5), "none", 3).poly([(50, 18), (58, 46), (36, 56)])
    a.L(p.s, "glow", 4).tube([(50, 40), (66, 66)], 3.4, 1.4)
    b = a.L(p.x, "glow", 3)
    b.tube([(66, 66), (96, 90)], 2, 1).tube([(50, 40), (6, 26)], 2, 1)


@shape
def keystone(a, p):
    a.L(p.s, "facets", 9, gloss=.7).poly(a.jp([(22, 12), (78, 12), (64, 88), (36, 88)], 2))
    a.L(dk(p.s, .5), "cracks", 4).poly(a.jp([(34, 24), (66, 24), (58, 76), (42, 76)], 1))
    k = a.L(p.x, "glow", 3)
    k.ell(50, 38, 7)
    k.ell(50, 38, 3.4, val=0)
    k.tube([(50, 45), (50, 70)], 2.2, 2.2).tube([(50, 60), (57, 60)], 1.6, 1.6).tube([(50, 67), (57, 67)], 1.6, 1.6)


# ---------------- Styles supplementaires des formes frequentes ----------------
def _cord(a, pts, col, r=2.2):
    a.L(col, "rings", 3).tube(pts, r, r)


def hide_b(a, p):                                                       # peau roulee et ficelee
    a.L(p.m, p.pat, 11).tube([(12, 68), (50, 52), (84, 34)], [17, 21, 19])
    a.L(dk(p.m, .3), p.pat, 7).ell(84, 34, 18, 18)
    pts = [(84 + (2 + t * 2.3) * math.cos(t), 34 + (2 + t * 2.3) * math.sin(t)) for t in [i * .5 for i in range(14)]]
    a.L(lt(p.s, .25), "none", 2).tube(pts, 1.2, 1.5)
    for x, y in ((32, 62), (58, 48)):
        _cord(a, [(x - 9, y - 19), (x, y), (x + 9, y + 19)], p.x)


def hide_c(a, p):                                                       # peau tendue sur un cadre
    a.L(p.m, p.pat, 8).blob(a.jp([(22, 20), (50, 26), (78, 20), (74, 50), (80, 80), (50, 74), (20, 80), (26, 50)], 2.5))
    f = a.L(WOOD, "rings", 5)
    f.tube([(10, 12), (90, 12)], 4, 4).tube([(10, 88), (90, 88)], 4, 4).tube([(12, 10), (12, 90)], 4, 4).tube([(88, 10), (88, 90)], 4, 4)
    l = a.L(lt(p.s, .2), "none", 2)
    for u, v, w, z in ((14, 14, 24, 22), (86, 14, 76, 22), (14, 86, 24, 78), (86, 86, 76, 78), (50, 14, 50, 28), (50, 86, 50, 72), (14, 50, 27, 50), (86, 50, 73, 50)):
        l.tube([(u, v), (w, z)], 1.1, 1.1)
    a.L(p.x, "glow", 3).ell(50, 50, 4)


def hide_d(a, p):                                                       # pile de peaux pliees
    for y, c, w in ((72, dk(p.m, .25), 40), (52, dk(p.m, .1), 37), (32, p.m, 34)):
        a.L(c, p.pat, 8).blob(a.jp([(50 - w, y - 9), (50, y - 13), (50 + w, y - 9), (50 + w, y + 8), (50, y + 12), (50 - w, y + 8)], 1.2))
    _cord(a, [(42, 16), (46, 50), (42, 86)], p.x, 2.6)
    a.L(lt(p.x, .2), "grain", 3).ell(44, 18, 5, 4)


def hide_e(a, p):                                                       # depouille avec tete et oreilles
    a.L(p.m, p.pat, 10).blob(a.jp([(50, 26), (70, 30), (88, 52), (74, 58), (72, 88), (50, 80), (28, 88), (26, 58), (12, 52), (30, 30)], 2))
    h = a.L(dk(p.m, .12), p.pat, 8)
    h.blob(a.jp([(34, 26), (50, 14), (66, 26), (62, 42), (50, 50), (38, 42)], 1))
    h.poly([(35, 24), (32, 6), (46, 18)]).poly([(65, 24), (68, 6), (54, 18)])
    a.L(p.x, "glow", 3).ell(43, 30, 2.6).ell(57, 30, 2.6)
    a.L(p.s, "grain", 3).tube([(50, 52), (50, 76)], 1.8, .8)


variants = {"hide": [hide_a, hide_b, hide_c, hide_d, hide_e]}


def scale_b(a, p):                                                      # grande ecaille unique, nervuree et dentelee
    a.L(p.m, "rings", 10, gloss=.8).poly(a.jp([(50, 6), (68, 18), (64, 26), (80, 38), (74, 46), (84, 62), (66, 70), (58, 92), (42, 92), (34, 70), (16, 62), (26, 46), (20, 38), (36, 26), (32, 18)], 1.2))
    a.L(lt(p.s, .2), "grain", 3).tube([(50, 10), (50, 90)], 2.4, 1.2).tube([(50, 40), (28, 30)], 1.3, .6).tube([(50, 40), (72, 30)], 1.3, .6).tube([(50, 60), (28, 52)], 1.3, .6).tube([(50, 60), (72, 52)], 1.3, .6)
    a.L(p.x, "glow", 3).ell(50, 24, 3)


def scale_c(a, p):                                                      # rangee d'ecailles imbriquees
    for i, (y, c) in enumerate(((22, dk(p.m, .25)), (44, dk(p.m, .1)), (66, p.m))):
        a.L(c, "metal", 9, gloss=.8).blob(a.jp([(50 - 26, y - 8), (50, y - 16), (50 + 26, y - 8), (50 + 20, y + 12), (50, y + 25), (50 - 20, y + 12)], 1.2))
    a.L(p.s, "grain", 3).tube([(50, 58), (50, 86)], 2, .8)


variants["scale"] = [scale_a, scale_b, scale_c]


def claw_b(a, p):                                                       # griffe double
    bone = mix(IVORY, p.m, .45)
    a.L(dk(bone, .12), "grain", 7, gloss=.6).tube([(54, 28), (76, 44), (84, 84)], [12, 9, .8])
    a.L(bone, "grain", 8, gloss=.6).tube([(40, 30), (28, 58), (44, 92)], [14, 11, .8])
    a.L(p.s, p.pat, 8).blob(a.jp([(24, 14), (50, 8), (76, 18), (72, 36), (46, 40), (26, 34)], 1.5))
    a.L(p.x, "glow", 3).ell(50, 24, 4)


def claw_c(a, p):                                                       # griffe en crochet sur moignon de fourrure
    bone = mix(IVORY, p.m, .55)
    a.L(bone, "rings", 9, gloss=.7).tube([(38, 66), (66, 66), (84, 40), (64, 14)], [13, 13, 8, .8])
    a.L(p.s, "fur", 11).blob(a.jp([(10, 62), (26, 44), (44, 50), (50, 72), (40, 88), (18, 88)], 2))
    a.L(p.x, "grain", 3).tube([(44, 52), (50, 66), (44, 84)], 2.4, 2.4)


variants["claw"] = [claw_a, claw_b, claw_c]


def fang_b(a, p):                                                       # paire de crocs
    col = mix(IVORY, p.m, .25)
    _cone(a, (66, 16), (58, 90), 12, 13, dk(col, .12))
    _cone(a, (34, 16), (42, 88), -12, 15, col)
    a.L(mix(BLOOD, p.x, .5), "grain", 7).tube([(14, 12), (50, 20), (86, 12)], 9, 9)


def fang_c(a, p):                                                       # croc en pendentif
    _cord(a, [(14, 8), (50, 40), (86, 8)], LEATHER, 1.6)
    col = mix(IVORY, p.m, .3)
    _cone(a, (50, 40), (46, 94), a.j(-9, 2), 15, col)
    b = a.L(p.x, "metal", 3, gloss=1)
    b.ell(50, 36, 7, 6).ell(32, 25, 3.6).ell(68, 25, 3.6)


def fang_d(a, p):                                                       # croc brise avec racine double
    col = mix(IVORY, p.m, .42)
    a.L(col, "cracks", 9, gloss=.4).poly(a.jp([(30, 14), (72, 18), (70, 50), (56, 92), (46, 68), (38, 72), (32, 48)], 1.6))
    a.L(mix(BLOOD, p.x, .4), "veins", 4, glow=lt(p.x, .3)).tube([(32, 14), (50, 20), (70, 16)], 5, 5)


variants["fang"] = [fang_a, fang_b, fang_c, fang_d]


def tooth_b(a, p):                                                      # incisive plate
    col = mix(IVORY, p.m, .35)
    a.L(col, p.pat if p.pat in ("metal", "spots", "cracks") else "grain", 9, gloss=.7).poly(a.jp([(26, 12), (74, 12), (70, 46), (62, 66), (54, 94), (46, 94), (38, 66), (30, 46)], 1.2))
    a.L(lt(col, .4), "none", 3).tube([(36, 20), (34, 42)], 2.4, 1.2)
    a.L(dk(col, .4), "none", 2).tube([(60, 12), (56, 34)], 1.2, .7)


def tooth_c(a, p):                                                      # dent creuse (carie, poison)
    col = mix(IVORY, p.m, .45)
    a.L(col, "spots", 10, gloss=.4).blob(a.jp([(50, 8), (72, 26), (70, 54), (58, 90), (50, 74), (42, 90), (30, 54), (28, 26)], 1.5))
    a.L(dk(p.m, .6), "none", 5, gloss=0).ell(a.j(52, 3), a.j(36, 3), 12, 10)
    a.L(p.x, "glow", 3).ell(a.j(52, 2), a.j(38, 2), 4.4)


def tooth_d(a, p):                                                      # rangee de dents sur un bout de gencive
    a.L(mix(BLOOD, p.m, .35), "grain", 7).tube([(10, 30), (50, 22), (90, 30)], 12, 12)
    col = mix(IVORY, p.m, .25)
    for x, h in ((24, 62), (41, 84), (59, 88), (76, 66)):
        a.L(col, "grain", 6, gloss=.7).poly(a.jp([(x - 9, 32), (x + 9, 32), (x + a.j(0, 2), h)], .8))


variants["tooth"] = [tooth_a, tooth_b, tooth_c, tooth_d]


def bone_b(a, p):                                                       # cote courbe
    l = a.L(p.m, p.pat, 7, gloss=.3)
    l.tube([(18, 88), (22, 44), (56, 18), (88, 26)], [8, 8.5, 7.5, 4])
    a.L(dk(p.m, .3), "none", 2).tube([(20, 76), (25, 46), (54, 25)], 1, 1)
    a.L(p.x, "grain", 5).ell(18, 90, 10, 7)


def bone_c(a, p):                                                       # vertebre
    a.L(p.m, p.pat, 8).poly(a.jp([(50, 6), (58, 30), (92, 40), (62, 54), (72, 92), (50, 74), (28, 92), (38, 54), (8, 40), (42, 30)], 1.5))
    a.L(lt(p.m, .15), p.pat, 10).ell(50, 44, 21, 19)
    a.L(dk(p.m, .7), "none", 4, gloss=0).ell(50, 44, 8.5, 8)
    a.L(p.x, "glow", 3).ell(50, 44, 2.6)


def bone_d(a, p):                                                       # os croises
    for x0, y0, x1, y1, c in ((22, 22, 78, 78, dk(p.m, .14)), (78, 22, 22, 78, p.m)):
        l = a.L(c, p.pat, 7, gloss=.3)
        l.tube([(x0, y0), (x1, y1)], 6.5, 6.5)
        for px, py in ((x0, y0), (x1, y1)):
            dx, dy = (x1 - x0) * .13, (y1 - y0) * .13
            l.ell(px - dy * .75, py + dx * .75, 9).ell(px + dy * .75, py - dx * .75, 9)
    a.L(p.x, "glow", 3).ell(50, 50, 3.4)


variants["bone"] = [bone_a, bone_b, bone_c, bone_d]


def skull_b(a, p):                                                      # crane de profil a machoire
    c = a.L(p.m, p.pat, 11, gloss=.3)
    c.blob(a.jp([(66, 10), (88, 30), (86, 54), (70, 66), (56, 72), (24, 72), (12, 56), (26, 44), (34, 18)], 1.4))
    j = a.L(dk(p.m, .14), p.pat, 7)
    j.blob(a.jp([(70, 66), (60, 90), (26, 90), (16, 80), (40, 78), (54, 72)], 1.2))
    d = a.L(dk(p.m, .78), "none", 4, gloss=0)
    d.ell(42, 44, 10, 9).poly([(20, 60), (28, 52), (30, 64)])
    t = a.L(lt(p.m, .4), "none", 2)
    for x in (20, 27, 34, 41, 48):
        t.poly([(x - 2.5, 71), (x + 2.5, 71), (x, 80)])
    a.L(p.x, "glow", 4, glow=lt(p.x, .5)).ell(42, 45, 3.6)


def skull_c(a, p):                                                      # crane allonge couronne de pointes
    c = a.L(p.m, "cracks", 11, gloss=.3)
    c.blob(a.jp([(50, 20), (76, 26), (82, 50), (70, 68), (62, 88), (38, 88), (30, 68), (18, 50), (24, 26)], 1.3))
    s = a.L(lt(p.s, .1), "rings", 4)
    for x, y, tx in ((30, 26, 22), (42, 22, 38), (58, 22, 62), (70, 26, 78)):
        s.tube([(x, y), (tx, 4)], 4.4, .7)
    d = a.L(dk(p.m, .8), "none", 5, gloss=0)
    d.poly([(28, 44), (46, 46), (44, 60), (32, 60)]).poly([(72, 44), (54, 46), (56, 60), (68, 60)])
    d.ell(50, 70, 3.4, 4.5)
    a.L(p.x, "glow", 4).ell(38, 52, 3).ell(62, 52, 3)


variants["skull"] = [skull_a, skull_b, skull_c]


def eye_b(a, p):                                                        # oeil en amande, paupieres
    a.L(p.s, "veins", 8, glow=dk(BLOOD, .1), gloss=.6).blob([(6, 50), (28, 26), (50, 20), (72, 26), (94, 50), (72, 74), (50, 80), (28, 74)])
    a.L(p.x, "facets", 6).ell(a.j(50, 3), 50, 21)
    a.L("#100a14", "none", 3, gloss=0).ell(50, 50, a.j(5, 2), 18)
    a.L("#ffffff", "none", 2).ell(40, 40, 5, 3, rot=-30)
    l = a.L(dk(p.m, .1), p.pat, 5)
    l.tube([(4, 50), (28, 22), (50, 16), (72, 22), (96, 50)], 4, 4)
    l.tube([(4, 50), (28, 78), (50, 84), (72, 78), (96, 50)], 3.4, 3.4)


def eye_c(a, p):                                                        # oeil et son nerf
    a.L(mix(BLOOD, p.m, .3), "veins", 5, glow=lt(p.x, .3)).tube([(62, 62), (78, 82), (70, 94)], 8, 2)
    a.L(p.s, "veins", 15, glow=dk(BLOOD, .1), gloss=.8).ell(42, 42, 32)
    a.L(p.x, "rings", 7).ell(a.j(36, 2), a.j(38, 2), 16)
    a.L("#100a14", "none", 4, gloss=0).ell(36, 38, 7.5)
    a.L("#ffffff", "none", 2).ell(31, 32, 3.4, 2.4, rot=-30)


variants["eye"] = [eye_a, eye_b, eye_c]


def core_b(a, p):                                                       # gemme dans une cage
    c = a.L(dk(p.s, .15), "metal", 4, gloss=.9)
    c.tube([(50, 4), (88, 50), (50, 96), (12, 50), (50, 4)], 3.4, 3.4)
    c.tube([(50, 6), (50, 94)], 2, 2).tube([(14, 50), (86, 50)], 2, 2)
    a.L(p.x, "facets", 7, gloss=1).poly(a.jp([(50, 22), (74, 50), (50, 78), (26, 50)], 1.5))
    a.L("#ffffff", "none", 3).poly([(50, 28), (58, 42), (44, 44)])


def core_c(a, p):                                                       # orbe et anneaux en orbite
    r = a.L(p.s, "metal", 3, gloss=1)
    r.tube([(8, 66), (50, 80), (92, 34), (50, 20), (8, 66)], 2.2, 2.2)
    a.L(p.m, p.pat if p.pat in ("lava", "cracks", "frost", "void") else "smoke", 16, glow=p.x, gloss=.6).ell(50, 50, 26)
    a.L(p.x, "glow", 8).ell(50, 50, 10)
    a.L(lt(p.s, .3), "metal", 3).tube([(26, 74), (50, 80), (74, 60)], 3, 3)


def core_d(a, p):                                                       # bloc-noyau a runes
    a.L(dk(p.m, .2), "metal", 8, gloss=.8).poly(a.jp([(50, 8), (90, 30), (90, 70), (50, 92), (10, 70), (10, 30)], 1))
    a.L(p.m, p.pat if p.pat in ("lava", "cracks", "frost") else "metal", 5, glow=p.x).poly([(50, 12), (86, 32), (50, 52), (14, 32)])
    l = a.L(p.x, "glow", 3)
    l.tube([(30, 54), (42, 62), (30, 74)], 2, 2).tube([(70, 54), (58, 62), (70, 74)], 2, 2)
    l.ell(50, 32, 6)


variants["core"] = [core_a, core_b, core_c, core_d]


def gland_b(a, p):                                                      # sac allonge a double canal
    a.L(p.m, "veins", 12, glow=lt(p.x, .1), gloss=.8).tube([(30, 72), (48, 48), (72, 30)], [17, 21, 13])
    l = a.L(p.s, "rings", 4)
    l.tube([(78, 26), (92, 8)], 5, 2.6).tube([(74, 24), (70, 4)], 4, 2)
    a.L(p.x, "glow", 7).ell(44, 54, 10, 7, rot=-40)
    d = a.L(p.x, "glow", 3)
    d.ell(92, 6, 3)
    d.ell(70, 2, 2)


variants["gland"] = [gland_a, gland_b]


def heart_b(a, p):                                                      # coeur fendu par une lueur
    m = mix(p.m, BLOOD, .1)
    a.L(p.s, "rings", 7).tube([(30, 30), (20, 12)], 7, 5).tube([(48, 26), (46, 8)], 8, 6)
    a.L(m, "cracks", 15, gloss=.5).blob(a.jp([(50, 22), (76, 26), (86, 48), (72, 72), (54, 92), (28, 74), (14, 48), (24, 26)], 2))
    a.L(p.x, "glow", 3, glow="#ffffff").tube(a.jp([(58, 26), (46, 44), (58, 56), (46, 72), (52, 86)], 1.5), 3, 1)


def heart_c(a, p):                                                      # coeur herisse
    m = mix(p.m, BLOOD, .1)
    s = a.L(lt(p.s, .1), "rings", 4)
    for t in (.2, 1.0, 1.9, 2.8, 3.6, 4.4, 5.2, 6.0):
        s.poly([(50 + 25 * math.cos(t - .3), 52 + 25 * math.sin(t - .3)), (50 + 25 * math.cos(t + .3), 52 + 25 * math.sin(t + .3)), (50 + 47 * math.cos(t), 52 + 47 * math.sin(t))])
    a.L(m, "veins", 15, glow=lt(p.x, .25), gloss=.6).blob(a.jp([(36, 26), (50, 32), (64, 26), (78, 42), (72, 64), (50, 82), (28, 64), (22, 42)], 1.5))
    a.L(lt(m, .55), "none", 3).ell(38, 42, 5, 3, rot=-40)


def heart_d(a, p):                                                      # coeur enchaine
    m = mix(p.m, BLOOD, .1)
    a.L(m, p.pat if p.pat in ("lava", "frost", "void", "spots") else "smoke", 15, glow=lt(p.x, .2), gloss=.6).blob(a.jp([(50, 26), (66, 16), (86, 30), (82, 56), (50, 90), (18, 56), (14, 30), (34, 16)], 1.5))
    c = a.L(lt(p.s, .15), "metal", 3, gloss=1)
    for x0, y0, x1, y1 in ((8, 26, 92, 62), (92, 22, 10, 64)):
        for i in range(8):
            t = i / 7
            x, y = x0 + (x1 - x0) * t, y0 + (y1 - y0) * t
            c.ell(x, y, 4.4, 3.2, rot=25 if x1 > x0 else -25)
            c.ell(x, y, 1.8, 1.2, val=0)


variants["heart"] = [heart_a, heart_b, heart_c, heart_d]


def vial_b(a, p):                                                       # tube a essai incline
    a.L(GLASS, "none", 6, gloss=1).tube([(24, 84), (70, 30)], 13, 13)
    a.L(p.m, "glow", 8).tube([(26, 82), (56, 46)], 10.5, 10.5)
    a.L(WOOD, "grain", 4).tube([(70, 30), (80, 18)], 13, 13)
    b = a.L(lt(p.x, .35), "glow", 3)
    b.ell(36, 68, 3).ell(46, 58, 2)


def vial_c(a, p):                                                       # flacon conique
    a.L(GLASS, "none", 6, gloss=1).poly([(40, 10), (60, 10), (60, 36), (88, 90), (12, 90), (40, 36)])
    a.L(p.m, "glow", 8).poly([(38, 52), (62, 52), (82, 87), (18, 87)])
    a.L(lt(p.m, .4), "none", 2).ell(50, 52, 12, 2)
    a.L(lt(p.x, .35), "glow", 3).ell(a.j(44, 3), 70, 3.4).ell(a.j(60, 3), 76, 2.4)
    a.L(p.s, "metal", 4).rect(36, 6, 64, 14)


def vial_d(a, p):                                                       # fiole plate etiquetee
    a.L(dk(p.m, .1), "glow", 11, gloss=1).blob(a.jp([(38, 26), (62, 26), (82, 42), (82, 86), (18, 86), (18, 42)], .6))
    a.L(GLASS, "none", 4, gloss=1).rect(40, 12, 60, 28)
    a.L(PAPER, "grain", 3).rect(30, 50, 70, 74)
    a.L(p.x, "glow", 3).ell(50, 62, 6)
    a.L(WOOD, "grain", 4).rect(38, 6, 62, 15)


variants["vial"] = [vial_a, vial_b, vial_c, vial_d]


def soul_b(a, p):                                                       # flamme d'ame en spirale
    pts = [(50 + (30 - t * 3) * math.cos(t), 56 + (30 - t * 3) * math.sin(t) - t * 3) for t in [i * .45 for i in range(19)]]
    a.L(p.s, "smoke", 8).tube(pts, 12, 1)
    a.L(p.m, "glow", 9).ell(52, 62, 14)
    a.L("#ffffff", "glow", 4).ell(52, 62, 5)


def soul_c(a, p):                                                       # orbe a queue de comete
    a.L(p.s, "smoke", 8).tube([(84, 12), (70, 30), (58, 44), (46, 58)], [1, 8, 15, 21])
    a.L(p.m, "glow", 18).ell(40, 62, 26)
    f = a.L(dk(p.s, .6), "none", 3, gloss=0)
    f.ell(33, 58, 3).ell(47, 58, 3)
    f.tube([(32, 70), (40, 74), (48, 70)], 1.4, 1.4)


variants["soul"] = [soul_a, soul_b, soul_c]


def meat_b(a, p):                                                       # flanc et cotes
    a.L(p.m, p.pat, 11).blob(a.jp([(12, 30), (50, 16), (88, 30), (86, 72), (50, 86), (14, 72)], 2.5))
    r = a.L(IVORY, "grain", 4)
    for x in (28, 43, 58, 73):
        r.tube([(x, 26), (x + 2, 74)], 4, 3)
    a.L(lt(p.s, .3), "grain", 3).tube([(14, 50), (50, 56), (86, 50)], 2.2, 2.2)


def meat_c(a, p):                                                       # morceau sur l'os
    a.L(IVORY, "grain", 5).tube([(26, 80), (10, 94)], 6, 6)
    a.L(IVORY, "grain", 5).ell(8, 90, 6).ell(14, 97, 6)
    a.L(p.m, p.pat, 14).blob(a.jp([(22, 66), (32, 34), (58, 14), (88, 22), (86, 52), (62, 76), (36, 82)], 3))
    a.L(lt(p.s, .3), "grain", 4).tube(a.jp([(44, 42), (60, 30), (74, 36)], 2), 2.2, 1.2)
    a.L(p.x, "glow", 3).ell(a.j(54, 4), a.j(58, 4), 4, 3)


variants["meat"] = [meat_a, meat_b, meat_c]


def shard_b(a, p):                                                      # double eclat diagonal
    a.L(dk(p.m, .12), "facets", 8, glow=lt(p.x, .4), gloss=.8).poly(a.jp([(78, 12), (90, 34), (56, 82), (42, 60)], 2))
    a.L(p.m, "facets", 8, glow=lt(p.x, .4), gloss=.9).poly(a.jp([(20, 16), (40, 8), (70, 62), (56, 92), (36, 76)], 2))
    a.L(lt(p.m, .5), "none", 3).poly(a.jp([(27, 16), (37, 13), (50, 42), (40, 44)], 1))
    a.L(p.s, "cracks", 5).ell(40, 88, 24, 7)


def shard_c(a, p):                                                      # gros cristal hexagonal casse
    a.L(p.m, "facets", 10, glow=lt(p.x, .4), gloss=.9).poly(a.jp([(32, 10), (68, 10), (86, 44), (72, 86), (50, 74), (28, 86), (14, 44)], 2.5))
    a.L(lt(p.m, .45), "none", 3).poly(a.jp([(36, 16), (50, 16), (44, 42), (24, 42)], 1))
    a.L(dk(p.m, .35), "none", 3).poly(a.jp([(56, 44), (80, 46), (70, 80), (54, 68)], 1.2))


variants["shard"] = [shard_a, shard_b, shard_c]


def horn_b(a, p):                                                       # corne droite annelee
    col = mix(p.m, IVORY, .25)
    a.L(col, "rings", 10, gloss=.5).tube([(24, 88), (52, 54), (86, 8)], 15, .8)
    a.L(p.x, p.pat if p.pat in ("lava", "frost") else "metal", 5, glow=lt(p.x, .3)).tube([(10, 78), (22, 92), (36, 96)], 6, 6)


def horn_c(a, p):                                                       # corne de belier enroulee
    col = mix(p.m, IVORY, .3)
    pts = [(46 + (38 - t * 5.2) * math.cos(t + 1.9), 52 + (38 - t * 5.2) * math.sin(t + 1.9)) for t in [i * .45 for i in range(14)]]
    a.L(col, "rings", 9, gloss=.5).tube(pts, 15, 1.4)
    a.L(p.x, "glow", 3).ell(a.j(30, 1), 86, 3.4)


variants["horn"] = [horn_a, horn_b, horn_c]


def jaw_b(a, p):                                                        # machoire de profil
    col = mix(IVORY, p.m, .4)
    a.L(col, "cracks", 9, gloss=.2).tube([(90, 26), (76, 62), (40, 76), (10, 62)], [11, 12, 10, 6])
    t = a.L(lt(IVORY, .3), "none", 4, gloss=.6)
    for x, y in ((64, 58), (50, 64), (36, 64), (22, 58)):
        t.poly([(x - 6, y + 3), (x + 6, y + 3), (x - 2, y - 26)])
    a.L(p.x, "veins", 3, glow=lt(p.x, .4)).ell(82, 40, 4.6)
    a.L(p.s, "grain", 3).tube([(90, 18), (96, 6)], 4, 2)


variants["jaw"] = [jaw_a, jaw_b]


def carapace_b(a, p):                                                   # carapace segmentee
    for i, (y, w, h) in enumerate(((16, 16, 11), (31, 30, 13), (48, 37, 14), (66, 31, 13), (82, 18, 11))):
        a.L(p.m if i % 2 == 0 else dk(p.m, .15), p.pat if p.pat in ("metal", "rings") else "metal", 8, gloss=.9).blob(a.jp([(50 - w, y), (50, y - h), (50 + w, y), (50, y + h)], .8))
    a.L(p.s, "grain", 3).tube([(50, 8), (50, 92)], 2, 1)
    a.L(p.x, "glow", 3).ell(50, 48, 3.2)


variants["carapace"] = [carapace_a, carapace_b]


def plate_b(a, p):                                                      # epauliere
    a.L(p.m, p.pat if p.pat in ("void", "cracks") else "metal", 13, glow=p.x, gloss=1).blob(a.jp([(10, 72), (20, 32), (50, 14), (80, 32), (90, 72), (50, 58)], 1))
    a.L(dk(p.s, .1), "metal", 5, gloss=1).tube([(12, 76), (50, 62), (88, 76)], 5, 5)
    s = a.L(lt(p.s, .4), "metal", 4, gloss=1)
    for x, y, tx, ty in ((32, 30, 20, 8), (50, 22, 50, 2), (68, 30, 80, 8)):
        s.tube([(x, y), (tx, ty)], 5, .8)


variants["plate"] = [plate_a, plate_b]


def crown_b(a, p):                                                      # diademe de pointes
    col = mix(p.m, GOLD, .35)
    r = a.L(col, "metal", 5, gloss=1)
    r.tube([(10, 62), (30, 78), (70, 78), (90, 62)], 6, 6)
    for x, y, tx, ty in ((16, 62, 8, 22), (32, 72, 28, 12), (50, 76, 50, 4), (68, 72, 72, 12), (84, 62, 92, 22)):
        r.tube([(x, y), (tx, ty)], 6, .9)
    a.L(p.x, "facets", 5, gloss=1).ell(50, 76, 7.5)
    a.L(p.s, "glow", 3).ell(30, 76, 3).ell(70, 76, 3)


variants["crown"] = [crown_a, crown_b]


def _dispatch(name, fns):
    SHAPES[name] = lambda a, p: fns[a.v % len(fns)](a, p)


for _k, _f in variants.items():
    _dispatch(_k, _f)
