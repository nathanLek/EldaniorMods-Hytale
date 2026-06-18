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
    public static final Map<String, PassiveSkill> BENCH_SKILLS = Map.ofEntries(
            Map.entry("cooking", PassiveSkill.CRAFT_CUISINE),
            Map.entry("furnace", PassiveSkill.CRAFT_FONDERIE),
            Map.entry("armor", PassiveSkill.CRAFT_ARMURERIE),
            Map.entry("armour", PassiveSkill.CRAFT_ARMURERIE),
            Map.entry("weapon", PassiveSkill.CRAFT_FORGE_ARMES),
            Map.entry("tannery", PassiveSkill.CRAFT_TANNERIE),
            Map.entry("alchemy", PassiveSkill.CRAFT_ALCHIMIE),
            Map.entry("lumbermill", PassiveSkill.CRAFT_SCIERIE),
            Map.entry("farming", PassiveSkill.CRAFT_AGRICULTURE),
            Map.entry("salvage", PassiveSkill.CRAFT_RECYCLAGE)
    );
}
