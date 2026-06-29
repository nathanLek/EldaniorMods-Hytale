package com.eldanior.system.gui.tabs;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.eldanior.system.titles.nobility.family.KnightOrder;
import com.eldanior.system.titles.nobility.family.KnightOrderManager;
import com.eldanior.system.titles.nobility.family.KnightOrderRole;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FamilleTab {

    public static final int MAX_FAMILY_SLOTS = 9;
    public static final int MAX_MEMBER_SLOTS = 5;
    public static final int MAX_INVITE_SLOTS = 8;
    public static final int MAX_ORDRE_MEMBER_SLOTS = 5;
    public static final int MAX_ORDRE_CARDS = 3;

    // Per-player pagination state
    private static final ConcurrentHashMap<UUID, Integer> memberPages = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Integer> invitePages = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Integer> ordreMemberPages = new ConcurrentHashMap<>();

    // Accordion state: "members", "ordres", "invite" — one open at a time, null = "ordres" by default
    private static final ConcurrentHashMap<UUID, String> openAccordion = new ConcurrentHashMap<>();

    // Order detail view: null = family view, orderId = detail view
    private static final ConcurrentHashMap<UUID, String> viewingOrderId = new ConcurrentHashMap<>();

    // Cached data for slot -> actual index mapping
    private static final ConcurrentHashMap<UUID, List<UUID>> playerCachedMembers = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, List<String>> playerCachedInviteNames = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, List<String>> playerCachedFamilyIds = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, List<UUID>> playerCachedOrdreMembers = new ConcurrentHashMap<>();

    // ==================== POPULATE ====================

    public static void populate(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) return;

        UUID uuid = getPlayerUUID(ref, store);

        String rankStr = data.getNobilityRank();
        boolean isNoble = rankStr != null && !"ROTURIER".equals(rankStr) && !"".equals(rankStr);
        String familyId = data.getNobleFamilyId();
        boolean hasFamily = familyId != null && !familyId.isEmpty();

        // Check if viewing an order detail
        String orderDetailId = viewingOrderId.get(uuid);
        boolean showingOrderDetail = orderDetailId != null;

        // 4 states
        ui.set("#FamilleNotNoble.Visible", !isNoble && !hasFamily);
        ui.set("#FamilleNoFamily.Visible", isNoble && !hasFamily);
        ui.set("#FamilleHasFamily.Visible", hasFamily && !showingOrderDetail);
        ui.set("#OrdreDetailView.Visible", hasFamily && showingOrderDetail);

        if (!isNoble && !hasFamily) return;

        if (isNoble && !hasFamily) {
            populateFamilyChoices(ui, data, uuid);
            return;
        }

        if (showingOrderDetail) {
            populateOrderDetail(ui, orderDetailId, data, uuid);
            return;
        }

        // === HAS FAMILY — populate main panel ===
        NobleFamilyModel family = FamilyManager.get(familyId);
        if (family == null) return;

        boolean isPatriarch = data.isPatriarch();
        boolean isVice = data.isVicePatriarch();

        // 1. Header
        ui.set("#FamName.Text", "Von " + family.getDisplayName());
        ui.set("#FamMotto.Text", "\"" + family.getMotto() + "\"");

        // 2. Info cards
        ui.set("#FamRarity.Text", family.getRarity().name());
        ui.set("#FamRank.Text", family.getMinimumRank().getDisplayName());
        String status = data.getStatus();
        ui.set("#FamStatus.Text", "PATRIARCH".equals(status) ? "Patriarche" : "VICE".equals(status) ? "Vice-Patriarche" : "Membre");

        // 3. Treasury + Contribution
        FamilyManager.FamilyRuntimeData runtime = FamilyManager.getRuntimeData(familyId);
        ui.set("#FamTreasury.Text", runtime.getTreasury() + " Or");
        ui.set("#FamContribution.Text", String.valueOf(runtime.getContribution()));
        ui.set("#FamBtnWithdraw.Visible", isPatriarch);

        // 4. Family passive
        if (family.getFamilyPassive() != null) {
            ui.set("#FamPassiveName.Text", family.getFamilyPassive().getDisplayName());
            ui.set("#FamPassiveDesc.Text", family.getFamilyPassive().getDescription());
        }

        // 5. History
        String history = family.getHistory();
        ui.set("#FamHistoryCard.Visible", history != null && !history.isEmpty());
        ui.set("#FamHistory.Text", history != null ? history : "");

        // 6. Accordion state
        String currentAccordion = openAccordion.get(uuid);
        boolean membersOpen = "members".equals(currentAccordion);
        boolean ordresOpen = "ordres".equals(currentAccordion) || currentAccordion == null;
        boolean inviteOpen = "invite".equals(currentAccordion);

        // === Members accordion ===
        populateMembers(ui, familyId, uuid, isPatriarch, isVice);
        ui.set("#FamBtnToggleMembers.Text", (membersOpen ? "v" : ">") + " MEMBRES");
        ui.set("#FamMembersContent.Visible", membersOpen);

        if (!membersOpen) {
            for (int i = 0; i < MAX_MEMBER_SLOTS; i++) {
                ui.set("#FMemberSlot" + i + ".Visible", false);
            }
        }

        // === Knight Orders accordion ===
        List<KnightOrder> orders = KnightOrderManager.getOrdersForFamily(familyId);
        int orderCount = orders.size();
        ui.set("#FamBtnToggleOrdres.Text", (ordresOpen ? "v" : ">") + " ORDRES DE CHEVALIER (" + orderCount + "/3)");
        ui.set("#FamOrdresContent.Visible", ordresOpen);

        if (ordresOpen) {
            for (int i = 0; i < MAX_ORDRE_CARDS; i++) {
                if (i < orders.size()) {
                    KnightOrder order = orders.get(i);
                    ui.set("#OrdreCard" + i + ".Visible", true);
                    ui.set("#OrdreName" + i + ".Text", order.getName());
                    ui.set("#OrdreMotto" + i + ".Text", "\"" + order.getMotto() + "\"");
                    ui.set("#OrdreMembers" + i + ".Text", order.getMemberCount() + " chevaliers");
                    ui.set("#OrdreStats" + i + ".Text", order.getCapitaineName() + " \u00b7 " + order.getTotalKills() + " kills");
                } else {
                    ui.set("#OrdreCard" + i + ".Visible", false);
                }
            }
        } else {
            for (int i = 0; i < MAX_ORDRE_CARDS; i++) {
                ui.set("#OrdreCard" + i + ".Visible", false);
            }
        }

        // Create button visible if < 3 orders AND (isPatriarch or is CHEVALIER+)
        NobilityRank playerRank = NobilityRank.fromString(rankStr);
        boolean canCreateOrdre = KnightOrderManager.canCreateOrder(familyId)
                && (isPatriarch || (playerRank != null && playerRank.ordinal() >= NobilityRank.CHEVALIER.ordinal()));
        ui.set("#FamBtnCreateOrdre.Visible", ordresOpen && canCreateOrdre);

        // === Invite accordion (Patriarch + Vice only) ===
        boolean canInvite = isPatriarch || isVice;
        ui.set("#FamInviteAccordion.Visible", canInvite);
        if (canInvite) {
            ui.set("#FamBtnToggleInvite.Text", (inviteOpen ? "v" : ">") + " INVITER");
            ui.set("#FamInviteContent.Visible", inviteOpen);

            if (inviteOpen) {
                populateInviteList(ui, data, uuid);
            } else {
                for (int i = 0; i < MAX_INVITE_SLOTS; i++) {
                    ui.set("#FInviteSlot" + i + ".Visible", false);
                }
            }
        }

        // 9. Danger zone
        ui.set("#FamBtnLeave.Visible", !isPatriarch);
        ui.set("#FamBtnDisband.Visible", isPatriarch);
    }

    // ==================== FAMILY CHOICES (NO FAMILY STATE) ====================

    private static void populateFamilyChoices(UICommandBuilder ui, PlayerLevelData data, UUID uuid) {
        NobilityRank rank = NobilityRank.fromString(data.getNobilityRank());
        if (rank == null) rank = NobilityRank.ROTURIER;

        ui.set("#FamChooseTitle.Text", "CHOISIR VOTRE FAMILLE (" + rank.getDisplayName() + ")");

        List<NobleFamilyModel> available = FamilyManager.getAvailableFamiliesForRank(rank);
        List<String> cachedFamilyIds = playerCachedFamilyIds.computeIfAbsent(uuid, k -> new ArrayList<>());
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

    // ==================== MEMBERS ACCORDION ====================

    private static void populateMembers(UICommandBuilder ui, String familyId, UUID viewerUUID, boolean isPatriarch, boolean isVice) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();

        // Build sorted member list: Patriarch first, then Vice, then Members
        List<UUID> patriarchs = new ArrayList<>();
        List<UUID> vices = new ArrayList<>();
        List<UUID> members = new ArrayList<>();

        for (PlayerRef pRef : Universe.get().getPlayers()) {
            try {
                var eRef = pRef.getReference();
                if (eRef == null) continue;
                var s = eRef.getStore();
                PlayerLevelData mData = s.getComponent(eRef, type);
                if (mData == null) continue;
                if (!familyId.equals(mData.getNobleFamilyId())) continue;

                UUID memberUUID = extractUUID(pRef);
                if (memberUUID == null) continue;

                String memberStatus = mData.getStatus();
                if ("PATRIARCH".equals(memberStatus)) {
                    patriarchs.add(memberUUID);
                } else if ("VICE".equals(memberStatus)) {
                    vices.add(memberUUID);
                } else {
                    members.add(memberUUID);
                }
            } catch (Exception e) { EldaniorLogger.error("FamilleTab", e); }
        }

        List<UUID> sortedMembers = new ArrayList<>();
        sortedMembers.addAll(patriarchs);
        sortedMembers.addAll(vices);
        sortedMembers.addAll(members);

        // Cache for action buttons
        playerCachedMembers.put(viewerUUID, sortedMembers);

        // Pagination
        int totalMembers = sortedMembers.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalMembers / MAX_MEMBER_SLOTS));
        int currentPage = memberPages.getOrDefault(viewerUUID, 0);
        if (currentPage >= totalPages) currentPage = totalPages - 1;
        if (currentPage < 0) currentPage = 0;
        memberPages.put(viewerUUID, currentPage);

        int startIdx = currentPage * MAX_MEMBER_SLOTS;

        ui.set("#FMemberPage.Text", (currentPage + 1) + "/" + totalPages);

        for (int i = 0; i < MAX_MEMBER_SLOTS; i++) {
            int memberIdx = startIdx + i;
            if (memberIdx < totalMembers) {
                UUID memberUUID = sortedMembers.get(memberIdx);
                ui.set("#FMemberSlot" + i + ".Visible", true);

                PlayerRef pRef = Universe.get().getPlayer(memberUUID);
                boolean isOnline = pRef != null;

                // Status indicator
                if (isOnline) {
                    ui.set("#FMemberStatus" + i + ".Text", "\u25cf");
                } else {
                    ui.set("#FMemberStatus" + i + ".Text", "\u25cf hors ligne");
                }

                // Get member data
                String memberName = "Inconnu";
                int memberLevel = 0;
                String memberStatus = "MEMBER";

                if (isOnline) {
                    try {
                        var eRef = pRef.getReference();
                        if (eRef != null) {
                            var s = eRef.getStore();
                            PlayerLevelData mData = s.getComponent(eRef, type);
                            if (mData != null) {
                                memberLevel = mData.getLevel();
                                memberStatus = mData.getStatus();
                            }
                        }
                        memberName = pRef.getUsername();
                    } catch (Exception e) { EldaniorLogger.error("FamilleTab", e); }
                } else {
                    if (patriarchs.contains(memberUUID)) memberStatus = "PATRIARCH";
                    else if (vices.contains(memberUUID)) memberStatus = "VICE";
                }

                // Name + level
                String nameDisplay = memberName + (memberLevel > 0 ? " Niv." + memberLevel : "");
                ui.set("#FMemberName" + i + ".Text", nameDisplay);

                // Role display
                String roleLabel = "PATRIARCH".equals(memberStatus) ? "Patriarche" : "VICE".equals(memberStatus) ? "Vice-Patriarche" : "Membre";
                ui.set("#FMemberRole" + i + ".Text", roleLabel);

                if ("PATRIARCH".equals(memberStatus)) {
                } else if ("VICE".equals(memberStatus)) {
                } else {
                }

                // Action buttons (Patriarch only, not on self)
                boolean isSelf = memberUUID.equals(viewerUUID);
                boolean canAct = isPatriarch && !isSelf;
                ui.set("#FMemberBtnPromote" + i + ".Visible", canAct && !"PATRIARCH".equals(memberStatus) && !"VICE".equals(memberStatus));
                ui.set("#FMemberBtnDemote" + i + ".Visible", canAct && "VICE".equals(memberStatus));
                ui.set("#FMemberBtnKick" + i + ".Visible", canAct);
            } else {
                ui.set("#FMemberSlot" + i + ".Visible", false);
            }
        }
    }

    // ==================== ORDER DETAIL VIEW ====================

    private static void populateOrderDetail(UICommandBuilder ui, String orderId, PlayerLevelData data, UUID uuid) {
        KnightOrder order = KnightOrderManager.get(orderId);
        if (order == null) {
            viewingOrderId.remove(uuid);
            return;
        }

        ui.set("#OrdreDetailName.Text", order.getName());
        ui.set("#OrdreDetailMotto.Text", "\"" + order.getMotto() + "\"");
        ui.set("#OrdreDetailCapitaine.Text", order.getCapitaineName());

        String ltName = order.getLieutenantName();
        ui.set("#OrdreDetailLieutenant.Text", ltName != null ? ltName : "Aucun");
        ui.set("#OrdreDetailMemberCount.Text", order.getMemberCount() + "/10");

        String territory = order.getTerritoryId();
        ui.set("#OrdreDetailTerritory.Text", territory != null ? territory : "Non assigne");

        ui.set("#OrdreDetailKills.Text", String.valueOf(order.getTotalKills()));

        // Contribution = kills (serves as ranking metric)
        ui.set("#OrdreDetailMissions.Text", String.valueOf(order.getTotalKills()));

        // Rank = position among all orders sorted by kills (contribution)
        List<KnightOrder> allOrders = new ArrayList<>(KnightOrderManager.getAll());
        allOrders.sort((a, b) -> Integer.compare(b.getTotalKills(), a.getTotalKills()));
        int rank = 1;
        for (KnightOrder o : allOrders) {
            if (o.getId().equals(order.getId())) break;
            rank++;
        }
        ui.set("#OrdreDetailRank.Text", "#" + rank + "/" + allOrders.size());

        // Populate order member list (paginated, 5 slots)
        populateOrdreMembers(ui, order, uuid, data);

        // Invite button visible if player is the Captain
        boolean isCapitaine = order.isCapitaine(uuid);
        ui.set("#OrdreBtnInvite.Visible", isCapitaine);

        // Join button visible if player is NOT in an order and is a member of the family
        boolean canJoin = KnightOrderManager.getPlayerOrder(uuid) == null
                && order.getMemberCount() < 10
                && !isCapitaine;
        ui.set("#OrdreBtnJoin.Visible", canJoin);

        // Disband button visible if player is Patriarch of the family
        ui.set("#OrdreBtnDisband.Visible", data.isPatriarch());
    }

    // ==================== ORDER MEMBERS (PAGINATED) ====================

    private static void populateOrdreMembers(UICommandBuilder ui, KnightOrder order, UUID viewerUUID, PlayerLevelData viewerData) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();

        // Build sorted member list: Capitaine first, then Lieutenant, then members
        List<UUID> capitaines = new ArrayList<>();
        List<UUID> lieutenants = new ArrayList<>();
        List<UUID> membres = new ArrayList<>();

        for (UUID memberUUID : order.getMembers()) {
            KnightOrderRole role = order.getRoleOf(memberUUID);
            if (role == KnightOrderRole.CAPITAINE) {
                capitaines.add(memberUUID);
            } else if (role == KnightOrderRole.LIEUTENANT) {
                lieutenants.add(memberUUID);
            } else {
                membres.add(memberUUID);
            }
        }

        List<UUID> sortedMembers = new ArrayList<>();
        sortedMembers.addAll(capitaines);
        sortedMembers.addAll(lieutenants);
        sortedMembers.addAll(membres);

        // Cache for action buttons
        playerCachedOrdreMembers.put(viewerUUID, sortedMembers);

        // Pagination
        int totalMembers = sortedMembers.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalMembers / MAX_ORDRE_MEMBER_SLOTS));
        int currentPage = ordreMemberPages.getOrDefault(viewerUUID, 0);
        if (currentPage >= totalPages) currentPage = totalPages - 1;
        if (currentPage < 0) currentPage = 0;
        ordreMemberPages.put(viewerUUID, currentPage);

        int startIdx = currentPage * MAX_ORDRE_MEMBER_SLOTS;


        boolean isCapitaine = order.isCapitaine(viewerUUID);

        for (int i = 0; i < MAX_ORDRE_MEMBER_SLOTS; i++) {
            int memberIdx = startIdx + i;
            if (memberIdx < totalMembers) {
                UUID memberUUID = sortedMembers.get(memberIdx);
                ui.set("#OMemberSlot" + i + ".Visible", true);

                PlayerRef pRef = Universe.get().getPlayer(memberUUID);
                boolean isOnline = pRef != null;

                // Status indicator
                if (isOnline) {
                } else {
                }

                // Member data
                String memberName = "Inconnu";
                int memberLevel = 0;

                if (isOnline) {
                    try {
                        var eRef = pRef.getReference();
                        if (eRef != null) {
                            var s = eRef.getStore();
                            PlayerLevelData mData = s.getComponent(eRef, type);
                            if (mData != null) {
                                memberLevel = mData.getLevel();
                            }
                        }
                        memberName = pRef.getUsername();
                    } catch (Exception e) { EldaniorLogger.error("FamilleTab", e); }
                }

                // Name + level
                String nameDisplay = memberName + (memberLevel > 0 ? " Niv." + memberLevel : "");
                ui.set("#OMemberName" + i + ".Text", nameDisplay);

                // Role display
                KnightOrderRole role = order.getRoleOf(memberUUID);
                String roleLabel = role != null ? role.getDisplayName() : "Membre";
                ui.set("#OMemberRole" + i + ".Text", roleLabel);

                String roleColor = role != null ? role.getColorCode() : "#7d8fa3";

                // Action buttons (Captain only, not on self)
                boolean isSelf = memberUUID.equals(viewerUUID);
                boolean canAct = isCapitaine && !isSelf;
                boolean isLt = order.isLieutenant(memberUUID);
                boolean isCap = order.isCapitaine(memberUUID);
                ui.set("#OMemberBtnLt" + i + ".Visible", canAct && !isLt && !isCap);
                ui.set("#OMemberBtnDemote" + i + ".Visible", canAct && isLt);
                ui.set("#OMemberBtnKick" + i + ".Visible", canAct && !isCap);
            } else {
                ui.set("#OMemberSlot" + i + ".Visible", false);
            }
        }
    }

    // ==================== INVITE ACCORDION ====================

    private static void populateInviteList(UICommandBuilder ui, PlayerLevelData ownerData, UUID uuid) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        List<String> cachedInviteNames = playerCachedInviteNames.computeIfAbsent(uuid, k -> new ArrayList<>());
        cachedInviteNames.clear();

        String famId = ownerData.getNobleFamilyId();
        NobleFamilyModel family = FamilyManager.get(famId);
        if (family == null) return;

        // Collect all eligible players
        List<String> eligibleNames = new ArrayList<>();
        for (PlayerRef pRef : Universe.get().getPlayers()) {
            try {
                var eRef = pRef.getReference();
                if (eRef == null) continue;
                var s = eRef.getStore();
                PlayerLevelData d = s.getComponent(eRef, type);
                if (d == null) continue;
                // Must be noble enough, no family yet
                NobilityRank rank = NobilityRank.fromString(d.getNobilityRank());
                if (rank == null || rank.ordinal() < NobilityRank.BARON.ordinal()) continue;
                if (d.getNobleFamilyId() != null && !d.getNobleFamilyId().isEmpty()) continue;
                eligibleNames.add(pRef.getUsername());
            } catch (Exception e) { EldaniorLogger.error("FamilleTab", e); }
        }

        // Pagination
        int totalPlayers = eligibleNames.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalPlayers / MAX_INVITE_SLOTS));
        int currentPage = invitePages.getOrDefault(uuid, 0);
        if (currentPage >= totalPages) currentPage = totalPages - 1;
        if (currentPage < 0) currentPage = 0;
        invitePages.put(uuid, currentPage);

        int startIdx = currentPage * MAX_INVITE_SLOTS;

        ui.set("#FInvitePage.Text", (currentPage + 1) + "/" + totalPages);

        // Cache the visible page for invite action mapping
        for (int i = 0; i < MAX_INVITE_SLOTS; i++) {
            int playerIdx = startIdx + i;
            if (playerIdx < totalPlayers) {
                cachedInviteNames.add(eligibleNames.get(playerIdx));
            }
        }

        for (int i = 0; i < MAX_INVITE_SLOTS; i++) {
            if (i < cachedInviteNames.size()) {
                ui.set("#FInviteSlot" + i + ".Visible", true);
                ui.set("#FInviteName" + i + ".Text", cachedInviteNames.get(i));
                ui.set("#FInviteBtn" + i + ".Text", "INVITER");
            } else {
                ui.set("#FInviteSlot" + i + ".Visible", false);
            }
        }
    }

    // ==================== ACCORDION TOGGLE HANDLERS ====================

    public static void handleToggleMembers(UUID playerUUID) {
        String current = openAccordion.get(playerUUID);
        if ("members".equals(current)) {
            openAccordion.remove(playerUUID);
        } else {
            openAccordion.put(playerUUID, "members");
        }
    }

    public static void handleToggleOrdres(UUID playerUUID) {
        String current = openAccordion.get(playerUUID);
        if ("ordres".equals(current)) {
            openAccordion.remove(playerUUID);
        } else {
            openAccordion.put(playerUUID, "ordres");
        }
    }

    public static void handleToggleInvite(UUID playerUUID) {
        String current = openAccordion.get(playerUUID);
        if ("invite".equals(current)) {
            openAccordion.remove(playerUUID);
        } else {
            openAccordion.put(playerUUID, "invite");
        }
    }

    // ==================== MEMBER PAGINATION ====================

    public static void handleMemberNext(UUID playerUUID) {
        memberPages.merge(playerUUID, 1, Integer::sum);
    }

    public static void handleMemberPrev(UUID playerUUID) {
        memberPages.compute(playerUUID, (k, v) -> v != null && v > 0 ? v - 1 : 0);
    }

    // ==================== INVITE PAGINATION ====================

    public static void handleInviteNext(UUID playerUUID) {
        invitePages.merge(playerUUID, 1, Integer::sum);
    }

    public static void handleInvitePrev(UUID playerUUID) {
        invitePages.compute(playerUUID, (k, v) -> v != null && v > 0 ? v - 1 : 0);
    }

    // ==================== ORDER MEMBER PAGINATION ====================

    public static void handleOrdreMemberNext(UUID playerUUID) {
        ordreMemberPages.merge(playerUUID, 1, Integer::sum);
    }

    public static void handleOrdreMemberPrev(UUID playerUUID) {
        ordreMemberPages.compute(playerUUID, (k, v) -> v != null && v > 0 ? v - 1 : 0);
    }

    // ==================== ORDER DETAIL NAVIGATION ====================

    public static boolean handleOrdreDetail(String slotIndex, UUID uuid) {
        int idx;
        try { idx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return false; }

        // We need the family ID from the cached data — find from online players
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerRef pRef = Universe.get().getPlayer(uuid);
        if (pRef == null) return false;

        String familyId = null;
        try {
            var eRef = pRef.getReference();
            if (eRef != null) {
                var s = eRef.getStore();
                PlayerLevelData data = s.getComponent(eRef, type);
                if (data != null) familyId = data.getNobleFamilyId();
            }
        } catch (Exception e) { EldaniorLogger.error("FamilleTab", e); }

        if (familyId == null || familyId.isEmpty()) return false;

        List<KnightOrder> orders = KnightOrderManager.getOrdersForFamily(familyId);
        if (idx < 0 || idx >= orders.size()) return false;

        viewingOrderId.put(uuid, orders.get(idx).getId());
        ordreMemberPages.put(uuid, 0);
        return true;
    }

    public static boolean handleOrdreBack(UUID uuid) {
        viewingOrderId.remove(uuid);
        return true;
    }

    // ==================== MEMBER ACTION HANDLERS ====================

    public static boolean handlePromote(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int idx;
        try { idx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return false; }

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null || !data.isPatriarch()) return false;

        UUID myUUID = getPlayerUUID(ref, store);
        List<UUID> cached = playerCachedMembers.getOrDefault(myUUID, new ArrayList<>());
        int page = memberPages.getOrDefault(myUUID, 0);
        int memberIdx = page * MAX_MEMBER_SLOTS + idx;
        if (memberIdx < 0 || memberIdx >= cached.size()) return false;

        UUID targetUUID = cached.get(memberIdx);
        if (targetUUID.equals(myUUID)) return false;

        // Find target online and change status to VICE
        PlayerRef targetRef = Universe.get().getPlayer(targetUUID);
        if (targetRef == null) {
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player != null) player.getPlayerRef().sendMessage(Message.raw("Ce joueur est hors ligne."));
            return false;
        }

        try {
            var tRef = targetRef.getReference();
            if (tRef != null) {
                var tStore = tRef.getStore();
                PlayerLevelData tData = tStore.getComponent(tRef, type);
                if (tData != null) {
                    PlayerLevelData tCopy = (PlayerLevelData) tData.clone();
                    if (tCopy != null) { tCopy.setStatus("VICE"); tStore.putComponent(tRef, type, tCopy); }
                }
            }
            targetRef.sendMessage(Message.raw("Vous avez ete promu Vice-Patriarche !"));
        } catch (Exception e) { EldaniorLogger.error("FamilleTab", e); return false; }

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) player.getPlayerRef().sendMessage(Message.raw(targetRef.getUsername() + " promu Vice-Patriarche."));
        return true;
    }

    public static boolean handleDemote(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int idx;
        try { idx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return false; }

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null || !data.isPatriarch()) return false;

        UUID myUUID = getPlayerUUID(ref, store);
        List<UUID> cached = playerCachedMembers.getOrDefault(myUUID, new ArrayList<>());
        int page = memberPages.getOrDefault(myUUID, 0);
        int memberIdx = page * MAX_MEMBER_SLOTS + idx;
        if (memberIdx < 0 || memberIdx >= cached.size()) return false;

        UUID targetUUID = cached.get(memberIdx);
        if (targetUUID.equals(myUUID)) return false;

        PlayerRef targetRef = Universe.get().getPlayer(targetUUID);
        if (targetRef == null) {
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player != null) player.getPlayerRef().sendMessage(Message.raw("Ce joueur est hors ligne."));
            return false;
        }

        try {
            var tRef = targetRef.getReference();
            if (tRef != null) {
                var tStore = tRef.getStore();
                PlayerLevelData tData = tStore.getComponent(tRef, type);
                if (tData != null) {
                    PlayerLevelData tCopy = (PlayerLevelData) tData.clone();
                    if (tCopy != null) { tCopy.setStatus("MEMBER"); tStore.putComponent(tRef, type, tCopy); }
                }
            }
            targetRef.sendMessage(Message.raw("Vous avez ete retrograde au rang de Membre."));
        } catch (Exception e) { EldaniorLogger.error("FamilleTab", e); return false; }

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) player.getPlayerRef().sendMessage(Message.raw(targetRef.getUsername() + " retrograde a Membre."));
        return true;
    }

    public static boolean handleKick(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int idx;
        try { idx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return false; }

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null || !data.isPatriarch()) return false;

        UUID myUUID = getPlayerUUID(ref, store);
        List<UUID> cached = playerCachedMembers.getOrDefault(myUUID, new ArrayList<>());
        int page = memberPages.getOrDefault(myUUID, 0);
        int memberIdx = page * MAX_MEMBER_SLOTS + idx;
        if (memberIdx < 0 || memberIdx >= cached.size()) return false;

        UUID targetUUID = cached.get(memberIdx);
        if (targetUUID.equals(myUUID)) return false;

        // Clear family data for target if online
        PlayerRef targetRef = Universe.get().getPlayer(targetUUID);
        if (targetRef != null) {
            try {
                var tRef = targetRef.getReference();
                if (tRef != null) {
                    var tStore = tRef.getStore();
                    PlayerLevelData tData = tStore.getComponent(tRef, type);
                    if (tData != null) {
                        PlayerLevelData tCopy = (PlayerLevelData) tData.clone();
                        if (tCopy != null) { tCopy.setNobleFamilyId(""); tCopy.setStatus(""); tStore.putComponent(tRef, type, tCopy); }
                    }
                }
                targetRef.sendMessage(Message.raw("Vous avez ete expulse de la famille."));
            } catch (Exception e) { EldaniorLogger.error("FamilleTab", e); }
        }

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            String targetName = targetRef != null ? targetRef.getUsername() : "Joueur";
            player.getPlayerRef().sendMessage(Message.raw(targetName + " a ete expulse de la famille."));
        }
        return true;
    }

    // ==================== ORDER MEMBER ACTION HANDLERS ====================

    public static boolean handleOrdrePromoteLt(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int idx;
        try { idx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return false; }

        UUID myUUID = getPlayerUUID(ref, store);
        String orderId = viewingOrderId.get(myUUID);
        if (orderId == null) return false;

        KnightOrder order = KnightOrderManager.get(orderId);
        if (order == null || !order.isCapitaine(myUUID)) return false;

        List<UUID> cached = playerCachedOrdreMembers.getOrDefault(myUUID, new ArrayList<>());
        int page = ordreMemberPages.getOrDefault(myUUID, 0);
        int memberIdx = page * MAX_ORDRE_MEMBER_SLOTS + idx;
        if (memberIdx < 0 || memberIdx >= cached.size()) return false;

        UUID targetUUID = cached.get(memberIdx);
        if (targetUUID.equals(myUUID)) return false;

        // Get target name
        PlayerRef targetRef = Universe.get().getPlayer(targetUUID);
        String targetName = targetRef != null ? targetRef.getUsername() : targetUUID.toString();

        order.setLieutenant(targetUUID, targetName);

        if (targetRef != null) {
            targetRef.sendMessage(Message.raw("Vous avez ete promu Lieutenant de l'Ordre " + order.getName() + " !"));
        }

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) player.getPlayerRef().sendMessage(Message.raw(targetName + " promu Lieutenant."));
        return true;
    }

    public static boolean handleOrdreDemote(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int idx;
        try { idx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return false; }

        UUID myUUID = getPlayerUUID(ref, store);
        String orderId = viewingOrderId.get(myUUID);
        if (orderId == null) return false;

        KnightOrder order = KnightOrderManager.get(orderId);
        if (order == null || !order.isCapitaine(myUUID)) return false;

        List<UUID> cached = playerCachedOrdreMembers.getOrDefault(myUUID, new ArrayList<>());
        int page = ordreMemberPages.getOrDefault(myUUID, 0);
        int memberIdx = page * MAX_ORDRE_MEMBER_SLOTS + idx;
        if (memberIdx < 0 || memberIdx >= cached.size()) return false;

        UUID targetUUID = cached.get(memberIdx);
        if (!order.isLieutenant(targetUUID)) return false;

        order.clearLieutenant();

        PlayerRef targetRef = Universe.get().getPlayer(targetUUID);
        if (targetRef != null) {
            targetRef.sendMessage(Message.raw("Vous avez ete retrograde au rang de Membre de l'Ordre."));
        }

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            String targetName = targetRef != null ? targetRef.getUsername() : "Joueur";
            player.getPlayerRef().sendMessage(Message.raw(targetName + " retrograde a Membre de l'Ordre."));
        }
        return true;
    }

    public static boolean handleOrdreKick(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int idx;
        try { idx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return false; }

        UUID myUUID = getPlayerUUID(ref, store);
        String orderId = viewingOrderId.get(myUUID);
        if (orderId == null) return false;

        KnightOrder order = KnightOrderManager.get(orderId);
        if (order == null || !order.isCapitaine(myUUID)) return false;

        List<UUID> cached = playerCachedOrdreMembers.getOrDefault(myUUID, new ArrayList<>());
        int page = ordreMemberPages.getOrDefault(myUUID, 0);
        int memberIdx = page * MAX_ORDRE_MEMBER_SLOTS + idx;
        if (memberIdx < 0 || memberIdx >= cached.size()) return false;

        UUID targetUUID = cached.get(memberIdx);
        if (targetUUID.equals(myUUID)) return false;
        if (order.isCapitaine(targetUUID)) return false;

        KnightOrderManager.leaveOrder(targetUUID);

        PlayerRef targetRef = Universe.get().getPlayer(targetUUID);
        if (targetRef != null) {
            targetRef.sendMessage(Message.raw("Vous avez ete expulse de l'Ordre " + order.getName() + "."));
        }

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            String targetName = targetRef != null ? targetRef.getUsername() : "Joueur";
            player.getPlayerRef().sendMessage(Message.raw(targetName + " expulse de l'Ordre."));
        }
        return true;
    }

    // ==================== ORDER MANAGEMENT ====================

    public static boolean handleOrdreDisband(Ref<EntityStore> ref, Store<EntityStore> store) {
        UUID myUUID = getPlayerUUID(ref, store);
        String orderId = viewingOrderId.get(myUUID);
        if (orderId == null) return false;

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null || !data.isPatriarch()) return false;

        KnightOrder order = KnightOrderManager.get(orderId);
        if (order == null) return false;

        String orderName = order.getName();

        // Notify all online members
        for (UUID memberUUID : order.getMembers()) {
            PlayerRef memberRef = Universe.get().getPlayer(memberUUID);
            if (memberRef != null) {
                memberRef.sendMessage(Message.raw("L'Ordre " + orderName + " a ete dissous par le Patriarche."));
            }
        }

        KnightOrderManager.disbandOrder(orderId);
        viewingOrderId.remove(myUUID);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) player.getPlayerRef().sendMessage(Message.raw("L'Ordre " + orderName + " a ete dissous."));
        return true;
    }

    // ==================== JOIN ORDER ====================

    public static boolean handleOrdreJoin(Ref<EntityStore> ref, Store<EntityStore> store) {
        UUID myUUID = getPlayerUUID(ref, store);
        String orderId = viewingOrderId.get(myUUID);
        if (orderId == null) return false;

        KnightOrder order = KnightOrderManager.get(orderId);
        if (order == null) return false;

        // Check player is not already in an order
        if (KnightOrderManager.getPlayerOrder(myUUID) != null) {
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player != null) player.getPlayerRef().sendMessage(Message.raw("Vous etes deja dans un ordre."));
            return false;
        }

        // Check rank >= CHEVALIER
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) return false;

        NobilityRank rank = NobilityRank.fromString(data.getNobilityRank());
        if (rank == null || rank.ordinal() < NobilityRank.CHEVALIER.ordinal()) {
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player != null) player.getPlayerRef().sendMessage(Message.raw("Rang minimum requis : Chevalier."));
            return false;
        }

        if (!KnightOrderManager.joinOrder(myUUID, order)) {
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player != null) player.getPlayerRef().sendMessage(Message.raw("L'ordre est plein (10 membres max)."));
            return false;
        }

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) player.getPlayerRef().sendMessage(Message.raw("Vous avez rejoint l'Ordre " + order.getName() + " !"));

        // Notify captain
        PlayerRef capRef = Universe.get().getPlayer(order.getCapitaineUUID());
        if (capRef != null) {
            String playerName = player != null ? player.getPlayerRef().getUsername() : "Un chevalier";
            capRef.sendMessage(Message.raw(playerName + " a rejoint votre Ordre !"));
        }

        return true;
    }

    // ==================== EXISTING HANDLERS (kept) ====================

    public static boolean handleWithdraw(Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null || !data.isPatriarch()) return false;

        String famId = data.getNobleFamilyId();
        if (famId == null || famId.isEmpty()) return false;

        FamilyManager.FamilyRuntimeData runtime = FamilyManager.getRuntimeData(famId);
        if (!runtime.withdrawTreasury(1000)) return false;

        PlayerLevelData wCopy = (PlayerLevelData) data.clone();
        if (wCopy == null) return false;
        wCopy.addMoney(1000);
        store.putComponent(ref, type, wCopy);
        return true;
    }

    public static boolean handleDeposit(Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) return false;

        String famId = data.getNobleFamilyId();
        if (famId == null || famId.isEmpty()) return false;
        if (data.getMoney() < 1000) return false;

        PlayerLevelData dCopy = (PlayerLevelData) data.clone();
        if (dCopy == null) return false;
        dCopy.removeMoney(1000);
        store.putComponent(ref, type, dCopy);
        FamilyManager.getRuntimeData(famId).addTreasury(1000);
        return true;
    }

    public static boolean handleLeave(Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) return false;

        String famId = data.getNobleFamilyId();
        if (famId == null || famId.isEmpty()) return false;

        // Si Patriarch -> chercher un Vice pour transferer, sinon dissoudre
        if (data.isPatriarch()) {
            // Chercher un Vice-Patriarche pour transferer le role
            PlayerRef viceRef = null;
            UUID viceUUID = null;
            for (PlayerRef pRef : Universe.get().getPlayers()) {
                try {
                    var eRef = pRef.getReference();
                    if (eRef == null) continue;
                    var s = eRef.getStore();
                    PlayerLevelData mData = s.getComponent(eRef, type);
                    if (mData != null && famId.equals(mData.getNobleFamilyId()) && mData.isVicePatriarch()) {
                        viceRef = pRef;
                        viceUUID = UUIDExtractor.getUUID(pRef);
                        break;
                    }
                } catch (Exception e) { /* skip */ }
            }

            if (viceRef != null && viceUUID != null) {
                // Transferer au Vice-Patriarche
                try {
                    var vRef = viceRef.getReference();
                    if (vRef != null) {
                        var vStore = vRef.getStore();
                        PlayerLevelData vData = vStore.getComponent(vRef, type);
                        if (vData != null) {
                            PlayerLevelData vCopy = (PlayerLevelData) vData.clone();
                            if (vCopy != null) { vCopy.setStatus("PATRIARCH"); vStore.putComponent(vRef, type, vCopy); }
                            viceRef.sendMessage(Message.raw("Vous etes maintenant Patriarche de la famille !"));
                            // Transferer la propriete des parcelles au nouveau Patriarche
                            com.eldanior.system.territory.ParcelManager.transferFamilyParcelsOwnership(
                                    famId, viceUUID, viceRef.getUsername());
                        }
                    }
                } catch (Exception e) { EldaniorLogger.error("FamilleTab", e); }

                // Le Patriarch quitte
                PlayerLevelData leaveCopy = (PlayerLevelData) data.clone();
                if (leaveCopy != null) { leaveCopy.setNobleFamilyId(""); leaveCopy.setStatus(""); store.putComponent(ref, type, leaveCopy); }
            } else {
                // Pas de Vice -> dissoudre : expulser tous les membres
                for (PlayerRef pRef : Universe.get().getPlayers()) {
                    try {
                        var eRef = pRef.getReference();
                        if (eRef == null) continue;
                        var s = eRef.getStore();
                        PlayerLevelData mData = s.getComponent(eRef, type);
                        if (mData == null) continue;
                        if (famId.equals(mData.getNobleFamilyId())) {
                            PlayerLevelData mCopy = (PlayerLevelData) mData.clone();
                            if (mCopy != null) { mCopy.setNobleFamilyId(""); mCopy.setStatus(""); s.putComponent(eRef, type, mCopy); }
                            pRef.sendMessage(Message.raw("La famille a ete dissoute par le Patriarch."));
                        }
                    } catch (Exception e) { EldaniorLogger.error("FamilleTab", e); }
                }
                // Remettre les parcelles en propriete famille (sans joueur owner)
                com.eldanior.system.territory.ParcelManager.clearFamilyParcelsOwnership(famId);
                FamilyManager.releaseFamily(famId);
            }
        } else {
            // Membre simple -> juste quitter
            PlayerLevelData simpleCopy = (PlayerLevelData) data.clone();
            if (simpleCopy != null) { simpleCopy.setNobleFamilyId(""); simpleCopy.setStatus(""); store.putComponent(ref, type, simpleCopy); }
        }

        return true;
    }

    public static boolean handleDisband(Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null || !data.isPatriarch()) return false;

        String famId = data.getNobleFamilyId();
        if (famId == null || famId.isEmpty()) return false;

        // Expulser tous les membres online
        for (PlayerRef pRef : Universe.get().getPlayers()) {
            try {
                var eRef = pRef.getReference();
                if (eRef == null) continue;
                var s = eRef.getStore();
                PlayerLevelData mData = s.getComponent(eRef, type);
                if (mData == null) continue;
                if (famId.equals(mData.getNobleFamilyId())) {
                    PlayerLevelData dCopy = (PlayerLevelData) mData.clone();
                    if (dCopy != null) { dCopy.setNobleFamilyId(""); dCopy.setStatus(""); s.putComponent(eRef, type, dCopy); }
                    pRef.sendMessage(Message.raw("La famille a ete dissoute par le Patriarche."));
                }
            } catch (Exception e) { EldaniorLogger.error("FamilleTab", e); }
        }

        // Dissoudre tous les ordres de chevalier de la famille
        List<KnightOrder> orders = KnightOrderManager.getOrdersForFamily(famId);
        for (KnightOrder order : orders) {
            KnightOrderManager.disbandOrder(order.getId());
        }

        // Remettre les parcelles
        com.eldanior.system.territory.ParcelManager.clearFamilyParcelsOwnership(famId);
        FamilyManager.releaseFamily(famId);

        return true;
    }

    public static boolean handleInviteByIndex(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int idx;
        try { idx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return false; }
        UUID uuid = getPlayerUUID(ref, store);
        List<String> cachedInviteNames = playerCachedInviteNames.getOrDefault(uuid, new ArrayList<>());
        if (idx < 0 || idx >= cachedInviteNames.size()) return false;

        String targetName = cachedInviteNames.get(idx);

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null || !data.canInviteToFamily()) return false;

        String famId = data.getNobleFamilyId();
        if (famId == null || famId.isEmpty()) return false;
        NobleFamilyModel family = FamilyManager.get(famId);
        if (family == null) return false;

        PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
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
                PlayerLevelData tCopy = (PlayerLevelData) tData.clone();
                if (tCopy != null) { tCopy.setNobleFamilyId(famId); tCopy.setStatus("MEMBER"); tStore.putComponent(tRef, type, tCopy); }
            }
        }

        targetRef.sendMessage(Message.raw("Vous avez rejoint la famille Von " + family.getDisplayName() + " !"));

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            player.getPlayerRef().sendMessage(Message.raw(targetName + " a rejoint la famille !"));
        }
        return true;
    }

    public static boolean handleChoose(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int idx;
        try { idx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return false; }
        UUID uuid = getPlayerUUID(ref, store);
        List<String> cachedFamilyIds = playerCachedFamilyIds.getOrDefault(uuid, new ArrayList<>());
        if (idx < 0 || idx >= cachedFamilyIds.size()) return false;

        String chosenId = cachedFamilyIds.get(idx);
        NobleFamilyModel family = FamilyManager.get(chosenId);
        if (family == null) return false;

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) return false;

        // Check player doesn't already have a family
        if (data.getNobleFamilyId() != null && !data.getNobleFamilyId().isEmpty()) return false;

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
                        pRef.sendMessage(Message.raw("Pas assez d'Or ! Il faut " + String.format("%,d", price) + " Or pour acheter ce territoire."));
                    }
                } catch (Exception e) { EldaniorLogger.error("FamilleTab", e); }
                return false;
            }
            // Deduire l'argent
            data.removeMoney(price);
        }

        // Assigner la propriete de TOUTES les parcelles de la famille au nouveau Patriarche
        try {
            PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
            if (pRef != null) {
                UUID playerUUID = UUIDExtractor.getUUID(pRef);
                String playerName = pRef.getUsername();
                if (playerUUID != null) {
                    com.eldanior.system.territory.ParcelManager.transferFamilyParcelsOwnership(
                            chosenId, playerUUID, playerName);
                }
            }
        } catch (Exception e) { EldaniorLogger.error("FamilleTab", e); }

        // Claim famille
        PlayerLevelData claimCopy = (PlayerLevelData) data.clone();
        if (claimCopy == null) return false;
        claimCopy.setNobleFamilyId(chosenId);
        claimCopy.setStatus("PATRIARCH");
        store.putComponent(ref, type, claimCopy);
        FamilyManager.claimFamily(chosenId);

        return true;
    }

    // ==================== UTILS ====================

    private static UUID getPlayerUUID(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
        return extractUUID(pRef);
    }

    private static UUID extractUUID(PlayerRef playerRef) {
        if (playerRef == null) return null;
        try { return UUIDExtractor.getUUID(playerRef); } catch (Exception e) { return null; }
    }

    private static String formatPrice(long price) {
        if (price >= 1_000_000_000L) return String.format("%.0fG", price / 1_000_000_000.0);
        if (price >= 1_000_000L) return String.format("%.0fM", price / 1_000_000.0);
        if (price >= 1_000L) return String.format("%.0fK", price / 1_000.0);
        return price + " Or";
    }

    /** Nettoyage memoire quand un joueur se deconnecte */
    public static void cleanupPlayer(UUID uuid) {
        memberPages.remove(uuid);
        invitePages.remove(uuid);
        ordreMemberPages.remove(uuid);
        openAccordion.remove(uuid);
        viewingOrderId.remove(uuid);
        playerCachedMembers.remove(uuid);
        playerCachedInviteNames.remove(uuid);
        playerCachedFamilyIds.remove(uuid);
        playerCachedOrdreMembers.remove(uuid);
    }
}
