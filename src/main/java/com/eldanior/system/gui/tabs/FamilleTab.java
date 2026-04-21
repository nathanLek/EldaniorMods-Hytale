package com.eldanior.system.gui.tabs;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.ArrayList;
import java.util.List;

public class FamilleTab {

    public static final int MAX_FAMILY_SLOTS = 9;
    private static final List<String> cachedFamilyIds = new ArrayList<>();

    public static void populate(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) return;

        String familyId = data.getNobleFamilyId();
        boolean hasFamily = familyId != null && !familyId.isEmpty();

        ui.set("#FamilleNoFamily.Visible", !hasFamily);
        ui.set("#FamilleHasFamily.Visible", hasFamily);

        if (hasFamily) {
            // Show current family info
            NobleFamilyModel family = FamilyManager.get(familyId);
            if (family != null) {
                ui.set("#FamName.Text", "Von " + family.getDisplayName());
                ui.set("#FamMotto.Text", "\"" + family.getMotto() + "\"");
                ui.set("#FamRarity.Text", family.getRarity().name());
                ui.set("#FamRank.Text", "Rang minimum : " + family.getMinimumRank().getDisplayName());
                ui.set("#FamPassive.Text", family.getFamilyPassive() != null ? family.getFamilyPassive().getDisplayName() + " - " + family.getFamilyPassive().getDescription() : "Aucun passif");

                String status = data.getStatus();
                ui.set("#FamStatus.Text", status != null && !status.isEmpty() ? status : "MEMBER");
            }
        } else {
            // Show available families for player's rank
            NobilityRank rank = NobilityRank.fromString(data.getNobilityRank());
            if (rank == null) rank = NobilityRank.ROTURIER;

            ui.set("#FamChooseTitle.Text", "CHOISIR VOTRE FAMILLE (" + rank.getDisplayName() + ")");

            List<NobleFamilyModel> available = FamilyManager.getAvailableFamiliesForRank(rank);
            cachedFamilyIds.clear();

            for (int i = 0; i < MAX_FAMILY_SLOTS; i++) {
                if (i < available.size()) {
                    NobleFamilyModel fam = available.get(i);
                    cachedFamilyIds.add(fam.getId());

                    ui.set("#FamChoice" + i + ".Visible", true);
                    ui.set("#FamCName" + i + ".Text", "Von " + fam.getDisplayName());
                    ui.set("#FamCMotto" + i + ".Text", "\"" + fam.getMotto() + "\"");
                    ui.set("#FamCRarity" + i + ".Text", fam.getRarity().name());
                    ui.set("#FamCPassive" + i + ".Text", fam.getFamilyPassive() != null ? fam.getFamilyPassive().getDisplayName() : "");
                } else {
                    ui.set("#FamChoice" + i + ".Visible", false);
                }
            }
        }
    }

    public static boolean handleChoose(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int idx;
        try { idx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return false; }
        if (idx < 0 || idx >= cachedFamilyIds.size()) return false;

        String chosenId = cachedFamilyIds.get(idx);
        NobleFamilyModel family = FamilyManager.get(chosenId);
        if (family == null) return false;

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) return false;

        // Check rank matches
        NobilityRank rank = NobilityRank.fromString(data.getNobilityRank());
        if (rank == null || rank.ordinal() < family.getMinimumRank().ordinal()) return false;

        // Check not taken
        if (FamilyManager.isFamilyTaken(chosenId)) return false;

        // Claim
        data.setNobleFamilyId(chosenId);
        data.setStatus("PATRIARCH");
        store.putComponent(ref, type, data);
        FamilyManager.claimFamily(chosenId);

        return true;
    }
}
