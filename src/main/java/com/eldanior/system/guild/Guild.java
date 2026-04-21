package com.eldanior.system.guild;

import java.util.*;

public class Guild {

    private final String id;
    private final String name;
    private final String tag;
    private UUID founderUUID;
    private String founderName;

    // Stats de guilde
    private int totalMobKills = 0;
    private int totalPlayerKills = 0;
    private int totalDeaths = 0;
    private final Set<UUID> members = new HashSet<>();

    public Guild(String id, String name, String tag, UUID founderUUID, String founderName) {
        this.id = id;
        this.name = name;
        this.tag = tag;
        this.founderUUID = founderUUID;
        this.founderName = founderName;
        this.members.add(founderUUID);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getTag() { return tag; }
    public UUID getFounderUUID() { return founderUUID; }
    public String getFounderName() { return founderName; }

    // Stats
    public int getTotalMobKills() { return totalMobKills; }
    public int getTotalPlayerKills() { return totalPlayerKills; }
    public int getTotalDeaths() { return totalDeaths; }
    public void addMobKill() { this.totalMobKills++; }
    public void addPlayerKill() { this.totalPlayerKills++; }
    public void addDeath() { this.totalDeaths++; }

    // Membres
    public Set<UUID> getMembers() { return members; }
    public int getMemberCount() { return members.size(); }
    public void addMember(UUID uuid) { members.add(uuid); }
    public void removeMember(UUID uuid) { members.remove(uuid); }
    public boolean hasMember(UUID uuid) { return members.contains(uuid); }

    public String getFormattedTag() { return "§8[§e" + tag + "§8]"; }
    public String getFormattedName() { return "§e" + name; }
}