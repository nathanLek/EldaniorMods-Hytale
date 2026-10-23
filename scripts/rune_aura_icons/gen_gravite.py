"""Assets visuels du Puits Gravitationnel (rune Spatial, Unique).

1. Modele porteur de trails : noeuds repartis sur 3 anneaux (rayons 3.2 / 2.0 / 0.8) qui TOMBENT en boucle de 50 blocs
   (animation de position) -> chaque trail dessine un trait vertical qui chute (principe de la tornade vanilla : trails
   sur des noeuds animes, sans geometrie).
2. Systeme de particules emis par le code : traits noir-violet epais, entraines vers le bas par une acceleration lineaire
   (ne depend pas du signe du Pitch), voile sombre sur le bord du cylindre, poussiere plaquee au sol.

Usage : python3 gen_gravite.py <src/main/resources>
"""
import json, math, os, sys

RES = sys.argv[1]
U = 64                  # unites par bloc (modeles d'entite)
H = 50                  # hauteur de la colonne (blocs)
ZONE = 3.5              # rayon de la zone (7 blocs de diametre)
DURATION = 60           # 1 s par chute (60 frames)
IDENT = {"x": 0, "y": 0, "z": 0, "w": 1}

def node(nid, name, pos, children=()):
    return {"id": str(nid), "name": name, "children": list(children),
            "position": {"x": round(pos[0], 2), "y": round(pos[1], 2), "z": round(pos[2], 2)}, "orientation": IDENT,
            "shape": {"type": "none", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": 1, "y": 1, "z": 1}, "settings": {"isPiece": False},
                      "visible": True, "doubleSided": False, "shadingMode": "flat", "unwrapMode": "custom", "textureLayout": {}}}

def r(a, b): return {"Min": a, "Max": b}

# ---------------- 1. Modele de trails qui tombent ----------------
kids, anims, trails = [], {}, []
nid = 1
k = 0
for radius, count in ((3.2, 14), (2.0, 9), (0.8, 4)):
    for i in range(count):
        a = 2 * math.pi * i / count + radius
        name = f"G{k}"
        kids.append(node(nid, name, (math.cos(a) * radius * U, 0, math.sin(a) * radius * U))); nid += 1
        phase = 0.05 + ((k * 0.37) % 0.9)                                    # chutes decalees
        tb = round((1 - phase) * DURATION)
        frames = [{"time": 0, "delta": {"x": 0, "y": round(H * (1 - phase) * U, 1), "z": 0}, "interpolationType": "linear"},
                  {"time": tb, "delta": {"x": 0, "y": 0, "z": 0}, "interpolationType": "linear"}]
        if tb + 1 < DURATION:
            frames.append({"time": tb + 1, "delta": {"x": 0, "y": H * U, "z": 0}, "interpolationType": "linear"})
        anims[name] = {"position": frames, "orientation": [], "shapeStretch": [], "shapeVisible": [], "shapeUvOffset": []}
        trails.append({"TargetNodeName": name, "TrailId": "Gravite_Trait"})
        if k % 2 == 0: trails.append({"TargetNodeName": name, "TrailId": "Gravite_Trait_Lueur", "PositionOffset": {"X": 0.3, "Y": 0, "Z": 0}})
        k += 1

D = os.path.join(RES, "Common/Items/Projectiles/Gravite"); os.makedirs(D, exist_ok=True)
json.dump({"nodes": [node(0, "Base", (0, 0, 0), kids)], "lod": "auto"}, open(os.path.join(D, "Colonne.blockymodel"), "w"), indent=1)
json.dump({"formatVersion": 1, "duration": DURATION, "holdLastKeyframe": False, "nodeAnimations": anims}, open(os.path.join(D, "Colonne_Chute.blockyanim"), "w"), indent=1)
json.dump({"$Comment": "Colonne du Puits Gravitationnel (genere par scripts/rune_aura_icons/gen_gravite.py) : traits noir-violet qui tombent sur 50 blocs dans un cylindre de 7 blocs de diametre. HitBox large en X : le serveur masque les entites a hitbox minuscule.",
           "Model": "Items/Projectiles/Gravite/Colonne.blockymodel", "Texture": "Items/Projectiles/Tornado_Texture.png", "MinScale": 1, "MaxScale": 1,
           "HitBox": {"Max": {"X": 8.0, "Y": 0.01, "Z": 0.01}, "Min": {"X": -8.0, "Y": -0.01, "Z": -0.01}},
           "AnimationSets": {"Idle": {"Animations": [{"Animation": "Items/Projectiles/Gravite/Colonne_Chute.blockyanim", "BlendingDuration": 0, "Speed": 1}]}},
           "Trails": trails, "Light": {"Color": "#5a1aa0", "Radius": 12}},
          open(os.path.join(RES, "Server/Models/Projectiles/Abilities/Gravite_Colonne.json"), "w"), indent=2)
cfg = {"$Comment": "Porte la colonne du Puits Gravitationnel. Interactions neutres obligatoires.", "Model": "Gravite_Colonne", "UseModelScale": True,
       "Physics": {"Type": "Standard", "Gravity": 0, "TerminalVelocityAir": 1, "TerminalVelocityWater": 1, "Bounciness": 0, "SticksVertically": False},
       "LaunchForce": 0, "SpawnOffset": {"X": 0, "Y": 0, "Z": 0},
       "Interactions": {"ProjectileHit": {"Interactions": [{"Type": "Simple", "RunTime": 0.01}]}, "ProjectileMiss": {"Interactions": [{"Type": "Simple", "RunTime": 0.01}]}}}
json.dump(cfg, open(os.path.join(RES, "Server/ProjectileConfigs/Abilities/Projectile_Config_Gravite_Colonne.json"), "w"), indent=2)
T = os.path.join(RES, "Server/Entity/Trails")
json.dump({"$Comment": "Trait noir-violet qui tombe dans la colonne de gravite.", "TexturePath": "Trails/Basic2.png", "LifeSpan": 240, "Roll": 0,
           "Start": {"Color": "rgba(#1a0729, 0.95)", "Width": 0.8}, "End": {"Color": "rgba(#3b1260, 0)", "Width": 0.3},
           "Smooth": True, "RenderMode": "BlendLinear", "LightInfluence": 0}, open(os.path.join(T, "Gravite_Trait.json"), "w"), indent=2)
json.dump({"$Comment": "Lueur violette d'un trait de gravite.", "TexturePath": "Trails/Basic2.png", "LifeSpan": 200, "Roll": 90,
           "Start": {"Color": "rgba(#b070ff, 0.9)", "Width": 0.5}, "End": {"Color": "rgba(#6a22c8, 0)", "Width": 0.1},
           "Smooth": True, "RenderMode": "BlendAdd", "LightInfluence": 0}, open(os.path.join(T, "Gravite_Trait_Lueur.json"), "w"), indent=2)

# ---------------- 2. Particules emises par le code ----------------
P = os.path.join(RES, "Server/Particles/Spell/Gravite"); os.makedirs(os.path.join(P, "Spawners"), exist_ok=True)
for old in ("Gravite_Streak_Dark", "Gravite_Streak_Glow"):
    f = os.path.join(P, "Spawners", old + ".particlespawner")
    if os.path.exists(f): os.remove(f)

def falling(name, comment, blend, rate, colors, sx, sy, op, tex="Particles/Textures/Basic/Glow.png", frame=None, ymin=6, h=H):
    part = {"Texture": tex, "ScaleRatioConstraint": "None", "UVOption": "None", "SoftParticles": "Disable",
            "InitialAnimationFrame": {"Opacity": 0, "Color": colors[0], "Scale": {"X": r(*sx), "Y": r(*sy)}},
            "Animation": {"0": {"Opacity": 0, "Color": colors[0]}, "12": {"Opacity": op, "Color": colors[0]}, "75": {"Opacity": op * 0.9, "Color": colors[1]}, "100": {"Opacity": 0, "Color": colors[2]}}}
    if frame: part["FrameSize"] = {"Width": frame[0], "Height": frame[1]}
    json.dump({"$Comment": comment, "RenderMode": blend,
               "EmitOffset": {"X": r(-0.6, 0.6), "Y": r(ymin, h), "Z": r(-0.6, 0.6)},
               "ParticleRotationInfluence": "Velocity", "LinearFiltering": True, "LightInfluence": 0, "ParticleRotateWithSpawner": False,
               "MaxConcurrentParticles": 120, "SpawnRate": r(rate, rate), "ParticleLifeSpan": r(0.8, 1.2),
               "InitialVelocity": {"Yaw": r(-180, 180), "Pitch": r(-10, 10), "Speed": r(0.5, 1.0)},
               "Attractors": [{"Position": {"X": 0, "Y": 0, "Z": 0}, "Radius": 0, "LinearAcceleration": {"X": 0, "Y": -75, "Z": 0}}],
               "Particle": part}, open(os.path.join(P, "Spawners", name + ".particlespawner"), "w"), indent=2)

falling("Gravite_Pluie_Noire", "Trait noir epais aspire vers le sol par la gravite (acceleration vers le bas : chute garantie).", "BlendLinear", 34,
        ("#12041c", "#1f0933", "#2c0d4a"), (0.45, 0.65), (4.5, 7.5), 0.92)
falling("Gravite_Pluie_Violette", "Trait violet lumineux qui tombe dans la colonne de gravite.", "BlendAdd", 20,
        ("#d4a3ff", "#9446ff", "#4a1599"), (0.3, 0.45), (3.5, 6.0), 0.9)
falling("Gravite_Voile", "Voile de fumee sombre qui tombe sur le bord du cylindre de gravite.", "BlendLinear", 8,
        ("#160a22", "#22113a", "#2f1a4d"), (2.5, 3.5), (5.0, 8.0), 0.55, tex="Particles/Textures/Smoke/Smoke_Dense.png", frame=(64, 64), ymin=2)
json.dump({"$Comment": "Poussiere ecrasee, plaquee au sol de toute la zone de gravite.", "RenderMode": "BlendLinear",
           "EmitOffset": {"X": r(-ZONE, ZONE), "Y": r(0, 0.4), "Z": r(-ZONE, ZONE)}, "ParticleRotationInfluence": "Billboard", "LinearFiltering": True,
           "LightInfluence": 0.4, "ParticleRotateWithSpawner": False, "MaxConcurrentParticles": 90, "SpawnRate": r(60, 60), "ParticleLifeSpan": r(0.9, 1.3),
           "InitialVelocity": {"Yaw": r(-180, 180), "Pitch": r(-10, 0), "Speed": r(0.3, 1.0)},
           "Attractors": [{"Position": {"X": 0, "Y": 0, "Z": 0}, "Radius": 0, "LinearAcceleration": {"X": 0, "Y": -20, "Z": 0}}],
           "Particle": {"Texture": "Particles/Textures/Smoke/Smoke_Dense.png", "FrameSize": {"Width": 64, "Height": 64}, "ScaleRatioConstraint": "None", "UVOption": "RandomFlipU",
                        "InitialAnimationFrame": {"Opacity": 0, "Color": "#1c0f26", "Scale": {"X": r(2.2, 3.2), "Y": r(0.8, 1.2)}},
                        "Animation": {"0": {"Opacity": 0}, "20": {"Opacity": 0.6, "Color": "#241233"}, "100": {"Opacity": 0, "Color": "#3a1d52"}}}},
          open(os.path.join(P, "Spawners", "Gravite_Sol.particlespawner"), "w"), indent=2)
json.dump({"$Comment": "Energie gravitationnelle qui suinte d'une fissure : lueur violette qui monte a peine puis est rabattue au sol.", "RenderMode": "BlendAdd",
           "EmitOffset": {"X": r(-0.45, 0.45), "Y": r(-0.3, 0.1), "Z": r(-0.45, 0.45)}, "ParticleRotationInfluence": "Billboard", "LinearFiltering": True,
           "LightInfluence": 0, "ParticleRotateWithSpawner": False, "MaxConcurrentParticles": 14, "SpawnRate": r(22, 22), "ParticleLifeSpan": r(0.6, 1.0),
           "InitialVelocity": {"Yaw": r(-180, 180), "Pitch": r(60, 90), "Speed": r(1.0, 2.2)},
           "Attractors": [{"Position": {"X": 0, "Y": 0, "Z": 0}, "Radius": 0, "LinearAcceleration": {"X": 0, "Y": -6, "Z": 0}}],
           "Particle": {"Texture": "Particles/Textures/Basic/Glow.png", "ScaleRatioConstraint": "OneToOne", "UVOption": "None", "SoftParticles": "Disable",
                        "InitialAnimationFrame": {"Opacity": 0, "Color": "#c27bff", "Scale": {"X": r(0.35, 0.6)}},
                        "Animation": {"0": {"Opacity": 0}, "20": {"Opacity": 0.95, "Color": "#a24dff"}, "100": {"Opacity": 0, "Color": "#4a1599", "Scale": {"X": r(0.15, 0.2)}}}}},
          open(os.path.join(P, "Spawners", "Gravite_Fissure_Lueur.particlespawner"), "w"), indent=2)
json.dump({"$Comment": "Fumee sombre et grains qui sortent d'une fissure et retombent dedans.", "RenderMode": "BlendLinear",
           "EmitOffset": {"X": r(-0.45, 0.45), "Y": r(-0.2, 0.2), "Z": r(-0.45, 0.45)}, "ParticleRotationInfluence": "Billboard", "LinearFiltering": True,
           "LightInfluence": 0.3, "ParticleRotateWithSpawner": False, "MaxConcurrentParticles": 10, "SpawnRate": r(12, 12), "ParticleLifeSpan": r(0.7, 1.1),
           "InitialVelocity": {"Yaw": r(-180, 180), "Pitch": r(50, 85), "Speed": r(0.8, 1.6)},
           "Attractors": [{"Position": {"X": 0, "Y": 0, "Z": 0}, "Radius": 0, "LinearAcceleration": {"X": 0, "Y": -4, "Z": 0}}],
           "Particle": {"Texture": "Particles/Textures/Smoke/Smoke_Dense.png", "FrameSize": {"Width": 64, "Height": 64}, "ScaleRatioConstraint": "OneToOne", "UVOption": "RandomFlipU",
                        "InitialAnimationFrame": {"Rotation": {"Z": r(0, 360)}, "Opacity": 0, "Color": "#1a0b26", "Scale": {"X": r(0.7, 1.1)}},
                        "Animation": {"0": {"Opacity": 0}, "25": {"Opacity": 0.7, "Color": "#24113a"}, "100": {"Opacity": 0, "Color": "#3a1d52", "Scale": {"X": r(1.4, 1.8)}}}}},
          open(os.path.join(P, "Spawners", "Gravite_Fissure_Fumee.particlespawner"), "w"), indent=2)
json.dump({"$Comment": "Fissure gravitationnelle : lueur violette et fumee sombre qui suintent d'une case de fissure (emis par le code le long des fissures).",
           "Spawners": [{"SpawnerId": "Gravite_Fissure_Lueur"}, {"SpawnerId": "Gravite_Fissure_Fumee"}],
           "LifeSpan": 0.6, "BoundingRadius": 3, "IsImportant": True, "CullDistance": 160},
          open(os.path.join(P, "Gravite_Fissure.particlesystem"), "w"), indent=2)

sp = []
for radius, count, glow_every, veil in ((3.2, 14, 1, True), (2.0, 9, 1, False), (0.9, 4, 1, False), (0.0, 1, 1, False)):
    for i in range(count):
        a = 2 * math.pi * i / count
        off = {"X": round(math.cos(a) * radius, 2), "Y": 0, "Z": round(math.sin(a) * radius, 2)}
        sp.append({"SpawnerId": "Gravite_Pluie_Noire", "PositionOffset": off})
        if i % glow_every == 0: sp.append({"SpawnerId": "Gravite_Pluie_Violette", "PositionOffset": off})
        if veil: sp.append({"SpawnerId": "Gravite_Voile", "PositionOffset": off})
sp.append({"SpawnerId": "Gravite_Sol"})
json.dump({"$Comment": "Colonne du Puits Gravitationnel : pluie de traits noir-violet (cylindre de 7 blocs de diametre, 50 de haut), voile sombre au bord, poussiere plaquee au sol. Genere par gen_gravite.py, reemis par le code toutes les 0.5 s.",
           "Spawners": sp, "LifeSpan": 0.7, "BoundingRadius": 28, "IsImportant": True, "CullDistance": 300},
          open(os.path.join(P, "Gravite_Colonne.particlesystem"), "w"), indent=2)
print(f"modele : {len(kids)} traits qui tombent ({len(trails)} trails) ; particules : {len(sp)} emetteurs")


# =====================================================================================================================
# 3. Colonne allegee : une par ennemi ecrase par l'Autorite Gravitationnelle (jusqu'a 10 a la fois)
# =====================================================================================================================
sp = []
for radius, count in ((3.2, 8), (1.6, 4), (0.0, 1)):
    for i in range(count):
        a = 2 * math.pi * i / count
        off = {"X": round(math.cos(a) * radius, 2), "Y": 0, "Z": round(math.sin(a) * radius, 2)}
        sp.append({"SpawnerId": "Gravite_Pluie_Noire", "PositionOffset": off})
        if i % 2 == 0: sp.append({"SpawnerId": "Gravite_Pluie_Violette", "PositionOffset": off})
json.dump({"$Comment": "Colonne de gravite allegee (une par ennemi ecrase par l'Autorite Gravitationnelle) : meme pluie noir-violet que le Puits Gravitationnel, moins d'emetteurs. Genere par gen_gravite.py.",
           "Spawners": sp, "LifeSpan": 0.7, "BoundingRadius": 28, "IsImportant": True, "CullDistance": 300},
          open(os.path.join(P, "Gravite_Colonne_Legere.particlesystem"), "w"), indent=2)
print(f"colonne allegee : {len(sp)} emetteurs")
