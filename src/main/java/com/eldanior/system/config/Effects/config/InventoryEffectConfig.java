package com.eldanior.system.config.Effects.config;

import com.eldanior.system.config.enums.ParticleEffectIds;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class InventoryEffectConfig {

    public static final BuilderCodec<InventoryEffectConfig> CODEC = BuilderCodec.builder(InventoryEffectConfig.class, InventoryEffectConfig::new)
            // --- Configuration Ouverture (Spawn) ---
            .append(new KeyedCodec<>("SpawnDistance", Codec.FLOAT),
                    ((config, value) -> config.spawnDistance = value),
                    (InventoryEffectConfig::getSpawnDistance)).add()

            .append(new KeyedCodec<>("OpenParticleId", Codec.STRING),
                    ((config, value) -> config.openParticleId = value),
                    (InventoryEffectConfig::getOpenParticleId)).add()

            .append(new KeyedCodec<>("OpenStatusEffectId", Codec.STRING),
                    ((config, value) -> config.openStatusEffectId = value),
                    (InventoryEffectConfig::getOpenStatusEffectId)).add()

            .append(new KeyedCodec<>("OpenEffectDuration", Codec.FLOAT),
                    ((config, value) -> config.openEffectDuration = value),
                    (InventoryEffectConfig::getOpenEffectDuration)).add()

            // --- Configuration Fermeture (Close) ---
            .append(new KeyedCodec<>("CloseParticleId", Codec.STRING),
                    ((config, value) -> config.closeParticleId = value),
                    (InventoryEffectConfig::getCloseParticleId)).add()

            .build();

    private String openParticleId = ParticleEffectIds.STATUS_EROSION_EFFECT_TEMP.getId();
    private String openStatusEffectId = ParticleEffectIds.STATUS_EROSION_EFFECT_TEMP.getId();
    private float openEffectDuration = 5.0f;
    private String closeParticleId = ParticleEffectIds.STATUS_EROSION_EFFECT_TEMP.getId();
    private float spawnDistance = 1.5f;

    // --- GETTERS MANUELS ---
    public String getOpenParticleId() { return openParticleId; }
    public String getOpenStatusEffectId() { return openStatusEffectId; }
    public float getOpenEffectDuration() { return openEffectDuration; }
    public String getCloseParticleId() { return closeParticleId; }
    public float getSpawnDistance() { return spawnDistance;}
}