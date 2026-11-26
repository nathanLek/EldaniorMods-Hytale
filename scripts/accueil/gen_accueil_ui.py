"""Fenetre d'accueil des nouveaux joueurs (2026-11-25), facon « Chapitres » : Common/UI/Custom/Accueil/Accueil.ui et ses
illustrations, pilotee par accueil/CardsPage.java (accueil : WelcomePage ; statue des classes : classes/gui/ClassChoicePage).

Meme mise en page que la fenetre des quetes (scripts/quetes/gen_chapitres_ui.py, dont on reprend les outils de dessin, les
cadres, eclats et fonds : ../Quest/Chap*.png) :
  - bandeau Eldanior en haut (blason, nom, sous-titre) et les 4 etapes, ou un grand titre (statue) ;
  - fond propre au serveur : la cote du continent d'Eldanior a l'encre doree, quadrillage et rose des vents ;
  - a droite : carrousel de 3 cartes illustrees (vitrail), fleches et compteur si plus de 3 ; #AccArtSlot<i> recoit au
    besoin l'illustration propre de la carte (classes : Accueil/Classes/Art_<id>C|G.ui, scripts/classes/gen_class_art.py) ;
  - a gauche : embleme, titre, accroche, texte, points, passifs (effet), runes (icone), bonus, kit (icones, infobulles) ;
  - sous les cartes : Retour/Fermer, Relancer (statue), bouton principal, ligne d'aide.

Pas de race (2026-11-25) : tout le monde est humain.

Usage : python3 scripts/accueil/gen_accueil_ui.py [--ui] [--art=Mage ...] [--planche=/chemin/planche.png]
"""
import math, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, '..', 'rune_aura_icons'))
sys.path.insert(0, os.path.join(HERE, '..', 'quetes'))
_argv = sys.argv
sys.argv = [sys.argv[0]]                       # gen_chapitres_ui lit sys.argv a l'import
import gen_chapitres_ui as g                   # noqa: E402
sys.argv = _argv
from gen_chapitres_ui import (AW, AH, hexc, mix, vgrad, ridge, seg_dist, inpoly, _person, _paint)   # noqa: E402

RES = os.path.join(HERE, "..", "..", "src", "main", "resources")
OUT = os.path.join(RES, "Common", "UI", "Custom", "Accueil")

# Doit correspondre a accueil/CardsPage.java
CARDS, NOTES, STATS, STEPS, PASSIVES, RUNES, KIT = 3, 5, 6, 4, 4, 3, 14
# « Base » : cadre et eclats d'origine (gris), rarete Commune ; les autres : cadres teintes des Chapitres (2026-11-25 :
# contour des cartes de classe selon la rarete, Rare bleu, Epique violet, Unique rouge, Legendaire or, Divine cyan)
TINTS = ["Base", "Blanc", "Or", "Bleu", "Violet", "Vert", "Rouge", "Cyan"]
LOCAL_TINTS = ["Blanc"]                         # cadres et eclats generes ici (white_frames), pas dans ../Quest
NEW_ARTS = ["Royaume", "Grimoire", "Guerrier", "Mage", "Archer", "Assassin", "Marchand", "Runes", "Faille", "Guildes", "Banque"]
QUEST_ARTS = ["Dojo", "Aube", "Arene", "Rumeurs"]                       # deja dans ../Quest
STAT_W = 69
CONTINENT = os.path.join(HERE, "..", "carte", "sources", "continent_reference.png")

PAGE_L, PAGE_T, PANEL_W = g.PAGE_L, g.PAGE_T, g.PANEL_W
TEXT_L, TEXT_W = g.TEXT_L, g.TEXT_W
CARD_L0, CARD_T, CARD_PITCH, CARD_H = g.CARD_L0, g.CARD_T, g.CARD_PITCH, g.CARD_H
FRAME_DX, FRAME_DY, FRAME_W, FRAME_H = g.FRAME_DX, g.FRAME_DY, g.FRAME_W, g.FRAME_H
ART_DX, ART_DY, ART_W, ART_H = g.ART_DX, g.ART_DY, g.ART_W, g.ART_H
GOLD_TITLE, TAGLINE, TEXT2, HEAD = g.GOLD_TITLE, g.TAGLINE, g.TEXT2, g.HEAD
Q = "../Quest/"


# ==================== ILLUSTRATIONS ====================

def _sun(col, x, y, cx, cy, r, core, halo, k=0.5):
    d = math.hypot(x - cx, y - cy)
    if d < r: return hexc(core)
    if d < r * 2.2: return mix(col, hexc(halo), (r * 2.2 - d) / (r * 1.2) * k)
    return col


def scene_royaume(u, v, rnd):
    """Bienvenue : le chateau du Roi au lever du jour, bannieres rouge et or, route qui monte vers la porte."""
    x, y = u * AW, v * AH
    col = vgrad([(0, "3a5ea8"), (0.3, "8fb4e0"), (0.5, "f5c98a"), (1, "f5c98a")], v)
    col = _sun(col, x, y, 250, 250, 26, "fff4c4", "ffe0a0")
    if y > ridge(u, 0.50, 0.03, 6, 14, 0.7) * AH: col = mix(hexc("7f8fb8"), hexc("6a7aa6"), (y - 290) / 60)
    # chateau
    stone, dark, roof = hexc("c9c2b0"), hexc("8e8676"), hexc("3f5a8f")
    for x0, x1, top in ((62, 104, 150), (230, 272, 150), (124, 210, 190)):       # tours et donjon
        if x0 <= x <= x1 and top <= y <= 360:
            col = stone if (x - x0) > 5 else dark
    for x0, x1, top in ((62, 104, 150), (230, 272, 150)):                          # toits pointus
        if inpoly(x, y, [(x0 - 6, top), ((x0 + x1) / 2, top - 62), (x1 + 6, top)]): col = roof if x > (x0 + x1) / 2 - 2 else hexc("2c4373")
    if 124 <= x <= 210 and 178 <= y < 190 and int((x - 124) / 9) % 2 == 0: col = stone          # creneaux du donjon
    if 104 < x < 124 and 250 <= y <= 360 or 210 < x < 230 and 250 <= y <= 360: col = hexc("b5ad9b")   # courtines
    for wx, wy in ((83, 200), (251, 200), (146, 230), (188, 230), (167, 230)):    # fenetres eclairees
        if abs(x - wx) < 4 and wy - 8 < y < wy + 8: col = hexc("ffd27a")
    if inpoly(x, y, [(147, 360), (147, 312), (167, 296), (187, 312), (187, 360)]): col = hexc("2a1e18")   # porte
    if inpoly(x, y, [(150, 360), (150, 314), (167, 300), (184, 314), (184, 360)]) and int(x) % 6 == 0: col = hexc("5a3a22")
    for bx in (83, 251):                                                           # bannieres
        if abs(x - bx) < 1.2 and 56 <= y <= 88: col = hexc("2a1a10")
        if bx + 1 <= x <= bx + 22 and 58 <= y <= 74 + 4 * math.sin(x * 0.4): col = hexc("b8322c") if y > 61 else hexc("e0b050")
    if 124 <= x <= 210 and 250 <= y <= 300 and abs(x - 167) < 14:                  # grand etendard sur le donjon
        col = hexc("8a1c26") if abs(x - 167) < 12 else hexc("e0b050")
        if inpoly(x, y, [(159, 270), (159, 262), (163, 266), (167, 258), (171, 266), (175, 262), (175, 270)]): col = hexc("f2c45e")
    # campagne et route
    if y > 360:
        col = mix(hexc("6fa046"), hexc("3f6b2c"), (y - 360) / 244)
        if (int(x / 3) + int(y / 5)) % 7 == 0: col = mix(col, hexc("8cbf55"), 0.4)
        half = 12 + (y - 360) * 0.55
        if abs(x - 167 - (y - 360) * 0.08) < half: col = mix(hexc("d9c08a"), hexc("bfa16a"), (y - 360) / 244)
    # oiseaux
    for bx, by in ((90, 110), (110, 98), (200, 120)):
        if abs(abs(x - bx) * 0.5 - (by - y)) < 0.9 and abs(x - bx) < 7: col = hexc("2a2a3a")
    return col


def scene_grimoire(u, v, rnd):
    """Commandes : grimoire ouvert sur un lutrin dans une bibliotheque, cercle de runes qui s'eleve des pages."""
    x, y = u * AW, v * AH
    col = vgrad([(0, "1a1530"), (1, "2c2148")], v)
    # etageres
    for sy in (40, 140, 240):
        if sy <= y < sy + 92:
            k = int(x / 11)
            hue = ["7a2a2a", "2a4a7a", "6a5a2a", "2a6a4a", "5a2a6a", "8a6a3a"][(k * 7 + sy) % 6]
            hgt = 70 + (k * 13 + sy) % 20
            if y > sy + 92 - hgt and x % 11 > 1: col = mix(hexc(hue), hexc("120e1e"), 0.35)
            if (y - (sy + 92 - hgt)) % 30 < 2 and x % 11 > 1: col = hexc("c9a24a")
        if sy + 92 <= y < sy + 100: col = hexc("4a2c18")
    # lueur
    d = math.hypot(x - 167, y - 250)
    if d < 150: col = mix(col, hexc("6a8cff"), (150 - d) / 150 * 0.45)
    # cercle de runes
    r = math.hypot(x - 167, y - 230)
    if abs(r - 70) < 2.2 or abs(r - 56) < 1.4: col = hexc("bcd2ff")
    tri = [(167 + 56 * math.cos(math.radians(a)), 230 + 56 * math.sin(math.radians(a))) for a in (-90, 30, 150)]
    for i in range(3):
        if seg_dist(x, y, *tri[i], *tri[(i + 1) % 3]) < 1.4: col = hexc("bcd2ff")
    for a in range(0, 360, 30):
        rx, ry = 167 + 63 * math.cos(math.radians(a)), 230 + 63 * math.sin(math.radians(a))
        if math.hypot(x - rx, y - ry) < 2.6: col = hexc("ffffff")
    # lutrin
    if inpoly(x, y, [(150, 400), (184, 400), (196, 470), (138, 470)]): col = hexc("5a3a22")
    if inpoly(x, y, [(110, 470), (224, 470), (232, 484), (102, 484)]): col = hexc("3e2614")
    # livre ouvert
    left = [(96, 330), (165, 344), (165, 404), (90, 392)]
    right = [(169, 344), (238, 330), (244, 392), (169, 404)]
    if inpoly(x, y, [(86, 334), (248, 334), (252, 398), (82, 398)]) and y > 336: col = hexc("6b1a2a")     # couverture
    for page in (left, right):
        if inpoly(x, y, page):
            col = hexc("f2e8cf")
            if int(y - 352) % 9 < 2 and 352 < y < 392 and abs(x - 167) > 12: col = hexc("8a7a5a")
    if abs(x - 167) < 2 and 342 < y < 404: col = hexc("4a3a2a")
    # etincelles
    if 150 < y < 340 and rnd(int(x / 3), int(y / 3)) > 0.985: col = hexc("e8f0ff")
    # bougies
    for cx in (60, 274):
        if abs(x - cx) < 7 and 420 < y < 470: col = hexc("efe6cf")
        if math.hypot((x - cx) * 1.6, y - 410) < 7: col = hexc("ffd27a")
        if math.hypot(x - cx, y - 410) < 22: col = mix(col, hexc("ffb347"), 0.25)
    if y > 484: col = mix(hexc("2a1e2e"), hexc("1a1420"), (y - 484) / 120)
    return col


ARMOR = {'weapon': "dfe5ee", 'skin': "e9c29a", 'sleeve': "7d8796", 'hair': "5a3a22", 'gi': "9aa4b2", 'inner': "c8ced8",
         'belt': "6b4a2a", 'hakama': "4a5260", 'beard': "5a3a22"}


def scene_guerrier(u, v, rnd):
    """Guerrier : chevalier en armure, epee levee et bouclier ecu, cape rouge, remparts au crepuscule."""
    x, y = u * AW, v * AH
    col = vgrad([(0, "2a1830"), (0.35, "8a2c3a"), (0.6, "f08a4b"), (1, "f08a4b")], v)
    col = _sun(col, x, y, 167, 300, 60, "ffd98a", "ff9a5a", 0.4)
    if y > 330 and (y < 352 and int(x / 16) % 2 == 0 or y >= 352): col = hexc("3a2530")      # remparts
    if y > 470: col = mix(hexc("5a4a40"), hexc("2e2420"), (y - 470) / 134)
    cx, feet, s = 167, 470, 1.55
    if inpoly(x, y, [(cx - 30, feet - 118 * s), (cx + 30, feet - 118 * s), (cx + 52, feet - 8), (cx - 52, feet - 8)]):
        col = hexc("8a1c26") if x < cx + 40 else hexc("6a1420")                              # cape
    col = _paint(col, _person(x, y, cx, feet, s, (196, 300), (262, 128)), ARMOR)
    if 192 < x < 202 and 296 < y < 306: col = hexc("c58643")                                  # garde
    if seg_dist(x, y, 186, 304, 206, 296) < 3: col = hexc("c58643")
    # bouclier ecu
    sx, sy = 112, 360
    shield = [(sx - 34, sy - 44), (sx + 34, sy - 44), (sx + 34, sy + 6), (sx, sy + 50), (sx - 34, sy + 6)]
    inner = [(sx - 28, sy - 38), (sx + 28, sy - 38), (sx + 28, sy + 4), (sx, sy + 42), (sx - 28, sy + 4)]
    if inpoly(x, y, shield): col = hexc("c58643")
    if inpoly(x, y, inner):
        col = hexc("2c4373") if (x < sx) == (y < sy - 10) else hexc("e0dccf")
        if abs(x - sx) < 4 or abs(y - (sy - 10)) < 4: col = hexc("c58643")
    return col


def scene_mage(u, v, rnd):
    """Mage : mage encapuchonne, baton surmonte d'un orbe, nuit etoilee, cercle magique au sol."""
    x, y = u * AW, v * AH
    col = vgrad([(0, "070b1d"), (0.55, "1f2a5a"), (1, "2a1f4a")], v)
    if y < 330 and rnd(int(x), int(y)) > 0.993: col = hexc("e8eeff")
    if y > ridge(u, 0.6, 0.03, 5, 12, 1.4) * AH: col = hexc("141a36")
    # cercle magique au sol (ellipse)
    e = math.hypot((x - 167) / 120, (y - 470) / 30)
    if abs(e - 1) < 0.03 or abs(e - 0.8) < 0.025: col = hexc("9f7aff")
    if e < 1: col = mix(col, hexc("7a5aff"), 0.18)
    robe = {'weapon': "6b4a2a", 'skin': "e9c29a", 'sleeve': "4a2a7a", 'hood': "3a1f66", 'face': "1a1030", 'gi': "4a2a7a",
            'inner': "c9a24a", 'belt': "c9a24a", 'hakama': "3a1f66", 'hair': "c9c9d9"}
    col = _paint(col, _person(x, y, 167, 470, 1.55, (206, 320), (222, 118), hood=True), robe)
    # orbe
    d = math.hypot(x - 223, y - 112)
    if d < 13: col = mix(hexc("ffffff"), hexc("8fd0ff"), d / 13)
    elif d < 46: col = mix(col, hexc("6ab0ff"), (46 - d) / 33 * 0.55)
    for a in (0, 120, 240):                                                                # griffes du baton
        if seg_dist(x, y, 222, 130, 222 + 14 * math.cos(math.radians(a + 60)), 112 + 14 * math.sin(math.radians(a + 60))) < 1.6 and y > 108:
            col = hexc("6b4a2a")
    # yeux luisants sous la capuche
    for ex in (162, 172):
        if math.hypot(x - ex, y - 255) < 2: col = hexc("8fd0ff")
    if 100 < y < 400 and rnd(int(x / 4), int(y / 4)) > 0.99 and math.hypot(x - 223, y - 112) < 110: col = hexc("bfe2ff")
    return col


def scene_archer(u, v, rnd):
    """Archer : archer encapuchonne de vert, arc bande, foret de grands pins et rais de soleil."""
    x, y = u * AW, v * AH
    col = vgrad([(0, "cfe8c0"), (0.5, "8fc07a"), (1, "4f7f3c")], v)
    for tx, w in ((30, 26), (300, 30), (80, 16), (250, 18)):                               # troncs
        if abs(x - tx) < w / 2: col = mix(hexc("4a3420"), hexc("2e2014"), 0.5 if abs(x - tx) > w / 3 else 0)
    for tx, base, w in ((60, 230, 70), (276, 210, 80), (150, 120, 50)):                    # cimes
        if inpoly(x, y, [(tx - w, base), (tx, base - 150), (tx + w, base)]): col = hexc("2f5a2c")
    if (x * 0.6 + y) % 90 < 22 and y < 420: col = mix(col, hexc("fff4c4"), 0.22)             # rais de soleil
    if y > 440: col = mix(hexc("5a8a3c"), hexc("33521f"), (y - 440) / 164)
    pal = {'weapon': "8a5a2b", 'skin': "e9c29a", 'sleeve': "3f6b2c", 'hood': "2f5a2c", 'face': "e9c29a", 'gi': "4a7a34",
           'inner': "c9b27a", 'belt': "6b4a2a", 'hakama': "4a3420", 'hair': "8a5a2b"}
    cx, feet, s = 190, 470, 1.5
    col = _paint(col, _person(x, y, cx, feet, s, (118, 292)), pal)
    # arc : arc de cercle a gauche, corde, fleche encochee
    cxb, cyb, rb = 150, 292, 92
    a = math.degrees(math.atan2(y - cyb, x - cxb))
    if abs(math.hypot(x - cxb, y - cyb) - rb) < 3 and 135 < (a % 360) < 225: col = hexc("6b3a1a")
    top = (cxb + rb * math.cos(math.radians(225)), cyb + rb * math.sin(math.radians(225)))
    bot = (cxb + rb * math.cos(math.radians(135)), cyb + rb * math.sin(math.radians(135)))
    if seg_dist(x, y, top[0], top[1], 176, 292) < 0.8 or seg_dist(x, y, bot[0], bot[1], 176, 292) < 0.8: col = hexc("efe6cf")
    if seg_dist(x, y, 40, 292, 178, 292) < 1.5: col = hexc("c9a24a")
    if inpoly(x, y, [(30, 292), (42, 286), (42, 298)]): col = hexc("dfe5ee")
    if 170 < x < 182 and abs(y - 292) < 5 and int(x) % 3 == 0: col = hexc("b8322c")         # empennage
    return col


def scene_assassin(u, v, rnd):
    """Assassin : silhouette encapuchonnee sur les toits sous une lune rouge, deux dagues."""
    x, y = u * AW, v * AH
    col = vgrad([(0, "12081a"), (0.5, "3a1428"), (1, "1a0c18")], v)
    d = math.hypot(x - 167, y - 210)
    if d < 92: col = mix(hexc("ff6a5a"), hexc("c9302c"), d / 92)
    elif d < 140: col = mix(col, hexc("8a2030"), (140 - d) / 48 * 0.5)
    if y < 380 and rnd(int(x), int(y)) > 0.995: col = hexc("ffd0d0")
    # toits
    for x0, x1, top in ((0, 120, 400), (214, 334, 380), (100, 230, 430)):
        if x0 <= x <= x1 and y > top + abs(x - (x0 + x1) / 2) * 0.35: col = hexc("0c0610")
    if y > 470: col = hexc("08040a")
    pal = {'weapon': "dfe5ee", 'skin': "2a1a20", 'sleeve': "1a1018", 'hood': "140a12", 'face': "05030a", 'gi': "1a1018",
           'inner': "6a1420", 'belt': "6a1420", 'hakama': "120a10", 'hair': "05030a"}
    col = _paint(col, _person(x, y, 167, 470, 1.55, (214, 330), (252, 286), hood=True), pal)
    if seg_dist(x, y, 120, 330, 84, 296) < 3: col = hexc("dfe5ee")                           # seconde dague
    if seg_dist(x, y, 120, 330, 167, 300) < 9: col = hexc("1a1018") if col != hexc("dfe5ee") else col
    for ex in (162, 172):                                                                   # yeux
        if math.hypot(x - ex, y - 256) < 1.8: col = hexc("ff8a7a")
    # echarpe qui flotte
    if inpoly(x, y, [(176, 268), (250, 250), (240, 262), (178, 278)]): col = hexc("8a1c26")
    return col


def scene_marchand(u, v, rnd):
    """Marchand : marchand barbu devant son etal a auvent raye, bourse d'or, pieces et balance."""
    x, y = u * AW, v * AH
    col = vgrad([(0, "f5e6c4"), (1, "e3c690")], v)
    if y < 120:                                                                            # auvent raye
        col = hexc("b8322c") if int(x / 28) % 2 == 0 else hexc("f2e8cf")
        if y > 104 and (x % 28) > 14: col = hexc("f5e6c4") if y > 112 else col
    if (x < 22 or x > 312) and y >= 100: col = hexc("6b4a2a")                              # poteaux
    for sx, sy, c in ((50, 170, "b8322c"), (90, 176, "e0b050"), (250, 170, "4a7a34"), (290, 176, "2c4373")):   # tissus
        if abs(x - sx) < 18 and sy < y < sy + 110: col = mix(hexc(c), hexc("000000"), 0.15 if int(y / 8) % 2 else 0)
    for jx in (60, 274):                                                                   # jarres
        if math.hypot((x - jx) / 1.1, y - 330) < 24: col = hexc("a0623a")
    if y > 380: col = mix(hexc("8a5a2b"), hexc("5a3a1e"), (y - 380) / 224)                  # comptoir
    if 380 <= y <= 392: col = hexc("c58643")
    pal = {'weapon': "c9a24a", 'skin': "e9c29a", 'sleeve': "2f6a44", 'hair': "6b4a2a", 'beard': "8a6a4a", 'gi': "2f6a44",
           'inner': "e0b050", 'belt': "c58643", 'hakama': "3a2a1a"}
    col = _paint(col, _person(x, y, 167, 470, 1.5, (210, 336), beard=True), pal)
    # bourse d'or
    d = math.hypot(x - 220, y - 352)
    if d < 22: col = hexc("8a5a2b")
    if d < 22 and y < 340: col = hexc("6b4a2a")
    if math.hypot(x - 220, y - 356) < 7: col = hexc("f2c45e")
    # pieces sur le comptoir
    for px, py in ((80, 400), (96, 404), (88, 396), (250, 402), (264, 398), (258, 406)):
        if math.hypot((x - px) / 1.6, y - py) < 4.5: col = hexc("f2c45e") if y < py else hexc("c58643")
    # balance
    if abs(x - 120) < 1.5 and 300 < y < 380: col = hexc("c58643")
    if seg_dist(x, y, 96, 304, 144, 304) < 1.5: col = hexc("c58643")
    for bx in (96, 144):
        if abs(math.hypot(x - bx, y - 316) - 10) < 1.5 and y > 316: col = hexc("c58643")
    return col


def scene_runes(u, v, rnd):
    """Runes et magie : nuit, cercle de menhirs, grande pierre runique gravee de bleu, pierres de couleur en orbite."""
    x, y = u * AW, v * AH
    col = vgrad([(0, "060914"), (0.5, "141c3c"), (1, "1c1638")], v)
    if y < 330 and rnd(int(x), int(y)) > 0.992: col = hexc("e8eeff")
    if y > ridge(u, 0.62, 0.025, 6, 13, 0.4) * AH: col = hexc("0f1428")
    for mx, top, w in ((40, 300, 34), (294, 290, 36), (92, 330, 26), (244, 326, 26)):        # menhirs
        if abs(x - mx) < w / 2 and y > top + abs(x - mx) * 0.3: col = mix(hexc("3a4058"), hexc("252a40"), (x - mx + w / 2) / w)
    # halo
    d = math.hypot(x - 167, y - 270)
    if d < 170: col = mix(col, hexc("3a7aff"), (170 - d) / 170 * 0.35)
    # grande pierre runique
    if inpoly(x, y, [(122, 410), (128, 200), (167, 168), (206, 200), (212, 410)]):
        col = mix(hexc("5a6078"), hexc("3a3f58"), (x - 122) / 90)
        for a, b in (((167, 214), (167, 360)), ((143, 250), (191, 250)), ((150, 300), (167, 330)), ((184, 300), (167, 330)),
                     ((147, 214), (187, 214)), ((152, 370), (182, 370))):
            if seg_dist(x, y, *a, *b) < 3.2: col = hexc("9fd0ff")
            elif seg_dist(x, y, *a, *b) < 7: col = mix(col, hexc("5aa8ff"), 0.5)
    # pierres de couleur en orbite (ecoles de magie)
    for k, c in enumerate(("ff6a3a", "6ad8ff", "c9a24a", "ffe45a", "9f6aff", "f5f5ff", "6adf7a")):
        a = k / 7 * 2 * math.pi + 0.4
        ox, oy = 167 + 128 * math.cos(a), 290 + 60 * math.sin(a)
        dd = math.hypot(x - ox, y - oy)
        if dd < 10: col = mix(hexc("ffffff"), hexc(c), dd / 10)
        elif dd < 24: col = mix(col, hexc(c), (24 - dd) / 14 * 0.45)
    e = math.hypot((x - 167) / 128, (y - 290) / 60)
    if abs(e - 1) < 0.012: col = mix(col, hexc("bcd2ff"), 0.6)
    if y > 470: col = mix(hexc("141a30"), hexc("0a0e1c"), (y - 470) / 134)
    return col


def scene_faille(u, v, rnd):
    """Failles et donjons : arche de pierre en ruine, faille violette dechiree, yeux de monstres, coffre au tresor."""
    x, y = u * AW, v * AH
    col = vgrad([(0, "0c0a16"), (1, "1c1428")], v)
    if (int(y / 26) % 2 == 0 and abs(x % 52 - 26) < 1.2) or (int(y / 26) % 2 == 1 and x % 52 < 1.2) or y % 26 < 1.2:
        col = mix(col, hexc("05040a"), 0.6)
    # faille : losange dechire
    cx, cy = 167, 260
    w = 62 * max(0.0, 1 - abs(y - cy) / 190) + 6 * math.sin(y * 0.21) * max(0.0, 1 - abs(y - cy) / 190)
    if abs(x - cx - 8 * math.sin(y * 0.05)) < w:
        k = abs(x - cx - 8 * math.sin(y * 0.05)) / max(1.0, w)
        col = mix(hexc("f0d0ff"), hexc("6a1a9a"), k ** 0.6)
        for ex, ey in ((150, 220), (160, 220), (180, 300), (190, 300)):                    # yeux
            if math.hypot(x - ex, y - ey) < 3.5: col = hexc("ffe45a")
    elif abs(x - cx) < w + 40: col = mix(col, hexc("8a3aff"), (1 - (abs(x - cx) - w) / 40) * 0.35)
    # arche
    if 30 <= x <= 76 or 258 <= x <= 304:
        if y > 110: col = mix(hexc("4a4458"), hexc("2c2838"), 0.5 if int(y / 30) % 2 else 0)
    if 110 > y > 70 and 30 <= x <= 304 and math.hypot(x - 167, (y - 130) * 2.2) > 130:
        col = mix(hexc("5a5468"), hexc("3a3448"), 0.5 if int(x / 34) % 2 else 0)
    # coffre au tresor
    if inpoly(x, y, [(122, 470), (212, 470), (212, 418), (122, 418)]): col = hexc("6b3a1a")
    if inpoly(x, y, [(118, 420), (216, 420), (206, 392), (128, 392)]): col = hexc("7a4422")
    if (abs(y - 418) < 3 or abs(x - 167) < 4) and 392 <= y <= 470 and 118 <= x <= 216: col = hexc("e0b050")
    d = math.hypot(x - 167, y - 400)
    if d < 70 and y < 420: col = mix(col, hexc("ffd27a"), (70 - d) / 70 * 0.4)
    for px, py in ((140, 412), (156, 408), (178, 410), (194, 413), (168, 404)):
        if math.hypot(x - px, y - py) < 5: col = hexc("f2c45e")
    if y > 470: col = mix(hexc("1c1626"), hexc("0c0a12"), (y - 470) / 134)
    return col


def scene_guildes(u, v, rnd):
    """Guildes et noblesse : grande salle, bannieres de guildes de toutes les couleurs, blason couronne, tapis rouge."""
    x, y = u * AW, v * AH
    col = vgrad([(0, "2a2030"), (1, "3a2c32")], v)
    if (int(y / 22) % 2 == 0 and abs(x % 40 - 20) < 1) or (int(y / 22) % 2 == 1 and x % 40 < 1) or y % 22 < 1:
        col = mix(col, hexc("1a1218"), 0.5)
    for k, (c, em) in enumerate((("b8322c", "e0b050"), ("2c4373", "dfe5ee"), ("2f6a44", "e0b050"), ("6a1f7a", "dfe5ee"),
                                 ("c58643", "2a1a10"))):
        bx = 30 + k * 68
        bottom = 330 - (k % 2) * 30
        if bx <= x <= bx + 44 and 60 <= y <= bottom + 18 * (1 - abs(x - bx - 22) / 22):
            col = hexc(c) if bx + 3 < x < bx + 41 else mix(hexc(c), hexc("000000"), 0.4)
            if abs(y - 66) < 3: col = hexc("e0b050")
            if math.hypot(x - bx - 22, y - 150) < 11: col = hexc(em)
        if bx - 4 <= x <= bx + 48 and 52 <= y <= 58: col = hexc("4a2c18")
    # blason couronne au centre
    sx, sy = 167, 395
    if inpoly(x, y, [(sx - 34, sy - 40), (sx + 34, sy - 40), (sx + 34, sy + 4), (sx, sy + 46), (sx - 34, sy + 4)]):
        col = hexc("2c4373") if abs(x - sx) > 3 else hexc("e0b050")
        if inpoly(x, y, [(sx - 16, sy - 6), (sx - 16, sy - 22), (sx - 8, sy - 14), (sx, sy - 28), (sx + 8, sy - 14), (sx + 16, sy - 22), (sx + 16, sy - 6)]):
            col = hexc("f2c45e")
    if y > 440:
        col = mix(hexc("4a3428"), hexc("2a1e18"), (y - 440) / 164)
        if abs(x - 167) < 34 + (y - 440) * 0.25: col = hexc("8a1c26")
    # chandelier
    if abs(x - 167) < 60 and abs(y - 24) < 3: col = hexc("c58643")
    for fx in (117, 142, 167, 192, 217):
        if math.hypot((x - fx) * 1.6, y - 16) < 5: col = hexc("ffd27a")
    return col


def scene_banque(u, v, rnd):
    """Commerce et banque : salle des coffres a colonnes, piles de pieces, coffre debordant d'or, balance."""
    x, y = u * AW, v * AH
    col = vgrad([(0, "2a2418"), (1, "4a3a22")], v)
    for cx in (40, 294):                                                                    # colonnes
        if abs(x - cx) < 24: col = mix(hexc("d8cdb0"), hexc("a89a78"), abs(x - cx) / 24)
        if abs(x - cx) < 30 and (60 < y < 76 or 430 < y < 446): col = hexc("c9bc98")
    if 72 <= x <= 262 and 60 <= y <= 340:                                                    # porte du coffre-fort
        d = math.hypot(x - 167, y - 200)
        if d < 118: col = mix(hexc("8a8a94"), hexc("5a5a66"), d / 118)
        if abs(d - 100) < 4: col = hexc("c9c9d2")
        for a in range(0, 360, 60):
            if seg_dist(x, y, 167, 200, 167 + 40 * math.cos(math.radians(a)), 200 + 40 * math.sin(math.radians(a))) < 4:
                col = hexc("c58643")
        if d < 12: col = hexc("e0b050")
    # piles de pieces
    for px, base, n in ((70, 470, 7), (100, 470, 10), (240, 470, 9), (268, 470, 6)):
        if abs(x - px) < 14 and base - n * 7 < y < base: col = hexc("f2c45e") if int((base - y) / 3.5) % 2 else hexc("c58643")
    # coffre debordant
    if inpoly(x, y, [(124, 470), (210, 470), (210, 420), (124, 420)]): col = hexc("6b3a1a")
    if (abs(y - 432) < 3 or abs(x - 167) < 4) and 420 <= y <= 470 and 124 <= x <= 210: col = hexc("e0b050")
    if inpoly(x, y, [(120, 422), (214, 422), (200, 396), (134, 396)]): col = hexc("f2c45e") if (int(x / 6) + int(y / 5)) % 3 else hexc("c58643")
    d = math.hypot(x - 167, y - 410)
    if d < 90: col = mix(col, hexc("ffe08a"), (90 - d) / 90 * 0.3)
    if y > 470: col = mix(hexc("5a4630"), hexc("2e2418"), (y - 470) / 134)
    return col


SCENES = {"Royaume": scene_royaume, "Grimoire": scene_grimoire, "Guerrier": scene_guerrier, "Mage": scene_mage,
          "Archer": scene_archer, "Assassin": scene_assassin, "Marchand": scene_marchand,
          "Runes": scene_runes, "Faille": scene_faille, "Guildes": scene_guildes, "Banque": scene_banque}


# ==================== CADRES BLANCS (rarete Commune, 2026-11-25) ====================

def white_frames():
    """Cadres et eclats du client passes en blanc : luminosite gardee (relief intact), couleur retiree, eclaircis."""
    def whiten(px, lift, gain):
        for row in px:
            for q in row:
                if q[3] == 0: continue
                l = 0.3 * q[0] + 0.59 * q[1] + 0.11 * q[2]
                v = min(255.0, l * gain + lift)
                q[0], q[1], q[2] = v * 0.97, v * 0.98, v
        return px
    for src, name, lift, gain in (("ChapterCardFrameDefault.png", "ChapFrameDefaultBlanc@2x.png", 100, 1.35),
                                  ("ChapterCardFrameSelected.png", "ChapFrameSelectedBlanc@2x.png", 70, 1.3)):
        w, h, px = g.read_any(os.path.join(g.CHAP, src))
        g.save(name, whiten(px, lift, gain))
    for n in range(1, 17):
        w, h, px = g.read_any(os.path.join(g.CHAP, "ChapterCardShard%02d.png" % n))
        g.save("ChapShard%02dBlanc.png" % n, whiten(px, 70, 1.3))


# ==================== BLASON ET FOND ====================

def crest():
    """Blason d'Eldanior (152 px, affiche en 76) : ecu bleu nuit borde d'or, couronne, epees croisees, cercle runique."""
    S = 152
    img = g.blank(S, S)
    cx = S / 2
    gold, gold2, navy, steel = hexc("f2c45e"), hexc("c58643"), hexc("1c2a55"), hexc("dfe5ee")
    for (ax, ay, bx, by) in ((22, 128, 128, 30), (130, 128, 24, 30)):                        # epees croisees
        g.stroke(img, [(ax, ay), (bx, by)], 6, lambda x, y: steel)
        g.fill_poly(img, [(ax - 6, ay), (ax, ay - 6), (ax + 6, ay), (ax, ay + 6)], gold2)          # pommeau
    shield = [(cx - 44, 40), (cx + 44, 40), (cx + 44, 92), (cx, 138), (cx - 44, 92)]
    inner = [(cx - 37, 47), (cx + 37, 47), (cx + 37, 90), (cx, 129), (cx - 37, 90)]
    g.fill_poly(img, shield, gold)
    g.fill_poly(img, inner, navy)
    for y in range(S):                                                                        # reflet
        for x in range(S):
            if inpoly(x + 0.5, y + 0.5, inner) and x < cx:
                g.over(img[y][x], hexc("2c4373"), 0.5)
    for r, w in ((26, 2.2), (19, 1.4)):                                                       # cercle runique
        pts = [(cx + r * math.cos(a / 40 * 2 * math.pi), 86 + r * math.sin(a / 40 * 2 * math.pi)) for a in range(41)]
        g.stroke(img, pts, w, lambda x, y: gold)
    g.stroke(img, [(cx, 62), (cx, 112)], 3.2, lambda x, y: steel)                              # epee centrale
    g.stroke(img, [(cx - 9, 98), (cx + 9, 98)], 3, lambda x, y: gold)
    for a in range(0, 360, 45):
        rx, ry = cx + 22.5 * math.cos(math.radians(a)), 86 + 22.5 * math.sin(math.radians(a))
        g.fill_poly(img, [(rx, ry - 2.6), (rx + 2.6, ry), (rx, ry + 2.6), (rx - 2.6, ry)], hexc("ffffff"))
    crown = [(cx - 30, 40), (cx - 30, 18), (cx - 18, 30), (cx, 8), (cx + 18, 30), (cx + 30, 18), (cx + 30, 40)]
    g.fill_poly(img, crown, gold)
    g.fill_poly(img, [(cx - 30, 40), (cx + 30, 40), (cx + 30, 33), (cx - 30, 33)], gold2)
    for gx, gc in ((cx - 14, "c9302c"), (cx, "4fd08a"), (cx + 14, "2c6ad0")):
        g.fill_poly(img, [(gx, 33), (gx + 4, 37), (gx, 41), (gx - 4, 37)], hexc(gc))
    g.save("AccCrest@2x.png", img)


def continent_mask(size):
    """Silhouette du continent (terre = 1) a la taille voulue, depuis l'image de reference de la carte du serveur :
    seule la forme des cotes est gardee (l'eau est bleue), rien de l'image elle-meme."""
    w, h, px = g.read_any(CONTINENT)
    m = [[0.0] * size for _ in range(size)]
    for y in range(size):
        for x in range(size):
            sx, sy = int(x * w / size), int(y * h / size)
            if sx < w * 0.06 or sy < h * 0.06 or sx > w * 0.96 or sy > h * 0.96: continue       # cadre de l'image source
            r, gg, b, a = px[sy][sx]
            m[y][x] = 0.0 if (b - r > 38 and b >= gg) else 1.0
    flat = [m[y][x] for y in range(size) for x in range(size)]
    flat = g.box_blur(flat, size, size, 2)
    return [[flat[y * size + x] for x in range(size)] for y in range(size)]


def background():
    """Fond 1280x720 (affiche en 1920x1080) : nuit bleue, quadrillage de carte, cote du continent a l'encre doree derriere
    les cartes, rose des vents, vignette."""
    W, H = 1280, 720
    M = 640
    mask = continent_mask(M)
    ox, oy = 470, 50                                                                          # coin de la carte
    img = [[[0, 0, 0, 255] for _ in range(W)] for _ in range(H)]
    gold, ink = hexc("c58643"), hexc("223058")
    for y in range(H):
        for x in range(W):
            col = mix(hexc("0e1428"), hexc("070a16"), math.hypot((x - 800) / 900, (y - 360) / 600))
            if (x % 64 < 1 or y % 64 < 1): col = mix(col, gold, 0.05)                          # quadrillage
            mx, my = x - ox, y - oy
            if 1 <= mx < M - 1 and 1 <= my < M - 1:
                v = mask[my][mx]
                if v > 0.5:
                    col = mix(col, ink, 0.32)
                    if (mx * 7 + my * 13) % 29 == 0: col = mix(col, gold, 0.18)                    # pointille de la terre
                if abs(v - 0.5) < 0.2: col = mix(col, gold, 0.5)                                    # cote
                elif 0.06 < v < 0.3 and (mx + my) % 4 < 2: col = mix(col, gold, 0.12)               # trait cotier au large
            rx, ry = x - 1180, y - 640                                                         # rose des vents
            d = math.hypot(rx, ry)
            if d < 60:
                if abs(d - 52) < 1 or abs(d - 44) < 0.8: col = mix(col, gold, 0.4)
                if (abs(rx) < 4 - abs(ry) / 15 or abs(ry) < 4 - abs(rx) / 15): col = mix(col, gold, 0.5)
            vig = max(0.0, math.hypot((x - 640) / 640, (y - 360) / 360) - 0.75)
            col = mix(col, hexc("000000"), min(0.8, vig * 1.4))
            img[y][x] = [col[0], col[1], col[2], 255]
    g.save("AccBackground@2x.png", img)


# ==================== .ui ====================

def art_path(name, k):
    return (Q if name in QUEST_ARTS else "") + f"ChapArt{name}{k}.png"


def card(i):
    x = CARD_L0 + i * CARD_PITCH
    arts = "\n".join(
        f'      Group #AccArt{t}{k}{i} {{ Anchor: (Left: {ART_DX - FRAME_DX}, Top: {ART_DY - FRAME_DY}, Width: {ART_W}, Height: {ART_H}); '
        f'Background: (TexturePath: "{art_path(t, k)}"); Visible: false; }}'
        for t in NEW_ARTS + QUEST_ARTS for k in ("G", "C"))
    frames = "\n".join(
        f'      Group #AccFrame{k}{i}{t} {{ Anchor: (Left: 0, Top: 0, Width: {FRAME_W}, Height: {FRAME_H}); '
        f'Background: (TexturePath: "{"" if t in LOCAL_TINTS else Q}ChapFrame{"Default" if k == "D" else "Selected"}{"" if t == "Base" else t}.png"); Visible: false; }}'
        for t in TINTS for k in ("D", "S"))
    shards = "\n".join(
        f'      Group #AccShards{i}{t} {{\n        Anchor: (Left: 0, Top: 0, Width: {FRAME_W}, Height: {FRAME_H});\n        Visible: false;\n'
        + "\n".join(f'        Group {{ Anchor: (Left: {round(sx)}, Top: {round(sy)}, Width: {w}, Height: {h}); '
                    f'Background: (TexturePath: "{"" if t in LOCAL_TINTS else Q}ChapShard{n:02d}{"" if t == "Base" else t}.png"); }}' for n, w, h, sx, sy in g.SHARDS)
        + '\n      }'
        for t in TINTS)
    return f'''    Button #AccCard{i} {{
      Anchor: (Left: {x + FRAME_DX}, Top: {CARD_T + FRAME_DY}, Width: {FRAME_W}, Height: {FRAME_H});
      Style: (
        Default: (Background: #000000(0)),
        Hovered: (Background: (TexturePath: "{Q}ChapCardHover.png")),
        Pressed: (Background: (TexturePath: "{Q}ChapCardHover.png", Color: #ffffff(0.6))),
        Sounds: $C.@ButtonSounds
      );
{arts}
      Group #AccArtSlot{i} {{ Anchor: (Left: {ART_DX - FRAME_DX}, Top: {ART_DY - FRAME_DY}, Width: {ART_W}, Height: {ART_H}); }}
      Group {{ Anchor: (Left: {ART_DX - FRAME_DX}, Top: {ART_DY - FRAME_DY + ART_H - 165}, Width: {ART_W}, Height: 165); Background: (TexturePath: "{Q}ChapCardShade.png"); }}
{frames}
{shards}
      Label #AccCardTitle{i} {{
        Anchor: (Left: 30, Top: {ART_DY - FRAME_DY + ART_H - 92}, Width: {FRAME_W - 60}, Height: 36);
        Style: (FontName: "Secondary", FontSize: 21, RenderBold: true, RenderUppercase: true, TextColor: {GOLD_TITLE}, HorizontalAlignment: Center, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 14);
      }}
      Label #AccCardState{i} {{
        Anchor: (Left: 30, Top: {ART_DY - FRAME_DY + ART_H - 54}, Width: {FRAME_W - 60}, Height: 20);
        Style: (FontSize: 13, RenderBold: true, RenderUppercase: true, TextColor: #7ed37e, HorizontalAlignment: Center, VerticalAlignment: Center);
      }}
    }}'''


STEP_W = (CARDS * CARD_PITCH - 32) // STEPS


def step(k):
    x = CARD_L0 + k * STEP_W
    return f'''      Button #AccStep{k} {{
        Anchor: (Left: {x - CARD_L0}, Top: 0, Width: {STEP_W - 14}, Height: 64);
        Style: (Default: (Background: #000000(0)), Hovered: (Background: #ffffff(0.05)), Sounds: $C.@ButtonSounds);
        Group #AccStepOff{k} {{ Anchor: (Left: 6, Top: 13, Width: 38, Height: 38); Background: (TexturePath: "{Q}ChapCheckOff.png"); }}
        Group #AccStepOn{k} {{ Anchor: (Left: 6, Top: 13, Width: 38, Height: 38); Background: (TexturePath: "{Q}ChapCheckOn.png"); Visible: false; }}
        Label #AccStepNum{k} {{
          Anchor: (Left: 6, Top: 13, Width: 38, Height: 38);
          Style: (FontName: "Secondary", FontSize: 17, RenderBold: true, TextColor: #1b1420, HorizontalAlignment: Center, VerticalAlignment: Center);
        }}
        Label #AccStepLbl{k} {{
          Anchor: (Left: 54, Top: 0, Width: {STEP_W - 74}, Height: 64);
          Style: (FontName: "Secondary", FontSize: 21, RenderBold: true, RenderUppercase: true, TextColor: #77879d, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 14);
        }}
        Group #AccStepBar{k} {{ Anchor: (Left: 6, Top: 60, Width: {STEP_W - 34}, Height: 3); Background: {GOLD_TITLE}; Visible: false; }}
      }}'''


def head(id_, text, top=22):
    return f'''        Group #{id_} {{
          Anchor: (Top: {top}, Height: 34);
          LayoutMode: Top;
          Label #{id_}Text {{ Anchor: (Height: 28); Text: "{text}"; Style: (FontName: "Secondary", FontSize: 19, RenderBold: true, RenderUppercase: true, TextColor: {HEAD}, VerticalAlignment: Center); }}
          Group {{ Anchor: (Height: 2); Background: #c58643(0.45); }}
        }}'''


def note(i):
    return f'''        Group #AccNote{i} {{
          Anchor: (Top: 10);
          LayoutMode: Left;
          Visible: false;
          Group {{ Anchor: (Width: 22); LayoutMode: Top; Group {{ Anchor: (Width: 20, Height: 20, Top: 1); Background: (TexturePath: "{Q}ChapBullet.png"); }} }}
          Group {{
            FlexWeight: 1;
            LayoutMode: Top;
            Padding: (Left: 10, Top: 1);
            Label #AccNoteText{i} {{ Style: (FontSize: 15, RenderBold: true, TextColor: {TEXT2}, Wrap: true); }}
          }}
        }}'''


def passive(i):
    return f'''        Group #AccPas{i} {{
          Anchor: (Top: 10);
          LayoutMode: Left;
          Visible: false;
          Group {{ Anchor: (Width: 4); Background: #9b7ad8; }}
          Group {{
            FlexWeight: 1;
            LayoutMode: Top;
            Padding: (Left: 12, Top: 2, Bottom: 2);
            Background: #ffffff(0.03);
            Label #AccPasName{i} {{ Anchor: (Height: 22); Style: (FontSize: 16, RenderBold: true, TextColor: #d6c4ff, VerticalAlignment: Center); }}
            Label #AccPasDesc{i} {{ Style: (FontSize: 14, RenderBold: true, TextColor: {TEXT2}, Wrap: true); }}
          }}
        }}'''


def rune(i):
    return f'''        Group #AccRune{i} {{
          Anchor: (Top: 10);
          LayoutMode: Left;
          Visible: false;
          Group {{
            Anchor: (Width: 60, Height: 60);
            Group #AccRuneFrame{i} {{ Anchor: (Left: 0, Top: 0, Width: 60, Height: 60); Background: #c9d2dd; }}
            Group {{ Anchor: (Left: 2, Top: 2, Width: 56, Height: 56); Background: #0c1220; }}
            ItemIcon #AccRuneIcon{i} {{ Anchor: (Left: 6, Top: 6, Width: 48, Height: 48); }}
          }}
          Group {{
            FlexWeight: 1;
            LayoutMode: Top;
            Padding: (Left: 12);
            Label #AccRuneName{i} {{ Anchor: (Height: 22); Style: (FontSize: 16, RenderBold: true, TextColor: #e8eef6, VerticalAlignment: Center); }}
            Label #AccRuneMeta{i} {{ Anchor: (Height: 18); Style: (FontSize: 12, RenderBold: true, RenderUppercase: true, TextColor: #c9d2dd, VerticalAlignment: Center); }}
            Label #AccRuneDesc{i} {{ Style: (FontSize: 14, RenderBold: true, TextColor: {TEXT2}, Wrap: true); }}
          }}
        }}'''


def stat(k):
    return f'''          Group {{
            Anchor: (Width: {STAT_W}, Height: 60);
            LayoutMode: Top;
            Label #AccStatVal{k} {{ Anchor: (Height: 34); Style: (FontName: "Secondary", FontSize: 25, RenderBold: true, TextColor: {GOLD_TITLE}, HorizontalAlignment: Center, VerticalAlignment: Center); }}
            Label #AccStatLbl{k} {{ Anchor: (Height: 20); Style: (FontSize: 11, RenderBold: true, RenderUppercase: true, TextColor: {TEXT2}, HorizontalAlignment: Center, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 8); }}
          }}'''


KIT_SLOT, KIT_PITCH = 52, 58


def kit_slot(k):
    return f'''          Button #AccKit{k} {{
            Anchor: (Width: {KIT_SLOT}, Height: {KIT_SLOT}, Right: {KIT_PITCH - KIT_SLOT});
            Visible: false;
            Style: (Default: (Background: #000000(0)), Hovered: (Background: #ffffff(0.08)), Sounds: $C.@ButtonSounds);
            TextTooltipStyle: $C.@DefaultTextTooltipStyle;
            TextTooltipShowDelay: 0.1;
            Group {{ Anchor: (Left: 0, Top: 0, Width: {KIT_SLOT}, Height: {KIT_SLOT}); Background: (TexturePath: "{Q}ChapSlot.png", Border: 12); }}
            ItemIcon #AccKitIcon{k} {{ Anchor: (Left: 7, Top: 6, Width: 38, Height: 38); }}
            Label #AccKitQty{k} {{
              Anchor: (Left: 2, Top: {KIT_SLOT - 20}, Width: {KIT_SLOT - 6}, Height: 16);
              Style: (FontSize: 12, RenderBold: true, TextColor: #ffffff, HorizontalAlignment: End, VerticalAlignment: Center);
            }}
          }}'''


def build_ui():
    cards = "\n".join(card(i) for i in range(CARDS))
    steps = "\n".join(step(k) for k in range(STEPS))
    notes = "\n".join(note(i) for i in range(NOTES))
    passives = "\n".join(passive(i) for i in range(PASSIVES))
    runes = "\n".join(rune(i) for i in range(RUNES))
    stats = "\n".join(stat(k) for k in range(STATS))
    half = KIT // 2
    kit0 = "\n".join(kit_slot(k) for k in range(half))
    kit1 = "\n".join(kit_slot(k) for k in range(half, KIT))
    em = 150
    em_l, em_t = PAGE_L + (PANEL_W - em) // 2, 116
    body_t = 388
    ui = f'''$C = "../Common.ui";

// Fenetre a cartes d'Eldanior : accueil (accueil/WelcomePage) et statue des classes (classes/gui/ClassChoicePage),
// generee par scripts/accueil/gen_accueil_ui.py.
Group {{
  LayoutMode: Middle;
  Background: (TexturePath: "AccBackground.png");

  Group #AccPage {{
    Anchor: (Width: 1920, Height: 1080);

    // ---- fond : carte du continent (AccBackground), bande d'encre, lueur, ombre, eclats ----
    Group {{ Anchor: (Left: 499, Top: 0, Width: 504, Height: 1080); Background: (TexturePath: "{Q}ChapSeparator.png"); }}
    Group {{ Anchor: (Left: 480, Top: 25, Width: 488, Height: 1055); Background: (TexturePath: "{Q}ChapGlows.png"); }}
    Group {{ Anchor: (Left: 615, Top: 194, Width: 391, Height: 736); Background: (TexturePath: "{Q}ChapShadow.png"); }}
    Group {{ Anchor: (Left: 0, Top: 0, Width: 1920, Height: 1080); Background: (TexturePath: "{Q}ChapShards.png"); }}

    // ---- bandeau Eldanior ----
    Group {{ Anchor: (Left: 0, Top: 0, Width: 1920, Height: 100); Background: #05070f(0.62); }}
    Group {{ Anchor: (Left: 0, Top: 100, Width: 1920, Height: 2); Background: #c58643(0.55); }}
    Group {{ Anchor: (Left: 64, Top: 12, Width: 76, Height: 76); Background: (TexturePath: "AccCrest.png"); }}
    Label {{
      Anchor: (Left: 152, Top: 10, Width: 420, Height: 50);
      Text: "Eldanior";
      Style: (FontName: "Secondary", FontSize: 42, RenderBold: true, RenderUppercase: true, TextColor: {GOLD_TITLE}, VerticalAlignment: Center);
    }}
    Label #AccBrandSub {{
      Anchor: (Left: 154, Top: 58, Width: 440, Height: 26);
      Style: (FontSize: 14, RenderBold: true, RenderUppercase: true, TextColor: #b9a2d8, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 10);
    }}
    Group #AccSteps {{
      Anchor: (Left: {CARD_L0}, Top: 18, Width: {CARDS * CARD_PITCH - 32}, Height: 66);
{steps}
    }}
    Label #AccHeadline {{
      Anchor: (Left: {CARD_L0}, Top: 18, Width: {CARDS * CARD_PITCH - 32}, Height: 64);
      Visible: false;
      Style: (FontName: "Secondary", FontSize: 36, RenderBold: true, RenderUppercase: true, TextColor: {GOLD_TITLE}, HorizontalAlignment: Center, VerticalAlignment: Center);
    }}

    // ---- carrousel ----
{cards}
    Button #AccPrev {{
      Anchor: (Left: {CARD_L0 - 58}, Top: {CARD_T + CARD_H // 2 - 40}, Width: 40, Height: 80);
      Visible: false;
      Style: (Default: (Background: #000000(0)), Hovered: (Background: #000000(0)), Sounds: $C.@ButtonSounds);
      Group {{ Anchor: (Left: 0, Top: 0, Width: 40, Height: 80); Background: (TexturePath: "{Q}ChapArrowLeft.png"); }}
    }}
    Button #AccNext {{
      Anchor: (Left: {CARD_L0 + CARDS * CARD_PITCH - 14}, Top: {CARD_T + CARD_H // 2 - 40}, Width: 40, Height: 80);
      Visible: false;
      Style: (Default: (Background: #000000(0)), Hovered: (Background: #000000(0)), Sounds: $C.@ButtonSounds);
      Group {{ Anchor: (Left: 0, Top: 0, Width: 40, Height: 80); Background: (TexturePath: "{Q}ChapArrowRight.png"); }}
    }}
    Label #AccCount {{
      Anchor: (Left: {CARD_L0}, Top: 914, Width: {CARDS * CARD_PITCH - 32}, Height: 22);
      Style: (FontSize: 14, RenderBold: true, TextColor: #8a96a8, HorizontalAlignment: Center, VerticalAlignment: Center);
    }}

    // ---- actions ----
    Group {{
      Anchor: (Left: {CARD_L0}, Top: 948, Width: {CARDS * CARD_PITCH - 32}, Height: 48);
      LayoutMode: Center;
      $C.@SecondaryTextButton #AccBack {{
        @Anchor = (MinWidth: 180, Right: 16);
        Text: "Retour";
        Visible: false;
      }}
      $C.@SecondaryTextButton #AccAlt {{
        @Anchor = (MinWidth: 200, Right: 16);
        Text: "Relancer";
        Visible: false;
      }}
      $C.@TextButton #AccMain {{
        @Anchor = Anchor(MinWidth: 320);
        Text: "Suivant";
      }}
    }}
    Label #AccHint {{
      Anchor: (Left: {CARD_L0}, Top: 1004, Width: {CARDS * CARD_PITCH - 32}, Height: 24);
      Style: (FontSize: 15, RenderBold: true, TextColor: #8a96a8, HorizontalAlignment: Center, VerticalAlignment: Center);
    }}

    // ---- detail de la carte designee ----
    Group #AccDetail {{
      Anchor: (Left: 0, Top: 0, Width: 540, Height: 1080);

      Group {{ Anchor: (Left: {em_l}, Top: {em_t}, Width: {em}, Height: {em}); Background: (TexturePath: "{Q}ChapEmblemFrame.png"); }}
      Group {{ Anchor: (Left: {em_l + 27}, Top: {em_t + 25}, Width: {em - 54}, Height: {em - 54}); Background: (TexturePath: "{Q}ChapEmblemBack.png"); }}
      Label #AccMark {{
        Anchor: (Left: {em_l + 27}, Top: {em_t + 27}, Width: {em - 54}, Height: {em - 64});
        Style: (FontName: "Secondary", FontSize: 52, RenderBold: true, TextColor: #ffe98a, HorizontalAlignment: Center, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 26);
      }}

      Label #AccKicker {{
        Anchor: (Left: {TEXT_L}, Top: 270, Width: {TEXT_W}, Height: 22);
        Style: (FontSize: 14, RenderBold: true, RenderUppercase: true, TextColor: #b9a2d8, HorizontalAlignment: Center, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 10);
      }}
      Label #AccTitle {{
        Anchor: (Left: {TEXT_L}, Top: 294, Width: {TEXT_W}, Height: 44);
        Style: (FontName: "Secondary", FontSize: 30, RenderBold: true, RenderUppercase: true, TextColor: {GOLD_TITLE}, HorizontalAlignment: Center, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 18);
      }}
      Label #AccTagline {{
        Anchor: (Left: {TEXT_L}, Top: 338, Width: {TEXT_W}, Height: 28);
        Style: (FontSize: 19, RenderBold: true, TextColor: {TAGLINE}, HorizontalAlignment: Center, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 13);
      }}
      Group {{ Anchor: (Left: {PAGE_L}, Top: 372, Width: 418, Height: 8); Background: (TexturePath: "{Q}ChapDivider.png", HorizontalBorder: 24, VerticalBorder: 0); }}

      Group #AccBody {{
        Anchor: (Left: {TEXT_L}, Top: {body_t}, Width: {TEXT_W + 14}, Height: {1050 - body_t});
        LayoutMode: TopScrolling;
        ScrollbarStyle: $C.@DefaultScrollbarStyle;
        Padding: (Right: 14, Bottom: 12);

        Label #AccStory {{
          Style: (FontSize: 16, RenderBold: true, TextColor: #b8c2d0, Wrap: true);
        }}
{notes}
{head("AccPasHead", "Passifs de classe")}
{passives}
{head("AccRuneHead", "Runes de classe")}
{runes}
        Group #AccStatHead {{
          Anchor: (Top: 22, Height: 34);
          LayoutMode: Top;
          Label #AccStatHeadText {{ Anchor: (Height: 28); Style: (FontName: "Secondary", FontSize: 19, RenderBold: true, RenderUppercase: true, TextColor: {HEAD}, VerticalAlignment: Center); }}
          Group {{ Anchor: (Height: 2); Background: #c58643(0.45); }}
        }}
        Group #AccStats {{
          Anchor: (Top: 8, Height: 60);
          LayoutMode: Left;
{stats}
        }}
        Group #AccKitHead {{
          Anchor: (Top: 22, Height: 34);
          LayoutMode: Top;
          Label #AccKitHeadText {{ Anchor: (Height: 28); Style: (FontName: "Secondary", FontSize: 19, RenderBold: true, RenderUppercase: true, TextColor: {HEAD}, VerticalAlignment: Center); }}
          Group {{ Anchor: (Height: 2); Background: #c58643(0.45); }}
        }}
        Group #AccKitRow0 {{
          Anchor: (Top: 10, Height: {KIT_SLOT});
          LayoutMode: Left;
{kit0}
        }}
        Group #AccKitRow1 {{
          Anchor: (Top: 6, Height: {KIT_SLOT});
          LayoutMode: Left;
{kit1}
        }}
      }}
    }}
  }}
}}
'''
    g.check_ui(ui)
    os.makedirs(OUT, exist_ok=True)
    path = os.path.join(OUT, "Accueil.ui")
    with open(path, "w", encoding="utf-8") as f:
        f.write(ui)
    print("ecrit", path)


def paint(names):
    g.OUT = OUT                                  # paint_art et planche ecrivent / lisent dans g.OUT
    g.SCENES.update(SCENES)
    m = g.art_mask()
    for n in names:
        g.paint_art(n, m)
        print("illustration", n)


if __name__ == "__main__":
    build_ui()
    if "--ui" in sys.argv:
        sys.exit(0)
    g.OUT = OUT
    if "--blanc" in sys.argv:
        white_frames()
        print("cadres blancs")
        sys.exit(0)
    if "--fond" in sys.argv or not any(a.startswith("--art=") for a in sys.argv):
        white_frames()
        crest()
        print("blason")
        background()
        print("fond")
    only = [a.split("=", 1)[1] for a in sys.argv if a.startswith("--art=")]
    if "--fond" not in sys.argv or only:
        paint(only or NEW_ARTS)
    sheet = [a.split("=", 1)[1] for a in sys.argv if a.startswith("--planche=")]
    if sheet:
        g.planche(only or NEW_ARTS, sheet[0])
