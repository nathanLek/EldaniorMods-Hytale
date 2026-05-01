package com.eldanior.system.config.Effects;

import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.eldanior.system.config.EldaniorLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapping centralise : PassiveSkill -> EntityEffect ID de Hytale.
 * Quand une competence proc, l'effet visuel associe est joue automatiquement.
 */
public class SkillEffectConfig {

    // Effet sur l'ATTAQUANT quand il proc
    private static final Map<PassiveSkill, String> ATTACKER_EFFECTS = new HashMap<>();
    // Effet sur la VICTIME quand le skill proc
    private static final Map<PassiveSkill, String> VICTIM_EFFECTS = new HashMap<>();

    static {
        // ==================== ATTAQUE ====================
        // Frappes critiques -> Red Flash sur la victime
        VICTIM_EFFECTS.put(PassiveSkill.INSTINCTIVE_STRIKE, "Red_Flash");
        VICTIM_EFFECTS.put(PassiveSkill.PREDATORY_STRIKE, "Red_Flash");
        VICTIM_EFFECTS.put(PassiveSkill.FURY_STRIKE, "Red_Flash");
        VICTIM_EFFECTS.put(PassiveSkill.OPPORTUNIST_STRIKE, "Red_Flash");
        VICTIM_EFFECTS.put(PassiveSkill.COSMIC_STRIKE, "Red_Flash");
        VICTIM_EFFECTS.put(PassiveSkill.DIVINE_STRIKE, "Red_Flash");
        VICTIM_EFFECTS.put(PassiveSkill.GENESIS_STRIKE, "Red_Flash");
        VICTIM_EFFECTS.put(PassiveSkill.CREATOR_STRIKE, "Red_Flash");

        // Lames speciales -> effet sombre sur l'attaquant
        ATTACKER_EFFECTS.put(PassiveSkill.CRIMSON_BLADE, "Dagger_Dash");
        ATTACKER_EFFECTS.put(PassiveSkill.VOID_BLADE, "Intangible_Dark");
        ATTACKER_EFFECTS.put(PassiveSkill.ABYSS_BLADE, "Intangible_Dark");
        ATTACKER_EFFECTS.put(PassiveSkill.GENESIS_EDGE, "Intangible_Dark");
        ATTACKER_EFFECTS.put(PassiveSkill.CREATOR_EDGE, "Intangible_Dark");

        // Frappes sismiques -> effet de frappe au sol
        ATTACKER_EFFECTS.put(PassiveSkill.SEISMIC_STRIKE, "Battleaxe_Downstrike_Jump");

        // Pression -> stun sur la victime
        VICTIM_EFFECTS.put(PassiveSkill.PRESSURE_POINT, "Slow");
        VICTIM_EFFECTS.put(PassiveSkill.VITAL_PRESSURE, "Slow");
        VICTIM_EFFECTS.put(PassiveSkill.CRUSHING_PRESSURE, "Stun");
        VICTIM_EFFECTS.put(PassiveSkill.ANNIHILATING_PRESSURE, "Stun");
        VICTIM_EFFECTS.put(PassiveSkill.SOUL_CRUSHING_PRESSURE, "Stun");

        // Empoisonnement
        VICTIM_EFFECTS.put(PassiveSkill.HAUNTING_THRUST, "Poison_T1");

        // Chasse -> brulure
        VICTIM_EFFECTS.put(PassiveSkill.BLOOD_HUNT, "Burn");
        VICTIM_EFFECTS.put(PassiveSkill.DEATH_HUNT, "Burn");
        VICTIM_EFFECTS.put(PassiveSkill.RELENTLESS_HUNT, "Slow");

        // Jugement -> explosion
        VICTIM_EFFECTS.put(PassiveSkill.KODA_JUDGMENT, "Bomb_Explode_Stun");

        // Spellblade -> brulure magique
        VICTIM_EFFECTS.put(PassiveSkill.SPELLBLADE, "Flame_Staff_Burn");

        // ==================== DEFENSE ====================
        // Peau de pierre -> Stoneskin
        ATTACKER_EFFECTS.put(PassiveSkill.STONE_SKIN, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.FORTIFIED_SKIN, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.DIAMOND_SKIN, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.IRON_BODY, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.STEEL_BODY, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.DIAMOND_BODY, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.COSMIC_BODY, "Stoneskin");

        // Parades -> dodge visuel
        ATTACKER_EFFECTS.put(PassiveSkill.MINOR_PARRY, "Dodge_Left");
        ATTACKER_EFFECTS.put(PassiveSkill.PERFECT_PARRY, "Dodge_Right");
        ATTACKER_EFFECTS.put(PassiveSkill.CREATOR_PARRY, "Dodge_Right");

        // Boucliers divins -> immunite
        ATTACKER_EFFECTS.put(PassiveSkill.DIVINE_AEGIS, "Immune");
        ATTACKER_EFFECTS.put(PassiveSkill.DYNA_AEGIS, "Immune");

        // Resolve -> immunite flash
        ATTACKER_EFFECTS.put(PassiveSkill.IRON_RESOLVE, "Dodge_Invulnerability");
        ATTACKER_EFFECTS.put(PassiveSkill.STEEL_RESOLVE, "Dodge_Invulnerability");
        ATTACKER_EFFECTS.put(PassiveSkill.IMMORTAL_RESOLVE, "Dodge_Invulnerability");

        // Incassable -> fatigue l'attaquant
        VICTIM_EFFECTS.put(PassiveSkill.UNBREAKABLE, "Stamina_Broken");
        VICTIM_EFFECTS.put(PassiveSkill.UNYIELDING, "Slow");

        // ==================== ESQUIVE SPECIALES ====================
        ATTACKER_EFFECTS.put(PassiveSkill.SHADOW_DODGE, "Intangible_Dark");
        ATTACKER_EFFECTS.put(PassiveSkill.PHANTOM_DODGE, "Intangible_Smol");
        ATTACKER_EFFECTS.put(PassiveSkill.DIMENSIONAL_DODGE, "Intangible_Smol");
        ATTACKER_EFFECTS.put(PassiveSkill.FATE_DODGE, "Dodge_Invulnerability");

        // ==================== MAGIE ====================
        VICTIM_EFFECTS.put(PassiveSkill.ARCANE_STRIKE, "Flame_Staff_Burn");
        VICTIM_EFFECTS.put(PassiveSkill.ARCANE_DEVASTATION, "Flame_Staff_Burn");
        VICTIM_EFFECTS.put(PassiveSkill.ARCANE_ANNIHILATION, "Flame_Staff_Burn");
        VICTIM_EFFECTS.put(PassiveSkill.ARCANE_SUPREMACY, "Flame_Staff_Burn");
        VICTIM_EFFECTS.put(PassiveSkill.ARCANE_GENESIS, "Flame_Staff_Burn");
        VICTIM_EFFECTS.put(PassiveSkill.ARCANE_CREATION, "Flame_Staff_Burn");

        // ==================== DRAIN / VAMPIRISME ====================
        VICTIM_EFFECTS.put(PassiveSkill.SPIRITUAL_SIPHON, "Mana_Drain");
        VICTIM_EFFECTS.put(PassiveSkill.SPIRIT_DRAIN, "Mana_Drain");

        // ==================== VITESSE ====================
        ATTACKER_EFFECTS.put(PassiveSkill.WIND_STEP, "Dodge_Left");
        ATTACKER_EFFECTS.put(PassiveSkill.STORM_STEP, "Dodge_Right");
        ATTACKER_EFFECTS.put(PassiveSkill.VOID_STEP, "Intangible_Dark");
        ATTACKER_EFFECTS.put(PassiveSkill.DIMENSIONAL_STEP, "Intangible_Smol");
        ATTACKER_EFFECTS.put(PassiveSkill.GALE_STEP, "Dodge_Left");

        // ==================== DAGGER ====================
        ATTACKER_EFFECTS.put(PassiveSkill.DAGGER_MASTERY, "Dagger_Dash");
        ATTACKER_EFFECTS.put(PassiveSkill.SHADOW_BLADE_MASTERY, "Dagger_Pounce");
        ATTACKER_EFFECTS.put(PassiveSkill.ASSASSIN_BLADE_MASTERY, "Dagger_Signature");
        ATTACKER_EFFECTS.put(PassiveSkill.CREATOR_DAGGER_MASTERY, "Dagger_Signature");

        // ==================== ARMES LOURDES ====================
        ATTACKER_EFFECTS.put(PassiveSkill.GREAT_AXE_MASTERY, "Battleaxe_Whirlwind");
        ATTACKER_EFFECTS.put(PassiveSkill.WAR_AXE_MASTERY, "Battleaxe_Whirlwind");
        ATTACKER_EFFECTS.put(PassiveSkill.SWORD_MASTERY, "Sword_Signature_SpinStab");
        ATTACKER_EFFECTS.put(PassiveSkill.SPEAR_MASTERY, "Mace_Signature");
        ATTACKER_EFFECTS.put(PassiveSkill.BOW_MASTERY, "Crossbow_Combo_1");
        ATTACKER_EFFECTS.put(PassiveSkill.SNIPER_MASTERY, "Crossbow_Combo_2");
        ATTACKER_EFFECTS.put(PassiveSkill.MARKSMAN_MASTERY, "Crossbow_Combo_1");
        ATTACKER_EFFECTS.put(PassiveSkill.MINOR_SWORD_MASTERY, "Sword_Signature_SpinStab");

        // ==================== UNCOMMON ====================
        // Attaque
        VICTIM_EFFECTS.put(PassiveSkill.SHARP_BLADE, "Red_Flash");
        ATTACKER_EFFECTS.put(PassiveSkill.COMBATANT_SWIFTNESS, "Dagger_Dash");
        VICTIM_EFFECTS.put(PassiveSkill.DEEP_SLASH, "Red_Flash");
        ATTACKER_EFFECTS.put(PassiveSkill.DUELIST_SWIFTNESS, "Dodge_Left");

        // Defense
        ATTACKER_EFFECTS.put(PassiveSkill.BRONZE_SKIN, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.EXPERT_PARRY, "Dodge_Right");
        ATTACKER_EFFECTS.put(PassiveSkill.BEAST_GUARD, "Stoneskin");

        // Chance
        VICTIM_EFFECTS.put(PassiveSkill.CRITICAL_LUCK, "Red_Flash");
        VICTIM_EFFECTS.put(PassiveSkill.FATED_OMEN, "Red_Flash");
        ATTACKER_EFFECTS.put(PassiveSkill.GOLDEN_TOUCH, "Drop_Rare");
        ATTACKER_EFFECTS.put(PassiveSkill.RELIC_HUNTER, "Drop_Uncommon");

        // Magique
        ATTACKER_EFFECTS.put(PassiveSkill.EXPANDED_MIND, "Mana_Regen");
        ATTACKER_EFFECTS.put(PassiveSkill.UNLEASHED_MAGIC, "Mana_High");
        ATTACKER_EFFECTS.put(PassiveSkill.ASTRAL_CLOAK, "Intangible_Smol");
        ATTACKER_EFFECTS.put(PassiveSkill.ARCANE_SHIELD, "Stoneskin");

        // Regeneration
        ATTACKER_EFFECTS.put(PassiveSkill.TROLL_BLOOD, "Food_Health_Regen_Small");
        VICTIM_EFFECTS.put(PassiveSkill.SOUL_STEALER, "Mana_Drain");
        ATTACKER_EFFECTS.put(PassiveSkill.VITAL_RECOVERY, "Food_Health_Regen_Tiny");
        ATTACKER_EFFECTS.put(PassiveSkill.IRON_LUNGS, "Food_Stamina_Regen_Small");
        ATTACKER_EFFECTS.put(PassiveSkill.MANA_STREAM, "Mana_Regen");

        // Resistance
        ATTACKER_EFFECTS.put(PassiveSkill.FORTIFICATION, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.BATTLE_SCARS, "Dodge_Invulnerability");
        ATTACKER_EFFECTS.put(PassiveSkill.REACTIVE_BULWARK, "Stoneskin");

        // Vie
        ATTACKER_EFFECTS.put(PassiveSkill.OVERFLOWING_LIFE, "Food_Health_Boost_Small");
        ATTACKER_EFFECTS.put(PassiveSkill.ENRICHED_BLOOD, "Food_Health_Regen_Tiny");
        ATTACKER_EFFECTS.put(PassiveSkill.UNDYING, "Immune");

        // Endurance
        ATTACKER_EFFECTS.put(PassiveSkill.BATTLE_FRENZY, "Dagger_Dash");
        ATTACKER_EFFECTS.put(PassiveSkill.UNMOVABLE_MOUNTAIN, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.SURVIVOR_SPIRIT, "Food_Health_Regen_Tiny");

        // Agilite
        ATTACKER_EFFECTS.put(PassiveSkill.THUNDER_REFLEXES, "Dodge_Right");
        ATTACKER_EFFECTS.put(PassiveSkill.MARATHON_RUNNER, "Dodge_Left");
        ATTACKER_EFFECTS.put(PassiveSkill.ACROBATIC_POISE, "Dodge_Right");

        // Detection
        ATTACKER_EFFECTS.put(PassiveSkill.DARK_VISION, "Intangible_Dark");
        ATTACKER_EFFECTS.put(PassiveSkill.PSYCHIC_AWARENESS, "Mana_Regen_Low");

        // ==================== RARE ====================
        VICTIM_EFFECTS.put(PassiveSkill.CRIMSON_BLADE, "Burn");
        ATTACKER_EFFECTS.put(PassiveSkill.WARRIOR_SWIFTNESS, "Dagger_Dash");
        VICTIM_EFFECTS.put(PassiveSkill.DESTINY_STRIKE, "Red_Flash");
        ATTACKER_EFFECTS.put(PassiveSkill.LIGHTNING_REFLEXES, "Dodge_Right");
        ATTACKER_EFFECTS.put(PassiveSkill.DEADLY_PRECISION, "Dagger_Pounce");
        ATTACKER_EFFECTS.put(PassiveSkill.IRON_SKIN, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.MASTER_PARRY, "Dodge_Left");
        ATTACKER_EFFECTS.put(PassiveSkill.MONSTER_SLAYER_GUARD, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.STEEL_BODY, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.TITAN_RESOLVE, "Dodge_Invulnerability");
        ATTACKER_EFFECTS.put(PassiveSkill.IRON_FORTIFICATION, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.WAR_FRENZY, "Battleaxe_Whirlwind");
        ATTACKER_EFFECTS.put(PassiveSkill.LIVING_FORTRESS, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.DRAGON_LUNGS, "Food_Stamina_Regen_Medium");
        ATTACKER_EFFECTS.put(PassiveSkill.HYDRA_BLOOD, "Food_Health_Regen_Medium");
        ATTACKER_EFFECTS.put(PassiveSkill.MANA_RIVER, "Mana_Regen");
        ATTACKER_EFFECTS.put(PassiveSkill.BRILLIANT_MIND, "Mana_Regen");
        ATTACKER_EFFECTS.put(PassiveSkill.PURE_MAGIC, "Mana_High");
        ATTACKER_EFFECTS.put(PassiveSkill.MANA_FORTRESS, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.TITAN_CONSTITUTION, "Food_Health_Boost_Medium");
        ATTACKER_EFFECTS.put(PassiveSkill.BURSTING_LIFE, "Food_Health_Boost_Small");
        ATTACKER_EFFECTS.put(PassiveSkill.ANCIENT_BLOOD, "Food_Health_Regen_Small");
        VICTIM_EFFECTS.put(PassiveSkill.SPIRIT_DRAIN, "Mana_Drain");
        VICTIM_EFFECTS.put(PassiveSkill.SHADOW_DODGE, "Intangible_Dark");
        VICTIM_EFFECTS.put(PassiveSkill.PROPHECY_OMEN, "Red_Flash");

        // ==================== EPIQUE ====================
        VICTIM_EFFECTS.put(PassiveSkill.VOID_BLADE, "Intangible_Dark");
        VICTIM_EFFECTS.put(PassiveSkill.DEATH_HUNT, "Burn");
        VICTIM_EFFECTS.put(PassiveSkill.DETECTIONOFVITALPOINTS, "Red_Flash");
        ATTACKER_EFFECTS.put(PassiveSkill.BERSERKER_SWIFTNESS, "Battleaxe_Whirlwind");
        ATTACKER_EFFECTS.put(PassiveSkill.FATAL_PRECISION, "Dagger_Pounce");
        ATTACKER_EFFECTS.put(PassiveSkill.DIVINE_REFLEXES, "Dodge_Right");
        ATTACKER_EFFECTS.put(PassiveSkill.GRAVITY_DEFIANCE, "Dodge_Invulnerability");
        ATTACKER_EFFECTS.put(PassiveSkill.VOID_STEP, "Intangible_Dark");
        ATTACKER_EFFECTS.put(PassiveSkill.DIMENSIONAL_DODGE, "Intangible_Smol");
        ATTACKER_EFFECTS.put(PassiveSkill.STEEL_SKIN, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.GOD_RESOLVE, "Immune");
        ATTACKER_EFFECTS.put(PassiveSkill.PERFECT_PARRY, "Dodge_Right");
        ATTACKER_EFFECTS.put(PassiveSkill.DRAGON_BLOOD, "Food_Health_Regen_Large");
        ATTACKER_EFFECTS.put(PassiveSkill.ETERNAL_LIFE, "Food_Health_Boost_Large");
        ATTACKER_EFFECTS.put(PassiveSkill.GOD_CONSTITUTION, "Food_Health_Boost_Large");
        ATTACKER_EFFECTS.put(PassiveSkill.INVINCIBLE, "Immune");
        ATTACKER_EFFECTS.put(PassiveSkill.MYTHRIL_FORTIFICATION, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.DIVINE_BULWARK, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.WAR_LEGEND, "Battleaxe_Whirlwind");
        ATTACKER_EFFECTS.put(PassiveSkill.ETERNAL_FORTRESS, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.TITAN_LUNGS, "Food_Stamina_Regen_Large");
        ATTACKER_EFFECTS.put(PassiveSkill.BLOOD_FRENZY, "Dagger_Dash");
        ATTACKER_EFFECTS.put(PassiveSkill.ADAMANTINE_SKIN, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.GENIUS_MIND, "Mana_High");
        ATTACKER_EFFECTS.put(PassiveSkill.MANA_CITADEL, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.MANA_OCEAN, "Mana_Regen_High");
        ATTACKER_EFFECTS.put(PassiveSkill.PHOENIX_BLOOD, "Food_Health_Regen_Large");
        VICTIM_EFFECTS.put(PassiveSkill.ARCANE_VAMPIRISM, "Mana_Drain");
        VICTIM_EFFECTS.put(PassiveSkill.DIVINE_STRIKE, "Red_Flash");
        VICTIM_EFFECTS.put(PassiveSkill.COSMIC_OMEN, "Red_Flash");
        VICTIM_EFFECTS.put(PassiveSkill.LEGEND_HUNTER, "Red_Flash");
        ATTACKER_EFFECTS.put(PassiveSkill.SOUL_READER, "Mana_Regen_Low");

        // ==================== LEGENDAIRE ====================
        ATTACKER_EFFECTS.put(PassiveSkill.CELESTIAL_STEP, "Intangible_Smol");
        ATTACKER_EFFECTS.put(PassiveSkill.OMNISCIENT_PRECISION, "Dagger_Pounce");
        VICTIM_EFFECTS.put(PassiveSkill.ANNIHILATOR_STRIKE, "Bomb_Explode_Stun");
        ATTACKER_EFFECTS.put(PassiveSkill.GOD_SLAYER_SWIFTNESS, "Intangible_Dark");
        VICTIM_EFFECTS.put(PassiveSkill.GENESIS_STRIKE, "Red_Flash");
        ATTACKER_EFFECTS.put(PassiveSkill.TIME_DODGE, "Dodge_Invulnerability");
        ATTACKER_EFFECTS.put(PassiveSkill.CELESTIAL_PARRY, "Dodge_Right");
        ATTACKER_EFFECTS.put(PassiveSkill.ETERNITY_RESOLVE, "Immune");
        ATTACKER_EFFECTS.put(PassiveSkill.DIAMOND_SKIN, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.GENESIS_BULWARK, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.CELESTIAL_IMMORTALITY, "Immune");
        ATTACKER_EFFECTS.put(PassiveSkill.CELESTIAL_CONSTITUTION, "Food_Health_Boost_Large");
        ATTACKER_EFFECTS.put(PassiveSkill.HEART_OF_GENESIS, "Food_Health_Boost_Large");
        ATTACKER_EFFECTS.put(PassiveSkill.INFINITE_VAMPIRISM, "Healing_Totem_Heal");
        VICTIM_EFFECTS.put(PassiveSkill.INFINITE_VAMPIRISM, "Mana_Drain");
        ATTACKER_EFFECTS.put(PassiveSkill.GENESIS_BLOOD, "Food_Health_Regen_Large");
        ATTACKER_EFFECTS.put(PassiveSkill.INFINITE_MIND, "Mana_High");
        ATTACKER_EFFECTS.put(PassiveSkill.ARCANE_GENESIS, "Flame_Staff_Burn");
        ATTACKER_EFFECTS.put(PassiveSkill.CELESTIAL_FORTRESS, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.CELESTIAL_LUNGS, "Food_Stamina_Regen_Large");
        ATTACKER_EFFECTS.put(PassiveSkill.VOL, "Intangible_Smol");

        // ==================== DIVIN ====================
        ATTACKER_EFFECTS.put(PassiveSkill.CREATOR_STEP, "Intangible_Smol");
        ATTACKER_EFFECTS.put(PassiveSkill.CREATOR_PRECISION, "Dagger_Signature");
        ATTACKER_EFFECTS.put(PassiveSkill.CREATOR_SWIFTNESS, "Intangible_Dark");
        VICTIM_EFFECTS.put(PassiveSkill.JUDGMENT_OF_GENESIS, "Bomb_Explode_Stun");
        ATTACKER_EFFECTS.put(PassiveSkill.ABSOLUTE_RESOLVE, "Immune");
        ATTACKER_EFFECTS.put(PassiveSkill.CREATOR_BULWARK, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.TRUE_IMMORTALITY, "Immune");
        ATTACKER_EFFECTS.put(PassiveSkill.CREATOR_CONSTITUTION, "Food_Health_Boost_Large");
        ATTACKER_EFFECTS.put(PassiveSkill.HEART_OF_CREATION, "Food_Health_Boost_Large");
        ATTACKER_EFFECTS.put(PassiveSkill.CREATOR_VAMPIRISM, "Healing_Totem_Heal");
        VICTIM_EFFECTS.put(PassiveSkill.CREATOR_VAMPIRISM, "Mana_Drain");
        ATTACKER_EFFECTS.put(PassiveSkill.CREATOR_BLOOD, "Food_Health_Regen_Large");
        ATTACKER_EFFECTS.put(PassiveSkill.MANA_HEART, "Mana_Regen_High");
        ATTACKER_EFFECTS.put(PassiveSkill.CREATOR_MIND, "Mana_High");
        ATTACKER_EFFECTS.put(PassiveSkill.ARCANE_CREATION, "Flame_Staff_Burn");
        ATTACKER_EFFECTS.put(PassiveSkill.CREATOR_FORTRESS, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.CREATOR_LUNGS, "Food_Stamina_Regen_Large");

        // ==================== UNIQUE ====================
        ATTACKER_EFFECTS.put(PassiveSkill.ABSOLUTE_PRECISION, "Dagger_Signature");
        ATTACKER_EFFECTS.put(PassiveSkill.COSMIC_REFLEXES, "Dodge_Right");
        ATTACKER_EFFECTS.put(PassiveSkill.DEMIGOD_SWIFTNESS, "Intangible_Dark");
        VICTIM_EFFECTS.put(PassiveSkill.PHANTOM_STRIKE, "Red_Flash");
        VICTIM_EFFECTS.put(PassiveSkill.MAUVAIS_PRESAGE, "Slow");
        ATTACKER_EFFECTS.put(PassiveSkill.REALITY_DODGE, "Intangible_Smol");
        VICTIM_EFFECTS.put(PassiveSkill.MYTH_HUNTER, "Red_Flash");
        ATTACKER_EFFECTS.put(PassiveSkill.DIVINE_PARRY, "Dodge_Right");
        ATTACKER_EFFECTS.put(PassiveSkill.OBSIDIAN_SKIN, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.COSMIC_BULWARK, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.ETERNAL_FORTIFICATION, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.COSMIC_CONSTITUTION, "Food_Health_Boost_Large");
        ATTACKER_EFFECTS.put(PassiveSkill.HEART_OF_ETERNITY, "Food_Health_Boost_Large");
        ATTACKER_EFFECTS.put(PassiveSkill.INFINITE_LIFE, "Food_Health_Boost_Large");
        ATTACKER_EFFECTS.put(PassiveSkill.COSMIC_VAMPIRISM, "Healing_Totem_Heal");
        VICTIM_EFFECTS.put(PassiveSkill.COSMIC_VAMPIRISM, "Mana_Drain");
        ATTACKER_EFFECTS.put(PassiveSkill.ETERNAL_BLOOD, "Food_Health_Regen_Large");
        ATTACKER_EFFECTS.put(PassiveSkill.MANA_INFINITY, "Mana_Regen_High");
        ATTACKER_EFFECTS.put(PassiveSkill.COSMIC_MIND, "Mana_High");
        ATTACKER_EFFECTS.put(PassiveSkill.ARCANE_OBLIVION, "Flame_Staff_Burn");
        ATTACKER_EFFECTS.put(PassiveSkill.DIVINE_FORTRESS, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.COSMIC_LUNGS, "Food_Stamina_Regen_Large");
        ATTACKER_EFFECTS.put(PassiveSkill.RAGE_FRENZY, "Battleaxe_Whirlwind");

        // ==================== FAMILY ====================
        ATTACKER_EFFECTS.put(PassiveSkill.FAMILY_DIVINE_LIGHT, "Immune");
        ATTACKER_EFFECTS.put(PassiveSkill.FAMILY_DRAGON_FURY, "Burn");
        ATTACKER_EFFECTS.put(PassiveSkill.FAMILY_FROST_RESILIENCE, "Freeze");
        ATTACKER_EFFECTS.put(PassiveSkill.FAMILY_GOLDEN_FORTUNE, "Drop_Legendary");
        ATTACKER_EFFECTS.put(PassiveSkill.FAMILY_STORM_VIGOR, "Dodge_Right");
        VICTIM_EFFECTS.put(PassiveSkill.FAMILY_SHADOW_STRIKE, "Intangible_Dark");
        ATTACKER_EFFECTS.put(PassiveSkill.FAMILY_PHOENIX_BLOOD, "Food_Health_Regen_Large");
        ATTACKER_EFFECTS.put(PassiveSkill.FAMILY_ROYAL_AUTHORITY, "Immune");
    }

    /**
     * Retourne l'effet a jouer sur l'attaquant quand ce skill proc.
     */
    public static String getAttackerEffect(PassiveSkill skill) {
        return ATTACKER_EFFECTS.get(skill);
    }

    /**
     * Retourne l'effet a jouer sur la victime quand ce skill proc.
     */
    public static String getVictimEffect(PassiveSkill skill) {
        return VICTIM_EFFECTS.get(skill);
    }

    /**
     * Applique les effets visuels d'un skill qui vient de proc.
     */
    public static void applySkillEffects(PassiveSkill skill,
                                          com.hypixel.hytale.component.Ref<com.hypixel.hytale.server.core.universe.world.storage.EntityStore> attackerRef,
                                          com.hypixel.hytale.component.Ref<com.hypixel.hytale.server.core.universe.world.storage.EntityStore> victimRef,
                                          com.hypixel.hytale.component.ComponentAccessor<com.hypixel.hytale.server.core.universe.world.storage.EntityStore> store) {
        try {
            String attackerEffect = ATTACKER_EFFECTS.get(skill);
            if (attackerEffect != null && attackerRef != null) {
                EffectsManager.applyEffect(attackerRef, attackerEffect, store);
            }

            String victimEffect = VICTIM_EFFECTS.get(skill);
            if (victimEffect != null && victimRef != null) {
                EffectsManager.applyEffect(victimRef, victimEffect, store);
            }
        } catch (Exception e) { EldaniorLogger.error("SkillEffectConfig", e); }
    }
}
