"""PNJ de poste ajoutes pour le Clan Luo, et variantes de role "_Luo" des PNJ poses sur le territoire Luo.

Nouveaux postes :
  Assassin  Eldanior_Guard_Assassin : dagues, reste a son poste (deplacement propre a venir) ; cuir de corbeau.
  Pelerin   Eldanior_Npc_Pelerin : se promene comme le villageois, fuit le combat, prie de temps en temps.
  Disciple  Eldanior_Guard_Disciple : epee de bois, reste a son poste et y enchaine des kata ; se bat (CAE du chevalier
            vanilla sans bouclier).
Variantes "_Luo" (GuardManager.spawnAs les choisit pour un poste sur le territoire Luo, LuoNpcs.LUO_ROLES) : meme role,
katana du clan au lieu de l'arme et du bouclier, combat d'epee sans parade au bouclier (CAE du role, parades retirees),
fourreau en main gauche ; au repos le katana est rengaine, en combat au clair (LuoNpcs.sheathTick) :
  garde de ville, garde royal, patrouilleur, criminel, chasseur de prime, aventurier recrute (guerriers) : katana de fer ;
  chevalier : Aonami (rare) ; assassin : Tsukikage, Ombre de Lune ; disciple : bokken.
Tenues (kimonos) et armes des aventuriers libres : posees par le code (LuoNpcs). Niveaux : NpcData (le nom de la variante
contient celui du role de base).

Usage : python3 scripts/guards/gen_postes_luo.py <src/main/resources>
"""
import copy, json, os, sys, zipfile

RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "src", "main", "resources")
ASSETS = os.path.expanduser("~/Library/Application Support/Hytale/install/release/package/game/latest/Assets.zip")
ROLES, CAES = "Server/NPC/Roles/Eldanior/", "Server/NPC/Balancing/Eldanior/"
FER = "Eldanior_Weapon_Sword_Katana_Fer_Degainee"
AONAMI = "Eldanior_Weapon_Sword_Katana_Aonami_Degainee"
TSUKIKAGE = "Eldanior_Weapon_Sword_Katana_Tsukikage_Degainee"
BOKKEN = "Eldanior_Weapon_Sword_Katana_Bokken_Degainee"
# role de base (dossier), katana, CAE dont la variante reprend les coups (degats deja a l'echelle du role)
VARIANTS = [
    ("Guards/Eldanior_Guard_Ville", FER, "CAE_Eldanior_Guard_Ville"),
    ("Guards/Eldanior_Guard_Royal", FER, "CAE_Eldanior_Guard_Royal"),
    ("Guards/Eldanior_Guard_Patrouilleur", FER, "CAE_Eldanior_Guard_Ville"),
    ("Guards/Eldanior_Guard_Chevalier", AONAMI, "CAE_Eldanior_Guard_Chevalier"),
    ("Guards/Eldanior_Guard_Assassin", TSUKIKAGE, "CAE_Eldanior_Guard_Royal"),
    # Disciple : plus de variante ici (2026-11-13) ; ses deux roles sont des roles civils (scripts/npcs/gen_roles_civils.py)
    ("Criminels/Eldanior_Criminel", FER, "CAE_Eldanior_Npc_Chevalier_Errant"),
    ("Npcs/Eldanior_Npc_Chasseur_Prime", FER, "CAE_Eldanior_Npc_Chevalier_Errant"),
    ("Npcs/Eldanior_Npc_Aventurier_Compagnon", FER, "CAE_Eldanior_Npc_Chevalier_Errant"),
]
# Cles d'une attaque sans CAE (dagues, lance), retirees quand la variante passe au CAE
MELEE_KEYS = ("Attack", "_InteractionVars", "AttackDistance", "DesiredAttackDistanceRange", "AttackPauseRange")


def write(rel, data):
    path = os.path.join(RES, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")
    print("ecrit", rel)


def intelligent(rid, modify):
    base = {
        "Appearance": "Player",
        "DropList": "Empty",
        "WakingPeriod": [0, 24],
        "BedBlockSet": "",
        "Patrol": False,
        "FollowPatrolPath": False,
        "ViewRange": 24,
        "ViewSector": 240,
        "HearingRange": 14,
        "AlertedRange": 48,
        "AlertedTime": [0.3, 0.6],
        "LeashMinPlayerDistance": 4,
        "LeashTimer": [2, 3],
        "SearchTimeRange": [4, 6],
        "BreathesInWater": True,
        "NameTranslationKey": {"Compute": "NameTranslationKey"},
    }
    base.update(modify)
    return {"Type": "Variant", "Reference": "Template_Intelligent", "Modify": base,
            "Parameters": {"NameTranslationKey": {"Value": f"server.npcRoles.{rid}.name", "Description": "Translation key for NPC name display"}}}


write("Server/NPC/Roles/Eldanior/Guards/Eldanior_Guard_Assassin.json", intelligent("Eldanior_Guard_Assassin", {
    "MaxHealth": 450, "MaxSpeed": 14, "RunThreshold": 0.3, "KnockbackScale": 0.4,
    "Weapons": ["Weapon_Daggers_Iron"], "OffHand": [], "DefaultOffHandSlot": -1, "NighttimeOffhandSlot": -1,
    "WakingIdleBehaviorComponent": "Component_Instruction_Eldanior_Leash_Return",
    "WanderRadius": 2, "LeashDistance": 30, "HardLeashDistance": 60, "AbsoluteDetectionRange": 4, "InvestigateRange": 40,
    "ChanceToBeAlertedWhenReceivingCallForHelp": 100, "CombatMessageTargetGroups": ["Eldanior_Guards"],
    "UseCombatActionEvaluator": False, "Attack": "Root_NPC_Eldanior_Assassin_Daggers", "AttackPauseRange": [0.5, 0.9],
    "AttackDistance": 3, "DesiredAttackDistanceRange": [2, 2.5], "BlockAbility": "",
    "_InteractionVars": {"Melee_Damage": {"Interactions": [
        {"Parent": "NPC_Attack_Melee_Damage", "DamageCalculator": {"BaseDamage": {"Physical": 20}}}]}},
    "AttitudeGroup": "Eldanior_Guard", "DefaultPlayerAttitude": "Ignore", "DisableDamageGroups": ["Self", "Eldanior_Guards"],
}))

# Role du pelerin : scripts/npcs/gen_roles_civils.py (role interactif qui marche).


# ===== Disciple =====
def without_shield(cae):
    ev = cae["CombatActionEvaluator"]
    blocks = [k for k, v in ev["AvailableActions"].items() if v.get("Ability") == "Root_NPC_Shield_Block"]
    for k in blocks:
        del ev["AvailableActions"][k]
    for st in ev.get("ActionSets", {}).values():
        st["Actions"] = [a for a in st.get("Actions", []) if a not in blocks]
    return cae


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


with zipfile.ZipFile(ASSETS) as z:
    knight = json.loads(z.read("Server/NPC/Balancing/CAE_Test_Combat_Knight.json"))
disciple_cae = without_shield(copy.deepcopy(knight))
scale(disciple_cae, 0.9)
write(CAES + "CAE_Eldanior_Guard_Disciple.json", disciple_cae)
# 2026-11-13 : le role du disciple (et sa variante _Luo) est ecrit par scripts/npcs/gen_roles_civils.py : on lui parle (duel).


# ===== Variantes du territoire Luo =====
def load(rel):
    with open(os.path.join(RES, rel), encoding="utf-8") as f:
        return json.load(f)


for base, katana, cae_id in VARIANTS:
    role = load(ROLES + base + ".json")
    m = role["Modify"]
    for k in MELEE_KEYS:
        m.pop(k, None)
    luo_cae = cae_id + "_Luo"
    base_id = katana[:-len("_Degainee")]
    # emplacement 0 : lame au clair (combat, remise par le gabarit a chaque changement d'etat) ; 1 : au fourreau (repos, LuoNpcs)
    # main gauche : fourreau vide ; LuoNpcs.sheathTick y met le fourreau plein (avec la poignee) au repos
    # modele Player nu : le skin et le kimono sont poses par le code (l'armure du modele de garde se superposait au kimono)
    m["Appearance"] = "Player"
    m.update({"Weapons": [katana, base_id], "OffHand": [base_id + "_Fourreau_Vide"],
              "DefaultOffHandSlot": 0, "NighttimeOffhandSlot": 0,
              "UseCombatActionEvaluator": True, "_CombatConfig": luo_cae, "BlockAbility": ""})
    role["$Comment"] = "Variante du territoire Luo de " + base.split("/")[-1] + " (scripts/guards/gen_postes_luo.py) : katana du clan, sans bouclier."
    write(ROLES + base + "_Luo.json", role)
    write(CAES + luo_cae + ".json", without_shield(load(CAES + cae_id + ".json")))


# ===== Alchimiste royal (2026-11-10) : pas de magie dans l'Empire Luo, le mage royal y est alchimiste =====
# Meme role que le mage royal, modele Player (kimono Alchimiste pose par LuoNpcs), fioles de poison lancees comme le squelette
# alchimiste vanilla (Skeleton_Burnt_Alchemist : arme Weapon_Bomb_Potion_Poison, attaque Skeleton_Burnt_Alchemist_Bomb_Throw).
mage = load(ROLES + "Guards/Eldanior_Guard_Mage.json")
mm = mage["Modify"]
mm.update({"Appearance": "Player", "Weapons": ["Weapon_Bomb_Potion_Poison"], "OffHand": [], "DefaultOffHandSlot": -1,
           "NighttimeOffhandSlot": -1, "UseCombatActionEvaluator": False, "Attack": "Skeleton_Burnt_Alchemist_Bomb_Throw",
           "AttackDistance": 12, "DesiredAttackDistanceRange": [5, 12]})
mage["$Comment"] = ("Variante du territoire Luo du mage royal (scripts/guards/gen_postes_luo.py) : alchimiste royal, fioles de "
                    "poison (attaque du squelette alchimiste vanilla), kimono Alchimiste pose par LuoNpcs.")
write(ROLES + "Guards/Eldanior_Guard_Mage_Luo.json", mage)
