package com.eldanior.system.anchors;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.joml.Vector3i;

/**
 * Touche d'interaction sur un Cercle d'Ancrage : son proprietaire le renomme ; un autre joueur lit a qui il appartient. Un cercle
 * sans ancre (pose par un outil d'admin, sauvegarde perdue) est repris par le premier joueur qui a encore une place d'ancre.
 */
public class AnchorUseEvent extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {

    public AnchorUseEvent() { super(UseBlockEvent.Pre.class); }

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl UseBlockEvent.Pre event) {
        if (event.getInteractionType() != InteractionType.Use || !AnchorManager.isCircle(event.getBlockType())) return;
        Ref<EntityStore> ref = event.getContext().getEntity();
        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        World world = player != null ? player.getWorld() : null;
        if (pr == null || world == null) return;
        Vector3i base = AnchorManager.base(world, event.getTargetBlock());
        world.execute(() -> {
            if (!ref.isValid()) return;
            AnchorManager.Anchor anchor = AnchorManager.at(world.getName(), base);
            if (anchor == null) {
                Store<EntityStore> s = ref.getStore();
                int max = AnchorManager.maxAnchors(ref, s);
                if (AnchorManager.countOf(pr.getUuid()) >= max) {
                    pr.sendMessage(Message.raw("Ce cercle n'est lié à personne, mais vous avez déjà toutes vos ancres (" + max + ").").color("#e8c060"));
                    return;
                }
                anchor = AnchorManager.placed(world, base, pr.getUuid(), pr.getUsername());
                player.getPageManager().openCustomPage(ref, s, new AnchorNamePage(pr, anchor, true));
                return;
            }
            if (!anchor.owner.equals(pr.getUuid())) {
                pr.sendMessage(Message.raw("Ancre « " + anchor.name + " » de " + anchor.ownerName + ".").color("#c8a8ff"));
                return;
            }
            anchor.ownerName = pr.getUsername();
            player.getPageManager().openCustomPage(ref, ref.getStore(), new AnchorNamePage(pr, anchor, false));
        });
    }

    @Override
    public Query<EntityStore> getQuery() { return Archetype.empty(); }
}
