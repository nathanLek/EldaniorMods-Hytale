package com.eldanior.system.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.components.PlayerLevelData;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
// Import essentiel trouvé grâce à toi :
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EnduranceSystem extends EntityTickingSystem<EntityStore> {

    private final Map<UUID, Float> previousStaminaValues = new ConcurrentHashMap<>();

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
        if (!entityRef.isValid()) return;

        // 1. Récupération UUID (Sécurité)
        UUIDComponent uuidComp = store.getComponent(entityRef, UUIDComponent.getComponentType());
        if (uuidComp == null) return;
        UUID uuid = uuidComp.getUuid();

        // 2. Récupération Données Joueur
        PlayerLevelData data = store.getComponent(entityRef, EldaniorSystem.get().getPlayerLevelDataType());
        if (data == null) return;

        // 3. Récupération Stats (Stamina)
        EntityStatMap statMap = store.getComponent(entityRef, EntityStatsModule.get().getEntityStatMapComponentType());
        if (statMap == null) return;

        int staminaIndex = DefaultEntityStatTypes.getStamina();
        EntityStatValue staminaValue = statMap.get(staminaIndex);
        if (staminaValue == null) return;

        float currentStamina = staminaValue.get();
        float maxStamina = staminaValue.getMax();

        // Initialisation si première fois
        if (!previousStaminaValues.containsKey(uuid)) {
            previousStaminaValues.put(uuid, currentStamina);
            return;
        }
        float previousStamina = previousStaminaValues.get(uuid);

        // --- 4. DÉTECTION SPRINT (Ta méthode robuste) ---
        boolean isSprinting = false;

        // On récupère le composant avec le Cast explicite (Ta correction)
        MovementStatesComponent moveComp = (MovementStatesComponent) store.getComponent(entityRef, MovementStatesComponent.getComponentType());

        if (moveComp != null && moveComp.getMovementStates() != null) {
            // On vérifie Sprint OU Run (Ta correction)
            isSprinting = moveComp.getMovementStates().sprinting || moveComp.getMovementStates().running;
        }

        // --- 5. LOGIQUE RPG ---

        if (isSprinting) {
            // A. Si on court : On met juste à jour la valeur précédente
            // (La consommation est gérée par le jeu, ou on peut la réduire ici si on veut)
            previousStaminaValues.put(uuid, currentStamina);
        } else {
            // B. Si on ne court pas : On gère la RÉGÉNÉRATION
            boolean isRegenerating = currentStamina > previousStamina;

            if (isRegenerating) {
                // Formule de Vitesse de Regen : +0.02% par point d'Endurance
                // Lvl 999 (3000 pts) = +60% vitesse de regen
                float speedMultiplier = 1.0f + (data.getEndurance() * 0.0002f);

                float naturalIncrease = currentStamina - previousStamina;
                float acceleratedIncrease = naturalIncrease * speedMultiplier;

                // On applique le boost
                float newStamina = Math.min(maxStamina, previousStamina + acceleratedIncrease);

                // On ne modifie que si le boost est significatif
                if (newStamina > currentStamina) {
                    statMap.setStatValue(staminaIndex, newStamina);
                    currentStamina = newStamina; // On met à jour pour la map
                }
            }
            previousStaminaValues.put(uuid, currentStamina);
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}