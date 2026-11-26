package com.eldanior.system.guards;

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
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
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
 * Bloc de poste de PNJ (NpcPostBlocks) : le poser (admins seulement) l'enregistre et ouvre sa fenetre ; F dessus la rouvre ;
 * le casser retire le PNJ (les non-admins ne peuvent pas le casser).
 */
public final class NpcPostEvents {

    private NpcPostEvents() {}

    static boolean isPostBlock(BlockType type) {
        String id = type != null ? type.getId() : null;
        return id != null && (id.equals(NpcPostBlocks.ITEM) || id.endsWith("*" + NpcPostBlocks.ITEM) || id.startsWith(NpcPostBlocks.ITEM + "_State"));
    }

    private static boolean admin(PlayerRef pr) {
        return pr != null && pr.hasPermission(EldaniorLogger.ADMIN_PERMISSION);
    }

    private static void open(Ref<EntityStore> ref, Store<EntityStore> store, Player player, PlayerRef pr, String key) {
        try {
            player.getPageManager().openCustomPage(ref, store, new NpcPostPage(pr, key));
        } catch (Exception e) {
            EldaniorLogger.error("[NpcPost] fenetre", e);
        }
    }

    public static class Place extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
        public Place() { super(PlaceBlockEvent.class); }

        @Override
        public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl PlaceBlockEvent event) {
            ItemStack item = event.getItemInHand();
            if (item == null || !NpcPostBlocks.ITEM.equals(item.getItemId())) return;
            Player player = chunk.getComponent(index, Player.getComponentType());
            Ref<EntityStore> ref = chunk.getReferenceTo(index);
            PlayerRef pr = ref != null ? store.getComponent(ref, PlayerRef.getComponentType()) : null;
            World world = player != null ? player.getWorld() : null;
            if (player == null || world == null || !admin(pr)) {
                event.setCancelled(true);
                if (pr != null) pr.sendMessage(Message.raw("Seuls les admins posent des postes de PNJ."));
                return;
            }
            Vector3i b = event.getTargetBlock();
            String key = NpcPostBlocks.key(world.getName(), b.x(), b.y(), b.z());
            NpcPostBlocks.register(key);
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
            if (event.getInteractionType() != InteractionType.Use || !isPostBlock(event.getBlockType())) return;
            event.setCancelled(true);
            Ref<EntityStore> ref = event.getContext().getEntity();
            Player player = store.getComponent(ref, Player.getComponentType());
            PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
            World world = player != null ? player.getWorld() : null;
            if (world == null || !admin(pr)) return;
            Vector3i b = event.getTargetBlock();
            String key = NpcPostBlocks.key(world.getName(), b.x(), b.y(), b.z());
            NpcPostBlocks.register(key);                                                    // bloc pose avant le registre
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
            if (!isPostBlock(event.getBlockType())) return;
            Player player = chunk.getComponent(index, Player.getComponentType());
            Ref<EntityStore> ref = chunk.getReferenceTo(index);
            PlayerRef pr = ref != null ? store.getComponent(ref, PlayerRef.getComponentType()) : null;
            World world = player != null ? player.getWorld() : null;
            if (world == null || !admin(pr)) { event.setCancelled(true); return; }
            Vector3i b = event.getTargetBlock();
            String key = NpcPostBlocks.key(world.getName(), b.x(), b.y(), b.z());
            if (!NpcPostBlocks.isBlock(key)) return;
            org.joml.Vector3d at = NpcPostBlocks.center(key);
            world.execute(() -> {
                NpcPostBlocks.unregister(key, world);
                // monstre etoile pose a ce poste (2026-11-16) : sa zone et son contrat disparaissent avec le bloc
                var gone = at != null ? com.eldanior.system.boards.MonsterContracts.removeStarredNear(world.getName(), at) : java.util.List.<com.eldanior.system.boards.MonsterContracts.Zone>of();
                var boss = at != null ? com.eldanior.system.boards.DungeonBosses.removeNear(world.getName(), at) : java.util.List.<com.eldanior.system.boards.DungeonBosses.Boss>of();
                EldaniorLogger.info("[NpcPost] bloc casse " + key + " : " + gone.size() + " zone(s) etoilee(s), " + boss.size() + " boss retire(s)");
                pr.sendMessage(Message.raw(!gone.isEmpty() ? "Poste supprimé, avec le contrat, la zone et le monstre étoilé « " + gone.get(0).name() + " »."
                        : !boss.isEmpty() ? "Poste supprimé, avec le " + boss.get(0).kind().toLowerCase() + " « " + boss.get(0).getName() + " »."
                        : "Poste de PNJ supprimé."));
            });
        }

        @Override
        public Query<EntityStore> getQuery() { return Archetype.empty(); }
    }
}
