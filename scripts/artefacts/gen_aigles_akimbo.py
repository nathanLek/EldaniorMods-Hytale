"""Colts de cavalerie akimbo (2026-11-15, ex-« Seraphin et Cherubin, Aigles du Desert ») : forme transformee du mousquet
legendaire Green Colt (5 min, ArmeTransfo), seule facon de les avoir. Deux Colt Single Action Army 1873 canon cavalerie,
chargeurs de 5 + 5 Balles Divines (Mousquet.coltTrigger), portee 40 blocs. Ancienne description : deux pistolets lourds, tenus a deux
mains (akimbo), qui tirent des balles divines.

Modele : hierarchie du pistolet vanilla (Items/Hypixel/Minigames/GunPvP/Weapons/Handguns/Handgun.blockymodel :
R-Attachment > Handle) gardee, donc tenu comme lui ; la poignee (Handle) et ses enfants sont remplaces (outils de
gen_divines.py). Le corps de l'arme est un pivot place et tourne comme le noeud "Base" vanilla (canon vers -x). Culasse
d'argent poli gravee d'or, carcasse d'or, pontet carre, plaquettes d'ivoire a medaillon de saphir, veines et croix de
lumiere sur la culasse, bouche du canon lumineuse. Le meme modele est rendu dans les deux mains ("RenderDualWielded").

Animations (jeu Aigles_Akimbo, derive du jeu vanilla "Handgun", qui n'anime que le bras droit) : le squelette du joueur
est symetrique, donc rotation (x, y, z, w) -> (x, -y, -z, w), position x -> -x, noeuds R <-> L (comme les Lames Jumelles).
  - repos, marche, course, accroupi, saut, chute, garde, interaction = "double" (le bras gauche vise en miroir du droit) ;
  - Shoot (pistolet droit) = tir vanilla + bras gauche fige en visee ; ShootLeft = miroir complet du tir + bras droit
    fige en visee ; ShootBoth = tir des deux pistolets a la fois (signature) ;
  - nage, vol, escalade et coup de crosse : vanilla.

Tir (clic, ou maintenu) : alternance droite / gauche (Chaining), un sort Java par balle (AiglesDuDesert.java) : la balle
divine est un modele deplace par le code (Balle_Divine : trait de lumiere doree), qui traverse deux ennemis et s'arrete au
premier mur ; une balle sur sept est une Balle de Jugement (une colonne de lumiere tombe du ciel la ou elle frappe).
Signature, Jugement Celeste : rafale de douze balles qui cherchent les ennemis, puis les deux pistolets tirent ensemble
une balle geante qui appelle une grande colonne de lumiere.

Usage : python3 scripts/artefacts/gen_aigles_akimbo.py <src/main/resources> [dossier_apercu]
"""
import copy, json, math, os, sys, zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import gen_divines as G  # noqa: E402  (outils de modelisation et de peinture ; son main() ne s'execute pas a l'import)

K, png = G.K, G.png
RES = sys.argv[1]
PREVIEW = sys.argv[2] if len(sys.argv) > 2 else None
Z = zipfile.ZipFile(K.ASSETS)
NAME = "Artefact_Handgun_Aigles_Desert"
VANILLA = "Items/Hypixel/Minigames/GunPvP/Weapons/Handguns/Handgun.blockymodel"
MODEL_REL = G.MODEL_REL

CHROME, CHROME_DARK = (222, 226, 236), (128, 132, 148)
IVORY = (240, 232, 212)
HOLY, HOLY_CORE = (255, 206, 96), (255, 252, 230)              # lumiere doree
SAPPHIRE = (70, 140, 255)
JADE, JADE_CORE = (60, 230, 130), (210, 255, 225)
STEEL, BLACK, BRASS = (92, 96, 108), (26, 26, 30), (214, 168, 84)
BASH_DAMAGE = 45                                              # coup de crosse du gun-kata
HOLD = 0.45                                                   # maintien du clic pour la Balle Perforante (s)


# ---------------------------------------------------------------------------------------------------------------------
# Modele
# ---------------------------------------------------------------------------------------------------------------------
def checker(base, line, step=4):
    """Quadrillage en pointes de diamant (plaquettes), bordure plus sombre."""
    def paint(face, w, h, seed):
        img = K.canvas(w, h, base)
        for y in range(h):
            for x in range(w):
                a, b = (x + y) % step, (x - y) % step
                if a == 0 or b == 0:
                    img[y][x][:3] = list(line)
                elif a == 1 or b == 1:
                    img[y][x][:3] = [min(255, c * 1.08) for c in base]
                if min(x, y, w - 1 - x, h - 1 - y) < 1:
                    img[y][x][:3] = [c * 0.7 for c in base]
        return K.finish(img, face, edges=False)
    return paint


def scrolls(base, line):
    """Or grave de rinceaux (gravure de luxe des armes de poing) : tige ondulee le long des grands flancs, une volute
    enroulee et une feuille a chaque vague ; trait sombre souligne d'un liseré clair. Petites faces : metal a filet."""
    m = G.metal(base, 0.25, finish=False)
    plain = G.metal(base, 0.25, edge=line)
    light = G.mixc(base, (255, 255, 255), 0.45)

    def paint(face, w, h, seed):
        if face not in ("front", "back") or w < 3 * h or h < 12:
            return plain(face, w, h, seed)
        img = m(face, w, h, seed)
        P = h * 1.7
        stem = [(x, h * (0.5 + 0.22 * math.sin(2 * math.pi * x / P))) for x in [i * 0.5 for i in range(int(w * 2) + 1)]]
        K.stroke(img, [(x, y + 0.7) for x, y in stem], light, 0.3)
        K.stroke(img, stem, line, 0.4)
        k = 0
        x0 = P / 4
        while x0 < w - 2:
            up = k % 2 == 0
            cy = h * (0.5 + (0.22 if not up else -0.22))
            curl = [(x0 + math.cos(t) * t * h * 0.035, cy + (1 if up else -1) * math.sin(t) * t * h * 0.035) for t in [i * 0.3 for i in range(22)]]
            K.stroke(img, [(x, y + 0.6) for x, y in curl], light, 0.3)
            K.stroke(img, curl, line, 0.35)
            leaf = [(x0 + P * 0.18 + t * P * 0.18, cy + (1 if up else -1) * (h * 0.18) * math.sin(t * math.pi)) for t in [i / 8.0 for i in range(9)]]
            K.stroke(img, leaf, line, 0.35)
            x0 += P / 2
            k += 1
        for y in range(h):                                                       # filet en bordure
            for x in range(w):
                if min(x, y, w - 1 - x, h - 1 - y) < 1:
                    img[y][x][:3] = list(line)
        return G.bevel(K.finish(img, face, edges=False))
    return paint


def case_hardened(seed0=0):
    """Acier trempe en paquet (carcasse des Colt) : marbrures bleues, violettes et ambrees sur fond gris."""
    cols = [(70, 90, 140), (120, 80, 140), (190, 150, 80), (140, 146, 156)]

    def paint(face, w, h, seed):
        img = K.canvas(w, h, (140, 146, 156))
        for y in range(h):
            for x in range(w):
                n = K.fbm(x * 0.12 + seed + seed0, y * 0.12)
                m = K.fbm(x * 0.2 + 9 + seed, y * 0.2 + 3)
                c = G.mixc(cols[int(n * 3.99) % 4], cols[int(m * 3.99) % 4], 0.5)
                k = 1.0 + 0.12 * (K.noise(x * 0.5, y * 0.5 + seed) - 0.5)
                img[y][x][:3] = [ch * k for ch in c]
        return G.bevel(K.finish(img, face, edges=False))
    return paint


def flutes(base, groove, period=4):
    """Barillet cannele : cannelures dans la longueur (lignes sombres le long de x sur les faces laterales)."""
    m = G.metal(base, 0.25, finish=False)

    def paint(face, w, h, seed):
        img = m(face, w, h, seed)
        if face in ("left", "right"):
            return G.bevel(K.finish(img, face, edges=False))
        for y in range(h):
            if y % period == 2:
                for x in range(1, w - 1):
                    img[y][x][:3] = list(groove)
        return K.finish(img, face, edges=False)
    return paint


def tube_x(name, pos, d, length, paint, n=3, lit=False):
    """Tube le long de x (12 pans : boites tournees de 0, 30, 60 degres autour de x)."""
    make = G.lit if lit else G.box
    out = []
    for i, a in enumerate((0, 30, 60)[:n]):
        k = 1.0 - 0.02 * i
        out.append(make(name if i == 0 else "%s_%d" % (name, a), pos, (length - 0.25 * i, d * k, d * k), paint,
                        q=G.qx(a) if a else K.ID_Q))
    return out


def build():
    """Colt Single Action Army 1873, canon « Cavalry » 7 1/2 pouces (forme transformee de Green Colt) : canon rond bleui
    grave d'or et veine de jade, guidon en lame, tube d'extracteur et sa tete, barillet a six cannelures et six chambres,
    axe de barillet, carcasse trempee en paquet (marbrures), pontet et dos de crosse en laiton, porte de chargement,
    chien arme a crete quadrillee, crosse d'ivoire a medaillon d'emeraude. Reperes du pivot Corps (noeud Base vanilla) :
    bouche vers -x, haut +y, cote droit +z ; le haut de la crosse est vers x = 3,5."""
    box, lit, pivot = G.box, G.lit, G.pivot
    blued = G.metal((34, 44, 70), 0.3, edge=G.GOLD)
    barrel = scrolls((40, 52, 80), G.GOLD)
    frame = case_hardened()
    brass = G.metal(BRASS, 0.32)
    steel = G.metal(STEEL, 0.2)
    jade = G.glow(JADE, JADE_CORE)
    BY = 5.5                                                                  # axe du canon
    CY = 3.25                                                                 # axe du barillet (chambre du haut alignee)
    corps = []
    corps += tube_x("Canon", (-19.0, BY, 0), 3.4, 22.0, barrel)
    corps += [box("Bouche", (-30.1, BY, 0), (0.25, 2.6, 2.6), steel),
              lit("Ame", (-30.3, BY, 0), (0.25, 1.4, 1.4), jade),
              box("Guidon", (-28.8, BY + 2.1, 0), (1.6, 1.0, 0.5), steel),
              box("Fut_Canon", (-8.75, BY, 0), (1.5, 4.2, 4.2), frame)]           # renfort vissé dans la carcasse
    corps += tube_x("Extracteur_Tube", (-14.5, BY - 2.2, 1.6), 1.5, 13.0, blued, n=2)
    corps += [box("Extracteur_Tete", (-16.5, BY - 2.2, 2.6), (1.4, 1.2, 1.0), blued),
              box("Extracteur_Vis", (-21.25, BY - 2.2, 1.6), (0.5, 1.6, 1.6), blued)]
    # barillet
    corps += tube_x("Barillet", (-2.0, CY, 0), 7.0, 8.0, flutes((38, 48, 76), (24, 30, 50), 5))
    for k in range(6):
        a = math.radians(60 * k + 30)
        corps.append(box("Chambre_%d" % k, (-6.1, CY + 2.15 * math.sin(a), 2.15 * math.cos(a)), (0.25, 1.3, 1.3), G.metal((10, 10, 14), 0.02)))
    corps += [box("Axe", (-7.0, CY, 0), (2.0, 1.2, 1.2), steel),
              box("Axe_Tete", (-8.1, CY - 0.9, 0), (0.6, 1.2, 1.4), steel)]
    # carcasse : avant, pont superieur, bouclier, sous le barillet
    corps += [box("Carcasse_Avant", (-7.0, CY + 0.5, 0), (2.0, 7.5, 4.6), frame),
              box("Pont", (-2.0, 7.4, 0), (10.0, 1.0, 3.2), frame),
              box("Rainure_Visee", (-2.0, 7.95, 0), (10.0, 0.25, 1.0), G.metal((20, 22, 30), 0.05)),
              box("Bouclier", (2.75, 3.75, 0), (1.5, 8.0, 5.2), frame),
              box("Carcasse_Bas", (-2.0, -0.6, 0), (12.0, 1.4, 4.0), frame),
              box("Porte_Chargement", (3.0, CY, 2.7), (1.0, 2.2, 0.5), frame),
              box("Vis_Carcasse_A", (-4.0, 0.0, 2.05), (0.6, 0.6, 0.25), steel),
              box("Vis_Carcasse_B", (1.0, 1.0, 2.7), (0.6, 0.6, 0.25), steel)]
    # chien arme (crete quadrillee) et detente, pontet en laiton
    corps += [box("Chien", (4.1, 5.2, 0), (1.4, 5.0, 1.4), frame, q=G.qz(-18)),
              box("Chien_Crete", (5.3, 7.8, 0), (2.2, 1.0, 1.6), checker((150, 150, 160), (60, 60, 70), 3), q=G.qz(-30)),
              box("Queue_Carcasse", (3.2, -2.3, 0), (4.6, 3.0, 3.4), frame, q=G.qz(-15)),
              box("Detente", (-1.0, -2.6, 0), (0.8, 2.8, 0.9), brass, q=G.qz(-15)),
              box("Pontet", (-1.25, -4.6, 0), (6.0, 0.8, 1.6), brass),
              box("Pontet_Avant", (-4.0, -3.0, 0), (0.8, 3.2, 1.6), brass, q=G.qz(10)),
              box("Pontet_Arriere", (1.6, -3.2, 0), (0.8, 2.8, 1.6), brass, q=G.qz(-20))]
    corps += [lit("Veine_D", (-19.0, BY, 1.72), (20.0, 0.5, 0.25), jade),
              lit("Veine_G", (-19.0, BY, -1.72), (20.0, 0.5, 0.25), jade)]
    van = G.vanilla_handle_children(VANILLA)
    base = next(c for c in van["children"] if c["name"] == "Base")
    body = pivot("Corps", (base["position"]["x"], base["position"]["y"], base["position"]["z"]), base["orientation"], corps)
    ivory = G.wood(IVORY, 0.06)
    kids = [body,
            box("Dos", (2.9, 0.0, 0), (0.9, 13.0, 3.0), brass),                                   # dos de crosse en laiton
            box("Talon", (0.4, -6.9, 0), (5.6, 0.9, 3.4), brass, q=G.qz(-8)),
            box("Talon_Anneau", (1.0, -7.7, 0), (1.0, 1.0, 0.5), brass)]
    for s in (1, -1):
        n = "D" if s > 0 else "G"
        kids += [box("Plaquette_%s" % n, (-0.1, -0.3, 1.95 * s), (5.2, 12.4, 0.75), ivory),
                 box("Medaillon_%s" % n, (-0.1, 2.6, 2.45 * s), (2.4, 2.4, 0.5), G.metal(G.GOLD_LIGHT)),
                 box("Emeraude_%s" % n, (-0.1, 2.6, 2.65 * s), (1.4, 1.4, 0.5), G.gem((40, 200, 100))),
                 box("Vis_Plaquette_%s" % n, (-0.1, -2.5, 2.45 * s), (0.6, 0.6, 0.25), steel)]
    return G.mount(VANILLA, (4.8, 13.0, 3.0), G.wood(IVORY, 0.06), kids)


def bullet():
    """Balle divine en vol : traceur fin et allonge (coeur blanc, gaine doree), le long de z (axe de vol)."""
    G.M = K.Model("Balle_Divine")
    G.M.reuse = True
    kids = [G.lit("Noyau", (0, 0, 0), (1.25, 1.25, 14), G.glow(HOLY, HOLY_CORE)),
            G.lit("Coeur", (0, 0, 0), (0.75, 0.75, 18), G.glow(HOLY_CORE, (255, 255, 255)))]
    root = G.pivot("Root", (0, 0, 0), K.ID_Q, kids)
    G.renumber(root)
    W, H, tex = G.M.texture()
    out = os.path.join(RES, "Common", "Items", "Projectiles")
    png.write(os.path.join(out, "Balle_Divine.png"), W, H, tex)
    json.dump({"nodes": [root], "lod": "auto"}, open(os.path.join(out, "Balle_Divine.blockymodel"), "w"), indent=1)
    trail = {"TexturePath": "Trails/Gold.png", "LifeSpan": 4, "Roll": 0, "Start": {"Color": "rgba(#fff8e0, 0.9)", "Width": 0.07},
             "End": {"Color": "rgba(#ffb030, 0)", "Width": 0}, "LightInfluence": 0, "Smooth": True, "RenderMode": "BlendLinear"}
    json.dump(trail, open(os.path.join(RES, "Server", "Entity", "Trails", "Balle_Divine_Trail.json"), "w"), indent=2)
    asset = {"$Comment": "Balle divine (Aigles du Desert), deplacee par le code (AiglesDuDesert). HitBox elargie (LOD serveur). "
                         "Genere par scripts/artefacts/gen_aigles_akimbo.py.",
             "Model": "Items/Projectiles/Balle_Divine.blockymodel", "Texture": "Items/Projectiles/Balle_Divine.png",
             "MinScale": 1.0, "MaxScale": 1.0,
             "HitBox": {"Max": {"X": 0.6, "Y": 0.1, "Z": 0.1}, "Min": {"X": -0.6, "Y": -0.1, "Z": -0.1}},
             "Trails": [{"TrailId": "Balle_Divine_Trail", "TargetNodeName": "Noyau", "RotationOffset": {"Roll": r}} for r in (0, 90)],
             "Light": {"Color": "#ffd070", "Radius": 3}}
    json.dump(asset, open(os.path.join(RES, "Server", "Models", "Projectiles", "Abilities", "Balle_Divine.json"), "w"), indent=2)
    neutral = {"Interactions": [{"Type": "Simple", "RunTime": 0.01}]}
    config = {"$Comment": "Balle divine, deplacee par le code (AiglesDuDesert). Interactions neutres obligatoires.",
              "Model": "Balle_Divine", "UseModelScale": True,
              "Physics": {"Type": "Standard", "Gravity": 0, "TerminalVelocityAir": 1, "TerminalVelocityWater": 1, "Bounciness": 0, "SticksVertically": False},
              "LaunchForce": 0, "SpawnOffset": {"X": 0, "Y": 0, "Z": 0},
              "Interactions": {"ProjectileHit": neutral, "ProjectileMiss": neutral}}
    json.dump(config, open(os.path.join(RES, "Server", "ProjectileConfigs", "Abilities", "Projectile_Config_Balle_Divine.json"), "w"), indent=2)


# ---------------------------------------------------------------------------------------------------------------------
# Munition : cartouche .50 a ogive de minerai divin (item)
# ---------------------------------------------------------------------------------------------------------------------
AMMO = "Munition_Balle_Divine"
AMMO_PER_CRAFT = 12
SIGNATURE_AMMO = 15                                           # rafale de 12 + balle de jugement (3)


def cartridge():
    """Cartouche sur la hierarchie de la fleche vanilla (tenue et lachee comme elle) : douille de laiton octogonale,
    bourrelet et gorge d'extraction, amorce, ogive d'or blanc au coeur lumineux, croix gravee."""
    G.M = K.Model(AMMO)
    G.M.reuse = True
    brass, brass_dark = G.metal(BRASS, 0.3), G.metal(G.mixc(BRASS, (60, 40, 20), 0.35), 0.15)
    head = G.metal(G.mixc(G.GOLD_LIGHT, (255, 255, 255), 0.35), 0.35)
    kids = [G.box("Douille_B", (0, 0, 0), (12, 4.5, 4.5), brass, q=G.qx(45)),
            G.box("Gorge", (-6.75, 0, 0), (1.5, 3.75, 3.75), brass_dark),
            G.box("Bourrelet", (-8, 0, 0), (1, 5, 5), brass),
            G.box("Amorce", (-8.625, 0, 0), (0.5, 1.75, 1.75), G.metal((200, 150, 120), 0.3)),
            G.box("Ogive", (7.5, 0, 0), (3.5, 4, 4), head),
            G.box("Ogive_B", (7.5, 0, 0), (3.25, 4, 4), head, q=G.qx(45)),
            G.box("Pointe", (9.75, 0, 0), (1.5, 2.75, 2.75), head),
            G.lit("Coeur", (10.6, 0, 0), (0.75, 1.5, 1.5), G.glow(HOLY_CORE, (255, 255, 255))),
            G.lit("Croix_V", (1, 0, 2.3), (0.5, 3, 0.5), G.glow(HOLY, HOLY_CORE)),
            G.lit("Croix_H", (1.75, 0, 2.3), (2, 0.5, 0.75), G.glow(HOLY, HOLY_CORE))]
    root, handle = G.mount("Items/Weapons/Arrow/Arrow.blockymodel", (12, 4.5, 4.5), brass, kids)
    G.renumber(root)
    G.fix_coplanar(handle, AMMO)
    W, H, tex = G.M.texture()
    png.write(os.path.join(RES, "Common", MODEL_REL, AMMO + ".png"), W, H, tex)
    json.dump({"nodes": [root], "lod": "auto"}, open(os.path.join(RES, "Common", MODEL_REL, AMMO + ".blockymodel"), "w"), indent=1)
    lifted = [[[min(255, c[0] * 1.12 + 10), min(255, c[1] * 1.12 + 10), min(255, c[2] * 1.12 + 12), c[3]] for c in row] for row in tex]
    S = 256
    one = K.draw(G.handle_quads(handle, lifted, G.qz(55)), S, 15, 10, margin=0.32)
    big = [[[0, 0, 0, 0] for _ in range(S)] for _ in range(S)]
    for dx, dy, k in ((40, -28, 0.78), (0, 0, 0.9), (-40, 28, 1.0)):                       # trois cartouches alignees
        for y in range(S):
            for x in range(S):
                p = one[y][x]
                X, Y = x + dx, y + dy
                if p[3] > 0 and 0 <= X < S and 0 <= Y < S:
                    big[Y][X] = [p[0] * k, p[1] * k, p[2] * k, p[3]]
    icon = G.outline(K.downscale(big, 4))
    png.write(os.path.join(RES, "Common", "Icons", "ItemsGenerated", AMMO + ".png"), 64, 64, icon)
    arrow = json.loads(Z.read("Server/Item/Items/Weapon/Arrow/Weapon_Arrow_Crude.json"))
    data = {
        "$Comment": "Munition des Colts de cavalerie (ex-Aigles du Desert) (une par tir, %d par signature). Modele, icone et ce fichier : scripts/artefacts/gen_aigles_akimbo.py." % SIGNATURE_AMMO,
        "TranslationProperties": {
            "Name": "Balle Divine",
            "Description": "Cartouche lourde à ogive de minerai divin fondu, gravée d'une croix. Munitions des Colts de cavalerie (forme de Green Colt) : chargeurs de 5, "
                           "une balle par tir, %d pour le Jugement Céleste. Forgée par %d à la Forge Divine." % (SIGNATURE_AMMO, AMMO_PER_CRAFT)},
        "Categories": ["Items.Weapons"],
        "Quality": "Epic",
        "ItemLevel": 300,
        "Model": "%s/%s.blockymodel" % (MODEL_REL, AMMO),
        "Texture": "%s/%s.png" % (MODEL_REL, AMMO),
        "Icon": "Icons/ItemsGenerated/%s.png" % AMMO,
        "IconProperties": arrow["IconProperties"],
        "MaxStack": 100,
        "DroppedItemAnimation": arrow["DroppedItemAnimation"],
        "Tags": {"Type": ["Weapon"], "Family": ["Munition"]},
        "Recipe": {"TimeSeconds": 3,
                   "Input": [{"ItemId": "Ingredient_Bar_Divin", "Quantity": 1}, {"ItemId": "Ingredient_Bar_Iron", "Quantity": 1}],
                   "OutputQuantity": AMMO_PER_CRAFT,
                   "BenchRequirement": [{"Type": "Crafting", "Id": "Forge_Divine", "Categories": ["Forge_Arcs"]}]},
    }
    with open(os.path.join(RES, "Server", "Item", "Items", "Weapon", "Handgun", AMMO + ".json"), "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")
    return icon


# ---------------------------------------------------------------------------------------------------------------------
# Icone (deux pistolets croises) et apercu
# ---------------------------------------------------------------------------------------------------------------------
def icon_and_preview(handle, tex, ammo_icon):
    lifted = [[[min(255, c[0] * 1.12 + 12), min(255, c[1] * 1.12 + 12), min(255, c[2] * 1.12 + 14), c[3]] for c in row] for row in tex]
    S = 256
    front = K.draw(G.handle_quads(handle, lifted, G.qz(32)), S, 12, 8, margin=0.2)        # canon a gauche, legerement releve
    back = [row[::-1] for row in front]                                                     # le jumeau, en miroir, derriere
    big = [[[0, 0, 0, 0] for _ in range(S)] for _ in range(S)]
    for img, dx, dy, k in ((back, 26, -20, 0.8), (front, -22, 18, 1.0)):
        for y in range(S):
            for x in range(S):
                p = img[y][x]
                X, Y = x + dx, y + dy
                if p[3] > 0 and 0 <= X < S and 0 <= Y < S:
                    big[Y][X] = [p[0] * k, p[1] * k, p[2] * k, p[3]]
    icon = G.outline(K.downscale(big, 4))
    png.write(os.path.join(RES, "Common", "Icons", "ItemsGenerated", NAME + ".png"), 64, 64, icon)
    if PREVIEW:
        os.makedirs(PREVIEW, exist_ok=True)
        side = K.draw(G.handle_quads(handle, tex, G.qz(20)), 512, 0, 0, margin=0.05)
        three = K.draw(G.handle_quads(handle, tex, G.qz(20)), 512, 35, 20, margin=0.05)
        sheet = [[[44, 44, 52, 255] for _ in range(1024 + 256)] for _ in range(512)]
        for ox, img in ((0, side), (512, three)):
            for y in range(512):
                for x in range(512):
                    if img[y][x][3] > 0:
                        sheet[y][ox + x] = list(img[y][x][:3]) + [255]
        for oy, ic in ((0, icon), (256, ammo_icon)):
            for y in range(256):
                for x in range(256):
                    p = ic[y // 4][x // 4]
                    if p[3] > 0:
                        sheet[oy + y][1024 + x] = list(p[:3]) + [255]
        png.write(os.path.join(PREVIEW, "aigles_apercu.png"), 1024 + 256, 512, sheet)


# ---------------------------------------------------------------------------------------------------------------------
# Animations
# ---------------------------------------------------------------------------------------------------------------------
ANIM_SET = "Aigles_Akimbo"
SRC_ANIMS = "Characters/Animations/Items/Main_Handed/Handgun/"
DST_ANIMS = "Characters/Animations/Items/Dual_Handed/Aigles_Akimbo/"
DOUBLE = ["Idle", "Walk", "WalkBackward", "Run", "RunBackward", "Sprint", "Crouch", "CrouchWalk", "CrouchWalkBackward",
          "Jump", "JumpWalk", "JumpRun", "Fall", "FallFar", "Guard", "Interact"]


def side_swap(name):
    for a, b in (("R-", "L-"), ("L-", "R-"), ("R_", "L_"), ("L_", "R_")):
        if name.startswith(a):
            return b + name[len(a):]
    return None


def mirror_track(track):
    out = copy.deepcopy(track)
    for k in out.get("orientation", []):
        k["delta"]["y"], k["delta"]["z"] = -k["delta"]["y"], -k["delta"]["z"]
    for k in out.get("position", []):
        k["delta"]["x"] = -k["delta"]["x"]
    return out


def static(track):
    """Pose figee : la premiere cle de chaque piste."""
    out = {}
    for key, frames in track.items():
        if isinstance(frames, list) and frames:
            f = copy.deepcopy(frames[0])
            f["time"] = 0
            out[key] = [f]
        else:
            out[key] = copy.deepcopy(frames)
    return out


def mirrored(anim):
    out = copy.deepcopy(anim)
    out["nodeAnimations"] = {(side_swap(n) or n): mirror_track(t) for n, t in anim.get("nodeAnimations", {}).items()}
    return out


def doubled(anim):
    out = copy.deepcopy(anim)
    nodes = out.setdefault("nodeAnimations", {})
    for n in [n for n in nodes if n.startswith(("L-", "L_"))]:
        del nodes[n]
    for n, t in anim.get("nodeAnimations", {}).items():
        if n.startswith(("R-", "R_")):
            nodes[side_swap(n)] = mirror_track(t)
    return out


def with_aim(anim, idle, side):
    """Ajoute le bras de l'autre cote, fige dans sa pose de visee (pose de repos du bras droit, en miroir si gauche)."""
    out = copy.deepcopy(anim)
    nodes = out.setdefault("nodeAnimations", {})
    for n, t in idle.get("nodeAnimations", {}).items():
        if not n.startswith(("R-", "R_")):
            continue
        if side == "L":
            nodes[side_swap(n)] = static(mirror_track(t))
        else:
            nodes[n] = static(t)
    return out


def animations():
    z = Z
    anim_set = json.loads(z.read("Server/Item/Animations/Handgun.json"))
    pitch = anim_set.setdefault("Camera", {}).setdefault("Pitch", {}).setdefault("TargetNodes", [])
    if "LShoulder" not in pitch:
        pitch.append("LShoulder")
    entries = anim_set["Animations"]
    written = {}
    load = lambda p: json.loads(z.read("Common/" + p))
    idle_tp, idle_fp = SRC_ANIMS + "Idle.blockyanim", SRC_ANIMS + "Idle_FPS.blockyanim"

    def derive(entry, kind, fn):
        entry = copy.deepcopy(entry)
        for key, path in list(entry.items()):
            if not (isinstance(path, str) and path.startswith(SRC_ANIMS) and path.endswith(".blockyanim")):
                continue
            dst = DST_ANIMS + path[len(SRC_ANIMS):-len(".blockyanim")] + "_" + kind + ".blockyanim"
            if dst not in written:
                written[dst] = fn(load(path), load(idle_fp if key == "FirstPerson" else idle_tp))
            entry[key] = dst
        return entry

    shoot = entries["Shoot"]
    entries["ShootLeft"] = derive(shoot, "Gauche", lambda a, idle: with_aim(mirrored(a), idle, "R"))
    entries["ShootBoth"] = derive(shoot, "Double", lambda a, idle: doubled(a))
    entries["Shoot"] = derive(shoot, "Droite", lambda a, idle: with_aim(a, idle, "L"))
    bash = entries["GuardBash"]                                                     # gun-kata : crosse droite, puis gauche
    entries["GuardBashLeft"] = derive(bash, "Gauche", lambda a, idle: with_aim(mirrored(a), idle, "R"))
    entries["GuardBash"] = derive(bash, "Droite", lambda a, idle: with_aim(a, idle, "L"))
    for name in DOUBLE:
        if name in entries:
            entries[name] = derive(entries[name], "Double", lambda a, idle: doubled(a))
    for dst, anim in written.items():
        path = os.path.join(RES, "Common", dst)
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w", encoding="utf-8") as f:
            json.dump(anim, f, separators=(",", ":"))
    with open(os.path.join(RES, "Server", "Item", "Animations", ANIM_SET + ".json"), "w", encoding="utf-8") as f:
        json.dump(anim_set, f, indent=2)
        f.write("\n")
    return len(written)


# ---------------------------------------------------------------------------------------------------------------------
# Interactions et item
# ---------------------------------------------------------------------------------------------------------------------
SHOT_FX = {"CameraEffect": "Handgun_Shoot", "WorldSoundEventId": "SFX_Handgun_Fire", "LocalSoundEventId": "SFX_Handgun_Fire_Local"}
RAFALE = 12


def shot(anim, spell, wait):
    return [{"Type": "Simple", "RunTime": 0.02, "Effects": dict(SHOT_FX, ItemAnimationId=anim)},
            {"Type": "RuneSpell", "RunTime": 0.05, "Spell": spell},
            {"Type": "Simple", "RunTime": wait}]


def with_mag(quantity, then):
    """Colts : balles prises dans les chargeurs (5 + 5 Balles Divines, rechargement 0,6 s, Mousquet.coltTrigger) ; chargeur
    vide ou rechargement en cours : cliquetis a vide."""
    return {"Type": "Eldanior_MousquetTir", "Colt": quantity, "Next": then, "Failed": "Gun_Shoot_Fail"}


def with_ammo(quantity, then):
    """Retire des Balles Divines de l'inventaire avant de tirer ; sans munitions, cliquetis a vide (Gun_Shoot_Fail vanilla).
    Pas de RequiredGameMode : il en faut aussi en creatif."""
    return {"Type": "ModifyInventory", "ItemToRemove": {"Id": AMMO, "Quantity": quantity}, "Next": then, "Failed": "Gun_Shoot_Fail"}


def crosse(anim, direction, yaw, knock_x):
    """Coup de crosse (gun-kata) : balayage horizontal au corps a corps, comme Gun_Swing_Left vanilla, plus fort."""
    damage = {"Type": "DamageEntity", "DamageCalculator": {"BaseDamage": {"Physical": BASH_DAMAGE}},
              "EntityStatsOnHit": [{"EntityStatId": "SignatureEnergy", "Amount": 2}],
              "DamageEffects": {"Knockback": {"Force": 1.0, "RelativeX": knock_x, "RelativeZ": -5, "VelocityY": 5},
                                "WorldSoundEventId": "SFX_Unarmed_Impact", "WorldParticles": [{"SystemId": "Impact_Blade_01"}]}}
    return {"Type": "Simple", "RunTime": 0.15,
            "Effects": {"ItemAnimationId": anim, "WorldSoundEventId": "SFX_Unarmed_Swing",
                        "Trails": [{"TrailId": "Excalibur_Trail", "TargetNodeName": "Handle", "RotationOffset": {"Pitch": 45}}]},
            "Next": {"Type": "Selector", "RunTime": 0.083,
                     "Selector": {"Id": "Horizontal", "Direction": direction, "TestLineOfSight": True, "ExtendTop": 0.5, "ExtendBottom": 0.5,
                                  "StartDistance": 0.1, "EndDistance": 3.0, "Length": 60, "RollOffset": 0, "YawStartOffset": yaw},
                     "HitEntity": {"Interactions": [damage]},
                     "Next": {"Type": "Simple", "RunTime": 0.22}}}


def interactions():
    d_int = os.path.join(RES, "Server", "Item", "Interactions", "Weapons", "Aigles_Akimbo")
    d_root = os.path.join(RES, "Server", "Item", "RootInteractions", "Weapons", "Aigles_Akimbo")
    os.makedirs(d_int, exist_ok=True)
    os.makedirs(d_root, exist_ok=True)
    heavy = dict(SHOT_FX, ItemAnimationId="ShootBoth")
    files = {
        # ---- clic : Cadence de l'Aigle (droite, gauche, droite, doublé) ; maintien : Balle Perforante
        "Aigles_Tir_Droite": dict(with_mag(1, {"Type": "Serial", "Interactions": shot("Shoot", "AiglesBalleDroite", 0.09)}),
                                  **{"$Comment": "Aigles du Desert : tir du pistolet droit (chargeur : 1 balle), sort Java AiglesBalleDroite."}),
        "Aigles_Tir_Gauche": dict(with_mag(1, {"Type": "Serial", "Interactions": shot("ShootLeft", "AiglesBalleGauche", 0.09)}),
                                  **{"$Comment": "Aigles du Desert : tir du pistolet gauche (chargeur : 1 balle), sort Java AiglesBalleGauche."}),
        "Aigles_Double": dict(with_mag(2, {"Type": "Serial", "Interactions": [
                                  {"Type": "Simple", "RunTime": 0.03, "Effects": heavy},
                                  {"Type": "RuneSpell", "RunTime": 0.05, "Spell": "AiglesDoubleTir"},
                                  {"Type": "Simple", "RunTime": 0.15}]}),
                              **{"$Comment": "Aigles du Desert : 4e temps de la cadence, les deux pistolets tirent ensemble (chargeur : 2 balles)."}),
        "Aigles_Perforante": dict(with_mag(3, {"Type": "Serial", "Interactions": [
                                      {"Type": "Simple", "RunTime": 0.08, "Effects": dict(heavy, CameraEffect="Handgun_Shoot")},
                                      {"Type": "RuneSpell", "RunTime": 0.05, "Spell": "AiglesPerforante"},
                                      {"Type": "Simple", "RunTime": 0.5, "Effects": {"ClearAnimationOnFinish": True}}]}),
                                  **{"$Comment": "Aigles du Desert : attaque maintenue, Balle Perforante (chargeur : 3 balles) : traverse tous les ennemis et 4 blocs."}),
        "Aigles_Tir": {"$Comment": "Aigles du Desert : clic = Cadence de l'Aigle (droite, gauche, droite, double tir des deux pistolets) ; "
                                   "maintien %.2f s = Balle Perforante. Genere par scripts/artefacts/gen_aigles_akimbo.py." % HOLD,
                       "Type": "Charging", "AllowIndefiniteHold": True,
                       "Next": {"0": {"Type": "Chaining", "ChainingAllowance": 0.9,
                                      "Next": ["Aigles_Tir_Droite", "Aigles_Tir_Gauche", "Aigles_Tir_Droite", "Aigles_Double"]},
                                str(HOLD): "Aigles_Perforante"}},
        # ---- clic droit : gun-kata (crosse droite, crosse gauche, bout portant)
        "Aigles_Crosse_Droite": crosse("GuardBash", "ToLeft", -15, -5),
        "Aigles_Crosse_Gauche": crosse("GuardBashLeft", "ToRight", 15, 5),
        "Aigles_Bout_Portant": dict(with_mag(2, {"Type": "Serial", "Interactions": [
                                        {"Type": "ApplyForce", "Force": 9, "Direction": {"X": 0, "Y": 0, "Z": -10}, "AdjustVertical": True,
                                         "VerticalClamp": [-85, 10], "WaitForGround": False, "ChangeVelocityType": "Add",
                                         "VelocityConfig": {"AirResistance": 0.97, "AirResistanceMax": 0.96, "GroundResistance": 0.94,
                                                            "GroundResistanceMax": 0.82, "Threshold": 3.0, "Style": "Exp"}},
                                        {"Type": "Simple", "RunTime": 0.12, "Effects": dict(heavy, CameraEffect="Handgun_Shoot")},
                                        {"Type": "RuneSpell", "RunTime": 0.05, "Spell": "AiglesBoutPortant"},
                                        {"Type": "Simple", "RunTime": 0.4, "Effects": {"ClearAnimationOnFinish": True}}]}),
                                    **{"$Comment": "Aigles du Desert : 3e temps du gun-kata, bond et double tir a bout portant (chargeur : 2 balles), sort AiglesBoutPortant."}),
        "Aigles_Kata": {"$Comment": "Aigles du Desert : clic droit = gun-kata (crosse droite, crosse gauche, bout portant).",
                        "Type": "Chaining", "ChainingAllowance": 1.1,
                        "Next": ["Aigles_Crosse_Droite", "Aigles_Crosse_Gauche", "Aigles_Bout_Portant"]},
    }
    seq = [{"Type": "Simple", "RunTime": 0.3, "HorizontalSpeedMultiplier": 0.5,
            "Effects": {"ItemAnimationId": "ShootBoth", "LocalSoundEventId": "SFX_Sword_T2_Lunge_Charge_Local",
                        "Particles": [{"SystemId": "Sword_Charging", "TargetNodeName": "Handle", "PositionOffset": {"X": 0.3}}]}}]
    for i in range(RAFALE):
        right = i % 2 == 0
        seq += shot("Shoot" if right else "ShootLeft", "AiglesRafaleDroite" if right else "AiglesRafaleGauche", 0.04)
    seq += [{"Type": "Simple", "RunTime": 0.25, "Effects": heavy},
            {"Type": "RuneSpell", "RunTime": 0.05, "Spell": "AiglesJugement"},
            {"Type": "Simple", "RunTime": 0.3, "Effects": {"ClearAnimationOnFinish": True}},
            {"Type": "ChangeStat", "StatModifiers": {"SignatureEnergy": -100}, "ValueType": "Percent"}]
    files["Aigles_Signature"] = {
        "$Comment": "Signature des Aigles du Desert, Jugement Celeste (energie pleine, %d Balles Divines) : rafale de %d balles qui "
                    "cherchent les ennemis (AiglesRafaleDroite/Gauche), puis les deux pistolets tirent la balle de jugement (AiglesJugement). "
                    "Genere par scripts/artefacts/gen_aigles_akimbo.py." % (SIGNATURE_AMMO, RAFALE),
        "Type": "StatsCondition", "Costs": {"SignatureEnergy": 100}, "ValueType": "Percent",
        "Next": with_ammo(SIGNATURE_AMMO, {"Type": "Serial", "Interactions": seq})}
    for name, obj in files.items():
        json.dump(obj, open(os.path.join(d_int, name + ".json"), "w"), indent=2, ensure_ascii=False)
    json.dump({"Interactions": ["Aigles_Tir"]}, open(os.path.join(d_root, "Root_Aigles_Tir.json"), "w"), indent=2)
    json.dump({"Interactions": ["Aigles_Kata"], "Tags": {"Attack": ["Melee"]}}, open(os.path.join(d_root, "Root_Aigles_Kata.json"), "w"), indent=2)
    json.dump({"RequireNewClick": True, "Interactions": ["Aigles_Signature"]}, open(os.path.join(d_root, "Root_Aigles_Signature.json"), "w"), indent=2)


def item():
    van = json.loads(Z.read("Server/Item/Items/Hypixel/Minigames/GunPvP/Weapon_Handgun.json"))
    ready = copy.deepcopy(G.SIGNATURE_READY)
    for p in ready["SignatureEnergy"][0]["Particles"] + ready["SignatureEnergy"][0]["FirstPersonParticles"]:
        p["PositionOffset"] = {"X": 0.3}
    data = {
        "$Comment": "Aigles du Desert : deux pistolets lourds (modele maison sur la hierarchie du pistolet vanilla), rendus dans les deux mains, "
                    "animations Aigles_Akimbo, balles divines deplacees par le code (AiglesDuDesert.java). "
                    "Modele, textures, icone, animations, interactions et ce fichier : scripts/artefacts/gen_aigles_akimbo.py.",
        "TranslationProperties": {
            "Name": "Colts de cavalerie (Green Colt)",
            "Description": "Forme transformée de Green Colt, pour 5 minutes : deux Colt Single Action Army 1873 à canon de cavalerie, "
                           "carcasse trempée, crosses d'ivoire à émeraude. Chargeurs de 5 Balles Divines par pistolet, rechargés en 0,6 s ; "
                           "portée 40 blocs ; dégâts ×1,5, augmentés par la Force. "
                           "Cadence (clics) : droite, gauche, droite, puis les deux ensemble ; une balle sur sept est une Balle de Jugement "
                           "(colonne de lumière). Maintenir : Balle Perforante (3 balles). Clic droit, gun-kata : crosse droite, crosse "
                           "gauche, bond et double tir à bout portant. Rythme du Pistolero : +6 %% par touche enchaînée (jusqu'à +60 %%). "
                           "Signature, Jugement Céleste (%d Balles Divines de l'inventaire) : rafale de douze balles chercheuses puis "
                           "une balle géante et une immense colonne de lumière." % SIGNATURE_AMMO},
        "Quality": "Legendary",
        "ItemLevel": 580,
        "Model": "%s/%s.blockymodel" % (MODEL_REL, NAME),
        "Texture": "%s/%s.png" % (MODEL_REL, NAME),
        "Icon": "Icons/ItemsGenerated/%s.png" % NAME,
        "IconProperties": van["IconProperties"],
        "PlayerAnimationsId": ANIM_SET,
        "Categories": [],
        "MaxStack": 1,
        "Variant": False,
        "Interactions": {"Primary": "Root_Aigles_Tir", "Secondary": "Root_Aigles_Kata", "Ability1": "Root_Aigles_Signature"},
        "Weapon": {"EntityStatsToClear": ["SignatureEnergy"],
                   "StatModifiers": {"SignatureEnergy": [{"Amount": 20, "CalculationType": "Additive"}]},
                   "RenderDualWielded": True},
        "MaxDurability": 0,
        "DurabilityLossOnHit": 0,
        "Tags": {"Type": ["Weapon"]},
    }
    path = os.path.join(RES, "Server", "Item", "Items", "Weapon", "Handgun", NAME + ".json")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


if __name__ == "__main__":
    for d in (("Common", MODEL_REL), ("Common", "Icons", "ItemsGenerated"), ("Common", "Items", "Projectiles"), ("Server", "Entity", "Trails"),
              ("Server", "Models", "Projectiles", "Abilities"), ("Server", "ProjectileConfigs", "Abilities"), ("Server", "Item", "Animations")):
        os.makedirs(os.path.join(RES, *d), exist_ok=True)
    assert "Common/Trails/Gold.png" in G.NAMES, "texture de traine vanilla introuvable"
    G.M = K.Model(NAME)
    G.M.reuse = True
    root, handle = build()
    G.renumber(root)
    G.fix_coplanar(handle, "Aigles")
    W, H, tex = G.M.texture()
    png.write(os.path.join(RES, "Common", MODEL_REL, NAME + ".png"), W, H, tex)
    json.dump({"nodes": [root], "lod": "auto"}, open(os.path.join(RES, "Common", MODEL_REL, NAME + ".blockymodel"), "w"), indent=1)
    ammo_icon = cartridge()
    icon_and_preview(handle, tex, ammo_icon)
    n = animations()
    bullet()
    interactions()
    item()
    print("ok : %s (texture %dx%d), %d animations" % (NAME, W, H, n))
