package com.eldanior.system.guild;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GuildManager {

    private static final Map<String, Guild> guilds = new ConcurrentHashMap<>();
    private static final Map<UUID, String> playerGuildMap = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> pendingInvites = new ConcurrentHashMap<>();

    public static void init() {
        System.out.println("[Eldanior] Systeme de Guildes initialise.");
    }

    // ==================== CREATION ====================

    public static Guild createGuild(String name, String tag, UUID founderUUID, String founderName) {
        String id = name.toLowerCase().replace(" ", "_");
        Guild guild = new Guild(id, name, tag, founderUUID, founderName);
        guilds.put(id, guild);
        playerGuildMap.put(founderUUID, id);
        return guild;
    }

    // ==================== GETTERS ====================

    public static Guild get(String id) {
        return guilds.get(id);
    }

    public static Guild getByTag(String tag) {
        for (Guild guild : guilds.values()) {
            if (guild.getTag().equalsIgnoreCase(tag)) return guild;
        }
        return null;
    }

    public static Guild getByName(String name) {
        for (Guild guild : guilds.values()) {
            if (guild.getName().equalsIgnoreCase(name)) return guild;
        }
        return null;
    }

    public static Guild getPlayerGuild(UUID playerUUID) {
        String guildId = playerGuildMap.get(playerUUID);
        if (guildId == null) return null;
        return guilds.get(guildId);
    }

    public static Collection<Guild> getAll() { return guilds.values(); }

    public static boolean guildExists(String name) {
        return getByName(name) != null;
    }

    public static boolean tagExists(String tag) {
        return getByTag(tag) != null;
    }

    // ==================== MEMBERSHIP ====================

    public static void joinGuild(UUID playerUUID, Guild guild) {
        guild.addMember(playerUUID);
        playerGuildMap.put(playerUUID, guild.getId());
    }

    public static void leaveGuild(UUID playerUUID) {
        String guildId = playerGuildMap.remove(playerUUID);
        if (guildId != null) {
            Guild guild = guilds.get(guildId);
            if (guild != null) guild.removeMember(playerUUID);
        }
    }

    public static void disbandGuild(String guildId) {
        Guild guild = guilds.remove(guildId);
        if (guild != null) {
            for (UUID member : guild.getMembers()) {
                playerGuildMap.remove(member);
            }
        }
    }

    // ==================== INVITATIONS ====================

    public static void sendInvite(UUID targetUUID, UUID fromUUID) {
        pendingInvites.put(targetUUID, fromUUID);
    }

    public static UUID getPendingInvite(UUID targetUUID) {
        return pendingInvites.get(targetUUID);
    }

    public static void clearInvite(UUID targetUUID) {
        pendingInvites.remove(targetUUID);
    }

    public static boolean hasPendingInvite(UUID targetUUID) {
        return pendingInvites.containsKey(targetUUID);
    }
}