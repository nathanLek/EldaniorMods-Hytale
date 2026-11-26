"""Pancarte de propriete (demande du 2026-11-12) : bloc Eldanior_Pancarte_Propriete, fenetres Territoire/Pancarte.ui et
Territoire/PancarteLien.ui, embleme PancarteEmblem@2x.png. Pilotee par territory/pancarte (Pancartes, PancartePage,
PancarteLienPage, PancarteEvents). Remplace l'onglet Proprietes du menu systeme.

Fenetre dans le style du recrutement d'aventurier (Npc/Recruit.ui, scripts/npcs/gen_recruit_ui.py) : carte verticale entre
les arches dorees du portail. Premiere carte : le bien (statut, prix, loyer, proprietaire, bail) et les boutons Acheter / Louer
(ou Prolonger / Quitter pour le locataire). Seconde carte, seulement pour le proprietaire et les admins : gestion (prix,
loyer, mise en vente ou en location, protection, acces des joueurs, locataire, lien de la pancarte).

Bloc : panneau du village (modele vanilla), F = interaction Use "Simple" (UseBlockEvent, cf. memoire du projet).

Usage : python3 scripts/territoire/gen_pancarte.py [src/main/resources]
"""
import json, math, os, sys, zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "rune_aura_icons"))
import png  # noqa: E402

RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "..", "..", "src", "main", "resources")
ITEM = "Eldanior_Pancarte_Propriete"
POSTER = "Eldanior_Affiche_Propriete"                         # variante murale (retour du 2026-11-12 : « ou une affiche »)
MODEL_DIR = os.path.join(RES, "Common", "Blocks", "Eldanior", "Territoire")
UI = os.path.join(RES, "Common", "UI", "Custom", "Territoire")
ASSETS = os.path.expanduser("~/Library/Application Support/Hytale/install/release/package/game/latest/Assets.zip")
MEMBERS = 4                                                   # lignes d'acces affichees dans la carte de gestion

# Etats de l'affiche : texte du bandeau, couleur du bandeau, couleur des lettres
POSTER_STATES = {"Vendre": ("À VENDRE", (168, 44, 38), (250, 236, 206)),
                 "Louer": ("À LOUER", (44, 86, 150), (236, 242, 250)),
                 "Occupe": ("OCCUPÉ", (82, 70, 58), (226, 206, 160)),
                 # 2026-11-17 : vente et location a la fois, bandeau coupe en deux (rouge VENDRE, bleu LOUER)
                 "VendreLouer": (None, None, None)}
# police 3 x 5 (lignes de haut en bas) et accents (x, y) au-dessus de la lettre
_G = {"A": "010101111101101", "V": "101101101101010", "E": "111100110100111", "N": "101111111101101", "D": "110101101101110",
      "R": "110101110101101", "L": "100100100100111", "O": "010101101101010", "U": "101101101101111", "C": "011100100100011",
      "P": "110101110100100", " ": "000000000000000"}
FONT = {k: ([v[i:i + 3] for i in range(0, 15, 3)], []) for k, v in _G.items()}
FONT["N"] = (["1001", "1101", "1011", "1001", "1001"], [])          # sur 3 pixels, un N se lit comme un M
FONT["À"] = (FONT["A"][0], [(0, 0), (1, 1)])
FONT["É"] = (FONT["E"][0], [(2, 0), (1, 1)])

# couleurs de Recruit.ui
GOLD, GOLD_LINE, EDGE, BODY, CELL, ROW = "#e8b34f", "#c99436", "#16212f", "#0b141d(0.92)", "#132130", "#101c28"
TITLE, SUB, MUTED, VALUE, LABEL, QUOTE = "#d1dae6", "#96a9be", "#6f8296", "#f0c868", "#a9b8c8", "#c9b37a"


# ---------------------------------------------------------------------------------------------------------------------
# Bloc
# ---------------------------------------------------------------------------------------------------------------------
def block():
    sign = json.loads(zipfile.ZipFile(ASSETS).read("Server/Item/Items/Furniture/Village/Furniture_Village_Sign.json"))
    bt = sign["BlockType"]
    bt["InteractionHint"] = "server.interactionHints.eldanior_pancarte"
    bt["Interactions"] = {"Use": {"Interactions": [{"Type": "Simple"}]}}
    data = {
        "$Comment": "Pancarte de propriete (territory/pancarte) : panneau du village lie a une zone ; F ouvre la fenetre du bien. "
                    "Genere par scripts/territoire/gen_pancarte.py.",
        "TranslationProperties": {
            "Name": "Pancarte de propriété",
            "Description": "Posez-la près d'un bien et liez-la à sa zone (admins : tout bien proche ; joueurs : les biens dont ils "
                           "sont propriétaires). Touche F : informations, achat et location du bien ; le propriétaire y règle le "
                           "prix, le loyer, la mise en vente ou en location, la protection et les accès."},
        "Icon": sign["Icon"], "IconProperties": sign["IconProperties"],
        "Categories": ["Eldanior_System.Autres"], "Quality": "Uncommon",
        "Interactions": sign["Interactions"], "Recipe": sign["Recipe"],
        "BlockType": bt, "PlayerAnimationsId": "Block", "ItemSoundSetId": sign.get("ItemSoundSetId"), "MaxStack": 16,
    }
    path = os.path.join(RES, "Server", "Item", "Items", "Eldanior", "Territoire", ITEM + ".json")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")
    print("ecrit", path)
    lang = os.path.join(RES, "Server", "Languages", "en-US", "server.lang")
    text = open(lang, encoding="utf-8").read()
    key = "interactionHints.eldanior_pancarte="
    if key not in text:
        text = text.replace("interactionHints.eldanior_npcpost=", key + "Appuyez sur [{key}] pour consulter la propriété\n"
                            "interactionHints.eldanior_npcpost=", 1)
        open(lang, "w", encoding="utf-8").write(text)
        print("ajoute", key)


# ---------------------------------------------------------------------------------------------------------------------
# Affiche murale : un quad contre le mur nord du bloc (comme Poster_Wall_Small du jeu), parchemin peint ici
# ---------------------------------------------------------------------------------------------------------------------
def poster():
    os.makedirs(MODEL_DIR, exist_ok=True)
    W, H = 48, 60                                              # zone peinte de la texture (64 x 64), affichee a 0,5
    paper, paper_dark, ink, gold, gold_dark, wax = (226, 206, 160), (176, 146, 98), (74, 52, 34), (214, 160, 62), (140, 92, 30), (150, 32, 36)
    rows = [[[0, 0, 0, 0] for _ in range(64)] for _ in range(64)]
    for y in range(H):
        for x in range(W):
            edge = min(x, y, W - 1 - x, H - 1 - y)
            if edge == 0 and (x + y) % 3 == 0:                  # bord dechire
                continue
            n = ((x * 73 + y * 151) % 17) / 17.0                # grain du papier
            c = [paper[i] - 10 * n for i in range(3)]
            if edge <= 2:
                c = [c[i] * 0.85 + paper_dark[i] * 0.15 for i in range(3)]
            if edge == 1 or edge == 0:
                c = list(paper_dark)
            # cadre d'encre
            if edge == 4 and 4 <= x <= W - 5 and 4 <= y <= H - 5:
                c = list(ink)
            # maison : toit (triangle) et murs
            u, v = x - W / 2 + 0.5, y
            if 9 <= v <= 19 and abs(u) <= (v - 9) * 1.15:
                c = list(gold_dark if abs(u) > (v - 9) * 1.15 - 1.5 or v == 19 else gold)
            elif 20 <= v <= 31 and abs(u) <= 9:
                c = list(ink if abs(u) >= 8 or v == 31 else paper_dark)
                if abs(u) <= 2 and v >= 25:
                    c = list(ink)                               # porte
                elif 4 <= abs(u) <= 6 and 22 <= v <= 24:
                    c = list(gold)                              # fenetres
            # lignes d'ecriture
            if v in (37, 41, 45) and 10 <= x <= W - 11 and (x // 3 + v) % 5 != 0:
                c = list(ink)
            if v == 49 and 16 <= x <= W - 17:
                c = list(ink)
            # cachet de cire
            if math.hypot(x - (W - 11), y - (H - 10)) <= 4.2:
                c = list(wax) if math.hypot(x - (W - 11), y - (H - 10)) <= 3.2 else [wax[i] * 0.7 for i in range(3)]
            rows[y][x] = [int(c[0]), int(c[1]), int(c[2]), 255]
    tex = os.path.join(MODEL_DIR, "Affiche_Propriete.png")
    png.write(tex, 64, 64, rows)
    # Etats de l'affiche selon le bien (2026-11-12 : « affiche dynamique : occupe, a vendre, a louer ») : un bandeau colore
    # lettre remplace les lignes d'ecriture ; Pancartes.refreshPoster choisit l'etat.
    for state, (text, band, letter) in POSTER_STATES.items():
        st = [list(map(list, r)) for r in rows]
        if text is None:
            split_band(st, W, [("VENDRE", POSTER_STATES["Vendre"][1], POSTER_STATES["Vendre"][2], 33),
                               ("LOUER", POSTER_STATES["Louer"][1], POSTER_STATES["Louer"][2], 40)])
            png.write(os.path.join(MODEL_DIR, "Affiche_Propriete_%s.png" % state), 64, 64, st)
            continue
        bx0, bx1, by0, by1 = 6, W - 7, 34, 45
        for y in range(by0, by1 + 1):
            for x in range(bx0, bx1 + 1):
                edge = y in (by0, by1) or x in (bx0, bx1)
                c = [band[i] * (0.7 if edge else 1.0) for i in range(3)]
                st[y][x] = [int(c[0]), int(c[1]), int(c[2]), 255]
        width = sum(len(FONT[ch][0][0]) + 1 for ch in text) - 1
        x0 = bx0 + (bx1 - bx0 + 1 - width) // 2
        gx = x0
        for ch in text:
            glyph, accent = FONT[ch]
            for gy, line in enumerate(glyph):
                for dx, bit in enumerate(line):
                    if bit == "1":
                        st[38 + gy][gx + dx] = list(letter) + [255]
            for (ax, ay) in accent:
                st[36 + ay][gx + ax] = list(letter) + [255]
            gx += len(glyph[0]) + 1
        png.write(os.path.join(MODEL_DIR, "Affiche_Propriete_%s.png" % state), 64, 64, st)
    model = {"lod": "auto", "nodes": [{
        "id": "0", "name": "Affiche", "children": [],
        "position": {"x": 0, "y": 16, "z": -15.6}, "orientation": {"x": 0, "y": 0, "z": 0.012, "w": 0.99993},
        "shape": {"type": "quad", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": 0.5, "y": 0.5, "z": 0.5},
                  "settings": {"size": {"x": W, "y": H}, "normal": "+Z"}, "visible": True, "doubleSided": True,
                  "shadingMode": "standard", "unwrapMode": "custom",
                  "textureLayout": {"front": {"offset": {"x": 0, "y": 0}, "mirror": {"x": False, "y": False}, "angle": 0}}}}]}
    with open(os.path.join(MODEL_DIR, "Affiche_Propriete.blockymodel"), "w") as f:
        json.dump(model, f, indent=2)
    data = {
        "$Comment": "Affiche de propriete (territory/pancarte) : liee a un bien, F ouvre sa fenetre ; etats Vendre / Louer / VendreLouer / Occupe "
                    "selon le bien (Pancartes.refreshPoster). Genere par scripts/territoire/gen_pancarte.py.",
        "TranslationProperties": {
            "Name": "Affiche de propriété",
            "Description": "Se colle contre un mur. Liez-la à un bien proche (admins : tout bien ; joueurs : leurs biens). Touche F : "
                           "informations, achat et location ; le propriétaire y règle le prix, le loyer et les accès."},
        "Icon": "Icons/ItemsGenerated/" + POSTER + ".png",
        "IconProperties": {"Scale": 0.9, "Translation": [0, -4], "Rotation": [0, 180, 0]},
        "Categories": ["Eldanior_System.Autres"], "Quality": "Uncommon",
        "Interactions": {"Primary": "Block_Primary", "Secondary": "Block_Secondary"},
        "Recipe": {"Input": [{"ItemId": "Ingredient_Fabric_Scrap_Cotton", "Quantity": 1}],
                   "BenchRequirement": [{"Id": "Builders", "Type": "StructuralCrafting", "Categories": ["Sign"]}]},
        "BlockType": {"Material": "Solid", "DrawType": "Model", "Opacity": "Transparent",
                      "CustomModel": "Blocks/Eldanior/Territoire/Affiche_Propriete.blockymodel",
                      "CustomModelTexture": [{"Weight": 1, "Texture": "Blocks/Eldanior/Territoire/Affiche_Propriete.png"}],
                      "State": {"Definitions": {state: {
                          "CustomModel": "Blocks/Eldanior/Territoire/Affiche_Propriete.blockymodel",
                          "CustomModelTexture": [{"Weight": 1, "Texture": "Blocks/Eldanior/Territoire/Affiche_Propriete_%s.png" % state}],
                          "Interactions": {"Use": {"Interactions": [{"Type": "Simple"}]}}}   # F reste actif dans chaque etat
                          for state in POSTER_STATES}},
                      "HitboxType": "Sign_Wall", "VariantRotation": "NESW",
                      "Support": {"North": [{"FaceType": "Full"}]},
                      "Gathering": {"Soft": {"IsWeaponBreakable": False}},
                      "BlockParticleSetId": "Dust", "BlockSoundSetId": "Cloth", "PhysicalMaterialId": "Wool",
                      "ParticleColor": "#e2cea0", "TextureComputedColor": "#c9b07c",
                      "InteractionHint": "server.interactionHints.eldanior_pancarte",
                      "Interactions": {"Use": {"Interactions": [{"Type": "Simple"}]}}},
        "PlayerAnimationsId": "Block", "ItemSoundSetId": "ISS_Blocks_Wood", "MaxStack": 16,
    }
    path = os.path.join(RES, "Server", "Item", "Items", "Eldanior", "Territoire", POSTER + ".json")
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")
    # icone : le parchemin agrandi (64 x 64)
    icon = [[[0, 0, 0, 0] for _ in range(64)] for _ in range(64)]
    for y in range(64):
        for x in range(64):
            sx, sy = int((x - 8) * W / 48), int((y + 0) * H / 64)
            if 0 <= x - 8 < 48 and 0 <= sy < H and 0 <= sx < W:
                icon[y][x] = rows[sy][sx]
    png.write(os.path.join(RES, "Common", "Icons", "ItemsGenerated", POSTER + ".png"), 64, 64, icon)
    print("ecrit", path)


def split_band(st, W, parts):
    """Bandeaux superposes de 7 px (bord fonce en haut et en bas), un mot sans accent centre dans chacun."""
    bx0, bx1 = 6, W - 7
    for text, band, letter, by0 in parts:
        by1 = by0 + 6
        for y in range(by0, by1 + 1):
            for x in range(bx0, bx1 + 1):
                edge = y in (by0, by1) or x in (bx0, bx1)
                c = [band[i] * (0.7 if edge else 1.0) for i in range(3)]
                st[y][x] = [int(c[0]), int(c[1]), int(c[2]), 255]
        width = sum(len(FONT[ch][0][0]) + 1 for ch in text) - 1
        gx = bx0 + (bx1 - bx0 + 1 - width) // 2
        for ch in text:
            glyph, _ = FONT[ch]
            for gy, line in enumerate(glyph):
                for dx, bit in enumerate(line):
                    if bit == "1":
                        st[by0 + 1 + gy][gx + dx] = list(letter) + [255]
            gx += len(glyph[0]) + 1


# ---------------------------------------------------------------------------------------------------------------------
# Embleme : losange du portail, maison doree
# ---------------------------------------------------------------------------------------------------------------------
def emblem():
    S = 192
    border, fill = (214, 160, 62), (26, 34, 49)
    gold, gold_dark, wall, door = (232, 169, 59), (150, 98, 30), (214, 222, 232), (96, 64, 38)

    def inside_tri(u, v):                                       # toit : triangle sommet (0,-0.52), base y=-0.12 de -0.5 a 0.5
        if v < -0.52 or v > -0.12:
            return False
        half = 0.5 * (v + 0.52) / 0.40
        return abs(u) <= half

    rows = []
    for y in range(S):
        row = []
        for x in range(S):
            u, v = (x + 0.5) / S * 2 - 1, (y + 0.5) / S * 2 - 1
            d = abs(u) + abs(v)
            col, a = [0, 0, 0], 0.0
            if d <= 0.97:
                a = 1.0
                if d >= 0.88:
                    col = list(border)
                else:
                    glow = max(0.0, 1 - math.hypot(u, v) / 0.75)
                    col = [c + (g - c) * 0.25 * glow for c, g in zip(fill, gold)]
                    if inside_tri(u, v):
                        edge = abs(u) > 0.5 * (v + 0.52) / 0.40 - 0.05 or v > -0.17
                        col = list(gold_dark if edge else gold)
                    elif -0.36 <= u <= 0.36 and -0.12 <= v <= 0.45:      # murs
                        col = list(wall)
                        if -0.09 <= u <= 0.09 and 0.12 <= v <= 0.45:     # porte
                            col = list(door)
                        elif 0.16 <= abs(u) <= 0.29 and 0.0 <= v <= 0.13:  # fenetres
                            col = list(gold)
                        elif abs(u) > 0.33 or v > 0.42:
                            col = [c * 0.8 for c in wall]
                if d > 0.95:
                    a *= max(0.0, (0.97 - d) / 0.02)
            row.append([col[0], col[1], col[2], a * 255])
        rows.append(row)
    png.write(os.path.join(UI, "PancarteEmblem@2x.png"), S, S, rows)
    print("ecrit PancarteEmblem@2x.png")


# ---------------------------------------------------------------------------------------------------------------------
# Morceaux de fenetre
# ---------------------------------------------------------------------------------------------------------------------
def ind(n):
    return " " * n


def section(title, top=12, sid=None):
    line = f'Group {{ FlexWeight: 1; LayoutMode: Top; Padding: (Top: 8); Group {{ Anchor: (Height: 1); Background: {GOLD_LINE}(0.55); }} }}'
    gid = f" #{sid}" if sid else ""
    return f'''
        Group{gid} {{
          Anchor: (Top: {top}, Height: 16);
          LayoutMode: Left;
          {line}
          Label {{
            Anchor: (Width: 190);
            Text: "{title}";
            Style: (FontSize: 12, RenderBold: true, RenderUppercase: true, TextColor: {GOLD}, HorizontalAlignment: Center);
          }}
          {line}
        }}
'''


def info(label, i):
    return f'''
        Group {{
          Anchor: (Top: 4, Height: 24);
          LayoutMode: Left;
          Background: {ROW};
          Group {{ Anchor: (Width: 3); Background: {GOLD}; }}
          Label {{
            Anchor: (Left: 8, Width: 120);
            Text: "{label}";
            Style: (FontSize: 12, RenderBold: true, TextColor: #f0d58c, VerticalAlignment: Center);
          }}
          Label #{i} {{
            FlexWeight: 1;
            Anchor: (Right: 8);
            Style: (FontSize: 12, TextColor: {LABEL}, VerticalAlignment: Center, HorizontalAlignment: End);
          }}
        }}
'''


def indicator(i, label):
    return f'''
          Group {{
            FlexWeight: 1;
            LayoutMode: Top;
            Background: {CELL};
            Padding: (Top: 4);
            Label #{i} {{
              Style: (FontSize: 15, RenderBold: true, TextColor: {VALUE}, HorizontalAlignment: Center);
            }}
            Label {{
              Text: "{label}";
              Style: (FontSize: 10, RenderBold: true, RenderUppercase: true, TextColor: {MUTED}, HorizontalAlignment: Center);
            }}
          }}
'''


def field_row(label, control, rid=None):
    gid = f" #{rid}" if rid else ""
    return f'''
        Group{gid} {{
          Anchor: (Top: 6, Height: 30);
          LayoutMode: Left;
          Label {{
            FlexWeight: 1;
            Text: "{label}";
            Style: (FontSize: 13, RenderBold: true, TextColor: {QUOTE}, VerticalAlignment: Center);
          }}
{control}
        }}
'''


def number(i):
    return f'''          $C.@NumberField #{i} {{
            Anchor: (Width: 170, Height: 26);
            Format: (MaxDecimalPlaces: 0, Step: 100);
          }}'''


def dropdown(i):
    return f'''          $C.@DropdownBox #{i} {{
            Anchor: (Width: 170, Height: 26);
            Style: @DefaultDropdownBoxStyle;
          }}'''


def card(cid, top, body, bottom, body_height=468):
    """Carte du recrutement : arche du haut (embleme), corps borde d'or, arche du bas."""
    return f'''
    Group #{cid} {{
      Anchor: (Width: 439);
      LayoutMode: Top;

      Group {{
        Anchor: (Height: 252);
        LayoutMode: Top;
        Background: (TexturePath: "../Npc/RecruitFrameTop.png");

        Group {{
          Anchor: (Height: 0);
          Background: (TexturePath: "PancarteEmblem.png", Anchor: (Width: 96, Height: 96, Top: -34));
        }}

        Group {{ Anchor: (Height: 124); }}
{top}
      }}

      Group {{
        Anchor: (Height: {body_height});
        LayoutMode: Left;

        Group {{ Anchor: (Width: 3); Background: {EDGE}; }}
        Group {{ Anchor: (Width: 7); Background: {GOLD}; }}
        Group {{ Anchor: (Width: 2); Background: {EDGE}; }}

        Group {{
          FlexWeight: 1;
          LayoutMode: Top;
          Background: {BODY};
          Padding: (Horizontal: 20, Top: 6);

          Group {{ Anchor: (Height: 2); Background: {GOLD_LINE}; }}
{body}
        }}

        Group {{ Anchor: (Width: 2); Background: {EDGE}; }}
        Group {{ Anchor: (Width: 7); Background: {GOLD}; }}
        Group {{ Anchor: (Width: 3); Background: {EDGE}; }}
      }}

      Group {{
        Anchor: (Height: 201);
        LayoutMode: Top;
        Background: (TexturePath: "../Npc/RecruitFrameBottom.png");
{bottom}
      }}
    }}
'''


def title_block(name, level, foot):
    return f'''
        Label #{name} {{
          Style: (FontSize: 22, RenderBold: true, RenderUppercase: true, TextColor: {TITLE}, Wrap: true, HorizontalAlignment: Center);
        }}

        Label #{level} {{
          Anchor: (Top: 10);
          Style: (FontSize: 15, RenderBold: true, RenderUppercase: true, TextColor: {SUB}, HorizontalAlignment: Center);
        }}

        Label #{foot} {{
          Anchor: (Top: 6);
          Style: (FontSize: 13, RenderBold: true, TextColor: #878e9c, HorizontalAlignment: Center);
        }}
'''


def button_row(*buttons, top=10, rid=None):
    inner = "\n".join(
        f'''          {"Group { Anchor: (Width: 10); }" if n else ""}
          $C.@{kind} #{i} {{
            @Text = "{text}";
            @Anchor = Anchor(MinWidth: {width});
          }}''' for n, (kind, i, text, width) in enumerate(buttons))
    return f'''
        Group{" #" + rid if rid else ""} {{
          Anchor: (Top: {top}, Height: 44);
          LayoutMode: Center;
{inner}
        }}
'''


def page():
    # ----- carte 1 : le bien -----
    info_top = title_block("PcName", "PcType", "PcStatus")
    info_body = f'''
        Label #PcQuote {{
          Anchor: (Top: 10);
          Style: (FontSize: 13, RenderBold: true, TextColor: {QUOTE}, Wrap: true, HorizontalAlignment: Center);
        }}

        Group {{
          Anchor: (Top: 10, Height: 36);
          LayoutMode: Left;
{indicator("PcSurface", "Surface")}
          Group {{ Anchor: (Width: 6); }}
{indicator("PcPrice", "Prix")}
          Group {{ Anchor: (Width: 6); }}
{indicator("PcRent", "Loyer (7 j)")}
        }}
{section("Le bien")}{info("Propriétaire", "PcOwner")}{info("Locataire", "PcRenter")}{info("Bail", "PcLease")}{info("Situé dans", "PcParent")}{info("Accès", "PcAccess")}{info("Coffres", "PcChests")}
        Label #PcNote {{
          Anchor: (Top: 8);
          Style: (FontSize: 12, TextColor: {MUTED}, HorizontalAlignment: Center, Wrap: true);
        }}

        Group {{ FlexWeight: 1; }}

        Group {{
          Anchor: (Height: 56, Bottom: 8);
          LayoutMode: Center;

          Group {{
            Anchor: (Width: 64, Height: 54);
            Background: #8a6420;
            Padding: (Full: 2);

            Group {{
              Background: #1d2837;
              LayoutMode: Middle;

              Label #PcSlotValue {{
                Style: (FontSize: 13, RenderBold: true, TextColor: #E8A93B, HorizontalAlignment: Center);
              }}

              Label {{
                Text: "OR";
                Style: (FontSize: 10, RenderBold: true, TextColor: #878e9c, HorizontalAlignment: Center);
              }}
            }}
          }}

          Group {{ Anchor: (Width: 14); }}

          Group {{
            Anchor: (Width: 140);
            LayoutMode: Middle;

            Label {{
              Text: "Votre bourse";
              Style: (FontSize: 10, RenderBold: true, RenderUppercase: true, TextColor: {MUTED});
            }}

            Label #PcPurse {{
              Style: (FontSize: 15, RenderBold: true, TextColor: #cc3361);
            }}
          }}
        }}
'''
    info_bottom = f'''
        Label #PcCost {{
          Anchor: (Top: 8);
          Style: (FontSize: 13, RenderBold: true, TextColor: #878e9c, HorizontalAlignment: Center);
        }}
{button_row(("TextButton", "PcBtnBuy", "Acheter", 172), ("TextButton", "PcBtnRent", "Louer", 172), rid="PcRowDeal")}
{button_row(("TextButton", "PcBtnRenew", "Prolonger (+7 j)", 172), ("SecondaryTextButton", "PcBtnQuit", "Quitter", 140), rid="PcRowLease")}
        Label {{
          Anchor: (Top: 10);
          Text: "Echap : partir";
          Style: (FontSize: 12, TextColor: #5a6a7a, HorizontalAlignment: Center);
        }}
'''
    # ----- carte 2 : gestion -----
    own_top = title_block("PgTitle", "PgSubtitle", "PgTreasury")
    members = "".join(f'''
        Group #PgMem{n} {{
          Anchor: (Top: 4, Height: 32);
          LayoutMode: Left;
          Background: {ROW};
          Visible: false;
          Group {{ Anchor: (Width: 3); Background: {GOLD}; }}
          Label #PgMemName{n} {{
            FlexWeight: 1;
            Anchor: (Left: 8);
            Style: (FontSize: 12, RenderBold: true, TextColor: #e6edf5, VerticalAlignment: Center);
          }}
          $C.@SmallSecondaryTextButton #PgMemKick{n} {{
            @Text = "Retirer";
          }}
        }}
''' for n in range(MEMBERS))
    own_body = f'''
{section("Vente et location", 10, "PgSecSale")}{field_row("Proposer le bien", dropdown("PgMode"), "PgRowMode")}{field_row("Prix de vente", number("PgPrice"), "PgRowPrice")}{field_row("Loyer (7 jours)", number("PgRent"), "PgRowRent")}{field_row("Protection des blocs", dropdown("PgProtect"), "PgRowProtect")}
{section("Accès", 12, "PgSecAccess")}
        Label #PgAccessLocked {{
          Anchor: (Top: 6);
          Visible: false;
          Style: (FontSize: 12, TextColor: {MUTED}, HorizontalAlignment: Center, Wrap: true);
        }}
        Label #PgNoMember {{
          Anchor: (Top: 6);
          Visible: false;
          Text: "Personne d'autre n'a accès à ce bien.";
          Style: (FontSize: 12, TextColor: {MUTED}, HorizontalAlignment: Center);
        }}
{members}
        Group #PgRowInvite {{
          Anchor: (Top: 6, Height: 32);
          LayoutMode: Left;
          $C.@TextField #PgInvite {{
            FlexWeight: 1;
            Anchor: (Height: 28);
            PlaceholderText: "Nom d'un joueur connecté";
          }}
          Group {{ Anchor: (Width: 6); }}
          $C.@SmallSecondaryTextButton #PgBtnInvite {{
            @Text = "Inviter";
          }}
        }}

        Group {{ FlexWeight: 1; }}

        Group {{
          Anchor: (Height: 34, Bottom: 8);
          LayoutMode: Left;
          Label #PgRenter {{
            FlexWeight: 1;
            Style: (FontSize: 12, RenderBold: true, TextColor: {LABEL}, VerticalAlignment: Center, Wrap: true);
          }}
          $C.@SmallSecondaryTextButton #PgBtnEvict {{
            @Text = "Expulser";
          }}
        }}
'''
    own_bottom = f'''
        Label #PgHint {{
          Anchor: (Top: 8);
          Style: (FontSize: 13, RenderBold: true, TextColor: #878e9c, HorizontalAlignment: Center);
        }}
{button_row(("TextButton", "PgBtnSave", "Enregistrer", 246), rid="PgRowSave")}
        Group #PgRowUnlink {{
          Anchor: (Top: 6, Height: 32);
          LayoutMode: Center;
          $C.@SmallSecondaryTextButton #PgBtnUnlink {{
            @Text = "Délier la pancarte";
          }}
        }}
'''
    out = f'''$C = "../Common.ui";

// Pancarte de propriete (bloc {ITEM}), generee par scripts/territoire/gen_pancarte.py : voir ce script.
// Carte du bien (#PcCard) a gauche ; carte de gestion (#PgCard) a droite, seulement pour le proprietaire et les admins.
Group {{
  LayoutMode: Middle;
  Background: #05080d(0.55);

  Group {{
    Anchor: (Height: 921);
    LayoutMode: Center;
{card("PcCard", info_top, info_body, info_bottom)}
    Group #PgGap {{ Anchor: (Width: 24); Visible: false; }}
{card("PgCard", own_top, own_body, own_bottom)}
  }}
}}
'''
    path = os.path.join(UI, "Pancarte.ui")
    with open(path, "w", encoding="utf-8") as f:
        f.write(out)
    print("ecrit", path)


def link_page():
    body = f'''
        Label #PlState {{
          Anchor: (Top: 12);
          Style: (FontSize: 13, RenderBold: true, TextColor: {QUOTE}, Wrap: true, HorizontalAlignment: Center);
        }}
{section("Bien à présenter", 16)}
        Group {{
          Anchor: (Top: 10, Height: 30);
          LayoutMode: Center;
          $C.@DropdownBox #PlZone {{
            Anchor: (Width: 360, Height: 28);
            Style: @DefaultDropdownBoxStyle;
          }}
        }}

        Label {{
          Anchor: (Top: 10);
          Text: "Biens à moins de 48 blocs de la pancarte, les plus proches d'abord.";
          Style: (FontSize: 11, TextColor: {MUTED}, HorizontalAlignment: Center, Wrap: true);
        }}
'''
    top = title_block("PlTitle", "PlSubtitle", "PlFoot")
    bottom = f'''
{button_row(("TextButton", "PlBtnLink", "Lier la pancarte", 246), top=16)}
        Label {{
          Anchor: (Top: 10);
          Text: "Echap : plus tard";
          Style: (FontSize: 12, TextColor: #5a6a7a, HorizontalAlignment: Center);
        }}
'''
    out = f'''$C = "../Common.ui";

// Lien d'une pancarte de propriete a sa zone, genere par scripts/territoire/gen_pancarte.py.
Group {{
  LayoutMode: Middle;
  Background: #05080d(0.55);

  Group {{
    Anchor: (Height: 693);
    LayoutMode: Center;
{card("PlCard", top, body, bottom, body_height=240)}
  }}
}}
'''
    path = os.path.join(UI, "PancarteLien.ui")
    with open(path, "w", encoding="utf-8") as f:
        f.write(out)
    print("ecrit", path)


if __name__ == "__main__":
    os.makedirs(UI, exist_ok=True)
    old = os.path.join(RES, "Server", "Item", "Items", "Eldanior", "Territoire", ITEM + ".json")
    if os.path.exists(old):                                   # 2026-11-12 : seule l'affiche reste (block() n'est plus appele)
        os.remove(old)
    poster()
    emblem()
    page()
    link_page()
