"""Fenetre du bloc de poste de PNJ (Common/UI/Custom/Npc/NpcPost.ui), pilotee par guards/NpcPostPage.

Dans le style de la carte de recrutement des aventuriers (Npc/Recruit.ui) : fond assombri, carte bleu nuit cerclee d'or (liseres
sombre / or / sombre), embleme, titre en capitales, separateurs dores, en-tetes de section en petites capitales entre deux
traits, libelles dores. Sections : Identite (PNJ, specialite, statut ou rang, niveau, regard) et Services (commerce, quetes,
repliques), cette derniere remplacee par une explication quand le PNJ choisi ne se parle pas (gardes, combattants).

Usage : python3 scripts/npcs/gen_npc_post_ui.py <src/main/resources>
"""
import os, sys

RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "src", "main", "resources")

GOLD, GOLD_LINE, DARK, BODY = "#e8b34f", "#c99436", "#16212f", "#0b141d(0.96)"
LABEL, MUTED, TITLE = "#c9b37a", "#6f8296", "#d1dae6"


def section(title):
    return f'''        Group {{
          Anchor: (Top: 14, Height: 18);
          LayoutMode: Left;
          Group {{ FlexWeight: 1; LayoutMode: Top; Padding: (Top: 8); Group {{ Anchor: (Height: 1); Background: {GOLD_LINE}(0.55); }} }}
          Label {{
            Anchor: (Left: 10, Right: 10);
            Text: "{title}";
            Style: (FontSize: 12, RenderBold: true, RenderUppercase: true, TextColor: {GOLD_LINE}, VerticalAlignment: Center);
          }}
          Group {{ FlexWeight: 1; LayoutMode: Top; Padding: (Top: 8); Group {{ Anchor: (Height: 1); Background: {GOLD_LINE}(0.55); }} }}
        }}
'''


def row(label, control, hint=None):
    sub = f'''
            Label {{
              Text: "{hint}";
              Style: (FontSize: 11, TextColor: {MUTED});
            }}''' if hint else ""
    return f'''        Group {{
          Anchor: (Top: 8, Height: {40 if hint else 30});
          LayoutMode: Left;
          Group {{
            FlexWeight: 1;
            LayoutMode: Top;
            Label {{
              Text: "{label}";
              Style: (FontSize: 15, RenderBold: true, TextColor: {LABEL});
            }}{sub}
          }}
{control}
        }}
'''


def dropdown(i):
    return f'''          $C.@DropdownBox #{i} {{
            Anchor: (Width: 360, Height: 26);
            Style: @DefaultDropdownBoxStyle;
          }}'''


def main():
    identity = (row("Catégorie", dropdown("PostCategory"), "Filtre la liste des PNJ : gardes, peuple, commerce, aventuriers, hors-la-loi.")
                + row("PNJ", dropdown("PostType"), "Le type fixe son comportement, son combat et sa tenue.")
                + row("Spécialité", dropdown("PostSpec"), "Classe, métier, boutique ; espèce d'un monstre étoilé.")
                + row("Statut", dropdown("PostTier"), "Apprenti, Expert, Maître (Héros chez les aventuriers ; 1 à 3 étoiles pour un monstre étoilé).")
                + row("Niveau", '''          $C.@NumberField #PostLevel {
            Anchor: (Width: 360, Height: 26);
            Format: (MaxDecimalPlaces: 0, Step: 10);
          }''', "0 : au hasard. Aventurier, chasseur, criminel ; gardes, chevaliers, assassins, disciples : niveau martial (rang).")
                + row("Regard", dropdown("PostFacing"), "Direction vers laquelle il se tourne au repos.")
                + row("Famille", dropdown("PostFamily"), "Celle du territoire, neutre, ou une famille. Empire Luo : clan tiré au hasard (sauf gardes)."))
    services = (row("Commerce", dropdown("PostShop"), "Boutique proposée quand on lui parle.")
                + row("Quêtes", dropdown("PostQuest"), "Quêtes du donneur choisi.")
                + row("Répliques", '''          $C.@TextField #PostLines {
            Anchor: (Width: 360, Height: 28);
            PlaceholderText: "Bonjour ! | La vallée est belle ce matin.";
          }''', "Séparées par | ; il les dit à tour de rôle (« Discuter »)."))
    ui = f'''$C = "../Common.ui";

// Poste de PNJ (bloc Eldanior_Poste_PNJ, admins), genere par scripts/npcs/gen_npc_post_ui.py : voir ce script.
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
      Background: {GOLD};

      Group {{
        LayoutMode: Top;
        Padding: (Full: 2);
        Background: {DARK};

        Group {{
          LayoutMode: Top;
          Background: {BODY};
          Padding: (Left: 26, Right: 26, Top: 16, Bottom: 14);

          // ===== En-tete : embleme, titre, etat du poste =====
          Group {{
            Anchor: (Height: 70);
            LayoutMode: Left;
            Group {{ Anchor: (Width: 70, Height: 70); Background: (TexturePath: "RecruitEmblem.png"); }}
            Group {{
              FlexWeight: 1;
              LayoutMode: Top;
              Padding: (Left: 14, Top: 8);
              Label {{
                Text: "Poste de PNJ";
                Style: (FontSize: 22, RenderBold: true, RenderUppercase: true, TextColor: {TITLE});
              }}
              Label #PostState {{
                Anchor: (Top: 4);
                Style: (FontSize: 13, RenderBold: true, TextColor: #878e9c, Wrap: true);
              }}
            }}
          }}

          Group {{ Anchor: (Top: 10, Height: 2); Background: {GOLD_LINE}; }}

{section("Identité")}{identity}
{section("Services")}
          Group #PostServices {{
            LayoutMode: Top;
{services}
          }}
          Label #PostServicesOff {{
            Anchor: (Top: 10, Height: 60);
            Style: (FontSize: 13, RenderBold: true, TextColor: #c9b37a, Wrap: true, HorizontalAlignment: Center, VerticalAlignment: Center);
          }}

          Group {{ Anchor: (Top: 14, Height: 2); Background: {GOLD_LINE}; }}

          Group {{
            LayoutMode: Left;
            Anchor: (Top: 14, Height: 44);
            $C.@TextButton #PostPlace {{
              @Anchor = (Right: 4);
              Text: "Poser le PNJ";
              FlexWeight: 1;
            }}
            $C.@SecondaryTextButton #PostSave {{
              @Anchor = (Left: 4, Right: 4);
              Text: "Enregistrer les services";
              FlexWeight: 1;
            }}
            $C.@SecondaryTextButton #PostRemove {{
              @Anchor = (Left: 4);
              Text: "Retirer le PNJ";
              FlexWeight: 1;
            }}
          }}

          Label {{
            Anchor: (Top: 10);
            Text: "Échap : fermer  ·  « Poser » remplace le PNJ ; « Enregistrer » garde le même PNJ et change ses services";
            Style: (FontSize: 11, TextColor: #5a6a7a, HorizontalAlignment: Center);
          }}
        }}
      }}
    }}
  }}
}}
'''
    path = os.path.join(RES, "Common", "UI", "Custom", "Npc", "NpcPost.ui")
    with open(path, "w", encoding="utf-8") as f:
        f.write(ui)
    print("ecrit", path)


if __name__ == "__main__":
    main()
