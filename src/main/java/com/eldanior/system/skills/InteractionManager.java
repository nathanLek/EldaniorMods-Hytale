package com.eldanior.system.skills;

import com.eldanior.system.classes.gui.OpenClassSelectionInteraction;
import com.eldanior.system.skills.interaction.ConsumableItemMoneyInteraction;
import com.eldanior.system.skills.interaction.ConsumableItemSkillInteraction;
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
        // TA NOUVELLE INTERACTION BENCH CLASS
        plugin.getCodecRegistry(Interaction.CODEC)
                .register("OpenClassSelectionInteraction",
                        OpenClassSelectionInteraction.class, OpenClassSelectionInteraction.CODEC);

    }
}