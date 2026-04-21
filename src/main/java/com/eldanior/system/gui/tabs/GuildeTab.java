package com.eldanior.system.gui.tabs;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.guild.Guild;
import com.eldanior.system.guild.GuildManager;
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

import java.lang.reflect.Field;
import java.util.*;

public class GuildeTab {

    public static final int MAX_INVITE_SLOTS = 10;
    private static final List<String> cachedInviteNames = new ArrayList<>();

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

        if (!hasGuild) return;

        Guild guild = GuildManager.getPlayerGuild(playerUUID);
        if (guild == null) return;

        boolean isChef = data.isGuildChef();
        boolean isOfficer = data.isGuildOfficer();

        // Header
        ui.set("#GuildeName.Text", guild.getName() + "  " + guild.getFormattedTag());
        ui.set("#GuildeFounder.Text", "Fondateur : " + guild.getFounderName());
        ui.set("#GuildeMemberCount.Text", "Membres : " + guild.getMemberCount());

        // Role
        String role = data.getGuildRole();
        String roleDisplay = "CHEF".equals(role) ? "Chef" : "OFFICER".equals(role) ? "Officier" : "Membre";
        ui.set("#GuildeRole.Text", "Role : " + roleDisplay);

        // Stats
        ui.set("#GuildeStatMobs.Text", String.valueOf(guild.getTotalMobKills()));
        ui.set("#GuildeStatPvP.Text", String.valueOf(guild.getTotalPlayerKills()));
        ui.set("#GuildeStatDeaths.Text", String.valueOf(guild.getTotalDeaths()));

        // Chef actions
        ui.set("#GuildeChefActions.Visible", isChef);
        ui.set("#GuildeInviteSection.Visible", isChef || isOfficer);

        // Invite list (chef + officer)
        if (isChef || isOfficer) {
            populateInviteList(ui, guild, playerUUID);
        } else {
            for (int i = 0; i < MAX_INVITE_SLOTS; i++) {
                ui.set("#GInvitePlayer" + i + ".Visible", false);
            }
        }
    }

    public static boolean handleLeave(Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null || !data.hasGuild()) return false;

        UUID uuid = getPlayerUUID(ref, store);

        if (data.isGuildChef()) {
            Guild guild = GuildManager.getPlayerGuild(uuid);
            if (guild != null && guild.getMemberCount() > 1) {
                // Can't leave as chef with members
                Player player = store.getComponent(ref, Player.getComponentType());
                if (player != null) {
                    player.sendMessage(Message.raw("§cTransferez le role de Chef ou dissolvez la guilde."));
                }
                return false;
            }
            // Seul membre -> disband
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

        // Clear guild data for all online members
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
                memberRef.sendMessage(Message.raw("§cLa guilde a ete dissoute."));
            }
        }

        GuildManager.disbandGuild(guild.getId());
        return true;
    }

    public static boolean handleInviteByIndex(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int idx;
        try { idx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return false; }
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
            player.sendMessage(Message.raw("§aInvitation envoyee a " + targetName));
        }
        targetRef.sendMessage(Message.raw("§eVous etes invite a rejoindre la guilde " + guild.getFormattedName()));
        targetRef.sendMessage(Message.raw("§7Tapez §f/es guild accept _ §7pour accepter."));
        return true;
    }

    private static void populateInviteList(UICommandBuilder ui, Guild guild, UUID myUUID) {
        cachedInviteNames.clear();

        List<PlayerRef> allPlayers = Universe.get().getPlayers();
        Set<UUID> memberUUIDs = guild.getMembers();

        for (PlayerRef pRef : allPlayers) {
            if (cachedInviteNames.size() >= MAX_INVITE_SLOTS) break;

            UUID pUUID = extractUUID(pRef);
            if (pUUID == null || memberUUIDs.contains(pUUID)) continue;

            // Check can join guild
            try {
                var eRef = pRef.getReference();
                if (eRef != null) {
                    var s = eRef.getStore();
                    PlayerLevelData d = s.getComponent(eRef, EldaniorSystem.get().getPlayerLevelDataType());
                    if (d != null && !d.canJoinGuild()) continue;
                }
            } catch (Exception ignored) {}

            cachedInviteNames.add(pRef.getUsername());
        }

        for (int i = 0; i < MAX_INVITE_SLOTS; i++) {
            if (i < cachedInviteNames.size()) {
                ui.set("#GInvitePlayer" + i + ".Visible", true);
                ui.set("#GInviteName" + i + ".Text", cachedInviteNames.get(i));
            } else {
                ui.set("#GInvitePlayer" + i + ".Visible", false);
            }
        }
    }

    private static UUID getPlayerUUID(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
        return extractUUID(pRef);
    }

    private static UUID extractUUID(PlayerRef playerRef) {
        if (playerRef == null) return null;
        try {
            Field f = PlayerRef.class.getDeclaredField("uuid");
            f.setAccessible(true);
            return (UUID) f.get(playerRef);
        } catch (Exception e) { return null; }
    }
}
