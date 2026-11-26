package com.eldanior.system.dojo;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

/** Coups portes aux mannequins d'entrainement (voir Mannequins). */
public final class MannequinDamage {

    private MannequinDamage() {}

    /** Filtre : vie remise au plein avant le coup ; le mannequin ne peut pas mourir. */
    public static final class Filter extends DamageEventSystem {
        @Override
        public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store,
                           @Nonnull CommandBuffer<EntityStore> cb, @Nonnull Damage damage) {
            if (damage.isCancelled()) return;
            Ref<EntityStore> target = chunk.getReferenceTo(index);
            if (!Mannequins.isDummy(target, store)) return;
            try {
                Mannequins.beforeHit(target, damage, store);
            } catch (Exception e) {
                EldaniorLogger.error("[Mannequin] filtre", e);
            }
        }

        @Override
        public SystemGroup<EntityStore> getGroup() { return DamageModule.get().getFilterDamageGroup(); }

        @Nonnull
        @Override
        public Query<EntityStore> getQuery() { return NPCEntity.getComponentType(); }
    }

    /** Apres les filtres : degats affiches sur la plaque, energie de signature rendue au joueur qui frappe. */
    public static final class Inspect extends DamageEventSystem {
        @Override
        public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store,
                           @Nonnull CommandBuffer<EntityStore> cb, @Nonnull Damage damage) {
            if (damage.isCancelled() || damage.getAmount() <= 0) return;
            Ref<EntityStore> target = chunk.getReferenceTo(index);
            if (!Mannequins.isDummy(target, store)) return;
            try {
                UUIDComponent u = store.getComponent(target, UUIDComponent.getComponentType());
                String plate = u != null ? Mannequins.recordHit(u.getUuid(), damage.getAmount()) : null;
                if (plate != null) cb.putComponent(target, Nameplate.getComponentType(), new Nameplate(plate));
                Ref<EntityStore> attacker = damage.getSource() instanceof Damage.EntitySource es ? es.getRef() : null;
                if (attacker != null && attacker.isValid() && store.getComponent(attacker, Player.getComponentType()) != null)
                    signature(attacker, store);
            } catch (Exception e) {
                EldaniorLogger.error("[Mannequin] coup", e);
            }
        }

        /** ENERGY_PER_HIT de la jauge de signature de l'arme tenue (au moins 1 point). Lu a l'appel : jamais en static (StatConfig). */
        private static void signature(Ref<EntityStore> player, Store<EntityStore> store) {
            EntityStatMap stats = store.getComponent(player, EntityStatsModule.get().getEntityStatMapComponentType());
            int id = DefaultEntityStatTypes.getSignatureEnergy();
            EntityStatValue v = stats != null ? stats.get(id) : null;
            if (v == null || v.getMax() <= 0 || v.get() >= v.getMax()) return;
            stats.addStatValue(id, Math.max(1f, v.getMax() * Mannequins.ENERGY_PER_HIT));
        }

        @Override
        public SystemGroup<EntityStore> getGroup() { return DamageModule.get().getInspectDamageGroup(); }

        @Nonnull
        @Override
        public Query<EntityStore> getQuery() { return NPCEntity.getComponentType(); }
    }

    /**
     * Doublons : une entite NonSerialized peut revenir au reveil de son chunk alors qu'une autre l'a remplacee, et un mannequin
     * peut survivre a son socle (rechargement du mod). Tout mannequin qui n'est pas celui d'un socle connu est retire.
     */
    public static final class Entities extends RefSystem<EntityStore> {
        @Override
        public void onEntityAdded(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl AddReason reason, @NonNullDecl Store<EntityStore> store,
                                  @NonNullDecl CommandBuffer<EntityStore> cb) {
            if (Mannequins.spawning) return;                                                // le notre, en cours d'apparition
            NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
            if (npc == null || !Mannequins.ID.equals(npc.getRoleName())) return;
            UUIDComponent u = store.getComponent(ref, UUIDComponent.getComponentType());
            if (u == null || !Mannequins.isCurrent(u.getUuid())) cb.tryRemoveEntity(ref, RemoveReason.REMOVE);
        }

        @Override
        public void onEntityRemove(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl RemoveReason reason, @NonNullDecl Store<EntityStore> store,
                                   @NonNullDecl CommandBuffer<EntityStore> cb) { }

        @Nonnull
        @Override
        public Query<EntityStore> getQuery() { return NPCEntity.getComponentType(); }
    }
}
