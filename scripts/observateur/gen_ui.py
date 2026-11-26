#!/usr/bin/env python3
"""
Interfaces de l'Observateur, refaites le 2026-11-17 pour suivre le style du mod :
  Hud/ArkiesDomini.ui         carte de l'entite visee (hud/ArkiesDominiHud.java) : style des cartes de quete du HUD
                              (Quest/QuestHud.ui : contour colore, fond QHudPatch, blason de rang QHudRank<R>), a droite SOUS le
                              panneau de groupe (Party/PartyHud.ui : 210 x 310 en haut a droite) pour ne jamais le chevaucher.
  Classes/SansVisage.ui       fenetre de la rune Sans Visage (classes/observateur/SansVisagePage.java)
  System/SystemPage.ui        #TabObservateur : Cour de l'Observateur, panneau du menu Systeme (classes/observateur/ObservateurCourTab.java)
  Classes/Observateur.ui      proposition recue par le joueur nomme (classes/observateur/ObservateurOfferPage.java)
Les deux fenetres reprennent le menu Systeme (System/SystemPage.ui) : cadre dore @GoldContainer, titres de section a carre
dore, rangees GoldPanelPatch a liseret, cases CharacterPanelButton, boutons standard du jeu ($C.@TextButton...).
Modeles 3D : ItemPreviewComponent (propriete ItemId), portraits de scripts/observateur/gen_visages.py.
IDs apres # : lettres et chiffres seulement (sinon le client refuse tout le document).

Usage : python3 scripts/observateur/gen_ui.py
"""
import os

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))
UI = os.path.join(ROOT, 'src', 'main', 'resources', 'Common', 'UI', 'Custom')

# palette du menu Systeme et des cartes de quete
GOLD, GOLD_T, GOLD_NAME, CAPTION, VALUE, BODY, MUTED = '#c99436', '#e8b34f', '#f0d58c', '#878e9c', '#d1dae6', '#a9b8c8', '#77879d'
QUEST_GOLD, QUEST_TEXT = '#f2c45e', '#b8c2d0'
RANKS = ['F', 'E', 'D', 'C', 'B', 'A', 'S', 'SS', 'EX']
STAT_ICONS = ['Force', 'Vitalite', 'Intelligence', 'Endurance', 'Agilite', 'Chance']
STAT_TIPS = ['Force', 'Vitalité', 'Intelligence', 'Endurance', 'Agilité', 'Chance']


def ind(block, n):
    pad = ' ' * n
    return '\n'.join(pad + line if line.strip() else line for line in block.split('\n'))


def lbl(id_=None, text='', size=12, color=VALUE, bold=False, height=None, top=None, width=None, flex=False, extra='',
        visible=None):
    a = [f'Height: {height}'] if height else []
    if top is not None:
        a.append(f'Top: {top}')
    if width:
        a.append(f'Width: {width}')
    parts = [f'Anchor: ({", ".join(a)});'] if a else []
    if flex:
        parts.append('FlexWeight: 1;')
    if visible is not None:
        parts.append(f'Visible: {visible};')
    parts.append(f'Text: "{text}";')
    parts.append(f'Style: (FontSize: {size}, TextColor: {color}, RenderBold: {"true" if bold else "false"}{extra});')
    return f'Label{" #" + id_ if id_ else ""} {{ {" ".join(parts)} }}'


# =====================================================================================
# Kit des fenetres (menu Systeme)
# =====================================================================================

GOLD_CONTAINER = '''// Cadre des fenetres du menu Systeme (System/SystemPage.ui, @GoldContainer), textures du dossier System.
@GoldContainer = Group {
  Group #FrameTitle {
    Anchor: (Height: 38, Top: 0);
    Background: (TexturePath: "../System/GoldHeader.png", HorizontalBorder: 50, VerticalBorder: 0);
  }
  Group #FrameBody {
    Anchor: (Top: 38);
    Background: (TexturePath: "../System/GoldPatch.png", Border: 23);
  }
  Group #FrameDecoTop {
    Anchor: (Width: 236, Height: 11, Top: -12);
    Background: "../System/GoldDecorationTop.png";
  }
  Group #Title {
    Anchor: (Height: 38, Top: 0);
    Padding: (Top: 7);
  }
  Group #Content {
    LayoutMode: Top;
    Anchor: (Top: 38);
    Padding: (Full: 17);
  }
  Group #FrameDecoBottom {
    Anchor: (Width: 236, Height: 11, Bottom: -6);
    Background: "../System/GoldDecorationBottom.png";
  }
};'''


def window(frame_id, title, width, height, content, comment):
    return f'''$C = "../Common.ui";

$C.@PageOverlay {{}}

// {comment}
// Genere par scripts/observateur/gen_ui.py : modifier le script, pas ce fichier.
{GOLD_CONTAINER}

Group {{
  LayoutMode: Center;

  Group {{
    Anchor: (Height: {height});
    LayoutMode: Left;

    @GoldContainer #{frame_id} {{
      Anchor: (Width: {width}, Height: {height});

      #Title {{
        Label {{
          Text: "{title}";
          Style: (...$C.@TitleStyle, HorizontalAlignment: Center);
          Padding: (Horizontal: 19);
        }}
      }}

      #Content {{
        LayoutMode: Top;
{ind(content, 8)}
      }}
    }}
  }}
}}
'''


def section(title, top=10, id_=None):
    gid = f' #{id_}' if id_ else ''
    return f'''Group{gid} {{
  Anchor: (Top: {top}, Height: 18);
  LayoutMode: Left;
  Group {{ Anchor: (Width: 6, Height: 6, Top: 6); Background: {GOLD}; }}
  Label {{ Anchor: (Left: 6); FlexWeight: 1; Text: "{title}"; Style: (TextColor: {GOLD_T}, FontSize: 13, RenderBold: true, RenderUppercase: true); }}
}}
Group {{ Anchor: (Top: 4, Height: 2); Background: {GOLD}(0.18); }}'''


def divider(top=8):
    return f'Group {{ Anchor: (Top: {top}, Height: 2); Background: {GOLD}(0.18); }}'


def badge(inner, size=46):
    return f'''Group {{
  Anchor: (Width: {size}, Height: {size});
  Background: "../System/CharacterPanelButton.png";
  LayoutMode: CenterMiddle;
{ind(inner, 2)}
}}'''


def btn(kind, id_, text, width=None, visible=None):
    """Bouton standard du jeu : TextButton, SecondaryTextButton, CancelTextButton, SmallSecondaryTextButton."""
    a = f'\n  @Anchor = Anchor(Width: {width});' if width else ''
    v = f'\n  Visible: {visible};' if visible is not None else ''
    return f'''$C.@{kind} #{id_} {{
  @Text = "{text}";{a}{v}
}}'''


def field(id_, placeholder, width=None, height=30):
    w = f'Width: {width}, ' if width else ''
    flex = '' if width else '\n  FlexWeight: 1;'
    return f'''$C.@TextField #{id_} {{
  Anchor: ({w}Height: {height});{flex}
  PlaceholderText: "{placeholder}";
}}'''


def gold_row(id_, inner, height, top=6, visible='false', bar_id=None, bar=GOLD):
    bid = f' #{bar_id}' if bar_id else ''
    return f'''Group #{id_} {{
  Anchor: (Top: {top}, Height: {height});
  LayoutMode: Left;
  Background: (TexturePath: "../System/GoldPanelPatch.png", Border: 4);
  Visible: {visible};
  Group{bid} {{ Anchor: (Width: 4); Background: {bar}; }}
{ind(inner, 2)}
}}'''


def tab(id_, text, on_id, width=170):
    return f'''Group {{
  Anchor: (Width: {width});
  LayoutMode: Top;
  TextButton #{id_} {{
    Text: "{text}";
    Anchor: (Height: 31);
    Style: (Default: (LabelStyle: (FontSize: 13, TextColor: #c9d1dc, HorizontalAlignment: Center, VerticalAlignment: Center, RenderBold: true, RenderUppercase: true, LetterSpacing: 1)), Hovered: (LabelStyle: (FontSize: 13, TextColor: #FFD700, HorizontalAlignment: Center, VerticalAlignment: Center, RenderBold: true, RenderUppercase: true, LetterSpacing: 1)));
    Background: #141a24(0.85);
  }}
  Group #{on_id} {{ Anchor: (Height: 3); Background: {GOLD_T}; Visible: false; }}
}}'''


def pager(prefix):
    return f'''Group {{
  Anchor: (Top: 8, Height: 32);
  LayoutMode: Left;
{ind(btn("SmallSecondaryTextButton", prefix + "Prev", "< Précédent", 140), 2)}
  Group {{ FlexWeight: 1; }}
  {lbl(prefix + "Page", "", 13, "#FFD700", True, extra=", HorizontalAlignment: Center, VerticalAlignment: Center")}
  Group {{ FlexWeight: 1; }}
{ind(btn("SmallSecondaryTextButton", prefix + "Next", "Suivant >", 140), 2)}
}}'''


# =====================================================================================
# HUD : carte de l'entite visee (style carte de quete)
# =====================================================================================

HUD_W, HUD_ROWS = 320, 6


def hud():
    badges = '\n'.join(
        f'          Group #AdBadge{r} {{ Anchor: (Left: 0, Top: 0, Width: 58, Height: 58); Background: (TexturePath: "../Quest/QHudRank{r}.png"); Visible: false; }}'
        for r in RANKS)
    chips = []
    for row in range(2):
        cells = []
        for col in range(3):
            i = row * 3 + col
            cells.append(f'''      Group {{
        FlexWeight: 1;
        LayoutMode: Left;
        Group {{ Anchor: (Width: 20, Height: 20); Background: "../System/CharacterPanelStatIconBackground.png";
          Group {{ Background: "../System/StatIcon{STAT_ICONS[i]}.png"; }}
        }}
        {lbl("AdS" + str(i), "", 12, VALUE, True, extra=", VerticalAlignment: Center")[:-1]} Anchor: (Left: 6); FlexWeight: 1; }}
      }}''')
        chips.append(f'''    Group {{
      Anchor: (Top: {0 if row == 0 else 4}, Height: 20);
      LayoutMode: Left;
{chr(10).join(cells)}
    }}''')
    rows = '\n'.join(f'''    Group #AdR{i} {{
      Anchor: (Height: 18);
      LayoutMode: Left;
      Visible: false;
      Group {{ Anchor: (Width: 5, Height: 5, Top: 7); Background: {GOLD}; }}
      {lbl("AdL" + str(i), "", 12, QUEST_TEXT, True, extra=", VerticalAlignment: Center")[:-1]} Anchor: (Left: 8); FlexWeight: 1; }}
    }}''' for i in range(HUD_ROWS))
    return f'''// Carte de l'entite visee pendant la rune Arkies Domini (hud/ArkiesDominiHud.java), style des cartes de quete du HUD.
// Genere par scripts/observateur/gen_ui.py : modifier le script, pas ce fichier.
// Place : a droite, SOUS le panneau de groupe (Party/PartyHud.ui, 210 x 310 en haut a droite, bas a ~320 px).
// Contour #AdPanel et type #AdKind colores par le serveur (joueur, PNJ, creature) ; blason = rang de menace (F a EX).
// La zone (Height) n'est qu'une reservation : LayoutMode Top pour que #AdPanel prenne la hauteur de son contenu (sans lui,
// le contour colore s'etirait sur toute la zone : « fond rose trop grand », 2026-11-17).
Group {{
  Anchor: (Right: 10, Top: 334, Width: {HUD_W}, Height: 420);
  LayoutMode: Top;

  Group #AdPanel {{
    Visible: false;
    LayoutMode: Top;
    Padding: (Full: 2);
    Background: {QUEST_GOLD};

    Group {{
      LayoutMode: Top;
      Background: (TexturePath: "../Quest/QHudPatch.png", Border: 16);
      Padding: (Left: 8, Right: 12, Top: 8, Bottom: 8);

      // ---- en-tete : blason du rang, type, nom
      Group {{
        Anchor: (Height: 58);
        LayoutMode: Left;
        Group {{
          Anchor: (Width: 58, Height: 58);
{badges}
          Label #AdRankLetter {{
            Anchor: (Left: 0, Top: 0, Width: 58, Height: 58);
            Text: "?";
            Style: (FontName: "Secondary", FontSize: 24, RenderBold: true, TextColor: #ffe98a, HorizontalAlignment: Center, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 14);
          }}
        }}
        Group {{ Anchor: (Width: 2); }}
        Group #AdRankLine {{ Anchor: (Width: 2); Background: {QUEST_GOLD}; }}
        Group {{
          FlexWeight: 1;
          LayoutMode: Top;
          Padding: (Left: 10, Top: 4);
          Label #AdKind {{
            Anchor: (Height: 16);
            Style: (FontSize: 10, RenderBold: true, RenderUppercase: true, TextColor: {QUEST_GOLD}, VerticalAlignment: Center, LetterSpacing: 1);
          }}
          Label #AdTitle {{
            Anchor: (Height: 22);
            Style: (FontSize: 16, RenderBold: true, TextColor: {QUEST_GOLD}, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 11);
          }}
          Label #AdSub {{
            Anchor: (Height: 16);
            Style: (FontSize: 11, RenderBold: true, TextColor: {MUTED}, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 9);
          }}
        }}
      }}
      Group {{ Anchor: (Top: 4, Height: 1); Background: #ffffff(0.14); }}

      // ---- modele 3D a gauche ; vie, mana, classe, rang a droite
      Group {{
        Anchor: (Top: 6, Height: 112);
        LayoutMode: Left;
        Group {{
          Anchor: (Width: 96, Height: 112);
          LayoutMode: Top;
          Padding: (Full: 1);
          Background: #ffffff(0.12);
          Group {{
            FlexWeight: 1;
            LayoutMode: Top;
            Background: #0b0e14(0.88);
            ItemPreviewComponent #AdModel {{
              Anchor: (Height: 110);
              Visible: false;
            }}
            Group #AdNoModel {{
              Anchor: (Height: 110);
              LayoutMode: Middle;
              {lbl(None, "?", 40, "#3a4256", True, extra=", HorizontalAlignment: Center, VerticalAlignment: Center")}
            }}
          }}
        }}
        Group {{
          FlexWeight: 1;
          LayoutMode: Top;
          Padding: (Left: 10);
          Group {{ Anchor: (Height: 14); LayoutMode: Left;
            {lbl(None, "Vie", 10, "#e07070", True, flex=True, extra=", RenderUppercase: true")}
            {lbl("AdHpText", "", 10, "#f0d0d0", True, flex=True, extra=", HorizontalAlignment: End")}
          }}
          Group {{ Anchor: (Top: 2, Height: 8); Background: #333a47;
            ProgressBar #AdHp {{ BarTexturePath: "../Party/HPFill.png"; Value: 1.0; }}
          }}
          Group #AdManaRow {{
            Anchor: (Top: 5, Height: 24);
            LayoutMode: Top;
            Group {{ Anchor: (Height: 14); LayoutMode: Left;
              {lbl(None, "Mana", 10, "#7aa8e8", True, flex=True, extra=", RenderUppercase: true")}
              {lbl("AdManaText", "", 10, "#d0dcf0", True, flex=True, extra=", HorizontalAlignment: End")}
            }}
            Group {{ Anchor: (Top: 2, Height: 8); Background: #1a2030;
              ProgressBar #AdMana {{ BarTexturePath: "../Status/MPFill.png"; Value: 1.0; }}
            }}
          }}
          Group {{ Anchor: (Top: 7, Height: 1); Background: #ffffff(0.10); }}
          {lbl("AdClass", "", 12, GOLD_NAME, True, height=18, top=5, extra=", ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 9")}
          {lbl("AdRank", "", 11, BODY, False, height=16, extra=", ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 9")}
        }}
      }}

      // ---- caracteristiques (joueurs) : icones du menu Systeme
      Group #AdStats {{
        Anchor: (Top: 8);
        LayoutMode: Top;
{chr(10).join(chips)}
      }}

      Group {{ Anchor: (Top: 7, Height: 1); Background: #ffffff(0.14); }}

      // ---- details (lignes masquees si vides)
      Group {{
        Anchor: (Top: 4);
        LayoutMode: Top;
{rows}
      }}

      // ---- pied : distance, mur
      Group {{
        Anchor: (Top: 4, Height: 16);
        LayoutMode: Left;
        {lbl("AdDist", "", 11, MUTED, True, flex=True, extra=", VerticalAlignment: Center")}
        {lbl("AdWall", "", 10, "#e05050", True, width=120, extra=", RenderUppercase: true, HorizontalAlignment: End, VerticalAlignment: Center")}
      }}
    }}
  }}
}}
'''


# =====================================================================================
# Fenetre Sans Visage
# =====================================================================================

TILES, COLS = 15, 3


def sans_visage():
    tabs = []
    for i in range(4):
        tabs.append(tab(f'SvTab{i}', '', f'SvTabOn{i}', 150))
        if i < 3:
            tabs.append('Group { Anchor: (Width: 6); }')
    rows = []
    for r in range(TILES // COLS):
        cells = []
        for c in range(COLS):
            i = r * COLS + c
            cells.append(f'''Button #SvT{i} {{
  FlexWeight: 1;
  LayoutMode: Left;
  Visible: false;
  Style: (Default: (Background: PatchStyle(TexturePath: "../System/GoldPanelPatch.png", Border: 4)), Hovered: (Background: PatchStyle(TexturePath: "../System/GoldPanelHover.png", Border: 4)), Sounds: $C.@ButtonSounds);
  Group #SvTBar{i} {{ Anchor: (Width: 4); Background: {GOLD}(0.45); }}
  Group {{
    FlexWeight: 1;
    LayoutMode: Top;
    Padding: (Left: 10, Right: 6, Top: 6);
    {lbl("SvTL" + str(i), "", 13, GOLD_NAME, True, height=18, extra=", ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 9")}
    {lbl("SvTS" + str(i), "", 11, BODY, False, height=15, extra=", ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 8")}
  }}
}}''')
            if c < COLS - 1:
                cells.append('Group { Anchor: (Width: 6); }')
        rows.append(f'''Group {{
  Anchor: (Top: 6, Height: 50);
  LayoutMode: Left;
{ind(chr(10).join(cells), 2)}
}}''')

    left = f'''Group {{
  Anchor: (Width: 320);
  LayoutMode: Top;
{ind(section("Aperçu", 0), 2)}
  Group {{
    Anchor: (Top: 6, Height: 272);
    LayoutMode: Top;
    Padding: (Full: 1);
    Background: #ffffff(0.12);
    Group {{
      FlexWeight: 1;
      LayoutMode: Top;
      Background: #0b0e14(0.88);
      ItemPreviewComponent #SvPreview {{
        Anchor: (Height: 238);
        Visible: false;
      }}
      Group #SvNoPreview {{
        Anchor: (Height: 238);
        LayoutMode: Middle;
        {lbl(None, "?", 64, "#3a4256", True, extra=", HorizontalAlignment: Center, VerticalAlignment: Center")}
      }}
      {lbl("SvPreviewName", "", 14, GOLD_NAME, True, height=30, extra=", HorizontalAlignment: Center, VerticalAlignment: Center")}
    }}
  }}
  Group {{ Anchor: (Height: 8); }}
{ind(btn("TextButton", "SvWear", "Choisissez un visage"), 2)}
  Group {{
    Anchor: (Top: 6, Height: 32);
    LayoutMode: Left;
{ind(btn("SmallSecondaryTextButton", "SvRandom", "Au hasard", 140), 4)}
    Group {{ FlexWeight: 1; }}
{ind(btn("SmallSecondaryTextButton", "SvReveal", "Mon vrai visage", 170), 4)}
  }}
{ind(section("Nom d'emprunt", 14), 2)}
  Group {{
    Anchor: (Top: 8, Height: 32);
    LayoutMode: Left;
{ind(field("SvName", "Nom au-dessus de vous", height=32), 4)}
    Group {{ Anchor: (Width: 6); }}
{ind(btn("SmallSecondaryTextButton", "SvNameOk", "OK", 60), 4)}
    Group {{ Anchor: (Width: 4); }}
{ind(btn("SmallSecondaryTextButton", "SvNameOff", "Retirer", 90), 4)}
  }}
{ind(section("Classe apparente", 14), 2)}
  Group {{
    Anchor: (Top: 8, Height: 32);
    LayoutMode: Left;
    $C.@DropdownBox #SvClass {{
      Anchor: (Width: 160, Height: 30);
    }}
    Group {{ Anchor: (Width: 6); }}
{ind(btn("SmallSecondaryTextButton", "SvClassOk", "OK", 60), 4)}
    Group {{ Anchor: (Width: 4); }}
{ind(btn("SmallSecondaryTextButton", "SvClassOff", "Retirer", 90), 4)}
  }}
}}'''

    right = f'''Group {{
  FlexWeight: 1;
  LayoutMode: Top;
  Group {{
    Anchor: (Height: 34);
    LayoutMode: Left;
    Group {{ FlexWeight: 1; }}
{ind(chr(10).join(tabs), 4)}
    Group {{ FlexWeight: 1; }}
  }}
  Group {{
    Anchor: (Top: 10, Height: 32);
    LayoutMode: Left;
{ind(field("SvSearch", "Rechercher : ours, kweebec, squelette...", height=32), 4)}
    Group {{ Anchor: (Width: 6); }}
{ind(btn("SmallSecondaryTextButton", "SvSearchBtn", "Chercher", 120), 4)}
  }}
  {lbl("SvCount", "", 12, CAPTION, False, height=20, top=6, extra=", VerticalAlignment: Center")}
{ind(chr(10).join(rows), 2)}
{ind(pager("Sv"), 2)}
}}'''

    content = f'''// ---- etat actuel : case de la rune, visage / nom / classe portes
Group {{
  Anchor: (Height: 54);
  LayoutMode: Left;
{ind(badge('ItemIcon #SvIcon { Anchor: (Width: 38, Height: 38); }', 50), 2)}
  Group {{
    FlexWeight: 1;
    LayoutMode: Top;
    Padding: (Left: 12, Top: 4);
    {lbl("SvState", "", 15, GOLD_NAME, True, height=22, extra=", ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 10")}
    {lbl("SvState2", "", 12, BODY, False, height=18, extra=", ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 9")}
  }}
}}
{divider(10)}
Group {{
  Anchor: (Top: 12);
  FlexWeight: 1;
  LayoutMode: Left;
{ind(left, 2)}
  Group {{ Anchor: (Width: 18); }}
  Group {{ Anchor: (Width: 2); Background: {GOLD}(0.18); }}
  Group {{ Anchor: (Width: 18); }}
{ind(right, 2)}
}}
{divider(8)}
Group {{
  Anchor: (Top: 8, Height: 44);
  LayoutMode: Left;
  {lbl("SvHint", "", 12, CAPTION, False, flex=True, extra=", VerticalAlignment: Center, Wrap: true")}
  Group {{ Anchor: (Width: 10); }}
{ind(btn("SecondaryTextButton", "SvCour", "Ma charge", 170), 2)}
  Group {{ Anchor: (Width: 8); }}
{ind(btn("SecondaryTextButton", "SvClose", "Fermer", 130), 2)}
}}'''
    return window('SvFrame', 'Sans Visage', 1080, 780, content,
                  "Fenetre de la rune Sans Visage de l'Observateur (classes/observateur/SansVisagePage), style du menu Systeme.")


# =====================================================================================
# Cour de l'Observateur
# =====================================================================================

PICK_ROWS = 6


def cour():
    picks = '\n'.join(gold_row(f'OcP{i}', f'''Group {{
  FlexWeight: 1;
  LayoutMode: Top;
  Padding: (Horizontal: 12, Vertical: 5);
  {lbl("OcPL" + str(i), "", 14, GOLD_NAME, True, height=19)}
  {lbl("OcPS" + str(i), "", 11, BODY, False, height=15)}
}}
Group {{
  Anchor: (Width: 136);
  LayoutMode: Middle;
  Padding: (Right: 8);
{ind(btn("SmallSecondaryTextButton", "OcPB" + str(i), "Proposer", 124), 2)}
}}''', 48, top=6) for i in range(PICK_ROWS))

    holder = f'''Group #OcHolderCard {{
  LayoutMode: Top;
  Visible: false;
{ind(section("Votre Observateur", 12), 2)}
{ind(gold_row("OcHolderRow", f"""Group {{
  Anchor: (Width: 78);
  LayoutMode: Middle;
{ind(badge(lbl("OcHolderInitial", "", 26, GOLD_T, True, extra=", HorizontalAlignment: Center, VerticalAlignment: Center"), 56), 2)}
}}
Group {{
  FlexWeight: 1;
  LayoutMode: Top;
  Padding: (Right: 10, Vertical: 8);
  Group {{
    Anchor: (Height: 24);
    LayoutMode: Left;
    {lbl("OcHolderName", "", 17, GOLD_NAME, True, flex=True, extra=", ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 11")}
    {lbl("OcHolderState", "", 11, "#7ed47e", True, width=110, extra=", RenderUppercase: true, HorizontalAlignment: End, VerticalAlignment: Center")}
  }}
  {lbl("OcHolderInfo0", "", 12, BODY, False, height=17, top=4)}
  {lbl("OcHolderInfo1", "", 12, BODY, False, height=17)}
  {lbl("OcHolderInfo2", "", 12, BODY, False, height=17)}
}}""", 104, top=8, visible="true", bar_id="OcHolderBar"), 2)}
  Group {{
    Anchor: (Top: 10, Height: 44);
    LayoutMode: Left;
    {lbl("OcHolderWarn", "", 12, CAPTION, False, flex=True, extra=", VerticalAlignment: Center, Wrap: true")}
    Group {{ Anchor: (Width: 10); }}
{ind(btn("SecondaryTextButton", "OcRevoke", "Révoquer", 150), 4)}
    Group {{ Anchor: (Width: 8); }}
{ind(btn("CancelTextButton", "OcExecute", "Exécuter", 220), 4)}
  }}
}}'''

    pick = f'''Group #OcPickCard {{
  LayoutMode: Top;
  Visible: false;
{ind(section("Nommer un Observateur", 12), 2)}
  {lbl("OcPickInfo", "", 12, CAPTION, False, height=34, top=6, extra=", Wrap: true")}
{ind(picks, 2)}
{ind(pager("Oc"), 2)}
}}'''

    selfc = f'''Group #OcSelfCard {{
  LayoutMode: Top;
  Visible: false;
{ind(section("Votre serment", 12), 2)}
{ind(gold_row("OcSelfRow", f"""Group {{
  FlexWeight: 1;
  LayoutMode: Top;
  Padding: (Horizontal: 12, Vertical: 8);
  {lbl("OcSelfTitle", "", 15, GOLD_NAME, True, height=20)}
  {lbl("OcSelfInfo", "", 12, BODY, False, height=34, top=4, extra=", Wrap: true")}
}}""", 70, top=8, visible="true"), 2)}
{ind(section("Nom d'emprunt", 14), 2)}
  Group {{
    Anchor: (Top: 8, Height: 44);
    LayoutMode: Left;
{ind(field("OcAlias", "Nom affiché au-dessus de vous", height=36), 4)}
    Group {{ Anchor: (Width: 8); }}
{ind(btn("TextButton", "OcAliasOk", "Prendre ce nom", 190), 4)}
    Group {{ Anchor: (Width: 8); }}
{ind(btn("SecondaryTextButton", "OcAliasOff", "Vrai nom", 140), 4)}
  }}
  Group {{
    Anchor: (Top: 14, Height: 44);
    LayoutMode: Left;
    {lbl("OcRenounceWarn", "", 12, CAPTION, False, flex=True, extra=", VerticalAlignment: Center, Wrap: true")}
    Group {{ Anchor: (Width: 10); }}
{ind(btn("CancelTextButton", "OcRenounce", "Rendre mon titre", 260), 4)}
  }}
}}'''

    content = f'''// ---- en-tete : case, trone, role ; choix du trone pour un admin
Group {{
  Anchor: (Height: 54);
  LayoutMode: Left;
{ind(badge('ItemIcon #OcIcon { Anchor: (Width: 38, Height: 38); }', 50), 2)}
  Group {{
    FlexWeight: 1;
    LayoutMode: Top;
    Padding: (Left: 12, Top: 4);
    {lbl("OcSub", "", 16, GOLD_NAME, True, height=22, extra=", ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 11")}
    {lbl("OcLead", "", 12, CAPTION, False, height=18)}
  }}
  Group #OcAdminRow {{
    Anchor: (Width: 300);
    LayoutMode: Left;
    Visible: false;
    Padding: (Top: 11);
{ind(btn("SmallSecondaryTextButton", "OcThroneRoi", "Roi", 146), 4)}
    Group {{ Anchor: (Width: 8); }}
{ind(btn("SmallSecondaryTextButton", "OcThroneEmp", "Empereur", 146), 4)}
  }}
}}
{divider(10)}
{holder}
{pick}
{selfc}
{divider(14)}
Group {{
  Anchor: (Top: 8, Height: 44);
  LayoutMode: Top;
  {lbl("OcStatus", "", 13, GOLD_T, True, height=20)}
  {lbl("OcOther", "", 11, CAPTION, False, height=18)}
}}'''
    # 2026-11-19 (demande de l'utilisateur) : panneau du menu Systeme, a droite des onglets, plus une fenetre a part
    return f'''{TAB_BEGIN}
Group #TabObservateur {{
  LayoutMode: Top;
  Visible: false;
  Padding: (Horizontal: 10, Top: 6);

  // Cour de l'Observateur (classes/observateur/ObservateurCourTab) : souverain et Observateur.
  // Genere par scripts/observateur/gen_ui.py (cour) : modifier le script, pas ce bloc.
{ind(content, 2)}
}}
{TAB_END}'''


TAB_BEGIN = '// >>> Observateur (scripts/observateur/gen_ui.py)'
TAB_END = '// <<< Observateur'


def inject_cour(block):
    """Remplace le bloc genere entre les marqueurs de System/SystemPage.ui (insere avant #TabWiki la premiere fois)."""
    path = os.path.join(UI, 'System', 'SystemPage.ui')
    with open(path, encoding='utf-8') as fh:
        text = fh.read()
    indent = ' ' * 20
    block = ind(block, 20)
    if TAB_BEGIN in text:
        a = text.index(TAB_BEGIN)
        a = text.rindex('\n', 0, a) + 1
        b = text.index(TAB_END) + len(TAB_END)
        text = text[:a] + block + text[b:]
    else:
        anchor = indent + 'Group #TabWiki {'
        if anchor not in text:
            raise SystemExit('SystemPage.ui : #TabWiki introuvable')
        text = text.replace(anchor, block + '\n\n' + anchor, 1)
    with open(path, 'w', encoding='utf-8') as fh:
        fh.write(text)
    print('injecte dans', os.path.relpath(path, ROOT))


# =====================================================================================
# Proposition du souverain (ObservateurOfferPage)
# =====================================================================================

def offre():
    content = f'''Group {{
  Anchor: (Height: 54);
  LayoutMode: Left;
{ind(badge('Group { Anchor: (Width: 40, Height: 40); Background: "ClassEmblem_LEGENDARY.png"; }', 50), 2)}
  Group {{
    FlexWeight: 1;
    LayoutMode: Top;
    Padding: (Left: 12, Top: 4);
    {lbl("ArkSubtitle", "", 16, GOLD_NAME, True, height=22)}
    {lbl(None, "Classe unique · Légendaire", 12, CAPTION, False, height=18)}
  }}
}}
{divider(10)}
{lbl("ArkText", "", 14, VALUE, False, top=14, extra=", Wrap: true")}
{gold_row("ArkWarnRow", f"""Group {{
  FlexWeight: 1;
  LayoutMode: Middle;
  Padding: (Horizontal: 12);
  {lbl("ArkWarn", "", 13, GOLD_NAME, True, extra=", Wrap: true")}
}}""", 64, top=14, visible="true")}
{lbl("ArkKeep", "", 12, CAPTION, False, top=10, extra=", Wrap: true, HorizontalAlignment: Center")}
Group {{ FlexWeight: 1; }}
{divider(6)}
Group {{
  Anchor: (Top: 10, Height: 44);
  LayoutMode: Left;
{ind(btn("SecondaryTextButton", "ArkCancel", "Décliner", 200), 2)}
  Group {{ FlexWeight: 1; }}
{ind(btn("TextButton", "ArkAccept", "Servir le souverain", 260), 2)}
}}'''
    return window('ArkFrame', 'Observateur', 620, 470, content,
                  "Proposition d'Observateur (classes/observateur/ObservateurOfferPage), envoyee depuis la Cour ; style du menu Systeme.")


def write(rel, content):
    path = os.path.join(UI, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8') as fh:
        fh.write(content)
    print('ecrit', os.path.relpath(path, ROOT))


if __name__ == '__main__':
    write('Hud/ArkiesDomini.ui', hud())
    write('Classes/SansVisage.ui', sans_visage())
    inject_cour(cour())
    write('Classes/Observateur.ui', offre())
