package com.eldanior.system.rpg.classes.skills.Skills.definitions;

import com.eldanior.system.rpg.classes.skills.Skills.InstantSkill;
import com.eldanior.system.rpg.classes.skills.Skills.effects.EffectManager;
import com.eldanior.system.rpg.enums.ClassType;
import com.eldanior.system.rpg.enums.Rarity;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.modules.projectile.config.ProjectileConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class FireballSkill extends InstantSkill {

    public FireballSkill() {
        super(
                "fireball_skill",
                "Boule de Feu",
                "Lance un projectile brûlant.",
                Rarity.COMMON,
                ClassType.MAGE,
                2.0f,  // 2 secondes de cooldown
                15.0f  // 15 de mana
        );
    }

    @Override
    public boolean cast(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer) {
        TransformComponent transform = store.getComponent(caster, TransformComponent.getComponentType());
        HeadRotation headRot = store.getComponent(caster, HeadRotation.getComponentType());

        if (transform == null || headRot == null) return false;

        // 1. Position et Direction
        Vector3d startPos = transform.getPosition().clone().add(0, 1.6, 0);
        double yawRad = Math.toRadians(-headRot.getRotation().getYaw() - 90);
        double pitchRad = Math.toRadians(-headRot.getRotation().getPitch());
        double x = Math.cos(yawRad) * Math.cos(pitchRad);
        double y = Math.sin(pitchRad);
        double z = Math.sin(yawRad) * Math.cos(pitchRad);
        Vector3d direction = new Vector3d(x, y, z).normalize();

        // 2. PROJECTILE : Utiliser le tien !
        String projectileId = "eldaniorsystem:fireball_config"; // <--- CHANGEMENT ICI
        ProjectileConfig config = ProjectileConfig.getAssetMap().getAsset(projectileId);

        if (config == null) {
            System.out.println("[Eldanior] ERREUR : Config '" + projectileId + "' introuvable !");
            return false;
        }

        // 3. Vitesse manuelle
        double speed = 1.5;
        Vector3d velocity = new Vector3d(direction.x * speed, direction.y * speed, direction.z * speed);

        // 4. Tir
        ProjectileModule.get().spawnProjectile(caster, commandBuffer, config, startPos, velocity);
        EffectManager.playSound("hytale:sfx/magic/fire_cast", startPos, 1.0f, 1.0f, commandBuffer);
        System.out.println("[Eldanior] BOUM ! Boule de feu lancée !");

        return true;
    }
}