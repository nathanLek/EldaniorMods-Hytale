package com.eldanior.system.luo;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.UUIDExtractor;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.DelayedEntitySystem;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/**
 * Remise et reprise des plaques d'identification Luo, sur le modele de la cle du tresor (FamilyKeySystem) : toutes les 5 s,
 * l'inventaire est compare au rang. Chacun garde un exemplaire de sa plaque, la recoit s'il ne l'a pas, et toute autre plaque
 * disparait (ancien rang, plaque volee ou ramassee). Le rang change a de nombreux endroits : aucun n'a a s'en occuper.
 * Les admins gardent les plaques qu'ils portent, pour tester l'inclinaison et le territoire.
 */
public class LuoPlaqueSystem extends DelayedEntitySystem<EntityStore> {

    private static final float INTERVAL_SECONDS = 5f;

    public LuoPlaqueSystem() { super(INTERVAL_SECONDS); }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> ref = chunk.getReferenceTo(index);
        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
        if (player == null || pr == null || data == null) return;

        com.eldanior.system.titles.nobility.family.PatriarchOutfit.check(player, pr, data, UUIDExtractor.getUUID(pr));   // 2026-11-18
        com.eldanior.system.titles.nobility.family.HouseRoles.sync(ref, pr, data, UUIDExtractor.getUUID(pr), cb);   // rangs automatiques
        LuoPlaques.Plaque own = LuoPlaques.owned(data, UUIDExtractor.getUUID(pr));
        if (own != null && Item.getAssetMap().getAsset(own.itemId()) == null) own = null;   // assets absents : ne rien toucher
        boolean admin = pr.hasPermission(EldaniorLogger.ADMIN_PERMISSION);

        ItemContainer bag = player.getInventory().getCombinedBackpackStorageHotbar();
        boolean kept = false, expired = false;
        LuoPlaques.Plaque lost = null;
        for (short slot = 0; slot < bag.getCapacity(); slot++) {
            ItemStack stack = bag.getItemStack(slot);
            if (stack == null || stack.isEmpty()) continue;
            LuoPlaques.Plaque p = LuoPlaques.Plaque.of(stack.getItemId());
            if (p == null) continue;
            if (!p.personal()) { if (pass(bag, slot, stack)) expired = true; continue; }   // laissez-passer : il s'use avec le temps
            if (!kept && p == own) kept = true;
            else if (!admin) {
                bag.removeItemStackFromSlot(slot);
                if (p != own) lost = p;
            }
        }
        if (expired) NotificationHelper.sendWarning(pr, "Votre laissez-passer du Clan Luo a expiré : il tombe en poussière. "
                + "Les gardes Luo vous attaqueront de nouveau sur leur territoire.");
        if (lost != null) {
            NotificationHelper.sendWarning(pr, own != null
                    ? "La plaque « " + lost.label() + " » est reprise par le clan."
                    : "La plaque d'identification « " + lost.label() + " » ne vous appartient pas : elle se défait en poussière.");
        }
        if (own != null && !kept && bag.addItemStack(new ItemStack(own.itemId(), 1)).succeeded()) {
            NotificationHelper.sendSuccess(pr, own == LuoPlaques.Plaque.MARCHAND
                    ? "Vous recevez la plaque de Marchand agréé du Comptoir de la Porte du Dragon."
                    : "Vous recevez votre plaque d'identification du Clan Luo : " + own.label() + ".");
        }
    }

    /**
     * Laissez-passer (2026-11-16) : date de fin posee la premiere fois qu'il est vu (achat), durabilite = minutes restantes
     * (mise a jour une fois par minute, la barre de l'objet se vide en 2 jours), retire a l'expiration. Le temps compte meme
     * deconnecte (date reelle). Renvoie true s'il vient d'expirer.
     */
    private static boolean pass(ItemContainer bag, short slot, ItemStack stack) {
        long now = System.currentTimeMillis();
        long end = LuoPlaques.passEnd(stack);
        if (end == 0) {
            end = now + LuoPlaques.VISITOR_MINUTES * 60_000L;
            stack = stack.withMetadata(LuoPlaques.PASS_END, new org.bson.BsonInt64(end));
        }
        if (now >= end) {
            bag.removeItemStackFromSlot(slot);
            return true;
        }
        double left = Math.ceil((end - now) / 60_000.0);
        if (LuoPlaques.passEnd(bag.getItemStack(slot)) != end || Math.abs(stack.getDurability() - left) >= 1)
            bag.setItemStackForSlot(slot, stack.withDurability(left));
        return false;
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() { return Player.getComponentType(); }
}
