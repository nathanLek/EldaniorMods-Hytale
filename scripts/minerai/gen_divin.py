"""Minerai Divin : toute la chaine, de la veine dans la roche aux artefacts (textures, icones, items, generation du monde).

  Veines         Ore_Divin_<Roche>          6 roches, bloc lumineux, lache 1 minerai brut et la pierre de la roche
  Roches fissurees Ore_Divin_<Roche>_Cracked  entourent les veines, 25 % de chance de lacher un minerai brut
  Minerai brut   Ore_Divin
  Lingot         Ingredient_Bar_Divin       2 minerais bruts au four
  Bloc Divin     Metal_Divin                4 lingots, a la Forge Divine

Generation : modificateur officiel du generateur Hytale (Server/WorldGen/Modifier, generateur OreCluster), sans ecraser aucun
fichier vanilla. Chaque roche a son propre type de "grotte" : BlockMask = cette roche, Blocks = sa variante, donc une veine a
toujours la bonne texture. Zones hautes uniquement. Seuls les chunks pas encore generes recoivent le minerai.

La forge est dans scripts/minerai/gen_forge_divine.py, les recettes des artefacts dans scripts/artefacts/artefact_recipes.py.
Tout part d'assets vanilla (minerai de Mithril, lingot, metal de bronze) recolores par une rampe de couleurs.

Usage : python3 scripts/minerai/gen_divin.py <src/main/resources> [dossier_apercu]
"""
import json, math, os, random, sys

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "rune_aura_icons"))
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import png  # noqa: E402
from vanilla_assets import ZIP, vanilla  # noqa: E402

RES = sys.argv[1]
PREVIEW = sys.argv[2] if len(sys.argv) > 2 else None
COMMON = os.path.join(RES, "Common")
SERVER = os.path.join(RES, "Server")

QUALITY = "Divin"
LEVEL = 800
CATEGORY = "Eldanior_System.Autres"
GLOW = "#bdf"                            # lumiere emise par les blocs (bleu ciel)
PARTICLE = "#9fe8ff"
SPARK = {"SystemId": "Block_Gem_Sparks", "Color": "#ffeeb0", "TargetEntityPart": "Entity"}

# Rampe : ombres bleu nuit, corps cyan, reflets ivoire et or, eclats blancs
RAMP = ((10, 44, 92), (34, 128, 190), (120, 224, 236), (246, 236, 178), (255, 214, 110), (255, 255, 244))
ICON_PROPS = {"Scale": 0.58823, "Rotation": [22.5, 45, 22.5], "Translation": [0, -13.5]}

ROCKS = {   # nom -> (bloc de roche, texture, cobble lache, nom affiche)
    "Stone": ("Rock_Stone", "Rock_Stone", "Rock_Stone_Cobble", "Pierre"),
    "Basalt": ("Rock_Basalt", "Rock_Basalt", "Rock_Basalt_Cobble", "Basalte"),
    "Slate": ("Rock_Slate", "Rock_Slate", "Rock_Slate_Cobble", "Ardoise"),
    "Shale": ("Rock_Shale", "Rock_Shale", "Rock_Shale_Cobble", "Schiste"),
    "Volcanic": ("Rock_Volcanic", "Rock_Volcanic", "Rock_Volcanic_Cobble", "Roche Volcanique"),
    "Magma": ("Rock_Magma_Cooled", "Rock_Magma_Cooled", "Rock_Magma_Cooled", "Magma Refroidi"),
}
METALS = {"": ("Bloc Divin", 4)}    # suffixe vanilla -> (nom affiche, cout en lingots)
METAL_TEXTURES = ("Metal_Bronze", "Metal_Bronze02")


def clamp(v):
    return max(0, min(255, int(round(v))))


def mix(a, b, t):
    t = max(0.0, min(1.0, t))
    return [a[i] + (b[i] - a[i]) * t for i in range(3)]


def ramp(v):
    v = max(0.0, min(0.999, v)) * (len(RAMP) - 1)
    i = int(v)
    return mix(RAMP[i], RAMP[i + 1], v - i)


def write_png(rel, w, h, px):
    path = os.path.join(COMMON, *rel.split("/"))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    png.write(path, w, h, px)


def write_json(rel, data):
    path = os.path.join(SERVER, *rel.split("/"))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


def recolor(px, contrast=1.0, bias=0.0):
    """Remplace chaque couleur par la rampe selon sa luminance, etiree sur toute la plage de l'image ; bias > 0 tire vers l'or et le blanc."""
    lums = [(p[0] * 0.3 + p[1] * 0.59 + p[2] * 0.11) for row in px for p in row if p[3] > 0]
    lo, hi = min(lums), max(lums)
    out = []
    for row in px:
        line = []
        for p in row:
            t = ((p[0] * 0.3 + p[1] * 0.59 + p[2] * 0.11) - lo) / max(1.0, hi - lo)
            c = ramp(0.5 + bias + (t - 0.5) * contrast)
            line.append([clamp(c[0]), clamp(c[1]), clamp(c[2]), p[3]])
        out.append(line)
    return out


def cracks(px, seed):
    """Fissures lumineuses sur une texture de roche ; elles rebouclent sur les bords pour que le bloc se repete sans couture."""
    rnd = random.Random(seed)
    h, w = len(px), len(px[0])
    out = [[list(p) for p in row] for row in px]
    field = {}

    def hit(x, y, v):
        k = (int(x) % w, int(y) % h)
        field[k] = max(field.get(k, 0), v)

    for _ in range(4):
        x, y, a = rnd.uniform(0, w), rnd.uniform(0, h), rnd.uniform(0, 6.28)
        for _ in range(rnd.randint(14, 24)):
            a += rnd.uniform(-0.75, 0.75)
            x, y = x + math.cos(a), y + math.sin(a)
            hit(x, y, 1.0)
    for (x, y), v in list(field.items()):                         # halo cyan autour du trait
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            hit(x + dx, y + dy, 0.45)
    for (x, y), v in field.items():
        p = out[y][x]
        c = ramp(0.86) if v > 0.9 else mix(p, ramp(0.3), 0.75)
        p[0], p[1], p[2] = clamp(c[0]), clamp(c[1]), clamp(c[2])
    return out


# ---------------------------------------------------------------------------------------------------------------------
# Rendu isometrique des icones (64 px, supersampling)
# ---------------------------------------------------------------------------------------------------------------------
def qrot(q, v):
    u = (q["x"], q["y"], q["z"])
    c1 = (u[1] * v[2] - u[2] * v[1], u[2] * v[0] - u[0] * v[2], u[0] * v[1] - u[1] * v[0])
    c2 = (u[1] * c1[2] - u[2] * c1[1], u[2] * c1[0] - u[0] * c1[2], u[0] * c1[1] - u[1] * c1[0])
    return tuple(v[i] + 2 * (q["w"] * c1[i] + c2[i]) for i in range(3))


def cube_quads(tex, height=32):
    """Cube de bloc (32 unites), base au sol."""
    s, H = 16.0, float(height)
    w, h = len(tex[0]), len(tex)
    uv = [(0, 0), (w, 0), (w, h), (0, h)]
    faces = (
        ([(-s, H, -s), (s, H, -s), (s, H, s), (-s, H, s)], 1.08),      # dessus
        ([(-s, H, s), (s, H, s), (s, 0, s), (-s, 0, s)], 0.9),         # face avant
        ([(-s, H, -s), (-s, H, s), (-s, 0, s), (-s, 0, -s)], 0.68),    # face gauche, celle que voit la camera a 45 degres
    )
    return [(verts, uv, tex, light) for verts, light in faces]


def model_quads(tex):
    """Quads du modele vanilla Ore_Large, poses dans le meme repere que le cube."""
    model = json.loads(ZIP.read("Common/Resources/Ores/Ore_Large.blockymodel"))
    out = []
    for root in model["nodes"]:
        for n in root["children"]:
            s = n["shape"]
            size, st, q = s["settings"]["size"], s["stretch"], n["orientation"]
            hx, hy, hz = size["x"] / 2.0, size["y"] / 2.0, size["z"] / 2.0
            pos = n["position"]
            faces = {
                "front": ([(-hx, hy, hz), (hx, hy, hz), (hx, -hy, hz), (-hx, -hy, hz)], (size["x"], size["y"])),
                "back": ([(hx, hy, -hz), (-hx, hy, -hz), (-hx, -hy, -hz), (hx, -hy, -hz)], (size["x"], size["y"])),
                "right": ([(hx, hy, hz), (hx, hy, -hz), (hx, -hy, -hz), (hx, -hy, hz)], (size["z"], size["y"])),
                "left": ([(-hx, hy, -hz), (-hx, hy, hz), (-hx, -hy, hz), (-hx, -hy, -hz)], (size["z"], size["y"])),
                "top": ([(-hx, hy, -hz), (hx, hy, -hz), (hx, hy, hz), (-hx, hy, hz)], (size["x"], size["z"])),
                "bottom": ([(-hx, -hy, hz), (hx, -hy, hz), (hx, -hy, -hz), (-hx, -hy, -hz)], (size["x"], size["z"])),
            }
            for face, (verts, (w, h)) in faces.items():
                o = s["textureLayout"].get(face, {}).get("offset", {"x": 0, "y": 0})
                world = []
                for v in verts:
                    r = qrot(q, (v[0] * st["x"], v[1] * st["y"], v[2] * st["z"]))
                    world.append((pos["x"] + r[0], pos["y"] + r[1], pos["z"] + r[2]))
                uv = [(o["x"], o["y"]), (o["x"] + w, o["y"]), (o["x"] + w, o["y"] + h), (o["x"], o["y"] + h)]
                out.append((world, uv, tex, 1.0))
    return out


def iso(quads, size=256, fit=None):
    yaw, pitch = math.radians(45), math.radians(26)
    cy, sy, cp, sp = math.cos(yaw), math.sin(yaw), math.cos(pitch), math.sin(pitch)

    def project(v):
        x = v[0] * cy + v[2] * sy
        z = -v[0] * sy + v[2] * cy
        return x, v[1] * cp - z * sp, v[1] * sp + z * cp

    pts = [project(v) for q in quads for v in q[0]]
    x0, x1 = min(p[0] for p in pts), max(p[0] for p in pts)
    y0, y1 = min(p[1] for p in pts), max(p[1] for p in pts)
    scale = fit or size * 0.86 / max(x1 - x0, y1 - y0)
    mx, my = (x0 + x1) / 2, (y0 + y1) / 2
    img = [[[0, 0, 0, 0] for _ in range(size)] for _ in range(size)]
    zb = [[-1e9] * size for _ in range(size)]

    def tri(a, b, c, ta, tb, tc, tex, light):
        area = (b[0] - a[0]) * (c[1] - a[1]) - (b[1] - a[1]) * (c[0] - a[0])
        if abs(area) < 1e-6:
            return
        for y in range(max(0, int(min(a[1], b[1], c[1]))), min(size, int(max(a[1], b[1], c[1])) + 2)):
            for x in range(max(0, int(min(a[0], b[0], c[0]))), min(size, int(max(a[0], b[0], c[0])) + 2)):
                px, py = x + 0.5, y + 0.5
                w0 = ((b[0] - px) * (c[1] - py) - (b[1] - py) * (c[0] - px)) / area
                w1 = ((c[0] - px) * (a[1] - py) - (c[1] - py) * (a[0] - px)) / area
                w2 = 1 - w0 - w1
                if w0 < -1e-4 or w1 < -1e-4 or w2 < -1e-4:
                    continue
                d = w0 * a[2] + w1 * b[2] + w2 * c[2]
                if d <= zb[y][x]:
                    continue
                u, v = w0 * ta[0] + w1 * tb[0] + w2 * tc[0], w0 * ta[1] + w1 * tb[1] + w2 * tc[1]
                col = tex[min(len(tex) - 1, max(0, int(v)))][min(len(tex[0]) - 1, max(0, int(u)))]
                if col[3] < 128:
                    continue
                zb[y][x] = d
                img[y][x] = [clamp(col[0] * light), clamp(col[1] * light), clamp(col[2] * light), 255]

    for verts, uv, tex, light in quads:
        s = [project(v) for v in verts]
        s = [(size / 2 + (p[0] - mx) * scale, size / 2 - (p[1] - my) * scale, p[2]) for p in s]
        tri(s[0], s[1], s[2], uv[0], uv[1], uv[2], tex, light)
        tri(s[0], s[2], s[3], uv[0], uv[2], uv[3], tex, light)
    return img


def downscale(img, f=4):
    n = len(img) // f
    out = []
    for y in range(n):
        row = []
        for x in range(n):
            px = [img[y * f + j][x * f + i] for j in range(f) for i in range(f)]
            a = sum(p[3] for p in px)
            row.append([0, 0, 0, 0] if a == 0 else [sum(p[k] * p[3] for p in px) / a for k in range(3)] + [a / len(px)])
        out.append(row)
    return out


def icon(name, quads):
    write_png("Icons/ItemsGenerated/%s.png" % name, 64, 64, downscale(iso(quads)))


# ---------------------------------------------------------------------------------------------------------------------
# Items
# ---------------------------------------------------------------------------------------------------------------------
def base_item(name, description, icon_name, categories, tags):
    item = {
        "TranslationProperties": {"Name": name},
        "Quality": QUALITY,
        "ItemLevel": LEVEL,
        "Categories": [CATEGORY] + categories,
        "Icon": "Icons/ItemsGenerated/%s.png" % icon_name,
        "IconProperties": dict(ICON_PROPS),
        "Tags": tags,
    }
    if description:
        item["TranslationProperties"]["Description"] = description
    return item


def vein_block(rock, spec):
    block, texture, cobble, label = spec
    item = base_item("Minerai Divin (%s)" % label, "Veine céleste. Une pioche d'adamantite ou meilleure est requise pour l'extraire.",
                     "Ore_Divin_" + rock, ["Blocks.Ores"], {"Type": ["Ore"], "Family": ["Divin"]})
    item.update({"SubCategory": "Ore", "PlayerAnimationsId": "Block", "ItemSoundSetId": "ISS_Blocks_Stone", "MaxStack": 25})
    item["BlockType"] = {
        "Material": "Solid",
        "DrawType": "CubeWithModel",
        "CustomModel": "Resources/Ores/Ore_Large.blockymodel",
        "CustomModelTexture": [{"Texture": "Resources/Ores/Ore_Textures/Divin.png", "Weight": 1}],
        "Group": "Stone",
        "Flags": {},
        "RandomRotation": "YawStep90",
        "Textures": [{"Weight": 1, "All": "BlockTextures/%s.png" % texture}],
        "Gathering": {"Breaking": {"GatherType": "Rocks", "Quality": 5, "DropList": {"Container": {"Type": "Multiple", "Containers": [
            {"Type": "Single", "Item": {"ItemId": "Ore_Divin"}}, {"Type": "Single", "Item": {"ItemId": cobble}}]}}}},
        "Light": {"Color": GLOW},
        "Particles": [dict(SPARK)],
        "BlockSoundSetId": "Ore",
        "PhysicalMaterialId": "Stone",
        "BlockParticleSetId": "Ore",
        "ParticleColor": PARTICLE,
        "TextureComputedColor": "#7FD6E8",
    }
    return item


def cracked_block(rock, spec):
    block, texture, cobble, label = spec
    item = base_item("Roche Fissurée Divine (%s)" % label, "Roche veinée de lumière. Il arrive qu'elle cache un fragment de minerai.",
                     "Ore_Divin_%s_Cracked" % rock, ["Blocks.Ores"], {"Type": ["Ore"], "Family": ["Divin"]})
    item["Parent"] = block
    item["BlockType"] = {
        "Textures": [{"All": "BlockTextures/%s_Divin_Cracks.png" % texture, "Weight": 1}],
        "Gathering": {"Breaking": {"GatherType": "Rocks", "DropList": {"Container": {"Type": "Multiple", "Containers": [
            {"Weight": 25, "$Comment": "Weight = pourcentage de chance", "Type": "Single", "Item": {"ItemId": "Ore_Divin"}},
            {"Type": "Single", "Item": {"ItemId": cobble}}]}}}},
        "Light": {"Color": "#468"},
    }
    return item


def metal_block(suffix, label, cost):
    """Copie du bloc de bronze vanilla (les dalles et escaliers ont des etats complexes), tous les noms remplaces."""
    text = ZIP.read("Server/Item/Items/Metal/Bronze/Metal_Bronze%s.json" % suffix).decode("utf-8").replace("Metal_Bronze", "Metal_Divin")
    item = json.loads(text)
    item.pop("Set", None)
    item.pop("ItemLevel", None)
    item["TranslationProperties"] = {"Name": label}
    item["Quality"] = QUALITY
    item["ItemLevel"] = LEVEL
    item["Categories"] = [CATEGORY, "Blocks.Metal"]
    item["Tags"] = {"Type": ["Metal"], "Family": ["Divin"]}
    item["ResourceTypes"] = []
    item["Recipe"] = {"Input": [{"ItemId": "Ingredient_Bar_Divin", "Quantity": cost}], "OutputQuantity": 1, "TimeSeconds": 4,
                      "BenchRequirement": [{"Type": "Crafting", "Id": "Forge_Divine", "Categories": ["Forge_Blocs"]}]}
    bt = item["BlockType"]
    bt["ParticleColor"] = PARTICLE
    bt["Light"] = {"Color": "#68a"}
    bt.pop("TextureComputedColor", None)
    return item


def worldgen():
    """Un type de 'grotte' par roche : le masque ne remplace que cette roche, la veine a donc toujours la bonne texture."""
    operations = []
    for rock, (block, _, _, _) in ROCKS.items():
        operations.append({"Type": "Add", "Content": {
            "Type": "CaveType",
            "Name": "Minerai_Divin_" + rock,
            "PointGrid": {"Seed": "Eldanior-Minerai-Divin-" + rock, "Scale": 0.009, "Jitter": 0.75, "Density": {"Min": 0.0, "Max": 1.0}},
            "HeightRange": {"Min": 8, "Max": 90},
            "BlockMask": {"Include": {"Blocks": [block]}},
            "Generator": {
                "Type": "OreCluster",
                "Blocks": [{"Block": "Ore_Divin_" + rock, "Weight": 35}, {"Block": "Ore_Divin_%s_Cracked" % rock, "Weight": 65}],
                "Radius": {"Min": 1, "Max": 2},
                "Length": {"Min": 3, "Max": 6},
                "Repetitions": {"Min": 2, "Max": 3},
            },
        }})
    return {"Target": {"Root": "Default", "Rules": ["*Zone3_Tier3*", "*Zone4_Tier4*", "*Zone4_Tier5*"]}, "Operations": operations}


# ---------------------------------------------------------------------------------------------------------------------
# Generation
# ---------------------------------------------------------------------------------------------------------------------
w, h, px = vanilla("Common/Resources/Ores/Ore_Textures/Mithril.png")
ore_tex = recolor(px, 1.0, 0.2)
write_png("Resources/Ores/Ore_Textures/Divin.png", w, h, ore_tex)
w, h, px = vanilla("Common/Resources/Materials/Ingot_Textures/Mithril.png")
write_png("Resources/Materials/Ingot_Textures/Divin.png", w, h, recolor(px, 1.1))

rock_textures = {}
for rock, (block, texture, cobble, label) in ROCKS.items():
    w, h, px = vanilla("Common/BlockTextures/%s.png" % texture)
    rock_textures[rock] = px
    crack = cracks(px, sum(ord(c) for c in rock))
    write_png("BlockTextures/%s_Divin_Cracks.png" % texture, w, h, crack)
    write_json("Item/Items/Ore/Divin/Ore_Divin_%s.json" % rock, vein_block(rock, ROCKS[rock]))
    write_json("Item/Items/Ore/Divin/Ore_Divin_%s_Cracked.json" % rock, cracked_block(rock, ROCKS[rock]))
    icon("Ore_Divin_" + rock, cube_quads(px) + model_quads(ore_tex))
    icon("Ore_Divin_%s_Cracked" % rock, cube_quads(crack))

ore = base_item("Minerai Divin", "Fragment tombé des cieux. Se fond au four pour obtenir du Lingot Divin.", "Ore_Divin", ["Blocks.Ores"], {"Type": ["Ore"], "Family": ["Divin"]})
ore.update({"SubCategory": "Ore", "Model": "Resources/Ores/Ore_Large.blockymodel", "Texture": "Resources/Ores/Ore_Textures/Divin.png", "PlayerAnimationsId": "Block",
            "ItemEntity": {"ParticleSystemId": None}, "MaxStack": 25, "ItemSoundSetId": "ISS_Blocks_Stone", "DropOnDeath": True})
ore["IconProperties"] = {"Scale": 0.57323, "Translation": [-3.6, -16.2], "Rotation": [22.5, 45, 22.5]}
write_json("Item/Items/Ore/Divin/Ore_Divin.json", ore)
icon("Ore_Divin", model_quads(ore_tex))

bar = base_item("Lingot Divin", "Métal céleste. Indispensable pour forger les artefacts à la Forge Divine.", "Ingredient_Bar_Divin", ["Items"], {"Type": ["Ingredient"]})
bar.update({"Model": "Resources/Materials/Ingot.blockymodel", "Texture": "Resources/Materials/Ingot_Textures/Divin.png", "PlayerAnimationsId": "Item", "ItemEntity": {"ParticleSystemId": None},
            "ItemSoundSetId": "ISS_Items_Ingots", "DropOnDeath": True})
bar["IconProperties"] = {"Scale": 1, "Translation": [0, -3], "Rotation": [22.5, 45, 22.5]}
bar["Recipe"] = {"Input": [{"ItemId": "Ore_Divin", "Quantity": 2}], "BenchRequirement": [{"Type": "Processing", "Id": "Furnace"}], "OutputQuantity": 1, "TimeSeconds": 45}
write_json("Item/Items/Ingredient/Bar/Ingredient_Bar_Divin.json", bar)
w, h, px = vanilla("Common/Icons/ItemsGenerated/Ingredient_Bar_Mithril.png")
write_png("Icons/ItemsGenerated/Ingredient_Bar_Divin.png", w, h, recolor(px, 1.15))

for name in METAL_TEXTURES:
    w, h, px = vanilla("Common/BlockTextures/%s.png" % name)
    write_png("BlockTextures/%s.png" % name.replace("Metal_Bronze", "Metal_Divin"), w, h, recolor(px, 1.0, 0.08))
for suffix, (label, cost) in METALS.items():
    write_json("Item/Items/Metal/Divin/Metal_Divin%s.json" % suffix, metal_block(suffix, label, cost))
    w, h, px = vanilla("Common/Icons/ItemsGenerated/Metal_Bronze%s.png" % suffix)
    write_png("Icons/ItemsGenerated/Metal_Divin%s.png" % suffix, w, h, recolor(px, 1.0, 0.08))

write_json("WorldGen/Modifier/Minerai_Divin.json", worldgen())

if PREVIEW:
    os.makedirs(PREVIEW, exist_ok=True)
    names = ["Ore_Divin", "Ingredient_Bar_Divin"] + ["Ore_Divin_" + r for r in ROCKS] + ["Ore_Divin_%s_Cracked" % r for r in ROCKS] + ["Metal_Divin" + s for s in METALS]
    cols, Z = 7, 3
    rows = (len(names) + cols - 1) // cols
    W, H = cols * 64 * Z, rows * 64 * Z
    sheet = [[[30, 34, 46, 255] for _ in range(W)] for _ in range(H)]
    for i, name in enumerate(names):
        _, _, ic = png.read(os.path.join(COMMON, "Icons", "ItemsGenerated", name + ".png"))
        ox, oy = (i % cols) * 64 * Z, (i // cols) * 64 * Z
        for y in range(64):
            for x in range(64):
                c = ic[y][x]
                a = c[3] / 255.0
                for j in range(Z):
                    for k in range(Z):
                        p = sheet[oy + y * Z + j][ox + x * Z + k]
                        sheet[oy + y * Z + j][ox + x * Z + k] = [c[t] * a + p[t] * (1 - a) for t in range(3)] + [255]
    png.write(os.path.join(PREVIEW, "divin_icones.png"), W, H, sheet)
print("Minerai Divin genere : %d roches, %d blocs de metal" % (len(ROCKS), len(METALS)))
