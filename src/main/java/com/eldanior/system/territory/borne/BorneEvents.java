package com.eldanior.system.territory.borne;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.territory.ParcelData;
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

/**
 * Bloc de borne de territoire (Bornes) : le poser (admins) ouvre sa fenetre ; F dessus la rouvre ; le casser (admins) supprime
 * sa zone (regle validee : la borne doit rester posee pour que la zone existe). Les non-admins ne posent ni ne cassent de borne.
 */
public final class BorneEvents {

    private BorneEvents() {}

    private static boolean admin(PlayerRef pr) {
        return pr != null && pr.hasPermission(EldaniorLogger.ADMIN_PERMISSION);
    }

    private static void open(Ref<EntityStore> ref, Store<EntityStore> store, Player player, PlayerRef pr, String key) {
        try {
            player.getPageManager().openCustomPage(ref, store, new BornePage(pr, key));
        } catch (Exception e) {
            EldaniorLogger.error("[Borne] fenetre", e);
        }
    }

    public static class Place extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
        public Place() { super(PlaceBlockEvent.class); }

        @Override
        public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl PlaceBlockEvent event) {
            ItemStack item = event.getItemInHand();
            if (item == null || !Bornes.ITEM.equals(item.getItemId())) return;
            Player player = chunk.getComponent(index, Player.getComponentType());
            Ref<EntityStore> ref = chunk.getReferenceTo(index);
            PlayerRef pr = ref != null ? store.getComponent(ref, PlayerRef.getComponentType()) : null;
            World world = player != null ? player.getWorld() : null;
            if (player == null || world == null || !admin(pr)) {
                event.setCancelled(true);
                if (pr != null) pr.sendMessage(Message.raw("Seuls les admins posent des bornes de territoire."));
                return;
            }
            Vector3i b = event.getTargetBlock();
            String key = Bornes.key(world.getName(), b.x(), b.y(), b.z());
            world.execute(() -> { if (ref.isValid()) open(ref, store, player, pr, key); });
        }

        @Override
        public Query<EntityStore> getQuery() { return Player.getComponentType(); }
    }

    public static class Use extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {
        public Use() { super(UseBlockEvent.Pre.class); }

        @Override
        public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl UseBlockEvent.Pre event) {
            if (event.getInteractionType() != InteractionType.Use || !Bornes.isBorne(event.getBlockType())) return;
            event.setCancelled(true);
            Ref<EntityStore> ref = event.getContext().getEntity();
            Player player = store.getComponent(ref, Player.getComponentType());
            PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
            World world = player != null ? player.getWorld() : null;
            if (world == null || !admin(pr)) return;                                  // bloc d'admin (la ville a sa propre borne)
            Vector3i b = event.getTargetBlock();
            String key = Bornes.key(world.getName(), b.x(), b.y(), b.z());
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
            if (!Bornes.isBorne(event.getBlockType())) return;
            Player player = chunk.getComponent(index, Player.getComponentType());
            Ref<EntityStore> ref = chunk.getReferenceTo(index);
            PlayerRef pr = ref != null ? store.getComponent(ref, PlayerRef.getComponentType()) : null;
            World world = player != null ? player.getWorld() : null;
            if (world == null || !admin(pr)) { event.setCancelled(true); return; }
            Vector3i b = event.getTargetBlock();
            String key = Bornes.key(world.getName(), b.x(), b.y(), b.z());
            ParcelData removed = Bornes.remove(key);
            pr.sendMessage(Message.raw(removed != null ? "Borne retirée : " + removed.getType().getLabel() + " « " + removed.getName()
                    + " » supprimé (ses sous-zones remontent à la zone parente)." : "Borne retirée (elle ne réglait aucune zone)."));
        }

        @Override
        public Query<EntityStore> getQuery() { return Archetype.empty(); }
    }
}
