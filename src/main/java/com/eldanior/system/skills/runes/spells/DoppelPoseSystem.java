package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.systems.MovementStatesSystem;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * Regard du Doppelganger (vers ce que vise le curseur du lanceur), applique APRES l'IA des PNJ
 * (RoleSystems.BehaviourTickSystem) et le calcul de leurs animations (MovementStatesSystem) : sinon l'IA le retourne dans
 * le sens de sa marche. Le deplacement lui-meme reste celui du moteur (point de laisse), avec ses vraies animations.
 * Applique aussi les regards imposes aux PNJ de poste (util/NpcFacing : villageois qui parlent, regardent un etabli...).
 */
public class DoppelPoseSystem extends EntityTickingSystem<EntityStore> {

    private final Set<Dependency<EntityStore>> dependencies = new HashSet<>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public DoppelPoseSystem() {
        dependencies.add(new SystemDependency<>(Order.AFTER, MovementStatesSystem.class));
        try {
            Class<?> behaviour = Class.forName("com.hypixel.hytale.server.npc.systems.RoleSystems$BehaviourTickSystem");
            dependencies.add(new SystemDependency(Order.AFTER, (Class<? extends ISystem<EntityStore>>) behaviour));
        } catch (Throwable e) {
            EldaniorLogger.warn("[Doppelganger] systeme d'IA des PNJ introuvable : pose appliquee apres les animations seulement");
        }
    }

    @Override
    public Set<Dependency<EntityStore>> getDependencies() { return dependencies; }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> ref = chunk.getReferenceTo(index);
        Doppelganger.Pose pose = Doppelganger.poseOf(ref);
        if (pose == null) {
            com.eldanior.system.util.NpcFacing.Look look = com.eldanior.system.util.NpcFacing.get(ref);   // PNJ de poste qui regarde quelque chose
            if (look != null) CloneFx.faceTowards(ref, look.point(), store);
            return;
        }
        if (pose.pos() != null) CloneFx.pose(ref, pose.pos(), pose.body(), pose.head(), pose.states(), store);
        else CloneFx.face(ref, pose.body(), pose.head(), store);                // regard seul : le moteur gere marche et animations
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return NPCEntity.getComponentType();
    }
}
