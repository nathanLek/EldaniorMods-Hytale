"""Briques communes des fenetres « deux panneaux » facon etabli / enclume du jeu (boutique des PNJ, editeur des prix).

Panneaux construits a la main avec les textures des gabarits vanilla de Common.ui (@Container : en-tete ContainerHeaderNoRunes,
corps ContainerPatch ; @DecoratedContainer : en-tete ContainerHeader + ornements haut/bas) : les gabarits eux-memes portent des
identifiants fixes (#Title, #Content, #CloseButton) qui seraient en double avec deux panneaux dans la meme page.
Chemins des textures relatifs a un .ui place dans Common/UI/Custom/<Dossier>/.
"""

TITLE_H = 38
PANEL_BG = '(TexturePath: "../Common/ContainerPanelPatch.png", Border: 4)'
SLOT, SLOT_HOVER, SLOT_PRESS = "#1b2836", "#26384b", "#2f4660"
MUTED, LABEL, GOLD, TEXT = "#878e9c", "#96a9be", "#e8a93b", "#d3d6db"
OK, KO = "#3fbf6a", "#cc3361"


def indent(text, n):
    pad = " " * n
    return "\n".join(pad + line if line.strip() else line for line in text.split("\n"))


def container(gid, width, height, title, body, decorated=False, body_pad="(Full: 17)"):
    """Panneau du jeu : en-tete a texture (titre) + corps ContainerPatch. title et body : contenu .ui deja indente de 4."""
    if decorated:
        head = '(TexturePath: "../Common/ContainerHeader.png", HorizontalBorder: 50, VerticalBorder: 0)'
        top = '''    Group {
      Anchor: (Width: 236, Height: 11, Top: -12);
      Background: "../Common/ContainerDecorationTop.png";
    }
'''
        bottom = '''  Group {
    Anchor: (Width: 236, Height: 11, Bottom: -6);
    Background: "../Common/ContainerDecorationBottom.png";
  }
'''
    else:
        head = '(TexturePath: "../Common/ContainerHeaderNoRunes.png", HorizontalBorder: 35, VerticalBorder: 0)'
        top = bottom = ""
    return f'''Group #{gid} {{
  Anchor: (Width: {width}, Height: {height});
  Group #{gid}Head {{
    Anchor: (Height: {TITLE_H}, Top: 0);
    Padding: (Top: 7);
    Background: {head};
{top}{indent(title, 4)}
  }}
  Group #{gid}Body {{
    LayoutMode: Top;
    Anchor: (Top: {TITLE_H});
    Padding: {body_pad};
    Background: (TexturePath: "../Common/ContainerPatch.png", Border: 23);
{indent(body, 4)}
  }}
{bottom}}}'''


def title_label(lid, text="", align="Center"):
    t = f'\n  Text: "{text}";' if text else ""
    return f'''Label #{lid} {{{t}
  Style: (...$C.@TitleStyle, HorizontalAlignment: {align});
}}'''


def caption(text, top=0, color=MUTED, size=11, align="Start", lid=""):
    anchor = f"\n  Anchor: (Top: {top});" if top else ""
    ident = f" #{lid}" if lid else ""
    return f'''Label{ident} {{{anchor}
  Text: "{text}";
  Style: (FontSize: {size}, RenderBold: true, RenderUppercase: true, TextColor: {color}, HorizontalAlignment: {align});
}}'''


def fancy_sep(top=8):
    return f'''$C.@PanelSeparatorFancy {{ @Anchor = (Top: {top}); }}'''


def coins(prefix, n, size=18, font=14, color=GOLD):
    """Rangee de pieces : n paires icone + nombre (#<prefix>Icon<j>, #<prefix>N<j>), masquees ; le Java les remplit."""
    out = []
    for j in range(n):
        out.append(f'''ItemIcon #{prefix}Icon{j} {{ Anchor: (Width: {size}, Height: {size}); Visible: false; }}
Label #{prefix}N{j} {{
  Anchor: (Left: 2, Right: 7);
  Visible: false;
  Style: (FontSize: {font}, RenderBold: true, TextColor: {color}, VerticalAlignment: Center);
}}''')
    return "\n".join(out)


def side_tab(prefix, i, height=34, top=3, font=12):
    """Bouton de categorie vertical : trait de couleur a gauche (#<prefix>Line<i>) quand il est choisi, libelle (#<prefix>Text<i>)."""
    return f'''Button #{prefix}{i} {{
  Anchor: (Height: {height}, Top: {top});
  LayoutMode: Left;
  Visible: false;
  Style: (
    Default: (Background: {SLOT}),
    Hovered: (Background: {SLOT_HOVER}),
    Pressed: (Background: {SLOT_PRESS}),
    Sounds: $C.@ButtonSounds
  );
  Group #{prefix}Line{i} {{ Anchor: (Width: 4); Background: {GOLD}; Visible: false; }}
  Label #{prefix}Text{i} {{
    FlexWeight: 1;
    Anchor: (Left: 10, Right: 6);
    Style: (FontSize: {font}, RenderBold: true, TextColor: {LABEL}, VerticalAlignment: Center);
  }}
}}'''
