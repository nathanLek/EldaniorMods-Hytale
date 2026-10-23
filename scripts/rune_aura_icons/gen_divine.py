"""Images des runes divines (emblemes : roue de Taranis + eclair, porte de Babylone + epees, arche de glace de Niflheim) : Sanctuaire de Taranis (foudre), Gilgamesh Babylon (lumiere), Porte de Niflheim (glace).
Usage : python3 gen_divine.py <dossier_resources_Common> <dossier_contenant_Common/...extrait d'Assets.zip>
Ecrit les icones (HUD 128, inventaire 64), les textures de rune 3D et la texture de l'epee de lumiere."""
import sys, os, math, random, colorsys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import png
from gen import blend, add

def seg(px, py, ax, ay, bx, by):
    vx, vy = bx - ax, by - ay; wx, wy = px - ax, py - ay
    t = max(0.0, min(1.0, (wx * vx + wy * vy) / (vx * vx + vy * vy or 1)))
    return math.hypot(px - (ax + vx * t), py - (ay + vy * t))

def in_poly(x, y, poly):
    inside = False; j = len(poly) - 1
    for i in range(len(poly)):
        xi, yi = poly[i]; xj, yj = poly[j]
        if (yi > y) != (yj > y) and x < (xj - xi) * (y - yi) / (yj - yi + 1e-12) + xi: inside = not inside
        j = i
    return inside

# ---------- Emblemes (coordonnees normalisees, rayon 1 autour de (cx, cy)) ----------
BOLT = [(0.18, -1.0), (-0.42, 0.12), (-0.02, 0.12), (-0.28, 1.0), (0.45, -0.2), (0.05, -0.2), (0.42, -1.0)]
def bolt_emblem(px, w, h, cx, cy, R, ss=3):
    pts = [(cx + x * R, cy + y * R) for x, y in BOLT]
    for y in range(max(0, int(cy - R * 1.4)), min(h, int(cy + R * 1.4))):
        for x in range(max(0, int(cx - R * 1.4)), min(w, int(cx + R * 1.4))):
            cov = sum(in_poly(x + (i + .5) / ss, y + (j + .5) / ss, pts) for i in range(ss) for j in range(ss)) / (ss * ss)
            d = min(seg(x + .5, y + .5, *pts[k], *pts[(k + 1) % len(pts)]) for k in range(len(pts)))
            p = px[y][x]
            add(p, (90, 160, 255), math.exp(-(d / (R * 0.18)) ** 2) * 0.9 * (1 - cov))
            blend(p, (255, 245, 170), cov)
            blend(p, (255, 255, 255), cov * max(0, 1 - d / (R * 0.06)))

def sword_emblem(px, w, h, cx, cy, R, ss=3):
    def dist(fx, fy):
        x, y = (fx - cx) / R, (fy - cy) / R
        blade = abs(x) - 0.13 * min(1, max(0, (0.95 - y) / 0.35))   # lame qui s'affine en pointe vers le bas
        blade = blade if -0.42 <= y <= 0.95 else 9
        guard = max(abs(x) - 0.55, abs(y + 0.47) - 0.07)
        grip = max(abs(x) - 0.06, abs(y + 0.68) - 0.18)
        pommel = math.hypot(x, y + 0.92) - 0.09
        return blade, min(guard, grip, pommel)
    for y in range(max(0, int(cy - R * 1.3)), min(h, int(cy + R * 1.3))):
        for x in range(max(0, int(cx - R * 1.3)), min(w, int(cx + R * 1.3))):
            b = g = 0
            for i in range(ss):
                for j in range(ss):
                    db, dg = dist(x + (i + .5) / ss, y + (j + .5) / ss)
                    b += db < 0; g += dg < 0
            b /= ss * ss; g /= ss * ss
            db, dg = dist(x + .5, y + .5)
            p = px[y][x]
            add(p, (255, 215, 120), math.exp(-(max(0, min(db, dg)) / 0.25) ** 2) * 0.7 * (1 - max(b, g)))
            blend(p, (240, 250, 255), b)
            blend(p, (255, 200, 70), g)

def snow_emblem(px, w, h, cx, cy, R, ss=3):
    segs = []
    for k in range(6):
        a = k * math.pi / 3 - math.pi / 2; ca, sa = math.cos(a), math.sin(a)
        segs.append((0, 0, ca, sa))
        for t, l in ((0.45, 0.3), (0.72, 0.22)):
            bx, by = ca * t, sa * t
            for s in (1, -1):
                b = a + s * math.pi / 4
                segs.append((bx, by, bx + math.cos(b) * l, by + math.sin(b) * l))
    th = 0.075
    for y in range(max(0, int(cy - R * 1.2)), min(h, int(cy + R * 1.2))):
        for x in range(max(0, int(cx - R * 1.2)), min(w, int(cx + R * 1.2))):
            cov = 0
            for i in range(ss):
                for j in range(ss):
                    fx = (x + (i + .5) / ss - cx) / R; fy = (y + (j + .5) / ss - cy) / R
                    cov += min(seg(fx, fy, *s) for s in segs) < th
            cov /= ss * ss
            d = min(seg((x + .5 - cx) / R, (y + .5 - cy) / R, *s) for s in segs)
            p = px[y][x]
            add(p, (90, 200, 255), math.exp(-(d / 0.16) ** 2) * 0.8 * (1 - cov))
            blend(p, (235, 250, 255), cov)


# ---------- Emblemes v2 (coherents avec les noms) ----------
def raster(px, w, h, cx, cy, R, layers, ss=3, extent=1.3):
    """layers : liste de (sdf(x, y) en coordonnees normalisees, couleur, couleur_halo, largeur_halo, force_halo)."""
    for y in range(max(0, int(cy - R * extent)), min(h, int(cy + R * extent) + 1)):
        for x in range(max(0, int(cx - R * extent)), min(w, int(cx + R * extent) + 1)):
            p = px[y][x]
            for sdf, col, gcol, gw, gs in layers:
                cov = sum(sdf((x + (i + .5) / ss - cx) / R, (y + (j + .5) / ss - cy) / R) < 0
                          for i in range(ss) for j in range(ss)) / (ss * ss)
                if gcol:
                    d = max(0.0, sdf((x + .5 - cx) / R, (y + .5 - cy) / R))
                    add(p, gcol, math.exp(-(d / gw) ** 2) * gs * (1 - cov))
                if cov: blend(p, col, cov)

def sd_seg(x, y, ax, ay, bx, by, r): return seg(x, y, ax, ay, bx, by) - r
def sd_poly(x, y, pts):
    d = min(seg(x, y, *pts[k], *pts[(k + 1) % len(pts)]) for k in range(len(pts)))
    return -d if in_poly(x, y, pts) else d

def taranis_emblem(px, w, h, cx, cy, R, ss=3):
    """Roue de Taranis (dieu celte de l'orage) traversee d'un eclair."""
    def wheel(x, y):
        r = math.hypot(x, y)
        d = min(abs(r - 0.80) - 0.085, r - 0.17)
        for k in range(8):
            a = k * math.pi / 4
            d = min(d, sd_seg(x, y, math.cos(a) * 0.17, math.sin(a) * 0.17, math.cos(a) * 0.8, math.sin(a) * 0.8, 0.045))
        return d
    bolt = [(x * 0.78 + 0.02, y * 0.9) for x, y in BOLT]
    raster(px, w, h, cx, cy, R, [
        (wheel, (205, 225, 255), (80, 150, 255), 0.18, 0.8),
        (lambda x, y: sd_poly(x, y, bolt) - 0.025, (25, 30, 60), None, 0, 0),   # liseré sombre pour detacher l'eclair
        (lambda x, y: sd_poly(x, y, bolt), (255, 240, 150), (255, 230, 120), 0.2, 0.9),
    ], ss)

def babylon_emblem(px, w, h, cx, cy, R, ss=3):
    """Porte de Babylone : portail dore ondulant d'ou jaillissent trois epees (l'anneau passe devant leur base)."""
    pcx, pcy, rx, ry = 0.0, -0.5, 0.62, 0.36
    def ell(x, y): return math.hypot((x - pcx) / rx, (y - pcy) / ry)
    def portal_fill(x, y): return (ell(x, y) - 1) * ry
    def portal_ring(x, y):
        e = ell(x, y)
        return min(abs(e - 1) * ry - 0.05, abs(e - 0.62) * ry - 0.022)
    def sword(ang):
        dx, dy = math.sin(ang), math.cos(ang)
        bx, by = pcx + dx * 0.05, pcy + dy * 0.05
        L = 1.38
        tx, ty = bx + dx * L, by + dy * L
        def blade(x, y):
            t = max(0.0, min(1.0, ((x - bx) * dx + (y - by) * dy) / L))
            return seg(x, y, bx, by, tx, ty) - 0.1 * min(1.0, (1 - t) / 0.28)
        def guard(x, y):
            gx, gy = bx + dx * 0.36, by + dy * 0.36
            return sd_seg(x, y, gx - dy * 0.19, gy + dx * 0.19, gx + dy * 0.19, gy - dx * 0.19, 0.045)
        return blade, guard
    layers = [(portal_fill, (95, 60, 10), (255, 190, 60), 0.12, 0.45)]
    for ang in (-0.5, 0.0, 0.5):
        blade, guard = sword(ang)
        layers.append((lambda x, y, f=blade: f(x, y) - 0.035, (30, 22, 10), None, 0, 0))   # liseré sombre
        layers.append((blade, (238, 244, 255), (255, 225, 140), 0.08, 0.35))
        layers.append((lambda x, y, f=guard: f(x, y) - 0.03, (30, 22, 10), None, 0, 0))
        layers.append((guard, (255, 195, 60), None, 0, 0))
    layers.append((portal_ring, (255, 214, 95), (255, 190, 60), 0.1, 0.5))
    raster(px, w, h, cx, cy, R, layers, ss, extent=1.45)

def niflheim_emblem(px, w, h, cx, cy, R, ss=3):
    """Porte de Niflheim : arche de glace ouverte sur une lumiere glacee, flocon au centre."""
    def arch(x, y, half, top):
        rect = max(abs(x) - half, abs(y - (top + 0.95) / 2) - (0.95 - top) / 2)
        return min(rect, math.hypot(x, y - top) - half)
    def frame(x, y): return max(arch(x, y, 0.66, -0.2), -arch(x, y, 0.42, -0.2))
    def opening(x, y): return arch(x, y, 0.42, -0.2)
    flake = []
    for k in range(6):
        a = k * math.pi / 3 - math.pi / 2
        flake.append((0, 0.2, math.cos(a) * 0.3, 0.2 + math.sin(a) * 0.3))
        for s2 in (1, -1):
            b = a + s2 * math.pi / 4; mx, my = math.cos(a) * 0.17, 0.2 + math.sin(a) * 0.17
            flake.append((mx, my, mx + math.cos(b) * 0.1, my + math.sin(b) * 0.1))
    def snowflake(x, y): return min(seg(x, y, *f) for f in flake) - 0.035
    def icicles(x, y):
        d = 9
        for ix in (-0.5, -0.25, 0.0, 0.25, 0.5):
            d = min(d, sd_poly(x, y, [(ix - 0.06, -0.86 + abs(ix) * 0.5 + 0.02), (ix + 0.06, -0.86 + abs(ix) * 0.5 + 0.02), (ix, -0.62 + abs(ix) * 0.5)]))
        return d
    raster(px, w, h, cx, cy, R, [
        (opening, (40, 120, 190), (120, 220, 255), 0.1, 0.5),
        (lambda x, y: math.hypot(x, y - 0.2) - 0.36, (150, 225, 255), (180, 240, 255), 0.25, 0.6),
        (frame, (215, 240, 255), (90, 190, 255), 0.18, 0.8),
        (snowflake, (255, 255, 255), None, 0, 0),
    ], ss, extent=1.25)


def muspelheim_emblem(px, w, h, cx, cy, R, ss=3):
    """Courroux de Muspelheim : l'epee de flammes de Surtr, lame dressee entouree de langues de feu."""
    def blade(x, y):
        t = (y + 0.95) / 1.45                                  # 0 pointe (haut) -> 1 garde
        if not (-0.95 <= y <= 0.5): return 9
        return abs(x) - 0.13 * min(1.0, t / 0.25)
    def guard(x, y): return max(abs(x) - 0.48, abs(y - 0.56) - 0.07)
    def grip(x, y): return max(abs(x) - 0.06, abs(y - 0.76) - 0.14)
    def pommel(x, y): return math.hypot(x, y - 0.95) - 0.09
    def flames(x, y):
        d = 9
        for k, (fx, top, wdt) in enumerate(((-0.34, -0.55, 0.13), (0.34, -0.7, 0.14), (-0.22, -0.95, 0.09), (0.22, -1.05, 0.1), (0.0, -1.2, 0.08))):
            # langue de feu : goutte etiree vers le haut, legerement ondulee
            if top <= y <= 0.45:
                t = (y - top) / (0.45 - top)
                off = 0.06 * math.sin(t * 7 + k)
                d = min(d, abs(x - fx - off) - wdt * math.sin(min(1.0, t * 1.3) * math.pi / 2) * (1.1 - t * 0.6))
        return d
    raster(px, w, h, cx, cy, R, [
        (flames, (255, 120, 30), (255, 70, 10), 0.25, 0.9),
        (lambda x, y: min(blade(x, y), guard(x, y), grip(x, y), pommel(x, y)) - 0.035, (40, 8, 0), None, 0, 0),
        (blade, (255, 236, 190), (255, 160, 60), 0.12, 0.6),
        (guard, (255, 170, 40), None, 0, 0),
        (grip, (120, 30, 10), None, 0, 0),
        (pommel, (255, 170, 40), None, 0, 0),
    ], ss, extent=1.35)


def bahamut_emblem(px, w, h, cx, cy, R, ss=3):
    """Avenement de Bahamut : oeil de dragon dore a pupille fendue, entoure d'ecailles et de flammes."""
    def eye(x, y):                                          # amande : intersection de deux disques
        return max(math.hypot(x, y - 0.55) - 0.95, math.hypot(x, y + 0.55) - 0.95)
    def iris(x, y): return math.hypot(x, y) - 0.36
    def pupil(x, y): return max(abs(x) - 0.07 * (1 - abs(y) / 0.36), abs(y) - 0.34)
    def scales(x, y):                                       # couronne d'ecailles autour de l'oeil
        r = math.hypot(x, y); a = math.atan2(y, x)
        return max(abs(r - 0.95) - 0.1, -(0.5 + 0.5 * math.cos(a * 10)) + 0.35)
    raster(px, w, h, cx, cy, R, [
        (scales, (150, 30, 20), (255, 120, 40), 0.2, 0.6),
        (eye, (255, 196, 60), (255, 150, 40), 0.18, 0.8),
        (lambda x, y: iris(x, y) - 0.03, (120, 40, 0), None, 0, 0),
        (iris, (255, 120, 20), (255, 220, 120), 0.1, 0.5),
        (pupil, (20, 4, 2), None, 0, 0),
    ], ss, extent=1.3)


def nyx_emblem(px, w, h, cx, cy, R, ss=3):
    """Voile de Nyx : croissant de lune et etoiles."""
    def moon(x, y): return max(math.hypot(x, y) - 0.72, -(math.hypot(x - 0.32, y - 0.18) - 0.62))
    def star(sx, sy, r):
        return lambda x, y: (abs(x - sx) ** 0.6 + abs(y - sy) ** 0.6) ** (1 / 0.6) - r
    layers = [(moon, (235, 225, 255), (150, 90, 255), 0.25, 0.8)]
    for sx, sy, r in ((0.55, -0.55, 0.16), (0.75, 0.15, 0.1), (0.35, 0.62, 0.08), (-0.15, -0.85, 0.07)):
        layers.append((star(sx, sy, r), (255, 255, 255), (190, 150, 255), 0.12, 0.7))
    raster(px, w, h, cx, cy, R, layers, ss, extent=1.3)

def apollo_emblem(px, w, h, cx, cy, R, ss=3):
    """Sanctuaire d'Apollon : temple a colonnes devant un soleil."""
    def sun(x, y): return math.hypot(x, y + 0.25) - 0.55
    def rays(x, y):
        a = math.atan2(y + 0.25, x); d = math.hypot(x, y + 0.25)
        return max(abs(d - 0.8) - 0.18, -((0.5 + 0.5 * math.cos(a * 12)) - 0.75))
    def pediment(x, y): return sd_poly(x, y, [(-0.8, -0.05), (0.8, -0.05), (0.0, -0.5)])
    def base(x, y): return max(abs(x) - 0.85, abs(y - 0.78) - 0.08)
    def cols(x, y):
        d = 9
        for k in range(5):
            d = min(d, max(abs(x - (-0.6 + k * 0.3)) - 0.07, abs(y - 0.36) - 0.36))
        return d
    raster(px, w, h, cx, cy, R, [
        (rays, (255, 210, 90), (255, 200, 80), 0.15, 0.5),
        (sun, (255, 230, 140), (255, 210, 90), 0.25, 0.8),
        (lambda x, y: min(pediment(x, y), base(x, y), cols(x, y)) - 0.04, (90, 60, 10), None, 0, 0),
        (pediment, (250, 244, 225), None, 0, 0), (cols, (250, 244, 225), None, 0, 0), (base, (255, 215, 110), None, 0, 0),
    ], ss, extent=1.3)

def fujin_emblem(px, w, h, cx, cy, R, ss=3):
    """Courroux de Fujin : spirale de vent et rafales."""
    def spiral(x, y):
        r = math.hypot(x, y); a = math.atan2(y, x)
        best = 9
        for k in range(-1, 4):
            target = 0.12 * (a / (2 * math.pi) + k)          # spirale d'Archimede
            if target > 0: best = min(best, abs(r - target * 4.2))
        return max(best - 0.07 * (1 - r * 0.6), r - 0.95)
    def gust(y0, x0, x1):
        return lambda x, y: sd_seg(x, y, x0, y0 + 0.05 * math.sin(x * 6), x1, y0, 0.045)
    raster(px, w, h, cx, cy, R, [
        (spiral, (225, 250, 255), (120, 220, 230), 0.18, 0.8),
        (gust(0.8, -0.9, 0.2), (200, 240, 245), None, 0, 0),
        (gust(-0.85, -0.2, 0.9), (200, 240, 245), None, 0, 0),
    ], ss, extent=1.3)

def gaia_emblem(px, w, h, cx, cy, R, ss=3):
    """Reveil de Gaia : montagne fendue, eclats de roche."""
    mountain = [(-0.95, 0.7), (-0.35, -0.45), (-0.1, -0.15), (0.25, -0.8), (0.95, 0.7)]
    def mount(x, y): return sd_poly(x, y, mountain)
    def crack(x, y):
        return min(sd_seg(x, y, 0.25, -0.75, 0.1, -0.2, 0.04), sd_seg(x, y, 0.1, -0.2, 0.3, 0.2, 0.04), sd_seg(x, y, 0.3, 0.2, 0.05, 0.7, 0.04))
    def ground(x, y): return max(abs(x) - 0.95, abs(y - 0.8) - 0.08)
    raster(px, w, h, cx, cy, R, [
        (mount, (150, 112, 70), (210, 160, 90), 0.15, 0.5),
        (lambda x, y: sd_poly(x, y, [(0.25, -0.8), (0.42, -0.5), (0.1, -0.45)]), (235, 230, 220), None, 0, 0),
        (crack, (255, 160, 60), (255, 140, 40), 0.1, 0.8),
        (ground, (110, 80, 50), None, 0, 0),
    ], ss, extent=1.3)

def yggdrasil_emblem(px, w, h, cx, cy, R, ss=3):
    """Eveil d'Yggdrasil : arbre-monde, couronne de feuillage et racines."""
    def crown(x, y):
        return min(math.hypot(x, y + 0.35) - 0.55, math.hypot(x - 0.45, y + 0.1) - 0.38, math.hypot(x + 0.45, y + 0.1) - 0.38)
    def trunk(x, y): return max(abs(x) - (0.09 + 0.08 * max(0, y - 0.3)), abs(y - 0.35) - 0.45)
    def roots(x, y):
        return min(sd_seg(x, y, 0, 0.75, -0.6, 0.92, 0.05), sd_seg(x, y, 0, 0.75, 0.6, 0.92, 0.05), sd_seg(x, y, 0, 0.75, -0.25, 0.98, 0.045), sd_seg(x, y, 0, 0.75, 0.25, 0.98, 0.045))
    def ring(x, y): return abs(math.hypot(x, y) - 1.05) - 0.04
    raster(px, w, h, cx, cy, R, [
        (ring, (220, 200, 120), None, 0, 0),
        (crown, (90, 190, 70), (150, 230, 90), 0.15, 0.6),
        (lambda x, y: min(trunk(x, y), roots(x, y)) - 0.03, (40, 25, 10), None, 0, 0),
        (trunk, (140, 100, 60), None, 0, 0), (roots, (140, 100, 60), None, 0, 0),
    ], ss, extent=1.2)


def chronos_emblem(px, w, h, cx, cy, R, ss=3):
    """Sablier de Chronos : sablier dore, sable qui coule, cadran en arriere-plan."""
    def dial(x, y): return abs(math.hypot(x, y) - 0.92) - 0.05
    def ticks(x, y):
        a = math.atan2(y, x); r = math.hypot(x, y)
        return max(abs(r - 0.8) - 0.07, -((0.5 + 0.5 * math.cos(a * 12)) ** 20 - 0.5))
    def glass(x, y): return sd_poly(x, y, [(-0.42, -0.7), (0.42, -0.7), (0.06, 0.0), (0.42, 0.7), (-0.42, 0.7), (-0.06, 0.0)])
    def frame(x, y): return min(max(abs(x) - 0.55, abs(y + 0.76) - 0.06), max(abs(x) - 0.55, abs(y - 0.76) - 0.06),
                                max(abs(abs(x) - 0.5) - 0.04, abs(y) - 0.76))
    def sand(x, y): return min(sd_poly(x, y, [(-0.2, -0.38), (0.2, -0.38), (0.02, -0.05), (-0.02, -0.05)]),
                               sd_poly(x, y, [(-0.36, 0.66), (0.36, 0.66), (0.0, 0.38)]), max(abs(x) - 0.015, abs(y - 0.18) - 0.2))
    raster(px, w, h, cx, cy, R, [
        (dial, (200, 170, 90), None, 0, 0), (ticks, (230, 200, 120), None, 0, 0),
        (glass, (170, 220, 255), (140, 200, 255), 0.12, 0.5), (sand, (255, 215, 120), None, 0, 0),
        (frame, (255, 200, 80), (255, 190, 70), 0.1, 0.5),
    ], ss, extent=1.25)

def chaos_emblem(px, w, h, cx, cy, R, ss=3):
    """Abime du Chaos : singularite noire, disque d'accretion incline, etoiles aspirees."""
    def disk(x, y):
        r = math.hypot(x, y / 0.38)
        return abs(r - 0.68) - 0.18
    def core(x, y): return math.hypot(x, y) - 0.34
    def halo(x, y): return abs(math.hypot(x, y) - 0.38) - 0.04
    def stars(x, y):
        return min(math.hypot(x - sx, y - sy) - r for sx, sy, r in ((0.7, -0.6, 0.05), (-0.75, 0.5, 0.04), (-0.55, -0.7, 0.035), (0.8, 0.55, 0.03)))
    raster(px, w, h, cx, cy, R, [
        (stars, (255, 255, 255), (200, 170, 255), 0.08, 0.6),
        (disk, (210, 140, 255), (160, 90, 255), 0.18, 0.8),
        (halo, (240, 220, 255), (180, 120, 255), 0.1, 0.6),
        (core, (4, 0, 10), None, 0, 0),
        (lambda x, y: max(disk(x, y), y), (225, 170, 255), None, 0, 0),           # avant du disque devant le trou noir
    ], ss, extent=1.25)

def bifrost_emblem(px, w, h, cx, cy, R, ss=3):
    """Pont du Bifrost : arc-en-ciel en arche entre deux piliers de lumiere."""
    cols = [(255, 70, 70), (255, 160, 60), (255, 235, 90), (90, 230, 110), (80, 170, 255), (110, 90, 255), (200, 110, 255)]
    layers = []
    for i, col in enumerate(cols):
        rr = 0.82 - i * 0.075
        layers.append((lambda x, y, rr=rr: max(abs(math.hypot(x, y - 0.45) - rr) - 0.036, y - 0.45), col, None, 0, 0))
    for sx in (-0.62, 0.62):
        layers.append((lambda x, y, sx=sx: max(abs(x - sx) - 0.07, abs(y - 0.62) - 0.2), (255, 250, 230), (255, 255, 255), 0.15, 0.7))
    raster(px, w, h, cx, cy, R, layers, ss, extent=1.25)


def avatar_emblem(px, w, h, cx, cy, R, ss=3):
    """Avatar du Dragon Divin : tete de dragon de face, cornes, ailes deployees, gemme frontale."""
    def wing(sx):
        pts = [(0.15 * sx, -0.1), (0.95 * sx, -0.75), (0.8 * sx, -0.3), (1.0 * sx, -0.15), (0.75 * sx, 0.05), (0.85 * sx, 0.3), (0.2 * sx, 0.2)]
        return lambda x, y: sd_poly(x, y, pts)
    head = [(0, -0.45), (0.32, -0.2), (0.24, 0.35), (0.0, 0.75), (-0.24, 0.35), (-0.32, -0.2)]
    def horns(x, y):
        return min(sd_poly(x, y, [(0.18, -0.38), (0.3, -0.3), (0.55, -0.95)]), sd_poly(x, y, [(-0.18, -0.38), (-0.3, -0.3), (-0.55, -0.95)]))
    def eyes(x, y): return min(math.hypot((x - 0.13) / 1.8, y + 0.02) - 0.035, math.hypot((x + 0.13) / 1.8, y + 0.02) - 0.035)
    def gem(x, y): return abs(x) + abs(y + 0.25) - 0.08
    raster(px, w, h, cx, cy, R, [
        (wing(1), (190, 150, 90), (255, 190, 90), 0.06, 0.3), (wing(-1), (190, 150, 90), (255, 190, 90), 0.06, 0.3),
        (horns, (250, 240, 215), None, 0, 0),
        (lambda x, y: sd_poly(x, y, head) - 0.03, (40, 30, 20), None, 0, 0),
        (lambda x, y: sd_poly(x, y, head), (185, 200, 230), None, 0, 0),
        (lambda x, y: max(sd_poly(x, y, head), abs(x) - 0.05), (255, 205, 95), None, 0, 0),
        (eyes, (120, 240, 255), (120, 240, 255), 0.08, 0.9), (gem, (120, 240, 255), None, 0, 0),
    ], ss, extent=1.3)

# ---------- Fonds ----------
def background(size, top, bottom, seed, specks):
    rnd = random.Random(seed)
    px = [[[0, 0, 0, 255] for _ in range(size)] for _ in range(size)]
    for y in range(size):
        t = y / size
        for x in range(size):
            px[y][x] = [top[i] * (1 - t) + bottom[i] * t for i in range(3)] + [255]
    for _ in range(specks[0]):
        x, y = rnd.randrange(size), rnd.randrange(size)
        add(px[y][x], specks[1], rnd.uniform(0.3, 0.9))
    return px

def border(px, size):
    for y in range(size):
        for x in range(size):
            e = min(x, y, size - 1 - x, size - 1 - y)
            if e < 3:
                for i in range(3): px[y][x][i] *= 0.55 + 0.15 * e

def rays(px, size, color, n, strength):
    c = size / 2
    for y in range(size):
        for x in range(size):
            a = math.atan2(y + .5 - c, x + .5 - c); d = math.hypot(x + .5 - c, y + .5 - c) / c
            r = (math.cos(a * n) * 0.5 + 0.5) ** 8 * max(0, 1 - d) * strength
            add(px[y][x], color, r)

def clouds(px, size, seed):
    rnd = random.Random(seed)
    for _ in range(14):
        cx, cy, r = rnd.uniform(0, size), rnd.uniform(-size * 0.1, size * 0.35), rnd.uniform(size * 0.12, size * 0.25)
        for y in range(max(0, int(cy - r)), min(size, int(cy + r))):
            for x in range(max(0, int(cx - r)), min(size, int(cx + r))):
                k = max(0, 1 - math.hypot(x - cx, y - cy) / r)
                add(px[y][x], (55, 50, 90), k * 0.35)

# ---------- Sorties ----------
def ability_icons(out):
    S = 128
    px = background(S, (25, 18, 45), (10, 12, 30), 3, (30, (200, 210, 255))); clouds(px, S, 5)
    taranis_emblem(px, S, S, S / 2, S / 2, S * 0.4); border(px, S)
    png.write(out + '/Icons/Abilities/Rune_SanctuaireTaranis.png', S, S, px)
    px = background(S, (20, 26, 60), (8, 10, 28), 4, (25, (255, 235, 180))); rays(px, S, (255, 210, 110), 10, 0.25)
    babylon_emblem(px, S, S, S / 2, S / 2 + 4, S * 0.38); border(px, S)
    png.write(out + '/Icons/Abilities/Rune_GilgameshBabylon.png', S, S, px)
    px = background(S, (14, 40, 70), (5, 12, 30), 6, (70, (200, 240, 255))); rays(px, S, (120, 210, 255), 6, 0.25)
    niflheim_emblem(px, S, S, S / 2, S / 2 + 4, S * 0.4); border(px, S)
    png.write(out + '/Icons/Abilities/Rune_PorteNiflheim.png', S, S, px)
    px = background(S, (60, 10, 6), (14, 4, 4), 8, (60, (255, 150, 60))); rays(px, S, (255, 90, 20), 8, 0.3)
    muspelheim_emblem(px, S, S, S / 2, S / 2 + 4, S * 0.4); border(px, S)
    png.write(out + '/Icons/Abilities/Rune_CourrouxMuspelheim.png', S, S, px)
    px = background(S, (40, 14, 6), (10, 4, 2), 9, (40, (255, 200, 120))); rays(px, S, (255, 170, 60), 12, 0.3)
    bahamut_emblem(px, S, S, S / 2, S / 2, S * 0.4); border(px, S)
    png.write(out + '/Icons/Abilities/Rune_AvenementBahamut.png', S, S, px)
    for name, fn, top, bot, spk, ray in (
            ('VoileNyx', nyx_emblem, (30, 10, 50), (6, 2, 14), (200, 170, 255), (140, 80, 255)),
            ('SanctuaireApollon', apollo_emblem, (70, 90, 150), (20, 30, 70), (255, 240, 190), (255, 220, 120)),
            ('CourrouxFujin', fujin_emblem, (20, 70, 80), (6, 22, 30), (220, 250, 255), (150, 230, 240)),
            ('ReveilGaia', gaia_emblem, (60, 40, 20), (18, 12, 6), (220, 180, 120), (200, 150, 80)),
            ('EveilYggdrasil', yggdrasil_emblem, (20, 60, 25), (6, 20, 8), (200, 255, 170), (150, 230, 120)),
            ('SablierChronos', chronos_emblem, (40, 50, 90), (10, 12, 30), (220, 230, 255), (255, 215, 120)),
            ('AbimeChaos', chaos_emblem, (20, 6, 40), (2, 0, 8), (220, 200, 255), (150, 90, 255)),
            ('PontBifrost', bifrost_emblem, (30, 50, 90), (8, 14, 34), (255, 255, 255), (200, 220, 255)),
            ('AvatarDragon', avatar_emblem, (50, 26, 10), (12, 6, 3), (255, 220, 160), (255, 170, 70))):
        px = background(S, top, bot, len(name), (40, spk)); rays(px, S, ray, 10, 0.2)
        fn(px, S, S, S / 2, S / 2, S * 0.4); border(px, S)
        png.write(out + '/Icons/Abilities/Rune_%s.png' % name, S, S, px)

def item_icons(out, src, cx=35.5, cy=33.3, gx=33, gy=34):
    for name, fn, R in (('Rune_SanctuaireTaranis', taranis_emblem, 10.5), ('Rune_GilgameshBabylon', babylon_emblem, 9.5), ('Rune_PorteNiflheim', niflheim_emblem, 10.5), ('Rune_CourrouxMuspelheim', muspelheim_emblem, 10.5), ('Rune_AvenementBahamut', bahamut_emblem, 10.5), ('Rune_VoileNyx', nyx_emblem, 10.5), ('Rune_SanctuaireApollon', apollo_emblem, 10.5), ('Rune_CourrouxFujin', fujin_emblem, 10.5), ('Rune_ReveilGaia', gaia_emblem, 10.5), ('Rune_EveilYggdrasil', yggdrasil_emblem, 10.5), ('Rune_SablierChronos', chronos_emblem, 10.5), ('Rune_AbimeChaos', chaos_emblem, 10.5), ('Rune_PontBifrost', bifrost_emblem, 10.5), ('Rune_AvatarDragon', avatar_emblem, 10.5)):
        w, h, px = png.read(src + '/Common/Icons/ItemsGenerated/Rune_Enrage.png')
        def warmth(p): return max(0.0, min(1.0, (p[0] - p[2] - 15) / 90.0)) if p[3] > 0 else 0
        stone = [p for row in px for p in row if p[3] > 200 and warmth(p) == 0]
        sc = [sum(p[i] for p in stone) / len(stone) for i in range(3)]
        for y in range(h):
            for x in range(w):
                p = px[y][x]; k = warmth(p)
                if k > 0 and math.hypot(x - gx, y - gy) < 22:
                    for i in range(3): p[i] = p[i] * (1 - k) + sc[i] * 0.8 * k
        fn(px, w, h, cx, cy, R, ss=4)
        png.write(out + f'/Icons/ItemsGenerated/{name}.png', w, h, px)

def rune_textures(out, src):
    for name, hue, sat in (('SanctuaireTaranis', 0.16, 0.8), ('GilgameshBabylon', 0.12, 0.45), ('PorteNiflheim', 0.53, 0.75), ('CourrouxMuspelheim', 0.04, 0.9), ('AvenementBahamut', 0.1, 0.85), ('VoileNyx', 0.75, 0.7), ('SanctuaireApollon', 0.13, 0.5), ('CourrouxFujin', 0.5, 0.45), ('ReveilGaia', 0.08, 0.6), ('EveilYggdrasil', 0.3, 0.75), ('SablierChronos', 0.58, 0.5), ('AbimeChaos', 0.78, 0.8), ('PontBifrost', 0.55, 0.3), ('AvatarDragon', 0.11, 0.7)):
        w, h, px = png.read(src + '/Common/Items/Runes/Rune_Textures/Rune_Ability_Enrage.png')
        for row in px:
            for p in row:
                if p[3] == 0: continue
                hh, s, v = colorsys.rgb_to_hsv(*(c / 255 for c in p[:3]))
                if s > 0.35 and (hh < 0.12 or hh > 0.9):
                    r, g, b = colorsys.hsv_to_rgb(hue, sat, min(1, v * 1.2))
                    p[0], p[1], p[2] = r * 255, g * 255, b * 255
        png.write(out + f'/Items/Runes/Rune_Textures/Rune_Ability_{name}.png', w, h, px)

def light_sword(out, src):
    """Texture de l'epee de lumiere : epee Adamantite vanilla recoloree blanc-or lumineux (motifs conserves)."""
    w, h, px = png.read(src + '/Common/Items/Weapons/Sword/Adamantite_Texture.png')
    for row in px:
        for p in row:
            if p[3] == 0: continue
            l = (0.3 * p[0] + 0.59 * p[1] + 0.11 * p[2]) / 255
            l = 0.55 + 0.45 * l
            p[0], p[1], p[2] = 255 * l, 240 * l, 190 * l + 40 * (1 - l)
    os.makedirs(out + '/Items/Projectiles/Gilgamesh', exist_ok=True)
    png.write(out + '/Items/Projectiles/Gilgamesh/Light_Sword.png', w, h, px)

if __name__ == '__main__':
    out, src = sys.argv[1], sys.argv[2]
    ability_icons(out); item_icons(out, src); rune_textures(out, src); light_sword(out, src)
    print('ok')
