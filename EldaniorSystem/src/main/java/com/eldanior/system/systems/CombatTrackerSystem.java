package com.eldanior.system.systems;

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

import javax.annotation.Nonnull;

public class CombatTrackerSystem extends DamageEventSystem {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public void handle(int index, ArchetypeChunk<EntityStore> archetypeChunk, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer, Damage damage) {

        // 1. Vérifier la source (Est-ce une entité ?)
        Damage.Source source = damage.getSource();
        if (!(source instanceof Damage.EntitySource)) {
            return; // Ce n'est pas une entité qui tape (Chute, Feu, etc.)
        }

        Damage.EntitySource entitySource = (Damage.EntitySource) source;
        Ref<EntityStore> attackerRef = entitySource.getRef();

        if (attackerRef == null || !attackerRef.isValid()) {
            return;
        }

        // 2. Vérifier si l'attaquant est un Joueur
        // Si cette ligne renvoie null, c'est que le jeu ne considère pas l'attaquant comme un "Player"
        Player attacker = (Player) store.getComponent(attackerRef, Player.getComponentType());
        if (attacker == null) {
            return; // C'est un monstre qui tape un autre monstre
        }

        // 3. Récupérer l'UUID de l'attaquant
        UUIDComponent attackerUUIDComp = (UUIDComponent) store.getComponent(attackerRef, UUIDComponent.getComponentType());
        if (attackerUUIDComp == null) return;

        // 4. Récupérer l'UUID de la victime
        Ref<EntityStore> victimRef = archetypeChunk.getReferenceTo(index);
        if (victimRef == null || !victimRef.isValid()) return;

        UUIDComponent victimUUIDComp = (UUIDComponent) store.getComponent(victimRef, UUIDComponent.getComponentType());
        if (victimUUIDComp == null) return;

        // 5. ENREGISTREMENT ET LOG
        EldaniorSystem.get().getLastAttackers().put(victimUUIDComp.getUuid(), attackerUUIDComp.getUuid());

        // --- LE MESSAGE IMPORTANT À CHERCHER DANS LA CONSOLE ---
        LOGGER.atInfo().log(">>> COUP VALIDÉ ! Joueur (" + attackerUUIDComp.getUuid() + ") a tapé Victime (" + victimUUIDComp.getUuid() + ")");
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }
}