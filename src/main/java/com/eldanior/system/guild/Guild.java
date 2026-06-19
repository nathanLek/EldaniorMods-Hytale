package com.eldanior.system.guild;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Guild {

    private final String id;
    private final String name;
    private final String tag;
    private UUID founderUUID;
    private String founderName;

    // Stats de guilde (thread-safe)
    private final AtomicInteger totalMobKills = new AtomicInteger(0);
    private final AtomicInteger totalPlayerKills = new AtomicInteger(0);
    private final AtomicInteger totalDeaths = new AtomicInteger(0);
    private final Set<UUID> members = ConcurrentHashMap.newKeySet();

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
    public int getTotalMobKills() { return totalMobKills.get(); }
    public int getTotalPlayerKills() { return totalPlayerKills.get(); }
    public int getTotalDeaths() { return totalDeaths.get(); }
    public void addMobKill() { this.totalMobKills.incrementAndGet(); }
    public void addPlayerKill() { this.totalPlayerKills.incrementAndGet(); }
    public void addDeath() { this.totalDeaths.incrementAndGet(); }

    // Tresorerie & Contribution (thread-safe)
    private final AtomicLong treasury = new AtomicLong(0);
    private final AtomicLong contribution = new AtomicLong(0);

    public long getTreasury() { return treasury.get(); }
    public void addTreasury(long amount) { this.treasury.addAndGet(amount); }
    public boolean withdrawTreasury(long amount) {
        long prev = treasury.getAndUpdate(current -> current >= amount ? current - amount : current);
        return prev >= amount;
    }
    public long getContribution() { return contribution.get(); }
    public void addContribution(long points) { this.contribution.addAndGet(points); }
    public void setTreasury(long v) { this.treasury.set(v); }
    public void setContribution(long v) { this.contribution.set(v); }

    // Membres — retourne une copie immuable pour la thread-safety
    public Set<UUID> getMembers() { return Collections.unmodifiableSet(new HashSet<>(members)); }
    public int getMemberCount() { return members.size(); }
    public void addMember(UUID uuid) { members.add(uuid); }
    public void removeMember(UUID uuid) { members.remove(uuid); }
    public boolean hasMember(UUID uuid) { return members.contains(uuid); }

    public String getFormattedTag() { return "§8[§e" + tag + "§8]"; }
    public String getFormattedName() { return "§e" + name; }
}