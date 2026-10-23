"""Avatar du Dragon Divin : dragon humanoide muscle, construit sur le squelette du Loup-garou vanilla (jambes
digitigrades cuisse > genou > mollet > pied, queue animee) et ses animations.

Methode (identique au Loup-garou) :
  - chaque face de chaque boite a sa propre zone de texture, a sa taille exacte (1 pixel = 1 unite) ;
  - l'eclairage est PEINT dans la texture (mode 'flat') : dessus clair, dessous sombre, arrondi des volumes,
    occlusion ambiante sur les bords, musculature (pectoraux, abdominaux, dorsaux, biceps...) peinte en relief ;
  - ailes : membranes = quads double face decoupes en alpha (bord festonne entre les doigts, nervures), os en boites ;
  - le cote droit est le miroir exact du cote gauche (etirement -1, comme le Loup-garou).
Les ailes et le bout de queue sont des noeuds a part : des copies des animations du Loup-garou leur ajoutent
respiration, repli a la course et battements en vol.

Rappels blockymodel : un enfant est place par rapport au CENTRE de la forme de son parent ; 64 unites = 1 bloc.
Usage : python3 gen_dragon_avatar.py <dossier_resources_Common> <dossier des assets vanilla (contient Common/ et Server/)>"""
import copy, json, math, os, random, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import png

out, src = sys.argv[1], sys.argv[2]
REL = "Items/Projectiles/Dragon_Avatar"
OUT = os.path.join(out, REL); os.makedirs(os.path.join(OUT, "Animations"), exist_ok=True)
WW = "NPC/Undead/Werewolf"
SERVER_JSON = os.path.join(out, "..", "Server/Models/Eldanior/Eldanior_Dragon_Avatar.json")
rnd = random.Random(7)

# =============================================================================================================
# Atlas : chaque face recoit sa zone (rangees successives), la texture finale est arrondie a un multiple de 32
# =============================================================================================================
ATLAS_W = 512
class Atlas:
    def __init__(self):
        self.x = self.y = self.row = 0; self.blits = []
    def put(self, img):
        h, w = len(img), len(img[0])
        if self.x + w > ATLAS_W: self.x, self.y, self.row = 0, self.y + self.row, 0
        pos = (self.x, self.y); self.blits.append((pos, img))
        self.x += w; self.row = max(self.row, h)
        return pos
    def save(self, path):
        H = (self.y + self.row + 31) // 32 * 32
        tex = [[[0, 0, 0, 0] for _ in range(ATLAS_W)] for _ in range(H)]
        for (x0, y0), img in self.blits:
            for y, row in enumerate(img): tex[y0 + y][x0:x0 + len(row)] = row
        png.write(path, ATLAS_W, H, tex)
        return H
atlas = Atlas()

def clamp(v): return max(0, min(255, int(v)))
def mix(a, b, t): return [a[i] + (b[i] - a[i]) * t for i in range(3)]
def smooth(t): t = max(0.0, min(1.0, t)); return t * t * (3 - 2 * t)

# ----- Palette -----
AZURE, AZURE_DARK, AZURE_HI, GAP = (52, 92, 168), (30, 56, 118), (132, 182, 240), (12, 20, 50)
IVORY, IVORY_GROOVE = (228, 208, 162), (120, 92, 54)
GOLD, GOLD_DARK, GOLD_HI = (236, 184, 72), (112, 72, 22), (255, 238, 165)
HORN_BASE, HORN_MID, HORN_TIP = (242, 234, 212), (212, 186, 134), (60, 52, 52)
CLAW, CLAW_HI = (32, 32, 44), (126, 130, 156)
CLOTH, CLOTH_DARK = (34, 46, 120), (16, 22, 64)
MOUTH, TOOTH = (112, 26, 38), (246, 238, 214)
RUNE = (140, 238, 255)

# ----- Motifs de matiere (couleur avant eclairage) -----
def scales(x, y, size, base, dark, seed):
    """Ecailles imbriquees vers le bas : reflet en haut, liseré sombre en bas, interstices noirs."""
    h = size * 0.72
    row = math.floor(y / h)
    cx = (x + (size / 2 if row % 2 else 0)) % size
    ly = y - row * h
    d = ((cx - size / 2) / (size / 2)) ** 2 + ((ly - 0.15 * h) / (h * 1.05)) ** 2
    rr = random.Random(hash((seed, row, math.floor((x + (size / 2 if row % 2 else 0)) / size)))).uniform(-0.07, 0.07)
    if d >= 1: return [v * 0.72 for v in mix(base, dark, 0.6)]
    k = 1.1 - 0.22 * (ly / h) + rr * 0.6
    irid = 0.5 + 0.5 * math.sin(x * 0.07 + y * 0.05 + seed)        # reflets irises azur / turquoise / violet
    c = [base[0] * k * (0.9 + 0.2 * irid), base[1] * k * (1.05 - 0.1 * irid), base[2] * k]
    if d > 0.75: c = mix(c, dark, 0.3 * (d - 0.75) / 0.25 + 0.1)   # lisere
    if ly < h * 0.28 and abs(cx - size / 2) < size * 0.25: c = mix(c, AZURE_HI, 0.35)   # reflet
    return c

def plates(x, y, fh):
    """Plaques ventrales ivoire : bandes horizontales bombees separees par un sillon."""
    band = 5
    ly = y % band
    if ly == band - 1: return list(IVORY_GROOVE)
    k = 1.12 - 0.26 * ly / (band - 1)
    c = [v * k for v in IVORY]
    if x % 9 == 0: c = mix(c, IVORY_GROOVE, 0.25)
    return c

def gold(x, y, fw, fh):
    e = min(x, y, fw - 1 - x, fh - 1 - y)
    c = list(GOLD)
    if e == 0: c = list(GOLD_DARK) if (x == fw - 1 or y == fh - 1) else list(GOLD_HI)
    elif e == 1: c = mix(GOLD, GOLD_HI if (x < fw / 2 and y < fh / 2) else GOLD_DARK, 0.35)
    elif fw > 6 and fh > 6:
        if (x + y) % 7 == 0 or (x - y) % 7 == 0: c = mix(c, GOLD_DARK, 0.35)          # gravure en losanges
        if (x + y) % 7 == 3 and (x - y) % 7 == 3: c = mix(c, GOLD_HI, 0.6)
    k = 1.15 - 0.3 * y / max(1, fh - 1)
    return [v * k for v in c]

def horn(t, x, y):
    c = mix(HORN_BASE, HORN_MID, smooth(t * 1.6)) if t < 0.62 else mix(HORN_MID, HORN_TIP, smooth((t - 0.62) / 0.38))
    if x % 4 == 0: c = mix(c, (90, 70, 50), 0.35)               # anneaux de croissance
    return c

def claw(t, x, y, fh):
    c = mix(CLAW_HI, CLAW, smooth(y / max(1, fh - 1) * 0.8 + t * 0.4))
    return c

def cloth(x, y, fw, fh):
    fold = 0.8 + 0.2 * math.sin(x * 0.9)
    c = [v * fold for v in CLOTH]
    if y >= fh - 3: c = list(GOLD) if y != fh - 1 else list(GOLD_DARK)
    elif y < 2: c = list(GOLD_DARK)
    return c

def base_color(spec, face, x, y, fw, fh, u, v):
    mat = spec.get("fmat", {}).get(face, spec["mat"])
    if mat == "scale":   return scales(x + spec["seed"], y, 5, AZURE, AZURE_DARK, spec["seed"])
    if mat == "scale_dark": return scales(x + spec["seed"], y, 5, AZURE_DARK, GAP, spec["seed"])
    if mat == "fine":    return scales(x + spec["seed"], y, 4, AZURE, AZURE_DARK, spec["seed"])
    if mat == "belly":   return plates(x, y, fh)
    if mat == "gold":    return gold(x, y, fw, fh)
    if mat == "cloth":   return cloth(x, y, fw, fh)
    if mat == "mouth":   return [c * (0.7 + 0.3 * v) for c in MOUTH]
    if mat in ("horn", "claw"):
        t0, t1 = spec.get("grad", (0, 1))
        along = {"front": u, "top": u, "bottom": u, "back": 1 - u, "left": 0.5 + 0.4 * (0.5 - v), "right": 0.5 + 0.4 * (0.5 - v)}[face]
        t = t0 + (t1 - t0) * along
        return horn(t, x, y) if mat == "horn" else claw(t, x, y, fh)
    if mat == "rune":
        on = (x % 6 in (2, 3) and y % 10 < 7) or (y % 10 == 3 and 1 < x % 12 < 9)
        return list(RUNE) if on else scales(x, y, 4, AZURE_DARK, GAP, 3)
    if mat == "eye":
        c = mix((255, 236, 140), (255, 140, 30), abs(u - 0.5) * 2)
        if abs(u - 0.5) < 0.14: c = (24, 10, 0)                  # pupille fendue
        return c
    raise ValueError(mat)

FACE_LIGHT = {"top": 1.2, "front": 1.0, "left": 0.9, "right": 0.9, "back": 0.82, "bottom": 0.55}
GLOW = {"eye", "rune"}

def paint_face(spec, face, fw, fh):
    img = []
    glow = spec["mat"] in GLOW
    for y in range(fh):
        row = []
        for x in range(fw):
            u, v = (x + 0.5) / fw, (y + 0.5) / fh
            c = base_color(spec, face, x, y, fw, fh, u, v)
            if glow: row.append([clamp(c[0]), clamp(c[1]), clamp(c[2]), 255]); continue
            k = FACE_LIGHT[face] * spec.get("light", 1.0)
            if face in ("top", "bottom"):
                k *= 0.86 + 0.14 * math.sin(math.pi * u) * math.sin(math.pi * v)
            else:
                k *= 1.1 - 0.26 * v                                    # lumiere zenithale
                k *= 0.8 + 0.2 * math.sin(math.pi * u) ** 0.7          # volume arrondi
            e = min(x, y, fw - 1 - x, fh - 1 - y)
            if e < 2 and min(fw, fh) > 4: k *= 0.8 + 0.1 * e            # occlusion des aretes
            if face not in ("top", "bottom") and fh - 1 - y < 2 and fh > 6: k *= 0.85
            # musculature : volume eclaire par le haut, ombre portee dessous, contour marque
            for (cx, cy, rx, ry, s) in spec.get("mus", {}).get(face, ()):
                dx, dy = (u - cx) / rx, (v - cy) / ry
                d = dx * dx + dy * dy
                if d < 1:
                    k *= 1 + s * (0.22 * (1 - d) - 0.3 * max(0.0, dy) * d - 0.12 * dy)
                elif d < 1.35:
                    k *= 1 - s * 0.32 * (1 - (d - 1) / 0.35) * (1.2 if dy > 0 else 0.6)
            for (x0, y0, x1, y1, w) in spec.get("groove", {}).get(face, ()):
                px, py = u * fw, v * fh
                ax, ay, bx, by = x0 * fw, y0 * fh, x1 * fw, y1 * fh
                L2 = (bx - ax) ** 2 + (by - ay) ** 2 or 1
                t = max(0, min(1, ((px - ax) * (bx - ax) + (py - ay) * (by - ay)) / L2))
                dist = math.hypot(px - ax - t * (bx - ax), py - ay - t * (by - ay))
                if dist < w: k *= 0.6 + 0.4 * dist / w
            row.append([clamp(c[0] * k), clamp(c[1] * k), clamp(c[2] * k), 255])
        img.append(row)
    return img

# =============================================================================================================
# Geometrie
# =============================================================================================================
ID = {"x": 0, "y": 0, "z": 0, "w": 1}
_ids = [5000]
def nid(): _ids[0] += 1; return str(_ids[0])
def face_uv(pos, mirror=False): return {"offset": {"x": pos[0], "y": pos[1]}, "mirror": {"x": mirror, "y": False}, "angle": 0}
def q_axis(axis, deg):
    a = math.radians(deg) / 2; s = math.sin(a); l = math.sqrt(sum(c * c for c in axis))
    return {"x": axis[0] / l * s, "y": axis[1] / l * s, "z": axis[2] / l * s, "w": math.cos(a)}
def qmul(a, b):
    return {"x": a["w"]*b["x"]+a["x"]*b["w"]+a["y"]*b["z"]-a["z"]*b["y"], "y": a["w"]*b["y"]-a["x"]*b["z"]+a["y"]*b["w"]+a["z"]*b["x"],
            "z": a["w"]*b["z"]+a["x"]*b["y"]-a["y"]*b["x"]+a["z"]*b["w"], "w": a["w"]*b["w"]-a["x"]*b["x"]-a["y"]*b["y"]-a["z"]*b["z"]}
def qs(*qs_):
    r = ID
    for q in qs_: r = qmul(r, q)
    return r
def qrot(q, v):
    x, y, z, w = q["x"], q["y"], q["z"], q["w"]; ux, uy, uz = v
    tx = 2*(y*uz - z*uy); ty = 2*(z*ux - x*uz); tz = 2*(x*uy - y*ux)
    return [ux + w*tx + (y*tz - z*ty), uy + w*ty + (z*tx - x*tz), uz + w*tz + (x*ty - y*tx)]
def norm(v): l = math.sqrt(sum(c * c for c in v)) or 1; return [c / l for c in v]
def cross(a, b): return [a[1]*b[2]-a[2]*b[1], a[2]*b[0]-a[0]*b[2], a[0]*b[1]-a[1]*b[0]]
def basis_quat(X, Y):
    X = norm(X); Z = norm(cross(X, Y)); Y = cross(Z, X)
    m = [[X[0], Y[0], Z[0]], [X[1], Y[1], Z[1]], [X[2], Y[2], Z[2]]]
    tr = m[0][0] + m[1][1] + m[2][2]
    if tr > 0:
        s = math.sqrt(tr + 1) * 2
        return {"w": s / 4, "x": (m[2][1] - m[1][2]) / s, "y": (m[0][2] - m[2][0]) / s, "z": (m[1][0] - m[0][1]) / s}
    if m[0][0] > m[1][1] and m[0][0] > m[2][2]:
        s = math.sqrt(1 + m[0][0] - m[1][1] - m[2][2]) * 2
        return {"w": (m[2][1] - m[1][2]) / s, "x": s / 4, "y": (m[0][1] + m[1][0]) / s, "z": (m[0][2] + m[2][0]) / s}
    if m[1][1] > m[2][2]:
        s = math.sqrt(1 + m[1][1] - m[0][0] - m[2][2]) * 2
        return {"w": (m[0][2] - m[2][0]) / s, "x": (m[0][1] + m[1][0]) / s, "y": s / 4, "z": (m[1][2] + m[2][1]) / s}
    s = math.sqrt(1 + m[2][2] - m[0][0] - m[1][1]) * 2
    return {"w": (m[1][0] - m[0][1]) / s, "x": (m[0][2] + m[2][0]) / s, "y": (m[1][2] + m[2][1]) / s, "z": s / 4}
def r6(q): return {k: round(q[k], 6) for k in "xyzw"}
def vec(v): return {"x": round(v[0], 3), "y": round(v[1], 3), "z": round(v[2], 3)}

def box(size, spec, offset=(0, 0, 0)):
    sx, sy, sz = [max(1, int(round(s))) for s in size]
    spec = dict(spec); spec.setdefault("seed", rnd.randint(0, 999))
    dims = {"front": (sx, sy), "back": (sx, sy), "left": (sz, sy), "right": (sz, sy), "top": (sx, sz), "bottom": (sx, sz)}
    lay = {f: face_uv(atlas.put(paint_face(spec, f, w, h))) for f, (w, h) in dims.items()}
    return {"type": "box", "offset": vec(offset), "stretch": {"x": 1, "y": 1, "z": 1}, "settings": {"size": {"x": sx, "y": sy, "z": sz}},
            "textureLayout": lay, "unwrapMode": "custom", "visible": True, "doubleSided": False,
            "shadingMode": "fullbright" if spec["mat"] in GLOW else "flat"}

def P(name, pos, size, mat, q=ID, ch=(), **spec):
    spec["mat"] = mat
    return {"id": nid(), "name": name, "position": vec(pos), "orientation": r6(q), "shape": box(size, spec), "children": list(ch)}

def G(name, pos, q=ID, ch=()):
    """Noeud vide (pivot d'animation)."""
    return {"id": nid(), "name": name, "position": vec(pos), "orientation": r6(q),
            "shape": {"type": "none", "offset": vec((0, 0, 0)), "stretch": {"x": 1, "y": 1, "z": 1}, "settings": {}, "textureLayout": {},
                      "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": "flat"}, "children": list(ch)}

def Q(name, pos, img, q=ID):
    """Quad double face peint (alpha decoupe), centre sur pos, normale +Z locale."""
    h, w = len(img), len(img[0])
    return {"id": nid(), "name": name, "position": vec(pos), "orientation": r6(q),
            "shape": {"type": "quad", "offset": vec((0, 0, 0)), "stretch": {"x": 1, "y": 1, "z": 1}, "settings": {"size": {"x": w, "y": h}, "normal": "+Z"},
                      "textureLayout": {"front": face_uv(atlas.put(img))}, "unwrapMode": "custom", "visible": True, "doubleSided": True,
                      "shadingMode": "flat"}, "children": []}

def bone(name, a, b, thick, mat, up=(0, 1, 0), overlap=0.6, **spec):
    d = [b[i] - a[i] for i in range(3)]; L = math.sqrt(sum(c * c for c in d))
    t = thick if isinstance(thick, (tuple, list)) else (thick, thick)
    if abs(sum(norm(d)[i] * up[i] for i in range(3))) > 0.97: up = (0, 0, 1)
    return P(name, [(a[i] + b[i]) / 2 for i in range(3)], (L + min(t) * overlap, t[0], t[1]), mat, basis_quat(d, up), **spec)

def chain(name, pts, t0, t1, mat, up=(0, 1, 0)):
    """Suite de segments qui s'affinent ; le degrade de matiere (corne, griffe) court sur toute la chaine."""
    n = len(pts) - 1
    return [bone("%s_%d" % (name, k), pts[k], pts[k + 1], max(1, round(t0 + (t1 - t0) * k / max(1, n - 1))), mat, up,
                 grad=(k / n, (k + 1) / n)) for k in range(n)]

def mirror(node):
    """Copie miroir (plan X = 0) : L-/R- echanges, positions et rotations reflechies, forme etiree en -X."""
    m = copy.deepcopy(node)
    def walk(n):
        n["id"] = nid()
        nm = n["name"]
        n["name"] = "R-" + nm[2:] if nm.startswith("L-") else "L-" + nm[2:] if nm.startswith("R-") else nm
        n["position"]["x"] = -n["position"]["x"]
        o = n["orientation"]; n["orientation"] = {"x": o["x"], "y": -o["y"], "z": -o["z"], "w": o["w"]}
        s = n["shape"]; s["offset"]["x"] = -s["offset"]["x"]; s["stretch"]["x"] = -s["stretch"]["x"]
        for c in n["children"]: walk(c)
    walk(m)
    return m
def sym(*parts): return list(parts) + [mirror(p) for p in parts]

# =============================================================================================================
# Squelette du Loup-garou : noeuds animes conserves (noms, pivots, rotations), formes remplacees
# =============================================================================================================
rig = json.load(open(os.path.join(src, "Common", WW, "Models/Model.blockymodel")))
DROP_PREFIX = ("L-Claw", "R-Claw", "L-Forearm-Fur", "R-Forearm-Fur", "L-Shoulder-Fur", "R-Shoulder-Fur", "L-Eye", "R-Eye", "Eye-Background",
               "L-Ear", "R-Ear", "Snout", "L-Eyebrow", "R-Eyebrow", "L-Face-Fur", "R-Face-Fur", "Teeth", "Fangs", "Beard", "Back-Hair",
               "Pelvis-Cloth", "Tail-End")
nodes = {}
def prune(n):
    n["children"] = [c for c in n["children"] if not c["name"].startswith(DROP_PREFIX)]
    nodes[n["name"]] = n
    for c in n["children"]: prune(c)
for n in rig["nodes"]: prune(n)

def center(name):
    o = nodes[name]["shape"].get("offset", {"x": 0, "y": 0, "z": 0}); return [o["x"], o["y"], o["z"]]

def reshape(name, size, offset, mat, mirror_of=None, **spec):
    """Nouvelle forme ; les enfants gardent leur pivot (positions relatives au centre de forme compensees)."""
    n = nodes[name]
    old = center(name)
    if mirror_of:
        src_shape = copy.deepcopy(nodes[mirror_of]["shape"])
        src_shape["offset"]["x"] = -src_shape["offset"]["x"]; src_shape["stretch"]["x"] = -src_shape["stretch"]["x"]
        n["shape"] = src_shape
    else:
        spec["mat"] = mat
        n["shape"] = box(size, spec, offset)
    new = center(name)
    for c in n["children"]:
        for i, k in enumerate("xyz"): c["position"][k] = round(c["position"][k] - (new[i] - old[i]), 3)

def reshape_pair(base, size, offset, mat, **spec):
    reshape("L-" + base, size, offset, mat, **spec)
    reshape("R-" + base, None, None, None, mirror_of="L-" + base)

def add(name, *parts):
    """Ajoute des pieces exprimees dans le repere du PIVOT du noeud (converties par rapport au centre de forme)."""
    c = center(name)
    for p in parts:
        for i, k in enumerate("xyz"): p["position"][k] = round(p["position"][k] - c[i], 3)
        nodes[name]["children"].append(p)

def add_pair(base, *parts):
    mirrored = [mirror(p) for p in parts]                      # avant conversion : les deux cotes partent du repere du pivot
    add("L-" + base, *parts)
    add("R-" + base, *mirrored)

def place_pair(base, pos):
    nodes["L-" + base]["position"] = vec(pos)
    nodes["R-" + base]["position"] = vec((-pos[0], pos[1], pos[2]))

# ----- Bassin : ceinture d'or, tassettes, pagne royal -----
reshape("Pelvis", (30, 16, 20), (0, 0, 0), "scale_dark")
add("Pelvis",
    P("Belt", (0, 5, 0), (33, 5, 23), "gold"), P("Belt_disc", (0, 5, 11.8), (9, 9, 2), "gold", ch=[P("Belt_gem", (0, 0, 1.2), (4, 4, 1), "eye")]),
    P("Loin_front", (0, -9, 10.6), (15, 22, 1), "cloth"), P("Loin_back", (0, -8, -10.6), (18, 20, 1), "cloth"),
    *sym(P("L-Tasset", (14, -1, 1), (6, 13, 20), "gold", q_axis((0, 0, 1), 14)),
         P("L-Tasset_low", (16, -9, 1), (5, 7, 18), "scale_dark", q_axis((0, 0, 1), 18))))

# ----- Abdomen : plaques ventrales, abdominaux en relief, obliques, lombaires -----
reshape("Belly", (30, 30, 22), (0, 15, 1), "scale", fmat={"front": "belly"},
        mus={"front": [(cx, cy, 0.19, 0.12, 1.4) for cy in (0.2, 0.47, 0.74) for cx in (0.31, 0.69)],
             "back": [(0.3, 0.55, 0.2, 0.45, 1.0), (0.7, 0.55, 0.2, 0.45, 1.0)]},
        groove={"front": [(0.5, 0.05, 0.5, 0.95, 1.6)], "back": [(0.5, 0, 0.5, 1, 2.0)]})
add("Belly",
    *sym(P("L-Oblique", (14, 12, 2), (4, 22, 16), "fine", q_axis((0, 0, 1), -5), mus={"left": [(0.5, 0.45, 0.45, 0.4, 1.2)]})),
    *[P("Belly_spike_%d" % k, (0, y, -11.5), (2, 6, 5), "gold", q_axis((1, 0, 0), -35)) for k, y in enumerate((8, 20))])

# ----- Torse en V : pectoraux, trapezes, grands dorsaux, dentele, plastron solaire -----
reshape("Chest", (48, 32, 32), (0, 14, 2), "scale",
        mus={"back": [(0.27, 0.45, 0.24, 0.4, 1.2), (0.73, 0.45, 0.24, 0.4, 1.2)], "front": [(0.5, 0.85, 0.3, 0.2, 0.8)],
             "left": [(0.55, 0.5, 0.35, 0.4, 1.0)], "right": [(0.45, 0.5, 0.35, 0.4, 1.0)]},
        groove={"back": [(0.5, 0.05, 0.5, 1, 2.2)], "front": [(0.5, 0.35, 0.5, 1, 1.4)]})
add("Chest",
    *sym(P("L-Pec", (11, 20, 18), (22, 13, 6), "fine", q_axis((0, 1, 0), -10),
           mus={"front": [(0.5, 0.42, 0.5, 0.5, 1.6)]}, groove={"front": [(0.1, 0.25, 0.9, 0.6, 0.9), (0.1, 0.55, 0.9, 0.75, 0.8)]}),
         P("L-Trap", (12, 30.5, -1), (16, 7, 18), "fine", q_axis((0, 0, 1), -22), mus={"top": [(0.5, 0.5, 0.45, 0.4, 1.2)]}),
         P("L-Lat", (22.5, 9, -5), (6, 26, 20), "fine", q_axis((0, 0, 1), 7), mus={"left": [(0.5, 0.4, 0.42, 0.42, 1.4)], "back": [(0.5, 0.4, 0.4, 0.45, 1)]}),
         *[P("L-Serratus_%d" % k, (23, 12 - k * 4.5, 12), (3, 3.5, 7), "fine", q_axis((0, 0, 1), 10), mus={"left": [(0.5, 0.5, 0.48, 0.45, 1.5)]}) for k in range(3)],
         P("L-Shoulder_blade", (11, 20, -14.5), (16, 16, 3), "fine", q_axis((0, 0, 1), 12), mus={"back": [(0.5, 0.45, 0.45, 0.45, 1.3)]}),
         P("L-Chest_rune", (24.4, 18, 2), (1, 12, 14), "rune")),
    P("Sternum_disc", (0, 17, 20.6), (10, 10, 2), "gold", q_axis((0, 0, 1), 45), ch=[P("Sternum_gem", (0, 0, 1.3), (5, 5, 1), "eye")]),
    P("Gorget", (0, 30.5, 3), (26, 5, 22), "gold"),
    *[P("Dorsal_spike_%d" % k, (0, y, -16.5), (2, h, 7), "gold", q_axis((1, 0, 0), -32)) for k, (y, h) in enumerate(((27, 9), (19, 11), (11, 10), (3, 8)))])

# ----- Ailes : racine animable (L-Wing), main animable (L-Wing-Hand), membranes festonnees -----
def membrane_img(poly, bounds, veins, rim_edges):
    """Rasterise un polygone en texture : degrade azur vers l'or au bord, nervures, liseré dore, decoupe alpha."""
    (x0, y0, x1, y1) = bounds
    w, h = int(math.ceil(x1 - x0)), int(math.ceil(y1 - y0))
    img = []
    def inside(px, py):
        c = False
        for i in range(len(poly)):
            ax, ay = poly[i]; bx, by = poly[i - 1]
            if (ay > py) != (by > py) and px < (bx - ax) * (py - ay) / (by - ay) + ax: c = not c
        return c
    def seg_dist(px, py, a, b):
        L2 = (b[0] - a[0]) ** 2 + (b[1] - a[1]) ** 2 or 1
        t = max(0, min(1, ((px - a[0]) * (b[0] - a[0]) + (py - a[1]) * (b[1] - a[1])) / L2))
        return math.hypot(px - a[0] - t * (b[0] - a[0]), py - a[1] - t * (b[1] - a[1]))
    for j in range(h):
        row = []
        for i in range(w):
            px, py = x0 + i + 0.5, y1 - j - 0.5                 # y du dessin vers le haut
            if not inside(px, py): row.append([0, 0, 0, 0]); continue
            de = min(seg_dist(px, py, poly[k - 1], poly[k]) for k in rim_edges)
            t = smooth(min(1, de / 26))
            c = mix((150, 196, 236), (40, 78, 150), t)                          # clair au bord, profond au coeur
            c = [v * (0.92 + 0.08 * math.sin(px * 0.35 + py * 0.12)) for v in c]  # plis de la membrane
            dv = min(seg_dist(px, py, a, b) for a, b in veins)
            if dv < 1.6: c = mix(c, (22, 40, 92), 0.75 * (1 - dv / 1.6))
            elif dv < 3: c = mix(c, (110, 160, 225), 0.25)
            if de < 1.6: c = mix(GOLD, GOLD_HI, 0.3)
            elif de < 3: c = mix(c, GOLD, 0.45)
            row.append([clamp(c[0]), clamp(c[1]), clamp(c[2]), 255])
        img.append(row)
    return img

def scallop(a, b, toward, n=7, depth=0.35):
    """Bord concave entre deux bouts de doigts (courbe tiree vers 'toward')."""
    m = [(a[0] + b[0]) / 2, (a[1] + b[1]) / 2]
    c = [m[0] + (toward[0] - m[0]) * depth, m[1] + (toward[1] - m[1]) * depth]
    return [((1 - t) ** 2 * a[0] + 2 * (1 - t) * t * c[0] + t * t * b[0], (1 - t) ** 2 * a[1] + 2 * (1 - t) * t * c[1] + t * t * b[1])
            for t in [k / n for k in range(1, n)]]

def wing_left():
    E, W = (28, 18), (58, 26)                       # coude, poignet (repere de la racine : x vers l'exterieur, y vers le haut)
    TIPS = [(56, -4), (46, -38), (28, -62), (8, -74)]  # bouts des 4 doigts (repere du poignet)
    B = (4, -46)                                    # attache basse de la membrane sur le flanc
    F4 = (W[0] + TIPS[3][0], W[1] + TIPS[3][1])
    inner = [(0, 2), E, W, F4] + scallop(F4, B, (34, -8), depth=0.3) + [B, (0, -18)]
    ib = (min(p[0] for p in inner), min(p[1] for p in inner), max(p[0] for p in inner), max(p[1] for p in inner))
    inner_img = membrane_img(inner, ib, [((0, -6), (40, -40)), ((10, 0), (30, -46)), ((E[0], E[1] - 4), (50, -30))],
                             rim_edges=list(range(4, len(inner) - 1)))
    outer = [(0, 0)]
    for k, t in enumerate(TIPS):
        outer.append(t)
        if k + 1 < len(TIPS): outer += scallop(t, TIPS[k + 1], (0, 0), depth=0.38)
    ob = (min(p[0] for p in outer), min(p[1] for p in outer), max(p[0] for p in outer), max(p[1] for p in outer))
    outer_img = membrane_img(outer, ob, [((0, 0), (t[0] * 0.92, t[1] * 0.92)) for t in TIPS] +
                             [((t[0] * 0.45, t[1] * 0.45), ((t[0] + TIPS[k + 1][0]) * 0.4, (t[1] + TIPS[k + 1][1]) * 0.4)) for k, t in enumerate(TIPS[:-1])],
                             rim_edges=list(range(2, len(outer))))
    up = (0, 0, 1)
    hand = G("L-Wing-Hand", (W[0], W[1], 0), ch=[
        Q("L-Wing-Membrane-Outer", ((ob[0] + ob[2]) / 2, (ob[1] + ob[3]) / 2, 0), outer_img),
        P("L-Wing-Wrist", (0, 0, 0), (7, 7, 7), "gold", q_axis((0, 0, 1), 45)),
        *chain("L-Wing-Thumb", [(0, 2, 0), (2, 9, 0), (0, 15, 0)], 3, 2, "claw", up)] +
        [part for k, t in enumerate(TIPS) for part in
         chain("L-Wing-Finger%d" % k, [(0, 0, 0), (t[0] * 0.5 + (2 if k < 2 else -2), t[1] * 0.5 + 3, 0), (t[0], t[1], 0)], 3.4 - k * 0.3, 2, "horn", up)] +
        [P("L-Wing-Knuckle%d" % k, (t[0] * 0.5 + (2 if k < 2 else -2), t[1] * 0.5 + 3, 0), (4, 4, 4), "gold") for k, t in enumerate(TIPS)] +
        [P("L-Wing-Tipclaw%d" % k, (t[0], t[1], 0), (3, 3, 3), "claw", q_axis((0, 0, 1), 45)) for k, t in enumerate(TIPS)])
    root = G("L-Wing", (10, 26, -15), qs(q_axis((0, 1, 0), 32), q_axis((0, 0, 1), 12), q_axis((1, 0, 0), -12)), ch=[
        Q("L-Wing-Membrane-Inner", ((ib[0] + ib[2]) / 2, (ib[1] + ib[3]) / 2, 0), inner_img),
        P("L-Wing-Root", (0, 0, 0), (9, 9, 9), "scale_dark", q_axis((0, 0, 1), 30)),
        bone("L-Wing-Humerus", (0, 0, 0), (E[0], E[1], 0), (7, 6), "scale", up, mus={"top": [(0.5, 0.5, 0.45, 0.4, 1)]}),
        P("L-Wing-Elbow", (E[0], E[1], 0), (6, 6, 6), "gold", q_axis((0, 0, 1), 45)),
        P("L-Wing-Elbow_spike", (E[0] - 2, E[1] + 5, 0), (2, 7, 2), "horn", q_axis((0, 0, 1), 30)),
        bone("L-Wing-Forearm", (E[0], E[1], 0), (W[0], W[1], 0), (5, 5), "scale", up),
        hand])
    return root
add("Chest", *sym(wing_left()))

# ----- Cou : muscles, plaques de gorge, crete -----
reshape("Neck", (17, 30, 17), (0, 10, -1), "scale", mus={"front": [(0.5, 0.5, 0.35, 0.5, 0.8)]})
add("Neck", P("Throat_plates", (0, 9, 8), (9, 28, 2), "belly"),
    *sym(P("L-Neck_muscle", (6.5, 10, 4), (4, 26, 7), "fine", q_axis((1, 0, 0), 8), mus={"left": [(0.5, 0.5, 0.45, 0.48, 1.3)]})),
    *[P("Neck_spike_%d" % k, (0, y, -10), (2, 7, 6), "gold", q_axis((1, 0, 0), -38)) for k, y in enumerate((6, 15, 24))])

# ----- Tete de dragon -----
reshape("Head", (22, 18, 24), (0, 9, 0), "scale", mus={"left": [(0.5, 0.5, 0.4, 0.4, 0.8)], "right": [(0.5, 0.5, 0.4, 0.4, 0.8)]})
def teeth_img(w, h, n, down=True):
    img = []
    for y in range(h):
        row = []
        for x in range(w):
            k = (x % (w / n)) / (w / n)
            tip = 1 - abs(k - 0.5) * 2
            yy = y if down else h - 1 - y
            row.append(list(TOOTH) + [255] if yy <= tip * (h - 0.5) else [0, 0, 0, 0])
        img.append(row)
    return img
add("Head",
    P("Cranium_top", (0, 18.5, -1), (16, 3, 20), "scale_dark"),
    P("Forehead", (0, 15, 12), (18, 4, 14), "scale_dark", q_axis((1, 0, 0), 22), mus={"top": [(0.5, 0.5, 0.4, 0.45, 0.8)]}),
    P("Snout", (0, 2.5, 24), (17, 12, 24), "scale", mus={"top": [(0.5, 0.5, 0.35, 0.45, 0.6)]}, ch=[
        P("Snout_ridge", (0, 6, 0), (6, 2, 20), "scale_dark"),
        P("Nose_horn", (0, 8.5, 7), (2, 6, 4), "horn", q_axis((1, 0, 0), -25)),
        *sym(P("L-Nostril", (4, 3.5, 11.6), (3, 2, 1), "claw"), P("L-Lip", (8.2, -3.5, 0), (1, 3, 20), "scale_dark"),
             Q("L-Teeth_up", (8.4, -6.5, 1), teeth_img(20, 4, 7), q_axis((0, 1, 0), 90)))]),
    P("Mouth", (0, -4, 19), (13, 3, 18), "mouth"),
    *sym(P("L-Eye_socket", (10, 8.5, 10), (4, 6, 9), "scale_dark", q_axis((0, 1, 0), 22)),
         P("L-Eye", (11.6, 8.8, 10.5), (2, 4, 7), "eye", q_axis((0, 1, 0), 22)),
         P("L-Brow", (10.5, 12.5, 10), (5, 3, 12), "gold", qs(q_axis((0, 1, 0), 22), q_axis((0, 0, 1), -18))),
         P("L-Cheek", (11.5, 1, 1), (4, 10, 14), "fine", q_axis((0, 1, 0), 8), mus={"left": [(0.5, 0.5, 0.45, 0.45, 1.3)]}),
         *[P("L-Frill_%d" % k, (13 + k * 1.5, 4 - k * 4, -8 - k * 2), (1, 9 - k * 2, 7), "gold", q_axis((0, 1, 0), -35)) for k in range(3)],
         *chain("L-Horn", [(7, 15, -4), (10, 20, -14), (12.5, 24, -24), (13.5, 29, -33), (12.5, 35, -40), (10, 39, -43)], 6, 2, "horn"),
         *chain("L-Horn_low", [(11, 6, -7), (16, 7, -15), (19, 6, -22), (20, 3, -27)], 4, 1, "horn")),
    *[P("Crest_%d" % k, (0, y, z), (2, h, 6), "gold", q_axis((1, 0, 0), -30)) for k, (y, z, h) in enumerate(((19, 4, 6), (18, -4, 8), (14, -10, 7), (8, -12, 6)))])
reshape("Jaw", (14, 6, 26), (0, 2, 13), "scale_dark")
add("Jaw", P("Chin_plates", (0, -1.3, 13), (9, 1, 22), "belly"), P("Tongue", (0, 5.2, 13), (7, 1, 16), "mouth"),
    *sym(Q("L-Teeth_low", (6.6, 6.2, 16), teeth_img(18, 3, 6, down=False), q_axis((0, 1, 0), 90)),
         P("L-Jaw_muscle", (7.5, 2, 4), (3, 7, 9), "fine", mus={"left": [(0.5, 0.5, 0.45, 0.45, 1.2)]})))

# ----- Bras : deltoide + epaulette, biceps, triceps, avant-bras, brassard, main griffue -----
reshape_pair("Arm", (17, 46, 17), (1, -19, 0), "scale", mus={"left": [(0.5, 0.35, 0.4, 0.3, 1)], "right": [(0.5, 0.35, 0.4, 0.3, 1)]})
add_pair("Arm",
         P("L-Biceps", (1, -15, 8), (12, 20, 5), "fine", mus={"front": [(0.5, 0.5, 0.48, 0.48, 1.6)]}),
         P("L-Triceps", (1, -13, -8), (12, 24, 5), "fine", mus={"back": [(0.5, 0.45, 0.48, 0.48, 1.4)]}, groove={"back": [(0.5, 0.15, 0.5, 0.75, 1)]}),
         P("L-Arm_rune", (9.6, -18, 0), (1, 18, 6), "rune"))
reshape_pair("Shoulder2", (22, 20, 24), (0, 0, 0), "fine", mus={f: [(0.5, 0.45, 0.48, 0.48, 1.3)] for f in ("front", "back", "left", "right", "top")})
add_pair("Shoulder2",
         P("L-Pauldron", (1, 7, 0), (25, 7, 27), "gold", q_axis((0, 0, 1), -8)),
         P("L-Pauldron_lame1", (5, 1, 0), (22, 4, 26), "gold", q_axis((0, 0, 1), -24), light=0.85),
         P("L-Pauldron_lame2", (8, -4, 0), (18, 4, 24), "gold", q_axis((0, 0, 1), -38), light=0.75),
         P("L-Pauldron_spike", (4, 13, -3), (3, 11, 3), "horn", q_axis((0, 0, 1), -20), grad=(0.2, 1)),
         P("L-Pauldron_spike2", (9, 10, 2), (2, 7, 2), "horn", q_axis((0, 0, 1), -40), grad=(0.3, 1)))
reshape_pair("Forearm", (18, 36, 17), (0, -17, 1), "scale", mus={"front": [(0.5, 0.3, 0.45, 0.3, 1.2)], "left": [(0.5, 0.3, 0.45, 0.3, 1)]})
add_pair("Forearm",
         P("L-Brachioradialis", (2, -9, 9), (13, 16, 4), "fine", mus={"front": [(0.5, 0.45, 0.48, 0.48, 1.5)]}),
         P("L-Bracer", (0, -27, 1), (20, 14, 19), "gold", ch=[P("L-Bracer_gem", (10.3, 0, 0), (1, 5, 5), "eye")]),
         P("L-Forearm_fin", (9.5, -12, -5), (2, 24, 10), "gold", q_axis((1, 0, 0), 22)),
         P("L-Forearm_rune", (9.3, -10, 4), (1, 12, 4), "rune"))
reshape_pair("Hand", (13, 15, 17), (0, -8, 1), "scale", mus={"front": [(0.5, 0.4, 0.45, 0.4, 0.8)]})
add_pair("Hand", P("L-Knuckles", (0, -14, 9.2), (11, 3, 2), "gold"))
nodes["L-Attachment"]["position"] = vec((-4 - 0, -20 + 8, 2 - 1)); nodes["R-Attachment"]["position"] = vec((4, -12, 1))
for k, (fname, z) in enumerate((("Finger-Back", -6), ("Finger-Mid", -1), ("Finger-Front", 5))):
    place_pair(fname, (2, -15 + 8 - 1, z - 1))
    reshape_pair(fname, (5, 11, 5), (0, -5, 0), "scale_dark")
    reshape_pair(fname + "2", (4, 9, 4), (0, -4, -1), "scale_dark")
    add_pair(fname + "2", *chain("L-%s-Claw" % fname, [(0, -8, -1), (0, -13, 0.5), (0, -16, 3)], 3, 2, "claw", (0, 0, 1)))
place_pair("Thumb", (-5, -6 + 8 - 1, 8 - 1))
reshape_pair("Thumb", (5, 10, 5), (0, -5, 0), "scale_dark")
reshape_pair("Thumb2", (4, 8, 4), (0, -4, -1), "scale_dark")
add_pair("Thumb2", *chain("L-Thumb-Claw", [(0, -7, -1), (0, -11, 0.5), (0, -14, 3)], 3, 2, "claw", (0, 0, 1)))
for base_ in ("Finger-Back", "Finger-Mid", "Finger-Front", "Thumb"):
    for side in "LR":
        n2 = nodes["%s-%s2" % (side, base_)]
        n2["position"] = vec((0, -5, 0.5))

# ----- Jambes digitigrades : quadriceps, ischios, mollet, jambieres, pied griffu -----
reshape_pair("Thigh", (20, 40, 20), (2, -15, 1), "scale",
             mus={"front": [(0.5, 0.45, 0.42, 0.45, 1.3)], "back": [(0.5, 0.4, 0.42, 0.45, 1.2)], "left": [(0.5, 0.45, 0.45, 0.45, 1.1)]})
add_pair("Thigh",
         P("L-Vastus", (8, -17, 5), (6, 22, 9), "fine", q_axis((0, 0, 1), 4), mus={"left": [(0.5, 0.55, 0.48, 0.48, 1.5)], "front": [(0.5, 0.55, 0.48, 0.48, 1.3)]}),
         P("L-Rectus", (2, -13, 11), (10, 26, 4), "fine", mus={"front": [(0.5, 0.45, 0.48, 0.48, 1.5)]}),
         P("L-Hamstring", (2, -14, -10), (14, 24, 4), "fine", mus={"back": [(0.5, 0.45, 0.48, 0.48, 1.3)]}, groove={"back": [(0.5, 0.2, 0.5, 0.85, 1)]}),
         P("L-Hip_plate", (6, 0, 9), (12, 10, 3), "gold", q_axis((0, 1, 0), 15)))
reshape_pair("Knee", (15, 36, 15), (0, -11, -2), "scale", mus={"back": [(0.5, 0.3, 0.45, 0.3, 1.4)]})
add_pair("Knee",
         P("L-Gastrocnemius", (0, -6, -10), (12, 18, 5), "fine", mus={"back": [(0.5, 0.4, 0.48, 0.48, 1.6)]}, groove={"back": [(0.5, 0.15, 0.5, 0.7, 0.9)]}),
         P("L-Kneecap", (0, 6, 5), (12, 10, 5), "gold", ch=[P("L-Knee_spike", (0, 2, 3), (2, 7, 2), "horn", q_axis((1, 0, 0), 40), grad=(0.3, 1))]),
         P("L-Shin_plate", (0, -14, 5.5), (9, 22, 2), "gold"))
reshape_pair("Calf", (12, 30, 12), (0, -12, 1), "scale_dark")
add_pair("Calf", P("L-Calf_spur", (0, -2, -6.5), (2, 12, 4), "horn", q_axis((1, 0, 0), 25), grad=(0.2, 1)),
         P("L-Ankle_band", (0, -21, 1), (13, 4, 13), "gold"))
reshape_pair("Foot", (19, 9, 22), (0, -3, 6), "scale", mus={"top": [(0.5, 0.5, 0.45, 0.45, 0.8)]})
add_pair("Foot",
         *[part for k, x in enumerate((-6, 0, 6)) for part in
           [P("L-Toe%d" % k, (x, -4, 19), (5, 6, 10), "scale_dark"),
            *chain("L-Toe%d-Claw" % k, [(x, -2.5, 23.5), (x, -3.5, 27.5), (x, -6.5, 29.5)], 3, 2, "claw", (1, 0, 0))]],
         *chain("L-Dewclaw", [(0, -3, -4), (0, -4, -9), (0, -7, -11)], 3, 2, "claw", (1, 0, 0)))

# ----- Queue : 7 segments qui s'affinent, epines dorsales dorees, plaques ventrales, lame au bout -----
# Repere local de la queue (rotation du Loup-garou) : +Y = vers l'arriere ; dorsal D et lateral A calcules
nodes["Tail"]["orientation"] = r6(qmul(q_axis((1, 0, 0), -14), nodes["Tail"]["orientation"]))
TQ = nodes["Tail"]["orientation"]
def to_local(v):
    inv = {"x": -TQ["x"], "y": -TQ["y"], "z": -TQ["z"], "w": TQ["w"]}; return qrot(inv, v)
D, A = norm(to_local((0, 1, 0))), norm(to_local((1, 0, 0)))
TAIL = [(20, 32, 18), (16, 30, 15), (13, 28, 12), (10, 26, 9), (8, 24, 7), (6, 22, 5), (4, 20, 4)]
CURL = [0, 0, -8, -6, 6, 12, 16]                                    # vers le bas, puis la pointe se releve
reshape("Tail", TAIL[0], (0, 14, 0), "scale", mus={"front": [(0.5, 0.5, 0.45, 0.48, 0.8)]})
reshape("Tail2", TAIL[1], (0, 15, 0), "scale")
def tail_chain(k):
    w, L, t = TAIL[k]
    ch = [P("Tail_spike_%d" % k, [D[i] * (t / 2 + 3) for i in range(3)], (2, max(5, L * 0.35), t * 0.4 + 4), "gold", basis_quat(A, (0, 1, 0))),
          P("Tail_belly_%d" % k, [-D[i] * (t / 2 + 0.2) for i in range(3)], (max(2, w - 4), L - 4, 1), "belly", basis_quat(A, (0, 1, 0)))]
    if k + 1 < len(TAIL): ch.append(tail_chain(k + 1))
    else:
        ch += [P("Tail_blade", (0, L / 2 + 8, 0), (2, 18, 14), "gold", basis_quat(A, (0, 1, 0))),
               P("Tail_blade_wing", (0, L / 2 + 2, 0), (2, 8, 22), "gold", qmul(basis_quat(A, (0, 1, 0)), q_axis((0, 0, 1), 0)), light=0.9),
               P("Tail_blade_gem", (0, L / 2 + 6, 0), (3, 4, 4), "eye", basis_quat(A, (0, 1, 0)))]
    n = P("Tail%d" % (k + 1), (0, TAIL[k - 1][1] / 2 - 1, 0), (w, L, t), "scale" if k % 2 else "scale_dark", q_axis(A, CURL[k]), ch=ch)
    n["shape"]["offset"] = vec((0, L / 2, 0))
    return n
# epines et plaques des deux premiers segments (noeuds animes du Loup-garou)
for name, k in (("Tail", 0), ("Tail2", 1)):
    w, L, t = TAIL[k]
    add(name, P("%s_spike" % name, [D[i] * (t / 2 + 3) + (center(name)[i]) for i in range(3)], (2, 8, 7), "gold", basis_quat(A, (0, 1, 0))),
        P("%s_spike_b" % name, [D[i] * (t / 2 + 2) + (center(name)[i] + (L / 3 if i == 1 else 0)) for i in range(3)], (2, 6, 5), "gold", basis_quat(A, (0, 1, 0))),
        P("%s_belly" % name, [-D[i] * (t / 2 + 0.2) + center(name)[i] for i in range(3)], (w - 6, L - 4, 1), "belly", basis_quat(A, (0, 1, 0))))
nodes["Tail2"]["children"].append(tail_chain(2))          # deja relatif au centre de forme de Tail2

json.dump(rig, open(os.path.join(OUT, "Dragon_Avatar.blockymodel"), "w"), indent=1)
H = atlas.save(os.path.join(OUT, "Dragon_Avatar.png"))

# =============================================================================================================
# Hauteur du modele (pour l'echelle x3 du joueur)
# =============================================================================================================
def world_points():
    pts = []
    def walk(n, P_, Q_):
        p = [n["position"][k] for k in "xyz"]; wp = [P_[i] + qrot(Q_, p)[i] for i in range(3)]; wq = qmul(Q_, n["orientation"])
        s = n["shape"]; off = [s.get("offset", {}).get(k, 0) for k in "xyz"]; c = [wp[i] + qrot(wq, off)[i] for i in range(3)]
        if s["type"] == "box":
            sz = [s["settings"]["size"][k] / 2 for k in "xyz"]
            for dx in (-1, 1):
                for dy in (-1, 1):
                    for dz in (-1, 1):
                        pts.append([c[i] + qrot(wq, [dx * sz[0], dy * sz[1], dz * sz[2]])[i] for i in range(3)])
        for ch in n["children"]: walk(ch, c, wq)
    for n in rig["nodes"]: walk(n, [0, 0, 0], ID)
    return pts
pts = world_points()
head_top = max(p[1] for p in pts)
print("modele : texture %dx%d, hauteur %.0f unites (%.2f blocs)" % (ATLAS_W, H, head_top, head_top / 64))

# =============================================================================================================
# Animations : copies de celles du Loup-garou + ailes (respiration, repli, battements) et bout de queue
# =============================================================================================================
def key(t, q): return {"time": int(t), "delta": r6(q), "interpolationType": "smooth"}
def mq(q): return {"x": q["x"], "y": -q["y"], "z": -q["z"], "w": q["w"]}
def wing_pose(fold, lift, hand=0.0):
    """fold : balaye vers l'arriere (+) / l'avant (-) ; lift : leve l'aile ; hand : replie la main."""
    return qs(q_axis((0, 1, 0), fold), q_axis((0, 0, 1), lift)), q_axis((0, 1, 0), hand)

def wing_track(dur, poses):
    """poses : liste (fraction de duree, fold, lift, hand) -> pistes L/R aile et main."""
    tracks = {"L-Wing": [], "R-Wing": [], "L-Wing-Hand": [], "R-Wing-Hand": []}
    for f, fold, lift, hand in poses:
        w, h = wing_pose(fold, lift, hand)
        t = round(f * dur)
        tracks["L-Wing"].append(key(t, w)); tracks["R-Wing"].append(key(t, mq(w)))
        tracks["L-Wing-Hand"].append(key(t, h)); tracks["R-Wing-Hand"].append(key(t, mq(h)))
    return tracks

def tail_track(dur, amp, cycles=1):
    tracks = {}
    for k in range(2, len(TAIL)):
        ph = k * 0.12
        keys = []
        for j in range(4 * cycles + 1):
            f = j / (4 * cycles)
            ang = amp * (1 + 0.25 * k) * math.sin(2 * math.pi * (f * cycles - ph))
            keys.append(key(f * dur, q_axis(D, ang)))
        tracks["Tail%d" % (k + 1)] = keys
    return tracks

def flap(cycles, amp, base_fold=-10):
    poses = []
    for j in range(8 * cycles + 1):
        f = j / (8 * cycles); s = math.sin(2 * math.pi * f * cycles)
        poses.append((f, base_fold + 10 * s, amp * s, -18 * math.sin(2 * math.pi * f * cycles - 0.9)))
    return poses

CUSTOM = {
    "Default/Idle":                  lambda d: (wing_track(d, [(0, 0, 0, 0), (0.5, -5, 5, -6), (1, 0, 0, 0)]), tail_track(d, 4)),
    "Default/Alerted":               lambda d: (wing_track(d, [(0, -18, 14, -10), (1, -18, 14, -10)]), tail_track(d, 6)),
    "Default/Walk":                  lambda d: (wing_track(d, [(0, 14, -4, 6), (0.5, 12, -1, 6), (1, 14, -4, 6)]), tail_track(d, 7)),
    "Default/Walk_Backward":         lambda d: (wing_track(d, [(0, 14, -4, 6), (0.5, 12, -1, 6), (1, 14, -4, 6)]), tail_track(d, 7)),
    "Default/Run":                   lambda d: (wing_track(d, [(0, 26, -10, 14), (0.5, 22, -6, 14), (1, 26, -10, 14)]), tail_track(d, 9)),
    "Default/Sprint":                lambda d: (wing_track(d, [(0, 34, -14, 18), (0.5, 30, -10, 18), (1, 34, -14, 18)]), tail_track(d, 10)),
    "Default/Crouch":                lambda d: (wing_track(d, [(0, 20, -12, 10), (1, 20, -12, 10)]), tail_track(d, 3)),
    "Default/Crouch_Walk":           lambda d: (wing_track(d, [(0, 20, -12, 10), (0.5, 18, -10, 10), (1, 20, -12, 10)]), tail_track(d, 6)),
    "Default/Crouch_Walk_Backward":  lambda d: (wing_track(d, [(0, 20, -12, 10), (0.5, 18, -10, 10), (1, 20, -12, 10)]), tail_track(d, 6)),
    "Default/Jump":                  lambda d: (wing_track(d, [(0, -10, 10, -8), (0.4, -24, 30, -16), (1, -12, 18, -10)]), tail_track(d, 5)),
    "Default/Jump_Far":              lambda d: (wing_track(d, [(0, -10, 10, -8), (0.4, -26, 34, -18), (1, -14, 20, -12)]), tail_track(d, 5)),
    "Default/Jump_Far2":             lambda d: (wing_track(d, [(0, -10, 10, -8), (0.4, -26, 34, -18), (1, -14, 20, -12)]), tail_track(d, 5)),
    "Default/Fall":                  lambda d: (wing_track(d, [(0, -20, 26, -14), (0.5, -24, 30, -16), (1, -20, 26, -14)]), tail_track(d, 4)),
    "Default/Fall_Far":              lambda d: (wing_track(d, [(0, -26, 34, -18), (0.5, -30, 38, -20), (1, -26, 34, -18)]), tail_track(d, 4)),
    "Fly/Fly_Idle":                  lambda d: (wing_track(d, flap(1, 26)), tail_track(d, 6)),
    "Fly/Fly":                       lambda d: (wing_track(d, flap(2, 30)), tail_track(d, 8, 2)),
    "Fly/Fly_Backward":              lambda d: (wing_track(d, flap(2, 26, -18)), tail_track(d, 6, 2)),
    "Fly/Fly_Fast":                  lambda d: (wing_track(d, flap(3, 32, 6)), tail_track(d, 10, 3)),
}
anim_paths = {}
for rel, fn in CUSTOM.items():
    a = json.load(open(os.path.join(src, "Common", WW, "Animations", rel + ".blockyanim")))
    dur = a["duration"]
    for tracks in fn(dur):
        for node_name, keys in tracks.items():
            a["nodeAnimations"][node_name] = {"position": [], "orientation": keys, "shapeStretch": [], "shapeVisible": [], "shapeUvOffset": []}
    name = rel.split("/")[1]
    json.dump(a, open(os.path.join(OUT, "Animations", name + ".blockyanim"), "w"), indent=1)
    anim_paths["%s/Animations/%s.blockyanim" % (WW, rel)] = "%s/Animations/%s.blockyanim" % (REL, name)

# ----- ModelAsset : jeux d'animations du Loup-garou (nos copies si personnalisees) + etats propres au joueur -----
ww_json = json.load(open(os.path.join(src, "Server/Models/Undead/Werewolf.json")))
sets = {}
for k, v in ww_json["AnimationSets"].items():
    v = copy.deepcopy(v)
    for an in v["Animations"]: an["Animation"] = anim_paths.get(an["Animation"], an["Animation"])
    sets[k] = v
ALIASES = {"RunBackward": "WalkBackward", "CrouchSlide": "Crouch", "SafetyRoll": "JumpRun", "JumpCrouch": "Jump", "JumpSprint": "JumpRun",
           "StepWalk": "Walk", "StepRun": "Run", "StepSprint": "Sprint", "StepCrouchWalk": "CrouchWalk",
           "GlideIdle": "FlyIdle", "Glide": "Fly", "GlideBackward": "FlyBackward", "GlideFast": "FlyFast",
           "FlyCrouchIdle": "FlyIdle", "FlyCrouch": "Fly", "FlyCrouchBackward": "FlyBackward",
           "FluidRunBackward": "FluidWalkBackward", "FluidSprint": "FluidRun",
           "MantleIdle": "ClimbIdle", "MantleUp": "ClimbUp", "MantleLeft": "ClimbLeft", "MantleRight": "ClimbRight",
           "DashForward": "JumpRun", "DashBackward": "JumpRun", "DashLeft": "JumpRun", "DashRight": "JumpRun",
           "Roll": "JumpRun", "RollBackward": "JumpRun", "RollLeft": "JumpRun", "RollRight": "JumpRun",
           "HurtFall": "Hurt", "HurtFly": "Hurt", "HurtDrown": "Hurt", "HurtBurn": "Hurt",
           "DeathFall": "Death", "DeathFly": "Death", "DeathFlyImpact": "Death", "DeathDrown": "Death",
           "Sleep2": "Sleep", "Sit": "Crouch", "Sit2": "Crouch", "SitGround": "Laydown", "Slide": "Crouch",
           "IdlePassive": "Idle", "FlyIdlePassive": "FlyIdle", "SwimIdlePassive": "SwimIdle", "FluidIdlePassive": "FluidIdle",
           "MountIdle": "Idle", "MountWalk": "Walk", "MountWalkBackward": "WalkBackward", "MountRun": "Run",
           "MountRunBackward": "WalkBackward", "MountSprint": "Sprint"}
for k, v in ALIASES.items():
    if k not in sets and v in sets: sets[k] = copy.deepcopy(sets[v])
model = json.load(open(SERVER_JSON))
model["$Comment"] = ("Avatar du Dragon Divin : dragon humanoide muscle sur le squelette et les animations du Loup-garou "
                     "(ailes et queue animees par nos copies d'animations) ; genere par scripts/rune_aura_icons/gen_dragon_avatar.py.")
model["Model"] = REL + "/Dragon_Avatar.blockymodel"
model["Texture"] = REL + "/Dragon_Avatar.png"
model["EyeHeight"] = round(ww_json["EyeHeight"] * head_top / 198, 2)
model["CrouchOffset"] = ww_json.get("CrouchOffset", -1)
model["HitBox"] = {"Max": {"X": 0.45, "Y": round(head_top / 64 * 0.92, 2), "Z": 0.45}, "Min": {"X": -0.45, "Y": 0, "Z": -0.45}}
model.pop("DetailBoxes", None)
model["AnimationSets"] = sets
json.dump(model, open(SERVER_JSON, "w"), indent=2, ensure_ascii=False)
print("animations : %d personnalisees, %d jeux" % (len(anim_paths), len(sets)))
print("echelle conseillee pour x3 joueur (1.85 bloc) : %.2f" % (1.85 * 3 / (head_top / 64)))
