package com.eldanior.system.config.configs.system;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;

import javax.annotation.Nonnull;
import java.util.Objects;

public class MobDeathCheckSystem extends EntityTickingSystem<EntityStore> {

    private static float globalCheckTimer = 0;
    private static boolean shouldCheck = false;

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
                     @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        if (index == 0) {
            globalCheckTimer += dt;
            if (globalCheckTimer >= 0.5f) {
                shouldCheck = true;
                globalCheckTimer = 0;
            } else {
                shouldCheck = false;
            }
        }

        if (!shouldCheck) return;

        Ref<EntityStore> mobRef = chunk.getReferenceTo(index);
        if (!mobRef.isValid()) return;

        NPCEntity npc = store.getComponent(mobRef, Objects.requireNonNull(NPCEntity.getComponentType()));
        if (npc == null) return;

        EntityStatMap statMap = store.getComponent(mobRef,
                EntityStatsModule.get().getEntityStatMapComponentType());

        if (statMap == null) return;

        int healthIndex = DefaultEntityStatTypes.getHealth();
        if (statMap.get(healthIndex) == null) return;

        float currentHP = Objects.requireNonNull(statMap.get(healthIndex)).get();
        float maxHPFromGetMax = Objects.requireNonNull(statMap.get(healthIndex)).getMax();

        // CALCUL MANUEL DU MAX À PARTIR DES MODIFIERS
        float calculatedMax = 100.0f; // Base HP des ours

        if (statMap.get(healthIndex).getModifiers() != null) {
            for (var entry : statMap.get(healthIndex).getModifiers().entrySet()) {
                Modifier mod = entry.getValue();

                if (mod.getTarget() == Modifier.ModifierTarget.MAX) {
                    // Cast en StaticModifier pour accéder à l'amount
                    if (mod instanceof StaticModifier staticMod) {
                        calculatedMax += staticMod.getAmount();
                    }
                }
            }
        }

        if (currentHP <= 0) {
            System.out.println("💀 [DeathCheck] Mob invulnérable détecté ! Type: " + npc.getNPCTypeId() + " HP: " + currentHP + "/" + maxHPFromGetMax + " - KILL forcé");
            commandBuffer.removeEntity(mobRef, RemoveReason.REMOVE);
            return;
        }

        if (currentHP > maxHPFromGetMax) {
            System.out.println("⚠️ [DeathCheck] HP corrompus : " + npc.getNPCTypeId() + " " + currentHP + "/" + maxHPFromGetMax + " - Correction");
            statMap.setStatValue(healthIndex, maxHPFromGetMax);
            commandBuffer.putComponent(mobRef,
                    EntityStatsModule.get().getEntityStatMapComponentType(),
                    statMap);
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Objects.requireNonNull(NPCEntity.getComponentType());
    }
}