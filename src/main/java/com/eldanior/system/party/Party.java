package com.eldanior.system.party;

import java.util.*;

public class Party {

    public static final int MAX_MEMBERS = 5;

    private UUID captainUUID;
    private String captainName;
    private final Map<UUID, String> members = new LinkedHashMap<>();

    public Party(UUID captainUUID, String captainName) {
        this.captainUUID = captainUUID;
        this.captainName = captainName;
        this.members.put(captainUUID, captainName);
    }

    public UUID getCaptainUUID() { return captainUUID; }
    public String getCaptainName() { return captainName; }
    public boolean isCaptain(UUID uuid) { return captainUUID.equals(uuid); }
    public boolean isMember(UUID uuid) { return members.containsKey(uuid); }
    public boolean isFull() { return members.size() >= MAX_MEMBERS; }
    public int getSize() { return members.size(); }
    public Map<UUID, String> getMembers() { return members; }

    public boolean addMember(UUID uuid, String name) {
        if (isFull() || members.containsKey(uuid)) return false;
        members.put(uuid, name);
        return true;
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
        // Si le capitaine part et qu'il reste des membres, le premier devient capitaine
        if (captainUUID.equals(uuid) && !members.isEmpty()) {
            Map.Entry<UUID, String> first = members.entrySet().iterator().next();
            captainUUID = first.getKey();
            captainName = first.getValue();
        }
    }

    public void transferCaptain(UUID newCaptainUUID) {
        if (!members.containsKey(newCaptainUUID)) return;
        this.captainUUID = newCaptainUUID;
        this.captainName = members.get(newCaptainUUID);
    }

    public List<UUID> getMemberUUIDs() {
        return new ArrayList<>(members.keySet());
    }

    public String getMemberName(UUID uuid) {
        return members.get(uuid);
    }
}