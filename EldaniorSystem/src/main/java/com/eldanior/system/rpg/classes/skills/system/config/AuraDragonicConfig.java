package com.eldanior.system.rpg.classes.skills.system.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class AuraDragonicConfig {

    public static final BuilderCodec<AuraDragonicConfig> CODEC = BuilderCodec.builder(AuraDragonicConfig.class, AuraDragonicConfig::new)
            .append(new KeyedCodec<>("AuraParticle", Codec.STRING),
                    ((config, val) -> config.auraParticle = val),
                    (AuraDragonicConfig::getAuraParticle)).add()
            .append(new KeyedCodec<>("ImpactParticle", Codec.STRING),
                    ((config, val) -> config.impactParticle = val),
                    (AuraDragonicConfig::getImpactParticle)).add()
            .append(new KeyedCodec<>("LightningParticle", Codec.STRING),
                    ((config, val) -> config.lightningParticle = val),
                    (AuraDragonicConfig::getLightningParticle)).add()
            .append(new KeyedCodec<>("SoundId", Codec.STRING),
                    ((config, val) -> config.soundId = val),
                    (AuraDragonicConfig::getSoundId)).add()
            .build();

    // Valeurs par défaut utilisant les noms d'assets que tu as fournis
    private String auraParticle = "Lightning";
    private String impactParticle = "Void_Dragon_Effects";
    private String lightningParticle = "Lightning";
    private String soundId = "SFX_Dragon_Sleep";

    // Getters manuels
    public String getAuraParticle() { return auraParticle; }
    public String getImpactParticle() { return impactParticle; }
    public String getLightningParticle() { return lightningParticle; }
    public String getSoundId() { return soundId; }
}