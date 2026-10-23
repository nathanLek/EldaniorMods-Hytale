"""Main spectrale de la Telekinesie : modele 3D (paume, 4 doigts a 2 phalanges recourbes, pouce, poignet), texture violette
lumineuse et translucide, aura d'etincelles attachee (en boucle) et trainees au bout des doigts.

La main plane au-dessus de ce qui est tenu, paume vers le bas, doigts recourbes autour de la prise.
Doigts vers -Z (avant du modele) ; si la main apparait a l'envers en jeu, passer HAND_REVERSED a true dans Telekinesie.java.

Usage : python3 gen_telekinesie.py <src/main/resources>
"""
import json, math, os, random, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import png
from gen_divine_models import q_axis, node, save_model

RES = sys.argv[1]
D = os.path.join(RES, "Common/Items/Projectiles/Telekinesie"); os.makedirs(D, exist_ok=True)

# ---------------- Texture : lueur violette translucide, veinee de lumiere ----------------
rnd = random.Random(7)
px = []
for y in range(32):
    row = []
    for x in range(32):
        v = 0.5 + 0.5 * math.sin(x * 0.7 + math.sin(y * 0.5) * 2) * math.cos(y * 0.4)
        r, g, b = 150 + 70 * v, 90 + 70 * v, 235 + 20 * v
        row.append([min(255, r + rnd.uniform(-8, 8)), min(255, g + rnd.uniform(-8, 8)), min(255, b), 175 + 50 * v])
    px.append(row)
png.write(os.path.join(D, "Main.png"), 32, 32, px)

# ---------------- Modele (64 unites = 1 bloc ; doigts vers -Z) ----------------
CURL1, CURL2 = -35, -70             # rotation autour de X : doigts recourbes vers le bas (vers -Y quand on avance vers -Z)
fingers = []
for i, (x, length) in enumerate(((-33, 46), (-11, 52), (11, 50), (33, 42))):
    tip = node(f"Doigt{i}_Bout", (0, 0, -length), q_axis((1, 0, 0), CURL2), (15, 15, 40), offset=(0, 0, -20))
    mid = node(f"Doigt{i}", (x, 0, -40), q_axis((1, 0, 0), CURL1), (17, 17, length), offset=(0, 0, -length / 2), children=[tip])
    fingers.append(mid)
thumb_tip = node("Pouce_Bout", (0, 0, -34), q_axis((1, 0, 0), -50), (16, 16, 32), offset=(0, 0, -16))
thumb = node("Pouce", (52, -4, -5), q_axis((0, 1, 0), -40), (18, 18, 36), offset=(0, 0, -18), children=[thumb_tip])
wrist = node("Poignet", (0, 4, 52), None or {"x": 0, "y": 0, "z": 0, "w": 1}, (62, 30, 46), offset=(0, 0, 0))
palm = node("Paume", (0, 0, 0), {"x": 0, "y": 0, "z": 0, "w": 1}, (90, 26, 80), offset=(0, 0, 0), children=fingers + [thumb, wrist])
root = node("Base", (0, 0, 0), children=[palm])
save_model(os.path.join(D, "Main.blockymodel"), root)

# ---------------- Particules attachees (en boucle tant que la main existe) ----------------
P = os.path.join(RES, "Server/Particles/Spell/Telekinesie"); os.makedirs(os.path.join(P, "Spawners"), exist_ok=True)
def r(a, b): return {"Min": a, "Max": b}
json.dump({"$Comment": "Etincelles violettes qui s'echappent de la main spectrale (continu, attache au modele).", "RenderMode": "BlendAdd",
           "EmitOffset": {"X": r(-0.9, 0.9), "Y": r(-0.4, 0.4), "Z": r(-1.4, 0.8)}, "ParticleRotationInfluence": "Billboard", "LinearFiltering": True,
           "LightInfluence": 0, "ParticleRotateWithSpawner": False, "MaxConcurrentParticles": 40, "SpawnRate": r(30, 30), "ParticleLifeSpan": r(0.5, 0.9),
           "InitialVelocity": {"Yaw": r(-180, 180), "Pitch": r(-60, 30), "Speed": r(0.4, 1.2)},
           "Attractors": [{"Position": {"X": 0, "Y": 0, "Z": 0}, "Radius": 0, "LinearAcceleration": {"X": 0, "Y": -1.5, "Z": 0}}],
           "Particle": {"Texture": "Particles/Textures/Basic/Glow.png", "ScaleRatioConstraint": "OneToOne", "UVOption": "None", "SoftParticles": "Disable",
                        "InitialAnimationFrame": {"Opacity": 0, "Color": "#e2c2ff", "Scale": {"X": r(0.12, 0.25)}},
                        "Animation": {"0": {"Opacity": 0}, "20": {"Opacity": 1, "Color": "#b878ff"}, "100": {"Opacity": 0, "Color": "#5b1fb0"}}}},
          open(os.path.join(P, "Spawners", "Telekinesie_Main_Etincelles.particlespawner"), "w"), indent=2)
json.dump({"$Comment": "Brume violette qui enveloppe la main spectrale (continu).", "RenderMode": "BlendAdd",
           "EmitOffset": {"X": r(-0.6, 0.6), "Y": r(-0.3, 0.3), "Z": r(-0.9, 0.6)}, "ParticleRotationInfluence": "Billboard", "LinearFiltering": True,
           "LightInfluence": 0, "ParticleRotateWithSpawner": False, "MaxConcurrentParticles": 10, "SpawnRate": r(8, 8), "ParticleLifeSpan": r(0.8, 1.2),
           "InitialVelocity": {"Yaw": r(-180, 180), "Pitch": r(-10, 30), "Speed": r(0.1, 0.4)},
           "Particle": {"Texture": "Particles/Textures/Smoke/Smoke_Dense.png", "FrameSize": {"Width": 64, "Height": 64}, "ScaleRatioConstraint": "OneToOne", "UVOption": "RandomFlipU",
                        "InitialAnimationFrame": {"Rotation": {"Z": r(0, 360)}, "Opacity": 0, "Color": "#6a2fc0", "Scale": {"X": r(1.0, 1.4)}},
                        "Animation": {"0": {"Opacity": 0}, "30": {"Opacity": 0.35}, "100": {"Opacity": 0, "Scale": {"X": r(1.8, 2.2)}}}}},
          open(os.path.join(P, "Spawners", "Telekinesie_Main_Brume.particlespawner"), "w"), indent=2)
json.dump({"$Comment": "Aura de la main spectrale de la Telekinesie (en boucle : attachee au modele, s'arrete avec lui).",
           "Spawners": [{"SpawnerId": "Telekinesie_Main_Brume"}, {"SpawnerId": "Telekinesie_Main_Etincelles"}], "BoundingRadius": 4, "IsImportant": True, "CullDistance": 200},
          open(os.path.join(P, "Telekinesie_Main_Aura.particlesystem"), "w"), indent=2)

T = os.path.join(RES, "Server/Entity/Trails")
json.dump({"$Comment": "Trainee spectrale au bout des doigts de la main de Telekinesie.", "TexturePath": "Trails/Basic2.png", "LifeSpan": 300, "Roll": 0,
           "Start": {"Color": "rgba(#c58bff, 0.85)", "Width": 0.35}, "End": {"Color": "rgba(#6a22c8, 0)", "Width": 0.05},
           "Smooth": True, "RenderMode": "BlendAdd", "LightInfluence": 0}, open(os.path.join(T, "Telekinesie_Trainee.json"), "w"), indent=2)

M = {"$Comment": "Main spectrale de la Telekinesie (genere par scripts/rune_aura_icons/gen_telekinesie.py). HitBox large en X : le serveur masque les entites a hitbox minuscule.",
     "Model": "Items/Projectiles/Telekinesie/Main.blockymodel", "Texture": "Items/Projectiles/Telekinesie/Main.png", "MinScale": 1, "MaxScale": 1,
     "HitBox": {"Max": {"X": 1.5, "Y": 0.01, "Z": 0.01}, "Min": {"X": -1.5, "Y": -0.01, "Z": -0.01}},
     "Particles": [{"SystemId": "Telekinesie_Main_Aura", "TargetNodeName": "Paume", "Scale": 1.0}],
     "Trails": [{"TargetNodeName": f"Doigt{i}_Bout", "TrailId": "Telekinesie_Trainee", "PositionOffset": {"X": 0, "Y": 0, "Z": -0.6}} for i in range(4)]
             + [{"TargetNodeName": "Pouce_Bout", "TrailId": "Telekinesie_Trainee", "PositionOffset": {"X": 0, "Y": 0, "Z": -0.5}}],
     "Light": {"Color": "#9b4dff", "Radius": 8}}
json.dump(M, open(os.path.join(RES, "Server/Models/Projectiles/Abilities/Telekinesie_Main.json"), "w"), indent=2)
cfg = {"$Comment": "Porte la main spectrale de la Telekinesie. Interactions neutres obligatoires.", "Model": "Telekinesie_Main", "UseModelScale": True,
       "Physics": {"Type": "Standard", "Gravity": 0, "TerminalVelocityAir": 1, "TerminalVelocityWater": 1, "Bounciness": 0, "SticksVertically": False},
       "LaunchForce": 0, "SpawnOffset": {"X": 0, "Y": 0, "Z": 0},
       "Interactions": {"ProjectileHit": {"Interactions": [{"Type": "Simple", "RunTime": 0.01}]}, "ProjectileMiss": {"Interactions": [{"Type": "Simple", "RunTime": 0.01}]}}}
json.dump(cfg, open(os.path.join(RES, "Server/ProjectileConfigs/Abilities/Projectile_Config_Telekinesie_Main.json"), "w"), indent=2)
print("main spectrale generee")
