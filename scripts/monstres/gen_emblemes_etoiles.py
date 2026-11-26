"""Emblemes flottant au-dessus des monstres speciaux (boards.MonsterContracts / boards.DungeonBosses, 2026-11-16).

- Monstres etoiles : 1, 2 ou 3 etoiles ROUGES a 5 branches.
- Boss de donjon : une petite tete de mort.
- Boss Mondial (2026-11-19) : une couronne d'or (Couronne_x1/x2/x3/x5 : le Roi de la Mort est agrandi x5).
  2026-11-20 : 1 a 3 couronnes tirees au hasard (comme les etoiles), une version par Boss Mondial (CROWN_BOSSES : echelle,
  hauteur, avance de la tete) : Couronnes_<N>_<Boss>.blockymodel. Roi de la Mort : couronne remontee d'un bloc (« un bloc
  trop bas »).

Retours de l'utilisateur (2026-11-16) : pas d'images croisees, etoiles rouges, crane en 2D et moins gros. Chaque embleme est donc
une image de pixel-art EXTRUDEE : une boite par suite de pixels pleins d'une ligne, epaisse de quelques pixels, face avant et
arriere peintes avec l'image, cotes d'une couleur sombre. Vue de face c'est l'image ; de profil, une tranche epaisse (jamais
invisible) ; aucune transparence a gerer.

Le nom au-dessus d'une entite (Nameplate) n'accepte que du texte : l'embleme est un petit modele ajoute au monstre comme
attachement (ModelAttachment). Sa racine s'appelle « Head » : elle se fond dans l'os Head du monstre, l'embleme suit la tete.
Il flotte a HEIGHT_BLOCKS blocs au-dessus du pivot de la tete (demande : « 2/3 blocs au-dessus de la tete »).

L'attachement prend l'echelle du monstre (x2 pour les monstres de contrat et les boss, x1 pour les dragons) : chaque embleme
existe en deux versions, _x1 et _x2, dont les dimensions sont divisees par l'echelle (meme taille et meme hauteur a l'ecran).
Le serveur choisit la version la plus proche de l'echelle du modele (MonsterContracts.dress).

Sortie : Common/NPC/Eldanior/Emblemes/Etoiles_<N>_x<S>.blockymodel + Etoiles_Texture.png,
         Crane_x<S>.blockymodel + Crane_Texture.png ; apercu optionnel.
Usage : python3 scripts/monstres/gen_emblemes_etoiles.py <src/main/resources> [dossier_apercu]
"""
import json, math, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
for folder in ("armures", "rune_aura_icons"):
    sys.path.insert(0, os.path.join(HERE, "..", folder))
import png  # noqa: E402
import armor_kit as K  # noqa: E402

RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "..", "..", "src", "main", "resources")
PREVIEW = sys.argv[2] if len(sys.argv) > 2 else None
OUT = os.path.join(RES, "Common", "NPC", "Eldanior", "Emblemes")

UNITS_PER_BLOCK = 64          # entites : 64 unites par bloc
HEIGHT_BLOCKS = 2.5           # au-dessus du pivot de la tete (~2 blocs au-dessus du sommet du crane)
SCALES = (1, 2)

STAR_PX, STAR_BLOCKS, STAR_SPACING, STAR_DEPTH = 21, 0.6, 0.66, 3     # image 21 px, 0,6 bloc de large, 3 px d'epaisseur
SKULL_PX, SKULL_BLOCKS, SKULL_DEPTH = 16, 0.42, 2                     # crane 16 px, 0,42 bloc (plus petit)

CROWN_PX, CROWN_BLOCKS, CROWN_DEPTH, CROWN_HEIGHT = 18, 0.7, 4, 1.6    # couronne : plus large, plus basse (posee sur la tete)
CROWN_SPACING = 0.8
# Boss Mondial -> (echelle du modele, hauteur au-dessus du pivot de la tete en blocs a l'ecran, avance en unites du modele : le
# centre de la tete de l'ours est 15 unites devant le pivot de son os Head). Meme cle que WorldBosses.Kind.crown.
# Pivots des os Head mesures sur les modeles du jeu (gen_boss_mondiaux.py) : dragon (Dragon_Frost) tete basse et avancee,
# Rex_Cave tete haute, Golem_Firesteel tete fondue dans le torse (sommet 40 unites au-dessus du pivot).
# Ryu (Kokuō-Ryū, gen_ryu_celeste.py) : couronnes au-dessus de ses cornes d'or (pointes a ~122 unites au-dessus du pivot Head, tete x1,18).
CROWN_BOSSES = {"Roi": (5, 2.6, 0), "Ours": (3, 2.6, 15), "Dragon": (1.5, 1.8, 15), "Rex": (2, 2.4, 10), "Golem": (3, 2.8, 0),
                "Ryu": (6, 12.2, 0)}
GOLD, GOLD_LIGHT, GOLD_DARK, GOLD_EDGE, RUBY, SAPPHIRE = (232, 182, 40), (255, 226, 120), (176, 120, 18), (96, 58, 6), (214, 30, 46), (60, 110, 230)
RED, RED_LIGHT, RED_DARK, RED_EDGE, RED_SHINE = (214, 32, 36), (255, 104, 92), (150, 14, 20), (70, 4, 10), (255, 200, 190)
BONE, BONE_LIGHT, BONE_SHADE, BONE_EDGE, HOLE, EYE = (230, 222, 200), (250, 246, 232), (190, 178, 152), (92, 80, 64), (24, 14, 16), (210, 30, 30)


# ===================================================================== images (pixel-art, opaque ou vide)

def star_img(n=STAR_PX, ss=5):
    """Etoile rouge a 5 branches : liseré sombre, degrade clair en haut a gauche, reflet."""
    R, r = 0.5, 0.5 * 0.42
    pts = [(0.5 + (R if i % 2 == 0 else r) * math.cos(math.radians(90 + 36 * i)),
            0.53 - (R if i % 2 == 0 else r) * math.sin(math.radians(90 + 36 * i))) for i in range(10)]

    def inside(x, y):
        c, j = False, len(pts) - 1
        for i in range(len(pts)):
            (xi, yi), (xj, yj) = pts[i], pts[j]
            if (yi > y) != (yj > y) and x < (xj - xi) * (y - yi) / (yj - yi) + xi:
                c = not c
            j = i
        return c

    mask = [[sum(inside((px + (a + 0.5) / ss) / n, (py + (b + 0.5) / ss) / n) for a in range(ss) for b in range(ss)) * 2 >= ss * ss
             for px in range(n)] for py in range(n)]
    img = [[[0, 0, 0, 0] for _ in range(n)] for _ in range(n)]
    for y in range(n):
        for x in range(n):
            if not mask[y][x]:
                continue
            edge = any(not (0 <= x + dx < n and 0 <= y + dy < n and mask[y + dy][x + dx]) for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
            t = max(0.0, min(1.0, (x * 0.5 + y) / (n * 1.1)))
            c = RED_EDGE if edge else K.mix(RED_LIGHT, RED, t * 1.6) if t < 0.62 else K.mix(RED, RED_DARK, (t - 0.62) * 2.2)
            if not edge and (x - n * 0.42) ** 2 + (y - n * 0.42) ** 2 < 2.2:
                c = RED_SHINE
            img[y][x] = [int(c[0]), int(c[1]), int(c[2]), 255]
    return img


SKULL = [  # 16 x 14, symetrique : . vide, # contour, o os, l os clair, s ombre, e orbite, r lueur rouge, n nez, t dent
    "....########....",
    "..##llllllll##..",
    ".#llllllllllll#.",
    "#llllllllllllll#",
    "#lllllllllllloo#",
    "#oeeeeooooeeeeo#",
    "#oerreooooerreo#",
    "#oeeeeooooeeeeo#",
    "#sooooonnooooos#",
    ".#soooonnoooos#.",
    "..#soooooooos#..",
    "..#tetteettet#..",
    "..#ssssssssss#..",
    "...##########...",
]
SKULL_COLORS = {"#": BONE_EDGE, "o": BONE, "l": BONE_LIGHT, "s": BONE_SHADE, "e": HOLE, "r": EYE, "n": HOLE, "t": BONE_LIGHT}


CROWN = [  # 18 x 12 : # contour, g or, l or clair, d or sombre, r rubis, s saphir
    "#.......##.......#",
    "##......##......##",
    "#l#....#ll#....#l#",
    "#lg#..#lggl#..#gl#",
    "#lgg##lggggl##ggl#",
    "#lggglggrrgglggggd",
    "#lgggggrrrrgggggd#",
    "#llllllllllllllld#",
    "#gsggggrggrggggsd#",
    "#ddddddddddddddd##",
    "##################",
    "..................",
]
CROWN_COLORS = {"#": GOLD_EDGE, "g": GOLD, "l": GOLD_LIGHT, "d": GOLD_DARK, "r": RUBY, "s": SAPPHIRE}


def crown_img():
    assert all(len(r) == CROWN_PX for r in CROWN), "lignes de la couronne de 18 caracteres"
    img = [[[0, 0, 0, 0] for _ in range(CROWN_PX)] for _ in range(len(CROWN))]
    for y, row in enumerate(CROWN):
        for x, ch in enumerate(row):
            if ch in CROWN_COLORS:
                c = CROWN_COLORS[ch]
                img[y][x] = [c[0], c[1], c[2], 255]
    return img


def skull_img():
    assert all(len(r) == SKULL_PX for r in SKULL), "lignes du crane de 16 caracteres"
    img = [[[0, 0, 0, 0] for _ in range(SKULL_PX)] for _ in range(len(SKULL))]
    for y, row in enumerate(SKULL):
        for x, ch in enumerate(row):
            if ch in SKULL_COLORS:
                c = SKULL_COLORS[ch]
                img[y][x] = [c[0], c[1], c[2], 255]
    return img


# ===================================================================== extrusion

def box_node(m, name, pos, px, upx, layout):
    return {"id": m.nid(), "name": name, "position": K.vec(pos), "orientation": dict(K.ID_Q),
            "shape": {"type": "box", "offset": K.vec((0, 0, 0)), "stretch": {"x": upx, "y": upx, "z": upx},
                      "settings": {"size": {"x": px[0], "y": px[1], "z": px[2]}},
                      "textureLayout": {f: K.uv(p) for f, p in layout.items()}, "unwrapMode": "custom", "visible": True,
                      "doubleSided": False, "shadingMode": "fullbright"},
            "children": []}


def extrude(m, name, img, center, upx, depth, side):
    """Une boite par suite de pixels pleins de chaque ligne ; faces avant/arriere = la ligne de l'image, le reste = side."""
    h, w = len(img), len(img[0])
    flat = m.cache                                      # tranches communes a toutes les copies : une seule texture

    def plain(pw, ph):
        if (pw, ph) not in flat:
            flat[(pw, ph)] = m.put([[[side[0], side[1], side[2], 255] for _ in range(pw)] for _ in range(ph)])
        return flat[(pw, ph)]

    def row(y, x0, x1):
        if ("row", y, x0) not in flat:
            flat[("row", y, x0)] = m.put([img[y][x0:x1]])
        return flat[("row", y, x0)]

    out = []
    for y in range(h):
        x = 0
        while x < w:
            if img[y][x][3] == 0:
                x += 1
                continue
            x0 = x
            while x < w and img[y][x][3]:
                x += 1
            run = x - x0
            face = row(y, x0, x)
            cx = center[0] + (x0 + run / 2.0 - w / 2.0) * upx
            cy = center[1] + (h / 2.0 - y - 0.5) * upx
            out.append(box_node(m, "%s_L%d_%d" % (name, y, x0), (cx, cy, center[2]), (run, 1, depth), upx,
                                {"front": face, "back": face, "left": plain(depth, 1), "right": plain(depth, 1),
                                 "top": plain(run, depth), "bottom": plain(run, depth)}))
    return out


def root(m, children):
    return {"nodes": [{"id": m.nid(), "name": "Head", "position": K.vec((0, 0, 0)), "orientation": dict(K.ID_Q),
                       "shape": {"type": "none", "offset": K.vec((0, 0, 0)), "stretch": {"x": 1, "y": 1, "z": 1},
                                 "settings": {"isPiece": True}, "textureLayout": {}, "unwrapMode": "custom", "visible": True,
                                 "doubleSided": False, "shadingMode": "flat"},
                       "children": children}], "lod": "auto"}


def build_stars(n, scale, star):
    m = K.Model("Etoiles")
    f = 1.0 / scale
    upx = STAR_BLOCKS * UNITS_PER_BLOCK / STAR_PX * f
    y = HEIGHT_BLOCKS * UNITS_PER_BLOCK * f
    kids = []
    for i in range(n):
        x = (i - (n - 1) / 2.0) * STAR_SPACING * UNITS_PER_BLOCK * f
        kids += extrude(m, "Etoile%d" % (i + 1), star, (x, y, 0), upx, STAR_DEPTH, RED_EDGE)
    return root(m, kids), m


def build_skull(scale, skull):
    m = K.Model("Crane")
    f = 1.0 / scale
    upx = SKULL_BLOCKS * UNITS_PER_BLOCK / SKULL_PX * f
    return root(m, extrude(m, "Crane", skull, (0, HEIGHT_BLOCKS * UNITS_PER_BLOCK * f, 0), upx, SKULL_DEPTH, BONE_EDGE)), m


def build_crowns(n, boss, crown):
    """n couronnes cote a cote au-dessus de la tete du Boss Mondial boss (une image extrudee chacune, pas d'images croisees)."""
    scale, height, ahead = CROWN_BOSSES[boss]
    m = K.Model("Couronnes")
    f = 1.0 / scale
    upx = CROWN_BLOCKS * UNITS_PER_BLOCK / CROWN_PX * f
    y = height * UNITS_PER_BLOCK * f
    kids = []
    for i in range(n):
        x = (i - (n - 1) / 2.0) * CROWN_SPACING * UNITS_PER_BLOCK * f
        kids += extrude(m, "Couronne%d" % (i + 1), crown, (x, y, ahead), upx, CROWN_DEPTH, GOLD_EDGE)
    return root(m, kids), m


# ===================================================================== sortie

def write_model(name, tree):
    with open(os.path.join(OUT, name + ".blockymodel"), "w", encoding="utf-8") as fh:
        json.dump(tree, fh, indent=1)
        fh.write("\n")


def preview(tree, tex, yaw, size=300):
    quads = []
    for c in tree["nodes"][0]["children"]:
        quads += K.child_quads(c, (0, 0, 0), K.ID_Q, tex, ("\0",))
    return K.downscale(K.draw(quads, size * 2, yaw, 8, margin=0.08), 2)


def main():
    K.set_density(1, 256)
    os.makedirs(OUT, exist_ok=True)
    for old in ("Etoiles_1", "Etoiles_2", "Etoiles_3", "Couronne_x1", "Couronne_x2", "Couronne_x3", "Couronne_x5"):   # anciens emblemes
        p = os.path.join(OUT, old + ".blockymodel")
        if os.path.exists(p):
            os.remove(p)
    star, skull, crown = star_img(), skull_img(), crown_img()
    shots = []
    for kind, builds in (("Etoiles", [((n, s), lambda n=n, s=s: build_stars(n, s, star)) for n in (1, 2, 3) for s in SCALES]),
                         ("Crane", [((0, s), lambda s=s: build_skull(s, skull)) for s in SCALES]),
                         ("Couronne", [((n, b), lambda n=n, b=b: build_crowns(n, b, crown)) for b in CROWN_BOSSES for n in (1, 2, 3)])):
        tex_ref = None
        for (n, s), fn in builds:
            tree, m = fn()
            w, h, tex = m.texture()
            if tex_ref is None:
                tex_ref = tex
                png.write(os.path.join(OUT, kind + "_Texture.png"), w, h, tex)
            elif tex != tex_ref:
                raise SystemExit("texture differente : " + kind)
            write_model(("Etoiles_%d_x%d" % (n, s)) if kind == "Etoiles" else ("Couronnes_%d_%s" % (n, s)) if kind == "Couronne"
                        else "%s_x%d" % (kind, s), tree)
            if PREVIEW and s in (1, "Roi") and (n in (0, 3)):
                shots += [preview(tree, tex, 0), preview(tree, tex, -55)]
    if PREVIEW:
        os.makedirs(PREVIEW, exist_ok=True)
        W, H = sum(len(p[0]) for p in shots), max(len(p) for p in shots)
        sheet = [[[28, 30, 40, 255] for _ in range(W)] for _ in range(H)]
        ox = 0
        for p in shots:
            for y, row in enumerate(p):
                for x, c in enumerate(row):
                    if c[3]:
                        sheet[y][ox + x] = [int(c[0]), int(c[1]), int(c[2]), 255]
            ox += len(p[0])
        png.write(os.path.join(PREVIEW, "emblemes.png"), W, H, sheet)
    print("Emblemes ecrits dans", OUT)


if __name__ == "__main__":
    main()
