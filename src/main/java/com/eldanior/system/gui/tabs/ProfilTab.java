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

        // Identite (panneau de gauche, a cote du personnage ; l'onglet Profil a ete retire le 2026-11-12)
        ui.set("#ProfilAdvRank.Text", getAdventureRank(data.getLevel()));
        ui.set("#ProfilAdvRank.Style.TextColor", getAdventureRankColor(data.getLevel()));
        ui.set("#IdLevel.Text", String.valueOf(data.getLevel()));
        ui.set("#IdClass.Text", data.getPlayerClass() != null ? data.getPlayerClass() : "Sans classe");

        // Noblesse (icone : le decret du rang ; couronne pour le roi, parchemin pour le roturier)
        com.eldanior.system.titles.nobility.NobilityRank noble =
                com.eldanior.system.titles.nobility.NobilityRank.fromString(data.getNobilityRank());
        ui.set("#IdNoble.Text", noble != null ? noble.getDisplayName() : "Roturier");
        String nobleIcon = noble == null || noble == com.eldanior.system.titles.nobility.NobilityRank.ROTURIER ? "Deco_Scroll"
                : noble == com.eldanior.system.titles.nobility.NobilityRank.ROI ? "Butin_Couronne_Spectrale"
                : "Decret_" + noble.getDisplayName();
        icon(ui, "#IdIconNoble", nobleIcon, "Deco_Scroll");

        // Eglise : criminel (PK, le cadre du menu passe au rouge), laique ou rang religieux
        com.eldanior.system.titles.church.ChurchRank rank = null;
        try {
            String cr = data.getChurchRank();
            if (cr != null && !cr.isEmpty()) rank = com.eldanior.system.titles.church.ChurchRank.valueOf(cr.toUpperCase());
        } catch (IllegalArgumentException ignored) { }
        if (data.isPK()) {
            com.hypixel.hytale.server.core.universe.PlayerRef me = store.getComponent(ref, com.hypixel.hytale.server.core.universe.PlayerRef.getComponentType());
            boolean starred = me != null && com.eldanior.system.guards.BankLoans.isStarred(me.getUuid());   // dette impayee
            ui.set("#IdChurch.Text", (starred ? "Criminel étoilé · " : "Criminel · ") + data.getBounty() + " or");
            ui.set("#IdChurch.Style.TextColor", "#ff6b5e");
            icon(ui, "#IdIconChurch", "Deco_Bone_Skulls", null);
        } else {
            boolean clergy = rank != null && rank.isClergy();
            ui.set("#IdChurch.Text", clergy ? accent(rank.getDisplayName()) : "Laïque");
            ui.set("#IdChurch.Style.TextColor", clergy ? "#f0d58c" : "#d1dae6");
            icon(ui, "#IdIconChurch", clergy ? "Benediction_" + rank.getDisplayName() : "Furniture_Human_Ruins_Candle",
                    "Furniture_Temple_Light_Statue");
        }

        // Famille et guilde : seulement si le joueur en a une
        String familyId = data.getNobleFamilyId();
        NobleFamilyModel family = familyId != null && !familyId.isEmpty() ? FamilyManager.get(familyId) : null;
        ui.set("#IdRowFamily.Visible", family != null);
        if (family != null) {
            var role = data.getFamilyRole();
            ui.set("#IdFamily.Text", family.getDisplayName() + (role != null ? " · " + role.label() : ""));
            icon(ui, "#IdIconFamily", "Eldanior_Banniere_Famille_" + Character.toUpperCase(familyId.charAt(0)) + familyId.substring(1),
                    "Furniture_Outlander_Banner");
        }
        var g = data.hasGuild() ? com.eldanior.system.guild.GuildManager.get(data.getGuildId()) : null;
        ui.set("#IdRowGuild.Visible", g != null);
        if (g != null) {
            String gr = data.getGuildRole() == null ? "" : data.getGuildRole().toUpperCase();
            String grLabel = switch (gr) { case "CHEF" -> "Chef"; case "OFFICER" -> "Officier"; default -> "Membre"; };
            ui.set("#IdGuild.Text", g.getName() + " · " + grLabel);
            icon(ui, "#IdIconGuild", "Furniture_Human_Ruins_Banner", null);
        }

        // XP (panneau de gauche, refonte du 2026-11-12)
        ui.set("#ProfilXPLabel.Text", data.getLevel() >= PlayerLevelData.MAX_LEVEL ? "Niveau maximal"
                : group(data.getExperience()) + " / " + group(data.getRequiredExperience()));
        ui.set("#ProfilXPBar.Value", Math.max(0f, Math.min(1f, data.getExperienceProgress())));

        ui.set("#ProfilPreviewBox.TooltipText", combatTooltip(data));

        // Statistiques reelles (vie, endurance, mana, defense) : remplacent la barre de mana (2026-11-12)
        populateResourceBars(ui, ref, store);
        populateDerived(ui, data, classModel);

        // Points disponibles : les boutons + n'apparaissent que s'il en reste (+5 : au moins 5)
        int pts = data.getAttributePoints();
        ui.set("#ProfilPoints.Text", pts > 0 ? pts + (pts > 1 ? " points à répartir" : " point à répartir") : "Aucun point");
        String[][] attrs = {{"Str", String.valueOf(data.getStrength()), String.valueOf(bStr)},
                {"Vit", String.valueOf(data.getVitality()), String.valueOf(bVit)},
                {"Int", String.valueOf(data.getIntelligence()), String.valueOf(bInt)},
                {"End", String.valueOf(data.getEndurance()), String.valueOf(bEnd)},
                {"Agl", String.valueOf(data.getAgility()), String.valueOf(bAgl)},
                {"Lck", String.valueOf(data.getLuck()), String.valueOf(bLck)}};
        for (String[] a : attrs) {
            ui.set("#Profil" + a[0] + ".Text", a[1] + ("0".equals(a[2]) ? "" : "  (+" + a[2] + ")"));
            ui.set("#Btn" + a[0] + "1.Visible", pts > 0);
            ui.set("#Btn" + a[0] + "5.Visible", pts >= 5);
        }

        // Foi & Dignite
        ui.set("#ProfilFaith.Text", String.valueOf(data.getFaith()));
        ui.set("#ProfilDignity.Text", String.valueOf(data.getDignity()));

    }

    /** Icone d'objet d'une ligne d'identite ; repli si l'objet n'existe pas (un ItemId inconnu n'est jamais envoye). */
    private static void icon(UICommandBuilder ui, String id, String item, String fallback) {
        var map = com.hypixel.hytale.server.core.asset.type.item.config.Item.getAssetMap();
        String use = item != null && map.getAsset(item) != null ? item : fallback != null && map.getAsset(fallback) != null ? fallback : null;
        if (use != null) ui.set(id + ".ItemId", use);
    }

    /** Libelles d'Eglise sans accents dans l'enum (Pretre, Archeveque) : accents pour l'affichage. */
    private static String accent(String s) {
        return s.replace("Pretre", "Prêtre").replace("Archeveque", "Archevêque").replace("Laique", "Laïque");
    }

    private static String group(long v) {
        return String.format(java.util.Locale.FRANCE, "%,d", v).replace('\u202f', ' ').replace('\u00a0', ' ');
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

    /** Vie, endurance et mana actuelles / maximum, comme le panneau de l'inventaire. */
    public static void populateResourceBars(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        EntityStatMap statMap = store.getComponent(ref, EntityStatsModule.get().getEntityStatMapComponentType());
        String[][] stats = {{"#RealHealth", String.valueOf(DefaultEntityStatTypes.getHealth())},
                {"#RealStamina", String.valueOf(DefaultEntityStatTypes.getStamina())},
                {"#RealMana", String.valueOf(DefaultEntityStatTypes.getMana())}};
        for (String[] st : stats) {
            var v = statMap != null ? statMap.get(Integer.parseInt(st[1])) : null;
            ui.set(st[0] + ".Text", v != null ? Math.round(v.get()) + " / " + Math.round(v.getMax()) : "—");
        }
    }

    /** Empreinte des valeurs actuelles (vie, endurance, mana) : SystemScreen ne renvoie les statistiques que si elle change. */
    public static String resourceKey(Ref<EntityStore> ref, Store<EntityStore> store) {
        EntityStatMap statMap = store.getComponent(ref, EntityStatsModule.get().getEntityStatMapComponentType());
        if (statMap == null) return "";
        StringBuilder b = new StringBuilder();
        for (int id : new int[]{DefaultEntityStatTypes.getHealth(), DefaultEntityStatTypes.getStamina(), DefaultEntityStatTypes.getMana()}) {
            var v = statMap.get(id);
            b.append(v != null ? Math.round(v.get()) + "/" + Math.round(v.getMax()) : "-").append('|');
        }
        return b.toString();
    }

    /** Infobulle du personnage 3D : statistiques de combat qui ne sont plus affichees ailleurs (2026-11-12). */
    private static String combatTooltip(PlayerLevelData d) {
        return "Joueurs tués : " + d.getPlayerKills()
                + "\nMorts : " + d.getPlayerDeaths()
                + "\nRatio (KDR) : " + String.format(java.util.Locale.FRANCE, "%.2f", d.getKDR())
                + "\nSérie actuelle : " + d.getKillStreak() + "  ·  meilleure : " + d.getBestKillStreak()
                + "\nMonstres tués : " + d.getTotalMobKills()
                + "\nDuels : " + d.getDuelWins() + " gagnés, " + d.getDuelLosses() + " perdus"
                + (d.getDuelBestStreak() > 0 ? "  ·  meilleure série " + d.getDuelBestStreak() : "");
    }

    /** Defense et effets des caracteristiques (StatConfig, memes formules que le combat) : Force, Defense, critique et esquive en %. */
    private static void populateDerived(UICommandBuilder ui, PlayerLevelData data, ClassModel model) {
        try {
            float def = com.eldanior.system.config.configs.StatConfig.ENDURANCE_DEFENSE.getFinalValue(data, model);
            float dmg = com.eldanior.system.config.configs.StatConfig.STRENGTH_DAMAGE.getFinalValue(data, model);
            float crit = com.eldanior.system.config.configs.StatConfig.LUCK_CRITICAL.getFinalValue(data, model);
            float dodge = com.eldanior.system.config.configs.StatConfig.DODGE_CHANCE.getFinalValue(data, model);
            ui.set("#RealDefense.Text", "-" + num(def) + " % de dégâts reçus");       // CombatStatsSystem (en % depuis le 2026-11-25)
            ui.set("#RealDerived.Text", "Dégâts +" + num(dmg) + " %   ·   Critique " + num(crit) + " %   ·   Esquive " + num(dodge) + " %");
        } catch (Exception e) {
            ui.set("#RealDefense.Text", "—");
            ui.set("#RealDerived.Text", "");
        }
    }

    private static String num(float v) {
        return Math.abs(v - Math.round(v)) < 0.05f ? String.valueOf(Math.round(v)) : String.format(java.util.Locale.FRANCE, "%.1f", v);
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
        if (level >= 650) return "S";
        if (level >= 400) return "A";
        if (level >= 250) return "B";
        if (level >= 150) return "C";
        if (level >= 80) return "D";
        if (level >= 30) return "E";
        return "F";
    }

    public static String getAdventureRankColor(int level) {
        if (level >= 650) return "#FFD700";
        if (level >= 400) return "#cc4444";
        if (level >= 250) return "#ff9800";
        if (level >= 150) return "#9C27B0";
        if (level >= 80) return "#2196F3";
        if (level >= 30) return "#4CAF50";
        return "#8899aa";
    }
}
