"""HUD des quetes en cours (Common/UI/Custom/Quest/QuestHud.ui), rendu par quest/QuestHud.java (aussi via hud/CombinedHud).

2026-11-13 (demande de l'utilisateur) : chaque quete suivie est encadree comme une carte de la fenetre Chapitres (contour a la
couleur de son type : principale or, secondaire bleu, journaliere vert, exclusive violet, prime rouge), avec son type, son rang
de mission (F a S, a sa couleur), son nom, et ses attendus comme dans la fenetre de quete (case cochee, texte, avancement).
Trois panneaux : la quete principale (#QHudMain...), la quete suivie (#QHud...) et l'autre quete en cours (#QHudAlt...,
2 quetes en cours au plus en plus de la principale).

Le contour est une bordure de couleur (Background pose par le code) autour d'un fond decore (QHudPatch, neuf-tranches) :
filet interieur et equerres claires aux coins, comme les ornements des cartes.

Usage : python3 scripts/quetes/gen_quest_hud.py [src/main/resources]
"""
import math, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "rune_aura_icons"))
import png  # noqa: E402

RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "..", "..", "src", "main", "resources")
OUT = os.path.join(RES, "Common", "UI", "Custom", "Quest")
OBJECTIVES = 4                         # = QuestHud.OBJECTIVES
WIDTH = 430
# Rangs de mission et leur couleur (= quest/QuestDifficulty.java) : un blason par rang (QHudRank<R>@2x.png, 2026-11-14)
# SS et EX (2026-11-16, SSS renomme EX le 2026-11-19) : monstres au-dela du niveau 999 (SS jusqu au niveau 1500, EX au-dessus)
RANKS = {"F": "8899aa", "E": "4CAF50", "D": "2196F3", "C": "9C27B0", "B": "ff9800", "A": "cc4444", "S": "FFD700",
         "SS": "ff7a3d", "EX": "c46bff"}
BADGE = 58                             # taille du blason en px 1x


def patch_png():
    """Fond 9-tranches a 2x (96 px, bord 16 en 1x) : fond sombre degrade, liseré sombre, filet interieur, equerres claires aux
    coins (comme les ornements des cartes)."""
    n = 96
    img = [[[0, 0, 0, 0] for _ in range(n)] for _ in range(n)]
    for y in range(n):
        for x in range(n):
            dx, dy = min(x, n - 1 - x), min(y, n - 1 - y)
            t = y / (n - 1)
            c, a = [int(10 + 6 * (1 - t)), int(15 + 8 * (1 - t)), int(24 + 10 * (1 - t))], 236
            if dx < 3 or dy < 3:                                            # liseré sombre sous le contour colore
                c, a = [6, 9, 14], 255
            elif (dx in (6, 7) and dy >= 6) or (dy in (6, 7) and dx >= 6):  # filet interieur
                c, a = [44, 56, 72], 255
            if 5 <= dx <= 8 and 5 <= dy <= 22 or 5 <= dy <= 8 and 5 <= dx <= 22:   # equerres de coin
                c, a = [150, 170, 196], 255
            img[y][x] = c + [a]
    return n, n, img


def rank_badge(hexcol, ss=3):
    """Blason de rang (2026-11-14, demande de l'utilisateur : « ameliore le design du rang ») : losange a bord metallique
    teinte du rang (clair en haut, sombre en bas), gorge sombre, champ interieur sombre eclaire au centre par la couleur du
    rang, filet interieur, quatre pointes claires, halo autour (plus fort pour A et S). La lettre est un Label pose dessus."""
    n = BADGE * 2
    c = [int(hexcol[i:i + 2], 16) for i in (0, 2, 4)]
    light = [min(255, v + (255 - v) * 0.55) for v in c]
    dark = [v * 0.42 for v in c]
    halo = 0.5 if hexcol in (RANKS["A"], RANKS["S"], RANKS["SS"], RANKS["EX"]) else 0.28
    R, cx = n / 2 - 9, n / 2
    img = [[[0, 0, 0, 0] for _ in range(n)] for _ in range(n)]

    def sample(px, py):
        d = abs(px - cx) + abs(py - cx)                                  # distance « losange »
        t = py / n
        if d > R:
            return c, max(0.0, 1 - (d - R) / 9) ** 2 * halo               # halo
        if d > R - 4: return [10, 14, 20], 1.0                           # bord sombre
        if d > R - 12:                                                   # anneau metallique
            k = t * 0.85 + (0.15 if px > cx else 0)
            col = [light[i] + (dark[i] - light[i]) * k for i in range(3)]
            if R - 7 < d < R - 5.5 and py < cx: col = [min(255, v + 60) for v in col]   # reflet haut
            return col, 1.0
        if d > R - 15: return [8, 11, 17], 1.0                           # gorge
        r = math.hypot(px - cx, py - cx) / (R - 15)
        base = [13, 18, 27]
        col = [base[i] + (c[i] * 0.55 - base[i]) * max(0.0, 1 - r) ** 1.4 for i in range(3)]
        if abs(d - (R - 24)) < 0.9: col = [col[i] * 0.5 + c[i] * 0.5 for i in range(3)]  # filet interieur
        return col, 1.0

    for y in range(n):
        for x in range(n):
            acc, aa = [0.0, 0.0, 0.0], 0.0
            for sy in range(ss):
                for sx in range(ss):
                    col, a = sample(x + (sx + 0.5) / ss, y + (sy + 0.5) / ss)
                    for i in range(3): acc[i] += col[i] * a
                    aa += a
            if aa > 0:
                img[y][x] = [int(acc[i] / aa) for i in range(3)] + [int(255 * aa / (ss * ss))]
    for px, py in ((cx, cx - R + 1), (cx, cx + R - 1), (cx - R + 1, cx), (cx + R - 1, cx)):   # pointes
        for y in range(int(py) - 3, int(py) + 4):
            for x in range(int(px) - 3, int(px) + 4):
                if 0 <= x < n and 0 <= y < n and abs(x - px) + abs(y - py) <= 3:
                    img[y][x] = [int(v) for v in light] + [255]
    return n, n, img


def badges(p):
    return "\n".join(f'                                Group #{p}Badge{r} {{ Anchor: (Left: 0, Top: 0, Width: {BADGE}, Height: {BADGE}); '
                     f'Background: (TexturePath: "QHudRank{r}.png"); Visible: false; }}' for r in RANKS)


def obj_rows(p):
    rows = []
    for i in range(OBJECTIVES):
        rows.append(f"""                    Group #{p}Obj{i} {{
                        Anchor: (Height: 21);
                        LayoutMode: Left;
                        Visible: false;
                        Group {{
                            Anchor: (Width: 16, Height: 16, Top: 2);
                            Group #{p}ObjOff{i} {{ Anchor: (Left: 0, Top: 0, Width: 16, Height: 16); Background: (TexturePath: "ChapCheckOff.png"); }}
                            Group #{p}ObjOn{i} {{ Anchor: (Left: 0, Top: 0, Width: 16, Height: 16); Background: (TexturePath: "ChapCheckOn.png"); Visible: false; }}
                        }}
                        Label #{p}ObjText{i} {{
                            FlexWeight: 1;
                            Anchor: (Left: 7);
                            Style: (FontSize: 12, RenderBold: true, TextColor: #b8c2d0, VerticalAlignment: Center);
                        }}
                        Label #{p}ObjCount{i} {{
                            Anchor: (Width: 78);
                            Style: (FontSize: 12, RenderBold: true, TextColor: #77879d, HorizontalAlignment: End, VerticalAlignment: Center);
                        }}
                    }}""")
    return "\n".join(rows)


def panel(p, comment):
    """Carte : bandeau de rang a gauche (fond teinte du type, filet de sa couleur, « RANG » et la lettre), type et nom, attendus.
    2026-11-13 : plus de ligne de récompense (demande de l'utilisateur)."""
    return f"""            // === {comment} ===
            Group #{p}Panel {{
                LayoutMode: Top;
                Visible: false;
                Padding: (Full: 2);
                Background: #f2c45e;

                Group {{
                    LayoutMode: Top;
                    Background: (TexturePath: "QHudPatch.png", Border: 16);
                    Padding: (Left: 8, Right: 14, Top: 8, Bottom: 10);

                    Group {{
                        Anchor: (Height: {BADGE});
                        LayoutMode: Left;
                        Group #{p}RankTab {{
                            Anchor: (Width: {BADGE + 4}, Height: {BADGE});
                            LayoutMode: Left;
                            Group {{
                                Anchor: (Width: {BADGE}, Height: {BADGE});
{badges(p)}
                                Label #{p}Rank {{
                                    Anchor: (Left: 0, Top: 0, Width: {BADGE}, Height: {BADGE});
                                    Style: (FontName: "Secondary", FontSize: 24, RenderBold: true, TextColor: #ffe98a, HorizontalAlignment: Center, VerticalAlignment: Center, ShrinkTextToFit: true, MinShrinkTextToFitFontSize: 14);
                                }}
                            }}
                            Group {{ Anchor: (Width: 2); }}
                            Group #{p}RankLine {{ Anchor: (Width: 2); Background: #f2c45e; }}
                        }}
                        Group {{
                            FlexWeight: 1;
                            LayoutMode: Top;
                            Padding: (Left: 10, Top: 4);
                            Label #{p}Type {{
                                Anchor: (Height: 16);
                                Style: (FontSize: 10, RenderBold: true, RenderUppercase: true, TextColor: #f2c45e, VerticalAlignment: Center, LetterSpacing: 1);
                            }}
                            Label #{p}Name {{
                                Style: (FontSize: 15, RenderBold: true, TextColor: #f2c45e, Wrap: true);
                            }}
                        }}
                    }}
                    Group {{ Anchor: (Top: 4, Height: 1); Background: #ffffff(0.14); }}
                    Group {{ Anchor: (Height: 4); }}
{obj_rows(p)}
                }}
            }}"""


def hud_ui():
    return f"""// HUD des quetes en cours (quest/QuestHud.java), genere par scripts/quetes/gen_quest_hud.py.
Group {{
    LayoutMode: Top;
    Padding: (Top: 10);

    Group {{
        LayoutMode: Left;
        Anchor: (Height: 520);

        Group {{ Anchor: (Width: 10); }}

        Group {{
            Anchor: (Width: {WIDTH});
            LayoutMode: Top;

{panel("QHudMain", "QUETE PRINCIPALE")}

            Group {{ Anchor: (Height: 6); }}

{panel("QHud", "QUETE SUIVIE")}

            Group {{ Anchor: (Height: 6); }}

{panel("QHudAlt", "AUTRE QUETE EN COURS (limite : 2 + la principale)")}
        }}

        Group {{ FlexWeight: 1; }}
    }}
}}
"""


os.makedirs(OUT, exist_ok=True)
w, h, img = patch_png()
png.write(os.path.join(OUT, "QHudPatch@2x.png"), w, h, img)
for r, col in RANKS.items():
    w, h, img = rank_badge(col)
    png.write(os.path.join(OUT, f"QHudRank{r}@2x.png"), w, h, img)
with open(os.path.join(OUT, "QuestHud.ui"), "w", encoding="utf-8") as fh:
    fh.write(hud_ui())
print("HUD des quetes ecrit dans", OUT)
