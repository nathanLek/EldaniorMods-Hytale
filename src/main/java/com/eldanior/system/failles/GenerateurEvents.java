package com.eldanior.system.failles;

import com.eldanior.system.config.EldaniorLogger;
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

/** Bloc generateur de monstres (admins) : le poser ou F dessus ouvre sa fenetre ; le casser l'efface. */
public final class GenerateurEvents {

    private GenerateurEvents() {}

    private static boolean admin(PlayerRef pr) { return pr != null && pr.hasPermission(EldaniorLogger.ADMIN_PERMISSION); }

    private static boolean isGen(String id) { return id != null && (id.equals(Generateurs.ITEM) || id.endsWith("*" + Generateurs.ITEM)); }

    private static void open(Ref<EntityStore> ref, Store<EntityStore> store, Player player, PlayerRef pr, World world, Vector3i b) {
        try { player.getPageManager().openCustomPage(ref, store, new GenerateurPage(pr, world.getName(), b)); }
        catch (Exception e) { EldaniorLogger.error("[Generateurs] fenetre", e); }
    }

    public static class Place extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
        public Place() { super(PlaceBlockEvent.class); }

        @Override
        public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl PlaceBlockEvent event) {
            ItemStack item = event.getItemInHand();
            if (item == null || !Generateurs.ITEM.equals(item.getItemId())) return;
            Player player = chunk.getComponent(index, Player.getComponentType());
            Ref<EntityStore> ref = chunk.getReferenceTo(index);
            PlayerRef pr = ref != null ? store.getComponent(ref, PlayerRef.getComponentType()) : null;
            World world = player != null ? player.getWorld() : null;
            if (player == null || world == null || !admin(pr)) {
                event.setCancelled(true);
                if (pr != null) pr.sendMessage(Message.raw("Seuls les admins posent des générateurs de monstres."));
                return;
            }
            Vector3i b = event.getTargetBlock();
            world.execute(() -> { if (ref.isValid()) open(ref, store, player, pr, world, b); });
        }

        @Override
        public Query<EntityStore> getQuery() { return Player.getComponentType(); }
    }

    public static class Use extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {
        public Use() { super(UseBlockEvent.Pre.class); }

        @Override
        public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl UseBlockEvent.Pre event) {
            if (event.getInteractionType() != InteractionType.Use || event.getBlockType() == null || !isGen(event.getBlockType().getId())) return;
            event.setCancelled(true);
            Ref<EntityStore> ref = event.getContext().getEntity();
            Player player = store.getComponent(ref, Player.getComponentType());
            PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
            World world = player != null ? player.getWorld() : null;
            if (world == null || !admin(pr)) return;
            Vector3i b = event.getTargetBlock();
            world.execute(() -> { if (ref.isValid()) open(ref, store, player, pr, world, b); });
        }

        @Override
        public Query<EntityStore> getQuery() { return Archetype.empty(); }
    }

    public static class Break extends EntityEventSystem<EntityStore, BreakBlockEvent> {
        public Break() { super(BreakBlockEvent.class); }

        @Override
        public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl BreakBlockEvent event) {
            if (event.getBlockType() == null || !isGen(event.getBlockType().getId())) return;
            Player player = chunk.getComponent(index, Player.getComponentType());
            Ref<EntityStore> ref = chunk.getReferenceTo(index);
            PlayerRef pr = ref != null ? store.getComponent(ref, PlayerRef.getComponentType()) : null;
            World world = player != null ? player.getWorld() : null;
            if (world == null || !admin(pr)) { event.setCancelled(true); return; }
            Vector3i b = event.getTargetBlock();
            Generateurs.set(world, b.x(), b.y(), b.z(), null);
        }

        @Override
        public Query<EntityStore> getQuery() { return Player.getComponentType(); }
    }
}
