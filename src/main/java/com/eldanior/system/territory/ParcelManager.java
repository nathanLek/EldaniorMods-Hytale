package com.eldanior.system.territory;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ParcelManager {

    private static final Map<String, ParcelData> parcels = new ConcurrentHashMap<>();
    private static final Map<UUID, int[]> playerSelections = new ConcurrentHashMap<>(); // pos1[3] + pos2[3]
    private static Path dataDir;
    private static volatile long version = 0;

    public static long getVersion() { return version; }

    public static void init(Path pluginDataDir) {
        dataDir = pluginDataDir;
        load();
        assignDefaultOwners();
        System.out.println("[Eldanior] ParcelManager: " + parcels.size() + " parcelles chargees.");
    }

    // ==================== SELECTION (pos1/pos2) ====================

    public static void setPos1(UUID playerUUID, int x, int y, int z) {
        int[] sel = playerSelections.computeIfAbsent(playerUUID, k -> new int[6]);
        sel[0] = x; sel[1] = y; sel[2] = z;
    }

    public static void setPos2(UUID playerUUID, int x, int y, int z) {
        int[] sel = playerSelections.computeIfAbsent(playerUUID, k -> new int[6]);
        sel[3] = x; sel[4] = y; sel[5] = z;
    }

    public static int[] getSelection(UUID playerUUID) {
        return playerSelections.get(playerUUID);
    }

    public static boolean hasFullSelection(UUID playerUUID) {
        int[] sel = playerSelections.get(playerUUID);
        return sel != null;
    }

    public static void clearSelection(UUID playerUUID) {
        playerSelections.remove(playerUUID);
    }

    // ==================== CRUD ====================

    public static String createParcel(String name, ParcelType type, UUID ownerUUID, String ownerName,
                                      String world, int x1, int y1, int z1, int x2, int y2, int z2) {
        String id = type.name().toLowerCase() + "_" + name.toLowerCase().replace(" ", "_") + "_" + System.currentTimeMillis() % 100000;
        ParcelData parcel = new ParcelData(id, name, type, ownerUUID, ownerName, world, x1, y1, z1, x2, y2, z2);
        parcels.put(id, parcel);
        save();
        return id;
    }

    /**
     * Prix pour choisir une famille (achat du territoire associe).
     */
    public static long getFamilyTerritoryPrice(String familyId) {
        for (ParcelData p : parcels.values()) {
            if (familyId.equalsIgnoreCase(p.getFamilyId())) {
                return switch (p.getType()) {
                    case GRAND_TERRITORY -> 100_000_000L;
                    case TERRITORY -> 30_000_000L;
                    default -> 0L;
                };
            }
        }
        return 0L;
    }

    /**
     * Trouve la parcelle associee a une famille.
     */
    public static ParcelData getFamilyParcel(String familyId) {
        for (ParcelData p : parcels.values()) {
            if (familyId.equalsIgnoreCase(p.getFamilyId())) {
                return p;
            }
        }
        return null;
    }

    /**
     * Prix pour acheter une ville (Comte + Guilde).
     */
    public static final long CITY_PRICE = 5_000_000L;

    /**
     * Trouve les villes sans proprietaire joueur dans un territoire.
     */
    public static List<ParcelData> getAvailableCities() {
        List<ParcelData> cities = new ArrayList<>();
        for (ParcelData p : parcels.values()) {
            if (p.getType() == ParcelType.CITY && p.getOwnerUUID() == null) {
                cities.add(p);
            }
        }
        return cities;
    }

    /** Table de hierarchie valide : type parent → types enfants autorises */
    private static final java.util.Map<ParcelType, java.util.Set<ParcelType>> VALID_CHILDREN = java.util.Map.of(
        ParcelType.KINGDOM, java.util.Set.of(ParcelType.GRAND_TERRITORY, ParcelType.TERRITORY, ParcelType.CITY, ParcelType.ARENA, ParcelType.DUNGEON, ParcelType.MINE, ParcelType.FARM, ParcelType.FOREST, ParcelType.PLOT, ParcelType.HOUSING),
        ParcelType.GRAND_TERRITORY, java.util.Set.of(ParcelType.TERRITORY, ParcelType.CITY, ParcelType.ARENA, ParcelType.DUNGEON, ParcelType.MINE, ParcelType.FARM, ParcelType.FOREST, ParcelType.PLOT, ParcelType.HOUSING),
        ParcelType.TERRITORY, java.util.Set.of(ParcelType.CITY, ParcelType.ARENA, ParcelType.DUNGEON, ParcelType.MINE, ParcelType.FARM, ParcelType.FOREST, ParcelType.PLOT, ParcelType.HOUSING),
        ParcelType.CITY, java.util.Set.of(ParcelType.PLOT, ParcelType.HOUSING, ParcelType.FARM, ParcelType.FOREST, ParcelType.ARENA, ParcelType.DUNGEON, ParcelType.MINE),
        ParcelType.HOUSING, java.util.Set.of(ParcelType.ROOM),
        ParcelType.MINE, java.util.Set.of(ParcelType.FARM)
    );

    public static String createParcel(String name, ParcelType type, UUID ownerUUID, String ownerName,
                                      String world, int x1, int y1, int z1, int x2, int y2, int z2, String parentId) {
        // Validation du parent
        if (parentId != null && !parentId.isEmpty()) {
            ParcelData parent = parcels.get(parentId);
            if (parent == null) {
                System.err.println("[Eldanior] Parent inexistant: " + parentId);
                return null;
            }
            java.util.Set<ParcelType> allowed = VALID_CHILDREN.getOrDefault(parent.getType(), java.util.Set.of());
            if (!allowed.contains(type)) {
                System.err.println("[Eldanior] " + type + " ne peut pas etre enfant de " + parent.getType());
                return null;
            }
        }

        String id = createParcel(name, type, ownerUUID, ownerName, world, x1, y1, z1, x2, y2, z2);
        ParcelData parcel = parcels.get(id);
        if (parcel != null && parentId != null) {
            parcel.setParentId(parentId);
        }

        // Si c'est un Royaume ou Territoire, adopter les parcelles orphelines a l'interieur
        if (parcel != null && (type == ParcelType.KINGDOM || type == ParcelType.TERRITORY)) {
            adoptOrphanParcels(parcel);
        }

        save();
        return id;
    }

    /**
     * Adopte les parcelles orphelines (sans parent) qui sont physiquement
     * a l'interieur de cette parcelle, si le type est compatible.
     */
    private static void adoptOrphanParcels(ParcelData newParent) {
        int adopted = 0;
        for (ParcelData p : parcels.values()) {
            if (p.getId().equals(newParent.getId())) continue;
            if (p.getParentId() != null && !p.getParentId().isEmpty()) continue;
            if (p.getType() == ParcelType.KINGDOM) continue;

            java.util.Set<ParcelType> allowed = VALID_CHILDREN.getOrDefault(newParent.getType(), java.util.Set.of());
            if (!allowed.contains(p.getType())) continue;

            if (newParent.getWorld() != null && newParent.getWorld().equals(p.getWorld())
                    && newParent.contains(p.getMinX(), p.getMinY(), p.getMinZ())
                    && newParent.contains(p.getMaxX(), p.getMaxY(), p.getMaxZ())) {
                p.setParentId(newParent.getId());
                adopted++;
                System.out.println("[Eldanior] [Parcel] " + p.getName() + " (" + p.getType() +
                        ") adopte par " + newParent.getName() + " (" + newParent.getType() + ")");
            }
        }
        if (adopted > 0) {
            System.out.println("[Eldanior] [Parcel] " + adopted + " parcelles orphelines adoptees par " + newParent.getName());
        }
    }

    /**
     * Re-optimise la hierarchie : chaque parcelle est rattachee au parent
     * le plus precis (le plus petit) qui la contient et dont le type est compatible.
     * A appeler apres la generation d'un royaume complet.
     */
    public static void optimizeHierarchy() {
        int moved = 0;
        for (ParcelData p : parcels.values()) {
            if (p.getType() == ParcelType.KINGDOM) continue;

            // Trouver le parent le plus precis (le plus petit qui contient entierement cette parcelle)
            ParcelData bestParent = null;
            long bestVolume = Long.MAX_VALUE;

            for (ParcelData candidate : parcels.values()) {
                if (candidate.getId().equals(p.getId())) continue;
                if (candidate.getWorld() == null || !candidate.getWorld().equals(p.getWorld())) continue;

                // Le candidat doit etre un type parent valide
                java.util.Set<ParcelType> allowed = VALID_CHILDREN.getOrDefault(candidate.getType(), java.util.Set.of());
                if (!allowed.contains(p.getType())) continue;

                // Le candidat doit contenir entierement la parcelle
                if (!candidate.contains(p.getMinX(), p.getMinY(), p.getMinZ())) continue;
                if (!candidate.contains(p.getMaxX(), p.getMaxY(), p.getMaxZ())) continue;

                long volume = (long)(candidate.getMaxX() - candidate.getMinX()) *
                              (candidate.getMaxY() - candidate.getMinY()) *
                              (candidate.getMaxZ() - candidate.getMinZ());

                if (volume < bestVolume) {
                    bestVolume = volume;
                    bestParent = candidate;
                }
            }

            if (bestParent != null) {
                String oldParent = p.getParentId();
                String newParentId = bestParent.getId();
                if (!newParentId.equals(oldParent)) {
                    p.setParentId(newParentId);
                    // Heriter la famille du parent si la parcelle n'en a pas
                    if ((p.getFamilyId() == null || p.getFamilyId().isEmpty())
                            && bestParent.getFamilyId() != null && !bestParent.getFamilyId().isEmpty()) {
                        p.setFamilyId(bestParent.getFamilyId());
                        // Mettre le nom de famille comme proprietaire si pas de joueur
                        if (p.getOwnerUUID() == null) {
                            com.eldanior.system.titles.nobility.family.NobleFamilyModel fam =
                                    com.eldanior.system.titles.nobility.family.FamilyManager.get(bestParent.getFamilyId());
                            if (fam != null) p.setOwnerName("Famille " + fam.getDisplayName());
                        }
                        System.out.println("[Eldanior] [Hierarchy] " + p.getName() +
                                " herite famille " + bestParent.getFamilyId());
                    }
                    moved++;
                    System.out.println("[Eldanior] [Hierarchy] " + p.getName() +
                            " -> " + bestParent.getName() + " (" + bestParent.getType() + ")");
                }
            }
        }
        if (moved > 0) {
            System.out.println("[Eldanior] [Hierarchy] " + moved + " parcelles re-optimisees");
            save();
        }
    }

    /**
     * Supprime une parcelle. Les enfants sont re-parentes au grand-parent
     * (ex: supprimer un Territoire -> ses Villes passent sous le Royaume).
     */
    public static void deleteParcel(String id) {
        ParcelData parcel = parcels.get(id);
        if (parcel == null) return;

        String grandParentId = parcel.getParentId(); // peut etre null (Royaume)

        // Re-parenter les enfants au grand-parent
        List<String> children = getChildrenOf(id);
        for (String childId : children) {
            ParcelData child = parcels.get(childId);
            if (child != null) {
                child.setParentId(grandParentId);
                System.out.println("[Eldanior] [Parcel] " + child.getName() + " re-parente vers " +
                        (grandParentId != null ? grandParentId : "aucun (top-level)"));
            }
        }

        parcels.remove(id);
        save();
    }

    /**
     * Supprime une parcelle ET tous ses enfants recursivement.
     * A utiliser pour un nettoyage complet (ex: reset).
     */
    public static void deleteParcelRecursive(String id) {
        List<String> children = getChildrenOf(id);
        for (String childId : children) {
            deleteParcelRecursive(childId);
        }
        parcels.remove(id);
    }

    public static ParcelData get(String id) {
        return parcels.get(id);
    }

    public static Collection<ParcelData> getAll() {
        return parcels.values();
    }

    public static int countOwnedParcels(UUID ownerUUID) {
        int count = 0;
        for (ParcelData p : parcels.values()) {
            if (ownerUUID.equals(p.getOwnerUUID())) count++;
        }
        return count;
    }

    public static int countOwnedParcelsOfType(UUID ownerUUID, ParcelType type) {
        int count = 0;
        for (ParcelData p : parcels.values()) {
            if (ownerUUID.equals(p.getOwnerUUID()) && p.getType() == type) count++;
        }
        return count;
    }

    // ==================== LOOKUP ====================

    public static ParcelData getParcelAt(String world, int x, int y, int z) {
        // Retourne la parcelle la plus precise (enfant > parent)
        ParcelData best = null;
        long bestVolume = Long.MAX_VALUE;

        for (ParcelData parcel : parcels.values()) {
            if (!world.equals(parcel.getWorld())) continue;
            if (!parcel.contains(x, y, z)) continue;

            long volume = (long)(parcel.getMaxX() - parcel.getMinX()) *
                          (parcel.getMaxY() - parcel.getMinY()) *
                          (parcel.getMaxZ() - parcel.getMinZ());
            if (volume < bestVolume) {
                bestVolume = volume;
                best = parcel;
            }
        }
        return best;
    }

    public static ParcelData getParcelAt(String world, double x, double y, double z) {
        return getParcelAt(world, (int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    public static List<ParcelData> getParcelsOwnedBy(UUID ownerUUID) {
        return parcels.values().stream()
                .filter(p -> p.getOwnerUUID() != null && p.getOwnerUUID().equals(ownerUUID))
                .collect(Collectors.toList());
    }

    public static List<String> getChildrenOf(String parentId) {
        return parcels.values().stream()
                .filter(p -> parentId.equals(p.getParentId()))
                .map(ParcelData::getId)
                .collect(Collectors.toList());
    }

    public static List<ParcelData> getByType(ParcelType type) {
        return parcels.values().stream()
                .filter(p -> p.getType() == type)
                .collect(Collectors.toList());
    }

    // ==================== PERMISSION CHECK ====================

    public static boolean canPerform(String world, int x, int y, int z, UUID playerUUID, ParcelPermission permission) {
        ParcelData parcel = getParcelAt(world, x, y, z);
        if (parcel == null) return true; // Monde sauvage, pas de protection
        return parcel.hasPermission(playerUUID, permission);
    }

    // ==================== ACHAT / LOCATION ====================

    public static boolean buyParcel(String parcelId, UUID buyerUUID, String buyerName) {
        ParcelData parcel = get(parcelId);
        if (parcel == null || !parcel.isForSale()) return false;
        // Ne pas acheter son propre bien
        if (parcel.getOwnerUUID() != null && parcel.getOwnerUUID().equals(buyerUUID)) return false;

        // Nettoyer
        parcel.getMembers().clear();

        parcel.setOwnerUUID(buyerUUID);
        parcel.setOwnerName(buyerName);
        parcel.setPurchaseType("BOUGHT");
        parcel.setForSale(false);
        parcel.setForRent(false);
        parcel.setRenterUUID(null);
        parcel.setRentEndTime(0);
        parcel.addMember(buyerUUID, ParcelRole.OWNER);
        save();

        // Check titres de propriete
        checkOwnershipTitles(buyerUUID, parcel);

        return true;
    }

    public static boolean rentParcel(String parcelId, UUID renterUUID, String renterName) {
        ParcelData parcel = get(parcelId);
        if (parcel == null || !parcel.isForRent()) return false;
        if (parcel.getRenterUUID() != null) return false; // Deja loue

        // Le proprio ne change pas — si pas de proprio, la ville parente devient proprio
        if (parcel.getOwnerUUID() == null) {
            assignCityAsOwner(parcel);
        }

        parcel.setRenterUUID(renterUUID);
        parcel.setPurchaseType("RENTED");
        parcel.setRentEndTime(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000); // +7 jours
        parcel.setForRent(false);
        parcel.addMember(renterUUID, ParcelRole.OWNER); // Le locataire gere la parcelle
        save();
        return true;
    }

    public static void quitRental(String parcelId) {
        ParcelData parcel = get(parcelId);
        if (parcel == null || !parcel.isRented()) return;

        UUID renterUUID = parcel.getRenterUUID();
        if (renterUUID != null) parcel.removeMember(renterUUID);
        parcel.setRenterUUID(null);
        parcel.setRentEndTime(0);
        parcel.setPurchaseType("");
        parcel.setForRent(parcel.getRentPrice() > 0); // Remet en location
        save();
    }

    public static void assignCityAsOwnerPublic(ParcelData parcel) { assignCityAsOwner(parcel); }

    static void assignCityAsOwner(ParcelData parcel) {
        // Remonter la hierarchie pour trouver le parent naturel
        // Plot/Housing → Ville → Territoire → Royaume
        if (parcel.getParentId() != null) {
            ParcelData parent = get(parcel.getParentId());
            if (parent != null) {
                parcel.setOwnerName("[" + parent.getType().getLabel() + "] " + parent.getName());
                return;
            }
        }
        parcel.setOwnerName("[Libre]");
    }

    /**
     * Assigne les proprios par defaut pour toute la hierarchie.
     * Ville -> proprio = Territoire parent
     * Territoire -> proprio = Royaume parent
     */
    public static void assignDefaultOwners() {
        for (ParcelData p : parcels.values()) {
            if (p.getOwnerUUID() != null) continue; // Deja un proprio joueur
            if (p.getType() == ParcelType.PLOT || p.getType() == ParcelType.HOUSING
                    || p.getType() == ParcelType.CITY || p.getType() == ParcelType.TERRITORY) {
                if (p.getOwnerName() == null || p.getOwnerName().isEmpty() || "[Libre]".equals(p.getOwnerName())) {
                    assignCityAsOwner(p);
                }
            }
        }
        save();
    }

    public static boolean renewRent(String parcelId) {
        ParcelData parcel = get(parcelId);
        if (parcel == null || !parcel.isRented()) return false;
        // Ajouter 7 jours a partir de maintenant OU de la fin actuelle (si pas encore expire)
        long base = Math.max(System.currentTimeMillis(), parcel.getRentEndTime());
        parcel.setRentEndTime(base + 7L * 24 * 60 * 60 * 1000);
        save();
        return true;
    }

    public static void evict(String parcelId) {
        ParcelData parcel = get(parcelId);
        if (parcel == null) return;
        parcel.clearOwnership();
        parcel.setForRent(true); // Remet en location
        save();
    }

    public static void releaseParcel(String parcelId) {
        ParcelData parcel = get(parcelId);
        if (parcel == null) return;
        parcel.clearOwnership();
        parcel.setForSale(parcel.getPrice() > 0);
        parcel.setForRent(parcel.getRentPrice() > 0);
        save();
    }

    public static List<ParcelData> getAvailable() {
        return parcels.values().stream()
                .filter(ParcelData::isAvailable)
                .collect(Collectors.toList());
    }

    public static List<ParcelData> getByFamily(String familyId) {
        return parcels.values().stream()
                .filter(p -> familyId.equals(p.getFamilyId()))
                .collect(Collectors.toList());
    }

    public static void assignToFamily(String parcelId, String familyId) {
        ParcelData parcel = get(parcelId);
        if (parcel == null) return;
        parcel.setFamilyId(familyId);

        // Mettre le nom de la famille comme proprietaire si pas de joueur proprio
        if (parcel.getOwnerUUID() == null) {
            com.eldanior.system.titles.nobility.family.NobleFamilyModel family =
                    com.eldanior.system.titles.nobility.family.FamilyManager.get(familyId);
            if (family != null) {
                parcel.setOwnerName("Famille " + family.getDisplayName());
            }
        }
        save();
    }

    // ==================== HIERARCHY VALIDATION ====================

    public static boolean isValidParent(String parentId, ParcelType childType) {
        if (parentId == null) return childType == ParcelType.KINGDOM;
        ParcelData parent = get(parentId);
        if (parent == null) return false;

        ParcelType pt = parent.getType();
        return switch (childType) {
            case KINGDOM -> false;
            case GRAND_TERRITORY -> pt == ParcelType.KINGDOM;
            case TERRITORY -> pt == ParcelType.KINGDOM || pt == ParcelType.GRAND_TERRITORY || pt == ParcelType.TERRITORY;
            case CITY -> pt == ParcelType.TERRITORY || pt == ParcelType.GRAND_TERRITORY || pt == ParcelType.KINGDOM;
            case PLOT, HOUSING -> pt == ParcelType.CITY || pt == ParcelType.KINGDOM || pt == ParcelType.TERRITORY || pt == ParcelType.GRAND_TERRITORY;
            case ROOM -> pt == ParcelType.HOUSING;
            case FARM, FOREST -> pt == ParcelType.CITY || pt == ParcelType.TERRITORY || pt == ParcelType.GRAND_TERRITORY || pt == ParcelType.MINE || pt == ParcelType.KINGDOM;
            case ARENA, DUNGEON, MINE -> pt == ParcelType.KINGDOM || pt == ParcelType.GRAND_TERRITORY || pt == ParcelType.TERRITORY || pt == ParcelType.CITY;
        };
    }

    // ==================== PERSISTENCE ====================

    public static void save() {
        version++;
        try {
            Properties props = new Properties();
            for (ParcelData p : parcels.values()) {
                String prefix = p.getId() + ".";
                props.setProperty(prefix + "name", p.getName() != null ? p.getName() : "");
                props.setProperty(prefix + "type", p.getType() != null ? p.getType().name() : "PLOT");
                props.setProperty(prefix + "owner", p.getOwnerUUID() != null ? p.getOwnerUUID().toString() : "");
                props.setProperty(prefix + "ownerName", p.getOwnerName() != null ? p.getOwnerName() : "");
                props.setProperty(prefix + "parent", p.getParentId() != null ? p.getParentId() : "");
                props.setProperty(prefix + "world", p.getWorld() != null ? p.getWorld() : "");
                props.setProperty(prefix + "minX", String.valueOf(p.getMinX()));
                props.setProperty(prefix + "minY", String.valueOf(p.getMinY()));
                props.setProperty(prefix + "minZ", String.valueOf(p.getMinZ()));
                props.setProperty(prefix + "maxX", String.valueOf(p.getMaxX()));
                props.setProperty(prefix + "maxY", String.valueOf(p.getMaxY()));
                props.setProperty(prefix + "maxZ", String.valueOf(p.getMaxZ()));
                props.setProperty(prefix + "price", String.valueOf(p.getPrice()));
                props.setProperty(prefix + "taxRate", String.valueOf(p.getTaxRate()));
                props.setProperty(prefix + "forSale", String.valueOf(p.isForSale()));
                props.setProperty(prefix + "forRent", String.valueOf(p.isForRent()));
                props.setProperty(prefix + "renter", p.getRenterUUID() != null ? p.getRenterUUID().toString() : "");
                props.setProperty(prefix + "treasury", String.valueOf(p.getTreasury()));
                props.setProperty(prefix + "protected", String.valueOf(p.isProtectedByDefault()));
                props.setProperty(prefix + "rentPrice", String.valueOf(p.getRentPrice()));
                props.setProperty(prefix + "rentEndTime", String.valueOf(p.getRentEndTime()));
                props.setProperty(prefix + "purchaseType", p.getPurchaseType());
                props.setProperty(prefix + "familyId", p.getFamilyId());
                props.setProperty(prefix + "guildId", p.getGuildId());
                props.setProperty(prefix + "pvpEnabled", String.valueOf(p.isPvpEnabled()));
                props.setProperty(prefix + "lastTaxCollection", String.valueOf(p.getLastTaxCollection()));
                props.setProperty(prefix + "lastTaxAmount", String.valueOf(p.getLastTaxAmount()));
                props.setProperty(prefix + "lastTreasuryTransfer", String.valueOf(p.getLastTreasuryTransfer()));
                props.setProperty(prefix + "members", p.serializeMembers());
                props.setProperty(prefix + "permissions", p.serializePermissions());
                props.setProperty(prefix + "dungeonRank", p.getDungeonRank());
                props.setProperty(prefix + "regenDelaySec", String.valueOf(p.getRegenDelaySec()));
                props.setProperty(prefix + "firstDiscovererName", p.getFirstDiscovererName() != null ? p.getFirstDiscovererName() : "");
                props.setProperty(prefix + "firstDiscovererUUID", p.getFirstDiscovererUUID() != null ? p.getFirstDiscovererUUID() : "");
            }

            File file = dataDir.resolve("parcels.properties").toFile();
            file.getParentFile().mkdirs();
            try (FileOutputStream fos = new FileOutputStream(file)) {
                props.store(fos, "Eldanior Parcels");
            }
        } catch (Exception e) {
            System.err.println("[ParcelManager] Erreur sauvegarde: " + e.getMessage());
        }
    }

    public static void load() {
        parcels.clear();
        File file = dataDir.resolve("parcels.properties").toFile();
        if (!file.exists()) return;

        try {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
            }

            // Collecter les IDs uniques
            Set<String> ids = new HashSet<>();
            for (String key : props.stringPropertyNames()) {
                ids.add(key.substring(0, key.indexOf('.')));
            }

            for (String id : ids) {
                String prefix = id + ".";
                ParcelData p = new ParcelData(id);
                p.setName(props.getProperty(prefix + "name", ""));
                try { p.setType(ParcelType.valueOf(props.getProperty(prefix + "type", "PLOT"))); }
                catch (Exception e) { p.setType(ParcelType.PLOT); }

                String ownerStr = props.getProperty(prefix + "owner", "");
                if (!ownerStr.isEmpty()) p.setOwnerUUID(UUID.fromString(ownerStr));
                p.setOwnerName(props.getProperty(prefix + "ownerName", ""));

                String parentStr = props.getProperty(prefix + "parent", "");
                if (!parentStr.isEmpty()) p.setParentId(parentStr);

                p.setWorld(props.getProperty(prefix + "world", ""));
                p.setBounds(
                        Integer.parseInt(props.getProperty(prefix + "minX", "0")),
                        Integer.parseInt(props.getProperty(prefix + "minY", "0")),
                        Integer.parseInt(props.getProperty(prefix + "minZ", "0")),
                        Integer.parseInt(props.getProperty(prefix + "maxX", "0")),
                        Integer.parseInt(props.getProperty(prefix + "maxY", "0")),
                        Integer.parseInt(props.getProperty(prefix + "maxZ", "0"))
                );
                p.setPrice(Long.parseLong(props.getProperty(prefix + "price", "0")));
                p.setTaxRate(Float.parseFloat(props.getProperty(prefix + "taxRate", "0")));
                p.setForSale(Boolean.parseBoolean(props.getProperty(prefix + "forSale", "false")));
                p.setForRent(Boolean.parseBoolean(props.getProperty(prefix + "forRent", "false")));

                String renterStr = props.getProperty(prefix + "renter", "");
                if (!renterStr.isEmpty()) p.setRenterUUID(UUID.fromString(renterStr));

                p.setTreasury(Long.parseLong(props.getProperty(prefix + "treasury", "0")));
                p.setProtectedByDefault(Boolean.parseBoolean(props.getProperty(prefix + "protected", "true")));
                p.setRentPrice(Long.parseLong(props.getProperty(prefix + "rentPrice", "0")));
                p.setRentEndTime(Long.parseLong(props.getProperty(prefix + "rentEndTime", "0")));
                p.setPurchaseType(props.getProperty(prefix + "purchaseType", ""));
                p.setFamilyId(props.getProperty(prefix + "familyId", ""));
                p.setGuildId(props.getProperty(prefix + "guildId", ""));
                p.setPvpEnabled(Boolean.parseBoolean(props.getProperty(prefix + "pvpEnabled", "false")));
                p.setLastTaxCollection(Long.parseLong(props.getProperty(prefix + "lastTaxCollection", "0")));
                p.setLastTaxAmount(Long.parseLong(props.getProperty(prefix + "lastTaxAmount", "0")));
                p.setLastTreasuryTransfer(Long.parseLong(props.getProperty(prefix + "lastTreasuryTransfer", "0")));
                p.deserializeMembers(props.getProperty(prefix + "members", ""));
                p.deserializePermissions(props.getProperty(prefix + "permissions", ""));
                p.setDungeonRank(props.getProperty(prefix + "dungeonRank", "E"));
                p.setRegenDelaySec(Integer.parseInt(props.getProperty(prefix + "regenDelaySec", "300")));
                p.setFirstDiscoverer(
                        props.getProperty(prefix + "firstDiscovererName", ""),
                        props.getProperty(prefix + "firstDiscovererUUID", ""));


                // Fix: si location et proprio == locataire, enlever le proprio
                if (p.isRented() && p.getOwnerUUID() != null && p.getRenterUUID() != null
                        && p.getOwnerUUID().equals(p.getRenterUUID())) {
                    System.out.println("[ParcelManager] Fix location corrompue: " + p.getName());
                    p.setOwnerUUID(null);
                    p.setOwnerName("");
                    assignCityAsOwner(p);
                }
                // Fix: locataire doit etre OWNER dans les membres (pas MEMBER)
                if (p.isRented() && p.getRenterUUID() != null) {
                    ParcelRole renterRole = p.getRole(p.getRenterUUID());
                    if (renterRole == ParcelRole.MEMBER) {
                        p.addMember(p.getRenterUUID(), ParcelRole.OWNER);
                    }
                }

                parcels.put(id, p);
            }
        } catch (Exception e) {
            System.err.println("[ParcelManager] Erreur chargement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void saveAll() { save(); }

    private static void checkOwnershipTitles(UUID ownerUUID, ParcelData parcel) {
        try {
            com.hypixel.hytale.server.core.universe.PlayerRef playerRef =
                    com.hypixel.hytale.server.core.universe.Universe.get().getPlayer(ownerUUID);
            if (playerRef == null) return;

            var sRef = playerRef.getReference();
            if (sRef == null) return;
            var sStore = sRef.getStore();

            var lvlType = com.eldanior.system.EldaniorSystem.get().getPlayerLevelDataType();
            var data = sStore.getComponent(sRef, lvlType);
            if (data == null) return;

            int totalOwned = countOwnedParcels(ownerUUID);

            // Proprietaire (premiere parcelle)
            grantTitle(data, "land_owner", playerRef);
            // Baron Foncier (5 parcelles)
            if (totalOwned >= 5) grantTitle(data, "land_baron", playerRef);
            // Magnat Foncier (10 parcelles)
            if (totalOwned >= 10) grantTitle(data, "land_mogul", playerRef);
            // Empereur des Terres (25 parcelles)
            if (totalOwned >= 25) grantTitle(data, "land_emperor", playerRef);
            // Gouverneur (ville)
            if (parcel.getType() == ParcelType.CITY) grantTitle(data, "city_owner", playerRef);
            // Seigneur des Terres (territoire)
            if (parcel.getType() == ParcelType.TERRITORY || parcel.getType() == ParcelType.GRAND_TERRITORY)
                grantTitle(data, "territory_owner", playerRef);

            sStore.putComponent(sRef, lvlType, data);
        } catch (Exception e) {
            com.eldanior.system.config.EldaniorLogger.error("ParcelManager:titles", e);
        }
    }

    private static void grantTitle(com.eldanior.system.config.Player.PlayerLevelData data, String titleId,
                                   com.hypixel.hytale.server.core.universe.PlayerRef playerRef) {
        if (!data.getUnlockedTitles().contains(titleId)) {
            data.addTitle(titleId);
            com.eldanior.system.titles.models.TitleModel title = com.eldanior.system.titles.TitleManager.get(titleId);
            if (title != null && playerRef != null) {
                com.eldanior.system.Leveling.utils.NotificationHelper.showEventTitle(playerRef,
                        "TITRE DEBLOQUE", title.getDisplayName(), true);
            }
        }
    }
}
