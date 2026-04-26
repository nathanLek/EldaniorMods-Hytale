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

    public static void init(Path pluginDataDir) {
        dataDir = pluginDataDir;
        load();
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

    public static String createParcel(String name, ParcelType type, UUID ownerUUID, String ownerName,
                                      String world, int x1, int y1, int z1, int x2, int y2, int z2, String parentId) {
        String id = createParcel(name, type, ownerUUID, ownerName, world, x1, y1, z1, x2, y2, z2);
        ParcelData parcel = parcels.get(id);
        if (parcel != null && parentId != null) {
            parcel.setParentId(parentId);
            save();
        }
        return id;
    }

    public static void deleteParcel(String id) {
        // Supprimer aussi les enfants
        List<String> children = getChildrenOf(id);
        for (String childId : children) {
            deleteParcel(childId);
        }
        parcels.remove(id);
        save();
    }

    public static ParcelData get(String id) {
        return parcels.get(id);
    }

    public static Collection<ParcelData> getAll() {
        return parcels.values();
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
        if (parcel == null || !parcel.isForSale() || !parcel.isFree()) return false;

        parcel.setOwnerUUID(buyerUUID);
        parcel.setOwnerName(buyerName);
        parcel.setPurchaseType("BOUGHT");
        parcel.setForSale(false);
        parcel.setForRent(false);
        parcel.addMember(buyerUUID, ParcelRole.OWNER);
        save();
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

    private static void assignCityAsOwner(ParcelData parcel) {
        // Chercher la ville/territoire parent pour l'afficher comme proprio
        if (parcel.getParentId() != null) {
            ParcelData parent = get(parcel.getParentId());
            if (parent != null) {
                parcel.setOwnerName("[" + parent.getType().getLabel() + "] " + parent.getName());
                return;
            }
        }
        parcel.setOwnerName("[Libre]");
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
        save();
    }

    // ==================== HIERARCHY VALIDATION ====================

    public static boolean isValidParent(String parentId, ParcelType childType) {
        if (parentId == null) return childType == ParcelType.KINGDOM;
        ParcelData parent = get(parentId);
        if (parent == null) return false;

        return switch (childType) {
            case KINGDOM -> false;
            case TERRITORY -> parent.getType() == ParcelType.KINGDOM || parent.getType() == ParcelType.TERRITORY;
            case CITY -> parent.getType() == ParcelType.TERRITORY;
            case PLOT -> parent.getType() == ParcelType.CITY;
            case FARM -> parent.getType() == ParcelType.CITY || parent.getType() == ParcelType.TERRITORY;
        };
    }

    // ==================== PERSISTENCE ====================

    public static void save() {
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
                props.setProperty(prefix + "members", p.serializeMembers());
                props.setProperty(prefix + "permissions", p.serializePermissions());
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
                p.deserializeMembers(props.getProperty(prefix + "members", ""));
                p.deserializePermissions(props.getProperty(prefix + "permissions", ""));

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
}
