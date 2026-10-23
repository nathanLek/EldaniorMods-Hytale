"""Roles PNJ des clones du sort Clonage (rune Lumiere, Epic).

Deux variantes, choisies par le sort selon l'arme du lanceur :
  - Eldanior_Clone_Lumiere       : epee -> combat de Test_Combat_Knight (evaluateur CAE : vrais combos d'epee du joueur,
                                   coup charge, signature, pas de cote), degats x CLONE_DAMAGE ;
  - Eldanior_Clone_Lumiere_Melee : autre arme ou mains nues -> attaque de melee des PNJ (Root_NPC_Attack_Melee).
Au repos le clone suit le lanceur : il rejoint son point de laisse, que le sort place a sa place en formation. Il n'attaque que ce qu'il voit comme hostile :
le sort rend hostile la cible du clic droit puis, tant que le clone est lance, les monstres proches (autonomie).

Usage : python3 gen_clone_roles.py <src/main/resources> <dossier Server/NPC vanilla>
"""
import copy, json, os, sys

RES, NPC = sys.argv[1], sys.argv[2]
CLONE_DAMAGE = 2.0          # base vanilla du chevalier : 9 (coup) / 12 (charge)
MELEE_DAMAGE = 18
DETECT_RANGE = 30


def load(rel):
    with open(os.path.join(NPC, rel), encoding="utf-8") as f:
        return json.load(f)


def write(rel, data):
    path = os.path.join(RES, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")
    print("ecrit", rel)


def scale_damage(node, mult):
    if isinstance(node, dict):
        for k, v in node.items():
            if k == "BaseDamage" and isinstance(v, dict):
                for t in v:
                    v[t] = round(v[t] * mult)
            else:
                scale_damage(v, mult)
    elif isinstance(node, list):
        for v in node:
            scale_damage(v, mult)


def hostile_targets(lock):
    return {"Type": "Mob", "Range": DETECT_RANGE, "GetPlayers": True, "LockOnTarget": lock,
            "Filters": [{"Type": "Attitude", "Attitudes": ["Hostile"]}], "Collector": {"Type": "CombatTargets"}}


LIFE = {
    "$Comment": "Securite : le sort retire les clones apres 20 s ; s'il n'a pas pu (arret du serveur), ils disparaissent seuls.",
    "Continue": True,
    "Instructions": [
        {"Continue": True, "Sensor": {"Type": "Any", "Once": True},
         "Actions": [{"Type": "TimerStart", "Name": "Life", "StartValueRange": [45, 45], "RestartValueRange": [45, 45]}]},
        {"Sensor": {"Type": "Timer", "Name": "Life", "State": "Stopped"}, "Actions": [{"Type": "Despawn"}]},
    ],
}

IDLE = {
    "$Comment": "Au repos : suit le lanceur (point de laisse) ; un ennemi hostile (cible du clic droit) declenche le combat.",
    "Sensor": {"Type": "State", "State": "Idle"},
    "Instructions": [
        {"Sensor": hostile_targets(True), "Actions": [{"Type": "State", "State": "Combat"}]},
        {"$Comment": "Suit le lanceur : rejoint son point de laisse (sa place en formation, deplacee par le sort), en contournant les obstacles.",
         "Sensor": {"Type": "Leash", "Range": 1.5},
         "BodyMotion": {"Type": "Seek", "SlowDownDistance": 3, "StopDistance": 0.8, "RelativeSpeed": 0.9, "UsePathfinder": True}},
    ],
}

MELEE_COMBAT = {
    "Sensor": {"Type": "State", "State": "Combat"},
    "Instructions": [
        {"Sensor": {"Type": "Or", "Sensors": [hostile_targets(True), {"Type": "HasHostileTargetMemory"}]},
         "Instructions": [
             {"Sensor": {"Type": "Target", "Range": 2.5, "Filters": [{"Type": "LineOfSight"}]},
              "HeadMotion": {"Type": "Aim", "RelativeTurnSpeed": 1.5},
              "BodyMotion": {"Type": "MaintainDistance", "DesiredDistanceRange": [1.5, 2], "MoveThreshold": 0.2,
                             "RelativeForwardsSpeed": 0.5, "RelativeBackwardsSpeed": 0.4},
              "ActionsBlocking": True,
              "Actions": [{"Type": "Timeout", "Delay": [0.1, 0.2]},
                          {"Type": "Attack", "Attack": "Root_NPC_Attack_Melee", "AttackPauseRange": [0.4, 0.7]}]},
             {"Sensor": {"Type": "Target"},
              "HeadMotion": {"Type": "Aim", "RelativeTurnSpeed": 1.5},
              "BodyMotion": {"Type": "Seek", "SlowDownDistance": 2, "StopDistance": 1.5}},
         ]},
        {"Actions": [{"Type": "State", "State": "Idle"}]},
    ],
}


def role(name_key, combat, extra):
    r = {
        "Type": "Generic",
        "Parameters": {"MaxHealth": {"Value": 100, "Description": "Vie de base (le sort la remplace par 50 % de celle du lanceur)"}},
        "Appearance": "Player",
        "DefaultPlayerAttitude": "Ignore",
        "DefaultNPCAttitude": "Ignore",
        "HotbarItems": [],
        "HotbarSize": 3,
        "OffHandItems": [],
        "OffHandSlots": 1,
        "DefaultOffHandSlot": 0,
        "StartState": "Idle",
        "KnockbackScale": 0.5,
        "ApplySeparation": True,
        "SeparationDistance": 1.5,
        "SeparationWeight": 1,
        "BreathesInWater": True,
        "MotionControllerList": [{"Type": "Walk", "MaxWalkSpeed": 14, "RunThreshold": 0.3, "Gravity": 10,
                                  "MaxFallSpeed": 20, "MaxRotationSpeed": 360, "Acceleration": 12}],
        "MaxHealth": {"Compute": "MaxHealth"},
        "Instructions": [LIFE, {"Instructions": [IDLE, combat]}],
        "NameTranslationKey": f"server.npcRoles.{name_key}.name",
    }
    r.update(extra)
    return r


# Variante epee : combat du chevalier de test vanilla (etat Combat repris tel quel, detection elargie)
knight = load("Roles/_Core/Tests_Development/Test_Combat_Knight.json")
sword_combat = copy.deepcopy(knight["Instructions"][0]["Instructions"][1])
for s in sword_combat["Instructions"][0]["Sensor"]["Sensors"]:
    if s.get("Type") == "Mob":
        s["Range"] = DETECT_RANGE
        s["Filters"] = [{"Type": "Attitude", "Attitudes": ["Hostile"]}]
cae = load("Balancing/CAE_Test_Combat_Knight.json")
scale_damage(cae, CLONE_DAMAGE)
write("Server/NPC/Balancing/Eldanior/CAE_Eldanior_Clone.json", cae)
write("Server/NPC/Roles/Eldanior/Summons/Eldanior_Clone_Lumiere.json",
      role("Eldanior_Clone_Lumiere", sword_combat, {"CombatConfig": "CAE_Eldanior_Clone"}))
write("Server/NPC/Roles/Eldanior/Summons/Eldanior_Clone_Lumiere_Melee.json",
      role("Eldanior_Clone_Lumiere", MELEE_COMBAT, {"InteractionVars": {"Melee_Damage": {"Interactions": [
          {"Parent": "NPC_Attack_Melee_Damage", "DamageCalculator": {"BaseDamage": {"Physical": MELEE_DAMAGE}}}]}}}))
