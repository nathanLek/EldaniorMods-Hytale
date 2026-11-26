package com.eldanior.system.config;

import com.eldanior.system.skills.skillsInteraction.Metier;

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

    // Mapping : mot-cle du bench -> metier requis (les metiers sont sortis des passifs le 2026-11-24)
    // Utilise contains() donc matche "bench_weapon", "weapon_bench", etc.
    public static final Map<String, Metier> BENCH_SKILLS = Map.ofEntries(
            Map.entry("cooking", Metier.CRAFT_CUISINE),
            Map.entry("furnace", Metier.CRAFT_FONDERIE),
            Map.entry("armor", Metier.CRAFT_ARMURERIE),
            Map.entry("armour", Metier.CRAFT_ARMURERIE),
            Map.entry("weapon", Metier.CRAFT_FORGE_ARMES),
            Map.entry("forge_divine", Metier.CRAFT_FORGE_DIVINE),
            Map.entry("tannery", Metier.CRAFT_TANNERIE),
            Map.entry("alchemy", Metier.CRAFT_ALCHIMIE),
            Map.entry("lumbermill", Metier.CRAFT_SCIERIE),
            Map.entry("farming", Metier.CRAFT_AGRICULTURE),
            Map.entry("salvage", Metier.CRAFT_RECYCLAGE)
    );
}
