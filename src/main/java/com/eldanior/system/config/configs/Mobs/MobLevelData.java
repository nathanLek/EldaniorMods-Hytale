package com.eldanior.system.config.configs.Mobs;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jetbrains.annotations.Nullable;

public class MobLevelData implements Component<EntityStore> {

    public static final BuilderCodec<MobLevelData> CODEC = BuilderCodec.builder(MobLevelData.class, MobLevelData::new)
            .append(new KeyedCodec<>("Level", Codec.INTEGER),  // MAJUSCULE !
                    (obj, val) -> obj.level = val,
                    obj -> obj.level)
            .add()
            .append(new KeyedCodec<>("MobTypeId", Codec.STRING),  // MAJUSCULE !
                    (obj, val) -> obj.mobTypeId = val,
                    obj -> obj.mobTypeId)
            .add()
            .append(new KeyedCodec<>("StatsApplied", Codec.BOOLEAN),  // MAJUSCULE !
                    (obj, val) -> obj.statsApplied = val,
                    obj -> obj.statsApplied)
            .add()
            .build();

    private int level;
    private String mobTypeId;
    private boolean statsApplied;

    public MobLevelData() {
        this.level = 1;
        this.mobTypeId = "";
        this.statsApplied = false;
    }

    public MobLevelData(int level, String mobTypeId, boolean statsApplied) {
        this.level = level;
        this.mobTypeId = mobTypeId;
        this.statsApplied = statsApplied;
    }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public String getMobTypeId() { return mobTypeId; }
    public void setMobTypeId(String mobTypeId) { this.mobTypeId = mobTypeId; }

    public boolean isStatsApplied() { return statsApplied; }
    public void setStatsApplied(boolean statsApplied) { this.statsApplied = statsApplied; }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        MobLevelData copy = new MobLevelData();
        copy.level = this.level;
        copy.mobTypeId = this.mobTypeId;
        copy.statsApplied = this.statsApplied;
        return copy;
    }
}