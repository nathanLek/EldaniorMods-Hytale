package com.eldanior.system.gui.tabs;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

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

                // Histoire
                String history = family.getHistory();
                ui.set("#FamHistory.Text", history != null && !history.isEmpty() ? history : "");
                ui.set("#FamHistory.Visible", history != null && !history.isEmpty());

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
                    String hist = fam.getHistory();
                    ui.set("#FamCHistory" + i + ".Text", hist != null && !hist.isEmpty() ? hist : "");
                    ui.set("#FamCHistory" + i + ".Visible", hist != null && !hist.isEmpty());

                    // Prix et bouton
                    long famPrice = com.eldanior.system.territory.ParcelManager.getFamilyTerritoryPrice(fam.getId());
                    boolean canAfford = data.getMoney() >= famPrice;
                    if (famPrice > 0) {
                        ui.set("#FamCPriceLabel" + i + ".Text", formatPrice(famPrice));
                        ui.set("#FamCPriceLabel" + i + ".Visible", true);
                        if (canAfford) {
                            ui.set("#FamBtn" + i + ".Text", "CHOISIR");
                            ui.set("#FamBtn" + i + ".Background", "#1a1208");
                        } else {
                            ui.set("#FamBtn" + i + ".Text", "TROP CHER");
                            ui.set("#FamBtn" + i + ".Background", "#2a1a1a");
                        }
                    } else {
                        ui.set("#FamCPriceLabel" + i + ".Visible", false);
                        ui.set("#FamBtn" + i + ".Text", "CHOISIR");
                        ui.set("#FamBtn" + i + ".Background", "#1a1208");
                    }
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
                } catch (Exception e) { EldaniorLogger.error("FamilleTab", e); }
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
            } catch (Exception e) { EldaniorLogger.error("FamilleTab", e); }
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

    private static String formatPrice(long price) {
        if (price >= 1_000_000_000L) return String.format("%.0fG", price / 1_000_000_000.0);
        if (price >= 1_000_000L) return String.format("%.0fM", price / 1_000_000.0);
        if (price >= 1_000L) return String.format("%.0fK", price / 1_000.0);
        return price + " Or";
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

        // Verifier le prix du territoire associe a la famille
        long price = com.eldanior.system.territory.ParcelManager.getFamilyTerritoryPrice(chosenId);
        if (price > 0) {
            if (data.getMoney() < price) {
                // Pas assez d'argent
                try {
                    PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
                    if (pRef != null) {
                        pRef.sendMessage(Message.raw("§cPas assez d'Or ! Il faut " + String.format("%,d", price) + " Or pour acheter ce territoire."));
                    }
                } catch (Exception e) { EldaniorLogger.error("FamilleTab", e); }
                return false;
            }
            // Deduire l'argent
            data.removeMoney(price);

            // Assigner le territoire au joueur
            com.eldanior.system.territory.ParcelData parcel = com.eldanior.system.territory.ParcelManager.getFamilyParcel(chosenId);
            if (parcel != null) {
                try {
                    PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
                    UUID playerUUID = pRef != null ? com.eldanior.system.config.UUIDExtractor.getUUID(pRef) : null;
                    com.hypixel.hytale.server.core.entity.entities.Player player = store.getComponent(ref,
                            com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
                    String playerName = player != null ? player.getDisplayName() : "";
                    if (playerUUID != null) {
                        parcel.setOwnerUUID(playerUUID);
                        parcel.setOwnerName(playerName);
                        parcel.addMember(playerUUID, com.eldanior.system.territory.ParcelRole.OWNER);
                        com.eldanior.system.territory.ParcelManager.save();
                    }
                } catch (Exception e) { EldaniorLogger.error("FamilleTab", e); }
            }
        }

        // Claim famille
        data.setNobleFamilyId(chosenId);
        data.setStatus("PATRIARCH");
        store.putComponent(ref, type, data);
        FamilyManager.claimFamily(chosenId);

        return true;
    }
}
