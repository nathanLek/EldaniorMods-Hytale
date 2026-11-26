"""Conception des competences passives (refonte du 2026-11-24).

Une competence passive = une SERIE a rangs (1 Commun .. 6 Legendaire). L'ancien systeme avait un passif par rarete
(Frappe Instinctive, Frappe de Predateur, Frappe de Fureur...) : chaque ancien identifiant devient (serie, rang).

Decisions de l'utilisateur (2026-11-24) :
  - plus aucun passif « qui ne fait rien » (stats jamais lues : vitesse d'attaque, portee de detection, invisibilite...) ;
  - plus de cout en mana ni de recharge sur les passifs (les runes sont les competences actives) ;
  - plus de rang Divin dans les series : le Divin est reserve aux 7 Anges et 7 Peches ;
  - les metiers (etablis) sortent des passifs : parchemins de Metier ;
  - Aura de Dignite passive supprimee (la Rune d'Aura la remplace).

Ce fichier est la SOURCE : gen_passifs.py en tire le Java, les pages, les traductions et le nettoyage du butin.
"""

RANGS = {1: "Commun", 2: "Inhabituel", 3: "Rare", 4: "Épique", 5: "Unique", 6: "Légendaire"}
QUALITE = {1: "Common", 2: "Uncommon", 3: "Rare", 4: "Epic", 5: "Unique", 6: "Legendary"}

# Anciens tiers (dossiers) -> rang
TIER_RANG = {"Common": 1, "Uncommon": 2, "Rare": 3, "Epique": 4, "Unique": 5, "Legendaire": 6, "Divin": 6}


def pct(v):
    s = f"{v:g}"
    return s.replace(".", ",")


# --------------------------------------------------------------------------------------------------------------------
# Effets : (fabrique Java, phrase par rang). Les tableaux ont une valeur par rang 1..6 ; None = rang absent.
# --------------------------------------------------------------------------------------------------------------------

def arr(vals):
    """Tableau Java de 6 valeurs (les rangs absents reprennent la valeur la plus proche)."""
    full = list(vals) + [None] * (6 - len(vals))
    last = next(v for v in full if v is not None)
    out = []
    for v in full:
        if v is None:
            v = last
        last = v
        out.append(v)
    return "new float[]{" + ", ".join(f"{float(v):g}f" for v in out) + "}"


def at(vals, r):
    full = list(vals) + [None] * (6 - len(vals))
    return full[r - 1]


class E:
    """Un effet : code Java + phrase pour un rang."""
    def __init__(self, java, phrase):
        self.java = java
        self.phrase = phrase


def chance_degats(c, b, double=0):
    return E(f"PassiveEffects.chanceDegats({arr(c)}, {arr(b)}, {double}f)",
             lambda r: f"{pct(at(c, r))} % de chance d'infliger +{pct(at(b, r))} % de dégâts"
                       + (f" ; {pct(double * 100)} % de chance de doubler l'impact" if double and r == 6 else "") + ".")


def degats_fixes(v):
    return E(f"PassiveEffects.degatsFixes({arr(v)})", lambda r: f"+{pct(at(v, r))} dégâts à chaque coup.")


def cible_pleine_vie(v):
    return E(f"PassiveEffects.ciblePleineVie({arr(v)})",
             lambda r: f"+{pct(at(v, r))} % de dégâts contre une cible à plus de 90 % de vie.")


def coups_enchaines(v):
    return E(f"PassiveEffects.coupsEnchaines({arr(v)})",
             lambda r: f"Chaque coup de suite sur la même cible : +{pct(at(v, r))} % de dégâts (jusqu'à 5 fois).")


def riposte(v):
    return E(f"PassiveEffects.riposte({arr(v)})",
             lambda r: f"+{pct(at(v, r))} % de dégâts pendant 2 s après avoir été touché.")


def chance_arcane(c, v):
    return E(f"PassiveEffects.chanceDegatsFixes({arr(c)}, {arr(v)})",
             lambda r: f"{pct(at(c, r))} % de chance d'infliger {pct(at(v, r))} dégâts arcaniques en plus.")


def mana_haut(t, b):
    return E(f"PassiveEffects.manaHaut({arr(t)}, {arr(b)})",
             lambda r: f"+{pct(at(b, r))} % de dégâts quand ton mana dépasse {pct(at(t, r))} %.")


def arme(mots, exclus, nom, v):
    jm = "new String[]{" + ", ".join(f'"{m}"' for m in mots) + "}"
    je = "new String[]{" + ", ".join(f'"{m}"' for m in exclus) + "}"
    return E(f"PassiveEffects.arme({jm}, {je}, {arr(v)})", lambda r: f"+{pct(at(v, r))} % de dégâts avec {nom}.")


def reduction_fixe(v):
    return E(f"PassiveEffects.reductionFixe({arr(v)})", lambda r: f"-{pct(at(v, r))} dégâts sur chaque coup reçu.")


def reduction(v):
    return E(f"PassiveEffects.reduction({arr(v)})", lambda r: f"-{pct(at(v, r))} % de dégâts reçus.")


def chance_reduction(c, v):
    return E(f"PassiveEffects.chanceReduction({arr(c)}, {arr(v)})",
             lambda r: f"{pct(at(c, r))} % de chance de réduire un coup reçu de {pct(at(v, r))} %.")


def contre_monstres(v):
    return E(f"PassiveEffects.contreMonstres({arr(v)})", lambda r: f"-{pct(at(v, r))} % de dégâts reçus des monstres.")


def vie_basse(t, v):
    return E(f"PassiveEffects.vieBasse({arr(t)}, {arr(v)})",
             lambda r: f"Sous {pct(at(t, r))} % de vie : -{pct(at(v, r))} % de dégâts reçus.")


def endurance_haute(t, v):
    return E(f"PassiveEffects.enduranceHaute({arr(t)}, {arr(v)})",
             lambda r: f"Endurance au-dessus de {pct(at(t, r))} % : -{pct(at(v, r))} % de dégâts reçus.")


def mana_bouclier(t, v):
    return E(f"PassiveEffects.manaBouclier({arr(t)}, {arr(v)})",
             lambda r: f"Mana au-dessus de {pct(at(t, r))} % : -{pct(at(v, r))} % de dégâts reçus.")


def gros_coups(t, v):
    return E(f"PassiveEffects.grosCoups({arr(t)}, {arr(v)})",
             lambda r: f"Les coups de plus de {pct(at(t, r))} dégâts sont réduits de {pct(at(v, r))} %.")


def esquive(v):
    return E(f"PassiveEffects.esquive({arr(v)})", lambda r: f"+{pct(at(v, r))} % de chance d'esquive automatique (plafond 30 %).")


def endurance_au_coup(c, v):
    return E(f"PassiveEffects.enduranceAuCoup({arr(c)}, {arr(v)})",
             lambda r: f"{pct(at(c, r))} % de chance de rendre {pct(at(v, r))} d'endurance à chaque coup donné.")


def mana_au_coup(c, v):
    return E(f"PassiveEffects.manaAuCoup({arr(c)}, {arr(v)})",
             lambda r: f"{pct(at(c, r))} % de chance de rendre {pct(at(v, r))} de mana à chaque coup donné.")


def soin_touche(c, v):
    return E(f"PassiveEffects.soinTouche({arr(c)}, {arr(v)})",
             lambda r: f"{pct(at(c, r))} % de chance de rendre {pct(at(v, r))} % de ta vie max quand tu es touché.")


def second_souffle(t, c, v):
    return E(f"PassiveEffects.secondSouffle({arr(t)}, {arr(c)}, {arr(v)})",
             lambda r: f"Endurance sous {pct(at(t, r))} % : {pct(at(c, r))} % de chance de rendre {pct(at(v, r))} "
                       f"d'endurance quand tu es touché.")


def soin_vie_basse(t, v):
    return E(f"PassiveEffects.soinVieBasse({arr(t)}, {arr(v)})",
             lambda r: f"Sous {pct(at(t, r))} % de vie : chaque coup reçu rend {pct(at(v, r))} % de ta vie max.")


def radar(m):
    return E(f"PassiveEffects.radar({arr(m)})",
             lambda r: f"Signale les présences proches (portée ×{pct(at(m, r))}), joueurs invisibles compris.")


AUCUN = lambda phrase: E("PassiveEffects.AUCUN", lambda r: phrase)

# Stats : (stat Java, mode, valeurs, libelle)
STAT_TXT = {
    "VITALITY": ("Vitalité", "de vie max"),
    "ENDURANCE": ("Endurance max", "d'endurance"),
    "INTELLIGENCE": ("Intelligence", "de mana"),
    "ENDURANCE_DEFENSE": ("% de dégâts reçus en moins", ""),
    "LUCK_CRITICAL": ("% de chance de critique", ""),
    "LUCK_LOOT": ("% de chance de butin", ""),
    "LUCK_EVENT": ("% de chance d'événement rare", ""),
    "AGILITY_SPEED": ("", "de vitesse de déplacement"),
    "AGILITY_JUMP": ("", "de hauteur de saut"),
}


def stats(declencheur, *parts):
    """parts = (stat, 'FLAT'|'MULT'|'REGEN', valeurs)"""
    js = ", ".join(f"PassiveEffects.bonus(\"{s}\", PassiveEffects.Mode.{m}, {arr(v)})" for s, m, v in parts)

    def phrase(r):
        out = []
        for s, m, v in parts:
            x = at(v, r)
            flat_lbl, mult_lbl = STAT_TXT[s]
            if m == "FLAT":
                out.append(f"+{pct(x)} {flat_lbl}")
            elif m == "MULT":
                out.append(f"+{pct(x)} % {mult_lbl}")
            else:
                lbl = "de vie" if s == "VITALITY" else "de mana"
                out.append(f"régénération {lbl} ×{pct(x)}" if x >= 2 else f"+{pct(round((x - 1) * 100))} % de régénération {lbl}")
        txt = ", ".join(out)
        return txt[0].upper() + txt[1:] + "."
    return E(f"PassiveEffects.stats(PassiveEffects.Declencheur.{declencheur}, {js})", phrase)


# --------------------------------------------------------------------------------------------------------------------
# Les series. membres = anciens identifiants -> rang (None = rang tire du dossier de l'ancien passif).
# Le premier membre de rang r de la serie elle-meme donne la page « officielle » de ce rang.
# --------------------------------------------------------------------------------------------------------------------

class Serie:
    def __init__(self, ident, nom, theme, effet, membres, absorbes=(), rang_min=None):
        self.id = ident
        self.nom = nom
        self.theme = theme
        self.effet = effet
        self.membres = list(membres)        # anciens ids de la serie elle-meme (ordre croissant)
        self.absorbes = list(absorbes)      # (ancien id, rang) venus d'une autre serie fusionnee
        self.rang_min = rang_min


ATTAQUE, DEFENSE, VIE, ENDURANCE, AGILITE, CHANCE, MAGIE = "ATTAQUE", "DEFENSE", "VIE", "ENDURANCE", "AGILITE", "CHANCE", "MAGIE"

SERIES = [
    # ---------------------------------------------------------------- ATTAQUE
    Serie("INSTINCTIVE_STRIKE", "Frappe Instinctive", ATTAQUE,
          chance_degats([10, 15, 20, 20, 25, 35], [10, 15, 25, 35, 50, 75], 0.40),
          ["INSTINCTIVE_STRIKE", "PREDATORY_STRIKE", "FURY_STRIKE", "SEISMIC_STRIKE", "PHANTOM_STRIKE", "ANNIHILATOR_STRIKE",
           "JUDGMENT_OF_GENESIS"]),
    Serie("DEEP_SLASH", "Entaille Profonde", ATTAQUE, degats_fixes([1, 2, 4, 6, 9, 12]),
          ["DEEP_SLASH", "SHARP_BLADE", "CRIMSON_BLADE", "VOID_BLADE", "ABYSS_BLADE", "GENESIS_EDGE", "CREATOR_EDGE"]),
    Serie("PRESSURE_POINT", "Point de Pression", ATTAQUE, cible_pleine_vie([15, 25, 35, 45, 55]),
          ["PRESSURE_POINT", "VITAL_PRESSURE", "CRUSHING_PRESSURE", "ANNIHILATING_PRESSURE", "SOUL_CRUSHING_PRESSURE"]),
    Serie("HAUNTING_THRUST", "Traque", ATTAQUE, coups_enchaines([3, 5, 7, 9]),
          ["HAUNTING_THRUST", "RELENTLESS_HUNT", "BLOOD_HUNT", "DEATH_HUNT"]),
    Serie("OPPORTUNIST_STRIKE", "Frappe Opportuniste", ATTAQUE, riposte([12]), ["OPPORTUNIST_STRIKE"]),
    Serie("AXE_MASTERY", "Maîtrise de la Hache", ATTAQUE, arme(["axe"], ["pickaxe"], "une hache", [None, 10, 15, 20, 25, 30]),
          ["AXE_MASTERY", "GREAT_AXE_MASTERY", "WAR_AXE_MASTERY", "LEGENDARY_AXE_MASTERY", "DIVIN_AXE_MASTERY",
           "CREATOR_AXE_MASTERY"]),
    Serie("BOW_MASTERY", "Maîtrise de l'Arc", ATTAQUE, arme(["bow"], [], "un arc", [None, 10, 15, 20, 25]),
          ["BOW_MASTERY", "MARKSMAN_MASTERY", "SNIPER_MASTERY", "LEGENDARY_SNIPER_MASTERY"]),
    Serie("SPEAR_MASTERY", "Maîtrise de la Lance", ATTAQUE, arme(["spear", "halberd", "lance"], [], "une lance", [None, 10, 15, 20]),
          ["SPEAR_MASTERY", "HALBERD_MASTERY", "DRAGON_SPEAR_MASTERY"]),
    Serie("DAGGER_MASTERY", "Maîtrise de la Dague", ATTAQUE, arme(["dagger", "kodachi"], [], "une dague", [None, 10, 15, 20, 25, 30]),
          ["DAGGER_MASTERY", "SHADOW_BLADE_MASTERY", "ASSASSIN_BLADE_MASTERY", "LEGENDARY_DAGGER_MASTERY", "DIVIN_DAGGER_MASTERY",
           "CREATOR_DAGGER_MASTERY"]),
    Serie("SWORD_MASTERY", "Maîtrise de l'Épée", ATTAQUE,
          arme(["sword", "katana", "sabre", "saber"], [], "une épée", [None, 8, 11, 15, 20, 25]),
          ["MINOR_SWORD_MASTERY", "SWORD_MASTERY"]),
    # ---------------------------------------------------------------- MAGIE
    Serie("ARCANE_STRIKE", "Frappe Arcanique", MAGIE, chance_arcane([30, 40, 45, 50, 60, 70], [8, 12, 16, 22, 30, 40]),
          ["ARCANE_STRIKE", "SPELLBLADE", "ARCANE_DEVASTATION", "ARCANE_ANNIHILATION", "ARCANE_OBLIVION", "ARCANE_GENESIS",
           "ARCANE_CREATION"]),
    Serie("OVERFLOWING_POWER", "Puissance Débordante", MAGIE, mana_haut([85, 80, 60, 50, 40], [15, 25, 35, 45, 55]),
          ["OVERFLOWING_POWER", "UNLEASHED_MAGIC", "PURE_MAGIC", "ARCANE_SUPREMACY", "ABSOLUTE_SUPREMACY"]),
    Serie("AWAKENED_MIND", "Esprit Éveillé", MAGIE, stats("AUCUN", ("INTELLIGENCE", "FLAT", [15, 25, 35, 50, 70, 90])),
          ["AWAKENED_MIND", "EXPANDED_MIND", "BRILLIANT_MIND", "GENIUS_MIND", "COSMIC_MIND", "INFINITE_MIND", "CREATOR_MIND"],
          absorbes=[("MANAWELL", 4)]),
    Serie("MANA_FONT", "Source de Mana", MAGIE, stats("AUCUN", ("INTELLIGENCE", "REGEN", [1.2, 2, 3, 4, 6])),
          ["MANA_FONT", "MANA_STREAM", "MANA_RIVER", "MANA_OCEAN", "MANA_INFINITY"]),
    Serie("SPIRITUAL_SIPHON", "Siphon Spirituel", MAGIE, mana_au_coup([30, 40, 45, 50, 60, 70], [5, 8, 12, 16, 22, 30]),
          ["SPIRITUAL_SIPHON", "SOUL_STEALER", "SPIRIT_DRAIN", "ARCANE_VAMPIRISM", "COSMIC_VAMPIRISM", "INFINITE_VAMPIRISM",
           "CREATOR_VAMPIRISM"]),
    Serie("MANA_BARRIER", "Barrière de Mana", MAGIE, mana_bouclier([50, 40, 30, 20], [10, 15, 20, 25]),
          ["MANA_BARRIER", "ARCANE_SHIELD", "MANA_FORTRESS", "MANA_CITADEL"]),
    Serie("MYSTIC_VEIL", "Voile Mystique", MAGIE, chance_reduction([10, 30], [25, 35]), ["MYSTIC_VEIL", "ASTRAL_CLOAK"]),
    Serie("VOL", "Vol", MAGIE, AUCUN("Permet de voler en consommant 20 points de mana par seconde."), ["VOL"]),
    # ---------------------------------------------------------------- DEFENSE
    Serie("IRON_RESOLVE", "Résolution de Fer", DEFENSE, reduction_fixe([3, 5, 8, 12, 16, 20]),
          ["IRON_RESOLVE", "STEEL_RESOLVE", "TITAN_RESOLVE", "GOD_RESOLVE", "IMMORTAL_RESOLVE", "ETERNITY_RESOLVE",
           "ABSOLUTE_RESOLVE"],
          absorbes=[("PAIN_TOLERANCE", 1), ("BATTLE_SCARS", 2), ("WAR_VETERAN", 3), ("WAR_LEGEND", 4)]),
    Serie("MINOR_PARRY", "Parade", DEFENSE, chance_reduction([20, 30, 40, 45, 50, 60], [15, 20, 25, 30, 35, 40]),
          ["MINOR_PARRY", "EXPERT_PARRY", "MASTER_PARRY", "PERFECT_PARRY", "DIVINE_PARRY", "CELESTIAL_PARRY", "CREATOR_PARRY"]),
    Serie("ADAPTIVE_SHIELD", "Rempart", DEFENSE, chance_reduction([10, 20, 30, 40, 45, 50], [15, 30, 40, 50, 60, 70]),
          ["ADAPTIVE_SHIELD", "REACTIVE_BULWARK", "ADAMANTINE_BULWARK", "DIVINE_BULWARK", "COSMIC_BULWARK", "GENESIS_BULWARK",
           "CREATOR_BULWARK"]),
    Serie("HUNTER_GUARD", "Garde du Chasseur", DEFENSE, contre_monstres([10, 15, 20, 25]),
          ["HUNTER_GUARD", "BEAST_GUARD", "MONSTER_SLAYER_GUARD", "DRAGON_SLAYER_GUARD"]),
    # Corps Robuste et Cœur de Chêne : Défense en % depuis le 2026-11-25 (avant : points fixes retirés de chaque coup)
    Serie("STURDY_BODY", "Corps Robuste", DEFENSE, stats("TOUCHE", ("ENDURANCE_DEFENSE", "FLAT", [3, 5, 8, 11, 15])),
          ["STURDY_BODY", "IRON_BODY", "STEEL_BODY", "DIAMOND_BODY", "COSMIC_BODY"],
          absorbes=[("THICK_SKIN", 1), ("ARMORED_SKIN", 2), ("FORTIFIED_SKIN", 3), ("ADAMANTINE_SKIN", 4)]),
    Serie("STONE_SKIN", "Peau de Pierre", DEFENSE, reduction([5, 8, 10, 15, 25, 35]),
          ["STONE_SKIN", "BRONZE_SKIN", "IRON_SKIN", "STEEL_SKIN", "OBSIDIAN_SKIN", "DIAMOND_SKIN", "DIVINE_AEGIS"],
          absorbes=[("HARDENING", 1), ("FORTIFICATION", 2), ("IRON_FORTIFICATION", 3), ("MYTHRIL_FORTIFICATION", 4),
                    ("ETERNAL_FORTIFICATION", 5)]),
    Serie("TENACITY", "Ténacité", DEFENSE, vie_basse([30, 35, 40, 45, 50, 55], [8, 12, 16, 20, 25, 30]),
          ["TENACITY", "UNYIELDING", "UNBREAKABLE", "INVINCIBLE", "IMMORTAL_ABSOLUTE", "CELESTIAL_IMMORTALITY",
           "TRUE_IMMORTALITY"]),
    Serie("SOLID_STANCE", "Posture Solide", DEFENSE, endurance_haute([80, 70, 60, 50, 40, 30], [10, 15, 20, 25, 30, 35]),
          ["SOLID_STANCE", "UNMOVABLE_MOUNTAIN", "LIVING_FORTRESS", "ETERNAL_FORTRESS", "DIVINE_FORTRESS", "CELESTIAL_FORTRESS",
           "CREATOR_FORTRESS"]),
    Serie("STEEL_NERVES", "Nerfs d'Acier", DEFENSE, gros_coups([15, 12], [10, 15]), ["STEEL_NERVES", "IRON_WILL"]),
    # Esquive : valeurs baissées le 2026-11-25 (s'ajoutent à l'Agilité, plafond de l'esquive automatique : 30 %)
    Serie("MIRACLE_DODGE", "Esquive", DEFENSE, esquive([2, 3, 5, 7, 9, 12]),
          ["MIRACLE_DODGE", "PHANTOM_DODGE", "SHADOW_DODGE", "DIMENSIONAL_DODGE", "REALITY_DODGE", "TIME_DODGE", "FATE_DODGE"],
          absorbes=[("TONOSQUIVE", 5)]),
    # ---------------------------------------------------------------- VIE
    Serie("ROBUST_CONSTITUTION", "Constitution", VIE, stats("TOUCHE", ("VITALITY", "FLAT", [5, 10, 15, 20, 25, 30])),
          ["ROBUST_CONSTITUTION", "STEEL_CONSTITUTION", "TITAN_CONSTITUTION", "GOD_CONSTITUTION", "COSMIC_CONSTITUTION",
           "CELESTIAL_CONSTITUTION", "CREATOR_CONSTITUTION"]),
    Serie("LIFE_FORCE", "Force Vitale", VIE, stats("TOUCHE", ("VITALITY", "MULT", [5, 10, 15, 20, 25])),
          ["LIFE_FORCE", "OVERFLOWING_LIFE", "BURSTING_LIFE", "ETERNAL_LIFE", "INFINITE_LIFE"]),
    Serie("VITAL_BLOOD", "Sang Vital", VIE,
          stats("TOUCHE", ("VITALITY", "FLAT", [3, 6, 9, 12]), ("VITALITY", "REGEN", [1.1, 1.2, 1.3, 1.4])),
          ["VITAL_BLOOD", "ENRICHED_BLOOD", "ANCIENT_BLOOD", "DRAGON_BLOOD"]),
    Serie("HEART_OF_OAK", "Cœur de Chêne", VIE,
          stats("TOUCHE", ("VITALITY", "FLAT", [4, 8, 12, 16, 20, 25]), ("ENDURANCE_DEFENSE", "FLAT", [1, 2, 3, 4, 5, 6])),
          ["HEART_OF_OAK", "HEART_OF_IRON", "HEART_OF_STEEL", "HEART_OF_DIAMOND", "HEART_OF_ETERNITY", "HEART_OF_GENESIS",
           "HEART_OF_CREATION"]),
    Serie("CELLULAR_REGENERATION", "Régénération Cellulaire", VIE, soin_touche([20, 30, 40, 45, 50, 60], [5, 8, 12, 16, 20, 25]),
          ["CELLULAR_REGENERATION", "TROLL_BLOOD", "HYDRA_BLOOD", "PHOENIX_BLOOD", "ETERNAL_BLOOD", "GENESIS_BLOOD",
           "CREATOR_BLOOD"]),
    Serie("PERSEVERANCE", "Persévérance", VIE, soin_vie_basse([25, 30], [2, 4]), ["PERSEVERANCE", "UNDYING"]),
    Serie("NATURAL_RECOVERY", "Rétablissement Naturel", VIE, stats("AUCUN", ("VITALITY", "REGEN", [1.2, 2])),
          ["NATURAL_RECOVERY", "VITAL_RECOVERY"]),
    # ---------------------------------------------------------------- ENDURANCE
    Serie("TIRELESS_BREATH", "Souffle Inépuisable", ENDURANCE, stats("AUCUN", ("ENDURANCE", "FLAT", [10, 20, 30, 40, 55, 70])),
          ["TIRELESS_BREATH", "LUNGS_OF_STEEL", "DRAGON_LUNGS", "TITAN_LUNGS", "COSMIC_LUNGS", "CELESTIAL_LUNGS", "CREATOR_LUNGS"]),
    Serie("COMBAT_VIGOR", "Vigueur Combative", ENDURANCE, endurance_au_coup([30, 45, 60, 70, 70], [5, 8, 12, 16, 20]),
          ["COMBAT_VIGOR", "BATTLE_FRENZY", "WAR_FRENZY", "BLOOD_FRENZY", "RAGE_FRENZY"],
          absorbes=[("ACTIVE_BREATHING", 1), ("IRON_LUNGS", 2), ("ADAMANTINE_LUNGS", 3), ("MYTHRIL_LUNGS", 4)]),
    Serie("SECOND_WIND", "Second Souffle", ENDURANCE, second_souffle([20, 25], [20, 30], [25, 40]), ["SECOND_WIND", "SURVIVOR_SPIRIT"]),
    # ---------------------------------------------------------------- AGILITE
    Serie("WIND_STEP", "Pas de Vent", AGILITE, stats("AUCUN", ("AGILITY_SPEED", "MULT", [3, 5, 8, 12, 16, 20])),
          ["WIND_STEP", "GALE_STEP", "STORM_STEP", "VOID_STEP", "DIMENSIONAL_STEP", "CELESTIAL_STEP", "CREATOR_STEP"],
          absorbes=[("ATHLETICISM", 2), ("MARATHON_RUNNER", 3)]),
    Serie("ELDANIOR_SUPPLENESS", "Souplesse", AGILITE, stats("AUCUN", ("AGILITY_JUMP", "MULT", [10, 18, 25, 35])),
          ["ELDANIOR_SUPPLENESS", "CATLIKE_POISE", "ACROBATIC_POISE", "GRAVITY_DEFIANCE"]),
    Serie("SURVIVAL_INSTINCT", "Instinct de Survie", AGILITE, radar([1.5, 1.8, 2.2, 2.8, 3.5]),
          ["SURVIVAL_INSTINCT", "DANGER_SENSE", "COMBAT_INTUITION", "WAR_PROPHECY", "FATE_VISION"],
          absorbes=[("SIXTH_SENSE", 1), ("UNIVERSAL_DETECTION", 4)]),
    Serie("NIGHT_VISION", "Vision Nocturne", AGILITE, AUCUN("Tu vois clair la nuit."), ["NIGHT_VISION"],
          absorbes=[("DARK_VISION", 1), ("ABYSSAL_VISION", 1), ("VOID_SIGHT", 1)]),
    # ---------------------------------------------------------------- CHANCE
    Serie("LUCKY_STRIKE", "Frappe Chanceuse", CHANCE, stats("CRITIQUE", ("LUCK_CRITICAL", "FLAT", [3, 5, 8, 12, 16, 20])),
          ["LUCKY_STRIKE", "CRITICAL_LUCK", "DESTINY_STRIKE", "DIVINE_STRIKE", "COSMIC_STRIKE", "GENESIS_STRIKE", "CREATOR_STRIKE"],
          absorbes=[("KEEN_SENSES", 1), ("RAZOR_SENSES", 2), ("DEADLY_PRECISION", 3), ("FATAL_PRECISION", 4),
                    ("ABSOLUTE_PRECISION", 5), ("OMNISCIENT_PRECISION", 6), ("CREATOR_PRECISION", 6)]),
    Serie("TREASURE_HUNTER", "Chasseur de Trésors", CHANCE, stats("AUCUN", ("LUCK_LOOT", "FLAT", [5, 10, 15, 20, 25])),
          ["TREASURE_HUNTER", "RELIC_HUNTER", "ARTIFACT_HUNTER", "LEGEND_HUNTER", "MYTH_HUNTER"],
          absorbes=[("FORTUNE_COINS", 2), ("GOLDEN_TOUCH", 4)]),
    Serie("GOOD_OMEN", "Bon Présage", CHANCE, stats("AUCUN", ("LUCK_EVENT", "FLAT", [15, 25, 35, 50])),
          ["GOOD_OMEN", "FATED_OMEN", "PROPHECY_OMEN", "COSMIC_OMEN"]),
]

# Anciens passifs supprimes (ne faisaient rien, faisaient doublon avec une rune, ou rang Divin isole).
SUPPRIMES = {
    # vitesse d'attaque : stat jamais lue par le jeu
    "DUELIST_SWIFTNESS", "COMBATANT_SWIFTNESS", "WARRIOR_SWIFTNESS", "BERSERKER_SWIFTNESS", "DEMIGOD_SWIFTNESS",
    "GOD_SLAYER_SWIFTNESS", "CREATOR_SWIFTNESS",
    "LIGHT_REFLEXES", "THUNDER_REFLEXES", "LIGHTNING_REFLEXES", "DIVINE_REFLEXES", "COSMIC_REFLEXES",
    # portee de detection / invisibilite : stats jamais lues
    "EAGLE_EYE", "HAWK_EYE", "EAGLE_VISION", "OMNISCIENT_VISION", "ALL_SEEING_EYE", "GENESIS_VISION", "CREATOR_VISION",
    "PSYCHIC_AWARENESS", "MIND_READER", "SOUL_READER", "TRUE_SIGHT", "OMNI_SIGHT", "ABSOLUTE_SIGHT",
    # faux actifs (mana + recharge) : le role des runes
    "TRACKER", "MASTER_TRACKER", "KODA_JUDGMENT", "MAUVAIS_PRESAGE", "DETECTIONOFVITALPOINTS",
    # divins isoles (le Divin est reserve aux Anges et Peches)
    "DYNA_AEGIS", "MANA_HEART",
    # aura de dignite passive (remplacee par la Rune d'Aura)
    "DIGNITY_AURA", "DIGNITY_AURA_1", "DIGNITY_AURA_2", "DIGNITY_AURA_3", "DIGNITY_AURA_4", "DIGNITY_AURA_5",
    "BATON_MAGIQUE",
}

# Les 7 Peches et 7 Anges restent des passifs uniques (Divin), un seul a la fois.
DIVINS = ["PECHE_ORGUEIL", "PECHE_COLERE", "PECHE_AVARICE", "PECHE_ENVIE", "PECHE_LUXURE", "PECHE_GOURMANDISE", "PECHE_PARESSE",
          "ANGE_HUMILITE", "ANGE_PATIENCE", "ANGE_TEMPERANCE", "ANGE_CHARITE", "ANGE_BIENVEILLANCE", "ANGE_CHASTETE",
          "ANGE_DILIGENCE"]

# Metiers : (id conserve pour les sauvegardes et la progression, nom, etabli, qualite, objet du parchemin)
METIERS = [
    ("ARTISANAT", "Artisanat", "tous les établis (passe-partout)", "Rare", "Eldanior_Parchemin_Metier_Artisanat"),
    ("CRAFT_ARTISAN_BASE", "Artisan", "l'établi de base", "Common", "Eldanior_Parchemin_Metier_Artisan"),
    ("CRAFT_CUISINE", "Cuisine", "l'établi de cuisine", "Uncommon", "Eldanior_Parchemin_Metier_Cuisine"),
    ("CRAFT_FONDERIE", "Fonderie", "le fourneau", "Epic", "Eldanior_Parchemin_Metier_Fonderie"),
    ("CRAFT_ARMURERIE", "Armurerie", "l'établi d'armurier", "Epic", "Eldanior_Parchemin_Metier_Armurerie"),
    ("CRAFT_FORGE_ARMES", "Forge d'Armes", "l'établi d'armes", "Epic", "Eldanior_Parchemin_Metier_Forge_Armes"),
    ("CRAFT_FORGE_DIVINE", "Forge Divine", "la Forge Divine (artefacts)", "Legendary", "Eldanior_Parchemin_Metier_Forge_Divine"),
    ("CRAFT_TANNERIE", "Tannerie", "la tannerie", "Uncommon", "Eldanior_Parchemin_Metier_Tannerie"),
    ("CRAFT_ALCHIMIE", "Alchimie", "l'établi d'alchimie", "Rare", "Eldanior_Parchemin_Metier_Alchimie"),
    ("CRAFT_SCIERIE", "Scierie", "la scierie", "Uncommon", "Eldanior_Parchemin_Metier_Scierie"),
    ("CRAFT_AGRICULTURE", "Agriculture", "l'établi de ferme", "Common", "Eldanior_Parchemin_Metier_Agriculture"),
    ("CRAFT_RECYCLAGE", "Recyclage", "l'établi de recyclage", "Uncommon", "Eldanior_Parchemin_Metier_Recyclage"),
]
