package com.eldanior.system.guild;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GuildManager {

    private static final Map<String, Guild> guilds = new ConcurrentHashMap<>();
    private static final Map<String, Guild> tagIndex = new ConcurrentHashMap<>();
    private static final Map<String, Guild> nameIndex = new ConcurrentHashMap<>();
    private static final Map<UUID, String> playerGuildMap = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> pendingInvites = new ConcurrentHashMap<>();

    public static void init() {
        guilds.clear();
        tagIndex.clear();
        nameIndex.clear();
        playerGuildMap.clear();
        pendingInvites.clear();
        System.out.println("[Eldanior] Systeme de Guildes initialise.");
    }

    // ==================== CREATION ====================

    public static Guild createGuild(String name, String tag, UUID founderUUID, String founderName) {
        // Validation des entrees
        name = name.trim();
        tag = tag.trim().toUpperCase();
        if (name.length() < 3 || name.length() > 24) return null;
        if (tag.length() < 2 || tag.length() > 5) return null;
        if (!name.matches("[a-zA-Z0-9_ \\-]+")) return null;
        if (!tag.matches("[A-Z0-9]+")) return null;
        if (getByName(name) != null || getByTag(tag) != null) return null;

        String id = name.toLowerCase().replace(" ", "_");
        Guild guild = new Guild(id, name, tag, founderUUID, founderName);
        guilds.put(id, guild);
        tagIndex.put(tag.toLowerCase(), guild);
        nameIndex.put(name.toLowerCase(), guild);
        playerGuildMap.put(founderUUID, id);
        return guild;
    }

    // ==================== GETTERS ====================

    public static Guild get(String id) {
        return guilds.get(id);
    }

    public static Guild getByTag(String tag) {
        return tagIndex.get(tag.toLowerCase());
    }

    public static Guild getByName(String name) {
        return nameIndex.get(name.toLowerCase());
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
            tagIndex.remove(guild.getTag().toLowerCase());
            nameIndex.remove(guild.getName().toLowerCase());
            for (UUID member : new ArrayList<>(guild.getMembers())) {
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