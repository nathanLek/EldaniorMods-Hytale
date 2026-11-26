"""Armes divines (une par famille d'arme) : modeles, textures, icones, traines, items, signatures, assets des sorts.

Chaque arme reprend la hierarchie du modele vanilla de sa famille (R-Attachment > Origin_Projectile > Origin_Item > Handle,
memes positions et orientation du Handle) : les animations du jeu la tiennent donc comme l'arme vanilla. Seuls la forme
du Handle (la poignee) et ses enfants sont remplaces. Les enfants sont places par rapport au CENTRE de la forme du Handle,
comme dans le vanilla (les positions des enfants vanilla sont directement reutilisables). Pas d'offset de forme sur les
boites etirees (4 px par unite) : des pivots vides portent les rotations. Exceptions a 1 px par unite (offsets vanilla
gardes, etirement 1) : les noeuds de l'arc (noms et formes vanilla, dont ARROW-PLACEHOLDER) et la toupie du baton de Ra.

Item : copie de l'arme vanilla de reference (ses champs propres, avec son Parent de template s'il en a un), degats x5
(ligne Divin du catalogue : DIVINE_FACTOR), signature (Ability1) remplacee par le sort Java de l'arme (ArmesDivines.java),
passif dans ArtefactWeapons, reservation de classe dans MasterySystem. Recette : artefact_recipes.py.

Usage : python3 scripts/artefacts/gen_divines.py <src/main/resources> [dossier_apercu] [cle ...]
"""
import copy, json, math, os, sys, tempfile, zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "armures"))
sys.path.insert(0, os.path.join(HERE, "..", "rune_aura_icons"))
import armor_kit as K  # noqa: E402
import png  # noqa: E402
sys.path.insert(0, HERE)
from coplanar import fix_coplanar  # noqa: E402

RES = sys.argv[1]
PREVIEW = sys.argv[2] if len(sys.argv) > 2 and sys.argv[2] != "-" else None
ONLY = set(sys.argv[3:])
K.set_density(4, 512)
Z = zipfile.ZipFile(K.ASSETS)
NAMES = set(Z.namelist())
MODEL_REL = "Items/Weapons/artefacts"
DIVINE_FACTOR = 5.0
ID_Q = K.ID_Q


def zjson(path):
    return json.loads(Z.read(path))


# ---------------------------------------------------------------------------------------------------------------------
# Couleurs et peintures
# ---------------------------------------------------------------------------------------------------------------------
def hexc(h):
    h = h.lstrip("#")
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


GOLD, GOLD_LIGHT, GOLD_DARK = (232, 188, 92), (255, 228, 150), (150, 108, 46)
SILVER, SILVER_DARK = (206, 212, 224), (120, 126, 140)
BRONZE = (184, 120, 64)
BLACK = (18, 16, 22)


def mixc(a, b, t):
    return K.mix(a, b, t)


def bevel(img, strength=1.0):
    """Biseau peint : arete du haut et de gauche eclaircies, du bas et de droite ombrees (relief des petites pieces)."""
    h, w = len(img), len(img[0])
    if min(w, h) < 5:
        return img
    for y in range(h):
        for x in range(w):
            k = 1.0
            if y == 0: k += 0.16 * strength
            elif y == h - 1: k -= 0.2 * strength
            if x == 0: k += 0.06 * strength
            elif x == w - 1: k -= 0.1 * strength
            if k != 1.0:
                img[y][x][:3] = [min(255, ch * k) for ch in img[y][x][:3]]
    return img


def metal(c, sheen=0.22, edge=None, finish=True):
    """Metal brosse : lumiere douce du haut vers le bas, reflet large et flou, fins traits de brossage dans la longueur
    (jamais de motif en diagonale), grain a peine visible. edge = filet de couleur sur le bord des grandes faces."""
    def paint(face, w, h, seed):
        img = K.canvas(w, h, c)
        for y in range(h):
            v = (y + 0.5) / h
            band = math.exp(-((v - 0.32) / 0.2) ** 2) * sheen * 0.8          # reflet large
            for x in range(w):
                brushed = 0.025 * (K.noise(x * 0.04 + seed, y * 1.7) - 0.5) * 2
                grain = 0.03 * (K.fbm(x * 0.15 + seed * 3, y * 0.15) - 0.5)
                k = 1.06 - 0.14 * v + band + brushed + grain
                img[y][x][:3] = [ch * k for ch in c]
        if min(w, h) >= 8:                                              # usure : fines rayures claires, coins un peu sombres
            rng = __import__("random").Random(seed * 7 + w * 3 + h)
            light = [min(255, ch * 1.22) for ch in c]
            for _ in range(max(1, w * h // 900)):
                x0, y0 = rng.uniform(1, w - 2), rng.uniform(1, h - 2)
                L = rng.uniform(2, 5)
                a = rng.uniform(-0.4, 0.4)
                K.stroke(img, [(x0, y0), (x0 + math.cos(a) * L, y0 + math.sin(a) * L)], light, 0.3)
            for y in range(h):
                for x in range(w):
                    d = min(x, y, w - 1 - x, h - 1 - y)
                    if d < 2 and min(x, w - 1 - x) < 2 and min(y, h - 1 - y) < 2:
                        img[y][x][:3] = [ch * 0.85 for ch in img[y][x][:3]]
        if edge and min(w, h) > 6:
            for y in range(h):
                for x in range(w):
                    if min(x, y, w - 1 - x, h - 1 - y) < 1:
                        img[y][x][:3] = list(edge)
        return bevel(K.finish(img, face, edges=False)) if finish else img
    return paint


def glow(c, core=None):
    """Matiere lumineuse (shading fullbright) : coeur plus clair."""
    core = core or mixc(c, (255, 255, 255), 0.55)

    def paint(face, w, h, seed):
        img = K.canvas(w, h, c)
        for y in range(h):
            for x in range(w):
                d = max(abs((x + 0.5) / w - 0.5), abs((y + 0.5) / h - 0.5)) * 2
                img[y][x][:3] = list(mixc(core, c, d))
        return img
    return paint


def gem(c):
    """Gemme taillee : table claire au centre, facettes en couronne (aretes nettes), reflet blanc en haut a gauche."""
    def paint(face, w, h, seed):
        img = K.canvas(w, h, c)
        cx, cy = w / 2.0, h / 2.0
        for y in range(h):
            for x in range(w):
                u, v = (x + 0.5 - cx) / (w / 2.0), (y + 0.5 - cy) / (h / 2.0)
                r = max(abs(u), abs(v))
                ang = math.atan2(v, u)
                facet = int((ang + math.pi) / (math.pi / 4)) % 2
                if r < 0.45:
                    col = mixc(c, (255, 255, 255), 0.35)
                else:
                    col = mixc(c, (0, 0, 0), 0.15 + 0.2 * facet) if u + v > 0 else mixc(c, (255, 255, 255), 0.1 + 0.15 * facet)
                if abs(r - 0.45) < 1.2 / max(w, h):
                    col = mixc(c, (255, 255, 255), 0.55)
                img[y][x][:3] = list(col)
        if w >= 4 and h >= 4:
            K.put(img, int(w * 0.25), int(h * 0.25), (255, 255, 255))
        return img
    return paint


def wood(c, grain=0.18):
    """Bois : veines continues dans la longueur, noeuds sombres epars, legers cernes."""
    def paint(face, w, h, seed):
        img = K.canvas(w, h, c)
        for y in range(h):
            for x in range(w):
                vein = K.fbm(x * 0.03 + seed, y * 0.45 + 0.3 * math.sin(x * 0.05 + seed))
                line = 0.5 + 0.5 * math.sin(vein * 14)
                k = 1 + grain * (line - 0.5) * 1.3 + 0.05 * (K.noise(x * 0.3, y * 0.3 + seed) - 0.5)
                img[y][x][:3] = [ch * k for ch in c]
        rng = __import__("random").Random(seed)
        for _ in range(max(0, w // 40)):
            K.disc(img, rng.uniform(3, w - 3), rng.uniform(1, h - 1), min(2.2, h / 4.0), [ch * 0.6 for ch in c], 0.8)
        return K.finish(img, face, edges=False)
    return paint


def wrap(c1, c2, period=5):
    """Poignee tressee : bandes croisees en losanges, ombrees aux croisements, reflet sur chaque bande."""
    def paint(face, w, h, seed):
        img = K.canvas(w, h, c1)
        p = float(period * 2)
        for y in range(h):
            for x in range(w):
                a = ((x + y) % p) / p
                b = ((x - y) % p) / p
                over = a < 0.5
                t = a * 2 if over else b * 2 - (0 if b < 0.5 else 1)
                t = (a * 2) % 1.0 if over else (b * 2) % 1.0
                c = c2 if over else c1
                k = 0.78 + 0.32 * math.sin(math.pi * t)                     # chaque bande est bombee
                img[y][x][:3] = [ch * k for ch in c]
                if over and abs(t - 0.5) < 0.08 and (x + y) % 3 == 0:          # point de couture au milieu de la bande
                    img[y][x][:3] = [min(255, ch * 1.35) for ch in c2]
        return K.finish(img, face, edges=False)
    return paint


def runes(base, line, step=7):
    """Metal grave : runes en creux (trait sombre souligne d'un liseré clair en dessous) le long des grandes faces."""
    m = metal(base, finish=False)
    light = mixc(base, (255, 255, 255), 0.45)

    def paint(face, w, h, seed):
        img = m(face, w, h, seed)
        if min(w, h) >= 20:                                             # grande face : volutes de filigrane
            for x0 in range(int(h * 0.5), w - int(h * 0.5) + 1, max(8, int(h * 0.9))):
                cx, cy = x0, h / 2.0
                for sgn in (1, -1):
                    pts = [(cx + sgn * math.cos(t) * t * h * 0.045, cy + math.sin(t) * t * h * 0.045) for t in [i * 0.35 for i in range(18)]]
                    K.stroke(img, [(px, py + 0.7) for px, py in pts], light, 0.35)
                    K.stroke(img, pts, line, 0.4)
            return bevel(K.finish(img, face, edges=False))
        if face in ("front", "back", "top", "bottom") and h >= 8:
            for i, x0 in enumerate(range(3, w - 3, step)):
                cy = h // 2
                pattern = [((0, -2), (0, 2)), ((-1, -2), (1, 2)), ((-1, 0), (1, 0)), ((1, -2), (-1, 2))][(i + seed) % 4]
                a = (x0 + pattern[0][0], cy + pattern[0][1])
                b = (x0 + pattern[1][0], cy + pattern[1][1])
                K.stroke(img, [(a[0], a[1] + 0.8), (b[0], b[1] + 0.8)], light, 0.4)
                K.stroke(img, [a, b], line, 0.45)
                K.put(img, x0, cy - 3, line)
        return bevel(K.finish(img, face, edges=False))
    return paint


def blade(base, edge, fuller=None, sheen=0.25):
    """Lame a double tranchant : arete mediane, biseaux qui s'eclaircissent vers le fil, fil poli d'un pixel, gouttiere
    (fuller) sombre au centre ; brossage tres fin dans la longueur (pas de motif en diagonale)."""
    def paint(face, w, h, seed):
        img = K.canvas(w, h, base)
        for y in range(h):
            v = abs((y + 0.5) / h - 0.5) * 2                                  # 0 au centre, 1 au fil
            if v < 0.14:
                c = mixc(base, (0, 0, 0), 0.22) if fuller is None else mixc(fuller, base, 0.25)
            elif v < 0.2:
                c = mixc(base, (255, 255, 255), 0.25)                        # liseré de la gouttiere
            else:
                t = (v - 0.2) / 0.8
                c = mixc(base, edge, t ** 1.4)
            if v > 1 - 1.5 / h:
                c = mixc(edge, (255, 255, 255), 0.5)
            for x in range(w):
                k = 1 + 0.02 * (K.noise(x * 0.05 + seed, y * 2.3) - 0.5) * 2 + 0.06 * math.exp(-((y + 0.5) / h - 0.3) ** 2 / 0.02)
                img[y][x][:3] = [ch * k for ch in c]
        return img
    return paint


def stripes(c1, c2, period=8, width=2):
    """Bandes nettes mais adoucies (bord d'un demi-pixel ombre), sur fond de metal brosse."""
    base = metal(c1, 0.15, finish=False)

    def paint(face, w, h, seed):
        img = base(face, w, h, seed)
        for y in range(h):
            for x in range(w):
                m = x % period
                if m < width:
                    k = 0.85 if m in (0, width - 1) and width > 2 else 1.0
                    img[y][x][:3] = [ch * k for ch in c2]
                elif m == width:
                    img[y][x][:3] = [ch * 0.82 for ch in img[y][x][:3]]
        return K.finish(img, face, edges=False)
    return paint


def inlay(base, stone, border=None):
    """Or incruste : bande de pierre (lapis...) au centre, filets d'or sombre de part et d'autre, filigrane."""
    m = runes(base, mixc(base, (0, 0, 0), 0.45), 9)
    border = border or mixc(base, (0, 0, 0), 0.35)

    def paint(face, w, h, seed):
        img = m(face, w, h, seed)
        if h >= 6 and face in ("front", "back", "left", "right"):
            band = max(1, h // 5)
            y0 = h // 2 - band // 2
            for y in range(y0, y0 + band):
                for x in range(1, w - 1):
                    t = 0.9 + 0.2 * K.noise(x * 0.4 + seed, y * 0.6)
                    img[y][x][:3] = [ch * t for ch in stone]
            for x in range(w):
                for y in (y0 - 1, y0 + band):
                    if 0 <= y < h:
                        img[y][x][:3] = list(border)
        return img
    return paint


def feather(base, tip):
    """Plume : degrade vers la pointe, rachis central clair, barbes obliques."""
    def paint(face, w, h, seed):
        img = K.canvas(w, h, base)
        for y in range(h):
            for x in range(w):
                t = (x + 0.5) / w
                c = mixc(base, tip, max(0.0, (t - 0.55) / 0.45))
                barb = 0.92 if ((x + abs(y - h / 2.0) * 1.5) % 3) < 1 else 1.0
                img[y][x][:3] = [ch * barb for ch in c]
            if h >= 3:
                pass
        mid = h // 2
        for x in range(w):
            img[mid][x][:3] = [min(255, ch * 1.25) for ch in img[mid][x][:3]]
        return bevel(K.finish(img, face, edges=False), 0.6)
    return paint


def fur(c):
    def paint(face, w, h, seed):
        img = K.canvas(w, h, c)
        for y in range(h):
            for x in range(w):
                k = 0.8 + 0.4 * K.noise(x * 0.9 + seed, y * 0.25)
                img[y][x][:3] = [v * k for v in c]
        return K.finish(img, face, edges=False)
    return paint


def disc_face(base, rim, center, motif=None):
    """Grande face ronde (bouclier, disque solaire) : fond, anneaux, motif central. Faces laterales gauche/droite."""
    def paint(face, w, h, seed):
        img = K.canvas(w, h, base)
        cx, cy = w / 2.0, h / 2.0
        for y in range(h):
            for x in range(w):
                r = math.hypot((x + 0.5 - cx) / (w / 2.0), (y + 0.5 - cy) / (h / 2.0))
                c = list(img[y][x][:3])
                if r > 0.88:
                    c = list(rim)
                elif 0.62 < r < 0.68:
                    c = list(rim)
                elif r < 0.3:
                    c = list(center)
                img[y][x][:3] = c
        if motif:
            motif(img, w, h)
        return K.finish(img, face, edges=False)
    return paint


# ---------------------------------------------------------------------------------------------------------------------
# Construction : modele courant, boites, pivots
# ---------------------------------------------------------------------------------------------------------------------
M = None


def qz(d):
    return K.q_axis((0, 0, 1), d)


def qx(d):
    return K.q_axis((1, 0, 0), d)


def qy(d):
    return K.q_axis((0, 1, 0), d)


def snap(v):
    return round(v * 4) / 4.0


def box(name, pos, size, paint, q=ID_Q, shading="flat"):
    keep(paint)
    return K.box(M, name, pos, tuple(max(0.25, snap(s)) for s in size), paint, q=q, shading=shading)


def keep(paint):
    """Le cache de textures de armor_kit est indexe par id(peinture) : une peinture liberee peut laisser son id a une
    autre, qui recevrait alors la texture de la premiere. On garde donc chaque peinture en vie le temps du modele."""
    if not hasattr(M, "keep"):
        M.keep = []
    M.keep.append(paint)


def lit(name, pos, size, paint, q=ID_Q):
    return box(name, pos, size, paint, q=q, shading="fullbright")


def pivot(name, pos, q=ID_Q, children=()):
    return {"id": M.nid(), "name": name, "position": K.vec(pos), "orientation": K.round_q(q), "shape": empty(), "children": list(children)}


def empty(piece=False):
    return {"type": "none", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": 1, "y": 1, "z": 1}, "settings": {"isPiece": piece},
            "textureLayout": {}, "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": "flat"}


def seg(name, pos, size, paint, q=ID_Q, shading="flat", children=()):
    """Segment qui tourne autour de son debut : pivot (q) + boite a mi-longueur ; enfants places depuis le debut."""
    p = pivot(name, pos, q)
    p["children"].append(box(name + "_Box", (size[0] / 2.0, 0, 0), size, paint, shading=shading))
    p["children"] += list(children)
    return p


def chain(name, pos, parts, q0=ID_Q, overlap=0.5):
    """Suite de segments emboites : parts = [(longueur, largeur, epaisseur, courbure, peinture, shading), ...]."""
    node = None
    for i in reversed(range(len(parts))):
        L, W, T, bend, paint, shading = parts[i]
        n = seg("%s_%d" % (name, i), (0, 0, 0), (L, W, T), paint, q=qz(bend), shading=shading)
        if node is not None:
            node["position"] = K.vec((L - overlap, 0, 0))
            n["children"].append(node)
        node = n
    top = pivot(name, pos, q0, [node])
    return top


def last_seg(root, prefix):
    """Dernier segment d'une chaine (pivot), pour y accrocher une pointe."""
    node = root["children"][0]
    while True:
        nxt = [c for c in node["children"] if c["name"].startswith(prefix + "_") and not c["name"].endswith("_Box")]
        if not nxt:
            return node
        node = nxt[0]


def fan(name, center, base_angle, spread, n, length, width, thick, paint, edge_paint=None, edge_glow=True):
    """Fer en eventail (hache) : n lames rayonnant de center autour de base_angle (+-spread), et un tranchant en arc
    fait de petites boites lumineuses tangentes au bout des lames."""
    out = []
    for i in range(n):
        a = base_angle - spread + 2 * spread * i / max(1, n - 1)
        out.append(seg("%s_%d" % (name, i), center, (length, width, thick - 0.25 * (i % 2)), paint, q=qz(a)))
    if edge_paint:
        steps = n * 3
        for k in range(steps + 1):
            a = base_angle - spread - 6 + (2 * spread + 12) * k / steps
            r = length - 0.4
            p = (center[0] + math.cos(math.radians(a)) * r, center[1] + math.sin(math.radians(a)) * r, 0)
            chord = 2 * r * math.sin(math.radians((2 * spread + 12) / steps / 2)) + 0.5
            out.append(box("%s_Edge_%d" % (name, k), p, (1.5, chord, thick + 0.5), edge_paint, q=qz(a), shading="fullbright" if edge_glow else "flat"))
    return out


def helix(name, x0, x1, r, step_deg, pitch, size, paint, phase=0.0, shading="flat"):
    """Spirale continue autour de l'axe x (fil d'or, serpent enroule, dragon...) : un tube fait de segments qui se
    suivent de point en point (pas de petits blocs separes)."""
    keep(paint)
    pts = []
    a, x = phase, x0
    while x <= x1 + 1e-6:
        rad = math.radians(a)
        pts.append((x, math.cos(rad) * r, math.sin(rad) * r))
        a += step_deg
        x += pitch
    thick = max(0.5, snap(min(size[1], size[2]) if len(size) > 2 else size[1]))
    out = []
    for i in range(len(pts) - 1):
        a_, b_ = pts[i], pts[i + 1]
        d = tuple(b_[k] - a_[k] for k in range(3))
        L = math.sqrt(sum(c * c for c in d)) + thick * 0.6               # recouvrement : le tube ne se casse pas
        mid = tuple((a_[k] + b_[k]) / 2.0 for k in range(3))
        out.append(K.box(M, "%s_%d" % (name, i), mid, (thick, snap(L), thick), paint, q=K.q_from_y(d), shading=shading))
    return out

def ring_x(name, x, r, thick, paint, n=8, shading="flat"):
    """Anneau autour de l'axe x (poignee) : 2 boites croisees a 45 degres (octogone)."""
    return [box("%s_a" % name, (x, 0, 0), (thick, r * 2, r * 2), paint, shading=shading),
            box("%s_b" % name, (x, 0, 0), (thick - 0.25, r * 2, r * 2), paint, q=qx(45), shading=shading)]


def taper_quad(name, pos, length, width, paint_img_fn, q=ID_Q):
    """Pointe detouree (quad) : paint_img_fn(w, h) -> image RGBA."""
    w, h = int(round(length * K.D)), int(round(width * K.D))
    p = pivot(name, pos, q)
    p["children"].append(K.quad(M, name + "_Quad", (length / 2.0, 0, 0), paint_img_fn(w, h)))
    return p


def point_img(base, edge, sym=True):
    def fn(w, h):
        img = [[[0, 0, 0, 0] for _ in range(w)] for _ in range(h)]
        for y in range(h):
            for x in range(w):
                t = (x + 0.5) / w
                v = (y + 0.5) / h
                half = 0.5 * (1 - t ** 1.15)
                if sym and abs(v - 0.5) > half:
                    continue
                if not sym and (v < 0.0 or v > 1 - (t ** 1.6)):
                    continue
                d = abs(v - 0.5) / max(half, 1e-3) if sym else 0
                c = mixc(base, edge, max(0.0, (d - 0.5) * 2))
                img[y][x] = [int(c[0]), int(c[1]), int(c[2]), 255]
        return img
    return fn


# ---------------------------------------------------------------------------------------------------------------------
# Hierarchie vanilla
# ---------------------------------------------------------------------------------------------------------------------
def vanilla_chain(model_path):
    """Racine copiee du modele vanilla jusqu'au Handle (seul chemin garde) ; renvoie (racine, handle)."""
    m = zjson("Common/" + model_path)
    root = copy.deepcopy(m["nodes"][0])

    def find(n):
        if n["name"] == "Handle":
            return [n]
        for c in n.get("children", []):
            p = find(c)
            if p:
                return [n] + p
        return None
    path = find(root)
    for parent, child in zip(path, path[1:]):
        parent["children"] = [child]
    return root, path[-1]


def mount(model_path, grip_size, grip_paint, kids, grip_shading="flat"):
    """Remplace la poignee vanilla : meme centre de forme (pivot deplace de l'offset), nos enfants autour."""
    root, handle = vanilla_chain(model_path)
    off = handle["shape"].get("offset", {"x": 0, "y": 0, "z": 0})
    shift = K.qrot(handle["orientation"], (off["x"], off["y"], off["z"]))
    grip = box("Handle", (handle["position"]["x"] + shift[0], handle["position"]["y"] + shift[1], handle["position"]["z"] + shift[2]),
               grip_size, grip_paint, q=handle["orientation"], shading=grip_shading)
    grip["children"] = list(kids)
    handle.clear()
    handle.update(grip)
    return root, handle


def reskin(node, paint_of, keep=()):
    """Copie d'un sous-arbre vanilla a 1 px par unite (offsets et etirements vanilla valables), peint a notre facon."""
    d_saved = K.D
    K.D = 1
    try:
        def rec(n):
            s = n.get("shape", {})
            st = s.get("settings", {})
            paint, shading = paint_of(n["name"])
            globals()["keep"](paint)
            if s.get("type") == "box":
                sz = st["size"]
                out = K.box(M, n["name"], (n["position"]["x"], n["position"]["y"], n["position"]["z"]), (sz["x"], sz["y"], sz["z"]),
                            paint, q=n.get("orientation") or ID_Q, shading=shading)
            elif s.get("type") == "quad":
                sz = st["size"]
                img = paint("front", int(sz["x"]), int(sz["y"]), 0)
                out = K.quad(M, n["name"], (n["position"]["x"], n["position"]["y"], n["position"]["z"]), img, q=n.get("orientation") or ID_Q)
                out["shape"]["settings"]["normal"] = st.get("normal", "+Z")
                out["shape"]["shadingMode"] = shading
            else:
                out = {"id": M.nid(), "name": n["name"], "position": dict(n["position"]), "orientation": n.get("orientation") or dict(ID_Q),
                       "shape": copy.deepcopy(s) if s else empty(), "children": []}
                out["shape"].setdefault("settings", {})
            if s.get("offset"):
                out["shape"]["offset"] = dict(s["offset"])
            out["children"] = [rec(c) for c in n.get("children", [])]
            return out
        return rec(node)
    finally:
        K.D = d_saved


def vanilla_handle_children(model_path):
    m = zjson("Common/" + model_path)
    found = []

    def walk(n):
        if n["name"] == "Handle":
            found.append(n)
        for c in n.get("children", []):
            walk(c)
    walk(m["nodes"][0])
    return found[0]


def renumber(root):
    ids = iter(range(1, 100000))

    def walk(n):
        n["id"] = str(next(ids))
        n.setdefault("children", [])
        for c in n["children"]:
            walk(c)
    walk(root)


# ---------------------------------------------------------------------------------------------------------------------
# Les armes. Chaque fonction renvoie (racine, handle). Coordonnees : x le long de la poignee depuis son centre.
# ---------------------------------------------------------------------------------------------------------------------
LAPIS, LAPIS_DARK = (34, 62, 156), (18, 30, 86)
SUN, SUN_CORE = (255, 150, 30), (255, 238, 170)


def build_ra():
    """Ouas de Ra (baton magique) : hampe de lapis-lazuli baguee d'or, ailes de faucon, disque solaire rayonnant ;
    la toupie vanilla (Spin1 et ses 4 attaches, animee par SpinningFire4) porte 4 petits soleils."""
    kids = []
    for i, x in enumerate((-30, -12, 8, 26)):
        kids += ring_x("Band_%d" % i, x, 2.75, 2.0, metal(GOLD))
    kids.append(box("Ferrule", (-44, 0, 0), (5, 5.5, 5.5), metal(GOLD)))
    for sgn in (1, -1):                                                # fourche du sceptre ouas
        kids.append(chain("Fork_%s" % ("P" if sgn > 0 else "N"), (-46, 1.2 * sgn, 0),
                          [(5, 2, 2, 0, metal(GOLD), "flat"), (3, 1.5, 1.5, 30 * sgn, metal(GOLD_LIGHT), "flat")], q0=qz(180 - 22 * sgn)))
    kids += helix("Wire", -38, 40, 2.6, 40, 2.5, (1.75, 0.75, 1.0), metal(GOLD))
    kids += ring_x("Collar", 46, 3.75, 4.0, metal(GOLD))
    for s in (1, -1):                                                  # ailes de faucon, de part et d'autre
        for k, (L, W, a) in enumerate(((16, 4.5, 58), (13, 3.5, 74), (10, 2.75, 90))):
            ang = a * s
            kids.append(seg("Wing_%s_%d" % ("P" if s > 0 else "N", k), (48, 2.0 * s, 0), (L, W, 2.0 - 0.25 * k),
                            feather(GOLD, LAPIS), q=qz(ang)))
    kids.append(box("Neck", (53, 0, 0), (6, 4.5, 4.5), metal(GOLD)))
    kids.append(lit("Sun", (64, 0, 0), (18, 18, 2.5), glow(SUN, SUN_CORE)))
    kids.append(lit("Sun_B", (64, 0, 0), (18, 18, 2.25), glow(SUN, SUN_CORE), q=qz(45)))
    for k in range(8):                                                # rayons
        a = k * 45 + 22.5
        d = (math.cos(math.radians(a)), math.sin(math.radians(a)))
        kids.append(lit("Ray_%d" % k, (64 + d[0] * 13, d[1] * 13, 0), (7, 1.5, 1.5), glow(SUN_CORE, (255, 255, 230)), q=qz(a)))
    kids.append(box("Eye", (64, 0, 0), (5, 5, 3.5), gem((200, 20, 30))))
    for k in range(12):                                                # sertissure d'or autour du disque solaire
        a = k * 30
        d = (math.cos(math.radians(a)), math.sin(math.radians(a)))
        kids.append(box("Sun_Rim_%d" % k, (64 + d[0] * 9.5, d[1] * 9.5, 0), (1.5, 4.75, 3.25), metal(GOLD), q=qz(a)))
    for k in range(4):
        a = math.radians(k * 90 + 45)
        kids.append(box("Collar_Gem_%d" % k, (46, math.cos(a) * 4.1, math.sin(a) * 4.1), (2, 1.75, 1.75), gem(LAPIS), q=qx(k * 90 + 45)))
    for sgn in (1, -1):                                                # cobras dressees (uraeus) de part et d'autre du soleil
        tag = "P" if sgn > 0 else "N"
        kids.append(chain("Cobra_%s" % tag, (54, 5 * sgn, 0),
                          [(5, 2, 2, 0, metal((60, 140, 90)), "flat"), (4, 1.75, 1.75, -25 * sgn, metal((60, 140, 90)), "flat"),
                           (3, 3.5, 1.5, -20 * sgn, metal(GOLD), "flat")], q0=qz(70 * sgn)))
    # toupie vanilla : memes noms et positions (l'animation de l'objet la fait tourner)
    van = vanilla_handle_children("Items/Weapons/Staff/Crystal_Flame.blockymodel")
    spin = [c for c in van["children"] if c["name"] == "Spin1"][0]
    kids.append(reskin(spin, lambda n: (glow(SUN, SUN_CORE), "fullbright")))
    return mount("Items/Weapons/Staff/Crystal_Flame.blockymodel", (84, 4.5, 4.5), stripes(LAPIS, LAPIS_DARK, 10, 3), kids)


AMETHYST = (150, 60, 220)


def build_circe():
    """Baguette de Circe : ebene et argent, deux serpents d'argent enroules, cristal d'amethyste a la pointe."""
    kids = [box("Pommel", (-11, 0, 0), (3.5, 5, 5), metal(SILVER)),
            box("Pommel_Gem", (-13.25, 0, 0), (1.25, 2.5, 2.5), gem(AMETHYST)),
            box("Shaft", (18, 0, 0), (16, 2.75, 2.75), wood((40, 30, 46)))]
    kids += helix("Serpent", 4, 25, 1.75, 22, 0.6, (1.0, 0.9, 1.3), metal(SILVER))      # serpent enroule (spirale serree)
    kids.append(box("Snake_Head", (26.5, 2.4, 0), (3, 1.75, 2.25), metal(SILVER_DARK), q=qz(25)))
    for sgn in (1, -1):
        kids.append(lit("Snake_Eye_%d" % sgn, (27.2, 3.0, 0.9 * sgn), (0.75, 0.6, 0.5), glow((255, 60, 60), (255, 200, 200))))
    kids.append(box("Cradle", (27.5, 0, 0), (2, 4.5, 4.5), metal(SILVER)))
    kids += ring_x("Grip_Ring_A", -8.5, 2.1, 1.0, metal(SILVER))
    kids += ring_x("Grip_Ring_B", 8.5, 2.1, 1.0, metal(SILVER))
    for k in range(4):                                                 # griffes d'argent qui tiennent le cristal
        a = k * 90 + 45
        d = (math.cos(math.radians(a)), math.sin(math.radians(a)))
        kids.append(pivot("Prong_%d" % k, (28.5, d[0] * 1.6, d[1] * 1.6), K.qmul(qx(a), qz(20)),
                          [box("Prong_%d_Box" % k, (2.5, 0, 0), (5, 0.9, 0.9), metal(SILVER))]))
    kids.append(lit("Crystal", (32.5, 0, 0), (7, 3.75, 3.75), glow(AMETHYST, (240, 200, 255)), q=qx(45)))
    kids.append(lit("Crystal_Tip", (37, 0, 0), (3, 2.25, 2.25), glow(AMETHYST, (255, 230, 255)), q=qx(45)))
    for sgn in (1, -1):
        kids.append(seg("Leaf_%d" % sgn, (-6, 1.2 * sgn, 0), (4, 1.75, 0.5), metal((70, 140, 70)), q=qz(150 * sgn)))
    return mount("Items/Weapons/Wand/Wood.blockymodel", (18, 3.5, 3.5), wrap((26, 20, 30), (90, 90, 110), 4), kids)


def medusa(img, w, h):
    cx, cy = w / 2.0, h / 2.0
    r = min(w, h) * 0.13
    for k in range(10):                                               # serpents de la chevelure
        a = k * 36
        pts = []
        for i in range(8):
            t = i / 7.0
            rr = r * (1.0 + 1.6 * t)
            aa = math.radians(a + 25 * math.sin(t * 6))
            pts.append((cx + math.cos(aa) * rr, cy + math.sin(aa) * rr))
        K.stroke(img, pts, (60, 120, 70), 0.9)
    K.disc(img, cx, cy, r, (226, 200, 150))
    K.disc(img, cx - r * 0.4, cy - r * 0.15, r * 0.18, (220, 20, 30))
    K.disc(img, cx + r * 0.4, cy - r * 0.15, r * 0.18, (220, 20, 30))
    K.stroke(img, [(cx - r * 0.35, cy + r * 0.45), (cx + r * 0.35, cy + r * 0.45)], (90, 40, 40), 0.6)


def meander(img, w, h):
    """Frise grecque entre les deux anneaux d'or."""
    cx, cy = w / 2.0, h / 2.0
    for k in range(48):
        a = math.radians(k * 7.5)
        r1, r2 = 0.72 * w / 2, 0.84 * w / 2
        p1 = (cx + math.cos(a) * r1, cy + math.sin(a) * r1)
        p2 = (cx + math.cos(a) * r2, cy + math.sin(a) * r2)
        if k % 2 == 0:
            K.stroke(img, [p1, p2], GOLD_DARK, 0.5)




ICE, ICE_CORE = (110, 210, 255), (225, 250, 255)


def build_skadi():
    """Hache de Skadi : manche de bouleau blanc bague d'argent, fer d'acier de glace a barbe, tranchant de glace
    lumineux, eperon et couronne de cristaux de glace."""
    steel = (176, 200, 222)
    kids = [box("Pommel", (-23, 0, 0), (6, 6.5, 6.5), metal(SILVER)),
            lit("Pommel_Ice", (-27, 0, 0), (3, 3.5, 3.5), glow(ICE, ICE_CORE), q=qx(45))]
    for i, x in enumerate((-12, 2, 16)):
        kids += ring_x("Band_%d" % i, x, 3.0, 1.5, metal(SILVER))
    kids.append(box("Socket", (27, 0, 0), (12, 7.5, 7.5), runes((150, 170, 190), ICE, 5)))
    kids += rivets("Socket_Rivet", [(x, 0, z) for x in (23, 31) for z in (3.9, -3.9)], 1.25, metal(SILVER))
    for sgn in (1, -1):                                                # runes de givre sur les joues de la douille
        kids.append(lit("Frost_Rune_%d" % sgn, (27, 1, 4.0 * sgn), (6, 0.75, 0.5), glow(ICE, ICE_CORE), q=qz(30)))
        kids.append(lit("Frost_Rune_B_%d" % sgn, (27, 1, 4.0 * sgn), (6, 0.75, 0.75), glow(ICE, ICE_CORE), q=qz(-30)))
    kids += fan("Blade", (27, -2, 0), -95, 32, 5, 19, 7, 2.5, metal(steel), glow(ICE, ICE_CORE))
    kids.append(box("Grip", (-11, 0, 0), (14, 5.75, 5.75), wrap((60, 70, 90), (140, 160, 190), 4)))
    for k, (x, a, L) in enumerate(((19, 30, 4), (22, -35, 3.5), (33, 60, 4), (35, -50, 3))):   # givre qui pousse sur le fer
        kids.append(lit("Rime_%d" % k, (x, 3.8, 0), (1.25, L, 1.25), glow(ICE, ICE_CORE), q=qz(a)))
    kids.append(lit("Spur", (27, 8, 0), (3, 9, 2.5), glow(ICE, ICE_CORE)))
    kids.append(lit("Spur_Tip", (29, 14, 0), (2, 5, 2), glow(ICE, ICE_CORE), q=qz(-25)))
    for k, (dy, h, a) in enumerate(((0, 9, 0), (2.5, 6, -25), (-2.5, 6, 25))):
        kids.append(lit("Crown_%d" % k, (34 + h / 2.0 - 1, dy, 0), (h, 2.5, 2.5), glow(ICE, ICE_CORE), q=qz(a)))
    return mount("Items/Weapons/Axe/Iron.blockymodel", (38, 5, 5), wood((226, 222, 210), 0.12), kids)


BLOOD = (170, 20, 26)



SOUL, SOUL_CORE = (120, 255, 190), (230, 255, 240)


def build_thanatos():
    """Faux de Thanatos : longue hampe d'os noir cerclee d'argent, crane au sommet, grande lame noire recourbee
    au fil spectral vert, pointe d'argent au talon."""
    bone, dark = (40, 38, 46), (14, 12, 18)
    kids = [box("Spike", (-50, 0, 0), (8, 3, 3), metal(SILVER)),
            box("Butt", (-45, 0, 0), (4, 5.5, 5.5), metal(SILVER_DARK))]
    vert = (214, 206, 188)
    for i, x in enumerate(range(-36, 40, 8)):                           # hampe en colonne vertebrale
        kids.append(box("Vert_%d" % i, (x, 0, 0), (3, 6.5, 6.5), metal(vert, 0.1)))
        kids.append(box("Vert_%d_B" % i, (x, 0, 0), (2.75, 6.5, 6.5), metal(vert, 0.1), q=qx(45)))
        kids.append(box("Vert_%d_Spine" % i, (x, 4, 0), (2.25, 3, 1.5), metal(vert, 0.1), q=qz(-20)))
    skull = (226, 220, 206)

    def skull_paint(face, w, h, seed):
        img = K.canvas(w, h, skull)
        if face in ("front", "back", "left", "right"):
            K.disc(img, w * 0.3, h * 0.4, w * 0.13, (10, 10, 12))
            K.disc(img, w * 0.7, h * 0.4, w * 0.13, (10, 10, 12))
            K.disc(img, w * 0.3, h * 0.4, w * 0.04, SOUL)
            K.disc(img, w * 0.7, h * 0.4, w * 0.04, SOUL)
            K.rect(img, w * 0.42, h * 0.62, w * 0.58, h * 0.72, (30, 30, 34))
        return K.finish(img, face, edges=False)
    kids.append(box("Skull", (48, 0, 0), (9, 9, 9), skull_paint))
    kids.append(box("Jaw", (47, -5.5, 0), (6, 2.5, 7), metal(skull)))
    for i, z in enumerate((-2.25, -0.75, 0.75, 2.25)):
        kids.append(box("Tooth_%d" % i, (51.75, -4.1, z), (0.75, 1.5, 0.75), metal((246, 242, 230))))
    for i in range(5):                                                 # chaine qui pend sous le crane
        kids.append(box("Link_%d" % i, (44, -7 - i * 2.2, 0), (1.0, 2.5, 1.75 if i % 2 else 1.0), metal(SILVER_DARK), q=qy(90 * (i % 2))))
    for sgn in (1, -1):                                                # lambeaux de linceul
        kids.append(chain("Shroud_%d" % sgn, (40, -3, 2.5 * sgn), [(6, 3, 0.75, 0, metal((40, 44, 40), 0.05), "flat"),
                                                                   (5, 2.25, 0.5, -15, metal((30, 34, 30), 0.05), "flat")], q0=qz(-100)))
    parts = [(14, 8, 2.0, 0, metal(dark), "flat"), (13, 7, 1.75, -14, metal(dark), "flat"), (12, 6, 1.5, -16, metal(dark), "flat"),
             (10, 4.5, 1.25, -18, metal(dark), "flat"), (8, 2.5, 1.0, -22, metal(dark), "flat")]
    b = chain("Blade", (50, -3, 0), parts, q0=qz(-95))
    # fil spectral sur le bord interieur (cote -y local) de chaque segment
    node = b["children"][0]
    i = 0
    while node is not None:
        L, W = parts[i][0], parts[i][1]
        node["children"].append(lit("Edge_%d" % i, (L / 2.0, -W / 2.0, 0), (L, 1.25, parts[i][2] + 0.5), glow(SOUL, SOUL_CORE)))
        nxt = [c for c in node["children"] if c["name"].startswith("Blade_") and not c["name"].endswith("_Box")]
        node = nxt[0] if nxt else None
        i += 1
    kids.append(b)
    kids.append(lit("Soul", (48, 0, 5), (3, 3, 1.5), glow(SOUL, SOUL_CORE)))
    return mount("Items/Weapons/Battleaxe/Iron.blockymodel", (92, 5, 5), metal(bone, 0.12), kids)


STORM, STORM_CORE = (90, 170, 255), (220, 240, 255)




MOON, MOON_CORE = (190, 220, 255), (250, 252, 255)



LACQUER_RED = (176, 30, 30)



def build_heracles():
    """Gourdin d'Heracles : massue d'olivier noueuse, cerclee de bronze, poignee dans la peau du lion de Nemee,
    pommeau en tete de lion d'or."""
    olive = (110, 88, 58)
    kids = [box("Lion", (-22, 0, 0), (6, 6.5, 6.5), metal(GOLD)),
            box("Lion_Snout", (-25.5, -0.8, 0), (2.5, 3.5, 4), metal(GOLD_LIGHT)),
            box("Mane", (-19, 0, 0), (2.5, 9.5, 9.5), fur((200, 150, 60))),
            box("Mane_B", (-19, 0, 0), (2.25, 9.5, 9.5), fur((180, 130, 50)), q=qx(45))]
    for sgn in (1, -1):
        kids.append(box("Lion_Ear_%d" % sgn, (-21, 3.5, 2.5 * sgn), (2, 2, 1.5), metal(GOLD)))
        kids.append(lit("Lion_Eye_%d" % sgn, (-24.6, 1.2, 1.6 * sgn), (0.75, 0.9, 0.9), glow((255, 170, 30), (255, 240, 180))))
    for x, (L, w) in ((11, (14, 8)), (24, (12, 10)), (35, (10, 12))):
        kids.append(box("Body_%d" % x, (x, 0, 0), (L, w, w), wood(olive, 0.25)))
    kids.append(box("Top", (41.5, 0, 0), (3, 10, 10), wood((96, 76, 50), 0.25)))
    for i, x in enumerate((17.5, 30)):
        kids += ring_x("Bronze_%d" % i, x, 5.25 + i, 2.0, metal(BRONZE))
    for k, (x, a) in enumerate(((10, 60), (22, 200), (26, 330), (34, 120), (39, 260), (16, 280))):   # clous de bronze plantes
        r = 4.3 + x / 9.0
        kids.append(box("Stud_%d" % k, (x, math.cos(math.radians(a)) * r, math.sin(math.radians(a)) * r), (1.75, 1.75, 1.75), metal(BRONZE), q=qx(a + 45)))
    for k, (x, a) in enumerate(((8, 0), (14, 120), (21, 240), (27, 60), (33, 180), (38, 300), (12, 300), (36, 90))):
        r = 4.5 + x / 12.0
        d = (math.cos(math.radians(a)), math.sin(math.radians(a)))
        kids.append(box("Knot_%d" % k, (x, d[0] * r, d[1] * r), (3, 3, 3), wood((80, 62, 40), 0.3), q=qx(a)))
    return mount("Items/Weapons/Club/Iron.blockymodel", (38, 5.5, 5.5), fur((196, 146, 64)), kids)


EMERALD = (40, 200, 110)


def build_dagda():
    """Massue du Dagda : enorme tete de chene octogonale, cloutee d'or, triskeles grave ; l'autre bout (celui qui
    ranime) porte une emeraude lumineuse."""
    oak = (92, 64, 36)

    def triskel(face, w, h, seed):
        img = wood(oak, 0.2)(face, w, h, seed)
        if face in ("front", "back", "top", "bottom", "left", "right") and min(w, h) > 20:
            cx, cy = w / 2.0, h / 2.0
            for k in range(3):
                pts = []
                for i in range(14):
                    t = i / 13.0
                    a = math.radians(k * 120 + t * 300)
                    r = t * min(w, h) * 0.3
                    pts.append((cx + math.cos(a) * r, cy + math.sin(a) * r))
                K.stroke(img, pts, GOLD, 0.7)
        return img
    kids = [box("Butt", (-23, 0, 0), (7, 8, 8), wood(oak)),
            lit("Emerald", (-27.5, 0, 0), (3, 4.5, 4.5), glow(EMERALD, (200, 255, 220)), q=qx(45))]
    kids += ring_x("Band_Low", -16, 3.75, 2.0, metal(GOLD))
    kids += ring_x("Band_High", 12, 3.75, 2.0, metal(GOLD))
    kids.append(box("Head", (30, 0, 0), (26, 16, 16), triskel))
    kids.append(box("Head_B", (30, 0, 0), (25.5, 16, 16), triskel, q=qx(45)))
    kids.append(box("Cap", (43.75, 0, 0), (1.5, 12, 12), metal(GOLD)))
    for i, x in enumerate((19, 41)):                                   # cerclages de fer aux spirales d'or
        kids += ring_x("Iron_%d" % i, x, 8.75, 2.5, runes((70, 70, 78), GOLD, 4))
    for k, (x, a) in enumerate(((24, 20), (29, 110), (34, 200), (38, 290), (26, 245), (36, 65))):
        d = (math.cos(math.radians(a)), math.sin(math.radians(a)))
        kids.append(box("Knot_%d" % k, (x, d[0] * 8.6, d[1] * 8.6), (3.5, 2.5, 3.5), wood((70, 48, 26), 0.3), q=qx(a)))
    for sgn in (1, -1):                                                # feuilles autour de l'emeraude (le bout qui ranime)
        kids.append(seg("Leaf_%d" % sgn, (-25, 3 * sgn, 0), (5, 2.5, 0.6), metal((60, 150, 70)), q=qz(180 - 35 * sgn)))
        kids.append(seg("Leaf_Z_%d" % sgn, (-25, 0, 3 * sgn), (5, 0.6, 2.5), metal((50, 130, 60)), q=qy(180 + 35 * sgn)))
    for k in range(8):
        a = k * 45 + 22.5
        d = (math.cos(math.radians(a)), math.sin(math.radians(a)))
        for x in (22, 30, 38):
            kids.append(box("Stud_%d_%d" % (k, x), (x, d[0] * 8.4, d[1] * 8.4), (2.5, 2.5, 2.5), metal(GOLD_LIGHT), q=qx(a)))
    return mount("Items/Weapons/Club/Iron.blockymodel", (38, 6, 6), wrap((40, 70, 40), (90, 60, 34), 6), kids)


SHADOW, SHADOW_CORE = (150, 70, 255), (230, 200, 255)



JADE, JADE_DARK = (60, 190, 120), (20, 110, 70)



def maze(base, line):
    """Bronze grave d'un labyrinthe (carres concentriques ouverts), motif du Minotaure."""
    m = metal(base, finish=False)

    def paint(face, w, h, seed):
        img = m(face, w, h, seed)
        if min(w, h) >= 16:
            cx, cy = w / 2.0, h / 2.0
            for k in range(1, 6):
                r = k * min(w, h) / 12.0
                gap = (k * 37) % 4
                sides = [((cx - r, cy - r), (cx + r, cy - r)), ((cx + r, cy - r), (cx + r, cy + r)),
                         ((cx + r, cy + r), (cx - r, cy + r)), ((cx - r, cy + r), (cx - r, cy - r))]
                for i, (a, b) in enumerate(sides):
                    if i != gap:
                        K.stroke(img, [a, b], line, 0.45)
        return K.finish(img, face, edges=False)
    return paint


def knotwork(base, line):
    """Acier grave d'entrelacs celtiques (ondes croisees)."""
    m = metal(base, finish=False)

    def paint(face, w, h, seed):
        img = m(face, w, h, seed)
        if min(w, h) >= 12:
            for s in (1, -1):
                pts = [(x, h / 2.0 + s * h * 0.28 * math.sin(x * 2 * math.pi / max(12, w / 3.0))) for x in range(0, w + 1, 2)]
                K.stroke(img, pts, line, 0.6)
            K.stroke(img, [(2, 2), (w - 3, 2), (w - 3, h - 3), (2, h - 3), (2, 2)], line, 0.45)
        return K.finish(img, face, edges=False)
    return paint


def gorgon(face, w, h, seed):
    img = K.canvas(w, h, GOLD)
    for y in range(h):
        for x in range(w):
            img[y][x][:3] = list(mixc(GOLD_LIGHT, GOLD_DARK, (x + y) / float(w + h)))
    if face in ("left", "right"):
        medusa(img, w, h)
    return K.finish(img, face, edges=False)


def build_egide():
    """Egide d'Athena v2 : bouclier rond (dodecagone) de bronze, couronne d'or a douze segments, frise grecque, disque
    interieur clouté d'or, Gorgoneion en relief des deux cotes, chevelure de serpents qui sortent du bouclier."""
    bronze, bronze_dark = (178, 118, 60), (116, 72, 36)
    face = disc_face(bronze, GOLD, (150, 98, 50), meander)
    inner = disc_face(bronze_dark, GOLD_DARK, bronze_dark)
    # disque : 6 plaques carrees tournees de 15 degres (24 cotes), bordees par une couronne de 16 segments qui couvre les coins
    plate = [box("Plate_%d" % i, (0, 0, 0), (4 - 0.25 * i, 31, 31), face, q=qx(15 * i)) for i in range(6)]
    plate += [box("Inner_%d" % i, (0, 0, 0), (5.75 - 0.25 * i, 20, 20), inner, q=qx(15 * i + 7.5)) for i in range(6)]
    for k in range(16):
        a = k * 22.5
        d = (math.cos(math.radians(a)), math.sin(math.radians(a)))
        plate.append(box("Rim_%d" % k, (0, d[0] * 18.5, d[1] * 18.5), (6.5, 6.5, 8.25), metal(GOLD), q=qx(a)))
    for k in range(12):
        b = math.radians(k * 30)
        plate.append(box("Stud_%d" % k, (0, math.cos(b) * 12.75, math.sin(b) * 12.75), (7.25, 1.75, 1.75), metal(GOLD_LIGHT), q=qx(45)))
    plate.append(box("Gorgon", (0, 0, 0), (9.5, 9, 8.5), gorgon))
    for side in (1, -1):                                               # relief du visage : front, nez, crocs
        x = side * 5.0
        plate.append(box("Brow_%d" % side, (x, 1.8, 0), (1.25, 1.25, 6), metal(GOLD_DARK)))
        plate.append(box("Nose_%d" % side, (x, 0, 0), (1.25, 2.5, 1.5), metal(GOLD)))
        for z in (-1.6, 1.6):
            plate.append(box("Fang_%d_%s" % (side, z), (x, -2.4, z), (1.0, 1.5, 0.75), metal((240, 236, 220))))
    for k in range(16):
        b = math.radians(k * 22.5 + 11.25)
        for side in (1, -1):
            plate.append(box("Rim_Stud_%d_%d" % (k, side), (side * 3.4, math.cos(b) * 18.5, math.sin(b) * 18.5), (1.0, 1.75, 1.75), metal(GOLD_LIGHT), q=qx(45)))
    snake = (70, 130, 70)
    for side in (1, -1):
        for k in range(10):
            a = k * 36 + 18
            d = (math.cos(math.radians(a)), math.sin(math.radians(a)))
            q0 = K.qmul(qx(a), qz(90 * side))
            plate.append(chain("Snake_%s_%d" % ("P" if side > 0 else "N", k), (side * 3.5, d[0] * 4.5, d[1] * 4.5),
                               [(3, 1.75, 1.5, 0, metal(snake), "flat"), (2.75, 1.5, 1.25, 25, metal(snake), "flat"),
                                (2.25, 1.25, 1.0, -30, metal(GOLD), "flat")], q0=q0))
    shield = pivot("Shield1", (0, 0, -6.6), {"x": 0.43, "y": 0.561, "z": -0.43, "w": 0.561}, plate)
    return mount("Items/Weapons/Shield/Iron.blockymodel", (24, 6, 7), wrap((70, 40, 30), (120, 80, 50), 6), [shield])


def durandal_blade(steel, edge):
    base = blade(steel, edge)

    def paint(face, w, h, seed):
        img = base(face, w, h, seed)
        if face in ("front", "back"):
            for y in range(h):
                v = abs((y + 0.5) / h - 0.5) * 2
                if v < 0.16:
                    for x in range(w):
                        img[y][x][:3] = [c * 0.72 for c in img[y][x][:3]]
            for x0 in range(4, int(w * 0.75), 6):                    # inscription d'or dans la gouttiere
                K.stroke(img, [(x0, h / 2.0 - 1), (x0 + 2, h / 2.0 + 1)], GOLD, 0.4)
                K.put(img, x0 + 3, h / 2.0, GOLD_LIGHT)
        return img
    return paint


def build_durandal():
    """Durandal v2 : lame large d'acier clair a gouttiere gravee d'or, ricasso a la croix, garde d'or aux fleurs de lys
    et rubis, fusee de cuir bleu filee d'or, pommeau en roue avec fenetre de reliquaire en cristal."""
    steel, edge = (200, 206, 218), (248, 250, 255)
    kids = []
    for i in range(3):
        kids.append(box("Pommel_%d" % i, (-15, 0, 0), (3.5 - 0.25 * i, 10, 10), runes(GOLD, GOLD_DARK, 5), q=qx(30 * i)))
    kids.append(lit("Relic", (-15, 0, 0), (4.5, 4.5, 4.5), glow((210, 236, 255), (255, 255, 255)), q=qx(45)))
    kids.append(box("Pommel_Cap", (-18, 0, 0), (2.5, 4.5, 4.5), metal(GOLD_LIGHT)))
    for i, x in enumerate(range(-10, 12, 3)):
        kids += ring_x("Wire_%d" % i, x, 2.5, 0.75, metal(GOLD))
    kids.append(box("Guard", (15, 0, 0), (5, 22, 4.5), runes(GOLD, GOLD_DARK, 5)))
    kids.append(box("Guard_Core", (15, 0, 0), (7, 8, 6.5), metal(GOLD)))
    kids.append(box("Ruby", (15, 0, 0), (3, 3, 7.25), gem((210, 20, 40))))
    kids += rivets("Guard_Rivet", [(15, y, z) for y in (-6.5, 6.5) for z in (2.5, -2.5)], 1.0)
    kids += ring_x("Ferrule_Top", 11.5, 2.75, 2.0, metal(GOLD))                     # viroles de la fusee
    kids += ring_x("Ferrule_Low", -11.5, 2.75, 2.0, metal(GOLD))
    for sgn in (1, -1):                                                # medaillon a la croix au centre de la garde
        kids.append(box("Medal_%d" % sgn, (15, 0, 3.6 * sgn), (4.5, 4.5, 0.75), metal(GOLD_LIGHT), q=qz(45)))
        kids.append(box("Medal_Cross_V_%d" % sgn, (15, 0, 4.05 * sgn), (3.5, 0.75, 0.5), metal((180, 20, 40))))
        kids.append(box("Medal_Cross_H_%d" % sgn, (15.5, 0, 4.05 * sgn), (0.75, 2.5, 0.75), metal((180, 20, 40))))
    for s in (1, -1):
        tag = "P" if s > 0 else "N"
        kids.append(box("Lys_%s" % tag, (15, 12.5 * s, 0), (2.5, 5, 2.5), metal(GOLD_LIGHT)))
        kids.append(seg("Lys_%s_A" % tag, (15, 11 * s, 0), (4.5, 1.75, 2.25), metal(GOLD_LIGHT), q=qz(90 * s + 50)))
        kids.append(seg("Lys_%s_B" % tag, (15, 11 * s, 0), (4.5, 1.75, 2.25), metal(GOLD_LIGHT), q=qz(90 * s - 50)))
    kids.append(box("Ricasso", (20, 0, 0), (5, 14.5, 3.5), runes(GOLD, (140, 20, 30), 4)))
    paint = durandal_blade(steel, edge)
    b = chain("Blade", (22, 0, 0), [(26, 14, 3.0, 0, paint, "flat"), (24, 12.5, 2.6, 0, paint, "flat"), (18, 11, 2.2, 0, paint, "flat")])
    last_seg(b, "Blade")["children"].append(taper_quad("Blade_Tip", (18 - 0.5, 0, 0), 14, 11, point_img(steel, edge)))
    kids.append(b)
    return mount("Items/Weapons/Longsword/Iron.blockymodel", (22, 4.5, 4.5), wrap((30, 50, 130), (60, 90, 170), 4), kids)


def build_labrys():
    """Labrys v2 : grande hache double minoenne, croissants de bronze graves du labyrinthe aux tranchants rouges,
    tete de taureau de bronze au sommet (cornes d'ivoire, yeux rouges, anneau d'or), hampe gainee, pique au talon."""
    br = (196, 132, 60)
    kids = [box("Pommel", (-36, 0, 0), (6, 7, 7), metal(GOLD)),
            box("Pommel_Spike", (-41, 0, 0), (5, 3, 3), metal(GOLD_LIGHT))]
    for i, x in enumerate((-24, -8, 8, 24)):
        kids += ring_x("Band_%d" % i, x, 3.5, 2.0, metal(GOLD))
    kids.append(box("Socket", (50, 0, 0), (26, 9, 8), maze(GOLD, (110, 70, 30))))
    kids += rivets("Socket_Rivet", [(x, 0, z) for x in (40, 50, 60) for z in (4.1, -4.1)], 1.5)
    for s in (1, -1):
        tag = "P" if s > 0 else "N"
        kids += fan("Wing_%s" % tag, (50, 4 * s, 0), 90 * s, 40, 6, 27, 10, 2.75, maze(br, (120, 72, 30)), glow(BLOOD, (255, 90, 70)))
        kids.append(seg("Ridge_%s" % tag, (50, 4 * s, 0), (24, 2, 3.75), metal(GOLD_DARK), q=qz(90 * s)))   # nervure centrale
    kids.append(box("Bull", (66, 0, 0), (9, 10, 8.5), metal(br)))
    kids.append(box("Bull_Snout", (71.5, -1.5, 0), (4, 6, 6.5), metal((170, 110, 50))))
    kids.append(box("Bull_Ring", (74, -3.5, 0), (1.5, 3.5, 3.5), metal(GOLD_LIGHT), q=qx(45)))
    for s in (1, -1):
        kids.append(lit("Bull_Eye_%s" % ("P" if s > 0 else "N"), (68, 1.5, 4.4 * s), (1.5, 1.5, 0.5), glow(BLOOD, (255, 120, 90))))
    ivory = (236, 226, 200)
    for s in (1, -1):
        kids.append(chain("Horn_%s" % ("P" if s > 0 else "N"), (66, 4.5 * s, 0),
                          [(5, 3.25, 3.25, 0, metal(ivory), "flat"), (5, 2.5, 2.5, -35 * s, metal(ivory), "flat"),
                           (4, 1.75, 1.75, -35 * s, metal(ivory), "flat"), (3, 1.0, 1.0, -30 * s, metal(GOLD_LIGHT), "flat")], q0=qz(75 * s)))
    return mount("Items/Weapons/Battleaxe/Iron.blockymodel", (64, 6, 6), wrap((70, 40, 24), (110, 70, 40), 6), kids)


def build_mjollnir():
    """Mjollnir v2 : lourde tete d'acier aux aretes chanfreinees, entrelacs celtiques, bandeau central aux runes de
    foudre, faces de frappe sombres, manche court gaine de cuir, pommeau et dragonne en anneau."""
    steel, dark = (150, 158, 172), (84, 90, 104)
    kids = [box("Pommel", (-23, 0, 0), (5, 7, 7), metal(SILVER)),
            box("Neck", (30, 0, 0), (6, 7, 7), metal(SILVER)),
            box("Collar", (34, 0, 0), (2.5, 9.5, 9.5), metal(dark))]
    for k in range(10):                                                # dragonne en anneau sous le pommeau
        a = math.radians(k * 36)
        kids.append(box("Strap_%d" % k, (-30 + math.cos(a) * 4, math.sin(a) * 4, 0), (2.75, 1.25, 1.25), metal((90, 56, 32)), q=qz(k * 36 + 90)))
    for i, x in enumerate((-12, 2, 16)):
        kids += ring_x("Band_%d" % i, x, 3.0, 1.5, metal(SILVER_DARK))
    kids.append(box("Head", (47, 0, 0), (20, 28, 18), knotwork(steel, (60, 70, 90))))
    for sx in (1, -1):                                                 # chanfreins le long des aretes
        for sz in (1, -1):
            kids.append(box("Bevel_%d_%d" % (sx, sz), (47 + 9.5 * sx, 0, 8.5 * sz), (4, 27, 4), metal(steel), q=qy(45)))
    for s in (1, -1):
        tag = "P" if s > 0 else "N"
        kids.append(box("Face_%s" % tag, (47, 14.5 * s, 0), (17, 2.5, 15), knotwork(dark, (40, 46, 60))))
        kids.append(lit("Face_Rune_%s" % tag, (47, 15.9 * s, 0), (6, 0.5, 6), glow(STORM, STORM_CORE), q=qy(45)))
    kids.append(box("Band", (47, 0, 0), (21, 7, 19), metal(dark)))
    for k in range(8):                                                 # anneau de runes au col
        a = k * 45
        kids.append(lit("Collar_Rune_%d" % k, (34, math.cos(math.radians(a)) * 4.9, math.sin(math.radians(a)) * 4.9), (1.5, 0.5, 1.25), glow(STORM, STORM_CORE), q=qx(a)))
    kids += rivets("Head_Rivet", [(47 + dx, dy, 9.1 * sz) for dx in (-7, 7) for dy in (-10, 10) for sz in (1, -1)], 1.5, metal(SILVER))
    for s in (1, -1):
        for k, dy in enumerate((-2, 2)):
            kids.append(lit("Rune_%s_%d" % ("P" if s > 0 else "N", k), (47 + 4 * (k * 2 - 1), dy, 9.75 * s), (5, 1.25, 0.5), glow(STORM, STORM_CORE), q=qz(50 * (k * 2 - 1))))
        kids.append(lit("Rune_%s_C" % ("P" if s > 0 else "N"), (47, 0, 9.75 * s), (1.25, 5, 0.5), glow(STORM, STORM_CORE)))
    return mount("Items/Weapons/Mace/Iron.blockymodel", (44, 5.5, 5.5), wrap((90, 56, 32), (130, 84, 48), 5), kids)


def build_sharur():
    """Sharur v2 : masse royale d'or et de lapis, huit grandes ailettes incrustees de lapis aux pierres lumineuses,
    couronne a pointe, ailes d'aigle deployees au col, hampe d'or baguee de lapis."""
    kids = [box("Pommel", (-36, 0, 0), (6, 7, 7), metal(GOLD)),
            lit("Pommel_Gem", (-40, 0, 0), (2.5, 3.5, 3.5), glow((70, 120, 255), (210, 225, 255)), q=qx(45))]
    for i, x in enumerate((-22, -8, 6, 20, 32)):
        kids += ring_x("Band_%d" % i, x, 3.0, 2.0, metal(LAPIS))
    for s in (1, -1):
        for k, (L, a) in enumerate(((15, 28), (12.5, 46), (10, 64))):
            kids.append(chain("Wing_%s_%d" % ("P" if s > 0 else "N", k), (40, 2.5 * s, 0),
                              [(L * 0.6, 3.5, 1.5, 0, feather(GOLD, GOLD_LIGHT), "flat"),
                               (L * 0.5, 2.75, 1.25, -12 * s, feather(GOLD, LAPIS), "flat")], q0=qz(a * s)))
    for k in range(4):                                                 # griffes d'or qui enserrent la tete
        a = k * 90 + 45
        kids.append(pivot("Claw_%d" % k, (44, 0, 0), qx(a), [chain("Claw_%d_C" % k, (0, 2.5, 0),
                    [(5, 2, 2, 0, metal(GOLD), "flat"), (4, 1.6, 1.6, 30, metal(GOLD), "flat"), (2.5, 1.0, 1.0, 35, metal(GOLD_LIGHT), "flat")], q0=qz(20))]))
    kids.append(box("Core", (54, 0, 0), (14, 12, 12), stripes(LAPIS, LAPIS_DARK, 5, 1)))
    kids.append(box("Core_B", (54, 0, 0), (13.5, 12, 12), stripes(LAPIS, LAPIS_DARK, 5, 1), q=qx(45)))
    for k in range(4):
        kids.append(box("Flange_%d" % k, (54, 0, 0), (18, 30, 2.5 - 0.25 * (k % 2)), inlay(GOLD, LAPIS), q=qx(k * 45)))
    for k in range(8):
        a = math.radians(k * 45)
        kids.append(lit("Flange_Gem_%d" % k, (54, math.cos(a) * 14.5, math.sin(a) * 14.5), (4, 2, 2), glow((80, 130, 255), (220, 230, 255)), q=qx(k * 45)))
    kids.append(box("Cap", (64, 0, 0), (3, 10, 10), metal(GOLD)))
    kids.append(box("Crown", (67, 0, 0), (3, 7, 7), metal(GOLD_LIGHT), q=qx(45)))
    for k in range(4):                                                 # pointes de la couronne
        a = k * 90 + 45
        kids.append(pivot("Crown_Spike_%d" % k, (66.5, 0, 0), qx(a), [seg("Crown_Spike_%d_S" % k, (0, 3, 0), (4, 1.5, 1.5), metal(GOLD_LIGHT), q=qz(35))]))
    kids.append(box("Spike", (71.5, 0, 0), (6, 3.5, 3.5), metal(GOLD_LIGHT)))
    kids.append(lit("Spike_Tip", (75, 0, 0), (2.5, 2, 2), glow((120, 160, 255), (230, 240, 255))))
    return mount("Items/Weapons/Mace/Iron.blockymodel", (64, 5, 5), metal(GOLD_DARK, 0.15), kids)


def bow_build(metal_c, line_c, string_c, string_core, accent, accent_core, grip_c, deco):
    """Arc sur les noeuds vanilla (noms gardes pour l'animation de tir), avec ornements : deco(kids, van)."""
    van = vanilla_handle_children("Items/Weapons/Bow/Iron.blockymodel")

    def paint_of(name):
        if name.startswith("Rope"):
            return glow(string_c, string_core), "fullbright"
        if name.startswith("ARROW"):
            return metal(metal_c), "flat"
        return metal(metal_c, 0.3), "flat"                              # 1 px par unite : metal propre, sans gravure bruitee
    kids = [reskin(c, paint_of) for c in van["children"]]
    for c in kids:                                                    # filigrane lumineux le long du dos des branches
        if c["name"] in ("Bow-Top", "Bow-Bot"):
            c["children"].append(lit("Filigree_%s" % c["name"], (0, 4.25, 0), (26, 1, 5.5), glow(accent, accent_core)))
            tip = c["children"][0]
            sgn = 1 if c["name"] == "Bow-Top" else -1
            tip["children"].append(chain("Curl_%s" % c["name"], (0, -6, 0), [(4, 3, 4, 0, metal(metal_c), "flat"), (3, 2.25, 3.5, -50 * sgn, metal(metal_c), "flat"),
                                                                         (2.5, 1.75, 3, -50 * sgn, metal(accent), "flat")], q0=qz(-90 + 30 * sgn)))
            deco(tip, sgn)
    kids.append(box("Arrow_Rest", (0, -4.5, 3.75), (3, 1.5, 1.5), metal(metal_c)))
    for c in kids:                                                     # pierre au milieu de chaque branche
        if c["name"] in ("Bow-Top", "Bow-Bot"):
            c["children"].append(box("Limb_Gem_%s" % c["name"], (0, 4.5, 2.9), (2.5, 2.5, 1), gem(accent)))
    for sgn in (1, -1):
        kids.append(box("Grip_Cap_%d" % sgn, (8.5 * sgn, 0, 0), (2, 9, 8), metal(accent)))
    return kids


def build_artemis():
    """Arc d'Artemis v2 : branches d'argent filigranees de lune, croissants de lune aux extremites, disque lunaire et
    etoiles a la poignee, corde de lumiere."""
    def tip_deco(tip, s):
        for k, a in enumerate((-50, 0, 50)):                         # croissant de lune
            r = 5.5
            p = (2 * s + math.cos(math.radians(a + 90)) * r * 0.4, -9 + math.sin(math.radians(a)) * r, 0)
            tip["children"].append(lit("Moon_%d_%d" % (s, k), p, (1.75, 4, 3), glow(MOON, MOON_CORE), q=qz(a)))
    kids = bow_build((226, 232, 244), (120, 150, 210), MOON, MOON_CORE, MOON, MOON_CORE, None, tip_deco)
    kids.append(lit("Moon_Disc", (0, 7, 0), (7, 7, 1.5), glow(MOON, MOON_CORE)))
    kids.append(lit("Moon_Disc_B", (0, 7, 0), (7, 7, 1.25), glow(MOON, MOON_CORE), q=qz(45)))
    kids.append(box("Moon_Shadow", (1.75, 7.75, 0), (4.5, 4.5, 1.75), metal((60, 80, 130))))
    for k in range(4):
        a = math.radians(k * 90 + 45)
        kids.append(lit("Star_%d" % k, (math.cos(a) * 6.5, 7 + math.sin(a) * 6.5, 0), (1.5, 1.5, 1.5), glow((255, 255, 255), (255, 255, 255)), q=qz(45)))
    root, handle = mount("Items/Weapons/Bow/Iron.blockymodel", (39, 8, 7), wrap((214, 220, 236), (120, 140, 190), 5), kids)
    return root, handle


def build_apollon():
    """Arc d'Apollon : branches d'or rayonnantes, pointes en flammes, soleil a la poignee, corde de lumiere doree."""
    def tip_deco(tip, s):
        for k, (a, L) in enumerate(((-30, 6), (0, 8), (30, 6))):    # flammes aux extremites
            tip["children"].append(lit("Flame_%d_%d" % (s, k), (2 * s, -9, 0), (L, 2, 2.5), glow((255, 120, 20), (255, 230, 150)), q=qz(-90 + a)))
    kids = bow_build(GOLD, (150, 60, 10), (255, 220, 120), (255, 250, 220), SUN, SUN_CORE, None, tip_deco)
    kids.append(lit("Sun", (0, 7.5, 0), (8, 8, 2), glow(SUN, SUN_CORE)))
    kids.append(lit("Sun_B", (0, 7.5, 0), (8, 8, 1.75), glow(SUN, SUN_CORE), q=qz(45)))
    for k in range(8):
        a = k * 45 + 22.5
        d = (math.cos(math.radians(a)), math.sin(math.radians(a)))
        kids.append(lit("Ray_%d" % k, (d[0] * 7, 7.5 + d[1] * 7, 0), (4, 1.25, 1.25), glow(SUN_CORE, (255, 255, 230)), q=qz(a)))
    root, handle = mount("Items/Weapons/Bow/Iron.blockymodel", (39, 8, 7), wrap(GOLD, (180, 60, 20), 5), kids)
    return root, handle


def ruyi_lacquer():
    """Laque rouge du Ruyi : rouge profond, volutes de nuages d'or (spirales), filets d'or."""
    base = metal(LACQUER_RED, 0.3, finish=False)

    def paint(face, w, h, seed):
        img = base(face, w, h, seed)
        for x0 in range(8, w - 8, 22):
            for k in range(3):
                cx, cy = x0 + k * 4.5, h / 2.0 + (1.0 if k % 2 else -1.0)
                pts = [(cx + math.cos(t) * (0.4 + t * 0.35), cy + math.sin(t) * (0.4 + t * 0.35)) for t in [i * 0.5 for i in range(12)]]
                K.stroke(img, pts, GOLD, 0.45)
        for y in (1, h - 2):
            for x in range(w):
                img[y][x][:3] = list(GOLD_DARK)
        return K.finish(img, face, edges=False)
    return paint


def build_ruyi():
    """Ruyi Jingu Bang v2 (baton de combat, tenu comme une lance) : fer laque rouge, larges viroles d'or gravees,
    nuages d'or peints, anneaux d'or a la prise."""
    lacquer = ruyi_lacquer()
    kids = []
    for s in (1, -1):
        tag = "P" if s > 0 else "N"
        kids.append(box("Handle2_%s" % tag, (45 * s, 0, 0), (58, 6, 6), lacquer))
        kids.append(box("Cap_%s" % tag, (66 * s, 0, 0), (16, 7.5, 7.5), runes(GOLD, (60, 30, 10), 5)))
        kids.append(box("Cap_Ring_%s" % tag, (58.5 * s, 0, 0), (1.5, 8.5, 8.5), metal(GOLD_LIGHT), q=qx(45)))
        kids.append(box("Cap_End_%s" % tag, (74.75 * s, 0, 0), (1.5, 6.5, 6.5), metal(GOLD_LIGHT)))
        kids += ring_x("Band_%s" % tag, 18 * s, 3.5, 2.0, metal(GOLD))
        kids += ring_x("Band2_%s" % tag, 22 * s, 3.25, 1.0, metal(GOLD_LIGHT))
        kids += helix("Cap_Wire_%s" % tag, 50 * s if s > 0 else -57, 57 if s > 0 else -50, 3.1, 45, 0.8, (1.0, 0.5, 0.75), metal(GOLD))
    return mount("Items/Weapons/Spear/Iron.blockymodel", (32, 6, 6), wrap(LACQUER_RED, (120, 16, 16), 6), kids)


def build_carnwennan():
    """Carnwennan v2 : kris d'obsidienne a six ondes, fil et gouttiere violets lumineux, garde en ailes de chauve-souris
    (os d'argent, membranes sombres), fusee baguee d'argent, pommeau en croissant a l'amethyste."""
    obs = (24, 18, 34)
    kids = [box("Pommel", (-9, 0, 0), (3, 5, 5), metal(SILVER)),
            lit("Pommel_Gem", (-11, 0, 0), (2.25, 3.25, 3.25), glow(SHADOW, SHADOW_CORE), q=qx(45))]
    for s in (1, -1):
        kids.append(seg("Crescent_%s" % ("P" if s > 0 else "N"), (-10.5, 1.5 * s, 0), (4, 1.5, 2), metal(SILVER), q=qz(180 - 55 * s)))
    kids += helix("Grip_Wire", -7, 8, 2.35, 50, 0.9, (1.0, 0.5, 0.75), metal(SILVER))   # fil d'argent sur la fusee
    kids.append(box("Guard", (13, 0, 0), (3.5, 7, 5), metal(SILVER)))
    membrane = metal((50, 30, 70), 0.1)
    for s in (1, -1):
        tag = "P" if s > 0 else "N"
        kids.append(chain("Wing_%s" % tag, (13, 3 * s, 0),
                          [(6, 2, 2.25, 0, metal(SILVER), "flat"), (5, 1.75, 2, -45 * s, metal(SILVER), "flat"),
                           (3, 1.25, 1.75, -30 * s, metal(SILVER), "flat")], q0=qz(75 * s)))
        kids.append(box("Membrane_%s" % tag, (11, 6.5 * s, 0), (5, 6, 0.75), membrane, q=qz(-20 * s)))
        kids.append(lit("Wing_Gem_%s" % tag, (16, 10 * s, 0), (1.5, 1.5, 1.5), glow(SHADOW, SHADOW_CORE), q=qx(45)))
    parts = []
    for i, (L, W) in enumerate(((6.5, 7.5), (6, 7), (6, 6.5), (5.5, 5.5), (5, 4.5), (4.5, 3))):
        parts.append((L, W, 2.0 - i * 0.2, (16 if i % 2 else -16) if i else 0, blade(obs, (90, 60, 130)), "flat"))
    b = chain("Blade", (15, 0, 0), parts)
    kids.append(b)
    node, i = b["children"][0], 0
    while node is not None:
        L, W, T = parts[i][0], parts[i][1], parts[i][2]
        for s in (1, -1):
            node["children"].append(lit("Edge_%d_%s" % (i, "P" if s > 0 else "N"), (L / 2.0, s * (W / 2.0 - 0.25), 0), (L, 0.75, T + 0.25), glow(SHADOW, SHADOW_CORE)))
        node["children"].append(lit("Fuller_%d" % i, (L / 2.0, 0, 0), (L - 0.5, 0.75, T + 0.25), glow(SHADOW, SHADOW_CORE)))
        nxt = [c for c in node["children"] if c["name"].startswith("Blade_") and not c["name"].endswith("_Box")]
        node = nxt[0] if nxt else None
        i += 1
    return mount("Items/Weapons/Dagger/Iron.blockymodel", (20, 4.5, 4.5), wrap((16, 14, 20), (60, 50, 70), 4), kids)


def build_guandao():
    """Guandao v2 : grande lame en croissant qui sort de la gueule d'un dragon de jade (machoires, cornes d'or, criniere,
    yeux d'ambre), gouttiere de jade gravee d'or, eperon dorsal, pompon rouge, hampe laquee a spirale d'or."""
    steel, edge = (206, 212, 222), (250, 252, 255)
    kids = [box("Butt", (-62, 0, 0), (6, 6, 6), metal(GOLD)),
            box("Butt_Spike", (-67, 0, 0), (5, 3.5, 3.5), metal(GOLD_LIGHT))]
    for i, x in enumerate((-40, -15, 15, 40)):
        kids += ring_x("Band_%d" % i, x, 3.0, 1.5, metal(GOLD))
    kids += ring_x("Collar", 56, 3.75, 3.0, metal(JADE))
    kids += rivets("Collar_Rivet", [(56, 3.9 * math.cos(a), 3.9 * math.sin(a)) for a in (0, 1.57, 3.14, 4.71)], 1.0)
    kids.append(box("Dragon_Skull", (62, 1, 0), (10, 10, 8.5), runes(JADE, JADE_DARK, 4)))
    kids.append(box("Dragon_Snout", (68.5, 0, 0), (5, 6, 6.5), metal(JADE)))
    kids.append(box("Dragon_Jaw", (67.5, -4.5, 0), (6, 2.5, 6), metal(JADE_DARK)))
    for s in (1, -1):
        tag = "P" if s > 0 else "N"
        kids.append(chain("Horn_%s" % tag, (60, 5.5, 2.5 * s), [(5, 1.75, 1.75, 0, metal(GOLD), "flat"), (4, 1.25, 1.25, -25, metal(GOLD), "flat")], q0=qz(155)))
        kids.append(lit("Eye_%s" % tag, (65, 2.5, 4.4 * s), (1.5, 1.5, 0.5), glow((255, 190, 40), (255, 250, 200))))
        for k in range(3):
            kids.append(box("Mane_%s_%d" % (tag, k), (57 + k * 2.5, 6 + k * 0.5, 3.5 * s), (2.5, 4, 1.5), fur((200, 60, 30)), q=qz(130)))
    blade_p = blade(steel, edge, JADE)
    parts = [(16, 16, 2.75, 0, blade_p, "flat"), (16, 17, 2.5, 4, blade_p, "flat"), (15, 15.5, 2.25, 7, blade_p, "flat"), (12, 12, 2.0, 9, blade_p, "flat")]
    b = chain("Blade", (68, -1.5, 0), parts)
    last_seg(b, "Blade")["children"].append(taper_quad("Blade_Tip", (12 - 0.5, 0, 0), 14, 12, point_img(steel, edge), q=qz(8)))
    kids.append(b)
    kids.append(seg("Spur", (80, 8, 0), (10, 4, 2), metal(steel), q=qz(140)))
    kids.append(chain("Tassel", (57, -5, 0), [(3, 2.5, 2.5, 0, fur((200, 30, 30)), "flat"), (3, 3, 3, 10, fur((200, 30, 30)), "flat"),
                                               (4, 3.75, 3.75, 10, fur((170, 20, 20)), "flat")], q0=qz(-150)))
    kids += helix("Coil", 8, 52, 2.9, 32, 1.4, (1.75, 1.0, 1.5), metal(GOLD))          # dragon d'or enroule sur la hampe
    return mount("Items/Weapons/Spear/Iron.blockymodel", (120, 5, 5), ruyi_lacquer(), kids)


def spectral_scythe():
    """Faux spectrale de Thanatos (Moisson des Ames) : hampe le long de +z depuis l'origine (le porteur), grande lame
    courbe a l'horizontale au bout ; noir et vert des ames, lumineuse. Deplacee par le code autour du porteur."""
    global M
    M = K.Model("Thanatos_Faux_Spectrale")
    M.reuse = True
    dark = (20, 30, 26)
    kids = [box("Shaft", (0, 0, 40), (4, 4, 80), metal(dark))]
    parts = [(14, 9, 2.0, 0, glow(SOUL, SOUL_CORE), "fullbright"), (13, 8, 1.75, -16, glow(SOUL, SOUL_CORE), "fullbright"),
             (12, 6.5, 1.5, -18, glow((60, 200, 140), SOUL_CORE), "fullbright"), (10, 4.5, 1.25, -20, glow((60, 200, 140), SOUL_CORE), "fullbright"),
             (8, 2.5, 1.0, -24, glow(SOUL, SOUL_CORE), "fullbright")]
    blade_root = chain("Blade", (0, 0, 80), parts, q0=K.qmul(qx(90), qz(180)))
    kids.append(blade_root)
    kids.append(lit("Skull", (0, 0, 81), (7, 7, 7), glow((230, 255, 240), (255, 255, 255))))
    root = pivot("Root", (0, 0, 0), ID_Q, kids)
    renumber(root)
    W, H, tex = M.texture()
    out = os.path.join(RES, "Common", "Items", "Projectiles")
    png.write(os.path.join(out, "Thanatos_Faux_Spectrale.png"), W, H, tex)
    json.dump({"nodes": [root], "lod": "auto"}, open(os.path.join(out, "Thanatos_Faux_Spectrale.blockymodel"), "w"), indent=1)
    write_carrier("Thanatos_Faux_Spectrale", "#70ffb0", 1.5)


def write_attached(name, weapon_id, light, particle=None, scale_hitbox=0.6):
    """Arme en vol (lancer) : squelette de projectile vanilla + l'arme en attache (montee comme une fleche vanilla :
    son Origin_Projectile suit l'axe du vol), comme Gungnir_Flying."""
    asset = {"$Comment": "Arme divine en vol, deplacee par le code (ArmesDivines). Genere par gen_divines.py.",
             "Model": "Items/Projectiles/Projectile.blockymodel", "Texture": "Items/Projectiles/Projectile_default.png",
             "DefaultAttachments": [{"Model": "%s/%s.blockymodel" % (MODEL_REL, weapon_id), "Texture": "%s/%s.png" % (MODEL_REL, weapon_id)}],
             "AnimationSets": {"FlyIdle": {"Animations": [{"Animation": "Items/Animations/Fly/Fly_Vertical.blockyanim"}]}},
             "MinScale": 1.0, "MaxScale": 1.0,
             "HitBox": {"Max": {"X": scale_hitbox, "Y": 0.1, "Z": 0.1}, "Min": {"X": -scale_hitbox, "Y": -0.1, "Z": -0.1}},
             "Light": {"Color": light, "Radius": 7}}
    if particle:
        asset["Particles"] = [{"SystemId": particle, "TargetNodeName": "", "Scale": 1.0}]
    json.dump(asset, open(os.path.join(RES, "Server", "Models", "Projectiles", "Abilities", name + ".json"), "w"), indent=2)
    neutral = {"Interactions": [{"Type": "Simple", "RunTime": 0.01}]}
    config = {"$Comment": "Porteur de modele deplace par le code (ArmesDivines). Interactions neutres obligatoires.",
              "Model": name, "UseModelScale": True,
              "Physics": {"Type": "Standard", "Gravity": 0, "TerminalVelocityAir": 1, "TerminalVelocityWater": 1, "Bounciness": 0, "SticksVertically": False},
              "LaunchForce": 0, "SpawnOffset": {"X": 0, "Y": 0, "Z": 0},
              "Interactions": {"ProjectileHit": neutral, "ProjectileMiss": neutral}}
    json.dump(config, open(os.path.join(RES, "Server", "ProjectileConfigs", "Abilities", "Projectile_Config_" + name + ".json"), "w"), indent=2)


def divine_arrow(name, colors, trail, light, particle=None):
    """Fleche divine : notre modele de fleche (arrow_model), traine et lumiere ; deplacee par le code (sorts) ou tiree
    par l'arc (bow_shots)."""
    arrow_model(name, *colors)
    start, end, tex = trail
    t = {"TexturePath": tex, "LifeSpan": 8, "Roll": 0, "Start": {"Color": "rgba(%s, 1)" % start, "Width": 0.25},
         "End": {"Color": "rgba(%s, 0)" % end, "Width": 0}, "LightInfluence": 0, "Smooth": True, "RenderMode": "BlendLinear"}
    json.dump(t, open(os.path.join(RES, "Server", "Entity", "Trails", name + "_Trail.json"), "w"), indent=2)
    asset = {"$Comment": "Fleche divine deplacee par le code (ArmesDivines). Genere par gen_divines.py.",
             "Model": "Items/Projectiles/Projectile.blockymodel", "Texture": "Items/Projectiles/Projectile_default.png",
             "DefaultAttachments": [{"Model": "Items/Projectiles/" + name + ".blockymodel", "Texture": "Items/Projectiles/" + name + ".png"}],
             "AnimationSets": {"FlyIdle": {"Animations": [{"Animation": "Items/Animations/Fly/Fly_Vertical.blockyanim"}]}},
             "MinScale": 1.0, "MaxScale": 1.0,
             "HitBox": {"Max": {"X": 0.6, "Y": 0.1, "Z": 0.1}, "Min": {"X": -0.6, "Y": -0.1, "Z": -0.1}},
             "Trails": [{"TrailId": name + "_Trail", "TargetNodeName": "Handle", "PositionOffset": {"X": -0.3}, "RotationOffset": {"Roll": r}} for r in (0, 90)],
             "Light": {"Color": light, "Radius": 5}}
    if particle:
        asset["Particles"] = [{"SystemId": particle, "TargetNodeName": "", "Scale": 0.6}]
    json.dump(asset, open(os.path.join(RES, "Server", "Models", "Projectiles", "Abilities", name + ".json"), "w"), indent=2)
    neutral = {"Interactions": [{"Type": "Simple", "RunTime": 0.01}]}
    config = {"$Comment": "Fleche divine deplacee par le code (ArmesDivines). Interactions neutres obligatoires.",
              "Model": name, "UseModelScale": True,
              "Physics": {"Type": "Standard", "Gravity": 0, "TerminalVelocityAir": 1, "TerminalVelocityWater": 1, "Bounciness": 0, "SticksVertically": False},
              "LaunchForce": 0, "SpawnOffset": {"X": 0, "Y": 0, "Z": 0},
              "Interactions": {"ProjectileHit": neutral, "ProjectileMiss": neutral}}
    json.dump(config, open(os.path.join(RES, "Server", "ProjectileConfigs", "Abilities", "Projectile_Config_" + name + ".json"), "w"), indent=2)


# ---------------------------------------------------------------------------------------------------------------------
# Fiche de chaque arme : famille vanilla de reference, classes, signature (animations), traine, passif (texte)
# ---------------------------------------------------------------------------------------------------------------------
WEAPONS = {
    "Ra": dict(build=build_ra, id="Artefact_Staff_Ouas_Ra", folder="Staff", ref="Staff/Weapon_Staff_Crystal_Flame",
               name="Ouas de Râ, Sceptre du Soleil",
               desc="Artefact divin. Le sceptre du dieu soleil. Ses coups embrasent et infligent +25 % de dégâts aux morts-vivants. "
                    "Signature, Œil de Râ : un soleil s'allume au-dessus du porteur et frappe de ses rayons les ennemis proches pendant 5 secondes.",
               anims=("CastSummonCharging", "CastSummonCharged"), spell="OeilDeRa", trail=("#fff1b0", "#ff7a10")),
    "Circe": dict(build=build_circe, id="Artefact_Wand_Circe", folder="Wand", ref="Wand/Weapon_Wand_Wood",
                  name="Baguette de Circé",
                  desc="Artefact divin. La baguette de l'enchanteresse. Ses coups affaiblissent les ennemis. "
                       "Signature, Philtre de Circé : un maléfice d'améthyste bondit d'ennemi en ennemi (jusqu'à 8), les frappe, les affaiblit et les fige.",
                  anims=("CastLeftCharging", "CastLeftCharged"), spell="PhiltreDeCirce", trail=("#f0d0ff", "#7a20d0")),
    "Egide": dict(build=build_egide, id="Artefact_Shield_Egide", folder="Shield", ref="Shield/Weapon_Shield_Iron",
                  name="Égide, Bouclier d'Athéna",
                  desc="Artefact divin. Le bouclier d'Athéna, orné de la tête de Méduse. Tenu en main, il réduit de 25 % tous les dégâts subis. "
                       "Compétence (toutes les 25 s), Regard de Méduse : le regard de la Gorgone balaie le terrain devant le porteur ; les ennemis "
                       "touchés deviennent gris, se figent et sont pris dans une gangue de pierre pendant 4 secondes.",
                  anims=("Guard", "GuardBash"), spell="RegardDeMeduse", trail=None, cooldown=25),
    "Durandal": dict(build=build_durandal, id="Artefact_Longsword_Durandal", folder="Longsword", ref="Longsword/Weapon_Longsword_Iron",
                     name="Durandal, Lame de Roland",
                     desc="Artefact divin. L'épée de Roland, qui jamais ne se brise. +30 % de dégâts quand le porteur est sous la moitié de ses points de vie. "
                          "Signature, Brèche de Roland : la lame frappe le sol et le fend ; une faille court droit devant sur 22 blocs, projette la roche "
                          "et soulève les ennemis qu'elle croise.",
                     anims=("SwingLeftCharging", "SwingDown"), spell="BrecheDeRoland", trail=("#ffffff", "#ffd36b")),
    "Skadi": dict(build=build_skadi, id="Artefact_Axe_Skadi", folder="Axe", ref="Axe/Weapon_Axe_Iron",
                  name="Hache de Skaði, Croc de l'Hiver",
                  desc="Artefact divin. La hache de la déesse de l'hiver. Ses coups ralentissent, et gèlent parfois la cible. "
                       "Signature, Hiver de Skaði : le sol gèle devant le porteur, des pics de glace en jaillissent un à un et emprisonnent les ennemis dans la glace, puis volent en éclats.",
                  anims=("SwingLeftCharging", "SwingDown"), spell="HiverDeSkadi", trail=("#ffffff", "#60c8ff")),
    "Labrys": dict(build=build_labrys, id="Artefact_Battleaxe_Labrys", folder="Battleaxe", ref="Battleaxe/Weapon_Battleaxe_Iron",
                   name="Labrys, Hache du Minotaure",
                   desc="Artefact divin. La hache double du Labyrinthe. +35 % de dégâts sur une cible en pleine santé. "
                        "Signature, Charge du Minotaure : le porteur fonce tête baissée en labourant le sol, encorne et projette tout sur sa route, puis abat la hache qui fend la terre.",
                   anims=("SwingLeftCharging", "SwingLeftCharged"), spell="ChargeDuMinotaure", trail=("#ffd090", "#b01010")),
    "Thanatos": dict(build=build_thanatos, id="Artefact_Battleaxe_Faux_Thanatos", folder="Battleaxe", ref="Battleaxe/Weapon_Battleaxe_Iron",
                     name="Faux de Thanatos",
                     desc="Artefact divin. La faux de la Mort. Vole 12 % des dégâts infligés et fauche net les ennemis sous 10 % de leurs points de vie. "
                          "Signature, Moisson des Âmes : une faux spectrale géante tourne trois fois autour du porteur ; les âmes arrachées aux ennemis viennent le soigner, et les plus faibles sont fauchées.",
                     anims=("WhirlwindCharging", "WhirlwindChargedSpin"), spell="MoissonDesAmes", trail=("#d0ffe8", "#103020")),
    "Mjollnir": dict(build=build_mjollnir, id="Artefact_Mace_Mjollnir", folder="Mace", ref="Mace/Weapon_Mace_Iron",
                     name="Mjöllnir, Marteau de Thor",
                     desc="Artefact divin. Le marteau du dieu du tonnerre. 20 % de chance qu'un coup appelle la foudre (+50 % de dégâts, choc). "
                          "Attaque maintenue : Mjöllnir est lancé droit devant sur 100 blocs, traverse tout, foudroie ce qu'il touche et revient dans la main. "
                          "Signature, Colère de Thor : le ciel s'assombrit, la foudre s'abat sur les ennemis l'un après l'autre et brûle le sol, puis un éclair "
                          "frappe le marteau levé et libère une onde de tonnerre.",
                     anims=("SwingDownCharging", "SwingDownCharged"), spell="ColereDeThor", trail=("#e0f0ff", "#3080ff"),
                     primary=dict(charge_anim="SwingDownCharging", chain=["Weapon_Mace_Primary_Swing_Left", "Weapon_Mace_Primary_Swing_Right",
                                  "Weapon_Mace_Primary_Swing_Up_Left"], allowance=2.1, hold=0.7, hold_anim="SwingDownCharged", hold_spell="MjollnirLancer", vars=[])),
    "Sharur": dict(build=build_sharur, id="Artefact_Mace_Sharur", folder="Mace", ref="Mace/Weapon_Mace_Iron",
                   name="Sharur, Écraseur de Milliers",
                   desc="Artefact divin. La masse ailée de Ninurta. Un coup sur quatre frappe deux fois et demie plus fort. "
                        "Signature, Écraseur de Milliers : un tourbillon de poussière attire les ennemis, puis la masse s'abat : cratère, onde de choc et roches projetées tout autour.",
                   anims=("SwingDownCharging", "SwingDownCharged"), spell="EcraseurDeMilliers", trail=("#ffe8a0", "#2040c0")),
    "Artemis": dict(build=build_artemis, id="Artefact_Shortbow_Artemis", folder="Shortbow", ref="Shortbow/Weapon_Shortbow_Iron",
                    name="Arc d'Artémis, l'Arc d'Argent",
                    desc="Artefact légendaire. L'arc de la chasseresse. +25 % de chance de coup critique. "
                         "Signature, Pluie Lunaire : une volée de vraies flèches d'argent tombe du ciel sur la zone visée et s'y plante.",
                    anims=("ShootCharging", "ShootCharged"), spell="PluieLunaire", trail=None, arrow="Artemis_Fleche",
                    quality="Legendary", level=550, factor=3.8),             # legendaire (demande du 2026-11-03)
    "Ruyi": dict(build=build_ruyi, id="Artefact_Staff_Ruyi_Jingu_Bang", folder="Staff", ref="Spear/Weapon_Spear_Iron",
                 name="Ruyi Jingu Bang, Bâton du Roi Singe",
                 desc="Artefact divin. Le bâton de combat qui grandit à volonté. Ses coups étourdissent parfois. Clic : enchaînement de cinq coups de bâton. "
                      "Attaque maintenue : le bâton s'allonge d'un coup droit devant sur 14 blocs et renverse tout sur sa longueur. "
                      "Signature, Bâton Céleste : le bâton grandit jusqu'au ciel puis s'abat devant le porteur et fend le sol sur toute sa longueur.",
                 anims=("ThrowCharging", "SpinSwingLeftHeavy"), spell="BatonCeleste", trail=("#ffe090", "#c01818"), anim_set="Spear",
                 primary=dict(charge_anim="ThrowCharging", chain=["Spear_Swing_Left", "Spear_Swing_Right", "Spear_Spin_Swing_Left", "Spear_Spin_Swing_Right",
                              "Spear_Spin_Swing_Left_Heavy"], allowance=1.3, hold=0.6, hold_anim="Stab", hold_spell="RuyiExtension",
                              vars=["Spear_Swing_Left_Damage", "Spear_Swing_Right_Damage", "Spear_Spin_Swing_Left_Damage", "Spear_Spin_Swing_Right_Damage",
                                    "Spear_Spin_Swing_Left_Heavy_Damage"])),
    "Heracles": dict(build=build_heracles, id="Artefact_Club_Heracles", folder="Club", ref="Club/Weapon_Club_Iron",
                     name="Gourdin d'Héraclès",
                     desc="Artefact divin. La massue d'olivier du héros aux douze travaux. +50 % de dégâts contre les bêtes et les créatures colossales. "
                          "Signature, Coup d'Héraclès : un coup titanesque fend le sol en éventail, arrache la roche et projette au loin tout ce qui se trouve devant.",
                     anims=("SwingDownCharging", "SwingDownCharged"), spell="CoupDHeracles", trail=("#fff0c0", "#b07020")),
    "Dagda": dict(build=build_dagda, id="Artefact_Club_Dagda", folder="Club", ref="Club/Weapon_Club_Iron",
                  name="Massue du Dagda",
                  desc="Artefact divin. Un bout tue, l'autre ranime. Chaque coup soigne les alliés proches. "
                       "Signature, Chaudron du Dagda : la massue frappe le sol ; l'onde blesse les ennemis, les fleurs éclosent tout autour et les alliés se régénèrent pendant 6 secondes.",
                  anims=("SwingDownCharging", "SwingDownCharged"), spell="ChaudronDuDagda", trail=("#d8ffe0", "#208040")),
    "Carnwennan": dict(build=build_carnwennan, id="Artefact_Daggers_Carnwennan", folder="Daggers", ref="Daggers/Weapon_Daggers_Iron",
                       name="Carnwennan, Dague de l'Ombre",
                       desc="Artefact divin. La dague d'Arthur, qui voile son porteur d'ombre. +60 % de dégâts dans le dos. "
                            "Signature, Voile de l'Ombre : le porteur se fond dans l'ombre et surgit dans le dos de cinq ennemis l'un après l'autre, puis réapparaît d'où il est parti.",
                       anims=("RazorstrikeCharging", "RazorstrikeLunge"), spell="VoileDeLOmbre", trail=("#e0c0ff", "#300860")),
    "Guandao": dict(build=build_guandao, id="Artefact_Spear_Guandao", folder="Spear", ref="Spear/Weapon_Spear_Iron",
                    name="Guandao du Dragon de Jade",
                    desc="Artefact divin. La lame en croissant du général. 20 % de chance qu'un coup libère une onde qui blesse les ennemis voisins. "
                         "Clic : enchaînement de cinq coups (taille, revers, estoc, moulinet, grand moulinet). Attaque maintenue : le guandao est lancé en "
                         "tournoyant, fauche tout sur 30 blocs et revient toujours dans la main. Signature, Croissant du Dragon : un grand moulinet, puis "
                         "un croissant de jade file droit devant, fauche les herbes et entaille le sol.",
                    anims=("ThrowCharging", "SpinSwingLeftHeavy"), spell="CroissantDuDragon", trail=("#d0ffe0", "#20a060"),
                    primary=dict(charge_anim="ThrowCharging", chain=["Spear_Swing_Left", "Spear_Swing_Right", "Spear_Stab", "Spear_Spin_Swing_Right",
                                 "Spear_Spin_Swing_Left_Heavy"], allowance=1.3, hold=0.8, hold_anim="ThrowCharged", hold_spell="GuandaoLancer",
                                 vars=["Spear_Swing_Left_Damage", "Spear_Swing_Right_Damage", "Spear_Stab_Damage", "Spear_Spin_Swing_Right_Damage",
                                       "Spear_Spin_Swing_Left_Heavy_Damage"])),
    "Apollon": dict(build=build_apollon, id="Artefact_Shortbow_Apollon", folder="Shortbow", ref="Shortbow/Weapon_Shortbow_Iron",
                    name="Arc d'Apollon, l'Arc du Soleil",
                    desc="Artefact divin. L'arc d'or du dieu du soleil. Ses flèches embrasent et soignent l'archer de 6 % des dégâts infligés. "
                         "Signature, Flèche du Soleil : une immense flèche d'or file sur 80 blocs, transperce tout, embrase son sillage et explose "
                         "en un soleil au bout de sa course.",
                    anims=("ShootCharging", "ShootCharged"), spell="FlecheDuSoleil", trail=None, arrow="Apollon_Fleche"),
}


# ---------------------------------------------------------------------------------------------------------------------
# Item
# ---------------------------------------------------------------------------------------------------------------------
INTERACTION_FILES = {}


def vanilla_interaction(name):
    """Fichier d'interaction vanilla de ce nom (pour ses degats)."""
    if not INTERACTION_FILES:
        for n in NAMES:
            if n.startswith("Server/Item/Interactions/") and n.endswith(".json"):
                INTERACTION_FILES[os.path.basename(n)[:-5]] = n
    p = INTERACTION_FILES.get(name)
    return zjson(p) if p else None


def scale_vars(variables, factor=None):
    """Degats x DIVINE_FACTOR ; les variables-chaines de degats deviennent des surcharges avec la valeur vanilla x5 ;
    chaque coup charge l'energie de signature."""
    out = {}
    for k, v in variables.items():
        if isinstance(v, str) and k.endswith("_Damage"):
            base = vanilla_interaction(v)
            dmg = base.get("DamageCalculator", {}).get("BaseDamage") if base else None
            if dmg:
                v = {"Interactions": [{"Parent": v, "DamageCalculator": {"BaseDamage": dict(dmg)}}]}
        if isinstance(v, dict):
            v = copy.deepcopy(v)
            for it in v.get("Interactions", []):
                if not isinstance(it, dict):
                    continue
                dc = it.get("DamageCalculator", {}).get("BaseDamage")
                if dc:
                    for t in dc:
                        dc[t] = round(dc[t] * (factor or DIVINE_FACTOR), 1)
                    it.setdefault("EntityStatsOnHit", [{"EntityStatId": "SignatureEnergy", "Amount": 4}])
                cfg = it.get("Config")
                if isinstance(cfg, dict) and "EntityDamage" in cfg:
                    cfg["EntityDamage"] = round(cfg["EntityDamage"] * (factor or DIVINE_FACTOR), 1)
        out[k] = v
    return out


SIGNATURE_READY = {
    "SignatureEnergy": [{
        "Condition": [100, 100], "ConditionValueType": "Percent",
        "Particles": [{"SystemId": "Sword_Signature_Ready", "TargetNodeName": "Handle", "PositionOffset": {"X": 0.8}, "TargetEntityPart": "PrimaryItem"},
                      {"SystemId": "Sword_Signature_Status", "TargetNodeName": "Handle", "PositionOffset": {"X": 0.6}, "TargetEntityPart": "PrimaryItem", "Scale": 1.3}],
        "FirstPersonParticles": [{"SystemId": "Sword_Signature_Ready", "TargetNodeName": "Handle", "PositionOffset": {"X": 0.6}, "TargetEntityPart": "PrimaryItem"}],
        "ModelVFXId": "Sword_Signature_Status"}]
}


def write_item(key, spec):
    raw = zjson("Server/Item/Items/Weapon/%s.json" % spec["ref"])
    item = {k: v for k, v in raw.items() if k not in ("Recipe", "TranslationProperties", "Variant")}
    item["TranslationProperties"] = {"Name": spec["name"], "Description": spec["desc"]}
    item.update({"Quality": spec.get("quality", "Divin"), "ItemLevel": spec.get("level", 800),
                 "Model": "%s/%s.blockymodel" % (MODEL_REL, spec["id"]), "Texture": "%s/%s.png" % (MODEL_REL, spec["id"]),
                 "Icon": "Icons/ItemsGenerated/%s.png" % spec["id"], "Categories": ["Eldanior_System.Artefacts", "Items.Weapons"],
                 "MaxStack": 1, "Variant": False, "MaxDurability": 0, "DurabilityLossOnHit": 0})
    pri = spec.get("primary")
    if pri:
        for v in pri["vars"]:
            item.setdefault("InteractionVars", {}).setdefault(v, v)
    if spec.get("anim_set"):
        item["PlayerAnimationsId"] = spec["anim_set"]
    if spec.get("arrow"):                                               # l'arc tire nos fleches
        item.setdefault("InteractionVars", {}).update(bow_shots(key, spec, spec["arrow"]))
    if "InteractionVars" in item:
        item["InteractionVars"] = scale_vars(item["InteractionVars"], spec.get("factor"))   # x5 divin, x3,8 legendaire
    inter = dict(item.get("Interactions") or {})
    inter["Ability1"] = "Root_Divin_%s_Signature" % key
    if pri:
        inter["Primary"] = "Root_Divin_%s_Attack" % key
    item["Interactions"] = inter
    if "Parent" not in raw and not spec.get("cooldown"):                # le template garde son bloc Weapon (deux mains, munitions...)
        weapon = item.setdefault("Weapon", {})
        if "SignatureEnergy" not in json.dumps(weapon):
            weapon.setdefault("EntityStatsToClear", []).append("SignatureEnergy")
            weapon.setdefault("StatModifiers", {})["SignatureEnergy"] = [{"Amount": 20, "CalculationType": "Additive"}]
    if "Parent" not in raw and not spec.get("cooldown"):              # pas de template : l'aura de signature prete
        conds = item.setdefault("ItemAppearanceConditions", {})
        conds.update(copy.deepcopy(SIGNATURE_READY))
    if spec["trail"]:
        item["Trails"] = [{"TrailId": "Divin_%s_Trail" % key, "TargetNodeName": "Handle", "TargetEntityPart": "PrimaryItem",
                           "PositionOffset": {"X": 0.8, "Y": 0}, "RotationOffset": {"Roll": 90}}]
        start, end = spec["trail"]
        trail = {"TexturePath": "Trails/Basic.png", "LifeSpan": 6, "Roll": 0, "Start": {"Color": "rgba(%s, 1)" % start, "Width": 0.3},
                 "End": {"Color": "rgba(%s, 0.3)" % end, "Width": 0}, "LightInfluence": 0, "Smooth": True, "RenderMode": "BlendLinear"}
        json.dump(trail, open(os.path.join(RES, "Server", "Entity", "Trails", "Divin_%s_Trail.json" % key), "w"), indent=2)
    path = os.path.join(RES, "Server", "Item", "Items", "Weapon", spec["folder"], spec["id"] + ".json")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    json.dump(item, open(path, "w"), indent=2, ensure_ascii=False)

    charge, strike = spec["anims"]
    root = {"RequireNewClick": True, "Interactions": ["Divin_%s_Signature" % key]}
    files = []
    if pri:
        attack = {"$Comment": "%s : clic = enchainement ; maintien (%.1f s) = sort Java %s. Genere par gen_divines.py." % (spec["name"], pri["hold"], pri["hold_spell"]),
                  "Type": "Charging", "Effects": {"ItemAnimationId": pri["charge_anim"], "ClearAnimationOnFinish": True}, "AllowIndefiniteHold": True,
                  "Next": {"0": {"Type": "Chaining", "ChainingAllowance": pri["allowance"], "Next": pri["chain"]}, str(pri["hold"]): "Divin_%s_Hold" % key}}
        hold = {"Type": "Simple", "Effects": {"ItemAnimationId": pri["hold_anim"]}, "RunTime": 0,
                "Next": {"Type": "RuneSpell", "RunTime": 0.05, "Spell": pri["hold_spell"]}}
        files += [("RootInteractions", {"Interactions": ["Divin_%s_Attack" % key], "Tags": {"Attack": ["Melee"]}}, "Root_Divin_%s_Attack" % key),
                  ("Interactions", attack, "Divin_%s_Attack" % key), ("Interactions", hold, "Divin_%s_Hold" % key)]
    if spec.get("cooldown"):
        root["Cooldown"] = {"Cooldown": spec["cooldown"]}
    sig = {"$Comment": "Signature de %s (energie de signature pleine) : sort Java %s (ArmesDivines). Genere par gen_divines.py." % (spec["name"], spec["spell"]),
           "Type": "StatsCondition", "Costs": {"SignatureEnergy": 100}, "ValueType": "Percent",
           "Next": {"Type": "Serial", "Interactions": [
               {"Type": "Simple", "RunTime": 0.45, "Effects": {"ItemAnimationId": charge}},
               {"Type": "Simple", "RunTime": 0.3, "Effects": {"ItemAnimationId": strike, "ClearAnimationOnFinish": True}},
               {"Type": "RuneSpell", "RunTime": 0.05, "Spell": spec["spell"]},
               {"Type": "ChangeStat", "StatModifiers": {"SignatureEnergy": -100}, "ValueType": "Percent"}]}}
    if spec.get("cooldown"):                                           # sans energie de signature : recharge de la racine
        sig = sig["Next"]
        sig["Interactions"] = [i for i in sig["Interactions"] if i["Type"] != "ChangeStat"]
        sig["$Comment"] = "Competence de %s (recharge %d s) : sort Java %s. Genere par gen_divines.py." % (spec["name"], spec["cooldown"], spec["spell"])
    files += [("RootInteractions", root, "Root_Divin_%s_Signature" % key), ("Interactions", sig, "Divin_%s_Signature" % key)]
    for sub, obj, name in files:
        d = os.path.join(RES, "Server", "Item", sub, "Weapons", "Divin")
        os.makedirs(d, exist_ok=True)
        json.dump(obj, open(os.path.join(d, name + ".json"), "w"), indent=2, ensure_ascii=False)


# ---------------------------------------------------------------------------------------------------------------------
# Assets des sorts : croissant de jade (Guandao), baton geant (Ruyi), petrification (Egide)
# ---------------------------------------------------------------------------------------------------------------------
def energy_blade(name, ramp, roll, light):
    base = zjson("Common/Items/Projectiles/WindStrike_Projectile.blockymodel")
    with tempfile.NamedTemporaryFile(suffix=".png", delete=False) as f:
        f.write(Z.read("Common/Items/Projectiles/WindStrike_Projectile_Texture.png"))
    w, h, px = png.read(f.name)
    os.unlink(f.name)
    lo, hi = ramp
    for row in px:
        for c in row:
            lum = ((0.3 * c[0] + 0.59 * c[1] + 0.11 * c[2]) / 255) ** 3
            c[0], c[1], c[2] = [int(lo[i] + (hi[i] - lo[i]) * lum) for i in range(3)]
    out = os.path.join(RES, "Common", "Items", "Projectiles")
    png.write(os.path.join(out, name + ".png"), w, h, px)
    ids = iter(range(1000, 2000))

    def renum(n):
        n["id"] = str(next(ids))
        for c in n.get("children", []):
            renum(c)
    kids = copy.deepcopy(base["nodes"][0]["children"])
    for c in kids:
        renum(c)
    model = copy.deepcopy(base)
    model["nodes"][0]["children"] = [{"id": "999", "name": "Lame", "position": {"x": 0, "y": 0, "z": 0},
                                      "orientation": K.round_q(qz(roll)), "shape": empty(), "children": kids}]
    json.dump(model, open(os.path.join(out, name + ".blockymodel"), "w"), indent=1)
    write_carrier(name, light)


def write_carrier(name, light, hitbox=1.2):
    asset = {"$Comment": "Sort d'arme divine, deplace par le code (ArmesDivines). HitBox elargie (LOD serveur). Genere par gen_divines.py.",
             "Model": "Items/Projectiles/" + name + ".blockymodel", "Texture": "Items/Projectiles/" + name + ".png",
             "MinScale": 1.0, "MaxScale": 1.0,
             "HitBox": {"Max": {"X": hitbox, "Y": hitbox, "Z": hitbox}, "Min": {"X": -hitbox, "Y": -hitbox, "Z": -hitbox}},
             "Light": {"Color": light, "Radius": 8}}
    json.dump(asset, open(os.path.join(RES, "Server", "Models", "Projectiles", "Abilities", name + ".json"), "w"), indent=2)
    neutral = {"Interactions": [{"Type": "Simple", "RunTime": 0.01}]}
    config = {"$Comment": "Porteur de modele deplace par le code (ArmesDivines). Interactions neutres obligatoires.",
              "Model": name, "UseModelScale": True,
              "Physics": {"Type": "Standard", "Gravity": 0, "TerminalVelocityAir": 1, "TerminalVelocityWater": 1, "Bounciness": 0, "SticksVertically": False},
              "LaunchForce": 0, "SpawnOffset": {"X": 0, "Y": 0, "Z": 0},
              "Interactions": {"ProjectileHit": neutral, "ProjectileMiss": neutral}}
    json.dump(config, open(os.path.join(RES, "Server", "ProjectileConfigs", "Abilities", "Projectile_Config_" + name + ".json"), "w"), indent=2)


def giant_staff():
    """Baton geant du Ruyi : laque rouge aux nuages d'or, viroles d'or gravees, anneaux, embouts ; le long de z, centre."""
    global M
    M = K.Model("Ruyi_Geant")
    M.reuse = True
    lac = ruyi_lacquer()
    kids = [box("Bar", (0, 0, 0), (104, 6, 6), lac, q=qy(90))]
    for s_ in (1, -1):
        kids.append(box("Cap_%d" % s_, (0, 0, 58 * s_), (14, 7.5, 7.5), runes(GOLD, (60, 30, 10), 5), q=qy(90)))
        kids.append(box("Ring_%d" % s_, (0, 0, 50.25 * s_), (1.5, 8.5, 8.5), metal(GOLD_LIGHT), q=K.qmul(qy(90), qx(45))))
        kids.append(box("End_%d" % s_, (0, 0, 65.75 * s_), (1.5, 6.5, 6.5), metal(GOLD_LIGHT), q=qy(90)))
        kids.append(box("Band_%d" % s_, (0, 0, 20 * s_), (2, 7, 7), metal(GOLD), q=qy(90)))
    root = pivot("Root", (0, 0, 0), ID_Q, kids)
    renumber(root)
    W, H, tex = M.texture()
    out = os.path.join(RES, "Common", "Items", "Projectiles")
    png.write(os.path.join(out, "Ruyi_Geant.png"), W, H, tex)
    json.dump({"nodes": [root], "lod": "auto"}, open(os.path.join(out, "Ruyi_Geant.blockymodel"), "w"), indent=1)
    write_carrier("Ruyi_Geant", "#ffb040", 1.0)


def petrification():
    eff = {"$Comment": "Petrification (Regard de Meduse, Egide) : la cible est changee en pierre, immobile et sans attaque. Duree donnee par le code.",
           "Duration": 4.0, "OverlapBehavior": "Overwrite", "Debuff": True,
           "ApplicationEffects": {"EntityTopTint": "#b8b4ac", "EntityBottomTint": "#4a4640",
                                  "Particles": [{"SystemId": "Divine_Smoke_Short", "TargetEntityPart": "Entity"}],
                                  "MovementEffects": {"DisableAll": True}, "KnockbackMultiplier": 0,
                                  "AbilityEffects": {"Disabled": ["Primary", "Secondary", "Ability1", "Ability3"]}}}
    d = os.path.join(RES, "Server", "Entity", "Effects", "Eldanior", "Armes")
    json.dump(eff, open(os.path.join(d, "Divin_Petrification.json"), "w"), indent=2, ensure_ascii=False)


# ---------------------------------------------------------------------------------------------------------------------
# Fleches divines, soleil de Ra, faisceau, baton geant (assets des sorts)
# ---------------------------------------------------------------------------------------------------------------------
def rivets(name, points, size=1.25, paint=None):
    return [box("%s_%d" % (name, i), p, (size, size, size), paint or metal(GOLD_LIGHT), q=qx(45)) for i, p in enumerate(points)]


def arrow_model(name, shaft, band, head, head_glow, fletch, fletch_core):
    """Fleche sur la hierarchie de la fleche vanilla (Arrow.blockymodel) : fer en deux plaques croisees et pointe
    lumineuse, viroles, trois plumes lumineuses a 120 degres, encoche."""
    global M
    M = K.Model(name)
    M.reuse = True
    kids = [box("Collar", (27, 0, 0), (2.5, 3, 3), metal(band)),
            box("Head_A", (32, 0, 0), (9, 6.5, 0.75), metal(head)),
            box("Head_B", (32, 0, 0), (9, 0.75, 6.5), metal(head)),
            box("Barb_A", (28.5, 0, 0), (3, 8, 0.5), metal(head)),
            box("Barb_B", (28.5, 0, 0), (3, 0.5, 8), metal(head)),
            lit("Head_Core", (31.5, 0, 0), (6, 2, 2), glow(head_glow)),
            lit("Tip", (38, 0, 0), (4, 1.5, 1.5), glow(head_glow, (255, 255, 255)), q=qx(45)),
            box("Nock", (-29.5, 0, 0), (2, 2.5, 2.5), metal(band))]
    for i, x in enumerate((-20, 0, 18)):
        kids += ring_x("Band_%d" % i, x, 1.25, 1.0, metal(band))
    for k in range(3):
        kids.append(pivot("Fletch_%d" % k, (-24, 0, 0), qx(k * 120),
                          [lit("Feather_%d" % k, (0, 1.75, 0), (14, 2.75, 0.4), glow(fletch, fletch_core))]))
    root, handle = mount("Items/Weapons/Arrow/Arrow.blockymodel", (57, 1.5, 1.5), stripes(shaft, band, 14, 1), kids)
    renumber(root)
    W, H, tex = M.texture()
    out = os.path.join(RES, "Common", "Items", "Projectiles")
    png.write(os.path.join(out, name + ".png"), W, H, tex)
    json.dump({"nodes": [root], "lod": "auto"}, open(os.path.join(out, name + ".blockymodel"), "w"), indent=1)
    return root, handle


def bow_shots(key, spec, arrow):
    """Tir normal de l'arc : nos fleches (configs de projectile filles des vanilla, seul le modele change)."""
    configs = {}
    for n in range(5):
        cid = "Projectile_Config_Divin_%s_Strength_%d" % (key, n)
        cfg = {"$Comment": "Fleche de %s (tir de force %d) : la fleche vanilla, avec notre modele. Genere par gen_divines.py." % (spec["name"], n),
               "Parent": "Projectile_Config_Arrow_Shortbow_Strength_%d" % n, "Model": arrow}
        json.dump(cfg, open(os.path.join(RES, "Server", "ProjectileConfigs", "Abilities", cid + ".json"), "w"), indent=2, ensure_ascii=False)
        configs["Primary_Shoot_Strength_%d" % n] = {"Interactions": [{"Parent": "Weapon_Shortbow_Primary_Shoot_Strength_%d" % n, "Config": cid}]}
    return configs


def ra_sun():
    """Soleil de Ra : boule jaune incandescente (trois cubes croises), couronne de rayons, anneau de feu."""
    global M
    M = K.Model("Ra_Soleil")
    M.reuse = True
    kids = [lit("Core_%d" % i, (0, 0, 0), (12, 12, 12), glow((255, 210, 40), (255, 250, 210)), q=q) for i, q in enumerate((ID_Q, qx(45), qy(45)))]
    kids.append(lit("Heart", (0, 0, 0), (9, 9, 9), glow((255, 250, 220), (255, 255, 255)), q=K.qmul(qx(45), qy(45))))
    dirs = []
    for i in range(14):                                                  # rayons repartis sur la sphere
        yv = 1 - 2 * (i + 0.5) / 14
        r = math.sqrt(1 - yv * yv)
        a = i * 2.39996
        dirs.append((math.cos(a) * r, yv, math.sin(a) * r))
    for i, d in enumerate(dirs):
        L = 9 if i % 2 else 14
        kids.append(lit("Ray_%d" % i, (d[0] * (8 + L / 2.0), d[1] * (8 + L / 2.0), d[2] * (8 + L / 2.0)), (2.5, L, 2.5),
                        glow((255, 150, 20), (255, 230, 120)), q=K.q_from_y(d)))
    for k in range(16):
        a = math.radians(k * 22.5)
        kids.append(lit("Halo_%d" % k, (math.cos(a) * 15, 0, math.sin(a) * 15), (6.5, 1.5, 1.5), glow((255, 120, 10), (255, 200, 80)), q=qy(-k * 22.5 + 90)))
    root = pivot("Root", (0, 0, 0), ID_Q, kids)
    renumber(root)
    W, H, tex = M.texture()
    out = os.path.join(RES, "Common", "Items", "Projectiles")
    png.write(os.path.join(out, "Ra_Soleil.png"), W, H, tex)
    json.dump({"nodes": [root], "lod": "auto"}, open(os.path.join(out, "Ra_Soleil.blockymodel"), "w"), indent=1)
    write_carrier("Ra_Soleil", "#ffd060", 1.2)
    # faisceau des rayons : coeur blanc-jaune, bords dores qui s'effacent
    w, h = 256, 96
    img = []
    for y in range(h):
        v = abs((y + 0.5) / h - 0.5) * 2
        row = []
        for x in range(w):
            flicker = 0.85 + 0.15 * K.noise(x * 0.06, y * 0.2)
            a = max(0.0, 1 - v ** 1.6) * flicker
            c = mixc((255, 255, 235), (255, 170, 30), min(1.0, v * 1.4))
            row.append([int(c[0]), int(c[1]), int(c[2]), int(255 * a)])
        img.append(row)
    os.makedirs(os.path.join(RES, "Common", "Trails"), exist_ok=True)
    png.write(os.path.join(RES, "Common", "Trails", "Ra_Rayon.png"), w, h, img)
    os.makedirs(os.path.join(RES, "Server", "Entity", "Beams"), exist_ok=True)
    json.dump({"TexturePath": "Trails/Ra_Rayon.png"}, open(os.path.join(RES, "Server", "Entity", "Beams", "Ra_Rayon.json"), "w"), indent=2)


# ---------------------------------------------------------------------------------------------------------------------
# Rendu (icones, apercus)
# ---------------------------------------------------------------------------------------------------------------------
def q_from_to(a, b):
    ax = (a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0])
    s = math.sqrt(sum(c * c for c in ax))
    d = sum(a[i] * b[i] for i in range(3))
    if s < 1e-6:
        return dict(ID_Q) if d > 0 else K.q_axis((0, 1, 0), 180)
    return K.q_axis(tuple(c / s for c in ax), math.degrees(math.atan2(s, d)))


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


def handle_quads(handle, tex, q):
    h = copy.deepcopy(handle)
    h["position"] = {"x": 0, "y": 0, "z": 0}
    h["orientation"] = dict(ID_Q)
    return K.quads_of(h, (0, 0, 0), q, tex, ("\0",))


def render(key, handle, tex):
    lifted = [[[min(255, c[0] * 1.15 + 14), min(255, c[1] * 1.15 + 14), min(255, c[2] * 1.15 + 16), c[3]] for c in row] for row in tex]
    if key == "Egide":
        normal = K.qrot({"x": 0.43, "y": 0.561, "z": -0.43, "w": 0.561}, (1, 0, 0))
        q = q_from_to(normal, (0, 0, 1))
        yaw, pitch = 20, 15
    else:
        q = K.q_axis((0, 0, 1), 45)
        yaw, pitch = 15, 10
    big = K.draw(handle_quads(handle, lifted, q), 256, yaw, pitch, margin=0.04)
    icon = outline(K.downscale(big, 4))
    face = K.draw(handle_quads(handle, tex, K.ID_Q if key != "Egide" else q), 384, 0 if key != "Egide" else 20, 0 if key != "Egide" else 15, margin=0.04)
    return icon, face


# ---------------------------------------------------------------------------------------------------------------------
def main():
    global M
    os.makedirs(os.path.join(RES, "Common", MODEL_REL), exist_ok=True)
    sheet = []
    for key, spec in WEAPONS.items():
        if ONLY and key not in ONLY:
            continue
        M = K.Model(spec["id"])
        M.reuse = True
        root, handle = spec["build"]()
        renumber(root)
        fix_coplanar(handle, key)
        W, H, tex = M.texture()
        png.write(os.path.join(RES, "Common", MODEL_REL, spec["id"] + ".png"), W, H, tex)
        json.dump({"nodes": [root], "lod": "auto"}, open(os.path.join(RES, "Common", MODEL_REL, spec["id"] + ".blockymodel"), "w"), indent=1)
        icon, face = render(key, handle, tex)
        png.write(os.path.join(RES, "Common", "Icons", "ItemsGenerated", spec["id"] + ".png"), 64, 64, icon)
        write_item(key, spec)
        sheet.append((key, icon, face))
        print("%-10s %-36s texture %dx%d" % (key, spec["id"], W, H))
    if not ONLY or "Guandao" in ONLY:
        energy_blade("Guandao_Croissant", ((10, 70, 40), (200, 255, 220)), 0, "#60ffa0")
        write_attached("Guandao_Vol", "Artefact_Spear_Guandao", "#80ffb0", "Gilgamesh_Sword_Glow")
    if not ONLY or "Mjollnir" in ONLY:
        write_attached("Mjollnir_Vol", "Artefact_Mace_Mjollnir", "#a0d0ff", "Gilgamesh_Sword_Glow")
    if not ONLY or "Thanatos" in ONLY:
        spectral_scythe()
    if not ONLY or "Ra" in ONLY:
        ra_sun()
    if not ONLY or "Artemis" in ONLY:
        divine_arrow("Artemis_Fleche", ((214, 222, 238), (120, 150, 220), (230, 236, 250), MOON, MOON, MOON_CORE), ("#eef6ff", "#5070ff", "Trails/Ice.png"), "#c8e0ff")
    if not ONLY or "Apollon" in ONLY:
        divine_arrow("Apollon_Fleche", (GOLD, (190, 60, 20), GOLD_LIGHT, SUN, (255, 120, 20), SUN_CORE), ("#fff2b0", "#ff6010", "Trails/Gold.png"), "#ffc060", "Gilgamesh_Sword_Glow")
    if not ONLY or "Ruyi" in ONLY:
        giant_staff()
    if not ONLY or "Egide" in ONLY:
        petrification()
    if PREVIEW and sheet:
        os.makedirs(PREVIEW, exist_ok=True)
        cols = 5
        rows = (len(sheet) + cols - 1) // cols
        S = 384
        img = [[[44, 44, 52, 255] for _ in range(cols * S)] for _ in range(rows * (S + 128))]
        for i, (key, icon, face) in enumerate(sheet):
            ox, oy = (i % cols) * S, (i // cols) * (S + 128)
            for y in range(S):
                for x in range(S):
                    p = face[y][x]
                    if p[3] > 0:
                        img[oy + y][ox + x] = list(p[:3]) + [255]
            for y in range(128):
                for x in range(128):
                    p = icon[y // 2][x // 2]
                    if p[3] > 0:
                        img[oy + S + y][ox + 128 + x] = list(p[:3]) + [255]
        png.write(os.path.join(PREVIEW, "divines_planche.png"), cols * S, rows * (S + 128), img)
    print("ok")


if __name__ == "__main__":                                       # importable (outils de modelisation : gen_aigles_akimbo.py)
    main()
