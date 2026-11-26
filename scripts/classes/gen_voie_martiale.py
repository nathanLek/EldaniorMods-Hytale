"""Visuels de la voie martiale du Clan Luo (runes Ki de l'Epee, Meditation martiale, Transformation Hwakyung, Frappe des
Meridiens, Unite du Ciel et de la Terre), valides le 2026-11-09.

Les particules partent de la recette de l'aura de dignite (Server/Particles/Abilities/Aura/Spawners/Aura_T1_*), vue en jeu :
systeme sans TargetEntityPart, accroche au noeud Chest, decalages dans les PositionOffset du .particlesystem.

  Ki_T1..Ki_T8        Ki de l'Epee, une couleur par palier : blanc pale, bleu pale, bleu vif, cyan, violet, or, blanc nacre,
                      puis presque rien (Dieu Martial : le Ki est devenu invisible, quelques etincelles vertes pales)
  Ki_Meditation       volutes douces qui montent pendant la meditation
  Ki_Hwakyung         corps de Ki de la Transformation Hwakyung (blanc nacre, dense)

Effets (Server/Entity/Effects/Eldanior/Ki/), poses et rafraichis par le code (duree courte : ils s'eteignent seuls si le
sort s'arrete sans nettoyage) :
  Ki_Visual_1..8          aura du Ki + vitesse x1,05
  Meditation_Martiale     posture : deplacements bloques, volutes
  Hwakyung_Corps          aura dense de la transformation
  Meridiens_Scelles       ralenti x0,6 (Frappe des Meridiens)
  Unite_Vent              ralenti x0,6 (Unite du Ciel et de la Terre)

Usage : python3 scripts/classes/gen_voie_martiale.py [src/main/resources]
"""
import json, os, sys, copy

HERE = os.path.dirname(os.path.abspath(__file__))
RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "..", "..", "src", "main", "resources")
SRC = os.path.join(RES, "Server", "Particles", "Abilities", "Aura", "Spawners")
DST = os.path.join(RES, "Server", "Particles", "Abilities", "Ki")
FX = os.path.join(RES, "Server", "Entity", "Effects", "Eldanior", "Ki")

# palier -> (couleur, parties de l'aura reprises, multiplicateur de densite)
KI = {
    1: ("#e8eef5", ("Particles", "Sparks"), 0.6),
    2: ("#a8d0ff", ("Particles", "Sparks"), 0.7),
    3: ("#3d7dff", ("Particles", "Sparks", "Lines"), 0.8),
    4: ("#3fe6ff", ("Particles", "Sparks", "Lines"), 0.9),
    5: ("#a050ff", ("Particles", "Sparks", "Lines", "Mist"), 1.0),
    6: ("#ffc83a", ("Particles", "Sparks", "Lines", "Mist"), 1.0),
    7: ("#fff3e6", ("Particles", "Sparks", "Lines", "Mist"), 1.1),
    8: ("#d8ffe0", ("Sparks",), 0.3),
}
EXTRA = {
    "Meditation": ("#bfe3ff", ("Sparks", "Lines"), 0.5),
    "Hwakyung": ("#fff3e6", ("Particles", "Sparks", "Lines", "Mist"), 1.6),
}
OFFSETS = {"Particles": 0.5, "Mist": 0.5, "Sparks": 0.0, "Lines": 1.0}


def hex2rgb(h): return tuple(int(h[i:i + 2], 16) for i in (1, 3, 5))
def rgb2hex(c): return "#%02x%02x%02x" % tuple(max(0, min(255, int(round(v)))) for v in c)


def tint(orig, target):
    """Recolore une couleur grise de l'aura : sa luminance module la couleur cible, les blancs restent lumineux."""
    o, t = hex2rgb(orig), hex2rgb(target)
    lum = (0.299 * o[0] + 0.587 * o[1] + 0.114 * o[2]) / 255.0
    base = [c * min(1.0, lum * 1.3) for c in t]
    w = max(0.0, (lum - 0.85) * 3.0)                      # le coeur blanc des particules garde un peu de blanc
    return rgb2hex([b * (1 - w) + 255 * w for b in base])


def recolor(node, target):
    if isinstance(node, dict):
        return {k: (tint(v, target) if k == "Color" and isinstance(v, str) and v.startswith("#") else recolor(v, target))
                for k, v in node.items()}
    if isinstance(node, list):
        return [recolor(v, target) for v in node]
    return node


def write(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


def system(name, color, parts, density):
    spawners = []
    for part in parts:
        sp = recolor(json.load(open(os.path.join(SRC, "Aura_T1_%s.particlespawner" % part), encoding="utf-8")), color)
        rate = sp.get("SpawnRate")
        if isinstance(rate, dict):
            sp["SpawnRate"] = {k: round(v * density, 2) for k, v in rate.items()}
        sid = "%s_%s" % (name, part)
        write(os.path.join(DST, "Spawners", sid + ".particlespawner"), sp)
        spawners.append({"SpawnerId": sid, "PositionOffset": {"Y": OFFSETS[part]}})
    write(os.path.join(DST, name + ".particlesystem"), {"Spawners": spawners})


ZIP = os.path.expanduser("~/Library/Application Support/Hytale/install/release/package/game/latest/Assets.zip")


def blade_aura(n, color, density=1.0):
    """Lueur de signature d'epee du jeu (Sword_Signature_Status : halo, etincelles, lignes) recoloree au Ki du palier, plus dense
    avec le palier : Ki_Lame_Aura_T<n>."""
    import zipfile
    z = zipfile.ZipFile(ZIP)
    spawners = []
    for part, dx in (("Glow", 0.2), ("Sparkles", -0.2), ("Lines", -0.1)):
        sp = recolor(json.loads(z.read("Server/Particles/Combat/Sword/Signature/Spawners/Status/Sword_Signature_Status_%s.particlespawner" % part)), color)
        rate = sp.get("SpawnRate")
        if isinstance(rate, dict):
            sp["SpawnRate"] = {k: round(v * (0.7 + 0.12 * n) * density, 2) for k, v in rate.items()}
        sid = "Ki_Lame_Aura_T%d_%s" % (n, part)
        write(os.path.join(DST, "Spawners", sid + ".particlespawner"), sp)
        spawners.append({"SpawnerId": sid, "PositionOffset": {"X": dx, "Z": 0}, "FixedRotation": False})
    write(os.path.join(DST, "Ki_Lame_Aura_T%d.particlesystem" % n), {"Spawners": spawners})


def effect(name, particles, scale, duration, extra, comment, locale=None, debuff=False):
    app = {"Particles": [{"SystemId": particles, "TargetNodeName": "Chest", "PositionOffset": {"X": 0, "Y": -1, "Z": 0},
                          "Scale": scale}]} if particles else {}
    app.update(extra)
    data = {"$Comment": comment, "ApplicationEffects": app, "Duration": duration, "OverlapBehavior": "Overwrite",
            "Debuff": debuff}
    # locale : nom lisible garde dans le $Comment (le moteur ignore la cle "Locale" : « Unused key » au chargement)
    if locale: data["$Comment"] += " Nom : " + locale + "."
    write(os.path.join(FX, name + ".json"), data)


def main():
    for n, (color, parts, density) in KI.items():
        system("Ki_T%d" % n, color, parts, density)
        blade_aura(n, "#fff3c8" if n == 8 else color, 0.55 if n == 3 else 1.0)
        # Retours du 2026-11-10 : le Ki de l'Epee ne brille que sur la lame (comme l'attaque signature chargee : particules
        # attachees a l'arme, TargetEntityPart PrimaryItem, noeud Handle). Apprenti, Disciple : rien ; Expert : lueur attenuee ;
        # Maitre : lueur ; Grand Maitre et au-dela : lueur + aura du Renforcement corporel sur la lame.
        on_blade = []
        if n >= 3:
            on_blade.append({"SystemId": "Ki_Lame_Aura_T%d" % n, "TargetEntityPart": "PrimaryItem", "TargetNodeName": "Handle",
                             "PositionOffset": {"X": 0.55, "Y": 0, "Z": 0}, "Scale": round((0.7 if n == 3 else 1.0) + 0.07 * n, 2)})
        if n >= 5:
            on_blade.append({"SystemId": "Ki_Renforcement_T%d" % n, "TargetEntityPart": "PrimaryItem", "TargetNodeName": "Handle",
                             "PositionOffset": {"X": 0.6, "Y": 0, "Z": 0}, "Scale": 0.45})
        write(os.path.join(FX, "Ki_Visual_%d.json" % n), {
            "$Comment": "Ki de l'Epee, palier %d (couleur %s) : %s. Pose et rafraichi chaque seconde par KiEpee "
                        "(gen_voie_martiale.py). Nom : Ki de l'Épée." % (n, color, "rien de visible" if n < 3 else "lueur de Ki sur la lame"),
            "ApplicationEffects": {"Particles": on_blade, "FirstPersonParticles": [dict(x) for x in on_blade], "HorizontalSpeedMultiplier": 1.05},
            "Duration": 2.5, "OverlapBehavior": "Overwrite", "Debuff": False})
    for name, (color, parts, density) in EXTRA.items():
        system("Ki_" + name, color, parts, density)
    effect("Meditation_Martiale", "Ki_Meditation", 1.1, 2.5,
           {"HorizontalSpeedMultiplier": 0, "MovementEffects": {"DisableAll": True},
            "AbilityEffects": {"Disabled": ["Primary", "Secondary"]}},
           "Meditation martiale : posture assise, deplacements, attaque et garde bloques (les runes restent utilisables pour se "
           "relever). Rafraichi par MeditationMartiale (gen_voie_martiale.py).",
           "Méditation")
    # Meditation par palier (retour du 2026-11-10) : l'Apprenti ne montre aucun signe de Ki ; l'aura grandit avec le rang, a la
    # couleur du Ki du palier ; Saint et au-dela levitent (MeditationMartiale) dans une aura puissante ; Dieu Martial : or blanc.
    MED = {1: None, 2: (("Sparks",), 0.35, 0.9), 3: (("Particles", "Sparks"), 0.55, 1.0), 4: (("Particles", "Sparks", "Lines"), 0.75, 1.1),
           5: (("Particles", "Sparks", "Lines", "Mist"), 0.95, 1.25), 6: (("Particles", "Sparks", "Lines", "Mist"), 1.6, 1.5),
           7: (("Particles", "Sparks", "Lines", "Mist"), 1.8, 1.65), 8: (("Particles", "Sparks", "Lines", "Mist"), 2.1, 1.8)}
    for n, spec in MED.items():
        color = "#fff3c8" if n == 8 else KI[n][0]
        if spec:
            system("Ki_Meditation_T%d" % n, color, spec[0], spec[1])
        effect("Meditation_Martiale_T%d" % n, "Ki_Meditation_T%d" % n if spec else None, spec[2] if spec else 1.0, 2.5,
               {"HorizontalSpeedMultiplier": 0, "MovementEffects": {"DisableAll": True},
                "AbilityEffects": {"Disabled": ["Primary", "Secondary"]}},
               "Meditation martiale, palier %d : posture assise, deplacements, attaque et garde bloques ; aura %s. Rafraichi par "
               "MeditationMartiale (gen_voie_martiale.py)." % (n, "aucune" if not spec else color), "Méditation")
    effect("Hwakyung_Corps", "Ki_Hwakyung", 1.5, 2.5, {"EntityBottomTint": "#3a3020", "EntityTopTint": "#fff3e6"},
           "Transformation Hwakyung : corps de Ki. Rafraichi par TransformationHwakyung (gen_voie_martiale.py).",
           "Corps Hwakyung")
    effect("Meridiens_Scelles", None, 1.0, 4.0, {"HorizontalSpeedMultiplier": 0.6, "EntityTopTint": "#ff9a9a"},
           "Frappe des Meridiens : meridiens scelles 4 s, ralenti (gen_voie_martiale.py).", "Méridiens scellés", debuff=True)
    effect("Unite_Vent", None, 1.0, 1.0, {"HorizontalSpeedMultiplier": 0.6},
           "Unite du Ciel et de la Terre : ennemis ralentis par le vent, rafraichi par le sort (gen_voie_martiale.py).",
           "Vent du Ciel et de la Terre", debuff=True)
    # Competences de la voie martiale (2026-11-10) : aura ambree du Renforcement corporel, vitesse apres un Pas leger.
    system("Ki_Renforcement", "#e8c070", ("Sparks", "Lines"), 0.5)
    effect("Renforcement_Corps", "Ki_Renforcement", 1.0, 2.5, {},
           "Renforcement corporel : corps durci par le Ki. Rafraichi par RenforcementCorps (gen_voie_martiale.py).",
           "Renforcement corporel")
    # Renforcement par palier (retour du 2026-11-10) : aura de la couleur du Ki du palier, de plus en plus dense et haute ;
    # corps legerement teinte a partir du Grand Maitre (sensation de force)
    for n, (color, parts, density) in KI.items():
        c = "#fff3c8" if n == 8 else color
        system("Ki_Renforcement_T%d" % n, c, ("Particles", "Sparks", "Lines", "Mist") if n >= 4 else ("Sparks", "Lines"),
               0.5 + 0.12 * n)
        extra = {"EntityTopTint": c} if n >= 5 else {}
        effect("Renforcement_Corps_T%d" % n, "Ki_Renforcement_T%d" % n, 1.0 + 0.06 * n, 2.5, extra,
               "Renforcement corporel, palier %d : aura %s. Rafraichi par RenforcementCorps (gen_voie_martiale.py)." % (n, c),
               "Renforcement corporel")
    # Incarnation de l'Ame (retour du 2026-11-10) : double translucide (ModelVFX Ame_Spectrale : Opacity), lueur blanc nacre
    write(os.path.join(RES, "Server", "Entity", "ModelVFX", "Ame_Spectrale.json"), {
        "SwitchTo": "PostColor", "EffectDirection": "BottomUp", "AnimationDuration": 0.8, "CurveType": "QuartOut",
        "NoiseScale": {"X": 30.0, "Y": 30.0}, "NoiseScrollSpeed": {"Y": 0.15, "X": 0.02},
        "PostColor": "#dff3ff", "PostColorOpacity": 0.45, "Opacity": 0.45,
        "HighlightColor": "#bfe8ff", "HighlightThickness": 2.0, "UseBloomOnHighlight": True, "LoopOption": "Loop"})
    effect("Incarnation_Ame", "Ki_Hwakyung", 1.2, 2.5, {"ModelVFXId": "Ame_Spectrale"},
           "Incarnation de l'Ame : double spirituel translucide (ModelVFX Ame_Spectrale), rafraichi par IncarnationAme "
           "(gen_voie_martiale.py).", "Âme")
    # Sillage du Pas leger (retour du 2026-11-10 soir) : particules Pas_Sillage_T<n> attachees aux hanches pendant la ruee et
    # corps lumineux de la couleur du Ki (ModelVFX Pas_Leger_Fantome_T<n>), pose par PasLeger.
    for n in range(3, 9):
        c = "#fff3c8" if n == 8 else KI[n][0]
        write(os.path.join(RES, "Server", "Entity", "ModelVFX", "Pas_Leger_Fantome_T%d.json" % n), {
            "SwitchTo": "PostColor", "EffectDirection": "BottomUp", "AnimationDuration": 0.25, "CurveType": "QuartOut",
            "NoiseScale": {"X": 20.0, "Y": 20.0}, "PostColor": c, "PostColorOpacity": 0.45,
            "HighlightColor": c, "HighlightThickness": 1.5, "UseBloomOnHighlight": True})
        # retour du 2026-11-10 (nuit) : sans les particules (la trainee vient des Trails du dash vanilla dans l'interaction)
        effect("Pas_Leger_Sillage_%d" % n, None, 1.0, 0.9, {"ModelVFXId": "Pas_Leger_Fantome_T%d" % n},
               "Pas leger, palier %d : sillage de Ki et corps lumineux pendant la ruee (PasLeger, gen_voie_martiale.py)." % n,
               "Pas léger")
    for n, mult in PAS_LEGER.items():
        effect("Pas_Leger_%d" % n, None, 1.0, 4.0, {"HorizontalSpeedMultiplier": mult},
               "Pas leger, palier %d : vitesse x%s pendant 4 s apres le pas (PasLeger, gen_voie_martiale.py)." % (n, mult),
               "Pas léger")
    epees_ki()
    epees_projection()
    sens_fantome()
    print("Voie martiale : %d auras de Ki, systemes et effets ecrits." % len(KI))


PAS_LEGER = {3: 1.15, 4: 1.18, 5: 1.22, 6: 1.26, 7: 1.3, 8: 1.35}


# Epees de Ki (competences Projection d'epee d'Aura et Vol sur epee de Ki, 2026-11-10) : katana du tresor de Gilgamesh
# (Katana.blockymodel vanilla) dont la texture de lumiere doree est recoloree en Ki bleu pale ; porte par un projectile
# immobile (gravite nulle) deplace par le code (SpellFx.hold). Ki_Epee = epee d'orbite (x1,4), Ki_Epee_Vol = epee geante
# sous les pieds (x3,2).
KI_SWORD_COLOR = "#bfe8ff"
KI_SWORDS = {"Ki_Epee": 1.4, "Ki_Epee_Vol": 3.2}


def epees_ki():
    sys.path.insert(0, os.path.join(HERE, "..", "rune_aura_icons"))
    import png
    src = os.path.join(RES, "Common", "Items", "Projectiles", "Gilgamesh", "Light_Katana.png")
    w, h, px = png.read(src)
    target = hex2rgb(KI_SWORD_COLOR)
    out = []
    for row in px:
        line = []
        for r, g, b, a in row:
            lum = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
            white = max(0.0, (lum - 0.75) * 3.0)
            line.append([min(255, t * min(1.0, lum * 1.25) * (1 - white) + 255 * white) for t in target] + [a])
        out.append(line)
    tex_rel = "Items/Projectiles/Ki/Ki_Epee.png"
    os.makedirs(os.path.join(RES, "Common", "Items", "Projectiles", "Ki"), exist_ok=True)
    png.write(os.path.join(RES, "Common", tex_rel), w, h, out)
    models = os.path.join(RES, "Server", "Models", "Projectiles", "Abilities")
    configs = os.path.join(RES, "Server", "ProjectileConfigs", "Abilities")
    for name, scale in KI_SWORDS.items():
        dump(os.path.join(models, name + ".json"), {
            "$Comment": "Epee de Ki de la voie martiale (gen_voie_martiale.py, epees_ki) : katana de lumiere recolore en Ki.",
            "Model": "Items/Projectiles/Projectile.blockymodel", "Texture": "Items/Projectiles/Projectile_default.png",
            "DefaultAttachments": [{"Model": "Items/Weapons/Longsword/Katana.blockymodel", "Texture": tex_rel}],
            "AnimationSets": {"FlyIdle": {"Animations": [{"Animation": "Items/Animations/Fly/Fly_Vertical.blockyanim"}]}},
            "MinScale": scale, "MaxScale": scale,
            "HitBox": {"Max": {"X": 0.5, "Y": 0.1, "Z": 0.1}, "Min": {"X": -0.5, "Y": -0.1, "Z": -0.1}},
            "Light": {"Color": KI_SWORD_COLOR, "Radius": 5}})
        dump(os.path.join(configs, "Projectile_Config_" + name + ".json"), {
            "$Comment": "Epee de Ki immobile, deplacee par le code (ProjectionEpeeAura, VolEpeeKi). Interactions neutres obligatoires.",
            "Model": name, "UseModelScale": True,
            "Physics": {"Type": "Standard", "Gravity": 0, "TerminalVelocityAir": 1, "TerminalVelocityWater": 1, "Bounciness": 0,
                        "SticksVertically": False},
            "LaunchForce": 0, "SpawnOffset": {"X": 0, "Y": 0, "Z": 0},
            "Interactions": {"ProjectileHit": {"Interactions": [{"Type": "Simple", "RunTime": 0.01}]},
                             "ProjectileMiss": {"Interactions": [{"Type": "Simple", "RunTime": 0.01}]}}})


# Epees 3D de Ki de la Projection d'epee d'Aura (retour du 2026-11-10, soir) : le katana vanilla (Katana.blockymodel), texture
# de lumiere recoloree a la couleur du Ki du palier, rendu translucide par un ModelVFX (Ki_Epee_Spectrale_T<n>, porte par
# l'effet Ki_Epee_Corps_T<n> que ProjectionEpeeAura pose sur l'epee), une trainee (Ki_Epee_Trail_T<n>) et une fumee
# continue (particules Ki_Epee_Fumee_T<n>, gen_fx_martiaux.py) attachees a la lame. Porteur projectile immobile.
KI_PROJ_SCALE = 1.5


def epees_projection():
    sys.path.insert(0, os.path.join(HERE, "..", "rune_aura_icons"))
    import png
    src = os.path.join(RES, "Common", "Items", "Projectiles", "Gilgamesh", "Light_Katana.png")
    w, h, px = png.read(src)
    models = os.path.join(RES, "Server", "Models", "Projectiles", "Abilities")
    configs = os.path.join(RES, "Server", "ProjectileConfigs", "Abilities")
    for n, (color, _, _) in KI.items():
        c = "#fff3c8" if n == 8 else color
        target = hex2rgb(c)
        out = []
        for row in px:
            line = []
            for r, g, b, a in row:
                lum = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
                white = max(0.0, (lum - 0.7) * 2.5)                         # coeur blanc, bords de la couleur du Ki
                line.append([min(255, t * min(1.0, 0.35 + lum) * (1 - white) + 255 * white) for t in target] + [a])
            out.append(line)
        tex_rel = "Items/Projectiles/Ki/Ki_Epee_T%d.png" % n
        png.write(os.path.join(RES, "Common", tex_rel), w, h, out)
        name = "Ki_Epee_Proj_T%d" % n
        dump(os.path.join(models, name + ".json"), {
            "$Comment": "Epee 3D de Ki, palier %d (Projection d'epee d'Aura) : katana vanilla recolore, trainee et fumee de Ki "
                        "(gen_voie_martiale.py, epees_projection). L'origine du modele est la pointe." % n,
            "Model": "Items/Projectiles/Projectile.blockymodel", "Texture": "Items/Projectiles/Projectile_default.png",
            "DefaultAttachments": [{"Model": "Items/Weapons/Longsword/Katana.blockymodel", "Texture": tex_rel}],
            "MinScale": KI_PROJ_SCALE, "MaxScale": KI_PROJ_SCALE,
            "HitBox": {"Max": {"X": 0.5, "Y": 0.1, "Z": 0.1}, "Min": {"X": -0.5, "Y": -0.1, "Z": -0.1}},
            "Trails": [{"TrailId": "Ki_Epee_Trail_T%d" % n, "TargetNodeName": "Sword Base", "PositionOffset": {"X": 0.3, "Y": 0, "Z": 0}}],
            "Particles": [{"SystemId": "Ki_Epee_Fumee_T%d" % n, "TargetNodeName": "Sword Base", "PositionOffset": {"X": 0.3, "Y": 0, "Z": 0}}],
            "Light": {"Color": c, "Radius": 6}})
        dump(os.path.join(configs, "Projectile_Config_" + name + ".json"), {
            "$Comment": "Epee 3D de Ki immobile, deplacee par le code (ProjectionEpeeAura). Interactions neutres obligatoires.",
            "Model": name, "UseModelScale": True,
            "Physics": {"Type": "Standard", "Gravity": 0, "TerminalVelocityAir": 1, "TerminalVelocityWater": 1, "Bounciness": 0,
                        "SticksVertically": False},
            "LaunchForce": 0, "SpawnOffset": {"X": 0, "Y": 0, "Z": 0},
            "Interactions": {"ProjectileHit": {"Interactions": [{"Type": "Simple", "RunTime": 0.01}]},
                             "ProjectileMiss": {"Interactions": [{"Type": "Simple", "RunTime": 0.01}]}}})
        dump(os.path.join(RES, "Server", "Entity", "Trails", "Ki_Epee_Trail_T%d.json" % n), {
            "TexturePath": "Trails/Basic2.png", "LifeSpan": 14, "Roll": 90,
            "Start": {"Color": "rgba(%s, 0.85)" % c, "Width": 0.9},
            "End": {"Color": "rgba(%s, 0)" % c, "Width": 0.15},
            "LightInfluence": 0, "Smooth": True, "RenderMode": "BlendAdd"})
        write(os.path.join(RES, "Server", "Entity", "ModelVFX", "Ki_Epee_Spectrale_T%d.json" % n), {
            "SwitchTo": "PostColor", "EffectDirection": "BottomUp", "AnimationDuration": 0.6, "CurveType": "QuartOut",
            "NoiseScale": {"X": 25.0, "Y": 25.0}, "NoiseScrollSpeed": {"Y": 0.25, "X": 0.03},
            "PostColor": c, "PostColorOpacity": 0.5, "Opacity": 0.55,
            "HighlightColor": c, "HighlightThickness": 2.5, "UseBloomOnHighlight": True, "LoopOption": "Loop"})
        effect("Ki_Epee_Corps_T%d" % n, None, 1.0, 60.0, {"ModelVFXId": "Ki_Epee_Spectrale_T%d" % n},
               "Epee 3D de Ki translucide, palier %d (ModelVFX Ki_Epee_Spectrale_T%d), posee par ProjectionEpeeAura "
               "(gen_voie_martiale.py)." % (n, n), "Épée de Ki")


# Sens martial « a travers les murs » (retour du 2026-11-10 soir) : pour un ennemi cache par un mur, SensMartial place devant
# le mur, sur la ligne de vue, un double reduit de son modele (echelle = distance du double / distance reelle : il couvre a
# l'ecran exactement la place de l'ennemi), rouge (ModelVFX Sens_Fantome_Rouge, effet Sens_Fantome), vu du seul lanceur
# (SensGhostVisibility). Porteur projectile au modele vide, remplace par le modele de l'ennemi (SpellFx.setModelScale).
def sens_fantome():
    models = os.path.join(RES, "Server", "Models", "Projectiles", "Abilities")
    configs = os.path.join(RES, "Server", "ProjectileConfigs", "Abilities")
    dump(os.path.join(models, "Sens_Fantome_Porteur.json"), {
        "$Comment": "Porteur invisible du double rouge du Sens martial (gen_voie_martiale.py) ; son modele est remplace par celui de l'ennemi.",
        "Model": "Items/Weapons/artefacts/Artefact_Sword_Tenryumaru_Main_Vide.blockymodel",
        "Texture": "Items/Projectiles/Projectile_default.png", "MinScale": 1.0, "MaxScale": 1.0,
        "HitBox": {"Max": {"X": 0.5, "Y": 0.5, "Z": 0.5}, "Min": {"X": -0.5, "Y": -0.5, "Z": -0.5}}})
    dump(os.path.join(configs, "Projectile_Config_Sens_Fantome.json"), {
        "$Comment": "Double rouge du Sens martial, immobile, deplace par le code (SensMartial). Interactions neutres obligatoires.",
        "Model": "Sens_Fantome_Porteur", "UseModelScale": True,
        "Physics": {"Type": "Standard", "Gravity": 0, "TerminalVelocityAir": 1, "TerminalVelocityWater": 1, "Bounciness": 0,
                    "SticksVertically": False},
        "LaunchForce": 0, "SpawnOffset": {"X": 0, "Y": 0, "Z": 0},
        "Interactions": {"ProjectileHit": {"Interactions": [{"Type": "Simple", "RunTime": 0.01}]},
                         "ProjectileMiss": {"Interactions": [{"Type": "Simple", "RunTime": 0.01}]}}})
    write(os.path.join(RES, "Server", "Entity", "ModelVFX", "Sens_Fantome_Rouge.json"), {
        "SwitchTo": "PostColor", "EffectDirection": "BottomUp", "AnimationDuration": 0.3, "CurveType": "QuartOut",
        "NoiseScale": {"X": 18.0, "Y": 18.0}, "NoiseScrollSpeed": {"Y": 0.2, "X": 0.0},
        "PostColor": "#ff1a1a", "PostColorOpacity": 0.85, "Opacity": 0.8,
        "HighlightColor": "#ff4040", "HighlightThickness": 2.0, "UseBloomOnHighlight": True, "LoopOption": "Loop"})
    # au loin (plus de NEAR blocs, SensMartial) : rouge sombre, plus discret (retour du 2026-11-10 nuit : affiner)
    write(os.path.join(RES, "Server", "Entity", "ModelVFX", "Sens_Fantome_Sombre.json"), {
        "SwitchTo": "PostColor", "EffectDirection": "BottomUp", "AnimationDuration": 0.3, "CurveType": "QuartOut",
        "NoiseScale": {"X": 18.0, "Y": 18.0}, "NoiseScrollSpeed": {"Y": 0.1, "X": 0.0},
        "PostColor": "#8a0a14", "PostColorOpacity": 0.8, "Opacity": 0.6,
        "HighlightColor": "#c0202a", "HighlightThickness": 1.2, "UseBloomOnHighlight": True, "LoopOption": "Loop"})
    effect("Sens_Fantome_Loin", None, 1.0, 120.0, {"ModelVFXId": "Sens_Fantome_Sombre"},
           "Sens martial : double d'un ennemi lointain (rouge sombre), pose par SensMartial (gen_voie_martiale.py).", "Sens martial")
    effect("Sens_Fantome", None, 1.0, 120.0, {"ModelVFXId": "Sens_Fantome_Rouge"},
           "Sens martial : double rouge d'un ennemi vu a travers les murs (ModelVFX Sens_Fantome_Rouge), pose par SensMartial "
           "(gen_voie_martiale.py).", "Sens martial")


def dump(path, data):
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")


if __name__ == "__main__":
    main()
