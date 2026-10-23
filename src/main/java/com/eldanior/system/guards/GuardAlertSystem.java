package com.eldanior.system.guards;

import com.eldanior.system.duel.DuelManager;
import com.eldanior.system.territory.ArenaManager;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * PvP pres des gardes : un joueur qui frappe un autre joueur (ou un PNJ de poste : garde, villageois...) devient recherche, et les gardes proches
 * l'attaquent. Tourne apres les filtres (groupe Inspect) : un coup annule (zone sans PvP, duel...) ne compte pas.
 * Legitime defense : frapper un joueur deja recherche ne rend pas recherche. Duels et arenes sont exclus.
 * Les gardes sont indestructibles (NpcData, MobDamageReductionSystem met leurs degats recus a 0) : frapper un garde compte
 * quand meme.
 */
public class GuardAlertSystem extends DamageEventSystem {

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store,
                       @Nonnull CommandBuffer<EntityStore> cb, @Nonnull Damage damage) {
        if (damage.isCancelled()) return;                                                // gardes indestructibles : leurs coups recus valent 0
        if (!(damage.getSource() instanceof Damage.EntitySource source)) return;
        Ref<EntityStore> attacker = source.getRef(), victim = chunk.getReferenceTo(index);
        if (attacker == null || !attacker.isValid() || victim == null || !victim.isValid() || attacker.equals(victim)) return;
        boolean attackerIsPlayer = store.getComponent(attacker, Player.getComponentType()) != null;
        boolean victimIsPlayer = store.getComponent(victim, Player.getComponentType()) != null;
        // Aventurier recrute : il s'en prend a ce que son joueur frappe et a ceux qui frappent son joueur
        if (attackerIsPlayer) AdventurerSystem.onOwnerFight(attacker, victim, store);
        if (victimIsPlayer) AdventurerSystem.onOwnerFight(victim, attacker, store);
        if (!attackerIsPlayer) return;
        UUID a = uuid(attacker, store);
        if (a == null || DuelManager.isInDuel(a) || ArenaManager.isInArena(a)) return;

        if (victimIsPlayer) {
            UUID v = uuid(victim, store);
            if (v == null || damage.getAmount() <= 0 || GuardManager.isWanted(v)) return;   // legitime defense
            GuardManager.markWanted(a, attacker, store);
        } else if (GuardManager.isProtected(victim, store)) {                           // garde, villageois, aventurier libre...
            GuardManager.markWanted(a, attacker, store);
        }
    }

    private static UUID uuid(Ref<EntityStore> ref, Store<EntityStore> store) {
        UUIDComponent u = store.getComponent(ref, UUIDComponent.getComponentType());
        return u != null ? u.getUuid() : null;
    }

    private static SystemGroup<EntityStore> inspectGroup = null;

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public SystemGroup<EntityStore> getGroup() {
        if (inspectGroup != null) return inspectGroup;
        try {
            Class<?> mod = Class.forName("com.hypixel.hytale.server.core.modules.entity.damage.DamageModule");
            Object inst = mod.getMethod("get").invoke(null);
            inspectGroup = (SystemGroup<EntityStore>) mod.getMethod("getInspectDamageGroup").invoke(inst);
            return inspectGroup;
        } catch (Throwable e) {
            return null;
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }
}
