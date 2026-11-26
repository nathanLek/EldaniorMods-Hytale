"""Plaques d'identification du Clan Luo : une par rang, plus celle des marchands agrees du Comptoir de la Porte du Dragon.

  Recrue      Commun      bois de hinoki, cadre de bois sombre, mon peint a l'encre, cordon de chanvre
  Noble       Rare        laque noire, cadre et mon d'argent, cordon blanc
  Seigneur    Epique      laque cramoisie, cadre et mon d'or, cordon noir
  Doyen       Unique      laque violette, cadre et mon d'or, cordon pourpre, deux perles de jade
  Patriarche  Legendaire  or massif, champ de laque noire, mon d'or au soleil noir, cordon cramoisi, jade
  Marchand    Rare        bronze, mon de cuivre, frise de pieces trouees, cordon ocre
  Visiteur    Peu commun  laissez-passer (2026-11-16) vendu aux etrangers par les marchands Luo : bois clair, cadre et mon
                          vermillon (sceau), cordon rouge et blanc ; 2 jours (durabilite), puis il disparait (LuoPlaqueSystem)
  Clan_<X>    Rare        membres des quatre clans de l'Empire Luo : laque de la couleur du Doyen (Sama cramoisi, Hanazakura
                          violet, Kage noir, Setsuka blanc)

Un seul modele (racine R-Attachment, comme la cle du tresor) tenu par le bas de la plaque : boucle de cordon en haut, noeud
et gland (fusa) sous le poing. Une texture par rang : la disposition de l'atlas ne depend que des tailles, elle est la meme
pour toutes (verifie a l'ecriture). Face avant : mon et rang ; face arriere : vagues seigaiha et inscription.

La remise, la reprise et les effets (territoire, inclinaison des PNJ) sont dans com.eldanior.system.luo (LuoPlaques).
Aucune recette, aucun butin : le Clan Luo n'est pas ouvert au monde exterieur.

Usage : python3 scripts/familles/gen_plaques_luo.py <src/main/resources> [dossier_apercu]
"""
import json, math, os, sys

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

D = 4                                    # pixels par unite : le mon reste lisible en main
K.set_density(D, 128)
ROOT = "R-Attachment"
MODEL = "Items/Eldanior/Plaques/Plaque_Luo.blockymodel"
PREFIX = "Eldanior_Plaque_Luo_"

PW, PH, PT = 11, 16, 1.5                 # plaque, en unites
CAP_W, CAP_H = 8, 2                      # chapeau en gradin au-dessus

WOOD, WOOD_DARK = (214, 176, 122), (110, 74, 44)
INK = ((40, 30, 24), (70, 54, 42), (104, 82, 60))
SILVERS = ((128, 132, 148), (204, 208, 220), (244, 246, 252))
GOLDS = (familles.GOLD_DARK, familles.GOLD, familles.GOLD_LIGHT)
COPPERS = ((112, 58, 30), (184, 104, 58), (232, 164, 112))
BRONZES = ((86, 62, 30), (150, 112, 58), (204, 166, 104))
JADE = (64, 168, 132)
PALE_WOOD = (232, 206, 160)
VERMILLON = ((120, 22, 18), (196, 48, 36), (236, 108, 84))

# id, nom, qualite, niveau, champ, cadre (sombre, moyen, clair), mon, cordon, gemme, description
RANKS = [
    ("Recrue", "Recrue", "Common", 20, WOOD, INK, INK, (176, 150, 104), None,
     "Plaque de bois des recrues du clan, au mon peint à l'encre."),
    ("Noble", "Noble", "Rare", 100, (26, 24, 30), SILVERS, SILVERS, (232, 228, 216), None,
     "Plaque de laque noire et d'argent des Nobles du clan."),
    ("Seigneur", "Seigneur", "Epic", 250, (132, 22, 36), GOLDS, GOLDS, (24, 22, 28), None,
     "Plaque de laque cramoisie et d'or du Seigneur du clan. Tenue en main, les gens du clan s'inclinent."),
    ("Doyen", "Doyen", "Unique", 400, (58, 30, 84), GOLDS, GOLDS, (96, 44, 120), JADE,
     "Plaque de laque violette et d'or des Doyens du clan. Tenue en main, les gens du clan s'inclinent."),
    ("Patriarche", "Patriarche", "Legendary", 550, (18, 16, 22), GOLDS, GOLDS, (170, 30, 50), JADE,
     "Plaque d'or du Patriarche du Clan Luo. Tenue en main, tout le clan s'incline profondément."),
    ("Marchand", "Marchand agréé du Comptoir", "Rare", 60, BRONZES[1], BRONZES, COPPERS, (196, 140, 60), None,
     "Remise par le Patriarche ou un Doyen à un marchand étranger : elle ouvre le Comptoir de la Porte du Dragon."),
    # Laissez-passer des etrangers (2026-11-16) : achete a un marchand Luo, valable 2 jours
    ("Visiteur", "Visiteur (laissez-passer)", "Uncommon", 20, PALE_WOOD, VERMILLON, VERMILLON, (210, 60, 48), None,
     "Laissez-passer de bois au sceau vermillon, vendu aux étrangers par les marchands du Clan Luo. Le porter sur soi permet "
     "d'entrer sur le territoire Luo sans être attaqué. Valable deux jours : sa durabilité baisse avec le temps, puis il disparaît."),
    # Les quatre grands clans de l'Empire Luo (2026-11-10) : plaque des membres, couleur du kimono de leur Doyen
    ("Clan_Sama", "Clan Sama", "Rare", 80, (132, 22, 36), GOLDS, GOLDS, (24, 22, 28), None,
     "Plaque de laque cramoisie des membres du Clan Sama, pugilistes de l'Empire Luo."),
    ("Clan_Hanazakura", "Clan Hanazakura", "Rare", 80, (70, 36, 104), SILVERS, SILVERS, (232, 180, 200), None,
     "Plaque de laque violette des membres du Clan Hanazakura, épéistes des cerisiers de l'Empire Luo."),
    ("Clan_Kage", "Clan Kage", "Rare", 80, (14, 12, 18), SILVERS, SILVERS, (60, 60, 70), None,
     "Plaque de laque noire des membres du Clan Kage, assassins de l'Empire Luo."),
    ("Clan_Setsuka", "Clan Setsuka", "Rare", 80, (226, 228, 236), SILVERS, SILVERS, (64, 168, 132), None,
     "Plaque de laque blanche des membres du Clan Setsuka, maîtres de l'épée de l'Empire Luo."),
]

COMMON_DESC = (" Plaque personnelle : remise et reprise selon le rang, elle disparaît entre d'autres mains."
               " La porter sur soi permet d'entrer sur le territoire Luo sans être attaqué.")


# ---------------------------------------------------------------------------------------------------------------------
# Peintures
# ---------------------------------------------------------------------------------------------------------------------
_MON = {}


def mon(px, metal):
    """Blason Luo (dragon d'or enroule autour du soleil noir) sans ecu, ors repeints dans la gamme metal."""
    key = (px, metal)
    if key not in _MON:
        k = px * 4 / 2.1
        cv = blason.Canvas(k, -1.05, 1.05, -1.0, 1.1)
        familles.coiled_dragon(cv)
        img = blason.shrink(cv.img, px, px)
        if metal != GOLDS:
            dark, mid, light = metal
            for row in img:
                for p in row:
                    if not p[3]:
                        continue
                    r, g, b = p[:3]
                    if r > 150 and g < 90:                       # oeil
                        continue
                    lum = 0.3 * r + 0.59 * g + 0.11 * b
                    if lum < 50:                                 # trait et soleil noir
                        continue
                    t = min(1.0, max(0.0, (lum - 90) / 150.0))
                    c = K.mix(dark, mid, t * 2) if t < 0.5 else K.mix(mid, light, (t - 0.5) * 2)
                    p[0], p[1], p[2] = c
        _MON[key] = img
    return _MON[key]


def stamp(img, src, cx, cy):
    h, w = len(src), len(src[0])
    for y in range(h):
        for x in range(w):
            p = src[y][x]
            if p[3]:
                K.put(img, cx - w / 2.0 + x, cy - h / 2.0 + y, p[:3], p[3] / 255.0)


def ground(w, h, rank, seed):
    """Champ de la plaque : bois veine, bronze martele, ou laque aux reflets en biais."""
    rid, field, frame = rank[0], rank[4], rank[5]
    img = K.canvas(w, h, field)
    for y in range(h):
        for x in range(w):
            p = img[y][x]
            if rid in ("Recrue", "Visiteur"):
                k = 1 + 0.12 * math.sin(x * 0.9 + 3 * K.fbm(x * 0.08 + seed, y * 0.02)) + 0.05 * (K.noise(x * 0.6, y * 0.6) - 0.5)
            elif rid == "Marchand":
                k = 1 + 0.16 * (K.noise(x * 0.45 + seed, y * 0.45) - 0.5) + 0.06 * math.sin(x * 1.3 + y * 0.7)
            else:
                s = max(0.0, math.sin((x + y * 0.6) * 4.0 / max(w, 8))) ** 6
                c = K.mix(field, tuple(min(255, v * 2.2 + 50) for v in field), s * 0.4)
                p[0], p[1], p[2] = c
                k = 1.0
            for i in range(3):
                p[i] *= k
    return img


def frame_edge(img, rank, inset=0, width=2):
    """Cadre de metal (ou de bois sombre) : trait clair dehors, sombre dedans."""
    dark, mid, light = rank[5]
    h, w = len(img), len(img[0])
    for y in range(h):
        for x in range(w):
            d = min(x, y, w - 1 - x, h - 1 - y) - inset
            if 0 <= d < width:
                K.put(img, x, y, light if d == 0 else mid)
            elif d == width:
                K.put(img, x, y, dark)


def rank_marks(img, rank, y):
    """Marques de rang sous le mon : 1 (Recrue) a 5 (Patriarche) ; pieces trouees pour le marchand."""
    rid = rank[0]
    w = len(img[0])
    dark, mid, light = rank[6]
    if rid == "Visiteur":                                         # sceau carre vermillon
        K.rect(img, w / 2.0 - 3, y - 3, w / 2.0 + 3, y + 3, dark)
        K.rect(img, w / 2.0 - 2, y - 2, w / 2.0 + 2, y + 2, mid)
        K.put(img, w / 2.0 - 1, y - 1, light)
        return
    if rid == "Marchand":
        for cx in (w / 2.0 - 9, w / 2.0, w / 2.0 + 9):
            K.disc(img, cx, y, 3.4, dark)
            K.disc(img, cx, y, 2.8, mid)
            K.disc(img, cx - 0.6, y - 0.6, 1.2, light)
            K.rect(img, cx - 1, y - 1, cx + 1, y + 1, dark)
        return
    n = 2 if rid.startswith("Clan_") else ["Recrue", "Noble", "Seigneur", "Doyen", "Patriarche"].index(rid) + 1
    step = 6
    x0 = w / 2.0 - (n - 1) * step / 2.0
    for i in range(n):
        cx = x0 + i * step
        K.disc(img, cx, y, 2.1, dark)
        K.disc(img, cx, y, 1.6, mid)
        K.put(img, cx - 1, y - 1, light)


def front(rank, w, h, seed):
    img = ground(w, h, rank, seed)
    frame_edge(img, rank)
    if rank[0] == "Patriarche":                                  # champ de laque noire serti dans l'or
        dark, mid, light = rank[5]
        for y in range(h):
            for x in range(w):
                d = min(x, y, w - 1 - x, h - 1 - y)
                if d < 5:
                    c = light if d == 0 else mid if d < 4 else dark
                    p = img[y][x]
                    k = 1 + 0.12 * math.sin((x + y) * 0.8)
                    p[0], p[1], p[2] = [c[i] * k for i in range(3)]
    r = w * 0.41
    cx, cy = w / 2.0, h * 0.42
    dark, mid, light = rank[6]
    if rank[0] in ("Recrue", "Visiteur"):
        K.disc(img, cx, cy, r + 1.2, dark)                       # mon peint : cercle d'encre
        K.disc(img, cx, cy, r, rank[4])
    else:
        K.disc(img, cx, cy, r + 1.5, light)
        K.disc(img, cx, cy, r + 0.6, mid)
        K.disc(img, cx, cy, r - 0.4, (18, 16, 22) if rank[0] != "Marchand" else BRONZES[0])
    stamp(img, mon(int(2 * r + 1), rank[6]), cx, cy)
    rank_marks(img, rank, h * 0.82)
    return K.finish(img, "front", edges=False)


def back(rank, w, h, seed):
    """Vagues seigaiha gravees et colonne d'inscription."""
    img = ground(w, h, rank, seed + 7)
    dark, mid, light = rank[6]
    R = 5.0
    for y in range(h):
        for x in range(w):
            row = int(y // (R / 2))
            ox = (R if row % 2 else 0)
            gx = ((x + ox) % (2 * R)) - R
            gy = (y % (R / 2)) - R / 2
            d = math.hypot(gx, gy + R / 2)
            if d < R and abs((d % 1.6) - 0.8) < 0.3:
                K.put(img, x, y, dark, 0.45)
    K.rect(img, w / 2.0 - 5, 5, w / 2.0 + 5, h - 5, rank[4])
    K.rect(img, w / 2.0 - 5, 5, w / 2.0 - 4, h - 5, mid)
    K.rect(img, w / 2.0 + 4, 5, w / 2.0 + 5, h - 5, mid)
    y = 9
    k = 0
    while y < h - 9:                                             # traits de pinceau, faux caracteres
        n = 2 + (k * 7 + seed) % 3
        for i in range(n):
            yy = y + i * 2
            x0 = w / 2.0 - 2.5 + ((k + i) % 2)
            K.stroke(img, [(x0, yy), (x0 + 3 + ((k * 3 + i) % 2), yy + ((i + k) % 2))], mid, 0.5)
        K.stroke(img, [(w / 2.0, y - 1), (w / 2.0 - 0.5, y + 2 * n)], mid, 0.45)
        y += 2 * n + 4
        k += 1
    frame_edge(img, rank)
    return K.finish(img, "back", edges=False)


def edge_paint(rank):
    def paint(face, w, h, seed):
        if face == "front":
            return front(rank, w, h, seed)
        if face == "back":
            return back(rank, w, h, seed)
        dark, mid, light = rank[5]
        img = K.canvas(w, h, mid)
        for y in range(h):
            for x in range(w):
                k = 1 + 0.18 * math.sin(x * 0.9 + y * 0.5)
                for i in range(3):
                    img[y][x][i] *= k
        return K.finish(img, face)
    return paint


def cap_paint(rank):
    def paint(face, w, h, seed):
        dark, mid, light = rank[5]
        img = K.canvas(w, h, mid)
        for y in range(h):
            for x in range(w):
                k = 1 + 0.15 * math.sin(x * 0.8 + y * 1.3)
                for i in range(3):
                    img[y][x][i] *= k
        if face in ("front", "back"):
            K.rect(img, 0, h - 1, w, h, dark)
            K.rect(img, 0, 0, w, 1, light)
            if rank[8]:
                K.disc(img, w / 2.0, h / 2.0, min(h, w) * 0.42, rank[8])
                K.put(img, w / 2.0 - 1, h / 2.0 - 1, (200, 250, 226))
        return K.finish(img, face, edges=False)
    return paint


def cord_paint(rank):
    def paint(face, w, h, seed):
        c = rank[7]
        img = K.canvas(w, h, c)
        K.rope_tex(img, c, tuple(v * 0.6 for v in c), 4)
        return K.finish(img, face, edges=False)
    return paint


def tassel_paint(rank):
    def paint(face, w, h, seed):
        c = rank[7]
        img = K.canvas(w, h, c)
        for y in range(h):
            for x in range(w):
                k = 0.78 + 0.32 * ((x * 3 + seed) % 2) + 0.1 * K.noise(x, y * 0.3)
                for i in range(3):
                    img[y][x][i] *= k
        if h > 4:
            K.rect(img, 0, 1, w, 3, tuple(v * 0.55 for v in c))           # ligature du gland
        return K.finish(img, face, edges=False)
    return paint


def gem_paint(rank):
    def paint(face, w, h, seed):
        g = rank[8] or rank[5][2]
        img = K.canvas(w, h, g)
        K.put(img, 0, 0, (230, 255, 240))
        return K.finish(img, face, light=1.15, edges=False)
    return paint


# ---------------------------------------------------------------------------------------------------------------------
# Modele
# ---------------------------------------------------------------------------------------------------------------------
def build(rank):
    m = K.Model("Plaque_" + rank[0])
    cy = PH / 2.0                                                    # le poing tient le bas de la plaque
    top = PH + CAP_H
    parts = [
        K.box(m, "Plaque", (0, cy, 0), (PW, PH, PT), edge_paint(rank)),
        K.box(m, "Chapeau", (0, PH + CAP_H / 2.0, 0), (CAP_W, CAP_H, PT + 0.5), cap_paint(rank)),
        K.box(m, "Oeillet", (0, top + 1.0, 0), (2.5, 2, 2), cap_paint(rank)),
        K.rod(m, "Cordon_D", (-0.8, top + 1.6, 0), (-2.4, top + 5.5, 0), 0.75, cord_paint(rank)),
        K.rod(m, "Cordon_G", (0.8, top + 1.6, 0), (2.4, top + 5.5, 0), 0.75, cord_paint(rank)),
        K.rod(m, "Cordon_H", (-2.6, top + 5.6, 0), (2.6, top + 5.6, 0), 0.75, cord_paint(rank)),
        K.box(m, "Noeud", (0, -1.0, 0), (2.2, 1.6, 2.2), cord_paint(rank)),
        K.box(m, "Gland", (0, -4.6, 0), (2.6, 5.6, 2.6), tassel_paint(rank)),
        K.box(m, "Perle_D", (-1.6, -1.6, 0), (1.2, 1.2, 1.2), gem_paint(rank)),
        K.box(m, "Perle_G", (1.6, -1.6, 0), (1.2, 1.2, 1.2), gem_paint(rank)),
    ]
    root = {"id": m.nid(), "name": ROOT, "position": K.vec((0, 0, 0)), "orientation": dict(K.ID_Q),
            "shape": {"type": "none", "offset": K.vec((0, 0, 0)), "stretch": {"x": 1, "y": 1, "z": 1}, "settings": {"isPiece": True},
                      "textureLayout": {}, "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": "flat"},
            "children": parts}
    return {"nodes": [root], "lod": "auto"}, m


def icon(tree, tex, size=64, yaw=-24, pitch=8):
    quads = []
    for c in tree["nodes"][0]["children"]:
        quads += K.child_quads(c, (0, 0, 0), K.ID_Q, tex, ("\0",))
    big = K.draw(quads, size * 4, yaw, pitch, margin=0.05)
    return K.downscale(big, 4)


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


def strip_layout(node):
    """Arbre sans les sommets d'atlas : pour verifier que tous les rangs partagent le meme modele."""
    return json.dumps(node, sort_keys=True)


def main():
    first = None
    previews = []
    for rank in RANKS:
        rid, name, quality, level = rank[:4]
        tree, m = build(rank)
        if first is None:
            first = strip_layout(tree)
            write_json(os.path.join(COMMON, MODEL), tree)
        elif strip_layout(tree) != first:
            raise SystemExit("La disposition de l'atlas differe pour " + rid)
        w, h, tex = m.texture()
        item_id = PREFIX + rid
        tex_rel, icon_rel = "Items/Eldanior/Plaques/%s_Texture.png" % item_id, "Icons/ItemsGenerated/%s.png" % item_id
        png.write(os.path.join(COMMON, tex_rel), w, h, tex)
        png.write(os.path.join(COMMON, icon_rel), 64, 64, icon(tree, tex))
        light = rank[8] or rank[5][1]
        visitor = rid == "Visiteur"
        data = {
            "$Comment": "Plaque d'identification du Clan Luo, generee par scripts/familles/gen_plaques_luo.py. Remise et reprise par LuoPlaqueSystem.",
            "TranslationProperties": {"Name": "Plaque d'identification — " + name, "Description": rank[9] + ("" if visitor else COMMON_DESC)},
            "Categories": [familles.category(rid[5:] if rid.startswith("Clan_") else "Civils" if rid in ("Marchand", "Visiteur") else "Luo")], "ItemLevel": level, "Quality": quality,
            "Model": MODEL, "Texture": tex_rel, "Icon": icon_rel,
            "PlayerAnimationsId": "Item",
            "IconProperties": {"Scale": 0.55, "Rotation": [0, 0, 0], "Translation": [0, -12]},
            "Tags": {"Type": ["Ingredient"]}, "ItemSoundSetId": "ISS_Items_Ingots", "MaxStack": 1, "DropOnDeath": False,
            "Light": {"Color": "#%x%x%x" % tuple(int(c) >> 4 for c in light)},
        }
        if visitor:                                               # 2 jours = 2 880 minutes (LuoPlaques.VISITOR_MINUTES)
            data["MaxDurability"] = 2880
            data["DurabilityLossOnHit"] = 0
        write_json(os.path.join(SERVER, "Item", "Items", "Eldanior", "Plaques", item_id + ".json"), data)
        if PREVIEW:
            previews.append(icon(tree, tex, 256, -28, 10))
            previews.append(icon(tree, tex, 256, 180 - 20, 10))
    if PREVIEW:
        import pose_kit as P  # noqa: E402
        os.makedirs(PREVIEW, exist_ok=True)
        png.write(os.path.join(PREVIEW, "plaques_luo.png"), 256 * 4, 256 * 3, P.sheet(previews, 4))
    print("Plaques Luo : %d objets ecrits." % len(RANKS))


if __name__ == "__main__":
    main()
