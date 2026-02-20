package com.eldanior.system.TreasureChest.events;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.TreasureChest.resources.TreasureChestConfig;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.events.StartWorldEvent;

public class TreasureStartWorldEventListener {

    public static void onStartWorldEvent(StartWorldEvent event) {
        World defaultWorld = Universe.get().getDefaultWorld();
        World startingWorld = event.getWorld();

        if (defaultWorld != null) {
            // Si le monde qui démarre n'est pas le monde principal
            if (!defaultWorld.getName().equals(startingWorld.getName())) {

                TreasureChestConfig defaultConfig = defaultWorld.getChunkStore().getStore().getResource(EldaniorSystem.CONFIG_RESOURCE_TYPE);
                TreasureChestConfig startingConfig = startingWorld.getChunkStore().getStore().getResource(EldaniorSystem.CONFIG_RESOURCE_TYPE);

                // Les timers de reset globaux sont désactivés pour les mondes secondaires (gérés par le principal)
                startingConfig.setNextLootResetInterval(0);
                startingConfig.setNextLootReset(-1);

                // Copie des paramètres cosmétiques et de gameplay
                startingConfig.setLootRandom(defaultConfig.isLootChestRandom());
                startingConfig.setCanPlayerBreakLootChests(defaultConfig.isCanPlayerBreakLootChests());
                startingConfig.setParticlesAppear(defaultConfig.isParticlesAppear());
                startingConfig.setParticlesColor(defaultConfig.getParticlesColor());
                startingConfig.setMessageAppear(defaultConfig.isMessageAppear());
            }
        }
    }
}