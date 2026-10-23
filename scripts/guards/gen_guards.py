"""Assets des gardes de zone (PNJ de protection) : garde de ville, garde royal, chevalier, et le patrouilleur.

Pour chaque rang :
  1. Modele : le joueur vanilla (Parent "Player") avec l'armure complete en attachements (comme Temple_Mithril_Guard).
  2. Combat : un evaluateur d'actions (CAE) copie de CAE_Test_Combat_Knight (vrais coups d'epee du joueur, coup charge,
     signature, blocage au bouclier, pas de cote), degats multiplies par le rang.
  3. Role : variante de Template_Intelligent (detection, appel a l'aide, laisse et retour au poste, attente immobile).
Plus le groupe de PNJ Eldanior_Guards (pas de tir ami, appel a l'aide) et le groupe d'attitude Eldanior_Guard
(hostile aux monstres, amical envers les civils). Les joueurs sont ignores : c'est le code (GuardManager) qui rend
hostile un joueur qui fait du PvP pres d'un garde.
Au repos, un garde rejoint son poste (composant Component_Instruction_Eldanior_Leash_Return, scripts/npcs/gen_npcs.py) :
apres un combat ou un appel, il y revient et GuardManager le remet face a sa direction d'origine.
Le patrouilleur (armure de fer sans casque, lance et torche) fait des rondes : composant d'attente
Component_Instruction_Eldanior_Patrol_Round (rejoint son point de laisse avec la recherche de chemin ; GuardManager le
deplace de point en point sur un cercle praticable autour du poste) ; il se bat a la lance (attaque de melee des PNJ) et GuardManager lui fait appeler les gardes proches.
Niveaux (PV virtuels, degats) : NpcData (GARDE_*).

Usage : python3 gen_guards.py <src/main/resources> <CAE_Test_Combat_Knight.json vanilla>
"""
import copy, json, os, sys

RES, KNIGHT_CAE = sys.argv[1], sys.argv[2]

# id, nom, armure, arme, bouclier, PV de base, vitesse, recul, multiplicateur de degats (base vanilla : 9 / 12)
TIERS = [
    ("Ville",     "Garde de ville", "Iron",    "Weapon_Sword_Iron",    "Weapon_Shield_Iron",    300,  10, 0.4,  1.5),
    ("Royal",     "Garde royal",    "Mithril", "Weapon_Sword_Mithril", "Weapon_Shield_Mithril", 600,  11, 0.25, 2.5),
    ("Chevalier", "Chevalier",      "Onyxium", "Weapon_Sword_Onyxium", "Weapon_Shield_Onyxium", 1000, 12, 0.1,  3.5),
]

# Monstres que les gardes attaquent a vue (groupes de PNJ vanilla, Server/NPC/Groups)
# Patrouilleur : id, nom, armure (sans casque), arme, main gauche, PV de base, vitesse, degats de la lance, rayon de ronde
# La lance (deux mains) masque la main gauche : torche en main pendant la ronde (emplacement 1), lance en combat
# (emplacement 0, remis par le gabarit a chaque changement d'etat).
PATROL = ("Patrouilleur", "Patrouilleur", "Iron", ["Weapon_Spear_Iron", "Furniture_Crude_Torch"], None, 200, 10, 12)
HAIR = ("Characters/Haircuts/GenericShort.blockymodel", "Characters/Haircuts/GenericShort_Greyscale.png", "Hair", "BrownDark")

HOSTILE_GROUPS = ["Undead", "Void", "Predators", "PredatorsBig", "Trork", "Goblin", "Outlander", "Scarak",
                  "Spiders", "Snakes", "Scorpions"]
FRIENDLY_GROUPS = ["Eldanior_Guards", "Kweebec", "Feran"]

BODY = [
    ("Characters/Body_Attachments/Faces/Player_Face_Detached.blockymodel",
     "Characters/Body_Attachments/Faces/Faces_Detached_Textures/Face.png", "Skin", "02"),
    ("Characters/Body_Attachments/Mouths/Mouth1.blockymodel",
     "Characters/Body_Attachments/Mouths/Mouth1_Textures/Default_Greyscale.png", "Skin", "02"),
    ("Characters/Body_Attachments/Ears/Ears1.blockymodel", "Characters/Body_Attachments/Ears/Ears.png", "Skin", "02"),
    ("Characters/Body_Attachments/Eyes/Eyes.blockymodel",
     "Characters/Body_Attachments/Eyes/Eyes_Textures/Greyscale.png", "Eyes_Gradient", "Black"),
    ("Characters/Body_Attachments/Eyebrows/Eyebrows_Medium.blockymodel",
     "Characters/Body_Attachments/Eyebrows/Plucked_Greyscale.png", "Hair", "BrownDark"),
]


def write(rel, data):
    path = os.path.join(RES, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")
    print("ecrit", rel)


def model(armor, helmet=True):
    body = BODY if helmet else BODY + [HAIR]
    att = [{"Model": m, "Texture": t, "GradientSet": g, "GradientId": i} for m, t, g, i in body]
    for part in ("Chest", "Legs", "Hands", "Head") if helmet else ("Chest", "Legs", "Hands"):
        att.append({"Model": f"Items/Armors/{armor}/{part}.blockymodel", "Texture": f"Items/Armors/{armor}/{part}_Texture.png"})
    return {
        "Parent": "Player",
        "Model": "Characters/Player.blockymodel",
        "Texture": "Characters/Player_Textures/Player_Muscular_Greyscale.png",
        "GradientSet": "Skin",
        "GradientId": "02",
        "DefaultAttachments": att,
    }


def scale_damage(node, mult):
    """Multiplie toutes les valeurs BaseDamage du CAE (coup de base, charge, signature)."""
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


def role(tid, armor_weapon, shield, hp, speed, knockback, patrol_damage=None):
    rid = f"Eldanior_Guard_{tid}"
    r = {
        "Type": "Variant",
        "Reference": "Template_Intelligent",
        "Modify": {
            "Appearance": rid,
            "DropList": "Empty",
            "MaxHealth": hp,
            "MaxSpeed": speed,
            "KnockbackScale": knockback,
            "Weapons": armor_weapon if isinstance(armor_weapon, list) else [armor_weapon],
            "OffHand": [shield] if shield else [],
            "DefaultOffHandSlot": 0,
            "NighttimeOffhandSlot": 0,
            "WakingPeriod": [0, 24],
            "BedBlockSet": "",
            "WakingIdleBehaviorComponent": "Component_Instruction_Eldanior_Leash_Return",
            "WanderRadius": 2,
            "Patrol": False,
            "FollowPatrolPath": False,
            "ViewRange": 20,
            "ViewSector": 240,
            "HearingRange": 12,
            "AbsoluteDetectionRange": 4,
            "AlertedRange": 48,
            "AlertedTime": [0.3, 0.6],
            "LeashDistance": 24,
            "LeashMinPlayerDistance": 4,
            "LeashTimer": [2, 3],
            "HardLeashDistance": 48,
            "ChanceToBeAlertedWhenReceivingCallForHelp": 100,
            "CombatMessageTargetGroups": ["Eldanior_Guards"],
            "InvestigateRange": 48,
            "SearchTimeRange": [4, 6],
            "UseCombatActionEvaluator": True,
            "_CombatConfig": f"CAE_{rid}",
            "AttitudeGroup": "Eldanior_Guard",
            "DefaultPlayerAttitude": "Ignore",
            "DisableDamageGroups": ["Self", "Eldanior_Guards"],
            "BreathesInWater": True,
            "NameTranslationKey": {"Compute": "NameTranslationKey"},
        },
        "Parameters": {
            "NameTranslationKey": {
                "Value": f"server.npcRoles.{rid}.name",
                "Description": "Translation key for NPC name display",
            }
        },
    }
    if patrol_damage is not None:
        m = r["Modify"]
        for k in ("_CombatConfig",):
            m.pop(k)
        m.update({
            "UseCombatActionEvaluator": False,
            "WakingIdleBehaviorComponent": "Component_Instruction_Eldanior_Patrol_Round",
            "DefaultOffHandSlot": -1,
            "NighttimeOffhandSlot": -1,
            "LeashDistance": 40,
            "HardLeashDistance": 70,
            "Attack": "Root_NPC_Attack_Melee",
            "AttackDistance": 3,
            "DesiredAttackDistanceRange": [2, 2.5],
            "BlockAbility": "",
            "_InteractionVars": {"Melee_Damage": {"Interactions": [
                {"Parent": "NPC_Attack_Melee_Damage", "DamageCalculator": {"BaseDamage": {"Physical": patrol_damage}}}]}},
        })
    return r


PATROL_ROUND = {
    "Type": "Component",
    "Class": "Instruction",
    "Interface": "Hytale.Instruction.Idle",
    "Parameters": {
        "WanderRadius": {"Value": 25, "Description": "Non utilise : exige par l'interface du gabarit."}
    },
    "Content": {
        "Weight": 100,
        "Instructions": [
            {"$Comment": "Torche en main pendant la ronde (la lance revient en combat).", "Continue": True,
             "Sensor": {"Type": "Any", "Once": True},
             "Actions": [{"Type": "Inventory", "Operation": "EquipHotbar", "UseTarget": False, "Slot": 1}]},
            {"$Comment": "Ronde : rejoint le point de laisse, que GuardManager deplace de point en point sur un cercle de 25 blocs ; la recherche de chemin contourne murs et angles.",
             "Sensor": {"Type": "Leash", "Range": 1.5},
             "BodyMotion": {"Type": "Seek", "SlowDownDistance": 1.5, "StopDistance": 0.8, "RelativeSpeed": 0.3,
                            "UsePathfinder": True}},
            {"BodyMotion": {"Type": "Nothing"}},
        ],
    },
}


with open(KNIGHT_CAE, encoding="utf-8") as f:
    knight = json.load(f)

for tid, name, armor, weapon, shield, hp, speed, kb, mult in TIERS:
    rid = f"Eldanior_Guard_{tid}"
    write(f"Server/Models/Eldanior/Guards/{rid}.json", model(armor))
    cae = copy.deepcopy(knight)
    scale_damage(cae, mult)
    write(f"Server/NPC/Balancing/Eldanior/CAE_{rid}.json", cae)
    write(f"Server/NPC/Roles/Eldanior/Guards/{rid}.json", role(tid, weapon, shield, hp, speed, kb))

pid, pname, parmor, pweapon, poff, php, pspeed, pdmg = PATROL
write(f"Server/Models/Eldanior/Guards/Eldanior_Guard_{pid}.json", model(parmor, helmet=False))
write(f"Server/NPC/Roles/Eldanior/Guards/Eldanior_Guard_{pid}.json", role(pid, pweapon, poff, php, pspeed, 0.5, pdmg))
write("Server/NPC/Roles/Eldanior/Components/Component_Instruction_Eldanior_Patrol_Round.json", PATROL_ROUND)

write("Server/NPC/Groups/Eldanior/Eldanior_Guards.json", {"IncludeRoles": ["Eldanior_Guard_*"]})
write("Server/NPC/Attitude/Roles/Eldanior/Eldanior_Guard.json",
      {"Groups": {"Friendly": FRIENDLY_GROUPS, "Hostile": HOSTILE_GROUPS}})
