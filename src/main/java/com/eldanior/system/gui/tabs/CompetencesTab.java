package com.eldanior.system.gui.tabs;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.Metier;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.*;

public class CompetencesTab {

    public static final int MAX_SKILL_SLOTS = 20;

    /** Per-player state for pagination, tab view, and cooldowns */
    private static final java.util.concurrent.ConcurrentHashMap<java.util.UUID, PlayerSkillState> playerStates = new java.util.concurrent.ConcurrentHashMap<>();

    private static PlayerSkillState getState(java.util.UUID uuid) {
        return playerStates.computeIfAbsent(uuid, k -> new PlayerSkillState());
    }

    private static class PlayerSkillState {
        int currentPage = 0;
        boolean viewingActives = true;                 // runes actives d'abord (2026-11-12)
        boolean hasCooldownActive = false;
        final Map<Integer, CooldownEntry> activeCooldowns = new HashMap<>();
        String pendingForget = null;                   // passif en attente de confirmation d'oubli
        int filter = 0;                                // filtre par theme (0 = tout)
    }

    /** Une ligne de la liste : un passif (avec son rang) ou un metier. */
    private record Entry(String id, PassiveSkill skill, int rank, Metier metier, boolean learned) {}

    /** Passifs actifs ou desactives du joueur (classe + appris), puis ses metiers. Tri : rang le plus haut d'abord. */
    private static List<Entry> buildEntries(PlayerLevelData data) {
        Map<PassiveSkill, Integer> ranks = new LinkedHashMap<>();
        ClassModel classModel = ClassManager.get(data.getPlayerClassId());
        if (classModel != null) for (PassiveSkill.Ranked r : classModel.getClassPassives()) ranks.merge(r.skill(), r.rank(), Math::max);
        for (String id : data.getUnlockedSkills()) {
            PassiveSkill p = PassiveSkill.fromName(id);
            if (p != null) ranks.merge(p, data.getLearnedRank(id), Math::max);
        }
        List<Entry> passives = new ArrayList<>();
        for (Map.Entry<PassiveSkill, Integer> e : ranks.entrySet()) {
            passives.add(new Entry(e.getKey().name(), e.getKey(), e.getValue(), null, data.getLearnedRank(e.getKey().name()) > 0));
        }
        passives.sort(Comparator.comparingInt((Entry e) -> -e.rank()).thenComparing(e -> e.skill().getTheme().ordinal())
                .thenComparing(e -> e.skill().getDisplayName()));
        List<Entry> out = new ArrayList<>(passives);
        for (Metier m : Metier.values()) {
            if (data.hasMetier(m)) out.add(new Entry(m.name(), null, 0, m, data.getUnlockedSkills().contains(m.name())));
        }
        return out;
    }

    private static java.util.UUID getPlayerUUID(Ref<EntityStore> ref, Store<EntityStore> store) {
        com.hypixel.hytale.server.core.universe.PlayerRef pRef = store.getComponent(ref, com.hypixel.hytale.server.core.universe.PlayerRef.getComponentType());
        if (pRef == null) return new java.util.UUID(0, 0);
        try { return com.eldanior.system.config.UUIDExtractor.getUUID(pRef); } catch (Exception e) { return new java.util.UUID(0, 0); }
    }

    /** Filtres par theme (boutons #SkillFilter0..9). */
    private static final String[] FILTERS = {"Tout", "Attaque", "Magie", "Défense", "Vie", "Endurance", "Agilité", "Chance", "Divin", "Métiers"};

    private static boolean matches(Entry e, int filter) {
        if (filter <= 0) return true;
        if (filter == 9) return e.metier() != null;
        if (e.skill() == null) return false;
        return switch (filter) {
            case 1 -> e.skill().getTheme() == PassiveSkill.Theme.ATTAQUE;
            case 2 -> e.skill().getTheme() == PassiveSkill.Theme.MAGIE;
            case 3 -> e.skill().getTheme() == PassiveSkill.Theme.DEFENSE;
            case 4 -> e.skill().getTheme() == PassiveSkill.Theme.VIE;
            case 5 -> e.skill().getTheme() == PassiveSkill.Theme.ENDURANCE;
            case 6 -> e.skill().getTheme() == PassiveSkill.Theme.AGILITE;
            case 7 -> e.skill().getTheme() == PassiveSkill.Theme.CHANCE;
            case 8 -> e.skill().isDivine();
            default -> true;
        };
    }

    /** Liste affichee : celle du joueur, filtree par theme. */
    private static List<Entry> visibleEntries(PlayerLevelData data, PlayerSkillState state) {
        List<Entry> out = new ArrayList<>();
        for (Entry e : buildEntries(data)) if (matches(e, state.filter)) out.add(e);
        return out;
    }

    public static void populate(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) return;
        java.util.UUID playerUUID = getPlayerUUID(ref, store);
        PlayerSkillState state = getState(playerUUID);
        state.hasCooldownActive = false;
        state.activeCooldowns.clear();

        ui.set("#SkillTabPassive.Background", state.viewingActives ? "#141a24" : "#3d2f14");
        ui.set("#SkillTabActive.Background", state.viewingActives ? "#3d2f14" : "#141a24");
        ui.set("#SkillTabPassiveOn.Visible", !state.viewingActives);
        ui.set("#SkillTabActiveOn.Visible", state.viewingActives);

        // Actives = runes (emplacements et sac, RunesTab) ; Passives = competences passives et metiers (liste ci-dessous)
        ui.set("#RuneManager.Visible", state.viewingActives);
        ui.set("#SkillPager.Visible", !state.viewingActives);
        ui.set("#SkillCardList.Visible", !state.viewingActives);
        ui.set("#SkillFilters.Visible", !state.viewingActives);
        ui.set("#SkillSummary.Visible", !state.viewingActives);
        if (state.viewingActives) {
            for (int i = 0; i < MAX_SKILL_SLOTS; i++) ui.set("#SkillCard" + i + ".Visible", false);
            RunesTab.populate(ui, ref, store);
            return;
        }

        // Filtres et resume
        for (int k = 0; k < FILTERS.length; k++) {
            boolean on = k == state.filter;
            ui.set("#SkillFilter" + k + ".Background", on ? "#3d2f14" : "#141a24");
        }
        List<Entry> all = buildEntries(data);
        int actives = 0, mastered = 0, metiers = 0;
        for (Entry e : all) {
            if (e.metier() != null) { metiers++; continue; }
            if (data.isSkillEnabled(e.id())) actives++;
            if (data.isSkillMastered(e.id())) mastered++;
        }
        ui.set("#SkillSummary.Text", actives + " passif(s) actif(s)   ·   " + data.countLearnedPassives() + " / " + PlayerLevelData.MAX_LEARNED_PASSIVES
                + " appris   ·   " + mastered + " maîtrisé(s)   ·   " + metiers + " métier(s)");

        List<Entry> list = visibleEntries(data, state);
        int totalPages = Math.max(1, (int) Math.ceil((double) list.size() / MAX_SKILL_SLOTS));
        if (state.currentPage >= totalPages) state.currentPage = totalPages - 1;
        if (state.currentPage < 0) state.currentPage = 0;
        int startIdx = state.currentPage * MAX_SKILL_SLOTS;

        ui.set("#SkillCount.Text", state.filter == 0 ? "PASSIFS ET MÉTIERS (" + all.size() + ")"
                : FILTERS[state.filter].toUpperCase() + " (" + list.size() + ")");
        ui.set("#SkillPage.Text", "Page " + (state.currentPage + 1) + " / " + totalPages);

        for (int i = 0; i < MAX_SKILL_SLOTS; i++) {
            int listIdx = startIdx + i;
            if (listIdx >= list.size()) { ui.set("#SkillCard" + i + ".Visible", false); continue; }
            Entry e = list.get(listIdx);
            ui.set("#SkillCard" + i + ".Visible", true);

            if (e.metier() != null) {
                ui.set("#SkillName" + i + ".Text", e.metier().getDisplayName());
                origin(ui, i, e.learned() ? "APPRIS" : "CLASSE");
                ui.set("#SkillType" + i + ".Text", "MÉTIER  ·  PALIER " + data.getCraftTier(e.id()).name());
                ui.set("#SkillDesc" + i + ".Text", e.metier().getDescription());
                ui.set("#SkillNext" + i + ".Visible", true);
                ui.set("#SkillNext" + i + ".Text", "Progresse en fabriquant ; ne compte pas dans la limite de passifs.");
                ui.set("#SkillBorder" + i + ".Background", "#b08a4a");
                ui.set("#SkillType" + i + ".Style.TextColor", "#b08a4a");
                ui.set("#SkillPips" + i + ".Visible", false);
                mastery(ui, i, data.getSkillProgression(e.id()), data.isSkillMastered(e.id()));
                ui.set("#SkillCd" + i + ".Visible", false);
                ui.set("#SkillActiveBar" + i + ".Visible", true);
                ui.set("#SkillStateBadge" + i + ".Visible", false);
                ui.set("#SkillToggle" + i + ".Visible", false);
                ui.set("#SkillCdTimer" + i + ".Visible", false);
                ui.set("#SkillToggleWrap" + i + ".Background", "#2a2410");
                ui.set("#SkillForget" + i + ".Visible", false);
                continue;
            }

            PassiveSkill skill = e.skill();
            int rank = e.rank();
            String color = getSkillRarityColor(PassiveSkill.rankKey(rank));
            boolean isMastered = data.isSkillMastered(e.id());
            ui.set("#SkillName" + i + ".Text", skill.getDisplayName());
            origin(ui, i, e.learned() ? "APPRIS" : "CLASSE");
            String rankName = skill.getRankName(rank);
            ui.set("#SkillType" + i + ".Text", skill.isDivine()
                    ? skill.getTheme().label + "  ·  Divin  ·  un seul pouvoir divin"
                    : PassiveSkill.rankLabel(rank) + "  ·  rang " + rank + " / " + skill.getMaxRank() + "  ·  " + skill.getTheme().label
                    + (rankName.equals(skill.getDisplayName()) ? "" : "  ·  " + rankName));
            ui.set("#SkillDesc" + i + ".Text", skill.getDescription(rank) + (isMastered ? "  (maîtrisé : effet +10 %)" : ""));
            ui.set("#SkillBorder" + i + ".Background", color);
            ui.set("#SkillType" + i + ".Style.TextColor", color);

            // Prochain rang
            int next = 0;
            for (int r = rank + 1; r <= skill.getMaxRank() && !skill.isDivine(); r++) if (skill.hasRank(r)) { next = r; break; }
            ui.set("#SkillNext" + i + ".Visible", next > 0 || (!skill.isDivine() && rank >= skill.getMaxRank()));
            if (next > 0) ui.set("#SkillNext" + i + ".Text", "Rang suivant (" + PassiveSkill.rankLabel(next) + ") : " + skill.getDescription(next));
            else if (!skill.isDivine()) ui.set("#SkillNext" + i + ".Text", "Rang le plus haut atteint.");

            // Pastilles de rang : atteint = couleur du rang, a venir = gris, absent de la serie = presque invisible
            ui.set("#SkillPips" + i + ".Visible", !skill.isDivine());
            if (!skill.isDivine()) {
                for (int k = 0; k < 6; k++) {
                    int r = k + 1;
                    String c = !skill.hasRank(r) ? "#141a22" : r <= rank ? getSkillRarityColor(PassiveSkill.rankKey(r)) : "#2c3644";
                    ui.set("#SkillPip" + i + "R" + k + ".Background", c);
                }
            }
            mastery(ui, i, data.getSkillProgression(e.id()), isMastered);

            ui.set("#SkillCd" + i + ".Visible", skill.getCooldownSeconds() > 0);
            if (skill.getCooldownSeconds() > 0) ui.set("#SkillCdVal" + i + ".Text", formatCooldownMax(skill.getCooldownSeconds()));

            // Oublier : seulement un passif appris (un passif de classe reste) ; deuxieme clic = confirmation
            boolean confirm = e.id().equals(state.pendingForget);
            ui.set("#SkillForget" + i + ".Visible", e.learned());
            ui.set("#SkillForget" + i + ".Text", confirm ? "SÛR ?" : "OUBLIER");

            long remainingCd = data.getRemainingCooldown(e.id());
            boolean enabled = data.isSkillEnabled(e.id());
            if (remainingCd > 0) {
                state.hasCooldownActive = true;
                state.activeCooldowns.put(i, new CooldownEntry(System.currentTimeMillis() + remainingCd));
                ui.set("#SkillActiveBar" + i + ".Visible", false);
                ui.set("#SkillStateBadge" + i + ".Visible", true);
                ui.set("#SkillStateBadge" + i + ".Text", "RECHARGE");
                ui.set("#SkillStateBadge" + i + ".Style.TextColor", "#ef9f27");
                ui.set("#SkillStateBadge" + i + ".Background", "#2a2410");
                ui.set("#SkillToggleWrap" + i + ".Background", "#2a2410");
                ui.set("#SkillToggle" + i + ".Visible", false);
                ui.set("#SkillCdTimer" + i + ".Visible", true);
                ui.set("#SkillCdTimer" + i + ".Text", formatCooldown(remainingCd));
            } else if (enabled) {
                ui.set("#SkillActiveBar" + i + ".Visible", true);
                ui.set("#SkillStateBadge" + i + ".Visible", true);
                ui.set("#SkillStateBadge" + i + ".Text", "ACTIF");
                ui.set("#SkillStateBadge" + i + ".Style.TextColor", "#7ed47e");
                ui.set("#SkillStateBadge" + i + ".Background", "#1a3a1a");
                ui.set("#SkillToggleWrap" + i + ".Background", "#16301a");
                ui.set("#SkillToggle" + i + ".Visible", true);
                ui.set("#SkillToggle" + i + ".Text", "ON");
                ui.set("#SkillCdTimer" + i + ".Visible", false);
            } else {
                ui.set("#SkillActiveBar" + i + ".Visible", false);
                ui.set("#SkillStateBadge" + i + ".Visible", true);
                ui.set("#SkillStateBadge" + i + ".Text", "DÉSACTIVÉ");
                ui.set("#SkillStateBadge" + i + ".Style.TextColor", "#8a96a6");
                ui.set("#SkillStateBadge" + i + ".Background", "#222a36");
                ui.set("#SkillToggleWrap" + i + ".Background", "#1e2430");
                ui.set("#SkillToggle" + i + ".Visible", true);
                ui.set("#SkillToggle" + i + ".Text", "OFF");
                ui.set("#SkillCdTimer" + i + ".Visible", false);
            }
        }
    }

    /** Etiquette d'origine : CLASSE (donne par la classe) ou APPRIS (page, parchemin). */
    private static void origin(UICommandBuilder ui, int i, String text) {
        ui.set("#SkillOrigin" + i + ".Visible", true);
        ui.set("#SkillOrigin" + i + ".Text", text);
        ui.set("#SkillOrigin" + i + ".Background", "CLASSE".equals(text) ? "#2b2440" : "#1f3040");
        ui.set("#SkillOrigin" + i + ".Style.TextColor", "CLASSE".equals(text) ? "#c3a8f0" : "#9fd0f0");
    }

    /** Barre de maitrise en 10 segments et pourcentage (MAITRISE dore a 100 %). */
    private static void mastery(UICommandBuilder ui, int i, float progression, boolean mastered) {
        int filled = mastered ? 10 : (int) Math.floor(progression / 10f);
        for (int k = 0; k < 10; k++) {
            ui.set("#SkillMa" + i + "S" + k + ".Background", k < filled ? (mastered ? "#FFD700" : "#4caf50") : "#222a36");
        }
        ui.set("#SkillMasteryVal" + i + ".Text", mastered ? "MAÎTRISÉ" : String.format(java.util.Locale.FRANCE, "%.1f %%", progression));
        ui.set("#SkillMasteryVal" + i + ".Style.TextColor", mastered ? "#FFD700" : "#7ed47e");
    }

    /** Choix d'un filtre par theme. */
    public static void setFilter(java.util.UUID uuid, String index) {
        PlayerSkillState s = getState(uuid);
        try { s.filter = Math.max(0, Math.min(FILTERS.length - 1, Integer.parseInt(index))); } catch (NumberFormatException ignored) { }
        s.currentPage = 0;
        s.pendingForget = null;
    }

    private static Entry entryAt(PlayerLevelData data, PlayerSkillState state, String slotIndex) {
        int idx;
        try { idx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return null; }
        List<Entry> list = visibleEntries(data, state);
        int realIdx = state.currentPage * MAX_SKILL_SLOTS + idx;
        return realIdx >= 0 && realIdx < list.size() ? list.get(realIdx) : null;
    }

    /** Active ou desactive un passif (les metiers ne se desactivent pas). */
    public static boolean handleToggle(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null || getState(getPlayerUUID(ref, store)).viewingActives) return false;
        Entry e = entryAt(data, getState(getPlayerUUID(ref, store)), slotIndex);
        if (e == null || e.skill() == null) return false;
        if (data.isSkillEnabled(e.id())) data.disableSkill(e.id());
        else data.enableSkill(e.id());
        getState(getPlayerUUID(ref, store)).pendingForget = null;
        store.putComponent(ref, type, data);
        com.eldanior.system.Leveling.utils.StatCalculator.updatePlayerStats(ref, store, data);
        return true;
    }

    /** Oublier un passif appris : premier clic = « Sûr ? », second clic = oubli (rang et maitrise perdus). */
    public static boolean handleForget(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        PlayerSkillState state = getState(getPlayerUUID(ref, store));
        if (data == null || state.viewingActives) return false;
        Entry e = entryAt(data, state, slotIndex);
        if (e == null || e.skill() == null || !e.learned()) return false;
        if (!e.id().equals(state.pendingForget)) { state.pendingForget = e.id(); return true; }
        state.pendingForget = null;
        data.forgetPassive(e.id());
        store.putComponent(ref, type, data);
        com.eldanior.system.Leveling.utils.StatCalculator.updatePlayerStats(ref, store, data);
        com.hypixel.hytale.server.core.universe.PlayerRef pr = store.getComponent(ref, com.hypixel.hytale.server.core.universe.PlayerRef.getComponentType());
        if (pr != null) pr.sendMessage(com.hypixel.hytale.server.core.Message.raw("Passif oublié : " + e.skill().getDisplayName()
                + ". (" + data.countLearnedPassives() + " / " + PlayerLevelData.MAX_LEARNED_PASSIVES + " appris)"));
        return true;
    }

    public static boolean hasCooldownActive() {
        // Check if ANY player has active cooldowns (used by SystemScreen to decide timer)
        for (PlayerSkillState s : playerStates.values()) {
            if (s.hasCooldownActive) return true;
        }
        return false;
    }

    public static boolean hasCooldownActive(java.util.UUID uuid) {
        PlayerSkillState state = playerStates.get(uuid);
        return state != null && state.hasCooldownActive;
    }

    // Met a jour UNIQUEMENT les timers cooldown (pas besoin du store)
    public static void updateCooldownTimers(UICommandBuilder ui, java.util.UUID uuid) {
        PlayerSkillState state = playerStates.get(uuid);
        if (state == null) return;
        boolean anyActive = false;
        for (var entry : state.activeCooldowns.entrySet()) {
            int slot = entry.getKey();
            CooldownEntry cd = entry.getValue();
            long remaining = cd.endTime - System.currentTimeMillis();
            if (remaining > 0) {
                ui.set("#SkillCdTimer" + slot + ".Text", formatCooldown(remaining));
                anyActive = true;
            } else {
                // Cooldown termine -- repasser en etat ACTIF
                ui.set("#SkillStateBadge" + slot + ".Visible", true);
                ui.set("#SkillStateBadge" + slot + ".Text", "ACTIF");
                ui.set("#SkillStateBadge" + slot + ".Style.TextColor", "#7ed47e");
                ui.set("#SkillStateBadge" + slot + ".Background", "#1a3a1a");
                ui.set("#SkillActiveBar" + slot + ".Visible", true);
                ui.set("#SkillToggleWrap" + slot + ".Background", "#1a3a1a");
                ui.set("#SkillToggle" + slot + ".Visible", true);
                ui.set("#SkillToggle" + slot + ".Text", "ON");
                ui.set("#SkillCdTimer" + slot + ".Visible", false);
            }
        }
        if (!anyActive) {
            state.activeCooldowns.clear();
            state.hasCooldownActive = false;
        }
    }

    /** @deprecated Use updateCooldownTimers(ui, uuid) instead */
    public static void updateCooldownTimers(UICommandBuilder ui) {
        // Fallback: update all players (backward compat)
        for (var entry : playerStates.entrySet()) {
            updateCooldownTimers(ui, entry.getKey());
        }
    }

    private static class CooldownEntry {
        final long endTime;
        CooldownEntry(long endTime) { this.endTime = endTime; }
    }

    public static void nextPage(java.util.UUID uuid) { getState(uuid).currentPage++; }
    public static void prevPage(java.util.UUID uuid) { PlayerSkillState s = getState(uuid); if (s.currentPage > 0) s.currentPage--; }
    public static void switchToPassives(java.util.UUID uuid) { PlayerSkillState s = getState(uuid); s.viewingActives = false; s.currentPage = 0; s.pendingForget = null; }
    public static void switchToActives(java.util.UUID uuid) { PlayerSkillState s = getState(uuid); s.viewingActives = true; s.currentPage = 0; }

    /** @deprecated Use nextPage(uuid) */
    public static void nextPage() { }
    /** @deprecated Use prevPage(uuid) */
    public static void prevPage() { }
    /** @deprecated Use switchToPassives(uuid) */
    public static void switchToPassives() { }
    /** @deprecated Use switchToActives(uuid) */
    public static void switchToActives() { }

    private static String getSkillRarityColor(String rarity) {
        return switch (rarity) {
            case "DIVIN" -> "#7EB8DA";
            case "LEGENDAIRE" -> "#FFD700";
            case "EPIQUE" -> "#9C27B0";
            case "UNIQUE" -> "#cc4444";
            case "RARE" -> "#2196F3";
            case "UNCOMMON" -> "#4CAF50";
            case "FAMILLE" -> "#E91E63";
            default -> "#8899aa";
        };
    }

    private static String formatCooldown(long remainingMs) {
        long totalSeconds = remainingMs / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        if (minutes > 0) {
            return minutes + "m" + String.format("%02d", seconds) + "s";
        }
        return seconds + "s";
    }

    private static String formatCooldownMax(float seconds) {
        int min = (int)(seconds / 60);
        int sec = (int)(seconds % 60);
        if (min > 0 && sec > 0) return min + "m" + String.format("%02d", sec) + "s";
        if (min > 0) return min + "m";
        return sec + "s";
    }

}
