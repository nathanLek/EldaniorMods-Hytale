package com.eldanior.system.skills.skills.passives.Epique.Maitrise;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.inventory.ItemStack;

public class SwordMastery implements IPassiveCombatSkill {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override // 🌟 Signature mise à jour avec 5 arguments
    public boolean onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {

        if (attackerRef == null) return false;

        // 1. On récupère le composant Player directement via attackerRef
        Player player = store.getComponent(attackerRef, Player.getComponentType());
        if (player == null) return false;

        // 2. On récupère l'objet en main
        ItemStack mainHandItem = player.getInventory().getActiveHotbarItem();

        // 3. Vérification : Main vide ou pas une épée ?
        if (mainHandItem == null) return false;
        if (!mainHandItem.getItemId().toLowerCase().contains("sword")) return false;

        // --- APPLIQUE LE BONUS (+15%) ---
        float currentDamage = damage.getAmount();
        float newDamage = currentDamage * 1.15f;
        damage.setAmount(newDamage);

        LOGGER.atInfo().log("[Skill] SWORD_MASTERY activé ! " + currentDamage + " -> " + newDamage);

        // --- EFFET VISUEL SUR L'ENNEMI ---
        if (victimRef != null) {
            TransformComponent transform = store.getComponent(victimRef, TransformComponent.getComponentType());
            if (transform != null) {
                Vector3d pos = transform.getPosition().add(0, 1.2, 0);
                ParticleUtil.spawnParticleEffect("Shield_Block", pos, store);
            }
        }
        return false;
    }
}