package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.DelayedEntitySystem;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.entity.AnimationUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rengainage des katanas du Clan Luo ({@link LuoKatanas}, dont Tenryumaru) : tant que le joueur attaque, la lame reste au clair ; apres IDLE_MS sans aucune
 * interaction en cours, l'animation Rengainer (noto) est jouee, puis l'objet degaine redevient l'objet au fourreau. Une attaque
 * pendant le noto l'annule. Un objet degaine qui n'est pas en main (autre emplacement de la barre, sac, sac a dos) est rengaine
 * tout de suite.
 *
 * Fourreau : le moteur accroche tout l'objet tenu a la main droite ; le fourreau est donc un objet utilitaire (comme un
 * bouclier, rendu dans la main gauche), pose tant que le katana est en main : SHEATH (lame rengainee, monture visible) avec la
 * forme au fourreau, SHEATH_EMPTY avec la forme au clair. Il prend un emplacement utilitaire libre, devient l'emplacement actif,
 * puis disparait des que le katana n'est plus en main ; l'emplacement utilitaire actif d'avant est rendu. Un fourreau trouve
 * ailleurs (barre, sac, sac a dos) est supprime.
 */
public class TenryumaruSystem extends DelayedEntitySystem<EntityStore> {

    private static final float INTERVAL_SECONDS = 0.1f;
    /** Delai sans attaquer avant de rengainer. */
    static final long IDLE_MS = 2_500;
    /** Duree du noto (animation Rengainer, 40 images) jusqu'a l'echange d'objet : la lame disparait a l'image 25. */
    static final long SHEATHE_MS = 440;

    public static final String SHEATH = "Artefact_Tenryumaru_Fourreau", SHEATH_EMPTY = SHEATH + "_Vide";

    /** Emplacement utilitaire actif avant la pose du fourreau (rendu ensuite). */
    private static final Map<Ref<EntityStore>, Byte> previousUtility = new ConcurrentHashMap<>();
    /** Joueurs deja prevenus qu'aucun emplacement utilitaire n'est libre (jusqu'a ce qu'ils lachent le katana). */
    private static final Set<Ref<EntityStore>> warned = ConcurrentHashMap.newKeySet();

    public TenryumaruSystem() { super(INTERVAL_SECONDS); }

    static boolean isSheath(String id) {
        return LuoKatanas.isSheath(id);                                       // fourreaux de tous les katanas du Clan Luo
    }

    /** La lame vient d'etre degainee : le delai repart de maintenant. Etat : {derniere action, instant de l'echange, force}. */
    static void drawn(Ref<EntityStore> ref) {
        Tenryumaru.state.put(ref, new long[]{System.currentTimeMillis(), 0, 0});
    }

    /** Rengainage demande (clic droit bref) : noto tout de suite, echange a la fin, meme si l'interaction tourne encore. */
    static void sheatheNow(Ref<EntityStore> ref, Store<EntityStore> store) {
        long now = System.currentTimeMillis();
        AnimationUtils.playAnimation(ref, AnimationSlot.Action, "Tenryumaru_Degainee", "Rengainer", true, store);
        Tenryumaru.state.put(ref, new long[]{now, now + SHEATHE_MS, 1});
    }

    /** Remet tout de suite le bon fourreau (plein / vide) selon l'objet tenu. */
    static void syncSheath(Ref<EntityStore> ref, Store<EntityStore> store) {
        Player player = store.getComponent(ref, Player.getComponentType());
        InventoryComponent hotbar = store.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
        if (player == null || hotbar == null || player.getInventory() == null) return;
        short active = player.getInventory().getActiveHotbarSlot();
        ItemStack held = active < 0 ? null : hotbar.getInventory().getItemStack(active);
        String id = held == null || held.isEmpty() ? null : held.getItemId();
        manageSheath(ref, player, LuoKatanas.sheathFor(id), store);
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> ref = chunk.getReferenceTo(index);
        if (!ref.isValid()) return;
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null || player.getInventory() == null) return;
        short active = player.getInventory().getActiveHotbarSlot();

        sheatheIdle(ref, active, store);

        InventoryComponent hotbar = store.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
        ItemStack held = hotbar == null || active < 0 ? null : hotbar.getInventory().getItemStack(active);
        String heldId = held == null || held.isEmpty() ? null : held.getItemId();
        manageSheath(ref, player, LuoKatanas.sheathFor(heldId), store);
        LuoKatanas.Katana katana = LuoKatanas.of(heldId);
        if (katana != null) LuoPassives.trackGuard(ref, store);                  // contre parfait du Bokken
        if (katana != null) LuoPassives.trackTier(ref, store, cb);              // combo du Bokken selon la voie martiale
        if (katana == null || !katana.drawn().equals(heldId)) {
            Tenryumaru.state.remove(ref);
            return;
        }
        long now = System.currentTimeMillis();
        long[] st = Tenryumaru.state.computeIfAbsent(ref, r -> new long[]{now, 0, 0});
        if (st[2] == 0 && busy(ref, store)) {                                                  // attaque, parade, charge : la lame reste au clair
            st[0] = now;
            st[1] = 0;
            return;
        }
        if (st[1] == 0 && now - st[0] >= IDLE_MS) {
            AnimationUtils.playAnimation(ref, AnimationSlot.Action, "Tenryumaru_Degainee", "Rengainer", true, store);
            st[1] = now + SHEATHE_MS;
        } else if (st[1] != 0 && now >= st[1]) {
            Tenryumaru.swapActive(ref, katana.drawn(), katana.id(), store);
            Tenryumaru.state.remove(ref);
        }
    }

    /** Rengaine toute forme degainee qui n'est pas l'objet en main. */
    private static void sheatheIdle(Ref<EntityStore> ref, short active, Store<EntityStore> store) {
        for (var type : List.of(InventoryComponent.Hotbar.getComponentType(), InventoryComponent.Storage.getComponentType(),
                InventoryComponent.Backpack.getComponentType())) {
            InventoryComponent section = store.getComponent(ref, type);
            if (section == null) continue;
            boolean isHotbar = type == InventoryComponent.Hotbar.getComponentType();
            ItemContainer container = section.getInventory();
            Map<Short, ItemStack> replace = new HashMap<>();
            container.forEach((slot, stack) -> {
                if (stack != null && LuoKatanas.isDrawn(stack.getItemId()) && !(isHotbar && slot == active)) {
                    replace.put(slot, Tenryumaru.swap(stack, LuoKatanas.of(stack.getItemId()).id()));
                } else if (stack != null && isSheath(stack.getItemId())) {
                    replace.put(slot, null);                                      // fourreau hors de l'emplacement utilitaire
                }
            });
            if (replace.isEmpty()) continue;
            replace.forEach((slot, stack) -> {
                if (stack == null) container.removeItemStackFromSlot(slot);
                else container.setItemStackForSlot(slot, stack);
            });
            section.markDirty();
        }
    }

    /** Pose (wanted = forme du fourreau) ou retire (wanted = null) le fourreau dans l'emplacement utilitaire. */
    private static void manageSheath(Ref<EntityStore> ref, Player player, String wanted, Store<EntityStore> store) {
        InventoryComponent section = store.getComponent(ref, InventoryComponent.Utility.getComponentType());
        if (section == null) return;
        ItemContainer util = section.getInventory();
        short found = -1, free = -1;
        boolean dirty = false;
        for (short slot = 0; slot < util.getCapacity(); slot++) {
            ItemStack stack = util.getItemStack(slot);
            boolean empty = stack == null || stack.isEmpty();
            if (!empty && isSheath(stack.getItemId())) {
                if (wanted == null || found >= 0) { util.removeItemStackFromSlot(slot); dirty = true; continue; }
                found = slot;
                if (!wanted.equals(stack.getItemId())) { util.setItemStackForSlot(slot, new ItemStack(wanted, 1)); dirty = true; }
            } else if (empty && free < 0) {
                free = slot;
            }
        }
        if (wanted == null) {
            warned.remove(ref);
            Byte prev = previousUtility.remove(ref);
            if (dirty) {
                section.markDirty();
                if (prev != null) player.getInventory().setActiveUtilitySlot(ref, prev, store);
            }
            return;
        }
        if (found < 0) {
            if (free < 0) {
                if (warned.add(ref)) {
                    PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
                    if (pr != null) NotificationHelper.sendNotification(pr,
                            "<color:gold>Libérez un emplacement utilitaire pour porter le fourreau de Tenryūmaru.</color>", NotificationStyle.Warning);
                }
                return;
            }
            util.setItemStackForSlot(free, new ItemStack(wanted, 1));
            found = free;
            dirty = true;
        }
        if (dirty) section.markDirty();
        byte activeUtil = player.getInventory().getActiveUtilitySlot();
        if (activeUtil != found) {
            previousUtility.putIfAbsent(ref, activeUtil);
            player.getInventory().setActiveUtilitySlot(ref, (byte) found, store);
        }
    }

    /** true si une chaine d'interaction est en cours (attaque, charge, parade, signature). */
    private static boolean busy(Ref<EntityStore> ref, Store<EntityStore> store) {
        try {
            var manager = store.getComponent(ref, InteractionModule.get().getInteractionManagerComponent());
            return manager != null && !manager.getChains().isEmpty();
        } catch (Exception e) {
            EldaniorLogger.error("TenryumaruSystem: lecture des interactions", e);
            return true;
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() { return Player.getComponentType(); }
}
