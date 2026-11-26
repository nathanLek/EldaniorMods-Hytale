"""Kimonos du Clan Luo : les tenues de rang a cote du Vagabond, de Sakura, de l'Empereur et du Dieu-Dragon d'Or.

  Recrue    Rare       niv. 60   ordre des recrues : keikogi ecru pique de sashiko (juji-zashi), col noir surpique, tasuki
                                 cramoisie croisee dans le dos, hakama indigo noue devant, hachimaki au mon du clan
  Marchand  Rare       niv. 120  marchands du Comptoir de la Porte du Dragon : large kasa de paille au bord laque d'ocre,
                                 kimono brun aux shippo d'or, tablier maekake indigo au mon, bourse kinchaku et chapelet de pieces
  Negociant Rare       niv. 160  Grands Negociants du Comptoir (marchands du clan) : daikoku-zukin prune et lunettes d'or, haori aux
                                 pieces d'or, obi de brocart, bourse, chapelet de pieces et boulier, hakama raye d'or et geta
  Pelerin   Epique     niv. 300  pelerins de la vallee, messagers du clan : kasa profond de paille au bord laque, clochette,
                                 kimono gris aux asanoha (mino retire le 2026-11-11), col blanc, kyahan blanches
  Ombre     Unique     niv. 400  ordre d'assassins : zukin enroule, masque, hachigane d'acier au mon ; uwagi noir aux asanoha
                                 sur cotte de mailles, epaulieres lamellaires lacees de cramoisi, baudrier a trois kunai,
                                 shuriken a l'obi, tanto en travers du dos, sacoches ; tekko a attelles, porte-kunai a la cuisse,
                                 genouilleres matelassees, kyahan lacees, jika-tabi a agrafes
  Noble     Unique     niv. 450  membres de la famille : haori aux cinq mon sur damas kikko, ourlets aux vagues de metal,
                                 obi hakata, eventail, hakama raye sendai-hira, eboshi ; noir et or, cramoisi, blanc et jade, violet
  Rizieres  Commun     niv. 10   villageois de la vallee : sugegasa conique, noragi indigo pique de sashiko aux manches
                                 retroussees, tasuki, tablier raye, tenugui, momohiki retrousse, jambieres et waraji
  Ronin     Rare       niv. 100  aventuriers : haori use et rapiece au mon arrache, kimono sombre, gourde, baluchon sur le dos,
                                 hachimaki aux longs pans, bandes de toile, hakama delave effiloche, waraji
  Cour      Epique     niv. 320  Robe de Cour (nobles de salon) : robes superposees aux bords cramoisi, or, jade, ivoire
                                 (kasane), manches trainantes, traine damassee, haute coiffe a ruban et pampilles, hakama ecarlate
  Lettre    Epique     niv. 330  Kariginu du Lettre (nobles erudits) : col rond agekubi, manches a cordons kukuri, pans avant et
                                 arriere, rouleau et shaku a la ceinture, sashinuki violet noue aux chevilles, haut eboshi
  Garde     Epique     niv. 340  Haori de la Garde d'Honneur (nobles d'epee) : haori court bleu nuit au grand mon, cordon
                                 d'honneur, hachigane, kote d'acier, hakama serre au genou
  Seigneur  Legendaire niv. 500  Seigneur et Doyens : kamishimo (kataginu brode d'or aux ailes frappees du mon) sur kimono aux
                                 ecailles de dragon, nagabakama aux vagues d'or noue devant, eventail et inro de laque, grand eboshi

Aucun sabre en 3D sur les tenues (demande du 2026-11-05), sauf le tanto de l'Ombre.
Le mon du clan est le blason Luo (dragon d'or enroule autour d'un soleil noir), dessine par familles.coiled_dragon.
Meme densite et memes formes de base que gen_kimono.py (2 px par unite), importe sans rien ecrire.
Modeles sous Items/Armors/Kimono/<Tenue>, items sous Armor/Kimono, IDs Armor_Kimono_<Tenue>_<Piece>.

Usage : python3 scripts/armures/gen_kimono_luo.py <src/main/resources> [dossier_apercu] [tenue...]
"""
import math
import random

import gen_kimono as gk
from armor_kit import *  # noqa: F401,F403
from armor_kit import HIDE_HELMET, write_sets, coplanar_report
import blason
import familles

RES, PREVIEW = gk.RES, gk.PREVIEW
MODEL_DIR = gk.MODEL_DIR

KURO, KURO_LIGHT, KURO_DARK = (26, 24, 30), (72, 68, 82), (10, 10, 14)
ECRU, ECRU_DARK = (224, 212, 184), (168, 154, 126)
AI, AI_LIGHT = (32, 36, 62), (76, 84, 126)
STEEL, STEEL_LIGHT, STEEL_DARK = (124, 128, 140), (196, 200, 212), (66, 68, 78)
JADE, JADE_LIGHT, JADE_DARK = (64, 168, 132), (156, 224, 194), (26, 104, 80)
SILVER, SILVER_LIGHT, SILVER_DARK = (196, 200, 212), (240, 242, 248), (128, 132, 148)
LEATHER, LEATHER_DARK = (58, 40, 32), (30, 20, 16)
GOLDS = (GOLD_DARK, GOLD, GOLD_LIGHT)
SENTINEL = (1, 2, 3)


# ---------------------------------------------------------------------------------------------------------------------
# Mon du clan
# ---------------------------------------------------------------------------------------------------------------------
_MON = {}


def luo_mon(px, metal=GOLDS):
    """Blason Luo sans l'ecu, px de cote, transparent autour ; les ors sont repeints dans la gamme metal (sombre, moyen, clair)."""
    key = (px, metal)
    if key not in _MON:
        k = px * 4 / 2.1
        cv = blason.Canvas(k, -1.05, 1.05, -1.0, 1.1)
        familles.coiled_dragon(cv)
        img = blason.shrink(cv.img, px, px)
        if metal != GOLDS:
            dark, mid, light = metal
            for row in img:
                for p in row:
                    if not p[3]:
                        continue
                    r, g, b = p[:3]
                    if r > 150 and g < 90:                       # oeil
                        continue
                    lum = 0.3 * r + 0.59 * g + 0.11 * b
                    if lum < 50:                                 # trait et soleil noir
                        continue
                    t = min(1.0, max(0.0, (lum - 90) / 150.0))
                    c = mix(dark, mid, t * 2) if t < 0.5 else mix(mid, light, (t - 0.5) * 2)
                    p[0], p[1], p[2] = c
        _MON[key] = img
    return _MON[key]


def stamp(img, src, cx, cy):
    h, w = len(src), len(src[0])
    for y in range(h):
        for x in range(w):
            p = src[y][x]
            if p[3]:
                put(img, cx - w / 2.0 + x, cy - h / 2.0 + y, p[:3], p[3] / 255.0)


def kamon(img, cx, cy, r, ground, metal=GOLDS):
    """Mon brode : cercle de metal, fond, blason."""
    disc(img, cx, cy, r, metal[1])
    disc(img, cx, cy, r - 1, ground)
    stamp(img, luo_mon(max(4, int(round(2 * r - 2))), metal), cx, cy)


def cutout(img, keep):
    for y, row in enumerate(img):
        for x, p in enumerate(row):
            if not keep(x + 0.5, y + 0.5):
                p[3] = 0
    return img


def mon_quad(m, name, pos, size_u, ground, metal=GOLDS, q=ID_Q):
    """Mon rond en quad (pastille cousue ou cimier), detoure en alpha."""
    px = int(round(size_u * D))
    img = canvas(px, px, ground)
    kamon(img, px / 2.0, px / 2.0, px / 2.0, ground, metal)
    return quad(m, name, pos, cutout(img, lambda x, y: math.hypot(x - px / 2.0, y - px / 2.0) <= px / 2.0), q)


# ---------------------------------------------------------------------------------------------------------------------
# Motifs en tuiles (masques precalcules, puis repetes)
# ---------------------------------------------------------------------------------------------------------------------
def seg_dist(p, a, b):
    ax, ay = a
    bx, by = b
    dx, dy = bx - ax, by - ay
    L = dx * dx + dy * dy
    t = 0.0 if L == 0 else max(0.0, min(1.0, ((p[0] - ax) * dx + (p[1] - ay) * dy) / L))
    return math.hypot(p[0] - ax - t * dx, p[1] - ay - t * dy)


def tile(w, h, segs, width=0.55):
    mask = []
    for y in range(h):
        row = []
        for x in range(w):
            p = (x + 0.5, y + 0.5)
            row.append(any(seg_dist(p, a, b) < width for a, b in segs))
        mask.append(row)
    return mask


def circle_segs(cx, cy, r, n=28):
    pts = [(cx + r * math.cos(2 * math.pi * k / n), cy + r * math.sin(2 * math.pi * k / n)) for k in range(n + 1)]
    return list(zip(pts, pts[1:]))


_TILES = {}


def asanoha_tile(s=16):
    """Feuille de chanvre : reseau de triangles, chaque sommet relie au centre de son triangle."""
    key = ("asanoha", s)
    if key not in _TILES:
        h = int(round(s * math.sqrt(3)))
        row_h = h / 2.0
        rows = {k: [(i * s + (k % 2) * s / 2.0, k * row_h) for i in range(-2, 4)] for k in range(-2, 5)}
        segs = []
        for k in range(-2, 4):
            for upper, lower in ((rows[k], rows[k + 1]), (rows[k + 1], rows[k])):
                xs = {round(p[0], 3): p for p in lower}
                for a, b in zip(upper, upper[1:]):
                    c = xs.get(round((a[0] + b[0]) / 2.0, 3))
                    if c is None:
                        continue
                    g = ((a[0] + b[0] + c[0]) / 3.0, (a[1] + b[1] + c[1]) / 3.0)
                    segs += [(a, b), (b, c), (c, a), (a, g), (b, g), (c, g)]
        _TILES[key] = tile(s, h, segs, 0.5)
    return _TILES[key]


def kikko_tile(R=7):
    """Ecaille de tortue : hexagones a double trait."""
    key = ("kikko", R)
    if key not in _TILES:
        w, h = int(round(3 * R)), int(round(math.sqrt(3) * R))
        sy = h / (math.sqrt(3) * R)
        segs = []
        for i in range(-2, 4):
            for j in range(-2, 4):
                cx, cy = i * 1.5 * R, (j + (i % 2) * 0.5) * math.sqrt(3) * R * sy
                for rr in (R, R * 0.62):
                    pts = [(cx + rr * math.cos(math.radians(60 * k)), cy + rr * math.sin(math.radians(60 * k)) * sy) for k in range(7)]
                    segs += list(zip(pts, pts[1:]))
        _TILES[key] = tile(w, h, segs, 0.5)
    return _TILES[key]


def shippo_tile(S=12):
    """Sept tresors : cercles entrelaces."""
    key = ("shippo", S)
    if key not in _TILES:
        segs = []
        for i in range(-1, 3):
            for j in range(-1, 3):
                segs += circle_segs(i * S, j * S, S / math.sqrt(2))
        _TILES[key] = tile(S, S, segs, 0.5)
    return _TILES[key]


def juji_tile(s=8):
    """Sashiko juji-zashi : pointilles en grille et petites croix."""
    key = ("juji", s)
    if key not in _TILES:
        q = s / 4.0
        segs = [((q * 0.4, 0.5), (q * 1.6, 0.5)), ((q * 2.4, 0.5), (q * 3.6, 0.5)),
                ((0.5, q * 0.4), (0.5, q * 1.6)), ((0.5, q * 2.4), (0.5, q * 3.6)),
                ((s / 2.0 - 1, s / 2.0 + 0.5), (s / 2.0 + 2, s / 2.0 + 0.5)), ((s / 2.0 + 0.5, s / 2.0 - 1), (s / 2.0 + 0.5, s / 2.0 + 2))]
        _TILES[key] = tile(s, s, segs, 0.5)
    return _TILES[key]


def chain_tile(s=6):
    """Cotte de mailles kusari : anneaux relies par de petits maillons."""
    key = ("chain", s)
    if key not in _TILES:
        segs = circle_segs(s / 2.0, s / 2.0, s * 0.3, 12)
        segs += [((s * 0.8, s / 2.0), (s * 1.2, s / 2.0)), ((s / 2.0, s * 0.8), (s / 2.0, s * 1.2)),
                 ((-s * 0.2, s / 2.0), (s * 0.2, s / 2.0)), ((s / 2.0, -s * 0.2), (s / 2.0, s * 0.2))]
        _TILES[key] = tile(s, s, segs, 0.55)
    return _TILES[key]


def apply_tile(img, mask, c, a=1.0, ox=0, oy=0, only=None):
    th, tw = len(mask), len(mask[0])
    for y, row in enumerate(img):
        for x, p in enumerate(row):
            if mask[(y + oy) % th][(x + ox) % tw] and (only is None or only(x, y, p)):
                put(img, x, y, c, a)


def pat(kind, c, a, **kw):
    """Motif a poser sur une toile : pat("asanoha", couleur, opacite)."""
    tiles = {"asanoha": asanoha_tile, "kikko": kikko_tile, "shippo": shippo_tile, "juji": juji_tile, "chain": chain_tile}
    mask = tiles[kind](**kw)
    return lambda img: apply_tile(img, mask, c, a)


def uroko(c, step=10, a=0.35):
    """Ecailles de dragon (triangles en quinconce)."""
    def motif(img):
        for y in range(len(img)):
            row = y // step
            for x in range(len(img[0])):
                u = (x + (row % 2) * step / 2.0) % step
                v = y % step
                if abs(u - step / 2.0) * 2 > step - v - 1 and abs(u - step / 2.0) * 2 <= step - v + 0.5:
                    put(img, x, y, c, a)
    return motif


def gradient(k):
    """Assombrit la toile vers le bas (k = part d'ombre en bas)."""
    def motif(img):
        h = len(img)
        for y, row in enumerate(img):
            f = 1 - k * y / max(1.0, h - 1.0)
            for p in row:
                for i in range(3):
                    p[i] *= f
    return motif


def wrap_folds(c_dark, c_light, period=5, slant=0.25):
    """Tissu enroule : plis horizontaux en biais, ombre sous chaque tour, reflet dessus."""
    def motif(img):
        for y, row in enumerate(img):
            for x, p in enumerate(row):
                t = (y + x * slant) % period
                if t < 1:
                    put(img, x, y, c_dark, 0.6)
                elif t < 2:
                    put(img, x, y, c_light, 0.25)
    return motif


def edge_piping(c, inset=0):
    return lambda img: border(img, inset, c)


# ---------------------------------------------------------------------------------------------------------------------
# Toiles
# ---------------------------------------------------------------------------------------------------------------------
def fabric(ground, light=1.0, motifs=(), mon=(), opening=None, fill=None, hem=None, wave=None, tb=None, lining=None, tb_motifs=()):
    """Toile complete.
    motifs : fonctions sur l'image des cotes ; mon : (face, fx, fy, r, metal) ; opening : couches du col en V (face avant),
    la couche SENTINEL est remplie par fill(img, x, y) ; hem : (px, couleur) ; wave : (px, fond, trait) vagues seigaiha en bas ;
    lining : couleur de la doublure visible sur la face du bas (ouverture de manche)."""
    def paint(face, w, h, seed):
        if face == "bottom" and lining:
            img = canvas(w, h, LINING)
            border(img, 0, lining)
            border(img, 1, lining)
            border(img, 2, IVORY)
            return finish(img, face, 1.3, edges=False)
        if face in ("top", "bottom"):
            img = cloth(w, h, tb or ground, seed, 0.06, 0.02)
            for mt in tb_motifs:
                mt(img)
            for spec in mon:
                if spec[0] == face:
                    kamon(img, w * spec[1], h * spec[2], spec[3], tb or ground, spec[4])
            return finish(img, face, light)
        img = cloth(w, h, ground, seed, 0.14, 0.03)
        for mt in motifs:
            mt(img)
        if wave:
            gk.wave_band(img, h - wave[0], h - 2, wave[1], wave[2])
            band(img, h - wave[0] - 2, h - wave[0], wave[2])
            band(img, h - 2, h, tuple(v * 0.7 for v in wave[2]))
        if hem:
            band(img, h - hem[0], h, hem[1])
            band(img, h - hem[0] - 1, h - hem[0], tuple(v * 0.6 for v in hem[1]))
        if opening and face == "front":
            gk.v_opening(img, opening)
            if fill:
                for y, row in enumerate(img):
                    for x, p in enumerate(row):
                        if tuple(int(v) for v in p[:3]) == SENTINEL:
                            p[:3] = list(fill(x, y))
        for spec in mon:
            if spec[0] == face:
                kamon(img, w * spec[1], h * spec[2], spec[3], ground, spec[4])
        return finish(img, face, light)
    return paint


def striped_hakama(ground, line, light, period=10, hem=None, wave=None):
    """Hakama sendai-hira : plis, une rayure large et une fine par pli, ourlet en option."""
    base = gk.pleats(ground, light, period)

    def paint(face, w, h, seed):
        img = base(face, w, h, seed)
        if face not in ("top", "bottom"):
            for y in range(h):
                for x in range(w):
                    p = x % period
                    if p in (3, 4):
                        put(img, x, y, line, 0.5)
                    elif p == 7:
                        put(img, x, y, line, 0.25)
            if wave:
                gk.wave_band(img, h - wave[0], h - 2, wave[1], wave[2])
                band(img, h - wave[0] - 2, h - wave[0], wave[2])
            if hem:
                band(img, h - 4, h - 2, hem)
        return img
    return paint


def lacquer_c(color, edge=None, mon=None):
    def paint(face, w, h, seed):
        img = canvas(w, h, color)
        hi = mix(color, (200, 200, 215), 0.45)
        for y in range(h):
            for x in range(w):
                s = max(0.0, math.sin((x + y * 0.6) * 5.0 / max(w, 8))) ** 6
                img[y][x][:3] = list(mix(color, hi, s * 0.6))
        if edge and face not in ("top", "bottom"):
            band(img, h - 3, h - 1, edge)
        if mon and face == mon[0]:
            kamon(img, w / 2.0, h / 2.0, min(w, h) / 2.0 - 1, color, mon[1])
        return finish(img, face, edges=False)
    return paint


def straps(c, period=7):
    """Lacage croise (attelles, jambieres)."""
    def motif(img):
        h, w = len(img), len(img[0])
        for y in range(h):
            for x in range(w):
                if abs(((x + y) % period) - 0) < 1 or abs(((x - y) % period) - 0) < 1:
                    if (y // period) % 2 == 0:
                        put(img, x, y, c, 0.9)
    return motif


def white_tabi(face, w, h, seed):
    return gk.tabi_paint(face, w, h, seed)


def straw_strands(c0=STRAW, c1=STRAW_DARK, period=7):
    """Paille : brins verticaux, rangs tresses."""
    def motif(img):
        for y, row in enumerate(img):
            for x, p in enumerate(row):
                k = noise(x * 1.7, y * 0.12)
                c = mix(c0, c1, k * 0.8)
                if y % period == 0:
                    c = mix(c1, (60, 44, 24), 0.4)
                elif y % period == 1:
                    c = mix(c0, STRAW_LIGHT, 0.5)
                p[:3] = list(c)
    return motif


def fringe(paint, depth=6, seed_off=0):
    """Bas effiloche (paille, franges) ; faces du dessus et du dessous transparentes."""
    def wrapped(face, w, h, seed):
        if face in ("top", "bottom"):
            img = canvas(w, h, (0, 0, 0))
            for row in img:
                for p in row:
                    p[3] = 0
            return img
        img = paint(face, w, h, seed)
        torn(img, seed + seed_off, depth * D)
        return img
    return wrapped


def kasa_panel(base, dark, rim, rim_line, mon_ground=None, mon_metal=GOLDS, stripes_n=0):
    """Pan de kasa de paille : brins rayonnants, rangs tresses, bord laque ; mon sur le pan de face."""
    def paint(k, W, H, light):
        img = canvas(W, H, base)
        for y in range(H):
            for x in range(W):
                c = mix(base, dark, noise(x * 1.9 + k, y * 0.1) * 0.7)
                if y % 9 == 0:
                    c = mix(dark, (50, 36, 20), 0.5)
                elif y % 9 == 1:
                    c = mix(base, STRAW_LIGHT, 0.45)
                img[y][x][:3] = list(c)
        for i in range(stripes_n):
            band(img, int(H * (0.28 + 0.12 * i)), int(H * (0.28 + 0.12 * i)) + 2, rim)
        band(img, H - 7, H - 2, rim)
        band(img, H - 8, H - 7, rim_line)
        band(img, H - 2, H, tuple(v * 0.6 for v in rim))
        if k == 0 and mon_ground:
            r = W * 0.24
            kamon(img, W / 2.0, H * 0.66, r, mon_ground, mon_metal)
        for row in img:
            for p in row:
                for i in range(3):
                    p[i] = clamp(p[i] * light)
        return img
    return paint


def kunai_blade_img():
    w, h = 5, 12
    img = canvas(w, h, STEEL)
    for y in range(h):
        half = 2.5 * (1 - abs(y - 4.5) / 7.5) if y >= 2 else 1.0
        for x in range(w):
            if abs(x + 0.5 - 2.5) > half:
                img[y][x][3] = 0
            else:
                img[y][x][:3] = list(mix(STEEL_LIGHT, STEEL_DARK, abs(x + 0.5 - 2.5) / 2.5))
    return img


def ring_img(px, c, inner=0.55):
    img = canvas(px, px, c)
    return cutout(img, lambda x, y: inner * px / 2.0 <= math.hypot(x - px / 2.0, y - px / 2.0) <= px / 2.0)


def shuriken_img(px=10):
    img = canvas(px, px, STEEL)
    c = px / 2.0
    for y in range(px):
        for x in range(px):
            dx, dy = x + 0.5 - c, y + 0.5 - c
            r = math.hypot(dx, dy)
            a = math.atan2(dy, dx)
            star = c * (0.3 + 0.7 * abs(math.cos(2 * a)) ** 6)
            if r > star or r < px * 0.09:
                img[y][x][3] = 0
            else:
                img[y][x][:3] = list(mix(STEEL_LIGHT, STEEL_DARK, r / c))
    return img


def strap_box(m, name, a, b, z, width, thick, paint):
    """Courroie plate dans le plan XY, de a vers b."""
    dx, dy = b[0] - a[0], b[1] - a[1]
    L = math.hypot(dx, dy)
    ang = math.degrees(math.atan2(-dx, dy))
    return box(m, name, ((a[0] + b[0]) / 2.0, (a[1] + b[1]) / 2.0, z), (width, L, thick), paint, q=q_axis((0, 0, 1), ang))


def sensu(m, pos, tilt, frame, leaf):
    """Eventail plie glisse dans l'obi."""
    q = q_axis((0, 0, 1), tilt)
    return [box(m, "Sensu", pos, (1.8, 9, 3.2), lacquer_c(frame, leaf), q=q),
            box(m, "Sensu_Tete", (pos[0] - math.sin(math.radians(tilt)) * 4.6, pos[1] + math.cos(math.radians(tilt)) * 4.6, pos[2]), (2.2, 1.2, 3.6), metal(leaf), q=q)]


def front_himo(m, z, paint, y0=4.5, y1=-2.0, half=13.5):
    """Himo du hakama : deux cordons qui se croisent devant et un noeud juji."""
    nodes = sym(m, rod(m, "R-Himo", (-half, y0, z), (3, y1, z + 0.2), 1.3, paint))
    nodes.append(box(m, "Himo_Noeud", (0, y1 - 0.4, z + 0.5), (5.2, 2.6, 1.6), paint))
    nodes.append(box(m, "Himo_Noeud_Barre", (0, y1 - 0.4, z + 1.0), (1.8, 4.4, 1.2), paint))
    return nodes


def eboshi(m, size, metal_r, ground=KURO_DARK, tilt=-14):
    """Tate-eboshi : calotte de laque, haute coiffe etroite penchee en arriere, pli avant (maneki) et mon au front."""
    w, h, d = size
    q = q_axis((1, 0, 0), tilt)
    center = (0, 14 + h / 2.0 - 2, -3)

    def at(local):
        r = qrot(q, local)
        return tuple(center[i] + r[i] for i in range(3))
    return [box(m, "Coiffe", (0, 11, 0), (31.5, 7, 30.5), lacquer_c(KURO_DARK)),
            box(m, "Bandeau", (0, 8.6, 0), (32, 2.6, 31), metal(metal_r[1])),
            box(m, "Eboshi", center, (w, h, d), lacquer_c(ground), q=q),
            box(m, "Eboshi_Maneki", at((0, h / 2.0 - 3, d / 2.0 - 1)), (w + 0.8, 4, 4), lacquer_c(KURO), q=q),
            mon_quad(m, "Mon", at((0, -h / 2.0 + 0.45 * h, d / 2.0 + 0.3)), min(w - 1, 0.45 * h), ground, metal_r, q=q)]


def kasa_head(m, count, apothem, base_y, apex_y, panel, crown, finial, cord):
    kids = gk.cone_panels(m, count, apothem, base_y, apex_y, panel)
    kids.append(box(m, "Couronne", (0, 13.5, 0), (24, 2, 24), plain(crown)))
    kids.append(box(m, "Fleuron", (0, apex_y, 0), (5, 2, 5), metal(finial)))
    kids.append(box(m, "Fleuron_Haut", (0, apex_y + 1.5, 0), (2.5, 2, 2.5), metal(mix(finial, (255, 255, 255), 0.3))))
    kids += gk.cords_head(m, plain(cord))
    return kids


# ---------------------------------------------------------------------------------------------------------------------
# Recrue
# ---------------------------------------------------------------------------------------------------------------------
RECRUIT_STITCH = pat("juji", (246, 240, 226), 0.85)


def recruit_body(mon=(), opening=False, quilt=True):
    return fabric(ECRU, motifs=([RECRUIT_STITCH] if quilt else []) + [gradient(0.08)], mon=mon,
                  opening=[(1.2, ECRU_DARK), (40, KURO)] if opening else None)


def recruit_collar(face, w, h, seed):
    img = cloth(w, h, KURO, seed, 0.05, 0.02)
    if face in ("front", "back"):
        for x in (2, w // 2, w - 3):
            stitch_line(img, [(x, 0), (x, h)], (150, 146, 140), 2)
    return finish(img, face, edges=False)


def hachimaki(face, w, h, seed):
    img = cloth(w, h, IVORY, seed, 0.08, 0.02)
    if face not in ("top", "bottom"):
        wrap_folds(IVORY_SHADE, (255, 255, 255), 6, 0.6)(img)
        band(img, 0, 1, IVORY_SHADE)
        band(img, h - 1, h, IVORY_SHADE)
    return finish(img, face)


def build_recruit_head():
    m = Model("Head")
    kids = [box(m, "Hachimaki", (0, 4.5, 0), (31.5, 4, 29.6), hachimaki),
            mon_quad(m, "Mon", (0, 4.5, 15.1), 7, IVORY),
            box(m, "Noeud", (0, 4.5, -16), (6, 5, 3), hachimaki)]
    kids += sym(m, plank(m, "R-Queue", (-3.5, -13, -19.8), (-1.5, 3, -16.8), 3, 0.6, hachimaki))
    return m, [piece(m, "Head", (0, 0, 0), kids)]


def build_recruit_chest():
    m = Model("Chest")

    def arm():
        sode = box(m, "R-Sode", (-1, -1, -1), (12.5, 23, 15.5), recruit_body())
        cuff = box(m, "R-Poignet", (-1, -11.3, -1), (13, 2, 16), recruit_collar)
        return piece(m, "R-Arm", (-17, 0, 0), [sode, cuff])

    right_arm = arm()
    plate = box(m, "Torse", (0, 0, 0), (32, 24, 23), recruit_body(mon=[("back", 0.5, 0.2, 7, GOLDS)], opening=True))
    tasuki = plain(CRIMSON)
    cross = [rod(m, "Tasuki_A", (-13, 11, -12.4), (13, -11, -12.4), 1.4, tasuki),
             rod(m, "Tasuki_B", (13, 11, -12.4), (-13, -11, -12.4), 1.4, tasuki),
             box(m, "Tasuki_Noeud", (0, 0, -13.1), (3.4, 3.4, 1.8), tasuki)]
    chest = piece(m, "Chest", (0, 5, 0), [plate] + gk.lapels(m, recruit_collar, recruit_collar, 4.5) + gk.neck_band(m, recruit_collar, 12.1)
                  + cross + [right_arm, mirror(m, right_arm)], offset=(0, 11, 0))

    belly = box(m, "Ventre", (0, 0, 0), (30, 16, 22), recruit_body(quilt=False))
    obi = box(m, "Obi", (0, -2.5, 0), (34.5, 4.5, 26.5), fabric(KURO, motifs=[lambda img: [stitch_line(img, [(0, y), (len(img[0]), y)], (120, 116, 110), 2) for y in (2, len(img) - 3)]]))
    knot = box(m, "Noeud", (-5, -2.5, 13.8), (6, 4, 2.5), plain(KURO))
    tails = [rod(m, "Bout_A", (-6, -4, 14.2), (-8, -13, 14.6), 2.2, plain(KURO)),
             rod(m, "Bout_B", (-4, -4, 14.2), (-3, -12, 14.4), 2.2, plain(KURO))]
    node = piece(m, "Belly", (0, 8, 0), [belly, obi, knot] + tails + [chest], offset=(0, 4, 0))
    hip = box(m, "Hanches", (0, -0.5, 0), (31, 12, 23.6), recruit_body(quilt=False))
    return m, [piece(m, "Pelvis", (0, 12, 0), [hip, node])]


def build_recruit_hands():
    m = Model("Hands")
    wrap = box(m, "R-Bandes", (0, -2, 0), (10, 12, 14), wraps(edge=KURO))
    glove = box(m, "R-Bandes_Main", (0, 1.5, 0), (11, 7, 15), wraps())
    knuckle = box(m, "R-Bandes_Phalanges", (0, 3.6, 6.5), (11.4, 2.4, 2.6), wraps(IVORY_SHADE, (150, 140, 120)))
    hand = piece(m, "R-Hand", (0, -8, 0), [glove, knuckle], offset=(0, -5, 0))
    right = piece(m, "R-Forearm", (-18, 40, 0), [wrap, hand])
    return m, [right, mirror(m, right)]


def build_recruit_legs():
    m = Model("Legs")
    hak = striped_hakama(AI, mix(AI, AI_LIGHT, 0.6), AI_LIGHT, 10, hem=IVORY_SHADE)
    upper = box(m, "R-Hakama_Haut", (-0.5, -1, 0), (16, 24, 20), hak)
    mid = box(m, "R-Hakama_Genou", (-1.3, 5, -0.5), (17.5, 13, 23.5), hak)
    low = box(m, "R-Hakama_Bas", (-2.6, -6, -1), (20, 11, 28), hak)
    calf = piece(m, "R-Calf", (0, -12, 3), [mid, low], offset=(0, -12, -3))
    thigh = piece(m, "R-Thigh", (-8, -1, 1), [upper, calf], offset=(0, -9, 0))
    sock = box(m, "R-Tabi", (0, 1, 0), (15, 7, 21), white_tabi)
    sole = box(m, "R-Waraji", (-0.6, -3.2, 0), (16, 1.5, 22), gk.straw_paint())
    ties = [rod(m, "R-Attache_Waraji", (-7.7, -1.8, -6), (7.4, -1.8, -6.5), 0.8, plain(STRAW_DARK))]
    foot = piece(m, "R-Foot", (-7.85, 7, -3), [sock, sole] + ties + gk.thong(m, STRAW_DARK), offset=(0, -3, 6))
    waist = box(m, "Koshi", (0, 0.25, 0), (33.5, 12.5, 26), hak)
    board = box(m, "Koshi_Ita", (0, 1.5, -14.1), (14, 8, 2), fabric(AI, motifs=[RECRUIT_STITCH], mon=[("back", 0.5, 0.5, 5, GOLDS)]))
    himo = front_himo(m, 13.4, plain(AI_LIGHT))
    return m, [piece(m, "Pelvis", (0, 51, 0), [waist, board] + himo + [thigh, mirror(m, thigh)]), foot, mirror(m, foot)]


# ---------------------------------------------------------------------------------------------------------------------
# Marchand du Comptoir
# ---------------------------------------------------------------------------------------------------------------------
CHA, CHA_LIGHT, CHA_DARK = (104, 66, 42), (160, 112, 70), (60, 36, 22)
OCHRE = (196, 140, 52)


def merchant_body(mon=(), opening=False, wave=False):
    return fabric(CHA, motifs=[pat("shippo", OCHRE, 0.4), gradient(0.1)], mon=mon,
                  opening=[(1.0, IVORY), (1.2, OCHRE), (40, CHA_DARK)] if opening else None,
                  wave=(10, CHA_DARK, OCHRE) if wave else None, lining=OCHRE if wave else None)


def apron(face, w, h, seed):
    img = cloth(w, h, AI, seed, 0.1, 0.03)
    if face in ("front", "back"):
        border(img, 1, IVORY)
        kamon(img, w / 2.0, h * 0.45, min(w, h) * 0.26, AI, (IVORY_SHADE, IVORY, (255, 255, 255)))
        for x in range(4, w - 4, 3):
            put(img, x, h - 2, IVORY, 0.6)
    return finish(img, face)


def build_merchant_head():
    m = Model("Head")
    panel = kasa_panel(STRAW, STRAW_DARK, OCHRE, CHA_DARK, mon_ground=CHA_DARK)
    return m, [piece(m, "Head", (0, 0, 0), kasa_head(m, 16, 35.0, 10.5, 22.0, panel, CHA_DARK, GOLD, CRIMSON))]


def build_merchant_chest():
    m = Model("Chest")
    fuki = hem_band(CHA_DARK, OCHRE)

    def arm():
        tamoto = box(m, "R-Tamoto", (-1, -1, -4.2), (14.5, 17, 26), merchant_body(wave=True))
        juban = box(m, "R-Juban", (-1, -10.5, 0), (13.5, 3, 15.8), plain(IVORY))
        forearm = piece(m, "R-Forearm", (-1, 0, 0), [tamoto, juban])
        sode = box(m, "R-Sode", (-1, 0.5, -2), (14, 23, 22), merchant_body(mon=[("back", 0.5, 0.3, 4, GOLDS)]))
        return piece(m, "R-Arm", (-17, 0, 0), [sode, forearm])

    def thigh():
        suso = box(m, "R-Suso", (-1, 0.5, 0), (17, 17, 23), merchant_body(wave=True))
        ring = box(m, "R-Fuki_Pan", (-1.6, -8, 0), (18, 2, 24), fuki)
        return piece(m, "R-Thigh", (-8, -1, 1), [suso, ring], offset=(0, -9, 0))

    right_arm, right_thigh = arm(), thigh()
    plate = box(m, "Torse", (0, 0, 0), (33, 24, 24), merchant_body(mon=[("back", 0.5, 0.3, 8, GOLDS)], opening=True))
    col = stripes([(0.2, OCHRE), (0.45, CHA_DARK), (0.35, IVORY)])
    chest = piece(m, "Chest", (0, 5, 0), [plate] + gk.lapels(m, col, col, 4.5) + gk.neck_band(m, plain(IVORY)) + [right_arm, mirror(m, right_arm)], offset=(0, 11, 0))

    belly = box(m, "Ventre", (0, 0, 0), (31, 16, 23), merchant_body())
    obi = box(m, "Obi", (0, -2, 0), (35, 6, 26.5), fabric(CHA_DARK, motifs=[lambda img: [band(img, y, y + 1, OCHRE) for y in (1, len(img) - 2)]]))
    apron_tie = box(m, "Maekake_Lien", (0, -4.2, 0), (35.8, 1.4, 27.6), plain(AI))
    apron_box = box(m, "Maekake", (0, -13.5, 13.9), (24, 22, 1), apron)
    purse = [box(m, "Kinchaku", (-14.5, -10, 14.6), (5, 6, 3.6), fabric(CRIMSON_DARK, motifs=[pat("kikko", GOLD, 0.35, R=4)])),
             box(m, "Kinchaku_Col", (-14.5, -6.4, 14.6), (3.4, 1.6, 2.6), plain(CRIMSON_DARK)),
             rod(m, "Kinchaku_Cordon", (-14.5, -5.6, 14.6), (-13.2, -1, 14.2), 0.7, plain(GOLD))]
    coins = [rod(m, "Zeni_Cordon", (14.2, -3.5, 14.8), (14.2, -14.5, 14.8), 0.6, plain(CRIMSON))]
    coins += [box(m, "Zeni_%d" % i, (14.2, -6 - i * 1.45, 14.8), (2.8, 0.9, 2.8), metal()) for i in range(6)]
    node = piece(m, "Belly", (0, 8, 0), [belly, obi, apron_tie, apron_box] + purse + coins + [chest], offset=(0, 4, 0))
    hip = box(m, "Hanches", (0, -0.5, 0), (34.5, 13, 26), merchant_body())
    return m, [piece(m, "Pelvis", (0, 12, 0), [hip, right_thigh, mirror(m, right_thigh), node])]


def build_merchant_hands():
    m = Model("Hands")
    cover = box(m, "R-Udenuki", (0, -2.5, 0), (10.5, 12, 14.5), fabric(AI, motifs=[pat("juji", IVORY, 0.6)]))
    ring = box(m, "R-Udenuki_Bord", (0, 3.2, 0), (11, 1.4, 15), plain(OCHRE))
    glove = box(m, "R-Bandes", (0, 1.5, 0), (11, 7, 15), wraps())
    hand = piece(m, "R-Hand", (0, -8, 0), [glove], offset=(0, -5, 0))
    right = piece(m, "R-Forearm", (-18, 40, 0), [cover, ring, hand])
    return m, [right, mirror(m, right)]


def build_merchant_legs():
    m = Model("Legs")
    pants = fabric(AI, motifs=[pat("juji", AI_LIGHT, 0.5)])
    upper = box(m, "R-Momohiki", (-0.3, 0, 0), (13.5, 23, 16.5), pants)
    knee = box(m, "R-Genou", (-0.2, 7.5, 0), (13, 9, 15.5), pants)
    wrap = box(m, "R-Kyahan", (0, -2.5, 0), (12, 19, 14.5), fabric(IVORY, motifs=[wrap_folds(IVORY_SHADE, (255, 255, 255), 6, 0.0)]))
    lace = [rod(m, "R-Lacet_%d" % i, (-6.2, 4 - i * 7, 7.4), (6.2, 1 - i * 7, 7.4), 0.8, plain(AI)) for i in range(2)]
    calf = piece(m, "R-Calf", (0, -12, 3), [knee, wrap] + lace, offset=(0, -12, -3))
    thigh = piece(m, "R-Thigh", (-8, -1, 1), [upper, calf], offset=(0, -9, 0))
    sock = box(m, "R-Tabi", (0, 1, 0), (15, 7, 21), white_tabi)
    sole = box(m, "R-Zori", (-0.6, -3, 0), (16, 1.5, 23), gk.straw_paint())
    edge = box(m, "R-Zori_Semelle", (-0.9, -3.9, 0), (16.5, 0.5, 23.5), plain(CHA_DARK))
    foot = piece(m, "R-Foot", (-7.85, 7, -3), [sock, sole, edge] + gk.thong(m, CRIMSON), offset=(0, -3, 6))
    waist = box(m, "Koshi", (0, 0, 0), (29, 11, 22), pants)
    return m, [piece(m, "Pelvis", (0, 51, 0), [waist, thigh, mirror(m, thigh)]), foot, mirror(m, foot)]


# ---------------------------------------------------------------------------------------------------------------------
# Grand Negociant du Comptoir (tenue des marchands du clan)
# ---------------------------------------------------------------------------------------------------------------------
PLUM, PLUM_LIGHT, PLUM_DARK = (78, 34, 66), (128, 70, 112), (40, 16, 36)
MATCHA, MATCHA_DARK = (54, 84, 52), (30, 48, 30)
GOLD_TONES = (GOLD_DARK, GOLD, GOLD_LIGHT)


def zeni_tile(S=12):
    """Pieces trouees (mon-sen) en quinconce : cercle et trou carre."""
    key = ("zeni", S)
    if key not in _TILES:
        segs = []
        for cx, cy in ((S / 2.0, S / 2.0), (0, 0), (S, 0), (0, S), (S, S)):
            r, q = S * 0.3, S * 0.09
            segs += circle_segs(cx, cy, r, 16)
            segs += [((cx - q, cy - q), (cx + q, cy - q)), ((cx + q, cy - q), (cx + q, cy + q)),
                     ((cx + q, cy + q), (cx - q, cy + q)), ((cx - q, cy + q), (cx - q, cy - q))]
        _TILES[key] = tile(S, S, segs, 0.5)
    return _TILES[key]


ZENI = lambda img: apply_tile(img, zeni_tile(), GOLD, 0.45)                   # noqa: E731


def negociant_body(ground=PLUM, mon=(), opening=False, wave=False):
    return fabric(ground, motifs=[ZENI, gradient(0.12)], mon=mon,
                  opening=[(1.0, IVORY), (1.4, GOLD), (40, MATCHA_DARK)] if opening else None,
                  wave=(10, PLUM_DARK, GOLD) if wave else None, lining=GOLD if wave else None)


def soroban(face, w, h, seed):
    """Boulier : cadre de laque, barre de separation, une perle en haut et quatre en bas par tige."""
    img = canvas(w, h, (70, 40, 26))
    if face in ("front", "back"):
        border(img, 0, LACQUER)
        border(img, 1, (98, 58, 36))
        mid = int(h * 0.36)
        band(img, mid, mid + 1, LACQUER)
        cols = max(3, (w - 4) // 3)
        for c in range(cols):
            x = 3 + c * (w - 6) / max(1, cols - 1)
            for y in range(2, h - 2):
                put(img, x, y, (40, 26, 18))
            disc(img, x, mid - 2, 1.2, (196, 120, 60))
            for k in range(4):
                disc(img, x, mid + 3 + k * 2.2, 1.2, (214, 140, 72) if (c + k) % 3 else (230, 180, 90))
    return finish(img, face, edges=False)


def build_negociant_head():
    m = Model("Head")
    glasses = []
    for side in (-1, 1):
        glasses.append(quad(m, "Lunette" if side > 0 else "R-Lunette", (5.2 * side, -1.5, 14.7), ring_img(12, GOLD, 0.72)))
        glasses.append(rod(m, "Branche" if side > 0 else "R-Branche", (9.4 * side, -1.4, 14.4), (15.3 * side, -1.0, 3.0), 0.6, metal(GOLD)))
    glasses.append(rod(m, "Pont", (-1.6, -1.2, 14.8), (1.6, -1.2, 14.8), 0.6, metal(GOLD)))
    kids = [box(m, "Zukin", (0, 11.5, 0), (31.5, 8, 31), negociant_body(PLUM_DARK)),
            box(m, "Zukin_Bouffant", (0, 16.5, -0.5), (34, 5, 33.5), negociant_body(PLUM, mon=[("top", 0.5, 0.5, 6, GOLD_TONES)])),
            box(m, "Zukin_Cordon", (0, 8.2, 0), (32.2, 1.5, 31.6), metal(GOLD))] + glasses
    return m, [piece(m, "Head", (0, 0, 0), kids)]


def build_negociant_chest():
    m = Model("Chest")
    fuki = hem_band(PLUM_DARK, GOLD)

    def arm():
        tamoto = box(m, "R-Tamoto", (-1, -1, -4.2), (14.5, 17, 26), negociant_body(wave=True))
        juban = box(m, "R-Juban", (-1, -10.5, 0), (13.5, 3, 15.8), plain(MATCHA))
        forearm = piece(m, "R-Forearm", (-1, 0, 0), [tamoto, juban])
        sode = box(m, "R-Sode", (-1, 0.5, -2), (14, 23, 22), negociant_body(mon=[("back", 0.5, 0.3, 4, GOLD_TONES)]))
        return piece(m, "R-Arm", (-17, 0, 0), [sode, forearm])

    def thigh():
        suso = box(m, "R-Suso", (-1, 0.5, 0), (17, 17, 23), negociant_body(wave=True))
        ring = box(m, "R-Fuki_Pan", (-1.6, -8, 0), (18, 2, 24), fuki)
        return piece(m, "R-Thigh", (-8, -1, 1), [suso, ring], offset=(0, -9, 0))

    right_arm, right_thigh = arm(), thigh()
    plate = box(m, "Torse", (0, 0, 0), (33, 24, 24), negociant_body(mon=[("back", 0.5, 0.3, 8, GOLD_TONES)], opening=True))
    col = stripes([(0.25, GOLD), (0.45, PLUM_DARK), (0.3, IVORY)])
    chest = piece(m, "Chest", (0, 5, 0), [plate] + gk.lapels(m, col, col, 4.5) + gk.neck_band(m, plain(IVORY)) + [right_arm, mirror(m, right_arm)], offset=(0, 11, 0))
    belly = box(m, "Ventre", (0, 0, 0), (31, 16, 23), negociant_body())
    obi = box(m, "Obi", (0, -2, 0), (35, 6, 26.5), fabric(GOLD_DARK, motifs=[pat("kikko", GOLD_LIGHT, 0.4, R=4)]))
    knot = box(m, "Obi_Noeud", (0, -2, -14.6), (12, 7, 3), fabric(GOLD_DARK, motifs=[pat("kikko", GOLD_LIGHT, 0.4, R=4)]))
    abacus = [box(m, "Soroban", (13.6, -9.5, 13.9), (9, 7, 1.4), soroban),
              rod(m, "Soroban_Cordon", (13.6, -6, 13.9), (12.4, -2, 13.6), 0.6, plain(CRIMSON))]
    purse = [box(m, "Kinchaku", (-14.2, -10.5, 14.4), (7, 8, 4.4), fabric(GOLD_DARK, motifs=[ZENI])),
             box(m, "Kinchaku_Col", (-14.2, -5.8, 14.4), (4.6, 1.8, 3.2), plain(CRIMSON_DARK)),
             rod(m, "Kinchaku_Cordon", (-14.2, -4.9, 14.4), (-12.8, -1, 13.8), 0.8, plain(GOLD))]
    coins = [rod(m, "Zeni_Cordon", (-8.2, -3.5, 14.8), (-8.2, -12.5, 14.8), 0.6, plain(CRIMSON))]
    coins += [box(m, "Zeni_%d" % i, (-8.2, -5.6 - i * 1.4, 14.8), (2.8, 0.9, 2.8), metal()) for i in range(5)]
    node = piece(m, "Belly", (0, 8, 0), [belly, obi, knot] + abacus + purse + coins + [chest], offset=(0, 4, 0))
    hip = box(m, "Hanches", (0, -0.5, 0), (34.5, 13, 26), negociant_body())
    return m, [piece(m, "Pelvis", (0, 12, 0), [hip, right_thigh, mirror(m, right_thigh), node])]


def build_negociant_hands():
    m = Model("Hands")
    cover = box(m, "R-Manchette", (0, -2.5, 0), (10.5, 12, 14.5), negociant_body(PLUM_DARK))
    ring = box(m, "R-Manchette_Bord", (0, 3.2, 0), (11, 1.4, 15), metal(GOLD))
    glove = box(m, "R-Gant", (0, 1.5, 0), (11, 7, 15), plain(IVORY))
    bague = box(m, "R-Bague", (0, -1.8, 0), (11.4, 1.2, 15.4), metal(GOLD))
    hand = piece(m, "R-Hand", (0, -8, 0), [glove, bague], offset=(0, -5, 0))
    right = piece(m, "R-Forearm", (-18, 40, 0), [cover, ring, hand])
    return m, [right, mirror(m, right)]


def build_negociant_legs():
    m = Model("Legs")
    hak = striped_hakama(CHA_DARK, GOLD_DARK, CHA, period=8, wave=(8, PLUM_DARK, GOLD))
    upper = box(m, "R-Hakama", (-0.3, 0, 0), (14.5, 23, 17.5), hak)
    lower = box(m, "R-Hakama_Bas", (-0.5, -2, 0), (15.5, 21, 18.5), hak)
    calf = piece(m, "R-Calf", (0, -12, 3), [lower], offset=(0, -12, -3))
    thigh = piece(m, "R-Thigh", (-8, -1, 1), [upper, calf], offset=(0, -9, 0))
    sock = box(m, "R-Tabi", (0, 1, 0), (15, 7, 21), white_tabi)
    sole = box(m, "R-Geta", (-0.6, -3.2, 0), (16, 1.6, 23), lacquer_c(LACQUER))
    teeth = [box(m, "R-Geta_Ha_%d" % i, (-0.6, -4.8, z), (15, 1.6, 2.2), lacquer_c(LACQUER)) for i, z in enumerate((-6, 6))]
    foot = piece(m, "R-Foot", (-7.85, 7, -3), [sock, sole] + teeth + gk.thong(m, CRIMSON), offset=(0, -3, 6))
    waist = box(m, "Koshi", (0, 0.25, 0), (33.5, 12.5, 26), hak)
    himo = front_himo(m, 13.4, plain(GOLD_DARK))
    return m, [piece(m, "Pelvis", (0, 51, 0), [waist] + himo + [thigh, mirror(m, thigh)]), foot, mirror(m, foot)]


# ---------------------------------------------------------------------------------------------------------------------
# Pelerin de la vallee
# ---------------------------------------------------------------------------------------------------------------------
NEZUMI, NEZUMI_LIGHT, NEZUMI_DARK = (92, 100, 116), (150, 158, 176), (52, 56, 68)


def pilgrim_body(mon=(), opening=False, wave=False):
    return fabric(NEZUMI, motifs=[pat("asanoha", NEZUMI_LIGHT, 0.35), gradient(0.12)], mon=mon,
                  opening=[(1.0, IVORY), (1.2, CRIMSON), (40, NEZUMI_DARK)] if opening else None,
                  wave=(10, NEZUMI_DARK, IVORY) if wave else None, lining=CRIMSON if wave else None)


MINO = fringe(fabric(STRAW, motifs=[straw_strands(STRAW, STRAW_DARK, 7), gradient(0.25)]), 8)


def build_pilgrim_head():
    m = Model("Head")
    panel = kasa_panel((176, 140, 84), (110, 80, 44), LACQUER, CRIMSON, mon_ground=LACQUER, stripes_n=2)
    kids = kasa_head(m, 16, 30.0, 8.5, 31.0, panel, NEZUMI_DARK, GOLD, CRIMSON)
    return m, [piece(m, "Head", (0, 0, 0), kids)]


def build_pilgrim_chest():
    m = Model("Chest")
    fuki = hem_band(NEZUMI_DARK, IVORY)

    def arm():
        tamoto = box(m, "R-Tamoto", (-1, -1, -4.2), (14.5, 17, 26), pilgrim_body(wave=True))
        juban = box(m, "R-Juban", (-1, -10.5, 0), (13.5, 3, 15.8), plain(IVORY))
        forearm = piece(m, "R-Forearm", (-1, 0, 0), [tamoto, juban])
        sode = box(m, "R-Sode", (-1, 0.5, -2), (14, 23, 22), pilgrim_body())
        return piece(m, "R-Arm", (-17, 0, 0), [sode, forearm])     # 2026-11-11 : mino d'epaule retire (pris pour du cuir)

    def thigh():
        suso = box(m, "R-Suso", (-1, 0.5, 0), (17, 17, 23), pilgrim_body(wave=True))
        ring = box(m, "R-Fuki_Pan", (-1.6, -8, 0), (18, 2, 24), fuki)
        return piece(m, "R-Thigh", (-8, -1, 1), [suso, ring], offset=(0, -9, 0))

    right_arm, right_thigh = arm(), thigh()
    plate = box(m, "Torse", (0, 0, 0), (33, 24, 24), pilgrim_body(mon=[("back", 0.5, 0.3, 7, (SILVER_DARK, SILVER, SILVER_LIGHT))], opening=True))
    col = stripes([(0.25, IVORY), (0.25, CRIMSON), (0.5, IVORY)])
    # Retour du 2026-11-11 : la cape de paille (mino, au dos et aux epaules) se lisait en jeu comme une armure de cuir
    # sanglee sur le kimono. Retiree ; col de coton blanc a la place du col de paille.
    cape = []
    collar = gk.neck_band(m, plain(IVORY), 12.4)
    chest = piece(m, "Chest", (0, 5, 0), [plate] + gk.lapels(m, col, col, 4.5) + collar + cape + [right_arm, mirror(m, right_arm)], offset=(0, 11, 0))

    belly = box(m, "Ventre", (0, 0, 0), (31, 16, 23), pilgrim_body())
    obi = box(m, "Obi", (0, -2, 0), (35, 6, 26.5), fabric(AI, motifs=[pat("kikko", AI_LIGHT, 0.5, R=4)]))
    cord = box(m, "Cordon", (0, -2, 0), (35.6, 1.4, 27.2), wraps(CRIMSON, CRIMSON_DARK))
    bell = [rod(m, "Suzu_Cordon", (9, -2, 13.8), (9, -7, 14.4), 0.6, plain(CRIMSON)), box(m, "Suzu", (9, -8.4, 14.5), (2.8, 2.8, 2.8), metal(GOLD_LIGHT))]
    node = piece(m, "Belly", (0, 8, 0), [belly, obi, cord] + bell + [chest], offset=(0, 4, 0))
    hip = box(m, "Hanches", (0, -0.5, 0), (34.5, 13, 26), pilgrim_body())
    return m, [piece(m, "Pelvis", (0, 12, 0), [hip, right_thigh, mirror(m, right_thigh), node])]


def build_pilgrim_hands():
    m = Model("Hands")
    cover = box(m, "R-Tekko", (0, -2.5, 0), (10.5, 12, 14.5), fabric(IVORY, motifs=[edge_piping(NEZUMI_DARK, 0), edge_piping(NEZUMI_DARK, 1)]))
    loop = box(m, "R-Tekko_Boucle", (0, -7.6, 0), (11.6, 1.2, 15.6), plain(CRIMSON))
    glove = box(m, "R-Gant", (0, 1.5, 0), (11, 7, 15), plain(IVORY))
    hand = piece(m, "R-Hand", (0, -8, 0), [glove], offset=(0, -5, 0))
    right = piece(m, "R-Forearm", (-18, 40, 0), [cover, loop, hand])
    return m, [right, mirror(m, right)]


def build_pilgrim_legs():
    m = Model("Legs")
    hak = striped_hakama(NEZUMI_DARK, NEZUMI_LIGHT, NEZUMI, 10)
    upper = box(m, "R-Hakama_Haut", (-0.5, -1, 0), (16, 24, 20), hak)
    knee = box(m, "R-Hakama_Genou", (-0.3, 7, 0), (14.5, 11, 17.5), hak)
    wrap = box(m, "R-Kyahan", (0, -3, 0), (12.5, 17, 15), fabric(IVORY, motifs=[wrap_folds(IVORY_SHADE, (255, 255, 255), 6, 0.0)]))
    lace = [rod(m, "R-Lacet_%d" % i, (-6.4, 3.5 - i * 6.5, 7.7), (6.4, 0.5 - i * 6.5, 7.7), 0.8, plain(CRIMSON)) for i in range(3)]
    calf = piece(m, "R-Calf", (0, -12, 3), [knee, wrap] + lace, offset=(0, -12, -3))
    thigh = piece(m, "R-Thigh", (-8, -1, 1), [upper, calf], offset=(0, -9, 0))
    sock = box(m, "R-Tabi", (0, 1, 0), (15, 7, 21), white_tabi)
    sole = box(m, "R-Waraji", (-0.6, -3.2, 0), (16, 1.5, 22), gk.straw_paint())
    ties = [rod(m, "R-Attache_Waraji", (-7.7, -1.8, -6), (7.4, -1.8, -6.5), 0.8, plain(STRAW_DARK))]
    foot = piece(m, "R-Foot", (-7.85, 7, -3), [sock, sole] + ties + gk.thong(m, STRAW_DARK), offset=(0, -3, 6))
    waist = box(m, "Koshi", (0, 0, 0), (29, 11, 23), hak)
    return m, [piece(m, "Pelvis", (0, 51, 0), [waist, thigh, mirror(m, thigh)]), foot, mirror(m, foot)]


# ---------------------------------------------------------------------------------------------------------------------
# Ombre (ordre d'assassins)
# ---------------------------------------------------------------------------------------------------------------------
SHADOW, SHADOW_LIGHT, SHADOW_DARK = (34, 34, 42), (70, 70, 84), (16, 16, 22)
BLOOD, BLOOD_DARK, BLOOD_LIGHT = (132, 20, 36), (72, 10, 20), (190, 60, 72)
CHAIN_FILL = chain_tile()


def chain_fill(x, y):
    return mix(STEEL, SHADOW, 0.35) if CHAIN_FILL[y % len(CHAIN_FILL)][x % len(CHAIN_FILL[0])] else SHADOW_DARK


def shadow_cloth(light=1.0, mon=(), opening=False, folds=False):
    motifs = [pat("asanoha", SHADOW_LIGHT, 0.45)]
    if folds:
        motifs.append(wrap_folds(SHADOW_DARK, SHADOW_LIGHT, 5, 0.2))
    return fabric(SHADOW, light, motifs=motifs + [gradient(0.1)], mon=mon,
                  opening=[(1.0, BLOOD), (40, SENTINEL)] if opening else None, fill=chain_fill)


def lamellar(face, w, h, seed):
    """Lames d'acier bruni lacees de cramoisi."""
    img = canvas(w, h, STEEL_DARK)
    for y in range(h):
        for x in range(w):
            t = (y % 5) / 4.0
            img[y][x][:3] = list(mix(STEEL, SHADOW_DARK, t * 0.8))
    if face not in ("top", "bottom"):
        for y in range(2, h, 5):
            for x in range(2, w - 1, 4):
                put(img, x, y, BLOOD_LIGHT)
                put(img, x + 1, y, BLOOD)
    border(img, 0, SHADOW_DARK)
    return finish(img, face, edges=False)


def steel_splints(face, w, h, seed):
    img = canvas(w, h, STEEL_DARK)
    for y in range(h):
        for x in range(w):
            k = 1 + 0.2 * math.sin(x * 0.9 + y * 0.3)
            img[y][x][:3] = [clamp(v * k) for v in STEEL]
    if face not in ("top", "bottom"):
        for x in range(0, w, 4):
            rect(img, x, 0, x + 1, h, SHADOW_DARK)
        for y in (2, h - 3):
            for x in range(1, w, 4):
                put(img, x + 1, y, STEEL_LIGHT)
    return finish(img, face, edges=False)


def hachigane(face, w, h, seed):
    img = canvas(w, h, STEEL)
    for y in range(h):
        for x in range(w):
            k = 1 + 0.18 * math.sin(x * 0.5 + y * 0.9)
            img[y][x][:3] = [clamp(v * k) for v in STEEL]
    if face == "front":
        kamon(img, w / 2.0, h / 2.0, h / 2.0 - 0.5, STEEL, (SHADOW_DARK, STEEL_DARK, STEEL_LIGHT))
        for x in (1, w - 2):
            put(img, x, h / 2.0, STEEL_LIGHT)
    border(img, 0, STEEL_DARK)
    return finish(img, face, edges=False)


def mask_paint(face, w, h, seed):
    img = cloth(w, h, SHADOW, seed, 0.1, 0.03)
    if face not in ("top", "bottom"):
        for y, row in enumerate(img):
            for x, p in enumerate(row):
                if x % 7 == 0:
                    put(img, x, y, SHADOW_DARK, 0.6)
                elif x % 7 == 1:
                    put(img, x, y, SHADOW_LIGHT, 0.3)
        band(img, h - 2, h, SHADOW_DARK)
    return finish(img, face)


def build_shadow_head():
    m = Model("Head")
    hood = shadow_cloth(folds=True)
    kids = [box(m, "Capuche", (0, 11, -0.3), (32, 8, 30.6), hood),
            box(m, "Capuche_Nuque", (0, -1, -15.4), (31.2, 24, 2), hood),
            box(m, "Masque", (0, -8.2, 15.1), (31.4, 11, 1.2), mask_paint),
            box(m, "Masque_Bord", (0, -2.6, 15.4), (32.4, 1, 1.2), plain(BLOOD)),
            box(m, "Bandeau", (0, 7, -0.3), (32.6, 2, 31.2), plain(BLOOD)),
            box(m, "Hachigane", (0, 7.6, 15.9), (12, 5, 1), hachigane),
            box(m, "Noeud", (0, 7, -16.8), (5, 4, 2.5), plain(BLOOD))]
    kids += sym(m, box(m, "R-Capuche_Joue", (-15.4, -1.5, -0.5), (2, 22.6, 29.8), hood))
    kids += sym(m, plank(m, "R-Ruban", (-3, -13, -20.5), (-1.2, 6, -17.4), 2.5, 0.6, plain(BLOOD)))
    kids.append(plank(m, "Pan_Capuche", (0, -19, -17.6), (0, -9, -16.6), 18, 1, hood))
    return m, [piece(m, "Head", (0, 0, 0), kids)]


def kunai(m, name, x, y, z):
    """Kunai pointe en bas : lame en quad, poignee enroulee, anneau."""
    return [quad(m, name + "_Lame", (x, y - 3.4, z), kunai_blade_img()),
            rod(m, name + "_Poignee", (x, y - 0.4, z), (x, y + 2.8, z), 1.1, wraps(BLOOD, BLOOD_DARK)),
            quad(m, name + "_Anneau", (x, y + 4.0, z), ring_img(5, STEEL))]


def build_shadow_chest():
    m = Model("Chest")

    def arm():
        sode = box(m, "R-Sode", (-1, -1, -1), (11.5, 22, 14.5), shadow_cloth(mon=[("back", 0.5, 0.45, 3.5, GOLDS)]))
        tie = box(m, "R-Sode_Lien", (-1, -10.8, -1), (12, 1.4, 15), plain(BLOOD))
        pad = box(m, "R-Kataate", (-1.6, 10.6, -1), (13, 3, 16), lamellar, q=q_axis((0, 0, 1), 12))
        pad2 = box(m, "R-Kataate_Bas", (-2.2, 8, -1), (12.6, 2.6, 15.6), lamellar, q=q_axis((0, 0, 1), 12))
        return piece(m, "R-Arm", (-17, 0, 0), [sode, tie, pad, pad2])

    right_arm = arm()
    plate = box(m, "Torse", (0, 0, 0), (32, 24, 23), shadow_cloth(mon=[("back", 0.5, 0.56, 7, (BLOOD_DARK, BLOOD, BLOOD_LIGHT))], opening=True))
    col_over = stripes([(0.3, BLOOD), (0.7, SHADOW_LIGHT)])
    a, b = (-12, 11.5), (11, -12)
    belt = strap_box(m, "Baudrier", a, b, 14.1, 2.8, 0.8, fabric(LEATHER, motifs=[lambda img: [stitch_line(img, [(1, 0), (1, len(img))], BLOOD_LIGHT, 2), stitch_line(img, [(len(img[0]) - 2, 0), (len(img[0]) - 2, len(img))], BLOOD_LIGHT, 2)]]))
    blades = []
    for i, t in enumerate((0.32, 0.5, 0.68)):
        blades += kunai(m, "Kunai_%d" % i, a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t, 14.9)
    chest = piece(m, "Chest", (0, 5, 0), [plate] + gk.lapels(m, col_over, plain(BLOOD), 3) + gk.neck_band(m, plain(SHADOW_LIGHT), 12.1)
                  + [belt] + blades + [right_arm, mirror(m, right_arm)], offset=(0, 11, 0))

    belly = box(m, "Ventre", (0, 0, 0), (30, 16, 22), shadow_cloth())
    obi = box(m, "Obi", (0, -2.5, 0), (34.5, 6, 26.5), fabric(SHADOW_DARK, motifs=[pat("kikko", SHADOW_LIGHT, 0.5, R=4)]))
    cord = box(m, "Cordon", (0, -2.5, 0), (35.5, 1.5, 27.5), wraps(BLOOD, BLOOD_DARK))
    knot = [box(m, "Cordon_Noeud", (5, -2.5, 14.3), (3, 3, 1.6), plain(BLOOD)),
            rod(m, "Cordon_Bout_A", (4.4, -3.6, 14.6), (3.6, -10, 14.9), 1.0, plain(BLOOD)),
            rod(m, "Cordon_Bout_B", (5.6, -3.6, 14.6), (7, -9, 14.9), 1.0, plain(BLOOD))]
    stars = [quad(m, "Shuriken_%d" % i, (x, -1.4, 14.05), shuriken_img()) for i, x in enumerate((-8.5, -12.5))]
    tanto = [rod(m, "Tanto_Tsuka", (-8, -1.6, -16.2), (-15, -1, -16), 1.8, wraps(BLOOD, BLOOD_DARK)),
             rod(m, "Tanto_Saya", (-8, -1.6, -16.2), (12, -3.2, -16.4), 2.2, lacquer_c(BLOOD_DARK)),
             rod(m, "Tanto_Tsuba", (-8.6, -1.55, -16.2), (-7.4, -1.65, -16.2), 3.4, metal(STEEL)),
             rod(m, "Tanto_Kojiri", (10.5, -3.1, -16.4), (12, -3.2, -16.4), 3.0, metal(STEEL))]
    pouches = []
    for side in (-1, 1):
        n = "R" if side < 0 else "L"
        pouches += [box(m, "Sacoche_" + n, (side * 9.5, -9.2, -12.6), (6, 6, 3.4), fabric(LEATHER, motifs=[edge_piping(LEATHER_DARK)])),
                    box(m, "Sacoche_Rabat_" + n, (side * 9.5, -6.9, -14.5), (6.6, 2.4, 0.8), plain(LEATHER_DARK))]
    node = piece(m, "Belly", (0, 8, 0), [belly, obi, cord] + knot + stars + tanto + pouches + [chest], offset=(0, 4, 0))
    hip = box(m, "Hanches", (0, -0.5, 0), (32.5, 13, 25), shadow_cloth())
    return m, [piece(m, "Pelvis", (0, 12, 0), [hip, node])]


def build_shadow_hands():
    m = Model("Hands")
    wrap = box(m, "R-Bandes", (0, -2, 0), (10, 12, 14), wraps(SHADOW_LIGHT, SHADOW, edge=BLOOD))
    mail = box(m, "R-Kusari", (-5.3, -2, 0), (1.2, 11, 10), fabric(SHADOW_DARK, motifs=[pat("chain", STEEL, 1.0)]))
    splint = box(m, "R-Tekko", (-6.1, -2, 0), (1.2, 10, 7), steel_splints)
    laces = [box(m, "R-Lacet_%d" % i, (0, y, 0), (10.6, 1, 14.6), plain(BLOOD)) for i, y in enumerate((2.0, -6.2))]
    glove = box(m, "R-Gant", (0, 1.5, 0), (11, 8, 15), fabric(LEATHER_DARK, motifs=[edge_piping(BLOOD)]))
    knuckle = box(m, "R-Phalanges", (-0.2, 3.5, 6.3), (10, 3, 3), steel_splints)
    hand = piece(m, "R-Hand", (0, -8, 0), [glove, knuckle], offset=(0, -5, 0))
    right = piece(m, "R-Forearm", (-18, 40, 0), [wrap, mail, splint] + laces + [hand])
    return m, [right, mirror(m, right)]


def jika_tabi(face, w, h, seed):
    img = cloth(w, h, SHADOW_DARK, seed, 0.06, 0.02)
    if face == "front":
        rect(img, w // 3, 0, w // 3 + 1, h, (0, 0, 0))
    if face == "back":
        for y in range(2, h - 1, 3):
            put(img, w / 2.0, y, STEEL_LIGHT)
    if face in ("left", "right"):
        stitch_line(img, [(1, h - 2), (w - 1, h - 2)], SHADOW_LIGHT, 2)
    return finish(img, face, edges=False)


def build_shadow_legs():
    m = Model("Legs")
    pants = shadow_cloth()

    def thigh(holster):
        upper = box(m, "R-Pantalon", (-0.3, 0, 0), (13.5, 23, 16.5), pants)
        pad = box(m, "R-Genouillere", (-0.2, 7.5, 0.4), (13.6, 8, 16.2), fabric(SHADOW_DARK, motifs=[lambda img: lozenges(img, 0, len(img), 5, SHADOW_LIGHT, 0.45)]))
        wrap = box(m, "R-Kyahan", (0, -2.5, 0), (12, 19, 14.5), wraps(SHADOW_LIGHT, SHADOW))
        lace = [rod(m, "R-Lacet_%d" % i, (-6.2, 5 - i * 6, 7.4), (6.2, 1 - i * 6, 7.4), 0.9, plain(BLOOD)) for i in range(3)]
        lace += [rod(m, "R-Lacet_X%d" % i, (6.2, 5 - i * 6, 7.4), (-6.2, 1 - i * 6, 7.4), 0.9, plain(BLOOD)) for i in range(3)]
        calf = piece(m, "R-Calf", (0, -12, 3), [pad, wrap] + lace, offset=(0, -12, -3))
        kids = [upper, calf]
        if holster:
            kids += [box(m, "R-Sangle_Haut", (-0.3, 4, 0), (14.4, 1.6, 17.4), plain(LEATHER)),
                     box(m, "R-Sangle_Bas", (-0.3, -4, 0), (14.4, 1.6, 17.4), plain(LEATHER)),
                     box(m, "R-Etui", (-7.9, -0.5, 1.5), (2.2, 9.5, 6), fabric(LEATHER, motifs=[edge_piping(LEATHER_DARK)]))]
            for i, z in enumerate((0.0, 3.0)):
                kids += [rod(m, "R-Kunai_Poignee_%d" % i, (-7.9, 4.2, z), (-7.9, 7.6, z), 1.0, wraps(BLOOD, BLOOD_DARK)),
                         quad(m, "R-Kunai_Anneau_%d" % i, (-8.0, 8.8, z), ring_img(5, STEEL), q=q_axis((0, 1, 0), 90))]
        return piece(m, "R-Thigh", (-8, -1, 1), kids, offset=(0, -9, 0))

    sock = box(m, "R-Jika_Tabi", (0, 1, 0), (15, 7, 21), jika_tabi)
    sole = box(m, "R-Semelle", (-0.6, -3, 0), (15.6, 1.6, 21.6), plain((8, 8, 10)))
    ankle = box(m, "R-Cheville", (-0.4, 3.8, -1), (15.4, 1.4, 21.4), plain(BLOOD))
    foot = piece(m, "R-Foot", (-7.85, 7, -3), [sock, sole, ankle], offset=(0, -3, 6))
    waist = box(m, "Koshi", (0, 0, 0), (29, 11, 22), pants)
    left = mirror(m, thigh(False))
    return m, [piece(m, "Pelvis", (0, 51, 0), [waist, thigh(True), left]), foot, mirror(m, foot)]


# ---------------------------------------------------------------------------------------------------------------------
# Nobles : haori et hakama, une palette par variante
# ---------------------------------------------------------------------------------------------------------------------
NOBLE_PALETTES = {
    #            haori            reflet            metal du mon et des liseres          kimono dessous    hakama (fond, reflet, rayure)       accent
    "Noble": (KURO, KURO_LIGHT, GOLDS, (60, 56, 70), ((60, 62, 72), (124, 126, 140), (24, 24, 30)), CRIMSON),
    "Noble_Cramoisi": ((132, 20, 36), (198, 64, 78), GOLDS, KURO, ((40, 38, 46), (96, 92, 108), (132, 20, 36)), KURO),
    "Noble_Jade": ((228, 224, 214), (250, 248, 242), (JADE_DARK, JADE, JADE_LIGHT), (40, 96, 80), ((40, 92, 78), (96, 160, 138), (228, 224, 214)), JADE),
    "Noble_Violet": ((62, 30, 96), (126, 82, 168), (SILVER_DARK, SILVER, SILVER_LIGHT), KURO, ((48, 46, 56), (110, 106, 124), (126, 82, 168)), (150, 120, 190)),
}


def noble_builders(name):
    haori, haori_light, metal_r, under, hakama, accent = NOBLE_PALETTES[name]
    trim = metal_r[1]
    damask = pat("kikko", haori_light, 0.22)

    def body(mon=(), opening=None, wave=False, lining=None):
        return fabric(haori, motifs=[damask, gradient(0.1)], mon=mon, opening=opening,
                      wave=(10, mix(haori, (0, 0, 0), 0.35), trim) if wave else None, lining=lining)

    def head():
        m = Model("Head")
        kids = eboshi(m, (10, 22, 19), metal_r)
        kids += gk.cords_head(m, plain(accent))
        return m, [piece(m, "Head", (0, 0, 0), kids)]

    def chest():
        m = Model("Chest")

        def arm():
            tamoto = box(m, "R-Tamoto", (-1, -1, -5.7), (15, 19, 30), body(mon=[("back", 0.5, 0.35, 4, metal_r)], wave=True, lining=accent))
            juban = box(m, "R-Juban", (-1, -11.8, 0), (14, 3, 16), plain(IVORY))
            forearm = piece(m, "R-Forearm", (-1, 0, 0), [tamoto, juban])
            sode = box(m, "R-Sode", (-1, 0.5, -2), (14, 23, 22), body(mon=[("back", 0.5, 0.3, 4.5, metal_r)]))
            return piece(m, "R-Arm", (-17, 0, 0), [sode, forearm])

        def thigh():
            suso = box(m, "R-Suso", (-1, 3.5, 0), (17, 11, 23), body(wave=True))
            return piece(m, "R-Thigh", (-8, -1, 1), [suso], offset=(0, -9, 0))

        right_arm, right_thigh = arm(), thigh()
        plate = box(m, "Torse", (0, 0, 0), (33, 24, 24),
                    body(mon=[("back", 0.5, 0.3, 8.5, metal_r), ("front", 0.14, 0.24, 4, metal_r), ("front", 0.86, 0.24, 4, metal_r)],
                         opening=[(1.0, IVORY), (1.2, under), (40, KURO_DARK)]))
        col = stripes([(0.3, haori), (0.2, trim), (0.25, IVORY), (0.25, under)])
        himo = plain(trim)
        ties = sym(m, rod(m, "R-Himo", (-4.5, -3, 14.0), (-0.8, -6.5, 14.4), 1.2, himo))
        knot = [box(m, "Himo_Noeud", (0, -7, 14.5), (2.4, 2.4, 1.6), metal(trim)), box(m, "Himo_Pompon", (0, -10.2, 14.5), (2, 4, 1.1), himo)]
        chest_node = piece(m, "Chest", (0, 5, 0), [plate] + gk.lapels(m, col, col, 5) + gk.neck_band(m, plain(IVORY)) + ties + knot
                           + [right_arm, mirror(m, right_arm)], offset=(0, 11, 0))

        def hakata(img):
            w = len(img[0])
            for x in range(w):
                p = x % 12
                if p in (0, 1, 10, 11):
                    rect(img, x, 0, x + 1, len(img), trim, 0.8)
                elif p in (5, 6):
                    rect(img, x, 0, x + 1, len(img), accent, 0.7)
        belly = box(m, "Ventre", (0, 0, 0), (31, 16, 23), body())
        obi = box(m, "Obi", (0, -2, 0), (35, 6, 26.5), fabric(KURO_DARK, motifs=[hakata]))
        fan = sensu(m, (8, 0.5, 13.9), -10, KURO_DARK, trim)
        node = piece(m, "Belly", (0, 8, 0), [belly, obi] + fan + [chest_node], offset=(0, 4, 0))
        hip = box(m, "Hanches", (0, -0.5, 0), (34.5, 13, 26), body())
        return m, [piece(m, "Pelvis", (0, 12, 0), [hip, right_thigh, mirror(m, right_thigh), node])]

    def hands():
        m = Model("Hands")
        cuff = box(m, "R-Manchette", (0, -3, 0), (12, 11, 16), fabric(haori, motifs=[damask], mon=[("left", 0.5, 0.45, 3.5, metal_r)]))
        ring = box(m, "R-Liseré", (0, 2.2, 0), (12.4, 1.2, 16.4), metal(trim))
        glove = box(m, "R-Gant", (0, 1.5, 0), (11, 9, 15), plain(IVORY))
        hand = piece(m, "R-Hand", (0, -8, 0), [glove], offset=(0, -5, 0))
        right = piece(m, "R-Forearm", (-18, 40, 0), [cuff, ring, hand])
        return m, [right, mirror(m, right)]

    def legs():
        m = Model("Legs")
        hak = striped_hakama(hakama[0], hakama[2], hakama[1], 10)
        hak_low = striped_hakama(hakama[0], hakama[2], hakama[1], 10, wave=(12, mix(hakama[0], (0, 0, 0), 0.3), trim))
        upper = box(m, "R-Hakama_Haut", (-0.5, -1, 0), (16, 24, 20), hak)
        mid = box(m, "R-Hakama_Genou", (-1.3, 5, -0.5), (17.5, 13, 23.5), hak)
        low = box(m, "R-Hakama_Bas", (-2.6, -6, -1), (20, 11, 28), hak_low)
        ring = box(m, "R-Hakama_Ourlet", (-3.1, -11.2, -1), (21, 1.5, 29), hem_band(hakama[0], trim))
        calf = piece(m, "R-Calf", (0, -12, 3), [mid, low, ring], offset=(0, -12, -3))
        thigh = piece(m, "R-Thigh", (-8, -1, 1), [upper, calf], offset=(0, -9, 0))
        sock = box(m, "R-Tabi", (0, 1, 0), (15, 7, 21), white_tabi)
        sole = box(m, "R-Zori", (-0.6, -3, 0), (16, 1.5, 23), lacquer_c(KURO_DARK))
        edge = box(m, "R-Zori_Semelle", (-0.9, -3.9, 0), (16.5, 0.5, 23.5), plain(trim))
        foot = piece(m, "R-Foot", (-7.85, 7, -3), [sock, sole, edge] + gk.thong(m, accent), offset=(0, -3, 6))
        waist = box(m, "Koshi", (0, 0, 0), (29, 11, 23), hak)
        board = box(m, "Koshi_Ita", (0, 1.5, -12.6), (14, 8, 2), fabric(hakama[0], mon=[("back", 0.5, 0.5, 5, metal_r)], motifs=[edge_piping(trim)]))
        return m, [piece(m, "Pelvis", (0, 51, 0), [waist, board, thigh, mirror(m, thigh)]), foot, mirror(m, foot)]

    return {"Head": head, "Chest": chest, "Hands": hands, "Legs": legs}


# ---------------------------------------------------------------------------------------------------------------------
# Seigneur et Doyens : kamishimo
# ---------------------------------------------------------------------------------------------------------------------
SG_KIMONO, SG_KIMONO_LIGHT = (40, 22, 30), (96, 60, 72)
SG_KATAGINU = (14, 13, 18)


def gold_embroidery(img):
    """Broderie d'or du bord : double filet et points."""
    border(img, 0, GOLD_DARK)
    border(img, 1, GOLD)
    border(img, 3, GOLD_DARK)
    h, w = len(img), len(img[0])
    for x in range(4, w - 4, 4):
        put(img, x, 2, GOLD_LIGHT)
        put(img, x, h - 3, GOLD_LIGHT)
    for y in range(4, h - 4, 4):
        put(img, 2, y, GOLD_LIGHT)
        put(img, w - 3, y, GOLD_LIGHT)


def kataginu_paint(mon=(), edge=True):
    return fabric(SG_KATAGINU, motifs=[uroko(GOLD_DARK, 10, 0.22), gradient(0.06)] + ([gold_embroidery] if edge else []), mon=mon,
                  tb_motifs=[uroko(GOLD_DARK, 10, 0.22)] + ([gold_embroidery] if edge else []))


def lord_kimono(wave=False, lining=None):
    return fabric(SG_KIMONO, motifs=[uroko(GOLD_DARK, 10, 0.32), gradient(0.12)],
                  wave=(12, (24, 12, 18), GOLD) if wave else None, lining=lining)


def build_lord_head():
    m = Model("Head")
    kids = eboshi(m, (13, 28, 22), GOLDS)
    kids.append(box(m, "Epingle", (0, 20, -4.6), (38, 1.5, 1.5), metal(GOLD_LIGHT)))
    kids += sym(m, box(m, "R-Epingle_Jade", (-19.5, 20, -4.6), (2.6, 2.6, 2.6), metal(JADE)))
    tie = plain(GOLD_DARK)
    kids += sym(m, rod(m, "R-Cordon", (-15.7, 8, 3), (-15.7, -13, 5), 1, tie), box(m, "R-Pompon", (-15.7, -15.5, 5), (2.5, 5, 2.5), metal()))
    return m, [piece(m, "Head", (0, 0, 0), kids)]


def build_lord_chest():
    m = Model("Chest")

    def arm():
        tamoto = box(m, "R-Tamoto", (-1, -1, -5.7), (15, 19, 30), lord_kimono(wave=True, lining=GOLD))
        juban = box(m, "R-Juban", (-1, -11.8, 0), (14, 3, 16), plain(IVORY))
        forearm = piece(m, "R-Forearm", (-1, 0, 0), [tamoto, juban])
        sode = box(m, "R-Sode", (-1, 0.5, -2), (14, 23, 22), lord_kimono())
        wing = box(m, "R-Kataginu_Aile", (-6, 12.6, -1.2), (21, 2.6, 26), kataginu_paint(mon=[("top", 0.45, 0.5, 6, GOLDS)]), q=q_axis((0, 0, 1), 8))
        return piece(m, "R-Arm", (-17, 0, 0), [sode, wing, forearm])

    right_arm = arm()
    plate = box(m, "Torse", (0, 0, 0), (33.5, 24, 24.5),
                kataginu_paint(mon=[("back", 0.5, 0.32, 9.5, GOLDS), ("front", 0.15, 0.26, 4.5, GOLDS), ("front", 0.85, 0.26, 4.5, GOLDS)], edge=False))
    col = stripes([(0.18, GOLD), (0.3, SG_KIMONO), (0.22, CRIMSON), (0.3, IVORY)])
    chest = piece(m, "Chest", (0, 5, 0), [plate] + gk.lapels(m, col, col, 6) + gk.neck_band(m, plain(IVORY)) + [right_arm, mirror(m, right_arm)], offset=(0, 11, 0))

    def brocade(img):
        lozenges(img, 1, len(img) - 1, 6, GOLD, 0.45)
        band(img, 0, 1, GOLD)
        band(img, len(img) - 1, len(img), GOLD)
    belly = box(m, "Ventre", (0, 0, 0), (31.5, 16, 23.5), kataginu_paint(edge=False))
    obi = box(m, "Obi", (0, -2, 0), (35.5, 6.5, 27), fabric(CRIMSON_DARK, motifs=[brocade]))
    fan = sensu(m, (8.5, 0.5, 14.1), -10, KURO_DARK, GOLD)
    inro = [box(m, "Netsuke", (-11, 2.6, 14.2), (2.8, 2.8, 2.8), metal(JADE)),
            rod(m, "Inro_Cordon", (-11, 1.2, 14.6), (-11, -9.5, 15.2), 0.6, plain(GOLD)),
            box(m, "Ojime", (-11, -10, 15.2), (1.6, 1.6, 1.6), metal(CRIMSON)),
            box(m, "Inro", (-11, -14.4, 15.2), (5, 6.5, 2.6), lacquer_c(KURO_DARK, GOLD, ("front", GOLDS))),
            box(m, "Inro_Rang", (-11, -12.6, 15.2), (5.4, 0.6, 3.0), metal())]
    node = piece(m, "Belly", (0, 8, 0), [belly, obi] + fan + inro + [chest], offset=(0, 4, 0))
    hip = box(m, "Hanches", (0, -0.5, 0), (32.5, 12, 24.5), lord_kimono())
    return m, [piece(m, "Pelvis", (0, 12, 0), [hip, node])]


def build_lord_hands():
    m = Model("Hands")
    cuff = box(m, "R-Manchette", (0, -3, 0), (12, 11, 16), kataginu_paint(mon=[("left", 0.5, 0.45, 4, GOLDS)]))
    glove = box(m, "R-Gant", (0, 1.5, 0), (11, 9, 15), plain(KURO))
    ring = box(m, "R-Bague", (0, -2.2, 0), (11.4, 1.3, 15.4), metal())
    hand = piece(m, "R-Hand", (0, -8, 0), [glove, ring], offset=(0, -5, 0))
    right = piece(m, "R-Forearm", (-18, 40, 0), [cuff, hand])
    return m, [right, mirror(m, right)]


def build_lord_legs():
    m = Model("Legs")
    hak = striped_hakama(KURO, GOLD_DARK, KURO_LIGHT, 12)
    upper = box(m, "R-Hakama_Haut", (-1, -1, 0), (17, 24, 22), hak)
    mid = box(m, "R-Hakama_Genou", (-2.1, 5, -0.5), (19, 13, 25.5), hak)
    low = box(m, "R-Hakama_Bas", (-3.7, -6.5, -1), (22, 11, 30), hak)
    trail = box(m, "R-Hakama_Long", (-4.1, -13.6, -1.6), (22.6, 5, 32), fabric(KURO, wave=(10, KURO_DARK, GOLD), tb=KURO))
    calf = piece(m, "R-Calf", (0, -12, 3), [mid, low, trail], offset=(0, -12, -3))
    thigh = piece(m, "R-Thigh", (-8, -1, 1), [upper, calf], offset=(0, -9, 0))
    sock = box(m, "R-Tabi", (0, 1, 0), (15, 7, 21), white_tabi)
    sole = box(m, "R-Zori", (-0.6, -3, 0), (16, 1.5, 23), lacquer_c(KURO_DARK))
    foot = piece(m, "R-Foot", (-7.85, 7, -3), [sock, sole] + gk.thong(m, GOLD), offset=(0, -3, 6))
    waist = box(m, "Koshi", (0, 0.25, 0), (35, 12.5, 27), hak)
    board = box(m, "Koshi_Ita", (0, 1.3, -14.6), (15, 8.6, 2), kataginu_paint(mon=[("back", 0.5, 0.5, 5.5, GOLDS)]))
    himo = front_himo(m, 13.9, plain(GOLD_DARK), half=14.5)
    return m, [piece(m, "Pelvis", (0, 51, 0), [waist, board] + himo + [thigh, mirror(m, thigh)]), foot, mirror(m, foot)]


# ---------------------------------------------------------------------------------------------------------------------
# Robe de Cour (nobles de salon) : robes superposees aux bords de couleurs (kasane), manches trainantes, traine, haute coiffe
# ---------------------------------------------------------------------------------------------------------------------
COUR, COUR_LIGHT, COUR_DARK = (108, 26, 52), (176, 70, 98), (58, 12, 28)
LAVENDER = (150, 128, 196)
KASANE = (CRIMSON, GOLD, JADE, IVORY)                                          # bords des robes de dessous, du dessus au fond


def court_body(mon=(), opening=False, wave=False, lining=None):
    return fabric(COUR, motifs=[pat("kikko", GOLD, 0.22, R=6), gradient(0.12)], mon=mon,
                  opening=[(0.9, IVORY), (0.9, JADE), (0.9, GOLD), (1.0, CRIMSON), (40, COUR_DARK)] if opening else None,
                  wave=(12, COUR_DARK, GOLD) if wave else None, lining=lining)


def kasane_rings(m, prefix, pos, size, step=1.6, grow=(-1.2, -2.0)):
    """Bords des robes de dessous : un anneau par robe, chacun un peu plus bas et plus etroit que le precedent."""
    x, y, z = pos
    w, h, d = size
    out = []
    for i, c in enumerate(KASANE):
        out.append(box(m, "%s_Kasane_%d" % (prefix, i), (x, y - i * step, z), (w + i * grow[0], h, d + i * grow[1]), hem_band(c, mix(c, (0, 0, 0), 0.3))))
    return out


def build_court_head():
    m = Model("Head")
    tail = lambda face, w, h, seed: gk.ei_tail(face, w, h, seed)                 # noqa: E731
    kids = [box(m, "Coiffe", (0, 10.5, 0), (32, 9, 31), lacquer_c(KURO_DARK)),
            box(m, "Bandeau", (0, 6.8, 0), (32.6, 2.4, 31.6), metal(GOLD)),
            box(m, "Koji", (0, 20.5, -6), (12, 14, 10), lacquer_c(KURO_DARK)),
            box(m, "Koji_Anneau", (0, 15.5, -6), (13.4, 2, 11.4), metal(GOLD)),
            box(m, "Kanzashi", (0, 22, -6), (40, 1.4, 1.4), metal(GOLD_LIGHT)),
            mon_quad(m, "Mon", (0, 10.5, 15.6), 7, KURO_DARK),
            plank(m, "Ei", (0, 22, -11), (0, 8, -24), 9, 1.4, tail)]
    for side in (-1, 1):                                                         # pampilles d'or de part et d'autre
        x = 20.5 * side
        kids.append(box(m, "Kanzashi_Fleur" if side > 0 else "R-Kanzashi_Fleur", (x, 22, -6), (3, 3, 3), metal(CRIMSON)))
        for i in range(3):
            kids.append(rod(m, ("Pampille_%d" if side > 0 else "R-Pampille_%d") % i, (x, 21, -6 + (i - 1) * 1.6),
                            (x, 13 - i * 2, -6 + (i - 1) * 1.6), 0.5, metal(GOLD)))
            kids.append(box(m, ("Pampille_Perle_%d" if side > 0 else "R-Pampille_Perle_%d") % i, (x, 12.4 - i * 2, -6 + (i - 1) * 1.6), (1.4, 1.4, 1.4), metal(GOLD_LIGHT)))
    return m, [piece(m, "Head", (0, 0, 0), kids)]


def build_court_chest():
    m = Model("Chest")

    def arm():
        tamoto = box(m, "R-Tamoto", (-1.5, -2.5, -8.7), (17, 24, 38), court_body(wave=True, lining=CRIMSON))
        rings = kasane_rings(m, "R-Manche", (-1.5, -15.2, -6.5), (17.5, 1.6, 34))
        forearm = piece(m, "R-Forearm", (-1, 0, 0), [tamoto] + rings)
        sode = box(m, "R-Sode", (-1.5, 0.5, -3.5), (16, 24, 27), court_body(mon=[("back", 0.5, 0.3, 4.5, GOLDS)]))
        return piece(m, "R-Arm", (-17, 0, 0), [sode, forearm])

    def thigh():
        suso = box(m, "R-Suso", (-1.3, -2.3, 0), (17.5, 22, 24), court_body(wave=True))
        rings = kasane_rings(m, "R-Pan", (-1.75, -13.4, 0), (18.4, 1.6, 24.9), 1.5, (-0.7, -0.7))
        return piece(m, "R-Thigh", (-8, -1, 1), [suso] + rings, offset=(0, -9, 0))

    right_arm, right_thigh = arm(), thigh()
    plate = box(m, "Torse", (0, 0, 0), (34, 24, 25), court_body(mon=[("back", 0.5, 0.3, 8.5, GOLDS)], opening=True))
    col = stripes([(0.16, COUR), (0.14, CRIMSON), (0.14, GOLD), (0.14, JADE), (0.14, LAVENDER), (0.28, IVORY)])
    chest = piece(m, "Chest", (0, 5, 0), [plate] + gk.lapels(m, col, col, 7) + gk.neck_band(m, plain(IVORY)) + [right_arm, mirror(m, right_arm)], offset=(0, 11, 0))

    def brocade(img):
        lozenges(img, 1, len(img) - 1, 6, CRIMSON, 0.5)
        band(img, 0, 1, GOLD_LIGHT)
        band(img, len(img) - 1, len(img), GOLD_LIGHT)
    belly = box(m, "Ventre", (0, 0, 0), (32, 16, 24), court_body())
    obi = box(m, "Obi", (0, -2.5, 0), (36.5, 9, 28), fabric(GOLD_DARK, motifs=[brocade]))
    cord = box(m, "Obijime", (0, -2.5, 0), (37.2, 1.4, 28.8), wraps(JADE, mix(JADE, (0, 0, 0), 0.4)))
    fan = sensu(m, (9, 2, 14.3), -12, KURO_DARK, GOLD)
    node = piece(m, "Belly", (0, 8, 0), [belly, obi, cord] + fan + [chest], offset=(0, 4, 0))
    hip = box(m, "Hanches", (0, -0.5, 0), (35.5, 13, 27.6), court_body())
    train_paint = fabric(COUR, motifs=[pat("kikko", GOLD, 0.22, R=6)], wave=(16, COUR_DARK, GOLD), tb=COUR_DARK)
    train = [plank(m, "Mo_Haut", (0, 2, -13.5), (0, -26, -22), 30, 1.2, train_paint),
             plank(m, "Mo_Bas", (0, -26, -22), (0, -38.5, -44), 33, 1.2, train_paint)]
    return m, [piece(m, "Pelvis", (0, 12, 0), [hip, right_thigh, mirror(m, right_thigh), node] + train)]


def build_court_hands():
    m = Model("Hands")
    cuff = box(m, "R-Manchette", (0, -3, 0), (12, 11, 16), court_body())
    rings = kasane_rings(m, "R-Poignet", (0, 2.6, 0), (12.6, 1.2, 16.6), 1.0, (0.3, 0.3))
    glove = box(m, "R-Gant", (0, 1.5, 0), (11, 9, 15), plain(IVORY))
    hand = piece(m, "R-Hand", (0, -8, 0), [glove], offset=(0, -5, 0))
    right = piece(m, "R-Forearm", (-18, 40, 0), [cuff] + rings + [hand])
    return m, [right, mirror(m, right)]


def build_court_legs():
    m = Model("Legs")
    red, red_light = (150, 24, 40), (196, 60, 74)
    hak = striped_hakama(red, mix(red, (0, 0, 0), 0.3), red_light, 12)
    upper = box(m, "R-Hakama_Haut", (-1, -1, 0), (17, 24, 22), hak)
    mid = box(m, "R-Hakama_Genou", (-2.1, 5, -0.5), (19, 13, 25.5), hak)
    low = box(m, "R-Hakama_Bas", (-3.7, -6.5, -1), (22, 11, 30), hak)
    trail = box(m, "R-Hakama_Long", (-4.1, -13.6, -1.6), (22.6, 5, 32), fabric(red, wave=(10, mix(red, (0, 0, 0), 0.4), GOLD), tb=red))
    calf = piece(m, "R-Calf", (0, -12, 3), [mid, low, trail], offset=(0, -12, -3))
    thigh = piece(m, "R-Thigh", (-8, -1, 1), [upper, calf], offset=(0, -9, 0))
    sock = box(m, "R-Tabi", (0, 1, 0), (15, 7, 21), white_tabi)
    sole = box(m, "R-Zori", (-0.6, -3, 0), (16, 1.5, 23), lacquer_c(KURO_DARK))
    foot = piece(m, "R-Foot", (-7.85, 7, -3), [sock, sole] + gk.thong(m, GOLD), offset=(0, -3, 6))
    waist = box(m, "Koshi", (0, 0.25, 0), (35, 12.5, 27), hak)
    himo = front_himo(m, 13.9, plain(IVORY), half=14.5)
    return m, [piece(m, "Pelvis", (0, 51, 0), [waist] + himo + [thigh, mirror(m, thigh)]), foot, mirror(m, foot)]


# ---------------------------------------------------------------------------------------------------------------------
# Haori de la Garde d'Honneur (nobles d'epee) : haori court au grand mon, cordon d'honneur, kote d'acier, hakama serre
# ---------------------------------------------------------------------------------------------------------------------
GARDE, GARDE_LIGHT, GARDE_DARK = (26, 34, 58), (66, 80, 120), (12, 16, 30)


def guard_body(mon=(), opening=False, hem=True):
    return fabric(GARDE, motifs=[pat("asanoha", GARDE_LIGHT, 0.2), gradient(0.1)], mon=mon,
                  opening=[(1.0, IVORY), (1.2, CRIMSON), (40, GARDE_DARK)] if opening else None,
                  hem=(3, GOLD) if hem else None)


def build_guard_head():
    m = Model("Head")
    band_paint = fabric(GARDE_DARK, motifs=[wrap_folds(KURO_DARK, GARDE_LIGHT, 6, 0.5)])
    kids = [box(m, "Hachimaki", (0, 5, 0), (31.6, 4.4, 29.8), band_paint),
            box(m, "Hachigane", (0, 5, 15.3), (12, 5.6, 1.2), lacquer_c(STEEL)),
            mon_quad(m, "Mon", (0, 5, 16.05), 4.6, STEEL_DARK),
            box(m, "Noeud", (0, 5, -16.2), (6, 5, 3), band_paint)]
    kids += sym(m, plank(m, "R-Queue", (-3.5, -14, -20.5), (-1.5, 3, -16.9), 3, 0.6, plain(GOLD)))
    return m, [piece(m, "Head", (0, 0, 0), kids)]


def build_guard_chest():
    m = Model("Chest")

    def arm():
        sode = box(m, "R-Sode", (-1, -1, -1.5), (14, 23, 19), guard_body(mon=[("back", 0.5, 0.3, 4, GOLDS)]))
        cuff = box(m, "R-Poignet", (-1, -11.5, -1.5), (14.4, 1.6, 19.4), plain(GOLD))
        return piece(m, "R-Arm", (-17, 0, 0), [sode, cuff])

    right_arm = arm()
    plate = box(m, "Torse", (0, 0, 0), (33, 24, 24),
                guard_body(mon=[("back", 0.5, 0.35, 10, GOLDS), ("front", 0.15, 0.25, 3.8, GOLDS), ("front", 0.85, 0.25, 3.8, GOLDS)],
                           opening=True, hem=False))
    col = stripes([(0.3, GARDE), (0.2, GOLD), (0.5, IVORY)])
    ties = sym(m, rod(m, "R-Himo", (-4.5, -3, 14.0), (-0.8, -6.5, 14.4), 1.2, plain(IVORY)))
    knot = [box(m, "Himo_Noeud", (0, -7, 14.5), (2.4, 2.4, 1.6), plain(IVORY)), box(m, "Himo_Pompon", (0, -10.2, 14.5), (2, 4, 1.1), plain(IVORY))]
    gold_cord = plain(GOLD)
    honor = [rod(m, "Cordon_Honneur_A", (-15.5, 11, 6), (-6, -1, 13.6), 0.9, gold_cord),
             rod(m, "Cordon_Honneur_B", (-15.5, 9, 9), (-8, -4, 13.6), 0.9, gold_cord),
             box(m, "Cordon_Honneur_Ferret", (-6.6, -2.2, 13.8), (1.4, 3.2, 1.4), metal(GOLD_LIGHT)),
             box(m, "Cordon_Honneur_Epaulette", (-15.8, 11.6, 3), (5, 1.4, 8), metal(GOLD))]
    chest = piece(m, "Chest", (0, 5, 0), [plate] + gk.lapels(m, col, col, 4.5) + gk.neck_band(m, plain(IVORY)) + ties + knot + honor
                  + [right_arm, mirror(m, right_arm)], offset=(0, 11, 0))
    belly = box(m, "Ventre", (0, 0, 0), (31, 16, 23), guard_body(hem=False))
    obi = box(m, "Obi", (0, -2, 0), (35, 5.5, 26.5), fabric(CRIMSON_DARK, motifs=[lambda img: [band(img, y, y + 1, GOLD) for y in (0, len(img) - 1)]]))
    node = piece(m, "Belly", (0, 8, 0), [belly, obi, chest], offset=(0, 4, 0))
    hip = box(m, "Hanches", (0, -0.7, 0), (34, 12, 25.5), guard_body())
    return m, [piece(m, "Pelvis", (0, 12, 0), [hip, node])]


def build_guard_hands():
    m = Model("Hands")
    splint = box(m, "R-Kote", (0, -2.5, 0), (11.6, 12, 15.6), steel_splints)
    lace = box(m, "R-Kote_Lacet", (0, 3.4, 0), (12.2, 1.2, 16.2), plain(CRIMSON))
    glove = box(m, "R-Gant", (0, 1.5, 0), (11, 7, 15), plain(LEATHER))
    plate = box(m, "R-Tekko", (0, 2.5, 7.4), (9, 4.5, 1.4), lacquer_c(STEEL, mon=("front", GOLDS)))
    hand = piece(m, "R-Hand", (0, -8, 0), [glove, plate], offset=(0, -5, 0))
    right = piece(m, "R-Forearm", (-18, 40, 0), [splint, lace, hand])
    return m, [right, mirror(m, right)]


def build_guard_legs():
    m = Model("Legs")
    hak = striped_hakama((40, 40, 48), GOLD_DARK, (86, 86, 98), 9)
    upper = box(m, "R-Hakama_Haut", (-0.5, -1, 0), (16, 24, 20), hak)
    knee = box(m, "R-Hakama_Genou", (-0.3, 7, 0), (14.5, 11, 17.5), hak)
    wrap = box(m, "R-Kyahan", (0, -3, 0), (12.5, 17, 15), fabric(GARDE_DARK, motifs=[wrap_folds(KURO_DARK, GARDE_LIGHT, 6, 0.0)]))
    lace = [rod(m, "R-Lacet_%d" % i, (-6.4, 3.5 - i * 6.5, 7.7), (6.4, 0.5 - i * 6.5, 7.7), 0.8, plain(GOLD)) for i in range(3)]
    calf = piece(m, "R-Calf", (0, -12, 3), [knee, wrap] + lace, offset=(0, -12, -3))
    thigh = piece(m, "R-Thigh", (-8, -1, 1), [upper, calf], offset=(0, -9, 0))
    sock = box(m, "R-Tabi", (0, 1, 0), (15, 7, 21), white_tabi)
    sole = box(m, "R-Waraji", (-0.6, -3.2, 0), (16, 1.5, 22), gk.straw_paint())
    ties = [rod(m, "R-Attache_Waraji", (-7.7, -1.8, -6), (7.4, -1.8, -6.5), 0.8, plain(STRAW_DARK))]
    foot = piece(m, "R-Foot", (-7.85, 7, -3), [sock, sole] + ties + gk.thong(m, CRIMSON), offset=(0, -3, 6))
    waist = box(m, "Koshi", (0, 0, 0), (29, 11, 23), hak)
    board = box(m, "Koshi_Ita", (0, 1.5, -12.6), (14, 8, 2), fabric((40, 40, 48), mon=[("back", 0.5, 0.5, 5, GOLDS)], motifs=[edge_piping(GOLD)]))
    return m, [piece(m, "Pelvis", (0, 51, 0), [waist, board, thigh, mirror(m, thigh)]), foot, mirror(m, foot)]


# ---------------------------------------------------------------------------------------------------------------------
# Clan Sama, pugilistes de l'Empire Luo (2026-11-10 : « rends-la plus pugiliste ») : dogi cramoisi sans manches, manches
# arrachees aux epaules (bras nus : la tenue cache aussi le haut de dessous du joueur), sarashi blanc sous le col, soleil
# du clan au dos, ceinture noire nouee et corde tressee cramoisi et or ; hachimaki au soleil a longues queues ; avant-bras
# bandes, poings bandes a plaques de fer cloutees ; pantalon tattsuke serre aux mollets, bandes de jambe lacees, pieds bandes.
# ---------------------------------------------------------------------------------------------------------------------
SAMA, SAMA_LIGHT, SAMA_DARK = (150, 26, 38), (198, 66, 66), (82, 12, 22)
IRON, IRON_LIGHT = (70, 72, 80), (150, 152, 162)


def sun_mon(img, cx, cy, r, ground=SAMA_DARK):
    """Soleil du Clan Sama : disque d'or a douze rayons, cercle d'or."""
    disc(img, cx, cy, r, GOLD_DARK)
    disc(img, cx, cy, r - 1, ground)
    for k in range(12):
        a = math.radians(k * 30 + 15)
        stroke(img, [(cx + math.cos(a) * r * 0.48, cy + math.sin(a) * r * 0.48),
                     (cx + math.cos(a) * r * 0.86, cy + math.sin(a) * r * 0.86)], GOLD if k % 2 else GOLD_LIGHT, max(0.6, r * 0.07))
    disc(img, cx, cy, r * 0.42, GOLD)
    disc(img, cx, cy, r * 0.3, GOLD_LIGHT)


def sun_motif(fx, fy, fr):
    def motif(img):
        h, w = len(img), len(img[0])
        sun_mon(img, w * fx, h * fy, max(3.0, min(w, h) * fr))
    return motif


def sarashi(x, y):
    """Bandes de poitrine sarashi (remplissage de l'ouverture du col)."""
    return IVORY_SHADE if y % 4 == 0 else (IVORY if y % 4 != 3 else (224, 214, 194))


def sama_gi(motifs=(), opening=False, light=1.0):
    """Toile de coton epais cramoisi, piquee de sashiko en croix, usee aux plis."""
    return fabric(SAMA, light, motifs=[pat("juji", SAMA_DARK, 0.22, s=6), wear(), gradient(0.14)] + list(motifs),
                  opening=[(1.6, KURO), (1.0, GOLD_DARK), (40, SENTINEL)] if opening else None, fill=sarashi)


def wear(c=SAMA_LIGHT, k=0.35):
    """Coton use : zones palies aux frottements (bruit), taches plus sombres."""
    def motif(img):
        for y, row in enumerate(img):
            for x, p in enumerate(row):
                n = noise(x * 0.12 + 3.1, y * 0.12 - 1.7)
                if n > 0.6:
                    put(img, x, y, c, (n - 0.6) * k * 2.5)
                elif n < 0.3:
                    put(img, x, y, SAMA_DARK, (0.3 - n) * k * 2)
    return motif


def torn_paint(base, depth=4):
    def paint(face, w, h, seed):
        img = base(face, w, h, seed)
        if face not in ("top", "bottom"):
            torn(img, seed, depth)
        return img
    return paint


def sama_band(face, w, h, seed):
    img = cloth(w, h, SAMA, seed, 0.08, 0.02)
    if face not in ("top", "bottom"):
        wrap_folds(SAMA_DARK, SAMA_LIGHT, 6, 0.6)(img)
        band(img, 0, 1, SAMA_DARK)
        band(img, h - 1, h, SAMA_DARK)
    return finish(img, face)


def iron_plate(studs=True):
    def paint(face, w, h, seed):
        img = canvas(w, h, IRON)
        for y in range(h):
            for x in range(w):
                k = 1 + 0.22 * math.sin(x * 0.7 + y * 1.1) + 0.1 * noise(x * 0.4 + seed, y * 0.4)
                img[y][x][:3] = [clamp(v * k) for v in IRON]
        if studs and face in ("front", "back"):
            for x in range(2, w - 1, max(3, w // 4)):
                put(img, x, h // 2, IRON_LIGHT)
                put(img, x + 1, h // 2, (30, 30, 34))
        border(img, 0, (34, 34, 40))
        return finish(img, face, edges=False)
    return paint


def rope_paint(c0, c1, period=6):
    def paint(face, w, h, seed):
        img = canvas(w, h, c0)
        rope_tex(img, c0, c1, period)
        return finish(img, face, edges=False)
    return paint


def build_sama_head():
    m = Model("Head")
    kids = [box(m, "Hachimaki", (0, 4.5, 0), (31.5, 4, 29.6), sama_band),
            box(m, "Plaque_Soleil", (0, 4.5, 15.25), (7, 5, 0.9), fabric(KURO, motifs=[sun_motif(0.5, 0.5, 0.45)])),
            box(m, "Noeud", (0, 4.5, -16), (6, 5, 3), sama_band)]
    kids += sym(m, plank(m, "R-Queue", (-8.5, -13, -27), (-1.8, 3, -16.8), 3, 0.6, sama_band))
    kids += sym(m, box(m, "R-Queue_Bout", (-8.7, -13.6, -27.2), (3.4, 2, 1.2), plain(GOLD), q=q_axis((0, 0, 1), -22)))
    return m, [piece(m, "Head", (0, 0, 0), kids)]


def build_sama_chest():
    m = Model("Chest")

    def arm():
        # manche arrachee : un moignon de toile dechiree a l'epaule, le reste du bras nu ; brassard de corde
        stub = box(m, "R-Manche", (0, 6.6, 0), (9.6, 7.8, 13.6), torn_paint(sama_gi(), 4), hidden=("bottom",))
        cord = box(m, "R-Brassard", (0, 0.6, 0), (8.8, 1.8, 12.8), rope_paint(CRIMSON, GOLD, 5))
        return piece(m, "R-Arm", (-17, 0, 0), [stub, cord])

    right_arm = arm()
    plate = box(m, "Torse", (0, 0, 0), (32, 24, 23), sama_gi(opening=True))
    back = box(m, "Dos_Soleil", (0, 1, -11.9), (22, 18, 0.6), fabric(SAMA, motifs=[pat("juji", SAMA_DARK, 0.22, s=6), wear(), sun_motif(0.5, 0.5, 0.42)]))
    col = recruit_collar
    chest = piece(m, "Chest", (0, 5, 0), [plate, back] + gk.lapels(m, col, col, 5) + gk.neck_band(m, col, 12.1)
                  + [right_arm, mirror(m, right_arm)], offset=(0, 11, 0))

    belly = box(m, "Ventre", (0, 0, 0), (30, 16, 22), sama_gi())
    obi = box(m, "Obi", (0, -2.5, 0), (34.5, 5, 26.5), fabric(KURO, motifs=[lambda img: [stitch_line(img, [(0, y), (len(img[0]), y)], (96, 92, 100), 2) for y in (1, len(img) - 2)]]))
    tsuna = box(m, "Tsuna", (0, 1.1, 0), (35.4, 2.4, 27.4), rope_paint(GOLD, SAMA_DARK, 6))
    knot = box(m, "Noeud", (0, -2.5, 13.9), (6, 4.4, 2.6), plain(KURO))
    tails = [rod(m, "Bout_A", (-1.6, -4.4, 14.6), (-4.5, -14, 15.0), 2.4, plain(KURO)),
             rod(m, "Bout_B", (1.6, -4.4, 14.6), (4.2, -13.5, 14.8), 2.4, plain(KURO))]
    tsuna_knot = box(m, "Tsuna_Noeud", (-9, 1.1, 14.2), (4, 3.2, 2.4), rope_paint(GOLD, SAMA_DARK, 4))
    tassel = rod(m, "Tsuna_Pompon", (-9, -0.5, 14.6), (-9.6, -7.5, 15.0), 1.6, plain(GOLD))
    node = piece(m, "Belly", (0, 8, 0), [belly, obi, tsuna, knot, tsuna_knot, tassel] + tails + [chest], offset=(0, 4, 0))
    hip = box(m, "Hanches", (0, -0.5, 0), (31, 12, 23.6), sama_gi())
    return m, [piece(m, "Pelvis", (0, 12, 0), [hip, node])]


def build_sama_hands():
    m = Model("Hands")
    wrap = box(m, "R-Bandes", (0, -1, 0), (9.2, 15, 13.2), wraps(edge=SAMA))
    spiral = [rod(m, "R-Cordon_%d" % i, (-4.7, 4.5 - i * 4.5, 6.7), (4.7, 2.5 - i * 4.5, 6.7), 0.7, plain(CRIMSON)) for i in range(3)]
    cuff = box(m, "R-Bracelet", (0, -7.4, 0), (10.4, 2.2, 14.4), iron_plate(False))
    glove = box(m, "R-Bandes_Main", (0, 1.5, 0), (11, 8, 15), wraps())
    knuckle = box(m, "R-Tekko", (0, 3.4, 7.8), (11.6, 3.8, 1.4), iron_plate())
    studs = [box(m, "R-Clou_%d" % i, (-3.6 + i * 3.6, 3.4, 8.85), (1.4, 1.4, 0.7), metal(IRON_LIGHT)) for i in range(3)]
    back_plate = box(m, "R-Plaque_Main", (-5.9, 1.5, 0), (1.2, 6, 10), iron_plate(False))
    hand = piece(m, "R-Hand", (0, -8, 0), [glove, knuckle, back_plate] + studs, offset=(0, -5, 0))
    right = piece(m, "R-Forearm", (-18, 40, 0), [wrap, cuff, hand] + spiral)
    return m, [right, mirror(m, right)]


def build_sama_legs():
    m = Model("Legs")
    pants = fabric(KURO, motifs=[pat("juji", KURO_LIGHT, 0.14, s=6), lambda img: rect(img, 0, 0, 2, len(img), SAMA), gradient(0.12)])
    upper = box(m, "R-Tattsuke_Haut", (-0.5, -1, 0), (15.6, 24, 18.6), pants)
    knee = box(m, "R-Tattsuke_Genou", (-0.3, 7, 0), (13.6, 10, 16.4), pants)
    wrap = box(m, "R-Kyahan", (0, -4, 0), (12.4, 15, 14.4), wraps(edge=SAMA))
    lace = [rod(m, "R-Lacet_%d" % i, (-6.3, 2.5 - i * 5, 7.3), (6.3, -0.5 - i * 5, 7.3), 0.8, plain(CRIMSON)) for i in range(3)]
    calf = piece(m, "R-Calf", (0, -12, 3), [knee, wrap] + lace, offset=(0, -12, -3))
    thigh = piece(m, "R-Thigh", (-8, -1, 1), [upper, calf], offset=(0, -9, 0))
    sock = box(m, "R-Pied_Bande", (0, 1, 0), (15, 7, 21), wraps())
    sole = box(m, "R-Ashinaka", (-0.6, -3.2, 2.5), (16, 1.5, 15), gk.straw_paint())
    ties = [rod(m, "R-Attache", (-7.7, -1.8, -3), (7.4, -1.8, -3.5), 0.8, plain(STRAW_DARK))]
    foot = piece(m, "R-Foot", (-7.85, 7, -3), [sock, sole] + ties + gk.thong(m, CRIMSON), offset=(0, -3, 6))
    waist = box(m, "Koshi", (0, 0.25, 0), (32.2, 12.5, 24.4), pants)
    return m, [piece(m, "Pelvis", (0, 51, 0), [waist, thigh, mirror(m, thigh)]), foot, mirror(m, foot)]


# ---------------------------------------------------------------------------------------------------------------------
# Kariginu du Lettre (nobles erudits) : col rond, manches a cordons kukuri, pans avant et arriere, sashinuki, haut eboshi
# ---------------------------------------------------------------------------------------------------------------------
ASAGI, ASAGI_LIGHT, ASAGI_DARK = (104, 158, 160), (176, 214, 210), (48, 92, 96)
SASHINUKI, SASHINUKI_LIGHT = (74, 46, 98), (128, 96, 158)


def scholar_body(mon=(), wave=False, lining=None):
    return fabric(ASAGI, motifs=[pat("shippo", ASAGI_LIGHT, 0.28), gradient(0.1)], mon=mon,
                  wave=(10, ASAGI_DARK, IVORY) if wave else None, lining=lining)


def sashinuki_paint():
    return fabric(SASHINUKI, motifs=[pat("kikko", SASHINUKI_LIGHT, 0.28, R=5), gradient(0.15)])


def build_scholar_head():
    m = Model("Head")
    kids = eboshi(m, (11, 30, 19), (SILVER_DARK, SILVER, SILVER_LIGHT), tilt=-8)
    kids += gk.cords_head(m, plain(SASHINUKI))
    return m, [piece(m, "Head", (0, 0, 0), kids)]


def build_scholar_chest():
    m = Model("Chest")
    kukuri = plain(SASHINUKI_LIGHT)

    def arm():
        tamoto = box(m, "R-Tamoto", (-1.5, -2, -7.2), (16.5, 22, 35), scholar_body(wave=True, lining=SASHINUKI))
        cords = [rod(m, "R-Kukuri_%d" % i, (-10.1, 8 - i * 7, -24.5 + i * 2), (-10.1, 1 - i * 7, -22 + i * 2), 0.7, kukuri) for i in range(3)]
        tassel = box(m, "R-Kukuri_Pompon", (-10.2, -14, 7), (1.6, 4, 1.6), metal(SASHINUKI_LIGHT))
        juban = box(m, "R-Juban", (-1, -12.5, 0), (14, 3, 16), plain(IVORY))
        forearm = piece(m, "R-Forearm", (-1, 0, 0), [tamoto, juban, tassel] + cords)
        sode = box(m, "R-Sode", (-1.5, 0.5, -2.5), (15.5, 23, 24), scholar_body(mon=[("back", 0.5, 0.3, 4, (SILVER_DARK, SILVER, SILVER_LIGHT))]))
        return piece(m, "R-Arm", (-17, 0, 0), [sode, forearm])

    right_arm = arm()
    plate = box(m, "Torse", (0, 0, 0), (33.5, 24, 24.5), scholar_body(mon=[("back", 0.5, 0.3, 8, (SILVER_DARK, SILVER, SILVER_LIGHT))]))
    agekubi = [box(m, "Agekubi", (0, 12.6, 0), (22, 3.2, 21), fabric(ASAGI_DARK, motifs=[edge_piping(IVORY)])),
               box(m, "Agekubi_Bouton", (-9.6, 13.2, 9.4), (2.4, 2.4, 2.4), metal(SASHINUKI_LIGHT)),
               rod(m, "Agekubi_Boucle", (-9.6, 13.2, 9.4), (-12, 11.5, 8), 0.7, kukuri)]
    chest = piece(m, "Chest", (0, 5, 0), [plate] + agekubi + [right_arm, mirror(m, right_arm)], offset=(0, 11, 0))
    belly = box(m, "Ventre", (0, 0, 0), (31.5, 16, 23.5), scholar_body())
    ate = box(m, "Ate_Obi", (0, -2.5, 0), (35, 3.4, 26.5), fabric(ASAGI_DARK, motifs=[edge_piping(IVORY)]))
    scroll = [rod(m, "Makimono", (5, -1.6, 14.6), (15, 1.6, 14.6), 2.6, fabric(IVORY, motifs=[lambda img: band(img, 0, 2, CRIMSON)])),
              box(m, "Makimono_Embout_A", (4.6, -1.8, 14.6), (1.4, 3.2, 3.2), metal(GOLD)),
              box(m, "Makimono_Embout_B", (15.4, 1.8, 14.6), (1.4, 3.2, 3.2), metal(GOLD))]
    shaku = [box(m, "Shaku", (-9, 2, 14.6), (2.6, 13, 0.8), plain((150, 110, 66)), q=q_axis((0, 0, 1), 8))]
    node = piece(m, "Belly", (0, 8, 0), [belly, ate] + scroll + shaku + [chest], offset=(0, 4, 0))
    hip = box(m, "Hanches", (0, -0.5, 0), (34, 13, 25.5), scholar_body())
    front = box(m, "Mae_Mi", (0, -12, 13.6), (22, 18, 1.2), scholar_body(wave=True), double=True)
    back = box(m, "Ushiro_Mi", (0, -15, -13.6), (26, 24, 1.2), scholar_body(wave=True), double=True)
    return m, [piece(m, "Pelvis", (0, 12, 0), [hip, front, back, node])]


def build_scholar_hands():
    m = Model("Hands")
    cuff = box(m, "R-Manchette", (0, -3, 0), (12, 11, 16), scholar_body())
    ring = box(m, "R-Liseré", (0, 2.2, 0), (12.4, 1.2, 16.4), plain(IVORY))
    glove = box(m, "R-Gant", (0, 1.5, 0), (11, 9, 15), plain(IVORY))
    hand = piece(m, "R-Hand", (0, -8, 0), [glove], offset=(0, -5, 0))
    right = piece(m, "R-Forearm", (-18, 40, 0), [cuff, ring, hand])
    return m, [right, mirror(m, right)]


def build_scholar_legs():
    m = Model("Legs")
    sn = sashinuki_paint()
    upper = box(m, "R-Sashinuki_Haut", (-1, -1, 0), (17.5, 24, 21.5), sn)
    puff = box(m, "R-Sashinuki_Bouffant", (-1.4, 2, 0), (18.5, 18, 22.5), sn)
    tie = box(m, "R-Sashinuki_Lien", (-0.6, -6.6, 0), (14.6, 1.4, 16.6), plain(IVORY))
    calf = piece(m, "R-Calf", (0, -12, 3), [puff, tie], offset=(0, -12, -3))
    thigh = piece(m, "R-Thigh", (-8, -1, 1), [upper, calf], offset=(0, -9, 0))
    sock = box(m, "R-Tabi", (0, 1, 0), (15, 7, 21), white_tabi)
    shoe = box(m, "R-Asagutsu", (-0.6, -1.4, 0.4), (16, 5, 22.5), lacquer_c(KURO_DARK))
    foot = piece(m, "R-Foot", (-7.85, 7, -3), [sock, shoe], offset=(0, -3, 6))
    waist = box(m, "Koshi", (0, 0, 0), (30, 11.5, 24), sn)
    himo = front_himo(m, 12.4, plain(IVORY), half=14)
    return m, [piece(m, "Pelvis", (0, 51, 0), [waist] + himo + [thigh, mirror(m, thigh)]), foot, mirror(m, foot)]


# ---------------------------------------------------------------------------------------------------------------------
# Ronin Vagabond (aventuriers) : haori use et rapiece au mon arrache, hakama effiloche, hachimaki, gourde, baluchon
# ---------------------------------------------------------------------------------------------------------------------
RONIN, RONIN_LIGHT, RONIN_DARK = (98, 84, 70), (140, 124, 104), (56, 46, 38)
RONIN_HAKAMA = (54, 62, 86)


def ronin_cloth(ground, seed_patches=1, tear=1, mon_scar=False):
    def paint(face, w, h, seed):
        if face in ("top", "bottom"):
            return finish(cloth(w, h, mix(ground, (0, 0, 0), 0.25), seed, 0.1, 0.06), face)
        img = cloth(w, h, ground, seed, 0.2, 0.08)
        rnd = random.Random(seed)
        for y in range(h):                                                       # usure, taches
            for x in range(w):
                s = noise(x * 0.09 + seed, y * 0.09 - seed)
                if s > 0.62:
                    for i in range(3):
                        img[y][x][i] *= 0.82
        for _ in range(seed_patches if face in ("back", "left", "front") else 0):
            pw, ph = rnd.randint(int(w * 0.2), int(w * 0.4)) + 2, rnd.randint(int(h * 0.15), int(h * 0.3)) + 2
            gk.patch(img, rnd.randint(1, max(1, w - pw - 2)), rnd.randint(2, max(2, h - ph - 2)), pw, ph, rnd.choice(gk.PATCHES))
        for _ in range(tear):
            x, y = rnd.randint(2, w - 3), rnd.randint(2, h - 6)
            stroke(img, [(x, y), (x + rnd.randint(-2, 2), y + rnd.randint(3, 7))], LINING, 0.6)
        if mon_scar and face == "back":                                          # le mon du maitre, arrache : tissu plus clair, fils
            cx, cy, r = w / 2.0, h * 0.3, min(w, h) * 0.17
            for y in range(h):
                for x in range(w):
                    d = math.hypot(x + 0.5 - cx, y + 0.5 - cy)
                    if d < r:
                        put(img, x, y, RONIN_LIGHT, 0.55)
                    elif d < r + 1.2 and (x + y) % 3 == 0:
                        put(img, x, y, IVORY_SHADE, 0.8)
        return finish(img, face)
    return paint


def build_ronin_head():
    m = Model("Head")
    kids = [box(m, "Hachimaki", (0, 4.5, 0), (31.5, 4, 29.6), hachimaki),
            box(m, "Noeud", (0, 4.5, -16), (6, 5, 3), hachimaki)]
    kids += sym(m, plank(m, "R-Queue", (-3.5, -15, -21), (-1.5, 3, -16.8), 3, 0.6, hachimaki))
    return m, [piece(m, "Head", (0, 0, 0), kids)]


def build_ronin_chest():
    m = Model("Chest")

    def arm():
        sode = box(m, "R-Sode", (-1, -1, -1.5), (13.5, 23, 18), ronin_cloth(RONIN, 1, 1))
        cuff = box(m, "R-Poignet", (-1, -11.3, -1.5), (13.9, 2, 18.4), fringe(ronin_cloth(RONIN_DARK, 0, 0), 2))
        return piece(m, "R-Arm", (-17, 0, 0), [sode, cuff])

    def thigh():
        suso = box(m, "R-Suso", (-0.6, 2, 0), (16.6, 14, 22.5), fringe(ronin_cloth(RONIN, 0, 1), 6), double=True)
        return piece(m, "R-Thigh", (-8, -1, 1), [suso], offset=(0, -9, 0))

    right_arm, right_thigh = arm(), thigh()
    plate = box(m, "Torse", (0, 0, 0), (33, 24, 24), ronin_cloth(RONIN, 1, 1, mon_scar=True))
    col = stripes([(0.4, RONIN_DARK), (0.6, (176, 166, 140))])
    bundle_paint = fabric((60, 90, 120), motifs=[pat("shippo", (150, 180, 200), 0.35)])
    bundle = [box(m, "Furoshiki", (2, 3, -16.5), (14, 10, 7), bundle_paint),                  # baluchon sur le dos
              box(m, "Furoshiki_Noeud", (2, 8.6, -16.5), (4, 2.4, 3), bundle_paint),
              rod(m, "Furoshiki_Sangle_A", (-12, 12, 10), (2, 8, -15), 1.1, plain(STRAW_DARK)),
              rod(m, "Furoshiki_Sangle_B", (2, 8, -15), (12, -4, 12), 1.1, plain(STRAW_DARK))]
    chest = piece(m, "Chest", (0, 5, 0), [plate] + gk.lapels(m, col, col, 4) + gk.neck_band(m, plain((176, 166, 140))) + bundle
                  + [right_arm, mirror(m, right_arm)], offset=(0, 11, 0))
    belly = box(m, "Ventre", (0, 0, 0), (31, 16, 23), ronin_cloth(RONIN, 0, 0))
    obi = box(m, "Obi", (0, -2, 0), (35, 5, 26.5), fabric(RONIN_DARK, motifs=[wrap_folds((30, 24, 20), RONIN_LIGHT, 5, 0.2)]))
    gourd = [box(m, "Gourde_Panse", (-12, -11, 15.2), (6, 7, 6), gk.gourd_paint),
             box(m, "Gourde_Col", (-12, -5.7, 15.2), (4, 4, 4), gk.gourd_paint),
             box(m, "Gourde_Bouchon", (-12, -2.8, 15.2), (2.5, 2, 2.5), plain(CRIMSON_DARK)),
             rod(m, "Gourde_Cordon", (-12, -3.2, 15.2), (-10, -1.5, 14.2), 0.7, plain(STRAW_DARK))]
    node = piece(m, "Belly", (0, 8, 0), [belly, obi] + gourd + [chest], offset=(0, 4, 0))
    hip = box(m, "Hanches", (0, -0.5, 0), (34.2, 13, 25.5), ronin_cloth(RONIN, 1, 0))
    return m, [piece(m, "Pelvis", (0, 12, 0), [hip, right_thigh, mirror(m, right_thigh), node])]


def build_ronin_hands():
    m = Model("Hands")
    wrap = box(m, "R-Bandes", (0, -2, 0), (10, 12, 14), wraps((196, 186, 160), (140, 128, 104), edge=RONIN_DARK))
    glove = box(m, "R-Bandes_Main", (0, 1.5, 0), (11, 7, 15), wraps((196, 186, 160), (140, 128, 104)))
    loose = rod(m, "R-Bout", (-5, -1, 7), (-6.5, -7, 8.5), 1.2, wraps((196, 186, 160), (140, 128, 104)))
    hand = piece(m, "R-Hand", (0, -8, 0), [glove, loose], offset=(0, -5, 0))
    right = piece(m, "R-Forearm", (-18, 40, 0), [wrap, hand])
    return m, [right, mirror(m, right)]


def build_ronin_legs():
    m = Model("Legs")
    hak_base = striped_hakama(RONIN_HAKAMA, mix(RONIN_HAKAMA, (0, 0, 0), 0.35), mix(RONIN_HAKAMA, (255, 255, 255), 0.2), 10)
    upper = box(m, "R-Hakama_Haut", (-0.5, -1, 0), (16, 24, 20), hak_base)
    mid = box(m, "R-Hakama_Genou", (-1.3, 5, -0.5), (17.5, 13, 23.5), hak_base)
    low = box(m, "R-Hakama_Bas", (-2.6, -5, -1), (20, 13, 28), fringe(hak_base, 4), double=True)
    calf = piece(m, "R-Calf", (0, -12, 3), [mid, low], offset=(0, -12, -3))
    thigh = piece(m, "R-Thigh", (-8, -1, 1), [upper, calf], offset=(0, -9, 0))
    sock = box(m, "R-Tabi", (0, 1, 0), (15, 7, 21), gk.rag)
    sole = box(m, "R-Waraji", (-0.6, -3.2, 0), (16, 1.5, 22), gk.straw_paint(STRAW, STRAW_DARK, 0.85))
    foot = piece(m, "R-Foot", (-7.85, 7, -3), [sock, sole] + gk.thong(m, STRAW_DARK), offset=(0, -3, 6))
    waist = box(m, "Koshi", (0, 0.25, 0), (33.5, 12.5, 26), hak_base)
    himo = front_himo(m, 13.4, plain(RONIN_DARK))
    return m, [piece(m, "Pelvis", (0, 51, 0), [waist] + himo + [thigh, mirror(m, thigh)]), foot, mirror(m, foot)]


# ---------------------------------------------------------------------------------------------------------------------
# Kimono des Rizieres (villageois) : sugegasa, noragi indigo pique de sashiko aux manches retroussees, tablier, momohiki
# ---------------------------------------------------------------------------------------------------------------------
NORAGI, NORAGI_LIGHT = (40, 58, 96), (110, 132, 176)


def noragi(mon=(), opening=False):
    return fabric(NORAGI, motifs=[pat("juji", NORAGI_LIGHT, 0.55), gradient(0.1)], mon=mon,
                  opening=[(1.2, IVORY_SHADE), (40, (30, 40, 70))] if opening else None)


def field_apron(face, w, h, seed):
    img = cloth(w, h, (120, 150, 176), seed, 0.1, 0.04)
    if face in ("front", "back"):
        for x in range(w):
            if x % 6 in (0, 1):
                rect(img, x, 0, x + 1, h, (70, 96, 130), 0.7)
        band(img, 0, 2, NORAGI)
        torn(img, seed, 2)
    return finish(img, face)


def build_field_head():
    m = Model("Head")
    panel = kasa_panel(STRAW, STRAW_DARK, STRAW_DARK, CHA_DARK)
    kids = gk.cone_panels(m, 16, 34.0, 9.0, 21.0, panel)
    kids.append(box(m, "Couronne", (0, 12.5, 0), (24, 2, 24), plain(STRAW_DARK)))
    kids += gk.cords_head(m, plain(IVORY_SHADE))
    return m, [piece(m, "Head", (0, 0, 0), kids)]


def build_field_chest():
    m = Model("Chest")

    def arm():
        sode = box(m, "R-Sode", (-1, 1.5, -1), (12.8, 18, 15.6), noragi())
        roll = box(m, "R-Retrousse", (-1, -8.2, -1), (13.6, 3.4, 16.4), fabric(NORAGI_LIGHT, motifs=[wrap_folds(NORAGI, IVORY, 3, 0.0)]))
        return piece(m, "R-Arm", (-17, 0, 0), [sode, roll])

    right_arm = arm()
    plate = box(m, "Torse", (0, 0, 0), (32, 24, 23), noragi(mon=[("back", 0.5, 0.25, 6, (IVORY_SHADE, IVORY, (255, 255, 255)))], opening=True))
    col = stripes([(0.5, NORAGI), (0.5, IVORY_SHADE)])
    tasuki = plain((150, 60, 50))
    cross = [rod(m, "Tasuki_A", (-13, 11, -12.4), (13, -11, -12.4), 1.2, tasuki), rod(m, "Tasuki_B", (13, 11, -12.4), (-13, -11, -12.4), 1.2, tasuki)]
    chest = piece(m, "Chest", (0, 5, 0), [plate] + gk.lapels(m, col, col, 3.5) + gk.neck_band(m, plain(IVORY_SHADE), 12.1) + cross
                  + [right_arm, mirror(m, right_arm)], offset=(0, 11, 0))
    belly = box(m, "Ventre", (0, 0, 0), (30, 16, 22), noragi())
    obi = box(m, "Obi", (0, -2.5, 0), (34.5, 3.5, 26.5), gk.rope)
    apron_box = box(m, "Maekake", (0, -13, 13.7), (22, 20, 1), field_apron, double=True)
    towel = [box(m, "Tenugui", (13, -6, 13.9), (4, 9, 1.2), fabric(IVORY, motifs=[pat("asanoha", (120, 150, 176), 0.5)]))]
    node = piece(m, "Belly", (0, 8, 0), [belly, obi, apron_box] + towel + [chest], offset=(0, 4, 0))
    hip = box(m, "Hanches", (0, -0.7, 0), (31, 12, 23.6), noragi())
    return m, [piece(m, "Pelvis", (0, 12, 0), [hip, node])]


def build_field_hands():
    m = Model("Hands")
    cover = box(m, "R-Tekko", (0, -2.5, 0), (10.5, 12, 14.5), fabric(NORAGI, motifs=[pat("juji", NORAGI_LIGHT, 0.5), edge_piping(IVORY_SHADE, 0)]))
    loop = box(m, "R-Tekko_Boucle", (0, -7.6, 0), (11.6, 1.2, 15.6), plain(IVORY_SHADE))
    hand = piece(m, "R-Hand", (0, -8, 0), [], offset=(0, -5, 0))
    right = piece(m, "R-Forearm", (-18, 40, 0), [cover, loop, hand])
    return m, [right, mirror(m, right)]


def build_field_legs():
    m = Model("Legs")
    pants = fabric(NORAGI, motifs=[pat("juji", NORAGI_LIGHT, 0.45)])
    upper = box(m, "R-Momohiki", (-0.3, 0, 0), (13.5, 23, 16.5), pants)
    roll = box(m, "R-Retrousse", (-0.2, 6, 0), (14.2, 3.4, 17), fabric(NORAGI_LIGHT, motifs=[wrap_folds(NORAGI, IVORY, 3, 0.0)]))
    wrap = box(m, "R-Kyahan", (0, -3, 0), (12, 15, 14.5), fabric(IVORY_SHADE, motifs=[wrap_folds((150, 140, 120), IVORY, 6, 0.0)]))
    calf = piece(m, "R-Calf", (0, -12, 3), [roll, wrap], offset=(0, -12, -3))
    thigh = piece(m, "R-Thigh", (-8, -1, 1), [upper, calf], offset=(0, -9, 0))
    sock = box(m, "R-Pied", (0, 1, 0), (15, 7, 21), plain((196, 160, 128)))
    sole = box(m, "R-Waraji", (-0.6, -3.2, 0), (16, 1.5, 22), gk.straw_paint())
    ties = [rod(m, "R-Attache_Waraji", (-7.7, -1.8, -6), (7.4, -1.8, -6.5), 0.8, plain(STRAW_DARK))]
    foot = piece(m, "R-Foot", (-7.85, 7, -3), [sock, sole] + ties + gk.thong(m, STRAW_DARK), offset=(0, -3, 6))
    waist = box(m, "Koshi", (0, 0, 0), (29, 11, 22), pants)
    return m, [piece(m, "Pelvis", (0, 51, 0), [waist, thigh, mirror(m, thigh)]), foot, mirror(m, foot)]


# ---------------------------------------------------------------------------------------------------------------------
# Setsuka (2026-11-10) : le kimono Sakura en soie blanche nacree, aux motifs de lune (croissants, nuages, etoiles) a la place
# des cerisiers. Meme coupe : les fonctions de gen_kimono sont reprises, couleurs et motifs remplaces le temps de l'ecriture.
# ---------------------------------------------------------------------------------------------------------------------
SETSUKA_COLORS = dict(INDIGO=(226, 230, 240), PLUM=(168, 182, 214), SAKURA=(214, 224, 244), SAKURA_DEEP=(118, 138, 196),
                      BARK=(96, 108, 140), CRIMSON=(34, 46, 92), CRIMSON_DARK=(18, 24, 56), GOLD=(206, 212, 226),
                      GOLD_LIGHT=(250, 252, 255), GOLD_DARK=(128, 136, 160), SLATE=(64, 70, 92), SLATE_LIGHT=(116, 124, 150))


def moon(img, cx, cy, r, rot, pale=None, deep=None, heart=None):
    """Croissant de lune : disque argente evide par un disque decale (remplace la fleur de cerisier)."""
    pale = pale or gk.SAKURA
    deep = deep or gk.SAKURA_DEEP
    ox, oy = cx + math.cos(rot) * r * 0.5, cy + math.sin(rot) * r * 0.5
    ir = int(r) + 2
    for dy in range(-ir, ir + 1):
        for dx in range(-ir, ir + 1):
            d = math.hypot(dx, dy)
            if d > r or math.hypot(cx + dx - ox, cy + dy - oy) < r * 0.82:
                continue
            put(img, cx + dx, cy + dy, mix(pale, deep, min(1.0, d / max(0.1, r)) ** 2 * 0.6))


def moon_sprig(img, x, y, angle, length, rnd, thick=1.0):
    """Lune et nuages (remplace la branche de cerisier) : un croissant et deux traits de nuage effiles."""
    mx, my = x + math.cos(angle) * length * 0.6, y + math.sin(angle) * length * 0.6
    r = max(3.5, min(9.0, length * 0.2))
    moon(img, mx, my, r, rnd.uniform(-0.6, 0.6) + math.pi * 0.25)
    for k in (-1, 1):
        cy = my + k * r * 0.9 + rnd.uniform(-1, 1)
        stroke(img, [(mx - r * 2.2, cy), (mx - r * 0.6, cy - 1), (mx + r * 1.8, cy + 0.5)], gk.SAKURA_DEEP, 0.8)


def stars(img, seed, top, bottom):
    """Poussiere d'etoiles argentees (remplace les petales)."""
    rnd = random.Random(seed)
    h, w = len(img), len(img[0])
    for _ in range(int(w * h / 260)):
        x, y = rnd.uniform(0, w), rnd.uniform(0, h)
        if rnd.random() < top + (bottom - top) * y / h:
            put(img, x, y, gk.GOLD_LIGHT)
            if rnd.random() < 0.25:
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    put(img, x + dx, y + dy, gk.SAKURA, 0.6)


class setsuka_palette:
    """Remplace couleurs et motifs du Sakura dans gen_kimono le temps d'ecrire la tenue Setsuka."""
    def __enter__(self):
        self.saved = {k: getattr(gk, k) for k in list(SETSUKA_COLORS) + ["flower", "sprig", "petals"]}
        for k, v in SETSUKA_COLORS.items():
            setattr(gk, k, v)
        gk.flower, gk.sprig, gk.petals = moon, moon_sprig, stars

    def __exit__(self, *exc):
        for k, v in self.saved.items():
            setattr(gk, k, v)


# ---------------------------------------------------------------------------------------------------------------------
# Garde Royale de l'Empereur, version imperiale (retour du 2026-11-10 nuit : « meme principe que Sakura, chapeau et kimono,
# mais imperial ») : la forme du Sakura (gen_kimono : grand jingasa en cone laque, kimono a longues manches tombantes, obi et
# noeud dans le dos, hakama plisse) en laque noire, or et cramoisi ; chrysanthemes d'or imperiales sur le chapeau et les
# manches, mon de l'Empire au dos, epaulieres de laque bordees d'or (c'est une garde), brocart d'or a l'obi.
# ---------------------------------------------------------------------------------------------------------------------
IMP, IMP_LIGHT, IMP_DARK = (20, 18, 26), (70, 62, 76), (8, 8, 12)
IMP_SHEEN = (92, 84, 110)
# Retour du 2026-11-10 (« plus de details, ameliore la texture ») : soie lustree au tatewaku d'or (doubles lignes ondulees de
# la cour imperiale) semee de chrysanthemes, plaques lamellaires laquees lacees de cramoisi (epaulieres, kote, tassettes
# kusazuri sur les pans), hachigane imperial et voile de nuque cramoisi, hakama aux filets d'or et chrysanthemes d'ourlet.


def imperial_kiku_motif(fx, fy, fr):
    def motif(img):
        h, w = len(img), len(img[0])
        r = max(2.5, min(w, h) * fr)
        gk.kiku(img, w * fx, h * fy, r)
    return motif


def silk_sheen(k=0.22, c=IMP_SHEEN):
    """Lustre de la soie : reflet doux en diagonale."""
    def motif(img):
        h, w = len(img), len(img[0])
        for y, row in enumerate(img):
            for x, p in enumerate(row):
                s = max(0.0, math.sin((x * 0.55 + y * 0.35) * math.pi / max(12.0, w * 0.5))) ** 4
                if s > 0.02:
                    put(img, x, y, c, s * k)
    return motif


def tatewaku(line=GOLD_DARK, period=20, wave=24, amp=2.6, a=0.8, flowers=True):
    """Tatewaku : doubles lignes ondulees verticales qui s'ecartent et se resserrent ; chrysanthemes dans les renflements."""
    def motif(img):
        h, w = len(img), len(img[0])
        for x0 in range(period // 2, w + period, period):
            for y in range(h):
                off = 2.0 + amp * (0.5 + 0.5 * math.sin(y * 2 * math.pi / wave))
                put(img, x0 - off, y, line, a)
                put(img, x0 + off, y, line, a)
                put(img, x0 - off + 1, y, GOLD, a * 0.35)
            if flowers:
                for yy in range(wave // 4, h + wave, wave):
                    gk.kiku(img, x0 + 0.5, yy, 2.2, GOLD_LIGHT, GOLD_DARK, CRIMSON)
    return motif


def royal_body(mon=(), opening=False, wave=False, kiku=None, lining=None, flowers=True):
    motifs = [silk_sheen(), tatewaku(flowers=flowers), gradient(0.12)]
    if kiku:
        motifs.append(imperial_kiku_motif(*kiku))
    return fabric(IMP, motifs=motifs, mon=mon,
                  opening=[(1.0, IVORY), (1.4, GOLD), (1.2, CRIMSON), (40, IMP_DARK)] if opening else None,
                  wave=(10, CRIMSON_DARK, GOLD) if wave else None, lining=lining)


def imperial_lamellar(mon_face=None, kiku_face=None, rows=6):
    """Lamelles de laque noire en rangees (reflet en haut de chaque lame), lacage cramoisi, filets d'or en haut et en bas ;
    mon de l'Empire (mon_face) ou chrysanthemes d'or (kiku_face) sur une face."""
    def paint(face, w, h, seed):
        img = canvas(w, h, IMP)
        for y in range(h):
            t = (y % rows) / float(rows - 1)
            for x in range(w):
                c = mix(IMP_SHEEN, IMP_DARK, min(1.0, t * 1.1))
                s = max(0.0, math.sin((x + y * 0.6) * 5.0 / max(w, 8))) ** 6
                img[y][x][:3] = list(mix(c, (200, 196, 214), s * 0.25))
        if face not in ("top", "bottom"):
            for y in range(rows - 1, h, rows):
                for x in range(w):
                    put(img, x, y, IMP_DARK, 0.9)
            for y in range(2, h - 1, rows):                                 # lacage odoshi : paires de points cramoisis
                for x in range(1 + (y // rows) % 2, w - 1, 4):
                    put(img, x, y, CRIMSON)
                    put(img, x, y + 1, CRIMSON_DARK)
            band(img, 0, 1, GOLD_DARK)
            band(img, h - 2, h, GOLD)
            if mon_face and face == mon_face:
                kamon(img, w / 2.0, h / 2.0, min(w, h) / 2.0 - 2, IMP, GOLDS)
            if kiku_face and face == kiku_face:
                gk.kiku(img, w / 2.0, h / 2.0, min(w, h) / 2.0 - 2.5)
        return finish(img, face, edges=False)
    return paint


def royal_pleats(hem=False, flowers=False):
    """Hakama de soie noire plisse : filet d'or sur chaque arete, chrysanthemes au-dessus de l'ourlet."""
    base = gk.pleats(IMP, IMP_LIGHT, 10, hem_c=GOLD if hem else None)

    def paint(face, w, h, seed):
        img = base(face, w, h, seed)
        if face not in ("top", "bottom"):
            for y in range(h):
                for x in range(8, w, 10):
                    put(img, x, y, GOLD_DARK, 0.55)
            if flowers:
                for x in range(5, w, 10):
                    gk.kiku(img, x, h - 11, 2.4, GOLD_LIGHT, GOLD_DARK, CRIMSON)
        return img
    return paint


def royal_kasa_panel(k, W, H, light):
    img = canvas(W, H, IMP)
    for y in range(H):
        t = y / float(H)
        sh = max(0.0, math.sin(t * math.pi * 1.1)) ** 3
        img[y] = [[*mix(IMP_DARK, (60, 56, 74), sh * 0.6 * light), 255] for _ in range(W)]
    for x in range(W):                                              # cotes de laque (aretes du pan)
        if x % 6 == 0:
            for y in range(H):
                put(img, x, y, IMP_SHEEN, 0.18)
    band(img, int(H * 0.30), int(H * 0.30) + 1, GOLD_DARK)          # cercle d'or sous le fleuron
    if k % 2 == 0:                                                  # chrysanthemes d'or sur un pan sur deux
        gk.kiku(img, W / 2.0, H * 0.6, min(W, H * 0.6) * (0.32 if k % 4 == 0 else 0.22))
    band(img, H - 7, H - 6, GOLD_DARK)
    band(img, H - 6, H - 3, GOLD)
    band(img, H - 3, H - 1, CRIMSON)
    band(img, H - 1, H, GOLD_DARK)
    for row in img:
        for p in row:
            for i in range(3):
                p[i] = clamp(p[i] * light)
    return img


def royal_veil(face, w, h, seed):
    """Voile de nuque de soie cramoisie, ourle d'or, petite chrysanthemes."""
    img = cloth(w, h, CRIMSON_DARK, seed, 0.16, 0.03)
    if face not in ("top", "bottom"):
        for x in range(6, w, 12):
            gk.kiku(img, x, h * 0.45, 2.2, GOLD_LIGHT, GOLD_DARK, GOLD)
        band(img, h - 3, h - 1, GOLD)
    return finish(img, face)


def build_royal_head():
    m = Model("Head")
    kids = gk.cone_panels(m, 16, 31.0, 9.5, 27.5, royal_kasa_panel)        # jingasa imperial (meme cone que le Sakura)
    kids.append(box(m, "Couronne", (0, 13.5, 0), (24, 2, 24), plain(CRIMSON)))
    kids.append(box(m, "Fleuron", (0, 27.5, 0), (5, 2, 5), metal()))
    kids.append(box(m, "Fleuron_Haut", (0, 29, 0), (2.5, 2, 2.5), metal(GOLD_LIGHT)))
    kids.append(box(m, "Fleuron_Pointe", (0, 30.6, 0), (1.2, 1.4, 1.2), metal(GOLD_LIGHT)))
    # hachigane imperial : bandeau de soie noire et plaque laquee a la chrysantheme ; voile de nuque cramoisi
    band_paint = fabric(IMP_DARK, motifs=[wrap_folds(IMP_DARK, IMP_LIGHT, 6, 0.5)])
    kids.append(box(m, "Hachimaki", (0, 5, 0), (31.6, 4.4, 29.8), band_paint))
    kids.append(box(m, "Hachigane", (0, 5, 15.3), (12, 5.6, 1.2), imperial_lamellar(kiku_face="front", rows=7)))
    kids.append(box(m, "Shikoro", (0, 1.5, -15.5), (26, 11, 0.8), royal_veil))
    kids += gk.cords_head(m, plain(GOLD))
    tassel = box(m, "R-Pompon", (-15.7, -17.5, 8), (2.5, 5, 2.5), plain(CRIMSON))
    kids += sym(m, tassel)
    return m, [piece(m, "Head", (0, 0, 0), kids)]


def build_royal_chest():
    m = Model("Chest")
    fuki = hem_band(CRIMSON, GOLD)

    def arm():
        tamoto = box(m, "R-Tamoto", (-1, -1, -5.7), (15, 19, 30),
                     royal_body(wave=True, kiku=(0.5, 0.42, 0.22), lining=CRIMSON, flowers=False))
        ring = box(m, "R-Fuki_Manche", (-1, -10.5, -5.7), (16, 2, 31), fuki)
        juban = box(m, "R-Juban", (-1, -11.8, 0), (14, 3, 16), plain(IVORY))
        forearm = piece(m, "R-Forearm", (-1, 0, 0), [tamoto, ring, juban])
        sode = box(m, "R-Sode", (-1, 0.5, -2), (14, 23, 22), royal_body())
        # o-sode de garde : lamelles de laque lacees de cramoisi, bordees d'or, au mon de l'Empire
        plate = box(m, "R-Epauliere", (-2.2, 6.5, -2), (15.5, 10, 23.5), imperial_lamellar(mon_face="left", rows=5))
        lace = box(m, "R-Epauliere_Lacet", (-2.2, 1.2, -2), (15.9, 1.2, 23.9), plain(CRIMSON))
        tassel = box(m, "R-Epauliere_Agemaki", (-10.4, 3.4, -2), (1.2, 3.2, 3.2), plain(CRIMSON_DARK))
        return piece(m, "R-Arm", (-17, 0, 0), [sode, plate, lace, tassel, forearm])

    def thigh():
        suso = box(m, "R-Suso", (-1, 0.5, 0), (17, 17, 23), royal_body(wave=True))
        ring = box(m, "R-Fuki_Pan", (-1.6, -8, 0), (18, 2, 24), fuki)
        # kusazuri : tassettes lamellaires sur le devant et le cote du pan (suivent la jambe)
        front = box(m, "R-Kusazuri", (-1, 2.5, 12.4), (15, 12, 1.2), imperial_lamellar(rows=4))
        side = box(m, "R-Kusazuri_Cote", (-10.3, 2.5, 0.5), (1.2, 12, 19), imperial_lamellar(rows=4))
        return piece(m, "R-Thigh", (-8, -1, 1), [suso, ring, front, side], offset=(0, -9, 0))

    right_arm, right_thigh = arm(), thigh()
    plate = box(m, "Torse", (0, 0, 0), (33, 24, 24), royal_body(mon=[("back", 0.5, 0.32, 9, GOLDS)], opening=True))
    col = stripes([(0.14, GOLD), (0.5, IVORY), (0.36, CRIMSON)])
    gold_cord = plain(GOLD)
    honor = [rod(m, "Cordon_Honneur_A", (-15.5, 11, 6), (-6, -1, 13.6), 0.9, gold_cord),
             rod(m, "Cordon_Honneur_B", (-15.5, 9, 9), (-8, -4, 13.6), 0.9, gold_cord),
             box(m, "Cordon_Honneur_Ferret", (-6.6, -2.2, 13.8), (1.4, 3.2, 1.4), metal(GOLD_LIGHT)),
             box(m, "Cordon_Honneur_Fusa", (-6.6, -5.6, 13.8), (2.2, 3.6, 2.2), plain(CRIMSON))]
    chest = piece(m, "Chest", (0, 5, 0), [plate] + gk.lapels(m, col, col, 5) + gk.neck_band(m, plain(IVORY)) + honor
                  + [right_arm, mirror(m, right_arm)], offset=(0, 11, 0))

    brocade = fabric(GOLD_DARK, motifs=[pat("kikko", GOLD_LIGHT, 0.45, R=4), imperial_kiku_motif(0.5, 0.5, 0.18)])
    taiko = fabric(CRIMSON_DARK, motifs=[silk_sheen(0.25, CRIMSON), imperial_kiku_motif(0.5, 0.5, 0.3), edge_piping(GOLD)])
    belly = box(m, "Ventre", (0, 0, 0), (31, 16, 23), royal_body())
    belt = box(m, "Obi", (0, -2, 0), (36, 11, 27), brocade)
    age = box(m, "Obi_Age", (0, 4, 0), (35, 2, 26), plain(CRIMSON))
    cord = box(m, "Obijime", (0, -2, 0), (37, 1.5, 28), wraps(CRIMSON, CRIMSON_DARK))
    knot = box(m, "Noeud", (0, -2, 14.5), (5, 3.5, 2), metal())
    bow = box(m, "Musubi", (0, -1, -15), (21, 14, 5), taiko)
    top = box(m, "Musubi_Pli", (0, 7, -14.4), (17, 3, 4.2), plain(GOLD_DARK))
    wing = box(m, "R-Aile", (-13.5, 0.5, -14), (9, 6, 3), taiko, q=q_axis((0, 0, 1), 16))
    tail = box(m, "Tare", (0, -11, -14), (15, 9, 2), taiko)
    node = piece(m, "Belly", (0, 8, 0), [belly, belt, age, cord, knot, bow, top, wing, mirror(m, wing), tail, chest], offset=(0, 4, 0))
    hip = box(m, "Hanches", (0, -0.5, 0), (34.5, 13, 26), royal_body())
    return m, [piece(m, "Pelvis", (0, 12, 0), [hip, right_thigh, mirror(m, right_thigh), node])]


def build_royal_hands():
    m = Model("Hands")
    wrap = box(m, "R-Bandes", (0, -3.5, 0), (10, 9, 14), wraps(edge=CRIMSON))
    splint = box(m, "R-Kote", (0, -2.5, 0), (11.6, 12, 15.6), imperial_lamellar(rows=4))
    glove = box(m, "R-Gant", (0, 2, 0), (11, 8, 15), plain(IMP_DARK))
    plate = box(m, "R-Plaque", (-6, 1.5, 0), (2, 8, 11), lacquer_c(IMP, mon=("right", GOLDS)))
    lace = box(m, "R-Lacet", (0, -0.8, 0), (11.5, 1, 15.5), plain(GOLD))
    hand = piece(m, "R-Hand", (0, -8, 0), [glove, plate, lace], offset=(0, -5, 0))
    right = piece(m, "R-Forearm", (-18, 40, 0), [wrap, splint, hand])
    return m, [right, mirror(m, right)]


def build_royal_legs():
    m = Model("Legs")
    upper = box(m, "R-Hakama_Haut", (-0.5, -1, 0), (16, 24, 20), royal_pleats())
    mid = box(m, "R-Hakama_Genou", (-1.3, 5, -0.5), (17.5, 13, 23.5), royal_pleats())
    low = box(m, "R-Hakama_Bas", (-2.6, -6, -1), (20, 11, 28), royal_pleats(hem=True, flowers=True))
    ring = box(m, "R-Hakama_Ourlet", (-2.7, -11.2, -1), (20.2, 1.5, 28.6), hem_band(CRIMSON, GOLD))
    ankle = box(m, "R-Guetre", (0, -9, 0), (12, 6, 14), wraps(edge=CRIMSON))
    calf = piece(m, "R-Calf", (0, -12, 3), [mid, low, ring, ankle], offset=(0, -12, -3))
    thigh = piece(m, "R-Thigh", (-8, -1, 1), [upper, calf], offset=(0, -9, 0))
    sock = box(m, "R-Tabi", (0, 1, 0), (15, 7, 21), gk.tabi_paint)
    sole = box(m, "R-Zori", (-0.6, -3, 0), (16, 1.5, 23), lacquer_c(IMP_DARK))
    edge = box(m, "R-Zori_Semelle", (-0.9, -3.9, 0), (16.5, 0.5, 23.5), plain(GOLD_DARK))
    foot = piece(m, "R-Foot", (-7.85, 7, -3), [sock, sole, edge] + gk.thong(m, CRIMSON), offset=(0, -3, 6))
    waist = box(m, "Koshi", (0, 0, 0), (29, 11, 23.6), royal_pleats())
    board = box(m, "Koshi_Ita", (0, 0.8, -12.6), (14, 7.6, 2), imperial_lamellar(mon_face="back", rows=8))
    return m, [piece(m, "Pelvis", (0, 51, 0), [waist, board, thigh, mirror(m, thigh)]), foot, mirror(m, foot)]


# ---------------------------------------------------------------------------------------------------------------------
# Garde royale et Alchimiste royal (2026-11-10) : premieres versions, en variantes de couleur de tenues existantes
#   Garde_Royale : Haori de la Garde d'Honneur en noir laque, or et cramoisi (gardes et chevaliers royaux de l'Empereur)
#   Alchimiste   : Kariginu du Lettre en vert de jade et bronze (alchimiste royal : pas de magie dans l'Empire Luo, mais
#                  l'alchimie y est reine)
# ---------------------------------------------------------------------------------------------------------------------
PALETTES = {
    "Garde_Royale": dict(GARDE=(20, 18, 24), GARDE_LIGHT=(74, 62, 58), GARDE_DARK=(8, 8, 10)),
    "Alchimiste": dict(ASAGI=(34, 92, 70), ASAGI_LIGHT=(122, 182, 150), ASAGI_DARK=(16, 52, 40),
                       SASHINUKI=(116, 76, 34), SASHINUKI_LIGHT=(178, 126, 64)),
}


class palette:
    """Remplace des couleurs du module le temps d'ecrire une tenue (les fonctions de construction les lisent a l'appel)."""
    def __init__(self, name):
        self.colors = PALETTES[name]

    def __enter__(self):
        g = globals()
        self.saved = {k: g[k] for k in self.colors}
        g.update(self.colors)

    def __exit__(self, *exc):
        globals().update(self.saved)


# ---------------------------------------------------------------------------------------------------------------------
# Tenues et items
# ---------------------------------------------------------------------------------------------------------------------
# Les variantes du kimono Noble sont devenues les kimonos des Doyens de l'Empire Luo (2026-11-10) : un par clan, remis au
# Patriarche du clan (FamilyKeySystem). Identifiants inchanges (Armor_Kimono_Noble_*).
DOYENS = {"Noble": ("noir", "Kage"), "Noble_Cramoisi": ("cramoisi", "Sama"), "Noble_Jade": ("blanc et jade", "Setsuka"),
          "Noble_Violet": ("violet", "Hanazakura")}


def noble_set(name, label):
    color, clan = DOYENS[name]
    suffix = " (" + color + ")"
    intro = "Tenue du Doyen du Clan %s, un des quatre Doyens de l'Empire Luo." % clan
    return {
        "prefix": "Armor_Kimono_%s_" % name, "quality": "Unique", "level": 450, "durability": 320,
        "build": noble_builders(name),
        "pieces": {
            "Head": ("Eboshi du Doyen" + suffix, intro + " Un eboshi de laque noire au mon de l'empire, noué sous le menton.", 20, 29, 0.088),
            "Chest": ("Haori du Doyen" + suffix, intro + " Un haori damassé aux cinq mon du dragon et du soleil noir, ourlé de vagues, sur un kimono de soie ; un éventail glissé dans l'obi.", 37, 53, 0.163),
            "Hands": ("Gants du Doyen" + suffix, intro + " Des gants de soie ivoire et des manchettes damassées frappées du mon.", 15, 22, 0.07),
            "Legs": ("Hakama du Doyen" + suffix, intro + " Un hakama rayé à la manière de Sendai, ourlé de vagues, porté sur des tabi et des zori laqués.", 28, 41, 0.119),
        },
    }


SETS = {
    "Recrue": {
        "prefix": "Armor_Kimono_Recrue_", "quality": "Rare", "level": 60, "durability": 110,
        "build": {"Head": build_recruit_head, "Chest": build_recruit_chest, "Hands": build_recruit_hands, "Legs": build_recruit_legs},
        "pieces": {
            "Head": ("Hachimaki de Recrue", "Tenue de Recrue du Clan Luo. Un bandeau de coton blanc, cousu du mon du dragon et du soleil noir.", 7, 9, 0.04),
            "Chest": ("Keikogi de Recrue", "Tenue de Recrue du Clan Luo. Un keikogi de coton écru piqué de sashiko, au col noir surpiqué, et la tasuki cramoisie nouée dans le dos.", 13, 17, 0.074),
            "Hands": ("Bandes de Recrue", "Tenue de Recrue du Clan Luo. Des bandes de lin serrées autour des poignets et des phalanges.", 5, 7, 0.03),
            "Legs": ("Hakama de Recrue", "Tenue de Recrue du Clan Luo. Un hakama indigo noué devant, porté sur des tabi et des waraji de paille.", 10, 12, 0.056),
        },
    },
    "Marchand": {
        "prefix": "Armor_Kimono_Marchand_", "quality": "Rare", "level": 120, "durability": 140,
        "build": {"Head": build_merchant_head, "Chest": build_merchant_chest, "Hands": build_merchant_hands, "Legs": build_merchant_legs},
        "pieces": {
            "Head": ("Kasa du Comptoir", "Tenue des Marchands du Comptoir de la Porte du Dragon. Un large kasa de paille tressée, au bord laqué d'ocre et au mon du clan.", 8, 11, 0.044),
            "Chest": ("Kimono du Comptoir", "Tenue des Marchands du Comptoir de la Porte du Dragon. Un kimono brun aux sept trésors d'or, un tablier indigo au mon, une bourse et un chapelet de pièces.", 15, 20, 0.081),
            "Hands": ("Manchettes du Comptoir", "Tenue des Marchands du Comptoir de la Porte du Dragon. Des manchettes indigo piquées de blanc, pour ne pas tacher la soie en comptant.", 6, 8, 0.033),
            "Legs": ("Momohiki du Comptoir", "Tenue des Marchands du Comptoir de la Porte du Dragon. Un pantalon ajusté, des jambières blanches et des zori de paille.", 11, 16, 0.062),
        },
    },
    "Negociant": {
        "prefix": "Armor_Kimono_Negociant_", "quality": "Rare", "level": 160, "durability": 160,
        "build": {"Head": build_negociant_head, "Chest": build_negociant_chest, "Hands": build_negociant_hands, "Legs": build_negociant_legs},
        "pieces": {
            "Head": ("Daikoku-zukin du Grand Négociant", "Tenue des Grands Négociants du Comptoir, marchands du Clan Luo. Un bonnet rond de soie prune, au mon d'or, et des lunettes cerclées d'or pour compter juste.", 9, 12, 0.046),
            "Chest": ("Haori du Grand Négociant", "Tenue des Grands Négociants du Comptoir, marchands du Clan Luo. Un haori prune semé de pièces d'or, un obi de brocart, une bourse pleine, un chapelet de pièces et un boulier à la ceinture.", 17, 22, 0.086),
            "Hands": ("Manchettes du Grand Négociant", "Tenue des Grands Négociants du Comptoir, marchands du Clan Luo. Des manchettes prune bordées d'or, des gants ivoire et une bague d'or.", 7, 9, 0.035),
            "Legs": ("Hakama du Grand Négociant", "Tenue des Grands Négociants du Comptoir, marchands du Clan Luo. Un hakama rayé d'or ourlé de vagues, des tabi blancs et des geta de laque.", 12, 17, 0.065),
        },
    },
    "Pelerin": {
        "prefix": "Armor_Kimono_Pelerin_", "quality": "Epic", "level": 300, "durability": 240,
        "build": {"Head": build_pilgrim_head, "Chest": build_pilgrim_chest, "Hands": build_pilgrim_hands, "Legs": build_pilgrim_legs},
        "pieces": {
            "Head": ("Kasa du Pèlerin", "Tenue des Pèlerins de la vallée, messagers du Clan Luo. Un kasa profond de paille au bord laqué, une clochette d'or au front.", 16, 23, 0.076),
            "Chest": ("Kimono du Pèlerin", "Tenue des Pèlerins de la vallée, messagers du Clan Luo. Un kimono gris aux feuilles de chanvre, col de coton blanc et clochette à l'obi, qui franchit les cols par tous les temps.", 30, 42, 0.141),
            "Hands": ("Tekkō du Pèlerin", "Tenue des Pèlerins de la vallée, messagers du Clan Luo. Des protège-mains de coton blanc, lacés de cramoisi.", 12, 17, 0.057),
            "Legs": ("Hakama du Pèlerin", "Tenue des Pèlerins de la vallée, messagers du Clan Luo. Un hakama resserré au genou, des kyahan blanches et des waraji.", 22, 33, 0.106),
        },
    },
    "Ombre": {
        "prefix": "Armor_Kimono_Ombre_", "quality": "Unique", "level": 400, "durability": 300, "hide": {"Head": HIDE_HELMET},
        "build": {"Head": build_shadow_head, "Chest": build_shadow_chest, "Hands": build_shadow_hands, "Legs": build_shadow_legs},
        "pieces": {
            "Head": ("Zukin de l'Ombre", "Tenue de l'Ordre de l'Ombre du Clan Luo. Un capuchon enroulé, un masque qui ne laisse voir que les yeux et un hachigane d'acier au mon.", 19, 26, 0.084),
            "Chest": ("Shinobi-gi de l'Ombre", "Tenue de l'Ordre de l'Ombre du Clan Luo. Un uwagi noir sur cotte de mailles, des épaulières lacées de cramoisi, un baudrier à trois kunai, des shuriken à l'obi et un tantō en travers du dos.", 35, 48, 0.155),
            "Hands": ("Tekkō de l'Ombre", "Tenue de l'Ordre de l'Ombre du Clan Luo. Des attelles d'acier bruni sur mailles, lacées de cramoisi, et des gants de cuir noir.", 15, 20, 0.067),
            "Legs": ("Hakama de l'Ombre", "Tenue de l'Ordre de l'Ombre du Clan Luo. Un pantalon ajusté, un porte-kunai à la cuisse, des genouillères matelassées, des jambières lacées et des jika-tabi silencieux.", 26, 36, 0.114),
        },
    },
    "Noble": noble_set("Noble", ""),
    "Noble_Cramoisi": noble_set("Noble_Cramoisi", "cramoisi"),
    "Noble_Jade": noble_set("Noble_Jade", "blanc et jade"),
    "Noble_Violet": noble_set("Noble_Violet", "violet"),
    "Cour": {
        "prefix": "Armor_Kimono_Cour_", "quality": "Epic", "level": 320, "durability": 250,
        "build": {"Head": build_court_head, "Chest": build_court_chest, "Hands": build_court_hands, "Legs": build_court_legs},
        "pieces": {
            "Head": ("Coiffe de Cour", "Robe de Cour, tenue des nobles de l'Empire Luo (comtes, barons). Une haute coiffe de laque noire, un ruban qui retombe dans le dos, des épingles d'or à pampilles.", 17, 24, 0.079),
            "Chest": ("Robe de Cour", "Robe de Cour, tenue des nobles de l'Empire Luo (comtes, barons). Des robes de soie superposées dont les bords se suivent en cramoisi, or, jade et ivoire, de longues manches traînantes et une traîne damassée d'or.", 31, 44, 0.145),
            "Hands": ("Manches de Cour", "Robe de Cour, tenue des nobles de l'Empire Luo (comtes, barons). Des poignets de soie où paraissent les robes de dessous, sur des gants ivoire.", 13, 18, 0.06),
            "Legs": ("Hakama de Cour", "Robe de Cour, tenue des nobles de l'Empire Luo (comtes, barons). Un long hakama écarlate qui traîne sur des tabi et des zori laqués.", 23, 34, 0.109),
        },
    },
    "Lettre": {
        "prefix": "Armor_Kimono_Lettre_", "quality": "Epic", "level": 330, "durability": 255,
        "build": {"Head": build_scholar_head, "Chest": build_scholar_chest, "Hands": build_scholar_hands, "Legs": build_scholar_legs},
        "pieces": {
            "Head": ("Eboshi du Lettré", "Kariginu du Lettré, tenue des intendants et des lettrés de l'Empire Luo. Un haut eboshi de laque noire au mon d'argent.", 17, 25, 0.08),
            "Chest": ("Kariginu du Lettré", "Kariginu du Lettré, tenue des intendants et des lettrés de l'Empire Luo. Une robe de chasse à col rond, couleur d'eau, aux manches lacées de cordons ; un rouleau et une tablette de bois glissés à la ceinture.", 32, 45, 0.147),
            "Hands": ("Manchettes du Lettré", "Kariginu du Lettré, tenue des intendants et des lettrés de l'Empire Luo. Des manchettes couleur d'eau et des gants ivoire tachés d'encre.", 13, 18, 0.061),
            "Legs": ("Sashinuki du Lettré", "Kariginu du Lettré, tenue des intendants et des lettrés de l'Empire Luo. Un sashinuki violet bouffant, noué aux chevilles, et des asagutsu de laque noire.", 24, 35, 0.11),
        },
    },
    "Garde": {
        "prefix": "Armor_Kimono_Garde_", "quality": "Epic", "level": 340, "durability": 265,
        "build": {"Head": build_guard_head, "Chest": build_guard_chest, "Hands": build_guard_hands, "Legs": build_guard_legs},
        "pieces": {
            "Head": ("Hachigane de la Garde d'Honneur", "Haori de la Garde d'Honneur, tenue des Nobles d'épée du Clan Luo. Un bandeau nuit, une plaque d'acier frappée du mon et des rubans d'or.", 18, 25, 0.082),
            "Chest": ("Haori de la Garde d'Honneur", "Haori de la Garde d'Honneur, tenue des Nobles d'épée du Clan Luo. Un haori court bleu nuit au grand mon d'or, bordé d'or, et le cordon d'honneur sur l'épaule.", 33, 46, 0.15),
            "Hands": ("Kote de la Garde d'Honneur", "Haori de la Garde d'Honneur, tenue des Nobles d'épée du Clan Luo. Des attelles d'acier lacées de cramoisi et des plaques de main au mon.", 14, 19, 0.063),
            "Legs": ("Hakama de la Garde d'Honneur", "Haori de la Garde d'Honneur, tenue des Nobles d'épée du Clan Luo. Un hakama serré au genou, rayé d'or, des jambières lacées et des waraji.", 24, 36, 0.112),
        },
    },
    "Sama": {
        "prefix": "Armor_Kimono_Sama_", "quality": "Epic", "level": 340, "durability": 265,
        "hide": {"Chest": ["Overtop", "Undertop", "Cape"]},                      # bras nus : pas de haut de dessous
        "build": {"Head": build_sama_head, "Chest": build_sama_chest, "Hands": build_sama_hands, "Legs": build_sama_legs},
        "pieces": {
            "Head": ("Hachimaki de Sama", "Tenue du Clan Sama, pugilistes de l'Empire Luo. Un bandeau cramoisi au soleil d'or du clan, aux longues queues qui claquent à chaque coup.", 18, 25, 0.082),
            "Chest": ("Dōgi de Sama", "Tenue du Clan Sama. Un dōgi cramoisi piqué de sashiko, aux manches arrachées pour libérer les bras, le soleil du clan au dos, la poitrine serrée de sarashi ; ceinture noire et corde tressée d'or.", 33, 46, 0.15),
            "Hands": ("Poings de Sama", "Tenue du Clan Sama. Des avant-bras bandés et lacés de cramoisi, des bracelets de fer et des plaques de poing cloutées.", 16, 21, 0.068),
            "Legs": ("Tattsuke de Sama", "Tenue du Clan Sama. Un pantalon noir serré aux mollets, des bandes de jambe lacées de cramoisi et des pieds bandés sur des demi-sandales de paille.", 24, 36, 0.112),
        },
    },
    "Garde_Royale": {
        "prefix": "Armor_Kimono_Garde_Royale_", "quality": "Legendary", "level": 480, "durability": 340,
        "build": {"Head": build_royal_head, "Chest": build_royal_chest, "Hands": build_royal_hands, "Legs": build_royal_legs},
        "pieces": {
            "Head": ("Jingasa de la Garde Royale", "Tenue de la Garde Royale de l'Empereur Luo. Un grand chapeau conique de laque noire aux chrysanthèmes d'or impériales, cerclé d'or et de cramoisi.", 21, 30, 0.09),
            "Chest": ("Kimono de la Garde Royale", "Tenue de la Garde Royale de l'Empereur Luo. Un kimono de soie noire aux écailles d'or, longues manches aux chrysanthèmes et vagues cramoisies, épaulières laquées au mon de l'Empire, obi de brocart d'or.", 38, 54, 0.165),
            "Hands": ("Kote de la Garde Royale", "Tenue de la Garde Royale de l'Empereur Luo. Des attelles de laque noire bordées d'or, des plaques de main au mon de l'Empire.", 16, 22, 0.07),
            "Legs": ("Hakama de la Garde Royale", "Tenue de la Garde Royale de l'Empereur Luo. Un hakama plissé de soie noire ourlé d'or et de cramoisi, des zori de laque.", 28, 41, 0.122),
        },
    },
    "Alchimiste": {
        "prefix": "Armor_Kimono_Alchimiste_", "quality": "Epic", "level": 350, "durability": 260,
        "build": {"Head": build_scholar_head, "Chest": build_scholar_chest, "Hands": build_scholar_hands, "Legs": build_scholar_legs},
        "pieces": {
            "Head": ("Eboshi de l'Alchimiste Royal", "Tenue de l'Alchimiste Royal de l'Empire Luo, où l'alchimie remplace la magie. Un haut eboshi de laque au mon d'argent.", 17, 25, 0.08),
            "Chest": ("Kariginu de l'Alchimiste Royal", "Tenue de l'Alchimiste Royal de l'Empire Luo. Une robe à col rond vert de jade, aux manches lacées de cordons ; des rouleaux de formules glissés à la ceinture.", 32, 45, 0.147),
            "Hands": ("Manchettes de l'Alchimiste Royal", "Tenue de l'Alchimiste Royal de l'Empire Luo. Des manchettes de jade et des gants ivoire tachés de réactifs.", 13, 18, 0.061),
            "Legs": ("Sashinuki de l'Alchimiste Royal", "Tenue de l'Alchimiste Royal de l'Empire Luo. Un sashinuki couleur de bronze, noué aux chevilles, et des asagutsu de laque.", 24, 35, 0.11),
        },
    },
    "Ronin": {
        "prefix": "Armor_Kimono_Ronin_", "quality": "Rare", "level": 100, "durability": 130,
        "build": {"Head": build_ronin_head, "Chest": build_ronin_chest, "Hands": build_ronin_hands, "Legs": build_ronin_legs},
        "pieces": {
            "Head": ("Hachimaki du Rōnin", "Rōnin Vagabond, tenue des aventuriers de la vallée. Un bandeau blanc noué derrière la tête, aux longs pans.", 8, 10, 0.042),
            "Chest": ("Haori du Rōnin", "Rōnin Vagabond, tenue des aventuriers de la vallée. Un haori usé et rapiécé, le mon de l'ancien maître arraché dans le dos ; une gourde à la ceinture et un baluchon sur l'épaule.", 14, 19, 0.078),
            "Hands": ("Bandes du Rōnin", "Rōnin Vagabond, tenue des aventuriers de la vallée. Des bandes de toile serrées autour des poignets.", 6, 8, 0.032),
            "Legs": ("Hakama du Rōnin", "Rōnin Vagabond, tenue des aventuriers de la vallée. Un hakama indigo délavé, effiloché au bas, et des waraji de paille.", 11, 14, 0.06),
        },
    },
    "Rizieres": {
        "prefix": "Armor_Kimono_Rizieres_", "quality": "Common", "level": 10, "durability": 50,
        "build": {"Head": build_field_head, "Chest": build_field_chest, "Hands": build_field_hands, "Legs": build_field_legs},
        "pieces": {
            "Head": ("Sugegasa des Rizières", "Kimono des Rizières, tenue des villageois de la vallée. Un large chapeau conique de paille, contre le soleil et la pluie.", 2, 3, 0.015),
            "Chest": ("Noragi des Rizières", "Kimono des Rizières, tenue des villageois de la vallée. Une veste de travail indigo piquée de sashiko, aux manches retroussées, un tablier rayé et un tenugui à la ceinture.", 5, 7, 0.03),
            "Hands": ("Tekkō des Rizières", "Kimono des Rizières, tenue des villageois de la vallée. Des protège-mains indigo, contre la paille et la boue.", 2, 2, 0.012),
            "Legs": ("Momohiki des Rizières", "Kimono des Rizières, tenue des villageois de la vallée. Un pantalon retroussé au genou, des jambières de toile et des waraji.", 4, 5, 0.025),
        },
    },
    "Setsuka": {
        "prefix": "Armor_Kimono_Setsuka_", "quality": "Epic", "level": 180, "durability": 180,
        "build": {"Head": gk.build_sakura_head, "Chest": gk.build_sakura_chest, "Hands": gk.build_sakura_hands, "Legs": gk.build_sakura_legs},
        "pieces": {
            "Head": ("Kasa de Setsuka", "Tenue du Clan Setsuka, maîtres de l'épée de l'Empire Luo. Chapeau laqué de nuit, peint de lunes et de nuages.", 13, 18, 0.07),
            "Chest": ("Kimono de Setsuka", "Tenue du Clan Setsuka. Soie blanche nacrée teintée d'argent bleuté, semée de croissants de lune et d'étoiles, serrée par un obi bleu nuit.", 24, 33, 0.126),
            "Hands": ("Tekkō de Setsuka", "Tenue du Clan Setsuka. Bandes de lin et plaques blanches frappées d'un croissant d'argent.", 10, 15, 0.056),
            "Legs": ("Hakama de Setsuka", "Tenue du Clan Setsuka. Pantalon plissé gris ardoise semé de lunes, porté sur des tabi et des zori.", 18, 24, 0.098),
        },
    },
    "Seigneur": {
        "prefix": "Armor_Kimono_Seigneur_", "quality": "Legendary", "level": 500, "durability": 380,
        "build": {"Head": build_lord_head, "Chest": build_lord_chest, "Hands": build_lord_hands, "Legs": build_lord_legs},
        "pieces": {
            "Head": ("Eboshi du Seigneur", "Tenue des Seigneurs et des Doyens du Clan Luo. Un haut eboshi de laque, le grand mon d'or au front, traversé d'une épingle de jade.", 22, 33, 0.094),
            "Chest": ("Kamishimo du Seigneur", "Tenue des Seigneurs et des Doyens du Clan Luo. Un kataginu brodé d'or aux épaules frappées du mon, sur un kimono aux écailles de dragon ; un éventail et un inro de laque à l'obi.", 41, 60, 0.174),
            "Hands": ("Gants du Seigneur", "Tenue des Seigneurs et des Doyens du Clan Luo. Des gants de soie noire et des manchettes brodées d'or.", 17, 27, 0.075),
            "Legs": ("Nagabakama du Seigneur", "Tenue des Seigneurs et des Doyens du Clan Luo. Un long hakama noir rayé d'or, noué devant et ourlé de vagues d'or, qui tombe sur les pieds.", 30, 45, 0.127),
        },
    },
}

if __name__ == "__main__":
    import sys
    only = sys.argv[3:]
    chosen = {k: v for k, v in SETS.items() if (not only or k in only) and k != "Setsuka" and k not in PALETTES}
    reports = write_sets(RES, PREVIEW, MODEL_DIR, "Kimono", chosen, "luo", family_tag="Cloth_Kimono", closeup_fit=6.0) if chosen else {}
    if not only or "Setsuka" in only:
        with setsuka_palette():
            reports.update(write_sets(RES, PREVIEW, MODEL_DIR, "Kimono", {"Setsuka": SETS["Setsuka"]}, "luo",
                                      family_tag="Cloth_Kimono", closeup_fit=6.0))
    for name in PALETTES:                                                       # variantes de couleur
        if not only or name in only:
            with palette(name):
                reports.update(write_sets(RES, PREVIEW, MODEL_DIR, "Kimono", {name: SETS[name]}, "luo",
                                          family_tag="Cloth_Kimono", closeup_fit=6.0))
    for set_name, nodes in reports.items():
        issues = coplanar_report(nodes)
        if issues:
            print(set_name, "faces coplanaires :", len(issues))
            for line in issues[:16]:
                print("   ", line)
