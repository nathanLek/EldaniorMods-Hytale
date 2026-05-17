package com.eldanior.system.gui.tabs;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.StatCalculator;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.titles.nobility.NobilityManager;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.church.ChurchManager;
import com.eldanior.system.titles.church.ChurchRank;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.*;

public class AdminTab {

    public static final int MAX_PLAYER_SLOTS = 8;
    private static final List<String> cachedPlayerNames = new ArrayList<>();
    private static int selectedPlayer = -1;
    private static String pendingCommand = "";

    public static void populate(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        // Stats serveur
        ui.set("#AdminStatMobs.Text", String.valueOf(com.eldanior.system.config.configs.MobXP.getTotalMobCount()));
        ui.set("#AdminStatNpcs.Text", String.valueOf(com.eldanior.system.config.configs.MobXP.getTotalNpcCount()));
        ui.set("#AdminStatClasses.Text", String.valueOf(com.eldanior.system.classes.ClassManager.getAll().size()));
        ui.set("#AdminStatTitres.Text", String.valueOf(com.eldanior.system.titles.TitleManager.getAll().size()));
        ui.set("#AdminStatSkills.Text", String.valueOf(com.eldanior.system.skills.SkillManager.getAllSkills().size()));
        ui.set("#AdminStatQuetes.Text", String.valueOf(com.eldanior.system.quest.QuestManager.getAllQuests().size()));
        ui.set("#AdminStatPlayers.Text", String.valueOf(Universe.get().getPlayers().size()));

        cachedPlayerNames.clear();
        for (PlayerRef pRef : Universe.get().getPlayers()) {
            if (cachedPlayerNames.size() >= MAX_PLAYER_SLOTS) break;
            cachedPlayerNames.add(pRef.getUsername());
        }

        // Player selector buttons
        for (int i = 0; i < MAX_PLAYER_SLOTS; i++) {
            if (i < cachedPlayerNames.size()) {
                ui.set("#ASelPlayer" + i + ".Visible", true);
                ui.set("#ASelPlayer" + i + ".Text", cachedPlayerNames.get(i));
            } else {
                ui.set("#ASelPlayer" + i + ".Visible", false);
            }
        }

        // Show action panel if a player is selected
        boolean hasSelection = selectedPlayer >= 0 && selectedPlayer < cachedPlayerNames.size();
        ui.set("#AdminActionPanel.Visible", hasSelection);

        if (hasSelection) {
            String name = cachedPlayerNames.get(selectedPlayer);
            ui.set("#AdminSelectedName.Text", "Joueur : " + name);

            // Load target player info
            PlayerRef targetRef = Universe.get().getPlayerByUsername(name, com.hypixel.hytale.server.core.NameMatching.EXACT_IGNORE_CASE);
            if (targetRef != null) {
                try {
                    var tRef = targetRef.getReference();
                    if (tRef != null) {
                        var tStore = tRef.getStore();
                        PlayerLevelData tData = tStore.getComponent(tRef, EldaniorSystem.get().getPlayerLevelDataType());
                        if (tData != null) {
                            ui.set("#AdminTargetInfo.Text",
                                "Lv." + tData.getLevel() + " | " + tData.getPlayerClass() +
                                " | " + tData.getNobilityRank() + " | " + tData.getChurchRank() +
                                " | " + tData.getMoney() + " Or" +
                                (tData.isPK() ? " | PK" : ""));
                        }
                    }
                } catch (Exception e) { EldaniorLogger.error("AdminTab", e); }
            }
        }

    }

    public static void selectPlayer(int index) {
        selectedPlayer = index;
        pendingCommand = "";
    }

    public static void selectPlayerByName(String name) {
        // Refresh cache and find the player
        cachedPlayerNames.clear();
        for (com.hypixel.hytale.server.core.universe.PlayerRef pRef : com.hypixel.hytale.server.core.universe.Universe.get().getPlayers()) {
            if (cachedPlayerNames.size() >= MAX_PLAYER_SLOTS) break;
            cachedPlayerNames.add(pRef.getUsername());
        }
        for (int i = 0; i < cachedPlayerNames.size(); i++) {
            if (cachedPlayerNames.get(i).equalsIgnoreCase(name)) {
                selectedPlayer = i;
                pendingCommand = "";
                return;
            }
        }
        // Player not in first 8 — add them manually
        if (cachedPlayerNames.size() < MAX_PLAYER_SLOTS) {
            cachedPlayerNames.add(name);
            selectedPlayer = cachedPlayerNames.size() - 1;
        } else {
            cachedPlayerNames.set(MAX_PLAYER_SLOTS - 1, name);
            selectedPlayer = MAX_PLAYER_SLOTS - 1;
        }
        pendingCommand = "";
    }

    public static void setPendingCommand(String template) {
        String playerName = getSelectedPlayerName();
        pendingCommand = template.replace("<player>", playerName != null ? playerName : "???");
    }

    public static String getPendingCommand() { return pendingCommand; }

    public static String getSelectedPlayerName() {
        if (selectedPlayer < 0 || selectedPlayer >= cachedPlayerNames.size()) return null;
        return cachedPlayerNames.get(selectedPlayer);
    }

    /**
     * Execute la commande pendante directement cote serveur.
     * Verifie la permission admin avant execution.
     */
    public static boolean executePendingCommand(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (pendingCommand == null || pendingCommand.isEmpty()) return false;

        // Verification permission admin
        Player adminCheck = store.getComponent(ref, Player.getComponentType());
        if (adminCheck == null || !adminCheck.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) {
            return false;
        }
        if (pendingCommand.contains("<") || pendingCommand.contains(">")) return false; // Non rempli

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return false;

        // Parser la commande et executer
        String[] parts = pendingCommand.split("\\s+");
        if (parts.length == 0) return false;

        try {
            switch (parts[0]) {
                case "titleadmin" -> {
                    if (parts.length >= 4) {
                        String action = parts[1]; // grant/remove/reset
                        String target = parts[2];
                        String titleId = parts[3];
                        PlayerRef targetRef = Universe.get().getPlayerByUsername(target, com.hypixel.hytale.server.core.NameMatching.EXACT_IGNORE_CASE);
                        if (targetRef == null) { player.sendMessage(Message.raw("§cJoueur introuvable: " + target)); return false; }

                        return executeOnTarget(targetRef, (tRef, tStore, data) -> {
                            if ("grant".equals(action)) { data.addTitle(titleId); targetRef.sendMessage(Message.raw("§aTitre accorde: " + titleId)); }
                            else if ("remove".equals(action)) { data.removeTitle(titleId); targetRef.sendMessage(Message.raw("§cTitre retire: " + titleId)); }
                            else if ("reset".equals(action)) { data.resetTitles(); targetRef.sendMessage(Message.raw("§cTitres reinitialises")); }
                        });
                    }
                }
                case "familyset" -> {
                    if (parts.length >= 3) {
                        String target = parts[1];
                        String familyId = parts[2];
                        PlayerRef targetRef = Universe.get().getPlayerByUsername(target, com.hypixel.hytale.server.core.NameMatching.EXACT_IGNORE_CASE);
                        if (targetRef == null) { player.sendMessage(Message.raw("§cJoueur introuvable: " + target)); return false; }

                        return executeOnTarget(targetRef, (tRef, tStore, data) -> {
                            data.setNobleFamilyId(familyId);
                            data.setStatus("PATRIARCH");
                            com.eldanior.system.titles.nobility.family.FamilyManager.claimFamily(familyId);
                            targetRef.sendMessage(Message.raw("§eFamille assignee: " + familyId));
                        });
                    }
                }
                case "setlevel" -> {
                    if (parts.length >= 3) {
                        String target = parts[1];
                        int level = Integer.parseInt(parts[2]);
                        PlayerRef targetRef = Universe.get().getPlayerByUsername(target, com.hypixel.hytale.server.core.NameMatching.EXACT_IGNORE_CASE);
                        if (targetRef == null) return false;
                        return executeOnTarget(targetRef, (tRef, tStore, data) -> {
                            int old = data.getLevel(); data.setLevel(level); data.setExperience(0);
                            int gained = Math.max(0, level - old) * 3;
                            if (gained > 0) data.setAttributePoints(data.getAttributePoints() + gained);
                            targetRef.sendMessage(Message.raw("§eNiveau: " + level));
                        });
                    }
                }
                case "addxp" -> {
                    if (parts.length >= 3) {
                        String target = parts[1];
                        int amount = Integer.parseInt(parts[2]);
                        PlayerRef targetRef = Universe.get().getPlayerByUsername(target, com.hypixel.hytale.server.core.NameMatching.EXACT_IGNORE_CASE);
                        if (targetRef == null) return false;
                        return executeOnTarget(targetRef, (tRef, tStore, data) -> {
                            data.addExperience(amount);
                            targetRef.sendMessage(Message.raw("§a+" + amount + " XP"));
                        });
                    }
                }
                case "guildcreate" -> {
                    if (parts.length >= 3) {
                        player.sendMessage(Message.raw("§eTapez dans le chat: /es guildcreate " + parts[1] + " " + parts[2]));
                    }
                }
                case "guilddisband" -> {
                    player.sendMessage(Message.raw("§eTapez dans le chat: /es guilddisband"));
                }
                case "bankGive" -> {
                    if (parts.length >= 2) {
                        player.sendMessage(Message.raw("§eTapez dans le chat: /es bankGive " + parts[1]));
                    }
                }
                default -> {
                    player.sendMessage(Message.raw("§cCommande inconnue: " + parts[0]));
                    return false;
                }
            }
        } catch (Exception e) {
            player.sendMessage(Message.raw("§cErreur: " + e.getMessage()));
            return false;
        }
        return true;
    }

    // ==================== HANDLERS ====================

    public static boolean handleResetLevel(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef targetRef = getSelectedTargetRef();
        if (targetRef == null) return false;
        return executeOnTarget(targetRef, (tRef, tStore, data) -> {
            data.setLevel(1); data.setExperience(0);
            data.setStrength(1); data.setVitality(1); data.setIntelligence(1);
            data.setEndurance(1); data.setAgility(1); data.setLuck(1);
            data.setAttributePoints(0);
            data.setPlayerClass("Novice"); data.setPlayerClassId("novice");
            data.forgetAllSkills(); data.resetTitles();
            // Liberer le slot de decret si le joueur avait un rang noble
            String oldRank = data.getNobilityRank();
            if (oldRank != null && !"ROTURIER".equals(oldRank) && !"CHEVALIER".equals(oldRank) && !"ROI".equals(oldRank)) {
                com.eldanior.system.titles.nobility.NobilityRank nRank =
                    com.eldanior.system.titles.nobility.NobilityRank.fromString(oldRank);
                if (nRank != null) com.eldanior.system.titles.nobility.NobilityManager.unrecordKingPromotion(nRank);
            }
            data.setNobilityRank("ROTURIER"); data.setNobleFamilyId(""); data.setStatus(""); data.setDignity(0);
            data.setChurchRank("LAIQUE"); data.setFaith(0);
            data.setGuildId(""); data.setGuildRole("");
            data.setPlayerKills(0); data.setPlayerDeaths(0); data.setKillStreak(0); data.setBestKillStreak(0);
            data.setChestsDiscovered(0); data.setForcePK(false);

            // Reset argent
            data.setMoney(1000);

            // Reset quetes + cooldowns
            data.setQuestData("");
            data.setCooldownData("");
            try {
                                java.util.UUID resetUUID = UUIDExtractor.getUUID(targetRef);
                com.eldanior.system.quest.QuestManager.getPlayerQuests(resetUUID).clear();
            } catch (Exception e) { EldaniorLogger.error("AdminTab", e); }

            targetRef.sendMessage(Message.raw("§cPersonnage reinitialise (Niveau 1)."));
        });
    }

    public static boolean handleAddXP(int amount, Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef targetRef = getSelectedTargetRef();
        if (targetRef == null) return false;
        return executeOnTarget(targetRef, (tRef, tStore, data) -> {
            data.addExperience(amount);
            targetRef.sendMessage(Message.raw("§a+" + amount + " XP !"));
        });
    }

    public static boolean handleSetLevel(int level, Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef targetRef = getSelectedTargetRef();
        if (targetRef == null) return false;
        return executeOnTarget(targetRef, (tRef, tStore, data) -> {
            int oldLevel = data.getLevel();
            data.setLevel(level); data.setExperience(0);
            int gained = Math.max(0, level - oldLevel) * 3;
            if (gained > 0) data.setAttributePoints(data.getAttributePoints() + gained);
            targetRef.sendMessage(Message.raw("§eNiveau defini a " + level));
        });
    }

    public static boolean handleGiveGold(long amount, Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef targetRef = getSelectedTargetRef();
        if (targetRef == null) return false;
        return executeOnTarget(targetRef, (tRef, tStore, data) -> {
            data.addMoney(amount);
            targetRef.sendMessage(Message.raw("§a+" + amount + " Or !"));
        });
    }

    public static boolean handleSetPK(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef targetRef = getSelectedTargetRef();
        if (targetRef == null) return false;
        return executeOnTarget(targetRef, (tRef, tStore, data) -> {
            data.setForcePK(!data.isForcePK());
            targetRef.sendMessage(Message.raw(data.isPK() ? "§c§lVous etes maintenant PK !" : "§aPK retire."));
        });
    }

    public static boolean handleResetTitles(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef targetRef = getSelectedTargetRef();
        if (targetRef == null) return false;
        return executeOnTarget(targetRef, (tRef, tStore, data) -> {
            data.resetTitles();
            targetRef.sendMessage(Message.raw("§cTitres reinitialises !"));
        });
    }

    public static boolean handleSetClass(String classId, Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef targetRef = getSelectedTargetRef();
        if (targetRef == null) return false;
        var model = com.eldanior.system.classes.ClassManager.get(classId);
        if (model == null) return false;
        return executeOnTarget(targetRef, (tRef, tStore, data) -> {
            data.setPlayerClassId(classId); data.setPlayerClass(model.getDisplayName());
            data.forgetAllSkills();
            targetRef.sendMessage(Message.raw("§eClasse: " + model.getDisplayName()));
        });
    }

    public static boolean handleNobilityPromote(String rank, Ref<EntityStore> ref, Store<EntityStore> store) {
        NobilityRank newRank = NobilityRank.fromString(rank);
        if (newRank == null) return false;
        PlayerRef targetRef = getSelectedTargetRef();
        if (targetRef == null) return false;
        return executeOnTarget(targetRef, (tRef, tStore, data) -> {
            data.setNobilityRank(newRank.name()); data.setDignity(newRank.getBaseDignity());
            NobilityManager.recordKingPromotion(newRank);
            targetRef.sendMessage(Message.raw("§eRang: " + newRank.getFormattedName()));
        });
    }

    public static boolean handleChurchPromote(String rank, Ref<EntityStore> ref, Store<EntityStore> store) {
        ChurchRank newRank = ChurchRank.fromString(rank);
        if (newRank == null) return false;
        PlayerRef targetRef = getSelectedTargetRef();
        if (targetRef == null) return false;
        return executeOnTarget(targetRef, (tRef, tStore, data) -> {
            data.setChurchRank(newRank.name()); data.setFaith(newRank.getBaseFaith());
            ChurchManager.recordPopePromotion(newRank);
            targetRef.sendMessage(Message.raw("§eEglise: " + newRank.getFormattedName()));
        });
    }

    // ==================== UTILS ====================

    private static PlayerRef getSelectedTargetRef() {
        if (selectedPlayer < 0 || selectedPlayer >= cachedPlayerNames.size()) return null;
        return Universe.get().getPlayerByUsername(cachedPlayerNames.get(selectedPlayer),
                com.hypixel.hytale.server.core.NameMatching.EXACT_IGNORE_CASE);
    }

    @FunctionalInterface
    private interface TargetAction {
        void apply(Ref<EntityStore> ref, Store<EntityStore> store, PlayerLevelData data);
    }

    private static boolean executeOnTarget(PlayerRef targetRef, TargetAction action) {
        try {
            var tEntityRef = targetRef.getReference();
            if (tEntityRef == null) return false;
            var tStore = tEntityRef.getStore();
            ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
            PlayerLevelData data = tStore.getComponent(tEntityRef, type);
            if (data == null) data = new PlayerLevelData();
            action.apply(tEntityRef, tStore, data);
            tStore.putComponent(tEntityRef, type, data);
            StatCalculator.updatePlayerStats(tEntityRef, tStore, data);
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
}
