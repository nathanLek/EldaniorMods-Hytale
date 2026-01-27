package com.eldanior.system.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

public class PlayerLevelData implements Component<EntityStore> {

    // --- Variables ---
    private int level = 1;
    private int experience = 0;
    private int attributePoints = 0;

    // Identité
    private String playerClass = "Aventurier";
    private String classId = "none";

    private String currentTitle = "Novice";
    private List<String> unlockedTitles = new ArrayList<>();
    private String guildRank = "F";

    // Stats
    private int strength = 1;
    private int endurance = 1;
    private int agility = 1;
    private int vitality = 1;
    private int intelligence = 1;
    private int luck = 1;

    // Économie
    private long money = 1000;

    // --- Liste des compétences activées (Télécommande) ---
    private List<String> unlockedSkills = new ArrayList<>();
    private Set<String> enabledSkills = new HashSet<>();

    // --- NOUVEAU : Gestion Transitoire (Cooldowns) ---
    // "transient" signifie que ces données ne sont pas sauvegardées dans la base de données
    // (on reset les cooldowns si le serveur redémarre)
    private transient Map<String, Long> cooldowns = new HashMap<>();

    public PlayerLevelData() {
        this.unlockedTitles.add("Novice");
    }

    public int getRequiredExperience() {
        return (int) (100 + 140 * (level - 1) + 10L * (level - 1) * (level - 1));
    }

    public void addExperience(int amount) {
        this.experience += amount;
        while (this.experience >= getRequiredExperience()) {
            this.experience -= getRequiredExperience();
            this.level++;
            this.attributePoints += 3;
        }
    }

    public float getExperienceProgress() {
        return (float) this.experience / getRequiredExperience();
    }

    // --- LOGIQUE DE TÉLÉCOMMANDE (Compétences à bascule) ---
    public boolean toggleSkill(String skillId) {
        if (this.enabledSkills.contains(skillId)) {
            this.enabledSkills.remove(skillId);
            return false; // Désactivé
        } else {
            this.enabledSkills.add(skillId);
            return true; // Activé
        }
    }

    public boolean isSkillEnabled(String skillId) {
        return this.enabledSkills.contains(skillId);
    }

    // --- NOUVEAU : LOGIQUE DE COMBAT (Cooldowns & Mana) ---

    /**
     * Vérifie si le joueur a assez de mana.
     * @param amount Le coût en mana.
     * @return true si le joueur peut payer le coût.
     */
    public boolean hasEnoughMana(float amount) {
        // TODO: Connecter cela au système de stats réel (EntityStatMap)
        // Pour l'instant, on retourne toujours true pour faciliter les tests.
        return true;
    }

    /**
     * Vérifie si la compétence est disponible (pas en cooldown).
     */
    public boolean canCast(String skillId) {
        if (!cooldowns.containsKey(skillId)) return true;
        return System.currentTimeMillis() >= cooldowns.get(skillId);
    }

    /**
     * Applique un cooldown à une compétence.
     * @param durationSeconds La durée en secondes avant réutilisation.
     */
    public void applyCooldown(String skillId, float durationSeconds) {
        long readyTime = System.currentTimeMillis() + (long)(durationSeconds * 1000);
        cooldowns.put(skillId, readyTime);
    }

    /**
     * Récupère le temps restant avant la fin du cooldown (en millisecondes).
     */
    public long getRemainingCooldown(String skillId) {
        if (!cooldowns.containsKey(skillId)) return 0;
        return Math.max(0, cooldowns.get(skillId) - System.currentTimeMillis());
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
            .append(new KeyedCodec<>("Strength", Codec.INTEGER), (data, v) -> data.strength = v, data -> data.strength).add()
            .append(new KeyedCodec<>("Endurance", Codec.INTEGER), (data, v) -> data.endurance = v, data -> data.endurance).add()
            .append(new KeyedCodec<>("Agility", Codec.INTEGER), (data, v) -> data.agility = v, data -> data.agility).add()
            .append(new KeyedCodec<>("Vitality", Codec.INTEGER), (data, v) -> data.vitality = v, data -> data.vitality).add()
            .append(new KeyedCodec<>("Intelligence", Codec.INTEGER), (data, v) -> data.intelligence = v, data -> data.intelligence).add()
            .append(new KeyedCodec<>("Luck", Codec.INTEGER), (data, v) -> data.luck = v, data -> data.luck).add()
            .append(new KeyedCodec<>("Money", Codec.LONG), (data, v) -> data.money = v, data -> data.money).add()
            // CODEC pour EnabledSkills (Set -> String CSV)
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
            .append(new KeyedCodec<>("UnlockedTitles", Codec.STRING), (data, value) -> {
                data.unlockedTitles = new ArrayList<>();
                if (value != null && !value.isEmpty()) {
                    data.unlockedTitles.addAll(Arrays.asList(value.split(",")));
                }
            }, (data) -> String.join(",", data.unlockedTitles)).add()
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
        copy.strength = this.strength;
        copy.endurance = this.endurance;
        copy.agility = this.agility;
        copy.vitality = this.vitality;
        copy.intelligence = this.intelligence;
        copy.luck = this.luck;
        copy.money = this.money;
        copy.enabledSkills = new HashSet<>(this.enabledSkills); // Copie du Set

        if (this.unlockedTitles != null) {
            copy.unlockedTitles = new ArrayList<>(this.unlockedTitles);
        }
        // Pas besoin de cloner 'cooldowns' car c'est transitoire
        copy.cooldowns = new HashMap<>();
        return copy;
    }

    public List<String> getUnlockedSkills() {
        return unlockedSkills;
    }

    public void learnSkill(String skillId) {
        if (!unlockedSkills.contains(skillId)) {
            unlockedSkills.add(skillId);
        }
    }

    public void forgetAllSkills() {
        unlockedSkills.clear();
        enabledSkills.clear();
    }

    // ================= GETTERS =================
    public int getLevel() { return level; }
    public int getExperience() { return experience; }
    public int getAttributePoints() { return attributePoints; }
    public String getPlayerClass() { return playerClass; }
    public String getPlayerClassId() { return classId; }
    public String getCurrentTitle() { return currentTitle; }
    public List<String> getUnlockedTitles() { return unlockedTitles; }
    public String getGuildRank() { return guildRank; }
    public int getStrength() { return strength; }
    public int getEndurance() { return endurance; }
    public int getAgility() { return agility; }
    public int getVitality() { return vitality; }
    public int getIntelligence() { return intelligence; }
    public int getLuck() { return luck; }
    public long getMoney() { return money; }
    public int getMaxMana() { return intelligence * 10; }
    public int getMaxHealth() { return vitality * 10; }

    // ================= SETTERS =================
    public void setLevel(int level) { this.level = level; }
    public void setExperience(int experience) { this.experience = experience; }
    public void setAttributePoints(int points) { this.attributePoints = points; }
    public void setPlayerClass(String playerClass) { this.playerClass = playerClass; }
    public void setPlayerClassId(String classId) { this.classId = classId; }
    public void setCurrentTitle(String title) { this.currentTitle = title; }
    public void setGuildRank(String guildRank) { this.guildRank = guildRank; }
    public void setStrength(int strength) { this.strength = strength; }
    public void setEndurance(int endurance) { this.endurance = endurance; }
    public void setAgility(int agility) { this.agility = agility; }
    public void setVitality(int vitality) { this.vitality = vitality; }
    public void setIntelligence(int intelligence) { this.intelligence = intelligence; }
    public void setLuck(int luck) { this.luck = luck; }
    public void setMoney(long money) { this.money = money; }

    public void addTitle(String title) {
        if (this.unlockedTitles == null) this.unlockedTitles = new ArrayList<>();
        if (!this.unlockedTitles.contains(title)) {
            this.unlockedTitles.add(title);
        }
    }

    public void addMoney(long amount) { this.money += amount; }
    public void removeMoney(long amount) { this.money = Math.max(0, this.money - amount); }
}