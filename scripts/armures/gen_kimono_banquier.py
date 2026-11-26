"""Kimono du Banquier (commercant BANQUIER, guards/NpcTrades) : tenue de la banque du royaume, pas du Clan Luo.

  Banquier  Rare  niv. 150  bonnet laque noir a la piece d'or trouee et lunettes d'or ; haori bleu nuit seme de pieces d'or
                            (zeni) sur kimono ivoire, obi de brocart d'or, grand livre de comptes (daifukucho) et boulier a la
                            ceinture, trousseau de cles de la salle des coffres ; manchettes bleu nuit et chevaliere d'or ;
                            hakama raye gris et or ourle de vagues, tabi blancs et geta laquees.

Pas de mon du clan : la piece d'or trouee fait office d'embleme. Construit avec les briques de gen_kimono_luo (Grand
Negociant), meme densite (2 px par unite). IDs Armor_Banquier_<Piece> (hors des kimonos du clan : pas de categorie Famille,
rachetable, pas d'objet reserve) ; modeles sous Items/Armors/Banquier/Banquier, items sous Armor/Banquier.

Usage : python3 scripts/armures/gen_kimono_banquier.py <src/main/resources> [dossier_apercu]
"""
import gen_kimono as gk
import gen_kimono_luo as luo
from armor_kit import *  # noqa: F401,F403
from armor_kit import write_sets, coplanar_report

RES, PREVIEW = gk.RES, gk.PREVIEW
NAVY, NAVY_LIGHT, NAVY_DARK = (26, 36, 66), (58, 74, 118), (12, 18, 36)
SLATE, SLATE_DARK = (88, 92, 104), (52, 54, 62)
GOLD, GOLD_DARK, GOLD_LIGHT = luo.GOLD, luo.GOLD_DARK, luo.GOLD_LIGHT
IVORY, LACQUER, CRIMSON = luo.IVORY, luo.LACQUER, luo.CRIMSON


def bank_body(ground=NAVY, opening=False, wave=False):
    return luo.fabric(ground, motifs=[luo.ZENI, luo.gradient(0.12)],
                      opening=[(1.0, IVORY), (1.4, GOLD), (40, NAVY_DARK)] if opening else None,
                      wave=(10, NAVY_DARK, GOLD) if wave else None, lining=GOLD if wave else None)


def ledger(face, w, h, seed):
    """Grand livre de comptes : couverture ivoire, reliure cousue sombre a gauche, etiquette de titre."""
    img = canvas(w, h, (226, 214, 184))
    if face in ("front", "back"):
        for x in range(0, 2):
            for y in range(h):
                put(img, x, y, (52, 40, 30))
        for y in range(2, h - 1, 3):
            put(img, 2, y, (52, 40, 30))
        label_w = max(2, w // 3)
        x0 = w // 2 - label_w // 2 + 1
        for x in range(x0, x0 + label_w):
            for y in range(2, h - 2):
                put(img, x, y, (244, 238, 222))
        for y in range(3, h - 3, 2):
            for x in range(x0 + 1, x0 + label_w - 1):
                put(img, x, y, (60, 50, 44))
    else:
        for y in range(h):                                           # tranche : les pages
            for x in range(w):
                put(img, x, y, (238, 230, 210) if (x + y) % 2 else (214, 204, 180))
    return finish(img, face, edges=False)


def build_banker_head():
    m = Model("Head")
    glasses = []
    for side in (-1, 1):
        glasses.append(quad(m, "Lunette" if side > 0 else "R-Lunette", (5.2 * side, -1.5, 14.7), luo.ring_img(12, GOLD, 0.72)))
        glasses.append(rod(m, "Branche" if side > 0 else "R-Branche", (9.4 * side, -1.4, 14.4), (15.3 * side, -1.0, 3.0), 0.6, metal(GOLD)))
    glasses.append(rod(m, "Pont", (-1.6, -1.2, 14.8), (1.6, -1.2, 14.8), 0.6, metal(GOLD)))
    kids = [box(m, "Bonnet", (0, 11.5, 0), (31.5, 8, 31), luo.lacquer_c(LACQUER)),
            box(m, "Bonnet_Haut", (0, 16.8, -0.5), (26, 4.5, 26), luo.lacquer_c(LACQUER)),
            box(m, "Bonnet_Bande", (0, 8.2, 0), (32.2, 1.5, 31.6), metal(GOLD)),
            quad(m, "Piece_Embleme", (0, 12.5, 15.9), luo.ring_img(16, GOLD, 0.34))] + glasses
    return m, [piece(m, "Head", (0, 0, 0), kids)]


def build_banker_chest():
    m = Model("Chest")
    fuki = hem_band(NAVY_DARK, GOLD)

    def arm():
        tamoto = box(m, "R-Tamoto", (-1, -1, -4.2), (14.5, 17, 26), bank_body(wave=True))
        juban = box(m, "R-Juban", (-1, -10.5, 0), (13.5, 3, 15.8), plain(IVORY))
        forearm = piece(m, "R-Forearm", (-1, 0, 0), [tamoto, juban])
        sode = box(m, "R-Sode", (-1, 0.5, -2), (14, 23, 22), bank_body())
        return piece(m, "R-Arm", (-17, 0, 0), [sode, forearm])

    def thigh():
        suso = box(m, "R-Suso", (-1, 0.5, 0), (17, 17, 23), bank_body(wave=True))
        ring = box(m, "R-Fuki_Pan", (-1.6, -8, 0), (18, 2, 24), fuki)
        return piece(m, "R-Thigh", (-8, -1, 1), [suso, ring], offset=(0, -9, 0))

    right_arm, right_thigh = arm(), thigh()
    plate = box(m, "Torse", (0, 0, 0), (33, 24, 24), bank_body(opening=True))
    badge = quad(m, "Piece_Dos", (0, 4, -12.1), luo.ring_img(20, GOLD, 0.34))
    col = stripes([(0.25, GOLD), (0.45, NAVY_DARK), (0.3, IVORY)])
    chest = piece(m, "Chest", (0, 5, 0), [plate, badge] + gk.lapels(m, col, col, 4.5) + gk.neck_band(m, plain(IVORY))
                  + [right_arm, mirror(m, right_arm)], offset=(0, 11, 0))
    belly = box(m, "Ventre", (0, 0, 0), (31, 16, 23), bank_body())
    brocade = luo.fabric(GOLD_DARK, motifs=[luo.pat("kikko", GOLD_LIGHT, 0.4, R=4)])
    obi = box(m, "Obi", (0, -2, 0), (35, 6, 26.5), brocade)
    knot = box(m, "Obi_Noeud", (0, -2, -14.6), (12, 7, 3), brocade)
    book = [box(m, "Daifukucho", (-13.2, -9.8, 14.6), (9, 11, 2.6), ledger),
            rod(m, "Daifukucho_Cordon", (-13.2, -4.3, 14.6), (-12.0, -1, 14.0), 0.6, plain(CRIMSON))]
    abacus = [box(m, "Soroban", (13.6, -9.5, 13.9), (9, 7, 1.4), luo.soroban),
              rod(m, "Soroban_Cordon", (13.6, -6, 13.9), (12.4, -2, 13.6), 0.6, plain(CRIMSON))]
    keys = [rod(m, "Cles_Anneau", (6.8, -3.4, 14.6), (6.8, -5.4, 14.6), 0.7, metal(GOLD))]
    for i, (dx, ln) in enumerate(((-1.2, 4.2), (0.2, 5.0), (1.4, 3.6))):
        keys.append(rod(m, "Cle_%d" % i, (6.8 + dx * 0.4, -5.6, 14.7), (6.8 + dx, -5.6 - ln, 14.9), 0.6, metal(GOLD)))
        keys.append(box(m, "Cle_%d_Panneton" % i, (6.8 + dx + 0.5, -5.6 - ln + 0.6, 14.9), (1.2, 1.0, 0.6), metal(GOLD_DARK)))
    node = piece(m, "Belly", (0, 8, 0), [belly, obi, knot] + book + abacus + keys + [chest], offset=(0, 4, 0))
    hip = box(m, "Hanches", (0, -0.5, 0), (34.5, 13, 26), bank_body())
    return m, [piece(m, "Pelvis", (0, 12, 0), [hip, right_thigh, mirror(m, right_thigh), node])]


def build_banker_hands():
    m = Model("Hands")
    cover = box(m, "R-Manchette", (0, -2.5, 0), (10.5, 12, 14.5), bank_body(NAVY_DARK))
    ring = box(m, "R-Manchette_Bord", (0, 3.2, 0), (11, 1.4, 15), metal(GOLD))
    glove = box(m, "R-Gant", (0, 1.5, 0), (11, 7, 15), plain(IVORY))
    signet = box(m, "R-Chevaliere", (0, -1.8, 0), (11.4, 1.2, 15.4), metal(GOLD))
    hand = piece(m, "R-Hand", (0, -8, 0), [glove, signet], offset=(0, -5, 0))
    right = piece(m, "R-Forearm", (-18, 40, 0), [cover, ring, hand])
    return m, [right, mirror(m, right)]


def build_banker_legs():
    m = Model("Legs")
    hak = luo.striped_hakama(SLATE_DARK, GOLD_DARK, SLATE, period=8, wave=(8, NAVY_DARK, GOLD))
    upper = box(m, "R-Hakama", (-0.3, 0, 0), (14.5, 23, 17.5), hak)
    lower = box(m, "R-Hakama_Bas", (-0.5, -2, 0), (15.5, 21, 18.5), hak)
    calf = piece(m, "R-Calf", (0, -12, 3), [lower], offset=(0, -12, -3))
    thigh = piece(m, "R-Thigh", (-8, -1, 1), [upper, calf], offset=(0, -9, 0))
    sock = box(m, "R-Tabi", (0, 1, 0), (15, 7, 21), luo.white_tabi)
    sole = box(m, "R-Geta", (-0.6, -3.2, 0), (16, 1.6, 23), luo.lacquer_c(LACQUER))
    teeth = [box(m, "R-Geta_Ha_%d" % i, (-0.6, -4.8, z), (15, 1.6, 2.2), luo.lacquer_c(LACQUER)) for i, z in enumerate((-6, 6))]
    foot = piece(m, "R-Foot", (-7.85, 7, -3), [sock, sole] + teeth + gk.thong(m, CRIMSON), offset=(0, -3, 6))
    waist = box(m, "Koshi", (0, 0.25, 0), (33.5, 12.5, 26), hak)
    himo = luo.front_himo(m, 13.4, plain(GOLD_DARK))
    return m, [piece(m, "Pelvis", (0, 51, 0), [waist] + himo + [thigh, mirror(m, thigh)]), foot, mirror(m, foot)]


INTRO = "Tenue des Banquiers du royaume d'Eldanior."
SETS = {
    "Banquier": {
        "prefix": "Armor_Banquier_", "quality": "Rare", "level": 150, "durability": 150,
        "category": "Eldanior_System.Armures",
        "build": {"Head": build_banker_head, "Chest": build_banker_chest, "Hands": build_banker_hands, "Legs": build_banker_legs},
        "pieces": {
            "Head": ("Bonnet du Banquier", INTRO + " Un bonnet de laque noire cerclé d'or, frappé d'une pièce d'or trouée, et des lunettes cerclées d'or pour vérifier chaque compte.", 9, 12, 0.046),
            "Chest": ("Haori du Banquier", INTRO + " Un haori bleu nuit semé de pièces d'or sur un kimono ivoire, un obi de brocart, le grand livre de comptes, un boulier et le trousseau de la salle des coffres.", 17, 22, 0.086),
            "Hands": ("Manchettes du Banquier", INTRO + " Des manchettes bleu nuit bordées d'or, des gants ivoire et la chevalière qui scelle les contrats.", 7, 9, 0.035),
            "Legs": ("Hakama du Banquier", INTRO + " Un hakama rayé de gris et d'or, ourlé de vagues, des tabi blancs et des geta de laque.", 12, 17, 0.065),
        },
    },
}

if __name__ == "__main__":
    reports = write_sets(RES, PREVIEW, "Items/Armors/Banquier", "Banquier", SETS, "banquier", family_tag="Cloth_Kimono", closeup_fit=6.0)
    for set_name, nodes in reports.items():
        issues = coplanar_report(nodes)
        if issues:
            print(set_name, "faces coplanaires :", len(issues))
            for line in issues[:16]:
                print("   ", line)
