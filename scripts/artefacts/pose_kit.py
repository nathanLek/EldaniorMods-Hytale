"""Cinematique du squelette du joueur et rendu des poses (controle des animations d'objets avant de les ecrire).

Conventions (celles d'armor_kit, verifiees sur les armures et les objets tenus) :
  - un noeud est place par rapport au CENTRE DE FORME de son parent (pivot + offset tourne) ;
  - une piste d'animation s'ajoute a la pose de liaison : position = liaison + delta (repere du parent),
    orientation = liaison * delta ;
  - la racine d'un objet tenu (R-Attachment, L-Attachment) se colle a l'os du meme nom, sa position propre ne compte pas.
Face avant du joueur = +Z, cote droit = -X, haut = +Y ; 64 unites par bloc.

Usage interne : import pose_kit as P ; P.render(pose, items, size, yaw, pitch).
"""
import json, math, os, sys, zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "armures"))
import armor_kit as K  # noqa: E402

ASSETS = K.ASSETS
ID_Q = {"x": 0.0, "y": 0.0, "z": 0.0, "w": 1.0}


def _z():
    return zipfile.ZipFile(ASSETS)


def load_json(path):
    return json.loads(_z().read(path))


# ---------------------------------------------------------------------------------------------------------------------
# Quaternions
# ---------------------------------------------------------------------------------------------------------------------
def qn(q):
    n = math.sqrt(sum(q[k] * q[k] for k in "xyzw")) or 1.0
    return {k: q[k] / n for k in "xyzw"}


def qconj(q):
    return {"x": -q["x"], "y": -q["y"], "z": -q["z"], "w": q["w"]}


def slerp(a, b, t):
    d = sum(a[k] * b[k] for k in "xyzw")
    if d < 0:
        b, d = {k: -b[k] for k in "xyzw"}, -d
    if d > 0.9995:
        return qn({k: a[k] + (b[k] - a[k]) * t for k in "xyzw"})
    th = math.acos(min(1.0, d))
    s = math.sin(th)
    wa, wb = math.sin((1 - t) * th) / s, math.sin(t * th) / s
    return {k: a[k] * wa + b[k] * wb for k in "xyzw"}


def euler(x=0.0, y=0.0, z=0.0):
    """Rotation en degres, appliquee dans l'ordre Y (lacet), X (tangage), Z (roulis) : q = qy * qx * qz."""
    return K.qmul(K.qmul(K.q_axis((0, 1, 0), y), K.q_axis((1, 0, 0), x)), K.q_axis((0, 0, 1), z))


def look_rot(fwd, up):
    """Rotation qui envoie +X local sur fwd et +Y local (au mieux) sur up."""
    def norm(v):
        n = math.sqrt(sum(c * c for c in v)) or 1.0
        return tuple(c / n for c in v)

    def cross(a, b):
        return (a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0])
    x = norm(fwd)
    z = norm(cross(x, up))
    y = cross(z, x)
    m = [[x[0], y[0], z[0]], [x[1], y[1], z[1]], [x[2], y[2], z[2]]]
    tr = m[0][0] + m[1][1] + m[2][2]
    if tr > 0:
        s = math.sqrt(tr + 1.0) * 2
        q = {"w": 0.25 * s, "x": (m[2][1] - m[1][2]) / s, "y": (m[0][2] - m[2][0]) / s, "z": (m[1][0] - m[0][1]) / s}
    elif m[0][0] > m[1][1] and m[0][0] > m[2][2]:
        s = math.sqrt(1.0 + m[0][0] - m[1][1] - m[2][2]) * 2
        q = {"w": (m[2][1] - m[1][2]) / s, "x": 0.25 * s, "y": (m[0][1] + m[1][0]) / s, "z": (m[0][2] + m[2][0]) / s}
    elif m[1][1] > m[2][2]:
        s = math.sqrt(1.0 + m[1][1] - m[0][0] - m[2][2]) * 2
        q = {"w": (m[0][2] - m[2][0]) / s, "x": (m[0][1] + m[1][0]) / s, "y": 0.25 * s, "z": (m[1][2] + m[2][1]) / s}
    else:
        s = math.sqrt(1.0 + m[2][2] - m[0][0] - m[1][1]) * 2
        q = {"w": (m[1][0] - m[0][1]) / s, "x": (m[0][2] + m[2][0]) / s, "y": (m[1][2] + m[2][1]) / s, "z": 0.25 * s}
    return qn(q)


# ---------------------------------------------------------------------------------------------------------------------
# Squelette
# ---------------------------------------------------------------------------------------------------------------------
class Bone:
    __slots__ = ("name", "pos", "q", "off", "size", "parent", "children")

    def __init__(self, n, parent):
        s = n.get("shape", {})
        self.name = n["name"]
        self.pos = tuple(n["position"][k] for k in "xyz")
        self.q = qn(n.get("orientation") or ID_Q)
        self.off = tuple((s.get("offset") or {"x": 0, "y": 0, "z": 0})[k] for k in "xyz")
        self.size = s["settings"]["size"] if s.get("type") == "box" else None
        self.parent = parent
        self.children = []


def load_rig():
    bones, roots = {}, []

    def walk(n, parent):
        b = Bone(n, parent)
        bones[b.name] = b
        (parent.children if parent else roots).append(b)
        for c in n.get("children", []):
            walk(c, b)
    for n in load_json("Common/Characters/Player.blockymodel")["nodes"]:
        walk(n, None)
    return bones, roots


BONES, ROOTS = load_rig()


def add(a, b):
    return tuple(a[i] + b[i] for i in range(3))


def solve(deltas):
    """deltas : {os: (dpos, dq)} -> {os: (pivot, rot, centre)} dans le repere du modele."""
    out = {}

    def walk(b, pc, pq):
        dp, dq = deltas.get(b.name, ((0, 0, 0), ID_Q))
        pivot = add(pc, K.qrot(pq, add(b.pos, dp)))
        rot = qn(K.qmul(pq, K.qmul(b.q, dq)))
        center = add(pivot, K.qrot(rot, b.off))
        out[b.name] = (pivot, rot, center)
        for c in b.children:
            walk(c, center, rot)
    for r in ROOTS:
        walk(r, (0, 0, 0), ID_Q)
    return out


# ---------------------------------------------------------------------------------------------------------------------
# Animations
# ---------------------------------------------------------------------------------------------------------------------
def _interp(keys, t, default, lerp):
    if not keys:
        return default
    if t <= keys[0]["time"]:
        return keys[0]["delta"]
    for a, b in zip(keys, keys[1:]):
        if a["time"] <= t <= b["time"]:
            span = b["time"] - a["time"]
            u = 0 if span <= 0 else (t - a["time"]) / span
            if a.get("interpolationType", "smooth") == "smooth":
                u = u * u * (3 - 2 * u)
            return lerp(a["delta"], b["delta"], u)
    return keys[-1]["delta"]


def sample(anim, t):
    """Pose (deltas) d'une animation .blockyanim au temps t (images)."""
    deltas = {}
    for name, tr in anim.get("nodeAnimations", {}).items():
        p = _interp(tr.get("position", []), t, {"x": 0, "y": 0, "z": 0},
                    lambda a, b, u: {k: a[k] + (b[k] - a[k]) * u for k in "xyz"})
        q = _interp(tr.get("orientation", []), t, ID_Q, slerp)
        deltas[name] = ((p["x"], p["y"], p["z"]), qn(q))
    return deltas


def visible_at(anim, t):
    out = {}
    for name, tr in anim.get("nodeAnimations", {}).items():
        keys = tr.get("shapeVisible") or []
        v = None
        for k in keys:
            if k["time"] <= t:
                v = k["delta"]
        if v is not None:
            out[name] = v
    return out


def merge(*poses):
    out = {}
    for p in poses:
        out.update(p)
    return out


# ---------------------------------------------------------------------------------------------------------------------
# Rendu
# ---------------------------------------------------------------------------------------------------------------------
SKIN = (214, 178, 150)
BODY_COLORS = {"Head": SKIN, "Neck": SKIN, "R-Hand": (230, 150, 140), "L-Hand": (140, 170, 230), "R-Forearm": SKIN, "L-Forearm": SKIN,
               "R-Arm": (170, 120, 120), "L-Arm": (120, 130, 175)}


def box_faces(center, rot, size, color, face_color=None):
    hx, hy, hz = size["x"] / 2.0, size["y"] / 2.0, size["z"] / 2.0
    corners = [[(-hx, hy, hz), (hx, hy, hz), (hx, -hy, hz), (-hx, -hy, hz)], [(hx, hy, -hz), (-hx, hy, -hz), (-hx, -hy, -hz), (hx, -hy, -hz)],
               [(hx, hy, hz), (hx, hy, -hz), (hx, -hy, -hz), (hx, -hy, hz)], [(-hx, hy, -hz), (-hx, hy, hz), (-hx, -hy, hz), (-hx, -hy, -hz)],
               [(-hx, hy, -hz), (hx, hy, -hz), (hx, hy, hz), (-hx, hy, hz)], [(-hx, -hy, hz), (hx, -hy, hz), (hx, -hy, -hz), (-hx, -hy, -hz)]]
    out = []
    for i, f in enumerate(corners):
        shade = (1.0, 0.75, 0.88, 0.88, 1.12, 0.6)[i]
        c = face_color if (i == 0 and face_color) else color
        out.append(([add(center, K.qrot(rot, p)) for p in f], [(0, 0)] * 4, tuple(min(255, int(v * shade)) for v in c)))
    return out


def body(world, skip=("Eyelid", "Eye", "Ear", "Mouth", "Eyebrow")):
    out = []
    for name, b in BONES.items():
        if b.size is None or any(s in name for s in skip):
            continue
        _, rot, center = world[name]
        out += box_faces(center, rot, b.size, BODY_COLORS.get(name, (150, 150, 158)),
                         (120, 80, 60) if name == "Head" else (110, 110, 125) if name in ("Chest", "Belly", "Pelvis") else None)
    return out


def item_quads(model, tex, world, hidden=()):
    """Quads d'un objet tenu : chaque racine se colle a l'os de meme nom (pivot de l'os, rotation de l'os)."""
    out = []

    def walk(n, origin, q):
        s = n.get("shape", {})
        if s.get("type") in ("box", "quad") and n["name"] not in hidden and s.get("visible", True) is not False:
            leaf = dict(n)
            leaf["children"] = []
            layout = dict(s.get("textureLayout") or {})
            if s["type"] == "box" and layout:                           # faces absentes (jamais vues) : peinture d'une autre
                any_face = next(iter(layout.values()))
                for f in ("front", "back", "left", "right", "top", "bottom"):
                    layout.setdefault(f, any_face)
            leaf["shape"] = dict(s, textureLayout=layout)
            out.extend(K.quads_of(leaf, origin, q, tex, ("\0",)))
        off = (s.get("offset") or {"x": 0, "y": 0, "z": 0})
        center = add(origin, K.qrot(q, (off["x"], off["y"], off["z"])))
        for c in n.get("children", []):
            p = K.qrot(q, (c["position"]["x"], c["position"]["y"], c["position"]["z"]))
            walk(c, add(center, p), qn(K.qmul(q, c.get("orientation") or ID_Q)))
    for root in model["nodes"]:
        if root["name"] not in world:
            continue
        pivot, rot, _ = world[root["name"]]
        for c in root.get("children", []):
            p = K.qrot(rot, (c["position"]["x"], c["position"]["y"], c["position"]["z"]))
            walk(c, add(pivot, p), qn(K.qmul(rot, c.get("orientation") or ID_Q)))
    return out


def render(deltas, items=(), size=384, yaw=30, pitch=10, fit=None, hidden=(), with_body=True):
    world = solve(deltas)
    quads = body(world) if with_body else []
    for model, tex in items:
        quads += item_quads(model, tex, world, hidden)
    return K.draw(quads, size, yaw, pitch, fit=fit, margin=0.04)


def sheet(images, cols):
    """Planche : images carrees de meme taille cote a cote."""
    s = len(images[0])
    rows = (len(images) + cols - 1) // cols
    out = [[[30, 30, 36, 255] for _ in range(s * cols)] for _ in range(s * rows)]
    for i, img in enumerate(images):
        ox, oy = (i % cols) * s, (i // cols) * s
        for y in range(s):
            for x in range(s):
                if img[y][x][3]:
                    out[oy + y][ox + x] = img[y][x]
    return out
