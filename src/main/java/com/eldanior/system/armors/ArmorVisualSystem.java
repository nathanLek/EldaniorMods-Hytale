package com.eldanior.system.armors;

import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.DelayedEntitySystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Effets visuels portes par certaines pieces d'armure : tant que la piece est equipee, son effet (particules attachees a un os du
 * joueur) reste pose en infini ; il disparait des que la piece est retiree. Les particules d'un objet porte ne sont pas garanties
 * par le client, un effet d'entite l'est.
 *
 * Voile d'Ombre de la Lune Noire (Assassin divin) : la tete n'est plus qu'une fumee noire (Armure_Fumee_Ombre).
 */
public class ArmorVisualSystem extends DelayedEntitySystem<EntityStore> {

    private static final float INTERVAL_SECONDS = 0.5f;
    private static final short HEAD_SLOT = 0;

    /** Casque -> effet visuel. */
    private static final Map<String, String> HEAD_VISUALS = Map.of(
            "Armor_Divin_Assassin_Head", "Armure_Fumee_Ombre"
    );

    public ArmorVisualSystem() { super(INTERVAL_SECONDS); }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> ref = chunk.getReferenceTo(index);
        if (!ref.isValid()) return;
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        String wanted = HEAD_VISUALS.get(headItemId(player));
        for (String visual : HEAD_VISUALS.values()) {
            if (!visual.equals(wanted) && EffectsManager.hasEffect(ref, visual, store)) EffectsManager.removeEffect(ref, visual, store);
        }
        if (wanted != null && !EffectsManager.hasEffect(ref, wanted, store)) EffectsManager.applyInfiniteEffect(ref, wanted, store);
    }

    private static String headItemId(Player player) {
        Inventory inv = player.getInventory();
        ItemContainer armor = inv != null ? inv.getArmor() : null;
        if (armor == null) return "";
        ItemStack head = armor.getItemStack(HEAD_SLOT);
        return head == null || head.isEmpty() ? "" : head.getItemId();
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() { return Player.getComponentType(); }
}
