"""Illustrations propres a chaque classe (2026-11-26) pour les cartes de la statue des classes et de l'accueil.

Meme rendu vitrail que les cartes de l'accueil (scripts/accueil/gen_accueil_ui.py, outils de gen_chapitres_ui.py) :
une illustration = un DECOR + un PERSONNAGE (tenue, arme) + des EFFETS, decrits par une fiche par classe (FICHES, plus bas).

Sorties (Common/UI/Custom/Accueil/Classes/) :
    Art_<id>.png                          illustration, en DEMI-resolution (167x302) : l'atlas d'interface du client est limite
    Art_<id>C.ui / Art_<id>G.ui           petit document que accueil/CardsPage ajoute dans #AccArtSlot<i> (G : meme texture teintee)
Les 5 classes de base reprennent l'illustration de leur famille (../ChapArt<Famille>*.png) : seuls leurs .ui sont ecrits.
Une classe sans fiche garde l'illustration de sa famille (ClassCards.ownArt ne trouve pas ses .ui).

Usage : python3 scripts/classes/gen_class_art.py [ids...] [--ui] [--planche=/chemin.png] [--manquantes]
    ids          : seulement ces classes (defaut : toutes les fiches)
    --ui         : seulement les documents .ui
    --planche=   : planche de controle (vignettes) des classes demandees
    --manquantes : liste les classes du catalogue sans fiche
"""
import json, math, os, random, sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, '..', 'rune_aura_icons'))
sys.path.insert(0, os.path.join(HERE, '..', 'quetes'))
_argv = sys.argv
sys.argv = [sys.argv[0]]                       # gen_chapitres_ui lit sys.argv a l'import
import gen_chapitres_ui as g                   # noqa: E402
sys.argv = _argv
from gen_chapitres_ui import AW, AH, hexc, mix, vgrad, ridge, seg_dist, inpoly, _person, blank   # noqa: E402
import png                                     # noqa: E402

RES = os.path.join(HERE, "..", "..", "src", "main", "resources")
OUT = os.path.join(RES, "Common", "UI", "Custom", "Accueil", "Classes")
CATALOG = os.path.join(HERE, "classes.json")
ART_W, ART_H = 324, 586                        # ancre de l'illustration dans la carte (gen_chapitres_ui.ART_W/H)
BASE_ART = {"warrior": "Guerrier", "mage": "Mage", "archer": "Archer", "assassin": "Assassin", "merchant": "Marchand"}

H = {}                                         # cache hexc


def C(s):
    if s not in H: H[s] = hexc(s)
    return H[s]


# =====================================================================================================================
# DECORS : f(x, y, u, v, rnd) -> couleur
# =====================================================================================================================

def _disc(col, x, y, cx, cy, r, core, halo, k=0.5, edge=None):
    d = math.hypot(x - cx, y - cy)
    if d < r: return mix(C(core), C(edge), d / r) if edge else C(core)
    if d < r * 2.2: return mix(col, C(halo), (r * 2.2 - d) / (r * 1.2) * k)
    return col


def _stars(col, x, y, rnd, ymax, p=0.993, c="e8eeff"):
    if y < ymax and rnd(int(x), int(y)) > p: return C(c)
    return col


def bg_rempart(x, y, u, v, rnd):
    col = vgrad([(0, "2a1830"), (0.35, "8a2c3a"), (0.6, "f08a4b"), (1, "f08a4b")], v)
    col = _disc(col, x, y, 167, 300, 60, "ffd98a", "ff9a5a", 0.4)
    if y > 330 and (y < 352 and int(x / 16) % 2 == 0 or y >= 352): col = C("3a2530")
    if y > 470: col = mix(C("5a4a40"), C("2e2420"), (y - 470) / 134)
    return col


def bg_champ(x, y, u, v, rnd):
    """Champ de bataille : ciel de fumee rouge, bannieres dechirees, lances plantees."""
    col = vgrad([(0, "1e1418"), (0.4, "6a2a22"), (0.65, "c0583a"), (1, "c0583a")], v)
    if y > ridge(u, 0.62, 0.03, 6, 14, 1.2) * AH: col = C("3a2420")
    for bx, top, c in ((40, 250, "8a1c26"), (300, 230, "2c4373"), (250, 300, "8a1c26")):
        if abs(x - bx) < 1.8 and top < y < top + 200: col = C("2a1a14")
        if bx + 2 < x < bx + 40 and top < y < top + 46 - (x - bx) * 0.25: col = C(c)
    for lx in (20, 70, 280, 315):
        if seg_dist(x, y, lx, 470, lx + 8, 330) < 1.6: col = C("2a1a14")
    smoke = 0.5 + 0.5 * math.sin(u * 9 + v * 4) * math.sin(u * 3 - v * 7)
    if y < 380: col = mix(col, C("3a2a2a"), smoke * 0.25)
    if y > 470: col = mix(C("4a3428"), C("241812"), (y - 470) / 134)
    return col


def bg_arene(x, y, u, v, rnd):
    """Arene : gradins en arches, sable, velum, soleil de midi."""
    col = vgrad([(0, "f6d79a"), (0.3, "e8b76a"), (1, "e8b76a")], v)
    if 120 < y < 360:
        col = C("b8895a")
        for row, (y0, y1) in enumerate(((130, 230), (240, 350))):
            if y0 < y < y1:
                ax = (x + row * 22) % 44
                if math.hypot(ax - 22, y - (y0 + 34)) < 16 or (abs(ax - 22) < 16 and y > y0 + 34): col = C("5a3a22")
            if abs(y - y1) < 4: col = C("8a6440")
    if y < 120 and int(x / 34) % 2 == 0 and y < 40 + (x % 34): col = C("b8322c")       # velum
    if y >= 360: col = mix(C("e4c48a"), C("c49a5e"), (y - 360) / 244)
    if y > 360 and rnd(int(x / 3), int(y / 3)) > 0.97: col = C("b88a52")
    return col


def bg_cathedrale(x, y, u, v, rnd):
    """Cathedrale : nef sombre, grand vitrail en ogive, rais de lumiere doree."""
    col = vgrad([(0, "1a1622"), (1, "2e2632")], v)
    if inpoly(x, y, [(97, 330), (97, 140), (167, 40), (237, 140), (237, 330)]):
        cell = (int(x / 18) + int(y / 22)) % 4
        col = C(("e0b84a", "2c5fa8", "b8322c", "f2e6c4")[cell])
        if x % 18 < 2 or y % 22 < 2: col = C("1a1622")
    for px in (40, 294):
        if abs(x - px) < 16: col = mix(C("4a4250"), C("2a2430"), 0.4 if abs(x - px) > 10 else 0)
    if (x * 0.4 - y * 0.25) % 60 < 18 and y > 120: col = mix(col, C("ffe9a8"), 0.18)
    if y > 470:
        col = C("4a4250") if (int(x / 30) + int(y / 30)) % 2 else C("2e2834")
    return col


def bg_foret(x, y, u, v, rnd):
    col = vgrad([(0, "cfe8c0"), (0.5, "8fc07a"), (1, "4f7f3c")], v)
    for tx, w in ((30, 26), (300, 30), (80, 16), (250, 18)):
        if abs(x - tx) < w / 2: col = mix(C("4a3420"), C("2e2014"), 0.5 if abs(x - tx) > w / 3 else 0)
    for tx, base, w in ((60, 230, 70), (276, 210, 80), (150, 120, 50)):
        if inpoly(x, y, [(tx - w, base), (tx, base - 150), (tx + w, base)]): col = C("2f5a2c")
    if (x * 0.6 + y) % 90 < 22 and y < 420: col = mix(col, C("fff4c4"), 0.22)
    if y > 440: col = mix(C("5a8a3c"), C("33521f"), (y - 440) / 164)
    return col


def bg_foret_nuit(x, y, u, v, rnd):
    col = vgrad([(0, "0a1418"), (0.5, "16302a"), (1, "0c1a14")], v)
    for tx, w in ((24, 30), (310, 34), (90, 14), (240, 16)):
        if abs(x - tx) < w / 2: col = C("070d0a")
    for tx, base, w in ((50, 250, 80), (286, 230, 86), (167, 140, 60)):
        if inpoly(x, y, [(tx - w, base), (tx, base - 170), (tx + w, base)]): col = C("0d221a")
    if rnd(int(x / 2), int(y / 2)) > 0.9965 and 120 < y < 460: col = C("d8ff8a")          # lucioles
    fog = math.exp(-((v - 0.8) / 0.06) ** 2)
    col = mix(col, C("5a8a6a"), fog * 0.35)
    if y > 470: col = C("08120c")
    return col


def bg_nuit(x, y, u, v, rnd):
    col = vgrad([(0, "070b1d"), (0.55, "1f2a5a"), (1, "2a1f4a")], v)
    col = _stars(col, x, y, rnd, 330)
    col = _disc(col, x, y, 250, 110, 30, "e9efff", "8fa6e0", 0.35)
    if y > ridge(u, 0.62, 0.03, 5, 12, 1.4) * AH: col = C("141a36")
    return col


def bg_montagne(x, y, u, v, rnd):
    col = vgrad([(0, "a8c8e8"), (0.5, "dceaf6"), (1, "f2f6fa")], v)
    for base, amp, c, cap in ((0.45, 0.12, "7a8aa8", "f2f6fa"), (0.58, 0.07, "5a6a88", "dce6f0")):
        top = ridge(u, base, amp, 7, 15, 0.9) * AH
        if y > top:
            col = C(cap) if y < top + 30 else C(c)
    if y > 470: col = mix(C("e6eef6"), C("b8c8da"), (y - 470) / 134)
    return col


def bg_glace(x, y, u, v, rnd):
    """Grotte de glace : stalactites bleues, lueur froide."""
    col = vgrad([(0, "0c1c34"), (0.5, "1f4a7a"), (1, "123050")], v)
    for k in range(9):
        sx = 18 + k * 38
        ln = 60 + (k * 37) % 70
        if inpoly(x, y, [(sx - 14, 0), (sx + 14, 0), (sx, ln)]): col = C("8fd0ff" if k % 2 else "5aa0e0")
    for k in range(5):
        sx = 30 + k * 70
        if inpoly(x, y, [(sx - 22, 480), (sx + 22, 480), (sx + 4, 400 - (k * 19) % 40)]): col = C("6ab0f0")
    if y > 470: col = mix(C("cfe8ff"), C("8fb8e0"), (y - 470) / 134)
    return col


def bg_volcan(x, y, u, v, rnd):
    col = vgrad([(0, "1a0a0a"), (0.4, "6a1a10"), (0.7, "d0461a"), (1, "d0461a")], v)
    smoke = (math.sin(u * 8 + v * 5) * math.sin(u * 3 - v * 6) + 1) / 2
    if y < 200: col = mix(col, C("2a1414"), smoke * 0.35)
    if inpoly(x, y, [(110, 360), (228, 170), (288, 170), (334, 250), (334, 360)]):
        col = C("2a1410")
        for sx, slope in ((240, -0.55), (276, 0.4), (258, 0.05)):                          # coulees de lave
            if abs(x - (sx + (y - 170) * slope + math.sin(y * 0.08) * 5)) < 3.5: col = C("ff8a2a")
    if 228 < x < 288 and 154 < y < 176: col = C("ffb040")
    col = _disc(col, x, y, 258, 150, 16, "ffd080", "ff7a2a", 0.5)
    if y > ridge(u, 0.6, 0.02, 5, 11, 0.7) * AH: col = mix(C("3a1410"), C("1a0808"), (y - 360) / 244)
    if y > 470 and abs(math.sin(x * 0.05) * 30 + 520 - y) < 3: col = C("ff7a2a")
    return col


def bg_marais(x, y, u, v, rnd):
    col = vgrad([(0, "1a2a20"), (0.5, "3a5a3a"), (1, "1e2e1e")], v)
    for tx in (40, 290):
        if abs(x - tx - (y - 300) * 0.05) < 7 and y > 200: col = C("101810")
    for (x0, y0, x1, y1) in ((40, 260, 0, 200), (40, 280, 90, 230), (290, 250, 334, 210), (290, 270, 250, 220)):
        if seg_dist(x, y, x0, y0, x1, y1) < 3: col = C("101810")
    if y > 450: col = mix(C("2a3a2a"), C("142014"), (y - 450) / 154)
    fog = math.exp(-((v - 0.76) / 0.07) ** 2) * (0.6 + 0.3 * math.sin(u * 11))
    return mix(col, C("8ab88a"), fog * 0.4)


def bg_cimetiere(x, y, u, v, rnd):
    col = vgrad([(0, "070b1d"), (0.45, "18234d"), (0.7, "24346a"), (1, "24346a")], v)
    col = _stars(col, x, y, rnd, 330, 0.996)
    col = _disc(col, x, y, 220, 120, 28, "e9efff", "8fa6e0", 0.35)
    if y > ridge(u, 0.6, 0.04, 7, 15, 0.8) * AH: col = C("131b36")
    for cx, top, wd in ((40, 390, 30), (300, 400, 26), (80, 420, 20), (262, 425, 22)):
        if abs(x - cx) < wd / 2 and y > top: col = C("3a4660")
        if math.hypot(x - cx, y - top) < wd / 2 and y <= top: col = C("3a4660")
    if y > 470: col = C("0c1224")
    fog = math.exp(-((v - 0.78) / 0.05) ** 2) * (0.55 + 0.25 * math.sin(u * 13))
    return mix(col, C("4fae7c"), fog * 0.45)


def bg_toits(x, y, u, v, rnd):
    col = vgrad([(0, "12081a"), (0.5, "3a1428"), (1, "1a0c18")], v)
    col = _disc(col, x, y, 167, 200, 80, "ff6a5a", "8a2030", 0.5, edge="c9302c")
    col = _stars(col, x, y, rnd, 380, 0.995, "ffd0d0")
    for x0, x1, top in ((0, 120, 400), (214, 334, 380), (100, 230, 430)):
        if x0 <= x <= x1 and y > top + abs(x - (x0 + x1) / 2) * 0.35: col = C("0c0610")
    if y > 470: col = C("08040a")
    return col


def bg_ruelle(x, y, u, v, rnd):
    """Ruelle de nuit : murs de colombages, lanterne, pavés mouilles."""
    col = vgrad([(0, "141a2a"), (1, "242a3a")], v)
    for x0, x1 in ((0, 80), (254, 334)):
        if x0 <= x <= x1:
            col = C("3a3040")
            if (x % 40 < 3) or (y % 60 < 3) or seg_dist(x, y, x0, y - y % 60, x1, y - y % 60 + 60) < 2: col = C("1e1820")
            if 140 < y < 190 and abs(x - (x0 + x1) / 2) < 14: col = C("f2c45e")              # fenetre allumee
    col = _disc(col, x, y, 96, 230, 7, "ffe08a", "f2a040", 0.5)
    if y > 470:
        col = C("2e3040") if (int(x / 22) + int(y / 12)) % 2 else C("262838")
        col = mix(col, C("f2c45e"), max(0, 0.25 - abs(x - 96) / 400))
    return col


def bg_mer(x, y, u, v, rnd):
    col = vgrad([(0, "2a2050"), (0.35, "c05a4a"), (0.5, "f2a45a"), (1, "f2a45a")], v)
    col = _disc(col, x, y, 120, 300, 36, "ffe8a0", "ffb070", 0.4)
    if y > 300:
        col = mix(C("2a4a7a"), C("10203a"), (y - 300) / 300)
        if math.sin(x * 0.12 + y * 0.6) > 0.92: col = mix(col, C("ffd08a"), 0.5)
    # navire au loin
    if inpoly(x, y, [(230, 296), (310, 296), (300, 308), (240, 308)]): col = C("1a1020")
    if abs(x - 270) < 1.5 and 220 < y < 296: col = C("1a1020")
    if inpoly(x, y, [(272, 226), (302, 276), (272, 276)]): col = C("efe6cf")
    if y > 480: col = mix(C("6a4a30"), C("3a2818"), (y - 480) / 124)                          # ponton
    return col


def bg_japon(x, y, u, v, rnd):
    col = vgrad([(0, "f6e2c8"), (0.5, "f2c4a8"), (1, "e8a88a")], v)
    col = _disc(col, x, y, 220, 150, 52, "d8322c", "f08a6a", 0.3)
    if y > ridge(u, 0.55, 0.04, 6, 13, 0.5) * AH: col = C("8a5a6a")
    # pagode
    for k, (w, yy) in enumerate(((60, 220), (48, 262), (36, 300))):
        if inpoly(x, y, [(70 - w, yy), (70 + w, yy), (70 + w * 0.6, yy - 14), (70 - w * 0.6, yy - 14)]): col = C("3a1a1a")
        if abs(x - 70) < w * 0.45 and yy < y < yy + 28: col = C("b8322c")
    # cerisier
    for bx, by, r in ((290, 120, 40), (250, 90, 30), (310, 180, 34)):
        if math.hypot(x - bx, y - by) < r:
            col = C("f2a8c0") if math.sin(x * 0.35) * math.sin(y * 0.35) < 0.55 else C("f8d0dc")
    if seg_dist(x, y, 300, 470, 285, 130) < 5: col = C("3a2020")
    if rnd(int(x / 3), int(y / 3)) > 0.995: col = C("f8c8d8")
    if y > 470: col = mix(C("c8a080"), C("8a6a50"), (y - 470) / 134)
    return col


def bg_neant(x, y, u, v, rnd):
    """Neant : spirale violette autour d'un trou noir."""
    col = vgrad([(0, "05030c"), (1, "0e0818")], v)
    dx, dy = x - 167, y - 210
    r = math.hypot(dx, dy)
    a = math.atan2(dy, dx)
    arm = math.sin(a * 3 + r * 0.045)
    if r < 220: col = mix(col, C("7a2ac8"), max(0, arm) * (1 - r / 220) * 0.9)
    if r < 38: col = C("000000")
    elif r < 44: col = C("e0b0ff")
    col = _stars(col, x, y, rnd, 600, 0.995, "c8b0ff")
    if y > 480: col = mix(col, C("05030c"), 0.6)
    return col


def bg_cieux(x, y, u, v, rnd):
    """Cieux : mer de nuages dores, rais divins."""
    col = vgrad([(0, "fff4d6"), (0.4, "ffe08a"), (1, "f2b860")], v)
    a = math.degrees(math.atan2(y + 60, x - 167))
    if int(a / 6) % 2 == 0: col = mix(col, C("ffffff"), 0.25)
    for cx, cy, r in ((40, 420, 70), (140, 450, 80), (260, 430, 90), (330, 470, 60), (90, 500, 90), (230, 510, 100)):
        if math.hypot((x - cx) / 1.4, y - cy) < r * 0.6: col = mix(C("ffffff"), C("f2dca8"), (y - cy + 40) / 120)
    return col


def bg_cosmos(x, y, u, v, rnd):
    col = vgrad([(0, "07051a"), (1, "140a2a")], v)
    neb = (math.sin(u * 7 + v * 3) * math.sin(u * 2 - v * 9) + 1) / 2
    col = mix(col, C("3a2a8a"), neb * 0.5)
    col = mix(col, C("c04a8a"), max(0, neb - 0.6) * 1.2)
    col = _stars(col, x, y, rnd, 600, 0.99)
    col = _disc(col, x, y, 70, 100, 26, "f2a45a", "c06a3a", 0.3, edge="8a3a1a")
    if abs(math.hypot((x - 70) / 2.4, y - 100) - 28) < 1.6: col = C("e0c08a")
    col = _disc(col, x, y, 280, 60, 10, "8ac8ff", "4a7ac8", 0.4)
    return col


def bg_desert(x, y, u, v, rnd):
    col = vgrad([(0, "f6d2a0"), (0.5, "f2b070"), (1, "f2b070")], v)
    col = _disc(col, x, y, 240, 160, 40, "fff2c4", "ffd080", 0.35)
    for base, c in ((0.6, "d89050"), (0.7, "c07a40"), (0.8, "a86a38")):
        if y > ridge(u, base, 0.03, 4, 9, base * 7) * AH: col = C(c)
    return col


def bg_marche(x, y, u, v, rnd):
    col = vgrad([(0, "f5e6c4"), (1, "e3c690")], v)
    if y < 120:
        col = C("b8322c") if int(x / 28) % 2 == 0 else C("f2e8cf")
    if (x < 22 or x > 312) and y >= 100: col = C("6b4a2a")
    for sx, sy, c in ((50, 170, "b8322c"), (90, 176, "e0b050"), (250, 170, "4a7a34"), (290, 176, "2c4373")):
        if abs(x - sx) < 18 and sy < y < sy + 110: col = mix(C(c), C("000000"), 0.15 if int(y / 8) % 2 else 0)
    if y > 380: col = mix(C("8a5a2b"), C("5a3a1e"), (y - 380) / 224)
    if 380 <= y <= 392: col = C("c58643")
    return col


def bg_tresor(x, y, u, v, rnd):
    """Salle du tresor : coffres, piles d'or, lueur doree."""
    col = vgrad([(0, "2a1c10"), (1, "4a3018")], v)
    col = _disc(col, x, y, 167, 300, 120, "6a4a20", "8a6020", 0.3)
    for cx, cy, r in ((40, 520, 90), (300, 510, 80), (167, 560, 130)):
        if math.hypot((x - cx) / 1.6, y - cy) < r * 0.5:
            col = C("f2c45e") if rnd(int(x / 4), int(y / 3)) > 0.35 else C("c58643")
    for bx in (40, 290):
        if abs(x - bx) < 30 and 380 < y < 430: col = C("6b3a1a") if abs(y - 400) > 3 else C("c58643")
    if rnd(int(x / 2), int(y / 2)) > 0.996: col = C("fff4c4")
    return col


def bg_forge(x, y, u, v, rnd):
    col = vgrad([(0, "1a1010"), (1, "2a1810")], v)
    col = _disc(col, x, y, 60, 300, 50, "ffb040", "d0461a", 0.6)                             # foyer
    if 10 < x < 110 and 240 < y < 360 and not math.hypot(x - 60, y - 300) < 50: col = C("3a2a22")
    if 250 < x < 320 and 400 < y < 430: col = C("3a3a40")                                     # enclume
    if 270 < x < 300 and 430 < y < 470: col = C("2a2a30")
    for k in range(6):
        if math.hypot(x - (60 + k * 9), y - (240 - k * 30)) < 2: col = C("ffd08a")
    if y > 470: col = mix(C("3a2a22"), C("1a1210"), (y - 470) / 134)
    return col


def bg_bibliotheque(x, y, u, v, rnd):
    col = vgrad([(0, "2a1c14"), (1, "3a2818")], v)
    for x0, x1 in ((0, 90), (244, 334)):
        if x0 <= x <= x1 and y < 470:
            shelf = int(y / 70)
            if y % 70 < 6: col = C("5a3a22")
            else:
                book = int((x + shelf * 13) / 9)
                col = C(("8a1c26", "2c4373", "4a7a34", "c58643", "5a2a6a")[book % 5])
                if (x + shelf * 13) % 9 < 1: col = C("1a1008")
    col = _disc(col, x, y, 167, 150, 6, "ffe08a", "f2a040", 0.4)
    if y > 470: col = C("3a2414")
    return col


def bg_caverne(x, y, u, v, rnd):
    col = vgrad([(0, "141018"), (1, "241a2a")], v)
    for k, (cx, cy, hgt, c) in enumerate(((40, 480, 120, "7a5ac8"), (90, 480, 70, "4a9ac8"), (290, 480, 140, "c85a9a"),
                                          (250, 480, 80, "7a5ac8"), (320, 300, 60, "4a9ac8"))):
        if inpoly(x, y, [(cx - 18, cy), (cx + 18, cy), (cx + 6, cy - hgt), (cx - 6, cy - hgt)]):
            col = mix(C(c), C("ffffff"), 0.3 if x < cx else 0)
        col = _disc(col, x, y, cx, cy - hgt * 0.5, 4, c, c, 0.25)
    if y > 480: col = C("100c14")
    return col


BACKDROPS = {k[3:]: f for k, f in globals().items() if k.startswith("bg_")}


# =====================================================================================================================
# PERSONNAGE
# =====================================================================================================================

TENUES = {
    "armure":       {'weapon': "dfe5ee", 'skin': "e9c29a", 'sleeve': "7d8796", 'hair': "5a3a22", 'gi': "9aa4b2", 'inner': "c8ced8", 'belt': "6b4a2a", 'hakama': "4a5260", 'beard': "5a3a22"},
    "armure_noire": {'weapon': "c8ced8", 'skin': "c9a28a", 'sleeve': "2a2a34", 'hair': "1a1a20", 'gi': "34343e", 'inner': "8a1c26", 'belt': "1a1a20", 'hakama': "202028", 'beard': "1a1a20"},
    "armure_or":    {'weapon': "fff4c4", 'skin': "e9c29a", 'sleeve': "c9a24a", 'hair': "f2e6c4", 'gi': "e0b84a", 'inner': "fff4d6", 'belt': "8a5a2b", 'hakama': "b8923a", 'beard': "f2e6c4"},
    "armure_rouge": {'weapon': "dfe5ee", 'skin': "e9c29a", 'sleeve': "8a2c2a", 'hair': "3a2014", 'gi': "a83a30", 'inner': "e0b050", 'belt': "3a2014", 'hakama': "5a1a18", 'beard': "3a2014"},
    "cuir":         {'weapon': "c8ced8", 'skin': "e2b48a", 'sleeve': "6b4a2a", 'hair': "3a2414", 'gi': "8a5a2b", 'inner': "c9b27a", 'belt': "3a2414", 'hakama': "4a3420", 'beard': "3a2414", 'hood': "5a3a22", 'face': "e2b48a"},
    "foret":        {'weapon': "8a5a2b", 'skin': "e9c29a", 'sleeve': "3f6b2c", 'hood': "2f5a2c", 'face': "e9c29a", 'gi': "4a7a34", 'inner': "c9b27a", 'belt': "6b4a2a", 'hakama': "4a3420", 'hair': "8a5a2b"},
    "ombre":        {'weapon': "dfe5ee", 'skin': "2a1a20", 'sleeve': "1a1018", 'hood': "140a12", 'face': "05030a", 'gi': "1a1018", 'inner': "6a1420", 'belt': "6a1420", 'hakama': "120a10", 'hair': "05030a"},
    "gi":           {'weapon': "dfe5ee", 'skin': "e9c29a", 'sleeve': "efe6cf", 'hair': "1a1010", 'gi': "efe6cf", 'inner': "b8322c", 'belt': "1a1010", 'hakama': "2a2a3a", 'beard': "1a1010"},
    "robe":         {'weapon': "6b4a2a", 'skin': "e9c29a", 'sleeve': "4a2a7a", 'hood': "3a1f66", 'face': "1a1030", 'gi': "4a2a7a", 'inner': "c9a24a", 'belt': "c9a24a", 'hakama': "3a1f66", 'hair': "c9c9d9", 'beard': "c9c9d9"},
    "riche":        {'weapon': "c9a24a", 'skin': "e9c29a", 'sleeve': "2f6a44", 'hair': "6b4a2a", 'beard': "8a6a4a", 'gi': "2f6a44", 'inner': "e0b050", 'belt': "c58643", 'hakama': "3a2a1a", 'hood': "2f6a44", 'face': "e9c29a"},
    "pirate":       {'weapon': "dfe5ee", 'skin': "d8a878", 'sleeve': "efe6cf", 'hair': "1a1010", 'beard': "1a1010", 'gi': "8a1c26", 'inner': "efe6cf", 'belt': "c58643", 'hakama': "2a2a3a", 'hood': "8a1c26", 'face': "d8a878"},
    "squelette":    {'weapon': "c8ced8", 'skin': "e8e4d8", 'sleeve': "2a2034", 'hood': "1a1424", 'face': "e8e4d8", 'gi': "2a2034", 'inner': "5ac88a", 'belt': "5ac88a", 'hakama': "1a1424", 'hair': "e8e4d8"},
    "pierre":       {'weapon': "8a8a90", 'skin': "8a8070", 'sleeve': "6a6258", 'hair': "4a443c", 'gi': "7a7266", 'inner': "e08a3a", 'belt': "4a443c", 'hakama': "5a544a", 'beard': "4a443c"},
    "lumiere":      {'weapon': "fff4c4", 'skin': "f2d8b8", 'sleeve': "f6f0e0", 'hood': "f2e6c4", 'face': "f2d8b8", 'gi': "fffaf0", 'inner': "e0b84a", 'belt': "e0b84a", 'hakama': "f2e6c4", 'hair': "f2e6c4", 'beard': "f2e6c4"},
}


def tenue(base, **over):
    p = dict(TENUES[base])
    p.update(over)
    return p


# Armes : (main, pointe) pour _person, avec cx = 167, pieds 470, echelle 1.55 (epaule vers y = 284)
POSES = {
    "epee": ((196, 300), (262, 128)), "hache": ((198, 318), (246, 168)), "lance": ((206, 332), (236, 58)),
    "masse": ((198, 318), (238, 196)), "marteau": ((198, 318), (238, 190)), "fleau": ((200, 320), (236, 250)),
    "baton": ((206, 320), (222, 118)), "faux": ((206, 320), (222, 92)), "katana": ((196, 300), (276, 150)),
    "rapiere": ((222, 318), (324, 296)), "dague": ((214, 330), (252, 286)), "sceptre": ((206, 320), (214, 236)),
    "grimoire": ((214, 330), None), "arc": ((118, 292), None), "arbalete": ((214, 320), None),
    "bourse": ((210, 336), None), "pioche": ((198, 318), (240, 200)), "aucune": ((200, 340), None),
    "luth": ((206, 334), None),
}


def figure(spec):
    """Calque du personnage et de son arme."""
    pal = spec["tenue"]
    arme = spec.get("arme", "epee")
    hand, tip = POSES[arme]
    cx = spec.get("cx", 190 if arme == "arc" else 167)
    if arme == "arc": hand = (cx - 72, 292)
    hood, beard, wide = spec.get("capuche", False), spec.get("barbe", False), spec.get("large", False)
    s = spec.get("echelle", 1.5 if arme == "arc" else 1.55)
    metal = spec.get("metal", pal.get('weapon', "dfe5ee"))
    if "metal" in spec: pal = dict(pal, weapon=metal)                  # lame de la couleur de la fiche
    orbe = spec.get("orbe", "8fd0ff")

    def layer(x, y, col):
        part = _person(x, y, cx, 470, s, hand, tip if arme in ("epee", "katana", "rapiere", "dague", "lance", "baton", "faux", "sceptre") else None,
                       hood=hood, beard=beard, wide=wide)
        if part: col = C(pal[part]) if part in pal else col
        # ---- armes ----
        if arme in ("epee", "katana"):
            if seg_dist(x, y, hand[0] - 10, hand[1] + 4, hand[0] + 10, hand[1] - 4) < 3: col = C("c58643")
        elif arme in ("hache", "masse", "marteau", "pioche", "fleau"):
            if seg_dist(x, y, hand[0], hand[1], tip[0], tip[1]) < 3.2: col = C("6b4a2a")
            if arme == "hache":
                if inpoly(x, y, [(tip[0], tip[1] + 6), (tip[0] + 34, tip[1] - 12), (tip[0] + 40, tip[1] + 26), (tip[0] + 4, tip[1] + 24)]): col = C(metal)
                if inpoly(x, y, [(tip[0], tip[1] + 6), (tip[0] - 20, tip[1] - 2), (tip[0] - 22, tip[1] + 22), (tip[0], tip[1] + 22)]): col = C(metal)
            elif arme == "masse":
                if math.hypot(x - tip[0], y - tip[1]) < 15: col = C(metal)
                for a in range(0, 360, 45):
                    ex, ey = tip[0] + 22 * math.cos(math.radians(a)), tip[1] + 22 * math.sin(math.radians(a))
                    if seg_dist(x, y, tip[0], tip[1], ex, ey) < 2.2: col = C(metal)
            elif arme == "marteau":
                if inpoly(x, y, [(tip[0] - 30, tip[1] - 16), (tip[0] + 30, tip[1] - 30), (tip[0] + 36, tip[1] - 2), (tip[0] - 24, tip[1] + 12)]): col = C(metal)
            elif arme == "pioche":
                if seg_dist(x, y, tip[0] - 34, tip[1] + 16, tip[0], tip[1]) < 3 or seg_dist(x, y, tip[0], tip[1], tip[0] + 32, tip[1] + 20) < 3: col = C(metal)
            elif arme == "fleau":
                for k in range(7):
                    px, py = tip[0] + k * 5, tip[1] + k * 7
                    if abs(math.hypot(x - px, y - py) - 3) < 1.1: col = C("8a8a90")
                if math.hypot(x - (tip[0] + 40), y - (tip[1] + 56)) < 13: col = C(metal)
        elif arme == "lance":
            if inpoly(x, y, [(tip[0] - 6, tip[1] + 22), (tip[0] + 6, tip[1] + 22), (tip[0], tip[1] - 14)]): col = C(metal)
        elif arme in ("baton", "sceptre"):
            d = math.hypot(x - tip[0], y - tip[1])
            r = 13 if arme == "baton" else 9
            if d < r: col = mix(C("ffffff"), C(orbe), d / r)
            elif d < r * 3.4: col = mix(col, C(orbe), (r * 3.4 - d) / (r * 2.4) * 0.55)
        elif arme == "faux":
            cxl, cyl = tip[0] - 44, tip[1] + 10
            dd = math.hypot(x - cxl, y - cyl)
            if 40 < dd < 52 and y < cyl and x < tip[0] + 2 and x > cxl - 40: col = C(metal)
        elif arme == "grimoire":
            if inpoly(x, y, [(hand[0] - 4, hand[1] - 20), (hand[0] + 34, hand[1] - 28), (hand[0] + 34, hand[1] + 6), (hand[0] - 4, hand[1] + 14)]):
                col = C(spec.get("couverture", "6a1c2a"))
                if abs(x - (hand[0] + 15)) < 1.4: col = C("c9a24a")
            d = math.hypot(x - (hand[0] + 15), y - (hand[1] - 44))
            if d < 30: col = mix(col, C(orbe), (30 - d) / 30 * 0.7)
            for k in range(4):
                gx, gy = hand[0] + 4 + k * 10, hand[1] - 54 - (k % 2) * 14
                if abs(x - gx) < 3 and abs(y - gy) < 5 and (int(x) + int(y)) % 3: col = C("ffffff")
        elif arme == "arc":
            cxb, cyb, rb = cx - 40, 292, 92
            a = math.degrees(math.atan2(y - cyb, x - cxb)) % 360
            if abs(math.hypot(x - cxb, y - cyb) - rb) < 3 and 135 < a < 225: col = C(spec.get("bois", "6b3a1a"))
            top = (cxb + rb * math.cos(math.radians(225)), cyb + rb * math.sin(math.radians(225)))
            bot = (cxb + rb * math.cos(math.radians(135)), cyb + rb * math.sin(math.radians(135)))
            if seg_dist(x, y, top[0], top[1], cx - 14, 292) < 0.8 or seg_dist(x, y, bot[0], bot[1], cx - 14, 292) < 0.8: col = C("efe6cf")
            if seg_dist(x, y, 40, 292, cx - 12, 292) < 1.5: col = C(spec.get("fleche", "c9a24a"))
            if inpoly(x, y, [(30, 292), (42, 286), (42, 298)]): col = C(spec.get("pointe", "dfe5ee"))
        elif arme == "arbalete":
            if seg_dist(x, y, hand[0] - 70, hand[1], hand[0] + 14, hand[1]) < 4: col = C("6b4a2a")
            if abs(x - (hand[0] - 56)) < 3 and abs(y - hand[1]) < 40: col = C("3a3a40")
            if seg_dist(x, y, hand[0] - 56, hand[1] - 40, hand[0] - 20, hand[1]) < 0.9 or seg_dist(x, y, hand[0] - 56, hand[1] + 40, hand[0] - 20, hand[1]) < 0.9: col = C("efe6cf")
            if inpoly(x, y, [(hand[0] - 92, hand[1]), (hand[0] - 78, hand[1] - 6), (hand[0] - 78, hand[1] + 6)]): col = C("dfe5ee")
        elif arme == "dague" and spec.get("double", True):
            if seg_dist(x, y, 120, 330, 84, 296) < 3: col = C(metal)
        elif arme == "bourse":
            d = math.hypot(x - 222, y - 352)
            if d < 22: col = C("8a5a2b") if y > 340 else C("6b4a2a")
            if math.hypot(x - 222, y - 356) < 7: col = C("f2c45e")
        return col
    return layer


# =====================================================================================================================
# EFFETS (avant / arriere du personnage)
# =====================================================================================================================

def fx_aura(c, r=150, cy=290, k=0.4):
    def f(x, y, col):
        d = math.hypot(x - 167, (y - cy) * 0.8)
        return mix(col, C(c), (r - d) / r * k) if d < r else col
    return f


def fx_halo(c="ffe08a"):
    def f(x, y, col):
        return C(c) if abs(math.hypot((x - 167) / 1.6, y - 222) - 14) < 2 else col
    return f


def fx_couronne(c="f2c45e", gem="b8322c"):
    def f(x, y, col):
        if 150 < x < 184 and 222 < y < 234: return C(c)
        for px in (152, 167, 182):
            if inpoly(x, y, [(px - 5, 223), (px + 5, 223), (px, 210)]): return C(c)
        return C(gem) if math.hypot(x - 167, y - 228) < 3 else col
    return f


def fx_cornes(c="2a1a14"):
    def f(x, y, col):
        for sg in (-1, 1):
            if seg_dist(x, y, 167 + sg * 12, 236, 167 + sg * 26, 214) < 4 or seg_dist(x, y, 167 + sg * 26, 214, 167 + sg * 22, 198) < 2.5: return C(c)
        return col
    return f


def fx_ailes(c, bord, kind="ange"):
    def f(x, y, col):
        for sg in (-1, 1):
            dx = (x - 167) * sg
            if dx < 18: continue
            t = (dx - 18) / 130
            if t > 1: continue
            top = 300 - 150 * math.sin(t * math.pi * 0.85) - 30 * t
            bot = 300 + 40 - 160 * t ** 1.4 if kind != "demon" else 300 + 20 - 120 * t
            if top < y < bot:
                if kind == "ange":
                    feather = ((dx + y * 0.4) % 18) < 2
                    return C(bord) if feather or y > bot - 4 else C(c)
                if kind == "demon":
                    rib = abs(((dx * 0.9 - (y - 250) * 0.3) % 34) - 17) < 1.6
                    return C(bord) if rib or y < top + 3 else C(c)
                return mix(col, C(c), 0.8 if ((dx + y) % 14) > 3 else 0.4)
        return col
    return f


def fx_cape(c, c2=None):
    def f(x, y, col):
        if inpoly(x, y, [(137, 288), (197, 288), (225, 462), (109, 462)]):
            return C(c) if x < 200 else C(c2 or c)
        return col
    return f


def fx_bouclier(fond="2c4373", motif="e0dccf", bord="c58643", forme="croix"):
    def f(x, y, col):
        sx, sy = 112, 360
        if not (70 < x < 150 and 310 < y < 414): return col
        shield = [(sx - 34, sy - 44), (sx + 34, sy - 44), (sx + 34, sy + 6), (sx, sy + 50), (sx - 34, sy + 6)]
        inner = [(sx - 28, sy - 38), (sx + 28, sy - 38), (sx + 28, sy + 4), (sx, sy + 42), (sx - 28, sy + 4)]
        if inpoly(x, y, shield): col = C(bord)
        if inpoly(x, y, inner):
            col = C(fond)
            if forme == "croix" and (abs(x - sx) < 5 or abs(y - (sy - 10)) < 5): col = C(motif)
            if forme == "soleil" and math.hypot(x - sx, y - sy + 8) < 12: col = C(motif)
            if forme == "ecartele" and (x < sx) == (y < sy - 10): col = C(motif)
            if forme == "crane" and math.hypot(x - sx, y - sy + 10) < 11 and not math.hypot(x - sx - 4, y - sy + 12) < 3 and not math.hypot(x - sx + 4, y - sy + 12) < 3: col = C(motif)
            if forme == "rune" and (seg_dist(x, y, sx, sy - 30, sx, sy + 26) < 2.5 or seg_dist(x, y, sx, sy - 10, sx + 14, sy - 26) < 2.5 or seg_dist(x, y, sx, sy - 2, sx - 14, sy - 18) < 2.5): col = C(motif)
        return col
    return f


def fx_sol_flammes(c="ff7a2a", c2="ffd04a", h=90):
    def f(x, y, col):
        if y < 470 - h: return col
        lick = 470 - h * (0.5 + 0.5 * math.sin(x * 0.11) * math.sin(x * 0.047 + 1.3))
        if y > lick: return mix(C(c2), C(c), (y - lick) / (h + 1)) if y < 520 else C(c)
        return col
    return f


def fx_flammes_mains(c="ff7a2a", cx=230, cy=300):
    def f(x, y, col):
        d = math.hypot(x - cx, (y - cy) * 0.7)
        if d < 26 and math.sin(x * 0.5 + y * 0.3) > -0.3: return mix(C("ffe08a"), C(c), d / 26)
        return col
    return f


def fx_neige(c="ffffff"):
    def f(x, y, col):
        return C(c) if (int(x * 7 + y * 3) % 97 == 0 and int(y) % 5 == 0) else col
    return f


def fx_eclairs(c="cfe8ff", pts=((60, 40), (80, 120), (60, 160), (90, 250))):
    segs = list(zip(pts, pts[1:]))

    def f(x, y, col):
        for (a, b) in segs:
            dd = seg_dist(x, y, a[0], a[1], b[0], b[1])
            if dd < 2.2: return C("ffffff")
            if dd < 9: col = mix(col, C(c), (9 - dd) / 9 * 0.7)
        return col
    return f


def fx_bulles(c="7ad84a", n=14, seed=3):
    rr = random.Random(seed)
    pts = [(rr.uniform(30, 304), rr.uniform(140, 470), rr.uniform(4, 10)) for _ in range(n)]

    def f(x, y, col):
        for (bx, by, r) in pts:
            d = math.hypot(x - bx, y - by)
            if d < r: return mix(C("eaffd0"), C(c), d / r) if d > r - 2 else mix(col, C(c), 0.6)
        return col
    return f


def fx_brume(c="5a3a8a", y0=0.78, k=0.5):
    def f(x, y, col):
        fog = math.exp(-((y / AH - y0) / 0.08) ** 2) * (0.6 + 0.4 * math.sin(x * 0.05 + y * 0.02))
        return mix(col, C(c), fog * k)
    return f


def fx_ombres(c="05030a", k=0.6):
    def f(x, y, col):
        t = math.sin(x * 0.04 + y * 0.013) * math.sin(x * 0.017 - y * 0.03)
        return mix(col, C(c), max(0, t) * k) if y > 300 else col
    return f


def fx_cercle(c="9f7aff", r=120, cy=470):
    def f(x, y, col):
        e = math.hypot((x - 167) / r, (y - cy) / (r / 4))
        if abs(e - 1) < 0.03 or abs(e - 0.8) < 0.025: return C(c)
        if e < 1:
            a = math.degrees(math.atan2(y - cy, x - 167))
            if 0.82 < e < 0.98 and int(a / 15) % 2 == 0: return mix(col, C(c), 0.6)
            return mix(col, C(c), 0.15)
        return col
    return f


def fx_etincelles(c="fff4c4", p=0.994, zone=(0, AH)):
    def f(x, y, col, rnd=None):
        return col
    def g2(x, y, col, rnd):
        return C(c) if zone[0] < y < zone[1] and rnd(int(x / 2) + 911, int(y / 2) + 37) > p else col
    g2.rnd = True
    return g2


def fx_esprits(c="8affd8", pts=((70, 200), (270, 170), (300, 330), (50, 360))):
    def f(x, y, col):
        for px, py in pts:
            d = math.hypot(x - px, y - py)
            if d < 9: return mix(C("ffffff"), C(c), d / 9)
            if d < 30: col = mix(col, C(c), (30 - d) / 30 * 0.5)
            if py < y < py + 30 and abs(x - px - math.sin((y - py) * 0.2) * 4) < 3 * (1 - (y - py) / 30): col = mix(col, C(c), 0.7)
        return col
    return f


def fx_cranes(c="e8e4d8", pts=((60, 500), (280, 505), (110, 530))):
    def f(x, y, col):
        for px, py in pts:
            if math.hypot(x - px, y - py) < 12 or (abs(x - px) < 7 and py < y < py + 14):
                if math.hypot(x - px - 4, y - py) < 3 or math.hypot(x - px + 4, y - py) < 3: return C("1a1010")
                return C(c)
        return col
    return f


def fx_pieces(c="f2c45e", n=24, seed=5, pluie=False):
    rr = random.Random(seed)
    pts = [(rr.uniform(20, 314), rr.uniform(60, 300) if pluie else rr.uniform(480, 590)) for _ in range(n)]

    def f(x, y, col):
        for px, py in pts:
            if math.hypot((x - px) / 1.6, y - py) < 5: return C(c) if y < py else C("c58643")
        return col
    return f


def fx_yeux(c="ff4a4a", y0=256, ecart=5):
    def f(x, y, col):
        for ex in (167 - ecart, 167 + ecart):
            d = math.hypot(x - ex, y - y0)
            if d < 2: return C(c)
            if d < 7: col = mix(col, C(c), (7 - d) / 7 * 0.5)
        return col
    return f


def fx_fleches(c="1a1418", n=16, seed=7):
    rr = random.Random(seed)
    pts = [(rr.uniform(10, 324), rr.uniform(20, 200)) for _ in range(n)]

    def f(x, y, col):
        for px, py in pts:
            if seg_dist(x, y, px, py, px + 14, py + 26) < 1.2: return C(c)
        return col
    return f


def fx_echarpe(c="8a1c26"):
    def f(x, y, col):
        return C(c) if inpoly(x, y, [(176, 268), (250, 250), (240, 262), (178, 278)]) else col
    return f


def fx_cristaux(c="8fd0ff", pts=((60, 250), (280, 220), (300, 380), (40, 400))):
    def f(x, y, col):
        for px, py in pts:
            if inpoly(x, y, [(px, py - 26), (px + 11, py), (px, py + 26), (px - 11, py)]):
                return mix(C("ffffff"), C(c), 0.35 if x > px else 0.8)
        return col
    return f


def fx_sablier(c="e0b84a", cx=270, cy=170):
    def f(x, y, col):
        if not (cx - 30 < x < cx + 30 and cy - 46 < y < cy + 46): return col
        if abs(y - (cy - 40)) < 3 or abs(y - (cy + 40)) < 3: return C("6b4a2a")
        w = abs(y - cy) * 0.6 + 2
        if abs(x - cx) < w and abs(y - cy) < 38:
            sand = (y > cy + 38 - 14) or (y < cy and y > cy - 20)
            return C(c) if sand else mix(col, C("cfe8ff"), 0.5)
        return col
    return f


def fx_oeil(c="ffe08a", cx=167, cy=110):
    def f(x, y, col):
        e = math.hypot((x - cx) / 60, (y - cy) / 26)
        if e < 1:
            d = math.hypot(x - cx, y - cy)
            if d < 9: return C("05030a")
            if d < 22: return C(c)
            return C("f6f0e0")
        if e < 1.6: return mix(col, C(c), (1.6 - e) / 0.6 * 0.4)
        return col
    return f


def fx_fioles(pts=((60, 420, "7ad84a"), (90, 430, "b8322c"), (275, 425, "2c8ac8"))):
    def f(x, y, col):
        for px, py, c in pts:
            if math.hypot(x - px, y - py) < 13: return C(c) if y > py - 4 else mix(C(c), C("ffffff"), 0.4)
            if abs(x - px) < 4 and py - 26 < y < py - 10: return C("cfe8ff")
            if abs(x - px) < 5 and py - 30 < y < py - 25: return C("6b4a2a")
        return col
    return f


def fx_banniere(c="8a1c26", motif="f2c45e"):
    def f(x, y, col):
        if abs(x - 290) < 2 and 60 < y < 470: return C("3a2014")
        if 292 < x < 330 and 70 < y < 200 - (x - 292) * 0.3:
            return C(motif) if math.hypot(x - 311, y - 120) < 9 else C(c)
        return col
    return f


def fx_portail(c="7a2ac8", cx=167, cy=240, r=110):
    def f(x, y, col):
        e = math.hypot((x - cx) / r, (y - cy) / (r * 1.4))
        if abs(e - 1) < 0.05: return C("e0b0ff")
        if e < 1: return mix(col, C(c), (1 - e) * 0.8)
        return col
    return f


def fx_dragon(c="2a0a0a"):
    def f(x, y, col):
        if not (20 < x < 320 and 40 < y < 200): return col
        body = seg_dist(x, y, 70, 140, 260, 110) < 9
        neck = seg_dist(x, y, 260, 110, 300, 80) < 6
        wing = inpoly(x, y, [(140, 125), (110, 40), (200, 70), (190, 120)]) or inpoly(x, y, [(180, 120), (240, 50), (250, 100)])
        tail = seg_dist(x, y, 70, 140, 25, 175) < 4
        return C(c) if body or neck or wing or tail else col
    return f


def fx_fissures(c="ff7a2a"):
    def f(x, y, col):
        if y < 470: return col
        for (a, b) in (((40, 480), (120, 520)), ((120, 520), (100, 590)), ((200, 490), (260, 540)), ((260, 540), (320, 560)), ((167, 470), (180, 530))):
            dd = seg_dist(x, y, a[0], a[1], b[0], b[1])
            if dd < 2: return C("ffe08a")
            if dd < 7: col = mix(col, C(c), (7 - dd) / 7)
        return col
    return f


def fx_feuilles(c="7ab84a", n=22, seed=9):
    rr = random.Random(seed)
    pts = [(rr.uniform(10, 324), rr.uniform(40, 460), rr.uniform(0, 3)) for _ in range(n)]

    def f(x, y, col):
        for px, py, a in pts:
            dx, dy = x - px, y - py
            u_ = dx * math.cos(a) + dy * math.sin(a)
            v_ = -dx * math.sin(a) + dy * math.cos(a)
            if (u_ / 7) ** 2 + (v_ / 3) ** 2 < 1: return C(c)
        return col
    return f


def fx_double(dx=0, c="3a1f66", k=0.45):
    """Doubles fantomes (illusions, clones) de chaque cote."""
    def f(x, y, col):
        for ox in (-70, 70):
            if _person(x - ox, y, 167, 470, 1.4, (200, 330)): return mix(col, C(c), k)
        return col
    return f


def fx_masque(c="efe6cf"):
    def f(x, y, col):
        if math.hypot((x - 167) / 1.1, y - 254) < 9 and y < 262:
            if math.hypot(x - 163, y - 253) < 2 or math.hypot(x - 171, y - 253) < 2: return C("05030a")
            return C(c)
        return col
    return f


def fx_marteau_enclume():
    def f(x, y, col):
        if 60 < x < 130 and 430 < y < 452: return C("3a3a40")
        if 82 < x < 108 and 452 < y < 480: return C("2a2a30")
        return col
    return f


def fx_racines(c="4a3420"):
    def f(x, y, col):
        for k in range(6):
            x0 = 30 + k * 56
            if y > 440 and abs(x - x0 - math.sin(y * 0.06 + k) * 12) < 4 + (y - 440) / 40: return C(c)
        return col
    return f


def fx_eau(c="4a9ad8"):
    def f(x, y, col):
        for k in range(3):
            cy = 300 + k * 30
            if abs(y - cy - 14 * math.sin(x * 0.05 + k)) < 3 and 30 < x < 304 and abs(x - 167) > 40: return mix(C("cfe8ff"), C(c), 0.5)
        return col
    return f


def fx_bete(kind="loup", c="5a5048", ox=0, flip=False):
    """Compagnon du Dompteur, a ses pieds (loup, ours, serpent) ou dans le ciel / sur l'epaule (oiseau)."""
    def f(x, y, col):
        xx = (334 - x) if flip else x
        xx -= ox
        if kind == "loup":
            body = math.hypot((xx - 80) / 34, (y - 440) / 15) < 1
            head = math.hypot((xx - 118) / 13, (y - 424) / 11) < 1 or inpoly(xx, y, [(124, 420), (146, 428), (126, 434)])
            ears = inpoly(xx, y, [(112, 416), (116, 400), (121, 414)])
            legs = any(abs(xx - lx) < 3.5 and 446 < y < 472 for lx in (58, 70, 92, 104))
            tail = seg_dist(xx, y, 46, 434, 26, 418) < 4
            if body or head or ears or legs or tail:
                return C("ffd04a") if math.hypot(xx - 122, y - 422) < 2 else C(c)
        elif kind == "ours":
            body = math.hypot((xx - 75) / 48, (y - 425) / 30) < 1
            head = math.hypot((xx - 128) / 20, (y - 410) / 17) < 1
            ears = math.hypot(xx - 122, y - 392) < 6 or math.hypot(xx - 138, y - 394) < 6
            legs = any(abs(xx - lx) < 8 and 440 < y < 472 for lx in (45, 70, 98, 112))
            if body or head or ears or legs: return C(c)
        elif kind == "serpent":
            for k in range(60):
                t = k / 60
                px, py = 40 + t * 110, 455 - t * 120 + math.sin(t * 12) * 18
                if math.hypot(xx - px, y - py) < 6 - t * 2: return C(c) if k % 6 else C("e0c84a")
            if math.hypot(xx - 150, y - 335) < 7: return C(c)
        elif kind == "oiseau":
            bx, by = 225, 238
            if math.hypot((xx - bx) / 12, (y - by) / 8) < 1: return C(c)
            if inpoly(xx, y, [(bx - 6, by - 4), (bx - 40, by - 30), (bx - 20, by + 2)]) or inpoly(xx, y, [(bx + 4, by - 4), (bx + 40, by - 32), (bx + 18, by + 2)]): return C(c)
            if inpoly(xx, y, [(bx + 10, by - 2), (bx + 18, by + 1), (bx + 10, by + 3)]): return C("e0b84a")
        elif kind == "cheval":
            body = math.hypot((xx - 70) / 46, (y - 405) / 22) < 1
            neck = seg_dist(xx, y, 108, 398, 126, 360) < 10
            head = math.hypot((xx - 134) / 15, (y - 356) / 9) < 1
            legs = any(abs(xx - lx) < 4 and 420 < y < 472 for lx in (38, 52, 92, 106))
            mane = seg_dist(xx, y, 104, 392, 120, 352) < 4 and xx < 116
            if body or neck or head or legs: return C("2a1a14") if mane else C(c)
        return col
    return f


def fx_luth(c="a8703a"):
    def f(x, y, col):
        if math.hypot((x - 200) / 22, (y - 340) / 28) < 1:
            return C("1a1008") if math.hypot(x - 200, y - 336) < 6 else C(c)
        if seg_dist(x, y, 206, 316, 250, 250) < 3.5: return C("6b4a2a")
        if inpoly(x, y, [(246, 246), (258, 236), (262, 246), (252, 256)]): return C("4a2a14")
        if seg_dist(x, y, 200, 360, 250, 250) < 0.6: return C("efe6cf")
        return col
    return f


def fx_notes(c="ffe08a", pts=((70, 180), (110, 130), (260, 160), (290, 230), (60, 280))):
    def f(x, y, col):
        for px, py in pts:
            if math.hypot((x - px) / 1.3, y - py) < 6: return C(c)
            if abs(x - (px + 6)) < 1.6 and py - 26 < y < py: return C(c)
            if px + 6 < x < px + 14 and py - 26 < y < py - 20 - (x - px - 6) * 0.3: return C(c)
        return col
    return f


# =====================================================================================================================
# FICHES : une par classe (5 bases : illustration de leur famille)
# =====================================================================================================================

T = tenue
FICHES = {
    # ---------------------------------------------------------------- GUERRIER
    "epeiste": dict(decor="rempart", tenue=T("armure", sleeve="5a6a8a", gi="7a8aa8"), arme="epee", fx=[fx_cape("2c4373", "1f3050")]),
    "fantassin": dict(decor="champ", tenue=T("armure", gi="8a6a4a", hakama="5a4a3a"), arme="lance", fx=[fx_bouclier("8a1c26", "f2c45e", forme="ecartele")]),
    "brute": dict(decor="champ", tenue=T("cuir", skin="c99a7a", sleeve="c99a7a", gi="5a3a22"), arme="masse", large=True, metal="8a8a90", fx=[fx_aura("b8322c", 120, k=0.25)]),
    "mercenaire": dict(decor="ruelle", tenue=T("cuir", gi="4a4a52", inner="8a1c26"), arme="epee", barbe=True, fx=[fx_pieces(n=10, seed=2)]),
    "protecteur": dict(decor="rempart", tenue=T("armure"), arme="epee", fx=[fx_bouclier("2c4373", "e0dccf", forme="croix"), fx_aura("8fd0ff", 140, k=0.3)]),
    "berserker": dict(decor="volcan", tenue=T("cuir", skin="c99a7a", sleeve="c99a7a", hair="b8322c", beard="b8322c"), arme="hache", barbe=True, large=True,
                      fx=[fx_aura("ff3a2a", 150, k=0.45), fx_yeux("ff3a2a", 254)]),
    "duelliste": dict(decor="arene", tenue=T("armure_rouge", sleeve="efe6cf", gi="8a1c26"), arme="rapiere", fx=[fx_cape("8a1c26")]),
    "templier": dict(decor="cathedrale", tenue=T("armure", gi="efe6cf", inner="b8322c"), arme="epee", fx=[fx_bouclier("efe6cf", "b8322c", forme="croix"), fx_cape("efe6cf", "d8d0c0")]),
    "avant_garde": dict(decor="champ", tenue=T("armure", gi="6a7a96"), arme="lance", large=True, fx=[fx_bouclier("6a7a96", "f2c45e", forme="soleil"), fx_banniere("2c4373")]),
    "ravageur": dict(decor="champ", tenue=T("armure_noire", inner="b8322c"), arme="hache", large=True, fx=[fx_sol_flammes("b8322c", "ff7a2a", 60), fx_yeux("ff5a2a", 254)]),
    "bretteur": dict(decor="ruelle", tenue=T("cuir", gi="2c4373", inner="efe6cf"), arme="rapiere", fx=[fx_cape("1a2a4a"), fx_etincelles("cfe8ff", 0.995, (200, 420))]),
    "lame_mage": dict(decor="nuit", tenue=T("robe", sleeve="5a6a8a", gi="4a2a7a"), arme="epee", metal="b8a0ff", fx=[fx_cercle("9f7aff"), fx_aura("7a5aff", 130, k=0.35)]),
    "paladin": dict(decor="cathedrale", tenue=T("armure_or"), arme="marteau", fx=[fx_halo(), fx_bouclier("efe6cf", "e0b84a", forme="soleil"), fx_aura("ffe08a", 150, k=0.35)]),
    "gladiateur": dict(decor="arene", tenue=T("cuir", skin="d8a878", sleeve="d8a878", gi="8a6a3a", inner="c58643"), arme="epee", fx=[fx_bouclier("c58643", "8a1c26", "6b4a2a", forme="soleil")]),
    "samourai": dict(decor="japon", tenue=T("armure_rouge", gi="2a2a3a", sleeve="8a2c2a"), arme="katana", fx=[fx_feuilles("f2a8c0", 18)]),
    "champion": dict(decor="arene", tenue=T("armure_or", gi="c9a24a"), arme="epee", fx=[fx_cape("8a1c26"), fx_etincelles("fff4c4", 0.99, (40, 300)), fx_couronne("7ab84a", "7ab84a")]),
    "colosse": dict(decor="montagne", tenue=T("pierre", inner="8fd0ff"), arme="marteau", large=True, echelle=1.75, fx=[fx_fissures("8fd0ff")]),
    "warlord": dict(decor="champ", tenue=T("armure_rouge", gi="3a2a2a"), arme="hache", large=True, fx=[fx_banniere("8a1c26", "e0b84a"), fx_cape("5a1a18"), fx_fleches()]),
    "chevalier_noir": dict(decor="cimetiere", tenue=T("armure_noire"), arme="epee", metal="8a1c26", fx=[fx_cape("1a0a10"), fx_aura("6a1420", 140, k=0.45), fx_yeux("ff2a2a", 254), fx_cornes()]),
    "croise": dict(decor="cieux", tenue=T("armure", gi="efe6cf", inner="b8322c"), arme="epee", fx=[fx_halo(), fx_bouclier("efe6cf", "b8322c", forme="croix"), fx_ailes("fffaf0", "e0c88a")]),
    "executeur": dict(decor="cimetiere", tenue=T("ombre", hood="2a0a10", sleeve="3a1018", gi="2a0a10"), arme="hache", capuche=True, metal="c8ced8", fx=[fx_cranes(), fx_yeux("ff4a4a", 256)]),
    "gardien_runique": dict(decor="caverne", tenue=T("armure", inner="8fd0ff"), arme="marteau", metal="8fd0ff", fx=[fx_bouclier("2c4373", "8fd0ff", forme="rune"), fx_cercle("8fd0ff"), fx_cristaux("8fd0ff")]),
    "titan": dict(decor="montagne", tenue=T("pierre", inner="ffb040"), arme="masse", large=True, echelle=1.85, metal="6a6258", fx=[fx_fissures("ffb040"), fx_aura("ffb040", 160, k=0.2)]),
    "heros": dict(decor="cieux", tenue=T("armure_or", gi="2c4373", sleeve="c9a24a"), arme="epee", metal="fffaf0", fx=[fx_cape("2c4373"), fx_aura("ffffff", 160, k=0.4), fx_etincelles("ffffff", 0.99)]),
    "fleau": dict(decor="volcan", tenue=T("armure_noire", inner="ff7a2a"), arme="fleau", large=True, metal="3a3a40", fx=[fx_sol_flammes(), fx_cranes(), fx_yeux("ffb040", 254), fx_cornes("1a0a0a")]),
    "sang_dragon": dict(decor="volcan", tenue=T("armure_rouge", sleeve="6a1a10", gi="8a2c1a", inner="ffb040"), arme="epee", metal="ffb040",
                        fx=[fx_dragon("1a0606"), fx_ailes("6a1a10", "2a0a0a", "demon"), fx_aura("ff7a2a", 150, k=0.4), fx_yeux("ffd04a", 254)]),
    "DivineApotre": dict(decor="cieux", tenue=T("lumiere"), arme="lance", metal="fff4c4", fx=[fx_ailes("fffaf0", "e0c88a"), fx_halo(), fx_aura("ffffff", 170, k=0.5), fx_etincelles("fff4c4", 0.985)]),
    # ---------------------------------------------------------------- MAGE
    "elementaliste": dict(decor="montagne", tenue=T("robe", sleeve="2c4373", hood="1f3050", gi="2c4373"), arme="baton", capuche=True, orbe="cfe8ff",
                          fx=[fx_eclairs("8fd0ff", ((40, 30), (70, 110), (50, 150), (80, 240))), fx_flammes_mains("ff7a2a", 110, 330)]),
    "enchanteur": dict(decor="bibliotheque", tenue=T("robe", sleeve="2f6a6a", hood="1f4a4a", gi="2f6a6a"), arme="grimoire", capuche=True, orbe="8affd8", fx=[fx_etincelles("8affd8", 0.992, (100, 460))]),
    "necromancien": dict(decor="cimetiere", tenue=T("squelette", skin="b8c0b0"), arme="baton", capuche=True, orbe="5ac88a", fx=[fx_cranes(), fx_esprits("5ac88a")]),
    "invocateur": dict(decor="nuit", tenue=T("robe", sleeve="5a2a7a", gi="5a2a7a"), arme="grimoire", capuche=True, orbe="c8a0ff", fx=[fx_cercle("c8a0ff"), fx_esprits("c8a0ff", ((60, 380), (280, 380), (90, 230), (250, 220)))]),
    "guerisseur": dict(decor="foret", tenue=T("lumiere", sleeve="e6f2e0", gi="e6f2e0", inner="7ab84a"), arme="baton", orbe="b8ffb0", fx=[fx_aura("b8ffb0", 150, k=0.35), fx_feuilles("b8ffb0", 14)]),
    "pyromancien": dict(decor="volcan", tenue=T("robe", sleeve="8a2c1a", hood="6a1a10", gi="8a2c1a", inner="ffb040", hakama="5a1410"), arme="baton", capuche=True, orbe="ffb040", fx=[fx_flammes_mains("ff7a2a", 110, 330), fx_sol_flammes()]),
    "cryomancien": dict(decor="glace", tenue=T("robe", sleeve="4a8ac8", hood="2a5a9a", gi="4a8ac8", inner="cfe8ff"), arme="baton", capuche=True, orbe="cfe8ff", fx=[fx_neige(), fx_cristaux("cfe8ff")]),
    "archimage": dict(decor="bibliotheque", tenue=T("robe", sleeve="2c4373", gi="2c4373", inner="e0b84a"), arme="baton", barbe=True, orbe="8fd0ff", fx=[fx_cercle("8fd0ff"), fx_etincelles("ffffff", 0.993)]),
    "sorcier": dict(decor="marais", tenue=T("robe", sleeve="3a1a3a", hood="2a0a2a", gi="3a1a3a", inner="7ad84a"), arme="grimoire", capuche=True, orbe="7ad84a", fx=[fx_brume("5a8a4a"), fx_yeux("7ad84a", 255)]),
    "druide": dict(decor="foret", tenue=T("foret", sleeve="5a4a2a", gi="4a6a2a"), arme="baton", barbe=True, orbe="b8ff8a", fx=[fx_racines(), fx_feuilles()]),
    "illusionniste": dict(decor="nuit", tenue=T("robe", sleeve="8a2c8a", hood="5a1a5a", gi="8a2c8a"), arme="aucune", capuche=True, fx=[fx_double(c="c86ac8"), fx_masque("efe6cf"), fx_etincelles("f2a8f0", 0.99)]),
    "mystique": dict(decor="cosmos", tenue=T("robe", sleeve="3a2a6a", gi="3a2a6a"), arme="sceptre", capuche=True, orbe="c8b0ff", fx=[fx_cercle("c8b0ff", 100, 300)]),
    "thaumaturge": dict(decor="cieux", tenue=T("lumiere", sleeve="e0c88a", gi="f2e6c4"), arme="sceptre", orbe="ffe08a", fx=[fx_aura("ffe08a", 160, k=0.4), fx_etincelles("ffffff", 0.988)]),
    "alchimiste": dict(decor="bibliotheque", tenue=T("cuir", gi="5a4a2a", inner="c9b27a"), arme="aucune", barbe=True, fx=[fx_fioles(), fx_bulles("7ad84a", 8)]),
    "sage": dict(decor="montagne", tenue=T("robe", sleeve="efe6cf", hood="d8d0c0", gi="efe6cf", inner="2c4373", hair="ffffff", beard="ffffff"), arme="baton", barbe=True, orbe="ffffff", fx=[fx_aura("ffffff", 140, k=0.25)]),
    "magus": dict(decor="cosmos", tenue=T("robe", sleeve="2a1a5a", gi="2a1a5a", inner="e0b84a"), arme="baton", barbe=True, orbe="e0b0ff", fx=[fx_cercle("e0b0ff"), fx_eclairs("e0b0ff", ((280, 30), (260, 120), (290, 170), (260, 260)))]),
    "liche": dict(decor="cimetiere", tenue=T("squelette", sleeve="1a1424", inner="8affd8"), arme="baton", capuche=True, orbe="8affd8", fx=[fx_couronne("5a5a6a", "8affd8"), fx_esprits("8affd8"), fx_yeux("8affd8", 254)]),
    "oracle": dict(decor="nuit", tenue=T("robe", sleeve="efe6cf", hood="c9c0b0", gi="efe6cf", inner="e0b84a"), arme="aucune", capuche=True, fx=[fx_oeil("8fd0ff"), fx_etincelles("8fd0ff", 0.99)]),
    "maitre_elementaire": dict(decor="volcan", tenue=T("robe", sleeve="5a2a2a", gi="2c4373"), arme="baton", barbe=True, orbe="ffffff",
                               fx=[fx_eclairs(), fx_flammes_mains("ff7a2a", 110, 330), fx_neige(), fx_cristaux("8fd0ff", ((300, 380), (40, 400)))]),
    "chronoturge": dict(decor="cosmos", tenue=T("robe", sleeve="6a5a2a", gi="6a5a2a", inner="e0b84a"), arme="sceptre", capuche=True, orbe="ffe08a", fx=[fx_sablier(), fx_cercle("e0b84a", 110, 300)]),
    "archonte": dict(decor="cieux", tenue=T("armure_or", sleeve="efe6cf", gi="efe6cf"), arme="sceptre", orbe="ffffff", fx=[fx_couronne(), fx_aura("ffe08a", 170, k=0.45), fx_cercle("e0b84a")]),
    "mage_void": dict(decor="neant", tenue=T("robe", sleeve="1a0a2a", hood="0a0518", gi="1a0a2a", face="000000"), arme="baton", capuche=True, orbe="b06aff", fx=[fx_yeux("c88aff", 255)]),
    "primordial": dict(decor="caverne", tenue=T("pierre", sleeve="4a3a5a", gi="5a4a6a", inner="c85a9a"), arme="baton", barbe=True, orbe="c85a9a", fx=[fx_racines("3a2a3a"), fx_cristaux("c85a9a")]),
    "demiurge": dict(decor="cosmos", tenue=T("lumiere", sleeve="e0c88a"), arme="aucune", fx=[fx_aura("ffffff", 160, k=0.5), fx_cercle("ffffff", 130, 300), fx_etincelles("ffffff", 0.985)]),
    "prophete": dict(decor="desert", tenue=T("robe", sleeve="efe6cf", hood="d8c8a0", gi="efe6cf", inner="b8322c"), arme="baton", capuche=True, orbe="ffe08a", fx=[fx_oeil("ffe08a", cy=90), fx_aura("ffe08a", 140, k=0.3)]),
    "avatar_arcanique": dict(decor="neant", tenue=T("lumiere", sleeve="b8a0ff", gi="c8b0ff", inner="ffffff", skin="d8c8ff"), arme="aucune",
                             fx=[fx_aura("b8a0ff", 170, k=0.6), fx_cercle("ffffff"), fx_ailes("c8b0ff", "ffffff", "ombre"), fx_yeux("ffffff", 254)]),
    "dieu_des_arcanes": dict(decor="cosmos", tenue=T("lumiere", sleeve="2a1a5a", gi="2a1a5a", inner="e0b84a"), arme="baton", barbe=True, orbe="ffffff",
                             fx=[fx_couronne("e0b84a", "8fd0ff"), fx_halo("8fd0ff"), fx_aura("8fd0ff", 170, k=0.5), fx_etincelles("ffffff", 0.985)]),
    # ---------------------------------------------------------------- ARCHER
    "tireur": dict(decor="foret", tenue=T("foret"), arme="arc", capuche=True),
    "chasseur_archer": dict(decor="foret_nuit", tenue=T("cuir", hood="4a3420"), arme="arc", capuche=True, fx=[fx_yeux("ffd04a", 255, 4)]),
    "arbaletrier": dict(decor="rempart", tenue=T("armure", sleeve="6a5a4a", gi="7a6a5a"), arme="arbalete"),
    "eclaireur_archer": dict(decor="montagne", tenue=T("foret", sleeve="6a7a5a", hood="4a5a3a", gi="6a7a5a"), arme="arc", capuche=True, fx=[fx_echarpe("b8322c")]),
    "franc_tireur": dict(decor="desert", tenue=T("cuir", hood="8a6a4a", gi="8a6a4a"), arme="arc", capuche=True, fx=[fx_echarpe("efe6cf")]),
    "ranger_elite": dict(decor="foret", tenue=T("foret", sleeve="2f4a2c", hood="1f3a1c", gi="2f4a2c", inner="c9a24a"), arme="arc", capuche=True, fx=[fx_cape("1f3a1c"), fx_feuilles()]),
    "arc_mystique": dict(decor="nuit", tenue=T("robe", sleeve="3a4a7a", hood="2a3a6a", gi="3a4a7a"), arme="arc", capuche=True, fleche="b8a0ff", pointe="ffffff", bois="4a2a7a", fx=[fx_etincelles("b8a0ff", 0.99)]),
    "maitre_chasse": dict(decor="foret_nuit", tenue=T("cuir", sleeve="5a4a3a", gi="6b4a2a"), arme="arc", barbe=True, fx=[fx_cranes("d8d0c0", ((60, 500), (280, 505))), fx_yeux("ffd04a", 255, 4)]),
    "sniper_divin": dict(decor="cieux", tenue=T("lumiere", hood="f2e6c4"), arme="arc", capuche=True, fleche="ffe08a", pointe="ffffff", bois="c9a24a", fx=[fx_halo()]),
    "tireur_elementaire": dict(decor="montagne", tenue=T("foret", sleeve="2c4373", hood="1f3050", gi="2c4373"), arme="arc", capuche=True, fleche="ff7a2a", pointe="ffd04a",
                               fx=[fx_eclairs("8fd0ff", ((290, 30), (270, 110), (300, 160), (270, 230)))]),
    "general_archer": dict(decor="champ", tenue=T("armure", gi="2f4a2c", sleeve="5a6a5a"), arme="arc", fx=[fx_fleches("1a1418", 26), fx_banniere("2f4a2c", "e0b84a"), fx_cape("2f4a2c")]),
    "oeil_de_faucon": dict(decor="montagne", tenue=T("cuir", hood="5a4a3a"), arme="arc", capuche=True, fx=[fx_oeil("e0b84a", 250, 90)]),
    "arc_ancien": dict(decor="caverne", tenue=T("foret", sleeve="4a3a5a", hood="3a2a4a", gi="4a3a5a"), arme="arc", capuche=True, fleche="c85a9a", pointe="ffffff", bois="2a1a14", fx=[fx_cristaux("c85a9a"), fx_racines("2a1a14")]),
    "avatar_arc": dict(decor="neant", tenue=T("ombre", sleeve="2a2a4a", hood="1a1a3a", inner="8fd0ff"), arme="arc", capuche=True, fleche="8fd0ff", pointe="ffffff", bois="8fd0ff",
                       fx=[fx_fleches("8fd0ff", 22, 11), fx_yeux("8fd0ff", 255, 4)]),
    "dieu_arc": dict(decor="cieux", tenue=T("armure_or", hood="e0b84a"), arme="arc", fleche="ffffff", pointe="ffe08a", bois="e0b84a",
                     fx=[fx_ailes("fffaf0", "e0c88a"), fx_aura("ffffff", 160, k=0.45), fx_fleches("ffe08a", 18, 13)]),
    # ---------------------------------------------------------------- ASSASSIN
    "voleur": dict(decor="ruelle", tenue=T("cuir", hood="3a3a42", gi="3a3a42"), arme="dague", capuche=True, fx=[fx_pieces(n=8, seed=4)]),
    "rodeur": dict(decor="foret_nuit", tenue=T("cuir", hood="2a3a2a", gi="2a3a2a"), arme="dague", capuche=True, fx=[fx_brume("3a5a4a")]),
    "sicaire": dict(decor="toits", tenue=T("ombre", inner="2a2a34"), arme="dague", capuche=True, fx=[fx_yeux("efe6cf", 256)]),
    "eclaireur": dict(decor="montagne", tenue=T("cuir", hood="6a6a5a", gi="6a6a5a"), arme="dague", capuche=True, double=False, fx=[fx_echarpe("2c4373")]),
    "empoisonneur": dict(decor="marais", tenue=T("ombre", sleeve="1a2a14", hood="14200e", gi="1a2a14", inner="7ad84a"), arme="dague", capuche=True, metal="7ad84a", fx=[fx_bulles()]),
    "saboteur": dict(decor="ruelle", tenue=T("cuir", gi="4a3a2a"), arme="dague", capuche=True, fx=[fx_fioles(((60, 440, "3a3a40"), (90, 445, "3a3a40"))), fx_flammes_mains("ff7a2a", 75, 425)]),
    "acrobate": dict(decor="toits", tenue=T("cuir", gi="8a1c26", sleeve="2a1a20", hood="2a1a20"), arme="dague", capuche=True, fx=[fx_echarpe("e0b84a")]),
    "maitre_lame": dict(decor="toits", tenue=T("ombre", inner="c8ced8"), arme="dague", capuche=True, metal="ffffff", fx=[fx_etincelles("ffffff", 0.994, (200, 420))]),
    "ombre_furtive": dict(decor="nuit", tenue=T("ombre"), arme="dague", capuche=True, fx=[fx_ombres("05030a", 0.7), fx_yeux("8fd0ff", 256)]),
    "chasseur": dict(decor="desert", tenue=T("cuir", hood="6b4a2a"), arme="arbalete", capuche=True, fx=[fx_echarpe("8a1c26"), fx_pieces(n=6, seed=8)]),
    "ninja": dict(decor="japon", tenue=T("ombre", sleeve="1a1a2a", hood="12121e", gi="1a1a2a", inner="b8322c"), arme="katana", capuche=True, fx=[fx_echarpe("b8322c")]),
    "espion": dict(decor="ruelle", tenue=T("riche", sleeve="2a2a3a", gi="2a2a3a", hood="1a1a2a", inner="8a1c26"), arme="dague", double=False, fx=[fx_masque("1a1a1a"), fx_cape("1a1a2a")]),
    "corsaire": dict(decor="mer", tenue=T("pirate"), arme="rapiere", barbe=True, fx=[fx_echarpe("b8322c"), fx_pieces(n=8, seed=6)]),
    "traqueur": dict(decor="foret_nuit", tenue=T("cuir", hood="3a2a1a"), arme="dague", capuche=True, fx=[fx_yeux("ffd04a", 256)]),
    "lame_noire": dict(decor="toits", tenue=T("ombre", inner="05030a"), arme="dague", capuche=True, metal="2a2a34", fx=[fx_yeux("ff3a3a", 256), fx_echarpe("05030a")]),
    "maitre_ombre": dict(decor="neant", tenue=T("ombre"), arme="dague", capuche=True, fx=[fx_ombres("000000", 0.8), fx_double(c="1a1018", k=0.6), fx_yeux("c88aff", 256)]),
    "faucheur": dict(decor="cimetiere", tenue=T("squelette", sleeve="1a1424", gi="1a1424"), arme="faux", capuche=True, fx=[fx_cranes(), fx_yeux("ff4a4a", 255)]),
    "phantom_blade": dict(decor="nuit", tenue=T("ombre", sleeve="2a3a5a", hood="1a2a4a", gi="2a3a5a", inner="8fd0ff"), arme="katana", capuche=True, metal="cfe8ff",
                          fx=[fx_double(c="8fd0ff", k=0.3), fx_aura("8fd0ff", 130, k=0.3)]),
    "grand_maitre_poison": dict(decor="marais", tenue=T("ombre", sleeve="1a3a14", hood="14280e", gi="1a3a14", inner="7ad84a"), arme="dague", capuche=True, metal="7ad84a",
                                fx=[fx_bulles(n=20), fx_fioles(((60, 440, "7ad84a"), (90, 445, "a0ff6a"), (275, 440, "5ac88a")))]),
    "ange_dechu": dict(decor="toits", tenue=T("ombre", sleeve="2a2a34", hood="1a1a24"), arme="dague", capuche=True, fx=[fx_ailes("1a1a24", "4a4a5a", "ange"), fx_halo("6a6a7a")]),
    "spectre_assassin": dict(decor="cimetiere", tenue=T("ombre", sleeve="3a5a6a", hood="2a4a5a", gi="3a5a6a", face="8affd8"), arme="dague", capuche=True, metal="8affd8",
                             fx=[fx_aura("8affd8", 130, k=0.35), fx_esprits("8affd8")]),
    "seigneur_poison": dict(decor="marais", tenue=T("riche", sleeve="1a3a14", gi="1a3a14", inner="7ad84a", hood="14280e", face="5a8a4a"), arme="sceptre", capuche=True, orbe="7ad84a",
                            fx=[fx_couronne("5a8a4a", "7ad84a"), fx_bulles(n=24, seed=4), fx_brume("5ac88a")]),
    "lame_eternelle": dict(decor="nuit", tenue=T("ombre", inner="ffe08a"), arme="katana", capuche=True, metal="ffe08a", fx=[fx_aura("ffe08a", 130, k=0.3), fx_etincelles("ffe08a", 0.992)]),
    "ombre_supreme": dict(decor="neant", tenue=T("ombre", face="000000"), arme="dague", capuche=True, fx=[fx_ombres("000000", 0.9), fx_ailes("05030a", "2a1a3a", "ombre"), fx_yeux("ffffff", 256)]),
    "empereur_ombres": dict(decor="toits", tenue=T("ombre", inner="c9a24a"), arme="dague", capuche=True,
                            fx=[fx_couronne("2a2a34", "8a1c26"), fx_cape("05030a"), fx_double(c="05030a", k=0.7), fx_yeux("ff4a4a", 256)]),
    "avatar_neant": dict(decor="neant", tenue=T("ombre", sleeve="1a0a2a", gi="1a0a2a", face="000000"), arme="dague", capuche=True, metal="c88aff",
                         fx=[fx_portail(), fx_aura("7a2ac8", 160, k=0.5), fx_yeux("c88aff", 256)]),
    "dieu_ombres": dict(decor="neant", tenue=T("ombre", face="000000"), arme="faux", capuche=True, metal="c8ced8",
                        fx=[fx_ailes("05030a", "3a2a4a", "demon"), fx_couronne("1a1a24", "ff2a2a"), fx_ombres("000000", 0.9), fx_yeux("ff2a2a", 255)]),
    # ---------------------------------------------------------------- MARCHAND
    "negociant": dict(decor="marche", tenue=T("riche"), arme="bourse", fx=[fx_pieces(n=10, seed=1)]),
    "caravanier": dict(decor="desert", tenue=T("riche", sleeve="8a6a3a", gi="8a6a3a", hood="6a4a2a"), arme="lance", capuche=True, metal="c8ced8", fx=[fx_echarpe("b8322c")]),
    "prospecteur": dict(decor="caverne", tenue=T("cuir", gi="6a5a3a"), arme="pioche", barbe=True, fx=[fx_pieces(n=12, seed=3), fx_cristaux("f2c45e")]),
    "master_artisan": dict(decor="forge", tenue=T("cuir", gi="5a3a22", inner="8a6a4a"), arme="marteau", barbe=True, large=True, metal="5a5a60", fx=[fx_etincelles("ffd08a", 0.99, (200, 460))]),
    "relic_hunter": dict(decor="caverne", tenue=T("cuir", hood="8a6a4a", gi="8a6a4a"), arme="aucune", fx=[fx_cristaux("e0b84a"), fx_pieces(n=8, seed=9)]),
    "smuggler": dict(decor="mer", tenue=T("pirate", gi="2a2a3a", sleeve="3a3a4a"), arme="dague", double=False, capuche=True, fx=[fx_pieces(n=6, seed=12)]),
    "banquier": dict(decor="tresor", tenue=T("riche", sleeve="2a2a4a", gi="2a2a4a", inner="efe6cf"), arme="bourse", fx=[fx_pieces(n=16, seed=14, pluie=True)]),
    "black_market_prince": dict(decor="ruelle", tenue=T("riche", sleeve="1a1a24", gi="1a1a24", inner="8a1c26", hood="1a1a24"), arme="bourse", capuche=True, fx=[fx_masque("c9a24a"), fx_pieces(n=8, seed=15)]),
    "gold_baron": dict(decor="tresor", tenue=T("riche", sleeve="8a1c26", gi="8a1c26", inner="f2c45e"), arme="sceptre", orbe="f2c45e", barbe=True, fx=[fx_couronne(), fx_pieces(n=20, seed=16, pluie=True)]),
    "guild_master": dict(decor="marche", tenue=T("riche", sleeve="2c4373", gi="2c4373"), arme="bourse", barbe=True, fx=[fx_banniere("2c4373", "f2c45e"), fx_cape("2c4373")]),
    "prosperity_avatar": dict(decor="cieux", tenue=T("armure_or", sleeve="f2c45e"), arme="bourse", fx=[fx_aura("f2c45e", 170, k=0.5), fx_pieces(n=30, seed=17, pluie=True), fx_halo()]),
    "underworld_king": dict(decor="ruelle", tenue=T("riche", sleeve="1a1018", gi="1a1018", inner="8a1c26"), arme="sceptre", orbe="ff3a3a", barbe=True,
                            fx=[fx_couronne("3a3a40", "8a1c26"), fx_cape("3a0a10"), fx_ombres("05030a", 0.5)]),
    "world_forger": dict(decor="forge", tenue=T("pierre", inner="ffb040"), arme="marteau", barbe=True, large=True, metal="ffb040",
                         fx=[fx_aura("ff7a2a", 150, k=0.35), fx_etincelles("ffd08a", 0.985), fx_fissures("ffb040")]),
    "marchand_divin": dict(decor="cieux", tenue=T("lumiere", sleeve="e0c88a", inner="f2c45e"), arme="bourse", barbe=True,
                           fx=[fx_ailes("fffaf0", "e0c88a"), fx_halo(), fx_pieces(n=24, seed=18, pluie=True)]),
    "roi_commerce": dict(decor="tresor", tenue=T("armure_or", sleeve="8a1c26", gi="8a1c26"), arme="sceptre", orbe="f2c45e", barbe=True,
                         fx=[fx_couronne(), fx_cape("8a1c26"), fx_aura("f2c45e", 170, k=0.45), fx_pieces(n=26, seed=19, pluie=True)]),    # ---------------------------------------------------------------- CLERC (2026-11-26, lignees du Mage)
    "acolyte": dict(decor="cathedrale", tenue=T("robe", sleeve="d8d0c0", hood="c9c0b0", gi="efe6cf", inner="e0b84a", hakama="c9c0b0"), arme="baton", capuche=True, orbe="ffe08a"),
    "exorciste": dict(decor="cimetiere", tenue=T("robe", sleeve="2a2a3a", hood="1a1a2a", gi="2a2a3a", inner="efe6cf", hakama="1a1a2a"), arme="grimoire", couverture="efe6cf", orbe="fff4c4", fx=[fx_esprits("ff6a5a"), fx_cercle("fff4c4")]),
    "hospitalier": dict(decor="rempart", tenue=T("armure", gi="efe6cf", inner="b8322c"), arme="masse", fx=[fx_bouclier("efe6cf", "b8322c", forme="croix"), fx_aura("b8ffb0", 120, k=0.25)]),
    "pretre_de_guerre": dict(decor="champ", tenue=T("armure_or", gi="efe6cf", sleeve="9aa4b2"), arme="fleau", metal="e0b84a", fx=[fx_bouclier("efe6cf", "e0b84a", forme="soleil"), fx_halo()]),
    "gardien_du_sanctuaire": dict(decor="cathedrale", tenue=T("lumiere", sleeve="9aa4b2", gi="c8ced8"), arme="baton", orbe="ffe08a", fx=[fx_cercle("ffe08a"), fx_bouclier("e0b84a", "efe6cf", forme="soleil")]),
    "augure": dict(decor="montagne", tenue=T("robe", sleeve="efe6cf", hood="2c4373", gi="efe6cf", inner="2c4373", hakama="2c4373"), arme="baton", capuche=True, orbe="cfe8ff", fx=[fx_bete("oiseau", "efe6cf"), fx_bete("oiseau", "d8d0c0", ox=-120)]),
    "hierophante": dict(decor="cathedrale", tenue=T("lumiere", sleeve="8a1c26", gi="efe6cf", inner="e0b84a"), arme="sceptre", orbe="ffe08a", fx=[fx_couronne("e0b84a", "efe6cf"), fx_aura("ffe08a", 150, k=0.4)]),
    "martyr": dict(decor="champ", tenue=T("lumiere", sleeve="d8d0c0", gi="d8d0c0", inner="b8322c"), arme="masse", fx=[fx_halo("ff8a6a"), fx_aura("ff8a6a", 150, k=0.35), fx_sol_flammes("ffb040", "fff4c4", 50)]),
    "heraut_celeste": dict(decor="cieux", tenue=T("armure_or", sleeve="2c4373", gi="2c4373"), arme="lance", metal="fff4c4", fx=[fx_ailes("fffaf0", "c9a24a"), fx_banniere("2c4373", "f2c45e")]),
    "ange_gardien": dict(decor="nuit", tenue=T("lumiere", sleeve="c8ced8", gi="efe6cf"), arme="epee", metal="fff4c4", fx=[fx_ailes("fffaf0", "c8ced8"), fx_halo(), fx_bouclier("efe6cf", "e0b84a", forme="soleil")]),
    "grand_pontife": dict(decor="cathedrale", tenue=T("lumiere", sleeve="efe6cf", gi="efe6cf", inner="e0b84a"), arme="sceptre", barbe=True, orbe="ffffff", fx=[fx_couronne("efe6cf", "e0b84a"), fx_aura("ffffff", 170, k=0.45), fx_etincelles("fff4c4", 0.99)]),
    "avatar_lumiere": dict(decor="cieux", tenue=T("lumiere", skin="fff4d6", sleeve="fff4c4", gi="fffaf0"), arme="baton", orbe="ffffff", fx=[fx_ailes("fffaf0", "e0b84a"), fx_halo(), fx_aura("ffffff", 180, k=0.55), fx_yeux("ffffff", 254)]),
    # ---------------------------------------------------------------- DOMPTEUR (lignees du Marchand)
    "maitre_chien": dict(decor="champ", tenue=T("cuir", gi="6b4a2a"), arme="lance", barbe=True, fx=[fx_bete("loup", "3a3028"), fx_bete("loup", "5a4a3a", flip=True)]),
    "fauconnier": dict(decor="montagne", tenue=T("cuir", gi="5a4a3a", hood="4a3a2a"), arme="arc", capuche=True, fx=[fx_bete("oiseau", "6b4a2a", ox=-150), fx_bete("oiseau", "8a6a4a", ox=-60)]),
    "chevaucheur": dict(decor="desert", tenue=T("cuir", gi="8a6a3a", hood="6a4a2a"), arme="lance", capuche=True, fx=[fx_bete("cheval", "6b4a2a")]),
    "charmeur_de_serpents": dict(decor="desert", tenue=T("riche", sleeve="2f6a6a", gi="2f6a6a", inner="e0b050", hood="1f4a4a"), arme="dague", double=False, capuche=True, fx=[fx_bete("serpent", "4a7a34"), fx_bete("serpent", "7a8a34", flip=True)]),
    "dresseur_d_ours": dict(decor="foret", tenue=T("cuir", gi="6b4a2a", skin="d8a878"), arme="masse", barbe=True, large=True, metal="6b4a2a", fx=[fx_bete("ours", "4a3020")]),
    "belluaire": dict(decor="arene", tenue=T("cuir", gi="8a1c26", skin="d8a878"), arme="lance", fx=[fx_bete("ours", "c58a3a"), fx_bete("loup", "b8823a", flip=True)]),
    "seigneur_des_loups": dict(decor="montagne", tenue=T("cuir", gi="5a6a7a", sleeve="8a8a90", hood="6a6a70"), arme="lance", capuche=True, fx=[fx_bete("loup", "dcdce4"), fx_bete("loup", "a8a8b0", flip=True), fx_neige()]),
    "metamorphe": dict(decor="foret_nuit", tenue=T("cuir", gi="3a4a2a", skin="b8a078"), arme="aucune", fx=[fx_bete("ours", "2a2018"), fx_yeux("ffd04a", 254), fx_double(c="3a2a1a", k=0.5)]),
    "chevaucheur_de_wyverne": dict(decor="montagne", tenue=T("armure", gi="2f4a5a"), arme="lance", fx=[fx_dragon("2a3a4a"), fx_cape("2f4a5a")]),
    "gardien_des_betes_sacrees": dict(decor="foret", tenue=T("lumiere", sleeve="7ab84a", gi="e6f2e0"), arme="lance", metal="fff4c4", fx=[fx_bete("cheval", "f6f0e0"), fx_aura("b8ffb0", 140, k=0.3)]),
    "roi_des_betes": dict(decor="foret", tenue=T("cuir", gi="8a6a3a", inner="e0b84a", hair="c58643", beard="c58643"), arme="masse", barbe=True, large=True, metal="8a6a3a", fx=[fx_couronne("c58643", "4a7a34"), fx_bete("ours", "4a3020"), fx_bete("loup", "6a6058", flip=True)]),
    "lien_primordial": dict(decor="volcan", tenue=T("cuir", gi="3a2a1a", inner="ffb040"), arme="lance", metal="ffb040", fx=[fx_dragon("1a0606"), fx_bete("ours", "1a0e0a"), fx_aura("ff7a2a", 150, k=0.35), fx_yeux("ffd04a", 254)]),
    # ---------------------------------------------------------------- BARDE (lignees du Mage)
    "menestrel": dict(decor="marche", tenue=T("riche", sleeve="2c4373", gi="2c4373", inner="e0b050"), arme="luth", fx=[fx_luth(), fx_notes("8a1c26")]),
    "jongleur": dict(decor="arene", tenue=T("riche", sleeve="b8322c", gi="e0b050", inner="b8322c"), arme="dague", fx=[fx_pieces(c="dfe5ee", n=6, seed=21, pluie=True)]),
    "conteur": dict(decor="bibliotheque", tenue=T("robe", sleeve="6b4a2a", hood="5a3a22", gi="6b4a2a", inner="e0b050", hakama="5a3a22"), arme="grimoire", barbe=True, couverture="2c4373", orbe="ffe08a"),
    "skalde": dict(decor="montagne", tenue=T("cuir", gi="5a6a7a", hair="c58643", beard="c58643"), arme="epee", barbe=True, fx=[fx_cape("2c4373"), fx_notes("efe6cf"), fx_neige()]),
    "virtuose": dict(decor="cosmos", tenue=T("riche", sleeve="5a2a6a", gi="5a2a6a", inner="e0b050"), arme="luth", fx=[fx_luth("c9a24a"), fx_notes("c8b0ff"), fx_sablier("c8b0ff", 280, 130)]),
    "charmeur": dict(decor="nuit", tenue=T("riche", sleeve="8a2c6a", gi="8a2c6a", inner="f2a8c0"), arme="aucune", fx=[fx_masque("f2a8c0"), fx_notes("f2a8c0"), fx_double(c="c86ac8", k=0.3)]),
    "maitre_chanteur": dict(decor="cathedrale", tenue=T("riche", sleeve="2c4373", gi="2c4373", inner="efe6cf"), arme="grimoire", couverture="c9a24a", orbe="ffffff", fx=[fx_notes("ffffff"), fx_aura("cfe8ff", 140, k=0.3)]),
    "danseur_de_guerre": dict(decor="desert", tenue=T("riche", sleeve="b8322c", gi="e0b050", inner="b8322c", hood="b8322c"), arme="katana", metal="dfe5ee", fx=[fx_echarpe("b8322c"), fx_double(c="e0b050", k=0.3)]),
    "aede": dict(decor="cieux", tenue=T("lumiere", sleeve="2c4373", gi="efe6cf"), arme="luth", fx=[fx_luth("e0b84a"), fx_notes("c9a24a"), fx_halo()]),
    "voix_des_enfers": dict(decor="cimetiere", tenue=T("ombre", sleeve="2a2034", hood="1a1424", inner="8affd8"), arme="luth", capuche=True, fx=[fx_luth("3a3040"), fx_esprits("8affd8"), fx_notes("8affd8")]),
    "barde_legendaire": dict(decor="champ", tenue=T("armure_or", sleeve="8a1c26", gi="8a1c26"), arme="luth", fx=[fx_luth("e0b84a"), fx_cape("8a1c26"), fx_notes("f2c45e"), fx_banniere("8a1c26", "f2c45e")]),
    "chant_primordial": dict(decor="cosmos", tenue=T("lumiere", sleeve="c8b0ff", gi="fffaf0"), arme="luth", fx=[fx_luth("ffffff"), fx_aura("c8b0ff", 170, k=0.5), fx_notes("ffffff"), fx_etincelles("ffffff", 0.988)]),
}

# effets derriere le personnage (le reste passe devant)
BEHIND = {"fx_bete", "fx_aura", "fx_ailes", "fx_cape", "fx_dragon", "fx_portail", "fx_double", "fx_banniere", "fx_oeil", "fx_cercle", "fx_brume", "fx_sol_flammes"}


def layers(cid, spec):
    fx = spec.get("fx", [])
    back = [f for f in fx if f.__qualname__.split(".")[0] in BEHIND]
    front = [f for f in fx if f not in back]
    return BACKDROPS[spec["decor"]], back, figure(spec), front


# =====================================================================================================================
# RENDU (meme vitrail que gen_chapitres_ui.paint_art)
# =====================================================================================================================

def render(cid, mask):
    spec = FICHES[cid]
    bg, back, fig, front = layers(cid, spec)
    seed = sum(map(ord, cid)) * 31
    rr = random.Random(seed)
    cache = {}

    def rnd(a, b):
        k = (a, b)
        if k not in cache: cache[k] = random.Random(a * 7919 + b * 104729 + seed).random()
        return cache[k]
    G = 30
    gw, gh = AW // G + 2, AH // G + 2
    seeds = {(i, j): ((i + rr.random()) * G, (j + rr.random()) * G, 0.9 + rr.random() * 0.2) for i in range(gw) for j in range(gh)}
    img = blank(AW, AH)
    for y in range(AH):
        for x in range(AW):
            a = mask[y][x]
            if a == 0: continue
            col = bg(x, y, x / AW, y / AH, rnd)
            for f in back: col = f(x, y, col, rnd) if getattr(f, "rnd", False) else f(x, y, col)
            col = fig(x, y, col)
            for f in front: col = f(x, y, col, rnd) if getattr(f, "rnd", False) else f(x, y, col)
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
            if lead < 1.6: col = mix(col, C("1b1420"), 0.75 * (1.6 - lead) / 1.6 + 0.25)
            vig = math.hypot((x / AW - 0.5) * 1.2, y / AH - 0.5)
            col = mix(col, C("120e18"), max(0.0, vig - 0.42) * 1.2)
            img[y][x] = [max(0, min(255, int(col[0]))), max(0, min(255, int(col[1]))), max(0, min(255, int(col[2]))), a]
    save_small(cid, img)
    return img


def save_small(cid, img):
    """Une seule image par classe, en DEMI-resolution (2026-11-26) : l'atlas des textures d'interface du client est plein
    (~67 Mpx ; 606 images refusees, croix rouges, avec 2 x 334x604 par classe). 155 classes x 167x302 = ~7,8 Mpx.
    La variante grisee n'est plus un fichier : write_ui teinte la meme texture."""
    w, h = AW // 2, AH // 2
    small = blank(w, h)
    for y in range(h):
        for x in range(w):
            px = [img[2 * y + dy][2 * x + dx] for dy in (0, 1) for dx in (0, 1)]
            a = sum(p[3] for p in px)
            if a == 0: continue
            small[y][x] = [int(sum(p[i] * p[3] for p in px) / a) for i in range(3)] + [a // 4]
    png.write(os.path.join(OUT, f"Art_{cid}.png"), w, h, small)


GREY_TINT = "#8c93a6"          # carte non choisie : meme texture, teintee (remplace les anciens Art_<id>G@2x.png)


def write_ui(cid, tex, grey_tex=None):
    """tex : texture de la carte choisie ; grey_tex : texture grisee existante (bases), sinon tex teintee."""
    for k in ("C", "G"):
        if k == "C": bg = f'(TexturePath: "{tex}")'
        elif grey_tex: bg = f'(TexturePath: "{grey_tex}")'
        else: bg = f'(TexturePath: "{tex}", Color: {GREY_TINT})'
        open(os.path.join(OUT, f"Art_{cid}{k}.ui"), "w").write(
            f"// Illustration de la classe {cid} (genere par scripts/classes/gen_class_art.py)\n"
            f"Group {{ Anchor: (Left: 0, Top: 0, Width: {ART_W}, Height: {ART_H}); Background: {bg}; }}\n")


def planche(ids, path, imgs):
    """Planche de controle : vignettes au quart, 8 par ligne."""
    tw, th = AW // 3, AH // 3
    cols = 8
    rows = (len(ids) + cols - 1) // cols
    sheet = [[[20, 16, 24, 255] for _ in range(cols * tw)] for _ in range(rows * th)]
    for n, cid in enumerate(ids):
        img = imgs[cid]
        ox, oy = (n % cols) * tw, (n // cols) * th
        for y in range(th):
            for x in range(tw):
                p = img[y * 3][x * 3]
                if p[3]: sheet[oy + y][ox + x] = [p[0], p[1], p[2], 255]
    png.write(path, cols * tw, rows * th, sheet)
    print("planche :", path)


def main():
    os.makedirs(OUT, exist_ok=True)
    catalog = json.load(open(CATALOG, encoding="utf-8"))
    ids_cat = {c["id"] for c in catalog}
    unknown = [k for k in FICHES if k not in ids_cat]
    if unknown: sys.exit(f"fiches sans classe au catalogue : {unknown}")
    if "--manquantes" in sys.argv:
        miss = [c["id"] for c in catalog if c["id"] not in FICHES and c["id"] not in BASE_ART]
        print(len(miss), "classes sans illustration propre :", ", ".join(miss))
        return
    wanted = [a for a in sys.argv[1:] if not a.startswith("--")] or list(BASE_ART) + list(FICHES)
    for cid in wanted:
        if cid in BASE_ART: write_ui(cid, f"../ChapArt{BASE_ART[cid]}C.png", f"../ChapArt{BASE_ART[cid]}G.png")
        elif cid in FICHES: write_ui(cid, f"Art_{cid}.png")
        else: sys.exit(f"pas de fiche : {cid}")
    if "--ui" in sys.argv:
        print(len(wanted), "documents .ui")
        return
    mask = g.art_mask()
    imgs = {}
    for n, cid in enumerate(c for c in wanted if c in FICHES):
        imgs[cid] = render(cid, mask)
        print(f"[{n + 1}] {cid}", flush=True)
    sheet = [a.split("=", 1)[1] for a in sys.argv if a.startswith("--planche=")]
    if sheet: planche([c for c in wanted if c in imgs], sheet[0], imgs)


if __name__ == "__main__":
    main()
