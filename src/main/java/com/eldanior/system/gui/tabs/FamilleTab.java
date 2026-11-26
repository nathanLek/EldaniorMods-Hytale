package com.eldanior.system.gui.tabs;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.eldanior.system.titles.nobility.family.FamilyRegistry;
import com.eldanior.system.titles.nobility.family.FamilyRole;
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

    /**
     * Joueurs qui gerent leur famille aupres de l'intendant (SystemScreen en mode intendant, guards/LuoMerchants) : eux seuls
     * voient et utilisent la gestion (promouvoir, retrograder, exclure, inviter, retirer de la tresorerie, dissoudre, creer un
     * ordre). Le menu systeme garde la lecture, le depot, Quitter et le choix d'une famille (decision du 2026-11-09).
     */
    public static final java.util.Set<UUID> STEWARD = java.util.concurrent.ConcurrentHashMap.newKeySet();

    /** Actions de gestion, reservees a la fenetre de l'intendant. */
    /** Categorie ouverte dans la fenetre de l'intendant, par joueur (LISTES par defaut). */
    private static final ConcurrentHashMap<UUID, String> STEWARD_CAT = new ConcurrentHashMap<>();
    public static final String CAT_LISTES = "listes", CAT_ORDRES = "ordres", CAT_GESTION = "gestion", CAT_ACHATS = "achats";

    public static void setCategory(UUID uuid, String cat) {
        if (uuid != null && cat != null) STEWARD_CAT.put(uuid, cat);
    }

    /** Boutique de la reserve de la famille (scripts/guards/gen_intendant.py), null si elle n'existe pas. */
    public static String reserveShop(String familyId) {
        if (familyId == null || familyId.isEmpty()) return null;
        String id = FamilyManager.migrateId(familyId);
        String shop = "Eldanior_Intendant_" + Character.toUpperCase(id.charAt(0)) + id.substring(1);
        return com.hypixel.hytale.builtin.adventure.shop.barter.BarterShopAsset.getAssetMap().getAsset(shop) != null ? shop : null;
    }

    public static final java.util.Set<String> MANAGEMENT_ACTIONS = java.util.Set.of("fam_reserve", "fam_cat_listes", "fam_cat_ordres",
            "fam_cat_gestion", "fam_cat_achats", "fam_withdraw", "fam_invite", "fam_toggle_invite",
            "fam_invite_prev", "fam_invite_next", "fam_promote", "fam_demote", "fam_kick", "fam_disband", "fam_create_ordre");

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
        boolean manage = uuid != null && STEWARD.contains(uuid);
        FamilyRole viewerRole = data.getFamilyRole();
        ui.set("#FamManageHint.Visible", !manage && viewerRole != null && viewerRole.ordinal() >= FamilyRole.VICE.ordinal());

        // 1. Header
        ui.set("#FamName.Text", family.getDisplayName());
        ui.set("#FamMotto.Text", "\"" + family.getMotto() + "\"");

        // 2. Info cards
        ui.set("#FamRarity.Text", family.getRarity().name());
        ui.set("#FamRank.Text", family.getMinimumRank().getDisplayName());
        String status = data.getStatus();
        ui.set("#FamStatus.Text", FamilyRole.labelOf(status));

        // 3. Treasury + Contribution
        FamilyManager.FamilyRuntimeData runtime = FamilyManager.getRuntimeData(familyId);
        ui.set("#FamTreasury.Text", runtime.getTreasury() + " Or");
        ui.set("#FamContribution.Text", String.valueOf(runtime.getContribution()));
        ui.set("#FamBtnWithdraw.Visible", isPatriarch && manage);

        // 4. Competences de famille : les Mouvements (2026-11-12, titles.nobility.family.FamilyMovements)
        var movements = com.eldanior.system.titles.nobility.family.FamilyMovements.of(familyId);
        if (!movements.isEmpty()) {
            ui.set("#FamPassiveName.Text", "Les " + movements.size() + " Mouvements");
            ui.set("#FamPassiveDesc.Text", "Le manuel du " + movements.get(0).title() + " est remis à l'entrée dans la famille ; "
                    + "les suivants s'apprennent auprès du Sensei, selon votre rang martial.");
        } else {
            ui.set("#FamPassiveName.Text", "A venir");
            ui.set("#FamPassiveDesc.Text", "Chaque famille recevra sa propre rune.");
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

        // Fenetre de l'intendant (2026-11-10) : quatre categories au lieu d'une longue page. Listes = fiche de la famille et
        // membres ; Ordres = ordres de chevalier ; Gestions = tresorerie, invitations, dissolution ; Achats = reserve de la
        // famille. Le menu systeme (manage faux) garde la page entiere, sans la barre.
        String cat = manage ? STEWARD_CAT.getOrDefault(uuid, CAT_LISTES) : null;
        ui.set("#FamCatBar.Visible", manage);
        ui.set("#FamCatLine.Visible", manage);
        if (manage) {
            ui.set("#FamCatListes.Text", CAT_LISTES.equals(cat) ? "[ LISTES ]" : "LISTES");
            ui.set("#FamCatOrdres.Text", CAT_ORDRES.equals(cat) ? "[ ORDRES ]" : "ORDRES");
            ui.set("#FamCatGestion.Text", CAT_GESTION.equals(cat) ? "[ GESTIONS ]" : "GESTIONS");
            ui.set("#FamCatAchats.Text", CAT_ACHATS.equals(cat) ? "[ ACHATS ]" : "ACHATS");
            membersOpen = CAT_LISTES.equals(cat);
            ordresOpen = CAT_ORDRES.equals(cat);
            inviteOpen = CAT_GESTION.equals(cat);
        }
        ui.set("#FamSecCards.Visible", !manage || CAT_LISTES.equals(cat));
        ui.set("#FamSecLore.Visible", !manage || CAT_LISTES.equals(cat));
        ui.set("#FamSecMembers.Visible", !manage || CAT_LISTES.equals(cat));
        ui.set("#FamSecOrdres.Visible", !manage || CAT_ORDRES.equals(cat));
        ui.set("#FamSecTreasury.Visible", !manage || CAT_GESTION.equals(cat));
        ui.set("#FamSecInvite.Visible", !manage || CAT_GESTION.equals(cat));
        ui.set("#FamSecDanger.Visible", !manage || CAT_GESTION.equals(cat));
        ui.set("#FamSecAchats.Visible", manage && CAT_ACHATS.equals(cat));
        if (manage && CAT_ACHATS.equals(cat))
            ui.set("#FamAchatsInfo.Text", reserveShop(familyId) != null
                    ? "Bannière, présentoir et tenues de la famille " + family.getDisplayName() + ". Réassort tous les deux jours ; "
                      + "payé avec vos pièces (le paiement par la trésorerie viendra plus tard)."
                    : "La réserve de la famille " + family.getDisplayName() + " est vide pour l'instant.");

        // === Members accordion ===
        populateMembers(ui, familyId, uuid, manage ? data.getFamilyRole() : FamilyRole.MEMBER);
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
        ui.set("#FamBtnCreateOrdre.Visible", ordresOpen && canCreateOrdre && manage);

        // === Invite accordion (Patriarche, Doyens, Seigneur) ===
        boolean canInvite = data.canInviteToFamily() && manage;
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
        ui.set("#FamBtnDisband.Visible", isPatriarch && manage);
    }

    // ==================== FAMILY CHOICES (NO FAMILY STATE) ====================

    private static void populateFamilyChoices(UICommandBuilder ui, PlayerLevelData data, UUID uuid) {
        NobilityRank rank = NobilityRank.fromString(data.getNobilityRank());
        if (rank == null) rank = NobilityRank.ROTURIER;

        ui.set("#FamChooseTitle.Text", rank == NobilityRank.MARQUIS ? "ACHETER UN GRAND TERRITOIRE (Marquis)"
                : "CHOISIR VOTRE FAMILLE (" + rank.getDisplayName() + ")");

        List<NobleFamilyModel> available = FamilyManager.getAvailableFamiliesForRank(rank);
        List<String> cachedFamilyIds = playerCachedFamilyIds.computeIfAbsent(uuid, k -> new ArrayList<>());
        cachedFamilyIds.clear();

        for (int i = 0; i < MAX_FAMILY_SLOTS; i++) {
            if (i < available.size()) {
                NobleFamilyModel fam = available.get(i);
                cachedFamilyIds.add(fam.getId());

                ui.set("#FamChoice" + i + ".Visible", true);
                ui.set("#FamCName" + i + ".Text", fam.getDisplayName());
                ui.set("#FamCMotto" + i + ".Text", "\"" + fam.getMotto() + "\"");
                ui.set("#FamCRarity" + i + ".Text", fam.getRarity().name());
                var land = com.eldanior.system.territory.ParcelManager.getFamilyParcel(fam.getId());   // son grand territoire (2026-11-13)
                ui.set("#FamCPassive" + i + ".Text", land != null ? land.getType().getLabel() + " : " + land.getName().replace('_', ' ') : "");
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

    private static void populateMembers(UICommandBuilder ui, String familyId, UUID viewerUUID, FamilyRole viewerRole) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();

        // Membres connectes, du plus haut rang au plus bas : Patriarche, Doyens, Seigneur, Nobles, membres sans titre
        Map<UUID, FamilyRole> roles = new HashMap<>();
        Map<UUID, String> names = new HashMap<>();
        Map<UUID, Integer> levels = new HashMap<>();
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
                FamilyRole role = mData.getFamilyRole();
                roles.put(memberUUID, role != null ? role : FamilyRole.MEMBER);
                names.put(memberUUID, pRef.getUsername());
                levels.put(memberUUID, mData.getLevel());
            } catch (Exception e) { EldaniorLogger.error("FamilleTab", e); }
        }

        List<UUID> sortedMembers = new ArrayList<>(roles.keySet());
        sortedMembers.sort(Comparator.comparing((UUID u) -> roles.get(u).ordinal()).reversed().thenComparing(names::get));

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
        boolean eldersFull = FamilyManager.getElders(familyId).size() >= FamilyRole.MAX_ELDERS;

        for (int i = 0; i < MAX_MEMBER_SLOTS; i++) {
            int memberIdx = startIdx + i;
            if (memberIdx < totalMembers) {
                UUID memberUUID = sortedMembers.get(memberIdx);
                ui.set("#FMemberSlot" + i + ".Visible", true);
                ui.set("#FMemberStatus" + i + ".Text", "\u25cf");

                FamilyRole role = roles.get(memberUUID);
                int memberLevel = levels.getOrDefault(memberUUID, 0);
                ui.set("#FMemberName" + i + ".Text", names.get(memberUUID) + (memberLevel > 0 ? " Niv." + memberLevel : ""));
                ui.set("#FMemberRole" + i + ".Text", role.label());

                // Boutons : une marche a la fois, selon les droits du rang de celui qui regarde, jamais sur soi
                boolean isSelf = memberUUID.equals(viewerUUID);
                FamilyRole up = role.promoted();
                FamilyRole down = role.demoted();
                boolean canUp = !isSelf && viewerRole != null && viewerRole.canChange(role, up) && !(up == FamilyRole.ELDER && eldersFull);
                boolean canDown = !isSelf && viewerRole != null && viewerRole.canChange(role, down);
                ui.set("#FMemberBtnPromote" + i + ".Visible", canUp);
                if (canUp) ui.set("#FMemberBtnPromote" + i + ".Text", "\u2191 " + up.label().toUpperCase());
                ui.set("#FMemberBtnDemote" + i + ".Visible", canDown);
                ui.set("#FMemberBtnKick" + i + ".Visible", !isSelf && viewerRole == FamilyRole.PATRIARCH);
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
        return changeRole(slotIndex, ref, store, true);
    }

    public static boolean handleDemote(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        return changeRole(slotIndex, ref, store, false);
    }

    /** Fait monter ou descendre d'une marche le membre de la ligne cliquee, si le rang de l'auteur le permet. */
    private static boolean changeRole(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store, boolean up) {
        int idx;
        try { idx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return false; }
        UUID myUUID = getPlayerUUID(ref, store);
        List<UUID> cached = playerCachedMembers.getOrDefault(myUUID, new ArrayList<>());
        int memberIdx = memberPages.getOrDefault(myUUID, 0) * MAX_MEMBER_SLOTS + idx;
        if (memberIdx < 0 || memberIdx >= cached.size()) return false;
        return tell(ref, store, changeRoleOf(cached.get(memberIdx), up, ref, store));
    }

    /** Resultat d'une action de gestion : reussie ou non, message pour celui qui l'a faite. */
    public record Outcome(boolean ok, String message) {
        static Outcome no(String m) { return new Outcome(false, m); }
        static Outcome yes(String m) { return new Outcome(true, m); }
    }

    /** Envoie le message d'une action au joueur (menu systeme) ; true si elle a reussi. */
    private static boolean tell(Ref<EntityStore> ref, Store<EntityStore> store, Outcome o) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null && o.message() != null) player.getPlayerRef().sendMessage(Message.raw(o.message()));
        return o.ok();
    }

    /** Membre connecte de la famille (fenetre de l'intendant, liste des membres). */
    public record Member(UUID uuid, String name, FamilyRole role, int level, String classId) {}

    /** Membres connectes de la famille, du plus haut rang au plus bas, puis par nom. */
    public static List<Member> onlineMembers(String familyId) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        List<Member> out = new ArrayList<>();
        if (familyId == null) return out;
        for (PlayerRef pRef : Universe.get().getPlayers()) {
            try {
                var eRef = pRef.getReference();
                if (eRef == null) continue;
                PlayerLevelData m = eRef.getStore().getComponent(eRef, type);
                if (m == null || !familyId.equals(FamilyManager.migrateId(m.getNobleFamilyId()))) continue;
                UUID id = extractUUID(pRef);
                if (id == null) continue;
                FamilyRole r = m.getFamilyRole() != null ? m.getFamilyRole() : FamilyRole.MEMBER;
                out.add(new Member(id, pRef.getUsername(), r, m.getLevel(), m.getPlayerClassId()));
                com.eldanior.system.titles.nobility.family.FamilyRegistry.touch(familyId, id, pRef.getUsername(), r.code(), m.getLevel(),
                        m.getPlayerClassId());
            } catch (Exception e) { EldaniorLogger.error("FamilleTab", e); }
        }
        out.sort(Comparator.comparing((Member m) -> m.role().ordinal()).reversed().thenComparing(Member::name));
        return out;
    }

    /** Joueurs connectes qu'on peut inviter : Baron ou plus, sans famille. */
    public static List<String> invitableNames() {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        List<String> out = new ArrayList<>();
        for (PlayerRef pRef : Universe.get().getPlayers()) {
            try {
                var eRef = pRef.getReference();
                if (eRef == null) continue;
                PlayerLevelData d = eRef.getStore().getComponent(eRef, type);
                if (d == null) continue;
                NobilityRank rank = NobilityRank.fromString(d.getNobilityRank());
                if (rank == null || rank.ordinal() < NobilityRank.BARON.ordinal()) continue;
                if (d.getNobleFamilyId() != null && !d.getNobleFamilyId().isEmpty()) continue;
                out.add(pRef.getUsername());
            } catch (Exception e) { EldaniorLogger.error("FamilleTab", e); }
        }
        out.sort(String.CASE_INSENSITIVE_ORDER);
        return out;
    }

    /** Fait monter (up) ou descendre d'une marche ce membre connecte, si le rang de l'auteur le permet. */
    public static Outcome changeRoleOf(UUID targetUUID, boolean up, Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
        if (data == null || data.getFamilyRole() == null) return Outcome.no("Vous n'avez pas de rang dans cette famille.");
        return changeRoleOf(data.getNobleFamilyId(), data.getFamilyRole(), getPlayerUUID(ref, store), targetUUID, up);
    }

    /** Meme chose pour la famille famId et un auteur de rang actor (intendant : un admin agit en Patriarche). */
    public static Outcome changeRoleOf(String famId, FamilyRole actor, UUID actorUUID, UUID targetUUID, boolean up) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        if (famId == null || actor == null) return Outcome.no("Vous n'avez pas de rang dans cette famille.");
        if (targetUUID == null || targetUUID.equals(actorUUID)) return Outcome.no("Vous ne pouvez pas changer votre propre rang.");
        PlayerRef targetRef = Universe.get().getPlayer(targetUUID);
        if (targetRef == null || targetRef.getReference() == null) return changeRoleAbsent(famId, actor, targetUUID, up);
        try {
            var tRef = targetRef.getReference();
            var tStore = tRef.getStore();
            PlayerLevelData tData = tStore.getComponent(tRef, type);
            if (tData == null || !famId.equals(FamilyManager.migrateId(tData.getNobleFamilyId()))) return Outcome.no("Ce joueur n'est plus de la famille.");
            FamilyRole from = tData.getFamilyRole() != null ? tData.getFamilyRole() : FamilyRole.MEMBER;
            FamilyRole to = up ? from.promoted() : from.demoted();
            if (to == from || !actor.canChange(from, to)) return Outcome.no("Votre rang ne vous permet pas ce changement.");
            if (to == FamilyRole.ELDER && !FamilyManager.addElder(famId, targetUUID))
                return Outcome.no("La famille compte déjà " + FamilyRole.MAX_ELDERS + " Doyens.");
            if (from == FamilyRole.ELDER) FamilyManager.removeElder(famId, targetUUID);
            PlayerLevelData tCopy = (PlayerLevelData) tData.clone();
            if (tCopy == null) return Outcome.no("Une erreur est survenue.");
            tCopy.setStatus(to.code());
            tStore.putComponent(tRef, type, tCopy);
            String title = to.label();
            targetRef.sendMessage(Message.raw(up ? "Vous etes maintenant " + title + " de la famille !" : "Vous etes retrograde : " + title + "."));
            com.eldanior.system.titles.nobility.family.FamilyRegistry.touch(famId, targetUUID, targetRef.getUsername(), to.code(),
                    tData.getLevel(), tData.getPlayerClassId());
            com.eldanior.system.titles.nobility.family.FamilyRegistry.log(famId, targetRef.getUsername() + (up ? " est élevé au rang de " : " redescend au rang de ") + title);
            return Outcome.yes(targetRef.getUsername() + " est maintenant " + title + ".");
        } catch (Exception e) {
            EldaniorLogger.error("FamilleTab", e);
            return Outcome.no("Une erreur est survenue.");
        }
    }

    /**
     * Rang d'un membre absent (2026-11-18) : lu au registre (rang en attente compris), applique a sa connexion
     * (FamilyRegistry.roleLater, PlayerLoginSystem). Le registre des Doyens est tenu tout de suite.
     */
    private static Outcome changeRoleAbsent(String famId, FamilyRole actor, UUID targetUUID, boolean up) {
        com.eldanior.system.titles.nobility.family.FamilyRegistry.Entry entry = null;
        for (var e : com.eldanior.system.titles.nobility.family.FamilyRegistry.members(famId)) if (e.uuid().equals(targetUUID)) entry = e;
        if (entry == null) return Outcome.no("Ce nom n'est plus au registre de la maison.");
        FamilyRole pending = com.eldanior.system.titles.nobility.family.FamilyRegistry.pendingRole(famId, targetUUID);
        FamilyRole from = pending != null ? pending : FamilyRole.of(entry.role()) != null ? FamilyRole.of(entry.role()) : FamilyRole.MEMBER;
        FamilyRole to = up ? from.promoted() : from.demoted();
        if (to == null || to == from || !actor.canChange(from, to)) return Outcome.no("Votre rang ne vous permet pas ce changement.");
        if (to == FamilyRole.ELDER && !FamilyManager.addElder(famId, targetUUID))
            return Outcome.no("La famille compte déjà " + FamilyRole.MAX_ELDERS + " Doyens.");
        if (from == FamilyRole.ELDER) FamilyManager.removeElder(famId, targetUUID);
        com.eldanior.system.titles.nobility.family.FamilyRegistry.roleLater(famId, targetUUID, to);
        com.eldanior.system.titles.nobility.family.FamilyRegistry.log(famId, entry.name() + (up ? " est élevé au rang de " : " redescend au rang de ")
                + to.label() + " (absent)");
        return Outcome.yes(entry.name() + " sera " + to.label() + " à sa prochaine visite.");
    }

    public static boolean handleKick(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int idx;
        try { idx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return false; }
        UUID myUUID = getPlayerUUID(ref, store);
        List<UUID> cached = playerCachedMembers.getOrDefault(myUUID, new ArrayList<>());
        int memberIdx = memberPages.getOrDefault(myUUID, 0) * MAX_MEMBER_SLOTS + idx;
        if (memberIdx < 0 || memberIdx >= cached.size()) return false;
        return tell(ref, store, kickMember(cached.get(memberIdx), ref, store));
    }

    /** Exclusion d'un membre (Patriarche seul). */
    public static Outcome kickMember(UUID targetUUID, Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
        if (data == null || !data.isPatriarch()) return Outcome.no("Seul le Patriarche peut exclure un membre.");
        return kickMember(data.getNobleFamilyId(), FamilyRole.PATRIARCH, getPlayerUUID(ref, store), targetUUID, null);
    }

    /** Exclusion d'un membre de famId ; absent : appliquee a sa prochaine connexion (FamilyRegistry.kickLater). */
    public static Outcome kickMember(String famId, FamilyRole actor, UUID actorUUID, UUID targetUUID, String targetName) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        if (actor != FamilyRole.PATRIARCH) return Outcome.no("Seul le Patriarche peut exclure un membre.");
        if (targetUUID == null || targetUUID.equals(actorUUID)) return Outcome.no("Vous ne pouvez pas vous exclure.");
        if (roleOf(targetUUID, type) == FamilyRole.PATRIARCH) return Outcome.no("On n'exclut pas un Patriarche.");
        PlayerRef targetRef = Universe.get().getPlayer(targetUUID);
        String name = targetRef != null ? targetRef.getUsername() : targetName != null ? targetName : "Le joueur";
        FamilyManager.removeElder(famId, targetUUID);
        if (targetRef == null) {
            com.eldanior.system.titles.nobility.family.FamilyRegistry.kickLater(famId, targetUUID);
            com.eldanior.system.titles.nobility.family.FamilyRegistry.log(famId, name + " est rayé du registre (absent)");
            return Outcome.yes(name + " est rayé du registre ; il l'apprendra à son retour.");
        }
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
        com.eldanior.system.titles.nobility.family.FamilyRegistry.remove(famId, targetUUID);
        com.eldanior.system.titles.nobility.family.FamilyRegistry.log(famId, name + " est exclu de la maison");
        return Outcome.yes(name + " a été exclu de la famille.");
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

        // Si Patriarche -> transmettre a un Doyen connecte, a defaut au Seigneur ; sinon dissoudre
        if (data.isPatriarch()) {
            PlayerRef viceRef = null;
            UUID viceUUID = null;
            FamilyRole heirRole = null;
            for (PlayerRef pRef : Universe.get().getPlayers()) {
                try {
                    var eRef = pRef.getReference();
                    if (eRef == null) continue;
                    var s = eRef.getStore();
                    PlayerLevelData mData = s.getComponent(eRef, type);
                    if (mData == null || !famId.equals(mData.getNobleFamilyId())) continue;
                    FamilyRole r = mData.getFamilyRole();
                    if ((r == FamilyRole.ELDER || r == FamilyRole.VICE) && (heirRole == null || r.ordinal() > heirRole.ordinal())) {
                        viceRef = pRef;
                        viceUUID = UUIDExtractor.getUUID(pRef);
                        heirRole = r;
                    }
                } catch (Exception e) { /* skip */ }
            }

            if (viceRef != null && viceUUID != null) {
                // Transferer a l'heritier (Doyen, sinon Seigneur)
                try {
                    var vRef = viceRef.getReference();
                    if (vRef != null) {
                        var vStore = vRef.getStore();
                        PlayerLevelData vData = vStore.getComponent(vRef, type);
                        if (vData != null) {
                            PlayerLevelData vCopy = (PlayerLevelData) vData.clone();
                            if (vCopy != null) { vCopy.setStatus(FamilyRole.PATRIARCH.code()); vStore.putComponent(vRef, type, vCopy); }
                            FamilyManager.removeElder(famId, viceUUID);
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
            FamilyManager.removeElder(famId, getPlayerUUID(ref, store));
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
        return tell(ref, store, inviteByName(cachedInviteNames.get(idx), ref, store));
    }

    /** Fait entrer ce joueur connecte dans la famille (Baron ou plus, sans famille). */
    public static Outcome inviteByName(String targetName, Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
        if (data == null || !data.canInviteToFamily()) return Outcome.no("Votre rang ne vous permet pas d'inviter.");
        return inviteByName(data.getNobleFamilyId(), data.getFamilyRole(), targetName);
    }

    public static Outcome inviteByName(String famId, FamilyRole actor, String targetName) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        if (actor == null || !actor.canInvite()) return Outcome.no("Votre rang ne vous permet pas d'inviter.");
        NobleFamilyModel family = famId == null || famId.isEmpty() ? null : FamilyManager.get(famId);
        if (family == null) return Outcome.no("Maison inconnue.");
        PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
        var tRef = targetRef != null ? targetRef.getReference() : null;
        if (tRef == null) return Outcome.no(targetName + " n'est plus en ligne.");
        var tStore = tRef.getStore();
        PlayerLevelData tData = tStore.getComponent(tRef, type);
        if (tData == null) return Outcome.no("Joueur introuvable.");
        NobilityRank tRank = NobilityRank.fromString(tData.getNobilityRank());
        if (tRank == null || tRank.ordinal() < NobilityRank.BARON.ordinal()) return Outcome.no(targetName + " doit être au moins Baron.");
        if (tData.getNobleFamilyId() != null && !tData.getNobleFamilyId().isEmpty()) return Outcome.no(targetName + " a déjà une famille.");
        PlayerLevelData tCopy = (PlayerLevelData) tData.clone();
        if (tCopy == null) return Outcome.no("Une erreur est survenue.");
        tCopy.setNobleFamilyId(family.getId());
        tCopy.setStatus(FamilyRole.MEMBER.code());
        tStore.putComponent(tRef, type, tCopy);
        targetRef.sendMessage(Message.raw("Vous avez rejoint la famille " + family.getDisplayName() + " !"));
        UUID tu = extractUUID(targetRef);
        com.eldanior.system.titles.nobility.family.FamilyRegistry.touch(family.getId(), tu, targetName, FamilyRole.MEMBER.code(),
                tData.getLevel(), tData.getPlayerClassId());
        com.eldanior.system.titles.nobility.family.FamilyRegistry.log(family.getId(), targetName + " entre dans la maison");
        return Outcome.yes(targetName + " a rejoint la famille !");
    }

    /** Depot (deposit) ou retrait (Patriarche) d'un montant a la tresorerie de la famille. */
    public static Outcome treasury(boolean deposit, long amount, Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
        if (data == null) return Outcome.no("Joueur introuvable.");
        return treasury(deposit, amount, data.getNobleFamilyId(), data.isPatriarch() ? FamilyRole.PATRIARCH : data.getFamilyRole(), ref, store);
    }

    /** Depot ou retrait a la tresorerie de famId, avec la bourse de l'auteur (de rang actor). */
    public static Outcome treasury(boolean deposit, long amount, String famId, FamilyRole actor, Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        if (data == null) return Outcome.no("Joueur introuvable.");
        if (famId == null || famId.isEmpty()) return Outcome.no("Maison inconnue.");
        if (amount <= 0) return Outcome.no("Indiquez un montant.");
        String who = pr != null ? pr.getUsername() : "?";
        FamilyManager.FamilyRuntimeData runtime = FamilyManager.getRuntimeData(famId);
        PlayerLevelData copy = (PlayerLevelData) data.clone();
        if (copy == null) return Outcome.no("Une erreur est survenue.");
        if (deposit) {
            if (data.getMoney() < amount) return Outcome.no("Votre bourse ne suffit pas.");
            copy.removeMoney(amount);
            store.putComponent(ref, type, copy);
            runtime.addTreasury(amount);
            com.eldanior.system.titles.nobility.family.FamilyRegistry.log(famId, who + " dépose " + amount + " or aux coffres");
            return Outcome.yes("Déposé à la trésorerie : " + amount + " or.");
        }
        if (actor != FamilyRole.PATRIARCH) return Outcome.no("Seul le Patriarche puise dans la trésorerie.");
        if (!runtime.withdrawTreasury(amount)) return Outcome.no("La trésorerie ne contient pas autant.");
        copy.addMoney(amount);
        store.putComponent(ref, type, copy);
        com.eldanior.system.titles.nobility.family.FamilyRegistry.log(famId, who + " retire " + amount + " or des coffres");
        return Outcome.yes("Retiré de la trésorerie : " + amount + " or.");
    }

    /** Dissolution de famId : membres connectes congedies, ordres dissous, terres rendues, registre vide. */
    public static void disbandFamily(String famId) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        for (PlayerRef pRef : Universe.get().getPlayers()) {
            try {
                var eRef = pRef.getReference();
                if (eRef == null) continue;
                var s = eRef.getStore();
                PlayerLevelData mData = s.getComponent(eRef, type);
                if (mData == null || !famId.equals(FamilyManager.migrateId(mData.getNobleFamilyId()))) continue;
                PlayerLevelData dCopy = (PlayerLevelData) mData.clone();
                if (dCopy != null) { dCopy.setNobleFamilyId(""); dCopy.setStatus(""); s.putComponent(eRef, type, dCopy); }
                pRef.sendMessage(Message.raw("La famille a ete dissoute par le Patriarche."));
            } catch (Exception e) { EldaniorLogger.error("FamilleTab", e); }
        }
        for (KnightOrder order : KnightOrderManager.getOrdersForFamily(famId)) KnightOrderManager.disbandOrder(order.getId());
        com.eldanior.system.territory.ParcelManager.clearFamilyParcelsOwnership(famId);
        FamilyManager.releaseFamily(famId);
        com.eldanior.system.titles.nobility.family.FamilyRegistry.clear(famId);
        com.eldanior.system.titles.nobility.family.FamilyRegistry.log(famId, "La maison est dissoute");
    }

    public static boolean handleChoose(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int idx;
        try { idx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return false; }
        UUID uuid = getPlayerUUID(ref, store);
        List<String> cachedFamilyIds = playerCachedFamilyIds.getOrDefault(uuid, new ArrayList<>());
        if (idx < 0 || idx >= cachedFamilyIds.size()) return false;

        String refused = claim(cachedFamilyIds.get(idx), ref, store);
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        if (pr != null) pr.sendMessage(com.hypixel.hytale.server.core.Message.raw(refused != null ? refused
                : "Vous êtes le Patriarche de la maison " + FamilyManager.get(cachedFamilyIds.get(idx)).getDisplayName() + " et le maître de ses terres."));
        if (refused == null) FamilyRegistry.log(cachedFamilyIds.get(idx), (pr != null ? pr.getUsername() : "?") + " achète les terres et prend la tête de la maison");
        return refused == null;
    }

    /**
     * Prendre la tete d'une maison libre : rang suffisant, sans famille, territoire de la maison paye ; le joueur en devient le
     * Patriarche et recoit ses parcelles. Retourne le refus, ou null si c'est fait. Utilise par l'intendant (2026-11-12 : tout ce
     * qui touche a la famille passe par lui).
     */
    public static String claim(String chosenId, Ref<EntityStore> ref, Store<EntityStore> store) {
        NobleFamilyModel family = FamilyManager.get(chosenId);
        if (family == null) return "Cette maison n'existe pas.";
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) return "Données introuvables.";
        if (data.getNobleFamilyId() != null && !data.getNobleFamilyId().isEmpty()) return "Vous appartenez déjà à une maison.";
        NobilityRank rank = NobilityRank.fromString(data.getNobilityRank());
        if (rank == null || rank.ordinal() < family.getMinimumRank().ordinal())
            return "Il faut être au moins " + family.getMinimumRank().getDisplayName() + " pour prendre la tête de cette maison.";
        if (FamilyManager.isFamilyTaken(chosenId)) return "Cette maison a déjà un Patriarche.";
        long price = com.eldanior.system.territory.ParcelManager.getFamilyTerritoryPrice(chosenId);
        PlayerLevelData claimCopy = (PlayerLevelData) data.clone();
        if (claimCopy == null) return "Données introuvables.";
        if (price > 0) {
            if (claimCopy.getMoney() < price) return "Il faut " + String.format("%,d", price) + " or sur votre compte pour acheter les terres de la maison.";
            claimCopy.removeMoney(price);
        }
        try {
            PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
            if (pRef != null) {
                UUID playerUUID = UUIDExtractor.getUUID(pRef);
                if (playerUUID != null)
                    com.eldanior.system.territory.ParcelManager.transferFamilyParcelsOwnership(chosenId, playerUUID, pRef.getUsername());
            }
        } catch (Exception e) { EldaniorLogger.error("FamilleTab", e); }
        claimCopy.setNobleFamilyId(chosenId);
        claimCopy.setStatus(FamilyRole.PATRIARCH.code());
        store.putComponent(ref, type, claimCopy);
        FamilyManager.claimFamily(chosenId);
        return null;
    }

    // ==================== UTILS ====================

    /** Rang d'un joueur connecte, null s'il est hors ligne ou sans famille. */
    private static FamilyRole roleOf(UUID uuid, ComponentType<EntityStore, PlayerLevelData> type) {
        PlayerRef pRef = Universe.get().getPlayer(uuid);
        if (pRef == null || pRef.getReference() == null) return null;
        PlayerLevelData d = pRef.getReference().getStore().getComponent(pRef.getReference(), type);
        return d == null ? null : d.getFamilyRole();
    }

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
