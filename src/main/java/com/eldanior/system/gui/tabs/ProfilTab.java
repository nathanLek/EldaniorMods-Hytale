package com.eldanior.system.gui.tabs;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class ProfilTab {

    public static void populate(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store,
                                PlayerLevelData data, String playerName) {
        ClassModel classModel = ClassManager.get(data.getPlayerClassId());
        int cStr = classModel != null ? classModel.getBonusStr() : 0;
        int cVit = classModel != null ? classModel.getBonusVit() : 0;
        int cInt = classModel != null ? classModel.getBonusInt() : 0;
        int cEnd = classModel != null ? classModel.getBonusEnd() : 0;
        int cAgl = classModel != null ? classModel.getBonusAgl() : 0;
        int cLck = classModel != null ? classModel.getBonusLck() : 0;

        // Bonus titre CUMULE de TOUS les titres débloqués
        int tStr = 0, tVit = 0, tInt = 0, tEnd = 0, tAgl = 0, tLck = 0;
        if (data.getUnlockedTitles() != null) {
            for (String tid : data.getUnlockedTitles()) {
                com.eldanior.system.titles.models.TitleModel tm = com.eldanior.system.titles.TitleManager.get(tid);
                if (tm != null && tm.getBonus() != null) {
                    com.eldanior.system.titles.models.TitleBonus b = tm.getBonus();
                    tStr += b.strength(); tVit += b.vitality(); tInt += b.intelligence();
                    tEnd += b.endurance(); tAgl += b.agility(); tLck += b.luck();
                }
            }
        }

        int bStr = cStr + tStr;
        int bVit = cVit + tVit;
        int bInt = cInt + tInt;
        int bEnd = cEnd + tEnd;
        int bAgl = cAgl + tAgl;
        int bLck = cLck + tLck;

        // Nom + famille noble
        String displayName = playerName;
        String familyId = data.getNobleFamilyId();
        if (familyId != null && !familyId.isEmpty()) {
            NobleFamilyModel family = FamilyManager.get(familyId);
            if (family != null) {
                displayName = playerName + " Von " + family.getDisplayName();
            }
        }

        ui.set("#ProfilName.Text", displayName);

        // Rang aventurier basé sur le niveau
        String rank = getAdventureRank(data.getLevel());
        String rankColor = getAdventureRankColor(data.getLevel());
        ui.set("#ProfilAdvRank.Text", rank);
        ui.set("#ProfilAdvRank.Style.TextColor", rankColor);

        ui.set("#ProfilClass.Text", data.getPlayerClass());

        // Rang
        String rankStr = data.getNobilityRank();
        ui.set("#ProfilRank.Text", (rankStr != null && !rankStr.isEmpty() && !"ROTURIER".equals(rankStr)) ? rankStr : "Roturier");

        // Status (Patriarch/Vice/Membre/Chef de Guilde/etc - jamais PK ici)
        String status = data.getStatus();
        if (status != null && !status.isEmpty()) {
            ui.set("#ProfilStatus.Text", status);
        } else if (data.hasGuild()) {
            ui.set("#ProfilStatus.Text", data.getGuildRole());
        } else {
            ui.set("#ProfilStatus.Text", "-");
        }

        // Eglise / PK
        if (data.isPK()) {
            String bountyText = data.getBounty() > 0 ? " | Prime: " + data.getBounty() + " Or" : "";
            ui.set("#ProfilChurch.Text", "CRIMINEL - EXCOMMUNIE" + bountyText);
        } else {
            String churchRank = data.getChurchRank();
            if (churchRank != null && !churchRank.isEmpty() && !"LAIQUE".equals(churchRank)) {
                ui.set("#ProfilChurch.Text", "Eglise : " + churchRank);
            } else {
                ui.set("#ProfilChurch.Text", "Laique");
            }
        }

        // Titre
        // Titre : afficher le displayName au lieu de l'id
        String titleId = data.getCurrentTitle();
        com.eldanior.system.titles.models.TitleModel titleModel = com.eldanior.system.titles.TitleManager.get(titleId);
        ui.set("#ProfilTitle.Text", titleModel != null ? titleModel.getDisplayName() : (titleId != null ? titleId : "Aucun"));

        // XP
        ui.set("#ProfilXPLabel.TextSpans", Message.raw("XP : " + data.getExperience() + " / " + data.getRequiredExperience()));
        ui.set("#ProfilXPBar.Value", data.getExperienceProgress());

        // Mana
        populateResourceBars(ui, ref, store);

        // Points disponibles
        int pts = data.getAttributePoints();
        ui.set("#ProfilPoints.TextSpans", Message.raw("POINTS DISPONIBLES : " + pts));

        // Attributs
        ui.set("#ProfilStr.TextSpans", Message.raw("FORCE  " + data.getStrength() + "  (+" + bStr + ")"));
        ui.set("#ProfilVit.TextSpans", Message.raw("VITALITE  " + data.getVitality() + "  (+" + bVit + ")"));
        ui.set("#ProfilInt.TextSpans", Message.raw("INTELLIGENCE  " + data.getIntelligence() + "  (+" + bInt + ")"));
        ui.set("#ProfilEnd.TextSpans", Message.raw("ENDURANCE  " + data.getEndurance() + "  (+" + bEnd + ")"));
        ui.set("#ProfilAgl.TextSpans", Message.raw("AGILITE  " + data.getAgility() + "  (+" + bAgl + ")"));
        ui.set("#ProfilLck.TextSpans", Message.raw("CHANCE   " + data.getLuck() + "  (+" + bLck + ")"));

        // Foi & Dignite
        ui.set("#ProfilFaith.Text", String.valueOf(data.getFaith()));
        ui.set("#ProfilDignity.Text", String.valueOf(data.getDignity()));

        // Stats PvP
        ui.set("#StatKills.Text", String.valueOf(data.getPlayerKills()));
        ui.set("#StatDeaths.Text", String.valueOf(data.getPlayerDeaths()));
        ui.set("#StatKDR.Text", String.format("%.1f", data.getKDR()));
        ui.set("#StatStreak.Text", String.valueOf(data.getKillStreak()));
        ui.set("#StatBestStreak.Text", String.valueOf(data.getBestKillStreak()));
        ui.set("#StatChests.Text", String.valueOf(data.getChestsDiscovered()));
        ui.set("#StatTotalMobs.Text", "Total : " + data.getTotalMobKills());

        // Mob kills (max 15 slots)
        java.util.List<java.util.Map.Entry<String, Integer>> mobList = new java.util.ArrayList<>(data.getMobKills().entrySet());
        mobList.sort((a, b) -> b.getValue() - a.getValue()); // tri par nombre desc

        for (int i = 0; i < 15; i++) {
            if (i < mobList.size()) {
                var entry = mobList.get(i);
                ui.set("#MobKillRow" + i + ".Visible", true);
                // Formater le nom du mob : "goblin_scrapper" -> "Goblin Scrapper"
                String mobName = formatMobName(entry.getKey());
                ui.set("#MobName" + i + ".Text", mobName);
                ui.set("#MobCount" + i + ".Text", String.valueOf(entry.getValue()));
            } else {
                ui.set("#MobKillRow" + i + ".Visible", false);
            }
        }
    }

    private static String formatMobName(String mobId) {
        String[] parts = mobId.replace("_", " ").split(" ");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) sb.append(part.substring(1));
            }
        }
        return sb.toString();
    }

    public static void populateResourceBars(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        EntityStatMap statMap = store.getComponent(ref, EntityStatsModule.get().getEntityStatMapComponentType());
        if (statMap == null) return;

        int manaIndex = DefaultEntityStatTypes.getMana();
        var manaStat = statMap.get(manaIndex);
        if (manaStat != null) {
            float currentMp = manaStat.get();
            float maxMp = manaStat.getMax();
            ui.set("#ProfilMana.TextSpans", Message.raw((int) currentMp + " / " + (int) maxMp));
            ui.set("#ProfilManaBar.Value", maxMp > 0 ? currentMp / maxMp : 0.0f);
        }
    }

    public static boolean handleAttribute(String action, Ref<EntityStore> ref, Store<EntityStore> store) {
        if (!action.startsWith("attr_")) return false;

        // Parse: attr_str_1 or attr_str_5
        String[] parts = action.split("_");
        if (parts.length != 3) return false;

        String stat = parts[1];
        int amount;
        try {
            amount = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            return false;
        }

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null || data.getAttributePoints() < amount) return false;

        // Clamp to available points
        amount = Math.min(amount, data.getAttributePoints());

        PlayerLevelData copy = (PlayerLevelData) data.clone();
        if (copy == null) return false;

        switch (stat) {
            case "str" -> copy.setStrength(copy.getStrength() + amount);
            case "vit" -> copy.setVitality(copy.getVitality() + amount);
            case "int" -> copy.setIntelligence(copy.getIntelligence() + amount);
            case "end" -> copy.setEndurance(copy.getEndurance() + amount);
            case "agl" -> copy.setAgility(copy.getAgility() + amount);
            case "lck" -> copy.setLuck(copy.getLuck() + amount);
            default -> { return false; }
        }

        copy.setAttributePoints(copy.getAttributePoints() - amount);
        store.putComponent(ref, type, copy);

        com.eldanior.system.Leveling.utils.StatCalculator.updatePlayerStats(ref, store, copy);

        // Verifier titres en temps reel apres changement de stats
        com.hypixel.hytale.server.core.universe.PlayerRef playerRef =
                store.getComponent(ref, com.hypixel.hytale.server.core.universe.PlayerRef.getComponentType());
        com.eldanior.system.titles.TitleManager.checkAndUnlockTitles(ref, store, copy, playerRef);

        return true;
    }

    public static String getAdventureRank(int level) {
        if (level >= 600) return "S";
        if (level >= 400) return "A";
        if (level >= 250) return "B";
        if (level >= 150) return "C";
        if (level >= 80) return "D";
        if (level >= 30) return "E";
        return "F";
    }

    public static String getAdventureRankColor(int level) {
        if (level >= 600) return "#FFD700";
        if (level >= 400) return "#cc4444";
        if (level >= 250) return "#ff9800";
        if (level >= 150) return "#9C27B0";
        if (level >= 80) return "#2196F3";
        if (level >= 30) return "#4CAF50";
        return "#8899aa";
    }
}
