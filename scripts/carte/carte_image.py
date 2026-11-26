#!/usr/bin/env python3
"""Transforme une image de carte (vue du dessus colorée et ombrée) en données pour le générateur Hytale « Eldanior ».

Entrée : scripts/carte/sources/continent_reference.png (carte de 960 x 960 px, Nord en haut).
Chaque pixel couvre BPP blocs (13 par défaut : 960 px = 12 480 blocs, le continent fait environ 11 200 blocs de large).

Ce que le script déduit de l'image :
  - mer, rivières, fleuves et lacs (couleurs bleues et bleu-vert), la mer étant l'eau reliée au bord de l'image ;
  - le relief : un indice de montagne par couleur (neige, roche, collines sèches, forêts, prairies), lissé à deux
    échelles, plus les crêtes retrouvées dans l'ombrage de l'image (intégration le long de la direction de la lumière) ;
  - les vallées : le terrain descend vers chaque cours d'eau, dont le niveau monte doucement de l'embouchure vers la
    source (jamais de remontée vers l'aval) ; les lacs sont plats ;
  - les zones Hytale (biomes) : selon la couleur et la latitude (Nord froid, centre tempéré, Sud plus doux).

Sorties dans <dossier> :
  Mask.png     une couleur exacte par zone (lue par le générateur V1 de Hytale) ;
  zones.json   couleurs, emprise, point d'apparition ;
  Terrain.bin  (gzip) "TER2" | W | H | span | Y de la limite des neiges | puis, par pixel : int16 hauteur*8 (blocs au-dessus de la mer, le fond
               pour l'eau), int16 niveau d'eau*8, int16 distance signée à l'eau*4 (blocs, négative dans l'eau),
               uint8 rugosité (0..255), uint8 drapeaux (1 = mer, 2 = eau intérieure) ;
  apercu.png   relief ombré + zones, à comparer avec l'image d'origine.

Usage :
    python3 -I scripts/carte/carte_image.py <dossier> [--image <png>] [--bpp 13]
"""
import argparse, gzip, json, math, os, struct, sys
from array import array

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from image_util import read_png, save_png, box_blur, distance, components, smoothstep

HERE = os.path.dirname(os.path.abspath(__file__))
SEA = 114
MAX_H = 152            # hauteur maximale au-dessus de la mer avant le bruit de détail du générateur (Y 266)
SOFT = 100             # au-delà, la hauteur est compressée en douceur vers MAX_H

# Classes de couleur
EAU, NEIGE, ROCHE, FORET, PRAIRIE, COLLINE, SEC, AUTRE = range(8)
INDICE_MONTAGNE = {EAU: 0.0, NEIGE: 1.0, ROCHE: 0.72, FORET: 0.22, PRAIRIE: 0.04, COLLINE: 0.3, SEC: 0.42, AUTRE: 0.2}

# Zones : couleur exacte dans Mask.png
ZONE_COLORS = {
    'Ocean': '#0a2a5a', 'Ocean_Cote': '#1d4f8c', 'Cote': '#e8d8a0', 'Cote_Froide': '#c8d0d0',
    'Plaines': '#7ec850', 'Collines': '#b8b060', 'Automne': '#d08040', 'Foret': '#205a2a',
    'Nord': '#3f7f6a', 'Nord_Plaines': '#90b890', 'Montagnes': '#8a7a6a', 'Montagnes_Nord': '#6a6a7a',
    'Montagnes_Neige': '#f0f0f0', 'Montagnes_Foret': '#4a6a4a', 'Luo': '#60c0a0',
}
APERCU = {  # couleurs de l'aperçu (pas celles du masque)
    'Ocean': (30, 70, 150), 'Ocean_Cote': (50, 110, 190), 'Cote': (230, 214, 160), 'Cote_Froide': (200, 200, 190),
    'Plaines': (110, 180, 60), 'Collines': (160, 165, 80), 'Automne': (200, 130, 60), 'Foret': (40, 100, 45),
    'Nord': (60, 110, 85), 'Nord_Plaines': (130, 170, 110), 'Montagnes': (125, 115, 100),
    'Montagnes_Nord': (110, 110, 120), 'Montagnes_Neige': (240, 240, 245), 'Montagnes_Foret': (70, 100, 70),
    'Luo': (120, 200, 150),
}


def classe(r, g, b):
    mx = max(r, g, b); mn = min(r, g, b)
    if b > r + 35 and b >= g - 22:
        return EAU
    if mn > 150 and mx - mn < 45:
        return NEIGE
    if mx - mn < 38:
        return ROCHE
    if g > 2 * r and g < 110 and g > b + 20:
        return FORET
    if g > r + 28 and g > b + 50:
        return PRAIRIE
    if r > g + 15 and r > b + 40:
        return SEC
    if g > b + 25:
        return COLLINE
    return AUTRE


def build(img_path, out, bpp):
    W, H, img = read_png(img_path)
    N = W * H
    span = W * bpp
    print('image', W, 'x', H, '->', span, 'blocs', flush=True)

    # ---------------------------------------------------------------- classes
    cls = bytearray(N)
    lum = array('d', bytes(8 * N))
    for i in range(N):
        r, g, b = img[3 * i], img[3 * i + 1], img[3 * i + 2]
        cls[i] = classe(r, g, b)
        lum[i] = (r + g + b) / 3.0
    marge = 14
    for y in range(H):
        for x in range(W):
            if x < marge or y < marge or x >= W - marge or y >= H - marge:
                cls[y * W + x] = EAU
    water = bytearray(1 if c == EAU else 0 for c in cls)

    # Petites taches d'eau isolées (moins de 6 px) : du bruit de l'image, pas des lacs.
    lab, sizes = components(water, W, H)
    for i in range(N):
        if water[i] and sizes[lab[i]] < 6:
            water[i] = 0
            cls[i] = AUTRE

    # ---------------------------------------------------------------- mer / eau intérieure
    d_land, _ = distance(bytearray(1 - w for w in water), W, H)          # pour l'eau : distance à la terre
    core = bytearray(1 if (water[i] and d_land[i] > 3.2) else 0 for i in range(N))
    lab, sizes = components(core, W, H)
    big = max(range(len(sizes)), key=sizes.__getitem__)
    seacore = bytearray(1 if lab[i] == big else 0 for i in range(N))
    d_core, _ = distance(seacore, W, H)
    sea = bytearray(1 if (water[i] and d_core[i] <= 4.3) else 0 for i in range(N))
    inland = bytearray(1 if (water[i] and not sea[i]) else 0 for i in range(N))
    # Les rivières de l'image sont parfois coupées d'un pixel : fermeture (dilatation puis érosion d'un pixel).
    d_in, _ = distance(inland, W, H)
    dil = bytearray(1 if d_in[i] <= 1.0 else 0 for i in range(N))
    d_out, _ = distance(bytearray(1 - v for v in dil), W, H)
    for i in range(N):
        if dil[i] and d_out[i] > 1.0 and not sea[i]:
            inland[i] = 1
    water = bytearray(1 if (sea[i] or inland[i]) else 0 for i in range(N))
    land = bytearray(1 - w for w in water)
    print('eau : mer %d px, eau intérieure %d px' % (sum(sea), sum(inland)), flush=True)

    # ---------------------------------------------------------------- indices de relief
    mi = array('d', (INDICE_MONTAGNE[cls[i]] if land[i] else 0.0 for i in range(N)))
    ms = box_blur(mi, W, H, 4, 2)
    ml = box_blur(mi, W, H, 12, 2)
    d_sea, _ = distance(sea, W, H)                                   # distance à la mer (px)

    # Ombrage de l'image -> crêtes. Anomalie de luminosité (terres seulement), intégrée le long de la lumière.
    lm = array('d', (lum[i] if land[i] else 0.0 for i in range(N)))
    lw = box_blur(array('d', (1.0 if land[i] else 0.0 for i in range(N))), W, H, 6, 1)
    lb = box_blur(lm, W, H, 6, 1)
    anom = array('d', ((lum[i] - lb[i] / lw[i]) if (land[i] and lw[i] > 0.05) else 0.0 for i in range(N)))

    def integre(dx, dy, lam=0.9):
        f = array('d', bytes(8 * N)); bk = array('d', bytes(8 * N))
        ys = range(H) if dy >= 0 else range(H - 1, -1, -1)
        xs = list(range(W)) if dx >= 0 else list(range(W - 1, -1, -1))
        for y in ys:
            py = y - dy
            for x in xs:
                px = x - dx
                i = y * W + x
                prev = f[py * W + px] if (0 <= px < W and 0 <= py < H) else 0.0
                f[i] = lam * prev + anom[i]
        ys = range(H - 1, -1, -1) if dy >= 0 else range(H)
        xs = list(range(W - 1, -1, -1)) if dx >= 0 else list(range(W))
        for y in ys:
            py = y + dy
            for x in xs:
                px = x + dx
                i = y * W + x
                prev = bk[py * W + px] if (0 <= px < W and 0 <= py < H) else 0.0
                bk[i] = lam * prev - anom[i]
        return array('d', ((f[i] + bk[i]) * 0.5 for i in range(N)))

    # Direction de la lumière : celle dont les crêtes retrouvées collent le mieux aux massifs (corrélation avec ms).
    # On garde la moyenne des deux meilleures directions voisines (moins de stries dans un seul sens).
    scores = []
    ms_mean = sum(ms) / N
    for dx, dy in ((1, 1), (-1, 1), (1, -1), (-1, -1), (0, 1), (1, 0), (0, -1), (-1, 0)):
        rr = integre(dx, dy)
        c = sum((rr[i]) * (ms[i] - ms_mean) for i in range(0, N, 7) if land[i])
        print('  lumière venant de (%d,%d) : corrélation %.0f' % (-dx, -dy, c), flush=True)
        scores.append((c, dx, dy, rr))
    scores.sort(key=lambda t: -t[0])
    a, b = scores[0], scores[1]
    if abs(a[1] - b[1]) + abs(a[2] - b[2]) > 2:          # pas voisines : on garde la meilleure seule
        b = a
    ridges = array('d', ((a[3][i] + b[3][i]) * 0.5 for i in range(N)))
    ridges = box_blur(ridges, W, H, 1, 1)
    ldx, ldy = a[1], a[2]
    sd = math.sqrt(sum(v * v for v in ridges[::5]) / (N / 5)) or 1.0
    print('lumière retenue : vient de (%d,%d) et (%d,%d)' % (-a[1], -a[2], -b[1], -b[2]), flush=True)

    # ---------------------------------------------------------------- hauteur des terres (blocs au-dessus de la mer)
    h = array('d', bytes(8 * N))
    for i in range(N):
        if not land[i]:
            continue
        massif = 0.55 * ms[i] + 0.45 * ml[i]
        base = 2 + 10 * smoothstep(0, 12, d_sea[i]) + 22 * ml[i] + 125 * max(0.0, massif) ** 1.45
        detail = (3 + 45 * ms[i] ** 1.3) * max(-2.2, min(2.2, ridges[i] / sd))
        v = base + detail
        if v > SOFT:                                  # sommets compressés en douceur (pas de plateaux plafonnés)
            v = SOFT + (MAX_H - SOFT) * (1 - math.exp(-(v - SOFT) / (MAX_H - SOFT)))
        h[i] = max(1.0, v)
    h = box_blur(h, W, H, 1, 1)                                         # adoucit le bruit de l'image (JPEG)
    for i in range(N):
        if not land[i]:
            h[i] = 0.0

    # ---------------------------------------------------------------- niveau des cours d'eau (embouchure -> source)
    d_l, near_l = distance(land, W, H)                                   # plus proche pixel de terre
    wl = array('d', bytes(8 * N))
    seen = bytearray(N)
    from collections import deque
    q = deque()
    for i in range(N):
        if inland[i]:
            x = i % W
            for j in (i - 1, i + 1, i - W, i + W):
                if 0 <= j < N and sea[j]:
                    q.append(i); seen[i] = 1; wl[i] = 0.0
                    break
    # Des lacs sans exutoire vers la mer : on les relie quand même (départ depuis leur pixel le plus bas).
    order = []
    def bfs():
        while q:
            i = q.popleft()
            order.append(i)
            x = i % W
            for j in (i - 1, i + 1, i - W, i + W, i - W - 1, i - W + 1, i + W - 1, i + W + 1):
                if 0 <= j < N and inland[j] and not seen[j] and abs(j % W - x) <= 1:
                    seen[j] = 1
                    bank = h[near_l[j]] if near_l[j] >= 0 else 0.0
                    target = max(0.0, bank - 3.0)
                    wl[j] = max(wl[i], min(target, wl[i] + 0.6))
                    q.append(j)
    bfs()
    rest = [i for i in range(N) if inland[i] and not seen[i]]
    while rest:
        s = min(rest, key=lambda i: h[near_l[i]] if near_l[i] >= 0 else 0)
        seen[s] = 1
        wl[s] = max(0.0, (h[near_l[s]] if near_l[s] >= 0 else 0) - 3.0)
        q.append(s); bfs()
        rest = [i for i in rest if not seen[i]]
    # Lacs (eau large) : niveau plat = le plus bas du lac.
    wide = bytearray(1 if (inland[i] and d_land[i] > 1.5) else 0 for i in range(N))
    d_w, _ = distance(wide, W, H)
    lake = bytearray(1 if (inland[i] and d_w[i] <= 2.0) else 0 for i in range(N))
    lab, sizes = components(lake, W, H)
    lmin = {}
    for i in range(N):
        if lake[i]:
            k = lab[i]
            lmin[k] = min(lmin.get(k, 1e9), wl[i])
    for i in range(N):
        if lake[i]:
            wl[i] = lmin[lab[i]]
    print('cours d\'eau : %d px, lacs : %d (%d px), niveau max %.0f' % (
        sum(inland), len(sizes), sum(lake), max(wl)), flush=True)

    neige_h = sorted(h[i] for i in range(N) if land[i] and cls[i] == NEIGE)
    snow_y = int(SEA + (neige_h[int(len(neige_h) * 0.5)] if neige_h else 130))
    # 50e centile des pixels blancs de l'image : le générateur ajoute des pics, la neige couvre donc plus que la moitié.
    print('limite des neiges : Y %d (%d pixels blancs)' % (snow_y, len(neige_h)), flush=True)
    h_raw = array('d', h)
    wl_in = array('d', wl)

    # ---------------------------------------------------------------- vallées : le terrain descend vers l'eau
    d_wat, near_w = distance(water, W, H)
    for i in range(N):
        if not land[i]:
            continue
        j = near_w[i]
        lvl = wl[j] if inland[j] else 0.0
        k = 0.12 + 0.95 * ms[i]
        dist_b = d_wat[i] * bpp
        h[i] = max(lvl + 1.0, min(h[i], lvl + 1.5 + k * dist_b))
        wl[i] = lvl                                                     # niveau de l'eau la plus proche (berges)

    # Fond : mer (pente douce puis plus profonde), eau intérieure (WL - 3).
    for i in range(N):
        if sea[i]:
            dd = d_land[i] * bpp
            h[i] = -(2.0 + min(38.0, dd * 0.1 + (dd / 60.0) ** 1.4))
        elif inland[i]:
            h[i] = wl[i] - 3.0

    # Distance signée à l'eau (blocs)
    sdf = array('d', bytes(8 * N))
    for i in range(N):
        sdf[i] = (d_wat[i] - 0.5) * bpp if land[i] else -(d_land[i] - 0.5) * bpp

    # ---------------------------------------------------------------- zones
    frac = {}
    for c in (FORET, PRAIRIE, COLLINE, SEC):
        frac[c] = box_blur(array('d', (1.0 if cls[i] == c else 0.0 for i in range(N))), W, H, 3, 1)
    from continent import fbm
    wu = fbm(W, H, 71, 3, 4, 0.55)
    wv = fbm(W, H, 72, 3, 4, 0.55)
    zone = [None] * N
    for i in range(N):
        y, x = divmod(i, W)
        u = x / W + 0.07 * wu[y][x]
        v = y / H + 0.07 * wv[y][x]
        if sea[i]:
            zone[i] = 'Ocean_Cote' if d_land[i] <= 6 else 'Ocean'
            continue
        if inland[i]:
            continue
        nord = v < 0.36
        hh = h[i]
        if d_sea[i] <= 1.6 and hh <= 7:
            zone[i] = 'Cote_Froide' if nord else 'Cote'
        elif cls[i] == NEIGE or (ms[i] > 0.72 and hh > 120):
            zone[i] = 'Montagnes_Neige'
        elif ms[i] > 0.56 or (cls[i] == ROCHE and ms[i] > 0.4):
            zone[i] = 'Montagnes_Nord' if nord else ('Montagnes_Foret' if frac[FORET][i] > 0.12 else 'Montagnes')
        elif ms[i] > 0.42 and not nord and frac[FORET][i] > 0.08:
            zone[i] = 'Montagnes_Foret'
        elif frac[FORET][i] > 0.33:
            zone[i] = 'Nord' if nord else 'Foret'
        elif ((u - 0.74) / 0.17) ** 2 + ((v - 0.84) / 0.16) ** 2 < 1:
            zone[i] = 'Luo'
        elif nord:
            zone[i] = 'Nord_Plaines' if frac[PRAIRIE][i] > 0.5 else 'Nord'
        else:
            col, sec_ = frac[COLLINE][i], frac[SEC][i]
            if frac[PRAIRIE][i] >= max(col, sec_):
                zone[i] = 'Plaines'
            elif sec_ > col and v > 0.5:
                zone[i] = 'Automne'
            else:
                zone[i] = 'Collines'
    # Eau intérieure : la zone de la terre la plus proche (le lit garde les sols du pays traversé).
    for i in range(N):
        if zone[i] is None:
            j = near_l[i]
            zone[i] = zone[j] if (j >= 0 and zone[j] is not None) else 'Plaines'
            if zone[i] is None:
                zone[i] = 'Plaines'

    # ---------------------------------------------------------------- sorties
    os.makedirs(out, exist_ok=True)
    rgb = {z: bytes(int(c[k:k + 2], 16) for k in (1, 3, 5)) for z, c in ZONE_COLORS.items()}
    mask = bytearray(N * 3)
    count = {}
    for i in range(N):
        mask[3 * i:3 * i + 3] = rgb[zone[i]]
        count[zone[i]] = count.get(zone[i], 0) + 1
    save_png(os.path.join(out, 'Mask.png'), mask, W, H)

    fine = affine(W, H, bpp, h_raw, wl_in, ms, sea, inland, land, near_l)
    W2, H2, h2, wl2, sdf2, ms2, sea2, in2 = fine

    def clamp16(v):
        return max(-32768, min(32767, int(round(v))))
    N2 = W2 * H2
    buf = bytearray(b'TER2' + struct.pack('>iiii', W2, H2, span, snow_y))
    hh_ = array('h', (clamp16(v * 8) for v in h2)); hh_.byteswap()
    ww_ = array('h', (clamp16(v * 8) for v in wl2)); ww_.byteswap()
    ss_ = array('h', (clamp16(v * 4) for v in sdf2)); ss_.byteswap()
    rug = bytes(0 if (sea2[i] or in2[i]) else max(0, min(255, int(ms2[i] * 300))) for i in range(N2))
    flags = bytes((1 if sea2[i] else 0) | (2 if in2[i] else 0) for i in range(N2))
    buf += hh_.tobytes() + ww_.tobytes() + ss_.tobytes() + rug + flags
    with gzip.open(os.path.join(out, 'Terrain.bin'), 'wb', 9) as f:
        f.write(buf)
    print('Terrain.bin : %d x %d (%.1f blocs/px)' % (W2, H2, span / W2), flush=True)

    # Point d'apparition : prairie basse, peu pentue, la plus proche du centre.
    cx = cy = W // 2
    best_s = None
    for i in range(N):
        if zone[i] != 'Plaines' or sdf[i] < 60:
            continue
        y, x = divmod(i, W)
        if 2 <= x < W - 2 and 2 <= y < H - 2:
            slope = abs(h[i + 1] - h[i - 1]) + abs(h[i + W] - h[i - W])
            if slope > 3:
                continue
        dd = (x - cx) ** 2 + (y - cy) ** 2
        if best_s is None or dd < best_s[0]:
            best_s = (dd, x, y, h[i])
    _, sx, sy, sh = best_s
    spawn = [int((sx + 0.5) * bpp - span / 2), int(SEA + sh + 8), int((sy + 0.5) * bpp - span / 2)]

    land_px = sum(land)
    info = {'recettes': 'image', 'span_blocs': span, 'blocs_par_pixel': bpp, 'zones': ZONE_COLORS, 'pixels': count,
            'spawn': spawn, 'neige_y': snow_y, 'lumiere': [-ldx, -ldy], 'terres_km2': round(land_px * bpp * bpp / 1e6, 1)}
    json.dump(info, open(os.path.join(out, 'zones.json'), 'w'), indent=2, ensure_ascii=False)

    # Aperçu : couleurs des zones, ombrées par le relief final, eau en bleu selon la profondeur.
    prev = bytearray(N * 3)
    for i in range(N):
        y, x = divmod(i, W)
        if water[i]:
            dpt = (wl[i] if inland[i] else 0) - h[i]
            c = (max(0, 70 - 2 * int(dpt)), max(60, 150 - 2 * int(dpt)), max(140, 235 - 2 * int(dpt)))
        else:
            c = APERCU[zone[i]]
            if h[i] > 150 and zone[i].startswith('Montagnes'):
                c = (240, 240, 245)
            hl = h[i - 1] if x > 0 else h[i]
            hu = h[i - W] if y > 0 else h[i]
            f = max(0.45, min(1.5, 1 + ((h[i] - hl) + (h[i] - hu)) / bpp * 1.2))
            c = tuple(min(255, int(v * f)) for v in c)
        prev[3 * i:3 * i + 3] = bytes(c)
    save_png(os.path.join(out, 'apercu.png'), prev, W, H)
    print('zones', {z: round(100 * c / N, 1) for z, c in sorted(count.items())})
    print('terres %.1f km2, hauteur max %.0f, apparition %s' % (info['terres_km2'], max(h), spawn))
    print('écrit dans', out)


def upsample(a, W, H, U):
    """Agrandissement bilinéaire (centres de pixels alignés)."""
    W2, H2 = W * U, H * U
    out = array('d', bytes(8 * W2 * H2))
    xs = []
    for x2 in range(W2):
        fx = (x2 + 0.5) / U - 0.5
        x0 = int(math.floor(fx)); t = fx - x0
        xs.append((max(0, min(W - 1, x0)), max(0, min(W - 1, x0 + 1)), t))
    for y2 in range(H2):
        fy = (y2 + 0.5) / U - 0.5
        y0 = int(math.floor(fy)); ty = fy - y0
        r0 = max(0, min(H - 1, y0)) * W; r1 = max(0, min(H - 1, y0 + 1)) * W
        o = y2 * W2
        for x2, (a0, a1, t) in enumerate(xs):
            u = a[r0 + a0] + (a[r0 + a1] - a[r0 + a0]) * t
            v = a[r1 + a0] + (a[r1 + a1] - a[r1 + a0]) * t
            out[o + x2] = u + (v - u) * ty
    return out


def affine(W, H, bpp, h_raw, wl_in, ms, sea, inland, land, near_l, U=2):
    """Champs de sortie à résolution double : côtes et rivières lissées, vallées, fonds, distance signée."""
    N = W * H
    W2, H2 = W * U, H * U
    N2 = W2 * H2
    b2 = bpp / U
    print('résolution fine %d x %d...' % (W2, H2), flush=True)
    # Hauteur brute des terres, prolongée sous l'eau par la terre la plus proche (pas de berges qui plongent).
    h_ext = array('d', ((h_raw[i] if land[i] else (h_raw[near_l[i]] if near_l[i] >= 0 else 0.0)) for i in range(N)))
    # Niveau des eaux intérieures, prolongé partout par l'eau intérieure la plus proche.
    _, near_in = distance(inland, W, H)
    wl_ext = array('d', ((wl_in[near_in[i]] if near_in[i] >= 0 else 0.0) for i in range(N)))
    h2 = upsample(h_ext, W, H, U)
    wl2 = upsample(wl_ext, W, H, U)
    ms2 = upsample(ms, W, H, U)
    seacov = box_blur(upsample(array('d', (float(v) for v in sea)), W, H, U), W2, H2, 1, 1)
    incov = box_blur(upsample(array('d', (float(v) for v in inland)), W, H, U), W2, H2, 1, 1)
    sea2 = bytearray(1 if seacov[i] > 0.5 else 0 for i in range(N2))
    in2 = bytearray(1 if (not sea2[i] and incov[i] > 0.3) else 0 for i in range(N2))
    lab, sizes = components(in2, W2, H2)
    for i in range(N2):
        if in2[i] and sizes[lab[i]] < 12:
            in2[i] = 0
    water2 = bytearray(sea2[i] | in2[i] for i in range(N2))
    land2 = bytearray(1 - v for v in water2)
    d_land2, _ = distance(land2, W2, H2)
    d_wat2, near_w2 = distance(water2, W2, H2)
    wlf = array('d', bytes(8 * N2))
    sdf2 = array('d', bytes(8 * N2))
    for i in range(N2):
        if land2[i]:
            j = near_w2[i]
            lvl = wl2[j] if in2[j] else 0.0
            k = 0.12 + 0.95 * ms2[i]
            h2[i] = max(lvl + 1.0, min(h2[i], lvl + 1.5 + k * d_wat2[i] * b2))
            wlf[i] = lvl
            sdf2[i] = (d_wat2[i] - 0.5) * b2
        elif sea2[i]:
            dd = d_land2[i] * b2
            h2[i] = -(2.0 + min(38.0, dd * 0.1 + (dd / 60.0) ** 1.4))
            sdf2[i] = -(d_land2[i] - 0.5) * b2
        else:
            wlf[i] = wl2[i]
            h2[i] = wl2[i] - 3.0
            sdf2[i] = -(d_land2[i] - 0.5) * b2
    print('  mer %d px, eau intérieure %d px' % (sum(sea2), sum(in2)), flush=True)
    return W2, H2, h2, wlf, sdf2, ms2, sea2, in2


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('sortie')
    ap.add_argument('--image', default=os.path.join(HERE, 'sources', 'continent_reference.png'))
    ap.add_argument('--bpp', type=int, default=13)
    a = ap.parse_args()
    build(a.image, a.sortie, a.bpp)


if __name__ == '__main__':
    main()
