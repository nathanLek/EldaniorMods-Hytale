"""Generateur des butins de monstres : catalogue -> icones, modeles 3D, textures, items, tables de drops, table Java.

Usage :
  python3 scripts/butin/gen_butin.py sheet <dossier>   planches d'icones de controle, rien n'est ecrit dans le mod
  python3 scripts/butin/gen_butin.py all               ecrit tout dans src/main (a relancer apres un changement du catalogue)

Le modele 3D est le sprite 32x32 extrude en trois epaisseurs (bord fin, milieu, centre bombe), donc l'item au sol a la meme
silhouette et les memes couleurs que son icone. 1 pixel = 1 unite ; les boites sont fusionnees en grands rectangles.
"""
import glob, json, multiprocessing, os, re, sys, unicodedata, zlib

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
sys.path.insert(0, os.path.join(HERE, "..", "rune_aura_icons"))
import png  # noqa: E402
from catalog import FAMILIES  # noqa: E402
import sprites  # noqa: E402

ROOT = os.path.abspath(os.path.join(HERE, "..", ".."))
RES = os.path.join(ROOT, "src", "main", "resources")
JAVA = os.path.join(ROOT, "src", "main", "java", "com", "eldanior", "system", "monsterloot", "MonsterLootTable.java")
VANILLA = os.path.expanduser("~/Desktop/Assets Hytale/Server")

# Accents des noms affiches (le catalogue reste en ASCII pour les identifiants)
ACCENTS = {"Coeur": "Cœur", "Oeil": "Œil", "Oeuf": "Œuf", "Ecaille": "Écaille", "Eclat": "Éclat", "Epine": "Épine", "Etincelle": "Étincelle",
           "Spirituelle": "Spirituelle", "Crane": "Crâne", "Machoire": "Mâchoire", "Ame": "Âme", "Seve": "Sève", "Gelee": "Gelée", "Gele": "Gelé",
           "Eternelle": "Éternelle", "Charge": "Chargé", "Yeti": "Yéti", "Hyene": "Hyène", "Leopard": "Léopard", "Lezard": "Lézard",
           "Pterodactyle": "Ptérodactyle", "Archeopteryx": "Archéoptéryx", "Crete": "Crête", "Larve": "Larve", "Cristallisee": "Cristallisée",
           "Petrifie": "Pétrifié", "Tranche": "Tranché", "Ebrechee": "Ébréchée", "Trophee": "Trophée", "Dechire": "Déchiré", "Defense": "Défense",
           "Fetiche": "Fétiche", "Sorciere": "Sorcière", "Fletri": "Flétri", "Sechee": "Séchée", "Ensable": "Ensablé", "Desseche": "Desséché",
           "Carbonisee": "Carbonisée", "Calcine": "Calciné", "Mutee": "Mutée", "Bleme": "Blême", "Meche": "Mèche", "Fer": "Fer", "Avariee": "Avariée",
           "Sale": "Salé", "Use": "Usé", "Brisee": "Brisée", "Tresor": "Trésor", "Noirci": "Noirci", "Funeraire": "Funéraire", "Volee": "Volée",
           "Felee": "Fêlée", "Fele": "Fêlé", "Secrete": "Secrète", "Rouage": "Rouage", "Fige": "Figé", "Residu": "Résidu", "Electrique": "Électrique",
           "Ronceuse": "Ronceuse", "Ecorce": "Écorce", "Tete": "Tête", "Desert": "Désert", "Riviere": "Rivière", "Araignee": "Araignée",
           "Scarabee": "Scarabée", "Bete": "Bête", "Betail": "Bétail", "Goule": "Goule", "Reine": "Reine", "Brule": "Brûlé", "Maitre": "Maître",
           "Mystique": "Mystique", "Clee": "Clé", "Cle": "Clé", "Voute": "Voûte", "Tricorne": "Tricorne", "Rouille": "Rouillé", "Mort-vivant": "Mort-vivant",
           "Morts-vivants": "Morts-vivants", "Murene": "Murène", "Radicelle": "Radicelle", "Peche": "Pêche", "Dechiree": "Déchirée", "Serree": "Serrée",
           "a": "à", "Glacee": "Glacée", "Mutante": "Mutante", "Interdite": "Interdite", "Graisseux": "Graisseux", "Armure": "Armure", "Rampant": "Rampant"}


def pretty(s):
    return re.sub(r"[A-Za-z]+", lambda m: ACCENTS.get(m.group(0), m.group(0)), s)


def slug(name):
    t = unicodedata.normalize("NFD", name).encode("ascii", "ignore").decode()
    words = [w for w in re.split(r"[^A-Za-z0-9]+", t.replace("d'", " ")) if w and w.lower() not in ("de", "du", "des", "la", "le", "a", "d")]
    return "Butin_" + "_".join(w[0].upper() + w[1:] for w in words)


class Pal:
    def __init__(self, m, s, x, pat):
        self.m, self.s, self.x, self.pat = m, s, x, pat


def items():
    """Liste aplatie des items a generer. Rangs : 0 commun, 1 rare, 2 precieux, 3 trophee."""
    out = []
    seen = {}                                                       # rang de l'item parmi ceux de sa forme : choisit le style
    for f in FAMILIES:
        c1, c2, c3 = f["cols"]
        for rank, it in enumerate(f["items"]):
            name, kind = it[0], it[1]
            if kind == "existing":
                out.append(dict(id=name, name=name, kind=kind, rank=rank, fam=f))
                continue
            pat = it[2] if len(it) > 2 else f["pat"]
            order = {0: (c1, c2, c3), 1: (c2, c1, c3), 2: (c3, c1, c2), 3: (sprites.mix(c1, sprites.GOLD, .35), c2, c3)}[rank]
            seen[kind] = seen.get(kind, -1) + 1
            out.append(dict(id=slug(name), name=pretty(name), kind=kind, rank=rank, fam=f, m=order[0], s=order[1], x=order[2], pat=pat, var=seen[kind]))
    ids = [i["id"] for i in out]
    dup = {x for x in ids if ids.count(x) > 1}
    assert not dup, dup
    return out


def draw(it):
    seed = zlib.crc32(it["id"].encode())
    a = sprites.Art(seed)
    a.v = it["var"]
    # Teinte propre a l'item : leger decalage derive de la graine
    p = Pal(_tint(it["m"], seed), _tint(it["s"], seed >> 8), _tint(it["x"], seed >> 16), it["pat"])
    sprites.SHAPES[it["kind"]](a, p)
    return sprites.render(a, sprites.mix(sprites.dk(p.m, .7), "#0a0812", .4))


def _tint(col, seed):
    k = (seed & 255) / 255.0 - 0.5
    return sprites.mix(col, "#ffffff" if k > 0 else "#101018", abs(k) * 0.14)


def render_one(it):
    return it["id"], draw(it)


def all_art(todo):
    with multiprocessing.Pool() as pool:
        return dict(pool.map(render_one, todo, chunksize=4))


# ---------------- planche de controle ----------------
def sheet(folder, only=None):
    its = [i for i in items() if i["kind"] != "existing" and (not only or only in i["fam"]["section"] or only == i["kind"])]
    art = all_art(its)
    cols, cell = 12, 68
    per = 96
    for page in range(0, len(its), per):
        chunk = its[page:page + per]
        rows = -(-len(chunk) // cols)
        W, H = cols * cell, rows * cell
        px = [[[46, 42, 50, 255] for _ in range(W)] for _ in range(H)]
        for n, it in enumerate(chunk):
            ic = art[it["id"]][0]
            ox, oy = (n % cols) * cell + 2, (n // cols) * cell + 2
            for y in range(64):
                for x in range(64):
                    c = ic[y][x]
                    if c[3]:
                        t = c[3] / 255
                        d = px[oy + y][ox + x]
                        px[oy + y][ox + x] = [d[k] + (c[k] - d[k]) * t for k in range(3)] + [255]
        path = os.path.join(folder, "sheet_%02d.png" % (page // per))
        png.write(path, W, H, px)
        print(path, [i["kind"] for i in chunk][:0])


# ---------------- modele 3D ----------------
def erode(mask, n):
    m = [row[:] for row in mask]
    for _ in range(n):
        h, w = len(m), len(m[0])
        m2 = [[False] * w for _ in range(h)]
        for y in range(1, h - 1):
            for x in range(1, w - 1):
                m2[y][x] = m[y][x] and m[y - 1][x] and m[y + 1][x] and m[y][x - 1] and m[y][x + 1]
        m = m2
    return m


def rects(mask):
    """Decoupe gloutonne d'un masque en rectangles."""
    h, w = len(mask), len(mask[0])
    used = [[False] * w for _ in range(h)]
    out = []
    for y in range(h):
        for x in range(w):
            if mask[y][x] and not used[y][x]:
                x1 = x
                while x1 + 1 < w and mask[y][x1 + 1] and not used[y][x1 + 1]:
                    x1 += 1
                y1 = y
                while y1 + 1 < h and all(mask[y1 + 1][k] and not used[y1 + 1][k] for k in range(x, x1 + 1)):
                    y1 += 1
                for j in range(y, y1 + 1):
                    for i in range(x, x1 + 1):
                        used[j][i] = True
                out.append((x, y, x1 - x + 1, y1 - y + 1))
    return out


def dilate_colors(sp):
    """Copie du sprite dont le transparent est rempli par la couleur voisine : les faces de cote y lisent sans jamais tomber sur du vide."""
    h, w = len(sp), len(sp[0])
    out = [[c[:] for c in row] for row in sp]
    for _ in range(32):
        changed = False
        nxt = [[c[:] for c in row] for row in out]
        for y in range(h):
            for x in range(w):
                if out[y][x][3]:
                    continue
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    a, b = x + dx, y + dy
                    if 0 <= a < w and 0 <= b < h and out[b][a][3]:
                        nxt[y][x] = out[b][a][:3] + [255]
                        changed = True
                        break
        out = nxt
        if not changed:
            break
    return out


def face(u, v, mirror=False):
    return {"offset": {"x": u, "y": v}, "mirror": {"x": mirror, "y": False}, "angle": 0}


def model(sp):
    """Retourne (blockymodel, texture 64x32). Texture : sprite a gauche, sprite dilate a droite pour les cotes."""
    S = len(sp)
    solid = [[c[3] > 0 for c in row] for row in sp]
    ys = [y for y in range(S) if any(solid[y])]
    xs = [x for x in range(S) if any(solid[y][x] for y in range(S))]
    cx, cy = (min(xs) + max(xs) + 1) / 2, (min(ys) + max(ys) + 1) / 2
    kids = []
    nid = [2]
    for level, (ero, depth) in enumerate(((0, 3), (2, 6), (4, 9))):
        m = erode(solid, ero)
        if not any(any(r) for r in m):
            continue
        for x, y, w, h in rects(m):
            nid[0] += 1
            side = face(min(32 + x, 64 - depth), y)                 # cotes : depth x h, dans la moitie droite (sprite dilate)
            cap = face(32 + x, min(y, 32 - depth))                  # dessus et dessous : w x depth
            layout = {"front": face(x, y), "back": face(x, y), "left": side, "right": dict(side), "top": cap, "bottom": dict(cap)}
            shape = {"type": "box", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": 1, "y": 1, "z": 1},
                     "settings": {"size": {"x": w, "y": h, "z": depth}}, "textureLayout": layout, "unwrapMode": "custom",
                     "visible": True, "doubleSided": False, "shadingMode": "flat"}
            kids.append({"id": str(nid[0]), "name": "P%d" % nid[0],
                         "position": {"x": round(x + w / 2 - cx, 2), "y": round(cy - y - h / 2, 2), "z": 0},
                         "orientation": {"x": 0, "y": 0, "z": 0, "w": 1}, "shape": shape, "children": []})
    none = {"type": "none", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": 1, "y": 1, "z": 1}, "settings": {"isPiece": False},
            "textureLayout": {}, "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": "flat"}
    body = {"id": "2", "name": "Body", "position": {"x": 0, "y": 16, "z": 0}, "orientation": {"x": 0, "y": 0, "z": 0, "w": 1},
            "shape": none, "children": kids}
    root = {"id": "1", "name": "R-Attachment", "position": {"x": 0, "y": 0, "z": 0}, "orientation": {"x": 0, "y": 0, "z": 0, "w": 1},
            "shape": none, "children": [body]}
    dil = dilate_colors(sp)
    tex = [sp[y] + dil[y] for y in range(S)]
    return {"nodes": [root], "lod": "auto"}, tex


# ---------------- ecriture ----------------
QUALITY = {False: ["Common", "Uncommon", "Rare"], True: ["Uncommon", "Rare", "Epic", "Legendary"]}
QUALITY_HIGH = ["Uncommon", "Rare", "Epic"]
RANK_NAME = ["commun", "rare", "précieux", "trophée"]
RATES = {False: [30, 8, 1], True: [60, 25, 8, 2]}


def quality(it):
    f = it["fam"]
    if f["boss"]:
        return QUALITY[True][it["rank"]]
    return (QUALITY_HIGH if f["lvl"] >= 300 else QUALITY[False])[it["rank"]]


def item_json(it):
    f = it["fam"]
    d = "Butin %s de %s." % (RANK_NAME[it["rank"]], pretty(f["nom"])) if it["rank"] < 3 else "Trophée de %s." % pretty(f["nom"])
    path = "Items/Butin/%s" % it["id"]
    return {"TranslationProperties": {"Name": it["name"], "Description": d},
            "Icon": "Icons/ItemsGenerated/%s.png" % it["id"],
            "Quality": quality(it), "ItemLevel": f["lvl"], "MaxStack": 50,
            "Categories": ["Eldanior_System.Butin"],
            "Model": path + ".blockymodel", "Texture": path + ".png", "PlayerAnimationsId": "Item",
            "IconProperties": {"Scale": 0.9, "Rotation": [0, 0, 0], "Translation": [0, -13]},
            "Tags": {"Type": ["Ingredient"]}, "ItemEntity": {"ParticleSystemId": None}, "Scale": 1.0, "DropOnDeath": True}


def drop_json(f, mineur=False):
    rates = RATES[f["boss"] and not mineur]
    cont = []
    for rank, it in enumerate(f["items"][:len(rates)]):
        iid = it[0] if it[1] == "existing" else slug(it[0])
        cont.append({"Type": "Choice", "Weight": rates[rank], "Containers": [
            {"Type": "Single", "Item": {"ItemId": iid, "QuantityMin": 1, "QuantityMax": 1}}]})
    return {"Container": {"Type": "Multiple", "Containers": cont}}


def norm(n):
    n = re.sub(r"^(Dungeon_|Coffer_)", "", n)
    for _ in range(3):
        n = re.sub(r"_(Patrol\d*|Wander|Surge|Anomaly|Static|Turret|Enrage|Marker|HM)$", "", n)
    return n


def vanilla_roles():
    out = []
    for p in glob.glob(VANILLA + "/NPC/Roles/**/*.json", recursive=True):
        r = p.split("/Roles/")[1]
        n = os.path.basename(p)[:-5]
        if r.startswith("_Core") or "/Templates/" in r or n.startswith(("Template", "Component", "Temple_", "Edible_")) or "/_Deprecated/" in r:
            continue
        out.append(n)
    return sorted(out)


def role_table():
    roles = vanilla_roles()
    table = {}
    for f in FAMILIES:
        hit = [r for r in roles if re.fullmatch(f["regex"], norm(r))]
        assert hit, f["slug"]
        small = [r for r in roles if f["mineurs"] and re.fullmatch(f["mineurs"], norm(r))]
        for r in hit + small:
            assert r not in table, (r, table[r], f["slug"])
            table[r] = "Drop_Butin_" + f["slug"] + ("_Mineur" if r in small else "")
    return table


def write_java(table):
    rows = "\n".join('        LISTS.put("%s", "%s");' % kv for kv in sorted(table.items()))
    src = """package com.eldanior.system.monsterloot;

import java.util.HashMap;
import java.util.Map;

/** GENERE par scripts/butin/gen_butin.py, ne pas modifier a la main : role exact du monstre -> sa table de butin. */
final class MonsterLootTable {

    private static final Map<String, String> LISTS = new HashMap<>();

    private MonsterLootTable() {
    }

    static String listFor(String roleId) {
        return LISTS.get(roleId);
    }

    static int size() {
        return LISTS.size();
    }

    static {
%s
    }
}
""" % rows
    os.makedirs(os.path.dirname(JAVA), exist_ok=True)
    open(JAVA, "w", encoding="utf-8").write(src)


def write_all():
    its = items()
    table = role_table()
    gen = [i for i in its if i["kind"] != "existing"]
    art = all_art(gen)
    srv_items = os.path.join(RES, "Server", "Item", "Items", "Butin")
    srv_drops = os.path.join(RES, "Server", "Drops", "Butin")
    com_items = os.path.join(RES, "Common", "Items", "Butin")
    icons = os.path.join(RES, "Common", "Icons", "ItemsGenerated")
    for d in (srv_items, srv_drops, com_items, icons):
        os.makedirs(d, exist_ok=True)
    boxes = []
    for it in gen:
        ic, sp = art[it["id"]]
        png.write(os.path.join(icons, it["id"] + ".png"), 64, 64, ic)
        mdl, tex = model(sp)
        boxes.append(len(mdl["nodes"][0]["children"][0]["children"]))
        png.write(os.path.join(com_items, it["id"] + ".png"), 64, 32, tex)
        json.dump(mdl, open(os.path.join(com_items, it["id"] + ".blockymodel"), "w"), separators=(",", ":"))
        json.dump(item_json(it), open(os.path.join(srv_items, it["id"] + ".json"), "w", encoding="utf-8"), ensure_ascii=False, indent=2)
    for f in FAMILIES:
        json.dump(drop_json(f), open(os.path.join(srv_drops, "Drop_Butin_%s.json" % f["slug"]), "w"), indent=2)
        if f["mineurs"]:
            json.dump(drop_json(f, True), open(os.path.join(srv_drops, "Drop_Butin_%s_Mineur.json" % f["slug"]), "w"), indent=2)
    write_java(table)
    print("items", len(gen), "tables", len(FAMILIES), "roles", len(table), "boites max", max(boxes), "moyenne", sum(boxes) // len(boxes))


if __name__ == "__main__":
    if sys.argv[1] == "sheet":
        sheet(sys.argv[2], sys.argv[3] if len(sys.argv) > 3 else None)
    elif sys.argv[1] == "all":
        write_all()
