#!/usr/bin/env python3
"""
Fenetre de l'intendant « Registre de la maison » (2026-11-18), habillee comme la fenetre Chapitres des quetes
(Quest/Chapitres.ui, scripts/quetes/gen_chapitres_ui.py) dont elle reprend les images par « ../Quest/... ».

  - a gauche, la fiche : embleme (objet), surtitre, titre, sous-titre, texte et puces, champs (2 nombres, 2 textes),
    6 boutons d'action, ligne d'etat ;
  - a droite : barre de categories (icone + nom), replique de l'intendant, 4 tuiles de chiffres, titre de section,
    grille de 12 cartes cliquables (icone, nom, valeur, description, liseret colore), pages, Fermer.

Doit correspondre a guards/IntendantPage.java (CATS, CARDS, NOTES, ACTIONS).
Usage : python3 scripts/intendant/gen_intendant_ui.py
"""
import os
import re

RES = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "src", "main", "resources")
OUT = os.path.join(RES, "Common", "UI", "Custom", "Intendant")
Q = "../Quest/"

CATS, CARDS, NOTES, ACTIONS, FACTS = 9, 12, 10, 6, 8

# fiche (memes reperes que Chapitres)
PAGE_L, PAGE_T = 94, 114
TEXT_L, TEXT_W = PAGE_L + 2, 415
GOLD, TAGLINE, TEXT2, HEAD, BODY = "#f2c45e", "#C58643", "#77879D", "#C4CEDC", "#b8c2d0"

# partie droite
RIGHT_L = 611
CAT_T, CAT_W, CAT_H, CAT_PITCH = 34, 136, 78, 140
QUOTE_T = 126
STAT_T, STAT_W, STAT_H, STAT_PITCH = 182, 300, 82, 316
SECTION_T = 278
GRID_T, CARD_W, CARD_H, CARD_PX, CARD_PY, COLS = 328, 408, 128, 420, 138, 3
PAGER_T = 892
CX = TEXT_L + TEXT_W // 2          # axe de la fiche
STATUS_T, ACT_T = 770, 836         # message, puis actions (principale pleine largeur, puis grille 2 x 3)


def cat(i):
    x = RIGHT_L + i * CAT_PITCH
    return f'''    Button #IntCat{i} {{
      Anchor: (Left: {x}, Top: {CAT_T}, Width: {CAT_W}, Height: {CAT_H});
      Visible: false;
      Style: (Default: (Background: #000000(0)), Hovered: (Background: (TexturePath: "{Q}QHudPatch.png", Border: 16)), Pressed: (Background: (TexturePath: "{Q}ChapSlot.png", Border: 23)), Sounds: $C.@ButtonSounds);
      Group #IntCatGlow{i} {{ Anchor: (Left: 0, Top: 0, Width: {CAT_W}, Height: {CAT_H}); Background: (TexturePath: "{Q}ChapSlot.png", Border: 23); Visible: false; }}
      ItemIcon #IntCatIcon{i} {{ Anchor: (Left: {CAT_W // 2 - 22}, Top: 6, Width: 44, Height: 44); }}
      Label #IntCatLabel{i} {{
        Anchor: (Left: 4, Top: 50, Width: {CAT_W - 8}, Height: 20);
        Style: (FontSize: 13, RenderBold: true, RenderUppercase: true, TextColor: #96a9be, HorizontalAlignment: Center, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 10);
      }}
      Group #IntCatOn{i} {{ Anchor: (Left: 18, Top: {CAT_H - 5}, Width: {CAT_W - 36}, Height: 3); Background: {GOLD}; Visible: false; }}
    }}'''


def stat(i):
    x = RIGHT_L + i * STAT_PITCH
    return f'''    Group #IntStat{i} {{
      Anchor: (Left: {x}, Top: {STAT_T}, Width: {STAT_W}, Height: {STAT_H});
      Background: (TexturePath: "{Q}QHudPatch.png", Border: 16);
      Label #IntStatV{i} {{
        Anchor: (Left: 12, Top: 10, Width: {STAT_W - 24}, Height: 38);
        Style: (FontName: "Secondary", FontSize: 26, RenderBold: true, TextColor: {GOLD}, HorizontalAlignment: Center, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 14);
      }}
      Group {{ Anchor: (Left: {STAT_W // 2 - 40}, Top: 50, Width: 80, Height: 1); Background: #c58643(0.5); }}
      Label #IntStatL{i} {{
        Anchor: (Left: 12, Top: 54, Width: {STAT_W - 24}, Height: 20);
        Style: (FontSize: 12, RenderBold: true, RenderUppercase: true, TextColor: {TEXT2}, HorizontalAlignment: Center, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 9);
      }}
    }}'''


def card(i):
    x = RIGHT_L + (i % COLS) * CARD_PX
    y = GRID_T + (i // COLS) * CARD_PY
    w, h = CARD_W, CARD_H
    return f'''    Button #IntCard{i} {{
      Anchor: (Left: {x}, Top: {y}, Width: {w}, Height: {h});
      Visible: false;
      Style: (Default: (Background: (TexturePath: "{Q}QHudPatch.png", Border: 16)), Hovered: (Background: (TexturePath: "{Q}ChapSlot.png", Border: 23)), Pressed: (Background: (TexturePath: "{Q}ChapSlot.png", Border: 23, Color: #ffffff(0.7))), Sounds: $C.@ButtonSounds);
      Group #IntCardBar{i} {{ Anchor: (Left: 4, Top: 14, Width: 3, Height: {h - 28}); Background: #56677a; }}
      Group #IntCardSel{i} {{
        Anchor: (Left: 0, Top: 0, Width: {w}, Height: {h});
        Visible: false;
        Group {{ Anchor: (Left: 2, Top: 2, Width: {w - 4}, Height: {h - 4}); Background: #f2c45e(0.07); }}
        Group {{ Anchor: (Left: 0, Top: 0, Width: {w}, Height: 2); Background: {GOLD}; }}
        Group {{ Anchor: (Left: 0, Top: {h - 2}, Width: {w}, Height: 2); Background: {GOLD}; }}
        Group {{ Anchor: (Left: 0, Top: 0, Width: 2, Height: {h}); Background: {GOLD}; }}
        Group {{ Anchor: (Left: {w - 2}, Top: 0, Width: 2, Height: {h}); Background: {GOLD}; }}
      }}
      Group {{ Anchor: (Left: 14, Top: {h // 2 - 40}, Width: 80, Height: 80); Background: (TexturePath: "{Q}ChapSlot.png", Border: 23); }}
      ItemIcon #IntCardIcon{i} {{ Anchor: (Left: 22, Top: {h // 2 - 32}, Width: 64, Height: 64); }}
      Label #IntCardMark{i} {{
        Anchor: (Left: 14, Top: {h // 2 - 40}, Width: 80, Height: 80);
        Visible: false;
        Style: (FontName: "Secondary", FontSize: 40, RenderBold: true, TextColor: #c58643, HorizontalAlignment: Center, VerticalAlignment: Center);
      }}
      Label #IntCardName{i} {{
        Anchor: (Left: 106, Top: 12, Width: {w - 222}, Height: 28);
        Style: (FontSize: 17, RenderBold: true, TextColor: {GOLD}, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 12);
      }}
      Label #IntCardValue{i} {{
        Anchor: (Left: {w - 116}, Top: 12, Width: 102, Height: 28);
        Style: (FontSize: 15, RenderBold: true, TextColor: #e8d9b0, HorizontalAlignment: End, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 10);
      }}
      Group {{ Anchor: (Left: 106, Top: 42, Width: {w - 120}, Height: 1); Background: #48455c; }}
      Label #IntCardMeta{i} {{
        Anchor: (Left: 106, Top: 48, Width: {w - 120}, Height: {h - 58});
        Style: (FontSize: 13, RenderBold: true, TextColor: #9aa7b8, Wrap: true);
      }}
    }}'''


def note(i):
    return f'''        Group #IntNote{i} {{
          Anchor: (Top: 10);
          LayoutMode: Left;
          Visible: false;
          Group {{ Anchor: (Width: 22); LayoutMode: Top; Group {{ Anchor: (Width: 20, Height: 20); Background: (TexturePath: "{Q}ChapBullet.png"); }} }}
          Group {{
            FlexWeight: 1;
            LayoutMode: Top;
            Padding: (Left: 10, Top: 1);
            Label #IntNoteText{i} {{ Style: (FontSize: 15, RenderBold: true, TextColor: {TEXT2}, Wrap: true); }}
          }}
        }}'''


def action(i):
    """Emplacement d'action : bouton normal (#IntAct) et bouton rouge des gestes destructeurs (#IntActD), l'un ou l'autre visible.
    0 = action principale, pleine largeur ; 1 a 5 : grille de deux colonnes."""
    if i == 0:
        x, y, w = TEXT_L, ACT_T, TEXT_W
        kind = "$C.@TextButton"
    else:
        col, row = (i - 1) % 2, (i - 1) // 2
        w = (TEXT_W - 8) // 2
        x, y = TEXT_L + col * (w + 8), ACT_T + 52 + row * 50
        kind = "$C.@SecondaryTextButton"
    return f'''    Group {{
      Anchor: (Left: {x}, Top: {y}, Width: {w}, Height: 44);
      {kind} #IntAct{i} {{
        @Anchor = Anchor(Left: 0, Top: 0, Width: {w});
        Text: "Action";
        Visible: false;
      }}
      $C.@CancelTextButton #IntActD{i} {{
        @Anchor = Anchor(Left: 0, Top: 0, Width: {w});
        Text: "Action";
        Visible: false;
      }}
    }}'''


def fact_row(r):
    """Rangee de deux faits (cle en petites capitales, valeur en gras) en tete de la fiche."""
    tiles = []
    for c in range(2):
        i = r * 2 + c
        tiles.append(f'''          Group #IntFact{i} {{
            FlexWeight: 1;
            Anchor: ({"Right: 4" if c == 0 else "Left: 4"});
            LayoutMode: Top;
            Padding: (Left: 12, Right: 8, Top: 7);
            Background: (TexturePath: "{Q}QHudPatch.png", Border: 16);
            Label #IntFactK{i} {{
              Style: (FontSize: 11, RenderBold: true, RenderUppercase: true, TextColor: {TEXT2}, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 8);
            }}
            Label #IntFactV{i} {{
              Anchor: (Top: 2);
              Style: (FontSize: 15, RenderBold: true, TextColor: #e8d9b0, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 10);
            }}
          }}''')
    return f'''        Group #IntFactRow{r} {{
          Anchor: (Top: {0 if r == 0 else 6}, Height: 52);
          LayoutMode: Left;
          Visible: false;
{chr(10).join(tiles)}
        }}'''


def field(n, kind):
    tag = "NumberField" if kind == "n" else "TextField"
    extra = "\n          Format: (MaxDecimalPlaces: 0, Step: 1);" if kind == "n" else '\n          PlaceholderText: "";'
    return f'''        Group #IntField{n} {{
          Anchor: (Top: 12);
          LayoutMode: Top;
          Visible: false;
          Label #IntFieldLabel{n} {{
            Style: (FontSize: 12, RenderBold: true, RenderUppercase: true, TextColor: {TEXT2});
          }}
          $C.@{tag} #IntInput{n} {{
          @Anchor = Anchor(Top: 4);{extra}
          }}
        }}'''


def check_ui(ui):
    assert "{{" not in ui and "}}" not in ui, "accolades doublees"
    assert ui.count("{") == ui.count("}"), "accolades non equilibrees"
    bad = re.findall(r"#[A-Za-z0-9]*_\w*", ui)
    assert not bad, "identifiants avec tiret bas : %s" % bad[:3]


def build():
    cats = "\n".join(cat(i) for i in range(CATS))
    stats = "\n".join(stat(i) for i in range(4))
    cards = "\n".join(card(i) for i in range(CARDS))
    notes = "\n".join(note(i) for i in range(NOTES))
    actions = "\n".join(action(i) for i in range(ACTIONS))
    fields = "\n".join([field(0, "n"), field(1, "n"), field(4, "n"), field(2, "t"), field(3, "t")])
    facts = "\n".join(fact_row(r) for r in range(FACTS // 2))
    em_l, em_t = PAGE_L + 75, PAGE_T
    grid_w = COLS * CARD_PX - (CARD_PX - CARD_W)
    ui = f'''$C = "../Common.ui";

// Registre de la maison (intendant), facon fenetre Chapitres. Genere par scripts/intendant/gen_intendant_ui.py.
Group {{
  LayoutMode: Middle;
  Background: (TexturePath: "{Q}ChapBackground.png");

  Group #IntPage {{
    Anchor: (Width: 1920, Height: 1080);

    // ---- fond : voile de droite, bande d'encre, lueur, eclats ----
    Group {{ Anchor: (Left: 1003, Top: 0, Width: 917, Height: 1080); Background: #181822(0.5); }}
    Group {{ Anchor: (Left: 499, Top: 0, Width: 504, Height: 1080); Background: (TexturePath: "{Q}ChapSeparator.png"); }}
    Group {{ Anchor: (Left: 480, Top: 25, Width: 488, Height: 1055); Background: (TexturePath: "{Q}ChapGlows.png"); }}
    Group {{ Anchor: (Left: 0, Top: 0, Width: 1920, Height: 1080); Background: (TexturePath: "{Q}ChapShards.png"); }}
    Group {{ Anchor: (Left: {RIGHT_L - 12}, Top: 0, Width: {1920 - RIGHT_L + 12}, Height: 1080); Background: #0b0b12(0.45); }}

    // ---- categories ----
    Group {{ Anchor: (Left: {RIGHT_L - 8}, Top: {CAT_T - 6}, Width: {CATS * CAT_PITCH + 12}, Height: {CAT_H + 12}); Background: #0b0b12(0.55); }}
    Group {{ Anchor: (Left: {RIGHT_L - 8}, Top: {CAT_T + CAT_H + 6}, Width: {CATS * CAT_PITCH + 12}, Height: 8); Background: (TexturePath: "{Q}ChapDivider.png", HorizontalBorder: 24, VerticalBorder: 0); }}
{cats}

    // ---- replique de l'intendant ----
    Label #IntQuote {{
      Anchor: (Left: {RIGHT_L}, Top: {QUOTE_T}, Width: {grid_w}, Height: 46);
      Style: (FontSize: 17, RenderBold: true, TextColor: {TAGLINE}, HorizontalAlignment: Center, VerticalAlignment: Center, Wrap: true);
    }}

    // ---- tuiles ----
{stats}

    // ---- section ----
    Label #IntSection {{
      Anchor: (Left: {RIGHT_L}, Top: {SECTION_T}, Width: 700, Height: 30);
      Style: (FontName: "Secondary", FontSize: 21, RenderBold: true, RenderUppercase: true, TextColor: {HEAD}, VerticalAlignment: Center);
    }}
    Group {{ Anchor: (Left: {RIGHT_L}, Top: {SECTION_T + 32}, Width: {grid_w}, Height: 8); Background: (TexturePath: "{Q}ChapDivider.png", HorizontalBorder: 24, VerticalBorder: 0); }}
    Label #IntFooter {{
      Anchor: (Left: {RIGHT_L + 700}, Top: {SECTION_T}, Width: {grid_w - 700}, Height: 30);
      Style: (FontSize: 13, RenderBold: true, TextColor: {TEXT2}, HorizontalAlignment: End, VerticalAlignment: Center);
    }}

    // ---- cartes ----
{cards}
    Label #IntEmpty {{
      Anchor: (Left: {RIGHT_L}, Top: {GRID_T + 140}, Width: {grid_w}, Height: 60);
      Visible: false;
      Style: (FontSize: 18, RenderBold: true, TextColor: {TEXT2}, HorizontalAlignment: Center, VerticalAlignment: Center, Wrap: true);
    }}

    // ---- pages ----
    Group {{
      Anchor: (Left: {RIGHT_L}, Top: {PAGER_T}, Width: {grid_w}, Height: 40);
      LayoutMode: Center;
      Group #IntPager {{
        LayoutMode: Left;
        Visible: false;
        $C.@SmallSecondaryTextButton #IntPrev {{
          @Anchor = Anchor(Width: 130, Right: 14);
          Text: "Précédente";
        }}
        Label #IntPageLabel {{
          Anchor: (Width: 90);
          Style: (FontSize: 15, RenderBold: true, TextColor: {BODY}, HorizontalAlignment: Center, VerticalAlignment: Center);
        }}
        $C.@SmallSecondaryTextButton #IntNext {{
          @Anchor = Anchor(Width: 130, Left: 14);
          Text: "Suivante";
        }}
      }}
    }}

    Group {{
      Anchor: (Left: {RIGHT_L + grid_w - 220}, Top: 960, Width: 220, Height: 44);
      $C.@SecondaryTextButton #IntClose {{
        @Anchor = Anchor(Left: 0, Top: 0, Width: 220);
        Text: "Fermer";
      }}
    }}
    Label #IntPurse {{
      Anchor: (Left: {RIGHT_L}, Top: 960, Width: 760, Height: 44);
      Style: (FontSize: 15, RenderBold: true, TextColor: {BODY}, VerticalAlignment: Center);
    }}
    Label {{
      Anchor: (Left: {RIGHT_L + grid_w - 220}, Top: 1010, Width: 220, Height: 24);
      Text: "Échap  ·  Retour";
      Style: (FontSize: 13, RenderBold: true, RenderUppercase: true, TextColor: #77879d, HorizontalAlignment: Center, VerticalAlignment: Center);
    }}

    // ---- fiche (refaite le 2026-11-19 : embleme compact, faits, actions groupees) ----
    Group {{ Anchor: (Left: {PAGE_L - 14}, Top: 20, Width: {TEXT_W + 30}, Height: 1040); Background: #0b0b12(0.35); }}
    Group {{ Anchor: (Left: {CX - 84}, Top: 30, Width: 168, Height: 168); Background: (TexturePath: "{Q}ChapEmblemFrame.png"); }}
    Group {{ Anchor: (Left: {CX - 54}, Top: 60, Width: 108, Height: 108); Background: (TexturePath: "{Q}ChapEmblemBack.png"); }}
    ItemIcon #IntEmblem {{ Anchor: (Left: {CX - 40}, Top: 74, Width: 80, Height: 80); }}
    Label #IntEmblemMark {{
      Anchor: (Left: {CX - 54}, Top: 64, Width: 108, Height: 100);
      Visible: false;
      Style: (FontName: "Secondary", FontSize: 52, RenderBold: true, TextColor: #ffe98a, HorizontalAlignment: Center, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 24);
    }}
    Label #IntKicker {{
      Anchor: (Left: {TEXT_L}, Top: 202, Width: {TEXT_W}, Height: 20);
      Style: (FontSize: 13, RenderBold: true, RenderUppercase: true, TextColor: #b9a2d8, HorizontalAlignment: Center, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 9);
    }}
    Label #IntTitle {{
      Anchor: (Left: {TEXT_L}, Top: 224, Width: {TEXT_W}, Height: 40);
      Style: (FontName: "Secondary", FontSize: 27, RenderBold: true, RenderUppercase: true, TextColor: {GOLD}, HorizontalAlignment: Center, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 15);
    }}
    Label #IntTagline {{
      Anchor: (Left: {TEXT_L}, Top: 264, Width: {TEXT_W}, Height: 46);
      Style: (FontSize: 16, RenderBold: true, TextColor: {TAGLINE}, HorizontalAlignment: Center, VerticalAlignment: Center, Wrap: true);
    }}
    Group {{ Anchor: (Left: {PAGE_L}, Top: 316, Width: 418, Height: 8); Background: (TexturePath: "{Q}ChapDivider.png", HorizontalBorder: 24, VerticalBorder: 0); }}

    Group #IntBody {{
      Anchor: (Left: {TEXT_L}, Top: 334, Width: {TEXT_W + 14}, Height: {STATUS_T - 334 - 4});
      LayoutMode: TopScrolling;
      ScrollbarStyle: $C.@DefaultScrollbarStyle;
      Padding: (Right: 14);

{facts}
      Label #IntText {{
        Anchor: (Top: 10);
        Style: (FontSize: 15, RenderBold: true, TextColor: {BODY}, Wrap: true);
      }}
{notes}
{fields}
    }}

    Label #IntStatus {{
      Anchor: (Left: {TEXT_L}, Top: {STATUS_T}, Width: {TEXT_W}, Height: 36);
      Style: (FontSize: 14, RenderBold: true, TextColor: #00e7b6, HorizontalAlignment: Center, VerticalAlignment: Center, Wrap: true);
    }}

    // ---- actions ----
    Group #IntActHead {{
      Anchor: (Left: {TEXT_L}, Top: {ACT_T - 26}, Width: {TEXT_W}, Height: 20);
      LayoutMode: Left;
      Label {{
        Anchor: (Width: 84);
        Text: "Actions";
        Style: (FontSize: 13, RenderBold: true, RenderUppercase: true, TextColor: {TAGLINE}, VerticalAlignment: Center);
      }}
      Group {{ FlexWeight: 1; LayoutMode: Middle; Group {{ Anchor: (Height: 1); Background: #c58643(0.6); }} }}
    }}
{actions}
  }}
}}
'''
    check_ui(ui)
    os.makedirs(OUT, exist_ok=True)
    path = os.path.join(OUT, "Intendant.ui")
    with open(path, "w", encoding="utf-8") as f:
        f.write(ui)
    print("ecrit", path)


if __name__ == "__main__":
    build()
