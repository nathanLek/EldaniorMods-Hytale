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

    ATTACK_SPEED(StatType.MECHANIC, -1, "Eldanior_Attack_Speed", 1.0f, 0.005f, 0.0f, 4.0f, // Cap à x4
            PlayerLevelData::getAgility, ClassModel::getBonusAgl),

    DODGE_CHANCE(StatType.MECHANIC, -1, "Agility_Dodge", 0.0f, 0.5f, 0.0f, 80.0f,
            PlayerLevelData::getAgility, ClassModel::getBonusAgl),

    // 5. DEGATS FORCE : +0.032 dégâts par point
    STRENGTH_DAMAGE(StatType.MECHANIC, -1, "Strength_Dmg", 0.0f, 0.072f, 0.0f, 50.0f,
            PlayerLevelData::getStrength, ClassModel::getBonusStr),

    // 6. DEFENSE ENDURANCE : -0.3 dégâts reçus par point
    ENDURANCE_DEFENSE(StatType.MECHANIC, -1, "Endurance_Def", 0.0f, 0.05f, 0.0f, 35.0f,
            PlayerLevelData::getEndurance, ClassModel::getBonusEnd),

    // === MÉCANIQUES DE CHANCE (Nouveau !) ===
    // 1. CRITIQUE : Ratio 0.027%, Cap à 80%
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
        return playerPoints + classPoints + titlePoints;
    }

    private int getTitleBonusForStat(com.eldanior.system.titles.models.TitleBonus b) {
        return switch (this) {
            case VITALITY -> b.vitality();
            case INTELLIGENCE -> b.intelligence();
            case ENDURANCE, ENDURANCE_DEFENSE -> b.endurance();
            case STRENGTH_DAMAGE -> b.strength();
            case AGILITY_SPEED, AGILITY_JUMP, AGILITY_FALL_RESISTANCE, ATTACK_SPEED, DODGE_CHANCE,
                 DETECTION_RANGE, STEALTH_DETECTION -> b.agility();
            case LUCK_CRITICAL, LUCK_LOOT, LUCK_EVENT, THREAT_AWARENESS,
                 LOW_LIGHT_VISIBILITY, TRACKING_EVIDENCE -> b.luck();
        };
    }

    // Calcule la valeur finale (Base + (Points * Ratio)), en respectant le CAP
    public float getFinalValue(PlayerLevelData data, ClassModel model) {
        int points = getTotalPoints(data, model);
        float value = baseValue + (points * ratio);

        // --- LA MAGIE DES PASSIFS EST ICI ---
        if (data != null && data.getActivePassives() != null) {
            for (PassiveSkill skill : data.getActivePassives()) {
                if (skill.getLogic() != null) {
                    // 1. On ajoute les bonus bruts (ex: +20% de chances de crit brutes)
                    value += skill.getLogic().getFlatStatBonus(this);

                    // 2. On applique les multiplicateurs (ex: x1.15 vitesse)
                    value *= skill.getLogic().getStatMultiplier(this);
                }
            }
        }
        // ------------------------------------

        // Rendements décroissants au-delà du cap (soft cap)
        // Au lieu d'un mur dur, les points au-delà donnent 30% d'efficacité
        // Plafond absolu : cap + 15% (ex: dodge 80% → max 92%)
        if (cap > 0 && value > cap) {
            float overflow = value - cap;
            float bonus = overflow * 0.3f;
            float maxBonus = cap * 0.15f;
            return cap + Math.min(bonus, maxBonus);
        }
        return value;
    }
}