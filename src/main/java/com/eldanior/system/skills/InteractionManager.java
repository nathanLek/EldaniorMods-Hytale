package com.eldanior.system.skills;

import com.eldanior.system.skills.interaction.ConsumableItemSkillInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;

public class InteractionManager {

    public static void registerInteractions(JavaPlugin plugin) {
        // On récupère le registre dédié aux Interactions via le plugin
        plugin.getCodecRegistry(Interaction.CODEC)
                .register("eldaniorsystem:ConsumableItemSkillInteraction",
                        ConsumableItemSkillInteraction.class, ConsumableItemSkillInteraction.CODEC);
    }
}