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

        // Bonus titre
        com.eldanior.system.titles.models.TitleBonus tb = com.eldanior.system.titles.models.TitleBonus.NONE;
        String tId = data.getCurrentTitle();
        if (tId != null && !tId.isEmpty()) {
            com.eldanior.system.titles.models.TitleModel tm = com.eldanior.system.titles.TitleManager.get(tId);
            if (tm != null && tm.getBonus() != null) tb = tm.getBonus();
        }

        int bStr = cStr + tb.strength();
        int bVit = cVit + tb.vitality();
        int bInt = cInt + tb.intelligence();
        int bEnd = cEnd + tb.endurance();
        int bAgl = cAgl + tb.agility();
        int bLck = cLck + tb.luck();

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
        ui.set("#ProfilClass.Text", data.getPlayerClass());

        // Rang
        String rankStr = data.getNobilityRank();
        ui.set("#ProfilRank.Text", (rankStr != null && !rankStr.isEmpty() && !"ROTURIER".equals(rankStr)) ? rankStr : "Roturier");

        // Status
        String status = data.getStatus();
        ui.set("#ProfilStatus.Text", (status != null && !status.isEmpty()) ? status : "-");

        // Eglise
        String churchRank = data.getChurchRank();
        if (churchRank != null && !churchRank.isEmpty() && !"LAIQUE".equals(churchRank)) {
            ui.set("#ProfilChurch.Text", "Eglise : " + churchRank);
        } else {
            ui.set("#ProfilChurch.Text", "Laique");
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

        switch (stat) {
            case "str" -> data.setStrength(data.getStrength() + amount);
            case "vit" -> data.setVitality(data.getVitality() + amount);
            case "int" -> data.setIntelligence(data.getIntelligence() + amount);
            case "end" -> data.setEndurance(data.getEndurance() + amount);
            case "agl" -> data.setAgility(data.getAgility() + amount);
            case "lck" -> data.setLuck(data.getLuck() + amount);
            default -> { return false; }
        }

        data.setAttributePoints(data.getAttributePoints() - amount);
        store.putComponent(ref, type, data);

        com.eldanior.system.Leveling.utils.StatCalculator.updatePlayerStats(ref, store, data);
        return true;
    }
}
