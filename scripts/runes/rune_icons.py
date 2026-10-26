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

def e_onde(u, v):
    """Onde de repulsion : noyau, ondes concentriques et quatre fleches qui chassent vers l'exterieur."""
    d = min(circle(u, v, 0, 0, 0.17), arc(u, v, 0, 0, 0.4, 0.04, -math.pi, math.pi), ring(u, v, 0, 0, 0.6, 0.025))
    for k in range(4):
        a = k * math.pi / 2; c, s = math.cos(a), math.sin(a); n = (-s, c)
        d = min(d, poly(u, v, [(0.97 * c, 0.97 * s), (0.72 * c + 0.2 * n[0], 0.72 * s + 0.2 * n[1]),
                               (0.72 * c - 0.2 * n[0], 0.72 * s - 0.2 * n[1])]))
    return d

def _flame(u, v, ox, oy, sx, sy):
    """Langue de feu (base ronde, pointe vers le haut), echelle sx/sy, base en (ox, oy)."""
    x, y = (u - ox) / sx, (v - oy) / sy
    body = smin(circle(x, y, 0, -0.3, 0.3), poly(x, y, [(-0.28, -0.38), (0.28, -0.38), (0.08, -0.8), (0.0, -1.1), (-0.06, -0.85)]), 0.18)
    return body * min(sx, sy)

def e_flammeche(u, v):
    """Flammeche : boule de feu lancee en diagonale, queue de flamme et trainees de vitesse."""
    a = -math.pi / 4; c, s = math.cos(a), math.sin(a)
    x, y = u * c - v * s, u * s + v * c                                 # repere tourne : la boule file vers le haut-droite
    head = circle(x, y, 0, -0.34, 0.36)
    tail = poly(x, y, [(-0.29, -0.28), (0.29, -0.28), (0.12, 0.25), (0.0, 0.85), (-0.12, 0.25)])
    ball = smin(head, tail, 0.15)
    streaks = min(seg(x, y, -0.52, 0.0, -0.52, 0.5, 0.035), seg(x, y, 0.52, 0.0, 0.52, 0.5, 0.035))
    return min(ball, streaks)

def e_mur_flammes(u, v):
    """Mur de flammes : cinq langues de feu dressees cote a cote sur une ligne de sol."""
    d = seg(u, v, -0.85, 0.62, 0.85, 0.62, 0.06)
    for x, h in ((-0.62, 0.62), (-0.31, 0.85), (0.0, 1.05), (0.31, 0.85), (0.62, 0.62)):
        d = min(d, _flame(u, v, x, 0.55, 0.5, h))
    return d

def e_marque(u, v):
    """Marque infernale : sceau (double cercle a pointes) avec une flamme au centre."""
    d = min(ring(u, v, 0, 0, 0.72, 0.045), ring(u, v, 0, 0, 0.56, 0.02))
    for k in range(6):
        a = -math.pi / 2 + k * math.pi / 3; c, s = math.cos(a), math.sin(a); n = (-s, c)
        d = min(d, poly(u, v, [(0.74 * c + 0.1 * n[0], 0.74 * s + 0.1 * n[1]), (0.95 * c, 0.95 * s), (0.74 * c - 0.1 * n[0], 0.74 * s - 0.1 * n[1])]))
    f = _flame(u, v, 0, 0.32, 0.62, 0.66)
    return min(d, max(f, -_flame(u, v, 0.0, 0.3, 0.3, 0.32)))

def e_aura_braise(u, v):
    """Aura de braise : silhouette au centre, dans un brasier dont les flammes l'encadrent des deux cotes."""
    body = _silhouette(u, v, 0, 0.05, 0.85)
    fire = 9.0
    for x, h in ((-0.7, 0.7), (-0.48, 1.0), (0.48, 1.0), (0.7, 0.7), (-0.25, 0.55), (0.25, 0.55)):
        fire = min(fire, _flame(u, v, x, 0.78, 0.4, h))
    fire = max(fire, -(body - 0.07))                                    # detoure la silhouette
    return min(body, fire, seg(u, v, -0.85, 0.84, 0.85, 0.84, 0.035))

def _flake(u, v, cx, cy, r, w):
    """Petit flocon a six branches."""
    return min(seg(u, v, cx - r * math.cos(a), cy - r * math.sin(a), cx + r * math.cos(a), cy + r * math.sin(a), w) for a in (0, math.pi / 3, 2 * math.pi / 3))

def _rot(u, v, a):
    c, s = math.cos(a), math.sin(a); return u * c - v * s, u * s + v * c

def e_eclat_givre(u, v):
    """Eclat de givre : cristal effile lance en diagonale, trainee de givre et flocon."""
    x, y = _rot(u, v, -math.pi / 4)
    shard = poly(x, y, [(0, -0.75), (0.2, -0.25), (0.1, 0.2), (0, 0.3), (-0.1, 0.2), (-0.2, -0.25)])
    facet = seg(x, y, 0, -0.65, 0, 0.15, 0.02)
    trail = min(seg(x, y, -0.32, 0.25, -0.32, 0.7, 0.03), seg(x, y, 0.32, 0.25, 0.32, 0.7, 0.03), seg(x, y, 0, 0.45, 0, 0.9, 0.035))
    return min(max(shard, -facet), trail, _flake(u, v, -0.55, -0.5, 0.17, 0.03))

def e_lance_cristal(u, v):
    """Lance de cristal : lance a large pointe de cristal qui fait voler une cible en eclats."""
    x, y = _rot(u, v, -math.pi / 4)
    head = poly(x, y, [(0, -0.98), (0.2, -0.6), (0.09, -0.42), (-0.09, -0.42), (-0.2, -0.6)])
    head = max(head, -seg(x, y, 0, -0.85, 0, -0.5, 0.02))                    # arete du cristal
    shaft = poly(x, y, [(0.065, -0.45), (0.065, 0.95), (-0.065, 0.95), (-0.065, -0.45)])
    guard = poly(x, y, [(-0.18, -0.38), (0.18, -0.38), (0.1, -0.3), (-0.1, -0.3)])
    burst = 9.0
    for k in range(5):                                                         # eclats projetes autour de la pointe
        a = -math.pi / 2 + (k - 2) * 0.55
        burst = min(burst, poly(x, y, [(0.42 * math.cos(a) - 0.05 * math.sin(a), -0.55 + 0.42 * math.sin(a) + 0.05 * math.cos(a)),
                                       (0.62 * math.cos(a), -0.55 + 0.62 * math.sin(a)),
                                       (0.42 * math.cos(a) + 0.05 * math.sin(a), -0.55 + 0.42 * math.sin(a) - 0.05 * math.cos(a))]))
    return min(head, shaft, guard, burst)

def e_prison_glace(u, v):
    """Prison de glace : silhouette prise dans un bloc de glace aux aretes vives, cristaux aux coins."""
    body = _silhouette(u, v, 0, 0.12, 0.62)
    frame = max(poly(u, v, [(-0.62, -0.72), (0.62, -0.72), (0.62, 0.72), (-0.62, 0.72)]), -poly(u, v, [(-0.5, -0.6), (0.5, -0.6), (0.5, 0.6), (-0.5, 0.6)]))
    shine = min(seg(u, v, -0.38, -0.45, -0.18, -0.25, 0.025), seg(u, v, -0.38, -0.28, -0.3, -0.2, 0.02))
    spikes = min(poly(u, v, [(-0.62, -0.72), (-0.82, -0.92), (-0.48, -0.72)]), poly(u, v, [(0.62, -0.72), (0.82, -0.92), (0.48, -0.72)]),
                 poly(u, v, [(-0.62, 0.72), (-0.82, 0.92), (-0.48, 0.72)]), poly(u, v, [(0.62, 0.72), (0.82, 0.92), (0.48, 0.72)]))
    return min(body, frame, shine, spikes)

def _footprint(u, v, cx, cy, sc, a):
    x, y = _rot(u - cx, v - cy, a); x, y = x / sc, y / sc
    sole = (math.hypot(x / 0.55, (y - 0.05) / 0.9) - 1) * 0.5
    heel = circle(x, y, 0, 0.95, 0.34)
    toes = min(circle(x, y, -0.32, -1.08, 0.13), circle(x, y, -0.08, -1.18, 0.14), circle(x, y, 0.16, -1.13, 0.12), circle(x, y, 0.36, -1.0, 0.11))
    return min(sole, heel, toes) * sc

def e_sentier_hiver(u, v):
    """Sentier d'hiver : trois empreintes de pas montant en diagonale, flocon en tete du chemin."""
    d = _flake(u, v, 0.52, -0.55, 0.28, 0.045)
    for cx, cy in ((-0.62, 0.55), (-0.18, 0.42), (-0.3, -0.12)):
        d = min(d, _footprint(u, v, cx, cy, 0.22, 0.45))
    return d

def _bolt(u, v, ax, ay, bx, by, w, kinks=3, amp=0.12):
    """Eclair en zigzag de A a B."""
    pts = [(ax, ay)]
    nx, ny = -(by - ay), bx - ax
    L = math.hypot(nx, ny) or 1; nx, ny = nx / L, ny / L
    for i in range(1, kinks + 1):
        t = i / (kinks + 1); o = amp * (1 if i % 2 else -1)
        pts.append((ax + (bx - ax) * t + nx * o, ay + (by - ay) * t + ny * o))
    pts.append((bx, by))
    return min(seg(u, v, *pts[i], *pts[i + 1], w) for i in range(len(pts) - 1))

def e_decharge(u, v):
    """Decharge : eclair epais tire en diagonale qui eclate en etoile sur sa cible."""
    x, y = _rot(u, v, -0.6)
    bolt = poly(x, y, [(0.1, -0.95), (-0.28, -0.05), (-0.02, -0.05), (-0.18, 0.55), (0.3, -0.2), (0.04, -0.2), (0.26, -0.95)])
    burst = 9.0
    cx, cy = 0.42, 0.58
    for k in range(8):
        a = k * math.pi / 4; L = 0.36 if k % 2 == 0 else 0.22
        burst = min(burst, poly(u, v, [(cx + 0.06 * math.cos(a + 1.57), cy + 0.06 * math.sin(a + 1.57)), (cx + L * math.cos(a), cy + L * math.sin(a)),
                                       (cx + 0.06 * math.cos(a - 1.57), cy + 0.06 * math.sin(a - 1.57))]))
    return min(bolt, burst, circle(u, v, cx, cy, 0.1))

def e_chaine(u, v):
    """Chaine d'eclairs : un eclair epais qui relie cinq cibles, de plus en plus fin."""
    pts = [(-0.75, 0.55), (-0.32, -0.15), (0.08, 0.42), (0.42, -0.3), (0.78, 0.15)]
    d = 9.0
    for i, (x, y) in enumerate(pts):
        d = min(d, circle(u, v, x, y, 0.16 - i * 0.015))
        if i < len(pts) - 1:
            d = min(d, _bolt(u, v, x, y, *pts[i + 1], 0.075 - i * 0.01, 2, 0.09))
    return d

def e_orbe(u, v):
    """Orbe statique : sphere d'orage, eclairs qui en jaillissent et anneau d'orbite."""
    core = max(circle(u, v, 0, 0, 0.36), -_bolt(u, v, -0.12, -0.28, 0.1, 0.28, 0.045, 2, 0.1))
    a = 0.4; c, s = math.cos(a), math.sin(a); x, y = u * c + v * s, -u * s + v * c
    orbit = max(abs(math.hypot(x, y * 3.0) - 0.72) / 3.0 - 0.03, -circle(u, v, 0, 0, 0.42))
    d = min(core, orbit)
    for ang in (-2.3, -0.6, 1.0, 2.6):
        d = min(d, _bolt(u, v, 0.45 * math.cos(ang), 0.45 * math.sin(ang), 0.88 * math.cos(ang), 0.88 * math.sin(ang), 0.035, 2, 0.06))
    return d

def e_blizzard(u, v):
    """Blizzard : nuage de tempete, rafales obliques et flocons qui tombent."""
    cloud = min(circle(u, v, -0.35, -0.42, 0.26), circle(u, v, 0.05, -0.55, 0.32), circle(u, v, 0.42, -0.4, 0.24), poly(u, v, [(-0.6, -0.35), (0.65, -0.35), (0.6, -0.2), (-0.55, -0.2)]))
    winds = min(seg(u, v, -0.7, 0.0, -0.1, 0.0, 0.035), seg(u, v, -0.45, 0.22, 0.35, 0.22, 0.035), arc(u, v, 0.35, 0.1, 0.12, 0.035, -1.6, 1.6))
    flakes = min(_flake(u, v, -0.45, 0.55, 0.15, 0.03), _flake(u, v, 0.05, 0.72, 0.13, 0.028), _flake(u, v, 0.55, 0.5, 0.15, 0.03))
    return min(cloud, winds, flakes)

def e_domaine_elem(u, v):
    """Domaine elementaire : cercle partage en trois, flamme, flocon et eclair."""
    d = ring(u, v, 0, 0, 0.82, 0.05)
    for k in range(3):
        a = -math.pi / 2 + k * 2 * math.pi / 3
        d = min(d, seg(u, v, 0.12 * math.cos(a), 0.12 * math.sin(a), 0.8 * math.cos(a), 0.8 * math.sin(a), 0.03))
    d = min(d, circle(u, v, 0, 0, 0.1))
    fire = _flame(u, v, 0.0, 0.66, 0.42, 0.42)                                             # bas : flamme
    fire = _flame(u, v, -0.42, -0.02, 0.4, 0.42)                                           # gauche-haut
    flake = _flake(u, v, 0.43, -0.24, 0.2, 0.04)                                           # droite-haut
    bolt = _bolt(u, v, -0.08, 0.3, 0.12, 0.66, 0.05, 2, 0.08)                              # bas
    return min(d, fire, flake, bolt)

def e_tectonique(u, v):
    """Tremblement tectonique : sol fracture par une faille en zigzag, blocs souleves de part et d'autre."""
    ground = max(poly(u, v, [(-0.9, 0.15), (0.9, 0.15), (0.9, 0.75), (-0.9, 0.75)]), -_bolt(u, v, -0.05, 0.1, 0.05, 0.8, 0.07, 3, 0.12))
    blocks = min(poly(u, v, [(-0.75, 0.1), (-0.45, -0.05), (-0.3, -0.4), (-0.6, -0.3)]), poly(u, v, [(0.25, -0.15), (0.6, -0.5), (0.8, -0.2), (0.5, 0.1)]))
    shards = min(seg(u, v, -0.15, -0.35, -0.05, -0.7, 0.035), seg(u, v, 0.12, -0.25, 0.25, -0.62, 0.035), circle(u, v, 0.0, -0.82, 0.05))
    return min(ground, blocks, shards)

def e_danse_vents(u, v):
    """Danse des vents : silhouette soulevee par un tourbillon de rafales qui s'elargit vers le haut."""
    body = _silhouette(u, v, 0, -0.38, 0.55)
    d = body
    for i, (cy, r) in enumerate(((0.2, 0.62), (0.42, 0.45), (0.62, 0.3), (0.8, 0.16))):
        rx = abs(math.hypot(u / r, (v - cy) / (r * 0.28)) - 1) * r * 0.28 - 0.03
        gap = poly(u, v, [(-0.05 + i * 0.05, cy - 0.3), (0.25 + i * 0.05, cy - 0.3), (0.25 + i * 0.05, cy + 0.3), (-0.05 + i * 0.05, cy + 0.3)])
        d = min(d, max(rx, -gap))
    return d

def e_halo(u, v):
    """Halo protecteur : silhouette sous un halo, enveloppee d'un bouclier en arc."""
    body = _silhouette(u, v, 0, 0.18, 0.62)
    halo = abs(math.hypot(u / 0.3, (v + 0.58) / 0.09) - 1) * 0.09 - 0.022
    shield = arc(u, v, 0, 0.12, 0.72, 0.05, -math.pi * 0.95, -math.pi * 0.05)
    sides = min(seg(u, v, -0.72, 0.1, -0.5, 0.7, 0.05), seg(u, v, 0.72, 0.1, 0.5, 0.7, 0.05))
    return min(body, halo, shield, sides)

def e_arme_benie(u, v):
    """Arme benie : epee pointe en haut, auree d'une croix de lumiere."""
    blade = poly(u, v, [(0, -0.92), (0.09, -0.75), (0.07, 0.25), (-0.07, 0.25), (-0.09, -0.75)])
    blade = max(blade, -seg(u, v, 0, -0.7, 0, 0.18, 0.018))
    guard = poly(u, v, [(-0.36, 0.25), (0.36, 0.25), (0.3, 0.36), (-0.3, 0.36)])
    hilt = min(seg(u, v, 0, 0.36, 0, 0.72, 0.05), circle(u, v, 0, 0.8, 0.08))
    rays = 9.0
    for k in range(8):
        a = k * math.pi / 4; L = 0.75 if k % 2 == 0 else 0.55
        rays = min(rays, seg(u, v, -0.3 * 0 + 0.3 * math.cos(a) * 0 + 0.36 * math.cos(a), -0.35 + 0.36 * math.sin(a), L * math.cos(a), -0.35 + L * math.sin(a), 0.025))
    return min(blade, guard, hilt, rays)

def e_glyphes(u, v):
    """Glyphes foudroyants : trois cercles runiques en triangle, un eclair tombe sur l'un d'eux."""
    d = 9.0
    for cx, cy in ((-0.5, 0.5), (0.5, 0.5), (0.0, -0.25)):
        d = min(d, ring(u, v, cx, cy, 0.24, 0.035), seg(u, v, cx - 0.12, cy, cx + 0.12, cy, 0.03), seg(u, v, cx, cy - 0.12, cx, cy + 0.12, 0.03))
    d = min(d, seg(u, v, -0.5, 0.5, 0.5, 0.5, 0.015), seg(u, v, -0.5, 0.5, 0, -0.25, 0.015), seg(u, v, 0.5, 0.5, 0, -0.25, 0.015))
    return min(d, _bolt(u, v, 0.3, -0.95, 0.04, -0.5, 0.05, 2, 0.08))

def e_egide(u, v):
    """Egide royale : dome de lumiere pose sur le sol, couronne au sommet, fleche qui se brise sur la paroi."""
    dome = max(ring(u, v, 0, 0.62, 0.7, 0.055), v - 0.62)
    inner = max(ring(u, v, 0, 0.62, 0.5, 0.02), v - 0.62)
    ground = seg(u, v, -0.9, 0.66, 0.9, 0.66, 0.045)
    crown = poly(u, v, [(-0.28, -0.12), (-0.28, -0.45), (-0.14, -0.28), (0, -0.55), (0.14, -0.28), (0.28, -0.45), (0.28, -0.12)])
    arrow = min(seg(u, v, 0.98, -0.25, 0.72, 0.05, 0.03), poly(u, v, [(0.66, 0.12), (0.78, 0.08), (0.64, -0.02)]))
    sparks = min(seg(u, v, 0.6, 0.0, 0.5, -0.1, 0.022), seg(u, v, 0.62, 0.12, 0.48, 0.16, 0.022))
    return min(dome, inner, ground, crown, arrow, sparks)

def _heart(u, v, cx, cy, sc):
    x, y = (u - cx) / sc, (v - cy) / sc
    d = min(circle(x, y, -0.28, -0.12, 0.32), circle(x, y, 0.28, -0.12, 0.32), poly(x, y, [(-0.56, 0.0), (0.56, 0.0), (0, 0.62)]))
    return d * sc

def e_soin(u, v):
    """Soin mineur : coeur sous une croix de soin, petites etincelles qui montent."""
    cross = min(poly(u, v, [(-0.12, -0.92), (0.12, -0.92), (0.12, -0.28), (-0.12, -0.28)]), poly(u, v, [(-0.34, -0.72), (0.34, -0.72), (0.34, -0.48), (-0.34, -0.48)]))
    heart = _heart(u, v, 0, 0.32, 0.75)
    sparks = min(_flake(u, v, -0.6, -0.2, 0.09, 0.022), _flake(u, v, 0.62, -0.05, 0.09, 0.022))
    return min(cross, heart, sparks)

def e_fontaine(u, v):
    """Fontaine de vie : vasque d'ou jaillit un jet qui retombe en gouttes de part et d'autre."""
    basin = poly(u, v, [(-0.62, 0.42), (0.62, 0.42), (0.45, 0.62), (-0.45, 0.62)])
    stem = poly(u, v, [(-0.1, 0.62), (0.1, 0.62), (0.18, 0.85), (-0.18, 0.85)])
    jet = min(seg(u, v, 0, 0.38, 0, -0.75, 0.05), arc(u, v, -0.35, -0.45, 0.35, 0.035, -math.pi, 0), arc(u, v, 0.35, -0.45, 0.35, 0.035, -math.pi, 0))
    def drop(cx, cy, r):
        return smin(circle(u, v, cx, cy, r), poly(u, v, [(cx - r * 0.8, cy - r * 0.4), (cx + r * 0.8, cy - r * 0.4), (cx, cy - r * 2.2)]), 0.03)
    drops = min(drop(-0.7, 0.0, 0.08), drop(0.7, 0.0, 0.08), drop(-0.72, 0.28, 0.06), drop(0.72, 0.28, 0.06))
    return min(basin, stem, jet, drops)

def e_lance_lumiere(u, v):
    """Lance de lumiere : rayon epais tire en diagonale, aure de rayons, qui frappe un crane."""
    x, y = _rot(u, v, -math.pi / 4)
    beam = poly(x, y, [(-0.09, 0.95), (0.09, 0.95), (0.05, -0.45), (-0.05, -0.45)])
    rays = min(seg(x, y, -0.2, 0.6, -0.32, 0.1, 0.025), seg(x, y, 0.2, 0.6, 0.32, 0.1, 0.025), seg(x, y, -0.18, 0.1, -0.26, -0.3, 0.02), seg(x, y, 0.18, 0.1, 0.26, -0.3, 0.02))
    skull = max(min(circle(u, v, 0.48, -0.5, 0.24), poly(u, v, [(0.36, -0.38), (0.6, -0.38), (0.56, -0.22), (0.4, -0.22)])),
                -min(circle(u, v, 0.4, -0.52, 0.06), circle(u, v, 0.56, -0.52, 0.06)))
    return min(beam, rays, skull)

def e_lien_ame(u, v):
    """Lien d'ame : deux silhouettes face a face, reliees par un fil d'or qui passe par un coeur."""
    a = _silhouette(u, v, -0.6, 0.22, 0.5)
    b = _silhouette(u, v, 0.6, 0.22, 0.5)
    thread = min(seg(u, v, -0.45, -0.05, -0.18, -0.42, 0.025), seg(u, v, 0.45, -0.05, 0.18, -0.42, 0.025))
    return min(a, b, thread, _heart(u, v, 0, -0.52, 0.36))

def e_follet(u, v):
    """Esprit follet : petite flamme d'esprit aux grands yeux, ailes de lumiere, trait qu'il decoche."""
    body = _flame(u, v, -0.15, 0.25, 0.75, 0.85)
    eyes = min(circle(u, v, -0.27, -0.05, 0.07), circle(u, v, -0.03, -0.05, 0.07))
    wings = min(arc(u, v, -0.15, -0.1, 0.55, 0.035, -2.9, -2.0), arc(u, v, -0.15, -0.1, 0.55, 0.035, -1.1, -0.25))
    shot = min(seg(u, v, 0.35, 0.05, 0.85, -0.35, 0.035), _flake(u, v, 0.85, -0.35, 0.1, 0.025))
    return min(max(body, -eyes), wings, shot)

def e_golem(u, v):
    """Golem de pierre : colosse trapu de blocs, epaules massives, yeux luisants."""
    head = poly(u, v, [(-0.2, -0.78), (0.2, -0.78), (0.24, -0.48), (-0.24, -0.48)])
    torso = poly(u, v, [(-0.42, -0.42), (0.42, -0.42), (0.34, 0.25), (-0.34, 0.25)])
    arms = min(poly(u, v, [(-0.48, -0.42), (-0.78, -0.3), (-0.82, 0.38), (-0.56, 0.42), (-0.46, 0.0)]), poly(u, v, [(0.48, -0.42), (0.78, -0.3), (0.82, 0.38), (0.56, 0.42), (0.46, 0.0)]))
    legs = min(poly(u, v, [(-0.32, 0.28), (-0.06, 0.28), (-0.08, 0.85), (-0.36, 0.85)]), poly(u, v, [(0.06, 0.28), (0.32, 0.28), (0.36, 0.85), (0.08, 0.85)]))
    cracks = min(seg(u, v, -0.1, -0.3, 0.05, -0.05, 0.022), seg(u, v, 0.05, -0.05, -0.05, 0.15, 0.022))
    eyes = min(seg(u, v, -0.13, -0.63, -0.04, -0.63, 0.03), seg(u, v, 0.04, -0.63, 0.13, -0.63, 0.03))
    return max(min(head, torso, arms, legs), -min(cracks, eyes))

def e_sentinelle(u, v):
    """Sentinelle de givre : tour de cristal effilee qui tire un eclat, flocon au sommet."""
    tower = poly(u, v, [(-0.3, 0.85), (0.3, 0.85), (0.18, -0.25), (0.0, -0.7), (-0.18, -0.25)])
    facet = seg(u, v, 0, -0.55, 0, 0.75, 0.025)
    base = poly(u, v, [(-0.5, 0.72), (0.5, 0.72), (0.5, 0.88), (-0.5, 0.88)])
    shot = min(seg(u, v, 0.2, -0.35, 0.75, -0.6, 0.03), poly(u, v, [(0.68, -0.68), (0.88, -0.66), (0.74, -0.5)]))
    return min(max(tower, -facet), base, shot, _flake(u, v, -0.55, -0.55, 0.16, 0.03))

def e_meute(u, v):
    """Meute spectrale : trois tetes de loup de face (oreilles dressees, yeux fendus), la centrale devant."""
    def wolf(cx, cy, sc):
        x, y = (u - cx) / sc, (v - cy) / sc
        head = poly(x, y, [(-0.5, -0.6), (-0.22, -0.22), (0.22, -0.22), (0.5, -0.6), (0.48, 0.0), (0.16, 0.55), (-0.16, 0.55), (-0.48, 0.0)])
        eyes = min(seg(x, y, -0.3, -0.02, -0.12, 0.06, 0.035), seg(x, y, 0.3, -0.02, 0.12, 0.06, 0.035))
        nose = circle(x, y, 0, 0.42, 0.07)
        return max(head, -min(eyes, nose)) * sc
    back = min(wolf(-0.5, -0.12, 0.6), wolf(0.5, -0.12, 0.6))
    front = wolf(0, 0.18, 0.95)
    return min(max(back, -(front - 0.06)), front)

EMBLEMS = {"Feu": e_feu, "Glace": e_glace, "Terre": e_terre, "Air": e_air, "Tenebres": e_tenebres, "Lumiere": e_lumiere,
           "Temps": e_temps, "Espace": e_espace, "Combat": e_combat, "Spatial": e_spatial, "Dragonic": e_dragonic, "Foudre": e_foudre,
           "Puits": e_puits, "Autorite": e_autorite, "Cyclone": e_cyclone, "Telekinesie": e_telekinesie, "Clonage": e_clonage, "Miroir": e_miroir,
           "Onde": e_onde, "Flammeche": e_flammeche, "MurFlammes": e_mur_flammes, "Marque": e_marque, "AuraBraise": e_aura_braise,
           "EclatGivre": e_eclat_givre, "LanceCristal": e_lance_cristal, "PrisonGlace": e_prison_glace, "SentierHiver": e_sentier_hiver,
           "Decharge": e_decharge, "Chaine": e_chaine, "Orbe": e_orbe,
           "Blizzard": e_blizzard, "DomaineElem": e_domaine_elem, "Tectonique": e_tectonique, "DanseVents": e_danse_vents,
           "Halo": e_halo, "ArmeBenie": e_arme_benie, "Glyphes": e_glyphes, "Egide": e_egide,
           "Soin": e_soin, "Fontaine": e_fontaine, "LanceLumiere": e_lance_lumiere, "LienAme": e_lien_ame,
           "Follet": e_follet, "Golem": e_golem, "Sentinelle": e_sentinelle, "Meute": e_meute}

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
