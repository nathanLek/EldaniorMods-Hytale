package com.eldanior.system.config.configs.Mobs;

public interface IMobConfig {
    String getKeyword();
    int getXp();
    int getMinLevel();
    int getMaxLevel();
    boolean isInvincible();
    String getCustomTitle();
}