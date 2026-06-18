package com.eldanior.system.config;

import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.Map;
import java.util.Set;

/**
 * Configuration partagee des benchs d'artisanat.
 * Utilisee par CraftingRestrictionSystem et CraftingProgressionSystem.
 */
public final class CraftingConfig {

    private CraftingConfig() {}

    // Benchs accessibles a tous sans competence
    // Cles reduites au mot-cle pour matcher "bench_furniture" ET "furniture_bench"
    public static final Set<String> FREE_BENCHES = Set.of(
            "furniture", "builders", "campfire"
    );

    // Benchs bloques pour tout le monde (meme avec Artisanat)
    public static final Set<String> BLOCKED_BENCHES = Set.of(
            "arcane", "workbench"
    );

    // Mapping : mot-cle du bench -> competence requise
    // Utilise contains() donc matche "bench_weapon", "weapon_bench", etc.
    public static final Map<String, PassiveSkill> BENCH_SKILLS = Map.of(
            "cooking", PassiveSkill.CRAFT_CUISINE,
            "furnace", PassiveSkill.CRAFT_FONDERIE,
            "armor", PassiveSkill.CRAFT_ARMURERIE,
            "weapon", PassiveSkill.CRAFT_FORGE_ARMES,
            "tannery", PassiveSkill.CRAFT_TANNERIE,
            "alchemy", PassiveSkill.CRAFT_ALCHIMIE,
            "lumbermill", PassiveSkill.CRAFT_SCIERIE,
            "farming", PassiveSkill.CRAFT_AGRICULTURE,
            "salvage", PassiveSkill.CRAFT_RECYCLAGE
    );
}
