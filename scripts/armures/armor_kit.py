"""Boite a outils des armures Eldanior : peinture procedurale, atlas et noeuds .blockymodel, rendu sur le rig du joueur,
ecriture des tenues (modeles, textures, icones, items). Utilisee par gen_kimono.py et gen_runkandel.py.

Regles du rig (mesurees sur les 47 modeles d'armure vanilla) :
  - un noeud "isPiece" se colle a l'os du joueur de meme nom : sa position et son offset propres sont ignores en jeu ;
  - ses enfants sont places par rapport au CENTRE DE FORME DE L'OS DU JOUEUR (pivot + offset de Player.blockymodel).
Face avant = +Z. Cote droit du porteur = -X. Cote gauche = miroir du droit (etirement x negatif, meme texture).

Densite : 2 px par unite (D), soit le double des armures vanilla : une forme de N unites a N*D px et un etirement 1/D.
Les faces laterales gauche et droite partagent leur peinture : le sens de leurs UV n'a pas ete verifie, donc aucun motif
asymetrique avant/arriere sur ces faces.
"""
import copy, json, math, os, random, shutil, sys, zipfile

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "rune_aura_icons"))
import png  # noqa: E402
import familles  # noqa: E402  (meme dossier : categorie creative des tenues de famille)

ASSETS = os.path.expanduser("~/Library/Application Support/Hytale/install/release/package/game/latest/Assets.zip")
ATLAS_W = 512
D = 2


def set_density(density, atlas_width):
    """Densite d'un generateur : a appeler avant de construire les modeles. Les kimonos restent a 2 px par unite."""
    global D, ATLAS_W
    D, ATLAS_W = density, atlas_width

# Communes
IVORY = (238, 230, 210)
IVORY_SHADE = (196, 186, 168)
CRIMSON = (170, 30, 50)
CRIMSON_DARK = (104, 18, 34)
GOLD = (232, 188, 92)
GOLD_LIGHT = (255, 230, 150)
GOLD_DARK = (160, 116, 50)
STRAW = (206, 170, 104)
STRAW_LIGHT = (232, 202, 140)
STRAW_DARK = (146, 110, 60)
LINING = (24, 22, 34)
LACQUER = (24, 22, 30)
THREAD = (222, 212, 188)

FACE_LIGHT = {"front": 1.0, "back": 0.82, "left": 0.9, "right": 0.9, "top": 1.1, "bottom": 0.62}


def clamp(v):
    return max(0, min(255, int(round(v))))


def mix(a, b, t):
    t = max(0.0, min(1.0, t))
    return tuple(a[i] + (b[i] - a[i]) * t for i in range(3))


def smooth(t):
    t = max(0.0, min(1.0, t))
    return t * t * (3 - 2 * t)


_rng = random.Random(11)
_grid = [[_rng.random() for _ in range(128)] for _ in range(128)]


def noise(x, y):
    ix, iy = math.floor(x), math.floor(y)
    fx, fy = smooth(x - ix), smooth(y - iy)
    a, b = _grid[iy & 127][ix & 127], _grid[iy & 127][(ix + 1) & 127]
    c, d = _grid[(iy + 1) & 127][ix & 127], _grid[(iy + 1) & 127][(ix + 1) & 127]
    return (a + (b - a) * fx) * (1 - fy) + (c + (d - c) * fx) * fy


def fbm(x, y):
    return 0.6 * noise(x, y) + 0.3 * noise(x * 2.1, y * 2.1) + 0.1 * noise(x * 4.3, y * 4.3)


# ---------------------------------------------------------------------------------------------------------------------
# Toile : image = lignes de [r, g, b, a]
# ---------------------------------------------------------------------------------------------------------------------
def canvas(w, h, c):
    return [[[c[0], c[1], c[2], 255] for _ in range(w)] for _ in range(h)]


def put(img, x, y, c, a=1.0):
    x, y = int(x), int(y)
    if 0 <= y < len(img) and 0 <= x < len(img[0]):
        p = img[y][x]
        for i in range(3):
            p[i] = p[i] + (c[i] - p[i]) * a


def disc(img, cx, cy, r, c, a=1.0):
    for y in range(int(cy - r - 1), int(cy + r + 2)):
        for x in range(int(cx - r - 1), int(cx + r + 2)):
            if math.hypot(x + 0.5 - cx, y + 0.5 - cy) <= r:
                put(img, x, y, c, a)


def stroke(img, pts, c, r=0.6):
    for (x0, y0), (x1, y1) in zip(pts, pts[1:]):
        n = max(1, int(math.hypot(x1 - x0, y1 - y0) * 2))
        for i in range(n + 1):
            t = i / float(n)
            disc(img, x0 + (x1 - x0) * t, y0 + (y1 - y0) * t, r, c)


def rect(img, x0, y0, x1, y1, c, a=1.0):
    for y in range(int(y0), int(y1)):
        for x in range(int(x0), int(x1)):
            put(img, x, y, c, a)


def scale_rows(img, y0, y1, k):
    h = len(img)
    for y in range(max(0, int(y0)), min(h, int(y1))):
        for p in img[y]:
            for i in range(3):
                p[i] *= k


def finish(img, face, light=1.0, edges=True):
    """Ombres peintes (shadingMode flat) : lumiere zenithale, volume arrondi, aretes assombries."""
    h, w = len(img), len(img[0])
    for y, row in enumerate(img):
        for x, p in enumerate(row):
            u, v = (x + 0.5) / w, (y + 0.5) / h
            k = FACE_LIGHT[face] * light
            if face in ("top", "bottom"):
                k *= 0.9 + 0.1 * math.sin(math.pi * u) * math.sin(math.pi * v)
            else:
                k *= 1.1 - 0.24 * v
                k *= 0.82 + 0.18 * math.sin(math.pi * u) ** 0.7
            if edges and min(x, y, w - 1 - x, h - 1 - y) < 2 and min(w, h) > 6:
                k *= 0.86
            p[0], p[1], p[2] = clamp(p[0] * k), clamp(p[1] * k), clamp(p[2] * k)
    return img


def cloth(w, h, color, seed, fold=0.16, grain=0.035):
    """Tissu : plis verticaux irreguliers, tissage fin. color = couleur, ou fonction de v (0 en haut, 1 en bas)."""
    img = []
    for y in range(h):
        c = color(y / max(1.0, h - 1.0)) if callable(color) else color
        row = []
        for x in range(w):
            f = fbm(x * 0.05 + seed * 0.31, y * 0.014 + seed * 0.17)
            g = noise(x * 0.8 + seed, y * 0.8)
            k = 1 + fold * (f - 0.5) * 2.6 + grain * (g - 0.5) * 2 + (grain * 0.5 if (x + y) % 2 else -grain * 0.5)
            row.append([c[0] * k, c[1] * k, c[2] * k, 255])
        img.append(row)
    return img


def band(img, y0, y1, c):
    rect(img, 0, y0, len(img[0]), y1, c)



def lozenges(img, y0, y1, step, c, a=1.0):
    for y in range(int(y0), int(y1)):
        for x in range(len(img[0])):
            if (x + y) % step == 0 or (x - y) % step == 0:
                put(img, x, y, c, a)


def border(img, inset, c):
    h, w = len(img), len(img[0])
    for y in range(h):
        for x in range(w):
            if min(x, y, w - 1 - x, h - 1 - y) == inset:
                put(img, x, y, c)


def stitch_line(img, pts, c=THREAD, dash=3):
    n = 0
    for (x0, y0), (x1, y1) in zip(pts, pts[1:]):
        steps = int(max(abs(x1 - x0), abs(y1 - y0))) or 1
        for i in range(steps):
            if n % (dash * 2) < dash:
                put(img, x0 + (x1 - x0) * i / steps, y0 + (y1 - y0) * i / steps, c)
            n += 1


def rope_tex(img, c0, c1, period=6):
    for y, row in enumerate(img):
        for x, p in enumerate(row):
            t = ((x + y) % period) / float(period)
            c = mix(c1, c0, math.sin(t * math.pi))
            p[0], p[1], p[2] = c


def torn(img, seed, depth, amp=0.0):
    """Bas dechire : colonnes coupees irregulierement, bord effiloche assombri."""
    h, w = len(img), len(img[0])
    for x in range(w):
        cut = int(depth * (0.25 + 0.75 * fbm(x * 0.22 + seed, seed * 0.13)))
        for y in range(h - cut, h):
            img[y][x][3] = 0
        for y in range(max(0, h - cut - 2), h - cut):
            for i in range(3):
                img[y][x][i] *= 0.68


def plain(color, light=1.0):
    def paint(face, w, h, seed):
        return finish(cloth(w, h, color, seed, 0.07, 0.02), face, light)
    return paint


def metal(color=GOLD, light=1.0):
    def paint(face, w, h, seed):
        img = canvas(w, h, color)
        for y in range(h):
            for x in range(w):
                k = 1 + 0.2 * math.sin((x * 0.7 + y * 1.1)) * (0.5 + noise(x * 0.3, y * 0.3))
                for i in range(3):
                    img[y][x][i] *= k
        return finish(img, face, light)
    return paint


def lacquer(gold_edge=False):
    def paint(face, w, h, seed):
        img = canvas(w, h, LACQUER)
        for y in range(h):
            for x in range(w):
                s = max(0.0, math.sin((x + y * 0.6) * 5.0 / max(w, 8))) ** 6
                img[y][x][:3] = list(mix(LACQUER, (120, 118, 140), s * 0.65))
        if gold_edge and face not in ("top", "bottom"):
            band(img, h - 3, h - 1, GOLD)
        return finish(img, face, edges=False)
    return paint


def stripes(colors):
    """Rubans longitudinaux (cols) : colors = [(fraction de largeur, couleur), ...]."""
    def paint(face, w, h, seed):
        img = cloth(w, h, colors[0][1], seed, 0.05, 0.02)
        if face in ("front", "back"):
            x = 0.0
            for frac, c in colors:
                x1 = x + frac * w
                for yy in range(h):
                    for xx in range(int(x), int(x1)):
                        img[yy][xx][:3] = list(c)
                x = x1
            stitch_line(img, [(1, 0), (1, h)], GOLD_DARK, 2)
        return finish(img, face, edges=False)
    return paint


def hem_band(ground, line):
    def paint(face, w, h, seed):
        img = canvas(w, h, ground)
        if face not in ("top", "bottom"):
            band(img, 0, 1, line)
            band(img, h - 1, h, tuple(v * 0.6 for v in ground))
        return finish(img, face, edges=False)
    return paint


def wraps(light=IVORY, shade=IVORY_SHADE, edge=None):
    """Bandes de tissu enroulees en biais, chacune ombree par la suivante."""
    def paint(face, w, h, seed):
        img = canvas(w, h, light)
        if face not in ("top", "bottom"):
            for y in range(h):
                for x in range(w):
                    p = (y + x // 4) % 8
                    img[y][x][:3] = list(mix(light, shade, p / 7.0))
                    if p == 0:
                        img[y][x][:3] = list(tuple(v * 0.7 for v in shade))
            if edge:
                band(img, 0, 2, edge)
        return finish(img, face)
    return paint


# ---------------------------------------------------------------------------------------------------------------------
# Atlas et noeuds
# ---------------------------------------------------------------------------------------------------------------------
class Model:
    def __init__(self, name):
        self.name = name
        self.x = self.y = self.row = 0
        self.blits = []
        self.ids = 0
        self.cache = {}                                          # peintures deja posees, si reuse est actif
        self.reuse = False

    def put(self, img):
        h, w = len(img), len(img[0])
        if self.x + w > ATLAS_W:
            self.x, self.y, self.row = 0, self.y + self.row + 1, 0
        pos = (self.x, self.y)
        self.blits.append((pos, img))
        self.x += w + 1
        self.row = max(self.row, h)
        return pos

    def texture(self):
        H = (self.y + self.row + 31) // 32 * 32
        tex = [[[0, 0, 0, 0] for _ in range(ATLAS_W)] for _ in range(H)]
        for (x0, y0), img in self.blits:
            for y, row in enumerate(img):
                for x, c in enumerate(row):
                    tex[y0 + y][x0 + x] = [clamp(c[0]), clamp(c[1]), clamp(c[2]), clamp(c[3])]
        return ATLAS_W, H, tex

    def nid(self):
        self.ids += 1
        return str(self.ids)


ID_Q = {"x": 0, "y": 0, "z": 0, "w": 1}


def vec(v):
    return {"x": round(v[0], 3), "y": round(v[1], 3), "z": round(v[2], 3)}


def uv(pos):
    return {"offset": {"x": pos[0], "y": pos[1]}, "mirror": {"x": False, "y": False}, "angle": 0}


def q_axis(axis, deg):
    s, c = math.sin(math.radians(deg) / 2), math.cos(math.radians(deg) / 2)
    return {"x": axis[0] * s, "y": axis[1] * s, "z": axis[2] * s, "w": c}


def qmul(a, b):
    return {"w": a["w"] * b["w"] - a["x"] * b["x"] - a["y"] * b["y"] - a["z"] * b["z"],
            "x": a["w"] * b["x"] + a["x"] * b["w"] + a["y"] * b["z"] - a["z"] * b["y"],
            "y": a["w"] * b["y"] - a["x"] * b["z"] + a["y"] * b["w"] + a["z"] * b["x"],
            "z": a["w"] * b["z"] + a["x"] * b["y"] - a["y"] * b["x"] + a["z"] * b["w"]}


def qrot(q, v):
    u = (q["x"], q["y"], q["z"])
    c1 = (u[1] * v[2] - u[2] * v[1], u[2] * v[0] - u[0] * v[2], u[0] * v[1] - u[1] * v[0])
    c2 = (u[1] * c1[2] - u[2] * c1[1], u[2] * c1[0] - u[0] * c1[2], u[0] * c1[1] - u[1] * c1[0])
    return tuple(v[i] + 2 * (q["w"] * c1[i] + c2[i]) for i in range(3))


def q_from_y(d):
    """Rotation qui amene l'axe Y local sur la direction d."""
    n = math.sqrt(sum(c * c for c in d)) or 1.0
    d = tuple(c / n for c in d)
    axis = (d[2], 0.0, -d[0])
    s = math.hypot(axis[0], axis[2])
    if s < 1e-6:
        return dict(ID_Q) if d[1] > 0 else q_axis((1, 0, 0), 180)
    return q_axis((axis[0] / s, 0, axis[2] / s), math.degrees(math.atan2(s, d[1])))


def round_q(q):
    return {k: round(q[k], 6) for k in "xyzw"}


def piece(model, name, pos, children=(), offset=(0, 0, 0)):
    """Noeud colle a l'os du joueur de meme nom."""
    return {"id": model.nid(), "name": name, "position": vec(pos), "orientation": dict(ID_Q),
            "shape": {"type": "none", "offset": vec(offset), "stretch": {"x": 1, "y": 1, "z": 1}, "settings": {"isPiece": True},
                      "textureLayout": {}, "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": "flat"},
            "children": list(children)}


def hash_seed(s):
    return sum((i + 1) * ord(c) for i, c in enumerate(s)) % 977


def box(model, name, pos, size, paint, q=ID_Q, shading="flat", double=False, hidden=()):
    """size en unites ; la texture est a D px par unite. hidden : faces jamais visibles, qui ne prennent pas de place dans l'atlas."""
    sx, sy, sz = [max(1, int(round(s * D))) for s in size]
    dims = {"front": (sx, sy), "back": (sx, sy), "top": (sx, sz), "bottom": (sx, sz)}
    seed = hash_seed(model.name + name)

    def place(face, w, h, s):
        if face in hidden:
            return (0, 0)
        key = (id(paint), face, w, h)
        if model.reuse and key in model.cache:
            return model.cache[key]
        pos = model.put(paint(face, w, h, s))
        model.cache[key] = pos
        return pos

    if model.reuse or "left" in hidden:
        left = place("left", sz, sy, seed)
        layout = {"left": uv(left), "right": uv(left)}
    else:
        side = paint("left", sz, sy, seed)
        layout = {"left": uv(model.put(side)), "right": uv(model.put(side))}
    for i, (face, (w, h)) in enumerate(dims.items()):
        layout[face] = uv(place(face, w, h, seed + 3 * i + 1))
    s = 1.0 / D
    return {"id": model.nid(), "name": name, "position": vec(pos), "orientation": round_q(q),
            "shape": {"type": "box", "offset": vec((0, 0, 0)), "stretch": {"x": s, "y": s, "z": s}, "settings": {"size": {"x": sx, "y": sy, "z": sz}},
                      "textureLayout": layout, "unwrapMode": "custom", "visible": True, "doubleSided": double, "shadingMode": shading},
            "children": []}


def quad(model, name, pos, img, q=ID_Q):
    h, w = len(img), len(img[0])
    s = 1.0 / D
    return {"id": model.nid(), "name": name, "position": vec(pos), "orientation": round_q(q),
            "shape": {"type": "quad", "offset": vec((0, 0, 0)), "stretch": {"x": s, "y": s, "z": s}, "settings": {"size": {"x": w, "y": h}, "normal": "+Z"},
                      "textureLayout": {"front": uv(model.put(img))}, "unwrapMode": "custom", "visible": True, "doubleSided": True, "shadingMode": "flat"},
            "children": []}


def rod(model, name, a, b, thick, paint, **kw):
    """Cordon entre deux points."""
    d = tuple(b[i] - a[i] for i in range(3))
    L = math.sqrt(sum(c * c for c in d))
    return box(model, name, tuple((a[i] + b[i]) / 2.0 for i in range(3)), (thick, L, thick), paint, q=q_from_y(d), **kw)


def plank(model, name, a, b, width, thick, paint, **kw):
    """Pan plat dans le plan YZ, de a (bas) vers b (haut) : Y local monte, la face arriere regarde vers l'exterieur."""
    dy, dz = b[1] - a[1], b[2] - a[2]
    return box(model, name, tuple((a[i] + b[i]) / 2.0 for i in range(3)), (width, math.hypot(dy, dz), thick), paint,
               q=q_axis((1, 0, 0), math.degrees(math.atan2(dz, dy))), **kw)


def mirror(model, node):
    """Copie miroir en X : noms R-/L- echanges, position et rotation reflechies, forme etiree en -X (meme zone de texture)."""
    m = copy.deepcopy(node)

    def walk(n):
        n["id"] = model.nid()
        nm = n["name"]
        n["name"] = "L-" + nm[2:] if nm.startswith("R-") else "R-" + nm[2:] if nm.startswith("L-") else nm
        n["position"]["x"] = -n["position"]["x"]
        o = n["orientation"]
        n["orientation"] = {"x": o["x"], "y": -o["y"], "z": -o["z"], "w": o["w"]}
        n["shape"]["offset"]["x"] = -n["shape"]["offset"]["x"]
        n["shape"]["stretch"]["x"] = -n["shape"]["stretch"]["x"]
        for c in n["children"]:
            walk(c)
    walk(m)
    return m


def sym(model, *nodes):
    return list(nodes) + [mirror(model, n) for n in nodes]


# ---------------------------------------------------------------------------------------------------------------------
# Rendu logiciel : controle du modele sur le rig du joueur et icones (orthographique, sans lumiere : l'ombre est peinte)
# ---------------------------------------------------------------------------------------------------------------------
def player_rig():
    rig = json.loads(zipfile.ZipFile(ASSETS).read("Common/Characters/Player.blockymodel"))
    centers, shapes = {}, []

    def walk(n, origin):
        s = n.get("shape", {})
        pivot = tuple(origin[i] + n["position"][k] for i, k in enumerate("xyz"))
        off = s.get("offset", {"x": 0, "y": 0, "z": 0})
        center = tuple(pivot[i] + off[k] for i, k in enumerate("xyz"))
        centers[n["name"]] = center
        if s.get("type") == "box":
            shapes.append((center, s["settings"]["size"], n["name"]))
        for c in n.get("children", []):
            walk(c, center)
    for n in rig["nodes"]:
        walk(n, (0, 0, 0))
    return centers, shapes


def quads_of(node, origin, q, tex, skip):
    """Quads monde d'un noeud : (4 sommets, 4 uv en pixels). Les positions suivent le centre de forme du parent."""
    out = []
    s = node["shape"]
    size = s.get("settings", {}).get("size")
    st = (s["stretch"]["x"], s["stretch"]["y"], s["stretch"]["z"])
    off = (s["offset"]["x"], s["offset"]["y"], s["offset"]["z"])
    center = tuple(origin[i] + qrot(q, off)[i] for i in range(3))

    def world(v):
        r = qrot(q, (v[0] * st[0], v[1] * st[1], v[2] * st[2]))
        return tuple(center[i] + r[i] for i in range(3))

    if s["type"] == "box":
        hx, hy, hz = size["x"] / 2.0, size["y"] / 2.0, size["z"] / 2.0
        faces = {
            "front": ([(-hx, hy, hz), (hx, hy, hz), (hx, -hy, hz), (-hx, -hy, hz)], (size["x"], size["y"])),
            "back": ([(hx, hy, -hz), (-hx, hy, -hz), (-hx, -hy, -hz), (hx, -hy, -hz)], (size["x"], size["y"])),
            "right": ([(hx, hy, hz), (hx, hy, -hz), (hx, -hy, -hz), (hx, -hy, hz)], (size["z"], size["y"])),
            "left": ([(-hx, hy, -hz), (-hx, hy, hz), (-hx, -hy, hz), (-hx, -hy, -hz)], (size["z"], size["y"])),
            "top": ([(-hx, hy, -hz), (hx, hy, -hz), (hx, hy, hz), (-hx, hy, hz)], (size["x"], size["z"])),
            "bottom": ([(-hx, -hy, hz), (hx, -hy, hz), (hx, -hy, -hz), (-hx, -hy, -hz)], (size["x"], size["z"])),
        }
        for face, (verts, (w, h)) in faces.items():
            o = s["textureLayout"][face]["offset"]
            uvs = [(o["x"], o["y"]), (o["x"] + w, o["y"]), (o["x"] + w, o["y"] + h), (o["x"], o["y"] + h)]
            out.append(([world(v) for v in verts], uvs, tex))
    elif s["type"] == "quad":
        hx, hy = size["x"] / 2.0, size["y"] / 2.0
        o = s["textureLayout"]["front"]["offset"]
        verts = [(-hx, hy, 0), (hx, hy, 0), (hx, -hy, 0), (-hx, -hy, 0)]
        uvs = [(o["x"], o["y"]), (o["x"] + size["x"], o["y"]), (o["x"] + size["x"], o["y"] + size["y"]), (o["x"], o["y"] + size["y"])]
        out.append(([world(v) for v in verts], uvs, tex))
    for c in node["children"]:
        out += child_quads(c, center, q, tex, skip)
    return out


def child_quads(node, parent_center, parent_q, tex, skip):
    if node["name"].startswith(skip):
        return []
    if node["shape"].get("settings", {}).get("isPiece"):
        return attach(node, tex, skip)
    p = qrot(parent_q, (node["position"]["x"], node["position"]["y"], node["position"]["z"]))
    return quads_of(node, tuple(parent_center[i] + p[i] for i in range(3)), qmul(parent_q, node["orientation"]), tex, skip)


def attach(node, tex, skip=("\0",)):
    """Comme le jeu : la piece se colle au centre de l'os du joueur, sa position propre ne compte pas."""
    center = CENTERS[node["name"]]
    out = []
    for c in node["children"]:
        out += child_quads(c, center, ID_Q, tex, skip)
    return out


def draw(quads, size, yaw, pitch, solid=(), fit=None, margin=0.06):
    cy, sy, cp, sp = math.cos(math.radians(yaw)), math.sin(math.radians(yaw)), math.cos(math.radians(pitch)), math.sin(math.radians(pitch))

    def project(v):
        x = v[0] * cy + v[2] * sy
        z = -v[0] * sy + v[2] * cy
        y = v[1] * cp - z * sp
        d = v[1] * sp + z * cp
        return x, y, d

    pts = [project(v) for vs, _, _ in quads for v in vs]
    x0, x1 = min(p[0] for p in pts), max(p[0] for p in pts)
    y0, y1 = min(p[1] for p in pts), max(p[1] for p in pts)
    scale = fit or size * (1 - 2 * margin) / max(x1 - x0, y1 - y0)
    mx, my = (x0 + x1) / 2.0, (y0 + y1) / 2.0
    img = [[[0, 0, 0, 0] for _ in range(size)] for _ in range(size)]
    zbuf = [[-1e9] * size for _ in range(size)]

    def scr(p):
        return size / 2.0 + (p[0] - mx) * scale, size / 2.0 - (p[1] - my) * scale, p[2]

    def tri(a, b, c, ta, tb, tc, tex):
        area = (b[0] - a[0]) * (c[1] - a[1]) - (b[1] - a[1]) * (c[0] - a[0])
        if abs(area) < 1e-6:
            return
        for y in range(max(0, int(min(a[1], b[1], c[1]))), min(size, int(max(a[1], b[1], c[1])) + 2)):
            for x in range(max(0, int(min(a[0], b[0], c[0]))), min(size, int(max(a[0], b[0], c[0])) + 2)):
                px, py = x + 0.5, y + 0.5
                w0 = ((b[0] - px) * (c[1] - py) - (b[1] - py) * (c[0] - px)) / area
                w1 = ((c[0] - px) * (a[1] - py) - (c[1] - py) * (a[0] - px)) / area
                w2 = 1 - w0 - w1
                if w0 < -1e-4 or w1 < -1e-4 or w2 < -1e-4:
                    continue
                d = w0 * a[2] + w1 * b[2] + w2 * c[2]
                if d <= zbuf[y][x]:
                    continue
                if isinstance(tex, tuple):
                    col = list(tex) + [255]
                else:
                    u, v = w0 * ta[0] + w1 * tb[0] + w2 * tc[0], w0 * ta[1] + w1 * tb[1] + w2 * tc[1]
                    col = tex[min(len(tex) - 1, max(0, int(v)))][min(len(tex[0]) - 1, max(0, int(u)))]
                    if col[3] < 128:
                        continue
                zbuf[y][x] = d
                img[y][x] = list(col)

    for verts, uvs, tex in list(quads) + list(solid):
        s = [scr(project(v)) for v in verts]
        tri(s[0], s[1], s[2], uvs[0], uvs[1], uvs[2], tex)
        tri(s[0], s[2], s[3], uvs[0], uvs[2], uvs[3], tex)
    return img


def body_quads():
    out = []
    tone = {"Head": (214, 178, 150), "Neck": (214, 178, 150), "R-Hand": (214, 178, 150), "L-Hand": (214, 178, 150),
            "R-Forearm": (214, 178, 150), "L-Forearm": (214, 178, 150)}
    for center, size, name in BODY:
        if "Eye" in name:
            continue
        hx, hy, hz = size["x"] / 2.0, size["y"] / 2.0, size["z"] / 2.0
        c = tone.get(name, (150, 150, 158))
        corners = [[(-hx, hy, hz), (hx, hy, hz), (hx, -hy, hz), (-hx, -hy, hz)], [(hx, hy, -hz), (-hx, hy, -hz), (-hx, -hy, -hz), (hx, -hy, -hz)],
                   [(hx, hy, hz), (hx, hy, -hz), (hx, -hy, -hz), (hx, -hy, hz)], [(-hx, hy, -hz), (-hx, hy, hz), (-hx, -hy, hz), (-hx, -hy, -hz)],
                   [(-hx, hy, -hz), (hx, hy, -hz), (hx, hy, hz), (-hx, hy, hz)], [(-hx, -hy, hz), (hx, -hy, hz), (hx, -hy, -hz), (-hx, -hy, -hz)]]
        for i, f in enumerate(corners):
            shade = (1.0, 0.8, 0.9, 0.9, 1.1, 0.6)[i]
            out.append(([tuple(center[k] + p[k] for k in range(3)) for p in f], [(0, 0)] * 4, tuple(min(255, int(v * shade)) for v in c)))
    return out


def downscale(img, f):
    n = len(img) // f
    out = []
    for y in range(n):
        row = []
        for x in range(n):
            px = [img[y * f + j][x * f + i] for j in range(f) for i in range(f)]
            a = sum(p[3] for p in px)
            if a == 0:
                row.append([0, 0, 0, 0])
                continue
            row.append([sum(p[k] * p[3] for p in px) / a for k in range(3)] + [a / len(px)])
        out.append(row)
    return out

# ---------------------------------------------------------------------------------------------------------------------
# Controle : faces coplanaires (z-fighting)
# ---------------------------------------------------------------------------------------------------------------------
FACE_OF = {(0, 2): "left", (0, 3): "right", (1, 2): "bottom", (1, 3): "top", (2, 2): "back", (2, 3): "front"}


def hidden_marker(layout):
    """Une face masquee pointe sur (0, 0) ; une boite qui n'en a qu'une seule a cet endroit n'a rien de masque."""
    return sum(1 for v in layout.values() if v["offset"]["x"] == 0 and v["offset"]["y"] == 0) >= 2


def coplanar_report(nodes, tolerance=0.02, ignore=()):
    """Paires de boites droites dont deux faces de meme sens tombent dans le meme plan et se recouvrent.
    Toutes les pieces d'une tenue sont passees ensemble. Les boites tournees sont ignorees."""
    boxes = []

    def walk(node, origin, q, parent_bone):
        s = node["shape"]
        is_piece = s.get("settings", {}).get("isPiece")
        if is_piece:
            center = CENTERS[node["name"]]
            bone, q2 = node["name"], ID_Q
        else:
            p = qrot(q, (node["position"]["x"], node["position"]["y"], node["position"]["z"]))
            center = tuple(origin[i] + p[i] for i in range(3))
            bone, q2 = parent_bone, qmul(q, node["orientation"])
            if s["type"] == "box" and all(abs(q2[k]) < 1e-5 for k in "xyz"):
                size = s["settings"]["size"]
                half = tuple(abs(s["stretch"][k]) * size[k] / 2.0 for k in "xyz")
                layout = s["textureLayout"]
                unseen = {f for f, v in layout.items() if v["offset"]["x"] == 0 and v["offset"]["y"] == 0 and hidden_marker(layout)}
                boxes.append((node["name"], bone, tuple(center[i] - half[i] for i in range(3)), tuple(center[i] + half[i] for i in range(3)), unseen))
        for c in node["children"]:
            walk(c, center, q2, bone)

    for n in nodes:
        walk(n, (0, 0, 0), ID_Q, None)
    out = []
    for i, a in enumerate(boxes):
        for b in boxes[i + 1:]:
            if a[0].startswith(ignore) and ignore or b[0].startswith(ignore) and ignore:
                continue
            for axis in range(3):
                others = [k for k in range(3) if k != axis]
                if any(min(a[3][k], b[3][k]) - max(a[2][k], b[2][k]) <= 0.01 for k in others):
                    continue
                for side, label in ((2, "-"), (3, "+")):
                    face = FACE_OF[(axis, side)]
                    if face in a[4] or face in b[4]:
                        continue                                           # face jamais visible
                    if abs(a[side][axis] - b[side][axis]) < tolerance:
                        out.append("%s / %s  plan %s%s = %.2f" % (a[0], b[0], label, "xyz"[axis], a[side][axis]))
    return out


# ---------------------------------------------------------------------------------------------------------------------
# Items et ecriture des tenues
# ---------------------------------------------------------------------------------------------------------------------
HIDE = {"Head": ["HeadAccessory"], "Chest": ["Overtop", "Cape"], "Hands": ["Gloves"], "Legs": ["Pants", "Shoes", "Overpants"]}
HIDE_HELMET = ["EarAccessory", "Ear", "Haircut", "HeadAccessory"]
ICON_SCALE = {"Head": 0.34, "Chest": 0.3, "Hands": 0.5, "Legs": 0.34}


def item_json(model_dir, set_name, spec, slot, family_tag):
    """spec : prefix, quality, level, durability, pieces ; optionnels category, sound, hide, fire (part de la resistance appliquee au feu),
    particles ({slot: [{"SystemId", "TargetNodeName"}]})."""
    name, description, health, mana, resist = spec["pieces"][slot]
    res = {"Amount": resist, "CalculationType": "Percent"}
    damage = {"Physical": [dict(res)], "Projectile": [dict(res)]}
    if spec.get("fire"):
        damage["Fire"] = [{"Amount": round(resist * spec["fire"], 4), "CalculationType": "Percent"}]
    item = {
        "TranslationProperties": {"Name": name, "Description": description},
        "Quality": spec["quality"],
        "ItemLevel": spec["level"],
        "PlayerAnimationsId": "Block",                           # Sans Parent : un Parent Armor_* transmettrait sa recette
        "ItemSoundSetId": spec.get("sound", "ISS_Armor_Cloth"),
        "Categories": [spec.get("category") or familles.armor_category(spec["prefix"]) or "Eldanior_System.Armures", "Items.Armors"],
        "Icon": "Icons/ItemsGenerated/%s%s.png" % (spec["prefix"], slot),
        "IconProperties": {"Rotation": [22.5, 45, 22.5], "Scale": ICON_SCALE[slot], "Translation": [0, 0]},
        "Model": "%s/%s/%s.blockymodel" % (model_dir, set_name, slot),
        "Texture": "%s/%s/%s_Texture.png" % (model_dir, set_name, slot),
        "Interactions": {"Primary": {"Interactions": [{"Type": "EquipItem"}]}, "Secondary": {"Interactions": [{"Type": "EquipItem"}]}},
        "Armor": {
            "ArmorSlot": slot,
            "BaseDamageResistance": 0,
            "CosmeticsToHide": spec.get("hide", {}).get(slot, HIDE[slot]),
            "StatModifiers": {"Health": [{"Amount": health, "CalculationType": "Additive"}], "Mana": [{"Amount": mana, "CalculationType": "Additive"}]},
            "DamageResistance": damage,
        },
        "Tags": {"Type": ["Armor"], "Family": [family_tag]},
        "MaxDurability": spec["durability"],
        "DurabilityLossOnHit": 0.5,
    }
    if spec.get("particles", {}).get(slot):                     # particules attachees a un noeud du modele porte
        item["Particles"] = spec["particles"][slot]
    armor = item["Armor"]
    for cause, amount in spec.get("resist_extra", {}).get(slot, {}).items():   # resistances propres : Fire, Fall...
        armor["DamageResistance"][cause] = [{"Amount": amount, "CalculationType": "Percent"}]
    for stat, amount in spec.get("stats_extra", {}).get(slot, {}).items():     # autres stats : Stamina...
        armor["StatModifiers"][stat] = [{"Amount": amount, "CalculationType": "Additive"}]
    armor.update(spec.get("armor_extra", {}).get(slot, {}))                     # champs ItemArmor bruts (Regenerating, MovementSettings...)
    if "durability_loss" in spec:
        item["DurabilityLossOnHit"] = spec["durability_loss"]
    return item


def write_sets(res_dir, preview_dir, model_dir_rel, item_subdir, sets, preview_prefix, family_tag, skip_in_icon=("\0",), body_in_preview=True, preview_fit=4.2, closeup_fit=None):
    """Ecrit modeles, textures, icones et JSON de chaque tenue ; aperçu facultatif sur le corps du joueur."""
    common = os.path.join(res_dir, "Common")
    base_dir = os.path.join(common, *model_dir_rel.split("/"))
    item_dir = os.path.join(res_dir, "Server", "Item", "Items", "Armor", item_subdir)
    icon_dir = os.path.join(common, "Icons", "ItemsGenerated")
    os.makedirs(item_dir, exist_ok=True)

    for entry in os.listdir(base_dir) if os.path.isdir(base_dir) else ():      # anciens fichiers a la racine, avant les tenues
        path = os.path.join(base_dir, entry)
        if os.path.isfile(path):
            os.remove(path)

    previews = {}
    reports = {}
    for set_name, spec in sets.items():
        model_dir = os.path.join(base_dir, set_name)
        if os.path.isdir(model_dir):
            shutil.rmtree(model_dir)
        os.makedirs(model_dir)
        all_quads, all_nodes = [], []
        for slot, build in spec["build"].items():
            model, nodes = build()
            w, h, tex = model.texture()
            png.write(os.path.join(model_dir, slot + "_Texture.png"), w, h, tex)
            with open(os.path.join(model_dir, slot + ".blockymodel"), "w", encoding="utf-8") as f:
                json.dump({"nodes": nodes, "lod": "auto"}, f, indent=2, ensure_ascii=False)
            with open(os.path.join(item_dir, "%s%s.json" % (spec["prefix"], slot)), "w", encoding="utf-8") as f:
                json.dump(item_json(model_dir_rel, set_name, spec, slot, family_tag), f, indent=2, ensure_ascii=False)
                f.write("\n")
            all_quads += [q for n in nodes for q in attach(n, tex)]
            all_nodes += nodes
            icon_quads = [q for n in nodes for q in attach(n, tex, skip_in_icon)]
            icon = downscale(draw(icon_quads, 256, 45, 22.5), 4)
            png.write(os.path.join(icon_dir, "%s%s.png" % (spec["prefix"], slot)), 64, 64, icon)
            print("%-10s %-6s %dx%d  noeuds %d" % (set_name, slot, w, h, model.ids))
        previews[set_name] = all_quads
        reports[set_name] = all_nodes

    if preview_dir:
        os.makedirs(preview_dir, exist_ok=True)
        body = body_quads() if body_in_preview else []
        SIZE = 900
        pages = {"avant": (("front", 0, 6), ("three", 35, 14)), "arriere": (("back", 180, 6), ("back3", 215, 14))}
        for set_name, quads in previews.items():
            for page, views in pages.items():
                W, H = SIZE * len(views), SIZE
                sheet = [[[40, 44, 54, 255] for _ in range(W)] for _ in range(H)]
                for i, (name, yaw, pitch) in enumerate(views):
                    frame = draw(quads, SIZE, yaw, pitch, solid=body, fit=preview_fit)
                    for y in range(SIZE):
                        for x in range(SIZE):
                            if frame[y][x][3]:
                                sheet[y][i * SIZE + x] = frame[y][x]
                png.write(os.path.join(preview_dir, "%s_%s_%s.png" % (preview_prefix, set_name, page)), W, H, sheet)
            if closeup_fit:
                frame = draw(quads, SIZE, 20, 8, solid=body, fit=closeup_fit)
                sheet = [[list(p) if p[3] else [40, 44, 54, 255] for p in row] for row in frame]
                png.write(os.path.join(preview_dir, "%s_%s_detail.png" % (preview_prefix, set_name)), SIZE, SIZE, sheet)
    return reports


CENTERS, BODY = player_rig()
