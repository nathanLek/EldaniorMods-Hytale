package com.eldanior.system.boards;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import javax.annotation.Nonnull;

/**
 * Monstres a embleme orphelins (2026-11-16) : un monstre qui porte un embleme (etoiles, tete de mort : attachement
 * NPC/Eldanior/Emblemes/) mais qui n'est plus le monstre d'un contrat en cours ni un boss de donjon est retire (zone supprimee
 * pendant qu'il etait hors des chunks charges, ancienne version qui ne le retirait pas...). Les monstres morts sont laisses au
 * moteur (butin). Toutes les 5 s.
 */
public class EmblemOrphanSystem extends EntityTickingSystem<EntityStore> {

    private static final String EMBLEMS = "NPC/Eldanior/Emblemes/";
    private float timer;
    private boolean check;

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> cb) {
        if (index == 0) {
            timer += dt;
            check = timer >= 5f;
            if (check) timer = 0;
        }
        if (!check) return;
        Ref<EntityStore> ref = chunk.getReferenceTo(index);
        if (ref == null || !ref.isValid()) return;
        ModelComponent mc = store.getComponent(ref, ModelComponent.getComponentType());
        if (mc == null || mc.getModel() == null || mc.getModel().getAttachments() == null) return;
        boolean emblem = false;
        for (ModelAttachment a : mc.getModel().getAttachments())
            if (a != null && a.getModel() != null && a.getModel().startsWith(EMBLEMS)) { emblem = true; break; }
        if (!emblem) return;
        UUIDComponent u = store.getComponent(ref, UUIDComponent.getComponentType());
        if (u == null || MonsterContracts.isContractMob(u.getUuid())) return;            // contrat en cours ou boss de donjon
        var world = store.getExternalData().getWorld();                                   // failles-donjons : leurs boss et etoiles
        if (world != null && com.eldanior.system.failles.FailleManager.isRiftWorld(world.getName())) return;
        EntityStatMap stats = store.getComponent(ref, EntityStatsModule.get().getEntityStatMapComponentType());
        var hp = stats != null ? stats.get(DefaultEntityStatTypes.getHealth()) : null;
        if (hp != null && hp.get() <= 0) return;                                          // mort : butin laisse au moteur
        NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
        EldaniorLogger.info("[Boards] monstre a embleme orphelin retire : " + (npc != null ? npc.getNPCTypeId() : "?") + " " + u.getUuid());
        cb.removeEntity(ref, RemoveReason.REMOVE);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return NPCEntity.getComponentType();
    }
}
