package com.eldanior.system.Leveling.systems;

import com.eldanior.system.EldaniorSystem;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

public class CombatTrackerSystem extends DamageEventSystem {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer, Damage damage) {

        Damage.Source source = damage.getSource();
        if (!(source instanceof Damage.EntitySource entitySource)) {
            return;
        }

        Ref<EntityStore> attackerRef = entitySource.getRef();

        if (!attackerRef.isValid()) {
            return;
        }
        Player attacker = (Player) store.getComponent(attackerRef, Player.getComponentType());
        if (attacker == null) {
            return;
        }
        UUIDComponent attackerUUIDComp = (UUIDComponent) store.getComponent(attackerRef, UUIDComponent.getComponentType());
        if (attackerUUIDComp == null) return;
        Ref<EntityStore> victimRef = archetypeChunk.getReferenceTo(index);
        if (!victimRef.isValid()) return;
        UUIDComponent victimUUIDComp = (UUIDComponent) store.getComponent(victimRef, UUIDComponent.getComponentType());
        if (victimUUIDComp == null) return;
        EldaniorSystem.get().getLastAttackers().put(victimUUIDComp.getUuid(), attackerUUIDComp.getUuid());
        LOGGER.atInfo().log(">>> COUP VALIDÉ ! Joueur (" + attackerUUIDComp.getUuid() + ") a tapé Victime (" + victimUUIDComp.getUuid() + ")");
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }
}