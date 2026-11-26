#!/usr/bin/env python3
"""Dessine le continent d'Eldanior (aperçus à valider, puis masque pour le générateur Hytale).

Usage :
    python3 -I scripts/carte/continent.py <dossier de sortie> [--variantes A,B,C,D] [--px 800]

Chaque variante produit <dossier>/continent_<X>.png : relief ombré, territoires, frontières, climats, légende.
Le continent fait environ 10 000 blocs de diamètre, l'image couvre 12 000 blocs (anneau d'océan compris).
Le Nord est en haut. Le script n'utilise que la bibliothèque standard (pas de numpy ni de PIL).
"""
import argparse, math, os, random, struct, unicodedata, zlib

SPAN = 12000          # blocs couverts par l'image
RADIUS = 4700         # rayon moyen du continent (blocs)
MOUNTAIN_LIFT = 105   # soulèvement maximal des chaînes (blocs au-dessus des plaines, ~Y 225)
PLATEAU_LIFT = 24     # hauteur du plateau de la capitale au-dessus des plaines

# ------------------------------------------------------------------ bruit (interpolation séparable, rapide)
def _smooth(t):
    return t * t * (3 - 2 * t)

def fbm(W, H, seed, cells, octaves, persistence=0.5):
    rnd = random.Random(seed)
    total = [[0.0] * W for _ in range(H)]
    amp, norm = 1.0, 0.0
    for o in range(octaves):
        c = cells * (2 ** o)
        grid = [[rnd.random() for _ in range(c + 2)] for _ in range(c + 2)]
        xs = [i * c / W for i in range(W)]
        x0 = [int(v) for v in xs]
        tx = [_smooth(v - int(v)) for v in xs]
        rows = [[g[a] + (g[a + 1] - g[a]) * t for a, t in zip(x0, tx)] for g in grid]
        for j in range(H):
            v = j * c / H
            y0 = int(v)
            t = _smooth(v - y0)
            r0, r1 = rows[y0], rows[y0 + 1]
            total[j] = [s + amp * (a + (b - a) * t) for s, a, b in zip(total[j], r0, r1)]
        norm += amp
        amp *= persistence
    lo = min(min(r) for r in total); hi = max(max(r) for r in total)
    k = 2.0 / ((hi - lo) or 1)
    return [[(v - lo) * k - 1 for v in row] for row in total]      # étiré sur -1..1

# ------------------------------------------------------------------ PNG et texte
def save_png(path, img, W, H):
    raw = b''.join(b'\x00' + bytes(img[j * W * 3:(j + 1) * W * 3]) for j in range(H))
    def ch(t, d):
        return struct.pack('>I', len(d)) + t + d + struct.pack('>I', zlib.crc32(t + d) & 0xffffffff)
    with open(path, 'wb') as f:
        f.write(b'\x89PNG\r\n\x1a\n' + ch(b'IHDR', struct.pack('>IIBBBBB', W, H, 8, 2, 0, 0, 0))
                + ch(b'IDAT', zlib.compress(raw, 9)) + ch(b'IEND', b''))

FONT = {  # 5x7, une chaîne de 7 lignes de 5 caractères par glyphe
    'A': '01110100011000111111100011000110001', 'B': '11110100011000111110100011000111110',
    'C': '01110100011000010000100001000101110', 'D': '11110100011000110001100011000111110',
    'E': '11111100001000011110100001000011111', 'F': '11111100001000011110100001000010000',
    'G': '01110100011000010111100011000101111', 'H': '10001100011000111111100011000110001',
    'I': '01110001000010000100001000010001110', 'J': '00111000100001000010000101001001100',
    'K': '10001100101010011000101001001010001', 'L': '10000100001000010000100001000011111',
    'M': '10001110111010110101100011000110001', 'N': '10001100011100110101100111000110001',
    'O': '01110100011000110001100011000101110', 'P': '11110100011000111110100001000010000',
    'Q': '01110100011000110001101011001001101', 'R': '11110100011000111110101001001010001',
    'S': '01111100001000001110000010000111110', 'T': '11111001000010000100001000010000100',
    'U': '10001100011000110001100011000101110', 'V': '10001100011000110001100010101000100',
    'W': '10001100011000110101101011010101010', 'X': '10001100010101000100010101000110001',
    'Y': '10001100010101000100001000010000100', 'Z': '11111000010001000100010001000011111',
    '0': '01110100011001110101110011000101110', '1': '00100011000010000100001000010001110',
    '2': '01110100010000100010001000100011111', '3': '11111000100010000010000011000101110',
    '4': '00010001100101010010111110001000010', '5': '11111100001111000001000011000101110',
    '6': '00110010001000011110100011000101110', '7': '11111000010001000100010000100001000',
    '8': '01110100011000101110100011000101110', '9': '01110100011000101111000010001001100',
    ' ': '0' * 35, "'": '00100001000100000000000000000000000', '-': '00000000000000011111000000000000000',
    '.': '00000000000000000000000000110001100', ':': '00000011000110000000011000110000000',
    '(': '00010001000100001000010000010000010', ')': '01000001000001000010000100010001000',
    '/': '00001000010001000100010001000010000', '+': '00000001000010011111001000010000000',
}

def text(img, W, H, x, y, s, color, scale=2, shadow=True):
    s = unicodedata.normalize('NFD', s.upper())
    s = ''.join(c for c in s if c in FONT)
    if shadow:
        text(img, W, H, x + 1, y + 1, s, (0, 0, 0), scale, False)
    for k, c in enumerate(s):
        g = FONT[c]
        for gy in range(7):
            for gx in range(5):
                if g[gy * 5 + gx] == '1':
                    for dy in range(scale):
                        for dx in range(scale):
                            px, py = x + (k * 6 + gx) * scale + dx, y + gy * scale + dy
                            if 0 <= px < W and 0 <= py < H:
                                o = (py * W + px) * 3
                                img[o:o + 3] = bytes(color)

def text_width(s, scale=2):
    return len(s) * 6 * scale

def rect(img, W, H, x0, y0, x1, y1, color):
    for y in range(max(0, y0), min(H, y1)):
        o = (y * W + max(0, x0)) * 3
        n = min(W, x1) - max(0, x0)
        if n > 0:
            img[o:o + 3 * n] = bytes(color) * n

# ------------------------------------------------------------------ couleurs des biomes (aperçu)
C = {
    'ocean': (38, 78, 140), 'ocean_bas': (58, 112, 168), 'plage': (226, 211, 160),
    'lac': (64, 120, 190), 'fleuve': (70, 132, 200),
    'plaine': (138, 186, 92), 'foret': (66, 128, 62), 'colline': (150, 160, 100), 'plateau': (196, 196, 140),
    'taiga': (52, 102, 84), 'toundra': (200, 212, 214), 'neige': (244, 246, 250),
    'foret_dense': (34, 92, 46), 'marais': (96, 114, 66), 'automne': (196, 120, 50),
    'savane': (190, 182, 96), 'desert': (226, 196, 128), 'canyon': (192, 104, 64),
    'bouleau': (164, 196, 104), 'cotes_rocheuses': (140, 140, 128),
    'bambou': (110, 190, 80), 'rizieres': (120, 182, 150), 'glycine': (176, 140, 200),
    'montagne': (128, 116, 104), 'montagne_neige': (236, 238, 242),
}
LEGENDE = [
    ('plaine', 'Plaines'), ('foret', 'Forets de feuillus'), ('foret_dense', 'Foret dense / frontiere'),
    ('taiga', 'Taiga (sapins)'), ('toundra', 'Toundra enneigee'), ('marais', 'Marais'),
    ('automne', 'Foret d\'automne'), ('bouleau', 'Forets de bouleaux'), ('savane', 'Savane'),
    ('desert', 'Desert'), ('canyon', 'Canyons rouges'), ('bambou', 'Bambouseraies (Luo)'),
    ('rizieres', 'Plaines humides (Luo)'), ('glycine', 'Glycines (Luo)'), ('montagne', 'Montagnes'),
    ('montagne_neige', 'Sommets enneiges'), ('plateau', 'Plateau de la capitale'), ('lac', 'Lacs / rivieres'),
]

# ------------------------------------------------------------------ variantes
# Angles : 0 = Est, 90 = Sud, -90 = Nord (écran, y vers le bas). Distances en blocs.
VARIANTES = {
    'A': dict(seed=1101, marquisats=[(-95, 3200, 'froid'), (178, 3150, 'humide'), (-28, 3250, 'est')], luo=(42, 3350)),
    'B': dict(seed=2207, marquisats=[(-130, 3200, 'humide'), (-52, 3200, 'froid'), (152, 3150, 'chaud')], luo=(48, 3300)),
    'C': dict(seed=3313, marquisats=[(-80, 3250, 'froid'), (200, 3100, 'chaud'), (2, 3150, 'est')], luo=(62, 3300)),
    # Proposition retenue (2026-11-22) : C, avec l'Empire Luo séparé du continent par un détroit (île au sud-est).
    'Finale': dict(seed=3313, marquisats=[(-80, 3250, 'froid'), (200, 3100, 'chaud'), (2, 3150, 'est')], luo=(62, 3300),
                   detroit=True),
    'D': dict(seed=4421, marquisats=[(-112, 3150, 'froid'), (-15, 3200, 'est'), (168, 3250, 'humide')], luo=(55, 3400)),
}
DIRECTIONS = ["de l'Est", 'du Sud-Est', 'du Sud', 'du Sud-Ouest', "de l'Ouest", 'du Nord-Ouest', 'du Nord', 'du Nord-Est']

def direction(x, y):
    """Nom de direction (Nord en haut, y vers le bas)."""
    a = math.degrees(math.atan2(y, x)) % 360
    return DIRECTIONS[int((a + 22.5) // 45) % 8]

def build(variant, px, out_dir, masque=None):
    cfg = VARIANTES[variant]
    seed = cfg['seed']
    rnd = random.Random(seed)
    H = px
    MAPW = px
    W = px + 200
    S = SPAN / px                          # blocs par pixel
    half = SPAN / 2

    # Territoires : sites, poids (diagramme de puissance), thèmes.
    sites = [dict(name="Royaume d'Eldanior", x=0.0, y=0.0, w=1.9e6, theme='royaume')]
    for ang, dist, th in cfg['marquisats']:
        a = math.radians(ang + rnd.uniform(-6, 6))
        x, y = math.cos(a) * dist, math.sin(a) * dist
        sites.append(dict(name='Marquisat ' + direction(x, y), x=x, y=y, w=rnd.uniform(-2e5, 3e5), theme=th))
    a = math.radians(cfg['luo'][0])
    sites.append(dict(name='Empire Luo', x=math.cos(a) * cfg['luo'][1], y=math.sin(a) * cfg['luo'][1],
                      w=1.1e6, theme='luo'))
    LUO = len(sites) - 1
    n = len(sites)

    # Type de frontière pour chaque paire : Luo = fleuve + montagnes ; sinon montagnes, forêt ou lacs.
    kinds = ['montagnes', 'foret', 'lacs', 'montagnes', 'foret']
    rnd.shuffle(kinds)
    border = {}
    k = 0
    for i in range(n):
        for j in range(i + 1, n):
            if LUO in (i, j):
                border[(i, j)] = 'luo'
            else:
                border[(i, j)] = kinds[k % len(kinds)]
                k += 1
    dist_ij = [[math.hypot(sites[i]['x'] - sites[j]['x'], sites[i]['y'] - sites[j]['y']) or 1 for j in range(n)]
               for i in range(n)]

    print('variante', variant, ': bruits...', flush=True)
    coast = fbm(W, H, seed + 1, 3, 6, 0.55)
    coast2 = fbm(W, H, seed + 11, 14, 3, 0.5)
    warpx = fbm(W, H, seed + 2, 2, 5, 0.6)
    warpy = fbm(W, H, seed + 3, 2, 5, 0.6)
    ridge = fbm(W, H, seed + 9, 16, 4, 0.55)
    width = fbm(W, H, seed + 10, 5, 3)
    gaps = fbm(W, H, seed + 4, 6, 3)
    bio = fbm(W, H, seed + 5, 8, 4)
    bio2 = fbm(W, H, seed + 6, 14, 3)
    hills = fbm(W, H, seed + 7, 10, 5)
    lakes = fbm(W, H, seed + 8, 12, 3)

    print('variante', variant, ': territoires et biomes...', flush=True)
    height = [0.0] * (W * H)
    kind = [None] * (W * H)
    terr = [-1] * (W * H)
    relief = bytearray(W * H)                          # 1 = forêt de frontière (et non forêt du territoire)
    lift = bytearray(W * H)                            # soulèvement du terrain (blocs) appliqué par le générateur
    sx = [s['x'] for s in sites]
    sy = [s['y'] for s in sites]
    sw = [s['w'] for s in sites]
    rng_n = range(n)
    detroit = cfg.get('detroit', False)
    strait = [0, 0, 0]
    strait_px = []
    W_full = W
    W = MAPW
    for j in range(H):
        by = (j + 0.5) * S - half
        crow, wxr, wyr, gr, br, b2r, hr, lr = coast[j], warpx[j], warpy[j], gaps[j], bio[j], bio2[j], hills[j], lakes[j]
        rr, wr, c2r = ridge[j], width[j], coast2[j]
        for i in range(W):
            bx = (i + 0.5) * S - half
            o = j * W + i
            r = math.hypot(bx, by)
            # --- côte : rayon déformé
            edge = RADIUS * (1 + 0.14 * crow[i] + 0.045 * c2r[i]) - r
            if edge < 0:
                if edge > -260 * (1 + 0.5 * crow[i]):
                    kind[o] = 'ocean_bas'
                else:
                    kind[o] = 'ocean'
                height[o] = -1
                continue
            # --- territoire (coordonnées déformées)
            qx = bx + 1100 * wxr[i]
            qy = by + 1100 * wyr[i]
            best = sec = 1e18
            bi = si = 0
            pds = [0.0] * n
            for t in rng_n:
                dx = qx - sx[t]; dy = qy - sy[t]
                pd = dx * dx + dy * dy - sw[t]
                pds[t] = pd
                if pd < best:
                    sec, si = best, bi
                    best, bi = pd, t
                elif pd < sec:
                    sec, si = pd, t
            terr[o] = bi
            bd = (sec - best) / (2 * dist_ij[bi][si])          # distance (blocs) à la frontière
            pair = (bi, si) if bi < si else (si, bi)
            btype = border[pair]
            th = sites[bi]['theme']
            lat = by / RADIUS                                   # -1 Nord .. +1 Sud
            h = 8 + min(edge, 900) / 900 * 22 + 18 * hr[i]
            kd = None

            # --- frontières
            gap = gr[i] > 0.55                                  # cols et gués
            gt = min(1.0, max(0.0, (gr[i] - 0.4) / 0.3))
            gt = gt * gt * (3 - 2 * gt)                         # col progressif (évite les marches)
            wf = 0.65 + 0.55 * wr[i]                            # largeur variable de la bande
            rg = 1 - abs(rr[i])                                 # crêtes (bruit « ridged »)
            rg = rg * rg
            luo_done = False
            mlift = 0.0
            if detroit:
                # Distance continue à la limite de l'Empire Luo (positive des deux côtés).
                if bi == LUO:
                    dL = min((pds[t] - pds[LUO]) / (2 * dist_ij[LUO][t]) for t in rng_n if t != LUO)
                else:
                    dL = (pds[LUO] - pds[bi]) / (2 * dist_ij[bi][LUO])
                hw = 260 + 160 * wr[i]                          # demi-largeur du détroit (blocs)
                if dL < hw:
                    kind[o] = 'ocean_bas' if dL > hw - 140 else 'ocean'
                    height[o] = -1
                    terr[o] = -1
                    strait[0] += i; strait[1] += j; strait[2] += 1; strait_px.append((i, j))
                    continue
                if dL < hw + 70 + 40 * b2r[i]:
                    kd = 'cotes_rocheuses' if (bi == LUO and gr[i] > 0.2) else 'plage'
                    luo_done = True
                elif bi == LUO and dL < hw + 60 + 1100 * (0.5 + 0.5 * wf):
                    luo_done = True
                    m = 1 - abs((dL - hw - 60 - 550 * (0.5 + 0.5 * wf)) / (550 * (0.5 + 0.5 * wf)))
                    m = min(1.0, m * 2.2) * (1 - 0.45 * gt) * (0.6 + 0.6 * rg)
                    mlift = max(mlift, m)
                    if m > 0.22:
                        h += 170 * m
                        kd = 'montagne_neige' if m > 0.92 else 'montagne'
            if luo_done or (detroit and btype == 'luo'):
                pass
            elif btype == 'luo' and detroit:
                if bd < hw:
                    kind[o] = 'ocean_bas' if bd > hw - 140 else 'ocean'
                    height[o] = -1
                    terr[o] = -1
                    strait[0] += i; strait[1] += j; strait[2] += 1; strait_px.append((i, j))
                    continue
                if bd < hw + 70 + 40 * b2r[i]:
                    kd = 'cotes_rocheuses' if (bi == LUO and gr[i] > 0.2) else 'plage'
                elif bi == LUO and bd < hw + 60 + 1100 * (0.5 + 0.5 * wf):
                    m = 1 - abs((bd - hw - 60 - 550 * (0.5 + 0.5 * wf)) / (550 * (0.5 + 0.5 * wf)))
                    m = min(1.0, m * 2.2) * (1 - 0.45 * gt) * (0.6 + 0.6 * rg)
                    mlift = max(mlift, m)
                    if m > 0.22:
                        h += 170 * m
                        kd = 'montagne_neige' if m > 0.92 else 'montagne'
            elif btype == 'luo':
                if bd < 45 + 20 * gr[i]:
                    kd = 'fleuve'
                elif bi == LUO and 100 < bd < 100 + 1000 * wf:
                    m = 1 - abs((bd - 100 - 500 * wf) / (500 * wf))
                    m = min(1.0, m * 1.6) * (1 - 0.75 * gt) * (0.45 + 0.75 * rg)
                    mlift = max(mlift, m)
                    if m > 0.22:
                        h += 170 * m
                        kd = 'montagne_neige' if m > 0.95 else 'montagne'
            elif btype == 'montagnes' and bd < 650 * wf:
                m = min(1.0, (1 - bd / (650 * wf)) * 1.5) * (1 - 0.75 * gt) * (0.45 + 0.75 * rg)
                mlift = max(mlift, m)
                if m > 0.22:
                    h += 170 * m
                    kd = 'montagne_neige' if (m > 0.95 or (m > 0.7 and lat < -0.3)) else 'montagne'
            elif btype == 'foret' and bd < 400 * wf and not gap:
                kd = 'foret_dense'
                h += 10
            elif btype == 'lacs' and bd < 420:
                if lr[i] > -0.1 + bd / 400:
                    kd = 'lac'
                elif bd < 300:
                    kd = 'marais' if th != 'froid' else 'taiga'

            if kd == 'foret_dense':
                relief[o] = 1

            # --- capitale : plateau au centre
            if bi == 0 and r < 560:
                tp = min(1.0, max(0.0, (r - 380) / 180))
                lift[o] = max(lift[o], int(round(PLATEAU_LIFT * (1 - tp * tp * (3 - 2 * tp)))))
            if bi == 0 and r < 520:
                h = 70
                kd = 'plateau' if r < 380 else ('colline' if kd is None else kd)

            # --- lacs intérieurs
            if kd is None and lr[i] > 0.6 and edge > 400 and r > 700:
                kd = 'lac'

            # --- biomes selon le climat du territoire
            if kd is None:
                b, b2 = br[i], b2r[i]
                if edge < 90 + 60 * b2:
                    kd = 'cotes_rocheuses' if (th in ('froid', 'est') and b2 > 0.25) else 'plage'
                elif th == 'royaume':
                    kd = 'foret' if b > 0.18 else ('colline' if hr[i] > 0.35 else 'plaine')
                elif th == 'froid':
                    kd = 'toundra' if (lat + 0.25 * b2 < -0.7 or b > 0.45) else ('neige' if hr[i] > 0.55 else 'taiga')
                elif th == 'humide':
                    kd = 'marais' if (edge < 600 and b < -0.1) else ('automne' if b2 > 0.35 else 'foret_dense' if b > 0.1 else 'foret')
                elif th == 'chaud':
                    kd = 'desert' if b > 0.15 else ('canyon' if b2 > 0.4 else 'savane')
                    if kd == 'canyon':
                        h += 25
                elif th == 'est':
                    kd = 'bouleau' if b > 0.05 else ('colline' if hr[i] > 0.3 else 'plaine')
                elif th == 'luo':
                    kd = 'bambou' if b > 0.0 else ('glycine' if b2 > 0.45 else 'rizieres')
                h += 25 * max(0.0, hr[i])
            if kd in ('lac', 'fleuve'):
                h = 4
            if mlift > 0:
                mm = min(1.0, mlift)
                lift[o] = max(lift[o], int(round(MOUNTAIN_LIFT * mm * mm * (3 - 2 * mm))))
            height[o] = h
            kind[o] = kd

    # ------------------------------------------------------------------ rendu : couleurs + ombrage
    print('variante', variant, ': rendu...', flush=True)
    img = bytearray(W_full * H * 3)
    for j in range(H):
        for i in range(W):
            o = j * W + i
            oi = j * W_full + i
            c = C[kind[o]]
            if height[o] > 0 and kind[o] not in ('lac', 'fleuve'):
                hl = height[o - 1] if i > 0 else height[o]
                hu = height[o - W] if j > 0 else height[o]
                d = ((height[o] - hl) + (height[o] - hu)) / S * 6
                f = max(0.6, min(1.35, 1 + d))
                c = (min(255, int(c[0] * f)), min(255, int(c[1] * f)), min(255, int(c[2] * f)))
            # trait de frontière
            if 0 < i < W - 1 and 0 < j < H - 1 and terr[o] >= 0:
                if (terr[o + 1] >= 0 and terr[o + 1] != terr[o]) or (terr[o + W] >= 0 and terr[o + W] != terr[o]):
                    c = (40, 30, 30)
            img[oi * 3:oi * 3 + 3] = bytes(c)
    W = W_full
    rect(img, W, H, MAPW, 0, W, H, (24, 40, 70))

    # Capitale (étoile), noms des territoires, titre, échelle, légende.
    cx, cy = MAPW // 2, H // 2
    for d in range(-6, 7):
        for (x, y) in ((cx + d, cy), (cx, cy + d), (cx + d // 2 * 1, cy + d // 2 * 1), (cx - d // 2, cy + d // 2)):
            if 0 <= x < W and 0 <= y < H:
                img[(y * W + x) * 3:(y * W + x) * 3 + 3] = bytes((200, 30, 30))
    text(img, W, H, cx - text_width('Capitale', 1) // 2, cy + 10, 'Capitale', (255, 240, 200), 1)
    sumx = [0] * n; sumy = [0] * n; cnt = [0] * n
    for o, v in enumerate(terr):
        if v >= 0:
            sumx[v] += o % MAPW; sumy[v] += o // MAPW; cnt[v] += 1
    for t, s in enumerate(sites):
        lx = sumx[t] // max(1, cnt[t])
        ly = sumy[t] // max(1, cnt[t]) - (34 if t == 0 else 0)
        if t == 0:
            lx, ly = MAPW // 2, H // 2 - 34
        name = s['name']
        text(img, W, H, lx - text_width(name) // 2, ly, name, (255, 255, 255), 2)
    if strait[2]:
        lab = 'Detroit de Luo'
        xs_ = sorted(q[0] for q in strait_px)
        mxi = xs_[len(xs_) // 2]                                   # colonne médiane du détroit
        col = sorted(q[1] for q in strait_px if abs(q[0] - mxi) <= 1)
        px_, py_ = mxi, col[len(col) // 2]                         # milieu du chenal dans cette colonne
        text(img, W, H, px_ - text_width(lab, 1) // 2, py_ - 3, lab, (190, 220, 255), 1)
    titre = 'proposition finale (C + detroit)' if detroit else 'proposition ' + variant
    text(img, W, H, 12, 12, 'Continent d\'Eldanior - ' + titre, (255, 255, 255), 2)
    text(img, W, H, 12, 34, 'graine %d  -  Nord en haut' % seed, (220, 220, 230), 1)
    # échelle 1000 blocs
    bar = int(1000 / S)
    rect(img, W, H, MAPW - bar - 20, H - 30, MAPW - 20, H - 26, (255, 255, 255))
    text(img, W, H, MAPW - bar - 20, H - 22, '1000 blocs', (255, 255, 255), 1)
    # légende
    lx0, ly0 = MAPW + 14, 60
    text(img, W, H, lx0, ly0 - 24, 'Legende', (255, 255, 255), 2)
    for k2, (key, lab) in enumerate(LEGENDE):
        y = ly0 + k2 * 16
        rect(img, W, H, lx0, y, lx0 + 12, y + 10, C[key])
        text(img, W, H, lx0 + 18, y + 2, lab, (235, 235, 235), 1, False)
    y = ly0 + len(LEGENDE) * 16 + 14
    for line in ['Trait sombre : frontiere', 'Montagnes : cols la ou', 'la chaine s\'abaisse', 'Detroit : la mer separe', 'l\'Empire Luo du continent']:
        text(img, W, H, lx0, y, line, (200, 210, 230), 1, False)
        y += 12

    path = os.path.join(out_dir, 'continent_%s.png' % variant)
    save_png(path, img, W, H)
    if masque:
        export_mask(masque, kind, terr, relief, sites, MAPW, H, S, lift)
    # Statistiques : part de chaque territoire.
    counts = [0] * n
    for v in terr:
        if v >= 0:
            counts[v] += 1
    land = sum(counts)
    stats = ', '.join('%s %d %%' % (sites[t]['name'], round(100 * counts[t] / land)) for t in range(n))
    print('écrit', path, '| terres %.0f km2 |' % (land * S * S / 1e6), stats)
    return path

# ------------------------------------------------------------------ masque pour le générateur Hytale
# Une couleur exacte par zone (Server/World/Eldanior/Zones.json). Préfixe des zones : Eldanior_.
ZONE_COLORS = {
    'Ocean': '#0a2a5a', 'Ocean_Cote': '#1d4f8c', 'Cote': '#e8d8a0', 'Cote_Froide': '#c8d0d0',
    'Royaume': '#7ec850', 'Plateau': '#d9d98c', 'Marquisat_Nord': '#3f7f6a', 'Marquisat_Ouest': '#d8b060',
    'Marquisat_Est': '#a8d070', 'Luo': '#60c0a0', 'Montagnes': '#8a7a6a', 'Montagnes_Neige': '#f0f0f0',
    'Montagnes_Luo': '#6a5a7a', 'Foret_Frontiere': '#205a2a', 'Lacs': '#3a80d0',
}
THEME_ZONE = {'royaume': 'Royaume', 'froid': 'Marquisat_Nord', 'chaud': 'Marquisat_Ouest', 'est': 'Marquisat_Est',
              'humide': 'Marquisat_Ouest', 'luo': 'Luo'}

def zone_of(k, t, rel, sites):
    if k == 'ocean':
        return 'Ocean'
    if k == 'ocean_bas':
        return 'Ocean_Cote'
    th = sites[t]['theme'] if t >= 0 else 'royaume'
    if k in ('plage', 'cotes_rocheuses'):
        return 'Cote_Froide' if th == 'froid' else 'Cote'
    if k in ('montagne', 'montagne_neige'):
        return 'Montagnes_Luo' if th == 'luo' else ('Montagnes_Neige' if th == 'froid' else 'Montagnes')
    if k == 'plateau':
        return 'Plateau'
    if k in ('lac', 'fleuve'):
        return 'Lacs'
    if k == 'foret_dense' and rel:
        return 'Foret_Frontiere'
    return THEME_ZONE[th]

def export_mask(out_dir, kind, terr, relief, sites, W, H, S, lift=None):
    import json
    os.makedirs(out_dir, exist_ok=True)
    rgb = {z: bytes(int(c[i:i + 2], 16) for i in (1, 3, 5)) for z, c in ZONE_COLORS.items()}
    img = bytearray(W * H * 3)
    count = {}
    for o in range(W * H):
        z = zone_of(kind[o], terr[o], relief[o], sites)
        count[z] = count.get(z, 0) + 1
        img[o * 3:o * 3 + 3] = rgb[z]
    save_png(os.path.join(out_dir, 'Mask.png'), img, W, H)
    if lift is not None:
        # Relief.bin : "RELF" | int W | int H | int span | W*H octets (soulèvement en blocs, 0..255).
        with open(os.path.join(out_dir, 'Relief.bin'), 'wb') as f:
            f.write(b'RELF' + struct.pack('>iii', W, H, SPAN) + bytes(lift[:W * H]))
        print('relief : soulèvement max', max(lift), 'blocs')
    names = {t['theme']: t['name'] for t in sites}
    info = {'span_blocs': SPAN, 'blocs_par_pixel': S, 'zones': {z: ZONE_COLORS[z] for z in ZONE_COLORS},
            'pixels': count, 'territoires': [s['name'] for s in sites]}
    with open(os.path.join(out_dir, 'zones.json'), 'w') as f:
        json.dump(info, f, indent=2, ensure_ascii=False)
    print('masque', W, 'x', H, '->', out_dir, {z: round(100 * c / (W * H), 1) for z, c in sorted(count.items())})

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('sortie')
    ap.add_argument('--variantes', default='A,B,C,D')
    ap.add_argument('--px', type=int, default=800)
    ap.add_argument('--masque', help='dossier où écrire Mask.png + zones.json (une seule variante)')
    a = ap.parse_args()
    os.makedirs(a.sortie, exist_ok=True)
    for v in a.variantes.split(','):
        build(v.strip(), a.px, a.sortie, a.masque)

if __name__ == '__main__':
    main()
