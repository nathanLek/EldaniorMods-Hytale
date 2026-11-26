"""Designs uniques des consommables Eldanior (remplace les copies de l'oeuf vanilla et des parchemins de decor).

Seules les lignes Icon, Model et Texture des JSON changent : identifiants, effets et butins restent les memes.
Meme moteur que les butins (scripts/butin/sprites.py) : icone 64x64, sprite extrude en modele 3D.

Usage :
  python3 scripts/consommables/gen_consommables.py sheet <dossier>   planche de controle, rien n'est ecrit dans le mod
  python3 scripts/consommables/gen_consommables.py all               ecrit icones, modeles et JSON
"""
import json, math, multiprocessing, os, re, sys, zlib

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "butin"))
sys.path.insert(0, os.path.join(HERE, "..", "rune_aura_icons"))
import png  # noqa: E402
import sprites  # noqa: E402
from sprites import Art, GOLD, IVORY, PAPER, WOOD, GLASS, dk, lt, mix  # noqa: E402
import gen_butin  # noqa: E402

RES = os.path.join(HERE, "..", "..", "src", "main", "resources")
ITEMS = os.path.join(RES, "Server", "Item", "Items")

STATS = {  # stat : (couleur, seconde couleur, emblème)
    "Force": ("#c8302a", "#6a1616", "fist"), "Vitalite": ("#e8558a", "#8a2450", "life"), "Intelligence": ("#3a78e0", "#1c3a80", "mind"),
    "Endurance": ("#d07a28", "#70401a", "shield"), "Agilite": ("#3fb860", "#1c6a38", "plume"), "Chance": ("#e8c030", "#8a6a18", "clover"),
}


def emblem(l, kind, cx, cy, s=1.0):
    P = lambda x, y: (cx + x * s, cy + y * s)  # noqa: E731
    if kind == "fist":
        l.rect(*P(-2.6, -22), *P(2.6, 24))                               # hache a double tranchant
        l.poly([P(-3, -18), P(-12, -21), P(-23, -14), P(-23, 8), P(-12, 1), P(-3, -3)])
        l.poly([P(3, -18), P(12, -21), P(23, -14), P(23, 8), P(12, 1), P(3, -3)])
    elif kind == "life":
        l.blob([P(0, -9), P(9, -18), P(20, -8), P(16, 6), P(0, 21), P(-16, 6), P(-20, -8), P(-9, -18)])
    elif kind == "mind":
        l.poly([P(0, -23), P(6, -6), P(23, 0), P(6, 6), P(0, 23), P(-6, 6), P(-23, 0), P(-6, -6)])
        l.poly([P(13, -14), P(14, -8), P(20, -7), P(15, -4), P(13, 2), P(11, -4), P(6, -7), P(12, -8)])
    elif kind == "shield":
        l.poly([P(-18, -19), P(18, -19), P(18, 3), P(10, 15), P(0, 22), P(-10, 15), P(-18, 3)])
    elif kind == "plume":
        l.blob([P(16, -22), P(20, -6), P(8, 9), P(-8, 19), P(-12, 12), P(-6, -6), P(4, -17)])
        l.tube([P(10, -9), P(-19, 23)], 1.6 * s, 1.0 * s, val=0)
        l.tube([P(-8, 16), P(-19, 24)], 2 * s, 1.2 * s)
    elif kind == "clover":
        for dx, dy in ((-11, -11), (11, -11), (-11, 9), (11, 9)):
            l.ell(*P(dx, dy), 9 * s)
        l.tube([P(0, -2), P(3, 12), P(13, 25)], 2.6 * s, 1.6 * s)
    elif kind == "sword":
        l.poly([P(-3.4, -23), P(0, -28), P(3.4, -23), P(3.4, 8), P(-3.4, 8)])
        l.rect(*P(-11, 8), *P(11, 12))
        l.rect(*P(-2.4, 12), *P(2.4, 22))
    elif kind == "dagger":
        l.poly([P(0, -24), P(6, 2), P(-6, 2)])
        l.rect(*P(-9, 2), *P(9, 6))
        l.rect(*P(-2.2, 6), *P(2.2, 17))
    elif kind == "arrow":
        l.tube([P(-18, 18), P(14, -14)], 1.8 * s, 1.8 * s)
        l.poly([P(21, -21), P(16, -6), P(6, -16)])
        l.poly([P(-20, 12), P(-13, 20), P(-22, 25)]).poly([P(-13, 13), P(-7, 19), P(-14, 24)])
    elif kind == "cross":
        l.rect(*P(-3.6, -22), *P(3.6, 23))
        l.rect(*P(-15, -10), *P(15, -3))
    elif kind == "tower":
        l.rect(*P(-12, -8), *P(12, 20))
        for x in (-12, -3.2, 5.6):
            l.rect(*P(x, -16), *P(x + 6.4, -8))
    elif kind == "crown3":
        l.poly([P(-18, 12), P(-18, -10), P(-8, 0), P(0, -14), P(8, 0), P(18, -10), P(18, 12)])
    elif kind == "crown5":
        l.poly([P(-20, 14), P(-22, -12), P(-11, 0), P(-6, -16), P(0, -2), P(6, -16), P(11, 0), P(22, -12), P(20, 14)])
    elif kind == "crownduc":
        l.rect(*P(-22, 6), *P(22, 16))                                   # couronne ducale : bandeau et trois fleurons
        for x, h in ((-16, -8), (0, -16), (16, -8)):
            l.poly([P(x - 7, 7), P(x + 7, 7), P(x, h)])
            l.ell(*P(x, h - 3), 5 * s)
    elif kind == "chevrons":
        for y in (-16, -2, 12):
            l.tube([P(-15, y + 9), P(0, y - 5), P(15, y + 9)], 3.6 * s, 3.6 * s)
    return l


# ---------------- pilules ----------------
def pill_one(a, c, c2, e):                                               # gelule bicolore
    a.L(lt(c, .55), "none", 13, gloss=.9).tube([(18, 76), (46, 54)], 21, 21)
    a.L(c, "none", 13, gloss=.9).tube([(50, 50), (80, 28)], 21, 21)
    a.L(dk(c2, .3), "none", 2).tube([(36, 36), (60, 68)], 1.2, 1.2)
    emblem(a.L(lt(c, .8), "glow", 3), e, 67, 39, .58)
    a.L("#ffffff", "none", 3).tube([(16, 66), (30, 54)], 2.6, 1.4)


def pill_five(a, c, c2, e):                                              # pastille sertie d'or
    sides = {"fist": 6, "life": 12, "mind": 8, "shield": 5, "plume": 7, "clover": 10}[e]
    rim = [(50 + 42 * math.cos(i * 2 * math.pi / sides - 1.57), 50 + 42 * math.sin(i * 2 * math.pi / sides - 1.57)) for i in range(sides)]
    a.L(GOLD, "metal", 8, gloss=1).poly(rim)
    a.L(c, "facets", 14, gloss=.8).ell(50, 50, 31)
    emblem(a.L(lt(c, .7), "glow", 3), e, 50, 50, 1.0)
    b = a.L("#fff0b0", "glow", 3)
    for x, y in rim:
        b.ell(x, y, 3.2)


def pill_ten(a, c, c2, e):                                               # orbe ultime : rayons, couronne d'or
    r = a.L(lt(c, .25), "glow", 4)
    for i in range(12):
        t = i * math.pi / 6
        n = 47 if i % 2 == 0 else 38
        r.poly([(50 + 30 * math.cos(t - .17), 50 + 30 * math.sin(t - .17)), (50 + 30 * math.cos(t + .17), 50 + 30 * math.sin(t + .17)), (50 + n * math.cos(t), 50 + n * math.sin(t))])
    a.L(GOLD, "metal", 6, gloss=1).ell(50, 50, 35)
    a.L(c2, "void", 15, glow=lt(c, .4), gloss=.9).ell(50, 50, 29)
    emblem(a.L("#ffffff", "glow", 3, ), e, 50, 50, 1.02)
    a.L("#ffffff", "none", 3).ell(32, 32, 3.6, 2.2, rot=-40)


# ---------------- parchemins ----------------
def rolled(a, ribbon):                                                   # parchemin roule, ruban
    a.L(mix(PAPER, IVORY, .3), "grain", 12).tube([(18, 74), (82, 26)], 19, 19)
    a.L(mix(PAPER, WOOD, .3), "rings", 6).ell(84, 25, 19, 19)
    a.L(dk(PAPER, .35), "none", 2).tube([(84, 25), (88, 21), (80, 20)], 1.6, 1.6)
    a.L(ribbon, "weave", 4).tube([(36, 34), (64, 66)], 6.5, 6.5)


def open_scroll(a, trim):                                                # parchemin deroule
    a.L(PAPER, "grain", 5).rect(20, 16, 80, 84)
    r = a.L(trim, "rings", 7)
    r.tube([(13, 13), (87, 13)], 7.5, 7.5).tube([(13, 87), (87, 87)], 7.5, 7.5)
    t = a.L(dk(PAPER, .55), "none", 2)
    for y in (30, 39, 48):
        t.tube([(30, y), (70, y)], .9, .9)


def seal(a, col, em, x, y, r=15, em_col=None):
    a.L(col, "grain", 6, gloss=.8).blob([(x + r * (1 + .1 * math.sin(i * 2.5)) * math.cos(i * .785), y + r * (1 + .1 * math.sin(i * 2.5)) * math.sin(i * .785)) for i in range(8)])
    emblem(a.L(em_col or lt(col, .55), "none", 2, gloss=.4), em, x, y, r / 24)


def d_chevalier(a):
    rolled(a, "#2f5fb0")
    seal(a, "#2f5fb0", "sword", 50, 50, 18)


def d_baron(a):
    open_scroll(a, "#3f7a4a")
    a.L("#3f7a4a", "weave", 3).poly([(44, 60), (56, 60), (60, 96), (50, 90), (40, 96)])
    seal(a, "#2c6a3a", "tower", 50, 64, 16)


def d_comte(a):
    rolled(a, "#a82a2a")
    c = a.L(GOLD, "metal", 5, gloss=1)
    c.ell(18, 74, 8, 8)
    seal(a, "#a82a2a", "crown3", 50, 50, 20, "#f0c850")


def d_marquis(a):
    open_scroll(a, "#6a3aa8")
    b = a.L("#6a3aa8", "weave", 3)
    b.poly([(28, 66), (38, 66), (40, 96), (33, 90), (26, 96)]).poly([(62, 66), (72, 66), (74, 96), (67, 90), (60, 96)])
    seal(a, "#6a3aa8", "crown5", 33, 66, 13, "#f0c850")
    seal(a, "#5a2a98", "crown5", 67, 66, 13, "#f0c850")


def d_duc(a):
    a.L(GOLD, "metal", 6, gloss=1).rect(14, 18, 86, 86)
    a.L(PAPER, "grain", 4).rect(21, 25, 79, 79)
    t = a.L(dk(PAPER, .55), "none", 2)
    for y in (68, 73):
        t.tube([(32, y), (68, y)], .8, .8)
    emblem(a.L(GOLD, "metal", 4, gloss=1), "crownduc", 50, 46, 1.1)
    g = a.L("#c02a3a", "facets", 3)
    g.ell(30, 58, 2.6).ell(50, 58, 3).ell(70, 58, 2.6)
    a.L("#ffffff", "glow", 3).ell(14, 18, 4).ell(86, 18, 4).ell(14, 86, 4).ell(86, 86, 4)


# ---------------- benedictions ----------------
def b_pretre(a):                                                         # chapelet
    r = a.L(WOOD, "grain", 3)
    n = 15
    for i in range(n):
        t = i * 2 * math.pi / n
        r.ell(50 + 33 * math.cos(t), 36 + 28 * math.sin(t), 4.2)
    a.L("#c8b080", "metal", 3).tube([(50, 64), (50, 72)], 2, 2)
    emblem(a.L("#f0e8d8", "metal", 4, gloss=.9), "cross", 50, 74, .85)


def b_cardinal(a):                                                       # barrette rouge et anneau
    a.L("#a81c2a", "weave", 13, gloss=.4).blob([(12, 66), (20, 34), (50, 18), (80, 34), (88, 66), (50, 76)])
    a.L("#7a1420", "weave", 4).tube([(14, 66), (50, 77), (86, 66)], 5, 5)
    a.L("#d83a48", "none", 3).ell(50, 16, 7, 6)
    f = a.L("#6a0f1a", "none", 2)
    f.tube([(50, 22), (30, 66)], 1, 1).tube([(50, 22), (70, 66)], 1, 1)
    o = a.L(GOLD, "metal", 3, gloss=1)
    o.ell(50, 88, 9, 7)
    o.ell(50, 88, 5.6, 4, val=0)
    a.L("#c02a3a", "facets", 3).ell(50, 80, 4)


def b_archeveque(a):                                                     # crosse doree
    a.L("#e8e0d0", "metal", 4, gloss=1).tube([(40, 96), (44, 34)], 4, 4)
    pts = [(62 + (18 - t * 1.9) * math.cos(t + 2.9), 28 + (18 - t * 1.9) * math.sin(t + 2.9)) for t in [i * .45 for i in range(17)]]
    a.L(GOLD, "metal", 5, gloss=1).tube([(44, 40)] + pts, 6, 2.2)
    a.L("#3a5fc0", "facets", 3).ell(61, 29, 5)
    c = a.L(GOLD, "metal", 4, gloss=1)
    c.ell(44, 44, 9, 5).ell(43, 62, 6, 3.6)
    a.L("#ffffff", "glow", 3).ell(30, 20, 3).ell(20, 44, 2).ell(84, 58, 2.4)


# ---------------- elixirs ----------------
def glass(a, pts, liquid, liquid_pts, neck=(42, 58, 6, 26), cork=WOOD):
    x0, x1, y0, y1 = neck
    a.L(GLASS, "none", 9, gloss=1).blob(pts).rect(x0, y0 + 6, x1, y1 + 8)
    a.L(liquid, "glow", 13).blob(liquid_pts)
    a.L(cork, "grain", 4).rect(x0 - 2, y0, x1 + 2, y0 + 11)


def e_guerrier(a):                                                       # fiole carree, epee
    a.L(GLASS, "none", 7, gloss=1).poly([(22, 34), (78, 34), (84, 92), (16, 92)]).rect(40, 14, 60, 36)
    a.L("#b01e1a", "smoke", 9).poly([(26, 44), (74, 44), (79, 88), (21, 88)])
    a.L(WOOD, "grain", 4).rect(37, 6, 63, 18)
    emblem(a.L("#f0e8e0", "metal", 3, gloss=1), "sword", 50, 66, .85)


def e_assassin(a):                                                       # flasque noire effilee
    a.L("#1c1824", "void", 12, glow="#8a3ae0", gloss=1).blob([(50, 28), (72, 44), (66, 76), (50, 94), (34, 76), (28, 44)])
    a.L("#2a2436", "metal", 5, gloss=1).rect(44, 10, 56, 32)
    a.L("#7a2ad0", "facets", 4).ell(50, 8, 8, 6)
    emblem(a.L("#c8c4d8", "metal", 3, gloss=1), "dagger", 50, 62, .9)


def e_mage(a):                                                           # ballon rond, vapeur d'etoiles
    glass(a, [(50, 28), (76, 38), (84, 62), (70, 86), (30, 86), (16, 62), (24, 38)], "#2a6ae0", [(50, 38), (74, 50), (77, 66), (66, 82), (34, 82), (23, 66), (26, 50)], (42, 58, 8, 30), "#c8b8e8")
    emblem(a.L("#e0f0ff", "glow", 3), "mind", 50, 62, .8)
    v = a.L("#9ac8ff", "glow", 3)
    v.ell(68, 12, 3).ell(76, 22, 2).ell(32, 16, 2.4)


def e_archer(a):                                                         # tube effile, fleche
    a.L(GLASS, "none", 8, gloss=1).tube([(50, 90), (50, 22)], 19, 8)
    a.L("#1f8a3a", "smoke", 9).tube([(50, 86), (50, 44)], 16, 11)
    a.L("#8a6a3a", "grain", 4).rect(40, 8, 60, 20)
    emblem(a.L("#f0ead0", "metal", 3, gloss=.9), "arrow", 50, 62, .75)


def e_complet(a):                                                        # flacon cristal a six couleurs
    a.L(GOLD, "metal", 6, gloss=1).poly([(50, 18), (86, 40), (86, 74), (50, 96), (14, 74), (14, 40)])
    cols = [c for c, _, _ in STATS.values()]
    for i, c in enumerate(cols):
        t0, t1 = i * math.pi / 3 - 1.57, (i + 1) * math.pi / 3 - 1.57
        a.L(c, "glow", 4).poly([(50, 57), (50 + 30 * math.cos(t0), 57 + 30 * math.sin(t0)), (50 + 30 * math.cos(t1), 57 + 30 * math.sin(t1))])
    a.L("#ffffff", "glow", 6).ell(50, 57, 7)
    a.L(GOLD, "metal", 4, gloss=1).rect(42, 6, 58, 20)


# ---------------- speciaux ----------------
def s_dignite(a):                                                        # goutte divine dans un halo
    h = a.L(GOLD, "metal", 3, gloss=1)
    h.ell(50, 54, 40, 40)
    h.ell(50, 54, 35, 35, val=0)
    r = a.L("#fff0b0", "glow", 3)
    for i in range(8):
        t = i * .785 + .39
        r.tube([(50 + 39 * math.cos(t), 54 + 39 * math.sin(t)), (50 + 48 * math.cos(t), 54 + 48 * math.sin(t))], 2.4, .8)
    a.L("#ffffff", "frost", 14, glow="#ffe890", gloss=1).blob([(50, 16), (62, 40), (70, 60), (60, 78), (50, 82), (40, 78), (30, 60), (38, 40)])
    a.L("#ffd860", "glow", 8).ell(50, 62, 10, 11)


def s_relance(a):                                                        # parchemin, fleches en boucle
    a.L(PAPER, "grain", 5).rect(18, 18, 82, 82)
    r = a.L(mix(PAPER, WOOD, .35), "rings", 6)
    r.tube([(12, 16), (88, 16)], 6.5, 6.5).tube([(12, 84), (88, 84)], 6.5, 6.5)
    l = a.L("#22a8a0", "glow", 3)
    for k in (0, math.pi):
        pts = [(50 + 21 * math.cos(k + t), 50 + 21 * math.sin(k + t)) for t in [.25 + i * .28 for i in range(9)]]
        l.tube(pts, 3.4, 3.4)
        e = k + .25 + 8 * .28 + .1
        ex, ey = 50 + 21 * math.cos(e), 50 + 21 * math.sin(e)
        d = e + 1.57
        l.poly([(ex + 11 * math.cos(d), ey + 11 * math.sin(d)), (ex + 8 * math.cos(e), ey + 8 * math.sin(e)), (ex - 8 * math.cos(e), ey - 8 * math.sin(e))])


def book(a, cover, band, em, em_col):
    a.L(PAPER, "grain", 4).poly([(24, 16), (86, 12), (88, 84), (26, 90)])
    a.L(cover, "weave", 8, gloss=.4).poly([(14, 14), (78, 8), (80, 82), (18, 90)])
    a.L(band, "metal", 4, gloss=1).rect(12, 10, 24, 92)
    c = a.L(band, "metal", 3, gloss=1)
    c.poly([(66, 8), (80, 8), (80, 22)]).poly([(66, 84), (80, 72), (80, 84)])
    emblem(a.L(em_col, "glow", 3), em, 51, 46, 1.05)


def s_xp(a):
    book(a, "#1f7a4a", "#c8d850", "mind", "#d8ff70")


def s_niveau(a):
    book(a, "#4a2a90", GOLD, "chevrons", "#ffd860")


DESIGNS = {}
for st, (c, c2, e) in STATS.items():
    for tier, fn in (("One", pill_one), ("Five", pill_five), ("Ten", pill_ten)):
        DESIGNS["Food/Consomable_Stat_%s_%s" % (st, tier)] = (lambda f, x, y, z: (lambda a: f(a, x, y, z)))(fn, c, c2, e)
DESIGNS.update({"Food/Decret_Chevalier": d_chevalier, "Food/Decret_Baron": d_baron, "Food/Decret_Comte": d_comte, "Food/Decret_Marquis": d_marquis,
                "Food/Decret_Duc": d_duc, "Food/Benediction_Pretre": b_pretre, "Food/Benediction_Cardinal": b_cardinal,
                "Food/Benediction_Archeveque": b_archeveque, "Food/Elixir_Guerrier": e_guerrier, "Food/Elixir_Assassin": e_assassin,
                "Food/Elixir_Mage": e_mage, "Food/Elixir_Archer": e_archer, "Food/Elixir_Complet": e_complet, "Food/Essence_Dignite": s_dignite,
                "Food/Parchemin_Relance": s_relance, "Food/Tome_Experience": s_xp, "Food/Tome_Niveau": s_niveau})


def render(key):
    iid = key.split("/")[1]
    a = Art(zlib.crc32(iid.encode()), spin=5, zoom=(.97, 1.02), flip=False)
    DESIGNS[key](a)
    return key, sprites.render(a, "#14101c")


def all_art():
    with multiprocessing.Pool() as pool:
        return dict(pool.map(render, sorted(DESIGNS)))


def sheet(folder):
    art = all_art()
    keys = sorted(art)
    cols, cell = 9, 136
    rows = -(-len(keys) // cols)
    W, H = cols * cell, rows * cell
    px = [[[46, 42, 50, 255] for _ in range(W)] for _ in range(H)]
    for n, k in enumerate(keys):
        ic = art[k][0]
        ox, oy = (n % cols) * cell + 4, (n // cols) * cell + 4
        for y in range(128):
            for x in range(128):
                c = ic[y // 2][x // 2]
                if c[3]:
                    t = c[3] / 255
                    d = px[oy + y][ox + x]
                    px[oy + y][ox + x] = [d[i] + (c[i] - d[i]) * t for i in range(3)] + [255]
    png.write(os.path.join(folder, "consommables.png"), W, H, px)
    print(os.path.join(folder, "consommables.png"))


def write_all():
    art = all_art()
    models = os.path.join(RES, "Common", "Items", "Consommables")
    icons = os.path.join(RES, "Common", "Icons", "ItemsGenerated")
    os.makedirs(models, exist_ok=True)
    for key, (ic, sp) in art.items():
        iid = key.split("/")[1]
        png.write(os.path.join(icons, iid + ".png"), 64, 64, ic)
        mdl, tex = gen_butin.model(sp)
        png.write(os.path.join(models, iid + ".png"), 64, 32, tex)
        json.dump(mdl, open(os.path.join(models, iid + ".blockymodel"), "w"), separators=(",", ":"))
        path = os.path.join(ITEMS, key + ".json")
        s = open(path, encoding="utf-8").read()
        s = re.sub(r'"Icon": "[^"]*"', '"Icon": "Icons/ItemsGenerated/%s.png"' % iid, s, count=1)
        s = re.sub(r'"Model": "[^"]*"', '"Model": "Items/Consommables/%s.blockymodel"' % iid, s, count=1)
        s = re.sub(r'"Texture": "[^"]*"', '"Texture": "Items/Consommables/%s.png"' % iid, s, count=1)
        json.loads(s)
        open(path, "w", encoding="utf-8").write(s)
    print("consommables redessines :", len(art))


if __name__ == "__main__":
    if sys.argv[1] == "sheet":
        sheet(sys.argv[2])
    else:
        write_all()
