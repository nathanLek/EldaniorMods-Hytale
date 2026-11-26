"""Ryukiri, Tranche-Dragon des Runkandel (artefact divin, Artefact_Sword_Ryukiri) : modele, texture, icone, trainee, item.

Epee de la famille Runkandel (tueurs de dragons), dans l'esprit d'un katana stylise : lame courbe d'acier noir profond,
fil d'or le long de l'arete (shinogi), ligne de trempe (hamon) rouge, une patte de dragon en or qui serre la base de la
lame (pas de griffes sur le dos : refusees par l'utilisateur le 2026-11-03), tsuba noire cerclee d'or,
poignee tressee noire a losanges rouges, pommeau d'or a gemme rouge.

Repere du noeud Handle (le meme que le katana et les epees vanilla, donc les animations "Sword" le tiennent pareil) :
x le long de la lame (vers la pointe), +y le dos de la lame (mune), -y le tranchant, z l'epaisseur. Sur la texture du
katana vanilla, le haut des faces (+y) porte l'arete et la pointe remonte vers lui : +y est bien le dos.
Les enfants sont places par rapport au CENTRE de la forme du parent (voir hytale-model-units). La courbure (sori) vient
de segments emboites, chacun tourne de quelques degres autour de z vers le dos ; leur epaisseur decroit pour que leurs
faces ne soient jamais coplanaires la ou ils se chevauchent. La pointe (kissaki) est un quad detoure, comme le vanilla.

Usage : python3 scripts/artefacts/gen_ryukiri.py <src/main/resources> [dossier_apercu]
"""
import copy, json, math, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "armures"))
sys.path.insert(0, os.path.join(HERE, "..", "rune_aura_icons"))
import armor_kit as K  # noqa: E402
import familles  # noqa: E402
import png  # noqa: E402
sys.path.insert(0, HERE)
from coplanar import fix_coplanar  # noqa: E402

RES = sys.argv[1]
PREVIEW = sys.argv[2] if len(sys.argv) > 2 else None
NAME = "Artefact_Sword_Ryukiri"
MODEL_REL = "Items/Weapons/artefacts"
K.set_density(4, 512)                                            # 4 px par unite : filets d'or et de rouge d'un pixel

# ---------------------------------------------------------------------------------------------------------------------
# Couleurs
# ---------------------------------------------------------------------------------------------------------------------
BLACK = (14, 12, 18)
BLACK_SHEEN = (58, 54, 70)
EDGE = (104, 100, 118)                                              # tranchant : acier sombre, juste assez clair pour se lire
EDGE_LIGHT = (156, 152, 170)
RED = (190, 24, 34)
RED_DARK = (92, 10, 18)
RED_GLOW = (255, 76, 52)
GOLD, GOLD_LIGHT, GOLD_DARK = K.GOLD, K.GOLD_LIGHT, K.GOLD_DARK


def mixc(a, b, t):
    return K.mix(a, b, t)


def set_px(img, x, y, c, a=1.0):
    K.put(img, x, y, c, a)


# ---------------------------------------------------------------------------------------------------------------------
# Peintures (face, largeur px, hauteur px, graine) -> image
# ---------------------------------------------------------------------------------------------------------------------
BLADE_TOTAL = 77.0                                               # longueur de lame (unites), pour des motifs continus


def blade_pixel(gx, v, width_units):
    """Couleur de la lame en un point : gx = abscisse sur toute la lame (unites), v = 0 au dos, 1 au tranchant."""
    px_unit = 1.0 / (width_units * K.D)                          # un pixel, en fraction de largeur
    sheen = max(0.0, math.sin(gx * 0.11 + v * 2.2)) ** 3 * 0.55 + 0.12 * K.fbm(gx * 0.35, v * 4)
    c = mixc(BLACK, BLACK_SHEEN, sheen)
    if v < 0.26:                                                 # dos (shinogi-ji) un peu plus mat
        c = mixc(c, BLACK, 0.35)
    if abs(v - 0.27) < px_unit * 0.6:                            # fil d'or de l'arete
        c = GOLD_LIGHT if math.sin(gx * 0.9) > 0.6 else GOLD
    hamon = 0.66 + 0.055 * math.sin(gx * 0.55) + 0.025 * math.sin(gx * 1.7 + 1.3)
    if v > hamon:                                                # zone trempee : acier sombre vers le tranchant
        t = (v - hamon) / max(1e-3, 1 - hamon)
        c = mixc(EDGE, EDGE_LIGHT, t * 0.6)
        if v > 1 - px_unit * 1.1:
            c = EDGE_LIGHT
    d = abs(v - hamon)
    if d < px_unit * 0.6:                                        # ligne de trempe rouge, fine
        c = RED_GLOW if math.sin(gx * 2.3) > 0.85 else RED
    elif d < px_unit * 2.2 and v < hamon:                        # lueur rouge sous la ligne
        c = mixc(c, RED_DARK, 0.55 * (1 - d / (px_unit * 2.2)))
    return c


def engraving(img, w, h, gx0, flip):
    """Gravure d'or pres de la base (horimono) : trois griffures paralleles, motif des tueurs de dragons."""
    for k in range(3):
        base_x = 14 + k * 3.2                                    # unites depuis le debut de la lame
        pts = []
        for i in range(13):
            t = i / 12.0
            ux = base_x + 6.0 * t                                # griffure : diagonale courbe, du dos vers la trempe
            vv = 0.36 + 0.28 * t + 0.05 * math.sin(t * math.pi)
            pts.append((ux, vv))
        line = []
        for ux, vv in pts:
            lx = (ux - gx0) * K.D
            line.append((w - 1 - lx if flip else lx, vv * h))
        K.stroke(img, line, GOLD if k != 1 else GOLD_LIGHT, 0.55)


def blade_paint(gx0, length, width):
    def paint(face, w, h, seed):
        img = K.canvas(w, h, BLACK)
        if face in ("front", "back"):
            for y in range(h):
                for x in range(w):
                    lx = (w - 1 - x) if face == "back" else x     # face arriere : image inversee en x
                    gx = gx0 + (lx + 0.5) / K.D
                    img[y][x][:3] = list(blade_pixel(gx, (y + 0.5) / h, width))
            if gx0 < 20:
                engraving(img, w, h, gx0, face == "back")
        elif face == "top":                                      # dos : noir, fil d'or central
            for y in range(h):
                for x in range(w):
                    c = mixc(BLACK, BLACK_SHEEN, 0.2 + 0.2 * math.sin(x * 0.3))
                    if y == h // 2:
                        c = GOLD_DARK
                    img[y][x][:3] = list(c)
        elif face == "bottom":                                   # tranchant
            for y in range(h):
                for x in range(w):
                    img[y][x][:3] = list(mixc(EDGE_LIGHT, EDGE, abs(y - h / 2) / (h / 2)))
        return img
    return paint


def kissaki(width, length, gx0):
    """Pointe detouree : le dos file droit, le tranchant remonte vers la pointe ; trempe (boshi) qui suit le tranchant."""
    w, h = int(round(length * K.D)), int(round(width * K.D))
    img = [[[0, 0, 0, 0] for _ in range(w)] for _ in range(h)]
    for y in range(h):
        for x in range(w):
            t = (x + 0.5) / w
            spine = 0.10 * t ** 3                                # le dos s'incline a peine a l'extremite
            edge = 1 - 0.86 * t ** 1.7                           # le tranchant remonte vers la pointe
            v = (y + 0.5) / h
            if v < spine or v > edge:
                continue
            vv = (v - spine) / max(1e-3, edge - spine)           # position relative entre dos et tranchant
            c = blade_pixel(gx0 + (x + 0.5) / K.D, vv, width * max(0.3, edge - spine))
            if v > edge - 1.2 / h:
                c = EDGE_LIGHT
            img[y][x] = [c[0], c[1], c[2], 255]
    return img


def grip_paint(face, w, h, seed):
    """Tsuka : tresse noire en croisillons, losanges rouges (peau de raie laquee), menuki d'or au milieu."""
    img = K.canvas(w, h, BLACK)
    period = 4.5 * K.D
    for y in range(h):
        for x in range(w):
            u = (x % period) / period
            vv = (y + 0.5) / h
            d = abs(u - 0.5) * 2 + abs(vv - 0.5) * 2 * (h / period)  # losange au centre de chaque maille
            if d < 0.55:
                c = mixc(RED, RED_DARK, d / 0.55)
            else:
                band = 0.5 + 0.5 * math.sin((x + y) * 1.3)
                c = mixc((22, 20, 26), (52, 48, 60), band * 0.6)
            img[y][x][:3] = list(c)
    if face in ("front", "back"):                                # menuki : petite griffe d'or au milieu de la poignee
        cx, cy = w * 0.45, h * 0.5
        for k in range(3):
            pts = [(cx - 6 + k * 3 + i * 0.9, cy - 3 + i * 0.7) for i in range(7)]
            K.stroke(img, pts, GOLD, 0.7)
        K.disc(img, cx - 5, cy + 2, 1.3, GOLD_LIGHT)
    return K.finish(img, face, edges=False)


def metal_gold(face, w, h, seed):
    return K.metal(GOLD)(face, w, h, seed)


def black_gold_rim(face, w, h, seed):
    """Noir laque cercle d'or ; sur les grandes faces de la tsuba, anneau rouge fin."""
    img = K.canvas(w, h, BLACK)
    for y in range(h):
        for x in range(w):
            img[y][x][:3] = list(mixc(BLACK, BLACK_SHEEN, 0.25 * max(0.0, math.sin((x + y * 0.7) * 0.35)) ** 2))
    rim = 2 if min(w, h) > 10 else 1
    for y in range(h):
        for x in range(w):
            if min(x, y, w - 1 - x, h - 1 - y) < rim:
                img[y][x][:3] = list(GOLD if (x + y) % 5 else GOLD_LIGHT)
    if face in ("left", "right") and min(w, h) > 24:            # faces de la tsuba : anneau rouge et ajours d'or
        cx, cy = w / 2.0, h / 2.0
        for y in range(h):
            for x in range(w):
                r = math.hypot((x + 0.5 - cx) / (w / 2.0), (y + 0.5 - cy) / (h / 2.0))
                if 0.60 < r < 0.66:
                    img[y][x][:3] = list(RED)
                elif 0.72 < r < 0.76 and int(math.degrees(math.atan2(y - cy, x - cx)) / 30) % 2 == 0:
                    img[y][x][:3] = list(GOLD_DARK)
    return K.finish(img, face, edges=False)


def claw_red(face, w, h, seed):
    img = K.canvas(w, h, RED_DARK)
    for y in range(h):
        for x in range(w):
            img[y][x][:3] = list(mixc(BLACK, RED, (x + 0.5) / w))
    return K.finish(img, face, edges=False)


def gem(face, w, h, seed):
    img = K.canvas(w, h, RED)
    for y in range(h):
        for x in range(w):
            if (x + y) < 3:
                img[y][x][:3] = list(RED_GLOW)
    return img


# ---------------------------------------------------------------------------------------------------------------------
# Modele
# ---------------------------------------------------------------------------------------------------------------------
M = K.Model(NAME)
M.reuse = True
QZ = lambda deg: K.q_axis((0, 0, 1), deg)                         # rotation dans le plan de la lame (vers le dos si > 0)


def node_box(name, pos, size, paint, q=K.ID_Q, pivot_at_start=False, shading="flat"):
    """Boite. pivot_at_start : renvoie un pivot vide (tourne de q) au debut de la boite, la boite en enfant a mi-longueur.
    Pas d'offset de forme : les boites sont etirees (4 px par unite) et l'effet de l'etirement sur l'offset n'est pas verifie."""
    if not pivot_at_start:
        return K.box(M, name, pos, size, paint, q=q, shading=shading)
    p = pivot(name, pos, q)
    p["children"].append(K.box(M, name + "_Box", (size[0] / 2.0, 0, 0), size, paint, shading=shading))
    return p


def pivot(name, pos, q=K.ID_Q):
    return {"id": M.nid(), "name": name, "position": K.vec(pos), "orientation": K.round_q(q), "shape": empty(False), "children": []}


def build():
    # ---- Lame : 3 segments courbes + pointe ; chaque position est relative au centre du segment parent ----
    segs = [  # (longueur, largeur, epaisseur, courbure en degres)
        (24.0, 7.0, 2.0, 0.0),
        (22.0, 6.75, 1.75, 3.0),
        (20.0, 6.5, 1.5, 3.0),
    ]
    tip_len, tip_bend = 11.0, 3.0
    gx = 0.0
    nodes = []
    for i, (L, W, T, bend) in enumerate(segs):
        nodes.append(node_box("Blade_%d" % i, (0, 0, 0), (L, W, T), blade_paint(gx, L, W), q=QZ(bend), pivot_at_start=True))
        gx += L - 0.25
    tip = pivot("Blade_Tip", (0, 0, 0), QZ(tip_bend))
    tip["children"].append(K.quad(M, "Blade_Tip_Quad", (tip_len / 2.0, 0, 0), kissaki(segs[-1][1], tip_len, gx)))

    # emboitement : chaque segment part de la fin du precedent (chevauchement d'un quart d'unite)
    for i in range(len(nodes) - 1, 0, -1):
        nodes[i]["position"] = K.vec((segs[i - 1][0] - 0.25, 0, 0))
        nodes[i - 1]["children"].append(nodes[i])
    tip["position"] = K.vec((segs[-1][0] - 0.25, 0, 0))
    nodes[-1]["children"].append(tip)
    blade = nodes[0]

    # ---- Monture (positions depuis le pivot du Handle, converties : centre de la poignee a x = -4) ----
    GRIP_C = -4.0

    def at(x, y=0.0, z=0.0):
        return (x - GRIP_C, y, z)

    kids = []
    blade["position"] = K.vec(at(13.0))
    kids.append(blade)
    kids.append(node_box("Habaki", at(13.5), (2.75, 7.75, 2.75), metal_gold))
    kids.append(node_box("Fuchi", at(8.75), (1.5, 6.25, 5.25), metal_gold))
    kids.append(node_box("Tsuba", at(10.75), (2.0, 14.0, 9.5), black_gold_rim))
    kids.append(node_box("Tsuba_Cross", at(10.75), (1.75, 11.0, 12.5), black_gold_rim))
    kids.append(node_box("Kashira", at(-18.75), (3.5, 6.25, 5.25), black_gold_rim))
    kids.append(node_box("Kashira_Gem", at(-20.75), (0.75, 2.0, 2.0), gem, shading="fullbright"))
    # Patte de dragon en or qui serre la base de la lame : deux serres sur chaque face, rien sur le dos
    for z in (1.5, -1.5):
        side = "P" if z > 0 else "N"
        kids.append(claw_gold("Foot_%s_Up" % side, at(12.0, 2.0, z), 0.8, -6, (-8, -10)))
        kids.append(claw_gold("Foot_%s_Down" % side, at(12.0, -2.0, z), 0.8, 6, (8, 10)))

    # Pivot du Handle recule de 4 unites (x local = +y de l'attache) : la poignee tombe la ou est celle du katana vanilla
    handle = node_box("Handle", (0, -36 + GRIP_C, 0), (26.0, 5.5, 4.5), grip_paint, q={"x": -0.5, "y": -0.5, "z": 0.5, "w": 0.5})
    handle["children"] = kids
    origin_item = {"id": M.nid(), "name": "Origin_Item", "position": K.vec((0, -37, 5)), "orientation": dict(K.ID_Q),
                   "shape": empty(False), "children": [handle]}
    origin_proj = {"id": M.nid(), "name": "Origin_Projectile", "position": K.vec((0, 73, -5)), "orientation": dict(K.ID_Q),
                   "shape": empty(True), "children": [origin_item]}
    root = {"id": M.nid(), "name": "R-Attachment", "position": K.vec((0, 20, 0)), "orientation": dict(K.ID_Q),
            "shape": empty(True), "children": [origin_proj]}
    return root, handle


def claw_gold(name, pos, scale, angle, bends):
    """Serre d'or (patte de dragon) : deux segments qui se referment sur la lame."""
    a = node_box(name + "_0", (0, 0, 0), (round(4.0 * scale * 4) / 4, round(1.5 * scale * 4) / 4, 0.75), metal_gold,
                 q=QZ(angle), pivot_at_start=True)
    b = node_box(name + "_1", (4.0 * scale - 0.25, 0, 0), (round(3.0 * scale * 4) / 4, round(1.0 * scale * 4) / 4, 1.0), metal_gold,
                 q=QZ(bends[0]), pivot_at_start=True)
    c = node_box(name + "_2", (3.0 * scale - 0.25, 0, 0), (round(2.0 * scale * 4) / 4, 0.5, 0.5), claw_red,
                 q=QZ(bends[1]), pivot_at_start=True)
    b["children"].append(c)
    a["children"].append(b)
    a["position"] = K.vec(pos)
    return a


def empty(piece):
    return {"type": "none", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": 1, "y": 1, "z": 1}, "settings": {"isPiece": piece},
            "textureLayout": {}, "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": "flat"}


# ---------------------------------------------------------------------------------------------------------------------
# Rendu (icone et apercus) : quads dans le repere du Handle
# ---------------------------------------------------------------------------------------------------------------------
def handle_quads(handle, tex, q):
    h = copy.deepcopy(handle)
    return K.quads_of(h, (0, 0, 0), q, tex, ("\0",))


def outline(img, color=(20, 14, 12)):
    S = len(img)
    out = [[p[:] for p in row] for row in img]
    for y in range(S):
        for x in range(S):
            if img[y][x][3] > 40:
                continue
            if any(0 <= x + dx < S and 0 <= y + dy < S and img[y + dy][x + dx][3] > 120 for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                out[y][x] = [color[0], color[1], color[2], 255]
    return out


def main():
    root, handle = build()
    fix_coplanar(handle, "Ryukiri")                                    # pas de faces coplanaires (scintillement)
    model_dir = os.path.join(RES, "Common", MODEL_REL)
    os.makedirs(model_dir, exist_ok=True)
    W, H, tex = M.texture()
    png.write(os.path.join(model_dir, NAME + ".png"), W, H, tex)
    json.dump({"nodes": [root], "lod": "auto"}, open(os.path.join(model_dir, NAME + ".blockymodel"), "w"), indent=1)
    print("texture %dx%d" % (W, H))

    # Icone : lame en diagonale, pointe en haut a droite, dos vers le haut-gauche (comme les icones vanilla)
    quads = handle_quads(handle, tex, K.q_axis((0, 0, 1), 45))
    lifted = [[[min(255, c[0] * 1.25 + 22), min(255, c[1] * 1.25 + 20), min(255, c[2] * 1.25 + 26), c[3]] for c in row] for row in tex]
    big = K.draw(handle_quads(handle, lifted, K.q_axis((0, 0, 1), 45)), 256, 15, 10, margin=0.04)   # noir releve : lisible a 64 px
    icon = outline(K.downscale(big, 4))
    icon_dir = os.path.join(RES, "Common", "Icons", "ItemsGenerated")
    os.makedirs(icon_dir, exist_ok=True)
    png.write(os.path.join(icon_dir, NAME + ".png"), 64, 64, icon)

    if PREVIEW:
        os.makedirs(PREVIEW, exist_ok=True)
        png.write(os.path.join(PREVIEW, "ryukiri_face.png"), 1024, 1024, K.draw(handle_quads(handle, tex, K.ID_Q), 1024, 0, 0, margin=0.03))
        png.write(os.path.join(PREVIEW, "ryukiri_3q.png"), 768, 768, K.draw(quads, 768, 35, 20, margin=0.04))
        png.write(os.path.join(PREVIEW, "ryukiri_dos.png"), 768, 768, K.draw(quads, 768, 0, 75, margin=0.04))
        png.write(os.path.join(PREVIEW, "ryukiri_icon_x4.png"), 256, 256,
                  [[icon[y // 4][x // 4] for x in range(256)] for y in range(256)])
        png.write(os.path.join(PREVIEW, "ryukiri_atlas.png"), W, H, tex)

    write_trail()
    write_energy_blade()
    write_item()
    print("ok")


# ---------------------------------------------------------------------------------------------------------------------
# Trainee et item
# ---------------------------------------------------------------------------------------------------------------------
def write_trail():
    trail = {"TexturePath": "Trails/Charged_Red.png", "LifeSpan": 7, "Roll": 0,
             "Start": {"Color": "rgba(#ffd9a0, 1)", "Width": 0.32},
             "End": {"Color": "rgba(#5a0610, 0.35)", "Width": 0},
             "LightInfluence": 0, "Smooth": True, "RenderMode": "BlendLinear"}
    path = os.path.join(RES, "Server", "Entity", "Trails", "Ryukiri_Trail.json")
    json.dump(trail, open(path, "w"), indent=2, ensure_ascii=False)


# ---------------------------------------------------------------------------------------------------------------------
# Lame d'energie du Tranche-Montagne : la lame de la Windstrike vanilla (arc horizontal, qui fait face a +-Z), dressee a la
# verticale (roulis de 90 degres autour de z, l'axe du vol) et teintee rouge sang / or. Portee par
# Projectile_Config_Ryukiri_Lame et deplacee par le code (Ryukiri.TrancheMontagne), comme la croix des Lames Jumelles.
# ---------------------------------------------------------------------------------------------------------------------
BLADE = "Ryukiri_Lame"
BLADE_RAMP = ((110, 0, 12), (255, 214, 130))                     # ombre rouge sang -> lumiere d'or


def write_energy_blade():
    import tempfile, zipfile
    z = zipfile.ZipFile(K.ASSETS)
    base = json.loads(z.read("Common/Items/Projectiles/WindStrike_Projectile.blockymodel"))
    with tempfile.NamedTemporaryFile(suffix=".png", delete=False) as f:
        f.write(z.read("Common/Items/Projectiles/WindStrike_Projectile_Texture.png"))
    w, h, px = png.read(f.name)
    os.unlink(f.name)
    lo, hi = BLADE_RAMP
    for row in px:
        for c in row:
            lum = ((0.3 * c[0] + 0.59 * c[1] + 0.11 * c[2]) / 255) ** 4.0   # texture tres claire : contraste renforce
            c[0], c[1], c[2] = [int(lo[i] + (hi[i] - lo[i]) * lum) for i in range(3)]
    out_dir = os.path.join(RES, "Common", "Items", "Projectiles")
    png.write(os.path.join(out_dir, BLADE + ".png"), w, h, px)

    ids = iter(range(1000, 2000))

    def renum(n):
        n["id"] = str(next(ids))
        for c in n.get("children", []):
            renum(c)
    children = copy.deepcopy(base["nodes"][0]["children"])
    for c in children:
        renum(c)
    roll = {"id": str(next(ids)), "name": "Lame", "position": {"x": 0, "y": 0, "z": 0},
            "orientation": K.round_q(K.q_axis((0, 0, 1), 90)), "shape": empty(False), "children": children}
    model = copy.deepcopy(base)
    model["nodes"][0]["children"] = [roll]
    json.dump(model, open(os.path.join(out_dir, BLADE + ".blockymodel"), "w"), indent=1)

    z_trail = json.loads(z.read("Server/Entity/Trails/Abilities/Ability_Windstrike_Trail.json"))
    t = copy.deepcopy(z_trail)
    t["Start"]["Color"], t["End"]["Color"] = "rgba(#ffcf7a, 0.95)", "rgba(#8a0010, 0)"
    json.dump(t, open(os.path.join(RES, "Server", "Entity", "Trails", "Ryukiri_Lame_Trail.json"), "w"), indent=2)

    asset = {
        "$Comment": "Tranche-Montagne (Ryukiri) : lame d'energie verticale deplacee par le code. HitBox elargie (LOD serveur). "
                    "Genere par scripts/artefacts/gen_ryukiri.py.",
        "Model": "Items/Projectiles/" + BLADE + ".blockymodel",
        "Texture": "Items/Projectiles/" + BLADE + ".png",
        "MinScale": 1.0, "MaxScale": 1.0,
        "HitBox": {"Max": {"X": 1.2, "Y": 1.2, "Z": 0.6}, "Min": {"X": -1.2, "Y": -1.2, "Z": -0.6}},
        "Trails": [{"TrailId": "Ryukiri_Lame_Trail", "TargetNodeName": "Lame", "PositionOffset": {"X": x, "Z": 0.5}} for x in (1.0, -1.0)],
        "Light": {"Color": "#ff5a2a", "Radius": 8},
    }
    json.dump(asset, open(os.path.join(RES, "Server", "Models", "Projectiles", "Abilities", BLADE + ".json"), "w"), indent=2, ensure_ascii=False)
    neutral = {"Interactions": [{"Type": "Simple", "RunTime": 0.01}]}
    config = {"$Comment": "Lame du Tranche-Montagne (Ryukiri), deplacee par le code. Interactions neutres obligatoires.",
              "Model": BLADE, "UseModelScale": True,
              "Physics": {"Type": "Standard", "Gravity": 0, "TerminalVelocityAir": 1, "TerminalVelocityWater": 1,
                          "Bounciness": 0, "SticksVertically": False},
              "LaunchForce": 0, "SpawnOffset": {"X": 0, "Y": 0, "Z": 0},
              "Interactions": {"ProjectileHit": neutral, "ProjectileMiss": neutral}}
    json.dump(config, open(os.path.join(RES, "Server", "ProjectileConfigs", "Abilities", "Projectile_Config_" + BLADE + ".json"), "w"),
              indent=2, ensure_ascii=False)


def damage_var(parent, physical, energy=None):
    entry = {"Parent": parent, "DamageCalculator": {"BaseDamage": {"Physical": physical}},
             "DamageEffects": {"WorldSoundEventId": "SFX_Sword_T2_Impact", "LocalSoundEventId": "SFX_Sword_T2_Impact"}}
    if energy:
        entry["EntityStatsOnHit"] = [{"EntityStatId": "SignatureEnergy", "Amount": energy}]
    return {"Interactions": [entry]}


def write_item():
    item = {
        "Parent": "Template_Weapon_Sword",
        "TranslationProperties": {
            "Name": "Ryūkiri, Tranche-Dragon des Runkandel",
            "Description": "Artefact divin de la famille Runkandel, forgé pour abattre les dragons. Elle ne répond qu'au sang "
                           "des Runkandel. Ses coups infligent +100 % de dégâts aux dragons et le porteur subit 40 % de dégâts "
                           "de feu en moins. Signature, Tranche-Montagne : le porteur concentre le feu des dragons, puis libère une lame "
                           "d'énergie géante qui file droit devant sur 60 blocs, fend le terrain, projette la roche et frappe "
                           "tous les ennemis qu'elle traverse ; la saignée explose ensuite en chaîne jusqu'au bout de la ligne."
        },
        "Quality": "Divin",
        "ItemLevel": 800,
        "Model": MODEL_REL + "/" + NAME + ".blockymodel",
        "Texture": MODEL_REL + "/" + NAME + ".png",
        "Icon": "Icons/ItemsGenerated/" + NAME + ".png",
        "IconProperties": {"Scale": 0.33, "Rotation": [45, 90, 0], "Translation": [-33, -38]},
        "Categories": [familles.category("Runkandel"), "Items.Weapons"],
        "MaxStack": 1,
        "Variant": False,
        "Interactions": {"Ability1": "Root_Ryukiri_Signature"},
        "InteractionVars": {
            "Swing_Left_Damage": damage_var("Weapon_Sword_Primary_Swing_Left_Damage", 52),
            "Swing_Right_Damage": damage_var("Weapon_Sword_Primary_Swing_Right_Damage", 52),
            "Swing_Down_Damage": damage_var("Weapon_Sword_Primary_Swing_Down_Damage", 92),
            "Thrust_Damage": damage_var("Weapon_Sword_Primary_Thrust_Damage", 132, 3),
            "Guard_Wield": {"Interactions": [{"Parent": "Weapon_Sword_Secondary_Guard_Wield",
                                              "StaminaCost": {"Value": 10, "CostType": "Damage"}}]},
        },
        "MaxDurability": 0,
        "DurabilityLossOnHit": 0,
        "Trails": [{"TrailId": "Ryukiri_Trail", "TargetNodeName": "Handle", "TargetEntityPart": "PrimaryItem",
                    "PositionOffset": {"X": 0.9, "Y": 0.05}, "RotationOffset": {"Roll": 90}}],
        "Recipe": {
            "TimeSeconds": 30,
            "Input": [{"ItemId": "Ingredient_Bar_Divin", "Quantity": 24}, {"ItemId": "Ingredient_Bar_Adamantite", "Quantity": 40}],
            "BenchRequirement": [{"Type": "Crafting", "Id": "Forge_Divine", "Categories": ["Forge_Epees"]}]
        }
    }
    path = os.path.join(RES, "Server", "Item", "Items", "Weapon", "Sword", NAME + ".json")
    json.dump(item, open(path, "w"), indent=2, ensure_ascii=False)

    root = {"RequireNewClick": True, "Interactions": ["Ryukiri_Signature"]}
    d = os.path.join(RES, "Server", "Item", "RootInteractions", "Weapons", "Ryukiri")
    os.makedirs(d, exist_ok=True)
    json.dump(root, open(os.path.join(d, "Root_Ryukiri_Signature.json"), "w"), indent=2)

    sig = {
        "$Comment": "Signature de Ryukiri, Tranche-Montagne (energie de signature pleine) : sort Java TrancheMontagne.",
        "Type": "StatsCondition",
        "Costs": {"SignatureEnergy": 100},
        "ValueType": "Percent",
        "Next": {"Type": "Serial", "Interactions": [
            {"Type": "RuneSpell", "RunTime": 0.05, "Spell": "TrancheMontagneEveil"},
            {"Type": "Simple", "RunTime": 0.85, "Effects": {"ItemAnimationId": "SwingLeftCharging",
                                                             "WorldSoundEventId": "SFX_Sword_T2_Swing_Down",
                                                             "LocalSoundEventId": "SFX_Sword_T2_Swing_Down_Local"}},
            {"Type": "Simple", "RunTime": 0.3, "Effects": {"ItemAnimationId": "SwingDownStrong",
                                                            "WorldSoundEventId": "SFX_Sword_T2_Swing_Down",
                                                            "LocalSoundEventId": "SFX_Sword_T2_Swing_Down_Local",
                                                            "ClearAnimationOnFinish": True}},
            {"Type": "RuneSpell", "RunTime": 0.05, "Spell": "TrancheMontagne"},
            {"Type": "ChangeStat", "StatModifiers": {"SignatureEnergy": -100}, "ValueType": "Percent"}
        ]}
    }
    d = os.path.join(RES, "Server", "Item", "Interactions", "Weapons", "Ryukiri")
    os.makedirs(d, exist_ok=True)
    json.dump(sig, open(os.path.join(d, "Ryukiri_Signature.json"), "w"), indent=2)


main()
