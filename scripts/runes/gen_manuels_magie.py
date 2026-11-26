"""Manuels de magie (2026-11-24, demande de l'utilisateur) : les runes de sort ne s'apprennent plus avec un parchemin mais en
lisant un manuel, comme les techniques du Clan Luo. UN modele de livre par type de magie (Feu, Glace, ...), partage par toutes
les runes du type ; un objet par rune (« Manuel de Feu : Boule de Feu »). Les types se font un par un : le type est ajoute a
scripts/runes/manuels_magie.json (« faits ») quand il est genere.

  Eldanior_Manuel_Magie_<Type>_<Rune>   grimoire relie de cuir a la couleur du type, coins et fermoir de metal, medaillon
                                        central portant l'embleme du type (Icons/ItemCategories/Runes/<Type>.png), dos a nerfs.
                                        Pages de velin, lignes d'encre et cercle runique. Maintenir le clic droit : le livre
                                        s'ouvre et ses pages tournent (animation Lecture de Luo_Manuel), et au bout de 2,5 s le
                                        sort Java ManuelMagieApprendre (skills/runes/spells/MagicManuals.java) donne la rune.

Le livre reprend la geometrie des manuels Luo (livre de sorts vanilla : Handle, Book-Top, Book-Bot, Page-Top, Page-Bot,
Bookmark), donc leurs animations. Une texture par type, une icone par rune (livre + embleme de la rune dans le coin).

Ce que fait le script pour un type :
  - texture du type, modele commun, icones, un objet par rune enseignee par un parchemin du type (Categories du parchemin) ;
  - le parchemin reste dans le jeu (ceux deja trouves s'apprennent toujours) mais quitte l'onglet creatif ;
  - remplace le parchemin par le manuel dans les tables de butin (LootTableConfig), les recompenses de quete (QuetesMetiers)
    et les prix (prix.json, meme prix) ; relancer ensuite scripts/economie/gen_economie.py.

Usage : python3 scripts/runes/gen_manuels_magie.py Feu [dossier_apercu]
        python3 scripts/runes/gen_manuels_magie.py --faits [dossier_apercu]     (regenere tous les types deja faits)
"""
import json, math, os, re, sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.normpath(os.path.join(HERE, "..", ".."))
RES = os.path.join(ROOT, "src", "main", "resources")
COMMON, SERVER = os.path.join(RES, "Common"), os.path.join(RES, "Server")
JAVA = os.path.join(ROOT, "src", "main", "java", "com", "eldanior", "system")
REGISTRY = os.path.join(HERE, "manuels_magie.json")

_argv = sys.argv
sys.argv = [_argv[0], RES]                     # le module Luo lit sys.argv a l'import
for folder in ("familles", "armures", "rune_aura_icons", "artefacts"):
    sys.path.insert(0, os.path.join(HERE, "..", folder))
import gen_voie_martiale_objets as V  # noqa: E402
sys.argv = _argv
import armor_kit as K  # noqa: E402
import png  # noqa: E402

D = V.D
FOLDER = "Items/Eldanior/Manuels"
BOOK_MODEL = FOLDER + "/Manuel_Magie.blockymodel"
PREFIX = "Eldanior_Manuel_Magie_"
ROOT_INTERACTION = "Root_Manuel_Magie_Lecture"
VELLUM, VELLUM_SHADE = (236, 224, 196), (198, 180, 146)
INK = (46, 32, 26)

# type : (nom affiche « Manuel de ... », cuir, metal, encre du cercle runique)
TYPES = {
    "Feu": ("Manuel de Feu", (128, 30, 22), (214, 160, 64), (176, 44, 24)),
    "Glace": ("Manuel de Glace", (52, 104, 150), (206, 222, 236), (54, 120, 176)),
    "Terre": ("Manuel de Terre", (84, 62, 36), (206, 168, 96), (96, 74, 36)),
    "Air": ("Manuel d'Air", (96, 150, 134), (236, 242, 238), (70, 136, 112)),
    "Foudre": ("Manuel de Foudre", (54, 40, 104), (236, 206, 80), (108, 76, 196)),
    "Tenebres": ("Manuel des Ténèbres", (34, 24, 40), (150, 120, 170), (88, 40, 110)),
    "Lumiere": ("Manuel de Lumière", (232, 222, 196), (220, 176, 70), (196, 150, 40)),
    "Temps": ("Manuel du Temps", (40, 72, 84), (200, 170, 100), (40, 110, 120)),
    "Espace": ("Manuel de l'Espace", (24, 28, 64), (190, 196, 230), (70, 70, 170)),
    "Spatial": ("Manuel Spatial", (60, 34, 90), (200, 190, 230), (120, 70, 170)),
    "Combat": ("Manuel de Combat", (58, 60, 68), (196, 198, 208), (130, 36, 30)),
    "Dragonic": ("Manuel Draconique", (40, 70, 40), (210, 170, 70), (120, 30, 30)),
    "Speciales": ("Manuel Spécial", (120, 96, 40), (240, 214, 130), (170, 120, 30)),
}
# Runes sans parchemin qui ont quand meme leur manuel (« le seul moyen d'avoir une rune, c'est un manuel », 2026-11-24) :
# la Rune d'Aura, donnee a 5 de Dignite.
EXTRA_RUNES = {"Speciales": ["Rune_Aura"]}


def shade(c, k):
    return tuple(max(0, min(255, int(v * k))) for v in c)


# ---------------------------------------------------------------------------------------------------------------------
# Peintures
# ---------------------------------------------------------------------------------------------------------------------
def shrink(src, size):
    """Reduction par moyenne (pixels RGBA, alpha premultiplie)."""
    h, w = len(src), len(src[0])
    out = []
    for y in range(size):
        row = []
        y0, y1 = y * h // size, max(y * h // size + 1, (y + 1) * h // size)
        for x in range(size):
            x0, x1 = x * w // size, max(x * w // size + 1, (x + 1) * w // size)
            r = g = b = a = n = 0
            for yy in range(y0, y1):
                for xx in range(x0, x1):
                    p = src[yy][xx]
                    r += p[0] * p[3]; g += p[1] * p[3]; b += p[2] * p[3]; a += p[3]; n += 1
            row.append([r / a, g / a, b / a, a / n] if a else [0, 0, 0, 0])
        out.append(row)
    return out


def emblem(type_id, size, metal):
    """Embleme du type (icone de categorie), recolore dans le metal de la reliure (garde le modele de lumiere)."""
    w, h, px = png.read(os.path.join(COMMON, "Icons", "ItemCategories", "Runes", type_id + ".png"))
    img = shrink(px, size)
    for row in img:
        for p in row:
            if p[3]:
                lum = (0.3 * p[0] + 0.59 * p[1] + 0.11 * p[2]) / 160.0
                c = shade(metal, 0.85 + 0.45 * min(1.0, lum))
                p[0], p[1], p[2] = c
    return img


def leather(w, h, c, seed):
    """Cuir : grain fin et marbrure douce."""
    img = K.canvas(w, h, c)
    for y in range(h):
        for x in range(w):
            k = 0.9 + 0.14 * K.noise(x * 0.35 + seed, y * 0.35) + 0.05 * K.noise(x * 1.7, y * 1.7 + seed)
            for i in range(3):
                img[y][x][i] *= k
    return img


def corner(img, x, y, dx, dy, metal, n=5):
    """Coin de metal en equerre (dx, dy : sens vers l'interieur)."""
    for i in range(n):
        for j in range(n - i):
            K.put(img, x + dx * i, y + dy * j, shade(metal, 1.15 if i == 0 or j == 0 else 0.95))
    K.put(img, x + dx, y + dy, shade(metal, 0.6))                         # rivet


def cover_paint(spec, type_id, outer):
    name, color, metal, ink = spec

    def paint(face, w, h, seed):
        if face in ("top", "bottom") and face != outer:                    # interieur : contregarde (velin)
            img = page_img(w, h, seed, ink, face == "bottom")
            K.rect(img, 0, 0, 2, h, shade(color, 0.85))
            return K.finish(img, face, edges=False)
        if face in ("top", "bottom"):                                       # plat de couverture
            img = leather(w, h, color, seed)
            dark, light = shade(color, 0.6), shade(color, 1.3)
            K.rect(img, 0, 0, w, 1, dark)
            K.rect(img, 0, h - 1, w, h, dark)
            K.rect(img, w - 1, 0, w, h, dark)
            for x in range(3, w - 3):                                       # filet dore en encadrement
                K.put(img, x, 3, metal, 0.85); K.put(img, x, h - 4, metal, 0.85)
            for y in range(3, h - 3):
                K.put(img, 4, y, metal, 0.85); K.put(img, w - 4, y, metal, 0.85)
            corner(img, 0, 0, 1, 1, metal)                                  # coins de metal
            corner(img, w - 1, 0, -1, 1, metal)
            corner(img, 0, h - 1, 1, -1, metal)
            corner(img, w - 1, h - 1, -1, -1, metal)
            cx, cy = w / 2.0 + 1, h / 2.0                                   # medaillon et embleme du type
            r = min(w, h) * 0.3
            for y in range(h):
                for x in range(w):
                    d = ((x + 0.5 - cx) ** 2 + (y + 0.5 - cy) ** 2) ** 0.5
                    if r - 1.4 <= d <= r:
                        K.put(img, x, y, metal)
                    elif d < r - 1.4:
                        K.put(img, x, y, shade(color, 0.7), 0.8)
            V.stamp(img, emblem(type_id, int(r * 1.5), metal), cx, cy)
            K.rect(img, w - 3, int(h * 0.44), w, int(h * 0.56), shade(metal, 0.9))   # fermoir
            K.put(img, w - 2, int(h * 0.5), shade(metal, 0.55))
            return K.finish(img, face, edges=False)
        img = K.canvas(w, h, VELLUM)                                        # tranches : feuillets dores
        for y in range(h):
            for x in range(w):
                k = 0.92 if (y + x // 6) % 2 == 0 else 1.0
                for i in range(3):
                    img[y][x][i] = img[y][x][i] * 0.55 * k + metal[i] * 0.45 * k
        K.rect(img, 0, 0, w, 1, shade(color, 0.8))
        K.rect(img, 0, h - 1, w, h, shade(color, 0.8))
        return K.finish(img, face, edges=False)
    return paint


def spine_paint(spec):
    name, color, metal, ink = spec

    def paint(face, w, h, seed):
        img = leather(w, h, shade(color, 0.9), seed)
        n = 4                                                               # nerfs du dos, filets dores
        for i in range(n):
            if face in ("left", "right", "front", "back"):
                cx = int((i + 0.5) * w / n)
                K.rect(img, cx - 1, 0, cx + 1, h, shade(color, 0.6))
                K.rect(img, cx + 1, 0, cx + 2, h, metal, 0.8)
            else:
                cy = int((i + 0.5) * h / n)
                K.rect(img, 0, cy, w, cy + 1, metal, 0.8)
        return K.finish(img, face, edges=False)
    return paint


def page_img(w, h, seed, ink, circle):
    img = K.canvas(w, h, VELLUM)
    for y in range(h):
        for x in range(w):
            k = 0.95 + 0.07 * K.noise(x * 0.3 + seed, y * 0.3)
            for i in range(3):
                img[y][x][i] *= k
    n = seed
    for y in range(4, h - 4, 3):                                            # lignes d'ecriture
        x = 3
        while x < w - 3:
            n = (n * 1103515245 + 12345) & 0x7fffffff
            L = 2 + n % 5
            if circle and abs(y - h * 0.55) < w * 0.32:
                break
            K.rect(img, x, y, min(w - 3, x + L), y + 1, INK, 0.7)
            x += L + 1
    if circle:                                                              # cercle runique a l'encre du type
        cx, cy, r = w / 2.0, h * 0.55, w * 0.3
        for y in range(h):
            for x in range(w):
                d = ((x + 0.5 - cx) ** 2 + (y + 0.5 - cy) ** 2) ** 0.5
                if abs(d - r) < 0.7 or abs(d - r * 0.62) < 0.6:
                    K.put(img, x, y, ink, 0.9)
        for i in range(6):
            a = i * math.pi / 3
            K.put(img, cx + math.cos(a) * r * 0.81, cy + math.sin(a) * r * 0.81, ink)
    return img


def bookmark_img(w, h, ink):
    img = K.canvas(w, h, ink)
    for y in range(h):
        for x in range(w):
            a = 1.0 if abs(x - w / 2.0) < 1.6 else 0.0
            if y > h * 0.75:
                a = 1.0 if abs(x - w / 2.0) < 1.6 and (y % 3) else 0.0        # bout effrange
            img[y][x][3] = int(255 * a)
    return img


# ---------------------------------------------------------------------------------------------------------------------
# Modele : geometrie des manuels Luo (livre de sorts vanilla)
# ---------------------------------------------------------------------------------------------------------------------
def build_book(type_id, seed):
    spec = TYPES[type_id]
    ink = spec[3]
    m = K.Model("Manuel_Magie")
    cover_bot, cover_top = cover_paint(spec, type_id, "bottom"), cover_paint(spec, type_id, "top")
    page_w, page_h = 18 * D, 26 * D
    w1 = {"x": 0, "y": 0, "z": 0, "w": -1}
    bot = V.boxn(m, "Book-Bot", {"x": 3, "y": 0, "z": 0}, (20, 4, 26), {"x": 7, "y": -2, "z": 0}, cover_bot)
    bot["children"] = [
        V.quadn(m, "Bookmark", {"x": 1, "y": 1, "z": 13}, bookmark_img(7 * D, 10 * D, ink), {"x": 0, "y": -5, "z": 0},
                {"x": -0.608761, "y": 0, "z": 0, "w": 0.793353}, "+Z"),
        V.quadn(m, "Page-Bot", {"x": -7, "y": 2.1, "z": 0}, page_img(page_w, page_h, seed + 1, ink, False), {"x": 8, "y": 0, "z": 0}, w1, "-Y"),
    ]
    top = V.boxn(m, "Book-Top", {"x": 3, "y": 0, "z": 0}, (20, 4, 26), {"x": 7, "y": 2, "z": 0}, cover_top)
    top["children"] = [V.quadn(m, "Page-Top", {"x": -7, "y": -2.1, "z": 0}, page_img(page_w, page_h, seed + 2, ink, True),
                               {"x": 8, "y": 0, "z": 0}, w1, "-Y")]
    handle = V.boxn(m, "Handle", {"x": -2, "y": 0, "z": -8}, (6, 9, 27), {"x": 0, "y": -2, "z": 0}, spine_paint(spec),
                    q={"x": 0.5, "y": -0.5, "z": 0.5, "w": 0.5})
    handle["children"] = [bot, top]
    origin_item = V.empty(m, "Origin_Item", {"x": 0, "y": -9.192388, "z": 0.707107}, {"x": -0.382683, "y": 0, "z": 0, "w": 0.92388}, [handle])
    origin_proj = V.empty(m, "Origin_Projectile", {"x": 2, "y": 7, "z": 14}, {"x": 0.382683, "y": 0, "z": 0, "w": 0.92388}, [origin_item])
    root = V.empty(m, "R-Attachment", {"x": -2, "y": 13, "z": -8}, dict(K.ID_Q), [origin_proj])
    return {"nodes": [root], "lod": "auto"}, m


# ---------------------------------------------------------------------------------------------------------------------
# Donnees
# ---------------------------------------------------------------------------------------------------------------------
def load_registry():
    if os.path.exists(REGISTRY):
        with open(REGISTRY, encoding="utf-8") as f:
            return json.load(f)
    return {"$Comment": "Types de magie dont les runes s'apprennent par manuel (gen_manuels_magie.py), un par un ; "
                        "manuels : parchemin remplace -> manuel.", "faits": [], "manuels": {}}


def page_to_rune():
    src = open(os.path.join(JAVA, "skills", "runes", "RuneManager.java"), encoding="utf-8").read()
    block = src[src.index("PAGE_TO_RUNE = Map.ofEntries("):]
    block = block[:block.index(");")]
    return dict(re.findall(r'Map\.entry\("(\w+)",\s*"(\w+)"\)', block))


def runes_of(type_id):
    """(parchemin, rune) des parchemins de ce type, tries par qualite puis nom."""
    pages = page_to_rune()
    done_pages = load_registry().get("manuels", {})
    out = []
    folder = os.path.join(SERVER, "Item", "Items", "Rune", "Parchemin")
    for f in sorted(os.listdir(folder)):
        if not f.endswith(".json"):
            continue
        d = json.load(open(os.path.join(folder, f), encoding="utf-8"))
        cats = d.get("Categories") or []
        page = f[:-5]
        done = done_pages.get(page, "")
        if ("Eldanior_Runes." + type_id in cats or done.startswith(PREFIX + type_id + "_")) and page in pages:
            out.append((page, pages[page]))
    out += [(None, rune) for rune in EXTRA_RUNES.get(type_id, [])]
    return out


def _zip():
    import zipfile
    return zipfile.ZipFile(V.ZIP)


def load_rune(rune):
    """Objet de la rune : celui du mod, sinon celui du jeu de base (Assets.zip : Tir Charge, Rage, Grappin...). None si absent."""
    path = os.path.join(SERVER, "Item", "Items", "Rune", "Ability", rune + ".json")
    if os.path.exists(path):
        return json.load(open(path, encoding="utf-8"))
    try:
        return json.loads(_zip().read("Server/Item/Items/Rune/Ability/%s.json" % rune))
    except KeyError:
        return None


def read_icon(rel):
    """Image du mod, sinon du jeu de base. (w, h, pixels) ou None."""
    path = os.path.join(COMMON, rel)
    if os.path.exists(path):
        return png.read(path)
    try:
        import tempfile
        data = _zip().read("Common/" + rel)
    except KeyError:
        return None
    with tempfile.NamedTemporaryFile(suffix=".png", delete=False) as f:
        f.write(data)
    try:
        return png.read(f.name)
    finally:
        os.remove(f.name)


# anciens parchemins ecrits sans accents (runes du jeu de base)
ACCENTS = [("Tir Charge", "Tir Chargé"), ("Impregnation", "Imprégnation"), ("energie", "énergie"), ("devastateur", "dévastateur"),
           ("Decuple", "Décuple"), (" a vous", " à vous"), ("s'accompagnent", "s'accompagnent")]


def accents(text):
    for a, b in ACCENTS:
        text = text.replace(a, b)
    return text


_LANG = {}


def vanilla_text(key):
    """Traduction francaise du jeu de base (Server/Languages/fr-FR/server.lang) d'une cle « server.items.X.name »."""
    if not _LANG:
        for line in _zip().read("Server/Languages/fr-FR/server.lang").decode("utf-8-sig").splitlines():
            if "=" in line and not line.lstrip().startswith("#"):
                k, v = line.split("=", 1)
                _LANG[k.strip()] = v.strip()
    return _LANG.get(key[len("server."):] if key.startswith("server.") else key)


def page_texts(page):
    """Nom du sort et description tires du parchemin (runes du jeu de base : leurs textes sont des cles anglaises)."""
    d = json.load(open(os.path.join(SERVER, "Item", "Items", "Rune", "Parchemin", page + ".json"), encoding="utf-8"))
    name = d["TranslationProperties"]["Name"].split(":", 1)[-1].strip()
    desc = d["TranslationProperties"].get("Description", "")
    m = re.search(r"(?:Apprend|Enseigne) (?:la |le |l')?(?:Rune (?:de |d'|du |des )?)?[^:]*:\s*(.*?)(?:Utilisez-le.*)?$", desc)
    body = m.group(1).strip() if m else desc
    body = body.replace(" (Ancien parchemin : la rune s'apprend désormais dans un manuel.)", "").strip()
    return accents(name), accents(body[:1].upper() + body[1:])


def manual_id(type_id, rune):
    return PREFIX + type_id + "_" + rune[len("Rune_"):]


def spell_name(rune_json):
    n = rune_json["TranslationProperties"]["Name"]
    for p in ("Rune de la ", "Rune de l'", "Rune du ", "Rune des ", "Rune de ", "Rune d'", "Rune : ", "Rune "):
        if n.startswith(p):
            return n[len(p):][:1].upper() + n[len(p):][1:]
    return n


def item_icon(tree, tex, rune_json):
    """Icone : le livre du type, et l'embleme de la rune dans le coin bas droit (chaque manuel se reconnait)."""
    img = V.icon(tree, tex)
    got = read_icon(rune_json.get("Icon", "")) if rune_json.get("Icon") else None
    if got:
        w, h, px = got
        small = shrink(px, 30)
        for y in range(30):
            for x in range(30):
                p = small[y][x]
                if p[3]:
                    q = img[34 + y][34 + x]
                    a = p[3] / 255.0
                    for i in range(3):
                        q[i] = q[i] * (1 - a) + p[i] * a
                    q[3] = max(q[3], p[3])
    return img


def replace_everywhere(swaps):
    """Parchemin -> manuel dans les tables de butin, les recompenses de quete et les prix."""
    touched = []
    for rel in ("config/configs/LootTableConfig.java", "quest/chapitres/QuetesMetiers.java"):
        path = os.path.join(JAVA, rel)
        s = open(path, encoding="utf-8").read()
        s2 = s
        for page, book in swaps.items():
            s2 = s2.replace('"' + page + '"', '"' + book + '"')
        if s2 != s:
            open(path, "w", encoding="utf-8").write(s2)
            touched.append(rel)
    prix_path = os.path.join(ROOT, "scripts", "economie", "prix.json")
    prix = json.load(open(prix_path, encoding="utf-8"))
    changed = False

    def walk(o):
        nonlocal changed
        if isinstance(o, dict):
            for page, book in swaps.items():
                if page in o and isinstance(o[page], (int, float)) and book not in o:
                    o[book] = o[page]
                    changed = True
            for v in o.values():
                walk(v)
    walk(prix)
    if changed:
        with open(prix_path, "w", encoding="utf-8") as f:       # meme ecriture que gen_economie.py (indentation 1)
            json.dump(prix, f, ensure_ascii=False, indent=1)
            f.write("\n")
        touched.append("scripts/economie/prix.json")
    return touched


def hide_page(page):
    """Le parchemin reste (ceux deja trouves s'apprennent toujours) mais quitte l'onglet creatif."""
    path = os.path.join(SERVER, "Item", "Items", "Rune", "Parchemin", page + ".json")
    d = json.load(open(path, encoding="utf-8"))
    if "Categories" in d:
        del d["Categories"]
        d["TranslationProperties"]["Description"] = d["TranslationProperties"].get("Description", "") + \
            " (Ancien parchemin : la rune s'apprend désormais dans un manuel.)"
        V.write_json(path, d)


def write_interactions():
    inter = os.path.join(SERVER, "Item", "Interactions", "Manuels")
    roots = os.path.join(SERVER, "Item", "RootInteractions", "Manuels")
    close = {"Type": "Simple", "RunTime": 0.15, "Effects": {"ClearAnimationOnFinish": True}}
    V.write_json(os.path.join(inter, "Manuel_Magie_Lecture.json"), {
        "$Comment": "Manuel de magie (gen_manuels_magie.py) : maintenir le clic droit = lecture (livre ouvert, pages qui tournent) ; "
                    "2,5 s = rune apprise (sort Java ManuelMagieApprendre). Relache avant : le livre se referme.",
        "Type": "Charging", "DisplayProgress": True, "HorizontalSpeedMultiplier": 0.3,
        "Effects": {"ItemAnimationId": "Lecture", "LocalSoundEventId": "SFX_Drag_Weapons_Books"},
        "Next": {"0": close,
                 str(V.READ_S): {"Type": "Serial", "Interactions": [{"Type": "RuneSpell", "RunTime": 0.05, "Spell": "ManuelMagieApprendre"},
                                                              close]}}})
    V.write_json(os.path.join(roots, ROOT_INTERACTION + ".json"), {"RequireNewClick": True, "Interactions": ["Manuel_Magie_Lecture"]})


def generate(type_id, preview=None):
    if type_id not in TYPES:
        raise SystemExit("Type inconnu : %s (%s)" % (type_id, ", ".join(TYPES)))
    title = TYPES[type_id][0]
    runes = runes_of(type_id)
    if not runes:
        raise SystemExit("Aucun parchemin de type " + type_id)
    os.makedirs(os.path.join(COMMON, FOLDER), exist_ok=True)
    tree, m = build_book(type_id, 71 + sorted(TYPES).index(type_id) * 13)
    luo_tree, _ = V.build_book((10, 10, 10), 1)
    if V.layout_key(luo_tree) != V.layout_key(tree):
        raise SystemExit("La disposition de l'atlas differe de celle des manuels Luo (animations)")
    V.write_json(os.path.join(COMMON, BOOK_MODEL), V.held_model(tree, V.EXTRA_TURN))
    w, h, tex = m.texture()
    tex_rel = "%s/Manuel_Magie_%s_Texture.png" % (FOLDER, type_id)
    png.write(os.path.join(COMMON, tex_rel), w, h, tex)
    write_interactions()
    swaps, previews, made = {}, [], []
    for page, rune in runes:
        rj = load_rune(rune)
        if rj is None:
            print("  ! rune absente (ni mod ni jeu de base), manuel ignore :", rune)
            continue
        item = manual_id(type_id, rune)
        name = spell_name(rj)
        desc = rj["TranslationProperties"].get("Description", "")
        if page and (name.startswith("server.") or desc.startswith("server.")):          # rune du jeu de base
            p_name, p_desc = page_texts(page)                                     # textes du parchemin, a defaut
            classes = re.search(r"Classes : [^.]*\.", p_desc)
            name = vanilla_text(rj["TranslationProperties"]["Name"]) or p_name   # traduction francaise du jeu
            v_desc = vanilla_text(desc)
            desc = (v_desc + (" " + classes.group(0) if classes else "")) if v_desc else p_desc
        icon_rel = "Icons/ItemsGenerated/%s.png" % item
        ic = item_icon(tree, tex, rj)
        png.write(os.path.join(COMMON, icon_rel), 64, 64, ic)
        V.write_json(os.path.join(SERVER, "Item", "Items", "Rune", "Manuels", type_id, item + ".json"), {
            "$Comment": "Manuel de magie, genere par scripts/runes/gen_manuels_magie.py. Maintenir le clic droit : lecture, puis le sort "
                        "Java ManuelMagieApprendre enseigne " + rune + (" (remplace le parchemin " + page + ")." if page else " (rune sans parchemin)."),
            "TranslationProperties": {
                "Name": title + " : " + name,
                "Description": desc + "\n\nMaintenez le clic droit pour lire le manuel et apprendre la rune. Le manuel se consume une fois lu."},
            "Categories": ["Eldanior_Runes." + type_id], "Quality": rj.get("Quality", "Common"), "ItemLevel": rj.get("ItemLevel", 10),
            "Model": BOOK_MODEL, "Texture": tex_rel, "Icon": icon_rel, "PlayerAnimationsId": V.ANIM_SET,
            "IconProperties": {"Scale": 0.6, "Rotation": [0, 0, 0], "Translation": [0, -4]},
            "Interactions": {"Secondary": ROOT_INTERACTION},
            "Tags": {"Type": ["Item"]}, "ItemSoundSetId": "ISS_Weapons_Books", "MaxStack": 1, "Scale": 1.0,
        })
        made.append(item)
        if page:
            hide_page(page)
            swaps[page] = item
        if preview:
            previews.append(ic)
    touched = replace_everywhere(swaps)
    reg = load_registry()
    if type_id not in reg["faits"]:
        reg["faits"].append(type_id)
    reg.setdefault("manuels", {}).update(swaps)
    V.write_json(REGISTRY, reg)
    if preview:
        import pose_kit as P  # noqa: E402
        os.makedirs(preview, exist_ok=True)
        big = [V.icon(tree, tex, 256, -30, 24), V.icon(tree, tex, 256, 150, -24)]
        icons = [scale_up(i, 4) for i in previews]
        sheet = big + icons
        cols = 4
        while len(sheet) % cols:
            sheet.append([[[30, 30, 36, 255] for _ in range(256)] for _ in range(256)])
        png.write(os.path.join(preview, "manuels_%s.png" % type_id), 256 * cols, 256 * (len(sheet) // cols), P.sheet(sheet, cols))
    print("%s : %d manuels (%s). Fichiers modifies : %s" % (title, len(made), ", ".join(made), ", ".join(touched) or "aucun"))
    return swaps


def scale_up(img, f):
    return [[list(img[y // f][x // f]) for x in range(len(img[0]) * f)] for y in range(len(img) * f)]


def main():
    args = sys.argv[1:]
    if not args:
        raise SystemExit(__doc__)
    preview = args[1] if len(args) > 1 else None
    types = load_registry()["faits"] if args[0] == "--faits" else [args[0]]
    for t in types:
        generate(t, preview)


if __name__ == "__main__":
    main()
