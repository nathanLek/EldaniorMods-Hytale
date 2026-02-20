package com.eldanior.system.TreasureChest.resources;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nullable;

public class TreasureChestConfig implements Resource<ChunkStore> {
    public static final BuilderCodec<TreasureChestConfig> CODEC = BuilderCodec.builder(TreasureChestConfig.class, TreasureChestConfig::new)
            .addField(new KeyedCodec<>("CanPlayerBreakLootChests", Codec.BOOLEAN), (data, value) -> data.canPlayerBreakLootChests = value, (data) -> data.canPlayerBreakLootChests)
            .addField(new KeyedCodec<>("IsLootRandom", Codec.BOOLEAN), (data, value) -> data.isLootRandom = value, (data) -> data.isLootRandom)
            .addField(new KeyedCodec<>("IsMessageAppear", Codec.BOOLEAN), (data, value) -> data.isMessageAppear = value, (data) -> data.isMessageAppear)
            .addField(new KeyedCodec<>("IsParticlesAppear", Codec.BOOLEAN), (data, value) -> data.isParticlesAppear = value, (data) -> data.isParticlesAppear)
            .addField(new KeyedCodec<>("ParticlesColor", Codec.STRING), (data, value) -> data.particlesColor = value, (data) -> data.particlesColor)
            .addField(new KeyedCodec<>("NextLootResetInterval", Codec.INTEGER), (data, value) -> data.nextLootResetInterval = value, (data) -> data.nextLootResetInterval)
            .addField(new KeyedCodec<>("NextLootReset", Codec.INTEGER), (data, value) -> data.nextLootReset = value, (data) -> data.nextLootReset)
            .build();
    private boolean canPlayerBreakLootChests;
    private boolean isLootRandom;
    private boolean isMessageAppear;
    private boolean isParticlesAppear;
    private String particlesColor;
    private int nextLootResetInterval;
    private int nextLootReset;

    public TreasureChestConfig() {
        this.canPlayerBreakLootChests = false;
        this.isLootRandom = true;
        this.isMessageAppear = true;
        this.isParticlesAppear = true;
        this.particlesColor = "#ffffff00";
        this.nextLootResetInterval = 0;
        this.nextLootReset = -1;
    }

    public TreasureChestConfig(TreasureChestConfig other) {
        this.canPlayerBreakLootChests = other.canPlayerBreakLootChests;
        this.isLootRandom = other.isLootRandom;
        this.isMessageAppear = other.isMessageAppear;
        this.isParticlesAppear = other.isParticlesAppear;
        this.particlesColor = other.particlesColor;
        this.nextLootResetInterval = other.nextLootResetInterval;
        this.nextLootReset = other.nextLootReset;
    }

    @Nullable
    public Resource<ChunkStore> clone() {
        return new TreasureChestConfig(this);
    }

    public boolean isCanPlayerBreakLootChests() {
        return this.canPlayerBreakLootChests;
    }

    public void setCanPlayerBreakLootChests(boolean new_value) {
        this.canPlayerBreakLootChests = new_value;
    }

    public boolean isLootChestRandom() {
        return this.isLootRandom;
    }

    public void setLootRandom(boolean new_value) {
        this.isLootRandom = new_value;
    }

    public boolean isMessageAppear() {
        return this.isMessageAppear;
    }

    public void setMessageAppear(boolean new_value) {
        this.isMessageAppear = new_value;
    }

    public boolean isParticlesAppear() {
        return this.isParticlesAppear;
    }

    public void setParticlesAppear(boolean new_value) {
        this.isParticlesAppear = new_value;
    }

    public String getParticlesColor() {
        return this.particlesColor;
    }

    public void setParticlesColor(String new_value) {
        this.particlesColor = new_value;
    }

    public int getNextLootResetInterval() {
        return this.nextLootResetInterval;
    }

    public void setNextLootResetInterval(int new_value) {
        this.nextLootResetInterval = new_value;
    }

    public int getNextLootReset() {
        return this.nextLootReset;
    }

    public void setNextLootReset(int new_value) {
        this.nextLootReset = new_value;
    }


}