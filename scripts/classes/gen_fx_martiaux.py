"""Particules des competences de la voie martiale et des bornes de territoire (retours de test du 2026-11-10).

Toutes courtes (une emission = une bouffee qui s'eteint seule) : le code Java les reemet a la cadence voulue, souvent pour
un seul joueur (SpellFx.fxFor). Couleur par palier = couleur du Ki (gen_voie_martiale.KI ; Dieu Martial : or blanc).

  Ki_Orbe_T<n>        orbe de Ki (halo + etincelles) : orbites de la meditation, veines du renforcement
  Ki_Anneau_T<n>      anneau plat qui s'ouvre au sol : souffle de la meditation, onde du renforcement
  Ki_Colonne_T<n>     colonne d'etincelles qui montent : meditation du Saint et au-dela, eveil du renforcement
  Ki_Lame_T<n>        trainee lumineuse etiree le long d'une lame (Ki de l'Epee : seule l'epee brille)
  Sens_Silhouette_Tete / _Corps / _Jambes   silhouette rouge du Sens martial (projetee devant les murs, vue du seul lanceur)
  Domaine_Pression    etincelles d'or qui tombent (poids du ciel du Domaine Divin)
  Domaine_Voile       grand halo d'or au sol, le dome du Domaine Divin
  Borne_Colonne_<T> / Borne_Bord_<T>       bornes de territoire : colonne et piquets de bord, couleur du type de zone

Usage : python3 scripts/classes/gen_fx_martiaux.py [src/main/resources]
"""
import json, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import gen_voie_martiale as V  # noqa: E402

RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "..", "..", "src", "main", "resources")
OUT = os.path.join(RES, "Server", "Particles", "Eldanior", "Martial")
TIER_COLOR = {n: ("#fff3c8" if n == 8 else c) for n, (c, _, _) in V.KI.items()}
BORNE_COLOR = {"KINGDOM": ("#ffd75a", "#ff9a1a"), "GRAND_TERRITORY": ("#c08cff", "#7a3cff"),
               "TERRITORY": ("#7fd0ff", "#2a7cff"), "CITY": ("#8fffa8", "#22c45a"),
               # 2026-11-19 : zones larges posees aussi par borne
               "FOREST": ("#4fd16a", "#14702a"), "ARENA": ("#ff7a6a", "#d02020"), "PLOT": ("#fff6dc", "#c8b58a")}


def rng(a, b=None):
    return {"Min": a, "Max": a if b is None else b}


def spawner(texture, color, color_end, life, scale, count, opacity=1.0, scale_y=None, end_scale=None, ratio="OneToOne",
            rotation="Billboard", speed=None, pitch=(80, 90), offset=None, burst=True, opacity_end=0.0):
    init = {"Opacity": opacity, "Color": color, "Scale": {"X": rng(scale[0], scale[1])}}
    if scale_y is not None:
        init["Scale"]["Y"] = rng(scale_y[0], scale_y[1])
    end = {"Opacity": opacity_end, "Color": color_end}
    if end_scale is not None:
        end["Scale"] = {"X": rng(end_scale[0], end_scale[1])}
        if scale_y is not None:
            end["Scale"]["Y"] = rng(end_scale[0] * scale_y[0] / max(scale[0], 1e-6), end_scale[1] * scale_y[1] / max(scale[1], 1e-6))
    sp = {"RenderMode": "BlendAdd", "ParticleRotationInfluence": rotation, "LinearFiltering": True, "LightInfluence": 0,
          "ParticleRotateWithSpawner": False, "SpawnRate": rng(count), "SpawnBurst": burst, "MaxConcurrentParticles": count,
          "TotalParticles": rng(count), "ParticleLifeSpan": rng(life[0], life[1]),
          "Particle": {"Texture": texture, "ScaleRatioConstraint": ratio, "UVOption": "None", "SoftParticles": "Disable",
                       "InitialAnimationFrame": init, "Animation": {"0": dict(init), "100": end}}}
    if offset:
        sp["EmitOffset"] = {k: rng(-v, v) if k != "Y" else rng(0, v) for k, v in offset.items()}
    if speed:
        sp["InitialVelocity"] = {"Speed": rng(speed[0], speed[1]), "Yaw": rng(-180, 180), "Pitch": rng(pitch[0], pitch[1])}
    return sp


def flat_ring(color, color_end, life, start, end):
    sp = spawner("Particles/Textures/Circles/Explosion_Shockwave.png", color, color_end, (life, life), (start, start), 1,
                 scale_y=(0, 0), end_scale=(end, end), rotation="None")
    for frame in (sp["Particle"]["InitialAnimationFrame"], sp["Particle"]["Animation"]["0"]):
        frame["Rotation"] = {"X": rng(90), "Z": rng(0, 360)}
        frame["Scale"]["Y"] = rng(1)
    sp["Particle"]["Animation"]["100"]["Scale"]["Y"] = rng(1)
    return sp


def system(name, parts, comment, cull=64):
    spawners = []
    for i, (sp, dy) in enumerate(parts):
        sid = "%s_%d" % (name, i)
        write(os.path.join(OUT, "Spawners", sid + ".particlespawner"), sp)
        spawners.append({"SpawnerId": sid, "PositionOffset": {"Y": dy}})
    write(os.path.join(OUT, name + ".particlesystem"), {"$Comment": comment, "Spawners": spawners, "IsImportant": True,
                                                         "CullDistance": cull, "LifeSpan": 0.1})


def write(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")


def main():
    glow, spark = "Particles/Textures/Basic/Glow.png", "Particles/Textures/Basic/Spark.png"
    for n, c in TIER_COLOR.items():
        system("Ki_Orbe_T%d" % n, [(spawner(glow, c, c, (0.5, 0.5), (0.45, 0.45), 1, opacity=0.9, end_scale=(0.15, 0.15)), 0),
                                   (spawner(spark, "#ffffff", c, (0.4, 0.6), (0.12, 0.18), 3, speed=(0.2, 0.5), pitch=(-90, 90)), 0)],
               "Orbe de Ki, palier %d (gen_fx_martiaux.py)." % n)
        system("Ki_Anneau_T%d" % n, [(flat_ring(c, c, 1.0, 0.6, 2.6), 0.08),
                                     (spawner(spark, "#ffffff", c, (0.6, 0.9), (0.15, 0.22), 10, speed=(1.5, 2.5), pitch=(5, 20)), 0.1)],
               "Anneau de Ki au sol, palier %d (gen_fx_martiaux.py)." % n)
        system("Ki_Colonne_T%d" % n, [(spawner(spark, "#ffffff", c, (1.0, 1.6), (0.18, 0.28), 16, speed=(2.0, 3.5), pitch=(82, 90),
                                               offset={"X": 0.5, "Y": 0.2, "Z": 0.5}), 0),
                                      (spawner(glow, c, c, (0.9, 0.9), (1.2, 1.2), 1, opacity=0.35, scale_y=(3.0, 3.0), ratio="None",
                                               end_scale=(1.6, 1.6)), 1.4)],
               "Colonne de Ki, palier %d (gen_fx_martiaux.py)." % n)
        system("Ki_Lame_T%d" % n, [(spawner(glow, c, c, (0.25, 0.25), (0.22, 0.22), 1, opacity=0.95, end_scale=(0.05, 0.05)), 0),
                                   (spawner(spark, "#ffffff", c, (0.25, 0.4), (0.06, 0.1), 2, speed=(0.1, 0.3), pitch=(-90, 90)), 0)],
               "Lueur de Ki le long de la lame, palier %d (Ki de l'Epee, gen_fx_martiaux.py)." % n, cull=48)
    # Epee de Ki holographique (Projection d'epee d'Aura, retour du 2026-11-10) : lame faite de lumiere et de fumee, couleur de
    # l'aura. Orientee par la vitesse (BillboardVelocity : la lame s'etire dans le sens de l'emission, axe +Z du systeme) ;
    # reemise par le code toutes les 70 ms au milieu de la lame, avec lacet / tangage de sa direction.
    smoke = "Particles/Textures/Rnd/SmokeRound.png"
    for n, c in TIER_COLOR.items():
        blade = spawner(glow, c, c, (0.16, 0.16), (0.22, 0.22), 1, opacity=0.85, scale_y=(1.9, 1.9), ratio="None",
                        rotation="BillboardVelocity", speed=(0.02, 0.02), pitch=(0, 0), opacity_end=0.4)
        blade["InitialVelocity"]["Yaw"] = rng(0)
        core = spawner(glow, "#ffffff", c, (0.16, 0.16), (0.08, 0.08), 1, opacity=0.95, scale_y=(1.7, 1.7), ratio="None",
                       rotation="BillboardVelocity", speed=(0.02, 0.02), pitch=(0, 0), opacity_end=0.5)
        core["InitialVelocity"]["Yaw"] = rng(0)
        fumes = spawner(smoke, c, "#202028", (0.6, 0.9), (0.25, 0.4), 1, opacity=0.28, speed=(0.2, 0.4), pitch=(-30, 60),
                        end_scale=(0.6, 0.8), offset={"X": 0.15, "Y": 0.1, "Z": 0.6})
        glint = spawner(spark, "#ffffff", c, (0.2, 0.35), (0.06, 0.1), 2, speed=(0.1, 0.3), pitch=(-90, 90),
                        offset={"X": 0.1, "Y": 0.1, "Z": 0.8})
        system("Ki_Epee_Holo_T%d" % n, [(blade, 0), (core, 0), (fumes, 0), (glint, 0)],
               "Epee de Ki holographique, palier %d : lame de lumiere et fumee (gen_fx_martiaux.py)." % n, cull=96)
        system("Ki_Epee_Garde_T%d" % n, [(spawner(glow, c, c, (0.16, 0.16), (0.3, 0.3), 1, opacity=0.9, opacity_end=0.4), 0)],
               "Garde de l'epee de Ki, palier %d (gen_fx_martiaux.py)." % n, cull=96)
    # Fumee continue de l'epee 3D de Ki (Projection d'epee d'Aura, retour du 2026-11-10 soir), attachee a la lame du modele
    # Ki_Epee_Proj_T<n> (gen_voie_martiale.py) : volutes de la couleur du Ki qui s'etirent derriere l'epee, quelques eclats.
    for n, c in TIER_COLOR.items():
        fume = spawner(smoke, c, "#1c1c24", (0.7, 1.0), (0.3, 0.45), 6, opacity=0.35, speed=(0.1, 0.3), pitch=(-60, 60),
                       end_scale=(0.8, 1.1), offset={"X": 0.7, "Y": 0.12, "Z": 0.12}, burst=False)
        fume["SpawnRate"] = rng(14, 18)
        fume["MaxConcurrentParticles"] = 24
        del fume["TotalParticles"]
        fume["RenderMode"] = "BlendLinear"
        sparkle = spawner(spark, "#ffffff", c, (0.3, 0.5), (0.06, 0.1), 2, speed=(0.2, 0.6), pitch=(-90, 90),
                          offset={"X": 0.7, "Y": 0.1, "Z": 0.1}, burst=False)
        sparkle["SpawnRate"] = rng(8, 10)
        sparkle["MaxConcurrentParticles"] = 10
        del sparkle["TotalParticles"]
        path = os.path.join(OUT, "Ki_Epee_Fumee_T%d.particlesystem" % n)
        spawners = []
        for i, sp in enumerate((fume, sparkle)):
            sid = "Ki_Epee_Fumee_T%d_%d" % (n, i)
            write(os.path.join(OUT, "Spawners", sid + ".particlespawner"), sp)
            spawners.append({"SpawnerId": sid})
        write(path, {"$Comment": "Fumee continue de l'epee 3D de Ki, palier %d, attachee au modele (gen_fx_martiaux.py)." % n,
                     "Spawners": spawners, "IsImportant": True, "CullDistance": 96, "BoundingRadius": 6})
    # Sillage du Pas leger (retour du 2026-11-10 soir : « ameliore les trainees du dash ») : attache aux hanches par l'effet
    # Pas_Leger_Sillage_<n> (gen_voie_martiale.py) pendant la ruee. Les particules ne suivent pas le joueur
    # (TrailSpawnerPositionMultiplier 0) : elles restent dans l'air et dessinent le sillage. Traits de lumiere etires par la
    # vitesse sur toute la hauteur du corps, volutes de fumee de Ki, eclats blancs ; plus dense aux classes superieures.
    for n, c in TIER_COLOR.items():
        if n < 3:
            continue
        k = 0.6 + 0.1 * n
        streak = {"RenderMode": "BlendAdd", "ParticleRotationInfluence": "BillboardVelocity", "LinearFiltering": True,
                  "LightInfluence": 0, "ParticleRotateWithSpawner": False, "TrailSpawnerPositionMultiplier": 0.0,
                  "TrailSpawnerRotationMultiplier": 0.0, "MaxConcurrentParticles": 120, "SpawnRate": rng(60 * k, 80 * k),
                  "ParticleLifeSpan": rng(0.25, 0.45), "VelocityStretchMultiplier": 60.0,
                  "EmitOffset": {"X": rng(-0.35, 0.35), "Y": rng(-0.9, 0.8), "Z": rng(-0.35, 0.35)},
                  "InitialVelocity": {"Speed": rng(0.05, 0.2), "Yaw": rng(-180, 180), "Pitch": rng(-10, 10)},
                  "Particle": {"Texture": glow, "ScaleRatioConstraint": "None", "UVOption": "None", "SoftParticles": "Disable",
                               "InitialAnimationFrame": {"Opacity": 0.9, "Color": "#ffffff", "Scale": {"X": rng(0.05, 0.09), "Y": rng(0.5, 0.9)}},
                               "Animation": {"0": {"Opacity": 0.9, "Color": "#ffffff", "Scale": {"X": rng(0.05, 0.09), "Y": rng(0.5, 0.9)}},
                                             "30": {"Color": c},
                                             "100": {"Opacity": 0.0, "Color": c, "Scale": {"X": rng(0.01, 0.02), "Y": rng(0.2, 0.3)}}}}}
        veil = {"RenderMode": "BlendAdd", "ParticleRotationInfluence": "Billboard", "LinearFiltering": True, "LightInfluence": 0,
                "ParticleRotateWithSpawner": False, "TrailSpawnerPositionMultiplier": 0.0, "TrailSpawnerRotationMultiplier": 0.0,
                "MaxConcurrentParticles": 40, "SpawnRate": rng(30 * k, 40 * k), "ParticleLifeSpan": rng(0.35, 0.55),
                "EmitOffset": {"X": rng(-0.2, 0.2), "Y": rng(-0.8, 0.7), "Z": rng(-0.2, 0.2)},
                "Particle": {"Texture": glow, "ScaleRatioConstraint": "None", "UVOption": "None", "SoftParticles": "Disable",
                             "InitialAnimationFrame": {"Opacity": 0.45, "Color": c, "Scale": {"X": rng(0.5, 0.7), "Y": rng(0.9, 1.2)}},
                             "Animation": {"0": {"Opacity": 0.45, "Color": c, "Scale": {"X": rng(0.5, 0.7), "Y": rng(0.9, 1.2)}},
                                           "100": {"Opacity": 0.0, "Color": c, "Scale": {"X": rng(0.2, 0.3), "Y": rng(0.5, 0.7)}}}}}
        fumes = spawner(smoke, c, "#202028", (0.5, 0.8), (0.3, 0.45), 30, opacity=0.3, speed=(0.1, 0.4), pitch=(-20, 60),
                        end_scale=(0.7, 1.0), offset={"X": 0.3, "Y": 0.6, "Z": 0.3}, burst=False)
        fumes["SpawnRate"] = rng(18 * k, 24 * k)
        del fumes["TotalParticles"]
        fumes["RenderMode"] = "BlendLinear"
        fumes["TrailSpawnerPositionMultiplier"] = 0.0
        sparks = spawner(spark, "#ffffff", c, (0.3, 0.6), (0.06, 0.11), 30, speed=(0.5, 1.6), pitch=(-60, 60),
                         offset={"X": 0.3, "Y": 1.0, "Z": 0.3}, burst=False)
        sparks["SpawnRate"] = rng(20 * k, 30 * k)
        del sparks["TotalParticles"]
        sparks["TrailSpawnerPositionMultiplier"] = 0.0
        spawners = []
        for i, sp in enumerate((streak, veil, fumes, sparks)):
            sid = "Pas_Sillage_T%d_%d" % (n, i)
            write(os.path.join(OUT, "Spawners", sid + ".particlespawner"), sp)
            spawners.append({"SpawnerId": sid})
        write(os.path.join(OUT, "Pas_Sillage_T%d.particlesystem" % n),
              {"$Comment": "Sillage du Pas leger, palier %d : traits de lumiere, voile, fumee et eclats de Ki laisses dans l'air "
                           "(gen_fx_martiaux.py)." % n, "Spawners": spawners, "IsImportant": True, "CullDistance": 96,
               "BoundingRadius": 30})
    red = "#ff2a2a"
    system("Sens_Silhouette_Tete", [(spawner(glow, red, "#ff5050", (0.3, 0.3), (0.55, 0.55), 1, opacity=0.95, opacity_end=0.6), 0)],
           "Sens martial : tete de la silhouette rouge (vue du seul lanceur).", cull=200)
    system("Sens_Silhouette_Corps", [(spawner(glow, red, "#ff5050", (0.3, 0.3), (0.75, 0.75), 1, opacity=0.9, scale_y=(1.15, 1.15),
                                              ratio="None", opacity_end=0.55), 0)],
           "Sens martial : buste de la silhouette rouge.", cull=200)
    system("Sens_Silhouette_Jambes", [(spawner(glow, red, "#ff5050", (0.3, 0.3), (0.6, 0.6), 1, opacity=0.85, scale_y=(1.2, 1.2),
                                               ratio="None", opacity_end=0.5), 0)],
           "Sens martial : jambes de la silhouette rouge.", cull=200)
    gold = "#ffd75a"
    system("Domaine_Pression", [(spawner(spark, "#fff3c8", gold, (0.8, 1.1), (0.2, 0.3), 14, speed=(5, 8), pitch=(-90, -80),
                                         offset={"X": 1.2, "Y": 0.1, "Z": 1.2}), 4.0),
                                (flat_ring(gold, "#ff9a1a", 0.6, 0.3, 1.4), 0.08)],
           "Domaine Divin : poids du ciel qui ecrase un ennemi (gen_fx_martiaux.py).")
    system("Domaine_Voile", [(flat_ring("#fff3c8", gold, 1.6, 4.0, 15.0), 0.1),
                             (spawner(glow, gold, "#fff3c8", (1.4, 1.4), (6.0, 6.0), 1, opacity=0.25, scale_y=(10.0, 10.0),
                                      ratio="None", end_scale=(8.0, 8.0)), 5.0)],
           "Domaine Divin : voile d'or qui s'etend au sol et colonne de lumiere (gen_fx_martiaux.py).", cull=96)
    for t, (c, c2) in BORNE_COLOR.items():
        system("Borne_Colonne_" + t, [(spawner(spark, "#ffffff", c, (1.2, 1.8), (0.25, 0.35), 18, speed=(3, 5), pitch=(84, 90),
                                               offset={"X": 0.15, "Y": 0.1, "Z": 0.15}), 0),
                                      (flat_ring(c, c2, 1.1, 0.5, 1.8), 0.1),
                                      (spawner(glow, c, c2, (1.0, 1.0), (0.8, 0.8), 1, opacity=0.4, scale_y=(6.0, 6.0), ratio="None"), 3.0)],
               "Borne de territoire (%s) : colonne lumineuse, vue des admins tenant une borne." % t, cull=96)
        system("Borne_Bord_" + t, [(spawner(glow, c, c2, (1.05, 1.05), (0.3, 0.3), 1, opacity=0.8, scale_y=(1.6, 1.6), ratio="None"), 0.8),
                                   (spawner(spark, "#ffffff", c, (0.8, 1.0), (0.12, 0.16), 2, speed=(0.6, 1.0), pitch=(80, 90)), 0.1)],
               "Bord de zone (%s) : piquet lumineux, vu des admins tenant une borne." % t, cull=64)
    print("Particules martiales et bornes : ecrites dans", OUT)


if __name__ == "__main__":
    main()
