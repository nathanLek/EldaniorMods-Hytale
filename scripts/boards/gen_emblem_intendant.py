"""Embleme de la fenetre de l'intendant (Board/BoardEmblemIntendant@2x.png, guards/StewardScreen) : meme losange que les
emblemes des tableaux (gen_boards.py), registre relie de cuir rouge (fermoir d'or, signet) barre d'une cle d'or.

Usage : python3 scripts/boards/gen_emblem_intendant.py [src/main/resources]
"""
import math, os, sys

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'rune_aura_icons'))
import png  # noqa: E402

RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "src", "main", "resources")
E = 192
BORDER, FILL, GOLD, GOLD_DARK, PALE = (214, 160, 62), (26, 34, 49), (232, 169, 59), (150, 98, 30), (246, 222, 140)
LEATHER, LEATHER_DARK, PAGES, RIBBON = (128, 34, 30), (84, 20, 20), (236, 220, 178), (52, 92, 160)


def key(u, v):
    """Cle d'or en diagonale (anneau en haut a gauche, panneton en bas a droite)."""
    a = (u + v) / math.sqrt(2)                                                          # le long de la cle
    b = (u - v) / math.sqrt(2)                                                          # en travers
    ring = math.hypot(a + 0.42, b)
    if 0.07 <= ring <= 0.14:
        return PALE if b < -0.02 and ring > 0.11 else GOLD
    if -0.3 <= a <= 0.44 and abs(b) <= 0.032:
        return PALE if b < -0.012 else GOLD
    if 0.26 <= a <= 0.44 and 0.03 <= b <= 0.14 and not (0.32 <= a <= 0.36 and b > 0.07):
        return GOLD_DARK if b > 0.1 else GOLD
    if -0.3 <= a <= -0.25 and abs(b) <= 0.06:
        return GOLD_DARK
    return None


def ledger(u, v):
    """Registre ferme vu de face : tranche des pages a droite, couverture de cuir, coins et fermoir d'or, signet bleu."""
    if not (-0.34 <= u <= 0.3 and -0.4 <= v <= 0.4):
        return None
    if u > 0.24:
        return PAGES if int((v + 0.4) * 60) % 2 == 0 else (206, 190, 150)              # tranche
    if -0.1 <= u <= -0.04 and v > 0.3:
        return RIBBON
    corner = (abs(u + 0.34) < 0.08 or abs(u - 0.24) < 0.08) and (abs(v + 0.4) < 0.08 or abs(v - 0.4) < 0.08)
    if corner:
        return GOLD
    if u < -0.27:
        return LEATHER_DARK                                                             # dos
    if abs(v) < 0.05 and u > 0.12:
        return GOLD_DARK if abs(v) > 0.03 else GOLD                                     # fermoir
    if -0.2 <= u <= 0.1 and -0.26 <= v <= -0.16:
        return GOLD_DARK                                                                # etiquette de titre
    return LEATHER


def draw(u, v):
    return key(u, v) or ledger(u, v)


px = []
for y in range(E):
    row = []
    for x in range(E):
        u, v = (x + 0.5) / E * 2 - 1, (y + 0.5) / E * 2 - 1
        d = abs(u) + abs(v)
        col, a = [0, 0, 0], 0.0
        if d <= 0.97:
            if d >= 0.88:
                col, a = list(BORDER), 1.0
            else:
                glow = max(0.0, 1 - math.hypot(u, v) / 0.75)
                col, a = [c + (g - c) * 0.25 * glow for c, g in zip(FILL, GOLD)], 1.0
                c = draw(u, v)
                if c is not None:
                    col = list(c)
            if d > 0.95:
                a *= max(0.0, (0.97 - d) / 0.02)
        row.append([col[0], col[1], col[2], a * 255])
    px.append(row)
out = os.path.join(RES, "Common", "UI", "Custom", "Board", "BoardEmblemIntendant@2x.png")
png.write(out, E, E, px)
print("ecrit", out)
