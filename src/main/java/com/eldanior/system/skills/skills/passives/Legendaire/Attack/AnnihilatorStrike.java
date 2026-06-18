package com.eldanior.system.skills.skills.passives.Legendaire.Attack;

import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class AnnihilatorStrike implements IPassiveCombatSkill {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final float CHANCE = 0.22f; // 22% de chance
    private static final float BONUS_MULTIPLIER = 1.75f; // +75% dégâts
    private static final float DOUBLE_CAST_CHANCE = 0.25f; // 25% de chance de doubler l'impact

    @Override
    public boolean onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {

        if (Math.random() < CHANCE) {
            float currentDamage = damage.getAmount();
            float newDamage = currentDamage * BONUS_MULTIPLIER;

            // Vérification du Double Impact (Écho)
            boolean isEcho = Math.random() < DOUBLE_CAST_CHANCE;
            if (isEcho) {
                newDamage *= 2.0f; // On double encore les dégâts pour l'effet légendaire
            }

            damage.setAmount(newDamage);

            LOGGER.atInfo().log("[Skill] ANNIHILATOR_STRIKE : " + (isEcho ? "DOUBLE IMPACT! " : "") + newDamage + " DMG");

            if (attackerRef != null) {
                PlayerRef playerRef = store.getComponent(attackerRef, PlayerRef.getComponentType());

                if (playerRef != null) {
                    String msg = isEcho ? "<color:#ffcc00><b>ANNIHILATION : DOUBLE IMPACT !!!</b></color>"
                            : "<color:#ffcc00><b>FRAPPE DE L'ANNIHILATEUR : +75% </b></color>";
                    NotificationHelper.sendNotification(playerRef, msg, NotificationStyle.Success);
                }
            }
        }
        return false;
    }
}