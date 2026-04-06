package com.eldanior.system.Leveling.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.hypixel.hytale.builtin.crafting.component.BenchBlock;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

// Alertes redondantes supprimées
public class CraftingRestrictionSystem extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {

    public CraftingRestrictionSystem() {
        super(UseBlockEvent.Pre.class);
    }

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer, @NonNullDecl UseBlockEvent.Pre event) {

        Ref<EntityStore> playerEntityRef = event.getContext().getEntity();
        Player player = store.getComponent(playerEntityRef, Player.getComponentType());
        PlayerRef netRef = store.getComponent(playerEntityRef, PlayerRef.getComponentType());

        if (player == null || netRef == null) return;

        // On s'assure que c'est bien une interaction de type "Use" (clic droit)
        if (!event.getInteractionType().toString().equals("Use")) return;

        // 1. Récupération des coordonnées du bloc cliqué
        Vector3i target = event.getTargetBlock();

        // ✅ 2. FIX UPDATE 4 : On récupère le composant BenchBlock au lieu du BlockState mort
        assert player.getWorld() != null;
        BenchBlock benchComponent = BlockModule.getComponent(
                BenchBlock.getComponentType(),
                player.getWorld(), target.getX(), target.getY(), target.getZ()
        );

        // 3. Vérification si c'est un outil d'artisanat
        if (benchComponent != null) {

            // NOTE: L'Update 4 ne permet plus de faire des .contains() sur le nom du bloc ici.
            // Si tu as besoin d'exclure 'bench_builders', il faudra utiliser une méthode de 'benchComponent'
            // que ton IDE te proposera (ex: benchComponent.getType() ou .getAssetId()).

            // 4. On récupère les données du joueur
            PlayerLevelData playerData = store.getComponent(playerEntityRef, EldaniorSystem.get().getPlayerLevelDataType());
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
                NotificationHelper.sendNotification(netRef,
                        "<color:red>Vous devez apprendre l'Artisanat pour utiliser ceci !</color>",
                        NotificationStyle.Warning);
            }
        }
    }

    @Override
    public Query<EntityStore> getQuery() {
        // ✅ Remis exactement comme tu le souhaitais
        return Archetype.empty();
    }
}