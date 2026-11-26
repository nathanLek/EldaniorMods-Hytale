#!/usr/bin/env python3
"""Simulateur d'équilibrage des stats (étape 0 de docs/plan-equilibrage-stats.md, 2026-11-25).

Reproduit les formules du combat (StatConfig, CombatStatsSystem, AutoDodge, MobsWorldConfig) et calcule, par niveau et
par profil de joueur, face à un monstre du même niveau : PV, dégâts par coup, critique, esquive, défense, nombre de coups
pour le tuer et nombre de coups pour mourir.

Les valeurs sont lues dans le code Java (ratios, plafonds, progression des monstres, bonus des classes) : le simulateur
suit le jeu sans recopie à la main. Hypothèses (modifiables en tête de fichier) : arme de référence = épée de fer vanilla
(coup moyen 14) multipliée par 1 + niveau/200 (courbe du catalogue d'armes : niv. 80 ×1,4, 200 ×2, 560 ×3,8, 800 ×5) ;
monstre de référence = Guerrier Trork (61 PV, 23 dégâts).

Usage :
  python3 scripts/equilibrage/stats_sim.py                    # profils sans classe, formules du jeu
  python3 scripts/equilibrage/stats_sim.py --ancien           # compare avec les anciennes Force/Défense fixes
  python3 scripts/equilibrage/stats_sim.py --classe ombre_supreme
  python3 scripts/equilibrage/stats_sim.py --classes          # répartition des bonus de classe par rareté
"""
import argparse
import glob
import math
import os
import re
import statistics

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
JAVA = os.path.join(ROOT, "src/main/java/com/eldanior/system")

# --- hypothèses ---
PV_BASE_JOUEUR = 100.0
COUP_ARME = 14.0                       # épée de fer : 10, 10, 18 (enchaînement)
MONSTRE_PV, MONSTRE_DEGATS = 61.0, 23.0
POINTS_PAR_NIVEAU = 3
CRIT_MULT = 2.0
NIVEAUX = [1, 25, 50, 100, 200, 400, 600, 999]

STATS = ["str", "vit", "int", "end", "agl", "lck"]
PROFILS = {
    "Équilibré": dict(str=1, vit=1, int=1, end=1, agl=1, lck=1),
    "Guerrier": dict(str=4, vit=3, end=3),
    "Assassin": dict(agl=4, str=3, lck=3),
    "Tank": dict(vit=5, end=5),
    "Tout Force": dict(str=1),
    "Tout Agilité": dict(agl=1),
}


def lire(path):
    with open(path, encoding="utf-8") as f:
        return f.read()


def stat_config():
    """{NOM: (base, ratio, plafond)} lus dans StatConfig.java."""
    s = lire(os.path.join(JAVA, "config/configs/StatConfig.java"))
    out = {}
    for m in re.finditer(r"\n\s+([A-Z_]+)\(StatType\.\w+,\s*[^,]+,\s*\"[^\"]+\",\s*([\d.]+)f,\s*([\d.]+)f,\s*([\d.]+)f?,\s*([\d.]+)f", s):
        out[m.group(1)] = (float(m.group(2)), float(m.group(3)), float(m.group(5)))
    dm = re.search(r"DEFENSE_MAX = ([\d.]+)f", s)
    dh = re.search(r"DEFENSE_HALF_POINTS = ([\d.]+)f", s)
    out["_DEFENSE"] = (float(dm.group(1)), float(dh.group(1))) if dm and dh else (50.0, 1000.0)
    dmax = re.search(r"DODGE_MAX = ([\d.]+)f", s)
    dfull = re.search(r"DODGE_FULL_POINTS = ([\d.]+)f", s)
    out["_DODGE"] = (float(dmax.group(1)), float(dfull.group(1))) if dmax and dfull else (30.0, 3500.0)
    return out


def mobs_config():
    s = lire(os.path.join(JAVA, "config/configs/MobsWorldConfig.java"))
    hp = float(re.search(r"HP_PER_LEVEL = ([\d.]+)f", s).group(1))
    dmg = float(re.search(r"DAMAGE_PER_LEVEL = ([\d.]+)f", s).group(1))
    return hp, dmg


def classes():
    """[(id, rareté, {stat: bonus})] du catalogue des classes (scripts/classes/classes.json + barème de class_catalog.py)."""
    import sys
    sys.path.insert(0, os.path.join(ROOT, "scripts", "classes"))
    import class_catalog as cc
    return [(c["id"], c["rarete"], dict(zip(STATS, cc.distribute(cc.budget(c["palier"], c["rarete"]), c["profil"]))))
            for c in cc.load()]


def valeur(cfg, nom, points):
    """Valeur finale sans passifs : base + points × ratio, plafond souple (30 % au-delà, maxi plafond + 15 %)."""
    base, ratio, cap = cfg[nom]
    v = base + points * ratio
    if cap > 0 and v > cap:
        v = cap + min((v - cap) * 0.3, cap * 0.15)
    return v


def esquive(cfg, agl):
    dmax, full = cfg["_DODGE"]
    return 0.0 if agl <= 0 else dmax * math.sqrt(min(agl, full) / full)


def points(profil, niveau, bonus_classe):
    total = POINTS_PAR_NIVEAU * (niveau - 1)
    poids = sum(profil.values())
    pts = {s: 1 + total * profil.get(s, 0) / poids for s in STATS}
    for s in STATS:
        pts[s] += bonus_classe.get(s, 0)
    return pts


def combat(cfg, mob_hp, mob_dmg, niveau, pts, ancien=False):
    arme = COUP_ARME * (1 + niveau / 200)
    if ancien:     # formules d'avant le 2026-11-25 : Force +0,5 fixe (plafond 50), Défense -0,1 fixe (plafond 35)
        coup = arme + min(0.5 * pts["str"], 50) + min(max(0.5 * pts["str"] - 50, 0) * 0.3, 7.5)
        red_pct = 0.0
        red_fixe = min(0.1 * pts["end"], 35) + min(max(0.1 * pts["end"] - 35, 0) * 0.3, 5.25)
    else:          # formules du jeu (lues dans StatConfig) : Force en %, Défense en % (courbe), plafond 60 %
        coup = arme * (1 + cfg["STRENGTH_DAMAGE"][1] * pts["str"] / 100)
        dmax, half = cfg["_DEFENSE"]
        red_pct = min(dmax * pts["end"] / (pts["end"] + half), cfg["ENDURANCE_DEFENSE"][2]) / 100
        red_fixe = 0.0
    crit = min(valeur(cfg, "LUCK_CRITICAL", pts["lck"]), 92) / 100
    coup_moyen = coup * (1 + crit * (CRIT_MULT - 1))
    pv = PV_BASE_JOUEUR + valeur(cfg, "VITALITY", pts["vit"])
    m_pv = MONSTRE_PV + niveau * mob_hp
    m_coup = max(1.0, (MONSTRE_DEGATS + (niveau - 1) * mob_dmg) * (1 - red_pct) - red_fixe)
    esq = esquive(cfg, pts["agl"]) / 100
    return dict(pv=pv, coup=coup_moyen, crit=crit * 100, esq=esq * 100, def_=red_fixe if ancien else red_pct * 100,
                pour_tuer=math.ceil(m_pv / coup_moyen), pour_mourir=pv / (m_coup * (1 - esq)), m_coup=m_coup, m_pv=m_pv)


def tableau(cfg, mob, titre, bonus, ancien):
    print(f"\n=== {titre} ===")
    unite_def = "déf" if ancien else "déf %"
    for nom, prof in PROFILS.items():
        print(f"\n{nom}")
        print(f"{'niv':>4} {'PV':>6} {'coup':>7} {'crit%':>6} {'esq%':>5} {unite_def:>6} | {'monstre PV':>10} {'coup':>6} | {'coups pour tuer':>15} {'coups pour mourir':>17}")
        for n in NIVEAUX:
            r = combat(cfg, *mob, n, points(prof, n, bonus), ancien)
            print(f"{n:>4} {r['pv']:>6.0f} {r['coup']:>7.1f} {r['crit']:>6.1f} {r['esq']:>5.1f} {r['def_']:>6.1f} | "
                  f"{r['m_pv']:>10.0f} {r['m_coup']:>6.1f} | {r['pour_tuer']:>15} {r['pour_mourir']:>17.1f}")


def resume_classes():
    cl = classes()
    print(f"{len(cl)} classes")
    par = {}
    for cid, rar, b in cl:
        par.setdefault(rar, []).append(b)
    print(f"{'rareté':<12} {'n':>3} " + " ".join(f"{s + ' méd/max':>14}" for s in STATS))
    for rar, lst in sorted(par.items(), key=lambda kv: statistics.median(sum(b.values()) for b in kv[1])):
        cells = []
        for s in STATS:
            vals = [b[s] for b in lst]
            cells.append(f"{statistics.median(vals):>7.0f}/{max(vals):<6}")
        print(f"{rar:<12} {len(lst):>3} " + " ".join(f"{c:>14}" for c in cells))


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--ancien", action="store_true", help="compare avec les anciennes formules (Force/Défense fixes)")
    ap.add_argument("--classe", help="ajoute les bonus d'une classe (id)")
    ap.add_argument("--classes", action="store_true", help="bonus des classes par rareté")
    a = ap.parse_args()
    if a.classes:
        resume_classes()
        return
    cfg, mob = stat_config(), mobs_config()
    bonus = {}
    if a.classe:
        found = [b for cid, _, b in classes() if cid == a.classe]
        if not found:
            raise SystemExit(f"classe inconnue : {a.classe}")
        bonus = found[0]
    titre = f"classe {a.classe}" if a.classe else "sans classe"
    tableau(cfg, mob, f"Formules du jeu ({titre})", bonus, False)
    if a.ancien:
        tableau(cfg, mob, f"Anciennes formules, Force/Défense fixes ({titre})", bonus, True)


if __name__ == "__main__":
    main()
