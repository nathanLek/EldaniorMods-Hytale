"""Tenryumaru, Lame du Premier Serment (relique du Clan Luo) : modele, texture, icone, animations, objets, interactions, lames d'or.

Le katana de Luo Tenryu, fondateur du clan, forge d'un croc de son pere le Dieu-Dragon d'Or. Lame d'acier noir a la ligne de
trempe d'or, horimono de dragon, habaki d'or, tsuba ronde au mon du clan, poignee de soie noire sur galuchat ivoire ; fourreau
(saya) de laque noire paillete d'or, mon d'or, embouchure, bagues et bout d'or, cordon sageo cramoisi tresse d'or.
Legendaire, niveau 550 ; seul le Patriarche des Luo la manie, et elle lui revient toujours (FamilyKeySystem).

Le modele a deux racines : R-Attachment = la lame degainee (cachee par defaut), L-Attachment = le fourreau tenu dans la main
gauche, lame vers l'arriere, avec une copie de la monture (la lame rengainee). Les animations (tenryumaru_anims.py) basculent
la visibilite des deux, mais le jeu ignore shapeVisible sur les objets tenus (vu en jeu) : la forme au fourreau a donc un modele
vide en main droite, et Java echange les objets a l'instant de la saisie. Le fourreau est un objet utilitaire (main gauche).
  - Artefact_Sword_Tenryumaru (au fourreau) : clic = degaine-coupe ; maintien = posture iai puis Flash Celeste (Java) ;
    clic droit maintenu = garde au fourreau ; signature = les Dix Serments (dix clones, Java).
  - Artefact_Sword_Tenryumaru_Degainee (au clair, hors menu creatif) : combo de l'epee vanilla, chaque coup lance une lame d'or
    (TenryuCoup*) ; maintien (variable Thrust) = posture iai puis Flash Celeste ; clic droit bref = rengainer, maintenu = garde.
Sorts Java (Tenryumaru.java) : TenryumaruDegaine, TenryumaruRengaine, FlashCeleste, DixSerments, TenryuCoup* (lames
Tenryumaru_Lame_H/G/D/V).

Meme repere de Handle que Ryukiri (gen_ryukiri.py) : x le long de la lame, +y le dos (mune), -y le tranchant, z l'epaisseur.

Usage : python3 scripts/artefacts/gen_tenryumaru.py <src/main/resources> [dossier_apercu]
"""
import copy, json, math, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "armures"))
sys.path.insert(0, os.path.join(HERE, "..", "rune_aura_icons"))
import armor_kit as K  # noqa: E402
import blason  # noqa: E402
import familles  # noqa: E402
import png  # noqa: E402
sys.path.insert(0, HERE)
from coplanar import fix_coplanar  # noqa: E402

RES = sys.argv[1]
PREVIEW = sys.argv[2] if len(sys.argv) > 2 else None
NAME = "Artefact_Sword_Tenryumaru"
KEY = "Tenryumaru"
MODEL_REL = "Items/Weapons/artefacts"
K.set_density(4, 512)

BLACK = (12, 11, 16)
BLACK_SHEEN = (54, 50, 66)
EDGE = (120, 104, 70)                                            # zone trempee : acier sombre lave d'or
EDGE_LIGHT = (214, 186, 120)
SUN_BLACK = (8, 7, 10)
IVORY = (232, 224, 204)
GOLD, GOLD_LIGHT, GOLD_DARK = K.GOLD, K.GOLD_LIGHT, K.GOLD_DARK
BLADE_TOTAL = 77.0
# couleurs reglables par katana (gen_luo_katanas.py les remplace avant de construire) : trempe, arete, soie, gravure
HAMON, HAMON_LIGHT, HAMON_DARK = K.GOLD, K.GOLD_LIGHT, K.GOLD_DARK
SHINOGI = (96, 92, 108)
SILK_DARK, SILK_LIGHT = (16, 14, 20), (48, 44, 58)
HORIMONO = True


def mixc(a, b, t):
    return K.mix(a, b, t)


# ---------------------------------------------------------------------------------------------------------------------
# Mon du clan (blason Luo sans l'ecu)
# ---------------------------------------------------------------------------------------------------------------------
def luo_mon(px):
    k = px * 4 / 2.1
    cv = blason.Canvas(k, -1.05, 1.05, -1.0, 1.1)
    familles.coiled_dragon(cv)
    return blason.shrink(cv.img, px, px)


def stamp(img, src, cx, cy):
    h, w = len(src), len(src[0])
    for y in range(h):
        for x in range(w):
            p = src[y][x]
            if p[3]:
                K.put(img, cx - w / 2.0 + x, cy - h / 2.0 + y, p[:3], p[3] / 255.0)


# ---------------------------------------------------------------------------------------------------------------------
# Lame
# ---------------------------------------------------------------------------------------------------------------------
def blade_pixel(gx, v, width_units):
    """gx = abscisse sur toute la lame (unites), v = 0 au dos, 1 au tranchant. Acier noir, trempe d'or ondulee (notare)."""
    px_unit = 1.0 / (width_units * K.D)
    sheen = (0.5 + 0.5 * math.cos((v - 0.4) * 3.0)) * 0.32 + 0.1 * K.fbm(gx * 0.2, v * 3)   # brosse dans la longueur
    c = mixc(BLACK, BLACK_SHEEN, sheen)
    if v < 0.26:
        c = mixc(c, BLACK, 0.4)
    if abs(v - 0.27) < px_unit * 0.6:                            # arete (shinogi) : filet d'acier clair
        c = SHINOGI
    hamon = 0.64 + 0.06 * math.sin(gx * 0.5) + 0.03 * math.sin(gx * 1.4 + 0.8)
    if v > hamon:
        t = (v - hamon) / max(1e-3, 1 - hamon)
        c = mixc(EDGE, EDGE_LIGHT, t * 0.5)
        if v > 1 - px_unit * 1.1:
            c = EDGE_LIGHT
    d = abs(v - hamon)
    if d < px_unit * 0.7:
        c = HAMON_LIGHT if math.sin(gx * 2.1) > 0.7 else HAMON
    elif d < px_unit * 2.4 and v < hamon:                        # halo sous la trempe
        c = mixc(c, HAMON_DARK, 0.5 * (1 - d / (px_unit * 2.4)))
    return c


def horimono(img, w, h, gx0, flip):
    """Dragon d'or grave pres de la base : corps sinueux, ecailles en points, tete cornue vers la pointe."""
    pts = []
    for i in range(40):
        t = i / 39.0
        ux = 14.5 + 13.0 * t
        vv = 0.45 + 0.13 * math.sin(t * math.pi * 3.0)
        pts.append((ux, vv))

    def to_px(ux, vv):
        lx = (ux - gx0) * K.D
        return (w - 1 - lx if flip else lx, vv * h)
    K.stroke(img, [to_px(*p) for p in pts], GOLD, 0.9)
    for ux, vv in pts[4::4]:
        x, y = to_px(ux, vv)
        K.put(img, x, y - 1, GOLD_LIGHT)
    hx, hy = to_px(*pts[-1])
    K.disc(img, hx, hy, 1.6, GOLD_LIGHT)
    hx2, hy2 = to_px(pts[-1][0] - 1.2, pts[-1][1] - 0.18)
    K.stroke(img, [(hx, hy), (hx2, hy2)], GOLD_LIGHT, 0.5)        # corne
    tx, ty = to_px(pts[0][0] - 1.5, pts[0][1] + 0.1)
    K.stroke(img, [to_px(*pts[0]), (tx, ty)], GOLD_DARK, 0.5)     # queue


def blade_paint(gx0, length, width):
    def paint(face, w, h, seed):
        img = K.canvas(w, h, BLACK)
        if face in ("front", "back"):
            for y in range(h):
                for x in range(w):
                    lx = (w - 1 - x) if face == "back" else x
                    img[y][x][:3] = list(blade_pixel(gx0 + (lx + 0.5) / K.D, (y + 0.5) / h, width))
            if gx0 < 20 and HORIMONO:
                horimono(img, w, h, gx0, face == "back")
        elif face == "top":
            for y in range(h):
                for x in range(w):
                    img[y][x][:3] = list(mixc(BLACK, BLACK_SHEEN, 0.25))
        elif face == "bottom":
            for y in range(h):
                for x in range(w):
                    img[y][x][:3] = list(mixc(EDGE_LIGHT, EDGE, abs(y - h / 2) / (h / 2)))
        return img
    return paint


def kissaki(width, length, gx0):
    w, h = int(round(length * K.D)), int(round(width * K.D))
    img = [[[0, 0, 0, 0] for _ in range(w)] for _ in range(h)]
    for y in range(h):
        for x in range(w):
            t = (x + 0.5) / w
            spine = 0.10 * t ** 3
            edge = 1 - 0.86 * t ** 1.7
            v = (y + 0.5) / h
            if v < spine or v > edge:
                continue
            vv = (v - spine) / max(1e-3, edge - spine)
            c = blade_pixel(gx0 + (x + 0.5) / K.D, vv, width * max(0.3, edge - spine))
            if v > edge - 1.2 / h:
                c = EDGE_LIGHT
            img[y][x] = [c[0], c[1], c[2], 255]
    return img


# ---------------------------------------------------------------------------------------------------------------------
# Monture
# ---------------------------------------------------------------------------------------------------------------------
def grip_paint(face, w, h, seed):
    """Tsuka : galuchat ivoire sous une tresse de soie noire en losanges ; menuki d'or (petit dragon) au milieu."""
    img = K.canvas(w, h, IVORY)
    period = 4.5 * K.D
    for y in range(h):
        for x in range(w):
            u = (x % period) / period
            vv = (y + 0.5) / h
            d = abs(u - 0.5) * 2 + abs(vv - 0.5) * 2 * (h / period)
            if d < 0.5:
                grain = 0.9 + 0.1 * K.noise(x * 1.7, y * 1.7)
                c = tuple(v * grain for v in IVORY)
            else:
                band = 0.5 + 0.5 * math.cos((x - y) * 0.9)
                c = mixc(SILK_DARK, SILK_LIGHT, band * 0.5)
            img[y][x][:3] = list(c)
    if face in ("front", "back"):
        cx, cy = w * 0.45, h * 0.5
        pts = [(cx - 7 + i * 1.0, cy + 2.2 * math.sin(i * 0.8)) for i in range(15)]
        K.stroke(img, pts, GOLD, 0.8)
        K.disc(img, pts[-1][0], pts[-1][1], 1.4, GOLD_LIGHT)
    return K.finish(img, face, edges=False)


def metal_gold(face, w, h, seed):
    """Or brosse dans la longueur, degrade de lumiere (pas de reflets en sinus diagonal)."""
    img = K.canvas(w, h, GOLD)
    for y in range(h):
        for x in range(w):
            t = (y + 0.5) / h
            c = mixc(GOLD_LIGHT, GOLD_DARK, t * 0.8)
            k = 0.94 + 0.06 * K.noise(x * 0.3, y * 2.1)
            img[y][x][:3] = [K.clamp(v * k) for v in c]
    return K.finish(img, face, edges=False)


def tsuba_paint(face, w, h, seed):
    """Tsuba ronde au mon : disque noir cercle d'or, blason du clan ; coins detoures (faces de chant : bande centrale seule)."""
    img = K.canvas(w, h, BLACK)
    if face in ("left", "right"):
        cx, cy, r = w / 2.0, h / 2.0, min(w, h) / 2.0
        for y in range(h):
            for x in range(w):
                d = math.hypot(x + 0.5 - cx, y + 0.5 - cy)
                if d > r:
                    img[y][x][3] = 0
                elif d > r - 2.5:
                    img[y][x][:3] = list(GOLD_LIGHT if d > r - 1.2 else GOLD)
                else:
                    img[y][x][:3] = list(mixc(BLACK, BLACK_SHEEN, 0.2 + 0.15 * (1 - d / r)))
        stamp(img, luo_mon(int(r * 2 - 8)), cx, cy)
        for y in range(h):                                       # trou de la soie (nakago-ana) laisse au centre, en or sombre
            for x in range(w):
                if abs(x + 0.5 - cx) < 1.2 and abs(y + 0.5 - cy) < 2.4:
                    img[y][x][:3] = list(GOLD_DARK)
        return img
    for y in range(h):                                           # chant : visible seulement pres du milieu, comme un disque
        for x in range(w):
            u = abs((x + 0.5) / w - 0.5) * 2 if face in ("top", "bottom") else abs((y + 0.5) / h - 0.5) * 2
            if face in ("front", "back"):
                u = abs((x + 0.5) / w - 0.5) * 2
            if u > 0.42:
                img[y][x][3] = 0
            else:
                img[y][x][:3] = list(GOLD)
    return img


def kashira_paint(face, w, h, seed):
    img = metal_gold(face, w, h, seed)
    if face in ("left", "right") and min(w, h) > 10:
        cx, cy, r = w / 2.0, h / 2.0, min(w, h) / 2.0 - 2
        K.disc(img, cx, cy, r, GOLD_LIGHT)
        K.disc(img, cx, cy, r - 1.5, SUN_BLACK)
    return img


def sun_gem(face, w, h, seed):
    img = K.canvas(w, h, SUN_BLACK)
    K.border(img, 0, GOLD_LIGHT)
    return img


# ---------------------------------------------------------------------------------------------------------------------
# Modele
# ---------------------------------------------------------------------------------------------------------------------
M = K.Model(NAME)
M.reuse = True
QZ = lambda deg: K.q_axis((0, 0, 1), deg)


def empty(piece):
    return {"type": "none", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": 1, "y": 1, "z": 1}, "settings": {"isPiece": piece},
            "textureLayout": {}, "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": "flat"}


def pivot(name, pos, q=K.ID_Q):
    return {"id": M.nid(), "name": name, "position": K.vec(pos), "orientation": K.round_q(q), "shape": empty(False), "children": []}


def node_box(name, pos, size, paint, q=K.ID_Q, pivot_at_start=False, shading="flat"):
    if not pivot_at_start:
        return K.box(M, name, pos, size, paint, q=q, shading=shading)
    p = pivot(name, pos, q)
    p["children"].append(K.box(M, name + "_Box", (size[0] / 2.0, 0, 0), size, paint, shading=shading))
    return p


def build():
    segs = [(24.0, 7.0, 2.0, 0.0), (22.0, 6.75, 1.75, 3.0), (20.0, 6.5, 1.5, 3.0)]
    tip_len, tip_bend = 11.0, 3.0
    gx, nodes = 0.0, []
    for i, (L, W, T, bend) in enumerate(segs):
        nodes.append(node_box("Blade_%d" % i, (0, 0, 0), (L, W, T), blade_paint(gx, L, W), q=QZ(bend), pivot_at_start=True))
        gx += L - 0.25
    tip = pivot("Blade_Tip", (0, 0, 0), QZ(tip_bend))
    tip["children"].append(K.quad(M, "Blade_Tip_Quad", (tip_len / 2.0, 0, 0), kissaki(segs[-1][1], tip_len, gx)))
    for i in range(len(nodes) - 1, 0, -1):
        nodes[i]["position"] = K.vec((segs[i - 1][0] - 0.25, 0, 0))
        nodes[i - 1]["children"].append(nodes[i])
    tip["position"] = K.vec((segs[-1][0] - 0.25, 0, 0))
    nodes[-1]["children"].append(tip)
    blade = nodes[0]

    GRIP_C = -4.0

    def at(x, y=0.0, z=0.0):
        return (x - GRIP_C, y, z)

    blade["position"] = K.vec(at(13.0))
    kids = [blade,
            node_box("Habaki", at(13.5), (2.75, 7.75, 2.75), metal_gold),
            node_box("Seppa_A", at(11.75), (0.5, 9.0, 5.0), metal_gold),
            node_box("Tsuba", at(10.75), (1.5, 15.0, 15.0), tsuba_paint),
            node_box("Seppa_B", at(9.75), (0.5, 8.5, 5.5), metal_gold),
            node_box("Fuchi", at(8.75), (1.5, 6.25, 5.25), metal_gold),
            node_box("Kashira", at(-18.75), (3.5, 6.25, 5.25), kashira_paint),
            node_box("Kashira_Soleil", at(-20.75), (0.75, 3.0, 3.0), sun_gem, shading="fullbright")]
    handle = node_box("Handle", (0, -36 + GRIP_C, 0), (26.0, 5.5, 4.5), grip_paint, q={"x": -0.5, "y": -0.5, "z": 0.5, "w": 0.5})
    handle["children"] = kids
    origin_item = {"id": M.nid(), "name": "Origin_Item", "position": K.vec((0, -37, 5)), "orientation": dict(K.ID_Q),
                   "shape": empty(False), "children": [handle]}
    origin_proj = {"id": M.nid(), "name": "Origin_Projectile", "position": K.vec((0, 73, -5)), "orientation": dict(K.ID_Q),
                   "shape": empty(True), "children": [origin_item]}
    root = {"id": M.nid(), "name": "R-Attachment", "position": K.vec((0, 20, 0)), "orientation": dict(K.ID_Q),
            "shape": empty(True), "children": [origin_proj]}
    return root, handle


# ---------------------------------------------------------------------------------------------------------------------
# Fourreau (saya), tenu dans la main gauche : racine L-Attachment
# ---------------------------------------------------------------------------------------------------------------------
LACQ = (14, 12, 18)
LACQ_SHEEN = (70, 64, 84)
SAYA_SCALES = False                                              # fourreau aux ecailles de dragon (Kinryu-O, gen_luo_katanas)
SAGEO, SAGEO_DARK = (150, 22, 34), (84, 10, 22)


def saya_paint(gx0, mon=False):
    """Laque noire roiro : reflet brosse dans la longueur, paillettes d'or (nashiji) plus denses vers l'embouchure ; mon du clan
    en makie d'or sur les deux faces du premier troncon."""
    def paint(face, w, h, seed):
        img = K.canvas(w, h, LACQ)
        for y in range(h):
            for x in range(w):
                gx = gx0 + (x + 0.5) / K.D
                v = (y + 0.5) / h
                sheen = 0.55 * (0.5 + 0.5 * math.cos((v - 0.3) * 3.4)) ** 3 + 0.06 * K.fbm(gx * 0.15, v * 2)
                c = mixc(LACQ, LACQ_SHEEN, sheen)
                dens = 0.05 + 0.10 * max(0.0, 1 - gx / 50.0)
                if K.noise(x * 1.37 + seed, y * 1.91) > 1 - dens:
                    c = mixc(c, GOLD_LIGHT, 0.75)
                if SAYA_SCALES:                                  # ecailles en quinconce : arc sombre, reflet clair en haut
                    sx, sy = 6, 4
                    row = y // sy
                    u = ((x + (row % 2) * sx / 2.0) % sx - sx / 2.0) / (sx / 2.0)
                    t = (y % sy + 0.5) / sy
                    r = u * u + (1 - t) * (1 - t) * 1.3
                    if r > 0.85:
                        c = mixc(c, GOLD_DARK, 0.6)
                    elif t < 0.45:
                        c = mixc(c, GOLD_LIGHT, 0.35)
                img[y][x][:3] = list(c)
        if face in ("front", "back") and mon:
            stamp(img, luo_mon(int(h * 0.9)), w * 0.42, h / 2.0)
        return K.finish(img, face, edges=False)
    return paint


def sageo_paint(face, w, h, seed):
    img = K.canvas(w, h, SAGEO)
    K.rope_tex(img, SAGEO, SAGEO_DARK, period=5)
    for y in range(h):                                           # fil d'or tresse dans le cordon
        for x in range(w):
            if (x + 2 * y) % 9 == 0:
                img[y][x][:3] = list(GOLD)
    return K.finish(img, face, edges=False)


SAYA_PAINTS = []


def keep(p):
    SAYA_PAINTS.append(p)                                        # le cache d'atlas est indexe par id(peinture) : la garder en vie
    return p


def build_saya(handle, pivot_pos, pivot_q):
    """Racine L-Attachment : le fourreau et une copie de la monture (la lame rengainee), dans le repere du Handle de la lame,
    place par pivot_pos / pivot_q (calcules par tenryumaru_anims d'apres la pose de la main gauche)."""
    hilt = copy.deepcopy(handle)
    hilt["children"] = [c for c in hilt["children"] if not c["name"].startswith("Blade")]

    def rename(n):
        n["id"] = M.nid()
        n["name"] = "Saya-" + n["name"]
        for c in n["children"]:
            rename(c)
    rename(hilt)
    hilt["position"] = K.vec((0, 0, 0))
    hilt["orientation"] = dict(K.ID_Q)

    # troncons du fourreau : memes pivots et memes courbures que la lame, section plus large (la lame y disparait)
    starts, bends = [17.0, 23.75, 21.75, 19.75], [0.0, 3.0, 3.0, 3.0]
    dims = [(25.0, 8.75, 3.75), (22.0, 8.5, 3.5), (20.0, 8.25, 3.25), (12.5, 8.0, 3.0)]
    segs, gx = [], -1.0
    for i, ((L, W, T), bend) in enumerate(zip(dims, bends)):
        p = pivot("Saya-Seg%d" % i, (0, 0, 0), QZ(bend))
        lead = 1.0 if i == 0 else 0.0                            # le premier troncon commence a l'embouchure (x = 16)
        paint = keep(saya_paint(gx, mon=(i == 0)))
        p["children"].append(K.box(M, "Saya-Seg%d_Box" % i, (L / 2.0 - lead, 0, 0), (L, W, T), paint))
        segs.append(p)
        gx += L - lead
    for i in range(1, len(segs)):
        segs[i]["position"] = K.vec((starts[i], 0, 0))
        segs[i - 1]["children"].append(segs[i])
    segs[0]["position"] = K.vec((starts[0], 0, 0))
    last_L = dims[-1][0]
    segs[-1]["children"].append(node_box("Saya-Kojiri", (last_L + 1.25, 0.1, 0), (2.5, 8.5, 3.5), metal_gold))
    segs[-1]["children"].append(node_box("Saya-Kojiri_Bout", (last_L + 2.75, 0.1, 0), (0.75, 6.5, 2.5), metal_gold))

    mouth = [node_box("Saya-Koiguchi", (17.0, 0, 0), (2.25, 9.5, 4.5), metal_gold),
             node_box("Saya-Bague_A", (31.0, 0, 0), (0.75, 9.0, 4.0), metal_gold),
             node_box("Saya-Bague_B", (33.0, 0, 0), (0.75, 9.0, 4.0), metal_gold),
             # kurikata (bouton du cordon), cote exterieur (-z), vers le tranchant (-y)
             node_box("Saya-Kurikata", (24.0, -1.5, -2.6), (3.0, 2.25, 1.75), metal_gold),
             # sageo : cordon cramoisi tresse d'or, qui pend du kurikata et revient vers l'embouchure
             K.rod(M, "Saya-Sageo_A", (24.0, -1.0, -3.2), (25.5, 7.0, -3.6), 1.1, sageo_paint),
             K.rod(M, "Saya-Sageo_B", (25.5, 7.0, -3.6), (20.5, 10.5, -3.4), 1.1, sageo_paint),
             K.rod(M, "Saya-Sageo_C", (20.5, 10.5, -3.4), (17.5, 6.0, -3.0), 1.1, sageo_paint),
             node_box("Saya-Sageo_Noeud", (25.5, 7.0, -3.6), (1.75, 1.75, 1.75), sageo_paint)]
    root_children = [hilt, segs[0]] + mouth
    holder = {"id": M.nid(), "name": "Saya-Pivot", "position": K.vec(pivot_pos), "orientation": K.round_q(pivot_q),
              "shape": empty(False), "children": root_children}
    return {"id": M.nid(), "name": "L-Attachment", "position": K.vec((0, 51, 6)), "orientation": dict(K.ID_Q),
            "shape": empty(True), "children": [holder]}


def display_model(root, saya):
    """Modele d'exposition (presentoir) : la lame et son fourreau, rengainee, accrochee a R-Attachment comme une arme.
    Les pieces du fourreau sont dans le repere de la poignee : on les ajoute sous le Handle de la lame."""
    out = copy.deepcopy(root)
    holder = saya["children"][0]

    def find(n):
        if n["name"] == "Handle":
            return n
        for c in n["children"]:
            f = find(c)
            if f:
                return f
        return None
    handle = find(out)
    for c in holder["children"]:
        if c["name"] != "Saya-Handle":
            handle["children"].append(copy.deepcopy(c))
    return out


def shape_names(node, out=None):
    """Noms des noeuds qui portent une forme (box / quad) : les seuls que shapeVisible cache ou montre."""
    out = [] if out is None else out
    if node["shape"]["type"] in ("box", "quad"):
        out.append(node["name"])
    for c in node["children"]:
        shape_names(c, out)
    return out


def set_visible(node, visible):
    if node["shape"]["type"] in ("box", "quad"):
        node["shape"]["visible"] = visible
    for c in node["children"]:
        set_visible(c, visible)


def handle_quads(handle, tex, q):
    return K.quads_of(copy.deepcopy(handle), (0, 0, 0), q, tex, ("\0",))


def saya_quads(saya_root, tex, q, origin, with_hilt):
    """Quads du fourreau dans le repere de la poignee (poignee rengainee incluse ou non)."""
    holder = saya_root["children"][0]
    fake = {"name": "Saya", "position": K.vec((0, 0, 0)), "orientation": dict(K.ID_Q), "shape": empty(False),
            "children": [c for c in holder["children"] if with_hilt or c["name"] != "Saya-Handle"]}
    return K.quads_of(fake, origin, q, tex, ("\0",))


def glow(img, color=(255, 196, 90), strength=0.55):
    """Halo d'or derriere l'objet : flou des pixels opaques, pose sous l'image."""
    S = len(img)
    a = [[img[y][x][3] / 255.0 for x in range(S)] for y in range(S)]
    for _ in range(4):                                          # flou en boite, 4 passes
        b = [[0.0] * S for _ in range(S)]
        for y in range(S):
            for x in range(S):
                acc = n = 0
                for dy in (-2, -1, 0, 1, 2):
                    for dx in (-2, -1, 0, 1, 2):
                        if 0 <= x + dx < S and 0 <= y + dy < S:
                            acc += a[y + dy][x + dx]
                            n += 1
                b[y][x] = acc / n
        a = b
    out = [[p[:] for p in row] for row in img]
    for y in range(S):
        for x in range(S):
            if out[y][x][3] < 128 and a[y][x] > 0.02:
                k = min(1.0, a[y][x] * 2.2) * strength
                out[y][x] = [color[0], color[1], color[2], int(255 * k)]
    return out


def make_icons(handle, saya, tex):
    """Icone du katana : la lame (eclaircie) croisee devant son fourreau, halo d'or ; icone du fourreau : katana rengaine."""
    lifted = [[[min(255, c[0] * 1.25 + 22), min(255, c[1] * 1.25 + 20), min(255, c[2] * 1.25 + 26), c[3]] for c in row] for row in tex]
    q_sword = K.q_axis((0, 0, 1), 60)
    q_saya = K.q_axis((0, 0, 1), 30)
    quads = handle_quads(handle, lifted, q_sword)
    quads += saya_quads(saya, lifted, q_saya, K.qrot(q_saya, (2.0, -6.0, -5.0)), False)
    big = K.draw(quads, 256, 12, 8, margin=0.05)
    icon = glow(outline(K.downscale(big, 4)))
    q = K.q_axis((0, 0, 1), 45)
    big2 = K.draw(saya_quads(saya, lifted, q, (0, 0, 0), True), 256, 15, 10, margin=0.04)
    sheath_icon = glow(outline(K.downscale(big2, 4)), strength=0.4)
    return icon, sheath_icon


def outline(img, color=(20, 14, 12)):
    S = len(img)
    out = [[p[:] for p in row] for row in img]
    for y in range(S):
        for x in range(S):
            if img[y][x][3] > 40:
                continue
            if any(0 <= x + dx < S and 0 <= y + dy < S and img[y + dy][x + dx][3] > 120 for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                out[y][x] = [color[0], color[1], color[2], 255]
    return out


# ---------------------------------------------------------------------------------------------------------------------
# Trainee, item, interactions
# ---------------------------------------------------------------------------------------------------------------------
def write_trail():
    trail = {"TexturePath": "Trails/Charged_Red.png", "LifeSpan": 7, "Roll": 0,
             "Start": {"Color": "rgba(#ffe7a8, 1)", "Width": 0.32},
             "End": {"Color": "rgba(#140c06, 0.4)", "Width": 0},
             "LightInfluence": 0, "Smooth": True, "RenderMode": "BlendLinear"}
    json.dump(trail, open(os.path.join(RES, "Server", "Entity", "Trails", KEY + "_Trail.json"), "w"), indent=2, ensure_ascii=False)


def damage_var(parent, physical, energy=None):
    entry = {"Parent": parent, "DamageCalculator": {"BaseDamage": {"Physical": physical}},
             "DamageEffects": {"WorldSoundEventId": "SFX_Sword_T2_Impact", "LocalSoundEventId": "SFX_Sword_T2_Impact"}}
    if energy:
        entry["EntityStatsOnHit"] = [{"EntityStatId": "SignatureEnergy", "Amount": energy}]
    return {"Interactions": [entry]}


NAME_DRAWN = NAME + "_Degainee"
SHEATH = "Artefact_Tenryumaru_Fourreau"
SHEATH_EMPTY = SHEATH + "_Vide"
DESCRIPTION = ("Relique du Clan Luo : le katana de Luo Tenryū, fondateur du clan, forgé d'un croc de son père le Dieu-Dragon d'Or. "
               "Seul le Patriarche des Luo peut la manier, et elle lui revient toujours. "
               "Tenue au fourreau dans la main gauche ; au premier coup, la main droite dégaine et tranche d'un seul geste, "
               "et la lame reste au clair tant que l'on frappe. Chaque coup d'épée projette une lame d'or. "
               "Clic droit : bref, rengainer ; maintenu, garde. "
               "Maintenir l'attaque, Flash Céleste : posture iai, puis le Patriarche fond sur chaque ennemi alentour comme un éclair "
               "d'or, le tranche au passage, revient et rengaine ; au claquement de la garde, chaque ennemi touché reçoit dix slashs "
               "célestes à la fois (recharge 8 s). "
               "Signature, les Dix Serments : dix reflets du Patriarche encerclent la zone visée et y exécutent chacun le Flash Céleste.")


def sound(effects, world="SFX_Sword_T2_Swing", local="SFX_Sword_T2_Swing_RL_Local"):
    effects["WorldSoundEventId"], effects["LocalSoundEventId"] = world, local
    return effects


def step(anim, run, **extra):
    e = {"ItemAnimationId": anim}
    e.update(extra)
    return {"Type": "Simple", "RunTime": run, "HorizontalSpeedMultiplier": 0.75, "Effects": sound(e)}


def spell(name, run=0.05):
    return {"Type": "RuneSpell", "RunTime": run, "Spell": name}


SELECTOR_RIGHT = {"Type": "Replace", "Var": "Swing_Right_Selector", "DefaultOk": True,
                  "DefaultValue": {"Interactions": ["Weapon_Sword_Primary_Swing_Right_Selector"]}}
SHEATHE_SWAP = spell("TenryumaruDegaine", 0.0)                # l'objet au fourreau devient l'objet degaine (Java)
IGNORE = {"OnItemChangeBehavior": "Ignore"}
# combo Luo (interactions Luo_* de gen_luo_katanas.py), choisi par la variable Luo_Combo (Hien : version rapide)
LUO_COMBO = {"Type": "Replace", "Var": "Luo_Combo", "DefaultOk": True, "DefaultValue": {"Interactions": ["Luo_Combo"]}}


def ignoring(it):
    it = copy.deepcopy(it)
    it.update(IGNORE)
    return it


def write_interactions():
    d = os.path.join(RES, "Server", "Item", "Interactions", "Weapons", KEY)
    rd = os.path.join(RES, "Server", "Item", "RootInteractions", "Weapons", KEY)
    for old in (os.path.join(d, KEY + "_Signature.json"), os.path.join(rd, "Root_%s_Signature.json" % KEY)):
        if os.path.exists(old):
            os.remove(old)
    os.makedirs(d, exist_ok=True)
    os.makedirs(rd, exist_ok=True)
    files = {}
    iai_charge = {"ItemAnimationId": "IaiCharging", "LocalSoundEventId": "SFX_Sword_T2_Lunge_Charge_Local",
                  "Particles": [{"SystemId": "Sword_Charging", "TargetNodeName": "Saya-Handle", "PositionOffset": {"X": 0.4},
                                 "RotationOffset": {"Roll": 90}}]}
    def flash(from_sheath):
        """Flash Celeste (charge pleine) : degaine foudroyant (coupe + lame d'or), puis l'eclair d'or (Java FlashCeleste)."""
        seq = [{"Type": "Simple", "RunTime": 0.08,
                "Effects": {"ItemAnimationId": "IaiCharged", "WorldSoundEventId": "SFX_Ability_Wind_Strike_Slash",
                            "LocalSoundEventId": "SFX_Sword_T2_Lunge_Local", "CameraEffect": "Sword_Stab"}}]
        if from_sheath:
            seq.append(SHEATHE_SWAP)                             # la main est deja sur la poignee : la lame apparait dans le fourreau
        seq += [SELECTOR_RIGHT, spell("KiEpeeCharge", 0.0), spell("FlashCeleste", 0.05), {"Type": "Simple", "RunTime": 0.2}]
        return [seq[0]] + [ignoring(x) for x in seq[1:]]

    # ----- objet au fourreau -----
    files["Tenryumaru_Degainer"] = {
        "$Comment": "Clic, lame au fourreau : nukitsuke. La main droite atteint la poignee (0,05 s), l'objet devient alors l'objet "
                    "degaine (Java TenryumaruDegaine : la lame apparait dans la main, sur la poignee, et le fourreau passe a vide), "
                    "puis la lame sort et tranche vers la droite. Degats : variable Swing_Right_Damage.",
        "Type": "Serial", "Interactions": [step("Degainer", 0.05), SHEATHE_SWAP, ignoring(spell("LuoDegaineMouvement", 0.0)),
                                           ignoring({"Type": "Simple", "RunTime": 0.1}), ignoring(SELECTOR_RIGHT),
                                           ignoring({"Type": "Simple", "RunTime": 0.12})]}
    files["Tenryumaru_Fourreau_Primary"] = {
        "$Comment": "Attaque, lame au fourreau : clic = Tenryumaru_Degainer ; maintien = posture iai (Tenryumaru_Iai_Fourreau).",
        "Type": "Charging", "Rules": {"InterruptedBy": ["Secondary"]}, "DisplayProgress": False,
        "Next": {"0": "Tenryumaru_Degainer", "0.174": "Tenryumaru_Iai_Fourreau"}}
    files["Tenryumaru_Iai_Fourreau"] = {
        "$Comment": "Posture iai, lame au fourreau : relache avant la fin = degaine simple, charge pleine = Flash Celeste.",
        "Type": "Charging", "Rules": {"InterruptedBy": ["Secondary"]}, "DisplayProgress": True, "HorizontalSpeedMultiplier": 0.45,
        "Effects": copy.deepcopy(iai_charge),
        "Next": {"0": "Tenryumaru_Degainer", "0.75": "Tenryumaru_Flash_Fourreau"}}
    files["Tenryumaru_Flash_Fourreau"] = {
        "$Comment": "Flash Celeste depuis le fourreau (l'objet devient l'objet degaine des le degaine).",
        "Type": "Serial", "Interactions": flash(True)}
    # ----- objet degaine : combo vanilla de l'epee, maintien remplace (variable Thrust) -----
    files["Tenryumaru_Iai_Degainee"] = {
        "$Comment": "Maintien, lame au clair (variable Thrust de l'epee) : la lame revient au fourreau, posture iai ; relache avant "
                    "la fin = combo normal, charge pleine = Flash Celeste.",
        "Type": "Charging", "Rules": {"InterruptedBy": ["Secondary"]}, "DisplayProgress": True, "HorizontalSpeedMultiplier": 0.45,
        "Effects": copy.deepcopy(iai_charge),
        "Next": {"0": LUO_COMBO, "0.75": "Tenryumaru_Flash"}}
    files["Tenryumaru_Flash"] = {"$Comment": "Flash Celeste, lame deja au clair.", "Type": "Serial", "Interactions": flash(False)}

    # ----- signature : les Dix Serments (Java DixSerments), meme interaction pour les deux formes -----
    files["Tenryumaru_Signature"] = {
        "$Comment": "Signature de Tenryumaru, les Dix Serments (energie pleine) : tout le sort est en Java (DixSerments).",
        "Type": "StatsCondition", "Costs": {"SignatureEnergy": 100}, "ValueType": "Percent",
        "Next": {"Type": "Serial", "Interactions": [
            spell("DixSerments", 0.05),
            {"Type": "ChangeStat", "StatModifiers": {"SignatureEnergy": -100}, "ValueType": "Percent"}]}}
    for old in ("Tenryumaru_Rafale", "Tenryumaru_Rafale_Fourreau", "Tenryumaru_Signature_Fourreau"):
        if os.path.exists(os.path.join(d, old + ".json")):
            os.remove(os.path.join(d, old + ".json"))
    if os.path.exists(os.path.join(rd, "Root_Tenryumaru_Signature_Fourreau.json")):
        os.remove(os.path.join(rd, "Root_Tenryumaru_Signature_Fourreau.json"))
    files["Tenryumaru_Secondary_Degainee"] = {
        "$Comment": "Clic droit, lame au clair : clic bref = rengainer (Java TenryumaruRengaine : noto puis objet au fourreau), "
                    "maintien = garde de l'epee vanilla.",
        "Type": "Charging", "DisplayProgress": False,
        "Next": {"0": {"Type": "Serial", "Interactions": [spell("TenryumaruRengaine", 0.05)]},
                 "0.2": "Weapon_Sword_Secondary_Guard"}}
    for old in ("Tenryumaru_Premier_Serment", "Tenryumaru_Premier_Serment_Fourreau"):
        if os.path.exists(os.path.join(d, old + ".json")):
            os.remove(os.path.join(d, old + ".json"))

    for name, data in files.items():
        json.dump(data, open(os.path.join(d, name + ".json"), "w"), indent=2, ensure_ascii=False)
    roots = {"Root_Tenryumaru_Fourreau_Primary": {"RequireNewClick": True, "ClickQueuingTimeout": 0.2, "Cooldown": {"Cooldown": 0.25},
                                                  "Interactions": ["Tenryumaru_Fourreau_Primary"]},
             "Root_Tenryumaru_Secondary_Degainee": {"RequireNewClick": True, "Interactions": ["Tenryumaru_Secondary_Degainee"],
                                                    "Tags": {"Attack": ["Melee"]}},
             "Root_Tenryumaru_Signature": {"RequireNewClick": True, "Interactions": ["Tenryumaru_Signature"]}}
    for name, data in roots.items():
        json.dump(data, open(os.path.join(rd, name + ".json"), "w"), indent=2)


def write_item():
    variables = {
        # energie de signature par coup (vanilla : 1, 1, 3) : la signature se remplit en 5 a 6 coups
        "Swing_Left_Damage": damage_var("Weapon_Sword_Primary_Swing_Left_Damage", 40, 3),
        "Swing_Right_Damage": damage_var("Weapon_Sword_Primary_Swing_Right_Damage", 45, 3),
        "Swing_Down_Damage": damage_var("Weapon_Sword_Primary_Swing_Down_Damage", 70, 5),
        "Thrust": {"Interactions": ["Tenryumaru_Iai_Degainee"]},
        # chaque coup d'epee projette une lame d'or inclinee comme le geste
        "Swing_Left_Selector": {"Interactions": [{"Type": "Serial", "Interactions": [
            spell("TenryuCoupGauche", 0.0), "Weapon_Sword_Primary_Swing_Left_Selector"]}]},
        "Swing_Right_Selector": {"Interactions": [{"Type": "Serial", "Interactions": [
            spell("TenryuCoupDroit", 0.0), "Weapon_Sword_Primary_Swing_Right_Selector"]}]},
        "Swing_Down_Selector": {"Interactions": [{"Type": "Serial", "Interactions": [
            spell("TenryuCoupVertical", 0.0), "Weapon_Sword_Primary_Swing_Down_Selector"]}]},
        "Guard_Wield": {"Interactions": [{"Parent": "Weapon_Sword_Secondary_Guard_Wield",
                                          "StaminaCost": {"Value": 10, "CostType": "Damage"}}]},
    }
    base = {
        "Parent": "Template_Weapon_Sword",
        "TranslationProperties": {"Name": "Tenryūmaru, Lame du Premier Serment", "Description": DESCRIPTION},
        "Quality": "Legendary",
        "ItemLevel": 550,
        "Model": MODEL_REL + "/" + NAME + ".blockymodel",
        "Texture": MODEL_REL + "/" + NAME + ".png",
        "Icon": "Icons/ItemsGenerated/" + NAME + ".png",
        "IconProperties": {"Scale": 0.33, "Rotation": [45, 90, 0], "Translation": [-33, -38]},
        "MaxStack": 1,
        "Variant": False,
        "InteractionVars": variables,
        "MaxDurability": 0,
        "DurabilityLossOnHit": 0,
    }
    sheathed = dict(copy.deepcopy(base),
                    **{"$Comment": "Tenryumaru au fourreau (main gauche). Au premier coup, Java (TenryumaruDegaine) la remplace par "
                                   + NAME_DRAWN + " ; apres un temps sans attaquer, TenryumaruSystem la rengaine. Genere par "
                                   "scripts/artefacts/gen_tenryumaru.py.",
                       "PlayerAnimationsId": "Tenryumaru_Fourreau",
                       "Model": MODEL_REL + "/" + NAME + "_Main_Vide.blockymodel",
                       "Categories": [familles.category("Luo"), "Items.Weapons"],
                       "Interactions": {"Primary": "Root_Tenryumaru_Fourreau_Primary",
                                        "Secondary": "Root_Weapon_Sword_Secondary_Guard",
                                        "Ability1": "Root_Tenryumaru_Signature"}})
    drawn = dict(copy.deepcopy(base),
                 **{"$Comment": "Tenryumaru au clair (main droite), fourreau dans la main gauche : objet de passage, hors menu creatif. "
                                "Rendu au fourreau (" + NAME + ") par TenryumaruSystem. Genere par scripts/artefacts/gen_tenryumaru.py.",
                    "PlayerAnimationsId": "Tenryumaru_Degainee",
                    "Interactions": {"Primary": "Root_Luo_Primary",
                                     "Secondary": "Root_Tenryumaru_Secondary_Degainee",
                                     "Ability1": "Root_Tenryumaru_Signature"},
                    "Trails": [{"TrailId": KEY + "_Trail", "TargetNodeName": "Handle", "TargetEntityPart": "PrimaryItem",
                                "PositionOffset": {"X": 0.9, "Y": 0.05}, "RotationOffset": {"Roll": 90}}]})
    for name, data in ((NAME, sheathed), (NAME_DRAWN, drawn)):
        json.dump(data, open(os.path.join(RES, "Server", "Item", "Items", "Weapon", "Sword", name + ".json"), "w"), indent=2, ensure_ascii=False)
    for name, full in ((SHEATH, True), (SHEATH_EMPTY, False)):
        sheath = {"$Comment": "Fourreau de Tenryumaru, objet utilitaire pose dans la main gauche par TenryumaruSystem tant que le katana "
                              "est en main (" + ("lame rengainee" if full else "lame au clair : monture cachee") + "), retire ensuite. "
                              "Genere par scripts/artefacts/gen_tenryumaru.py.",
                  "TranslationProperties": {"Name": "Fourreau de Tenryūmaru",
                                            "Description": "Le fourreau de laque noire et d'or de la relique du Clan Luo. Il accompagne la lame et disparaît sans elle."},
                  "Quality": "Legendary", "ItemLevel": 550,
                  "Model": MODEL_REL + "/" + name + ".blockymodel", "Texture": MODEL_REL + "/" + NAME + ".png",
                  "Icon": "Icons/ItemsGenerated/" + SHEATH + ".png",
                  "IconProperties": {"Scale": 0.33, "Rotation": [45, 90, 0], "Translation": [-33, -38]},
                  "MaxStack": 1, "Variant": False, "Utility": {"Usable": True},
                  "DropOnDeath": False,
                  # objet utilitaire utilisable : c'est LUI qui recoit le clic droit (vu en jeu : garde muette sinon)
                  "PlayerAnimationsId": "Tenryumaru_Fourreau" if full else "Tenryumaru_Degainee",
                  "Interactions": {"Secondary": "Root_Weapon_Sword_Secondary_Guard" if full else "Root_Tenryumaru_Secondary_Degainee"},
                  "InteractionVars": {"Guard_Wield": copy.deepcopy(variables["Guard_Wield"])}}
        json.dump(sheath, open(os.path.join(RES, "Server", "Item", "Items", "Weapon", "Sword", name + ".json"), "w"), indent=2, ensure_ascii=False)
    write_interactions()


# ---------------------------------------------------------------------------------------------------------------------
# Lames d'air d'or (signature) et slash du Premier Serment : la lame de la Windstrike vanilla (arc horizontal face a +-Z),
# teintee d'or, en quatre inclinaisons (roulis autour de z, l'axe du vol). Deplacees par le code (Tenryumaru.java).
# ---------------------------------------------------------------------------------------------------------------------
AIR = "Tenryumaru_Lame"
AIR_RAMP = ((150, 92, 10), (255, 248, 214))
AIR_ROLLS = {"H": 0, "G": 32, "D": -32, "V": 90}


def write_air_blades():
    import tempfile, zipfile
    z = zipfile.ZipFile(K.ASSETS)
    base = json.loads(z.read("Common/Items/Projectiles/WindStrike_Projectile.blockymodel"))
    with tempfile.NamedTemporaryFile(suffix=".png", delete=False) as f:
        f.write(z.read("Common/Items/Projectiles/WindStrike_Projectile_Texture.png"))
    w, h, px = png.read(f.name)
    os.unlink(f.name)
    lo, hi = AIR_RAMP
    for row in px:
        for c in row:
            lum = ((0.3 * c[0] + 0.59 * c[1] + 0.11 * c[2]) / 255) ** 3.0
            c[0], c[1], c[2] = [int(lo[i] + (hi[i] - lo[i]) * lum) for i in range(3)]
    out_dir = os.path.join(RES, "Common", "Items", "Projectiles")
    os.makedirs(out_dir, exist_ok=True)
    png.write(os.path.join(out_dir, AIR + ".png"), w, h, px)

    z_trail = json.loads(z.read("Server/Entity/Trails/Abilities/Ability_Windstrike_Trail.json"))
    t = copy.deepcopy(z_trail)
    t["Start"]["Color"], t["End"]["Color"] = "rgba(#fff1c2, 0.95)", "rgba(#ffb02e, 0)"
    json.dump(t, open(os.path.join(RES, "Server", "Entity", "Trails", AIR + "_Trail.json"), "w"), indent=2)
    neutral = {"Interactions": [{"Type": "Simple", "RunTime": 0.01}]}
    for key, roll in AIR_ROLLS.items():
        ids = iter(range(1000, 2000))

        def renum(n):
            n["id"] = str(next(ids))
            for c in n.get("children", []):
                renum(c)
        children = copy.deepcopy(base["nodes"][0]["children"])
        for c in children:
            renum(c)
        node = {"id": str(next(ids)), "name": "Lame", "position": {"x": 0, "y": 0, "z": 0},
                "orientation": K.round_q(K.q_axis((0, 0, 1), roll)), "shape": empty(False), "children": children}
        model = copy.deepcopy(base)
        model["nodes"][0]["children"] = [node]
        name = AIR + "_" + key
        json.dump(model, open(os.path.join(out_dir, name + ".blockymodel"), "w"), indent=1)
        asset = {"$Comment": "Lame d'air d'or de Tenryumaru (signature et Premier Serment), deplacee par le code. "
                             "Genere par scripts/artefacts/gen_tenryumaru.py.",
                 "Model": "Items/Projectiles/" + name + ".blockymodel", "Texture": "Items/Projectiles/" + AIR + ".png",
                 "MinScale": 1.0, "MaxScale": 1.0,
                 "HitBox": {"Max": {"X": 1.2, "Y": 1.2, "Z": 0.6}, "Min": {"X": -1.2, "Y": -1.2, "Z": -0.6}},
                 "Trails": [{"TrailId": AIR + "_Trail", "TargetNodeName": "Lame", "PositionOffset": {"X": x, "Z": 0.5}} for x in (1.0, -1.0)],
                 "Light": {"Color": "#ffd27a", "Radius": 6}}
        json.dump(asset, open(os.path.join(RES, "Server", "Models", "Projectiles", "Abilities", name + ".json"), "w"), indent=2, ensure_ascii=False)
        config = {"$Comment": "Lame d'air d'or de Tenryumaru, deplacee par le code. Interactions neutres obligatoires.",
                  "Model": name, "UseModelScale": True,
                  "Physics": {"Type": "Standard", "Gravity": 0, "TerminalVelocityAir": 1, "TerminalVelocityWater": 1,
                              "Bounciness": 0, "SticksVertically": False},
                  "LaunchForce": 0, "SpawnOffset": {"X": 0, "Y": 0, "Z": 0},
                  "Interactions": {"ProjectileHit": neutral, "ProjectileMiss": neutral}}
        json.dump(config, open(os.path.join(RES, "Server", "ProjectileConfigs", "Abilities", "Projectile_Config_" + name + ".json"), "w"),
                  indent=2, ensure_ascii=False)


def preview_poses(model, tex, files, out_dir):
    """Planches de controle : chaque animation propre a quelques images, de face, de trois quarts et de profil."""
    import pose_kit as P
    import tenryumaru_anims as A
    shots = [("Fourreau/Garde_Fourreau", (0,)), ("Degainer", (0, 4, 8, 11, 15, 22)), ("Iai_Charging_Fourreau", (0, 18)),
             ("Iai_Charged", (0, 3, 6, 10, 18)), ("Rengainer", (0, 12, 20, 26, 36)), ("Garde_Fourreau_Parade", (0,)),
             ("Degainee/Idle", (0,)), ("Degainee/Attacks/Swing_Left/Swing_Left", (0, 12)),
             ("TsukiPosture", (0, 10, 90)), ("Tsuki", (0, 4, 8, 18, 32)), ("TsukiDouble", (6, 11, 16)), ("TsukiRuee", (5, 26)),
             ("TsukiRafale", (3, 6, 40)), ("TsukiProfond", (8, 44)), ("TsukiCharge", (0,)), ("TsukiCeleste", (0, 8, 30))]
    for name, times in shots:
        anim = files[A.DST + name + ".blockyanim"]
        imgs = []
        for t in times:
            vis = P.visible_at(anim, t)
            hidden = {n for n, v in vis.items() if v is False}
            shown = {n for n, v in vis.items() if v is True}
            m = copy.deepcopy(model)

            def walk(n):
                if n["name"] in shown:
                    n["shape"]["visible"] = True
                for c in n["children"]:
                    walk(c)
            for r in m["nodes"]:
                walk(r)
            for yaw, pitch in ((20, 10), (90, 5), (-60, 25)):
                imgs.append(P.render(P.sample(anim, t), [(m, tex)], 240, yaw, pitch, fit=1.05, hidden=hidden))
        png.write(os.path.join(out_dir, "pose_" + name.replace("/", "_") + ".png"), 240 * 3, 240 * len(times), P.sheet(imgs, 3))


def main():
    import tenryumaru_anims as A
    root, handle = build()
    fix_coplanar(handle, KEY)
    saya = build_saya(handle, A.SAYA_POS, A.SAYA_Q)
    A.Names.drawn = shape_names(root)
    A.Names.hilt = [n for n in shape_names(saya) if not n.startswith(("Saya-Seg", "Saya-Kojiri", "Saya-Koiguchi", "Saya-Bague",
                                                                         "Saya-Kurikata", "Saya-Sageo"))]
    # la lame reste VISIBLE dans le modele : le jeu ne reaffiche pas une forme cachee dans le modele (vu en jeu le 2026-11-06),
    # ce sont les animations qui la cachent (comme la fleche de l'arc vanilla)
    model_dir = os.path.join(RES, "Common", MODEL_REL)
    os.makedirs(model_dir, exist_ok=True)
    W, H, tex = M.texture()
    png.write(os.path.join(model_dir, NAME + ".png"), W, H, tex)
    model = {"nodes": [root, saya], "lod": "auto"}                # modele complet : apercus seulement
    json.dump({"nodes": [root], "lod": "auto"}, open(os.path.join(model_dir, NAME + ".blockymodel"), "w"), indent=1)
    # objet au fourreau : rien dans la main droite (le jeu n'applique pas shapeVisible des animations aux objets tenus)
    json.dump({"nodes": [{"id": "1", "name": "R-Attachment", "position": K.vec((0, 0, 0)), "orientation": dict(K.ID_Q),
                          "shape": empty(True), "children": []}], "lod": "auto"},
              open(os.path.join(model_dir, NAME + "_Main_Vide.blockymodel"), "w"), indent=1)
    # fourreau : objet utilitaire (main gauche). Plein = monture visible (lame rengainee), vide = monture cachee par defaut
    json.dump({"nodes": [saya], "lod": "auto"}, open(os.path.join(model_dir, SHEATH + ".blockymodel"), "w"), indent=1)
    empty_saya = copy.deepcopy(saya)

    def hide_hilt(n):
        if n["name"] in A.Names.hilt:
            n["shape"]["visible"] = False
        for c in n["children"]:
            hide_hilt(c)
    hide_hilt(empty_saya)
    json.dump({"nodes": [empty_saya], "lod": "auto"}, open(os.path.join(model_dir, SHEATH_EMPTY + ".blockymodel"), "w"), indent=1)
    json.dump({"nodes": [display_model(root, saya)], "lod": "auto"},
              open(os.path.join(model_dir, NAME + "_Presentoir.blockymodel"), "w"), indent=1)
    print("texture %dx%d" % (W, H))

    icon, sheath_icon = make_icons(handle, saya, tex)
    png.write(os.path.join(RES, "Common", "Icons", "ItemsGenerated", NAME + ".png"), 64, 64, icon)
    png.write(os.path.join(RES, "Common", "Icons", "ItemsGenerated", SHEATH + ".png"), 64, 64, sheath_icon)

    files, errors = A.build_sets(RES)
    for k, v in errors.items():
        print("IK %-22s %s" % (k, " ".join("%.1f" % e for e in v)))
    print(len(files), "animations")

    if PREVIEW:
        os.makedirs(PREVIEW, exist_ok=True)
        png.write(os.path.join(PREVIEW, "tenryumaru_face.png"), 1024, 1024, K.draw(handle_quads(handle, tex, K.ID_Q), 1024, 0, 0, margin=0.03))
        png.write(os.path.join(PREVIEW, "tenryumaru_icon_x4.png"), 256, 256, [[icon[y // 4][x // 4] for x in range(256)] for y in range(256)])
        png.write(os.path.join(PREVIEW, "fourreau_icon_x4.png"), 256, 256, [[sheath_icon[y // 4][x // 4] for x in range(256)] for y in range(256)])
        preview_poses(model, tex, files, PREVIEW)

    write_trail()
    write_item()
    write_air_blades()
    print("ok")


if __name__ == "__main__":
    main()
