"""OBSOLETE (2026-11-26) : ClassSelectionScreen, ClassSelection.ui et les cadres ClassFrame* sont supprimes (la statue utilise
ClassChoicePage / accueil.CardsPage). Seuls les emblemes ClassEmblem_* servent encore (Classes/Observateur.ui) : ne relancer
que pour eux.

Fenetre de choix de classe (Bench_Class_System, ClassSelectionScreen) : images et Common/UI/Custom/Classes/ClassSelection.ui.

Meme design que la fenetre de recrutement des aventuriers (Npc/Recruit.ui, scripts/npcs/gen_recruit_ui.py) : une banniere
verticale par classe, cote a cote (6 au plus), reduite a CARD_W de large. Le contour de chaque carte prend la couleur de la
rarete de sa classe : arches et embleme du recrutement recolores pour chaque rarete (l'or devient la couleur de la rarete,
nuances conservees) ; ClassSelectionScreen rend visible la variante de la rarete et colore bordures, separateurs et titres.

Carte : arche du haut (embleme, nom, type et rarete, note), corps (description, 6 bonus de stats, competences passives en
cartes, sorts de rune en cartes avec l'image de la rune, armes maniables), arche du bas (note d'evolution, bouton Choisir).

Usage : python3 scripts/classes/gen_class_ui.py <src/main/resources>
"""
import colorsys, os, sys

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "rune_aura_icons"))
import png  # noqa: E402  (module PNG du projet : RGBA 8 bits)

RES = sys.argv[1]
NPC = os.path.join(RES, "Common", "UI", "Custom", "Npc")
OUT = os.path.join(RES, "Common", "UI", "Custom", "Classes")

# Couleurs de rarete des classes (ClassSelectionScreen.rarityColor)
RARITIES = {"COMMON": "#E0E0E0", "RARE": "#3498DB", "EPIC": "#9B59B6", "UNIQUE": "#E74C3C", "LEGENDARY": "#F1C40F",
            "DIVINE": "#00FFFF"}
GOLD_REF = 232 / 255                                         # luminosite de l'or de reference (#e8b34f)

CARDS, CARD_W = 6, 290                                     # 6 cartes au plus (5 classes de depart : le Mousquetaire est une evolution de l'Archer depuis le 2026-11-16)
SCALE = CARD_W / 439                                         # banniere du recrutement : 439 de large
TOP_H, BOTTOM_H, BODY_H = round(252 * SCALE), round(201 * SCALE), 514
EMBLEM = round(96 * SCALE)
PASSIVES, RUNES = 3, 3
STATS = ["Force", "Vitalité", "Intellect", "Endurance", "Agilité", "Chance"]


def rgb(hex_color):
    return [int(hex_color[i:i + 2], 16) for i in (1, 3, 5)]


def recolor(p, target):
    """Pixel dore (teinte ~40 degres, sature) -> couleur de la rarete, meme luminosite relative ; le reste ne change pas."""
    r, g, b, a = p
    if a < 10:
        return p
    h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
    if not (0.06 <= h <= 0.17 and s >= 0.35 and v >= 0.3):
        return p
    k = v / GOLD_REF
    return [min(255, c * k) for c in target] + [a]


def images():
    os.makedirs(OUT, exist_ok=True)
    for src, dst in (("RecruitFrameTop", "ClassFrameTop"), ("RecruitFrameBottom", "ClassFrameBottom"), ("RecruitEmblem", "ClassEmblem")):
        w, h, img = png.read(os.path.join(NPC, src + "@2x.png"))
        for rarity, color in RARITIES.items():
            target = rgb(color)
            png.write(os.path.join(OUT, f"{dst}_{rarity}@2x.png"), w, h, [[recolor(p, target) for p in row] for row in img])


# ---------------------------------------------------------------- .ui ----------------------------------------------------------------
def variants(prefix, i, image, width, height, top=0):
    """Une image par rarete, superposees (groupes de hauteur nulle) ; ClassSelectionScreen n'en laisse qu'une visible.
    Identifiants sans tiret bas (#CTop0COMMON) : le format .ui n'accepte que lettres et chiffres apres #."""
    return "\n".join(
        f'      Group #{prefix}{i}{r} {{ Anchor: (Height: 0); Visible: false; '
        f'Background: (TexturePath: "{image}_{r}.png", Anchor: (Width: {width}, Height: {height}, Top: {top})); }}'
        for r in RARITIES)


def section(i, key, title):
    line = lambda n: (f'          Group {{ FlexWeight: 1; LayoutMode: Top; Padding: (Top: 7); '
                      f'Group #CLine{i}{key}{n} {{ Anchor: (Height: 1); Background: #c99436(0.55); }} }}')
    return f"""        Group #CHead{i}{key} {{
          Anchor: (Top: 10, Height: 14);
          LayoutMode: Left;
{line("a")}
          Label #CHeadLabel{i}{key} {{ Anchor: (Width: 170); Text: "{title}"; Style: (FontSize: 11, RenderBold: true, RenderUppercase: true, TextColor: #e8b34f, HorizontalAlignment: Center); }}
{line("b")}
        }}"""


def card(i):
    stats = []
    for row in (0, 1):
        boxes = []
        for col in range(3):
            k = row * 3 + col
            boxes.append(f"""            Group {{ FlexWeight: 1; LayoutMode: Top; Background: #132130; Padding: (Top: 3);
              Label #CStat{i}{k} {{ Style: (FontSize: 13, RenderBold: true, TextColor: #f0c868, HorizontalAlignment: Center); }}
              Label {{ Text: "{STATS[k]}"; Style: (FontSize: 9, RenderBold: true, RenderUppercase: true, TextColor: #6f8296, HorizontalAlignment: Center); }}
            }}""")
        stats.append(f"""          Group {{
            Anchor: (Top: {8 if row == 0 else 4}, Height: 32);
            LayoutMode: Left;
{(chr(10) + "            Group { Anchor: (Width: 4); }" + chr(10)).join(boxes)}
          }}""")
    passives = "\n".join(f"""        Group #CPas{i}{k} {{
          Anchor: (Top: 4, Height: 36);
          LayoutMode: Left;
          Background: #101c28;
          Visible: false;
          Group #CPasBar{i}{k} {{ Anchor: (Width: 3); Background: #e8b34f; }}
          Group {{
            FlexWeight: 1;
            LayoutMode: Top;
            Padding: (Horizontal: 7, Vertical: 2);
            Label #CPasName{i}{k} {{ Anchor: (Height: 14); Style: (FontSize: 12, RenderBold: true, TextColor: #f0d58c); }}
            Label #CPasDesc{i}{k} {{ FlexWeight: 1; Style: (FontSize: 10, TextColor: #a9b8c8, Wrap: true); }}
          }}
        }}""" for k in range(PASSIVES))
    runes = "\n".join(f"""        Group #CRune{i}{k} {{
          Anchor: (Top: 4, Height: 42);
          LayoutMode: Left;
          Background: #101c28;
          Padding: (Full: 3);
          Visible: false;
          Group #CRuneFrame{i}{k} {{
            Anchor: (Width: 36, Height: 36);
            Background: #e8b34f;
            Padding: (Full: 2);
            Group {{ Background: #0b141d; LayoutMode: Middle; ItemIcon #CRuneIcon{i}{k} {{ Anchor: (Width: 30, Height: 30); }} }}
          }}
          Group {{ Anchor: (Width: 7); }}
          Group {{
            FlexWeight: 1;
            LayoutMode: Top;
            Group {{
              Anchor: (Height: 15);
              LayoutMode: Left;
              Label #CRuneName{i}{k} {{ FlexWeight: 1; Style: (FontSize: 12, RenderBold: true, TextColor: #e6edf5); }}
              Label #CRuneMeta{i}{k} {{ Anchor: (Width: 92); Style: (FontSize: 9, RenderBold: true, RenderUppercase: true, TextColor: #6f8296, HorizontalAlignment: End); }}
            }}
            Label #CRuneDesc{i}{k} {{ FlexWeight: 1; Style: (FontSize: 10, TextColor: #a9b8c8, Wrap: true); }}
          }}
        }}""" for k in range(RUNES))
    bars = lambda side: (f"      Group {{ Anchor: (Width: 3); Background: #16212f; }}\n"
                         f"      Group #CBar{i}{side} {{ Anchor: (Width: 6); Background: #e8b34f; }}\n"
                         f"      Group {{ Anchor: (Width: 2); Background: #16212f; }}")
    bars_right = (f"      Group {{ Anchor: (Width: 2); Background: #16212f; }}\n"
                  f"      Group #CBar{i}R {{ Anchor: (Width: 6); Background: #e8b34f; }}\n"
                  f"      Group {{ Anchor: (Width: 3); Background: #16212f; }}")
    return f"""  Group #ClassCard{i} {{
    Anchor: (Width: {CARD_W});
    LayoutMode: Top;
    Visible: false;

    // ===== Arche du haut : embleme, nom, type et rarete, note =====
    Group {{
      Anchor: (Height: {TOP_H});
      LayoutMode: Top;
{variants("CTop", i, "ClassFrameTop", CARD_W, TOP_H)}
{variants("CEmblem", i, "ClassEmblem", EMBLEM, EMBLEM, -round(34 * SCALE))}
      Group {{ Anchor: (Height: {round(124 * SCALE)}); }}
      Label #CName{i} {{ Style: (FontSize: 18, RenderBold: true, RenderUppercase: true, TextColor: #d1dae6, Wrap: true, HorizontalAlignment: Center); }}
      Label #CType{i} {{ Anchor: (Top: 6); Style: (FontSize: 12, RenderBold: true, RenderUppercase: true, TextColor: #96a9be, HorizontalAlignment: Center); }}
      Label #CNote{i} {{ Anchor: (Top: 4); Style: (FontSize: 11, RenderBold: true, TextColor: #878e9c, HorizontalAlignment: Center); }}
    }}

    // ===== Corps : bordures a la couleur de la rarete, description, stats, competences, armes =====
    Group {{
      Anchor: (Height: {BODY_H});
      LayoutMode: Left;
{bars("L")}
      Group {{
        FlexWeight: 1;
        LayoutMode: Top;
        Background: #0b141d(0.92);
        Padding: (Horizontal: 12, Top: 6);
        Group #CSep{i} {{ Anchor: (Height: 2); Background: #c99436; }}
        Label #CDesc{i} {{ Anchor: (Top: 8, Height: 72); Style: (FontSize: 11, RenderBold: true, TextColor: #c9b37a, Wrap: true, HorizontalAlignment: Center); }}
{chr(10).join(stats)}
{section(i, "P", "Compétences passives")}
{passives}
{section(i, "R", "Sorts de rune")}
{runes}
        Group {{ FlexWeight: 1; }}
        Group {{
          Anchor: (Height: 38, Bottom: 6);
          LayoutMode: Top;
          Background: #132130;
          Padding: (Top: 4, Horizontal: 6);
          Label {{ Text: "Armes maniables"; Style: (FontSize: 9, RenderBold: true, RenderUppercase: true, TextColor: #6f8296, HorizontalAlignment: Center); }}
          Label #CWeapons{i} {{ Style: (FontSize: 11, RenderBold: true, TextColor: #e6edf5, HorizontalAlignment: Center, Wrap: true); }}
        }}
      }}
{bars_right}
    }}

    // ===== Arche du bas : note d'evolution, bouton =====
    Group {{
      Anchor: (Height: {BOTTOM_H});
      LayoutMode: Top;
{variants("CBottom", i, "ClassFrameBottom", CARD_W, BOTTOM_H)}
      Label #CEvo{i} {{ Anchor: (Top: 6); Style: (FontSize: 11, RenderBold: true, TextColor: #878e9c, HorizontalAlignment: Center); }}
      Group {{
        Anchor: (Top: 8, Height: 40);
        LayoutMode: Center;
        $C.@TextButton #CBtn{i} {{
          @Text = "Choisir";
          @Anchor = Anchor(MinWidth: 190);
        }}
      }}
    }}
  }}"""


def ui():
    cards = ("\n\n  Group { Anchor: (Width: 12); }\n\n").join(card(i) for i in range(CARDS))
    text = f"""$C = "../Common.ui";

// Fenetre de choix de classe (ClassSelectionScreen) : meme design que la fenetre de recrutement des aventuriers
// (Npc/Recruit.ui), une banniere par classe cote a cote, contour a la couleur de la rarete de la classe.
// GENERE par scripts/classes/gen_class_ui.py : modifier le script, pas ce fichier.

SceneBlur {{}}

Group {{
  LayoutMode: Middle;
  Background: #05080d(0.55);

  Group {{
    LayoutMode: Top;

    Label #Title {{
      Anchor: (Height: 34);
      Text: "Choix de classe";
      Style: (FontSize: 24, RenderBold: true, RenderUppercase: true, TextColor: #e8b34f, HorizontalAlignment: Center);
    }}

    Group {{ Anchor: (Height: 6); }}

    Group {{
      LayoutMode: Center;

{cards}
    }}

    Group {{ Anchor: (Height: 8); }}

    Group #RerollRow {{
      Visible: false;
      Anchor: (Height: 40);
      LayoutMode: Center;

      Button #RerollButton {{
        Anchor: (Width: 260, Height: 36);
        Style: (Default: (Background: #132130), Hovered: (Background: #1d3147), Pressed: (Background: #0b141d));

        Label #RerollLabel {{
          Style: (HorizontalAlignment: Center, VerticalAlignment: Center, FontSize: 14, TextColor: #e8b34f, RenderBold: true, RenderUppercase: true);
          Text: "Relancer (2 restants)";
        }}
      }}
    }}

    Label {{
      Anchor: (Top: 4);
      Text: "Echap : fermer";
      Style: (FontSize: 12, TextColor: #5a6a7a, HorizontalAlignment: Center);
    }}
  }}
}}
"""
    with open(os.path.join(OUT, "ClassSelection.ui"), "w", encoding="utf-8") as f:
        f.write(text)


if __name__ == "__main__":
    images()
    ui()
    print("ok :", len(RARITIES) * 3, "images,", CARDS, "cartes")
