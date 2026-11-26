package com.eldanior.system.territory;

import com.eldanior.system.config.EldaniorLogger;
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

    // Donjon: rank (E, D, C, B, A, S)
    private String dungeonRank = "E";
    // Premier joueur a decouvrir cette parcelle
    private String firstDiscovererName = "";
    private String firstDiscovererUUID = "";
    // Mine/Farm: delai de regeneration des blocs (en secondes)
    private int regenDelaySec = 300; // 5 min par defaut
    // Borne de territoire (2026-11-10, territory.borne) : forme de la zone, regeneration des blocs casses, bloc qui la fait exister
    private boolean cylinder = false;   // true : cylindre inscrit dans les limites (centre, rayon = demi-largeur est-ouest)
    private boolean regen = false;      // blocs casses remis apres regenDelaySec (FarmRegenSystem), hors fermes et mines
    private String borne = "";          // bloc de la borne ("monde|x|y|z"), "" : zone creee par commande
    private String announce = "";       // annonce a l'entree : "" = regle habituelle, "on" = toujours, "off" = jamais (borne)
    // Pancartes de propriete (2026-11-12, territory.pancarte) : blocs "monde|x|y|z" lies a cette zone, separes par ';'
    private String pancartes = "";
    /** Preavis d'expulsion donne par le proprietaire (2026-11-17) : fin de la location a cette date (ms), 0 = aucun. */
    private long evictionAt = 0;
    /** Invites du proprietaire mis de cote pendant une location (serializeMembers), rendus au depart du locataire. */
    private String ownerGuests = "";
    private long lastTaxCollection = 0;
    private long lastTaxAmount = 0;
    private long lastTreasuryTransfer = 0;
    /** Taxe du commerce percue (2026-11-18, intendant) : debut de la semaine en cours, montant de la semaine, total. */
    private long tradeTaxWeekStart = 0, tradeTaxWeek = 0, tradeTaxTotal = 0;
    /** Taxe Imperiale (2026-11-18) : derniere levee par le Clan Luo sur ce domaine d'un clan vassal, et son montant. */
    private long lastImperialTax = 0, lastImperialAmount = 0;

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
        // 2026-11-13 : une zone creee sans proprietaire (admin, zone de la ville) inscrivait un membre nul, et la fenetre de
        // l'affiche plantait dessus (« UUID.equals ... getKey() is null »)
        if (ownerUUID != null) this.members.put(ownerUUID, ParcelRole.OWNER);
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
        if (!(x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ)) return false;
        return !cylinder || inCircle(x + 0.5, z + 0.5);
    }

    public boolean contains(double x, double y, double z) {
        if (!(x >= minX && x <= maxX + 1 && y >= minY && y <= maxY + 1 && z >= minZ && z <= maxZ + 1)) return false;
        return !cylinder || inCircle(x, z);
    }

    /** Zone cylindrique : point (x, z) dans le cercle inscrit (centre des limites, rayon = demi-largeur est-ouest). */
    private boolean inCircle(double x, double z) {
        double cx = (minX + maxX + 1) / 2.0, cz = (minZ + maxZ + 1) / 2.0, r = (maxX - minX + 1) / 2.0;
        double dx = x - cx, dz = z - cz;
        return dx * dx + dz * dz <= r * r;
    }

    public boolean isCylinder() { return cylinder; }
    public void setCylinder(boolean cylinder) { this.cylinder = cylinder; }
    public boolean isRegen() { return regen; }
    public void setRegen(boolean regen) { this.regen = regen; }
    public String getBorne() { return borne != null ? borne : ""; }
    public String getAnnounce() { return announce != null ? announce : ""; }
    /** Ville d'une maison retiree du marche des terres par son Patriarche (2026-11-18) ; sinon, sans maitre, elle est a vendre. */
    /**
     * Lois du domaine (2026-11-18, territory/laws/DomainLaws) : cle -> valeur, vide = loi absente. Cles : curfew ("22-6"),
     * fine (amende en cuivres), border ("" ouvertes, "toll" droit d'entree, "closed" fermees), toll (droit d'entree), arms ("1" :
     * armes rengainees), banned ("uuid|nom,..."), paid ("uuid|fin,..." : droits d'entree payes), tp ("1" : teleporteur actif),
     * tpPrice (prix du voyage), tpAt ("monde|x|y|z" : bloc du Teleporteur Continental).
     */
    private final Map<String, String> laws = new java.util.concurrent.ConcurrentHashMap<>();
    public String law(String key) { return laws.getOrDefault(key, ""); }
    public void setLaw(String key, String value) { if (value == null || value.isEmpty()) laws.remove(key); else laws.put(key, value); }
    public long lawLong(String key, long def) { try { return Long.parseLong(law(key)); } catch (NumberFormatException e) { return def; } }
    public boolean hasLaws() { return !laws.isEmpty(); }
    public String serializeLaws() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : new java.util.TreeMap<>(laws).entrySet()) {
            if (sb.length() > 0) sb.append(';');
            sb.append(e.getKey()).append('=').append(java.net.URLEncoder.encode(e.getValue(), java.nio.charset.StandardCharsets.UTF_8));
        }
        return sb.toString();
    }
    public void deserializeLaws(String s) {
        laws.clear();
        if (s == null || s.isEmpty()) return;
        for (String part : s.split(";")) {
            int eq = part.indexOf('=');
            if (eq <= 0) continue;
            try { laws.put(part.substring(0, eq), java.net.URLDecoder.decode(part.substring(eq + 1), java.nio.charset.StandardCharsets.UTF_8)); }
            catch (Exception ignored) { }
        }
    }

    private boolean reserved = false;
    public boolean isReserved() { return reserved; }
    public void setReserved(boolean reserved) { this.reserved = reserved; }
    public void setAnnounce(String announce) { this.announce = announce != null ? announce : ""; }
    public void setBorne(String borne) { this.borne = borne != null ? borne : ""; }
    public String getPancartes() { return pancartes != null ? pancartes : ""; }
    public long getEvictionAt() { return evictionAt; }
    public void setEvictionAt(long evictionAt) { this.evictionAt = Math.max(0, evictionAt); }
    public String getOwnerGuests() { return ownerGuests != null ? ownerGuests : ""; }
    public void setOwnerGuests(String ownerGuests) { this.ownerGuests = ownerGuests != null ? ownerGuests : ""; }
    public void setPancartes(String pancartes) { this.pancartes = pancartes != null ? pancartes : ""; }
    public boolean hasPancarte(String key) {
        for (String k : getPancartes().split(";")) if (k.equals(key)) return true;
        return false;
    }
    public void addPancarte(String key) {
        if (key == null || key.isEmpty() || hasPancarte(key)) return;
        pancartes = getPancartes().isEmpty() ? key : getPancartes() + ";" + key;
    }
    public void removePancarte(String key) {
        StringBuilder b = new StringBuilder();
        for (String k : getPancartes().split(";")) {
            if (k.isEmpty() || k.equals(key)) continue;
            if (b.length() > 0) b.append(';');
            b.append(k);
        }
        pancartes = b.toString();
    }

    // ==================== PERMISSIONS ====================

    public boolean hasPermission(UUID playerUUID, ParcelPermission permission) {
        if (playerUUID == null) return false;

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

            // PVP : autorise pour tous les non-membres si la zone a le PvP active
            if (permission == ParcelPermission.PVP && pvpEnabled) return true;

            // Zones speciales : tout le monde peut entrer et interagir
            if (type == ParcelType.ARENA || type == ParcelType.DUNGEON || type == ParcelType.MINE) {
                return permission == ParcelPermission.INTERACT || permission == ParcelPermission.ENTER;
            }
            // Farm / Forest : tout le monde peut casser/entrer/interagir (zone de recolte)
            if (type == ParcelType.FARM || type == ParcelType.FOREST) {
                return permission == ParcelPermission.INTERACT || permission == ParcelPermission.ENTER
                        || permission == ParcelPermission.BREAK;
            }

            // Villes et Territoires : INTERACT et ENTER autorisés pour tous
            // Seuls BUILD et BREAK sont bloqués
            if (type == ParcelType.KINGDOM || type == ParcelType.GRAND_TERRITORY || type == ParcelType.TERRITORY || type == ParcelType.CITY) {
                return permission == ParcelPermission.INTERACT || permission == ParcelPermission.ENTER;
            }

            // Plots/Housing/Room/Farm : check les permissions du role VISITOR
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
        if (uuid == null || role == null) return;
        members.put(uuid, role);
    }

    public void removeMember(UUID uuid) {
        if (uuid != null && !uuid.equals(ownerUUID)) members.remove(uuid);
    }

    public ParcelRole getRole(UUID uuid) {
        return members.get(uuid);
    }

    public boolean isMember(UUID uuid) {
        return members.containsKey(uuid);
    }

    public boolean isOwner(UUID uuid) {
        return uuid != null && uuid.equals(ownerUUID);
    }

    // ==================== SERIALIZATION ====================

    public String serializeMembers() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<UUID, ParcelRole> e : members.entrySet()) {
            if (e.getKey() == null || e.getValue() == null) continue;
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
            if (parts.length == 2 && !"null".equals(parts[0])) {          // ancienne entree nulle (avant le 2026-11-13) : ignoree
                try {
                    members.put(UUID.fromString(parts[0]), ParcelRole.valueOf(parts[1]));
                } catch (Exception e) { EldaniorLogger.error("ParcelData", e); }
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
                } catch (Exception e) { EldaniorLogger.error("ParcelData", e); }
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

    public long getTradeTaxWeekStart() { return tradeTaxWeekStart; }
    public long getTradeTaxTotal() { return tradeTaxTotal; }
    /** Taxe du commerce de la semaine en cours (remise a zero apres 7 jours). */
    public long getTradeTaxWeek() {
        return System.currentTimeMillis() - tradeTaxWeekStart > 7L * 24 * 3600 * 1000 ? 0 : tradeTaxWeek;
    }
    public void setTradeTax(long weekStart, long week, long total) { tradeTaxWeekStart = weekStart; tradeTaxWeek = week; tradeTaxTotal = total; }
    public void addTradeTax(long amount) {
        long now = System.currentTimeMillis();
        if (now - tradeTaxWeekStart > 7L * 24 * 3600 * 1000) { tradeTaxWeekStart = now; tradeTaxWeek = 0; }
        tradeTaxWeek += amount;
        tradeTaxTotal += amount;
    }
    public long getLastImperialTax() { return lastImperialTax; }
    public long getLastImperialAmount() { return lastImperialAmount; }
    public void setImperialTax(long at, long amount) { lastImperialTax = at; lastImperialAmount = amount; }
    public boolean canLevyImperialTax() { return System.currentTimeMillis() - lastImperialTax >= 7L * 24 * 3600 * 1000; }
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

    // ==================== DONJON / MINE ====================

    public String getDungeonRank() { return dungeonRank != null ? dungeonRank : "E"; }
    public void setDungeonRank(String dungeonRank) { this.dungeonRank = dungeonRank; }
    public String getFirstDiscovererName() { return firstDiscovererName; }
    public String getFirstDiscovererUUID() { return firstDiscovererUUID; }
    public boolean setFirstDiscoverer(String name, String uuid) {
        if (firstDiscovererName == null || firstDiscovererName.isEmpty()) {
            this.firstDiscovererName = name;
            this.firstDiscovererUUID = uuid;
            return true;
        }
        return false;
    }
    public int getRegenDelaySec() { return regenDelaySec; }
    public void setRegenDelaySec(int regenDelaySec) { this.regenDelaySec = regenDelaySec; }

    public List<String> getChildIds() {
        return ParcelManager.getChildrenOf(this.id);
    }
}
