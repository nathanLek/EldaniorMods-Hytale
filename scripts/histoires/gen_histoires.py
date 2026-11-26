"""Histoires et indices du mod (2026-11-14, demande de l'utilisateur) : categorie creative Eldanior System > Histoires.

Trois sortes d'objets, pour raconter l'histoire du monde et semer des indices de quete a travers la carte :
  - HISTOIRE (Eldanior_Histoire_<Id>) : journal ouvert de gobelin (lueur), POSABLE et LISIBLE EN MAIN (2026-11-14).
  - TOME (Eldanior_Tome_01 a _10) : Tomes Imperiaux du Royaume d'Eldanior (textes dans tomes.py), livre ferme violet,
    posable et lisible en main comme les histoires.
  - INDICE (Eldanior_Indice_<Id>) : feuillets comme « Lore_Note_Enkindled_Journal_Vermin », NON posable : clic droit (ou F)
    en main ouvre la page du jeu (BlockLore, interaction Java Eldanior_LireIndice, histoires/LireIndiceInteraction).

Livres (histoires et tomes) : fenetre a pages histoires/LivrePage (Histoires/Livre.ui). En main : clic droit = lire,
accroupi + clic droit = poser (meme geste que l'arrosoir du jeu, Root_Eldanior_Livre). Pose : F ou clic droit dessus = lire
(BlockType.Interactions Use/Secondary = Simple, ouverture par histoires/LivreUseEvent).

Le texte est dans Server/Languages/en-US/server.lang (bloc entre les reperes « gen_histoires.py ») : items.<Id>.name,
.description (info-bulle courte), .surtitre (au-dessus du texte) et .page1, .page2... (une cle par page ; LivrePage compte les
pages en cherchant ces cles). Mise en forme du jeu : <i>italique</i>, <b>gras</b>, \\n pour un retour a la ligne.

Ajouter une histoire ou un indice : une entree dans HISTOIRES / INDICES (ou TOMES dans tomes.py), puis
  python3 scripts/histoires/gen_histoires.py
"""
import json, os, re, sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tomes import TOMES, ACROSTICHE

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "src", "main", "resources")
ITEMS = os.path.join(ROOT, "Server", "Item", "Items", "Histoires")
INTERS = os.path.join(ROOT, "Server", "Item", "Interactions")
ROOTS = os.path.join(ROOT, "Server", "Item", "RootInteractions")
LANG = os.path.join(ROOT, "Server", "Languages", "en-US", "server.lang")
CATEGORY = "Eldanior_System.Histoires"
BEGIN, END = "# --- Histoires et indices (gen_histoires.py) ---", "# --- fin des histoires et indices ---"

# (Id, titre, texte). Texte : paragraphes separes par une ligne vide.
LIRE = "<i>Clic droit : lire. Accroupi + clic droit : poser.</i>"

# (Id, titre, surtitre, info-bulle, [pages])
HISTOIRES = [
    ("Roi_Fou", "Chronique d'Eldanior : le Roi Fou", "Chronique populaire d'Eldanior",
     "Ce que l'on raconte du Roi Fou, à voix basse, au coin du feu.",
     ["<i>Il fut un temps où Eldanior avait un roi, et ce roi avait un nom que plus personne n'ose prononcer.</i>\n\n"
      "On raconte qu'il voulut la force des dragons et qu'il l'obtint. Le pouvoir le dévora lentement, comme une braise "
      "sous la cendre, jusqu'au soir où il ne reconnut plus ses propres chevaliers.\n\n"
      "Le trône est vide depuis. Les morts errent sur ses anciennes terres, et les vieux conseillers jurent qu'un jour, "
      "quelqu'un viendra le reprendre.\n\n"
      "<i>Mais un seul.</i>\n\n"
      "Ceux qui veulent la vérité la chercheront dans les Tomes Impériaux, si l'Empire Luo veut bien les ouvrir."]),
]

INDICES = [
    # Quete du Roi, etape 1 (QueteRoi) : les trois preuves des temoins
    ("Journal_Garde", "Journal d'un garde du palais",
     "<i>Troisième nuit de garde sur le rempart nord.</i>\n\n"
     "Le roi est rentré de la montagne à l'aube. Seul. Ses chevaliers ne sont jamais revenus, et lui ne s'en est pas soucié. "
     "Ses yeux brûlaient comme des braises sous la cendre.\n\n"
     "Il a ordonné de sceller les portes du palais et de ne laisser entrer personne, pas même la reine.\n\n"
     "Cette nuit, j'ai entendu quelque chose respirer derrière la porte de la salle du trône. Ce n'était pas un homme."),
    ("Lettre_Reine", "Lettre de la reine (révélée à l'encre d'azur)",
     "<i>À celle qui me reste fidèle,</i>\n\n"
     "Le roi n'est plus mon époux. Ce qui porte sa couronne de fer est revenu du Tombeau des Épreuves Divines avec la "
     "Fureur du Dragon dans le sang, et il la prend pour une bénédiction.\n\n"
     "J'ai emporté le Sceau d'Eldanior pendant l'une de ses crises. Je l'ai caché sous les ruines de l'aile de l'Est, là où "
     "le feu de la Nuit Écarlate ne meurt jamais. Sans le sceau, il ne pourra plus jamais retourner au Tombeau.\n\n"
     "Si quelqu'un lit ceci un jour, qu'il sache : le trône ne doit revenir qu'à celui que le Dragon aura reconnu.\n\n"
     "<i>Ta reine.</i>"),
    ("Page_Grimoire", "Page arrachée du grimoire royal",
     "<i>…de l'Épreuve Divine.</i>\n\n"
     "Que l'héritier monte au Tombeau des Épreuves Divines, au sommet du Mont de l'Aube, et qu'il porte les trois "
     "reliques : la Couronne d'Aube, le Sceau d'Eldanior et la Larme d'Or. Alors le Dragon viendra, le regardera, et dira "
     "s'il est digne. Le roi élu reçoit sa bénédiction, et le temps n'a plus de prise sur lui tant qu'il règne.\n\n"
     "Qui entre sans les trois reliques n'est pas un héritier, mais un voleur, et le Dragon traite les voleurs selon sa "
     "colère.\n\n"
     "<i>(Dans la marge, d'une écriture tremblante : « Il est entré quand même. Que le Dragon nous pardonne. »)</i>"),
    ("Sceau_Brise", "Feuillet déchiré : le sceau brisé",
     "<i>…le Sceau d'Eldanior n'a jamais été retrouvé. Le vieux gardien jurait l'avoir vu briller sous les ruines de l'aile "
     "de l'Est, là où le feu de la Nuit Écarlate n'a jamais cessé de couver…</i>\n\n"
     "La suite du feuillet est calcinée."),
]


def write(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")


def tome_id(i):
    return f"Eldanior_Tome_{i + 1:02d}"


def livre_interactions():
    """En main : clic droit = lire (ou utiliser le bloc vise s'il en a l'usage, ex. un livre pose) ; accroupi = poser."""
    return {"Primary": "Block_Primary", "Secondary": "Root_Eldanior_Livre"}


def livre_bloc():
    """Pose : F ou clic droit dessus emet UseBlockEvent (Simple, sinon pas d'evenement) -> LivreUseEvent ouvre la lecture."""
    return {"Use": {"Interactions": [{"Type": "Simple"}]}, "Secondary": {"Interactions": [{"Type": "Simple"}]}}


def histoire(iid):
    """Journal ouvert pose (parent du jeu : lueur, posable), lisible en main."""
    return {
        "Parent": "Template_Lore_Note_Goblin_Journal_Open",
        "Quality": "Uncommon",
        "Categories": [CATEGORY],
        "Interactions": livre_interactions(),
        "BlockType": {"Interactions": livre_bloc()},
        "TranslationProperties": {"Name": f"server.items.{iid}.name", "Description": f"server.items.{iid}.description"},
    }


def tome(iid):
    """Tome Imperial : livre ferme violet du jeu (Recipe_Book_Magic_Void), lueur des notes, posable et lisible en main."""
    return {
        "Parent": "Template_Lore_Note",
        "Quality": "Rare",
        "Categories": [CATEGORY],
        "Icon": "Icons/ItemsGenerated/Recipe_Book_Magic_Void.png",
        "IconProperties": {"Scale": 0.695, "Translation": [0.7, -4.1], "Rotation": [22.5, 45, 22.5]},
        "Interactions": livre_interactions(),
        "BlockType": {
            "CustomModel": "Blocks/Dungeons/Book.blockymodel",
            "CustomModelTexture": [{"Texture": "Blocks/Dungeons/Book_Purple.png", "Weight": 1}],
            "HitboxType": "Book",
            "ParticleColor": "#6b3f8f",
            "TextureComputedColor": "#5a3577",
            "Interactions": livre_bloc(),
        },
        "TranslationProperties": {"Name": f"server.items.{iid}.name", "Description": f"server.items.{iid}.description"},
    }


def indice(iid):
    """Feuillets non posables : le clic droit et F lisent au lieu de poser (le gauche reste celui des blocs)."""
    return {
        "Parent": "Template_Lore_Note_Pages_B",
        "Quality": "Common",
        "Categories": [CATEGORY],
        "BlockType": {},
        "Interactions": {"Primary": "Block_Primary", "Secondary": "Root_Eldanior_LireIndice", "Use": "Root_Eldanior_LireIndice"},
        "TranslationProperties": {"Name": f"server.items.{iid}.name", "Description": f"server.items.{iid}.description"},
    }


def esc(text):
    return text.replace("\n", "\\n")


def livres():
    """(Id, titre, surtitre, info-bulle, pages) des histoires et des tomes."""
    out = [(f"Eldanior_Histoire_{k}", t, sur, desc, pages) for k, t, sur, desc, pages in HISTOIRES]
    for i, (num, titre, pages) in enumerate(TOMES):
        out.append((tome_id(i), f"Tome {num} : {titre}", f"Tomes Impériaux du Royaume d'Eldanior · Livre {num}",
                    f"Tomes Impériaux du Royaume d'Eldanior, livre {num} sur {len(TOMES)}.\n{LIRE}", pages))
    return out


def lang_block():
    lines = [BEGIN]
    for iid, title, sur, desc, pages in livres():
        if "Histoire" in iid: desc = f"{desc}\n{LIRE}"
        lines.append(f"items.{iid}.name={title}")
        lines.append(f"items.{iid}.description={esc(desc)}")
        lines.append(f"items.{iid}.surtitre={esc(sur)}")
        for n, page in enumerate(pages, 1):
            lines.append(f"items.{iid}.page{n}={esc(page)}")
    for key, title, text in INDICES:
        iid = f"Eldanior_Indice_{key}"
        lines.append(f"items.{iid}.name={title}")
        lines.append(f"items.{iid}.description={esc(text)}")
    lines.append(END)
    return "\n".join(lines)


def check():
    first = "".join(re.sub(r"<[^>]+>", "", pages[0]).lstrip()[0] for _, _, pages in TOMES)
    plain = first.upper().replace("É", "E")
    if plain != ACROSTICHE: raise SystemExit(f"acrostiche casse : {plain} au lieu de {ACROSTICHE}")


def main():
    check()
    write(os.path.join(INTERS, "Eldanior_LireIndice.json"), {"Type": "Eldanior_LireIndice"})
    write(os.path.join(ROOTS, "Root_Eldanior_LireIndice.json"), {"Interactions": ["Eldanior_LireIndice"], "RequireNewClick": True})
    write(os.path.join(INTERS, "Eldanior_LireLivre.json"), {"Type": "Eldanior_LireLivre"})
    # Comme l'arrosoir du jeu (Watering_Can_Condition_Place) : accroupi -> Block_Secondary (pose) ; sinon on utilise le bloc
    # vise (UseBlock : un livre pose s'ouvre, un coffre aussi) et, s'il n'a pas d'usage ou dans le vide, on lit le livre tenu.
    write(os.path.join(INTERS, "Eldanior_Livre_Secondaire.json"),
          {"Type": "Condition", "Crouching": True, "Next": "Block_Secondary",
           "Failed": {"Type": "UseBlock", "Failed": "Eldanior_LireLivre"}})
    write(os.path.join(ROOTS, "Root_Eldanior_Livre.json"),
          {"Cooldown": {"Id": "BlockInteraction", "Cooldown": 0.278, "ClickBypass": True},
           "Interactions": ["Eldanior_Livre_Secondaire"], "RequireNewClick": True})
    for f in os.listdir(ITEMS) if os.path.isdir(ITEMS) else []:                  # objets retires de la liste
        if f.startswith(("Eldanior_Histoire_", "Eldanior_Indice_", "Eldanior_Tome_")): os.remove(os.path.join(ITEMS, f))
    for key, *_ in HISTOIRES:
        write(os.path.join(ITEMS, f"Eldanior_Histoire_{key}.json"), histoire(f"Eldanior_Histoire_{key}"))
    for i in range(len(TOMES)):
        write(os.path.join(ITEMS, f"{tome_id(i)}.json"), tome(tome_id(i)))
    for key, _, _ in INDICES:
        write(os.path.join(ITEMS, f"Eldanior_Indice_{key}.json"), indice(f"Eldanior_Indice_{key}"))
    with open(LANG, encoding="utf-8") as f:
        s = f.read()
    block = lang_block()
    if BEGIN in s:
        a, b = s.index(BEGIN), s.index(END) + len(END)
        s = s[:a] + block + s[b:]
    else:
        s = s.rstrip("\n") + "\n\n" + block + "\n"
    with open(LANG, "w", encoding="utf-8") as f:
        f.write(s)
    print(f"{len(HISTOIRES)} histoire(s), {len(TOMES)} tome(s), {len(INDICES)} indice(s) -> {ITEMS}")


if __name__ == "__main__":
    main()
