"""Mumei et Kagero, Lames de l'Observateur (2026-11-17) : arme reservee a la classe unique Observateur.

Demande de l'utilisateur : deux petits katanas, chacun dans un fourreau accroche dans le dos, pommeau vers l'exterieur de
chaque cote ; un melange de katana et de dague.

  - En main : deux lames courtes (kodachi : lame courbe de katana a pointe kissaki, mais de la taille d'une dague), une dans
    chaque main. L'objet est une dague (template vanilla Template_Weapon_Daggers : "RenderDualWielded", gestes, combo,
    bond et signature Razorstrike des dagues), donc la meme lame est rendue dans les deux mains. Hierarchie du modele
    vanilla Items/Weapons/Dagger/Iron.blockymodel (R-Attachment > Origin_Projectile > Origin_Item > Handle), seule la
    poignee (Handle) et ses enfants sont remplaces. Repere du Handle : x le long de la lame, +y le dos, -y le tranchant.
  - Dans le dos : deux fourreaux a l'horizontale au creux des reins, legerement inclines, qui se croisent en X ; la
    poignee de la lame droite sort a droite, celle de la lame gauche a gauche. Ce sont des attachements de modele
    (ModelAttachment) poses sur le modele du joueur par Java (ObservateurLamesSystem) : racine "Belly" (isPiece), qui se
    colle a l'os du ventre et suit le buste. Deux modeles qui partagent une texture :
      Observateur_Fourreaux_Dos (lames rengainees : l'arme est dans l'inventaire mais pas en main),
      Observateur_Fourreaux_Dos_Vide (fourreaux vides : les lames sont en main).

Monture : acier bleui a la trempe d'argent lunaire (lame), habaki, seppa et fuchi d'argent, tsuba ronde de fer noir a l'Oeil
de l'Observateur (amande d'argent, iris violet), poignee de soie indigo sur galuchat gris, kashira d'argent a la pierre-oeil
violette ; fourreaux de laque noire pailletee d'argent, embouchure, bague et bout d'argent, cordon sageo violet.

Usage : python3 scripts/artefacts/gen_lames_observateur.py <src/main/resources> [dossier_apercu]
"""
import copy, json, math, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
RES = sys.argv[1]
PREVIEW = sys.argv[2] if len(sys.argv) > 2 else None
sys.argv = [sys.argv[0], RES]                                   # gen_tenryumaru et gen_divines lisent sys.argv a l'import
sys.path.insert(0, HERE)
import gen_tenryumaru as T  # noqa: E402  (peintures de lame, poignee, fourreau ; couleurs remplacees ci-dessous)
import gen_divines as D  # noqa: E402  (hierarchie vanilla, mise a l'echelle des degats)
from coplanar import fix_coplanar  # noqa: E402

K, png = T.K, T.png
MODEL_REL = T.MODEL_REL
NAME = "Artefact_Daggers_Observateur"
BACK = "Observateur_Fourreaux_Dos"
VANILLA = "Items/Weapons/Dagger/Iron.blockymodel"
FACTOR = 3.8                                                    # ligne Legendaire du catalogue (x3,8 l'arme de fer vanilla)

# ---------------------------------------------------------------------------------------------------------------------
# Couleurs (globales de gen_tenryumaru lues par ses peintures au moment de peindre)
# ---------------------------------------------------------------------------------------------------------------------
T.BLACK, T.BLACK_SHEEN = (18, 22, 32), (80, 90, 116)           # acier bleui
T.EDGE, T.EDGE_LIGHT = (118, 130, 156), (228, 236, 250)        # zone trempee
T.HAMON, T.HAMON_LIGHT, T.HAMON_DARK = (172, 192, 236), (238, 246, 255), (66, 78, 114)   # trempe d'argent lunaire
T.SHINOGI = (132, 142, 168)
T.HORIMONO = False
T.GOLD, T.GOLD_LIGHT, T.GOLD_DARK = (180, 186, 200), (238, 242, 250), (88, 92, 108)    # argent (habaki, fuchi, paillettes)
T.IVORY = (172, 174, 186)                                       # galuchat gris
T.SILK_DARK, T.SILK_LIGHT = (22, 16, 40), (78, 58, 124)        # soie indigo
T.LACQ, T.LACQ_SHEEN = (10, 10, 14), (60, 58, 76)               # laque noire
T.SAGEO, T.SAGEO_DARK = (70, 40, 120), (32, 18, 60)            # cordon violet
IRIS, IRIS_CORE = (150, 104, 255), (232, 214, 255)
IRON, IRON_SHEEN = (20, 20, 26), (64, 64, 78)

M = None
PAINTS = []                                                     # le cache d'atlas est indexe par id(peinture) : les garder en vie


def keep(p):
    PAINTS.append(p)
    return p


def qz(d):
    return K.q_axis((0, 0, 1), d)


def qx(d):
    return K.q_axis((1, 0, 0), d)


def box(name, pos, size, paint, q=K.ID_Q, shading="flat"):
    keep(paint)
    return K.box(M, name, pos, size, paint, q=q, shading=shading)


def empty(piece=False):
    return {"type": "none", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": 1, "y": 1, "z": 1}, "settings": {"isPiece": piece},
            "textureLayout": {}, "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": "flat"}


def pivot(name, pos, q=K.ID_Q, children=()):
    return {"id": M.nid(), "name": name, "position": K.vec(pos), "orientation": K.round_q(q), "shape": empty(), "children": list(children)}


# ---------------------------------------------------------------------------------------------------------------------
# Peintures propres : l'Oeil de l'Observateur
# ---------------------------------------------------------------------------------------------------------------------
def draw_eye(img, cx, cy, r):
    """Oeil en amande d'argent, iris violet, pupille fendue noire, eclat clair."""
    for y in range(len(img)):
        for x in range(len(img[0])):
            u, v = (x + 0.5 - cx) / r, (y + 0.5 - cy) / r
            if abs(u) >= 0.92:
                continue
            half = 0.46 * (1 - (u / 0.92) ** 2)
            if abs(v) > half + 0.09:
                continue
            if abs(v) > half - 0.04:                               # paupieres d'argent
                img[y][x][:3] = list(T.GOLD_LIGHT if v < 0 else T.GOLD)
                continue
            d = math.hypot(u, v)
            if d < 0.08 or (abs(u) < 0.05 and abs(v) < 0.3):          # pupille fendue
                img[y][x][:3] = [8, 6, 12]
            elif d < 0.3:
                img[y][x][:3] = list(K.mix(IRIS_CORE, IRIS, d / 0.3))
            else:
                img[y][x][:3] = list(K.mix(T.GOLD_LIGHT, T.GOLD, 0.4))   # blanc de l'oeil : argent
    K.put(img, cx - 0.22 * r, cy - 0.16 * r, (255, 255, 255))


def eye_tsuba(face, w, h, seed):
    """Tsuba ronde de fer noir cerclee d'argent, l'Oeil au centre ; coins detoures, chant visible pres du milieu."""
    img = K.canvas(w, h, IRON)
    if face in ("left", "right"):
        cx, cy, r = w / 2.0, h / 2.0, min(w, h) / 2.0
        for y in range(h):
            for x in range(w):
                d = math.hypot(x + 0.5 - cx, y + 0.5 - cy)
                if d > r:
                    img[y][x][3] = 0
                elif d > r - 2.2:
                    img[y][x][:3] = list(T.GOLD_LIGHT if d > r - 1.0 else T.GOLD)
                else:
                    img[y][x][:3] = list(K.mix(IRON, IRON_SHEEN, 0.15 + 0.25 * K.noise(x * 0.7, y * 0.7)))
        draw_eye(img, cx, cy, r - 3.5)
        return img
    for y in range(h):
        for x in range(w):
            u = abs((x + 0.5) / w - 0.5) * 2 if face in ("top", "bottom", "front", "back") else abs((y + 0.5) / h - 0.5) * 2
            if u > 0.42:
                img[y][x][3] = 0
            else:
                img[y][x][:3] = list(T.GOLD)
    return img


def eye_gem(face, w, h, seed):
    img = K.canvas(w, h, IRIS)
    cx, cy = w / 2.0, h / 2.0
    for y in range(h):
        for x in range(w):
            d = math.hypot(x + 0.5 - cx, y + 0.5 - cy) / (min(w, h) / 2.0)
            img[y][x][:3] = list(K.mix(IRIS_CORE, IRIS, min(1.0, d)))
    K.border(img, 0, T.GOLD_LIGHT)
    return img


def kashira(face, w, h, seed):
    img = T.metal_gold(face, w, h, seed)
    if face in ("left", "right") and min(w, h) > 10:
        draw_eye(img, w / 2.0, h / 2.0, min(w, h) / 2.0 - 1)
    return img


# ---------------------------------------------------------------------------------------------------------------------
# Monture et lame (repere du Handle : origine au centre de la poignee)
# ---------------------------------------------------------------------------------------------------------------------
GRIP = (16.0, 4.5, 3.75)
BLADE_START = 11.5
BLADE = [(14.0, 5.0, 1.5, 0.0), (12.0, 4.75, 1.25, 2.5), (9.0, 4.5, 1.25, 2.5)]   # longueur, largeur, epaisseur, courbure
TIP = (8.0, 2.5)                                                                     # kissaki : longueur, courbure


def hilt_kids(prefix=""):
    """Monture autour de la poignee : fuchi, seppa, tsuba a l'Oeil, habaki ; kashira et sa pierre au bout."""
    return [box(prefix + "Fuchi", (8.6, 0, 0), (1.25, 5.0, 4.25), T.metal_gold),
            box(prefix + "Seppa_A", (9.5, 0, 0), (0.5, 6.5, 5.0), T.metal_gold),
            box(prefix + "Tsuba", (10.25, 0, 0), (1.0, 10.0, 10.0), eye_tsuba),
            box(prefix + "Seppa_B", (11.0, 0, 0), (0.5, 6.25, 4.5), T.metal_gold),
            box(prefix + "Kashira", (-8.85, 0, 0), (1.75, 5.0, 4.25), kashira),
            box(prefix + "Kashira_Oeil", (-9.85, 0, 0), (0.5, 2.5, 2.5), eye_gem, shading="fullbright")]


def blade():
    """Lame courbe de kodachi : troncons emboites (courbure vers le dos, +y) et pointe kissaki ; habaki d'argent a la base."""
    gx, nodes = 0.0, []
    for i, (L, W, Th, bend) in enumerate(BLADE):
        p = pivot("Blade_%d" % i, (0, 0, 0), qz(bend))
        p["children"].append(box("Blade_%d_Box" % i, (L / 2.0, 0, 0), (L, W, Th), keep(T.blade_paint(gx, L, W))))
        nodes.append(p)
        gx += L - 0.25
    tip = pivot("Blade_Tip", (BLADE[-1][0] - 0.25, 0, 0), qz(TIP[1]))
    tip["children"].append(K.quad(M, "Blade_Tip_Quad", (TIP[0] / 2.0, 0, 0), T.kissaki(BLADE[-1][1], TIP[0], gx)))
    for i in range(len(nodes) - 1, 0, -1):
        nodes[i]["position"] = K.vec((BLADE[i - 1][0] - 0.25, 0, 0))
        nodes[i - 1]["children"].append(nodes[i])
    nodes[-1]["children"].append(tip)
    nodes[0]["position"] = K.vec((BLADE_START, 0, 0))
    nodes[0]["children"].append(box("Habaki", (1.0, 0, 0), (2.25, 5.75, 2.5), T.metal_gold))
    return nodes[0]


def build_held():
    """Lame en main : hierarchie de la dague vanilla, notre poignee au meme centre de forme que la sienne."""
    root, handle = D.vanilla_chain(VANILLA)
    off = handle["shape"].get("offset", {"x": 0, "y": 0, "z": 0})
    shift = K.qrot(handle["orientation"], (off["x"], off["y"], off["z"]))
    pos = (handle["position"]["x"] + shift[0], handle["position"]["y"] + shift[1], handle["position"]["z"] + shift[2])
    grip = box("Handle", pos, GRIP, T.grip_paint, q=handle["orientation"])
    grip["children"] = hilt_kids() + [blade()]
    handle.clear()
    handle.update(grip)
    return root, handle


# ---------------------------------------------------------------------------------------------------------------------
# Fourreaux du dos
# ---------------------------------------------------------------------------------------------------------------------
SAYA = [(15.0, 6.75, 3.25, 0.0), (13.0, 6.5, 3.0, 2.5), (14.5, 6.25, 2.75, 2.5)]   # memes coudes que la lame
SAYA_START = 11.25                                                                 # embouchure (koiguchi)
TILT = 16.0                                                    # inclinaison : poignee plus haute que la pointe (degres)
CROSS_X = 2.0                                                  # decalage du milieu de l'arme vers sa poignee (unites)
HEIGHT = -2.5                                                  # hauteur du croisement, depuis le centre du ventre
DEPTH = (-12.0, -16.25)                                        # profondeur des deux fourreaux (dos du ventre : z = -9)
MID = (SAYA_START + sum(s[0] for s in SAYA) - 10.0) / 2.0      # milieu de l'arme rengainee, du kashira au kojiri


def sheath(prefix, out):
    """Fourreau dans le repere de la poignee (x vers la pointe) ; out = cote exterieur (+1 / -1 en z local)."""
    segs, gx = [], -1.0
    for i, (L, W, Th, bend) in enumerate(SAYA):
        p = pivot("%sSaya_%d" % (prefix, i), (0, 0, 0), qz(bend))
        p["children"].append(box("%sSaya_%d_Box" % (prefix, i), (L / 2.0, 0, 0), (L, W, Th), keep(T.saya_paint(gx))))
        segs.append(p)
        gx += L
    for i in range(1, len(segs)):
        segs[i]["position"] = K.vec((SAYA[i - 1][0], 0, 0))
        segs[i - 1]["children"].append(segs[i])
    segs[0]["position"] = K.vec((SAYA_START, 0, 0))
    L = SAYA[-1][0]
    segs[-1]["children"] += [box(prefix + "Kojiri", (L + 1.0, 0, 0), (2.0, 6.75, 3.5), T.metal_gold),
                             box(prefix + "Kojiri_Bout", (L + 2.25, 0, 0), (0.5, 5.0, 2.5), T.metal_gold)]
    k = SAYA_START
    return [segs[0],
            box(prefix + "Koiguchi", (k + 0.9, 0, 0), (1.75, 7.25, 3.75), T.metal_gold),
            box(prefix + "Bague", (k + 9.0, 0, 0), (0.75, 7.25, 3.75), T.metal_gold),
            box(prefix + "Kurikata", (k + 5.0, 1.0, out * 2.1), (2.5, 2.0, 1.0), T.metal_gold),
            K.rod(M, prefix + "Sageo_A", (k + 5.0, 1.5, out * 2.6), (k + 9.5, -1.5, out * 2.4), 1.0, keep(T.sageo_paint)),
            K.rod(M, prefix + "Sageo_B", (k + 9.5, -1.5, out * 2.4), (k + 14.0, 2.0, out * 2.3), 1.0, T.sageo_paint)]


def hilt(prefix):
    grip = box(prefix + "Tsuka", (0, 0, 0), GRIP, T.grip_paint)
    grip["children"] = hilt_kids(prefix)
    return grip


def back_assembly(side):
    """Une lame rengainee dans le dos. side = -1 : lame de la main droite (poignee a droite du porteur, -X), +1 : gauche.
    Tranchant vers le haut (dos de la lame en bas), face large du fourreau contre le dos."""
    prefix = "R-" if side < 0 else "L-"
    if side < 0:
        q = K.qmul(qz(-TILT), qx(180))                              # x local -> +X (pointe a gauche du porteur), y -> bas
    else:
        q = qz(180 + TILT)                                          # x local -> -X (pointe a droite du porteur), y -> bas
    ax = K.qrot(q, (1, 0, 0))
    zl = K.qrot(q, (0, 0, 1))
    out = 1 if zl[2] < 0 else -1                                    # cote local tourne vers -Z (loin du corps)
    depth = DEPTH[0] if side < 0 else DEPTH[1]
    target = (side * CROSS_X, HEIGHT, depth)                        # milieu de l'arme (vers sa poignee) au croisement
    pos = tuple(target[i] - ax[i] * MID for i in range(3))
    node = pivot(prefix + "Lame", pos, q)
    node["children"] = sheath(prefix, out)
    node["hilt"] = hilt(prefix)
    return node


def build_back(full):
    nodes = []
    for side in (-1, 1):
        a = back_assembly(side)
        h = a.pop("hilt")
        if full:
            a["children"].insert(0, h)
        nodes.append(a)
    plate = box("Attache", (0, HEIGHT, -10.0), (7.0, 7.0, 1.5), keep(T.saya_paint(0.0)))       # plaque de cuir laque ou les sangles se croisent
    plate["children"].append(box("Attache_Oeil", (0, 0, -0.875), (3.5, 3.5, 0.25), eye_gem, shading="fullbright"))
    nodes.append(plate)
    return K.piece(M, "Belly", (0, 0, 0), nodes)


# ---------------------------------------------------------------------------------------------------------------------
# Item, trainees
# ---------------------------------------------------------------------------------------------------------------------
DESCRIPTION = ("Arme de l'Observateur. Deux lames courtes, mi-katana mi-dague, que l'Œil du souverain porte en travers des reins, "
               "une poignée de chaque côté. Forgées pour frapper sans bruit et disparaître. Seul l'Observateur peut les manier.")


def write_item():
    raw = D.zjson("Server/Item/Items/Weapon/Daggers/Weapon_Daggers_Iron.json")
    item = {k: v for k, v in raw.items() if k not in ("Recipe", "TranslationProperties", "Variant")}
    item["TranslationProperties"] = {"Name": "Mumei et Kagerō, Lames de l'Observateur", "Description": DESCRIPTION}
    item.update({"Quality": "Legendary", "ItemLevel": 580,
                 "Model": "%s/%s.blockymodel" % (MODEL_REL, NAME), "Texture": "%s/%s.png" % (MODEL_REL, NAME),
                 "Icon": "Icons/ItemsGenerated/%s.png" % NAME, "Categories": ["Eldanior_System.Artefacts", "Items.Weapons"],
                 "MaxStack": 1, "Variant": False, "MaxDurability": 0, "DurabilityLossOnHit": 0})
    item["InteractionVars"] = D.scale_vars(item["InteractionVars"], FACTOR)
    item["Trails"] = [{"TrailId": "Observateur_Lames_Trail", "TargetNodeName": "Handle", "TargetEntityPart": part,
                       "PositionOffset": {"X": 0.6, "Y": 0}, "RotationOffset": {"Roll": 90}} for part in ("PrimaryItem", "SecondaryItem")]
    path = os.path.join(RES, "Server", "Item", "Items", "Weapon", "Daggers", NAME + ".json")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(item, f, indent=2, ensure_ascii=False)
    trail = {"TexturePath": "Trails/Basic.png", "LifeSpan": 6, "Roll": 0, "Start": {"Color": "rgba(#d8c8ff, 1)", "Width": 0.22},
             "End": {"Color": "rgba(#3a1870, 0.3)", "Width": 0}, "LightInfluence": 0, "Smooth": True, "RenderMode": "BlendLinear"}
    with open(os.path.join(RES, "Server", "Entity", "Trails", "Observateur_Lames_Trail.json"), "w") as f:
        json.dump(trail, f, indent=2)


# ---------------------------------------------------------------------------------------------------------------------
# Icone et apercus
# ---------------------------------------------------------------------------------------------------------------------
def icon(handle, tex):
    """Les deux lames croisees en X, poignees en bas de chaque cote, halo violet."""
    lifted = [[[min(255, c[0] * 1.2 + 20), min(255, c[1] * 1.2 + 20), min(255, c[2] * 1.2 + 26), c[3]] for c in row] for row in tex]
    quads = []
    for ang, shift in ((52, (-9.0, 0, 0)), (128, (9.0, 0, 1.0))):
        q = qz(ang)
        quads += K.quads_of(copy.deepcopy(dict(handle, position=K.vec((0, 0, 0)), orientation=dict(K.ID_Q))), shift, q, lifted, ("\0",))
    big = K.draw(quads, 256, 0, 0, margin=0.05)
    return T.glow(T.outline(K.downscale(big, 4)), color=(170, 120, 255), strength=0.35)


def previews(handle, tex_held, back_full, back_empty, tex_back):
    os.makedirs(PREVIEW, exist_ok=True)
    body = K.body_quads()
    S = 640
    for tag, node in (("plein", back_full), ("vide", back_empty)):
        quads = K.attach(node, tex_back)
        views = ((180, 6), (215, 14), (90, 4), (0, 6))
        sheet = [[[40, 44, 54, 255] for _ in range(S * len(views))] for _ in range(S)]
        for i, (yaw, pitch) in enumerate(views):
            frame = K.draw(quads, S, yaw, pitch, solid=body, fit=4.2)
            for y in range(S):
                for x in range(S):
                    if frame[y][x][3]:
                        sheet[y][i * S + x] = frame[y][x]
        png.write(os.path.join(PREVIEW, "observateur_dos_%s.png" % tag), S * len(views), S, sheet)
    face = K.draw(K.quads_of(copy.deepcopy(dict(handle, position=K.vec((0, 0, 0)), orientation=dict(K.ID_Q))), (0, 0, 0), K.ID_Q, tex_held, ("\0",)),
                  512, 0, 0, margin=0.04)
    png.write(os.path.join(PREVIEW, "observateur_lame.png"), 512, 512, [[list(p) if p[3] else [40, 44, 54, 255] for p in row] for row in face])


# ---------------------------------------------------------------------------------------------------------------------
def main():
    global M
    out_dir = os.path.join(RES, "Common", MODEL_REL)
    os.makedirs(out_dir, exist_ok=True)

    M = K.Model(NAME)
    M.reuse = True
    root, handle = build_held()
    D.renumber(root)
    fix_coplanar(handle, NAME)
    W, H, tex = M.texture()
    png.write(os.path.join(out_dir, NAME + ".png"), W, H, tex)
    with open(os.path.join(out_dir, NAME + ".blockymodel"), "w") as f:
        json.dump({"nodes": [root], "lod": "auto"}, f, indent=1)
    ic = icon(handle, tex)
    png.write(os.path.join(RES, "Common", "Icons", "ItemsGenerated", NAME + ".png"), 64, 64, ic)
    print("%-34s texture %dx%d" % (NAME, W, H))

    M = K.Model(BACK)                                               # les deux modeles du dos partagent un atlas
    M.reuse = True
    full = build_back(True)
    empty_ = build_back(False)
    for n in (full, empty_):
        D.renumber(n)
        fix_coplanar(n, BACK)
    W2, H2, tex2 = M.texture()
    png.write(os.path.join(out_dir, BACK + ".png"), W2, H2, tex2)
    for nm, n in ((BACK, full), (BACK + "_Vide", empty_)):
        with open(os.path.join(out_dir, nm + ".blockymodel"), "w") as f:
            json.dump({"nodes": [n], "lod": "auto"}, f, indent=1)
    print("%-34s texture %dx%d" % (BACK, W2, H2))

    write_item()
    if PREVIEW:
        previews(handle, tex, full, empty_, tex2)
    print("ok")


if __name__ == "__main__":
    main()
