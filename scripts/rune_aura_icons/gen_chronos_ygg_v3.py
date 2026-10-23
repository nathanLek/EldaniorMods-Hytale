"""Assets v3 : Sablier de Chronos (le temps en vert : cadran geant, aiguilles visibles sous le cadran, silhouettes
remanentes, stase des ennemis) et Eveil d'Yggdrasil (brume toxique, benediction, pieges, vague de poison).
Usage : python3 gen_chronos_ygg_v3.py <dossier_resources>   (contient Common/ et Server/)"""
import copy, json, math, os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import png

ID = {"x": 0, "y": 0, "z": 0, "w": 1}
_id = [1]
def q_axis(axis, deg):
    s = math.sin(math.radians(deg) / 2)
    return {"x": round(axis[0] * s, 6), "y": round(axis[1] * s, 6), "z": round(axis[2] * s, 6), "w": round(math.cos(math.radians(deg) / 2), 6)}
def face(uv): return {"offset": {"x": uv[0], "y": uv[1]}, "mirror": {"x": False, "y": False}, "angle": 0}
def box(name, pos, size, uv, orient=ID, bright=False):
    """Boite unie (zone 16x16) ; taille de base <= 16, le reste en etirement."""
    _id[0] += 1
    base = [max(1, min(16, round(s))) for s in size]
    return {"id": str(_id[0]), "name": name, "position": {"x": round(pos[0], 3), "y": round(pos[1], 3), "z": round(pos[2], 3)}, "orientation": orient,
            "shape": {"type": "box", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {k: round(size[i] / base[i], 4) for i, k in enumerate("xyz")},
                      "settings": {"size": {"x": base[0], "y": base[1], "z": base[2]}}, "textureLayout": {k: face(uv) for k in ("top", "bottom", "front", "back", "left", "right")},
                      "unwrapMode": "custom", "visible": True, "doubleSided": False, "shadingMode": "fullbright" if bright else "standard"}, "children": []}
def group(name, children, pos=(0, 0, 0)):
    _id[0] += 1
    return {"id": str(_id[0]), "name": name, "position": {"x": pos[0], "y": pos[1], "z": pos[2]}, "orientation": ID,
            "shape": {"type": "none", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": 1, "y": 1, "z": 1}, "settings": {"isPiece": False},
                      "visible": True, "doubleSided": False, "shadingMode": "standard", "unwrapMode": "custom", "textureLayout": {}}, "children": children}
def save(path, root): json.dump({"nodes": [root], "lod": "auto"}, open(path, "w"), indent=2)
def spin_anim(path, turns_by_node, axis=(0, 1, 0), steps=12):
    json.dump({"formatVersion": 1, "duration": 600, "holdLastKeyframe": False, "nodeAnimations": {
        n: {"position": [], "orientation": [{"time": round(600 * k / steps), "delta": q_axis(axis, 360 * t * k / steps), "interpolationType": "linear"} for k in range(steps + 1)],
            "shapeStretch": [], "shapeVisible": [], "shapeUvOffset": []} for n, t in turns_by_node.items()}}, open(path, "w"), indent=2)
def solid(px, x0, y0, col):
    for y in range(16):
        for x in range(16): px[y0 + y][x0 + x] = list(col)

res = sys.argv[1]
COMMON, SERVER = os.path.join(res, "Common"), os.path.join(res, "Server")
T = os.path.join(COMMON, "Items/Projectiles/Chronos")
GREEN, GREEN_HI, GREEN_DEEP, GOLD = (90, 255, 160), (200, 255, 220), (10, 70, 45), (230, 210, 110)

def dump(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    json.dump(data, open(path, "w"), indent=2, ensure_ascii=False)

# =============================================================================================================
# Chronos : cadran 256x256 (emeraude translucide, chiffres romains en barres, graduations, engrenages, runes)
# =============================================================================================================
N = 256; c = (N - 1) / 2
ROMAN = {1: "I", 2: "II", 3: "III", 4: "IV", 5: "V", 6: "VI", 7: "VII", 8: "VIII", 9: "IX", 10: "X", 11: "XI", 12: "XII"}
def roman_hit(txt, lx, ly):
    """Chiffre romain dessine en traits dans une case [-1,1]x[-1,1] (lx le long du texte)."""
    n = len(txt); w = 2.0 / n
    for k, ch in enumerate(txt):
        x0 = -1 + k * w; u = (lx - x0) / w * 2 - 1
        if not -1 <= u <= 1: continue
        if ch == "I" and abs(u) < 0.28: return True
        if ch == "V" and abs(abs(u) - (ly + 1) / 2 * 0.8) < 0.25: return True
        if ch == "X" and (abs(u - ly * 0.8) < 0.25 or abs(u + ly * 0.8) < 0.25): return True
    return False
px = []
for y in range(N):
    row = []
    for x in range(N):
        dx, dy = (x - c) / c, (y - c) / c
        d = math.hypot(dx, dy); a = math.atan2(dy, dx)
        if d > 1: row.append([0, 0, 0, 0]); continue
        col, al = [20, 90, 60], 0.22 + 0.12 * (1 - d)                                    # fond emeraude translucide
        for r, w_ in ((0.985, 0.012), (0.93, 0.008), (0.66, 0.008), (0.30, 0.006)):        # anneaux
            if abs(d - r) < w_: col, al = list(GOLD), 1.0
        tick = (a / (2 * math.pi) * 60) % 1
        if 0.90 < d < 0.925 and (tick < 0.1 or tick > 0.9): col, al = list(GREEN_HI), 1.0  # minutes
        hour = (a / (2 * math.pi) * 12) % 1
        if 0.86 < d < 0.925 and (hour < 0.025 or hour > 0.975): col, al = list(GOLD), 1.0  # heures
        # chiffres romains entre 0.72 et 0.84
        hi = round(a / (2 * math.pi) * 12) % 12
        ha = hi * 2 * math.pi / 12
        if 0.72 < d < 0.84:
            # repere local : le long de la tangente / vers le centre
            ex, ey = math.cos(ha), math.sin(ha)
            px_, py_ = dx - ex * 0.78, dy - ey * 0.78
            lx = (-px_ * ey + py_ * ex) / 0.075; ly = (px_ * ex + py_ * ey) / 0.05
            num = (hi + 3) % 12 or 12
            if abs(ly) <= 1 and roman_hit(ROMAN[num], lx / (0.35 * len(ROMAN[num])), ly): col, al = list(GREEN_HI), 1.0
        gear = 0.5 + 0.5 * math.cos(a * 20)
        if 0.40 < d < 0.47 + 0.035 * gear: col, al = [70, 200, 130], 0.85                    # engrenage
        gear2 = 0.5 + 0.5 * math.cos(a * 10 + 1)
        if 0.16 < d < 0.21 + 0.03 * gear2: col, al = list(GOLD), 0.9
        if 0.53 < d < 0.62 and int((a + math.pi) / (2 * math.pi) * 36) % 2 == 0 and abs(((a + math.pi) / (2 * math.pi) * 36) % 1 - 0.5) < 0.18:
            col, al = [120, 255, 180], 0.75                                                 # runes du temps
        if d < 0.06: col, al = list(GREEN_HI), 1.0
        row.append([int(v) for v in col] + [int(255 * al)])
    px.append(row)
png.write(os.path.join(T, "Clock_Face.png"), N, N, px)
# Cadran : quad 256 etire a 128 unites (echelle 35 -> 70 blocs de diametre = la zone), double face
dial = group("Origin", [{
    "id": "900", "name": "Circle", "position": {"x": 0, "y": 0, "z": 0}, "orientation": {"x": 0, "y": 0, "z": 0, "w": 1},
    "shape": {"type": "quad", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": 0.5, "y": 0.5, "z": 1}, "settings": {"size": {"x": N, "y": N}, "normal": "+Y"},
              "visible": True, "doubleSided": True, "shadingMode": "fullbright", "unwrapMode": "custom",
              "textureLayout": {"front": {"offset": {"x": 0, "y": 0}, "mirror": {"x": False, "y": False}, "angle": 0}}}, "children": []}])
save(os.path.join(T, "Dial.blockymodel"), dial)

# Atlas 32x32 : or (0,0), vert lumineux (16,0), verre vert (0,16), sable du temps (16,16)
atlas = [[[0, 0, 0, 0] for _ in range(32)] for _ in range(32)]
solid(atlas, 0, 0, (240, 215, 120, 255)); solid(atlas, 16, 0, (110, 255, 170, 255))
solid(atlas, 0, 16, (170, 255, 210, 150)); solid(atlas, 16, 16, (120, 255, 160, 255))
png.write(os.path.join(T, "Chronos.png"), 32, 32, atlas)
GOLD_UV, GLOW_UV = (0, 0), (16, 0)

# Aiguilles (echelle 35 : 1 unite = 0.55 bloc), SOUS le cadran (y negatif) pour etre vues depuis le sol
def hand(name, length, width, y, uv, tip):
    parts = [box(name + "_blade", (length / 2, y, 0), (length, 1.2, width), uv, bright=True),
             box(name + "_tip", (length, y, 0), (tip, 1.4, tip), uv, q_axis((0, 1, 0), 45), bright=True),
             box(name + "_tail", (-length * 0.18, y, 0), (length * 0.36, 1.2, width * 0.7), uv, bright=True),
             box(name + "_weight", (-length * 0.36, y, 0), (width * 1.6, 1.4, width * 1.6), GOLD_UV, q_axis((0, 1, 0), 45), bright=True)]
    return group(name, parts)
hands = group("Origin", [hand("Seconds", 60, 1.2, -1.2, GLOW_UV, 3), hand("Minute", 54, 3.5, -2.6, GOLD_UV, 7),
                         hand("Hour", 36, 5.5, -4.0, GLOW_UV, 9),
                         box("Hub", (0, -5, 0), (9, 2, 9), GOLD_UV, q_axis((0, 1, 0), 45), bright=True),
                         box("Hub_gem", (0, -6.3, 0), (5, 1.5, 5), GLOW_UV, bright=True)])
save(os.path.join(T, "Hands.blockymodel"), hands)
spin_anim(os.path.join(T, "Hands_Spin.blockyanim"), {"Seconds": -6, "Minute": -1, "Hour": -1 / 12}, steps=24)
spin_anim(os.path.join(T, "Hands_Rewind.blockyanim"), {"Seconds": 24, "Minute": 10, "Hour": 3}, steps=24)

# Glyphe d'horloge (particule de stase au-dessus des ennemis figes)
G = 64; gc = (G - 1) / 2
glyph = []
for y in range(G):
    row = []
    for x in range(G):
        dx, dy = (x - gc) / gc, (y - gc) / gc; d = math.hypot(dx, dy); a = math.atan2(dy, dx)
        k = 0.0
        if abs(d - 0.88) < 0.07: k = 1.0
        if 0.62 < d < 0.78 and ((a / (2 * math.pi) * 12) % 1 < 0.08 or (a / (2 * math.pi) * 12) % 1 > 0.92): k = 1.0
        if abs(dx) < 0.06 and -0.6 < dy < 0.05: k = 1.0                                           # aiguille des minutes
        if abs(dy) < 0.06 and 0 < dx < 0.42: k = 1.0                                              # aiguille des heures
        if d < 0.1: k = 1.0
        glow = max(0.0, 1 - d) * 0.25
        row.append([255, 255, 255, int(255 * min(1.0, k + glow))])
    glyph.append(row)
os.makedirs(os.path.join(COMMON, "Particles/Textures/Eldanior"), exist_ok=True)
png.write(os.path.join(COMMON, "Particles/Textures/Eldanior/Chronos_Glyph.png"), G, G, glyph)

# ----- ModelAssets du cadran et des aiguilles : echelle 35 (70 blocs = diametre de la zone), lumiere verte -----
for name, model in (("Chronos_Clock", "Items/Projectiles/Chronos/Dial.blockymodel"), ("Chronos_Hands", None), ("Chronos_Hands_Rewind", None)):
    p = os.path.join(SERVER, "Models/Projectiles/Abilities", name + ".json")
    m = json.load(open(p))
    if model: m["Model"] = model
    m["MinScale"] = m["MaxScale"] = 35
    if name == "Chronos_Clock":
        m["Light"] = {"Color": "#7dffb0", "Radius": 24}
        m["$Comment"] = "Cadran geant de l'horloge de Chronos (70 blocs = la zone), au-dessus de la zone. HitBox large en X : le serveur masque (LOD) les entites a hitbox trop petite."
    dump(p, m)
# Silhouette remanente : porteur invisible (le code lui donne le modele du lanceur)
dump(os.path.join(SERVER, "Models/Projectiles/Abilities/Chronos_Ghost.json"), {
    "$Comment": "Porteur des silhouettes remanentes de Chronos : modele minuscule remplace par celui du lanceur par le code.",
    "Model": "Items/Projectiles/Chronos/Hourglass.blockymodel", "Texture": "Items/Projectiles/Chronos/Chronos.png",
    "MinScale": 0.01, "MaxScale": 0.01,
    "HitBox": {"Max": {"X": 0.4, "Y": 1.8, "Z": 0.4}, "Min": {"X": -0.4, "Y": 0, "Z": -0.4}}})
cfg = json.load(open(os.path.join(SERVER, "ProjectileConfigs/Abilities/Projectile_Config_Chronos_Hands.json")))
cfg["$Comment"] = "Porte une silhouette remanente de Chronos. Interactions neutres obligatoires."
cfg["Model"] = "Chronos_Ghost"
dump(os.path.join(SERVER, "ProjectileConfigs/Abilities/Projectile_Config_Chronos_Ghost.json"), cfg)

# =============================================================================================================
# Particules
# =============================================================================================================
P = os.path.join(SERVER, "Particles/Spell")
def recolor(path, color):
    s = json.load(open(path))
    def walk(o):
        if isinstance(o, dict):
            for k, v in o.items():
                if k == "Color" and isinstance(v, str): o[k] = color
                else: walk(v)
        elif isinstance(o, list):
            for v in o: walk(v)
    walk(s); dump(path, s)
recolor(os.path.join(P, "Chronos/Spawners/Chronos_Burst_Sparks.particlespawner"), "#6dffae")
recolor(os.path.join(P, "Chronos/Spawners/Chronos_Burst_Glow.particlespawner"), "#6dffae")
recolor(os.path.join(P, "Chronos/Spawners/Chronos_Gold_Sparks.particlespawner"), "#c8ff8a")
recolor(os.path.join(P, "Chronos/Spawners/Chronos_Gold_Glow.particlespawner"), "#b0ffb0")
for f, txt in (("Chronos/Chronos_Burst.particlesystem", "Eclat temporel vert (Sablier de Chronos : arret et reprise du temps, retour en arriere)."),
               ("Chronos/Chronos_Gold.particlesystem", "Eclat d'engrenage vert-or (Sablier de Chronos).")):
    s = json.load(open(os.path.join(P, f))); s["$Comment"] = txt; dump(os.path.join(P, f), s)

def rng(a, b): return {"Min": a, "Max": b}
def spawner(texture, color, life, rate, count, scale, opacity_keys, offset=(0, 0, 0, 0, 0, 0), speed=(0, 0), pitch=(-90, 90),
            mode="BlendAdd", rotation="Billboard", burst=False, frame=None, light=0, gravity=None, end_scale=1.0):
    s = {"RenderMode": mode, "ParticleRotationInfluence": rotation, "LinearFiltering": True, "LightInfluence": light,
         "ParticleRotateWithSpawner": False, "MaxConcurrentParticles": count, "ParticleLifeSpan": rng(*life),
         "SpawnRate": rng(*rate),
         "EmitOffset": {"X": rng(offset[0], offset[1]), "Y": rng(offset[2], offset[3]), "Z": rng(offset[4], offset[5])},
         "InitialVelocity": {"Speed": rng(*speed), "Yaw": rng(-180, 180), "Pitch": rng(*pitch)},
         "Particle": {"Texture": texture, "ScaleRatioConstraint": "OneToOne", "UVOption": "None", "SoftParticles": "Disable",
                      "InitialAnimationFrame": {"Opacity": 1, "Color": color, "Scale": {"X": rng(*scale), "Y": rng(*scale)}},
                      "Animation": {**{str(k): {"Opacity": v} for k, v in opacity_keys},
                                    "100": {"Opacity": 0, "Scale": {"X": rng(end_scale, end_scale), "Y": rng(end_scale, end_scale)}}}}}
    if burst: s["SpawnBurst"] = True
    if frame: s["Particle"]["FrameSize"] = {"Width": frame, "Height": frame}; s["Particle"]["UVOption"] = "RandomFlipU"
    if gravity is not None: s["Gravity"] = gravity
    return s
def system(path, comment, spawners, life, cull=160):
    data = {"$Comment": comment, "Spawners": [{"SpawnerId": sid} for sid in spawners], "IsImportant": True, "CullDistance": cull}
    if life: data["LifeSpan"] = life                      # sans duree : boucle tant que l'effet porteur est actif
    dump(path, data)

GLOW, SPARK, SMOKE = "Particles/Textures/Basic/Glow.png", "Particles/Textures/Basic/Spark.png", "Particles/Textures/Smoke/Smoke_Dense.png"
CH = os.path.join(P, "Chronos")
# Silhouette remanente : poussiere de temps verte qui s'echappe du corps
dump(os.path.join(CH, "Spawners/Chronos_Afterimage_Dust.particlespawner"),
     spawner(GLOW, "#7dffb5", (0.5, 0.8), (40, 40), 18, (0.15, 0.3), [(0, 0.9), (60, 0.6)], (-0.35, 0.35, 0, 1.8, -0.35, 0.35), (0.2, 0.8), (40, 90), burst=True, end_scale=0.3))
system(os.path.join(CH, "Chronos_Afterimage.particlesystem"), "Silhouette remanente de Chronos : poussiere de temps verte.", ["Chronos_Afterimage_Dust"], 0.1)
# Dash temporel : trainee d'etincelles vertes
dump(os.path.join(CH, "Spawners/Chronos_Dash_Streak.particlespawner"),
     spawner(SPARK, "#9dffc8", (0.3, 0.5), (14, 14), 14, (0.3, 0.5), [(0, 1)], (-0.3, 0.3, 0.2, 1.6, -0.3, 0.3), (1, 3), rotation="BillboardVelocity", burst=True))
system(os.path.join(CH, "Chronos_Dash.particlesystem"), "Trainee du dash temporel de Chronos.", ["Chronos_Dash_Streak"], 0.1)
# Stase : glyphe d'horloge au-dessus de la tete + sable du temps qui monte lentement (attache par l'effet, en boucle)
dump(os.path.join(CH, "Spawners/Chronos_Stase_Glyph.particlespawner"),
     spawner("Particles/Textures/Eldanior/Chronos_Glyph.png", "#7dffb5", (1.0, 1.0), (1, 1), 1, (0.9, 0.9), [(0, 0), (20, 0.95), (80, 0.95)], (0, 0, 1.2, 1.2, 0, 0), end_scale=1.15))
dump(os.path.join(CH, "Spawners/Chronos_Stase_Sand.particlespawner"),
     spawner(GLOW, "#9dffc8", (1.5, 2.2), (6, 8), 14, (0.08, 0.16), [(0, 0), (20, 0.9), (80, 0.6)], (-0.6, 0.6, -0.9, 0.9, -0.6, 0.6), (0.15, 0.35), (60, 90), end_scale=0.4))
system(os.path.join(CH, "Chronos_Stase.particlesystem"), "Stase de Chronos (attachee aux ennemis figes) : glyphe d'horloge et sable du temps verts.",
       ["Chronos_Stase_Glyph", "Chronos_Stase_Sand"], 0)

YG = os.path.join(P, "Yggdrasil")
# Brume toxique rasante (1 bloc de haut), posee en grille sur l'anneau exterieur (la foret) seulement
dump(os.path.join(YG, "Spawners/Yggdrasil_Brume_Ground.particlespawner"),
     spawner(SMOKE, "#5cc23c", (4, 6), (2, 3), 10, (1.4, 2.2), [(0, 0), (25, 0.34), (75, 0.3)], (-2.5, 2.5, 0, 0.2, -2.5, 2.5), (0.05, 0.25), (-3, 3),
             mode="BlendLinear", frame=64, light=0.6, end_scale=1.25))
system(os.path.join(YG, "Yggdrasil_Brume.particlesystem"), "Brume toxique verte d'Yggdrasil, au ras du sol (1 bloc), sur l'anneau de la foret (reposee toutes les 9 s).",
       ["Yggdrasil_Brume_Ground"], 10, 140)
# Vague de poison : bouffee verte au front de la vague
dump(os.path.join(YG, "Spawners/Yggdrasil_Wave_Puff.particlespawner"),
     spawner(SMOKE, "#7ae04a", (0.8, 1.2), (6, 6), 6, (2.5, 4), [(0, 0.6), (40, 0.45)], (-0.8, 0.8, 0, 0.8, -0.8, 0.8), (0.5, 1.5), (0, 40),
             mode="BlendLinear", burst=True, frame=64, light=0.5, end_scale=1.6))
dump(os.path.join(YG, "Spawners/Yggdrasil_Wave_Sparks.particlespawner"),
     spawner(SPARK, "#b6ff5a", (0.4, 0.7), (10, 10), 10, (0.2, 0.35), [(0, 1)], (-0.5, 0.5, 0, 0.5, -0.5, 0.5), (2, 4), (20, 80), rotation="BillboardVelocity", burst=True))
system(os.path.join(YG, "Yggdrasil_Wave.particlesystem"), "Front de la vague de poison d'Yggdrasil.", ["Yggdrasil_Wave_Puff", "Yggdrasil_Wave_Sparks"], 0.1)
# Benediction : motes vert-or qui montent autour de l'allie (attache par l'effet)
dump(os.path.join(YG, "Spawners/Yggdrasil_Blessing_Motes.particlespawner"),
     spawner(GLOW, "#d8ff8a", (1.0, 1.6), (6, 9), 12, (0.1, 0.2), [(0, 0), (20, 1), (70, 0.8)], (-0.5, 0.5, -0.8, 0.6, -0.5, 0.5), (0.4, 0.8), (70, 90), end_scale=0.3))
system(os.path.join(YG, "Yggdrasil_Blessing.particlesystem"), "Benediction de l'arbre-monde (allies pres d'Yggdrasil) : lucioles vert-or.", ["Yggdrasil_Blessing_Motes"], 0)
# Piege vegetal arme : lueur verte qui pulse au ras du sol
dump(os.path.join(YG, "Spawners/Yggdrasil_Trap_Glow.particlespawner"),
     spawner(GLOW, "#5aff6a", (1.2, 1.2), (1, 1), 1, (1.6, 1.6), [(0, 0), (40, 0.7), (60, 0.7)], (0, 0, 0.15, 0.15, 0, 0), burst=True, end_scale=1.3))
dump(os.path.join(YG, "Spawners/Yggdrasil_Trap_Spores.particlespawner"),
     spawner(GLOW, "#a8ff70", (0.8, 1.2), (6, 6), 6, (0.08, 0.14), [(0, 1)], (-0.6, 0.6, 0, 0.3, -0.6, 0.6), (0.3, 0.7), (70, 90), burst=True, end_scale=0.3))
system(os.path.join(YG, "Yggdrasil_Trap.particlesystem"), "Piege vegetal d'Yggdrasil arme (repose toutes les 1,5 s).", ["Yggdrasil_Trap_Glow", "Yggdrasil_Trap_Spores"], 0.1)

# =============================================================================================================
# Effets visuels de modele (ModelVFX) et effets d'entite
# =============================================================================================================
MV = os.path.join(SERVER, "Entity/ModelVFX/Eldanior")
dump(os.path.join(MV, "Chronos_Ghost.json"), {"SwitchTo": "PostColor", "EffectDirection": "BottomUp", "AnimationDuration": 0.15,
     "NoiseScale": {"X": 20.0, "Y": 20.0}, "PostColorOpacity": 0.32, "CurveType": "QuartOut", "HighlightThickness": 0.08,
     "UseBloomOnHighlight": True, "HighlightColor": "#c8ffe0", "PostColor": "#45ff9a"})
dump(os.path.join(MV, "Chronos_Stase.json"), {"SwitchTo": "PostColor", "EffectDirection": "TopDown", "AnimationDuration": 0.6,
     "NoiseScale": {"X": 14.0, "Y": 14.0}, "PostColorOpacity": 0.38, "CurveType": "QuartOut", "HighlightThickness": 0.05,
     "UseBloomOnHighlight": True, "HighlightColor": "#b8ffd8", "PostColor": "#2fd47a"})
EF = os.path.join(SERVER, "Entity/Effects/Eldanior")
dump(os.path.join(EF, "Temps/Chronos_Fige.json"), {
    "$Comment": "Arret du temps (Sablier de Chronos) : la cible est figee dans une stase verte (glyphe d'horloge, sable du temps) ; ses degats sont mis en reserve. Retire par le code quand le temps se brise.",
    "Duration": 40.0, "OverlapBehavior": "Overwrite", "Debuff": True, "StatusEffectIcon": "Icons/ItemsGenerated/Rune_SablierChronos.png",
    "ApplicationEffects": {"EntityTopTint": "#9dffc8", "EntityBottomTint": "#0f5a35", "ModelVFXId": "Chronos_Stase",
                           "Particles": [{"SystemId": "Chronos_Stase", "TargetEntityPart": "Entity", "TargetNodeName": "Chest"}],
                           "MovementEffects": {"DisableAll": True}, "KnockbackMultiplier": 0,
                           "AbilityEffects": {"Disabled": ["Primary", "Secondary", "Ability1", "Ability3"]}}})
dump(os.path.join(EF, "Temps/Chronos_Remanence.json"), {
    "$Comment": "Image remanente de Chronos (echo du lanceur, avec son skin et ses mouvements) : voile vert lumineux. Reappliquee par le code.",
    "Duration": 2.0, "OverlapBehavior": "Overwrite", "Debuff": False,
    "ApplicationEffects": {"EntityTopTint": "#e8fff0", "EntityBottomTint": "#8dffbe", "ModelVFXId": "Chronos_Ghost",
                           "Particles": [{"SystemId": "Chronos_Afterimage", "TargetEntityPart": "Entity"}]}})
h = json.load(open(os.path.join(EF, "Temps/Chronos_Hate.json")))
h["ApplicationEffects"]["EntityTopTint"] = "#c8ffe0"; h["ApplicationEffects"]["EntityBottomTint"] = "#3fd488"
dump(os.path.join(EF, "Temps/Chronos_Hate.json"), h)
dump(os.path.join(EF, "Terre/Yggdrasil_Benediction.json"), {
    "$Comment": "Benediction de l'arbre-monde (allies dans le jardin d'Yggdrasil) : soin, endurance et mana rendus, -20 % de degats subis (geres par le code).",
    "Duration": 1.6, "OverlapBehavior": "Overwrite", "Debuff": False, "StatusEffectIcon": "Icons/ItemsGenerated/Rune_EveilYggdrasil.png",
    "ApplicationEffects": {"EntityTopTint": "#e8ffb0", "EntityBottomTint": "#5fbf3a",
                           "Particles": [{"SystemId": "Yggdrasil_Blessing", "TargetEntityPart": "Entity", "TargetNodeName": "Chest"},
                                         {"SystemId": "Effect_Heal", "TargetEntityPart": "Entity", "TargetNodeName": "Chest", "Scale": 0.6}]}})
# Fontaine de vie (sanctuaire sous l'arbre-monde) : gerbe de lumiere vert-or qui jaillit du cristal, reposee toutes les 2 s
dump(os.path.join(YG, "Spawners/Yggdrasil_Fountain_Spray.particlespawner"),
     spawner(GLOW, "#b8ffb0", (0.9, 1.4), (14, 18), 24, (0.12, 0.22), [(0, 1), (60, 0.8)], (-0.2, 0.2, 0, 0.3, -0.2, 0.2), (2.0, 3.5), (70, 90), end_scale=0.3))
dump(os.path.join(YG, "Spawners/Yggdrasil_Fountain_Halo.particlespawner"),
     spawner(GLOW, "#f0ffa8", (2.0, 2.0), (1, 1), 1, (2.2, 2.2), [(0, 0), (30, 0.55), (70, 0.55)], burst=True, end_scale=1.2))
dump(os.path.join(YG, "Spawners/Yggdrasil_Fountain_Motes.particlespawner"),
     spawner(GLOW, "#ffe28a", (1.5, 2.0), (4, 6), 10, (0.06, 0.12), [(0, 0), (30, 1)], (-1.8, 1.8, -1.2, 0.5, -1.8, 1.8), (0.2, 0.5), (60, 90), end_scale=0.3))
system(os.path.join(YG, "Yggdrasil_Fountain.particlesystem"), "Fontaine de vie du sanctuaire d'Yggdrasil (gerbe de lumiere, halo, lucioles).",
       ["Yggdrasil_Fountain_Spray", "Yggdrasil_Fountain_Halo", "Yggdrasil_Fountain_Motes"], 2.2)
# Eclat de temps (fin de Chronos) : eclats verts, cercle d'horloge qui s'elargit, sable du temps
dump(os.path.join(CH, "Spawners/Chronos_Shatter_Shards.particlespawner"),
     spawner(SPARK, "#d8ffe8", (0.6, 1.0), (40, 40), 40, (0.35, 0.6), [(0, 1), (50, 0.9)], (-0.4, 0.4, 0, 1.6, -0.4, 0.4), (5, 11), (-30, 80), rotation="BillboardVelocity", burst=True))
dump(os.path.join(CH, "Spawners/Chronos_Shatter_Glyph.particlespawner"),
     spawner("Particles/Textures/Eldanior/Chronos_Glyph.png", "#7dffb5", (0.7, 0.7), (1, 1), 1, (1.0, 1.0), [(0, 1), (40, 0.9)], (0, 0, 1.0, 1.0, 0, 0), burst=True, end_scale=4.0))
dump(os.path.join(CH, "Spawners/Chronos_Shatter_Sand.particlespawner"),
     spawner(GLOW, "#9dffc8", (1.2, 1.8), (30, 30), 30, (0.08, 0.16), [(0, 1), (60, 0.7)], (-0.6, 0.6, 0.2, 1.8, -0.6, 0.6), (0.6, 2.0), (-20, 60), burst=True, end_scale=0.3))
dump(os.path.join(CH, "Spawners/Chronos_Shatter_Flash.particlespawner"),
     spawner(GLOW, "#b8ffd8", (0.4, 0.4), (1, 1), 1, (3.0, 3.0), [(0, 1)], (0, 0, 1.0, 1.0, 0, 0), burst=True, end_scale=1.6))
system(os.path.join(CH, "Chronos_Shatter.particlesystem"), "Eclat de temps (fin du Sablier de Chronos) : le temps se brise en eclats verts.",
       ["Chronos_Shatter_Flash", "Chronos_Shatter_Shards", "Chronos_Shatter_Glyph", "Chronos_Shatter_Sand"], 0.1)
print("ok")
