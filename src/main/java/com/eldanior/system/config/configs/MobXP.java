package com.eldanior.system.config.configs;

import com.eldanior.system.config.configs.Mobs.IMobConfig;
import com.eldanior.system.config.configs.Mobs.Mobs.*; // Importe tes sous-fichiers

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MobXP {

    private static final List<IMobConfig> ALL_MOBS = new ArrayList<>();

    public static final IMobConfig DEFAULT = new IMobConfig() {
        @Override public String getKeyword() { return "default"; }
        @Override public int getXp() { return 10; }
        @Override public int getMinLevel() { return 1; }
        @Override public int getMaxLevel() { return 50; }
        @Override public boolean isInvincible() { return false; }
        @Override public String getCustomTitle() { return null; }
    };

    static {
        register(AnimalData.values());
        register(AquaticData.values());
        register(BossData.values());
        register(defaultData.values());
        register(DinosaureData.values());
        register(DragonData.values());
        register(ElementalData.values());
        register(FenData.values());
        register(FeranData.values());
        register(GoblinData.values());
        register(GolemData.values());
        register(KweebecData.values());
        register(NpcData.values());
        register(OtherMobData.values());
        register(OutlandersData.values());
        register(SaurianData.values());
        register(ScaraksData.values());
        register(SkeletonData.values());
        register(SlothianData.values());
        register(TrorkData.values());
        register(VoidData.values());
        register(ZombieData.values());
    }

    private static void register(IMobConfig[] categoryMobs) {
        ALL_MOBS.addAll(Arrays.asList(categoryMobs));
    }

    public static int getXpForId(String typeId) {
        if (typeId == null) return DEFAULT.getXp();
        String cleanId = typeId.replace("_", " ").toLowerCase();

        for (IMobConfig mob : ALL_MOBS) {
            if (cleanId.contains(mob.getKeyword())) {
                return mob.getXp();
            }
        }
        return DEFAULT.getXp();
    }

    public static IMobConfig getMobDataForId(String typeId) {
        if (typeId == null) return DEFAULT;
        String cleanId = typeId.replace("_", " ").toLowerCase();

        for (IMobConfig mob : ALL_MOBS) {
            if (cleanId.contains(mob.getKeyword().replace("_", " ").toLowerCase())) {
                return mob;
            }
        }
        return DEFAULT;
    }

    public static int getMinLevelForId(String typeId) {
        if (typeId == null) return DEFAULT.getMinLevel();
        String cleanId = typeId.replace("_", " ").toLowerCase();

        for (IMobConfig mob : ALL_MOBS) {
            if (cleanId.contains(mob.getKeyword())) {
                return mob.getMinLevel();
            }
        }
        return DEFAULT.getMinLevel();
    }

    public static int getMaxLevelForId(String typeId) {
        if (typeId == null) return DEFAULT.getMaxLevel();
        String cleanId = typeId.replace("_", " ").toLowerCase();

        for (IMobConfig mob : ALL_MOBS) {
            if (cleanId.contains(mob.getKeyword())) {
                return mob.getMaxLevel();
            }
        }
        return DEFAULT.getMaxLevel();
    }
}