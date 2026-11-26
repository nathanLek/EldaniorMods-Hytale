"""Armure du Roi d'Eldanior : une tenue de 4 pieces, unique (qualite Unique).

  Heaume couronne   acier blanc poli ouvert sur le visage, couronne fermee du royaume (croix pattees et fleurons d'or,
                    perles, rubis, amethystes et saphirs, arches perlees, bonnet de velours pourpre, globe crucigere)
  Harnois           acier blanc poli, frises de rinceaux d'or sur email pourpre, collier de l'ordre royal et son joyau aux
                    armes d'Eldanior, pelerine d'hermine, grand manteau de velours pourpre seme de couronnes, borde d'hermine
  Gantelets         harnois blanc, revers graves, amethyste au poignet
  Greves            harnois blanc, genouilleres gravees a l'amethyste, solerets

Armoiries d'Eldanior (familles.py) : de pourpre a la couronne d'or. L'or reste place : frises, arches, collier, joyaux.
Reprend les formes du harnois Runkandel (gen_runkandel.py, importe sans rien ecrire) a 4 px par unite.

Usage : python3 scripts/armures/gen_roi.py <src/main/resources> [dossier_apercu]
"""
import math

import gen_runkandel as rk
from armor_kit import (ID_Q, box, canvas, disc, finish, mix, noise, put, q_axis, quad, rect, rod, sym, HIDE_HELMET,
                       coplanar_report, write_sets)
import blason
import familles
from blason import fill as fill_poly, layer

D = rk.D
RES, PREVIEW = rk.RES, rk.PREVIEW
MODEL_DIR = "Items/Armors/Eldanior"

R_STEEL = (194, 200, 214)                  # harnois blanc : acier poli presque argent
R_STEEL_DARK = (84, 92, 112)
R_STEEL_LIGHT = (250, 251, 255)
ENAMEL = (46, 20, 74)                      # email pourpre sous les rinceaux
PURPLE = (74, 36, 120)                     # pourpre d'Eldanior (familles.py)
PURPLE_DARK = (30, 12, 54)
PURPLE_LIGHT = (128, 74, 184)
AMETHYST = (150, 62, 206)
AMETHYST_LIGHT = (236, 196, 255)
SAPPHIRE = (40, 78, 196)
SAPPHIRE_LIGHT = (176, 206, 255)
PEARL = (246, 240, 226)
GOLD_MID, GOLD_SHADE, GOLD_BRIGHT = rk.GOLD_MID, rk.GOLD_SHADE, rk.GOLD_BRIGHT


# ---------------------------------------------------------------------------------------------------------------------
# Peintures
# ---------------------------------------------------------------------------------------------------------------------
def royal_steel(**kw):
    base = dict(c=R_STEEL, dark=R_STEEL_DARK, light=R_STEEL_LIGHT, gold=GOLD_MID, ground=ENAMEL, sheen=0.42, grain=0.04)
    base.update(kw)
    return rk.steel(**base)


K = rk.Harness(R_STEEL, R_STEEL_DARK, R_STEEL_LIGHT, GOLD_MID, ENAMEL, sheen=0.42, grain=0.04)
K.trim = royal_steel(lames=2)              # pas de filet d'or sur chaque lame : l'or reste aux frises
K_GOLD = rk.steel(GOLD_MID, GOLD_SHADE, GOLD_BRIGHT, sheen=0.3)
K_VELVET = rk.velvet(PURPLE, PURPLE_LIGHT, PURPLE_DARK)
AMETHYST_GEM = rk.gem(AMETHYST, AMETHYST_LIGHT)
SAPPHIRE_GEM = rk.gem(SAPPHIRE, SAPPHIRE_LIGHT)
RUBY_GEM = rk.gem()


def cabochon(img, cx, cy, rx, ry, c, light):
    for y in range(int(cy - ry - 1), int(cy + ry + 2)):
        for x in range(int(cx - rx - 1), int(cx + rx + 2)):
            d = math.hypot((x + 0.5 - cx) / rx, (y + 0.5 - cy) / ry)
            if d <= 1.0:
                put(img, x, y, (60, 40, 16) if d > 0.82 else mix(light, tuple(v * 0.6 for v in c), math.hypot((x + 0.5 - cx + rx * 0.3) / rx, (y + 0.5 - cy + ry * 0.35) / ry)))


def pearl(img, cx, cy, r):
    disc(img, cx, cy, r, (150, 140, 120))
    disc(img, cx - 0.2, cy - 0.2, r * 0.75, PEARL)
    put(img, cx - r * 0.4, cy - r * 0.4, (255, 255, 255))


def circlet(face, w, h, seed):
    """Cercle de couronne : or poli, rangs de perles, cabochons alternes rubis, amethyste, saphir."""
    img = rk.steel(GOLD_MID, GOLD_SHADE, GOLD_BRIGHT, sheen=0.3)(face, w, h, seed)
    if face in ("top", "bottom"):
        return img
    rect(img, 0, 0, w, 1, GOLD_SHADE)
    rect(img, 0, h - 1, w, h, GOLD_SHADE)
    for x in range(2, w, 4):
        pearl(img, x + 0.5, 2.5, 1.3)
        pearl(img, x + 0.5, h - 2.5, 1.3)
    gems = ((rk.RUBY, rk.RUBY_LIGHT), (AMETHYST, AMETHYST_LIGHT), (SAPPHIRE, SAPPHIRE_LIGHT))
    for i, x in enumerate(range(8, w - 4, 16)):
        c, light = gems[i % 3]
        cabochon(img, x, h / 2.0, 3.2, 2.6, c, light)
    return img


def pearled(face, w, h, seed):
    """Arche de couronne : or, perles enfilees."""
    img = rk.steel(GOLD_MID, GOLD_SHADE, GOLD_BRIGHT, sheen=0.3)(face, w, h, seed)
    if face in ("top", "bottom"):
        return img
    for y in range(3, h - 1, 5):
        pearl(img, w / 2.0, y, min(w, 6) * 0.36)
    return img


def chain_paint(face, w, h, seed):
    """Collier de l'ordre : maillons d'or alternes, a plat puis de chant."""
    img = canvas(w, h, GOLD_MID)
    for y in range(h):
        flat = (y // 5) % 2 == 0
        for x in range(w):
            if flat:
                t = abs(((y % 5) - 2) / 2.5) + abs((x + 0.5) / w - 0.5)
                img[y][x][:3] = list(mix(GOLD_BRIGHT, GOLD_SHADE, t))
            else:
                img[y][x][:3] = list(mix(GOLD_MID, GOLD_SHADE, abs((x + 0.5) / w - 0.5) * 2))
    return finish(img, face, edges=False)


def crown_art(px=64):
    """Couronne des armes d'Eldanior, seule, pour les gravures."""
    cv = blason.Canvas(px, -0.9, 0.9, -0.8, 0.75)
    familles.charge(cv, "couronne")
    return cv.img


_engr = {}


def crown_engraving(width_px):
    if width_px not in _engr:
        big = crown_art()
        small = blason.shrink(big, width_px, int(round(width_px * len(big) / float(len(big[0])))))
        _engr[width_px] = rk.engraved(small, hatch=False, inlay=ENAMEL)
    return _engr[width_px]


def pauldron_emblem(img, w, h):
    art = crown_engraving(int(min(w * 0.7, h * 0.8 * 1.16)))
    rk.stamp(img, art, (w - len(art[0])) // 2, max(0, (h - len(art)) // 2))


def eldanior_arms(width_px, px=64):
    return familles.arms(PURPLE, "couronne", width_px, px=px)


# ---------------------------------------------------------------------------------------------------------------------
# Heaume couronne
# ---------------------------------------------------------------------------------------------------------------------
def fleur(height=10.0, width=8.0, gem=(rk.RUBY, rk.RUBY_LIGHT)):
    """Fleuron a trois petales, decoupe en alpha, gemme au coeur."""
    W, H = int(width * D), int(height * D)
    img = layer(W, H)
    pts = [(0.5, 0.0), (0.62, 0.3), (0.86, 0.26), (0.72, 0.52), (0.98, 0.58), (0.74, 1.0), (0.26, 1.0), (0.02, 0.58), (0.28, 0.52), (0.14, 0.26), (0.38, 0.3)]
    fill_poly(img, [(x * W, y * H) for x, y in pts], GOLD_MID)
    shade_gold(img)
    cabochon(img, W / 2.0, H * 0.7, W * 0.12, W * 0.12, gem[0], gem[1])
    pearl(img, W / 2.0, 2.0, 1.8)
    return finish(img, "front", edges=False)


def croix_pattee(height=11.0, width=9.0, gem=(AMETHYST, AMETHYST_LIGHT)):
    """Croix pattee posee sur le cercle : quatre bras evases, joyau au centre."""
    W, H = int(width * D), int(height * D)
    img = layer(W, H)
    cx, cy = W / 2.0, H * 0.42
    arm = W * 0.5
    for (dx, dy), length in (((0, -1), cy), ((-1, 0), arm), ((1, 0), arm), ((0, 1), H - cy)):
        px_, py_ = -dy, dx
        narrow, wide = W * 0.07, W * 0.24
        fill_poly(img, [(cx + px_ * narrow, cy + py_ * narrow), (cx + dx * length + px_ * wide, cy + dy * length + py_ * wide),
                        (cx + dx * length - px_ * wide, cy + dy * length - py_ * wide), (cx - px_ * narrow, cy - py_ * narrow)], GOLD_MID)
    shade_gold(img)
    cabochon(img, cx, cy, W * 0.13, W * 0.13, gem[0], gem[1])
    return finish(img, "front", edges=False)


def shade_gold(img):
    H, W = len(img), len(img[0])
    for y in range(H):
        for x in range(W):
            if img[y][x][3]:
                d = abs((x + 0.5) / W - 0.5) * 2
                img[y][x][:3] = list(mix(GOLD_BRIGHT, GOLD_SHADE, d))
                left = x > 0 and img[y][x - 1][3] == 0
                if left or (y > 0 and img[y - 1][x][3] == 0):
                    img[y][x][:3] = list(GOLD_BRIGHT)


def arch(m, name, axis, profile, thick):
    """Arche de couronne : profil (distance au centre, hauteur) de l'avant vers le centre, puis le miroir."""
    pts = profile + [(-d, y) for d, y in reversed(profile[:-1])]
    world = [((0, y, d) if axis == "z" else (d, y, 0)) for d, y in pts]
    return [rod(m, "%s_%d" % (name, i), world[i], world[i + 1], thick, pearled) for i in range(len(world) - 1)]


def build_roi_head():
    m = rk.new_model("Head")
    h = K
    kids = rk.dome_stack(m, h.plain, band=royal_steel(edge="bottom"))
    kids += rk.back_shell(m, h.plain, -4.5, 18, cut=2, x=16, z=15.25)
    kids += sym(m, box(m, "R-Joue", (-16.25, -3.5, 7.75), (1.25, 19, 12.5), royal_steel(edge="bottom")),
                box(m, "R-Charniere", (-17.1, 4, 9), (0.75, 2, 2), K_GOLD))
    kids.append(box(m, "Front_Joyau", (0, 5.25, 16.95), (2.5, 2, 0.6), AMETHYST_GEM, hidden=("back",)))
    # Couronne fermee
    kids += rk.octa(m, "Couronne", (0, 10.5, 0), (35, 4, 34), circlet, 3.5, top=False, bottom=False)
    cross, flower = croix_pattee(), fleur()
    ring = [(0, 17.75, 0.0, cross), (11, 17.75, 0.0, flower), (-11, 17.75, 0.0, flower), (0, -17.75, 180.0, cross),
            (11, -17.75, 180.0, flower), (-11, -17.75, 180.0, flower), (17.75, 0, 90.0, cross), (-17.75, 0, -90.0, cross),
            (17.75, 9, 90.0, flower), (-17.75, 9, -90.0, flower), (17.75, -9, 90.0, flower), (-17.75, -9, -90.0, flower)]
    for i, (x, z, turn, art) in enumerate(ring):
        hgt = (len(art) / float(D)) / 2.0 + 12.0
        kids.append(quad(m, "Fleuron_%d" % i, (x, hgt, z * 0.98 if abs(z) > abs(x) else z), art, rk.yaw(turn)))
    kids += rk.octa(m, "Bonnet_0", (0, 19.25, 0), (29, 5.5, 28), K_VELVET, 6, bottom=False)
    kids += rk.octa(m, "Bonnet_1", (0, 23, 0), (21, 2.5, 20), K_VELVET, 5, bottom=False)
    kids += rk.octa(m, "Bonnet_2", (0, 25.25, 0), (13, 2.5, 12.5), K_VELVET, 3.5, bottom=False)
    profile = [(17.0, 12.25), (16.6, 17.5), (14.5, 22.5), (10.5, 26.0), (5.5, 27.8), (0.0, 27.2)]
    kids += arch(m, "Arche_AvAr", "z", profile, 1.6)
    kids += arch(m, "Arche_Cotes", "x", [(d + 0.3, y) for d, y in profile], 1.45)
    kids += rk.octa(m, "Globe", (0, 28.8, 0), (4.5, 4, 4.5), K_GOLD, 1.5)
    kids.append(box(m, "Globe_Bande", (0, 28.8, 0), (4.75, 0.75, 4.75), SAPPHIRE_GEM))
    kids.append(box(m, "Croix", (0, 33, 0), (1.25, 5, 1.25), K_GOLD, hidden=("bottom",)))
    kids.append(box(m, "Croix_Bras", (0, 33.75, 0), (3.75, 1.25, 1), K_GOLD))
    return m, [rk.piece(m, "Head", (0, 0, 0), kids)]


# ---------------------------------------------------------------------------------------------------------------------
# Harnois et manteau
# ---------------------------------------------------------------------------------------------------------------------
def small_crown(img, x0, y0, c, line):
    pts = [(0, 7), (0, 2), (2.5, 4.2), (5, 0), (7.5, 4.2), (10, 2), (10, 7)]
    rk.poly_img(img, [(x0 + x, y0 + y) for x, y in pts], c)
    rect(img, x0, y0 + 6, x0 + 11, y0 + 8, line)
    for x in (0, 5, 10):
        put(img, x0 + x, y0 + (0 if x == 5 else 1), GOLD_BRIGHT)


def royal_cape():
    """Grand manteau de velours pourpre seme de couronnes brodees, borde d'hermine, armes d'Eldanior sous la pelerine."""
    w, h = 216, 380
    img = K_VELVET("front", w, h, 29)
    for y in range(h):
        k = 1 - 0.3 * y / float(h)
        for x in range(w):
            for i in range(3):
                img[y][x][i] *= k
    side, hem_h = 24, 46
    pelt = rk.ermine("front", side, h, 5)
    for y in range(h):
        for x in range(side):
            img[y][x][:3] = list(pelt[y][x][:3])
            img[y][w - 1 - x][:3] = list(pelt[y][side - 1 - x][:3])
    for x0 in (side, w - side - 3):
        rect(img, x0, 0, x0 + 3, h - hem_h, GOLD_MID)
    rk.rinceau(img, 4, 20, PURPLE_DARK)
    arms = rk.embroider(eldanior_arms(104, px=56))
    ax0, ay0 = (w - len(arms[0])) // 2, 30
    for j, y in enumerate(range(30, h - hem_h - 14, 30)):                    # seme de couronnes, en quinconce
        for x in range(side + 12 + (j % 2) * 20, w - side - 22, 40):
            if ax0 - 14 < x < ax0 + len(arms[0]) + 4 and y < ay0 + len(arms) + 6:
                continue
            small_crown(img, x, y, mix(GOLD_MID, GOLD_SHADE, 0.35), GOLD_SHADE)
    rk.stamp(img, arms, ax0, ay0)
    rect(img, side, h - hem_h - 4, w - side, h - hem_h, GOLD_MID)
    hem = rk.ermine("front", w, hem_h, 9)
    for y in range(hem_h):
        for x in range(w):
            img[h - hem_h + y][x][:3] = list(hem[y][x][:3])
    return img


def grand_pauldron(m, h):
    """Epauliere : coupe a la frise d'or sur email pourpre, couronne gravee au sommet, incrustations, amethyste."""
    out = []
    out += rk.octa(m, "R-Epaule_Haut", (-2.75, 15, 0), (16, 3, 19), royal_steel(emblem=pauldron_emblem, emblem_faces=("top",)), 4.5, bottom=False)
    out += rk.octa(m, "R-Epaule_Coupe", (-2.5, 10.75, 0), (21, 6, 25), h.frieze, 5.5, bottom=False)
    out += rk.octa(m, "R-Epaule_Lame1", (-3.5, 6.25, 0), (20, 3.5, 24), h.plain, 5.5, bottom=False)
    out += rk.octa(m, "R-Epaule_Lame2", (-4, 3, 0), (18.75, 3.5, 22.75), h.plain, 5.5, bottom=False)
    out += rk.octa(m, "R-Epaule_Lame3", (-4.25, -0.25, 0), (17.5, 3.25, 21.5), royal_steel(edge="bottom"), 5.5)
    out.append(box(m, "R-Haute_Piece", (6.25, 15.5, 0), (1.5, 7, 16), royal_steel(edge="top")))
    out.append(box(m, "R-Rondache_Or", (-13.3, 6.25, 0), (0.75, 4, 4), K_GOLD))
    out.append(box(m, "R-Rondache", (-13.85, 6.25, 0), (0.5, 2.5, 2.5), AMETHYST_GEM, hidden=("right",)))
    return out


def build_roi_chest():
    m = rk.new_model("Chest")
    h = K
    arm = rk.plate_arm(m, h, grand_pauldron(m, h))
    thigh = rk.thigh_piece(m, rk.tassets(m, h, royal_steel(edge="bottom")))
    pelvis = rk.fauld(m, h)
    tabard_art = rk.embroider(eldanior_arms(44, px=40))
    pelvis.append(quad(m, "Tabard", (0, -17, 13.25), rk.banner(13, 32, ground=PURPLE, light=PURPLE_LIGHT, dark=PURPLE_DARK, art=tabard_art)))
    breast = royal_steel(edge="bottom", bands=((0.03, 12),), spine=True)
    chest = rk.octa(m, "Cuirasse", (0, 0, 0), (32, 24.5, 25), breast, 4.25, corners=h.plain)
    chest += rk.octa(m, "Gorgerin", (0, 13.4, -0.5), (21.5, 3, 21), h.lames2, 4.5)
    belly = rk.octa(m, "Pansiere", (0, 1, 0), (29.5, 16, 23.5), royal_steel(lames=3), 4.5)
    belly += rk.octa(m, "Ceinture", (0, -6.25, 0), (31, 3.5, 25.25), rk.fine_leather(PURPLE_DARK, GOLD_MID, dark=(14, 6, 26), light=PURPLE), 4.75)
    belly.append(box(m, "Boucle", (0, -6.25, 12.9), (6.5, 5, 1), K_GOLD))
    belly.append(box(m, "Boucle_Gemme", (0, -6.25, 13.6), (2.5, 2.5, 0.5), AMETHYST_GEM, hidden=("back",)))
    chest += rk.octa(m, "Pelerine", (0, 13.5, -1), (36, 10, 29), rk.ermine, 8, bottom=False)
    path = [(-9.0, 9.0, 12.9), (-6.75, 5.75, 13.05), (-3.25, 3.75, 13.15), (0.0, 3.4, 13.2)]
    links = [rod(m, "R-Collier_%d" % i, path[i], path[i + 1], 0.9, chain_paint) for i in range(len(path) - 1)]
    chest += sym(m, *links)
    jewel = rk.harden(eldanior_arms(int(8 * D), px=64))
    chest.append(quad(m, "Joyau_Ordre", (0, 3.4 - len(jewel) / float(D) / 2.0 + 0.6, 13.75), jewel))
    chest += rk.cape_quads(m, royal_cape(), [((0, 17, -15.6), (0, -14, -18.5)), ((0, -14, -18.5), (0, -44, -23.5)), ((0, -44, -23.5), (0, -70, -30))], 38, 52)
    return m, rk.body_chain(m, pelvis, belly, chest, arm, thigh)


def build_roi_hands():
    m = rk.new_model("Hands")
    gem = box(m, "R-Joyau_Poignet", (0, -6.25, 8.95), (2.5, 2.5, 0.6), AMETHYST_GEM, hidden=("back",))
    return m, rk.plate_gauntlet(m, K, cuff_extra=[gem])


def build_roi_legs():
    m = rk.new_model("Legs")
    gem = box(m, "R-Joyau_Genou", (0, 11.5, 8.05), (2, 2, 0.6), AMETHYST_GEM, hidden=("back",))
    return rk.plate_legs(m, K, knee_extra=[gem])


# ---------------------------------------------------------------------------------------------------------------------
# Tenue
# ---------------------------------------------------------------------------------------------------------------------
SETS = {
    "Roi": {
        "prefix": "Armor_Eldanior_Roi_", "quality": "Unique", "level": 600, "durability": 600, "sound": "ISS_Armor_Heavy", "hide": {"Head": HIDE_HELMET},
        "build": {"Head": build_roi_head, "Chest": build_roi_chest, "Hands": build_roi_hands, "Legs": build_roi_legs},
        "pieces": {
            "Head": ("Heaume Couronné d'Eldanior", "Armure du Roi d'Eldanior. Un heaume d'acier blanc ouvert sur le visage, ceint de la couronne fermée du royaume : croix pattées et fleurons d'or, perles, rubis, améthystes et saphirs, sous le globe crucigère.", 34, 14, 0.13),
            "Chest": ("Harnois du Roi d'Eldanior", "Armure du Roi d'Eldanior. Un harnois d'acier blanc aux rinceaux d'or sur émail pourpre, le collier de l'ordre royal et son joyau aux armes du royaume, une pèlerine d'hermine et le grand manteau de velours pourpre semé de couronnes.", 62, 22, 0.23),
            "Hands": ("Gantelets du Roi d'Eldanior", "Armure du Roi d'Eldanior. Des gantelets d'acier blanc aux revers gravés d'or, une améthyste au poignet.", 24, 9, 0.10),
            "Legs": ("Grèves du Roi d'Eldanior", "Armure du Roi d'Eldanior. Des cuissards et des grèves d'acier blanc, des genouillères gravées d'or ornées d'une améthyste.", 44, 16, 0.18),
        },
    },
}

if __name__ == "__main__":
    reports = write_sets(RES, PREVIEW, MODEL_DIR, "Eldanior", SETS, "eldanior", family_tag="Plate_Eldanior", skip_in_icon=("Cape",), preview_fit=5.2, closeup_fit=9.5)
    for set_name, nodes in reports.items():
        found = coplanar_report(nodes)
        print("%s : %d paires coplanaires" % (set_name, len(found)))
        for line in found[:40]:
            print("   ", line)
