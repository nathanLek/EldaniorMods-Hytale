"""Cyclone de Fujin (Oeil du Cyclone) : colonne de 100 blocs faite de trails sur des anneaux de noeuds qui tournent.

Meme principe que la tornade vanilla (Tornado.blockymodel n'a aucune geometrie : tout son rendu vient de trails et de
particules sur des noeuds animes). Chaque anneau tourne a sa propre vitesse ; les noeuds alternent entre le bord interieur
(oeil, 3.5 blocs) et le bord exterieur (8.5 blocs) du mur de vent.

Usage : python3 gen_fujin_cyclone.py <src/main/resources>
"""
import json, math, os, sys

RES = sys.argv[1]
U = 64                      # unites par bloc (modeles d'entite)
HEIGHT, STEP = 100, 1       # colonne de 100 blocs, un anneau a chaque bloc (sinon la tornade parait coupee)
R_IN, R_OUT = 4.4, 7.6      # rayons des noeuds (le trail les elargit jusqu'a ~3.5 / ~8.5)
SPEEDS = (7, 10, 14)        # tours par cycle de 7 s (1 ; 1.43 ; 2 tours/s)
DURATION = 420              # frames (60 / s)

IDENT = {"x": 0, "y": 0, "z": 0, "w": 1}
def node(nid, name, pos, children=()):
    return {"id": str(nid), "name": name, "children": list(children),
            "position": {"x": round(pos[0], 2), "y": round(pos[1], 2), "z": round(pos[2], 2)}, "orientation": IDENT,
            "shape": {"type": "none", "offset": {"x": 0, "y": 0, "z": 0}, "stretch": {"x": 1, "y": 1, "z": 1}, "settings": {"isPiece": False},
                      "visible": True, "doubleSided": False, "shadingMode": "flat", "unwrapMode": "custom", "textureLayout": {}}}

levels, anims, trails, particles = [], {}, [], []
nid = 1

def spin_frames(turns):
    steps = turns * 3; dt = DURATION // steps
    frames, prev = [], None
    for st in range(steps):
        th = math.radians(st * 120) / 2
        q = [0.0, math.sin(th), 0.0, math.cos(th)]
        if prev and sum(x * y for x, y in zip(q, prev)) < 0: q = [-x for x in q]
        prev = q
        frames.append({"time": st * dt, "delta": {"x": 0, "y": round(q[1], 6), "z": 0, "w": round(q[3], 6)}, "interpolationType": "linear"})
    return {"position": [], "orientation": frames, "shapeStretch": [], "shapeVisible": [], "shapeUvOffset": []}

def ring(lname, h, radii, count, phase, turns, trail_fn, particle_fn=None):
    """Anneau de noeuds a la hauteur h (blocs), tournant de 'turns' tours par cycle."""
    global nid
    kids = []
    for i in range(count):
        a = math.radians(i * 360 / count + phase)
        r = radii[i % len(radii)]
        name = f"{lname}_{i}"
        kids.append(node(nid, name, (math.cos(a) * r * U, 0, math.sin(a) * r * U))); nid += 1
        trails.extend(trail_fn(i, name))
        if particle_fn: particles.extend(particle_fn(i, name))
    levels.append(node(nid, lname, (0, h * U, 0), kids)); nid += 1
    anims[lname] = spin_frames(turns)

# Mur : un anneau par bloc de 0 a 100 ; evasement au-dessus de 40 blocs, rotation plus rapide en bas (par bandes, sans cassure)
for k in range(HEIGHT // STEP + 1):
    h = k * STEP
    widen = max(0.0, h - 40) * 0.08
    turns = 14 if h < 40 else 10 if h < 70 else 7
    def wall(i, name, h=h, k=k):
        # deux rubans croises a 90 degres par noeud : un ruban vu par la tranche disparait, l'autre reste face a la camera
        t = [{"TargetNodeName": name, "TrailId": "Fujin_Cyclone_Wall"}, {"TargetNodeName": name, "TrailId": "Fujin_Cyclone_Wall_Flat"}]
        if k % 3 == 0 and i % 2 == 0:
            t.append({"TargetNodeName": name, "TrailId": "Fujin_Cyclone_Streak", "PositionOffset": {"X": 0, "Y": 0.4, "Z": 0}})
            t.append({"TargetNodeName": name, "TrailId": "Fujin_Cyclone_Streak", "PositionOffset": {"X": 0, "Y": 0.4, "Z": 0}, "RotationOffset": {"Pitch": 0, "Roll": 90, "Yaw": 0}})
        if h <= 32 and k % 2 == 0 and i % 2 == 1:
            t.append({"TargetNodeName": name, "TrailId": "Fujin_Cyclone_Dust", "PositionOffset": {"X": 0, "Y": -0.4, "Z": 0}})
            t.append({"TargetNodeName": name, "TrailId": "Fujin_Cyclone_Dust", "PositionOffset": {"X": 0, "Y": -0.4, "Z": 0}, "RotationOffset": {"Pitch": 0, "Roll": 90, "Yaw": 0}})
        return t
    def dust(i, name, k=k):
        return [{"SystemId": "Fujin_Tornado_Node", "TargetNodeName": name, "Scale": 1.4}] if i == 1 and k % 9 == 0 else []
    ring(f"L{k}", h, (R_IN + widen, R_OUT + widen), 4, k * 37, turns, wall, dust)

# Jupe de poussiere au sol (terre arrachee qui tourbillonne autour du pied)
for k, h in enumerate((0.4, 1.6, 3.0)):
    ring(f"S{k}", h, (9.0, 10.5, 11.5), 6, k * 20, 10,
         lambda i, name: [{"TargetNodeName": name, "TrailId": "Fujin_Cyclone_Skirt"},
                          {"TargetNodeName": name, "TrailId": "Fujin_Cyclone_Skirt", "RotationOffset": {"Pitch": 0, "Roll": 90, "Yaw": 0}}]
                         + ([{"TargetNodeName": name, "TrailId": "Fujin_Cyclone_Dust"}] if i % 2 else []))
# Enclume : nuage sombre qui s'etale au sommet
for k, (h, r) in enumerate(((98, (11.0, 13.0)), (102, (14.0, 16.5)), (105, (17.0, 19.0)))):
    ring(f"C{k}", h, r, 8, k * 15, 7, lambda i, name: [{"TargetNodeName": name, "TrailId": "Fujin_Cyclone_Cloud"},
                                                        {"TargetNodeName": name, "TrailId": "Fujin_Cyclone_Cloud", "RotationOffset": {"Pitch": 0, "Roll": 90, "Yaw": 0}}])

# Plafond nuageux : larges bandes grises qui tournent lentement au-dessus de la zone (jamais au-dessus de l'oeil)
for k, (h, radii) in enumerate(((32, (12.0, 15.0)), (34, (16.0, 19.0)), (36, (20.0, 23.0)), (38, (24.0, 27.0)), (40, (28.0, 31.0)), (42, (31.0, 34.0)),
                                (35, (14.0, 25.0)), (39, (22.0, 33.0)))):
    ring(f"N{k}", h, radii, 8, k * 23, 1 + (k % 2),
         lambda i, name, k=k: [{"TargetNodeName": name, "TrailId": "Fujin_Cyclone_Nuage"},
                               {"TargetNodeName": name, "TrailId": "Fujin_Cyclone_Nuage", "RotationOffset": {"Pitch": 0, "Roll": 90, "Yaw": 0}},
                               {"TargetNodeName": name, "TrailId": "Fujin_Cyclone_Nuage_Sombre", "PositionOffset": {"X": 0, "Y": -1.5, "Z": 0}}])

D = os.path.join(RES, "Common/Items/Projectiles/Fujin"); os.makedirs(D, exist_ok=True)
json.dump({"nodes": [node(0, "Base", (0, 0, 0), levels)], "lod": "auto"}, open(os.path.join(D, "Cyclone.blockymodel"), "w"), indent=1)
json.dump({"formatVersion": 1, "duration": DURATION, "holdLastKeyframe": False, "nodeAnimations": anims},
          open(os.path.join(D, "Cyclone_Spin.blockyanim"), "w"), indent=1)
particles.append({"SystemId": "Fujin_Vortex_Base", "TargetNodeName": "Base", "Scale": 1.2})
model = {"$Comment": "Mur du cyclone du Courroux de Fujin (genere par scripts/rune_aura_icons/gen_fujin_cyclone.py) : colonne de 100 blocs, oeil de 7 blocs, mur de 5 blocs. Trails sur des anneaux de noeuds qui tournent. HitBox large en X : le serveur masque les entites a hitbox minuscule.",
         "Model": "Items/Projectiles/Fujin/Cyclone.blockymodel", "Texture": "Items/Projectiles/Tornado_Texture.png", "MinScale": 1, "MaxScale": 1,
         "HitBox": {"Max": {"X": 36.0, "Y": 0.01, "Z": 0.01}, "Min": {"X": -36.0, "Y": -0.01, "Z": -0.01}},
         "AnimationSets": {"Idle": {"Animations": [{"Animation": "Items/Projectiles/Fujin/Cyclone_Spin.blockyanim", "BlendingDuration": 0, "SoundEventId": "SFX_Tornado", "Speed": 1}]}},
         "Particles": particles, "Trails": trails}
json.dump(model, open(os.path.join(RES, "Server/Models/Projectiles/Abilities/Fujin_Cyclone.json"), "w"), indent=2)
print(f"{len(levels)} anneaux, {len(trails)} trails, {len(particles)} systemes de particules")
