"""Fenetre de discussion avec un PNJ (Common/UI/Custom/Npc/Talk.ui), pilotee par guards/NpcTalkPage.

Style de la carte de recrutement (Npc/Recruit.ui) : fond assombri, carte bleu nuit cerclee d'or, embleme, nom du PNJ en
capitales, replique en citation dans un encart, puis ses services en lignes cliquables (titre dore et description), et
Au revoir.

Usage : python3 scripts/npcs/gen_talk_ui.py <src/main/resources>
"""
import os, sys

RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "src", "main", "resources")
OPTIONS = 5
GOLD, GOLD_LINE, DARK, BODY, PANEL = "#e8b34f", "#c99436", "#16212f", "#0b141d(0.96)", "#132130"


def option(i):
    return f'''            Button #TalkOpt{i} {{
              Anchor: (Top: 6, Height: 50);
              LayoutMode: Left;
              Visible: false;
              Style: (
                Default: (Background: {PANEL}),
                Hovered: (Background: #1d3044),
                Pressed: (Background: #24405a),
                Sounds: $C.@ButtonSounds
              );
              Group {{ Anchor: (Width: 4); Background: {GOLD_LINE}; }}
              Group {{
                FlexWeight: 1;
                LayoutMode: Top;
                Padding: (Left: 14, Top: 6);
                Label #TalkOptTitle{i} {{
                  Style: (FontSize: 15, RenderBold: true, RenderUppercase: true, TextColor: #f0c868);
                }}
                Label #TalkOptDesc{i} {{
                  Anchor: (Top: 2);
                  Style: (FontSize: 12, TextColor: #8a96a8);
                }}
              }}
              Label {{
                Anchor: (Width: 30);
                Text: "›";
                Style: (FontSize: 22, RenderBold: true, TextColor: {GOLD_LINE}, HorizontalAlignment: Center, VerticalAlignment: Center);
              }}
            }}'''


ui = f'''$C = "../Common.ui";

// Discussion avec un PNJ (guards/NpcTalkPage), generee par scripts/npcs/gen_talk_ui.py : voir ce script.
Group {{
  LayoutMode: Middle;
  Background: #05080d(0.55);

  Group {{
    Anchor: (Width: 520);
    LayoutMode: Top;
    Padding: (Full: 3);
    Background: {DARK};
    Group {{
      LayoutMode: Top;
      Padding: (Full: 3);
      Background: {GOLD};
      Group {{
        LayoutMode: Top;
        Padding: (Full: 2);
        Background: {DARK};
        Group {{
          LayoutMode: Top;
          Background: {BODY};
          Padding: (Left: 22, Right: 22, Top: 16, Bottom: 14);

          Group {{
            Anchor: (Height: 64);
            LayoutMode: Left;
            Group {{ Anchor: (Width: 64, Height: 64); Background: (TexturePath: "RecruitEmblem.png"); }}
            Group {{
              FlexWeight: 1;
              LayoutMode: Top;
              Padding: (Left: 14, Top: 10);
              Label #TalkName {{
                Style: (FontSize: 19, RenderBold: true, RenderUppercase: true, TextColor: #d1dae6, Wrap: true);
              }}
              Label {{
                Anchor: (Top: 2);
                Text: "Que voulez-vous ?";
                Style: (FontSize: 12, RenderBold: true, TextColor: #878e9c);
              }}
            }}
          }}

          Group {{ Anchor: (Top: 10, Height: 2); Background: {GOLD_LINE}; }}

          Group {{
            Anchor: (Top: 12);
            LayoutMode: Top;
            Background: {PANEL};
            Padding: (Left: 16, Right: 16, Top: 12, Bottom: 12);
            Label #TalkText {{
              Style: (FontSize: 14, RenderBold: true, TextColor: #c9b37a, Wrap: true, HorizontalAlignment: Center);
            }}
          }}

          Group {{
            Anchor: (Top: 8);
            LayoutMode: Top;
{chr(10).join(option(i) for i in range(OPTIONS))}
          }}

          Group {{ Anchor: (Top: 12, Height: 2); Background: {GOLD_LINE}; }}

          Group {{
            Anchor: (Top: 12, Height: 44);
            LayoutMode: Center;
            $C.@TextButton #TalkBye {{
              @Anchor = Anchor(MinWidth: 220);
              Text: "Au revoir";
            }}
          }}
        }}
      }}
    }}
  }}
}}
'''
path = os.path.join(RES, "Common", "UI", "Custom", "Npc", "Talk.ui")
with open(path, "w", encoding="utf-8") as f:
    f.write(ui)
print("ecrit", path)
