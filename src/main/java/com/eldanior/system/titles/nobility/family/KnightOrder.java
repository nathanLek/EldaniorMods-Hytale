package com.eldanior.system.titles.nobility.family;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class KnightOrder {

    private final String id;
    private final String name;
    private final String familyId;
    private final String motto;
    private final UUID capitaineUUID;
    private final String capitaineName;

    private volatile UUID lieutenantUUID;
    private volatile String lieutenantName;

    // Stats d'ordre (thread-safe)
    private final AtomicInteger totalKills = new AtomicInteger(0);
    private final AtomicInteger totalMissions = new AtomicInteger(0);
    private final Set<UUID> members = ConcurrentHashMap.newKeySet();

    private volatile String territoryId;
    private final long createdAt;

    public KnightOrder(String name, String motto, String familyId, UUID capitaineUUID, String capitaineName) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.motto = motto;
        this.familyId = familyId;
        this.capitaineUUID = capitaineUUID;
        this.capitaineName = capitaineName;
        this.createdAt = System.currentTimeMillis();
        this.members.add(capitaineUUID);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getFamilyId() { return familyId; }
    public String getMotto() { return motto; }
    public UUID getCapitaineUUID() { return capitaineUUID; }
    public String getCapitaineName() { return capitaineName; }
    public UUID getLieutenantUUID() { return lieutenantUUID; }
    public String getLieutenantName() { return lieutenantName; }
    public String getTerritoryId() { return territoryId; }
    public long getCreatedAt() { return createdAt; }

    // Stats
    public int getTotalKills() { return totalKills.get(); }
    public int getTotalMissions() { return totalMissions.get(); }
    public void addKill() { this.totalKills.incrementAndGet(); }
    public void addMission() { this.totalMissions.incrementAndGet(); }

    // Membres — retourne une copie immuable pour la thread-safety
    public Set<UUID> getMembers() { return Collections.unmodifiableSet(new HashSet<>(members)); }
    public int getMemberCount() { return members.size(); }

    public boolean addMember(UUID uuid) {
        if (members.size() >= 10) return false;
        members.add(uuid);
        return true;
    }

    public void removeMember(UUID uuid) { members.remove(uuid); }
    public boolean hasMember(UUID uuid) { return members.contains(uuid); }

    // Lieutenant
    public void setLieutenant(UUID uuid, String name) {
        this.lieutenantUUID = uuid;
        this.lieutenantName = name;
    }

    public void clearLieutenant() {
        this.lieutenantUUID = null;
        this.lieutenantName = null;
    }

    // Roles
    public boolean isCapitaine(UUID uuid) { return capitaineUUID.equals(uuid); }

    public boolean isLieutenant(UUID uuid) {
        UUID lt = lieutenantUUID;
        return lt != null && lt.equals(uuid);
    }

    public KnightOrderRole getRoleOf(UUID uuid) {
        if (isCapitaine(uuid)) return KnightOrderRole.CAPITAINE;
        if (isLieutenant(uuid)) return KnightOrderRole.LIEUTENANT;
        if (hasMember(uuid)) return KnightOrderRole.MEMBRE;
        return null;
    }

    // Territoire
    public void setTerritoryId(String territoryId) { this.territoryId = territoryId; }

    public String getFormattedName() { return "#FFD700" + name; }
}
