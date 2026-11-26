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

def e_boule_feu(u, v):
    """Boule de feu : grosse sphere en bas, embrasee : trois grandes langues de feu montent d'elle, une ligne d'eclat la cerne."""
    ball = circle(u, v, 0, 0.3, 0.5)
    fire = min(_flame(u, v, -0.34, 0.2, 0.42, 0.72), _flame(u, v, 0.0, 0.1, 0.5, 1.0), _flame(u, v, 0.34, 0.2, 0.42, 0.72))
    d = smin(ball, fire, 0.12)
    return max(d, -ring(u, v, 0, 0.3, 0.32, 0.03))

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

def _skull(u, v, cx, cy, sc):
    x, y = (u - cx) / sc, (v - cy) / sc
    head = min(circle(x, y, 0, -0.1, 0.5), poly(x, y, [(-0.3, 0.2), (0.3, 0.2), (0.24, 0.55), (-0.24, 0.55)]))
    holes = min(circle(x, y, -0.2, -0.05, 0.13), circle(x, y, 0.2, -0.05, 0.13), poly(x, y, [(-0.06, 0.22), (0.06, 0.22), (0, 0.1)]),
                seg(x, y, -0.1, 0.42, -0.1, 0.56, 0.03), seg(x, y, 0.1, 0.42, 0.1, 0.56, 0.03))
    return max(head, -holes) * sc

def e_marque_funeste(u, v):
    """Marque funeste : crane dans un viseur de malediction (cercle et quatre pointes)."""
    d = min(ring(u, v, 0, 0, 0.78, 0.04), _skull(u, v, 0, 0.02, 0.8))
    for k in range(4):
        a = k * math.pi / 2 + math.pi / 4; c, s = math.cos(a), math.sin(a)
        d = min(d, poly(u, v, [(0.62 * c - 0.07 * s, 0.62 * s + 0.07 * c), (0.95 * c, 0.95 * s), (0.62 * c + 0.07 * s, 0.62 * s - 0.07 * c)]))
    return d

def e_puits_ames(u, v):
    """Puits d'ames : tourbillon qui plonge vers une fosse, ames aspirees en spirale."""
    d = 9.0
    for cy, r in ((-0.55, 0.7), (-0.22, 0.5), (0.08, 0.32), (0.32, 0.16)):
        d = min(d, abs(math.hypot(u, (v - cy) * 3.2) - r) / 3.2 - 0.03)
    for cx, cy, sc in ((-0.62, -0.75, 0.3), (0.55, -0.15, 0.24), (-0.3, 0.2, 0.18)):
        d = min(d, circle(u, v, cx, cy, sc * 0.4), seg(u, v, cx, cy, cx * 0.5, cy + 0.25, 0.025))
    return min(d, circle(u, v, 0, 0.55, 0.12))

def e_releve(u, v):
    """Releve des morts : tombe arrondie marquee d'une croix, main squelettique qui jaillit du sol devant elle."""
    stone = min(circle(u, v, -0.25, -0.3, 0.32), poly(u, v, [(-0.57, -0.3), (0.07, -0.3), (0.07, 0.55), (-0.57, 0.55)]))
    cross = min(poly(u, v, [(-0.29, -0.42), (-0.21, -0.42), (-0.21, 0.1), (-0.29, 0.1)]), poly(u, v, [(-0.42, -0.25), (-0.08, -0.25), (-0.08, -0.17), (-0.42, -0.17)]))
    ground = poly(u, v, [(-0.9, 0.55), (0.9, 0.55), (0.9, 0.7), (-0.9, 0.7)])
    arm = seg(u, v, 0.5, 0.6, 0.45, 0.0, 0.065)
    palm = circle(u, v, 0.45, -0.02, 0.1)
    fingers = min(seg(u, v, 0.42, -0.05, 0.25, -0.38, 0.04), seg(u, v, 0.45, -0.07, 0.4, -0.5, 0.04), seg(u, v, 0.48, -0.07, 0.58, -0.47, 0.04),
                  seg(u, v, 0.52, -0.02, 0.72, -0.3, 0.04), seg(u, v, 0.4, 0.02, 0.2, -0.05, 0.035))
    return min(max(stone, -cross), ground, arm, palm, fingers)

def e_ames_errantes(u, v):
    """Ames errantes : trois fantomes a queue effilee qui tournent autour d'un coeur sombre."""
    d = circle(u, v, 0, 0, 0.16)
    for k in range(3):
        a = -math.pi / 2 + k * 2 * math.pi / 3
        cx, cy = 0.55 * math.cos(a), 0.55 * math.sin(a)
        t = a + math.pi / 2                                                     # queue tangente a l'orbite
        tail = poly(u, v, [(cx - 0.15 * math.cos(a), cy - 0.15 * math.sin(a)), (cx + 0.15 * math.cos(a), cy + 0.15 * math.sin(a)),
                           (cx - 0.45 * math.cos(t), cy - 0.45 * math.sin(t))])
        ghost = max(min(circle(u, v, cx, cy, 0.17), tail), -min(circle(u, v, cx + 0.06 * math.cos(t), cy + 0.06 * math.sin(t) - 0.03, 0.04),
                                                              circle(u, v, cx + 0.06 * math.cos(t) - 0.08, cy + 0.06 * math.sin(t) - 0.03, 0.04)))
        d = min(d, ghost)
    return min(d, ring(u, v, 0, 0, 0.55, 0.015))

def e_phylactere(u, v):
    """Phylactere : cristal taille enchasse dans une monture, ame (flamme) captive au centre."""
    gem = poly(u, v, [(0, -0.85), (0.38, -0.35), (0.3, 0.45), (0, 0.75), (-0.3, 0.45), (-0.38, -0.35)])
    soul = _flame(u, v, 0, 0.2, 0.3, 0.42)
    facets = min(seg(u, v, -0.38, -0.35, 0.38, -0.35, 0.02), seg(u, v, 0, -0.85, 0, -0.35, 0.02))
    mount = min(arc(u, v, 0, 0.35, 0.55, 0.045, 0.2, math.pi - 0.2), seg(u, v, -0.52, 0.55, -0.7, 0.75, 0.04), seg(u, v, 0.52, 0.55, 0.7, 0.75, 0.04))
    return min(max(gem, -min(soul, facets)), mount, max(soul, -_flame(u, v, 0, 0.2, 0.15, 0.2)))

def e_domaine_mort(u, v):
    """Domaine de la mort : crane au centre d'un cercle de faux pointees vers l'interieur."""
    d = min(ring(u, v, 0, 0, 0.82, 0.04), _skull(u, v, 0, 0.05, 0.62))
    for k in range(6):
        a = k * math.pi / 3; c, s = math.cos(a), math.sin(a)
        hx, hy = 0.6 * c, 0.6 * s                                                # lame de faux au bout d'un manche radial
        d = min(d, seg(u, v, 0.92 * c, 0.92 * s, hx, hy, 0.025),
                max(circle(u, v, hx - 0.12 * s, hy + 0.12 * c, 0.17), -circle(u, v, hx - 0.18 * s + 0.05 * c, hy + 0.18 * c + 0.05 * s, 0.17)))
    return d

def e_forme_spectrale(u, v):
    """Forme spectrale : silhouette dont le bas se dissout en volutes, epee qui la traverse sans la toucher."""
    body = _silhouette(u, v, 0, -0.05, 0.85)
    fade = poly(u, v, [(-0.6, 0.25), (0.6, 0.25), (0.6, 1.0), (-0.6, 1.0)])
    wisps = min(arc(u, v, -0.12, 0.42, 0.14, 0.03, 0, math.pi), arc(u, v, 0.14, 0.55, 0.12, 0.03, math.pi, 2 * math.pi), arc(u, v, 0, 0.72, 0.1, 0.025, 0, math.pi))
    sword = min(seg(u, v, -0.85, 0.35, 0.85, -0.45, 0.035), seg(u, v, -0.7, 0.2, -0.6, 0.42, 0.04))
    return min(max(body, -fade), wisps, max(sword, -(body - 0.06)))

def e_armee_tombes(u, v):
    """Armee des tombes : trois cranes alignes sortant du sol, derriere une rangee de tombes."""
    ground = poly(u, v, [(-0.9, 0.62), (0.9, 0.62), (0.9, 0.75), (-0.9, 0.75)])
    d = ground
    for x, sc in ((-0.55, 0.5), (0.55, 0.5), (0.0, 0.62)):
        d = min(d, _skull(u, v, x, 0.3 if sc < 0.6 else 0.25, sc))
    for x in (-0.75, -0.28, 0.28, 0.75):
        d = min(d, min(circle(u, v, x, -0.42, 0.1), poly(u, v, [(x - 0.1, -0.42), (x + 0.1, -0.42), (x + 0.1, -0.08), (x - 0.1, -0.08)])))
    return d

def e_miroir_trompeur(u, v):
    """Miroir trompeur : contour vide au centre (le lanceur invisible), deux reflets pleins qui s'eloignent."""
    ghost = abs(_silhouette(u, v, 0, 0.05, 0.7)) - 0.022
    left = _silhouette(u, v, -0.6, 0.12, 0.55)
    right = _silhouette(u, v, 0.6, 0.12, 0.55)
    arrows = min(seg(u, v, -0.3, -0.62, -0.78, -0.62, 0.03), poly(u, v, [(-0.86, -0.62), (-0.74, -0.7), (-0.74, -0.54)]),
                 seg(u, v, 0.3, -0.62, 0.78, -0.62, 0.03), poly(u, v, [(0.86, -0.62), (0.74, -0.7), (0.74, -0.54)]))
    return min(ghost, left, right, arrows)

def e_galerie(u, v):
    """Galerie des glaces : cible au centre entouree d'un cercle de miroirs (rectangles) tournes vers elle."""
    d = circle(u, v, 0, 0, 0.16)
    for k in range(8):
        a = k * math.pi / 4; c, s = math.cos(a), math.sin(a)
        cx, cy = 0.68 * c, 0.68 * s
        w, h = 0.07, 0.17                                                     # miroir : rectangle tangent au cercle
        pts = [(cx + dx * c - dy * s, cy + dx * s + dy * c) for dx, dy in ((-w, -h), (w, -h), (w, h), (-w, h))]
        d = min(d, poly(u, v, pts), seg(u, v, 0.48 * c, 0.48 * s, 0.24 * c, 0.24 * s, 0.018))
    return d

def e_hallucination(u, v):
    """Hallucination : oeil dont la pupille est une spirale, cils ondules."""
    lens = max(circle(u, v, 0, 0.55, 0.95), circle(u, v, 0, -0.55, 0.95))
    eye = max(lens, -max(circle(u, v, 0, 0.55, 0.87), circle(u, v, 0, -0.55, 0.87)))
    spiral = 9.0
    pts = [(math.cos(t * 0.5) * (0.04 + t * 0.022), math.sin(t * 0.5) * (0.04 + t * 0.022)) for t in range(16)]
    for i in range(len(pts) - 1): spiral = min(spiral, seg(u, v, *pts[i], *pts[i + 1], 0.035))
    lashes = min(arc(u, v, -0.35, -0.55, 0.12, 0.025, 0, math.pi), arc(u, v, 0.0, -0.62, 0.12, 0.025, math.pi, 2 * math.pi), arc(u, v, 0.35, -0.55, 0.12, 0.025, 0, math.pi))
    return min(eye, spiral, lashes)

def _link(u, v, cx, cy, a, w=0.13, h=0.07, t=0.03):
    """Maillon de chaine (ellipse creuse) centre en (cx, cy), oriente selon a."""
    x, y = _rot(u - cx, v - cy, -a)
    return abs(math.hypot(x / w, y / h) - 1) * min(w, h) - t

def e_chaines_noires(u, v):
    """Chaines noires : trois cercles (ennemis) relies en triangle par des chaines de maillons."""
    pts = [(-0.55, 0.45), (0.55, 0.45), (0.0, -0.5)]
    d = min(circle(u, v, x, y, 0.17) for x, y in pts)
    for i in range(3):
        (ax, ay), (bx, by) = pts[i], pts[(i + 1) % 3]
        a = math.atan2(by - ay, bx - ax)
        for k in range(1, 5):
            t = k / 5
            d = min(d, _link(u, v, ax + (bx - ax) * t, ay + (by - ay) * t, a + (math.pi / 2 if k % 2 else 0)))
    return d

def e_pacte_sang(u, v):
    """Pacte de sang : dague pointe en bas d'ou tombe une grosse goutte de sang."""
    x, y = _rot(u, v, 0.5)
    blade = poly(x, y, [(-0.08, -0.85), (0.08, -0.85), (0.06, 0.05), (0.0, 0.25), (-0.06, 0.05)])
    guard = poly(x, y, [(-0.28, -0.85), (0.28, -0.85), (0.24, -0.76), (-0.24, -0.76)])
    hilt = seg(x, y, 0, -0.85, 0, -1.1, 0.05)
    drop = smin(circle(u, v, 0.25, 0.6, 0.2), poly(u, v, [(0.09, 0.52), (0.41, 0.52), (0.25, 0.18)]), 0.04)
    return min(blade, guard, hilt, drop)

def e_marionnette(u, v):
    """Marionnette : croix de marionnettiste dont les fils tiennent une petite silhouette."""
    cross = min(poly(u, v, [(-0.55, -0.82), (0.55, -0.82), (0.55, -0.72), (-0.55, -0.72)]), poly(u, v, [(-0.05, -0.95), (0.05, -0.95), (0.05, -0.55), (-0.05, -0.55)]))
    puppet = _silhouette(u, v, 0, 0.32, 0.62)
    strings = min(seg(u, v, -0.5, -0.72, -0.3, 0.2, 0.015), seg(u, v, 0.5, -0.72, 0.3, 0.2, 0.015), seg(u, v, 0, -0.55, 0, -0.1, 0.015))
    return min(cross, puppet, strings)

def e_ruee(u, v):
    """Ruee : epee pointee vers l'avant (droite), lignes de vitesse derriere, onde d'impact devant la pointe."""
    blade = poly(u, v, [(-0.12, -0.07), (0.5, -0.07), (0.74, 0.0), (0.5, 0.07), (-0.12, 0.07)])
    guard = seg(u, v, -0.14, -0.24, -0.14, 0.24, 0.05)
    hilt = min(seg(u, v, -0.14, 0, -0.4, 0, 0.045), circle(u, v, -0.46, 0, 0.08))
    speed = min(seg(u, v, -0.95, -0.38, -0.3, -0.38, 0.035), seg(u, v, -0.95, 0.38, -0.3, 0.38, 0.035),
                seg(u, v, -0.98, 0.0, -0.62, 0.0, 0.03))
    impact = arc(u, v, 0.72, 0.0, 0.26, 0.035, -1.1, 1.1)
    return min(blade, guard, hilt, speed, impact)

def e_voile_fumee(u, v):
    """Voile de fumee : bombe ronde (reflet creuse), meche qui grésille, nuage de fumee en contour qui s'en echappe."""
    bomb = max(circle(u, v, -0.2, 0.3, 0.4), -circle(u, v, -0.35, 0.15, 0.09))
    fuse = seg(u, v, 0.02, -0.05, 0.18, -0.3, 0.045)
    cloud = min(circle(u, v, 0.42, -0.5, 0.24), circle(u, v, 0.68, -0.24, 0.17), circle(u, v, 0.2, -0.74, 0.16))
    return min(bomb, fuse, abs(cloud) - 0.04)

def e_pluie_fleches(u, v):
    """Pluie de fleches : trois fleches qui tombent en biais vers une ligne de sol."""
    dx, dy = 0.38, 0.92
    n = math.hypot(dx, dy); dx, dy = dx / n, dy / n
    px, py = -dy, dx                                                   # perpendiculaire
    d = seg(u, v, -0.85, 0.82, 0.85, 0.82, 0.04)
    for tx, ty, length in ((-0.62, -0.85, 1.2), (-0.12, -0.95, 1.35), (0.38, -0.8, 1.15)):
        hx, hy = tx + dx * length, ty + dy * length                    # pointe
        d = min(d, seg(u, v, tx, ty, hx - dx * 0.16, hy - dy * 0.16, 0.03))
        d = min(d, poly(u, v, [(hx, hy), (hx - dx * 0.22 + px * 0.1, hy - dy * 0.22 + py * 0.1),
                                (hx - dx * 0.22 - px * 0.1, hy - dy * 0.22 - py * 0.1)]))
        for s in (-1, 1):                                              # empennage
            d = min(d, seg(u, v, tx + dx * 0.06, ty + dy * 0.06, tx - dx * 0.05 + px * 0.1 * s, ty - dy * 0.05 + py * 0.1 * s, 0.025))
    return d

def e_lancer_pieces(u, v):
    """Lancer de pieces : trois pieces (anneau + barre) en eventail, chacune suivie d'une trainee."""
    d = 9.0
    for cx, cy, r in ((-0.48, 0.42, 0.22), (0.02, -0.02, 0.2), (0.5, -0.5, 0.18)):
        d = min(d, ring(u, v, cx, cy, r, 0.05), seg(u, v, cx, cy - r * 0.45, cx, cy + r * 0.45, 0.04))
        d = min(d, seg(u, v, cx - r - 0.32, cy + r + 0.12, cx - r - 0.06, cy + r - 0.04, 0.03))
    return d

def e_projection(u, v):
    """Projection astrale : corps plein a gauche, son double en contour qui s'envole a droite, fil d'argent entre eux."""
    body = _silhouette(u, v, -0.42, 0.18, 0.62)
    spirit = abs(_silhouette(u, v, 0.4, -0.22, 0.62)) - 0.035
    cord = arc(u, v, 0.0, 0.5, 0.42, 0.03, -math.pi + 0.5, -0.35)
    sparks = min(circle(u, v, 0.78, -0.72, 0.05), circle(u, v, 0.62, -0.88, 0.035), circle(u, v, 0.86, -0.42, 0.035))
    return min(body, spirit, cord, sparks)

def e_ceinture(u, v):
    """Ceinture d'asteroides : noyau au centre, orbite inclinee, cinq blocs carres poses sur l'orbite."""
    a = 0.45; c, s = math.cos(a), math.sin(a)
    x, y = u * c + v * s, -u * s + v * c
    orbit = abs(math.hypot(x, y * 2.4) - 0.78) / 2.0 - 0.025
    d = min(orbit, circle(u, v, 0, 0, 0.2))
    for k in range(5):
        t = k * 2 * math.pi / 5 + 0.3
        ox, oy = 0.78 * math.cos(t), 0.78 / 2.4 * math.sin(t)
        bx, by = ox * c - oy * s, ox * s + oy * c
        h = 0.13 if k % 2 == 0 else 0.1
        d = min(d, poly(u, v, [(bx - h, by - h), (bx + h, by - h), (bx + h, by + h), (bx - h, by + h)]))
    return d

def e_prescience(u, v):
    """Prescience : oeil ouvert (pupille en sablier), deux silhouettes fantomes decalees de chaque cote."""
    eye = max(circle(u, v, 0, 0.55, 0.85), circle(u, v, 0, -0.55, 0.85))
    lid = abs(eye) - 0.04
    hour = min(poly(u, v, [(-0.14, -0.2), (0.14, -0.2), (0.02, 0), (-0.02, 0)]), poly(u, v, [(-0.02, 0), (0.02, 0), (0.14, 0.2), (-0.14, 0.2)]))
    iris = max(ring(u, v, 0, 0, 0.24, 0.035), -eye + 0.0)
    ghosts = min(abs(_silhouette(u, v, -0.72, 0.42, 0.42)) - 0.025, abs(_silhouette(u, v, 0.72, 0.42, 0.42)) - 0.025)
    return min(lid, hour, ring(u, v, 0, 0, 0.24, 0.035), max(ghosts, -(eye - 0.08)))

def e_rayon_celeste(u, v):
    """Rayon celeste : etoile en haut, colonne de lumiere evasee qui tombe, onde au sol a l'impact."""
    star = poly(u, v, [(0, -0.98), (0.07, -0.8), (0.24, -0.74), (0.07, -0.68), (0, -0.5), (-0.07, -0.68), (-0.24, -0.74), (-0.07, -0.8)])
    beam = poly(u, v, [(-0.08, -0.55), (0.08, -0.55), (0.24, 0.55), (-0.24, 0.55)])
    core = poly(u, v, [(-0.025, -0.45), (0.025, -0.45), (0.07, 0.45), (-0.07, 0.45)])
    ground = min(arc(u, v, 0, 0.75, 0.5, 0.035, -math.pi + 0.35, -0.35), seg(u, v, -0.85, 0.75, 0.85, 0.75, 0.04))
    return min(star, max(beam, -core), ground)

def e_distorsion(u, v):
    """Distorsion : cadran d'horloge dont le bas ondule et se deforme, aiguilles figees, fleche immobile prise dedans."""
    wob = 0.06 * math.sin(v * 9) * max(0.0, v + 0.1)
    face = ring(u + wob, v, 0, 0, 0.68, 0.045)
    hands = min(seg(u, v, 0, 0, 0, -0.45, 0.04), seg(u, v, 0, 0, 0.3, 0.12, 0.035), circle(u, v, 0, 0, 0.07))
    arrow = min(seg(u, v, -0.95, 0.62, -0.45, 0.38, 0.025), poly(u, v, [(-0.45, 0.32), (-0.34, 0.33), (-0.44, 0.44)]))
    ticks = min(seg(u, v, 0.68 * math.cos(a) * 0.82, 0.68 * math.sin(a) * 0.82, 0.68 * math.cos(a) * 0.95, 0.68 * math.sin(a) * 0.95, 0.025)
                for a in (k * math.pi / 2 for k in range(4)))
    return min(face, hands, arrow, ticks)

def e_retour_arriere(u, v):
    """Retour en arriere : fleche circulaire qui tourne a rebours autour d'un sablier."""
    loop = arc(u, v, 0, 0, 0.72, 0.05, -math.pi * 0.95, math.pi * 0.55)
    head = poly(u, v, [(-0.86, -0.2), (-0.56, -0.2), (-0.71, 0.08)])
    glass = min(poly(u, v, [(-0.26, -0.4), (0.26, -0.4), (0.03, 0), (-0.03, 0)]), poly(u, v, [(-0.03, 0), (0.03, 0), (0.26, 0.4), (-0.26, 0.4)]))
    return min(loop, head, glass, seg(u, v, -0.32, -0.46, 0.32, -0.46, 0.035), seg(u, v, -0.32, 0.46, 0.32, 0.46, 0.035))

def e_oeil_omniscient(u, v):
    """Oeil omniscient : grand oeil rayonnant, deux fleches qui s'echangent dessous."""
    eye = max(circle(u, v, 0, 0.42, 0.72), circle(u, v, 0, -0.62, 0.72))
    d = min(abs(eye) - 0.04, circle(u, v, 0, -0.1, 0.16), ring(u, v, 0, -0.1, 0.27, 0.03))
    for k in range(5):
        a = -math.pi / 2 + (k - 2) * 0.42
        d = min(d, seg(u, v, 0.66 * math.cos(a), -0.1 + 0.66 * math.sin(a), 0.86 * math.cos(a), -0.1 + 0.86 * math.sin(a), 0.03))
    swap = min(seg(u, v, -0.55, 0.52, 0.45, 0.52, 0.03), poly(u, v, [(0.45, 0.44), (0.6, 0.52), (0.45, 0.6)]),
               seg(u, v, 0.55, 0.74, -0.45, 0.74, 0.03), poly(u, v, [(-0.45, 0.66), (-0.6, 0.74), (-0.45, 0.82)]))
    return min(d, swap)

def e_sentence(u, v):
    """Sentence : marteau de juge au-dessus d'un cadran, l'aiguille sur l'echeance."""
    face = min(ring(u, v, 0, 0.25, 0.5, 0.04), seg(u, v, 0, 0.25, 0, -0.12, 0.035), seg(u, v, 0, 0.25, 0.28, 0.25, 0.03))
    head = poly(u, v, [(-0.62, -0.78), (-0.1, -0.78), (-0.1, -0.5), (-0.62, -0.5)])
    handle = seg(u, v, -0.36, -0.5, 0.42, -0.14, 0.045)
    return min(face, head, handle, circle(u, v, 0, 0.25, 0.06))

def e_bulle_temporelle(u, v):
    """Bulle temporelle : dome en contour au-dessus d'une ligne de sol, petit sablier a l'interieur."""
    dome = max(abs(circle(u, v, 0, 0.45, 0.85)) - 0.045, v - 0.45)
    ground = seg(u, v, -0.95, 0.5, 0.95, 0.5, 0.04)
    glass = min(poly(u, v, [(-0.22, -0.2), (0.22, -0.2), (0.03, 0.12), (-0.03, 0.12)]), poly(u, v, [(-0.03, 0.12), (0.03, 0.12), (0.22, 0.42), (-0.22, 0.42)]))
    return min(dome, ground, glass, seg(u, v, -0.28, -0.26, 0.28, -0.26, 0.03))

def e_arret_temps(u, v):
    """Arret du temps : cadran fissure, aiguilles bloquees, symbole pause au centre."""
    face = ring(u, v, 0, 0, 0.72, 0.05)
    crack = min(seg(u, v, 0.35, -0.62, 0.18, -0.3, 0.025), seg(u, v, 0.18, -0.3, 0.32, -0.1, 0.025))
    pause = min(poly(u, v, [(-0.24, -0.3), (-0.08, -0.3), (-0.08, 0.3), (-0.24, 0.3)]), poly(u, v, [(0.08, -0.3), (0.24, -0.3), (0.24, 0.3), (0.08, 0.3)]))
    ticks = min(seg(u, v, 0.6 * math.cos(a), 0.6 * math.sin(a), 0.7 * math.cos(a), 0.7 * math.sin(a), 0.03) for a in (k * math.pi / 2 for k in range(4)))
    return min(face, crack, pause, ticks)

def e_surregime(u, v):
    """Surregime : sablier incline vers l'avant, trois lignes de vitesse derriere lui."""
    a = -0.5; c, s = math.cos(a), math.sin(a)
    x, y = u * c + v * s, -u * s + v * c
    glass = min(poly(x, y, [(-0.3, -0.5), (0.3, -0.5), (0.03, 0), (-0.03, 0)]), poly(x, y, [(-0.03, 0), (0.03, 0), (0.3, 0.5), (-0.3, 0.5)]))
    caps = min(seg(x, y, -0.38, -0.56, 0.38, -0.56, 0.04), seg(x, y, -0.38, 0.56, 0.38, 0.56, 0.04))
    speed = min(seg(u, v, -0.95, -0.3, -0.55, -0.3, 0.03), seg(u, v, -0.98, 0.0, -0.5, 0.0, 0.03), seg(u, v, -0.95, 0.3, -0.55, 0.3, 0.03))
    return min(glass, caps, speed)

def e_echos_passe(u, v):
    """Echos du passe : silhouette pleine devant, trois contours de plus en plus loin derriere."""
    d = _silhouette(u, v, 0.42, 0.05, 0.75)
    for k, (dx, w) in enumerate(((0.12, 0.035), (-0.18, 0.028), (-0.48, 0.022))):
        d = min(d, max(abs(_silhouette(u, v, dx, 0.05, 0.75)) - w, -(_silhouette(u, v, dx + 0.3, 0.05, 0.75) - 0.04)))
    return d

def e_meteore(u, v):
    """Meteore : boule de feu qui plonge vers le bas a gauche, trois trainees derriere elle, cratere en cuvette au sol."""
    rock = circle(u, v, -0.12, 0.05, 0.3)
    streaks = min(seg(u, v, 0.1, -0.22, 0.85, -0.9, 0.07), seg(u, v, 0.2, 0.02, 0.92, -0.5, 0.045), seg(u, v, -0.12, -0.3, 0.45, -0.95, 0.045))
    crater = min(arc(u, v, -0.35, 0.55, 0.42, 0.04, 0.35, math.pi - 0.35), seg(u, v, -0.95, 0.62, -0.75, 0.62, 0.04), seg(u, v, 0.05, 0.62, 0.25, 0.62, 0.04))
    return min(rock, max(streaks, -(rock - 0.06)), crater)

def e_rayon_arcanique(u, v):
    """Rayon arcanique : main ouverte a gauche d'ou part un rayon epais qui traverse l'embleme."""
    palm = min(circle(u, v, -0.62, 0.05, 0.2), seg(u, v, -0.62, 0.05, -0.85, 0.45, 0.07))
    beam = poly(u, v, [(-0.45, -0.07), (0.95, -0.16), (0.95, 0.16), (-0.45, 0.07)])
    core = poly(u, v, [(-0.4, -0.02), (0.9, -0.05), (0.9, 0.05), (-0.4, 0.02)])
    sparks = min(circle(u, v, 0.2, -0.38, 0.05), circle(u, v, 0.55, 0.36, 0.05), circle(u, v, -0.05, 0.34, 0.04))
    return min(palm, max(beam, -core), sparks)

def e_avatar_orage(u, v):
    """Avatar de l'orage : silhouette dont les poings lancent deux eclairs vers le bas."""
    body = _silhouette(u, v, 0, -0.05, 0.72)
    def bolt(sx):
        return poly(u, v, [(0.32 * sx, -0.05), (0.62 * sx, 0.12), (0.5 * sx, 0.2), (0.86 * sx, 0.62), (0.4 * sx, 0.28), (0.52 * sx, 0.2), (0.24 * sx, 0.04)])
    return min(body, bolt(1), bolt(-1))

def e_eternite(u, v):
    """Eternite : symbole infini dore traverse par un sablier vertical."""
    inf = min(ring(u, v, -0.38, 0.0, 0.3, 0.05), ring(u, v, 0.38, 0.0, 0.3, 0.05))
    glass = min(poly(u, v, [(-0.16, -0.72), (0.16, -0.72), (0.02, -0.45), (-0.02, -0.45)]), poly(u, v, [(-0.02, 0.45), (0.02, 0.45), (0.16, 0.72), (-0.16, 0.72)]))
    caps = min(seg(u, v, -0.22, -0.78, 0.22, -0.78, 0.03), seg(u, v, -0.22, 0.78, 0.22, 0.78, 0.03))
    return min(inf, glass, caps, circle(u, v, 0, 0, 0.08))

def e_salve_arcanique(u, v):
    """Salve arcanique : cinq orbes en eventail qui partent d'un point en bas, chacun suivi d'un trait courbe."""
    d = circle(u, v, 0, 0.75, 0.1)
    for k in range(5):
        a = -math.pi / 2 + (k - 2) * 0.42
        ox, oy = 0.72 * math.cos(a), 0.75 + 1.25 * math.sin(a)
        d = min(d, circle(u, v, ox, oy, 0.13), seg(u, v, 0, 0.7, ox * 0.82, 0.75 + (oy - 0.75) * 0.82, 0.025))
    return d

def e_nuage_orage(u, v):
    """Nuage d'orage : nuage plein en haut, deux eclairs qui en tombent."""
    cloud = min(circle(u, v, -0.35, -0.3, 0.3), circle(u, v, 0.05, -0.45, 0.38), circle(u, v, 0.45, -0.28, 0.28), poly(u, v, [(-0.65, -0.3), (0.72, -0.3), (0.72, -0.05), (-0.65, -0.05)]))
    def bolt(x0):
        return poly(u, v, [(x0, 0.0), (x0 + 0.18, 0.0), (x0 + 0.06, 0.32), (x0 + 0.2, 0.32), (x0 - 0.08, 0.9), (x0 + 0.0, 0.42), (x0 - 0.12, 0.42)])
    return min(cloud, bolt(-0.3), bolt(0.25))

def e_reflexion(u, v):
    """Reflexion : dome miroir pose au sol ; une fleche venue d'en haut rebondit sur sa paroi et repart."""
    dome = max(abs(circle(u, v, 0, 0.55, 0.72)) - 0.05, v - 0.55)
    ground = seg(u, v, -0.9, 0.6, 0.9, 0.6, 0.04)
    px, py = 0.38, -0.06
    arrow_in = seg(u, v, -0.15, -0.9, px, py - 0.06, 0.035)
    arrow_out = min(seg(u, v, px + 0.04, py - 0.06, 0.9, -0.62, 0.035), poly(u, v, [(0.95, -0.66), (0.74, -0.68), (0.84, -0.5)]))
    spark = min(seg(u, v, px - 0.14, py - 0.08, px - 0.26, py - 0.16, 0.025), seg(u, v, px + 0.02, py - 0.2, px, py - 0.34, 0.025))
    return min(dome, ground, arrow_in, arrow_out, spark)

def e_decret(u, v):
    """Decret : sceau royal (couronne) qui ecrase vers le bas, fleches de gravite, ligne de sol."""
    crown = poly(u, v, [(-0.45, -0.25), (-0.45, -0.75), (-0.22, -0.5), (0, -0.85), (0.22, -0.5), (0.45, -0.75), (0.45, -0.25)])
    arrows = min(seg(u, v, -0.4, -0.1, -0.4, 0.45, 0.035), poly(u, v, [(-0.52, 0.4), (-0.28, 0.4), (-0.4, 0.58)]),
                 seg(u, v, 0.4, -0.1, 0.4, 0.45, 0.035), poly(u, v, [(0.28, 0.4), (0.52, 0.4), (0.4, 0.58)]),
                 seg(u, v, 0, -0.1, 0, 0.5, 0.035), poly(u, v, [(-0.12, 0.45), (0.12, 0.45), (0, 0.63)]))
    return min(crown, arrows, seg(u, v, -0.85, 0.75, 0.85, 0.75, 0.05))

def e_meditation(u, v):
    """Meditation : silhouette assise en tailleur (base large), halo au-dessus de la tete, onde au sol."""
    head = circle(u, v, 0, -0.45, 0.17)
    torso = poly(u, v, [(-0.2, -0.25), (0.2, -0.25), (0.26, 0.2), (-0.26, 0.2)])
    legs = poly(u, v, [(-0.6, 0.2), (0.6, 0.2), (0.5, 0.38), (-0.5, 0.38)])
    halo = ring(u, v, 0, -0.72, 0.2, 0.03)
    wave = arc(u, v, 0, 0.3, 0.85, 0.035, 0.3, math.pi - 0.3)
    return min(head, torso, legs, halo, wave)

def e_sanctuaire_mineur(u, v):
    """Sanctuaire mineur : dome plein clair avec une croix de lumiere au centre, pose sur le sol."""
    dome = max(abs(circle(u, v, 0, 0.55, 0.78)) - 0.05, v - 0.55)
    cross = min(poly(u, v, [(-0.06, -0.15), (0.06, -0.15), (0.06, 0.45), (-0.06, 0.45)]), poly(u, v, [(-0.24, 0.02), (0.24, 0.02), (0.24, 0.14), (-0.24, 0.14)]))
    return min(dome, cross, seg(u, v, -0.95, 0.6, 0.95, 0.6, 0.04))

def e_savoir_accelere(u, v):
    """Savoir accelere : livre ouvert dont les pages s'envolent, petite aiguille d'horloge au-dessus."""
    book = min(poly(u, v, [(-0.75, 0.15), (-0.02, 0.3), (-0.02, 0.62), (-0.75, 0.47)]), poly(u, v, [(0.02, 0.3), (0.75, 0.15), (0.75, 0.47), (0.02, 0.62)]))
    pages = min(poly(u, v, [(0.15, -0.05), (0.42, -0.2), (0.48, 0.02), (0.22, 0.14)]), poly(u, v, [(0.35, -0.45), (0.6, -0.55), (0.66, -0.35), (0.42, -0.26)]))
    clock = min(ring(u, v, -0.35, -0.42, 0.25, 0.035), seg(u, v, -0.35, -0.42, -0.35, -0.58, 0.03), seg(u, v, -0.35, -0.42, -0.24, -0.38, 0.03))
    return min(book, pages, clock)

def e_enseignement(u, v):
    """Enseignement : grande silhouette au centre, deux petites de chaque cote, reliees par des traits de lumiere."""
    d = _silhouette(u, v, 0, -0.1, 0.72)
    for sx in (-1, 1):
        d = min(d, _silhouette(u, v, 0.62 * sx, 0.28, 0.45), seg(u, v, 0.15 * sx, -0.25, 0.55 * sx, 0.02, 0.025))
    return d

def e_miracle(u, v):
    """Miracle : main ouverte vers le haut, d'ou jaillit une etoile rayonnante."""
    palm = min(poly(u, v, [(-0.45, 0.35), (0.45, 0.35), (0.3, 0.7), (-0.3, 0.7)]),
               seg(u, v, -0.42, 0.35, -0.55, 0.05, 0.06), seg(u, v, 0.42, 0.35, 0.55, 0.05, 0.06))
    star = poly(u, v, [(0, -0.75), (0.08, -0.33), (0.38, -0.25), (0.08, -0.17), (0, 0.15), (-0.08, -0.17), (-0.38, -0.25), (-0.08, -0.33)])
    rays = min(seg(u, v, 0.55 * math.cos(a), -0.25 + 0.55 * math.sin(a), 0.75 * math.cos(a), -0.25 + 0.75 * math.sin(a), 0.03)
               for a in (-math.pi / 4, -3 * math.pi / 4, -math.pi / 2 - 0.0001, 0.0, math.pi))
    return min(palm, star, rays)

def e_pilier_ascendant(u, v):
    """Pilier ascendant : colonne de blocs empiles qui monte, petite silhouette au sommet, fleche vers le haut."""
    d = _silhouette(u, v, 0, -0.68, 0.38)
    for k in range(4):
        y0 = -0.42 + k * 0.3
        d = min(d, poly(u, v, [(-0.26, y0), (0.26, y0), (0.26, y0 + 0.25), (-0.26, y0 + 0.25)]))
    arrow = min(seg(u, v, 0.62, 0.6, 0.62, -0.5, 0.035), poly(u, v, [(0.5, -0.45), (0.74, -0.45), (0.62, -0.65)]))
    return min(d, arrow, seg(u, v, -0.85, 0.85, 0.85, 0.85, 0.04))

def e_inversion(u, v):
    """Inversion : silhouette a l'envers flottant au-dessus du sol, deux fleches qui montent."""
    body = _silhouette(-u, -v, 0, 0.15, 0.62)
    arrows = min(seg(u, v, -0.55, 0.65, -0.55, -0.35, 0.035), poly(u, v, [(-0.67, -0.3), (-0.43, -0.3), (-0.55, -0.5)]),
                 seg(u, v, 0.55, 0.65, 0.55, -0.35, 0.035), poly(u, v, [(0.43, -0.3), (0.67, -0.3), (0.55, -0.5)]))
    return min(body, arrows, seg(u, v, -0.85, 0.82, 0.85, 0.82, 0.04))

def e_rempart(u, v):
    """Rempart : muraille crenelee vue de face, arche de porte au centre."""
    wall = poly(u, v, [(-0.85, -0.2), (0.85, -0.2), (0.85, 0.7), (-0.85, 0.7)])
    d = wall
    for k in range(5):
        x = -0.85 + k * 0.4
        d = min(d, poly(u, v, [(x, -0.45), (x + 0.22, -0.45), (x + 0.22, -0.2), (x, -0.2)]))
    gate = min(circle(u, v, 0, 0.35, 0.22), poly(u, v, [(-0.22, 0.35), (0.22, 0.35), (0.22, 0.7), (-0.22, 0.7)]))
    bricks = min(seg(u, v, -0.85, 0.15, -0.3, 0.15, 0.015), seg(u, v, 0.3, 0.15, 0.85, 0.15, 0.015))
    return max(d, -gate, -bricks)

def e_sables_mouvants(u, v):
    """Sables mouvants : fosse de sable pleine (ellipse) creusee de remous en spirale, une main qui s'y enfonce."""
    pit = (math.hypot(u / 0.9, (v - 0.45) / 0.4) - 1.0) * 0.4
    grooves = min(abs(math.hypot(u / 0.9, (v - 0.45) / 0.4) - r) * 0.4 - 0.018 for r in (0.35, 0.68))
    palm = poly(u, v, [(-0.17, 0.25), (0.17, 0.25), (0.15, -0.12), (-0.15, -0.12)])
    fingers = min(seg(u, v, x, -0.12, x * 1.15, -0.5 + abs(x) * 0.6, 0.045) for x in (-0.12, -0.04, 0.04, 0.12))
    thumb = seg(u, v, 0.15, 0.05, 0.32, -0.12, 0.045)
    return min(max(pit, -grooves, -(palm - 0.05)), palm, fingers, thumb)

def e_nuage_toxique(u, v):
    """Nuage toxique : nuage en contour, crane au centre, gouttes qui tombent."""
    cloud = min(circle(u, v, -0.4, 0.0, 0.3), circle(u, v, 0.05, -0.2, 0.42), circle(u, v, 0.48, 0.02, 0.28), poly(u, v, [(-0.7, 0.0), (0.76, 0.0), (0.76, 0.28), (-0.7, 0.28)]))
    drops = min(circle(u, v, -0.35, 0.6, 0.07), circle(u, v, 0.1, 0.75, 0.07), circle(u, v, 0.5, 0.58, 0.06))
    return min(max(cloud, -_skull(u, v, 0.05, 0.0, 0.42)), drops)

def e_pierre_philosophale(u, v):
    """Pierre philosophale : gemme taillee dans un cercle transmutatoire (cercle, triangle, carre)."""
    gem = poly(u, v, [(0, -0.42), (0.3, -0.12), (0, 0.42), (-0.3, -0.12)])
    facets = min(seg(u, v, -0.3, -0.12, 0.3, -0.12, 0.02), seg(u, v, 0, -0.42, 0, 0.42, 0.02))
    circ = ring(u, v, 0, 0, 0.8, 0.035)
    tri = min(seg(u, v, 0, -0.8, 0.69, 0.4, 0.025), seg(u, v, 0.69, 0.4, -0.69, 0.4, 0.025), seg(u, v, -0.69, 0.4, 0, -0.8, 0.025))
    return min(max(gem, -facets), circ, max(tri, -(gem - 0.08)))

def e_petrification(u, v):
    """Petrification : silhouette faite de blocs (fissuree), un oeil de meduse au-dessus."""
    body = _silhouette(u, v, 0, 0.2, 0.72)
    cracks = min(seg(u, v, -0.12, 0.05, 0.05, 0.3, 0.025), seg(u, v, 0.05, 0.3, -0.05, 0.55, 0.025), seg(u, v, 0.1, -0.3, 0.2, -0.05, 0.02))
    eye = max(circle(u, v, 0, -0.42, 0.55), circle(u, v, 0, -1.02, 0.55))
    eye = min(abs(eye) - 0.03, circle(u, v, 0, -0.72, 0.08))
    return min(max(body, -cracks), eye)

def _leaf(u, v, cx, cy, a, sc):
    """Feuille en amande (intersection de deux disques) centree en (cx, cy), orientee selon a, nervure creusee."""
    x, y = _rot(u - cx, v - cy, -a)
    x, y = x / sc, y / sc
    lens = max(circle(x, y, 0, 0.55, 0.85), circle(x, y, 0, -0.55, 0.85)) * sc
    return max(lens, -(seg(x, y, -0.25, 0, 0.25, 0, 0.02) * sc))

def _tendril(u, v, pts, r0, r1, n=14):
    """Racine effilee : courbe de Bezier quadratique (3 points), rayon r0 a la base, r1 a la pointe."""
    (ax, ay), (bx, by), (cx, cy) = pts
    d, prev = 9.0, (ax, ay)
    for i in range(1, n + 1):
        t = i / n
        x = (1 - t) ** 2 * ax + 2 * (1 - t) * t * bx + t * t * cx
        y = (1 - t) ** 2 * ay + 2 * (1 - t) * t * by + t * t * cy
        d = min(d, seg(u, v, prev[0], prev[1], x, y, r0 + (r1 - r0) * t))
        prev = (x, y)
    return d

def e_racines(u, v):
    """Racines : trois racines effilees jaillissent d'une motte fissuree et se recourbent vers le centre pour enserrer."""
    mound = max(circle(u, v, 0, 1.25, 0.72), v - 0.85)
    crack = min(seg(u, v, -0.2, 0.62, -0.05, 0.85, 0.025), seg(u, v, 0.25, 0.6, 0.15, 0.85, 0.025))
    d = max(mound, -crack)
    d = min(d, _tendril(u, v, [(-0.4, 0.62), (-0.85, -0.2), (-0.12, -0.55)], 0.11, 0.025))
    d = min(d, _tendril(u, v, [(0.4, 0.62), (0.85, -0.2), (0.12, -0.55)], 0.11, 0.025))
    d = min(d, _tendril(u, v, [(0.0, 0.55), (-0.15, -0.2), (0.05, -0.85)], 0.12, 0.025))
    d = min(d, _tendril(u, v, [(-0.62, 0.05), (-0.85, -0.05), (-0.88, -0.3)], 0.04, 0.015, 6))
    d = min(d, _tendril(u, v, [(0.62, 0.05), (0.85, -0.05), (0.88, -0.3)], 0.04, 0.015, 6))
    return d

def _tree(u, v, cx, base, sc):
    """Petit arbre : tronc evase et houppier rond."""
    x, y = (u - cx) / sc, (v - base) / sc
    trunk = poly(x, y, [(-0.14, 0), (0.14, 0), (0.08, -0.55), (-0.08, -0.55)])
    crown = smin(circle(x, y, 0, -0.85, 0.42), min(circle(x, y, -0.3, -0.62, 0.28), circle(x, y, 0.3, -0.62, 0.28)), 0.1)
    return min(trunk, crown) * sc

def e_bosquet(u, v):
    """Bosquet : trois arbres (le central plus haut) au-dessus d'une haie de ronces a epines."""
    trees = min(_tree(u, v, -0.5, 0.45, 0.62), _tree(u, v, 0.5, 0.45, 0.62), _tree(u, v, 0.0, 0.35, 0.82))
    vine = abs(v - 0.7 - 0.07 * math.sin(u * 9)) - 0.04
    vine = max(vine, abs(u) - 0.88)
    thorns = min(poly(u, v, [(x - 0.06, 0.66), (x + 0.06, 0.66), (x, 0.5 if k % 2 else 0.9)]) for k, x in enumerate((-0.7, -0.42, -0.14, 0.14, 0.42, 0.7)))
    return min(trees, vine, thorns)

def e_esprit_foret(u, v):
    """Esprit de la foret : gardien-arbre, tronc a visage (deux yeux luisants), bras-branches leves, feuillage en couronne."""
    trunk = poly(u, v, [(-0.3, 0.85), (0.3, 0.85), (0.24, -0.3), (-0.24, -0.3)])
    crown = smin(circle(u, v, 0, -0.55, 0.36), min(circle(u, v, -0.33, -0.38, 0.22), circle(u, v, 0.33, -0.38, 0.22)), 0.1)
    arms = min(seg(u, v, -0.24, 0.05, -0.62, -0.25, 0.07), seg(u, v, -0.62, -0.25, -0.78, -0.55, 0.05), seg(u, v, -0.62, -0.25, -0.88, -0.22, 0.045),
               seg(u, v, 0.24, 0.05, 0.62, -0.25, 0.07), seg(u, v, 0.62, -0.25, 0.78, -0.55, 0.05), seg(u, v, 0.62, -0.25, 0.88, -0.22, 0.045))
    roots = min(seg(u, v, -0.28, 0.82, -0.55, 0.9, 0.05), seg(u, v, 0.28, 0.82, 0.55, 0.9, 0.05))
    eyes = min(circle(u, v, -0.11, 0.08, 0.07), circle(u, v, 0.11, 0.08, 0.07))
    mouth = arc(u, v, 0, 0.18, 0.12, 0.025, 0.4, 2.7)
    return max(min(trunk, crown, arms, roots), -eyes, -mouth)

def e_tempete_feuilles(u, v):
    """Tempete de feuilles : entonnoir de rafales qui s'evase vers le haut, feuilles tranchantes emportees autour."""
    d = 9.0
    for i, (cy, r) in enumerate(((-0.55, 0.62), (-0.25, 0.48), (0.05, 0.34), (0.32, 0.22), (0.55, 0.12))):
        rx = abs(math.hypot(u / r, (v - cy) / (r * 0.3)) - 1) * r * 0.3 - 0.035
        gap = poly(u, v, [(0.05 - i * 0.04, cy - 0.3), (0.4, cy - 0.3), (0.4, cy + 0.3), (0.05 - i * 0.04, cy + 0.3)])
        d = min(d, max(rx, -gap))
    d = min(d, seg(u, v, 0.0, 0.55, 0.05, 0.82, 0.04))
    leaves = min(_leaf(u, v, -0.72, 0.15, 0.6, 0.16), _leaf(u, v, 0.74, -0.2, -0.5, 0.16), _leaf(u, v, 0.55, 0.55, 1.1, 0.13), _leaf(u, v, -0.5, -0.85, -0.3, 0.12))
    return min(d, leaves)

def e_lentille(u, v):
    """Lentille gravitationnelle : coeur noir dans un anneau de lumiere, etoiles etirees en arcs autour (lumiere courbee)."""
    core = circle(u, v, 0, 0, 0.16)
    halo = ring(u, v, 0, 0, 0.32, 0.05)
    arcs = min(arc(u, v, 0, 0, 0.55, 0.035, -2.6, -1.4), arc(u, v, 0, 0, 0.55, 0.035, 0.5, 1.7),
               arc(u, v, 0, 0, 0.78, 0.03, -0.9, 0.1), arc(u, v, 0, 0, 0.78, 0.03, 2.2, 3.1))
    stars = min(_flake(u, v, -0.55, -0.6, 0.09, 0.025), _flake(u, v, 0.62, 0.55, 0.09, 0.025))
    return min(core, halo, arcs, stars)

def e_couronne(u, v):
    """Couronne d'autorite : couronne a cinq pointes au centre, quatre fleches qui repoussent vers l'exterieur."""
    band = poly(u, v, [(-0.42, 0.18), (0.42, 0.18), (0.42, 0.34), (-0.42, 0.34)])
    spikes = poly(u, v, [(-0.42, 0.18), (-0.42, -0.25), (-0.24, -0.02), (0, -0.38), (0.24, -0.02), (0.42, -0.25), (0.42, 0.18)])
    gems = min(circle(u, v, 0, 0.26, 0.05), circle(u, v, -0.25, 0.26, 0.04), circle(u, v, 0.25, 0.26, 0.04))
    crown = max(min(band, spikes), -gems)
    d = crown
    for a in (math.pi / 4, 3 * math.pi / 4, 5 * math.pi / 4, 7 * math.pi / 4):
        c, s_ = math.cos(a), math.sin(a)
        d = min(d, seg(u, v, 0.6 * c, 0.6 * s_, 0.82 * c, 0.82 * s_, 0.035))
        tip = (0.92 * c, 0.92 * s_); l = (0.78 * c - 0.1 * s_, 0.78 * s_ + 0.1 * c); r = (0.78 * c + 0.1 * s_, 0.78 * s_ - 0.1 * c)
        d = min(d, poly(u, v, [tip, l, r]))
    return d

def e_loi(u, v):
    """Loi arcanique : cercle du domaine, une fleche qui en sort a droite et rentre par la gauche (pas d'issue)."""
    border = ring(u, v, 0, 0, 0.62, 0.04)
    path = min(seg(u, v, 0.25, 0, 0.82, 0, 0.04), arc(u, v, 0, 0, 0.88, 0.035, -3.1, -0.05))
    head = poly(u, v, [(-0.62, 0.0), (-0.84, -0.12), (-0.84, 0.12)])
    head = min(head, seg(u, v, -0.88, -0.02, -0.75, 0.0, 0.04))
    rune = min(seg(u, v, -0.12, -0.3, -0.12, 0.3, 0.035), seg(u, v, -0.12, -0.3, 0.12, -0.15, 0.03), seg(u, v, -0.12, 0.0, 0.12, 0.15, 0.03))
    return min(border, path, head, rune)

def e_pilier_monde(u, v):
    """Pilier du monde : colonne monumentale (chapiteau et base), ondes sismiques qui partent du pied."""
    shaft = poly(u, v, [(-0.18, -0.6), (0.18, -0.6), (0.18, 0.45), (-0.18, 0.45)])
    cap = poly(u, v, [(-0.32, -0.82), (0.32, -0.82), (0.26, -0.6), (-0.26, -0.6)])
    base = poly(u, v, [(-0.3, 0.45), (0.3, 0.45), (0.34, 0.6), (-0.34, 0.6)])
    flutes = min(seg(u, v, -0.07, -0.55, -0.07, 0.4, 0.018), seg(u, v, 0.07, -0.55, 0.07, 0.4, 0.018))
    waves = min(abs(math.hypot(u / r, (v - 0.62) / (r * 0.28)) - 1) * r * 0.28 - 0.025 for r in (0.55, 0.78))
    waves = max(waves, -(v - 0.6))
    return min(max(min(shaft, cap, base), -flutes), waves)

def e_bannissement(u, v):
    """Bannissement : faille ovale en haut qui aspire une silhouette dont le bas s'efface en tirets."""
    rift = abs(math.hypot(u / 0.62, (v + 0.55) / 0.2) - 1) * 0.2 - 0.035
    core = (math.hypot(u / 0.45, (v + 0.55) / 0.11) - 1) * 0.11
    body = max(_silhouette(u, v, 0, 0.05, 0.6), v - 0.35)
    dashes = min(poly(u, v, [(-0.14 + 0.02 * k, 0.42 + 0.14 * k), (0.14 - 0.02 * k, 0.42 + 0.14 * k), (0.14 - 0.02 * k, 0.48 + 0.14 * k), (-0.14 + 0.02 * k, 0.48 + 0.14 * k)]) for k in range(3))
    arrows = min(seg(u, v, -0.45, 0.2, -0.45, -0.25, 0.03), seg(u, v, 0.45, 0.2, 0.45, -0.25, 0.03),
                 poly(u, v, [(-0.45, -0.38), (-0.55, -0.22), (-0.35, -0.22)]), poly(u, v, [(0.45, -0.38), (0.35, -0.22), (0.55, -0.22)]))
    return min(rift, core, body, dashes, arrows)

def e_effacement(u, v):
    """Effacement : rayon qui frappe une silhouette ; sa moitie droite se desagrege en carres qui s'envolent."""
    body = max(_silhouette(u, v, 0.15, 0.08, 0.85), u - 0.15)
    beam = seg(u, v, -0.92, -0.12, -0.15, -0.12, 0.06)
    flare = circle(u, v, -0.15, -0.12, 0.11)
    bits = 9.0
    for k, (x, y, sz) in enumerate(((0.32, -0.45, 0.07), (0.3, -0.05, 0.08), (0.28, 0.35, 0.08), (0.5, -0.25, 0.06), (0.52, 0.15, 0.06), (0.7, -0.4, 0.045), (0.72, 0.0, 0.05), (0.86, -0.18, 0.035))):
        bits = min(bits, poly(u, v, [(x - sz, y - sz), (x + sz, y - sz), (x + sz, y + sz), (x - sz, y + sz)]))
    return min(max(body, -(circle(u, v, -0.15, -0.12, 0.16))), beam, flare, bits)

def e_fissure_vide(u, v):
    """Fissure du vide : dechirure verticale aux bords dentes, spirale du vide au coeur, fleche qui y entre et une qui ressort ailleurs."""
    edge = [(0, -0.85), (0.12, -0.5), (0.05, -0.35), (0.22, -0.05), (0.08, 0.15), (0.16, 0.45), (0, 0.85),
            (-0.14, 0.5), (-0.06, 0.3), (-0.22, 0.0), (-0.08, -0.2), (-0.16, -0.5)]
    tear = poly(u, v, edge)
    rim = abs(tear) - 0.035
    spiral = min(arc(u, v, 0, 0, r, 0.025, -2.5 + r * 6, 1.5 + r * 6) for r in (0.06, 0.12))
    a_in = min(seg(u, v, -0.85, 0.35, -0.38, 0.12, 0.035), poly(u, v, [(-0.3, 0.08), (-0.46, 0.04), (-0.4, 0.2)]))
    a_out = min(seg(u, v, 0.38, -0.2, 0.82, -0.55, 0.035), poly(u, v, [(0.9, -0.62), (0.74, -0.6), (0.82, -0.46)]))
    return min(rim, spiral, a_in, a_out)

def e_silence(u, v):
    """Silence du neant : etoile de sort a quatre branches dans un cercle barre (capacites interdites)."""
    star = poly(u, v, [(0, -0.42), (0.1, -0.1), (0.42, 0), (0.1, 0.1), (0, 0.42), (-0.1, 0.1), (-0.42, 0), (-0.1, -0.1)])
    circle_ = ring(u, v, 0, 0, 0.68, 0.07)
    bar = seg(u, v, -0.48, 0.48, 0.48, -0.48, 0.07)
    gap = seg(u, v, -0.48, 0.48, 0.48, -0.48, 0.15)
    return min(max(star, -gap), circle_, bar)

def e_eruption(u, v):
    """Eruption primordiale : sol fendu d'ou jaillissent trois geysers de lave (le central plus haut), gouttes retombantes."""
    ground = poly(u, v, [(-0.9, 0.55), (0.9, 0.55), (0.9, 0.75), (-0.9, 0.75)])
    cracks = min(seg(u, v, x, 0.55, x + 0.06, 0.75, 0.03) for x in (-0.45, 0.0, 0.45))
    d = max(ground, -cracks)
    for x, top, w in ((-0.45, -0.15, 0.09), (0.0, -0.7, 0.12), (0.45, -0.2, 0.09)):
        d = min(d, poly(u, v, [(x - w * 0.6, 0.55), (x + w * 0.6, 0.55), (x + w, top + 0.15), (x, top), (x - w, top + 0.15)]))
        d = min(d, circle(u, v, x, top + 0.02, w * 1.1))
    drops = min(circle(u, v, -0.25, -0.55, 0.05), circle(u, v, 0.25, -0.6, 0.05), circle(u, v, -0.7, -0.3, 0.045), circle(u, v, 0.72, -0.35, 0.045))
    return min(d, drops)

def e_sculpteur(u, v):
    """Sculpteur de glace : mur de blocs de glace a gauche, rampe en escalier a droite, ciseau de sculpteur au-dessus."""
    d = 9.0
    for row in range(3):
        for col in range(2):
            x0, y0 = -0.82 + col * 0.26, 0.7 - (row + 1) * 0.26
            d = min(d, poly(u, v, [(x0, y0), (x0 + 0.23, y0), (x0 + 0.23, y0 + 0.23), (x0, y0 + 0.23)]))
    for k in range(4):
        x0 = -0.15 + k * 0.24
        d = min(d, poly(u, v, [(x0, 0.7 - (k + 1) * 0.2), (x0 + 0.22, 0.7 - (k + 1) * 0.2), (x0 + 0.22, 0.7), (x0, 0.7)]))
    chisel = min(seg(u, v, -0.05, -0.85, 0.35, -0.45, 0.06), poly(u, v, [(0.35, -0.45), (0.5, -0.38), (0.42, -0.3), (0.3, -0.37)]))
    return min(d, chisel, _flake(u, v, -0.55, -0.55, 0.13, 0.03))

def e_onde_originelle(u, v):
    """Onde originelle : anneaux concentriques au sol (ellipses), blocs et une silhouette souleves au-dessus."""
    rings = min(abs(math.hypot(u / r, (v - 0.55) / (r * 0.3)) - 1) * r * 0.3 - 0.025 for r in (0.35, 0.62, 0.88))
    blocks = min(poly(u, v, [(x - s_, y - s_), (x + s_, y - s_), (x + s_, y + s_), (x - s_, y + s_)]) for x, y, s_ in ((-0.6, -0.15, 0.09), (0.62, -0.25, 0.1), (-0.35, -0.55, 0.07), (0.38, -0.65, 0.07), (0.0, -0.85, 0.05)))
    body = _silhouette(u, v, 0, -0.1, 0.5)
    arrows = min(seg(u, v, -0.2, 0.35, -0.2, 0.1, 0.025), seg(u, v, 0.2, 0.35, 0.2, 0.1, 0.025))
    return min(rings, blocks, body, arrows)

def e_resurrection(u, v):
    """Resurrection : silhouette pleine qui s'eleve, ailes dorees deployees, aureole, rayons de lumiere qui montent du sol."""
    body = _silhouette(u, v, 0, -0.05, 0.62)
    halo = abs((math.hypot(u / 0.2, (v + 0.6) / 0.06) - 1) * 0.06) - 0.02
    wings = min(arc(u, v, 0, -0.15, r, 0.035, a0, a1) for r in (0.38, 0.52, 0.66) for a0, a1 in ((-3.0, -2.0), (-1.15, -0.15)))
    rays = min(seg(u, v, x, 0.85, x * 0.7, 0.5, 0.025) for x in (-0.5, -0.25, 0.25, 0.5))
    ground = seg(u, v, -0.7, 0.85, 0.7, 0.85, 0.03)
    return min(body, halo, wings, rays, ground)

def _crystal(u, v, cx, cy, h, w):
    """Cristal hexagonal pointu (pointe en haut), facette centrale creusee."""
    body = poly(u, v, [(cx - w, cy), (cx - w, cy - h * 0.6), (cx, cy - h), (cx + w, cy - h * 0.6), (cx + w, cy)])
    return max(body, -seg(u, v, cx, cy - h * 0.85, cx, cy - 0.04, 0.018))

def e_golems_cristal(u, v):
    """Golems de cristal : trois golems trapus (le central devant), chacun couronne d'un cristal."""
    def golem(cx, cy, sc):
        x, y = (u - cx) / sc, (v - cy) / sc
        torso = poly(x, y, [(-0.42, -0.35), (0.42, -0.35), (0.34, 0.3), (-0.34, 0.3)])
        head = poly(x, y, [(-0.2, -0.62), (0.2, -0.62), (0.22, -0.38), (-0.22, -0.38)])
        arms = min(seg(x, y, -0.48, -0.3, -0.62, 0.3, 0.12), seg(x, y, 0.48, -0.3, 0.62, 0.3, 0.12))
        legs = min(poly(x, y, [(-0.3, 0.3), (-0.06, 0.3), (-0.08, 0.7), (-0.32, 0.7)]), poly(x, y, [(0.06, 0.3), (0.3, 0.3), (0.32, 0.7), (0.08, 0.7)]))
        eyes = min(circle(x, y, -0.09, -0.5, 0.04), circle(x, y, 0.09, -0.5, 0.04))
        return max(min(torso, head, arms, legs), -eyes) * sc
    d = min(golem(-0.55, 0.25, 0.48), golem(0.55, 0.25, 0.48))
    d = max(d, -(golem(0, 0.3, 0.62) - 0.06))
    d = min(d, golem(0, 0.3, 0.62))
    return min(d, _crystal(u, v, -0.55, -0.12, 0.3, 0.07), _crystal(u, v, 0.55, -0.12, 0.3, 0.07), _crystal(u, v, 0, -0.12, 0.42, 0.09))

def e_faconnage(u, v):
    """Faconnage : main ouverte au-dessus du sol, trois pics de roche qui en surgissent vers elle."""
    ground = poly(u, v, [(-0.9, 0.55), (0.9, 0.55), (0.9, 0.75), (-0.9, 0.75)])
    spikes = min(poly(u, v, [(x - w, 0.56), (x + w, 0.56), (x, t)]) for x, w, t in ((-0.5, 0.17, -0.05), (0.0, 0.22, -0.3), (0.5, 0.17, 0.0)))
    palm = poly(u, v, [(-0.24, -0.52), (0.24, -0.52), (0.2, -0.75), (-0.2, -0.75)])
    fingers = min(seg(u, v, x, -0.52, x * 1.2, -0.3, 0.045) for x in (-0.18, -0.06, 0.06, 0.18))
    thumb = seg(u, v, -0.24, -0.62, -0.42, -0.5, 0.045)
    return min(ground, spikes, palm, fingers, thumb)

def e_colosse(u, v):
    """Colosse de pierre : geant cuirasse de blocs (epaulieres, heaume a fente), poing leve, petit cratere a ses pieds."""
    helm = poly(u, v, [(-0.18, -0.85), (0.18, -0.85), (0.22, -0.55), (-0.22, -0.55)])
    slit = seg(u, v, -0.12, -0.7, 0.12, -0.7, 0.025)
    torso = poly(u, v, [(-0.38, -0.5), (0.38, -0.5), (0.3, 0.2), (-0.3, 0.2)])
    pauldrons = min(poly(u, v, [(-0.62, -0.55), (-0.34, -0.55), (-0.34, -0.3), (-0.62, -0.3)]), poly(u, v, [(0.34, -0.55), (0.62, -0.55), (0.62, -0.3), (0.34, -0.3)]))
    arm_l = poly(u, v, [(-0.6, -0.3), (-0.42, -0.3), (-0.45, 0.25), (-0.65, 0.25)])
    arm_r = min(seg(u, v, 0.5, -0.4, 0.72, -0.82, 0.09), poly(u, v, [(0.6, -0.98), (0.86, -0.98), (0.86, -0.76), (0.6, -0.76)]))
    legs = min(poly(u, v, [(-0.28, 0.2), (-0.04, 0.2), (-0.06, 0.62), (-0.32, 0.62)]), poly(u, v, [(0.04, 0.2), (0.28, 0.2), (0.32, 0.62), (0.06, 0.62)]))
    seams = min(seg(u, v, -0.3, -0.15, 0.3, -0.15, 0.015), seg(u, v, 0, -0.5, 0, 0.2, 0.015))
    crater = abs((math.hypot(u / 0.75, (v - 0.75) / 0.12) - 1) * 0.12) - 0.025
    return min(max(min(helm, torso, pauldrons, arm_l, arm_r, legs), -slit, -seams), crater)

def e_matiere_vive(u, v):
    """Matiere vive : trois blocs dotes d'yeux qui bondissent en arcs pointilles vers la droite."""
    d = 9.0
    for cx, cy, sz in ((-0.55, 0.45, 0.2), (0.0, -0.05, 0.24), (0.55, 0.4, 0.2)):
        cube = poly(u, v, [(cx - sz, cy - sz), (cx + sz, cy - sz), (cx + sz, cy + sz), (cx - sz, cy + sz)])
        eyes = min(circle(u, v, cx - sz * 0.4, cy - sz * 0.2, sz * 0.18), circle(u, v, cx + sz * 0.4, cy - sz * 0.2, sz * 0.18))
        d = min(d, max(cube, -eyes))
    hops = min(arc(u, v, -0.28, 0.35, 0.3, 0.025, -2.8, -0.4), arc(u, v, 0.3, 0.3, 0.3, 0.025, -2.6, -0.3))
    hops = max(hops, -(abs(math.sin(math.atan2(v, u) * 18)) - 0.4))
    ground = seg(u, v, -0.9, 0.72, 0.9, 0.72, 0.03)
    return min(d, hops, ground)

def e_reecriture(u, v):
    """Reecriture : deux silhouettes au sol, fleche a double pointe en arc au-dessus d'elles (elles echangent leur place), plume."""
    a = _silhouette(u, v, -0.55, 0.42, 0.48)
    b = _silhouette(u, v, 0.55, 0.42, 0.48)
    bridge = arc(u, v, 0, 0.25, 0.55, 0.035, -2.75, -0.39)
    heads = min(poly(u, v, [(-0.52, 0.06), (-0.66, -0.12), (-0.38, -0.1)]), poly(u, v, [(0.52, 0.06), (0.38, -0.1), (0.66, -0.12)]))
    quill = min(poly(u, v, [(0.0, -0.92), (0.16, -0.86), (0.03, -0.5), (-0.03, -0.52)]), seg(u, v, 0.0, -0.51, -0.04, -0.4, 0.02))
    return min(a, b, bridge, heads, quill)

def e_maree(u, v):
    """Maree gravitationnelle : onde sinusoidale au centre, fleches montantes au-dessus des creux, descendantes sous les crêtes."""
    wave = abs(v - 0.28 * math.sin(u * 4.2)) - 0.045
    wave = max(wave, abs(u) - 0.9)
    d = wave
    for x in (-0.75, 0.0, 0.75):
        up = math.sin(x * 4.2) < 0
        y0 = 0.28 * math.sin(x * 4.2)
        if up:
            d = min(d, seg(u, v, x, y0 - 0.15, x, y0 - 0.55, 0.03), poly(u, v, [(x, y0 - 0.7), (x - 0.1, y0 - 0.52), (x + 0.1, y0 - 0.52)]))
        else:
            d = min(d, seg(u, v, x, y0 + 0.15, x, y0 + 0.55, 0.03), poly(u, v, [(x, y0 + 0.7), (x - 0.1, y0 + 0.52), (x + 0.1, y0 + 0.52)]))
    blocks = min(poly(u, v, [(x - 0.06, y - 0.06), (x + 0.06, y - 0.06), (x + 0.06, y + 0.06), (x - 0.06, y + 0.06)]) for x, y in ((-0.4, -0.6), (0.4, 0.62), (-0.35, 0.7), (0.38, -0.7)))
    return min(d, blocks)

def _comet(u, v, hx, hy, r, ln):
    """Comete : tete ronde en bas a droite, queue effilee vers le haut a gauche."""
    head = circle(u, v, hx, hy, r)
    tail = poly(u, v, [(hx - r * 0.7, hy - r * 0.7), (hx + r * 0.75, hy - r * 0.6), (hx - ln, hy - ln * 1.05)])
    tail = min(tail, poly(u, v, [(hx - r * 0.75, hy + r * 0.6), (hx - r * 0.7, hy - r * 0.7), (hx - ln * 1.05, hy - ln)]))
    return min(head, tail)

def e_cometes(u, v):
    """Pluie de cometes : trois cometes en diagonale vers le sol, petits impacts en etoile."""
    d = min(_comet(u, v, 0.25, 0.1, 0.16, 0.65), _comet(u, v, -0.35, 0.35, 0.12, 0.45), _comet(u, v, 0.62, 0.5, 0.1, 0.38))
    ground = seg(u, v, -0.9, 0.8, 0.9, 0.8, 0.03)
    sparks = min(_flake(u, v, -0.15, 0.72, 0.1, 0.02), _flake(u, v, 0.75, 0.72, 0.08, 0.02))
    return min(d, ground, sparks)

def e_naine_blanche(u, v):
    """Naine blanche : petite etoile dense au centre, longs rayons fins, orbite inclinee et deux corps qu'elle attire."""
    core = circle(u, v, 0, 0, 0.2)
    rays = min(seg(u, v, 0, 0, 0.85 * math.cos(a), 0.85 * math.sin(a), 0.022) for a in [k * math.pi / 4 for k in range(8)])
    rays = max(rays, -circle(u, v, 0, 0, 0.3))
    orbit = abs((math.hypot(u / 0.62, v / 0.22) - 1) * 0.22) - 0.022
    bodies = min(circle(u, v, -0.6, 0.06, 0.07), circle(u, v, 0.52, -0.12, 0.06))
    return min(core, rays, orbit, bodies)

def e_supernova(u, v):
    """Supernova : etoile qui eclate (pointes irregulieres), deux ondes de choc concentriques."""
    pts = []
    for k in range(16):
        a = k * math.pi / 8 + 0.1
        r = 0.55 if k % 2 == 0 else 0.26
        if k % 4 == 0: r = 0.68
        pts.append((r * math.cos(a), r * math.sin(a)))
    burst = poly(u, v, pts)
    rings = min(ring(u, v, 0, 0, 0.82, 0.03), ring(u, v, 0, 0, 0.94, 0.018))
    return min(burst, rings)

def e_plan(u, v):
    """Plan d'architecte : equerre et compas sur une ligne de mur en perspective (briques), trace pointille."""
    wall = poly(u, v, [(-0.85, 0.75), (0.85, 0.2), (0.85, 0.45), (-0.85, 0.95)])
    joints = min(seg(u, v, x, 0.75 - (x + 0.85) * 0.32, x, 0.95 - (x + 0.85) * 0.29, 0.018) for x in (-0.45, -0.05, 0.35))
    square = min(seg(u, v, -0.7, -0.75, -0.7, 0.1, 0.045), seg(u, v, -0.7, 0.1, 0.05, 0.1, 0.045))
    compass = min(seg(u, v, 0.35, -0.85, 0.12, -0.05, 0.04), seg(u, v, 0.35, -0.85, 0.6, -0.05, 0.04), circle(u, v, 0.35, -0.85, 0.07))
    dots = min(circle(u, v, x, -0.25 - x * 0.1, 0.03) for x in (-0.45, -0.25, -0.05, 0.15, 0.35, 0.55, 0.75))
    return min(max(wall, -joints), square, compass, dots)

def e_labyrinthe(u, v):
    """Labyrinthe : trois anneaux concentriques ouverts (breches decalees), point au centre, fleche qui saute d'un anneau a l'autre."""
    d = circle(u, v, 0, 0, 0.08)
    for r, gap in ((0.3, 0.6), (0.55, 2.6), (0.8, 4.4)):
        a = math.atan2(v, u)
        da = abs((a - gap + math.pi) % (2 * math.pi) - math.pi)
        rr = ring(u, v, 0, 0, r, 0.045)
        d = min(d, max(rr, (0.45 - da) * r))
    return d

def e_porte_mondes(u, v):
    """Porte des mondes : grande arche de pierre, tourbillon dans l'ouverture, petit globe au-dessus."""
    arch = max(poly(u, v, [(-0.62, 0.85), (0.62, 0.85), (0.62, -0.25), (-0.62, -0.25)]), -poly(u, v, [(-0.42, 0.86), (0.42, 0.86), (0.42, -0.25), (-0.42, -0.25)]))
    top = max(circle(u, v, 0, -0.25, 0.62), -circle(u, v, 0, -0.25, 0.42), v + 0.25)
    swirl = min(arc(u, v, 0, 0.2, r, 0.03, -3.0 + r * 8, 0.5 + r * 8) for r in (0.12, 0.24, 0.34))
    globe = min(ring(u, v, 0, -0.98, 0.1, 0.025), seg(u, v, -0.1, -0.98, 0.1, -0.98, 0.02))
    return min(arch, top, swirl, globe)

def e_presage(u, v):
    """Presage : silhouette, sa trajectoire future en pointilles, et une cible doree a l'endroit ou elle sera."""
    body = _silhouette(u, v, -0.55, 0.25, 0.55)
    path = arc(u, v, 0.05, 0.45, 0.55, 0.03, -2.6, -0.5)
    path = max(path, -(abs(math.sin(math.atan2(v - 0.45, u - 0.05) * 14)) - 0.45))
    target = min(ring(u, v, 0.55, 0.25, 0.26, 0.035), circle(u, v, 0.55, 0.25, 0.07))
    ticks = min(seg(u, v, 0.55, -0.12, 0.55, 0.0, 0.03), seg(u, v, 0.55, 0.5, 0.55, 0.62, 0.03), seg(u, v, 0.18, 0.25, 0.3, 0.25, 0.03), seg(u, v, 0.8, 0.25, 0.92, 0.25, 0.03))
    return min(body, path, target, ticks)

def e_revelation(u, v):
    """Revelation : rideau entrouvert sur une lumiere rayonnante, un oeil au coeur."""
    rays = min(seg(u, v, 0, -0.05, 0.75 * math.cos(a), -0.05 + 0.75 * math.sin(a), 0.025) for a in [k * math.pi / 6 for k in range(12)])
    rays = max(rays, -circle(u, v, 0, -0.05, 0.3))
    eye = max(circle(u, v, 0, -0.27, 0.32), circle(u, v, 0, 0.17, 0.32))
    eye = max(eye, -circle(u, v, 0, -0.05, 0.09))
    curtain_l = poly(u, v, [(-0.9, -0.9), (-0.45, -0.9), (-0.62, -0.1), (-0.5, 0.9), (-0.9, 0.9)])
    curtain_r = poly(u, v, [(0.9, -0.9), (0.45, -0.9), (0.62, -0.1), (0.5, 0.9), (0.9, 0.9)])
    folds = min(seg(u, v, -0.75, -0.85, -0.75, 0.85, 0.02), seg(u, v, 0.75, -0.85, 0.75, 0.85, 0.02))
    return min(max(min(curtain_l, curtain_r), -folds), max(rays, -min(curtain_l, curtain_r)), eye)

def e_prophetie(u, v):
    """Prophetie : sablier dont le sable forme un oeil, deux silhouettes fantomes (l'avenir) de part et d'autre."""
    glass = min(poly(u, v, [(-0.3, -0.75), (0.3, -0.75), (0.04, 0), (-0.04, 0)]), poly(u, v, [(-0.04, 0), (0.04, 0), (0.3, 0.75), (-0.3, 0.75)]))
    glass = max(abs(glass) - 0.035, -1)
    caps = min(seg(u, v, -0.38, -0.8, 0.38, -0.8, 0.045), seg(u, v, -0.38, 0.8, 0.38, 0.8, 0.045))
    eye = max(circle(u, v, 0, 0.35, 0.2), circle(u, v, 0, 0.63, 0.2))
    eye = max(eye, -circle(u, v, 0, 0.49, 0.05))
    ghosts = min(abs(_silhouette(u, v, -0.65, 0.2, 0.42)) - 0.025, abs(_silhouette(u, v, 0.65, 0.2, 0.42)) - 0.025)
    return min(glass, caps, eye, ghosts)


# ---------------- Voie martiale du Clan Luo (2026-11-09) ----------------
def _katana(u, v, ax, ay, bx, by, w=0.055):
    """Katana droit de (ax, ay) (poignee) a (bx, by) (pointe) : lame, tsuba, poignee."""
    L = math.hypot(bx - ax, by - ay); dx, dy = (bx - ax) / L, (by - ay) / L
    gx, gy = ax + dx * 0.28 * L, ay + dy * 0.28 * L
    blade = seg(u, v, gx, gy, bx - dx * 0.04, by - dy * 0.04, w)
    tip = circle(u, v, bx - dx * 0.05, by - dy * 0.05, w * 0.6)
    tsuba = seg(u, v, gx - dy * 0.14, gy + dx * 0.14, gx + dy * 0.14, gy - dx * 0.14, 0.045)
    hilt = seg(u, v, ax, ay, gx, gy, w * 0.85)
    return min(blade, tip, tsuba, hilt)

def e_ki_epee(u, v):
    """Ki de l'Epee : katana dresse, flammes de Ki qui montent de chaque cote de la lame."""
    sword = _katana(u, v, 0, 0.85, 0, -0.85)
    flames = min(arc(u, v, -0.55, -0.1, 0.42, 0.035, -0.9, 0.9), arc(u, v, 0.55, -0.1, 0.42, 0.035, math.pi - 0.9, math.pi + 0.9),
                 arc(u, v, -0.7, -0.35, 0.3, 0.03, -0.7, 0.7), arc(u, v, 0.7, -0.35, 0.3, 0.03, math.pi - 0.7, math.pi + 0.7))
    return min(sword, flames)

def e_meditation_martiale(u, v):
    """Meditation martiale : silhouette en tailleur, katana pose a l'horizontale devant, trois volutes de Ki qui montent."""
    head = circle(u, v, 0, -0.42, 0.16)
    torso = poly(u, v, [(-0.19, -0.22), (0.19, -0.22), (0.25, 0.18), (-0.25, 0.18)])
    legs = poly(u, v, [(-0.58, 0.18), (0.58, 0.18), (0.48, 0.34), (-0.48, 0.34)])
    sword = _katana(u, v, -0.78, 0.5, 0.82, 0.5, 0.04)
    wisps = min(arc(u, v, -0.5, -0.55, 0.14, 0.028, -1.6, 1.0), arc(u, v, 0.5, -0.55, 0.14, 0.028, 2.1, 4.7),
                circle(u, v, 0, -0.82, 0.06))
    return min(head, torso, legs, sword, wisps)

def e_paume_ki(u, v):
    """Paume de Ki : main ouverte, doigts serres vers le haut, onde de choc devant la paume."""
    palm = poly(u, v, [(-0.3, -0.05), (0.26, -0.05), (0.22, 0.45), (-0.24, 0.45)])
    fingers = min(seg(u, v, x, -0.08, x, -0.55 + abs(x) * 0.5, 0.065) for x in (-0.21, -0.07, 0.07, 0.2))
    thumb = seg(u, v, -0.3, 0.2, -0.55, -0.05, 0.065)
    waves = min(ring(u, v, 0, 0.1, 0.72, 0.03), ring(u, v, 0, 0.1, 0.9, 0.025))
    return min(palm, fingers, thumb, max(waves, -(v - 0.75)))

def e_frappe_meridiens(u, v):
    """Frappe des Meridiens : ligne de meridien et ses trois points vitaux, deux doigts qui frappent."""
    line = seg(u, v, -0.15, -0.8, -0.15, 0.8, 0.03)
    points = min(ring(u, v, -0.15, y, 0.12, 0.035) for y in (-0.5, 0.0, 0.5))
    cores = min(circle(u, v, -0.15, y, 0.05) for y in (-0.5, 0.0, 0.5))
    strikes = min(seg(u, v, 0.75, -0.75, 0.08, -0.52, 0.045), seg(u, v, 0.78, -0.2, 0.08, -0.02, 0.045),
                  seg(u, v, 0.75, 0.3, 0.08, 0.48, 0.045))
    return min(line, points, cores, strikes)

def e_garde_fleuve(u, v):
    """Garde du Fleuve : katana a l'horizontale en garde, vague qui revient sur elle-meme dessous."""
    sword = _katana(u, v, -0.8, -0.35, 0.82, -0.35)
    pts = [(-0.75 + i * 0.075, 0.3 + 0.16 * math.sin(i * 0.075 * 2 * math.pi / 0.75)) for i in range(21)]
    wave = min(seg(u, v, *pts[i], *pts[i + 1], 0.045) for i in range(20))
    curl = arc(u, v, 0.58, 0.18, 0.2, 0.045, -0.4, 2.6)                  # la vague revient sur elle-meme
    return min(sword, wave, curl)

def e_croissant_ki(u, v):
    """Croissant de Ki : grand croissant tranchant lance vers la droite, lignes de vitesse."""
    cres = max(circle(u, v, 0.1, 0, 0.7), -circle(u, v, -0.18, 0, 0.62))
    speed = min(seg(u, v, -0.95, -0.3, -0.55, -0.3, 0.035), seg(u, v, -0.95, 0.3, -0.55, 0.3, 0.035),
                seg(u, v, -0.98, 0.0, -0.65, 0.0, 0.03))
    return min(cres, speed)

def e_domaine_epee(u, v):
    """Domaine de l'Epee : cercle du domaine, six lames pointees vers le centre."""
    d = ring(u, v, 0, 0, 0.8, 0.035)
    for i in range(6):
        a = i * math.pi / 3 + math.pi / 6
        ca, sa = math.cos(a), math.sin(a)
        d = min(d, poly(u, v, [(ca * 0.72 - sa * 0.07, sa * 0.72 + ca * 0.07), (ca * 0.28, sa * 0.28),
                               (ca * 0.72 + sa * 0.07, sa * 0.72 - ca * 0.07)]))
    return min(d, circle(u, v, 0, 0, 0.08))

def e_fleur_prunier(u, v):
    """Fleur de Prunier : fleur a cinq petales, coeur cercle, petales detaches qui s'envolent."""
    d = 9.0
    for i in range(5):
        a = -math.pi / 2 + i * 2 * math.pi / 5
        d = min(d, circle(u, v, math.cos(a) * 0.33, math.sin(a) * 0.33 - 0.05, 0.22))
    d = max(d, -circle(u, v, 0, -0.05, 0.12))
    d = min(d, circle(u, v, 0, -0.05, 0.06))
    loose = min(circle(u, v, 0.68, 0.55, 0.07), circle(u, v, 0.45, 0.78, 0.055), circle(u, v, -0.7, 0.62, 0.06))
    return min(d, loose)

def e_hwakyung(u, v):
    """Transformation Hwakyung : silhouette debout, rayons de Ki qui jaillissent tout autour."""
    body = min(circle(u, v, 0, -0.42, 0.15), poly(u, v, [(-0.2, -0.24), (0.2, -0.24), (0.15, 0.3), (-0.15, 0.3)]),
               seg(u, v, -0.08, 0.3, -0.14, 0.72, 0.06), seg(u, v, 0.08, 0.3, 0.14, 0.72, 0.06),
               seg(u, v, -0.2, -0.2, -0.42, -0.5, 0.05), seg(u, v, 0.2, -0.2, 0.42, -0.5, 0.05))
    rays = 9.0
    for i in range(10):
        a = i * math.pi / 5
        rays = min(rays, seg(u, v, math.cos(a) * 0.62, math.sin(a) * 0.62, math.cos(a) * 0.9, math.sin(a) * 0.9, 0.035))
    return min(body, rays)

def e_unite_ciel_terre(u, v):
    """Unite du Ciel et de la Terre : soleil au-dessus de la montagne, coupe horizontale qui traverse le monde."""
    sun = ring(u, v, 0, -0.42, 0.24, 0.04)
    mountain = poly(u, v, [(-0.85, 0.7), (-0.25, 0.05), (0.05, 0.35), (0.35, 0.0), (0.85, 0.7)])
    cut = seg(u, v, -0.95, 0.12, 0.95, 0.12, 0.04)
    return min(sun, max(mountain, -(abs(v - 0.12) - 0.09)), cut)

def _figure(u, v, x, y, k=1.0):
    """Silhouette debout (tete, buste, jambes), centree en (x, y), echelle k."""
    return min(circle(u, v, x, y - 0.42 * k, 0.13 * k), poly(u, v, [(x - 0.17 * k, y - 0.25 * k), (x + 0.17 * k, y - 0.25 * k),
                                                                      (x + 0.12 * k, y + 0.22 * k), (x - 0.12 * k, y + 0.22 * k)]),
               seg(u, v, x - 0.07 * k, y + 0.22 * k, x - 0.12 * k, y + 0.6 * k, 0.05 * k),
               seg(u, v, x + 0.07 * k, y + 0.22 * k, x + 0.12 * k, y + 0.6 * k, 0.05 * k))

def _blade(u, v, x0, y0, x1, y1, w):
    """Lame effilee de (x0, y0) (garde) a (x1, y1) (pointe), garde perpendiculaire."""
    dx, dy = x1 - x0, y1 - y0
    n = math.hypot(dx, dy) or 1
    px, py = -dy / n, dx / n
    body = poly(u, v, [(x0 + px * w, y0 + py * w), (x1, y1), (x0 - px * w, y0 - py * w)])
    guard = seg(u, v, x0 + px * w * 2.4, y0 + py * w * 2.4, x0 - px * w * 2.4, y0 - py * w * 2.4, w * 0.45)
    grip = seg(u, v, x0, y0, x0 - dx / n * 0.14, y0 - dy / n * 0.14, w * 0.5)
    return min(body, guard, grip)

def e_renforcement_corps(u, v):
    """Renforcement corporel : silhouette dans une carapace hexagonale de Ki."""
    hexa = [(math.cos(i * math.pi / 3 + math.pi / 6) * 0.8, math.sin(i * math.pi / 3 + math.pi / 6) * 0.8) for i in range(6)]
    shell = max(poly(u, v, hexa), -poly(u, v, [(x * 0.86, y * 0.86) for x, y in hexa]))
    return min(shell, _figure(u, v, 0, 0.0, 0.95))

def e_pas_leger(u, v):
    """Pas leger : empreinte de pied penchee et trois traits de vitesse."""
    sole = min(circle(u, v, 0.18, -0.2, 0.2), circle(u, v, 0.08, 0.3, 0.15), poly(u, v, [(0.0, -0.18), (0.36, -0.15), (0.2, 0.3), (-0.05, 0.3)]))
    toes = min(circle(u, v, 0.05 + i * 0.1, -0.52 + abs(i - 1.5) * 0.04, 0.055 - i * 0.004) for i in range(4))
    lines = min(seg(u, v, -0.85, -0.3 + i * 0.3, -0.25, -0.3 + i * 0.3, 0.04) for i in range(3))
    return min(sole, toes, lines)

def e_sens_martial(u, v):
    """Sens martial : oeil ouvert au centre, trois presences marquees autour."""
    eye = max(circle(u, v, 0, 0.5, 0.82), circle(u, v, 0, -0.5, 0.82))
    eye = max(eye, -max(circle(u, v, 0, 0.5, 0.73), circle(u, v, 0, -0.5, 0.73)))
    pupil = min(ring(u, v, 0, 0, 0.17, 0.03), circle(u, v, 0, 0, 0.08))
    marks = min(ring(u, v, math.cos(a) * 0.74, math.sin(a) * 0.74, 0.08, 0.03) for a in (-math.pi / 2, math.pi / 6, 5 * math.pi / 6))
    return min(eye, pupil, marks)

def e_arkies_domini(u, v):
    """Arkies Domini (rune de l'Observateur) : cercle du domaine, grand oeil ouvert au centre, ondes d'ecoute de part et d'autre."""
    dom = ring(u, v, 0, 0, 0.88, 0.035)
    eye = max(circle(u, v, 0, 0.36, 0.62), circle(u, v, 0, -0.36, 0.62))
    eye = max(eye, -max(circle(u, v, 0, 0.36, 0.54), circle(u, v, 0, -0.36, 0.54)))
    pupil = min(circle(u, v, 0, 0, 0.11), ring(u, v, 0, 0, 0.2, 0.03))
    waves = min(arc(sx * u, v, 0.3, 0, r, 0.03, -0.55, 0.55) for sx in (-1, 1) for r in (0.36, 0.5))   # gauche = miroir
    return min(dom, eye, pupil, waves)

def e_projection_epee_aura(u, v):
    """Projection d'epee d'Aura : trois epees en orbite autour d'un coeur de Ki."""
    d = min(circle(u, v, 0, 0, 0.12), ring(u, v, 0, 0, 0.42, 0.02))
    for i in range(3):
        a = i * 2 * math.pi / 3 - math.pi / 2
        cx, cy = math.cos(a) * 0.62, math.sin(a) * 0.62
        tx, ty = -math.sin(a), math.cos(a)
        d = min(d, _blade(u, v, cx - tx * 0.26, cy - ty * 0.26, cx + tx * 0.3, cy + ty * 0.3, 0.07))
    return d

def e_vol_epee_ki(u, v):
    """Vol sur epee de Ki : silhouette debout sur une grande epee horizontale, trainee d'air."""
    sword = _blade(u, v, -0.55, 0.45, 0.85, 0.45, 0.08)
    trail = min(seg(u, v, -0.95, 0.72, -0.5, 0.72, 0.03), seg(u, v, -0.85, 0.2, -0.45, 0.2, 0.03))
    return min(sword, trail, _figure(u, v, 0.1, -0.12, 0.85))

def e_incarnation_ame(u, v):
    """Incarnation de l'Ame : silhouette pleine et son double spirituel en contour, decale."""
    body = _figure(u, v, -0.22, 0.05, 1.0)
    ghost = abs(_figure(u, v, 0.28, -0.05, 1.05)) - 0.03
    return min(body, max(ghost, -(_figure(u, v, -0.22, 0.05, 1.0) - 0.06)))

def e_domaine_divin(u, v):
    """Domaine Divin : grand cercle du domaine, couronne de rayons tournes vers l'interieur, figure au centre."""
    d = ring(u, v, 0, 0, 0.84, 0.035)
    for i in range(12):
        a = i * math.pi / 6
        d = min(d, seg(u, v, math.cos(a) * 0.76, math.sin(a) * 0.76, math.cos(a) * 0.58, math.sin(a) * 0.58, 0.03))
    return min(d, _figure(u, v, 0, 0.02, 0.75))

def e_execution_celeste(u, v):
    """Execution Celeste (rune de famille du Clan Luo) : la cible au centre, tranchee par deux lames en croix ; une ligne de
    ruee la traverse, deux images remanentes en contour a gauche, le lanceur a droite, dos a elle."""
    target = _figure(u, v, -0.02, 0.08, 0.72)
    cross = min(_blade(u, v, -0.38, -0.36, 0.34, 0.44, 0.055), _blade(u, v, 0.34, -0.36, -0.38, 0.44, 0.055))
    dash = min(seg(u, v, -0.95, 0.62, 0.95, 0.62, 0.025), seg(u, v, -0.95, 0.74, -0.45, 0.74, 0.02))
    ghosts = min(abs(_figure(u, v, -0.72, 0.18, 0.62)) - 0.025, abs(_figure(u, v, -0.5, 0.16, 0.66)) - 0.025)
    me = _figure(u, v, 0.66, 0.14, 0.74)
    return min(max(target, -(cross - 0.06)), cross, dash, ghosts, me)

def e_transfo_arme(u, v):
    """Transformation d'arme : mousquet en diagonale (crosse large en bas a gauche, bouche en haut a droite), platine,
    pontet et baguette, entre deux fleches circulaires opposees (l'arme change de forme puis revient)."""
    barrel = seg(u, v, -0.2, 0.14, 0.56, -0.42, 0.065)
    rod = seg(u, v, -0.12, 0.26, 0.5, -0.22, 0.022)
    muzzle = circle(u, v, 0.58, -0.44, 0.085)
    stock = poly(u, v, [(-0.26, 0.04), (-0.08, 0.24), (-0.4, 0.62), (-0.68, 0.6), (-0.66, 0.4)])
    lock = circle(u, v, -0.12, 0.14, 0.08)
    guard = max(ring(u, v, -0.22, 0.34, 0.09, 0.025), -(v - 0.34))
    d = min(barrel, rod, muzzle, stock, lock, guard)
    r = 0.86
    for a0, a1 in ((-2.95, -1.45), (0.2, 1.7)):
        d = min(d, arc(u, v, 0, 0, r, 0.04, a0, a1))
        px, py = r * math.cos(a1), r * math.sin(a1)
        tx, ty = -math.sin(a1), math.cos(a1)
        nx, ny = math.cos(a1), math.sin(a1)
        d = min(d, poly(u, v, [(px + tx * 0.24, py + ty * 0.24), (px + nx * 0.14, py + ny * 0.14), (px - nx * 0.14, py - ny * 0.14)]))
    return d

def _oval(u, v, cx, cy, rx, ry):
    """Ellipse pleine (distance approchee)."""
    return (math.hypot((u - cx) / rx, (v - cy) / ry) - 1) * min(rx, ry)

def e_sans_visage(u, v):
    """Sans Visage (rune de l'Observateur) : masque ovale lisse, deux fentes pour les yeux, sans bouche ; deux visages en
    contour derriere lui, decales a gauche et a droite (les visages qu'il peut prendre)."""
    mask = _oval(u, v, 0, 0.02, 0.42, 0.6)
    eyes = min(seg(u, v, -0.24, -0.1, -0.09, -0.06, 0.035), seg(u, v, 0.09, -0.06, 0.24, -0.1, 0.035))
    mask = max(mask, -eyes)
    ghosts = min(abs(_oval(u, v, sx * 0.42, -0.04, 0.36, 0.52)) - 0.028 for sx in (-1, 1))
    ghosts = max(ghosts, -(_oval(u, v, 0, 0.02, 0.42, 0.6) - 0.07))      # coupes par le masque
    return min(mask, ghosts)

def e_sentence_silencieuse(u, v):
    """Sentence Silencieuse (technique ultime de l'Observateur) : petite couronne du souverain en haut, dague verticale
    pointe en bas, croissant de lune qui l'entoure a gauche, goutte au bout de la pointe."""
    crown = poly(u, v, [(-0.3, -0.56), (-0.3, -0.86), (-0.15, -0.7), (0, -0.92), (0.15, -0.7), (0.3, -0.86), (0.3, -0.56)])
    dagger = _blade(u, v, 0, -0.32, 0, 0.66, 0.11)
    moon = max(circle(u, v, -0.05, 0.12, 0.66), -circle(u, v, 0.2, 0.04, 0.6))
    moon = max(moon, -(_blade(u, v, 0, -0.32, 0, 0.66, 0.11) - 0.05))
    drop = min(circle(u, v, 0, 0.86, 0.06), poly(u, v, [(-0.05, 0.84), (0.05, 0.84), (0, 0.74)]))
    return min(crown, dagger, moon, drop)

EMBLEMS = {"Feu": e_feu, "Glace": e_glace, "Terre": e_terre, "Air": e_air, "Tenebres": e_tenebres, "Lumiere": e_lumiere,
           "Temps": e_temps, "Espace": e_espace, "Combat": e_combat, "Spatial": e_spatial, "Dragonic": e_dragonic, "Foudre": e_foudre,
           "Puits": e_puits, "Autorite": e_autorite, "Cyclone": e_cyclone, "Telekinesie": e_telekinesie, "Clonage": e_clonage, "Miroir": e_miroir,
           "Onde": e_onde, "Flammeche": e_flammeche, "BouleFeu": e_boule_feu, "MurFlammes": e_mur_flammes, "Marque": e_marque, "AuraBraise": e_aura_braise,
           "EclatGivre": e_eclat_givre, "LanceCristal": e_lance_cristal, "PrisonGlace": e_prison_glace, "SentierHiver": e_sentier_hiver,
           "Decharge": e_decharge, "Chaine": e_chaine, "Orbe": e_orbe,
           "Blizzard": e_blizzard, "DomaineElem": e_domaine_elem, "Tectonique": e_tectonique, "DanseVents": e_danse_vents,
           "Halo": e_halo, "ArmeBenie": e_arme_benie, "Glyphes": e_glyphes, "Egide": e_egide,
           "Soin": e_soin, "Fontaine": e_fontaine, "LanceLumiere": e_lance_lumiere, "LienAme": e_lien_ame,
           "Follet": e_follet, "Golem": e_golem, "Sentinelle": e_sentinelle, "Meute": e_meute,
           "MarqueFuneste": e_marque_funeste, "PuitsAmes": e_puits_ames, "Releve": e_releve, "AmesErrantes": e_ames_errantes,
           "Phylactere": e_phylactere, "DomaineMort": e_domaine_mort, "FormeSpectrale": e_forme_spectrale, "ArmeeTombes": e_armee_tombes,
           "MiroirTrompeur": e_miroir_trompeur, "Galerie": e_galerie, "Hallucination": e_hallucination,
           "ChainesNoires": e_chaines_noires, "PacteSang": e_pacte_sang, "Marionnette": e_marionnette,
           "Ruee": e_ruee, "VoileFumee": e_voile_fumee, "PluieFleches": e_pluie_fleches, "LancerPieces": e_lancer_pieces,
           "Projection": e_projection, "Ceinture": e_ceinture, "Prescience": e_prescience, "RayonCeleste": e_rayon_celeste,
           "Distorsion": e_distorsion, "RetourArriere": e_retour_arriere, "OeilOmniscient": e_oeil_omniscient, "Sentence": e_sentence,
           "BulleTemporelle": e_bulle_temporelle, "ArretTemps": e_arret_temps, "Surregime": e_surregime, "EchosPasse": e_echos_passe,
           "Meteore": e_meteore, "RayonArcanique": e_rayon_arcanique, "AvatarOrage": e_avatar_orage, "Eternite": e_eternite,
           "SalveArcanique": e_salve_arcanique, "NuageOrage": e_nuage_orage, "Reflexion": e_reflexion, "Decret": e_decret,
           "Meditation": e_meditation, "SanctuaireMineur": e_sanctuaire_mineur, "SavoirAccelere": e_savoir_accelere, "Enseignement": e_enseignement,
           "Miracle": e_miracle, "PilierAscendant": e_pilier_ascendant, "Inversion": e_inversion, "Rempart": e_rempart,
           "SablesMouvants": e_sables_mouvants, "NuageToxique": e_nuage_toxique, "PierrePhilosophale": e_pierre_philosophale, "Petrification": e_petrification,
           "Racines": e_racines, "Bosquet": e_bosquet, "EspritForet": e_esprit_foret, "TempeteFeuilles": e_tempete_feuilles,
           "Lentille": e_lentille, "Couronne": e_couronne, "Loi": e_loi, "PilierMonde": e_pilier_monde,
           "Bannissement": e_bannissement, "Effacement": e_effacement, "FissureVide": e_fissure_vide, "Silence": e_silence,
           "Eruption": e_eruption, "Sculpteur": e_sculpteur, "OndeOriginelle": e_onde_originelle, "Resurrection": e_resurrection,
           "GolemsCristal": e_golems_cristal, "Faconnage": e_faconnage, "Colosse": e_colosse,
           "MatiereVive": e_matiere_vive, "Reecriture": e_reecriture, "Maree": e_maree,
           "Cometes": e_cometes, "NaineBlanche": e_naine_blanche, "Supernova": e_supernova,
           "Plan": e_plan, "TransfoArme": e_transfo_arme, "Labyrinthe": e_labyrinthe, "PorteMondes": e_porte_mondes,
           "Presage": e_presage, "Revelation": e_revelation, "Prophetie": e_prophetie,
           "KiEpee": e_ki_epee, "MeditationMartiale": e_meditation_martiale, "PaumeKi": e_paume_ki,
           "FrappeMeridiens": e_frappe_meridiens, "GardeFleuve": e_garde_fleuve, "CroissantKi": e_croissant_ki,
           "DomaineEpee": e_domaine_epee, "FleurPrunier": e_fleur_prunier, "Hwakyung": e_hwakyung,
           "UniteCielTerre": e_unite_ciel_terre, "Luo": e_ki_epee,
           "RenforcementCorps": e_renforcement_corps, "PasLeger": e_pas_leger, "SensMartial": e_sens_martial, "ArkiesDomini": e_arkies_domini,
           "ProjectionEpeeAura": e_projection_epee_aura, "VolEpeeKi": e_vol_epee_ki, "IncarnationAme": e_incarnation_ame,
           "DomaineDivin": e_domaine_divin, "ExecutionCeleste": e_execution_celeste,
           "SansVisage": e_sans_visage, "SentenceSilencieuse": e_sentence_silencieuse}

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
