package com.eldanior.system.gui.tabs;

import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelType;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.EldaniorSystem;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.lang.reflect.Field;
import java.util.*;

public class TerritoiresTab {

    public static void populate(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        String familyId = "";
        String rank = "";
        try {
            PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
            if (data != null) {
                familyId = data.getNobleFamilyId() != null ? data.getNobleFamilyId() : "";
                rank = data.getNobilityRank() != null ? data.getNobilityRank() : "";
            }
        } catch (Exception ignored) {}

        // Trouver les territoires associes a la famille du joueur
        List<ParcelData> familyParcels = !familyId.isEmpty()
                ? ParcelManager.getByFamily(familyId)
                : List.of();

        StringBuilder sb = new StringBuilder();
        if (familyParcels.isEmpty()) {
            sb.append("Aucun territoire associe a votre famille.");
        } else {
            sb.append("Territoires de votre famille (").append(familyParcels.size()).append(") :\n");
            for (ParcelData p : familyParcels) {
                sb.append("\n[").append(p.getType().getLabel()).append("] ").append(p.getName());
                sb.append(" - ").append(p.getMembers().size()).append(" membres");
                if (p.isProtectedByDefault()) sb.append(" (Protege)");
            }
        }

        ui.set("#TerrInfoLabel.Text", sb.toString());
    }
}
