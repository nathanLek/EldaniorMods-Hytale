"""Fenetre de la banque (Common/UI/Custom/Npc/Bank.ui), pilotee par guards/BankPage.

Meme carte que la boutique (gen_shop_ui : cadre sombre / or / sombre, corps bleu nuit) :
  - en-tete : embleme, nom du banquier, sa phrase ;
  - deux cartes de solde cote a cote : Bourse (pieces portees) et Compte (argent a l'abri, PlayerLevelData.money),
    chacune avec son icone de piece et un liseret de couleur ;
  - trois onglets (categories, comme la boutique) : Compte, Change, Prets ; un seul panneau visible a la fois :
      Compte : montant + piece, gros boutons Deposer (vert) / Retirer (or), depots rapides (1 or, 10 or, 1 diamant,
               1 zenith), Tout deposer / Tout retirer ;
      Change : grille de 7 cartes (4 + 3) (une par piece : icone, nom, nombre porte, cours), Regrouper (10 -> 1 au-dessus) et
               Casser (1 -> 10 en dessous) ; Tout regrouper ;
      Prets  : plafond et dette (cartes), montant, Emprunter (confirme par un 2e clic) / Rembourser / Tout rembourser,
               avertissement des regles et des consequences (BankLoans) ;
  - bandeau de message (vert : fait ; rouge : refus), Fermer.

Usage : python3 scripts/npcs/gen_bank_ui.py <src/main/resources>
"""
import os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import gen_shop_ui as shop  # noqa: E402

RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "..", "..", "src", "main", "resources")
OUT = os.path.join(RES, "Common", "UI", "Custom", "Npc")
COINS, QUICK, TABS = 7, 4, 3                                  # 7 pieces depuis l'Imperiale (2026-11-16)
PANEL, MUTED, LABEL, GOLD_LINE, TITLE = shop.PANEL, shop.MUTED, shop.LABEL, shop.GOLD_LINE, shop.TITLE
CARD = "#101b28"
GREEN, GREEN_HOVER, GOLD_BTN, GOLD_HOVER = "#1f4a32", "#2b6544", "#5a4318", "#78591f"
SLATE_BTN, SLATE_HOVER = "#1b2a3b", "#26394f"


def btn(bid, text, bg, hover, color="#f2f4f7", height=34, size=13, extra=""):
    """Bouton a fond plein, libelle centre (texte fixe, ou #<bid>Text modifiable)."""
    return f'''Button #{bid} {{
                Anchor: (Height: {height}{extra});
                LayoutMode: Middle;
                Style: (
                  Default: (Background: {bg}),
                  Hovered: (Background: {hover}),
                  Pressed: (Background: {hover}),
                  Sounds: $C.@ButtonSounds
                );
                Label #{bid}Text {{
                  Text: "{text}";
                  Style: (FontSize: {size}, RenderBold: true, RenderUppercase: true, TextColor: {color}, HorizontalAlignment: Center, VerticalAlignment: Center);
                }}
              }}'''


def balance(vid, caption, coin_id, accent):
    return f'''            Group {{
              FlexWeight: 1;
              Anchor: (Height: 64);
              LayoutMode: Left;
              Background: {CARD};
              Group {{ Anchor: (Width: 4); Background: {accent}; }}
              Group {{ Anchor: (Width: 60); LayoutMode: Middle; ItemIcon #{coin_id} {{ Anchor: (Width: 44, Height: 44); }} }}
              Group {{
                FlexWeight: 1;
                LayoutMode: Top;
                Padding: (Top: 10);
                Label {{
                  Text: "{caption}";
                  Style: (FontSize: 10, RenderBold: true, RenderUppercase: true, TextColor: {MUTED});
                }}
                Label #{vid} {{
                  Anchor: (Top: 2);
                  Style: (FontSize: 20, RenderBold: true, TextColor: #f0c868);
                }}
              }}
            }}'''


def tab(i, text):
    return f'''            Button #BankTab{i} {{
              FlexWeight: 1;
              Anchor: (Right: {3 if i < TABS - 1 else 0});
              LayoutMode: Top;
              Style: (
                Default: (Background: {PANEL}),
                Hovered: (Background: #1d3044),
                Pressed: (Background: #24405a),
                Sounds: $C.@ButtonSounds
              );
              Label #BankTabText{i} {{
                Anchor: (Height: 32);
                Text: "{text}";
                Style: (FontSize: 12, RenderBold: true, RenderUppercase: true, TextColor: {MUTED}, HorizontalAlignment: Center, VerticalAlignment: Center);
              }}
              Group #BankTabLine{i} {{ Anchor: (Height: 3); Background: #d4af37; Visible: false; }}
            }}'''


ROW1 = 4                                                      # cartes de la 1re rangee (Imperiale ... Diamant)


def coin_card(i):
    return f'''                Group {{
                  FlexWeight: 1;
                  Anchor: (Right: {0 if i in (ROW1 - 1, COINS - 1) else 6}, Height: 118);
                  LayoutMode: Top;
                  Background: {CARD};
                  Padding: (Left: 10, Right: 10, Top: 8, Bottom: 8);
                  Group {{
                    Anchor: (Height: 44);
                    LayoutMode: Left;
                    ItemIcon #BankCoinIcon{i} {{ Anchor: (Width: 42, Height: 42); }}
                    Group {{
                      FlexWeight: 1;
                      LayoutMode: Top;
                      Padding: (Left: 8, Top: 2);
                      Label #BankCoinName{i} {{ Style: (FontSize: 14, RenderBold: true, TextColor: #f0c868); }}
                      Label #BankCoinRate{i} {{ Style: (FontSize: 10, TextColor: {MUTED}); }}
                    }}
                    Label #BankCoinCount{i} {{
                      Anchor: (Width: 52);
                      Style: (FontSize: 18, RenderBold: true, TextColor: #d1dae6, HorizontalAlignment: End, VerticalAlignment: Center);
                    }}
                  }}
                  Group {{ Anchor: (Height: 8); }}
                  Group {{
                    Anchor: (Height: 30);
                    LayoutMode: Left;
                    {btn(f"BankUp{i}", "↑ Regrouper", SLATE_BTN, SLATE_HOVER, "#9fd3a8", 30, 11, ", Right: 3").replace("Anchor: (Height: 30, Right: 3);", "FlexWeight: 1; Anchor: (Height: 30, Right: 3);")}
                    {btn(f"BankDown{i}", "↓ Casser", SLATE_BTN, SLATE_HOVER, "#e3c27a", 30, 11, ", Left: 3").replace("Anchor: (Height: 30, Left: 3);", "FlexWeight: 1; Anchor: (Height: 30, Left: 3);")}
                  }}
                }}'''


def loan_card(vid, sub_id, caption, accent, side):
    """Carte de l'onglet Prets : intitule, montant, ligne d'explication (plafond, echeance)."""
    return f'''              Group {{
                FlexWeight: 1;
                Anchor: ({side}: 4, Height: 54);
                LayoutMode: Left;
                Background: {CARD};
                Group {{ Anchor: (Width: 4); Background: {accent}; }}
                Group {{
                  FlexWeight: 1;
                  LayoutMode: Top;
                  Padding: (Left: 10, Top: 6);
                  Label {{ Text: "{caption}"; Style: (FontSize: 10, RenderBold: true, RenderUppercase: true, TextColor: {MUTED}); }}
                  Label #{vid} {{ Style: (FontSize: 17, RenderBold: true, TextColor: #f0c868); }}
                  Label #{sub_id} {{ Style: (FontSize: 10, TextColor: {MUTED}); }}
                }}
              }}'''


def flex(button, side):
    return button.replace("Anchor: (Height:", "FlexWeight: 1; Anchor: (%s: 3, Height:" % side, 1)


def main():
    os.makedirs(OUT, exist_ok=True)
    quick = "\n              ".join(flex(btn(f"BankQuick{i}", "", SLATE_BTN, SLATE_HOVER, "#d1dae6", 30, 11), "Right" if i < QUICK - 1 else "Left")
                                    for i in range(QUICK))
    inner = f'''          // ===== En-tete =====
          Group {{
            Anchor: (Height: 72);
            LayoutMode: Left;
            Group {{ Anchor: (Width: 72, Height: 72); Background: (TexturePath: "ShopEmblem.png"); }}
            Group {{
              FlexWeight: 1;
              LayoutMode: Top;
              Padding: (Left: 16, Top: 12);
              Label #BankTitle {{ Style: (FontSize: 22, RenderBold: true, RenderUppercase: true, TextColor: {TITLE}); }}
              Label #BankSub {{ Anchor: (Top: 4); Style: (FontSize: 13, RenderBold: true, TextColor: {LABEL}, Wrap: true); }}
            }}
          }}

          // ===== Soldes =====
          Group {{
            Anchor: (Top: 12, Height: 64);
            LayoutMode: Left;
{balance("BankPurse", "Bourse · pièces portées", "BankPurseIcon", "#6f8faf")}
            Label {{
              Anchor: (Width: 40);
              Text: "›";
              Style: (FontSize: 22, RenderBold: true, TextColor: {GOLD_LINE}, HorizontalAlignment: Center, VerticalAlignment: Center);
            }}
{balance("BankAccount", "Compte · à l'abri à la banque", "BankAccountIcon", "#d4af37")}
          }}

          // ===== Onglets =====
          Group {{
            Anchor: (Top: 14, Height: 35);
            LayoutMode: Left;
{tab(0, "Compte")}
{tab(1, "Change")}
{tab(2, "Prêts")}
          }}
          Group {{ Anchor: (Height: 1); Background: {GOLD_LINE}(0.5); }}

          // ===== Panneau Compte =====
          Group #BankPanel0 {{
            Anchor: (Top: 12, Height: 268);
            LayoutMode: Top;
            Label {{
              Text: "Montant";
              Style: (FontSize: 11, RenderBold: true, RenderUppercase: true, TextColor: {MUTED});
            }}
            Group {{
              Anchor: (Top: 4, Height: 34);
              LayoutMode: Left;
              $C.@NumberField #BankAmount {{
                FlexWeight: 1;
                Anchor: (Height: 32);
                Format: (MaxDecimalPlaces: 0, Step: 1);
              }}
              Group {{ Anchor: (Width: 8); }}
              $C.@DropdownBox #BankUnit {{
                Anchor: (Width: 220, Height: 32);
                Style: @DefaultDropdownBoxStyle;
              }}
            }}
            Group {{
              Anchor: (Top: 10, Height: 44);
              LayoutMode: Left;
              {flex(btn("BankDeposit", "↓ Déposer au compte", GREEN, GREEN_HOVER, "#c8f0d2", 44, 14), "Right")}
              {flex(btn("BankWithdraw", "↑ Retirer en pièces", GOLD_BTN, GOLD_HOVER, "#ffe7a8", 44, 14), "Left")}
            }}
            Label {{
              Anchor: (Top: 14);
              Text: "Dépôts rapides";
              Style: (FontSize: 11, RenderBold: true, RenderUppercase: true, TextColor: {MUTED});
            }}
            Group {{
              Anchor: (Top: 4, Height: 30);
              LayoutMode: Left;
              {quick}
            }}
            Group {{
              Anchor: (Top: 10, Height: 34);
              LayoutMode: Left;
              {flex(btn("BankDepositAll", "Tout déposer", SLATE_BTN, SLATE_HOVER, "#9fd3a8", 34, 12), "Right")}
              {flex(btn("BankWithdrawAll", "Tout retirer", SLATE_BTN, SLATE_HOVER, "#e3c27a", 34, 12), "Left")}
            }}
            Label {{
              Anchor: (Top: 12);
              Text: "Le compte est l'or de votre menu : il paie recrutements, primes et frais de famille, et ne se perd pas à la mort.";
              Style: (FontSize: 11, TextColor: {MUTED}, Wrap: true, HorizontalAlignment: Center);
            }}
          }}

          // ===== Panneau Change =====
          Group #BankPanel1 {{
            Anchor: (Top: 12, Height: 268);
            LayoutMode: Top;
            Visible: false;
            Group {{
              Anchor: (Height: 118);
              LayoutMode: Left;
{chr(10).join(coin_card(i) for i in range(ROW1))}
            }}
            Group {{
              Anchor: (Top: 6, Height: 118);
              LayoutMode: Left;
{chr(10).join(coin_card(i) for i in range(ROW1, COINS))}
            }}
            Group {{
              Anchor: (Top: 8, Height: 34);
              LayoutMode: Center;
              {btn("BankTidy", "Tout regrouper : le moins de pièces possible", SLATE_BTN, SLATE_HOVER, "#d1dae6", 34, 12, ", Width: 420")}
            }}
          }}

          // ===== Panneau Prets (BankLoans, 2026-11-12) =====
          Group #BankPanel2 {{
            Anchor: (Top: 12, Height: 268);
            LayoutMode: Top;
            Visible: false;
            Group {{
              Anchor: (Height: 54);
              LayoutMode: Left;
{loan_card("BankLoanMax", "BankLoanMaxSub", "Vous pouvez emprunter", "#d4af37", "Right")}
{loan_card("BankLoanDue", "BankLoanTime", "Dette envers la banque", "#e0796b", "Left")}
            }}
            Label {{
              Anchor: (Top: 8);
              Text: "Montant";
              Style: (FontSize: 11, RenderBold: true, RenderUppercase: true, TextColor: {MUTED});
            }}
            Group {{
              Anchor: (Top: 4, Height: 32);
              LayoutMode: Left;
              $C.@NumberField #BankLoanAmount {{
                FlexWeight: 1;
                Anchor: (Height: 30);
                Format: (MaxDecimalPlaces: 0, Step: 1);
              }}
              Group {{ Anchor: (Width: 8); }}
              $C.@DropdownBox #BankLoanUnit {{
                Anchor: (Width: 220, Height: 30);
                Style: @DefaultDropdownBoxStyle;
              }}
            }}
            Group {{
              Anchor: (Top: 8, Height: 38);
              LayoutMode: Left;
              {flex(btn("BankBorrow", "Emprunter", GOLD_BTN, GOLD_HOVER, "#ffe7a8", 38, 13), "Right")}
              {flex(btn("BankRepay", "Rembourser", GREEN, GREEN_HOVER, "#c8f0d2", 38, 13), "Left")}
              {flex(btn("BankRepayAll", "Tout rembourser", SLATE_BTN, SLATE_HOVER, "#9fd3a8", 38, 12), "Left")}
            }}
            Group {{
              Anchor: (Top: 8, Height: 86);
              LayoutMode: Left;
              Background: #2a1414;
              Group {{ Anchor: (Width: 4); Background: #e0796b; }}
              Label #BankLoanRules {{
                FlexWeight: 1;
                Padding: (Left: 10, Right: 10, Top: 6);
                Style: (FontSize: 11, TextColor: #f1c3ba, Wrap: true);
              }}
            }}
          }}

          // ===== Message, fermer =====
          Group {{
            Anchor: (Top: 10, Height: 30);
            LayoutMode: Left;
            Background: {CARD};
            Group #BankMsgBar {{ Anchor: (Width: 4); Background: #8fd18f; }}
            Label #BankMsg {{
              FlexWeight: 1;
              Style: (FontSize: 13, RenderBold: true, TextColor: #8fd18f, HorizontalAlignment: Center, VerticalAlignment: Center);
            }}
          }}
          Group {{
            Anchor: (Top: 10, Height: 40);
            LayoutMode: Center;
            $C.@SecondaryTextButton #BankClose {{ @Anchor = Anchor(MinWidth: 220); Text: "Fermer"; }}
          }}'''
    ui = f'''$C = "../Common.ui";

// Banque (guards/BankPage), generee par scripts/npcs/gen_bank_ui.py : voir ce script.
Group {{
  LayoutMode: Middle;
  Background: #05080d(0.55);
{shop.framed(inner, "BankGold", width=780)}
}}
'''
    path = os.path.join(OUT, "Bank.ui")
    with open(path, "w", encoding="utf-8") as f:
        f.write(ui)
    print("ecrit", path)


if __name__ == "__main__":
    main()
