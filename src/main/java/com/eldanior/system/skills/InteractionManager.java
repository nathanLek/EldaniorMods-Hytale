package com.eldanior.system.skills;

import com.eldanior.system.classes.gui.OpenClassSelectionInteraction;
import com.eldanior.system.skills.interaction.ConsumableItemMoneyInteraction;
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
                .register("ConsumableItemMoneyInteraction",
                        ConsumableItemMoneyInteraction.class, ConsumableItemMoneyInteraction.CODEC);
        plugin.getCodecRegistry(Interaction.CODEC)
                .register("ConsumableItemStatsInteraction",
                        ConsumableItemStatsInteraction.class, ConsumableItemStatsInteraction.CODEC);
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