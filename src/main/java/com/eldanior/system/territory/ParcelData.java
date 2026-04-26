package com.eldanior.system.territory;

import java.util.*;

public class ParcelData {

    private final String id;
    private String name;
    private ParcelType type;
    private UUID ownerUUID;
    private String ownerName;
    private String parentId; // ID de la parcelle parente (null si top-level)
    private String world;

    // Bornes du cuboid
    private int minX, minY, minZ;
    private int maxX, maxY, maxZ;

    // Membres et permissions
    private final Map<UUID, ParcelRole> members = new HashMap<>();
    private final Map<ParcelRole, Set<ParcelPermission>> rolePermissions = new HashMap<>();

    // Economie
    private long price;        // Prix d'achat
    private long rentPrice;    // Prix de location (par 7 jours)
    private float taxRate;
    private boolean forSale;
    private boolean forRent;
    private UUID renterUUID;
    private long rentEndTime;  // Timestamp fin de location (ms)
    private long treasury;
    private String purchaseType = ""; // "BOUGHT" ou "RENTED" ou "" (libre)

    // Famille noble ou guilde associee (pour Royaume/Territoire/Ville)
    private String familyId = "";
    private String guildId = ""; // Pour les villes gerees par une guilde

    // Protection par defaut
    private boolean protectedByDefault = true;
    private boolean pvpEnabled = false;
    private long lastTaxCollection = 0;
    private long lastTaxAmount = 0;
    private long lastTreasuryTransfer = 0;

    public ParcelData(String id, String name, ParcelType type, UUID ownerUUID, String ownerName,
                      String world, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;
        this.world = world;
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);

        // Permissions par defaut
        this.members.put(ownerUUID, ParcelRole.OWNER);
        initDefaultPermissions();
    }

    public ParcelData(String id) {
        this.id = id;
        initDefaultPermissions();
    }

    private void initDefaultPermissions() {
        // Owner : tout
        rolePermissions.put(ParcelRole.OWNER, EnumSet.allOf(ParcelPermission.class));
        // Officer : tout sauf PVP
        rolePermissions.put(ParcelRole.OFFICER, EnumSet.of(
                ParcelPermission.BUILD, ParcelPermission.BREAK, ParcelPermission.INTERACT, ParcelPermission.ENTER));
        // Member : interact + enter
        rolePermissions.put(ParcelRole.MEMBER, EnumSet.of(
                ParcelPermission.INTERACT, ParcelPermission.ENTER));
        // Visitor : enter seulement
        rolePermissions.put(ParcelRole.VISITOR, EnumSet.of(ParcelPermission.ENTER));
    }

    // ==================== POSITION CHECK ====================

    public boolean contains(int x, int y, int z) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    public boolean contains(double x, double y, double z) {
        return x >= minX && x <= maxX + 1 && y >= minY && y <= maxY + 1 && z >= minZ && z <= maxZ + 1;
    }

    // ==================== PERMISSIONS ====================

    public boolean hasPermission(UUID playerUUID, ParcelPermission permission) {
        // Si en location, seul le locataire (et ses invites) ont les permissions
        // Le proprio original n'a PAS acces pendant la location
        if (isRented() && renterUUID != null) {
            // Le locataire a toutes les permissions d'OWNER
            if (playerUUID.equals(renterUUID)) {
                Set<ParcelPermission> ownerPerms = rolePermissions.get(ParcelRole.OWNER);
                return ownerPerms != null && ownerPerms.contains(permission);
            }

            // Le proprio original n'a AUCUN acces pendant la location
            if (ownerUUID != null && playerUUID.equals(ownerUUID)) {
                return false;
            }
        }

        ParcelRole role = members.getOrDefault(playerUUID, null);

        // Non-membre
        if (role == null) {
            if (!protectedByDefault) return true; // Zone ouverte
            if (permission == ParcelPermission.ENTER) {
                Set<ParcelPermission> visitorPerms = rolePermissions.get(ParcelRole.VISITOR);
                return visitorPerms != null && visitorPerms.contains(permission);
            }
            return false;
        }

        Set<ParcelPermission> perms = rolePermissions.get(role);
        return perms != null && perms.contains(permission);
    }

    public void setRolePermission(ParcelRole role, ParcelPermission permission, boolean allowed) {
        Set<ParcelPermission> perms = rolePermissions.computeIfAbsent(role, k -> EnumSet.noneOf(ParcelPermission.class));
        if (allowed) perms.add(permission);
        else perms.remove(permission);
    }

    // ==================== MEMBRES ====================

    public void addMember(UUID uuid, ParcelRole role) {
        members.put(uuid, role);
    }

    public void removeMember(UUID uuid) {
        if (!uuid.equals(ownerUUID)) members.remove(uuid);
    }

    public ParcelRole getRole(UUID uuid) {
        return members.get(uuid);
    }

    public boolean isMember(UUID uuid) {
        return members.containsKey(uuid);
    }

    public boolean isOwner(UUID uuid) {
        return uuid.equals(ownerUUID);
    }

    // ==================== SERIALIZATION ====================

    public String serializeMembers() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<UUID, ParcelRole> e : members.entrySet()) {
            if (!sb.isEmpty()) sb.append(";");
            sb.append(e.getKey()).append(":").append(e.getValue().name());
        }
        return sb.toString();
    }

    public void deserializeMembers(String data) {
        members.clear();
        if (data == null || data.isEmpty()) return;
        for (String entry : data.split(";")) {
            String[] parts = entry.split(":");
            if (parts.length == 2) {
                try {
                    members.put(UUID.fromString(parts[0]), ParcelRole.valueOf(parts[1]));
                } catch (Exception ignored) {}
            }
        }
    }

    public String serializePermissions() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<ParcelRole, Set<ParcelPermission>> e : rolePermissions.entrySet()) {
            if (!sb.isEmpty()) sb.append(";");
            sb.append(e.getKey().name()).append("=");
            StringJoiner sj = new StringJoiner(",");
            for (ParcelPermission p : e.getValue()) sj.add(p.name());
            sb.append(sj);
        }
        return sb.toString();
    }

    public void deserializePermissions(String data) {
        rolePermissions.clear();
        if (data == null || data.isEmpty()) { initDefaultPermissions(); return; }
        for (String entry : data.split(";")) {
            String[] parts = entry.split("=");
            if (parts.length == 2) {
                try {
                    ParcelRole role = ParcelRole.valueOf(parts[0]);
                    Set<ParcelPermission> perms = EnumSet.noneOf(ParcelPermission.class);
                    if (!parts[1].isEmpty()) {
                        for (String p : parts[1].split(",")) perms.add(ParcelPermission.valueOf(p));
                    }
                    rolePermissions.put(role, perms);
                } catch (Exception ignored) {}
            }
        }
    }

    // ==================== GETTERS / SETTERS ====================

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public ParcelType getType() { return type; }
    public void setType(ParcelType type) { this.type = type; }
    public UUID getOwnerUUID() { return ownerUUID; }
    public void setOwnerUUID(UUID ownerUUID) { this.ownerUUID = ownerUUID; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    public String getWorld() { return world; }
    public void setWorld(String world) { this.world = world; }
    public int getMinX() { return minX; }
    public int getMinY() { return minY; }
    public int getMinZ() { return minZ; }
    public int getMaxX() { return maxX; }
    public int getMaxY() { return maxY; }
    public int getMaxZ() { return maxZ; }
    public void setBounds(int x1, int y1, int z1, int x2, int y2, int z2) {
        this.minX = Math.min(x1, x2); this.minY = Math.min(y1, y2); this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2); this.maxY = Math.max(y1, y2); this.maxZ = Math.max(z1, z2);
    }
    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }
    public float getTaxRate() { return taxRate; }
    public void setTaxRate(float taxRate) { this.taxRate = taxRate; }
    public boolean isForSale() { return forSale; }
    public void setForSale(boolean forSale) { this.forSale = forSale; }
    public boolean isForRent() { return forRent; }
    public void setForRent(boolean forRent) { this.forRent = forRent; }
    public UUID getRenterUUID() { return renterUUID; }
    public void setRenterUUID(UUID renterUUID) { this.renterUUID = renterUUID; }
    public long getTreasury() { return treasury; }
    public void setTreasury(long treasury) { this.treasury = treasury; }
    public boolean isProtectedByDefault() { return protectedByDefault; }
    public void setProtectedByDefault(boolean protectedByDefault) { this.protectedByDefault = protectedByDefault; }
    public Map<UUID, ParcelRole> getMembers() { return members; }
    public Map<ParcelRole, Set<ParcelPermission>> getRolePermissions() { return rolePermissions; }

    // Nouveaux champs
    public long getRentPrice() { return rentPrice; }
    public void setRentPrice(long rentPrice) { this.rentPrice = rentPrice; }
    public long getRentEndTime() { return rentEndTime; }
    public void setRentEndTime(long rentEndTime) { this.rentEndTime = rentEndTime; }
    public String getPurchaseType() { return purchaseType != null ? purchaseType : ""; }
    public void setPurchaseType(String purchaseType) { this.purchaseType = purchaseType; }
    public String getFamilyId() { return familyId != null ? familyId : ""; }
    public void setFamilyId(String familyId) { this.familyId = familyId; }
    public String getGuildId() { return guildId != null ? guildId : ""; }
    public void setGuildId(String guildId) { this.guildId = guildId; }
    public void addTreasury(long amount) { this.treasury += amount; }

    public boolean isPvpEnabled() { return pvpEnabled; }
    public void setPvpEnabled(boolean pvpEnabled) { this.pvpEnabled = pvpEnabled; }
    public long getLastTaxCollection() { return lastTaxCollection; }
    public void setLastTaxCollection(long t) { this.lastTaxCollection = t; }
    public long getLastTaxAmount() { return lastTaxAmount; }
    public void setLastTaxAmount(long a) { this.lastTaxAmount = a; }
    public long getLastTreasuryTransfer() { return lastTreasuryTransfer; }
    public void setLastTreasuryTransfer(long t) { this.lastTreasuryTransfer = t; }

    public boolean canCollectTax() {
        return System.currentTimeMillis() - lastTaxCollection >= 7L * 24 * 60 * 60 * 1000;
    }
    public boolean canTransferTreasury() {
        return System.currentTimeMillis() - lastTreasuryTransfer >= 7L * 24 * 60 * 60 * 1000;
    }

    public boolean isBought() { return "BOUGHT".equals(purchaseType); }
    public boolean isRented() { return "RENTED".equals(purchaseType); }
    public boolean isFree() { return ownerUUID == null && (purchaseType == null || purchaseType.isEmpty()); }
    public boolean isAvailable() {
        // Disponible si en vente OU en location (sans locataire)
        if (forSale) return true;
        if (forRent && renterUUID == null) return true;
        return false;
    }

    public boolean isRentExpired() {
        return isRented() && rentEndTime > 0 && System.currentTimeMillis() > rentEndTime;
    }
    public boolean isInGracePeriod() {
        // 24h de grace apres expiration
        return isRented() && rentEndTime > 0
                && System.currentTimeMillis() > rentEndTime
                && System.currentTimeMillis() < rentEndTime + 86400000L;
    }

    public void clearOwnership() {
        this.ownerUUID = null;
        this.ownerName = "";
        this.renterUUID = null;
        this.rentEndTime = 0;
        this.purchaseType = "";
        this.members.clear();
        initDefaultPermissions();
    }

    public List<String> getChildIds() {
        return ParcelManager.getChildrenOf(this.id);
    }
}
