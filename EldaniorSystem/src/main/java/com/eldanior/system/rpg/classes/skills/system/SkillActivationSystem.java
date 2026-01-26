package com.eldanior.system.rpg.classes.skills.system;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.components.PlayerLevelData;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;

import javax.annotation.Nonnull;

public class SkillActivationSystem extends EntityTickingSystem<EntityStore> {

    private long lastDebugTime = 0;

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        // 1. Vérification que le système tourne (1 log toutes les 5 secondes max)
        long now = System.currentTimeMillis();
        if (now - lastDebugTime > 5000) {
            System.out.println("[ELDANIOR] SkillActivationSystem est EN VIE et scanne...");
            lastDebugTime = now;
        }

        Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
        if (!entityRef.isValid()) return;

        // 2. Vérification du composant Interactions
        Interactions interactions = store.getComponent(entityRef, Interactions.getComponentType());

        // Si le joueur n'a pas de composant Interactions, c'est grave, on le log
        if (interactions == null) {
            if (now % 100 == 0) System.out.println("[ELDANIOR] ALERTE: Joueur sans composant Interactions !");
            return;
        }

        // 3. SCAN LARGE : On regarde toutes les interactions possibles
        // Si une interaction (n'importe laquelle) est détectée, on l'affiche
        boolean clickDetected = false;

        // Test Interaction Secondaire (Clic Droit)
        if (interactions.getInteractionId(InteractionType.Secondary) != null) {
            System.out.println("[ELDANIOR] Clic Droit (Secondary) détecté ! ID: " + interactions.getInteractionId(InteractionType.Secondary));
            clickDetected = true;
        }

        // Test Interaction Primaire (Clic Gauche) - Juste pour voir si ça marche
        if (interactions.getInteractionId(InteractionType.Primary) != null) {
            System.out.println("[ELDANIOR] Clic Gauche (Primary) détecté ! ID: " + interactions.getInteractionId(InteractionType.Primary));
            clickDetected = true;
        }

        // Si on a détecté un clic, on tente l'activation
        if (clickDetected) {
            handleActivation(entityRef, store, commandBuffer);
        }
    }

    private void handleActivation(Ref<EntityStore> playerRef, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer) {
        Player player = store.getComponent(playerRef, Player.getComponentType());
        if (player == null) return;

        ItemStack item = player.getInventory().getItemInHand();
        if (item == null || item.isEmpty()) {
            System.out.println("[ELDANIOR] Clic détecté mais main vide.");
            return;
        }

        System.out.println("[ELDANIOR] Item en main: " + item.getItemId());

        String skillId = item.getFromMetadataOrNull(EldaniorSystem.getSkillIdKey());

        if (skillId != null) {
            System.out.println("[ELDANIOR] Skill ID trouvé: " + skillId + ". ACTIVATION !");

            ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
            PlayerLevelData data = store.getComponent(playerRef, type);

            if (data != null) {
                boolean active = data.toggleSkill(skillId);
                commandBuffer.putComponent(playerRef, type, data);
                String msg = active ? "§aACTIVÉ" : "§cDÉSACTIVÉ";
                player.sendMessage(com.hypixel.hytale.server.core.Message.raw("§6[TEST] " + skillId + " : " + msg));
            }
        } else {
            System.out.println("[ELDANIOR] Item sans SkillId.");
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(Player.getComponentType(), Interactions.getComponentType());
    }
}