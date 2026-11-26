#!/usr/bin/env python3
"""Genere les competences passives a partir de conception.py (refonte du 2026-11-24).

Produit :
  - PassiveSkill.java          (les series a rangs + Anges + Peches)
  - PassiveMigration.java      (ancien identifiant -> serie et rang, anciens passifs supprimes)
  - SkillManager.java          (pages de competence -> serie et rang ; parchemins de metier)
  - Metier.java                (metiers, sortis des passifs)
  - classes : PassiveSkill.X -> PassiveSkill.SERIE.r(rang)
  - pages : nom et description en jeu, pages en double cachees du mode creatif, retirees du butin et des prix
  - parchemins de Metier (objets + traductions), a la place des pages de metier dans le butin

Les anciennes donnees (enum, pages) sont lues dans ancien/ (copie d'avant la refonte) et les dossiers des anciennes
classes dans git (HEAD) : le script peut etre relance sans tout casser.
Usage : python3 scripts/passifs/gen_passifs.py [--check]
"""
import json
import os
import re
import subprocess
import sys

sys.path.insert(0, os.path.dirname(__file__))
from conception import SERIES, SUPPRIMES, DIVINS, METIERS, RANGS, QUALITE, TIER_RANG  # noqa: E402

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
J = os.path.join(ROOT, "src/main/java/com/eldanior/system")
RES = os.path.join(ROOT, "src/main/resources")
CHECK = "--check" in sys.argv


def git_show(path):
    return subprocess.run(["git", "show", f"HEAD:{path}"], cwd=ROOT, capture_output=True, text=True, check=True).stdout


def git_ls(prefix):
    out = subprocess.run(["git", "-c", "core.quotepath=off", "ls-tree", "-r", "HEAD", "--name-only", prefix], cwd=ROOT, capture_output=True, text=True,
                         check=True).stdout
    return out.split()


# ------------------------------------------------------------------------------------------------ anciennes donnees
ANCIEN = os.path.join(os.path.dirname(os.path.abspath(__file__)), "ancien")
OLD_ENUM = open(os.path.join(ANCIEN, "PassiveSkill.java.txt")).read()   # enum d'avant la refonte (copie du 2026-11-24)
OLD_SM = open(os.path.join(ANCIEN, "SkillManager.java.txt")).read()

old = {}  # id -> (nom, description, classe java)
for m in re.finditer(r'^\s+([A-Z0-9_]+)\("[^"]*",\s*"([^"]*)",\s*"((?:[^"\\]|\\.)*)"(.*?)new\s+([\w\.é́]+)\(\)\)', OLD_ENUM, re.M):
    old[m.group(1)] = (m.group(2), m.group(3), m.group(5).split(".")[-1])

cls_tier = {}
for f in git_ls("src/main/java/com/eldanior/system/skills/skills/passives"):
    rel = f.split("/passives/")[1]
    cls_tier[os.path.basename(f)[:-5]] = rel.split("/")[0]


def old_rank(oid):
    tier = cls_tier.get(old[oid][2])
    return TIER_RANG.get(tier)


pages = []  # (item, old id, classes)
for m in re.finditer(r'register\("([^"]+)",\s*new SkillModel\(\s*"([^"]+)",\s*(?:null|"[^"]*"),\s*(?://[^\n]*\n\s*)?"([^"]*)",\s*"([^"]*)"', OLD_SM, re.S):
    pages.append((m.group(1), m.group(2), m.group(4)))

# ------------------------------------------------------------------------------------------------ correspondances
by_id = {s.id: s for s in SERIES}
mapping = {}       # ancien id -> (serie, rang)
canon = {}         # (serie, rang) -> ancien id de la serie elle-meme (nom du rang)
for s in SERIES:
    for oid in s.membres:
        r = old_rank(oid)
        assert r, oid
        mapping[oid] = (s.id, r)
        if (s.id, r) not in canon:
            canon[(s.id, r)] = oid
    for oid, r in s.absorbes:
        mapping[oid] = (s.id, r)

metier_ids = {m[0] for m in METIERS}
unknown = [o for o in old if o not in mapping and o not in SUPPRIMES and o not in DIVINS and o not in metier_ids]
assert not unknown, f"anciens passifs sans destination : {unknown}"


def ranks_of(s):
    return sorted({r for (sid, r) in canon if sid == s.id})


def jstr(t):
    return '"' + t.replace("\\", "\\\\").replace('"', '\\"') + '"'


# ------------------------------------------------------------------------------------------------ PassiveSkill.java
DIVIN_TXT = {
    "PECHE_ORGUEIL": ("Orgueil", "+5 % de dégâts et -3 % de dégâts reçus par pièce d'armure portée (armure complète : +20 % et -12 %).", "new Orgueil()"),
    "PECHE_COLERE": ("Colère", "Au-dessus de 70 % de vie : +40 % de dégâts. Chaque coup reçu d'un joueur ajoute une charge de rage (+5 % de dégâts, 6 charges, 10 s).", "new Colere()"),
    "PECHE_AVARICE": ("Avarice", "+15 % de chance de butin. En tuant un joueur : 25 % de chance de lui voler un objet de sa barre d'action (recharge : 1 jour).", "new Avarice()"),
    "PECHE_ENVIE": ("Envie", "+1 % de dégâts par % de vie perdue (jusqu'à +80 %). Contre un joueur de plus haut niveau : +25 % de dégâts en plus.", "new Envie()"),
    "PECHE_LUXURE": ("Luxure", "+10 % de vitesse de déplacement. En tuant un joueur : tu lui voles 10 % de son or (recharge : 12 h).", "new Luxure()"),
    "PECHE_GOURMANDISE": ("Gourmandise", "Chaque joueur tué te rend 20 % de ta vie max. 25 % de chance de copier l'un de ses passifs à son rang (recharge : 1 jour).", "new Gourmandise()"),
    "PECHE_PARESSE": ("Paresse", "-15 % de dégâts reçus des joueurs, et les joueurs qui te frappent sont ralentis.", "new Paresse()"),
    "ANGE_HUMILITE": ("Humilité (Michaël)", "+15 % de dégâts et -10 % de dégâts reçus par pièce d'armure en moins (sans armure : +60 % et -40 %).", "new Humilite()"),
    "ANGE_PATIENCE": ("Patience (Raphaël)", "+5 % de dégâts et -5 % de dégâts reçus par membre de ton groupe à moins de 15 blocs (4 au plus).", "new Patience()"),
    "ANGE_TEMPERANCE": ("Tempérance (Uriel)", "La main vide : +200 % de dégâts et dégâts reçus divisés par deux.", "new Temperance()"),
    "ANGE_CHARITE": ("Charité (Gabriel)", "Dégâts reçus divisés par 2,5. Quand tu es touché, les membres blessés de ton groupe à moins de 15 blocs récupèrent 3 % de leur vie max.", "new Charite()"),
    "ANGE_BIENVEILLANCE": ("Bienveillance (Chamuel)", "+10 % d'XP. Ton groupe reçoit 30 % de l'XP de tes victimes au lieu de 10 %.", "PassiveEffects.AUCUN"),
    "ANGE_CHASTETE": ("Chasteté (Azraël)", "Contre les monstres : +150 % de dégâts et -25 % de dégâts reçus.", "new Chastete()"),
    "ANGE_DILIGENCE": ("Diligence (Métatron)", "+20 % d'XP, maîtrise des passifs cinq fois plus rapide, +100 % de vitesse de déplacement.", "new Diligence()"),
}
DIVIN_CD = {"PECHE_AVARICE": 86400, "PECHE_LUXURE": 43200}

lines = []
desc_rows = {}
for s in SERIES:
    rs = ranks_of(s)
    names = ["null"] * 7
    descs = ["null"] * 7
    for r in range(1, 7):
        if r in rs:
            names[r] = jstr(old[canon[(s.id, r)]][0])
            descs[r] = jstr(s.effet.phrase(r))
    desc_rows[s.id] = {r: s.effet.phrase(r) for r in rs}
    lines.append(f"    {s.id}(Theme.{s.theme}, {jstr(s.nom)}, {rs[0]}, {rs[-1]},\n"
                 f"            new String[]{{{', '.join(names)}}},\n"
                 f"            new String[]{{{', '.join(descs)}}},\n"
                 f"            {s.effet.java})")
for d in DIVINS:
    nom, txt, logic = DIVIN_TXT[d]
    theme = "PECHE" if d.startswith("PECHE_") else "ANGE"
    cd = DIVIN_CD.get(d, 0)
    lines.append(f"    {d}(Theme.{theme}, {jstr(nom)}, 7, 7, null, new String[]{{null, null, null, null, null, null, null, {jstr(txt)}}},\n"
                 f"            {logic}{', ' + str(cd) + 'f' if cd else ''})")

ENUM_BODY = ",\n".join(lines)
ENUM = f'''package com.eldanior.system.skills.skillsInteraction;

import com.eldanior.system.skills.skills.passives.Divin.Ange.*;
import com.eldanior.system.skills.skills.passives.Divin.Peche.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Competences passives (refonte du 2026-11-24) : chaque passif est une SERIE a rangs, du rang 1 (Commun) au rang 6
 * (Legendaire). Une page de rang superieur fait monter le passif. Les 7 Anges et 7 Peches sont des passifs uniques (Divin).
 *
 * FICHIER GENERE par scripts/passifs/gen_passifs.py depuis scripts/passifs/conception.py : modifier la conception, pas ce
 * fichier. Les valeurs de chaque rang sont dans les tableaux passes a PassiveEffects (index 0 = rang 1).
 * Les stats sont designees par leur nom : StatConfig ne doit pas etre charge au demarrage (ses IDs de stats seraient faux).
 */
public enum PassiveSkill {{

{ENUM_BODY};

    /** Theme d'un passif (tri et filtre de l'interface). */
    public enum Theme {{
        ATTAQUE("Attaque"), MAGIE("Magie"), DEFENSE("Défense"), VIE("Vie"), ENDURANCE("Endurance"), AGILITE("Agilité"),
        CHANCE("Chance"), PECHE("Péché capital"), ANGE("Ange");
        public final String label;
        Theme(String label) {{ this.label = label; }}
    }}

    /** Un passif a un rang donne (passifs de classe, passifs d'un joueur). */
    public record Ranked(PassiveSkill skill, int rank) {{}}

    /** Rang maximal d'un joueur pour un passif de serie (Legendaire). */
    public static final int MAX_RANK = 6;
    /** Rang affiche des Anges et Peches. */
    public static final int DIVINE_RANK = 7;

    private static final String[] RANK_LABELS = {{"", "Commun", "Inhabituel", "Rare", "Épique", "Unique", "Légendaire", "Divin"}};
    private static final String[] RANK_KEYS = {{"", "COMMUN", "UNCOMMON", "RARE", "EPIQUE", "UNIQUE", "LEGENDAIRE", "DIVIN"}};

    private final Theme theme;
    private final String displayName;
    private final int minRank, maxRank;
    private final String[] rankNames;
    private final String[] descriptions;
    private final IPassiveCombatSkill logic;
    private final float cooldownSeconds;

    PassiveSkill(Theme theme, String displayName, int minRank, int maxRank, String[] rankNames, String[] descriptions,
                 IPassiveCombatSkill logic) {{
        this(theme, displayName, minRank, maxRank, rankNames, descriptions, logic, 0f);
    }}

    PassiveSkill(Theme theme, String displayName, int minRank, int maxRank, String[] rankNames, String[] descriptions,
                 IPassiveCombatSkill logic, float cooldownSeconds) {{
        this.theme = theme;
        this.displayName = displayName;
        this.minRank = minRank;
        this.maxRank = maxRank;
        this.rankNames = rankNames;
        this.descriptions = descriptions;
        this.logic = logic;
        this.cooldownSeconds = cooldownSeconds;
    }}

    public Ranked r(int rank) {{ return new Ranked(this, clamp(rank)); }}

    public String getId() {{ return name(); }}
    public Theme getTheme() {{ return theme; }}
    public String getDisplayName() {{ return displayName; }}
    public int getMinRank() {{ return minRank; }}
    public int getMaxRank() {{ return maxRank; }}
    public IPassiveCombatSkill getLogic() {{ return logic; }}
    /** Recharge apres un declenchement (seulement certains Peches). */
    public float getCooldownSeconds() {{ return cooldownSeconds; }}

    /** Rang ramene entre le premier et le dernier rang de la serie. */
    public int clamp(int rank) {{ return Math.max(minRank, Math.min(maxRank, rank)); }}

    /** Nom du rang (ancien nom du passif de ce rang : Frappe de Fureur...) ; le nom de la serie s'il n'en a pas. */
    public String getRankName(int rank) {{
        int r = clamp(rank);
        return rankNames != null && rankNames[r] != null ? rankNames[r] : displayName;
    }}

    /** Effet au rang donne (le plus proche si ce rang n'existe pas). */
    public String getDescription(int rank) {{
        int r = clamp(rank);
        for (int i = r; i >= 1; i--) if (descriptions[i] != null) return descriptions[i];
        for (int i = r; i < descriptions.length; i++) if (descriptions[i] != null) return descriptions[i];
        return "";
    }}

    public boolean hasRank(int rank) {{ return rank >= 1 && rank < descriptions.length && descriptions[rank] != null; }}

    public static String rankLabel(int rank) {{ return rank >= 0 && rank < RANK_LABELS.length ? RANK_LABELS[rank] : ""; }}
    /** Cle de rarete de l'interface (COMMUN, UNCOMMON...). */
    public static String rankKey(int rank) {{ return rank >= 0 && rank < RANK_KEYS.length ? RANK_KEYS[rank] : ""; }}

    public boolean isDivine() {{ return theme == Theme.PECHE || theme == Theme.ANGE; }}
    public boolean isPeche() {{ return theme == Theme.PECHE; }}
    public boolean isAnge() {{ return theme == Theme.ANGE; }}

    private static final Map<String, PassiveSkill> LOOKUP;
    static {{
        Map<String, PassiveSkill> map = new HashMap<>();
        for (PassiveSkill ps : values()) map.put(ps.name(), ps);
        LOOKUP = Collections.unmodifiableMap(map);
    }}

    /** Le passif de cet identifiant, ou null (sans exception). */
    public static PassiveSkill fromName(String name) {{ return name == null ? null : LOOKUP.get(name.toUpperCase()); }}

    public static boolean isDivineSkill(String id) {{ PassiveSkill p = fromName(id); return p != null && p.isDivine(); }}
    public static boolean isPecheSkill(String id) {{ PassiveSkill p = fromName(id); return p != null && p.isPeche(); }}
    public static boolean isAngeSkill(String id) {{ PassiveSkill p = fromName(id); return p != null && p.isAnge(); }}
}}
'''

# ------------------------------------------------------------------------------------------------ PassiveMigration.java
mig_lines = []
for oid, (sid, r) in sorted(mapping.items()):
    mig_lines.append(f'        m("{oid}", "{sid}", {r});')
sup = ", ".join(f'"{x}"' for x in sorted(SUPPRIMES))
MIG = f'''package com.eldanior.system.skills.skillsInteraction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Anciens identifiants de passifs (avant la refonte du 2026-11-24) -> serie et rang. Sert a convertir les sauvegardes :
 * « FURY_STRIKE » devient « INSTINCTIVE_STRIKE » au rang 3.
 *
 * FICHIER GENERE par scripts/passifs/gen_passifs.py : ne pas modifier a la main.
 */
public final class PassiveMigration {{

    private PassiveMigration() {{}}

    /** Serie et rang d'un ancien passif. */
    public record Target(String skillId, int rank) {{}}

    private static final Map<String, Target> MAP = new HashMap<>();
    /** Anciens passifs supprimes (ne faisaient rien, faux actifs, divins isoles, aura de dignite). */
    public static final Set<String> REMOVED = Set.of({sup});

    private static void m(String oldId, String skillId, int rank) {{ MAP.put(oldId, new Target(skillId, rank)); }}

    static {{
{chr(10).join(mig_lines)}
    }}

    /** Destination d'un ancien identifiant (null s'il n'en a pas : identifiant actuel, metier, ou inconnu). */
    public static Target of(String oldId) {{ return oldId == null ? null : MAP.get(oldId.toUpperCase()); }}

    public static boolean isRemoved(String id) {{ return id != null && REMOVED.contains(id.toUpperCase()); }}
}}
'''

# ------------------------------------------------------------------------------------------------ pages
page_map = {}      # item -> (sid, rang) ou ("METIER", id) ou ("PERDU", None)
visible = set()    # pages gardees (butin, mode creatif, prix)
seen_canon = set()
for item, oid, classes in pages:
    if oid in DIVINS:
        page_map[item] = (oid, 7, classes)
        visible.add(item)
    elif oid in metier_ids:
        page_map[item] = ("METIER", oid, classes)
    elif oid in mapping:
        sid, r = mapping[oid]
        page_map[item] = (sid, r, classes)
        is_canon = canon.get((sid, r)) == oid and old_rank(oid) == r and cls_tier.get(old[oid][2]) != "Divin"
        if is_canon and (sid, r) not in seen_canon:
            seen_canon.add((sid, r))
            visible.add(item)
    else:
        page_map[item] = ("PERDU", None, classes)
missing_canon = [(s.id, r) for s in SERIES for r in ranks_of(s) if (s.id, r) not in seen_canon]
assert not missing_canon, f"rangs sans page : {missing_canon}"

reg = []
for item, (a, b, classes) in page_map.items():
    if a == "METIER":
        continue  # les anciennes pages de metier sont lues comme les parchemins de metier (plus bas)
    if a == "PERDU":
        reg.append(f'        lost("{item}");')
    else:
        reg.append(f'        page("{item}", "{a}", {b}, {jstr(classes)}, {"true" if item in visible else "false"});')
met_reg = []
old_metier_pages = {oid: item for item, (a, oid, _c) in page_map.items() if a == "METIER"}
for mid, nom, etabli, q, obj in METIERS:
    met_reg.append(f'        metier("{obj}", "{mid}");')
    if mid in old_metier_pages:
        met_reg.append(f'        metier("{old_metier_pages[mid]}", "{mid}");')

SM = f'''package com.eldanior.system.skills;

import java.util.*;

/**
 * Pages de competence -> passif et rang ; parchemins de Metier -> metier.
 *
 * FICHIER GENERE par scripts/passifs/gen_passifs.py (refonte des passifs du 2026-11-24) : ne pas modifier a la main.
 * Les anciennes pages d'un meme rang (series fusionnees) et les pages Divines des series restent lisibles, mais ne
 * sont plus dans le butin ni dans le mode creatif.
 */
public final class SkillManager {{

    private SkillManager() {{}}

    /** Une page : le passif qu'elle apprend, son rang, les classes qui peuvent la lire ("all" = toutes), et si c'est la page
     *  de reference de ce rang (dans le butin et le mode creatif). */
    public record Page(String itemId, String skillId, int rank, String requiredClass, boolean main) {{}}

    private static final Map<String, Page> PAGES = new HashMap<>();
    private static final Map<String, String> METIERS = new HashMap<>();
    private static final Set<String> LOST = new HashSet<>();

    public static void init() {{
        if (!PAGES.isEmpty()) return;
{chr(10).join(reg)}
{chr(10).join(met_reg)}
    }}

    private static void page(String item, String skillId, int rank, String classes, boolean main) {{
        PAGES.put(item, new Page(item, skillId, rank, classes, main));
    }}

    private static void metier(String item, String metierId) {{ METIERS.put(item, metierId); }}

    private static void lost(String item) {{ LOST.add(item); }}

    public static Optional<Page> getPage(String itemId) {{ return Optional.ofNullable(PAGES.get(itemId)); }}

    /** Metier appris par ce parchemin (ou ancienne page de metier). */
    public static Optional<String> getMetier(String itemId) {{ return Optional.ofNullable(METIERS.get(itemId)); }}

    /** Page d'un passif supprime le 2026-11-24 : elle ne s'apprend plus. */
    public static boolean isLost(String itemId) {{ return LOST.contains(itemId); }}

    public static Collection<Page> getAllPages() {{ return Collections.unmodifiableCollection(PAGES.values()); }}

    /** Page de reference d'un passif a un rang (null s'il n'y en a pas). */
    public static String pageFor(String skillId, int rank) {{
        for (Page p : PAGES.values()) if (p.main() && p.skillId().equals(skillId) && p.rank() == rank) return p.itemId();
        return null;
    }}

    /** Parchemin d'un metier. */
    public static String scrollFor(String metierId) {{
        com.eldanior.system.skills.skillsInteraction.Metier m = com.eldanior.system.skills.skillsInteraction.Metier.fromName(metierId);
        return m != null ? m.getScrollItem() : null;
    }}
}}
'''

# ------------------------------------------------------------------------------------------------ Metier.java
met_enum = ",\n".join(
    f'    {mid}({jstr(nom)}, {jstr("Permet d" + chr(39) + "utiliser " + etabli + ".")}, "{obj}")' for mid, nom, etabli, q, obj in METIERS)
METIER = f'''package com.eldanior.system.skills.skillsInteraction;

/**
 * Metiers : acces aux etablis. Sortis des competences passives le 2026-11-24 (demande de l'utilisateur) ; ils
 * s'apprennent avec un parchemin de Metier et ne comptent pas dans la limite de passifs.
 * L'identifiant (CRAFT_...) est garde : il sert aux sauvegardes, a la progression d'artisanat et aux titres.
 *
 * FICHIER GENERE par scripts/passifs/gen_passifs.py.
 */
public enum Metier {{

{met_enum};

    private final String displayName;
    private final String description;
    private final String scrollItem;

    Metier(String displayName, String description, String scrollItem) {{
        this.displayName = displayName;
        this.description = description;
        this.scrollItem = scrollItem;
    }}

    public String getDisplayName() {{ return displayName; }}
    public String getDescription() {{ return description; }}
    /** Objet du parchemin qui apprend ce metier. */
    public String getScrollItem() {{ return scrollItem; }}

    public static Metier fromName(String id) {{
        if (id == null) return null;
        for (Metier m : values()) if (m.name().equalsIgnoreCase(id)) return m;
        return null;
    }}
}}
'''


def write(path, text):
    full = os.path.join(ROOT, path)
    cur = open(full).read() if os.path.exists(full) else None
    if cur != text:
        if CHECK:
            print("a regenerer :", path)
        else:
            os.makedirs(os.path.dirname(full), exist_ok=True)
            open(full, "w").write(text)
            print("ecrit :", path)


write("src/main/java/com/eldanior/system/skills/skillsInteraction/PassiveSkill.java", ENUM)
write("src/main/java/com/eldanior/system/skills/skillsInteraction/PassiveMigration.java", MIG)
write("src/main/java/com/eldanior/system/skills/SkillManager.java", SM)
write("src/main/java/com/eldanior/system/skills/skillsInteraction/Metier.java", METIER)

# ------------------------------------------------------------------------------------------------ classes
CLS_DIR = os.path.join(J, "classes")
n_cls = 0
for dp, _, fs in os.walk(CLS_DIR):
    for f in fs:
        if not f.endswith(".java"):
            continue
        p = os.path.join(dp, f)
        if os.sep + "catalog" + os.sep in p:      # catalogue genere (scripts/classes/class_catalog.py) : deja converti
            continue
        txt = open(p).read()
        if "PassiveSkill." not in txt:
            continue

        found_metiers = []

        def fix_list(m):
            body = m.group(1)
            ids = re.findall(r"PassiveSkill\.([A-Z0-9_]+)(\.r\((\d)\))?", body)
            if not ids:
                return m.group(0)
            best = {}
            for oid, _r, rr in ids:
                if rr:  # deja converti
                    sid, r = oid, int(rr)
                elif oid in mapping:
                    sid, r = mapping[oid]
                elif oid in SUPPRIMES:
                    continue
                elif oid in DIVINS:
                    sid, r = oid, 7
                elif oid in metier_ids:
                    found_metiers.append(oid)
                    continue
                else:
                    raise SystemExit(f"{p} : passif inconnu {oid}")
                best[sid] = max(best.get(sid, 0), r)
            items = ", ".join(f"PassiveSkill.{sid}.r({r})" for sid, r in best.items())
            return f"List.of({items})"

        new = re.sub(r"List\.of\(((?:\s*PassiveSkill\.[A-Z0-9_]+(?:\.r\(\d\))?\s*,?)+)\)", fix_list, txt)
        if found_metiers and "metiers(" not in new:
            call = "        metiers(" + ", ".join(f'"{x}"' for x in dict.fromkeys(found_metiers)) + ");   // metiers de la classe (2026-11-24)\n"
            new = re.sub(r"(super\((?:.|\n)*?\n\s*\);\n)", lambda mm: mm.group(1) + call, new, count=1)
        if new != txt:
            n_cls += 1
            if not CHECK:
                open(p, "w").write(new)
print("classes converties :", n_cls)

# ------------------------------------------------------------------------------------------------ objets des pages
ITEM_DIR = os.path.join(RES, "Server/Item/Items/PassiveSkill")
n_hidden = 0
for item, (a, b, classes) in page_map.items():
    p = os.path.join(ITEM_DIR, item + ".json")
    if not os.path.exists(p):
        continue
    data = json.load(open(p))
    changed = False
    if item in visible:
        if "Categories" not in data:
            data["Categories"] = ["Eldanior_System.Parchemin"]
            changed = True
        if a not in DIVINS and a not in ("METIER", "PERDU") and data.get("Quality") != QUALITE[b]:
            data["Quality"] = QUALITE[b]
            changed = True
    elif "Categories" in data:
        del data["Categories"]
        n_hidden += 1
        changed = True
    if changed and not CHECK:
        open(p, "w").write(json.dumps(data, ensure_ascii=False, indent=2))
print("pages cachees du mode creatif :", n_hidden, "| pages visibles :", len(visible))

# parchemins de metier
MET_DIR = os.path.join(RES, "Server/Item/Items/PassiveSkill/Metier")
for mid, nom, etabli, q, obj in METIERS:
    data = {
        "Id": obj,
        "TranslationProperties": {"Name": f"server.items.{obj}.name", "Description": f"server.items.{obj}.description"},
        "Icon": "Icons/ItemsGenerated/Recipe_Page.png",
        "Quality": q,
        "Categories": ["Eldanior_System.Parchemin"],
        "Tags": {"Type": ["Item", "Consumable"]},
        "MaxStack": 1,
        "PlayerAnimationsId": "Item",
        "Model": "Items/Consumables/Recipes/Recipe.blockymodel",
        "Texture": "Items/Consumables/Recipes/Recipe_Texture.png",
        "IconProperties": {"Scale": 0.76, "Rotation": [135, 135, 0], "Translation": [-1, 5]},
        "Interactions": {"Use": "Root_ConsumableItemSkillInteraction"},
    }
    p = os.path.join(MET_DIR, obj + ".json")
    txt = json.dumps(data, ensure_ascii=False, indent=2) + "\n"
    if not CHECK:
        os.makedirs(MET_DIR, exist_ok=True)
        if not os.path.exists(p) or open(p).read() != txt:
            open(p, "w").write(txt)

# ------------------------------------------------------------------------------------------------ traductions
LANG = os.path.join(RES, "Server/Languages/en-US/server.lang")
lang = open(LANG).read().split("\n")
keep = [l for l in lang if not re.match(r"items\.(skill_page_[a-z0-9_]+|Eldanior_Parchemin_Metier_\w+)\.(name|description)=", l)]
new_lang = []
FOOT = "\\n\\n<b>[Clic F]</b> pour apprendre."
for item, (a, b, classes) in page_map.items():
    cls_txt = "Toutes les classes" if classes.strip().lower() in ("all", "") else "Classes : " + classes
    if a == "PERDU":
        new_lang.append(f"items.{item}.name=Page effacée")
        new_lang.append(f"items.{item}.description=<i>Ce savoir s'est perdu : la page ne s'apprend plus.</i>")
    elif a == "METIER":
        m = next(x for x in METIERS if x[0] == b)
        new_lang.append(f"items.{item}.name=Ancienne page : {m[1]}")
        new_lang.append(f"items.{item}.description=<b>Métier</b>\\n<i>Permet d'utiliser {m[2]}.</i>{FOOT}")
    elif a in DIVINS:
        nom, txt, _ = DIVIN_TXT[a]
        cond = "Réservé aux PK." if a.startswith("PECHE_") else "Réservé aux Religieux de l'Église et au-dessus."
        new_lang.append(f"items.{item}.name={nom}")
        new_lang.append(f"items.{item}.description=<b>Rareté : Divin</b>\\n<i>{txt}</i>\\n{cond} Un seul pouvoir divin à la fois.{FOOT}")
    else:
        s = by_id[a]
        rang_nom = old[canon[(a, b)]][0] if (a, b) in canon else s.nom
        titre = s.nom if rang_nom == s.nom else f"{s.nom} : {rang_nom}"
        new_lang.append(f"items.{item}.name={titre} ({RANGS[b]})")
        new_lang.append(f"items.{item}.description=<b>Passif · Rang {b} ({RANGS[b]})</b>\\n<i>{s.effet.phrase(b)}</i>\\n"
                        f"Apprend ce passif, ou le fait monter à ce rang. {cls_txt}.{FOOT}")
for mid, nom, etabli, q, obj in METIERS:
    new_lang.append(f"items.{obj}.name=Parchemin de Métier : {nom}")
    new_lang.append(f"items.{obj}.description=<b>Métier</b>\\n<i>Permet d'utiliser {etabli}.</i>\\n"
                    f"Ne compte pas dans la limite de passifs.{FOOT}")
while keep and keep[-1] == "":
    keep.pop()
out = "\n".join(keep) + "\n\n# --- Pages de competence et parchemins de metier (scripts/passifs/gen_passifs.py) ---\n" + "\n".join(new_lang) + "\n"
out = out.replace("\n\n# --- Pages de competence et parchemins de metier (scripts/passifs/gen_passifs.py) ---\n\n# --- Pages",
                  "\n\n# --- Pages")
if not CHECK and out != open(LANG).read():
    open(LANG, "w").write(out)
    print("traductions ecrites")

# ------------------------------------------------------------------------------------------------ butin
LOOT = os.path.join(J, "config/configs/LootTableConfig.java")
loot = open(LOOT).read()
met_item = {old_metier_pages[mid]: obj for mid, nom, e, q, obj in METIERS if mid in old_metier_pages}
removed_loot = 0


def loot_line(m):
    global removed_loot
    item = m.group(2)
    if item in met_item:
        return m.group(0).replace(f'"{item}"', f'"{met_item[item]}"')
    if item in page_map and item not in visible:
        removed_loot += 1
        return ""
    return m.group(0)


loot2 = re.sub(r'(\n[ \t]*new LootEntry\("(skill_page_[a-z0-9_]+)"[^\n]*)', loot_line, loot)
if loot2 != loot and not CHECK:
    open(LOOT, "w").write(loot2)
print("lignes de butin retirees :", removed_loot)

# failles : verifier qu'aucune page cachee n'y est
for dp, _, fs in os.walk(os.path.join(RES, "failles-pack")):
    for f in fs:
        if f.endswith(".json"):
            t = open(os.path.join(dp, f)).read()
            for item in re.findall(r'"(skill_page_[a-z0-9_]+)"', t):
                if item not in visible:
                    print("ATTENTION page cachee dans", os.path.join(dp, f), item)

# ------------------------------------------------------------------------------------------------ prix
PRIX = os.path.join(ROOT, "scripts/economie/prix.json")
prix = json.load(open(PRIX))
fam = prix["ressources"]["Runes & compétences"]
n_prix = 0
for item, (a, b, c) in page_map.items():
    if item in met_item and item in fam:
        fam.setdefault(met_item[item], fam[item])
    if item not in visible and item in fam:
        del fam[item]
        n_prix += 1
for mid, nom, e, q, obj in METIERS:
    fam.setdefault(obj, 800)
if not CHECK:
    open(PRIX, "w").write(json.dumps(prix, ensure_ascii=False, indent=1) + "\n")
print("prix retires :", n_prix)

# rapport
print(f"series : {len(SERIES)} | rangs : {len(seen_canon)} | anciens ids convertis : {len(mapping)} | supprimes : {len(SUPPRIMES)}")
