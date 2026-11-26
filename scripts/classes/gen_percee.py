"""Faisceaux, particules et effet de posture de la Percee des katanas Luo, aux couleurs du Ki de chaque palier.

Refaits le 2026-11-12 (« associer la couleur de Ki du joueur avec son attaque, ameliore le faisceau de Ki ») : la Percee
prend la couleur du Ki de la classe du lanceur (gen_voie_martiale.KI ; Dieu Martial : or blanc), comme son aura, ses lames
de Ki et ses epees. Percee.java choisit l'asset par le suffixe _T<palier>.

Faisceaux (Server/Entity/Beams/<id>.json + Common/Trails/<id>.png, 256 x 128, x = le long du faisceau, periodique) :
  Percee_Air_T<n>      souffle de chaque estoc : presque transparent, stries de vitesse, bords teintes de Ki
  Percee_Trait_T<n>    trait de Ki (aiguille, lance, coeur du canon) : coeur blanc brulant, gaine de Ki saturee, filaments
                       lumineux qui ondulent, halo doux
  Percee_Aura_T<n>     gaine large et translucide (lance, canon) : volutes et stries qui filent
  Percee_Spirale_T<n>  fil tres lumineux des spirales du canon

Particules (Server/Particles/Eldanior/Martial/Percee) :
  Percee_Posture_T<n>     continue, accrochee a la lame pendant la posture : poussieres de Ki aspirees vers la lame
  Percee_Pret_T<n>        eclat en etoile a la pointe : la charge est pleine
  Percee_Pointe_T<n>      estoc qui part : etoile, onde de choc, gerbe d'etincelles
  Percee_Impact_T<n>      ennemi transperce : eclat, etincelles, onde, fumee de Ki
  Percee_Trace_T<n>       poussieres de Ki laissees le long de la ligne de l'estoc
  Percee_Silhouette_T<n>  l'ame du lanceur (Lance de l'Ame) : silhouette de lumiere qui se dissipe
  Percee_Convergence_T<n> charge du canon : Ki aspire en spirale vers la pointe
  Percee_Bouche_T<n>      depart du canon : etoile geante, double onde
  Percee_Explosion_T<n>   bout du canon : explosion de Ki
  Percee_Entaille_T<n>    entaille en X qui eclate sur un ennemi (coupe differee, impacts forts)
  Percee_Rayon_T<n>       maillon lumineux du rayon du canon (reemis le long du tir, insensible a la lumiere)
  Percee_Spirale_Point_T<n> point lumineux des spirales du canon
  Percee_Prunier          Percee du Prunier : fleurs de prunier qui eclatent et retombent (or et rose pale)

Effets d'entite (Server/Entity/Effects/Eldanior/Luo/Percee_Posture_T<n>.json) : Percee_Posture_T<n> sur la lame
(PrimaryItem, noeud Handle), pose et retire par PerceePosture.java.

Usage : python3 scripts/classes/gen_percee.py [src/main/resources]
"""
import json, math, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
for folder in ("armures", "rune_aura_icons", "artefacts"):
    sys.path.insert(0, os.path.join(HERE, "..", folder))
sys.path.insert(0, HERE)
import png  # noqa: E402
import gen_voie_martiale as V  # noqa: E402

RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "..", "..", "src", "main", "resources")
OUT = os.path.join(RES, "Server", "Particles", "Eldanior", "Martial", "Percee")
W, H = 256, 128

# Couleur du Ki (gen_voie_martiale.KI) et teinte profonde des bords, par palier
PALETTE = {1: ("#e8eef5", "#9fbde6"), 2: ("#a8d0ff", "#4f8fff"), 3: ("#3d7dff", "#1a3fd6"), 4: ("#3fe6ff", "#0fa0dc"),
           5: ("#a050ff", "#5f16d8"), 6: ("#ffc83a", "#ff7f0a"), 7: ("#fff3e6", "#ffb98c"), 8: ("#fff3c8", "#ffb42a")}
for _n, (_c, _, _) in V.KI.items():                           # garde-fou : la couleur du Ki reste celle de la voie martiale
    if _n < 8:
        assert PALETTE[_n][0].lower() == _c.lower(), (_n, _c)


def rgb(h):
    h = h.lstrip("#")
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def mix(a, b, t):
    t = max(0.0, min(1.0, t))
    return tuple(a[i] + (b[i] - a[i]) * t for i in range(3))


# ---------------------------------------------------------------------------------------------------------------------
# Textures de faisceaux (periodiques le long de x : pas de couture quand le moteur repete la texture)
# ---------------------------------------------------------------------------------------------------------------------
def write_beam(name, pixel):
    img = []
    for y in range(H):
        v = (y + 0.5) / H * 2 - 1                                               # -1 .. 1 a travers le faisceau
        img.append([pixel(x, y, v) for x in range(W)])
    os.makedirs(os.path.join(RES, "Common", "Trails"), exist_ok=True)
    png.write(os.path.join(RES, "Common", "Trails", name + ".png"), W, H, img)
    os.makedirs(os.path.join(RES, "Server", "Entity", "Beams"), exist_ok=True)
    json.dump({"TexturePath": "Trails/%s.png" % name}, open(os.path.join(RES, "Server", "Entity", "Beams", name + ".json"), "w"), indent=2)


def px(c, a):
    return [int(max(0, min(255, c[0]))), int(max(0, min(255, c[1]))), int(max(0, min(255, c[2]))), int(255 * max(0.0, min(1.0, a)))]


def streaks(x, y, seed, fine=1.0):
    """Stries qui filent le long du faisceau : varient vite a travers (y), lentement le long (x, periodique)."""
    v = 0.0
    for k, (fy, fx, ph) in enumerate(((0.55, 1, 0.4), (1.3, 2, 2.3), (0.23, 1, 5.1), (2.1, 3, 1.1))):
        v += math.sin(y * fy * fine + 2 * math.pi * fx * x / W + ph + seed * 1.7 * (k + 1)
                      + 1.2 * math.sin(2 * math.pi * (k + 1) * x / W + seed)) / (k + 1.2)
    return max(0.0, min(1.0, 0.5 + 0.5 * v / 1.5))


def beams(n):
    ki, deep = rgb(PALETTE[n][0]), rgb(PALETTE[n][1])
    white = (255, 255, 255)
    tint = min(1.0, 0.3 + 0.09 * n)                               # le souffle prend de plus en plus la couleur du Ki

    def air(x, y, v):
        a = abs(v)
        body = max(0.0, 1 - a * a) ** 2.5
        st = streaks(x, y, 1, 1.4) ** 2
        alpha = body * (0.12 + 0.5 * st)
        c = mix(white, mix(white, ki, tint), a * 1.6 + 0.2)
        return px(c, alpha)

    def trait(x, y, v):
        a = abs(v)
        core = max(0.0, 1 - a / 0.16) ** 1.3                                     # coeur blanc brulant
        sheath = max(0.0, 1 - a / 0.58) ** 1.1                                   # gaine de Ki
        halo = max(0.0, 1 - a) ** 2.4
        flow = streaks(x, y, 3, 1.1)                                             # energie qui file
        fil = 0.0
        for k in range(2):                                                        # deux filaments, amplitude qui respire
            amp = 0.18 + 0.2 * streaks(x * 0.5, k * 40.0, 7 + k)
            yc = amp * math.sin(2 * math.pi * (k + 1) * x / W + k * 2.6)
            fil = max(fil, max(0.0, 1 - abs(v - yc) / 0.035) * (0.4 + 0.6 * streaks(x, k * 9.0, 9)))
        c = mix(deep, ki, sheath * (0.6 + 0.4 * flow))
        c = mix(c, white, min(1.0, core + 0.45 * fil + 0.25 * sheath * flow))
        alpha = min(1.0, core + sheath * (0.55 + 0.4 * flow) + 0.4 * halo + 0.45 * fil)
        return px(c, alpha)

    def aura(x, y, v):
        a = abs(v)
        body = max(0.0, 1 - a * a) ** 1.4
        st = streaks(x, y, 4, 0.8)
        wisp = streaks(x * 2, y * 0.35, 5)
        alpha = body * (0.12 + 0.45 * st * (0.5 + 0.5 * wisp))
        c = mix(mix(ki, white, 0.3 * (1 - a)), deep, a ** 1.5)
        return px(c, alpha)

    def spirale(x, y, v):
        a = abs(v)
        core = max(0.0, 1 - a / 0.3) ** 2
        glow = max(0.0, 1 - a) ** 3
        c = mix(ki, white, core)
        return px(c, min(1.0, core + 0.6 * glow) * (0.8 + 0.2 * streaks(x, y, 6)))

    for name, fn in (("Percee_Air_T%d", air), ("Percee_Trait_T%d", trait), ("Percee_Aura_T%d", aura), ("Percee_Spirale_T%d", spirale)):
        write_beam(name % n, fn)


# ---------------------------------------------------------------------------------------------------------------------
# Particules
# ---------------------------------------------------------------------------------------------------------------------
GLOW, SPARK = "Particles/Textures/Basic/Glow.png", "Particles/Textures/Basic/Spark.png"
RING = "Particles/Textures/Circles/Explosion_Shockwave.png"
SMOKE, PETAL, FLOWER = "Particles/Textures/Rnd/SmokeRound.png", "Particles/Textures/Nature/Petal_GS.png", "Particles/Textures/Shapes/Flower_GS.png"


def rng(a, b=None):
    return {"Min": a, "Max": a if b is None else b}


def sp(texture, c0, c1, life, scale, count, *, opacity=1.0, scale_y=None, end=None, ratio="OneToOne", rotation="Billboard",
       speed=None, pitch=(-90, 90), yaw=(-180, 180), shape=None, offset=None, burst=True, rate=None, mode="BlendAdd",
       rot_z=None, attract=None, stretch=None, mid=None, opacity_end=0.0):
    init = {"Opacity": opacity, "Color": c0, "Scale": {"X": rng(*scale)}}
    if scale_y is not None:
        init["Scale"]["Y"] = rng(*scale_y)
    if rot_z is not None:
        init["Rotation"] = {"Z": rng(*rot_z)}
    last = {"Opacity": opacity_end, "Color": c1}
    if end is not None:
        last["Scale"] = {"X": rng(*end[0])}
        if len(end) > 1:
            last["Scale"]["Y"] = rng(*end[1])
    anim = {"0": dict(init)}
    if mid:
        anim[str(mid[0])] = mid[1]
    anim["100"] = last
    s = {"RenderMode": mode, "ParticleRotationInfluence": rotation, "LinearFiltering": True, "LightInfluence": 0,
         "ParticleRotateWithSpawner": False, "MaxConcurrentParticles": max(count, 1) * (1 if burst else 3),
         "ParticleLifeSpan": rng(*life), "SpawnBurst": burst,
         "Particle": {"Texture": texture, "ScaleRatioConstraint": ratio, "UVOption": "None", "SoftParticles": "Disable",
                      "InitialAnimationFrame": init, "Animation": anim}}
    if burst:
        s["SpawnRate"] = rng(count)
        s["TotalParticles"] = rng(count)
    else:
        s["SpawnRate"] = rng(*rate)
    if speed:
        s["InitialVelocity"] = {"Speed": rng(*speed), "Yaw": rng(*yaw), "Pitch": rng(*pitch)}
    if shape:
        s["Shape"] = shape
        s["UseEmitDirection"] = True
    if offset:
        s["EmitOffset"] = {k: rng(-v, v) for k, v in offset.items()}
    if attract:
        s["Attractors"] = [attract]
    if stretch:
        s["VelocityStretchMultiplier"] = stretch
    return s


def star(c, size, life=0.22):
    """Eclat en etoile : quatre traits de lumiere croises (rotation dans le plan de l'ecran)."""
    out = []
    for z, k in ((0, 1.0), (90, 1.0), (45, 0.55), (135, 0.55)):
        out.append(sp(GLOW, "#ffffff", c, (life, life), (size * 0.07, size * 0.07), 1, scale_y=(size * k, size * k), ratio="None",
                      rot_z=(z, z), end=((size * 0.02, size * 0.02), (size * k * 1.3, size * k * 1.3))))
    out.append(sp(GLOW, "#ffffff", c, (life, life), (size * 0.45, size * 0.45), 1, opacity=0.9, end=((size * 0.15, size * 0.15),)))
    return out


def shock(c, start, end, life=0.3):
    """Onde de choc : anneau face a la camera qui s'ouvre."""
    return sp(RING, "#ffffff", c, (life, life), (start, start), 1, opacity=0.9, end=((end, end),))


def system(name, parts, comment, cull=80, life=0.1, continuous=False, bounding=None):
    spawners = []
    for i, (s, dy) in enumerate(parts):
        sid = "%s_%d" % (name, i)
        write(os.path.join(OUT, "Spawners", sid + ".particlespawner"), s)
        spawners.append({"SpawnerId": sid, "PositionOffset": {"Y": dy}})
    data = {"$Comment": comment + " (gen_percee.py)", "Spawners": spawners, "IsImportant": True, "CullDistance": cull}
    if not continuous:
        data["LifeSpan"] = life
    if bounding:
        data["BoundingRadius"] = bounding
    write(os.path.join(OUT, name + ".particlesystem"), data)


def write(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")


def particles(n):
    c, d = PALETTE[n]
    k = 0.7 + 0.075 * n                                                         # densite / taille selon la classe
    # posture : poussieres de Ki aspirees vers la lame, lueur qui pulse, etincelles
    motes = sp(GLOW, c, "#ffffff", (0.5, 0.8), (0.08, 0.14), int(6 * k), shape="Sphere", offset={"X": 0.9, "Y": 0.9, "Z": 0.9},
               speed=(0.2, 0.4), burst=False, rate=(10 * k, 14 * k),
               attract={"RadialAcceleration": -6, "DampingMultiplier": {"X": 0.9, "Y": 0.9, "Z": 0.9}},
               mid=(30, {"Opacity": 1.0, "Color": c}), opacity=0.0, opacity_end=0.6, end=((0.03, 0.05),))
    motes["ParticleRotationInfluence"] = "BillboardVelocity"
    motes["VelocityStretchMultiplier"] = 3.0
    motes["Particle"]["ScaleRatioConstraint"] = "None"
    glow = sp(GLOW, c, d, (0.35, 0.45), (0.3 * k, 0.36 * k), 3, opacity=0.35, burst=False, rate=(7, 9),
              offset={"X": 0.35, "Y": 0.04, "Z": 0.04}, end=((0.2 * k, 0.25 * k),))
    sparks = sp(SPARK, "#ffffff", c, (0.25, 0.4), (0.05, 0.08), 4, speed=(0.3, 0.8), burst=False, rate=(5 * k, 8 * k),
                offset={"X": 0.45, "Y": 0.05, "Z": 0.05})
    system("Percee_Posture_T%d" % n, [(motes, 0), (glow, 0), (sparks, 0)], "Posture de la Percee, palier %d : Ki aspire vers la lame" % n,
           cull=64, continuous=True, bounding=3)
    system("Percee_Pret_T%d" % n, [(s, 0) for s in star(c, 0.9 * k, 0.3)] + [(shock(c, 0.2, 1.4 * k, 0.35), 0)],
           "Percee, palier %d : charge pleine (eclat a la pointe)" % n)
    tip_sparks = sp(SPARK, "#ffffff", c, (0.2, 0.35), (0.06, 0.1), int(12 * k), shape="Sphere", speed=(5, 10), stretch=4.0,
                    rotation="BillboardVelocity", ratio="None", scale_y=(0.3, 0.5), offset={"X": 0.05, "Y": 0.05, "Z": 0.05},
                    attract={"DampingMultiplier": {"X": 0.85, "Y": 0.85, "Z": 0.85}})
    system("Percee_Pointe_T%d" % n, [(s, 0) for s in star(c, 1.1 * k)] + [(shock(c, 0.3, 1.8 * k, 0.28), 0), (tip_sparks, 0)],
           "Percee, palier %d : l'estoc part (pointe de la lame)" % n)
    hit_sparks = sp(SPARK, "#ffffff", c, (0.25, 0.45), (0.07, 0.11), int(16 * k), shape="Sphere", speed=(4, 9), stretch=3.0,
                    rotation="BillboardVelocity", ratio="None", scale_y=(0.3, 0.6), offset={"X": 0.1, "Y": 0.1, "Z": 0.1},
                    attract={"LinearAcceleration": {"Y": -6}, "DampingMultiplier": {"X": 0.9, "Y": 0.9, "Z": 0.9}})
    hit_glow = sp(GLOW, "#ffffff", c, (0.25, 0.25), (1.0 * k, 1.0 * k), 1, opacity=0.95, end=((0.3, 0.3),))
    hit_smoke = sp(SMOKE, c, d, (0.5, 0.8), (0.4, 0.6), 4, opacity=0.3, speed=(0.4, 1.0),          # volutes de Ki (plus de noir)
                   offset={"X": 0.3, "Y": 0.3, "Z": 0.3}, end=((0.9, 1.2),))
    system("Percee_Impact_T%d" % n, [(hit_glow, 0), (shock(c, 0.4, 2.2 * k, 0.3), 0), (hit_sparks, 0), (hit_smoke, 0)],
           "Percee, palier %d : ennemi transperce" % n)
    trace = sp(GLOW, c, d, (0.35, 0.6), (0.12 * k, 0.2 * k), 3, opacity=0.8, speed=(0.1, 0.4), offset={"X": 0.15, "Y": 0.15, "Z": 0.15},
               end=((0.03, 0.05),))
    trace_sp = sp(SPARK, "#ffffff", c, (0.2, 0.35), (0.04, 0.07), 2, speed=(0.3, 1.0))
    system("Percee_Trace_T%d" % n, [(trace, 0), (trace_sp, 0)], "Percee, palier %d : Ki laisse le long de l'estoc" % n)
    sil = []
    for dy, sx, sy in ((1.62, 0.5, 0.5), (1.05, 0.62, 1.0), (0.4, 0.5, 0.95)):
        sil.append((sp(GLOW, c, d, (0.7, 0.7), (sx, sx), 1, opacity=0.6, scale_y=(sy, sy), ratio="None",
                       end=((sx * 1.3, sx * 1.3), (sy * 1.1, sy * 1.1))), dy))
    sil.append((sp(SPARK, "#ffffff", c, (0.6, 0.9), (0.05, 0.09), 10, speed=(0.4, 1.2), pitch=(60, 90),
                   offset={"X": 0.3, "Y": 0.8, "Z": 0.3}), 0.9))
    system("Percee_Silhouette_T%d" % n, sil, "Percee, palier %d : l'ame du lanceur, silhouette de lumiere" % n)
    conv = sp(GLOW, c, "#ffffff", (0.35, 0.5), (0.06, 0.09), int(30 * k), shape="Sphere", offset={"X": 3.0, "Y": 3.0, "Z": 3.0},
              speed=(0.5, 1.0), rotation="BillboardVelocity", ratio="None", scale_y=(0.4, 0.7), stretch=6.0,
              attract={"RadialAcceleration": -40, "RadialTangentAcceleration": 6, "DampingMultiplier": {"X": 0.92, "Y": 0.92, "Z": 0.92}},
              opacity=0.0, mid=(25, {"Opacity": 1.0}), opacity_end=0.8)
    core = sp(GLOW, "#ffffff", c, (0.2, 0.2), (0.6 * k, 0.6 * k), 1, opacity=0.9, end=((0.9 * k, 0.9 * k),))
    system("Percee_Convergence_T%d" % n, [(conv, 0), (core, 0)], "Percee, palier %d : Ki aspire vers la pointe (charge du canon)" % n)
    system("Percee_Bouche_T%d" % n, [(s, 0) for s in star(c, 3.2, 0.35)] + [(shock(c, 0.8, 6.0, 0.4), 0), (shock(d, 0.5, 9.0, 0.6), 0),
                                                                          (tip_sparks, 0)],
           "Percee, palier %d : depart du canon de Ki" % n, cull=128)
    boom_glow = sp(GLOW, "#ffffff", c, (0.5, 0.5), (5.0, 5.0), 1, opacity=0.95, end=((1.5, 1.5),))
    boom_sparks = sp(SPARK, "#ffffff", c, (0.6, 1.0), (0.12, 0.2), 60, shape="Sphere", speed=(8, 18), stretch=3.0,
                     rotation="BillboardVelocity", ratio="None", scale_y=(0.5, 0.9), offset={"X": 0.5, "Y": 0.5, "Z": 0.5},
                     attract={"LinearAcceleration": {"Y": -8}, "DampingMultiplier": {"X": 0.92, "Y": 0.92, "Z": 0.92}})
    boom_smoke = sp(SMOKE, c, d, (1.2, 1.8), (1.5, 2.2), 14, opacity=0.3, speed=(1.5, 4.0),
                    offset={"X": 1.0, "Y": 1.0, "Z": 1.0}, end=((3.5, 4.5),))
    system("Percee_Explosion_T%d" % n, [(boom_glow, 0), (shock(c, 1.0, 12.0, 0.5), 0), (shock(d, 0.5, 7.0, 0.7), 0.2),
                                        (boom_sparks, 0), (boom_smoke, 0)],
           "Percee, palier %d : explosion de Ki au bout du canon" % n, cull=160)
    # entaille en X : la coupe qui eclate apres le passage (ruee de l'Expert, impacts des paliers superieurs)
    cuts = []
    for z in (32, -32):
        cuts.append((sp(GLOW, "#ffffff", c, (0.28, 0.28), (0.09 * k, 0.09 * k), 1, scale_y=(2.2 * k, 2.2 * k), ratio="None",
                        rot_z=(z, z), end=((0.02, 0.02), (2.8 * k, 2.8 * k))), 0))
    cuts.append((sp(GLOW, c, d, (0.3, 0.3), (0.9 * k, 0.9 * k), 1, opacity=0.6, end=((0.3, 0.3),)), 0))
    cuts.append((sp(SPARK, "#ffffff", c, (0.25, 0.45), (0.05, 0.09), int(10 * k), shape="Sphere", speed=(3, 7), stretch=3.0,
                    rotation="BillboardVelocity", ratio="None", scale_y=(0.3, 0.5), offset={"X": 0.1, "Y": 0.1, "Z": 0.1}), 0))
    system("Percee_Entaille_T%d" % n, cuts, "Percee, palier %d : entaille en X qui eclate sur un ennemi" % n)
    # rayon du canon en particules (2026-11-12 : les faisceaux du moteur sont eclaires par le monde et devenaient noirs dans
    # le tunnel creuse) : chapelet de lueurs reemis tous les ~90 ms le long du tir, insensibles a la lumiere (LightInfluence 0)
    ray_core = sp(GLOW, "#ffffff", c, (0.2, 0.2), (1.3, 1.3), 1, opacity=0.95, end=((1.0, 1.0),))
    ray_glow = sp(GLOW, c, d, (0.22, 0.22), (3.4, 3.4), 1, opacity=0.42, end=((2.8, 2.8),))
    ray_spark = sp(SPARK, "#ffffff", c, (0.2, 0.3), (0.06, 0.1), 2, speed=(1.5, 4.0), stretch=3.0, rotation="BillboardVelocity",
                   ratio="None", scale_y=(0.3, 0.5), offset={"X": 0.8, "Y": 0.8, "Z": 0.8})
    system("Percee_Rayon_T%d" % n, [(ray_core, 0), (ray_glow, 0), (ray_spark, 0)],
           "Percee, palier %d : maillon lumineux du rayon du canon de Ki" % n, cull=160)
    system("Percee_Spirale_Point_T%d" % n, [(sp(GLOW, "#ffffff", c, (0.18, 0.18), (0.45, 0.45), 1, opacity=0.95, end=((0.2, 0.2),)), 0)],
           "Percee, palier %d : point lumineux des spirales du canon" % n, cull=160)
    # effet d'entite : la posture sur la lame (PrimaryItem / Handle, comme le Ki de l'Epee)
    att = {"SystemId": "Percee_Posture_T%d" % n, "TargetEntityPart": "PrimaryItem", "TargetNodeName": "Handle",
           "PositionOffset": {"X": 0.6, "Y": 0, "Z": 0}, "Scale": 1.0}
    write(os.path.join(RES, "Server", "Entity", "Effects", "Eldanior", "Luo", "Percee_Posture_T%d.json" % n),
          {"$Comment": "Posture de la Percee, palier %d (Ki %s) : le Ki est aspire vers la lame pendant la charge. Pose et retire par "
                       "PerceePosture.java (gen_percee.py)." % (n, c),
           "ApplicationEffects": {"Particles": [att], "FirstPersonParticles": [dict(att, Scale=0.6)]},
           "Duration": 4.0, "OverlapBehavior": "Overwrite", "Debuff": False})


def prunier():
    petals = sp(PETAL, "#ffe4ee", "#fff6d0", (1.4, 2.2), (0.16, 0.24), 22, shape="Sphere", speed=(1.5, 3.5), mode="BlendLinear",
                rotation="Billboard", rot_z=(0, 360), offset={"X": 0.2, "Y": 0.2, "Z": 0.2},
                attract={"LinearAcceleration": {"Y": -1.2}, "DampingMultiplier": {"X": 0.9, "Y": 0.9, "Z": 0.9}},
                opacity=1.0, opacity_end=0.0)
    flowers = sp(FLOWER, "#ffffff", "#ffd0e0", (0.6, 0.8), (0.35, 0.5), 3, opacity=0.95, rot_z=(0, 360), mode="BlendLinear",
                 speed=(0.2, 0.6), end=((0.6, 0.8),))
    gold = sp(SPARK, "#ffffff", "#ffc83a", (0.4, 0.7), (0.06, 0.1), 10, shape="Sphere", speed=(2, 5))
    system("Percee_Prunier", [(petals, 0), (flowers, 0), (gold, 0)], "Percee du Prunier : fleurs de prunier qui eclatent et retombent")


def main():
    for n in range(1, 9):
        beams(n)
        particles(n)
    prunier()
    print("Percee : 32 faisceaux, %d systemes de particules, 8 effets de posture." % (8 * 13 + 1))


if __name__ == "__main__":
    main()
