package com.eldanior.system.skills.interaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StatsItemRegistry {

    private static final Map<String, StatsItemEffect> REGISTRY = new HashMap<>();

    public static void init() {

        // ========== PILLULES DE STATS (+1) ==========
        register("Consomable_Stat_Force_One", new StatsItemEffect("Pillule de Force", StatType.STRENGTH, 1));
        register("Consomable_Stat_Vitalite_One", new StatsItemEffect("Pillule de Vitalite", StatType.VITALITY, 1));
        register("Consomable_Stat_Intelligence_One", new StatsItemEffect("Pillule d'Intelligence", StatType.INTELLIGENCE, 1));
        register("Consomable_Stat_Endurance_One", new StatsItemEffect("Pillule d'Endurance", StatType.ENDURANCE, 1));
        register("Consomable_Stat_Agilite_One", new StatsItemEffect("Pillule d'Agilite", StatType.AGILITY, 1));
        register("Consomable_Stat_Chance_One", new StatsItemEffect("Pillule de Chance", StatType.LUCK, 1));

        // ========== PILLULES DE STATS (+5) ==========
        register("Consomable_Stat_Force_Five", new StatsItemEffect("Pillule de Force Superieure", StatType.STRENGTH, 5));
        register("Consomable_Stat_Vitalite_Five", new StatsItemEffect("Pillule de Vitalite Superieure", StatType.VITALITY, 5));
        register("Consomable_Stat_Intelligence_Five", new StatsItemEffect("Pillule d'Intelligence Superieure", StatType.INTELLIGENCE, 5));
        register("Consomable_Stat_Endurance_Five", new StatsItemEffect("Pillule d'Endurance Superieure", StatType.ENDURANCE, 5));
        register("Consomable_Stat_Agilite_Five", new StatsItemEffect("Pillule d'Agilite Superieure", StatType.AGILITY, 5));
        register("Consomable_Stat_Chance_Five", new StatsItemEffect("Pillule de Chance Superieure", StatType.LUCK, 5));

        // ========== PILLULES DE STATS (+10) ==========
        register("Consomable_Stat_Force_Ten", new StatsItemEffect("Pillule de Force Ultime", StatType.STRENGTH, 10));
        register("Consomable_Stat_Vitalite_Ten", new StatsItemEffect("Pillule de Vitalite Ultime", StatType.VITALITY, 10));
        register("Consomable_Stat_Intelligence_Ten", new StatsItemEffect("Pillule d'Intelligence Ultime", StatType.INTELLIGENCE, 10));
        register("Consomable_Stat_Endurance_Ten", new StatsItemEffect("Pillule d'Endurance Ultime", StatType.ENDURANCE, 10));
        register("Consomable_Stat_Agilite_Ten", new StatsItemEffect("Pillule d'Agilite Ultime", StatType.AGILITY, 10));
        register("Consomable_Stat_Chance_Ten", new StatsItemEffect("Pillule de Chance Ultime", StatType.LUCK, 10));

        // ========== ELIXIRS COMBINES ==========
        register("Elixir_Guerrier", StatsItemEffect.builder("Elixir du Guerrier")
                .add(StatType.STRENGTH, 3).add(StatType.ENDURANCE, 3).build());
        register("Elixir_Assassin", StatsItemEffect.builder("Elixir de l'Assassin")
                .add(StatType.AGILITY, 3).add(StatType.LUCK, 3).build());
        register("Elixir_Mage", StatsItemEffect.builder("Elixir du Mage")
                .add(StatType.INTELLIGENCE, 3).add(StatType.VITALITY, 3).build());
        register("Elixir_Archer", StatsItemEffect.builder("Elixir de l'Archer")
                .add(StatType.AGILITY, 3).add(StatType.LUCK, 3).build());
        register("Elixir_Complet", StatsItemEffect.builder("Elixir Complet")
                .add(StatType.STRENGTH, 1).add(StatType.VITALITY, 1).add(StatType.INTELLIGENCE, 1)
                .add(StatType.ENDURANCE, 1).add(StatType.AGILITY, 1).add(StatType.LUCK, 1).build());

        // ========== SPECIAUX ==========
        register("Parchemin_Relance", new StatsItemEffect("Parchemin de Relance", StatType.REROLL, -1));
        register("Tome_Experience", new StatsItemEffect("Tome d'Experience", StatType.XP, 5000));
        register("Tome_Niveau", new StatsItemEffect("Tome de Niveau", StatType.LEVEL, 1));

        // ========== DECRETS DE NOBLESSE ==========
        register("Decret_Chevalier", StatsItemEffect.rank("Decret de Chevalier", StatType.NOBILITY_RANK, "CHEVALIER"));
        register("Decret_Baron", StatsItemEffect.rank("Decret de Baron", StatType.NOBILITY_RANK, "BARON"));
        register("Decret_Comte", StatsItemEffect.rank("Decret de Comte", StatType.NOBILITY_RANK, "COMTE"));
        register("Decret_Duc", StatsItemEffect.rank("Decret de Duc", StatType.NOBILITY_RANK, "DUC"));
        register("Decret_Marquis", StatsItemEffect.rank("Decret de Marquis", StatType.NOBILITY_RANK, "MARQUIS"));

        // ========== DIGNITE ==========
        register("Essence_Dignite", new StatsItemEffect("Essence de Dignite", StatType.DIGNITY, 1));

        // ========== BENEDICTIONS D'EGLISE ==========
        register("Benediction_Pretre", StatsItemEffect.rank("Benediction de Pretre", StatType.CHURCH_RANK, "PRETRE"));
        register("Benediction_Archeveque", StatsItemEffect.rank("Benediction d'Archeveque", StatType.CHURCH_RANK, "ARCHEVEQUE"));
        register("Benediction_Cardinal", StatsItemEffect.rank("Benediction de Cardinal", StatType.CHURCH_RANK, "CARDINAL"));

        System.out.println("[Eldanior] " + REGISTRY.size() + " items consommables stats charges.");
    }

    public static void register(String itemId, StatsItemEffect effect) {
        REGISTRY.put(itemId, effect);
    }

    public static Optional<StatsItemEffect> getEffect(String itemId) {
        // Cherche d'abord l'ID exact, puis sans le prefix namespace
        StatsItemEffect effect = REGISTRY.get(itemId);
        if (effect == null && itemId.contains(":")) {
            effect = REGISTRY.get(itemId.substring(itemId.indexOf(':') + 1));
        }
        return Optional.ofNullable(effect);
    }
}
