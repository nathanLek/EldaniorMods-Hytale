"""Images d'une rune : icone de competence (128), icone d'inventaire (64) et texture du modele 3D (64).

Base vanilla (pierre de Rune_Enrage lue dans Assets.zip) + embleme vectoriel du type de la rune.
Utilise par rune_tool.py ; utilisable seul :
    python3 scripts/runes/rune_icons.py <Type> <Rarete> <dossier_sortie> [Embleme]   (apercu des 3 images)
"""
import colorsys, math, os, random, sys, tempfile, zipfile

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'rune_aura_icons'))
import png  # noqa: E402  (module PNG du projet : RGBA 8 bits)

# ---------------- SDF (distance signee, < 0 a l'interieur) en coordonnees [-1, 1], v vers le bas ----------------
def circle(u, v, cx, cy, r): return math.hypot(u - cx, v - cy) - r
def ring(u, v, cx, cy, r, w): return abs(math.hypot(u - cx, v - cy) - r) - w
def seg(u, v, ax, ay, bx, by, r):
    px, py, dx, dy = u - ax, v - ay, bx - ax, by - ay
    t = max(0.0, min(1.0, (px * dx + py * dy) / (dx * dx + dy * dy or 1)))
    return math.hypot(px - dx * t, py - dy * t) - r
def poly(u, v, pts):
    d = min(seg(u, v, *pts[i], *pts[(i + 1) % len(pts)], 0) for i in range(len(pts)))
    inside = False
    for i in range(len(pts)):
        (x1, y1), (x2, y2) = pts[i], pts[(i + 1) % len(pts)]
        if (y1 > v) != (y2 > v) and u < (x2 - x1) * (v - y1) / (y2 - y1) + x1: inside = not inside
    return -d if inside else d
def arc(u, v, cx, cy, r, w, a0, a1):
    a = math.atan2(v - cy, u - cx)
    if a0 <= a <= a1: return ring(u, v, cx, cy, r, w)
    ends = [(cx + r * math.cos(a0), cy + r * math.sin(a0)), (cx + r * math.cos(a1), cy + r * math.sin(a1))]
    return min(math.hypot(u - x, v - y) for x, y in ends) - w
def smin(a, b, k):
    h = max(0.0, min(1.0, 0.5 + 0.5 * (b - a) / k)); return b * (1 - h) + a * h - k * h * (1 - h)

# ---------------- Emblemes par type ----------------
def e_feu(u, v):
    def flame(sx, sy, ox, oy):
        x, y = (u - ox) / sx, (v - oy) / sy
        body = smin(circle(x, y, 0, 0.32, 0.4), poly(x, y, [(-0.36, 0.22), (0.36, 0.22), (0.12, -0.45), (0.02, -0.92), (-0.1, -0.5)]), 0.25)
        return body * min(sx, sy)
    return max(flame(1, 1, 0, 0), -flame(0.48, 0.5, 0.03, 0.3))
def e_glace(u, v):
    d = 9.0
    for k in range(3):
        a = math.pi * k / 3; c, s = math.cos(a), math.sin(a)
        d = min(d, seg(u, v, -0.8 * c, -0.8 * s, 0.8 * c, 0.8 * s, 0.07))
        for sign in (-1, 1):
            bx, by = 0.5 * c * sign, 0.5 * s * sign
            for b in (a + 0.7, a - 0.7):
                d = min(d, seg(u, v, bx, by, bx + 0.22 * math.cos(b) * sign, by + 0.22 * math.sin(b) * sign, 0.05))
    return d
def e_terre(u, v):
    m = poly(u, v, [(-0.85, 0.65), (-0.25, -0.55), (0.05, -0.1), (0.3, -0.7), (0.85, 0.65)])
    crack = seg(u, v, -0.25, -0.55, -0.1, 0.1, 0.05)
    crack = min(crack, seg(u, v, -0.1, 0.1, 0.1, 0.65, 0.05))
    return max(m, -crack)
def e_air(u, v):
    return min(arc(u, v, 0, 0, 0.72, 0.07, -2.6, 1.2), arc(u, v, 0.05, 0.05, 0.45, 0.07, -0.6, 2.9), arc(u, v, -0.05, 0, 0.2, 0.07, -2.8, 0.9))
def e_tenebres(u, v):
    return min(max(circle(u, v, 0, 0, 0.72), -circle(u, v, 0.32, -0.18, 0.6)), circle(u, v, 0.48, 0.42, 0.07))
def e_lumiere(u, v):
    d = circle(u, v, 0, 0, 0.32)
    for k in range(12):
        a = math.pi * k / 6; L = 0.85 if k % 2 == 0 else 0.62
        d = min(d, seg(u, v, 0.44 * math.cos(a), 0.44 * math.sin(a), L * math.cos(a), L * math.sin(a), 0.05))
    return d
def e_temps(u, v):
    glass = min(poly(u, v, [(-0.5, -0.65), (0.5, -0.65), (0.06, 0), (-0.06, 0)]), poly(u, v, [(-0.06, 0), (0.06, 0), (0.5, 0.65), (-0.5, 0.65)]))
    hollow = max(glass, -min(poly(u, v, [(-0.36, -0.55), (0.36, -0.55), (0.02, -0.06), (-0.02, -0.06)]), poly(u, v, [(-0.02, 0.2), (0.02, 0.2), (0.36, 0.55), (-0.36, 0.55)])))
    return min(hollow, seg(u, v, -0.62, -0.75, 0.62, -0.75, 0.06), seg(u, v, -0.62, 0.75, 0.62, 0.75, 0.06))
def e_espace(u, v):
    star = poly(u, v, [(0, -0.42), (0.1, -0.1), (0.42, 0), (0.1, 0.1), (0, 0.42), (-0.1, 0.1), (-0.42, 0), (-0.1, -0.1)])
    return min(ring(u, v, 0, 0, 0.66, 0.08), ring(u, v, 0, 0, 0.82, 0.025), star)
def e_combat(u, v):
    d = min(seg(u, v, -0.7, -0.7, 0.55, 0.55, 0.07), seg(u, v, 0.7, -0.7, -0.55, 0.55, 0.07))
    d = min(d, seg(u, v, 0.28, 0.62, 0.62, 0.28, 0.06), seg(u, v, -0.28, 0.62, -0.62, 0.28, 0.06))
    return min(d, circle(u, v, 0.7, 0.7, 0.09), circle(u, v, -0.7, 0.7, 0.09))
def e_spatial(u, v):
    a = 0.5; c, s = math.cos(a), math.sin(a)
    x, y = u * c + v * s, -u * s + v * c
    orbit = abs(math.hypot(x, y * 2.6) - 0.8) / 2.0 - 0.035
    return min(circle(u, v, 0, 0, 0.3), max(orbit, -circle(u, v, 0, 0, 0.38)), circle(u, v, 0.78 * c, 0.78 * s, 0.08))
def e_dragonic(u, v):
    d = 9.0
    for o in (-0.38, 0.0, 0.38):                     # trois griffures courbes paralleles
        pts = [(-0.55 + o + 0.25 * math.sin(math.pi * t / 4) ** 2, -0.75 + 1.5 * t / 4) for t in range(5)]
        for i in range(4):
            r = 0.03 + 0.06 * math.sin(math.pi * (i + 0.5) / 4)
            d = min(d, seg(u, v, pts[i][0] + 0.3 * pts[i][1] * 0.4, pts[i][1], pts[i + 1][0] + 0.3 * pts[i + 1][1] * 0.4, pts[i + 1][1], r))
    return d

def e_foudre(u, v):
    bolt = poly(u, v, [(0.18, -0.9), (-0.42, 0.08), (-0.04, 0.08), (-0.22, 0.9), (0.44, -0.16), (0.04, -0.16), (0.3, -0.9)])
    return min(bolt, seg(u, v, 0.55, -0.55, 0.75, -0.72, 0.035), seg(u, v, -0.6, 0.45, -0.78, 0.62, 0.035))

# Emblemes dedies a un sort (champ "emblem" de la fiche)
def e_puits(u, v):
    """Puits gravitationnel : entonnoir d'anneaux qui se resserrent vers le bas, fleche qui plonge."""
    d = 9.0
    for cy, r in ((-0.5, 0.78), (-0.12, 0.55), (0.2, 0.34), (0.44, 0.16)):
        d = min(d, abs(math.hypot(u, (v - cy) * 3.0) - r) / 3.0 - 0.028)
    return min(d, seg(u, v, 0, -0.95, 0, 0.62, 0.05), poly(u, v, [(-0.17, 0.5), (0.17, 0.5), (0, 0.8)]))
def e_autorite(u, v):
    """Autorite gravitationnelle : couronne au-dessus de trois chevrons qui ecrasent vers le bas."""
    crown = poly(u, v, [(-0.6, -0.25), (-0.6, -0.72), (-0.32, -0.45), (0, -0.85), (0.32, -0.45), (0.6, -0.72), (0.6, -0.25)])
    crown = max(crown, -poly(u, v, [(-0.45, -0.33), (0.45, -0.33), (0.45, -0.38), (-0.45, -0.38)]))
    d = crown
    for k, y in enumerate((0.0, 0.3, 0.6)):
        wdt = 0.62 - k * 0.14
        d = min(d, seg(u, v, -wdt, y - 0.12, 0, y + 0.1, 0.055), seg(u, v, wdt, y - 0.12, 0, y + 0.1, 0.055))
    return d
def e_cyclone(u, v):
    """Oeil du cyclone : spirale serree autour d'un oeil calme."""
    d = circle(u, v, 0, 0, 0.12)
    for k in range(3):
        a0 = k * 2 * math.pi / 3
        pts = [(math.cos(a0 + t * 0.35) * (0.22 + t * 0.055), math.sin(a0 + t * 0.35) * (0.22 + t * 0.055)) for t in range(12)]
        for i in range(len(pts) - 1): d = min(d, seg(u, v, *pts[i], *pts[i + 1], 0.035 + 0.025 * i / 11))
    return d

def e_telekinesie(u, v):
    """Telekinesie : main ouverte, paume vers le haut, qui fait leviter un bloc entoure d'ondes."""
    palm = smin(poly(u, v, [(-0.5, 0.35), (0.5, 0.35), (0.42, 0.8), (-0.42, 0.8)]), circle(u, v, 0, 0.62, 0.36), 0.1)
    fingers = min(seg(u, v, x, 0.38, x * 1.15, 0.05 + abs(x) * 0.3, 0.075) for x in (-0.42, -0.15, 0.13, 0.4))
    hand = min(palm, fingers, seg(u, v, -0.52, 0.55, -0.78, 0.25, 0.08))
    cube = poly(u, v, [(0, -0.82), (0.28, -0.62), (0, -0.42), (-0.28, -0.62)])
    waves = min(arc(u, v, 0, -0.62, 0.48, 0.025, -2.6, -0.55), arc(u, v, 0, -0.62, 0.62, 0.02, -2.4, -0.75))
    return min(hand, cube, waves)

def _silhouette(u, v, cx, cy, sc):
    x, y = (u - cx) / sc, (v - cy) / sc
    body = smin(circle(x, y, 0, -0.62, 0.22), poly(x, y, [(-0.36, -0.3), (0.36, -0.3), (0.28, 0.75), (-0.28, 0.75)]), 0.12)
    return body * sc

def e_clonage(u, v):
    """Clonage : trois silhouettes, la centrale au premier plan, deux copies derriere."""
    back = min(_silhouette(u, v, -0.45, 0.08, 0.7), _silhouette(u, v, 0.45, 0.08, 0.7))
    front = _silhouette(u, v, 0, 0.12, 0.95)
    return min(max(back, -(front - 0.06)), front)

def e_miroir(u, v):
    """Doppelganger : une silhouette et son reflet de part et d'autre d'un miroir."""
    mirror = seg(u, v, 0, -0.9, 0, 0.9, 0.035)
    return min(_silhouette(u, v, -0.42, 0.05, 0.8), _silhouette(u, v, 0.42, 0.05, 0.8), mirror)

EMBLEMS = {"Feu": e_feu, "Glace": e_glace, "Terre": e_terre, "Air": e_air, "Tenebres": e_tenebres, "Lumiere": e_lumiere,
           "Temps": e_temps, "Espace": e_espace, "Combat": e_combat, "Spatial": e_spatial, "Dragonic": e_dragonic, "Foudre": e_foudre,
           "Puits": e_puits, "Autorite": e_autorite, "Cyclone": e_cyclone, "Telekinesie": e_telekinesie, "Clonage": e_clonage, "Miroir": e_miroir}

# ---------------- Dessin ----------------
def blend(p, col, a):
    a = max(0.0, min(1.0, a))
    if a <= 0: return
    da = p[3] / 255.0; oa = a + da * (1 - a)
    for i in range(3): p[i] = (col[i] * a + p[i] * da * (1 - a)) / oa if oa else 0
    p[3] = oa * 255
def add(p, col, a):
    for i in range(3): p[i] = min(255, p[i] + col[i] * a)

def mix(a, b, t): return [a[i] + (b[i] - a[i]) * t for i in range(3)]

def draw_emblem(px, w, h, cx, cy, R, fn, col, glow_col=None, ss=3):
    """Embleme en relief : halo, contour sombre, remplissage en degrade, biseau eclaire d'en haut a gauche, reflet."""
    light = (-0.6, -0.8)
    hi, lo = mix(col, (255, 255, 255), 0.35), mix(col, (0, 0, 0), 0.35)
    eps = 1.0 / R
    for y in range(max(0, int(cy - R * 1.45)), min(h, int(cy + R * 1.45) + 1)):
        for x in range(max(0, int(cx - R * 1.45)), min(w, int(cx + R * 1.45) + 1)):
            u0, v0 = (x + 0.5 - cx) / R, (y + 0.5 - cy) / R
            d0 = fn(u0, v0)
            if glow_col and d0 > 0: add(px[y][x], glow_col, math.exp(-d0 * 6) * 0.6)                       # halo
            if 0 <= d0 < 0.07: blend(px[y][x], mix(col, (0, 0, 0), 0.8), (1 - d0 / 0.07) * 0.9)              # contour
            cov = 0.0
            for sy in range(ss):
                for sx in range(ss):
                    if fn((x + (sx + 0.5) / ss - cx) / R, (y + (sy + 0.5) / ss - cy) / R) < 0: cov += 1
            cov /= ss * ss
            if cov <= 0: continue
            gx = (fn(u0 + eps, v0) - fn(u0 - eps, v0)) / (2 * eps); gy = (fn(u0, v0 + eps) - fn(u0, v0 - eps)) / (2 * eps)
            gl = math.hypot(gx, gy) or 1
            facing = -(gx * light[0] + gy * light[1]) / gl                                   # face tournee vers la lumiere
            c = mix(hi, lo, max(0.0, min(1.0, (v0 + 1) / 2)))                                # degrade haut -> bas
            bevel = max(0.0, min(1.0, 1 + d0 / 0.14))                                       # 1 au bord, 0 au coeur
            c = mix(c, (255, 255, 255), max(0.0, facing) * bevel * 0.55) if facing > 0 else mix(c, (0, 0, 0), -facing * bevel * 0.45)
            if -0.05 < d0 < -0.015 and facing > 0.4: c = mix(c, (255, 255, 255), 0.5)        # reflet
            blend(px[y][x], c, cov)

def _noise(rnd, n=6):
    waves = [(rnd.uniform(0.02, 0.09), rnd.uniform(0.02, 0.09), rnd.uniform(0, 6.3), rnd.uniform(0.5, 1)) for _ in range(n)]
    def f(x, y): return sum(a * math.sin(x * kx + y * ky + ph) for kx, ky, ph, a in waves) / sum(w[3] for w in waves)
    return f

def frame_border(px, size, color, ornate):
    """Cadre de rarete : filet sombre, double bordure a la couleur de la rarete, gemmes aux coins (ornements si ornate)."""
    dark = mix(color, (0, 0, 0), 0.7)
    for y in range(size):
        for x in range(size):
            e = min(x, y, size - 1 - x, size - 1 - y)
            if e == 0: blend(px[y][x], dark, 1)
            elif e <= 2: blend(px[y][x], mix(color, (255, 255, 255), 0.25 if e == 1 else 0), 0.95)
            elif e == 3: blend(px[y][x], dark, 0.8)
            elif e == 5: blend(px[y][x], color, 0.45)
            elif e < 10: add(px[y][x], color, 0.05 * (10 - e) / 5)                               # lueur interieure
    gems = [(6, 6), (size - 7, 6), (6, size - 7), (size - 7, size - 7)]
    if ornate: gems += [(size // 2, 3), (size // 2, size - 4), (3, size // 2), (size - 4, size // 2)]
    for gx, gy in gems:
        r = 5 if ornate else 4
        for y in range(gy - r, gy + r + 1):
            for x in range(gx - r, gx + r + 1):
                if 0 <= x < size and 0 <= y < size:
                    d = abs(x - gx) + abs(y - gy)
                    if d <= r:
                        blend(px[y][x], mix(color, (255, 255, 255), 0.45 * (1 - d / r)) if d < r else dark, 1)
        if 0 <= gx - 1 < size and 0 <= gy - 1 < size: blend(px[gy - 1][gx - 1], (255, 255, 255), 0.9)

def ability_icon(path, type_rules, frame, fn, size=128, seed=1, ornate=False):
    """Icone de competence : fond radial et nebuleuse, cercle runique, rayons, etoiles, embleme en relief, cadre de rarete."""
    rnd = random.Random(seed); top, bot, acc, glyph = type_rules["top"], type_rules["bottom"], type_rules["accent"], type_rules["glyph"]
    neb = _noise(rnd)
    c = size / 2
    px = []
    for y in range(size):
        row = []
        for x in range(size):
            d = math.hypot(x + .5 - c, y + .5 - c) / c
            base = mix(mix(top, acc, 0.22), bot, min(1.0, d ** 1.4))                                 # lumiere au centre, vignette
            base = mix(base, acc, max(0.0, neb(x, y)) * 0.18 * (1 - min(1, d)))                       # nebuleuse
            row.append(base + [255])
        px.append(row)
    for y in range(size):                                                                        # rayons + cercle runique
        for x in range(size):
            a = math.atan2(y + .5 - c, x + .5 - c); d = math.hypot(x + .5 - c, y + .5 - c) / c
            add(px[y][x], acc, (math.cos(a * 10) * 0.5 + 0.5) ** 10 * max(0, 1 - d) * 0.25)
            for rr, wdt in ((0.86, 0.012), (0.78, 0.008)):
                if abs(d - rr) < wdt: add(px[y][x], acc, 0.35)
            if 0.79 < d < 0.85 and (math.degrees(a) % 15) < 2.2: add(px[y][x], glyph, 0.35)        # graduations runiques
    for _ in range(55):
        x, y, b = rnd.randrange(size), rnd.randrange(size), rnd.uniform(0.25, 0.9)
        add(px[y][x], glyph, b)
        if b > 0.75:
            for ox, oy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                if 0 <= x + ox < size and 0 <= y + oy < size: add(px[y + oy][x + ox], glyph, b * 0.35)
    draw_emblem(px, size, size, c, c, size * 0.34, fn, glyph, acc)
    frame_border(px, size, frame, ornate)
    png.write(path, size, size, px)

def _vanilla_png(zip_path, entry):
    with zipfile.ZipFile(zip_path) as z, tempfile.NamedTemporaryFile(suffix='.png', delete=False) as f:
        f.write(z.read(entry)); tmp = f.name
    try: return png.read(tmp)
    finally: os.remove(tmp)

def item_icon(path, zip_path, type_rules, fn):
    """Pierre de rune vanilla, glyphe rouge efface, embleme du type grave au centre."""
    w, h, px = _vanilla_png(zip_path, 'Common/Icons/ItemsGenerated/Rune_Enrage.png')
    def warmth(p): return max(0.0, min(1.0, (p[0] - p[2] - 15) / 90.0)) if p[3] > 0 else 0
    stone = [p for row in px for p in row if p[3] > 200 and warmth(p) == 0]
    sc = [sum(p[i] for p in stone) / max(1, len(stone)) for i in range(3)]
    for y in range(h):
        for x in range(w):
            p = px[y][x]; k = warmth(p)
            if k > 0 and math.hypot(x - 33, y - 34) < 22:
                for i in range(3): p[i] = p[i] * (1 - k) + sc[i] * 0.8 * k
    draw_emblem(px, w, h, 35.5, 33.3, 10.5, fn, type_rules["glyph"], type_rules["accent"], ss=4)
    png.write(path, w, h, px)

def texture(path, zip_path, type_rules):
    """Texture du modele 3D : glyphes rouges de la rune vanilla recolores a la teinte du type."""
    w, h, px = _vanilla_png(zip_path, 'Common/Items/Runes/Rune_Textures/Rune_Ability_Enrage.png')
    hue = type_rules["hue"]
    for row in px:
        for p in row:
            if p[3] == 0: continue
            _, s, v = colorsys.rgb_to_hsv(*(c / 255 for c in p[:3]))
            hh = colorsys.rgb_to_hsv(*(c / 255 for c in p[:3]))[0]
            if s > 0.35 and (hh < 0.12 or hh > 0.9):
                r, g, b = colorsys.hsv_to_rgb(hue, 0.75, min(1, v * 1.2))
                p[0], p[1], p[2] = r * 255, g * 255, b * 255
    png.write(path, w, h, px)

def generate(type_name, rarity_rules, type_rules, zip_path, ability_path, item_path, texture_path, seed=1, emblem=None):
    fn = EMBLEMS[emblem or type_name]
    for p in (ability_path, item_path, texture_path): os.makedirs(os.path.dirname(p), exist_ok=True)
    ability_icon(ability_path, type_rules, rarity_rules["frame"], fn, seed=seed, ornate=rarity_rules.get("itemLevel", 0) >= 90)
    item_icon(item_path, zip_path, type_rules, fn)
    texture(texture_path, zip_path, type_rules)

if __name__ == '__main__':
    import json
    here = os.path.dirname(os.path.abspath(__file__))
    rules = json.load(open(os.path.join(here, 'rune_rules.json'), encoding='utf-8'))
    t, r, out = sys.argv[1], sys.argv[2], sys.argv[3]
    emblem = sys.argv[4] if len(sys.argv) > 4 else None
    zp = os.environ.get('HYTALE_ASSETS_ZIP', os.path.expanduser('~/Library/Application Support/Hytale/install/release/package/game/latest/Assets.zip'))
    generate(t, rules['rarities'][r], rules['types'][t], zp, f'{out}/ability.png', f'{out}/item.png', f'{out}/texture.png', emblem=emblem)
    print('ok', out)
