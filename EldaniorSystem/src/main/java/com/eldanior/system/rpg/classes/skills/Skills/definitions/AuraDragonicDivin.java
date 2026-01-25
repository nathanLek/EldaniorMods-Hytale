package com.eldanior.system.rpg.classes.skills.Skills.definitions;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.rpg.classes.skills.Skills.SkillModel;
import com.eldanior.system.rpg.classes.skills.system.effects.EffectManager;
import com.eldanior.system.components.PlayerLevelData;
import com.eldanior.system.rpg.enums.ClassType;
import com.eldanior.system.rpg.enums.Rarity;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef; // Important
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.List;
import java.util.Random;

public class AuraDragonicDivin extends SkillModel {

    private final Random random = new Random();

    public AuraDragonicDivin() {
        super(
                "aura_dragonic_divin",
                "Cataclysme Draconique",
                "Une tempête de chaos qui foudroie et terrifie tous les ennemis proches.",
                Rarity.DIVINE,
                ClassType.DRAGON,
                10.0f,
                20.0f,
                1.0f,
                true,
                5.0f,
                30.0f,
                List.of("dragon_fear", "lightning_storm")
        );
    }

    /**
     * VISUEL SUR LE LANCEUR (Le Dragon)
     */
    @Override
    public void onTick(Ref<EntityStore> playerRef, Store<EntityStore> store, PlayerLevelData data) {
        TransformComponent trans = store.getComponent(playerRef, TransformComponent.getComponentType());
        if (trans == null) return;

        // CORRECTION : On récupère le monde via PlayerRef, pas TransformComponent
        PlayerRef pRef = store.getComponent(playerRef, PlayerRef.getComponentType());
        if (pRef == null) return;

        World world = Universe.get().getWorld(pRef.getWorldUuid());
        if (world == null) return;

        // Aura violette
        EffectManager.spawnParticle(world, trans.getPosition(), "hytale:fx_dragon_aura", 8);

        // Si Mana > 80%, lueur sacrée
        EntityStatMap stats = store.getComponent(playerRef, EntityStatsModule.get().getEntityStatMapComponentType());
        if (stats != null) {
            var mana = stats.get(DefaultEntityStatTypes.getMana());
            if (mana != null && (mana.get() / mana.getMax()) > 0.8f) {
                EffectManager.spawnParticle(world, trans.getPosition(), "hytale:fx_holy_glow", 3);
            }
        }
    }

    /**
     * LOGIQUE & VISUEL SUR LES CIBLES
     */
    @Override
    public void onAuraTick(Ref<EntityStore> source, Ref<EntityStore> target, Store<EntityStore> store, double distance) {
        PlayerLevelData data = store.getComponent(source, EldaniorSystem.get().getPlayerLevelDataType());
        EntityStatMap sourceStats = store.getComponent(source, EntityStatsModule.get().getEntityStatMapComponentType());
        TransformComponent targetTrans = store.getComponent(target, TransformComponent.getComponentType());

        if (data == null || sourceStats == null || targetTrans == null) return;

        // CORRECTION : On utilise le PlayerRef de la SOURCE (le lanceur) pour trouver le monde
        // Car la cible (monstre) n'a pas forcément de PlayerRef
        PlayerRef sourcePRef = store.getComponent(source, PlayerRef.getComponentType());
        if (sourcePRef == null) return;

        World world = Universe.get().getWorld(sourcePRef.getWorldUuid());
        if (world == null) return;

        // Calcul Puissance
        float rarityMult = getRarityMultiplier();
        float levelMult = 1.0f + (data.getLevel() / 50.0f);
        float manaMult = 1.0f;

        var manaStat = sourceStats.get(DefaultEntityStatTypes.getMana());
        if (manaStat != null && manaStat.getMax() > 0) {
            manaMult = 1.0f + (manaStat.get() / manaStat.getMax());
        }

        float puissanceTotale = rarityMult * levelMult * manaMult;

        // Application Dégâts
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

                    // --- VISUEL D'IMPACT ---
                    EffectManager.spawnParticle(world, targetTrans.getPosition(), "hytale:fx_curse_black", 2);

                    // --- SON & ECLAIR SI PUISSANT ---
                    if (puissanceTotale > 5.0f && random.nextFloat() < 0.20f) {
                        EffectManager.spawnParticle(world, targetTrans.getPosition(), "hytale:fx_lightning_strike", 1);
                        EffectManager.playSound(world, targetTrans.getPosition(), "eldanior:sfx_dragon_thunder", 1.0f, 0.8f + (random.nextFloat() * 0.4f));
                    }
                }
            }
        }
    }
}