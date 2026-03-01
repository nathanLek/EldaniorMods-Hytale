package com.eldanior.system.skills.skills.passives;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.inventory.ItemStack;

public class SwordMastery implements IPassiveCombatSkill {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public void onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> victimRef) {

        // 1. On récupère la référence de l'attaquant
        Damage.Source source = damage.getSource();
        if (!(source instanceof Damage.EntitySource entitySource)) return;
        Ref<EntityStore> attackerRef = entitySource.getRef();

        // 2. On récupère le composant Player de l'attaquant
        Player player = store.getComponent(attackerRef, Player.getComponentType());
        if (player == null) return;

        // 3. 🌟 ON UTILISE LA MÉTHODE PARFAITE DE TON API !
        ItemStack mainHandItem = player.getInventory().getItemInHand();

        // Si la main est vide ou que l'objet n'est pas une épée, on annule
        if (mainHandItem == null) return;
        if (!mainHandItem.getItemId().toLowerCase().contains("sword")) return;

        // --- APPLIQUE LE BONUS ---
        float currentDamage = damage.getAmount();
        damage.setAmount(currentDamage * 1.15f); // +15% de dégâts

        LOGGER.atInfo().log("[Skill] SWORD_MASTERY activé ! Dégâts modifiés : " + damage.getAmount());

        // --- EFFET VISUEL SUR L'ENNEMI ---
        if (victimRef != null) {
            TransformComponent transform = store.getComponent(victimRef, TransformComponent.getComponentType());

            if (transform != null) {
                Vector3d pos = transform.getPosition().add(0, 1.0, 0);
                ParticleUtil.spawnParticleEffect("Shield_Block", pos, store);
            }
        }
    }
}