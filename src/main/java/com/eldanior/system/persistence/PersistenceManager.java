package com.eldanior.system.persistence;

import com.eldanior.system.classement.ClassementManager;
import com.eldanior.system.guild.Guild;
import com.eldanior.system.guild.GuildManager;
import com.eldanior.system.shop.ShopManager;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.PersistenceUtils;
import com.hypixel.hytale.server.core.inventory.ItemStack;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PersistenceManager {

    private static Path dataDir;
    private static java.util.Timer autoSaveTimer;

    public static void init(Path pluginDataDir) {
        dataDir = pluginDataDir.resolve("eldanior_data");
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            System.err.println("[Persistence] Impossible de creer le dossier: " + e.getMessage());
        }
        load();

        // Donner le chemin de donnees au QuestManager pour la persistence fichier
        com.eldanior.system.quest.QuestManager.setDataDir(dataDir);

        // Autosave toutes les 5 minutes
        autoSaveTimer = new java.util.Timer("EldaniorAutoSave", true);
        autoSaveTimer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                saveAll();
            }
        }, 5 * 60 * 1000L, 5 * 60 * 1000L);

        System.out.println("[Eldanior] Persistence initialisee (" + dataDir + ") - Autosave: 5min");
    }

    public static void shutdown() {
        if (autoSaveTimer != null) {
            autoSaveTimer.cancel();
            autoSaveTimer = null;
        }
    }

    // ==================== SAVE ====================

    public static void saveAll() {
        try {
            saveGuilds();
            saveFamilies();
            saveClassements();
            saveShop();
            saveHierarchies();
            // Sauvegarder quetes et cooldowns en memoire (filet de securite shutdown)
            com.eldanior.system.quest.QuestManager.saveAllToFile();
            // Verifier les impots hebdomadaires
            com.eldanior.system.territory.ParcelEconomyManager.collectWeeklyTaxes();
            com.eldanior.system.territory.ParcelManager.saveAll();
            System.out.println("[Persistence] Donnees sauvegardees !");
        } catch (Exception e) {
            System.err.println("[Persistence] Erreur de sauvegarde: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void saveGuilds() throws IOException {
        Properties props = new Properties();
        int count = 0;
        for (Guild guild : GuildManager.getAll()) {
            String prefix = guild.getId();
            props.setProperty(prefix + ".name", guild.getName());
            props.setProperty(prefix + ".tag", guild.getTag());
            props.setProperty(prefix + ".founder", guild.getFounderUUID().toString());
            props.setProperty(prefix + ".founderName", guild.getFounderName());
            props.setProperty(prefix + ".treasury", String.valueOf(guild.getTreasury()));
            props.setProperty(prefix + ".contribution", String.valueOf(guild.getContribution()));
            props.setProperty(prefix + ".mobKills", String.valueOf(guild.getTotalMobKills()));
            props.setProperty(prefix + ".pvpKills", String.valueOf(guild.getTotalPlayerKills()));
            props.setProperty(prefix + ".deaths", String.valueOf(guild.getTotalDeaths()));

            // Members as comma-separated UUIDs
            StringBuilder members = new StringBuilder();
            for (UUID uuid : guild.getMembers()) {
                if (members.length() > 0) members.append(",");
                members.append(uuid.toString());
            }
            props.setProperty(prefix + ".members", members.toString());
            count++;
        }
        props.setProperty("_count", String.valueOf(count));

        PersistenceUtils.writeAtomicWithBackup(dataDir.resolve("guilds.properties"), props, "Guild data (full persistence)");
    }

    private static void saveFamilies() throws IOException {
        Properties props = new Properties();
        for (NobleFamilyModel family : FamilyManager.getAll()) {
            FamilyManager.FamilyRuntimeData runtime = FamilyManager.getRuntimeData(family.getId());
            props.setProperty(family.getId() + ".treasury", String.valueOf(runtime.getTreasury()));
            props.setProperty(family.getId() + ".contribution", String.valueOf(runtime.getContribution()));
        }
        // Sauvegarder les familles prises
        StringBuilder taken = new StringBuilder();
        for (NobleFamilyModel family : FamilyManager.getAll()) {
            if (FamilyManager.isFamilyTaken(family.getId())) {
                if (taken.length() > 0) taken.append(",");
                taken.append(family.getId());
            }
        }
        props.setProperty("_takenFamilies", taken.toString());
        PersistenceUtils.writeAtomicWithBackup(dataDir.resolve("families.properties"), props, "Family treasury & contribution data");
    }

    private static void saveClassements() throws IOException {
        Properties props = new Properties();
        for (var entry : ClassementManager.getMobRanking(200)) {
            props.setProperty("mob." + entry.name(), String.valueOf(entry.value()));
        }
        for (var entry : ClassementManager.getPvPRanking(200)) {
            props.setProperty("pvp." + entry.name(), String.valueOf(entry.value()));
        }
        for (var entry : ClassementManager.getDuelRanking(200)) {
            props.setProperty("duel." + entry.name(), String.valueOf(entry.value()));
        }
        PersistenceUtils.writeAtomicWithBackup(dataDir.resolve("classements.properties"), props, "Classement data");
    }

    // ==================== LOAD ====================

    public static void load() {
        try { loadShop(); } catch (Exception e) {
            System.out.println("[Persistence] Shop: " + e.getMessage());
        }
        try { loadGuilds(); } catch (Exception e) {
            System.out.println("[Persistence] Guildes: " + e.getMessage());
        }
        try { loadFamilies(); } catch (Exception e) {
            System.out.println("[Persistence] Familles: " + e.getMessage());
        }
        try { loadClassements(); } catch (Exception e) {
            System.out.println("[Persistence] Classements: " + e.getMessage());
        }
        try { loadHierarchies(); } catch (Exception e) {
            System.out.println("[Persistence] Hierarchies: " + e.getMessage());
        }
    }

    private static void loadGuilds() throws IOException {
        Path file = dataDir.resolve("guilds.properties");
        if (!Files.exists(file)) return;

        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            props.load(in);
        }

        // Reconstruire les guildes depuis le fichier
        Set<String> guildIds = new HashSet<>();
        for (String key : props.stringPropertyNames()) {
            if (key.contains(".") && !key.startsWith("_")) {
                guildIds.add(key.substring(0, key.indexOf('.')));
            }
        }

        for (String guildId : guildIds) {
            String name = props.getProperty(guildId + ".name");
            String tag = props.getProperty(guildId + ".tag");
            String founderStr = props.getProperty(guildId + ".founder");
            String founderName = props.getProperty(guildId + ".founderName");

            if (name == null || tag == null || founderStr == null) continue;

            // Ne pas recreer si elle existe deja
            if (GuildManager.get(guildId) != null) {
                Guild existing = GuildManager.get(guildId);
                existing.setTreasury(Long.parseLong(props.getProperty(guildId + ".treasury", "0")));
                existing.setContribution(Long.parseLong(props.getProperty(guildId + ".contribution", "0")));
                continue;
            }

            UUID founderUUID = UUID.fromString(founderStr);
            Guild guild = GuildManager.createGuild(name, tag, founderUUID, founderName != null ? founderName : "?");

            guild.setTreasury(Long.parseLong(props.getProperty(guildId + ".treasury", "0")));
            guild.setContribution(Long.parseLong(props.getProperty(guildId + ".contribution", "0")));

            // Restaurer les membres
            String membersStr = props.getProperty(guildId + ".members", "");
            if (!membersStr.isEmpty()) {
                for (String uuidStr : membersStr.split(",")) {
                    try {
                        UUID memberUUID = UUID.fromString(uuidStr.trim());
                        if (!memberUUID.equals(founderUUID)) {
                            GuildManager.joinGuild(memberUUID, guild);
                        }
                    } catch (Exception e) { EldaniorLogger.error("PersistenceManager", e); }
                }
            }

            System.out.println("[Persistence] Guilde restauree: " + name + " [" + tag + "] treasury=" + guild.getTreasury() + " contrib=" + guild.getContribution());
        }
    }

    private static void loadFamilies() throws IOException {
        Path file = dataDir.resolve("families.properties");
        if (!Files.exists(file)) return;

        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            props.load(in);
        }

        // Charger directement dans runtimeData pour TOUTES les familles sauvegardees
        Set<String> familyIds = new HashSet<>();
        for (String key : props.stringPropertyNames()) {
            if (key.contains(".")) {
                familyIds.add(key.substring(0, key.indexOf('.')));
            }
        }

        for (String familyId : familyIds) {
            if (FamilyManager.get(familyId) == null) continue; // Famille inconnue

            long treasury = Long.parseLong(props.getProperty(familyId + ".treasury", "0"));
            long contribution = Long.parseLong(props.getProperty(familyId + ".contribution", "0"));

            // Injecter directement dans le runtime
            FamilyManager.setRuntimeData(familyId, treasury, contribution);
            System.out.println("[Persistence] Famille restauree: " + familyId + " treasury=" + treasury + " contrib=" + contribution);
        }

        // Restaurer les familles prises
        String takenStr = props.getProperty("_takenFamilies", "");
        if (!takenStr.isEmpty()) {
            for (String familyId : takenStr.split(",")) {
                familyId = familyId.trim();
                if (!familyId.isEmpty() && FamilyManager.get(familyId) != null) {
                    FamilyManager.claimFamily(familyId);
                }
            }
            System.out.println("[Persistence] Familles prises restaurees: " + takenStr);
        }
    }

    private static void loadClassements() throws IOException {
        Path file = dataDir.resolve("classements.properties");
        if (!Files.exists(file)) return;

        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            props.load(in);
        }

        for (String key : props.stringPropertyNames()) {
            long val = Long.parseLong(props.getProperty(key));
            if (key.startsWith("mob.")) {
                ClassementManager.updateMobKills(key.substring(4), val);
            } else if (key.startsWith("pvp.")) {
                ClassementManager.updatePvPKills(key.substring(4), val);
            } else if (key.startsWith("duel.")) {
                ClassementManager.updateDuelWins(key.substring(5), val);
            }
        }
    }

    // ==================== HIERARCHIES (Noblesse + Eglise) ====================

    private static void saveHierarchies() throws IOException {
        Properties props = new Properties();
        com.eldanior.system.titles.nobility.NobilityManager.saveTo(props);
        com.eldanior.system.titles.church.ChurchManager.saveTo(props);
        PersistenceUtils.writeAtomicWithBackup(dataDir.resolve("hierarchies.properties"), props, "Nobility & Church hierarchies");
    }

    private static void loadHierarchies() throws IOException {
        java.nio.file.Path file = dataDir.resolve("hierarchies.properties");
        if (!java.nio.file.Files.exists(file)) return;
        Properties props = new Properties();
        try (java.io.InputStream in = java.nio.file.Files.newInputStream(file)) {
            props.load(in);
        }
        com.eldanior.system.titles.nobility.NobilityManager.loadFrom(props);
        com.eldanior.system.titles.church.ChurchManager.loadFrom(props);
        System.out.println("[Persistence] Hierarchies restaurees.");
    }

    // ==================== SHOP ====================

    private static void saveShop() throws IOException {
        // Save black market too
        saveMarketListings("shop.properties", ShopManager.getListings());
        saveMarketListings("blackmarket.properties", ShopManager.getBlackMarketListings());

        // Save pending earnings in shop file
        Properties pendingProps = new Properties();
        for (var entry : ShopManager.getPendingEarningsMap().entrySet()) {
            pendingProps.setProperty(entry.getKey().toString(), String.valueOf(entry.getValue()));
        }
        PersistenceUtils.writeAtomicWithBackup(dataDir.resolve("pending_earnings.properties"), pendingProps, "Pending shop earnings");

        // Save pending parcel earnings
        Properties parcelPendingProps = new Properties();
        for (var entry : ParcelManager.getPendingEarningsMap().entrySet()) {
            parcelPendingProps.setProperty(entry.getKey().toString(), String.valueOf(entry.getValue()));
        }
        PersistenceUtils.writeAtomicWithBackup(dataDir.resolve("pending_parcel_earnings.properties"), parcelPendingProps, "Pending parcel earnings");
    }

    private static void saveMarketListings(String filename, List<ShopManager.ShopListing> listings) throws IOException {
        Properties props = new Properties();
        props.setProperty("count", String.valueOf(listings.size()));

        for (int i = 0; i < listings.size(); i++) {
            ShopManager.ShopListing listing = listings.get(i);
            String prefix = "item." + i;
            props.setProperty(prefix + ".sellerUUID", listing.getSellerUUID().toString());
            props.setProperty(prefix + ".sellerName", listing.getSellerName());
            props.setProperty(prefix + ".itemId", listing.getItem().getItemId());
            props.setProperty(prefix + ".quantity", String.valueOf(listing.getItem().getQuantity()));
            props.setProperty(prefix + ".durability", String.valueOf(listing.getItem().getDurability()));
            props.setProperty(prefix + ".maxDurability", String.valueOf(listing.getItem().getMaxDurability()));
            props.setProperty(prefix + ".price", String.valueOf(listing.getPrice()));
        }

        PersistenceUtils.writeAtomicWithBackup(dataDir.resolve(filename), props, "Market listings data");
    }

    private static void loadShop() throws IOException {
        loadMarketListings("shop.properties", false);
        loadMarketListings("blackmarket.properties", true);

        // Pending earnings
        Path pendingFile = dataDir.resolve("pending_earnings.properties");
        if (Files.exists(pendingFile)) {
            Properties pp = new Properties();
            try (InputStream in = Files.newInputStream(pendingFile)) { pp.load(in); }
            for (String key : pp.stringPropertyNames()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    long amount = Long.parseLong(pp.getProperty(key));
                    ShopManager.addPendingEarnings(uuid, amount);
                } catch (Exception e) { EldaniorLogger.error("PersistenceManager", e); }
            }
        }

        // Pending parcel earnings
        Path parcelPendingFile = dataDir.resolve("pending_parcel_earnings.properties");
        if (Files.exists(parcelPendingFile)) {
            Properties ppe = new Properties();
            try (InputStream in = Files.newInputStream(parcelPendingFile)) { ppe.load(in); }
            for (String key : ppe.stringPropertyNames()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    long amount = Long.parseLong(ppe.getProperty(key));
                    ParcelManager.addPendingEarnings(uuid, amount);
                } catch (Exception e) { EldaniorLogger.error("PersistenceManager", e); }
            }
        }
    }

    private static void loadMarketListings(String filename, boolean blackMarket) throws IOException {
        Path file = dataDir.resolve(filename);
        if (!Files.exists(file)) return;

        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(file)) { props.load(in); }

        int count = Integer.parseInt(props.getProperty("count", "0"));
        for (int i = 0; i < count; i++) {
            String prefix = "item." + i;
            String sellerUUIDStr = props.getProperty(prefix + ".sellerUUID");
            String sellerName = props.getProperty(prefix + ".sellerName");
            String itemId = props.getProperty(prefix + ".itemId");
            int qty = Integer.parseInt(props.getProperty(prefix + ".quantity", "1"));
            double dur = Double.parseDouble(props.getProperty(prefix + ".durability", "0"));
            double maxDur = Double.parseDouble(props.getProperty(prefix + ".maxDurability", "0"));
            long price = Long.parseLong(props.getProperty(prefix + ".price", "0"));

            if (sellerUUIDStr == null || itemId == null) continue;
            try {
                UUID sellerUUID = UUID.fromString(sellerUUIDStr);
                ItemStack item = maxDur > 0 ? new ItemStack(itemId, qty, dur, maxDur, null) : new ItemStack(itemId, qty);

                if (blackMarket) {
                    ShopManager.addBlackMarketListing(sellerUUID, sellerName != null ? sellerName : "?", item, price);
                } else {
                    ShopManager.addListing(sellerUUID, sellerName != null ? sellerName : "?", item, price);
                }
            } catch (Exception e) {
                System.out.println("[Persistence] " + filename + " item " + i + " skip: " + e.getMessage());
            }
        }
        System.out.println("[Persistence] " + filename + " restaure: " + count + " annonces");
    }
}
