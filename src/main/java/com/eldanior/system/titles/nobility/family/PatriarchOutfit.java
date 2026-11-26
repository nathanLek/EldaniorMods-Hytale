package com.eldanior.system.titles.nobility.family;

import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Tenue du Patriarche (2026-11-18, decision de l'utilisateur) : seul le Patriarche d'une maison recoit un equipement a sa
 * promotion, quelle qu'en soit la voie (prendre la tete d'une maison libre, acheter un territoire, couronnement, commande).
 * Le rang change a de nombreux endroits : LuoPlaqueSystem appelle {@link #check} toutes les 5 s pour chaque joueur, et la
 * tenue est remise une seule fois par maison (FamilyRegistry, cle « <maison>#tenue »). Inventaire plein : nouvel essai ensuite.
 * Les tenues de chef ne sont pas vendues par la reserve (scripts/guards/gen_intendant.py).
 */
public final class PatriarchOutfit {

    private PatriarchOutfit() {}

    /** Maison -> prefixe de la tenue (pieces _Head, _Chest, _Hands, _Legs). */
    private static final Map<String, String> OUTFITS = Map.of(
            "luo", "Armor_Kimono_Empereur",
            "runkandel", "Armor_Runkandel_Patriarche",
            "eldanior", "Armor_Eldanior_Roi");

    private static final List<String> PIECES = List.of("_Head", "_Chest", "_Hands", "_Legs");

    public static String outfitOf(String familyId) {
        return familyId == null ? null : OUTFITS.get(FamilyManager.migrateId(familyId).toLowerCase());
    }

    public static void check(Player player, PlayerRef pr, PlayerLevelData data, UUID uuid) {
        if (player == null || data == null || uuid == null || !data.isPatriarch()) return;
        String fam = data.getNobleFamilyId();
        String prefix = outfitOf(fam);
        if (prefix == null) return;
        String key = FamilyManager.migrateId(fam).toLowerCase() + "#tenue";
        if (FamilyRegistry.welcomed(key, uuid)) return;
        List<String> ids = PIECES.stream().map(p -> prefix + p).filter(id -> Item.getAssetMap().getAsset(id) != null).toList();
        if (ids.isEmpty()) return;
        ItemContainer bag = player.getInventory().getCombinedBackpackStorageHotbar();
        int free = 0;
        for (short s = 0; s < bag.getCapacity(); s++) {
            ItemStack st = bag.getItemStack(s);
            if (st == null || st.isEmpty()) free++;
        }
        if (free < ids.size()) return;                                                 // place insuffisante : plus tard
        for (String id : ids) bag.addItemStack(new ItemStack(id, 1));
        FamilyRegistry.markWelcomed(key, uuid);
        var f = FamilyManager.get(FamilyManager.migrateId(fam));
        FamilyRegistry.log(fam, pr.getUsername() + " reçoit la tenue du Patriarche");
        NotificationHelper.sendSuccess(pr, "Patriarche de la maison " + (f != null ? f.getDisplayName() : fam)
                + " : vous recevez la tenue de votre rang.");
    }
}
