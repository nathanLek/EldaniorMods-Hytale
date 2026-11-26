"""Gestes du Clan Luo : jeu d'animations Luo_Gestes (Server/Item/Animations/Luo_Gestes.json), joue par le code sur les PNJ Luo
et sur le joueur (/incliner) via AnimationUtils.playAnimation(ref, Action, "Luo_Gestes", <geste>).

  Inclinaison          ojigi : buste incline a 35 degres, bras le long du corps, tenu une seconde (Seigneur, Doyens, /incliner)
  InclinaisonProfonde  saikeirei : buste plie a 70 degres depuis les hanches, tenu deux secondes (devant le Patriarche)
  Priere               gassho des pelerins : mains jointes devant la poitrine, tete baissee
  Salut                salut des gardes (emote vanilla Salute_Chest)
  Appel                appel des marchands (emote vanilla Wave_Signal)
  Kata1..Kata4         kata des disciples : coups d'epee vanilla, enchaines par le code (LuoNpcs)
  Forger, Forger2      forgerons legendaires a l'etabli : frappe de masse, geste de minage (SmithBehavior)
  Assis                forgerons legendaires sur un tabouret ou une chaise (Sit vanilla, en boucle)
  Meditation           voie martiale (Rune de Meditation martiale) : assis en tailleur au sol, mains jointes devant le
                       ventre, respiration lente ; en boucle jusqu'a ce que le code l'arrete

Un jeu d'animations d'objet exige WiggleWeights (et Camera) : sans eux, l'asset est refuse et tout le mod avec (2026-11-08).

Les poses sont des rotations d'os du squelette du joueur (pose_kit), controlees sur une planche de rendu.
Temps en images (60 par seconde, comme les animations vanilla).

Usage : python3 scripts/npcs/gen_luo_anims.py <src/main/resources> [dossier_apercu]
"""
import json, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "artefacts"))
import pose_kit as P  # noqa: E402

RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "..", "..", "src", "main", "resources")
PREVIEW = sys.argv[2] if len(sys.argv) > 2 else None
DST = "Characters/Animations/Luo/"
SET = "Luo_Gestes"
E = P.euler
Z3 = (0.0, 0.0, 0.0)

# Poses : os -> rotation (ou (deplacement, rotation))
BOW = {"Belly": E(14), "Chest": E(14), "Head": E(10), "R-Arm": E(-12, 0, 4), "L-Arm": E(-12, 0, -4)}
DEEP_BOW = {"Pelvis": ((0.0, 0.0, -3.0), E(34)), "R-Thigh": E(-34), "L-Thigh": E(-34), "Belly": E(18), "Chest": E(16),
            "Head": E(8), "R-Arm": E(-30, 0, 2), "L-Arm": E(-30, 0, -2), "R-Forearm": E(-8), "L-Forearm": E(-8)}
PRAY = {"Head": E(18), "Chest": E(4),                         # mains jointes devant la poitrine (placees par calcul)
        "R-Arm": E(-24, 0, 5), "R-Forearm": E(-106, 50, 0),
        "L-Arm": E(-24, 0, -5), "L-Forearm": E(-106, -50, 0)}
# Meditation : bassin pose au sol, cuisses relevees et ouvertes, mollets replies et croises devant, mains devant le ventre
LOTUS = {"Pelvis": ((0.0, -40.0, 0.0), E(0)),
         "R-Thigh": E(-80, -40, 0), "L-Thigh": E(-80, 40, 0), "R-Calf": E(130), "L-Calf": E(130),
         "Chest": E(-4), "Head": E(6),
         "R-Arm": E(-8, 0, 16), "L-Arm": E(-8, 0, -16), "R-Forearm": E(-40, 0, -10), "L-Forearm": E(-40, 0, 10)}
LOTUS_IN = dict(LOTUS, Chest=E(-7), Head=E(3))                 # inspiration : buste qui se redresse
# Meditation eveillee (Maitre Martial et plus, retour du 2026-11-10) : mains jointes en gassho devant la poitrine, respiration
# ample (le buste s'ouvre et le menton se leve a l'inspiration), leger balancement
MUDRA = dict(LOTUS, **{"R-Arm": E(-24, 0, 5), "R-Forearm": E(-106, 50, 0), "L-Arm": E(-24, 0, -5),
                       "L-Forearm": E(-106, -50, 0), "Head": E(10)})
MUDRA_IN = dict(MUDRA, Chest=E(-10), Belly=E(-4), Head=E(-4))
MUDRA_SWAY = dict(MUDRA, Chest=E(-6, 0, 3), Head=E(6, 0, 2))
# Renforcement corporel : il se ramasse (genoux flechis, poings aux hanches, tete baissee) puis libere sa force (bras ecartes
# et flechis, jambes ancrees, buste ouvert, tete en arriere) ; signes verifies sur planche (pose_kit, vues face/dos/profil)
POWER_LOAD = {"Pelvis": ((0.0, -5.0, 0.0), E(0)), "R-Thigh": E(-22, 0, -8), "L-Thigh": E(-22, 0, 8), "R-Calf": E(36), "L-Calf": E(36),
              "Belly": E(10), "Chest": E(12), "Head": E(14),
              "R-Arm": E(20, 0, -12), "L-Arm": E(20, 0, 12), "R-Forearm": E(-110), "L-Forearm": E(-110)}
POWER_BURST = {"Pelvis": ((0.0, -8.0, 0.0), E(0)), "R-Thigh": E(-24, 0, -24), "L-Thigh": E(-24, 0, 24), "R-Calf": E(38), "L-Calf": E(38),
               "Belly": E(-6), "Chest": E(-12), "Head": E(-14),
               "R-Arm": E(0, 0, -70), "L-Arm": E(0, 0, 70), "R-Forearm": E(-100), "L-Forearm": E(-100)}


def deltas(pose):
    return {k: (v if isinstance(v, tuple) else (Z3, v)) for k, v in pose.items()}


def anim(keys, duration, loop_hold=False):
    """keys : [(image, pose)] ; un os absent d'une pose y est au repos."""
    bones = sorted({b for _, pose in keys for b in pose})
    nodes = {}
    for b in bones:
        q_keys, p_keys = [], []
        for t, pose in keys:
            dp, dq = deltas(pose).get(b, (Z3, P.ID_Q))
            q_keys.append({"time": t, "delta": {k: round(dq[k], 6) for k in "xyzw"}, "interpolationType": "smooth"})
            p_keys.append({"time": t, "delta": {"x": dp[0], "y": dp[1], "z": dp[2]}, "interpolationType": "smooth"})
        nodes[b] = {"position": p_keys, "orientation": q_keys, "shapeStretch": [], "shapeVisible": [], "shapeUvOffset": []}
    return {"formatVersion": 1, "duration": duration, "holdLastKeyframe": loop_hold, "nodeAnimations": nodes}


def eyes_closed(a):
    """Yeux fermes (meditation, retour du 2026-11-10) : comme les expressions du jeu (Cheerful etire la paupiere du bas),
    les paupieres du haut sont etirees vers le bas et les yeux ecrases en un trait, tenus toute l'animation."""
    d = a["duration"]

    def track(pos, stretch):
        k = lambda v: [{"time": t, "delta": dict(v), "interpolationType": "smooth"} for t in (0, d)]
        return {"position": k(pos) if pos else [], "orientation": [], "shapeStretch": k(stretch), "shapeVisible": [], "shapeUvOffset": []}
    for side in ("R", "L"):
        a["nodeAnimations"][side + "-Eyelid-Top"] = track({"x": 0, "y": -1, "z": 0}, {"x": 1, "y": 10, "z": 1})
        a["nodeAnimations"][side + "-Eye"] = track(None, {"x": 1, "y": 0.15, "z": 1})
    return a


REST = {}
ANIMS = {
    "Inclinaison": anim([(0, REST), (16, BOW), (66, BOW), (88, REST)], 92),
    "InclinaisonProfonde": anim([(0, REST), (28, DEEP_BOW), (128, DEEP_BOW), (160, REST)], 165),
    "Priere": anim([(0, REST), (18, PRAY), (200, PRAY), (222, REST)], 226),
    "Meditation": eyes_closed(anim([(0, LOTUS), (120, LOTUS_IN), (240, LOTUS)], 240)),
    "MeditationEveil": eyes_closed(anim([(0, MUDRA), (90, MUDRA_IN), (180, MUDRA), (240, MUDRA_SWAY), (300, MUDRA)], 300)),
    "Renforcement": anim([(0, REST), (14, POWER_LOAD), (32, POWER_LOAD), (40, POWER_BURST), (84, POWER_BURST), (104, REST)], 108),
}
LOOPING_CUSTOM = {"Meditation", "MeditationEveil"}          # en boucle (le passage debout -> assis se fait par le fondu BlendingDuration)
VANILLA = {
    "Salut": "Characters/Animations/Emote/Salute_Chest.blockyanim",
    "Appel": "Characters/Animations/Emote/Wave_Signal.blockyanim",
    "Kata1": "Characters/Animations/Items/Main_Handed/Sword/Attacks/Swing_Left/Swing_Left.blockyanim",
    "Kata2": "Characters/Animations/Items/Main_Handed/Sword/Attacks/Swing_Right/Swing_Right.blockyanim",
    "Kata3": "Characters/Animations/Items/Main_Handed/Sword/Attacks/Swing_Down/Swing_Down.blockyanim",
    "Kata4": "Characters/Animations/Items/Main_Handed/Sword/Attacks/Swing_Down_Strong/Swing_Down_Strong.blockyanim",
    "Forger": "Characters/Animations/Items/Main_Handed/Club/Attacks/Swing_Down/Swing_Down.blockyanim",
    "Forger2": "Characters/Animations/Items/Main_Handed/Pickaxe/Attacks/Mine/Mine.blockyanim",
    "Assis": "Characters/Animations/Flavor/Sit.blockyanim",
}
LOOPING = {"Assis"}                      # tenu jusqu'a ce que le code l'arrete (forgerons legendaires assis sur un tabouret)


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


def main():
    names = P._z().namelist()
    entries = {}
    for name, a in ANIMS.items():
        rel = DST + name + ".blockyanim"
        write_json(os.path.join(RES, "Common", rel), a)
        entries[name] = {"ThirdPerson": rel, "FirstPerson": rel, "Speed": 1.0, "Looping": name in LOOPING_CUSTOM,
                         "BlendingDuration": 0.5 if name in LOOPING_CUSTOM else 0.25}
    for name, rel in VANILLA.items():
        if "Common/" + rel not in names:
            raise SystemExit("Animation vanilla introuvable : " + rel)
        entries[name] = {"ThirdPerson": rel, "FirstPerson": rel, "Speed": 1.0, "Looping": name in LOOPING, "BlendingDuration": 0.2}
    write_json(os.path.join(RES, "Server", "Item", "Animations", SET + ".json"), {
        "Animations": entries,
        "Camera": {"Pitch": {"TargetNodes": ["Head"]}, "Yaw": {"TargetNodes": ["Belly"]}},
        "WiggleWeights": {"Pitch": 2.5, "PitchDeceleration": 0.1, "Roll": 0.1, "RollDeceleration": 0.1, "X": 2.5,
                          "XDeceleration": 0.1, "Y": 0.1, "YDeceleration": 0.1, "Z": 0.1, "ZDeceleration": 0.1},
    })
    print("Luo_Gestes : %d gestes." % len(entries))
    if PREVIEW:
        os.makedirs(PREVIEW, exist_ok=True)
        imgs = []
        for pose in (BOW, DEEP_BOW, PRAY, LOTUS):
            for yaw in (90, 20):
                imgs.append(P.render(deltas(pose), size=240, yaw=yaw, pitch=8))
        import png  # noqa: E402  (scripts/rune_aura_icons, ajoute au chemin par armor_kit)
        png.write(os.path.join(PREVIEW, "luo_gestes.png"), 240 * 2, 240 * 4, P.sheet(imgs, 2))


if __name__ == "__main__":
    main()
