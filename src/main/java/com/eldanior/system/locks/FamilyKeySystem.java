package com.eldanior.system.locks;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.titles.nobility.family.FamilyManager;
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
import java.util.List;
import java.util.Map;

/**
 * Seul le Patriarche possede la cle du tresor : chaque Patriarche a la sienne, en un exemplaire, et toute autre cle de famille
 * disparait de l'inventaire qui la porte (cle volee, tombee au sol, ancien Patriarche). Comparer l'inventaire au statut suffit : le
 * titre change a une dizaine d'endroits (commande, succession, dissolution, admin...), aucun n'a a s'en occuper.
 *
 * Meme regle pour les reliques de famille (RELICS) : un exemplaire de chacune, au Patriarche seul, qui lui revient toujours
 * (pieces d'armure portees comprises). Les admins gardent les reliques qu'ils portent, pour les tester (MasterySystem les leur
 * laisse tenir).
 */
public class FamilyKeySystem extends DelayedEntitySystem<EntityStore> {

    /** Don automatique des manuels de clan (remplace par le Sensei le 2026-11-10). */
    static final boolean AUTO_TECHNIQUES = false;


    private static final float INTERVAL_SECONDS = 5f;

    /**
     * Reliques et tresor de chaque famille (identifiant de famille -> objets). Clan Luo : Tenryumaru, Kinryu-O et la Tenue
     * Ancestrale du Dieu-Dragon d'Or (decision du 2026-11-06 : tresor familial, au Patriarche seul, repris a la succession).
     */
    private static final Map<String, List<String>> RELICS = Map.of("luo", List.of(
            com.eldanior.system.skills.runes.spells.Tenryumaru.ITEM, com.eldanior.system.skills.runes.spells.Kinryu.ITEM,
            "Armor_Kimono_Divin_Head", "Armor_Kimono_Divin_Chest", "Armor_Kimono_Divin_Hands", "Armor_Kimono_Divin_Legs"),
            // Doyens de l'Empire Luo : le Patriarche de chaque clan recoit le kimono de Doyen de sa couleur (2026-11-10)
            "sama", com.eldanior.system.luo.LuoClans.Clan.SAMA.elderPieces(),
            "hanazakura", com.eldanior.system.luo.LuoClans.Clan.HANAZAKURA.elderPieces(),
            "kage", com.eldanior.system.luo.LuoClans.Clan.KAGE.elderPieces(),
            "setsuka", com.eldanior.system.luo.LuoClans.Clan.SETSUKA.elderPieces());

    /** Forme de reference d'un objet : lame au clair = la relique, hakama eveille = le hakama. */
    private static String relicBase(String id) {
        if (id == null) return null;
        var k = com.eldanior.system.skills.runes.spells.LuoKatanas.of(id);
        if (k != null) return k.id();
        if (id.equals("Armor_Kimono_Divin_Legs_Eveil")) return "Armor_Kimono_Divin_Legs";
        return id;
    }

    private static boolean isRelic(String base) {
        for (List<String> l : RELICS.values()) if (l.contains(base)) return true;
        return false;
    }

    public FamilyKeySystem() { super(INTERVAL_SECONDS); }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> ref = chunk.getReferenceTo(index);
        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
        if (player == null || pr == null || data == null) return;

        String own = ownKey(data);
        ItemContainer bag = player.getInventory().getCombinedBackpackStorageHotbar();
        boolean kept = false;
        for (short slot = 0; slot < bag.getCapacity(); slot++) {
            ItemStack stack = bag.getItemStack(slot);
            if (stack == null || stack.isEmpty() || !stack.getItemId().startsWith(DoorLock.FAMILY_KEY)) continue;
            if (!kept && stack.getItemId().equals(own)) kept = true;
            else bag.removeItemStackFromSlot(slot);
        }
        if (own != null && !kept && bag.addItemStack(new ItemStack(own, 1)).succeeded()) {
            NotificationHelper.sendSuccess(pr, "Vous détenez la Clé du Trésor de la famille " + FamilyManager.get(data.getNobleFamilyId()).getDisplayName() + ".");
        }

        // 2026-11-10 (nuit) : les techniques du clan s'apprennent aupres du Sensei de la famille (guards.Sensei), plus de don
        // automatique des manuels (remettre AUTO_TECHNIQUES a true pour revenir a l'ancien fonctionnement)
        int manuals = AUTO_TECHNIQUES ? com.eldanior.system.luo.LuoClans.grantTechniques(ref, store, data, pr.getUuid()) : 0;
        if (manuals > 0) NotificationHelper.sendSuccess(pr, manuals == 1 ? "Votre clan vous confie un manuel secret."
                : "Votre clan vous confie " + manuals + " manuels secrets.");

        // 2026-11-12 : en rejoignant une famille, le manuel de son 1er Mouvement (une seule fois, FamilyRegistry)
        String movement = com.eldanior.system.skills.runes.spells.LuoManuels.welcome(ref, store, data, pr.getUuid());
        if (movement != null) NotificationHelper.sendSuccess(pr, "Votre famille vous confie le manuel du " + movement + ".");

        List<String> mine = ownRelics(data);
        boolean admin = pr.hasPermission(com.eldanior.system.config.EldaniorLogger.ADMIN_PERMISSION);
        java.util.Set<String> held = new java.util.HashSet<>();
        ItemContainer armor = player.getInventory().getArmor();
        for (ItemContainer c : armor != null ? List.of(armor, bag) : List.of(bag)) {          // pieces portees d'abord
            for (short slot = 0; slot < c.getCapacity(); slot++) {
                ItemStack stack = c.getItemStack(slot);
                if (stack == null || stack.isEmpty()) continue;
                String base = relicBase(stack.getItemId());
                if (!isRelic(base)) continue;
                if (mine.contains(base) && held.add(base)) continue;                         // le sien, un exemplaire
                if (!admin) c.removeItemStackFromSlot(slot);                                 // en double, ou pas le Patriarche
            }
        }
        int given = 0;
        for (String relic : mine) {
            if (!held.contains(relic) && bag.addItemStack(new ItemStack(relic, 1)).succeeded()) given++;
        }
        if (given > 0) NotificationHelper.sendSuccess(pr, given == 1 ? "La relique de votre famille vous revient."
                : "Le trésor de votre famille vous revient (" + given + " objets).");
    }

    /** Reliques du joueur s'il est Patriarche d'une famille qui en a, sinon liste vide. */
    private static List<String> ownRelics(PlayerLevelData data) {
        String familyId = data.getNobleFamilyId();
        if (!data.isPatriarch() || familyId == null) return List.of();
        List<String> out = new java.util.ArrayList<>();
        for (String relic : RELICS.getOrDefault(familyId, List.of()))
            if (Item.getAssetMap().getAsset(relic) != null) out.add(relic);
        return out;
    }

    /** Cle du joueur s'il est Patriarche d'une famille connue, sinon null. */
    private static String ownKey(PlayerLevelData data) {
        String familyId = data.getNobleFamilyId();
        if (!data.isPatriarch() || familyId == null || familyId.isEmpty() || FamilyManager.get(familyId) == null) return null;
        String key = DoorLock.familyKey(familyId);
        return Item.getAssetMap().getAsset(key) != null ? key : null;
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() { return Player.getComponentType(); }
}
