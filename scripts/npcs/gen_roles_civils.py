"""Roles des PNJ civils a qui l'on parle (touche F) : villageois, pelerin, marchand du Clan Luo, marchand ambulant.

Roles « Generic » interactifs (base : le maitre de guilde) qui savent aussi marcher : au repos (etat Idle), le PNJ rejoint son
point de laisse avec la recherche de chemin. GuardManager / PostMovement deplacent ce point (promenade du villageois et du
pelerin) ; pour un marchand, c'est son poste (il y revient s'il a ete pousse). Un joueur a moins de 8 blocs : etat Watching,
le PNJ s'arrete et le regarde ; F : etat $Interaction, que guards/NpcInteractSystem traite (services du poste : commerce,
quete, discussion, recrutement...). Le salut « Wave » du role d'origine est retire (absent du modele Player).
Tenues, armes et skins : poses par le code (GuardManager).

Usage : python3 scripts/npcs/gen_roles_civils.py <src/main/resources>
"""
import copy, json, os, sys

RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "src", "main", "resources")
ROLES = os.path.join(RES, "Server/NPC/Roles/Eldanior/Npcs/")

# role, invulnerable, PV, vitesse relative de marche, texte de la touche F
CIVILS = [
    ("Eldanior_Npc_Villageois", False, 120, 0.45, "server.interactionHints.eldanior_talk"),
    ("Eldanior_Npc_Pelerin", False, 140, 0.4, "server.interactionHints.eldanior_talk"),
    ("Eldanior_Npc_Marchand_Luo", True, 300, 0.6, "server.interactionHints.trade"),
    ("Eldanior_Npc_Marchand_Ambulant", True, 300, 0.6, "server.interactionHints.trade"),
    ("Eldanior_Npc_Commercant", True, 300, 0.6, "server.interactionHints.trade"),
    ("Eldanior_Npc_Forgeron_Legendaire", True, 600, 0.4, "server.interactionHints.trade"),
    ("Eldanior_Npc_Sensei", False, 800, 0.4, "server.interactionHints.eldanior_talk"),       # 2026-11-10 : un par famille ; coups annules par Sensei.onAttacked (riposte)
]

# Disciples du dojo (2026-11-13) : on leur parle pour un duel (quetes du Sensei) ; ils restent dans le dossier des gardes et
# gardent leurs identifiants (postes deja poses). Coups annules et riposte comme le Sensei (Sensei.onAttacked).
GUARDS = os.path.join(RES, "Server/NPC/Roles/Eldanior/Guards/")
DISCIPLES = [
    ("Eldanior_Guard_Disciple", False, 220, 0.45, "server.interactionHints.eldanior_talk"),
    ("Eldanior_Guard_Disciple_Luo", False, 220, 0.45, "server.interactionHints.eldanior_talk"),
]


def walk(speed):
    return {"$Comment": "Rejoint son point de laisse (poste, ou point de promenade choisi par PostMovement).",
            "Sensor": {"Type": "Leash", "Range": 1.2},
            "BodyMotion": {"Type": "Seek", "SlowDownDistance": 1.5, "StopDistance": 0.5, "RelativeSpeed": speed, "UsePathfinder": True}}


def strip_wave(node):
    """Retire les PlayAnimation « Wave » (et l'attente qu'elles bloquent) en gardant le changement d'etat."""
    if isinstance(node, dict):
        if node.get("ActionsBlocking") and any(a.get("Animation") == "Wave" for a in node.get("Actions", [])):
            node.pop("ActionsBlocking")
        if "Actions" in node:
            node["Actions"] = [a for a in node["Actions"] if not (a.get("Type") == "PlayAnimation" and a.get("Animation") == "Wave")]
        for v in node.values():
            strip_wave(v)
    elif isinstance(node, list):
        for v in node:
            strip_wave(v)


def main():
    with open(ROLES + "Eldanior_Npc_Maitre_Guilde.json", encoding="utf-8") as f:
        base = json.load(f)
    for folder, rid, invulnerable, hp, speed, hint in [(ROLES,) + c for c in CIVILS] + [(GUARDS,) + d for d in DISCIPLES]:
        r = copy.deepcopy(base)
        strip_wave(r)
        idle = r["Instructions"][0]["Instructions"][0]
        assert idle["Sensor"].get("State") == "Idle"
        idle["Instructions"].insert(len(idle["Instructions"]) - 1, walk(speed))
        r["MaxHealth"] = hp
        if folder == GUARDS:                                           # disciple : fourreau du Bokken en main gauche
            r["OffHandSlots"] = 1
        r["Armor"] = []
        r["Parameters"]["Invulnerable"]["Value"] = invulnerable
        r["Parameters"]["NameTranslationKey"]["Value"] = "server.npcRoles.%s.name" % rid
        text = json.dumps(r).replace("server.interactionHints.eldanior_guildmaster", hint)
        r = json.loads(text)
        r["$Comment"] = "PNJ civil a qui l'on parle (scripts/npcs/gen_roles_civils.py) : marche vers son point de laisse, F -> NpcInteractSystem."
        with open(folder + rid + ".json", "w", encoding="utf-8") as f:
            json.dump(r, f, indent=2, ensure_ascii=False)
            f.write("\n")
        print("ecrit", rid)


if __name__ == "__main__":
    main()
