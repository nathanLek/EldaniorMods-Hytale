package com.eldanior.system.rpg.classes.skills.Skills.definitions;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.components.PlayerLevelData;
import com.eldanior.system.rpg.classes.skills.Skills.ToggledSkill; // Changement ici !
import com.eldanior.system.rpg.classes.skills.Skills.config.AuraDragonicConfig;
import com.eldanior.system.rpg.classes.skills.Skills.effects.EffectManager;
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

import java.util.Random;

public class AuraDragonicDivin extends ToggledSkill { // Hérite de ToggledSkill

    private final Random random = new Random();
    private final AuraDragonicConfig config;

    public AuraDragonicDivin(AuraDragonicConfig config) {
        super(
                "aura_dragonic_divin",
                "Aura Draconique",
                "Une tempête de chaos qui foudroie et terrifie tous les ennemis proches.",
                Rarity.DIVINE,
                ClassType.DRAGON,
                5.0f,   // Cooldown (si on la désactive/réactive)
                1.0f,   // Coût Mana/sec
                1.0f,   // Intervalle de tick (toutes les 1 seconde)
                20.0f   // Rayon
        );
        this.config = config != null ? config : new AuraDragonicConfig();
    }

    @Override
    public void onTick(Ref<EntityStore> playerRef, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer, PlayerLevelData data) {
        // La vérification "isSkillEnabled" est déjà faite par le système qui appelle onTick,
        // mais on peut la garder par sécurité ou pour des effets visuels spécifiques.

        TransformComponent trans = store.getComponent(playerRef, TransformComponent.getComponentType());
        if (trans == null) return;

        // Effet visuel sur le joueur
        EffectManager.spawnParticle(config.getAuraParticle(), trans.getPosition(), store);

        // Effet "Holy Glow" si mana élevé
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
        if (data == null) return;

        TransformComponent targetTrans = store.getComponent(target, TransformComponent.getComponentType());
        EntityStatMap targetStats = store.getComponent(target, EntityStatsModule.get().getEntityStatMapComponentType());

        if (targetTrans == null || targetStats == null) return;

        // Calcul des dégâts
        float puissanceTotale = getRarityMultiplier() * (1.0f + (data.getLevel() / 50.0f));
        float finalDamage = 10.0f * puissanceTotale * (1.0f - (float)(distance / getRadius()));

        int hpIdx = DefaultEntityStatTypes.getHealth();
        var hp = targetStats.get(hpIdx);

        if (hp != null && hp.get() > 0 && finalDamage > 1.0f) {
            targetStats.setStatValue(hpIdx, Math.max(0, hp.get() - finalDamage));

            // Effets d'impact
            EffectManager.spawnParticle(config.getImpactParticle(), targetTrans.getPosition(), store);

            if (random.nextFloat() < 0.20f) {
                EffectManager.spawnParticle(config.getLightningParticle(), targetTrans.getPosition(), store);
                EffectManager.playSound(config.getSoundId(), targetTrans.getPosition(), 1.0f, 1.0f, commandBuffer);
            }
        }
    }
}