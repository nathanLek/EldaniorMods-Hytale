package com.eldanior.system.skills.skills.passives.Rare.Defense;

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
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

// 🌟 Import nécessaire pour le PlayerRef
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class IronSkin implements IPassiveCombatSkill {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public boolean onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {

        if (damage.isCancelled()) return false;

        float currentDamage = damage.getAmount();
        float newDamage = currentDamage * 0.90f;
        damage.setAmount(newDamage);

        LOGGER.atInfo().log("[Skill] IRON_SKIN activé ! Dégâts réduits de " + currentDamage + " à " + newDamage);

        if (victimRef != null) {

            // --- 🌟 NOTIFICATION VISUELLE ---
            // On extrait le composant PlayerRef de l'entité
            PlayerRef playerRef = store.getComponent(victimRef, PlayerRef.getComponentType());

            // Si l'entité qui se fait taper est bien un joueur, on lui affiche le texte !
            if (playerRef != null) {
                NotificationHelper.sendNotification(playerRef, "<color:gray>Peau d'Acier : -10% de dégâts</color>", NotificationStyle.Success);
            }

            // --- APPARITION DE LA PARTICULE ---
            TransformComponent transform = store.getComponent(victimRef, TransformComponent.getComponentType());

            if (transform != null) {
                Vector3d pos = transform.getPosition().add(0, 1.0, 0);
                ParticleUtil.spawnParticleEffect("Shield_Block", pos, store);
            }
        }
        return false;
    }
}