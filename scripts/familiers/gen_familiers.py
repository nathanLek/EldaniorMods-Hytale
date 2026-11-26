"""Familiers : animaux de compagnie lies a un joueur (package Java com.eldanior.system.familiers).

Premiere espece : le Fenrir (bete legendaire). Un petit loup blanc a l'eclosion, un loup normal adolescent, un Fenrir geant a
l'age adulte (collier brise de Gleipnir, cristaux de glace sur le dos, brume de givre a la queue).

Genere tout ce qui n'est pas du code :
- l'oeuf (bloc 1x1, 32 unites = 1 bloc, origine = centre bas) : modele intact (incubation, repos apres la mort) et etat
  "Eclos" (coquille ouverte, le familier est dehors), hitbox, objet et icone ;
- les textures du Fenrir (repeintes depuis le loup blanc vanilla) et ses ornements d'adulte (attachements sur les os Neck et
  Pelvis du loup) ;
- les apparences (Server/Models) : Parent Wolf_Black, une echelle par age ;
- le gabarit Template_Eldanior_Familier : copie de Template_Intelligent (combat, laisse) + une InteractionInstruction de
  monture (celle de Template_Livestock) reservee a ceux envers qui le familier est Friendly (son maitre : FamilierManager) ;
- les roles par age (Eldanior_Familier_Fenrir_Jeune / _Adolescent / _Adulte) ; le Jeune ne se monte pas ;
- la boutique du Dresseur de betes (NpcTrades.Metier.DRESSEUR).

Usage : python3 scripts/familiers/gen_familiers.py <src/main/resources> [dossier_apercu]
"""
import copy, json, math, os, sys, zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "rune_aura_icons"))
import png  # noqa: E402

RES = sys.argv[1]
PREVIEW = sys.argv[2] if len(sys.argv) > 2 else None
COMMON = os.path.join(RES, "Common")
SERVER = os.path.join(RES, "Server")
ASSETS = os.path.expanduser("~/Library/Application Support/Hytale/install/release/package/game/latest/Assets.zip")

EGG_HITBOX = "Eldanior_Oeuf_Familier"
TEMPLATE = "Template_Eldanior_Familier"
SHOP_ID = "Eldanior_Dresseur_Betes"
NPC_DIR = "NPC/Eldanior/Familiers/Fenrir"
MYSTERY_ID = "Eldanior_Oeuf_Mystere"          # objet d'affichage : recompense de la quete du Dresseur (oeuf tire au hasard)
BLOCK_DIR = "Blocks/Eldanior/Familiers"

# Especes (meme ordre et memes cles que familiers/Espece.java). Ages : (echelle, PV de base du role, vitesse, portee).
# Les PV et degats reels sont poses par FamilierManager (niveau et age) ; ceux-ci ne servent qu'a l'apparition.
# mount : indice de l'age a partir duquel il se monte (None : jamais) ; saddle : hauteur de selle par unite d'echelle.
# price : prix au Dresseur de betes (None : pas vendu, quete seulement).
COMMON_PRICE, UNCOMMON_PRICE = ("Elda_Gold_Coins", 25), ("Elda_Diamond_Coins", 10)   # prix x5 (2026-11-13)


def E(key, label, quality, vanilla, scales, hp, speed, reach, mount, saddle, young):
    rarity = {"Common": "Commune", "Uncommon": "Inhabituelle", "Rare": "Rare", "Epic": "Épique", "Legendary": "Légendaire"}[quality]
    price = COMMON_PRICE if quality == "Common" else UNCOMMON_PRICE if quality == "Uncommon" else None
    ages = [(scales[i], hp[i], speed + i, round(reach * (0.8 + 0.2 * i), 2)) for i in range(3)]
    return dict(key=key, label=label, quality=quality, rarity=rarity, vanilla=vanilla, ages=ages, mount=mount,
                saddle=saddle, price=price, young=young)


# saddle : hauteur de selle (blocs) par age = Espece.saddle (FamilierManager.mount)
ESPECES = [
    E("Renard", "Renard roux", "Common", "Fox", (0.6, 0.85, 1.1), (40, 90, 170), 9, 2.5, None, (0, 0, 0), "un renardeau roux, vif et rusé"),
    E("Chat", "Chat", "Common", "Cat", (0.6, 0.85, 1.15), (35, 80, 150), 9, 2.2, None, (0, 0, 0), "un chaton curieux"),
    E("Chien", "Chien", "Common", "Dog", (0.45, 0.7, 0.95), (45, 100, 190), 9, 2.4, None, (0, 0, 0), "un chiot fidèle"),
    E("Corbeau", "Corbeau", "Common", "Crow", (0.7, 1.0, 1.4), (30, 70, 130), 9, 2.0, None, (0, 0, 0), "un oisillon noir aux yeux vifs"),
    E("Loup", "Loup sombre", "Uncommon", "Wolf_Black", (0.6, 0.9, 1.25), (60, 140, 280), 10, 2.8, 2, (0, 0, 1.61),
      "un louveteau au pelage sombre, fidèle et endurant"),
    E("Cheval", "Cheval", "Uncommon", "Horse", (0.5, 0.8, 1.0), (60, 150, 300), 11, 2.6, 1, (0, 1.16, 1.45), "un poulain"),
    E("Faucon", "Faucon", "Uncommon", "Hawk", (0.6, 0.85, 1.2), (45, 110, 220), 11, 2.2, None, (0, 0, 0), "un fauconneau"),
    E("Ours", "Ours brun", "Rare", "Bear_Grizzly", (0.5, 0.8, 1.15), (100, 240, 500), 7, 3.4, 2, (0, 0, 1.67),
      "un ourson, qui deviendra une montagne de muscles"),
    E("OursPolaire", "Ours polaire", "Rare", "Bear_Polar", (0.5, 0.8, 1.15), (110, 260, 540), 7, 3.4, 2, (0, 0, 1.67),
      "un ourson blanc des glaces"),
    E("Leopard", "Léopard des neiges", "Rare", "Leopard_Snow", (0.55, 0.85, 1.2), (80, 200, 420), 10, 2.8, 2, (0, 0, 1.56),
      "un jeune léopard des neiges, silencieux et agile"),
    E("LoupBlanc", "Loup blanc", "Rare", "Wolf_White", (0.6, 0.9, 1.25), (80, 190, 400), 10, 2.8, 2, (0, 0, 1.61),
      "un louveteau des neiges"),
    E("Chouette", "Chouette des neiges", "Rare", "Owl_Snow", (0.7, 1.0, 1.4), (60, 150, 320), 10, 2.2, None, (0, 0, 0),
      "une chouette duveteuse"),
    E("Tigre", "Tigre à dents de sabre", "Epic", "Tiger_Sabertooth", (0.55, 0.9, 1.35), (100, 260, 580), 10, 3.0, 1, (0, 1.17, 1.76),
      "un petit tigre aux crocs déjà longs"),
    E("Raptor", "Raptor des cavernes", "Epic", "Raptor_Cave", (0.45, 0.75, 1.05), (90, 240, 540), 11, 2.8, 1, (0, 0.94, 1.31),
      "un petit raptor, déjà affamé"),
    E("Pterodactyle", "Ptérodactyle", "Epic", "Pterodactyl", (0.4, 0.65, 0.9), (80, 220, 500), 11, 2.6, None, (0, 0, 0),
      "un bébé ptérodactyle"),
    E("Fenrir", "Fenrir", "Legendary", "Wolf_Black", (0.55, 1.05, 1.8), (120, 320, 700), 11, 3.6, 1, (0, 1.35, 2.3),
      "un louveteau blanc, qui deviendra le Fenrir légendaire"),
    E("DragonGivre", "Dragon de givre", "Legendary", "Dragon_Frost", (0.3, 0.55, 0.85), (130, 340, 760), 11, 3.6, 1, (0, 1.3, 2.0),
      "un dragonneau de givre, né sous l'élément Glace"),
    E("DragonFeu", "Dragon de feu", "Legendary", "Dragon_Fire", (0.4, 0.75, 1.15), (130, 340, 760), 11, 3.6, 1, (0, 1.0, 1.55),
      "un dragonneau de feu, né sous l'élément Dragonic"),
]
AGE_NAMES = ["Jeune", "Adolescent", "Adulte"]
FOODS = [("Food_Pie_Meat", 2, "Elda_Gold_Coins"), ("Food_Kebab_Meat", 1, "Elda_Gold_Coins"),
         ("Food_Wildmeat_Raw", 3, "Elda_Silver_Coins"), ("Food_Beef_Raw", 3, "Elda_Silver_Coins"),
         ("Food_Fish_Raw", 2, "Elda_Silver_Coins")]


def egg_id(sp):
    return "Eldanior_Oeuf_" + sp["key"]


def role_id(sp, age):
    return "Eldanior_Familier_%s_%s" % (sp["key"], age)


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")


def vanilla(name):
    with zipfile.ZipFile(ASSETS) as z:
        return z.read(name)


def vanilla_png(name):
    tmp = os.path.join(os.environ.get("TMPDIR", "/tmp"), "eld_fam_" + os.path.basename(name))
    with open(tmp, "wb") as f:
        f.write(vanilla(name))
    return png.read(tmp)


# ---------------------------------------------------------------------------------------------------------------------
# Atlas et noeuds (comme scripts/presentoir/gen_presentoir.py)
# ---------------------------------------------------------------------------------------------------------------------
class Atlas:
    def __init__(self, width):
        self.w, self.x, self.y, self.row, self.rects, self.ids, self.seen = width, 0, 0, 0, [], 0, {}

    def put(self, w, h, paint, key=None):
        k = (key or id(paint), w, h)
        if k in self.seen:
            return self.seen[k]
        if self.x + w > self.w:
            self.x, self.y, self.row = 0, self.y + self.row, 0
        pos = (self.x, self.y)
        self.seen[k] = pos
        self.rects.append((pos, w, h, paint))
        self.x += w
        self.row = max(self.row, h)
        return pos

    def nid(self):
        self.ids += 1
        return str(self.ids)

    def image(self):
        h = max(32, (self.y + self.row + 31) // 32 * 32)
        img = [[[0, 0, 0, 0] for _ in range(self.w)] for _ in range(h)]
        for (ox, oy), w, rh, paint in self.rects:
            for j in range(rh):
                for i in range(w):
                    c = paint(i, j, w, rh)
                    img[oy + j][ox + i] = c if len(c) == 4 else c + [255]
        return self.w, h, img


ID_Q = (0, 0, 0, 1)
FACE_LIGHT = {"front": 1.0, "back": 0.84, "left": 0.92, "right": 0.92, "top": 1.1, "bottom": 0.62}


def vec(v):
    return {"x": v[0], "y": v[1], "z": v[2]}


def quat(q):
    return {"x": round(q[0], 6), "y": round(q[1], 6), "z": round(q[2], 6), "w": round(q[3], 6)}


def axis(ax, deg):
    h = math.radians(deg) / 2
    return (ax[0] * math.sin(h), ax[1] * math.sin(h), ax[2] * math.sin(h), math.cos(h))


def qmul(a, b):
    ax, ay, az, aw = a
    bx, by, bz, bw = b
    return (aw * bx + ax * bw + ay * bz - az * by, aw * by - ax * bz + ay * bw + az * bx,
            aw * bz + ax * by - ay * bx + az * bw, aw * bw - ax * bx - ay * by - az * bz)


def uv(pos):
    return {"offset": {"x": pos[0], "y": pos[1]}, "mirror": {"x": False, "y": False}, "angle": 0}


def box(atlas, name, center, size, paint, q=ID_Q, fullbright=False, px=2, key=None):
    """Tailles en unites ; px pixels de texture par unite (stretch 1/px). key : peinture partagee entre boites."""
    sx, sy, sz = [max(1, int(round(v * px))) for v in size]
    dims = {"front": (sx, sy), "back": (sx, sy), "left": (sz, sy), "right": (sz, sy), "top": (sx, sz), "bottom": (sx, sz)}
    layout = {}
    for f, (w, h) in dims.items():
        k = 1.0 if fullbright else FACE_LIGHT[f]
        shaded = lambda i, j, fw, fh, k=k, f=f: [min(255, int(c * k)) for c in paint(i, j, fw, fh, f)[:3]] + [255]
        layout[f] = uv(atlas.put(w, h, shaded, key=(key or id(paint), k, f)))
    return {"id": atlas.nid(), "name": name, "position": vec(center), "orientation": quat(q),
            "shape": {"type": "box", "offset": vec((0, 0, 0)), "stretch": vec((1.0 / px, 1.0 / px, 1.0 / px)),
                      "settings": {"size": vec((sx, sy, sz))}, "textureLayout": layout, "unwrapMode": "custom",
                      "visible": True, "doubleSided": False, "shadingMode": "fullbright" if fullbright else "flat"},
            "children": []}


def group(atlas, name, pos, children):
    return {"id": atlas.nid(), "name": name, "position": vec(pos), "orientation": quat(ID_Q),
            "shape": {"type": "none", "offset": vec((0, 0, 0)), "stretch": vec((1, 1, 1)), "settings": {}, "textureLayout": {},
                      "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": "flat"},
            "children": list(children)}


def clamp(c):
    return [max(0, min(255, int(v))) for v in c]


def noise(i, j, amp, seed=0):
    return (((i + seed * 7) * 73856093 ^ (j + seed * 13) * 19349663) >> 3) % (2 * amp + 1) - amp


def mix(a, b, t):
    return [a[k] + (b[k] - a[k]) * t for k in range(3)]


# ---------------------------------------------------------------------------------------------------------------------
# Oeufs (blocs) : meme modele pour toutes les especes (2026-11-13, demande de l'utilisateur) : un petit nid de paille et un
# oeuf lisse en tranches dodecagonales. Seules la texture de la coquille (motif de l'espece) et la rarete changent.
# Le modele est ecrit une fois (Oeuf_Familier[_Eclos].blockymodel) ; chaque espece a ses textures (meme disposition).
# ---------------------------------------------------------------------------------------------------------------------
WHITE = [246, 250, 255]
RUNE = [120, 232, 255]
STRAW = [214, 178, 92]
STRAW_DARK = [150, 112, 52]
EGG_Y0, EGG_Y1, EGG_CY, EGG_HY, EGG_R = 3, 29, 14.6, 12.6, 8.4

# Motifs de coquille : (couleur du bas, couleur du haut, couleur du motif, motif, lueur)
SHELLS = {
    "Renard":  ([214, 150, 92], [246, 226, 196], [170, 84, 40], "taches", None),
    "Loup":    ([70, 74, 86], [150, 156, 170], [36, 38, 46], "cendres", None),
    "Ours":    ([112, 76, 46], [176, 132, 88], [70, 46, 28], "taches", None),
    "Leopard": ([196, 204, 214], [246, 248, 252], [70, 76, 90], "rosettes", None),
    "Tigre":   ([226, 128, 40], [250, 196, 110], [40, 26, 20], "rayures", [255, 200, 120]),
    "Fenrir":  ([124, 168, 222], [246, 250, 255], [120, 232, 255], "runes", [120, 232, 255]),
    "Chat":    ([226, 208, 182], [250, 240, 225], [150, 110, 70], "rayures", None),
    "Chien":   ([200, 160, 110], [240, 215, 175], [120, 80, 50], "taches", None),
    "Corbeau": ([40, 42, 50], [96, 100, 118], [18, 18, 24], "cendres", None),
    "Cheval":  ([104, 66, 38], [168, 116, 72], [236, 226, 210], "taches", None),        # robe pie
    "Faucon":  ([196, 166, 126], [244, 230, 206], [118, 84, 54], "rayures", None),      # plumes barrees
    "OursPolaire": ([212, 222, 234], [250, 252, 255], [168, 188, 210], "taches", None),
    "LoupBlanc": ([198, 208, 224], [248, 250, 255], [148, 162, 184], "cendres", None),
    "Chouette": ([224, 230, 238], [255, 255, 255], [80, 85, 95], "taches", None),
    "Raptor":  ([84, 124, 66], [170, 200, 130], [44, 64, 36], "rayures", [170, 230, 120]),
    "Pterodactyle": ([116, 126, 106], [188, 194, 168], [66, 72, 58], "cendres", [200, 220, 160]),
    "DragonGivre": ([80, 140, 205], [222, 242, 255], [150, 240, 255], "runes", [140, 230, 255]),
    "DragonFeu": ([140, 34, 18], [255, 166, 66], [255, 226, 120], "runes", [255, 150, 60]),
    "Mystere": ([90, 60, 150], [236, 200, 120], [255, 240, 180], "etoiles", [255, 230, 160]),
}


def egg_radius(y):
    t = (y - EGG_CY) / EGG_HY
    return EGG_R * math.sqrt(max(0.0, 1 - t * t)) * (1 - 0.22 * t)


def h01(*v):
    """Hasard deterministe dans [0, 1)."""
    x = 0
    for k in v:
        x = (x * 1000003 + int(k) * 2654435761) & 0xFFFFFFFF
    x ^= x >> 13
    x = (x * 1274126177) & 0xFFFFFFFF
    return (x ^ (x >> 16)) / 4294967296.0


def shell_paint(shell, n, total, inside=False, k=0):
    """Peinture d'une tranche n (0 = bas) de la boite k (0, 30, 60 degres) selon le motif de l'espece. Seules les extremites des
    faces laterales depassent (sections tournees) : les motifs y sont places."""
    low, high, accent, pattern, glow_col = shell
    t = n / max(1, total - 1)
    base = mix(low, high, t ** 0.75)

    def paint(i, j, w, h, face):
        if inside and face == "top":
            cx, cz = (w - 1) / 2.0, (h - 1) / 2.0
            r = math.hypot((i - cx) / max(1, cx), (j - cz) / max(1, cz))
            if r < 0.78:
                g = glow_col or [120, 90, 60]
                glow = max(0.0, 1 - r / 0.78)
                return clamp(mix([28, 22, 30], g, glow * glow * 0.7))
        c = [base[q] + noise(i, j + n * 2, 4, 7) for q in range(3)]
        side = face in ("front", "back", "left", "right")
        u = i / max(1, w - 1)                                             # 0..1 le long de la face
        edge = side and (u < 0.22 or u > 0.78)
        fk = ("front", "back", "left", "right", "top", "bottom").index(face)
        if pattern == "taches" and side and edge:
            if h01(n // 2, i // 2, k, fk) < 0.16:
                c = mix(c, accent, 0.85)
        elif pattern == "cendres":
            if h01(n, i, k, fk, 3) < 0.12:
                c = mix(c, accent, 0.7)
            elif h01(n, i, k, fk, 5) < 0.05:
                c = mix(c, [190, 196, 210], 0.7)
        elif pattern == "rosettes" and side and edge:
            cell = (n // 3, (i // 3), k, fk)
            if h01(*cell) < 0.35:
                du, dv = (i % 3) - 1, (n % 3) - 1
                if abs(du) + abs(dv) == 2 or (du == 0 and dv == 0 and h01(*cell, 9) < 0.5):
                    c = mix(c, accent, 0.8)
        elif pattern == "rayures" and side and edge:
            wob = int(1.6 * math.sin(i * 0.9 + k * 2.0 + fk))
            if (n + wob) % 5 == 0 and h01(n, k, fk) < 0.85:
                c = mix(c, accent, 0.9)
        elif pattern == "runes" and side and edge:
            x = w * 0.11 + 1.3 * math.sin(n * 0.6 + len(face) * 1.7 + k * 1.1)
            d = abs(i + 0.5 - x) if face in (("front", "back"), ("right", "left"), ("front", "back"))[k] else 9
            if d < 1.0:
                c = accent
            elif d < 2.2:
                c = mix(c, accent, 0.4)
            if n == total // 2 and i % 3 == 1:
                c = mix(c, accent, 0.8)                                    # ceinture de runes au milieu de l'oeuf
        elif pattern == "etoiles":
            if h01(n, i, k, fk, 11) < 0.05:
                c = accent
        if face == "top" and not inside:
            c = mix(c, WHITE, 0.2)
        return clamp(c)
    return paint


def egg_slices(atlas, shell, y0, y1, open_top=False):
    """Coquille : tranches de 1 unite, trois boites par tranche (0, 30, 60 degres) = section a 12 cotes."""
    out = []
    total = y1 - y0
    for n in range(total):
        y = y0 + n
        r = egg_radius(y + 0.5)
        if r < 1.0:
            continue
        s = 2 * r * 0.94
        for k, deg in enumerate((0, 30, 60)):
            paint = shell_paint(shell, n, EGG_Y1 - EGG_Y0, inside=open_top and n == total - 1, k=k)
            hgt = 1.0 - 0.02 * k                                                    # pas de faces confondues
            name = "Coquille" if n == 0 and k == 0 else "Coquille_%d_%d" % (n, k)
            out.append(box(atlas, name, (0, y + hgt / 2.0, 0), (s, hgt, s), paint, q=axis((0, 1, 0), deg), key=("tranche", n, k, open_top)))
    return out


def straw(i, j, w, h, face):
    """Paille : brins dans le sens de la longueur, clairs et fonces."""
    along = j if face in ("left", "right", "front", "back") and h > w else i
    across = i if along == j else j
    c = mix(STRAW, STRAW_DARK, 0.55 * h01(across, 17))
    if h01(across, along // 3, 5) < 0.12:
        c = mix(c, [240, 214, 140], 0.6)
    return clamp([c[q] + noise(i, j, 6, 23) for q in range(3)])


def nest(atlas):
    """Petit nid de paille, commun a tous les oeufs."""
    nodes = []
    for k, deg in enumerate((0, 30, 60)):                                            # fond de paille tassee
        nodes.append(box(atlas, "Fond_%d" % k, (0, 0.75 - 0.02 * k, 0), (19, 1.5 - 0.04 * k, 19), straw, q=axis((0, 1, 0), deg), key="fond"))
    seg = 12
    for k in range(seg):                                                              # couronne de bottes de paille
        a = k * 2 * math.pi / seg
        tilt = 14 if k % 2 else -10
        q = qmul(axis((0, 1, 0), -math.degrees(a)), axis((1, 0, 0), tilt))
        nodes.append(box(atlas, "Botte_%d" % k, (10.5 * math.cos(a), 2.4 + 0.3 * (k % 3), 10.5 * math.sin(a)), (4, 3.5, 7.5), straw,
                         q=q, key="botte"))
    for k in range(8):                                                                # brins qui depassent
        a = k * 2 * math.pi / 8 + 0.35
        q = qmul(axis((0, 1, 0), -math.degrees(a) + 35 * (1 if k % 2 else -1)), axis((0, 0, 1), 12 + 8 * (k % 3)))
        nodes.append(box(atlas, "Brin_%d" % k, (12 * math.cos(a), 3.6 + 0.4 * (k % 2), 12 * math.sin(a)), (0.8, 0.8, 9), straw,
                         q=q, key="brin"))
    return nodes


def egg_model(shell, open_shell):
    atlas = Atlas(256)
    total = EGG_Y1 - EGG_Y0
    if open_shell:
        top = 15
        nodes = nest(atlas) + egg_slices(atlas, shell, EGG_Y0, top, open_top=True)
        r = egg_radius(top) * 0.9
        for k in range(14):                                                           # bord brise en dents
            a = k * 2 * math.pi / 14 + 0.2
            h = 2 + (k * 7) % 4
            q = qmul(axis((0, 1, 0), -math.degrees(a) + 90), axis((1, 0, 0), 12))
            nodes.append(box(atlas, "Dent_%d" % k, (r * math.cos(a), top + h / 2.0 - 0.3, r * math.sin(a)), (3, h, 1.2),
                             shell_paint(shell, 13, total), q=q, key="dent"))
        for k, (x, z, yaw, roll, w, d) in enumerate(((-7.5, -5, 30, 28, 8, 6), (7, 6.5, -40, -22, 7, 5), (2, -9, 75, 18, 5, 4))):
            nodes.append(box(atlas, "Eclat_%d" % k, (x, 3.2, z), (w, 1.4, d), shell_paint(shell, 20, total),
                             q=qmul(axis((0, 1, 0), yaw), axis((0, 0, 1), roll)), key=("eclat", k)))
    else:
        nodes = nest(atlas) + egg_slices(atlas, shell, EGG_Y0, EGG_Y1)
    return atlas, group(atlas, "Oeuf", (0, 0, 0), nodes)


def egg_icon_model(shell):
    """Icone de l'objet : l'oeuf seul, sans le nid (demande de l'utilisateur)."""
    atlas = Atlas(256)
    return atlas, group(atlas, "Oeuf", (0, 0, 0), egg_slices(atlas, shell, EGG_Y0, EGG_Y1))


# ---------------------------------------------------------------------------------------------------------------------
# Rendu de l'icone (orthographique, boites a un niveau sous la racine)
# ---------------------------------------------------------------------------------------------------------------------
def qrot(q, v):
    x, y, z, w = q
    t = (2 * (y * v[2] - z * v[1]), 2 * (z * v[0] - x * v[2]), 2 * (x * v[1] - y * v[0]))
    return (v[0] + w * t[0] + y * t[2] - z * t[1], v[1] + w * t[1] + z * t[0] - x * t[2], v[2] + w * t[2] + x * t[1] - y * t[0])


def faces_of(root):
    out = []
    for n in root["children"]:
        c, o = n["position"], n["orientation"]
        at, q = (c["x"], c["y"], c["z"]), (o["x"], o["y"], o["z"], o["w"])
        s, L = n["shape"], n["shape"]["textureLayout"]
        if s["type"] != "box":
            continue
        def world(p, at=at, q=q):
            r = qrot(q, p)
            return (at[0] + r[0], at[1] + r[1], at[2] + r[2])
        sz = s["settings"]["size"]
        st = s["stretch"]["x"]
        hx, hy, hz = sz["x"] * st / 2, sz["y"] * st / 2, sz["z"] * st / 2
        def add(face, w, h, fn):
            out.append((L[face], w, h, lambda u, v, fn=fn, world=world: world(fn(u * st, v * st))))
        add("front", sz["x"], sz["y"], lambda u, v, hx=hx, hy=hy, hz=hz: (-hx + u, hy - v, hz))
        add("back", sz["x"], sz["y"], lambda u, v, hx=hx, hy=hy, hz=hz: (hx - u, hy - v, -hz))
        add("right", sz["z"], sz["y"], lambda u, v, hx=hx, hy=hy, hz=hz: (hx, hy - v, hz - u))
        add("left", sz["z"], sz["y"], lambda u, v, hx=hx, hy=hy, hz=hz: (-hx, hy - v, -hz + u))
        add("top", sz["x"], sz["z"], lambda u, v, hx=hx, hy=hy, hz=hz: (-hx + u, hy, -hz + v))
        add("bottom", sz["x"], sz["z"], lambda u, v, hx=hx, hy=hy, hz=hz: (-hx + u, -hy, hz - v))
    return out


def render(root, tex, size, yaw_deg, pitch_deg):
    yaw, pitch = math.radians(yaw_deg), math.radians(pitch_deg)
    cy, sy, cp, sp = math.cos(yaw), math.sin(yaw), math.cos(pitch), math.sin(pitch)

    def project(v):
        x, y, z = v
        rx, rz = x * cy + z * sy, -x * sy + z * cy
        return rx, y * cp - rz * sp, y * sp + rz * cp

    faces = faces_of(root)
    pts = [project(f[3](u, v)) for f in faces for u, v in ((0, 0), (f[1], 0), (0, f[2]), (f[1], f[2]))]
    x0, x1 = min(p[0] for p in pts), max(p[0] for p in pts)
    y0, y1 = min(p[1] for p in pts), max(p[1] for p in pts)
    k = 0.94 * size / max(x1 - x0, y1 - y0)
    ox, oy = size / 2.0 - k * (x0 + x1) / 2, size / 2.0 + k * (y0 + y1) / 2
    img = [[[0, 0, 0, 0] for _ in range(size)] for _ in range(size)]
    zb = [[-1e9] * size for _ in range(size)]
    step = min(0.9, 1.0 / k)
    for layout, w, h, at in faces:
        tx0, ty0 = layout["offset"]["x"], layout["offset"]["y"]
        u = 0.0
        while u < w:
            v = 0.0
            while v < h:
                px, py, d = project(at(u, v))
                sx, sy2 = int(ox + k * px), int(oy - k * py)
                if 0 <= sx < size and 0 <= sy2 < size and d > zb[sy2][sx]:
                    col = tex[ty0 + int(v)][tx0 + int(u)]
                    if col[3]:
                        zb[sy2][sx] = d
                        img[sy2][sx] = list(col)
                v += step
            u += step
    return img


def downscale(img, f):
    n = len(img) // f
    out = []
    for y in range(n):
        row = []
        for x in range(n):
            px = [img[y * f + j][x * f + i] for j in range(f) for i in range(f)]
            a = sum(p[3] for p in px)
            row.append([0, 0, 0, 0] if a == 0 else [sum(p[k] * p[3] for p in px) // a for k in range(3)] + [a // len(px)])
        out.append(row)
    return out


def egg_item(sp):
    key = sp["key"]
    rare = sp["quality"] in ("Epic", "Legendary")                                # lueur et etincelles
    mount = ("Il peut être monté à partir de l'âge %s." % AGE_NAMES[sp["mount"]].lower()) if sp["mount"] is not None else "Il ne se monte pas."
    elem = {"Common": "", "Uncommon": "",
            "Rare": " Il naît sous un élément rare (Eau, Feu, Terre ou Air).",
            "Epic": " Il naît sous un élément épique (Éclair, Lumière, Ténèbres ou Nature).",
            "Legendary": " Bête légendaire : son élément est le sien depuis toujours."}[sp["quality"]]
    item = {
        "TranslationProperties": {
            "Name": "Œuf de " + sp["label"],
            "Description": "Bête %s. Posé au sol, l'œuf se lie à vous et éclot au bout de 5 minutes : %s.%s "
                           "Il vous suit, combat à vos côtés et gagne en force à chaque combat ; adulte, il peut évoluer et "
                           "apprendre une compétence. %s Gérez-le depuis son œuf. S'il meurt, l'œuf se brise et le lien est "
                           "rompu : -20 %% d'expérience pendant 7 jours. Un seul compagnon par maître."
                           % (sp["rarity"].lower(), sp["young"], elem, mount)
        },
        "Icon": "Icons/ItemsGenerated/%s.png" % egg_id(sp),
        "Categories": ["Eldanior_System.Consommables"],
        "Interactions": {"Primary": "Block_Primary", "Secondary": "Block_Secondary"},
        "BlockType": {
            "Material": "Solid",
            "DrawType": "Model",
            "Opacity": "Transparent",
            "CustomModel": BLOCK_DIR + "/Oeuf_Familier.blockymodel",
            "CustomModelTexture": [{"Texture": "%s/Oeuf_%s_Texture.png" % (BLOCK_DIR, key), "Weight": 1}],
            "HitboxType": EGG_HITBOX,
            "Gathering": {"Breaking": {"GatherType": "Benches"}},
            "BlockParticleSetId": "Stone",
            "ParticleColor": "#e8d8a0",
            "BlockSoundSetId": "Stone",
            "PhysicalMaterialId": "Stone",
            "TextureComputedColor": "#D8B870",
            "Support": {"Down": [{"FaceType": "Full"}]},
            "State": {"Definitions": {"Eclos": {
                "CustomModel": BLOCK_DIR + "/Oeuf_Familier_Eclos.blockymodel",
                "CustomModelTexture": [{"Texture": "%s/Oeuf_%s_Eclos_Texture.png" % (BLOCK_DIR, key), "Weight": 1}]
            }}},
            "Interactions": {"Use": {"Interactions": [{"Type": "Simple"}]}}
        },
        "PlayerAnimationsId": "Block",
        "IconProperties": {"Scale": 0.5, "Translation": [0, -14], "Rotation": [22.5, 45, 22.5]},
        "MaxStack": 1,
        "ItemLevel": {"Common": 5, "Uncommon": 10, "Rare": 15, "Epic": 18, "Legendary": 20}[sp["quality"]],
        "ItemSoundSetId": "ISS_Blocks_Stone",
        "Quality": sp["quality"]
    }
    glow = SHELLS[key][4]
    if rare and glow:                                                              # epique, legendaire : lueur et etincelles
        item["BlockType"]["Light"] = {"Color": "#%x%x%x" % tuple(c // 64 for c in glow)}
        item["BlockType"]["Particles"] = [{"SystemId": "Block_Gem_Sparks", "Color": "#%02x%02x%02x" % tuple(glow), "TargetNodeName": "Coquille"}]
    return item


def mystery_item():
    """Objet d'affichage (icone de la recompense de la quete du Dresseur) : jamais donne, l'oeuf reel est tire au rendu."""
    return {
        "TranslationProperties": {
            "Name": "Œuf mystérieux",
            "Description": "Un œuf tiré au hasard parmi les bêtes rares (62 %), épiques (30 %) et légendaires (8 %)."
        },
        "Icon": "Icons/ItemsGenerated/%s.png" % MYSTERY_ID,
        "MaxStack": 1,
        "ItemLevel": 15,
        "Quality": "Epic"
    }


def hitbox():
    return {"Boxes": [{"Min": {"X": 0.2, "Y": 0, "Z": 0.2}, "Max": {"X": 0.8, "Y": 0.95, "Z": 0.8}}]}


# ---------------------------------------------------------------------------------------------------------------------
# Textures du Fenrir (depuis le loup blanc vanilla)
# ---------------------------------------------------------------------------------------------------------------------
def wolf_faces(model):
    """Rectangles de texture (x, y, w, h) de chaque face, par noeud : {nom: {face: rect}}."""
    out = {}
    def walk(n):
        s = n.get("shape") or {}
        size = (s.get("settings") or {}).get("size")
        L = s.get("textureLayout") or {}
        if size and L:
            sx, sy, sz = size.get("x", 0), size.get("y", 0), size.get("z", 0)
            dims = {"front": (sx, sy), "back": (sx, sy), "left": (sz, sy), "right": (sz, sy), "top": (sx, sz), "bottom": (sx, sz)}
            faces = {}
            for f, lay in L.items():
                w, h = dims.get(f, (0, 0))
                if s.get("type") == "quad":
                    w, h = sx, sy
                if lay.get("angle", 0) in (90, 270):
                    w, h = h, w
                faces[f] = (int(lay["offset"]["x"]), int(lay["offset"]["y"]), int(w), int(h))
            out.setdefault(n["name"], faces)
        for c in n.get("children", []):
            walk(c)
    for n in model["nodes"]:
        walk(n)
    return out


def luma(c):
    return (c[0] * 0.3 + c[1] * 0.59 + c[2] * 0.11)


def paint_fenrir(base, faces, age):
    """Repeint la texture du loup blanc : neige pour le louveteau, argent et runes de givre pour l'adulte."""
    h, w = len(base), len(base[0])
    img = [[list(p) for p in row] for row in base]
    for y in range(h):
        for x in range(w):
            p = img[y][x]
            if p[3] == 0:
                continue
            l = luma(p)
            if age == "Jeune":       # neige : tres clair, ombre bleutee douce
                c = [l * 0.42 + 150, l * 0.42 + 154, l * 0.40 + 166]
            elif age == "Adolescent":
                c = [l * 0.55 + 118, l * 0.55 + 124, l * 0.52 + 140]
            else:                    # argent bleute, plus contraste
                c = [l * 0.78 + 62, l * 0.80 + 74, l * 0.78 + 98]
            img[y][x] = clamp(c) + [p[3]]

    def rect_paint(node, face, fn):
        r = faces.get(node, {}).get(face)
        if not r:
            return
        x0, y0, rw, rh = r
        for j in range(rh):
            for i in range(rw):
                yy, xx = y0 + j, x0 + i
                if 0 <= yy < h and 0 <= xx < w and img[yy][xx][3]:
                    c = fn(i, j, rw, rh, img[yy][xx])
                    if c is not None:
                        img[yy][xx] = clamp(c[:3]) + [img[yy][xx][3]]

    # Yeux bleu glace (pupille sombre -> lumineuse), pour tous les ages
    def eye(i, j, rw, rh, p):
        l = luma(p)
        if l < 120:
            return [70, 200, 255] if age == "Adulte" else [90, 170, 230]
        return None
    for n in ("R-Eye", "L-Eye"):
        for f in faces.get(n, {}):
            rect_paint(n, f, eye)

    if age == "Jeune":
        return img

    # Dos plus sombre (degrade gris-bleu) : dessus du bassin, de la poitrine, de la nuque, queue
    dark = [92, 112, 150] if age == "Adulte" else [150, 168, 196]
    def dorsal(i, j, rw, rh, p):
        t = 0.55 if age == "Adulte" else 0.35
        edge = min(i, rw - 1 - i) / max(1.0, rw / 2.0)
        return mix(p, dark, t * (1 - 0.6 * edge))
    for n in ("Pelvis", "Chest", "Neck"):
        rect_paint(n, "top", dorsal)
    for n in ("Tail", "Tail2", "Tail-End", "Tail-End2"):
        for f in faces.get(n, {}):
            rect_paint(n, f, lambda i, j, rw, rh, p: mix(p, dark, 0.25 + 0.35 * j / max(1, rh - 1)) if age == "Adulte" else None)

    # Runes de givre : chevrons sur les flancs et les pattes
    glow = [120, 230, 255] if age == "Adulte" else [170, 215, 245]
    strength = 1.0 if age == "Adulte" else 0.45
    def chevrons(i, j, rw, rh, p):
        period = 9
        k = (j + abs(i - rw // 2)) % period
        if k == 0:
            return mix(p, glow, strength)
        if k == 1 and age == "Adulte":
            return mix(p, glow, 0.35)
        return None
    for n in ("Pelvis", "Chest"):
        for f in ("left", "right"):
            rect_paint(n, f, chevrons)
    if age == "Adulte":
        def band(i, j, rw, rh, p):
            return mix(p, glow, 0.9) if j in (rh // 3, rh // 3 + 1) else None
        for n in ("L-Thigh", "R-Thigh", "L-Arm", "R-Arm", "L-Forearm", "R-Forearm", "L-Calf", "R-Calf"):
            for f in ("front", "back", "left", "right"):
                rect_paint(n, f, band)
    return img


# ---------------------------------------------------------------------------------------------------------------------
# Fenrir adulte : modele du loup + ornements greffes dans ses os (collier brise de Gleipnir sur le cou, pics de glace sur le
# dos et les epaules). Les enfants d'un noeud sont places par rapport au CENTRE de sa forme, dans son repere (le cou est
# tourne de 75 degres : son +Y local va vers l'avant-haut, son +Z vers le bas, la nuque est en -Z). Les attachements
# (DefaultAttachments) avaient fini au sol, aux pieds du Fenrir (essai du 2026-11-13).
# La texture du loup (256 x 160) est agrandie : les ornements sont peints en dessous.
# ---------------------------------------------------------------------------------------------------------------------
def iron(i, j, w, h, face):
    n = noise(i, j, 9, 23)
    c = [62 + n, 66 + n, 80 + n]
    if face in ("front", "back", "left", "right", "top") and (i % 8 in (3, 4)) and 1 <= j <= h - 2:
        c = RUNE                                                                       # runes gravees de Gleipnir
    if j == 0 or j == h - 1:
        c = mix(c, [150, 160, 180], 0.4)
    return clamp(c)


def chain(i, j, w, h, face):
    n = noise(i, j, 8, 29)
    edge = i in (0, w - 1) or j in (0, h - 1)
    k = -22 if edge else 0
    return clamp([104 + n + k, 110 + n + k, 126 + n + k])


def ice(i, j, w, h, face):
    t = j / max(1, h - 1)
    c = mix([244, 253, 255], [70, 175, 240], t)
    if (i + j) % 6 == 0:
        c = mix(c, [255, 255, 255], 0.5)
    return clamp(c)


def find_node(model, name):
    stack = list(model["nodes"])
    while stack:
        n = stack.pop()
        if n["name"] == name:
            return n
        stack.extend(n.get("children", []))
    return None


def adult_model(wolf_model, painted):
    """(modele, largeur, hauteur, texture) du Fenrir adulte."""
    model = copy.deepcopy(wolf_model)
    base_h = len(painted)
    atlas = Atlas(256)
    atlas.y, atlas.ids = base_h, 9000
    # Cou : collier sous la machoire, chaine brisee qui pend sous la gorge (+Z local = vers le bas), pics sur la nuque (-Z)
    neck = [box(atlas, "Gleipnir", (0, -7, 0), (56, 8, 48), iron, px=1)]
    for k, (y, z, rot) in enumerate(((-7, 26, 0), (-7, 31, 90))):                    # chaine courte (2026-11-13 : pendait aux genoux)
        neck.append(box(atlas, "Maillon_%d" % k, (5, y, z), (5, 2, 7), chain, q=axis((0, 0, 1), rot), px=1, key="maillon"))
    neck.append(box(atlas, "Maillon_Brise", (6, -6, 35), (4, 2, 4), chain, q=axis((0, 1, 0), 35), px=1, key="maillon"))
    for k, (x, y, h, w) in enumerate(((0, 4, 16, 6), (-9, -1, 12, 5), (9, -1, 12, 5), (0, -9, 11, 5))):
        q = qmul(axis((1, 0, 0), -90 - 25), axis((0, 1, 0), 45))                      # pointe vers la nuque, couchee vers l'arriere
        neck.append(box(atlas, "Glace_Nuque_%d" % k, (x, y, -21 - h / 2.0 + 4), (w, h, w), ice, q=q, px=1, fullbright=True,
                        key=("glace", h, w)))
    find_node(model, "Neck")["children"].extend(neck)
    # Dos (arriere du bassin) et epaules (avant de la poitrine) : pics de glace ; le milieu du dos reste libre pour le cavalier
    pelvis = []
    for k, (x, z, h, w) in enumerate(((0, 1, 14, 6), (-6, -5, 11, 5), (6, -5, 11, 5), (0, -10, 10, 5), (-5, -15, 7, 4), (5, -16, 6, 4))):
        q = qmul(axis((1, 0, 0), -22), axis((0, 1, 0), 45))
        pelvis.append(box(atlas, "Glace_Dos_%d" % k, (x, 16 + h / 2.0 - 4, z), (w, h, w), ice, q=q, px=1, fullbright=True,
                          key=("glace", h, w)))
    find_node(model, "Pelvis")["children"].extend(pelvis)
    chest = []
    for k, (x, z, h, w) in enumerate(((0, 8, 18, 7), (-9, 3, 13, 5), (9, 3, 13, 5))):
        q = qmul(axis((1, 0, 0), -18), axis((0, 1, 0), 45))
        chest.append(box(atlas, "Glace_Epaule_%d" % k, (x, 17 + h / 2.0 - 4, z), (w, h, w), ice, q=q, px=1, fullbright=True,
                         key=("glace", h, w)))
    find_node(model, "Chest")["children"].extend(chest)
    w, h, img = atlas.image()
    for y in range(base_h):
        img[y] = [list(p) for p in painted[y]]
    return model, w, h, img


def model_asset(sp, age, scale):
    if sp["key"] != "Fenrir":
        return {"Parent": sp["vanilla"], "MinScale": scale, "MaxScale": scale}
    data = {
        "Parent": "Wolf_Black",
        "Texture": "%s/Fenrir_%s.png" % (NPC_DIR, age),
        "MinScale": scale,
        "MaxScale": scale,
        "Icon": "Icons/ModelsGenerated/Wolf_White.png",
    }
    if age == "Adulte":
        data["Model"] = "%s/Fenrir_Adulte.blockymodel" % NPC_DIR
        data["Particles"] = [                                                         # aura de glace de l'adulte
            {"SystemId": "Bat_Ice", "TargetNodeName": "Tail", "TargetEntityPart": "Entity", "Scale": 1.2, "PositionOffset": {}},
            {"SystemId": "Effect_Snow", "TargetNodeName": "Chest", "TargetEntityPart": "Entity", "Scale": 1.6, "PositionOffset": {}},
            {"SystemId": "Weapon_Frost_Mist", "TargetNodeName": "Pelvis", "TargetEntityPart": "Entity", "Scale": 1.4, "PositionOffset": {}},
        ]
    elif age == "Adolescent":
        data["Particles"] = [{"SystemId": "Bat_Ice", "TargetNodeName": "Tail", "TargetEntityPart": "Entity", "Scale": 0.5,
                              "PositionOffset": {}}]
    return data


# ---------------------------------------------------------------------------------------------------------------------
# Gabarit et roles
# ---------------------------------------------------------------------------------------------------------------------
def template():
    t = json.loads(vanilla("Server/NPC/Roles/_Core/Templates/Template_Intelligent.json"))
    assert "InteractionInstruction" not in t, "Template_Intelligent a maintenant sa propre InteractionInstruction : fusionner"
    p = t["Parameters"]
    p["IsMountable"] = {"Value": False, "Description": "Eldanior : le maitre (attitude Friendly, posee par FamilierManager) peut le monter."}
    p["MountAnchorX"] = {"Value": 0, "Description": "Position de la selle (blocs)."}
    p["MountAnchorY"] = {"Value": 1.2, "Description": "Hauteur de la selle (blocs)."}
    p["MountAnchorZ"] = {"Value": 0, "Description": "Position de la selle (blocs)."}
    p["MountMovementConfig"] = {"Value": "Mount", "Description": "Deplacement du cavalier (MovementConfig vanilla des montures)."}
    t["$Comment"] = ("Eldanior : copie de Template_Intelligent (scripts/familiers/gen_familiers.py) + monture de Template_Livestock. "
                     "Familiers lies a un joueur : laisse, combat et stats pilotes par FamilierManager.")
    # 2026-11-13 : F sur le familier ouvre le choix Chevaucher / Gestion (familiers.FamilierInteractEvent, UseEntityEvent) ;
    # la monte passe par FamilierManager.mount (selle Espece.saddle). Interactif pour son maitre seulement (attitude Friendly).
    t["InteractionInstruction"] = {"Instructions": [
        {"Sensor": {"Type": "Not", "Sensor": {"Type": "CanInteract", "ViewSector": 360, "Attitudes": ["Revered", "Friendly"]}},
         "Actions": [{"Type": "SetInteractable", "Interactable": False}]},
        {"Continue": True, "Sensor": {"Type": "Any"},
         "Actions": [{"Type": "SetInteractable", "Interactable": True, "Hint": "server.interactionHints.eldanior_familier"}]},
        {"Sensor": {"Type": "HasInteracted"}, "Actions": [{"Type": "Nothing"}]}
    ]}
    return t


ROLE_PATHS = None


def vanilla_role(name):
    global ROLE_PATHS
    if ROLE_PATHS is None:
        with zipfile.ZipFile(ASSETS) as z:
            ROLE_PATHS = {os.path.basename(n)[:-5]: n for n in z.namelist() if n.startswith("Server/NPC/Roles/") and n.endswith(".json")}
    path = ROLE_PATHS.get(name)
    return json.loads(vanilla(path)) if path else None


def vanilla_attack(name, depth=0):
    """Attaque du role vanilla : interaction racine et variables (animations et degats ; les degats reels de la morsure sont
    recalcules par FamilierManager.biteDamage). Role sans attaque (chat, chien, oiseaux, dragons) : celle de son role parent,
    sinon la morsure du loup (pas d'animation de morsure sur ces modeles, mais les degats passent)."""
    d = vanilla_role(name)
    if d and depth < 3:
        m, params = d.get("Modify", {}), d.get("Parameters", {})
        attack = m.get("Attack")
        if isinstance(attack, dict):
            attack = params.get(attack.get("Compute"), {}).get("Value")
        if attack and m.get("_InteractionVars"):
            return attack, m["_InteractionVars"]
        if d.get("Reference") and vanilla_role(d["Reference"]):
            return vanilla_attack(d["Reference"], depth + 1)
    return vanilla_attack("Wolf_Black") if name != "Wolf_Black" else ("Root_NPC_Wolf_Attack", {})


def role(sp, age, hp, speed, reach, mountable, anchor):
    attack, ivars = vanilla_attack(sp["vanilla"])
    modify = {
        "Appearance": role_id(sp, age),
        "DropList": "Empty",
        "WakingPeriod": [0, 24],
        "BedBlockSet": "",
        "Patrol": False,
        "FollowPatrolPath": False,
        "ViewRange": 18,
        "ViewSector": 300,
        "HearingRange": 14,
        "AlertedRange": 30,
        "AlertedTime": [0.2, 0.4],
        "LeashMinPlayerDistance": 3,
        "LeashTimer": [1, 2],
        "SearchTimeRange": [3, 5],
        "BreathesInWater": True,
        "NameTranslationKey": {"Compute": "NameTranslationKey"},
        "MaxHealth": hp,
        "MaxSpeed": speed,
        "RunThreshold": 0.3,
        "KnockbackScale": 0.3,
        "Weapons": [],
        "OffHand": [],
        "DefaultOffHandSlot": -1,
        "NighttimeOffhandSlot": -1,
        "WakingIdleBehaviorComponent": "Component_Instruction_Eldanior_Leash_Follow",
        "WanderRadius": 1,                                                    # assis pres du maitre : il ne s'eloigne pas
        "LeashDistance": 40,
        "HardLeashDistance": 80,
        "UseCombatActionEvaluator": False,
        "Attack": attack,
        "AttackDistance": reach,
        "DesiredAttackDistanceRange": [round(reach * 0.55, 2), round(reach * 0.75, 2)],
        "AttackPauseRange": [0.9, 1.3],
        "CombatAttackPreDelay": [0.1, 0.25],
        "BlockAbility": "",
        "BlockProbability": 0,
        "AttitudeGroup": "Eldanior_Guard",
        "DefaultPlayerAttitude": "Ignore",
        "DisableDamageGroups": ["Self", "Eldanior_Guards"],
        "IsMemory": False,
        "IsMountable": mountable,
        "MountAnchorY": anchor,
        "_InteractionVars": ivars
    }
    return {
        "$Comment": "%s %s : familier lie a un joueur (FamilierManager : niveau, PV, degats, laisse, monture). Genere par "
                    "scripts/familiers/gen_familiers.py." % (sp["label"], age.lower()),
        "Type": "Variant",
        "Reference": TEMPLATE,
        "Modify": modify,
        "Parameters": {"NameTranslationKey": {"Value": "server.npcRoles.Eldanior_Familier_%s.name" % sp["key"],
                                              "Description": "Translation key for NPC name display"}}
    }


def shop():
    """Dresseur de betes (2026-11-13) : oeufs communs et inhabituels seulement, et de quoi nourrir son familier. Les oeufs
    rares et au-dela s'obtiennent par sa quete (« Un Œuf venu d'ailleurs »)."""
    slots = []
    for sp in ESPECES:
        if sp["price"]:
            slots.append({"Type": "Fixed", "Trade": {"Output": {"ItemId": egg_id(sp), "Quantity": 1},
                                                     "Input": [{"ItemId": sp["price"][0], "Quantity": sp["price"][1]}], "Stock": 3}})
    for item, qty, coin in FOODS:
        slots.append({"Type": "Fixed", "Trade": {"Output": {"ItemId": item, "Quantity": 1},
                                                 "Input": [{"ItemId": coin, "Quantity": qty}], "Stock": 32}})
    return {"DisplayNameKey": "Dresseur de bêtes", "RefreshInterval": {"Days": 7}, "RestockHour": 6, "TradeSlots": slots}


# ---------------------------------------------------------------------------------------------------------------------
# Fenetre de gestion du familier (Common/UI/Custom/Familier/Oeuf.ui, familiers.FamilierPage), ouverte depuis son oeuf.
# 2026-11-13 (amelioree a la demande de l'utilisateur) : portrait (icone de l'oeuf de l'espece), rarete en couleur, barres
# de vie, d'experience et de croissance, fiche en deux colonnes, panneau d'evolution (adulte : competence tiree au hasard),
# Nourrir, Suivre/Rester, Relacher ; outils d'administration en bas.
# ---------------------------------------------------------------------------------------------------------------------
UI_ICE, UI_LINE, UI_DARK, UI_BODY, UI_PANEL = "#9fdcff", "#3f6f94", "#0c141e", "#0b141d(0.97)", "#122030"
BARS = [("Vie", "Hp", "FamBarVie.png"), ("Expérience", "Xp", "FamBarXp.png"), ("Croissance", "Age", "FamBarAge.png")]
FIELDS = [("Âge", 0), ("Puissance", 1), ("Monture", 2), ("Nourriture", 3)]


def bar_png(c0, c1):
    w, h = 256, 16
    img = []
    for y in range(h):
        row = []
        for x in range(w):
            t = x / (w - 1)
            c = mix(c0, c1, t)
            if y < 4:
                c = mix(c, [255, 255, 255], 0.25)
            elif y > h - 4:
                c = mix(c, [0, 0, 0], 0.25)
            row.append(clamp(c) + [255])
        img.append(row)
    return w, h, img


def egg_ui():
    bars = "\n".join(f"""          Group {{
            Anchor: (Top: 6, Height: 20);
            LayoutMode: Left;
            Label {{
              Anchor: (Width: 120);
              Text: "{label}";
              Style: (FontSize: 12, RenderBold: true, RenderUppercase: true, TextColor: #7f93a8, VerticalAlignment: Center);
            }}
            Group {{
              FlexWeight: 1;
              Padding: (Top: 4, Bottom: 4);
              Group {{
                Background: #1b2938;
                ProgressBar #Fam{key}Bar {{
                  BarTexturePath: "{tex}";
                  Value: 0.0;
                }}
              }}
            }}
            Label #Fam{key}Text {{
              Anchor: (Width: 250);
              Style: (FontSize: 13, TextColor: #d6e2ee, HorizontalAlignment: End, VerticalAlignment: Center);
            }}
          }}""" for label, key, tex in BARS)

    def field(label, i):
        return f"""              Group {{
                Anchor: (Top: 4);
                LayoutMode: Top;
                Label {{
                  Text: "{label}";
                  Style: (FontSize: 11, RenderBold: true, RenderUppercase: true, TextColor: #6f8398);
                }}
                Label #FamVal{i} {{
                  Style: (FontSize: 14, TextColor: #d6e2ee, Wrap: true);
                }}
              }}"""

    left = "\n".join(field(l, i) for l, i in FIELDS[:2])
    right = "\n".join(field(l, i) for l, i in FIELDS[2:])

    def small(name, text):
        return f"""            $C.@SecondaryTextButton #Fam{name} {{ Text: "{text}"; FlexWeight: 1; }}"""

    return f"""$C = "../Common.ui";

// Fenetre de gestion d'un familier (familiers/FamilierPage), generee par scripts/familiers/gen_familiers.py.
Group {{
  LayoutMode: Middle;
  Background: #05080d(0.6);

  Group {{
    Anchor: (Width: 820);
    LayoutMode: Top;
    Padding: (Full: 2);
    Background: {UI_DARK};
    Group {{
      LayoutMode: Top;
      Padding: (Full: 2);
      Background: {UI_LINE};
      Group {{
        LayoutMode: Top;
        Background: {UI_BODY};
        Padding: (Left: 24, Right: 24, Top: 18, Bottom: 16);

        Group {{
          Anchor: (Height: 116);
          LayoutMode: Left;
          Group {{
            Anchor: (Width: 116, Height: 116);
            Padding: (Full: 3);
            Background: {UI_LINE};
            Group {{
              Background: #0f1b28;
              ItemIcon #FamPortrait {{ Anchor: (Left: 7, Top: 7, Width: 96, Height: 96); }}
            }}
          }}
          Group {{
            FlexWeight: 1;
            LayoutMode: Top;
            Padding: (Left: 18, Top: 4);
            Label #FamName {{
              Style: (FontSize: 24, RenderBold: true, RenderUppercase: true, TextColor: #eef6ff);
            }}
            Group {{
              Anchor: (Top: 2, Height: 22);
              LayoutMode: Left;
              Label #FamRarity {{
                Anchor: (Width: 130);
                Style: (FontSize: 14, RenderBold: true, RenderUppercase: true, TextColor: #c8c8c8, VerticalAlignment: Center);
              }}
              Label #FamSpecies {{
                FlexWeight: 1;
                Style: (FontSize: 14, TextColor: #b8c6d4, VerticalAlignment: Center);
              }}
            }}
            Label #FamElement {{
              Anchor: (Top: 2);
              Style: (FontSize: 14, RenderBold: true, TextColor: #9fdcff);
            }}
            Label #FamState {{
              Anchor: (Top: 6);
              Style: (FontSize: 14, TextColor: #f0c868);
            }}
          }}
        }}

        Group {{ Anchor: (Top: 12, Height: 2); Background: {UI_LINE}; }}

        Group {{
          Anchor: (Top: 8);
          LayoutMode: Top;
{bars}
        }}

        Group {{
          Anchor: (Top: 12);
          LayoutMode: Left;
          Group {{
            FlexWeight: 1;
            LayoutMode: Top;
            Background: {UI_PANEL};
            Padding: (Left: 14, Right: 14, Top: 6, Bottom: 10);
{left}
          }}
          Group {{ Anchor: (Width: 10); }}
          Group {{
            FlexWeight: 1;
            LayoutMode: Top;
            Background: {UI_PANEL};
            Padding: (Left: 14, Right: 14, Top: 6, Bottom: 10);
{right}
          }}
        }}

        Group {{
          Anchor: (Top: 10);
          LayoutMode: Top;
          Background: {UI_PANEL};
          Padding: (Left: 14, Right: 14, Top: 8, Bottom: 10);
          Group {{
            LayoutMode: Left;
            Label {{
              Anchor: (Width: 170);
              Text: "Compétence";
              Style: (FontSize: 12, RenderBold: true, RenderUppercase: true, TextColor: #7f93a8);
            }}
            Label #FamSkill {{
              FlexWeight: 1;
              Style: (FontSize: 15, RenderBold: true, TextColor: #e8c8a0, Wrap: true);
            }}
          }}
          Label #FamSkillDesc {{
            Anchor: (Top: 2);
            Style: (FontSize: 13, TextColor: #a9b8c8, Wrap: true);
          }}
          Group {{
            Anchor: (Top: 6);
            LayoutMode: Left;
            Label {{
              Anchor: (Width: 170);
              Text: "Prochaine évolution";
              Style: (FontSize: 12, RenderBold: true, RenderUppercase: true, TextColor: #7f93a8);
            }}
            Label #FamNext {{
              FlexWeight: 1;
              Style: (FontSize: 14, TextColor: #d6e2ee, Wrap: true);
            }}
          }}
        }}

        Group #FamEvolve {{
          Anchor: (Top: 10);
          LayoutMode: Left;
          Visible: false;
          Background: #2a2312(0.95);
          Padding: (Left: 14, Right: 10, Top: 8, Bottom: 8);
          Group {{
            FlexWeight: 1;
            LayoutMode: Top;
            Label {{
              Text: "Prêt à évoluer";
              Style: (FontSize: 14, RenderBold: true, RenderUppercase: true, TextColor: #f2c45e);
            }}
            Label #FamEvolveText {{
              Anchor: (Top: 2);
              Style: (FontSize: 13, TextColor: #d8c8a0, Wrap: true);
            }}
          }}
          $C.@TextButton #FamEvolveBtn {{ Text: "Faire évoluer"; Anchor: (Width: 210, Height: 44); }}
        }}

        Label #FamMsg {{
          Anchor: (Top: 8);
          Style: (FontSize: 13, TextColor: #c9b37a, Wrap: true, HorizontalAlignment: Center);
        }}

        Group {{
          Anchor: (Top: 8, Height: 44);
          LayoutMode: Left;
          $C.@TextButton #FamFollow {{ Text: "Me suivre"; FlexWeight: 1; }}
          Group {{ Anchor: (Width: 8); }}
          $C.@TextButton #FamFeed {{ Text: "Nourrir"; FlexWeight: 1; }}
          Group {{ Anchor: (Width: 8); }}
          $C.@SecondaryTextButton #FamRelease {{ Text: "Relâcher"; FlexWeight: 1; }}
          Group {{ Anchor: (Width: 8); }}
          $C.@SecondaryTextButton #FamClose {{ Text: "Fermer"; FlexWeight: 1; }}
        }}

        Group #FamAdmin {{
          Anchor: (Top: 12);
          LayoutMode: Top;
          Visible: false;
          Background: #1a1414(0.9);
          Padding: (Left: 12, Right: 12, Top: 8, Bottom: 10);
          Label {{
            Text: "Administration (tests)";
            Style: (FontSize: 12, RenderBold: true, RenderUppercase: true, TextColor: #e88a6a);
          }}
          Group {{
            Anchor: (Top: 6, Height: 40);
            LayoutMode: Left;
{small("AdmHatch", "Éclore")}
{small("AdmAge", "Âge suivant")}
{small("AdmElem", "Élément suivant")}
{small("AdmSkill", "Autre compétence")}
{small("AdmHeal", "Soigner")}
          }}
          Group {{
            Anchor: (Top: 4, Height: 40);
            LayoutMode: Left;
{small("AdmLvlDown", "-10 niveaux")}
{small("AdmLvlUp", "+10 niveaux")}
{small("AdmLvlMax", "Niveau max")}
{small("AdmMalus", "Retirer malus")}
          }}
        }}
      }}
    }}
  }}
}}
"""


def write_lang():
    """Noms des roles (server.npcRoles.Eldanior_Familier_<Cle>.name), ajoutes s'ils manquent."""
    path = os.path.join(SERVER, "Languages", "en-US", "server.lang")
    lines = open(path, encoding="utf-8").read().split("\n") if os.path.exists(path) else []
    have = {l.split("=", 1)[0] for l in lines if "=" in l}
    add = ["npcRoles.Eldanior_Familier_%s.name=%s" % (sp["key"], sp["label"]) for sp in ESPECES
           if "npcRoles.Eldanior_Familier_%s.name" % sp["key"] not in have]
    if "interactionHints.eldanior_familier" not in have:
        add.append("interactionHints.eldanior_familier=Appuyez sur [{key}] pour interagir")
    if add:
        while lines and lines[-1] == "":
            lines.pop()
        with open(path, "w", encoding="utf-8") as fh:
            fh.write("\n".join(lines + add) + "\n")


# ---------------------------------------------------------------------------------------------------------------------
# Ecriture
# ---------------------------------------------------------------------------------------------------------------------
block_dir = os.path.join(COMMON, *BLOCK_DIR.split("/"))
os.makedirs(block_dir, exist_ok=True)
for old in os.listdir(block_dir):                                                     # anciens modeles propres au Fenrir
    if old.startswith("Oeuf_Fenrir") and old.endswith(".blockymodel"):
        os.remove(os.path.join(block_dir, old))
models = {}
previews = {}
icons = {}
for key, shell in SHELLS.items():
    for opened in (False, True):
        suffix = "_Eclos" if opened else ""
        atlas, root = egg_model(shell, opened)
        w, h, tex = atlas.image()
        if key != "Mystere":
            png.write(os.path.join(block_dir, "Oeuf_%s%s_Texture.png" % (key, suffix)), w, h, tex)
        model = json.dumps({"nodes": [root], "lod": "auto"}, sort_keys=True)
        assert models.setdefault(suffix, model) == model, "disposition differente selon l'espece : " + key
        previews[(key, opened)] = (root, tex)
    ia, iroot = egg_icon_model(shell)
    iw, ih, itex = ia.image()
    big = render(iroot, itex, 256, 30, 14)
    icons[key] = big
    eid = MYSTERY_ID if key == "Mystere" else "Eldanior_Oeuf_" + key
    png.write(os.path.join(COMMON, "Icons", "ItemsGenerated", eid + ".png"), 64, 64, downscale(big, 4))
for suffix, model in models.items():
    write_json(os.path.join(block_dir, "Oeuf_Familier%s.blockymodel" % suffix), json.loads(model))
items_dir = os.path.join(SERVER, "Item", "Items", "Eldanior", "Familiers")
for sp in ESPECES:
    write_json(os.path.join(items_dir, egg_id(sp) + ".json"), egg_item(sp))
write_json(os.path.join(items_dir, MYSTERY_ID + ".json"), mystery_item())
write_json(os.path.join(SERVER, "Item", "Block", "Hitboxes", "Furniture", EGG_HITBOX + ".json"), hitbox())

wolf_model = json.loads(vanilla("Common/NPC/Beast/Wolf/Models/Model.blockymodel"))
faces = wolf_faces(wolf_model)
bw, bh, base = vanilla_png("Common/NPC/Beast/Wolf/Models/Model_Textures/White.png")
npc_dir = os.path.join(COMMON, *NPC_DIR.split("/"))
os.makedirs(npc_dir, exist_ok=True)
for sp in ESPECES:
    for i, (age, (scale, hp, speed, reach)) in enumerate(zip(AGE_NAMES, sp["ages"])):
        mountable = sp["mount"] is not None and i >= sp["mount"]
        anchor = float(sp["saddle"][i]) if mountable else 0.0
        if sp["key"] == "Fenrir":
            painted = paint_fenrir(base, faces, age)
            if age == "Adulte":
                amodel, aw, ah, atex = adult_model(wolf_model, painted)
                png.write(os.path.join(npc_dir, "Fenrir_Adulte.png"), aw, ah, atex)
                write_json(os.path.join(npc_dir, "Fenrir_Adulte.blockymodel"), amodel)
            else:
                png.write(os.path.join(npc_dir, "Fenrir_%s.png" % age), bw, bh, painted)
        write_json(os.path.join(SERVER, "Models", "Eldanior", "Familiers", role_id(sp, age) + ".json"), model_asset(sp, age, scale))
        write_json(os.path.join(SERVER, "NPC", "Roles", "Eldanior", "Familiers", role_id(sp, age) + ".json"),
                   role(sp, age, hp, speed, reach, mountable, anchor))
write_json(os.path.join(SERVER, "NPC", "Roles", "Eldanior", "Familiers", TEMPLATE + ".json"), template())
write_json(os.path.join(SERVER, "BarterShops", SHOP_ID + ".json"), shop())
write_lang()

ui_dir = os.path.join(COMMON, "UI", "Custom", "Familier")
os.makedirs(ui_dir, exist_ok=True)
with open(os.path.join(ui_dir, "Oeuf.ui"), "w", encoding="utf-8") as fh:
    fh.write(egg_ui())
for name, c0, c1 in (("FamBarVie.png", [170, 40, 40], [90, 220, 110]), ("FamBarXp.png", [60, 110, 220], [140, 220, 255]),
                     ("FamBarAge.png", [200, 140, 40], [255, 226, 130])):
    w, h, img = bar_png(c0, c1)
    png.write(os.path.join(ui_dir, name), w, h, img)
if os.path.exists(os.path.join(ui_dir, "FamilierEmblem@2x.png")):
    os.remove(os.path.join(ui_dir, "FamilierEmblem@2x.png"))

if PREVIEW:
    os.makedirs(PREVIEW, exist_ok=True)
    for (key, opened), (root, tex) in previews.items():
        if key == "Mystere" and opened:
            continue
        png.write(os.path.join(PREVIEW, "oeuf_%s%s.png" % (key, "_eclos" if opened else "")), 256, 256, render(root, tex, 256, 35, 22))
    for key, big in icons.items():
        png.write(os.path.join(PREVIEW, "icone_%s.png" % key), 128, 128, downscale(big, 2))
print("Familiers : %d especes, oeufs, roles, boutique et fenetre generes." % len(ESPECES))
