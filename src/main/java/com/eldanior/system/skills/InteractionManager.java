package com.eldanior.system.skills;

import com.eldanior.system.classes.gui.OpenClassSelectionInteraction;
import com.eldanior.system.skills.interaction.ConsumableItemSkillInteraction;
import com.eldanior.system.skills.interaction.ConsumableItemStatsInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;

public class InteractionManager {

    public static void registerInteractions(JavaPlugin plugin) {
        // On récupère le registre dédié aux Interactions via le plugin
        plugin.getCodecRegistry(Interaction.CODEC)
                .register("ConsumableItemSkillInteraction",
                        ConsumableItemSkillInteraction.class, ConsumableItemSkillInteraction.CODEC);
        plugin.getCodecRegistry(Interaction.CODEC)
                .register("ConsumableItemStatsInteraction",
                        ConsumableItemStatsInteraction.class, ConsumableItemStatsInteraction.CODEC);
        plugin.getCodecRegistry(Interaction.CODEC)                                                  // indices lus en main (2026-11-14)
                .register("Eldanior_LireIndice", com.eldanior.system.histoires.LireIndiceInteraction.class,
                        com.eldanior.system.histoires.LireIndiceInteraction.CODEC);
        plugin.getCodecRegistry(Interaction.CODEC)                                                  // poche dimensionnelle (2026-11-16)
                .register("Eldanior_OuvrirPoche", com.eldanior.system.poche.OuvrirPocheInteraction.class,
                        com.eldanior.system.poche.OuvrirPocheInteraction.CODEC);
        plugin.getCodecRegistry(Interaction.CODEC)                                                  // livres lus en main (2026-11-14)
                .register("Eldanior_LireLivre", com.eldanior.system.histoires.LireLivreInteraction.class,
                        com.eldanior.system.histoires.LireLivreInteraction.CODEC);
        // TA NOUVELLE INTERACTION BENCH CLASS
        plugin.getCodecRegistry(Interaction.CODEC)
                .register("MuspelheimWrath",
                        com.eldanior.system.skills.runes.spells.MuspelheimWrathInteraction.class,
                        com.eldanior.system.skills.runes.spells.MuspelheimWrathInteraction.CODEC);
        plugin.getCodecRegistry(Interaction.CODEC)
                .register("TaranisSanctuary", com.eldanior.system.skills.runes.spells.TaranisSanctuaryInteraction.class, com.eldanior.system.skills.runes.spells.TaranisSanctuaryInteraction.CODEC);
        plugin.getCodecRegistry(Interaction.CODEC)
                .register("BahamutAdvent", com.eldanior.system.skills.runes.spells.BahamutAdventInteraction.class, com.eldanior.system.skills.runes.spells.BahamutAdventInteraction.CODEC);
        plugin.getCodecRegistry(Interaction.CODEC)
                .register("DragonAvatar", com.eldanior.system.skills.runes.spells.DragonAvatarInteraction.class, com.eldanior.system.skills.runes.spells.DragonAvatarInteraction.CODEC);
        plugin.getCodecRegistry(Interaction.CODEC)
                .register("ChronosHourglass", com.eldanior.system.skills.runes.spells.ChronosHourglassInteraction.class, com.eldanior.system.skills.runes.spells.ChronosHourglassInteraction.CODEC);
        plugin.getCodecRegistry(Interaction.CODEC)
                .register("ChaosAbyss", com.eldanior.system.skills.runes.spells.ChaosAbyssInteraction.class, com.eldanior.system.skills.runes.spells.ChaosAbyssInteraction.CODEC);
        plugin.getCodecRegistry(Interaction.CODEC)
                .register("RuneSpell", com.eldanior.system.skills.runes.spells.RuneSpellInteraction.class, com.eldanior.system.skills.runes.spells.RuneSpellInteraction.CODEC);
        plugin.getCodecRegistry(Interaction.CODEC)                                                  // detente du mousquet (2026-11-15)
                .register("Eldanior_MousquetTir", com.eldanior.system.skills.runes.spells.MousquetTirInteraction.class,
                        com.eldanior.system.skills.runes.spells.MousquetTirInteraction.CODEC);
        plugin.getCodecRegistry(Interaction.CODEC)                                                  // garde de la Rune de Transformation d'Arme
                .register("Eldanior_ArmeLegendaireTenue", com.eldanior.system.skills.runes.spells.ArmeLegendaireTenueInteraction.class,
                        com.eldanior.system.skills.runes.spells.ArmeLegendaireTenueInteraction.CODEC);
        plugin.getCodecRegistry(Interaction.CODEC)                                                  // lunette des armes a feu (2026-11-15)
                .register("Eldanior_SeuilEndurance", com.eldanior.system.skills.runes.spells.SeuilEnduranceInteraction.class,
                        com.eldanior.system.skills.runes.spells.SeuilEnduranceInteraction.CODEC);
        plugin.getCodecRegistry(Interaction.CODEC)                                                  // entrer dans une faille (2026-11-23)
                .register("Eldanior_EntrerFaille", com.eldanior.system.failles.EntrerFailleInteraction.class,
                        com.eldanior.system.failles.EntrerFailleInteraction.CODEC);
        com.eldanior.system.skills.runes.spells.RuneSpells.registerAll(); // sorts de rune du moteur commun
        plugin.getCodecRegistry(Interaction.CODEC)
                .register("BifrostBridge", com.eldanior.system.skills.runes.spells.BifrostBridgeInteraction.class, com.eldanior.system.skills.runes.spells.BifrostBridgeInteraction.CODEC);
        plugin.getCodecRegistry(Interaction.CODEC)
                .register("NyxVeil", com.eldanior.system.skills.runes.spells.NyxVeilInteraction.class, com.eldanior.system.skills.runes.spells.NyxVeilInteraction.CODEC);
        plugin.getCodecRegistry(Interaction.CODEC)
                .register("ApolloSanctuary", com.eldanior.system.skills.runes.spells.ApolloSanctuaryInteraction.class, com.eldanior.system.skills.runes.spells.ApolloSanctuaryInteraction.CODEC);
        plugin.getCodecRegistry(Interaction.CODEC)
                .register("GaiaAwakening", com.eldanior.system.skills.runes.spells.GaiaAwakeningInteraction.class, com.eldanior.system.skills.runes.spells.GaiaAwakeningInteraction.CODEC);
        plugin.getCodecRegistry(Interaction.CODEC)
                .register("YggdrasilAwakening", com.eldanior.system.skills.runes.spells.YggdrasilAwakeningInteraction.class, com.eldanior.system.skills.runes.spells.YggdrasilAwakeningInteraction.CODEC);
        plugin.getCodecRegistry(Interaction.CODEC)
                .register("GilgameshBabylon", com.eldanior.system.skills.runes.spells.GilgameshBabylonInteraction.class, com.eldanior.system.skills.runes.spells.GilgameshBabylonInteraction.CODEC);
        plugin.getCodecRegistry(Interaction.CODEC)
                .register("NiflheimGate", com.eldanior.system.skills.runes.spells.NiflheimGateInteraction.class, com.eldanior.system.skills.runes.spells.NiflheimGateInteraction.CODEC);
        plugin.getCodecRegistry(Interaction.CODEC)
                .register("OpenClassSelectionInteraction",
                        OpenClassSelectionInteraction.class, OpenClassSelectionInteraction.CODEC);

        // NOTE: L'interaction QuestNpcInteraction a ete retiree (ELD-131).
        // Le dialogue NPC de quete est desormais gere uniquement par
        // NpcQuestDetectionSystem (systeme ECS) pour eviter les doubles dialogues.
    }
}