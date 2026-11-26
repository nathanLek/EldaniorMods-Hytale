"""Tenue du forgeron legendaire humain, Tetsugen Luo, le Marteau du Dragon (guards/LegendarySmiths, Clan Luo).

  Forgeron_Luo  Legendaire  niv. 600  tenugui gris de suie noue, au mon du clan ; samue indigo pique de sashiko, manches
                                       courtes, tasuki cramoisie croisee dans le dos, kote de cuir aux avant-bras ; grand
                                       tablier de cuir brule (maekake) du torse aux genoux, frappe du mon d'or ; obi de corde,
                                       marteau a la hanche droite, tenailles a gauche ; gants de cuir noircis ; momohiki
                                       indigo, genouilleres de cuir, jika-tabi.

Memes briques que gen_kimono_luo (2 px par unite). IDs Armor_Forgeron_Luo_<Piece>, modeles sous
Items/Armors/Forgerons/Forgeron_Luo. Seule tenue de forgeron gardee (2026-11-16) : celles du nain et de l elfe ont ete supprimees.

Usage : python3 scripts/armures/gen_forgeron_luo.py <src/main/resources> [dossier_apercu]
"""
import gen_kimono as gk
import gen_kimono_luo as luo
from armor_kit import *  # noqa: F401,F403
from armor_kit import write_sets, coplanar_report

RES, PREVIEW = gk.RES, gk.PREVIEW
AI, AI_LIGHT, KURO = luo.AI, luo.AI_LIGHT, luo.KURO
SOOT, SOOT_LIGHT = (92, 90, 92), (150, 146, 140)
HIDE, HIDE_DARK, HIDE_LIGHT = (96, 58, 34), (48, 28, 18), (140, 92, 56)
IRON, IRON_DARK = (110, 112, 120), (52, 54, 60)
HAIR, HAIR_LIGHT = (24, 20, 22), (64, 56, 58)
STONE = (126, 128, 134)
STITCH = luo.pat("juji", (110, 122, 170), 0.3)


def samue(opening=False, mon=()):
    return luo.fabric(AI, motifs=[STITCH, luo.gradient(0.1)], mon=mon, opening=[(1.2, AI_LIGHT), (40, KURO)] if opening else None)


def hide(mon=(), dark=False):
    """Cuir epais, brule par endroits, coutures claires sur les bords."""
    def paint(face, w, h, seed):
        img = cloth(w, h, HIDE_DARK if dark else HIDE, seed, 0.1, 0.06)
        for k in range(max(2, w * h // 90)):                              # brulures
            x, y = (seed * 31 + k * 17) % max(1, w), (seed * 13 + k * 29) % max(1, h)
            put(img, x, y, HIDE_DARK)
        if face in ("front", "back"):
            stitch_line(img, [(1, 1), (w - 2, 1)], HIDE_LIGHT, 2)
            stitch_line(img, [(1, h - 2), (w - 2, h - 2)], HIDE_LIGHT, 2)
        return finish(img, face, edges=False)
    if mon:
        base = paint

        def with_mon(face, w, h, seed):
            img = base(face, w, h, seed)
            for side, fx, fy, r, metal_c in mon:
                if face == side:
                    luo.kamon(img, w * fx, h * fy, r, HIDE_DARK, metal_c)
            return img
        return with_mon
    return paint


def tenugui(face, w, h, seed):
    img = cloth(w, h, SOOT, seed, 0.08, 0.03)
    if face not in ("top", "bottom"):
        luo.wrap_folds(mix(SOOT, (0, 0, 0), 0.3), SOOT_LIGHT, 6, 0.6)(img)
    return finish(img, face)


def build_smith_head():
    m = Model("Head")
    kids = [box(m, "Tenugui", (0, 4.5, 0), (31.5, 4.5, 29.6), tenugui),
            luo.mon_quad(m, "Mon", (0, 4.5, 15.1), 7, SOOT),
            box(m, "Noeud", (0, 4.5, -16), (6, 5, 3), tenugui)]
    kids += sym(m, plank(m, "R-Queue", (-3.5, -10, -19.6), (-1.5, 3, -16.8), 3, 0.6, tenugui))
    hair = luo.fabric(HAIR, motifs=[luo.wrap_folds(HAIR, HAIR_LIGHT, 3, 0.4)])
    kids += [box(m, "Chignon_Base", (0, 16.8, -4), (8, 2.4, 10), hair),             # chonmage : chignon replie sur le crane
             box(m, "Chignon", (0, 18.6, -1.5), (4.5, 2.6, 12), hair),
             box(m, "Chignon_Lien", (0, 18.6, -6.5), (5, 3, 1.4), plain(luo.CRIMSON)),
             box(m, "Barbiche", (0, -12.6, 16.4), (6, 4, 1.4), hair),
             box(m, "Moustache", (0, -8.6, 16.6), (11, 1.6, 1.2), hair)]
    return m, [piece(m, "Head", (0, 0, 0), kids)]


def build_smith_chest():
    m = Model("Chest")
    leather = hide()

    def arm():
        sode = box(m, "R-Sode", (-1, 4, -1), (12.5, 13, 15.5), samue())
        kote = box(m, "R-Kote", (-1, -6, -1), (11.5, 12, 14.5), hide(dark=True))
        lace = box(m, "R-Kote_Lacet", (-1, -1.4, -1), (12, 1.2, 15), plain(luo.CRIMSON))
        pad = box(m, "R-Epauliere", (-1.5, 11.4, -1), (14, 2.4, 17), hide(dark=True))       # cuir contre la chaleur du foyer
        pad_rim = box(m, "R-Epauliere_Bord", (-1.5, 9.8, -1), (14.4, 1.2, 17.4), plain(HIDE_LIGHT))
        return piece(m, "R-Arm", (-17, 0, 0), [sode, kote, lace, pad, pad_rim])

    def thigh():
        flap = box(m, "R-Tablier_Pan", (-0.5, 0, 12.6), (15.5, 18, 1.4), leather)
        return piece(m, "R-Thigh", (-8, -1, 1), [flap], offset=(0, -9, 0))

    right_arm, right_thigh = arm(), thigh()
    plate = box(m, "Torse", (0, 0, 0), (32, 24, 23), samue(opening=True))
    bib = box(m, "Tablier_Haut", (0, -1, 12.3), (22, 20, 1.4), hide(mon=[("front", 0.5, 0.45, 8, luo.GOLDS)]))
    straps = [rod(m, "Bretelle_A", (-10, 9.5, 12.2), (-14, 12, -11.6), 1.4, plain(HIDE_DARK)),
              rod(m, "Bretelle_B", (10, 9.5, 12.2), (14, 12, -11.6), 1.4, plain(HIDE_DARK))]
    tasuki = plain(luo.CRIMSON)
    cross = [rod(m, "Tasuki_A", (-13, 11, -12.4), (13, -11, -12.4), 1.4, tasuki),
             rod(m, "Tasuki_B", (13, 11, -12.4), (-13, -11, -12.4), 1.4, tasuki),
             box(m, "Tasuki_Noeud", (0, 0, -13.1), (3.4, 3.4, 1.8), tasuki)]
    chest = piece(m, "Chest", (0, 5, 0), [plate, bib] + straps + cross
                  + gk.neck_band(m, plain(AI_LIGHT), 12.1) + [right_arm, mirror(m, right_arm)], offset=(0, 11, 0))
    belly = box(m, "Ventre", (0, 0, 0), (30, 16, 22), samue())
    apron = box(m, "Tablier", (0, -1, 12.4), (27, 16, 1.4), leather)
    rope = box(m, "Obi_Corde", (0, -2.5, 0), (34, 3, 26.8), straw_rope())
    hammer = [rod(m, "Marteau_Manche", (15.4, -3, 4), (15.4, -16, 6), 1.6, plain((120, 84, 50))),
              box(m, "Marteau_Tete", (15.4, -16.9, 6.2), (2.6, 3.2, 7.5), metal(IRON)),
              box(m, "Marteau_Frappe", (15.4, -16.9, 10.4), (3.2, 3.6, 1.2), metal(IRON_DARK))]
    tongs = [rod(m, "Tenailles_A", (-15.6, -3, 3), (-15.6, -16, 1.5), 0.9, metal(IRON_DARK)),
             rod(m, "Tenailles_B", (-15.6, -3, 5), (-15.6, -16, 6.5), 0.9, metal(IRON_DARK)),
             box(m, "Tenailles_Pivot", (-15.6, -6, 4), (1.6, 1.6, 2.6), metal(IRON))]
    pouch = [box(m, "Bourse_Outils", (-8, -6.5, 13.8), (6.5, 6, 2.6), hide(dark=True)),
             box(m, "Bourse_Rabat", (-8, -4.2, 15.2), (6.9, 2, 0.8), plain(HIDE_LIGHT)),
             box(m, "Pierre_Aiguiser", (7.5, -7.5, 13.9), (3, 6, 2), plain(STONE)),
             rod(m, "Pierre_Cordon", (7.5, -4.5, 13.9), (8.5, -1.5, 13.6), 0.6, plain(luo.CRIMSON))]
    node = piece(m, "Belly", (0, 8, 0), [belly, apron, rope] + hammer + tongs + pouch + [chest], offset=(0, 4, 0))
    hip = box(m, "Hanches", (0, -0.5, 0), (31, 12, 23.6), samue())
    hip_apron = box(m, "Tablier_Bas", (0, -0.5, 12.5), (28, 12.6, 1.4), leather)
    return m, [piece(m, "Pelvis", (0, 12, 0), [hip, hip_apron, right_thigh, mirror(m, right_thigh), node])]


def straw_rope():
    def paint(face, w, h, seed):
        img = cloth(w, h, STRAW_DARK, seed, 0.05, 0.03)
        for x in range(0, w, 3):
            for y in range(h):
                if (x + y) % 3 == 0:
                    put(img, x, y, STRAW)
        return finish(img, face, edges=False)
    return paint


def build_smith_hands():
    m = Model("Hands")
    cuff = box(m, "R-Manchette", (0, -2, 0), (11, 11, 15), hide(dark=True))
    rim = box(m, "R-Manchette_Bord", (0, 3.6, 0), (11.6, 1.4, 15.6), plain(HIDE_LIGHT))
    glove = box(m, "R-Gant", (0, 1.5, 0), (11.2, 7, 15.2), hide(dark=True))
    soot = box(m, "R-Gant_Paume", (0, 3.6, 6.6), (11.6, 2.4, 2.4), plain((30, 26, 24)))
    hand = piece(m, "R-Hand", (0, -8, 0), [glove, soot], offset=(0, -5, 0))
    right = piece(m, "R-Forearm", (-18, 40, 0), [cuff, rim, hand])
    return m, [right, mirror(m, right)]


def build_smith_legs():
    m = Model("Legs")
    pants = samue()
    upper = box(m, "R-Momohiki", (-0.3, 0, 0), (14.5, 23, 17.5), pants)
    lower = box(m, "R-Momohiki_Bas", (-0.3, -2, 0), (14, 21, 17.6), pants)
    knee = box(m, "R-Genouillere", (-0.3, 6, 8.9), (11, 8, 1.6), hide(dark=True))
    calf = piece(m, "R-Calf", (0, -12, 3), [lower, knee], offset=(0, -12, -3))
    thigh = piece(m, "R-Thigh", (-8, -1, 1), [upper, calf], offset=(0, -9, 0))
    boot = box(m, "R-Tabi", (0, 1, 0), (15, 8, 21), luo.jika_tabi)
    sole = box(m, "R-Semelle", (-0.4, -3.4, 0), (15.6, 1.4, 22), plain(KURO))
    foot = piece(m, "R-Foot", (-7.85, 7, -3), [boot, sole], offset=(0, -3, 6))
    waist = box(m, "Koshi", (0, 0.25, 0), (33.5, 12.5, 26), pants)
    return m, [piece(m, "Pelvis", (0, 51, 0), [waist, thigh, mirror(m, thigh)]), foot, mirror(m, foot)]


INTRO = "Tenue de Tetsugen Luo, le Marteau du Dragon, forgeron légendaire du Clan Luo."
SETS = {
    "Forgeron_Luo": {
        "prefix": "Armor_Forgeron_Luo_", "quality": "Legendary", "level": 600, "durability": 420,
        "category": "Eldanior_System.Armures",
        "build": {"Head": build_smith_head, "Chest": build_smith_chest, "Hands": build_smith_hands, "Legs": build_smith_legs},
        "pieces": {
            "Head": ("Tenugui du Marteau du Dragon", INTRO + " Un tenugui gris de suie noué derrière la tête, au mon du clan.", 18, 26, 0.08),
            "Chest": ("Samue du Marteau du Dragon", INTRO + " Un samue indigo piqué de sashiko, une tasuki cramoisie, un grand tablier de cuir brûlé frappé du mon d'or, le marteau et les tenailles à la ceinture.", 34, 48, 0.15),
            "Hands": ("Gants du Marteau du Dragon", INTRO + " Des gants de cuir épais, noircis par mille trempes.", 14, 20, 0.066),
            "Legs": ("Momohiki du Marteau du Dragon", INTRO + " Un momohiki indigo, des genouillères de cuir et des jika-tabi.", 26, 36, 0.11),
        },
    },
}

if __name__ == "__main__":
    reports = write_sets(RES, PREVIEW, "Items/Armors/Forgerons", "Forgerons", SETS, "forgeron", family_tag="Cloth_Kimono", closeup_fit=6.0)
    for set_name, nodes in reports.items():
        issues = coplanar_report(nodes)
        if issues:
            print(set_name, "faces coplanaires :", len(issues))
            for line in issues[:16]:
                print("   ", line)
