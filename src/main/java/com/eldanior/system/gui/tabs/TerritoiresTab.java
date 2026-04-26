package com.eldanior.system.gui.tabs;

import com.eldanior.system.territory.*;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.EldaniorSystem;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.lang.reflect.Field;
import java.util.*;

public class TerritoiresTab {

    public static final int MAX_TERR_SLOTS = 4;
    public static final int MAX_DETAIL_CHILDREN = 6;

    private static final List<String> cachedTerrIds = new ArrayList<>();
    private static int selectedIndex = -1;
    private static String selectedTerrId = null;

    public static void populate(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        String familyId = "";
        String guildId = "";
        boolean isAdmin = false;

        try {
            PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
            if (data != null) {
                familyId = data.getNobleFamilyId() != null ? data.getNobleFamilyId() : "";
                guildId = data.getGuildId() != null ? data.getGuildId() : "";
            }
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player != null) isAdmin = player.hasPermission("eldanior.command.setlevel");
        } catch (Exception ignored) {}

        List<ParcelData> territories = new ArrayList<>();

        if (isAdmin) {
            for (ParcelData p : ParcelManager.getAll()) {
                if (p.getType() == ParcelType.KINGDOM || p.getType() == ParcelType.TERRITORY || p.getType() == ParcelType.CITY) {
                    territories.add(p);
                }
            }
        } else {
            if (!familyId.isEmpty()) {
                for (ParcelData p : ParcelManager.getByFamily(familyId)) {
                    if (p.getType() == ParcelType.KINGDOM || p.getType() == ParcelType.TERRITORY || p.getType() == ParcelType.CITY) {
                        territories.add(p);
                    }
                }
            }
            if (!guildId.isEmpty()) {
                for (ParcelData p : ParcelManager.getAll()) {
                    if (guildId.equals(p.getGuildId()) && !territories.contains(p)) {
                        territories.add(p);
                    }
                }
            }
        }

        territories.sort(Comparator.comparingInt(p -> p.getType().ordinal()));

        cachedTerrIds.clear();
        ui.set("#TerrCount.Text", territories.size() + " TERRITOIRE(S)" + (isAdmin ? " - Admin" : ""));

        for (int i = 0; i < MAX_TERR_SLOTS; i++) {
            if (i < territories.size()) {
                ParcelData p = territories.get(i);
                cachedTerrIds.add(p.getId());
                ui.set("#TerrSlot" + i + ".Visible", true);

                String typeColor = switch (p.getType()) {
                    case KINGDOM -> "#FFD700";
                    case TERRITORY -> "#3498DB";
                    case CITY -> "#2ECC71";
                    default -> "#8899aa";
                };
                ui.set("#TerrType" + i + ".Text", "[" + p.getType().getLabel() + "]");
                ui.set("#TerrType" + i + ".Style.TextColor", typeColor);
                ui.set("#TerrName" + i + ".Text", p.getName());
                ui.set("#TerrTreasury" + i + ".Text", p.getTreasury() + " Or");

                String locationText = "";
                if (p.getType() == ParcelType.CITY) {
                    ParcelData pt = ParcelEconomyManager.findParentOfType(p, ParcelType.TERRITORY);
                    if (pt != null) locationText = "Dans : " + pt.getName();
                } else if (p.getType() == ParcelType.TERRITORY) {
                    ParcelData pk = ParcelEconomyManager.findParentOfType(p, ParcelType.KINGDOM);
                    if (pk != null) locationText = "Dans : " + pk.getName();
                }
                ui.set("#TerrLocation" + i + ".Text", locationText);
                ui.set("#TerrFamily" + i + ".Text", !p.getFamilyId().isEmpty() ? "Famille : " + p.getFamilyId() : "");

                // Stats boxes
                int[] counts = countAllChildren(p);
                ui.set("#TerrBoxTerr" + i + ".Visible", counts[0] > 0);
                ui.set("#TerrCountTerr" + i + ".Text", String.valueOf(counts[0]));
                ui.set("#TerrBoxCity" + i + ".Visible", counts[1] > 0);
                ui.set("#TerrCountCity" + i + ".Text", String.valueOf(counts[1]));
                ui.set("#TerrBoxHousing" + i + ".Visible", counts[2] > 0);
                ui.set("#TerrCountHousing" + i + ".Text", String.valueOf(counts[2]));
                ui.set("#TerrBoxPlot" + i + ".Visible", counts[3] > 0);
                ui.set("#TerrCountPlot" + i + ".Text", String.valueOf(counts[3]));
                ui.set("#TerrPopulation" + i + ".Text", String.valueOf(countPopulation(p)));
                ui.set("#TerrProt" + i + ".Text", p.isProtectedByDefault() ? "Protege" : "Ouvert");
                ui.set("#TerrProt" + i + ".Style.TextColor", p.isProtectedByDefault() ? "#4CAF50" : "#cc4444");

                // Highlight selected
                boolean sel = (i == selectedIndex);
                ui.set("#TerrSlot" + i + ".Style.Default.Background", sel ? "#1a2a3a" : "#0d1520");
            } else {
                cachedTerrIds.add("");
                ui.set("#TerrSlot" + i + ".Visible", false);
            }
        }

        // Detail panel
        populateDetail(ui, isAdmin);

        ui.set("#TerrInfoLabel.Text", territories.isEmpty() ? "Aucun territoire sous votre gestion." : "");
    }

    // === PANNEAU DETAIL ===
    private static void populateDetail(UICommandBuilder ui, boolean isAdmin) {
        if (selectedTerrId == null) {
            ui.set("#TerrDetail.Visible", false);
            return;
        }

        ParcelData p = ParcelManager.get(selectedTerrId);
        if (p == null) {
            ui.set("#TerrDetail.Visible", false);
            selectedTerrId = null;
            selectedIndex = -1;
            return;
        }

        ui.set("#TerrDetail.Visible", true);

        // Header
        ui.set("#TDetName.Text", p.getName());
        String typeColor = switch (p.getType()) {
            case KINGDOM -> "#FFD700";
            case TERRITORY -> "#3498DB";
            case CITY -> "#2ECC71";
            default -> "#8899aa";
        };
        ui.set("#TDetName.Style.TextColor", typeColor);
        ui.set("#TDetType.Text", p.getType().getLabel());

        // Infos
        ui.set("#TDetTreasury.Text", "Tresorerie : " + p.getTreasury() + " Or");
        ui.set("#TDetFamily.Text", !p.getFamilyId().isEmpty() ? "Famille : " + p.getFamilyId() : "Famille : Aucune");
        ui.set("#TDetGuild.Text", !p.getGuildId().isEmpty() ? "Guilde : " + p.getGuildId() : "");
        ui.set("#TDetProt.Text", p.isProtectedByDefault() ? "ZONE PROTEGEE" : "ZONE OUVERTE");
        ui.set("#TDetProt.Style.TextColor", p.isProtectedByDefault() ? "#4CAF50" : "#cc4444");

        int sX = p.getMaxX() - p.getMinX() + 1;
        int sZ = p.getMaxZ() - p.getMinZ() + 1;
        ui.set("#TDetSize.Text", "Taille : " + sX + " x " + sZ + " blocs");

        int population = countPopulation(p);
        ui.set("#TDetPopulation.Text", "Population : " + population + " habitant(s)");

        // PvP
        ui.set("#TDetPvp.Text", "PvP : " + (p.isPvpEnabled() ? "ACTIVE" : "DESACTIVE"));
        ui.set("#TDetPvp.Style.TextColor", p.isPvpEnabled() ? "#cc4444" : "#4CAF50");

        // Infos economiques
        ui.set("#TDetTaxRate.Text", "Taxe : 12% sur transactions");
        if (p.getLastTaxCollection() > 0) {
            long nextTax = (p.getLastTaxCollection() + 7L * 24 * 60 * 60 * 1000) - System.currentTimeMillis();
            if (nextTax > 0) {
                long days = nextTax / (24 * 60 * 60 * 1000);
                long hours = (nextTax % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
                ui.set("#TDetNextTax.Text", "Prochain impot : " + days + "j " + hours + "h");
            } else {
                ui.set("#TDetNextTax.Text", "Impots reclamables !");
                ui.set("#TDetNextTax.Style.TextColor", "#4CAF50");
            }
        } else {
            ui.set("#TDetNextTax.Text", "Aucun impot collecte");
        }
        if (p.getLastTaxAmount() > 0) {
            ui.set("#TDetLastTax.Text", "Dernier impot : " + p.getLastTaxAmount() + " Or");
        } else {
            ui.set("#TDetLastTax.Text", "");
        }

        // Liste des sous-zones directes
        List<String> childIds = p.getChildIds();
        for (int i = 0; i < MAX_DETAIL_CHILDREN; i++) {
            if (i < childIds.size()) {
                ParcelData child = ParcelManager.get(childIds.get(i));
                if (child == null) { ui.set("#TDetChild" + i + ".Visible", false); continue; }
                ui.set("#TDetChild" + i + ".Visible", true);
                ui.set("#TDetChildType" + i + ".Text", "[" + child.getType().getLabel() + "]");
                String childColor = switch (child.getType()) {
                    case TERRITORY -> "#3498DB";
                    case CITY -> "#2ECC71";
                    case PLOT -> "#aabbcc";
                    case HOUSING -> "#9B59B6";
                    default -> "#8899aa";
                };
                ui.set("#TDetChildType" + i + ".Style.TextColor", childColor);
                ui.set("#TDetChildName" + i + ".Text", child.getName());
                ui.set("#TDetChildOwner" + i + ".Text", child.getOwnerName().isEmpty() ? "-" : child.getOwnerName());
                ui.set("#TDetChildTreasury" + i + ".Text", child.getTreasury() > 0 ? child.getTreasury() + " Or" : "");
            } else {
                ui.set("#TDetChild" + i + ".Visible", false);
            }
        }

        // Boutons gestion (proprio + admin)
        ui.set("#TDetBtnPvp.Visible", true);
        ui.set("#TDetBtnPvp.Text", p.isPvpEnabled() ? "DESACTIVER PVP" : "ACTIVER PVP");
        ui.set("#TDetBtnTax.Visible", p.canCollectTax() && !p.getChildIds().isEmpty());
        ui.set("#TDetBtnTransfer.Visible", p.canTransferTreasury() && p.getTreasury() > 0
                && (!p.getFamilyId().isEmpty() || !p.getGuildId().isEmpty()));
        if (p.canTransferTreasury() && p.getTreasury() > 0) {
            ui.set("#TDetBtnTransfer.Text", "TRANSFERER " + (p.getTreasury() / 2) + " Or");
        }

        // Boutons admin
        ui.set("#TDetBtnProt.Visible", isAdmin);
        ui.set("#TDetBtnFamily.Visible", isAdmin);
        ui.set("#TDetBtnGuild.Visible", isAdmin && p.getType() == ParcelType.CITY);
        ui.set("#TDetBtnInvasion.Visible", isAdmin);
        ui.set("#TDetBtnDel.Visible", isAdmin);
    }

    // === HANDLERS ===

    public static boolean handleSelect(int index) {
        if (index < 0 || index >= cachedTerrIds.size()) return false;
        String id = cachedTerrIds.get(index);
        if (id == null || id.isEmpty()) return false;
        if (selectedIndex == index) {
            selectedIndex = -1;
            selectedTerrId = null;
        } else {
            selectedIndex = index;
            selectedTerrId = id;
        }
        return true;
    }

    public static boolean handleTogglePvp() {
        if (selectedTerrId == null) return false;
        ParcelData p = ParcelManager.get(selectedTerrId);
        if (p == null) return false;
        p.setPvpEnabled(!p.isPvpEnabled());
        ParcelManager.save();
        return true;
    }

    public static boolean handleCollectTax() {
        if (selectedTerrId == null) return false;
        ParcelData p = ParcelManager.get(selectedTerrId);
        if (p == null) return false;
        if (!p.canCollectTax()) return false;

        // Collecter 10% de la tresorerie des sous-zones directes
        long totalCollected = 0;
        for (String childId : p.getChildIds()) {
            ParcelData child = ParcelManager.get(childId);
            if (child == null || child.getTreasury() <= 0) continue;
            long amount = child.getTreasury() / 10; // 10%
            child.addTreasury(-amount);
            totalCollected += amount;
        }
        p.addTreasury(totalCollected);
        p.setLastTaxCollection(System.currentTimeMillis());
        p.setLastTaxAmount(totalCollected);
        ParcelManager.save();
        return true;
    }

    public static boolean handleTransferTreasury() {
        if (selectedTerrId == null) return false;
        ParcelData p = ParcelManager.get(selectedTerrId);
        if (p == null) return false;
        if (!p.canTransferTreasury()) return false;
        if (p.getTreasury() <= 0) return false;

        long transferAmount = p.getTreasury() / 2; // 50%
        if (transferAmount <= 0) return false;

        boolean transferred = false;

        // Guilde
        if (!p.getGuildId().isEmpty()) {
            try {
                com.eldanior.system.guild.Guild guild = com.eldanior.system.guild.GuildManager.get(p.getGuildId());
                if (guild != null) {
                    guild.addTreasury(transferAmount);
                    p.addTreasury(-transferAmount);
                    transferred = true;
                }
            } catch (Exception ignored) {}
        }

        // Famille
        if (!transferred && !p.getFamilyId().isEmpty()) {
            try {
                var runtimeData = com.eldanior.system.titles.nobility.family.FamilyManager.getRuntimeData(p.getFamilyId());
                if (runtimeData != null) {
                    runtimeData.addTreasury(transferAmount);
                    p.addTreasury(-transferAmount);
                    transferred = true;
                }
            } catch (Exception ignored) {}
        }

        if (transferred) {
            p.setLastTreasuryTransfer(System.currentTimeMillis());
            ParcelManager.save();
        }
        return transferred;
    }

    public static boolean handleToggleProtection() {
        if (selectedTerrId == null) return false;
        ParcelData p = ParcelManager.get(selectedTerrId);
        if (p == null) return false;
        p.setProtectedByDefault(!p.isProtectedByDefault());
        ParcelManager.save();
        return true;
    }

    public static boolean handleDelete() {
        if (selectedTerrId == null) return false;
        ParcelManager.deleteParcel(selectedTerrId);
        selectedTerrId = null;
        selectedIndex = -1;
        return true;
    }

    // === UTILS ===

    private static int[] countAllChildren(ParcelData parent) {
        int[] counts = new int[4];
        countChildrenRecursive(parent.getId(), counts, 0);
        return counts;
    }

    private static void countChildrenRecursive(String parentId, int[] counts, int depth) {
        if (depth > 5) return;
        for (String childId : ParcelManager.getChildrenOf(parentId)) {
            ParcelData child = ParcelManager.get(childId);
            if (child == null) continue;
            switch (child.getType()) {
                case TERRITORY -> counts[0]++;
                case CITY -> counts[1]++;
                case HOUSING, ROOM -> counts[2]++;
                case PLOT -> counts[3]++;
                default -> {}
            }
            countChildrenRecursive(childId, counts, depth + 1);
        }
    }

    private static int countPopulation(ParcelData parent) {
        Set<UUID> owners = new HashSet<>();
        countOwnersRecursive(parent.getId(), owners, 0);
        return owners.size();
    }

    private static void countOwnersRecursive(String parentId, Set<UUID> owners, int depth) {
        if (depth > 5) return;
        for (String childId : ParcelManager.getChildrenOf(parentId)) {
            ParcelData child = ParcelManager.get(childId);
            if (child == null) continue;
            if (child.getOwnerUUID() != null) owners.add(child.getOwnerUUID());
            if (child.getRenterUUID() != null) owners.add(child.getRenterUUID());
            countOwnersRecursive(childId, owners, depth + 1);
        }
    }
}
