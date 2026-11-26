"""Borne de territoire (decision du 2026-11-10, docs/plan-bornes-territoire.md) : bloc Eldanior_Borne_Territoire et sa fenetre
Common/UI/Custom/Territoire/Borne.ui, pilotee par territory/borne/BornePage.

Bloc invisible et traversable comme le poste de PNJ (admins) : le poser ou F dessus ouvre la fenetre ; il marque le centre de la
zone ; le casser supprime la zone. Fenetre dans le style du poste de PNJ (scripts/npcs/gen_npc_post_ui.py, fonctions reprises) :
Zone (type, nom, famille), Dimensions (forme, demi-largeurs ou rayon, hauteurs), Regles (PvP, protection, regeneration).

Usage : python3 scripts/territoire/gen_borne.py [src/main/resources]
"""
import json, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "npcs"))
import gen_npc_post_ui as U  # noqa: E402

RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "..", "..", "src", "main", "resources")
ITEM = "Eldanior_Borne_Territoire"


def number(i, step=10):
    return f'''          $C.@NumberField #{i} {{
            Anchor: (Width: 360, Height: 26);
            Format: (MaxDecimalPlaces: 0, Step: {step});
          }}'''


def text(i, placeholder):
    return f'''          $C.@TextField #{i} {{
            Anchor: (Width: 360, Height: 28);
            PlaceholderText: "{placeholder}";
          }}'''


def block():
    path = os.path.join(RES, "Server", "Item", "Items", "Eldanior", "Territoire", ITEM + ".json")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    data = {
        "$Comment": "Borne de territoire (territory/borne) : bloc invisible et traversable ; le poser ou F dessus ouvre la fenetre "
                    "de la zone (admins). Genere par scripts/territoire/gen_borne.py.",
        "TranslationProperties": {
            "Name": "Borne de territoire",
            "Description": "Admins : posez cette borne invisible au centre d'un royaume, d'un grand territoire, d'un territoire ou "
                           "d'une ville, puis réglez la zone (type, nom, famille, dimensions rectangulaires ou cylindriques, PvP, "
                           "protection, régénération). Touche F sur la borne pour y revenir. La zone n'existe que tant que la "
                           "borne reste posée : la casser supprime la zone. Tenez une borne en main pour voir les bornes et les "
                           "bords des zones."},
        "Categories": ["Eldanior_System.Autres"], "Quality": "Technical", "PlayerAnimationsId": "Block",
        "Icon": "Icons/ItemsGenerated/Editor_Empty.png", "MaxStack": 64,
        "BlockType": {"Material": "Empty", "DrawType": "Empty", "Opacity": "Transparent",
                      "Support": {"Down": [{"Support": "Ignored"}]},
                      "InteractionHint": "server.interactionHints.eldanior_npcpost",
                      "Interactions": {"Use": {"Interactions": [{"Type": "Simple"}]}}},
    }
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")
    print("ecrit", path)


def ui():
    zone = (U.row("Type", U.dropdown("BorneType"), "Royaume, grand territoire, territoire ou ville (hiérarchie des zones).")
            + U.row("Nom", text("BorneName", "Marquisat du Nord"), "Nom affiché à l'entrée de la zone.")
            + U.row("Famille", U.dropdown("BorneFamily"), "Famille propriétaire (aucune : terres de la couronne)."))
    dims = (U.row("Forme", U.dropdown("BorneShape"), "Rectangle autour de la borne, ou cylindre centré sur elle.")
            + U.row("Est-ouest", number("BorneSizeX"), "Demi-largeur en blocs depuis la borne (rayon du cylindre).")
            + U.row("Nord-sud", number("BorneSizeZ"), "Demi-largeur en blocs depuis la borne (ignoré pour un cylindre).")
            + U.row("Hauteur min.", number("BorneYMin"), "Hauteur basse de la zone (0 : bas du monde).")
            + U.row("Hauteur max.", number("BorneYMax"), "Hauteur haute de la zone (319 : haut du monde)."))
    rules = (U.row("PvP", U.dropdown("BornePvp"), "Combat entre joueurs autorisé dans la zone.")
             + U.row("Protection", U.dropdown("BorneProtect"), "Blocs protégés par défaut (seuls les membres construisent).")
             + U.row("Régénération", number("BorneRegen", 30), "Secondes avant qu'un bloc cassé revienne ; 0 : pas de régénération.")
             + U.row("Annonce", U.dropdown("BorneAnnounce"), "Titre affiché aux joueurs qui entrent dans la zone."))
    G, GL, DARK, BODY, TITLE = U.GOLD, U.GOLD_LINE, U.DARK, U.BODY, U.TITLE
    out = f'''$C = "../Common.ui";

// Borne de territoire (bloc {ITEM}, admins), genere par scripts/territoire/gen_borne.py : voir ce script.
Group {{
  LayoutMode: Middle;
  Background: #05080d(0.55);

  Group {{
    Anchor: (Width: 760);
    LayoutMode: Top;
    Padding: (Full: 3);
    Background: {DARK};

    Group {{
      LayoutMode: Top;
      Padding: (Full: 3);
      Background: {G};

      Group {{
        LayoutMode: Top;
        Padding: (Full: 2);
        Background: {DARK};

        Group {{
          LayoutMode: Top;
          Background: {BODY};
          Padding: (Left: 26, Right: 26, Top: 16, Bottom: 14);

          Group {{
            Anchor: (Height: 56);
            LayoutMode: Top;
            Label {{
              Text: "Borne de territoire";
              Style: (FontSize: 22, RenderBold: true, RenderUppercase: true, TextColor: {TITLE});
            }}
            Label #BorneState {{
              Anchor: (Top: 4);
              Style: (FontSize: 13, RenderBold: true, TextColor: #878e9c, Wrap: true);
            }}
          }}

          Group {{ Anchor: (Top: 10, Height: 2); Background: {GL}; }}

{U.section("Zone")}{zone}
{U.section("Dimensions")}{dims}
{U.section("Règles")}{rules}
          Group {{ Anchor: (Top: 14, Height: 2); Background: {GL}; }}

          Group {{
            LayoutMode: Left;
            Anchor: (Top: 14, Height: 44);
            $C.@TextButton #BorneSave {{
              @Anchor = (Right: 4);
              Text: "Créer / mettre à jour";
              FlexWeight: 1;
            }}
            $C.@SecondaryTextButton #BorneLink {{
              @Anchor = (Left: 4, Right: 4);
              Text: "Lier la zone existante";
              FlexWeight: 1;
            }}
            $C.@SecondaryTextButton #BorneDelete {{
              @Anchor = (Left: 4);
              Text: "Supprimer la zone";
              FlexWeight: 1;
            }}
          }}

          Label {{
            Anchor: (Top: 10);
            Text: "Échap : fermer  ·  Tenez une borne en main pour voir les bornes et les bords des zones  ·  Casser la borne supprime la zone";
            Style: (FontSize: 11, TextColor: #5a6a7a, HorizontalAlignment: Center);
          }}
        }}
      }}
    }}
  }}
}}
'''
    path = os.path.join(RES, "Common", "UI", "Custom", "Territoire", "Borne.ui")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write(out)
    print("ecrit", path)


if __name__ == "__main__":
    block()
    ui()
