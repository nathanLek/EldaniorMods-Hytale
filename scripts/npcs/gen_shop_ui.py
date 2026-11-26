"""Fenetre de boutique des PNJ (Common/UI/Custom/Npc/Shop.ui + ShopEmblem@2x.png), pilotee par guards/ShopPage.

Refaite le 2026-11-18 comme la fenetre de l'intendant (Intendant/Intendant.ui, scripts/intendant/gen_intendant_ui.py), elle-meme
habillee comme la fenetre Chapitres des quetes : memes images, reprises par « ../Quest/... ».
  - a gauche, la fiche de l'article : nom du marchand et son metier, embleme (grande icone), categorie / qualite / niveau, nom
    (couleur de qualite), ligne d'aide (vous pouvez en acheter N / il vous manque ...), description, prix et stock, ingredients
    requis (troc), barre de quantite (- / + / +10 / Max), total, bouton Acheter N (Vendre N dans Rachat), message ;
  - a droite : barre de categories (icone + nom, trait a la couleur du marchand), replique du marchand, 4 tuiles de chiffres,
    titre de section, grille de 20 cartes d'articles (icone, nom, prix en pieces, etiquette, liseret de qualite), pages, bourse
    en pieces et Fermer.

Doit correspondre a guards/ShopPage.java (CELLS, TABS, WALLET, PRICE, CELL_COINS, ING).
Usage : python3 scripts/npcs/gen_shop_ui.py [src/main/resources]
"""
import math, os, re, sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "rune_aura_icons"))
sys.path.insert(0, HERE)
import png  # noqa: E402
from ui_kit import coins, indent  # noqa: E402

RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "..", "..", "src", "main", "resources")
OUT = os.path.join(RES, "Common", "UI", "Custom", "Npc")
Q = "../Quest/"

TABS, CELLS, WALLET, PRICE, CELL_COINS, ING, FACTS = 11, 20, 7, 6, 3, 4, 6

# fiche (memes reperes que l'intendant et Chapitres)
PAGE_L, PAGE_T = 94, 114
TEXT_L, TEXT_W = PAGE_L + 2, 415
GOLD, TAGLINE, TEXT2, HEAD, BODY = "#f2c45e", "#C58643", "#77879D", "#C4CEDC", "#b8c2d0"
COIN = "#e8a93b"
OK = "#3fbf6a"

# partie droite
RIGHT_L = 611
CAT_T, CAT_W, CAT_H, CAT_PITCH = 34, 114, 78, 118
QUOTE_T = 126
STAT_T, STAT_W, STAT_H, STAT_PITCH = 182, 314, 82, 324
SECTION_T = 278
GRID_T, CARD_W, CARD_H, CARD_PX, CARD_PY, COLS = 328, 314, 102, 324, 112, 4
GRID_W = COLS * CARD_PX - (CARD_PX - CARD_W)
PAGER_T = 892
CX = TEXT_L + TEXT_W // 2          # axe de la fiche
BUY_T = 776                        # bloc Achat : cases du total, puis quantite, bouton, message
SLOT_PX, SLOT_PITCH = 64, 70       # cases du total (une piece par case, comme les recompenses des quetes)


def emblem():
    """Losange du portail (comme RecruitEmblem) et piece trouee doree (aussi utilise par Bank.ui)."""
    S = 192
    BORDER, FILL = (214, 160, 62), (26, 34, 49)
    G, GL, GD = (232, 169, 59), (255, 222, 140), (150, 98, 30)
    px = []
    for y in range(S):
        row = []
        for x in range(S):
            u, v = (x + 0.5) / S * 2 - 1, (y + 0.5) / S * 2 - 1
            d = abs(u) + abs(v)
            col, a = [0, 0, 0], 0.0
            if d <= 0.97:
                a = 1.0
                col = list(BORDER) if d >= 0.88 else list(FILL)
                if d < 0.88:
                    r = math.hypot(u, v)
                    glow = max(0.0, 1 - r / 0.75)
                    col = [c + (g - c) * 0.25 * glow for c, g in zip(col, G)]
                    hole = max(abs(u), abs(v)) < 0.11
                    if r < 0.46 and not hole:
                        shade = 0.8 + 0.35 * max(0.0, -u - v) / 0.9
                        col = [min(255, c * shade) for c in G]
                        if abs(r - 0.4) < 0.025 or (max(abs(u), abs(v)) < 0.15):
                            col = list(GD)
                        if r < 0.36 and (u - 0.1) ** 2 + (v + 0.2) ** 2 < 0.004:
                            col = list(GL)
                if d > 0.95:
                    a *= max(0.0, (0.97 - d) / 0.02)
            row.append([col[0], col[1], col[2], a * 255])
        px.append(row)
    png.write(os.path.join(OUT, "ShopEmblem@2x.png"), S, S, px)


def tab(i):
    x = RIGHT_L + i * CAT_PITCH
    return f'''    Button #ShopTab{i} {{
      Anchor: (Left: {x}, Top: {CAT_T}, Width: {CAT_W}, Height: {CAT_H});
      Visible: false;
      Style: (Default: (Background: #000000(0)), Hovered: (Background: (TexturePath: "{Q}QHudPatch.png", Border: 16)), Pressed: (Background: (TexturePath: "{Q}ChapSlot.png", Border: 23)), Sounds: $C.@ButtonSounds);
      Group #ShopTabGlow{i} {{ Anchor: (Left: 0, Top: 0, Width: {CAT_W}, Height: {CAT_H}); Background: (TexturePath: "{Q}ChapSlot.png", Border: 23); Visible: false; }}
      ItemIcon #ShopTabIcon{i} {{ Anchor: (Left: {CAT_W // 2 - 22}, Top: 6, Width: 44, Height: 44); }}
      Label #ShopTabText{i} {{
        Anchor: (Left: 4, Top: 50, Width: {CAT_W - 8}, Height: 20);
        Style: (FontSize: 12, RenderBold: true, RenderUppercase: true, TextColor: #96a9be, HorizontalAlignment: Center, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 9);
      }}
      Group #ShopTabLine{i} {{ Anchor: (Left: 16, Top: {CAT_H - 5}, Width: {CAT_W - 32}, Height: 3); Background: {GOLD}; Visible: false; }}
    }}'''


def stat(i):
    x = RIGHT_L + i * STAT_PITCH
    return f'''    Group #ShopStat{i} {{
      Anchor: (Left: {x}, Top: {STAT_T}, Width: {STAT_W}, Height: {STAT_H});
      Background: (TexturePath: "{Q}QHudPatch.png", Border: 16);
      Label #ShopStatV{i} {{
        Anchor: (Left: 12, Top: 10, Width: {STAT_W - 24}, Height: 38);
        Style: (FontName: "Secondary", FontSize: 24, RenderBold: true, TextColor: {GOLD}, HorizontalAlignment: Center, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 12);
      }}
      Group {{ Anchor: (Left: {STAT_W // 2 - 40}, Top: 50, Width: 80, Height: 1); Background: #c58643(0.5); }}
      Label #ShopStatL{i} {{
        Anchor: (Left: 12, Top: 54, Width: {STAT_W - 24}, Height: 20);
        Style: (FontSize: 12, RenderBold: true, RenderUppercase: true, TextColor: {TEXT2}, HorizontalAlignment: Center, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 9);
      }}
    }}'''


def card(i):
    """Carte d'article : liseret de qualite, cadre de choix (couleur du marchand), icone, nom, prix en pieces, etiquette."""
    x = RIGHT_L + (i % COLS) * CARD_PX
    y = GRID_T + (i // COLS) * CARD_PY
    w, h = CARD_W, CARD_H
    sel = "\n".join(f'        Group #ShopCellSel{s}{i} {{ Anchor: ({a}); Background: {GOLD}; }}' for s, a in (
        ("T", f"Left: 0, Top: 0, Width: {w}, Height: 2"), ("B", f"Left: 0, Top: {h - 2}, Width: {w}, Height: 2"),
        ("L", f"Left: 0, Top: 0, Width: 2, Height: {h}"), ("R", f"Left: {w - 2}, Top: 0, Width: 2, Height: {h}")))
    return f'''    Button #ShopCell{i} {{
      Anchor: (Left: {x}, Top: {y}, Width: {w}, Height: {h});
      Visible: false;
      Style: (Default: (Background: (TexturePath: "{Q}QHudPatch.png", Border: 16)), Hovered: (Background: (TexturePath: "{Q}ChapSlot.png", Border: 23)), Pressed: (Background: (TexturePath: "{Q}ChapSlot.png", Border: 23, Color: #ffffff(0.7))), Sounds: $C.@ButtonSounds);
      Group #ShopCellBar{i} {{ Anchor: (Left: 4, Top: 12, Width: 3, Height: {h - 24}); Background: #56677a; }}
      Group #ShopCellSel{i} {{
        Anchor: (Left: 0, Top: 0, Width: {w}, Height: {h});
        Visible: false;
        Group {{ Anchor: (Left: 2, Top: 2, Width: {w - 4}, Height: {h - 4}); Background: #f2c45e(0.07); }}
{sel}
      }}
      Group {{ Anchor: (Left: 12, Top: {h // 2 - 36}, Width: 72, Height: 72); Background: (TexturePath: "{Q}ChapSlot.png", Border: 23); }}
      ItemIcon #ShopCellIcon{i} {{ Anchor: (Left: 18, Top: {h // 2 - 30}, Width: 60, Height: 60); }}
      Label #ShopCellName{i} {{
        Anchor: (Left: 94, Top: 8, Width: {w - 104}, Height: 26);
        Style: (FontSize: 15, RenderBold: true, TextColor: {GOLD}, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 10);
      }}
      Group {{ Anchor: (Left: 94, Top: 36, Width: {w - 108}, Height: 1); Background: #48455c; }}
      Group {{
        Anchor: (Left: 94, Top: 42, Width: {w - 104}, Height: 24);
        LayoutMode: Left;
{indent(coins(f"ShopCell{i}C", CELL_COINS, size=18, font=14), 8)}
        Label #ShopCellInfo{i} {{
          Style: (FontSize: 14, RenderBold: true, TextColor: {COIN}, VerticalAlignment: Center);
        }}
      }}
      Label #ShopCellTag{i} {{
        Anchor: (Left: 94, Top: 70, Width: {w - 104}, Height: 22);
        Style: (FontSize: 11, RenderBold: true, RenderUppercase: true, TextColor: {TEXT2}, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 8);
      }}
    }}'''


def fact_row(r):
    """Rangee de deux faits (cle en petites capitales, valeur en gras) : categorie, qualite, niveau, lot, stock, sac."""
    tiles = []
    for c in range(2):
        i = r * 2 + c
        tiles.append(f'''          Group #ShopFact{i} {{
            FlexWeight: 1;
            Anchor: ({"Right: 4" if c == 0 else "Left: 4"});
            LayoutMode: Top;
            Padding: (Left: 12, Right: 8, Top: 6);
            Background: (TexturePath: "{Q}QHudPatch.png", Border: 16);
            Label #ShopFactK{i} {{
              Style: (FontSize: 11, RenderBold: true, RenderUppercase: true, TextColor: {TEXT2}, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 8);
            }}
            Label #ShopFactV{i} {{
              Anchor: (Top: 2);
              Style: (FontSize: 15, RenderBold: true, TextColor: #e8d9b0, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 10);
            }}
          }}''')
    return f'''      Group #ShopFactRow{r} {{
        Anchor: (Top: 6, Height: 50);
        LayoutMode: Left;
        Visible: false;
{chr(10).join(tiles)}
      }}'''


def total_slot(j):
    """Case du total : cadre de recompense (ChapSlot), piece, nombre en bas a droite."""
    x = TEXT_L + j * SLOT_PITCH
    return f'''    Group #ShopTotalSlot{j} {{
      Anchor: (Left: {x}, Top: {BUY_T}, Width: {SLOT_PX}, Height: {SLOT_PX});
      Visible: false;
      Background: (TexturePath: "{Q}ChapSlot.png", Border: 23);
      ItemIcon #ShopTotalIcon{j} {{ Anchor: (Left: 10, Top: 8, Width: 44, Height: 44); }}
      Label #ShopTotalQty{j} {{
        Anchor: (Left: 4, Top: {SLOT_PX - 24}, Width: {SLOT_PX - 10}, Height: 20);
        Style: (FontSize: 14, RenderBold: true, TextColor: #ffffff, HorizontalAlignment: End, VerticalAlignment: Center);
      }}
    }}'''


def ingredient(i):
    """Ligne d'ingredient : barre d'etat (vert : vous en avez assez, rouge : il en manque), icone, nom, possede / requis."""
    return f'''        Group #ShopIng{i} {{
          Anchor: (Top: 6, Height: 50);
          LayoutMode: Left;
          Background: (TexturePath: "{Q}QHudPatch.png", Border: 16);
          Visible: false;
          Group {{ Anchor: (Width: 8); LayoutMode: Middle; Group #ShopIngBar{i} {{ Anchor: (Width: 3, Height: 30); Background: {OK}; }} }}
          Group {{
            Anchor: (Width: 48);
            LayoutMode: Middle;
            ItemIcon #ShopIngIcon{i} {{ Anchor: (Width: 36, Height: 36); }}
          }}
          Group {{
            FlexWeight: 1;
            LayoutMode: Top;
            Padding: (Top: 7, Right: 4);
            Label #ShopIngName{i} {{
              Style: (FontSize: 14, RenderBold: true, TextColor: {HEAD});
            }}
            Label #ShopIngHave{i} {{
              Anchor: (Top: 2);
              Style: (FontSize: 12, RenderBold: true, TextColor: {TEXT2});
            }}
          }}
          Label #ShopIngQty{i} {{
            Anchor: (Width: 60);
            Style: (FontSize: 17, RenderBold: true, TextColor: {GOLD}, HorizontalAlignment: Center, VerticalAlignment: Center);
          }}
        }}'''


def check_ui(ui):
    assert "{{" not in ui and "}}" not in ui, "accolades doublees"
    assert ui.count("{") == ui.count("}"), "accolades non equilibrees"
    bad = re.findall(r"#[A-Za-z0-9]*_\w*", ui)
    assert not bad, "identifiants avec tiret bas : %s" % bad[:3]
    ids = re.findall(r"#([A-Za-z][A-Za-z0-9]*) \{", ui)
    dup = {i for i in ids if ids.count(i) > 1}
    assert not dup, "identifiants en double : %s" % sorted(dup)[:5]


def build():
    tabs = "\n".join(tab(i) for i in range(TABS))
    stats = "\n".join(stat(i) for i in range(4))
    cards = "\n".join(card(i) for i in range(CELLS))
    ings = "\n".join(ingredient(i) for i in range(ING))
    facts = "\n".join(fact_row(r) for r in range(FACTS // 2))
    slots = "\n".join(total_slot(j) for j in range(PRICE))
    ui = f'''$C = "../Common.ui";

// Boutique des PNJ (guards/ShopPage), facon fenetre de l'intendant et Chapitres. Genere par scripts/npcs/gen_shop_ui.py.
Group {{
  LayoutMode: Middle;
  Background: (TexturePath: "{Q}ChapBackground.png");

  Group #ShopRoot {{
    Anchor: (Width: 1920, Height: 1080);

    // ---- fond : voile de droite, bande d'encre, lueur, eclats ----
    Group {{ Anchor: (Left: 1003, Top: 0, Width: 917, Height: 1080); Background: #181822(0.5); }}
    Group {{ Anchor: (Left: 499, Top: 0, Width: 504, Height: 1080); Background: (TexturePath: "{Q}ChapSeparator.png"); }}
    Group {{ Anchor: (Left: 480, Top: 25, Width: 488, Height: 1055); Background: (TexturePath: "{Q}ChapGlows.png"); }}
    Group {{ Anchor: (Left: 0, Top: 0, Width: 1920, Height: 1080); Background: (TexturePath: "{Q}ChapShards.png"); }}
    Group {{ Anchor: (Left: {RIGHT_L - 12}, Top: 0, Width: {1920 - RIGHT_L + 12}, Height: 1080); Background: #0b0b12(0.45); }}

    // ---- categories ----
    Group {{ Anchor: (Left: {RIGHT_L - 8}, Top: {CAT_T - 6}, Width: {GRID_W + 16}, Height: {CAT_H + 12}); Background: #0b0b12(0.55); }}
    Group {{ Anchor: (Left: {RIGHT_L - 8}, Top: {CAT_T + CAT_H + 6}, Width: {GRID_W + 16}, Height: 8); Background: (TexturePath: "{Q}ChapDivider.png", HorizontalBorder: 24, VerticalBorder: 0); }}
{tabs}

    // ---- replique du marchand ----
    Label #ShopQuote {{
      Anchor: (Left: {RIGHT_L}, Top: {QUOTE_T}, Width: {GRID_W}, Height: 46);
      Style: (FontSize: 17, RenderBold: true, TextColor: {TAGLINE}, HorizontalAlignment: Center, VerticalAlignment: Center, Wrap: true);
    }}

    // ---- tuiles ----
{stats}

    // ---- section ----
    Label #ShopGridTitle {{
      Anchor: (Left: {RIGHT_L}, Top: {SECTION_T}, Width: 700, Height: 30);
      Style: (FontName: "Secondary", FontSize: 21, RenderBold: true, RenderUppercase: true, TextColor: {HEAD}, VerticalAlignment: Center);
    }}
    Group {{ Anchor: (Left: {RIGHT_L}, Top: {SECTION_T + 32}, Width: {GRID_W}, Height: 8); Background: (TexturePath: "{Q}ChapDivider.png", HorizontalBorder: 24, VerticalBorder: 0); }}
    Label #ShopFooter {{
      Anchor: (Left: {RIGHT_L + 700}, Top: {SECTION_T}, Width: {GRID_W - 700}, Height: 30);
      Style: (FontSize: 13, RenderBold: true, TextColor: {TEXT2}, HorizontalAlignment: End, VerticalAlignment: Center);
    }}

    // ---- cartes ----
{cards}
    Label #ShopEmpty {{
      Anchor: (Left: {RIGHT_L}, Top: {GRID_T + 140}, Width: {GRID_W}, Height: 60);
      Visible: false;
      Style: (FontSize: 18, RenderBold: true, TextColor: {TEXT2}, HorizontalAlignment: Center, VerticalAlignment: Center, Wrap: true);
    }}

    // ---- pages ----
    Group {{
      Anchor: (Left: {RIGHT_L}, Top: {PAGER_T}, Width: {GRID_W}, Height: 40);
      LayoutMode: Center;
      Group #ShopPager {{
        LayoutMode: Left;
        Visible: false;
        $C.@SmallSecondaryTextButton #ShopPrev {{
          @Anchor = Anchor(Width: 130, Right: 14);
          Text: "Précédente";
        }}
        Label #ShopPageLabel {{
          Anchor: (Width: 90);
          Style: (FontSize: 15, RenderBold: true, TextColor: {BODY}, HorizontalAlignment: Center, VerticalAlignment: Center);
        }}
        $C.@SmallSecondaryTextButton #ShopNext {{
          @Anchor = Anchor(Width: 130, Left: 14);
          Text: "Suivante";
        }}
      }}
    }}

    // ---- bourse et Fermer ----
    Group {{
      Anchor: (Left: {RIGHT_L}, Top: 960, Width: {GRID_W - 240}, Height: 44);
      LayoutMode: Left;
      Label {{
        Anchor: (Width: 86);
        Text: "Bourse";
        Style: (FontSize: 14, RenderBold: true, RenderUppercase: true, TextColor: {TEXT2}, VerticalAlignment: Center);
      }}
{indent(coins("ShopWallet", WALLET, size=22, font=16, color=COIN), 6)}
      Label #ShopWalletEmpty {{
        Text: "vide";
        Visible: false;
        Style: (FontSize: 15, RenderBold: true, TextColor: #8a6a6a, VerticalAlignment: Center);
      }}
      Label #ShopAccount {{
        Anchor: (Left: 18);
        Style: (FontSize: 14, RenderBold: true, TextColor: {TEXT2}, VerticalAlignment: Center);
      }}
    }}
    Group {{
      Anchor: (Left: {RIGHT_L + GRID_W - 220}, Top: 960, Width: 220, Height: 44);
      $C.@SecondaryTextButton #ShopClose {{
        @Anchor = Anchor(Left: 0, Top: 0, Width: 220);
        Text: "Fermer";
      }}
    }}
    Label {{
      Anchor: (Left: 40, Top: 1028, Width: 300, Height: 24);
      Text: "Échap  ·  Retour";
      Style: (FontSize: 15, RenderBold: true, RenderUppercase: true, TextColor: #77879d, VerticalAlignment: Center);
    }}

    // ---- marchand (colonne de gauche refaite le 2026-11-19, comme l'intendant) ----
    Group {{ Anchor: (Left: {PAGE_L - 14}, Top: 20, Width: {TEXT_W + 30}, Height: 1040); Background: #0b0b12(0.35); }}
    Label #ShopTitle {{
      Anchor: (Left: {TEXT_L}, Top: 30, Width: {TEXT_W}, Height: 34);
      Style: (FontName: "Secondary", FontSize: 22, RenderBold: true, RenderUppercase: true, TextColor: {HEAD}, HorizontalAlignment: Center, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 13);
    }}
    Label #ShopSub {{
      Anchor: (Left: {TEXT_L}, Top: 64, Width: {TEXT_W}, Height: 22);
      Style: (FontSize: 14, RenderBold: true, TextColor: {TAGLINE}, HorizontalAlignment: Center, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 10);
    }}

    // ---- fiche de l'article ----
    Group {{ Anchor: (Left: {CX - 84}, Top: 96, Width: 168, Height: 168); Background: (TexturePath: "{Q}ChapEmblemFrame.png"); }}
    Group {{ Anchor: (Left: {CX - 54}, Top: 126, Width: 108, Height: 108); Background: (TexturePath: "{Q}ChapEmblemBack.png"); }}
    ItemIcon #ShopBig {{ Anchor: (Left: {CX - 40}, Top: 140, Width: 80, Height: 80); }}
    Group #ShopBigMark {{ Anchor: (Left: {CX - 32}, Top: 148, Width: 64, Height: 64); Background: (TexturePath: "ShopEmblem.png"); Visible: false; }}
    Label #ShopKind {{
      Anchor: (Left: {TEXT_L}, Top: 268, Width: {TEXT_W}, Height: 20);
      Style: (FontSize: 13, RenderBold: true, RenderUppercase: true, TextColor: #b9a2d8, HorizontalAlignment: Center, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 9);
    }}
    Label #ShopName {{
      Anchor: (Left: {TEXT_L}, Top: 290, Width: {TEXT_W}, Height: 40);
      Style: (FontName: "Secondary", FontSize: 25, RenderBold: true, RenderUppercase: true, TextColor: {GOLD}, HorizontalAlignment: Center, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 14);
    }}
    Label #ShopHint {{
      Anchor: (Left: {TEXT_L}, Top: 330, Width: {TEXT_W}, Height: 40);
      Style: (FontSize: 15, RenderBold: true, TextColor: {OK}, HorizontalAlignment: Center, VerticalAlignment: Center, Wrap: true);
    }}
    Group {{ Anchor: (Left: {PAGE_L}, Top: 374, Width: 418, Height: 8); Background: (TexturePath: "{Q}ChapDivider.png", HorizontalBorder: 24, VerticalBorder: 0); }}

    Group #ShopBody {{
      Anchor: (Left: {TEXT_L}, Top: 390, Width: {TEXT_W + 14}, Height: {BUY_T - 30 - 390});
      LayoutMode: TopScrolling;
      ScrollbarStyle: $C.@DefaultScrollbarStyle;
      Padding: (Right: 14);

      // prix (pieces) sur toute la largeur
      Group {{
        Anchor: (Height: 62);
        LayoutMode: Top;
        Padding: (Top: 8, Bottom: 4);
        Background: (TexturePath: "{Q}QHudPatch.png", Border: 16);
        Group {{
          Anchor: (Height: 28);
          LayoutMode: Center;
{indent(coins("ShopPriceC", PRICE, size=22, font=17, color=COIN), 10)}
          Label #ShopPriceText {{
            Style: (FontSize: 17, RenderBold: true, TextColor: {COIN}, VerticalAlignment: Center);
          }}
        }}
        Label #ShopPriceCap {{
          Anchor: (Top: 2);
          Text: "Prix";
          Style: (FontSize: 11, RenderBold: true, RenderUppercase: true, TextColor: {TEXT2}, HorizontalAlignment: Center);
        }}
      }}
{facts}
      Label #ShopDesc {{
        Anchor: (Top: 10);
        Style: (FontSize: 14, RenderBold: true, TextColor: {BODY}, Wrap: true);
      }}

      // ingredients requis : seulement quand l'article demande des objets en plus des pieces
      Group #ShopIngPanel {{
        Anchor: (Top: 12);
        LayoutMode: Top;
        Visible: false;
        Label #ShopIngCap {{
          Text: "Ingrédients requis";
          Style: (FontSize: 12, RenderBold: true, RenderUppercase: true, TextColor: {GOLD});
        }}
{ings}
        Label #ShopIngState {{
          Anchor: (Top: 6, Height: 20);
          Style: (FontSize: 13, RenderBold: true, TextColor: {OK}, HorizontalAlignment: Center, VerticalAlignment: Center);
        }}
      }}
    }}

    // ---- achat : total en cases de recompense, puis quantite, puis bouton (2026-11-19) ----
    Group {{
      Anchor: (Left: {TEXT_L}, Top: {BUY_T - 26}, Width: {TEXT_W}, Height: 20);
      LayoutMode: Left;
      Label #ShopActHead {{
        Anchor: (Width: 90);
        Text: "Achat";
        Style: (FontSize: 13, RenderBold: true, RenderUppercase: true, TextColor: {TAGLINE}, VerticalAlignment: Center);
      }}
      Group {{ FlexWeight: 1; LayoutMode: Middle; Group {{ Anchor: (Height: 1); Background: #c58643(0.6); }} }}
      Label #ShopTotalCap {{
        Anchor: (Left: 10);
        Text: "Total";
        Style: (FontSize: 12, RenderBold: true, RenderUppercase: true, TextColor: {TEXT2}, VerticalAlignment: Center);
      }}
    }}
{slots}
    Label #ShopTotalText {{
      Anchor: (Left: {TEXT_L}, Top: {BUY_T}, Width: {TEXT_W}, Height: {SLOT_PX});
      Style: (FontName: "Secondary", FontSize: 22, RenderBold: true, TextColor: {COIN}, HorizontalAlignment: Center, VerticalAlignment: Center);
    }}
    Group {{
      Anchor: (Left: {TEXT_L}, Top: {BUY_T + SLOT_PX + 8}, Width: {TEXT_W}, Height: 40);
      LayoutMode: Left;
      $C.@SmallSecondaryTextButton #ShopQMinus {{ @Anchor = (Width: 48, Right: 4); Text: "-"; }}
      Group {{
        FlexWeight: 1;
        LayoutMode: Middle;
        Background: (TexturePath: "{Q}QHudPatch.png", Border: 16);
        Label #ShopQty {{
          Style: (FontSize: 18, RenderBold: true, TextColor: #ffffff, HorizontalAlignment: Center, VerticalAlignment: Center);
        }}
      }}
      $C.@SmallSecondaryTextButton #ShopQPlus {{ @Anchor = (Width: 48, Left: 4); Text: "+"; }}
      $C.@SmallSecondaryTextButton #ShopQ10 {{ @Anchor = (Width: 62, Left: 8); Text: "+10"; }}
      $C.@SmallSecondaryTextButton #ShopQMax {{ @Anchor = (Width: 70, Left: 4); Text: "Max"; }}
    }}
    Group {{
      Anchor: (Left: {TEXT_L}, Top: {BUY_T + SLOT_PX + 56}, Width: {TEXT_W}, Height: 48);
      $C.@TextButton #ShopBuy1 {{
        @Anchor = Anchor(Left: 0, Top: 0, Width: {TEXT_W}, Height: 48);
        Text: "Acheter";
      }}
    }}
    Label #ShopMsg {{
      Anchor: (Left: {TEXT_L}, Top: {BUY_T + SLOT_PX + 108}, Width: {TEXT_W}, Height: 56);
      Style: (FontSize: 14, RenderBold: true, TextColor: #00e7b6, HorizontalAlignment: Center, VerticalAlignment: Center, Wrap: true);
    }}
  }}
}}
'''
    check_ui(ui)
    os.makedirs(OUT, exist_ok=True)
    with open(os.path.join(OUT, "Shop.ui"), "w", encoding="utf-8") as f:
        f.write(ui)
    print("ecrit", os.path.join(OUT, "Shop.ui"))


if __name__ == "__main__":
    emblem()
    build()
