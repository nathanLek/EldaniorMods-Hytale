"""Catalogue des classes d'Eldanior (refonte du 2026-11-25) : scripts/classes/classes.json -> ClassCatalog.java.

Une seule source pour les ~400 classes ordinaires (bases, paliers 2 et 3). Les anciens fichiers definitions/<famille>/*.java
sont supprimes. Les classes speciales restent ecrites a la main en Java : Novice, Dragon Ancestral, Observateur,
Mousquetaire et la voie martiale du Clan Luo (definitions/luo/VoieMartiale).

Une classe de classes.json :
    id, nom, famille (guerrier|mage|archer|assassin|marchand), palier (1 base, 2, 3), rarete (COMMON..DIVINE), description,
    passifs [[PASSIF, rang]], runes [Rune_*], armes [WeaponMastery], metiers [Metier], profil [FOR VIT INT END AGI CHA],
    evolutions [ids du palier suivant].
Le « profil » donne seulement la REPARTITION des caracteristiques : le total vient du bareme ci-dessous (BUDGET), le meme
pour toutes les familles. Une Divine vaut 30 fois une Commune du meme palier (jackpot valide le 2026-11-25).

Verifie : ids et noms uniques (classes speciales comprises), liens d'evolution (un seul parent, palier suivant), passifs,
rangs, runes (objets Rune_*), armes, metiers, profil.

Usage : python3 scripts/classes/class_catalog.py [--check] [--report]
    --check  : verifie seulement (rien n'est ecrit)
    --report : en plus, le recap identite (runes propres, armes, passifs les plus repetes)
"""
import collections, glob, json, os, re, sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.join(HERE, "..", "..")
JAVA = os.path.join(ROOT, "src", "main", "java", "com", "eldanior", "system")
RES = os.path.join(ROOT, "src", "main", "resources")
DATA = os.path.join(HERE, "classes.json")
OUT = os.path.join(JAVA, "classes", "catalog", "ClassCatalog.java")
CHECK = "--check" in sys.argv
REPORT = "--report" in sys.argv

RARITIES = ["COMMON", "RARE", "EPIC", "UNIQUE", "LEGENDARY", "DIVINE"]
FAMILIES = {"guerrier": "WARRIOR", "mage": "MAGE", "archer": "ARCHER", "assassin": "ASSASSIN", "marchand": "MERCHANT"}
# Classes ecrites a la main (hors catalogue)
SPECIAL_FILES = ["Novice.java", "DragonAncestral.java", "Observateur.java", "Mousquetaire.java", "luo/VoieMartiale.java"]
STATS = ["FOR", "VIT", "INT", "END", "AGI", "CHA"]

# ---------------------------------------------------------------------------------------------------- bareme (2026-11-25)
# Total des bonus de caracteristiques = BASE[palier] x MULT[rarete]. Divine = 30 x Commune (jackpot garde, ramene de x40).
MULT = {"COMMON": 1, "RARE": 2.5, "EPIC": 5, "UNIQUE": 9, "LEGENDARY": 16, "DIVINE": 30}
# 4 = Eveil (x1,375 du palier 3), 5 = Transcendance (x1,75) : Divine eveillee 3 300, transcendante 4 200 (Dragon Ancestral 4 500)
BASE = {1: 36, 2: 50, 3: 80, 4: 110, 5: 140}
# Niveau de l'evolution suivante : base 180, palier 2 400, palier 3 -> Eveil 650, Eveil -> Transcendance 900.
PROMOTION = {1: 180, 2: 400, 3: 650, 4: 900, 5: 0}

# ---------------------------------------------------------------------------------------------------- paliers derives
# Eveil (palier 4, valide le 2026-11-25) : chaque classe du palier 3 a sa forme eveillee, generee ici (pas dans classes.json) :
# meme classe, nom « ... Eveille(e) », passifs +1 rang, bareme du palier 4.
# Transcendance (palier 5, niveau 900) : une classe par lignee du palier 2, declaree dans classes.json par le champ
# « transcendance » de la classe du palier 2 ({id, nom, description}) ; les formes eveillees de la lignee y menent.
FEMININ = {"Lame", "Ombre", "Faucheuse", "Fureur", "Sentinelle", "Force", "Liche", "Main", "Voix", "Vipere", "Hydre", "Fleche",
           "Epee", "Aile", "Chute", "Toxine", "Transcendance", "Apocalypse", "Extinction", "Legende", "Vision", "Avant", "Arche",
           "Arbalete", "Forteresse", "Nuit", "Geneses"}


def eveil_id(cid):
    return cid + "_eveil"


def eveil_nom(nom):
    first = nom.split()[0].split("-")[0]
    return nom + (" Éveillée" if first in FEMININ else " Éveillé")


def derive(classes, max_ranks=None):
    """Classes du catalogue + formes eveillees (palier 4) + transcendantes (palier 5), liens d'evolution compris."""
    by = {c["id"]: c for c in classes}
    parent = {n: c["id"] for c in classes for n in c["evolutions"]}
    trans = {}
    out = [dict(c) for c in classes]
    for c in classes:
        t = c.get("transcendance")
        if c["palier"] == 2 and t:
            kids = [by[k] for k in c["evolutions"] if k in by]
            prof = [sum(k["profil"][i] / sum(k["profil"]) for k in kids) / len(kids) * 1000 for i in range(6)]
            best = {}
            for k in kids:
                for p, r in k["passifs"]:
                    best[p] = max(best.get(p, 0), r + 1)
            runes = list(dict.fromkeys(t.get("runes", []) + [r for k in kids for r in k["runes"]]))[:3]
            trans[c["id"]] = dict(id=t["id"], nom=t["nom"], famille=c["famille"], palier=5, rarete=kids[0]["rarete"],
                                  description=t["description"], passifs=[[p, min(r, (max_ranks or {}).get(p, r))] for p, r in best.items()],
                                  runes=runes, armes=t.get("armes", kids[0]["armes"]), metiers=kids[0]["metiers"],
                                  profil=[round(x) for x in prof], evolutions=[])
    for c in out:
        if c["palier"] != 3:
            continue
        lineage = parent.get(c["id"])
        ev = dict(c, id=eveil_id(c["id"]), nom=eveil_nom(c["nom"]), palier=4,
                  description=c["description"] + " Éveil : sa puissance a franchi les limites de sa lignée.",
                  passifs=[[p, min(r + 1, (max_ranks or {}).get(p, r + 1))] for p, r in c["passifs"]],
                  evolutions=[trans[lineage]["id"]] if lineage in trans else [])
        ev.pop("transcendance", None)
        c["evolutions"] = [ev["id"]]
        out.append(ev)
    out.extend(trans.values())
    for c in out:
        c.pop("transcendance", None)
    return out


def budget(palier, rarete):
    return round(BASE[palier] * MULT[rarete])


def distribute(total, profil):
    """Repartit total selon les poids du profil (plus grands restes) : la somme vaut exactement total."""
    w = sum(profil)
    raw = [total * p / w for p in profil]
    out = [int(x) for x in raw]
    for i in sorted(range(6), key=lambda i: raw[i] - out[i], reverse=True)[: total - sum(out)]:
        out[i] += 1
    return out


# ---------------------------------------------------------------------------------------------------- references du mod
def read(p):
    return open(p, encoding="utf-8").read()


def passives():
    """{PASSIF: rang maxi} lus dans PassiveSkill.java."""
    src = read(os.path.join(JAVA, "skills", "skillsInteraction", "PassiveSkill.java"))
    return {m.group(1): int(m.group(3)) for m in
            re.finditer(r'^\s{4}([A-Z][A-Z0-9_]*)\(Theme\.\w+,\s*"[^"]*",\s*(\d+),\s*(\d+)', src, re.M)}


def enum_names(path):
    src = read(path)
    body = src[src.index("{") + 1:]
    body = body[: body.index(";")] if ";" in body else body
    return set(re.findall(r'^\s*([A-Z][A-Z0-9_]*)\s*(?:\(|,|$)', body, re.M))


def rune_items():
    return {os.path.basename(p)[:-5] for p in glob.glob(os.path.join(RES, "Server", "Item", "Items", "**", "Rune_*.json"),
                                                         recursive=True)}


def special_classes():
    """{id: nom} des classes ecrites a la main (Novice, Dragon, Observateur, Mousquetaire, voie martiale)."""
    out = {}
    for name in SPECIAL_FILES:
        f = os.path.join(JAVA, "classes", "definitions", name)
        src = read(f)
        if f.endswith("VoieMartiale.java"):
            for sid, name in re.findall(r'new Step\((?:FIRST|"(\w+)"),\s*"([^"]+)"', src):
                out[sid or "apprenti_arts_martiaux"] = name
            continue
        m = re.search(r'super\(\s*(?:"([^"]+)"|ID)\s*,\s*"([^"]+)"', src)
        if m:
            cid = m.group(1) or re.search(r'ID\s*=\s*"([^"]+)"', src).group(1)
            out[cid] = m.group(2)
    return out


# ---------------------------------------------------------------------------------------------------- verification
def load():
    return json.load(open(DATA, encoding="utf-8"))


def save(classes):
    """Reecrit classes.json : une cle par ligne, listes sur une ligne (diff lisible). Pour les scripts qui l'editent."""
    lines = ["["]
    for i, c in enumerate(classes):
        body = ",\n".join(f"  {json.dumps(k, ensure_ascii=False)}: {json.dumps(v, ensure_ascii=False)}" for k, v in c.items())
        lines.append(" {\n" + body + "\n }" + ("," if i < len(classes) - 1 else ""))
    lines.append("]")
    open(DATA, "w", encoding="utf-8").write("\n".join(lines) + "\n")


def validate(classes):
    errs, warns = [], []
    pas = passives()
    weapons = enum_names(os.path.join(JAVA, "config", "configs", "WeaponMastery.java"))
    metiers = enum_names(os.path.join(JAVA, "skills", "skillsInteraction", "Metier.java"))
    runes = rune_items()
    special = special_classes()
    by = {}
    names = collections.defaultdict(list)
    for sid, n in special.items():
        names[n.lower()].append(sid)
    for c in classes:
        who = c.get("id", "?")
        for k in ("id", "nom", "famille", "palier", "rarete", "description", "passifs", "runes", "armes", "metiers",
                  "profil", "evolutions"):
            if k not in c:
                errs.append(f"{who} : champ manquant {k}")
        t = c.get("transcendance")
        if t:
            if c["palier"] != 2:
                errs.append(f"{who} : transcendance hors du palier 2")
            for k in ("id", "nom", "description"):
                if k not in t:
                    errs.append(f"{who} : transcendance sans {k}")
            if t.get("id") in by or t.get("id") in special:
                errs.append(f"{who} : transcendance {t.get('id')} : id deja pris")
            names[t.get("nom", "").lower()].append(t.get("id"))
            for r in t.get("runes", []):
                if r not in runes:
                    errs.append(f"{who} : transcendance, rune inconnue {r}")
        if who in by or who in special:
            errs.append(f"{who} : id en double")
        by[who] = c
        names[c["nom"].lower()].append(who)
        if c["famille"] not in FAMILIES:
            errs.append(f"{who} : famille inconnue {c['famille']}")
        if c["palier"] not in (1, 2, 3):
            errs.append(f"{who} : palier {c['palier']}")
        if c["rarete"] not in RARITIES:
            errs.append(f"{who} : rarete {c['rarete']}")
        if c["palier"] == 1 and c["rarete"] != "COMMON":
            errs.append(f"{who} : une classe de base est Commune")
        for p, r in c["passifs"]:
            if p not in pas:
                errs.append(f"{who} : passif inconnu {p}")
            elif not 1 <= r <= pas[p]:
                warns.append(f"{who} : {p} rang {r} hors de 1..{pas[p]} (ramene par PassiveSkill.r)")
        for r in c["runes"]:
            if r not in runes:
                errs.append(f"{who} : rune inconnue {r}")
        for a in c["armes"]:
            if a not in weapons:
                errs.append(f"{who} : arme inconnue {a}")
        for m in c["metiers"]:
            if m not in metiers:
                errs.append(f"{who} : metier inconnu {m}")
        if len(c["profil"]) != 6 or min(c["profil"]) < 0 or sum(c["profil"]) <= 0:
            errs.append(f"{who} : profil invalide {c['profil']}")
        if len(c["description"]) < 30:
            warns.append(f"{who} : description courte")
    for n, ids in names.items():
        if len(ids) > 1:
            errs.append(f"nom en double « {n} » : {', '.join(ids)}")
    parents = collections.defaultdict(list)
    for c in classes:
        for n in c["evolutions"]:
            parents[n].append(c["id"])
            if n in special:
                continue
            if n not in by:
                errs.append(f"{c['id']} : evolution inconnue {n}")
            elif by[n]["palier"] != c["palier"] + 1:
                errs.append(f"{c['id']} -> {n} : palier {c['palier']} -> {by[n]['palier']}")
            elif by[n]["famille"] != c["famille"]:
                errs.append(f"{c['id']} -> {n} : change de famille")
    for c in classes:
        p = parents.get(c["id"], [])
        if c["palier"] > 1 and len(p) != 1:
            errs.append(f"{c['id']} : {len(p)} parent(s) {p}")
    return errs, warns


# ---------------------------------------------------------------------------------------------------- Java
def jstr(s):
    return '"' + s.replace("\\", "\\\\").replace('"', '\\"') + '"'


def jlist(items, fmt):
    return "List.of(" + ", ".join(fmt(x) for x in items) + ")"


def entry(c):
    stats = distribute(budget(c["palier"], c["rarete"]), c["profil"])
    nxt = c["evolutions"]
    return ("        c({id}, {nom},\n"
            "                {desc},\n"
            "                Rarity.{rar}, ClassType.{typ}, {pal}, {promo},\n"
            "                {pas},\n"
            "                {arm},\n"
            "                {nxt},\n"
            "                new int[]{{{st}}}, {runes}, {met});").format(
        id=jstr(c["id"]), nom=jstr(c["nom"]), desc=jstr(c["description"]), rar=c["rarete"], typ=FAMILIES[c["famille"]],
        pal=c["palier"], promo=PROMOTION[c["palier"]] if nxt else 0,
        pas=jlist(c["passifs"], lambda p: f"PassiveSkill.{p[0]}.r({p[1]})"),
        arm=jlist(c["armes"], lambda a: f"WeaponMastery.{a}"),
        nxt=jlist(nxt, jstr), st=", ".join(map(str, stats)),
        runes="new String[]{" + ", ".join(map(jstr, c["runes"])) + "}",
        met="new String[]{" + ", ".join(map(jstr, c["metiers"])) + "}")


def java(classes):
    parts = []
    methods = []
    for fam in FAMILIES:
        fc = [c for c in classes if c["famille"] == fam]
        # une methode par famille et par palier : reste loin de la limite de 64 Ko de bytecode par methode
        for pal in sorted({c["palier"] for c in fc}):
            name = f"{fam}{pal}"
            methods.append(name)
            body = "\n".join(entry(c) for c in fc if c["palier"] == pal)
            parts.append(f"    private static void {name}() {{\n{body}\n    }}\n")
    calls = "\n".join(f"        {m}();" for m in methods)
    return f"""package com.eldanior.system.classes.catalog;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.ArrayList;
import java.util.List;

/**
 * FICHIER GENERE par scripts/classes/class_catalog.py depuis scripts/classes/classes.json : ne pas modifier a la main.
 * {len(classes)} classes ordinaires (bases, paliers 2 et 3). Bonus de caracteristiques = bareme du palier et de la rarete
 * (Divine = 30 x Commune), reparti selon le profil de la classe.
 */
public final class ClassCatalog {{

    private ClassCatalog() {{}}

    private static List<ClassModel> out;

    public static synchronized List<ClassModel> all() {{
        out = new ArrayList<>();
{calls}
        List<ClassModel> r = out;
        out = null;
        return r;
    }}

    private static void c(String id, String name, String desc, Rarity rarity, ClassType type, int tier, int promotion,
                          List<PassiveSkill.Ranked> passives, List<WeaponMastery> weapons, List<String> next, int[] s,
                          String[] runes, String[] metiers) {{
        out.add(new CatalogClass(id, name, desc, rarity, type, tier, promotion, passives, weapons, next, s, runes, metiers));
    }}

{chr(10).join(parts)}}}
"""


def report(classes):
    rf = collections.Counter(r for c in classes for r in c["runes"])
    print("\nRunes propres (rune portee par cette seule classe) :")
    for fam in FAMILIES:
        fc = [c for c in classes if c["famille"] == fam]
        own = sum(1 for c in fc if any(rf[r] == 1 for r in c["runes"]))
        sig = collections.Counter(tuple(c["armes"]) for c in fc).most_common(1)[0]
        print(f"  {fam:9s} {own:3d}/{len(fc):3d}   liste d'armes la plus courante x{sig[1]}")
    pf = collections.Counter(p for c in classes for p, _ in c["passifs"])
    print("Passifs les plus repetes :", ", ".join(f"{p} x{n}" for p, n in pf.most_common(6)))


def main():
    raw = load()
    errs, warns = validate(raw)
    classes = derive(raw, {p: m for p, m in passives().items()})
    ids = [c["id"] for c in classes]
    for cid in {i for i in ids if ids.count(i) > 1}:
        errs.append(f"id en double apres derivation : {cid}")
    names = [c["nom"].lower() for c in classes]
    for n in {n for n in names if names.count(n) > 1}:
        errs.append(f"nom en double apres derivation : {n}")
    for w in warns:
        print("  ATTENTION", w)
    if errs:
        for e in errs:
            print("  ERREUR", e)
        sys.exit(f"{len(errs)} erreur(s) : rien n'est ecrit")
    recap = collections.Counter((c["famille"], c["palier"], c["rarete"]) for c in classes)
    print(f"{len(classes)} classes valides. Bareme (total des bonus) :")
    for pal in sorted(BASE):
        row = [f"{r[:4]} {budget(pal, r):5d}" for r in RARITIES if any(k[1] == pal and k[2] == r for k in recap)]
        print(f"  palier {pal} (evolution au niveau {PROMOTION[pal]}) : " + "  ".join(row))
    for fam in FAMILIES:
        print(f"  {fam:9s}", " ".join(f"p{p}:{sum(v for k, v in recap.items() if k[0] == fam and k[1] == p)}" for p in BASE))
    if REPORT:
        report([c for c in classes if c["palier"] <= 3])
    if CHECK:
        return
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    src = java(classes)
    if not os.path.exists(OUT) or read(OUT) != src:
        open(OUT, "w", encoding="utf-8").write(src)
        print("ecrit :", os.path.relpath(OUT, ROOT))


if __name__ == "__main__":
    main()
