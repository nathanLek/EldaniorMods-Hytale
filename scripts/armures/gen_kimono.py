"""Kimonos d'Eldanior : trois tenues de 4 pieces, generees entierement ici (modeles, textures, icones, items).

  Pauvre    Commune     chanvre rapiece et dechire, corde, kasa troue, waraji de paille
  Sakura    Epique      soie indigo teintee vers le prune, branches de cerisier, obi cramoisi, kasa laque
  Empereur  Legendaire  brocart d'or aux chrysanthemes, cols superposes, kanmuri, traine aux plumes de phenix

Les IDs de Sakura restent Armor_Kimono_<Piece> (deja deployes) ; les autres sont Armor_Kimono_<Tenue>_<Piece>.

Regles du rig (mesurees sur les 47 modeles d'armure vanilla) :
  - un noeud "isPiece" se colle a l'os du joueur de meme nom : sa position et son offset propres sont ignores en jeu ;
  - ses enfants sont places par rapport au CENTRE DE FORME DE L'OS DU JOUEUR (pivot + offset de Player.blockymodel).
Les positions des pieces reprennent celles de l'armure de soie vanilla : elles servent au rendu de l'objet au sol.
Face avant = +Z. Cote droit du porteur = -X. Le kimono se croise gauche sur droite : le pan gauche du porteur (+X) passe
dessus (l'inverse est reserve aux defunts). Cote gauche = miroir du droit (etirement x negatif, meme texture).

Densite : 2 px par unite (D), soit le double des armures vanilla : une forme de N unites a N*D px et un etirement 1/D.
Les faces laterales gauche et droite partagent leur peinture : le sens de leurs UV n'a pas ete verifie, donc aucun motif
asymetrique avant/arriere sur ces faces.

Usage : python3 scripts/armures/gen_kimono.py <src/main/resources> [dossier_apercu]
"""
import copy, json, math, os, random, sys

from armor_kit import *  # noqa: F401,F403
from armor_kit import write_sets

RES = sys.argv[1]
PREVIEW = sys.argv[2] if len(sys.argv) > 2 else None
MODEL_DIR = "Items/Armors/Kimono"

# Sakura
INDIGO = (36, 44, 104)
INDIGO_LIGHT = (84, 98, 170)
PLUM = (110, 48, 118)
SAKURA = (252, 202, 218)
SAKURA_DEEP = (232, 128, 166)
BARK = (74, 44, 40)
SLATE = (62, 60, 80)
SLATE_LIGHT = (104, 102, 128)
# Pauvre
HEMP = (146, 122, 88)
HEMP_DARK = (104, 84, 60)
PATCHES = ((76, 84, 104), (98, 72, 50), (124, 66, 50), (112, 112, 88))
# Empereur
ROYAL = (204, 150, 52)
ROYAL_LIGHT = (246, 208, 112)
ROYAL_DARK = (140, 92, 30)
PURPLE = (72, 30, 110)
PURPLE_LIGHT = (130, 74, 170)
JADE = (70, 190, 150)



# ---------------------------------------------------------------------------------------------------------------------
# Motifs
# ---------------------------------------------------------------------------------------------------------------------
def flower(img, cx, cy, r, rot, pale=SAKURA, deep=SAKURA_DEEP, heart=GOLD):
    """Fleur de cerisier a 5 petales echancres."""
    ir = int(r) + 2
    for dy in range(-ir, ir + 1):
        for dx in range(-ir, ir + 1):
            d = math.hypot(dx, dy)
            a = math.atan2(dy, dx) - rot
            e = abs(((a * 5 / (2 * math.pi)) % 1.0) - 0.5) * 2          # 0 au milieu d'un petale, 1 entre deux petales
            edge = r * (0.6 + 0.4 * (1 - e * e))
            if e < 0.14:
                edge *= 0.84                                             # echancrure du bout du petale
            if d <= edge:
                c = mix(deep, pale, min(1.0, d / max(0.1, edge) * 1.5))
                if e > 0.84:
                    c = mix(c, deep, 0.8)
                put(img, cx + dx, cy + dy, heart if d < r * 0.24 else c)


def sprig(img, x, y, angle, length, rnd, thick=1.0):
    """Branche de cerisier : tige sinueuse, rameaux et fleurs le long du trajet."""
    pts = [(x, y)]
    for i in range(int(length / 5)):
        angle += rnd.uniform(-0.42, 0.42)
        x, y = x + math.cos(angle) * 5, y + math.sin(angle) * 5
        pts.append((x, y))
        if i % 2 == 1 and thick > 0.55:
            sprig(img, x, y, angle + rnd.choice((-1, 1)) * rnd.uniform(0.6, 1.0), length * 0.42, rnd, thick * 0.6)
    stroke(img, pts, BARK, thick)
    for k, (px, py) in enumerate(pts):
        if k and (k % 2 == 0 or k == len(pts) - 1):
            flower(img, px + rnd.uniform(-2, 2), py + rnd.uniform(-2, 2), rnd.uniform(3.4, 5.2), rnd.uniform(0, 6.28))


def petals(img, seed, top, bottom):
    rnd = random.Random(seed)
    h, w = len(img), len(img[0])
    for _ in range(int(w * h / 240)):
        x, y = rnd.uniform(0, w), rnd.uniform(0, h)
        if rnd.random() < top + (bottom - top) * y / h:
            a = rnd.uniform(0, 6.28)
            c = rnd.choice((SAKURA, SAKURA, SAKURA_DEEP))
            stroke(img, [(x, y), (x + math.cos(a) * 2.2, y + math.sin(a) * 2.2)], c, 1.0)
            put(img, x, y, tuple(v * 0.8 for v in c))


def seigaiha(x, y, s=14):
    """Vagues : anneaux superposes, la rangee du bas recouvre celle du haut. Vrai sur un trait."""
    r = s / 2.0
    base = int(y // (s / 4.0))
    for j in range(base + 3, base - 4, -1):
        cy = j * s / 4.0
        cx0 = s / 2.0 if j % 2 else 0.0
        cx = cx0 + round((x - cx0) / s) * s
        d = math.hypot(x - cx, y - cy)
        if d <= r and y <= cy:
            return int(d) % 3 == 0
    return False


def wave_band(img, y0, y1, ground, line):
    w = len(img[0])
    for y in range(int(y0), min(len(img), int(y1))):
        for x in range(w):
            c = line if seigaiha(x, y - y0) else ground
            img[y][x][:3] = list(c)


def kiku(img, cx, cy, r, light=GOLD_LIGHT, dark=GOLD_DARK, heart=GOLD):
    """Chrysanthemes : 16 petales rayonnants."""
    ir = int(r) + 1
    for dy in range(-ir, ir + 1):
        for dx in range(-ir, ir + 1):
            d = math.hypot(dx, dy)
            a = math.atan2(dy, dx)
            e = abs(math.cos(a * 8))
            if d <= r * (0.82 + 0.18 * e):
                c = mix(dark, light, 0.25 + 0.75 * e) if d > r * 0.3 else heart
                if e < 0.28 and d > r * 0.3:
                    c = dark
                put(img, cx + dx, cy + dy, c)



def plank_slice(big, y0, y1, inner=CRIMSON_DARK):
    """Une tranche de la grande image big, posee sur la face arriere (exterieur de la traine)."""
    def paint(face, w, h, seed):
        if face != "back":
            return finish(canvas(w, h, inner), face, 0.9, edges=False)
        bw = len(big[0])
        img = []
        for y in range(h):
            sy = int(y0 + (y1 - y0) * (y + 0.5) / h)
            row = []
            for x in range(w):
                row.append(list(big[min(len(big) - 1, sy)][int((x + 0.5) * bw / w)]))
            img.append(row)
        return finish(img, "front", 1.0, edges=False)
    return paint


def pleats(ground, light, period=10, hem_c=None, flowers=0.0):
    def paint(face, w, h, seed):
        img = canvas(w, h, ground)
        if face not in ("top", "bottom"):
            for y in range(h):
                for x in range(w):
                    p = x % period
                    t = p / float(period - 1)
                    c = mix(tuple(v * 0.66 for v in ground), light, t * t)
                    if p == 0:
                        c = tuple(v * 0.44 for v in ground)              # creux du pli
                    img[y][x][:3] = list(c)
            if flowers:
                petals(img, seed, 0.0, flowers)
            if hem_c:
                band(img, h - 6, h - 4, hem_c)
        return finish(img, face)
    return paint



# ---------------------------------------------------------------------------------------------------------------------
# Elements communs
# ---------------------------------------------------------------------------------------------------------------------
CROSS_Y = 73.5    # croisement des pans, hauteur dans le corps
TOP_Y = 91.0      # haut du torse
SLOPE = 0.52      # decalage en x par unite de hauteur


def v_opening(img, layers):
    """Peint l'ouverture en V du col sur la face avant du torse. layers = [(largeur en unites, couleur), ...] du bord vers le centre."""
    h, w = len(img), len(img[0])
    for y in range(h):
        wy = TOP_Y - (y + 0.5) / D
        if wy <= CROSS_Y:
            continue
        half = (wy - CROSS_Y) * SLOPE
        for x in range(w):
            mx = (x - w / 2.0 + 0.5) / D
            if abs(mx) < half:
                t = half - abs(mx)
                acc = 0.0
                for width, c in layers:
                    acc += width
                    if t < acc:
                        img[y][x][:3] = list(c)
                        break
    return img


def lapels(m, paint_over, paint_under, width):
    """Pans du col en relief, poses sur le torse ; le pan gauche du porteur passe dessus."""
    angle = -math.degrees(math.atan(SLOPE))
    cos = math.cos(math.radians(angle))
    shift = width / 2.0 / cos
    y_low, y_high = 63.0, TOP_Y + 1
    c = (y_low + y_high) / 2.0
    over = box(m, "Col_Dessus", ((c - CROSS_Y) * SLOPE + shift, c - 79, 12.8), (width, (y_high - y_low) / cos, 1.5), paint_over, q=q_axis((0, 0, 1), angle))
    y_low = CROSS_Y - 1
    c = (y_low + y_high) / 2.0
    under = box(m, "Col_Dessous", (-(c - CROSS_Y) * SLOPE - shift, c - 79, 12.4), (width, (y_high - y_low) / cos, 1), paint_under, q=q_axis((0, 0, 1), -angle))
    return [over, under]


def neck_band(m, paint, y=12.2):
    side = box(m, "R-Col_Epaule", (-11, y, -1), (4, 2.5, 19), paint)
    back = box(m, "Col_Nuque", (0, y + 0.5, -11.4), (25, 3.5, 3), paint)
    return sym(m, side) + [back]


def cone_panels(m, count, apothem, base_y, apex_y, paint_panel):
    """Cone a count pans : quads triangulaires decoupes en alpha, qui se recouvrent pour ne laisser aucun jour."""
    rise = apex_y - base_y
    slant = math.hypot(apothem, rise)
    w_u = 2 * apothem * math.tan(math.pi / count) + 1.5
    W = int(math.ceil(w_u * D))
    W += W % 2
    H = int(round(slant * D))
    tilt = math.degrees(math.atan2(apothem, rise))
    nodes = []
    for k in range(count):
        yaw = k * 360.0 / count
        light = 0.86 + 0.2 * math.cos(math.radians(yaw - 30))
        qy = q_axis((0, 1, 0), yaw)
        pos = qrot(qy, (0, (base_y + apex_y) / 2.0, apothem / 2.0))
        img = paint_panel(k, W, H, light)
        for y in range(H):
            half = W / 2.0 * (y + 0.5) / H + 1.5
            for x in range(W):
                if abs(x - W / 2.0 + 0.5) > half:
                    img[y][x][3] = 0
        nodes.append(quad(m, "Cone_%d" % k, pos, img, qmul(qy, q_axis((1, 0, 0), -tilt))))
    return nodes


def cords_head(m, paint):
    side = rod(m, "R-Cordon", (-15.7, 13, 6), (-15.7, -14.8, 8), 1, paint)
    chin = box(m, "Cordon_Menton", (0, -14.8, 8), (31.4, 1, 1), paint)
    return sym(m, side) + [chin]


def thong(m, c=CRIMSON):
    """Lanieres de sandale en V, du gros orteil vers les cotes du pied."""
    paint = plain(c)
    toe = (-2.5, 5, 8.5)
    return [rod(m, "R-Lanière_A", toe, (-8, 2.2, 0.5), 1, paint), rod(m, "R-Lanière_B", toe, (7, 2.2, 0.5), 1, paint)]


def tabi_paint(face, w, h, seed):
    img = cloth(w, h, IVORY, seed, 0.06, 0.02)
    if face == "front":
        rect(img, w // 3, 0, w // 3 + 1, h, IVORY_SHADE)
    if face in ("left", "right"):
        stitch_line(img, [(2, h - 2), (w - 2, h - 2)], IVORY_SHADE, 2)
    return finish(img, face, edges=False)


def straw_paint(c0=STRAW, c1=STRAW_DARK, light=0.95):
    def paint(face, w, h, seed):
        img = canvas(w, h, c0)
        for y in range(h):
            for x in range(w):
                t = ((x * 2 + y * 3) % 7) / 6.0
                img[y][x][:3] = list(mix(c0, c1, t * 0.8))
        return finish(img, face, light, edges=False)
    return paint


# ---------------------------------------------------------------------------------------------------------------------
# Sakura
# ---------------------------------------------------------------------------------------------------------------------
def sakura_ground(v):
    return mix(INDIGO, PLUM, smooth((v - 0.25) / 0.75) * 0.75)


def sakura_cloth(w, h, seed, top=0.1, bottom=0.5, hem=0, branch=0, y0=0.0, y1=1.0):
    """Soie indigo teintee vers le prune, comme une teinture en degrade ; y0-y1 situe la face sur la hauteur du vetement."""
    img = cloth(w, h, lambda v: sakura_ground(y0 + (y1 - y0) * v), seed, 0.18, 0.03)
    rnd = random.Random(seed)
    petals(img, seed + 1, top * 0.6, bottom * 0.7)
    for i in range(branch):
        side = i % 2
        sprig(img, w * (0.04 if side == 0 else 0.96), h - hem - 2, -math.pi / 2 + (0.5 if side == 0 else -0.5), min(h, w) * rnd.uniform(0.9, 1.3), rnd, 1.1)
    if hem:
        wave_band(img, h - hem, h, tuple(v * 0.72 for v in PLUM), mix(PLUM, GOLD, 0.6))
        band(img, h - hem - 2, h - hem, GOLD)
    return img


def sakura_body(hem=0, top=0.1, bottom=0.5, branch=0, y0=0.0, y1=1.0, light=1.0, opening=False):
    def paint(face, w, h, seed):
        if face == "bottom" and opening:                         # ouverture de la manche : doublure ivoire, interieur sombre
            img = canvas(w, h, LINING)
            border(img, 0, CRIMSON)
            border(img, 1, IVORY)
            border(img, 2, IVORY)
            return finish(img, face, 1.3, edges=False)
        if face in ("top", "bottom"):
            return finish(cloth(w, h, INDIGO, seed, 0.08, 0.02), face, light)
        return finish(sakura_cloth(w, h, seed, top, bottom, hem, branch, y0, y1), face, light)
    return paint


def sakura_mon(img, cx, cy, r):
    disc(img, cx, cy, r, GOLD)
    disc(img, cx, cy, r - 1.5, INDIGO)
    flower(img, cx, cy, r - 2, -math.pi / 2, GOLD_LIGHT, GOLD, CRIMSON)


def sakura_torso(face, w, h, seed):
    img = sakura_cloth(w, h, seed, 0.12, 0.3, branch=2 if face in ("front", "back") else 0, y0=0.0, y1=0.3)
    if face == "front":
        v_opening(img, [(1.2, IVORY), (1.2, SAKURA_DEEP), (40, CRIMSON_DARK)])
    elif face == "back":
        sakura_mon(img, w / 2.0, h * 0.3, 7)
    elif face == "top":
        img = cloth(w, h, INDIGO, seed, 0.06, 0.02)
        for y in range(h):
            for x in range(w):
                d = math.hypot((x - w / 2.0 + 0.5) / (10.0 * D), (y - h / 2.0 + 0.5) / (8.0 * D))
                if d < 1:
                    img[y][x][:3] = list(CRIMSON_DARK)
    elif face == "bottom":
        img = canvas(w, h, INDIGO)
    return finish(img, face)


def sakura_hips(face, w, h, seed):
    img = sakura_cloth(w, h, seed, 0.3, 0.45, y0=0.45, y1=0.65)
    scale_rows(img, 0, 5, 0.72)                                  # ombre portee de l'obi
    if face == "front":
        for y in range(h):
            x = int(w / 2 - 14 - y * 0.2)
            rect(img, x, y, x + 2, y + 1, GOLD)
            rect(img, x - 2, y, x, y + 1, (20, 18, 44))
    return finish(img, face)


def sakura_obi(face, w, h, seed):
    img = canvas(w, h, CRIMSON)
    if face not in ("top", "bottom"):
        lozenges(img, 3, h - 3, 8, (206, 70, 80))
        for y in range(4, h - 4, 8):
            for x in range((y // 8) % 2 * 8, w, 16):
                disc(img, x, y, 1.1, GOLD)
        for y in (2, 3, h - 4, h - 3):
            band(img, y, y + 1, GOLD)
        band(img, 0, 2, CRIMSON_DARK)
        band(img, h - 2, h, CRIMSON_DARK)
    return finish(img, face)


def sakura_taiko(face, w, h, seed):
    img = canvas(w, h, CRIMSON)
    if face not in ("top", "bottom"):
        lozenges(img, 0, h, 8, (206, 70, 80), 0.6)
        border(img, 2, GOLD)
        border(img, 3, GOLD_DARK)
    if face == "back":
        cx, cy, r = w / 2.0, h / 2.0, min(w, h) / 2.0 - 5
        disc(img, cx, cy, r, GOLD)
        disc(img, cx, cy, r - 1.5, CRIMSON_DARK)
        flower(img, cx, cy, r - 2, -math.pi / 2, GOLD_LIGHT, GOLD, CRIMSON)
    return finish(img, face)


def sakura_lacquer_panel(k, W, H, light):
    img = canvas(W, H, (30, 28, 58))
    for y in range(H):
        t = y / float(H)
        s = max(0.0, math.sin(t * math.pi * 1.1)) ** 3
        img[y] = [[*mix((22, 22, 50), (70, 76, 130), s * 0.5 * light), 255] for _ in range(W)]
    if k % 4 == 0:                                               # branche fleurie sur quatre pans
        rnd = random.Random(k + 5)
        sprig(img, W / 2.0, H - 8, -math.pi / 2, H * 0.55, rnd, 0.8)
    band(img, H - 5, H - 2, GOLD)
    band(img, H - 2, H, GOLD_DARK)
    scale = light
    for row in img:
        for p in row:
            for i in range(3):
                p[i] = clamp(p[i] * scale)
    return img


def build_sakura_head():
    m = Model("Head")
    kids = cone_panels(m, 16, 31.0, 9.5, 27.5, sakura_lacquer_panel)     # coins de la tete (r 21, y 14) sous le cone
    kids.append(box(m, "Couronne", (0, 13.5, 0), (24, 2, 24), plain(CRIMSON)))
    kids.append(box(m, "Fleuron", (0, 27.5, 0), (5, 2, 5), metal()))
    kids.append(box(m, "Fleuron_Haut", (0, 29, 0), (2.5, 2, 2.5), metal(GOLD_LIGHT)))
    cord = plain(CRIMSON)
    kids += cords_head(m, cord)
    tassel = box(m, "R-Pompon", (-15.7, -17.5, 8), (2.5, 5, 2.5), plain(GOLD))
    kids += sym(m, tassel)
    return m, [piece(m, "Head", (0, 0, 0), kids)]


def build_sakura_chest():
    m = Model("Chest")
    fuki = hem_band(CRIMSON, GOLD)

    def arm():
        tamoto = box(m, "R-Tamoto", (-1, -1, -5.7), (15, 19, 30), sakura_body(12, 0.3, 0.7, branch=2, y0=0.45, y1=0.95, opening=True))
        ring = box(m, "R-Fuki_Manche", (-1, -10.5, -5.7), (16, 2, 31), fuki)
        juban = box(m, "R-Juban", (-1, -11.8, 0), (14, 3, 16), plain(IVORY))
        forearm = piece(m, "R-Forearm", (-1, 0, 0), [tamoto, ring, juban])
        sode = box(m, "R-Sode", (-1, 0.5, -2), (14, 23, 22), sakura_body(0, 0.15, 0.3, branch=1, y0=0.05, y1=0.45))
        return piece(m, "R-Arm", (-17, 0, 0), [sode, forearm])

    def thigh():
        suso = box(m, "R-Suso", (-1, 0.5, 0), (17, 17, 23), sakura_body(12, 0.5, 0.8, branch=1, y0=0.6, y1=1.0))
        ring = box(m, "R-Fuki_Pan", (-1.6, -8, 0), (18, 2, 24), fuki)
        return piece(m, "R-Thigh", (-8, -1, 1), [suso, ring], offset=(0, -9, 0))

    right_arm, right_thigh = arm(), thigh()
    plate = box(m, "Torse", (0, 0, 0), (33, 24, 24), sakura_torso)
    col = stripes([(0.14, GOLD), (0.5, IVORY), (0.36, SAKURA)])
    chest = piece(m, "Chest", (0, 5, 0), [plate] + lapels(m, col, col, 5) + neck_band(m, plain(IVORY)) + [right_arm, mirror(m, right_arm)], offset=(0, 11, 0))

    belly = box(m, "Ventre", (0, 0, 0), (31, 16, 23), sakura_body(0, 0.15, 0.2, y0=0.3, y1=0.45))
    belt = box(m, "Obi", (0, -2, 0), (36, 11, 27), sakura_obi)
    age = box(m, "Obi_Age", (0, 4, 0), (35, 2, 26), plain(SAKURA))
    cord = box(m, "Obijime", (0, -2, 0), (37, 1.5, 28), wraps(IVORY, GOLD))
    knot = box(m, "Noeud", (0, -2, 14.5), (5, 3.5, 2), metal())
    charm = rod(m, "R-Cordon_Breloque", (-11, -5, 13.5), (-11, -12, 14), 0.8, plain(GOLD))
    bead = box(m, "R-Breloque", (-11, -13.5, 14), (3, 3, 3), metal(CRIMSON))
    tassel = box(m, "R-Pompon", (-11, -17, 14), (2.5, 4.5, 2), plain(GOLD))
    bow = box(m, "Musubi", (0, -1, -15), (21, 14, 5), sakura_taiko)
    top = box(m, "Musubi_Pli", (0, 7, -14.5), (17, 3, 4), plain(CRIMSON_DARK))
    wing = box(m, "R-Aile", (-13.5, 0.5, -14), (9, 6, 3), sakura_taiko, q=q_axis((0, 0, 1), 16))
    tail = box(m, "Tare", (0, -11, -14), (15, 9, 2), sakura_taiko)
    node = piece(m, "Belly", (0, 8, 0), [belly, belt, age, cord, knot, charm, bead, tassel, bow, top, wing, mirror(m, wing), tail, chest], offset=(0, 4, 0))

    hip = box(m, "Hanches", (0, -0.5, 0), (34.5, 13, 26), sakura_hips)
    return m, [piece(m, "Pelvis", (0, 12, 0), [hip, right_thigh, mirror(m, right_thigh), node])]


def sakura_tekko(face, w, h, seed):
    img = canvas(w, h, INDIGO)
    if face in ("left", "right", "front", "back"):
        cy, cx, r = h / 2.0, w / 2.0, min(w, h) / 2.0 - 2
        border(img, 0, GOLD)
        if face in ("left", "right") and r > 4:
            sakura_mon(img, cx, cy, r)
    return finish(img, face, edges=False)


def build_sakura_hands():
    m = Model("Hands")
    wrap = box(m, "R-Bandes", (0, -3.5, 0), (10, 9, 14), wraps(edge=CRIMSON))
    glove = box(m, "R-Gant", (0, 2, 0), (11, 8, 15), wraps())
    plate = box(m, "R-Plaque", (-6, 1.5, 0), (2, 8, 11), sakura_tekko)
    lace = box(m, "R-Lacet", (0, -0.8, 0), (11.5, 1, 15.5), plain(CRIMSON))
    hand = piece(m, "R-Hand", (0, -8, 0), [glove, plate, lace], offset=(0, -5, 0))
    right = piece(m, "R-Forearm", (-18, 40, 0), [wrap, hand])
    return m, [right, mirror(m, right)]


def build_sakura_legs():
    m = Model("Legs")
    upper = box(m, "R-Hakama_Haut", (-0.5, -1, 0), (16, 24, 20), pleats(SLATE, SLATE_LIGHT))
    mid = box(m, "R-Hakama_Genou", (-1.3, 5, -0.5), (17.5, 13, 23.5), pleats(SLATE, SLATE_LIGHT, flowers=0.2))
    low = box(m, "R-Hakama_Bas", (-2.6, -6, -1), (20, 11, 28), pleats(SLATE, SLATE_LIGHT, hem_c=GOLD, flowers=0.8))
    ring = box(m, "R-Hakama_Ourlet", (-3.1, -11.2, -1), (21, 1.5, 29), hem_band(CRIMSON, GOLD))
    ankle = box(m, "R-Guetre", (0, -9, 0), (12, 6, 14), wraps())
    calf = piece(m, "R-Calf", (0, -12, 3), [mid, low, ring, ankle], offset=(0, -12, -3))
    thigh = piece(m, "R-Thigh", (-8, -1, 1), [upper, calf], offset=(0, -9, 0))
    sock = box(m, "R-Tabi", (0, 1, 0), (15, 7, 21), tabi_paint)
    sole = box(m, "R-Zori", (-0.6, -3, 0), (16, 1.5, 23), straw_paint())
    edge = box(m, "R-Zori_Semelle", (-0.9, -3.9, 0), (16.5, 0.5, 23.5), plain(CRIMSON_DARK))
    foot = piece(m, "R-Foot", (-7.85, 7, -3), [sock, sole, edge] + thong(m), offset=(0, -3, 6))
    waist = box(m, "Koshi", (0, 0, 0), (29, 11, 23), pleats(SLATE, SLATE_LIGHT))
    return m, [piece(m, "Pelvis", (0, 51, 0), [waist, thigh, mirror(m, thigh)]), foot, mirror(m, foot)]


# ---------------------------------------------------------------------------------------------------------------------
# Pauvre
# ---------------------------------------------------------------------------------------------------------------------
def patch(img, x, y, w, h, c):
    rect(img, x + 1, y + 1, x + w + 1, y + h + 1, (0, 0, 0), 0.3)
    rect(img, x, y, x + w, y + h, c)
    for py in range(int(y), int(y + h)):
        for px in range(int(x), int(x + w)):
            put(img, px, py, tuple(v * (0.88 + 0.24 * noise(px * 0.5, py * 0.5)) for v in c))
    stitch_line(img, [(x + 1, y + 1), (x + w - 1, y + 1), (x + w - 1, y + h - 1), (x + 1, y + h - 1), (x + 1, y + 1)])


def hemp_cloth(w, h, seed, patches=1, tear=0):
    img = cloth(w, h, HEMP, seed, 0.2, 0.09)
    rnd = random.Random(seed)
    for y in range(h):
        for x in range(w):
            s = fbm(x * 0.07 + seed, y * 0.07 - seed)                  # taches d'usure
            if s > 0.56:
                for i in range(3):
                    img[y][x][i] *= 1 - min(0.3, (s - 0.56) * 2.4)
            if y % 7 == 0 and rnd.random() < 0.3:
                put(img, x, y, HEMP_DARK, 0.4)
    for _ in range(patches):
        pw, ph = rnd.randint(int(w * 0.25), int(w * 0.5)) + 2, rnd.randint(int(h * 0.2), int(h * 0.42)) + 2
        patch(img, rnd.randint(1, max(1, w - pw - 2)), rnd.randint(2, max(2, h - ph - 2)), pw, ph, rnd.choice(PATCHES))
    for _ in range(tear):
        x, y = rnd.randint(2, w - 3), rnd.randint(2, h - 6)
        stroke(img, [(x, y), (x + rnd.randint(-2, 2), y + rnd.randint(3, 7))], LINING, 0.6)
    return img


def hemp(patches=1, tear=0, tatters=0):
    def paint(face, w, h, seed):
        if face in ("top", "bottom"):
            img = cloth(w, h, HEMP_DARK, seed, 0.1, 0.06)
            if tatters and face == "bottom":
                for row in img:
                    for p in row:
                        p[3] = 0
            return finish(img, face)
        img = finish(hemp_cloth(w, h, seed, patches, tear), face)
        if tatters:
            torn(img, seed, tatters)
        return img
    return paint


def poor_torso(face, w, h, seed):
    img = hemp_cloth(w, h, seed, 1 if face in ("back", "left") else 0, 1)
    if face == "front":
        v_opening(img, [(1.4, (176, 166, 140)), (40, (70, 56, 44))])
    return finish(img, face)


def worn_lapel(face, w, h, seed):
    img = cloth(w, h, (174, 158, 124), seed, 0.1, 0.09)
    if face in ("front", "back"):
        stitch_line(img, [(2, 0), (2, h)], HEMP_DARK, 3)
        stitch_line(img, [(w - 3, 0), (w - 3, h)], HEMP_DARK, 3)
    return finish(img, face, edges=False)


def rope(face, w, h, seed):
    img = canvas(w, h, STRAW)
    rope_tex(img, STRAW_LIGHT, STRAW_DARK, 7)
    return finish(img, face, edges=False)


def torn_panel(k, W, H, light):
    """Kasa de paille abime : pans ternes, cercle de bord casse, trous."""
    rnd = random.Random(k * 7 + 3)
    img = canvas(W, H, STRAW)
    for y in range(H):
        ring = y % 7
        for x in range(W):
            mx = x - W / 2.0
            c = STRAW_DARK if ring < 2 else STRAW
            if int(mx + y * 0.5) % 9 == 0 and ring > 1:
                c = mix(c, STRAW_DARK, 0.5)
            k2 = (0.55 + 0.5 * fbm(x * 0.08 + k, y * 0.08)) * light * (0.8 + 0.25 * y / H)
            img[y][x][:3] = [clamp(c[0] * k2), clamp(c[1] * k2), clamp(c[2] * k2)]
    for x in range(W):                                           # bord ronge
        cut = int(10 * max(0.0, fbm(x * 0.18 + k * 3, k) - 0.32) * 2.2)
        for y in range(H - cut, H):
            img[y][x][3] = 0
    for _ in range(rnd.randint(0, 2)):                           # trous
        cx, cy, r = rnd.uniform(W * 0.3, W * 0.7), rnd.uniform(H * 0.55, H * 0.85), rnd.uniform(1.5, 3.2)
        for y in range(H):
            for x in range(W):
                d = math.hypot(x - cx, (y - cy) * 0.8)
                if d < r:
                    img[y][x][3] = 0
                elif d < r + 1:
                    img[y][x][:3] = [v * 0.6 for v in img[y][x][:3]]
    return img


def build_poor_head():
    m = Model("Head")
    kids = cone_panels(m, 12, 29.0, 10.0, 28.0, torn_panel)
    kids.append(box(m, "Couronne", (0, 13.5, 0), (24, 2, 24), rope))
    kids += cords_head(m, rope)
    return m, [piece(m, "Head", (0, 0, 0), kids)]


def gourd_paint(face, w, h, seed):
    img = cloth(w, h, (172, 118, 54), seed, 0.1, 0.03)
    return finish(img, face)


def build_poor_chest():
    m = Model("Chest")

    def arm():
        sode = box(m, "R-Sode", (-1, -1, -1), (12, 23, 15), hemp(1, 1, tatters=8), double=True)
        return piece(m, "R-Arm", (-17, 0, 0), [sode])

    def thigh():
        suso = box(m, "R-Suso", (-0.5, 2, 0), (16, 14, 22), hemp(1, 0, tatters=9), double=True)
        return piece(m, "R-Thigh", (-8, -1, 1), [suso], offset=(0, -9, 0))

    right_arm, right_thigh = arm(), thigh()
    plate = box(m, "Torse", (0, 0, 0), (32, 24, 23), poor_torso)
    col = neck_band(m, worn_lapel, 12.1)
    chest = piece(m, "Chest", (0, 5, 0), [plate] + lapels(m, worn_lapel, worn_lapel, 3.5) + col + [right_arm, mirror(m, right_arm)], offset=(0, 11, 0))

    belly = box(m, "Ventre", (0, 0, 0), (30, 16, 22), hemp(0))
    belt = box(m, "Corde", (0, -2, 0), (34.5, 3.5, 27), rope)
    belt2 = box(m, "Corde_2", (0, -5, 0), (34, 3, 26.5), rope)
    knot = box(m, "Noeud", (6, -3, 14), (5, 5, 3), rope)
    end1 = rod(m, "Bout_A", (6, -4, 14.5), (4, -15, 15.5), 1.6, rope)
    end2 = rod(m, "Bout_B", (7, -4, 14.5), (10, -13, 15), 1.6, rope)
    gourd_a = box(m, "Gourde_Panse", (-11, -12, 15.5), (6, 7, 6), gourd_paint)
    gourd_b = box(m, "Gourde_Col", (-11, -6.7, 15.5), (4, 4, 4), gourd_paint)
    gourd_c = box(m, "Gourde_Bouchon", (-11, -3.8, 15.5), (2.5, 2, 2.5), plain(CRIMSON_DARK))
    node = piece(m, "Belly", (0, 8, 0), [belly, belt, belt2, knot, end1, end2, gourd_a, gourd_b, gourd_c, chest], offset=(0, 4, 0))

    hip = box(m, "Hanches", (0, -0.5, 0), (32.5, 13, 25), hemp(1))
    return m, [piece(m, "Pelvis", (0, 12, 0), [hip, right_thigh, mirror(m, right_thigh), node])]


def rag(face, w, h, seed):
    img = canvas(w, h, (172, 154, 118))
    if face not in ("top", "bottom"):
        for y in range(h):
            for x in range(w):
                p = (y + x // 3) % 7
                img[y][x][:3] = list(mix((186, 168, 132), (118, 100, 74), p / 6.0))
        for y in range(h):
            for x in range(w):
                if fbm(x * 0.2 + seed, y * 0.2) > 0.62:
                    put(img, x, y, (92, 74, 54), 0.5)
    return finish(img, face)


def build_poor_hands():
    m = Model("Hands")
    wrap = box(m, "R-Chiffon", (0, -2, 0), (10, 12, 14), rag)
    glove = box(m, "R-Chiffon_Main", (0, 1.5, 0), (11, 7, 15), rag)
    loose = rod(m, "R-Bout", (-5, -1, 7), (-6.5, -7, 8.5), 1.2, rag)
    hand = piece(m, "R-Hand", (0, -8, 0), [glove, loose], offset=(0, -5, 0))
    right = piece(m, "R-Forearm", (-18, 40, 0), [wrap, hand])
    return m, [right, mirror(m, right)]


def build_poor_legs():
    m = Model("Legs")
    upper = box(m, "R-Pantalon", (-0.3, 0, 0), (13.5, 23, 16), hemp(1, 1))
    knee = box(m, "R-Genou", (-0.2, 7.5, 0), (13, 9, 15.5), hemp(1, 0, tatters=7), double=True)
    wrap = box(m, "R-Bande_Tibia", (0, -2.5, 0), (12, 19, 14.5), rag)
    lace = rod(m, "R-Lacet", (-6.2, 3, 7.5), (6.2, -8, 7.5), 0.9, rope)
    calf = piece(m, "R-Calf", (0, -12, 3), [knee, wrap, lace], offset=(0, -12, -3))
    thigh = piece(m, "R-Thigh", (-8, -1, 1), [upper, calf], offset=(0, -9, 0))
    sock = box(m, "R-Pied", (0, 1, 0), (15, 7, 21), rag)
    sole = box(m, "R-Waraji", (-0.6, -3.2, 0), (16, 1.5, 22), straw_paint(STRAW, STRAW_DARK, 0.85))
    foot = piece(m, "R-Foot", (-7.85, 7, -3), [sock, sole] + thong(m, STRAW_DARK), offset=(0, -3, 6))
    waist = box(m, "Koshi", (0, 0, 0), (29, 11, 22), hemp(0))
    return m, [piece(m, "Pelvis", (0, 51, 0), [waist, thigh, mirror(m, thigh)]), foot, mirror(m, foot)]


# ---------------------------------------------------------------------------------------------------------------------
# Empereur
# ---------------------------------------------------------------------------------------------------------------------
def tatewaku(img, ground_light, seed):
    """Tatewaku, le motif de cour : lignes ondulantes qui montent, chrysanthemes dans les renflements."""
    h, w = len(img), len(img[0])
    period, step = 44, 26
    for col in range(-1, w // step + 2):
        x0 = col * step + (seed % 7)
        for y in range(h):
            for sign in (-1, 1):
                x = x0 + sign * (5 + 4 * math.sin(y * 2 * math.pi / period + col * math.pi))
                put(img, x, y, ground_light)
                put(img, x + 1, y, ground_light, 0.5)
        for j in range(-1, h // period + 2):
            cy = j * period + (period / 4.0 if col % 2 == 0 else period * 0.75)
            kiku(img, x0, cy, 5.5, GOLD_LIGHT, ROYAL_DARK, CRIMSON)


def imperial_cloth(w, h, seed, hem=0):
    img = cloth(w, h, ROYAL, seed, 0.2, 0.05)
    tatewaku(img, ROYAL_LIGHT, seed)
    if hem:
        wave_band(img, h - hem, h, CRIMSON_DARK, GOLD)
        band(img, h - hem - 3, h - hem - 1, PURPLE)
        band(img, h - hem - 1, h - hem, GOLD_LIGHT)
    return img


def imperial(hem=0, opening=False, light=1.0):
    def paint(face, w, h, seed):
        if face == "bottom" and opening:
            img = canvas(w, h, LINING)
            border(img, 0, GOLD)
            border(img, 1, CRIMSON)
            border(img, 2, PURPLE)
            return finish(img, face, 1.3, edges=False)
        if face in ("top", "bottom"):
            return finish(cloth(w, h, ROYAL, seed, 0.1, 0.03), face, light)
        return finish(imperial_cloth(w, h, seed, hem), face, light)
    return paint


def emperor_torso(face, w, h, seed):
    img = imperial_cloth(w, h, seed)
    if face == "front":
        v_opening(img, [(1.3, CRIMSON), (1.3, PURPLE), (1.3, IVORY), (40, CRIMSON_DARK)])
    elif face == "back":
        cx, cy = w / 2.0, h * 0.42
        disc(img, cx, cy, 18, GOLD_LIGHT)
        disc(img, cx, cy, 16.5, CRIMSON_DARK)
        kiku(img, cx, cy, 15)
    elif face == "top":
        img = cloth(w, h, ROYAL, seed, 0.08, 0.03)
        for y in range(h):
            for x in range(w):
                if math.hypot((x - w / 2.0 + 0.5) / (10.0 * D), (y - h / 2.0 + 0.5) / (8.0 * D)) < 1:
                    img[y][x][:3] = list(CRIMSON_DARK)
    return finish(img, face)


def emperor_belt(face, w, h, seed):
    """Sekitai : ceinture de laque noire, plaques d'or et de jade."""
    img = canvas(w, h, LACQUER)
    if face not in ("top", "bottom"):
        for x in range(4, w - 8, 18):
            rect(img, x, 3, x + 14, h - 3, GOLD)
            rect(img, x + 2, 5, x + 12, h - 5, ROYAL_DARK)
            disc(img, x + 7, h / 2.0, 3.6, JADE)
            disc(img, x + 6, h / 2.0 - 1, 1.2, (210, 255, 235))
        band(img, 0, 1, GOLD)
        band(img, h - 1, h, GOLD)
    return finish(img, face, edges=False)


def emperor_medallion(face, w, h, seed):
    img = canvas(w, h, CRIMSON_DARK)
    if face not in ("top", "bottom"):
        border(img, 1, GOLD)
        kiku(img, w / 2.0, h / 2.0, min(w, h) / 2.0 - 4)
    return finish(img, face, 1.1, edges=False)


def hirao(face, w, h, seed):
    """Sash d'apparat : ruban pourpre brode, frange d'or."""
    img = cloth(w, h, PURPLE, seed, 0.1, 0.03)
    if face in ("front", "back"):
        rect(img, 0, 0, 2, h, GOLD)
        rect(img, w - 2, 0, w, h, GOLD)
        for y in range(10, h - 12, 18):
            kiku(img, w / 2.0, y, 5, GOLD_LIGHT, GOLD_DARK, CRIMSON)
        for x in range(w):
            if x % 2 == 0:
                rect(img, x, h - 8, x + 1, h, GOLD_LIGHT)
            else:
                rect(img, x, h - 8, x + 1, h, GOLD_DARK)
    return finish(img, face, edges=False)


def phoenix_train():
    """Grande image de la traine : degrade cramoisi-pourpre, queue de phenix en eventail."""
    W, H = 74, 230
    img = cloth(W, H, lambda v: mix(CRIMSON, PURPLE, v), 13, 0.16, 0.03)
    rect(img, 0, 0, 3, H, GOLD)
    rect(img, W - 3, 0, W, H, GOLD)
    rect(img, 0, H - 6, W, H - 3, GOLD)
    for i in (-2, 2, -1, 1, 0):                                  # plumes pleines, des bords vers le centre
        length = 1.0 - abs(i) * 0.09
        pts = []
        for s in range(61):
            t = s / 60.0 * length
            pts.append((W / 2.0 + i * W * 0.2 * smooth(t * 1.2), 10 + t * (H - 34)))
        for k, (x, y) in enumerate(pts):
            t = k / 60.0
            hw = 1.2 + 6.5 * smooth(t * 1.3)
            disc(img, x, y, hw + 1, ROYAL_DARK)
            disc(img, x, y, hw, mix(ROYAL_DARK, GOLD, 0.3 + 0.7 * t))
        for k, (x, y) in enumerate(pts):                         # barbes
            if k % 3 == 0 and k > 6:
                hw = 1.2 + 6.5 * smooth(k / 60.0 * 1.3)
                for sgn in (-1, 1):
                    stroke(img, [(x, y), (x + sgn * hw * 0.9, y + 3.5)], ROYAL_DARK, 0.45)
        stroke(img, pts, GOLD_LIGHT, 0.6)
        ex, ey = pts[-1]
        disc(img, ex, ey, 8.5, GOLD_LIGHT)
        disc(img, ex, ey, 7, PURPLE_LIGHT)
        disc(img, ex, ey, 4.6, JADE)
        disc(img, ex, ey, 2.2, LACQUER)
    return img


def crest_quad():
    """Cimier d'or : soleil rayonnant, decoupe en alpha."""
    W, H = 56, 34
    img = canvas(W, H, GOLD)
    for y in range(H):
        for x in range(W):
            dx, dy = x - W / 2.0 + 0.5, H - y - 0.5
            r, a = math.hypot(dx, dy), math.atan2(dy, dx)
            ray = 19 + 13 * abs(math.cos(a * 6.5))
            if r > ray or r > (W / 2.0 - 1):
                img[y][x][3] = 0
                continue
            img[y][x][:3] = list(mix(GOLD_LIGHT, ROYAL_DARK, r / ray * 0.8))
            if abs(math.cos(a * 6.5)) < 0.16 and r > 12:
                img[y][x][:3] = list(ROYAL_DARK)
            if r < 11:
                img[y][x][:3] = list(GOLD_LIGHT)
    return img


def ei_tail(face, w, h, seed):
    img = canvas(w, h, LACQUER)
    for y in range(h):
        for x in range(w):
            if y % 5 == 0:
                img[y][x][:3] = list((48, 46, 58))
    if face in ("front", "back"):
        rect(img, 0, 0, 2, h, GOLD)
        rect(img, w - 2, 0, w, h, GOLD)
    return finish(img, face, 1.1, edges=False)


def build_emperor_head():
    m = Model("Head")
    kids = [
        box(m, "Coiffe", (0, 10.5, 0), (32, 10, 31), lacquer(True)),
        box(m, "Coiffe_Dome", (0, 17.5, -1), (27, 5, 27), lacquer()),
        box(m, "Bandeau", (0, 6.5, 0), (33, 3, 32), metal()),
        box(m, "Koji", (0, 24, -8), (13, 12, 11), lacquer()),
        box(m, "Koji_Anneau", (0, 19.5, -8), (14.5, 2, 12.5), metal()),
        box(m, "Epingle", (0, 25, -8), (46, 1.5, 1.5), metal(GOLD_LIGHT)),
        box(m, "Joyau_Front", (0, 20, 16), (5, 5, 2), metal(CRIMSON), shading="fullbright"),
        quad(m, "Cimier", (0, 26, 15.4), crest_quad()),
    ]
    kids += sym(m, box(m, "R-Epingle_Bout", (-23.5, 25, -8), (3, 3, 3), metal(JADE)))
    kids.append(plank(m, "Ei_Bas", (0, 26, -14), (0, 40, -19), 10, 1.5, ei_tail))
    kids.append(plank(m, "Ei_Haut", (0, 40, -19), (0, 51, -28), 10, 1.5, ei_tail))
    tie = plain(CRIMSON)
    kids += sym(m, rod(m, "R-Cordon", (-16.2, 7, 3), (-16.2, -13, 5), 1, tie), box(m, "R-Pompon", (-16.2, -15.5, 5), (2.5, 5, 2.5), metal()))
    return m, [piece(m, "Head", (0, 0, 0), kids)]


def build_emperor_chest():
    m = Model("Chest")
    fuki = hem_band(CRIMSON, GOLD_LIGHT)
    layer = hem_band(PURPLE, GOLD)

    def arm():
        tamoto = box(m, "R-Tamoto", (-1.5, -1.5, -8.7), (17, 21, 38), imperial(14, opening=True))
        ring = box(m, "R-Fuki_Manche", (-1.5, -12, -8.7), (18, 2, 39), fuki)
        under = box(m, "R-Manche_Pourpre", (-1.5, -13.5, -5.5), (15, 2, 31), layer)
        inner = box(m, "R-Manche_Ivoire", (-1.5, -15.2, -2), (13, 2, 22), plain(IVORY))
        forearm = piece(m, "R-Forearm", (-1, 0, 0), [tamoto, ring, under, inner])
        sode = box(m, "R-Sode", (-1.5, 0.5, -3.5), (16, 24, 27), imperial())
        return piece(m, "R-Arm", (-17, 0, 0), [sode, forearm])

    def thigh():
        suso = box(m, "R-Suso", (-1.3, -2.3, 0), (17.5, 22, 24), imperial(14))
        ring = box(m, "R-Fuki_Pan", (-1.8, -13.3, 0), (18.5, 2, 25), fuki)
        under = box(m, "R-Pan_Pourpre", (-1, -14.8, 0), (17, 2, 23), layer)
        return piece(m, "R-Thigh", (-8, -1, 1), [suso, ring, under], offset=(0, -9, 0))

    right_arm, right_thigh = arm(), thigh()
    plate = box(m, "Torse", (0, 0, 0), (34, 24, 25), emperor_torso)
    col = stripes([(0.12, GOLD_LIGHT), (0.24, CRIMSON), (0.24, PURPLE), (0.24, IVORY), (0.16, GOLD)])
    wing = box(m, "R-Epaule", (-21, 11.5, -1), (14, 1.5, 25), imperial(), q=q_axis((0, 0, 1), 9))
    band_gold = box(m, "R-Epaule_Bord", (-28, 10.4, -1), (1.5, 2, 26), metal(), q=q_axis((0, 0, 1), 9))
    chest = piece(m, "Chest", (0, 5, 0), [plate] + lapels(m, col, col, 7) + neck_band(m, plain(IVORY)) + sym(m, wing, band_gold) + [right_arm, mirror(m, right_arm)], offset=(0, 11, 0))

    belly = box(m, "Ventre", (0, 0, 0), (32, 16, 24), imperial())
    obi = box(m, "Obi", (0, -2.5, 0), (37, 10, 28), plain(PURPLE))
    belt = box(m, "Sekitai", (0, -2.5, 0), (38, 6, 29), emperor_belt)
    plaque = box(m, "Plaque", (0, -2.5, 15), (10, 10, 1.5), emperor_medallion)
    jewel = box(m, "Joyau", (0, -2.5, 16), (3, 3, 1.2), metal(CRIMSON), shading="fullbright")
    bow = box(m, "Musubi", (0, -1, -15.5), (24, 15, 5), emperor_medallion)
    node = piece(m, "Belly", (0, 8, 0), [belly, obi, belt, plaque, jewel, bow, chest], offset=(0, 4, 0))

    hip = box(m, "Hanches", (0, -0.5, 0), (36, 13, 27), imperial())
    sash = rod(m, "Hirao", (0, 6, 14.5), (0, -26, 16), 7, hirao)
    train = phoenix_train()
    segs = [((0, 8, -13), (0, -24, -24)), ((0, -24, -24), (0, -42, -42)), ((0, -42, -42), (0, -50, -82))]
    total = sum(math.hypot(b[1] - a[1], b[2] - a[2]) for a, b in segs)
    y, kyo = 0.0, []
    for i, (a, b) in enumerate(segs):
        L = math.hypot(b[1] - a[1], b[2] - a[2])
        kyo.append(plank(m, "Kyo_%d" % i, b, a, 30 + i * 3, 1.2, plank_slice(train, y / total * len(train), (y + L) / total * len(train))))
        y += L
    return m, [piece(m, "Pelvis", (0, 12, 0), [hip, sash, right_thigh, mirror(m, right_thigh), node] + kyo)]


def emperor_glove(face, w, h, seed):
    img = cloth(w, h, IVORY, seed, 0.06, 0.02)
    if face not in ("top", "bottom"):
        band(img, 0, 3, GOLD)
    return finish(img, face)


def emperor_cuff(face, w, h, seed):
    img = canvas(w, h, ROYAL)
    if face not in ("top", "bottom"):
        tatewaku(img, ROYAL_LIGHT, seed)
        band(img, 0, 2, GOLD_LIGHT)
        band(img, h - 4, h - 2, CRIMSON)
        band(img, h - 2, h, GOLD_LIGHT)
    return finish(img, face)


def build_emperor_hands():
    m = Model("Hands")
    cuff = box(m, "R-Manchette", (0, -3, 0), (12, 11, 16), emperor_cuff)
    glove = box(m, "R-Gant", (0, 1.5, 0), (11, 9, 15), emperor_glove)
    plate = box(m, "R-Plaque", (-6, 1.5, 0), (2, 8, 11), emperor_medallion)
    ring = box(m, "R-Bague", (0, -2.2, 0), (11.4, 1.3, 15.4), metal())
    hand = piece(m, "R-Hand", (0, -8, 0), [glove, plate, ring], offset=(0, -5, 0))
    right = piece(m, "R-Forearm", (-18, 40, 0), [cuff, hand])
    return m, [right, mirror(m, right)]


def emperor_hakama(hem=False):
    base = pleats(CRIMSON, (222, 76, 84), 12, None)

    def paint(face, w, h, seed):
        img = base(face, w, h, seed)
        if hem and face not in ("top", "bottom"):
            wave_band(img, h - 20, h - 4, CRIMSON_DARK, GOLD)
            band(img, h - 22, h - 20, GOLD_LIGHT)
            band(img, h - 4, h, GOLD)
        return img
    return paint


def boot_paint(face, w, h, seed):
    img = canvas(w, h, LACQUER)
    for y in range(h):
        for x in range(w):
            s = max(0.0, math.sin((x + y) * 4.0 / max(w, 8))) ** 5
            img[y][x][:3] = list(mix(LACQUER, (120, 118, 140), s * 0.6))
    if face in ("left", "right", "front", "back"):
        band(img, 0, 2, GOLD)
    return finish(img, face, edges=False)


def build_emperor_legs():
    m = Model("Legs")
    upper = box(m, "R-Hakama_Haut", (-1, -1, 0), (17, 24, 22), emperor_hakama())
    mid = box(m, "R-Hakama_Genou", (-2.1, 5, -0.5), (19, 13, 25.5), emperor_hakama())
    low = box(m, "R-Hakama_Bas", (-3.6, -6.5, -1), (22, 11, 30), emperor_hakama(True))
    calf = piece(m, "R-Calf", (0, -12, 3), [mid, low], offset=(0, -12, -3))
    thigh = piece(m, "R-Thigh", (-8, -1, 1), [upper, calf], offset=(0, -9, 0))
    shoe = box(m, "R-Kutsu", (0, 0.5, 0), (15, 8, 21), boot_paint)
    tip = box(m, "R-Pointe", (0, 3, 9.5), (9, 4, 7), boot_paint, q=q_axis((1, 0, 0), -24))
    cap = box(m, "R-Embout", (0, 5, 12), (5, 2, 4), metal(), q=q_axis((1, 0, 0), -24))
    sole = box(m, "R-Semelle", (-0.6, -3.6, 0), (16, 1, 22), metal())
    foot = piece(m, "R-Foot", (-7.85, 7, -3), [shoe, tip, cap, sole], offset=(0, -3, 6))
    waist = box(m, "Koshi", (0, 0, 0), (30, 11, 23), emperor_hakama())
    return m, [piece(m, "Pelvis", (0, 51, 0), [waist, thigh, mirror(m, thigh)]), foot, mirror(m, foot)]



# ---------------------------------------------------------------------------------------------------------------------
# Tenues et items
# ---------------------------------------------------------------------------------------------------------------------
SKIP_IN_ICON = ("Kyo",)

SETS = {
    "Pauvre": {
        "prefix": "Armor_Kimono_Pauvre_", "quality": "Common", "level": 15, "durability": 60,
        "build": {"Head": build_poor_head, "Chest": build_poor_chest, "Hands": build_poor_hands, "Legs": build_poor_legs},
        "pieces": {
            "Head": ("Kasa du Vagabond", "Tenue du Vagabond. Un vieux chapeau de paille, troué et rongé sur les bords.", 3, 4, 0.02),
            "Chest": ("Kimono Rapiécé", "Tenue du Vagabond. Du chanvre usé, reprisé cent fois et noué d'une corde.", 6, 8, 0.035),
            "Hands": ("Bandes de Chiffon", "Tenue du Vagabond. Des chiffons enroulés autour des mains et des poignets.", 2, 3, 0.015),
            "Legs": ("Pantalon Déchiré", "Tenue du Vagabond. Un pantalon effiloché, des jambières de toile et des waraji de paille.", 4, 6, 0.03),
        },
    },
    "Sakura": {
        "prefix": "Armor_Kimono_", "quality": "Epic", "level": 180, "durability": 180,
        "build": {"Head": build_sakura_head, "Chest": build_sakura_chest, "Hands": build_sakura_hands, "Legs": build_sakura_legs},
        "pieces": {
            "Head": ("Kasa de Sakura", "Tenue de Sakura. Chapeau laqué, peint de branches de cerisier et noué d'un cordon cramoisi.", 13, 18, 0.07),
            "Chest": ("Kimono de Sakura", "Tenue de Sakura. Soie indigo teintée de prune, semée de fleurs de cerisier et serrée par un obi cramoisi.", 24, 33, 0.126),
            "Hands": ("Tekkō de Sakura", "Tenue de Sakura. Bandes de lin et plaques indigo frappées d'un mon doré.", 10, 15, 0.056),
            "Legs": ("Hakama de Sakura", "Tenue de Sakura. Pantalon plissé porté sur des tabi et des zori de paille.", 18, 24, 0.098),
        },
    },
    "Empereur": {
        "prefix": "Armor_Kimono_Empereur_", "quality": "Legendary", "level": 550, "durability": 400,
        "build": {"Head": build_emperor_head, "Chest": build_emperor_chest, "Hands": build_emperor_hands, "Legs": build_emperor_legs},
        "pieces": {
            "Head": ("Kanmuri de l'Empereur", "Tenue de l'Empereur. Couronne de laque noire au cimier solaire, dressée d'une longue queue.", 24, 36, 0.1),
            "Chest": ("Kimono de l'Empereur", "Tenue de l'Empereur. Brocart d'or aux chrysanthèmes, cols superposés et traîne aux plumes de phénix.", 44, 66, 0.18),
            "Hands": ("Manchettes de l'Empereur", "Tenue de l'Empereur. Gants de soie ivoire et manchettes de brocart d'or.", 18, 30, 0.08),
            "Legs": ("Hakama de l'Empereur", "Tenue de l'Empereur. Hakama cramoisi aux vagues d'or, porté sur des souliers laqués.", 32, 48, 0.14),
        },
    },
}

if __name__ == "__main__":                                                   # importable par gen_kimono_luo.py
    write_sets(RES, PREVIEW, MODEL_DIR, "Kimono", SETS, "kimono", family_tag="Cloth_Kimono", skip_in_icon=SKIP_IN_ICON)
