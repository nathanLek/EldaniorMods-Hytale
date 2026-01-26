package com.eldanior.system.rpg.classes.skills.Skills.definitions;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.rpg.classes.skills.Skills.SkillModel;
import com.eldanior.system.rpg.classes.skills.system.effects.EffectManager;
import com.eldanior.system.rpg.classes.skills.system.config.AuraDragonicConfig; // Import de la config
import com.eldanior.system.components.PlayerLevelData;
import com.eldanior.system.rpg.enums.ClassType;
import com.eldanior.system.rpg.enums.Rarity;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.List;
import java.util.Random;

public class AuraDragonicDivin extends SkillModel {

    private final Random random = new Random();
    private final AuraDragonicConfig config; // Instance de config

    public AuraDragonicDivin(AuraDragonicConfig config) {
        super(
                "aura_dragonic_divin",
                "Cataclysme Draconique",
                "Une tempête de chaos qui foudroie et terrifie tous les ennemis proches.",
                Rarity.DIVINE,
                ClassType.DRAGON,
                20.0f,
                30.0f,
                1.0f,
                true,
                5.0f,
                10.0f,
                List.of("dragon_fear", "lightning_storm")
        );
        this.config = config != null ? config : new AuraDragonicConfig();
    }

    @Override
    public void onTick(Ref<EntityStore> playerRef, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer, PlayerLevelData data) {
        TransformComponent trans = store.getComponent(playerRef, TransformComponent.getComponentType());
        if (trans == null) return;

        // Utilisation de la config pour la particule d'aura
        EffectManager.spawnParticle(config.getAuraParticle(), trans.getPosition(), store);

        EntityStatMap stats = store.getComponent(playerRef, EntityStatsModule.get().getEntityStatMapComponentType());
        if (stats != null) {
            var mana = stats.get(DefaultEntityStatTypes.getMana());
            if (mana != null && (mana.get() / mana.getMax()) > 0.8f) {
                EffectManager.spawnParticle("hytale:fx_holy_glow", trans.getPosition(), store);
            }
        }
    }

    @Override
    public void onAuraTick(Ref<EntityStore> source, Ref<EntityStore> target, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer, double distance) {
        PlayerLevelData data = store.getComponent(source, EldaniorSystem.get().getPlayerLevelDataType());
        EntityStatMap sourceStats = store.getComponent(source, EntityStatsModule.get().getEntityStatMapComponentType());
        TransformComponent targetTrans = store.getComponent(target, TransformComponent.getComponentType());

        if (data == null || sourceStats == null || targetTrans == null) return;

        float rarityMult = getRarityMultiplier();
        float levelMult = 1.0f + (data.getLevel() / 50.0f);
        float manaMult = 1.0f;

        var manaStat = sourceStats.get(DefaultEntityStatTypes.getMana());
        if (manaStat != null && manaStat.getMax() > 0) {
            manaMult = 1.0f + (manaStat.get() / manaStat.getMax());
        }

        float puissanceTotale = rarityMult * levelMult * manaMult;

        EntityStatMap targetStats = store.getComponent(target, EntityStatsModule.get().getEntityStatMapComponentType());
        if (targetStats != null) {
            int hpIdx = DefaultEntityStatTypes.getHealth();
            var hp = targetStats.get(hpIdx);

            if (hp != null && hp.get() > 0) {
                float baseDmg = 10.0f;
                float ratioDistance = 1.0f - (float)(distance / getRadius());
                float finalDamage = baseDmg * puissanceTotale * ratioDistance;

                if (finalDamage > 1.0f) {
                    targetStats.setStatValue(hpIdx, Math.max(0, hp.get() - finalDamage));

                    // Particule d'impact via config
                    EffectManager.spawnParticle(config.getImpactParticle(), targetTrans.getPosition(), store);

                    if (puissanceTotale > 5.0f && random.nextFloat() < 0.20f) {
                        // Particule d'éclair via config
                        EffectManager.spawnParticle(config.getLightningParticle(), targetTrans.getPosition(), store);

                        // Son via config
                        EffectManager.playSound(
                                config.getSoundId(),
                                targetTrans.getPosition(),
                                1.0f,
                                0.8f + (random.nextFloat() * 0.4f),
                                commandBuffer
                        );
                    }
                }
            }
        }
    }
}