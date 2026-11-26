package com.eldanior.system.skills.runes;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/**
 * Entretien des runes, toutes les secondes :
 *  - don automatique de la Rune d'Aura (sac de runes) des que la dignite le permet,
 *  - restriction de classe a l'equipement : une rune non autorisee retourne dans le sac.
 */
public class RuneSystem extends EntityTickingSystem<EntityStore> {

    private float updateTimer = 0;
    private boolean shouldUpdate = false;

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
                     @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        if (index == 0) {
            updateTimer += dt;
            if (updateTimer >= 1.0f) {
                shouldUpdate = true;
                updateTimer = 0;
            } else {
                shouldUpdate = false;
            }
        }
        if (!shouldUpdate) return;

        Ref<EntityStore> playerRef = chunk.getReferenceTo(index);
        if (!playerRef.isValid()) return;

        PlayerLevelData data = store.getComponent(playerRef, EldaniorSystem.get().getPlayerLevelDataType());
        PlayerRef netRef = store.getComponent(playerRef, PlayerRef.getComponentType());
        if (data == null || netRef == null) return;

        RuneManager.migrateLegacyRunes(playerRef, store); // runes divines renommees

        if (data.getDignity() >= RuneManager.AURA_RUNE_MIN_DIGNITY
                && com.eldanior.system.classes.definitions.luo.VoieMartiale.tier(data) == 0   // artistes martiaux : pas d'aura
                && RuneManager.grantAuraRuneIfMissing(playerRef, store)) {
            NotificationHelper.sendSuccess(netRef,
                    "<color:gold>Manuel de l'Aura reçu !</color> <color:gray>Maintenez le clic droit sur le manuel pour apprendre la Rune d'Aura.</color>");
        }

        java.util.List<String> classRunes = RuneManager.grantClassRunes(playerRef, store, data);   // manuels de sa classe
        if (!classRunes.isEmpty()) {
            String names = String.join(", ", classRunes.stream().map(RuneSystem::bookLabel).toList());
            NotificationHelper.sendSuccess(netRef,
                    "<color:gold>Manuels de classe reçus : " + names + "</color> <color:gray>Maintenez le clic droit sur un manuel "
                            + "pour le lire et apprendre sa rune.</color>");
        }

        // Cercle d'Ancrage : donne avec les runes d'Espace (jusqu'a la limite d'ancres du joueur)
        com.eldanior.system.anchors.AnchorManager.grantCircles(playerRef, store, netRef);

        if (RuneManager.enforceAbilitySlots(playerRef, store, data) > 0) {
            NotificationHelper.sendWarning(netRef,
                    "<color:red>Votre classe ne peut pas utiliser cette rune.</color> <color:gray>Elle a ete renvoyee dans votre sac de runes.</color>");
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }

    /** Nom court d'un manuel remis : nom affiche de l'objet, sinon la fin de son identifiant. */
    private static String bookLabel(String itemId) {
        try {
            var item = com.hypixel.hytale.server.core.asset.type.item.config.Item.getAssetMap().getAsset(itemId);
            var tp = item != null ? item.getTranslationProperties() : null;
            String name = tp != null ? tp.getName() : null;
            if (name != null && !name.isBlank() && !name.startsWith("server.")) return name;
        } catch (Exception ignored) { }
        return itemId.replaceAll("^Eldanior_Manuel_(Magie_[A-Za-z]+_|Luo_)", "").replace("Rune_", "");
    }

}
