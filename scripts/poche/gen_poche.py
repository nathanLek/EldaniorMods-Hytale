"""Poche dimensionnelle (Eldanior_Poche_Dimensionnelle, 2026-11-16) : objet, texture et icone.

Meme sac que le grand sac a dos du jeu (Utility_Leather_Medium_Backpack : modele Items/Back/BackpackBig.blockymodel, porte
recolore : cuir violet du vide, coutures cyan lumineuses, boucles d'or, quelques etoiles dans les ombres. Clic : l'equiper dans
sa case du menu Systeme (interaction Eldanior_OuvrirPoche) ; elle ne va pas dans l'emplacement d'utilitaire du jeu.

La poche elle-meme (4 rangees de 9, gardee a la mort, rangee hors de l'inventaire du joueur), la remise a l'arrivee et le
bouton du menu Systeme sont dans com.eldanior.system.poche.PocheDimensionnelle.

Usage : python3 scripts/poche/gen_poche.py <src/main/resources>
"""
import colorsys, json, os, random, sys, zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "rune_aura_icons"))
import png  # noqa: E402

RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "..", "..", "src", "main", "resources")
ASSETS = os.path.expanduser("~/Library/Application Support/Hytale/install/release/package/game/latest/Assets.zip")
ITEM_ID = "Eldanior_Poche_Dimensionnelle"
TEX_REL = "Items/Eldanior/Poche/Poche_Dimensionnelle_Texture.png"
ICON_REL = "Icons/ItemsGenerated/%s.png" % ITEM_ID

VIOLET, CYAN, GOLD = 0.74, 0.52, 0.12
STAR = (226, 214, 255)


def read_png(zf, name, tmp):
    with open(tmp, "wb") as f:
        f.write(zf.read(name))
    return png.read(tmp)


def recolor(px, seed, stars):
    rng = random.Random(seed)
    for row in px:
        for p in row:
            if p[3] == 0:
                continue
            h, l, s = colorsys.rgb_to_hls(p[0] / 255, p[1] / 255, p[2] / 255)
            if s < 0.14:                                              # metal (boucles, rivets) : or
                h2, l2, s2 = GOLD, min(1.0, l * 1.1 + 0.08), 0.62
            elif l > 0.3:                                             # coutures claires : fil cyan lumineux
                h2, l2, s2 = CYAN, min(0.85, l * 1.35 + 0.08), 0.8
            else:                                                     # cuir : violet du vide (eclairci, le cuir du jeu est tres sombre)
                h2, l2, s2 = VIOLET, min(0.5, l * 1.55 + 0.05), min(1.0, s * 0.9 + 0.1)
            r, g, b = colorsys.hls_to_rgb(h2, l2, s2)
            p[0], p[1], p[2] = round(r * 255), round(g * 255), round(b * 255)
            if l < 0.16 and s >= 0.14 and rng.random() < stars:       # etoiles dans les ombres du cuir
                p[0], p[1], p[2] = STAR
    return px


def main():
    zf = zipfile.ZipFile(ASSETS)
    tmp = os.path.join(RES, "..", "..", "..", "build", "poche_tmp.png")
    os.makedirs(os.path.dirname(tmp), exist_ok=True)
    w, h, tex = read_png(zf, "Common/Items/Back/BackpackBig_Texture.png", tmp)
    os.makedirs(os.path.dirname(os.path.join(RES, "Common", TEX_REL)), exist_ok=True)
    png.write(os.path.join(RES, "Common", TEX_REL), w, h, recolor(tex, 7, 0.012))
    w, h, icon = read_png(zf, "Common/Icons/ItemsGenerated/Utility_Leather_Medium_Backpack.png", tmp)
    png.write(os.path.join(RES, "Common", ICON_REL), w, h, recolor(icon, 11, 0.03))
    os.remove(tmp)

    item = json.loads(zf.read("Server/Item/Items/Utility/Utility_Leather_Medium_Backpack.json"))
    item.pop("Recipe", None)                                          # s'achete chez le Maroquinier (10 imperiales)
    item["$Comment"] = "Poche dimensionnelle, generee par scripts/poche/gen_poche.py (poche : poche.PocheDimensionnelle)."
    item["TranslationProperties"] = {
        "Name": "Poche dimensionnelle",
        "Description": "Un sac cousu de fil d'étoile qui ouvre sur une poche hors du monde : 4 rangées de rangement. "
                       "Ce que vous y rangez n'est jamais perdu, même à la mort. Clic droit : l'équiper dans sa case du menu "
                       "Système (en bas à droite de votre personnage), d'où elle s'ouvre.",
    }
    item["Categories"] = ["Eldanior_System.Autres", "Items.Armors"]
    item["Icon"] = ICON_REL
    item["Texture"] = TEX_REL
    item["Quality"] = "Epic"
    item["ItemLevel"] = 1
    item["DropOnDeath"] = False
    item["Interactions"] = {
        "Primary": "Root_Eldanior_OuvrirPoche",                    # plus d'EquipItem : sa case est dans le menu Systeme
        "Secondary": "Root_Eldanior_OuvrirPoche",
        "Use": "Root_Eldanior_OuvrirPoche",
    }
    out = os.path.join(RES, "Server", "Item", "Items", "Eldanior", "Poche", ITEM_ID + ".json")
    os.makedirs(os.path.dirname(out), exist_ok=True)
    with open(out, "w", encoding="utf-8") as f:
        json.dump(item, f, indent=2, ensure_ascii=False)
        f.write("\n")
    for rel, data in (("Server/Item/Interactions/Eldanior_OuvrirPoche.json", {"Type": "Eldanior_OuvrirPoche"}),
                      ("Server/Item/RootInteractions/Root_Eldanior_OuvrirPoche.json",
                       {"Interactions": ["Eldanior_OuvrirPoche"], "RequireNewClick": True})):
        with open(os.path.join(RES, rel), "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
            f.write("\n")
    print("Poche dimensionnelle : objet, texture, icone et interactions ecrits.")


if __name__ == "__main__":
    main()
