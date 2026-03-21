package com.eldanior.system.Leveling.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

@SuppressWarnings({"deprecation", "removal", "unchecked", "ConstantConditions"})
public class CraftingRestrictionSystem extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {

    public CraftingRestrictionSystem() {
        super(UseBlockEvent.Pre.class);
    }

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer, @NonNullDecl UseBlockEvent.Pre event) {

        Ref<EntityStore> playerRef = event.getContext().getEntity();
        Player player = store.getComponent(playerRef, Player.getComponentType());
        if (player == null) return;

        // On s'assure que c'est bien une interaction de type "Use" (clic droit)
        if (!event.getInteractionType().toString().equals("Use")) return;

        // 1. Récupération des coordonnées du bloc cliqué
        Vector3i target = event.getTargetBlock();
        com.hypixel.hytale.server.core.universe.world.meta.BlockState blockState = player.getWorld().getState(target.getX(), target.getY(), target.getZ(), true);

        // 2. Vérification infaillible du nom du bloc via toString()
        if (blockState != null) {
            String blockInfo = blockState.toString().toLowerCase();

            // 3. Vérification si c'est un outil d'artisanat
            if ((blockInfo.contains("workbench") || blockInfo.contains("forge") || blockInfo.contains("anvil") || blockInfo.contains("crafting"))
                    && !blockInfo.contains("bench_builders") && !blockInfo.contains("bench_furniture")) {
                // 4. On récupère les données du joueur
                PlayerLevelData playerData = store.getComponent(playerRef, EldaniorSystem.get().getPlayerLevelDataType());
                boolean hasArtisanat = false;

                // 5. On vérifie s'il possède la compétence Artisanat
                if (playerData != null && playerData.getActivePassives() != null) {
                    for (PassiveSkill skill : playerData.getActivePassives()) {
                        if (skill == PassiveSkill.ARTISANAT) {
                            hasArtisanat = true;
                            break;
                        }
                    }
                }

                // 6. S'il n'a pas la compétence, on bloque tout !
                if (!hasArtisanat) {

                    // On annule l'événement (empêche l'ouverture de l'interface)
                    event.setCancelled(true);

                    // On envoie la notification
                    NotificationHelper.sendNotification(player.getPlayerRef(),
                            "<color:red>Vous devez apprendre l'Artisanat pour utiliser ceci !</color>",
                            NotificationStyle.Warning);
                }
            }
        }
    }

    // NOUVEAU : Méthode obligatoire pour EntityEventSystem
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}