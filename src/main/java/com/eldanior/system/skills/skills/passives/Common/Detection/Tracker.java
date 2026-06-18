package com.eldanior.system.skills.skills.passives.Common.Detection;

import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.Effects.EffectsManager;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class Tracker implements IPassiveCombatSkill {

    private static final float TRACKING_BONUS = 1.25f;
    private static final float TRACKING_BONUS_MASTERED = 1.275f;
    private static final String TRACKING_EFFECT = "Elda_Tracking";

    private boolean lastProc = false;

    @Override
    public boolean didProc() { return lastProc; }

    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.TRACKING_EVIDENCE) {
            return TRACKING_BONUS;
        }
        return 1.0f;
    }

    @Override
    public void onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        lastProc = false;
        if (victimRef == null || !victimRef.isValid()) return;

        // Appliquer l'effet de tracking sur la victime (15s)
        boolean applied = EffectsManager.applyEffect(victimRef, TRACKING_EFFECT, store);
        if (applied) {
            lastProc = true;

            if (attackerRef != null) {
                PlayerRef playerRef = store.getComponent(attackerRef, PlayerRef.getComponentType());
                if (playerRef != null) {
                    NotificationHelper.sendNotification(playerRef,
                            "<color:#ff6600>Cible marquee !</color>", NotificationStyle.Success);
                }
            }
        }
    }
    // Progression aussi gérée par DetectionSystem (chaque message radar)
}
