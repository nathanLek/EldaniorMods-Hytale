package com.eldanior.system.territory.teleport;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.territory.ParcelData;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.DelayedEntitySystem;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import javax.annotation.Nonnull;

/** Traversee du Portail Continental : un voyageur qui a paye et passe dans l'ouverture est teleporte (4 fois par seconde). */
public class GateSystem extends DelayedEntitySystem<EntityStore> {

    public GateSystem() { super(0.25f); }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> cb) {
        try {
            Ref<EntityStore> ref = chunk.getReferenceTo(index);
            PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
            TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
            World world = store.getExternalData().getWorld();
            if (pr == null || t == null || world == null) return;
            ParcelData to = ContinentalGates.destination(pr.getUuid());
            if (to == null) return;
            Vector3d dest = ContinentalGates.crossing(pr.getUuid(), world.getName(), new Vector3d(t.getPosition()));
            if (dest == null) return;
            cb.putComponent(ref, Teleport.getComponentType(), Teleport.createForPlayer(dest, new Rotation3f(t.getRotation())));
            pr.sendMessage(Message.raw("Vous traversez le Portail Continental : bienvenue à " + to.getName() + ".").color("#7fd4ff"));
        } catch (Exception e) {
            EldaniorLogger.error("[Portail] traversee", e);
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() { return Player.getComponentType(); }
}
