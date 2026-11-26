package com.eldanior.system.territory.pancarte;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.util.WorldBlockUtil;
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
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.joml.Vector3i;

import java.util.UUID;

/**
 * Pancarte et affiche de propriete (Pancartes) : la poser ouvre le choix du bien a presenter ; F dessus ouvre la fenetre du bien
 * (ou le choix du bien si elle n'est liee a rien) ; une pancarte liee ne se casse que par un admin ou le proprietaire du bien.
 */
public final class PancarteEvents {

    private PancarteEvents() {}

    static void open(Ref<EntityStore> ref, Store<EntityStore> store, Player player, PlayerRef pr, String key) {
        try {
            ParcelData p = Pancartes.parcelOf(key);
            if (p != null) {
                player.getPageManager().openCustomPage(ref, store, new PancartePage(pr, key));
            } else if (!Pancartes.candidates(key, pr).isEmpty()) {
                player.getPageManager().openCustomPage(ref, store, new PancarteLienPage(pr, key));
            } else {
                pr.sendMessage(Message.raw(Pancartes.admin(pr)
                        ? "Aucun bien à moins de " + Pancartes.LINK_RANGE + " blocs : créez d'abord la zone du bien."
                        : "Cette pancarte ne présente aucun bien."));
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Pancarte] fenetre", e);
        }
    }

    public static class Place extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
        public Place() { super(PlaceBlockEvent.class); }

        @Override
        public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl PlaceBlockEvent event) {
            ItemStack item = event.getItemInHand();
            if (item == null || !Pancartes.isItem(item.getItemId())) return;
            Player player = chunk.getComponent(index, Player.getComponentType());
            Ref<EntityStore> ref = chunk.getReferenceTo(index);
            PlayerRef pr = ref != null ? store.getComponent(ref, PlayerRef.getComponentType()) : null;
            World world = player != null ? player.getWorld() : null;
            if (player == null || world == null || pr == null) return;
            Vector3i b = event.getTargetBlock();
            String key = Pancartes.key(world.getName(), b.x(), b.y(), b.z());
            if (Pancartes.candidates(key, pr).isEmpty()) {
                event.setCancelled(true);
                pr.sendMessage(Message.raw(Pancartes.admin(pr)
                        ? "Aucun bien à moins de " + Pancartes.LINK_RANGE + " blocs : créez d'abord la zone du bien."
                        : "Posez la pancarte à moins de " + Pancartes.LINK_RANGE + " blocs d'un bien dont vous êtes propriétaire."));
                return;
            }
            world.execute(() -> {                                              // pose refusee par une protection : rien a ouvrir
                if (!ref.isValid() || !Pancartes.isPancarte(WorldBlockUtil.getBlockType(world, b.x(), b.y(), b.z()))) return;
                player.getPageManager().openCustomPage(ref, store, new PancarteLienPage(pr, key));
            });
        }

        @Override
        public Query<EntityStore> getQuery() { return Player.getComponentType(); }
    }

    public static class Use extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {
        public Use() { super(UseBlockEvent.Pre.class); }

        @Override
        public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl UseBlockEvent.Pre event) {
            if (event.getInteractionType() != InteractionType.Use || !Pancartes.isPancarte(event.getBlockType())) return;
            event.setCancelled(true);
            Ref<EntityStore> ref = event.getContext().getEntity();
            Player player = store.getComponent(ref, Player.getComponentType());
            PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
            World world = player != null ? player.getWorld() : null;
            if (world == null || pr == null) return;
            Vector3i b = event.getTargetBlock();
            String key = Pancartes.key(world.getName(), b.x(), b.y(), b.z());
            world.execute(() -> { if (ref.isValid()) open(ref, store, player, pr, key); });
        }

        @Override
        public Query<EntityStore> getQuery() { return Archetype.empty(); }
    }

    public static class Break extends EntityEventSystem<EntityStore, BreakBlockEvent> {
        public Break() { super(BreakBlockEvent.class); }

        @Override
        public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl BreakBlockEvent event) {
            if (!Pancartes.isPancarte(event.getBlockType())) return;
            Player player = chunk.getComponent(index, Player.getComponentType());
            Ref<EntityStore> ref = chunk.getReferenceTo(index);
            PlayerRef pr = ref != null ? store.getComponent(ref, PlayerRef.getComponentType()) : null;
            World world = player != null ? player.getWorld() : null;
            if (world == null || pr == null) return;
            Vector3i b = event.getTargetBlock();
            String key = Pancartes.key(world.getName(), b.x(), b.y(), b.z());
            ParcelData p = Pancartes.parcelOf(key);
            if (p == null) return;                                             // pancarte libre : bloc ordinaire
            UUID u = Pancartes.uuid(pr);
            if (!Pancartes.admin(pr) && (u == null || !p.isOwner(u))) {
                event.setCancelled(true);
                pr.sendMessage(Message.raw("Cette pancarte présente « " + p.getName() + " » : seul son propriétaire peut la retirer."));
                return;
            }
            Pancartes.unlink(key);
            pr.sendMessage(Message.raw("Pancarte de « " + p.getName() + " » retirée."));
        }

        @Override
        public Query<EntityStore> getQuery() { return Archetype.empty(); }
    }
}
