package com.eldanior.system.skills.system;

import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.MovementSettings;
import com.hypixel.hytale.protocol.SavedMovementStates;
import com.hypixel.hytale.protocol.packets.player.SetMovementStates;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Active le vol pour les joueurs morphés en créature volante.
 * - Détecte les effets de morph actifs via {@link EffectsManager}
 * - Active {@code canFly} si l'effet appartient à la whitelist
 * - Désactive {@code canFly} (si NOUS l'avions activé) dès que le morph expire
 * - Ne touche jamais à {@code canFly} activé par un autre système (créatif, FlySystem, etc.)
 */
public class MorphFlightSystem extends EntityTickingSystem<EntityStore> {

    private static final float MORPH_FLY_SPEED = 6.5f;

    /** Whitelist des potions de morph qui donnent le vol. */
    private static final Set<String> FLYING_MORPH_EFFECT_IDS = Set.of(
            "Potion_Morph_Pigeon",
            "Morph_Dragon"
            // Ajouter ici d'autres morphs volants quand les potions seront créées :
            // "Potion_Morph_Hawk", "Potion_Morph_Owl", "Potion_Morph_Bat", ...
    );

    /** Joueurs dont le vol a été activé PAR CE système (pour ne pas écraser un vol externe). */
    private final Set<UUID> morphFlyEnabled = ConcurrentHashMap.newKeySet();

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                     @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        Player player = archetypeChunk.getComponent(index, Player.getComponentType());
        PlayerRef playerRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType());

        if (player == null || playerRef == null) return;
        if (player.getReference() == null || !player.getReference().isValid()) return;

        UUID playerUUID = playerRef.getUuid();

        // 1. Détection : un morph volant est-il actif sur le joueur ?
        boolean isFlyingMorphActive = false;
        for (String effectId : FLYING_MORPH_EFFECT_IDS) {
            if (EffectsManager.hasEffect(player.getReference(), effectId, store)) {
                isFlyingMorphActive = true;
                break;
            }
        }

        MovementManager movementManager = store.getComponent(player.getReference(), MovementManager.getComponentType());
        if (movementManager == null) return;

        MovementSettings settings = movementManager.getSettings();
        if (settings == null) return;

        boolean currentlyCanFly = settings.canFly;

        // 2. Activation : morph volant détecté et vol pas encore actif → on l'active
        if (isFlyingMorphActive && !currentlyCanFly) {
            settings.canFly = true;
            settings.horizontalFlySpeed = MORPH_FLY_SPEED;
            settings.verticalFlySpeed = MORPH_FLY_SPEED;
            movementManager.update(playerRef.getPacketHandler());
            commandBuffer.putComponent(player.getReference(), MovementManager.getComponentType(), movementManager);

            SavedMovementStates saved = new SavedMovementStates(false);
            playerRef.getPacketHandler().writeNoCache(new SetMovementStates(saved));

            morphFlyEnabled.add(playerUUID);
        }
        // 3. Désactivation : plus de morph volant ET on l'avait activé → on le désactive
        else if (!isFlyingMorphActive && currentlyCanFly && morphFlyEnabled.contains(playerUUID)) {
            settings.canFly = false;
            movementManager.update(playerRef.getPacketHandler());
            commandBuffer.putComponent(player.getReference(), MovementManager.getComponentType(), movementManager);

            SavedMovementStates saved = new SavedMovementStates(false);
            playerRef.getPacketHandler().writeNoCache(new SetMovementStates(saved));

            morphFlyEnabled.remove(playerUUID);
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(
                Player.getComponentType(),
                PlayerRef.getComponentType()
        );
    }
}
