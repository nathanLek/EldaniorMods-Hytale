"""Familles nobles et leurs armoiries, partagees par les portes du tresor et les presentoirs de famille.
Les ecus sont dessines en vectoriel (blason.py) : plus px est grand, plus le trait reste net a grande taille.
"""
import math

import blason

GOLD, GOLD_LIGHT, GOLD_DARK = (232, 182, 78), (255, 234, 164), (138, 92, 34)
LINE = (24, 18, 16)

# Categorie creative "Famille" (Server/Item/Category/CreativeLibrary/Eldanior_Familles.json, scripts/familles/gen_categorie.py) :
# tout objet lie a une famille va dans sa sous-categorie, et nulle part ailleurs dans Eldanior System.
CATEGORY = "Eldanior_Familles"

# Categorie creative "Empire Luo" (2026-11-11, Eldanior_Empire_Luo.json, meme generateur) : les cinq clans de l'Empire
# (deplaces hors de "Famille") et quatre pages communes. category(fid) choisit la bonne categorie, les generateurs n'ont
# rien d'autre a savoir.
EMPIRE = "Eldanior_Empire_Luo"
EMPIRE_CLANS = ("Luo", "Kage", "Setsuka", "Sama", "Hanazakura")
EMPIRE_GROUPS = (
    ("Civils", "Civils"),                    # tenues, katanas et plaque hors clan (villageois, marchands, ronins, cour...)
    ("Comestibles", "Comestibles"),          # thes, onguents
    ("ArtsMartiaux", "Arts Martiaux"),       # voie martiale : Rouleau, manuels des techniques de classe, Bokken
    ("Runes", "Runes"),                      # runes de type Luo (techniques)
)


def category(fid):
    """fid = une famille (FAMILIES) ou une page commune de l'Empire Luo (EMPIRE_GROUPS)."""
    if fid in EMPIRE_CLANS or fid in dict(EMPIRE_GROUPS):
        return EMPIRE + "." + fid
    return CATEGORY + "." + fid


# Tenues de famille, par prefixe d'ID (armor_kit.item_json) : le plus long prefixe gagne.
ARMOR_PREFIXES = {
    "Armor_Eldanior_Roi_": "Eldanior",
    "Armor_Runkandel_": "Runkandel",
    "Armor_Kimono_": "Luo",            # kimonos du Clan Luo (Vagabond, Empereur, Dieu-Dragon, Recrue, Seigneur...)
    # tenues des quatre clans de l'Empire Luo (2026-11-10) : membres et kimono de Doyen
    # Sama : tenue pugiliste propre depuis le 2026-11-10 (la Garde d'Honneur redevient une tenue du Clan Luo)
    "Armor_Kimono_Sama_": "Sama", "Armor_Kimono_Noble_Cramoisi_": "Sama",
    "Armor_Kimono_Noble_Violet_": "Hanazakura",
    "Armor_Kimono_Ombre_": "Kage", "Armor_Kimono_Noble_": "Kage",
    "Armor_Kimono_Setsuka_": "Setsuka", "Armor_Kimono_Noble_Jade_": "Setsuka",
    # Garde Royale de l'Empereur et Alchimiste royal (2026-11-10) : tenues du Clan Luo
    "Armor_Kimono_Garde_Royale_": "Luo", "Armor_Kimono_Alchimiste_": "Luo",
    # civils de l'Empire Luo (2026-11-11) : villageois, Vagabond, ronins, marchands, pelerins, nobles de cour et lettres
    "Armor_Kimono_Pauvre_": "Civils", "Armor_Kimono_Rizieres_": "Civils", "Armor_Kimono_Ronin_": "Civils",
    "Armor_Kimono_Marchand_": "Civils", "Armor_Kimono_Negociant_": "Civils", "Armor_Kimono_Pelerin_": "Civils",
    "Armor_Kimono_Cour_": "Civils", "Armor_Kimono_Lettre_": "Civils",
}
# prefixes exacts : Sakura (Armor_Kimono_<piece>) = tenue des membres du Clan Hanazakura
ARMOR_EXACT = {"Armor_Kimono_": "Hanazakura"}


def armor_category(prefix):
    if prefix in ARMOR_EXACT:
        return category(ARMOR_EXACT[prefix])
    for p in sorted(ARMOR_PREFIXES, key=len, reverse=True):
        if prefix.startswith(p):
            return category(ARMOR_PREFIXES[p])
    return None


# id, nom, couleur du champ et de la cle, charge du blason
FAMILIES = [
    ("Eldanior", "Eldanior", (74, 36, 120), "couronne"),
    ("Zippel", "Zippel", (44, 48, 132), "etoile"),
    ("Runkandel", "Runkandel", (132, 22, 30), "dragon"),
    ("Luo", "Luo", (22, 20, 26), "dragon_soleil"),
    # les quatre grands clans de l'Empire Luo (2026-11-10 ; Kage = ex-Nighthollow, Hanazakura = ex-Swiftquiver)
    ("Sama", "Sama", (132, 22, 36), "soleil"),
    ("Hanazakura", "Hanazakura", (70, 36, 104), "fleur"),
    ("Kage", "Kage", (14, 12, 18), "shuriken"),
    ("Setsuka", "Setsuka", (34, 46, 92), "croissant"),
    ("Valmontis", "Valmontis", (24, 96, 66), "besant"),
    ("Ironveil", "Ironveil", (78, 86, 100), "tour"),
    ("Warbane", "Warbane", (96, 30, 22), "epees"),
    ("Frostguard", "Frostguard", (58, 128, 164), "flocon"),
    ("Spellweave", "Spellweave", (98, 40, 140), "hexagramme"),
    ("Goldcrest", "Goldcrest", (146, 96, 24), "losange"),
    ("Silkroad", "Silkroad", (20, 104, 112), "vagues"),
]
def regular(n, r, start=-90.0, cx=0.0, cy=0.05):
    return [(cx + r * math.cos(math.radians(start + k * 360.0 / n)), cy + r * math.sin(math.radians(start + k * 360.0 / n))) for k in range(n)]


def starry(n, outer, inner, cy=0.05):
    pts = []
    for k in range(n * 2):
        r, a = (outer if k % 2 == 0 else inner), math.radians(-90 + k * 180.0 / n)
        pts.append((r * math.cos(a), cy + r * math.sin(a)))
    return pts


SUN_BLACK = (10, 9, 12)


def coiled_dragon(cv):
    """Clan Luo : dragon d'or enroule autour d'un soleil noir, la tete en haut tournee vers lui."""
    w = 0.03
    cv.fill(starry(12, 0.58, 0.4), GOLD_DARK, LINE, w)              # couronne du soleil
    cv.fill(regular(36, 0.4), GOLD, LINE, w)
    cv.fill(regular(36, 0.33), SUN_BLACK)
    n, a0, a1 = 60, -40.0, 245.0                                    # queue en haut a droite, tete en haut a gauche
    spine = []
    for k in range(n + 1):
        t = k / float(n)
        a = math.radians(a0 + (a1 - a0) * t)
        r = 0.72 + 0.045 * math.sin(t * math.pi * 7)                 # ondulation du corps
        spine.append((r * math.cos(a), 0.05 + r * math.sin(a), 0.025 + 0.1 * min(1.0, t * 1.6), a))
    for color, extra in ((LINE, 0.032), (GOLD, 0.0)):
        for (u0, v0, r0, _), (u1, v1, _, _) in zip(spine, spine[1:]):
            cv.line([(u0, v0), (u1, v1)], color, r0 + extra)
    for k, (u, v, r, a) in enumerate(spine[:-1]):
        if k % 3 == 1 and k > 6:                                   # nageoire dorsale, vers l'exterieur
            ou, ov = math.cos(a), math.sin(a)
            tu, tv = -math.sin(a), math.cos(a)
            base = r * 0.9
            cv.fill([(u + ou * base - tu * 0.05, v + ov * base - tv * 0.05), (u + ou * (base + 0.1) - tu * 0.07, v + ov * (base + 0.1) - tv * 0.07),
                     (u + ou * base + tu * 0.03, v + ov * base + tv * 0.03)], GOLD_DARK, LINE, 0.012)
        if k % 2 == 0 and k > 4:                                   # ecailles, cote soleil
            cv.dot(u - math.cos(a) * r * 0.35, v - math.sin(a) * r * 0.35, r * 0.28, GOLD_LIGHT)
    # pointe de queue en fer de lance
    (u0, v0, _, a), (u1, v1, _, _) = spine[1], spine[0]
    du, dv = u1 - u0, v1 - v0
    m = math.hypot(du, dv)
    du, dv = du / m, dv / m
    cv.fill([(u1 - dv * 0.07, v1 + du * 0.07), (u1 + du * 0.16, v1 + dv * 0.16), (u1 + dv * 0.07, v1 - du * 0.07)], GOLD_DARK, LINE, 0.015)
    # tete : dans le sens du corps, inclinee vers le soleil
    u, v, _, a = spine[-1]
    fwd = a + math.pi / 2 + math.radians(30)
    fu, fv = math.cos(fwd), math.sin(fwd)
    su, sv = math.cos(a), math.sin(a)                              # cote exterieur
    S = 1.3

    def at(x, y):
        return (u + (fu * x + su * y) * S, v + (fv * x + sv * y) * S)
    for k in range(4):                                             # criniere
        b = -0.1 - k * 0.06
        cv.fill([at(b + 0.06, 0.06), at(b - 0.1, 0.2 - k * 0.02), at(b - 0.02, 0.02)], GOLD_DARK, LINE, 0.012)
    cv.fill([at(0.0, 0.08), at(-0.26, 0.22), at(-0.06, 0.03)], GOLD_LIGHT, LINE, 0.016)    # corne arriere
    cv.fill([at(0.06, 0.09), at(-0.12, 0.3), at(0.0, 0.06)], GOLD_LIGHT, LINE, 0.016)      # corne avant
    head = [at(-0.1, 0.09), at(0.08, 0.1), at(0.18, 0.06), at(0.32, 0.05), at(0.36, 0.0), at(0.33, -0.04),
            at(0.2, -0.04), at(0.26, -0.09), at(0.08, -0.1), at(-0.1, -0.08)]
    cv.fill(head, GOLD, LINE, 0.022)
    cv.line([at(0.16, -0.035), at(0.34, -0.02)], LINE, 0.01)       # gueule
    cv.line([at(-0.02, 0.07), at(0.16, 0.06)], GOLD_LIGHT, 0.014)  # reflet du chanfrein
    cv.dot(*at(0.08, 0.035), 0.026, (210, 36, 28))                 # oeil
    cv.line([at(0.34, 0.03), at(0.2, 0.2), at(0.02, 0.28)], GOLD_LIGHT, 0.01)   # moustaches au vent
    cv.line([at(0.3, -0.06), at(0.14, -0.2), at(-0.04, -0.2)], GOLD_LIGHT, 0.01)


def charge(cv, kind):
    w = 0.03
    if kind == "dragon_soleil":
        coiled_dragon(cv)
    elif kind == "couronne":
        cv.fill([(-0.66, 0.42), (-0.74, -0.34), (-0.36, 0.02), (0.0, -0.58), (0.36, 0.02), (0.74, -0.34), (0.66, 0.42)], GOLD, LINE, w)
        cv.fill([(-0.68, 0.42), (0.68, 0.42), (0.68, 0.62), (-0.68, 0.62)], GOLD_DARK, LINE, w)
        for u, v in ((-0.74, -0.42), (0.0, -0.66), (0.74, -0.42)):
            cv.dot(u, v, 0.1, GOLD_LIGHT)
    elif kind == "etoile":
        cv.fill(starry(8, 0.8, 0.3), GOLD, LINE, w)
        cv.fill(starry(8, 0.42, 0.16), GOLD_LIGHT)
    elif kind == "soleil":
        cv.fill(starry(16, 0.84, 0.46), GOLD, LINE, w)
        cv.fill(regular(24, 0.42), GOLD_LIGHT, LINE, w)
    elif kind == "besant":
        cv.fill(regular(32, 0.72), GOLD, LINE, w)
        cv.fill(regular(32, 0.56), GOLD_DARK)
        cv.fill(regular(32, 0.5), GOLD_LIGHT)
        cv.fill([(0.0, -0.3), (0.3, 0.05), (0.0, 0.4), (-0.3, 0.05)], GOLD_DARK)
    elif kind == "tour":
        pts = [(-0.5, 0.8), (-0.5, -0.3), (-0.62, -0.3), (-0.62, -0.72), (-0.36, -0.72), (-0.36, -0.5), (-0.13, -0.5), (-0.13, -0.72),
               (0.13, -0.72), (0.13, -0.5), (0.36, -0.5), (0.36, -0.72), (0.62, -0.72), (0.62, -0.3), (0.5, -0.3), (0.5, 0.8)]
        cv.fill(pts, (214, 218, 226), LINE, w)
        cv.fill([(-0.18, 0.8), (-0.18, 0.3), (0.0, 0.1), (0.18, 0.3), (0.18, 0.8)], LINE)
    elif kind == "epees":
        for s in (-1, 1):
            cv.line([(s * 0.66, 0.78), (-s * 0.56, -0.74)], LINE, 0.15)
            cv.line([(s * 0.66, 0.78), (-s * 0.56, -0.74)], (222, 226, 234), 0.1)
            cv.line([(s * 0.3, 0.72), (s * 0.82, 0.3)], GOLD, 0.11)
            cv.line([(s * 0.66, 0.74), (s * 0.82, 0.96)], GOLD_DARK, 0.09)
    elif kind == "flocon":
        for k in range(6):
            a = math.radians(k * 60 - 90)
            cos, sin = math.cos(a), math.sin(a)
            cv.line([(0, 0.05), (0.82 * cos, 0.05 + 0.82 * sin)], (232, 244, 255), 0.075)
            for r, length in ((0.5, 0.24), (0.68, 0.16)):
                bx, by = r * cos, 0.05 + r * sin
                for turn in (-60, 60):
                    t = a + math.radians(turn)
                    cv.line([(bx, by), (bx + length * math.cos(t), by + length * math.sin(t))], (232, 244, 255), 0.055)
    elif kind == "hexagramme":
        for start in (-90, 90):
            pts = regular(3, 0.82, start)
            cv.line(pts + pts[:1], GOLD, 0.075)
        cv.fill(regular(6, 0.2, 0), GOLD_LIGHT)
    elif kind == "croissant":
        outer = [(0.76 * math.cos(math.radians(a)), 0.05 + 0.76 * math.sin(math.radians(a))) for a in range(40, 321, 8)]
        inner = [(0.28 + 0.56 * math.cos(math.radians(a)), 0.05 + 0.56 * math.sin(math.radians(a))) for a in range(290, 69, -8)]
        cv.fill(outer + inner, (214, 220, 236), LINE, w)
        cv.fill(starry(4, 0.2, 0.06), (214, 220, 236))
    elif kind == "fleur":                                            # fleur de cerisier a cinq petales echancres
        for k in range(5):
            a = math.radians(-90 + k * 72)
            cx, cy = 0.4 * math.cos(a), 0.05 + 0.4 * math.sin(a)
            cv.fill(regular(20, 0.34, 0, cx, cy), (246, 200, 220), LINE, w)
            cv.fill([(0.62 * math.cos(a) - 0.06 * math.sin(a), 0.05 + 0.62 * math.sin(a) + 0.06 * math.cos(a)),
                     (0.5 * math.cos(a), 0.05 + 0.5 * math.sin(a)),
                     (0.62 * math.cos(a) + 0.06 * math.sin(a), 0.05 + 0.62 * math.sin(a) - 0.06 * math.cos(a))], (70, 36, 104))
        cv.fill(regular(16, 0.18), GOLD, LINE, w)
        for k in range(5):
            a = math.radians(-54 + k * 72)
            cv.dot(0.26 * math.cos(a), 0.05 + 0.26 * math.sin(a), 0.045, GOLD_LIGHT)
    elif kind == "shuriken":                                         # etoile de lancer a quatre branches, acier
        cv.fill(starry(4, 0.84, 0.24), (196, 200, 212), LINE, w)
        cv.fill(regular(16, 0.13), LINE)
    elif kind == "fleche":
        cv.line([(-0.5, 0.82), (0.46, -0.5)], (232, 214, 170), 0.09)
        cv.fill([(0.68, -0.82), (0.24, -0.62), (0.56, -0.34)], (226, 230, 236), LINE, w)
        for t in (0.0, 0.12, 0.24):
            bx, by = -0.5 + t * 0.7, 0.82 - t * 0.96
            cv.line([(bx, by), (bx - 0.24, by - 0.14)], GOLD, 0.06)
            cv.line([(bx, by), (bx + 0.16, by + 0.2)], GOLD, 0.06)
    elif kind == "losange":
        cv.fill([(0.0, -0.84), (0.66, 0.05), (0.0, 0.94), (-0.66, 0.05)], GOLD, LINE, w)
        cv.fill([(0.0, -0.46), (0.36, 0.05), (0.0, 0.56), (-0.36, 0.05)], GOLD_LIGHT)
    elif kind == "vagues":
        for v0 in (-0.42, 0.05, 0.52):
            pts = [(-0.76 + k * 0.038, v0 + 0.14 * math.sin(k * 0.48)) for k in range(41)]
            cv.line(pts, (236, 226, 196), 0.095)


def arms(color, kind, width, px=48):
    """Ecu de la famille, reduit a width pixels de large. Runkandel garde les armoiries deja dessinees pour ses tenues."""
    if kind == "dragon":
        return blason.arms_sized("capitaine", width, px=px)
    u0, u1, v0, v1 = -1.14, 1.14, -1.3, 1.66
    cv = blason.Canvas(px, u0, u1, v0, v1)
    cv.fill(blason.shield_outline(1.09), LINE)
    cv.fill(blason.shield_outline(1.07), GOLD)
    cv.fill(blason.shield_outline(0.955), GOLD_DARK)
    cv.fill(blason.shield_outline(0.94), tuple(color))
    charge(cv, kind)
    img = cv.img
    return blason.shrink(img, width, int(round(width * len(img) / float(len(img[0])))))
