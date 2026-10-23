"""Assets des PNJ de poste (hors gardes classiques, voir scripts/guards/gen_guards.py) :

  - Mage royal (Eldanior_Guard_Mage) : archimage garde royal, tenue de mage (Trailer_Mage), baton d'onyxium, boule de feu
    a distance (role) + sorts lances par le code (RoyalMageMagic : pluie de flammes, nova de givre, soin des gardes).
  - Aventurier (Eldanior_Npc_Aventurier) : recrutable contre de l'or (interaction -> fenetre RecruitScreen). Une fois
    recrute, le code le remplace par Eldanior_Npc_Aventurier_Compagnon (suit son joueur : point de laisse place derriere lui,
    combat ce que le joueur combat). Mort ou fin de contrat : il revient a son poste.
  - Chevalier errant (Eldanior_Npc_Chevalier_Errant) : armure d'acier sans casque, epee et bouclier, erre dans 200 blocs.
  - Villageois (Eldanior_Npc_Villageois) : se promene dans sa ville, va voir les autres PNJ et les blocs (etablis, coffres...).
Skins : joueur aleatoire par PNJ (pose par GuardManager), d'ou l'apparence "Player". Niveaux : NpcData.
Armure des variantes de gabarit (chevalier errant, compagnon) : posee par GuardManager (GuardType.armor) ; les gabarits
n'acceptent pas de champ Armor.
Les deplacements (errance, promenade) sont pilotes par GuardManager via le point de laisse (composant
Component_Instruction_Eldanior_Leash_Walk) : le moteur fait marcher le PNJ avec ses animations et sa recherche de chemin.

Usage : python3 gen_npcs.py <src/main/resources> <CAE_Test_Combat_Knight.json vanilla>
"""
import copy, json, os, sys

RES, KNIGHT_CAE = sys.argv[1], sys.argv[2]
QUEST_ROLE = os.path.join(RES, "Server/NPC/Roles/Intelligent/Neutral/Kweebec/Quest_Npc_Secondaire_Duel_N9.json")

BODY = [
    ("Characters/Body_Attachments/Faces/Player_Face_Detached.blockymodel",
     "Characters/Body_Attachments/Faces/Faces_Detached_Textures/Face.png", "Skin", "03"),
    ("Characters/Body_Attachments/Mouths/Mouth1.blockymodel",
     "Characters/Body_Attachments/Mouths/Mouth1_Textures/Default_Greyscale.png", "Skin", "03"),
    ("Characters/Body_Attachments/Ears/Ears1.blockymodel", "Characters/Body_Attachments/Ears/Ears.png", "Skin", "03"),
    ("Characters/Body_Attachments/Eyes/Eyes.blockymodel",
     "Characters/Body_Attachments/Eyes/Eyes_Textures/Greyscale.png", "Eyes_Gradient", "Black"),
    ("Characters/Body_Attachments/Eyebrows/Eyebrows_Medium.blockymodel",
     "Characters/Body_Attachments/Eyebrows/Plucked_Greyscale.png", "Hair", "Grey"),
]
MAGE_OUTFIT = [
    ("Items/Armors/Trailer_Mage/Chest.blockymodel", "Items/Armors/Trailer_Mage/Chest_Texture.png"),
    ("Items/Armors/Trailer_Mage/Legs.blockymodel", "Items/Armors/Trailer_Mage/Legs_default.png"),
    ("Items/Armors/Trailer_Mage/Head.blockymodel", "Items/Armors/Trailer_Mage/Head_Texture.png"),
    ("Items/Armors/Trailer_Mage/Cape.blockymodel", "Items/Armors/Trailer_Mage/Cape.png"),
]

HOSTILE_GROUPS = ["Undead", "Void", "Predators", "PredatorsBig", "Trork", "Goblin", "Outlander", "Scarak",
                  "Spiders", "Snakes", "Scorpions"]


def write(rel, data):
    path = os.path.join(RES, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")
    print("ecrit", rel)


def name_param(rid):
    return {"NameTranslationKey": {"Value": f"server.npcRoles.{rid}.name", "Description": "Translation key for NPC name display"}}


def intelligent(rid, modify):
    """Variante de Template_Intelligent, reglages communs des PNJ de poste."""
    base = {
        "DropList": "Empty",
        "WakingPeriod": [0, 24],
        "BedBlockSet": "",
        "Patrol": False,
        "FollowPatrolPath": False,
        "ViewRange": 20,
        "ViewSector": 240,
        "HearingRange": 12,
        "AlertedRange": 40,
        "AlertedTime": [0.3, 0.6],
        "LeashMinPlayerDistance": 4,
        "LeashTimer": [2, 3],
        "SearchTimeRange": [4, 6],
        "BreathesInWater": True,
        "NameTranslationKey": {"Compute": "NameTranslationKey"},
    }
    base.update(modify)
    return {"Type": "Variant", "Reference": "Template_Intelligent", "Modify": base, "Parameters": name_param(rid)}


# ===== Composants : marcher vers le point de laisse (deplace par GuardManager / PostMovement) =====
def leash_component(name, comment, speed, slow, stop):
    write(f"Server/NPC/Roles/Eldanior/Components/{name}.json", {
        "Type": "Component",
        "Class": "Instruction",
        "Interface": "Hytale.Instruction.Idle",
        "Parameters": {
            "WanderRadius": {"Value": 10, "Description": "Non utilise : exige par l'interface du gabarit."},
        },
        "Content": {
            "Weight": 100,
            "Instructions": [
                {"$Comment": comment,
                 "Sensor": {"Type": "Leash", "Range": 1.5},
                 "BodyMotion": {"Type": "Seek", "SlowDownDistance": slow, "StopDistance": stop, "RelativeSpeed": speed,
                                "UsePathfinder": True}},
                {"BodyMotion": {"Type": "Nothing"}},
            ],
        },
    })


leash_component("Component_Instruction_Eldanior_Leash_Walk",
                "Promenade / errance : rejoint le point de laisse en marchant, avec la recherche de chemin.", 0.28, 1.5, 0.8)
leash_component("Component_Instruction_Eldanior_Leash_Return",
                "Garde au repos : revient a son poste (point de laisse) apres un combat ou un appel.", 0.45, 1.5, 0.6)
leash_component("Component_Instruction_Eldanior_Leash_Follow",
                "Aventurier recrute : suit son joueur (point de laisse place derriere lui par GuardManager).", 0.9, 3, 1.2)

# ===== Mage royal =====
write("Server/Models/Eldanior/Guards/Eldanior_Guard_Mage.json", {
    "Parent": "Player",
    "Model": "Characters/Player.blockymodel",
    "Texture": "Characters/Player_Textures/Player_Muscular_Greyscale.png",
    "GradientSet": "Skin",
    "GradientId": "03",
    "DefaultAttachments": [{"Model": m, "Texture": t, "GradientSet": g, "GradientId": i} for m, t, g, i in BODY]
                          + [{"Model": m, "Texture": t} for m, t in MAGE_OUTFIT],
})
# Boule de feu du mage : copie du projectile vanilla Fireball (60 degats) reglee a MAGE_FIREBALL_DAMAGE
MAGE_FIREBALL_DAMAGE = 35
with open(os.path.join(os.path.dirname(KNIGHT_CAE), "..", "..", "Projectiles", "Spells", "Fireball.json"), encoding="utf-8") as f:
    fireball = json.load(f)
fireball["Damage"] = MAGE_FIREBALL_DAMAGE
write("Server/Projectiles/Eldanior/Eldanior_Mage_Fireball.json", fireball)

write("Server/Item/Interactions/NPCs/Eldanior/Eldanior_NPC_Mage_Fireball.json", {
    "Type": "Simple",
    "Effects": {"ItemPlayerAnimationsId": "Staff", "ItemAnimationId": "CastSummon"},
    "$Comment": "Incantation puis boule de feu (copie du projectile vanilla Fireball, degats reduits).",
    "RunTime": 0.6,
    "Next": {
        "Type": "LaunchProjectile",
        "RunTime": 0.2,
        "Effects": {"WorldSoundEventId": "SFX_Staff_Flame_Fireball_Launch"},
        "ProjectileId": "Eldanior_Mage_Fireball",
        "Tags": {"AimingReference": []},
    },
})
write("Server/Item/RootInteractions/NPCs/Eldanior/Root_NPC_Eldanior_Mage_Attack.json", {
    "Interactions": ["Eldanior_NPC_Mage_Fireball"],
    "Tags": {"Attack": ["Ranged"]},
})
write("Server/NPC/Roles/Eldanior/Guards/Eldanior_Guard_Mage.json", intelligent("Eldanior_Guard_Mage", {
    "Appearance": "Eldanior_Guard_Mage",
    "MaxHealth": 400,
    "MaxSpeed": 9,
    "KnockbackScale": 0.3,
    "Weapons": ["Weapon_Staff_Onyxium"],
    "OffHand": [],
    "DefaultOffHandSlot": -1,
    "NighttimeOffhandSlot": -1,
    "WakingIdleBehaviorComponent": "Component_Instruction_Eldanior_Leash_Return",
    "WanderRadius": 2,
    "LeashDistance": 24,
    "HardLeashDistance": 48,
    "ChanceToBeAlertedWhenReceivingCallForHelp": 100,
    "CombatMessageTargetGroups": ["Eldanior_Guards"],
    "InvestigateRange": 48,
    "UseCombatActionEvaluator": False,
    "Attack": "Root_NPC_Eldanior_Mage_Attack",
    "AttackDistance": 16,
    "DesiredAttackDistanceRange": [7, 14],
    "AttackPauseRange": [1.2, 1.8],
    "CombatBehaviorDistance": 16,
    "TargetRange": 16,
    "CombatBackOffAfterAttack": False,
    "CombatStrafeWeight": 3,
    "CombatFleeIfTooCloseDistance": 3,
    "BlockAbility": "",
    "AttitudeGroup": "Eldanior_Guard",
    "DefaultPlayerAttitude": "Ignore",
    "DisableDamageGroups": ["Self", "Eldanior_Guards"],
}))

# ===== Chevalier errant =====
write("Server/NPC/Roles/Eldanior/Npcs/Eldanior_Npc_Chevalier_Errant.json", intelligent("Eldanior_Npc_Chevalier_Errant", {
    "Appearance": "Player",
    "MaxHealth": 500,
    "MaxSpeed": 10,
    "KnockbackScale": 0.3,
    "Weapons": ["Weapon_Sword_Steel"],
    "OffHand": ["Weapon_Shield_Iron"],
    "DefaultOffHandSlot": 0,
    "NighttimeOffhandSlot": 0,
    "WakingIdleBehaviorComponent": "Component_Instruction_Eldanior_Leash_Walk",
    "WanderRadius": 10,
    "LeashDistance": 60,
    "HardLeashDistance": 120,
    "UseCombatActionEvaluator": True,
    "_CombatConfig": "CAE_Eldanior_Npc_Chevalier_Errant",
    "AttitudeGroup": "Eldanior_Guard",
    "DefaultPlayerAttitude": "Ignore",
    "DisableDamageGroups": ["Self", "Eldanior_Guards"],
}))
with open(KNIGHT_CAE, encoding="utf-8") as f:
    cae = json.load(f)


def scale(node, mult):
    if isinstance(node, dict):
        for k, v in node.items():
            if k == "BaseDamage" and isinstance(v, dict):
                for t in v:
                    v[t] = round(v[t] * mult)
            else:
                scale(v, mult)
    elif isinstance(node, list):
        for v in node:
            scale(v, mult)


errant_cae = copy.deepcopy(cae)
scale(errant_cae, 2.5)
write("Server/NPC/Balancing/Eldanior/CAE_Eldanior_Npc_Chevalier_Errant.json", errant_cae)

# ===== Villageois =====
write("Server/NPC/Attitude/Roles/Eldanior/Eldanior_Villager.json",
      {"Groups": {"Friendly": ["Eldanior_Guards", "Kweebec", "Feran"]}})
write("Server/NPC/Roles/Eldanior/Npcs/Eldanior_Npc_Villageois.json", intelligent("Eldanior_Npc_Villageois", {
    "Appearance": "Player",
    "MaxHealth": 120,
    "MaxSpeed": 8,
    "KnockbackScale": 0.8,
    "Weapons": [],
    "OffHand": [],
    "DefaultOffHandSlot": -1,
    "NighttimeOffhandSlot": -1,
    "WakingIdleBehaviorComponent": "Component_Instruction_Eldanior_Leash_Walk",
    "WanderRadius": 10,
    "LeashDistance": 50,
    "HardLeashDistance": 80,
    "UseCombatActionEvaluator": False,
    "AttitudeGroup": "Eldanior_Villager",
    "DefaultPlayerAttitude": "Ignore",
    "CombatFleeIfTooCloseDistance": 6,
}))

# ===== Aventurier recrutable (role interactif des PNJ de quete) =====
with open(QUEST_ROLE, encoding="utf-8") as f:
    quest = json.load(f)
recruit = copy.deepcopy(quest)
recruit["Appearance"] = "Player"
recruit["MaxHealth"] = 300
recruit["DefaultPlayerAttitude"] = "Neutral"
recruit["HotbarItems"] = ["Weapon_Sword_Iron"]
recruit["HotbarSize"] = 3
recruit["OffHandItems"] = ["Weapon_Shield_Iron"]
recruit["OffHandSlots"] = 1
recruit["DefaultOffHandSlot"] = 0
recruit["Armor"] = ["Armor_Leather_Medium_Chest", "Armor_Leather_Medium_Hands", "Armor_Leather_Medium_Legs"]
recruit.pop("MemoriesCategory", None)
recruit.pop("MemoriesNameOverride", None)
recruit["Parameters"].pop("MemoriesNameOverride", None)
recruit["Parameters"]["NameTranslationKey"] = name_param("Eldanior_Npc_Aventurier")["NameTranslationKey"]
recruit["NameTranslationKey"] = {"Compute": "NameTranslationKey"}
for ins in recruit["InteractionInstruction"]["Instructions"]:
    for a in ins.get("Actions", []):
        if a.get("Type") == "SetInteractable" and a.get("Interactable"):
            a["Hint"] = "server.interactionHints.eldanior_recruit"
write("Server/NPC/Roles/Eldanior/Npcs/Eldanior_Npc_Aventurier.json", recruit)

# ===== Aventurier compagnon (suit le joueur qui l'a recrute : point de laisse deplace par GuardManager) =====
write("Server/NPC/Roles/Eldanior/Npcs/Eldanior_Npc_Aventurier_Compagnon.json", intelligent("Eldanior_Npc_Aventurier", {
    "Appearance": "Player",
    "MaxHealth": 300,
    "MaxSpeed": 12,
    "RunThreshold": 0.3,
    "KnockbackScale": 0.4,
    "Weapons": ["Weapon_Sword_Iron"],
    "OffHand": ["Weapon_Shield_Iron"],
    "DefaultOffHandSlot": 0,
    "NighttimeOffhandSlot": 0,
    "WakingIdleBehaviorComponent": "Component_Instruction_Eldanior_Leash_Follow",
    "WanderRadius": 3,
    "LeashDistance": 30,
    "HardLeashDistance": 60,
    "UseCombatActionEvaluator": True,
    "_CombatConfig": "CAE_Eldanior_Npc_Chevalier_Errant",
    "AttitudeGroup": "Eldanior_Guard",
    "DefaultPlayerAttitude": "Ignore",
    "DisableDamageGroups": ["Self", "Eldanior_Guards"],
}))

write("Server/NPC/Groups/Eldanior/Eldanior_Npcs.json", {"IncludeRoles": ["Eldanior_Npc_*"]})
