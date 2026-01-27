package com.eldanior.system.rpg.classes.skills.Skills.definitions;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.rpg.classes.skills.Skills.SkillModel;
import com.eldanior.system.rpg.classes.skills.system.effects.EffectManager;
import com.eldanior.system.rpg.classes.skills.system.config.AuraDragonicConfig;
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
    private final AuraDragonicConfig config;

    public AuraDragonicDivin(AuraDragonicConfig config) {
        super(
                "aura_dragonic_divin",
                "Aura Draconique",
                "Une tempête de chaos qui foudroie et terrifie tous les ennemis proches.",
                Rarity.DIVINE,
                ClassType.DRAGON,
                20.0f,  // Rayon
                0.0f,   // Durée 0 = Infini (Toggle)
                1.0f,   // Coût Mana/sec
                true,   // Reste un passif de classe
                5.0f,   // Cooldown
                10.0f,  // XP Gain
                List.of("dragon_fear", "lightning_storm")
        );
        this.config = config != null ? config : new AuraDragonicConfig();
    }

    @Override
    public void onTick(Ref<EntityStore> playerRef, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer, PlayerLevelData data) {
        // SÉCURITÉ : Ne rien faire si le skill n'est pas activé dans la télécommande
        if (data == null || !data.isSkillEnabled(this.getId())) return;

        TransformComponent trans = store.getComponent(playerRef, TransformComponent.getComponentType());
        if (trans == null) return;

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
        // Sécurité supplémentaire sur l'aura tick
        if (data == null || !data.isSkillEnabled(this.getId())) return;

        EntityStatMap sourceStats = store.getComponent(source, EntityStatsModule.get().getEntityStatMapComponentType());
        TransformComponent targetTrans = store.getComponent(target, TransformComponent.getComponentType());

        if (sourceStats == null || targetTrans == null) return;

        float puissanceTotale = getRarityMultiplier() * (1.0f + (data.getLevel() / 50.0f));

        EntityStatMap targetStats = store.getComponent(target, EntityStatsModule.get().getEntityStatMapComponentType());
        if (targetStats != null) {
            int hpIdx = DefaultEntityStatTypes.getHealth();
            var hp = targetStats.get(hpIdx);
            if (hp != null && hp.get() > 0) {
                float finalDamage = 10.0f * puissanceTotale * (1.0f - (float)(distance / getRadius()));
                if (finalDamage > 1.0f) {
                    targetStats.setStatValue(hpIdx, Math.max(0, hp.get() - finalDamage));
                    EffectManager.spawnParticle(config.getImpactParticle(), targetTrans.getPosition(), store);
                    if (random.nextFloat() < 0.20f) {
                        EffectManager.spawnParticle(config.getLightningParticle(), targetTrans.getPosition(), store);
                        EffectManager.playSound(config.getSoundId(), targetTrans.getPosition(), 1.0f, 1.0f, commandBuffer);
                    }
                }
            }
        }
    }
}