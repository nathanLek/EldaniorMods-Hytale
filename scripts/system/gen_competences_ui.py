#!/usr/bin/env python3
"""Onglet Competences du menu Systeme (SystemPage.ui) : filtres par theme, resume, et les 20 cartes de passifs.

Refait le 2026-11-25 (demande de l'utilisateur : « ameliore la vue des competences passives, le design »).
Remplace dans SystemPage.ui le bloc entre les marqueurs « // @competences-debut » et « // @competences-fin » (la premiere
fois : le bloc #SkillCardList d'origine). Rempli par CompetencesTab.java.

Une carte :
  liseré de rarete | nom + origine (CLASSE / APPRIS / METIER) + etat (ACTIVE / RECHARGE) + 6 pastilles de rang
                   | rarete · rang · nom du rang
                   | effet du rang
                   | prochain rang (gris)
                   | barre de maitrise en 10 segments + pourcentage
  colonne droite   | recharge | ON/OFF | OUBLIER
Identifiants sans tiret bas apres # (sinon « failed customUI ») ; pastilles #SkillPip<i>R<k>, segments #SkillMa<i>S<k>.

Usage : python3 scripts/system/gen_competences_ui.py
"""
import os
import re

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
UI = os.path.join(ROOT, "src/main/resources/Common/UI/Custom/System/SystemPage.ui")

CARDS = 20
FILTERS = ["Tout", "Attaque", "Magie", "Défense", "Vie", "Endurance", "Agilité", "Chance", "Divin", "Métiers"]


def lbl(size, color, bold=False, upper=False, align=None, extra=""):
    parts = [f"FontSize: {size}", f"TextColor: {color}"]
    if bold:
        parts.append("RenderBold: true")
    if upper:
        parts.append("RenderUppercase: true")
    if align:
        parts.append(f"HorizontalAlignment: {align}")
    parts.append("VerticalAlignment: Center")
    if extra:
        parts.append(extra)
    return "(" + ", ".join(parts) + ")"


def button_style(size, color, hover="#ffffff"):
    return (f"(Default: (LabelStyle: {lbl(size, color, True, align='Center')}), "
            f"Hovered: (LabelStyle: {lbl(size, hover, True, align='Center')}))")


def card(i):
    pips = " ".join(f"Group #SkillPip{i}R{k} {{ Anchor: (Width: 16, Height: 6); Background: #2c3644; }} Group {{ Anchor: (Width: 3); }}"
                    for k in range(6))
    segs = " ".join(f"Group #SkillMa{i}S{k} {{ Anchor: (Width: 22, Height: 5); Background: #222a36; }} Group {{ Anchor: (Width: 2); }}"
                    for k in range(10))
    return (
        f"Group #SkillCard{i} {{ Anchor: (Height: 132); Background: #10151d(0.82); LayoutMode: Left; Visible: false; "
        f"Group #SkillBorder{i} {{ Anchor: (Width: 5); Background: #8899aa; }} "
        f"Group #SkillActiveBar{i} {{ Anchor: (Width: 3); Background: #4caf50; Visible: false; }} "
        # --- centre
        f"Group {{ LayoutMode: Top; FlexWeight: 1; Padding: (Horizontal: 12, Vertical: 7); "
        f"Group {{ LayoutMode: Left; Anchor: (Height: 22); "
        f"Label #SkillName{i} {{ Text: \"\"; Style: {lbl(16, '#ffffff', True)}; }} "
        f"Group {{ Anchor: (Width: 8); }} "
        f"Label #SkillOrigin{i} {{ Text: \"\"; Visible: false; Style: {lbl(8, '#c8d4e0', True, True, 'Center')}; Anchor: (Height: 16); "
        f"Background: #26303e(0.95); Padding: (Horizontal: 6, Vertical: 1); }} "
        f"Group {{ Anchor: (Width: 6); }} "
        f"Label #SkillStateBadge{i} {{ Text: \"\"; Visible: false; Style: {lbl(8, '#7ed47e', True, True, 'Center')}; Anchor: (Height: 16); "
        f"Background: #1a3a1a(0.9); Padding: (Horizontal: 6, Vertical: 1); }} "
        f"Group {{ FlexWeight: 1; }} "
        f"Group #SkillPips{i} {{ LayoutMode: Left; Anchor: (Height: 22); Padding: (Top: 8); {pips} }} "
        f"}} "
        f"Label #SkillType{i} {{ Text: \"\"; Style: {lbl(10, '#c8a2c8', True, True)}; Anchor: (Height: 15); }} "
        f"Label #SkillDesc{i} {{ Text: \"\"; Style: (FontSize: 14, TextColor: #d2dbe6, Wrap: true); FlexWeight: 1; }} "
        f"Label #SkillNext{i} {{ Text: \"\"; Visible: false; Style: (FontSize: 11, TextColor: #7f8fa3, Wrap: true); Anchor: (Height: 16); }} "
        f"Group {{ LayoutMode: Left; Anchor: (Height: 14); "
        f"Label {{ Text: \"MAÎTRISE\"; Style: {lbl(8, '#7f8fa3', True)}; Anchor: (Width: 58); }} "
        f"Group {{ LayoutMode: Left; Anchor: (Height: 14); Padding: (Top: 5); {segs} }} "
        f"Group {{ Anchor: (Width: 6); }} "
        f"Label #SkillMasteryVal{i} {{ Text: \"\"; Style: {lbl(10, '#7ed47e', True)}; }} "
        f"Group {{ FlexWeight: 1; }} "
        f"}} "
        f"}} "
        # --- colonne droite
        f"Group #SkillToggleWrap{i} {{ Anchor: (Width: 64); LayoutMode: Top; Background: #1a3a1a(0.9); Padding: (Vertical: 6, Horizontal: 4); "
        f"Group #SkillCd{i} {{ LayoutMode: Top; Visible: false; Background: #2a2410(0.9); Padding: (Vertical: 2); "
        f"Label {{ Text: \"RECHARGE\"; Style: {lbl(7, '#ef9f27', True, True, 'Center')}; Anchor: (Height: 9); }} "
        f"Label #SkillCdVal{i} {{ Text: \"\"; Style: {lbl(11, '#ef9f27', True, align='Center')}; Anchor: (Height: 14); }} }} "
        f"Group {{ FlexWeight: 1; }} "
        f"TextButton #SkillToggle{i} {{ Text: \"\"; Anchor: (Height: 24); Style: {button_style(13, '#7ed47e')}; }} "
        f"Label #SkillCdTimer{i} {{ Text: \"\"; Visible: false; Anchor: (Height: 16); Style: {lbl(9, '#ef9f27', True, align='Center')}; }} "
        f"Group {{ FlexWeight: 1; }} "
        f"TextButton #SkillForget{i} {{ Text: \"OUBLIER\"; Visible: false; Anchor: (Height: 18); Style: {button_style(8, '#c06060', '#ff8080')}; }} "
        f"}} "
        f"}}"
    )


def block():
    ind = "            "
    out = [f"{ind}// @competences-debut (scripts/system/gen_competences_ui.py : ne pas modifier a la main)"]
    # filtres par theme
    out.append(f"{ind}Group #SkillFilters {{ LayoutMode: Left; Anchor: (Height: 26); Visible: false; "
               + " ".join(f"TextButton #SkillFilter{k} {{ Text: \"{name}\"; FlexWeight: 1; Anchor: (Height: 24); "
                          f"Style: {button_style(10, '#aab6c4', '#FFD700')}; Background: #141a24(0.85); }} Group {{ Anchor: (Width: 3); }}"
                          for k, name in enumerate(FILTERS))
               + " }")
    out.append(f"{ind}Group {{ Anchor: (Height: 4); }}")
    out.append(f"{ind}Label #SkillSummary {{ Text: \"\"; Visible: false; Anchor: (Height: 18); "
               f"Style: {lbl(11, '#9fb0c2', align='Center')}; }}")
    out.append(f"{ind}Group {{ Anchor: (Height: 4); }}")
    out.append(f"{ind}// Fiches des passifs, cachees dans la vue Runes actives (sinon leurs intercalaires font defiler la page)")
    out.append(f"{ind}Group #SkillCardList {{")
    out.append(f"{ind}LayoutMode: Top;")
    for i in range(CARDS):
        out.append(ind + card(i))
        out.append(f"{ind}Group {{ Anchor: (Height: 5); }}")
    out.append(f"{ind}}}")
    out.append(f"{ind}// @competences-fin")
    return "\n".join(out)


s = open(UI, encoding="utf-8").read()
if "// @competences-debut" in s:
    a = s.index("            // @competences-debut")
    b = s.index("// @competences-fin") + len("// @competences-fin")
else:
    a = s.index("            // Fiches des passifs, cachees dans la vue Runes actives")
    start = s.index("Group #SkillCardList {", a)
    # fin du bloc : l'accolade fermante au meme niveau apres la derniere carte
    m = re.compile(r"\n            \}\n").search(s, start)
    b = m.end() - 1
s = s[:a] + block() + s[b:]
open(UI, "w", encoding="utf-8").write(s)
print("Onglet Competences ecrit :", CARDS, "cartes,", len(FILTERS), "filtres")
