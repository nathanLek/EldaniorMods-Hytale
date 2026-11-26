"""Objets de la voie martiale du Clan Luo (2026-11-09) : le Rouleau de la Voie Martiale (entree dans la voie) et les manuels
secrets qui enseignent les techniques (runes du type Luo).

  Eldanior_Parchemin_Voie_Martiale   makimono : rouleau de soie indigo brode d'or, embouts de laque noire et d'or, cordon
                                     cramoisi noue, etiquette blanche a l'encre. Clic droit = fenetre de serment
                                     (com.eldanior.system.luo.VoieMartialePage) : le personnage perd tout sauf 5 % de ses
                                     caracteristiques et recommence Apprenti. Ouvert a tous, pas seulement au Clan Luo.
  Server/Emote/Luo_Meditation        emote (corps entier, objet cache) jouee par la Meditation martiale.
  Eldanior_Manuel_Luo_<Technique>    fukuro-toji (livre cousu a la japonaise) : couverture de soie de la couleur de la technique,
                                     motif tisse, etiquette de titre blanche (daisen), mon d'or, couture blanche sur le dos, coins
                                     de soie cramoisie ; pages crème en colonnes d'encre, sceau rouge. Maintenir le clic droit :
                                     le livre s'ouvre et ses pages tournent (animation Lecture du jeu Luo_Manuel), et au bout de
                                     2,5 s la technique est apprise (sort Java LuoManuelApprendre), le livre disparait.

Le livre reprend exactement la geometrie du livre de sorts vanilla (Items/Weapons/Spellbook/Book.blockymodel : Handle,
Book-Top, Book-Bot, Page-Top, Page-Bot, Bookmark) : les animations du joueur animent ces noeuds (ouverture, pages qui
tournent). Une texture par manuel ; la disposition de l'atlas est la meme pour tous (verifiee a l'ecriture).
Animation Lecture (troisieme personne) = pose vanilla Poses/Book_Read (corps) + noeuds du livre de Cast_Hurl_Charging
(couvertures ouvertes, pages qui tournent) ; premiere personne = Cast_Hurl_Charging_FPS vanilla.

Aucune recette, aucun butin : le Clan Luo n'est pas ouvert au monde exterieur.

Usage : python3 scripts/familles/gen_voie_martiale_objets.py [src/main/resources] [dossier_apercu]
"""
import copy, json, math, os, sys, zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
for folder in ("armures", "rune_aura_icons", "artefacts"):
    sys.path.insert(0, os.path.join(HERE, "..", folder))
import png  # noqa: E402
import armor_kit as K  # noqa: E402
import blason  # noqa: E402
import familles  # noqa: E402

RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "..", "..", "src", "main", "resources")
PREVIEW = sys.argv[2] if len(sys.argv) > 2 else None
COMMON, SERVER = os.path.join(RES, "Common"), os.path.join(RES, "Server")
ZIP = os.path.expanduser("~/Library/Application Support/Hytale/install/release/package/game/latest/Assets.zip")

D = 2
K.set_density(D, 256)
FOLDER = "Items/Eldanior/Luo/Voie"
BOOK_MODEL = FOLDER + "/Manuel_Luo.blockymodel"
SCROLL_MODEL = FOLDER + "/Rouleau_Voie_Martiale.blockymodel"
ANIM_SET = "Luo_Manuel"
READ_S = 2.5                                                         # duree de lecture (charge de Luo_Manuel_Lecture)
GOLDS = (familles.GOLD_DARK, familles.GOLD, familles.GOLD_LIGHT)
PAPER, PAPER_SHADE = (238, 228, 204), (206, 192, 162)
INK = (34, 26, 22)
SEAL = (178, 36, 30)
CRIMSON = (150, 26, 38)

# technique (cle courte de la rune), nom, palier requis, qualite, couleur de couverture
# (cle, nom, palier de la voie martiale OU (clan, niveau) pour une technique de clan, qualite, couleur de couverture)
BOOKS = [
    ("KiEpee", "Ki de l'Épée", 1, "Rare", (30, 42, 82)),
    ("MeditationMartiale", "Méditation martiale", 1, "Rare", (58, 78, 56)),
    # competences de palier de la voie martiale (2026-11-10)
    ("RenforcementCorps", "Renforcement corporel", 2, "Uncommon", (150, 110, 50)),
    ("PasLeger", "Pas léger", 3, "Rare", (190, 200, 210)),
    ("SensMartial", "Sens martial", 4, "Rare", (40, 70, 110)),
    ("ProjectionEpeeAura", "Projection d'épée d'Aura", 5, "Epic", (90, 150, 190)),
    ("VolEpeeKi", "Vol sur épée de Ki", 6, "Unique", (60, 120, 170)),
    ("IncarnationAme", "Incarnation de l'Âme", 7, "Legendary", (236, 232, 222)),
    ("DomaineDivin", "Domaine Divin", 8, "Legendary", (200, 160, 60)),
    # techniques des clans de l'Empire Luo (anciennes runes de palier, LuoClans.TECHNIQUES)
    ("PaumeKi", "Paume de Ki", ("Sama", 20), "Common", (112, 66, 40)),
    ("FrappeMeridiens", "Frappe des Méridiens", ("Sama", 100), "Uncommon", (128, 30, 40)),
    ("TransformationHwakyung", "Transformation Hwakyung", ("Sama", 500), "Legendary", (226, 218, 200)),
    ("DomaineEpee", "Domaine de l'Épée", ("Hanazakura", 300), "Epic", (70, 36, 96)),
    ("FleurPrunier", "Fleur de Prunier", ("Hanazakura", 400), "Unique", (150, 70, 100)),
    ("UniteCielTerre", "Unité du Ciel et de la Terre", ("Hanazakura", 600), "Legendary", (20, 18, 22)),
    ("GardeFleuve", "Garde du Fleuve", ("Setsuka", 150), "Rare", (28, 92, 104)),
    ("CroissantKi", "Croissant de Ki", ("Setsuka", 220), "Rare", (22, 30, 60)),
]
# Mouvements de famille (2026-11-12) : ~12 competences par famille, manuel stylise de la famille (build_family_book).
# (numero, rang martial requis 1-8, cle de la rune ou None = technique a venir, nom ou None = pas encore nomme). Le 1er est
# remis a l'entree dans la famille, les autres sont vendus par le Sensei selon le rang. A garder en accord avec
# titles/nobility/family/FamilyMovements.java. Rangs du Clan Luo choisis par l'utilisateur : « gros mouvements en fin de voie ».
LUO_TIERS = [1, 2, 3, 4, 5, 6, 6, 7, 7, 7, 8, 8]
MOVEMENTS = {"Luo": [(i + 1, LUO_TIERS[i], "ExecutionCeleste" if i == 0 else None, "Exécution Céleste" if i == 0 else None)
                     for i in range(12)]}
MOVEMENT_QUALITY = {1: "Rare", 2: "Rare", 3: "Epic", 4: "Epic", 5: "Unique", 6: "Unique", 7: "Legendary", 8: "Legendary"}
FAMILY_BOOK_MODEL = FOLDER + "/Manuel_Famille_Luo.blockymodel"
# anciens fichiers retires (l'Execution Celeste est devenue le 1er Mouvement)
OBSOLETE = ["Server/Item/Items/Eldanior/Luo/Eldanior_Manuel_Luo_ExecutionCeleste.json",
            "Common/" + FOLDER + "/Eldanior_Manuel_Luo_ExecutionCeleste_Texture.png",
            "Common/Icons/ItemsGenerated/Eldanior_Manuel_Luo_ExecutionCeleste.png"]


def ordinal(n):
    return "1er" if n == 1 else "%de" % n


def movement_item(family, n):
    return "Eldanior_Mouvement_%s_%02d" % (family, n)


def movement_title(n, name):
    return ordinal(n) + " Mouvement" + (" : " + name if name else "")
RANKS = {1: "Apprenti en Arts Martiaux", 2: "Disciple Martial", 3: "Expert Martial", 4: "Maître Martial",
         5: "Grand Maître Martial", 6: "Saint Martial", 7: "Maître Suprême", 8: "Dieu Martial"}


def shade(c, k):
    return tuple(max(0, min(255, v * k)) for v in c)


# ---------------------------------------------------------------------------------------------------------------------
# Peintures
# ---------------------------------------------------------------------------------------------------------------------
_MON = {}


def mon(px):
    if px not in _MON:
        k = px * 4 / 2.1
        cv = blason.Canvas(k, -1.05, 1.05, -1.0, 1.1)
        familles.coiled_dragon(cv)
        _MON[px] = blason.shrink(cv.img, px, px)
    return _MON[px]


def stamp(img, src, cx, cy, alpha=1.0):
    h, w = len(src), len(src[0])
    for y in range(h):
        for x in range(w):
            p = src[y][x]
            if p[3]:
                K.put(img, cx - w / 2.0 + x, cy - h / 2.0 + y, p[:3], alpha * p[3] / 255.0)


def silk(w, h, c, seed):
    """Soie tissee : petit motif de losanges (hishi) en ton sur ton."""
    img = K.canvas(w, h, c)
    for y in range(h):
        for x in range(w):
            d = (abs((x + seed) % 6 - 3) + abs(y % 6 - 3))
            k = 1.08 if d == 2 else 0.95 if d == 0 else 1.0
            k *= 0.94 + 0.12 * K.noise(x * 0.4 + seed, y * 0.4)
            for i in range(3):
                img[y][x][i] *= k
    return img


def ink_columns(img, x0, y0, x1, y1, seed, density=0.55):
    """Colonnes de signes a l'encre, de droite a gauche."""
    col_w = 3
    x = x1 - col_w
    n = seed
    while x >= x0:
        y = y0
        while y < y1 - 2:
            n = (n * 1103515245 + 12345) & 0x7fffffff
            if (n % 100) / 100.0 < density:
                h = 1 + n % 2
                K.rect(img, x, y, x + 2, y + h, INK, 0.85)
                if n % 5 == 0:
                    K.put(img, x + 1, y + h, INK, 0.7)
            y += 3
        x -= col_w + 1


def cover_paint(color, title_seed, outer):
    """outer : face exterieure de la moitie ("top" pour Book-Top, "bottom" pour Book-Bot) ; l'autre plat est l'interieur."""
    def paint(face, w, h, seed):
        if face in ("top", "bottom") and face != outer:            # interieur : page imprimee, visible livre ouvert
            img = page_img(w, h, seed + title_seed, face == "bottom")
            K.rect(img, 0, 0, 2, h, shade(color, 0.9))                  # rembord de soie le long du dos
            return K.finish(img, face, edges=False)
        if face in ("top", "bottom"):                              # plat de couverture
            img = silk(w, h, color, seed)
            light = sum(color) > 450
            border = shade(color, 0.7 if light else 1.35)
            K.rect(img, 0, 0, w, 1, border)
            K.rect(img, 0, h - 1, w, h, border)
            K.rect(img, w - 1, 0, w, h, border)
            # etiquette de titre (daisen), le long du bord libre, en haut
            lx0, lx1, ly0, ly1 = int(w * 0.62), int(w * 0.84), int(h * 0.06), int(h * 0.62)
            K.rect(img, lx0 - 1, ly0 - 1, lx1 + 1, ly1 + 1, shade(PAPER, 0.75))
            K.rect(img, lx0, ly0, lx1, ly1, PAPER)
            ink_columns(img, lx0 + 2, ly0 + 2, lx1 - 1, ly1 - 2, title_seed, 0.8)
            # mon d'or en bas
            stamp(img, mon(max(8, int(w * 0.32))), w * 0.42, h * 0.8)
            # coins de soie cramoisie (kadogire), cote couture
            K.rect(img, 0, 0, 3, 3, CRIMSON)
            K.rect(img, 0, h - 3, 3, h, CRIMSON)
            return K.finish(img, face, edges=False)
        # tranches : pile de feuilles
        img = K.canvas(w, h, PAPER)
        for y in range(h):
            for x in range(w):
                if (y + (x // 7)) % 2 == 0:
                    for i in range(3):
                        img[y][x][i] *= 0.9
        K.rect(img, 0, 0, w, 1, shade(color, 0.9))
        K.rect(img, 0, h - 1, w, h, shade(color, 0.9))
        return K.finish(img, face, edges=False)
    return paint


def spine_paint(color):
    def paint(face, w, h, seed):
        img = silk(w, h, shade(color, 0.85), seed)
        # couture yotsume-toji : fil blanc qui passe par quatre trous, le long du dos
        n = 4
        for i in range(n):
            if face in ("left", "right", "front", "back"):
                cx = int((i + 0.5) * w / n)
                K.rect(img, cx - 1, 0, cx + 1, h, (236, 232, 222))
                K.put(img, cx, h // 2, (60, 50, 40))
            else:
                cy = int((i + 0.5) * h / n)
                K.rect(img, 0, cy, w, cy + 1, (236, 232, 222))
        return K.finish(img, face, edges=False)
    return paint


def page_img(w, h, seed, seal):
    img = K.canvas(w, h, PAPER)
    for y in range(h):
        for x in range(w):
            k = 0.95 + 0.07 * K.noise(x * 0.3 + seed, y * 0.3)
            for i in range(3):
                img[y][x][i] *= k
    K.rect(img, 1, 1, w - 1, 2, shade(PAPER, 0.8))                  # cadre d'impression
    K.rect(img, 1, h - 2, w - 1, h - 1, shade(PAPER, 0.8))
    ink_columns(img, 3, 4, w - 3, h - 4, seed)
    if seal:
        K.rect(img, 3, h - 10, 9, h - 4, SEAL, 0.9)                     # sceau rouge
        K.rect(img, 4, h - 9, 8, h - 5, shade(SEAL, 1.25), 0.6)
    return img


def bookmark_img(w, h):
    img = K.canvas(w, h, CRIMSON)
    for y in range(h):
        for x in range(w):
            a = 1.0 if abs(x - w / 2.0) < 1.6 else 0.0                   # cordon de soie
            if y > h * 0.7:
                a = 1.0 if abs(x - w / 2.0) < 1 + (y - h * 0.7) * 0.4 else 0.0   # gland
            img[y][x][3] = int(255 * a)
            if a:
                k = 0.85 + 0.25 * ((x + y) % 2)
                for i in range(3):
                    img[y][x][i] *= k
    return img


# ---------------------------------------------------------------------------------------------------------------------
# Modele du livre : geometrie du livre de sorts vanilla
# ---------------------------------------------------------------------------------------------------------------------
def empty(m, name, pos, q, children):
    return {"id": m.nid(), "name": name, "position": pos, "orientation": q,
            "shape": {"type": "none", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": 1, "y": 1, "z": 1}, "settings": {},
                      "textureLayout": {}, "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": "flat"},
            "children": children}


def boxn(m, name, pos, size, offset, paint, q=K.ID_Q):
    n = K.box(m, name, (0, 0, 0), size, paint, q=q)
    n["position"] = pos
    n["shape"]["offset"] = offset
    return n


def quadn(m, name, pos, img, offset, q, normal):
    n = K.quad(m, name, (0, 0, 0), img, q=q)
    n["position"] = pos
    n["shape"]["offset"] = offset
    n["shape"]["settings"]["normal"] = normal
    return n


def build_book(color, seed):
    m = K.Model("Manuel_Luo")
    cover_bot, cover_top = cover_paint(color, seed, "bottom"), cover_paint(color, seed, "top")
    page_w, page_h = 18 * D, 26 * D
    w1 = {"x": 0, "y": 0, "z": 0, "w": -1}
    bot = boxn(m, "Book-Bot", {"x": 3, "y": 0, "z": 0}, (20, 4, 26), {"x": 7, "y": -2, "z": 0}, cover_bot)
    bot["children"] = [
        quadn(m, "Bookmark", {"x": 1, "y": 1, "z": 13}, bookmark_img(7 * D, 10 * D), {"x": 0, "y": -5, "z": 0},
              {"x": -0.608761, "y": 0, "z": 0, "w": 0.793353}, "+Z"),
        quadn(m, "Page-Bot", {"x": -7, "y": 2.1, "z": 0}, page_img(page_w, page_h, seed + 1, False), {"x": 8, "y": 0, "z": 0}, w1, "-Y"),
    ]
    top = boxn(m, "Book-Top", {"x": 3, "y": 0, "z": 0}, (20, 4, 26), {"x": 7, "y": 2, "z": 0}, cover_top)
    top["children"] = [quadn(m, "Page-Top", {"x": -7, "y": -2.1, "z": 0}, page_img(page_w, page_h, seed + 2, True),
                             {"x": 8, "y": 0, "z": 0}, w1, "-Y")]
    handle = boxn(m, "Handle", {"x": -2, "y": 0, "z": -8}, (6, 9, 27), {"x": 0, "y": -2, "z": 0}, spine_paint(color),
                  q={"x": 0.5, "y": -0.5, "z": 0.5, "w": 0.5})
    handle["children"] = [bot, top]
    origin_item = empty(m, "Origin_Item", {"x": 0, "y": -9.192388, "z": 0.707107}, {"x": -0.382683, "y": 0, "z": 0, "w": 0.92388}, [handle])
    origin_proj = empty(m, "Origin_Projectile", {"x": 2, "y": 7, "z": 14}, {"x": 0.382683, "y": 0, "z": 0, "w": 0.92388}, [origin_item])
    root = empty(m, "R-Attachment", {"x": -2, "y": 13, "z": -8}, dict(K.ID_Q), [origin_proj])
    return {"nodes": [root], "lod": "auto"}, m


# ---------------------------------------------------------------------------------------------------------------------
# Manuel de famille (les 12 Mouvements, 2026-11-12) : meme geometrie que les manuels (animations du livre de sorts), habille
# en livre de tresor : plats de laque noire semee d'or (nashiji) et de nuages d'or, double filet d'or, grand mon d'or ;
# etiquette de soie cramoisie portant le numero du Mouvement en chiffres japonais d'or ; tranches dorees ; dos de soie
# cramoisie cousu d'or ; quatre coins de metal dore (kanagu) en relief.
# ---------------------------------------------------------------------------------------------------------------------
LACQUER = (20, 16, 18)
GLYPHS = {   # chiffres japonais, 7 x 7, haut en premiere ligne
    1: [".......", ".......", ".......", "#######", ".......", ".......", "......."],
    2: [".......", ".#####.", ".......", ".......", ".......", "#######", "......."],
    3: [".#####.", ".......", ".......", "..###..", ".......", ".......", "#######"],
    4: ["#######", "#.#.#.#", "#.#.#.#", "#.#..##", "##....#", "#.....#", "#######"],
    5: ["#######", "...#...", "...#...", ".#####.", ".#...#.", ".#...#.", "#######"],
    6: ["...#...", "...#...", "#######", ".......", "..#.#..", ".#...#.", "#.....#"],
    7: [".#.....", ".#...##", ".####..", ".#.....", ".#.....", ".#....#", "..#####"],
    8: ["..#.#..", "..#.#..", "..#.#..", "..#..#.", ".#...#.", ".#....#", "#.....#"],
    9: ["..#....", "..#....", "######.", "..#..#.", "..#..#.", ".#...#.", "#....##"],
    10: ["...#...", "...#...", "#######", "...#...", "...#...", "...#...", "...#..."],
}


def numeral_glyphs(n):
    """Signes du nombre n (1 a 12) en chiffres japonais : 十二 = [10, 2]."""
    return [n] if n <= 10 else [10, n - 10]


def draw_glyph(img, glyph, x0, y0, c, flip=False):
    rows = GLYPHS[glyph]
    for gy, row in enumerate(rows):
        for gx, ch in enumerate(row):
            if ch == "#":
                yy = (6 - gy) if flip else gy
                K.put(img, x0 + gx, y0 + yy, c)


def lacquer_ground(w, h, seed):
    """Laque noire : poudre d'or eparse (nashiji) et volutes de nuages d'or."""
    img = K.canvas(w, h, LACQUER)
    for y in range(h):
        for x in range(w):
            k = 0.9 + 0.2 * K.noise(x * 0.25 + seed, y * 0.25)
            for i in range(3):
                img[y][x][i] *= k
            n = (x * 73856093 ^ y * 19349663 ^ seed * 83492791) & 0xffff
            if n % 23 == 0:
                K.put(img, x, y, familles.GOLD, 0.35 + (n % 7) * 0.06)
    for cx, cy, r in ((0.2, 0.25, 0.16), (0.78, 0.7, 0.2), (0.3, 0.88, 0.12)):     # nuages d'or (kumo)
        for a in range(0, 360, 6):
            t = math.radians(a)
            rr = r * (1 + 0.25 * math.sin(3 * t))
            K.put(img, w * cx + math.cos(t) * rr * w, h * cy + math.sin(t) * rr * w * 0.6, familles.GOLD_DARK, 0.7)
    return img


def family_cover_paint(number, title_seed, outer, flip):
    def paint(face, w, h, seed):
        if face in ("top", "bottom") and face != outer:            # interieur : page imprimee
            img = page_img(w, h, seed + title_seed, face == "bottom")
            K.rect(img, 0, 0, 2, h, CRIMSON)
            return K.finish(img, face, edges=False)
        if face in ("top", "bottom"):                              # plat de laque
            img = lacquer_ground(w, h, seed)
            for inset, c in ((0, familles.GOLD_DARK), (2, familles.GOLD)):          # double filet d'or
                K.rect(img, inset, inset, w - inset, inset + 1, c)
                K.rect(img, inset, h - inset - 1, w - inset, h - inset, c)
                K.rect(img, inset, inset, inset + 1, h - inset, c)
                K.rect(img, w - inset - 1, inset, w - inset, h - inset, c)
            # etiquette de soie cramoisie (daisen) bordee d'or, numero du Mouvement en or
            lx0, lx1, ly0, ly1 = int(w * 0.60), int(w * 0.86), int(h * 0.07), int(h * 0.56)
            K.rect(img, lx0 - 1, ly0 - 1, lx1 + 1, ly1 + 1, familles.GOLD)
            label = silk(lx1 - lx0, ly1 - ly0, CRIMSON, seed)
            for y, row in enumerate(label):
                for x, p in enumerate(row):
                    K.put(img, lx0 + x, ly0 + y, p[:3])
            glyphs = numeral_glyphs(number)
            if flip:
                glyphs = glyphs[::-1]
            total = 7 * len(glyphs) + 2 * (len(glyphs) - 1)
            gx = lx0 + (lx1 - lx0 - 7) // 2
            gy = ly0 + 3 if not flip else ly1 - 3 - total
            for g in glyphs:
                draw_glyph(img, g, gx, gy, familles.GOLD_LIGHT, flip)
                gy += 9
            # trois traits d'encre d'or sous le numero (« Mouvement »)
            for i in range(3):
                yy = (ly1 - 4 - i * 2) if not flip else (ly0 + 3 + i * 2)
                K.rect(img, gx + 1, yy, gx + 6, yy + 1, familles.GOLD_DARK)
            stamp(img, mon(max(10, int(w * 0.44))), w * 0.36, h * 0.72)       # grand mon d'or
            return K.finish(img, face, edges=False)
        # tranches : pages dorees
        img = K.canvas(w, h, familles.GOLD)
        for y in range(h):
            for x in range(w):
                k = 0.88 + 0.18 * ((y + (x // 5)) % 2) + 0.08 * K.noise(x * 0.4, y * 0.4)
                for i in range(3):
                    img[y][x][i] *= k
        K.rect(img, 0, 0, w, 1, LACQUER)
        K.rect(img, 0, h - 1, w, h, LACQUER)
        return K.finish(img, face, edges=False)
    return paint


def family_spine_paint(face, w, h, seed):
    img = silk(w, h, shade(CRIMSON, 0.8), seed)
    for i in range(4):                                                   # couture d'or
        if face in ("left", "right", "front", "back"):
            cx = int((i + 0.5) * w / 4)
            K.rect(img, cx - 1, 0, cx + 1, h, familles.GOLD)
            K.put(img, cx, h // 2, familles.GOLD_DARK)
        else:
            cy = int((i + 0.5) * h / 4)
            K.rect(img, 0, cy, w, cy + 1, familles.GOLD)
    return K.finish(img, face, edges=False)


def kanagu_paint(face, w, h, seed):
    img = K.canvas(w, h, familles.GOLD)
    for y in range(h):
        for x in range(w):
            k = 1 + 0.18 * math.sin(x * 0.9 + y * 1.3) * (0.5 + K.noise(x * 0.3, y * 0.3))
            for i in range(3):
                img[y][x][i] *= k
    K.rect(img, 0, 0, w, 1, familles.GOLD_DARK)
    K.rect(img, 0, h - 1, w, h, familles.GOLD_DARK)
    if w > 3 and h > 3:
        K.put(img, w // 2, h // 2, familles.GOLD_LIGHT)
    return K.finish(img, face, edges=False)


def build_family_book(number, seed):
    """Geometrie du manuel (build_book) + coins de metal dore ; peinture du livre de tresor de la famille."""
    m = K.Model("Manuel_Famille_Luo")
    cover_bot = family_cover_paint(number, seed, "bottom", True)
    cover_top = family_cover_paint(number, seed, "top", False)
    page_w, page_h = 18 * D, 26 * D
    w1 = {"x": 0, "y": 0, "z": 0, "w": -1}

    def corners(top):
        # position d'un enfant = relative au CENTRE de la boite parente (origine + offset), comme les pages : coins du bord libre,
        # 0,25 unite plus grands que le plat (pas de faces confondues)
        return [boxn(m, "Kanagu_%d_%d" % (int(top), i), {"x": 8.95, "y": 0, "z": z}, (2.6, 4.6, 2.6), {"x": 0, "y": 0, "z": 0}, kanagu_paint)
                for i, z in enumerate((11.95, -11.95))]
    bot = boxn(m, "Book-Bot", {"x": 3, "y": 0, "z": 0}, (20, 4, 26), {"x": 7, "y": -2, "z": 0}, cover_bot)
    bot["children"] = [
        quadn(m, "Bookmark", {"x": 1, "y": 1, "z": 13}, bookmark_img(7 * D, 10 * D), {"x": 0, "y": -5, "z": 0},
              {"x": -0.608761, "y": 0, "z": 0, "w": 0.793353}, "+Z"),
        quadn(m, "Page-Bot", {"x": -7, "y": 2.1, "z": 0}, page_img(page_w, page_h, seed + 1, False), {"x": 8, "y": 0, "z": 0}, w1, "-Y"),
    ] + corners(False)
    top = boxn(m, "Book-Top", {"x": 3, "y": 0, "z": 0}, (20, 4, 26), {"x": 7, "y": 2, "z": 0}, cover_top)
    top["children"] = [quadn(m, "Page-Top", {"x": -7, "y": -2.1, "z": 0}, page_img(page_w, page_h, seed + 2, True),
                             {"x": 8, "y": 0, "z": 0}, w1, "-Y")] + corners(True)
    handle = boxn(m, "Handle", {"x": -2, "y": 0, "z": -8}, (6, 9, 27), {"x": 0, "y": -2, "z": 0}, family_spine_paint,
                  q={"x": 0.5, "y": -0.5, "z": 0.5, "w": 0.5})
    handle["children"] = [bot, top]
    origin_item = empty(m, "Origin_Item", {"x": 0, "y": -9.192388, "z": 0.707107}, {"x": -0.382683, "y": 0, "z": 0, "w": 0.92388}, [handle])
    origin_proj = empty(m, "Origin_Projectile", {"x": 2, "y": 7, "z": 14}, {"x": 0.382683, "y": 0, "z": 0, "w": 0.92388}, [origin_item])
    root = empty(m, "R-Attachment", {"x": -2, "y": 13, "z": -8}, dict(K.ID_Q), [origin_proj])
    return {"nodes": [root], "lod": "auto"}, m


# ---------------------------------------------------------------------------------------------------------------------
# Rouleau (makimono)
# ---------------------------------------------------------------------------------------------------------------------
def scroll_paint(face, w, h, seed):
    img = silk(w, h, (26, 36, 74), seed)
    for y in range(h):                                                   # rinceaux d'or
        for x in range(w):
            if (x * 3 + y * 2 + seed) % 11 == 0 and K.noise(x * 0.5, y * 0.5) > 0.45:
                K.put(img, x, y, familles.GOLD, 0.8)
    K.rect(img, 0, 0, w, 2, familles.GOLD_DARK)
    K.rect(img, 0, h - 2, w, h, familles.GOLD_DARK)
    if face == "front":                                                  # etiquette blanche a l'encre
        lx0, lx1 = int(w * 0.3), int(w * 0.7)
        K.rect(img, lx0, 4, lx1, h - 5, PAPER)
        ink_columns(img, lx0 + 1, 6, lx1 - 1, h - 7, 77, 0.85)
    return K.finish(img, face, edges=False)


def knob_paint(face, w, h, seed):
    img = K.canvas(w, h, (22, 18, 20))
    K.rect(img, 0, 0, w, 1, familles.GOLD)
    K.rect(img, 0, h - 1, w, h, familles.GOLD)
    if face in ("top", "bottom"):
        K.disc(img, w / 2.0, h / 2.0, min(w, h) * 0.3, familles.GOLD_LIGHT)
    return K.finish(img, face, edges=False)


def cord_paint(face, w, h, seed):
    img = K.canvas(w, h, CRIMSON)
    K.rope_tex(img, CRIMSON, shade(CRIMSON, 0.6), 4)
    return K.finish(img, face, edges=False)


def build_scroll():
    m = K.Model("Rouleau_Voie_Martiale")
    L = 15.0
    y0 = -4.0
    parts = [
        K.box(m, "Rouleau", (0, y0 + L / 2, 0), (3.2, L, 3.2), scroll_paint),
        K.box(m, "Embout_Bas", (0, y0 - 0.9, 0), (2.2, 1.8, 2.2), knob_paint),
        K.box(m, "Embout_Haut", (0, y0 + L + 0.9, 0), (2.2, 1.8, 2.2), knob_paint),
        K.box(m, "Cordon", (0, y0 + L * 0.7, 0), (3.6, 0.8, 3.6), cord_paint),
        K.rod(m, "Cordon_Pan_G", (1.6, y0 + L * 0.7, 1.4), (2.6, y0 + L * 0.7 - 4.0, 2.0), 0.7, cord_paint),
        K.rod(m, "Cordon_Pan_D", (1.6, y0 + L * 0.7, 1.4), (3.8, y0 + L * 0.7 - 3.2, 1.0), 0.7, cord_paint),
    ]
    root = {"id": m.nid(), "name": "R-Attachment", "position": K.vec((0, 0, 0)), "orientation": dict(K.ID_Q),
            "shape": {"type": "none", "offset": K.vec((0, 0, 0)), "stretch": {"x": 1, "y": 1, "z": 1}, "settings": {"isPiece": True},
                      "textureLayout": {}, "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": "flat"},
            "children": parts}
    return {"nodes": [root], "lod": "auto"}, m


# ---------------------------------------------------------------------------------------------------------------------
# Ecriture
# ---------------------------------------------------------------------------------------------------------------------
def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


def icon(tree, tex, size=64, yaw=-30, pitch=24, skip=("Page", "Bookmark")):
    quads = []
    for c in tree["nodes"][0]["children"]:
        quads += K.child_quads(c, (0, 0, 0), K.ID_Q, tex, skip)
    big = K.draw(quads, size * 4, yaw, pitch, margin=0.06)
    return K.downscale(big, 4)


def layout_key(tree):
    return json.dumps(tree, sort_keys=True)


# Sens du livre (2026-11-09 au 2026-11-10) : les demi-tours de l'attache dans l'animation (axes X, Y, Z) ne changeaient rien
# en jeu ; le bon sens a ete trouve avec un demi-tour autour de l'axe Y cuit dans le MODELE (manuel de test « modele Y »),
# mais le livre etait trop enfonce dans la main : il est decale de BOOK_SHIFT (repere de l'attache de la main).
# Repere calcule avec pose_kit (2026-11-10) : +Z local = le long de l'avant-bras vers la main (vue 3e personne et FPS) ;
# haut du monde = +Y local en FPS, (0, 0.7, -0.7) en 3e personne. Recul de -6 en Z = livre rentre dans le bras (vu en jeu) :
# Z ramene a -3 et Y +5 ; encore trop dans la main (4e de couverture au poignet, 2026-11-10) : Z +5.
FLIPS = {"0": {"x": 0.0, "y": 0.0, "z": 0.0, "w": 1.0}, "Y": {"x": 0.0, "y": 1.0, "z": 0.0, "w": 0.0}}
FLIP_DEFAULT = "0"
BOOK_TURN = FLIPS["Y"]
BOOK_SHIFT = (2.4, 6.1, 9.1)  # « recule encore un peu le livre » : Z +5 -> +8 (2026-11-10), -> +13 (2026-11-11) ;
# 2026-11-12 : « un peu a gauche, releve, rapproche de l'interieur » = monde (+3 gauche, +3 haut, -2 vers le corps) en vue a la
# 3e personne, converti dans le repere de la main (pose_kit sur Manuel_Lecture : X local = gauche, Y = haut-avant, Z = avant-bas)
# Manuels de test du sens (demi-tours X, Y, Z) retires le 2026-11-12 : le sens actuel est le bon (decision de l'utilisateur).
EXTRA_TURN = None
FLIP = FLIPS[FLIP_DEFAULT]
# Retour du 2026-11-11 (soir) : position bonne, mais « les pages s'animent de droite a gauche quand j'ouvre le livre, essayons de
# gauche a droite » : demi-tour du livre sur lui-meme autour de l'axe normal aux pages ouvertes. Dans le repere de Handle, la
# charniere des couvertures est l'axe Z (Cast_Hurl_Charging fait tourner Book-Top/Book-Bot autour de Z) et les couvertures
# s'etendent vers +X : le livre ouvert se lit depuis +X. Demi-tour autour de X local de Handle (le centre du livre est sur cet
# axe, il ne bouge pas) : le livre regarde toujours le lecteur, les pages tournent dans l'autre sens.
PAGE_TURN = {"x": 1.0, "y": 0.0, "z": 0.0, "w": 0.0}


def held_model(tree, extra):
    """Livre dans la main : demi-tour (BOOK_TURN, EXTRA_TURN), recul (BOOK_SHIFT) et sens des pages (PAGE_TURN)."""
    held = copy.deepcopy(tree)                                 # demi-tour et recul du livre dans la main
    proj = held["nodes"][0]["children"][0]                     # Origin_Projectile sous R-Attachment
    turn = K.qmul(extra, BOOK_TURN) if extra else BOOK_TURN
    pos = proj["position"]
    p2 = K.qrot(turn, (pos["x"], pos["y"], pos["z"]))
    proj["position"] = {k2: round(p2[i] + BOOK_SHIFT[i], 6) for i, k2 in enumerate("xyz")}
    proj["orientation"] = {k2: round(v, 6) for k2, v in K.qmul(turn, proj["orientation"]).items()}

    def handle(n):
        if n.get("name") == "Handle":
            return n
        for c in n.get("children", []):
            h = handle(c)
            if h:
                return h
        return None
    h = handle(proj)
    if h is None:
        raise SystemExit("Noeud Handle introuvable dans le livre")
    h["orientation"] = {k2: round(v, 6) for k2, v in K.qmul(h["orientation"], PAGE_TURN).items()}
    return held


def flipped(anim, q=None):
    """Ajoute le demi-tour de R-Attachment (cles constantes) a une animation."""
    q = q or FLIP
    keys = [{"time": t, "delta": dict(q), "interpolationType": "smooth"} for t in (0, anim["duration"])]
    zero = [{"time": t, "delta": {"x": 0, "y": 0, "z": 0}, "interpolationType": "smooth"} for t in (0, anim["duration"])]
    anim["nodeAnimations"]["R-Attachment"] = {"position": zero, "orientation": keys, "shapeStretch": [], "shapeVisible": [],
                                              "shapeUvOffset": []}
    return anim


def lecture_animation_fps(q=None):
    z = zipfile.ZipFile(ZIP)
    return flipped(json.loads(z.read("Common/Characters/Animations/Items/Dual_Handed/Spellbook/Attacks/Cast_Hurl_Charged/"
                                     "Cast_Hurl_Charging_FPS.blockyanim")), q)


def lecture_animation(q=None):
    """Lecture (troisieme personne) : corps de la pose vanilla Book_Read + livre de Cast_Hurl_Charging (ouvert, pages), livre
    retourne vers le lecteur (FLIP)."""
    z = zipfile.ZipFile(ZIP)
    pose = json.loads(z.read("Common/Characters/Animations/Poses/Book_Read.blockyanim"))
    cast = json.loads(z.read("Common/Characters/Animations/Items/Dual_Handed/Spellbook/Attacks/Cast_Hurl_Charged/Cast_Hurl_Charging.blockyanim"))
    nodes = {k: v for k, v in pose["nodeAnimations"].items()}
    for k in ("Book-Top", "Book-Bot", "Page-Top", "Page-Bot", "Bookmark"):
        nodes[k] = cast["nodeAnimations"][k]
    return flipped({"formatVersion": pose.get("formatVersion", 1), "duration": max(pose["duration"], cast["duration"]),
                    "holdLastKeyframe": True, "nodeAnimations": nodes}, q)


def main():
    os.makedirs(os.path.join(COMMON, FOLDER), exist_ok=True)
    previews = []
    # ----- manuels -----
    first = None
    for i, (key, name, tier, quality, color) in enumerate(BOOKS):
        tree, m = build_book(color, 31 + i * 17)
        k = layout_key(tree)
        if first is None:
            first = k
            write_json(os.path.join(COMMON, BOOK_MODEL), held_model(tree, EXTRA_TURN))
        elif k != first:
            raise SystemExit("La disposition de l'atlas differe pour " + key)
        w, h, tex = m.texture()
        item_id = "Eldanior_Manuel_Luo_" + key
        tex_rel, icon_rel = "%s/%s_Texture.png" % (FOLDER, item_id), "Icons/ItemsGenerated/%s.png" % item_id
        png.write(os.path.join(COMMON, tex_rel), w, h, tex)
        png.write(os.path.join(COMMON, icon_rel), 64, 64, icon(tree, tex))
        if isinstance(tier, tuple):                                       # technique de clan
            clan, level = tier
            house = "famille " if clan == "Luo" else "clan "                  # rune de famille du Clan Luo
            need = "membres de la " + house + clan + ", niveau " + str(level) if clan == "Luo" else "membres du clan " + clan + ", niveau " + str(level)
            kind, owner, item_level = ("Technique de la famille " if clan == "Luo" else "Technique du clan ") + clan, clan, 10 + level // 10
        else:
            need = "toutes les classes de la voie martiale" if tier == 1 else "la classe " + RANKS[tier] + " ou plus haut"
            kind, owner, item_level = "Technique de la voie martiale", "Luo", 10 + 10 * tier
        page = "ArtsMartiaux" if not isinstance(tier, tuple) else owner    # Empire Luo : techniques de classe a part
        write_json(os.path.join(SERVER, "Item", "Items", "Eldanior", "Luo", item_id + ".json"), {
            "$Comment": "Manuel secret du Clan Luo, genere par scripts/familles/gen_voie_martiale_objets.py. Maintenir le clic droit : "
                        "lecture (animation Lecture de Luo_Manuel), puis le sort Java LuoManuelApprendre enseigne Rune_" + key + ".",
            "TranslationProperties": {"Name": "Manuel secret " + ("du clan " + owner if owner != "Luo" else "de la famille Luo" if isinstance(tier, tuple) else "de la voie martiale") + " — " + name,
                                      "Description": kind + " : " + name + ". Maintenez le clic droit pour lire le "
                                                     "manuel et apprendre la technique (" + need + "). Le manuel se consume une "
                                                     "fois lu."},
            "Categories": [familles.category(page)], "Quality": quality, "ItemLevel": item_level,
            "Model": BOOK_MODEL, "Texture": tex_rel, "Icon": icon_rel, "PlayerAnimationsId": ANIM_SET,
            "IconProperties": {"Scale": 0.6, "Rotation": [0, 0, 0], "Translation": [0, -4]},
            "Interactions": {"Secondary": "Root_Luo_Manuel_Lecture"},
            "Tags": {"Type": ["Item"]}, "ItemSoundSetId": "ISS_Weapons_Books", "MaxStack": 1, "DropOnDeath": False,
            "Scale": 1.0,
        })
        if PREVIEW:
            previews.append(icon(tree, tex, 256, -30, 24))
    # ----- manuels de famille : les Mouvements (2026-11-12) -----
    for path in OBSOLETE:
        full = os.path.join(RES, path)
        if os.path.exists(full):
            os.remove(full)
    family_previews = []
    for family, moves in MOVEMENTS.items():
        first_layout = None
        for n, tier, rune_key, name in moves:
            tree, m = build_family_book(n, 211 + n * 13)
            k = layout_key(tree)
            if first_layout is None:
                first_layout = k
                write_json(os.path.join(COMMON, FAMILY_BOOK_MODEL), held_model(tree, EXTRA_TURN))
            elif k != first_layout:
                raise SystemExit("La disposition de l'atlas differe pour le Mouvement %d" % n)
            w, h, tex = m.texture()
            item_id = movement_item(family, n)
            tex_rel, icon_rel = "%s/%s_Texture.png" % (FOLDER, item_id), "Icons/ItemsGenerated/%s.png" % item_id
            png.write(os.path.join(COMMON, tex_rel), w, h, tex)
            png.write(os.path.join(COMMON, icon_rel), 64, 64, icon(tree, tex))
            title = movement_title(n, name)
            if rune_key:
                desc = ("Manuel secret de la famille " + family + " : " + title + ". Technique évolutive : elle grandit avec votre "
                        "rang martial. Maintenez le clic droit pour lire le manuel et apprendre la technique ("
                        + RANKS[tier] + " ou plus haut, membres de la famille). Le manuel se consume une fois lu.")
            else:
                desc = ("Manuel secret de la famille " + family + " : " + title + ". La technique de ce Mouvement n'est pas encore "
                        "transmise : le manuel ne se consume pas. Il demande d'être " + RANKS[tier] + " ou plus haut.")
            write_json(os.path.join(SERVER, "Item", "Items", "Eldanior", family, item_id + ".json"), {
                "$Comment": "Mouvement de la famille " + family + " (manuel de famille), genere par scripts/familles/gen_voie_martiale_objets.py. "
                            "Maintenir le clic droit : lecture, puis LuoManuelApprendre (FamilyMovements.java)"
                            + (" enseigne Rune_" + rune_key + "." if rune_key else " ; technique a venir."),
                "TranslationProperties": {"Name": "Manuel de la famille " + family + " — " + title, "Description": desc},
                "Categories": [familles.category(family)], "Quality": MOVEMENT_QUALITY[tier], "ItemLevel": 10 + 10 * tier,
                "Model": FAMILY_BOOK_MODEL, "Texture": tex_rel, "Icon": icon_rel, "PlayerAnimationsId": ANIM_SET,
                "IconProperties": {"Scale": 0.6, "Rotation": [0, 0, 0], "Translation": [0, -4]},
                "Interactions": {"Secondary": "Root_Luo_Manuel_Lecture"},
                "Tags": {"Type": ["Item"]}, "ItemSoundSetId": "ISS_Weapons_Books", "MaxStack": 1, "DropOnDeath": False,
                "Scale": 1.0,
            })
            if PREVIEW:
                family_previews.append(icon(tree, tex, 256, -30, 24))
                if n in (1, 12):
                    family_previews.append(icon(tree, tex, 256, 150, -24))         # dos et plat inferieur
    # ----- rouleau -----
    tree, m = build_scroll()
    write_json(os.path.join(COMMON, SCROLL_MODEL), tree)
    w, h, tex = m.texture()
    sid = "Eldanior_Parchemin_Voie_Martiale"
    tex_rel, icon_rel = "%s/%s_Texture.png" % (FOLDER, sid), "Icons/ItemsGenerated/%s.png" % sid
    png.write(os.path.join(COMMON, tex_rel), w, h, tex)
    png.write(os.path.join(COMMON, icon_rel), 64, 64, icon(tree, tex, 64, -24, 10, ("\0",)))
    if PREVIEW:
        previews.append(icon(tree, tex, 256, -24, 10, ("\0",)))
    write_json(os.path.join(SERVER, "Item", "Items", "Eldanior", "Luo", sid + ".json"), {
        "$Comment": "Rouleau de la Voie Martiale du Clan Luo, genere par scripts/familles/gen_voie_martiale_objets.py. Clic droit : "
                    "sort Java VoieMartialeParchemin, qui ouvre la fenetre de serment (luo.VoieMartialePage).",
        "TranslationProperties": {"Name": "Rouleau de la Voie Martiale",
                                  "Description": "Rouleau scellé du dojo du Clan Luo. Clic droit pour le dérouler : qui prête "
                                                 "serment sur la voie martiale perd tout (niveau, classe, compétences, runes, "
                                                 "famille, guilde, argent, objets en main, titres, quêtes) sauf 5 % de ses "
                                                 "caractéristiques, et recommence Apprenti en Arts Martiaux."},
        "Categories": [familles.category("ArtsMartiaux")], "Quality": "Epic", "ItemLevel": 40,
        "Model": SCROLL_MODEL, "Texture": tex_rel, "Icon": icon_rel, "PlayerAnimationsId": "Item",
        "IconProperties": {"Scale": 0.55, "Rotation": [0, 0, 0], "Translation": [0, -10]},
        "Interactions": {"Secondary": "Root_Luo_Parchemin_Voie"},
        "Tags": {"Type": ["Item"]}, "ItemSoundSetId": "ISS_Items_Paper", "MaxStack": 1, "DropOnDeath": False,
    })
    # ----- animations et interactions -----
    rel = "Characters/Animations/Luo/Manuel_Lecture.blockyanim"
    write_json(os.path.join(COMMON, rel), lecture_animation())
    fps = "Characters/Animations/Luo/Manuel_Lecture_FPS.blockyanim"
    write_json(os.path.join(COMMON, fps), lecture_animation_fps())
    # Retour du 2026-11-10 (nuit) : l'animation (60 images, 1 s) bouclait a 0,7 pendant les 2,5 s de lecture -> jouee deux fois et
    # coupee au milieu. Une seule fois, etiree sur la lecture (vitesse 1 / READ_S), figee sur sa derniere image.
    anims = {"Lecture": {"ThirdPerson": rel, "FirstPerson": fps, "Speed": round(1.0 / READ_S, 3), "Looping": False,
                         "BlendingDuration": 0.3}}
    write_json(os.path.join(SERVER, "Item", "Animations", ANIM_SET + ".json"), {
        "$Comment": "Manuels secrets du Clan Luo (gen_voie_martiale_objets.py) : Lecture = livre ouvert, pages qui tournent.",
        "Parent": "Item",
        "Animations": anims,
        "WiggleWeights": {"Pitch": 3, "PitchDeceleration": 0.1, "Roll": 0.1, "RollDeceleration": 0.1, "X": 3,
                          "XDeceleration": 0.1, "Y": 0.1, "YDeceleration": 0.1, "Z": 0.1, "ZDeceleration": 0.1},
    })
    inter = os.path.join(SERVER, "Item", "Interactions", "Luo")
    roots = os.path.join(SERVER, "Item", "RootInteractions", "Luo")
    close = {"Type": "Simple", "RunTime": 0.15, "Effects": {"ClearAnimationOnFinish": True}}
    write_json(os.path.join(inter, "Luo_Manuel_Lecture.json"), {
        "$Comment": "Manuel du Clan Luo : maintenir le clic droit = lecture (livre ouvert, pages qui tournent) ; 2,5 s = technique "
                    "apprise (LuoManuelApprendre). Relache avant : le livre se referme.",
        "Type": "Charging", "DisplayProgress": True, "HorizontalSpeedMultiplier": 0.3,
        "Effects": {"ItemAnimationId": "Lecture", "LocalSoundEventId": "SFX_Drag_Weapons_Books"},
        "Next": {"0": close,
                 str(READ_S): {"Type": "Serial", "Interactions": [{"Type": "RuneSpell", "RunTime": 0.05, "Spell": "LuoManuelApprendre"},
                                                            close]}}})
    write_json(os.path.join(roots, "Root_Luo_Manuel_Lecture.json"), {"RequireNewClick": True, "Interactions": ["Luo_Manuel_Lecture"]})
    write_json(os.path.join(inter, "Luo_Parchemin_Voie.json"), {
        "$Comment": "Rouleau de la Voie Martiale : clic droit = derouler (sort VoieMartialeParchemin -> fenetre de serment).",
        "Type": "Serial", "Interactions": [
            {"Type": "Simple", "RunTime": 0.3, "Effects": {"ItemAnimationId": "Interact", "LocalSoundEventId": "SFX_Drag_Items_Paper"}},
            {"Type": "RuneSpell", "RunTime": 0.05, "Spell": "VoieMartialeParchemin"}]})
    write_json(os.path.join(roots, "Root_Luo_Parchemin_Voie.json"), {"RequireNewClick": True, "Cooldown": {"Cooldown": 1.0},
                                                                    "Interactions": ["Luo_Parchemin_Voie"]})
    # ----- emote de meditation (Rune de la Meditation martiale) : animation du corps entier, objet en main cache -----
    write_json(os.path.join(SERVER, "Emote", "Luo_Meditation.json"), {
        "Name": "Méditation", "Animation": "Characters/Animations/Luo/Meditation.blockyanim",
        "Icon": "Icons/Emotes/Luo_Meditation.png", "IsLooping": True, "HideItemInHand": True})
    # emotes ajoutees le 2026-11-10 : meditation eveillee (Maitre Martial et plus), montee en puissance du Renforcement corporel
    write_json(os.path.join(SERVER, "Emote", "Luo_Meditation_Eveil.json"), {
        "Name": "Méditation éveillée", "Animation": "Characters/Animations/Luo/MeditationEveil.blockyanim",
        "Icon": "Icons/Emotes/Luo_Meditation.png", "IsLooping": True, "HideItemInHand": True})
    write_json(os.path.join(SERVER, "Emote", "Luo_Renforcement.json"), {
        "Name": "Renforcement corporel", "Animation": "Characters/Animations/Luo/Renforcement.blockyanim",
        "Icon": "Icons/Emotes/Luo_Meditation.png", "IsLooping": False, "HideItemInHand": False})
    os.makedirs(os.path.join(COMMON, "Icons", "Emotes"), exist_ok=True)
    w2, h2, ic = png.read(os.path.join(COMMON, "Icons", "ItemsGenerated", "Rune_MeditationMartiale.png"))
    png.write(os.path.join(COMMON, "Icons", "Emotes", "Luo_Meditation.png"), w2, h2, ic)
    if PREVIEW:
        import pose_kit as P  # noqa: E402
        os.makedirs(PREVIEW, exist_ok=True)
        cols = 4
        while len(previews) % cols:
            previews.append([[[30, 30, 36, 255] for _ in range(256)] for _ in range(256)])
        png.write(os.path.join(PREVIEW, "voie_martiale_objets.png"), 256 * cols, 256 * (len(previews) // cols), P.sheet(previews, cols))
        while len(family_previews) % cols:
            family_previews.append([[[30, 30, 36, 255] for _ in range(256)] for _ in range(256)])
        png.write(os.path.join(PREVIEW, "mouvements_famille.png"), 256 * cols, 256 * (len(family_previews) // cols),
                  P.sheet(family_previews, cols))
    print("Voie martiale : %d manuels, %d Mouvements de famille, 1 rouleau, animation Lecture."
          % (len(BOOKS), sum(len(v) for v in MOVEMENTS.values())))


if __name__ == "__main__":
    main()
