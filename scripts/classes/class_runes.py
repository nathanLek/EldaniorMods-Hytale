"""Runes (competences actives) des classes : ecrit la ligne `runes("Rune_...", ...);` dans le constructeur de chaque classe
(src/main/java/com/eldanior/system/classes/definitions), comme ses passifs. Les aventuriers d'une classe lancent ces runes.

Regles (peu de runes existent : rester coherent) :
  - runes permises par type (RuneManager.RUNE_CLASSES) ;
  - theme de la classe lu dans son identifiant (rage, defense, lames, feu, ombre, poison, vent, chasse...) : ordre de preference ;
  - nombre selon la rarete de la classe : commune/rare 1, epique/unique 2, legendaire/divine 3 ;
  - gravite reservee : Puits gravitationnel a partir d'unique, Autorite gravitationnelle legendaire et divine.
Marchands, dragon et novice : pas de rune. Idempotent : relancer apres avoir change une regle.

Usage : python3 scripts/classes/class_runes.py [--dry]
"""
import collections, glob, os, re, sys

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "src", "main", "java", "com", "eldanior",
                    "system", "classes", "definitions")
DRY = "--dry" in sys.argv

COUNT = {"COMMON": 1, "RARE": 1, "EPIC": 2, "UNIQUE": 2, "LEGENDARY": 3, "DIVINE": 3}
ORDER = ["COMMON", "RARE", "EPIC", "UNIQUE", "LEGENDARY", "DIVINE"]
MIN_RARITY = {"PuitsGravitationnel": "UNIQUE", "AutoriteGravitationnelle": "LEGENDARY"}

# Themes : (mots de l'identifiant, runes par preference). Le premier theme reconnu l'emporte ; sinon le dernier (defaut).
THEMES = {
    "WARRIOR": [
        ("berserk fureur enrage ravageur brute boucher devast destruct fleau loup sang annihil apocalyp cataclysm extinction tyran",
         ["Enrage", "GroundSlam", "AuraBlade"]),
        ("lame epee epeiste bretteur duelliste escrim fleurett virtuose danseur samourai kensei ronin shogun trancheur sabre "
         "maitre_d_armes mage mystique arcan runique celeste fantome",
         ["AuraBlade", "Enrage", "GroundSlam"]),
        ("", ["GroundSlam", "Enrage", "AuraBlade"]),
    ],
    "MAGE": [
        ("pyro flamme brasier infernal feu",
         ["Fireball", "Bourrasque", "PuitsGravitationnel"]),
        ("void neant noir liche necro morts mort ames chrono temps temporel archonte demiurge cosmique createur architecte "
         "pilier genese arcan magus",
         ["PuitsGravitationnel", "AutoriteGravitationnelle", "Fireball", "Bourrasque"]),
        ("druide nature naturelle originelle element guerisseur oracle prophete sage devin voyant visionnaire givre froid "
         "glacial cryo",
         ["Bourrasque", "Fireball", "PuitsGravitationnel"]),
        ("", ["Fireball", "Bourrasque", "PuitsGravitationnel"]),
    ],
    "ARCHER": [
        ("chasse traque pisteur nemrod fauves ranger forestier",
         ["ChainHook", "ChargedShot", "Bourrasque"]),
        ("eclaireur vent aurore coureur",
         ["Bourrasque", "ChargedShot", "ChainHook"]),
        ("", ["ChargedShot", "ChainHook", "Bourrasque"]),
    ],
    "ASSASSIN": [
        ("poison venin toxine vipere hydre empoison alchimiste",
         ["PoisonImbue", "WindStrike", "Bourrasque"]),
        ("void neant vide abyss ombre nuit spectre spectral faucheur moissonneur ange archange revenant phantom phantome "
         "esprit monarque souverain tyran regne chute aile noir",
         ["PuitsGravitationnel", "AutoriteGravitationnelle", "WindStrike", "PoisonImbue"]),
        ("acrobate danseur ninja shinobi kage rapide eclaireur corsaire flibustier pirate voleur larcin main_d_argent",
         ["WindStrike", "Bourrasque", "PoisonImbue"]),
        ("", ["WindStrike", "PoisonImbue", "Bourrasque"]),
    ],
}


def runes_for(class_id, rarity, ctype):
    words = class_id.lower()
    prefs = next(p for keys, p in THEMES[ctype] if not keys or any(k in words for k in keys.split()))
    allowed = [r for r in prefs if ORDER.index(rarity) >= ORDER.index(MIN_RARITY.get(r, "COMMON"))]
    for fallback in THEMES[ctype][-1][1]:                      # complete avec les runes de base du type
        if fallback not in allowed and fallback not in MIN_RARITY:
            allowed.append(fallback)
    return ["Rune_" + r for r in allowed[:COUNT[rarity]]]


stats = collections.Counter()
for path in sorted(glob.glob(os.path.join(ROOT, "**", "*.java"), recursive=True)):
    src = open(path, encoding="utf-8").read()
    m = re.search(r'super\(\s*"([^"]+)",\s*"([^"]+)".*?Rarity\.(\w+),\s*ClassType\.(\w+)', src, re.S)
    if not m or m.group(4) not in THEMES:
        continue
    class_id, name, rarity, ctype = m.groups()
    runes = runes_for(class_id, rarity, ctype)
    line = "        runes(" + ", ".join('"%s"' % r for r in runes) + ");"
    out = re.sub(r'\n[ \t]*runes\([^)]*\);', "", src)
    out, n = re.subn(r'\);(\s*)\}(\s*)\}\s*$', ");\n" + line + "\n    }\n}\n", out)
    if n != 1:
        print("IGNORE (forme inattendue)", path)
        continue
    for r in runes:
        stats[(ctype, r)] += 1
    if DRY:
        print(f"{ctype:8s} {rarity:9s} {class_id:28s} {', '.join(runes)}")
    elif out != src:
        open(path, "w", encoding="utf-8").write(out)

for k, v in sorted(stats.items()):
    print(f"{k[0]:8s} {k[1]:28s} {v}")
