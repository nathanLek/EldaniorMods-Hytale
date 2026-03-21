package com.eldanior.system.skills.skills.passives.Divin.Defense;

import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.HashMap;
import java.util.Map;

public class DynaAegis implements IPassiveCombatSkill {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    // CORRECTION : On utilise directement PlayerRef au lieu de l'UUID qui faisait crasher
    private final Map<PlayerRef, Long> invincibilityTimers = new HashMap<>();
    private final Map<PlayerRef, Long> cooldowns = new HashMap<>();

    // Temps de la compétence
    private static final long INVINCIBILITY_DURATION = 10 * 1000L; // 10 secondes
    private static final long COOLDOWN_DURATION = 2 * 60 * 1000L; // 2 minutes

    @Override
    public void onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled() || victimRef == null) return;

        // 🛡️ NOUVEAU : Un bloc Try/Catch pour empêcher le monstre de disparaître en cas d'erreur
        try {
            PlayerRef playerRef = store.getComponent(victimRef, PlayerRef.getComponentType());
            if (playerRef == null) return;

            long currentTime = System.currentTimeMillis();

            // --- 1. LE JOUEUR EST-IL DÉJÀ INVINCIBLE ? ---
            if (invincibilityTimers.containsKey(playerRef) && currentTime < invincibilityTimers.get(playerRef)) {
                damage.setCancelled(true);
                return;
            }

            // --- 2. LA COMPÉTENCE EST-ELLE EN COOLDOWN ? ---
            if (cooldowns.containsKey(playerRef) && currentTime < cooldowns.get(playerRef)) {
                return;
            }

            // --- 3. VÉRIFICATION DE LA VIE VIA LA STATMAP ---
            EntityStatMap statMap = store.getComponent(victimRef, EntityStatsModule.get().getEntityStatMapComponentType());

            if (statMap != null) {
                // ✅ CORRECTION : On utilise l'ID officiel de ta configuration VITALITY !
                EntityStatValue healthStat = statMap.get(StatConfig.VITALITY.getStatId());

                if (healthStat != null) {
                    float maxHealth = healthStat.getMax();
                    float currentHealth = healthStat.get();

                    float expectedHealth = currentHealth - damage.getAmount();

                    if (expectedHealth <= (maxHealth * 0.5f)) {

                        // 🌟 ACTIVATION DU MIRACLE !
                        invincibilityTimers.put(playerRef, currentTime + INVINCIBILITY_DURATION);
                        cooldowns.put(playerRef, currentTime + COOLDOWN_DURATION);

                        damage.setCancelled(true);

                        NotificationHelper.sendNotification(playerRef,
                                "<color:aqua>Égide de Dyna activée ! Vous êtes invincible (10s) ! ✦</color>",
                                NotificationStyle.Success);

                        TransformComponent transform = store.getComponent(victimRef, TransformComponent.getComponentType());
                        if (transform != null) {
                            Vector3d pos = transform.getPosition().add(0, 1.0, 0);
                            ParticleUtil.spawnParticleEffect("Divine_Shield_Burst", pos, store);
                        }
                    }
                } else {
                    LOGGER.atWarning().log("[DynaAegis] Impossible de trouver la stat VITALITY dans la statMap.");
                }
            }
        } catch (Exception e) {
            LOGGER.atWarning().log("[DynaAegis] Error Critique.");
            e.printStackTrace();
        }
    }
}