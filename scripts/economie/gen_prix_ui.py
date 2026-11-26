"""Editeur des prix en jeu (/es prix) : Common/UI/Custom/Economie/Prix.ui, pilote par economy/PriceEditorPage.

Meme presentation que la boutique (deux panneaux facon etabli / enclume, scripts/npcs/ui_kit.py) :
  - au centre : familles a gauche (Tout, Modifies en jeu, puis les familles de docs/economie.md), recherche, liste des objets
    (icone, nom, identifiant, prix en pieces, source : fixe / calcule / force / jeu), pages ;
  - a droite : l'objet choisi (prix actuel, recette avec le prix de chaque ingredient — cliquer un ingredient l'ouvre —,
    recettes qui l'utilisent), saisie du nouveau prix, raccourcis /2 -10 % +10 % x2, retour au prix de base.

Usage : python3 scripts/economie/gen_prix_ui.py [src/main/resources]
"""
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "npcs"))
from ui_kit import (container, title_label, caption, fancy_sep, coins, side_tab, indent, PANEL_BG,  # noqa: E402
                    SLOT, SLOT_HOVER, SLOT_PRESS, MUTED, LABEL, GOLD, TEXT)

RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "..", "..", "src", "main", "resources")
OUT = os.path.join(RES, "Common", "UI", "Custom", "Economie")
TABS, ROWS, ING = 21, 12, 5            # PriceEditorPage.TABS, ROWS, ING
MAIN_W, SIDE_W, GUTTER, HEIGHT, FAM_W = 870, 420, 14, 720, 200


def row(i):
    return f'''Button #PxRow{i} {{
  Anchor: (Height: 38, Top: 3);
  LayoutMode: Left;
  Visible: false;
  Style: (
    Default: (Background: {SLOT}),
    Hovered: (Background: {SLOT_HOVER}),
    Pressed: (Background: {SLOT_PRESS}),
    Sounds: $C.@ButtonSounds
  );
  Group #PxRowQ{i} {{ Anchor: (Width: 4); Background: {SLOT}; }}
  Group {{
    Anchor: (Width: 42);
    LayoutMode: Middle;
    ItemIcon #PxRowIcon{i} {{ Anchor: (Width: 32, Height: 32); }}
  }}
  Group {{
    FlexWeight: 1;
    LayoutMode: Top;
    Padding: (Top: 3);
    Label #PxRowName{i} {{
      Style: (FontSize: 13, RenderBold: true, TextColor: #ffffff);
    }}
    Label #PxRowId{i} {{
      Style: (FontSize: 10, TextColor: {MUTED});
    }}
  }}
  Group {{
    Anchor: (Width: 210);
    LayoutMode: Center;
{indent(coins(f"PxRow{i}C", 3, size=16, font=13), 4)}
    Label #PxRowText{i} {{
      Style: (FontSize: 12, RenderBold: true, TextColor: {MUTED}, VerticalAlignment: Center);
    }}
  }}
  Label #PxRowSrc{i} {{
    Anchor: (Width: 86);
    Style: (FontSize: 11, RenderBold: true, RenderUppercase: true, TextColor: {LABEL}, HorizontalAlignment: Center, VerticalAlignment: Center);
  }}
}}'''


def ingredient(i):
    return f'''Button #PxIng{i} {{
  Anchor: (Height: 32, Top: 3);
  LayoutMode: Left;
  Visible: false;
  Style: (
    Default: (Background: {SLOT}),
    Hovered: (Background: {SLOT_HOVER}),
    Pressed: (Background: {SLOT_PRESS}),
    Sounds: $C.@ButtonSounds
  );
  Group {{
    Anchor: (Width: 34);
    LayoutMode: Middle;
    ItemIcon #PxIngIcon{i} {{ Anchor: (Width: 26, Height: 26); }}
  }}
  Label #PxIngName{i} {{
    FlexWeight: 1;
    Style: (FontSize: 12, RenderBold: true, TextColor: #ffffff, VerticalAlignment: Center);
  }}
  Label #PxIngInfo{i} {{
    Anchor: (Width: 150, Right: 6);
    Style: (FontSize: 11, RenderBold: true, TextColor: {GOLD}, HorizontalAlignment: End, VerticalAlignment: Center);
  }}
}}'''


def main_panel():
    title = f'''Group {{
  LayoutMode: Left;
  Padding: (Left: 16, Right: 16);
  Label {{
    Text: "Économie";
    Style: (...$C.@TitleStyle);
  }}
  Group {{
    Anchor: (Width: 6, Height: 10, Left: 10, Right: 8);
    Background: "../Common/ContainerTitleArrow.png";
  }}
  Label #PxSub {{
    Style: (...$C.@TitleStyle, RenderBold: true, TextColor: #ffffff);
  }}
  Group {{ FlexWeight: 1; }}
  Label #PxCount {{
    Style: (FontSize: 13, RenderBold: true, TextColor: {LABEL}, VerticalAlignment: Center);
  }}
}}'''
    body = f'''Group {{
  FlexWeight: 1;
  LayoutMode: Left;
  Group {{
    Anchor: (Width: {FAM_W});
    LayoutMode: Top;
    Padding: (Full: 6);
    Background: {PANEL_BG};
{indent(caption("Familles", align="Center"), 4)}
{indent(chr(10).join(side_tab("PxFam", i, height=24, top=2, font=11) for i in range(TABS)), 4)}
  }}
  Group {{ Anchor: (Width: 12); }}
  Group {{
    FlexWeight: 1;
    LayoutMode: Top;
    Group {{
      Anchor: (Height: 32);
      LayoutMode: Left;
      $C.@TextField #PxSearch {{
        FlexWeight: 1;
        Anchor: (Height: 30);
        PlaceholderText: "Nom ou identifiant (ex. fer, Ingredient_Bar)";
      }}
      $C.@SmallSecondaryTextButton #PxSearchBtn {{ @Anchor = (Width: 100, Left: 6); Text: "Chercher"; }}
      $C.@SmallSecondaryTextButton #PxClear {{ @Anchor = (Width: 90, Left: 4); Text: "Effacer"; }}
    }}
    Group {{
      Anchor: (Height: 20, Top: 8);
      LayoutMode: Left;
      Label {{
        FlexWeight: 1;
        Anchor: (Left: 46);
        Text: "Objet";
        Style: (FontSize: 10, RenderBold: true, RenderUppercase: true, TextColor: {MUTED}, VerticalAlignment: Center);
      }}
      Label {{
        Anchor: (Width: 210);
        Text: "Prix";
        Style: (FontSize: 10, RenderBold: true, RenderUppercase: true, TextColor: {MUTED}, HorizontalAlignment: Center, VerticalAlignment: Center);
      }}
      Label {{
        Anchor: (Width: 86);
        Text: "Source";
        Style: (FontSize: 10, RenderBold: true, RenderUppercase: true, TextColor: {MUTED}, HorizontalAlignment: Center, VerticalAlignment: Center);
      }}
    }}
{indent(chr(10).join(row(i) for i in range(ROWS)), 4)}
    Group {{ FlexWeight: 1; }}
    Group {{
      LayoutMode: Left;
      Anchor: (Height: 32, Top: 6);
      $C.@SmallSecondaryTextButton #PxPrev {{ @Anchor = (Width: 130); Text: "Précédent"; }}
      Label #PxPage {{
        FlexWeight: 1;
        Style: (FontSize: 12, RenderBold: true, TextColor: {MUTED}, HorizontalAlignment: Center, VerticalAlignment: Center);
      }}
      $C.@SmallSecondaryTextButton #PxNext {{ @Anchor = (Width: 130); Text: "Suivant"; }}
    }}
  }}
}}
{fancy_sep(8)}
Label #PxStats {{
  Anchor: (Top: 6, Height: 20);
  Style: (FontSize: 11, TextColor: {MUTED}, HorizontalAlignment: Center, VerticalAlignment: Center);
}}'''
    return container("PxMain", MAIN_W, HEIGHT, title, body)


def side_panel():
    title = title_label("PxSideTitle", "Prix de l'objet")
    body = f'''Group {{
  Anchor: (Height: 96);
  LayoutMode: Left;
  Group {{
    Anchor: (Width: 96, Height: 96);
    LayoutMode: Middle;
    Padding: (Full: 2);
    Background: {GOLD};
    Group {{
      Anchor: (Width: 92, Height: 92);
      LayoutMode: Middle;
      Background: {SLOT};
      ItemIcon #PxBig {{ Anchor: (Width: 78, Height: 78); }}
    }}
  }}
  Group {{
    FlexWeight: 1;
    LayoutMode: Top;
    Padding: (Left: 12, Top: 2);
    Label #PxName {{
      Style: (FontSize: 17, RenderBold: true, TextColor: #ffffff, Wrap: true);
    }}
    Label #PxId {{
      Anchor: (Top: 2);
      Style: (FontSize: 11, TextColor: {MUTED});
    }}
    Label #PxKind {{
      Anchor: (Top: 4);
      Style: (FontSize: 12, RenderBold: true, TextColor: {LABEL}, Wrap: true);
    }}
  }}
}}
{fancy_sep(8)}
Group {{
  Anchor: (Top: 8, Height: 56);
  LayoutMode: Top;
  Background: {PANEL_BG};
  Padding: (Top: 6);
  Group {{
    Anchor: (Height: 26);
    LayoutMode: Center;
{indent(coins("PxPriceC", 6, size=22, font=17), 4)}
    Label #PxPriceText {{
      Style: (FontSize: 15, RenderBold: true, TextColor: {MUTED}, VerticalAlignment: Center);
    }}
  }}
  Label #PxPriceCap {{
    Style: (FontSize: 10, RenderBold: true, RenderUppercase: true, TextColor: {MUTED}, HorizontalAlignment: Center);
  }}
}}
Group #PxRecipe {{
  Anchor: (Top: 8);
  LayoutMode: Top;
  Visible: false;
{indent(caption("Recette — cliquer un ingrédient pour l'ouvrir", color=GOLD, size=10, lid="PxRecipeCap"), 2)}
{indent(chr(10).join(ingredient(i) for i in range(ING)), 2)}
  Label #PxRecipeNote {{
    Anchor: (Top: 4);
    Style: (FontSize: 11, TextColor: {LABEL}, Wrap: true);
  }}
}}
Label #PxUsed {{
  Anchor: (Top: 6);
  Style: (FontSize: 11, TextColor: {MUTED}, Wrap: true);
}}
Group {{ FlexWeight: 1; }}
{caption("Nouveau prix, en cuivres (1 or = 100)", color=LABEL, size=10)}
Group {{
  Anchor: (Top: 4, Height: 32);
  LayoutMode: Left;
  $C.@NumberField #PxValue {{
    Anchor: (Width: 170, Height: 30);
    Format: (MaxDecimalPlaces: 0, Step: 10);
  }}
  $C.@SmallSecondaryTextButton #PxSave {{ @Anchor = (Left: 6); Text: "Enregistrer"; FlexWeight: 1; }}
}}
Group {{
  Anchor: (Top: 6, Height: 32);
  LayoutMode: Left;
  $C.@SmallSecondaryTextButton #PxDiv2 {{ @Anchor = (Right: 3); Text: "/2"; FlexWeight: 1; }}
  $C.@SmallSecondaryTextButton #PxM10 {{ @Anchor = (Right: 3); Text: "-10 %"; FlexWeight: 1; }}
  $C.@SmallSecondaryTextButton #PxP10 {{ @Anchor = (Right: 3); Text: "+10 %"; FlexWeight: 1; }}
  $C.@SmallSecondaryTextButton #PxMul2 {{ Text: "x2"; FlexWeight: 1; }}
}}
Group {{
  Anchor: (Top: 6, Height: 32);
  LayoutMode: Left;
  $C.@SmallSecondaryTextButton #PxReset {{ Text: "Revenir au prix de base"; FlexWeight: 1; }}
}}
Label #PxMsg {{
  Anchor: (Top: 4, Height: 34);
  Style: (FontSize: 12, RenderBold: true, TextColor: #00e7b6, HorizontalAlignment: Center, VerticalAlignment: Center, Wrap: true);
}}'''
    return container("PxSide", SIDE_W, HEIGHT, title, body, decorated=True)


def main():
    os.makedirs(OUT, exist_ok=True)
    ui = f'''$C = "../Common.ui";

// Editeur des prix (/es prix, economy/PriceEditorPage), genere par scripts/economie/gen_prix_ui.py : voir ce script.
$C.@PageOverlay {{
  LayoutMode: Middle;

  Group {{
    Anchor: (Width: {MAIN_W + GUTTER + SIDE_W}, Height: {HEIGHT});
    LayoutMode: Left;
{indent(main_panel(), 4)}
    Group {{ Anchor: (Width: {GUTTER}); }}
{indent(side_panel(), 4)}
  }}
}}
'''
    with open(os.path.join(OUT, "Prix.ui"), "w", encoding="utf-8") as f:
        f.write(ui)
    print("ecrit Economie/Prix.ui")


if __name__ == "__main__":
    main()
