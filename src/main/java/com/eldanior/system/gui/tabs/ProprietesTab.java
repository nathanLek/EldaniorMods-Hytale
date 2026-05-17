package com.eldanior.system.gui.tabs;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.Player.PlayerPositionTracker;
import com.eldanior.system.territory.*;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.*;

public class ProprietesTab {

    public static final int MAX_OWNED_SLOTS = 8;
    public static final int MAX_AVAILABLE_SLOTS = 5;
    public static final int MAX_DETAIL_MEMBERS = 6;
    public static final int MAX_INVITE_SLOTS = 4;
    public static final int MAX_CITY_SLOTS = 5;

    private static final List<String> cachedOwnedIds = new ArrayList<>();
    private static final List<String> cachedAvailableIds = new ArrayList<>();
    private static final List<String> cachedCityIds = new ArrayList<>();
    private static final List<String> cachedInviteNames = new ArrayList<>();
    private static int selectedIndex = -1;
    private static String selectedParcelId = null;

    // Pagination
    private static int ownedPage = 0;
    private static int availablePage = 0;
    private static List<ParcelData> allOwned = new ArrayList<>();
    private static List<ParcelData> allAvailable = new ArrayList<>();

    public static void populate(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        UUID myUUID = null;
        try {
            PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
            if (pRef != null) {
                                myUUID = UUIDExtractor.getUUID(pRef);
            }
        } catch (Exception e) { EldaniorLogger.error("ProprietesTab", e); }

        cachedOwnedIds.clear();
        cachedAvailableIds.clear();

        // === MES BIENS : seulement PLOT, HOUSING ===
        List<ParcelData> owned = new ArrayList<>();
        UUID finalUUID = myUUID;
        for (ParcelData p : ParcelManager.getAll()) {
            if (p.getType() != ParcelType.PLOT && p.getType() != ParcelType.HOUSING) continue;
            if (finalUUID != null && (p.isOwner(finalUUID) || p.isMember(finalUUID)
                    || (p.getRenterUUID() != null && p.getRenterUUID().equals(finalUUID)))) {
                owned.add(p);
            }
        }
        owned.sort(Comparator.comparingInt(p -> p.getType().ordinal()));
        allOwned = owned;

        int ownedTotalPages = Math.max(1, (int) Math.ceil((double) owned.size() / MAX_OWNED_SLOTS));
        if (ownedPage >= ownedTotalPages) ownedPage = ownedTotalPages - 1;
        if (ownedPage < 0) ownedPage = 0;
        int ownedStart = ownedPage * MAX_OWNED_SLOTS;

        ui.set("#PropInfoLabel.Text", "MES BIENS (" + owned.size() + ") — Page " + (ownedPage + 1) + "/" + ownedTotalPages);
        ui.set("#POwnPrev.Visible", ownedPage > 0);
        ui.set("#POwnNext.Visible", ownedPage < ownedTotalPages - 1);

        for (int i = 0; i < MAX_OWNED_SLOTS; i++) {
            int dataIdx = ownedStart + i;
            if (dataIdx < owned.size()) {
                ParcelData p = owned.get(dataIdx);
                cachedOwnedIds.add(p.getId());
                ui.set("#POwnSlot" + i + ".Visible", true);
                ui.set("#POwnType" + i + ".Text", "[" + p.getType().getLabel() + "]");
                ui.set("#POwnType" + i + ".Style.TextColor", getTypeColor(p));
                ui.set("#POwnName" + i + ".Text", p.getName());
                // Owner / locataire info
                String ownerDisplay;
                String ownerColor;
                if (p.isRented() && p.getRenterUUID() != null) {
                    String renterName = "???";
                    try {
                        PlayerRef rRef = Universe.get().getPlayer(p.getRenterUUID());
                        if (rRef != null) renterName = rRef.getUsername();
                    } catch (Exception e) { EldaniorLogger.error("ProprietesTab", e); }
                    ownerDisplay = "Loue a " + renterName;
                    ownerColor = "#ff9800";
                } else if (p.isForRent()) {
                    ownerDisplay = "En location";
                    ownerColor = "#ff9800";
                } else if (p.isForSale()) {
                    ownerDisplay = "En vente";
                    ownerColor = "#4CAF50";
                } else if (p.getOwnerName().isEmpty() || p.isFree()) {
                    ownerDisplay = "Libre";
                    ownerColor = "#cc4444";
                } else {
                    ownerDisplay = p.getOwnerName();
                    ownerColor = "#8899aa";
                }
                ui.set("#POwnOwner" + i + ".Text", ownerDisplay);
                ui.set("#POwnOwner" + i + ".Style.TextColor", ownerColor);

                // Prix
                String priceText = "";
                if (p.getPrice() > 0) priceText += p.getPrice() + " Or";
                if (p.getRentPrice() > 0) priceText += (priceText.isEmpty() ? "" : " / ") + p.getRentPrice() + "/7j";
                if (priceText.isEmpty() && p.isFree()) priceText = "Non configure";
                ui.set("#POwnPrice" + i + ".Text", priceText);

                // Highlight si selectionne
                boolean selected = (i == selectedIndex);
                ui.set("#POwnSlot" + i + ".Background", selected ? "#1a2a3a" : "#111825(0.7)");
            } else {
                cachedOwnedIds.add("");
                ui.set("#POwnSlot" + i + ".Visible", false);
            }
        }

        // === PANNEAU DETAIL ===
        populateDetail(ui, myUUID);

        // === DISPONIBLES ===
        List<ParcelData> available = ParcelManager.getAvailable();
        available.sort(Comparator.comparingLong(ParcelData::getPrice));
        allAvailable = available;

        int availTotalPages = Math.max(1, (int) Math.ceil((double) available.size() / MAX_AVAILABLE_SLOTS));
        if (availablePage >= availTotalPages) availablePage = availTotalPages - 1;
        if (availablePage < 0) availablePage = 0;
        int availStart = availablePage * MAX_AVAILABLE_SLOTS;

        ui.set("#PropAvailLabel.Text", "MARCHE IMMOBILIER (" + available.size() + ") — Page " + (availablePage + 1) + "/" + availTotalPages);
        ui.set("#PAvPrev.Visible", availablePage > 0);
        ui.set("#PAvNext.Visible", availablePage < availTotalPages - 1);

        for (int i = 0; i < MAX_AVAILABLE_SLOTS; i++) {
            int dataIdx = availStart + i;
            if (dataIdx < available.size()) {
                ParcelData p = available.get(dataIdx);
                cachedAvailableIds.add(p.getId());
                ui.set("#PAvSlot" + i + ".Visible", true);
                ui.set("#PAvType" + i + ".Text", "[" + p.getType().getLabel() + "]");
                ui.set("#PAvType" + i + ".Style.TextColor", getTypeColor(p));

                // Nom + localisation (ville > territoire > royaume)
                String location = p.getName();
                ParcelData parentCity = ParcelEconomyManager.findParentOfType(p, ParcelType.CITY);
                ParcelData parentTerr = ParcelEconomyManager.findParentOfType(p, ParcelType.TERRITORY);
                ParcelData parentKing = ParcelEconomyManager.findParentOfType(p, ParcelType.KINGDOM);
                if (parentCity != null) {
                    location += " - " + parentCity.getName();
                } else if (parentTerr != null) {
                    location += " - " + parentTerr.getName();
                } else if (parentKing != null) {
                    location += " - " + parentKing.getName();
                }
                ui.set("#PAvName" + i + ".Text", location);

                // Proprio actuel
                String sellerName = p.getOwnerName().isEmpty() ? "Ville" : p.getOwnerName();
                ui.set("#PAvOwner" + i + ".Text", sellerName);

                // Prix fixes
                ui.set("#PAvPrice" + i + ".Text", p.isForSale() && p.getPrice() > 0 ? p.getPrice() + " Or" : "-");
                ui.set("#PAvRent" + i + ".Text", p.isForRent() && p.getRentPrice() > 0 ? p.getRentPrice() + " Or" : "-");

                // Ne pas afficher acheter/louer si c'est le proprio
                UUID finalMyUUID = myUUID;
                boolean isMyProperty = finalMyUUID != null && p.getOwnerUUID() != null && p.getOwnerUUID().equals(finalMyUUID);
                ui.set("#PAvBtnBuy" + i + ".Visible", p.isForSale() && p.getPrice() > 0 && !isMyProperty);
                ui.set("#PAvBtnRent" + i + ".Visible", p.isForRent() && p.getRentPrice() > 0 && !isMyProperty);
            } else {
                cachedAvailableIds.add("");
                ui.set("#PAvSlot" + i + ".Visible", false);
            }
        }

        // === VILLES DISPONIBLES (Comte + Chef de guilde uniquement) ===
        PlayerLevelData playerData = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
        com.eldanior.system.titles.nobility.NobilityRank playerRank = playerData != null
                ? com.eldanior.system.titles.nobility.NobilityRank.fromString(playerData.getNobilityRank()) : null;
        boolean isComteOrHigher = playerRank != null && playerRank.ordinal() >= com.eldanior.system.titles.nobility.NobilityRank.COMTE.ordinal();
        boolean isGuildLeader = playerData != null && "CHEF".equalsIgnoreCase(playerData.getGuildRole());
        boolean canBuyCity = isComteOrHigher && isGuildLeader;

        ui.set("#CitySection.Visible", canBuyCity);
        cachedCityIds.clear();

        if (canBuyCity) {
            List<ParcelData> cities = ParcelManager.getAvailableCities();
            ui.set("#CityLabel.Text", "VILLES DISPONIBLES (" + cities.size() + ") — " + String.format("%,d", ParcelManager.CITY_PRICE) + " Or");

            for (int i = 0; i < MAX_CITY_SLOTS; i++) {
                if (i < cities.size()) {
                    ParcelData city = cities.get(i);
                    cachedCityIds.add(city.getId());
                    ui.set("#CitySlot" + i + ".Visible", true);
                    ui.set("#CityName" + i + ".Text", city.getName());

                    // Localisation (territoire parent)
                    ParcelData parentTerr = ParcelEconomyManager.findParentOfType(city, ParcelType.TERRITORY);
                    ParcelData parentGrand = ParcelEconomyManager.findParentOfType(city, ParcelType.GRAND_TERRITORY);
                    String loc = "";
                    if (parentTerr != null) loc = parentTerr.getName();
                    else if (parentGrand != null) loc = parentGrand.getName();
                    ui.set("#CityLoc" + i + ".Text", loc.isEmpty() ? "" : loc);
                    ui.set("#CityPrice" + i + ".Text", String.format("%,d", ParcelManager.CITY_PRICE) + " Or");
                } else {
                    cachedCityIds.add("");
                    ui.set("#CitySlot" + i + ".Visible", false);
                }
            }
        }
    }

    // === PANNEAU DETAIL D'UNE PARCELLE SELECTIONNEE ===
    private static void populateDetail(UICommandBuilder ui, UUID myUUID) {
        if (selectedParcelId == null) {
            ui.set("#PropDetail.Visible", false);
            return;
        }

        ParcelData p = ParcelManager.get(selectedParcelId);
        if (p == null) {
            ui.set("#PropDetail.Visible", false);
            selectedParcelId = null;
            selectedIndex = -1;
            return;
        }

        ui.set("#PropDetail.Visible", true);

        // Infos
        ui.set("#PDetName.Text", p.getName());
        ui.set("#PDetName.Style.TextColor", getTypeColor(p));
        ui.set("#PDetType.Text", p.getType().getLabel());

        // Proprio
        String ownerText = p.getOwnerName().isEmpty() ? "Aucun (Libre)" : p.getOwnerName();
        ui.set("#PDetOwner.Text", "Proprietaire : " + ownerText);
        ui.set("#PDetOwner.Style.TextColor", p.getOwnerName().isEmpty() ? "#cc4444" : "#ddeeff");

        // Locataire
        String renterName = "";
        if (p.getRenterUUID() != null) {
            try {
                PlayerRef rRef = Universe.get().getPlayer(p.getRenterUUID());
                renterName = rRef != null ? rRef.getUsername() : p.getRenterUUID().toString().substring(0, 8);
            } catch (Exception e) { renterName = "???"; }
        }
        ui.set("#PDetRenter.Text", p.isRented() ? "Locataire : " + renterName : "");
        ui.set("#PDetRenter.Visible", p.isRented());

        // Location expiration
        if (p.isRented() && p.getRentEndTime() > 0) {
            long remaining = p.getRentEndTime() - System.currentTimeMillis();
            if (remaining > 0) {
                long days = remaining / (24 * 60 * 60 * 1000);
                long hours = (remaining % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
                ui.set("#PDetExpiry.Text", "Expire dans " + days + "j " + hours + "h");
                ui.set("#PDetExpiry.Style.TextColor", days <= 1 ? "#cc4444" : "#ff9800");
            } else {
                ui.set("#PDetExpiry.Text", "EXPIRE - En attente de paiement");
                ui.set("#PDetExpiry.Style.TextColor", "#cc4444");
            }
            ui.set("#PDetExpiry.Visible", true);
        } else {
            ui.set("#PDetExpiry.Visible", false);
        }

        int sX = p.getMaxX() - p.getMinX() + 1;
        int sY = p.getMaxY() - p.getMinY() + 1;
        int sZ = p.getMaxZ() - p.getMinZ() + 1;
        ui.set("#PDetSize.Text", "Taille : " + sX + " x " + sZ + " (" + (sX * sZ) + " blocs)  |  Hauteur : " + sY);
        ui.set("#PDetFamily.Text", p.getFamilyId().isEmpty() ? "Famille : Aucune" : "Famille : " + p.getFamilyId());
        ui.set("#PDetProt.Text", p.isProtectedByDefault() ? "ZONE PROTEGEE" : "ZONE OUVERTE");
        ui.set("#PDetProt.Style.TextColor", p.isProtectedByDefault() ? "#4CAF50" : "#cc4444");

        // Tresorerie (pour villes, territoires, royaumes)
        if (p.getType() == ParcelType.CITY || p.getType() == ParcelType.TERRITORY || p.getType() == ParcelType.KINGDOM) {
            ui.set("#PDetTreasury.Text", "Tresorerie : " + p.getTreasury() + " Or");
            ui.set("#PDetTreasury.Visible", true);
        } else {
            ui.set("#PDetTreasury.Visible", false);
        }

        String statusText = p.isBought() ? "Achete" : p.isRented() ? "En location" : p.isFree() ? "Libre" : "Attribue";
        String statusColor = p.isBought() ? "#4CAF50" : p.isRented() ? "#ff9800" : p.isFree() ? "#cc4444" : "#3498DB";
        ui.set("#PDetStatus.Text", "Statut : " + statusText);
        ui.set("#PDetStatus.Style.TextColor", statusColor);

        // Prix
        ui.set("#PDetPriceVal.Text", p.getPrice() > 0 ? p.getPrice() + " Or" : "Non defini");
        ui.set("#PDetRentVal.Text", p.getRentPrice() > 0 ? p.getRentPrice() + " Or/7j" : "Non defini");

        // Membres
        List<Map.Entry<UUID, ParcelRole>> members = new ArrayList<>(p.getMembers().entrySet());
        for (int i = 0; i < MAX_DETAIL_MEMBERS; i++) {
            if (i < members.size()) {
                UUID memberUUID = members.get(i).getKey();
                ParcelRole role = members.get(i).getValue();
                String memberName = "???";
                try {
                    PlayerRef mRef = Universe.get().getPlayer(memberUUID);
                    if (mRef != null) memberName = mRef.getUsername();
                } catch (Exception e) { EldaniorLogger.error("ProprietesTab", e); }
                ui.set("#PDetMSlot" + i + ".Visible", true);
                ui.set("#PDetMName" + i + ".Text", memberName);
                // Afficher LOCATAIRE si c'est le renter
                boolean isRenter = p.getRenterUUID() != null && p.getRenterUUID().equals(memberUUID);
                String roleText = isRenter ? "LOCATAIRE" : role.name();
                String roleColor = isRenter ? "#ff9800" : role == ParcelRole.OWNER ? "#FFD700" : role == ParcelRole.OFFICER ? "#3498DB" : "#8899aa";
                ui.set("#PDetMRole" + i + ".Text", roleText);
                ui.set("#PDetMRole" + i + ".Style.TextColor", roleColor);
            } else {
                ui.set("#PDetMSlot" + i + ".Visible", false);
            }
        }

        // Qui suis-je par rapport a cette parcelle ?
        boolean iAmOwner = myUUID != null && p.getOwnerUUID() != null && p.getOwnerUUID().equals(myUUID);
        boolean iAmRenter = myUUID != null && p.getRenterUUID() != null && p.getRenterUUID().equals(myUUID);
        boolean hasRenter = p.getRenterUUID() != null;

        // Boutons admin (caches — admin utilise /es admin)
        ui.set("#PDetBtnPrice.Visible", false);
        ui.set("#PDetBtnRent.Visible", false);
        ui.set("#PDetBtnFamily.Visible", false);
        ui.set("#PDetBtnProt.Visible", false);
        ui.set("#PDetBtnDel.Visible", false);

        // Boutons PROPRIO
        boolean canManage = iAmOwner;
        // REVENDRE / ANNULER VENTE (toggle)
        ui.set("#PDetBtnSell.Visible", canManage && !hasRenter && !p.isForRent());
        if (canManage && !hasRenter && !p.isForRent()) {
            ui.set("#PDetBtnSell.Text", p.isForSale() ? "ANNULER VENTE" : "REVENDRE");
        }
        // METTRE EN LOCATION / RETIRER LOCATION
        ui.set("#PDetBtnRentOut.Visible", canManage && !hasRenter && !p.isForSale() && !p.isForRent());
        ui.set("#PDetBtnCancelRent.Visible", canManage && p.isForRent() && !hasRenter);

        // Boutons LOCATAIRE
        ui.set("#PDetBtnQuit.Visible", iAmRenter); // Quitter location
        ui.set("#PDetBtnRenew.Visible", iAmRenter && p.getRentPrice() > 0); // Prolonger
        if (iAmRenter && p.getRentPrice() > 0) {
            ui.set("#PDetBtnRenew.Text", "PROLONGER (" + p.getRentPrice() + " Or)");
        }

        // Joueurs a proximite invitables
        cachedInviteNames.clear();
        List<UUID> nearbyPlayers = new ArrayList<>();
        for (Map.Entry<UUID, Vector3d> entry : PlayerPositionTracker.PLAYER_POSITIONS.entrySet()) {
            UUID otherUUID = entry.getKey();
            if (p.isMember(otherUUID)) continue; // Deja membre
            if (p.getRenterUUID() != null && p.getRenterUUID().equals(otherUUID)) continue;
            nearbyPlayers.add(otherUUID);
        }

        for (int i = 0; i < MAX_INVITE_SLOTS; i++) {
            if (i < nearbyPlayers.size()) {
                UUID otherUUID = nearbyPlayers.get(i);
                String invName = "???";
                try {
                    PlayerRef invRef = Universe.get().getPlayer(otherUUID);
                    if (invRef != null) invName = invRef.getUsername();
                } catch (Exception e) { EldaniorLogger.error("ProprietesTab", e); }
                cachedInviteNames.add(invName);
                ui.set("#PDetInv" + i + ".Visible", true);
                ui.set("#PDetInvName" + i + ".Text", invName);
            } else {
                cachedInviteNames.add("");
                ui.set("#PDetInv" + i + ".Visible", false);
            }
        }
    }

    // === PAGINATION ===

    public static boolean handleOwnedPrev() {
        if (ownedPage > 0) { ownedPage--; selectedIndex = -1; selectedParcelId = null; return true; }
        return false;
    }

    public static boolean handleOwnedNext() {
        int totalPages = Math.max(1, (int) Math.ceil((double) allOwned.size() / MAX_OWNED_SLOTS));
        if (ownedPage < totalPages - 1) { ownedPage++; selectedIndex = -1; selectedParcelId = null; return true; }
        return false;
    }

    public static boolean handleAvailablePrev() {
        if (availablePage > 0) { availablePage--; return true; }
        return false;
    }

    public static boolean handleAvailableNext() {
        int totalPages = Math.max(1, (int) Math.ceil((double) allAvailable.size() / MAX_AVAILABLE_SLOTS));
        if (availablePage < totalPages - 1) { availablePage++; return true; }
        return false;
    }

    // === HANDLERS ===

    public static boolean handleSelect(int index) {
        if (index < 0 || index >= cachedOwnedIds.size()) return false;
        String id = cachedOwnedIds.get(index);
        if (id == null || id.isEmpty()) return false;

        if (selectedIndex == index) {
            selectedIndex = -1;
            selectedParcelId = null;
        } else {
            selectedIndex = index;
            selectedParcelId = id;
        }
        return true;
    }

    public static boolean handleBuy(int index, Ref<EntityStore> ref, Store<EntityStore> store) {
        if (index < 0 || index >= cachedAvailableIds.size()) return false;
        String id = cachedAvailableIds.get(index);
        if (id == null || id.isEmpty()) return false;

        ParcelData parcel = ParcelManager.get(id);
        if (parcel == null || !parcel.isForSale()) return false;

        try {
            PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
            if (pRef == null) return false;
                        UUID buyerUUID = UUIDExtractor.getUUID(pRef);

            PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
            if (data == null || data.getMoney() < parcel.getPrice()) return false;

            long totalPrice = parcel.getPrice();
            long[] taxResult = ParcelEconomyManager.calculateTax(totalPrice);
            long netAmount = taxResult[0];
            long taxAmount = taxResult[1];

            // Prelever le montant total au joueur
            data.addMoney(-totalPrice);
            store.putComponent(ref, EldaniorSystem.get().getPlayerLevelDataType(), data);

            // Le net va au proprio (joueur) ou a la ville (si pas de proprio joueur)
            payOwnerOrCity(parcel, netAmount, "vente de " + parcel.getName());

            // Taxe va dans la hierarchie
            ParcelEconomyManager.distributeTax(id, taxAmount);

            return ParcelManager.buyParcel(id, buyerUUID, pRef.getUsername());
        } catch (Exception e) { return false; }
    }

    public static boolean handleRent(int index, Ref<EntityStore> ref, Store<EntityStore> store) {
        if (index < 0 || index >= cachedAvailableIds.size()) return false;
        String id = cachedAvailableIds.get(index);
        if (id == null || id.isEmpty()) return false;

        ParcelData parcel = ParcelManager.get(id);
        if (parcel == null || !parcel.isForRent()) return false;

        try {
            PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
            if (pRef == null) return false;
                        UUID renterUUID = UUIDExtractor.getUUID(pRef);

            PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
            if (data == null || data.getMoney() < parcel.getRentPrice()) return false;

            long totalRent = parcel.getRentPrice();
            long[] taxResult = ParcelEconomyManager.calculateTax(totalRent);
            long netAmount = taxResult[0];
            long taxAmount = taxResult[1];

            data.addMoney(-totalRent);
            store.putComponent(ref, EldaniorSystem.get().getPlayerLevelDataType(), data);

            // Le net va au proprio (joueur) ou a la ville
            payOwnerOrCity(parcel, netAmount, "location de " + parcel.getName());

            ParcelEconomyManager.distributeTax(id, taxAmount);

            return ParcelManager.rentParcel(id, renterUUID, pRef.getUsername());
        } catch (Exception e) { return false; }
    }

    public static boolean handleRenew(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (selectedParcelId == null) return false;
        ParcelData p = ParcelManager.get(selectedParcelId);
        if (p == null || !p.isRented() || p.getRentPrice() <= 0) return false;

        try {
            PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
            if (data == null || data.getMoney() < p.getRentPrice()) return false;

            long totalRent = p.getRentPrice();
            long[] taxResult = ParcelEconomyManager.calculateTax(totalRent);
            long taxAmount = taxResult[1];

            data.addMoney(-totalRent);
            store.putComponent(ref, EldaniorSystem.get().getPlayerLevelDataType(), data);

            // Le net va au proprio (joueur) ou a la ville
            payOwnerOrCity(p, taxResult[0], "prolongation location");

            ParcelEconomyManager.distributeTax(selectedParcelId, taxAmount);
            ParcelManager.renewRent(selectedParcelId);
            return true;
        } catch (Exception e) { return false; }
    }

    public static boolean handleRentOut() {
        if (selectedParcelId == null) return false;
        ParcelData p = ParcelManager.get(selectedParcelId);
        if (p == null) return false;
        // Met en location au prix actuel (ou 500 par defaut)
        long rentPrice = p.getRentPrice() > 0 ? p.getRentPrice() : 500;
        p.setRentPrice(rentPrice);
        p.setForRent(true);
        // Le proprio reste OWNER dans les membres
        if (p.getOwnerUUID() != null && p.getRole(p.getOwnerUUID()) == null) {
            p.addMember(p.getOwnerUUID(), ParcelRole.OWNER);
        }
        ParcelManager.save();
        return true;
    }

    public static boolean handleInvite(int index) {
        if (selectedParcelId == null) return false;
        if (index < 0 || index >= cachedInviteNames.size()) return false;
        String invName = cachedInviteNames.get(index);
        if (invName == null || invName.isEmpty() || "???".equals(invName)) return false;

        ParcelData p = ParcelManager.get(selectedParcelId);
        if (p == null) return false;

        try {
            PlayerRef targetRef = Universe.get().getPlayerByUsername(invName, com.hypixel.hytale.server.core.NameMatching.EXACT_IGNORE_CASE);
            if (targetRef == null) return false;
                        UUID targetUUID = UUIDExtractor.getUUID(targetRef);

            p.addMember(targetUUID, ParcelRole.MEMBER);
            ParcelManager.save();

            targetRef.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                    "§eVous avez ete invite dans la parcelle §f" + p.getName()));
            return true;
        } catch (Exception e) {
            System.err.println("[Proprietes] Erreur invite: " + e.getMessage());
            return false;
        }
    }

    public static boolean handleCancelRent() {
        if (selectedParcelId == null) return false;
        ParcelData p = ParcelManager.get(selectedParcelId);
        if (p == null || !p.isForRent() || p.getRenterUUID() != null) return false;
        p.setForRent(false);
        ParcelManager.save();
        return true;
    }

    public static boolean handleQuitRental() {
        if (selectedParcelId == null) return false;
        ParcelData p = ParcelManager.get(selectedParcelId);
        if (p == null || !p.isRented()) return false;
        ParcelManager.quitRental(selectedParcelId);
        selectedParcelId = null;
        selectedIndex = -1;
        return true;
    }

    public static boolean handleSellSelected() {
        if (selectedParcelId == null) return false;
        ParcelData p = ParcelManager.get(selectedParcelId);
        if (p == null) return false;
        // Toggle vente
        if (p.isForSale()) {
            p.setForSale(false); // Retirer de la vente
        } else {
            if (p.getPrice() <= 0) p.setPrice(1000); // Prix par defaut si pas configure
            p.setForSale(true);
            p.setForRent(false); // Pas vente ET location en meme temps
        }
        ParcelManager.save();
        return true;
    }

    public static boolean handleDeleteSelected() {
        if (selectedParcelId == null) return false;
        ParcelManager.deleteParcel(selectedParcelId);
        selectedParcelId = null;
        selectedIndex = -1;
        return true;
    }

    public static boolean handleToggleProtection() {
        if (selectedParcelId == null) return false;
        ParcelData p = ParcelManager.get(selectedParcelId);
        if (p == null) return false;
        p.setProtectedByDefault(!p.isProtectedByDefault());
        ParcelManager.save();
        return true;
    }

    public static boolean handleSetPrice(long price) {
        if (selectedParcelId == null) return false;
        ParcelData p = ParcelManager.get(selectedParcelId);
        if (p == null) return false;
        p.setPrice(price);
        // Ne PAS mettre en vente automatiquement — juste configurer le prix
        ParcelManager.save();
        return true;
    }

    public static boolean handleSetRentPrice(long rentPrice) {
        if (selectedParcelId == null) return false;
        ParcelData p = ParcelManager.get(selectedParcelId);
        if (p == null) return false;
        p.setRentPrice(rentPrice);
        // Ne PAS mettre en location automatiquement — juste configurer le prix
        ParcelManager.save();
        return true;
    }

    public static boolean handleAssignFamily(String familyId) {
        if (selectedParcelId == null) return false;
        ParcelManager.assignToFamily(selectedParcelId, familyId);
        return true;
    }

    public static boolean handleDelete(int index) {
        if (index < 0 || index >= cachedOwnedIds.size()) return false;
        String id = cachedOwnedIds.get(index);
        if (id == null || id.isEmpty()) return false;
        ParcelManager.deleteParcel(id);
        if (id.equals(selectedParcelId)) { selectedParcelId = null; selectedIndex = -1; }
        return true;
    }

    public static boolean handleSell(int index) {
        if (index < 0 || index >= cachedOwnedIds.size()) return false;
        String id = cachedOwnedIds.get(index);
        if (id == null || id.isEmpty()) return false;
        ParcelData p = ParcelManager.get(id);
        if (p == null || !p.isBought()) return false;
        ParcelManager.releaseParcel(id);
        return true;
    }

    /**
     * Envoie le montant net au proprio joueur, ou a la ville si pas de proprio joueur.
     */
    private static void payOwnerOrCity(ParcelData parcel, long netAmount, String reason) {
        if (netAmount <= 0) return;

        if (parcel.getOwnerUUID() != null) {
            // Proprio joueur recoit l'argent
            try {
                PlayerRef ownerRef = Universe.get().getPlayer(parcel.getOwnerUUID());
                if (ownerRef != null) {
                    var ownerEntRef = ownerRef.getReference();
                    if (ownerEntRef != null) {
                        PlayerLevelData ownerData = ownerEntRef.getStore().getComponent(ownerEntRef, EldaniorSystem.get().getPlayerLevelDataType());
                        if (ownerData != null) {
                            ownerData.addMoney(netAmount);
                            ownerEntRef.getStore().putComponent(ownerEntRef, EldaniorSystem.get().getPlayerLevelDataType(), ownerData);
                        }
                    }
                    ownerRef.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                            "§a+" + netAmount + " Or §7(" + reason + ")"));
                }
            } catch (Exception e) { EldaniorLogger.error("ProprietesTab", e); }
        } else {
            // Pas de proprio joueur -> l'argent va dans la tresorerie de la ville parente
            ParcelData city = ParcelEconomyManager.findParentOfType(parcel, ParcelType.CITY);
            if (city != null) {
                city.addTreasury(netAmount);
                System.out.println("[Economy] " + netAmount + " Or -> Tresorerie Ville " + city.getName() + " (" + reason + ")");
            } else {
                // Pas de ville, chercher territoire ou royaume
                ParcelData parent = parcel.getParentId() != null ? ParcelManager.get(parcel.getParentId()) : null;
                if (parent != null) {
                    parent.addTreasury(netAmount);
                    System.out.println("[Economy] " + netAmount + " Or -> Tresorerie " + parent.getName() + " (" + reason + ")");
                }
            }
            ParcelManager.save();
        }
    }

    public static boolean handleBuyCity(int index, Ref<EntityStore> ref, Store<EntityStore> store) {
        if (index < 0 || index >= cachedCityIds.size()) return false;
        String id = cachedCityIds.get(index);
        if (id == null || id.isEmpty()) return false;

        ParcelData city = ParcelManager.get(id);
        if (city == null || city.getType() != ParcelType.CITY || city.getOwnerUUID() != null) return false;

        try {
            PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
            if (pRef == null) return false;
            UUID buyerUUID = UUIDExtractor.getUUID(pRef);

            PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
            if (data == null) return false;

            // Verifier Comte + Chef de guilde
            com.eldanior.system.titles.nobility.NobilityRank rank =
                    com.eldanior.system.titles.nobility.NobilityRank.fromString(data.getNobilityRank());
            if (rank == null || rank.ordinal() < com.eldanior.system.titles.nobility.NobilityRank.COMTE.ordinal()) return false;
            if (!"CHEF".equalsIgnoreCase(data.getGuildRole())) return false;

            // Verifier argent
            if (data.getMoney() < ParcelManager.CITY_PRICE) {
                pRef.sendMessage(Message.raw("§cPas assez d'Or ! Il faut " + String.format("%,d", ParcelManager.CITY_PRICE) + " Or."));
                return false;
            }

            // Deduire l'argent
            data.removeMoney(ParcelManager.CITY_PRICE);
            store.putComponent(ref, EldaniorSystem.get().getPlayerLevelDataType(), data);

            // Assigner la ville au joueur
            com.hypixel.hytale.server.core.entity.entities.Player player = store.getComponent(ref,
                    com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
            String playerName = player != null ? player.getDisplayName() : "";
            city.setOwnerUUID(buyerUUID);
            city.setOwnerName(playerName);
            city.addMember(buyerUUID, ParcelRole.OWNER);

            // Associer la guilde a la ville
            city.setGuildId(data.getGuildId());

            ParcelManager.save();

            pRef.sendMessage(Message.raw("§a§lVille achetee : §f" + city.getName() + " §a§l! Associee a votre guilde."));
            return true;
        } catch (Exception e) {
            EldaniorLogger.error("ProprietesTab:buyCity", e);
            return false;
        }
    }

    private static String getTypeColor(ParcelData p) {
        return switch (p.getType()) {
            case KINGDOM -> "#FFD700";
            case GRAND_TERRITORY -> "#1E90FF";
            case TERRITORY -> "#3498DB";
            case CITY -> "#2ECC71";
            case PLOT -> "#aabbcc";
            case HOUSING -> "#9B59B6";
            case ROOM -> "#E91E63";
            case FARM -> "#8B4513";
            case FOREST -> "#228B22";
            case ARENA -> "#E74C3C";
            case DUNGEON -> "#8E44AD";
            case MINE -> "#F39C12";
        };
    }
}
