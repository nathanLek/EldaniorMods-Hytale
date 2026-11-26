package com.eldanior.system.guards;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelType;
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

import java.util.concurrent.TimeUnit;

/**
 * Borne de ville (2026-11-19, demande de l'utilisateur : « la borne de ville doit etre un bloc posable a part, car le bloc de
 * territoire est invisible et a une autre fonction ») : bloc visible (modele de l etabli des capacites, Bench_Abilities, a la demande de l utilisateur) que le Comte pose dans sa
 * ville, une par ville (loi « cityBorne » de la ville). F : le registre de la ville pour le Comte (ou un admin), les lois de la
 * ville pour les autres. Recue a l'achat d'une ville et depuis le registre (« Recevoir la borne de ville »).
 */
public final class CityBornes {

    private CityBornes() {}

    public static final String ITEM = "Eldanior_Borne_Ville";
    private static final String WARN = "#e8c060";

    private static boolean isBorne(BlockType t) {
        if (t == null) return false;
        var item = t.getItem();
        return item != null && ITEM.equals(item.getId()) || t.getId() != null && t.getId().contains(ITEM);
    }

    private static String code(String world, Vector3i b) { return world + "|" + b.x() + "|" + b.y() + "|" + b.z(); }

    /** Borne posee de la ville encore la ? (chunk non charge : on suppose que oui) */
    public static boolean placed(ParcelData city) {
        String c = city.law("cityBorne");
        if (c.isEmpty()) return false;
        String[] p = c.split("\\|");
        try {
            World w = com.hypixel.hytale.server.core.universe.Universe.get().getWorld(p[0]);
            if (w == null) return true;
            BlockType t = WorldBlockUtil.getBlockType(w, Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]));
            return t == null || isBorne(t);
        } catch (Exception e) {
            return false;
        }
    }

    /** Donne une borne de ville (sac) ; false si le sac est plein. */
    public static boolean give(Player player) {
        return player != null && player.getInventory().getCombinedBackpackStorageHotbar().addItemStack(new ItemStack(ITEM, 1)).succeeded();
    }

    public static boolean carries(Player player) {
        if (player == null) return false;
        var bag = player.getInventory().getCombinedBackpackStorageHotbar();
        for (short s = 0; s < bag.getCapacity(); s++) {
            ItemStack st = bag.getItemStack(s);
            if (st != null && !st.isEmpty() && ITEM.equals(st.getItemId())) return true;
        }
        return false;
    }

    private static boolean admin(PlayerRef pr) { return pr != null && pr.hasPermission(EldaniorLogger.ADMIN_PERMISSION); }

    public static class Place extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
        public Place() { super(PlaceBlockEvent.class); }

        @Override
        public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl PlaceBlockEvent event) {
            ItemStack hand = event.getItemInHand();
            if (hand == null || hand.isEmpty() || !ITEM.equals(hand.getItemId())) return;
            Player player = chunk.getComponent(index, Player.getComponentType());
            PlayerRef pr = chunk.getComponent(index, PlayerRef.getComponentType());
            World world = player != null ? player.getWorld() : null;
            if (pr == null || world == null) return;
            Vector3i at = new Vector3i(event.getTargetBlock());
            ParcelData city = CityStewards.cityAt(world.getName(), at.x() + 0.5, at.y() + 0.5, at.z() + 0.5);
            String refusal = city == null ? "La borne de ville se pose dans une ville."
                    : !admin(pr) && !city.isOwner(pr.getUuid()) ? "Seul le Comte de " + city.getName() + " y pose la borne de la ville."
                    : placed(city) ? city.getName() + " a déjà sa borne." : null;
            if (refusal != null) {
                event.setCancelled(true);
                pr.sendMessage(Message.raw(refusal).color(WARN));
                return;
            }
            String id = city.getId();
            EldaniorLogger.SCHEDULER.schedule(() -> world.execute(() -> {
                try {
                    if (!isBorne(WorldBlockUtil.getBlockType(world, at.x(), at.y(), at.z()))) return;   // pose annulee
                    ParcelData c = ParcelManager.get(id);
                    if (c == null) return;
                    c.setLaw("cityBorne", code(world.getName(), at));
                    ParcelManager.save();
                    pr.sendMessage(Message.raw("Borne de " + c.getName() + " posée : touche d'interaction pour ouvrir le registre de la ville.").color("#7fd4ff"));
                } catch (Exception e) {
                    EldaniorLogger.error("[Borne de ville] pose", e);
                }
            }), 300, TimeUnit.MILLISECONDS);
        }

        @Override
        public Query<EntityStore> getQuery() { return Archetype.empty(); }
    }

    public static class Use extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {
        public Use() { super(UseBlockEvent.Pre.class); }

        @Override
        public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl UseBlockEvent.Pre event) {
            if (event.getInteractionType() != InteractionType.Use || !isBorne(event.getBlockType())) return;
            Ref<EntityStore> ref = event.getContext().getEntity();
            Player player = store.getComponent(ref, Player.getComponentType());
            PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
            World world = player != null ? player.getWorld() : null;
            if (pr == null || world == null) return;
            Vector3i b = new Vector3i(event.getTargetBlock());
            world.execute(() -> {
                if (!ref.isValid()) return;
                ParcelData city = CityStewards.cityAt(world.getName(), b.x() + 0.5, b.y() + 0.5, b.z() + 0.5);
                if (city == null) { pr.sendMessage(Message.raw("Cette borne n'est dans aucune ville.").color(WARN)); return; }
                if (IntendantPage.canOpenCity(city, pr.getUuid(), pr))
                    player.getPageManager().openCustomPage(ref, ref.getStore(), IntendantPage.forCity(pr, city, "Borne de " + city.getName()));
                else pr.sendMessage(Message.raw("Borne de " + city.getName() + (city.getOwnerName() != null && !city.getOwnerName().isEmpty()
                        ? " (Comte : " + city.getOwnerName() + ")" : "") + ". " + CityStewards.greeting(city)).color("#c8b48a"));
            });
        }

        @Override
        public Query<EntityStore> getQuery() { return Archetype.empty(); }
    }

    public static class Break extends EntityEventSystem<EntityStore, BreakBlockEvent> {
        public Break() { super(BreakBlockEvent.class); }

        @Override
        public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl BreakBlockEvent event) {
            if (!isBorne(event.getBlockType())) return;
            Player player = chunk.getComponent(index, Player.getComponentType());
            PlayerRef pr = chunk.getComponent(index, PlayerRef.getComponentType());
            World world = player != null ? player.getWorld() : null;
            if (pr == null || world == null) return;
            Vector3i b = event.getTargetBlock();
            ParcelData city = CityStewards.cityAt(world.getName(), b.x() + 0.5, b.y() + 0.5, b.z() + 0.5);
            if (city != null && !admin(pr) && !city.isOwner(pr.getUuid())) {
                event.setCancelled(true);
                pr.sendMessage(Message.raw("Cette borne appartient à " + city.getName() + ".").color(WARN));
                return;
            }
            if (city != null && code(world.getName(), b).equals(city.law("cityBorne"))) {
                city.setLaw("cityBorne", "");
                ParcelManager.save();
            }
        }

        @Override
        public Query<EntityStore> getQuery() { return Archetype.empty(); }
    }

    static boolean isCity(ParcelData p) { return p != null && p.getType() == ParcelType.CITY; }
}
