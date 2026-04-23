package com.eldanior.system.gui.tabs;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.lang.reflect.Field;
import java.util.*;;

public class FamilleTab {

    public static final int MAX_FAMILY_SLOTS = 9;
    public static final int MAX_INVITE_SLOTS = 8;
    private static final List<String> cachedFamilyIds = new ArrayList<>();
    private static final List<String> cachedInviteNames = new ArrayList<>();

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

                // Tresorerie & Contribution
                FamilyManager.FamilyRuntimeData runtime = FamilyManager.getRuntimeData(familyId);
                ui.set("#FamTreasury.Text", runtime.getTreasury() + " Or");
                ui.set("#FamContribution.Text", String.valueOf(runtime.getContribution()));

                // Retrait visible uniquement pour le Patriarch
                ui.set("#FamBtnWithdraw.Visible", data.isPatriarch());

                // Invite visible pour Patriarch + Vice
                boolean canInvite = data.isPatriarch() || data.isVicePatriarch();
                ui.set("#FamInviteSection.Visible", canInvite);
                if (canInvite) {
                    populateInviteList(ui, data);
                }

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

    public static boolean handleWithdraw(Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null || !data.isPatriarch()) return false;

        String famId = data.getNobleFamilyId();
        if (famId == null || famId.isEmpty()) return false;

        FamilyManager.FamilyRuntimeData runtime = FamilyManager.getRuntimeData(famId);
        if (!runtime.withdrawTreasury(1000)) return false;

        data.addMoney(1000);
        store.putComponent(ref, type, data);
        return true;
    }

    public static boolean handleDeposit(Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) return false;

        String famId = data.getNobleFamilyId();
        if (famId == null || famId.isEmpty()) return false;
        if (data.getMoney() < 1000) return false;

        data.removeMoney(1000);
        store.putComponent(ref, type, data);
        FamilyManager.getRuntimeData(famId).addTreasury(1000);
        return true;
    }

    public static boolean handleLeave(Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) return false;

        String famId = data.getNobleFamilyId();
        if (famId == null || famId.isEmpty()) return false;

        // Si Patriarch -> dissoudre : expulser tous les membres en ligne
        if (data.isPatriarch()) {
            for (PlayerRef pRef : Universe.get().getPlayers()) {
                try {
                    var eRef = pRef.getReference();
                    if (eRef == null) continue;
                    var s = eRef.getStore();
                    PlayerLevelData mData = s.getComponent(eRef, type);
                    if (mData == null) continue;
                    if (famId.equals(mData.getNobleFamilyId())) {
                        mData.setNobleFamilyId("");
                        mData.setStatus("");
                        s.putComponent(eRef, type, mData);
                        pRef.sendMessage(Message.raw("§cLa famille a ete dissoute par le Patriarch."));
                    }
                } catch (Exception ignored) {}
            }
            FamilyManager.releaseFamily(famId);
        } else {
            // Membre simple -> juste quitter
            data.setNobleFamilyId("");
            data.setStatus("");
            store.putComponent(ref, type, data);
        }

        return true;
    }

    public static boolean handleInviteByIndex(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int idx;
        try { idx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return false; }
        if (idx < 0 || idx >= cachedInviteNames.size()) return false;

        String targetName = cachedInviteNames.get(idx);

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null || !data.canInviteToFamily()) return false;

        String famId = data.getNobleFamilyId();
        if (famId == null || famId.isEmpty()) return false;
        NobleFamilyModel family = FamilyManager.get(famId);
        if (family == null) return false;

        PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, com.hypixel.hytale.server.core.NameMatching.EXACT_IGNORE_CASE);
        if (targetRef == null) return false;

        // Check target is noble enough and has no family
        var tRef = targetRef.getReference();
        if (tRef != null) {
            var tStore = tRef.getStore();
            PlayerLevelData tData = tStore.getComponent(tRef, type);
            if (tData != null) {
                // Minimum Baron pour rejoindre une famille
                NobilityRank tRank = NobilityRank.fromString(tData.getNobilityRank());
                if (tRank == null || tRank.ordinal() < NobilityRank.BARON.ordinal()) return false;
                if (tData.getNobleFamilyId() != null && !tData.getNobleFamilyId().isEmpty()) return false;
            }
        }

        // Set family on target
        if (tRef != null) {
            var tStore = tRef.getStore();
            PlayerLevelData tData = tStore.getComponent(tRef, type);
            if (tData != null) {
                tData.setNobleFamilyId(famId);
                tData.setStatus("MEMBER");
                tStore.putComponent(tRef, type, tData);
            }
        }

        targetRef.sendMessage(Message.raw("§eVous avez rejoint la famille Von " + family.getDisplayName() + " !"));
        return true;
    }

    private static void populateInviteList(UICommandBuilder ui, PlayerLevelData ownerData) {
        cachedInviteNames.clear();
        String famId = ownerData.getNobleFamilyId();
        NobleFamilyModel family = FamilyManager.get(famId);
        if (family == null) return;

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();

        for (PlayerRef pRef : Universe.get().getPlayers()) {
            if (cachedInviteNames.size() >= MAX_INVITE_SLOTS) break;
            try {
                var eRef = pRef.getReference();
                if (eRef == null) continue;
                var s = eRef.getStore();
                PlayerLevelData d = s.getComponent(eRef, type);
                if (d == null) continue;
                // Must be noble enough, no family yet
                // Minimum Baron pour rejoindre une famille
                NobilityRank rank = NobilityRank.fromString(d.getNobilityRank());
                if (rank == null || rank.ordinal() < NobilityRank.BARON.ordinal()) continue;
                if (d.getNobleFamilyId() != null && !d.getNobleFamilyId().isEmpty()) continue;
                cachedInviteNames.add(pRef.getUsername());
            } catch (Exception ignored) {}
        }

        for (int i = 0; i < MAX_INVITE_SLOTS; i++) {
            if (i < cachedInviteNames.size()) {
                ui.set("#FamInvPlayer" + i + ".Visible", true);
                ui.set("#FamInvName" + i + ".Text", cachedInviteNames.get(i));
            } else {
                ui.set("#FamInvPlayer" + i + ".Visible", false);
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
