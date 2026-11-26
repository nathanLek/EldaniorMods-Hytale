"""Fenetre des quetes de PNJ facon « Chapitres » du jeu (Common/UI/Custom/Quest/Chapitres.ui + images Quest/Chap*.png),
pilotee par quest/chapitres/QuestChaptersPage.java.

Mise en page reprise du client (Data/Shared/UI/Chapters : ChaptersPage.xaml, ChapterDetailPanel.xaml, ChapterCard.xaml,
ChaptersTokens.xaml), en px de conception 1920x1080 :
  - fond : plaque floutee, bande d'encre (separateur en biais), voile sombre a droite, eclats, lueur derriere la carte ;
  - a gauche (94, 114, largeur 419) : embleme, chaine + PNJ, titre (« Acte I »), sous-titre (nom de la quete), trait,
    histoire / explications / attendus (zone defilante), recompenses (cases de 122, pas de 138, 3 visibles) ;
  - a droite : carrousel de 3 cartes (347x673, pas de 379), cadre par defaut / choisi / verrouille, illustration couleur
    pour la carte choisie et grisee pour les autres, nom de l'acte et etat en bas de la carte ;
  - sous les cartes : bouton d'action (provisoire) et Fermer, ligne d'aide.

Images : copies du client (fond, eclats, lueurs, cadres de carte, cadre d'embleme, case de recompense, trait) et images
dessinees ici (illustrations des actes, separateur, puces, coches, cadenas, fleches).

Usage : python3 scripts/quetes/gen_chapitres_ui.py [src/main/resources] [--ui] [--tints] [--art=Trone ...] [--planche=/chemin/planche.png]
"""
import math, os, random, shutil, struct, sys, zlib

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'rune_aura_icons'))
import png  # noqa: E402

_ARGS = [a for a in sys.argv[1:] if not a.startswith("--")]
RES = _ARGS[0] if _ARGS else os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "src", "main", "resources")
OUT = os.path.join(RES, "Common", "UI", "Custom", "Quest")
CLIENT = os.path.expanduser("~/Library/Application Support/Hytale/install/release/package/game/latest/Client/"
                            "Hytale.app/Contents/Resources/Data")
CHAP = os.path.join(CLIENT, "Shared", "UI", "Chapters", "Images")

# Doit correspondre a QuestChaptersPage.java
CARDS, NOTES, OBJECTIVES, REWARDS = 3, 3, 4, 3
ARTS = ["Aube", "Nuit", "Duel", "Trone",              # ChapterQuests.Art (Trone : quete principale, 2026-11-14)
        "Dojo", "Vent", "Brigands", "Trork", "Sensei",  # Epreuves du Dojo, une par etape (2026-11-14)
        "Lisiere", "Marais", "Forge", "Dunes", "Outlanders", "Arene", "Vide", "Ombre",   # QuetesMetiers (2026-11-14)
        "Rumeurs", "Hameau", "Azur", "Mine"]             # quete du Roi, etape 1 et temoins (QueteRoi, 2026-11-14)

# Jetons du client (ChaptersTokens.xaml)
PAGE_L, PAGE_T, PANEL_W = 94, 114, 419
TEXT_L, TEXT_W = PAGE_L + 2, 415
CARD_W, CARD_H, CARD_PITCH = 347, 673, 379
CARD_L0, CARD_T = PAGE_L + PANEL_W + 42 + 56, PAGE_T + 90 + 47          # 611, 251
FRAME_DX, FRAME_DY, FRAME_W, FRAME_H = -24, -47, 392, 720
ART_DX, ART_DY, ART_W, ART_H = 10, 15, 324, 586
SLOT, SLOT_PITCH = 122, 138
REW_L, REW_T = PAGE_L + 3, PAGE_T + 698                                   # 97, 812
GOLD_TITLE, TAGLINE, TEXT2, HEAD = "#f2c45e", "#C58643", "#77879D", "#C4CEDC"


# ==================== PNG ====================

def read_any(path):
    """PNG 8 bits RGBA, RGB ou palette (avec tRNS) -> (w, h, px[y][x] = [r, g, b, a])."""
    d = open(path, 'rb').read()
    pos, idat, plte, trns = 8, b'', None, None
    while pos < len(d):
        ln = struct.unpack('>I', d[pos:pos + 4])[0]; t = d[pos + 4:pos + 8]; c = d[pos + 8:pos + 8 + ln]; pos += 12 + ln
        if t == b'IHDR': w, h, bd, ct = struct.unpack('>IIBB', c[:10]); assert bd == 8
        elif t == b'PLTE': plte = c
        elif t == b'tRNS': trns = c
        elif t == b'IDAT': idat += c
    bpp = {6: 4, 2: 3, 3: 1}[ct]
    raw = zlib.decompress(idat); stride = w * bpp; prev = bytearray(stride); i = 0; px = []
    for y in range(h):
        f = raw[i]; i += 1; line = bytearray(raw[i:i + stride]); i += stride
        for x in range(stride):
            a = line[x - bpp] if x >= bpp else 0; b = prev[x]; c = prev[x - bpp] if x >= bpp else 0
            if f == 1: line[x] = (line[x] + a) & 255
            elif f == 2: line[x] = (line[x] + b) & 255
            elif f == 3: line[x] = (line[x] + ((a + b) >> 1)) & 255
            elif f == 4:
                p = a + b - c; pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
                line[x] = (line[x] + (a if pa <= pb and pa <= pc else (b if pb <= pc else c))) & 255
        row = []
        for x in range(w):
            if ct == 6: row.append(list(line[x * 4:x * 4 + 4]))
            elif ct == 2: row.append(list(line[x * 3:x * 3 + 3]) + [255])
            else:
                k = line[x]
                row.append([plte[k * 3], plte[k * 3 + 1], plte[k * 3 + 2], trns[k] if trns and k < len(trns) else 255])
        px.append(row); prev = line
    return w, h, px


def copy(src, name):
    shutil.copyfile(src, os.path.join(OUT, name))


def blank(w, h):
    return [[[0, 0, 0, 0] for _ in range(w)] for _ in range(h)]


def mix(a, b, t):
    t = max(0.0, min(1.0, t))
    return [a[i] + (b[i] - a[i]) * t for i in range(len(a))]


def hexc(s):
    s = s.lstrip('#')
    return [int(s[i:i + 2], 16) for i in (0, 2, 4)]


def over(dst, col, a):
    """Compose col (rgb) d'opacite a sur dst (rgba)."""
    if a <= 0: return
    a = min(1.0, a)
    da = dst[3] / 255.0
    oa = a + da * (1 - a)
    for i in range(3):
        dst[i] = (col[i] * a + dst[i] * da * (1 - a)) / oa if oa > 0 else 0
    dst[3] = oa * 255


def fill_poly(img, pts, col, alpha=1.0, ss=3):
    """Polygone plein, anticrenele par sur-echantillonnage vertical (ss lignes par pixel) et couverture horizontale."""
    h, w = len(img), len(img[0])
    ys = [p[1] for p in pts]
    y0, y1 = max(0, int(min(ys))), min(h - 1, int(max(ys)) + 1)
    for y in range(y0, y1 + 1):
        cover = [0.0] * w
        for s in range(ss):
            yy = y + (s + 0.5) / ss
            xs = []
            for i in range(len(pts)):
                (ax, ay), (bx, by) = pts[i], pts[(i + 1) % len(pts)]
                if (ay <= yy < by) or (by <= yy < ay):
                    xs.append(ax + (yy - ay) * (bx - ax) / (by - ay))
            xs.sort()
            for k in range(0, len(xs) - 1, 2):
                xa, xb = max(0.0, xs[k]), min(float(w), xs[k + 1])
                if xb <= xa: continue
                ia, ib = int(xa), min(w - 1, int(xb))
                for x in range(ia, ib + 1):
                    c = min(xb, x + 1) - max(xa, x)
                    if c > 0: cover[x] += c / ss
        for x in range(w):
            if cover[x] > 0: over(img[y][x], col, alpha * min(1.0, cover[x]))


def seg_dist(px_, py, ax, ay, bx, by):
    dx, dy = bx - ax, by - ay
    t = max(0.0, min(1.0, ((px_ - ax) * dx + (py - ay) * dy) / (dx * dx + dy * dy or 1)))
    return math.hypot(px_ - ax - t * dx, py - ay - t * dy)


def stroke(img, pts, width, color_at, alpha=1.0):
    """Polyligne d'epaisseur width ; color_at(x, y) -> rgb."""
    h, w = len(img), len(img[0])
    r = width / 2.0
    for i in range(len(pts) - 1):
        (ax, ay), (bx, by) = pts[i], pts[i + 1]
        for y in range(max(0, int(min(ay, by) - r - 1)), min(h, int(max(ay, by) + r + 2))):
            for x in range(max(0, int(min(ax, bx) - r - 1)), min(w, int(max(ax, bx) + r + 2))):
                d = seg_dist(x + 0.5, y + 0.5, ax, ay, bx, by)
                cov = max(0.0, min(1.0, r + 0.5 - d))
                if cov > 0 and img[y][x][3] < 255 * alpha * cov + 1:
                    over(img[y][x], color_at(x, y), alpha * cov)


def save(name, img):
    png.write(os.path.join(OUT, name), len(img[0]), len(img), img)


# ==================== IMAGES COPIEES DU CLIENT ====================

def fade_bottom(img, start=0.6):
    """Masque du client sur l'embleme : opaque en haut, transparent en bas (fondu de 40 % a 0 % de la hauteur, par le bas)."""
    h = len(img)
    for y in range(h):
        t = (h - 1 - y) / (h - 1)                 # 1 en haut, 0 en bas
        k = min(1.0, t / 0.4)
        for p in img[y]: p[3] *= k


def client_images():
    copy(os.path.join(CHAP, "ChaptersBackground.png"), "ChapBackground@2x.png")
    for n in range(1, 17):
        copy(os.path.join(CHAP, "ChapterCardShard%02d.png" % n), "ChapShard%02d.png" % n)
    copy(os.path.join(CHAP, "ChapterShards.png"), "ChapShards@2x.png")
    copy(os.path.join(CHAP, "ChaptersBackgroundGlows.png"), "ChapGlows@2x.png")
    copy(os.path.join(CHAP, "ChaptersBackgroundShadow.png"), "ChapShadow@2x.png")
    copy(os.path.join(CHAP, "ChapterCardFrameDefault.png"), "ChapFrameDefault@2x.png")
    copy(os.path.join(CHAP, "ChapterCardFrameSelected.png"), "ChapFrameSelected@2x.png")
    copy(os.path.join(CHAP, "ChapterCardFrameLocked.png"), "ChapFrameLocked@2x.png")
    copy(os.path.join(CLIENT, "Game", "Interface", "Common", "ContainerFullPatch@2x.png"), "ChapSlot@2x.png")
    copy(os.path.join(CLIENT, "Shared", "UI", "Textures", "Decorations", "DividerHorizontal.png"), "ChapDivider.png")
    for src, name in (("ChapterEmblemFrame.png", "ChapEmblemFrame@2x.png"), ("GoblinEmblemBackground.png", "ChapEmblemBack@2x.png")):
        w, h, img = read_any(os.path.join(CHAP, src))
        fade_bottom(img)
        save(name, img)


# ==================== CADRES TEINTES PAR TYPE DE QUETE (2026-11-13) ====================

# Teintes des cadres (QuestChaptersPage.tint) : principale ou rang S = or, secondaire = bleu, journaliere = vert,
# exclusive = violet. Teinte (degres), saturation minimale du cadre au repos, eclaircissement.
TINTS = {"Or": (40, 0.62, 1.12), "Bleu": (212, 0.55, 1.10), "Vert": (128, 0.50, 1.08), "Violet": (276, 0.52, 1.12),
         "Rouge": (2, 0.62, 1.10),                                    # contrats de prime (2026-11-13)
         "Orange": (26, 0.66, 1.12),                                  # contrats de monstres (2026-11-13)
         "Cyan": (188, 0.55, 1.10),                                   # quetes de classe (2026-11-16)
         "Rose": (326, 0.55, 1.12)}                                   # quetes et missions de guilde (2026-11-19)


def tinted_frames():
    """Cadres du client recolores : la teinte de chaque pixel colore devient celle du type, sa luminosite est gardee
    (relief et biseaux intacts) ; le cadre choisi garde sa saturation, le cadre au repos est ravive."""
    import colorsys
    for src, base, selected in (("ChapterCardFrameDefault.png", "ChapFrameDefault", False),
                                ("ChapterCardFrameSelected.png", "ChapFrameSelected", True)):
        w, h, px = read_any(os.path.join(CHAP, src))
        for name, (hue, smin, light) in TINTS.items():
            img = [[list(p) for p in row] for row in px]
            for row in img:
                for p in row:
                    if p[3] == 0: continue
                    hh, l, sat = colorsys.rgb_to_hls(p[0] / 255, p[1] / 255, p[2] / 255)
                    if sat < 0.06 and not selected: continue                 # gris neutres : inchanges
                    sat = sat if selected else max(sat, smin) * min(1.0, l * 3)   # ombres profondes moins saturees
                    l = min(1.0, l * (1.0 if selected else light))
                    r, g, b = colorsys.hls_to_rgb(hue / 360, l, min(1.0, sat))
                    p[0], p[1], p[2] = round(r * 255), round(g * 255), round(b * 255)
            save(f"{base}{name}@2x.png", img)
    # eclats autour de la carte choisie, de la couleur du contour (2026-11-13) : ChapShardNN<Teinte>.png
    for n in range(1, 17):
        w, h, px = read_any(os.path.join(CHAP, "ChapterCardShard%02d.png" % n))
        for name, (hue, _smin, _light) in TINTS.items():
            img = [[list(p) for p in row] for row in px]
            for row in img:
                for p in row:
                    if p[3] == 0: continue
                    hh, l, sat = colorsys.rgb_to_hls(p[0] / 255, p[1] / 255, p[2] / 255)
                    r, g, b = colorsys.hls_to_rgb(hue / 360, l, sat)
                    p[0], p[1], p[2] = round(r * 255), round(g * 255), round(b * 255)
            save("ChapShard%02d%s.png" % (n, name), img)


# ==================== SEPARATEUR (bande d'encre) ====================

def separator():
    """Bande de 504x1080 (ChaptersTokens : Separator.Panel, Divider, InnerDivider) ; le voile a droite est un Group."""
    W, H = 504, 1080
    img = blank(W, H)
    panel = [(259.5, 892.5), (323, 1084.5), (1481.5, 1089.5), (1481.5, -4.5), (58.5, -4.5), (160.5, 279.5), (101.5, 314.5), (101.5, 806.5)]
    fill_poly(img, panel, hexc("181822"), 0x80 / 255)

    def grad(stops, a, b):
        ax, ay = a; bx, by = b
        L = (bx - ax) ** 2 + (by - ay) ** 2

        def f(x, y):
            t = ((x - ax) * (bx - ax) + (y - ay) * (by - ay)) / L
            for i in range(len(stops) - 1):
                (o0, c0), (o1, c1) = stops[i], stops[i + 1]
                if t <= o1 or i == len(stops) - 2:
                    return mix(hexc(c0), hexc(c1), (t - o0) / (o1 - o0) if o1 > o0 else 0)
            return hexc(stops[-1][1])
        return f
    curve = [(257.456 + (318.5 - 257.456) * k / 6, 893 + (1078 - 893) * k / 6) for k in range(7)]
    divider = [(58.5, -4), (157.5, 278.5), (99.195, 313.5), (99.195, 806.5)] + curve
    stroke(img, divider, 4, grad([(0.0, "16212F"), (0.1346, "16212F"), (0.3798, "48455C"), (0.7163, "48455C"), (0.9711, "16212F"), (1.0, "16212F")],
                                 (58.5, 6.5), (358.5, 1076)))
    inner = [(278.5, 996), (249.5, 906.13), (88.195, 813.001), (88.195, 302), (133, 273.045), (104, 173)]
    stroke(img, inner, 4, grad([(0.0, "16212F"), (0.4991, "48455C"), (1.0, "16212F")], (78, 173), (262, 1011.5)), alpha=0.3)
    save("ChapSeparator.png", img)


# ==================== PETITES IMAGES ====================

def diamond(size, fill_top, fill_bot, border, bw, inner=None, check=None):
    S = size * 2
    img = blank(S, S)
    c = S / 2
    r = S * 0.42
    outer = [(c, c - r), (c + r, c), (c, c + r), (c - r, c)]
    fill_poly(img, outer, hexc(border))
    ri = r - bw * 2 * math.sqrt(2)
    for y in range(S):
        for x in range(S):
            if abs(x + 0.5 - c) + abs(y + 0.5 - c) <= ri:
                p = img[y][x]
                col = inner if inner else mix(hexc(fill_top), hexc(fill_bot), y / S)
                p[0], p[1], p[2], p[3] = col[0], col[1], col[2], 255
    if check:
        k = S / 27.0
        stroke(img, [(8.5 * k, 13.5 * k), (12 * k, 17 * k), (19 * k, 9.5 * k)], 2.6 * k, lambda x, y: hexc(check))
    return img


def lock(size, col_top="E7EBF0", col_bot="B2BBD2"):
    S = size * 2
    img = blank(S, S)
    f = lambda x, y: mix(hexc(col_top), hexc(col_bot), y / S)
    # anse
    cx, cy, rr = S * 0.5, S * 0.42, S * 0.22
    arc = [(cx + rr * math.cos(math.pi + math.pi * k / 16), cy + rr * math.sin(math.pi + math.pi * k / 16)) for k in range(17)]
    stroke(img, [(cx - rr, S * 0.5)] + arc + [(cx + rr, S * 0.5)], S * 0.09, f)
    # corps
    body = [(S * 0.18, S * 0.47), (S * 0.82, S * 0.47), (S * 0.82, S * 0.92), (S * 0.18, S * 0.92)]
    for y in range(S):
        for x in range(S):
            if S * 0.18 <= x < S * 0.82 and S * 0.47 <= y < S * 0.92:
                p = img[y][x]; col = f(x, y)
                p[0], p[1], p[2], p[3] = col[0], col[1], col[2], 255
    # trou de serrure
    for y in range(S):
        for x in range(S):
            if math.hypot(x - S * 0.5, y - S * 0.64) < S * 0.06 or (abs(x - S * 0.5) < S * 0.025 and S * 0.64 <= y < S * 0.8):
                img[y][x] = [14, 20, 30, 255]
    del body
    return img


def chevron(w, h, top, bot, flip=False):
    W, H = w * 2, h * 2
    img = blank(W, H)
    sx, sy = W / 12.0, H / 24.0
    pts = [(12, 12), (0, 0), (0, 8), (4, 12), (0, 16), (0, 24)]
    pts = [((12 - x) if flip else x, y) for x, y in pts]
    tmp = blank(W, H)
    fill_poly(tmp, [(x * sx, y * sy) for x, y in pts], [255, 255, 255])
    for y in range(H):
        for x in range(W):
            a = tmp[y][x][3]
            if a:
                t = (x / W) if not flip else 1 - x / W
                col = mix(hexc(top), hexc(bot), t)
                img[y][x] = [col[0], col[1], col[2], a]
    return img


def small_images():
    save("ChapBullet@2x.png", diamond(24, "E7EBF0", "B2BBD2", "58606B", 1))
    save("ChapCheckOff@2x.png", diamond(22, "000000", "000000", "58606B", 1, inner=hexc("0C151E")))
    save("ChapCheckOn@2x.png", diamond(22, "FFE98A", "DAA444", "9D6427", 1))
    save("ChapRewardCheck@2x.png", diamond(27, "FFE98A", "DAA444", "C58643", 1.2, inner=hexc("0C151E"), check="FFE98A"))
    save("ChapLock@2x.png", lock(56, "FF9A8A", "C9302C"))                  # quetes verrouillees en rouge (2026-11-13)
    save("ChapRankBadge@2x.png", diamond(BADGE, "000000", "000000", "C58643", 1.6, inner=hexc("0C151E")))
    save("ChapArrowRight@2x.png", chevron(20, 40, "8590AD", "BCC2CB"))
    save("ChapArrowLeft@2x.png", chevron(20, 40, "8590AD", "BCC2CB", flip=True))
    save("ChapRewArrowRight@2x.png", chevron(12, 24, "B2BBD2", "E7EBF0"))
    save("ChapRewArrowLeft@2x.png", chevron(12, 24, "B2BBD2", "E7EBF0", flip=True))


# ==================== ILLUSTRATIONS DES ACTES ====================

AW, AH = 334, 604   # taille de l'illustration du client (ChapterArtGoblin.png)


def art_mask():
    """Silhouette de l'illustration du client (alpha de ChapterArtGoblin.png) : les cartes ont la meme decoupe."""
    w, h, px = read_any(os.path.join(CHAP, "ChapterArtGoblin.png"))
    assert (w, h) == (AW, AH)
    return [[px[y][x][3] for x in range(w)] for y in range(h)]


def vgrad(stops, v):
    for i in range(len(stops) - 1):
        (o0, c0), (o1, c1) = stops[i], stops[i + 1]
        if v <= o1:
            return mix(hexc(c0), hexc(c1), (v - o0) / (o1 - o0))
    return hexc(stops[-1][1])


def ridge(u, base, amp, f1, f2, ph):
    return base + amp * (0.6 * math.sin(u * f1 + ph) + 0.4 * math.sin(u * f2 + ph * 1.7))


def scene_aube(u, v, rnd):
    col = vgrad([(0, "2b2350"), (0.22, "6b3f6e"), (0.42, "e2875a"), (0.56, "ffd98a"), (1, "ffd98a")], v)
    d = math.hypot((u - 0.5) * AW / AH, v - 0.40)
    if d < 0.105: col = mix(hexc("fff4c4"), hexc("ffe08a"), d / 0.105)
    elif d < 0.2: col = mix(col, hexc("ffe9a8"), (0.2 - d) / 0.095 * 0.55)
    if v > ridge(u, 0.50, 0.035, 9, 17, 0.4): col = mix(hexc("8a5c86"), hexc("6e4a75"), (v - 0.5) * 4)
    if v > ridge(u, 0.575, 0.03, 6, 13, 2.1): col = hexc("4c3a63")
    # torii au loin
    if 0.62 > v > 0.475:
        if (0.355 < u < 0.385 or 0.615 < u < 0.645) and v > 0.50: col = hexc("b8322c")
        if 0.474 < v < 0.492 and 0.31 < u < 0.69 + 0.0: col = hexc("c63a30") if v > 0.478 else hexc("2a1a22")
        if 0.515 < v < 0.528 and 0.34 < u < 0.66: col = hexc("a72c27")
    if v > ridge(u, 0.655, 0.02, 4, 11, 1.0): col = mix(hexc("2f4a2c"), hexc("1c2b1d"), (v - 0.65) * 3)
    # bambous
    for bu, bw, sh in ((0.06, 0.05, "6f9d3e"), (0.17, 0.04, "5a8a33"), (0.86, 0.05, "6f9d3e"), (0.95, 0.04, "5a8a33")):
        if abs(u - bu) < bw / 2:
            node = (v * 9 + bu * 7) % 1.0 < 0.035
            edge = abs(u - bu) > bw / 2 * 0.7
            col = hexc("33521f") if node else mix(hexc(sh), hexc("2d4a1c"), 0.5 if edge else 0)
    # tas de buches
    for cx, cy, r in ((0.30, 0.90, 0.075), (0.47, 0.905, 0.08), (0.64, 0.90, 0.075), (0.385, 0.83, 0.075), (0.555, 0.83, 0.075), (0.47, 0.765, 0.07)):
        dd = math.hypot((u - cx) * AW / AH, v - cy) / (r * AW / AH)
        if dd < 1:
            ring = (dd * 4.2) % 1.0
            col = hexc("4a2a16") if dd > 0.86 else (hexc("b27a43") if ring > 0.25 else hexc("d39a5b"))
    return col


def scene_nuit(u, v, rnd):
    col = vgrad([(0, "070b1d"), (0.45, "18234d"), (0.7, "24346a"), (1, "24346a")], v)
    if v < 0.6 and rnd(int(u * AW), int(v * AH)) > 0.996: col = hexc("e8eeff")
    d = math.hypot((u - 0.66) * AW / AH, v - 0.2)
    if d < 0.085:
        col = hexc("e9efff")
        for cx, cy, r in ((0.64, 0.19, 0.02), (0.69, 0.22, 0.014), (0.655, 0.235, 0.01)):
            if math.hypot((u - cx) * AW / AH, v - cy) < r: col = hexc("c7d0ea")
    elif d < 0.16: col = mix(col, hexc("8fa6e0"), (0.16 - d) / 0.075 * 0.4)
    if v > ridge(u, 0.6, 0.04, 7, 15, 0.8): col = hexc("131b36")
    # arbre mort
    tx = 0.2
    if v > 0.32 and abs(u - tx - (v - 0.32) * 0.05) < 0.018 * (0.5 + v): col = hexc("070910")
    for (x0, y0, x1, y1, wd) in ((0.2, 0.45, 0.04, 0.30, 0.012), (0.21, 0.4, 0.36, 0.26, 0.01), (0.3, 0.33, 0.4, 0.31, 0.006),
                                 (0.12, 0.38, 0.08, 0.27, 0.007)):
        if seg_dist(u, v, x0, y0, x1, y1) < wd: col = hexc("070910")
    # tombes
    for cx, top, wd in ((0.78, 0.66, 0.07), (0.9, 0.69, 0.05), (0.62, 0.7, 0.05)):
        if abs(u - cx) < wd / 2 and v > top + 0.02: col = hexc("3a4660")
        if math.hypot((u - cx) * AW / AH, v - top - 0.02) < wd / 2 * AW / AH and v <= top + 0.02: col = hexc("3a4660")
    if v > ridge(u, 0.74, 0.012, 5, 13, 3.0): col = hexc("0c1224")
    fog = math.exp(-((v - 0.78) / 0.05) ** 2) * (0.55 + 0.25 * math.sin(u * 13))
    col = mix(col, hexc("4fae7c"), fog * 0.45)
    # crane
    sx, sy = (u - 0.47) * AW / AH, v - 0.86
    if (sx / 0.13) ** 2 + (sy / 0.09) ** 2 < 1 or (abs(sx) < 0.07 and 0 < sy < 0.13):
        col = hexc("ddd6c1")
        if abs(sx) < 0.07 and sy > 0.09 and (int((sx + 0.07) / 0.0175) % 2 == 0): col = hexc("2a2620")
        for ex in (-0.05, 0.05):
            de = math.hypot(sx - ex, (sy - 0.01) * 1.2)
            if de < 0.035: col = hexc("7dffb0") if de < 0.012 else hexc("12100e")
        if abs(sx) < 0.012 and 0.045 < sy < 0.075: col = hexc("12100e")
    return col


def scene_duel(u, v, rnd):
    col = vgrad([(0, "220c1c"), (0.3, "6b1c2c"), (0.5, "c43a36"), (0.62, "f08a4b"), (1, "f08a4b")], v)
    d = math.hypot((u - 0.5) * AW / AH, v - 0.45)
    if d < 0.21: col = mix(hexc("ffd98a"), hexc("ff7a45"), (d / 0.21) ** 2)
    # toit du dojo (silhouette en haut)
    roof = 0.13 + 0.05 * ((u - 0.5) * 2) ** 2
    if v < roof: col = hexc("150810")
    if abs(v - roof) < 0.006: col = hexc("3a1418")
    if v > ridge(u, 0.6, 0.02, 5, 12, 0.3): col = mix(hexc("5a1e2a"), hexc("3a1220"), (v - 0.6) * 3)
    if v > 0.78: col = hexc("1d0b14")
    # deux bretteurs face a face
    for cx, s in ((0.27, 1), (0.73, -1)):
        x = (u - cx) * s
        if math.hypot(x * AW / AH, v - 0.555) < 0.028: col = hexc("120610")
        if 0.58 < v < 0.7 and abs(x) < 0.035 + (v - 0.58) * 0.35: col = hexc("120610")
        if 0.69 < v < 0.79 and (abs(x - 0.035) < 0.014 or abs(x + 0.035) < 0.014): col = hexc("120610")
        if seg_dist(x, v, 0.02, 0.63, 0.2, 0.5) < 0.006: col = hexc("e7e3dc")
    # petales
    if v > roof + 0.02 and rnd(int(u * 16), int(v * 30)) > 0.86:
        fx, fy = (u * 16) % 1.0, (v * 30) % 1.0
        if ((fx - 0.5) * 1.6) ** 2 + ((fy - 0.5) * 2.2) ** 2 < 0.05: col = mix(hexc("ffb3c7"), hexc("ff8fae"), fy)
    return col


def inpoly(x, y, pts):
    """Point (x, y) dans le polygone pts (pair-impair)."""
    inside = False
    for i in range(len(pts)):
        (ax, ay), (bx, by) = pts[i], pts[i - 1]
        if (ay > y) != (by > y) and x < ax + (y - ay) * (bx - ax) / (by - ay):
            inside = not inside
    return inside


def rot(pts, cx, cy, deg):
    c, s = math.cos(math.radians(deg)), math.sin(math.radians(deg))
    return [(cx + (x - cx) * c - (y - cy) * s, cy + (x - cx) * s + (y - cy) * c) for x, y in pts]


# couronne tombee sur les marches (inclinee), en px de l'illustration
_CROWN = rot([(196, 382), (196, 360), (203, 370), (210, 354), (217, 370), (224, 360), (224, 382)], 210, 372, 18)
_CROWN_BAND = rot([(196, 382), (196, 375), (224, 375), (224, 382)], 210, 372, 18)
_CROWN_GEMS = [(rot([(203, 378)], 210, 372, 18)[0], "c9302c"), (rot([(210, 378)], 210, 372, 18)[0], "4fd08a"),
               (rot([(217, 378)], 210, 372, 18)[0], "c9302c")]


def scene_trone(u, v, rnd):
    """Quete principale « L'Epopee d'un Roi » (2026-11-14) : la salle du trone du Roi Fou, la nuit. Trone vide a contre-jour
    devant une grande fenetre gothique, fantome vert du roi assis, couronne tombee sur les marches, bannieres dechirees,
    tapis rouge et morts-vivants qui montent vers le trone dans la brume."""
    x, y = u * AW, v * AH
    # mur de pierre
    col = vgrad([(0, "0b0c1a"), (0.5, "191b31"), (1, "141527")], v)
    if (int(y / 22) % 2 == 0 and abs((x + 0) % 44 - 22) < 1) or (int(y / 22) % 2 == 1 and abs(x % 44) < 1) or y % 22 < 1:
        col = mix(col, hexc("07070f"), 0.6)
    # fenetre gothique (arc brise equilateral), source de lumiere
    wl, wr, spring, wbot = 100, 234, 150, 262
    span = wr - wl

    def in_arch(m):
        if not (wl - m <= x <= wr + m and y <= wbot + m): return False
        if y >= spring: return True
        return math.hypot(x - (wl - m), y - spring) <= span + 2 * m and math.hypot(x - (wr + m), y - spring) <= span + 2 * m
    if in_arch(7): col = hexc("4a4e6e") if not in_arch(4) else hexc("2e3150")
    if in_arch(0):
        col = vgrad([(0, "0a1238"), (0.35, "1d2f72"), (0.45, "33498f")], v)
        if rnd(int(x / 2), int(y / 2)) > 0.985: col = hexc("e8eeff")
        d = math.hypot(x - 200, y - 112)
        if d < 24:
            col = hexc("eef2ff")
            for cx, cy, r in ((193, 106, 6), (206, 119, 4), (203, 104, 3)):
                if math.hypot(x - cx, y - cy) < r: col = hexc("c7d0ea")
        elif d < 46: col = mix(col, hexc("9fb3ee"), (46 - d) / 22 * 0.45)
        if abs(x - 167) < 2.5 or abs(y - 196) < 2.5: col = hexc("12142a")            # meneau et traverse
        if seg_dist(x, y, 118, 220, 150, 250) < 1.2 or seg_dist(x, y, 130, 232, 112, 258) < 1.0: col = hexc("12142a")  # vitre fendue
    # piliers et chapiteaux
    for p0, p1 in ((0, 38), (296, 334)):
        if p0 <= x <= p1:
            edge = (x - p0) / (p1 - p0)
            col = mix(hexc("2b2d48"), hexc("15162a"), edge if p0 == 0 else 1 - edge)
            if 44 < y < 60: col = hexc("3a3d5c")
    # bannieres dechirees (rouge et or, couronne)
    for b0, b1 in ((48, 88), (246, 286)):
        bottom = 214 + 10 * math.sin(x * 0.55) - (16 if abs(x - (b0 + b1) / 2) < 6 else 0)
        if b0 <= x <= b1 and 34 <= y <= bottom:
            col = hexc("6e1622") if b0 + 4 < x < b1 - 4 else hexc("4a0e18")
            if abs(y - 40) < 3: col = hexc("c58643")
            cx = (b0 + b1) / 2
            if inpoly(x, y, [(cx - 11, 128), (cx - 11, 112), (cx - 5, 119), (cx, 108), (cx + 5, 119), (cx + 11, 112), (cx + 11, 128)]):
                col = hexc("e0b050")
        if b0 + 18 <= x <= b0 + 22 and 26 <= y <= 34: col = hexc("2a1a10")
    # sol dalle en perspective
    if y > 410:
        col = mix(hexc("1c1e33"), hexc("0f1020"), (y - 410) / 194)
        t = (y - 410) / 194
        if abs((math.sqrt(t) * 6) % 1.0) < 0.06: col = hexc("0a0b16")
        if abs(((x - 167) / (y - 300) * 4) % 1.0) < 0.05: col = hexc("0a0b16")
    # faisceau de lune depuis la fenetre jusqu'au trone
    if y > wbot:
        k = (y - wbot) / (604 - wbot)
        if wl - 40 * k < x < wr + 40 * k:
            col = mix(col, hexc("8fa6e0"), 0.16 * (1 - k * 0.6))
    # marches
    for s0, s1, x0, x1 in ((345, 367, 110, 224), (367, 389, 92, 242), (389, 411, 74, 260)):
        if s0 <= y < s1 and x0 <= x <= x1:
            col = hexc("4a4d6c") if y < s0 + 4 else hexc("2c2e48")
    # tapis rouge (marches et sol)
    if y >= 345:
        k = max(0.0, (y - 411) / 193)
        half = 22 if y < 411 else 27 + 30 * k
        if abs(x - 167) < half:
            col = hexc("7a1a26") if abs(x - 167) < half - 4 else hexc("c58643")
            if y < 411 and (y - 345) % 22 < 4: col = hexc("9a2a36") if abs(x - 167) < half - 4 else hexc("e0b050")
    # trone : dossier a fleches, accoudoirs, siege, pieds
    throne = [(138, 305), (138, 232), (142, 205), (150, 232), (167, 178), (184, 232), (192, 205), (196, 232), (196, 305)]
    if inpoly(x, y, throne): col = hexc("c58643")
    if inpoly(x, y, [(141, 302), (141, 236), (142, 214), (147, 236), (167, 186), (187, 236), (192, 214), (193, 236), (193, 302)]):
        col = hexc("1a1024")
    if 148 <= x <= 186 and 244 <= y <= 300: col = hexc("4a1422") if (x + y) % 9 > 1 else hexc("3a0e1a")
    if (126 <= x <= 140 or 194 <= x <= 208) and 282 <= y <= 302: col = hexc("1a1024") if y > 285 else hexc("c58643")
    for kx in (133, 201):
        if math.hypot(x - kx, y - 281) < 5: col = hexc("e0b050")
    if 130 <= x <= 204 and 300 <= y <= 316: col = hexc("c58643") if y < 303 else hexc("6b1a2a")
    if (136 <= x <= 144 or 190 <= x <= 198) and 316 <= y <= 345: col = hexc("1a1024")
    # fantome du Roi Fou, assis sur le trone
    gh = math.hypot(x - 167, y - 262)
    torso = inpoly(x, y, [(152, 276), (182, 276), (192, 318), (142, 318)])
    if gh < 13 or torso:
        col = mix(col, hexc("7dffb0"), 0.32)
        for ex in (162, 172):
            if math.hypot(x - ex, y - 261) < 2.2: col = hexc("e6fff0")
    elif gh < 20 or inpoly(x, y, [(146, 272), (188, 272), (198, 322), (136, 322)]):
        col = mix(col, hexc("7dffb0"), 0.12)
    # couronne tombee
    if inpoly(x, y, _CROWN):
        col = hexc("f2c45e") if inpoly(x, y, _CROWN_BAND) or (x + y) % 7 > 1 else hexc("c58643")
        for (gx, gy), gc in _CROWN_GEMS:
            if math.hypot(x - gx, y - gy) < 2.4: col = hexc(gc)
    # brume verte au ras du sol
    fog = math.exp(-((y - 500) / 45) ** 2) * (0.55 + 0.3 * math.sin(x * 0.05 + y * 0.02))
    col = mix(col, hexc("4fae7c"), fog * 0.35)
    # morts-vivants qui montent vers le trone (un de chaque cote)
    for cx, top, s in ((50, 432, 1), (286, 446, -1)):
        dx = (x - cx) * s
        if _zombie(dx, y, top):
            col = hexc("0d1712")
            if not _zombie(dx + 2.5, y, top) or not _zombie(dx, y - 2.5, top): col = hexc("2f6a44")   # liseré cote trone
            for ex in (5, 12):
                if math.hypot(dx - ex, y - top + 1) < 2.2: col = hexc("9dff7a")
    return col


def _zombie(dx, y, top):
    """Silhouette d'un mort-vivant voute, bras tendus vers dx > 0 (le trone), haillons et jambes ecartees."""
    if math.hypot(dx - 6, y - top) < 12: return True                                            # tete penchee en avant
    if y > top + 96:
        if inpoly(dx, y, [(-14, top + 96), (-2, top + 96), (-6, 604), (-20, 604)]): return True   # jambe arriere
        if inpoly(dx, y, [(2, top + 96), (14, top + 96), (22, 604), (8, 604)]): return True       # jambe avant
    rag = top + 100 + 6 * math.sin(dx * 0.9)
    if y < rag and inpoly(dx, y, [(-16, top + 12), (14, top + 12), (20, top + 60), (16, top + 108), (-18, top + 108), (-20, top + 58)]):
        return True                                                                           # torse en haillons
    for pts, w in ((((10, top + 20), (34, top + 30), (58, top + 24)), 5.0), (((4, top + 30), (30, top + 46), (54, top + 44)), 4.5)):
        if any(seg_dist(dx, y, *pts[i], *pts[i + 1]) < w for i in range(2)): return True        # bras tendus
        hx, hy = pts[2]
        for fx, fy in ((9, -3), (10, 1), (8, 5)):
            if seg_dist(dx, y, hx, hy, hx + fx, hy + fy) < 1.6: return True                    # doigts
    return False


# ---------- personnages (silhouettes reutilisables) ----------

def _person(x, y, cx, feet, s, hand, tip=None, hood=False, beard=False, wide=False):
    """Combattant de face, pieds en (cx, feet), echelle s (~150*s px de haut), arme tenue a deux mains en hand -> tip.
    Renvoie la partie touchee ('weapon', 'sleeve', 'skin', 'hair', 'hood', 'face', 'beard', 'gi', 'inner', 'belt',
    'hakama') ou None ; la scene choisit les couleurs."""
    xs = [cx - 40 * s, cx + 40 * s, hand[0]] + ([tip[0]] if tip else [])
    ys = [feet - 175 * s, feet + 1, hand[1]] + ([tip[1]] if tip else [])
    if not (min(xs) - 8 <= x <= max(xs) + 8 and min(ys) - 8 <= y <= max(ys) + 8): return None
    hy, sh = feet - 140 * s, feet - 120 * s
    if tip and seg_dist(x, y, hand[0], hand[1], tip[0], tip[1]) < 2.6 * s: return 'weapon'
    if math.hypot(x - hand[0], y - hand[1]) < 5 * s: return 'skin'
    if hood:
        if math.hypot(x - cx, y - hy) < 13 * s or inpoly(x, y, [(cx - 13 * s, hy), (cx + 3 * s, hy - 28 * s), (cx + 13 * s, hy)]):
            return 'face' if math.hypot(x - cx, y - hy - 2 * s) < 7.5 * s else 'hood'
    else:
        if math.hypot(x - cx, y - hy) < 11 * s:
            if beard and y > hy + 3 * s: return 'beard'
            return 'hair' if y < hy - 3 * s else 'skin'
        if math.hypot(x - cx, y - (hy - 13 * s)) < 5 * s: return 'hair'                         # chignon
    for sx in (cx - 15 * s, cx + 15 * s):                                                      # bras derriere la tete
        if seg_dist(x, y, sx, sh, hand[0], hand[1]) < 6 * s: return 'sleeve'
    if beard and inpoly(x, y, [(cx - 9 * s, hy + 4 * s), (cx + 9 * s, hy + 4 * s), (cx, hy + 36 * s)]): return 'beard'
    if inpoly(x, y, [(cx - 17 * s, feet - 128 * s), (cx + 17 * s, feet - 128 * s), (cx + 21 * s, feet - 62 * s), (cx - 21 * s, feet - 62 * s)]):
        if feet - 82 * s < y < feet - 74 * s: return 'belt'
        if abs(x - cx) < (y - (feet - 128 * s)) * 0.35 and y < feet - 106 * s: return 'inner'
        return 'gi'
    w = 31 if wide else 26
    if inpoly(x, y, [(cx - 21 * s, feet - 62 * s), (cx + 21 * s, feet - 62 * s), (cx + w * s, feet), (cx + 4 * s, feet),
                     (cx, feet - 30 * s), (cx - 4 * s, feet), (cx - w * s, feet)]):
        return 'hakama'
    return None


def _paint(col, part, pal):
    return hexc(pal[part]) if part and part in pal else col


def scene_dojo(u, v, rnd):
    """Epreuves du Dojo 1 « Les Disciples du Dojo » (2026-11-14) : dojo en plein jour, cloisons de papier, rouleau
    calligraphie, trois disciples en gi blanc a l'entrainement au Bokken."""
    x, y = u * AW, v * AH
    col = vgrad([(0, "f5e6c4"), (0.6, "e3c690")], v)                                   # cloisons shoji
    if (x % 42 < 3 or y % 56 < 3) and y < 380: col = hexc("5a3a22")
    if y < 36: col = hexc("3e2614") if y > 20 else hexc("2a180c")                        # poutre
    if x < 14 or x > 320: col = hexc("4a2c18")                                           # piliers
    if (x * 0.55 + y) % 120 < 38 and y < 380: col = mix(col, hexc("fff4d6"), 0.18)       # rais de soleil
    # rouleau calligraphie
    if 128 <= x <= 206 and 54 <= y <= 221:
        col = hexc("2a1a10") if (y < 60 or y > 215) else (hexc("6b4a2a") if (x < 133 or x > 201) else hexc("efe6cf"))
        for a, b in (((150, 90), (184, 90)), ((167, 78), (167, 150)), ((148, 120), (186, 120)), ((158, 150), (144, 186)),
                     ((176, 150), (190, 186)), ((158, 166), (176, 166))):
            if seg_dist(x, y, *a, *b) < 3.2: col = hexc("15100c")
        if 178 <= x <= 190 and 190 <= y <= 202: col = hexc("b8322c")
    # ratelier d'armes (a gauche)
    if 22 <= x <= 92 and 250 <= y <= 262: col = hexc("4a2c18")
    for k in range(5):
        if seg_dist(x, y, 30 + k * 14, 232, 34 + k * 14, 360) < 2.4: col = hexc("8a5a2b")
    # tatamis
    if y >= 380:
        col = mix(hexc("d6cb92"), hexc("b9ad70"), (y - 380) / 224)
        if int(x / 3) % 2 == 0: col = mix(col, hexc("c4b97c"), 0.4)
        for ly in (380, 418, 476, 560):
            if abs(y - ly) < 2: col = hexc("3b3a20")
        if abs(((x - 167) / max(1.0, y - 250) * 2.2) % 1.0) < 0.035: col = hexc("3b3a20")
    # trois disciples
    gi = {'weapon': "8a5a2b", 'skin': "e9c29a", 'sleeve': "ddd6c6", 'hair': "1c1410", 'gi': "ece6d8", 'inner': "c9c2b2",
          'belt': "6b4a2a", 'hakama': "2b2f45"}
    for cx, feet, s, hand, tip in ((70, 400, 0.85, (88, 318), (122, 258)), (264, 400, 0.85, (246, 318), (212, 258)),
                                   (167, 470, 1.25, (180, 282), (110, 214))):
        if math.hypot((x - cx) / 2.6, y - feet) < 9 * s: col = mix(col, hexc("6b5a30"), 0.5)   # ombre au sol
        col = _paint(col, _person(x, y, cx, feet, s, hand, tip), gi)
    return col


def _deer(x, y):
    """Cerf en plein bond (tete a droite) : partie touchee ou None."""
    if not (70 <= x <= 230 and 200 <= y <= 345): return None
    if math.hypot(x - 208, y - 250) < 1.8: return 'eye'
    if math.hypot(x - 112, y - 298) < 5: return 'tail'
    if any(seg_dist(x, y, *a, *b) < 1.9 for a, b in (((200, 247), (196, 222)), ((198, 234), (186, 226)), ((196, 222), (204, 210)),
                                                     ((207, 247), (214, 224)), ((212, 232), (224, 226)), ((214, 224), (210, 210)))):
        return 'antler'
    body = rot([(112, 300), (122, 286), (160, 282), (186, 288), (192, 302), (176, 314), (126, 314)], 152, 300, -10)
    if (inpoly(x, y, body) or seg_dist(x, y, 182, 290, 198, 258) < 7 or ((x - 205) / 11) ** 2 + ((y - 252) / 6.5) ** 2 < 1
            or seg_dist(x, y, 196, 248, 192, 238) < 2.5
            or any(seg_dist(x, y, *a, *b) < 3.4 for a, b in (((176, 306), (200, 322)), ((200, 322), (222, 316)),
                                                             ((170, 308), (192, 330)), ((192, 330), (214, 330)),
                                                             ((124, 306), (100, 318)), ((100, 318), (78, 330)),
                                                             ((130, 308), (110, 326)), ((110, 326), (90, 340))))):
        return 'belly' if y > 306 - (x - 150) * 0.12 and 118 < x < 186 else 'body'
    return None


def scene_vent(u, v, rnd):
    """Epreuves du Dojo 2 « La Chasse du Vent » : plaines dorees balayees par le vent, cerf en plein bond, lievre et
    renard qui detalent, herbes couchees."""
    x, y = u * AW, v * AH
    col = vgrad([(0, "7fbbe6"), (0.42, "f6e7b0"), (1, "f6e7b0")], v)
    d = math.hypot(x - 250, y - 120)
    if d < 30: col = hexc("fff6d0")
    elif d < 80: col = mix(col, hexc("fff2c0"), (80 - d) / 50 * 0.6)
    for cx, cy, rx, ry in ((70, 80, 46, 12), (120, 70, 30, 10), (220, 190, 40, 9)):
        if ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2 < 1: col = mix(col, hexc("ffffff"), 0.7)
    if y > ridge(u, 330, 14, 7, 15, 0.4): col = hexc("9fb86a")
    if y > ridge(u, 372, 10, 5, 11, 2.0): col = hexc("6f9a45")
    if y > ridge(u, 410, 6, 4, 9, 1.0): col = mix(hexc("5a8a36"), hexc("3f6a26"), (y - 410) / 194)
    # ombre du cerf au sol
    if math.hypot((x - 160) / 3.2, y - 430) < 9: col = mix(col, hexc("2f4f1c"), 0.45)
    # cerf en plein bond
    part = _deer(x, y)
    if part:
        col = hexc({'body': "7a4626", 'belly': "e6c49a", 'antler': "efe4c8", 'eye': "120a06", 'tail': "f5ecd8"}[part])
        if part in ('body', 'belly') and not (_deer(x + 2, y) and _deer(x - 2, y) and _deer(x, y + 2) and _deer(x, y - 2)):
            col = hexc("2a160a")
    # lievre
    if (((x - 68) / 14) ** 2 + ((y - 398) / 8.5) ** 2 < 1 or math.hypot(x - 83, y - 392) < 6
            or seg_dist(x, y, 82, 387, 72, 370) < 2.4 or seg_dist(x, y, 86, 387, 80, 368) < 2.4
            or seg_dist(x, y, 58, 402, 46, 410) < 2.6 or seg_dist(x, y, 78, 404, 90, 410) < 2.2):
        col = hexc("b8a07a")
    # renard au loin
    if (((x - 268) / 15) ** 2 + ((y - 366) / 5.5) ** 2 < 1 or ((x - 248) / 10) ** 2 + ((y - 362) / 4.5) ** 2 < 1
            or inpoly(x, y, [(280, 360), (294, 362), (282, 370)]) or inpoly(x, y, [(284, 358), (288, 352), (290, 360)])):
        col = hexc("d06a2a") if not ((x - 244) ** 2 / 16 + (y - 362) ** 2 / 4 < 1) else hexc("f2e6d6")
    # herbes hautes couchees par le vent (premier plan)
    k = int(x // 5)
    hgt = 40 + 50 * rnd(k, 7)
    base = 604
    if y > base - hgt - 60:
        for kk in (k - 1, k, k + 1):
            hh = 60 + 70 * rnd(kk, 7)
            t = (base - y) / hh
            if 0 <= t <= 1:
                bx = kk * 5 + 2 + 26 * t * t
                if abs(x - bx) < 1.6 * (1 - t) + 0.5:
                    col = hexc("8fbf5a") if rnd(kk, 3) > 0.5 else hexc("4f7f2c")
    # rafales de vent
    for y0, ph, x0, x1 in ((150, 0.0, 20, 210), (215, 1.4, 120, 320), (262, 2.6, 10, 140), (345, 0.7, 150, 330)):
        if x0 < x < x1:
            cy = y0 + 9 * math.sin(x * 0.035 + ph)
            fade = math.sin((x - x0) / (x1 - x0) * math.pi)
            if abs(y - cy) < 1.3: col = mix(col, hexc("ffffff"), 0.75 * fade)
    return col


def scene_brigands(u, v, rnd):
    """Epreuves du Dojo 3 « La Bande des Collines » : camp de brigands la nuit dans les collines, feu de camp, trois
    bandits encapuchonnes eclaires par les flammes, tente et coffre pille."""
    x, y = u * AW, v * AH
    col = vgrad([(0, "05081a"), (0.5, "1b1f40"), (1, "1b1f40")], v)
    if y < 330 and rnd(int(x / 2), int(y / 2)) > 0.988: col = hexc("dfe6ff")
    if math.hypot(x - 80, y - 90) < 20 and math.hypot(x - 89, y - 83) > 17: col = hexc("f1ead2")   # croissant de lune
    if y > ridge(u, 300, 18, 6, 13, 0.5): col = hexc("10142a")
    if y > ridge(u, 345, 10, 4, 10, 2.3): col = hexc("0b0e1e")
    if y > 390: col = hexc("120c10")
    fx, fy = 180, 428
    # lueur du feu
    d = math.hypot(x - fx, (y - fy) * 1.3)
    col = mix(col, hexc("e07a30"), max(0.0, 1 - d / 170) ** 2 * 0.75)
    # tente
    if inpoly(x, y, [(30, 392), (88, 300), (146, 392)]):
        col = mix(hexc("3a2c22"), hexc("b8642a"), max(0.0, (x - 88) / 58) * 0.7)
        if inpoly(x, y, [(76, 392), (88, 340), (100, 392)]): col = hexc("0a0608")
    # coffre pille
    if 220 <= x <= 262 and 372 <= y <= 398:
        col = hexc("5a3a1e") if not (abs(y - 380) < 2 or abs(x - 241) < 2) else hexc("c58643")
    if 222 <= x <= 260 and 364 <= y <= 372 and rnd(int(x / 3), int(y / 3)) > 0.3: col = hexc("f2c45e")
    # buches et flammes
    if seg_dist(x, y, 158, 446, 202, 432) < 4.5 or seg_dist(x, y, 160, 432, 204, 446) < 4.5: col = hexc("3a2214")
    for cx, h, w in ((180, 64, 18), (168, 40, 10), (193, 46, 11)):
        t = (fy + 12 - y) / h
        if 0 <= t <= 1 and abs(x - cx - 4 * math.sin(t * 6)) < w * math.sin(math.pi * (0.15 + 0.85 * (1 - t)) / 1.0) * (1 - t * 0.6):
            col = mix(hexc("fff1a8"), hexc("e8481c"), t ** 0.7)
    # trois bandits encapuchonnes
    cloak = {'weapon': "c9ccd6", 'skin': "8a5a3a", 'sleeve': "241a1e", 'hood': "1c1418", 'face': "07050a", 'gi': "1c1418",
             'inner': "3a1a1a", 'belt': "5a3a1e", 'hakama': "15101a"}
    for cx, feet, s, hand, tip in ((100, 470, 0.95, (126, 368), (150, 344)), (268, 476, 1.0, (242, 372), (226, 336)),
                                   (184, 382, 0.62, (196, 324), (206, 300))):
        part = _person(x, y, cx, feet, s, hand, tip, hood=True)
        if part:
            c = hexc(cloak[part])
            if part not in ('face', 'weapon'):
                side = (fx - x) * (1 if cx < fx else -1)
                edge = not _person(x + (3 if cx < fx else -3), y, cx, feet, s, hand, tip, hood=True)
                if edge: c = mix(c, hexc("f08a3a"), 0.85)
                elif side > 0: c = mix(c, hexc("b8542a"), 0.12)
            if part == 'face':
                for ex in (-3.5 * s, 3.5 * s):
                    if math.hypot(x - cx - ex, y - (feet - 139 * s)) < 1.6 * s: c = hexc("ffb060")
            col = c
    return col


def _trork(x, y):
    """Silhouette du Trork (corps, tete, groin, bras, jambes)."""
    if not (140 <= x <= 320 and 200 <= y <= 500): return False
    return (((x - 238) / 64) ** 2 + ((y - 345) / 80) ** 2 < 1
            or seg_dist(x, y, 216, 410, 210, 474) < 18 or seg_dist(x, y, 262, 410, 270, 474) < 18
            or seg_dist(x, y, 186, 300, 168, 388) < 15 or seg_dist(x, y, 284, 290, 300, 222) < 15
            or math.hypot(x - 226, y - 246) < 33 or ((x - 204) / 20) ** 2 + ((y - 262) / 14) ** 2 < 1)


def scene_trork(u, v, rnd):
    """Epreuves du Dojo 4 « Le Grand Maitre et le Trork » : crepuscule sur les terres sauvages, un Trork massif a
    defenses levant sa massue face a un Grand Maitre au katana nimbe d'aura, defenses arrachees au premier plan."""
    x, y = u * AW, v * AH
    col = vgrad([(0, "2a1430"), (0.3, "7a2a3a"), (0.55, "e0703a"), (0.7, "f2b060"), (1, "f2b060")], v)
    d = math.hypot(x - 120, y - 330)
    if d < 26: col = hexc("fff0b0")
    # plateaux rocheux
    for x0, x1, top in ((0, 70, 300), (60, 150, 340), (200, 334, 315), (140, 230, 360)):
        if x0 <= x <= x1 and y >= top + 6 * math.sin(x * 0.2): col = mix(hexc("4a1e26"), hexc("2a1018"), (y - top) / 100)
    if y > 410: col = mix(hexc("5a2a22"), hexc("2a1210"), (y - 410) / 194)
    # Trork
    tx = 238
    tro = _trork(x, y)
    club = seg_dist(x, y, 300, 222, 276, 128) < 13 + (128 - min(y, 222)) * -0.05
    if club:
        col = hexc("4a2c18")
        if rnd(int(x / 6), int(y / 6)) > 0.7 and seg_dist(x, y, 300, 222, 276, 128) > 9: col = hexc("c9ccd6")
    if tro:
        col = mix(hexc("5a5442"), hexc("3a3628"), (x - 170) / 140)
        if ((x - 236) / 34) ** 2 + ((y - 362) / 40) ** 2 < 1: col = hexc("6a6250")              # ventre
        if not _trork(x - 4, y) or not _trork(x, y - 4): col = hexc("f0a060")             # liseré du couchant
        if 392 <= y <= 420 and abs(x - tx) < 60: col = hexc("5a3a22")                  # pagne
        for ex in (214, 232):
            if math.hypot(x - ex, y - 238) < 3.4: col = hexc("ff3a2a")
        if math.hypot(x - 192, y - 262) < 2.5 or math.hypot(x - 200, y - 266) < 2.5: col = hexc("1a1410")
    for pts in (((196, 270), (184, 252), (188, 232)), ((214, 274), (206, 256), (212, 238))):   # defenses
        if any(seg_dist(x, y, *pts[i], *pts[i + 1]) < 4.2 - i * 1.5 for i in range(2)): col = hexc("efe6cf")
    # Grand Maitre au katana d'aura
    cx, feet, s, hand, tip = 84, 470, 1.0, (104, 366), (150, 306)
    glow = seg_dist(x, y, *hand, *tip)
    if glow < 14 and not tro: col = mix(col, hexc("9fe8ff"), (1 - glow / 14) * 0.6)
    part = _person(x, y, cx, feet, s, hand, tip, wide=True)
    pal = {'weapon': "e8fbff", 'skin': "e2b48a", 'sleeve': "24305a", 'hair': "141018", 'gi': "2a3a6a", 'inner': "c9c2b2",
           'belt': "c58643", 'hakama': "1a2040"}
    if part:
        col = hexc(pal[part])
        if part not in ('weapon',) and not _person(x - 3, y, cx, feet, s, hand, tip, wide=True): col = mix(col, hexc("9fe8ff"), 0.7)
    # defenses arrachees au premier plan
    for cx2, cy2, a in ((150, 506, 0), (178, 512, 40), (200, 500, -30), (130, 516, 70), (215, 520, 15)):
        pts = rot([(cx2 - 14, cy2), (cx2 - 2, cy2 - 6), (cx2 + 12, cy2 - 16)], cx2, cy2, a)
        if any(seg_dist(x, y, *pts[i], *pts[i + 1]) < 3.6 - i * 1.4 for i in range(2)): col = hexc("efe6cf")
    return col


def scene_sensei(u, v, rnd):
    """Epreuves du Dojo 5 « L'Epreuve du Maitre » : le Sensei a barbe blanche, Bokken a la main, se detache devant une
    grande lune cerclee d'un enso, branche de cerisier en fleurs ; le disciple vu de dos au premier plan."""
    x, y = u * AW, v * AH
    col = vgrad([(0, "141230"), (0.4, "3b2a5a"), (0.7, "6a3a6a"), (1, "6a3a6a")], v)
    mx, my, r = 167, 205, 96
    d = math.hypot(x - mx, y - my)
    if d < r: col = mix(hexc("fbf2dc"), hexc("e8d6b8"), d / r)
    elif d < r + 50: col = mix(col, hexc("d8b8d8"), (r + 50 - d) / 50 * 0.35)
    ang = (math.degrees(math.atan2(y - my, x - mx)) + 360 - 75) % 360                     # enso ouvert en haut a droite
    if ang < 335:
        th = 3 + 9 * math.sin(ang / 335 * math.pi) ** 0.6
        if abs(d - (r + 4)) < th / 2 + (rnd(int(ang * 3), 1) * 1.5 if ang > 300 else 0): col = hexc("1a1218")
    # branche de cerisier
    br = [(334, 40), (290, 62), (250, 70), (222, 92)]
    if any(seg_dist(x, y, *br[i], *br[i + 1]) < 5 - i * 1.2 for i in range(3)) or seg_dist(x, y, 270, 66, 262, 104) < 2.2:
        col = hexc("2a1612")
    for bx, by in ((300, 52), (284, 76), (262, 60), (240, 84), (226, 100), (262, 104), (310, 72), (250, 96), (276, 50)):
        dd = math.hypot(x - bx, y - by)
        if dd < 9: col = mix(hexc("ffd6e2"), hexc("f08fae"), dd / 9)
    # sol de bois
    if y > 420:
        col = mix(hexc("3a2636"), hexc("1a1020"), (y - 420) / 184)
        if (y - 420) % 26 < 2: col = hexc("120a14")
    # petales qui tombent
    if rnd(int(x / 14), int(y / 18)) > 0.9:
        fx, fy = (x / 14) % 1.0, (y / 18) % 1.0
        if ((fx - 0.5) * 1.6) ** 2 + ((fy - 0.5) * 2.4) ** 2 < 0.06: col = hexc("ffb3c7")
    # le Sensei, a contre-jour devant la lune
    pal = {'weapon': "8a5a2b", 'skin': "d8b090", 'sleeve': "4a3060", 'hair': "f2eee6", 'beard': "f5f2ea", 'gi': "5a3a72",
           'inner': "efe6cf", 'belt': "c58643", 'hakama': "2a1a3a"}
    part = _person(x, y, 167, 430, 1.3, (196, 336), (250, 420), beard=True, wide=True)
    if part:
        col = hexc(pal[part])
        if part in ('gi', 'sleeve', 'hakama', 'skin') and d < r + 6 and not _person(x, y - 3, 167, 430, 1.3, (196, 336), (250, 420), beard=True, wide=True):
            col = mix(col, hexc("fbf2dc"), 0.75)                                          # liseré de lune
    # le disciple vu de dos, premier plan
    if (math.hypot(x - 62, y - 470) < 26 or inpoly(x, y, [(14, 604), (24, 500), (62, 488), (104, 500), (118, 604)])
            or seg_dist(x, y, 100, 512, 132, 470) < 9):
        col = hexc("0a0810")
        if math.hypot(x - 62, y - 470) < 26 and y < 462: col = hexc("1a1218")
    if seg_dist(x, y, 132, 470, 176, 404) < 3.2: col = hexc("8a5a2b")                        # son Bokken
    return col


# ---------- quetes des PNJ de metier (QuetesMetiers, 2026-11-14) ----------

def _wolf(x, y, cx, base, s, d):
    """Loup de profil (tete vers d = +1 droite / -1 gauche), pattes au sol en y = base, echelle s : partie touchee ou None."""
    dx, dy = (x - cx) * d / s, (y - base) / s
    if not (-40 <= dx <= 44 and -46 <= dy <= 1): return None
    if math.hypot(dx - 30, dy + 33) < 2.2: return 'eye'
    if ((dx + 2) / 24) ** 2 + ((dy + 24) / 9) ** 2 < 1: return 'body'
    if inpoly(dx, dy, [(16, -30), (30, -40), (40, -34), (44, -30), (34, -26), (20, -20)]): return 'body'   # tete et museau
    if inpoly(dx, dy, [(26, -38), (28, -46), (32, -38)]) or inpoly(dx, dy, [(31, -37), (34, -45), (36, -36)]): return 'body'
    if seg_dist(dx, dy, -24, -26, -38, -14) < 3.2: return 'body'                                           # queue basse
    for a, b in (((-16, -18), (-18, 0)), ((-8, -18), (-6, 0)), ((10, -18), (12, 0)), ((17, -18), (20, 0))):
        if seg_dist(dx, dy, *a, *b) < 2.4: return 'body'
    return None


def scene_lisiere(u, v, rnd):
    """« Les Crocs de la Lisiere » (Eleveur) : crepuscule brumeux, sapins noirs, deux loups aux yeux jaunes sortent de la
    lisiere vers l'enclos ; cloture de bois et mouton au premier plan, lune montante."""
    x, y = u * AW, v * AH
    col = vgrad([(0, "241a3a"), (0.3, "6a3a5a"), (0.5, "d8805a"), (0.62, "f0b070"), (1, "f0b070")], v)
    d = math.hypot(x - 90, y - 150)
    if d < 22: col = hexc("fbeed0")
    elif d < 50: col = mix(col, hexc("f6d0b0"), (50 - d) / 28 * 0.4)
    # sapins
    for k in range(-1, 15):
        tx = k * 26 + 13 * rnd(k, 1)
        top = 200 + 70 * rnd(k, 2)
        w = 16 + 8 * rnd(k, 3)
        if top < y < 380 and abs(x - tx) < (y - top) / (380 - top) * w + 1: col = mix(hexc("141a24"), hexc("0b0f16"), rnd(k, 4))
    if y >= 360: col = mix(hexc("3a4a2a"), hexc("1c2614"), (y - 360) / 244)                  # pre
    fog = math.exp(-((y - 360) / 22) ** 2) * (0.6 + 0.3 * math.sin(x * 0.04))
    col = mix(col, hexc("c8b0c0"), fog * 0.5)
    for cx, base, s, dd in ((96, 392, 1.25, 1), (228, 384, 1.05, -1)):
        part = _wolf(x, y, cx, base, s, dd)
        if part: col = hexc("ffd23a") if part == 'eye' else hexc("1a1a22")
    # mouton derriere la cloture
    if ((x - 268) / 26) ** 2 + ((y - 438) / 16) ** 2 < 1 or math.hypot(x - 244, y - 432) < 9:
        col = hexc("ece6d8") if math.hypot(x - 244, y - 432) >= 7 else hexc("3a3028")
        if (x + y) % 7 < 1.2 and math.hypot(x - 244, y - 432) >= 9: col = hexc("cfc8b8")
    if any(seg_dist(x, y, lx, 452, lx + 1, 472) < 2 for lx in (252, 260, 276, 284)): col = hexc("2a2420")
    # cloture
    for px in range(10, 334, 62):
        if abs(x - px) < 5 and 400 < y < 500: col = hexc("6b4a2a") if abs(x - px) < 3.5 else hexc("3a2614")
    for ry in (420, 456):
        if abs(y - ry - (x - 167) * 0.03) < 4: col = hexc("8a5a32") if abs(y - ry - (x - 167) * 0.03) < 2.6 else hexc("3a2614")
    return col


def scene_marais(u, v, rnd):
    """« Remedes du Marais » (Alchimiste) : marais au crepuscule vert, arbres morts et mousse pendante, crapaud rhino sur
    une souche, ronceux aux yeux rouges, lucioles et eau qui reflete."""
    x, y = u * AW, v * AH
    col = vgrad([(0, "142218"), (0.4, "3a5a3a"), (0.55, "8aa86a"), (1, "8aa86a")], v)
    if y > ridge(u, 330, 10, 5, 12, 1.0): col = hexc("1e2e1e")
    water = y > 380
    if water:
        col = mix(hexc("2a4436"), hexc("101c16"), (y - 380) / 224)
        if (y + 3 * math.sin(x * 0.08)) % 14 < 1.2: col = mix(col, hexc("8aa86a"), 0.35)
    # arbres morts et mousse
    for tx, lean in ((40, 0.06), (300, -0.05)):
        cx = tx + (380 - y) * lean
        if 90 < y < 400 and abs(x - cx) < 9 * (0.6 + (y - 90) / 400): col = hexc("0e1410")
        for bx, by, ex, ey in ((tx + 20 * lean * 40, 170, tx + (60 if lean > 0 else -60), 120), (tx, 230, tx + (-40 if lean > 0 else 40), 190)):
            if seg_dist(x, y, bx, by, ex, ey) < 3: col = hexc("0e1410")
        for k in range(5):
            mx = tx + (k * 9 - 18) * (1 if lean > 0 else -1) + 20
            if abs(x - mx) < 1.6 and 130 + k * 6 < y < 200 + 30 * rnd(k, int(tx)): col = hexc("4a6a3a")
    # souche et crapaud rhino
    if 150 <= x <= 250 and 380 <= y <= 410: col = hexc("3a2a1c") if y > 384 else hexc("6b5034")
    toad = ((x - 200) / 40) ** 2 + ((y - 356) / 26) ** 2 < 1 or math.hypot(x - 232, y - 346) < 16
    if toad:
        col = mix(hexc("8a9a4a"), hexc("4a5a2a"), (y - 330) / 50)
        if rnd(int(x / 5), int(y / 5)) > 0.8: col = hexc("6a7a34")
    if inpoly(x, y, [(240, 338), (262, 320), (248, 344)]): col = hexc("e8e0c0")                 # corne
    if math.hypot(x - 236, y - 338) < 3: col = hexc("ffd23a")
    for lx in (178, 222):
        if seg_dist(x, y, lx, 372, lx - 8, 392) < 5: col = hexc("4a5a2a")
    # ronceux
    rb = math.hypot((x - 80) / 1.2, y - 360)
    if rb < 34 + 6 * math.sin(math.atan2(y - 360, x - 80) * 9):
        col = hexc("2a4a22") if rnd(int(x / 4), int(y / 4)) > 0.3 else hexc("3a6a2a")
        for ex in (70, 90):
            if math.hypot(x - ex, y - 352) < 3: col = hexc("ff3a2a")
    # reflets et lucioles
    if water and 160 < x < 260 and rnd(int(x / 3), int(y / 2)) > 0.93: col = mix(col, hexc("6a7a34"), 0.5)
    if y < 420 and rnd(int(x / 6), int(y / 6)) > 0.985:
        fx, fy = (x / 6) % 1.0 - 0.5, (y / 6) % 1.0 - 0.5
        if fx * fx + fy * fy < 0.1: col = hexc("e8ff8a")
    return col


def scene_forge(u, v, rnd):
    """« Braises pour la Forge » (Forgeron) : bouche du fourneau rougeoyante, enclume et lame chauffee a blanc, brasero d'os
    calcines, cœur de braise dans un creuset, etincelles."""
    x, y = u * AW, v * AH
    col = vgrad([(0, "120c0c"), (1, "2a1810")], v)
    if (int(y / 20) % 2 == 0 and x % 40 < 1.5) or (int(y / 20) % 2 == 1 and (x + 20) % 40 < 1.5) or y % 20 < 1.5:
        col = mix(col, hexc("060404"), 0.6)
    # fourneau
    fd = math.hypot((x - 167) / 1.0, max(0.0, 200 - y))
    if 100 < x < 234 and 120 < y < 300 and (y > 200 or fd < 67):
        col = mix(hexc("fff0a0"), hexc("e8481c"), min(1.0, math.hypot(x - 167, y - 250) / 90))
    elif 88 < x < 246 and 108 < y < 312 and (y > 200 or fd < 79): col = hexc("3a2420")
    glow = max(0.0, 1 - math.hypot(x - 167, y - 230) / 220)
    col = mix(col, hexc("e07a30"), glow ** 2 * 0.5)
    # sol
    if y > 420: col = mix(hexc("2a1a12"), hexc("120a08"), (y - 420) / 184)
    # enclume
    anv = inpoly(x, y, [(110, 360), (230, 360), (250, 350), (212, 382), (196, 382), (204, 420), (226, 432), (114, 432),
                        (136, 420), (144, 382), (128, 382)])
    if anv: col = hexc("2a2a30") if y > 366 else hexc("6a6a74")
    if seg_dist(x, y, 120, 352, 226, 346) < 3: col = mix(hexc("fff6d0"), hexc("ff8a2a"), abs(x - 175) / 60)   # lame chauffee
    if seg_dist(x, y, 226, 346, 246, 344) < 2.2: col = hexc("4a2c18")
    # marteau
    if seg_dist(x, y, 252, 410, 290, 330) < 3: col = hexc("6b4a2a")
    if seg_dist(x, y, 276, 322, 306, 340) < 9: col = hexc("4a4a52")
    # brasero d'os calcines
    if 24 <= x <= 76 and 380 <= y <= 420: col = hexc("3a3a40") if y > 386 else hexc("5a5a64")
    for a, b in (((30, 378), (62, 364)), ((40, 366), (70, 380)), ((34, 372), (58, 356))):
        if seg_dist(x, y, *a, *b) < 3.5: col = hexc("4a3a30")
    for cx, h in ((50, 60), (38, 40), (62, 44)):
        t = (380 - y) / h
        if 0 <= t <= 1 and abs(x - cx - 3 * math.sin(t * 7)) < 10 * (1 - t): col = mix(hexc("fff1a8"), hexc("e8481c"), t)
    # creuset et coeur de braise
    if inpoly(x, y, [(266, 380), (310, 380), (304, 420), (272, 420)]): col = hexc("3a2a24")
    hd = math.hypot(x - 288, y - 370)
    if hd < 13: col = mix(hexc("ffe08a"), hexc("d8301c"), hd / 13)
    elif hd < 26: col = mix(col, hexc("ff7a30"), (26 - hd) / 13 * 0.5)
    # etincelles
    if y < 360 and rnd(int(x / 3), int(y / 3)) > 0.992: col = hexc("ffd27a")
    return col


def scene_dunes(u, v, rnd):
    """« Les Morts du Desert » (Marchand Luo) : soleil couchant sur les dunes, caravane brisee, zombies des sables en
    bandages qui sortent du sable."""
    x, y = u * AW, v * AH
    col = vgrad([(0, "3a1a3a"), (0.3, "c0483a"), (0.48, "f0a050"), (1, "f0a050")], v)
    d = math.hypot(x - 200, y - 260)
    if d < 44: col = hexc("ffe6a0")
    elif d < 90: col = mix(col, hexc("ffd080"), (90 - d) / 46 * 0.5)
    if y > ridge(u, 290, 18, 3, 7, 0.5): col = hexc("a85a3a")
    if y > ridge(u, 340, 22, 2.5, 6, 2.0): col = mix(hexc("d8904a"), hexc("8a4a2a"), (y - 340) / 120)
    if y > ridge(u, 420, 14, 3, 9, 1.2): col = mix(hexc("c07a3a"), hexc("5a2a1a"), (y - 420) / 184)
    if y > 300 and (x * 0.8 + y * 2 + 10 * math.sin(x * 0.05)) % 24 < 1.2: col = mix(col, hexc("6a3020"), 0.35)
    # caravane brisee
    if inpoly(x, y, [(16, 380), (110, 366), (116, 400), (22, 414)]): col = hexc("4a2a1a")
    if inpoly(x, y, [(24, 372), (60, 330), (104, 362)]) and not inpoly(x, y, [(40, 368), (60, 342), (88, 362)]): col = hexc("e8d8b8")
    wd = math.hypot(x - 96, y - 414)
    if 14 < wd < 19 or (wd < 15 and min(abs(math.atan2(y - 414, x - 96) * 4 / math.pi % 1.0 - 0.5), 1) > 0.42): col = hexc("2a1810")
    # zombies des sables
    for cx, top, sd in ((178, 380, 1), (262, 404, -1), (300, 452, -1)):
        dx = (x - cx) * sd
        if y < top + 40 + (cx % 7) and _zombie(dx, y, top):
            col = hexc("5a4a34")
            if (y - top) % 9 < 2.4: col = hexc("c8b890")                                    # bandages
            if not _zombie(dx + 2.5, y, top): col = hexc("ffb060")
            for ex in (5, 12):
                if math.hypot(dx - ex, y - top + 1) < 2.2: col = hexc("ffe060")
    if rnd(int(x / 4), int(y / 4)) > 0.995 and y > 300: col = hexc("f0e0c0")
    return col


def scene_outlanders(u, v, rnd):
    """« Trophees pour un Collectionneur » (Marchand ambulant) : nuit polaire et aurore, montagnes enneigees, guerrier
    Outlander au casque a cornes et sa hache, mystique encapuchonne au talisman d'os, totem de cranes, neige."""
    x, y = u * AW, v * AH
    col = vgrad([(0, "060a1a"), (0.5, "142040"), (1, "142040")], v)
    for k, (base, amp, c) in enumerate(((90, 26, "3aff9a"), (130, 22, "6affd0"), (70, 18, "9a6aff"))):
        cy = base + amp * math.sin(x * 0.025 + k * 1.3)
        if 0 < y - cy < 50: col = mix(col, hexc(c), (1 - (y - cy) / 50) * 0.45)
    if y < 250 and rnd(int(x / 2), int(y / 2)) > 0.99: col = hexc("e8eeff")
    peaks = 260 + 70 * abs(((u * 2.2) % 1.0) - 0.5)
    if y > peaks: col = hexc("dfe8f5") if y < peaks + 18 else hexc("3a4a6a")
    if y > 380: col = mix(hexc("cfdcee"), hexc("8a9ab8"), (y - 380) / 224)
    # totem de cranes
    if abs(x - 300) < 3 and 250 < y < 420: col = hexc("3a2a1c")
    for ty in (262, 290):
        if math.hypot(x - 300, y - ty) < 9: col = hexc("e8e0c8") if math.hypot(x - 300, y - ty - 2) > 3 else hexc("1a1410")
    # guerrier Outlander (cornes, hache)
    pal = {'weapon': "4a2c18", 'skin': "c89a78", 'sleeve': "5a4a3a", 'hair': "3a3a44", 'gi': "6a5a48", 'inner': "8a7a68",
           'belt': "2a1a10", 'hakama': "3a3028"}
    part = _person(x, y, 130, 470, 1.15, (162, 332), (196, 250), wide=True)
    if part: col = _paint(col, part, pal)
    if seg_dist(x, y, 186, 262, 206, 238) < 9 and not seg_dist(x, y, 196, 250, 214, 232) < 3: col = hexc("c9ccd6")   # fer de hache
    hy = 470 - 140 * 1.15
    for sx in (-1, 1):
        if seg_dist(x, y, 130 + sx * 10, hy - 6, 130 + sx * 24, hy - 26) < 3.5 or seg_dist(x, y, 130 + sx * 24, hy - 26, 130 + sx * 20, hy - 40) < 2.2:
            col = hexc("e8dcc0")
    if math.hypot(x - 130, y - hy) < 13 and y < hy - 2: col = hexc("6a6a74")                  # casque
    # mystique au talisman
    part = _person(x, y, 240, 440, 0.85, (222, 360), (214, 300), hood=True)
    if part:
        col = _paint(col, part, {'weapon': "5a3a22", 'skin': "c89a78", 'sleeve': "2a2a40", 'hood': "2a2a40", 'face': "0a0a14",
                                 'gi': "2a2a40", 'inner': "4a3a60", 'belt': "c58643", 'hakama': "1a1a2a"})
    td = math.hypot(x - 214, y - 296)
    if td < 7: col = hexc("e8e0c8")
    elif td < 22: col = mix(col, hexc("9a6aff"), (22 - td) / 15 * 0.5)
    if rnd(int(x / 5), int(y / 7)) > 0.97 and (x / 5) % 1.0 < 0.4 and (y / 7) % 1.0 < 0.3: col = hexc("ffffff")   # neige
    return col


def scene_arene(u, v, rnd):
    """« Le Droit d'Entree » (Maitre de guilde) : arene de guilde en plein jour, gradins et foule, bannieres et ecusson,
    deux aventuriers (rouge et bleu) croisent le fer."""
    x, y = u * AW, v * AH
    col = vgrad([(0, "6aa8e0"), (0.25, "bcdcf0"), (1, "bcdcf0")], v)
    if y > 90:                                                                                 # gradins
        row = int((y - 90) / 26)
        col = hexc("8a7a68") if (y - 90) % 26 > 6 else hexc("5a4a3a")
        if y < 300 and (y - 90) % 26 > 8:                                                      # foule
            hx = (x + row * 9) % 14
            if math.hypot(hx - 7, (y - 90) % 26 - 16) < 5.5:
                col = hexc(["7a2a2a", "2a4a7a", "6a5a2a", "3a6a3a", "5a3a6a"][int((x + row * 9) / 14 + row) % 5])
    # bannieres et ecusson
    for bx in (40, 294):
        if abs(x - bx) < 16 and 60 < y < 230 - (8 if abs(x - bx) < 4 else 0):
            col = hexc("8a1a2a") if abs(x - bx) < 13 else hexc("c58643")
    sh = inpoly(x, y, [(137, 40), (197, 40), (197, 90), (167, 118), (137, 90)])
    if sh:
        col = hexc("2a3a7a")
        if abs(x - 167) < 4 or abs(y - 70) < 4: col = hexc("f2c45e")
    elif inpoly(x, y, [(132, 35), (202, 35), (202, 92), (167, 124), (132, 92)]): col = hexc("c58643")
    # mur et sol de l'arene
    if 300 < y < 330: col = hexc("6a5a48") if (x % 30) > 2 else hexc("3a3028")
    if y >= 330: col = mix(hexc("d8c08a"), hexc("a88a5a"), (y - 330) / 274)
    # deux aventuriers
    red = {'weapon': "d8dce6", 'skin': "e2b48a", 'sleeve': "8a1a1a", 'hair': "3a2010", 'gi': "a82a2a", 'inner': "e8d8b8",
           'belt': "2a1a10", 'hakama': "3a2a20"}
    blue = {'weapon': "d8dce6", 'skin': "d8a880", 'sleeve': "1a3a7a", 'hair': "c8a060", 'gi': "2a4aa0", 'inner': "e8d8b8",
            'belt': "c58643", 'hakama': "1a2440"}
    for cx, pal, hand, tip in ((105, red, (130, 346), (186, 300)), (232, blue, (206, 350), (156, 296))):
        if math.hypot((x - cx) / 3, y - 470) < 9: col = mix(col, hexc("6a5030"), 0.4)
        part = _person(x, y, cx, 470, 1.15, hand, tip)
        if part: col = _paint(col, part, pal)
    if math.hypot(x - 171, y - 298) < 7: col = hexc("fff6c0")                                   # etincelle du choc
    return col


def scene_vide(u, v, rnd):
    """« L'Oeil du Vide » (Alchimiste) : faille du Vide, oeil geant a pupille fendue, tentacules, prismes violets flottants."""
    x, y = u * AW, v * AH
    col = vgrad([(0, "05030a"), (0.5, "1a0a2a"), (1, "0a0510")], v)
    if rnd(int(x / 2), int(y / 2)) > 0.992: col = hexc("c8a8ff")
    rift = abs(x - 167 - 20 * math.sin(y * 0.02)) < 70 * math.sin(min(math.pi, max(0.0, (y - 60) / 360 * math.pi)))
    if rift: col = mix(hexc("3a1060"), hexc("8a3ac0"), 0.3 + 0.3 * math.sin(y * 0.05 + x * 0.03))
    # oeil
    ex, ey = 167, 240
    e = ((x - ex) / 62) ** 2 + ((y - ey) / 38) ** 2
    if e < 1:
        col = hexc("e8dcf0")
        ir = math.hypot(x - ex, y - ey)
        if ir < 30: col = mix(hexc("c060ff"), hexc("4a0a8a"), ir / 30)
        if abs(x - ex) < 5 * (1 - abs(y - ey) / 30) and ir < 30: col = hexc("05030a")
        if math.hypot(x - ex + 9, y - ey + 10) < 5: col = hexc("ffffff")
        if e > 0.8 and rnd(int(x / 3), int(y / 3)) > 0.6: col = hexc("c0405a")                 # veines
    elif e < 1.25: col = hexc("2a0a3a")
    # tentacules
    for k, (ox, oy, ph) in enumerate(((60, 600, 0.0), (280, 600, 1.7), (20, 420, 3.1), (320, 400, 2.2))):
        t = (oy - y) / 260
        if 0 <= t <= 1:
            cx = ox + (ex - ox) * t * 0.6 + 26 * math.sin(t * 7 + ph)
            if abs(x - cx) < 12 * (1 - t) + 2:
                col = hexc("2a0a3a") if abs(x - cx) > 9 * (1 - t) else hexc("5a1a6a")
                if abs(x - cx) < 3 and int(t * 20) % 2 == 0: col = hexc("c080d0")
    # prismes
    for px, py, sz in ((70, 140, 18), (270, 150, 22), (90, 400, 16), (250, 380, 20)):
        if abs(x - px) / sz + abs(y - py) / (sz * 1.6) < 1:
            col = mix(hexc("f0c0ff"), hexc("8a3ac0"), abs(x - px) / sz)
        elif abs(x - px) / sz + abs(y - py) / (sz * 1.6) < 1.6: col = mix(col, hexc("b070ff"), 0.25)
    return col


def scene_ombre(u, v, rnd):
    """« L'Acier de l'Ombre » (Forgeron legendaire) : enclume sous une lumiere partagee (braise a gauche, glace a droite),
    lame noire nimbee de violet (Croc du Neant), noyau de firesteel incandescent, heaume du Chevalier de l'Ombre."""
    x, y = u * AW, v * AH
    col = mix(vgrad([(0, "1a0c08"), (1, "2a140c")], v), vgrad([(0, "080c1a"), (1, "0c1428")], v), min(1.0, max(0.0, (x - 120) / 100)))
    col = mix(col, hexc("e06a2a"), max(0.0, 1 - math.hypot(x - 60, y - 330) / 200) ** 2 * 0.5)
    col = mix(col, hexc("4a7ad8"), max(0.0, 1 - math.hypot(x - 280, y - 330) / 200) ** 2 * 0.45)
    if y > 430: col = mix(col, hexc("05050a"), 0.5)
    # lame (Croc du Neant), verticale, pointe en bas, aura violette
    bd = seg_dist(x, y, 167, 90, 167, 360)
    if bd < 22: col = mix(col, hexc("9a4aff"), (1 - bd / 22) * 0.5)
    if inpoly(x, y, [(158, 110), (176, 110), (178, 330), (167, 372), (156, 330)]):
        col = hexc("1a1424") if abs(x - 167) > 2 else hexc("6a4aa0")
        if inpoly(x, y, [(176, 200), (190, 214), (177, 226)]): col = hexc("e8e0f0")           # croc
    if abs(y - 104) < 6 and abs(x - 167) < 34: col = hexc("3a2a4a")                            # garde
    if 60 < y < 98 and abs(x - 167) < 5: col = hexc("2a1a10")
    if math.hypot(x - 167, y - 56) < 8: col = hexc("c0a0ff")
    # enclume
    if inpoly(x, y, [(96, 372), (238, 372), (258, 362), (214, 396), (204, 396), (212, 440), (236, 452), (98, 452),
                     (122, 440), (130, 396), (116, 396)]):
        col = hexc("2a2a34") if y > 378 else hexc("5a5a6a")
    # noyau de firesteel (gauche)
    if inpoly(x, y, [(40, 420), (80, 420), (74, 452), (46, 452)]): col = hexc("3a2420")
    nd = math.hypot(x - 60, y - 398)
    if nd < 18: col = mix(hexc("fff0a0"), hexc("e8481c"), nd / 18)
    elif nd < 40: col = mix(col, hexc("ff8a30"), (40 - nd) / 22 * 0.45)
    # heaume du Chevalier de l'Ombre (droite)
    hx, hy = 278, 400
    if ((x - hx) / 28) ** 2 + ((y - hy) / 34) ** 2 < 1 and y < hy + 26:
        col = hexc("14141c") if ((x - hx) / 28) ** 2 + ((y - hy) / 34) ** 2 < 0.8 else hexc("4a5a7a")
        if abs(y - hy + 2) < 3 and abs(x - hx) < 18: col = hexc("8ac0ff")                      # fente des yeux
        if abs(x - hx) < 2 and hy - 30 < y < hy - 4: col = hexc("4a5a7a")
    if seg_dist(x, y, hx, hy - 34, hx + 6, hy - 56) < 3: col = hexc("2a2a3a")                  # cimier
    return col


# ---------- quete du Roi, etape 1 et temoins (QueteRoi, 2026-11-14) ----------

def _house(x, y, x0, x1, base, roof_h, wall, roof, lit):
    """Maison de village : murs, toit en pente, fenetres eclairees (lit) ; renvoie une couleur ou None."""
    w = x1 - x0
    top = base - w * 0.55
    if x0 <= x <= x1 and top <= y <= base:
        if lit and (abs(x - (x0 + w * 0.3)) < 6 or abs(x - (x0 + w * 0.7)) < 6) and top + 12 < y < top + 26: return hexc("ffc860")
        if abs(x - (x0 + w * 0.5)) < 7 and y > base - 22: return hexc("2a1810")
        return hexc(wall)
    if inpoly(x, y, [(x0 - 6, top), (x0 + w / 2, top - roof_h), (x1 + 6, top)]): return hexc(roof)
    return None


def scene_rumeurs(u, v, rnd):
    """« Les Rumeurs du Roi Fou » : place de village au crepuscule, fenetres de taverne allumees, un vieux villageois a la
    pipe raconte ; la fumee forme une couronne au-dessus de lui ; l'aventurier ecoute."""
    x, y = u * AW, v * AH
    col = vgrad([(0, "1e1838"), (0.35, "5a3a6a"), (0.55, "d8885a"), (1, "d8885a")], v)
    if y < 200 and rnd(int(x / 2), int(y / 2)) > 0.993: col = hexc("f0e8ff")
    for x0, x1, base, rh, wall, roof, lit in ((-10, 90, 360, 50, "5a4a3e", "3a2420", True), (100, 230, 350, 60, "6a5444", "4a2a22", True),
                                              (240, 344, 365, 46, "5a4a3e", "3a2420", False)):
        c = _house(x, y, x0, x1, base, rh, wall, roof, lit)
        if c: col = c
    if y > 360: col = mix(hexc("6a5a4a"), hexc("2a2018"), (y - 360) / 244)
    if y > 360 and ((x + (int((y - 360) / 18) % 2) * 15) % 30 < 1.5 or (y - 360) % 18 < 1.5): col = mix(col, hexc("1a1410"), 0.5)
    # lanterne
    if abs(x - 300) < 2.5 and 250 < y < 460: col = hexc("1a1410")
    ld = math.hypot(x - 300, y - 256)
    if ld < 7: col = hexc("ffe08a")
    elif ld < 60: col = mix(col, hexc("ffb050"), (60 - ld) / 53 * 0.35)
    # fumee de pipe en couronne
    sm = [(118, 300), (124, 270), (112, 240), (128, 210)]
    if any(seg_dist(x, y, *sm[i], *sm[i + 1]) < 4 + i * 1.5 for i in range(3)): col = mix(col, hexc("d8d0e0"), 0.55)
    if inpoly(x, y, [(96, 196), (96, 168), (110, 182), (128, 160), (146, 182), (160, 168), (160, 196)]):
        col = mix(col, hexc("e8e0f0"), 0.6) if not inpoly(x, y, [(100, 192), (100, 176), (110, 186), (128, 166), (146, 186), (156, 176), (156, 192)]) else mix(col, hexc("f2c45e"), 0.25)
    # le vieux villageois (barbe, canne) et l'aventurier
    old = {'weapon': "6b4a2a", 'skin': "d8b090", 'sleeve': "5a4a3a", 'hair': "e8e4da", 'beard': "efeae0", 'gi': "6a5a48",
           'inner': "8a7a68", 'belt': "3a2a1a", 'hakama': "3a3028"}
    part = _person(x, y, 96, 470, 1.0, (114, 404), (120, 470), beard=True, wide=True)
    if part: col = _paint(col, part, old)
    if math.hypot(x - 112, y - 306) < 3: col = hexc("4a2c18")                                   # pipe
    adv = {'weapon': "c9ccd6", 'skin': "e2b48a", 'sleeve': "2a3a6a", 'hair': "3a2010", 'gi': "3a4a7a", 'inner': "c9c2b2",
           'belt': "c58643", 'hakama': "1a2040"}
    part = _person(x, y, 236, 478, 1.05, (222, 410), (206, 470))
    if part: col = _paint(col, part, adv)
    return col


def scene_hameau(u, v, rnd):
    """« Le Vieux Garde » : hameau pris par les morts, la nuit ; une maison brule, zombies entre les toits de chaume,
    tombes ouvertes au premier plan, brume verte."""
    x, y = u * AW, v * AH
    col = vgrad([(0, "05081a"), (0.5, "1a2240"), (1, "1a2240")], v)
    if y < 220 and rnd(int(x / 2), int(y / 2)) > 0.992: col = hexc("e0e8ff")
    if math.hypot(x - 260, y - 90) < 20: col = hexc("e8eef8")
    fire = max(0.0, 1 - math.hypot(x - 90, y - 260) / 170)
    col = mix(col, hexc("e06a2a"), fire ** 2 * 0.55)
    for x0, x1, base, rh, wall, roof, lit in ((20, 150, 330, 48, "3a3028", "6a5a2a", False), (190, 320, 320, 44, "2a2420", "5a4a24", False)):
        c = _house(x, y, x0, x1, base, rh, wall, roof, lit)
        if c: col = c
    for cx, h, w in ((70, 120, 30), (95, 90, 22), (120, 70, 18)):                                 # flammes sur le toit
        t = (290 - y) / h
        if 0 <= t <= 1 and abs(x - cx - 6 * math.sin(t * 6 + cx)) < w * (1 - t): col = mix(hexc("fff1a8"), hexc("e8481c"), t)
    if y > 340: col = mix(hexc("1c2418"), hexc("0a0e0a"), (y - 340) / 264)
    fog = math.exp(-((y - 400) / 40) ** 2) * (0.5 + 0.3 * math.sin(x * 0.05))
    col = mix(col, hexc("4fae7c"), fog * 0.35)
    for cx, top, sd in ((160, 352, 1), (232, 368, -1), (58, 384, 1)):
        dx = (x - cx) * sd
        if _zombie(dx, y, top):
            col = mix(hexc("4a5a40"), hexc("2a3426"), (y - top) / 200)                          # chair grise et haillons
            if (y - top) > 40 and (y - top) % 23 < 3: col = hexc("3a2a20")
            if not _zombie(dx - 2.5, y, top) and cx < 120: col = hexc("e08a3a")              # lueur du feu
            elif not _zombie(dx + 2.5, y, top): col = hexc("2f6a44")
            for ex in (5, 12):
                if math.hypot(dx - ex, y - top + 1) < 2.2: col = hexc("9dff7a")
    for gx, gy in ((40, 520), (150, 540), (280, 515)):                                           # tombes ouvertes
        if abs(x - gx) < 14 and gy - 30 < y < gy and (y > gy - 22 or math.hypot(x - gx, y - (gy - 22)) < 14): col = hexc("4a5060")
        if abs(x - gx - 26) < 16 and abs(y - gy - 4) < 5: col = hexc("2a1e14")
    return col


def scene_azur(u, v, rnd):
    """« La Servante de la Reine » : foret d'azur, feuillages bleus, fruits lumineux, seve qui perle d'un tronc, panier."""
    x, y = u * AW, v * AH
    col = vgrad([(0, "0a1430"), (0.5, "1a3a6a"), (1, "102040")], v)
    for k in range(-1, 8):                                                                      # troncs
        tx = k * 52 + 20 * rnd(k, 5)
        if abs(x - tx) < 9 + 3 * rnd(k, 6) and y > 120: col = mix(hexc("3a2a3a"), hexc("1a1420"), rnd(k, 7))
    for k in range(-1, 9):                                                                      # feuillages
        cx, cy, r = k * 44 + 30 * rnd(k, 1), 110 + 80 * rnd(k, 2), 50 + 20 * rnd(k, 3)
        d = math.hypot(x - cx, (y - cy) * 1.3)
        if d < r: col = mix(hexc("4a9ae8"), hexc("1a4a9a"), d / r * 0.8 + 0.2 * rnd(int(x / 8), int(y / 8)))
    # grand tronc central et seve
    if abs(x - 167 - 6 * math.sin(y * 0.02)) < 20 and y > 180: col = hexc("4a3048") if abs(x - 167) < 16 else hexc("2a1a2a")
    for sy in range(0, 3):
        dd = math.hypot(x - 180, y - (300 + sy * 26))
        if dd < 4 + sy: col = mix(hexc("ffd27a"), hexc("d8902a"), dd / 6)
    if y > 440: col = mix(hexc("1a3a4a"), hexc("08141c"), (y - 440) / 164)
    # fruits d'azur lumineux
    for k in range(14):
        fx, fy = 20 + rnd(k, 11) * 294, 80 + rnd(k, 12) * 200
        dd = math.hypot(x - fx, y - fy)
        if dd < 6: col = mix(hexc("e0f8ff"), hexc("3ab0ff"), dd / 6)
        elif dd < 16: col = mix(col, hexc("8ad8ff"), (16 - dd) / 10 * 0.35)
    # panier de fruits
    if inpoly(x, y, [(86, 470), (150, 470), (140, 510), (96, 510)]):
        col = hexc("8a6a3a") if (int(x / 6) + int(y / 6)) % 2 else hexc("6a4a2a")
    for fx in (100, 114, 128, 140):
        if math.hypot(x - fx, y - 466) < 7: col = hexc("6ac8ff")
    if seg_dist(x, y, 90, 470, 118, 436) < 2.5 or seg_dist(x, y, 118, 436, 146, 470) < 2.5: col = hexc("6a4a2a")
    if rnd(int(x / 5), int(y / 5)) > 0.99: col = hexc("c8f0ff")
    return col


def scene_mine(u, v, rnd):
    """« L'Ermite des Livres » : galerie de mine, etais de bois, rails qui fuient vers le fond, wagonnet, lanterne,
    filons de cobalt (bleu) et de thorium (vert) dans la roche, pioche."""
    x, y = u * AW, v * AH
    vx, vy = 167, 250                                                                            # point de fuite
    col = mix(hexc("2a2420"), hexc("0a0806"), min(1.0, math.hypot(x - vx, y - vy) / 260))
    if math.hypot(x - vx, (y - vy) * 1.2) < 34: col = hexc("040302")
    # filons
    for k in range(18):
        ox, oy = rnd(k, 21) * 334, 40 + rnd(k, 22) * 420
        if math.hypot(ox - vx, oy - vy) < 70: continue
        dd = math.hypot(x - ox, (y - oy) * 1.4)
        c = "3a7aff" if k % 2 else "6aff7a"
        if dd < 7 + 4 * rnd(k, 23): col = mix(hexc(c), hexc("ffffff"), 0.2 if dd < 3 else 0)
        elif dd < 22: col = mix(col, hexc(c), (22 - dd) / 15 * 0.25)
    # etais en perspective
    for d in (0.25, 0.5, 1.0):
        l, r = vx - 170 * d, vx + 170 * d
        t, b = vy - 200 * d, vy + 300 * d
        thick = 12 * d
        if (abs(x - l) < thick or abs(x - r) < thick) and t < y < b: col = hexc("6b4a2a") if d < 1 else hexc("8a5a32")
        if abs(y - t) < thick and l - thick < x < r + thick: col = hexc("5a3a20") if d < 1 else hexc("7a5030")
    # rails
    if y > vy:
        k = (y - vy) / (604 - vy)
        for side in (-1, 1):
            if abs(x - (vx + side * (8 + 90 * k))) < 1 + 3 * k: col = hexc("9a9aa8")
        if ((y - vy) ** 0.5 * 3) % 1.0 < 0.18 and abs(x - vx) < 10 + 100 * k: col = hexc("4a2c18")
    # wagonnet de minerai
    if inpoly(x, y, [(196, 420), (296, 420), (286, 470), (206, 470)]): col = hexc("4a4a52") if y > 426 else hexc("6a6a74")
    for fx, fy, c in ((216, 414, "3a7aff"), (236, 410, "6aff7a"), (256, 414, "3a7aff"), (276, 412, "6aff7a")):
        if math.hypot(x - fx, y - fy) < 9: col = hexc(c)
    for wx in (220, 274):
        if math.hypot(x - wx, y - 476) < 10: col = hexc("1a1a20")
    # lanterne et pioche
    if abs(x - 70) < 2 and 120 < y < 160: col = hexc("1a1410")
    ld = math.hypot(x - 70, y - 168)
    if ld < 8: col = hexc("ffe08a")
    elif ld < 90: col = mix(col, hexc("ffb050"), (90 - ld) / 82 * 0.3)
    if seg_dist(x, y, 60, 520, 120, 430) < 3.5: col = hexc("6b4a2a")
    if seg_dist(x, y, 96, 414, 146, 450) < 5: col = hexc("8a8a94")
    return col


SCENES = {"Aube": scene_aube, "Nuit": scene_nuit, "Duel": scene_duel, "Trone": scene_trone, "Dojo": scene_dojo,
          "Vent": scene_vent, "Brigands": scene_brigands, "Trork": scene_trork, "Sensei": scene_sensei,
          "Lisiere": scene_lisiere, "Marais": scene_marais, "Forge": scene_forge, "Dunes": scene_dunes,
          "Outlanders": scene_outlanders, "Arene": scene_arene, "Vide": scene_vide, "Ombre": scene_ombre,
          "Rumeurs": scene_rumeurs, "Hameau": scene_hameau, "Azur": scene_azur, "Mine": scene_mine}


def paint_art(name, mask):
    seed = sum(map(ord, name))
    rr = random.Random(seed)
    cache = {}

    def rnd(a, b):
        k = (a, b)
        if k not in cache: cache[k] = random.Random(a * 7919 + b * 104729 + seed).random()
        return cache[k]
    # vitrail : graines sur une grille perturbee
    G = 30
    gw, gh = AW // G + 2, AH // G + 2
    seeds = {(i, j): ((i + rr.random()) * G, (j + rr.random()) * G, 0.9 + rr.random() * 0.2) for i in range(gw) for j in range(gh)}
    img = blank(AW, AH)
    scene = SCENES[name]
    for y in range(AH):
        for x in range(AW):
            a = mask[y][x]
            if a == 0: continue
            col = scene(x / AW, y / AH, rnd)
            gi, gj = int(x / G), int(y / G)
            best, second, k = 1e9, 1e9, 1.0
            for di in (-1, 0, 1):
                for dj in (-1, 0, 1):
                    s = seeds.get((gi + di, gj + dj))
                    if not s: continue
                    dd = math.hypot(x - s[0], y - s[1])
                    if dd < best: best, second, k = dd, best, s[2]
                    elif dd < second: second = dd
            col = [c * k for c in col]
            lead = second - best
            if lead < 1.6: col = mix(col, hexc("1b1420"), 0.75 * (1.6 - lead) / 1.6 + 0.25)
            vig = math.hypot((x / AW - 0.5) * 1.2, y / AH - 0.5)
            col = mix(col, hexc("120e18"), max(0.0, vig - 0.42) * 1.2)
            img[y][x] = [col[0], col[1], col[2], a]
    save(f"ChapArt{name}C@2x.png", img)
    gray = blank(AW, AH)
    for y in range(AH):
        for x in range(AW):
            r, g, b, a = img[y][x]
            l = 0.3 * r + 0.59 * g + 0.11 * b
            gray[y][x] = [l * 0.62 + 18, l * 0.64 + 20, l * 0.7 + 26, a]
    save(f"ChapArt{name}G@2x.png", gray)


def box_blur(a, w, h, r):
    """Flou boite separable (3 passes = presque gaussien) d'un champ w x h (liste plate)."""
    for _ in range(3):
        out = [0.0] * (w * h)
        for y in range(h):
            row = a[y * w:(y + 1) * w]
            acc = sum(row[0:r + 1]) + row[0] * r
            for x in range(w):
                out[y * w + x] = acc / (2 * r + 1)
                acc += row[min(w - 1, x + r + 1)] - row[max(0, x - r)]
        a = out
        out = [0.0] * (w * h)
        for x in range(w):
            col = [a[y * w + x] for y in range(h)]
            acc = sum(col[0:r + 1]) + col[0] * r
            for y in range(h):
                out[y * w + x] = acc / (2 * r + 1)
                acc += col[min(h - 1, y + r + 1)] - col[max(0, y - r)]
        a = out
    return a


def card_hover():
    """Survol d'une carte (2026-11-13) : halo dore autour de la silhouette de la carte (cadre verrouille du client = forme pleine),
    dessine en fond du bouton, donc derriere l'illustration et le cadre : seul le halo depasse."""
    w, h, px = read_any(os.path.join(CHAP, "ChapterCardFrameLocked.png"))
    m = [1.0 if px[y][x][3] > 128 else 0.0 for y in range(h) for x in range(w)]
    g = box_blur(m, w, h, 14)
    img = blank(w, h)
    gold = hexc("ffd27a")
    for y in range(h):
        for x in range(w):
            a = min(1.0, g[y * w + x] * 2.2)
            img[y][x] = [gold[0], gold[1], gold[2], 255 * a * 0.9]
    save("ChapCardHover@2x.png", img)


def card_shade(mask):
    """Fondu sombre au pied de la carte, sous le nom de l'acte."""
    H0 = 170
    img = blank(AW, H0)
    for y in range(H0):
        t = y / (H0 - 1)
        a = (t ** 1.4) * 0.92
        for x in range(AW):
            m = mask[AH - H0 + y][x] / 255.0
            img[y][x] = [10, 13, 22, 255 * a * m]
    save("ChapCardShade@2x.png", img)


# ==================== .ui ====================

# Eclats autour de la carte choisie (ChapterCardShards.xaml : nom, largeur, hauteur, marge gauche, marge haute, repere du cadre)
SHARDS = [(1, 6, 17, 41.7, 77.6), (2, 40, 42, 81.5, 34.9), (3, 20, 9, 48.5, 52.4), (4, 36, 20, 125.2, 28.1),
          (5, 32, 11, 205.7, 24.3), (6, 60, 40, 245.5, 25.2), (7, 17, 17, 296.9, 58.2), (8, 20, 23, 328.0, 60.2),
          (9, 38, 35, 312.4, 619.0), (10, 16, 16, 229.0, 679.2), (11, 23, 10, 258.1, 668.5), (12, 36, 35, 129.0, 669.5),
          (13, 51, 38, 62.1, 651.1), (14, 9, 16, 31.0, 615.2), (15, 19, 16, 164.0, 668.5), (16, 32, 19, 276.5, 642.3)]
BADGE = 52


def card(i):
    x = CARD_L0 + i * CARD_PITCH
    arts = "\n".join(
        f'      Group #ChapArt{t}{k}{i} {{ Anchor: (Left: {ART_DX - FRAME_DX}, Top: {ART_DY - FRAME_DY}, Width: {ART_W}, Height: {ART_H}); '
        f'Background: (TexturePath: "ChapArt{t}{k}.png"); Visible: false; }}'
        for t in ARTS for k in ("G", "C"))
    shard_groups = "\n".join(
        f'        Group {{ Anchor: (Left: {round(x)}, Top: {round(y)}, Width: {w}, Height: {h}); '
        f'Background: (TexturePath: "ChapShard{n:02d}.png"); }}'
        for n, w, h, x, y in SHARDS)
    tint_shards = "\n".join(
        f'      Group #ChapCardShards{i}{t} {{\n        Anchor: (Left: 0, Top: 0, Width: {FRAME_W}, Height: {FRAME_H});\n        Visible: false;\n'
        + "\n".join(f'        Group {{ Anchor: (Left: {round(x)}, Top: {round(y)}, Width: {w}, Height: {h}); '
                    f'Background: (TexturePath: "ChapShard{n:02d}{t}.png"); }}' for n, w, h, x, y in SHARDS)
        + '\n      }'
        for t in TINTS)
    tint_groups = "\n".join(
        f'      Group #ChapFrame{k}{i}{t} {{ Anchor: (Left: 0, Top: 0, Width: {FRAME_W}, Height: {FRAME_H}); '
        f'Background: (TexturePath: "ChapFrame{"Default" if k == "D" else "Selected"}{t}.png"); Visible: false; }}'
        for t in TINTS for k in ("D", "S"))
    return f'''    Button #ChapCard{i} {{
      Anchor: (Left: {x + FRAME_DX}, Top: {CARD_T + FRAME_DY}, Width: {FRAME_W}, Height: {FRAME_H});
      Style: (
        Default: (Background: #000000(0)),
        Hovered: (Background: (TexturePath: "ChapCardHover.png")),
        Pressed: (Background: (TexturePath: "ChapCardHover.png", Color: #ffffff(0.6))),
        Sounds: $C.@ButtonSounds
      );
{arts}
      Group #ChapCardShade{i} {{ Anchor: (Left: {ART_DX - FRAME_DX}, Top: {ART_DY - FRAME_DY + ART_H - 165}, Width: {ART_W}, Height: 165); Background: (TexturePath: "ChapCardShade.png"); }}
      Group #ChapFrameL{i} {{ Anchor: (Left: 0, Top: 0, Width: {FRAME_W}, Height: {FRAME_H}); Background: (TexturePath: "ChapFrameLocked.png"); Visible: false; }}
      Group #ChapFrameLS{i} {{ Anchor: (Left: 0, Top: 0, Width: {FRAME_W}, Height: {FRAME_H}); Background: (TexturePath: "ChapFrameDefault.png"); Visible: false; }}
      Group #ChapFrameD{i} {{ Anchor: (Left: 0, Top: 0, Width: {FRAME_W}, Height: {FRAME_H}); Background: (TexturePath: "ChapFrameDefault.png"); }}
      Group #ChapFrameS{i} {{ Anchor: (Left: 0, Top: 0, Width: {FRAME_W}, Height: {FRAME_H}); Background: (TexturePath: "ChapFrameSelected.png"); Visible: false; }}
{tint_groups}
      Group #ChapCardShards{i} {{
        Anchor: (Left: 0, Top: 0, Width: {FRAME_W}, Height: {FRAME_H});
        Visible: false;
{shard_groups}
      }}
{tint_shards}
      Group #ChapCardBadge{i} {{ Anchor: (Left: {FRAME_W // 2 - BADGE // 2}, Top: 20, Width: {BADGE}, Height: {BADGE}); Background: (TexturePath: "ChapRankBadge.png"); }}
      Label #ChapCardPlus{i} {{
        Anchor: (Left: 0, Top: {FRAME_H // 2 - 90}, Width: {FRAME_W}, Height: 120);
        Text: "+";
        Visible: false;
        Style: (FontName: "Secondary", FontSize: 110, RenderBold: true, TextColor: #c58643, HorizontalAlignment: Center, VerticalAlignment: Center);
      }}
      Label #ChapCardRank{i} {{
        Anchor: (Left: {FRAME_W // 2 - BADGE // 2}, Top: 20, Width: {BADGE}, Height: {BADGE});
        Style: (FontName: "Secondary", FontSize: 24, RenderBold: true, TextColor: #ffe98a, HorizontalAlignment: Center, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 14);
      }}
      Group #ChapCardLock{i} {{ Anchor: (Left: {FRAME_W // 2 - 28}, Top: {FRAME_H // 2 - 60}, Width: 56, Height: 56); Background: (TexturePath: "ChapLock.png"); Visible: false; }}
      Label #ChapCardTitle{i} {{
        Anchor: (Left: 30, Top: {ART_DY - FRAME_DY + ART_H - 92}, Width: {FRAME_W - 60}, Height: 36);
        Style: (FontName: "Secondary", FontSize: 21, RenderBold: true, RenderUppercase: true, TextColor: {GOLD_TITLE}, HorizontalAlignment: Center, VerticalAlignment: Center);
      }}
      Label #ChapCardState{i} {{
        Anchor: (Left: 30, Top: {ART_DY - FRAME_DY + ART_H - 54}, Width: {FRAME_W - 60}, Height: 20);
        Style: (FontSize: 13, RenderBold: true, RenderUppercase: true, TextColor: #d1dae6, HorizontalAlignment: Center, VerticalAlignment: Center);
      }}
    }}'''


def note(i):
    return f'''        Group #ChapNote{i} {{
          Anchor: (Top: 12);
          LayoutMode: Left;
          Visible: false;
          Group {{ Anchor: (Width: 24); LayoutMode: Top; Group {{ Anchor: (Width: 24, Height: 24); Background: (TexturePath: "ChapBullet.png"); }} }}
          Group {{
            FlexWeight: 1;
            LayoutMode: Top;
            Padding: (Left: 12, Top: 2);
            Label #ChapNoteText{i} {{ Style: (FontSize: 15, RenderBold: true, TextColor: {TEXT2}, Wrap: true); }}
          }}
        }}'''


def objective(i):
    return f'''        Group #ChapObj{i} {{
          Anchor: (Top: 8, Height: 26);
          LayoutMode: Left;
          Visible: false;
          Group {{
            Anchor: (Width: 22, Height: 22, Top: 2);
            Group #ChapObjOff{i} {{ Anchor: (Left: 0, Top: 0, Width: 22, Height: 22); Background: (TexturePath: "ChapCheckOff.png"); }}
            Group #ChapObjOn{i} {{ Anchor: (Left: 0, Top: 0, Width: 22, Height: 22); Background: (TexturePath: "ChapCheckOn.png"); Visible: false; }}
          }}
          Label #ChapObjText{i} {{
            FlexWeight: 1;
            Anchor: (Left: 12);
            Style: (FontSize: 16, RenderBold: true, TextColor: #b8c2d0, VerticalAlignment: Center);
          }}
          Label #ChapObjCount{i} {{
            Anchor: (Width: 110);
            Style: (FontSize: 15, RenderBold: true, TextColor: {TEXT2}, HorizontalAlignment: End, VerticalAlignment: Center);
          }}
        }}'''


def reward(j):
    x = REW_L + j * SLOT_PITCH
    return f'''    Button #ChapRew{j} {{
      Anchor: (Left: {x}, Top: {REW_T + 50}, Width: {SLOT}, Height: {SLOT + 6});
      Style: (Default: (Background: #000000(0)), Hovered: (Background: #000000(0)), Sounds: $C.@ButtonSounds);
      TextTooltipStyle: $C.@DefaultTextTooltipStyle;
      TextTooltipShowDelay: 0.1;
      Group {{ Anchor: (Left: 0, Top: 0, Width: {SLOT}, Height: {SLOT}); Background: (TexturePath: "ChapSlot.png", Border: 23); }}
      ItemIcon #ChapRewIcon{j} {{ Anchor: (Left: 17, Top: 13, Width: 88, Height: 88); }}
      Label #ChapRewXp{j} {{
        Anchor: (Left: 0, Top: 22, Width: {SLOT}, Height: 56);
        Text: "XP";
        Visible: false;
        Style: (FontName: "Secondary", FontSize: 44, RenderBold: true, TextColor: {GOLD_TITLE}, HorizontalAlignment: Center, VerticalAlignment: Center);
      }}
      Label #ChapRewQty{j} {{
        Anchor: (Left: 8, Top: {SLOT - 32}, Width: {SLOT - 18}, Height: 22);
        Style: (FontSize: 16, RenderBold: true, TextColor: #ffffff, HorizontalAlignment: End, VerticalAlignment: Center);
      }}
      Group #ChapRewCheck{j} {{ Anchor: (Left: 48, Top: {SLOT - 21}, Width: 27, Height: 27); Background: (TexturePath: "ChapRewardCheck.png"); Visible: false; }}
    }}'''


def check_ui(ui):
    """Le client refuse tout le fichier a la premiere faute (deconnexion « failed to load CustomUI ») : verifier avant d'ecrire."""
    import re
    assert "{{" not in ui and "}}" not in ui, "accolades doublees dans le .ui"
    assert ui.count("{") == ui.count("}"), "accolades non equilibrees"
    bad = re.findall(r"#[A-Za-z0-9]*_\w*", ui)
    assert not bad, "identifiants avec tiret bas : %s" % bad[:3]


def build_ui():
    cards = "\n".join(card(i) for i in range(CARDS))
    notes = "\n".join(note(i) for i in range(NOTES))
    objs = "\n".join(objective(i) for i in range(OBJECTIVES))
    rewards = "\n".join(reward(j) for j in range(REWARDS))
    em_l, em_t = PAGE_L + 75, PAGE_T
    ui = f'''$C = "../Common.ui";

// Quetes d'un PNJ facon « Chapitres » (quest/chapitres/QuestChaptersPage), generee par scripts/quetes/gen_chapitres_ui.py.
Group {{
  LayoutMode: Middle;
  Background: (TexturePath: "ChapBackground.png");

  Group #ChapPage {{
    Anchor: (Width: 1920, Height: 1080);

    // ---- fond : voile de droite, bande d'encre, lueur, ombre, eclats ----
    Group {{ Anchor: (Left: 1003, Top: 0, Width: 917, Height: 1080); Background: #181822(0.5); }}
    Group {{ Anchor: (Left: 499, Top: 0, Width: 504, Height: 1080); Background: (TexturePath: "ChapSeparator.png"); }}
    Group #ChapGlows {{ Anchor: (Left: 480, Top: 25, Width: 488, Height: 1055); Background: (TexturePath: "ChapGlows.png"); }}
    Group {{ Anchor: (Left: 615, Top: 194, Width: 391, Height: 736); Background: (TexturePath: "ChapShadow.png"); }}
    Group {{ Anchor: (Left: 0, Top: 0, Width: 1920, Height: 1080); Background: (TexturePath: "ChapShards.png"); }}

    // ---- carrousel ----
{cards}
    Button #ChapPrev {{
      Anchor: (Left: {CARD_L0 - 58}, Top: {CARD_T + CARD_H // 2 - 40}, Width: 40, Height: 80);
      Visible: false;
      Style: (Default: (Background: #000000(0)), Hovered: (Background: #000000(0)), Sounds: $C.@ButtonSounds);
      Group {{ Anchor: (Left: 0, Top: 0, Width: 40, Height: 80); Background: (TexturePath: "ChapArrowLeft.png"); }}
    }}
    Button #ChapNext {{
      Anchor: (Left: {CARD_L0 + CARDS * CARD_PITCH - 14}, Top: {CARD_T + CARD_H // 2 - 40}, Width: 40, Height: 80);
      Visible: false;
      Style: (Default: (Background: #000000(0)), Hovered: (Background: #000000(0)), Sounds: $C.@ButtonSounds);
      Group {{ Anchor: (Left: 0, Top: 0, Width: 40, Height: 80); Background: (TexturePath: "ChapArrowRight.png"); }}
    }}

    // ---- actions (provisoires) ----
    Group {{
      Anchor: (Left: {CARD_L0}, Top: 948, Width: {CARDS * CARD_PITCH - 32}, Height: 48);
      LayoutMode: Center;
      Group #ChapActionBox {{
        LayoutMode: Left;
        $C.@TextButton #ChapAction {{
          @Anchor = Anchor(MinWidth: 260, Right: 16);
          Text: "Accepter";
        }}
        $C.@SecondaryTextButton #ChapAlt {{
          @Anchor = (MinWidth: 180, Right: 16);
          Text: "Autre quête";
          Visible: false;
        }}
        $C.@SecondaryTextButton #ChapFilter {{
          @Anchor = (MinWidth: 240, Right: 16);
          Text: "Catégorie : toutes";
          Visible: false;
        }}
      }}
      $C.@SecondaryTextButton #ChapClose {{
        @Anchor = (MinWidth: 200);
        Text: "Fermer";
      }}
    }}
    Label #ChapHint {{
      Anchor: (Left: {CARD_L0}, Top: 1004, Width: {CARDS * CARD_PITCH - 32}, Height: 24);
      Style: (FontSize: 15, RenderBold: true, TextColor: #8a96a8, HorizontalAlignment: Center, VerticalAlignment: Center);
    }}
    Label {{
      Anchor: (Left: 40, Top: 1028, Width: 300, Height: 24);
      Text: "Échap  ·  Retour";
      Style: (FontSize: 15, RenderBold: true, RenderUppercase: true, TextColor: #77879d, VerticalAlignment: Center);
    }}

    Label #ChapEmpty {{
      Anchor: (Left: {PAGE_L}, Top: 500, Width: {PANEL_W}, Height: 40);
      Text: "Aucune quête pour le moment.";
      Visible: false;
      Style: (FontSize: 20, RenderBold: true, TextColor: {TEXT2}, HorizontalAlignment: Center);
    }}

    // ---- detail de l'acte choisi ----
    Group #ChapDetail {{
      Anchor: (Left: 0, Top: 0, Width: 540, Height: 1080);

      Group {{ Anchor: (Left: {em_l + 27}, Top: {em_t + 9}, Width: 216, Height: 216); Background: (TexturePath: "ChapEmblemFrame.png"); }}
      Group {{ Anchor: (Left: {em_l + 65}, Top: {em_t + 44}, Width: 140, Height: 140); Background: (TexturePath: "ChapEmblemBack.png"); }}
      Label #ChapRank {{
        Anchor: (Left: {em_l + 65}, Top: {em_t + 44 + 8}, Width: 140, Height: 104);
        Style: (FontName: "Secondary", FontSize: 76, RenderBold: true, TextColor: #ffe98a, HorizontalAlignment: Center, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 44);
      }}

      Label #ChapMission {{
        Anchor: (Left: {TEXT_L}, Top: {PAGE_T + 140}, Width: {TEXT_W}, Height: 22);
        Style: (FontSize: 15, RenderBold: true, RenderUppercase: true, TextColor: #b9a2d8, HorizontalAlignment: Center, VerticalAlignment: Center);
      }}
      Label #ChapChain {{
        Anchor: (Left: {TEXT_L}, Top: {PAGE_T + 166}, Width: {TEXT_W}, Height: 22);
        Style: (FontSize: 14, RenderBold: true, RenderUppercase: true, TextColor: #8a96a8, HorizontalAlignment: Center, VerticalAlignment: Center);
      }}
      Label #ChapTitle {{
        Anchor: (Left: {TEXT_L}, Top: {PAGE_T + 192}, Width: {TEXT_W}, Height: 46);
        Style: (FontName: "Secondary", FontSize: 30, RenderBold: true, RenderUppercase: true, TextColor: {GOLD_TITLE}, HorizontalAlignment: Center, VerticalAlignment: Center);
      }}
      Label #ChapTagline {{
        Anchor: (Left: {TEXT_L}, Top: {PAGE_T + 244}, Width: {TEXT_W}, Height: 32);
        Style: (FontSize: 24, RenderBold: true, TextColor: {TAGLINE}, HorizontalAlignment: Center, VerticalAlignment: Center);
      }}
      Group {{ Anchor: (Left: {PAGE_L}, Top: {PAGE_T + 290}, Width: 418, Height: 8); Background: (TexturePath: "ChapDivider.png", HorizontalBorder: 24, VerticalBorder: 0); }}

      Group #ChapBody {{
        Anchor: (Left: {TEXT_L}, Top: {PAGE_T + 308}, Width: {TEXT_W + 14}, Height: 372);
        LayoutMode: TopScrolling;
        ScrollbarStyle: $C.@DefaultScrollbarStyle;
        Padding: (Right: 14);

        Label #ChapStory {{
          Style: (FontSize: 16, RenderBold: true, TextColor: #b8c2d0, Wrap: true);
        }}
{notes}
        Label #ChapObjHead {{
          Anchor: (Top: 20, Height: 24);
          Text: "Attendus";
          Style: (FontName: "Secondary", FontSize: 20, RenderBold: true, RenderUppercase: true, TextColor: {HEAD}, VerticalAlignment: Center);
        }}
        Group {{ Anchor: (Top: 4, Height: 1); Background: #48455c; }}
{objs}
      }}

      Group #ChapRewards {{
        Anchor: (Left: 0, Top: 0, Width: 540, Height: 1080);
        Label {{
          Anchor: (Left: {REW_L}, Top: {REW_T}, Width: 414, Height: 32);
          Text: "Récompenses";
          Style: (FontName: "Secondary", FontSize: 24, RenderBold: true, RenderUppercase: true, TextColor: {HEAD}, HorizontalAlignment: Center, VerticalAlignment: Center);
        }}
        Group #ChapTick0 {{ Anchor: (Left: {REW_L + 126}, Top: {REW_T + 50 + SLOT // 2 - 1}, Width: 8, Height: 2); Background: #d9d9d9(0.32); }}
        Group #ChapTick1 {{ Anchor: (Left: {REW_L + 264}, Top: {REW_T + 50 + SLOT // 2 - 1}, Width: 8, Height: 2); Background: #d9d9d9(0.32); }}
{rewards}
        Button #ChapRewPrev {{
      Anchor: (Left: {REW_L - 30}, Top: {REW_T + 50 + SLOT // 2 - 20}, Width: 24, Height: 40);
      Visible: false;
      Style: (Default: (Background: #000000(0)), Hovered: (Background: #000000(0)), Sounds: $C.@ButtonSounds);
      Group {{ Anchor: (Left: 0, Top: 0, Width: 24, Height: 40); Background: (TexturePath: "ChapRewArrowLeft.png"); }}
    }}
        Button #ChapRewNext {{
      Anchor: (Left: {REW_L + 3 * SLOT_PITCH - 10}, Top: {REW_T + 50 + SLOT // 2 - 20}, Width: 24, Height: 40);
      Visible: false;
      Style: (Default: (Background: #000000(0)), Hovered: (Background: #000000(0)), Sounds: $C.@ButtonSounds);
      Group {{ Anchor: (Left: 0, Top: 0, Width: 24, Height: 40); Background: (TexturePath: "ChapRewArrowRight.png"); }}
    }}
      }}
    }}
  }}
}}
'''
    check_ui(ui)
    path = os.path.join(OUT, "Chapitres.ui")
    with open(path, "w", encoding="utf-8") as f:
        f.write(ui)
    print("ecrit", path)


def planche(names, path):
    """Planche de previsualisation : illustrations couleur cote a cote (fond gris), a montrer a l'utilisateur."""
    imgs = [read_any(os.path.join(OUT, f"ChapArt{n}C@2x.png")) for n in names]
    W = sum(i[0] for i in imgs) + 10 * (len(imgs) - 1)
    out = [[[30, 30, 30, 255] for _ in range(W)] for _ in range(AH)]
    ox = 0
    for w, h, px in imgs:
        for y in range(h):
            for x in range(w):
                if px[y][x][3] > 0: out[y][ox + x] = px[y][x]
        ox += w + 10
    png.write(path, W, AH, out)
    print("planche", path)


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build_ui()
    if "--ui" in sys.argv:
        sys.exit(0)
    if "--tints" in sys.argv:                                         # cadres et eclats teintes seulement (2026-11-19)
        tinted_frames()
        print("cadres teintes ecrits dans", OUT)
        sys.exit(0)
    only = [a.split("=", 1)[1] for a in sys.argv if a.startswith("--art=")]   # --art=Trone : une seule illustration
    if only:
        m = art_mask()
        for t in only:
            paint_art(t, m)
            print("illustration", t)
        sheet = [a.split("=", 1)[1] for a in sys.argv if a.startswith("--planche=")]
        if sheet:
            planche(only, sheet[0])
        sys.exit(0)
    client_images()
    tinted_frames()
    separator()
    small_images()
    m = art_mask()
    card_shade(m)
    card_hover()
    for t in ARTS:
        paint_art(t, m)
        print("illustration", t)
    print("images ecrites dans", OUT)
