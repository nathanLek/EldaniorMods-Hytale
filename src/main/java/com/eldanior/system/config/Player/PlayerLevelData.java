package com.eldanior.system.config.Player;

import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.eldanior.system.titles.TitleManager;
import com.eldanior.system.titles.models.TitleBonus;
import com.eldanior.system.titles.models.TitleModel;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nullable;
import java.util.*;

public class PlayerLevelData implements Component<EntityStore> {

    public static ComponentType<EntityStore, PlayerLevelData> TYPE;

    // --- Variables ---
    private int level = 1;
    private int experience = 0;
    private int attributePoints = 0;

    // Identité
    private String playerClass = "Novice";
    private String classId = "novice";
    private String currentTitle = "";
    private List<String> unlockedTitles = new ArrayList<>();
    private String guildRank = "F";
    private String activeSkillId = "";

    // Noblesse
    private String nobilityRank = "ROTURIER";
    private String nobleFamilyId = "";
    private String status = "";  // PATRIARCH, VICE, MEMBER, ou vide
    private int dignity = 0;

    // Eglise
    private String churchRank = "LAIQUE";
    private int faith = 0;

    // Exploration
    private int chestsDiscovered = 0;

    // Territoire
    private int territoriesDiscovered = 0;
    private int dungeonsDiscovered = 0;
    private Set<String> discoveredParcelIds = new HashSet<>();

    // PvP
    private int playerKills = 0;
    private int playerDeaths = 0;
    private int killStreak = 0;
    private int bestKillStreak = 0;

    // Duel
    private int duelWins = 0;
    private int duelLosses = 0;
    private int duelStreak = 0;
    private int duelBestStreak = 0;
    private boolean forcePK = false;
    private long bounty = 0; // Prime sur la tete du criminel
    private String questData = ""; // Serialized quest data
    private String cooldownData = ""; // Serialized cooldowns: questId=timestamp|...
    private transient long lastPvPKillTime = 0; // Pour le timer de redemption
    private String duelHistoryData = ""; // Serialized duel history
    private transient List<String> duelHistory = new ArrayList<>();

    // Guilde
    private String guildId = "";
    private String guildRole = "";  // CHEF, OFFICER, MEMBER

    // Stats de BASE (Investies par le joueur)
    private int strength = 1;
    private int endurance = 1;
    private int agility = 1;
    private int vitality = 1;
    private int intelligence = 1;
    private int luck = 1;
    private long money = 0;
    private UUID lastVictimUUID;
    private int hauntingThrustStacks;
    private transient long lastDamageTakenTime = 0;
    private transient boolean lastAttackWasCrit = false;
    private int currentMana = -1; // -1 = pas encore initialisé, sera mis à maxMana au premier accès

    private List<String> unlockedSkills = new ArrayList<>();
    private Set<String> enabledSkills = new HashSet<>();
    private Set<String> disabledSkills = new HashSet<>();
    private transient Map<String, Long> cooldowns = new HashMap<>();

    // Kill tracking par type de mob (ex: "goblin_scrapper" -> 150)
    private Map<String, Integer> mobKills = new HashMap<>();

    // Skill progression : nombre de procs par skill (ex: "PHANTOM_STRIKE" -> 4523)
    private Map<String, Integer> skillProcs = new HashMap<>();

    // Evolution de classe : choix sauvegardés + rerolls
    private String savedEvolutionChoices = ""; // IDs séparés par des virgules (ex: "templier,paladin,berserker")
    private int evolutionRerolls = 0; // Nombre de relances utilisées (max 2 pour les joueurs normaux)

    public PlayerLevelData() {
        this.unlockedTitles.add("novice");
        this.currentTitle = "novice";
    }

    public static final int MAX_LEVEL = 500;

    public int getRequiredExperience() {
        return (int) (100 + 140 * (level - 1) + 5L * (level - 1) * (level - 1));
    }

    public void addExperience(int amount) {
        if (this.level >= MAX_LEVEL) return;
        this.experience += amount;
        while (this.experience >= getRequiredExperience() && this.level < MAX_LEVEL) {
            this.experience -= getRequiredExperience();
            this.level++;
            this.attributePoints += 3;
        }
    }

    public int removeExperiencePercent(double percent) {
        int loss = (int) (this.experience * percent);
        this.experience = Math.max(0, this.experience - loss);
        return loss;
    }

    public float getExperienceProgress() {
        return (float) this.experience / getRequiredExperience();
    }

    public int getCurrentMana() {
        if (currentMana < 0) currentMana = getMaxMana();
        return currentMana;
    }

    public int getRawCurrentMana() {
        return currentMana;
    }

    public void setCurrentMana(int mana) {
        this.currentMana = Math.max(0, mana);
    }

    public boolean hasEnoughMana(float amount) {
        return getCurrentMana() >= (int) amount;
    }

    public void consumeMana(int amount) {
        if (currentMana < 0) currentMana = getMaxMana();
        currentMana = Math.max(0, currentMana - amount);
    }

    public void restoreMana(int amount) {
        if (currentMana < 0) currentMana = getMaxMana();
        currentMana = Math.min(getMaxMana(), currentMana + amount);
    }

    public boolean canCast(String skillId) {
        if (!cooldowns.containsKey(skillId)) return true;
        return System.currentTimeMillis() >= cooldowns.get(skillId);
    }

    public void applyCooldown(String skillId, float durationSeconds) {
        long readyTime = System.currentTimeMillis() + (long)(durationSeconds * 1000);
        cooldowns.put(skillId, readyTime);
    }

    public long getRemainingCooldown(String skillId) {
        if (!cooldowns.containsKey(skillId)) return 0;
        return Math.max(0, cooldowns.get(skillId) - System.currentTimeMillis());
    }

    // --- SKILL PROGRESSION (1 proc = 0.01%, 10000 procs = 100%) ---
    private static final int PROCS_TO_MASTER = 10000;

    public void addSkillProc(String skillId) {
        skillProcs.put(skillId, Math.min(PROCS_TO_MASTER, skillProcs.getOrDefault(skillId, 0) + 1));
    }

    public void setSkillProcs(String skillId, int procs) {
        skillProcs.put(skillId, Math.min(PROCS_TO_MASTER, Math.max(0, procs)));
    }

    public int getSkillProcs(String skillId) {
        return skillProcs.getOrDefault(skillId, 0);
    }

    public float getSkillProgression(String skillId) {
        return (float) getSkillProcs(skillId) / PROCS_TO_MASTER * 100f;
    }

    public boolean isSkillMastered(String skillId) {
        return getSkillProcs(skillId) >= PROCS_TO_MASTER;
    }

    public com.eldanior.system.Leveling.CraftTier getCraftTier(String skillId) {
        return com.eldanior.system.Leveling.CraftTier.fromProcs(getSkillProcs(skillId));
    }

    // --- LE CODEC ---
    public static final BuilderCodec<PlayerLevelData> CODEC = BuilderCodec.builder(PlayerLevelData.class, PlayerLevelData::new)
            .append(new KeyedCodec<>("Level", Codec.INTEGER), (data, v) -> data.level = v, data -> data.level).add()
            .append(new KeyedCodec<>("Experience", Codec.INTEGER), (data, v) -> data.experience = v, data -> data.experience).add()
            .append(new KeyedCodec<>("AttributePoints", Codec.INTEGER), (data, v) -> data.attributePoints = v, data -> data.attributePoints).add()
            .append(new KeyedCodec<>("PlayerClass", Codec.STRING), (data, v) -> data.playerClass = v, data -> data.playerClass).add()
            .append(new KeyedCodec<>("ClassId", Codec.STRING), (data, v) -> data.classId = v, data -> data.classId).add()
            .append(new KeyedCodec<>("CurrentTitle", Codec.STRING), (data, v) -> data.currentTitle = v, data -> data.currentTitle).add()
            .append(new KeyedCodec<>("GuildRank", Codec.STRING), (data, v) -> data.guildRank = v, data -> data.guildRank).add()
            .append(new KeyedCodec<>("ActiveSkillId", Codec.STRING), (data, v) -> data.activeSkillId = (v != null ? v : ""), data -> (data.activeSkillId != null ? data.activeSkillId : "")).add()
            .append(new KeyedCodec<>("NobilityRank", Codec.STRING), (data, v) -> data.nobilityRank = v, data -> data.nobilityRank).add()
            .append(new KeyedCodec<>("NobleFamilyId", Codec.STRING), (data, v) -> data.nobleFamilyId = v, data -> data.nobleFamilyId).add()
            .append(new KeyedCodec<>("Status", Codec.STRING), (data, v) -> data.status = v, data -> data.status).add()
            .append(new KeyedCodec<>("Dignity", Codec.INTEGER), (data, v) -> data.dignity = v, data -> data.dignity).add()
            .append(new KeyedCodec<>("ChurchRank", Codec.STRING), (data, v) -> data.churchRank = v, data -> data.churchRank).add()
            .append(new KeyedCodec<>("Faith", Codec.INTEGER), (data, v) -> data.faith = v, data -> data.faith).add()
            .append(new KeyedCodec<>("ChestsDiscovered", Codec.INTEGER), (data, v) -> data.chestsDiscovered = v, data -> data.chestsDiscovered).add()
            .append(new KeyedCodec<>("TerritoriesDiscovered", Codec.INTEGER), (data, v) -> data.territoriesDiscovered = v, data -> data.territoriesDiscovered).add()
            .append(new KeyedCodec<>("DungeonsDiscovered", Codec.INTEGER), (data, v) -> data.dungeonsDiscovered = v, data -> data.dungeonsDiscovered).add()
            .append(new KeyedCodec<>("DiscoveredParcelIds", Codec.STRING), (data, value) -> {
                data.discoveredParcelIds = new HashSet<>();
                if (value != null && !value.isEmpty()) {
                    data.discoveredParcelIds.addAll(Arrays.asList(value.split(",")));
                }
            }, (data) -> String.join(",", data.discoveredParcelIds)).add()
            .append(new KeyedCodec<>("PlayerKills", Codec.INTEGER), (data, v) -> data.playerKills = v, data -> data.playerKills).add()
            .append(new KeyedCodec<>("PlayerDeaths", Codec.INTEGER), (data, v) -> data.playerDeaths = v, data -> data.playerDeaths).add()
            .append(new KeyedCodec<>("KillStreak", Codec.INTEGER), (data, v) -> data.killStreak = v, data -> data.killStreak).add()
            .append(new KeyedCodec<>("BestKillStreak", Codec.INTEGER), (data, v) -> data.bestKillStreak = v, data -> data.bestKillStreak).add()
            .append(new KeyedCodec<>("GuildId", Codec.STRING), (data, v) -> data.guildId = v, data -> data.guildId).add()
            .append(new KeyedCodec<>("GuildRole", Codec.STRING), (data, v) -> data.guildRole = v, data -> data.guildRole).add()
            .append(new KeyedCodec<>("Strength", Codec.INTEGER), (data, v) -> data.strength = v, data -> data.strength).add()
            .append(new KeyedCodec<>("Endurance", Codec.INTEGER), (data, v) -> data.endurance = v, data -> data.endurance).add()
            .append(new KeyedCodec<>("Agility", Codec.INTEGER), (data, v) -> data.agility = v, data -> data.agility).add()
            .append(new KeyedCodec<>("Vitality", Codec.INTEGER), (data, v) -> data.vitality = v, data -> data.vitality).add()
            .append(new KeyedCodec<>("Intelligence", Codec.INTEGER), (data, v) -> data.intelligence = v, data -> data.intelligence).add()
            .append(new KeyedCodec<>("Luck", Codec.INTEGER), (data, v) -> data.luck = v, data -> data.luck).add()
            .append(new KeyedCodec<>("Money", Codec.LONG), (data, v) -> data.money = v, data -> data.money).add()
            .append(new KeyedCodec<>("CurrentMana", Codec.INTEGER), (data, v) -> data.currentMana = v, data -> data.getCurrentMana()).add()
            .append(new KeyedCodec<>("UnlockedSkills", Codec.STRING), (data, value) -> {
                data.unlockedSkills = new ArrayList<>();
                if (value != null && !value.isEmpty()) {
                    data.unlockedSkills.addAll(Arrays.asList(value.split(",")));
                }
            }, (data) -> String.join(",", data.unlockedSkills)).add()
            .append(new KeyedCodec<>("EnabledSkills", Codec.STRING), (data, value) -> {
                data.enabledSkills = new HashSet<>();
                if (value != null && !value.isEmpty()) {
                    data.enabledSkills.addAll(Arrays.asList(value.split(",")));
                }
            }, (data) -> String.join(",", data.enabledSkills)).add()
            .append(new KeyedCodec<>("DisabledSkills", Codec.STRING), (data, value) -> {
                data.disabledSkills = new HashSet<>();
                if (value != null && !value.isEmpty()) {
                    data.disabledSkills.addAll(Arrays.asList(value.split(",")));
                }
            }, (data) -> String.join(",", data.disabledSkills)).add()
            .append(new KeyedCodec<>("UnlockedTitles", Codec.STRING), (data, value) -> {
                data.unlockedTitles = new ArrayList<>();
                if (value != null && !value.isEmpty()) {
                    data.unlockedTitles.addAll(Arrays.asList(value.split(",")));
                }
            }, (data) -> String.join(",", data.unlockedTitles)).add()
            .append(new KeyedCodec<>("MobKills", Codec.STRING), (data, value) -> {
                data.mobKills = new HashMap<>();
                if (value != null && !value.isEmpty()) {
                    for (String entry : value.split(",")) {
                        String[] parts = entry.split(":");
                        if (parts.length == 2) {
                            try {
                                data.mobKills.put(parts[0], Integer.parseInt(parts[1]));
                            } catch (NumberFormatException e) { /* format invalide */ }
                        }
                    }
                }
            }, (data) -> {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, Integer> e : data.mobKills.entrySet()) {
                    if (sb.length() > 0) sb.append(",");
                    sb.append(e.getKey()).append(":").append(e.getValue());
                }
                return sb.toString();
            }).add()
            .append(new KeyedCodec<>("SkillProcs", Codec.STRING), (data, value) -> {
                data.skillProcs = new HashMap<>();
                if (value != null && !value.isEmpty()) {
                    for (String entry : value.split(",")) {
                        String[] parts = entry.split(":");
                        if (parts.length == 2) {
                            try {
                                data.skillProcs.put(parts[0], Integer.parseInt(parts[1]));
                            } catch (NumberFormatException e) { /* format invalide */ }
                        }
                    }
                }
            }, (data) -> {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, Integer> e : data.skillProcs.entrySet()) {
                    if (sb.length() > 0) sb.append(",");
                    sb.append(e.getKey()).append(":").append(e.getValue());
                }
                return sb.toString();
            }).add()
            .append(new KeyedCodec<>("DuelWins", Codec.INTEGER), (data, v) -> data.duelWins = v, data -> data.duelWins).add()
            .append(new KeyedCodec<>("DuelLosses", Codec.INTEGER), (data, v) -> data.duelLosses = v, data -> data.duelLosses).add()
            .append(new KeyedCodec<>("ForcePK", Codec.BOOLEAN), (data, v) -> data.forcePK = v, data -> data.forcePK).add()
            .append(new KeyedCodec<>("Bounty", Codec.LONG), (data, v) -> data.bounty = v, data -> data.bounty).add()
            .append(new KeyedCodec<>("Quests", Codec.STRING), (data, v) -> data.questData = v, data -> data.questData).add()
            .append(new KeyedCodec<>("Cooldowns", Codec.STRING), (data, v) -> data.cooldownData = v, data -> data.cooldownData).add()
            .append(new KeyedCodec<>("DuelStreak", Codec.INTEGER), (data, v) -> data.duelStreak = v, data -> data.duelStreak).add()
            .append(new KeyedCodec<>("DuelBestStreak", Codec.INTEGER), (data, v) -> data.duelBestStreak = v, data -> data.duelBestStreak).add()
            .append(new KeyedCodec<>("SavedEvolutionChoices", Codec.STRING), (data, v) -> data.savedEvolutionChoices = (v != null ? v : ""), data -> (data.savedEvolutionChoices != null ? data.savedEvolutionChoices : "")).add()
            .append(new KeyedCodec<>("EvolutionRerolls", Codec.INTEGER), (data, v) -> data.evolutionRerolls = (v != null ? v : 0), data -> data.evolutionRerolls).add()
            .append(new KeyedCodec<>("DuelHistory", Codec.STRING), (data, v) -> { data.duelHistoryData = (v != null ? v : ""); data.deserializeDuelHistory(); }, data -> data.serializeDuelHistory()).add()
            .build();

    // --- Méthode CLONE ---
    @Nullable
    @Override
    public Component<EntityStore> clone() {
        PlayerLevelData copy = new PlayerLevelData();
        copy.level = this.level;
        copy.experience = this.experience;
        copy.attributePoints = this.attributePoints;
        copy.playerClass = this.playerClass;
        copy.classId = this.classId;
        copy.currentTitle = this.currentTitle;
        copy.guildRank = this.guildRank;
        copy.activeSkillId = this.activeSkillId;
        copy.nobilityRank = this.nobilityRank;
        copy.nobleFamilyId = this.nobleFamilyId;
        copy.status = this.status;
        copy.dignity = this.dignity;
        copy.churchRank = this.churchRank;
        copy.faith = this.faith;
        copy.chestsDiscovered = this.chestsDiscovered;
        copy.territoriesDiscovered = this.territoriesDiscovered;
        copy.dungeonsDiscovered = this.dungeonsDiscovered;
        copy.discoveredParcelIds = new HashSet<>(this.discoveredParcelIds);
        copy.playerKills = this.playerKills;
        copy.playerDeaths = this.playerDeaths;
        copy.killStreak = this.killStreak;
        copy.bestKillStreak = this.bestKillStreak;
        copy.forcePK = this.forcePK;
        copy.bounty = this.bounty;
        copy.questData = this.questData;
        copy.cooldownData = this.cooldownData;
        copy.duelWins = this.duelWins;
        copy.duelLosses = this.duelLosses;
        copy.duelStreak = this.duelStreak;
        copy.duelBestStreak = this.duelBestStreak;
        copy.duelHistory = new ArrayList<>(this.getDuelHistory());
        copy.guildId = this.guildId;
        copy.guildRole = this.guildRole;
        copy.strength = this.strength;
        copy.endurance = this.endurance;
        copy.agility = this.agility;
        copy.vitality = this.vitality;
        copy.intelligence = this.intelligence;
        copy.luck = this.luck;
        copy.money = this.money;
        copy.currentMana = this.currentMana;
        copy.lastVictimUUID = this.lastVictimUUID;
        copy.hauntingThrustStacks = this.hauntingThrustStacks;
        copy.lastDamageTakenTime = this.lastDamageTakenTime;


        copy.enabledSkills = new HashSet<>(this.enabledSkills);
        copy.disabledSkills = new HashSet<>(this.disabledSkills);
        if (this.unlockedTitles != null) {
            copy.unlockedTitles = new ArrayList<>(this.unlockedTitles);
        }
        if (this.unlockedSkills != null) {
            copy.unlockedSkills = new ArrayList<>(this.unlockedSkills);
        }

        copy.cooldowns = new HashMap<>(this.cooldowns);
        copy.mobKills = new HashMap<>(this.mobKills);
        copy.skillProcs = new HashMap<>(this.skillProcs);
        copy.savedEvolutionChoices = this.savedEvolutionChoices;
        copy.evolutionRerolls = this.evolutionRerolls;

        return copy;
    }

    // ================= SKILLS METHODS =================

    public List<String> getUnlockedSkills() { return unlockedSkills; }
    public Set<String> getEnabledSkills() { return enabledSkills; }
    public Set<String> getDisabledSkills() { return disabledSkills; }

    public boolean isSkillEnabled(String skillId) {
        // Si explicitement désactivé → non
        if (disabledSkills.contains(skillId)) return false;
        // Si dans enabledSkills → oui
        if (enabledSkills.contains(skillId)) return true;
        // Skills de classe sont actives par défaut (pas dans enabledSkills)
        ClassModel model = ClassManager.get(this.classId);
        if (model != null && model.getSkillsPassiveIds() != null) {
            for (PassiveSkill ps : model.getSkillsPassiveIds()) {
                if (ps.name().equals(skillId)) return true;
            }
        }
        // Skill familial actif par défaut
        if (this.nobleFamilyId != null && !this.nobleFamilyId.isEmpty()) {
            NobleFamilyModel family = FamilyManager.get(this.nobleFamilyId);
            if (family != null && family.getFamilyPassive() != null
                    && family.getFamilyPassive().name().equals(skillId)) return true;
        }
        return false;
    }

    public void enableSkill(String skillId) {
        enabledSkills.add(skillId);
        disabledSkills.remove(skillId);
    }

    public void disableSkill(String skillId) {
        enabledSkills.remove(skillId);
        disabledSkills.add(skillId);
    }
    public void learnSkill(String skillId) {
        if (!unlockedSkills.contains(skillId)) unlockedSkills.add(skillId);
    }
    public void forgetAllSkills() {
        unlockedSkills.clear();
        enabledSkills.clear();
        disabledSkills.clear();
    }
    public String getActiveSkillId() { return activeSkillId; }
    public void setActiveSkillId(String id) { this.activeSkillId = id; }

    // === NOUVEAU GETTER DYNAMIQUE DES PASSIFS ===
    // Retourne uniquement les passifs qui ne sont PAS désactivés manuellement.
    // Une compétence est considérée active si elle n'a pas été explicitement désactivée
    // (c.à.d. si elle n'est pas dans disabledSkills).
    public List<PassiveSkill> getActivePassives() {
        List<PassiveSkill> activePassives = new ArrayList<>();

        // 1. Récupération des passifs "Innés" de la classe
        ClassModel model = ClassManager.get(this.classId);
        if (model != null && model.getSkillsPassiveIds() != null) {
            for (PassiveSkill ps : model.getSkillsPassiveIds()) {
                // Les skills de classe sont actives par défaut, sauf si désactivées manuellement
                if (!disabledSkills.contains(ps.name())) {
                    activePassives.add(ps);
                }
            }
        }

        // 2. Récupération des passifs "Appris" via les parchemins
        for (String skillId : this.unlockedSkills) {
            PassiveSkill learnedPassive = PassiveSkill.fromName(skillId.toUpperCase());
            if (learnedPassive != null && !activePassives.contains(learnedPassive) && !disabledSkills.contains(skillId.toUpperCase())) {
                activePassives.add(learnedPassive);
            }
        }

        // 3. Récupération du passif familial (noblesse)
        if (this.nobleFamilyId != null && !this.nobleFamilyId.isEmpty()) {
            NobleFamilyModel family = FamilyManager.get(this.nobleFamilyId);
            if (family != null && family.getFamilyPassive() != null) {
                PassiveSkill familySkill = family.getFamilyPassive();
                if (!activePassives.contains(familySkill) && !disabledSkills.contains(familySkill.name())) {
                    activePassives.add(familySkill);
                }
            }
        }

        return activePassives;
    }

    public void removeSkill(String skillId) {
        // On la retire des compétences débloquées
        unlockedSkills.remove(skillId);

        // Par sécurité, on la retire aussi des compétences actives/équipées !
        enabledSkills.remove(skillId);
    }

    // ================= GETTERS (Identité & Base) =================
    public int getLevel() { return level; }
    public int getExperience() { return experience; }
    public int getAttributePoints() { return attributePoints; }
    public String getPlayerClass() { return playerClass; }
    public String getPlayerClassId() { return classId; }
    public String getCurrentTitle() { return currentTitle; }
    public List<String> getUnlockedTitles() { return unlockedTitles; }
    public String getGuildRank() { return guildRank; }
    public long getMoney() { return money; }
    public String getNobilityRank() { return nobilityRank; }
    public String getNobleFamilyId() { return nobleFamilyId; }
    public String getStatus() { return status; }
    public int getDignity() { return dignity; }
    public String getChurchRank() { return churchRank; }
    public int getFaith() { return faith; }
    public int getChestsDiscovered() { return chestsDiscovered; }
    public void addChestDiscovered() { this.chestsDiscovered++; }
    public int getTerritoriesDiscovered() { return territoriesDiscovered; }
    public int getDungeonsDiscovered() { return dungeonsDiscovered; }
    public Set<String> getDiscoveredParcelIds() { return discoveredParcelIds; }
    public boolean discoverParcel(String parcelId, boolean isDungeon) {
        if (discoveredParcelIds.add(parcelId)) {
            territoriesDiscovered++;
            if (isDungeon) dungeonsDiscovered++;
            return true;
        }
        return false;
    }

    // Guilde
    public String getGuildId() { return guildId; }
    public String getGuildRole() { return guildRole; }
    public void setGuildId(String id) { this.guildId = id; }
    public void setGuildRole(String role) { this.guildRole = role; }
    public boolean hasGuild() { return guildId != null && !guildId.isEmpty(); }
    public boolean isGuildChef() { return "CHEF".equals(guildRole); }
    public boolean isGuildOfficer() { return "OFFICER".equals(guildRole); }
    public boolean canInviteToGuild() { return isGuildChef() || isGuildOfficer(); }
    public boolean canJoinGuild() {
        // Ne peut pas rejoindre une guilde si membre d'une famille noble
        String familyId = getNobleFamilyId();
        return (familyId == null || familyId.isEmpty()) && !hasGuild();
    }

    // PvP
    public int getPlayerKills() { return playerKills; }
    public int getPlayerDeaths() { return playerDeaths; }
    public int getKillStreak() { return killStreak; }
    public int getBestKillStreak() { return bestKillStreak; }
    public void setPlayerKills(int v) { this.playerKills = v; }
    public void setPlayerDeaths(int v) { this.playerDeaths = v; }
    public void setKillStreak(int v) { this.killStreak = v; }
    public void setBestKillStreak(int v) { this.bestKillStreak = v; }
    public void setChestsDiscovered(int v) { this.chestsDiscovered = v; }

    // Duel
    public int getDuelWins() { return duelWins; }
    public int getDuelLosses() { return duelLosses; }
    public int getDuelTotal() { return duelWins + duelLosses; }
    public int getDuelStreak() { return duelStreak; }
    public int getDuelBestStreak() { return duelBestStreak; }
    public void addDuelWin() {
        this.duelWins++;
        this.duelStreak++;
        if (this.duelStreak > this.duelBestStreak) this.duelBestStreak = this.duelStreak;
    }
    public void addDuelLoss() {
        this.duelLosses++;
        this.duelStreak = 0;
    }
    public List<String> getDuelHistory() { return duelHistory != null ? duelHistory : new ArrayList<>(); }
    public void addDuelHistory(String opponentName, boolean won, float myHPPercent, float opHPPercent) {
        if (duelHistory == null) duelHistory = new ArrayList<>();
        String entry = (won ? "V" : "D") + "|" + opponentName + "|" + (int)(myHPPercent * 100) + "%|" + (int)(opHPPercent * 100) + "%";
        duelHistory.add(0, entry); // Plus recent en premier
        if (duelHistory.size() > 10) duelHistory.remove(duelHistory.size() - 1); // Max 10
    }

    public String serializeDuelHistory() {
        if (duelHistory == null || duelHistory.isEmpty()) return "";
        return String.join(";;", duelHistory);
    }

    public void deserializeDuelHistory() {
        if (duelHistoryData == null || duelHistoryData.isEmpty()) return;
        if (duelHistory == null) duelHistory = new ArrayList<>();
        duelHistory.clear();
        for (String entry : duelHistoryData.split(";;")) {
            if (!entry.isEmpty()) duelHistory.add(entry);
        }
    }

    public double getKDR() {
        if (playerDeaths == 0) return playerKills;
        return (double) playerKills / playerDeaths;
    }

    public boolean isPK() {
        return forcePK || (playerKills >= 10 && getKDR() >= 2.0);
    }
    public boolean isForcePK() { return forcePK; }
    public void setForcePK(boolean v) { this.forcePK = v; }
    public String getQuestData() { return questData; }
    public void setQuestData(String v) { this.questData = v; }
    public String getCooldownData() { return cooldownData; }
    public void setCooldownData(String v) { this.cooldownData = v; }
    public long getBounty() { return bounty; }
    public void addBounty(long amount) { this.bounty += amount; }
    public long collectBounty() { long b = bounty; bounty = 0; return b; }
    public long getLastPvPKillTime() { return lastPvPKillTime; }
    public void setLastPvPKillTime(long t) { this.lastPvPKillTime = t; }

    public void addPlayerKill() {
        this.playerKills++;
        this.killStreak++;
        if (this.killStreak > this.bestKillStreak) {
            this.bestKillStreak = this.killStreak;
        }
        // Devenir PK = excommunication automatique
        if (isPK() && !"LAIQUE".equals(this.churchRank)) {
            this.churchRank = "LAIQUE";
            this.faith = 0;
        }
    }

    public void addPlayerDeath() {
        this.playerDeaths++;
        this.killStreak = 0;
    }

    // Points investis par le joueur (sans bonus de classe)
    public int getStrength() { return strength; }
    public int getEndurance() { return endurance; }
    public int getAgility() { return agility; }
    public int getVitality() { return vitality; }
    public int getIntelligence() { return intelligence; }
    public int getLuck() { return luck; }

    // ================= GETTERS (TOTAUX AVEC BONUS DE CLASSE + TITRE) =================
    public int getTotalStrength() { return strength + getClassBonusStr() + getTitleBonusStr(); }
    public int getTotalEndurance() { return endurance + getClassBonusEnd() + getTitleBonusEnd(); }
    public int getTotalAgility() { return agility + getClassBonusAgl() + getTitleBonusAgl(); }
    public int getTotalVitality() { return vitality + getClassBonusVit() + getTitleBonusVit(); }
    public int getTotalIntelligence() { return intelligence + getClassBonusInt() + getTitleBonusInt(); }
    public int getTotalLuck() { return luck + getClassBonusLck() + getTitleBonusLck(); }

    public int getMaxMana() { return getTotalIntelligence() * 10; }
    public int getMaxHealth() { return getTotalVitality() * 10; }

    // Utilitaires internes pour récupérer le bonus de classe
    private int getClassBonusStr() {
        ClassModel model = ClassManager.get(this.classId);
        return model != null ? model.getBonusStr() : 0;
    }
    private int getClassBonusEnd() {
        ClassModel model = ClassManager.get(this.classId);
        return model != null ? model.getBonusEnd() : 0;
    }
    private int getClassBonusAgl() {
        ClassModel model = ClassManager.get(this.classId);
        return model != null ? model.getBonusAgl() : 0;
    }
    private int getClassBonusVit() {
        ClassModel model = ClassManager.get(this.classId);
        return model != null ? model.getBonusVit() : 0;
    }
    private int getClassBonusInt() {
        ClassModel model = ClassManager.get(this.classId);
        return model != null ? model.getBonusInt() : 0;
    }
    private int getClassBonusLck() {
        ClassModel model = ClassManager.get(this.classId);
        return model != null ? model.getBonusLck() : 0;
    }

    // Utilitaires internes pour recuperer le bonus de titre
    private TitleBonus getTitleBonus() {
        if (currentTitle == null || currentTitle.isEmpty()) return TitleBonus.NONE;
        TitleModel title = TitleManager.get(currentTitle);
        return title != null ? title.getBonus() : TitleBonus.NONE;
    }
    private int getTitleBonusStr() { return getTitleBonus().strength(); }
    private int getTitleBonusVit() { return getTitleBonus().vitality(); }
    private int getTitleBonusInt() { return getTitleBonus().intelligence(); }
    private int getTitleBonusEnd() { return getTitleBonus().endurance(); }
    private int getTitleBonusAgl() { return getTitleBonus().agility(); }
    private int getTitleBonusLck() { return getTitleBonus().luck(); }

    public UUID getLastVictimUUID() { return lastVictimUUID; }
    public void setLastVictimUUID(UUID lastVictimUUID) { this.lastVictimUUID = lastVictimUUID; }
    public int getHauntingThrustStacks() { return hauntingThrustStacks; }
    public void setHauntingThrustStacks(int stacks) { this.hauntingThrustStacks = stacks; }
    public long getLastDamageTakenTime() { return lastDamageTakenTime; }
    public void setLastDamageTakenTime(long time) { this.lastDamageTakenTime = time; }
    public boolean wasLastAttackCrit() { return lastAttackWasCrit; }
    public void setLastAttackWasCrit(boolean crit) { this.lastAttackWasCrit = crit; }

    // ================= SETTERS =================
    public void setLevel(int level) { this.level = level; }
    public void setExperience(int experience) { this.experience = experience; }
    public void setAttributePoints(int points) { this.attributePoints = points; }
    public void setPlayerClass(String playerClass) { this.playerClass = playerClass; }
    public void setPlayerClassId(String classId) { this.classId = classId; }

    // --- Evolution de classe : sauvegarde des choix ---
    public List<String> getSavedEvolutionChoices() {
        if (savedEvolutionChoices == null || savedEvolutionChoices.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(savedEvolutionChoices.split(",")));
    }
    public void setSavedEvolutionChoices(List<String> choices) {
        this.savedEvolutionChoices = (choices == null || choices.isEmpty()) ? "" : String.join(",", choices);
    }
    public void clearSavedEvolutionChoices() { this.savedEvolutionChoices = ""; }
    public int getEvolutionRerolls() { return evolutionRerolls; }
    public void setEvolutionRerolls(int rerolls) { this.evolutionRerolls = rerolls; }
    public void useReroll() { this.evolutionRerolls++; }

    public void setCurrentTitle(String title) { this.currentTitle = title; }
    public void setGuildRank(String guildRank) { this.guildRank = guildRank; }
    public void setNobilityRank(String rank) { this.nobilityRank = rank; }
    public void setNobleFamilyId(String id) { this.nobleFamilyId = id; }
    public void setStatus(String role) { this.status = role; }
    public void setDignity(int dignity) { this.dignity = dignity; }
    public void setChurchRank(String rank) { this.churchRank = rank; }
    public void setFaith(int faith) { this.faith = faith; }

    public boolean isPatriarch() { return "PATRIARCH".equals(status); }
    public boolean isVicePatriarch() { return "VICE".equals(status); }
    public boolean canInviteToFamily() { return isPatriarch() || isVicePatriarch(); }

    // Ces setters ne modifient que les points de base (ex: lors d'un Level Up)
    public void setStrength(int strength) { this.strength = strength; }
    public void setEndurance(int endurance) { this.endurance = endurance; }
    public void setAgility(int agility) { this.agility = agility; }
    public void setVitality(int vitality) { this.vitality = vitality; }
    public void setIntelligence(int intelligence) { this.intelligence = intelligence; }
    public void setLuck(int luck) { this.luck = luck; }
    public void setMoney(long money) { this.money = Math.max(0, money); }

    public void addTitle(String title) {
        if (this.unlockedTitles == null) this.unlockedTitles = new ArrayList<>();
        if (!this.unlockedTitles.contains(title)) {
            this.unlockedTitles.add(title);
        }
    }

    public void removeTitle(String titleId) {
        if (this.unlockedTitles != null) {
            this.unlockedTitles.remove(titleId);
        }
        // Si le titre retiré était équipé, on repasse sur "novice"
        if (titleId.equals(this.currentTitle)) {
            this.currentTitle = "novice";
        }
    }

    public void resetTitles() {
        this.unlockedTitles = new ArrayList<>();
        this.unlockedTitles.add("novice");
        this.currentTitle = "novice";
        this.mobKills = new HashMap<>();
        this.chestsDiscovered = 0;
        this.territoriesDiscovered = 0;
        this.dungeonsDiscovered = 0;
        this.discoveredParcelIds = new HashSet<>();
        this.playerKills = 0;
        this.playerDeaths = 0;
        this.killStreak = 0;
        this.bestKillStreak = 0;
        this.duelWins = 0;
        this.duelLosses = 0;
        this.duelStreak = 0;
        this.duelBestStreak = 0;
        this.bounty = 0;
    }

    // ================= KILL TRACKING =================
    public Map<String, Integer> getMobKills() { return mobKills; }

    public int getMobKillCount(String mobTypeId) {
        return mobKills.getOrDefault(mobTypeId.toLowerCase(), 0);
    }

    public int getMobKillCountContaining(String keyword) {
        String key = keyword.toLowerCase();
        int total = 0;
        for (Map.Entry<String, Integer> entry : mobKills.entrySet()) {
            if (entry.getKey().contains(key)) {
                total += entry.getValue();
            }
        }
        return total;
    }

    public int getTotalMobKills() {
        return mobKills.values().stream().mapToInt(Integer::intValue).sum();
    }

    public void addMobKill(String mobTypeId) {
        String key = mobTypeId.toLowerCase();
        mobKills.put(key, mobKills.getOrDefault(key, 0) + 1);
    }

    public void addMoney(long amount) {
        if (amount < 0) {
            removeMoney(-amount);
            return;
        }
        this.money += amount;
    }
    public void removeMoney(long amount) { this.money = Math.max(0, this.money - amount); }
}