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

public class PlayerLevelData implements Component<EntityStore> {

    // --- Variables ---
    private int level = 1;
    private int experience = 0;
    private int attributePoints = 0;

    // Identité
    private String playerClass = "Aventurier";
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

    // --- Constructeur par défaut ---
    public PlayerLevelData() {
        this.unlockedTitles.add("Novice");
    }

    public int getRequiredExperience() {
        return (int) (100 + 140 * (level - 1) + 10L * (level - 1) * (level - 1));
    }

    public void addExperience(int amount) {
        this.experience += amount;
        // Boucle au cas où on gagne assez d'XP pour prendre plusieurs niveaux d'un coup
        while (this.experience >= getRequiredExperience()) {
            this.experience -= getRequiredExperience();
            this.level++;
            this.attributePoints += 3; // Bonus : 3 points par niveau
        }
    }

    public float getExperienceProgress() {
        return (float) this.experience / getRequiredExperience();
    }

    // --- LE CODEC CORRIGÉ (MAJUSCULES OBLIGATOIRES POUR LES CLÉS) ---
    public static final BuilderCodec<PlayerLevelData> CODEC = BuilderCodec.builder(PlayerLevelData.class, PlayerLevelData::new)
            // Progression (J'ai mis des Majuscules partout ici)
            .append(new KeyedCodec<>("Level", Codec.INTEGER), (data, v) -> data.level = v, data -> data.level).add()
            .append(new KeyedCodec<>("Experience", Codec.INTEGER), (data, v) -> data.experience = v, data -> data.experience).add()
            .append(new KeyedCodec<>("AttributePoints", Codec.INTEGER), (data, v) -> data.attributePoints = v, data -> data.attributePoints).add()

            // Strings
            .append(new KeyedCodec<>("PlayerClass", Codec.STRING), (data, v) -> data.playerClass = v, data -> data.playerClass).add()
            .append(new KeyedCodec<>("CurrentTitle", Codec.STRING), (data, v) -> data.currentTitle = v, data -> data.currentTitle).add()
            .append(new KeyedCodec<>("GuildRank", Codec.STRING), (data, v) -> data.guildRank = v, data -> data.guildRank).add()

            // Stats
            .append(new KeyedCodec<>("Strength", Codec.INTEGER), (data, v) -> data.strength = v, data -> data.strength).add()
            .append(new KeyedCodec<>("Endurance", Codec.INTEGER), (data, v) -> data.endurance = v, data -> data.endurance).add()
            .append(new KeyedCodec<>("Agility", Codec.INTEGER), (data, v) -> data.agility = v, data -> data.agility).add()
            .append(new KeyedCodec<>("Vitality", Codec.INTEGER), (data, v) -> data.vitality = v, data -> data.vitality).add()
            .append(new KeyedCodec<>("Intelligence", Codec.INTEGER), (data, v) -> data.intelligence = v, data -> data.intelligence).add()
            .append(new KeyedCodec<>("Luck", Codec.INTEGER), (data, v) -> data.luck = v, data -> data.luck).add()

            // Argent
            .append(new KeyedCodec<>("Money", Codec.LONG), (data, v) -> data.money = v, data -> data.money).add()

            // Liste de titres
            .append(new KeyedCodec<>("UnlockedTitles", Codec.STRING), (data, value) -> {
                data.unlockedTitles = new ArrayList<>();
                if (value != null && !value.isEmpty()) {
                    data.unlockedTitles.addAll(Arrays.asList(value.split(",")));
                }
            }, (data) -> {
                if (data.unlockedTitles != null && !data.unlockedTitles.isEmpty()) {
                    return String.join(",", data.unlockedTitles);
                } else {
                    return "";
                }
            }).add()
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
        copy.currentTitle = this.currentTitle;
        copy.guildRank = this.guildRank;

        copy.strength = this.strength;
        copy.endurance = this.endurance;
        copy.agility = this.agility;
        copy.vitality = this.vitality;
        copy.intelligence = this.intelligence;
        copy.luck = this.luck;
        copy.money = this.money;

        if (this.unlockedTitles != null) {
            copy.unlockedTitles = new ArrayList<>(this.unlockedTitles);
        }
        return copy;
    }

    // ================= GETTERS =================
    public int getLevel() { return level; }
    public int getExperience() { return experience; }
    public int getAttributePoints() { return attributePoints; }
    public String getPlayerClass() { return playerClass; }
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
    public void setCurrentTitle(String title) { this.currentTitle = title; }
    public void setGuildRank(String guildRank) { this.guildRank = guildRank; }

    public void addTitle(String title) {
        if (this.unlockedTitles == null) this.unlockedTitles = new ArrayList<>();
        if (!this.unlockedTitles.contains(title)) {
            this.unlockedTitles.add(title);
        }
    }

    public void setStrength(int strength) { this.strength = strength; }
    public void setEndurance(int endurance) { this.endurance = endurance; }
    public void setAgility(int agility) { this.agility = agility; }
    public void setVitality(int vitality) { this.vitality = vitality; }
    public void setIntelligence(int intelligence) { this.intelligence = intelligence; }
    public void setLuck(int luck) { this.luck = luck; }
    public void setMoney(long money) { this.money = money; }

    public void addMoney(long amount) { this.money += amount; }
    public void removeMoney(long amount) { this.money = Math.max(0, this.money - amount); }
}