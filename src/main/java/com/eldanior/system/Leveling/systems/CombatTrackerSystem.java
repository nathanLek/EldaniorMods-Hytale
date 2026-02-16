package com.eldanior.system.Leveling.systems;

import com.eldanior.system.EldaniorSystem;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.UUID;

public class CombatTrackerSystem extends DamageEventSystem {

    // On garde le logger mais on ne l'utilise qu'en cas de besoin critique
    // private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                       @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer,
                       @Nonnull Damage damage) {

        // 1. Vérifier la source : Est-ce une entité qui tape ?
        Damage.Source source = damage.getSource();
        if (!(source instanceof Damage.EntitySource entitySource)) {
            return;
        }

        Ref<EntityStore> attackerRef = entitySource.getRef();
        if (!attackerRef.isValid()) return;

        // 2. OPTIMISATION MAJEURE : Si l'attaquant n'est pas un JOUEUR, on s'arrête !
        // On ne track pas les combats Mob vs Mob pour économiser la RAM
        if (store.getComponent(attackerRef, Player.getComponentType()) == null) {
            return;
        }

        // 3. Récupération des UUIDs
        UUIDComponent attackerUUIDComp = store.getComponent(attackerRef, UUIDComponent.getComponentType());
        if (attackerUUIDComp == null) return;

        Ref<EntityStore> victimRef = archetypeChunk.getReferenceTo(index);
        if (!victimRef.isValid()) return;

        UUIDComponent victimUUIDComp = store.getComponent(victimRef, UUIDComponent.getComponentType());
        if (victimUUIDComp == null) return;

        // 4. Enregistrement du combat
        UUID attackerUUID = attackerUUIDComp.getUuid();
        UUID victimUUID = victimUUIDComp.getUuid();

        // On évite de s'enregistrer soi-même (dégâts auto-infligés)
        if (attackerUUID.equals(victimUUID)) return;

        // Mise à jour de la Map dans le système principal
        EldaniorSystem.get().getLastAttackers().put(victimUUID, attackerUUID);

        // LOGGER RETIRÉ : Pour la performance en production
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        // On surveille toutes les entités qui prennent des dégâts
        return Query.any();
    }
}