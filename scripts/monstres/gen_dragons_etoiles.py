"""Dragons des monstres etoiles (boards.MonsterContracts, 2026-11-16) : roles combattants Eldanior_Dragon_Etoile_<Feu|Givre>.

Pourquoi : le jeu n'a aucun dragon qui se bat. Dragon_Fire et Dragon_Frost (Server/NPC/Roles/Boss) sont des figurants
(Template_Placeholder : Invulnerable, degats des joueurs desactives, marche au ralenti) ; Test_Dragon vole en rond sans combattre.
Ici : un predateur du jeu (Template_Predator, comme Yeti et Bear_Grizzly) avec le modele du dragon (Appearance Dragon_Fire /
Dragon_Frost, verifies dans Server/Models/Elemental). Le modele n'a pas d'animation d'attaque (Idle, Walk, Run, Fly, Hurt,
Death, Sleep...) : morsure SANS animation (Dragon_Etoile_Morsure), portee 6 blocs, souffle sonore. Il marche au sol.

Usage : python3 scripts/monstres/gen_dragons_etoiles.py <src/main/resources>
"""
import json, os, sys

RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "src", "main", "resources")
SERVER = os.path.join(RES, "Server")
DRAGONS = {"Feu": ("Dragon_Fire", "server.npcRoles.Dragon_Fire.name", "Fire", 70),
           "Givre": ("Dragon_Frost", "server.npcRoles.Dragon_Frost.name", "Ice", 65)}


def write(rel, data):
    path = os.path.join(SERVER, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


# Morsure : le Yeti_Swing_Left du jeu sans son animation (le dragon n'en a pas), plus large et plus longue
write("Item/Interactions/NPCs/Eldanior/Dragon_Etoile_Morsure.json", {
    "$Comment": "Morsure des dragons etoiles (scripts/monstres/gen_dragons_etoiles.py) : pas d'animation, le modele n'en a pas.",
    "Type": "Simple",
    "RunTime": 0.6,
    "Next": {
        "Type": "Parallel",
        "Interactions": [
            {"Interactions": [{
                "Type": "Selector",
                "RunTime": 0.2,
                "Selector": {"Id": "Horizontal", "Direction": "ToLeft", "TestLineOfSight": True, "ExtendTop": 2, "ExtendBottom": 1,
                             "StartDistance": 0.5, "EndDistance": 6, "Length": 120, "RollOffset": 0, "YawStartOffset": -60,
                             "PitchOffset": -10},
                "HitEntity": {"Interactions": [{"Type": "Replace", "DefaultValue": {"Interactions": ["NPC_Attack_Melee_Damage"]},
                                                "Var": "Melee_Damage"}]},
                "Next": {"Type": "Simple", "RunTime": 0.8}}]},
            {"Interactions": [{"Type": "Simple", "RunTime": 0.25, "Effects": {"WorldSoundEventId": "SFX_Golem_Earth_Swing"}}]},
        ],
    },
})
write("Item/RootInteractions/NPCs/Eldanior/Root_NPC_Eldanior_Dragon_Attack.json", {
    "Interactions": ["Dragon_Etoile_Morsure"],
    "Tags": {"Attack": ["Melee"]},
})

for suffix, (appearance, name_key, element, damage) in DRAGONS.items():
    write("NPC/Roles/Eldanior/Monstres/Eldanior_Dragon_Etoile_%s.json" % suffix, {
        "$Comment": "Dragon des monstres etoiles (scripts/monstres/gen_dragons_etoiles.py) ; vie et niveau poses par MonsterContracts.",
        "Type": "Variant",
        "Reference": "Template_Predator",
        "Modify": {
            "Appearance": appearance,
            "AttitudeGroup": "PredatorsBig",
            "DropList": "Empty",
            "MaxHealth": 600,
            "MaxSpeed": 7,
            "WanderRadius": 10,
            "ViewRange": 24,
            "HearingRange": 16,
            "AbsoluteDetectionRange": 4,
            "AlertedRange": 30,
            "AlertedTime": [2, 2],
            "MaintainDistanceWhileAlerted": False,
            "ThreatenAnimation": "",
            "ThreatenDelayRange": [1, 1],
            "FleeIfNotThreatened": False,
            "LeashDistance": 40,
            "HardLeashDistance": 80,
            "LeashMinPlayerDistance": 10,
            "LeashTimer": [3, 4],
            "CombatBehaviorDistance": 9,
            "CombatRelativeTurnSpeed": 0.6,
            "CombatDirectWeight": 10,
            "CombatStrafeWeight": 0,
            "CombatBackOffAfterAttack": False,
            "CombatMovingRelativeSpeed": 0.4,
            "Attack": "Root_NPC_Eldanior_Dragon_Attack",
            "AttackDistance": 5.5,
            "TargetRange": 5.5,
            "AttackPauseRange": [1.5, 2.5],
            "ChaseRelativeSpeed": 1,
            "AlwaysSleepAtNight": False,
            "DayFlavorAnimation": "",
            "NightFlavorAnimation": "",
            "DayTimeNapWeight": 0,
            "KnockbackScale": 0,
            "IsMemory": False,
            "_InteractionVars": {"Melee_Damage": {"Interactions": [{
                "Parent": "NPC_Attack_Melee_Damage",
                "DamageCalculator": {"Type": "Absolute", "BaseDamage": {element: damage}, "RandomPercentageModifier": 0.1},
                "DamageEffects": {"Knockback": {"Force": 3}}}]}},
            "NameTranslationKey": name_key,
        },
    })
print("Dragons etoiles : %d roles, morsure et attaque ecrites." % len(DRAGONS))
