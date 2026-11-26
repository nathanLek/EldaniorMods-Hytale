"""Katanas du Clan Luo : bokken du dojo, katana de fer, Aonami (Rare), Tsukikage (Epique), Shirasagi et Hien (Unique), Kinryu-O
(Divin, tresor du Patriarche : maintien et signature propres, Java Kinryu).

Ils fonctionnent exactement comme Tenryumaru (gen_tenryumaru.py) : katana au fourreau dans la main gauche, lame vers l'arriere ;
la main droite degaine au premier coup, la lame reste au clair tant que l'on frappe, clic droit bref = rengainer, maintenu =
garde ; maintien de l'attaque = posture iai puis degaine (sort Java LuoIai) ; signature = Iaigiri (sort Java LuoIaigiri, un
Flash Celeste a l'echelle du katana). Memes animations (jeux Tenryumaru_Fourreau / Tenryumaru_Degainee) : le modele a
exactement la geometrie de Tenryumaru, seules les couleurs changent (globales de gen_tenryumaru remplacees avant chaque
construction). Reglages des sorts : LuoKatanas.java. Reserves aux membres du Clan Luo (MasterySystem).

Quatre objets par katana : <id> (au fourreau), <id>_Degainee (au clair, hors menu creatif), <id>_Fourreau et
<id>_Fourreau_Vide (objets utilitaires de la main gauche, poses par TenryumaruSystem).
Degats : epee de fer vanilla x multiplicateur (regle du catalogue). Le Clan Luo n'est pas ouvert au monde exterieur : ni
recette, ni butin (decision de l'utilisateur, 2026-11-06).

Usage : python3 scripts/artefacts/gen_luo_katanas.py <src/main/resources> [dossier_apercu]
"""
import copy, json, os, sys, zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
RES = sys.argv[1]
PREVIEW = sys.argv[2] if len(sys.argv) > 2 else None
sys.argv = [sys.argv[0], RES]                                   # gen_tenryumaru lit sys.argv a l'import
sys.path.insert(0, HERE)
import gen_tenryumaru as G  # noqa: E402
import tenryumaru_anims as A  # noqa: E402
K, png, familles = G.K, G.png, G.familles

MODEL_REL = G.MODEL_REL
EMPTY_MAIN = MODEL_REL + "/" + G.NAME + "_Main_Vide.blockymodel"     # rien dans la main droite (cree par gen_tenryumaru)
QUALITY = {"Commune": "Common", "Rare": "Rare", "Epique": "Epic", "Unique": "Unique", "Divin": "Divin"}
Z = zipfile.ZipFile(K.ASSETS)

GOLD3 = (K.GOLD, K.GOLD_LIGHT, K.GOLD_DARK)
KATANAS = [
    dict(id="Eldanior_Weapon_Sword_Katana_Bokken", name="Bokken du Dojo Luo", rarity="Commune", level=3, mult=0.6, durability=150,
         pal=dict(BLACK=(120, 80, 44), BLACK_SHEEN=(205, 160, 105), EDGE=(150, 104, 60), EDGE_LIGHT=(196, 150, 98),
                  HAMON=(170, 124, 76), HAMON_LIGHT=(180, 134, 86), HAMON_DARK=(150, 104, 60), SHINOGI=(140, 96, 54),
                  GOLD=(110, 70, 38), GOLD_LIGHT=(150, 104, 60), GOLD_DARK=(70, 42, 22), IVORY=(170, 128, 84),
                  SILK_DARK=(50, 32, 18), SILK_LIGHT=(96, 64, 38), LACQ=(126, 84, 48), LACQ_SHEEN=(196, 152, 100),
                  SAGEO=(96, 64, 38), SAGEO_DARK=(60, 38, 22), SUN_BLACK=(60, 36, 18)),
         training=True, family="ArtsMartiaux",                              # Empire Luo : page Arts Martiaux
         mechanics=(" Katana du Clan Luo, réservé à ses membres : tenu au fourreau dans la main gauche, dégainé au premier coup, "
                    "il reste au clair tant que l'on frappe. Clic droit : bref, rengainer ; maintenu, garde. Maintenir l'attaque "
                    "(ou garde levée puis clic gauche) : Percée, un estoc qui grandit avec votre classe (ruée, aiguille de Ki, mille "
                    "aiguilles, fleurs de prunier, puis le canon de Ki du Maître Suprême et, pour le Dieu Martial, cinq lances de Ki qui "
                    "naissent derrière vous et partent une à une). Elle coûte de l'endurance et du mana, d'autant plus que la "
                    "classe est haute. Compétence (énergie de signature pleine) : rafale de tout le combo en un éclair (une lame de Ki par mouvement dès le Saint "
                    "Martial, Ki de l'Épée déployé). Chaque coup coûte 5 % d'endurance, la rafale la moitié."),
         desc="Le sabre de bois des disciples du dojo Luo, rangé dans un fourreau de bois clair : on y apprend le dégainé "
              "avant de toucher une lame. Sabre d'entraînement : il ne peut tuer ni joueur ni PNJ (il laisse toujours 1 PV). "
              "Son combo s'allonge avec votre classe de la voie martiale (2 coups hors de la voie, puis 3 à 8)."),
    dict(id="Eldanior_Weapon_Sword_Katana_Fer", name="Katana de fer du Clan Luo", rarity="Commune", level=18, mult=1.0, durability=260,
         family="Civils",
         pal=dict(BLACK=(70, 72, 80), BLACK_SHEEN=(190, 194, 204), EDGE=(200, 204, 212), EDGE_LIGHT=(240, 242, 246),
                  HAMON=(226, 230, 236), HAMON_LIGHT=(250, 250, 252), HAMON_DARK=(150, 154, 162), SHINOGI=(220, 222, 228),
                  GOLD=(120, 120, 128), GOLD_LIGHT=(176, 176, 184), GOLD_DARK=(70, 70, 76), IVORY=(200, 196, 186),
                  SILK_DARK=(14, 14, 18), SILK_LIGHT=(50, 48, 56), LACQ=(14, 12, 18), LACQ_SHEEN=(70, 64, 84),
                  SAGEO=(110, 20, 30), SAGEO_DARK=(60, 10, 18), SUN_BLACK=(30, 30, 34)),
         desc="Le katana des recrues du Clan Luo : acier clair, garde de fer au mon du clan, fourreau de laque noire."),
    dict(id="Eldanior_Weapon_Sword_Katana_Aonami", name="Aonami, la Vague Bleue", rarity="Rare", level=85, mult=1.4, durability=350,
         family="Civils",
         pal=dict(BLACK=(60, 70, 90), BLACK_SHEEN=(190, 206, 226), EDGE=(150, 200, 250), EDGE_LIGHT=(225, 240, 255),
                  HAMON=(120, 190, 255), HAMON_LIGHT=(200, 232, 255), HAMON_DARK=(40, 90, 170), SHINOGI=(210, 222, 240),
                  GOLD=(176, 184, 196), GOLD_LIGHT=(236, 240, 246), GOLD_DARK=(110, 118, 132), IVORY=(220, 226, 232),
                  SILK_DARK=(10, 22, 58), SILK_LIGHT=(40, 78, 150), LACQ=(12, 22, 52), LACQ_SHEEN=(60, 90, 150),
                  SAGEO=(40, 90, 190), SAGEO_DARK=(16, 40, 100), SUN_BLACK=(20, 40, 90)),
         blade=((20, 60, 160), (220, 240, 255)), trail=("#9fd4ff", "#1c4cbf"),
         desc="Sa ligne de trempe ondule comme une vague sous la lune ; garde d'argent, fourreau de laque bleu nuit."),
    dict(id="Eldanior_Weapon_Sword_Katana_Tsukikage", name="Tsukikage, Ombre de Lune", rarity="Epique", level=190, mult=2.0, durability=450,
         pal=dict(BLACK=(60, 52, 80), BLACK_SHEEN=(210, 200, 232), EDGE=(196, 150, 255), EDGE_LIGHT=(236, 220, 255),
                  HAMON=(196, 150, 255), HAMON_LIGHT=(240, 224, 255), HAMON_DARK=(110, 60, 170), SHINOGI=(220, 210, 240),
                  GOLD=(170, 160, 196), GOLD_LIGHT=(230, 224, 246), GOLD_DARK=(96, 86, 130), IVORY=(226, 220, 236),
                  SILK_DARK=(30, 10, 48), SILK_LIGHT=(96, 48, 140), LACQ=(22, 12, 34), LACQ_SHEEN=(80, 56, 120),
                  SAGEO=(120, 60, 190), SAGEO_DARK=(60, 24, 100), SUN_BLACK=(40, 20, 60)),
         blade=((70, 20, 130), (240, 225, 255)), trail=("#e0c8ff", "#5a1fa0"), next="Eldanior_OnHit_Givre", family="Kage",
         desc="Forgée sous une éclipse de lune ; chaque coup glace la cible (−40 % de vitesse pendant 2,5 s)."),
    dict(id="Artefact_Sword_Katana_Shirasagi", name="Shirasagi, l'Aigrette Blanche", rarity="Unique", level=380, mult=2.8,
         durability=600,
         pal=dict(BLACK=(150, 156, 168), BLACK_SHEEN=(245, 247, 252), EDGE=(236, 242, 252), EDGE_LIGHT=(255, 255, 255),
                  HAMON=(255, 255, 255), HAMON_LIGHT=(255, 255, 255), HAMON_DARK=(196, 206, 222), SHINOGI=(232, 236, 244),
                  GOLD=(200, 206, 216), GOLD_LIGHT=(246, 248, 252), GOLD_DARK=(140, 148, 160), IVORY=(246, 244, 238),
                  SILK_DARK=(206, 208, 214), SILK_LIGHT=(252, 252, 254), LACQ=(226, 228, 234), LACQ_SHEEN=(255, 255, 255),
                  SAGEO=(196, 202, 214), SAGEO_DARK=(140, 148, 162), SUN_BLACK=(180, 186, 198)),
         blade=((170, 186, 210), (255, 255, 255)), trail=("#ffffff", "#b8c8e8"), next_down="Eldanior_OnHit_Eblouir", family="Setsuka",
         desc="Artefact unique. Une lame blanche comme l'aigrette des rizières, gardée d'argent dans un fourreau de laque "
              "blanche ; le dernier coup du combo éblouit (ralenti, 2 s)."),
    dict(id="Artefact_Sword_Katana_Hien", name="Hien, l'Hirondelle Écarlate", rarity="Unique", level=360, mult=2.8, durability=600,
         pal=dict(BLACK=(26, 20, 24), BLACK_SHEEN=(110, 96, 104), EDGE=(220, 90, 40), EDGE_LIGHT=(255, 190, 120),
                  HAMON=(255, 120, 60), HAMON_LIGHT=(255, 210, 140), HAMON_DARK=(150, 30, 20), SHINOGI=(140, 120, 128),
                  GOLD=GOLD3[0], GOLD_LIGHT=GOLD3[1], GOLD_DARK=GOLD3[2], IVORY=(238, 230, 210),
                  SILK_DARK=(80, 8, 20), SILK_LIGHT=(190, 30, 48), LACQ=(110, 10, 22), LACQ_SHEEN=(200, 60, 70),
                  SAGEO=(232, 188, 92), SAGEO_DARK=(160, 116, 50), SUN_BLACK=(8, 7, 10)),
         blade=((150, 20, 10), (255, 220, 150)), trail=("#ffd08a", "#c41c1c"), next="Eldanior_OnHit_Embrasement", fast=True,
         family="Hanazakura",
         desc="Artefact unique. Plus rapide qu'une hirondelle, sa lame noire brûle d'un fil écarlate ; chaque coup embrase la "
              "cible (6 dégâts de feu par seconde pendant 3 s). Fourreau de laque cramoisie à la garde d'or."),
    # Katanas uniques des clans (2026-11-10) : Hanazakura = Hien, Setsuka = Shirasagi, Sama = Gotetsu, Kage = Yamikiri
    dict(id="Artefact_Sword_Katana_Gotetsu", name="Gōtetsu, l'Acier Cramoisi", rarity="Unique", level=370, mult=3.0,
         durability=650, family="Sama",
         pal=dict(BLACK=(46, 40, 42), BLACK_SHEEN=(150, 132, 136), EDGE=(196, 176, 172), EDGE_LIGHT=(244, 226, 220),
                  HAMON=(230, 120, 110), HAMON_LIGHT=(255, 210, 196), HAMON_DARK=(132, 30, 30), SHINOGI=(160, 140, 140),
                  GOLD=GOLD3[0], GOLD_LIGHT=GOLD3[1], GOLD_DARK=GOLD3[2], IVORY=(238, 230, 210),
                  SILK_DARK=(70, 10, 18), SILK_LIGHT=(150, 30, 40), LACQ=(54, 52, 60), LACQ_SHEEN=(132, 128, 142),
                  SAGEO=(150, 22, 34), SAGEO_DARK=(84, 10, 22), SUN_BLACK=(8, 7, 10)),
         blade=((120, 10, 10), (255, 214, 196)), trail=("#ffb6a0", "#a01818"),
         desc="Artefact unique du Clan Sama. Une lame épaisse d'acier sombre au fil cramoisi, forgée pour Sama Gōtetsu, qui "
              "préférait ses poings mais respectait l'acier ; ses coups sont les plus lourds des katanas du clan. Fourreau de "
              "laque d'acier sombre au cordon cramoisi, garde d'or."),
    dict(id="Artefact_Sword_Katana_Yamikiri", name="Yamikiri, Tranche-Ténèbres", rarity="Unique", level=380, mult=2.8,
         durability=600, family="Kage",
         pal=dict(BLACK=(12, 10, 16), BLACK_SHEEN=(70, 60, 90), EDGE=(90, 80, 110), EDGE_LIGHT=(170, 150, 210),
                  HAMON=(130, 90, 200), HAMON_LIGHT=(200, 170, 255), HAMON_DARK=(40, 20, 70), SHINOGI=(50, 44, 62),
                  GOLD=(150, 156, 170), GOLD_LIGHT=(220, 224, 236), GOLD_DARK=(80, 84, 96), IVORY=(200, 196, 210),
                  SILK_DARK=(8, 8, 12), SILK_LIGHT=(40, 36, 52), LACQ=(10, 9, 14), LACQ_SHEEN=(56, 48, 74),
                  SAGEO=(60, 30, 100), SAGEO_DARK=(30, 14, 50), SUN_BLACK=(4, 4, 6)),
         blade=((30, 10, 60), (210, 190, 255)), trail=("#c8a8ff", "#2a1050"), next="Eldanior_OnHit_Venin",
         desc="Artefact unique du Clan Kage. Une lame noire qui ne renvoie aucune lumière, au fil violet comme une veine "
              "d'ombre ; chaque coup empoisonne la cible. Fourreau de laque noire à la garde d'argent bruni."),
    dict(id="Artefact_Sword_Kinryu", name="Kinryū-Ō, Lame du Dieu-Dragon d'Or", rarity="Divin", level=800, mult=5.0,
         durability=1200, horimono=True, scales=True,
         pal=dict(BLACK=(150, 118, 48), BLACK_SHEEN=(250, 226, 150), EDGE=(244, 236, 214), EDGE_LIGHT=(255, 252, 240),
                  HAMON=(255, 246, 220), HAMON_LIGHT=(255, 255, 250), HAMON_DARK=(214, 176, 92), SHINOGI=(236, 222, 180),
                  GOLD=(232, 188, 92), GOLD_LIGHT=(255, 236, 160), GOLD_DARK=(150, 104, 36), IVORY=(250, 244, 226),
                  SILK_DARK=(14, 12, 16), SILK_LIGHT=(86, 66, 26), LACQ=(176, 128, 40), LACQ_SHEEN=(255, 224, 130),
                  SAGEO=(20, 16, 18), SAGEO_DARK=(8, 6, 8), SUN_BLACK=(8, 7, 10)),
         blade=((160, 110, 20), (255, 248, 214)), trail=("#fff1b8", "#c8901c"), family_kit=True,
         mechanics=(" Trésor du Clan Luo, avec la Tenue Ancestrale du Dieu-Dragon d'Or : remis au Patriarche seul, et repris à "
                    "la succession. Tenu au fourreau dans la main gauche, dégainé au premier coup, il reste au clair tant que l'on "
                    "frappe. Clic droit : bref, rengainer ; maintenu, garde. Maintenir l'attaque : Souffle du Dragon d'Or, un dragon "
                    "de lumière serpente devant le porteur et transperce tout sur son passage. Signature, Éclipse du Dieu-Dragon : "
                    "le ciel s'assombrit sur la zone visée, puis un dragon d'or en descend et la frappe tout entière."),
         desc="Arme divine. Forgée d'une écaille du Dieu-Dragon d'Or, celui qui avala le soleil : lame d'or blanc gravée du "
              "dragon, garde au dragon enroulé autour du soleil noir, fourreau d'écailles d'or."),
]
DEFAULTS = {k: getattr(G, k) for k in KATANAS[0]["pal"]}
# 2026-11-12 : tous les katanas sauf le Divin et le Legendaire prennent le fonctionnement complet du Bokken (bokken_kit).
# 2026-11-11 : tous les katanas (sauf ceux de famille, `family_kit`) prennent le systeme du Bokken : kata selon la classe,
# rafale au maintien, Percee (garde + clic), Fente du Ciel en signature ; les specialites de chaque katana viendront plus tard.
MECHANICS = (" Katana du Clan Luo, réservé à ses membres : tenu au fourreau dans la main gauche, dégainé au premier coup, il "
             "reste au clair tant que l'on frappe. Son kata s'allonge avec votre classe de la voie martiale. Clic droit : bref, "
             "rengainer ; maintenu, garde. Maintenir l'attaque (ou garde levée puis clic gauche) : Percée, un estoc qui grandit "
             "avec votre classe (ruée, aiguille de Ki, mille aiguilles, fleurs de prunier, canon de Ki du Maître Suprême, cinq "
             "lances de Ki du Dieu Martial), payée en endurance et en mana. Compétence (énergie de signature pleine) : rafale de tout le kata en un éclair (une "
             "lame de Ki par mouvement dès le Saint Martial, Ki de l'Épée déployé). Chaque coup coûte 5 % d'endurance, la rafale "
             "la moitié.")


def bokken_kit(k):
    """Fonctionnement du Bokken (maintien = Percee avec posture, competence = rafale, garde + clic = Percee) : le Bokken, puis
    tous les katanas sauf le Divin et le Legendaire (decision de l'utilisateur, 2026-11-12) ; Kinryu-O garde son kit de famille."""
    return not k.get("family_kit") and k["rarity"] not in ("Divin", "Legendaire")


def short(k):
    return k["id"].rsplit("_", 1)[1]


# ---------------------------------------------------------------------------------------------------------------------
# Modeles
# ---------------------------------------------------------------------------------------------------------------------
def build_models(k):
    for name, value in DEFAULTS.items():
        setattr(G, name, k["pal"].get(name, value))
    G.HORIMONO = bool(k.get("horimono"))
    G.SAYA_SCALES = bool(k.get("scales"))
    G.M = K.Model(k["id"])
    G.M.reuse = True
    root, handle = G.build()
    G.fix_coplanar(handle, short(k))
    saya = G.build_saya(handle, A.SAYA_POS, A.SAYA_Q)
    hilt = [n for n in G.shape_names(saya) if not n.startswith(("Saya-Seg", "Saya-Kojiri", "Saya-Koiguchi", "Saya-Bague",
                                                                  "Saya-Kurikata", "Saya-Sageo"))]
    W, H, tex = G.M.texture()
    d = os.path.join(RES, "Common", MODEL_REL)
    os.makedirs(d, exist_ok=True)
    png.write(os.path.join(d, k["id"] + ".png"), W, H, tex)
    json.dump({"nodes": [root], "lod": "auto"}, open(os.path.join(d, k["id"] + ".blockymodel"), "w"), indent=1)
    json.dump({"nodes": [saya], "lod": "auto"}, open(os.path.join(d, k["id"] + "_Fourreau.blockymodel"), "w"), indent=1)
    empty = copy.deepcopy(saya)

    def hide(n):
        if n["name"] in hilt:
            n["shape"]["visible"] = False
        for c in n["children"]:
            hide(c)
    hide(empty)
    json.dump({"nodes": [empty], "lod": "auto"}, open(os.path.join(d, k["id"] + "_Fourreau_Vide.blockymodel"), "w"), indent=1)
    json.dump({"nodes": [G.display_model(root, saya)], "lod": "auto"},
              open(os.path.join(d, k["id"] + "_Presentoir.blockymodel"), "w"), indent=1)
    icon, sheath_icon = G.make_icons(handle, saya, tex)
    icons = os.path.join(RES, "Common", "Icons", "ItemsGenerated")
    png.write(os.path.join(icons, k["id"] + ".png"), 64, 64, icon)
    png.write(os.path.join(icons, k["id"] + "_Fourreau.png"), 64, 64, sheath_icon)
    return icon


def write_blade(k):
    """Lame d'air de la couleur du katana (iai et Iaigiri) : la Windstrike vanilla teintee, horizontale."""
    if not k.get("blade"):
        return
    import tempfile
    with tempfile.NamedTemporaryFile(suffix=".png", delete=False) as f:
        f.write(Z.read("Common/Items/Projectiles/WindStrike_Projectile_Texture.png"))
    w, h, px = png.read(f.name)
    os.unlink(f.name)
    lo, hi = k["blade"]
    for row in px:
        for c in row:
            l = ((0.3 * c[0] + 0.59 * c[1] + 0.11 * c[2]) / 255) ** 3.0
            c[0], c[1], c[2] = [int(lo[i] + (hi[i] - lo[i]) * l) for i in range(3)]
    out = os.path.join(RES, "Common", "Items", "Projectiles")
    tex = G.AIR + "_" + short(k) + "_H"                                  # une texture partagee par les 4 inclinaisons
    png.write(os.path.join(out, tex + ".png"), w, h, px)
    base = json.loads(Z.read("Common/Items/Projectiles/WindStrike_Projectile.blockymodel"))
    trail = "Katana_%s_Trail" % short(k)
    neutral = {"Interactions": [{"Type": "Simple", "RunTime": 0.01}]}
    # H horizontale (iai, Iaigiri) ; G, D, V : inclinaisons des coups du kata (lames de Ki orientees comme le coup qui les
    # lance, retour du 2026-11-10), memes roulis que les lames d'or de Tenryumaru (G = coup vers la gauche, D = vers la droite)
    for orient, roll in G.AIR_ROLLS.items():
        name = G.AIR + "_" + short(k) + "_" + orient
        if roll == 0:
            model, node_name = base, base["nodes"][0]["name"]
        else:
            ids = iter(range(1000, 2000))

            def renum(n):
                n["id"] = str(next(ids))
                for c in n.get("children", []):
                    renum(c)
            children = copy.deepcopy(base["nodes"][0]["children"])
            for c in children:
                renum(c)
            model = copy.deepcopy(base)
            model["nodes"][0]["children"] = [{"id": str(next(ids)), "name": "Lame", "position": {"x": 0, "y": 0, "z": 0},
                                              "orientation": K.round_q(K.q_axis((0, 0, 1), roll)), "shape": G.empty(False),
                                              "children": children}]
            node_name = "Lame"
        json.dump(model, open(os.path.join(out, name + ".blockymodel"), "w"), indent=1)
        asset = {"$Comment": "Lame d'air du katana Luo %s (iai, Iaigiri, lames de Ki du kata), deplacee par le code. "
                             "Genere par gen_luo_katanas.py." % k["name"],
                 "Model": "Items/Projectiles/" + name + ".blockymodel", "Texture": "Items/Projectiles/" + tex + ".png",
                 "MinScale": 1.0, "MaxScale": 1.0,
                 "HitBox": {"Max": {"X": 1.2, "Y": 0.6, "Z": 0.6}, "Min": {"X": -1.2, "Y": -0.6, "Z": -0.6}},
                 "Trails": [{"TrailId": trail, "TargetNodeName": node_name, "PositionOffset": {"X": x, "Z": 0.5}} for x in (1.0, -1.0)],
                 "Light": {"Color": k["trail"][0], "Radius": 5}}
        json.dump(asset, open(os.path.join(RES, "Server", "Models", "Projectiles", "Abilities", name + ".json"), "w"), indent=2,
                  ensure_ascii=False)
        config = {"$Comment": "Lame d'air d'un katana Luo, deplacee par le code. Interactions neutres obligatoires.",
                  "Model": name, "UseModelScale": True,
                  "Physics": {"Type": "Standard", "Gravity": 0, "TerminalVelocityAir": 1, "TerminalVelocityWater": 1,
                              "Bounciness": 0, "SticksVertically": False},
                  "LaunchForce": 0, "SpawnOffset": {"X": 0, "Y": 0, "Z": 0},
                  "Interactions": {"ProjectileHit": neutral, "ProjectileMiss": neutral}}
        json.dump(config, open(os.path.join(RES, "Server", "ProjectileConfigs", "Abilities", "Projectile_Config_" + name + ".json"), "w"),
                  indent=2, ensure_ascii=False)


def write_trail(k):
    if not k.get("trail"):
        return
    t = {"TexturePath": "Trails/Charged_Red.png", "LifeSpan": 6, "Roll": 0,
         "Start": {"Color": "rgba(%s, 1)" % k["trail"][0], "Width": 0.3},
         "End": {"Color": "rgba(%s, 0.35)" % k["trail"][1], "Width": 0},
         "LightInfluence": 0, "Smooth": True, "RenderMode": "BlendLinear"}
    json.dump(t, open(os.path.join(RES, "Server", "Entity", "Trails", "Katana_%s_Trail.json" % short(k)), "w"), indent=2)


# ---------------------------------------------------------------------------------------------------------------------
# Objets et interactions
# ---------------------------------------------------------------------------------------------------------------------
ENERGY = {"Swing_Left_Damage": 2, "Swing_Right_Damage": 2, "Swing_Down_Damage": 4}


def variables(k, iron):
    out = {}
    for var in ("Swing_Left_Damage", "Swing_Right_Damage", "Swing_Down_Damage"):
        base = iron["InteractionVars"][var]["Interactions"][0]
        dmg = {t: max(1, round(v * k["mult"])) for t, v in base["DamageCalculator"]["BaseDamage"].items()}
        entry = {"Parent": base["Parent"], "DamageCalculator": {"BaseDamage": dmg},
                 "DamageEffects": {"WorldSoundEventId": "SFX_Sword_T2_Impact", "LocalSoundEventId": "SFX_Sword_T2_Impact"},
                 "EntityStatsOnHit": [{"EntityStatId": "SignatureEnergy", "Amount": ENERGY[var]}]}
        if k.get("next"):
            entry["Next"] = k["next"]
        if var == "Swing_Down_Damage" and k.get("next_down"):            # 4e coup du combo (karatake-wari)
            entry["Next"] = k["next_down"]
        out[var] = {"Interactions": [entry]}
    guard = iron["InteractionVars"]["Guard_Wield"]["Interactions"][0]
    cost = dict(guard["StaminaCost"])
    if k["id"].endswith("_Fer"):                                         # katana de fer : garde 30 % moins couteuse
        cost["Value"] = round(cost["Value"] * 0.7, 2)
    out["Guard_Wield"] = {"Interactions": [{"Parent": guard["Parent"], "StaminaCost": cost}]}
    out["Thrust"] = {"Interactions": ["Luo_Iai_Degainee"]}
    if k.get("fast"):                                                    # Hien : combo plus rapide
        out["Luo_Combo"] = {"Interactions": ["Luo_Combo_Rapide"]}
    if k.get("training"):                                                # Bokken : combo selon la voie martiale
        out["Luo_Combo"] = {"Interactions": ["Luo_Combo_Bokken"]}
    if not k.get("family_kit"):                                          # systeme du Bokken : rafale, Percee (2026-11-11)
        out["Thrust"] = {"Interactions": ["Luo_Bokken_Charge"]}
        out["Thrust_Fourreau"] = {"Interactions": ["Luo_Bokken_Charge_Fourreau"]}
        out["Guard_Bash"] = {"Interactions": ["Luo_Percee"]}             # garde + clic (embranchement de la garde vanilla)
    if bokken_kit(k):                                                    # Bokken (2026-11-12) : competence = rafale,
        out["Thrust"] = {"Interactions": ["Luo_Bokken_Percee"]}          # maintien = Percee, plus de Fente du Ciel ; etendu le
        out["Thrust_Fourreau"] = {"Interactions": ["Luo_Bokken_Percee_Fourreau"]}   # meme jour a tous les katanas (bokken_kit)
    return out


def write_items(k, iron):
    vars_ = variables(k, iron)
    base = {"Parent": "Template_Weapon_Sword",
            "TranslationProperties": {"Name": k["name"], "Description": k["desc"] + k.get("mechanics", MECHANICS)},
            "Quality": QUALITY[k["rarity"]], "ItemLevel": k["level"],
            "Texture": MODEL_REL + "/" + k["id"] + ".png", "Icon": "Icons/ItemsGenerated/" + k["id"] + ".png",
            "IconProperties": {"Scale": 0.33, "Rotation": [45, 90, 0], "Translation": [-33, -38]},
            "MaxStack": 1, "Variant": False, "InteractionVars": vars_, "MaxDurability": k["durability"]}
    sheathed = dict(copy.deepcopy(base), **{
        "$Comment": "Katana du Clan Luo au fourreau (rien en main droite). Genere par scripts/artefacts/gen_luo_katanas.py.",
        "Model": EMPTY_MAIN, "PlayerAnimationsId": "Tenryumaru_Fourreau",
        "Categories": [familles.category(k.get("family", "Luo")), "Items.Weapons"],
        "Interactions": {"Primary": "Root_Luo_Fourreau_Primary", "Secondary": "Root_Weapon_Sword_Secondary_Guard",
                         "Ability1": "Root_Luo_Signature_Fourreau"}})
    if bokken_kit(k):                                                    # Bokken : la competence lance la rafale
        sheathed["Interactions"]["Ability1"] = "Root_Luo_Bokken_Rafale_Fourreau"
    if "Guard_Bash" in vars_:                                            # au fourreau : degainer avant la Percee
        sheathed["InteractionVars"]["Guard_Bash"] = {"Interactions": ["Luo_Percee_Fourreau"]}
    drawn = dict(copy.deepcopy(base), **{
        "$Comment": "Katana du Clan Luo au clair : objet de passage, hors menu creatif. Genere par scripts/artefacts/gen_luo_katanas.py.",
        "Model": MODEL_REL + "/" + k["id"] + ".blockymodel",
        "PlayerAnimationsId": "Tenryumaru_Degainee_Rapide" if k.get("fast") else "Tenryumaru_Degainee",
        "Interactions": {"Primary": "Root_Luo_Primary", "Secondary": "Root_Tenryumaru_Secondary_Degainee",
                         "Ability1": "Root_Luo_Signature"}})
    if bokken_kit(k):
        drawn["Interactions"]["Ability1"] = "Root_Luo_Bokken_Rafale"
    if k.get("trail"):
        drawn["Trails"] = [{"TrailId": "Katana_%s_Trail" % short(k), "TargetNodeName": "Handle", "TargetEntityPart": "PrimaryItem",
                            "PositionOffset": {"X": 0.9, "Y": 0.05}, "RotationOffset": {"Roll": 90}}]
    folder = os.path.join(RES, "Server", "Item", "Items", "Weapon", "Sword")
    for name, data in ((k["id"], sheathed), (k["id"] + "_Degainee", drawn)):
        json.dump(data, open(os.path.join(folder, name + ".json"), "w"), indent=2, ensure_ascii=False)
    for suffix, full in (("_Fourreau", True), ("_Fourreau_Vide", False)):
        sheath = {"$Comment": "Fourreau de " + k["name"] + ", objet utilitaire pose dans la main gauche par TenryumaruSystem. "
                              "Genere par scripts/artefacts/gen_luo_katanas.py.",
                  "TranslationProperties": {"Name": "Fourreau — " + k["name"],
                                            "Description": "Il accompagne la lame et disparaît sans elle."},
                  "Quality": QUALITY[k["rarity"]], "ItemLevel": k["level"],
                  "Model": MODEL_REL + "/" + k["id"] + suffix + ".blockymodel", "Texture": MODEL_REL + "/" + k["id"] + ".png",
                  "Icon": "Icons/ItemsGenerated/" + k["id"] + "_Fourreau.png",
                  "IconProperties": {"Scale": 0.33, "Rotation": [45, 90, 0], "Translation": [-33, -38]},
                  "MaxStack": 1, "Variant": False, "Utility": {"Usable": True}, "DropOnDeath": False,
                  "PlayerAnimationsId": "Tenryumaru_Fourreau" if full else "Tenryumaru_Degainee",
                  "Interactions": {"Secondary": "Root_Weapon_Sword_Secondary_Guard" if full else "Root_Tenryumaru_Secondary_Degainee"},
                  "InteractionVars": {"Guard_Wield": copy.deepcopy(vars_["Guard_Wield"])}}
        json.dump(sheath, open(os.path.join(folder, k["id"] + suffix + ".json"), "w"), indent=2, ensure_ascii=False)


def write_interactions():
    d = os.path.join(RES, "Server", "Item", "Interactions", "Weapons", "Luo")
    rd = os.path.join(RES, "Server", "Item", "RootInteractions", "Weapons", "Luo")
    os.makedirs(d, exist_ok=True)
    os.makedirs(rd, exist_ok=True)
    iai_charge = {"ItemAnimationId": "IaiCharging", "LocalSoundEventId": "SFX_Sword_T2_Lunge_Charge_Local"}

    def release(from_sheath):
        seq = [{"Type": "Simple", "RunTime": 0.08,
                "Effects": {"ItemAnimationId": "IaiCharged", "WorldSoundEventId": "SFX_Ability_Wind_Strike_Slash",
                            "LocalSoundEventId": "SFX_Sword_T2_Lunge_Local", "CameraEffect": "Sword_Stab"}}]
        if from_sheath:
            seq.append(G.SHEATHE_SWAP)
        seq += [G.SELECTOR_RIGHT, G.spell("KiEpeeCharge", 0.0), G.spell("LuoIai", 0.05),
                {"Type": "Simple", "RunTime": 0.3, "Effects": {"ClearAnimationOnFinish": True}}]
        return [seq[0]] + [G.ignoring(x) for x in seq[1:]]

    files = {
        "Luo_Fourreau_Primary": {
            "$Comment": "Katana Luo au fourreau : clic = degaine-coupe (Tenryumaru_Degainer), maintien = posture iai.",
            "Type": "Charging", "Rules": {"InterruptedBy": ["Secondary"]}, "DisplayProgress": False,
            "Next": {"0": "Tenryumaru_Degainer",
                     "0.174": {"Type": "Replace", "Var": "Thrust_Fourreau", "DefaultOk": True,
                               "DefaultValue": {"Interactions": ["Luo_Iai_Fourreau"]}}}},
        "Luo_Iai_Fourreau": {
            "$Comment": "Posture iai, lame au fourreau : relache avant la fin = degaine simple, charge pleine = degaine foudroyant (LuoIai).",
            "Type": "Charging", "Rules": {"InterruptedBy": ["Secondary"]}, "DisplayProgress": True, "HorizontalSpeedMultiplier": 0.45,
            "Effects": dict(iai_charge), "Next": {"0": "Tenryumaru_Degainer", "0.75": "Luo_Iai_Release_Fourreau"}},
        "Luo_Iai_Release_Fourreau": {"$Comment": "Degaine foudroyant depuis le fourreau.", "Type": "Serial", "Interactions": release(True)},
        "Luo_Iai_Degainee": {
            "$Comment": "Maintien, lame au clair (variable Thrust) : posture iai ; relache avant la fin = combo, charge pleine = LuoIai.",
            "Type": "Charging", "Rules": {"InterruptedBy": ["Secondary"]}, "DisplayProgress": True, "HorizontalSpeedMultiplier": 0.45,
            "Effects": dict(iai_charge), "Next": {"0": G.LUO_COMBO, "0.75": "Luo_Iai_Release"}},
        "Luo_Iai_Release": {"$Comment": "Degaine foudroyant, lame deja au clair.", "Type": "Serial", "Interactions": release(False)},
    }

    def signature(from_sheath):
        seq = []
        if from_sheath:
            seq += [G.step("Degainer", 0.05), G.SHEATHE_SWAP, G.ignoring({"Type": "Simple", "RunTime": 0.1})]
        seq += [G.ignoring(G.spell("LuoIaigiri", 0.05)),
                G.ignoring({"Type": "ChangeStat", "StatModifiers": {"SignatureEnergy": -100}, "ValueType": "Percent"})]
        return {"$Comment": "Signature d'un katana Luo, Iaigiri (energie pleine) : sort Java LuoIaigiri.",
                "Type": "StatsCondition", "Costs": {"SignatureEnergy": 100}, "ValueType": "Percent",
                "Next": {"Type": "Serial", "Interactions": seq}}
    # ----- combo Luo : 4 coups (yoko-giri, kesa-giri, kiri-age, karatake-wari avec un pas), version rapide pour Hien -----
    def coup(n, run, selector, factor):
        return {"Type": "Simple", "RunTime": round(run * factor, 3), "HorizontalSpeedMultiplier": 0.75,
                "Effects": G.sound({"ItemAnimationId": "LuoCoup%d" % n}),
                "Next": {"Type": "Replace", "Var": selector, "DefaultOk": True,
                         "DefaultValue": {"Interactions": ["Weapon_Sword_Primary_" + selector]}}}

    files.update(kata_interactions(coup))
    files["Luo_Primary"] = {
        "$Comment": "Attaque d'un katana Luo au clair : clic = combo Luo (variable Luo_Combo), maintien = posture iai (variable Thrust).",
        "Type": "Charging", "Rules": {"InterruptedBy": ["Secondary"]}, "DisplayProgress": False,
        "Next": {"0": G.LUO_COMBO,
                 "0.174": {"Type": "Replace", "Var": "Thrust", "DefaultOk": True, "DefaultValue": {"Interactions": ["Luo_Iai_Degainee"]}}}}
    files.update(bokken_interactions(coup, iai_charge))
    files["Luo_Combo_Bokken"] = dict(kata_pick("", 2), **{
        "$Comment": "Combo du Bokken : le kata de la voie martiale (2 coups hors de la voie), comme les autres katanas."})
    percee_end = {"Type": "Simple", "RunTime": 0.35, "Effects": {"ClearAnimationOnFinish": True}}
    files["Luo_Percee"] = {"$Comment": "Percee : garde levee puis clic gauche (variable Guard_Bash), sort Java LuoPercee (8 paliers).",
                           "Type": "Serial", "Interactions": [G.spell("LuoPercee", 0.05), percee_end]}
    files["Luo_Percee_Fourreau"] = {"$Comment": "Percee depuis le fourreau : degaine, echange de forme, puis LuoPercee.", "Type": "Serial",
                                    "Interactions": [G.step("Degainer", 0.05), G.SHEATHE_SWAP, G.ignoring(G.spell("LuoPercee", 0.05)),
                                                     G.ignoring(percee_end)]}
    files["Luo_Signature_Fourreau"] = signature(True)
    files["Luo_Signature"] = signature(False)
    # Bokken (2026-11-12, demandes de l'utilisateur) : la competence lance la rafale du combo ; le maintien est la Percee
    # (posture chargee, relache apres 0,5 s = estoc ; avant = combo / degaine). La Fente du Ciel est retiree du Bokken (elle
    # reste la signature des autres katanas). Garde + clic donne aussi la Percee, comme sur les autres katanas.
    # Posture (2026-11-12, 2e serie) : le sort PerceePosture accroche le Ki du palier a la lame et lance l'eclat de charge pleine ;
    # animation TsukiPosture (percee_anims.py) au clair, posture iai au fourreau.
    for suffix, sheath in (("", False), ("_Fourreau", True)):
        files["Luo_Bokken_Percee" + suffix] = {
            "$Comment": "Bokken, maintien : posture de la Percee (Ki du palier sur la lame) ; relache avant 0,5 s = %s, apres = Percee "
                        "(LuoPercee)." % ("degaine" if sheath else "combo"),
            "Type": "Serial", "Interactions": [
                G.spell("LuoPerceePostureFourreau" if sheath else "LuoPerceePosture", 0.0),
                {"Type": "Charging", "Rules": {"InterruptedBy": ["Secondary"]}, "DisplayProgress": True, "HorizontalSpeedMultiplier": 0.45,
                 "Effects": dict(iai_charge) if sheath else {"ItemAnimationId": "TsukiPosture", "LocalSoundEventId": iai_charge["LocalSoundEventId"]},
                 "Next": {"0": "Tenryumaru_Degainer" if sheath else G.LUO_COMBO, "0.5": "Luo_Percee" + suffix}}]}
    # Competence du Bokken (et des katanas a son fonctionnement) = signature : la rafale demande l'energie de signature pleine
    # et la vide (retour de l'utilisateur, 2026-11-12 : « l'ability s'active sans signature »).
    for suffix in ("", "_Fourreau"):
        files["Luo_Bokken_Rafale_Signature" + suffix] = {
            "$Comment": "Rafale du combo en competence : energie de signature pleine, videe au lancement (gen_luo_katanas.py).",
            "Type": "StatsCondition", "Costs": {"SignatureEnergy": 100}, "ValueType": "Percent",
            "Next": {"Type": "Serial", "Interactions": [
                {"Type": "ChangeStat", "StatModifiers": {"SignatureEnergy": -100}, "ValueType": "Percent"},
                "Luo_Bokken_Rafale" + suffix]}}
    for name, data in files.items():
        json.dump(data, open(os.path.join(d, name + ".json"), "w"), indent=2, ensure_ascii=False)
    roots = {"Root_Luo_Primary": {"RequireNewClick": True, "ClickQueuingTimeout": 0.2, "Cooldown": {"Cooldown": 0.2},
                                  "Interactions": ["Luo_Primary"]},
             "Root_Luo_Fourreau_Primary": {"RequireNewClick": True, "ClickQueuingTimeout": 0.2, "Cooldown": {"Cooldown": 0.25},
                                           "Interactions": ["Luo_Fourreau_Primary"]},
             "Root_Luo_Signature_Fourreau": {"RequireNewClick": True, "Interactions": ["Luo_Signature_Fourreau"]},
             "Root_Luo_Signature": {"RequireNewClick": True, "Interactions": ["Luo_Signature"]},
             "Root_Luo_Bokken_Rafale": {"RequireNewClick": True, "Interactions": ["Luo_Bokken_Rafale_Signature"]},
             "Root_Luo_Bokken_Rafale_Fourreau": {"RequireNewClick": True, "Interactions": ["Luo_Bokken_Rafale_Signature_Fourreau"]}}
    for name, data in roots.items():
        json.dump(data, open(os.path.join(rd, name + ".json"), "w"), indent=2)


# ----- Kata du Dragon d'Or (retour du 2026-11-10) : combo des katanas Luo, de 3 (Apprenti) a 12 coups (Dieu Martial) -----
# Un seul enchainement de 12 mouvements, coherent du premier au dernier ; chaque classe en execute le debut, le dernier coup
# de sa portion sert de final (pas en avant, camera). Hors de la voie martiale : les 4 premiers (l'ancien combo Luo).
#   1 yoko-giri (coupe horizontale)      2 kesa-giri (diagonale)          3 kiri-age (remontante)
#   4 karatake-wari (verticale, un pas)  5 tsuki (estoc)                  6 coupe tournoyante
#   7 coupe sautee en vrille             8 frappe lourde descendante      9 estoc en ruee
#  10 salto avant tranchant              11 coup de pied tornade          12 estoc tournoyant en ruee (final)
# Chaque coup lance aussi KiEpeeCoup (sort Java : lame de Ki si le Ki de l'Epee est actif au Saint et au-dela).
# Retour du 2026-11-10 (nuit) : plus de mouvements circulaires. Le kata garde ses 12 temps mais remplace la frappe lourde et
# l'estoc en ruee par deux tours complets : coupe tournoyante de la lance (LuoKaiten, tour horizontal) et tourbillon de la
# hache de bataille (LuoTourbillon, double tour horizontal).
#   1 yoko-giri  2 kesa-giri  3 kiri-age  4 karatake-wari (pas)  5 coupe circulaire (demi-tour)  6 tsuki (estoc)
#   7 kaiten : tour complet horizontal  8 coupe sautee en vrille  9 tourbillon (double tour)  10 salto avant tranchant
#  11 coup de pied tornade  12 estoc tournoyant en ruee (final)
KATA = [("LuoCoup1", "Swing_Left_Selector", 0.111), ("LuoCoup2", "Swing_Left_Selector", 0.111),
        ("LuoCoup3", "Swing_Right_Selector", 0.111), ("LuoCoup4", "Swing_Down_Selector", 0.2),
        ("SpinRightCharged", "Swing_Left_Selector", 0.25), ("Stab", "Swing_Right_Selector", 0.15),
        ("LuoKaiten", "Swing_Left_Selector", 0.3), ("SwingLeftSpinJumpCharged", "Swing_Left_Selector", 0.3),
        ("LuoTourbillon", "Swing_Right_Selector", 0.35), ("LuoSalto", "Swing_Down_Selector", 0.35),
        ("LuoTornade", "Swing_Left_Selector", 0.3), ("SpinRightStabDashCharged", "Swing_Right_Selector", 0.35)]
# inclinaison de la lame de Ki lancee par chaque coup (KiEpeeCoup_<H|G|D|V>) : horizontale, vers la gauche, vers la droite,
# verticale (les estocs : lame dressee, fine vue de derriere)
KATA_ORIENT = {"LuoCoup1": "H", "LuoCoup2": "G", "LuoCoup3": "G", "LuoCoup4": "V", "Stab": "V", "SpinRightCharged": "H",
               "LuoKaiten": "H", "SwingLeftSpinJumpCharged": "G", "LuoTourbillon": "H", "LuoSalto": "V",
               "LuoTornade": "H", "SpinRightStabDashCharged": "D"}
KATA_DASH = {"LuoCoup4", "SpinRightStabDashCharged", "LuoSalto", "LuoKaiten"}   # ces coups avancent (LuoPas)
# longueur du kata par palier de la voie (LuoPassives.KATA_LENGTH) : Apprenti 3 ... Dieu Martial 12 ; 2 = Bokken hors voie
KATA_LENGTHS = (2, 3, 4, 5, 6, 8, 9, 10, 12)
KATA_EXTRA_ANIMS = A.KATA_EXTRA_ANIMS                                # salto, tornade, tours (tenryumaru_anims.py)


def kata_interactions(coup):
    files = {}
    for suffix, factor in (("", 1.0), ("_Rapide", 0.8)):
        for n in KATA_LENGTHS:
            steps = []
            for i in range(n):
                anim, selector, run = KATA[i]
                final = i == n - 1
                hit = {"Type": "Serial", "Interactions": ([G.spell("LuoPas", 0.0)] if (final or anim in KATA_DASH) else []) + [
                    G.spell("KiEpeeCoup_" + KATA_ORIENT[anim], 0.0),
                    {"Type": "Simple", "RunTime": round(run * factor, 3), "HorizontalSpeedMultiplier": 0.75,
                     "Effects": G.sound(dict({"ItemAnimationId": anim}, **({"CameraEffect": "Sword_Stab"} if final else {})),
                                        *(("SFX_Sword_T2_Swing_Down", "SFX_Sword_T2_Swing_Down_Local") if final else ()))},
                    {"Type": "Replace", "Var": selector, "DefaultOk": True,
                     "DefaultValue": {"Interactions": ["Weapon_Sword_Primary_" + selector]}}]
                    + ([{"Type": "Simple", "RunTime": round(0.12 * factor, 3)}, G.spell("LuoFinCombo", 0.0)] if final else [])}
                steps.append(hit)
            files["Luo_Kata%s_%d" % (suffix, n)] = {
                "$Comment": "Kata du Dragon d'Or, %d coups%s (gen_luo_katanas.py)." % (n, ", version rapide (Hien)" if suffix else ""),
                "Type": "Chaining", "ChainingAllowance": 1.6, "ChainId": "Luo_Kata%s_%d" % (suffix, n), "Next": steps}
        files["Luo_Combo" + suffix] = dict(kata_pick(suffix, 4), **{
            "$Comment": "Combo Luo : le plus long kata dont le joueur porte l'effet Voie_Combo_<N> (LuoPassives.trackTier), "
                        "4 coups hors de la voie martiale." + (" Version rapide (Hien)." if suffix else "")})
    return files


def kata_pick(suffix, fallback):
    """EffectCondition en cascade : Voie_Combo_12, puis 10, 9... jusqu'au kata par defaut."""
    node = "Luo_Kata%s_%d" % (suffix, fallback)
    for n in KATA_LENGTHS:
        if n <= fallback:
            continue
        node = {"Type": "EffectCondition", "EntityEffectIds": ["Voie_Combo_%d" % n], "Match": "All",
                "Next": "Luo_Kata%s_%d" % (suffix, n), "Failed": node}
    return node


# ----- Bokken du Dojo (retour de test du 2026-11-09) -----
# Le combo s'allonge avec le palier de la voie martiale : 2 coups hors de la voie, 3 (Apprenti) ... 8 (Saint et au-dela).
# LuoPassives.trackTier pose l'effet invisible Voie_Combo_<N> ; Luo_Combo_Bokken choisit la chaine par EffectCondition.
# Maintien : posture chargee (iai) puis rafale = tout le combo enchaine tres vite (sort Java LuoBokkenRafale).
BOKKEN_SEQ = [("LuoCoup1", "Swing_Left_Selector"), ("LuoCoup2", "Swing_Left_Selector"), ("LuoCoup3", "Swing_Right_Selector"),
              ("LuoCoup4", "Swing_Down_Selector"), ("Stab", "Swing_Right_Selector"), ("SpinRightCharged", "Swing_Left_Selector"),
              ("SwingRight", "Swing_Right_Selector"), ("SwingDownStrong", "Swing_Down_Selector")]
BOKKEN_MIN, BOKKEN_MAX = 2, 8


def bokken_interactions(coup, iai_charge):
    files = {}

    def finisher(anim, selector):
        return {"Type": "Serial", "Interactions": [
            G.spell("LuoEndurance", 0.0),                                 # 5 % d'endurance par coup (2026-11-11)
            G.spell("LuoPas", 0.0),
            {"Type": "Simple", "RunTime": 0.2, "HorizontalSpeedMultiplier": 0.75,
             "Effects": G.sound({"ItemAnimationId": anim, "CameraEffect": "Sword_Stab"},
                                "SFX_Sword_T2_Swing_Down", "SFX_Sword_T2_Swing_Down_Local")},
            {"Type": "Replace", "Var": selector, "DefaultOk": True,
             "DefaultValue": {"Interactions": ["Weapon_Sword_Primary_" + selector]}},
            {"Type": "Simple", "RunTime": 0.12},
            G.spell("LuoFinCombo", 0.0)]}

    for n in range(BOKKEN_MIN, BOKKEN_MAX + 1):
        steps = []
        for i in range(n):
            anim, selector = BOKKEN_SEQ[i]
            if i == n - 1:
                steps.append(finisher(anim, selector))
            else:
                c = coup(1, 0.111, selector, 1.0)
                c["Effects"]["ItemAnimationId"] = anim
                steps.append({"Type": "Serial", "Interactions": [G.spell("LuoEndurance", 0.0), c]})   # 5 % d'endurance par coup
        files["Luo_Combo_Bokken_%d" % n] = {
            "$Comment": "Combo du Bokken, %d coups (palier de la voie martiale, effet Voie_Combo_%d)." % (n, n),
            "Type": "Chaining", "ChainingAllowance": 1.6, "ChainId": "Luo_Bokken_%d" % n, "Next": steps}

    def pick(n):
        if n == BOKKEN_MIN:
            return "Luo_Combo_Bokken_%d" % n
        return {"Type": "EffectCondition", "EntityEffectIds": ["Voie_Combo_%d" % n], "Match": "All",
                "Next": "Luo_Combo_Bokken_%d" % n, "Failed": pick(n - 1)}
    files["Luo_Combo_Bokken"] = dict(pick(BOKKEN_MAX), **{
        "$Comment": "Combo du Bokken : la chaine la plus longue dont le joueur porte l'effet Voie_Combo_<N> (LuoPassives.trackTier)."})
    rafale_end = {"Type": "Simple", "RunTime": 0.9, "Effects": {"ClearAnimationOnFinish": True}}
    release = {"Type": "Simple", "RunTime": 0.08,
               "Effects": {"ItemAnimationId": "IaiCharged", "WorldSoundEventId": "SFX_Ability_Wind_Strike_Slash",
                           "LocalSoundEventId": "SFX_Sword_T2_Lunge_Local", "CameraEffect": "Sword_Stab"}}
    files["Luo_Bokken_Charge"] = {
        "$Comment": "Bokken, maintien lame au clair : posture chargee ; relache avant 0,5 s = combo, apres = rafale du combo entier.",
        "Type": "Charging", "Rules": {"InterruptedBy": ["Secondary"]}, "DisplayProgress": True, "HorizontalSpeedMultiplier": 0.45,
        "Effects": dict(iai_charge), "Next": {"0": G.LUO_COMBO, "0.5": "Luo_Bokken_Rafale"}}
    files["Luo_Bokken_Rafale"] = {"$Comment": "Rafale du Bokken : tout le combo en un eclair (LuoBokkenRafale).", "Type": "Serial",
                                  "Interactions": [release, G.spell("KiEpeeCharge", 0.0), G.spell("LuoBokkenRafale", 0.05), rafale_end]}
    files["Luo_Bokken_Charge_Fourreau"] = {
        "$Comment": "Bokken au fourreau, maintien : posture chargee ; relache avant 0,5 s = degaine simple, apres = degaine + rafale.",
        "Type": "Charging", "Rules": {"InterruptedBy": ["Secondary"]}, "DisplayProgress": True, "HorizontalSpeedMultiplier": 0.45,
        "Effects": dict(iai_charge), "Next": {"0": "Tenryumaru_Degainer", "0.5": "Luo_Bokken_Rafale_Fourreau"}}
    files["Luo_Bokken_Rafale_Fourreau"] = {
        "$Comment": "Rafale du Bokken depuis le fourreau : degaine, echange de forme, puis LuoBokkenRafale.", "Type": "Serial",
        "Interactions": [release, G.step("Degainer", 0.05), G.SHEATHE_SWAP, G.ignoring(G.spell("KiEpeeCharge", 0.0)), G.ignoring(G.spell("LuoBokkenRafale", 0.05)),
                         G.ignoring(rafale_end)]}
    return files


def write_extras():
    """Effets et interactions des kits (LuoPassives) et jeu d'animations rapide de Hien."""
    effects = os.path.join(RES, "Server", "Entity", "Effects", "Eldanior", "Luo")
    os.makedirs(effects, exist_ok=True)
    json.dump({"$Comment": "Tsukikage : fondu d'ombre au degaine et a l'Eclipse (invisible 0,8 s). Genere par gen_luo_katanas.py.",
               "Duration": 0.8, "ApplicationEffects": {"ModelVFXId": "Elda_Disparition"}, "OverlapBehavior": "Overwrite",
               "Infinite": False, "Debuff": False, "Locale": "Ombre de Lune"},
              open(os.path.join(effects, "Luo_Ombre_Lune.json"), "w"), indent=2, ensure_ascii=False)
    for n in sorted(set(range(BOKKEN_MIN + 1, BOKKEN_MAX + 1)) | set(KATA_LENGTHS[1:])):   # marqueurs invisibles du kata
        json.dump({"$Comment": "Marqueur invisible : combo du Bokken en %d coups (palier de la voie martiale). Pose et rafraichi "
                               "par LuoPassives.trackTier, lu par Luo_Combo_Bokken. Genere par gen_luo_katanas.py." % n,
                   "Duration": 1.5, "ApplicationEffects": {}, "OverlapBehavior": "Overwrite", "Infinite": False, "Debuff": False},
                  open(os.path.join(effects, "Voie_Combo_%d.json" % n), "w"), indent=2, ensure_ascii=False)
    json.dump({"$Comment": "Hien : Elan de l'hirondelle (3 coups enchaines) : deplacement plus rapide. Genere par gen_luo_katanas.py.",
               "Duration": 2.5, "ApplicationEffects": {"HorizontalSpeedMultiplier": 1.15}, "OverlapBehavior": "Overwrite",
               "Infinite": False, "Debuff": False, "Locale": "Élan de l'hirondelle"},
              open(os.path.join(effects, "Luo_Elan_Hirondelle.json"), "w"), indent=2, ensure_ascii=False)
    givre = os.path.join(RES, "Server", "Item", "Interactions")
    src = None
    for dp, _, fs in os.walk(givre):
        if "Eldanior_OnHit_Givre.json" in fs:
            src = os.path.join(dp, "Eldanior_OnHit_Givre.json")
    data = json.load(open(src))
    text = json.dumps(data).replace("Arme_Givre", "Apollo_Aveugle")
    data = json.loads(text)
    data["$Comment"] = "Suite du dernier coup de Shirasagi : interruption vanilla, puis eblouissement (Apollo_Aveugle). Genere par gen_luo_katanas.py."
    json.dump(data, open(os.path.join(os.path.dirname(src), "Eldanior_OnHit_Eblouir.json"), "w"), indent=2, ensure_ascii=False)
    # jeu d'animations de la lame au clair : mouvements du kata et jeu rapide de Hien (tenryumaru_anims.py, 2026-11-12)
    anims = os.path.join(RES, "Server", "Item", "Animations")
    drawn = json.load(open(os.path.join(anims, "Tenryumaru_Degainee.json")))
    A.add_kata_extras(drawn, Z)
    json.dump(drawn, open(os.path.join(anims, "Tenryumaru_Degainee.json"), "w"), indent=2)
    A.write_rapide(drawn, anims)


def main():
    iron = json.loads(Z.read("Server/Item/Items/Weapon/Sword/Weapon_Sword_Iron.json"))
    icons = []
    for k in KATANAS:
        icons.append(build_models(k))
        write_blade(k)
        write_trail(k)
        write_items(k, iron)
        print("%-42s %-8s niv. %3d  x%.1f" % (k["id"], k["rarity"], k["level"], k["mult"]))
    for name, value in DEFAULTS.items():                         # rend les couleurs de Tenryumaru
        setattr(G, name, value)
    G.HORIMONO = True
    G.SAYA_SCALES = False
    write_interactions()
    write_extras()
    if PREVIEW:
        os.makedirs(PREVIEW, exist_ok=True)
        S = 4
        out = [[[40, 40, 48, 255] for _ in range(64 * S * len(icons))] for _ in range(64 * S)]
        for n, ic in enumerate(icons):
            for y in range(64 * S):
                for x in range(64 * S):
                    c = ic[y // S][x // S]
                    if c[3] > 0:
                        k = c[3] / 255.0
                        bg = out[y][n * 64 * S + x]
                        out[y][n * 64 * S + x] = [int(c[i] * k + bg[i] * (1 - k)) for i in range(3)] + [255]
        png.write(os.path.join(PREVIEW, "luo_katanas_icones.png"), 64 * S * len(icons), 64 * S, out)


if __name__ == "__main__":
    main()
