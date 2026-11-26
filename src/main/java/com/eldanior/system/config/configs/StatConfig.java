package com.eldanior.system.config.configs;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;

import java.util.function.ToIntFunction;

public enum StatConfig {

    // === STATS CLASSIQUES (EntityStatMap) ===
    // Ordre : Type, StatID, Key, Base, Ratio, Regen, Cap (NOUVEAU), ProviderPlayer, ProviderClass

    VITALITY(StatType.ATTRIBUTE, DefaultEntityStatTypes.getHealth(), "Eldanior_Vitality", 0.0f, 0.5f, 0.0002778f, 0.0f,
            PlayerLevelData::getVitality, ClassModel::getBonusVit),

    INTELLIGENCE(StatType.ATTRIBUTE, DefaultEntityStatTypes.getMana(), "Eldanior_Intelligence", 0.0f, 3.33f, 0.0001852f, 0.0f,
            PlayerLevelData::getIntelligence, ClassModel::getBonusInt),

    ENDURANCE(StatType.ATTRIBUTE, DefaultEntityStatTypes.getStamina(), "Eldanior_Endurance", 0.0f, 0.5f, 0, 0.0f,
            PlayerLevelData::getEndurance, ClassModel::getBonusEnd),

    // === MOUVEMENTS ===
    DETECTION_RANGE(StatType.MECHANIC, -1, "Detection_Range", 30.0f, 0.1f, 0.0f, 100.0f,
            PlayerLevelData::getAgility, ClassModel::getBonusAgl),

    THREAT_AWARENESS(StatType.MECHANIC, -1, "Threat_Aware", 10.0f, 0.05f, 0.0f, 40.0f,
            PlayerLevelData::getLuck, ClassModel::getBonusLck),

    LOW_LIGHT_VISIBILITY(StatType.MECHANIC, -1, "Light_Vis", 1.0f, 0.002f, 0.0f, 2.0f,
            PlayerLevelData::getLuck, ClassModel::getBonusLck),

    STEALTH_DETECTION(StatType.MECHANIC, -1, "Stealth_Det", 5.0f, 0.1f, 0.0f, 30.0f,
            PlayerLevelData::getAgility, ClassModel::getBonusAgl),

    TRACKING_EVIDENCE(StatType.MECHANIC, -1, "Track_Evid", 1.0f, 0.01f, 0.0f, 5.0f,
            PlayerLevelData::getLuck, ClassModel::getBonusLck),

    // === MOUVEMENTS ===
    AGILITY_SPEED(StatType.MOVEMENT_SPEED, -1, "Eldanior_Speed", 1.5f, 0.0005f, 0.0f, 3.0f, // Cap vitesse x3
            PlayerLevelData::getAgility, ClassModel::getBonusAgl),

    AGILITY_JUMP(StatType.MOVEMENT_JUMP, -1, "Eldanior_Jump", 11.8f, 0.004f, 0.0f, 20.0f, // Cap saut 20 blocks
            PlayerLevelData::getAgility, ClassModel::getBonusAgl),

    AGILITY_FALL_RESISTANCE(StatType.MECHANIC, -1, "Agility_Fall", 0.0f, 0.0007f, 0.0f, 1.0f,
            PlayerLevelData::getAgility, ClassModel::getBonusAgl),

    // Esquive automatique : courbe propre (voir getFinalValue), 30 % vers 3 500 points d'Agilite, plafond dur 30 %.
    // La vitesse d'attaque (ATTACK_SPEED) a ete retiree le 2026-11-25 : elle n'etait lue nulle part.
    DODGE_CHANCE(StatType.MECHANIC, -1, "Agility_Dodge", 0.0f, 0.0f, 0.0f, 30.0f,
            PlayerLevelData::getAgility, ClassModel::getBonusAgl),

    // 5. DEGATS FORCE : +0,5 % de dégâts par point, sans plafond (2026-11-25 ; avant +0,5 fixe plafonné à 50 ;
    //    0,15 % essayé en jeu : trop faible, 1 158 points ne donnaient que x2,7)
    STRENGTH_DAMAGE(StatType.MECHANIC, -1, "Strength_Dmg", 0.0f, 0.5f, 0.0f, 0.0f,
            PlayerLevelData::getStrength, ClassModel::getBonusStr),

    // 6. DEFENSE ENDURANCE : % de dégâts reçus en moins, courbe 50 × pts / (pts + 1 000), passifs compris plafond dur 60 %
    //    (2026-11-25 ; avant -0,1 dégât fixe par point, plafond 35)
    ENDURANCE_DEFENSE(StatType.MECHANIC, -1, "Endurance_Def", 0.0f, 0.0f, 0.0f, 60.0f,
            PlayerLevelData::getEndurance, ClassModel::getBonusEnd),

    // === MÉCANIQUES DE CHANCE (Nouveau !) ===
    // 1. CRITIQUE : 0.05 % par point, Cap à 80 %
    LUCK_CRITICAL(StatType.MECHANIC, -1, "Luck_Crit", 0.0f, 0.05f, 0.0f, 80.0f,
            PlayerLevelData::getLuck, ClassModel::getBonusLck),

    // 2. LOOT : Ratio 0.5%, Pas de cap
    LUCK_LOOT(StatType.MECHANIC, -1, "Luck_Loot", 0.0f, 0.005f, 0.0f, 100.0f,
            PlayerLevelData::getLuck, ClassModel::getBonusLck),

    // 3. EVENT RARE : Ratio 0.2%, Pas de cap
    LUCK_EVENT(StatType.MECHANIC, -1, "Luck_Event", 0.0f, 0.005f, 0.0f, 50.0f,
            PlayerLevelData::getLuck, ClassModel::getBonusLck);


    // --- DÉFINITIONS ---
    public enum StatType {
        ATTRIBUTE,      // Vie, Mana...
        MOVEMENT_SPEED, // Vitesse
        MOVEMENT_JUMP,  // Saut
        MECHANIC        // Calculs internes (Crit, Loot...)
    }

    private final StatType type;
    private final int statId;
    private final String modifierKey;
    private final float baseValue;
    private final float ratio;
    private final float regenRate;
    private final float cap; // NOUVEAU CHAMP
    private final ToIntFunction<PlayerLevelData> playerProvider;
    private final ToIntFunction<ClassModel> classProvider;

    StatConfig(StatType type, int statId, String modifierKey, float baseValue, float ratio, float regenRate, float cap,
               ToIntFunction<PlayerLevelData> playerProvider, ToIntFunction<ClassModel> classProvider) {
        this.type = type;
        this.statId = statId;
        this.modifierKey = modifierKey;
        this.baseValue = baseValue;
        this.ratio = ratio;
        this.regenRate = regenRate;
        this.cap = cap;
        this.playerProvider = playerProvider;
        this.classProvider = classProvider;
    }

    /** Esquive automatique : plafond (en %) et points d'Agilite pour l'atteindre (decide le 2026-11-25). */
    public static final float DODGE_MAX = 30.0f;
    public static final float DODGE_FULL_POINTS = 3500.0f;

    /** 30 x racine(points / 3 500) : 100 pts = 5 %, 500 = 11 %, 1 000 = 16 %, 2 000 = 23 %, 3 500 = 30 %. */
    public static float dodgeCurve(int points) {
        if (points <= 0) return 0f;
        return DODGE_MAX * (float) Math.sqrt(Math.min(points, DODGE_FULL_POINTS) / DODGE_FULL_POINTS);
    }

    /** Defense : 50 × pts / (pts + 1 000) : 100 pts = 4,5 %, 500 = 16,7 %, 1 000 = 25 %, 3 000 = 37,5 %. */
    public static final float DEFENSE_MAX = 50.0f;
    public static final float DEFENSE_HALF_POINTS = 1000.0f;

    public static float defenseCurve(int points) {
        if (points <= 0) return 0f;
        return DEFENSE_MAX * points / (points + DEFENSE_HALF_POINTS);
    }

    // --- GETTERS ---
    public StatType getType() { return type; }
    public int getStatId() { return statId; }
    public String getModifierKey() { return modifierKey; }
    public float getBaseValue() { return baseValue; }
    public float getRatio() { return ratio; }
    public float getRegenRate() { return regenRate; }
    public float getCap() { return cap; } // NOUVEAU GETTER

    public ToIntFunction<PlayerLevelData> getPlayerProvider() { return playerProvider; }
    public ToIntFunction<ClassModel> getClassProvider() { return classProvider; }

    // --- MÉTHODES UTILES ---

    // Récupère les points totaux (Joueur + Classe + Titres cumulés)
    public int getTotalPoints(PlayerLevelData data, ClassModel model) {
        int playerPoints = playerProvider.applyAsInt(data);
        int classPoints = (model != null && classProvider != null) ? classProvider.applyAsInt(model) : 0;
        // Bonus titres cumulés : on utilise le même provider pour extraire le bon champ du TitleBonus
        int titlePoints = 0;
        if (data != null && data.getUnlockedTitles() != null) {
            for (String titleId : data.getUnlockedTitles()) {
                com.eldanior.system.titles.models.TitleModel title = com.eldanior.system.titles.TitleManager.get(titleId);
                if (title != null && title.getBonus() != null) {
                    com.eldanior.system.titles.models.TitleBonus b = title.getBonus();
                    titlePoints += getTitleBonusForStat(b);
                }
            }
        }
        int total = playerPoints + classPoints + titlePoints;
        return data != null ? data.boosted(total) : total;   // x2 pendant la rune Arkies Domini (2026-11-17)
    }

    private int getTitleBonusForStat(com.eldanior.system.titles.models.TitleBonus b) {
        return switch (this) {
            case VITALITY -> b.vitality();
            case INTELLIGENCE -> b.intelligence();
            case ENDURANCE, ENDURANCE_DEFENSE -> b.endurance();
            case STRENGTH_DAMAGE -> b.strength();
            case AGILITY_SPEED, AGILITY_JUMP, AGILITY_FALL_RESISTANCE, DODGE_CHANCE,
                 DETECTION_RANGE, STEALTH_DETECTION -> b.agility();
            case LUCK_CRITICAL, LUCK_LOOT, LUCK_EVENT, THREAT_AWARENESS,
                 LOW_LIGHT_VISIBILITY, TRACKING_EVIDENCE -> b.luck();
        };
    }

    /**
     * Stats physiques boostees par la Rune d'Aura : endurance, force, vitesse, saut, agilite.
     * PAS la vie : activer l'aura augmente la vie max ET la vie courante -> l'activer/desactiver en boucle soignerait a l'infini.
     */
    private boolean isAuraPhysicalStat() {
        return switch (this) {
            case ENDURANCE, ENDURANCE_DEFENSE, STRENGTH_DAMAGE,
                 AGILITY_SPEED, AGILITY_JUMP, DODGE_CHANCE -> true;
            default -> false;
        };
    }

    // Calcule la valeur finale (Base + (Points * Ratio)), en respectant le CAP
    public float getFinalValue(PlayerLevelData data, ClassModel model) {
        int points = getTotalPoints(data, model);
        float value = this == DODGE_CHANCE ? dodgeCurve(points)
                : this == ENDURANCE_DEFENSE ? defenseCurve(points)
                : baseValue + (points * ratio);

        // --- LA MAGIE DES PASSIFS EST ICI ---
        if (data != null) {
            for (PlayerLevelData.ActivePassive a : data.getActivePassiveEntries()) {
                boolean mastered = data.isSkillMastered(a.skill().name());
                value += a.skill().getLogic().getFlatStatBonus(this, a.rank(), mastered);     // bonus bruts (+3 % de critique...)
                value *= a.skill().getLogic().getStatMultiplier(this, a.rank(), mastered);    // multiplicateurs (+5 % de vitesse...)
            }
        }
        // ------------------------------------

        // Bonus physique de la Rune d'Aura active (scale sur le palier de dignite)
        if (data != null && data.getAuraPhysicalBonus() > 0 && isAuraPhysicalStat()) {
            value *= 1f + data.getAuraPhysicalBonus();
        }

        // Rendements décroissants au-delà du cap (soft cap)
        // Au lieu d'un mur dur, les points au-delà donnent 30% d'efficacité
        // Plafond absolu : cap + 15% (ex: critique 80% → max 92%)
        if (this == DODGE_CHANCE) return Math.min(value, DODGE_MAX);   // plafond dur, pas de plafond souple
        if (this == ENDURANCE_DEFENSE) return Math.min(value, cap);     // 60 % au plus, passifs compris
        if (cap > 0 && value > cap) {
            float overflow = value - cap;
            float bonus = overflow * 0.3f;
            float maxBonus = cap * 0.15f;
            return cap + Math.min(bonus, maxBonus);
        }
        return value;
    }
}