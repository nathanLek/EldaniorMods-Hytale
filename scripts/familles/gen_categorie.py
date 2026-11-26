"""Categorie creative "Famille" : une sous-categorie par famille noble, icone = blason de la famille.

Tout objet lie a une famille (cle et porte du tresor, presentoir, tenues, arme de famille...) va dans
Eldanior_Familles.<Id> (familles.category) au lieu des sous-categories d'Eldanior System. Les generateurs le font
deja : gen_portes.py, gen_presentoir.py, gen_ryukiri.py, et armor_kit.item_json par prefixe d'ID (familles.ARMOR_PREFIXES).

Categorie creative "Empire Luo" (2026-11-11) : les cinq clans de l'Empire (Luo, Kage, Setsuka, Sama, Hanazakura, sortis de
"Famille") et quatre pages communes, Civils, Comestibles, Arts Martiaux et Runes (familles.EMPIRE_GROUPS). familles.category
envoie chaque Id dans la bonne categorie. Icones des pages communes = icone d'un objet typique, reduite a 48 px.

Ecrit :
  Server/Item/Category/CreativeLibrary/Eldanior_Familles.json et Eldanior_Empire_Luo.json
  Common/Icons/ItemCategories/Familles/<Id>.png (48x48) et Familles.png (icone de l'onglet)
  Common/Icons/ItemCategories/EmpireLuo/<Id>.png (48x48) et EmpireLuo.png

Usage : python3 scripts/familles/gen_categorie.py <src/main/resources>
"""
import json, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
for folder in ("armures", "rune_aura_icons"):
    sys.path.insert(0, os.path.join(HERE, "..", folder))
import png  # noqa: E402
from familles import CATEGORY, EMPIRE, EMPIRE_CLANS, EMPIRE_GROUPS, FAMILIES, arms  # noqa: E402

RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "..", "..", "src", "main", "resources")
SIZE = 48
ICON_DIR = "Icons/ItemCategories/Familles"
EMPIRE_ICON_DIR = "Icons/ItemCategories/EmpireLuo"
# icone de chaque page commune : un objet typique (Common/Icons/ItemsGenerated/<Id>.png, 64 px)
GROUP_ICONS = {"Civils": "Armor_Kimono_Rizieres_Head", "Comestibles": "Luo_The_Jade",
               "ArtsMartiaux": "Eldanior_Manuel_Luo_KiEpee", "Runes": "Rune_KiEpee"}


def blank():
    return [[[0, 0, 0, 0] for _ in range(SIZE)] for _ in range(SIZE)]


def paste(dst, img, x0, y0):
    """Colle img sur dst (alpha 'over'), en ignorant ce qui deborde."""
    for y, row in enumerate(img):
        for x, p in enumerate(row):
            X, Y = x0 + x, y0 + y
            if not (0 <= X < SIZE and 0 <= Y < SIZE) or p[3] <= 0:
                continue
            a, b = p[3] / 255.0, dst[Y][X]
            ba = b[3] / 255.0
            out = a + ba * (1 - a)
            dst[Y][X] = [(p[i] * a + b[i] * ba * (1 - a)) / out for i in range(3)] + [out * 255]


def shield(color, kind, height):
    """Ecu a la hauteur voulue (la largeur suit les proportions du blason)."""
    probe = arms(color, kind, 20)
    width = max(1, int(round(height * len(probe[0]) / float(len(probe)))))
    img = arms(color, kind, width, px=96)
    return img


def centered(img):
    out = blank()
    paste(out, img, (SIZE - len(img[0])) // 2, (SIZE - len(img)) // 2)
    return out


def shrink(img):
    """Reduit une image carree a SIZE px (moyenne par surface, alpha premultiplie)."""
    n = len(img)
    k = n / float(SIZE)
    out = blank()
    for Y in range(SIZE):
        for X in range(SIZE):
            acc, wsum = [0.0, 0.0, 0.0, 0.0], 0.0
            y0, y1, x0, x1 = Y * k, (Y + 1) * k, X * k, (X + 1) * k
            for y in range(int(y0), min(n, int(-(-y1 // 1)))):
                wy = min(y + 1, y1) - max(y, y0)
                for x in range(int(x0), min(n, int(-(-x1 // 1)))):
                    w = wy * (min(x + 1, x1) - max(x, x0))
                    p = img[y][x]
                    a = p[3] / 255.0 * w
                    acc[0] += p[0] * a; acc[1] += p[1] * a; acc[2] += p[2] * a; acc[3] += a
                    wsum += w
            if acc[3] > 0:
                out[Y][X] = [acc[0] / acc[3], acc[1] / acc[3], acc[2] / acc[3], 255.0 * acc[3] / wsum]
    return out


def empire_tab_icon():
    """Onglet Empire Luo : les quatre clans en eventail derriere l'ecu du Clan Luo."""
    by_id = {f[0]: f for f in FAMILIES}
    out = blank()
    for fid, dx, dy, h in (("Sama", -16, 5, 24), ("Kage", 16, 5, 24), ("Hanazakura", -10, 2, 28), ("Setsuka", 10, 2, 28),
                           ("Luo", 0, -1, 36)):
        _, _, color, kind = by_id[fid]
        img = shield(color, kind, h)
        paste(out, img, (SIZE - len(img[0])) // 2 + dx, (SIZE - len(img)) // 2 + dy)
    return out


def write_category(path, data):
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")
    print("%s : %d pages" % (path, len(data["Children"])))


def tab_icon():
    """Onglet : trois ecus en eventail (Runkandel, Luo, Eldanior devant)."""
    by_id = {f[0]: f for f in FAMILIES}
    out = blank()
    for fid, dx, dy, h in (("Runkandel", -11, 2, 34), ("Luo", 11, 2, 34), ("Eldanior", 0, -1, 42)):
        _, _, color, kind = by_id[fid]
        img = shield(color, kind, h)
        paste(out, img, (SIZE - len(img[0])) // 2 + dx, (SIZE - len(img)) // 2 + dy)
    return out


def main():
    icon_dir = os.path.join(RES, "Common", *ICON_DIR.split("/"))
    empire_dir = os.path.join(RES, "Common", *EMPIRE_ICON_DIR.split("/"))
    os.makedirs(icon_dir, exist_ok=True)
    os.makedirs(empire_dir, exist_ok=True)
    by_id = {f[0]: f for f in FAMILIES}
    children = []
    for fid, name, color, kind in FAMILIES:
        if fid in EMPIRE_CLANS:
            old = os.path.join(icon_dir, fid + ".png")                     # icone d'avant le deplacement
            if os.path.exists(old):
                os.remove(old)
            continue
        png.write(os.path.join(icon_dir, fid + ".png"), SIZE, SIZE, centered(shield(color, kind, 44)))
        children.append({"Id": fid, "Name": name, "Icon": "%s/%s.png" % (ICON_DIR, fid)})
    png.write(os.path.join(icon_dir, "Familles.png"), SIZE, SIZE, tab_icon())
    category_dir = os.path.join(RES, "Server", "Item", "Category", "CreativeLibrary")
    write_category(os.path.join(category_dir, CATEGORY + ".json"),
                   {"Icon": ICON_DIR + "/Familles.png", "Order": 6, "Children": children, "Name": "Famille", "Id": "ES_Familles"})

    # ----- Empire Luo : les cinq clans, puis les pages communes -----
    children = []
    for fid in EMPIRE_CLANS:
        _, name, color, kind = by_id[fid]
        png.write(os.path.join(empire_dir, fid + ".png"), SIZE, SIZE, centered(shield(color, kind, 44)))
        children.append({"Id": fid, "Name": name, "Icon": "%s/%s.png" % (EMPIRE_ICON_DIR, fid)})
    for gid, name in EMPIRE_GROUPS:
        _, _, src = png.read(os.path.join(RES, "Common", "Icons", "ItemsGenerated", GROUP_ICONS[gid] + ".png"))
        png.write(os.path.join(empire_dir, gid + ".png"), SIZE, SIZE, shrink(src))
        children.append({"Id": gid, "Name": name, "Icon": "%s/%s.png" % (EMPIRE_ICON_DIR, gid)})
    png.write(os.path.join(empire_dir, "EmpireLuo.png"), SIZE, SIZE, empire_tab_icon())
    write_category(os.path.join(category_dir, EMPIRE + ".json"),
                   {"Icon": EMPIRE_ICON_DIR + "/EmpireLuo.png", "Order": 7, "Children": children, "Name": "Empire Luo",
                    "Id": "ES_Empire_Luo"})


if __name__ == "__main__":
    main()
