#!/usr/bin/env python3
"""
Fenetre du banquier « Banque » (Common/UI/Custom/Npc/Banque.ui), pilotee par guards/BankPage. Refaite le 2026-11-24 a la
demande de l'utilisateur (« pas assez claire pour l'UX/UI ») avec l'habillage de la fenetre Chapitres des quetes et de
l'intendant (images ../Quest/...).

  - a gauche, la fiche : embleme, nom du banquier, puis VOS AVOIRS en grand (dans le sac, a la banque, total, dette) et le
    message de la banque (vert : fait ; rouge : refus) ;
  - a droite, trois onglets larges avec une ligne d'explication :
      0 Deposer / Retirer : deux colonnes cote a cote, chacune avec son sens ecrit (Sac -> Banque, Banque -> Sac), une somme
        exacte en cuivres (sa valeur en pieces a cote), un apercu en direct, le bouton et 4 raccourcis ; dessous, les pieces du sac ;
      1 Change : une ligne par paire de pieces voisines (petite a gauche, grosse a droite), deux boutons qui disent tout
        (« 10 argent -> 1 or », « 1 or -> 10 argent »), remplaces par un texte gris s'il manque des pieces ; Tout regrouper ;
      2 Pret : a gauche l'etat (plafond ou dette), le champ, l'apercu et les boutons ; a droite les regles du pret.

Usage : python3 scripts/npcs/gen_banque_ui.py
"""
import os
import re

HERE = os.path.dirname(os.path.abspath(__file__))
RES = os.path.join(HERE, "..", "..", "src", "main", "resources")
OUT = os.path.join(RES, "Common", "UI", "Custom", "Npc")
Q = "../Quest/"

COINS, TABS, QUICK = 7, 3, 4
GOLD, TAGLINE, TEXT2, HEAD, BODY = "#f2c45e", "#C58643", "#77879D", "#C4CEDC", "#b8c2d0"
GREEN, RED = "#8fd18f", "#e0796b"

PAGE_L = 94
TEXT_L, TEXT_W = PAGE_L + 2, 415
CX = TEXT_L + TEXT_W // 2
RIGHT_L, RIGHT_W = 611, 1260
TAB_T, TAB_W, TAB_H, TAB_PITCH = 34, 408, 84, 426
QUOTE_T = 136
PANEL_T = 196
COL_W = 620


def tab(i):
    x = RIGHT_L + i * TAB_PITCH
    return f'''    Button #BqTab{i} {{
      Anchor: (Left: {x}, Top: {TAB_T}, Width: {TAB_W}, Height: {TAB_H});
      Style: (Default: (Background: #000000(0)), Hovered: (Background: (TexturePath: "{Q}QHudPatch.png", Border: 16)), Pressed: (Background: (TexturePath: "{Q}ChapSlot.png", Border: 23)), Sounds: $C.@ButtonSounds);
      Group #BqTabGlow{i} {{ Anchor: (Left: 0, Top: 0, Width: {TAB_W}, Height: {TAB_H}); Background: (TexturePath: "{Q}ChapSlot.png", Border: 23); Visible: false; }}
      ItemIcon #BqTabIcon{i} {{ Anchor: (Left: 18, Top: {TAB_H // 2 - 24}, Width: 48, Height: 48); }}
      Label #BqTabLabel{i} {{
        Anchor: (Left: 80, Top: 14, Width: {TAB_W - 96}, Height: 26);
        Style: (FontName: "Secondary", FontSize: 20, RenderBold: true, RenderUppercase: true, TextColor: #96a9be, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 12);
      }}
      Label #BqTabSub{i} {{
        Anchor: (Left: 80, Top: 42, Width: {TAB_W - 96}, Height: 26);
        Style: (FontSize: 13, RenderBold: true, TextColor: {TEXT2}, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 10);
      }}
      Group #BqTabOn{i} {{ Anchor: (Left: 18, Top: {TAB_H - 5}, Width: {TAB_W - 36}, Height: 3); Background: {GOLD}; Visible: false; }}
    }}'''


def label(lid, left, top, width, height, size=15, color=BODY, extra="", text=None, font="", visible=True):
    t = f'\n      Text: "{text}";' if text is not None else ""
    v = "" if visible else "\n      Visible: false;"
    f = 'FontName: "Secondary", ' if font else ""
    head = f"Label {lid}" if lid else "Label"
    return f'''      {head} {{
        Anchor: (Left: {left}, Top: {top}, Width: {width}, Height: {height});{t}{v}
        Style: ({f}FontSize: {size}, RenderBold: true, TextColor: {color}{extra});
      }}'''


def button(kind, bid, left, top, width, text, height=44):
    return f'''      Group {{
        Anchor: (Left: {left}, Top: {top}, Width: {width}, Height: {height});
        $C.@{kind} #{bid} {{
          @Anchor = Anchor(Left: 0, Top: 0, Width: {width});
          Text: "{text}";
        }}
      }}'''


def amount_row(prefix, left, top):
    """Somme exacte en cuivres (2026-11-24, demande de l'utilisateur), et sa valeur en pieces a cote."""
    return f'''      Group {{
        Anchor: (Left: {left}, Top: {top}, Width: 230, Height: 40);
        $C.@NumberField #{prefix}Amount {{
          Anchor: (Left: 0, Top: 0, Width: 230, Height: 40);
          Format: (MaxDecimalPlaces: 0, Step: 100);
        }}
      }}
      Label #{prefix}Worth {{
        Anchor: (Left: {left + 244}, Top: {top}, Width: 328, Height: 40);
        Style: (FontSize: 15, RenderBold: true, TextColor: {GOLD}, VerticalAlignment: Center, Wrap: true, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 10);
      }}'''


def box(left, top, width, height):
    return f'''      Group {{ Anchor: (Left: {left}, Top: {top}, Width: {width}, Height: {height}); Background: (TexturePath: "{Q}QHudPatch.png", Border: 16); }}'''


def divider(left, top, width):
    return f'''      Group {{ Anchor: (Left: {left}, Top: {top}, Width: {width}, Height: 8); Background: (TexturePath: "{Q}ChapDivider.png", HorizontalBorder: 24, VerticalBorder: 0); }}'''


def flow_column(prefix, left, title, flow, sub, action, main_kind, quick_labels):
    """Une colonne Deposer ou Retirer."""
    w = COL_W - 48
    qw = (w - 3 * 8) // QUICK
    quick = "\n".join(button("SecondaryTextButton", f"{prefix}Q{i}", left + 24 + i * (qw + 8), 384, qw, quick_labels[i])
                      for i in range(QUICK))
    return "\n".join([
        box(left, 0, COL_W, 448),
        label("", left + 24, 16, w, 34, 26, GOLD, ", RenderUppercase: true, VerticalAlignment: Center", title, font=True),
        label(f"#{prefix}Flow", left + 24, 52, w, 24, 15, GREEN if prefix == "BqDep" else GOLD,
              ", RenderUppercase: true, VerticalAlignment: Center", flow),
        label("", left + 24, 78, w, 22, 14, TEXT2, ", VerticalAlignment: Center, Wrap: true", sub),
        divider(left + 24, 108, w),
        label("", left + 24, 126, w, 20, 12, TEXT2, ", RenderUppercase: true, VerticalAlignment: Center", "Somme en cuivres (100 = 1 or)"),
        amount_row(prefix, left + 24, 150),
        label(f"#{prefix}Preview", left + 24, 198, w, 52, 15, BODY, ", VerticalAlignment: Center, Wrap: true", ""),
        button(main_kind, f"{prefix}Go", left + 24, 260, w, action, 50),
        label("", left + 24, 356, w, 20, 12, TEXT2, ", RenderUppercase: true, VerticalAlignment: Center", "Raccourcis"),
        quick,
    ])


def coin_strip(top):
    w, gap = 172, 9
    out = [label("", 0, top, 600, 26, 13, HEAD, ", RenderUppercase: true, VerticalAlignment: Center", "Les pièces de votre sac")]
    for i in range(COINS):
        x = i * (w + gap)
        out.append(f'''      Group {{ Anchor: (Left: {x}, Top: {top + 32}, Width: {w}, Height: 118); Background: (TexturePath: "{Q}ChapSlot.png", Border: 23); }}
      ItemIcon #BqSacIcon{i} {{ Anchor: (Left: {x + w // 2 - 24}, Top: {top + 42}, Width: 48, Height: 48); }}''')
        out.append(label(f"#BqSacName{i}", x + 6, top + 94, w - 12, 22, 13, TEXT2,
                         ", RenderUppercase: true, HorizontalAlignment: Center, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 9"))
        out.append(label(f"#BqSacCount{i}", x + 6, top + 116, w - 12, 26, 17, GOLD,
                         ", HorizontalAlignment: Center, VerticalAlignment: Center"))
    return "\n".join(out)


def coin_side(r, side, left, top):
    """Une piece de l'echange : cadre + icone, nom, nombre dans le sac (alignes a droite pour la grosse piece)."""
    right = side == "Big"
    icon_x = left + 300 if right else left
    text_x = left if right else left + 80
    align = ", HorizontalAlignment: End" if right else ""
    return "\n".join([
        f'''      Group {{ Anchor: (Left: {icon_x}, Top: {top + 6}, Width: 64, Height: 64); Background: (TexturePath: "{Q}ChapSlot.png", Border: 23); }}
      ItemIcon #BqChg{side}Icon{r} {{ Anchor: (Left: {icon_x + 8}, Top: {top + 14}, Width: 48, Height: 48); }}''',
        label(f"#BqChg{side}Name{r}", text_x, top + 10, 216, 28, 20, GOLD, ", RenderUppercase: true, VerticalAlignment: Center" + align, font=True),
        label(f"#BqChg{side}Count{r}", text_x, top + 40, 216, 24, 14, BODY, ", VerticalAlignment: Center" + align),
    ])


def change_row(r):
    """Echange entre une petite piece (a gauche) et celle du dessus (a droite) ; deux boutons qui disent tout
    (« 10 argent → 1 or », « 1 or → 10 argent »). Sans assez de pieces, un texte gris remplace le bouton."""
    top = 112 + r * 88
    mid, bw = 400, 222
    return "\n".join([
        box(0, top - 4, RIGHT_W, 80),
        coin_side(r, "Small", 20, top),
        coin_side(r, "Big", RIGHT_W - 384, top),
        button("TextButton", f"BqUp{r}", mid, top + 12, bw, "Regrouper"),
        label(f"#BqUpNo{r}", mid, top + 12, bw, 44, 13, "#56677a",
              ", HorizontalAlignment: Center, VerticalAlignment: Center, Wrap: true", visible=False),
        button("TextButton", f"BqDown{r}", mid + bw + 16, top + 12, bw, "Casser"),
        label(f"#BqDownNo{r}", mid + bw + 16, top + 12, bw, 44, 13, "#56677a",
              ", HorizontalAlignment: Center, VerticalAlignment: Center, Wrap: true", visible=False),
    ])


RULES = [
    "Vous pouvez emprunter jusqu'à 3 fois ce que vous possédez (sac + banque). Un seul prêt à la fois.",
    "Vous rendez la somme + 20 % d'intérêts, ici, depuis votre compte en banque, dans les 7 jours.",
    "Pour rembourser, déposez d'abord vos pièces dans l'onglet Déposer / Retirer.",
    "Passé 7 jours, la banque met une prime sur votre tête : vous devenez CRIMINEL ÉTOILÉ, traqué par les chasseurs de prime.",
    "Si vous mourez alors, vous perdez TOUT : niveau 1, classe, objets, argent, famille et guilde.",
    "Solder la dette, même en retard, lève la prime.",
]


def rules(left):
    w = COL_W - 48
    out = [box(left, 0, COL_W, 560),
           label("", left + 24, 16, w, 34, 26, GOLD, ", RenderUppercase: true, VerticalAlignment: Center", "Règles du prêt", font=True),
           divider(left + 24, 56, w)]
    for i, r in enumerate(RULES):
        top = 76 + i * 76
        out.append(f'''      Group {{ Anchor: (Left: {left + 24}, Top: {top + 2}, Width: 20, Height: 20); Background: (TexturePath: "{Q}ChapBullet.png"); }}''')
        color = "#e8b48a" if i >= 3 else BODY
        out.append(label("", left + 54, top, w - 30, 70, 15, color, ", Wrap: true", r))
    return "\n".join(out)


def loan_column():
    w = COL_W - 48
    return "\n".join([
        box(0, 0, COL_W, 560),
        label("#BqLoanHead", 24, 16, w, 34, 26, GOLD, ", RenderUppercase: true, VerticalAlignment: Center", font=True),
        divider(24, 56, w),
        label("#BqLoanBigKey", 24, 74, w, 20, 12, TEXT2, ", RenderUppercase: true, VerticalAlignment: Center"),
        label("#BqLoanBig", 24, 96, w, 44, 34, GOLD, ", VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 16", font=True),
        label("#BqLoanSmall", 24, 142, w, 48, 15, BODY, ", VerticalAlignment: Center, Wrap: true"),
        label("", 24, 204, w, 20, 12, TEXT2, ", RenderUppercase: true, VerticalAlignment: Center", "Somme en cuivres (100 = 1 or)"),
        amount_row("BqLoan", 24, 228),
        label("#BqLoanPreview", 24, 276, w, 60, 15, BODY, ", VerticalAlignment: Center, Wrap: true", ""),
        f'''      Group #BqBorrowBox {{
        Anchor: (Left: 24, Top: 350, Width: {w}, Height: 50);
        $C.@CancelTextButton #BqBorrow {{
          @Anchor = Anchor(Left: 0, Top: 0, Width: {w});
          Text: "Emprunter";
        }}
      }}''',
        f'''      Group #BqRepayBox {{
        Anchor: (Left: 24, Top: 350, Width: {w}, Height: 110);
        Visible: false;
        Group {{
          Anchor: (Left: 0, Top: 0, Width: {w}, Height: 50);
          $C.@TextButton #BqRepay {{
            @Anchor = Anchor(Left: 0, Top: 0, Width: {w});
            Text: "Rembourser cette somme";
          }}
        }}
        Group {{
          Anchor: (Left: 0, Top: 58, Width: {w}, Height: 44);
          $C.@SecondaryTextButton #BqRepayAll {{
            @Anchor = Anchor(Left: 0, Top: 0, Width: {w});
            Text: "Tout rembourser";
          }}
        }}
      }}''',
    ])


def check_ui(ui):
    assert "{{" not in ui and "}}" not in ui, "accolades doublees"
    assert ui.count("{") == ui.count("}"), "accolades non equilibrees"
    bad = re.findall(r"#[A-Za-z0-9]*_\w*", ui)
    assert not bad, "identifiants avec tiret bas : %s" % bad[:3]
    ids = re.findall(r"#([A-Z][A-Za-z0-9]*) \{", ui)
    dup = {i for i in ids if ids.count(i) > 1}
    assert not dup, "identifiants en double : %s" % sorted(dup)[:5]


def build():
    tabs = "\n".join(tab(i) for i in range(TABS))
    p0 = "\n".join([
        flow_column("BqDep", 0, "Déposer", "Sac  →  Banque", "Vos pièces quittent le sac et sont mises à l'abri à la banque.",
                    "Déposer", "TextButton", ["Tout le sac", "1 or", "1 diamant", "1 zénith"]),
        flow_column("BqWd", COL_W + 20, "Retirer", "Banque  →  Sac", "L'argent du compte revient dans votre sac, en pièces.",
                    "Retirer", "TextButton", ["Tout le compte", "1 or", "1 diamant", "1 zénith"]),
        coin_strip(470),
    ])
    p1 = "\n".join([
        box(0, 0, RIGHT_W, 92),
        label("", 24, 10, 800, 34, 22, GOLD, ", RenderUppercase: true, VerticalAlignment: Center", "Échanger des pièces", font=True),
        label("", 24, 46, 800, 40, 14, BODY, ", VerticalAlignment: Center, Wrap: true",
              "Gratuit. Chaque ligne échange deux pièces voisines : 10 petites valent 1 grosse. Chaque bouton dit ce que vous donnez et ce que vous recevez."),
        button("TextButton", "BqTidy", RIGHT_W - 344, 24, 320, "Tout regrouper"),
        "\n".join(change_row(r) for r in range(COINS - 1)),
        label("", 0, 112 + (COINS - 1) * 88, RIGHT_W, 30, 13, TEXT2, ", HorizontalAlignment: Center, VerticalAlignment: Center",
              "« Tout regrouper » range toute votre bourse dans le moins de pièces possible."),
    ])
    p2 = "\n".join([loan_column(), rules(COL_W + 20)])
    em_icon = f'''    ItemIcon #BqEmblem {{ Anchor: (Left: {CX - 40}, Top: 74, Width: 80, Height: 80); }}'''

    def tile(tid, top, key, color, h=92):
        return f'''    Group {{ Anchor: (Left: {TEXT_L}, Top: {top}, Width: {TEXT_W}, Height: {h}); Background: (TexturePath: "{Q}QHudPatch.png", Border: 16); }}
    Group {{ Anchor: (Left: {TEXT_L + 14}, Top: {top + h // 2 - 30}, Width: 60, Height: 60); Background: (TexturePath: "{Q}ChapSlot.png", Border: 23); }}
    ItemIcon #{tid}Icon {{ Anchor: (Left: {TEXT_L + 20}, Top: {top + h // 2 - 24}, Width: 48, Height: 48); }}
    Label {{
      Anchor: (Left: {TEXT_L + 88}, Top: {top + 12}, Width: {TEXT_W - 100}, Height: 20);
      Text: "{key}";
      Style: (FontSize: 12, RenderBold: true, RenderUppercase: true, TextColor: {TEXT2}, VerticalAlignment: Center);
    }}
    Label #{tid} {{
      Anchor: (Left: {TEXT_L + 88}, Top: {top + 34}, Width: {TEXT_W - 100}, Height: 44);
      Style: (FontName: "Secondary", FontSize: 30, RenderBold: true, TextColor: {color}, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 14);
    }}'''

    ui = f'''$C = "../Common.ui";

// Banque (guards/BankPage), facon fenetre Chapitres. Generee par scripts/npcs/gen_banque_ui.py : voir ce script.
Group {{
  LayoutMode: Middle;
  Background: (TexturePath: "{Q}ChapBackground.png");

  Group #BqPage {{
    Anchor: (Width: 1920, Height: 1080);

    // ---- fond : voile de droite, bande d'encre, lueur, eclats ----
    Group {{ Anchor: (Left: 1003, Top: 0, Width: 917, Height: 1080); Background: #181822(0.5); }}
    Group {{ Anchor: (Left: 499, Top: 0, Width: 504, Height: 1080); Background: (TexturePath: "{Q}ChapSeparator.png"); }}
    Group {{ Anchor: (Left: 480, Top: 25, Width: 488, Height: 1055); Background: (TexturePath: "{Q}ChapGlows.png"); }}
    Group {{ Anchor: (Left: 0, Top: 0, Width: 1920, Height: 1080); Background: (TexturePath: "{Q}ChapShards.png"); }}
    Group {{ Anchor: (Left: {RIGHT_L - 12}, Top: 0, Width: {1920 - RIGHT_L + 12}, Height: 1080); Background: #0b0b12(0.45); }}

    // ---- onglets ----
    Group {{ Anchor: (Left: {RIGHT_L - 8}, Top: {TAB_T - 6}, Width: {RIGHT_W + 16}, Height: {TAB_H + 12}); Background: #0b0b12(0.55); }}
    Group {{ Anchor: (Left: {RIGHT_L - 8}, Top: {TAB_T + TAB_H + 6}, Width: {RIGHT_W + 16}, Height: 8); Background: (TexturePath: "{Q}ChapDivider.png", HorizontalBorder: 24, VerticalBorder: 0); }}
{tabs}

    // ---- replique du banquier ----
    Label #BqQuote {{
      Anchor: (Left: {RIGHT_L}, Top: {QUOTE_T}, Width: {RIGHT_W}, Height: 46);
      Style: (FontSize: 17, RenderBold: true, TextColor: {TAGLINE}, HorizontalAlignment: Center, VerticalAlignment: Center, Wrap: true);
    }}

    // ---- onglet 0 : deposer / retirer ----
    Group #BqPanel0 {{
      Anchor: (Left: {RIGHT_L}, Top: {PANEL_T}, Width: {RIGHT_W}, Height: 740);
{p0}
    }}

    // ---- onglet 1 : change ----
    Group #BqPanel1 {{
      Anchor: (Left: {RIGHT_L}, Top: {PANEL_T}, Width: {RIGHT_W}, Height: 740);
      Visible: false;
{p1}
    }}

    // ---- onglet 2 : pret ----
    Group #BqPanel2 {{
      Anchor: (Left: {RIGHT_L}, Top: {PANEL_T}, Width: {RIGHT_W}, Height: 740);
      Visible: false;
{p2}
    }}

    Group {{
      Anchor: (Left: {RIGHT_L + RIGHT_W - 220}, Top: 960, Width: 220, Height: 44);
      $C.@SecondaryTextButton #BqClose {{
        @Anchor = Anchor(Left: 0, Top: 0, Width: 220);
        Text: "Fermer";
      }}
    }}
    Label {{
      Anchor: (Left: {RIGHT_L + RIGHT_W - 220}, Top: 1010, Width: 220, Height: 24);
      Text: "Échap  ·  Retour";
      Style: (FontSize: 13, RenderBold: true, RenderUppercase: true, TextColor: #77879d, HorizontalAlignment: Center, VerticalAlignment: Center);
    }}

    // ---- fiche : banquier et avoirs ----
    Group {{ Anchor: (Left: {PAGE_L - 14}, Top: 20, Width: {TEXT_W + 30}, Height: 1040); Background: #0b0b12(0.35); }}
    Group {{ Anchor: (Left: {CX - 84}, Top: 30, Width: 168, Height: 168); Background: (TexturePath: "{Q}ChapEmblemFrame.png"); }}
    Group {{ Anchor: (Left: {CX - 54}, Top: 60, Width: 108, Height: 108); Background: (TexturePath: "{Q}ChapEmblemBack.png"); }}
{em_icon}
    Label {{
      Anchor: (Left: {TEXT_L}, Top: 202, Width: {TEXT_W}, Height: 20);
      Text: "Banque d'Eldanior";
      Style: (FontSize: 13, RenderBold: true, RenderUppercase: true, TextColor: #b9a2d8, HorizontalAlignment: Center, VerticalAlignment: Center);
    }}
    Label #BqTitle {{
      Anchor: (Left: {TEXT_L}, Top: 224, Width: {TEXT_W}, Height: 40);
      Style: (FontName: "Secondary", FontSize: 27, RenderBold: true, RenderUppercase: true, TextColor: {GOLD}, HorizontalAlignment: Center, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 15);
    }}
    Label #BqSub {{
      Anchor: (Left: {TEXT_L}, Top: 264, Width: {TEXT_W}, Height: 40);
      Style: (FontSize: 16, RenderBold: true, TextColor: {TAGLINE}, HorizontalAlignment: Center, VerticalAlignment: Center, Wrap: true);
    }}
    Group {{ Anchor: (Left: {PAGE_L}, Top: 312, Width: 418, Height: 8); Background: (TexturePath: "{Q}ChapDivider.png", HorizontalBorder: 24, VerticalBorder: 0); }}
    Label {{
      Anchor: (Left: {TEXT_L}, Top: 330, Width: {TEXT_W}, Height: 26);
      Text: "Vos avoirs";
      Style: (FontName: "Secondary", FontSize: 19, RenderBold: true, RenderUppercase: true, TextColor: {HEAD}, VerticalAlignment: Center);
    }}
{tile("BqPurse", 362, "Dans votre sac", GOLD)}
{tile("BqAccount", 464, "À la banque", GREEN)}
    Label #BqTotal {{
      Anchor: (Left: {TEXT_L}, Top: 564, Width: {TEXT_W}, Height: 30);
      Style: (FontSize: 16, RenderBold: true, TextColor: {BODY}, HorizontalAlignment: Center, VerticalAlignment: Center);
    }}
    Group #BqDebtBox {{
      Anchor: (Left: {TEXT_L}, Top: 602, Width: {TEXT_W}, Height: 92);
      Visible: false;
      Group {{ Anchor: (Left: 0, Top: 0, Width: {TEXT_W}, Height: 92); Background: (TexturePath: "{Q}QHudPatch.png", Border: 16); }}
      Group {{ Anchor: (Left: 4, Top: 14, Width: 3, Height: 64); Background: {RED}; }}
      Label {{
        Anchor: (Left: 20, Top: 10, Width: {TEXT_W - 34}, Height: 20);
        Text: "Dette envers la banque";
        Style: (FontSize: 12, RenderBold: true, RenderUppercase: true, TextColor: {TEXT2}, VerticalAlignment: Center);
      }}
      Label #BqDebt {{
        Anchor: (Left: 20, Top: 30, Width: {TEXT_W - 34}, Height: 34);
        Style: (FontName: "Secondary", FontSize: 24, RenderBold: true, TextColor: {RED}, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 12);
      }}
      Label #BqDebtTime {{
        Anchor: (Left: 20, Top: 64, Width: {TEXT_W - 34}, Height: 20);
        Style: (FontSize: 13, RenderBold: true, TextColor: {BODY}, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 9);
      }}
    }}

    // ---- message de la banque ----
    Group #BqMsgBox {{
      Anchor: (Left: {TEXT_L}, Top: 760, Width: {TEXT_W}, Height: 120);
      Visible: false;
      Group {{ Anchor: (Left: 0, Top: 0, Width: {TEXT_W}, Height: 120); Background: (TexturePath: "{Q}QHudPatch.png", Border: 16); }}
      Group #BqMsgBar {{ Anchor: (Left: 4, Top: 14, Width: 3, Height: 92); Background: {GREEN}; }}
      Label #BqMsg {{
        Anchor: (Left: 20, Top: 8, Width: {TEXT_W - 34}, Height: 104);
        Style: (FontSize: 15, RenderBold: true, TextColor: {GREEN}, VerticalAlignment: Center, Wrap: true);
      }}
    }}
  }}
}}
'''
    check_ui(ui)
    os.makedirs(OUT, exist_ok=True)
    path = os.path.join(OUT, "Banque.ui")
    with open(path, "w", encoding="utf-8") as f:
        f.write(ui)
    print("ecrit", os.path.normpath(path))


if __name__ == "__main__":
    build()
