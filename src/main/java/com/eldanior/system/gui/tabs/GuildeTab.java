package com.eldanior.system.gui.tabs;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.guild.Guild;
import com.eldanior.system.guild.GuildManager;
import com.eldanior.system.guild.GuildRole;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.config.EldaniorLogger;
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

public class GuildeTab {

    public static final int MAX_MEMBER_SLOTS = 5;
    public static final int MAX_INVITE_SLOTS = 10;
    public static final int MAX_OPEN_GUILD_SLOTS = 5;

    // Per-player pagination state
    private static final ConcurrentHashMap<UUID, Integer> memberPages = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Integer> invitePages = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Integer> openGuildPages = new ConcurrentHashMap<>();

    // Accordion state: "members" or "invite" — one open at a time, null = all closed
    private static final ConcurrentHashMap<UUID, String> openAccordion = new ConcurrentHashMap<>();

    // Cached data for slot -> actual index mapping
    private static final ConcurrentHashMap<UUID, List<UUID>> playerCachedMembers = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, List<String>> playerCachedInviteNames = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, List<String>> playerCachedOpenGuilds = new ConcurrentHashMap<>();

    // ==================== POPULATE ====================

    public static void populate(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) return;

        UUID playerUUID = getPlayerUUID(ref, store);
        boolean hasGuild = data.hasGuild();
        boolean isNoble = data.getNobleFamilyId() != null && !data.getNobleFamilyId().isEmpty();

        // 3 etats: noble bloque / pas de guilde / a une guilde
        ui.set("#GuildeNobleBlock.Visible", !hasGuild && isNoble);
        ui.set("#GuildeNoGuild.Visible", !hasGuild && !isNoble);
        ui.set("#GuildeHasGuild.Visible", hasGuild);

        if (!hasGuild) {
            if (!isNoble) {
                populateOpenGuilds(ui, playerUUID);
            }
            return;
        }

        Guild guild = GuildManager.getPlayerGuild(playerUUID);
        if (guild == null) return;

        boolean isChef = data.isGuildChef();
        boolean isOfficer = data.isGuildOfficer();

        // Header
        ui.set("#GuildeName.Text", guild.getName() + " " + guild.getFormattedTag());

        // Recruit status
        if (guild.isOpenRecruitment()) {
            ui.set("#GuildeRecruitStatus.Text", "Recrutement : Ouvert");
        } else {
            ui.set("#GuildeRecruitStatus.Text", "Recrutement : Ferme");
        }

        // Info cards
        ui.set("#GuildeFounder.Text", guild.getFounderName());
        String role = data.getGuildRole();
        String roleDisplay = "CHEF".equals(role) ? "Chef" : "OFFICER".equals(role) ? "Officier" : "Membre";
        ui.set("#GuildeRole.Text", roleDisplay);
        ui.set("#GuildeMemberCount.Text", String.valueOf(guild.getMemberCount()));

        // Treasury
        ui.set("#GuildeTreasury.Text", guild.getTreasury() + " Or");
        ui.set("#GuildeContribution.Text", String.valueOf(guild.getContribution()));
        ui.set("#GuildeBtnWithdraw.Visible", isChef);

        // Stats
        ui.set("#GuildeStatMobs.Text", String.valueOf(guild.getTotalMobKills()));
        ui.set("#GuildeStatPvP.Text", String.valueOf(guild.getTotalPlayerKills()));
        ui.set("#GuildeStatDeaths.Text", String.valueOf(guild.getTotalDeaths()));

        // Accordion state
        String currentAccordion = openAccordion.get(playerUUID);
        boolean membersOpen = "members".equals(currentAccordion);
        boolean inviteOpen = "invite".equals(currentAccordion);

        // Members accordion
        int memberCount = guild.getMemberCount();
        ui.set("#GuildeBtnToggleMembers.Text", (membersOpen ? "v" : ">") + " MEMBRES (" + memberCount + ")");
        ui.set("#GuildeMembersContent.Visible", membersOpen);

        if (membersOpen) {
            populateMembers(ui, guild, playerUUID, isChef, isOfficer);
        } else {
            // Hide all slots when collapsed
            for (int i = 0; i < MAX_MEMBER_SLOTS; i++) {
                ui.set("#GMemberSlot" + i + ".Visible", false);
            }
        }

        // Invite accordion (Chef or Officer only)
        ui.set("#GuildeInviteAccordion.Visible", isChef || isOfficer);
        if (isChef || isOfficer) {
            ui.set("#GuildeBtnToggleInvite.Text", (inviteOpen ? "v" : ">") + " INVITER");
            ui.set("#GuildeInviteContent.Visible", inviteOpen);

            if (inviteOpen) {
                populateInviteList(ui, guild, playerUUID);
            } else {
                for (int i = 0; i < MAX_INVITE_SLOTS; i++) {
                    ui.set("#GInviteSlot" + i + ".Visible", false);
                }
            }
        }

        // Recruitment section (Chef only)
        ui.set("#GuildeRecruitmentSection.Visible", isChef);
        if (isChef) {
            ui.set("#GuildeRecruitmentStatus.Text", guild.isOpenRecruitment() ? "Recrutement : Ouvert" : "Recrutement : Ferme");
        }

        // Danger zone
        ui.set("#GuildeBtnDisband.Visible", isChef);
    }

    // ==================== MEMBERS ACCORDION ====================

    private static void populateMembers(UICommandBuilder ui, Guild guild, UUID viewerUUID, boolean isChef, boolean isOfficer) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();

        // Build sorted member list: Chef first, then Officers, then Members
        List<UUID> chefs = new ArrayList<>();
        List<UUID> officers = new ArrayList<>();
        List<UUID> members = new ArrayList<>();

        for (UUID memberUUID : guild.getMembers()) {
            PlayerRef pRef = Universe.get().getPlayer(memberUUID);
            String memberRole = "MEMBER";

            if (pRef != null) {
                try {
                    var eRef = pRef.getReference();
                    if (eRef != null) {
                        var s = eRef.getStore();
                        PlayerLevelData mData = s.getComponent(eRef, type);
                        if (mData != null) {
                            memberRole = mData.getGuildRole();
                        }
                    }
                } catch (Exception e) { EldaniorLogger.error("GuildeTab", e); }
            }

            if ("CHEF".equals(memberRole)) {
                chefs.add(memberUUID);
            } else if ("OFFICER".equals(memberRole)) {
                officers.add(memberUUID);
            } else {
                members.add(memberUUID);
            }
        }

        List<UUID> sortedMembers = new ArrayList<>();
        sortedMembers.addAll(chefs);
        sortedMembers.addAll(officers);
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

        ui.set("#GMemberPage.Text", (currentPage + 1) + "/" + totalPages);

        for (int i = 0; i < MAX_MEMBER_SLOTS; i++) {
            int memberIdx = startIdx + i;
            if (memberIdx < totalMembers) {
                UUID memberUUID = sortedMembers.get(memberIdx);
                ui.set("#GMemberSlot" + i + ".Visible", true);

                PlayerRef pRef = Universe.get().getPlayer(memberUUID);
                boolean isOnline = pRef != null;

                // Status indicator
                if (isOnline) {
                    ui.set("#GMemberStatus" + i + ".Text", "\u25cf");
                } else {
                    ui.set("#GMemberStatus" + i + ".Text", "\u25cf hors ligne");
                }

                // Get member data
                String memberName = "Inconnu";
                int memberLevel = 0;
                String memberRole = "MEMBER";

                if (isOnline) {
                    try {
                        var eRef = pRef.getReference();
                        if (eRef != null) {
                            var s = eRef.getStore();
                            PlayerLevelData mData = s.getComponent(eRef, type);
                            if (mData != null) {
                                memberLevel = mData.getLevel();
                                memberRole = mData.getGuildRole();
                            }
                        }
                        memberName = pRef.getUsername();
                    } catch (Exception e) { EldaniorLogger.error("GuildeTab", e); }
                } else {
                    // Offline: try to determine role from sorted position
                    if (chefs.contains(memberUUID)) memberRole = "CHEF";
                    else if (officers.contains(memberUUID)) memberRole = "OFFICER";
                }

                // Name + level
                String nameDisplay = memberName + (memberLevel > 0 ? " Niv." + memberLevel : "");
                ui.set("#GMemberName" + i + ".Text", nameDisplay);

                // Role display
                GuildRole guildRole = GuildRole.fromString(memberRole);
                String roleLabel = guildRole != null ? guildRole.getDisplayName() : "Membre";
                ui.set("#GMemberRole" + i + ".Text", roleLabel);

                if ("CHEF".equals(memberRole)) {
                } else if ("OFFICER".equals(memberRole)) {
                } else {
                }

                // Contribution (guild-level, shown per member slot — same value)
                ui.set("#GMemberContrib" + i + ".Text", String.valueOf(guild.getContribution()));

                // Action buttons (Chef only, not on self)
                boolean isSelf = memberUUID.equals(viewerUUID);
                boolean canAct = isChef && !isSelf;
                ui.set("#GMemberBtnPromote" + i + ".Visible", canAct && !"CHEF".equals(memberRole));
                ui.set("#GMemberBtnDemote" + i + ".Visible", canAct && "OFFICER".equals(memberRole));
                ui.set("#GMemberBtnKick" + i + ".Visible", canAct);

            } else {
                ui.set("#GMemberSlot" + i + ".Visible", false);
            }
        }
    }

    // ==================== INVITE ACCORDION ====================

    private static void populateInviteList(UICommandBuilder ui, Guild guild, UUID myUUID) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        List<String> cachedInviteNames = playerCachedInviteNames.computeIfAbsent(myUUID, k -> new ArrayList<>());
        cachedInviteNames.clear();

        List<PlayerRef> allPlayers = new ArrayList<>(Universe.get().getPlayers());
        Set<UUID> memberUUIDs = guild.getMembers();

        // Collect all eligible players
        List<String> eligibleNames = new ArrayList<>();
        for (PlayerRef pRef : allPlayers) {
            UUID pUUID = extractUUID(pRef);
            if (pUUID == null || memberUUIDs.contains(pUUID)) continue;

            try {
                var eRef = pRef.getReference();
                if (eRef != null) {
                    var s = eRef.getStore();
                    PlayerLevelData d = s.getComponent(eRef, type);
                    if (d != null && !d.canJoinGuild()) continue;
                }
            } catch (Exception e) { EldaniorLogger.error("GuildeTab", e); }

            eligibleNames.add(pRef.getUsername());
        }

        // Pagination
        int totalPlayers = eligibleNames.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalPlayers / MAX_INVITE_SLOTS));
        int currentPage = invitePages.getOrDefault(myUUID, 0);
        if (currentPage >= totalPages) currentPage = totalPages - 1;
        if (currentPage < 0) currentPage = 0;
        invitePages.put(myUUID, currentPage);

        int startIdx = currentPage * MAX_INVITE_SLOTS;

        // Cache the visible page for invite action mapping
        for (int i = 0; i < MAX_INVITE_SLOTS; i++) {
            int playerIdx = startIdx + i;
            if (playerIdx < totalPlayers) {
                cachedInviteNames.add(eligibleNames.get(playerIdx));
            }
        }

        for (int i = 0; i < MAX_INVITE_SLOTS; i++) {
            if (i < cachedInviteNames.size()) {
                ui.set("#GInviteSlot" + i + ".Visible", true);
                ui.set("#GInviteName" + i + ".Text", cachedInviteNames.get(i));
                ui.set("#GInviteBtn" + i + ".Text", "INVITER");
            } else {
                ui.set("#GInviteSlot" + i + ".Visible", false);
            }
        }
    }

    // ==================== OPEN GUILDS (NO GUILD STATE) ====================

    private static void populateOpenGuilds(UICommandBuilder ui, UUID myUUID) {
        List<Guild> openGuilds = GuildManager.getOpenGuilds();
        List<String> cachedIds = playerCachedOpenGuilds.computeIfAbsent(myUUID, k -> new ArrayList<>());
        cachedIds.clear();

        // Pagination
        int totalGuilds = openGuilds.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalGuilds / MAX_OPEN_GUILD_SLOTS));
        int currentPage = openGuildPages.getOrDefault(myUUID, 0);
        if (currentPage >= totalPages) currentPage = totalPages - 1;
        if (currentPage < 0) currentPage = 0;
        openGuildPages.put(myUUID, currentPage);

        int startIdx = currentPage * MAX_OPEN_GUILD_SLOTS;

        ui.set("#GOpenPage.Text", (currentPage + 1) + "/" + totalPages);

        for (int i = 0; i < MAX_OPEN_GUILD_SLOTS; i++) {
            int guildIdx = startIdx + i;
            if (guildIdx < totalGuilds) {
                Guild g = openGuilds.get(guildIdx);
                cachedIds.add(g.getId());
                ui.set("#GOpenGuild" + i + ".Visible", true);
                ui.set("#GOpenGuildName" + i + ".Text", g.getName() + " " + g.getFormattedTag());
                ui.set("#GOpenGuildMembers" + i + ".Text", g.getMemberCount() + " membres");
                ui.set("#GOpenGuildBtn" + i + ".Text", "REJOINDRE");
            } else {
                ui.set("#GOpenGuild" + i + ".Visible", false);
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

    // ==================== OPEN GUILDS PAGINATION ====================

    public static void handleOpenNext(UUID playerUUID) {
        openGuildPages.merge(playerUUID, 1, Integer::sum);
    }

    public static void handleOpenPrev(UUID playerUUID) {
        openGuildPages.compute(playerUUID, (k, v) -> v != null && v > 0 ? v - 1 : 0);
    }

    // ==================== MEMBER ACTION HANDLERS ====================

    public static boolean handlePromote(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int idx;
        try { idx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return false; }

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null || !data.isGuildChef()) return false;

        UUID myUUID = getPlayerUUID(ref, store);
        List<UUID> cached = playerCachedMembers.getOrDefault(myUUID, new ArrayList<>());
        int page = memberPages.getOrDefault(myUUID, 0);
        int memberIdx = page * MAX_MEMBER_SLOTS + idx;
        if (memberIdx < 0 || memberIdx >= cached.size()) return false;

        UUID targetUUID = cached.get(memberIdx);
        if (targetUUID.equals(myUUID)) return false;

        // Find target online and change role to OFFICER
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
                    if (tCopy != null) {
                        tCopy.setGuildRole("OFFICER");
                        tStore.putComponent(tRef, type, tCopy);
                    }
                }
            }
            targetRef.sendMessage(Message.raw("Vous avez ete promu Officier !"));
        } catch (Exception e) { EldaniorLogger.error("GuildeTab", e); return false; }

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) player.getPlayerRef().sendMessage(Message.raw(targetRef.getUsername() + " promu Officier."));
        return true;
    }

    public static boolean handleDemote(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int idx;
        try { idx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return false; }

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null || !data.isGuildChef()) return false;

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
                    if (tCopy != null) {
                        tCopy.setGuildRole("MEMBER");
                        tStore.putComponent(tRef, type, tCopy);
                    }
                }
            }
            targetRef.sendMessage(Message.raw("Vous avez ete retrograde au rang de Membre."));
        } catch (Exception e) { EldaniorLogger.error("GuildeTab", e); return false; }

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) player.getPlayerRef().sendMessage(Message.raw(targetRef.getUsername() + " retrograde a Membre."));
        return true;
    }

    public static boolean handleKick(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int idx;
        try { idx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return false; }

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null || !data.isGuildChef()) return false;

        UUID myUUID = getPlayerUUID(ref, store);
        List<UUID> cached = playerCachedMembers.getOrDefault(myUUID, new ArrayList<>());
        int page = memberPages.getOrDefault(myUUID, 0);
        int memberIdx = page * MAX_MEMBER_SLOTS + idx;
        if (memberIdx < 0 || memberIdx >= cached.size()) return false;

        UUID targetUUID = cached.get(memberIdx);
        if (targetUUID.equals(myUUID)) return false;

        // Clear guild data for target if online
        PlayerRef targetRef = Universe.get().getPlayer(targetUUID);
        if (targetRef != null) {
            try {
                var tRef = targetRef.getReference();
                if (tRef != null) {
                    var tStore = tRef.getStore();
                    PlayerLevelData tData = tStore.getComponent(tRef, type);
                    if (tData != null) {
                        PlayerLevelData tCopy = (PlayerLevelData) tData.clone();
                        if (tCopy != null) {
                            tCopy.setGuildId("");
                            tCopy.setGuildRole("");
                            tStore.putComponent(tRef, type, tCopy);
                        }
                    }
                }
                targetRef.sendMessage(Message.raw("Vous avez ete expulse de la guilde."));
            } catch (Exception e) { EldaniorLogger.error("GuildeTab", e); }
        }

        // Remove from guild
        GuildManager.leaveGuild(targetUUID);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            String targetName = targetRef != null ? targetRef.getUsername() : "Joueur";
            player.getPlayerRef().sendMessage(Message.raw(targetName + " a ete expulse de la guilde."));
        }
        return true;
    }

    // ==================== EXISTING HANDLERS (kept) ====================

    public static boolean handleLeave(Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null || !data.hasGuild()) return false;

        UUID uuid = getPlayerUUID(ref, store);

        if (data.isGuildChef()) {
            Guild guild = GuildManager.getPlayerGuild(uuid);
            if (guild != null && guild.getMemberCount() > 1) {
                Player player = store.getComponent(ref, Player.getComponentType());
                if (player != null) {
                    player.getPlayerRef().sendMessage(Message.raw("Transferez le role de Chef ou dissolvez la guilde."));
                }
                return false;
            }
            if (guild != null) GuildManager.disbandGuild(guild.getId());
        }

        PlayerLevelData copy = (PlayerLevelData) data.clone();
        if (copy == null) return false;
        copy.setGuildId("");
        copy.setGuildRole("");
        store.putComponent(ref, type, copy);

        GuildManager.leaveGuild(uuid);
        return true;
    }

    public static boolean handleDisband(Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null || !data.isGuildChef()) return false;

        UUID uuid = getPlayerUUID(ref, store);
        Guild guild = GuildManager.getPlayerGuild(uuid);
        if (guild == null) return false;

        for (UUID memberUUID : new HashSet<>(guild.getMembers())) {
            PlayerRef memberRef = Universe.get().getPlayer(memberUUID);
            if (memberRef != null) {
                var mRef = memberRef.getReference();
                if (mRef != null) {
                    var mStore = mRef.getStore();
                    PlayerLevelData mData = mStore.getComponent(mRef, type);
                    if (mData != null) {
                        PlayerLevelData mCopy = (PlayerLevelData) mData.clone();
                        if (mCopy != null) {
                            mCopy.setGuildId("");
                            mCopy.setGuildRole("");
                            mStore.putComponent(mRef, type, mCopy);
                        }
                    }
                }
                memberRef.sendMessage(Message.raw("La guilde a ete dissoute."));
            }
        }

        GuildManager.disbandGuild(guild.getId());
        return true;
    }

    public static boolean handleWithdraw(Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null || !data.isGuildChef()) return false;

        UUID uuid = getPlayerUUID(ref, store);
        Guild guild = GuildManager.getPlayerGuild(uuid);
        if (guild == null || !guild.withdrawTreasury(1000)) return false;

        data.addMoney(1000);
        store.putComponent(ref, type, data);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) player.getPlayerRef().sendMessage(Message.raw("-1000 Or retire de la tresorerie !"));
        return true;
    }

    public static boolean handleDeposit(Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null || !data.hasGuild()) return false;
        if (data.getMoney() < 1000) return false;

        UUID uuid = getPlayerUUID(ref, store);
        Guild guild = GuildManager.getPlayerGuild(uuid);
        if (guild == null) return false;

        data.removeMoney(1000);
        store.putComponent(ref, type, data);
        guild.addTreasury(1000);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) player.getPlayerRef().sendMessage(Message.raw("+1000 Or depose dans la tresorerie !"));
        return true;
    }

    public static boolean handleInviteByIndex(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int idx;
        try { idx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return false; }
        UUID myUUID = getPlayerUUID(ref, store);
        List<String> cachedInviteNames = playerCachedInviteNames.getOrDefault(myUUID, new ArrayList<>());
        if (idx < 0 || idx >= cachedInviteNames.size()) return false;

        String targetName = cachedInviteNames.get(idx);

        UUID uuid = getPlayerUUID(ref, store);
        if (uuid == null) return false;

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null || !data.canInviteToGuild()) return false;

        Guild guild = GuildManager.getPlayerGuild(uuid);
        if (guild == null) return false;

        PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
        if (targetRef == null) return false;

        UUID targetUUID = extractUUID(targetRef);
        if (targetUUID == null) return false;

        // Check target can join
        var tRef = targetRef.getReference();
        if (tRef != null) {
            var tStore = tRef.getStore();
            PlayerLevelData tData = tStore.getComponent(tRef, type);
            if (tData != null && !tData.canJoinGuild()) return false;
        }

        GuildManager.sendInvite(targetUUID, uuid);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            player.getPlayerRef().sendMessage(Message.raw("Invitation envoyee a " + targetName));
        }
        targetRef.sendMessage(Message.raw("Vous etes invite a rejoindre la guilde " + guild.getFormattedName()));
        targetRef.sendMessage(Message.raw("Tapez /es guild accept _ pour accepter."));
        return true;
    }

    public static boolean handleToggleRecruitment(Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null || !data.isGuildChef()) return false;

        UUID uuid = getPlayerUUID(ref, store);
        Guild guild = GuildManager.getPlayerGuild(uuid);
        if (guild == null) return false;

        guild.setOpenRecruitment(!guild.isOpenRecruitment());

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            if (guild.isOpenRecruitment()) {
                player.getPlayerRef().sendMessage(Message.raw("Recrutement ouvert !"));
            } else {
                player.getPlayerRef().sendMessage(Message.raw("Recrutement ferme."));
            }
        }
        return true;
    }

    public static boolean handleJoinByIndex(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int idx;
        try { idx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return false; }
        UUID myUUID = getPlayerUUID(ref, store);
        List<String> cachedGuilds = playerCachedOpenGuilds.getOrDefault(myUUID, new ArrayList<>());
        if (idx < 0 || idx >= cachedGuilds.size()) return false;

        String guildId = cachedGuilds.get(idx);
        Guild guild = GuildManager.get(guildId);
        if (guild == null || !guild.isOpenRecruitment()) return false;

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null || !data.canJoinGuild()) return false;

        PlayerLevelData copy = (PlayerLevelData) data.clone();
        if (copy == null) return false;
        copy.setGuildId(guild.getId());
        copy.setGuildRole("MEMBER");
        store.putComponent(ref, type, copy);

        GuildManager.joinGuild(myUUID, guild);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            com.eldanior.system.titles.TitleManager.checkAndUnlockTitles(ref, store, copy, player.getPlayerRef());
            player.getPlayerRef().sendMessage(Message.raw("Vous avez rejoint la guilde " + guild.getFormattedName() + " !"));
        }
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
}
